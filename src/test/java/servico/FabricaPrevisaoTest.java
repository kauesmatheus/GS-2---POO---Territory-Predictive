package servico;

import dominio.EstrategiaPrevisao;
import dominio.PrevisaoComparacao;
import dominio.PrevisaoMediaMovel;
import excecao.DadosInvalidosException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FabricaPrevisaoTest {

    @BeforeEach
    void comecarComConfiguracaoLimpa() {
        ConfiguracaoSistema.reiniciarParaTestes();
    }

    @Test
    void criaMediaMovelQuandoPedido() throws Exception {
        EstrategiaPrevisao estrategia = FabricaPrevisao.criar(TipoPrevisao.MEDIA_MOVEL, 3, 5);

        assertInstanceOf(PrevisaoMediaMovel.class, estrategia);
    }

    @Test
    void criaComparacaoQuandoPedido() throws Exception {
        EstrategiaPrevisao estrategia = FabricaPrevisao.criar(TipoPrevisao.COMPARACAO, 3, 5);

        assertInstanceOf(PrevisaoComparacao.class, estrategia);
    }

    @Test
    void minimoDeLeiturasNuncaFicaMenorOuIgualAJanela() throws Exception {
        // janela 6 com minimo 5 e impossivel: a fabrica ajusta para janela + 1
        EstrategiaPrevisao estrategia = FabricaPrevisao.criar(TipoPrevisao.MEDIA_MOVEL, 6, 5);

        assertEquals(7, estrategia.getMinimoLeituras());
    }

    @Test
    void criarTodas_devolveUmaEstrategiaParaCadaTipo() throws Exception {
        List<EstrategiaPrevisao> todas = FabricaPrevisao.criarTodas();

        assertEquals(TipoPrevisao.values().length, todas.size());
    }

    @Test
    void criarPadrao_usaOTipoDaConfiguracao() throws Exception {
        EstrategiaPrevisao estrategia = FabricaPrevisao.criarPadrao();

        assertInstanceOf(PrevisaoMediaMovel.class, estrategia); // padrao da configuracao
        assertEquals(ConfiguracaoSistema.getInstance().getJanelaPrevisao(), estrategia.getJanela());
    }

    @Test
    void tipoNulo_lancaDadosInvalidosException() {
        assertThrows(DadosInvalidosException.class, () -> FabricaPrevisao.criar(null, 3, 5));
    }
}
