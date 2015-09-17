package tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenforce.exception.TenforceException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class GetNaceLevel2Code {
  public static void main(String[] args) throws Exception {
    CSVParser parser = CSVParser.parse(new File("C:\\Temp\\esco\\Linkedin Industries-1.csv"), Charset.defaultCharset(), CSVFormat.EXCEL);

    Map<String, String[]> linkedInNameToUris = new HashMap<>();
    for (CSVRecord csvRecord : parser) {
      String[] split = StringUtils.split(csvRecord.get(3));
      if (split.length == 0) throw new Exception("no nace codes for " + csvRecord.get(2));

      String[] uris = new String[split.length];
      for (int i = 0; i < split.length; i++) {
        uris[i] = "http://data.europa.eu/esco/ConceptScheme/NACErev2/c." + split[i].substring(1);
      }
      TenforceException.when(linkedInNameToUris.containsKey(csvRecord.get(2)));
      linkedInNameToUris.put(csvRecord.get(2).toLowerCase(), uris);
    }
    ObjectMapper objectMapper = new ObjectMapper();

    System.out.println(objectMapper.writer().writeValueAsString(linkedInNameToUris));

//    for (Map.Entry<String, String[]> entry : linkedInNameToUris.entrySet()) {
//      System.out.println(entry.getKey());
//      for (String uri : entry.getValue()) {
//        System.out.println("   " + uri);
//      }
//    }


//    "http://tfvirt-semtech-dsk01:8080/esco-service-api/topConcepts;conceptScheme=http%3A%2F%2Fdata.europa.eu%2Fesco%2FConceptScheme%2FNACErev2%2Fcs;view=tree"

//    Pair<StatusLine, String> resultPair = new HttpClientFactory(null).executeStringGet("http://tfvirt-semtech-dsk01:8080/esco-service-api/topConcepts;conceptScheme=http%3A%2F%2Fdata.europa.eu%2Fesco%2FConceptScheme%2FNACErev2%2Fcs;view=tree");
//    ObjectMapper objectMapper = new ObjectMapper();
//    Map map = objectMapper.readValue(resultPair.getRight(),Map.class);
//    List topConcepts = (List) map.get("concepts");
//    for (Object topConcept : topConcepts) {
//      List narrowerConcepts = (List) ((Map) topConcept).get("narrower");
//      for (Object narrowerConceptObject : narrowerConcepts) {
//        Map narrowerConcept = (Map) narrowerConceptObject;
//        String uri = (String) narrowerConcept.get("uri");
//        List notations = (List) narrowerConcept.get("notation");
//        TenforceException.when(notations.size() != 1, uri );
//        String naceCode = (String) ((Map)notations.get(0)).get("value");
//        System.out.println(naceCode + " -> "+ uri);
//        TenforceException.when(!uri.equals("http://data.europa.eu/esco/ConceptScheme/NACErev2/c." + naceCode), uri );
//      }
//    }
  }


}
