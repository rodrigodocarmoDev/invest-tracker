package repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import model.User;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<User, UUID> {

    /**
     * E-mails são gravados sempre em minúsculas e sem espaços.
     * Normalizar também na busca garante que "Ana@Mail.com" e "ana@mail.com" sejam o mesmo usuário.
     */
    public Optional<User> findByEmail(String email) {
        return find("email", email.trim().toLowerCase()).firstResultOptional();
    }

    public Optional<User> findByVerificationToken(String token) {
        return find("verificationToken", token).firstResultOptional();
    }
}
