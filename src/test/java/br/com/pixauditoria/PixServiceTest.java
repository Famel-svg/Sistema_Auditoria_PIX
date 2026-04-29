package br.com.pixauditoria;

import br.com.pixauditoria.domain.entity.PixTransferencia;
import br.com.pixauditoria.domain.entity.TransferenciaStatus;
import br.com.pixauditoria.dto.PixRequest;
import br.com.pixauditoria.dto.PixResponse;
import br.com.pixauditoria.dto.AuditLogResponse;
import br.com.pixauditoria.exception.TransferenciaInvalidaException;
import br.com.pixauditoria.exception.TransferenciaNaoEncontradaException;
import br.com.pixauditoria.repository.AuditLogRepository;
import br.com.pixauditoria.repository.PixTransferenciaRepository;
import br.com.pixauditoria.service.PixService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PixServiceTest {

    @Mock private PixTransferenciaRepository pixRepository;
    @Mock private AuditLogRepository auditLogRepository;
    @InjectMocks private PixService pixService;

    @Test
    void deveCriarTransferenciaComSucesso() {
        PixRequest req = new PixRequest("chave1", "chave2", BigDecimal.TEN);
        when(pixRepository.save(any())).thenAnswer(i -> {
            PixTransferencia t = i.getArgument(0);
            return new PixTransferencia(t.getChaveOrigem(), t.getChaveDestino(), t.getValor());
        });

        PixResponse resp = pixService.criarTransferencia(req);
        assertEquals(TransferenciaStatus.PENDENTE, resp.status());
        verify(pixRepository).save(any());
    }

    @Test
    void deveLancarExcecaoSeChavesIguais() {
        PixRequest req = new PixRequest("mesma", "mesma", BigDecimal.TEN);
        assertThrows(TransferenciaInvalidaException.class, () -> pixService.criarTransferencia(req));
        verify(pixRepository, never()).save(any());
    }

    @Test
    void deveCancelarTransferenciaPendente() {
        UUID id = UUID.randomUUID();
        PixTransferencia t = new PixTransferencia("origem", "destino", BigDecimal.TEN);
        when(pixRepository.findById(id)).thenReturn(Optional.of(t));

        PixResponse resp = pixService.cancelarTransferencia(id);
        assertEquals(TransferenciaStatus.CANCELADA, resp.status());
        verify(pixRepository).save(t);
    }

    @Test
    void deveFalharCancelamentoSeConcluida() {
        UUID id = UUID.randomUUID();
        PixTransferencia t = new PixTransferencia("origem", "destino", BigDecimal.TEN);
        t.setStatus(TransferenciaStatus.CONCLUIDA);
        when(pixRepository.findById(id)).thenReturn(Optional.of(t));

        assertThrows(TransferenciaInvalidaException.class, () -> pixService.cancelarTransferencia(id));
    }

    @Test
    void deveLancarExcecaoSeIdNaoExistente() {
        UUID id = UUID.randomUUID();
        when(pixRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(TransferenciaNaoEncontradaException.class, () -> pixService.buscarPorId(id));
    }

    @Test
    void deveBuscarPorIdComSucesso() {
        UUID id = UUID.randomUUID();
        PixTransferencia t = new PixTransferencia("origem", "destino", BigDecimal.TEN);
        when(pixRepository.findById(id)).thenReturn(Optional.of(t));

        PixResponse resp = pixService.buscarPorId(id);
        assertNotNull(resp);
        assertEquals("origem", resp.chaveOrigem());
    }

    @Test
    void deveListarPorChave() {
        String chave = "test";
        when(pixRepository.findByChaveOrigemOrChaveDestino(chave, chave)).thenReturn(java.util.List.of());

        java.util.List<PixResponse> result = pixService.listarPorChave(chave);
        assertTrue(result.isEmpty());
    }
}