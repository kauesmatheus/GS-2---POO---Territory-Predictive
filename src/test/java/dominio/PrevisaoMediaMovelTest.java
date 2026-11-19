package dominio;

import excecao.DadosInvalidosException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrevisaoMediaMovelTest {

    @Test
    void calculaMediaDasUltimasTresLeituras() throws Exception {
        // Arrange
        EstrategiaPrevisao estrategia = new PrevisaoMediaMovel(3, 5);
        List<Double> historico = List.of(1.0, 2.0, 3.0, 4.0, 5.0);

        // Act
        double previsto = estrategia.prever(historico);

        // Assert: media de 3, 4 e 5
        assertEquals(4.0, previsto, 1e-9);
    }

    @Test
    void ignoraLeiturasAntigasForaDaJanela() throws Exception {
        // Arrange
        EstrategiaPrevisao estrategia = new PrevisaoMediaMovel(3, 5);
        List<Double> historico = List.of(100.0, 100.0, 2.0, 4.0, 6.0, 8.0);

        // Act
        double previsto = estrategia.prever(historico);

        // Assert: media de 4, 6 e 8 (os 100 nao entram)
        assertEquals(6.0, previsto, 1e-9);
    }

    @Test
    void historicoInsuficiente_lancaDadosInvalidosException() throws Exception {
        // Arrange
        EstrategiaPrevisao estrategia = new PrevisaoMediaMovel(3, 5);

        // Act
        DadosInvalidosException erro = assertThrows(DadosInvalidosException.class,
                () -> estrategia.prever(List.of(1.0, 2.0, 3.0)));

        // Assert: o sistema "se cala" e explica por que
        assertTrue(erro.getMessage().contains("insuficiente"));
    }

    @Test
    void historicoComValorNulo_lancaDadosInvalidosException() throws Exception {
        // Arrange
        EstrategiaPrevisao estrategia = new PrevisaoMediaMovel(3, 5);
        List<Double> historico = Arrays.asList(1.0, 2.0, null, 4.0, 5.0);

        // Act + Assert
        assertThrows(DadosInvalidosException.class, () -> estrategia.prever(historico));
    }

    @Test
    void janelaMenorQueDois_lancaDadosInvalidosException() {
        assertThrows(DadosInvalidosException.class, () -> new PrevisaoMediaMovel(1, 5));
    }
}
