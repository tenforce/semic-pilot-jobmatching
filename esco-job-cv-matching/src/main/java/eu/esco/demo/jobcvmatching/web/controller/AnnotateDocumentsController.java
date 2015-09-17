package eu.esco.demo.jobcvmatching.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.esco.demo.jobcvmatching.root.model.Occupation;
import eu.esco.demo.jobcvmatching.root.model.Skill;
import eu.esco.demo.jobcvmatching.root.service.DataService;
import eu.esco.demo.jobcvmatching.root.service.DocumentService;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

@Controller
public class AnnotateDocumentsController {
  @Inject
  private DataService dataService;
  @Inject
  private DocumentService documentService;

  @RequestMapping(value = "/changeLanguage")
  @ResponseBody
  public String changeLanguage() {
    return "OK";
  }

  @RequestMapping(value = "/document", method = RequestMethod.POST)
  @ResponseBody
  public String uploadDocument(@RequestBody String body, @RequestParam("uri") String uri) {
    documentService.updateDocument(uri, body);
    return "OK";
  }

  @RequestMapping("/documents")
  public String getDocuments(Model model) {
    List<String> notLoadedDocuments = documentService.findNotLoadedDocuments();
    Collections.sort(notLoadedDocuments);
    model.addAttribute("uris", notLoadedDocuments);
    return "documents";
  }

  @RequestMapping("/document")
  public String getDocument(Model model, @RequestParam("uri") String uri) throws JsonProcessingException {
    model.addAttribute("documentContent", documentService.getDocumentContent(uri));
    model.addAttribute("uri", uri);

    Pair<List<Skill>, List<Occupation>> semicConcepts = dataService.getSemicConcepts();

    ObjectMapper objectMapper = new ObjectMapper();
    model.addAttribute("semicSkills", objectMapper.writeValueAsString(semicConcepts.getLeft()));
    model.addAttribute("semicOccupations", objectMapper.writeValueAsString(semicConcepts.getRight()));
    return "documentToAnnotate";
  }
}
