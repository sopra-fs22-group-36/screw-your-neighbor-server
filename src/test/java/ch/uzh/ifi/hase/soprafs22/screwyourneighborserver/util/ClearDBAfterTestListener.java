package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.persistence.EntityManager;
import javax.sql.DataSource;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

public class ClearDBAfterTestListener implements TestExecutionListener {
  public ClearDBAfterTestListener() {}

  @Override
  public void beforeTestClass(TestContext testContext) {}

  @Override
  public void afterTestMethod(TestContext testContext) throws SQLException {
    AutowireCapableBeanFactory autowireCapableBeanFactory =
        testContext.getApplicationContext().getAutowireCapableBeanFactory();
    DataSource dataSource = autowireCapableBeanFactory.getBean(DataSource.class);
    EntityManager entityManager = autowireCapableBeanFactory.getBean(EntityManager.class);
    try (Connection connection = dataSource.getConnection()) {
      connection.setAutoCommit(false);
      ArrayList<String> tableNames = new ArrayList<>();
      ResultSet tables =
          connection
              .getMetaData()
              .getTables(connection.getCatalog(), null, null, new String[] {"TABLE"});
      while (tables.next()) {
        tableNames.add(tables.getString("TABLE_NAME"));
      }
      connection.createStatement().execute("SET FOREIGN_KEY_CHECKS = 0");
      tableNames.forEach(s -> truncateTable(connection, s));
      connection.createStatement().execute("SET FOREIGN_KEY_CHECKS = 1");
      connection.commit();
      entityManager.clear();
    }
  }

  private void truncateTable(Connection connection, String tableName) {
    try {
      connection.createStatement().execute("TRUNCATE TABLE %s".formatted(tableName));
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void afterTestClass(TestContext testContext) {}
}
