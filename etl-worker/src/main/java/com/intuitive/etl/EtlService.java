package com.intuitive.etl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class EtlService {
    private static final String RAW_DIR = "/app/data/raw";
    private static final String EXTRACTED_DIR = "/app/data/extracted";
    private static final String OUTPUT_FILE = "/app/data/consolidado_despesas.csv";

    public void execute() {
        processarArquivosZip();
        consolidarDados();
    }    

    private void processarArquivosZip() {
        System.out.println("=== 1. Extraindo Arquivos ZIP ===");

        try {
            File rawFolder = new File(RAW_DIR);
            File[] zips = rawFolder.listFiles((dir, name) -> name.endsWith(".zip"));

            if (zips == null || zips.length == 0) {
                System.out.println("Nenhum arquivo ZIP encontrado para processar.");
                return;
            }

            for (File zipFile : zips) {
                System.out.println("Processando ZIP: " + zipFile.getName());
                unzipAndProcess(zipFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void consolidarDados() {
        System.out.println("=== 2. Consolidando Dados de Despesas ===");
        File extractedFolder = new File(EXTRACTED_DIR);
        File[] files = extractedFolder.listFiles();

        if (files == null || files.length == 0) {
            System.out.println("Nenhum arquivo extraído encontrado para consolidar.");
            return;
        }

        Processor processor = new CsvProcessor();

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(OUTPUT_FILE), StandardCharsets.UTF_8))) {
            writer.write("REG_ANS;RazaoSocial;Trimestre;Ano;Valor;Descricao\n");
            for (File file : files) {
                if (processor.canProcess(file)) {
                    System.out.println("Processando: " + file.getName() + "...");
                    int linhas = processor.process(file, writer);
                    System.out.println(linhas + " registros de despesas encontrados.");
                }
            }
            System.out.println("Consolidação concluída: " + OUTPUT_FILE);

        } catch (Exception e) {
            System.err.println("Erro na consolidação: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void unzipAndProcess(File zipFile) {
        try (ZipFile zip = new ZipFile(zipFile)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();

                if (entry.isDirectory() || !isRelevantFile(entry.getName())) {
                    continue;
                }

                System.out.println(" -> Extraindo arquivos relevantes: " + entry.getName());

                extractFile(zip, entry);
            }
        } catch (IOException e) {
            System.out.println("Erro ao ler ZIP " + zipFile.getName() + ": " + e.getMessage());
        }
    }

    private boolean isRelevantFile(String name) {
        String lower = name.toLowerCase();
        return lower.endsWith(".csv") || lower.endsWith(".xlsx") || lower.endsWith(".txt");
    }

    private void extractFile(ZipFile zip, ZipEntry entry) throws IOException {
        File outputFile = new File(EXTRACTED_DIR, entry.getName());
        outputFile.getParentFile().mkdirs();

        try (InputStream is = zip.getInputStream(entry);
                FileOutputStream fos = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
        }
    }
}
