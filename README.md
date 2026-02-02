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


*O ETL iniciará automaticamente após o banco de dados estar saudável (Healthcheck). O sistema executará automaticamente o pipeline completo: Download -> ETL Java -> Criação do Banco -> Importação dos Dados.*

4. **Verifique os Resultados (Arquivos):**
Os arquivos gerados pelo ETL estarão na pasta local `./data` (mapeada via volume):
* `data/raw`: ZIPs originais da ANS.
* `data/extracted`: CSVs extraídos.
* `data/auxiliaty`: CSVs auxiliareas para dados de operadoras.
* `consolidado_despesas.csv`: Arquivo intermediario de despesas.
* `consolidado_despesas_final.csv`: Arquivo unificado e enriquecido.
* `despesas_agregadas.csv`: Relatório estatístico.

5. **Validação (Queries Analíticas):** Após o término do processamento (quando os containers estiverem estáveis), execute o script de validação para responder às perguntas de negócio (Item 3.4 do teste):

```bash
docker exec -i intuitive_db psql -U user_intuitive -d intuitive_db < sql-scripts/queries_analiticas.sql
```

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
    ├── init.sql                # DDL: Criação das Tabelas
    ├── import.sql              # DML: Carga dos dados (executado pelo importer)
    └── queries_analiticas.sql  # DQL: Respostas das perguntas de negócio

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

## 📂 Organização do Código (Python API)

O módulo de Backend segue uma **Arquitetura em Camadas Simplificada** (inspirada em Clean Architecture) para facilitar manutenção e testes:

```text
backend-api/app/
├── api/         # Controllers/Rotas (Endpoints HTTP)
├── core/        # Configurações globais (Env vars)
├── db/          # Configuração de conexão (Session)
├── models/      # Entidades do Banco (SQLAlchemy ORM)
├── repository/  # Camada de Acesso a Dados (Queries SQL abstraídas)
└── schemas/     # DTOs e Validação (Pydantic)
```

## 📂 Organização do Código (Frontend Vue.js)

O cliente web foi estruturado utilizando **Vue 3 (Composition API)** e **Vite**, com foco em componentização e separação de responsabilidades via Stores:

```text
frontend-web/src/
├── assets/          # Estilos globais (Tailwind CSS)
├── components/      # Componentes UI Reutilizáveis (Modal de Detalhes)
├── services/        # Camada de Comunicação HTTP (Axios Singleton)
├── stores/          # Gerenciamento de Estado Global (Pinia)
│   └── dashboard.js # Lógica de negócio (Busca, Paginação, API Calls)
├── App.vue          # Layout Principal (Dashboard, Gráficos, Tabela)
└── main.js          # Ponto de entrada
```

---

## 🧠 Decisões Técnicas e Trade-offs 

Respostas aos questionamentos específicos do PDF.

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

### FASE 3: Teste de Banco de Dados e Análise (PostgreSQL)

#### 3.1. Decisão de Infraestrutura: PostgreSQL vs MySQL
* **Decisão:** **PostgreSQL**.
* **Justificativa:**
    * **Analytics:** O PostgreSQL possui um otimizador de consultas superior para queries analíticas complexas.
    * **Tipagem e Integridade:** Oferece suporte nativo mais robusto para tipos de dados financeiros e validações de integridade (Constraints) que são vitais para dados contábeis.
    * **Escalabilidade Futura:** O suporte nativo a JSONB permite, no futuro, armazenar metadados não estruturados das operadoras sem precisar de um banco NoSQL separado (arquitetura híbrida).

#### 3.2. Trade-off técnico - Normalização

* **Decisão:** **Abordagem Híbrida**
* **Estratégia:** 
    * **Tabelas Transacionais (`despesas_detalhadas`, `operadoras`):** Totalmente normalizadas (3NF). Evita redundância de strings (Razão Social repetida milhões de vezes) e garante integridade referencial.
    * **Tabela Analítica (`despesas_agregadas`):** Desnormalizada.
* **Justificativa:** Para operações de escrita e manutenção, a normalização economiza espaço e evita anomalias de atualização. Para a leitura do Dashboard (Item 2.3), a tabela desnormalizada atua como um Data Mart, permitindo leitura instantânea sem a necessidade de JOINS custosos em tempo real.

#### 3.2. Trade-off técnico - Tipos de dados

* **Valores Monetários:** `DECIMAL(18,2)`.
* **Justificativa:** Jamais utilizar `FLOAT` ou `DOUBLE` para dinheiro devido a erros de precisão em cálculos de ponto flutuante (IEEE 754). `DECIMAL` garante a exatidão dos centavos contábeis.

* **Datas (Trimestre/Ano):** `INTEGER`.
* **Justificativa:** A fonte de dados fornece o conceito de "Trimestre" (ex: 1T, 2T) e não datas específicas. Converter para `DATE` (ex: 2023-01-01) seria semanticamente incorreto e induziria a erros de interpretação.


#### 3.3. Análise Crítica: Integridade e NULLs

Durante a importação, foi identificado um erro de **Restrição de Integridade (Not Null)** na tabela `despesas_agregadas`, causado por operadoras sem UF definida.

* **O Problema:** A UF fazia parte da Chave Primária Composta (`PRIMARY KEY (razao, uf)`), e chaves primárias não aceitam NULL.
* **A Solução:** Implementou-se no Java (`EtlService.java`) uma regra de negócio que atribui o valor padrão **"ND" (Não Definido)** para operadoras desconhecidas.
* **Justificativa:** Em sistemas financeiros, rejeitar o registro (perder o dado financeiro) é pior do que ter uma dimensão geográfica imprecisa. O uso de "ND" preserva o valor contábil total para auditoria.

#### 3.3. Automação de Carga (Docker Pattern)

Durante esta etapa encontrou-se outra barreira, porém dessa vez relacionada ao docker, o serviço de Banco de Dados subia antes dos arquivos CSV exitirem o que chashava o banco na hora de rodar a importação. Para resolver este conflito, utilizou-se o padrão de **Short-lived Container** onde criou-se um novo isolado (`intuitive_importer`) cuja unica função seria rodar o script de import após o ETL concluir a execução.

* Este container aguarda a conclusão do Java (`condition: service_completed_successfully`) e só então executa o comando `COPY`, garantindo uma orquestração livre de falhas manuais.


#### 3.4. Justificativa das Queries Analíticas

* **Query 1 (Crescimento das Operadoras):**
    * **Desafio:** Operadoras que não possuem dados em todos os trimestres.
    * **Decisão:** **Filtro Estrito de Ponta a Ponta**. Consideramos apenas operadoras que reportaram dados no *primeiro* E no *último* trimestre da análise global.
    * **Justificativa:** Para um ranking de crescimento ser justo, precisamos comparar o mesmo intervalo de tempo para todos. Uma operadora que começou a operar na metade do ano teria um "crescimento" distorcido ou incomparável com uma que operou o ano todo.

* **Query 3 (Despesas acima da Média):**
    * **Trade-off Técnico:** Performance vs Legibilidade
    * **Alternativas Consideradas:** Window Functions (`OVER PARTITION`) ou Subqueries aninhadas.
    * **Justificativa:** Embora Window Functions sejam ligeiramente mais performáticas, CTEs oferecem uma **Legibilidade** e **Manutenibilidade** superior. A query foi estruturada no padrão "Dividir para Conquistar":
        1. Calcula-se a média do mercado.
        2. Compara-se cada operadora com a média.
        3. Agrega-se o resultado final.
        
        Isso facilita a leitura por outros desenvolvedores e a depuração de erros.


### FASE 4: API e Interface Web

#### 4.2.1. Escolha do Framework: FastAPI vs Flask
* **Decisão:** **FastAPI**.
* **Justificativa:**
    * **Performance:** Utiliza ASGI (Assíncrono) nativamente, sendo muito mais performático que o Flask (WSGI) para I/O operations.
    * **Produtividade:** Validação de dados automática com Pydantic e geração automática de documentação (Swagger UI), economizando tempo de desenvolvimento manual.
    * **Tipagem:** Uso intensivo de Type Hints do Python moderno, reduzindo bugs.

#### 4.2.2. Estratégia de Paginação
* **Decisão:** **Offset-based** (Page/Limit).
* **Justificativa:**
    * **Contexto:** O volume de dados, embora grande, é estático (histórico) e ordenado.
    * **UX:** Para tabelas administrativas (Dashboard), o usuário geralmente prefere navegar por "Páginas" (1, 2, 3...) ao invés de "Carregar mais" (Cursor).
    * **Implementação:** É suportado nativamente pelo SQL (`OFFSET` / `LIMIT`) e fácil de integrar com componentes de tabela do Frontend.

#### 4.2.3. Cache vs Queries Diretas (/api/estatisticas)
* **Decisão:** **Queries Diretas (com Data Mart)**.
* **Justificativa:**
    * A opção de usar um Cache em memória (Redis) adicionaria complexidade de infraestrutura.
    * Como já criamos a tabela `despesas_agregadas` na Fase 3, ela atua como uma **Materialized View**. Consultar essa tabela é extremamente rápido (O(1) ou O(N_estados)), eliminando a necessidade de recalcular somas em milhões de linhas a cada requisição ou de gerenciar expiração de cache.

#### 4.2.4. Estrutura de Resposta da API
* **Decisão:** **Dados + Metadados (Envelope)**.
* **Justificativa:**
    * Retornar apenas a lista `[...]` impede o Frontend de saber quantas páginas existem.
    * O formato escolhido `{ data: [...], total: 100, page: 1, limit: 10 }` fornece ao componente visual todas as informações necessárias para renderizar a barra de paginação corretamente.


#### 4.3. Interface Web (Frontend)

#### 4.3.1. Estratégia de Busca/Filtro
* **Decisão:** **Busca no Servidor (Server-side)**.
* **Justificativa:**
    * Embora o dataset atual coubesse na memória do navegador, em um cenário real de operadoras de saúde, o volume de dados cresce exponencialmente. Filtrar no cliente causaria travamentos.
    * **Otimização:** Implementou-se um mecanismo de **Debounce** (atraso de 500ms) no input de busca para evitar "flooding" de requisições desnecessárias à API enquanto o usuário digita.

#### 4.3.2. Gerenciamento de Estado
* **Decisão:** **Pinia**.
* **Justificativa:**
    * A complexidade da aplicação (compartilhar filtros, paginação e dados selecionados entre a Tabela Principal e o Modal de Detalhes) exige um gerenciador de estado.
    * O Pinia foi escolhido por ser o padrão oficial do Vue 3, oferecendo melhor integração com TypeScript/IDE e uma API mais limpa (sem mutations complexas) em comparação ao Vuex.

#### 4.3.3. Performance da Tabela
* **Decisão:** **Paginação Real (Server-side)**.
* **Justificativa:** Renderizar milhares de linhas no DOM (HTML) degradaria severamente a performance do navegador. A paginação mantém o DOM leve (apenas 10 itens por vez), garantindo 60 FPS na rolagem e interação instantânea.

#### 4.3.4. Tratamento de Erros e Loading
* **Abordagem:** **Feedback Visual Contextual**.
* **Implementação:**
    * **Loading:** Em vez de bloquear a tela inteira, utilizamos *spinners* localizados (dentro do botão atualizar) ou estados de tabela específicos ("Carregando dados...").
    * **Empty States:** Mensagens amigáveis ("Nenhum registro encontrado") instruem o usuário quando uma busca não retorna resultados, melhorando a UX em comparação a uma tela em branco.
    * **Interatividade:** O botão de "Detalhes" só aparece ao passar o mouse (hover) sobre a linha, reduzindo a poluição visual e guiando a atenção do usuário.

---

## 🛠️ Stack Tecnológico

* **Linguagem 1:** Java 21 (ETL & Processamento)
* **Linguagem 2:** Python 3.10 (API - FastAPI, SQLAlchemy, Pydantic)
* **Frontend:** Vue.js 3, Vite, TailwindCSS (Estilização), Pinia (State), Chart.js (Visualização de Dados), Axios.
* **Banco:** PostgreSQL 13
* **Container:** Docker & Docker Compose
* **Libs Java:** Jsoup (Scraping), OpenCSV (Parsing), Commons-IO.
