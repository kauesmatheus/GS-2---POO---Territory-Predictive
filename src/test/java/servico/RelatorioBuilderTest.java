package servico;

import dominio.Alerta;
import dominio.DadosTerritorio;
import dominio.Medicao;
import dominio.NivelAlerta;
import dominio.PontoMonitoramento;
import dominio.SensorPluviometro;
import dominio.TipoSensor;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RelatorioBuilderTest {

    @Test
    void relatorioMontadoPassoAPasso_contemTodasAsPartesPedidas() throws Exception {
        // Arrange
        DadosTerritorio territorio = new DadosTerritorio("Bacia Teste", "Zona Teste", 1200);
        PontoMonitoramento ponto = new PontoMonitoramento("PT-01", "Ponte Teste", "Zona Teste",
                new SensorPluviometro("PLUV-01", new Random(1), 0.0));
        Alerta alerta = Alerta.gerar("PLUV-01", TipoSensor.CHUVA, NivelAlerta.ALERTA, 40.0, 35.0, "Media Movel");

        // Act
        String relatorio = RelatorioBuilder.novo()
                .comTitulo("RELATORIO DE TESTE")
                .comTerritorio(territorio)
                .comPontos(List.of(ponto))
                .comUltimasMedicoes("PLUV-01", List.of(new Medicao("PLUV-01", TipoSensor.CHUVA, 12.5)))
                .comAlertas(List.of(alerta), 5)
                .construir();

        // Assert
        assertTrue(relatorio.contains("RELATORIO DE TESTE"));
        assertTrue(relatorio.contains("Bacia Teste"));
        assertTrue(relatorio.contains("OPERACIONAL"));
        assertTrue(relatorio.contains("12,5"));
        assertTrue(relatorio.contains("ALERTA no sensor PLUV-01"));
    }

    @Test
    void semAlertas_relatorioInformaQueNaoHaAlertas() {
        String relatorio = RelatorioBuilder.novo().comAlertas(List.of(), 5).construir();

        assertTrue(relatorio.contains("nenhum alerta emitido"));
        assertFalse(relatorio.contains("ALERTA no sensor"));
    }

    @Test
    void tituloVazio_mantemOTituloPadrao() {
        String relatorio = RelatorioBuilder.novo().comTitulo("  ").construir();

        assertTrue(relatorio.contains("RELATORIO DE STATUS"));
    }
}
