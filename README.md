# Sistema de Auditoria e Rastreabilidade PIX

[![CI/CD Pipeline](https://github.com/Famel-svg/Sistema_Auditoria_PIX/actions/workflows/maven.yml/badge.svg)](https://github.com/Famel-svg/Sistema_Auditoria_PIX/actions/workflows/maven.yml)
[![Coverage](https://img.shields.io/badge/coverage-≥80%25-brightgreen)](#testes)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-green)

API REST para rastreabilidade completa de transações PIX, desenvolvida em **Java 21 + Spring Boot 3**, com conformidade à **Resolução BCB nº 1/2020** e **LGPD**. O sistema responde às quatro perguntas centrais de qualquer auditoria financeira:

| Pergunta | Mecanismo que responde |
|---|---|
| **Quem** realizou a operação? | Spring AOP + Spring Security |
| **Quando** foi realizada? | Timestamp em `audit_log` e `pix_transferencia_aud` |
| **O que mudou** nos dados (valor antes × depois)? | Hibernate Envers — diff automático por revisão |
| **De onde** veio a requisição? | AOP captura IP e endpoint HTTP |

> **Stack:** Java 21 · Spring Boot 3.3.4 · Hibernate Envers · Spring AOP · Spring Security · PostgreSQL 16 · Docker · GitHub Actions · JUnit 5 · JaCoCo (≥80%)

---

## O Problema

O ecossistema PIX processa mais de 60 milhões de transações por dia no Brasil. A Resolução BCB nº 1/2020 exige que instituições financeiras mantenham rastreabilidade completa de cada operação — incluindo contexto de quem executou, quando, de onde e qual foi o estado anterior dos dados. Sistemas que tratam a auditoria como um log simples de texto falham nesse requisito: registram o evento, mas não o diff.

Este projeto implementa uma estratégia de auditoria híbrida que separa as duas responsabilidades em mecanismos distintos, sem acoplamento entre eles.

---

## Decisão Central: Auditoria Híbrida

Duas cadeias operam em paralelo, cada uma respondendo o que a outra não consegue:

| Mecanismo | O que captura | O que **não** captura | Tabela gerada |
|---|---|---|---|
| **Hibernate Envers** | Diff de campos (estado antes × depois) a cada INSERT/UPDATE/DELETE | Contexto HTTP — não sabe quem ou de onde veio | `pix_transferencia_aud` |
| **Spring AOP** | Usuário autenticado, IP, endpoint, método HTTP | Diff de campos — não sabe o que mudou nos dados | `audit_log` |

Usar apenas Envers responderia *o que mudou*, mas não *quem mudou*. Usar apenas AOP responderia *quem fez*, mas não *o que estava diferente*. A combinação cobre o requisito de compliance completo.

O `AuditAspect` intercepta o `PixService` de forma **totalmente transparente** — o Service não sabe que está sendo auditado, não há acoplamento direto entre eles.

---

## Arquitetura

```
HTTP Request
     │
     ▼
Spring Security          ← extrai usuário autenticado do contexto
     │
     ▼
AuditAspect (@Around)    ← intercepta ANTES e DEPOIS do Service
     │    └──────────────────────────────→ AuditLogService.registrar()
     │                                          └→ salva em audit_log
     │                                             (REQUIRES_NEW — transação própria)
     ▼
PixService               ← executa lógica de negócio
     │
     ▼
JPA + Hibernate Envers   ← persiste entidade + grava diff automático
          └──────────────────────────────→ salva em pix_transferencia_aud
                                                    │
                                                    ▼
                                              PostgreSQL 16
```

**Por que `REQUIRES_NEW` no `AuditLogService`?**
O log de auditoria usa uma transação independente da transação do `PixService`. Se a transferência falhar e sofrer rollback, o registro de auditoria da operação com falha ainda é persistido. Auditar apenas operações bem-sucedidas viola o requisito de rastreabilidade.

---

## Estrutura do Projeto

```
src/main/java/br/com/pixauditoria/
├── aspect/
│   └── AuditAspect.java              ← interceptação AOP (@Around)
├── config/
│   ├── EnversConfig.java             ← configuração explícita do Envers
│   └── SecurityConfig.java           ← Basic Auth para dev/demo
├── controller/
│   ├── GlobalExceptionHandler.java   ← mapeamento de exceções → HTTP
│   └── PixController.java
├── domain/entity/
│   ├── AuditLog.java
│   ├── PixTransferencia.java         ← @Audited — gera pix_transferencia_aud
│   └── TransferenciaStatus.java      ← PENDENTE / CONCLUIDA / CANCELADA / FALHA
├── dto/
│   ├── PixRequest.java               ← Bean Validation na entrada
│   ├── PixResponse.java
│   └── AuditLogResponse.java
├── exception/
│   ├── TransferenciaInvalidaException.java
│   └── TransferenciaNaoEncontradaException.java
├── repository/
│   ├── PixTransferenciaRepository.java
│   └── AuditLogRepository.java
└── service/
    ├── PixService.java               ← regras de negócio
    └── AuditLogService.java          ← persistência do log (REQUIRES_NEW)

src/test/
├── PixServiceTest.java               ← unitário com Mockito
├── PixControllerTest.java            ← camada web com MockMvc
├── EnversConfigTest.java             ← unitário puro da configuração
└── AuditAspectTest.java              ← integração end-to-end (H2 + RANDOM_PORT)
```

---

## API Reference

| Método | Endpoint | Descrição | Status |
|---|---|---|---|
| `POST` | `/api/pix/transferencias` | Cria uma nova transferência | `201 Created` |
| `GET` | `/api/pix/transferencias/{id}` | Busca detalhes | `200 OK` |
| `GET` | `/api/pix/transferencias/{id}/historico` | Histórico de revisões (Envers) | `200 OK` |
| `PATCH` | `/api/pix/transferencias/{id}/cancelar` | Cancela transferência pendente | `200 OK` |
| `GET` | `/api/pix/transferencias?chave={chave}` | Lista por chave PIX | `200 OK` |

**Exemplo rápido:**

```bash
# Criar transferência
curl -X POST http://localhost:8080/api/pix/transferencias \
  -H "Content-Type: application/json" \
  -u admin:admin \
  -d '{"chaveOrigem": "11111111111", "chaveDestino": "22222222222", "valor": 150.00}'

# Consultar histórico de revisões
curl http://localhost:8080/api/pix/transferencias/{id}/historico -u admin:admin
```

---

## Como Executar

**Pré-requisitos:** Java 21 JDK, Maven 3.9+, Docker e Docker Compose.

```bash
# 1. Clonar e configurar variáveis
git clone https://github.com/Famel-svg/Sistema_Auditoria_PIX.git
cd Sistema_Auditoria_PIX
cp .env.example .env
# Preencha DB_URL, DB_USERNAME, DB_PASSWORD, SECURITY_USER, SECURITY_PASS

# 2. Subir o PostgreSQL
docker-compose up -d

# 3. Executar a aplicação
./mvnw spring-boot:run
# API disponível em http://localhost:8080
```

**Perfis disponíveis:**

| Perfil | Comando | Comportamento |
|---|---|---|
| `dev` (padrão) | `./mvnw spring-boot:run` | SQL visível, log DEBUG, `ddl-auto=update` |
| `prod` | `--spring.profiles.active=prod` | Sem SQL, log WARN, `ddl-auto=validate`, falha intencional sem variáveis de ambiente |

---

## Testes

Todos os testes rodam **sem Docker e sem banco externo** — H2 in-memory.

```bash
# Executar testes
./mvnw test

# Testes + relatório de cobertura
./mvnw verify

# Abrir relatório (Linux/Mac)
open target/site/jacoco/index.html
```

**Estratégia de testes:**

| Classe | Tipo | O que valida |
|---|---|---|
| `PixServiceTest` | Unitário (Mockito) | Regras de negócio, transições de estado, exceções |
| `PixControllerTest` | Camada web (MockMvc) | Status HTTP, Bean Validation, SecurityConfig |
| `EnversConfigTest` | Unitário puro | Propriedades do Envers configuradas corretamente |
| `AuditAspectTest` | Integração (H2 + `RANDOM_PORT`) | Log salvo em sucesso e em falha; IP e endpoint capturados |

> O `AuditAspectTest` usa `RANDOM_PORT` para subir um servidor HTTP real — necessário porque o `AuditAspect` captura IP e endpoint via `RequestContextHolder`, que só existe em contexto de request real. Simular isso com `MockMvc` mascararia a dependência.

**Cobertura mínima:** JaCoCo configurado com 80% de cobertura de linhas como requisito de build. Classes excluídas da contagem: DTOs, entidades JPA, exceptions e a classe `main`.

---

## CI/CD

Pipeline executado automaticamente a cada push para `main` ou `develop` e em pull requests.

```
build → testes unitários → testes de integração → cobertura JaCoCo → build Docker
```

| Etapa | O que faz |
|---|---|
| **Build** | Compila o projeto com Maven |
| **Testes unitários** | `PixServiceTest`, `PixControllerTest`, `EnversConfigTest` — sem banco externo |
| **Testes de integração** | `AuditAspectTest` — H2 in-memory, sem Docker |
| **Cobertura** | Valida ≥80% e publica relatório HTML como artefato da execução |
| **Build Docker** | Gera imagem via `spring-boot:build-image` |

> Relatório de cobertura disponível em cada execução: **Actions → execução → jacoco-report**

---

## Modelo de Dados

**`pix_transferencia`** — entidade principal

| Campo | Tipo | Descrição |
|---|---|---|
| `id` | UUID | Chave primária |
| `chave_origem` | VARCHAR | Chave PIX do remetente |
| `chave_destino` | VARCHAR | Chave PIX do destinatário |
| `valor` | DECIMAL(15,2) | Valor em reais (`BigDecimal`) |
| `status` | VARCHAR | `PENDENTE` / `CONCLUIDA` / `CANCELADA` / `FALHA` |
| `created_at` | TIMESTAMP | Criação automática |
| `updated_at` | TIMESTAMP | Atualização automática |

**`pix_transferencia_aud`** — gerada automaticamente pelo Envers

| Campo | Tipo | Descrição |
|---|---|---|
| `id` | UUID | FK para a entidade original |
| `rev_id` | INTEGER | Número da revisão |
| `rev_type` | SMALLINT | `0`=INSERT, `1`=UPDATE, `2`=DELETE |
| demais campos | — | Estado completo da entidade nessa revisão |

**`audit_log`** — preenchida pelo AOP

| Campo | Tipo | Descrição |
|---|---|---|
| `id` | UUID | Chave primária |
| `usuario` | VARCHAR | Username extraído do `SecurityContext` |
| `ip` | VARCHAR | IP da requisição HTTP |
| `operacao` | VARCHAR | Nome do método interceptado |
| `endpoint` | VARCHAR | Path HTTP |
| `entidade_afetada` | VARCHAR | Nome da entidade |
| `timestamp` | TIMESTAMP | Momento exato da interceptação |

---

## Limitações Conhecidas

- Autenticação em Basic Auth — adequada para demo; produção requer JWT ou OAuth2
- Sem paginação nos endpoints de listagem e histórico
- Para produção: habilitar HTTPS, substituir credenciais padrão e revisar regras de firewall do banco

---

## Sobre

Desenvolvido por **Rafael Andrade Tanimoto** — estudante de ADS na FIAP, com foco em desenvolvimento backend Java.

[![LinkedIn](https://img.shields.io/badge/LinkedIn-rafaeltanimoto-blue?style=flat&logo=linkedin)](https://linkedin.com/in/rafaeltanimoto)
[![GitHub](https://img.shields.io/badge/GitHub-Famel--svg-black?style=flat&logo=github)](https://github.com/Famel-svg)
