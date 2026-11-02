package br.com.fiap.api.model;

public class Usuario {
    
    private int id;
    
    private String nomeUsuario;
    
    private String perfil;
    
    private String senha;

    public Usuario() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNomeUsuario() { return nomeUsuario; }
    public void setNomeUsuario(String nomeUsuario) { this.nomeUsuario = nomeUsuario; }

    public String getPerfil() { return perfil; }
    public void setPerfil(String perfil) { this.perfil = perfil; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
}
