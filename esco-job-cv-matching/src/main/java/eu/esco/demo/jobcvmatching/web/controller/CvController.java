package eu.esco.demo.jobcvmatching.web.controller;

import eu.esco.demo.jobcvmatching.root.model.CV;
import eu.esco.demo.jobcvmatching.root.service.DataService;
import eu.esco.demo.jobcvmatching.root.service.DocumentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

@Controller
public class CvController {
  @Inject
  private DataService dataService;
  @Inject
  private DocumentService documentService;

  @RequestMapping("/cvs")
  public String getCVs(Model model, HttpServletRequest request) {
    model.addAttribute("cvs", dataService.getCVs(request.getLocale().getLanguage()));
    return "cvs";
  }

  @RequestMapping("/cv")
  public String getCV(Model model, HttpServletRequest request, @RequestParam("uri") String uri) {
    CV cv = dataService.getCV(uri, request.getLocale().getLanguage());
    if(null == cv) {
      model.addAttribute("errorMessage", "CV with uri " + uri + " not found.");
      return "error";
    }

    model.addAttribute("cv", cv);
    return "cv";
  }

  @RequestMapping(value = "/editCV/addSkillAjax", method = RequestMethod.POST)
  @ResponseBody
  public String editCVaddSkillAjax(@RequestParam("cvUri") String cvUri, @RequestParam("skillUri") String skillUri) throws Exception {
    documentService.addSkill(cvUri, skillUri);
    return "OK";
  }

}
