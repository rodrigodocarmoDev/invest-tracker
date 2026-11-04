package exception.custom;

/**
 * Quando lançar: Sempre que uma busca por ID ou chave não encontrar o registro no banco.
 * O GlobalExceptionHandler intercepta essa exceção e responde automaticamente com HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
