package com.intuitive.etl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class CsvProcessor implements Processor {
    @Override
    public boolean canProcess(File file) {
        return file.getName().toLowerCase().endsWith(".csv");
    }

    @Override
    public int process(File inputFile, Writer outputWriter) throws Exception {
        Charset charset = StandardCharsets.UTF_8;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), charset))) {
            CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
            CSVReader reader = new CSVReaderBuilder(br).withCSVParser(parser).build();

            String[] header = reader.readNext();
            if (header == null) return 0;

            Map<String, Integer> colMap = mapColumns(header);

            if (!colMap.containsKey("CONTA") || !colMap.containsKey("VALOR")) {
                System.err.println("Ignorando " + inputFile.getName() + ": Colunas obrigatórias não encontradas. Header: " +  Arrays.toString(header));
                return 0;
            }

            String[] line;
            int count = 0;

            String fileName = inputFile.getName();
            String trimestre = fileName.substring(0, 2);
            String ano = fileName.substring(2, 6);

            while ((line = reader.readNext()) != null) {
                if (line.length <= colMap.get("VALOR")) continue;

                String conta = line[colMap.get("CONTA")];

                if (conta != null && conta.startsWith("4")) {
                    String regAns = colMap.containsKey("REG_ANS") ? line[colMap.get("REG_ANS")] : "";
                    String valor = line[colMap.get("VALOR")];
                    String descricao = colMap.containsKey("DESCRICAO") ? line[colMap.get("DESCRICAO")] : "";

                    valor = valor.replace(".", "").replace(",", ".");

                    String csvLine = String.format("%s;%s;%s;%s;%s;%s\n",
                        regAns, "", trimestre, ano, valor, descricao);

                    outputWriter.write(csvLine);
                    count++;
                }
            }
            return count;
        }
    }

    private Map<String, Integer> mapColumns(String[] header) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            String col = header[i].toUpperCase().trim();
            if (col.equals("CD_CONTA_CONTABIL") || col.equals("CD_CONTA")) map.put("CONTA", i);
            if (col.equals("VL_SALDO_FINAL") || col.equals("VALOR")) map.put("VALOR", i);
            if (col.equals("REG_ANS")) map.put("REG_ANS", i);
            if (col.equals("DESCRICAO")) map.put("DESCRICAO", i);
        }
        return map;
    }
}
