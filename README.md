# Teste Técnico - Intuitive Care (Estágio)

Este repositório contém a solução para o desafio técnico de admissão da Intuitive Care. O projeto implementa uma arquitetura de **Microserviços "Light" (Container-based)**, orquestrada via Docker Compose, utilizando uma abordagem **Poliglota** para aproveitar o melhor dos ecossistemas Java e Python.

---

## 🚀 Como Executar o Projeto

O projeto é "cloud-native ready", rodando isoladamente em qualquer ambiente com Docker.

### Pré-requisitos

* Docker e Docker Compose instalados.

### Passo a Passo

1. **Clone o repositório:**
```bash
git clone https://github.com/EmesonNS/Teste-Intuitive
cd Teste_Intuitive

```


2. **Configure o ambiente:**
Verifique se o arquivo `.env` está presente na raiz (baseado no `.env.example`).
3. **Suba a infraestrutura:**
```bash
docker-compose up --build

```


*O ETL iniciará automaticamente após o banco de dados estar saudável (Healthcheck).*

4. **Verifique os Resultados (Arquivos):**
Os arquivos gerados pelo ETL estarão na pasta local `./data` (mapeada via volume):
* `data/raw`: ZIPs originais da ANS.
* `data/extracted`: CSVs extraídos.
* `data/auxiliaty`: CSVs auxiliareas para dados de operadoras.
* `consolidado_despesas.csv`: Arquivo intermediario de despesas.
* `consolidado_despesas_final.csv`: Arquivo unificado e enriquecido.
* `despesas_agregadas.csv`: Relatório estatístico.

---

## 🏗️ Arquitetura da Solução

A solução foi desenhada para desacoplar responsabilidades, garantindo que o processamento pesado não impacte a performance da API de consulta.

### Visão Geral dos Serviços

1. **Service A: ETL Worker (Java 21)**     * **Responsabilidade:** Etapas 1 e 2 (Extração, Transformação, Carga e Cálculos Estatísticos).
* **Por que Java?** Escolhido pela robustez em manipulação de I/O, tipagem forte e eficiência de memória (Streams) para processar grandes volumes de arquivos CSV/ZIP.
* **Design Pattern:** Utiliza o **Strategy Pattern** (`Processor` interface) para permitir a extensão fácil para novos formatos de arquivo futuros (XLSX, TXT) sem alterar a lógica principal.


2. **Service B: Backend API (Python + FastAPI)** *(Etapa 4)*
* **Responsabilidade:** Expor os dados processados para a web.
* **Por que Python com FastAPI?** Escolhido pela agilidade de desenvolvimento e performance assíncrona do FastAPI. Facilita a integração futura com bibliotecas de Data Science (Pandas/NumPy) se necessário.


3. **Service C: Database (PostgreSQL 13)** *(Etapa 3)*
* **Responsabilidade:** Persistência relacional e analítica.
* **Por que Postgres?** Preferido por sua robustez em queries complexas e suporte superior a JSONB em comparação ao MySQL.


4. **Service D: Frontend (Vue.js)** *(Etapa 4)*
* **Responsabilidade:** Interface do usuário e Visualização de Dados.

### Organização do Projeto

```text
Teste_SeuNome/
├── docker-compose.yml          # Orquestra tudo
├── README.md                   # Documentação
├── data/                       # Volume compartilhado para os CSVs gerados
├── etl-worker/                 # Projeto Java (Maven)
│   ├── Dockerfile
│   └── src/
├── backend-api/                # Projeto Python (FastAPI)
│   ├── Dockerfile
│   ├── requirements.txt
│   └── app/
├── frontend-web/               # Projeto Vue.js
│   ├── Dockerfile
│   └── src/
└── sql-scripts/                # Scripts da Etapa 3
    └── init.sql

```


### Fluxo de Dados e Comunicação

Optou-se pelo princípio **KISS (Keep It Simple, Stupid)**:

* **Comunicação:** Ao invés de introduzir complexidade operacional com filas (Kafka/RabbitMQ) para um teste de curto prazo, os serviços se comunicam via **Estado Compartilhado**:
1. **Volume Docker:** Compartilhamento de arquivos brutos/processados entre serviços.
2. **Banco de Dados:** O Java escreve, o Python lê.

---

## 📂 Organização do Código (Java ETL)

O módulo Java segue uma **Layered Architecture** para garantir separação de interesses:

```text
src/main/java/com/intuitive/etl/
├── model/       # Domínio (POJOs anêmicos)
├── service/     # Regras de Negócio (Orquestração, Scraping, Join)
├── processor/   # Strategy Pattern (Parsing de CSV/Excel)
└── util/        # Ferramentas auxiliares (Validação CNPJ)

```

---

## 🧠 Decisões Técnicas e Trade-offs (Documentação)

Respostas aos questionamentos específicos do PDF para as Fases 1 e 2.

### FASE 1: Integração e Processamento

#### 1.1. Resiliência a Variações de Diretório

* **Desafio:** A estrutura de pastas da ANS muda com o tempo.
* **Solução:** Implementação de um **Scraper Recursivo (Jsoup)**. O sistema não usa URLs fixas; ele navega na árvore HTML, identifica os anos disponíveis e busca arquivos ZIP dentro deles, independentemente se estão na raiz do ano ou em subpastas.

#### 1.2. Trade-off: Memória vs. Incremental

* **Decisão:** **Processamento Incremental (Streaming)**.
* **Justificativa:** Carregar gigabytes de CSVs na memória causaria `OutOfMemoryError`. Utilizzou-se `BufferedReader` e `OpenCSV` para ler linha a linha, processar e escrever no output imediatamente. Isso mantém o uso de RAM baixo e constante (O(1)).

#### 1.3. Tratamento de Inconsistências

* **Valores:** Mantidos valores originais (mesmo negativos), apenas normalizando a formatação decimal (pt-BR para en-US).
* **Datas:** Ignorada a data interna do CSV (frequentemente suja). A data (Trimestre/Ano) é inferida de forma confiável através do metadado do nome do arquivo (ex: `1T2025.zip`).

### FASE 2: Transformação e Validação

#### 2.1. Trade-off: Validação de CNPJ

* **Decisão:** **Flagging (Marcar) ao invés de Filtrar (Excluir)**.
* **Estratégia:** Criada coluna `CNPJ_Valido` (boolean).
* **Justificativa:** Em contabilidade, a integridade do valor total é sagrada. Descartar uma despesa válida por um erro de digitação no CNPJ alteraria o Balanço da empresa. O "Flagging" permite auditoria posterior sem corromper a soma financeira.

#### 2.2. Análise Crítica: Registros sem Match

* **Decisão:** Manter os dados financeiros.
* **Justificativa:** Marcar a razão social como "OPERADORA DESCONHECIDA/INATIVA" para não perder o valor contábil e conseguir realizar filtros posteriores para auditorias.

#### 2.2. Trade-off: Estratégia de Join (Enriquecimento)

* **Decisão:** **In-Memory Hash Join**.
* **Justificativa:** O dataset de "Operadoras Ativas" é pequeno (< 2000 registros).
* **Performance:** Carregar esse dataset em um `HashMap` permite que o enriquecimento das milhões de linhas de despesas ocorra em tempo constante O(1), sendo ordens de magnitude mais rápido que consultas repetitivas em Banco de Dados.

#### 2.3. Trade-off: Agregação Estatística

* **Decisão:** **Agregação em Memória**.
* **Justificativa:** Como filtramos apenas contas de Despesas (Classe 4), o volume final agregado (1 linha por Operadora) cabe confortavelmente na memória. Usou-se `Collections.sort` (TimSort) para ordenar e gerar o relatório final rapidamente.

---

## 🛠️ Stack Tecnológico

* **Linguagem 1:** Java 21 (ETL & Processamento)
* **Linguagem 2:** Python 3.10 (API - *Planejado*)
* **Banco:** PostgreSQL 13
* **Container:** Docker & Docker Compose
* **Libs Java:** Jsoup (Scraping), OpenCSV (Parsing), Commons-IO.