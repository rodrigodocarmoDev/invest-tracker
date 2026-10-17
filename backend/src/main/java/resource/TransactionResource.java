package resource;

import dto.transaction.TransactionDTO;
import dto.transaction.TransactionForm;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.inject.Inject;
import service.TransactionService;

import java.util.List;
import java.util.UUID;

@Path("/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransactionResource {

    @Inject
    TransactionService transactionService;

    @GET
    @Path("/")
    public Response findAll() {
        try {
            List<TransactionDTO> transaction = transactionService.getAll();
            if (transaction.isEmpty()) {
                return Response.noContent().build();
            }
            return Response.ok(transaction).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error retrieving assets").build();
        }
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") UUID id) {
        try {
            TransactionDTO transactionDTO = transactionService.getById(id);
            return Response.ok(transactionDTO).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error retrieving transaction").build();
        }
    }

    @POST
    @Path("/")
    public Response create(TransactionForm transactionForm) {
        try {
            TransactionDTO transactionDTO = transactionService.create(transactionForm);
            return Response.ok(transactionDTO).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error creating transaction").build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, TransactionForm transactionForm) {
        try {
            TransactionDTO transactionDTO = transactionService.update(id, transactionForm);
            return Response.ok(transactionDTO).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error updating transaction").build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        try {
            transactionService.delete(id);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error deleting transaction").build();
        }
    }
}
