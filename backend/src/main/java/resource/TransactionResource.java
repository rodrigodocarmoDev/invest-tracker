package resource;

import dto.transaction.TransactionDTO;
import dto.transaction.TransactionForm;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import service.TransactionService;

import java.util.List;
import java.util.UUID;

@Path("/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class TransactionResource {

    @Inject
    TransactionService transactionService;

    @GET
    public Response findAll() {
        List<TransactionDTO> transactions = transactionService.getAllTransactions();
        if (transactions.isEmpty()) {
            return Response.noContent().build();
        }
        return Response.ok(transactions).build();
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") UUID id) {
        TransactionDTO transactionDTO = transactionService.getById(id);
        return Response.ok(transactionDTO).build();
    }

    @POST
    public Response create(@Valid TransactionForm transactionForm) {
        TransactionDTO transactionDTO = transactionService.create(transactionForm);
        return Response.status(Response.Status.CREATED).entity(transactionDTO).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, @Valid TransactionForm transactionForm) {
        TransactionDTO transactionDTO = transactionService.update(id, transactionForm);
        return Response.ok(transactionDTO).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        transactionService.delete(id);
        return Response.noContent().build();
    }
}
