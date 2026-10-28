# Rag-AiChat v1.0 Specification

更新时间：2026-10-28

## 1. 版本目标

v1.0 完成可运行的 RAG 问答闭环和发布前管理能力：

- 文件上传和 Apache Tika 解析。
- 文本切分和 embedding 入库。
- PostgreSQL + pgvector 向量检索。
- LLM 回答生成和引用来源保存。
- 文件删除和重新索引。
- 用户管理和管理员管理。
- 系统状态页面。
- README 启动说明和环境变量说明。

## 2. 后端 API

### 2.1 文件管理

`POST /api/admin/knowledge-bases/{knowledgeBaseId}/files`

- multipart 字段：`file`
- 保存原始文件到上传目录。
- 写入 `document_files`。
- 使用 Apache Tika 提取文本。
- 文本切分、调用 embedding、写入 `document_chunks`。
- 成功状态为 `INDEXED`，解析失败为 `PARSED_FAILED` 或 `INDEX_FAILED`。

`GET /api/admin/knowledge-bases/{knowledgeBaseId}/files`

返回知识库下文件列表。

`GET /api/admin/knowledge-bases/{knowledgeBaseId}/files/{fileId}`

返回单个文件详情。

`POST /api/admin/knowledge-bases/{knowledgeBaseId}/files/{fileId}/reindex`

删除旧 chunks 后重新解析、切分和 embedding。

`DELETE /api/admin/knowledge-bases/{knowledgeBaseId}/files/{fileId}`

删除文件元数据、chunks 和上传目录中的原始文件。

### 2.2 RAG 问答

`POST /api/chat/sessions/{sessionId}/ask`

请求：

```json
{
  "content": "问题内容",
  "knowledgeBaseIds": ["uuid"]
}
```

处理流程：

1. 校验会话归属。
2. 保存 user 消息。
3. 调用 embedding API 生成问题向量。
4. 按知识库范围和 top-k 查询 pgvector。
5. 调用 LLM API 生成回答。
6. 保存 assistant 消息和引用来源。
7. 返回 assistant 消息。

若 embedding 或 LLM 未配置，返回明确业务错误。

### 2.3 用户管理

`GET /api/admin/users`

`GET /api/admin/users/{userId}`

`PATCH /api/admin/users/{userId}`

请求字段：`displayName`、`role`、`status`、`password`。

规则：

- 禁止禁用最后一个 ACTIVE ADMIN。
- 禁止降级最后一个 ACTIVE ADMIN。
- 修改密码为空时保持不变。
- 用户只返回安全字段，不返回密码哈希。

### 2.4 系统状态

`GET /api/admin/system/status`

返回：

- 应用版本。
- 数据库状态。
- Redis 状态。
- 上传目录可写状态。
- embedding 配置状态。
- LLM 配置状态。
- Tika 可用性。

## 3. 配置

后端新增配置：

- `APP_EMBEDDING_BASE_URL`
- `APP_EMBEDDING_API_KEY`
- `APP_EMBEDDING_MODEL`
- `APP_LLM_BASE_URL`
- `APP_LLM_API_KEY`
- `APP_LLM_MODEL`
- `APP_RAG_CHUNK_SIZE`
- `APP_RAG_CHUNK_OVERLAP`
- `APP_RAG_TOP_K`
- `APP_RAG_TIMEOUT_SECONDS`

接口按 OpenAI-compatible 协议调用：

- Embedding：`POST /embeddings`
- Chat：`POST /chat/completions`

## 4. 数据状态

文件状态：

- `UPLOADED`
- `PARSING`
- `PARSED`
- `INDEXING`
- `INDEXED`
- `PARSED_FAILED`
- `INDEX_FAILED`

用户角色和状态：

- `USER`
- `ADMIN`
- `ACTIVE`
- `DISABLED`

## 5. 前端页面

### 管理后台

管理后台提供以下分区：

- 知识库
- 文件索引
- 用户管理
- 系统状态

文件索引区支持选择知识库、上传文件、查看状态、删除和重新索引。

### 聊天页

聊天页支持：

- 新建、选择、删除会话。
- 选择知识库范围。
- 发送问题。
- 展示 assistant 回答。

## 6. 验收标准

- 后端可构建。
- 前端可构建。
- 核心接口有单元或控制器测试。
- README 写明 v1.0 环境变量和启动方式。

