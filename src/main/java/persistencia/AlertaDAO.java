package persistencia;

import dominio.Alerta;
import dominio.NivelAlerta;
import dominio.TipoSensor;
import excecao.FonteIndisponivelException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** Acesso JDBC a tabela de alertas (PreparedStatement + try-with-resources). */
public class AlertaDAO {

    private static final String SQL_INSERIR =
            "INSERT INTO alerta (sensor_id, tipo, nivel, valor_previsto, limite, estrategia, mensagem, instante) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_LISTAR =
            "SELECT id, sensor_id, tipo, nivel, valor_previsto, limite, estrategia, mensagem, instante "
                    + "FROM alerta ORDER BY id";
    private static final String SQL_POR_SENSOR =
            "SELECT id, sensor_id, tipo, nivel, valor_previsto, limite, estrategia, mensagem, instante "
                    + "FROM alerta WHERE sensor_id = ? ORDER BY id";

    private final ConexaoBanco conexaoBanco;

    public AlertaDAO(ConexaoBanco conexaoBanco) {
        this.conexaoBanco = conexaoBanco;
    }

    /** Grava o alerta e preenche o id gerado pelo banco. */
    public void salvar(Alerta alerta) throws FonteIndisponivelException {
        try (Connection conexao = conexaoBanco.abrir();
             PreparedStatement ps = conexao.prepareStatement(SQL_INSERIR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, alerta.getSensorId());
            ps.setString(2, alerta.getTipo().name());
            ps.setString(3, alerta.getNivel().name());
            ps.setDouble(4, alerta.getValorPrevisto());
            ps.setDouble(5, alerta.getLimite());
            ps.setString(6, alerta.getEstrategia());
            ps.setString(7, alerta.getMensagem());
            ps.setTimestamp(8, Timestamp.valueOf(alerta.getInstante()));
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    alerta.setId(chaves.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new FonteIndisponivelException("Falha ao gravar o alerta no banco: " + e.getMessage(), e);
        }
    }

    /** Todos os alertas ja emitidos, do mais antigo para o mais recente. */
    public List<Alerta> listarTodos() throws FonteIndisponivelException {
        try (Connection conexao = conexaoBanco.abrir();
             PreparedStatement ps = conexao.prepareStatement(SQL_LISTAR)) {
            return lerLista(ps);
        } catch (SQLException e) {
            throw new FonteIndisponivelException("Falha ao consultar os alertas: " + e.getMessage(), e);
        }
    }

    public List<Alerta> listarPorSensor(String sensorId) throws FonteIndisponivelException {
        try (Connection conexao = conexaoBanco.abrir();
             PreparedStatement ps = conexao.prepareStatement(SQL_POR_SENSOR)) {
            ps.setString(1, sensorId);
            return lerLista(ps);
        } catch (SQLException e) {
            throw new FonteIndisponivelException("Falha ao consultar os alertas do sensor "
                    + sensorId + ": " + e.getMessage(), e);
        }
    }

    private List<Alerta> lerLista(PreparedStatement ps) throws SQLException, FonteIndisponivelException {
        List<Alerta> lista = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                try {
                    lista.add(new Alerta(
                            rs.getLong("id"),
                            rs.getString("sensor_id"),
                            TipoSensor.valueOf(rs.getString("tipo")),
                            NivelAlerta.valueOf(rs.getString("nivel")),
                            rs.getDouble("valor_previsto"),
                            rs.getDouble("limite"),
                            rs.getString("estrategia"),
                            rs.getString("mensagem"),
                            rs.getTimestamp("instante").toLocalDateTime()));
                } catch (IllegalArgumentException e) {
                    throw new FonteIndisponivelException(
                            "Registro de alerta corrompido no banco (id " + rs.getLong("id") + "): "
                                    + e.getMessage(), e);
                }
            }
        }
        return lista;
    }
}
