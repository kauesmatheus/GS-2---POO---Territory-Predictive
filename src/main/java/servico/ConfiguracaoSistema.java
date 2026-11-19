package servico;

import dominio.DadosTerritorio;
import excecao.DadosInvalidosException;

/**
 * PADRAO SINGLETON: existe uma unica configuracao no sistema inteiro (dados do territorio,
 * limiares, janela da previsao, minimo de leituras). Menu, servico e fabrica consultam a
 * mesma instancia, entao uma alteracao de limiar vale para todo mundo ao mesmo tempo.
 * O construtor e privado: ninguem consegue criar uma segunda configuracao.
 */
public final class ConfiguracaoSistema {

    private static ConfiguracaoSistema instancia;

    private final DadosTerritorio dadosTerritorio;
    private int janelaPrevisao = 3;
    private int minimoLeituras = 5;
    private int historicoConsulta = 20;
    private TipoPrevisao tipoPrevisaoPadrao = TipoPrevisao.MEDIA_MOVEL;

    private ConfiguracaoSistema() {
        try {
            // territorio ficticio: descrito por regiao e equipamento, nunca por pessoas
            this.dadosTerritorio = new DadosTerritorio("Bacia do Corrego Exemplo", "Zona Norte (simulado)", 1200);
        } catch (DadosInvalidosException e) {
            throw new IllegalStateException("Configuracao padrao do territorio e invalida: " + e.getMessage(), e);
        }
    }

    public static synchronized ConfiguracaoSistema getInstance() {
        if (instancia == null) {
            instancia = new ConfiguracaoSistema();
        }
        return instancia;
    }

    /** Volta tudo ao padrao. Usado pelos testes para um comeco limpo. */
    static synchronized void reiniciarParaTestes() {
        instancia = null;
    }

    public DadosTerritorio getDadosTerritorio() {
        return dadosTerritorio;
    }

    public int getJanelaPrevisao() {
        return janelaPrevisao;
    }

    public void setJanelaPrevisao(int janelaPrevisao) throws DadosInvalidosException {
        if (janelaPrevisao < 2 || janelaPrevisao > 10) {
            throw new DadosInvalidosException("Janela da previsao invalida: " + janelaPrevisao
                    + " (use um valor entre 2 e 10).");
        }
        this.janelaPrevisao = janelaPrevisao;
    }

    public int getMinimoLeituras() {
        return minimoLeituras;
    }

    public void setMinimoLeituras(int minimoLeituras) throws DadosInvalidosException {
        if (minimoLeituras < 3) {
            throw new DadosInvalidosException("Minimo de leituras invalido: " + minimoLeituras
                    + " (use ao menos 3).");
        }
        this.minimoLeituras = minimoLeituras;
    }

    /** Quantas medicoes recentes sao buscadas no banco para alimentar a previsao. */
    public int getHistoricoConsulta() {
        return historicoConsulta;
    }

    public TipoPrevisao getTipoPrevisaoPadrao() {
        return tipoPrevisaoPadrao;
    }

    public void setTipoPrevisaoPadrao(TipoPrevisao tipoPrevisaoPadrao) throws DadosInvalidosException {
        if (tipoPrevisaoPadrao == null) {
            throw new DadosInvalidosException("Tipo de previsao padrao nao informado.");
        }
        this.tipoPrevisaoPadrao = tipoPrevisaoPadrao;
    }
}
