package tools;

import eu.esco.demo.jobcvmatching.root.HardcodedConfiguration;
import eu.esco.demo.jobcvmatching.root.SemicRdfModel;
import eu.esco.demo.jobcvmatching.root.service.RemoteSesameDatasource;
import org.openrdf.model.Literal;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class CreateUsedSkillsCloud {
  private static final String rdfUser = "escordf";
  private static final String rdfPassword = "secret";

  private static final Map<String, String> labelMap = new HashMap<>();
  private static final Map<String, List<String>> childMap = new HashMap<>();
  private static final Map<String, Integer> skillInJvCounts = new HashMap<>();
  private static final Map<String, Integer> skillInCvCounts = new HashMap<>();
  private static int maxCvCount;
  private static int maxJvCount;

  private static String hasSkillProperty = SemicRdfModel.propertyHasSkill_v0;

  public static void main(String[] args) throws Exception {
    loadSkills();
    loadSkillCounts();

    System.out.println("---------------------------------------------------");
    output(childMap.get(null), 0);
    System.out.println("---------------------------------------------------");
  }

  private static void output(List<String> uris, int level) {

    TreeMap<Integer, String> result = new TreeMap<>();
    for (Map.Entry<String, Integer> entry : skillInJvCounts.entrySet()) {
      if (entry.getValue() == 0) continue;
      addToResultMap(result, entry.getKey());
    }
    for (String uri : result.values()) {
      writeOneLine(uri);
    }

//    for (String uri : uris) {
//      if (!hasStuff(uri)) continue;
//
//      outputOneHtml(uri, level);
//      if (childMap.containsKey(uri)) output(childMap.get(uri), level + 1);
//    }
  }

  private static void writeOneLine(String uri) {
    Integer cvCount = skillInCvCounts.get(uri);
    if (null == cvCount) cvCount = 0;
    Integer jvCount = skillInJvCounts.get(uri);
    if (null == jvCount) jvCount = 0;

    int patchedJvCount = jvCount * 2600 / 124;


    String levelClass = "level JV_" + getClassSuffix(maxJvCount, jvCount) + " " + getClassSuffixInvert(cvCount + patchedJvCount, patchedJvCount);

    System.out.print("<div class=\"level\"><div class=\"" + levelClass + "\">");
    System.out.print(labelMap.get(uri) + " ");
    System.out.print("<span esco-demo=\"true\" style=\"display:none;\">CV: " + cvCount + " - JV: " + jvCount + " (" + patchedJvCount + ")</span>");
    if (cvCount > 0) System.out.print("<span class=\"CV_count\">" + cvCount + "</span>");
    if (jvCount > 0) System.out.print("<span class=\"JV_count\">" + jvCount + "</span>");
    System.out.println("</div></div>");


  }

  private static void addToResultMap(SortedMap<Integer, String> result, String uri) {
    Integer cvCount = skillInCvCounts.get(uri);
    if (null == cvCount) cvCount = 0;
    Integer jvCount = skillInJvCounts.get(uri);
    if (null == jvCount) jvCount = 0;

    int patchedJvCount = jvCount * 2600 / 124;

    result.put(100 - (100 * patchedJvCount / (cvCount + patchedJvCount)), uri);


  }

//  private static void outputOneHtml(String uri, int level) {
//    Integer cvCount = skillInCvCounts.get(uri);
//    if (null == cvCount) cvCount = 0;
//    Integer jvCount = skillInJvCounts.get(uri);
//    if (null == jvCount) jvCount = 0;
//
//
////    String levelClass = "level JV_" + getClassSuffix(maxJvCount, jvCount) + " CV_" + getClassSuffix(maxCvCount, cvCount) + " " + getClassSuffix(cvCount + jvCount, jvCount);
////    String levelClass = "level CV_" + getClassSuffix(maxCvCount, cvCount) + " " + getClassSuffix(cvCount + jvCount, jvCount);
//
//    int patchedJvCount = jvCount * 2600 / 124;
//    String levelClass = "level JV_" + getClassSuffix(maxJvCount, jvCount) + " " + getClassSuffix(cvCount + patchedJvCount, patchedJvCount);
//
//    System.out.print(StringUtils.repeat("<div class=\"level\">", level));
//    System.out.print("<div class=\"" + levelClass + "\">");
//    System.out.print(labelMap.get(uri) + " ");
//    if (cvCount > 0) System.out.print("<span class=\"CV_count\">" + cvCount + "</span>");
//    if (jvCount > 0) System.out.print("<span class=\"JV_count\">" + jvCount + "</span>");
//    System.out.print(StringUtils.repeat("</div>", level + 1));
//    System.out.println();
//  }

  private static String getClassSuffix(int max, int number) {
    if (max == 0) return "";
    int percent = 100 * number / max;
    return getClassSuffix(percent);
  }

  private static String getClassSuffixInvert(int max, int number) {
    if (max == 0) return "";
    int percent = 100 * number / max;
    return getClassSuffix(100 - percent);
  }

  private static String getClassSuffix(int percent) {
    if (percent >= 80) return "five";
    if (percent >= 60) return "four";
    if (percent >= 40) return "three";
    if (percent >= 20) return "two";
    return "one";
  }

//  private static boolean hasStuff(String uri) {
//    Integer inJvCount = skillInJvCounts.get(uri);
//    if (null != inJvCount && inJvCount > 0) return true;
////    Integer inCvCount = skillInCvCounts.get(uri);
////    if (null != inCvCount && inCvCount > 0) return true;
//    if (childMap.containsKey(uri)) {
//      for (String childUri : childMap.get(uri)) {
//        if (hasStuff(childUri)) return true;
//      }
//    }
//    return false;
//  }


  private static void loadSkillCounts() throws Exception {
    maxCvCount = loadSkillCounts(skillInCvCounts, true);
    maxJvCount = loadSkillCounts(skillInJvCounts, false);
  }

  private static int loadSkillCounts(Map<String, Integer> counts, boolean isCv) throws Exception {
    int maxCount = 0;
    String skillCountQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                             "select ?skill (count(distinct ?document) as ?skillCount) where { \n" +
                             "  ?document <" + hasSkillProperty + "> ?skill .\n" +
                             "  filter exists {?document rdf:type <" + (isCv ? SemicRdfModel.typeCV : SemicRdfModel.typeJV) + "> } .\n";
    if (isCv) skillCountQuery += "  filter exists {?document <" + SemicRdfModel.propertyInSector + "> <" + SemicRdfModel.sectorHosp + "> } .\n";
    skillCountQuery += "}\n" +
                       "group by ?skill";
    RemoteSesameDatasource destinationDataSource = new RemoteSesameDatasource(HardcodedConfiguration.sesameUrl, HardcodedConfiguration.sesameRepository, rdfUser, rdfPassword);
    RepositoryConnection connection = destinationDataSource.getConnection();
    TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, skillCountQuery);
    TupleQueryResult queryResult = tupleQuery.evaluate();

    while (queryResult.hasNext()) {
      BindingSet bindingSet = queryResult.next();
      String skill = bindingSet.getBinding("skill").getValue().stringValue();
      int count = ((Literal) bindingSet.getBinding("skillCount").getValue()).intValue();
//      if (!isCv) count = count * 2600 / 124;
      counts.put(skill, count);
      maxCount = Math.max(maxCount, count);
    }
    queryResult.close();
    destinationDataSource.closeQuietly(connection);
    destinationDataSource.closeQuietly();
    return maxCount;
  }


  private static void loadSkills() throws Exception {

    String skillCountQuery = "select ?skill ?label ?parent where { " +
                             "  ?skill <" + RDF.TYPE + "> <" + SemicRdfModel.escoTypeSkill + "> . " +
                             "  ?skill <" + SemicRdfModel.escoPropertyFallbackLabel + "> ?label  ." +
                             "  OPTIONAL { ?skill <" + SKOS.BROADER + "> ?parent . } . " +
                             "}";
    RemoteSesameDatasource destinationDataSource = new RemoteSesameDatasource(HardcodedConfiguration.sesameUrl, HardcodedConfiguration.sesameRepository, rdfUser, rdfPassword);
    RepositoryConnection connection = destinationDataSource.getConnection();
    TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, skillCountQuery);
    TupleQueryResult queryResult = tupleQuery.evaluate();

    while (queryResult.hasNext()) {
      BindingSet bindingSet = queryResult.next();


      String uri = bindingSet.getBinding("skill").getValue().stringValue();
      String label = bindingSet.getBinding("label").getValue().stringValue();

      Binding parent = bindingSet.getBinding("parent");
      String broaderUri = null == parent ? null : parent.getValue().stringValue();

      labelMap.put(uri, label);
      List<String> children = childMap.get(broaderUri);
      if (null == children) childMap.put(broaderUri, children = new ArrayList<>());
      children.add(uri);
    }
    queryResult.close();
    destinationDataSource.closeQuietly(connection);
    destinationDataSource.closeQuietly();
  }

//  private static void loadSkills() throws Exception {
//    String requestBody = "{\n" +
//                         "      \"language\": \"en\",\n" +
//                         "      \"view\": \"detail\",\n" +
//                         "      \"conceptScheme\": \"http://data.europa.eu/esco/ConceptScheme/ESCO_Skills\",\n" +
//                         "      \"size\": 999999,\n" +
//                         "      \"offset\": 0\n" +
//                         "    }";
//    String body = new HttpClientFactory(null).executeStringPost("http://tfvirt-semtech-dsk01:8080/esco-service-api/concepts", requestBody, ContentType.APPLICATION_JSON).getRight();
//    List concepts = (List) new ObjectMapper().readValue(body, Map.class).get("concepts");
//    for (Object conceptObject : concepts) {
//      Map<?, ?> concept = (Map<?, ?>) ((Map<?, ?>) conceptObject).get("concept");
//      String uri = (String) concept.get("uri");
//      String label = (String) ((Map) concept.get("prefLabel")).values().iterator().next();
//
//      List broaderList = (List) concept.get("broader");
//      String broaderUri = (null == broaderList || broaderList.isEmpty()) ? null : (String) ((Map) broaderList.get(0)).get("uri");
//
//      labelMap.put(uri, label);
//      List<String> children = childMap.get(broaderUri);
//      if (null == children) childMap.put(broaderUri, children = new ArrayList<>());
//      children.add(uri);
//    }
//  }
}
