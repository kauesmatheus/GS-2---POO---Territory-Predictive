package dominio;

import excecao.FonteIndisponivelException;

/** Contrato de qualquer sensor do territorio. */
public interface Sensor {

    /** Identificador unico do sensor (ex.: PLUV-01). */
    String getId();

    /** Tipo de grandeza que o sensor mede. */
    TipoSensor getTipo();

    /** Le o valor atual do sensor. Lanca FonteIndisponivelException se o sensor falhar. */
    double lerValor() throws FonteIndisponivelException;

    /** Indica se o sensor esta funcionando. */
    boolean verificarFuncionamento();
}
