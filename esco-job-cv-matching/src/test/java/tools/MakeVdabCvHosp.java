package tools;

import eu.esco.demo.jobcvmatching.root.HardcodedConfiguration;
import eu.esco.demo.jobcvmatching.root.SemicRdfModel;
import eu.esco.demo.jobcvmatching.root.service.RemoteSesameDatasource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;

public class MakeVdabCvHosp {
  private static final String[] hospUris = {"http://data.europa.eu/esco/semic/document/cv/vdab/18",
                                            "http://data.europa.eu/esco/semic/document/cv/vdab/1",
                                            "http://data.europa.eu/esco/semic/document/cv/vdab/101",
                                            "http://data.europa.eu/esco/semic/document/cv/vdab/103",
                                            "http://data.europa.eu/esco/semic/document/cv/vdab/100",
                                            "http://data.europa.eu/esco/semic/document/cv/vdab/110",
                                            "http://data.europa.eu/esco/semic/document/cv/vdab/11",
                                            "http://data.europa.eu/esco/semic/document/cv/vdab/77",
                                            "http://data.europa.eu/esco/semic/document/cv/vdab/111",
                                            "http://data.europa.eu/esco/semic/document/cv/vdab/56",
                                            "http://data.europa.eu/esco/semic/document/cv/vdab/131",
                                            "http://data.europa.eu/esco/semic/document/cv/vdab/139",
                                            "http://data.europa.eu/esco/semic/document/cv/vdab/184",
                                            "http://data.europa.eu/esco/semic/document/cv/vdab/204",
                                            "http://data.europa.eu/esco/semic/document/cv/vdab/183",
                                            "http://data.europa.eu/esco/semic/document/cv/vdab/252",
                                            "http://data.europa.eu/esco/semic/document/cv/vdab/128"};


  public static void main(String[] args) throws Exception {
    Class.forName(oracle.jdbc.OracleDriver.class.getName());


    String rdfUser = "escordf";
    String rdfPassword = "secret";
    RemoteSesameDatasource destinationDataSource = new RemoteSesameDatasource(HardcodedConfiguration.sesameUrl, HardcodedConfiguration.sesameRepository, rdfUser, rdfPassword);

    ValueFactory vf = destinationDataSource.getValueFactory();
    URI inSector = vf.createURI(SemicRdfModel.propertyInSector);
    URI sectorHosp = vf.createURI(SemicRdfModel.sectorHosp);



    RepositoryConnection repositoryConnection = destinationDataSource.getConnection();
    for (String hospUri : hospUris) {

      URI uri = vf.createURI(hospUri);
      repositoryConnection.add(uri, inSector, sectorHosp, uri);


    }
    repositoryConnection.commit();
      repositoryConnection.close();
  }
}
