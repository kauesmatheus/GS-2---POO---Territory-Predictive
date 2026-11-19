package servico;

import dominio.Alerta;
import dominio.DadosTerritorio;
import dominio.EstrategiaPrevisao;
import dominio.Formato;
import dominio.Medicao;
import dominio.NivelAlerta;
import dominio.ResultadoPrevisao;
import dominio.Sensor;
import dominio.TipoSensor;
import excecao.DadosInvalidosException;
import excecao.FonteIndisponivelException;
import persistencia.AlertaDAO;
import persistencia.MedicaoDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Regras de negocio do monitoramento: ler e validar medicoes, gravar no banco, prever
 * sobre o historico e emitir alertas. Recebe os DAOs pelo construtor, o que permite
 * testar a logica com Mockito, sem banco de dados.
 */
public class ServicoMonitoramento {

    private final MedicaoDAO medicaoDAO;
    private final AlertaDAO alertaDAO;
    private final ConfiguracaoSistema configuracao;

    public ServicoMonitoramento(MedicaoDAO medicaoDAO, AlertaDAO alertaDAO, ConfiguracaoSistema configuracao) {
        this.medicaoDAO = medicaoDAO;
        this.alertaDAO = alertaDAO;
        this.configuracao = configuracao;
    }

    /** Le o sensor, valida a medicao e grava no banco. Medicao invalida nunca e gravada. */
    public Medicao registrarLeitura(Sensor sensor) throws FonteIndisponivelException, DadosInvalidosException {
        if (!sensor.verificarFuncionamento()) {
            throw new FonteIndisponivelException("Sensor " + sensor.getId() + " esta fora de operacao.");
        }
        double valor = sensor.lerValor();
        return gravarMedicao(sensor, valor);
    }

    /** Registra um valor informado manualmente (ex.: leitura de campo), com a mesma validacao. */
    public Medicao registrarLeituraManual(Sensor sensor, double valor)
            throws FonteIndisponivelException, DadosInvalidosException {
        return gravarMedicao(sensor, valor);
    }

    private Medicao gravarMedicao(Sensor sensor, double valor)
            throws DadosInvalidosException, FonteIndisponivelException {
        Medicao medicao = new Medicao(sensor.getId(), sensor.getTipo(), valor); // valida aqui
        medicaoDAO.salvar(medicao);
        return medicao;
    }

    /**
     * Roda a previsao sobre o historico do banco e, se o valor previsto atingir algum limiar,
     * emite e grava um alerta explicando o porque. Com historico insuficiente o sistema
     * "se cala": lanca excecao em vez de emitir uma previsao pouco confiavel.
     */
    public ResultadoPrevisao preverEAlertar(Sensor sensor, EstrategiaPrevisao estrategia)
            throws FonteIndisponivelException, DadosInvalidosException {
        List<Medicao> historico = medicaoDAO.buscarUltimasPorSensor(sensor.getId(), configuracao.getHistoricoConsulta());
        if (historico.size() < estrategia.getMinimoLeituras()) {
            throw new DadosInvalidosException("Sensor " + sensor.getId() + ": apenas " + historico.size()
                    + " leitura(s) no banco; sao necessarias ao menos " + estrategia.getMinimoLeituras()
                    + " para prever com confianca. Nenhuma previsao ou alerta foi emitido.");
        }

        List<Double> valores = new ArrayList<>();
        for (Medicao m : historico) {
            valores.add(m.getValor());
        }

        TipoSensor tipo = sensor.getTipo();
        double previsto = estrategia.prever(valores);
        previsto = Math.min(tipo.getMaximo(), Math.max(tipo.getMinimo(), previsto)); // mantem na faixa fisica

        DadosTerritorio territorio = configuracao.getDadosTerritorio();
        Optional<NivelAlerta> nivel = territorio.classificar(tipo, previsto);

        Alerta alerta = null;
        String significado;
        if (nivel.isPresent()) {
            double limite = territorio.getLimiares(tipo).para(nivel.get());
            alerta = Alerta.gerar(sensor.getId(), tipo, nivel.get(), previsto, limite, estrategia.getNome());
            alertaDAO.salvar(alerta);
            significado = "Nivel " + nivel.get().getRotulo() + ": o valor previsto atingiu o limite de "
                    + Formato.numero(limite) + " " + tipo.getUnidade() + ".";
        } else {
            significado = "Dentro do esperado: abaixo do limite de atencao ("
                    + Formato.numero(territorio.getLimiares(tipo).atencao()) + " " + tipo.getUnidade() + ").";
        }
        return new ResultadoPrevisao(sensor.getId(), tipo, estrategia.getNome(), previsto, significado, alerta);
    }

    public List<Medicao> consultarHistorico(String sensorId) throws FonteIndisponivelException {
        return medicaoDAO.buscarHistoricoPorSensor(sensorId);
    }

    public List<Medicao> consultarUltimas(String sensorId, int quantidade) throws FonteIndisponivelException {
        return medicaoDAO.buscarUltimasPorSensor(sensorId, quantidade);
    }

    public List<Medicao> consultarTodasMedicoes() throws FonteIndisponivelException {
        return medicaoDAO.buscarTodas();
    }

    public List<Alerta> consultarAlertas() throws FonteIndisponivelException {
        return alertaDAO.listarTodos();
    }
}
