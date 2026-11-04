package model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(nullable = false, unique = true)
    public String email;

    @Column(name = "name")
    public String name;

    @Column(name = "password_hash", nullable = false)
    public String passwordHash;

    @Column(name = "email_verified", nullable = false)
    public boolean emailVerified = false;

    @Column(name = "verification_token")
    public String verificationToken;

    @Column(name = "verification_token_expires_at")
    public LocalDateTime verificationTokenExpiresAt;

    @Column(name = "role", nullable = false)
    public String role = "USER";

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt = LocalDateTime.now();
}
