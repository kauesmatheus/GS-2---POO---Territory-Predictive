package excecao;

/**
 * Lancada quando uma fonte de dados falha: sensor fora de operacao,
 * gerador de leituras com defeito ou banco de dados inacessivel.
 */
public class FonteIndisponivelException extends Exception {

    private static final long serialVersionUID = 1L;

    public FonteIndisponivelException(String mensagem) {
        super(mensagem);
    }

    public FonteIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
