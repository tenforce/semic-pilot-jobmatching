package eu.esco.demo.jobcvmatching.root.graph;

public class Edge implements AsJson {

  private final Node source;
  private final Node target;

  public Edge(Node source, Node target) {
    this.source = source;
    this.target = target;
  }

  public Type getType() {
    // TODO : we need a builder for JobCv graphs if this matures
    return target.getType() == Type.CV ? Type.CV : source.getType();
  }

  public Node getSource() {
    return source;
  }

  public Node getTarget() {
    return target;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Edge edge = (Edge) o;
    return source.equals(edge.source) && target.equals(edge.target);
  }

  @Override
  public int hashCode() {
    int result = source.hashCode();
    result = 31 * result + target.hashCode();
    return result;
  }

  @Override
  public String asJson() {
    return "{ \"data\": { \"type\": \"" + getType() + "\", \"source\": \"" + source.getId() + "\",  \"target\": \"" + target.getId() + "\" } }";
  }
}

