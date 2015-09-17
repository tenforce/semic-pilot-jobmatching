package tools;

import com.tenforce.core.io.FileHelper;
import com.tenforce.core.xml.DocumentContentHandler;
import com.tenforce.core.xml.DomHelper;
import com.tenforce.exception.TenforceException;
import info.aduna.iteration.Iterations;
import org.apache.any23.Any23;
import org.apache.any23.extractor.html.TagSoupParser;
import org.apache.any23.extractor.microdata.MicrodataParser;
import org.apache.any23.extractor.microdata.MicrodataParserReport;
import org.apache.any23.mime.MIMEType;
import org.apache.any23.mime.MIMETypeDetector;
import org.apache.any23.source.DocumentSource;
import org.apache.any23.source.StringDocumentSource;
import org.apache.any23.writer.RepositoryWriter;
import org.apache.any23.writer.TripleHandler;
import org.apache.any23.writer.TripleHandlerException;
import org.apache.commons.lang3.StringUtils;
import org.ccil.cowan.tagsoup.Parser;
import org.openrdf.model.Statement;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.List;

public class LinkedInCrawler {

  public static void main(String[] args) throws Exception {


//    Pair<StatusLine, String> statusLineStringPair = new HttpClientFactory(null).executeStringGet("https://www.linkedin.com/jobs2/view/29081227");
//    String html = statusLineStringPair.getRight();

    String baseUri = "https://www.linkedin.com/jobs2/view/12444166";
    File file = new File("C:\\Temp\\esco\\semic\\jobSearch\\jobs\\12444166-bis.html");
    String html = FileHelper.readFileToString(file, "UTF-8");

    if (true) {
      Document doc = new LinkedInCrawler().getDom(html, "https://www.linkedin.com/jobs2/view/12444166");
      XPath xPath = XPathFactory.newInstance().newXPath();
//      NodeList nodes = (NodeList) xPath.evaluate("//span[@itemprop='address' and @itemtype='http://schema.org/PostalAddress']", doc.getDocumentElement(), XPathConstants.NODESET);
//      NodeList nodes = (NodeList) xPath.evaluate("/span", doc.getDocumentElement(), XPathConstants.NODESET);
      NodeList nodes = (NodeList) xPath.compile("//SPAN[@itemprop='address' and @itemtype='http://schema.org/PostalAddress']/META[@itemprop='addressLocality']").evaluate(doc.getDocumentElement(), XPathConstants.NODESET);
//      itemprop="address" itemscope itemtype="http://schema.org/PostalAddress"

      System.out.println("---- " + nodes.getLength());
      for (int i = 0; i < nodes.getLength(); ++i) {
        Element e = (Element) nodes.item(i);
       System.out.println(e.getAttribute("content"));
      }
      System.out.println("----");

      return;
    }


//    new LinkedInCrawler().microdata(html, "https://www.linkedin.com/jobs2/view/12444166");
//    if (true) return;

//    int index = 0;
//    while ((index = html.indexOf("&", index)) != -1) {
//      int end = html.indexOf(";", index);
//      if (index + 7 >= end) {
//        html = html.substring(0, index) + "X" + html.substring(end + 1);
//      }
//      else {
//        index++;
//      }
//    }
//    html = html.replace("<META", "<meta");
//    html = fixCloseTag(html, "meta");
//    html = fixCloseTag(html, "link");
//    html = fixCloseTag(html, "input");
//    html = fixCloseTag(html, "img");
//    html = fixCloseTag(html, "br");
//    html = fixCloseTag(html, "ecirc");

//    File file = new File("C:\\Temp\\linkedinJob_2.html");
//    File file = new File("C:\\Temp\\linkedinJob.html.xml");
//    String html = FileHelper.readFileToString(file, "UTF-8");
    Repository repository = new LinkedInCrawler().parseRdfa(html, baseUri);
//    Repository repository = new LinkedInCrawler().parseRdfa("<div>" + testHtml + "</div>");
    RepositoryResult<Statement> statements = repository.getConnection().getStatements(null, null, null, false);
    List<Statement> statementsList = Iterations.asList(statements);
    System.out.println("----");
    int count = 0;
    for (Statement statement : statementsList) {
      count++;
      System.out.println(statement.getSubject());
      System.out.println("      " + statement.getPredicate() + "    ->    " + statement.getObject());
    }
    System.out.println("----");
    System.out.println("Count: " + count);

  }

  private MicrodataParserReport extractItems(String html, String baseUri) throws IOException {
    final Document document = getDom(html, baseUri);
    return MicrodataParser.getMicrodata(document);
  }


  private Document getDom(String html, String baseUri) {
    try {
      TagSoupParser tagSoupParser = new TagSoupParser(new ByteArrayInputStream(html.getBytes("UTF-8")), baseUri);
      return tagSoupParser.getDOM();
    }
    catch (IOException e) {
      throw new TenforceException(e);
    }
  }

  private void microdata(String html, String baseUri) {
    Document document = getDom(html, baseUri);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);


    MicrodataParserReport microdata = MicrodataParser.getMicrodata(document);


    MicrodataParser.getMicrodataAsJSON(document, ps);
    ps.flush();
    System.out.println(new String(baos.toByteArray()));
  }

  private Repository parseRdfa(String html, String baseUri) throws Exception {


    Any23 runner = new Any23();
    runner.setMIMETypeDetector(HtmlMIMETypeDetector.instance);
//    DocumentSource source = new StringDocumentSource(xmlizeHtml(html), "http://data.europa.eu/esco/semic/linkedIn/01");
//    DocumentSource source = new StringDocumentSource(xmlizeHtml(html), "https://www.linkedin.com/jobs2/view/29081227");

    Document document = getDom(html, baseUri);
    DocumentSource source = new StringDocumentSource(DomHelper.toString(document), baseUri);

//    DocumentSource source = new StringDocumentSource(html, "https://www.linkedin.com/jobs2/view/29081227");
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
//      xhtml = xhtml.replace("&euml;", "e");
//      xhtml = xhtml.replace("&nbsp;", " ");
//      xhtml = xhtml.replace("&eacute;", "e");
//      xhtml = xhtml.replace("&hellip;", "...");
//      xhtml = xhtml.replace("&ouml;", "u");
//      xhtml = xhtml.replace("&ntilde;", "~");
//      xhtml = xhtml.replace("&ccedil;", "c");
//      xhtml = xhtml.replace("&ecirc;", "c");
//      xhtml = StringUtils.substringAfter(xhtml, "<body>");
//      return StringUtils.substringBeforeLast(xhtml, "</body>");

      int index = 0;
      while ((index = xhtml.indexOf("&", index)) != -1) {
        int end = xhtml.indexOf(";", index);
        if (index + 7 >= end) {
          xhtml = xhtml.substring(0, index) + "X" + xhtml.substring(end + 1);
        }
        else {
          index++;
        }
      }


      xhtml = xhtml.replace("<META", "<meta");

      xhtml = fixCloseTag(xhtml, "meta");
      xhtml = fixCloseTag(xhtml, "link");
      xhtml = fixCloseTag(xhtml, "input");
      xhtml = fixCloseTag(xhtml, "img");
      xhtml = fixCloseTag(xhtml, "br");
      xhtml = fixCloseTag(xhtml, "ecirc");

      index = 0;
      while ((index = xhtml.indexOf("<script", index)) != -1) {
        int end = xhtml.indexOf("</script>", index);
        xhtml = xhtml.substring(0, index) + xhtml.substring(end + 9);
      }

      return xhtml;
    }
    catch (Throwable e) {
      throw new TenforceException(e, "Tagsoup failed: {}", e.getMessage());
    }
  }

  private static String fixCloseTag(String input, String tag) {
    int index = 0;
    while ((index = input.indexOf("<" + tag, index)) != -1) {
      int end = input.indexOf(">", index);
      input = input.substring(0, end + 1) + "</" + tag + ">" + input.substring(end + 1);
      index = end;
    }
    return input;

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
