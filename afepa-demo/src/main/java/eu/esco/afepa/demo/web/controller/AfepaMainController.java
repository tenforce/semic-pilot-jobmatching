package eu.esco.afepa.demo.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AfepaMainController {

  @RequestMapping("/")
  public String afepaMainPage(Model model) {
    String serviceApiUrl = System.getProperty("serviceApiUrl", "/esco-service-api/");
    if(!serviceApiUrl.endsWith("/")) serviceApiUrl = serviceApiUrl + "/";
    model.addAttribute("serviceApiUrl", serviceApiUrl);
    model.addAttribute("authorizationHeaderValue", null);
    return "afepa/root";
  }
}
