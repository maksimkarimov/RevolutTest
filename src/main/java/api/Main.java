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


    public static void main(String[] args) throws SQLException {
        URI baseUri = UriBuilder.fromUri(host).port(port).build();
        ResourceConfig config = new ResourceConfig(AccountController.class, TransferController.class);
        JdkHttpServerFactory.createHttpServer(baseUri, config);

        configureGuice();
        dbMigration();

        System.out.println("Application ready to work.");
    }

    private static void configureGuice() {
        Guice.createInjector(new GuiceModule());
    }

    private static void dbMigration() throws SQLException {
        JdbcConnectionPool cp = JdbcConnectionPool.create(
                "jdbc:h2:mem:revolut", "sa", "sa");
        String migrationFileName = "/migration/start_migration.sql";
        try (InputStream is = Main.class.getResourceAsStream(migrationFileName)) {
            RunScript.execute(cp.getConnection(), new BufferedReader(new InputStreamReader(is)));
        } catch (NullPointerException | FileNotFoundException e) {
            System.out.println(String.format("File '%s' not found in resources", migrationFileName));
        } catch (IOException e) {
            System.out.println("H2 didn't start. " + e.getMessage());
        }
    }
}