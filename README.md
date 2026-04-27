# Sistema de Auditoria e Rastreabilidade PIX

![CI/CD Pipeline](https://github.com/SEU_USUARIO/SEU_REPOSITORIO/actions/workflows/maven.yml/badge.svg)

Sistema back-end desenvolvido em **Java 21** e **Spring Boot** que expõe uma API REST para operações de transferência Pix, garantindo conformidade com normas de compliance bancário (Resolução BCB nº 1/2020) e **LGPD**.

O diferencial técnico reside na **rastreabilidade total**, respondendo quem realizou a operação, quando, o que mudou nos dados e de onde veio a requisição (IP e endpoint).

---

## 🚀 Tecnologias e Stack Técnica

- **Java 21 (LTS)**: Linguagem principal com recursos modernos.
- **Spring Boot 3.x**: Framework base para a construção da API.
- **Hibernate Envers**: Auditoria automática e versionamento das entidades JPA (tabelas `_aud`).
- **Spring AOP (AspectJ)**: Interceptação transparente de requisições para log de contexto HTTP.
- **Spring Security**: Gestão de contexto de autenticação para identificação do autor das operações.
- **PostgreSQL**: Banco de dados relacional para persistência robusta.
- **Docker & Docker Compose**: Containerização da infraestrutura local.
- **Testcontainers**: Testes de integração com instância real de PostgreSQL via Docker.
- **GitHub Actions**: Pipeline de CI/CD automatizado.

---

## 🛡️ Estratégia de Auditoria Híbrida

Duas cadeias de auditoria operam em paralelo para garantir rastreabilidade completa:

| Ferramenta | O que captura | Finalidade |
| :--- | :--- | :--- |
| **Hibernate Envers** | Diff de campos (antes × depois) | Histórico imutável de alterações nos dados |
| **Spring AOP** | Contexto HTTP (IP, usuário, endpoint) | Identificação da origem e autoria da requisição |

---

## 🛠️ Como Executar

### Pré-requisitos
- Java 21 JDK
- Maven 3.9+
- Docker e Docker Compose

### Passo a Passo

1. **Clonar o repositório:**
   ```bash
   git clone https://github.com/SEU_USUARIO/SEU_REPOSITORIO.git
   cd pix-auditoria
   ```

2. **Configurar variáveis de ambiente:**
   ```bash
   cp .env.example .env
   # Edite o .env com suas credenciais locais
   ```

3. **Subir a infraestrutura (PostgreSQL):**
   ```bash
   docker-compose up -d
   ```

4. **Executar a aplicação:**
   ```bash
   ./mvnw spring-boot:run
   ```

### Como Rodar os Testes

```bash
# Todos os testes (requer Docker para os de integração)
./mvnw test

# Apenas testes unitários (sem Docker)
./mvnw test -Dtest="PixServiceTest,PixControllerTest,EnversConfigTest"

# Apenas testes de integração
./mvnw test -Dtest="AuditAspectTest"
```

---

## 📋 Endpoints da API

| Método | Path | Descrição |
| :--- | :--- | :--- |
| `POST` | `/api/pix/transferencias` | Cria uma nova transferência Pix |
| `GET` | `/api/pix/transferencias/{id}` | Busca detalhes de uma transferência |
| `GET` | `/api/pix/transferencias/{id}/historico` | Retorna o histórico de revisões (Envers) |
| `PATCH` | `/api/pix/transferencias/{id}/cancelar` | Cancela uma transferência pendente |
| `GET` | `/api/pix/transferencias?chave={chave}` | Lista transferências por chave Pix |

---

## ⚙️ CI/CD

O pipeline é executado automaticamente a cada `push` para `main` ou `develop` e em pull requests. As etapas são:

1. **Build** — compila o projeto com Maven
2. **Testes unitários** — executa sem dependência de banco de dados
3. **Testes de integração** — sobe PostgreSQL real via Testcontainers
4. **Upload de resultados** — artefatos disponíveis mesmo em caso de falha
5. **Build da imagem Docker** — gera imagem pronta para deploy