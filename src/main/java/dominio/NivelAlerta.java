package dominio;

/** Niveis de alerta, do menos para o mais grave. Cada nivel sugere (nao impoe) uma acao. */
public enum NivelAlerta {

    ATENCAO("ATENCAO", "acompanhar a evolucao das leituras nas proximas horas"),
    ALERTA("ALERTA", "preparar as equipes locais e avisar os pontos vulneraveis"),
    CRITICO("CRITICO", "avaliar o acionamento imediato da Defesa Civil e o aviso aos moradores");

    private final String rotulo;
    private final String sugestao;

    NivelAlerta(String rotulo, String sugestao) {
        this.rotulo = rotulo;
        this.sugestao = sugestao;
    }

    public String getRotulo() {
        return rotulo;
    }

    /** Sugestao de apoio a decisao: a decisao final continua sendo da pessoa responsavel. */
    public String getSugestao() {
        return sugestao;
    }
}
