package dominio;

import excecao.DadosInvalidosException;

/** Ponto vulneravel do territorio (ex.: ponte, travessia) equipado com um sensor. */
public class PontoMonitoramento extends ComponenteTerritorial {

    private final Sensor sensor;

    public PontoMonitoramento(String id, String nome, String regiao, Sensor sensor) throws DadosInvalidosException {
        super(id, nome, regiao);
        if (sensor == null) {
            throw new DadosInvalidosException("Ponto de monitoramento " + id + " precisa de um sensor.");
        }
        this.sensor = sensor;
    }

    @Override
    public StatusComponente verificarStatus() {
        setStatus(sensor.verificarFuncionamento() ? StatusComponente.OPERACIONAL : StatusComponente.EM_FALHA);
        return getStatus();
    }

    public Sensor getSensor() {
        return sensor;
    }
}
