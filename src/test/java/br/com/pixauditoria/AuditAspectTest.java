package br.com.pixauditoria;

import br.com.pixauditoria.domain.entity.AuditLog;
import br.com.pixauditoria.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste de integração da auditoria end-to-end.
 *
 * Usa H2 in-memory (perfil "test") + RANDOM_PORT para subir um servidor
 * HTTP real, garantindo que o RequestContextHolder do AuditAspect tenha
 * acesso ao contexto de request durante a interceptação.
 *
 * Não requer Docker nem banco externo — roda com ./mvnw test normalmente.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuditAspectTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @BeforeEach
    void limparAuditLog() {
        auditLogRepository.deleteAll();
    }

    @Test
    void deveRegistrarLogAoCriarTransferenciaValida() {
        String body = """
                {
                    "chaveOrigem": "11111111111",
                    "chaveDestino": "22222222222",
                    "valor": 100.00
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth("admin", "admin");

        restTemplate.exchange(
                "/api/pix/transferencias",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                String.class
        );

        List<AuditLog> logs = auditLogRepository.findAll();
        assertThat(logs).isNotEmpty();
        assertThat(logs.get(0).getOperacao()).isEqualTo("criarTransferencia");
        assertThat(logs.get(0).getEntidadeAfetada()).isEqualTo("PixTransferencia");
    }

    @Test
    void deveRegistrarLogMesmoQuandoServiceLancaExcecao() {
        // Chaves iguais causam TransferenciaInvalidaException no Service
        String body = """
                {
                    "chaveOrigem": "mesma",
                    "chaveDestino": "mesma",
                    "valor": 10.00
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth("admin", "admin");

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/pix/transferencias",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                String.class
        );

        // Operação falhou mas o log deve existir (bloco finally do Aspect)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        List<AuditLog> logs = auditLogRepository.findAll();
        assertThat(logs).isNotEmpty();
        assertThat(logs.get(0).getOperacao()).isEqualTo("criarTransferencia");
    }

    @Test
    void deveRegistrarLogAoBuscarTransferenciaPorId() {
        // UUID inexistente → 404, mas o log ainda deve ser criado
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("admin", "admin");

        restTemplate.exchange(
                "/api/pix/transferencias/00000000-0000-0000-0000-000000000000",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        List<AuditLog> logs = auditLogRepository.findAll();
        assertThat(logs).isNotEmpty();
        assertThat(logs.get(0).getOperacao()).isEqualTo("buscarPorId");
    }

    @Test
    void deveSalvarIpEEndpointNoLog() {
        String body = """
                {
                    "chaveOrigem": "aaa",
                    "chaveDestino": "bbb",
                    "valor": 50.00
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth("admin", "admin");

        restTemplate.exchange(
                "/api/pix/transferencias",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                String.class
        );

        List<AuditLog> logs = auditLogRepository.findAll();
        assertThat(logs).isNotEmpty();

        AuditLog log = logs.get(0);
        assertThat(log.getIp()).isNotBlank();
        assertThat(log.getEndpoint()).contains("/api/pix/transferencias");
    }
}