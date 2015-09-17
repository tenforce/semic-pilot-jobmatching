package tools;

import com.tenforce.exception.TenforceException;
import eu.esco.demo.jobcvmatching.root.HardcodedConfiguration;
import eu.esco.demo.jobcvmatching.root.SemicRdfModel;
import eu.esco.demo.jobcvmatching.root.model.CV;
import eu.esco.demo.jobcvmatching.root.model.Occupation;
import eu.esco.demo.jobcvmatching.root.model.Skill;
import eu.esco.demo.jobcvmatching.root.service.RemoteSesameDatasource;
import eu.esco.demo.jobcvmatching.sesame.StatementIterable;
import org.apache.commons.lang3.StringUtils;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Testing {
  private static final Logger log = LoggerFactory.getLogger(Testing.class);

  private static final String query = "" +
                                      "\n" +
                                      "construct {\n" +
                                      "   ?cv <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://data.europa.eu/esco/semic/model#CV> .\n" +
                                      "   ?cv <http://purl.org/dc/terms/description> ?description .\n" +
                                      "\n" +
                                      "   ?cv <http://data.europa.eu/esco/semic/model#hasSkill> ?directSkill .\n" +
                                      "   ?directSkill <http://www.tenforce.com/esco/java#fallbackLabel> ?directSkillFallbackLabel .\n" +
                                      "   ?directSkill <http://www.w3.org/2004/02/skos/core#prefLabel> ?directSkillPrefLabel .\n" +
                                      "\n" +
                                      "   ?cv <http://data.europa.eu/esco/semic/model#hasSkill_v1> ?directSkill_v1 .\n" +
                                      "   ?directSkill_v1 <http://www.tenforce.com/esco/java#fallbackLabel> ?directSkill_v1FallbackLabel .\n" +
                                      "   ?directSkill_v1 <http://www.w3.org/2004/02/skos/core#prefLabel> ?directSkill_v1PrefLabel .\n" +
                                      "\n" +
                                      "   ?cv <http://data.europa.eu/esco/semic/model#hasOccupation> ?occupation .\n" +
                                      "   ?occupation <http://www.tenforce.com/esco/java#fallbackLabel> ?occupationFallbackLabel .\n" +
                                      "   ?occupation <http://www.w3.org/2004/02/skos/core#prefLabel> ?occupationPrefLabel .\n" +
                                      "   ?occupation <http://www.w3.org/2004/02/skos/core#related> ?skill_forOccupation .\n" +
                                      "   ?skill_forOccupation <http://www.tenforce.com/esco/java#fallbackLabel> ?skill_forOccupationFallbackLabel .\n" +
                                      "   ?skill_forOccupation <http://www.w3.org/2004/02/skos/core#prefLabel> ?skill_forOccupationPrefLabel .\n" +
                                      "\n" +
                                      "   ?cv <http://data.europa.eu/esco/semic/model#hasOccupation_v1> ?occupation_v1 .\n" +
                                      "   ?occupation_v1 <http://www.tenforce.com/esco/java#fallbackLabel> ?occupation_v1FallbackLabel .\n" +
                                      "   ?occupation_v1 <http://www.w3.org/2004/02/skos/core#prefLabel> ?occupation_v1PrefLabel .\n" +
                                      "   ?occupation_v1 <http://www.w3.org/2004/02/skos/core#related> ?skill_forOccupation_v1 .\n" +
                                      "   ?skill_forOccupation_v1 <http://www.tenforce.com/esco/java#fallbackLabel> ?skill_forOccupation_v1FallbackLabel .\n" +
                                      "   ?skill_forOccupation_v1 <http://www.w3.org/2004/02/skos/core#prefLabel> ?skill_forOccupation_v1PrefLabel .\n" +
                                      "\n" +
                                      "}\n" +
                                      "where {\n" +
                                      "\n" +
                                      "\n" +
                                      "\n" +
                                      "\n" +
                                      "  ?cv <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://data.europa.eu/esco/semic/model#CV> .\n" +
                                      "  OPTIONAL { ?cv <http://purl.org/dc/terms/description> ?description . } .\n" +
                                      "\n" +
                                      "  FILTER exists { ?cv <http://data.europa.eu/esco/semic/model#inSector> <http://data.europa.eu/esco/semic/sector#HOSP> . } .\n" +

                                      "\n" +
                                      "\n" +
                                      "{\n" +
                                      "  OPTIONAL { ?cv <http://data.europa.eu/esco/semic/model#hasSkill> ?directSkill .\n" +
                                      "             ?directSkill <http://www.tenforce.com/esco/java#fallbackLabel> ?directSkillFallbackLabel .\n" +
                                      "               OPTIONAL {\n" +
                                      "                  ?directSkill <http://www.w3.org/2004/02/skos/core#prefLabel> ?directSkillPrefLabel . filter(lang(?directSkillPrefLabel) = ?language)\n" +
                                      "               }\n" +
                                      "  }\n" +
                                      "}\n" +
                                      "UNION\n" +
                                      "{\n" +
                                      "  OPTIONAL { ?cv <http://data.europa.eu/esco/semic/model#hasSkill_v1> ?directSkill_v1 .\n" +
                                      "             ?directSkill_v1 <http://www.tenforce.com/esco/java#fallbackLabel> ?directSkill_v1FallbackLabel .\n" +
                                      "               OPTIONAL {\n" +
                                      "                  ?directSkill_v1 <http://www.w3.org/2004/02/skos/core#prefLabel> ?directSkill_v1PrefLabel . filter(lang(?directSkill_v1PrefLabel) = ?language)\n" +
                                      "               }\n" +
                                      "  }\n" +
                                      "}\n" +
                                      "UNION\n" +
                                      "{\n" +
                                      "  OPTIONAL { ?cv <http://data.europa.eu/esco/semic/model#hasOccupation> ?occupation .\n" +
                                      "             ?occupation <http://www.tenforce.com/esco/java#fallbackLabel> ?occupationFallbackLabel .\n" +
                                      "             OPTIONAL {\n" +
                                      "                ?occupation <http://www.w3.org/2004/02/skos/core#prefLabel> ?occupationPrefLabel . filter(lang(?occupationPrefLabel) = ?language)\n" +
                                      "             }\n" +
                                      "        OPTIONAL {\n" +
                                      "             ?occupation <http://www.w3.org/2004/02/skos/core#related> ?skill .\n" +
                                      "             ?skill <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://data.europa.eu/esco/model#Skill> .\n" +
                                      "             ?skill <http://www.tenforce.com/esco/java#fallbackLabel> ?skillFallbackLabel .\n" +
                                      "             OPTIONAL {\n" +
                                      "                ?skill <http://www.w3.org/2004/02/skos/core#prefLabel> ?skillPreflLabel . filter(lang(?skillPreflLabel) = ?language)\n" +
                                      "             }\n" +
                                      "        }\n" +
                                      "  }\n" +
                                      "}\n" +
                                      "UNION\n" +
                                      "{\n" +
                                      "  OPTIONAL { ?cv <http://data.europa.eu/esco/semic/model#hasOccupation_v1> ?occupation_v1 .\n" +
                                      "             ?occupation_v1 <http://www.tenforce.com/esco/java#fallbackLabel> ?occupation_v1FallbackLabel .\n" +
                                      "             OPTIONAL {\n" +
                                      "                ?occupation_v1 <http://www.w3.org/2004/02/skos/core#prefLabel> ?occupation_v1PrefLabel . filter(lang(?occupation_v1PrefLabel) = ?language)\n" +
                                      "             }\n" +
                                      "        OPTIONAL {\n" +
                                      "             ?occupation_v1 <http://data.europa.eu/esco/model#relatedEssentialSkill> ?skill_forOccupation_v1 .\n" +
                                      "             ?skill_forOccupation_v1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://data.europa.eu/esco/model#Skill> .\n" +
                                      "             ?skill_forOccupation_v1 <http://www.tenforce.com/esco/java#fallbackLabel> ?skill_forOccupation_v1FallbackLabel .\n" +
                                      "             OPTIONAL {\n" +
                                      "                ?skill_forOccupation_v1 <http://www.w3.org/2004/02/skos/core#prefLabel> ?skill_forOccupation_v1PrefLabel . filter(lang(?skill_forOccupation_v1PrefLabel) = ?language)\n" +
                                      "             }\n" +
                                      "        }\n" +
                                      "  }\n" +
                                      "}}\n";

  public static void main(String[] args) throws Exception {
    log.error("start");
    String rdfUser = "escordf";
    String rdfPassword = "secret";
    RemoteSesameDatasource destinationDataSource = new RemoteSesameDatasource(HardcodedConfiguration.sesameUrl, HardcodedConfiguration.sesameRepository, rdfUser, rdfPassword);
    RepositoryConnection queryConnection = destinationDataSource.getConnection();

    Repository resultRepository = new SailRepository(new MemoryStore());
    resultRepository.initialize();
    RepositoryConnection tempConnection = resultRepository.getConnection();
    log.error("init done");

    GraphQuery graphQuery = queryConnection.prepareGraphQuery(QueryLanguage.SPARQL, query);
    GraphQueryResult evaluate = graphQuery.evaluate();

    log.error("evaluate done");

    while (evaluate.hasNext()) {
      tempConnection.add(evaluate.next());
    }
    log.error("copy all done");
    handleAll(tempConnection);
    log.error("handle all done");
    tempConnection.close();
    queryConnection.close();
    destinationDataSource.closeQuietly();
    resultRepository.shutDown();
    log.error("all closed");
  }

  private static final Map<String, CV> cvPerUri = new HashMap<>();
  private static final Map<String, Set<CV>> cvPerSkillV0 = new HashMap<>();
  private static final Map<String, Set<CV>> cvPerSkillV1 = new HashMap<>();


  private static void handleAll(RepositoryConnection tempConnection) throws Exception {

    for (Statement statement : StatementIterable.list(tempConnection, null, RDF.TYPE, tempConnection.getValueFactory().createURI(SemicRdfModel.typeCV))) {
      String descriptionValue = getFirstValue(tempConnection, statement.getSubject(), DCTERMS.DESCRIPTION, null);
      String cvUri = statement.getSubject().stringValue();
      CV cv = new CV(cvUri, null == descriptionValue ? cvUri : descriptionValue, false);
      cvPerUri.put(cvUri, updateCV(cv, tempConnection, statement.getSubject()));
    }
    log.error("created all CV's");
    for (CV cv : cvPerUri.values()) {
      for (Skill skill : cv.getSkills()) {
        Set<CV> cvs = cvPerSkillV0.get(skill.getUri());
        if(null == cvs)cvPerSkillV0.put(skill.getUri(), cvs = new HashSet<>());
        cvs.add(cv);
      }
      for (Skill skill : cv.getSkillsV1()) {
        Set<CV> cvs = cvPerSkillV1.get(skill.getUri());
        if(null == cvs)cvPerSkillV1.put(skill.getUri(), cvs = new HashSet<>());
        cvs.add(cv);
      }

    }
    log.error("Sorted all CV's: " + cvPerUri.size());
  }

  private static String getFirstValue(RepositoryConnection data, Resource resource, URI property, String fallback) {

    try {
      RepositoryResult<Statement> statements = data.getStatements(resource, property, null, false);
      return statements.hasNext() ? statements.next().getObject().stringValue() : fallback;
    }
    catch (RepositoryException e) {
      throw new TenforceException(e);
    }
  }


  private static CV updateCV(CV cv, RepositoryConnection data, Resource cvURI) throws RepositoryException {
    ValueFactory vf = data.getValueFactory();
    for (Statement skillStatement : StatementIterable.list(data, cvURI, vf.createURI(SemicRdfModel.propertyHasSkill_v0), null)) {
      Resource skillResource = (Resource) skillStatement.getObject();
      String skillLabel = getFirstValue(data, skillResource, SKOS.PREF_LABEL, null);
      if (StringUtils.isBlank(skillLabel)) skillLabel = data.getStatements(skillResource, vf.createURI(SemicRdfModel.escoPropertyFallbackLabel), null, false).next().getObject().stringValue();
      Skill skill = new Skill(skillResource.stringValue(), skillLabel);
      cv.addSkill(skill);
    }

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

}
