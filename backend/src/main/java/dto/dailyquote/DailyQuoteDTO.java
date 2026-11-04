package dto.dailyquote;

import model.Asset;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record DailyQuoteDTO (
        UUID id,
        Asset asset,
        LocalDate quoteDate,
        BigDecimal closePrice
        ) {}
