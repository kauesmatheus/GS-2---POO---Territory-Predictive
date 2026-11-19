-- Script SQL do banco (H2). O programa já executa isto sozinho ao iniciar
-- (ConexaoBanco.inicializarEsquema); o arquivo está aqui como documentação/alternativa.

CREATE TABLE IF NOT EXISTS medicao (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    sensor_id VARCHAR(50) NOT NULL,
    tipo      VARCHAR(30) NOT NULL,
    valor     DOUBLE      NOT NULL,
    instante  TIMESTAMP   NOT NULL
);

CREATE TABLE IF NOT EXISTS alerta (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    sensor_id      VARCHAR(50)  NOT NULL,
    tipo           VARCHAR(30)  NOT NULL,
    nivel          VARCHAR(20)  NOT NULL,
    valor_previsto DOUBLE       NOT NULL,
    limite         DOUBLE       NOT NULL,
    estrategia     VARCHAR(80)  NOT NULL,
    mensagem       VARCHAR(700) NOT NULL,
    instante       TIMESTAMP    NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_medicao_sensor ON medicao (sensor_id, id);
