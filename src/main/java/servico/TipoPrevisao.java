package servico;

/** Estrategias de previsao disponiveis. Usado pela FabricaPrevisao e pela configuracao. */
public enum TipoPrevisao {
    MEDIA_MOVEL("Media Movel"),
    COMPARACAO("Comparacao com periodo anterior");

    private final String rotulo;

    TipoPrevisao(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
