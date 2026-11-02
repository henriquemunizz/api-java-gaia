package br.com.fiap.api.dto;

import jakarta.validation.constraints.*;

public class UsuarioResponseDto {
    
    private int id;
    
    private String nomeUsuario;
    
    private String perfil;

    public UsuarioResponseDto() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNomeUsuario() { return nomeUsuario; }
    public void setNomeUsuario(String nomeUsuario) { this.nomeUsuario = nomeUsuario; }

    public String getPerfil() { return perfil; }
    public void setPerfil(String perfil) { this.perfil = perfil; }
}
