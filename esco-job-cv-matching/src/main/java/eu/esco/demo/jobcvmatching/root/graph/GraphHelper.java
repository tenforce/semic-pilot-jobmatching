package eu.esco.demo.jobcvmatching.root.graph;

public class GraphHelper {
  private GraphHelper() {
  }

  public static String idizeUri(String uri) {
    return uri.replace(".", "").replace(":", "").replace("/", "");
  }
}
