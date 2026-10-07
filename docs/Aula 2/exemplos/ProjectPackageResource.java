package net.weg.capture.resource.project;

import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.weg.capture.constant.HTTPStatusCode;
import net.weg.capture.constant.UserRoles;
import net.weg.capture.constant.errors.GenericErrors;
import net.weg.capture.constant.errors.project.PackageErrors;
import net.weg.capture.dto.project.form.PackageForm;
import net.weg.capture.dto.project.projectpackage.MinifiedPackageDTO;
import net.weg.capture.dto.project.projectpackage.PackageDTO;
import net.weg.capture.exception.ErrorResponseFactory;
import net.weg.capture.service.project.projectpackage.ProjectPackageRelevantBusinessUnitService;
import net.weg.capture.service.project.projectpackage.ProjectPackageService;
import net.weg.capture.utils.CollectionUtils;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import java.util.List;

@Path("/api/project")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProjectPackageResource {

    @Inject
    public ProjectPackageService projectPackageService;

    @Inject
    public ProjectPackageRelevantBusinessUnitService projectPackageRelevantBusinessUnitService;

    @POST
    @Path("/{projectId}/package")
    // @RolesAllowed({ UserRoles.DEVELOPER, UserRoles.ADMIN, UserRoles.BDM, UserRoles.KEY_USER })
    @Authenticated
    @Operation(summary = "Create a new package", description = "Create a new package and return it with the generated id", operationId = "createPackage")
    @APIResponses(value = {
            @APIResponse(responseCode = HTTPStatusCode.CREATED, description = "Package created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PackageDTO.class))),
            @APIResponse(responseCode = HTTPStatusCode.BAD_REQUEST, description = GenericErrors.INVALID_DATA) })
    public Response createPackage(@PathParam("projectId") String projectId, @Valid PackageForm packageForm) {
        try {
            PackageDTO packageCreated = projectPackageService.create(projectId, packageForm);
            return Response.status(Response.Status.CREATED).entity(packageCreated).type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Exception e) {
            return ErrorResponseFactory.createErrorResponse(e);
        }
    }

    @POST
    @Path("/{projectId}/packages")
    @RolesAllowed({ UserRoles.DEVELOPER, UserRoles.ADMIN, UserRoles.BDM, UserRoles.KEY_USER })
    @Operation(summary = "Create new packages in batch", description = "Create new packages in batch and return them with the generated ids", operationId = "createPackages")
    @APIResponses(value = {
            @APIResponse(responseCode = HTTPStatusCode.CREATED, description = "Packages created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PackageDTO[].class))),
            @APIResponse(responseCode = HTTPStatusCode.BAD_REQUEST, description = GenericErrors.INVALID_DATA) })
    public Response createPackages(@PathParam("projectId") String projectId, List<@Valid PackageForm> packageForms) {
        if (CollectionUtils.isEmpty(packageForms)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(PackageErrors.PACKAGE_LIST_CANNOT_BE_EMPTY)
                    .type(MediaType.APPLICATION_JSON).build();
        }
        try {
            List<PackageDTO> packagesCreated = projectPackageService.createInBatch(projectId, packageForms);
            return Response.status(Response.Status.CREATED).entity(packagesCreated).type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Exception e) {
            return ErrorResponseFactory.createErrorResponse(e);
        }
    }

    @PUT
    @Path("/package/{packageId}")
    @RolesAllowed({ UserRoles.DEVELOPER, UserRoles.ADMIN, UserRoles.BDM, UserRoles.KEY_USER })
    @Operation(summary = "Update a package", description = "Update a package and return it", operationId = "updatePackage")
    @APIResponses(value = {
            @APIResponse(responseCode = HTTPStatusCode.OK, description = "Package updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PackageDTO.class))),
            @APIResponse(responseCode = HTTPStatusCode.BAD_REQUEST, description = PackageErrors.PACKAGE_NOT_FOUND),
            @APIResponse(responseCode = HTTPStatusCode.BAD_REQUEST, description = GenericErrors.INVALID_DATA) })
    public Response updatePackage(@PathParam("packageId") Long packageId, @Valid PackageForm packageForm) {
        try {
            PackageDTO packageUpdated = projectPackageService.update(packageId, packageForm);
            return Response.status(Response.Status.OK).entity(packageUpdated).type(MediaType.APPLICATION_JSON).build();
        } catch (Exception e) {
            return ErrorResponseFactory.createErrorResponse(e);
        }
    }

    @GET
    @Path("/package/{packageId}")
    // @RolesAllowed({ UserRoles.DEVELOPER, UserRoles.ADMIN, UserRoles.BDM, UserRoles.KEY_USER, UserRoles.USER })
    @Authenticated
    @Operation(summary = "Find a package by id", description = "Find a package by id and return it", operationId = "findPackageById")
    @APIResponses(value = {
            @APIResponse(responseCode = HTTPStatusCode.OK, description = "Package found successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PackageDTO.class))),
            @APIResponse(responseCode = HTTPStatusCode.BAD_REQUEST, description = PackageErrors.PACKAGE_NOT_FOUND),
            @APIResponse(responseCode = HTTPStatusCode.BAD_REQUEST, description = GenericErrors.INVALID_DATA) })
    public Response findPackageById(@PathParam("packageId") Long packageId) {
        try {
            PackageDTO packageFound = projectPackageService.findById(packageId);
            return Response.status(Response.Status.OK).entity(packageFound).type(MediaType.APPLICATION_JSON).build();
        } catch (Exception e) {
            return ErrorResponseFactory.createErrorResponse(e);
        }
    }

    @GET
    @Path("/package")
    @RolesAllowed({ UserRoles.DEVELOPER, UserRoles.ADMIN, UserRoles.BDM, UserRoles.KEY_USER, UserRoles.USER })
    @Operation(summary = "Find all packages", description = "Find all packages and return them", operationId = "findAllPackages")
    @APIResponses(value = {
            @APIResponse(responseCode = HTTPStatusCode.OK, description = "Packages found successfully", content = @Content(mediaType = "application/json", schema = @Schema(type = SchemaType.ARRAY, implementation = MinifiedPackageDTO.class))),
            @APIResponse(responseCode = HTTPStatusCode.NO_CONTENT, description = "No packages found") })
    public Response listAllPackages() {
        try {
            List<MinifiedPackageDTO> packagesFound = projectPackageService.listAll();
            if (packagesFound.isEmpty()) {
                return Response.status(Response.Status.NO_CONTENT).build();
            }
            return Response.status(Response.Status.OK).entity(packagesFound).type(MediaType.APPLICATION_JSON).build();
        } catch (Exception e) {
            return ErrorResponseFactory.createErrorResponse(e);
        }
    }

    @GET
    @Path("/{projectId}/package")
    @RolesAllowed({ UserRoles.DEVELOPER, UserRoles.ADMIN, UserRoles.BDM, UserRoles.KEY_USER, UserRoles.USER,
            UserRoles.SERVICE })
    @Operation(summary = "Find all packages by project", description = "Find all packages by project and return them", operationId = "findAllPackagesByProject")
    @APIResponses(value = {
            @APIResponse(responseCode = HTTPStatusCode.OK, description = "Packages found successfully", content = @Content(mediaType = "application/json", schema = @Schema(type = SchemaType.ARRAY, implementation = PackageDTO.class))),
            @APIResponse(responseCode = HTTPStatusCode.NO_CONTENT, description = "No packages found") })
    public Response findAllPackagesByProject(@PathParam("projectId") String projectId) {
        try {
            List<PackageDTO> packagesFound = projectPackageService.findAllByProjectIdWithQuoteCount(projectId);
            if (packagesFound.isEmpty()) {
                return Response.status(Response.Status.NO_CONTENT).build();
            }
            return Response.status(Response.Status.OK).entity(packagesFound).type(MediaType.APPLICATION_JSON).build();
        } catch (Exception e) {
            return ErrorResponseFactory.createErrorResponse(e);
        }
    }

    @DELETE
    @Path("/package/{packageId}")
    @RolesAllowed({ UserRoles.DEVELOPER, UserRoles.ADMIN, UserRoles.BDM, UserRoles.KEY_USER })
    @Operation(summary = "Delete a package", description = "Delete a package by id", operationId = "deletePackage")
    @APIResponses(value = {
            @APIResponse(responseCode = HTTPStatusCode.NO_CONTENT, description = "Package deleted successfully"),
            @APIResponse(responseCode = HTTPStatusCode.BAD_REQUEST, description = PackageErrors.PACKAGE_NOT_FOUND),
            @APIResponse(responseCode = HTTPStatusCode.BAD_REQUEST, description = GenericErrors.INVALID_DATA) })
    public Response deletePackage(@PathParam("packageId") Long packageId) {
        try {
            projectPackageService.delete(packageId);
            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (Exception e) {
            return ErrorResponseFactory.createErrorResponse(e);
        }
    }
}
