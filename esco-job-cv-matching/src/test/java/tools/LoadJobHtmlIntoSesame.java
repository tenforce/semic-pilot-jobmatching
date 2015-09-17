package tools;

import com.tenforce.core.io.FileExtensionFilter;
import com.tenforce.core.io.FileHelper;
import com.tenforce.exception.TenforceException;
import eu.esco.demo.jobcvmatching.root.SchemaOrgModel;
import eu.esco.demo.jobcvmatching.root.SemicRdfModel;
import eu.esco.demo.jobcvmatching.root.SemicRootConfiguration;
import eu.esco.demo.jobcvmatching.root.model.EmploymentType;
import eu.esco.demo.jobcvmatching.root.model.ExperienceLevel;
import eu.esco.demo.jobcvmatching.root.service.DocumentService;
import eu.esco.demo.jobcvmatching.root.service.GeonamesMapping;
import eu.esco.demo.jobcvmatching.root.service.LinkedinIndustryMapping;
import org.apache.any23.Any23;
import org.apache.any23.extractor.html.TagSoupParser;
import org.apache.any23.mime.MIMEType;
import org.apache.any23.mime.MIMETypeDetector;
import org.apache.any23.source.DocumentSource;
import org.apache.any23.source.StringDocumentSource;
import org.apache.any23.writer.RepositoryWriter;
import org.apache.any23.writer.TripleHandler;
import org.apache.any23.writer.TripleHandlerException;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.Language;
import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.inject.Inject;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class LoadJobHtmlIntoSesame {


  @Inject
  private GeonamesMapping geonamesMapping;
  @Inject
  private LinkedinIndustryMapping linkedinIndustryMapping;
  @Inject
  private DocumentService documentService;

  public static void main(String[] args) throws Exception {
    LoadJobHtmlIntoSesame loadJobHtmlIntoSesame = new LoadJobHtmlIntoSesame();
    ConfigurableApplicationContext context = new SpringApplicationBuilder(SemicRootConfiguration.class).web(false).run(args);
    try {
      context.getAutowireCapableBeanFactory().autowireBean(loadJobHtmlIntoSesame);

      int count = 0;
      File[] htmlFiles = new File("C:\\Temp\\esco\\semic\\jobSearch\\jobs_20150309_01").listFiles(new FileExtensionFilter("html"));
      for (File htmlFile : htmlFiles) {
        String html = FileHelper.readFileToString(htmlFile, "UTF-8");
        String fileName = htmlFile.getName();
        String id = StringUtils.substringBeforeLast(fileName, ".");
        String subjectUri = "https://www.linkedin.com/jobs2/view/" + id;

        RepositoryConnection connection = loadJobHtmlIntoSesame.parseRdfa(html, subjectUri).getConnection();
        loadJobHtmlIntoSesame.checkValues(connection, getDomDocument(html, subjectUri), subjectUri);
        connection.close();

        count++;
//        if(count > 5) break;
      }
    }
    finally {
      context.close();
    }

  }

  private static Document getDomDocument(String html, String baseUri) {
    try {
      TagSoupParser tagSoupParser = new TagSoupParser(new ByteArrayInputStream(html.getBytes("UTF-8")), baseUri);
      return tagSoupParser.getDOM();
    }
    catch (IOException e) {
      throw new TenforceException(e);
    }
  }

  private Element getElement(Document document, @Language("XPath2") String xpath, String name) throws Exception {
    XPath xPath = XPathFactory.newInstance().newXPath();
    NodeList nodes = (NodeList) xPath.compile(xpath).evaluate(document.getDocumentElement(), XPathConstants.NODESET);

    if (nodes.getLength() == 0) return null;
    if (nodes.getLength() > 1) throw new TenforceException("More than 1 {} found", name);
    return ((Element) nodes.item(0));
  }


  private ExperienceLevel getExperienceLevel(Document document) throws Exception {
    Element element = getElement(document, "//DIV[@itemprop='experienceRequirements']", "experienceRequirements");
    return null == element ? null : ExperienceLevel.fromHtml(element.getTextContent());
  }

  private EmploymentType getEmploymentType(Document document) throws Exception {
    Element element = getElement(document, "//DIV[@itemprop='employmentType']", "employmentType");
    return null == element ? null : EmploymentType.fromHtml(element.getTextContent());
  }

  private String getAddressLocality(Document document) throws Exception {
    String addressLocality = getAddressLocalityByPostalAddress(document);
    return StringUtils.isBlank(addressLocality) ? getAddressLocalityByDescription(document) : addressLocality;
  }

  private String getAddressLocalityByPostalAddress(Document document) throws Exception {
    Element element = getElement(document, "//SPAN[@itemprop='address' and @itemtype='http://schema.org/PostalAddress']/META[@itemprop='addressLocality']", "addressLocality");
    return null == element ? "" : element.getAttribute("content");
  }

  private String getAddressLocalityByDescription(Document document) throws Exception {
    Element element = getElement(document, "//SPAN[@itemprop='jobLocation' and @itemtype='http://schema.org/Place']/SPAN[@itemprop='description']", "addressLocality description");
    String content = null == element ? "" : element.getTextContent();
    return null == content ? "" : content;
  }

  public void checkValues(RepositoryConnection connection, Document document, String subjectUri) throws Exception {
    Repository newDataRepository = new SailRepository(new MemoryStore());
    newDataRepository.initialize();
    RepositoryConnection newData = newDataRepository.getConnection();

    ValueFactory vf = connection.getValueFactory();
    URI subject = vf.createURI(subjectUri);
    String title = connection.getStatements(subject, DCTERMS.TITLE, null, false).next().getObject().stringValue();
    RepositoryResult<Statement> jobPostingStatements = connection.getStatements(null, RDF.TYPE, vf.createURI(SchemaOrgModel.typeJobPosting), false);
    if (!jobPostingStatements.hasNext()) {
      System.out.println("No jobposting found in " + subjectUri);
      return;
    }

    Resource jobPosting = jobPostingStatements.next().getSubject();
    String occupationalCategory = getFirstValue(connection, jobPosting, SchemaOrgModel.propertyJobPostingOccupationalCategory);
    String jobDescription = getFirstValue(connection, jobPosting, SchemaOrgModel.propertyJobPostingDescription);
    String industry = getFirstValue(connection, jobPosting, SchemaOrgModel.propertyJobPostingIndustry);

    RepositoryResult<Statement> hiringOrganizationStatements = connection.getStatements(jobPosting, vf.createURI(SchemaOrgModel.propertyJobPostingHiringOrganization), null, false);
    String hiringOrganizationName = "";
    if (hiringOrganizationStatements.hasNext()) {
      Resource hiringOrganization = (Resource) hiringOrganizationStatements.next().getObject();
      hiringOrganizationName = getFirstValue(connection, hiringOrganization, SchemaOrgModel.propertyOrganizationName);
    }

    System.out.println(subjectUri);
    System.out.println("   title: " + title);
    System.out.println("   occupationalCategory: " + occupationalCategory);
    System.out.println("   jobDescription: " + jobDescription);
    System.out.println("   industry: " + industry);
    System.out.println("   hiringOrganizationName: " + hiringOrganizationName);

    String addressLocality = getAddressLocality(document);
    System.out.println("   getAddressLocality: " + addressLocality);
    GeonamesMapping.Info geonamesInfo = geonamesMapping.getInfo(addressLocality);
    System.out.println("   postcode: " + (null == geonamesInfo ? "" : geonamesInfo.getPostcode()));
    System.out.println("   nuts: " + (null == geonamesInfo ? "" : geonamesInfo.getNutsUri()));
    System.out.println("   geonamesId: " + (null == geonamesInfo ? "" : geonamesInfo.getGeonamesId()));

    EmploymentType employmentType = TenforceException.failIfNull(getEmploymentType(document), "No employmentType for " + subjectUri);
    System.out.println("   employmentType: " + employmentType);

    ExperienceLevel experienceLevel = TenforceException.failIfNull(getExperienceLevel(document), "No experienceLevel for " + subjectUri);
    System.out.println("   experienceLevel: " + experienceLevel);


    LinkedinIndustryMapping.LinkedinIndustry2NaceUris naceUris = linkedinIndustryMapping.getNaceUris(industry);
    System.out.println("   NACE uris: " + naceUris.getNaceUris());
    System.out.println("   industry not found as nace labels: " + naceUris.getNotFoundLabels());

    newData.add(subject, RDF.TYPE, vf.createURI(SemicRdfModel.typeJV));
    newData.add(subject, SKOS.PREF_LABEL, vf.createLiteral(title));
    newData.add(subject, DCTERMS.DESCRIPTION, vf.createLiteral(jobDescription));
    newData.add(subject, vf.createURI(SemicRdfModel.propertyHiringOrganizationName), vf.createLiteral(hiringOrganizationName));
    for (String naceUri : naceUris.getNaceUris()) {
      newData.add(subject, vf.createURI(SemicRdfModel.propertyHasNace), vf.createURI(naceUri));
    }
    newData.add(subject, vf.createURI(SemicRdfModel.propertyEmploymentType), vf.createLiteral(employmentType.name()));
    newData.add(subject, vf.createURI(SemicRdfModel.propertyExperienceLevel), vf.createLiteral(experienceLevel.name()));
    if (StringUtils.isNotBlank(addressLocality)) {
      newData.add(subject, vf.createURI(SemicRdfModel.propertyGeoname), vf.createLiteral(addressLocality));
    }
    if (null != geonamesInfo) {
      newData.add(subject, vf.createURI(SemicRdfModel.propertyPostcode), vf.createLiteral(geonamesInfo.getPostcode()));
      newData.add(subject, vf.createURI(SemicRdfModel.propertyLocation), vf.createURI(geonamesInfo.getNutsUri()));
      if (null != geonamesInfo.getGeonamesId()) {
        URI geonamesUri = vf.createURI("http://sws.geonames.org/" + geonamesInfo.getGeonamesId() + "/");
        newData.add(subject, vf.createURI(SemicRdfModel.propertyGeonameUri), geonamesUri);

        BNode locationNode = vf.createBNode();
        newData.add(subject, vf.createURI(SemicRdfModel.propertyLocationCore), locationNode);
        newData.add(locationNode, RDF.TYPE, DCTERMS.LOCATION);
        newData.add(locationNode, RDFS.SEEALSO, geonamesUri);
        newData.add(locationNode, vf.createURI(SemicRdfModel.coreVocabularyPropertyGeographicName), vf.createLiteral(addressLocality));
      }
    }

    newData.add(subject, vf.createURI(SemicRdfModel.propertyOccupationalCategory), vf.createLiteral(occupationalCategory));
    documentService.updateRdf(subjectUri, newData);

    newData.close();
    newDataRepository.shutDown();
  }

  private String getFirstValue(RepositoryConnection connection, Resource subject, String propertyUri) throws Exception {
    RepositoryResult<Statement> industryStatements = connection.getStatements(subject, connection.getValueFactory().createURI(propertyUri), null, false);
    return industryStatements.hasNext() ? industryStatements.next().getObject().stringValue() : "";
  }

  private Repository parseRdfa(String html, String baseUri) {
    Any23 runner = new Any23();
    runner.setMIMETypeDetector(HtmlMIMETypeDetector.instance);
    DocumentSource source = new StringDocumentSource(html, baseUri);
    Repository repository = new SailRepository(new MemoryStore());
    TripleHandler handler = null;
    try {
      repository.initialize();
      handler = new RepositoryWriter(repository.getConnection());
      runner.extract(source, handler);
    }
    catch (Exception e) {
      throw TenforceException.rethrow(e);
    }
    finally {
      try {
        if (null != handler) handler.close();
      }
      catch (TripleHandlerException ignore) {
      }
    }
    return repository;
  }

  private static class HtmlMIMETypeDetector implements MIMETypeDetector {
    private static final HtmlMIMETypeDetector instance = new HtmlMIMETypeDetector();
    private static final MIMEType mimeType = MIMEType.parse("text/html");
    //    private static final MIMEType mimeType = MIMEType.parse("application/xhtml+xml");

    @Override
    public MIMEType guessMIMEType(String fileName, InputStream input, MIMEType mimeTypeFromMetadata) {
      return mimeType;
    }
  }

}
