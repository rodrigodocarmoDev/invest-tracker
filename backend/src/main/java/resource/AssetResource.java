package resource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import model.Assets;

import java.util.UUID;

@Path("/assets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AssetResource {

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
    public Response create(Assets asset) {
        return Response.status(Response.Status.CREATED).entity(asset).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, Assets asset) {
        return Response.ok(asset).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        return Response.noContent().build();
    }
}
