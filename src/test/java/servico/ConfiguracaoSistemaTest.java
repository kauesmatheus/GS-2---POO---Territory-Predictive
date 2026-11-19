package servico;

import excecao.DadosInvalidosException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfiguracaoSistemaTest {

    @BeforeEach
    void comecarComConfiguracaoLimpa() {
        ConfiguracaoSistema.reiniciarParaTestes();
    }

    @Test
    void singleton_semprePedirDevolveAMesmaInstancia() {
        ConfiguracaoSistema primeira = ConfiguracaoSistema.getInstance();
        ConfiguracaoSistema segunda = ConfiguracaoSistema.getInstance();

        assertSame(primeira, segunda);
    }

    @Test
    void alteracaoNaConfiguracaoVaiParaTodosQueAConsultam() throws Exception {
        ConfiguracaoSistema.getInstance().setJanelaPrevisao(5);

        assertEquals(5, ConfiguracaoSistema.getInstance().getJanelaPrevisao());
    }

    @Test
    void janelaInvalida_lancaDadosInvalidosException() {
        ConfiguracaoSistema config = ConfiguracaoSistema.getInstance();

        assertThrows(DadosInvalidosException.class, () -> config.setJanelaPrevisao(1));
        assertThrows(DadosInvalidosException.class, () -> config.setJanelaPrevisao(11));
    }

    @Test
    void minimoDeLeiturasInvalido_lancaDadosInvalidosException() {
        ConfiguracaoSistema config = ConfiguracaoSistema.getInstance();

        assertThrows(DadosInvalidosException.class, () -> config.setMinimoLeituras(2));
    }
}
