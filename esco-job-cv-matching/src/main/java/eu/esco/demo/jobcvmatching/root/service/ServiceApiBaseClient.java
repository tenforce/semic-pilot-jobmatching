package eu.esco.demo.jobcvmatching.root.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenforce.core.net.HttpClientFactory;
import com.tenforce.exception.TenforceException;
import com.tenforce.helper.StringHelper;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.StatusLine;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ServiceApiBaseClient {
  private static final Logger log = LoggerFactory.getLogger(ServiceApiBaseClient.class);

  private HttpClientFactory httpClientFactory;
  private final String serviceUrl;

  public ServiceApiBaseClient() {
    this.serviceUrl = "http://tfvirt-semtech-dsk01:8080/esco-service-api/";
  }

  @PreDestroy
  private void close() {
    httpClientFactory.close();
  }

  @PostConstruct
  private void init() {
    httpClientFactory = new HttpClientFactory(null);
  }

  public Map<String, Object> getConcepts(Collection<String> concepts, String language) {
    if(concepts.isEmpty()) return Collections.emptyMap();

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("language", language);
    requestBody.put("view", "detail");
    requestBody.put("size", Integer.MAX_VALUE);
    requestBody.put("concepts", concepts);
    return doPostWithBody("concepts", requestBody);
  }

  public Map<String, Object> getOccupationsBySubjectTransitive(List<String> subjectTransitiveConcepts, String language) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("language", language);
    requestBody.put("view", "detail");
    requestBody.put("size", Integer.MAX_VALUE);
    requestBody.put("subjectTransitive", subjectTransitiveConcepts);
    requestBody.put("inScheme", Collections.singletonList("http://data.europa.eu/esco/ConceptScheme/ESCO_Occupations"));
    return doPostWithBody("relatedConcepts", requestBody);
  }

  private Map<String, Object> doPostWithBody(String path, Map<String, Object> requestBody, Object... params) {
    String url = serviceUrl + StringHelper.format(path, params);
    try {
      String requestBodyJson = new ObjectMapper().writer().writeValueAsString(requestBody);
      Pair<StatusLine, String> result = httpClientFactory.executeStringPost(url, requestBodyJson, ContentType.APPLICATION_JSON);
      TenforceException.when(!HttpClientFactory.isSuccess(result.getLeft()), "Request '{}' not 2xx result: {} - {}", url, result.getLeft().getStatusCode(), result.getLeft().getReasonPhrase());
      return (Map<String, Object>) new ObjectMapper().readValue(result.getRight(), Map.class);
    }
    catch (IOException e) {
      throw new TenforceException(e);
    }
  }

  @Nullable
  private Map<String, Object> doGet(String path, Object... params) {
    String url = serviceUrl + StringHelper.format(path, params);
    try {
      long start = System.currentTimeMillis();
      Pair<StatusLine, String> result = httpClientFactory.executeStringGet(url);
      long end = System.currentTimeMillis();
      log.info("Query '{}' took {} ms", url, (end - start));
      if (result.getLeft().getStatusCode() == 404) {
        log.info("Call '{}' returned 404", url);
        return null;
      }
      TenforceException.when(!HttpClientFactory.isSuccess(result.getLeft()), "Request '{}' not 2xx result: {} - {}", url, result.getLeft().getStatusCode(), result.getLeft().getReasonPhrase());
      ObjectMapper objectMapper = new ObjectMapper();
      start = System.currentTimeMillis();
      Map map = objectMapper.readValue(result.getRight(), Map.class);
      end = System.currentTimeMillis();
      log.info("Parsing of result '{}' took {} ms", url, (end - start));
      return (Map<String, Object>) map;
    }
    catch (IOException e) {
      throw new TenforceException(e);
    }
  }

}
