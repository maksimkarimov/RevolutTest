package api;

import java.io.*;
import java.net.URI;
import java.sql.SQLException;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.tools.RunScript;
import api.controllers.TestController;

public class Main {

    private final static int port = 8080;
    private final static String host = "http://localhost/";

    public static void main(String[] args) throws SQLException {
        System.out.println("Try to start server.");
        URI baseUri = UriBuilder.fromUri(host).port(port).build();
        ResourceConfig config = new ResourceConfig(TestController.class);
        JdkHttpServerFactory.createHttpServer(baseUri, config);
        System.out.println("Server started.");
        System.out.println("Try to start to start H2 database.");
        startH2DB();
        System.out.println("H2 started.");
    }

    private static void startH2DB() throws SQLException {
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