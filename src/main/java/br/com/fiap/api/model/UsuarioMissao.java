package br.com.fiap.api.model;

public class UsuarioMissao {
    private int id;
    private Usuario usuario;
    private Missao missao;
    private int pontos;
    private java.time.LocalDateTime data;

    public UsuarioMissao() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Missao getMissao() {
        return missao;
    }

    public void setMissao(Missao missao) {
        this.missao = missao;
    }

    public int getPontos() {
        return pontos;
    }

    public void setPontos(int pontos) {
        this.pontos = pontos;
    }

    public java.time.LocalDateTime getData() {
        return data;
    }

    public void setData(java.time.LocalDateTime data) {
        this.data = data;
    }
}
