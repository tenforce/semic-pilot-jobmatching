package eu.esco.demo.jobcvmatching.root;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class HardcodedConfiguration {

  public static final String sesameUrl = "http://tfvirt-esco-02-rdf:7001/openrdf-sesame";
  public static final String sesameRepository = "semic2";
  public static final String serviceApiUrl = "http://tfvirt-semtech-dsk01:8080/esco-service-api/";
  public static final String authorizationHeaderValue = null;
  //public static final String  serviceApiUrl = "https://esco-portal-02.tenforce.com/esco/api/";
//  public static final String  authorizationHeaderValue = "Basic ZG9taTpkZA==";

  public static final String defaultLanguage = "en";
  public static final Map<String, String> languages = Collections.synchronizedMap(new LinkedHashMap<String, String>());

  static {
    for (String lang : new String[]{"bg", "cs", "da", "de", "et", "el", "en", "es", "fr", "is", "it", "lv", "lt", "hr", "hu", "mt", "nl", "pl", "pt", "ro", "sk", "sl", "fi", "sv"}) {
      Locale locale = new Locale(lang);
      languages.put(lang, locale.getDisplayName(locale));
//      languages.put(lang, new LangInfo(lang));
    }
//    defaultLanguage = languages.get("en");
  }

//  public static class LangInfo {
//    private final String code;
//    private final String name;
//
//    public LangInfo(String code) {
//      this.code = code;
//      Locale locale = new Locale(code);
//      this.name = locale.getDisplayName(locale);
//    }
//
//    public String getCode() {
//      return code;
//    }
//
//    public String getName() {
//      return name;
//    }
//  }

}
