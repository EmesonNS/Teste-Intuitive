package com.intuitive.etl.service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class AnsScraper {
    private static final String BASE_URL = "https://dadosabertos.ans.gov.br/FTP/PDA/demonstracoes_contabeis/";
    private static final String DATA_DIR = "/app/data/raw";

    public void execute() {
        System.out.println("Iniciando processo de scraping da ANS...");
        try {
            Files.createDirectories(Paths.get(DATA_DIR));

            List<String> yearLinks = getLinks(BASE_URL);
            yearLinks.sort(Collections.reverseOrder());

            int quartersFound = 0;

            for (String yearUrl : yearLinks) {
                if (quartersFound >= 3) break;

                System.out.println("Verificando ano: " + yearUrl);
                
                List<String> zipLinks = getZipLinks(yearUrl);
                
                zipLinks.sort(Collections.reverseOrder());

                for (String zipUrl : zipLinks) {
                    if (quartersFound >= 3) break;

                    String fileName = zipUrl.substring(zipUrl.lastIndexOf("/") + 1);
                    
                    if (fileName.toUpperCase().contains("T20")) { 
                        System.out.println("Baixando: " + fileName);
                        if (downloadFile(zipUrl, fileName)) {
                            quartersFound++;
                        }
                    }
                }
            }

            if (quartersFound < 3) {
                System.out.println("AVISO: Encontramos apenas " + quartersFound + " trimestres.");
            } else {
                System.out.println("SUCESSO: " + quartersFound + " trimestres baixados.");
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> getLinks(String url) throws IOException {
        List<String> validLinks = new ArrayList<>();
        Document doc = Jsoup.connect(url).get();
        Elements links = doc.select("a[href]");

        for (Element link : links) {
            String href = link.attr("abs:href");
            if (href.startsWith(url) && href.length() > url.length() && href.endsWith("/")) {
                validLinks.add(href);
            }
        }
        return validLinks;
    }

    private List<String> getZipLinks(String url) throws IOException {
        List<String> zipLinks = new ArrayList<>();
        Document doc = Jsoup.connect(url).get();
        Elements links = doc.select("a[href$='.zip']");

        for (Element link : links) {
            String href = link.attr("abs:href");
            zipLinks.add(href);
        }
        return zipLinks;
    }

    private boolean downloadFile(String url, String fileName) {
        try {
            File destination = new File(DATA_DIR, fileName);
            if (destination.exists()) {
                 System.out.println("Arquivo já existe, pulando: " + fileName);
                 return true;
            }

            URL website = URI.create(url).toURL();
            FileUtils.copyURLToFile(website, destination, 10000, 10000); // 10s timeout
            
            System.out.println("Download concluído: " + destination.getAbsolutePath());
            return true;
        } catch (IOException e) {
            System.err.println("Erro ao baixar " + fileName + ": " + e.getMessage());
            return false;
        }
    }
}