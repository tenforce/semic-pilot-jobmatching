package eu.esco.demo.jobcvmatching.root.model;

public abstract class BaseConcept {

  private final String uri;
  private final String label;

  public BaseConcept(String uri, String label) {
    this.uri = uri;
    this.label = label;
  }

  public String getUri() {
    return uri;
  }

  public String getLabel() {
    return label;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof BaseConcept)) return false;

    BaseConcept that = (BaseConcept) o;
    return uri.equals(that.uri);

  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
           "uri='" + uri + '\'' +
           ", label='" + label + '\'' +
           '}';
  }

  @Override
  public int hashCode() {
    return uri.hashCode();
  }
}
