package dominio;

import excecao.DadosInvalidosException;

/**
 * Qualquer elemento do territorio que o sistema acompanha (ponto de monitoramento,
 * equipamento publico etc.). Cada subclasse define como verifica o proprio status.
 */
public abstract class ComponenteTerritorial {

    private final String id;
    private final String nome;
    private final String regiao;
    private StatusComponente status = StatusComponente.DESCONHECIDO;

    protected ComponenteTerritorial(String id, String nome, String regiao) throws DadosInvalidosException {
        if (id == null || id.isBlank()) {
            throw new DadosInvalidosException("Componente territorial sem identificador.");
        }
        if (nome == null || nome.isBlank()) {
            throw new DadosInvalidosException("Componente " + id + " sem nome.");
        }
        if (regiao == null || regiao.isBlank()) {
            throw new DadosInvalidosException("Componente " + id + " sem regiao.");
        }
        this.id = id;
        this.nome = nome;
        this.regiao = regiao;
    }

    /** Verifica e atualiza o status do componente. */
    public abstract StatusComponente verificarStatus();

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getRegiao() {
        return regiao;
    }

    public StatusComponente getStatus() {
        return status;
    }

    protected void setStatus(StatusComponente status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - regiao %s - %s", nome, id, regiao, status);
    }
}
