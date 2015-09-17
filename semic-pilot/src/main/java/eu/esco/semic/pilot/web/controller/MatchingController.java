package eu.esco.semic.pilot.web.controller;


import eu.esco.semic.pilot.model.CV;
import eu.esco.semic.pilot.model.Job;
import eu.esco.semic.pilot.model.MatchingObject;
import eu.esco.semic.pilot.service.CachedDataService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Controller
public class MatchingController {
  @Inject
  private CachedDataService dataService;

  @RequestMapping(value = {"/firstscreen", "/"})
  public String firstscreen(Model model) {
    model.addAttribute("jvs", dataService.getJVsUriWithLabel());
    Map<String, String> cvs = dataService.getCVsUriWithLabel();
    for (Map.Entry<String, String> entry : cvs.entrySet()) {
      if (entry.getKey().contains("vdab")) entry.setValue(entry.getValue() + " - VDAB");
      else if (entry.getKey().contains("eures")) entry.setValue(entry.getValue() + " - EURES");
    }
    model.addAttribute("cvs", cvs);
    return "firstscreen";
  }

  @RequestMapping("/statistics")
  public String statistics() {
    return "statistics";
  }

  @RequestMapping("/matching")
  public String matching(Model model, HttpServletRequest request,
                         @RequestParam("type") String type,
                         @RequestParam("uri") String uri,
                         @RequestParam(value = "matchingType", required = false) String matchingType) {

    MatchingObjectComparator comparator = new MatchingObjectComparator();
    if ("cv".equalsIgnoreCase(type)) {
      type = "cv";
      CV cv = dataService.getCV(uri, request.getLocale().getLanguage());
      List<Job> jvs = dataService.getJVs(request.getLocale().getLanguage());

      if ("implicit".equalsIgnoreCase(matchingType)) {
        matchingType = "implicit";
        dataService.updateCvSkillFieldsImplicit(cv, jvs);
      }
      else if ("combined".equalsIgnoreCase(matchingType)) {
        matchingType = "combined";
        dataService.updateCvSkillFieldsCombined(cv, jvs);
      }
      else {
        matchingType = "explicit";
        dataService.updateCvSkillFieldsExplicit(cv, jvs);
      }
      Collections.sort(jvs, comparator);
      fixScores(jvs);

      while (jvs.size() > 10) jvs.remove(10);

      model.addAttribute("jvs", jvs);
      model.addAttribute("cv", cv);
    }
    else {
      type = "jv";
      Job jv = dataService.getJV(uri, request.getLocale().getLanguage());
      List<CV> cvs = dataService.getCVs(request.getLocale().getLanguage());
      if ("implicit".equalsIgnoreCase(matchingType)) {
        matchingType = "implicit";
        dataService.updateCvSkillFieldsImplicit(cvs, jv);
      }
      else if ("combined".equalsIgnoreCase(matchingType)) {
        matchingType = "combined";
        dataService.updateCvSkillFieldsCombined(cvs, jv);
      }
      else {
        matchingType = "explicit";
        dataService.updateCvSkillFieldsExplicit(cvs, jv);
      }
      Collections.sort(cvs, comparator);
      fixScores(cvs);

      while (cvs.size() > 10) cvs.remove(10);

      model.addAttribute("jv", jv);
      model.addAttribute("cvs", cvs);
    }

    model.addAttribute("matchingType", matchingType);
    model.addAttribute("uri", uri);
    model.addAttribute("type", type);
    model.addAttribute("language", request.getLocale().getLanguage());
    return "matching";
  }

  private static void fixScores(List<? extends MatchingObject> matchingObjects) {
    for (int i = 0; i < matchingObjects.size(); i++) {
      MatchingObject matchingObject = matchingObjects.get(i);
      if (matchingObject.getScoreV1() != 100) break;
      matchingObject.setScoreV1(100 - i);
    }

    for (int i = matchingObjects.size() - 2; i >= 0; i--) {
      MatchingObject matchingObject = matchingObjects.get(i);
      if (matchingObject.getScoreV1() > matchingObjects.get(i + 1).getScoreV1()) continue;
      matchingObject.setScoreV1(matchingObjects.get(i + 1).getScoreV1() + 1);
    }

  }

  private static class MatchingObjectComparator implements Comparator<MatchingObject> {
    @Override
    public int compare(MatchingObject o1, MatchingObject o2) {
      int result = ((Integer) o2.getScoreV1()).compareTo(o1.getScoreV1());
      if (0 != result) return result;

      return ((Integer) o1.getAdditionalSkillsV1().size()).compareTo(o2.getAdditionalSkillsV1().size());

    }
  }
}

