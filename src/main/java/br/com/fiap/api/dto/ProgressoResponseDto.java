package br.com.fiap.api.dto;

import jakarta.validation.constraints.*;

public class ProgressoResponseDto {
    
    private int pontosTotais;
    
    private int saldo;
    
    private int missoesConcluidas;

    public ProgressoResponseDto() {}

    public int getPontosTotais() { return pontosTotais; }
    public void setPontosTotais(int pontosTotais) { this.pontosTotais = pontosTotais; }

    public int getSaldo() { return saldo; }
    public void setSaldo(int saldo) { this.saldo = saldo; }

    public int getMissoesConcluidas() { return missoesConcluidas; }
    public void setMissoesConcluidas(int missoesConcluidas) { this.missoesConcluidas = missoesConcluidas; }
}
