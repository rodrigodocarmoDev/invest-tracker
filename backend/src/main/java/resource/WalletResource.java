package resource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import model.Wallets;

import java.util.UUID;

@Path("/wallets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WalletResource {

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
    public Response create(Wallets wallet) {
        return Response.status(Response.Status.CREATED).entity(wallet).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, Wallets wallet) {
        return Response.ok(wallet).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        return Response.noContent().build();
    }
}
