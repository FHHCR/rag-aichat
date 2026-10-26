# Rag-AiChat v0.5-v0.6 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Complete Day 2 of Rag-AiChat: role-based access control and admin knowledge-base management.

**Architecture:** SpringBoot keeps security rules centralized in Spring Security. Admin-only REST endpoints are placed under `/api/admin/knowledge-bases`. Vue guards use stored user role from login state, while backend rules remain the source of truth.

**Tech Stack:** SpringBoot, Spring Security, JWT, Spring Data JPA, PostgreSQL, Maven, Vue 3, Vite, TypeScript, Vue Router, Pinia, Axios.

**Spec:** `docs/specs/2026-10-25-rag-ai-chat-v0.5-v0.6-spec.md`

## Global Constraints

- Today only covers `v0.5-v0.6`.
- Do not implement file upload, Apache Tika parsing, embedding, pgvector retrieval, LLM chat, or AgentScope Java 2.0 orchestration today.
- `/api/health` and `/api/auth/login` remain public.
- `/api/auth/me` requires JWT.
- `/api/admin/**` requires `ADMIN`.
- `/api/chat/**` requires an authenticated `USER` or `ADMIN`.
- API responses must use the existing `ApiResponse` wrapper.
- API responses must never include password or password hash.
- Backend package stays under `com.rag.aichat`.
- Frontend route guards must match backend rules.

## File Structure

### Backend files to create

- `backend/src/main/java/com/rag/aichat/dto/CreateKnowledgeBaseRequest.java`
- `backend/src/main/java/com/rag/aichat/dto/UpdateKnowledgeBaseRequest.java`
- `backend/src/main/java/com/rag/aichat/dto/KnowledgeBaseResponse.java`
- `backend/src/main/java/com/rag/aichat/service/KnowledgeBaseService.java`
- `backend/src/main/java/com/rag/aichat/controller/KnowledgeBaseController.java`
- `backend/src/test/java/com/rag/aichat/controller/KnowledgeBaseControllerTest.java`

### Backend files to modify

- `backend/src/main/java/com/rag/aichat/config/SecurityConfig.java`
- `backend/src/main/java/com/rag/aichat/common/GlobalExceptionHandler.java`
- `backend/src/main/java/com/rag/aichat/common/BusinessException.java` if custom error codes are needed

### Frontend files to create

- `frontend/src/api/knowledgeBases.ts`

### Frontend files to modify

- `frontend/src/router/index.ts`
- `frontend/src/stores/auth.ts` if route-level helper selectors are useful
- `frontend/src/views/admin/AdminView.vue`
- `frontend/src/views/login/LoginView.vue` if redirect behavior needs cleanup

## Task 1: Role-based backend security

**Files:**

- Modify: `backend/src/main/java/com/rag/aichat/config/SecurityConfig.java`
- Modify: `backend/src/main/java/com/rag/aichat/common/GlobalExceptionHandler.java`
- Test: `backend/src/test/java/com/rag/aichat/controller/AuthControllerTest.java`

**Produces:**

- Public `/api/health`.
- Public `/api/auth/login`.
- JWT-protected `/api/auth/me`.
- ADMIN-only `/api/admin/**`.
- Authenticated USER/ADMIN access to `/api/chat/**`.
- JSON 403 response for authenticated but insufficient-role requests.

- [x] Add or update Spring Security rules:

```java
requestMatchers("/api/health", "/api/auth/login").permitAll()
        .requestMatchers("/api/admin/**").hasRole("ADMIN")
        .requestMatchers("/api/chat/**").hasAnyRole("USER", "ADMIN")
        .anyRequest().authenticated()
```

- [x] Ensure `JwtAuthenticationFilter` creates authorities as `ROLE_ADMIN` or `ROLE_USER`.
- [x] Ensure `AccessDeniedException` is serialized into `ApiResponse` shape.
- [x] Add tests for anonymous admin request and USER admin request if existing controller test fixtures make it practical.
- [x] Run backend test command.

```bash
cd backend
mvn test
```

## Task 2: Frontend route role guard

**Files:**

- Modify: `frontend/src/router/index.ts`
- Modify: `frontend/src/views/login/LoginView.vue`
- Modify: `frontend/src/views/admin/AdminView.vue`

**Produces:**

- ADMIN can access `/admin`.
- USER can access `/chat`.
- USER redirected from `/admin` to `/chat`.
- unauthenticated users redirected to `/login`.

- [x] Update router guard so protected routes check both token and role.
- [x] Keep `/login`, `/chat`, `/admin` route names stable.
- [x] Redirect ADMIN login to `/admin`.
- [x] Redirect USER login to `/chat`.
- [x] Run frontend build command.

```bash
cd frontend
npm run build
```

## Task 3: Knowledge base request/response DTOs

**Files:**

- Create: `backend/src/main/java/com/rag/aichat/dto/CreateKnowledgeBaseRequest.java`
- Create: `backend/src/main/java/com/rag/aichat/dto/UpdateKnowledgeBaseRequest.java`
- Create: `backend/src/main/java/com/rag/aichat/dto/KnowledgeBaseResponse.java`

**Produces:**

- Request DTOs for create and update.
- Response DTO that omits sensitive or internal fields not needed by frontend.

- [x] Create DTOs with validation:

```java
@NotBlank(message = "name is required")
@Size(max = 120, message = "name must be 120 chars or less")
private String name;

@Size(max = 500, message = "description must be 500 chars or less")
private String description;
```

- [x] `KnowledgeBaseResponse` fields:
  - `id`
  - `name`
  - `description`
  - `status`
  - `createdAt`
  - `updatedAt`

## Task 4: Knowledge base service and repository methods

**Files:**

- Create: `backend/src/main/java/com/rag/aichat/service/KnowledgeBaseService.java`
- Modify: `backend/src/main/java/com/rag/aichat/repository/KnowledgeBaseRepository.java` if needed

**Produces:**

- Service methods:
  - `KnowledgeBaseResponse create(CreateKnowledgeBaseRequest request, String createdById)`
  - `List<KnowledgeBaseResponse> list()`
  - `KnowledgeBaseResponse get(String id)`
  - `KnowledgeBaseResponse update(String id, UpdateKnowledgeBaseRequest request)`
  - `void delete(String id)`

- [x] Create knowledge base with status `ACTIVE`.
- [x] List order by `createdAt desc`.
- [x] Throw business exception for missing knowledge base.
- [x] Update only `name` and `description`.
- [x] Delete directly in this version; cascading file behavior is deferred until v0.7+.

## Task 5: Knowledge base admin REST API

**Files:**

- Create: `backend/src/main/java/com/rag/aichat/controller/KnowledgeBaseController.java`
- Test: `backend/src/test/java/com/rag/aichat/controller/KnowledgeBaseControllerTest.java`

**Produces:**

- `POST /api/admin/knowledge-bases`
- `GET /api/admin/knowledge-bases`
- `GET /api/admin/knowledge-bases/{id}`
- `PUT /api/admin/knowledge-bases/{id}`
- `DELETE /api/admin/knowledge-bases/{id}`

- [x] Add controller methods using `ApiResponse`.
- [x] Resolve created-by id from current authenticated user.
- [x] Add tests for create/list/update/delete success path using mocks where practical.
- [x] Run backend test command.

```bash
cd backend
mvn test
```

## Task 6: Frontend knowledge base API and admin page

**Files:**

- Create: `frontend/src/api/knowledgeBases.ts`
- Modify: `frontend/src/views/admin/AdminView.vue`

**Produces:**

- ADMIN page can list, create, edit, and delete knowledge bases.
- User sees clear error messages for validation, permission, and server failures.

- [x] Add API functions:
  - `listKnowledgeBases()`
  - `createKnowledgeBase(input)`
  - `updateKnowledgeBase(id, input)`
  - `deleteKnowledgeBase(id)`
- [x] Add simple admin table/list UI.
- [x] Add create/edit modal or inline form.
- [x] Add delete confirmation.
- [x] Run frontend build command.

```bash
cd frontend
npm run build
```

## Task 7: Day 2 closeout

**Files:**

- Modify: `README.md`

**Produces:**

- README version record marks `v0.5` and `v0.6` complete.

- [x] Update README Day 2 status.
- [x] Mark `v0.5` and `v0.6` checked in version log.
- [x] Keep startup instructions unchanged unless actual commands changed.
- [x] Run final verification:

```bash
cd backend
mvn test
```

```bash
cd frontend
npm run build
```

- [x] Commit Day 2 implementation.
- [x] Push to GitHub.

## Acceptance Criteria

- [x] Backend test suite passes.
- [x] Frontend build passes.
- [x] ADMIN can access `/api/admin/**` and `/admin`.
- [x] USER cannot access `/api/admin/**` or `/admin`.
- [x] USER can still access `/chat` with a valid token.
- [x] Admin can create, list, update, and delete knowledge bases through API.
- [x] Admin can create, list, update, and delete knowledge bases through frontend admin page.
- [x] README version log reflects `v0.5-v0.6` completion.
- [x] 已重新覆盖知识库管理接口测试，测试用户名改为 UUID，匿名访问断言改为 403 JSON
- [x] 后端测试、前端构建、提交和推送已完成
