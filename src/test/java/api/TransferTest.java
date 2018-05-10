package api;

import api.dao.AccountDao;
import api.dao.TransferDao;
import api.models.Account;
import api.response.TransferResponse;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.commons.dbutils.QueryRunner;
import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.tools.RunScript;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class TransferTest {

    private Injector injector;
    private AccountDao accountDao;
    private TransferDao transferDao;

    @Before
    public void init() throws SQLException {
        injector = Guice.createInjector(new GuiceModuleTest());
        transferDao = injector.getInstance(TransferDao.class);
        accountDao = injector.getInstance(AccountDao.class);

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
        Assert.assertEquals(accountDao.getAll().size(), 2);
    }

    @Test
    public void checkTransfer() throws SQLException {
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

    @Test
    public void checkNotEnoughMoney() throws SQLException {
        Account from = accountDao.getById(2L);
        Account to = accountDao.getById(1L);

        TransferResponse transferResponse = transferDao.makeTransfer(from.getId(), to.getId(), 5000D);

        Assert.assertFalse(transferResponse.isDone());
        Assert.assertEquals(transferResponse.getErrorMessage(), "Not enough money for transfer");
    }

    @Test
    public void checkСonsistency() throws SQLException {
        int iterations = 50;
        double fromOneToTwoAmount = 100.0;
        double fromTwoToOneAmount = 10.0;


        List<Future<TransferResponse>> futuresFromOneToTwo = new ArrayList<>();
        List<Future<TransferResponse>> futuresFromTwoToOne = new ArrayList<>();

        List<TransferResponse> fromOneToTwo = new ArrayList<>();
        List<TransferResponse> fromTwoToOne = new ArrayList<>();
        Account oneOld = accountDao.getById(1L);
        Account twoOld = accountDao.getById(2L);

        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (int i = 0; i < iterations; i++) {
            futuresFromOneToTwo.add(executor.submit(() -> transferDao.makeTransfer(oneOld.getId(), twoOld.getId(), fromOneToTwoAmount)));
            sleepRandomMillis(0, 100);
            futuresFromTwoToOne.add(executor.submit(() -> transferDao.makeTransfer(twoOld.getId(), oneOld.getId(), fromTwoToOneAmount)));
            sleepRandomMillis(100, 500);
        }

        for (Future<TransferResponse> future : futuresFromOneToTwo) {
            try {
                fromOneToTwo.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                System.out.println(e.getMessage());
            }
        }

        for (Future<TransferResponse> future : futuresFromTwoToOne) {
            try {
                fromTwoToOne.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                System.out.println(e.getMessage());
            }
        }

        Account oneNew = accountDao.getById(1L);
        Account twoNew = accountDao.getById(2L);

        long fromOneToTwoDone = fromOneToTwo.stream().filter(TransferResponse::isDone).count();
        long fromTwoToOneDone = fromTwoToOne.stream().filter(TransferResponse::isDone).count();

        double expectedBalanceOne = (oneOld.getBalance() - (fromOneToTwoDone * fromOneToTwoAmount)) + (fromTwoToOneDone * fromTwoToOneAmount);
        Assert.assertEquals(expectedBalanceOne, oneNew.getBalance(), 0);
        double expectedBalanceTwo = (twoOld.getBalance() + (fromOneToTwoDone * fromOneToTwoAmount)) - (fromTwoToOneDone * fromTwoToOneAmount);
        Assert.assertEquals(expectedBalanceTwo, twoNew.getBalance(), 0);

    }

    private void sleepRandomMillis(int min, int max) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(min, max));
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

}