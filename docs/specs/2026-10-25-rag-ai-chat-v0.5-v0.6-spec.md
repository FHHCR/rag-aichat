# Rag-AiChat v0.5-v0.6 Specification

日期：2026-10-25

## 1. 范围

今天只完成 `v0.5-v0.6`：角色鉴权、管理后台访问控制和管理员知识库管理。

本文档不覆盖文件上传、Apache Tika 解析、文本切分、embedding、pgvector 检索、LLM 回答、AgentScope Java 2.0 RAG 编排、用户管理、管理员管理和系统状态页面。

## 2. 目标

### v0.5 角色鉴权

- 后端区分 `USER` 和 `ADMIN`。
- JWT 中保留用户角色。
- Spring Security 根据角色控制接口访问。
- 前端根据登录用户角色控制页面跳转和访问。

### v0.6 知识库管理

- 管理员可以维护知识库。
- 知识库支持创建、查询、更新、删除。
- 前端管理后台提供知识库管理页面。

## 3. 权限规则

### 公开接口

- `GET /api/health`
- `POST /api/auth/login`

### 认证接口

- `GET /api/auth/me`
- `/api/chat/**`

要求：必须携带有效 JWT。

允许角色：

- `USER`
- `ADMIN`

### 管理员接口

- `/api/admin/**`

要求：必须携带有效 JWT，且角色为 `ADMIN`。

## 4. API

所有响应继续使用现有统一结构：

```json
{
  "code": 0,
  "message": "ok",
  "data": {}
}
```

### 4.1 创建知识库

`POST /api/admin/knowledge-bases`

请求示例：

```json
{
  "name": "产品手册",
  "description": "产品介绍和常见问题"
}
```

返回示例：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "id": "uuid",
    "name": "产品手册",
    "description": "产品介绍和常见问题",
    "status": "ACTIVE",
    "createdAt": "2026-10-25T10:00:00",
    "updatedAt": "2026-10-25T10:00:00"
  }
}
```

### 4.2 查询知识库列表

`GET /api/admin/knowledge-bases`

返回知识库数组，按创建时间倒序排列。

### 4.3 查询知识库详情

`GET /api/admin/knowledge-bases/{id}`

知识库不存在时返回业务错误。

### 4.4 更新知识库

`PUT /api/admin/knowledge-bases/{id}`

请求示例：

```json
{
  "name": "产品手册 2026",
  "description": "2026 版产品介绍和常见问题"
}
```

只允许更新名称和描述。

### 4.5 删除知识库

`DELETE /api/admin/knowledge-bases/{id}`

本版本直接删除知识库记录。文件删除、级联处理和重新索引留到后续版本。

## 5. 校验规则

知识库名称：

- 必填。
- 去除首尾空白后不能为空。
- 最大长度 120。

知识库描述：

- 可选。
- 最大长度 500。

知识库状态：

- 创建时默认 `ACTIVE`。

## 6. 前端行为

登录跳转：

- ADMIN 登录成功进入 `/admin`。
- USER 登录成功进入 `/chat`。

路由守卫：

- 无 token 访问受保护页面进入 `/login`。
- USER 访问 `/admin` 跳转到 `/chat`。
- ADMIN 可以访问 `/admin` 和 `/chat`。

管理后台知识库页面：

- 展示知识库列表。
- 支持创建知识库。
- 支持编辑知识库名称和描述。
- 支持删除知识库。
- 操作失败时展示错误信息。
- 无权限或登录失效时回到登录页或展示认证失败提示。

## 7. 非功能要求

- 不返回密码或密码哈希。
- 管理员接口必须以后端权限为准，前端跳转只作为体验控制。
- 使用已有 `ApiResponse` 统一响应。
- 使用已有 JWT 机制，不新增独立 session。
- 不修改 v0.1-v0.4 已完成的数据库基础结构，除非发现阻塞性错误。

## 8. 验收标准

v0.5：

- ADMIN 可以访问 `/api/admin/**`。
- USER 不能访问 `/api/admin/**`。
- 无 token 不能访问 `/api/admin/**`。
- ADMIN 登录后进入 `/admin`。
- USER 登录后进入 `/chat`。
- USER 访问 `/admin` 被前端重定向到 `/chat`。

v0.6：

- ADMIN 可以创建知识库。
- ADMIN 可以查询知识库列表。
- ADMIN 可以查询知识库详情。
- ADMIN 可以更新知识库名称和描述。
- ADMIN 可以删除知识库。
- 前端管理后台可以完成知识库基础 CRUD。
- 后端测试通过。
- 前端构建通过。

## 9. 后续版本边界

- v0.7：文件上传、Apache Tika 解析和文件状态。
- v0.8：文本切分、embedding 和 pgvector 入库。
- v0.9：AgentScope Java 2.0 向量检索、LLM 回答和聊天页。
- v1.0：重新索引、用户管理、管理员管理、系统状态、测试和启动说明。
