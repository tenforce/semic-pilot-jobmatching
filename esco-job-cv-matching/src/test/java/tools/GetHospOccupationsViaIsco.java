package tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenforce.core.net.HttpClientFactory;
import org.apache.http.entity.ContentType;

import java.util.List;
import java.util.Map;

public class GetHospOccupationsViaIsco {
  private static final int[] iscoCodes = {1221, 1222, 1411, 1412, 1431, 1439, 2413, 2431, 2611, 3332, 3339, 3434, 4221, 4224, 4229, 5111, 5113, 5120, 5131, 5132, 5151, 5153, 5169, 5230, 9112, 9121, 9411, 9412, 9621, 9622, 9629};

  public static void main(String[] args) throws Exception {

    HttpClientFactory httpClientFactory = new HttpClientFactory(null);

    System.out.println("[");
    boolean firstDone = false;
    for (int iscoCode : iscoCodes) {
      if(firstDone) System.out.println(",");
      firstDone=true;
      System.out.print("{\"iscoCode\": " + iscoCode + ",");
      String requestBody = "{\n" +
                           "  \"language\": \"en\",\n" +
                           "  \"view\": \"detail\",\n" +
                           "  \"offset\": 0,\n" +
                           "  \"notation\": [\n" +
                           "    {\n" +
                           "      \"value\": \"" + iscoCode + "\",\n" +
                           "      \"datatype\": \"http://data.europa.eu/esco/Notation/ISCO08\"\n" +
                           "    }\n" +
                           "  ]\n" +
                           "}";
      String body = httpClientFactory.executeStringPost("http://tfvirt-semtech-dsk01:8080/esco-service-api/relatedConcepts", requestBody, ContentType.APPLICATION_JSON).getRight();

      List concepts = (List) new ObjectMapper().readValue(body, Map.class).get("concepts");
      if(concepts.size() != 1) throw new Exception("size not 1 for " + iscoCode);
      Map concept = (Map)( ((Map) concepts.get(0)).get("concept"));
      List exactMatches = (List) concept.get("exactMatch");
      if(exactMatches.size() != 1) throw new Exception("exactMatches not 1 for " + iscoCode);

      String uri = (String) ((Map)exactMatches.get(0)).get("uri");
      System.out.print("\"uri\": \"" + uri + "\"}");

    }
    System.out.println();
    System.out.println("]");

  }
}
