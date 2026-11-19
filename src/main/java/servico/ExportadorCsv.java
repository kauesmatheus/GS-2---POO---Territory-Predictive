package servico;

import dominio.Formato;
import dominio.Medicao;
import excecao.FonteIndisponivelException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** BONUS: exporta o historico de medicoes para CSV (separador ';' e virgula decimal, abre direto no Excel BR). */
public final class ExportadorCsv {

    private ExportadorCsv() {
    }

    public static void exportar(List<Medicao> medicoes, Path destino) throws FonteIndisponivelException {
        try (BufferedWriter escritor = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
            escritor.write("id;sensor;tipo;valor;unidade;instante");
            escritor.newLine();
            for (Medicao m : medicoes) {
                escritor.write(String.join(";",
                        String.valueOf(m.getId()),
                        m.getSensorId(),
                        m.getTipo().getNome(),
                        String.format(Formato.PT_BR, "%.1f", m.getValor()),
                        m.getTipo().getUnidade(),
                        Formato.dataHora(m.getInstante())));
                escritor.newLine();
            }
        } catch (IOException e) {
            throw new FonteIndisponivelException("Nao foi possivel gravar o arquivo " + destino + ": " + e.getMessage(), e);
        }
    }
}
