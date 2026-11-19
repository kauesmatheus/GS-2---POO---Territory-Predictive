package persistencia;

import excecao.FonteIndisponivelException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Abre conexoes JDBC com o H2 (banco embutido, sem instalacao) e cria as tabelas se
 * ainda nao existirem. Em arquivo, os dados sobrevivem ao reiniciar o programa.
 */
public class ConexaoBanco {

    private static final String DDL_MEDICAO =
            "CREATE TABLE IF NOT EXISTS medicao ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                    + "sensor_id VARCHAR(50) NOT NULL, "
                    + "tipo VARCHAR(30) NOT NULL, "
                    + "valor DOUBLE NOT NULL, "
                    + "instante TIMESTAMP NOT NULL)";

    private static final String DDL_ALERTA =
            "CREATE TABLE IF NOT EXISTS alerta ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                    + "sensor_id VARCHAR(50) NOT NULL, "
                    + "tipo VARCHAR(30) NOT NULL, "
                    + "nivel VARCHAR(20) NOT NULL, "
                    + "valor_previsto DOUBLE NOT NULL, "
                    + "limite DOUBLE NOT NULL, "
                    + "estrategia VARCHAR(80) NOT NULL, "
                    + "mensagem VARCHAR(700) NOT NULL, "
                    + "instante TIMESTAMP NOT NULL)";

    private static final String DDL_INDICE =
            "CREATE INDEX IF NOT EXISTS idx_medicao_sensor ON medicao (sensor_id, id)";

    private final String url;
    private final String usuario;
    private final String senha;

    public ConexaoBanco(String url, String usuario, String senha) {
        this.url = url;
        this.usuario = usuario;
        this.senha = senha;
    }

    /** Banco em arquivo (pasta ./dados): os dados sobrevivem ao reiniciar o programa. */
    public static ConexaoBanco emArquivo() {
        return new ConexaoBanco("jdbc:h2:file:./dados/territorio", "sa", "");
    }

    /** Banco so em memoria: some quando o programa termina (util em testes e demonstracoes). */
    public static ConexaoBanco emMemoria() {
        return new ConexaoBanco("jdbc:h2:mem:territorio;DB_CLOSE_DELAY=-1", "sa", "");
    }

    public Connection abrir() throws FonteIndisponivelException {
        try {
            return DriverManager.getConnection(url, usuario, senha);
        } catch (SQLException e) {
            throw new FonteIndisponivelException("Nao foi possivel conectar ao banco de dados: " + e.getMessage(), e);
        }
    }

    /** Cria as tabelas (idempotente). */
    public void inicializarEsquema() throws FonteIndisponivelException {
        try (Connection conexao = abrir(); Statement comando = conexao.createStatement()) {
            comando.execute(DDL_MEDICAO);
            comando.execute(DDL_ALERTA);
            comando.execute(DDL_INDICE);
        } catch (SQLException e) {
            throw new FonteIndisponivelException("Falha ao criar as tabelas do banco: " + e.getMessage(), e);
        }
    }
}
