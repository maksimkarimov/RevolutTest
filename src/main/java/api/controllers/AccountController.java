package api.controllers;

import api.models.Account;
import api.models.Transfer;
import api.response.TransferResponse;
import api.service.AccountService;
import com.google.inject.Inject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.sql.SQLException;
import java.util.List;

@Path("account")
public class AccountController {

    private final AccountService accountService;

    @Inject
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Account> getAll() throws SQLException {
        return accountService.getAll();
    }


    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Account getById(@PathParam("id") final Long id) throws SQLException {
        return accountService.getById(id);
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Account create(final Account account) throws SQLException {
        return accountService.save(account);
    }


    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Account update(final Account account) throws SQLException {
        return accountService.update(account);
    }

    @PUT
    @Path("/withdraw")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TransferResponse withdraw(@QueryParam("accountId") final Long accountId, @QueryParam("count") final Long count) {
        return accountService.withdraw(accountId, count);
    }

    @PUT
    @Path("/putMoney")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TransferResponse putMoney(@QueryParam("accountId") final Long accountId, @QueryParam("count") final Long count) {
        return accountService.putMoney(accountId, count);
    }
}
