package excecao;

/**
 * Lancada quando um dado nao respeita as regras do sistema:
 * medicao fora de faixa, populacao negativa, limiar invalido,
 * historico insuficiente para previsao etc.
 *
 * Exemplo: "DadosInvalidosException: medicao de chuva negativa".
 */
public class DadosInvalidosException extends Exception {

    private static final long serialVersionUID = 1L;

    public DadosInvalidosException(String mensagem) {
        super(mensagem);
    }

    public DadosInvalidosException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
