package dominio;

import excecao.FonteIndisponivelException;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SensorTest {

    @Test
    void cadaImplementacaoRetornaOSeuTipo() {
        Sensor chuva = new SensorPluviometro("PLUV-01", new Random(1), 0.0);
        Sensor rio = new SensorNivelRio("RIO-01", new Random(1), 0.0);
        Sensor solo = new SensorUmidadeSolo("SOLO-01", new Random(1), 0.0);

        assertEquals(TipoSensor.CHUVA, chuva.getTipo());
        assertEquals(TipoSensor.NIVEL_RIO, rio.getTipo());
        assertEquals(TipoSensor.UMIDADE_SOLO, solo.getTipo());
    }

    @Test
    void leiturasSimuladasSempreFicamDentroDaFaixaDoTipo() throws Exception {
        Sensor[] sensores = {
                new SensorPluviometro("PLUV-01", new Random(7), 0.0),
                new SensorNivelRio("RIO-01", new Random(7), 0.0),
                new SensorUmidadeSolo("SOLO-01", new Random(7), 0.0)
        };

        for (Sensor sensor : sensores) {
            for (int i = 0; i < 200; i++) {
                double valor = sensor.lerValor();
                assertTrue(sensor.getTipo().aceita(valor),
                        "valor fora da faixa em " + sensor.getId() + ": " + valor);
            }
        }
    }

    @Test
    void sensorForaDeOperacao_lancaFonteIndisponivelException() {
        SensorPluviometro sensor = new SensorPluviometro("PLUV-01", new Random(1), 0.0);
        sensor.setOperacional(false);

        FonteIndisponivelException erro = assertThrows(FonteIndisponivelException.class, sensor::lerValor);

        assertFalse(sensor.verificarFuncionamento());
        assertTrue(erro.getMessage().contains("PLUV-01"));
    }

    @Test
    void sensorComFalhaTemporaria_lancaFonteIndisponivelException() {
        // probabilidade de falha 100%: toda leitura falha
        Sensor sensor = new SensorNivelRio("RIO-01", new Random(1), 1.0);

        assertThrows(FonteIndisponivelException.class, sensor::lerValor);
    }
}
