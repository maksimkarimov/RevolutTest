package api.controllers;

import api.GuiceModule;
import api.dao.TransferDao;
import api.models.Account;
import com.google.inject.Guice;
import com.google.inject.Injector;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import java.sql.SQLException;
import java.util.List;

@Path("test")
public class TestController {

    private TransferDao transferDao;

    public TestController() {
        Injector injector = Guice.createInjector(new GuiceModule());
        transferDao = injector.getInstance(TransferDao.class);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String doGet(@Context Request req) throws SQLException {
        //transferDao.makeTransfer(1L, 2L, 500d);
        return "test";
    }

}
