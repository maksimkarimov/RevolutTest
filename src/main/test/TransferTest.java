import api.GuiceModuleTest;
import api.Main;
import api.dao.AccountDao;
import api.dao.TransferDao;
import api.models.Account;
import api.response.TransferResponse;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.dbutils.QueryRunner;
import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.tools.RunScript;
import org.junit.*;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TransferTest {

    private Injector injector;

    @Before
    public void init() throws SQLException {
        injector = Guice.createInjector(new GuiceModuleTest());

        startH2DB();
    }

    @After
    public void destroy() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:h2:mem:revolut_test", "sa", "sa");
        QueryRunner runner = new QueryRunner();
        runner.execute(connection, "DROP TABLE ACCOUNT");
        runner.execute(connection, "DROP TABLE TRANSFER");
    }


    private void startH2DB() throws SQLException {
        JdbcConnectionPool cp = JdbcConnectionPool.create(
                "jdbc:h2:mem:revolut_test", "sa", "sa");
        String migrationFileName = "/migration/start_migration.sql";
        try (InputStream is = Main.class.getResourceAsStream(migrationFileName)) {
            RunScript.execute(cp.getConnection(), new BufferedReader(new InputStreamReader(is)));
        } catch (NullPointerException | FileNotFoundException e) {
            System.out.println(String.format("File '%s' not found in resources", migrationFileName));
        } catch (IOException e) {
            System.out.println("H2 didn't start. " + e.getMessage());
        }
    }

    @Test
    public void checkDBAccounts() throws SQLException {
        AccountDao accountDao = injector.getInstance(AccountDao.class);

        Assert.assertEquals(accountDao.getAll().size(), 2);
    }

    @Test
    public void checkTransfer() throws SQLException {
        TransferDao transferDao = injector.getInstance(TransferDao.class);
        AccountDao accountDao = injector.getInstance(AccountDao.class);

        Account fromOld = accountDao.getById(1L);
        Account toOld = accountDao.getById(2L);

        Double amount = 500D;

        TransferResponse transferResponse = transferDao.makeTransfer(fromOld.getId(), toOld.getId(), amount);
        Assert.assertTrue(transferResponse.isDone());

        Account fromNew = accountDao.getById(fromOld.getId());
        Account toNew = accountDao.getById(toOld.getId());
        Assert.assertEquals(fromOld.getBalance() - amount, fromNew.getBalance(), 0);
        Assert.assertEquals(toOld.getBalance() + amount, toNew.getBalance(), 0);
    }

}