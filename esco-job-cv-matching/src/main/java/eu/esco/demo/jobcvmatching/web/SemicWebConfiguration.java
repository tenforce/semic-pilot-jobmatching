package eu.esco.demo.jobcvmatching.web;

import eu.esco.demo.jobcvmatching.web.filter.LanguageFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
public class SemicWebConfiguration {
  @Bean
  public LanguageFilter languageFilter() {
    return new LanguageFilter();
  }
}
