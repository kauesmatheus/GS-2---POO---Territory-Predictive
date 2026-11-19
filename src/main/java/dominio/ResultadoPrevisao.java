package dominio;

/**
 * Resultado de uma previsao: o valor previsto + o que ele significa.
 * O alerta e nulo quando o valor previsto esta abaixo do menor limiar.
 */
public record ResultadoPrevisao(String sensorId, TipoSensor tipo, String estrategia,
                                double valorPrevisto, String significado, Alerta alerta) {

    public boolean temAlerta() {
        return alerta != null;
    }
}
