package eu.esco.semic.pilot.model;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Job extends MatchingObject {

  private final String uri;
  private final String label;
  private String description;
  private String hiringOrganizationName;
  private String geonameLabel;
  private String geonameUri;
  private String occupationalCategory;
  private String employmentType;
  private String experienceLevel;
  private final Set<Occupation> occupations = new LinkedHashSet<>();
  private final Set<Occupation> occupationsV1 = new LinkedHashSet<>();
  private final Set<Skill> directSkills = new LinkedHashSet<>();
  private final Set<Skill> directV1Skills = new LinkedHashSet<>();
  private final Set<Skill> allSkills = new LinkedHashSet<>();
  private final Set<Skill> allV1Skills = new LinkedHashSet<>();
  private final Set<Nace> naces = new LinkedHashSet<>();

  public Job(String uri, String label) {
    this.uri = uri;
    this.label = label;
  }

  public String getId() {
    return StringUtils.substringAfterLast(uri, "/");
  }

  public String getUri() {
    return uri;
  }

  public String getLabel() {
    return label;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getOccupationalCategory() {
    return occupationalCategory;
  }

  public void setOccupationalCategory(String occupationalCategory) {
    this.occupationalCategory = occupationalCategory;
  }

  public String getGeonameUri() {
    return geonameUri;
  }

  public void setGeonameUri(String geonameUri) {
    this.geonameUri = geonameUri;
  }

  public String getGeonameLabel() {
    return geonameLabel;
  }

  public void setGeonameLabel(String geonameLabel) {
    this.geonameLabel = geonameLabel;
  }

  public String getEmploymentType() {
    return employmentType;
  }

  public void setEmploymentType(String employmentType) {
    this.employmentType = employmentType;
  }

  public String getExperienceLevel() {
    return experienceLevel;
  }

  public void setExperienceLevel(String experienceLevel) {
    this.experienceLevel = experienceLevel;
  }

  public String getHiringOrganizationName() {
    return hiringOrganizationName;
  }

  public void setHiringOrganizationName(String hiringOrganizationName) {
    this.hiringOrganizationName = hiringOrganizationName;
  }

  public Set<Skill> getDirectSkills() {
    return directSkills;
  }

  public Set<Skill> getDirectV1Skills() {
    return directV1Skills;
  }

  public Set<Occupation> getOccupations() {
    return occupations;
  }

  public Set<Occupation> getOccupationsV1() {
    return occupationsV1;
  }


  public Set<Skill> getAllSkills() {
    return allSkills;
  }

  public Set<Skill> getAllV1Skills() {
    return allV1Skills;
  }

  public Set<Nace> getNaces() {
    return naces;
  }

  public void addDirectSkills(List<Skill> skills) {
    this.directSkills.addAll(skills);
  }

  public void addDirectV1Skills(List<Skill> skills) {
    this.directV1Skills.addAll(skills);
  }

  public void addOccupations(List<Occupation> occupations) {
    this.occupations.addAll(occupations);
    for (Occupation occupation : occupations) {
      allSkills.addAll(occupation.getSkills());
    }
  }


  public void addOccupationsV1(List<Occupation> occupations) {
    this.occupationsV1.addAll(occupations);
    for (Occupation occupation : occupations) {
      allV1Skills.addAll(occupation.getSkills());
    }
  }

  public void addNaces(List<Nace> naces) {
    this.naces.addAll(naces);
  }


}
