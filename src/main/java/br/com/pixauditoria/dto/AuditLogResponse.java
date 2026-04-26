package br.com.pixauditoria.dto;

public record AuditLogResponse(
        String usuario,
        String ip,
        String operacao,
        String endpoint,
        String entidadeAfetada,
        String detalhesAdicionais
) {}