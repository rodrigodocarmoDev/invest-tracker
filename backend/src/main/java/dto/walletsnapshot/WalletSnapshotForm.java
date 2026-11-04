package dto.walletsnapshot;

import jakarta.validation.constraints.NotBlank;
import model.Wallet;
import java.math.BigDecimal;
import java.time.LocalDate;


public record WalletSnapshotForm (
        @NotBlank(message="")
        Wallet wallet,
        @NotBlank(message="")
        LocalDate snapshotDate,
        @NotBlank(message="")
        BigDecimal totalValue,
        @NotBlank(message="")
        BigDecimal totalInvested
){}
