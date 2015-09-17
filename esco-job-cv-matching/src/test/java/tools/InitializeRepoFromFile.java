package tools;

import com.tenforce.sesame.SesameUtils;
import eu.esco.demo.jobcvmatching.root.service.RemoteSesameDatasource;
import org.openrdf.model.Statement;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

import java.io.File;
import java.util.List;

public class InitializeRepoFromFile {

  private static final String destinationRdfServerUrl = "http://tfvirt-esco-02-rdf:7001/openrdf-sesame";
//  private static final String destinationRdfServerUrl = "http://localhost:8080/openrdf-sesame";

//  private static final String repoId = "semic";
  private static final String repoId = "semic2";
//  private static final String repoId = "demo";

  public static void main(String[] args) throws Exception {
    String sourceFile = "C:\\Temp\\esco\\pieter\\20150220-esco_consolidated_enriched.rdf";

    String rdfUser = "escordf";
    String rdfPassword = "secret";
//    RemoteSesameDatasource xxx = new RemoteSesameDatasource("http://tfvirt-esco-02-index:7001/openrdf-sesame", "SYSTEM",0 rdfUser, rdfPassword);
    RemoteSesameDatasource xxx = new RemoteSesameDatasource("http://tfvirt-esco-02-index:7001/openrdf-sesame", "SYSTEM", null, null);
    List<Statement> statements1 = xxx.getConnection().getStatements(null, null, null, false).asList();
    System.out.println();

    RemoteSesameDatasource destinationDataSource = new RemoteSesameDatasource(destinationRdfServerUrl, repoId, rdfUser, rdfPassword);




    System.out.println("Loading statements...");
    Repository repository = new SailRepository(new MemoryStore());
    repository.initialize();
    SesameUtils.add(repository, new File(sourceFile));
    RepositoryConnection sourceDataSource = repository.getConnection();
    RepositoryResult<Statement> statements = sourceDataSource.getStatements(null, null, null, false);
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

    connection.close();

    destinationDataSource.closeQuietly();
    sourceDataSource.close();
  }
}
