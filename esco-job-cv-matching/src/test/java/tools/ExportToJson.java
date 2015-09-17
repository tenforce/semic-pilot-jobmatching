package tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenforce.exception.TenforceException;
import com.tenforce.sesame.template.SesameDatasource;
import eu.esco.demo.jobcvmatching.root.SemicRdfModel;
import eu.esco.demo.jobcvmatching.root.SemicRootConfiguration;
import eu.esco.demo.jobcvmatching.root.SparqlQuery;
import eu.esco.demo.jobcvmatching.root.service.DataService;
import eu.esco.demo.jobcvmatching.sesame.StatementIterable;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.query.BindingSet;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ExportToJson {
  public static void main(String[] args) throws Exception {
    ExportToJson exportToJson = new ExportToJson();
    try (ConfigurableApplicationContext context = new SpringApplicationBuilder(SemicRootConfiguration.class).web(false).run(args)) {
      context.getAutowireCapableBeanFactory().autowireBean(exportToJson);
      exportToJson.doIt();
    }
  }

  @Inject
  @SparqlQuery(SparqlQuery.Value.allCvForCache)
  public String allCvForCacheSparqlQuery;
  @Inject
  @SparqlQuery(SparqlQuery.Value.allJvForCache)
  public String allJvForCacheSparqlQuery;
  @Inject
  private DataService dataService;
  @Inject
  private SesameDatasource sesameDatasource;

  private Map<String, Map<Object, Object>> skills = new HashMap<>();
  //  private Map<String, Map<Object, Object>> occupationsV0 = new HashMap<>();
  private Map<String, Map<Object, Object>> occupationsV1 = new HashMap<>();
  private Map<String, Map<Object, Object>> naces = new HashMap<>();

  public void doIt() throws Exception {
    Map<Object, Object> cvs = getCVs();
    Map<Object, Object> jvs = getJVs();

//    fillSkillsFromOccupations(occupationsV0, SKOS.RELATED.stringValue());
    fillSkillsFromOccupations(occupationsV1, "http://data.europa.eu/esco/model#relatedEssentialSkill");
//    occupationsV0.putAll(occupationsV1);

    fillLabels(skills);
//    fillLabels(occupationsV0);
    fillLabels(occupationsV1);
    fillLabels(naces);

    Map<String, Object> jsonObject = new HashMap<>();
    jsonObject.put("cvs", cvs);
    jsonObject.put("jvs", jvs);
    jsonObject.put("skills", skills);
    jsonObject.put("occupations", occupationsV1);
    jsonObject.put("naces", naces);

    File file = new File("C:\\Projects\\ESCO\\semic\\cache.json");
    new ObjectMapper().writeValue(file, jsonObject);

  }

  public Map<Object, Object> getJVs() {
    DataService.ConstructQueryHandler<Map<Object, Object>> handler = new DataService.ConstructQueryHandler<Map<Object, Object>>() {
      @Override
      public Map<Object, Object> handle(RepositoryConnection data, ValueFactory vf) throws Exception {
        Map<Object, Object> result = new HashMap<>();
        for (Statement statement : new StatementIterable(data.getStatements(null, RDF.TYPE, vf.createURI(SemicRdfModel.typeJV), false))) {
          Map<Object, Object> map = new HashMap<>();
          boolean hasData = updateJob(map, data, statement.getSubject());
          if (hasData) result.put(statement.getSubject().stringValue(), map);
        }
        return result;
      }
    };
    return dataService.doConstructQuery(handler, allJvForCacheSparqlQuery);
  }


  public Map<Object, Object> getCVs() {
    DataService.ConstructQueryHandler<Map<Object, Object>> handler = new DataService.ConstructQueryHandler<Map<Object, Object>>() {
      @Override
      public Map<Object, Object> handle(RepositoryConnection data, ValueFactory vf) throws Exception {
        Map<Object, Object> result = new HashMap<>();
        for (Statement statement : new StatementIterable(data.getStatements(null, RDF.TYPE, vf.createURI(SemicRdfModel.typeCV), false))) {
          Map<Object, Object> map = new HashMap<>();
          updateCV(map, data, statement.getSubject());
          result.put(statement.getSubject().stringValue(), map);
        }
        return result;
      }
    };
    return dataService.doConstructQuery(handler, allCvForCacheSparqlQuery);
  }

  private void fillLabels(final Map<String, Map<Object, Object>> map) throws Exception {
    ValueFactory vf = sesameDatasource.getValueFactory();
    DataService.SelectQueryHandler<Void> handler = new DataService.SelectQueryHandler<Void>() {
      @Override
      public void handleRow(BindingSet bindingSet, ValueFactory valueFactory) throws Exception {
        String uri = bindingSet.getValue("uri").stringValue();
        Map<Object, Object> values = map.get(uri);
        values.put("fallbackLabel", bindingSet.getValue("fallbackLabel").stringValue());
        Literal prefLabel = (Literal) bindingSet.getValue("prefLabel");
        values.put("prefLabel" + prefLabel.getLanguage(), prefLabel.getLabel());
      }

      @Override
      public Void getResult() {
        return null;
      }
    };
    String query = "select ?uri ?fallbackLabel ?prefLabel ?parent where {" +
                   "  ?uri <" + SemicRdfModel.escoPropertyFallbackLabel + "> ?fallbackLabel . " +
                   "  ?uri <" + SKOS.PREF_LABEL + "> ?prefLabel . " +
                   "}";

    for (Map.Entry<String, Map<Object, Object>> entry : map.entrySet()) {
      dataService.doSelectQuery(handler, query, new ImmutablePair<String, Value>("uri", vf.createURI(entry.getKey())));
    }
  }

  private void fillSkillsFromOccupations(final Map<String, Map<Object, Object>> occupationMap, String property) throws Exception {
    for (Map<Object, Object> occupationInfo : occupationMap.values()) {
      occupationInfo.put("skills", new HashSet());
    }
    ValueFactory vf = sesameDatasource.getValueFactory();
    DataService.SelectQueryHandler<Void> handler = new DataService.SelectQueryHandler<Void>() {
      @Override
      public void handleRow(BindingSet bindingSet, ValueFactory valueFactory) throws Exception {
        String occupationUri = bindingSet.getValue("occupation").stringValue();
        String skillUri = patchSkillUri(bindingSet.getValue("skill").stringValue());
        if (!skills.containsKey(skillUri)) skills.put(skillUri, new HashMap<>());

        Map<Object, Object> occupationInfoMap = occupationMap.get(occupationUri);
        ((Collection) occupationInfoMap.get("skills")).add(skillUri);
      }

      @Override
      public Void getResult() {
        return null;
      }
    };

    String query = "select ?occupation ?skill where {" +
                   "  ?occupation <" + property + "> ?skill . " +
                   "  ?skill <" + RDF.TYPE + "> <" + SemicRdfModel.escoTypeSkill + "> . " +
                   "}";
    for (String occupationUri : occupationMap.keySet()) {
      dataService.doSelectQuery(handler, query, new ImmutablePair<String, Value>("occupation", vf.createURI(occupationUri)));
    }
  }


  private String getFirstValue(RepositoryConnection data, Resource resource, URI property, String fallback) {
    try {
      RepositoryResult<Statement> statements = data.getStatements(resource, property, null, false);
      return statements.hasNext() ? statements.next().getObject().stringValue() : fallback;
    }
    catch (RepositoryException e) {
      throw new TenforceException(e);
    }
  }

  private String patchSkillUri(String uri) {
    if (uri.startsWith("http://data.europa.eu/esco/skill/367288;facet")) {
      return uri.replace("http://data.europa.eu/esco/skill/367288;facet", "http://data.europa.eu/esco/skill/SCG.TS.3.m.1;facet");
    }
    return uri;
  }
  private List<String> getAllValues(RepositoryConnection data, Resource resource, URI property, Map<String, Map<Object, Object>> addValuesTo) {
    List<String> result = new ArrayList<>();
    for (Statement statement : StatementIterable.list(data, resource, property, null)) {
      String value = patchSkillUri(statement.getObject().stringValue());

      result.add(value);
      if (null != addValuesTo && !addValuesTo.containsKey(value)) addValuesTo.put(value, new HashMap<>());
    }
    return result;
  }

  public void updateCV(Map<Object, Object> map, RepositoryConnection data, Resource cvURI) throws Exception {
    ValueFactory vf = data.getValueFactory();
    map.put("givenName", getFirstValue(data, cvURI, FOAF.GIVEN_NAME, StringUtils.substringAfterLast(cvURI.stringValue(), "/")));
    map.put("description", getFirstValue(data, cvURI, DCTERMS.DESCRIPTION, ""));
//    map.put("skillV0", getAllValues(data, cvURI, vf.createURI(SemicRdfModel.propertyHasSkill_v0), skills));
    map.put("skillV1", getAllValues(data, cvURI, vf.createURI(SemicRdfModel.propertyHasSkill_v1), skills));
//    map.put("occupationV0", getAllValues(data, cvURI, vf.createURI(SemicRdfModel.propertyHasOccupation_v0), occupationsV0));
    map.put("occupationV1", getAllValues(data, cvURI, vf.createURI(SemicRdfModel.propertyHasOccupation_v1), occupationsV1));
  }

  public boolean updateJob(Map<Object, Object> map, RepositoryConnection data, Resource jvURI) throws Exception {
    ValueFactory vf = data.getValueFactory();
    map.put("label", getFirstValue(data, jvURI, SKOS.PREF_LABEL, ""));
    map.put("nace", getAllValues(data, jvURI, vf.createURI(SemicRdfModel.propertyHasNace), naces));
    map.put("hiringOrganizationName", getFirstValue(data, jvURI, vf.createURI(SemicRdfModel.propertyHiringOrganizationName), ""));
    map.put("employmentType", getFirstValue(data, jvURI, vf.createURI(SemicRdfModel.propertyEmploymentType), ""));
    map.put("experienceLevel", getFirstValue(data, jvURI, vf.createURI(SemicRdfModel.propertyExperienceLevel), ""));
    map.put("geonameUri", getFirstValue(data, jvURI, vf.createURI(SemicRdfModel.propertyGeonameUri), ""));
    map.put("geonameLabel", getFirstValue(data, jvURI, vf.createURI(SemicRdfModel.propertyGeoname), ""));
    map.put("occupationalCategory", getFirstValue(data, jvURI, vf.createURI(SemicRdfModel.propertyOccupationalCategory), ""));
    map.put("description", getFirstValue(data, jvURI, DCTERMS.DESCRIPTION, ""));

//    List<String> skillsV0 = getAllValues(data, jvURI, vf.createURI(SemicRdfModel.propertyHasSkill_v0), skills);
    List<String> skillsV1 = getAllValues(data, jvURI, vf.createURI(SemicRdfModel.propertyHasSkill_v1), skills);
//    List<String> occupationsV0 = getAllValues(data, jvURI, vf.createURI(SemicRdfModel.propertyHasOccupation_v0), this.occupationsV0);
    List<String> occupationsV1 = getAllValues(data, jvURI, vf.createURI(SemicRdfModel.propertyHasOccupation_v1), this.occupationsV1);
//    map.put("skillV0", skillsV0);
    map.put("skillV1", skillsV1);
//    map.put("occupationV0", occupationsV0);
    map.put("occupationV1", occupationsV1);

//    return !skillsV0.isEmpty() && !skillsV1.isEmpty();
    return !skillsV1.isEmpty();
//    return !skillsV0.isEmpty() || !skillsV1.isEmpty() || !occupationsV0.isEmpty() || !occupationsV1.isEmpty();
  }

}
