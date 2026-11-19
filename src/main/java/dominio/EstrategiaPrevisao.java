package dominio;

import excecao.DadosInvalidosException;

import java.util.List;

/**
 * Estrategia de previsao sobre o historico de medicoes (valores em ordem cronologica).
 *
 * A classe base faz a validacao comum a todas as estrategias (historico suficiente e
 * sem valores invalidos) e devolve a ultima leitura como previsao "ingenua". Cada filha
 * sobrescreve prever(), chama super.prever(...) para reaproveitar a validacao e aplica
 * o seu proprio calculo. E o polimorfismo: quem usa so conhece EstrategiaPrevisao.
 */
public abstract class EstrategiaPrevisao {

    private final String nome;
    private final int janela;
    private final int minimoLeituras;

    protected EstrategiaPrevisao(String nome, int janela, int minimoLeituras) throws DadosInvalidosException {
        if (janela < 2) {
            throw new DadosInvalidosException("Janela da previsao invalida: " + janela + " (minimo 2).");
        }
        if (minimoLeituras <= janela) {
            throw new DadosInvalidosException("Minimo de leituras (" + minimoLeituras
                    + ") precisa ser maior que a janela (" + janela + ").");
        }
        this.nome = nome;
        this.janela = janela;
        this.minimoLeituras = minimoLeituras;
    }

    /**
     * Valida o historico e devolve a ultima leitura (previsao ingenua).
     * As filhas sobrescrevem este metodo e chamam super.prever(valores).
     */
    public double prever(List<Double> valores) throws DadosInvalidosException {
        if (valores == null || valores.size() < minimoLeituras) {
            int tamanho = valores == null ? 0 : valores.size();
            throw new DadosInvalidosException("Historico insuficiente: " + tamanho
                    + " leitura(s) disponivel(is); sao necessarias ao menos " + minimoLeituras
                    + " para uma previsao confiavel.");
        }
        for (Double v : valores) {
            if (v == null || v.isNaN() || v.isInfinite()) {
                throw new DadosInvalidosException("O historico contem uma leitura invalida.");
            }
        }
        return valores.get(valores.size() - 1);
    }

    /** Texto curto explicando como a estrategia calcula o valor previsto. */
    public abstract String descrever();

    public String getNome() {
        return nome;
    }

    public int getJanela() {
        return janela;
    }

    public int getMinimoLeituras() {
        return minimoLeituras;
    }

    /** Media simples de um trecho [inicio, fim) da lista. */
    protected static double media(List<Double> valores, int inicio, int fim) {
        double soma = 0.0;
        for (int i = inicio; i < fim; i++) {
            soma += valores.get(i);
        }
        return soma / (fim - inicio);
    }
}
