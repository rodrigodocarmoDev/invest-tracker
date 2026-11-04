package dto.dailyquote;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import model.Asset;
import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyQuoteForm (
        @NotBlank(message="")
        @Size(message="")
        Asset asset,
        @NotBlank(message="")
        LocalDate quoteDate,
        @NotBlank(message="")
        BigDecimal closePrice
) {}
