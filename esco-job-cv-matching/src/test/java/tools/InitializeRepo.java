package tools;

import eu.esco.demo.jobcvmatching.root.HardcodedConfiguration;
import eu.esco.demo.jobcvmatching.root.service.RemoteSesameDatasource;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;

public class InitializeRepo {
  public static void main(String[] args) throws Exception {
    String sourceRdfServerUrl = "http://tfvirt-esco-01-rdf:7001/openrdf-sesame";
    String rdfUser = "escordf";
    String rdfPassword = "secret";
    RemoteSesameDatasource sourceDataSource = new RemoteSesameDatasource(sourceRdfServerUrl, "esco_20150203_141031", rdfUser, rdfPassword);
    RemoteSesameDatasource destinationDataSource = new RemoteSesameDatasource(HardcodedConfiguration.sesameUrl, HardcodedConfiguration.sesameRepository, rdfUser, rdfPassword);


    System.out.println("Loading statements...");
    RepositoryResult<Statement> statements = sourceDataSource.getConnection().getStatements(null, null, null, false);
    System.out.println("Statements loaded...");
    RepositoryConnection connection = destinationDataSource.getConnection();


    int count = 0;
    System.out.println("Going to start adding...");
    while (statements.hasNext()) {
      connection.add(statements.next());
      count++;
      if (count >= 10000) {
        System.out.println("Added bunch of 10000 - committing...");
        connection.commit();
        connection.begin();
        count = 0;
        System.out.println("Commit done - starting next bunch...");
      }
    }
    System.out.println("All done - committing...");
    connection.commit();
    System.out.println("Commit done - close all...");

    sourceDataSource.closeQuietly();
    destinationDataSource.closeQuietly();
  }
}
