package br.com.pixauditoria.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Audited
@Table(name = "pix_transferencia")
public class PixTransferencia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String chaveOrigem;

    @Column(nullable = false)
    private String chaveDestino;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferenciaStatus status;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public PixTransferencia() {}

    public PixTransferencia(String chaveOrigem, String chaveDestino, BigDecimal valor) {
        this.chaveOrigem = chaveOrigem;
        this.chaveDestino = chaveDestino;
        this.valor = valor;
        this.status = TransferenciaStatus.PENDENTE;
    }

    // Getters & Setters
    public UUID getId() {
        return id;
    }

    public String getChaveOrigem() {
        return chaveOrigem;

    }
    public String getChaveDestino() {
        return chaveDestino;

    }
    public BigDecimal getValor() {
        return valor;
    }
    public TransferenciaStatus getStatus() {
        return status;

    }
    public LocalDateTime getCreatedAt() {
        return createdAt;

    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setStatus(TransferenciaStatus status) {
        this.status = status;
    }
}