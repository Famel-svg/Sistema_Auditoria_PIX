package br.com.pixauditoria.service;

import br.com.pixauditoria.domain.entity.AuditLog;
import br.com.pixauditoria.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;


/* Serviço responsável por persistir logs de auditoria.*/
@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(String metodo) {
        try {
            String usuario = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                    .map(Authentication::getName)
                    .orElse("ANONIMO");

            RequestAttributes attrs = RequestContextHolder.getRequestAttributes();

            String ip = "N/A";
            String endpoint = "N/A";

            if (attrs instanceof ServletRequestAttributes servletAttrs) {
                HttpServletRequest request = servletAttrs.getRequest();
                ip = request.getRemoteAddr();
                endpoint = request.getRequestURI();
            }

            auditLogRepository.save(new AuditLog(usuario, ip, metodo, endpoint, "PixTransferencia"));

        } catch (Exception e) {
            // Falha silenciosa — log nunca deve quebrar o fluxo de negócio
            System.err.println("Falha ao salvar log de auditoria: " + e.getMessage());
        }
    }
}