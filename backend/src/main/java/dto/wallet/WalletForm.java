package dto.wallet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record WalletForm(
    @NotNull(message = "User ID is mandatory.")
    UUID userId,

    @NotBlank(message = "Wallet name is mandatory.")
    String name
) {}
