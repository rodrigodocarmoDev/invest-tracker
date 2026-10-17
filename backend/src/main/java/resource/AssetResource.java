package resource;

import dto.asset.AssetDTO;
import dto.asset.AssetForm;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.inject.Inject;
import service.AssetService;

import java.util.List;
import java.util.UUID;

@Path("/assets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AssetResource {

    @Inject
    AssetService assetService;

    @GET
    @Path("/")
    public Response findAll() {
        try {
            List<AssetDTO> asset = assetService.getAllAssets();
            if (asset.isEmpty()) {
                return Response.noContent().build();
            }
        } catch(Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error retrieving assets").build();
        }
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") UUID id) {
        try {
            AssetDTO assetDTO = assetService.getById(id);
            return Response.ok(assetDTO).build();
        } catch(Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error retrieving daily quotes").build();
        }
    }

    @POST
    @Path("/")
    public Response create(AssetForm assetForm) {
        try {
            AssetDTO assetDTO = assetService.create(assetForm);
            return Response.status(Response.Status.CREATED).entity(assetDTO).build();
        } catch(Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error creating assets").build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, AssetForm assetForm) {
        try {
            AssetDTO assetDTO = assetService.update(id, assetForm);
            return Response.ok(assetDTO).build();
        } catch(Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error updating assets").build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        try {
            assetService.delete(id);
            return Response.noContent().build();
        } catch(Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error deleting daily quotes").build();
        }
    }
}
