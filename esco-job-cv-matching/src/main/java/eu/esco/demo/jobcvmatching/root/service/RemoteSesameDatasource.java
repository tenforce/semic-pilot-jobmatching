package eu.esco.demo.jobcvmatching.root.service;

import com.tenforce.exception.TenforceException;
import com.tenforce.sesame.SesameUtils;
import com.tenforce.sesame.template.SesameDatasource;
import org.apache.commons.lang3.StringUtils;
import org.openrdf.OpenRDFException;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.manager.RemoteRepositoryManager;
import org.openrdf.repository.manager.RepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;

import java.util.Properties;

public class RemoteSesameDatasource implements SesameDatasource {
  private static final Logger log = LoggerFactory.getLogger(RemoteSesameDatasource.class);

  private final String serverUrl;
  private final String repositoryId;
  private final String username;
  private final String password;

  // The repository manager
  private RepositoryManager repositoryManager;

  // From repositoryManager.getRepository(...) - the actual repository we will work with
  private Repository repository;

  private ValueFactory valueFactory;

  public RemoteSesameDatasource(String serverUrl, String repositoryId, String username, String password) {
    this.serverUrl = serverUrl;
    this.repositoryId = repositoryId;
    this.username = username;
    this.password = password;

    init();
  }

  private void init() {
    initRepoManager();
    initRepo();
  }

  @Override
  public RepositoryConnection getConnection() {
    try {
      // Open a connection to this repository
      RepositoryConnection repositoryConnection = repository.getConnection();
      repositoryConnection.setAutoCommit(false);
      return repositoryConnection;
    }

    catch (OpenRDFException e) {
      log.error("Unable to establish a connection to the repository '{}': ", e.getMessage());
      throw new TenforceException("Unable to establish a connection to the repository '{}': {}", repositoryId, e.getMessage(), e);
    }
  }

  @Override
  public ValueFactory getValueFactory() {
    if (valueFactory == null) {
      valueFactory = repository.getValueFactory();
    }
    return valueFactory;
  }

  @Override
  public void closeQuietly(RepositoryConnection connection) {
    SesameUtils.closeQuietly(connection);
  }

  @Override
  public void closeQuietly() {
    log.info("Destroy OwlimRepositoryService");
    SesameUtils.shutDownQuietly(repository);
    if (repositoryManager != null) repositoryManager.shutDown();
  }

  private void initRepo() {
    try {
      log.info("init repository {url {} - repoid {}}", serverUrl, repositoryId);
      repository = repositoryManager.getRepository(repositoryId);
      if (repository == null) {
        throw new TenforceException("Unknown repository '{}' on the Sesame server located at {}", repositoryId, serverUrl);
      }
    }
    catch (OpenRDFException e) {
      throw new TenforceException(e);
    }
  }

  private void initRepoManager() {
    try {
      // Create a manager for the remote Sesame server and initialise it
      RemoteRepositoryManager remote = new RemoteRepositoryManager(serverUrl);
      if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
        log.info("init repository manager with username/password");
        remote.setUsernameAndPassword(username, password);
      }
      repositoryManager = remote;
      log.info("init repository manager {}", serverUrl);
      repositoryManager.initialize();
    }
    catch (RepositoryException e) {
      log.error("Unable to establish a connection with the Sesame server '{}': {}", serverUrl, e.getMessage());
      throw new TenforceException(e);
    }
  }
}
