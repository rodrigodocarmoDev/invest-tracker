package model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
// Uma cotação por ativo por dia, garantido também pelo BANCO (protege contra duas sincronizações simultâneas)
@Table(name = "daily_quotes", uniqueConstraints = @UniqueConstraint(columnNames = {"asset_id", "quote_date"}))
public class DailyQuote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    public Asset asset;

    @Column(name = "quote_date", nullable = false)
    public LocalDate quoteDate;

    @Column(name = "close_price", nullable = false, precision = 19, scale = 4)
    public BigDecimal closePrice;
}
