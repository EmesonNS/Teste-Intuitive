package com.intuitive.etl;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Worker ETL Iniciado ===");

        AnsScraper scraper = new AnsScraper();
        scraper.execute();

        EtlService etl = new EtlService();
        etl.execute();

        System.out.println("=== Processo Finalizado ===");
    }
}
