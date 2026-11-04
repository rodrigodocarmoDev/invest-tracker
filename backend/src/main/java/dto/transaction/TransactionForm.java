package dto.transaction;

import jakarta.validation.constraints.NotBlank;
import model.Asset;
import model.Wallet;
import java.math.BigDecimal;


public record TransactionForm (
        @NotBlank(message="")
        @NotBlank(message="")
        Wallet wallet,
        @NotBlank(message="")
        Asset asset,
        @NotBlank(message="")
        String operationType,
        @NotBlank(message="")
        BigDecimal quantity,
        @NotBlank(message="")
        BigDecimal unitPrice,
        @NotBlank(message="")
        BigDecimal fees
) {}
