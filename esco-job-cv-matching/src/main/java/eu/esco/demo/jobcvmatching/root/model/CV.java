package eu.esco.demo.jobcvmatching.root.model;

import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CV {
  private static final String[] scoreClasses = {"one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten"};

  private final String uri;
  private String givenName;
  private final String description;
  private final boolean hasDocument;
  private Set<Skill> skills = new LinkedHashSet<>();
  private Set<Occupation> occupations = new LinkedHashSet<>();
  private Set<Occupation> occupationsV1 = new LinkedHashSet<>();
  private Set<Skill> skillsV1 = new LinkedHashSet<>();
  private List<Skill> missingSkills = new ArrayList<>();
  private List<Skill> missingSkillsV1 = new ArrayList<>();
  private List<Skill> matchingSkills = new ArrayList<>();
  private List<Skill> matchingSkillsV1 = new ArrayList<>();
  private List<Skill> additionalSkills = new ArrayList<>();
  private List<Skill> additionalSkillsV1 = new ArrayList<>();
  private int scoreV0;
  private int scoreV1;

  public CV(String uri, String description, boolean hasDocument) {
    this.uri = uri;
    this.description = description;
    this.hasDocument = hasDocument;
  }

  public String getHtmlClass() {
    if(uri.contains("/vdab/")) return "vdab";
    if(uri.contains("/eures/")) return "eures";
    return "";
  }

  public String getGivenName() {
    return givenName;
  }

  public void setGivenName(String givenName) {
    this.givenName = givenName;
  }

  public int getScoreV0() {
    return scoreV0;
  }

  public String getScoreV0Class() {
    return getScoreClass(scoreV0);
  }

  public String getScoreV1Class() {
    return getScoreClass(scoreV1);
  }

  private String getScoreClass(int score) {
    int index = score/10;
    if(index > 9) index = 9;
    return scoreClasses[index];
  }

  public void setScoreV0(int scoreV0) {
    this.scoreV0 = scoreV0;
  }

  public int getScoreV1() {
    return scoreV1;
  }

  public void setScoreV1(int scoreV1) {
    this.scoreV1 = scoreV1;
  }

  public String getUri() {
    return uri;
  }

  public String getDescription() {
    return description;
  }

  public boolean isHasDocument() {
    return hasDocument;
  }

  public Collection<Skill> getSkills() {
    return skills;
  }

  public Map<String, Skill> getSkillMap() {
    Map<String, Skill> result = new HashMap<>();
    for (Skill skill : skills) {
      Assert.isTrue(!result.containsKey(skill.getUri()));
      result.put(skill.getUri(), skill);
    }
    return result;
  }

  public void addSkillV1(Skill skill) {
    this.skillsV1.add(skill);
  }

  public void addOccupation(Occupation occupation) {
    this.occupations.add(occupation);
  }

  public void addOccupationV1(Occupation occupation) {
    this.occupationsV1.add(occupation);
  }

  public void addSkill(Skill skill) {
    Assert.notNull(skills);
    this.skills.add(skill);
  }

  public List<Skill> getMissingSkills() {
    return missingSkills;
  }

  public void addMissingSkills(Collection<Skill> missingSkills) {
    this.missingSkills.addAll(missingSkills);
  }

  public List<Skill> getMissingSkillsV1() {
    return missingSkillsV1;
  }

  public void addMissingSkillsV1(Collection<Skill> missingSkillsV1) {
    this.missingSkillsV1.addAll(missingSkillsV1);
  }

  public List<Skill> getAdditionalSkills() {
    return additionalSkills;
  }

  public void addAdditionalSkills(Collection<Skill> additionalSkills) {
    this.additionalSkills.addAll(additionalSkills);
  }

  public List<Skill> getAdditionalSkillsV1() {
    return additionalSkillsV1;
  }

  public void addAdditionalSkillsV1(Collection<Skill> additionalSkillsV1) {
    this.additionalSkillsV1.addAll(additionalSkillsV1);
  }

  public List<Skill> getMatchingSkills() {
    return matchingSkills;
  }

  public void addMatchingSkills(Collection<Skill> matchingSkills) {
    this.matchingSkills.addAll(matchingSkills);
  }

  public List<Skill> getMatchingSkillsV1() {
    return matchingSkillsV1;
  }

  public void addMatchingSkillsV1(Collection<Skill> matchingSkillsV1) {
    this.matchingSkillsV1.addAll(matchingSkillsV1);
  }

  public void setSkills(List<Skill> skills) {
    Assert.notNull(skills);
    this.skills.addAll(skills);
  }

  public Collection<Occupation> getOccupations() {
    return occupations;
  }

  public Set<Occupation> getOccupationsV1() {
    return occupationsV1;
  }

  public Collection<Skill> getSkillsV1() {
    return skillsV1;
  }
}
