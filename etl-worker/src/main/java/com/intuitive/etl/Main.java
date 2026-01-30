package com.intuitive.etl;

import com.intuitive.etl.service.AggregationService;
import com.intuitive.etl.service.AnsScraper;
import com.intuitive.etl.service.EtlService;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Worker ETL Iniciado ===");

        AnsScraper scraper = new AnsScraper();
        scraper.execute();

        EtlService etl = new EtlService();
        etl.execute();

        AggregationService aggregator = new AggregationService();
        aggregator.execute();

        System.out.println("=== Processo Finalizado ===");
    }
}
