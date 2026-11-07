package resource;

import dto.wallet.WalletDTO;
import dto.wallet.WalletForm;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import service.WalletService;

import java.util.List;
import java.util.UUID;

@Path("/wallets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class WalletResource {

    @Inject
    WalletService walletService;

    @GET
    public Response findAll() {
        List<WalletDTO> wallets = walletService.getAllWallets();
        if (wallets.isEmpty()) {
            return Response.noContent().build();
        }
        return Response.ok(wallets).build();
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") UUID id) {
        WalletDTO walletDTO = walletService.getById(id);
        return Response.ok(walletDTO).build();
    }

    @POST
    public Response create(@Valid WalletForm walletForm) {
        WalletDTO walletDTO = walletService.create(walletForm);
        return Response.status(Response.Status.CREATED).entity(walletDTO).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, @Valid WalletForm walletForm) {
        WalletDTO walletDTO = walletService.update(id, walletForm);
        return Response.ok(walletDTO).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        walletService.delete(id);
        return Response.noContent().build();
    }
}
