package eu.esco.demo.jobcvmatching.root.service;

import com.tenforce.exception.TenforceException;
import com.tenforce.sesame.template.RepoConnectionCallback;
import com.tenforce.sesame.template.SesameDatasource;
import com.tenforce.sesame.template.SesameRdfTemplate;
import com.tenforce.sesame.template.SesameResultSetExtractor;
import eu.esco.demo.jobcvmatching.root.SemicRdfModel;
import eu.esco.demo.jobcvmatching.root.SparqlQuery;
import eu.esco.demo.jobcvmatching.root.model.CV;
import eu.esco.demo.jobcvmatching.root.model.EmploymentType;
import eu.esco.demo.jobcvmatching.root.model.ExperienceLevel;
import eu.esco.demo.jobcvmatching.root.model.Job;
import eu.esco.demo.jobcvmatching.root.model.Nace;
import eu.esco.demo.jobcvmatching.root.model.Occupation;
import eu.esco.demo.jobcvmatching.root.model.Skill;
import eu.esco.demo.jobcvmatching.sesame.StatementIterable;
import eu.tenforce.commons.sem.sesame.SesameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
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
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DataService {
  private static final Logger log = LoggerFactory.getLogger(DataService.class);

  @Inject
  private DocumentService documentService;
  @Inject
  private ServiceApiBaseClient serviceApiBaseClient;
  @Inject
  private SesameDatasource sesameDatasource;

  @Inject
  @SparqlQuery(SparqlQuery.Value.matchingCvs)
  public String matchingCvsSparqlQuery;
  @Inject
  @SparqlQuery(SparqlQuery.Value.jvs)
  public String jvsSparqlQuery;
  @Inject
  @SparqlQuery(SparqlQuery.Value.cvs)
  public String cvsSparqlQuery;
  @Inject
  @SparqlQuery(SparqlQuery.Value.jvLabelOnly)
  public String jvLabelOnlySparqlQuery;
  @Inject
  @SparqlQuery(SparqlQuery.Value.jv)
  public String jvSparqlQuery;
  @Inject
  @SparqlQuery(SparqlQuery.Value.getSemicConcepts)
  public String getSemicConceptsQuery;


  public CV getCV(String uri, String language) {
    ValueFactory vf = sesameDatasource.getValueFactory();
    Collection<CV> cvs = doConstructQuery(getCVsByConstructQuery(cvsSparqlQuery), cvsSparqlQuery, new ImmutablePair<String, Value>("cv", vf.createURI(uri)));
    return cvs.isEmpty() ? null : cvs.iterator().next();
  }

  public Collection<CV> getCVs(String language) {
//    return doConstructQuery(getCVsByConstructQuery(cvsSparqlQuery), cvsSparqlQuery);

    String query = "select ?cv ?documentId ?description ?skill ?skillPrefLabel ?skillFallbackLabel ?occupationV0 ?occupationV0PrefLabel ?occupationV0FallbackLabel where { " +
                   "  ?cv <" + RDF.TYPE.stringValue() + "> <" + SemicRdfModel.typeCV + "> . " +
                   "  OPTIONAL { ?cv <" + DCTERMS.DESCRIPTION.stringValue() + "> ?description } . " +
                   "  OPTIONAL { " +
                   "     ?cv <" + SemicRdfModel.propertyHasSkill_v0 + "> ?skill . " +
                   "     ?skill <http://www.tenforce.com/esco/java#fallbackLabel> ?skillFallbackLabel . " +
                   "     OPTIONAL { ?skill <" + SKOS.PREF_LABEL.stringValue() + "> ?skillPrefLabel . filter(lang(?skillPrefLabel) = '" + language + "') }" +
                   "   } " +
                   "  OPTIONAL { " +
                   "     ?cv <" + SemicRdfModel.propertyHasOccupation_v0 + "> ?occupationV0 . " +
                   "     ?occupationV0 <http://www.tenforce.com/esco/java#fallbackLabel> ?occupationV0FallbackLabel . " +
                   "     OPTIONAL { ?occupationV0 <" + SKOS.PREF_LABEL.stringValue() + "> ?occupationV0PrefLabel . filter(lang(?occupationV0PrefLabel) = '" + language + "') }" +
                   "   } " +
                   "   FILTER exists { ?cv <" + SemicRdfModel.propertyInSector + "> <" + SemicRdfModel.sectorHosp + "> . } " +
                   " } ";
    return getCVsByQuery(query);
  }

  private ConstructQueryHandler<Collection<CV>> getCVsByConstructQuery(String query) {
    ConstructQueryHandler<Collection<CV>> handler = new ConstructQueryHandler<Collection<CV>>() {
      @Override
      public Collection<CV> handle(RepositoryConnection data, ValueFactory vf) throws Exception {
        Collection<CV> cvs = new ArrayList<>();
        for (Statement statement : StatementIterable.list(data, null, RDF.TYPE, vf.createURI(SemicRdfModel.typeCV))) {
          String cvUri = statement.getSubject().toString();
          RepositoryResult<Statement> cvPrefLabels = data.getStatements(statement.getSubject(), SKOS.PREF_LABEL, null, false);
          String label = cvPrefLabels.hasNext() ? cvPrefLabels.next().getObject().stringValue() : cvUri;
          CV cv = new CV(cvUri, label, documentService.hasDocumentContent(cvUri));

          for (Statement occupationStatement : StatementIterable.list(data, statement.getSubject(), vf.createURI(SemicRdfModel.propertyHasOccupation_v0), null)) {
            Resource occupationResource = (Resource) occupationStatement.getObject();

            String occupationLabel = data.getStatements(occupationResource, vf.createURI(SemicRdfModel.escoPropertyFallbackLabel), null, false).next().getObject().stringValue();

            Occupation occupation = new Occupation(occupationResource.stringValue(), occupationLabel);
            for (Statement skillStatement : StatementIterable.list(data, occupationResource, SKOS.RELATED, null)) {
              Resource skillResource = (Resource) skillStatement.getObject();
              String skillLabel = data.getStatements(skillResource, vf.createURI(SemicRdfModel.escoPropertyFallbackLabel), null, false).next().getObject().stringValue();
              Skill skill = new Skill(skillResource.stringValue(), skillLabel);
              occupation.addSkill(skill);
            }
            cv.addOccupation(occupation);
          }

          for (Statement occupationStatement : StatementIterable.list(data, statement.getSubject(), vf.createURI(SemicRdfModel.propertyHasOccupation_v1), null)) {
            Resource occupationResource = (Resource) occupationStatement.getObject();

            String occupationLabel = data.getStatements(occupationResource, vf.createURI(SemicRdfModel.escoPropertyFallbackLabel), null, false).next().getObject().stringValue();

            Occupation occupation = new Occupation(occupationResource.stringValue(), occupationLabel);
            for (Statement skillStatement : StatementIterable.list(data, occupationResource, SKOS.RELATED, null)) {
              Resource skillResource = (Resource) skillStatement.getObject();
              String skillLabel = data.getStatements(skillResource, vf.createURI(SemicRdfModel.escoPropertyFallbackLabel), null, false).next().getObject().stringValue();
              Skill skill = new Skill(skillResource.stringValue(), skillLabel);
              occupation.addSkill(skill);
            }
            cv.addOccupationV1(occupation);
          }

          for (Statement skillStatement : StatementIterable.list(data, statement.getSubject(), vf.createURI(SemicRdfModel.propertyHasSkill_v0), null)) {
            Resource skillResource = (Resource) skillStatement.getObject();
            String skillLabel = getFirstValue(data, skillResource, SKOS.PREF_LABEL, null);
            if (StringUtils.isBlank(skillLabel)) skillLabel = data.getStatements(skillResource, vf.createURI(SemicRdfModel.escoPropertyFallbackLabel), null, false).next().getObject().stringValue();
            Skill skill = new Skill(skillResource.stringValue(), skillLabel);
            cv.addSkill(skill);
          }

          for (Statement skillStatement : StatementIterable.list(data, statement.getSubject(), vf.createURI(SemicRdfModel.propertyHasSkill_v1), null)) {
            Resource skillResource = (Resource) skillStatement.getObject();
            String skillLabel = getFirstValue(data, skillResource, SKOS.PREF_LABEL, null);
            if (StringUtils.isBlank(skillLabel)) skillLabel = data.getStatements(skillResource, vf.createURI(SemicRdfModel.escoPropertyFallbackLabel), null, false).next().getObject().stringValue();
            Skill skill = new Skill(skillResource.stringValue(), skillLabel);
            cv.addSkillV1(skill);
          }

          cvs.add(cv);
        }
        return cvs;
      }
    };
    return handler;
  }

  private Collection<CV> getCVsByQuery(String query) {
    Map<String, CV> result = new HashMap<>();
    long start = System.currentTimeMillis();
    List<Map<String, Value>> resultSet = getResultSet(query);
    long end = System.currentTimeMillis();
    log.info("getCVsByQuery select took " + (end - start) + "ms");
    for (Map<String, Value> resultRow : resultSet) {
      String cvUri = resultRow.get("cv").stringValue();
      CV cv = result.get(cvUri);
      if (null == cv) {
        Value descriptionValue = resultRow.get("description");
        cv = new CV(cvUri, null == descriptionValue ? cvUri : descriptionValue.stringValue(), documentService.hasDocumentContent(cvUri));
        result.put(cvUri, cv);
      }
      Value skillValue = resultRow.get("skill");
      if (null != skillValue) {
        Value skillPrefLabel = resultRow.get("skillPrefLabel");
        if (null == skillPrefLabel) skillPrefLabel = resultRow.get("skillFallbackLabel");
        cv.addSkill(new Skill(skillValue.stringValue(), skillPrefLabel.stringValue()));
      }
      Value skillV1Value = resultRow.get("skillV1");
      if (null != skillV1Value) {
        Value skillV1PrefLabel = resultRow.get("skillV1PrefLabel");
        if (null == skillV1PrefLabel) skillV1PrefLabel = resultRow.get("skillV1FallbackLabel");
        cv.addSkillV1(new Skill(skillV1Value.stringValue(), skillV1PrefLabel.stringValue()));
      }
      Value occupationV0Value = resultRow.get("occupationV0");
      if (null != occupationV0Value) {
        Value occupationV0PrefLabel = resultRow.get("occupationV0PrefLabel");
        if (null == occupationV0PrefLabel) occupationV0PrefLabel = resultRow.get("occupationV0FallbackLabel");
        cv.addOccupation(new Occupation(occupationV0Value.stringValue(), occupationV0PrefLabel.stringValue()));
      }
    }
    return result.values();
  }

  public Pair<List<Skill>, List<Occupation>> getSemicConcepts() {
    List<Skill> skills = new ArrayList<>();
    List<Occupation> occupations = new ArrayList<>();
    List<Map<String, Value>> resultSet = getResultSet(getSemicConceptsQuery);
    for (Map<String, Value> row : resultSet) {
      if (row.get("type").stringValue().equals(SemicRdfModel.escoTypeSkill)) {
        skills.add(new Skill(row.get("concept").stringValue(), row.get("label").stringValue()));
      }
      else if (row.get("type").stringValue().equals(SemicRdfModel.escoTypeOccupation)) {
        occupations.add(new Occupation(row.get("concept").stringValue(), row.get("label").stringValue()));
      }
    }
    return new ImmutablePair<>(skills, occupations);
  }

  public Map<String, String> getJVsWithLabel() {
    SelectQueryHandler<Map<String, String>> handler = new SelectQueryHandler<Map<String, String>>() {
      final Map<String, String> result = new HashMap<>();

      @Override
      public void handleRow(BindingSet bindingSet, ValueFactory valueFactory) throws Exception {
        result.put(bindingSet.getValue("jv").stringValue(), bindingSet.getValue("label").stringValue());
      }

      @Override
      public Map<String, String> getResult() {
        return result;
      }
    };
    return doSelectQuery(handler, jvLabelOnlySparqlQuery);
  }

  public Collection<Job> getJVs(String language) {
    ConstructQueryHandler<Collection<Job>> handler = new ConstructQueryHandler<Collection<Job>>() {
      @Override
      public Collection<Job> handle(RepositoryConnection data, ValueFactory vf) throws Exception {
        Collection<Job> jobs = new ArrayList<>();
        for (Statement statement : StatementIterable.list(data, null, RDF.TYPE, vf.createURI(SemicRdfModel.typeJV))) {
          String label = data.getStatements(statement.getSubject(), SKOS.PREF_LABEL, null, false).next().getObject().stringValue();
          jobs.add(updateJob(new Job(statement.getSubject().toString(), label), data, statement.getSubject()));
        }
        return jobs;
      }
    };
    return doConstructQuery(handler, jvsSparqlQuery, new ImmutablePair<String, Value>("language", sesameDatasource.getValueFactory().createLiteral(language)));
  }


  public Job getJV(final String uri, String language) {
    ValueFactory vf = sesameDatasource.getValueFactory();
    final URI jvURI = vf.createURI(uri);
    ConstructQueryHandler<Job> handler = new ConstructQueryHandler<Job>() {
      @Override
      public Job handle(RepositoryConnection data, ValueFactory vf) throws Exception {
        RepositoryResult<Statement> statements = data.getStatements(jvURI, SKOS.PREF_LABEL, null, false);
        String label = statements.next().getObject().stringValue();
        return updateJob(new Job(uri, label), data, jvURI);
      }
    };
    return doConstructQuery(handler, jvSparqlQuery, new ImmutablePair<String, Value>("jv", jvURI), new ImmutablePair<String, Value>("language", vf.createLiteral(language)));
  }

  public Pair<Set<Skill>, Set<Skill>> getProposedSkillsViaOccupationV0toV1(Job job, String language) {
    List<String> occupationUris = new ArrayList<>();
    for (Occupation occupation : job.getOccupations()) {
      occupationUris.add(occupation.getUri());
    }
    List occupationWrappers = (List) serviceApiBaseClient.getConcepts(occupationUris, language).get("concepts");
    Set<String> newOccupationUris = new HashSet<>();
    for (Object occupationWrapper : occupationWrappers) {
      String releasedWithVersion = (String) ((Map) ((Map) occupationWrapper).get("concept")).get("releasedWithVersion");
      if ("v01".equals(releasedWithVersion)) newOccupationUris.add((String) ((Map) ((Map) occupationWrapper).get("concept")).get("uri"));

      List replacedBys = (List) ((Map) ((Map) occupationWrapper).get("concept")).get("replacedBy");
      if (null == replacedBys || replacedBys.isEmpty()) continue;
      for (Object replacedBy : replacedBys) {
        newOccupationUris.add((String) ((Map) replacedBy).get("uri"));
      }
    }

    Set<Skill> essentialSkills = new HashSet<>();
    Set<Skill> optionalSkills = new HashSet<>();
    List newOccupationWrappers = (List) serviceApiBaseClient.getConcepts(newOccupationUris, language).get("concepts");
    if (null == newOccupationWrappers) newOccupationWrappers = Collections.emptyList();

    for (Object newOccupationWrapper : newOccupationWrappers) {
      List hasRelationships = (List) ((Map) ((Map) newOccupationWrapper).get("concept")).get("hasRelationship");
      if (null == hasRelationships || hasRelationships.isEmpty()) continue;
      for (Object hasRelationshipObject : hasRelationships) {
        Map hasRelationship = (Map) hasRelationshipObject;
        String relationshipType = (String) ((Map) hasRelationship.get("hasRelationshipType")).get("uri");

        Set<Skill> skills;
        if ("http://data.europa.eu/esco/RelationshipType#iC.optionalSkill".equals(relationshipType)) skills = optionalSkills;
        else if ("http://data.europa.eu/esco/RelationshipType#iC.EssentialSkill".equals(relationshipType) || "http://data.europa.eu/esco/RelationshipType#iC.essentialSkill".equals(relationshipType)) skills = optionalSkills;
        else continue;

        String skillUri = (String) ((Map) hasRelationship.get("refersConcept")).get("uri");
        Map skillLabels = (Map) ((Map) hasRelationship.get("refersConcept")).get("prefLabel");
        skills.add(new Skill(skillUri, (String) skillLabels.values().iterator().next()));
      }
    }

    return new ImmutablePair<>(essentialSkills, optionalSkills);
  }

  public Map<Nace, Collection<Occupation>> getNaceProposedOccupations(Job job, String language) {
    Map<Nace, Collection<Occupation>> result = new HashMap<>();
    for (Nace nace : job.getNaces()) {
      List<Occupation> occupations = new ArrayList<>();
      Map<String, Object> values = serviceApiBaseClient.getOccupationsBySubjectTransitive(Collections.singletonList(nace.getUri()), language);
      for (Object conceptWrapper : (List<?>) values.get("concepts")) {
        Map concept = (Map) ((Map) conceptWrapper).get("concept");

        Occupation occupation = new Occupation((String) concept.get("uri"), (String) (((Map) concept.get("prefLabel")).values().iterator().next()));
        for (Object relatedMap : (List) concept.get("related")) {
          if (!((List) ((Map) relatedMap).get("type")).contains("http://data.europa.eu/esco/model#Skill")) continue;
          Skill skill = new Skill((String) ((Map) relatedMap).get("uri"), (String) (((Map) ((Map) relatedMap).get("prefLabel")).values().iterator().next()));
          occupation.addSkill(skill);
        }
        occupations.add(occupation);
      }
      result.put(nace, occupations);
    }
    return result;
  }

  private CV updateCV(CV cv, RepositoryConnection data, Resource cvURI) throws RepositoryException {
    ValueFactory vf = data.getValueFactory();
    for (Statement skillStatement : StatementIterable.list(data, cvURI, vf.createURI(SemicRdfModel.propertyHasSkill_v0), null)) {
      Resource skillResource = (Resource) skillStatement.getObject();
      String skillLabel = getFirstValue(data, skillResource, SKOS.PREF_LABEL, null);
      if (StringUtils.isBlank(skillLabel)) skillLabel = data.getStatements(skillResource, vf.createURI(SemicRdfModel.escoPropertyFallbackLabel), null, false).next().getObject().stringValue();
      Skill skill = new Skill(skillResource.stringValue(), skillLabel);
      cv.addSkill(skill);
    }

    cv.setGivenName(getFirstValue(data, cvURI, FOAF.GIVEN_NAME, StringUtils.substringAfterLast(cvURI.stringValue(), "/")));

    for (Statement skillStatement : StatementIterable.list(data, cvURI, vf.createURI(SemicRdfModel.propertyHasSkill_v1), null)) {
      Resource skillResource = (Resource) skillStatement.getObject();
      String skillLabel = getFirstValue(data, skillResource, SKOS.PREF_LABEL, null);
      if (StringUtils.isBlank(skillLabel)) skillLabel = data.getStatements(skillResource, vf.createURI(SemicRdfModel.escoPropertyFallbackLabel), null, false).next().getObject().stringValue();
      Skill skill = new Skill(skillResource.stringValue(), skillLabel);
      cv.addSkillV1(skill);
    }

    for (Statement occupationStatement : StatementIterable.list(data, cvURI, vf.createURI(SemicRdfModel.propertyHasOccupation_v0), null)) {
      Resource occupationResource = (Resource) occupationStatement.getObject();

      String occupationLabel = getFirstValue(data, occupationResource, SKOS.PREF_LABEL, null);
      if (StringUtils.isBlank(occupationLabel)) occupationLabel = data.getStatements(occupationResource, vf.createURI(SemicRdfModel.escoPropertyFallbackLabel), null, false).next().getObject().stringValue();

      Occupation occupation = new Occupation(occupationResource.stringValue(), occupationLabel);
      for (Statement skillStatement : StatementIterable.list(data, occupationResource, SKOS.RELATED, null)) {
        Resource skillResource = (Resource) skillStatement.getObject();
        String skillLabel = getFirstValue(data, skillResource, SKOS.PREF_LABEL, null);
        if (StringUtils.isBlank(skillLabel)) skillLabel = data.getStatements(skillResource, vf.createURI(SemicRdfModel.escoPropertyFallbackLabel), null, false).next().getObject().stringValue();
        Skill skill = new Skill(skillResource.stringValue(), skillLabel);
        occupation.addSkill(skill);
      }
      cv.addOccupation(occupation);
    }

    for (Statement occupationStatement : StatementIterable.list(data, cvURI, vf.createURI(SemicRdfModel.propertyHasOccupation_v1), null)) {
      Resource occupationResource = (Resource) occupationStatement.getObject();

      String occupationLabel = getFirstValue(data, occupationResource, SKOS.PREF_LABEL, null);
      if (StringUtils.isBlank(occupationLabel)) occupationLabel = data.getStatements(occupationResource, vf.createURI(SemicRdfModel.escoPropertyFallbackLabel), null, false).next().getObject().stringValue();

      Occupation occupation = new Occupation(occupationResource.stringValue(), occupationLabel);
      for (Statement skillStatement : StatementIterable.list(data, occupationResource, SKOS.RELATED, null)) {
        Resource skillResource = (Resource) skillStatement.getObject();
        String skillLabel = getFirstValue(data, skillResource, SKOS.PREF_LABEL, null);
        if (StringUtils.isBlank(skillLabel)) skillLabel = data.getStatements(skillResource, vf.createURI(SemicRdfModel.escoPropertyFallbackLabel), null, false).next().getObject().stringValue();
        Skill skill = new Skill(skillResource.stringValue(), skillLabel);
        occupation.addSkill(skill);
      }
      cv.addOccupationV1(occupation);
    }


    return cv;
  }

  public Job updateJob(Job job, RepositoryConnection data, Resource jvURI) throws RepositoryException {
    ValueFactory vf = data.getValueFactory();
    for (Statement skillStatement : StatementIterable.list(data, jvURI, vf.createURI(SemicRdfModel.propertyHasSkill_v0), null)) {
      Resource skillResource = (Resource) skillStatement.getObject();
      String skillLabel = getFirstValue(data, skillResource, SKOS.PREF_LABEL, null);
      if (StringUtils.isBlank(skillLabel)) skillLabel = data.getStatements(skillResource, vf.createURI(SemicRdfModel.escoPropertyFallbackLabel), null, false).next().getObject().stringValue();
      Skill skill = new Skill(skillResource.stringValue(), skillLabel);
      job.addDirectSkill(skill);
    }

    for (Statement skillStatement : StatementIterable.list(data, jvURI, vf.createURI(SemicRdfModel.propertyHasSkill_v1), null)) {
      Resource skillResource = (Resource) skillStatement.getObject();
      String skillLabel = getFirstValue(data, skillResource, SKOS.PREF_LABEL, null);
      if (StringUtils.isBlank(skillLabel)) skillLabel = data.getStatements(skillResource, vf.createURI(SemicRdfModel.escoPropertyFallbackLabel), null, false).next().getObject().stringValue();
      Skill skill = new Skill(skillResource.stringValue(), skillLabel);
      job.addDirectV1Skill(skill);
    }

    for (Statement occupationStatement : StatementIterable.list(data, jvURI, vf.createURI(SemicRdfModel.propertyHasOccupation_v0), null)) {
      Resource occupationResource = (Resource) occupationStatement.getObject();

      String occupationLabel = getFirstValue(data, occupationResource, SKOS.PREF_LABEL, null);
      if (StringUtils.isBlank(occupationLabel)) occupationLabel = data.getStatements(occupationResource, vf.createURI(SemicRdfModel.escoPropertyFallbackLabel), null, false).next().getObject().stringValue();

      Occupation occupation = new Occupation(occupationResource.stringValue(), occupationLabel);
      for (Statement skillStatement : StatementIterable.list(data, occupationResource, SKOS.RELATED, null)) {
        Resource skillResource = (Resource) skillStatement.getObject();
        String skillLabel = getFirstValue(data, skillResource, SKOS.PREF_LABEL, null);
        if (StringUtils.isBlank(skillLabel)) skillLabel = data.getStatements(skillResource, vf.createURI(SemicRdfModel.escoPropertyFallbackLabel), null, false).next().getObject().stringValue();
        Skill skill = new Skill(skillResource.stringValue(), skillLabel);
        occupation.addSkill(skill);
      }
      job.addOccupation(occupation);
    }

    for (Statement occupationStatement : StatementIterable.list(data, jvURI, vf.createURI(SemicRdfModel.propertyHasOccupation_v1), null)) {
      Resource occupationResource = (Resource) occupationStatement.getObject();

      String occupationLabel = getFirstValue(data, occupationResource, SKOS.PREF_LABEL, null);
      if (StringUtils.isBlank(occupationLabel)) occupationLabel = data.getStatements(occupationResource, vf.createURI(SemicRdfModel.escoPropertyFallbackLabel), null, false).next().getObject().stringValue();

      Occupation occupation = new Occupation(occupationResource.stringValue(), occupationLabel);
      for (Statement skillStatement : StatementIterable.list(data, occupationResource, SKOS.RELATED, null)) {
        Resource skillResource = (Resource) skillStatement.getObject();
        String skillLabel = getFirstValue(data, skillResource, SKOS.PREF_LABEL, null);
        if (StringUtils.isBlank(skillLabel)) skillLabel = data.getStatements(skillResource, vf.createURI(SemicRdfModel.escoPropertyFallbackLabel), null, false).next().getObject().stringValue();
        Skill skill = new Skill(skillResource.stringValue(), skillLabel);
        occupation.addSkill(skill);
      }
      job.addOccupationV1(occupation);
    }

    for (Statement naceStatement : StatementIterable.list(data, jvURI, vf.createURI(SemicRdfModel.propertyHasNace), null)) {
      Resource naceResource = (Resource) naceStatement.getObject();
      String naceLabel = getFirstValue(data, naceResource, SKOS.PREF_LABEL, null);
      if (StringUtils.isBlank(naceLabel)) naceLabel = data.getStatements(naceResource, vf.createURI(SemicRdfModel.escoPropertyFallbackLabel), null, false).next().getObject().stringValue();
      Nace nace = new Nace(naceResource.stringValue(), naceLabel);
      job.addNace(nace);
    }

    job.setDescription(getFirstValue(data, jvURI, DCTERMS.DESCRIPTION, ""));
    job.setEmploymentType(EmploymentType.valueOf(getFirstValue(data, jvURI, vf.createURI(SemicRdfModel.propertyEmploymentType), EmploymentType.notFound.name())));
    job.setExperienceLevel(ExperienceLevel.valueOf(getFirstValue(data, jvURI, vf.createURI(SemicRdfModel.propertyExperienceLevel), ExperienceLevel.notFound.name())));
    job.setGeonameUri(getFirstValue(data, jvURI, vf.createURI(SemicRdfModel.propertyGeonameUri), ""));
    job.setGeonameLabel(getFirstValue(data, jvURI, vf.createURI(SemicRdfModel.propertyGeoname), ""));
    job.setHiringOrganizationName(getFirstValue(data, jvURI, vf.createURI(SemicRdfModel.propertyHiringOrganizationName), ""));
    job.setOccupationalCategory(getFirstValue(data, jvURI, vf.createURI(SemicRdfModel.propertyOccupationalCategory), ""));

    return job;
  }

  public Collection<CV> getMatchingCvs(Job toMatchJV, String language) {
    ConstructQueryHandler<Collection<CV>> handler = new ConstructQueryHandler<Collection<CV>>() {
      @Override
      public Collection<CV> handle(RepositoryConnection data, ValueFactory vf) throws Exception {
        Collection<CV> cvs = new ArrayList<>();
        for (Statement statement : StatementIterable.list(data, null, RDF.TYPE, vf.createURI(SemicRdfModel.typeCV))) {
          String descriptionValue = getFirstValue(data, statement.getSubject(), DCTERMS.DESCRIPTION, null);
          String cvUri = statement.getSubject().stringValue();
          CV cv = new CV(cvUri, null == descriptionValue ? cvUri : descriptionValue, documentService.hasDocumentContent(cvUri));
          cvs.add(updateCV(cv, data, statement.getSubject()));
        }
        return cvs;
      }
    };
    return doConstructQuery(handler, matchingCvsSparqlQuery,
                            new ImmutablePair<String, Value>("jv", sesameDatasource.getValueFactory().createURI(toMatchJV.getUri())),
                            new ImmutablePair<String, Value>("language", sesameDatasource.getValueFactory().createLiteral(language)));
  }

  public Collection<CV> getCVs(Job toMatchJV, String language) {
    String query = "select ?cv ?description ?skill ?skillPrefLabel ?skillFallbackLabel ?skillV1 ?skillV1PrefLabel ?skillV1FallbackLabel " +
                   "       ?occupationV0 ?occupationV0PrefLabel ?occupationV0FallbackLabel where { " +
                   "  ?cv <" + RDF.TYPE.stringValue() + "> <" + SemicRdfModel.typeCV + "> . " +
                   "  OPTIONAL { ?cv <" + DCTERMS.DESCRIPTION.stringValue() + "> ?description } . " +
                   "  OPTIONAL { " +
                   "     ?cv <" + SemicRdfModel.propertyHasSkill_v0 + "> ?skill . " +
                   "     ?skill <http://www.tenforce.com/esco/java#fallbackLabel> ?skillFallbackLabel . " +
                   "     OPTIONAL { ?skill <" + SKOS.PREF_LABEL.stringValue() + "> ?skillPrefLabel . filter(lang(?skillPrefLabel) = '" + language + "') }" +
                   "   } . " +
                   "   OPTIONAL { " +
                   "     ?cv <" + SemicRdfModel.propertyHasSkill_v1 + "> ?skillV1 . " +
                   "     ?skillV1 <http://www.tenforce.com/esco/java#fallbackLabel> ?skillV1FallbackLabel . " +
                   "     OPTIONAL { ?skillV1 <" + SKOS.PREF_LABEL.stringValue() + "> ?skillV1PrefLabel . filter(lang(?skillV1PrefLabel) = '" + language + "') }" +
                   "   } . " +
                   "   OPTIONAL { " +
                   "     ?cv <" + SemicRdfModel.propertyHasOccupation_v0 + "> ?occupationV0 . " +
                   "     ?occupationV0 <http://www.tenforce.com/esco/java#fallbackLabel> ?occupationV0FallbackLabel . " +
                   "     OPTIONAL { ?occupationV0 <" + SKOS.PREF_LABEL.stringValue() + "> ?occupationV0PrefLabel . filter(lang(?occupationV0PrefLabel) = '" + language + "') }" +
                   "   } . " +
                   "   FILTER exists { ?cv <" + SemicRdfModel.propertyInSector + "> <" + SemicRdfModel.sectorHosp + "> . } " +
                   "   FILTER exists { " +
                   "       OPTIONAL { <" + toMatchJV.getUri() + "> <" + SemicRdfModel.propertyHasSkill_v0 + "> ?skill . } " +
                   "       OPTIONAL { <" + toMatchJV.getUri() + "> <" + SemicRdfModel.propertyHasSkill_v1 + "> ?skillV1 . } " +
                   "   } " +
                   " } ";
    return getCVsByQuery(query);
  }

  public void updateCvSkillFieldsImplicit(Collection<CV> cvs, Job toMatchJV) {
    updateCvSkillFields(cvs, toMatchJV.getDirectSkills(), toMatchJV.getDirectV1Skills());
  }

  public void updateCvSkillFieldsExplicit(Collection<CV> cvs, Job toMatchJV) {
    updateCvSkillFields(cvs, toMatchJV.getAllSkills(), toMatchJV.getAllV1Skills());
  }

  private void updateCvSkillFields(Collection<CV> cvs, Collection<Skill> jvV0Skills, Collection<Skill> jvV1Skills) {
    for (CV cv : cvs) {
      Set<Skill> matchingSkillsV1 = new HashSet<>(cv.getSkillsV1());
      matchingSkillsV1.retainAll(jvV1Skills);
      cv.addMatchingSkillsV1(matchingSkillsV1);

      Set<Skill> missingSkillsV1 = new HashSet<>(jvV1Skills);
      missingSkillsV1.removeAll(cv.getSkillsV1());
      cv.addMissingSkillsV1(missingSkillsV1);

      Set<Skill> additionalSkillsV1 = new HashSet<>(cv.getSkillsV1());
      additionalSkillsV1.removeAll(jvV1Skills);
      cv.addAdditionalSkillsV1(additionalSkillsV1);

      Set<Skill> matchingSkillsV0 = new HashSet<>(cv.getSkills());
      matchingSkillsV0.retainAll(jvV0Skills);
      cv.addMatchingSkills(matchingSkillsV0);

      Set<Skill> missingSkillsV0 = new HashSet<>(jvV0Skills);
      missingSkillsV0.removeAll(cv.getSkills());
      cv.addMissingSkills(missingSkillsV0);

      Set<Skill> additionalSkillsV0 = new HashSet<>(cv.getSkills());
      additionalSkillsV0.removeAll(jvV0Skills);
      cv.addAdditionalSkills(additionalSkillsV0);

      cv.setScoreV1(jvV1Skills.isEmpty() ? 0 : 100 * cv.getMatchingSkillsV1().size() / jvV1Skills.size());
      cv.setScoreV0(jvV0Skills.isEmpty() ? 0 : 100 * cv.getMatchingSkills().size() / jvV0Skills.size());
    }
  }

  public void updateCvSkillFieldsCombined(Collection<CV> cvs, Job toMatchJV) {
    for (CV cv : cvs) {
      Set<Skill> matchingDirectSkillsV1 = new HashSet<>(cv.getSkillsV1());
      matchingDirectSkillsV1.retainAll(toMatchJV.getDirectV1Skills());
      Set<Skill> matchingIndirectSkillsV1 = new HashSet<>(cv.getSkillsV1());
      matchingIndirectSkillsV1.retainAll(toMatchJV.getAllV1Skills());
      cv.addMatchingSkillsV1(matchingDirectSkillsV1);
      cv.addMatchingSkillsV1(matchingIndirectSkillsV1);

      Set<Skill> missingSkillsV1 = new HashSet<>(toMatchJV.getAllV1Skills());
      missingSkillsV1.addAll(toMatchJV.getDirectV1Skills());
      missingSkillsV1.removeAll(cv.getSkillsV1());
      cv.addMissingSkillsV1(missingSkillsV1);

      Set<Skill> additionalSkillsV1 = new HashSet<>(cv.getSkillsV1());
      additionalSkillsV1.removeAll(toMatchJV.getAllV1Skills());
      additionalSkillsV1.removeAll(toMatchJV.getDirectV1Skills());
      cv.addAdditionalSkillsV1(additionalSkillsV1);


      Set<Skill> matchingDirectSkillsV0 = new HashSet<>(cv.getSkills());
      matchingDirectSkillsV0.retainAll(toMatchJV.getDirectSkills());
      Set<Skill> matchingIndirectSkillsV0 = new HashSet<>(cv.getSkills());
      matchingIndirectSkillsV0.retainAll(toMatchJV.getAllSkills());
      cv.addMatchingSkills(matchingDirectSkillsV0);
      cv.addMatchingSkills(matchingIndirectSkillsV0);

      Set<Skill> missingSkillsV0 = new HashSet<>(toMatchJV.getAllSkills());
      missingSkillsV0.addAll(toMatchJV.getDirectSkills());
      missingSkillsV0.removeAll(cv.getSkills());
      cv.addMissingSkills(missingSkillsV0);

      Set<Skill> additionalSkillsV0 = new HashSet<>(cv.getSkills());
      additionalSkillsV0.removeAll(toMatchJV.getAllSkills());
      additionalSkillsV0.removeAll(toMatchJV.getDirectSkills());
      cv.addAdditionalSkills(additionalSkillsV0);

      int scoreV1 = toMatchJV.getDirectV1Skills().isEmpty() ? 0 : 66 * matchingDirectSkillsV1.size() / toMatchJV.getDirectV1Skills().size();
      scoreV1 += toMatchJV.getAllV1Skills().isEmpty() ? 0 : 34 * matchingIndirectSkillsV1.size() / toMatchJV.getAllV1Skills().size();

      int scoreV0 = toMatchJV.getDirectSkills().isEmpty() ? 0 : 66 * matchingDirectSkillsV0.size() / toMatchJV.getDirectSkills().size();
      scoreV0 += toMatchJV.getAllSkills().isEmpty() ? 0 : 34 * matchingIndirectSkillsV0.size() / toMatchJV.getAllSkills().size();

      cv.setScoreV1(scoreV1);
      cv.setScoreV0(scoreV0);
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

  public interface ConstructQueryHandler<T> {
    T handle(RepositoryConnection data, ValueFactory valueFactory) throws Exception;
  }

  public interface SelectQueryHandler<T> {
    void handleRow(BindingSet bindingSet, ValueFactory valueFactory) throws Exception;

    T getResult();
  }


  public <T> T doConstructQuery(ConstructQueryHandler<T> handler, String query, Pair<String, Value>... bindings) {
    RepositoryConnection sourceConnection = null;
    RepositoryConnection resultConnection = null;
    Repository resultRepository = new SailRepository(new MemoryStore());
    try {
      resultRepository.initialize();
      sourceConnection = sesameDatasource.getConnection();
      resultConnection = resultRepository.getConnection();

      GraphQuery graphQuery = sourceConnection.prepareGraphQuery(QueryLanguage.SPARQL, query);
      for (Pair<String, Value> binding : bindings) {
        graphQuery.setBinding(binding.getLeft(), binding.getRight());
      }

      log.info("Executing query: {}", query);
      long start = System.currentTimeMillis();
      GraphQueryResult graphQueryResult = graphQuery.evaluate();
      long endQuery = System.currentTimeMillis();
      log.info("Query done in {} ms", (endQuery - start));

      resultConnection.add(graphQueryResult);

      log.info("finished adding all results");

      resultConnection.commit();
      log.info("finished adding all results and commit");
      T result = handler.handle(resultConnection, resultConnection.getValueFactory());


      com.tenforce.sesame.SesameUtils.output(resultRepository, new File("c:\\temp\\testsemic.rdf"));
      long done = System.currentTimeMillis();
      log.info("Query result handled in {} ms", (done - endQuery));
      return result;
    }
    catch (Exception e) {
      throw TenforceException.rethrow(e);
    }
    finally {
      SesameUtils.closeQuietly(sourceConnection);
      SesameUtils.closeQuietly(resultConnection);
      SesameUtils.shutDownQuietly(resultRepository);
    }
  }

  public <T> T doSelectQuery(SelectQueryHandler<T> handler, String query, Pair<String, Value>... bindings) {
    RepositoryConnection sourceConnection = null;
    try {
      sourceConnection = sesameDatasource.getConnection();
      ValueFactory valueFactory = sourceConnection.getValueFactory();

      TupleQuery tupleQuery = sourceConnection.prepareTupleQuery(QueryLanguage.SPARQL, query);
      for (Pair<String, Value> binding : bindings) {
        tupleQuery.setBinding(binding.getLeft(), binding.getRight());
      }

      log.info("Executing query: {}", query);
      long start = System.currentTimeMillis();
      TupleQueryResult tupleQueryResult = tupleQuery.evaluate();
      long endQuery = System.currentTimeMillis();
      log.info("Query done in {} ms", (endQuery - start));
      while (tupleQueryResult.hasNext()) {
        handler.handleRow(tupleQueryResult.next(), valueFactory);
      }
      long done = System.currentTimeMillis();
      log.info("Query result handled in {} ms", (done - endQuery));
      return handler.getResult();
    }
    catch (Exception e) {
      throw TenforceException.rethrow(e);
    }
    finally {
      SesameUtils.closeQuietly(sourceConnection);
    }
  }

  private List<Map<String, Value>> getResultSet(final String sparql) {
    final SesameRdfTemplate sesameRdfTemplate = new SesameRdfTemplate(sesameDatasource);

    return sesameRdfTemplate.execute(new RepoConnectionCallback<List<Map<String, Value>>>() {
      @Override
      public List<Map<String, Value>> doInConnection(RepositoryConnection repositoryConnection) {
        return sesameRdfTemplate.query(repositoryConnection, sparql, new SesameResultSetExtractor<List<Map<String, Value>>>() {
          @Override
          public List<Map<String, Value>> extractData(List<Map<String, Value>> resultSet) {
            return resultSet;
          }
        });
      }
    });
  }
}
