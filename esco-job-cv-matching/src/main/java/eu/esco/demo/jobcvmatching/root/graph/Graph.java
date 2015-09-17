package eu.esco.demo.jobcvmatching.root.graph;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class Graph implements AsJson {

  private final Set<Node> nodes = new LinkedHashSet<>();
  private final Set<Edge> edges = new LinkedHashSet<>();

  public void addNodes(Node... nodes) {
    Collections.addAll(this.nodes, nodes);
  }

  public void addEdges(Edge... edges) {
    Collections.addAll(this.edges, edges);
  }

  public Collection<Node> getNodes() {
    return nodes;
  }

  public Collection<Edge> getEdges() {
    return edges;
  }

  @Override
  public String asJson() {
    String nodesString = Joiner.on(",").join(asJson(nodes));
    String edgesString = Joiner.on(",").join(asJson(edges));

    return "{ \"nodes\" : [ " + nodesString + " ], \"edges\" : [ " + edgesString + "] }";
  }

  private Iterable<String> asJson(Collection<? extends AsJson> asJsonList) {
    return Iterables.transform(asJsonList, new Function<AsJson, String>() {
      @Nullable
      @Override
      public String apply(AsJson input) {
        return input.asJson();
      }
    });
  }
}

