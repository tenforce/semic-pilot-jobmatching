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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CreateSkillsRatingLists {
  private static final String rdfUser = "escordf";
  private static final String rdfPassword = "secret";
  private static final int jvPatchNumber = 2600 / 124;

  private static final Map<String, String> labelMap = new HashMap<>();
  private static final Map<String, List<String>> childMap = new HashMap<>();
  private static final Map<String, Integer> skillInJvCounts = new HashMap<>();
  private static final Map<String, Integer> skillInCvCounts = new HashMap<>();
  private static int maxCvCount;
  private static int maxJvCount;

  private static final String hasSkillProperty = SemicRdfModel.propertyHasSkill_v1;
  private static final String attributeSuffix = "V1";


  public static void main(String[] args) throws Exception {
    loadSkills();
    loadSkillCounts();

    cleanMap(skillInJvCounts);
    cleanMap(skillInCvCounts);

    System.out.println("---------------------------------------------------");
    System.out.println("topRequested");
    System.out.println("---------------------------------------------------");
    outputTop(skillInJvCounts, "topRequested");
    System.out.println("---------------------------------------------------");
    System.out.println("topOffered");
    System.out.println("---------------------------------------------------");
    outputTop(skillInCvCounts, "topOffered");
    System.out.println("---------------------------------------------------");
    System.out.println("topGap");
    System.out.println("---------------------------------------------------");
    outputGap(getDiffMap());
    System.out.println("---------------------------------------------------");


  }

  private static Map<String, Integer> getDiffMap() {
    Map<String, Integer> diffMap = new HashMap<>();

    for (Map.Entry<String, Integer> entry : skillInJvCounts.entrySet()) {
      int jvCount = entry.getValue();
      Integer cvCount = skillInCvCounts.get(entry.getKey());
      if (null == cvCount) cvCount = 0;

      int patchedJvCount = jvCount * jvPatchNumber;
      if (cvCount >= patchedJvCount) continue;

//      int missing = (1000000 * (patchedJvCount - cvCount)) / patchedJvCount;
//      int missing = patchedJvCount - cvCount;
      int missing = jvCount - cvCount;
      if (missing <= 0) continue;
//      int missing  =    (1000000 * patchedJvCount) / (cvCount + patchedJvCount);;

      diffMap.put(entry.getKey(), missing);
    }
    return diffMap;
  }

  private static void cleanMap(Map<String, Integer> toClean) {
    Set<String> toRemove = new HashSet<>();
    for (String key : toClean.keySet()) {
      if (!labelMap.containsKey(key)) toRemove.add(key);
    }
    for (String toRemoveKey : toRemove) {
      toClean.remove(toRemoveKey);
    }
  }

  private static void outputTop(Map<String, Integer> counts, String groupName) {
    Map<String, Integer> orderedCounts = sortByValue(counts, false);
    int group = 0;
    int counter = 0;
    for (Map.Entry<String, Integer> entry : orderedCounts.entrySet()) {
      counter++;
      if (counter > 10) {
        counter = 1;
        group++;
        System.out.println();
      }
      System.out.print("<li " + groupName + attributeSuffix + "=\"" + group + "\"");
      if (group > 0) System.out.print(" style=\"display:none;\"");
      System.out.print(">" + labelMap.get(entry.getKey()) + " <span>" + entry.getValue() + "</span>" + "</li>");

    }
    System.out.println();
  }

  private static void outputGap(Map<String, Integer> counts) {
    Map<String, Integer> orderedCounts = sortByValue(counts, true);
    int group = 0;
    int counter = 0;
    for (Map.Entry<String, Integer> entry : orderedCounts.entrySet()) {
      counter++;
      if (counter > 10) {
        counter = 1;
        group++;
        System.out.println();
      }
      System.out.print("<li topGap" + attributeSuffix + "=\"" + group + "\"");
      if (group > 0) System.out.print(" style=\"display:none;\"");

//      int patchedJvCount = skillInJvCounts.get(entry.getKey()) * jvPatchNumber;
//      Integer cvCount = skillInCvCounts.get(entry.getKey());
//      if(null == cvCount) cvCount = 0;
//      System.out.print(">" + labelMap.get(entry.getKey()) + " (" + jvCount  + " requested for " + cvCount + " available)" + "</li>");
//      System.out.print(">" + labelMap.get(entry.getKey()) + " (" + skillInJvCounts.get(entry.getKey()) +   ")</li>");
//      System.out.print(">" + labelMap.get(entry.getKey()) + " (request coverage: " + entry.getValue() + "%)</li>");

      int jvCount = skillInJvCounts.get(entry.getKey());
      Integer cvCount = skillInCvCounts.get(entry.getKey());
      if (null == cvCount) cvCount = 0;

      int patchedJvCount = jvCount * jvPatchNumber;
      if (cvCount >= patchedJvCount) continue;


      int coverage = cvCount >= patchedJvCount ? 100 : 100 * ((cvCount - patchedJvCount) / patchedJvCount);


      String preCoverage = (coverage == 0 && cvCount > 0) ? "+" : "";

      System.out.print(">" + labelMap.get(entry.getKey()) + " <span>request: " + jvCount + " - available: " + cvCount + "</span></li>");
//      System.out.print(">" + labelMap.get(entry.getKey()) + " <span>coverage: " +preCoverage + coverage + "% - request: " + jvCount + " (" + patchedJvCount + ") - available: " + cvCount + "</span></li>");
//      System.out.print(">" + labelMap.get(entry.getKey()) + " <span>request coverage: " + (100 - (entry.getValue() / 10000)) + "% - requested " + skillInJvCounts.get(entry.getKey()) + "</span></li>");
    }
    System.out.println();
  }

  static <V extends Comparable<V>> Map<String, V> sortByValue(Map<String, V> map, final boolean checkNumberOfJvRequestAsFallback) {
    List<Map.Entry<String, V>> list = new LinkedList<>(map.entrySet());
    Collections.sort(list, new Comparator<Map.Entry<String, V>>() {
      public int compare(Map.Entry<String, V> o1, Map.Entry<String, V> o2) {
        int result = o2.getValue().compareTo(o1.getValue());

        if (0 == result && checkNumberOfJvRequestAsFallback) {
          Integer o1JvCount = skillInJvCounts.get(o1.getKey());
          Integer o2JvCount = skillInJvCounts.get(o2.getKey());
          result = o2JvCount.compareTo(o1JvCount);
        }

        return 0 == result ? labelMap.get(o1.getKey()).compareTo(labelMap.get(o2.getKey())) : result;
      }
    });

    Map<String, V> result = new LinkedHashMap<>();
    for (Map.Entry<String, V> entry : list) {
      result.put(entry.getKey(), entry.getValue());
    }
    return result;
  }


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

}
