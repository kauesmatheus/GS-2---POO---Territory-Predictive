package persistencia;

import dominio.Medicao;
import dominio.TipoSensor;
import excecao.DadosInvalidosException;
import excecao.FonteIndisponivelException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Acesso JDBC a tabela de medicoes. Todo SQL usa PreparedStatement com parametros (?),
 * nunca concatenacao de String, e todos os recursos sao fechados com try-with-resources.
 */
public class MedicaoDAO {

    private static final String SQL_INSERIR =
            "INSERT INTO medicao (sensor_id, tipo, valor, instante) VALUES (?, ?, ?, ?)";
    private static final String SQL_HISTORICO =
            "SELECT id, sensor_id, tipo, valor, instante FROM medicao WHERE sensor_id = ? ORDER BY id";
    private static final String SQL_ULTIMAS =
            "SELECT id, sensor_id, tipo, valor, instante FROM medicao WHERE sensor_id = ? ORDER BY id DESC LIMIT ?";
    private static final String SQL_TODAS =
            "SELECT id, sensor_id, tipo, valor, instante FROM medicao ORDER BY sensor_id, id";

    private final ConexaoBanco conexaoBanco;

    public MedicaoDAO(ConexaoBanco conexaoBanco) {
        this.conexaoBanco = conexaoBanco;
    }

    /** Grava a medicao e preenche o id gerado pelo banco. */
    public void salvar(Medicao medicao) throws FonteIndisponivelException {
        try (Connection conexao = conexaoBanco.abrir();
             PreparedStatement ps = conexao.prepareStatement(SQL_INSERIR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, medicao.getSensorId());
            ps.setString(2, medicao.getTipo().name());
            ps.setDouble(3, medicao.getValor());
            ps.setTimestamp(4, Timestamp.valueOf(medicao.getInstante()));
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    medicao.setId(chaves.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new FonteIndisponivelException("Falha ao gravar a medicao no banco: " + e.getMessage(), e);
        }
    }

    /** Todas as medicoes de um sensor, da mais antiga para a mais recente. */
    public List<Medicao> buscarHistoricoPorSensor(String sensorId) throws FonteIndisponivelException {
        try (Connection conexao = conexaoBanco.abrir();
             PreparedStatement ps = conexao.prepareStatement(SQL_HISTORICO)) {
            ps.setString(1, sensorId);
            return lerLista(ps);
        } catch (SQLException e) {
            throw new FonteIndisponivelException("Falha ao consultar o historico do sensor "
                    + sensorId + ": " + e.getMessage(), e);
        }
    }

    /** As ultimas N medicoes de um sensor, em ordem cronologica (da mais antiga para a mais recente). */
    public List<Medicao> buscarUltimasPorSensor(String sensorId, int limite) throws FonteIndisponivelException {
        try (Connection conexao = conexaoBanco.abrir();
             PreparedStatement ps = conexao.prepareStatement(SQL_ULTIMAS)) {
            ps.setString(1, sensorId);
            ps.setInt(2, limite);
            List<Medicao> maisRecentesPrimeiro = lerLista(ps);
            Collections.reverse(maisRecentesPrimeiro);
            return maisRecentesPrimeiro;
        } catch (SQLException e) {
            throw new FonteIndisponivelException("Falha ao consultar as ultimas medicoes do sensor "
                    + sensorId + ": " + e.getMessage(), e);
        }
    }

    /** Todas as medicoes de todos os sensores (usado na exportacao para CSV). */
    public List<Medicao> buscarTodas() throws FonteIndisponivelException {
        try (Connection conexao = conexaoBanco.abrir();
             PreparedStatement ps = conexao.prepareStatement(SQL_TODAS)) {
            return lerLista(ps);
        } catch (SQLException e) {
            throw new FonteIndisponivelException("Falha ao consultar as medicoes: " + e.getMessage(), e);
        }
    }

    private List<Medicao> lerLista(PreparedStatement ps) throws SQLException, FonteIndisponivelException {
        List<Medicao> lista = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                try {
                    lista.add(new Medicao(
                            rs.getLong("id"),
                            rs.getString("sensor_id"),
                            TipoSensor.valueOf(rs.getString("tipo")),
                            rs.getDouble("valor"),
                            rs.getTimestamp("instante").toLocalDateTime()));
                } catch (DadosInvalidosException | IllegalArgumentException e) {
                    throw new FonteIndisponivelException(
                            "Registro de medicao corrompido no banco (id " + rs.getLong("id") + "): "
                                    + e.getMessage(), e);
                }
            }
        }
        return lista;
    }
}
