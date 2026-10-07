package model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transactions {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    public Wallets wallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    public Assets asset;

    @Column(name = "operation_type", nullable = false)
    public String operationType;

    @Column(nullable = false, precision = 19, scale = 8)
    public BigDecimal quantity;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
    public BigDecimal unitPrice;

    @Column(precision = 19, scale = 4)
    public BigDecimal fees;
}
