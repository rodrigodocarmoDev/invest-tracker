package security;

import exception.custom.AccessDeniedException;
import io.quarkus.security.UnauthorizedException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.UUID;

/**
 * Representa o usuário autenticado na requisição atual.
 *
 * É @RequestScoped: cada requisição HTTP recebe a sua própria instância, montada a partir
 * do JWT enviado no header "Authorization: Bearer <token>".
 *
 * Regra de ouro: a identidade de quem faz a requisição vem SEMPRE do token (assinado pelo servidor),
 * nunca de um campo enviado no corpo da requisição — esse o cliente pode forjar à vontade.
 */
@RequestScoped
public class CurrentUser {

    @Inject
    JsonWebToken jwt;

    /** ID do usuário logado (claim "sub" do token). */
    public UUID id() {
        String subject = jwt.getSubject();
        if (subject == null) {
            throw new UnauthorizedException("Authentication is required.");
        }
        return UUID.fromString(subject);
    }

    public boolean isAdmin() {
        return jwt.getGroups() != null && jwt.getGroups().contains(Roles.ADMIN);
    }

    /**
     * Garante que o usuário logado é o dono do recurso — ou um administrador.
     *
     * @param ownerId ID do usuário dono do recurso (ex: wallet.users.id)
     */
    public void checkAccess(UUID ownerId) {
        if (!isAdmin() && !id().equals(ownerId)) {
            throw new AccessDeniedException("You do not have permission to access this resource.");
        }
    }

    /** Garante que o usuário logado é administrador, para regras que dependem do conteúdo da requisição. */
    public void requireAdmin(String message) {
        if (!isAdmin()) {
            throw new AccessDeniedException(message);
        }
    }
}
