package eu.esco.demo.jobcvmatching.root;

import javax.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Qualifier
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SparqlQuery {
  Value value();

  public enum Value {
    cvs, jvs, jv, getSemicConcepts, jvLabelOnly, matchingCvs, allJvForCache, allCvForCache
  }
}
