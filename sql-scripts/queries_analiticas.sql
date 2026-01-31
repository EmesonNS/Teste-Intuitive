-- Queries Analíticas

-- QUERY 1: Quais as 5 operadoras com maior crescimento percentual de despesas 
--          entre o primeiro e o último trimestre analisado?
-- ----------------------------------------------------------------------------
-- Desafio: Algumas operadoras podem não ter dados em todos os trimestres.
-- Solução: Usou-se filtros para pegar especificamente o 1º (min_trimestre) e 
--          o último (max_trimestre) de cada operadora, garantindo que a comparação 
--          seja feita apenas se ambos existirem.
WITH limites_periodo AS (
    SELECT
        MIN(ano * 10 + trimestre) as min_periodo_global,
        MAX(ano * 10 + trimestre) as max_periodo_global
    FROM despesas_detalhadas
),
despesas_por_trimestre AS (
    SELECT
        d.registro_ans,
        o.razao_social,
        (d.ano * 10 + d.trimestre) as periodo_absoluto,
        SUM(d.valor) as total_trimestre
    FROM despesas_detalhadas d
    JOIN operadoras o ON d.registro_ans = o.registro_ans
    GROUP BY 1, 2, 3
),
comparativo AS (
    SELECT
        dt.registro_ans,
        dt.razao_social,
        MAX(CASE WHEN dt.periodo_absoluto = lp.min_periodo_global THEN dt.total_trimestre END) as valor_inicial,
        MAX(CASE WHEN dt.periodo_absoluto = lp.max_periodo_global THEN dt.total_trimestre END) as valor_final
    FROM despesas_por_trimestre dt
    CROSS JOIN limites_periodo lp
    GROUP BY 1, 2
)
SELECT
    registro_ans,
    razao_social,
    valor_inicial,
    valor_final,
    ROUND(((valor_final - valor_inicial) / valor_inicial) * 100, 2) as crescimento_percentual
FROM comparativo
WHERE valor_inicial > 0
    AND valor_final IS NOT NULL
ORDER BY crescimento_percentual DESC
LIMIT 5;


-- ----------------------------------------------------------------------------
-- QUERY 2: Qual a distribuição de despesas por UF? 
--          Liste os 5 estados com maiores despesas totais.
-- ----------------------------------------------------------------------------
-- Desafio Adicional: Calcular a média de despesas por operadora em cada UF.
SELECT
    o.uf,
    SUM(d.valor) as despesa_total_estado,
    COUNT(DISTINCT d.registro_ans) as qtd_operadoras,
    ROUND(SUM(d.valor) / COUNT(DISTINCT d.registro_ans), 2) as media_por_operadoras
FROM despesas_detalhadas d
JOIN operadoras o ON d.registro_ans = o.registro_ans
WHERE o.uf IS NOT NULL AND o.uf != 'ND'
GROUP BY o.uf
ORDER BY despesa_total_estado DESC
LIMIT 5;


-- ----------------------------------------------------------------------------
-- QUERY 3: Quantas operadoras tiveram despesas acima da média geral 
--          em pelo menos 2 dos 3 trimestres analisados?
-- ----------------------------------------------------------------------------
-- Trade-off: Avaliou-se as seguinte opções:
-- Opção A: Window Functions (OVER PARTITION BY) - Mais performático, mas sintaxe complexa.
-- Opção B: Subqueries Aninhadas - Difícil leitura e manutenção.
-- Opção Escolhida: CTEs (Common Table Expressions).
-- Justificativa: Priorizou-se a LEGIBILIDADE e MANUTENIBILIDADE. 
-- O uso de CTEs permite quebrar o problema em passos lógicos ('dividir para conquistar'):
-- 1. Calcular média do mercado; 2. Comparar operadora vs mercado; 3. Contar ocorrências.
-- A perda de performance em relação a Window Functions é desprezível para este volume de dados.
WITH media_por_trimestre AS (
    SELECT
        ano,
        trimestre,
        AVG(total_operadora) as media_geral_mercado
    FROM (
        SELECT ano, trimestre, registro_ans, SUM(valor) as total_operadora
        FROM despesas_detalhadas
        GROUP BY 1, 2, 3
    ) sub
    GROUP BY 1, 2
),
operadoras_acima_media AS (
    SELECT
        d.registro_ans,
        d.ano,
        d.trimestre,
        SUM(d.valor) as total_operadora,
        m.media_geral_mercado,
        CASE WHEN SUM(d.valor) > m.media_geral_mercado THEN 1 ELSE 0 END as acima_da_media
    FROM despesas_detalhadas d
    JOIN media_por_trimestre m ON d.ano = m.ano AND d.trimestre = m.trimestre
    GROUP BY 1, 2, 3, 5
)
SELECT
    COUNT(*) as qtd_operadoras_consistentes
FROM (
    SELECT registro_ans
    FROM operadoras_acima_media
    GROUP BY registro_ans
    HAVING SUM(acima_da_media) >= 2
) finais;