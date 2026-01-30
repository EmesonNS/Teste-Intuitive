package com.intuitive.etl.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.intuitive.etl.model.StatsAccumulator;

public class AggregationService {
    private static final String INPUT_FILE = "/app/data/consolidado_despesas_final.csv";
    private static final String OUTPUT_FILE = "/app/data/despesas_agregadas.csv";

    public void execute() {
        System.out.println("=== 4. Calculando Agregações e Estatísticas ===");

        Map<String, StatsAccumulator> mapaAgregacao = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(INPUT_FILE), StandardCharsets.UTF_8))) {
            String line = br.readLine();

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";", -1);

                if (parts.length < 8) continue;

                String razaoSocial = parts[2];
                String uf = parts[4];
                String valorStr = parts[7];

                String chave = razaoSocial + ";" + uf;

                try {
                    double valor = Double.parseDouble(valorStr);
                    mapaAgregacao.computeIfAbsent(chave, k -> new StatsAccumulator(k)).addValor(valor);
                } catch (NumberFormatException e) {
                    // Ignora valore inválidos
                }
            }

            List<Map.Entry<String, StatsAccumulator>> listaOrdenada = new ArrayList<>(mapaAgregacao.entrySet());
            listaOrdenada.sort((e1, e2) -> Double.compare(e2.getValue().getTotal(), e1.getValue().getTotal()));

            escreverArquivoAgregado(listaOrdenada);
            System.out.println("Agregação concluída: " + OUTPUT_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void escreverArquivoAgregado(List<Map.Entry<String, StatsAccumulator>> list) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(OUTPUT_FILE), StandardCharsets.UTF_8))) {
            bw.write("RazaoSocial;UF;ValorTotal;MediaTrimestral;DesvioPadrao;QtdRegistros\n");

            for (Map.Entry<String,StatsAccumulator> entry : list) {
                String chave = entry.getKey();
                StatsAccumulator stats = entry.getValue();

                String line = String.format(Locale.US, "%s;%.2f;%.2f;%.2f;%d\n",
                    chave,
                    stats.getTotal(),
                    stats.getMedia(),
                    stats.getDesvioPadrao(),
                    stats.getQtdRegistros());
                bw.write(line);
            }
        }
    }
}
