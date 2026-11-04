package mapper;

import dto.dailyquote.DailyQuoteDTO;
import jakarta.enterprise.context.ApplicationScoped;
import model.DailyQuote;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class DailyQuoteMapper {

    public DailyQuoteDTO toDTO(DailyQuote dailyQuote) {
        if (dailyQuote == null) return null;
        UUID assetId = dailyQuote.asset != null ? dailyQuote.asset.id : null;
        String ticker = dailyQuote.asset != null ? dailyQuote.asset.ticker : null;
        return new DailyQuoteDTO(dailyQuote.id, assetId, ticker, dailyQuote.quoteDate, dailyQuote.closePrice);
    }

    public List<DailyQuoteDTO> listToDTOs(List<DailyQuote> dailyQuotes) {
        if (dailyQuotes == null) return List.of();
        return dailyQuotes.stream().map(this::toDTO).toList();
    }
}
