package dominio;

import excecao.DadosInvalidosException;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Dados do territorio monitorado. Todos os atributos sao privados; a unica forma de
 * alterar nome, populacao ou limiares e pelos setters, que validam e lancam
 * DadosInvalidosException quando o valor nao faz sentido.
 */
public final class DadosTerritorio {

    /** Limiares de um tipo de sensor (imutavel). */
    public record Limiares(double atencao, double alerta, double critico) {
        public double para(NivelAlerta nivel) {
            return switch (nivel) {
                case ATENCAO -> atencao;
                case ALERTA -> alerta;
                case CRITICO -> critico;
            };
        }
    }

    private String nome;
    private String regiao;
    private int populacaoExposta;
    private final Map<TipoSensor, Limiares> limiares = new EnumMap<>(TipoSensor.class);

    public DadosTerritorio(String nome, String regiao, int populacaoExposta) throws DadosInvalidosException {
        setNome(nome);
        setRegiao(regiao);
        setPopulacaoExposta(populacaoExposta);
        // limiares padrao (valores de referencia para a simulacao)
        definirLimiares(TipoSensor.CHUVA, 20.0, 35.0, 50.0);
        definirLimiares(TipoSensor.NIVEL_RIO, 270.0, 320.0, 360.0);
        definirLimiares(TipoSensor.UMIDADE_SOLO, 70.0, 85.0, 95.0);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) throws DadosInvalidosException {
        if (nome == null || nome.isBlank()) {
            throw new DadosInvalidosException("O nome do territorio nao pode ser vazio.");
        }
        this.nome = nome.trim();
    }

    public String getRegiao() {
        return regiao;
    }

    public void setRegiao(String regiao) throws DadosInvalidosException {
        if (regiao == null || regiao.isBlank()) {
            throw new DadosInvalidosException("A regiao do territorio nao pode ser vazia.");
        }
        this.regiao = regiao.trim();
    }

    public int getPopulacaoExposta() {
        return populacaoExposta;
    }

    public void setPopulacaoExposta(int populacaoExposta) throws DadosInvalidosException {
        if (populacaoExposta < 0) {
            throw new DadosInvalidosException(
                    "Populacao exposta invalida: " + populacaoExposta + " (nao pode ser negativa).");
        }
        this.populacaoExposta = populacaoExposta;
    }

    public Limiares getLimiares(TipoSensor tipo) {
        return limiares.get(tipo);
    }

    /**
     * Altera os limiares de um tipo de sensor. Regras: valores dentro da faixa fisica do
     * sensor e em ordem estritamente crescente (atencao &lt; alerta &lt; critico).
     */
    public void definirLimiares(TipoSensor tipo, double atencao, double alerta, double critico)
            throws DadosInvalidosException {
        if (tipo == null) {
            throw new DadosInvalidosException("Tipo de sensor nao informado para os limiares.");
        }
        double[] valores = {atencao, alerta, critico};
        for (double v : valores) {
            if (!tipo.aceita(v)) {
                throw new DadosInvalidosException(String.format(
                        "Limiar invalido para %s: %s %s esta fora da faixa %s a %s %s.",
                        tipo.getNome(), Formato.numero(v), tipo.getUnidade(),
                        Formato.numero(tipo.getMinimo()), Formato.numero(tipo.getMaximo()), tipo.getUnidade()));
            }
        }
        if (!(atencao < alerta && alerta < critico)) {
            throw new DadosInvalidosException(String.format(
                    "Limiares invalidos para %s: e preciso ATENCAO < ALERTA < CRITICO (recebido %s, %s, %s).",
                    tipo.getNome(), Formato.numero(atencao), Formato.numero(alerta), Formato.numero(critico)));
        }
        limiares.put(tipo, new Limiares(atencao, alerta, critico));
    }

    /** Nivel de alerta correspondente a um valor (vazio se estiver abaixo do limiar de atencao). */
    public Optional<NivelAlerta> classificar(TipoSensor tipo, double valor) {
        Limiares l = limiares.get(tipo);
        if (valor >= l.critico()) {
            return Optional.of(NivelAlerta.CRITICO);
        }
        if (valor >= l.alerta()) {
            return Optional.of(NivelAlerta.ALERTA);
        }
        if (valor >= l.atencao()) {
            return Optional.of(NivelAlerta.ATENCAO);
        }
        return Optional.empty();
    }
}
