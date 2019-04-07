package api.dao;

import api.exceptions.AccountNotFoundException;
import api.exceptions.NotEnoughMoneyException;
import api.exceptions.TransferErrorException;
import api.models.Account;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

public class AccountDao extends BasicDao<Account> {
    public AccountDao() {
        super(Account.class);
    }

    private final static String INSERT = "INSERT INTO account(name, balance) VALUES(?,?)";
    private final static String UPDATE_NAME = "UPDATE account set name = ? where id = ?";
    private final static String UPDATE_BALANCE = "UPDATE account set balance = ? where id = ?";

    @Override
    public Long save(final Account entity) throws SQLException {
        final QueryRunner run = new QueryRunner();

        return run.insert(dataSource.getConnection(), INSERT, new ScalarHandler<Integer>(),
                entity.getName(), entity.getBalance()).longValue();
    }

    @Override
    public Account update(final Account entity) throws SQLException {
        final QueryRunner run = new QueryRunner();
        run.update(dataSource.getConnection(), UPDATE_NAME, entity.getName(), entity.getId());

        return entity;
    }

    public void withdrawMoney(Long accountId, Long count)
            throws SQLException, AccountNotFoundException, NotEnoughMoneyException, TransferErrorException {
        final Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);

        try {
            final QueryRunner runner = new QueryRunner();
            final Account account = runner.query(connection, getSelectFromByIdForUpdateQuery(Account.class),
                    new BeanHandler<>(Account.class), accountId);

            if (account == null) {
                throw new AccountNotFoundException();
            }

            if (account.getBalance() < count) {
                throw new NotEnoughMoneyException();
            }
            runner.execute(connection, UPDATE_BALANCE, account.getBalance() - count, accountId);

            connection.commit();

        } catch (final AccountNotFoundException | NotEnoughMoneyException e) {
            connection.rollback();
            throw e;
        } catch (final Exception e) {
            connection.rollback();
            throw new TransferErrorException();
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public void putMoney(Long accountId, Long count)
            throws SQLException, AccountNotFoundException, TransferErrorException {
        final Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);

        try {
            final QueryRunner runner = new QueryRunner();
            final Account account = runner.query(connection, getSelectFromByIdForUpdateQuery(Account.class),
                    new BeanHandler<>(Account.class), accountId);

            if (account == null) {
                throw new AccountNotFoundException();
            }

            runner.execute(connection, UPDATE_BALANCE, account.getBalance() + count, accountId);

            connection.commit();

        } catch (final AccountNotFoundException e) {
            connection.rollback();
            throw e;
        } catch (final Exception e) {
            connection.rollback();
            throw new TransferErrorException();
        } finally {
            connection.setAutoCommit(true);
        }
    }


}
