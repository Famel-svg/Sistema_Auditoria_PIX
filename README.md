# Sistema de Auditoria e Rastreabilidade PIX

![CI/CD Pipeline](https://github.com/SEU_USUARIO/SEU_REPOSITORIO/actions/workflows/maven.yml/badge.svg)

Sistema back-end em **Java 21** e **Spring Boot** que expõe uma API REST para operações de transferência Pix com **rastreabilidade total** de cada alteração de dados — respondendo às perguntas de compliance:

- **Quem** realizou a operação?
- **Quando** foi realizada?
- **O que exatamente mudou** nos dados (valor antes × depois)?
- **De onde** veio a requisição (IP, endpoint)?

Desenvolvido com conformidade às normas do Banco Central (Resolução BCB nº 1/2020) e **LGPD**.

---

## 🚀 Stack Técnica

| Tecnologia | Versão | Papel |
| :--- | :--- | :--- |
| Java | 21 (LTS) | Linguagem principal |
| Spring Boot | 3.3.4 | Framework base |
| Hibernate Envers | 6.5.x | Auditoria automática de entidades JPA |
| Spring AOP (AspectJ) | incluso no Boot | Interceptação transparente de requisições |
| Spring Security | incluso no Boot | Contexto de autenticação por operação |
| PostgreSQL | 16 | Banco de dados principal |
| H2 | 2.4.x | Banco in-memory exclusivo para testes |
| JUnit 5 | incluso no Boot | Framework de testes unitários e de integração |
| JaCoCo | 0.8.12 | Cobertura de código (mínimo 80%) |
| GitHub Actions | — | Pipeline CI/CD automatizado |
| Docker & Docker Compose | — | Infraestrutura local |

---

## 🛡️ Estratégia de Auditoria Híbrida

Duas cadeias de auditoria operam em paralelo, sem acoplamento entre si:

| Ferramenta | O que captura | Tabela gerada |
| :--- | :--- | :--- |
| **Hibernate Envers** | Diff de campos (antes × depois) a cada INSERT/UPDATE/DELETE | `pix_transferencia_aud` |
| **Spring AOP** | Contexto HTTP da requisição (usuário, IP, endpoint, método) | `audit_log` |

O `AuditAspect` não conhece o `PixService` — a interceptação é **totalmente transparente**. O `PixService` não sabe que está sendo auditado.

### Por que dois mecanismos e não apenas um?

| Ferramenta | O que **não** captura |
| :--- | :--- |
| Envers sozinho | Contexto HTTP — não sabe quem ou de onde veio |
| AOP sozinho | Diff de campos — não sabe o que mudou nos dados |

A combinação responde à pergunta completa de compliance.

---

## 🏗️ Arquitetura

```
HTTP Request
     ↓
Spring Security          ← extrai usuário autenticado
     ↓
AuditAspect (@Around)    ← intercepta ANTES e DEPOIS do Service
     ↓  └─────────────────────────→ AuditLogService.registrar()
     ↓                                  └→ salva em audit_log
     ↓                                     (transação própria - REQUIRES_NEW)
PixService               ← executa a lógica de negócio
     ↓
JPA + Hibernate Envers   ← persiste entidade + grava diff automático
     ↓        └──────────────────────→ salva em pix_transferencia_aud
PostgreSQL
```

> **Detalhe importante:** o `AuditLogService` usa `@Transactional(REQUIRES_NEW)` em bean separado do `AuditAspect`. Isso garante que o log seja commitado em transação independente — mesmo que o `PixService` faça rollback, o registro de auditoria da operação com falha é preservado.

---

## 📁 Estrutura do Projeto

```
src/main/java/br/com/pixauditoria/
├── PixAuditoriaApplication.java
├── aspect/
│   └── AuditAspect.java              ← interceptação AOP
├── config/
│   ├── EnversConfig.java             ← configuração explícita do Envers
│   └── SecurityConfig.java           ← Basic Auth para dev/demo
├── controller/
│   ├── GlobalExceptionHandler.java   ← mapeamento de exceções → HTTP
│   └── PixController.java            ← endpoints REST
├── domain/entity/
│   ├── AuditLog.java                 ← entidade de log de contexto
│   ├── PixTransferencia.java         ← entidade principal @Audited
│   └── TransferenciaStatus.java      ← enum PENDENTE/CONCLUIDA/CANCELADA/FALHA
├── dto/
│   ├── AuditLogResponse.java
│   ├── PixRequest.java               ← Bean Validation na entrada
│   └── PixResponse.java
├── exception/
│   ├── TransferenciaInvalidaException.java
│   └── TransferenciaNaoEncontradaException.java
├── repository/
│   ├── AuditLogRepository.java
│   └── PixTransferenciaRepository.java
└── service/
    ├── AuditLogService.java          ← persistência do log (REQUIRES_NEW)
    └── PixService.java               ← regras de negócio

src/test/java/br/com/pixauditoria/
├── AuditAspectTest.java              ← integração end-to-end (H2 + RANDOM_PORT)
├── EnversConfigTest.java             ← unitário puro da configuração
├── PixControllerTest.java            ← camada web com MockMvc
└── PixServiceTest.java               ← unitário com Mockito
```

---

## ⚙️ Configuração e Segurança

As credenciais são externalizadas via variáveis de ambiente — **nenhum segredo está no código**.

### Setup local

```bash
# 1. Copiar o template de variáveis
cp .env.example .env

# 2. Preencher o .env com suas credenciais reais
# DB_URL, DB_USERNAME, DB_PASSWORD, SECURITY_USER, SECURITY_PASS
```

### Perfis disponíveis

| Perfil | Comando | Comportamento |
| :--- | :--- | :--- |
| `dev` (padrão) | `./mvnw spring-boot:run` | SQL visível, log DEBUG, ddl-auto=update |
| `prod` | `--spring.profiles.active=prod` | Sem SQL, log WARN, ddl-auto=validate, sem defaults nas variáveis |

> No perfil `prod`, a ausência de qualquer variável de ambiente causa **falha intencional** na inicialização — melhor falhar ao subir do que rodar com credencial errada.

---

## 🛠️ Como Executar

### Pré-requisitos
- Java 21 JDK
- Maven 3.9+
- Docker e Docker Compose

### Passo a Passo

```bash
# 1. Clonar o repositório
git clone https://github.com/SEU_USUARIO/SEU_REPOSITORIO.git
cd pix-auditoria

# 2. Configurar variáveis de ambiente
cp .env.example .env
# Edite o .env com suas credenciais

# 3. Subir o PostgreSQL via Docker
docker-compose up -d

# 4. Executar a aplicação
./mvnw spring-boot:run
```

---

## 🧪 Testes

Todos os testes rodam **sem Docker e sem banco externo** — usam H2 in-memory.

```bash
# Rodar todos os testes
./mvnw test

# Rodar testes + gerar relatório de cobertura
./mvnw verify

# Abrir relatório de cobertura (Windows)
start target\site\jacoco\index.html
```

### Estratégia de testes

| Classe | Tipo | O que valida |
| :--- | :--- | :--- |
| `PixServiceTest` | Unitário (JUnit 5) | Regras de negócio, transições de estado, exceções |
| `PixControllerTest` | Camada web (MockMvc) | Status HTTP, Bean Validation, SecurityConfig |
| `EnversConfigTest` | Unitário puro | As 4 propriedades do Envers configuradas |
| `AuditAspectTest` | Integração (H2 + HTTP real) | Log salvo em sucesso, em falha, IP e endpoint capturados |

> O `AuditAspectTest` usa `RANDOM_PORT` para subir um servidor HTTP real — necessário porque o `AuditAspect` captura IP e endpoint via `RequestContextHolder`, que só existe em contexto de request real.

### Cobertura mínima

JaCoCo configurado com **80% de cobertura de linhas** como requisito de build. Classes excluídas da contagem: DTOs, entidades JPA, exceptions e a classe `main`.

---

## 📋 Endpoints da API

| Método | Path | Descrição | Status de sucesso |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/pix/transferencias` | Cria uma nova transferência Pix | `201 Created` |
| `GET` | `/api/pix/transferencias/{id}` | Busca detalhes de uma transferência | `200 OK` |
| `GET` | `/api/pix/transferencias/{id}/historico` | Histórico de revisões via Envers | `200 OK` |
| `PATCH` | `/api/pix/transferencias/{id}/cancelar` | Cancela uma transferência pendente | `200 OK` |
| `GET` | `/api/pix/transferencias?chave={chave}` | Lista transferências por chave Pix | `200 OK` |

### Exemplo de requisição

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

## ⚙️ CI/CD

Pipeline executado automaticamente a cada `push` para `main` ou `develop` e em pull requests.

```
build → testes unitários → testes de integração → cobertura JaCoCo → build Docker
```

| Etapa | O que faz |
| :--- | :--- |
| **Build** | Compila o projeto com Maven |
| **Testes unitários** | `PixServiceTest`, `PixControllerTest`, `EnversConfigTest` — sem banco |
| **Testes de integração** | `AuditAspectTest` — H2 in-memory, sem Docker |
| **Cobertura** | Valida mínimo de 80% e publica relatório HTML como artefato |
| **Build Docker** | Gera imagem da aplicação via `spring-boot:build-image` |

> Relatório de cobertura disponível em cada execução: **Actions → sua execução → jacoco-report**

---

## 🗄️ Modelo de Dados

### `pix_transferencia` — entidade principal

| Campo | Tipo | Descrição |
| :--- | :--- | :--- |
| `id` | UUID | Chave primária |
| `chave_origem` | VARCHAR | Chave Pix do remetente |
| `chave_destino` | VARCHAR | Chave Pix do destinatário |
| `valor` | DECIMAL(15,2) | Valor em reais (`BigDecimal`) |
| `status` | VARCHAR | `PENDENTE` / `CONCLUIDA` / `CANCELADA` / `FALHA` |
| `created_at` | TIMESTAMP | Criação automática |
| `updated_at` | TIMESTAMP | Atualização automática |

### `pix_transferencia_aud` — gerada automaticamente pelo Envers

| Campo | Tipo | Descrição |
| :--- | :--- | :--- |
| `id` | UUID | FK para a entidade original |
| `rev_id` | INTEGER | Número da revisão |
| `rev_type` | SMALLINT | `0`=INSERT, `1`=UPDATE, `2`=DELETE |
| demais campos | — | Estado da entidade nessa revisão |

### `audit_log` — preenchida pelo AOP

| Campo | Tipo | Descrição |
| :--- | :--- | :--- |
| `id` | UUID | Chave primária |
| `usuario` | VARCHAR | Username do `SecurityContext` |
| `ip` | VARCHAR | IP da requisição HTTP |
| `operacao` | VARCHAR | Nome do método interceptado |
| `endpoint` | VARCHAR | Path HTTP |
| `entidade_afetada` | VARCHAR | Nome da entidade |
| `timestamp` | TIMESTAMP | Momento exato da interceptação |