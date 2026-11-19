package dominio;

import excecao.FonteIndisponivelException;

import java.util.Random;

/** Sensor de nivel de corrego/rio simulado, em centimetros (120 a 370 cm). */
public class SensorNivelRio implements Sensor {

    private final String id;
    private final Random random;
    private final double probabilidadeFalha;
    private boolean operacional = true;

    public SensorNivelRio(String id, Random random, double probabilidadeFalha) {
        this.id = id;
        this.random = random;
        this.probabilidadeFalha = probabilidadeFalha;
    }

    public SensorNivelRio(String id) {
        this(id, new Random(), 0.05);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public TipoSensor getTipo() {
        return TipoSensor.NIVEL_RIO;
    }

    @Override
    public double lerValor() throws FonteIndisponivelException {
        if (!operacional || random.nextDouble() < probabilidadeFalha) {
            throw new FonteIndisponivelException("Sensor de nivel " + id + " nao respondeu a leitura.");
        }
        double valor = 120.0 + random.nextDouble() * 250.0;
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
