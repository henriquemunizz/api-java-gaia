package br.com.fiap.api.dto;

import jakarta.validation.constraints.*;

public class MissaoRequestDto {
    @NotBlank @Size(max = 100)
    private String nome;
    @NotBlank @Size(max = 500)
    private String descricao;
    @Positive
    private int pontos;
    @Min(1) @Max(3)
    private int dificuldade;
    @Min(0) @Max(1)
    private int ativo;

    public MissaoRequestDto() {}

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

