package tools;

import eu.esco.demo.jobcvmatching.root.HardcodedConfiguration;
import eu.esco.demo.jobcvmatching.root.SemicRdfModel;
import eu.esco.demo.jobcvmatching.root.service.RemoteSesameDatasource;
import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;

public class AddV1OccBasedOnV0Occ {
  public static void main(String[] args) throws Exception {
    String query = "select ?cv ?newOcc where {\n" +
                   "  ?cv <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://data.europa.eu/esco/semic/model#CV> .\n" +
                   "  ?cv <http://data.europa.eu/esco/semic/model#hasOccupation> ?occ .\n" +
                   "  ?occ <http://data.europa.eu/esco/model#replacedBy> ?newOcc .\n" +
                   "}";

    String rdfUser = "escordf";
    String rdfPassword = "secret";
    RemoteSesameDatasource destinationDataSource = new RemoteSesameDatasource(HardcodedConfiguration.sesameUrl, HardcodedConfiguration.sesameRepository, rdfUser, rdfPassword);
    RepositoryConnection queryConnection = destinationDataSource.getConnection();
    RepositoryConnection storeConnection = destinationDataSource.getConnection();
    TupleQuery tupleQuery = queryConnection.prepareTupleQuery(QueryLanguage.SPARQL, query);

    URI hasOccupation = storeConnection.getValueFactory().createURI(SemicRdfModel.propertyHasOccupation_v1);

    TupleQueryResult evaluate = tupleQuery.evaluate();
    while(evaluate.hasNext()) {
      BindingSet next = evaluate.next();
      URI cv = (URI) next.getValue("cv");
      URI occ = (URI) next.getValue("newOcc");

      storeConnection.add(cv, hasOccupation, occ);
    }
    storeConnection.commit();
    queryConnection.close();
    storeConnection.close();

  }
}
