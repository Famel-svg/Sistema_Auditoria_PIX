package br.com.pixauditoria.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String usuario;

    @Column(nullable = false)
    private String ip;

    @Column(nullable = false)
    private String operacao;

    @Column(nullable = false)
    private String endpoint;

    @Column(nullable = false)
    private String entidadeAfetada;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    public AuditLog() {}

    public AuditLog(String usuario, String ip, String operacao, String endpoint, String entidadeAfetada) {
        this.usuario = usuario != null ? usuario : "ANONIMO";
        this.ip = ip;
        this.operacao = operacao;
        this.endpoint = endpoint;
        this.entidadeAfetada = entidadeAfetada;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getIp() {
        return ip;
    }

    public String getOperacao() {
        return operacao;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getEntidadeAfetada() {
        return entidadeAfetada;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}