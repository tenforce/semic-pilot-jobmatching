package eu.esco.semic.pilot;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class HardcodedConfiguration {

  public static final String defaultLanguage = "en";
  public static final Map<String, String> languages = Collections.synchronizedMap(new LinkedHashMap<String, String>());

  static {
    for (String lang : new String[]{"bg", "cs", "da", "de", "et", "el", "en", "es", "fr", "is", "it", "lv", "lt", "hr", "hu", "mt", "nl", "pl", "pt", "ro", "sk", "sl", "fi", "sv"}) {
      Locale locale = new Locale(lang);
      languages.put(lang, locale.getDisplayName(locale));
    }
  }


}
