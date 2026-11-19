package dominio;

import excecao.DadosInvalidosException;

import java.time.LocalDateTime;

/**
 * Uma leitura de sensor. A validacao acontece na construcao:
 * e impossivel existir uma Medicao com valor fora da faixa do seu tipo.
 */
public class Medicao {

    private Long id; // preenchido pelo banco ao gravar
    private final String sensorId;
    private final TipoSensor tipo;
    private final double valor;
    private final LocalDateTime instante;

    /** Nova leitura, feita agora. */
    public Medicao(String sensorId, TipoSensor tipo, double valor) throws DadosInvalidosException {
        this(null, sensorId, tipo, valor, LocalDateTime.now());
    }

    /** Usado tambem para reconstruir leituras vindas do banco. */
    public Medicao(Long id, String sensorId, TipoSensor tipo, double valor, LocalDateTime instante)
            throws DadosInvalidosException {
        if (sensorId == null || sensorId.isBlank()) {
            throw new DadosInvalidosException("Medicao sem identificacao de sensor.");
        }
        if (tipo == null) {
            throw new DadosInvalidosException("Medicao do sensor " + sensorId + " sem tipo definido.");
        }
        if (!tipo.aceita(valor)) {
            throw new DadosInvalidosException(String.format(
                    "Medicao de %s invalida no sensor %s: %s %s esta fora da faixa aceita (%s a %s %s).",
                    tipo.getNome().toLowerCase(), sensorId,
                    Formato.numero(valor), tipo.getUnidade(),
                    Formato.numero(tipo.getMinimo()), Formato.numero(tipo.getMaximo()), tipo.getUnidade()));
        }
        if (instante == null) {
            throw new DadosInvalidosException("Medicao do sensor " + sensorId + " sem data/hora.");
        }
        this.id = id;
        this.sensorId = sensorId;
        this.tipo = tipo;
        this.valor = valor;
        this.instante = instante;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSensorId() {
        return sensorId;
    }

    public TipoSensor getTipo() {
        return tipo;
    }

    public double getValor() {
        return valor;
    }

    public LocalDateTime getInstante() {
        return instante;
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %s %s", Formato.dataHora(instante), sensorId,
                Formato.numero(valor), tipo.getUnidade());
    }
}
