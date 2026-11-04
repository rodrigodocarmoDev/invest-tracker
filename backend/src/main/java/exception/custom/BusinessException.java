package exception.custom;

/**
 * Quando lançar: Em violações diretas das regras de negócio da aplicação.
 * Exemplos: tipo de operação desconhecido, datas incoerentes ou operações não autorizadas pelo domínio.
 * O GlobalExceptionHandler intercepta essa exceção e responde automaticamente com HTTP 400 Bad Request.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
