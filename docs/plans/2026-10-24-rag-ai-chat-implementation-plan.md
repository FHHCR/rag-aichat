# Rag-AiChat v0.1-v0.4 Implementation Plan

这是 Day 1 实施计划，只覆盖 `v0.1-v0.4`。完整几天路线图见：

- `docs/plans/2026-10-24-rag-ai-chat-days-plan.md`

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` or `superpowers:executing-plans` to implement this plan task-by-task.

**Goal:** Finish v0.1-v0.4 for Rag-AiChat: backend scaffold, frontend scaffold, PostgreSQL/Redis/pgvector foundation, and login with JWT.

**Architecture:** SpringBoot provides REST APIs, Spring Security + JWT authentication, Spring Data JPA entities, and PostgreSQL/Redis configuration. Vue 3 provides the login page, router, auth store, Axios wrapper, chat placeholder, and admin placeholder. This plan intentionally stops before Tika parsing, embedding, vector retrieval, LLM chat, and full admin features.

**Tech Stack:** SpringBoot, Spring Security, Spring Data JPA, PostgreSQL, pgvector, Redis, Maven, Vue 3, Vite, TypeScript, Vue Router, Pinia, Axios.

**Spec:** `docs/specs/2026-10-24-rag-ai-chat-spec.md`

## Global Constraints

- Only implement v0.1-v0.4 on Day 1.
- Do not implement Apache Tika parsing, embedding, vector retrieval, LLM chat, AgentScope Java 2.0 RAG orchestration, full knowledge base management, or full admin management yet.
- AgentScope Java 2.0 is reserved for v0.9 RAG orchestration, retrieval/generation flow, and chat integration.
- Java package must use lowercase: `com.rag.aichat`.
- JWT must be sent as `Authorization: Bearer <token>`.
- Passwords and password hashes must never be returned by API.
- `/api/health` and `/api/auth/login` are public.
- `/api/auth/me` requires JWT.
- Backend uses PostgreSQL as both business database and pgvector host.
- Redis is configured and connectivity-checked.
- Frontend token storage uses `localStorage` for this version.

## Files To Create

### Backend

- `backend/pom.xml`
- `backend/src/main/resources/application.yml`
- `backend/src/main/java/com/rag/aichat/RagAiChatApplication.java`
- `backend/src/main/java/com/rag/aichat/common/ApiResponse.java`
- `backend/src/main/java/com/rag/aichat/common/BusinessException.java`
- `backend/src/main/java/com/rag/aichat/common/GlobalExceptionHandler.java`
- `backend/src/main/java/com/rag/aichat/config/WebConfig.java`
- `backend/src/main/java/com/rag/aichat/config/SecurityConfig.java`
- `backend/src/main/java/com/rag/aichat/config/SecurityProperties.java`
- `backend/src/main/java/com/rag/aichat/controller/HealthController.java`
- `backend/src/main/java/com/rag/aichat/entity/User.java`
- `backend/src/main/java/com/rag/aichat/entity/KnowledgeBase.java`
- `backend/src/main/java/com/rag/aichat/entity/DocumentFile.java`
- `backend/src/main/java/com/rag/aichat/entity/DocumentChunk.java`
- `backend/src/main/java/com/rag/aichat/entity/ChatSession.java`
- `backend/src/main/java/com/rag/aichat/entity/ChatMessage.java`
- `backend/src/main/java/com/rag/aichat/entity/AuditLog.java`
- `backend/src/main/java/com/rag/aichat/repository/UserRepository.java`
- `backend/src/main/java/com/rag/aichat/repository/KnowledgeBaseRepository.java`
- `backend/src/main/java/com/rag/aichat/repository/DocumentFileRepository.java`
- `backend/src/main/java/com/rag/aichat/repository/DocumentChunkRepository.java`
- `backend/src/main/java/com/rag/aichat/repository/ChatSessionRepository.java`
- `backend/src/main/java/com/rag/aichat/repository/ChatMessageRepository.java`
- `backend/src/main/java/com/rag/aichat/repository/AuditLogRepository.java`
- `backend/src/main/java/com/rag/aichat/security/JwtTokenProvider.java`
- `backend/src/main/java/com/rag/aichat/security/JwtAuthenticationFilter.java`
- `backend/src/main/java/com/rag/aichat/security/LoginUser.java`
- `backend/src/main/java/com/rag/aichat/dto/AuthLoginRequest.java`
- `backend/src/main/java/com/rag/aichat/dto/AuthLoginResponse.java`
- `backend/src/main/java/com/rag/aichat/dto/UserInfoResponse.java`
- `backend/src/main/java/com/rag/aichat/service/AuthService.java`
- `backend/src/main/java/com/rag/aichat/controller/AuthController.java`
- `backend/src/main/resources/db/migration/V1__init_schema.sql`
- `backend/src/main/resources/db/migration/V2__init_default_admin.sql`
- `backend/src/test/java/com/rag/aichat/controller/HealthControllerTest.java`
- `backend/src/test/java/com/rag/aichat/security/JwtTokenProviderTest.java`
- `backend/src/test/java/com/rag/aichat/controller/AuthControllerTest.java`
- `backend/src/test/java/com/rag/aichat/config/InfrastructureTest.java`

### Frontend

- `frontend/package.json`
- `frontend/vite.config.ts`
- `frontend/tsconfig.json`
- `frontend/index.html`
- `frontend/src/main.ts`
- `frontend/src/App.vue`
- `frontend/src/router/index.ts`
- `frontend/src/stores/auth.ts`
- `frontend/src/api/http.ts`
- `frontend/src/api/auth.ts`
- `frontend/src/views/login/LoginView.vue`
- `frontend/src/views/chat/ChatView.vue`
- `frontend/src/views/admin/AdminView.vue`
- `frontend/src/layouts/BaseLayout.vue`
- `frontend/src/components/TopBar.vue`

## Task v0.1: Backend Scaffold

Status: completed on 2026-10-24.

**Files:**

- Create: `backend/pom.xml`
- Create: `backend/src/main/resources/application.yml`
- Create: `backend/src/main/java/com/rag/aichat/RagAiChatApplication.java`
- Create: `backend/src/main/java/com/rag/aichat/common/ApiResponse.java`
- Create: `backend/src/main/java/com/rag/aichat/common/BusinessException.java`
- Create: `backend/src/main/java/com/rag/aichat/common/GlobalExceptionHandler.java`
- Create: `backend/src/main/java/com/rag/aichat/config/WebConfig.java`
- Create: `backend/src/main/java/com/rag/aichat/config/SecurityConfig.java`
- Create: `backend/src/main/java/com/rag/aichat/config/SecurityProperties.java`
- Create: `backend/src/main/java/com/rag/aichat/controller/HealthController.java`
- Create: `backend/src/test/java/com/rag/aichat/controller/HealthControllerTest.java`

**Produces:**

- Bootable SpringBoot application.
- `GET /api/health`.
- Unified response shape: `code`, `message`, `data`.

- [x] **Step 1: Add Maven dependencies**

Add SpringBoot parent, Spring Web, Spring Security, Spring Data JPA, PostgreSQL driver, Spring Boot Data Redis, validation, Jackson, JJWT, Flyway, Lombok optional, and test dependencies.

- [x] **Step 2: Add application configuration**

`application.yml` must include server port, Spring datasource, JPA, Redis, JWT secret/expiration, upload path, and migration settings. Default backend port: `8080`.

- [x] **Step 3: Add app entrypoint**

```java
package com.rag.aichat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RagAiChatApplication {
    public static void main(String[] args) {
        SpringApplication.run(RagAiChatApplication.class, args);
    }
}
```

- [x] **Step 4: Add `ApiResponse`**

Use a generic wrapper:

```java
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;
}
```

- [x] **Step 5: Add health endpoint**

`GET /api/health` returns:

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "status": "UP"
  }
}
```

- [x] **Step 6: Add public security rule**

`/api/health` and `/api/auth/login` must be permit-all in this task.

- [x] **Step 7: Verify**

Run:

```bash
cd backend
mvn test
```

Expected: health controller test passes. Database-dependent tests may be excluded until v0.3.

- [x] **Step 8: Commit**

```bash
git add backend
git commit -m "feat: add backend scaffold"
```

## Task v0.2: Frontend Scaffold

Status: completed on 2026-10-24.

**Files:**

- Create: Vue frontend files listed above except `auth.ts` implementation details completed in v0.4.
- Create: `frontend/src/api/http.ts`
- Create: `frontend/src/stores/auth.ts`
- Create: `frontend/src/router/index.ts`
- Create placeholder views for `/login`, `/chat`, `/admin`.

**Consumes:**

- Future `/api/auth/login` and `/api/auth/me`.

**Produces:**

- Frontend routes `/login`, `/chat`, `/admin`.
- Router guards redirect unauthenticated users to `/login`.
- Axios wrapper attaches token when present.

- [x] **Step 1: Initialize Vue structure**

Use Vue 3, Vite, TypeScript, Vue Router, Pinia, Axios, and Vue official tooling.

- [x] **Step 2: Create Axios wrapper**

Default backend URL: `http://localhost:8080`.

Request behavior:

- Read token from auth store or `localStorage`.
- Attach `Authorization: Bearer <token>` when token exists.
- On HTTP 401, clear token/user and redirect to `/login`.

- [x] **Step 3: Create auth store**

Store shape:

```ts
{
  token: string | null,
  user: UserInfo | null
}
```

Store must persist token and user to `localStorage`.

- [x] **Step 4: Create router guards**

Public route:

- `/login`

Protected routes:

- `/chat`
- `/admin`

If no token exists, redirect to `/login`. If token exists and visiting `/login`, redirect to `/chat`.

- [x] **Step 5: Create placeholder pages**

- Login page: form with username/password, disabled until v0.4 wires API.
- Chat page: simple title and empty message area.
- Admin page: simple title and placeholder panel.

- [x] **Step 6: Verify**

Run:

```bash
cd frontend
npm install
npm run build
```

Expected: frontend build succeeds.

- [x] **Step 7: Commit**

```bash
git add frontend
git commit -m "feat: add frontend scaffold"
```

## Task v0.3: Database And Infrastructure

Status: completed on 2026-10-24.

**Files:**

- Create: `backend/src/main/resources/db/migration/V1__init_schema.sql`
- Create: `backend/src/main/resources/db/migration/V2__init_default_admin.sql`
- Create: entity files listed above.
- Create: repository files listed above.
- Create: `backend/src/test/java/com/rag/aichat/config/InfrastructureTest.java`

**Produces:**

- PostgreSQL schema.
- pgvector extension.
- Redis connectivity.
- Development default admin account.
- JPA entities and repositories.

- [x] **Step 1: Enable pgvector**

Migration must run:

```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

- [x] **Step 2: Create tables**

Create these tables:

- `users`
- `knowledge_bases`
- `document_files`
- `document_chunks`
- `chat_sessions`
- `chat_messages`
- `audit_logs`

Use UUID primary keys or configured ID strategy consistently. Store vector column in `document_chunks.embedding`.

- [x] **Step 3: Seed default admin**

Default development admin:

- username: `admin`
- password: `admin123`
- displayName: `Administrator`
- role: `ADMIN`
- status: `ACTIVE`

Store only BCrypt hash, not plain password.

- [x] **Step 4: Add entities**

Entities must not expose password hash through API DTOs. `User` may persist `passwordHash`, but response DTOs must not include it.

- [x] **Step 5: Add repositories**

Create repository interfaces for every entity. `UserRepository` must expose:

```java
Optional<User> findByUsername(String username);
```

- [x] **Step 6: Add infrastructure smoke test**

Test PostgreSQL connection and Redis connection using application context or explicit test beans.

- [x] **Step 7: Verify**

Run:

```bash
cd backend
mvn test
```

Expected: migration and infrastructure tests pass when PostgreSQL, Redis, and pgvector are available.

- [x] **Step 8: Commit**

```bash
git add backend
git commit -m "feat: add database schema and infrastructure"
```

## Task v0.4: Login And JWT

Status: completed on 2026-10-24.

**Files:**

- Create: `backend/src/main/java/com/rag/aichat/security/JwtTokenProvider.java`
- Create: `backend/src/main/java/com/rag/aichat/security/JwtAuthenticationFilter.java`
- Create: `backend/src/main/java/com/rag/aichat/security/LoginUser.java`
- Create: `backend/src/main/java/com/rag/aichat/dto/AuthLoginRequest.java`
- Create: `backend/src/main/java/com/rag/aichat/dto/AuthLoginResponse.java`
- Create: `backend/src/main/java/com/rag/aichat/dto/UserInfoResponse.java`
- Create: `backend/src/main/java/com/rag/aichat/service/AuthService.java`
- Create: `backend/src/main/java/com/rag/aichat/controller/AuthController.java`
- Create: `backend/src/test/java/com/rag/aichat/security/JwtTokenProviderTest.java`
- Create: `backend/src/test/java/com/rag/aichat/controller/AuthControllerTest.java`
- Update: `frontend/src/api/auth.ts`
- Update: `frontend/src/stores/auth.ts`
- Update: `frontend/src/views/login/LoginView.vue`
- Update: `frontend/src/router/index.ts`

**Produces:**

- `POST /api/auth/login`
- `GET /api/auth/me`
- `JwtTokenProvider.createToken(User user) -> String`
- `JwtTokenProvider.parseToken(String token) -> Claims`
- Frontend login flow.

- [x] **Step 1: Add DTOs**

`AuthLoginRequest`:

- `username`
- `password`

`UserInfoResponse`:

- `id`
- `username`
- `displayName`
- `role`

`AuthLoginResponse`:

- `token`
- `user`

- [x] **Step 2: Add JWT tests**

Test token creation and parsing round trip for a known user id, username, role, and expiration.

- [x] **Step 3: Implement `JwtTokenProvider`**

Use configured secret and expiration. Token claims must include user id, username, and role.

- [x] **Step 4: Implement `JwtAuthenticationFilter`**

Read header:

```text
Authorization: Bearer <token>
```

Parse token, load user by id, and set Spring Security authentication context.

- [x] **Step 5: Implement `AuthService`**

Login behavior:

- Find user by username.
- Verify password with BCrypt.
- Reject missing user or wrong password.
- Reject disabled user.
- Return JWT and sanitized user info.

- [x] **Step 6: Implement `AuthController`**

`POST /api/auth/login` uses `AuthLoginRequest` and returns `AuthLoginResponse`.

`GET /api/auth/me` returns `UserInfoResponse`.

- [x] **Step 7: Update frontend API**

`auth.ts` must expose:

```ts
login(username: string, password: string): Promise<LoginResult>
fetchCurrentUser(): Promise<UserInfo>
```

- [x] **Step 8: Update login page**

Submit username/password, save token/user, then redirect by role:

- ADMIN -> `/admin`
- USER -> `/chat`

- [x] **Step 9: Verify backend**

Run:

```bash
cd backend
mvn test
```

Expected: JWT and auth tests pass.

- [x] **Step 10: Verify frontend**

Run:

```bash
cd frontend
npm run build
```

Expected: frontend build succeeds.

- [x] **Step 11: Commit**

```bash
git add backend frontend
git commit -m "feat: add login and jwt"
```

## Acceptance Criteria

v0.1-v0.4 is complete when:

- Backend starts locally.
- Frontend starts locally.
- `GET /api/health` returns success.
- PostgreSQL is connected.
- Redis is connected.
- pgvector extension is enabled.
- Base tables are created.
- Default admin exists for development.
- Login endpoint issues JWT.
- `GET /api/auth/me` returns current user.
- Frontend login page can log in and show current user.
- Passwords are never returned by API.

## Follow-up Versions

- v0.5: full USER/ADMIN access control.
- v0.6: knowledge base CRUD.
- v0.7: file upload and Apache Tika parsing.
- v0.8: text chunking, embedding, and pgvector storage.
- v0.9: AgentScope Java 2.0-based vector retrieval, LLM answer, sources, and full chat page.
- v1.0: reindex, user/admin management, system status, tests, and startup docs.



