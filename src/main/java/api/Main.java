package api;

import api.config.GuiceModule;
import api.controllers.AccountController;
import api.controllers.TransferController;
import com.google.inject.Guice;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.tools.RunScript;

import javax.ws.rs.core.UriBuilder;
import java.io.*;
import java.net.URI;
import java.sql.SQLException;

public class Main {

    private final static int port = 8080;
    private final static String host = "http://localhost/";
    private static final String MIGRATION_SQL = "/migration/start_migration.sql";


    public static void main(String[] args) throws SQLException {
        final URI baseUri = UriBuilder.fromUri(host).port(port).build();
        final ResourceConfig config = new ResourceConfig(AccountController.class, TransferController.class);
        JdkHttpServerFactory.createHttpServer(baseUri, config);

        configureGuice();
        dbMigration();

        System.out.println("Application ready to work.");
    }

    private static void configureGuice() {
        Guice.createInjector(new GuiceModule());
    }

    private static void dbMigration() throws SQLException {
        final JdbcConnectionPool cp = JdbcConnectionPool.create(
                "jdbc:h2:mem:revolut", "sa", "sa");
        try (final Reader reader = new BufferedReader(
                new InputStreamReader(Main.class.getResourceAsStream(MIGRATION_SQL)))) {
            RunScript.execute(cp.getConnection(), reader);
        } catch (NullPointerException | FileNotFoundException e) {
            System.out.println(String.format("File '%s' not found in resources", MIGRATION_SQL));
        } catch (IOException e) {
            System.out.println("H2 didn't start. " + e.getMessage());
        }
    }
}