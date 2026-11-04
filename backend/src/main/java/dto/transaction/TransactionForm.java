package dto.transaction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record TransactionForm(
    @NotNull(message = "Wallet ID is mandatory.")
    UUID walletId,

    @NotNull(message = "Asset ID is mandatory.")
    UUID assetId,

    @NotBlank(message = "Operation type is mandatory.")
    @Pattern(regexp = "(?i)^(BUY|SELL)$", message = "Operation type must be BUY or SELL.")
    String operationType,

    @NotNull(message = "Quantity is mandatory.")
    @Positive(message = "Quantity must be greater than zero.")
    BigDecimal quantity,

    @NotNull(message = "Unit price is mandatory.")
    @Positive(message = "Unit price must be greater than zero.")
    BigDecimal unitPrice,

    BigDecimal fees
) {}
