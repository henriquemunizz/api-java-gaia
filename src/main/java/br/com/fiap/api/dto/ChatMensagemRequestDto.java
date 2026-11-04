package br.com.fiap.api.dto;

import jakarta.validation.constraints.*;

public class ChatMensagemRequestDto {
    @NotBlank @Size(max = 2000)
    private String texto;
    @NotBlank @Pattern(regexp = "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}")
    private String requisicaoId;

    public ChatMensagemRequestDto() {}
    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
    public String getRequisicaoId() { return requisicaoId; }
    public void setRequisicaoId(String requisicaoId) { this.requisicaoId = requisicaoId; }
}
