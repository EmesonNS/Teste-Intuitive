package com.intuitive.etl.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.intuitive.etl.model.Operadora;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class OperadoraService {
    private static final String BASE_URL = "https://dadosabertos.ans.gov.br/FTP/PDA/operadoras_de_plano_de_saude_ativas/";
    private static final String DATA_DIR = "/app/data/auxiliary";
    private static final String CADASTRO_FILE = DATA_DIR + "/operadoras.csv";

    private Map<String, Operadora> cacheOperadoras = new HashMap<>();

    public void carregarDados() {
        System.out.println("=== Carregando Dados Cadastrais das Operadoras ===");
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
            if (!new File(CADASTRO_FILE).exists()) {
                baixarArquivoCadastro();
            }

            parseCadastroCsv();
            System.out.println("Total de operadoras carregadas em memória: " + cacheOperadoras.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Operadora getOperadora(String registroAns) {
        return cacheOperadoras.get(registroAns);
    }

    private void baixarArquivoCadastro() throws IOException {
        System.out.println("Buscando CSV de operadoras ativas...");
        Document doc = Jsoup.connect(BASE_URL).get();
        Element link = doc.select("a[href$='.csv']").first();

        if (link != null) {
            String csvUrl = link.attr("abs:href");
            System.out.println("Baixando: " + csvUrl);
            FileUtils.copyURLToFile(URI.create(csvUrl).toURL(), new File(CADASTRO_FILE), 10000, 10000);
        } else {
            throw new FileNotFoundException("Não foi possível encontrar o CSV de operadora a página da ANS.");
        }
    }

    private void parseCadastroCsv() throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(CADASTRO_FILE), StandardCharsets.UTF_8))) {
            CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
            CSVReader reader = new CSVReaderBuilder(br).withCSVParser(parser).withSkipLines(1).build();

            String[] line;
            while ((line = reader.readNext()) != null) {
                if (line.length < 3) continue;

                String reg = line[0];
                String cnpj = line[1];
                String razao = line[2];
                String modalidade = line.length > 4 ? line[4] : "";
                String uf = line.length > 10 ? line[10] : "";

                cnpj = cnpj.replaceAll("\\D", "");

                Operadora op = new Operadora(reg, cnpj, razao, uf, modalidade);
                cacheOperadoras.put(reg, op);
            }
        } catch (Exception e) {
            System.err.println("Erro ao ler CSV de operadoras: " + e.getMessage());
        }
    }
}
