package br.com.fiap.api.model;

public class Mensagem {
    private int id;
    private String autor;
    private String texto;
    private String data;
    private String requisicaoId;

    public Mensagem() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public String getRequisicaoId() { return requisicaoId; }
    public void setRequisicaoId(String requisicaoId) { this.requisicaoId = requisicaoId; }
}
