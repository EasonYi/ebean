package io.ebeaninternal.dbmigration;

import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.annotation.Platform;
import io.ebean.config.DatabaseConfig;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/**
 * This is the Migrationscript generator. It generates 3 migrationscript for the models
 * @author Roland Praml, FOCONIS AG
 *
 */
public class DbMigrationGenerateTest {

  private static final Logger logger = LoggerFactory.getLogger(DbMigrationGenerateTest.class);

  public static void main(String[] args) throws IOException {
    run("ebean-test/src/test/resources");
  }

  @Test
  public void invokeTest() throws IOException {
    run("src/test/resources");
  }

  public static void run(String pathToResources) throws IOException {
    logger.info("start current directory: " + new File(".").getAbsolutePath());


    // First, we clean up the output-directory
    Files.walk(Paths.get(pathToResources, "migrationtest"))
      .filter(Files::isRegularFile).map(Path::toFile).forEach(File::delete);

    DefaultDbMigration migration = new DefaultDbMigration();
    migration.setIncludeIndex(true);
    // We use src/test/resources as output directory (so we see in GIT if files will change)
    migration.setPathToResources(pathToResources);

    migration.addPlatform(Platform.CLICKHOUSE);
    migration.addPlatform(Platform.COCKROACH);
    migration.addPlatform(Platform.DB2FORI);
    migration.addPlatform(Platform.DB2LUW);
    migration.addPlatform(Platform.DB2ZOS);
    migration.addPlatform(Platform.GENERIC);
    migration.addPlatform(Platform.H2);
    migration.addPlatform(Platform.HANA);
    migration.addPlatform(Platform.HSQLDB);
    migration.addPlatform(Platform.MARIADB);
    migration.addPlatform(Platform.MARIADB, "mariadb-noprocs");
    migration.addPlatform(Platform.MYSQL);
    migration.addPlatform(Platform.MYSQL55);
    migration.addPlatform(Platform.NUODB);
    migration.addPlatform(Platform.ORACLE);
    migration.addPlatform(Platform.ORACLE11);
    migration.addPlatform(Platform.POSTGRES);
    migration.addPlatform(Platform.POSTGRES9);
    migration.addPlatform(Platform.SQLANYWHERE);
    migration.addPlatform(Platform.SQLITE);
    migration.addPlatform(Platform.SQLSERVER16);
    migration.addPlatform(Platform.SQLSERVER17);
    migration.addPlatform(Platform.YUGABYTE);


    DatabaseConfig config = new DatabaseConfig();
    config.setName("migrationtest");
    config.loadFromProperties();
    config.setRegister(false);
    config.setDefaultServer(false);
    config.getProperties().put("ebean.hana.generateUniqueDdl", "true"); // need to generate unique statements to prevent them from being filtered out as duplicates by the DdlRunner

    config.setPackages(Arrays.asList("misc.migration.v1_0"));
    Database server = DatabaseFactory.create(config);
    migration.setServer(server);

    // then we generate migration scripts for v1_0
    assertThat(migration.generateMigration()).isEqualTo("1.0__initial");
    // and we check repeatative calls
    assertThat(migration.generateMigration()).isNull();

    // and now for v1_1
    config.setPackages(Arrays.asList("misc.migration.v1_1"));
    server.shutdown();
    server = DatabaseFactory.create(config);
    migration.setServer(server);
    assertThat(migration.generateMigration()).isEqualTo("1.1");
    assertThat(migration.generateMigration()).isNull(); // subsequent call



    System.setProperty("ddl.migration.pendingDropsFor", "1.1");
    assertThat(migration.generateMigration()).isEqualTo("1.2__dropsFor_1.1");

    assertThatThrownBy(()->migration.generateMigration())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("No 'pendingDrops'"); // subsequent call

    System.clearProperty("ddl.migration.pendingDropsFor");
    assertThat(migration.generateMigration()).isNull(); // subsequent call

    // and now for v1_2 with
    config.setPackages(Arrays.asList("misc.migration.v1_2"));
    server.shutdown();
    server = DatabaseFactory.create(config);
    migration.setServer(server);
    assertThat(migration.generateMigration()).isEqualTo("1.3");
    assertThat(migration.generateMigration()).isNull(); // subsequent call


    System.setProperty("ddl.migration.pendingDropsFor", "1.3");
    assertThat(migration.generateMigration()).isEqualTo("1.4__dropsFor_1.3");
    assertThatThrownBy(migration::generateMigration)
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("No 'pendingDrops'"); // subsequent call

    System.clearProperty("ddl.migration.pendingDropsFor");
    assertThat(migration.generateMigration()).isNull(); // subsequent call

    server.shutdown();
    logger.info("end");
  }

}
