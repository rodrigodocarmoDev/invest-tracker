package exception.custom;

/**
 * Quando lançar: O usuário está autenticado (sabemos QUEM ele é), mas não tem permissão
 * para acessar ou alterar o recurso pedido.
 * Exemplos: usuário comum tentando ver a carteira de outro usuário ou transferir uma carteira.
 * O GlobalExceptionHandler intercepta essa exceção e responde automaticamente com HTTP 403 Forbidden.
 */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }
}
