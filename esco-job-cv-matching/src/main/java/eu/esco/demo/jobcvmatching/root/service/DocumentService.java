package eu.esco.demo.jobcvmatching.root.service;

import com.tenforce.core.config.ExtConfigService;
import com.tenforce.core.io.FileExtensionFilter;
import com.tenforce.core.io.FileHelper;
import com.tenforce.core.net.EncodeHelper;
import com.tenforce.core.xml.DocumentContentHandler;
import com.tenforce.core.xml.DomHelper;
import com.tenforce.exception.TenforceException;
import com.tenforce.sesame.SesameUtils;
import com.tenforce.sesame.template.SesameDatasource;
import eu.esco.demo.jobcvmatching.root.SemicRdfModel;
import org.apache.any23.Any23;
import org.apache.any23.mime.MIMEType;
import org.apache.any23.mime.MIMETypeDetector;
import org.apache.any23.source.DocumentSource;
import org.apache.any23.source.StringDocumentSource;
import org.apache.any23.writer.RepositoryWriter;
import org.apache.any23.writer.TripleHandler;
import org.apache.any23.writer.TripleHandlerException;
import org.apache.commons.lang3.StringUtils;
import org.ccil.cowan.tagsoup.Parser;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.inject.Inject;
import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DocumentService {
  @Inject
  private SesameDatasource sesameDatasource;
  @Inject
  private ExtConfigService extConfigService;

  public boolean hasDocumentContent(String uri) {
    return null != getDocumentFile(uri);
  }

  private Set<String> findUrisByType(String type) {
    RepositoryConnection connection = null;
    try {
      connection = sesameDatasource.getConnection();
      RepositoryResult<Statement> statements = connection.getStatements(null, RDF.TYPE, connection.getValueFactory().createURI(type), false);
      Set<String> uris = new HashSet<>();
      while(statements.hasNext()) {
        uris.add(statements.next().getSubject().stringValue());
      }
      return uris;
    }
    catch (Exception e) {
      throw TenforceException.rethrow(e);
    }
    finally {
      SesameUtils.closeQuietly(connection);
    }
  }

  public List<String> findNotLoadedDocuments() {
    File[] documents = new File(extConfigService.getMainGenericExtRootFolder(), "documents").listFiles(new FileExtensionFilter("html"));
    if(null == documents) return Collections.emptyList();

    Set<String> allUris = new HashSet<>();
    allUris.addAll(findUrisByType(SemicRdfModel.typeCV));
    allUris.addAll(findUrisByType(SemicRdfModel.typeJV));

    List<String> result = new ArrayList<>();
    for (File document : documents) {
      String uri = EncodeHelper.decode(StringUtils.substringBeforeLast(document.getName(), "."));
      if(!allUris.contains(uri)) result.add(uri);
    }
    return result;

  }

  public String getDocumentContent(String uri) {
    File documentFile = TenforceException.failIfNull(getDocumentFile(uri), "Document with id {} not found", uri);
    return FileHelper.readFileToString(documentFile, "UTF-8");
  }

  public void addOccupation(String uri, String occupationUri) throws RepositoryException {
    RepositoryConnection destinationConnection = sesameDatasource.getConnection();
    try {
      ValueFactory vf = destinationConnection.getValueFactory();
      URI subjectUri = vf.createURI(uri);
      destinationConnection.add(subjectUri, vf.createURI(SemicRdfModel.propertyHasOccupation_toUseForAdd), vf.createURI(occupationUri), subjectUri);
      destinationConnection.commit();
    }
    catch (RepositoryException e) {
      destinationConnection.rollback();
      throw e;
    }
    finally {
      destinationConnection.close();
    }
  }

  public void addSkill(String uri, String skillUri) throws RepositoryException {
    RepositoryConnection destinationConnection = sesameDatasource.getConnection();
    try {
      ValueFactory vf = destinationConnection.getValueFactory();
      URI subjectUri = vf.createURI(uri);
      destinationConnection.add(subjectUri, vf.createURI(SemicRdfModel.propertyHasSkill_toUseForAdd), vf.createURI(skillUri), subjectUri);
      destinationConnection.commit();
    }
    catch (RepositoryException e) {
      destinationConnection.rollback();
      throw e;
    }
    finally {
      destinationConnection.close();
    }
  }

  public void updateDocument(String uri, String content) {
    Repository repository = parseRdfa(content, uri);
    RepositoryConnection connection = null;
    try {
      connection = repository.getConnection();
      updateRdf(uri, connection);
    }
    catch (RepositoryException e) {
      throw TenforceException.rethrow(e);
    }
    finally {
      SesameUtils.closeQuietly(connection);
      SesameUtils.shutDownQuietly(repository);
    }
    File documentFile = TenforceException.failIfNull(getDocumentFile(uri), "Document with id {} not found - cannot update non-existing document", uri);
    FileHelper.writeStringToFile(documentFile, content, "UTF-8");
  }

  public void updateRdf(String documentUri, RepositoryConnection newData) throws RepositoryException {
    RepositoryConnection destinationConnection = sesameDatasource.getConnection();

    try {
      RepositoryResult<Statement> newDataStatements = newData.getStatements(null, null, null, false);

      URI graph = destinationConnection.getValueFactory().createURI(documentUri);
      URI semicGraph = destinationConnection.getValueFactory().createURI(SemicRdfModel.namespace);
      URI semicConceptType = destinationConnection.getValueFactory().createURI(SemicRdfModel.typeSemicConcept);
      destinationConnection.remove((Resource) null, null, null, graph);

      while (newDataStatements.hasNext()) {
        Statement next = newDataStatements.next();
        String subject = next.getSubject().stringValue();
        if (!subject.startsWith(SemicRdfModel.skillBaseUri) && !subject.startsWith(SemicRdfModel.occupationBaseUri)) {
          destinationConnection.add(next, graph);
        }
        else if (!destinationConnection.hasStatement(next.getSubject(), RDF.TYPE, semicConceptType, false, semicGraph)) {
          destinationConnection.add(next, semicGraph);
          destinationConnection.add(next.getSubject(), RDF.TYPE, semicConceptType, semicGraph);
        }
      }
      destinationConnection.commit();
    }
    catch (RepositoryException e) {
      destinationConnection.rollback();
      throw e;
    }
    finally {
      destinationConnection.close();
    }
  }

  private File getDocumentFile(String uri) {
    File document = new File(extConfigService.getMainGenericExtRootFolder(), "documents/" + EncodeHelper.encode(uri) + ".html");
    return document.isFile() ? document : null;
  }

  private Repository parseRdfa(String html, String baseUri) {
    Any23 runner = new Any23();
    runner.setMIMETypeDetector(HtmlMIMETypeDetector.instance);
    DocumentSource source = new StringDocumentSource(xmlizeHtml(html), baseUri);
    Repository repository = new SailRepository(new MemoryStore());
    TripleHandler handler = null;
    RepositoryConnection connection = null;
    try {
      repository.initialize();
      connection = repository.getConnection();
      handler = new RepositoryWriter(connection);
      runner.extract(source, handler);
    }
    catch (Exception e) {
      throw TenforceException.rethrow(e);
    }
    finally {
      SesameUtils.closeQuietly(connection);
      try {
        if (null != handler) handler.close();
      }
      catch (TripleHandlerException ignore) {
      }
    }
    return repository;
  }

  private String xmlizeHtml(String html) {
    if (StringUtils.isBlank(html)) return "";
    try {
      Parser parser = new Parser();
      parser.setFeature(Parser.rootBogonsFeature, true);
      parser.setFeature(Parser.defaultAttributesFeature, false);

      DocumentContentHandler contentHandler = DomHelper.newDocumentContentHandler();
      parser.setContentHandler(contentHandler);
      parser.parse(new InputSource(new StringReader(html)));
      Document document = contentHandler.getDocument();

      String xhtml = DomHelper.toString(document);
      xhtml = StringUtils.substringAfter(xhtml, "<body>");
      return StringUtils.substringBeforeLast(xhtml, "</body>");
    }
    catch (Throwable e) {
      throw new TenforceException(e, "Tagsoup failed: {}", e.getMessage());
    }
  }

  private static class HtmlMIMETypeDetector implements MIMETypeDetector {
    private static final HtmlMIMETypeDetector instance = new HtmlMIMETypeDetector();
    private static final MIMEType mimeType = MIMEType.parse("text/html");

    @Override
    public MIMEType guessMIMEType(String fileName, InputStream input, MIMEType mimeTypeFromMetadata) {
      return mimeType;
    }
  }

  public static void main(String[] args) throws Exception {
    System.out.println(EncodeHelper.encode("http://data.europa.eu/esco/semic/document/cv/1"));
  }

}
