package servico;

import dominio.EstrategiaPrevisao;
import dominio.PrevisaoComparacao;
import dominio.PrevisaoMediaMovel;
import excecao.DadosInvalidosException;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * PADRAO FACTORY: concentra a criacao das estrategias de previsao. Quem precisa de uma
 * estrategia pede por TipoPrevisao (ou usa a da configuracao) e nao escreve if/switch
 * nem chama "new" das classes concretas. Para criar uma nova estrategia basta registra-la aqui.
 */
public final class FabricaPrevisao {

    /** Receita de criacao de uma estrategia. */
    @FunctionalInterface
    private interface Criador {
        EstrategiaPrevisao criar(int janela, int minimoLeituras) throws DadosInvalidosException;
    }

    private static final Map<TipoPrevisao, Criador> CRIADORES = new EnumMap<>(TipoPrevisao.class);

    static {
        CRIADORES.put(TipoPrevisao.MEDIA_MOVEL, PrevisaoMediaMovel::new);
        CRIADORES.put(TipoPrevisao.COMPARACAO, PrevisaoComparacao::new);
    }

    private FabricaPrevisao() {
    }

    /** Cria a estrategia pedida, com janela e minimo de leituras vindos da configuracao. */
    public static EstrategiaPrevisao criar(TipoPrevisao tipo) throws DadosInvalidosException {
        ConfiguracaoSistema config = ConfiguracaoSistema.getInstance();
        return criar(tipo, config.getJanelaPrevisao(), config.getMinimoLeituras());
    }

    public static EstrategiaPrevisao criar(TipoPrevisao tipo, int janela, int minimoLeituras)
            throws DadosInvalidosException {
        if (tipo == null) {
            throw new DadosInvalidosException("Tipo de previsao nao informado.");
        }
        // o minimo nunca pode ser menor ou igual a janela
        int minimo = Math.max(minimoLeituras, janela + 1);
        return CRIADORES.get(tipo).criar(janela, minimo);
    }

    /** Cria a estrategia padrao definida na configuracao do sistema. */
    public static EstrategiaPrevisao criarPadrao() throws DadosInvalidosException {
        return criar(ConfiguracaoSistema.getInstance().getTipoPrevisaoPadrao());
    }

    /** Cria uma instancia de cada estrategia disponivel. */
    public static List<EstrategiaPrevisao> criarTodas() throws DadosInvalidosException {
        List<EstrategiaPrevisao> todas = new ArrayList<>();
        for (TipoPrevisao tipo : TipoPrevisao.values()) {
            todas.add(criar(tipo));
        }
        return todas;
    }
}
