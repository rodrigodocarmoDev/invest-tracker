package dto.wallet;

import model.User;

import java.time.LocalDateTime;
import java.util.UUID;

public record WalletDTO (
        UUID id,
        User user,
        String name,
        LocalDateTime createdAt
) {}
