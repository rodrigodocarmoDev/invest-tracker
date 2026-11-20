package client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Formato da resposta da brapi: { "results": [ {...}, ... ], "requestedAt": ..., "took": ... }
 *
 * @JsonIgnoreProperties(ignoreUnknown = true): a brapi devolve dezenas de campos;
 * mapeamos só os que usamos e ignoramos o resto (e campos que ela adicionar no futuro).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BrapiQuoteResponse(
    List<BrapiQuote> results
) {}
