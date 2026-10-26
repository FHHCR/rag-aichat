# Rag-AiChat v0.7-v0.9 Implementation Plan

**Goal:** Complete Day 3 of Rag-AiChat: chat session persistence, chat API, usable chat UI with message history, and public user registration.

**Architecture:** SpringBoot exposes authenticated chat APIs under `/api/chat/**`. Frontend Vue 3 pages persist the selected session and display messages. This plan keeps LLM and RAG response generation out of scope and stores a local placeholder assistant response for the chat flow.

**Tech Stack:** SpringBoot, Spring Security, JWT, Spring Data JPA, PostgreSQL, H2 test database, Maven, Vue 3, Vite, TypeScript, Vue Router, Pinia, Axios.

**Spec:** `docs/specs/2026-10-26-rag-ai-chat-v0.7-v0.9-spec.md`

**Follow-up scope:** User asked that public user registration be added after Day 3 work. This adds a public registration endpoint and frontend registration flow while keeping admin/user management out of scope.

## Global Constraints

- Today only covers `v0.7-v0.9`.
- User registration creates only `USER` accounts and returns a JWT immediately after success.
- Do not implement file upload, Apache Tika parsing, chunking, embedding, pgvector retrieval, real LLM chat, AgentScope Java 2.0 orchestration, indexing, user management, admin management, or system status today.
- `/api/health` and `/api/auth/login` remain public.
- `/api/auth/me` requires JWT.
- `/api/admin/**` remains ADMIN-only.
- `/api/chat/**` requires an authenticated `USER` or `ADMIN`.
- API responses must use the existing `ApiResponse` wrapper.
- API responses must never include password or password hash.
- Backend package stays under `com.rag.aichat`.
- Frontend route guards must match backend rules.

## File Structure

### Backend files to create

- `backend/src/main/java/com/rag/aichat/dto/CreateChatSessionRequest.java`
- `backend/src/main/java/com/rag/aichat/dto/CreateChatMessageRequest.java`
- `backend/src/main/java/com/rag/aichat/dto/ChatSessionResponse.java`
- `backend/src/main/java/com/rag/aichat/dto/ChatMessageResponse.java`
- `backend/src/main/java/com/rag/aichat/service/ChatSessionService.java`
- `backend/src/main/java/com/rag/aichat/controller/ChatSessionController.java`
- `backend/src/main/java/com/rag/aichat/controller/ChatMessageController.java`
- `backend/src/main/java/com/rag/aichat/controller/AuthRegisterRequest.java` is implemented under `dto/`, not a controller.
- `backend/src/test/java/com/rag/aichat/controller/ChatSessionControllerTest.java`
- `backend/src/test/java/com/rag/aichat/controller/ChatMessageControllerTest.java`

### Backend files to modify

- `backend/src/main/java/com/rag/aichat/repository/ChatSessionRepository.java`
- `backend/src/main/java/com/rag/aichat/repository/ChatMessageRepository.java`
- `backend/src/main/java/com/rag/aichat/dto/AuthRegisterRequest.java`
- `backend/src/main/java/com/rag/aichat/controller/AuthController.java`
- `backend/src/main/java/com/rag/aichat/service/AuthService.java`
- `backend/src/main/java/com/rag/aichat/config/SecurityConfig.java`
- `backend/src/test/java/com/rag/aichat/controller/AuthControllerTest.java`
- `backend/src/main/java/com/rag/aichat/common/GlobalExceptionHandler.java` if validation or ownership errors need dedicated messages.

### Frontend files to create

- `frontend/src/api/chat.ts`

### Frontend files to modify

- `frontend/src/views/chat/ChatView.vue`
- `frontend/src/api/auth.ts`
- `frontend/src/views/login/LoginView.vue`
- `frontend/src/types/api.ts` if response type helpers are needed.

## Task 1: Backend chat DTOs and repository support

**Files:**

- Create: `CreateChatSessionRequest.java`
- Create: `CreateChatMessageRequest.java`
- Create: `ChatSessionResponse.java`
- Create: `ChatMessageResponse.java`
- Modify: `ChatSessionRepository.java`
- Modify: `ChatMessageRepository.java`

**Produces:**

- Request and response DTOs for chat sessions and messages.
- Repository methods for listing current-user sessions by updated time and messages by creation time.

- [x] Create DTOs with validation.
- [x] Add session query methods scoped by user id.
- [x] Add message query methods scoped by session id and user id.

## Task 2: Backend chat service and API ownership

**Files:**

- Create: `ChatSessionService.java`
- Create: `ChatSessionController.java`
- Create: `ChatMessageController.java`
- Test: `ChatSessionControllerTest.java`
- Test: `ChatMessageControllerTest.java`

**Produces:**

- `GET /api/chat/sessions`
- `POST /api/chat/sessions`
- `GET /api/chat/sessions/{id}`
- `DELETE /api/chat/sessions/{id}`
- `GET /api/chat/sessions/{id}/messages`
- `POST /api/chat/sessions/{id}/messages`

- [x] Resolve current user id from JWT principal.
- [x] Enforce ownership for all chat session and message operations.
- [x] Return 404 for missing or foreign chat resources.
- [x] Create default title `新会话` when title is blank.
- [x] Update session `updatedAt` when messages are created.
- [x] Add tests for USER access, ADMIN access, anonymous denial, and ownership isolation.
- [x] Run backend test command:

```bash
cd backend
mvn test
```

## Task 3: Frontend chat API client

**Files:**

- Create: `frontend/src/api/chat.ts`

**Produces:**

- API functions:
  - `listChatSessions()`
  - `createChatSession(input)`
  - `deleteChatSession(id)`
  - `listChatMessages(sessionId)`
  - `createChatMessage(sessionId, input)`

- [x] Add TypeScript types for sessions and messages.
- [x] Reuse the existing Axios instance.
- [x] Preserve existing 401 redirect behavior.

## Task 4: Frontend chat page

**Files:**

- Modify: `frontend/src/views/chat/ChatView.vue`

**Produces:**

- Chat page can list, create, and delete sessions.
- Chat page can select a session and show messages.
- Chat page can send user messages and show a placeholder assistant response.
- User sees clear error messages for validation, permission, and server failures.

- [x] Add session list and message area.
- [x] Add send message form.
- [x] Add clear error messages.
- [x] Run frontend build command:

```bash
cd frontend
npm run build
```

## Task 5: Day 3 closeout

**Files:**

- Modify: `README.md`

**Produces:**

- README version record marks `v0.7-v0.9` complete.

- [x] Update README Day 3 status.
- [x] Mark `v0.7`, `v0.8`, and `v0.9` checked in version log.
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

- [x] Commit Day 3 implementation.
- [x] Push to GitHub.

## Verification Results

- Backend: `cd backend && mvn clean test` passed with 9 tests.
- Frontend: `cd frontend && npm run build` passed.

## Acceptance Criteria

- [x] Backend test suite passes.
- [x] Frontend build passes.
- [x] USER can access `/api/chat/**` and `/chat`.
- [x] ADMIN can access `/api/chat/**` and `/chat`.
- [x] USER cannot access another user's chat session or messages.
- [x] USER can create, list, and delete chat sessions through API.
- [x] USER can list and create messages through API.
- [x] User can create, switch, delete, message, and view sessions through frontend chat page.
- [x] README version log reflects `v0.7-v0.9` completion.
