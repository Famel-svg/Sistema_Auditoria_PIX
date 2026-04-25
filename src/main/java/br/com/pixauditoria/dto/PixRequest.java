package br.com.pixauditoria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PixRequest(
        @NotBlank(message = "Chave de origem é obrigatória") String chaveOrigem,
        @NotBlank(message = "Chave de destino é obrigatória") String chaveDestino,
        @NotNull(message = "Valor é obrigatório") @Positive(message = "Valor deve ser positivo") BigDecimal valor
) {}