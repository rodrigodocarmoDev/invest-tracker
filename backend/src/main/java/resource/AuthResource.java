package resource;

import dto.auth.AuthResponseDTO;
import dto.auth.LoginForm;
import dto.auth.MessageResponseDTO;
import dto.auth.RegisterForm;
import dto.user.UserDTO;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import security.CurrentUser;
import service.AuthService;
import service.UserService;

/**
 * Endpoints públicos de autenticação.
 *
 * Como a aplicação nega por padrão qualquer endpoint sem anotação de segurança
 * (quarkus.security.jaxrs.deny-unannotated-endpoints=true), as rotas abertas
 * precisam ser liberadas EXPLICITAMENTE com @PermitAll.
 */
@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthService authService;

    @Inject
    UserService userService;

    @Inject
    CurrentUser currentUser;

    @POST
    @Path("/register")
    @PermitAll
    public Response register(@Valid RegisterForm form) {
        UserDTO userDTO = authService.register(form);
        return Response.status(Response.Status.CREATED).entity(userDTO).build();
    }

    @POST
    @Path("/login")
    @PermitAll
    public Response login(@Valid LoginForm form) {
        AuthResponseDTO authDTO = authService.login(form);
        return Response.ok(authDTO).build();
    }

    @GET
    @Path("/verify-email")
    @PermitAll
    public Response verifyEmail(@QueryParam("token") String token) {
        MessageResponseDTO response = authService.verifyEmail(token);
        return Response.ok(response).build();
    }

    @POST
    @Path("/resend-verification")
    @PermitAll
    public Response resendVerification(@QueryParam("email") String email) {
        MessageResponseDTO response = authService.resendVerificationEmail(email);
        return Response.ok(response).build();
    }

    @GET
    @Path("/me")
    @Authenticated
    public Response me() {
        return Response.ok(userService.getById(currentUser.id())).build();
    }
}
