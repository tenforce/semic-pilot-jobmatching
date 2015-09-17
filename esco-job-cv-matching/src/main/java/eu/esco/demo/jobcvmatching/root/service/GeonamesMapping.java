package eu.esco.demo.jobcvmatching.root.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenforce.core.config.ExtConfigService;
import com.tenforce.exception.TenforceException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Service
public class GeonamesMapping {
  @Inject
  private ExtConfigService extConfigService;

  private final Map<String, Info> values = new HashMap<>();

  @PostConstruct
  private void init() throws Exception {
    File configFile = extConfigService.findMandatoryFile("geonames2nuts.json");
    ObjectMapper objectMapper = new ObjectMapper();
    Map<?, ?> data = objectMapper.readValue(configFile, Map.class);
    for (Map.Entry<?, ?> entry : data.entrySet()) {
      Map<?, ?> infoMap = (Map<?, ?>) entry.getValue();
      values.put((String) entry.getKey(), new Info((String) infoMap.get("postcode"), (String) infoMap.get("nuts"), (String) infoMap.get("geonamesId")));
    }
  }

  public Info getInfo(String geonameLabel) {
    Info info = values.get(geonameLabel);
    if (null != info) return info;
    if (!geonameLabel.contains(",")) return null;

    for (String oneLabel : StringUtils.split(geonameLabel, ',')) {
      if (StringUtils.isBlank(oneLabel)) continue;
      info = values.get(oneLabel.trim());
      if (null != info) return info;
    }

    return null;
  }

  public static class Info {
    private final String postcode;
    private final String nutsUri;
    private final String geonamesId;

    public Info(String postcode, String nutsUri, String geonamesId) {
      this.postcode = TenforceException.failIfBlank(postcode, "No postcode");
      this.nutsUri = TenforceException.failIfBlank(nutsUri, "No nutsUri");
      this.geonamesId = geonamesId;
    }

    public String getPostcode() {
      return postcode;
    }

    public String getNutsUri() {
      return nutsUri;
    }

    public String getGeonamesId() {
      return geonamesId;
    }
  }
}
