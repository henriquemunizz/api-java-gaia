package br.com.fiap.api.dto;

import jakarta.validation.constraints.*;

public class LoginRequestDto {
    @NotBlank @Pattern(regexp = "[a-zA-Z0-9_.-]{3,40}")
    private String nomeUsuario;
    @NotBlank @Size(min = 8, max = 128)
    private String senha;

    public LoginRequestDto() {}

    public String getNomeUsuario() { return nomeUsuario; }
    public void setNomeUsuario(String nomeUsuario) { this.nomeUsuario = nomeUsuario; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
}
