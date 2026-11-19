package dominio;

import excecao.DadosInvalidosException;

import java.util.List;

/**
 * Previsao por comparacao com o periodo anterior: compara a media das ultimas N leituras
 * com a media das N leituras anteriores e projeta a mesma variacao para o proximo periodo
 * (tendencia de subida ou de queda). Nunca devolve valor negativo.
 */
public class PrevisaoComparacao extends EstrategiaPrevisao {

    public PrevisaoComparacao(int janela, int minimoLeituras) throws DadosInvalidosException {
        super("Comparacao com periodo anterior", janela, minimoLeituras);
    }

    @Override
    public double prever(List<Double> valores) throws DadosInvalidosException {
        super.prever(valores); // validacao comum
        int tamanho = valores.size();
        int inicioRecente = tamanho - getJanela();
        int inicioAnterior = Math.max(0, inicioRecente - getJanela());
        double mediaRecente = media(valores, inicioRecente, tamanho);
        double mediaAnterior = media(valores, inicioAnterior, inicioRecente);
        double previsto = mediaRecente + (mediaRecente - mediaAnterior);
        return Math.max(0.0, previsto);
    }

    @Override
    public String descrever() {
        return "media das ultimas " + getJanela()
                + " leituras somada a variacao em relacao ao periodo anterior (tendencia)";
    }
}
