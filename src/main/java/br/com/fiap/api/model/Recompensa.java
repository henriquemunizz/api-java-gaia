package br.com.fiap.api.model;

public class Recompensa {
    
    private int id;
    
    private String nome;
    
    private String descricao;
    
    private int pontos;
    
    private int ativo;

    public Recompensa() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public int getPontos() { return pontos; }
    public void setPontos(int pontos) { this.pontos = pontos; }

    public int getAtivo() { return ativo; }
    public void setAtivo(int ativo) { this.ativo = ativo; }
}
