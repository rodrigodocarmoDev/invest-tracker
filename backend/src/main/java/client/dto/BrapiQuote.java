package client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Uma cotação devolvida pela brapi. Os nomes dos campos são IGUAIS aos do JSON,
 * assim o Jackson converte sem precisar de nenhuma configuração extra.
 *
 * @param symbol             ticker (ex: PETR4)              -> Asset.ticker
 * @param longName           nome da empresa/fundo
 * @param regularMarketPrice último preço negociado          -> DailyQuote.closePrice
 * @param regularMarketTime  momento desse preço (UTC)       -> DailyQuote.quoteDate
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BrapiQuote(
    String symbol,
    String longName,
    String currency,
    BigDecimal regularMarketPrice,
    Instant regularMarketTime
) {}
