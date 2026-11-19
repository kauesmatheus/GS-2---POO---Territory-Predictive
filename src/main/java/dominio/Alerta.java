package dominio;

import java.time.LocalDateTime;

/**
 * Alerta emitido a partir de uma previsao. Sempre carrega a explicacao do porque:
 * qual sensor, qual valor previsto, qual limite foi ultrapassado e por qual estrategia.
 */
public class Alerta {

    private Long id; // preenchido pelo banco ao gravar
    private final String sensorId;
    private final TipoSensor tipo;
    private final NivelAlerta nivel;
    private final double valorPrevisto;
    private final double limite;
    private final String estrategia;
    private final String mensagem;
    private final LocalDateTime instante;

    /** Reconstrucao (vinda do banco). */
    public Alerta(Long id, String sensorId, TipoSensor tipo, NivelAlerta nivel, double valorPrevisto,
                  double limite, String estrategia, String mensagem, LocalDateTime instante) {
        this.id = id;
        this.sensorId = sensorId;
        this.tipo = tipo;
        this.nivel = nivel;
        this.valorPrevisto = valorPrevisto;
        this.limite = limite;
        this.estrategia = estrategia;
        this.mensagem = mensagem;
        this.instante = instante;
    }

    /** Cria um alerta novo, ja com a mensagem explicativa. */
    public static Alerta gerar(String sensorId, TipoSensor tipo, NivelAlerta nivel, double valorPrevisto,
                               double limite, String estrategia) {
        String mensagem = String.format(
                "%s no sensor %s (%s): valor previsto de %s %s (estrategia: %s) atingiu o limite de %s %s. "
                        + "Sugestao de apoio a decisao: %s.",
                nivel.getRotulo(), sensorId, tipo.getNome(),
                Formato.numero(valorPrevisto), tipo.getUnidade(), estrategia,
                Formato.numero(limite), tipo.getUnidade(), nivel.getSugestao());
        return new Alerta(null, sensorId, tipo, nivel, valorPrevisto, limite, estrategia, mensagem,
                LocalDateTime.now());
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

    public NivelAlerta getNivel() {
        return nivel;
    }

    public double getValorPrevisto() {
        return valorPrevisto;
    }

    public double getLimite() {
        return limite;
    }

    public String getEstrategia() {
        return estrategia;
    }

    public String getMensagem() {
        return mensagem;
    }

    public LocalDateTime getInstante() {
        return instante;
    }

    @Override
    public String toString() {
        return "[" + Formato.dataHora(instante) + "] " + mensagem;
    }
}
