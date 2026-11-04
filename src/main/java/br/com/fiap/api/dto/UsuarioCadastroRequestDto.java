package br.com.fiap.api.dto;

import jakarta.validation.constraints.*;

public class UsuarioCadastroRequestDto {
    @NotBlank @Pattern(regexp = "[a-zA-Z0-9_.-]{3,40}")
    private String nomeUsuario;
    @NotBlank @Size(min = 8, max = 128)
    private String senha;
    @NotNull @Min(0) @Max(1)
    private Integer perfil;

    public UsuarioCadastroRequestDto() {}

    public String getNomeUsuario() { return nomeUsuario; }
    public void setNomeUsuario(String nomeUsuario) { this.nomeUsuario = nomeUsuario; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public Integer getPerfil() { return perfil; }
    public void setPerfil(Integer perfil) { this.perfil = perfil; }
}
