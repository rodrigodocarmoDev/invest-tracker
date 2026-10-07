package resource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import model.DailyQuotes;

import java.util.UUID;

@Path("/daily-quotes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DailyQuoteResource {

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
    public Response create(DailyQuotes quote) {
        return Response.status(Response.Status.CREATED).entity(quote).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, DailyQuotes quote) {
        return Response.ok(quote).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        return Response.noContent().build();
    }
}
