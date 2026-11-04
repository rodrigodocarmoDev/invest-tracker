package dto.user;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDTO(
    UUID id,
    String email,
    String name,
    boolean emailVerified,
    String role,
    LocalDateTime createdAt
) {}
