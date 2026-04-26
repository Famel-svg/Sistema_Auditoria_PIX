package br.com.pixauditoria.exception;

import java.util.UUID;

public class TransferenciaNaoEncontradaException extends RuntimeException {
    public TransferenciaNaoEncontradaException(UUID id) {
        super("Transferência não encontrada com ID: " + id);
    }
}