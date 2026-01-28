package com.intuitive.etl;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Worker ETL Iniciado ===");

        AnsScraper scraper = new AnsScraper();
        scraper.execute();

        System.out.println("=== Processo Finalizado com Sucesso ===");
    }
}
