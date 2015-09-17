package eu.esco.semic.pilot.model;

import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CV extends MatchingObject {

  private final String uri;
  private String givenName;
  private final String description;
  private final boolean hasDocument;
  private Set<Skill> skills = new LinkedHashSet<>();
  private Set<Occupation> occupations = new LinkedHashSet<>();
  private Set<Occupation> occupationsV1 = new LinkedHashSet<>();
  private Set<Skill> skillsV1 = new LinkedHashSet<>();


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

  public void addSkillsV1(List<Skill> skills) {
    this.skillsV1.addAll(skills);
  }

  public void addOccupationsV0(List<Occupation> occupations) {
    this.occupations.addAll(occupations);
  }

  public void addOccupationsV1(List<Occupation> occupations) {
    this.occupationsV1.addAll(occupations);
  }

  public void addSkillsV0(List<Skill> skills) {
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
