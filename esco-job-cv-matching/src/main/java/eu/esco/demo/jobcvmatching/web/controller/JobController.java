package eu.esco.demo.jobcvmatching.web.controller;

import eu.esco.demo.jobcvmatching.root.model.CV;
import eu.esco.demo.jobcvmatching.root.model.Job;
import eu.esco.demo.jobcvmatching.root.model.Skill;
import eu.esco.demo.jobcvmatching.root.service.DataService;
import eu.esco.demo.jobcvmatching.root.service.DocumentService;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Controller
public class JobController {
  @Inject
  private DocumentService documentService;
  @Inject
  private DataService dataService;

  @RequestMapping("/jvs")
  public String getJVs(Model model, HttpServletRequest request) {
    model.addAttribute("jvs", dataService.getJVs(request.getLocale().getLanguage()));
    return "jvs";
  }

  @RequestMapping("/jv")
  public String getJV(Model model, HttpServletRequest request, @RequestParam("uri") String uri) {
    Job jv = dataService.getJV(uri, request.getLocale().getLanguage());
    if (null == jv) {
      model.addAttribute("errorMessage", "JV with uri " + uri + " not found.");
      return "error";
    }
    model.addAttribute("jv", jv);
    if (!jv.getDirectSkills().isEmpty()) {
      List<CV> cvs = new ArrayList<>(dataService.getCVs(jv, request.getLocale().getLanguage()));
      dataService.updateCvSkillFieldsImplicit(cvs, jv);
      for (CV cv : cvs) {
        cv.setScoreV1(100 * cv.getSkills().size() / jv.getDirectSkills().size());
      }
      Collections.sort(cvs, new Comparator<CV>() {
        @Override
        public int compare(CV o1, CV o2) {
          return ((Integer) o2.getScoreV1()).compareTo(o1.getScoreV1());
        }
      });
      model.addAttribute("cvs", cvs);
    }
    return "jv";
  }


  @RequestMapping("/editJV")
  public String editJV(Model model, HttpServletRequest request, @RequestParam("uri") String uri) {
    Job jv = dataService.getJV(uri, request.getLocale().getLanguage());
    if (null == jv) {
      model.addAttribute("errorMessage", "JV with uri " + uri + " not found.");
      return "error";
    }

    Pair<Set<Skill>, Set<Skill>> proposedSkillsViaOccupationV0toV1 = dataService.getProposedSkillsViaOccupationV0toV1(jv, request.getLocale().getLanguage());
    model.addAttribute("proposedOccupations", dataService.getNaceProposedOccupations(jv, request.getLocale().getLanguage()));
    model.addAttribute("proposedEssentialSkills", proposedSkillsViaOccupationV0toV1.getLeft());
    model.addAttribute("proposedOptionalSkills", proposedSkillsViaOccupationV0toV1.getRight());
    model.addAttribute("jv", jv);
    return "edit_jv";
  }

  @RequestMapping(value = "/editJV/addOccupation", method = RequestMethod.POST)
  public String editJVaddOccupation(RedirectAttributes redirectAttrs, @RequestParam("jvUri") String jvUri, @RequestParam("occupationUri") String occupationUri) throws Exception {
    documentService.addOccupation(jvUri, occupationUri);
    redirectAttrs.addAttribute("uri", jvUri);
    return "redirect:/editJV";
  }

  @RequestMapping(value = "/editJV/addSkill", method = RequestMethod.POST)
  public String editJVaddSkill(RedirectAttributes redirectAttrs, @RequestParam("jvUri") String jvUri, @RequestParam("skillUri") String skillUri) throws Exception {
    documentService.addSkill(jvUri, skillUri);
    redirectAttrs.addAttribute("uri", jvUri);
    return "redirect:/editJV";
  }

  @RequestMapping(value = "/editJV/addSkillAjax", method = RequestMethod.POST)
  @ResponseBody
  public String editJVaddSkillAjax(@RequestParam("jvUri") String jvUri, @RequestParam("skillUri") String skillUri) throws Exception {
    documentService.addSkill(jvUri, skillUri);
    return "OK";
  }
}
