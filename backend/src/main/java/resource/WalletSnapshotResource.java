package resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/wallet-snapshots")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WalletSnapshotResource {

    @Inject
    WalletSnapshotsService walletSnapshotService;

    @GET
    @Path("/")
    public Response findAll() {
        try {
            List<walletSnapshotDTO> walletSnapshot = walletSnapshotService.getAllwalletSnapshots();
            if (walletSnapshot.isEmpty()) {
                return Response.noContent().build();
            }
        } catch(Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error retrieving wallet snapshots").build();
        }
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") UUID id) {
        try {
            WalletSnapshotDTO walletSnapshotDTO = walletSnapshotService.getById(id);
            return Response.ok(walletSnapshotDTO).build();
        } catch(Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error retrieving wallet snapshot").build();
        }
    }

    @POST
    @Path("/")
    public Response create(WalletSnapshotsForm walletSnapshotForm) {
        try {
            WalletSnapshotDTO walletSnapshotDTO = walletSnapshotService.create(walletSnapshotForm);
            return Response.status(Response.Status.CREATED).entity(walletSnapshotDTO).build();
        } catch(Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error creating wallet snapshot").build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, WalletSnapshotsForm walletSnapshotForm) {
        try {
            WalletSnapshotDTO walletSnapshotDTO = walletSnapshotService.update(id, walletSnapshotForm);
            return Response.ok(walletSnapshotForm).build();
        } catch(Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error updating wallet snapshot").build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        try {
            WalletSnapshotDTO walletSnapshotDTO = walletSnapshotService.delete(id);
            return Response.noContent().build();
        } catch(Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error deleting wallet snapshot").build();
        }
    }
}
