package dominio;

import excecao.FonteIndisponivelException;

import java.util.Random;

/** Sensor de umidade do solo simulado, em porcentagem (45% a 95%). */
public class SensorUmidadeSolo implements Sensor {

    private final String id;
    private final Random random;
    private final double probabilidadeFalha;
    private boolean operacional = true;

    public SensorUmidadeSolo(String id, Random random, double probabilidadeFalha) {
        this.id = id;
        this.random = random;
        this.probabilidadeFalha = probabilidadeFalha;
    }

    public SensorUmidadeSolo(String id) {
        this(id, new Random(), 0.05);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public TipoSensor getTipo() {
        return TipoSensor.UMIDADE_SOLO;
    }

    @Override
    public double lerValor() throws FonteIndisponivelException {
        if (!operacional || random.nextDouble() < probabilidadeFalha) {
            throw new FonteIndisponivelException("Sensor de umidade " + id + " nao respondeu a leitura.");
        }
        double valor = 45.0 + random.nextDouble() * 50.0;
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
