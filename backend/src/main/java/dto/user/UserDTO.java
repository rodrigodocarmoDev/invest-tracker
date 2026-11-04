package dto.user;

import java.util.UUID;

public record UserDTO (
        UUID id,
        String ticker,
        String type,
        String name
) {}
