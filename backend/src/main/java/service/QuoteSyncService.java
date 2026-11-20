package service;

import client.BrapiClient;
import client.dto.BrapiQuote;
import client.dto.BrapiQuoteResponse;
import dto.dailyquote.QuoteSyncResultDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import model.Asset;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import repository.AssetRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Busca na brapi o preço de todos os ativos cadastrados e grava as cotações do dia (DailyQuote).
 *
 * Chamado por dois lugares:
 *   - QuoteSyncJob (scheduler): automaticamente, todo dia útil após o fechamento da B3
 *   - POST /daily-quotes/sync (ADMIN): manualmente, para testar ou recuperar um dia que falhou
 *
 * Importante: este fluxo roda SEM usuário logado (o scheduler não tem JWT),
 * por isso não usa nada que dependa do CurrentUser.
 */
@ApplicationScoped
public class QuoteSyncService {

    private static final Logger LOG = Logger.getLogger(QuoteSyncService.class);

    /** A data da cotação é a data no fuso da B3, não a do servidor (que pode estar em UTC). */
    private static final ZoneId B3_ZONE = ZoneId.of("America/Sao_Paulo");

    @Inject
    @RestClient
    BrapiClient brapiClient;

    @Inject
    AssetRepository assetRepository;

    @Inject
    DailyQuoteService dailyQuoteService;

    @ConfigProperty(name = "brapi.token")
    Optional<String> brapiToken;

    /**
     * @ActivateRequestContext: o Hibernate precisa de um "contexto de requisição" para consultar o banco.
     * Numa chamada HTTP ele já existe; no job e na inicialização, não — esta anotação cria um
     * só durante a execução do método (e reaproveita o existente, se já houver).
     */
    @ActivateRequestContext
    public QuoteSyncResultDTO syncAll() {
        List<Asset> assets = assetRepository.listAll();
        List<String> synced = new ArrayList<>();
        List<String> failed = new ArrayList<>();

        for (Asset asset : assets) {
            // Um ativo com problema (ticker inválido, brapi fora do ar) não pode impedir os demais.
            try {
                BrapiQuote quote = fetchQuote(asset.ticker);
                LocalDate quoteDate = LocalDate.ofInstant(quote.regularMarketTime(), B3_ZONE);

                dailyQuoteService.saveOrUpdate(asset.id, quoteDate, quote.regularMarketPrice());
                synced.add(asset.ticker);
            } catch (RuntimeException ex) {
                String reason = describeFailure(ex);
                LOG.warnf("Quote sync failed for %s: %s", asset.ticker, reason);
                failed.add(asset.ticker + ": " + reason);
            }
        }

        LOG.infof("Quote sync finished: %d synced, %d failed.", synced.size(), failed.size());
        return new QuoteSyncResultDTO(LocalDateTime.now(), synced, failed);
    }

    /** Um ticker por chamada: o plano gratuito da brapi limita a quantidade de ativos por requisição. */
    private BrapiQuote fetchQuote(String ticker) {
        String authorization = brapiToken.map(token -> "Bearer " + token).orElse(null);
        BrapiQuoteResponse response = brapiClient.getQuote(ticker, authorization);

        if (response == null || response.results() == null || response.results().isEmpty()) {
            throw new IllegalStateException("brapi returned no quote");
        }

        BrapiQuote quote = response.results().get(0);
        if (quote.regularMarketPrice() == null || quote.regularMarketTime() == null) {
            throw new IllegalStateException("brapi returned a quote without price or date");
        }
        return quote;
    }

    private String describeFailure(RuntimeException ex) {
        if (ex instanceof WebApplicationException webEx) {
            // A brapi respondeu, mas com erro (ex: 404 ticker inexistente, 401 token inválido, 429 limite de uso)
            return "brapi responded HTTP " + webEx.getResponse().getStatus();
        }
        if (ex instanceof ProcessingException) {
            // Nem chegou resposta: sem internet, DNS, timeout...
            return "brapi unreachable or timed out";
        }
        return ex.getMessage();
    }
}
