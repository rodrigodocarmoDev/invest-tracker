package mapper;

import dto.dailyquote.DailyQuoteDTO;
import dto.dailyquote.DailyQuoteForm;
import model.DailyQuote;
import java.util.List;

public class DailyQuoteMapper {

    private final ModelMapper modelMapper = new ModelMapper();

    public DailyQuoteDTO toDTO(DailyQuote dailyQuote) {
        return modelMapper.map(dailyQuote, DailyQuoteDTO.class);
    }

    public List<DailyQuoteDTO> listToDTOs (List<DailyQuote> dailyQuotes) {
        return dailyQuotes.stream().map((DailyQuote dailyQuote) -> toDTO(dailyQuote)).toList();
    }

    public DailyQuote formToModel(DailyQuoteForm dailyQuoteForm) {
        return modelMapper.map(dailyQuoteForm, DailyQuote.class);
    }
}
