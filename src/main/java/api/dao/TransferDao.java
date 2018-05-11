package api.dao;

import api.enums.ResponseError;
import api.models.Account;
import api.models.Transfer;
import api.response.TransferResponse;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

public class TransferDao extends BasicDao<Transfer>{
    public TransferDao() {
        super(Transfer.class);
    }

    public TransferResponse makeTransfer(Long fromId, Long toId, Double amount) throws SQLException {
        Connection connection = dataSource.getConnection();
        QueryRunner runner = new QueryRunner();
        connection.setAutoCommit(false);

        if (fromId == null || toId == null || amount == null) {
            return new TransferResponse(false, ResponseError.INSUFFICIENT_DATA);
        }

        if (fromId.equals(toId)) {
            return new TransferResponse(false, ResponseError.TRANSFER_ACCOUNTS_EQUALS);
        }

        try {
            String sql = "select * from account where id = ? FOR UPDATE";
            Account from = runner.query(connection, sql, new BeanHandler<>(Account.class), fromId);
            Account to = runner.query(connection, sql, new BeanHandler<>(Account.class), toId);

            if (from == null || to == null) {
                return new TransferResponse(false, ResponseError.ACCOUNT_NOT_FOUND);
            }

            if (from.getBalance() < amount) {
                return new TransferResponse(false, ResponseError.NOT_ENOUGH_MONEY);
            }

            sql = "update account set balance = ? where id = ?";
            runner.execute(connection, sql, from.getBalance() - amount, fromId);
            runner.execute(connection, sql, to.getBalance() + amount, toId);

            sql = "insert into transfer(fromAccountId, toAccountId, amount, date) values(?,?,?,?)";
            runner.execute(connection, sql, fromId, toId, amount, new Date());
            connection.commit();

        }catch(Exception e) {
            connection.rollback();
            return new TransferResponse(false, ResponseError.TRANSACTION_ROLLEDBACK);
        }finally {
            connection.setAutoCommit(true);
        }

        return new TransferResponse(true, null);
    }
}
