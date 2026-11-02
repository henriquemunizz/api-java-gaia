package br.com.fiap.api.model;

public class Missao {
    
    private int id;
    
    private String nome;
    
    private String descricao;
    
    private int pontos;
    private int dificuldade;
    
    private int ativo;

    public Missao() {}

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
    public int getDificuldade() { return dificuldade; }
    public void setDificuldade(int dificuldade) { this.dificuldade = dificuldade; }
}

