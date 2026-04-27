package br.com.pixauditoria;

import br.com.pixauditoria.domain.entity.AuditLog;
import br.com.pixauditoria.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste de integração end-to-end da auditoria.
 *
 * Usa Testcontainers para subir um PostgreSQL real (igual ao de produção),
 * garantindo que tanto o AuditLog (AOP) quanto as tabelas _aud (Envers)
 * funcionem corretamente.
 *
 * Usa RANDOM_PORT + TestRestTemplate para fazer requisições HTTP reais,
 * o que é necessário para que o RequestContextHolder (usado pelo AuditAspect
 * para capturar IP e endpoint) esteja disponível durante o teste.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class AuditAspectTest {

    // Container estático: sobe uma única vez para toda a classe de testes
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("pix_auditoria_test")
            .withUsername("test")
            .withPassword("test");

    // Sobrescreve as propriedades de datasource com os dados do container
    @DynamicPropertySource
    static void configurarPropriedades(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AuditLogRepository auditLogRepository;

    // Limpa a tabela de auditoria antes de cada teste para garantir isolamento
    @BeforeEach
    void limparAuditLog() {
        auditLogRepository.deleteAll();
    }

    @Test
    void deveRegistrarLogAoCriarTransferenciaValida() {
        // Given
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

        // When
        restTemplate.exchange(
                "/api/pix/transferencias",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                String.class
        );

        // Then — AOP deve ter salvo o log mesmo que a operação seja bem-sucedida
        List<AuditLog> logs = auditLogRepository.findAll();
        assertThat(logs).isNotEmpty();
        assertThat(logs.getFirst().getOperacao()).isEqualTo("criarTransferencia");
        assertThat(logs.getFirst().getEntidadeAfetada()).isEqualTo("PixTransferencia");
    }

    @Test
    void deveRegistrarLogMesmoQuandoServiceLancaExcecao() {
        // Given — chaves iguais causam TransferenciaInvalidaException
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

        // When — requisição retorna 400, mas o bloco finally do Aspect ainda executa
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/pix/transferencias",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                String.class
        );

        // Then — log deve existir mesmo com a operação tendo falhado
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        List<AuditLog> logs = auditLogRepository.findAll();
        assertThat(logs).isNotEmpty();
        assertThat(logs.getFirst().getOperacao()).isEqualTo("criarTransferencia");
    }

    @Test
    void deveRegistrarLogAoBuscarTransferenciaPorId() {
        // Given — busca com UUID inexistente (404), mas o log ainda deve ser gerado
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("admin", "admin");

        // When
        restTemplate.exchange(
                "/api/pix/transferencias/00000000-0000-0000-0000-000000000000",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        // Then
        List<AuditLog> logs = auditLogRepository.findAll();
        assertThat(logs).isNotEmpty();
        assertThat(logs.getFirst().getOperacao()).isEqualTo("buscarPorId");
    }

    @Test
    void deveSalvarIpEEndpointNoLog() {
        // Given
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

        // When
        restTemplate.exchange(
                "/api/pix/transferencias",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                String.class
        );

        // Then — valida que IP e endpoint foram capturados
        List<AuditLog> logs = auditLogRepository.findAll();
        assertThat(logs).isNotEmpty();

        AuditLog log = logs.getFirst();
        assertThat(log.getIp()).isNotBlank();
        assertThat(log.getEndpoint()).contains("/api/pix/transferencias");
    }
}