package br.com.pixauditoria.dto;

import br.com.pixauditoria.domain.entity.TransferenciaStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PixResponse(
        UUID id,
        String chaveOrigem,
        String chaveDestino,
        BigDecimal valor,
        TransferenciaStatus status,
        LocalDateTime createdAt
) {}