# Territory Predictive Insights (Fev) — Global Solution 2º/2026 · POO

> Tema **CitizenTech** · Recorte escolhido: **🌊 Clima local — chuva intensa e risco de alagamento em pontos vulneráveis**

**Equipe:** _(preencher: Nome 1, Nome 2, Nome 3)_ · **Turma:** Fevereiro · **Professor:** _(preencher)_

---

## 1. Público beneficiado e decisão apoiada

**Quem sofre o problema:** moradores de áreas sujeitas a alagamento (margens de córrego, travessias baixas e encostas), e a associação de bairro / defesa civil local que precisa avisá-los.

**Decisão que o sistema apoia:** *"Devemos avisar os moradores e preparar as equipes hoje, ou ainda dá para só acompanhar?"*. O sistema lê sensores de **chuva (mm/h)**, **nível do córrego (cm)** e **umidade do solo (%)**, guarda o histórico em banco de dados, prevê o valor das próximas leituras e emite alertas **ATENÇÃO / ALERTA / CRÍTICO** explicando o porquê. A decisão final continua sendo da equipe responsável.

> O território é descrito por região e equipamento (ex.: "Ponte do Córrego"), nunca por pessoas. Nenhum dado pessoal é usado.

---

## 2. Como executar (passo a passo)

**Requisitos:** JDK 17 ou superior e Maven 3.8+ (ou uma IDE que importe projetos Maven, como o IntelliJ).

```bash
# 1) Rodar os testes
mvn test

# 2) Rodar o sistema (banco H2 em arquivo, na pasta ./dados — os dados sobrevivem ao reiniciar)
mvn compile exec:java

# 2b) Alternativa: banco H2 só em memória (nada é gravado em disco)
mvn compile exec:java -Dexec.args="--memoria"
#   No Windows PowerShell use:  mvn compile exec:java "-Dexec.args=--memoria"
```

**Banco de dados:** H2 embutido, **não é preciso instalar nada**. As tabelas `medicao` e `alerta` são criadas automaticamente ao iniciar (`ConexaoBanco.inicializarEsquema`). O mesmo script está em [`docs/schema.sql`](docs/schema.sql).

**Roteiro rápido de demonstração no menu:**
1. `7` → gerar 10 rodadas de leituras simuladas (cria histórico no banco);
2. `2` → rodar previsões (opção "Todas") e ver os alertas explicados;
3. `3` → ver histórico por sensor e os alertas já emitidos;
4. `6` → registrar leitura manual `-5` e ver a `DadosInvalidosException` tratada;
5. `5` → status completo; `8` → exportar CSV (bônus); `0` → sair.
6. Rode de novo `mvn compile exec:java`: o histórico continua lá (persistência).

> Dica: se os acentos aparecerem estranhos no console do Windows, rode `chcp 65001` antes.

---

## 3. Estrutura do projeto

```
territory-predictive-insights-fev/
├── pom.xml
├── docs/schema.sql
├── src/main/java/
│   ├── dominio/      ComponenteTerritorial, PontoMonitoramento, Sensor (interface),
│   │                 SensorPluviometro, SensorNivelRio, SensorUmidadeSolo, DadosTerritorio,
│   │                 EstrategiaPrevisao, PrevisaoMediaMovel, PrevisaoComparacao,
│   │                 Medicao, Alerta, NivelAlerta, TipoSensor, StatusComponente,
│   │                 ResultadoPrevisao, Formato
│   ├── excecao/      DadosInvalidosException, FonteIndisponivelException
│   ├── persistencia/ ConexaoBanco, MedicaoDAO, AlertaDAO   (caminho JDBC)
│   ├── servico/      ServicoMonitoramento, FabricaPrevisao, ConfiguracaoSistema,
│   │                 RelatorioBuilder, TipoPrevisao, ExportadorCsv
│   └── SistemaMonitoramento.java   (menu principal)
└── src/test/java/    dominio/*Test, servico/*Test   (JUnit 5 + Mockito)
```

---

## 4. Decisões da equipe

| Decisão | Por quê |
|---|---|
| **JDBC + H2** (em vez de Spring/JPA) | Menos "mágica": fica claro o que cada `PreparedStatement` faz, o programa roda com um comando e o H2 não exige instalação. |
| **Exceções checadas** (`extends Exception`) | O compilador obriga a tratar o erro; todo ponto de uso decide o que fazer e o menu mostra a mensagem. |
| **DAO recebe `ConexaoBanco`** e o serviço recebe os DAOs no construtor | Permite trocar os DAOs por *mocks* nos testes: a lógica de alerta é testada sem banco. |
| **Sensores simulados** com `Random` (probabilidade de falha de 5%) | O enunciado permite simulação; a falha ocasional exercita `FonteIndisponivelException`. |
| **Sistema se cala** com menos de 5 leituras | Prever com poucos dados é "chute" (ver reflexão crítica). |
| Previsão limitada à faixa física do sensor | Evita prever, por exemplo, umidade de 116%. |

### Mapa: requisito do enunciado → onde está

| Requisito | Onde |
|---|---|
| Classe abstrata `ComponenteTerritorial` + método abstrato `verificarStatus()` + filha concreta | `dominio/ComponenteTerritorial`, `dominio/PontoMonitoramento` |
| Interface `Sensor` com 3 implementações | `dominio/Sensor` → `SensorPluviometro`, `SensorNivelRio`, `SensorUmidadeSolo` |
| `DadosTerritorio` com atributos privados e acesso para alterar limiares | `dominio/DadosTerritorio` (`definirLimiares`, setters validando) |
| `EstrategiaPrevisao` abstrata com 2 filhas sobrescrevendo `prever()` e usando `super` | `PrevisaoMediaMovel`, `PrevisaoComparacao` (chamam `super(...)` no construtor e `super.prever(...)`) |
| Escolha da estratégia **sem** `if tipo ==` / `instanceof` | `FabricaPrevisao` (mapa `TipoPrevisao → construtor`) e polimorfismo no serviço |
| 2 exceções customizadas, lançadas em setters/serviços | `excecao/*`; usadas em `Medicao`, `DadosTerritorio`, `ConfiguracaoSistema`, `EstrategiaPrevisao`, `ServicoMonitoramento`, sensores e DAOs |
| Captura no menu com mensagem clara, sem `catch` vazio | `SistemaMonitoramento` |
| Persistência JDBC: `MedicaoDAO` e `AlertaDAO` com `PreparedStatement` | `persistencia/*` — nenhum SQL é montado por concatenação com dados; recursos fechados com *try-with-resources* |
| Gravar cada medição e cada alerta; listar histórico e alertas no menu | `ServicoMonitoramento` + menu (opções 1, 2, 3, 7) |
| Padrões criacionais (Factory, Singleton, Builder) | `FabricaPrevisao`, `ConfiguracaoSistema`, `RelatorioBuilder` |
| ≥ 5 testes AAA, `assertThrows`, Mockito | `src/test/java` (ver seção 6) |
| Reflexão crítica, uso de IA, divisão de tarefas | seções 7, 8 e 9 deste README |
| Bônus (apenas 1): exportar histórico em CSV | menu opção 8 → `servico/ExportadorCsv` |

---

## 5. Padrões criacionais — onde foram usados e por quê

Foram aplicados **os três**, cada um onde resolve um problema real:

- **Factory — `FabricaPrevisao`.** O serviço e o menu precisam de "uma estratégia de previsão", mas não devem conhecer as classes concretas. A fábrica cria a estratégia a partir de um `TipoPrevisao` (ou da configuração) usando um mapa de construtores, **sem `if` espalhado**. Também garante a regra "mínimo de leituras > janela". *Por que ali e não em outro lugar:* é o único ponto em que as classes concretas são instanciadas; para criar uma 3ª estratégia basta registrá-la na fábrica, sem mexer no menu nem no serviço.
- **Singleton — `ConfiguracaoSistema`.** Limiares, janela e mínimo de leituras precisam ser **os mesmos** para o menu, o serviço e a fábrica; duas configurações diferentes gerariam alertas inconsistentes. Construtor privado + `getInstance()` garantem uma única instância. *Por que não em outro lugar:* só a configuração precisa ser única; sensores, DAOs e estratégias não.
- **Builder — `RelatorioBuilder`.** O relatório de status tem partes opcionais (território, pontos, medições, alertas). Montar passo a passo (`comTerritorio(...).comPontos(...).construir()`) evita um método com muitos parâmetros e permite relatórios parciais.

O código **chama** os padrões (`FabricaPrevisao.criar(...)`, `ConfiguracaoSistema.getInstance()`, `RelatorioBuilder.novo()`); a lógica de criação não é duplicada.

---

## 6. Testes

Comando: `mvn test`. São **54 testes** (JUnit 5), todos no padrão **AAA** (Arrange, Act, Assert), cobrindo:

- **Medição e validação** — `MedicaoTest` (inclui `assertThrows(DadosInvalidosException.class, …)` para chuva negativa);
- **Estratégias** — `PrevisaoMediaMovelTest`, `PrevisaoComparacaoTest` (valores calculados à mão; se o cálculo errar, o teste fica vermelho);
- **Regras do território** — `DadosTerritorioTest` (limiares inválidos, classificação dos níveis);
- **Sensores e componentes** — `SensorTest`, `ComponenteTerritorialTest` (inclui `FonteIndisponivelException`);
- **Padrões** — `FabricaPrevisaoTest`, `ConfiguracaoSistemaTest`, `RelatorioBuilderTest`;
- **Lógica de alerta com Mockito** — `ServicoMonitoramentoTest`: `MedicaoDAO`, `AlertaDAO` e `Sensor` são *mocks*; testa ATENÇÃO/ALERTA/CRÍTICO, ausência de alerta, histórico insuficiente, clamp na faixa física e que medição inválida **não** chega ao banco (`verify(..., never())`).

---

## 7. Reflexão crítica

**Quando o sistema deve se calar?** Quando não há dados suficientes para sustentar a previsão. Com menos de 5 leituras no banco (ou logo após uma falha prolongada de sensor) uma média móvel é praticamente um chute: um único valor atípico domina o resultado e pode gerar um alerta falso — ou, pior, esconder um problema real. Por isso o sistema lança `DadosInvalidosException("histórico insuficiente…")`, explica o motivo e **não emite alerta**. Um alarme falso repetido também desgasta a confiança da comunidade, e então o alerta verdadeiro deixa de ser levado a sério.

**O que os dados não representam?** Os valores são simulados e cada sensor representa um único ponto: um pluviômetro não descreve a chuva sobre o bairro inteiro, nem o nível do córrego num ponto diz como está a travessia a 500 m dali. As previsões são simples (média e tendência) e não consideram previsão meteorológica, relevo, drenagem nem a chuva acumulada dos dias anteriores. O sistema também não sabe nada sobre as pessoas, a infraestrutura de escoamento ou a capacidade de resposta local.

**Por que o alerta apoia a decisão em vez de decidir?** Porque quem conhece o território, os moradores mais vulneráveis e os recursos disponíveis é a equipe local. Cada alerta explica *qual sensor*, *qual valor previsto*, *qual limite foi atingido* e *qual estratégia* gerou o número, e traz apenas uma **sugestão**. Assim a pessoa pode conferir se faz sentido (ex.: sensor com defeito, leitura isolada), ponderar o contexto e assumir a responsabilidade pela decisão, que envolve riscos reais para pessoas.

---

## 8. Uso de IA

> ⚠️ **A equipe deve revisar e ajustar este texto para que reflita exatamente o que fez.**

Foi utilizada IA generativa (**Claude, da Anthropic**) como apoio. A IA foi usada para: analisar o enunciado, propor a arquitetura em camadas, gerar uma primeira versão do código, dos testes e deste README, e verificar o cumprimento dos requisitos.

**Como a equipe revisou:** _(preencher com o que realmente foi feito — por exemplo: lemos todas as classes; executamos `mvn test`; rodamos o menu e testamos cada opção; alteramos X; cada integrante estudou e consegue explicar o trecho Y.)_

Todos os integrantes devem ser capazes de explicar qualquer parte do código na conversa de correção.

---

## 9. Divisão de tarefas

| Integrante | Responsabilidade |
|---|---|
| _(Nome 1)_ | Domínio e exceções (`dominio/`, `excecao/`) |
| _(Nome 2)_ | Persistência (`persistencia/`, `ConexaoBanco`, `docs/schema.sql`) |
| _(Nome 3)_ | Previsões, alertas e testes (`EstrategiaPrevisao`, `ServicoMonitoramento`, `src/test`) |

Todos os integrantes commitam no GitHub, com mensagens claras.

---

## 10. Bônus (+0,5)

**Exportação do histórico consultado em arquivo CSV** — menu opção 8 (`ExportadorCsv`). Separador `;` e vírgula decimal, abre direto no Excel em português.
