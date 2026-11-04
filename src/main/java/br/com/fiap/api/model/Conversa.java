package br.com.fiap.api.model;

public class Conversa {
    private int id;
    private String titulo;
    private String data;

    public Conversa() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
}
