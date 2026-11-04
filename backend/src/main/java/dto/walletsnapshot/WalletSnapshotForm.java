package dto.walletsnapshot;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record WalletSnapshotForm(
    @NotNull(message = "Wallet ID is mandatory.")
    UUID walletId,

    @NotNull(message = "Snapshot date is mandatory.")
    LocalDate snapshotDate,

    @NotNull(message = "Total value is mandatory.")
    @PositiveOrZero(message = "Total value must be positive or zero.")
    BigDecimal totalValue,

    @NotNull(message = "Total invested is mandatory.")
    @PositiveOrZero(message = "Total invested must be positive or zero.")
    BigDecimal totalInvested
) {}
