package resource;

import dto.walletsnapshot.WalletSnapshotDTO;
import dto.walletsnapshot.WalletSnapshotForm;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import service.WalletSnapshotService;

import java.util.List;
import java.util.UUID;

@Path("/wallet-snapshots")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WalletSnapshotResource {

    @Inject
    WalletSnapshotService walletSnapshotService;

    @GET
    public Response findAll() {
        List<WalletSnapshotDTO> snapshots = walletSnapshotService.getAllWalletSnapshots();
        if (snapshots.isEmpty()) {
            return Response.noContent().build();
        }
        return Response.ok(snapshots).build();
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") UUID id) {
        WalletSnapshotDTO walletSnapshotDTO = walletSnapshotService.getById(id);
        return Response.ok(walletSnapshotDTO).build();
    }

    @POST
    public Response create(@Valid WalletSnapshotForm walletSnapshotForm) {
        WalletSnapshotDTO walletSnapshotDTO = walletSnapshotService.create(walletSnapshotForm);
        return Response.status(Response.Status.CREATED).entity(walletSnapshotDTO).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, @Valid WalletSnapshotForm walletSnapshotForm) {
        WalletSnapshotDTO walletSnapshotDTO = walletSnapshotService.update(id, walletSnapshotForm);
        return Response.ok(walletSnapshotDTO).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        walletSnapshotService.delete(id);
        return Response.noContent().build();
    }
}
