package dominio;

import excecao.DadosInvalidosException;

import java.util.List;

/** Previsao = media das ultimas N leituras do historico. */
public class PrevisaoMediaMovel extends EstrategiaPrevisao {

    public PrevisaoMediaMovel(int janela, int minimoLeituras) throws DadosInvalidosException {
        super("Media Movel", janela, minimoLeituras);
    }

    @Override
    public double prever(List<Double> valores) throws DadosInvalidosException {
        super.prever(valores); // validacao comum
        return media(valores, valores.size() - getJanela(), valores.size());
    }

    @Override
    public String descrever() {
        return "media das ultimas " + getJanela() + " leituras";
    }
}
