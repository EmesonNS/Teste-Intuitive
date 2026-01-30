package com.intuitive.etl.model;

import java.util.ArrayList;
import java.util.List;

public class StatsAccumulator {
    private String chave;
    private double somaTotal = 0.0;
    private List<Double> valores = new ArrayList<>();

    public StatsAccumulator(String chave) {
        this.chave = chave;
    }

    public void addValor(double valor) {
        this.somaTotal += valor;
        this.valores.add(valor);
    }

    public double getTotal() {
        return somaTotal;
    }

    public double getMedia() {
        return valores.isEmpty() ? 0.0 : somaTotal / valores.size();
    }

    public double getDesvioPadrao() {
        if (valores.size() <= 1) return 0.0;

        double media = getMedia();
        double somaQuadrados = 0.0;

        for (double v : valores) {
            somaQuadrados += Math.pow(v - media, 2);
        }

        return Math.sqrt(somaQuadrados / (valores.size() - 1));
    }

    public int getQtdRegistros() {
        return valores.size();
    }
}
