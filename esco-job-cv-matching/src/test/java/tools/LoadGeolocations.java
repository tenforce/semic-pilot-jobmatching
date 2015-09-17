package tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.tenforce.core.io.FileHelper;
import com.tenforce.exception.TenforceException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoadGeolocations {

  public static void main(String[] args) throws Exception {

    Map<String, String> geonameId = new HashMap<>();
    CSVParser geonameIdParser = CSVParser.parse(new File("C:\\Projects\\ESCO\\semic\\BE-fullGeoName.txt"), Charset.defaultCharset(), CSVFormat.newFormat('\t'));
    for (CSVRecord geonameIdLine : geonameIdParser) {
//      System.out.println(geonameIdLine.get(0) + " -> " + geonameIdLine.get(1));
      geonameId.put(StringUtils.stripAccents(geonameIdLine.get(1).toUpperCase()), geonameIdLine.get(0));
    }
//    if(true)return;

    Map<String, String> postcode2nuts = new HashMap<>();
    List<String> postcodeToNutsLines = FileHelper.readLines(new File("C:\\Projects\\ESCO\\semic\\BE_Locality-Postcode_postcode2nutsLevel3.txt"));
    for (String postcodeToNutsLine : postcodeToNutsLines) {
      if (StringUtils.isBlank(postcodeToNutsLine)) continue;
      postcode2nuts.put(postcodeToNutsLine.substring(12, 16), postcodeToNutsLine.substring(24, 29));
    }


    Map<String, Map<String, String>> geonames2info = new HashMap<>();

    CSVParser geonamesParser = CSVParser.parse(new File("C:\\Projects\\ESCO\\semic\\geonames2postcodes_BE.txt"), Charset.defaultCharset(), CSVFormat.newFormat('\t'));
    for (CSVRecord oneLine : geonamesParser) {
      String postcode = oneLine.get(1);
      String geolocationName = oneLine.get(2);
      String nutsCode = postcode2nuts.get(postcode);

      if (geonames2info.containsKey(geolocationName)) {
        System.out.println("Duplicate name [" + geolocationName + "][" + postcode + "][" + nutsCode + "] mapped to: [" + geonames2info.get(geolocationName) + "] ");
        continue;
      }
      if (StringUtils.isBlank(nutsCode)) {
        System.out.println("No nuts3 code for postcode: [" + postcode + "] [" + geolocationName + "]");
        continue;
      }

      String forIdName = geolocationName;
      if(forIdName.contains("Bruxelles")) forIdName = "Brussels";
      String geonamesId = geonameId.get(forIdName.toUpperCase());
      if(null == geonamesId) geonamesId = geonameId.get(StringUtils.stripAccents(forIdName).toUpperCase());

      if(null == geonamesId && forIdName.contains(" ")) {
        String newForIdName;
        while(null == geonamesId && !(newForIdName = StringUtils.substringBeforeLast(forIdName, " ")).equals(forIdName)) {
          forIdName = newForIdName;
          geonamesId = geonameId.get(forIdName.toUpperCase());
          if(null == geonamesId) geonamesId = geonameId.get(StringUtils.stripAccents(forIdName).toUpperCase());
        }
      }

      if(null == geonamesId) System.out.println("No id found for " + geolocationName);



      Map<String, String> info = new HashMap<>();
      info.put("postcode", postcode);
      if(null != geonamesId) info.put("geonamesId", geonamesId);
      info.put("nuts", "http://data.europa.eu/esco/ConceptScheme/NUTS2008/c." + nutsCode);

      geonames2info.put(geolocationName, info);


    }

    ObjectMapper objectMapper = new ObjectMapper();
    ObjectWriter writer = objectMapper.writer();
    writer = writer.withDefaultPrettyPrinter();
    System.out.println(writer.writeValueAsString(geonames2info));

  }
}
