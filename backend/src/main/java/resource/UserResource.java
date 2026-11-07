package resource;

import dto.user.RoleForm;
import dto.user.UserDTO;
import dto.user.UserForm;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import security.Roles;
import service.UserService;

import java.util.List;
import java.util.UUID;

/**
 * @Authenticated na classe: toda rota exige login.
 * @RolesAllowed no método: sobrescreve a regra da classe para rotas exclusivas de ADMIN.
 * A verificação "é o próprio usuário ou ADMIN?" fica no UserService, pois depende do ID pedido.
 */
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class UserResource {

    @Inject
    UserService userService;

    @GET
    @RolesAllowed(Roles.ADMIN)
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
    @RolesAllowed(Roles.ADMIN)
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

    @PUT
    @Path("/{id}/role")
    @RolesAllowed(Roles.ADMIN)
    public Response updateRole(@PathParam("id") UUID id, @Valid RoleForm roleForm) {
        UserDTO userDTO = userService.updateRole(id, roleForm);
        return Response.ok(userDTO).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        userService.delete(id);
        return Response.noContent().build();
    }
}
