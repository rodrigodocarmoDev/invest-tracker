package resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import model.Wallets;

import java.util.List;
import java.util.UUID;

@Path("/wallets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WalletResource {

    @Inject
    WalletService walletService;

    @GET
    @Path("/")
    public Response findAll() {
        try {
            List<WalletDTO> user = walletService.getAllWallets();
            if (wallet.isEmpty()) {
                return Response.noContent().build();
            }
        } catch(Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error retrieving wallets").build();
        }
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") UUID id) {
        try {
            WalletDTO walletDTO = walletService.getById(id);
            return Response.ok(walletDTO).build();
        } catch(Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error retrieving wallet").build();
        }
    }

    @POST
    @Path("/")
    public Response create(WalletsForm walletForm) {
        try {
            WalletDTO walletDTO = walletService.create(walletForm);
            return Response.status(Response.Status.CREATED).entity(walletDTO).build();
        } catch(Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error creating wallet").build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, WalletsForm walletForm) {
        try {
            WalletDTO walletDTO = walletService.update(id, walletForm);
            return Response.ok(walletForm).build();
        } catch(Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error updating wallet").build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        try {
            WalletDTO walletDTO = walletService.delete(id);
            return Response.noContent().build();
        } catch(Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error deleting wallet").build();
        }
    }
}
