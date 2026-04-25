package br.com.pixauditoria.repository;

import br.com.pixauditoria.domain.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    List<AuditLog> findByUsuarioAndTimestampBetween(String usuario, LocalDateTime inicio, LocalDateTime fim);
    List<AuditLog> findByIp(String ip);
}