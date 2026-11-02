package br.com.fiap.api.model;

public class UsuarioRecompensa {
    private int id;
    private Usuario usuario;
    private Recompensa recompensa;
    private int pontos;
    private java.time.LocalDateTime data;

    public UsuarioRecompensa() {}

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

    public Recompensa getRecompensa() {
        return recompensa;
    }

    public void setRecompensa(Recompensa recompensa) {
        this.recompensa = recompensa;
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
