# Rag-AiChat v0.1-v0.4 Specification

更新时间：2026-10-24

## 0. 项目几天计划

- Day 1：`v0.1-v0.4`，完成后端脚手架、前端脚手架、数据库基础设施和登录 JWT。
- Day 2：`v0.5-v0.6`，完成角色鉴权和管理员知识库管理。
- Day 3：`v0.7`，完成文件上传、Apache Tika 解析和解析状态管理。
- Day 4：`v0.8`，完成文本切分、embedding 调用和 pgvector 入库。
- Day 5：`v0.9`，基于 AgentScope Java 2.0 完成向量检索、LLM 回答、引用来源和用户聊天页。
- Day 6：`v1.0`，完成重新索引、用户管理、管理员管理、系统状态、测试和启动说明。

## 1. 范围

今天只完成 v0.1-v0.4：后端脚手架、前端脚手架、数据库与基础设施、登录与 JWT。

本文档不覆盖 RAG 完整能力。文件上传、Apache Tika 解析、文本切分、embedding、pgvector 检索、LLM 回答、完整聊天页和完整管理后台将在 v0.5-v1.0 完成。

## 2. 版本目标

### v0.1 后端脚手架

- 创建 SpringBoot 后端项目。
- 配置端口、数据库、Redis、JWT 等基础配置。
- 创建统一响应结构 `ApiResponse`。
- 创建全局异常处理。
- 创建基础 CORS 配置。
- 提供健康检查接口 `GET /api/health`。

交付结果：后端可以启动，健康检查接口可以访问。

### v0.2 前端脚手架

- 创建 Vue 3 + Vite + TypeScript 项目。
- 创建 Vue Router 路由。
- 创建 Pinia 状态管理。
- 创建 Axios 请求封装。
- 创建登录页、用户聊天页占位、管理后台占位。
- 创建基础布局。

交付结果：前端可以启动，可以访问 `/login`、`/chat`、`/admin`。

### v0.3 数据库与基础设施

- 接入 PostgreSQL。
- 启用 pgvector 扩展。
- 接入 Redis。
- 创建基础表结构：用户、知识库、文件、片段、会话、消息、审计日志。
- 创建开发环境默认管理员账号。

交付结果：服务可以连接 PostgreSQL 和 Redis，pgvector 可用，基础表结构就绪。

### v0.4 登录与 JWT

- 创建用户实体和登录校验。
- 创建 JWT 签发和解析。
- 创建 `POST /api/auth/login`。
- 创建 `GET /api/auth/me`。
- 前端完成登录页提交、token 保存和当前用户显示。

交付结果：用户可以用账号密码登录，前端可以保存 token 并显示当前登录用户。

## 3. 技术方向

后端：

- SpringBoot
- Spring Security
- Spring Data JPA
- Spring Web
- JWT
- PostgreSQL
- pgvector
- Redis
- Maven

前端：

- Vue 3
- Vite
- TypeScript
- Vue Router
- Pinia
- Axios

后续版本：

- Apache Tika：文件解析。
- AgentScope Java 2.0：Java RAG 编排、检索/生成链路和后续会话能力接入。
- pgvector：向量存储与检索。
- Embedding API：向量生成。
- LLM API：回答生成。

## 4. 项目目录

后端目录：

```text
backend/
  pom.xml
  src/main/java/com/rag/aichat/
  src/main/resources/
  src/test/java/com/rag/aichat/
```

前端目录：

```text
frontend/
  src/api/
  src/router/
  src/stores/
  src/views/
  src/layouts/
  src/components/
```

## 5. 数据模型

### User

- `id`
- `username`
- `passwordHash`
- `displayName`
- `role`
- `status`
- `createdAt`
- `updatedAt`

角色：

- `USER`
- `ADMIN`

状态：

- `ACTIVE`
- `DISABLED`

### KnowledgeBase

- `id`
- `name`
- `description`
- `status`
- `createdById`
- `createdAt`
- `updatedAt`

### DocumentFile

- `id`
- `knowledgeBaseId`
- `fileName`
- `originalPath`
- `mimeType`
- `fileSize`
- `parser`
- `status`
- `parseError`
- `chunkCount`
- `uploadedById`
- `createdAt`
- `updatedAt`

`parser` 默认值为 `TIKA`，用于后续文件解析。

### DocumentChunk

- `id`
- `documentFileId`
- `knowledgeBaseId`
- `chunkIndex`
- `content`
- `embeddingProvider`
- `embeddingModel`
- `embedding`
- `createdAt`

`embedding` 使用 pgvector 向量字段。今天只创建表结构和配置，不在 v0.1-v0.4 写入向量。

### ChatSession

- `id`
- `userId`
- `title`
- `knowledgeBaseScope`
- `createdAt`
- `updatedAt`

### ChatMessage

- `id`
- `sessionId`
- `userId`
- `role`
- `content`
- `sourceJson`
- `createdAt`

### AuditLog

- `id`
- `actorId`
- `action`
- `targetType`
- `targetId`
- `summary`
- `createdAt`

## 6. API

### 6.1 健康检查

`GET /api/health`

允许匿名访问。

返回示例：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "status": "UP"
  }
}
```

### 6.2 登录

`POST /api/auth/login`

允许匿名访问。

请求示例：

```json
{
  "username": "admin",
  "password": "admin123"
}
```

返回示例：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "token": "jwt-token",
    "user": {
      "id": "uuid",
      "username": "admin",
      "displayName": "Administrator",
      "role": "ADMIN"
    }
  }
}
```

### 6.3 当前用户

`GET /api/auth/me`

需要 JWT。

请求头：

```text
Authorization: Bearer <token>
```

返回当前登录用户信息。响应中不得包含密码、密码哈希或其他敏感字段。

## 7. 权限要求

v0.1-v0.4 只完成登录与 JWT，不完成完整后台权限控制。

- `/api/health` 允许匿名访问。
- `/api/auth/login` 允许匿名访问。
- `/api/auth/me` 需要有效 JWT。
- USER 和 ADMIN 都可以登录。
- `/api/admin/**` 的细粒度权限留到 v0.5。

## 8. 非功能要求

- 后端统一响应格式。
- JWT 只放在请求头，不放在 URL。
- API 不返回密码或密码哈希。
- 默认管理员账号仅用于开发环境。
- 数据库连接、Redis 地址、JWT secret 等敏感配置通过配置文件或环境变量注入。
- 前端 token 今天先用 `localStorage` 保存，后续可再调整。
- 后端 package 使用小写：`com.rag.aichat`。

## 9. 验收标准

v0.1：

- SpringBoot 项目可以启动。
- `GET /api/health` 返回成功。
- 统一响应结构可用。

v0.2：

- Vue 项目可以启动。
- 可以访问 `/login`、`/chat`、`/admin`。
- Axios 封装可以携带 token。

v0.3：

- PostgreSQL 可以连接。
- Redis 可以连接。
- pgvector 扩展可以启用。
- 基础表结构可以创建。
- 开发默认管理员账号可以创建。

v0.4：

- 登录接口可以签发 JWT。
- `GET /api/auth/me` 可以返回当前用户。
- 前端登录页可以提交账号密码。
- 登录成功后可以保存 token 并显示当前用户。
- 登录成功后按角色跳转聊天页或管理后台占位页。

## 10. 后续版本

- v0.5：完整 USER/ADMIN 角色鉴权和管理后台访问控制。
- v0.6：知识库 CRUD。
- v0.7：文件上传、文件元数据和 Apache Tika 解析。
- v0.8：文本切分、embedding 和 pgvector 入库。
- v0.9：基于 AgentScope Java 2.0 完成向量检索、LLM 回答、引用来源和用户聊天页完整实现。
- v1.0：重新索引、用户管理、管理员管理、系统状态、测试和发布说明。
