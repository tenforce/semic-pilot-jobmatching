package tools;

import eu.esco.demo.jobcvmatching.root.HardcodedConfiguration;
import eu.esco.demo.jobcvmatching.root.service.RemoteSesameDatasource;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

public class SetToUse {

  public static void main(String[] args) throws Exception {
    String rdfUser = "escordf";
    String rdfPassword = "secret";
    RemoteSesameDatasource destinationDataSource = new RemoteSesameDatasource(HardcodedConfiguration.sesameUrl, HardcodedConfiguration.sesameRepository, rdfUser, rdfPassword);
    RepositoryConnection connection = destinationDataSource.getConnection();



//    connection.add(vf.createURI("http://data.europa.eu/esco/semic/document/cv/vdab/183"), vf.createURI("http://data.europa.eu/esco/semic/model#toUse"), vf.createLiteral(true), vf.createURI("http://data.europa.eu/esco/semic/document/cv/vdab/183"));
//    connection.add(vf.createURI("http://data.europa.eu/esco/semic/document/cv/vdab/101"), vf.createURI("http://data.europa.eu/esco/semic/model#toUse"), vf.createLiteral(true), vf.createURI("http://data.europa.eu/esco/semic/document/cv/vdab/101"));
//    connection.add(vf.createURI("http://data.europa.eu/esco/semic/document/cv/vdab/103"), vf.createURI("http://data.europa.eu/esco/semic/model#toUse"), vf.createLiteral(true), vf.createURI("http://data.europa.eu/esco/semic/document/cv/vdab/103"));
//    connection.add(vf.createURI("http://data.europa.eu/esco/semic/document/cv/vdab/100"), vf.createURI("http://data.europa.eu/esco/semic/model#toUse"), vf.createLiteral(true), vf.createURI("http://data.europa.eu/esco/semic/document/cv/vdab/100"));
//    connection.add(vf.createURI("http://data.europa.eu/esco/semic/document/cv/vdab/11"), vf.createURI("http://data.europa.eu/esco/semic/model#toUse"), vf.createLiteral(true), vf.createURI("http://data.europa.eu/esco/semic/document/cv/vdab/11"));
//    connection.add(vf.createURI("http://data.europa.eu/esco/semic/document/cv/vdab/77"), vf.createURI("http://data.europa.eu/esco/semic/model#toUse"), vf.createLiteral(true), vf.createURI("http://data.europa.eu/esco/semic/document/cv/vdab/77"));
//    connection.add(vf.createURI("http://data.europa.eu/esco/semic/document/cv/vdab/111"), vf.createURI("http://data.europa.eu/esco/semic/model#toUse"), vf.createLiteral(true), vf.createURI("http://data.europa.eu/esco/semic/document/cv/vdab/111"));
//    connection.add(vf.createURI("http://data.europa.eu/esco/semic/document/cv/vdab/56"), vf.createURI("http://data.europa.eu/esco/semic/model#toUse"), vf.createLiteral(true), vf.createURI("http://data.europa.eu/esco/semic/document/cv/vdab/56"));
//    connection.add(vf.createURI("http://data.europa.eu/esco/semic/document/cv/vdab/131"), vf.createURI("http://data.europa.eu/esco/semic/model#toUse"), vf.createLiteral(true), vf.createURI("http://data.europa.eu/esco/semic/document/cv/vdab/131"));
//    connection.add(vf.createURI("http://data.europa.eu/esco/semic/document/cv/vdab/139"), vf.createURI("http://data.europa.eu/esco/semic/model#toUse"), vf.createLiteral(true), vf.createURI("http://data.europa.eu/esco/semic/document/cv/vdab/139"));
//    connection.add(vf.createURI("http://data.europa.eu/esco/semic/document/cv/vdab/204"), vf.createURI("http://data.europa.eu/esco/semic/model#toUse"), vf.createLiteral(true), vf.createURI("http://data.europa.eu/esco/semic/document/cv/vdab/204"));

//    setToUse(connection, "http://data.europa.eu/esco/semic/document/cv/eures/16870", "http://data.europa.eu/esco/semic/document/cv/eures/");
//    setToUse(connection, "http://data.europa.eu/esco/semic/document/cv/eures/8545", "http://data.europa.eu/esco/semic/document/cv/eures/");
//    setToUse(connection, "http://data.europa.eu/esco/semic/document/cv/eures/141912", "http://data.europa.eu/esco/semic/document/cv/eures/");
//    setToUse(connection, "http://data.europa.eu/esco/semic/document/cv/eures/179544", "http://data.europa.eu/esco/semic/document/cv/eures/");
//    setToUse(connection, "http://data.europa.eu/esco/semic/document/cv/eures/141331", "http://data.europa.eu/esco/semic/document/cv/eures/");
//    setToUse(connection, "http://data.europa.eu/esco/semic/document/cv/eures/12475", "http://data.europa.eu/esco/semic/document/cv/eures/");
//    setToUse(connection, "http://data.europa.eu/esco/semic/document/cv/eures/87187", "http://data.europa.eu/esco/semic/document/cv/eures/");
//    setToUse(connection, "http://data.europa.eu/esco/semic/document/cv/vdab/252", "http://data.europa.eu/esco/semic/document/cv/vdab/252");

    setToUse(connection, "http://data.europa.eu/esco/semic/document/cv/eures/174704", "http://data.europa.eu/esco/semic/document/cv/eures/");
    setToUse(connection, "http://data.europa.eu/esco/semic/document/cv/eures/148632", "http://data.europa.eu/esco/semic/document/cv/eures/");
    setToUse(connection, "http://data.europa.eu/esco/semic/document/cv/eures/155224", "http://data.europa.eu/esco/semic/document/cv/eures/");

    connection.commit();
    connection.close();
  }

  public static  void setToUse(RepositoryConnection connection, String uri, String graph) throws Exception {
    ValueFactory vf = connection.getValueFactory();
    connection.add(vf.createURI(uri), vf.createURI("http://data.europa.eu/esco/semic/model#toUse"), vf.createLiteral(true), vf.createURI(graph));











  }
}
