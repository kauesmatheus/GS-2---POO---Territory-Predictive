package servico;

import dominio.Alerta;
import dominio.EstrategiaPrevisao;
import dominio.Medicao;
import dominio.NivelAlerta;
import dominio.ResultadoPrevisao;
import dominio.Sensor;
import dominio.TipoSensor;
import excecao.DadosInvalidosException;
import excecao.FonteIndisponivelException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import persistencia.AlertaDAO;
import persistencia.MedicaoDAO;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testa a logica de previsao e alerta SEM banco de dados: os DAOs e o sensor sao mocks (Mockito).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ServicoMonitoramentoTest {

    @Mock
    private MedicaoDAO medicaoDAO;
    @Mock
    private AlertaDAO alertaDAO;
    @Mock
    private Sensor sensor;

    private ConfiguracaoSistema config;
    private ServicoMonitoramento servico;

    @BeforeEach
    void preparar() {
        ConfiguracaoSistema.reiniciarParaTestes();
        config = ConfiguracaoSistema.getInstance();
        servico = new ServicoMonitoramento(medicaoDAO, alertaDAO, config);
    }

    private void sensorDeChuvaComHistorico(double... valores) throws Exception {
        sensorComHistorico("PLUV-01", TipoSensor.CHUVA, valores);
    }

    private void sensorComHistorico(String id, TipoSensor tipo, double... valores) throws Exception {
        when(sensor.getId()).thenReturn(id);
        when(sensor.getTipo()).thenReturn(tipo);
        List<Medicao> historico = new ArrayList<>();
        for (double v : valores) {
            historico.add(new Medicao(id, tipo, v));
        }
        when(medicaoDAO.buscarUltimasPorSensor(id, config.getHistoricoConsulta())).thenReturn(historico);
    }

    // ------------------------------------------------------------------ alertas

    @Test
    void previsaoAcimaDoLimiteCritico_emiteEGravaAlertaCritico() throws Exception {
        // Arrange: media das 3 ultimas = (60 + 62 + 65) / 3 = 62,33 (limite critico de chuva: 50)
        sensorDeChuvaComHistorico(55, 58, 60, 62, 65);
        EstrategiaPrevisao estrategia = FabricaPrevisao.criar(TipoPrevisao.MEDIA_MOVEL);

        // Act
        ResultadoPrevisao resultado = servico.preverEAlertar(sensor, estrategia);

        // Assert
        ArgumentCaptor<Alerta> captor = ArgumentCaptor.forClass(Alerta.class);
        verify(alertaDAO).salvar(captor.capture());
        Alerta gravado = captor.getValue();
        assertEquals(NivelAlerta.CRITICO, gravado.getNivel());
        assertEquals(50.0, gravado.getLimite(), 1e-9);
        assertEquals("PLUV-01", gravado.getSensorId());
        assertTrue(gravado.getMensagem().contains("PLUV-01")); // explica qual sensor
        assertTrue(resultado.temAlerta());
        assertEquals(62.33, resultado.valorPrevisto(), 0.01);
    }

    @Test
    void previsaoEntreLimiteDeAlertaECritico_emiteAlertaDeNivelAlerta() throws Exception {
        // media das 3 ultimas = (34 + 36 + 38) / 3 = 36 -> acima de 35 (alerta), abaixo de 50 (critico)
        sensorDeChuvaComHistorico(30, 32, 34, 36, 38);

        ResultadoPrevisao resultado = servico.preverEAlertar(sensor, FabricaPrevisao.criar(TipoPrevisao.MEDIA_MOVEL));

        assertEquals(NivelAlerta.ALERTA, resultado.alerta().getNivel());
    }

    @Test
    void previsaoEntreAtencaoEAlerta_emiteAlertaDeNivelAtencao() throws Exception {
        // media das 3 ultimas = (22 + 21 + 23) / 3 = 22 -> acima de 20 (atencao), abaixo de 35 (alerta)
        sensorDeChuvaComHistorico(18, 20, 22, 21, 23);

        ResultadoPrevisao resultado = servico.preverEAlertar(sensor, FabricaPrevisao.criar(TipoPrevisao.MEDIA_MOVEL));

        assertEquals(NivelAlerta.ATENCAO, resultado.alerta().getNivel());
    }

    @Test
    void previsaoAbaixoDosLimiares_naoEmiteAlertaENaoGrava() throws Exception {
        // Arrange
        sensorDeChuvaComHistorico(1, 2, 3, 2, 1);

        // Act
        ResultadoPrevisao resultado = servico.preverEAlertar(sensor, FabricaPrevisao.criar(TipoPrevisao.MEDIA_MOVEL));

        // Assert
        assertFalse(resultado.temAlerta());
        assertTrue(resultado.significado().contains("Dentro do esperado"));
        verify(alertaDAO, never()).salvar(any(Alerta.class));
    }

    @Test
    void estrategiaDeComparacao_projetaATendenciaEGeraAlerta() throws Exception {
        // recente = 20, anterior = 10 -> previsto 30 (atencao >= 20, alerta >= 35)
        sensorDeChuvaComHistorico(10, 10, 10, 20, 20, 20);

        ResultadoPrevisao resultado = servico.preverEAlertar(sensor, FabricaPrevisao.criar(TipoPrevisao.COMPARACAO));

        assertEquals(30.0, resultado.valorPrevisto(), 1e-9);
        assertEquals(NivelAlerta.ATENCAO, resultado.alerta().getNivel());
    }

    @Test
    void previsaoAcimaDaFaixaFisica_ficaLimitadaAoMaximoDoSensor() throws Exception {
        // umidade: recente = 98, anterior = 80 -> projecao 116%, limitada a 100%
        sensorComHistorico("SOLO-01", TipoSensor.UMIDADE_SOLO, 80, 80, 80, 98, 98, 98);

        ResultadoPrevisao resultado = servico.preverEAlertar(sensor, FabricaPrevisao.criar(TipoPrevisao.COMPARACAO));

        assertEquals(100.0, resultado.valorPrevisto(), 1e-9);
        assertEquals(NivelAlerta.CRITICO, resultado.alerta().getNivel());
    }

    // --------------------------------------------------- quando o sistema se cala

    @Test
    void historicoComMenosDeCincoLeituras_lancaExcecaoENaoEmiteAlerta() throws Exception {
        // Arrange: so 3 leituras no banco, mesmo valores altos
        sensorDeChuvaComHistorico(80, 90, 100);

        // Act
        DadosInvalidosException erro = assertThrows(DadosInvalidosException.class,
                () -> servico.preverEAlertar(sensor, FabricaPrevisao.criar(TipoPrevisao.MEDIA_MOVEL)));

        // Assert
        assertTrue(erro.getMessage().contains("3 leitura"));
        verify(alertaDAO, never()).salvar(any(Alerta.class));
    }

    @Test
    void falhaNoBanco_propagaFonteIndisponivelException() throws Exception {
        when(sensor.getId()).thenReturn("PLUV-01");
        when(sensor.getTipo()).thenReturn(TipoSensor.CHUVA);
        when(medicaoDAO.buscarUltimasPorSensor("PLUV-01", config.getHistoricoConsulta()))
                .thenThrow(new FonteIndisponivelException("banco fora do ar"));

        FonteIndisponivelException erro = assertThrows(FonteIndisponivelException.class,
                () -> servico.preverEAlertar(sensor, FabricaPrevisao.criar(TipoPrevisao.MEDIA_MOVEL)));

        assertTrue(erro.getMessage().contains("banco fora do ar"));
    }

    // ------------------------------------------------------------------ leituras

    @Test
    void leituraValida_eGravadaNoBanco() throws Exception {
        // Arrange
        when(sensor.getId()).thenReturn("PLUV-01");
        when(sensor.getTipo()).thenReturn(TipoSensor.CHUVA);
        when(sensor.verificarFuncionamento()).thenReturn(true);
        when(sensor.lerValor()).thenReturn(12.0);

        // Act
        Medicao medicao = servico.registrarLeitura(sensor);

        // Assert
        verify(medicaoDAO).salvar(any(Medicao.class));
        assertEquals(12.0, medicao.getValor(), 1e-9);
        assertEquals("PLUV-01", medicao.getSensorId());
    }

    @Test
    void leituraInvalida_lancaDadosInvalidosExceptionENaoGrava() throws Exception {
        // Arrange: sensor devolve chuva negativa
        when(sensor.getId()).thenReturn("PLUV-01");
        when(sensor.getTipo()).thenReturn(TipoSensor.CHUVA);
        when(sensor.verificarFuncionamento()).thenReturn(true);
        when(sensor.lerValor()).thenReturn(-5.0);

        // Act + Assert
        assertThrows(DadosInvalidosException.class, () -> servico.registrarLeitura(sensor));
        verify(medicaoDAO, never()).salvar(any(Medicao.class));
    }

    @Test
    void sensorForaDeOperacao_lancaFonteIndisponivelExceptionSemTentarLer() throws Exception {
        when(sensor.getId()).thenReturn("PLUV-01");
        when(sensor.verificarFuncionamento()).thenReturn(false);

        assertThrows(FonteIndisponivelException.class, () -> servico.registrarLeitura(sensor));

        verify(sensor, never()).lerValor();
        verify(medicaoDAO, never()).salvar(any(Medicao.class));
    }

    @Test
    void leituraManualInvalida_naoEGravada() throws Exception {
        when(sensor.getId()).thenReturn("SOLO-01");
        when(sensor.getTipo()).thenReturn(TipoSensor.UMIDADE_SOLO);

        assertThrows(DadosInvalidosException.class, () -> servico.registrarLeituraManual(sensor, 250.0));

        verify(medicaoDAO, never()).salvar(any(Medicao.class));
    }
}
