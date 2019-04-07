package api.service;

import api.dao.AccountDao;
import api.enums.ResponseError;
import api.exceptions.AccountNotFoundException;
import api.exceptions.NotEnoughMoneyException;
import api.models.Account;
import api.response.TransferResponse;
import com.google.inject.Inject;

import java.sql.SQLException;
import java.util.List;

public class AccountService {

    private final AccountDao accountDao;

    @Inject
    public AccountService(final AccountDao accountDao) {
        this.accountDao = accountDao;
    }


    public List<Account> getAll() throws SQLException {
        return accountDao.getAll();
    }

    public Account save(final Account account) throws SQLException {
        final Long id = accountDao.save(account);
        return accountDao.getById(id);
    }

    public Account getById(final Long id) throws SQLException {
        return accountDao.getById(id);
    }

    public Account update(final Account account) throws SQLException {
        return accountDao.update(account);
    }

    public void delete(final Long id) throws SQLException {
        accountDao.delete(id);
    }

    public TransferResponse withdraw(Long accountId, Long count) {
        try {
            accountDao.withdrawMoney(accountId, count);
        } catch (AccountNotFoundException e) {
            return new TransferResponse(false, ResponseError.ACCOUNT_NOT_FOUND);
        } catch (NotEnoughMoneyException e) {
            return new TransferResponse(false, ResponseError.NOT_ENOUGH_MONEY);
        } catch (Exception e) {
            return new TransferResponse(false, ResponseError.TRANSACTION_ROLLEDBACK);
        }

        return new TransferResponse(true, null);
    }


    public TransferResponse putMoney(Long accountId, Long count) {
        try {
            accountDao.putMoney(accountId, count);
        } catch (AccountNotFoundException e) {
            return new TransferResponse(false, ResponseError.ACCOUNT_NOT_FOUND);
        } catch (Exception e) {
            return new TransferResponse(false, ResponseError.TRANSACTION_ROLLEDBACK);
        }

        return new TransferResponse(true, null);
    }
}
