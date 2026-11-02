package br.com.fiap.api.dto;

import jakarta.validation.constraints.*;

public class HistoricoResponseDto {
    
    private int id;
    
    private int itemId;
    
    private int pontos;
    
    private String data;

    public HistoricoResponseDto() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getPontos() { return pontos; }
    public void setPontos(int pontos) { this.pontos = pontos; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
}
