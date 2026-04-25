package br.com.pixauditoria.service;

import br.com.pixauditoria.domain.entity.PixTransferencia;
import br.com.pixauditoria.domain.entity.TransferenciaStatus;
import br.com.pixauditoria.dto.PixRequest;
import br.com.pixauditoria.dto.AuditLogResponse;
import br.com.pixauditoria.dto.PixResponse;
import br.com.pixauditoria.exception.TransferenciaInvalidaException;
import br.com.pixauditoria.exception.TransferenciaNaoEncontradaException;
import br.com.pixauditoria.repository.AuditLogRepository;
import br.com.pixauditoria.repository.PixTransferenciaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PixService {

    private final PixTransferenciaRepository pixRepository;
    private final AuditLogRepository auditLogRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public PixService(PixTransferenciaRepository pixRepository, AuditLogRepository auditLogRepository) {
        this.pixRepository = pixRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public PixResponse criarTransferencia(PixRequest request) {
        if (request.chaveOrigem().equalsIgnoreCase(request.chaveDestino())) {
            throw new TransferenciaInvalidaException("Chave de origem e destino não podem ser iguais.");
        }

        PixTransferencia transferencia = new PixTransferencia(
                request.chaveOrigem(), request.chaveDestino(), request.valor()
        );
        pixRepository.save(transferencia);
        return mapToResponse(transferencia);
    }

    @Transactional(readOnly = true)
    public PixResponse buscarPorId(UUID id) {
        return pixRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new TransferenciaNaoEncontradaException(id));
    }

    @Transactional(readOnly = true)
    public List<PixResponse> listarPorChave(String chave) {
        return pixRepository.findByChaveOrigemOrChaveDestino(chave, chave)
                .stream().map(this::mapToResponse).toList();
    }

    @Transactional
    public PixResponse cancelarTransferencia(UUID id) {
        PixTransferencia transferencia = pixRepository.findById(id)
                .orElseThrow(() -> new TransferenciaNaoEncontradaException(id));

        if (transferencia.getStatus() != TransferenciaStatus.PENDENTE) {
            throw new TransferenciaInvalidaException("Apenas transferências PENDENTES podem ser canceladas.");
        }

        transferencia.setStatus(TransferenciaStatus.CANCELADA);
        pixRepository.save(transferencia);
        return mapToResponse(transferencia);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> buscarHistorico(UUID id) {
        pixRepository.findById(id).orElseThrow(() -> new TransferenciaNaoEncontradaException(id));

        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        List<Number> revisions = auditReader.getRevisions(PixTransferencia.class, id);

        return revisions.stream().map(rev -> {
            PixTransferencia versao = auditReader.find(PixTransferencia.class, id, rev);
            return new AuditLogResponse(
                    "SYSTEM",
                    "N/A",
                    "ENVERS_REVISION",
                    "/historico",
                    String.valueOf(rev),
                    versao.getStatus().toString()
            );
        }).toList();
    }

    private PixResponse mapToResponse(PixTransferencia t) {
        return new PixResponse(t.getId(), t.getChaveOrigem(), t.getChaveDestino(), t.getValor(), t.getStatus(), t.getCreatedAt());
    }
}