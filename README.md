# Sistema de Auditoria e Rastreabilidade PIX

Este projeto consiste em um sistema back-end desenvolvido em **Java 21** e **Spring Boot** que expõe uma API REST para operações de transferência Pix, garantindo conformidade com normas de compliance bancário (Resolução BCB nº 1/2020) e **LGPD**.

O diferencial técnico reside na **rastreabilidade total**, respondendo quem realizou a operação, quando, o que mudou nos dados e de onde veio a requisição (IP e endpoint).

---

## 🚀 Tecnologias e Stack Técnica

*   **Java 21 (LTS)**: Utilização de recursos modernos como Virtual Threads.
*   **Spring Boot 3.x**: Framework base para a construção da API.
*   **Hibernate Envers**: Auditoria automática e versionamento das entidades JPA (Tabelas `_aud`).
*   **Spring AOP (AspectJ)**: Interceptação transparente de requisições para log de contexto HTTP.
*   **Spring Security**: Gestão de contexto de autenticação para identificação do autor das operações.
*   **PostgreSQL**: Banco de dados relacional para persistência robusta.
*   **Docker & Docker Compose**: Containerização completa da aplicação e infraestrutura.
*   **Testcontainers**: Validação de testes de integração com instâncias reais de banco de dados.

---

## 🏗️ Arquitetura: Modular Monolith

O projeto adota a estrutura de Monólito Modular, inspirada em **Clean Architecture**, para garantir baixo acoplamento e alta coesão. O código é organizado nos seguintes pacotes:

*   **domain**: Núcleo do sistema com entidades, enums e interfaces de repositório.
*   **application**: Casos de uso e orquestração da lógica de negócio.
*   **infrastructure**: Implementações concretas de persistência e integrações.
*   **api**: Controladores REST e definições de DTOs.
*   **aspect**: Camada isolada para auditoria transversal (AOP).

---

## 🛡️ Estratégia de Auditoria Híbrida

Para garantir rastreabilidade completa, o sistema utiliza duas cadeias que operam em paralelo:

| Ferramenta | O que captura | Finalidade |
| :--- | :--- | :--- |
| **Hibernate Envers** | Diff de campos (antes x depois) | Histórico imutável de alterações nos dados. |
| **Spring AOP** | Contexto HTTP (IP, Usuário, Endpoint) | Identificação da origem e autoria da requisição. |

---

## 🛠️ Como Executar

### Pré-requisitos
*   Java 21 JDK
*   Maven 3.9+
*   Docker e Docker Compose

### Passo a Passo

1. **Clonar o repositório:**
   ```bash
   git clone https://github.com/seu-usuario/auditoria_e_rastreabilidade.git
   cd auditoria_e_rastreabilidade
   ```

2. **Configurar variáveis de ambiente:**
   Copie o arquivo `.env.example` para `.env` (se disponível) e ajuste as credenciais se necessário.

3. **Subir a infraestrutura (PostgreSQL):**
   ```bash
   docker-compose up -d
   ```

4. **Executar a aplicação:**
   ```bash
   ./mvnw spring-boot:run
   ```

---

## 📋 Endpoints Principais

A documentação interativa completa pode ser acessada via Swagger UI em: `http://localhost:8080/swagger-ui.html`

| Método | Path | Descrição |
| :--- | :--- | :--- |
| `POST` | `/api/pix/transferencias` | Cria uma nova transferência Pix. |
| `GET` | `/api/pix/transferencias/{id}` | Busca detalhes de uma transferência. |
| `GET` | `/api/pix/transferencias/{id}/historico` | Retorna o histórico de revisões (Envers). |
| `PATCH` | `/api/pix/transferencias/{id}/cancelar` | Realiza o cancelamento de uma transferência. |
| `GET` | `/api/pix/audit` | Consulta logs de contexto (AOP) com filtros. |