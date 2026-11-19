package dominio;

import excecao.DadosInvalidosException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DadosTerritorioTest {

    private DadosTerritorio dados;

    @BeforeEach
    void preparar() throws Exception {
        dados = new DadosTerritorio("Bacia Teste", "Zona Teste", 500);
    }

    @Test
    void limiaresForaDeOrdem_lancaDadosInvalidosException() {
        DadosInvalidosException erro = assertThrows(DadosInvalidosException.class,
                () -> dados.definirLimiares(TipoSensor.CHUVA, 30.0, 20.0, 50.0));

        assertTrue(erro.getMessage().contains("ATENCAO < ALERTA < CRITICO"));
    }

    @Test
    void limiarForaDaFaixaDoSensor_lancaDadosInvalidosException() {
        assertThrows(DadosInvalidosException.class,
                () -> dados.definirLimiares(TipoSensor.UMIDADE_SOLO, 70.0, 85.0, 150.0));
    }

    @Test
    void populacaoNegativa_lancaDadosInvalidosException() {
        assertThrows(DadosInvalidosException.class, () -> dados.setPopulacaoExposta(-10));
    }

    @Test
    void nomeVazio_lancaDadosInvalidosException() {
        assertThrows(DadosInvalidosException.class, () -> dados.setNome("   "));
    }

    @Test
    void classificaCadaNivelConformeOsLimiaresPadraoDeChuva() {
        // limiares padrao de chuva: 20 / 35 / 50
        assertEquals(Optional.empty(), dados.classificar(TipoSensor.CHUVA, 10.0));
        assertEquals(Optional.of(NivelAlerta.ATENCAO), dados.classificar(TipoSensor.CHUVA, 25.0));
        assertEquals(Optional.of(NivelAlerta.ALERTA), dados.classificar(TipoSensor.CHUVA, 40.0));
        assertEquals(Optional.of(NivelAlerta.CRITICO), dados.classificar(TipoSensor.CHUVA, 50.0));
    }

    @Test
    void limiaresAlteradosPassamAValerNaClassificacao() throws Exception {
        // Arrange
        dados.definirLimiares(TipoSensor.CHUVA, 5.0, 10.0, 15.0);

        // Act
        Optional<NivelAlerta> nivel = dados.classificar(TipoSensor.CHUVA, 12.0);

        // Assert
        assertEquals(Optional.of(NivelAlerta.ALERTA), nivel);
    }
}
