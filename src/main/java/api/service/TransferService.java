package api.service;

import api.dao.TransferDao;
import api.enums.ResponseError;
import api.exceptions.AccountNotFoundException;
import api.exceptions.NotEnoughMoneyException;
import api.exceptions.TransferErrorException;
import api.models.Transfer;
import api.response.TransferResponse;
import com.google.inject.Inject;

import java.sql.SQLException;
import java.util.List;

public class TransferService {
    private final TransferDao transferDao;

    @Inject
    public TransferService(TransferDao transferDao) {
        this.transferDao = transferDao;
    }

    public Transfer getById(final Long id) throws SQLException {
        return transferDao.getById(id);
    }

    public synchronized TransferResponse makeTransfer(final Long fromId, final Long toId, final Long amount)
            throws SQLException {
        if (fromId == null || toId == null || amount == null) {
            return new TransferResponse(false, ResponseError.INSUFFICIENT_DATA);
        }

        if (amount <= 0) {
            return new TransferResponse(false, ResponseError.TRANSFER_LESS_THEN_ZERO);
        }

        if (fromId.equals(toId)) {
            return new TransferResponse(false, ResponseError.TRANSFER_ACCOUNTS_EQUALS);
        }

        try {
            transferDao.makeTransfer(fromId, toId, amount);
        } catch (TransferErrorException e) {
            return new TransferResponse(false, ResponseError.TRANSACTION_ROLLEDBACK);
        } catch (AccountNotFoundException e) {
            return new TransferResponse(false, ResponseError.ACCOUNT_NOT_FOUND);
        } catch (NotEnoughMoneyException e) {
            return new TransferResponse(false, ResponseError.NOT_ENOUGH_MONEY);
        }

        return new TransferResponse(true, null);
    }

    public List<Transfer> getAll() throws SQLException {
        return transferDao.getAll();
    }
}
