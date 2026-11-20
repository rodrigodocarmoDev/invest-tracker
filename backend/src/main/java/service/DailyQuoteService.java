package service;

import dto.dailyquote.DailyQuoteDTO;
import dto.dailyquote.DailyQuoteForm;
import exception.custom.ConflictException;
import exception.custom.ResourceNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import mapper.DailyQuoteMapper;
import model.Asset;
import model.DailyQuote;
import repository.AssetRepository;
import repository.DailyQuoteRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class DailyQuoteService {

    @Inject
    DailyQuoteRepository dailyQuoteRepository;

    @Inject
    AssetRepository assetRepository;

    @Inject
    DailyQuoteMapper dailyQuoteMapper;

    public List<DailyQuoteDTO> getAllDailyQuotes() {
        List<DailyQuote> dailyQuotes = dailyQuoteRepository.listAll();
        return dailyQuoteMapper.listToDTOs(dailyQuotes);
    }

    public DailyQuoteDTO getById(UUID id) {
        DailyQuote dailyQuote = dailyQuoteRepository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Daily quote not found for ID: " + id));
        return dailyQuoteMapper.toDTO(dailyQuote);
    }

    @Transactional
    public DailyQuoteDTO create(DailyQuoteForm form) {
        Asset asset = assetRepository.findByIdOptional(form.assetId())
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found for ID: " + form.assetId()));

        dailyQuoteRepository.findByAssetAndDate(form.assetId(), form.quoteDate()).ifPresent(existing -> {
            throw new ConflictException("Daily quote already registered for asset " + asset.ticker + " on date " + form.quoteDate());
        });

        DailyQuote quote = new DailyQuote();
        quote.asset = asset;
        quote.quoteDate = form.quoteDate();
        quote.closePrice = form.closePrice();

        dailyQuoteRepository.persist(quote);
        return dailyQuoteMapper.toDTO(quote);
    }

    @Transactional
    public DailyQuoteDTO update(UUID id, DailyQuoteForm form) {
        DailyQuote dailyQuote = dailyQuoteRepository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Daily quote not found for ID: " + id));

        if (form.assetId() != null) {
            Asset asset = assetRepository.findByIdOptional(form.assetId())
                    .orElseThrow(() -> new ResourceNotFoundException("Asset not found for ID: " + form.assetId()));
            dailyQuote.asset = asset;
        }

        if (form.quoteDate() != null) {
            dailyQuote.quoteDate = form.quoteDate();
        }

        if (form.closePrice() != null) {
            dailyQuote.closePrice = form.closePrice();
        }

        return dailyQuoteMapper.toDTO(dailyQuote);
    }

    /**
     * Usado pela sincronização automática (QuoteSyncService): grava a cotação do dia
     * ou, se ela já existir, apenas atualiza o preço.
     *
     * Isso torna a sincronização IDEMPOTENTE: rodar 1 ou 10 vezes no mesmo dia
     * produz o mesmo resultado (uma cotação por ativo por dia), sem erro de duplicidade.
     */
    @Transactional
    public void saveOrUpdate(UUID assetId, LocalDate quoteDate, BigDecimal closePrice) {
        Asset asset = assetRepository.findByIdOptional(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found for ID: " + assetId));

        dailyQuoteRepository.findByAssetAndDate(assetId, quoteDate).ifPresentOrElse(
                existing -> existing.closePrice = closePrice,
                () -> {
                    DailyQuote quote = new DailyQuote();
                    quote.asset = asset;
                    quote.quoteDate = quoteDate;
                    quote.closePrice = closePrice;
                    dailyQuoteRepository.persist(quote);
                });
    }

    @Transactional
    public void delete(UUID id) {
        DailyQuote dailyQuote = dailyQuoteRepository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Daily quote not found for ID: " + id));
        dailyQuoteRepository.delete(dailyQuote);
    }
}
