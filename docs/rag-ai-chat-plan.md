# Rag-AiChat 项目方案文档

更新时间：2026-10-24

## 1. 项目目标

Rag-AiChat 是一个基于 RAG 的 AI 问答系统，采用 SpringBoot + Vue 前后端分离架构。

项目包含两个主要使用入口：

- 用户页面：普通用户只使用聊天页面，基于已上传知识库进行问答。
- 管理后台：管理员负责上传文件、维护知识库、管理用户和管理员。

后端负责认证鉴权、文件上传与解析、文本切分、向量入库、向量检索、大模型调用和后台管理接口。前端负责用户聊天体验和管理员后台操作体验。

## 2. 已确定后端技术方向

本阶段先明确后端 RAG 核心能力边界：

- 后端框架：SpringBoot。
- 文件解析：统一使用 Apache Tika。
- 向量数据库：PostgreSQL + pgvector。
- 业务关系数据：PostgreSQL。
- 缓存与任务状态：Redis。
- 认证鉴权：Spring Security + JWT。

采用 Tika 后，后端不再按 PDF、Word、Excel 分别维护解析器实现，而是统一封装 Tika 解析服务，将文件转换为纯文本，再进入后续文本切分和向量化流程。

采用 PostgreSQL + pgvector 后，业务表、文档元数据、文本片段和向量数据可以统一存放在 PostgreSQL 中，降低部署复杂度，也方便做文件删除、片段重建和知识库隔离。

## 3. 角色与使用边界

### 3.1 普通用户

普通用户登录后只进入聊天页面。

用户可以：

- 新建聊天会话。
- 查看自己的历史会话。
- 发送自然语言问题。
- 查看模型回答。
- 查看回答引用来源。
- 删除自己的历史会话。
- 选择使用全部知识库或指定知识库。

普通用户不能：

- 上传文件。
- 管理知识库。
- 删除或重建索引。
- 管理用户和管理员。

### 3.2 管理员

管理员登录后进入管理后台。

管理员可以：

- 管理知识库。
- 上传文件。
- 查看文件解析状态。
- 删除文件。
- 重新解析或重新索引文件。
- 管理普通用户。
- 管理管理员账号。
- 查看基础运行状态和错误信息。

管理员是否可以进入用户聊天页进行测试，仍作为待确认项处理。

## 4. 核心功能

### 4.1 用户聊天页

用户聊天页包含：

- 登录。
- 新建会话。
- 历史会话列表。
- 多轮聊天。
- 基于知识库的 RAG 问答。
- 回答引用来源展示。
- 删除历史会话。
- 选择知识库范围。

用户侧重点应是轻量、清晰、稳定，不提供复杂文件管理功能。

### 4.2 管理员后台

管理后台包含：

- 登录。
- 知识库管理。
- 文件上传。
- 文件列表查询。
- 文件解析状态查询。
- 文件删除。
- 文件重新解析。
- 文件重新索引。
- 用户管理。
- 管理员管理。
- 系统状态展示。

管理后台重点支持文件生命周期管理，即从上传、解析、入库、检索可用，到删除和重建的完整闭环。

### 4.3 RAG 主流程

管理员上传文件后，后端按以下流程处理：

1. 接收文件并保存原始文件。
2. 记录文件元数据。
3. 使用 Tika 解析文件内容为纯文本。
4. 根据解析结果切分文本片段。
5. 为每个文本片段生成 embedding。
6. 将片段内容、来源信息、向量写入 PostgreSQL + pgvector。
7. 更新文件状态为可检索。

用户提问后，后端按以下流程处理：

1. 校验用户身份和会话权限。
2. 保存用户问题。
3. 将用户问题生成 embedding。
4. 基于知识库范围检索相关文本片段。
5. 将用户问题和检索片段交给大模型。
6. 生成回答。
7. 保存回答和引用来源。
8. 返回给前端展示。

## 5. 推荐项目结构

建议采用前后端分离结构：

```text
Rag-AiChat/
  backend/
    src/main/java/
      com/rag/aiChat/
        config/
        controller/
        service/
        repository/
        entity/
        dto/
        security/
        parser/
        rag/
        llm/
        embedding/
    src/main/resources/
      application.yml
  frontend/
    src/
      api/
      assets/
      components/
      layouts/
      router/
      stores/
      views/
        chat/
        admin/
      App.vue
      main.ts
  docs/
  docker-compose.yml
  README.md
```

后端包结构建议：

- `controller`：HTTP 接口。
- `service`：业务服务。
- `repository`：数据访问。
- `parser`：Tika 文件解析封装。
- `rag`：文本切分、检索、RAG 编排。
- `llm`：大模型调用适配。
- `embedding`：向量生成适配。
- `security`：Spring Security 和 JWT。

## 6. 页面设计草案

### 6.1 用户聊天页

布局建议：

- 左侧：历史会话列表、新建会话按钮。
- 中间：消息列表、输入框、发送按钮。
- 消息区域下方：引用来源。
- 顶部：当前用户、退出登录。

用户页面不展示文件上传、知识库维护、用户管理、管理员管理等入口。

### 6.2 管理员后台

布局建议：

- 左侧导航：知识库、文件、用户、管理员、系统状态。
- 顶部：当前管理员、退出登录。
- 文件管理页：上传按钮、文件列表、状态筛选、重新索引按钮、删除按钮。
- 知识库页：知识库列表、新建知识库、编辑知识库。

## 7. API 草案

### 7.1 认证接口

- `POST /api/auth/login`
- `POST /api/auth/logout`
- `GET /api/auth/me`

### 7.2 聊天接口

- `GET /api/chat/sessions`
- `POST /api/chat/sessions`
- `GET /api/chat/sessions/{sessionId}/messages`
- `DELETE /api/chat/sessions/{sessionId}`
- `POST /api/chat/send`
- `GET /api/chat/knowledge-bases`

`POST /api/chat/send` 请求示例：

```json
{
  "sessionId": "uuid",
  "question": "如何申请退款？",
  "knowledgeBaseIds": ["kb-id-1"],
  "topK": 5
}
```

返回示例：

```json
{
  "answer": "退款申请需要在订单完成后七天内提交...",
  "sources": [
    {
      "fileId": "file-id-1",
      "fileName": "退款政策.pdf",
      "chunkText": "退款申请需要在订单完成后七天内提交...",
      "score": 0.82
    }
  ]
}
```

### 7.3 管理接口

- `GET /api/admin/knowledge-bases`
- `POST /api/admin/knowledge-bases`
- `PUT /api/admin/knowledge-bases/{id}`
- `DELETE /api/admin/knowledge-bases/{id}`
- `GET /api/admin/files`
- `POST /api/admin/files/upload`
- `GET /api/admin/files/{id}/status`
- `POST /api/admin/files/{id}/reindex`
- `DELETE /api/admin/files/{id}`

### 7.4 用户和角色接口

- `GET /api/admin/users`
- `POST /api/admin/users`
- `PUT /api/admin/users/{id}`
- `DELETE /api/admin/users/{id}`
- `GET /api/admin/admins`
- `POST /api/admin/admins`
- `DELETE /api/admin/admins/{id}`

## 8. 数据模型草案

### 8.1 User

- id
- username
- passwordHash
- displayName
- role
- status
- createdAt
- updatedAt

### 8.2 KnowledgeBase

- id
- name
- description
- status
- createdById
- createdAt
- updatedAt

### 8.3 DocumentFile

- id
- knowledgeBaseId
- fileName
- originalPath
- mimeType
- fileSize
- parser
- status
- parseError
- chunkCount
- uploadedById
- createdAt
- updatedAt

`parser` 字段默认记录为 `TIKA`。

### 8.4 DocumentChunk

- id
- documentFileId
- knowledgeBaseId
- chunkIndex
- content
- embeddingProvider
- embeddingModel
- createdAt

向量字段使用 PostgreSQL + pgvector 存储，例如 `embedding vector(dim)`。具体维度由选定的 embedding 模型决定。

### 8.5 ChatSession

- id
- userId
- title
- knowledgeBaseScope
- createdAt
- updatedAt

### 8.6 ChatMessage

- id
- sessionId
- userId
- role
- content
- sourceJson
- createdAt

## 9. RAG 设计细节

### 9.1 文件上传

支持格式建议：

- `.pdf`
- `.doc`
- `.docx`
- `.ppt`
- `.pptx`
- `.xls`
- `.xlsx`
- `.md`
- `.txt`
- `.csv`

以上格式统一由 Tika 解析。后端实现时可以先开放常见格式，如 `.pdf`、`.docx`、`.txt`、`.md`、`.csv`，后续再根据 Tika 支持情况和实际效果扩展。

上传策略：

- 同步接收文件并落盘。
- 异步调用 Tika 解析。
- 异步进行文本切分、embedding 和向量写入。
- 前端通过轮询或 WebSocket 展示文件状态。
- 删除文件时同步删除对应文本片段和向量。
- 重新索引时先清理旧片段和旧向量，再重新解析写入。

### 9.2 Tika 解析策略

Tika 解析职责：

- 从上传文件中提取纯文本。
- 尽量保留标题、段落、表格和阅读顺序。
- 对无法解析或解析失败的文件记录错误状态。
- 将解析后的纯文本交给文本切分模块。

Tika 不负责：

- 语义切分。
- 向量生成。
- 大模型回答。
- 引用来源排序。

### 9.3 文本切分

默认策略：

- 按标题、段落和换行优先切分。
- 单片段长度建议 300 到 800 个字符。
- 相邻片段保留少量重叠。
- 保留文件名、页码、段落位置等来源信息。
- 切分结果写入 `DocumentChunk` 对应数据表。

### 9.4 向量存储

默认使用 PostgreSQL + pgvector。

设计要点：

- 文本片段内容保存在关系表中。
- 向量保存在 `vector` 字段中。
- 每个向量记录保留 `documentFileId`、`knowledgeBaseId`、`chunkIndex`、`embeddingModel` 等元数据。
- 查询时通过向量相似度和知识库范围过滤。
- 删除文件时删除对应向量记录。
- 重新索引时覆盖旧向量记录。

### 9.5 检索策略

默认策略：

- 使用 pgvector 向量相似度检索。
- 支持按 `knowledgeBaseIds` 过滤。
- 默认 `topK = 5`。
- 可后续扩展关键词检索做混合检索。
- 无结果时明确返回“未在知识库中找到相关内容”。

### 9.6 回答生成

提示词建议：

- 你是企业知识库问答助手。
- 只根据提供的资料回答问题。
- 如果资料不足，明确说明没有找到依据。
- 返回答案并列出引用来源。
- 不要编造文档中不存在的信息。

## 10. 权限设计

角色：

- `USER`
- `ADMIN`

权限矩阵：

| 功能 | USER | ADMIN |
| --- | --- | --- |
| 聊天 | 是 | 待确认 |
| 查看自己的历史 | 是 | 待确认 |
| 上传文件 | 否 | 是 |
| 管理知识库 | 否 | 是 |
| 管理用户 | 否 | 是 |
| 管理管理员 | 否 | 是 |

认证和权限设计：

- JWT 放在 HTTP Header。
- 后端通过 Spring Security 校验角色。
- 前端根据角色跳转默认页面。
- 用户角色不能访问 `/api/admin/**`。

## 11. 非功能需求

- 文件上传大小限制先默认 50MB。
- 文件大小、类型、解析状态需要可见。
- Tika 解析失败时返回明确错误信息。
- 模型调用失败时返回明确错误信息。
- 管理后台操作需要审计日志。
- 普通用户不能访问管理接口。
- 文件内容和向量数据删除时要保持一致。
- API 响应统一格式。
- 后端配置敏感信息不能写入前端。

## 12. 开发阶段建议

### 阶段一：基础脚手架

- 初始化 SpringBoot 后端。
- 初始化 Vue 前端。
- 接入 PostgreSQL、Redis。
- 初始化 pgvector。
- 完成登录、JWT、角色权限。
- 完成前端路由和页面布局。

### 阶段二：知识库和文件上传

- 管理员后台布局。
- 知识库 CRUD。
- 文件上传。
- 文件列表和状态查询。
- Tika 文件解析。
- 文件解析状态更新。

### 阶段三：RAG 能力

- 文本切分。
- Embedding。
- pgvector 向量存储。
- 向量相似度检索。
- LLM 回答。
- 引用来源展示。

### 阶段四：聊天体验完善

- 历史会话。
- 多轮上下文。
- 删除会话。
- 流式回答，待确认。
- 引用来源面板。
- 错误提示。

### 阶段五：管理增强

- 用户管理。
- 管理员管理。
- 文件重新索引。
- 操作日志。
- 基础统计。

## 13. 待确认问题

1. 是否已有指定大模型服务？
2. 是否已有指定 embedding 模型服务？
3. 是否需要支持扫描件、图片或 OCR 文档？
4. 后端持久层优先使用 MyBatis-Plus、Spring Data JPA，还是沿用团队习惯？
5. 前端 UI 组件库优先使用 Element Plus 还是 Ant Design Vue？
6. 聊天回答是否需要流式输出？
7. 管理员是否也可以进入用户聊天页测试问答？
8. 是否需要支持企业微信、钉钉或浏览器 Cookie 登录？
9. 是否需要多租户隔离，即不同企业或部门使用不同知识库？
10. 部署环境是本地 Docker、云服务器，还是已有公司内网环境？

