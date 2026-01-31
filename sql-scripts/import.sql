-- Script de Carga de Dados

TRUNCATE TABLE despesas_detalhadas CASCADE;
TRUNCATE TABLE operadoras CASCADE;
TRUNCATE TABLE despesas_agregadas;

CREATE TEMP TABLE temp_import_full (
    reg_ans VARCHAR,
    cnpj VARCHAR,
    razao VARCHAR,
    modalidade VARCHAR,
    uf VARCHAR,
    trimestre VARCHAR,
    ano INT,
    valor DECIMAL,
    descricao TEXT,
    cnpj_valido BOOLEAN
);

COPY temp_import_full FROM '/data_import/consolidado_despesas_final.csv' WITH (FORMAT CSV, HEADER true, DELIMITER ';', ENCODING 'UTF8');

INSERT INTO operadoras (registro_ans, cnpj, razao_social, modalidade, uf)
SELECT DISTINCT reg_ans, cnpj, razao, modalidade, uf
FROM temp_import_full
ON CONFLICT (registro_ans) DO NOTHING;

INSERT INTO despesas_detalhadas (registro_ans, trimestre, ano, valor, descricao)
SELECT
    reg_ans,
    CAST(REPLACE(trimestre, 'T', '') AS INTEGER),
    ano,
    valor,
    descricao
FROM temp_import_full;

DROP TABLE temp_import_full;

COPY despesas_agregadas FROM '/data_import/despesas_agregadas.csv' WITH (FORMAT CSV, HEADER true, DELIMITER ';', ENCODING 'UTF8');