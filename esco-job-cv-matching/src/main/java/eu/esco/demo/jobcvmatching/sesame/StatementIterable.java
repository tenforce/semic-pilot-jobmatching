package eu.esco.demo.jobcvmatching.sesame;

import com.tenforce.exception.TenforceException;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;

public class StatementIterable implements Iterable<Statement> {
  private final StatementIterator statementIterator;

  public StatementIterable(RepositoryResult<Statement> statements) {
    statementIterator = new StatementIterator(statements);
  }

  @Override
  public Iterator<Statement> iterator() {
    return statementIterator;
  }

  public static StatementIterable list(RepositoryConnection connection) {
    return list(connection, null, null, null);
  }

  public static StatementIterable list(@Nonnull RepositoryConnection connection,
                                       @Nullable Resource subject,
                                       @Nullable URI property,
                                       @Nullable Value value) {
    try {
      return new StatementIterable(connection.getStatements(subject, property, value, false));
    }
    catch (RepositoryException e) {
      throw new TenforceException(e);
    }
  }


  private static class StatementIterator implements Iterator<Statement> {
    private final RepositoryResult<Statement> statements;

    private StatementIterator(RepositoryResult<Statement> statements) {
      this.statements = statements;
    }

    @Override
    public boolean hasNext() {
      try {
        return statements.hasNext();
      }
      catch (RepositoryException e) {
        throw new TenforceException(e);
      }
    }

    @Override
    public Statement next() {
      try {
        return statements.next();
      }
      catch (RepositoryException e) {
        throw new TenforceException(e);
      }
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

}
