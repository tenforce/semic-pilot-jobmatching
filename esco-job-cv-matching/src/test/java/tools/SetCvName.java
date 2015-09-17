package tools;

import eu.esco.demo.jobcvmatching.root.HardcodedConfiguration;
import eu.esco.demo.jobcvmatching.root.service.RemoteSesameDatasource;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.repository.RepositoryConnection;

public class SetCvName {

  public static void main(String[] args) throws Exception {
    String rdfUser = "escordf";
    String rdfPassword = "secret";
    RemoteSesameDatasource destinationDataSource = new RemoteSesameDatasource(HardcodedConfiguration.sesameUrl, HardcodedConfiguration.sesameRepository, rdfUser, rdfPassword);
    RepositoryConnection connection = destinationDataSource.getConnection();


//    setName(connection, "http://data.europa.eu/esco/semic/document/cv/vdab/101", "http://data.europa.eu/esco/semic/document/cv/vdab/101", "Jar Jar");
//    setName(connection, "http://data.europa.eu/esco/semic/document/cv/vdab/103", "http://data.europa.eu/esco/semic/document/cv/vdab/103", "C3PO");
//    setName(connection, "http://data.europa.eu/esco/semic/document/cv/vdab/100", "http://data.europa.eu/esco/semic/document/cv/vdab/100", "Chewbacca");
//    setName(connection, "http://data.europa.eu/esco/semic/document/cv/vdab/11", "http://data.europa.eu/esco/semic/document/cv/vdab/11", "Count Dooku");
//    setName(connection, "http://data.europa.eu/esco/semic/document/cv/vdab/77", "http://data.europa.eu/esco/semic/document/cv/vdab/77", "Boba Fett");
//    setName(connection, "http://data.europa.eu/esco/semic/document/cv/vdab/111", "http://data.europa.eu/esco/semic/document/cv/vdab/111", "Greedo");
//    setName(connection, "http://data.europa.eu/esco/semic/document/cv/vdab/56", "http://data.europa.eu/esco/semic/document/cv/vdab/56", "Jabba the Hutt");
//    setName(connection, "http://data.europa.eu/esco/semic/document/cv/vdab/131", "http://data.europa.eu/esco/semic/document/cv/vdab/131", "Obi-Wan"); //Obi-Wan Kenobi
//    setName(connection, "http://data.europa.eu/esco/semic/document/cv/vdab/139", "http://data.europa.eu/esco/semic/document/cv/vdab/139", "Darth Maul");
//    setName(connection, "http://data.europa.eu/esco/semic/document/cv/vdab/204", "http://data.europa.eu/esco/semic/document/cv/vdab/204", "Princess Leia");
//    setName(connection, "http://data.europa.eu/esco/semic/document/cv/vdab/183", "http://data.europa.eu/esco/semic/document/cv/vdab/183", "Anakin"); //Anakin Skywalker
//    setName(connection, "http://data.europa.eu/esco/semic/document/cv/vdab/252", "http://data.europa.eu/esco/semic/document/cv/vdab/252", "Luke"); //Luke Skywalker
//
//    setName(connection, "http://data.europa.eu/esco/semic/document/cv/eures/16870", "http://data.europa.eu/esco/semic/document/cv/eures/", "Darth Vader");
//    setName(connection, "http://data.europa.eu/esco/semic/document/cv/eures/8545", "http://data.europa.eu/esco/semic/document/cv/eures/", "Yoda");
//    setName(connection, "http://data.europa.eu/esco/semic/document/cv/eures/141912", "http://data.europa.eu/esco/semic/document/cv/eures/", "Mace Windu");
//    setName(connection, "http://data.europa.eu/esco/semic/document/cv/eures/179544", "http://data.europa.eu/esco/semic/document/cv/eures/", "Han Solo");
//    setName(connection, "http://data.europa.eu/esco/semic/document/cv/eures/141331", "http://data.europa.eu/esco/semic/document/cv/eures/", "R2D2");
//    setName(connection, "http://data.europa.eu/esco/semic/document/cv/eures/12475", "http://data.europa.eu/esco/semic/document/cv/eures/", "Leia Organa");
//    setName(connection, "http://data.europa.eu/esco/semic/document/cv/eures/87187", "http://data.europa.eu/esco/semic/document/cv/eures/", "Palpatine");
//    setName(connection, "http://data.europa.eu/esco/semic/document/cv/eures/155224", "http://data.europa.eu/esco/semic/document/cv/eures/", "George Lucas");
//    setName(connection, "http://data.europa.eu/esco/semic/document/cv/eures/148632", "http://data.europa.eu/esco/semic/document/cv/eures/", "The Emperor");
//    setName(connection, "http://data.europa.eu/esco/semic/document/cv/eures/174704", "http://data.europa.eu/esco/semic/document/cv/eures/", "Qui-Gon Jinn");


    setName(connection, "http://data.europa.eu/esco/semic/document/cv/vdab/56", "http://data.europa.eu/esco/semic/document/cv/vdab/56", "AM");
    setName(connection, "http://data.europa.eu/esco/semic/document/cv/vdab/103", "http://data.europa.eu/esco/semic/document/cv/vdab/103", "AP");
    setName(connection, "http://data.europa.eu/esco/semic/document/cv/vdab/101", "http://data.europa.eu/esco/semic/document/cv/vdab/101", "DV");
    setName(connection, "http://data.europa.eu/esco/semic/document/cv/eures/155224", "http://data.europa.eu/esco/semic/document/cv/eures/", "GL");
    setName(connection, "http://data.europa.eu/esco/semic/document/cv/eures/179544", "http://data.europa.eu/esco/semic/document/cv/eures/", "HS");
    setName(connection, "http://data.europa.eu/esco/semic/document/cv/vdab/252", "http://data.europa.eu/esco/semic/document/cv/vdab/252", "IK");
    setName(connection, "http://data.europa.eu/esco/semic/document/cv/vdab/139", "http://data.europa.eu/esco/semic/document/cv/vdab/139", "JD");
    setName(connection, "http://data.europa.eu/esco/semic/document/cv/vdab/100", "http://data.europa.eu/esco/semic/document/cv/vdab/100", "KK");
    setName(connection, "http://data.europa.eu/esco/semic/document/cv/vdab/204", "http://data.europa.eu/esco/semic/document/cv/vdab/204", "LI");
    setName(connection, "http://data.europa.eu/esco/semic/document/cv/eures/12475", "http://data.europa.eu/esco/semic/document/cv/eures/", "LO");
    setName(connection, "http://data.europa.eu/esco/semic/document/cv/vdab/11", "http://data.europa.eu/esco/semic/document/cv/vdab/11", "ML");
    setName(connection, "http://data.europa.eu/esco/semic/document/cv/eures/8545", "http://data.europa.eu/esco/semic/document/cv/eures/", "MY");
    setName(connection, "http://data.europa.eu/esco/semic/document/cv/eures/141912", "http://data.europa.eu/esco/semic/document/cv/eures/", "MW");
    setName(connection, "http://data.europa.eu/esco/semic/document/cv/vdab/111", "http://data.europa.eu/esco/semic/document/cv/vdab/111", "NL");
    setName(connection, "http://data.europa.eu/esco/semic/document/cv/vdab/131", "http://data.europa.eu/esco/semic/document/cv/vdab/131", "OW");
    setName(connection, "http://data.europa.eu/esco/semic/document/cv/vdab/183", "http://data.europa.eu/esco/semic/document/cv/vdab/183", "PF");
    setName(connection, "http://data.europa.eu/esco/semic/document/cv/eures/87187", "http://data.europa.eu/esco/semic/document/cv/eures/", "PT");
    setName(connection, "http://data.europa.eu/esco/semic/document/cv/eures/174704", "http://data.europa.eu/esco/semic/document/cv/eures/", "QG");
    setName(connection, "http://data.europa.eu/esco/semic/document/cv/eures/141331", "http://data.europa.eu/esco/semic/document/cv/eures/", "RD");
    setName(connection, "http://data.europa.eu/esco/semic/document/cv/eures/148632", "http://data.europa.eu/esco/semic/document/cv/eures/", "TE");
    setName(connection, "http://data.europa.eu/esco/semic/document/cv/eures/16870", "http://data.europa.eu/esco/semic/document/cv/eures/", "UE");
    setName(connection, "http://data.europa.eu/esco/semic/document/cv/vdab/77", "http://data.europa.eu/esco/semic/document/cv/vdab/77", "VP");


    connection.commit();
    connection.close();
  }

  public static void setName(RepositoryConnection connection, String uri, String graph, String name) throws Exception {
    ValueFactory vf = connection.getValueFactory();

//    if (graph.contains("vdab")) name += " - VDAB";
//    else if (graph.contains("eures")) name += " - EURES";

    connection.remove(vf.createURI(uri), FOAF.GIVEN_NAME, null, vf.createURI(graph));
    connection.add(vf.createURI(uri), FOAF.GIVEN_NAME, vf.createLiteral(name), vf.createURI(graph));
  }
}
