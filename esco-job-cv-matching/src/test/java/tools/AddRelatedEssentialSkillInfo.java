package tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenforce.core.net.EncodeHelper;
import com.tenforce.core.net.HttpClientFactory;
import eu.esco.demo.jobcvmatching.root.HardcodedConfiguration;
import eu.esco.demo.jobcvmatching.root.service.RemoteSesameDatasource;
import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;

import java.util.List;
import java.util.Map;

public class AddRelatedEssentialSkillInfo {


  public static void main(String[] args) throws Exception {

    String query = "select distinct ?occ where {\n" +
                   "  ?p <http://data.europa.eu/esco/semic/model#hasOccupation_v1> ?occ .\n" +
                   "}";
    String rdfUser = "escordf";
    String rdfPassword = "secret";
    RemoteSesameDatasource destinationDataSource = new RemoteSesameDatasource(HardcodedConfiguration.sesameUrl, HardcodedConfiguration.sesameRepository, rdfUser, rdfPassword);
    RepositoryConnection queryConnection = destinationDataSource.getConnection();
    RepositoryConnection storeConnection = destinationDataSource.getConnection();
    TupleQuery tupleQuery = queryConnection.prepareTupleQuery(QueryLanguage.SPARQL, query);

    HttpClientFactory httpClientFactory = new HttpClientFactory(null);

    URI relatedEssentialSkill = storeConnection.getValueFactory().createURI("http://data.europa.eu/esco/model#relatedEssentialSkill");
    URI graph = storeConnection.getValueFactory().createURI("http://data.europa.eu/esco");


    TupleQueryResult evaluate = tupleQuery.evaluate();
    while (evaluate.hasNext()) {
      BindingSet next = evaluate.next();
      URI occ = (URI) next.getValue("occ");


      String body = httpClientFactory.executeStringGet("http://tfvirt-semtech-dsk01:8080/esco-service-api/concept;concept=" + EncodeHelper.encode(occ.stringValue())).getRight();
      List relationShips = (List)((Map) new ObjectMapper().readValue(body, Map.class).get("concept")).get("hasRelationship");
      for (Object relationShip : relationShips) {
        String relationShipType = (String) ((Map) ((Map) relationShip).get("hasRelationshipType")).get("uri");
        if(!relationShipType.equals("http://data.europa.eu/esco/RelationshipType#iC.EssentialSkill")) continue;

        String skillUri = (String) ((Map) ((Map) relationShip).get("refersConcept")).get("uri");

        storeConnection.add(occ, relatedEssentialSkill, storeConnection.getValueFactory().createURI(skillUri), graph);
      }
    }
    System.out.println("DONE 0 ");
    storeConnection.commit();
    System.out.println("DONE 1");
    queryConnection.close();
    System.out.println("DONE 2");
    storeConnection.close();
    System.out.println("DONE 3");
  }
}
