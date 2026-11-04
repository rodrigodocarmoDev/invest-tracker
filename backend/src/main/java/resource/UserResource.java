package resource;

import dto.user.UserDTO;
import dto.user.UserForm;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
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
    public Response findAll() {
        List<UserDTO> users = userService.getAllUsers();
        if (users.isEmpty()) {
            return Response.noContent().build();
        }
        return Response.ok(users).build();
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") UUID id) {
        UserDTO userDTO = userService.getById(id);
        return Response.ok(userDTO).build();
    }

    @POST
    public Response create(@Valid UserForm userForm) {
        UserDTO userDTO = userService.create(userForm);
        return Response.status(Response.Status.CREATED).entity(userDTO).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, @Valid UserForm userForm) {
        UserDTO userDTO = userService.update(id, userForm);
        return Response.ok(userDTO).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        userService.delete(id);
        return Response.noContent().build();
    }
}
