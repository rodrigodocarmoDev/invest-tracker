package dto.dailyquote;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Resultado de uma sincronização de cotações.
 *
 * @param synced tickers atualizados com sucesso
 * @param failed tickers que falharam, com o motivo (ex: "XPTO3: ticker not found on brapi")
 */
public record QuoteSyncResultDTO(
    LocalDateTime executedAt,
    List<String> synced,
    List<String> failed
) {}
