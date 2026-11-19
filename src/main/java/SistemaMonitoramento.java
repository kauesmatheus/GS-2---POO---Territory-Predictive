import dominio.Alerta;
import dominio.DadosTerritorio;
import dominio.EstrategiaPrevisao;
import dominio.Formato;
import dominio.Medicao;
import dominio.PontoMonitoramento;
import dominio.ResultadoPrevisao;
import dominio.SensorNivelRio;
import dominio.SensorPluviometro;
import dominio.SensorUmidadeSolo;
import dominio.TipoSensor;
import excecao.DadosInvalidosException;
import excecao.FonteIndisponivelException;
import persistencia.AlertaDAO;
import persistencia.ConexaoBanco;
import persistencia.MedicaoDAO;
import servico.ConfiguracaoSistema;
import servico.ExportadorCsv;
import servico.FabricaPrevisao;
import servico.RelatorioBuilder;
import servico.ServicoMonitoramento;
import servico.TipoPrevisao;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Menu principal (console). Aqui e o UNICO lugar que conversa com o usuario: captura as
 * excecoes customizadas e mostra uma mensagem clara. As regras de negocio ficam no servico.
 */
public class SistemaMonitoramento {

    private static final int LIMITE_LISTAGEM = 30;

    private final Scanner entrada;
    private final PrintStream saida;
    private final ServicoMonitoramento servico;
    private final ConfiguracaoSistema configuracao;
    private final List<PontoMonitoramento> pontos;
    private boolean entradaEncerrada = false;

    public SistemaMonitoramento(Scanner entrada, PrintStream saida, ServicoMonitoramento servico,
                                ConfiguracaoSistema configuracao, List<PontoMonitoramento> pontos) {
        this.entrada = entrada;
        this.saida = saida;
        this.servico = servico;
        this.configuracao = configuracao;
        this.pontos = pontos;
    }

    // ------------------------------------------------------------------ main

    public static void main(String[] args) {
        PrintStream saida = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        Scanner entrada = new Scanner(System.in, StandardCharsets.UTF_8);
        try {
            // padrao: banco em arquivo (os dados sobrevivem ao reiniciar). Com --memoria: banco so em memoria.
            boolean emMemoria = args.length > 0 && args[0].equalsIgnoreCase("--memoria");
            ConexaoBanco banco = emMemoria ? ConexaoBanco.emMemoria() : ConexaoBanco.emArquivo();
            saida.println(emMemoria
                    ? "[BANCO] H2 em memoria: os dados somem ao encerrar."
                    : "[BANCO] H2 em arquivo (pasta ./dados): os dados sobrevivem ao reiniciar.");
            banco.inicializarEsquema();

            ConfiguracaoSistema configuracao = ConfiguracaoSistema.getInstance();
            ServicoMonitoramento servico =
                    new ServicoMonitoramento(new MedicaoDAO(banco), new AlertaDAO(banco), configuracao);

            String regiao = configuracao.getDadosTerritorio().getRegiao();
            List<PontoMonitoramento> pontos = List.of(
                    new PontoMonitoramento("PT-01", "Ponte do Corrego (chuva)", regiao, new SensorPluviometro("PLUV-01")),
                    new PontoMonitoramento("PT-02", "Travessia da Rua Baixa (nivel)", regiao, new SensorNivelRio("RIO-01")),
                    new PontoMonitoramento("PT-03", "Encosta da Vila (solo)", regiao, new SensorUmidadeSolo("SOLO-01")));

            new SistemaMonitoramento(entrada, saida, servico, configuracao, pontos).executar();
        } catch (FonteIndisponivelException e) {
            saida.println("[ERRO] Nao foi possivel iniciar o sistema: " + e.getMessage());
        } catch (DadosInvalidosException e) {
            saida.println("[ERRO] Configuracao inicial invalida: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------ loop do menu

    public void executar() {
        saida.println();
        saida.println("=== TERRITORY PREDICTIVE INSIGHTS - Monitoramento territorial (CitizenTech) ===");
        saida.println("Territorio: " + configuracao.getDadosTerritorio().getNome());
        boolean ativo = true;
        while (ativo) {
            exibirMenu();
            int opcao = lerInteiro("Escolha uma opcao: ");
            if (entradaEncerrada) {
                break;
            }
            try {
                switch (opcao) {
                    case 1 -> verificarSensores();
                    case 2 -> rodarPrevisoes();
                    case 3 -> verHistorico();
                    case 4 -> gerenciarDadosTerritorio();
                    case 5 -> statusCompleto();
                    case 6 -> registrarLeituraManual();
                    case 7 -> gerarRodadasSimuladas();
                    case 8 -> exportarCsv();
                    case 0 -> ativo = false;
                    default -> saida.println("[AVISO] Opcao invalida. Escolha um numero do menu.");
                }
            } catch (DadosInvalidosException e) {
                saida.println("[DADO INVALIDO] " + e.getMessage());
            } catch (FonteIndisponivelException e) {
                saida.println("[FONTE INDISPONIVEL] " + e.getMessage());
            }
        }
        saida.println("Encerrando. Os dados ficaram gravados no banco. Ate a proxima!");
    }

    private void exibirMenu() {
        saida.println();
        saida.println("----------------------------- MENU -----------------------------");
        saida.println(" 1 - Verificar sensores (status + nova leitura gravada)");
        saida.println(" 2 - Rodar previsoes e alertas");
        saida.println(" 3 - Ver historico (medicoes por sensor / alertas emitidos)");
        saida.println(" 4 - Gerenciar dados do territorio (populacao, limiares, janela)");
        saida.println(" 5 - Status completo");
        saida.println(" 6 - Registrar leitura manual (testa a validacao)");
        saida.println(" 7 - Gerar varias rodadas de leituras simuladas");
        saida.println(" 8 - Exportar historico para CSV (bonus)");
        saida.println(" 0 - Sair");
        saida.println("----------------------------------------------------------------");
    }

    // ------------------------------------------------------------- 1 - sensores

    private void verificarSensores() {
        saida.println();
        for (PontoMonitoramento ponto : pontos) {
            ponto.verificarStatus();
            saida.println(ponto);
            try {
                Medicao medicao = servico.registrarLeitura(ponto.getSensor());
                saida.println("   Leitura gravada: " + Formato.numero(medicao.getValor()) + " "
                        + medicao.getTipo().getUnidade() + " (sensor " + medicao.getSensorId() + ")");
            } catch (DadosInvalidosException e) {
                saida.println("   [DADO INVALIDO] " + e.getMessage());
            } catch (FonteIndisponivelException e) {
                saida.println("   [FONTE INDISPONIVEL] " + e.getMessage());
            }
        }
    }

    // ------------------------------------------------------------ 2 - previsoes

    private void rodarPrevisoes() throws DadosInvalidosException {
        TipoPrevisao[] tipos = TipoPrevisao.values();
        saida.println();
        saida.println("Estrategia de previsao:");
        for (int i = 0; i < tipos.length; i++) {
            saida.println(" " + (i + 1) + " - " + tipos[i].getRotulo());
        }
        saida.println(" " + (tipos.length + 1) + " - Todas");
        int escolha = lerInteiro("Escolha: ");
        if (escolha < 1 || escolha > tipos.length + 1) {
            saida.println("[AVISO] Opcao de estrategia invalida.");
            return;
        }

        List<EstrategiaPrevisao> estrategias = new ArrayList<>();
        if (escolha == tipos.length + 1) {
            estrategias.addAll(FabricaPrevisao.criarTodas());
        } else {
            estrategias.add(FabricaPrevisao.criar(tipos[escolha - 1]));
        }

        for (PontoMonitoramento ponto : pontos) {
            for (EstrategiaPrevisao estrategia : estrategias) {
                try {
                    ResultadoPrevisao r = servico.preverEAlertar(ponto.getSensor(), estrategia);
                    saida.println();
                    saida.println("Sensor " + r.sensorId() + " | " + r.estrategia() + " (" + estrategia.descrever() + ")");
                    saida.println("   Valor previsto: " + Formato.numero(r.valorPrevisto()) + " " + r.tipo().getUnidade());
                    saida.println("   Significado: " + r.significado());
                    if (r.temAlerta()) {
                        saida.println("   >>> " + r.alerta().getMensagem());
                    }
                } catch (DadosInvalidosException e) {
                    saida.println();
                    saida.println("[SEM PREVISAO] " + e.getMessage());
                } catch (FonteIndisponivelException e) {
                    saida.println();
                    saida.println("[FONTE INDISPONIVEL] " + e.getMessage());
                }
            }
        }
    }

    // -------------------------------------------------------------- 3 - historico

    private void verHistorico() throws FonteIndisponivelException {
        saida.println();
        saida.println(" 1 - Medicoes de um sensor");
        saida.println(" 2 - Alertas ja emitidos");
        int escolha = lerInteiro("Escolha: ");
        if (escolha == 1) {
            PontoMonitoramento ponto = escolherPonto();
            if (ponto == null) {
                return;
            }
            List<Medicao> historico = servico.consultarHistorico(ponto.getSensor().getId());
            if (historico.isEmpty()) {
                saida.println("Nenhuma medicao gravada para o sensor " + ponto.getSensor().getId() + " ainda.");
                return;
            }
            int inicio = Math.max(0, historico.size() - LIMITE_LISTAGEM);
            saida.println("Sensor " + ponto.getSensor().getId() + ": " + historico.size()
                    + " medicao(oes) no banco (mostrando as " + (historico.size() - inicio) + " mais recentes).");
            for (int i = inicio; i < historico.size(); i++) {
                saida.println("  " + historico.get(i));
            }
        } else if (escolha == 2) {
            List<Alerta> alertas = servico.consultarAlertas();
            if (alertas.isEmpty()) {
                saida.println("Nenhum alerta emitido ate agora.");
                return;
            }
            int inicio = Math.max(0, alertas.size() - LIMITE_LISTAGEM);
            saida.println(alertas.size() + " alerta(s) no banco (mostrando os " + (alertas.size() - inicio)
                    + " mais recentes).");
            for (int i = inicio; i < alertas.size(); i++) {
                saida.println("  " + alertas.get(i));
            }
        } else {
            saida.println("[AVISO] Opcao invalida.");
        }
    }

    // ------------------------------------------------------- 4 - dados do territorio

    private void gerenciarDadosTerritorio() throws DadosInvalidosException {
        DadosTerritorio dados = configuracao.getDadosTerritorio();
        saida.println();
        saida.println("Territorio: " + dados.getNome() + " | Regiao: " + dados.getRegiao()
                + " | Populacao exposta: " + dados.getPopulacaoExposta());
        for (TipoSensor tipo : TipoSensor.values()) {
            DadosTerritorio.Limiares l = dados.getLimiares(tipo);
            saida.println("  Limiares de " + tipo.getNome() + " (" + tipo.getUnidade() + "): atencao "
                    + Formato.numero(l.atencao()) + " | alerta " + Formato.numero(l.alerta())
                    + " | critico " + Formato.numero(l.critico()));
        }
        saida.println("  Janela da previsao: " + configuracao.getJanelaPrevisao()
                + " leituras | Minimo para prever: " + configuracao.getMinimoLeituras() + " leituras");
        saida.println();
        saida.println(" 1 - Alterar populacao exposta");
        saida.println(" 2 - Alterar limiares de um tipo de sensor");
        saida.println(" 3 - Alterar janela da previsao");
        saida.println(" 4 - Alterar minimo de leituras para prever");
        saida.println(" 0 - Voltar");
        int escolha = lerInteiro("Escolha: ");
        switch (escolha) {
            case 1 -> {
                int populacao = lerInteiro("Nova populacao exposta (estimativa): ");
                dados.setPopulacaoExposta(populacao);
                saida.println("Populacao exposta atualizada para " + dados.getPopulacaoExposta() + ".");
            }
            case 2 -> alterarLimiares(dados);
            case 3 -> {
                int janela = lerInteiro("Nova janela (2 a 10): ");
                configuracao.setJanelaPrevisao(janela);
                saida.println("Janela da previsao atualizada para " + janela + ".");
            }
            case 4 -> {
                int minimo = lerInteiro("Novo minimo de leituras (ao menos 3): ");
                configuracao.setMinimoLeituras(minimo);
                saida.println("Minimo de leituras atualizado para " + minimo + ".");
            }
            case 0 -> saida.println("Voltando ao menu.");
            default -> saida.println("[AVISO] Opcao invalida.");
        }
    }

    private void alterarLimiares(DadosTerritorio dados) throws DadosInvalidosException {
        TipoSensor[] tipos = TipoSensor.values();
        for (int i = 0; i < tipos.length; i++) {
            saida.println(" " + (i + 1) + " - " + tipos[i].getNome() + " (" + tipos[i].getUnidade() + ")");
        }
        int escolha = lerInteiro("Tipo de sensor: ");
        if (escolha < 1 || escolha > tipos.length) {
            saida.println("[AVISO] Tipo de sensor invalido.");
            return;
        }
        TipoSensor tipo = tipos[escolha - 1];
        double atencao = lerDecimal("Novo limiar de ATENCAO: ");
        double alerta = lerDecimal("Novo limiar de ALERTA: ");
        double critico = lerDecimal("Novo limiar CRITICO: ");
        dados.definirLimiares(tipo, atencao, alerta, critico);
        saida.println("Limiares de " + tipo.getNome() + " atualizados.");
    }

    // ------------------------------------------------------------ 5 - status completo

    private void statusCompleto() throws FonteIndisponivelException {
        RelatorioBuilder relatorio = RelatorioBuilder.novo()
                .comTitulo("STATUS COMPLETO DO TERRITORIO")
                .comTerritorio(configuracao.getDadosTerritorio())
                .comPontos(pontos);
        for (PontoMonitoramento ponto : pontos) {
            String id = ponto.getSensor().getId();
            relatorio.comUltimasMedicoes(id, servico.consultarUltimas(id, 5));
        }
        relatorio.comAlertas(servico.consultarAlertas(), 5);
        saida.println();
        saida.println(relatorio.construir());
    }

    // ------------------------------------------------------- 6 - leitura manual

    private void registrarLeituraManual() throws DadosInvalidosException, FonteIndisponivelException {
        PontoMonitoramento ponto = escolherPonto();
        if (ponto == null) {
            return;
        }
        double valor = lerDecimal("Valor lido (" + ponto.getSensor().getTipo().getUnidade() + "): ");
        Medicao medicao = servico.registrarLeituraManual(ponto.getSensor(), valor);
        saida.println("Leitura manual gravada: " + medicao);
    }

    // ----------------------------------------------------- 7 - rodadas simuladas

    private void gerarRodadasSimuladas() {
        int rodadas = lerInteiro("Quantas rodadas de leituras (1 a 100)? ");
        if (rodadas < 1 || rodadas > 100) {
            saida.println("[AVISO] Informe um numero de rodadas entre 1 e 100.");
            return;
        }
        int gravadas = 0;
        int falhas = 0;
        for (int i = 0; i < rodadas; i++) {
            for (PontoMonitoramento ponto : pontos) {
                try {
                    servico.registrarLeitura(ponto.getSensor());
                    gravadas++;
                } catch (DadosInvalidosException | FonteIndisponivelException e) {
                    falhas++;
                    saida.println("  [FALHA] " + e.getMessage());
                }
            }
        }
        saida.println(gravadas + " leitura(s) gravada(s) e " + falhas + " falha(s) em " + rodadas + " rodada(s).");
    }

    // ------------------------------------------------------------ 8 - exportar CSV

    private void exportarCsv() throws FonteIndisponivelException {
        String nome = lerTexto("Nome do arquivo (Enter para historico.csv): ");
        if (nome == null) {
            return;
        }
        if (nome.isBlank()) {
            nome = "historico.csv";
        }
        List<Medicao> todas = servico.consultarTodasMedicoes();
        ExportadorCsv.exportar(todas, Path.of(nome.trim()));
        saida.println(todas.size() + " medicao(oes) exportada(s) para " + nome.trim() + ".");
    }

    // ------------------------------------------------------------------ entrada

    private PontoMonitoramento escolherPonto() {
        for (int i = 0; i < pontos.size(); i++) {
            PontoMonitoramento p = pontos.get(i);
            saida.println(" " + (i + 1) + " - " + p.getNome() + " [sensor " + p.getSensor().getId() + "]");
        }
        int escolha = lerInteiro("Escolha o ponto: ");
        if (escolha < 1 || escolha > pontos.size()) {
            saida.println("[AVISO] Ponto invalido.");
            return null;
        }
        return pontos.get(escolha - 1);
    }

    private String lerTexto(String mensagem) {
        saida.print(mensagem);
        saida.flush();
        if (!entrada.hasNextLine()) {
            entradaEncerrada = true;
            return null;
        }
        return entrada.nextLine();
    }

    /** Le um inteiro; entrada nao numerica vira -1 (e uma mensagem clara) em vez de derrubar o programa. */
    private int lerInteiro(String mensagem) {
        String texto = lerTexto(mensagem);
        if (texto == null) {
            return -1;
        }
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            saida.println("[AVISO] Entrada invalida: \"" + texto.trim() + "\" nao e um numero inteiro.");
            return -1;
        }
    }

    /** Le um decimal aceitando virgula ou ponto; entrada invalida vira NaN (a validacao do dominio rejeita). */
    private double lerDecimal(String mensagem) {
        String texto = lerTexto(mensagem);
        if (texto == null) {
            return Double.NaN;
        }
        try {
            return Double.parseDouble(texto.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            saida.println("[AVISO] Entrada invalida: \"" + texto.trim() + "\" nao e um numero.");
            return Double.NaN;
        }
    }
}
