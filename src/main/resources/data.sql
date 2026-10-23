-- =============================================================================
-- data.sql — Dados iniciais para demonstração do sistema
-- Executado automaticamente pelo Spring Boot após o Hibernate criar as tabelas.
--
-- Cobre os 4 status possíveis de uma transferência Pix:
-- PENDENTE, CONCLUIDA, CANCELADA, FALHA
-- =============================================================================

-- Limpa os dados existentes para evitar duplicatas ao reiniciar
-- (seguro em dev — não usar em produção)
DELETE FROM audit_log;
DELETE FROM pix_transferencia_aud;
DELETE FROM revinfo;
DELETE FROM pix_transferencia;

-- -----------------------------------------------------------------------------
-- Transferências Pix — um exemplo de cada status
-- -----------------------------------------------------------------------------

-- 1. PENDENTE — aguardando processamento
INSERT INTO pix_transferencia (id, chave_origem, chave_destino, valor, status, created_at, updated_at)
VALUES (
           '11111111-1111-1111-1111-111111111111',
           '111.111.111-11',
           '222.222.222-22',
           250.00,
           'PENDENTE',
           NOW() - INTERVAL '5 minutes',
           NOW() - INTERVAL '5 minutes'
       );

-- 2. CONCLUIDA — processada com sucesso
INSERT INTO pix_transferencia (id, chave_origem, chave_destino, valor, status, created_at, updated_at)
VALUES (
           '22222222-2222-2222-2222-222222222222',
           'joao@email.com',
           'maria@email.com',
           1500.00,
           'CONCLUIDA',
           NOW() - INTERVAL '2 hours',
           NOW() - INTERVAL '1 hour'
       );

-- 3. CANCELADA — cancelada pelo usuário antes do processamento
INSERT INTO pix_transferencia (id, chave_origem, chave_destino, valor, status, created_at, updated_at)
VALUES (
           '33333333-3333-3333-3333-333333333333',
           '+5511999990001',
           '+5511999990002',
           75.50,
           'CANCELADA',
           NOW() - INTERVAL '1 day',
           NOW() - INTERVAL '23 hours'
       );

-- 4. FALHA — erro durante o processamento
INSERT INTO pix_transferencia (id, chave_origem, chave_destino, valor, status, created_at, updated_at)
VALUES (
           '44444444-4444-4444-4444-444444444444',
           'empresa@dominio.com.br',
           'fornecedor@dominio.com.br',
           9999.99,
           'FALHA',
           NOW() - INTERVAL '3 days',
           NOW() - INTERVAL '3 days'
       );

-- 5. PENDENTE — transferência de alto valor para demonstrar o endpoint de listagem por chave
INSERT INTO pix_transferencia (id, chave_origem, chave_destino, valor, status, created_at, updated_at)
VALUES (
           '55555555-5555-5555-5555-555555555555',
           '111.111.111-11',
           'joao@email.com',
           3200.00,
           'PENDENTE',
           NOW() - INTERVAL '10 minutes',
           NOW() - INTERVAL '10 minutes'
       );

-- -----------------------------------------------------------------------------
-- Audit logs — contexto HTTP simulado para demonstração do painel de auditoria
-- -----------------------------------------------------------------------------

INSERT INTO audit_log (id, usuario, ip, operacao, endpoint, entidade_afetada, timestamp)
VALUES (
           'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
           'admin',
           '192.168.1.100',
           'criarTransferencia',
           '/api/pix/transferencias',
           'PixTransferencia',
           NOW() - INTERVAL '2 hours'
       );

INSERT INTO audit_log (id, usuario, ip, operacao, endpoint, entidade_afetada, timestamp)
VALUES (
           'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
           'admin',
           '192.168.1.100',
           'criarTransferencia',
           '/api/pix/transferencias',
           'PixTransferencia',
           NOW() - INTERVAL '1 day'
       );

INSERT INTO audit_log (id, usuario, ip, operacao, endpoint, entidade_afetada, timestamp)
VALUES (
           'cccccccc-cccc-cccc-cccc-cccccccccccc',
           'admin',
           '10.0.0.55',
           'cancelarTransferencia',
           '/api/pix/transferencias/33333333-3333-3333-3333-333333333333/cancelar',
           'PixTransferencia',
           NOW() - INTERVAL '23 hours'
       );

INSERT INTO audit_log (id, usuario, ip, operacao, endpoint, entidade_afetada, timestamp)
VALUES (
           'dddddddd-dddd-dddd-dddd-dddddddddddd',
           'admin',
           '10.0.0.55',
           'buscarPorId',
           '/api/pix/transferencias/22222222-2222-2222-2222-222222222222',
           'PixTransferencia',
           NOW() - INTERVAL '30 minutes'
       );

INSERT INTO audit_log (id, usuario, ip, operacao, endpoint, entidade_afetada, timestamp)
VALUES (
           'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
           'ANONIMO',
           '172.16.0.10',
           'listarPorChave',
           '/api/pix/transferencias',
           'PixTransferencia',
           NOW() - INTERVAL '5 minutes'
       );