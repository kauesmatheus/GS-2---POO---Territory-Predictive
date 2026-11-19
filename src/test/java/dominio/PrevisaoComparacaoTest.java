package dominio;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class PrevisaoComparacaoTest {

    @Test
    void tendenciaDeAlta_projetaAVariacaoParaOProximoPeriodo() throws Exception {
        // Arrange: periodo anterior = 10, periodo recente = 20
        EstrategiaPrevisao estrategia = new PrevisaoComparacao(3, 5);
        List<Double> historico = List.of(10.0, 10.0, 10.0, 20.0, 20.0, 20.0);

        // Act
        double previsto = estrategia.prever(historico);

        // Assert: 20 + (20 - 10)
        assertEquals(30.0, previsto, 1e-9);
    }

    @Test
    void tendenciaDeQueda_nuncaDevolveValorNegativo() throws Exception {
        // Arrange
        EstrategiaPrevisao estrategia = new PrevisaoComparacao(3, 5);
        List<Double> historico = List.of(30.0, 30.0, 30.0, 10.0, 10.0, 10.0);

        // Act
        double previsto = estrategia.prever(historico);

        // Assert: 10 + (10 - 30) = -10, limitado a 0
        assertEquals(0.0, previsto, 1e-9);
    }

    @Test
    void serieEstavel_mantemOValor() throws Exception {
        EstrategiaPrevisao estrategia = new PrevisaoComparacao(3, 5);

        double previsto = estrategia.prever(List.of(5.0, 5.0, 5.0, 5.0, 5.0, 5.0));

        assertEquals(5.0, previsto, 1e-9);
    }

    @Test
    void comApenasOMinimoDeLeituras_usaOQueSobraComoPeriodoAnterior() throws Exception {
        // Arrange: recente = 6, 8, 10 (media 8); anterior = 4, 4 (media 4)
        EstrategiaPrevisao estrategia = new PrevisaoComparacao(3, 5);

        // Act
        double previsto = estrategia.prever(List.of(4.0, 4.0, 6.0, 8.0, 10.0));

        // Assert: 8 + (8 - 4)
        assertEquals(12.0, previsto, 1e-9);
    }

    @Test
    void polimorfismo_estrategiasDiferentesDaoResultadosDiferentesParaOsMesmosDados() throws Exception {
        // Arrange: a variavel e do tipo da classe abstrata
        EstrategiaPrevisao media = new PrevisaoMediaMovel(3, 5);
        EstrategiaPrevisao comparacao = new PrevisaoComparacao(3, 5);
        List<Double> historico = List.of(10.0, 10.0, 10.0, 20.0, 20.0, 20.0);

        // Act
        double resultadoMedia = media.prever(historico);
        double resultadoComparacao = comparacao.prever(historico);

        // Assert
        assertEquals(20.0, resultadoMedia, 1e-9);
        assertEquals(30.0, resultadoComparacao, 1e-9);
        assertNotEquals(resultadoMedia, resultadoComparacao);
    }
}
