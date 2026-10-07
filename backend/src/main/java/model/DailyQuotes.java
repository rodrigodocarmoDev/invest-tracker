package model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "daily_quotes")
public class DailyQuotes {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    public Assets asset;

    @Column(name = "quote_date", nullable = false)
    public LocalDate quoteDate;

    @Column(name = "close_price", nullable = false, precision = 19, scale = 4)
    public BigDecimal closePrice;
}
