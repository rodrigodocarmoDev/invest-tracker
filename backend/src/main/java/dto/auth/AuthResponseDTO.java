package dto.auth;

import dto.user.UserDTO;

public record AuthResponseDTO(
    String accessToken,
    String tokenType,
    long expiresInSeconds,
    UserDTO user
) {
    public AuthResponseDTO(String accessToken, long expiresInSeconds, UserDTO user) {
        this(accessToken, "Bearer", expiresInSeconds, user);
    }
}
