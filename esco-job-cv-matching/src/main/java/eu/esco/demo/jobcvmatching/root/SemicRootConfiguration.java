package eu.esco.demo.jobcvmatching.root;

import com.tenforce.core.config.ExtConfigService;
import com.tenforce.core.spring.ResourceHelper;
import com.tenforce.sesame.template.SesameDatasource;
import eu.esco.demo.jobcvmatching.root.service.RemoteSesameDatasource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
@ComponentScan
public class SemicRootConfiguration {
  @Bean
  public ExtConfigService extConfigService() {
    return new ExtConfigService("semic");
  }

  @Bean(initMethod = "", destroyMethod = "closeQuietly")
  public SesameDatasource sesameDatasource() {
    String rdfUser = "escordf";
    String rdfPassword = "secret";
    return new RemoteSesameDatasource(HardcodedConfiguration.sesameUrl, HardcodedConfiguration.sesameRepository, rdfUser, rdfPassword);
  }

  @Bean
  @SparqlQuery(SparqlQuery.Value.allCvForCache)
  public String allCvForCacheSparqlQuery() {
    return ResourceHelper.toString(new ClassPathResource("/templates/sparql/allCvForCache.sparql"), "UTF-8");
  }

  @Bean
  @SparqlQuery(SparqlQuery.Value.allJvForCache)
  public String allJvForCacheSparqlQuery() {
    return ResourceHelper.toString(new ClassPathResource("/templates/sparql/allJvForCache.sparql"), "UTF-8");
  }

  @Bean
  @SparqlQuery(SparqlQuery.Value.matchingCvs)
  public String matchingCvsSparqlQuery() {
    return ResourceHelper.toString(new ClassPathResource("/templates/sparql/matchingCvs.sparql"), "UTF-8");
  }

  @Bean
  @SparqlQuery(SparqlQuery.Value.cvs)
  public String cvsSparqlQuery() {
    return ResourceHelper.toString(new ClassPathResource("/templates/sparql/cvs.sparql"), "UTF-8");
  }

  @Bean
  @SparqlQuery(SparqlQuery.Value.jvs)
  public String jvsSparqlQuery() {
    return ResourceHelper.toString(new ClassPathResource("/templates/sparql/jvs.sparql"), "UTF-8");
  }

  @Bean
  @SparqlQuery(SparqlQuery.Value.jvLabelOnly)
  public String jvLabelOnlySparqlQuery() {
    return ResourceHelper.toString(new ClassPathResource("/templates/sparql/jvLabelOnly.sparql"), "UTF-8");
  }

  @Bean
  @SparqlQuery(SparqlQuery.Value.jv)
  public String jvSparqlQuery() {
    return ResourceHelper.toString(new ClassPathResource("/templates/sparql/jv.sparql"), "UTF-8");
  }

  @Bean
  @SparqlQuery(SparqlQuery.Value.getSemicConcepts)
  public String getSemicConceptsSparqlQuery() {
    return ResourceHelper.toString(new ClassPathResource("/templates/sparql/getSemicConcepts.sparql"), "UTF-8");
  }

}
