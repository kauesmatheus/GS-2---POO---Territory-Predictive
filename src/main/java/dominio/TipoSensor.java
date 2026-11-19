package dominio;

/**
 * Tipos de grandeza monitorada no recorte escolhido:
 * "Clima local: chuva intensa e risco de alagamento em pontos vulneraveis".
 * Cada tipo conhece a unidade e a faixa fisica aceita para uma medicao.
 */
public enum TipoSensor {

    CHUVA("Chuva", "mm/h", 0.0, 300.0),
    NIVEL_RIO("Nivel do rio", "cm", 0.0, 1000.0),
    UMIDADE_SOLO("Umidade do solo", "%", 0.0, 100.0);

    private final String nome;
    private final String unidade;
    private final double minimo;
    private final double maximo;

    TipoSensor(String nome, String unidade, double minimo, double maximo) {
        this.nome = nome;
        this.unidade = unidade;
        this.minimo = minimo;
        this.maximo = maximo;
    }

    public String getNome() {
        return nome;
    }

    public String getUnidade() {
        return unidade;
    }

    public double getMinimo() {
        return minimo;
    }

    public double getMaximo() {
        return maximo;
    }

    public boolean aceita(double valor) {
        return !Double.isNaN(valor) && !Double.isInfinite(valor) && valor >= minimo && valor <= maximo;
    }
}
