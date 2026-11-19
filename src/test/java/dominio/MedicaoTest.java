package dominio;

import excecao.DadosInvalidosException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MedicaoTest {

    @Test
    void medicaoDeChuvaNegativa_lancaDadosInvalidosException() {
        // Arrange
        double valorInvalido = -5.0;

        // Act
        DadosInvalidosException erro = assertThrows(DadosInvalidosException.class,
                () -> new Medicao("PLUV-01", TipoSensor.CHUVA, valorInvalido));

        // Assert
        assertTrue(erro.getMessage().contains("invalida"));
        assertTrue(erro.getMessage().contains("PLUV-01"));
    }

    @Test
    void umidadeAcimaDe100_lancaDadosInvalidosException() {
        assertThrows(DadosInvalidosException.class,
                () -> new Medicao("SOLO-01", TipoSensor.UMIDADE_SOLO, 120.0));
    }

    @Test
    void valorNaoNumerico_lancaDadosInvalidosException() {
        assertThrows(DadosInvalidosException.class,
                () -> new Medicao("RIO-01", TipoSensor.NIVEL_RIO, Double.NaN));
    }

    @Test
    void sensorSemIdentificacao_lancaDadosInvalidosException() {
        assertThrows(DadosInvalidosException.class,
                () -> new Medicao(" ", TipoSensor.CHUVA, 10.0));
    }

    @Test
    void medicaoValida_guardaOsDados() throws Exception {
        // Arrange + Act
        Medicao medicao = new Medicao("PLUV-01", TipoSensor.CHUVA, 12.5);

        // Assert
        assertEquals("PLUV-01", medicao.getSensorId());
        assertEquals(TipoSensor.CHUVA, medicao.getTipo());
        assertEquals(12.5, medicao.getValor(), 1e-9);
        assertNotNull(medicao.getInstante());
        assertNull(medicao.getId()); // o id so existe depois de gravar no banco
    }
}
