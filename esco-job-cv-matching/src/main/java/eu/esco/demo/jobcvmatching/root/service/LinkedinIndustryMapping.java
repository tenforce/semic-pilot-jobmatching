package eu.esco.demo.jobcvmatching.root.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenforce.core.config.ExtConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Service
public class LinkedinIndustryMapping {
  @Inject
  private ExtConfigService extConfigService;

  private Map<String, Collection<String>> values;
  private Collection<String> keysWithComma;

  @PostConstruct
  private void init() throws Exception {
    File configFile = extConfigService.findMandatoryFile("linkedinIndustriesToNace2Level2.json");
    ObjectMapper objectMapper = new ObjectMapper();
    values = objectMapper.readValue(configFile, Map.class);

    keysWithComma = new ArrayList<>();
    for (String key : values.keySet()) {
      if (key.contains(",")) keysWithComma.add(key);
    }
  }

  public LinkedinIndustry2NaceUris getNaceUris(String linkedinIndustryLabel) {
    linkedinIndustryLabel = linkedinIndustryLabel.toLowerCase();
    Collection<String> uris = new ArrayList<>();
    for (String keyWithComma : keysWithComma) {
      if (!linkedinIndustryLabel.contains(keyWithComma)) continue;
      linkedinIndustryLabel = StringUtils.remove(linkedinIndustryLabel, keyWithComma);
      uris.addAll(values.get(keyWithComma));
    }

    Collection<String> notFoundLabels = new ArrayList<>();
    String[] linkedinIndustryLabels = StringUtils.split(linkedinIndustryLabel, ',');
    for (String oneLinkedinIndustryLabel : linkedinIndustryLabels) {
      if(StringUtils.isBlank(oneLinkedinIndustryLabel)) continue;
      Collection<String> thisLabelUris = values.get(oneLinkedinIndustryLabel.trim());
      if(null == thisLabelUris) {
        notFoundLabels.add(oneLinkedinIndustryLabel);
      }
      else {
        uris.addAll(thisLabelUris);
      }
    }
    return new LinkedinIndustry2NaceUris(uris, notFoundLabels);
  }

  public static class LinkedinIndustry2NaceUris {
    private final Collection<String> naceUris;
    private final Collection<String> notFoundLabels;

    private LinkedinIndustry2NaceUris(Collection<String> naceUris, Collection<String> notFoundLabels) {
      this.naceUris = naceUris;
      this.notFoundLabels = notFoundLabels;
    }

    public Collection<String> getNaceUris() {
      return naceUris;
    }

    public Collection<String> getNotFoundLabels() {
      return notFoundLabels;
    }
  }
}
