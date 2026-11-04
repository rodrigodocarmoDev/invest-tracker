package dto.walletsnapshot;

import model.Wallet;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record WalletSnapshotDTO (
        UUID id,
        Wallet wallet,
        LocalDate snapshotDate,
        BigDecimal totalValue,
        BigDecimal totalInvested
){}
