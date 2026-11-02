package br.com.fiap.api.dto;

import jakarta.validation.constraints.*;

public class UsuarioAtualizacaoRequestDto {
    @NotBlank @Pattern(regexp = "[a-zA-Z0-9_.-]{3,40}")
    private String nomeUsuario;
    @Size(min = 8, max = 128) @Pattern(regexp = "(?s).*\\S.*", message = "Senha não pode conter apenas espaços")
    private String senha;

    public UsuarioAtualizacaoRequestDto() {}

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
