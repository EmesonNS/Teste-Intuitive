-- Criação do Schema

CREATE TABLE IF NOT EXISTS operadoras (
    registro_ans VARCHAR(20) PRIMARY KEY,
    cnpj VARCHAR(20),
    razao_social VARCHAR(255),
    modalidade VARCHAR(100),
    uf CHAR(2)
);

CREATE INDEX idx_operadoras_cnpj ON operadoras(cnpj);
CREATE INDEX idx_operadoras_razao ON operadoras(razao_social);

CREATE TABLE IF NOT EXISTS despesas_detalhadas (
    id SERIAL PRIMARY KEY,
    registro_ans VARCHAR(20) REFERENCES operadoras(registro_ans),
    trimestre INT,
    ano INT,
    valor DECIMAL(18, 2),
    descricao TEXT,
    data_carga TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_despesas_ano_trimestre ON despesas_detalhadas(ano, trimestre);

CREATE TABLE IF NOT EXISTS despesas_agregadas (
    razao_social VARCHAR(255),
    uf CHAR(2),
    valor_total DECIMAL(18, 2),
    media_trimestral DECIMAL(18, 2),
    desvio_padrao DECIMAL(18, 2),
    qtd_registros INT,

    PRIMARY KEY (razao_social, uf)
);