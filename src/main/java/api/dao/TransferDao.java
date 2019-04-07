package api.dao;

import api.exceptions.AccountNotFoundException;
import api.exceptions.NotEnoughMoneyException;
import api.exceptions.TransferErrorException;
import api.models.Account;
import api.models.Transfer;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

public class TransferDao extends BasicDao<Transfer> {

    private static final String UPDATE_ACCOUNT_BALANCE_BY_ID = "update account set balance = ? where id = ?";
    private static final String INSERT_TRANSFER = "insert into transfer(fromAccountId, toAccountId, amount, date) " +
            "values(?,?,?,?)";

    public TransferDao() {
        super(Transfer.class);
    }

    public synchronized Transfer makeTransfer(final Long fromId, final Long toId, final Long amount)
            throws SQLException, TransferErrorException, AccountNotFoundException, NotEnoughMoneyException {

        final Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);

        try {
            final String SELECT_FROM_ACCOUNT_BY_ID_FOR_UPDATE = getSelectFromByIdForUpdateQuery(Account.class);
            final QueryRunner runner = new QueryRunner();
            final Account from = runner.query(connection, SELECT_FROM_ACCOUNT_BY_ID_FOR_UPDATE,
                    new BeanHandler<>(Account.class), fromId);
            final Account to = runner.query(connection, SELECT_FROM_ACCOUNT_BY_ID_FOR_UPDATE,
                    new BeanHandler<>(Account.class), toId);

            validateAccountsForTransfer(from, to, amount);

            runner.execute(connection, UPDATE_ACCOUNT_BALANCE_BY_ID, from.getBalance() - amount, fromId);
            runner.execute(connection, UPDATE_ACCOUNT_BALANCE_BY_ID, to.getBalance() + amount, toId);

            runner.execute(connection, INSERT_TRANSFER, fromId, toId, amount, new Date());
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

        return null;
    }


    @Override
    public Long save(final Transfer entity) throws SQLException {
        return null;
    }

    @Override
    public Transfer update(final Transfer entity) {
        return null;
    }

    @Override
    public Integer delete(final Long id) throws SQLException {
        return null;
    }


    private void validateAccountsForTransfer(final Account from, final Account to, final Long amount)
            throws NotEnoughMoneyException, AccountNotFoundException {
        if (from == null || to == null) {
            throw new AccountNotFoundException();
        }

        if (from.getBalance() < amount) {
            throw new NotEnoughMoneyException();
        }
    }
}
