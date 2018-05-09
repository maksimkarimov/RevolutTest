package api.controllers;

import api.GuiceModule;
import api.dao.AccountDao;
import api.models.Account;
import com.google.inject.Guice;
import com.google.inject.Injector;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.sql.SQLException;
import java.util.List;

@Path("account")
public class AccountController {

    private AccountDao accountDao;

    public AccountController() {
        Injector injector = Guice.createInjector(new GuiceModule());
        accountDao = injector.getInstance(AccountDao.class);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Account> getAll() throws SQLException {
        return accountDao.getAll();
    }


    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Account getById(@PathParam("id") Long id) throws SQLException {
        return accountDao.getById(id);
    }
}
