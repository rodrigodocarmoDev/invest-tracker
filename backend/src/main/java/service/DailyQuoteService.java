package service;

import dto.dailyquote.DailyQuoteDTO;
import dto.dailyquote.DailyQuoteForm;
import jakarta.inject.Inject;
import mapper.DailyQuoteMapper;
import model.DailyQuote;
import repository.DailyQuoteRepository;
import java.util.List;
import java.util.UUID;

public class DailyQuoteService {

    @Inject
    DailyQuoteRepository dailyQuoteRepository;
    @Inject
    DailyQuoteMapper dailyQuoteMapper;

    @Inject
    DailyQuoteRepository dailyQuoteRepository;
    @Inject
    DailyQuoteMapper dailyQuoteMapper;

    public List<DailyQuoteDTO> getAllDailyQuotes() {
        List<DailyQuote> dailyQuotes = dailyQuoteRepository.listAll();
        return dailyQuoteMapper.listToDTOs(dailyQuotes);
    }

    public DailyQuoteDTO getById(UUID id) {
        DailyQuote dailyQuote = dailyQuoteRepository.findById(id);
        return dailyQuoteMapper.toDTO(dailyQuote);
    }

    //@Transactional
    public DailyQuoteDTO create(DailyQuoteForm dailyQuoteForm) {
        DailyQuote dailyQuote = dailyQuoteRepository.create(dailyQuoteForm);
        return dailyQuoteMapper.toDTO(dailyQuote);
    }

    //@Transactional
    public DailyQuoteDTO update(UUID id, DailyQuoteForm dailyQuoteForm) {
        DailyQuote dailyQuote = dailyQuoteRepository.findById(id);
        return dailyQuoteMapper.toDTO(dailyQuote);
    }

    //@Transactional
    public void delete(UUID id) {
        dailyQuoteRepository.deleteById(id);
    }

}
