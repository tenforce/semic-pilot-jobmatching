package eu.esco.demo.jobcvmatching.root.model;

import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Occupation extends BaseConcept {
  private final Set<Skill> skills = new LinkedHashSet<>();

  public Occupation(String uri, String label) {
    super(uri, label);
  }

  public void addSkill(Skill skill) {
    this.skills.add(skill);
  }

  public Set<Skill> getSkills() {
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

}
