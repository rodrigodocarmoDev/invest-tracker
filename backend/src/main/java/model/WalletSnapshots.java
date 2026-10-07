package model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "wallet_snapshots")
public class WalletSnapshots {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    public Wallets wallet;

    @Column(name = "snapshot_date", nullable = false)
    public LocalDate snapshotDate;

    @Column(name = "total_value", nullable = false, precision = 19, scale = 4)
    public BigDecimal totalValue;

    @Column(name = "total_invested", nullable = false, precision = 19, scale = 4)
    public BigDecimal totalInvested;
}
