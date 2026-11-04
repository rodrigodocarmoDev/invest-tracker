package exception.dto;

/**
 * Detalha a falha de validação em um campo específico disparada pelo Bean Validation (@NotBlank, @NotNull, etc.).
 * Retornado na lista 'fieldErrors' do ErrorResponseDTO para facilitar o apontamento visual no frontend.
 */
public record ValidationErrorDTO(
    String field,
    String message
) {}
