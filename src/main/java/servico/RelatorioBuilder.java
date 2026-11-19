package servico;

import dominio.Alerta;
import dominio.DadosTerritorio;
import dominio.Formato;
import dominio.Medicao;
import dominio.NivelAlerta;
import dominio.PontoMonitoramento;
import dominio.TipoSensor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * PADRAO BUILDER: o relatorio de status tem varias partes opcionais (territorio, pontos,
 * ultimas medicoes, alertas). Em vez de um metodo gigante com muitos parametros, o relatorio
 * e montado passo a passo e so e gerado em construir().
 */
public class RelatorioBuilder {

    private String titulo = "RELATORIO DE STATUS";
    private DadosTerritorio territorio;
    private List<PontoMonitoramento> pontos = new ArrayList<>();
    private final Map<String, List<Medicao>> ultimasMedicoes = new LinkedHashMap<>();
    private List<Alerta> alertas = new ArrayList<>();
    private int limiteAlertasExibidos = 10;

    public static RelatorioBuilder novo() {
        return new RelatorioBuilder();
    }

    public RelatorioBuilder comTitulo(String titulo) {
        if (titulo != null && !titulo.isBlank()) {
            this.titulo = titulo;
        }
        return this;
    }

    public RelatorioBuilder comTerritorio(DadosTerritorio territorio) {
        this.territorio = territorio;
        return this;
    }

    public RelatorioBuilder comPontos(List<PontoMonitoramento> pontos) {
        this.pontos = new ArrayList<>(pontos);
        return this;
    }

    public RelatorioBuilder comUltimasMedicoes(String sensorId, List<Medicao> medicoes) {
        this.ultimasMedicoes.put(sensorId, new ArrayList<>(medicoes));
        return this;
    }

    public RelatorioBuilder comAlertas(List<Alerta> alertas, int limiteExibidos) {
        this.alertas = new ArrayList<>(alertas);
        this.limiteAlertasExibidos = limiteExibidos;
        return this;
    }

    public String construir() {
        StringBuilder sb = new StringBuilder();
        String linha = "=".repeat(64);
        sb.append(linha).append('\n');
        sb.append(' ').append(titulo).append('\n');
        sb.append(" Gerado em: ").append(Formato.dataHora(LocalDateTime.now())).append('\n');
        sb.append(linha).append('\n');

        if (territorio != null) {
            sb.append("\nTERRITORIO\n");
            sb.append("  Nome: ").append(territorio.getNome()).append('\n');
            sb.append("  Regiao: ").append(territorio.getRegiao()).append('\n');
            sb.append("  Populacao exposta (estimativa): ")
                    .append(String.format(Formato.PT_BR, "%,d", territorio.getPopulacaoExposta())).append('\n');
            sb.append("  Limiares configurados:\n");
            for (TipoSensor tipo : TipoSensor.values()) {
                DadosTerritorio.Limiares l = territorio.getLimiares(tipo);
                sb.append(String.format("    %-16s (%s): %s %s | %s %s | %s %s%n",
                        tipo.getNome(), tipo.getUnidade(),
                        NivelAlerta.ATENCAO.getRotulo(), Formato.numero(l.atencao()),
                        NivelAlerta.ALERTA.getRotulo(), Formato.numero(l.alerta()),
                        NivelAlerta.CRITICO.getRotulo(), Formato.numero(l.critico())));
            }
        }

        sb.append("\nPONTOS DE MONITORAMENTO\n");
        if (pontos.isEmpty()) {
            sb.append("  (nenhum ponto cadastrado)\n");
        }
        for (PontoMonitoramento p : pontos) {
            p.verificarStatus();
            sb.append(String.format("  [%s] %s - sensor %s (%s), regiao %s%n",
                    p.getStatus(), p.getNome(), p.getSensor().getId(),
                    p.getSensor().getTipo().getNome(), p.getRegiao()));
        }

        sb.append("\nULTIMAS MEDICOES POR SENSOR\n");
        if (ultimasMedicoes.isEmpty()) {
            sb.append("  (sem medicoes)\n");
        }
        for (Map.Entry<String, List<Medicao>> entrada : ultimasMedicoes.entrySet()) {
            List<Medicao> lista = entrada.getValue();
            if (lista.isEmpty()) {
                sb.append("  ").append(entrada.getKey()).append(": sem leituras gravadas\n");
                continue;
            }
            StringBuilder valores = new StringBuilder();
            for (Medicao m : lista) {
                if (valores.length() > 0) {
                    valores.append("; ");
                }
                valores.append(Formato.numero(m.getValor()));
            }
            sb.append("  ").append(entrada.getKey()).append(" (").append(lista.get(0).getTipo().getUnidade())
                    .append("): ").append(valores).append('\n');
        }

        sb.append("\nALERTAS EMITIDOS\n");
        if (alertas.isEmpty()) {
            sb.append("  (nenhum alerta emitido ate agora)\n");
        } else {
            int inicio = Math.max(0, alertas.size() - limiteAlertasExibidos);
            sb.append("  Total: ").append(alertas.size()).append(" (mostrando os ")
                    .append(alertas.size() - inicio).append(" mais recentes)\n");
            for (int i = inicio; i < alertas.size(); i++) {
                sb.append("  - ").append(alertas.get(i)).append('\n');
            }
        }
        sb.append('\n').append(linha).append('\n');
        sb.append(" O sistema apoia a decisao; quem decide e a equipe responsavel.\n");
        sb.append(linha).append('\n');
        return sb.toString();
    }
}
