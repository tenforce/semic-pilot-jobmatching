package tools;

import com.tenforce.core.io.FileHelper;
import com.tenforce.core.net.EncodeHelper;
import com.tenforce.core.xml.DomHelper;
import com.tenforce.exception.TenforceException;
import com.tenforce.sesame.SesameUtils;
import eu.esco.demo.jobcvmatching.root.SemicRdfModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.intellij.lang.annotations.Language;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Vdab2Html {
  private static final String baseUri = "http://data.europa.eu/esco/semic/document/cv/vdab/";
  private static final File outputFolder = new File("C:\\Temp\\esco\\extFolder\\semic\\vdab_cv");
  //  private static final File outputFolder = new File("C:\\Temp\\esco\\extFolder\\semic\\documents");
  private static final File inputFolder = new File("C:\\Projects\\ESCO\\semic\\vdab-cvs\\20150413_CV_VDAB_hrxml");
  private static final File nutsRdf = new File("C:\\Projects\\ESCO\\git\\esco-xl\\ext-v01\\ingest\\taxonomy\\support\\NUTS.rdf");
  private static RepositoryConnection nutsRepositoryConnection;
  private static ValueFactory vf;


  public static void main(String[] args) throws Exception {

    Repository nutsRepository = SesameUtils.createTempRepository();
    SesameUtils.loadInto(nutsRdf, RDFFormat.RDFXML, nutsRepository, "http://data.europa.eu/esco/ConceptScheme/NUTS2008/");
    nutsRepositoryConnection = nutsRepository.getConnection();
    vf = nutsRepository.getValueFactory();


//    new Vdab2Html(new File(inputFolder, "18.xml")).createFile();
    for (File file : inputFolder.listFiles()) {
      TenforceException.when(!file.getName().endsWith(".xml"), "Not expected filename: {}", file);
      new Vdab2Html(file).createFile();
    }

    nutsRepositoryConnection.close();
    nutsRepository.shutDown();
  }

  private static String findNuts(String nutsUri) throws Exception {
    RepositoryResult<Statement> statements = nutsRepositoryConnection.getStatements(vf.createURI(nutsUri), SKOS.PREF_LABEL, null, false);
    if (!statements.hasNext()) return "";

    return statements.next().getObject().stringValue();
  }

  private final Document document;
  private final XPath xPath = XPathFactory.newInstance().newXPath();
  private final Document htmlSnippet = DomHelper.newDocument();
  private final Element rootDiv;
  private final String documentUri;

  public Vdab2Html(File file) throws Exception {

    String id = StringUtils.substringBeforeLast(file.getName(), ".");

    System.out.println(id);

    this.document = DomHelper.read(FileHelper.readFileToString(file, "UTF-8"));
    rootDiv = (Element) htmlSnippet.appendChild(htmlSnippet.createElement("div"));

    documentUri = baseUri + id;
    rootDiv.setAttribute("about", documentUri);
    rootDiv.setAttribute("typeof", SemicRdfModel.typeCV);
  }

  public void createFile() {
    try {
//      setUserInfo();
//      setCompetences();
//      setExperience();
//      setEducation();
//      setLocations();
      setWantedJobs();

//      Transformer transformer = TransformerFactory.newInstance().newTransformer();
//      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
//      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
//      transformer.transform(new DOMSource(htmlSnippet), new StreamResult(new File(outputFolder, EncodeHelper.encode(documentUri) + ".html")));

    }
    catch (Exception e) {
      throw new TenforceException(e, "Create file said kaboum");
    }
  }

  private void setUserInfo() throws Exception {
    addElementWithText("h2", "Personal information");

    Element list = addElement("ul");
    Element personalDataElement = xpathElement(document, "//candidateType/CandidateProfile[ProfileName/text()='PersonalData']/PersonalData");
    addElementWithText(list, "li", "personId: " + xpathString(personalDataElement, "PersonId/IdValue/text()"));
    addElementWithText(list, "li", "personGivenName: " + xpathString(personalDataElement, "PersonName/GivenName/text()"));
    addElementWithText(list, "li", "personPreferredGivenName: " + xpathString(personalDataElement, "PersonName/PreferredGivenName/text()"));
    addElementWithText(list, "li", "personFamilyName: " + xpathString(personalDataElement, "PersonName/FamilyName/text()"));
    addElementWithText(list, "li", "personPostalCode: " + xpathString(personalDataElement, "ContactMethod/PostalAddress/PostalCode/text()"));
  }

  private void setLocations() throws Exception {
    addElementWithText("h2", "Locations");

    Element foundNutsTable = createTableElement("URI", "Label");
    Element otherNutsTable = createTableElement("Value", "URI", "Label");

    Set<String> values = new HashSet<>(xpathStringList(document, "//PhysicalLocation/Area/Value/text()"));
    for (String value : values) {
      String nutsUri = "http://data.europa.eu/esco/ConceptScheme/NUTS2008/c." + value;
      String nutsLabel = findNuts(nutsUri);
      if (StringUtils.isNotBlank(nutsLabel)) {
        Element foundNutsRow = addElement(foundNutsTable, "tr");
        Element locationTd = addElementWithText(foundNutsRow, "td", nutsUri);
        locationTd.setAttribute("rel", SemicRdfModel.propertyLocation);
        locationTd.setAttribute("resource", nutsUri);
        addElementWithText(foundNutsRow, "td", nutsLabel);
        continue;
      }
      Pair<String, String> strippedNuts = findStrippedNuts(value);
      Element otherNutsRow = addElement(otherNutsTable, "tr");
      addElementWithText(otherNutsRow, "td", value);
      Element locationTd = addElementWithText(otherNutsRow, "td", null == strippedNuts ? "" : strippedNuts.getLeft());
      if (null != strippedNuts) {
        locationTd.setAttribute("rel", SemicRdfModel.propertyLocation);
        locationTd.setAttribute("resource", strippedNuts.getLeft());
      }
      addElementWithText(otherNutsRow, "td", null == strippedNuts ? "" : strippedNuts.getRight());
    }

    addElementWithText("h3", "Found locations");
    rootDiv.appendChild(foundNutsTable);
    addElementWithText("h3", "Other locations");
    rootDiv.appendChild(otherNutsTable);
  }

  private void setCompetences() throws Exception {
    addElementWithText("h2", "Competences");

    Element competenceTable = addTableElement("Name", "TaxonomyId", "TaxonomyDescription");
//    for (Node competenceNode : xpathNodeList(document,  "//candidateType/CandidateProfile[ProfileName/text()='competenceprofile']/PreferredPosition/Competency")) {
    for (Node competenceNode : xpathNodeList(document, "//Competency")) {
      addTableRow(competenceTable, xpathString(competenceNode, "@name"), xpathString(competenceNode, "TaxonomyId/@id"), xpathString(competenceNode, "TaxonomyId/@description"));
    }
  }

  private void setExperience() throws Exception {
    addElementWithText("h2", "Experience");

    Element competenceTable = addTableElement("Title", "For organisation");
//    for (Node positionHistoryNode : xpathNodeList(document, "//candidateType/CandidateProfile[ProfileName/text()='competenceprofile']/PreferredPosition/Competency")) {
    for (Node positionHistoryNode : xpathNodeList(document, "//PositionHistory")) {
      addTableRow(competenceTable, xpathString(positionHistoryNode, "Title/text()"), xpathString(positionHistoryNode, "OrgName/OrganizationName"));
    }
  }

  private void setEducation() throws Exception {
    addElementWithText("h2", "Education");

    Element educationList = addElement("ul");
    for (String degreeName : xpathStringList(document, "//Degree/DegreeName/text()")) {
      addElementWithText(educationList, "li", degreeName);
    }
  }

  private void setWantedJobs() throws Exception {
    addElementWithText("h2", "Wanted jobs");

    Element table = addTableElement("Industry code", "Position title");
    for (Node node : xpathNodeList(document, "//candidateType/CandidateProfile[ProfileName/text()='wantedjob']/PreferredPosition")) {
      addTableRow(table, xpathString(node, "IndustryCode/text()"), xpathString(node, "PositionTitle/text()"));
      System.out.println("    " + xpathString(node, "PositionTitle/text()"));
    }
  }

  private Pair<String, String> findStrippedNuts(String value) throws Exception {
    while ((value = StringUtils.substring(value, 0, -1)).length() > 0) {
      String nutsUri = "http://data.europa.eu/esco/ConceptScheme/NUTS2008/c." + value;
      String nutsLabel = findNuts(nutsUri);
      if (StringUtils.isBlank(nutsLabel)) continue;
      return new ImmutablePair<>(nutsUri, nutsLabel);
    }
    return null;
  }

  private List<Node> xpathNodeList(Object source, @Language("XPath") String xpath) throws Exception {
    NodeList nodeList = (NodeList) xPath.compile(xpath).evaluate(source, XPathConstants.NODESET);
    List<Node> result = new ArrayList<>();
    for (int i = 0; i < nodeList.getLength(); i++) {
      result.add(nodeList.item(i));
    }
    return result;
  }

  private Element xpathElement(Object source, @Language("XPath") String xpath) throws Exception {
    return (Element) xPath.compile(xpath).evaluate(source, XPathConstants.NODE);
  }

  private String xpathString(Object source, @Language("XPath") String xpath) throws Exception {
    return xPath.compile(xpath).evaluate(source);
  }

  private List<String> xpathStringList(Object source, @Language("XPath") String xpath) throws Exception {
    List<Node> nodes = xpathNodeList(source, xpath);
    List<String> result = new ArrayList<>();
    for (Node node : nodes) {
      result.add(node.getTextContent());
    }
    return result;
  }

  private Element addElement(String tagName) {
    return addElement(rootDiv, tagName);
  }

  private Element addElement(Element parent, String tagName) {
    return (Element) parent.appendChild(htmlSnippet.createElement(tagName));
  }

  private Element addElementWithText(String tagName, String textContent) {
    return addElementWithText(rootDiv, tagName, textContent);
  }

  private Element addElementWithText(Element parent, String tagName, String textContent) {
    Element newElement = addElement(parent, tagName);
    newElement.setTextContent(textContent);
    return newElement;
  }

  private Element createTableElement(String... headers) {
    Element table = htmlSnippet.createElement("table");
    table.setAttribute("class", "table table-condensed table-striped table-hover");
    Element tableHeaderRow = addElement(table, "tr");
    for (String header : headers) {
      addElementWithText(tableHeaderRow, "th", header);
    }
    return table;
  }

  private Element addTableElement(String... headers) {
    Element tableElement = createTableElement(headers);
    rootDiv.appendChild(tableElement);
    return tableElement;
  }

  private void addTableRow(Element tableElement, String... values) {
    Element row = addElement(tableElement, "tr");
    for (String value : values) {
      addElementWithText(row, "td", value);
    }
  }
}
