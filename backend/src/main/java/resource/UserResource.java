package resource;

import dto.user.UserDTO;
import dto.user.UserForm;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import service.UserService;

import java.util.List;
import java.util.UUID;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserService userService;

    @GET
    @Path("/")
    public Response findAll() {
        try {
            List<UserDTO> user = userService.getAllUsers();
            if (user.isEmpty()) {
                return Response.noContent().build();
            }
            return Response.ok(user).build();
        } catch(Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error retrieving users").build();
        }
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") UUID id) {
        try {
            UserDTO userDTO = userService.getById(id);
            return Response.ok(userDTO).build();
        } catch(Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error retrieving user").build();
        }
    }

    @POST
    @Path("/")
    public Response create(UserForm userForm) {
        try {
            UserDTO userDTO = userService.create(userForm);
            return Response.status(Response.Status.CREATED).entity(userDTO).build();
        } catch(Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error creating user").build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, UserForm userForm) {
        try {
            UserDTO userDTO = userService.update(id, userForm);
            return Response.ok(userDTO).build();
        } catch(Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error updating user").build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        try {
            userService.delete(id);
            return Response.noContent().build();
        } catch(Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error deleting user").build();
        }
    }
}
