# Rag-AiChat v0.7-v0.9 Specification

日期：2026-10-26

## 1. 范围

今天完成 `v0.7-v0.9`：聊天会话持久化、聊天 API、聊天 UI、基础聊天消息记录。

用户反馈补充：增加普通用户注册闭环，提供公开注册接口和前端注册入口。注册只创建 `USER` 账号，不包含管理员管理、用户管理、审批、邮箱验证或第三方登录。

本文档不覆盖文件上传、Apache Tika 解析、文本切分、embedding、pgvector 检索、真实 LLM 回答、AgentScope Java 2.0 RAG 编排、重新索引、用户管理和管理员管理。

## 2. 目标

### v0.7 聊天会话与权限基础

- 后端区分 `USER` 和 `ADMIN`。
- JWT 中保留用户角色。
- Spring Security 根据角色控制接口访问。
- 前端根据登录用户角色控制页面跳转和访问。
- USER 和 ADMIN 可以访问 `/api/chat/**`。
- `/api/admin/**` 仅 ADMIN 可访问。
- 普通用户可通过公开注册接口创建自己的 `USER` 账号。

### v0.8 聊天 API

- 支持聊天会话创建、查询、删除。
- 支持会话消息查询和创建。
- 所有聊天资源按当前 JWT 用户归属校验。
- 会话不存在或不属于当前用户时返回 404。

### v0.9 聊天 UI

- 前端聊天页面展示当前用户会话列表。
- 支持新建、切换、删除会话。
- 支持查看历史消息。
- 支持发送用户消息并显示占位 assistant 回复。
- 真实 LLM/RAG 回答不在本版本范围内。

## 3. 权限规则

### 公开接口

- `GET /api/health`
- `POST /api/auth/login`
- `POST /api/auth/register`

### 认证接口

- `GET /api/auth/me`
- `GET /api/chat/sessions`
- `POST /api/chat/sessions`
- `GET /api/chat/sessions/{id}`
- `DELETE /api/chat/sessions/{id}`
- `GET /api/chat/sessions/{id}/messages`
- `POST /api/chat/sessions/{id}/messages`

要求：必须携带有效 JWT。

允许角色：

- `USER`
- `ADMIN`

### 管理员接口

- `/api/admin/**`

要求：必须携带有效 JWT，且角色为 `ADMIN`。

### 用户注册

`POST /api/auth/register`

请求体：

```json
{
  "username": "demo_user",
  "password": "password123",
  "confirmPassword": "password123",
  "displayName": "Demo User"
}
```

规则：

- `username` 必填，去除首尾空白后长度为 3 到 100，只能包含字母、数字、下划线、点或连字符。
- `password` 必填，长度 8 到 100。
- `confirmPassword` 必填，必须与 `password` 一致。
- `displayName` 必填，去除首尾空白后长度为 1 到 100。
- 用户名必须唯一；重复注册返回 409。
- 注册成功创建 `USER` 角色、`ACTIVE` 状态用户。
- 响应使用登录成功同样的结构，包含 `token` 和 `user`，响应不得包含密码或密码哈希。

## 4. API

所有响应继续使用现有统一结构：

```json
{
  "code": 0,
  "message": "ok",
  "data": {}
}
```

### 4.1 查询当前用户会话列表

`GET /api/chat/sessions`

返回当前用户会话数组，按更新时间倒序排列。

返回字段：

- `id`
- `title`
- `createdAt`
- `updatedAt`

### 4.2 创建聊天会话

`POST /api/chat/sessions`

请求体：

```json
{
  "title": "新会话"
}
```

规则：

- `title` 可选。
- 后端去除首尾空白。
- 为空时后端生成默认标题 `新会话`。
- 只能创建当前 JWT 对应用户的会话。
- `knowledge_base_scope` 可空，但必须是 PostgreSQL `jsonb` 类型安全写入。

实现要求：JPA 实体中的 `chat_sessions.knowledge_base_scope` 和 `chat_messages.source_json` 字段必须显式绑定 PostgreSQL JSON 类型，避免将 JSON 文本作为 `character varying` 插入 `jsonb` 列。

### 4.3 查询会话详情

`GET /api/chat/sessions/{id}`

会话不存在或不属于当前用户时返回业务错误。

### 4.4 删除会话

`DELETE /api/chat/sessions/{id}`

只能删除当前 JWT 对应用户的会话。

本版本删除会话时不级联删除消息。

### 4.5 查询会话消息

`GET /api/chat/sessions/{id}/messages`

只返回当前用户会话的消息，按创建时间升序排列。

返回字段：

- `id`
- `role`
- `content`
- `createdAt`

本版本允许角色：

- `user`
- `assistant`

### 4.6 创建聊天消息

`POST /api/chat/sessions/{id}/messages`

请求体：

```json
{
  "role": "user",
  "content": "帮我查一下项目启动步骤"
}
```

规则：

- `role` 必须为 `user` 或 `assistant`。
- `content` 必填，去除首尾空白后不能为空，最大长度 4000。
- 只能写入当前 JWT 对应用户的会话。
- 创建消息后更新会话 `updatedAt`。

本版本不自动调用 LLM 或 RAG 编排。前端可以只保存用户消息和占位 assistant 回复，真实智能问答留到后续版本。

## 5. 前端行为

登录跳转：

- ADMIN 登录成功进入 `/admin`。
- USER 登录成功进入 `/chat`。

路由守卫：

- 无 token 访问受保护页面进入 `/login`。
- USER 访问 `/admin` 跳转到 `/chat`。
- ADMIN 可以访问 `/admin` 和 `/chat`。

聊天页面：

- 展示当前用户会话列表。
- 支持创建新会话。
- 支持删除当前会话。
- 展示选中会话消息。
- 支持发送 `user` 消息。
- 发送消息后追加占位 `assistant` 回复，提示真实 LLM 回答将在后续版本完成。
- 操作失败时展示错误信息。
- 无权限或登录失效时回到登录页或展示认证失败提示。

登录/注册页面：

- 默认展示登录表单。
- 用户可切换到注册表单，输入用户名、密码、确认密码和昵称。
- 注册成功后保存 token 和用户信息，并跳转到 `/chat`。

## 6. 校验规则

会话标题：

- 可选。
- 去除首尾空白后不能为空。
- 最大长度 100。

聊天消息角色：

- 必填。
- 只允许 `user` 或 `assistant`。

聊天消息内容：

- 必填。
- 去除首尾空白后不能为空。
- 最大长度 4000。

注册信息：

- 用户名长度为 3 到 100，只允许字母、数字、下划线、点或连字符。
- 密码长度为 8 到 100。
- 确认密码必须和密码一致。
- 昵称去除首尾空白后长度为 1 到 100。

## 7. 非功能要求

- 不返回密码或密码哈希。
- 聊天接口必须以后端用户归属为准，前端跳转只作为体验控制。
- 使用已有 `ApiResponse` 统一响应。
- 使用已有 JWT 机制，不新增独立 session。
- 不修改 v0.1-v0.6 已完成的基础结构，除非发现阻塞性错误。
- 注册不授予 ADMIN 角色，也不提供管理员审核流程。

## 8. 验收标准

v0.7：

- USER 可以访问 `/api/chat/**`。
- ADMIN 可以访问 `/api/chat/**`。
- USER 不能访问 `/api/admin/**`。
- 无 token 不能访问 `/api/chat/**`。
- 无 token 可以调用 `POST /api/auth/register` 创建普通用户。

v0.8：

- USER 可以创建、查询、删除聊天会话。
- USER 只能访问自己的聊天会话和消息。
- USER 可以在会话中查询消息。
- USER 可以在会话中创建聊天消息。

v0.9：

- 前端聊天页面可以创建会话。
- 前端聊天页面可以切换和删除会话。
- 前端聊天页面可以查看消息。
- 前端聊天页面可以发送消息并看到占位 assistant 回复。
- 前端登录页面可以切换到注册表单并完成普通用户注册。
- 后端测试通过。
- 前端构建通过。

## 9. 后续版本边界

- v1.0：文件上传、Apache Tika 解析和文件状态。
- v1.1：文本切分、embedding 和 pgvector 入库。
- v1.2：AgentScope Java 2.0 向量检索、LLM 回答、引用来源保存。
- v1.3：重新索引、用户管理、管理员管理、系统状态、测试和启动说明。
