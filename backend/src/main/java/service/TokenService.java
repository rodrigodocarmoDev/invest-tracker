package service;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import model.User;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.Set;

@ApplicationScoped
public class TokenService {

    @ConfigProperty(name = "mp.jwt.verify.issuer", defaultValue = "https://invest-tracker.com")
    String issuer;

    public String generateToken(User user) {
        return Jwt.issuer(issuer)
                .upn(user.email)
                .subject(user.id.toString())
                .groups(Set.of(user.role))
                .claim("userId", user.id.toString())
                .claim("email", user.email)
                .claim("name", user.name != null ? user.name : user.email)
                .expiresIn(Duration.ofHours(24))
                .sign();
    }

    public long getExpirationTimeInSeconds() {
        return Duration.ofHours(24).toSeconds();
    }
}
