package exception.custom;

/**
 * Quando lançar: Em violações de regras de unicidade ou integridade relacional.
 * Exemplos: e-mail ou ticker já cadastrados, ou tentativa de excluir ativo/carteira com histórico.
 * O GlobalExceptionHandler intercepta essa exceção e responde automaticamente com HTTP 409 Conflict.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
