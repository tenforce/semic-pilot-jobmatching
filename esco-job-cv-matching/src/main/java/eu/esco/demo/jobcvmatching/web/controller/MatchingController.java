package eu.esco.demo.jobcvmatching.web.controller;

import eu.esco.demo.jobcvmatching.root.model.CV;
import eu.esco.demo.jobcvmatching.root.model.Job;
import eu.esco.demo.jobcvmatching.root.service.DataService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Controller
public class MatchingController {
  @Inject
  private DataService dataService;

  @RequestMapping("/firstscreen")
  public String firstscreen(Model model) {
    Map<String, String> jVsWithLabel = dataService.getJVsWithLabel();
    model.addAttribute("jvs", jVsWithLabel);
    return "firstscreen";
  }

  @RequestMapping("/matching")
  public String matching(Model model, HttpServletRequest request,
                         @RequestParam("type") String type,
                         @RequestParam("uri") String uri,
                         @RequestParam(value = "version", required = false) String version,
                         @RequestParam(value = "matchingType", required = false) String matchingType) {
    final boolean isV1 = !"v0".equalsIgnoreCase(version);
    Job jv = dataService.getJV(uri, request.getLocale().getLanguage());
    List<CV> cvs = new ArrayList<>(dataService.getMatchingCvs(jv, request.getLocale().getLanguage()));

    if ("explicit".equalsIgnoreCase(matchingType)) {
      matchingType = "explicit";
      dataService.updateCvSkillFieldsExplicit(cvs, jv);
    }
    else if ("combined".equalsIgnoreCase(matchingType)) {
      matchingType = "combined";
      dataService.updateCvSkillFieldsCombined(cvs, jv);
    }
    else {
      matchingType = "implicit";
      dataService.updateCvSkillFieldsImplicit(cvs, jv);
    }
    Collections.sort(cvs, new Comparator<CV>() {
      @Override
      public int compare(CV o1, CV o2) {
        return isV1 ? ((Integer) o2.getScoreV1()).compareTo(o1.getScoreV1())
                    : ((Integer) o2.getScoreV0()).compareTo(o1.getScoreV0());
      }
    });

    while (cvs.size() > 10) cvs.remove(10);
    model.addAttribute("matchingType", matchingType);
    model.addAttribute("type", type);
    model.addAttribute("isV1", isV1);
    model.addAttribute("language", request.getLocale().getLanguage());
    model.addAttribute("jv", jv);
    model.addAttribute("cvs", cvs);
    return "matching";
  }


  private void sortCombined(List<CV> cvs, Job jv) {

  }

}
