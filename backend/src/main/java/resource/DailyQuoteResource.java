package resource;

import dto.dailyquote.DailyQuoteDTO;
import dto.dailyquote.DailyQuoteForm;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import security.Roles;
import dto.dailyquote.QuoteSyncResultDTO;
import service.DailyQuoteService;
import service.QuoteSyncService;

import java.util.List;
import java.util.UUID;

@Path("/daily-quotes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class DailyQuoteResource {

    @Inject
    DailyQuoteService dailyQuoteService;

    @Inject
    QuoteSyncService quoteSyncService;

    @GET
    public Response findAll() {
        List<DailyQuoteDTO> quotes = dailyQuoteService.getAllDailyQuotes();
        if (quotes.isEmpty()) {
            return Response.noContent().build();
        }
        return Response.ok(quotes).build();
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") UUID id) {
        DailyQuoteDTO dailyQuoteDTO = dailyQuoteService.getById(id);
        return Response.ok(dailyQuoteDTO).build();
    }

    @RolesAllowed(Roles.ADMIN)
    @POST
    public Response create(@Valid DailyQuoteForm quoteForm) {
        DailyQuoteDTO dailyQuoteDTO = dailyQuoteService.create(quoteForm);
        return Response.status(Response.Status.CREATED).entity(dailyQuoteDTO).build();
    }

    /**
     * Busca agora na brapi a cotação de todos os ativos cadastrados.
     * É a mesma rotina que o QuoteSyncJob executa automaticamente todo dia útil.
     */
    @RolesAllowed(Roles.ADMIN)
    @POST
    @Path("/sync")
    public Response sync() {
        QuoteSyncResultDTO result = quoteSyncService.syncAll();
        return Response.ok(result).build();
    }

    @RolesAllowed(Roles.ADMIN)
    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, @Valid DailyQuoteForm quoteForm) {
        DailyQuoteDTO dailyQuoteDTO = dailyQuoteService.update(id, quoteForm);
        return Response.ok(dailyQuoteDTO).build();
    }

    @RolesAllowed(Roles.ADMIN)
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        dailyQuoteService.delete(id);
        return Response.noContent().build();
    }
}
