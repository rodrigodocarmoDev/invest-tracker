package dto.dailyquote;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record DailyQuoteDTO(
    UUID id,
    UUID assetId,
    String assetTicker,
    LocalDate quoteDate,
    BigDecimal closePrice
) {}
