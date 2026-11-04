package dto.transaction;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionDTO(
    UUID id,
    UUID walletId,
    String walletName,
    UUID assetId,
    String assetTicker,
    String operationType,
    BigDecimal quantity,
    BigDecimal unitPrice,
    BigDecimal fees
) {}
