package br.com.pixauditoria.aspect;

import br.com.pixauditoria.service.AuditLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(1)
public class AuditAspect {

    private final AuditLogService auditLogService;

    public AuditAspect(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Around("execution(* br.com.pixauditoria.service.PixService.*(..))")
    public Object interceptar(ProceedingJoinPoint joinPoint) throws Throwable {
        String metodo = joinPoint.getSignature().getName();
        try {
            return joinPoint.proceed();
        } finally {
            auditLogService.registrar(metodo); // ← delega para o service
        }
    }
}