package dto.dailyquote;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record DailyQuoteForm(
    @NotNull(message = "Asset ID is mandatory.")
    UUID assetId,

    @NotNull(message = "Quote date is mandatory.")
    LocalDate quoteDate,

    @NotNull(message = "Close price is mandatory.")
    @Positive(message = "Close price must be greater than zero.")
    BigDecimal closePrice
) {}
