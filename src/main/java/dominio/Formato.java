package dominio;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Utilitario de formatacao (padrao brasileiro) usado em mensagens e relatorios. */
public final class Formato {

    public static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private Formato() {
    }

    /** Numero com uma casa decimal e virgula: 12,5 */
    public static String numero(double valor) {
        return String.format(PT_BR, "%.1f", valor);
    }

    public static String dataHora(LocalDateTime instante) {
        return instante.format(DATA_HORA);
    }
}
