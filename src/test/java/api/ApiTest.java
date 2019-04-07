package api;

import api.config.GuiceModuleTest;
import api.dao.AccountDao;
import api.dao.TransferDao;
import api.enums.ResponseError;
import api.models.Account;
import api.response.TransferResponse;
import api.service.AccountService;
import api.service.TransferService;
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

public class ApiTest {

    private AccountService accountService;
    private TransferService transferService;

    @Before
    public void init() throws SQLException {
        final Injector injector = Guice.createInjector(new GuiceModuleTest());
        transferService = injector.getInstance(TransferService.class);
        accountService = injector.getInstance(AccountService.class);

        dbMigration();
    }

    @After
    public void destroy() throws SQLException {
        final Connection connection =
                DriverManager.getConnection("jdbc:h2:mem:revolut_test", "sa", "sa");
        final QueryRunner runner = new QueryRunner();
        runner.execute(connection, "DROP TABLE ACCOUNT");
        runner.execute(connection, "DROP TABLE TRANSFER");
    }


    private void dbMigration() throws SQLException {
        final JdbcConnectionPool cp = JdbcConnectionPool.create(
                "jdbc:h2:mem:revolut_test", "sa", "sa");
        final String migrationFileName = "/migration/start_migration.sql";
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
        Assert.assertEquals(2, accountService.getAll().size());
    }

    @Test
    public void checkCreateAccount() throws SQLException {
        final Account account = new Account();

        final long accountBalance = 5555L;
        final String accountName = "test1";

        account.setBalance(accountBalance);
        account.setName(accountName);

        Account savedAccount = accountService.save(account);

        Assert.assertNotNull(savedAccount);

        Assert.assertEquals(accountBalance, savedAccount.getBalance().longValue());
        Assert.assertEquals(accountName, savedAccount.getName());
    }

    @Test
    public void checkUpdateAccountName() throws SQLException {
        final Account account = accountService.getById(1L);
        final String changedName = "changedName";
        account.setName(changedName);

        accountService.update(account);

        final Account savedAccount = accountService.getById(1L);

        Assert.assertEquals(changedName, savedAccount.getName());
    }

    @Test
    public void checkRemoveAccount() throws SQLException {
        long id = 2L;
        final Account account = accountService.getById(id);

        Assert.assertNotNull(account);

        accountService.delete(account.getId());

        final Account deletedAccount = accountService.getById(id);

        Assert.assertNull(deletedAccount);
    }


    @Test
    public void checkTransferWithNotExistAccount() throws SQLException {
        final TransferResponse response = transferService.makeTransfer(1L, 1000L, 500L);
        Assert.assertFalse(response.isDone());
        Assert.assertEquals(ResponseError.ACCOUNT_NOT_FOUND, response.getError());
    }

    @Test
    public void checkTransferWithoutAccount() throws SQLException {
        final TransferResponse response = transferService.makeTransfer(null, 1000L, 500L);
        Assert.assertFalse(response.isDone());
        Assert.assertEquals(ResponseError.INSUFFICIENT_DATA, response.getError());
    }

    @Test
    public void checkTransferToYourself() throws SQLException {
        final TransferResponse response = transferService.makeTransfer(1L, 1L, 500L);
        Assert.assertFalse(response.isDone());
        Assert.assertEquals(ResponseError.TRANSFER_ACCOUNTS_EQUALS, response.getError());
    }

    @Test
    public void checkTransfer() throws SQLException {
        final Account fromOld = accountService.getById(1L);
        final Account toOld = accountService.getById(2L);

        final Long amount = 500L;

        final TransferResponse transferResponse = transferService.makeTransfer(fromOld.getId(), toOld.getId(), amount);
        Assert.assertTrue(transferResponse.isDone());

        final Account fromNew = accountService.getById(fromOld.getId());
        final Account toNew = accountService.getById(toOld.getId());
        Assert.assertEquals(fromOld.getBalance() - amount, fromNew.getBalance(), 0);
        Assert.assertEquals(toOld.getBalance() + amount, toNew.getBalance(), 0);
    }

    @Test
    public void checkNotEnoughMoney() throws SQLException {
        final Account from = accountService.getById(2L);
        final Account to = accountService.getById(1L);
        final TransferResponse transferResponse = transferService.makeTransfer(from.getId(), to.getId(), 5000L);

        Assert.assertFalse(transferResponse.isDone());
        Assert.assertEquals(ResponseError.NOT_ENOUGH_MONEY, transferResponse.getError());
    }

    @Test
    public void checkNegativeAmount() throws SQLException {
        final Account from = accountService.getById(2L);
        final Account to = accountService.getById(1L);
        final TransferResponse transferResponse = transferService.makeTransfer(from.getId(), to.getId(), -100L);

        Assert.assertFalse(transferResponse.isDone());
        Assert.assertEquals(ResponseError.TRANSFER_LESS_THEN_ZERO, transferResponse.getError());
    }

    @Test
    public void checkZeroAmount() throws SQLException {
        final Account from = accountService.getById(2L);
        final Account to = accountService.getById(1L);
        final TransferResponse transferResponse = transferService.makeTransfer(from.getId(), to.getId(), 0L);

        Assert.assertFalse(transferResponse.isDone());
        Assert.assertEquals(ResponseError.TRANSFER_LESS_THEN_ZERO, transferResponse.getError());
    }


    @Test
    public void checkConsistency() throws SQLException {
        final int iterations = 50;
        final long fromOneToTwoAmount = 100;
        final long fromTwoToOneAmount = 10;


        final List<Future<TransferResponse>> futuresFromOneToTwo = new ArrayList<>();
        final List<Future<TransferResponse>> futuresFromTwoToOne = new ArrayList<>();

        final Account oneOld = accountService.getById(1L);
        final Account twoOld = accountService.getById(2L);

        final ExecutorService executor = Executors.newFixedThreadPool(10);
        for (int i = 0; i < iterations; i++) {
            futuresFromOneToTwo.add(executor.submit(() -> transferService.makeTransfer(oneOld.getId(), twoOld.getId(),
                    fromOneToTwoAmount)));
            futuresFromTwoToOne.add(executor.submit(() -> transferService.makeTransfer(twoOld.getId(), oneOld.getId(),
                    fromTwoToOneAmount)));
        }

        final List<TransferResponse> fromOneToTwo = fromFuturesToResponse(futuresFromOneToTwo);
        final List<TransferResponse> fromTwoToOne = fromFuturesToResponse(futuresFromTwoToOne);

        final Account oneNew = accountService.getById(1L);
        final Account twoNew = accountService.getById(2L);

        final long fromOneToTwoDone = fromOneToTwo.stream().filter(TransferResponse::isDone).count();
        final long fromTwoToOneDone = fromTwoToOne.stream().filter(TransferResponse::isDone).count();

        Assert.assertEquals(iterations, fromOneToTwoDone);
        Assert.assertEquals(iterations, fromTwoToOneDone);

        final long expectedBalanceOne = (oneOld.getBalance() - (fromOneToTwoDone * fromOneToTwoAmount))
                + (fromTwoToOneDone * fromTwoToOneAmount);
        Assert.assertEquals(Long.valueOf(expectedBalanceOne), oneNew.getBalance());
        final long expectedBalanceTwo = (twoOld.getBalance() + (fromOneToTwoDone * fromOneToTwoAmount))
                - (fromTwoToOneDone * fromTwoToOneAmount);
        Assert.assertEquals(Long.valueOf(expectedBalanceTwo), twoNew.getBalance());

        Assert.assertEquals(iterations * 2, transferService.getAll().size());
    }


    @Test
    public void checkWithdraw() throws SQLException {
        final Long count = 100L;
        long accountId = 1L;
        final Account oldAccount = accountService.getById(accountId);

        Assert.assertNotNull(oldAccount);

        final TransferResponse response = accountService.withdraw(oldAccount.getId(), count);
        Assert.assertTrue(response.isDone());

        final Account updatedAccount = accountService.getById(accountId);

        Assert.assertEquals(oldAccount.getBalance() - count, updatedAccount.getBalance().longValue());
    }

    @Test
    public void checkWithdrawNotEnoughMoney() throws SQLException {
        long accountId = 1L;
        final Account oldAccount = accountService.getById(accountId);
        final Long count = oldAccount.getBalance() + 100L;

        Assert.assertNotNull(oldAccount);

        final TransferResponse response = accountService.withdraw(oldAccount.getId(), count);
        Assert.assertFalse(response.isDone());
        Assert.assertEquals(ResponseError.NOT_ENOUGH_MONEY, response.getError());

        final Account updatedAccount = accountService.getById(accountId);

        Assert.assertEquals(oldAccount.getBalance(), updatedAccount.getBalance());
    }

    @Test
    public void checkPutMoney() throws SQLException {
        final Long count = 100L;
        long accountId = 1L;
        final Account oldAccount = accountService.getById(accountId);

        Assert.assertNotNull(oldAccount);

        final TransferResponse response = accountService.putMoney(oldAccount.getId(), count);
        Assert.assertTrue(response.isDone());

        final Account updatedAccount = accountService.getById(accountId);
        Assert.assertEquals(oldAccount.getBalance() + count, updatedAccount.getBalance().longValue());
    }

    private List<TransferResponse> fromFuturesToResponse(final List<Future<TransferResponse>> futures) {
        final ArrayList<TransferResponse> list = new ArrayList<>();
        for (final Future<TransferResponse> future : futures) {
            try {
                list.add(future.get());
            } catch (final InterruptedException | ExecutionException e) {
                System.out.println(e.getMessage());
            }
        }

        return list;
    }

}