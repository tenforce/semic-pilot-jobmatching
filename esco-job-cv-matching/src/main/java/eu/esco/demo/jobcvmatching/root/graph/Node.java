package eu.esco.demo.jobcvmatching.root.graph;

public class Node implements AsJson {

  private final Type type;
  private final String label;
  private final String id;
  private final boolean match;

  public Node(Type type, String id, String label) {
    this(type, id, label, false);
  }

  public Node(Type type, String id, String label, boolean match) {
    this.type = type;
    this.id = id;
    this.label = label;
    this.match = match;
  }

  public Type getType() {
    return type;
  }

  public String getId() {
    return GraphHelper.idizeUri(id);
  }

  public boolean isMatch() {
    return match;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Node node = (Node) o;
    return getId().equals(node.getId());
  }

  @Override
  public int hashCode() {
    return getId().hashCode();
  }

  @Override
  public String asJson() {
    String match = isMatch() ? ", \"match\" : true" : "";
    return "{ \"data\" : { \"type\": \"" + type + "\", \"id\" : \"" + getId() + "\", \"label\" : \"" + label + "\" " + match + "} }";
  }
}

