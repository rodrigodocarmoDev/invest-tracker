package dto.wallet;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public record WalletForm (
        @NotBlank(message="User is mandatory.")
        String user,
        @NotBlank(message="Name is mandatory.")
        String name,
        @NotBlank(message="")
        LocalDateTime createAt
) {}
