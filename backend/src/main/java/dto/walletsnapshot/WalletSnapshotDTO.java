package dto.walletsnapshot;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record WalletSnapshotDTO(
    UUID id,
    UUID walletId,
    String walletName,
    LocalDate snapshotDate,
    BigDecimal totalValue,
    BigDecimal totalInvested
) {}
