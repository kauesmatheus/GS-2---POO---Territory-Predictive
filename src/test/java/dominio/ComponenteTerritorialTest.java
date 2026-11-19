package dominio;

import excecao.DadosInvalidosException;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ComponenteTerritorialTest {

    @Test
    void pontoComSensorFuncionando_ficaOperacional() throws Exception {
        // Arrange: a variavel usa o tipo abstrato (polimorfismo)
        SensorPluviometro sensor = new SensorPluviometro("PLUV-01", new Random(1), 0.0);
        ComponenteTerritorial ponto = new PontoMonitoramento("PT-01", "Ponte", "Zona Norte", sensor);

        // Act
        StatusComponente status = ponto.verificarStatus();

        // Assert
        assertEquals(StatusComponente.OPERACIONAL, status);
        assertEquals(StatusComponente.OPERACIONAL, ponto.getStatus());
    }

    @Test
    void pontoComSensorQuebrado_ficaEmFalha() throws Exception {
        SensorPluviometro sensor = new SensorPluviometro("PLUV-01", new Random(1), 0.0);
        sensor.setOperacional(false);
        ComponenteTerritorial ponto = new PontoMonitoramento("PT-01", "Ponte", "Zona Norte", sensor);

        assertEquals(StatusComponente.EM_FALHA, ponto.verificarStatus());
    }

    @Test
    void pontoSemSensor_lancaDadosInvalidosException() {
        assertThrows(DadosInvalidosException.class,
                () -> new PontoMonitoramento("PT-01", "Ponte", "Zona Norte", null));
    }

    @Test
    void componenteSemId_lancaDadosInvalidosException() {
        assertThrows(DadosInvalidosException.class,
                () -> new PontoMonitoramento("", "Ponte", "Zona Norte", new SensorPluviometro("PLUV-01")));
    }
}
