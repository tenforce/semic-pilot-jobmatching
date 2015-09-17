package eu.esco.demo.jobcvmatching.web.controller;

import com.google.common.collect.Sets;
import eu.esco.demo.jobcvmatching.root.graph.Edge;
import eu.esco.demo.jobcvmatching.root.graph.Graph;
import eu.esco.demo.jobcvmatching.root.graph.GraphHelper;
import eu.esco.demo.jobcvmatching.root.graph.Node;
import eu.esco.demo.jobcvmatching.root.graph.Type;
import eu.esco.demo.jobcvmatching.root.model.CV;
import eu.esco.demo.jobcvmatching.root.model.Job;
import eu.esco.demo.jobcvmatching.root.model.Occupation;
import eu.esco.demo.jobcvmatching.root.model.Skill;
import eu.esco.demo.jobcvmatching.root.service.DataService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class JobCvMatchingController {


  @Inject
  private DataService dataService;

  @RequestMapping("/graph")
  public String getGraph(org.springframework.ui.Model model, HttpServletRequest request, @RequestParam("cvUri") String cvUri, @RequestParam("jvUri") String jvUri) {
    CV cv = dataService.getCV(cvUri, request.getLocale().getLanguage());
    Job jv = dataService.getJV(jvUri, request.getLocale().getLanguage());

    model.addAttribute("json", buildGraphJson(jv, cv));
    model.addAttribute("jvUri", jv.getUri());
    model.addAttribute("rootUri", "#" + GraphHelper.idizeUri(jv.getUri()));
    return "jv_cv_graph";
  }

  private String buildGraphJson(Job job, CV cv) {
    Graph graph = new Graph();

    Node jobNode = new Node(Type.Job, job.getUri(), job.getLabel());
    graph.addNodes(jobNode);

    Node cvNode = new Node(Type.CV, cv.getUri(), cv.getDescription());
    graph.addNodes(cvNode);

    for (Occupation occupation : job.getOccupations()) {
      Node occupationNode = new Node(Type.Occupation, occupation.getUri(), occupation.getLabel());

      graph.addNodes(occupationNode);
      graph.addEdges(new Edge(jobNode, occupationNode));

      Map<String, Skill> occupationSkillMap = occupation.getSkillMap();
      Map<String, Skill> cvSkillMap = cv.getSkillMap();

      Sets.SetView<String> matches = Sets.intersection(occupationSkillMap.keySet(), cvSkillMap.keySet());

      Map<String, Skill> fullSkillMap = new HashMap<>();
      fullSkillMap.putAll(occupationSkillMap);
      fullSkillMap.putAll(cvSkillMap);

      for (Map.Entry<String, Skill> entry : fullSkillMap.entrySet()) {
        String uri = entry.getKey();
        boolean isMatch = matches.contains(uri);

        Node skillNode = new Node(Type.Skill, entry.getValue().getUri(), entry.getValue().getLabel(), isMatch);
        graph.addNodes(skillNode);

        if (occupationSkillMap.containsKey(uri)) {
          graph.addEdges(new Edge(occupationNode, skillNode));
        }
        if (cvSkillMap.containsKey(uri)) {
          graph.addEdges(new Edge(skillNode, cvNode));
        }
      }
    }
    return graph.asJson();
  }

//  private String buildGraphJson(Job job, CV cv) {
//    Graph graph = new Graph();
//
//    Node jobNode = new Node(Type.Job, "" + job.getDocumentId(), job.getLabel());
//    Node occupationNode = new Node(Type.Occupation, job.getOccupation(), job.getOccupation());
//
//    graph.addNodes(jobNode, occupationNode);
//    graph.addEdges(new Edge(jobNode, occupationNode));
//
//    Node cvNode = new Node(Type.CV, "" + cv.getDocumentId(), cv.getDescription());
//    graph.addNodes(cvNode);
//
//
//    Map<String, Skill> jobSkillMap = job.getSkillMap();
//    Map<String, Skill> cvSkillMap = cv.getSkillMap();
//
//    Sets.SetView<String> matches = Sets.intersection(jobSkillMap.keySet(), cvSkillMap.keySet());
//
//    Map<String, Skill> fullSkillMap = new HashMap<>();
//    fullSkillMap.putAll(jobSkillMap);
//    fullSkillMap.putAll(cvSkillMap);
//
//
//    for (Map.Entry<String, Skill> entry : fullSkillMap.entrySet()) {
//      String uri = entry.getKey();
//      boolean isMatch = matches.contains(uri);
//
//      Node skillNode = new Node(Type.Skill, entry.getValue().getUri(), entry.getValue().getDescription(), isMatch);
//      graph.addNodes(skillNode);
//
//      if (jobSkillMap.containsKey(uri)) {
//        graph.addEdges(new Edge(occupationNode, skillNode));
//      }
//      if (cvSkillMap.containsKey(uri)) {
//        graph.addEdges(new Edge(skillNode, cvNode));
//      }
//    }
//    return graph.asJson();
//  }


}
