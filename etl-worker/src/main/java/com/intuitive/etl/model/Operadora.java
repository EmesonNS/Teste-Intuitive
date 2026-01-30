package com.intuitive.etl.model;

public class Operadora {
    private String registroAns;
    private String cnpj;
    private String razaoSocial;
    private String uf;
    private String modalidade;

    public Operadora(String registroAns, String cnpj, String razaoSocial, String uf, String modalidade) {
        this.registroAns = registroAns;
        this.cnpj = cnpj;
        this.razaoSocial = razaoSocial;
        this.uf = uf;
        this.modalidade = modalidade;
    }
    
    public String getCnpj() { return cnpj; }
    public String getRazaoSocial() { return razaoSocial; }
    public String getUf() { return uf; }
    public String getModalidade() { return modalidade; }
}
