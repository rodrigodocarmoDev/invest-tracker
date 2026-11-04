package exception.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Contrato padrão de resposta de erro da API.
 * Garante que qualquer falha (400, 404, 409, 500) retorne sempre a mesma estrutura JSON previsível.
 */
public record ErrorResponseDTO(
    int status,
    String error,
    String message,
    String path,
    LocalDateTime timestamp,
    List<ValidationErrorDTO> fieldErrors
) {
    public ErrorResponseDTO(int status, String error, String message, String path) {
        this(status, error, message, path, LocalDateTime.now(), null);
    }
}
