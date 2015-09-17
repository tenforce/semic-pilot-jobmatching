package eu.esco.semic.pilot.model;

import org.springframework.util.Assert;

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

  public void addSkills(List<Skill> skills) {
    this.skills.addAll(skills);
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
