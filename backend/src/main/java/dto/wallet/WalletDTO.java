package dto.wallet;

import java.time.LocalDateTime;
import java.util.UUID;

public record WalletDTO(
    UUID id,
    UUID userId,
    String name,
    LocalDateTime createdAt
) {}
