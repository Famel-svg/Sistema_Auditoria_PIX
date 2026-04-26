package br.com.pixauditoria.controller;

import br.com.pixauditoria.dto.*;
import br.com.pixauditoria.service.PixService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pix")
public class PixController {

    private final PixService pixService;

    public PixController(PixService pixService) { this.pixService = pixService; }

    @PostMapping("/transferencias")
    public ResponseEntity<PixResponse> criar(@Valid @RequestBody PixRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pixService.criarTransferencia(request));
    }

    @GetMapping("/transferencias/{id}")
    public ResponseEntity<PixResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(pixService.buscarPorId(id));
    }

    @GetMapping("/transferencias/{id}/historico")
    public ResponseEntity<List<AuditLogResponse>> historico(@PathVariable UUID id) {
        return ResponseEntity.ok(pixService.buscarHistorico(id));
    }

    @PatchMapping("/transferencias/{id}/cancelar")
    public ResponseEntity<PixResponse> cancelar(@PathVariable UUID id) {
        return ResponseEntity.ok(pixService.cancelarTransferencia(id));
    }

    @GetMapping("/transferencias")
    public ResponseEntity<List<PixResponse>> listar(@RequestParam String chave) {
        return ResponseEntity.ok(pixService.listarPorChave(chave));
    }
}