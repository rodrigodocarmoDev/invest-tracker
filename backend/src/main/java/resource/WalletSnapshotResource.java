package resource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import model.WalletSnapshots;

import java.util.UUID;

@Path("/wallet-snapshots")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WalletSnapshotResource {

    @GET
    public Response findAll() {
        return Response.ok().build();
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") UUID id) {
        return Response.ok().build();
    }

    @POST
    public Response create(WalletSnapshots snapshot) {
        return Response.status(Response.Status.CREATED).entity(snapshot).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, WalletSnapshots snapshot) {
        return Response.ok(snapshot).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        return Response.noContent().build();
    }
}
