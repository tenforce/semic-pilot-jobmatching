package tools;

import eu.esco.demo.jobcvmatching.root.HardcodedConfiguration;
import eu.esco.demo.jobcvmatching.root.SemicRdfModel;
import eu.esco.demo.jobcvmatching.root.service.RemoteSesameDatasource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;

public class AddDataTool {
  public static void main(String[] args) throws Exception {
    String rdfUser = "escordf";
    String rdfPassword = "secret";
    RemoteSesameDatasource dataSource = new RemoteSesameDatasource(HardcodedConfiguration.sesameUrl, HardcodedConfiguration.sesameRepository, rdfUser, rdfPassword);
    ValueFactory vf = dataSource.getValueFactory();
    RepositoryConnection connection = dataSource.getConnection();

    URI graph = vf.createURI(SemicRdfModel.namespace);
    URI subject = vf.createURI("http://data.europa.eu/esco/semic/document/jv/100");
    connection.add(subject, RDF.TYPE, vf.createURI(SemicRdfModel.typeJV), graph);
    connection.add(subject, DCTERMS.DESCRIPTION, vf.createLiteral("Some job vacancy"), graph);
    connection.add(subject, vf.createURI(SemicRdfModel.propertyHasOccupation_v0), vf.createURI("http://data.europa.eu/esco/occupation/23573"), graph);
    connection.commit();
    dataSource.closeQuietly();
  }
}
