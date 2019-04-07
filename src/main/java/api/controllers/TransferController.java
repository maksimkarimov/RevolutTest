package api.controllers;

import api.models.Transfer;
import api.response.TransferResponse;
import api.service.TransferService;
import com.google.inject.Inject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.sql.SQLException;
import java.util.List;

@Path("transfer")
public class TransferController {

    private final TransferService transferService;

    @Inject
    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Transfer> getAll() throws SQLException {
        return transferService.getAll();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public TransferResponse makeTransfer(@FormParam("fromId") final Long fromId, @FormParam("toId") final Long toId,
                                         @FormParam("amount") final Long amount) throws SQLException {
        return transferService.makeTransfer(fromId, toId, amount);
    }

    @PUT
    public void update() {
        throw new NotSupportedException();
    }

    @DELETE
    public void delete() {
        throw new NotSupportedException();
    }
}
