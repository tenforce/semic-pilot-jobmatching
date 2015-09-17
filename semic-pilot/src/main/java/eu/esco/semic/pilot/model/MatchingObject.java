package eu.esco.semic.pilot.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class MatchingObject {
  private static final String[] scoreClasses = {"one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten"};

  private List<Skill> missingSkills = new ArrayList<>();
  private List<Skill> missingSkillsV1 = new ArrayList<>();
  private List<Skill> matchingSkills = new ArrayList<>();
  private List<Skill> matchingSkillsV1 = new ArrayList<>();
  private List<Skill> additionalSkills = new ArrayList<>();
  private List<Skill> additionalSkillsV1 = new ArrayList<>();
  private int scoreV0;
  private int scoreV1;

  public String getHtmlClass() {
    return "linkedin";
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
    int index = score / 10;
    if (index > 9) index = 9;
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

}
