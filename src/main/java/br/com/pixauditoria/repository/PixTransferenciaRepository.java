package br.com.pixauditoria.repository;

import br.com.pixauditoria.domain.entity.PixTransferencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PixTransferenciaRepository extends JpaRepository<PixTransferencia, UUID> {
    List<PixTransferencia> findByChaveOrigemOrChaveDestino(String chaveOrigem, String chaveDestino);
}