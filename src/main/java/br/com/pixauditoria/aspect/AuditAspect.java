package br.com.pixauditoria.aspect;

import br.com.pixauditoria.domain.entity.AuditLog;
import br.com.pixauditoria.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Aspect
@Component
@Order(1)
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;

    public AuditAspect(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Around("execution(* br.com.pixauditoria.service.PixService.*(..))")
    public Object interceptar(ProceedingJoinPoint joinPoint) throws Throwable {
        String metodo = joinPoint.getSignature().getName();
        try {
            return joinPoint.proceed();
        } finally {
            salvarLog(metodo, joinPoint);
        }
    }

    private void salvarLog(String metodo, ProceedingJoinPoint joinPoint) {
        try {
            String usuario = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                    .map(Authentication::getName)
                    .orElse("ANONIMO");

            String ip = "N/A";
            String endpoint = "N/A";

            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    ip = request.getRemoteAddr();
                    endpoint = request.getRequestURI();
                }
            } catch (Exception e) {
                // Fora de um contexto de request (ex: chamadas internas ou alguns testes)
            }

            AuditLog log = new AuditLog(usuario, ip, metodo, endpoint, "PixTransferencia");
            auditLogRepository.save(log);
        } catch (Exception e) {
            // Falha silenciosa no log não deve quebrar o fluxo de negócio
            System.err.println("Falha ao salvar log de auditoria: " + e.getMessage());
        }
    }
}