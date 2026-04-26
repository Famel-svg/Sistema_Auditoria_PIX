package br.com.pixauditoria;

import br.com.pixauditoria.domain.entity.AuditLog;
import br.com.pixauditoria.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuditAspectTest {

    @Autowired private AuditLogRepository auditLogRepository;
    @Autowired private br.com.pixauditoria.service.PixService pixService;

    @Test
    void deveRegistrarLogAoExecutarService() {
        // Executa qualquer método do service para disparar o Aspect
        try {
            pixService.buscarPorId(java.util.UUID.randomUUID());
        } catch (Exception ignored) {}

        List<AuditLog> logs = auditLogRepository.findAll();
        assertFalse(logs.isEmpty(), "O Aspect deve salvar pelo menos um registro na audit_log");
    }
}