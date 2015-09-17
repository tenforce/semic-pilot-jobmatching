package eu.esco.semic.pilot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.esco.semic.pilot.model.CV;
import eu.esco.semic.pilot.model.Job;
import eu.esco.semic.pilot.model.MatchingObject;
import eu.esco.semic.pilot.model.Nace;
import eu.esco.semic.pilot.model.Occupation;
import eu.esco.semic.pilot.model.Skill;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CachedDataService {
  private final Map<String, Map<Object, Object>> cvs = Collections.synchronizedMap(new HashMap<String, Map<Object, Object>>());
  private final Map<String, Map<Object, Object>> jvs = Collections.synchronizedMap(new HashMap<String, Map<Object, Object>>());
  private final Map<String, Map<Object, Object>> naces = Collections.synchronizedMap(new HashMap<String, Map<Object, Object>>());
  private final Map<String, Map<Object, Object>> occupations = Collections.synchronizedMap(new HashMap<String, Map<Object, Object>>());
  private final Map<String, Map<Object, Object>> skills = Collections.synchronizedMap(new HashMap<String, Map<Object, Object>>());
  //  private final Map<String, List<CV>> cvs = Collections.synchronizedMap(new HashMap<String, List<CV>>());

  @PostConstruct
  private void init() throws IOException {
    InputStream cacheStream = CachedDataService.class.getClassLoader().getResourceAsStream("cache.json");

    Map map = new ObjectMapper().readValue(cacheStream, Map.class);
    initMap(naces, (Map<?, ?>) map.get("naces"));
    initMap(occupations, (Map<?, ?>) map.get("occupations"));
    initMap(skills, (Map<?, ?>) map.get("skills"));
    initMap(jvs, (Map<?, ?>) map.get("jvs"));
    initMap(cvs, (Map<?, ?>) map.get("cvs"));
  }

  private void initMap(Map<String, Map<Object, Object>> toFill, Map<?, ?> skillMap) {
    for (Map.Entry<?, ?> entry : skillMap.entrySet()) {
      String uri = (String) entry.getKey();
      toFill.put(uri, (Map) entry.getValue());
    }
  }

  public Map<String, String> getJVsUriWithLabel() {
    return getUriWithLabel(jvs, "label");
  }

  public Map<String, String> getCVsUriWithLabel() {
    return getUriWithLabel(cvs, "givenName");
  }

  private Map<String, String> getUriWithLabel(Map<String, Map<Object, Object>> data, String key) {
    Map<String, String> result = new HashMap<>();
    for (Map.Entry<String, Map<Object, Object>> entry : data.entrySet()) {
      String value = (String) entry.getValue().get(key);
      if (null == value) value = StringUtils.substringAfterLast(entry.getKey(), "/");
      result.put(entry.getKey(), value);
    }
    return result;
  }

  public CV getCV(String uri, String language) {
    Map<Object, Object> values = cvs.get(uri);

    CV cv = new CV(uri, (String) values.get("description"), false);
    cv.setGivenName((String) values.get("givenName"));
//    cv.addSkillsV0(getSkills((List) values.get("skillV0"), language));
    cv.addSkillsV1(getSkills((List) values.get("skillV1"), language));
//    cv.addOccupationsV0(getOccupations((List) values.get("occupationV0"), language));
    cv.addOccupationsV1(getOccupations((List) values.get("occupationV1"), language));
    return cv;
  }

  public Job getJV(String uri, String language) {
    Map<Object, Object> values = jvs.get(uri);

    Job job = new Job(uri, (String) values.get("label"));
    job.setDescription((String) values.get("description"));
    job.setEmploymentType((String) values.get("employmentType"));
    job.setExperienceLevel((String) values.get("experienceLevel"));
    job.setGeonameLabel((String) values.get("geonameLabel"));
    job.setGeonameUri((String) values.get("geonameUri"));
    job.setHiringOrganizationName((String) values.get("hiringOrganizationName"));
    job.setOccupationalCategory((String) values.get("occupationalCategory"));

//    job.addDirectSkills(getSkills((List) values.get("skillV0"), language));
    job.addDirectV1Skills(getSkills((List) values.get("skillV1"), language));
//    job.addOccupations(getOccupations((List) values.get("occupationV0"), language));
    job.addOccupationsV1(getOccupations((List) values.get("occupationV1"), language));
    job.addNaces(getNaces((List) values.get("nace"), language));

    return job;
  }

  private List<Nace> getNaces(List uris, String language) {
    List<Nace> result = new ArrayList<>();
    for (Object uri : uris) {
      Map<Object, Object> info = naces.get(uri);
      result.add(new Nace((String) uri, getLabel(info, language)));

    }
    return result;
  }

  private List<Skill> getSkills(List uris, String language) {
    List<Skill> result = new ArrayList<>();
    for (Object uri : uris) {
      Map<Object, Object> info = skills.get(uri);
      result.add(new Skill((String) uri, getLabel(info, language)));
    }
    return result;
  }

  private List<Occupation> getOccupations(List uris, String language) {
    List<Occupation> result = new ArrayList<>();
    for (Object uri : uris) {
      Map<Object, Object> info = occupations.get(uri);
      Occupation occupation = new Occupation((String) uri, getLabel(info, language));
      occupation.addSkills(getSkills((List) info.get("skills"), language));
      result.add(occupation);
    }
    return result;
  }

  private String getLabel(Map<Object, Object> info, String language) {
    String label = (String) info.get("prefLabel" + language);
    return StringUtils.isBlank(label) ? (String) info.get("fallbackLabel") : label;
  }

  public List<Job> getJVs(String language) {
    List<Job> result = new ArrayList<>();
    for (String jvUri : jvs.keySet()) {
      result.add(getJV(jvUri, language));
    }
    return result;
  }

  public List<CV> getCVs(String language) {
    List<CV> result = new ArrayList<>();
    for (String cvUri : cvs.keySet()) {
      result.add(getCV(cvUri, language));
    }
    return result;
  }

  public void updateCvSkillFieldsExplicit(Collection<CV> cvs, Job toMatchJV) {
    for (CV cv : cvs) {
      updateCvSkillFields(cv, cv.getSkills(), cv.getSkillsV1(), toMatchJV.getDirectSkills(), toMatchJV.getDirectV1Skills());
    }
  }

  public void updateCvSkillFieldsExplicit(CV cv, Collection<Job> toMatchJVs) {
    for (Job toMatchJV : toMatchJVs) {
      updateCvSkillFields(toMatchJV, cv.getSkills(), cv.getSkillsV1(), toMatchJV.getDirectSkills(), toMatchJV.getDirectV1Skills());
    }
  }

  public void updateCvSkillFieldsImplicit(Collection<CV> cvs, Job toMatchJV) {
    for (CV cv : cvs) {
      updateCvSkillFields(cv, cv.getSkills(), cv.getSkillsV1(), toMatchJV.getAllSkills(), toMatchJV.getAllV1Skills());
    }
  }

  public void updateCvSkillFieldsImplicit(CV cv, Collection<Job> toMatchJVs) {
    for (Job toMatchJV : toMatchJVs) {
      updateCvSkillFields(toMatchJV, cv.getSkills(), cv.getSkillsV1(), toMatchJV.getAllSkills(), toMatchJV.getAllV1Skills());
    }
  }

  private void updateCvSkillFields(MatchingObject matchingObject, Collection<Skill> cvV0Skills, Collection<Skill> cvV1Skills, Collection<Skill> jvV0Skills, Collection<Skill> jvV1Skills) {
    Set<Skill> matchingSkillsV1 = new HashSet<>(cvV1Skills);
    matchingSkillsV1.retainAll(jvV1Skills);
    matchingObject.addMatchingSkillsV1(matchingSkillsV1);

    Set<Skill> missingSkillsV1 = new HashSet<>(jvV1Skills);
    missingSkillsV1.removeAll(cvV1Skills);
    matchingObject.addMissingSkillsV1(missingSkillsV1);

    Set<Skill> additionalSkillsV1 = new HashSet<>(cvV1Skills);
    additionalSkillsV1.removeAll(jvV1Skills);
    matchingObject.addAdditionalSkillsV1(additionalSkillsV1);

//    Set<Skill> matchingSkillsV0 = new HashSet<>(cvV0Skills);
//    matchingSkillsV0.retainAll(jvV0Skills);
//    matchingObject.addMatchingSkills(matchingSkillsV0);
//
//    Set<Skill> missingSkillsV0 = new HashSet<>(jvV0Skills);
//    missingSkillsV0.removeAll(cvV0Skills);
//    matchingObject.addMissingSkills(missingSkillsV0);
//
//    Set<Skill> additionalSkillsV0 = new HashSet<>(cvV0Skills);
//    additionalSkillsV0.removeAll(jvV0Skills);
//    matchingObject.addAdditionalSkills(additionalSkillsV0);

    matchingObject.setScoreV1(jvV1Skills.isEmpty() ? 0 : 100 * matchingObject.getMatchingSkillsV1().size() / jvV1Skills.size());
//    matchingObject.setScoreV0(jvV0Skills.isEmpty() ? 0 : 100 * matchingObject.getMatchingSkills().size() / jvV0Skills.size());
  }

  public void updateCvSkillFieldsCombined(Collection<CV> cvs, Job toMatchJV) {
    for (CV cv : cvs) {
      updateCvSkillFieldsCombined(cv, cv, toMatchJV);
    }
  }

  public void updateCvSkillFieldsCombined(CV cv, Collection<Job> jvs) {
    for (Job jv : jvs) {
      updateCvSkillFieldsCombined(jv, cv, jv);
    }
  }

  private void updateCvSkillFieldsCombined(MatchingObject matchingObject, CV cv, Job toMatchJV) {
    Set<Skill> matchingDirectSkillsV1 = new HashSet<>(cv.getSkillsV1());
    matchingDirectSkillsV1.retainAll(toMatchJV.getDirectV1Skills());
    Set<Skill> matchingIndirectSkillsV1 = new HashSet<>(cv.getSkillsV1());
    matchingIndirectSkillsV1.retainAll(toMatchJV.getAllV1Skills());
    matchingObject.addMatchingSkillsV1(matchingDirectSkillsV1);
    matchingObject.addMatchingSkillsV1(matchingIndirectSkillsV1);

    Set<Skill> missingSkillsV1 = new HashSet<>(toMatchJV.getAllV1Skills());
    missingSkillsV1.addAll(toMatchJV.getDirectV1Skills());
    missingSkillsV1.removeAll(cv.getSkillsV1());
    matchingObject.addMissingSkillsV1(missingSkillsV1);

    Set<Skill> additionalSkillsV1 = new HashSet<>(cv.getSkillsV1());
    additionalSkillsV1.removeAll(toMatchJV.getAllV1Skills());
    additionalSkillsV1.removeAll(toMatchJV.getDirectV1Skills());
    matchingObject.addAdditionalSkillsV1(additionalSkillsV1);


//    Set<Skill> matchingDirectSkillsV0 = new HashSet<>(cv.getSkills());
//    matchingDirectSkillsV0.retainAll(toMatchJV.getDirectSkills());
//    Set<Skill> matchingIndirectSkillsV0 = new HashSet<>(cv.getSkills());
//    matchingIndirectSkillsV0.retainAll(toMatchJV.getAllSkills());
//    matchingObject.addMatchingSkills(matchingDirectSkillsV0);
//    matchingObject.addMatchingSkills(matchingIndirectSkillsV0);
//
//    Set<Skill> missingSkillsV0 = new HashSet<>(toMatchJV.getAllSkills());
//    missingSkillsV0.addAll(toMatchJV.getDirectSkills());
//    missingSkillsV0.removeAll(cv.getSkills());
//    matchingObject.addMissingSkills(missingSkillsV0);
//
//    Set<Skill> additionalSkillsV0 = new HashSet<>(cv.getSkills());
//    additionalSkillsV0.removeAll(toMatchJV.getAllSkills());
//    additionalSkillsV0.removeAll(toMatchJV.getDirectSkills());
//    matchingObject.addAdditionalSkills(additionalSkillsV0);

    int scoreV1 = toMatchJV.getDirectV1Skills().isEmpty() ? 0 : 66 * matchingDirectSkillsV1.size() / toMatchJV.getDirectV1Skills().size();
    scoreV1 += toMatchJV.getAllV1Skills().isEmpty() ? 0 : 34 * matchingIndirectSkillsV1.size() / toMatchJV.getAllV1Skills().size();

//    int scoreV0 = toMatchJV.getDirectSkills().isEmpty() ? 0 : 66 * matchingDirectSkillsV0.size() / toMatchJV.getDirectSkills().size();
//    scoreV0 += toMatchJV.getAllSkills().isEmpty() ? 0 : 34 * matchingIndirectSkillsV0.size() / toMatchJV.getAllSkills().size();

    matchingObject.setScoreV1(scoreV1);
//    matchingObject.setScoreV0(scoreV0);
  }

}
