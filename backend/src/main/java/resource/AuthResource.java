package resource;

import dto.auth.AuthResponseDTO;
import dto.auth.LoginForm;
import dto.auth.MessageResponseDTO;
import dto.auth.RegisterForm;
import dto.user.UserDTO;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import mapper.UserMapper;
import model.User;
import org.eclipse.microprofile.jwt.JsonWebToken;
import repository.UserRepository;
import service.AuthService;

import java.util.UUID;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthService authService;

    @Inject
    UserRepository userRepository;

    @Inject
    UserMapper userMapper;

    @Inject
    JsonWebToken jwt;

    @POST
    @Path("/register")
    public Response register(@Valid RegisterForm form) {
        UserDTO userDTO = authService.register(form);
        return Response.status(Response.Status.CREATED).entity(userDTO).build();
    }

    @POST
    @Path("/login")
    public Response login(@Valid LoginForm form) {
        AuthResponseDTO authDTO = authService.login(form);
        return Response.ok(authDTO).build();
    }

    @GET
    @Path("/verify-email")
    public Response verifyEmail(@QueryParam("token") String token) {
        MessageResponseDTO response = authService.verifyEmail(token);
        return Response.ok(response).build();
    }

    @POST
    @Path("/resend-verification")
    public Response resendVerification(@QueryParam("email") String email) {
        MessageResponseDTO response = authService.resendVerificationEmail(email);
        return Response.ok(response).build();
    }

    @GET
    @Path("/me")
    @Authenticated
    public Response me() {
        String userIdStr = jwt.getSubject();
        if (userIdStr == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        UUID userId = UUID.fromString(userIdStr);
        User user = userRepository.findById(userId);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(userMapper.toDTO(user)).build();
    }
}
