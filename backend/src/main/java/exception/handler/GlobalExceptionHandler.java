package exception.handler;

import exception.custom.AccessDeniedException;
import exception.custom.BusinessException;
import exception.custom.ConflictException;
import exception.custom.ResourceNotFoundException;
import exception.dto.ErrorResponseDTO;
import exception.dto.ValidationErrorDTO;
import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.UnauthorizedException;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Interceptador Global de Exceções do Quarkus REST.
 * Como funciona:
 * O Quarkus escaneia esta classe e usa a anotação @ServerExceptionMapper para interceptar
 * exceções lançadas nos Services ou Resources. O roteamento é automático com base no
 * TIPO DO PRIMEIRO PARÂMETRO de cada método (ex: ResourceNotFoundException -> handleNotFound).
 * Isso elimina a necessidade de blocos try/catch manuais nos controllers.
 */
public class GlobalExceptionHandler {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionHandler.class);

    /**
     * Intercepta quando um registro não foi encontrado por ID ou busca (HTTP 404).
     */
    @ServerExceptionMapper
    public Response handleNotFound(ResourceNotFoundException ex, @Context UriInfo uriInfo) {
        ErrorResponseDTO error = new ErrorResponseDTO(
            Response.Status.NOT_FOUND.getStatusCode(),
            "Not Found",
            ex.getMessage(),
            uriInfo != null ? uriInfo.getPath() : null
        );
        return Response.status(Response.Status.NOT_FOUND).entity(error).build();
    }

    /**
     * Intercepta conflitos de unicidade (e-mail/ticker) ou integridade relacional ao deletar (HTTP 409).
     */
    @ServerExceptionMapper
    public Response handleConflict(ConflictException ex, @Context UriInfo uriInfo) {
        ErrorResponseDTO error = new ErrorResponseDTO(
            Response.Status.CONFLICT.getStatusCode(),
            "Conflict",
            ex.getMessage(),
            uriInfo != null ? uriInfo.getPath() : null
        );
        return Response.status(Response.Status.CONFLICT).entity(error).build();
    }

    /**
     * Intercepta quebras de regras de negócio disparadas pela aplicação (HTTP 400).
     */
    @ServerExceptionMapper
    public Response handleBusiness(BusinessException ex, @Context UriInfo uriInfo) {
        ErrorResponseDTO error = new ErrorResponseDTO(
            Response.Status.BAD_REQUEST.getStatusCode(),
            "Bad Request",
            ex.getMessage(),
            uriInfo != null ? uriInfo.getPath() : null
        );
        return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
    }

    /**
     * Intercepta falhas do Bean Validation (@Valid nos Forms) e mapeia os campos inválidos (HTTP 400).
     */
    @ServerExceptionMapper
    public Response handleValidation(ConstraintViolationException ex, @Context UriInfo uriInfo) {
        List<ValidationErrorDTO> fieldErrors = ex.getConstraintViolations().stream()
            .map(cv -> {
                String fullPath = cv.getPropertyPath().toString();
                String field = fullPath.substring(fullPath.lastIndexOf('.') + 1);
                return new ValidationErrorDTO(field, cv.getMessage());
            })
            .collect(Collectors.toList());

        ErrorResponseDTO error = new ErrorResponseDTO(
            Response.Status.BAD_REQUEST.getStatusCode(),
            "Validation Error",
            "Validation error on submitted fields",
            uriInfo != null ? uriInfo.getPath() : null,
            LocalDateTime.now(),
            fieldErrors
        );
        return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
    }

    /**
     * Mantém o status original de exceções padrão do JAX-RS / Quarkus REST (ex: 405 Method Not Allowed).
     */
    @ServerExceptionMapper
    public Response handleWebApplicationException(WebApplicationException ex, @Context UriInfo uriInfo) {
        ErrorResponseDTO error = new ErrorResponseDTO(
            ex.getResponse().getStatus(),
            ex.getResponse().getStatusInfo().getReasonPhrase(),
            ex.getMessage(),
            uriInfo != null ? uriInfo.getPath() : null
        );
        return Response.status(ex.getResponse().getStatus()).entity(error).build();
    }

    /**
     * Intercepta quando o usuário está logado, mas tenta acessar um recurso que não é dele (HTTP 403).
     */
    @ServerExceptionMapper
    public Response handleAccessDenied(AccessDeniedException ex, @Context UriInfo uriInfo) {
        return forbidden(ex.getMessage(), uriInfo);
    }

    /**
     * Intercepta o bloqueio do próprio Quarkus quando o usuário não tem a role exigida
     * por @RolesAllowed (ex: usuário comum chamando um endpoint só de ADMIN) (HTTP 403).
     */
    @ServerExceptionMapper
    public Response handleForbidden(ForbiddenException ex, @Context UriInfo uriInfo) {
        return forbidden("You do not have permission to access this resource.", uriInfo);
    }

    /**
     * Intercepta requisições sem token, com token inválido, adulterado ou expirado (HTTP 401).
     * O header WWW-Authenticate informa ao cliente qual esquema de autenticação usar.
     */
    @ServerExceptionMapper({UnauthorizedException.class, AuthenticationFailedException.class})
    public Response handleUnauthorized(RuntimeException ex, @Context UriInfo uriInfo) {
        ErrorResponseDTO error = new ErrorResponseDTO(
            Response.Status.UNAUTHORIZED.getStatusCode(),
            "Unauthorized",
            "Authentication is required. Send a valid token in the header 'Authorization: Bearer <token>'.",
            uriInfo != null ? uriInfo.getPath() : null
        );
        return Response.status(Response.Status.UNAUTHORIZED)
            .header("WWW-Authenticate", "Bearer")
            .entity(error)
            .build();
    }

    /**
     * Rede de segurança universal (Fallback): Captura qualquer erro não previsto da JVM,
     * falhas de banco de dados ou do Hibernate, retornando HTTP 500.
     *
     * O detalhe do erro vai para o LOG do servidor, nunca para o cliente: mensagens internas
     * (SQL, nomes de tabelas, constraints) ajudam um atacante a mapear o sistema.
     */
    @ServerExceptionMapper
    public Response handleGeneric(Throwable ex, @Context UriInfo uriInfo) {
        String path = uriInfo != null ? uriInfo.getPath() : null;
        LOG.errorf(ex, "Unexpected error on path %s", path);

        ErrorResponseDTO error = new ErrorResponseDTO(
            Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
            "Internal Server Error",
            "An unexpected internal error occurred.",
            path
        );
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
    }

    private Response forbidden(String message, UriInfo uriInfo) {
        ErrorResponseDTO error = new ErrorResponseDTO(
            Response.Status.FORBIDDEN.getStatusCode(),
            "Forbidden",
            message,
            uriInfo != null ? uriInfo.getPath() : null
        );
        return Response.status(Response.Status.FORBIDDEN).entity(error).build();
    }
}
