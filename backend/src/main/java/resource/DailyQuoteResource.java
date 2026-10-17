package resource;

import dto.dailyquote.DailyQuoteDTO;
import dto.dailyquote.DailyQuoteForm;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import service.DailyQuoteService;

import java.util.List;
import java.util.UUID;

@Path("/daily-quotes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DailyQuoteResource {

    @Inject
    DailyQuoteService dailyQuoteService;

    @GET
    @Path("/")
    public Response findAll() {
        try {
            List<DailyQuoteDTO> quotes = dailyQuoteService.getAllQuotes();
            if (quotes.isEmpty()) {
                return Response.noContent().build();
            }
            return Response.ok(quotes).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error retrieving daily quotes").build();
        }
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") UUID id) {
        try {
           DailyQuoteDTO dailyQuoteDTO = dailyQuoteService.getById(id);
           return Response.ok(dailyQuoteDTO).build();
        } catch(Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error retrieving daily quotes").build();
        }
    }

    @POST
    @Path("/")
    public Response create(DailyQuoteForm quoteForm) {
        try {
           DailyQuoteDTO dailyQuoteDTO = dailyQuoteService.create(quoteForm);
           return Response.status(Response.Status.CREATED).entity(dailyQuoteDTO).build();
        } catch(Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error creating daily quotes").build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, DailyQuoteForm quoteForm) {
        try {
            DailyQuoteDTO dailyQuoteDTO = dailyQuoteService.update(id, quoteForm);
            return Response.ok(dailyQuoteDTO).build();
        } catch(Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error updating daily quotes").build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        try {
            dailyQuoteService.delete(id);
            return Response.noContent().build();
        } catch(Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error deleting daily quotes").build();
        }
    }
}
