package dominio;

import excecao.FonteIndisponivelException;

import java.util.Random;

/** Pluviometro simulado: chuva em mm/h, com valores baixos frequentes e picos raros. */
public class SensorPluviometro implements Sensor {

    private final String id;
    private final Random random;
    private final double probabilidadeFalha;
    private boolean operacional = true;

    public SensorPluviometro(String id, Random random, double probabilidadeFalha) {
        this.id = id;
        this.random = random;
        this.probabilidadeFalha = probabilidadeFalha;
    }

    public SensorPluviometro(String id) {
        this(id, new Random(), 0.05);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public TipoSensor getTipo() {
        return TipoSensor.CHUVA;
    }

    @Override
    public double lerValor() throws FonteIndisponivelException {
        if (!operacional || random.nextDouble() < probabilidadeFalha) {
            throw new FonteIndisponivelException("Pluviometro " + id + " nao respondeu a leitura.");
        }
        double base = random.nextDouble();
        double valor = base * base * 60.0; // distribuicao inclinada: chuva forte e rara
        return Math.round(valor * 10.0) / 10.0;
    }

    @Override
    public boolean verificarFuncionamento() {
        return operacional;
    }

    public void setOperacional(boolean operacional) {
        this.operacional = operacional;
    }
}
