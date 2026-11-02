package br.com.fiap.api.dto;

import jakarta.validation.constraints.*;

public class RecompensaRequestDto {
    @NotBlank @Size(max = 100)
    private String nome;
    @NotBlank @Size(max = 500)
    private String descricao;
    @Positive
    private int pontos;
    @Min(0) @Max(1)
    private int ativo;

    public RecompensaRequestDto() {}

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public int getPontos() { return pontos; }
    public void setPontos(int pontos) { this.pontos = pontos; }

    public int getAtivo() { return ativo; }
    public void setAtivo(int ativo) { this.ativo = ativo; }
}
