package api.controllers;

import api.GuiceModule;
import api.dao.TransferDao;
import api.models.Transfer;
import api.response.TransferResponse;
import com.google.inject.Guice;
import com.google.inject.Injector;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.sql.SQLException;
import java.util.List;

@Path("transfer")
public class TransferController {

    private TransferDao transferDao;

    public TransferController() {
        Injector injector = Guice.createInjector(new GuiceModule());
        transferDao = injector.getInstance(TransferDao.class);
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Transfer> getAll() throws SQLException {
        return transferDao.getAll();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public TransferResponse makeTransfer(@QueryParam("fromId") Long fromId, @QueryParam("toId") Long toId, @QueryParam("amount") Double amount) throws SQLException{
        return transferDao.makeTransfer(fromId, toId, amount);
    }
}
