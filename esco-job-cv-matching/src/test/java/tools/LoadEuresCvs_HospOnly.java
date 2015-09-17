package tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenforce.exception.TenforceException;
import eu.esco.demo.jobcvmatching.root.HardcodedConfiguration;
import eu.esco.demo.jobcvmatching.root.SemicRdfModel;
import eu.esco.demo.jobcvmatching.root.service.RemoteSesameDatasource;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LoadEuresCvs_HospOnly {
  private static final String hospUrisJson = "[\n" +
                                             "{\"iscoCode\": 1221,\"uri\": \"http://data.europa.eu/esco/occupation/563\"},\n" +
                                             "{\"iscoCode\": 1222,\"uri\": \"http://data.europa.eu/esco/occupation/567\"},\n" +
                                             "{\"iscoCode\": 1411,\"uri\": \"http://data.europa.eu/esco/occupation/155\"},\n" +
                                             "{\"iscoCode\": 1412,\"uri\": \"http://data.europa.eu/esco/occupation/147\"},\n" +
                                             "{\"iscoCode\": 1431,\"uri\": \"http://data.europa.eu/esco/occupation/394\"},\n" +
                                             "{\"iscoCode\": 1439,\"uri\": \"http://data.europa.eu/esco/occupation/445\"},\n" +
                                             "{\"iscoCode\": 2413,\"uri\": \"http://data.europa.eu/esco/occupation/566\"},\n" +
                                             "{\"iscoCode\": 2431,\"uri\": \"http://data.europa.eu/esco/occupation/579\"},\n" +
                                             "{\"iscoCode\": 2611,\"uri\": \"http://data.europa.eu/esco/occupation/525\"},\n" +
                                             "{\"iscoCode\": 3332,\"uri\": \"http://data.europa.eu/esco/occupation/109\"},\n" +
                                             "{\"iscoCode\": 3339,\"uri\": \"http://data.europa.eu/esco/occupation/75\"},\n" +
                                             "{\"iscoCode\": 3434,\"uri\": \"http://data.europa.eu/esco/occupation/358\"},\n" +
                                             "{\"iscoCode\": 4221,\"uri\": \"http://data.europa.eu/esco/occupation/202\"},\n" +
                                             "{\"iscoCode\": 4224,\"uri\": \"http://data.europa.eu/esco/occupation/190\"},\n" +
                                             "{\"iscoCode\": 4229,\"uri\": \"http://data.europa.eu/esco/occupation/207\"},\n" +
                                             "{\"iscoCode\": 5111,\"uri\": \"http://data.europa.eu/esco/occupation/212\"},\n" +
                                             "{\"iscoCode\": 5113,\"uri\": \"http://data.europa.eu/esco/occupation/228\"},\n" +
                                             "{\"iscoCode\": 5120,\"uri\": \"http://data.europa.eu/esco/occupation/396\"},\n" +
                                             "{\"iscoCode\": 5131,\"uri\": \"http://data.europa.eu/esco/occupation/497\"},\n" +
                                             "{\"iscoCode\": 5132,\"uri\": \"http://data.europa.eu/esco/occupation/485\"},\n" +
                                             "{\"iscoCode\": 5151,\"uri\": \"http://data.europa.eu/esco/occupation/503\"},\n" +
                                             "{\"iscoCode\": 5153,\"uri\": \"http://data.europa.eu/esco/occupation/511\"},\n" +
                                             "{\"iscoCode\": 5169,\"uri\": \"http://data.europa.eu/esco/occupation/501\"},\n" +
                                             "{\"iscoCode\": 5230,\"uri\": \"http://data.europa.eu/esco/occupation/514\"},\n" +
                                             "{\"iscoCode\": 9112,\"uri\": \"http://data.europa.eu/esco/occupation/315\"},\n" +
                                             "{\"iscoCode\": 9121,\"uri\": \"http://data.europa.eu/esco/occupation/240\"},\n" +
                                             "{\"iscoCode\": 9411,\"uri\": \"http://data.europa.eu/esco/occupation/495\"},\n" +
                                             "{\"iscoCode\": 9412,\"uri\": \"http://data.europa.eu/esco/occupation/486\"},\n" +
                                             "{\"iscoCode\": 9621,\"uri\": \"http://data.europa.eu/esco/occupation/232\"},\n" +
                                             "{\"iscoCode\": 9622,\"uri\": \"http://data.europa.eu/esco/occupation/230\"},\n" +
                                             "{\"iscoCode\": 9629,\"uri\": \"http://data.europa.eu/esco/occupation/84\"}\n" +
                                             "]";

  private static final Set<String> hospUris = new HashSet<>();

  static {
    try {
      List list = new ObjectMapper().readValue(hospUrisJson, List.class);
      for (Object o : list) {
        hospUris.add((String) ((Map) o).get("uri"));
      }
    }
    catch (IOException e) {
      throw new TenforceException(e);
    }
  }

  public static void main(String[] args) throws Exception {
    Class.forName(oracle.jdbc.OracleDriver.class.getName());

    Set<Long> hospCvIds = new HashSet<>();
    try (Connection connection = DriverManager.getConnection("jdbc:oracle:thin:@tfvirt-esco-ora01:1521:orcl", "eures3", "eures3")) {
      PreparedStatement preparedStatement = connection.prepareStatement("select * from cvo_work_experience, cvo_occupation where cvo_work_experience.occupation_id = cvo_occupation.id");
      ResultSet resultSet = preparedStatement.executeQuery();

      while (resultSet.next()) {
        String iscoUri = resultSet.getString("ISCO4_ESCO_CODE");
        iscoUri = iscoUri.replace("http://ec.europa.eu/", "http://data.europa.eu/");
        if (hospUris.contains(iscoUri)) hospCvIds.add(resultSet.getLong("CV_ID"));
      }
    }


    String rdfUser = "escordf";
    String rdfPassword = "secret";
    RemoteSesameDatasource destinationDataSource = new RemoteSesameDatasource(HardcodedConfiguration.sesameUrl, HardcodedConfiguration.sesameRepository, rdfUser, rdfPassword);


    String baseUri = "http://data.europa.eu/esco/semic/document/cv/eures/";

    ValueFactory vf = destinationDataSource.getValueFactory();
    URI context = vf.createURI(baseUri);
    URI hasSkill = vf.createURI(SemicRdfModel.propertyHasSkill_v0);
    URI hasOccupation = vf.createURI(SemicRdfModel.propertyHasOccupation_v0);
    URI hasIsco = vf.createURI(SemicRdfModel.propertyHasIsco);
    URI cvType = vf.createURI(SemicRdfModel.typeCV);
    URI inSector = vf.createURI(SemicRdfModel.propertyInSector);
    URI sectorHosp = vf.createURI(SemicRdfModel.sectorHosp);
    RepositoryConnection repositoryConnection = destinationDataSource.getConnection();

    int addHostCvCount = 0;
    try (Connection connection = DriverManager.getConnection("jdbc:oracle:thin:@tfvirt-esco-ora01:1521:orcl", "eures3", "eures3")) {
      PreparedStatement workExperienceStatement = connection.prepareStatement(
          "select cv_id, esco_code, isco4_esco_code from cvo_occupation, CVO_WORK_EXPERIENCE where CVO_WORK_EXPERIENCE.occupation_id = cvo_occupation.id");
      ResultSet workExperienceRS = workExperienceStatement.executeQuery();
      Map<Long, List<Pair<String, String>>> workExperienceInfo = new HashMap<>();
      while(workExperienceRS.next()) {
        long cv_id = workExperienceRS.getLong("cv_id");
        String occ =workExperienceRS.getString("esco_code");
        String isco =workExperienceRS.getString("isco4_esco_code");

        List<Pair<String, String>> pairs = workExperienceInfo.get(cv_id);
        if(null == pairs) workExperienceInfo.put(cv_id, pairs = new ArrayList<>());
        pairs.add(new ImmutablePair<>(occ, isco));
      }



      PreparedStatement preparedStatement = connection.prepareStatement(
          "select cv2.id aajdie, esco_code  from " +
          "     cvo_cv cv2, cvo_cv_skill, cvo_skill, (select count(SKILL_ID) as skillCount, cvo_cv.id as cvId from cvo_cv, cvo_cv_skill where cvo_cv_skill.cv_id = cvo_cv.id group by cvo_cv_skill.cv_id) " +
          "     where cv2.id = cvId and cvo_skill.id = cvo_cv_skill.skill_id and cvo_cv_skill.cv_id = cvId and skillCount >3 and skillcount <20 ");


      ResultSet resultSet = preparedStatement.executeQuery();


      while (resultSet.next()) {
        long id = resultSet.getLong("aajdie");
        if (!hospCvIds.contains(id)) continue;
        addHostCvCount++;

        String escoUri = resultSet.getString("esco_code");
        escoUri = escoUri.replace("http://ec.europa.eu/", "http://data.europa.eu/");
        repositoryConnection.add(vf.createURI(baseUri + id), hasSkill, vf.createURI(escoUri), context);
        repositoryConnection.add(vf.createURI(baseUri + id), RDF.TYPE, cvType, context);
        repositoryConnection.add(vf.createURI(baseUri + id), inSector, sectorHosp, context);

        List<Pair<String, String>> pairs = workExperienceInfo.get(id);
        if(null == pairs) continue;
        for (Pair<String, String> pair : pairs) {
          repositoryConnection.remove(vf.createURI(baseUri + id), hasOccupation, vf.createURI(pair.getLeft()), context);
          String occUri = pair.getLeft().replace("http://ec.europa.eu/", "http://data.europa.eu/");
          repositoryConnection.add(vf.createURI(baseUri + id), hasOccupation, vf.createURI(occUri), context);

          repositoryConnection.remove(vf.createURI(baseUri + id), hasIsco, vf.createURI(pair.getRight()), context);
          String iscoUri = pair.getRight().replace("http://ec.europa.eu/", "http://data.europa.eu/");
          repositoryConnection.add(vf.createURI(baseUri + id), hasIsco, vf.createURI(iscoUri), context);
        }


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
    System.out.println("Total HOSP CV's: " + hospCvIds.size());
    System.out.println("Added HOSP CV's: " + addHostCvCount);
  }
}
