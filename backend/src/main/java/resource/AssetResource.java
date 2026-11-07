package resource;

import dto.asset.AssetDTO;
import dto.asset.AssetForm;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import security.Roles;
import service.AssetService;

import java.util.List;
import java.util.UUID;

@Path("/assets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class AssetResource {

    @Inject
    AssetService assetService;

    @GET
    public Response findAll() {
        List<AssetDTO> assets = assetService.getAllAssets();
        if (assets.isEmpty()) {
            return Response.noContent().build();
        }
        return Response.ok(assets).build();
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") UUID id) {
        AssetDTO assetDTO = assetService.getById(id);
        return Response.ok(assetDTO).build();
    }

    @RolesAllowed(Roles.ADMIN)
    @POST
    public Response create(@Valid AssetForm assetForm) {
        AssetDTO assetDTO = assetService.create(assetForm);
        return Response.status(Response.Status.CREATED).entity(assetDTO).build();
    }

    @RolesAllowed(Roles.ADMIN)
    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, @Valid AssetForm assetForm) {
        AssetDTO assetDTO = assetService.update(id, assetForm);
        return Response.ok(assetDTO).build();
    }

    @RolesAllowed(Roles.ADMIN)
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        assetService.delete(id);
        return Response.noContent().build();
    }
}
