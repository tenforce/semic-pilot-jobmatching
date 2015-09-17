package tools;

import com.tenforce.exception.TenforceException;
import eu.esco.demo.jobcvmatching.root.HardcodedConfiguration;
import eu.esco.demo.jobcvmatching.root.SemicRdfModel;
import eu.esco.demo.jobcvmatching.root.service.RemoteSesameDatasource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoadEuresCvs {


  public static void main(String[] args) throws Exception {
    Class.forName(oracle.jdbc.OracleDriver.class.getName());

    String rdfUser = "escordf";
    String rdfPassword = "secret";
    RemoteSesameDatasource destinationDataSource = new RemoteSesameDatasource(HardcodedConfiguration.sesameUrl, HardcodedConfiguration.sesameRepository, rdfUser, rdfPassword);


    String baseUri = "http://data.europa.eu/esco/semic/document/cv/eures/";

    ValueFactory vf = destinationDataSource.getValueFactory();
    URI context = vf.createURI(baseUri);
    URI hasSkill = vf.createURI(SemicRdfModel.propertyHasSkill_v0);
    URI cvType = vf.createURI(SemicRdfModel.typeCV);
    RepositoryConnection repositoryConnection = destinationDataSource.getConnection();


    try (Connection connection = DriverManager.getConnection("jdbc:oracle:thin:@tfvirt-esco-ora01:1521:orcl", "eures3", "eures3")) {
      PreparedStatement preparedStatement = connection.prepareStatement(
          "select cv2.id aajdie, esco_code  from " +
          "     cvo_cv cv2, cvo_cv_skill, cvo_skill, (select count(SKILL_ID) as skillCount, cvo_cv.id as cvId from cvo_cv, cvo_cv_skill where cvo_cv_skill.cv_id = cvo_cv.id group by cvo_cv_skill.cv_id) " +
          "     where cv2.id = cvId and cvo_skill.id = cvo_cv_skill.skill_id and cvo_cv_skill.cv_id = cvId and skillCount >3 and skillcount <20 ");

      ResultSet resultSet = preparedStatement.executeQuery();

      while (resultSet.next()) {
        long id = resultSet.getLong("aajdie");
        String escoUri = resultSet.getString("esco_code");
        escoUri = escoUri.replace("http://ec.europa.eu/", "http://data.europa.eu/");


        repositoryConnection.add(vf.createURI(baseUri + id), hasSkill, vf.createURI(escoUri), context);
        repositoryConnection.add(vf.createURI(baseUri + id), RDF.TYPE, cvType, context);

      }

      repositoryConnection.commit();

    }
    catch (SQLException e) {
      repositoryConnection.rollback();
      throw new TenforceException(e);
    }
    finally {
      repositoryConnection.close();
    }


  }
}
