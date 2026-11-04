package dto.transaction;

import model.Asset;
import model.Wallet;
import java.math.BigDecimal;
import java.util.UUID;

public record TransactionDTO (
        UUID id,
        Wallet wallet,
        Asset asset,
        String operationType,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal fees
) {}
