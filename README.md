# Rag-AiChat

更新时间：2026-10-28

Rag-AiChat 是一个基于 RAG（检索增强生成）的 AI 问答系统。后端使用 Spring Boot，前端使用 Vue 3，包含用户聊天页面和管理后台。

## 主要功能

### 用户端

- 登录、注册。
- 会话管理：新建、查看、删除（软删除）会话。
- 消息：查看历史消息，发送问题。
- 流式问答：提问时自动使用全部已启用的知识库，检索相关片段生成回答，并以流式方式返回。

### 管理后台

- 知识库：创建、编辑、删除、查看知识库。
- 文件：上传文件，使用 Apache Tika 解析，查看解析状态，删除文件，重新索引。
- 用户：查看用户列表，修改用户信息。
- 系统状态：查看数据库、pgvector、Redis、上传目录、Embedding、LLM、Tika 的运行状态。

## 技术栈

- 后端：Spring Boot、Spring Security、JWT
- 数据访问：MyBatis + MyBatis-Plus
- 文件解析：Apache Tika
- 数据库：PostgreSQL + pgvector
- 缓存：Redis
- 大模型：OpenAI 兼容接口（配置在 `application.yml`）
- 前端：Vue 3 + Vite + TypeScript

## 启动方式

```bash
# 后端
cd backend
mvn spring-boot:run
```

```bash
# 前端
cd frontend
npm install
npm run dev
```

- 后端默认端口：`8080`。
- 数据库、Redis、大模型、Embedding 等配置在 `backend/src/main/resources/application.yml`，均可用环境变量覆盖。
- 默认开发管理员账号：`admin` / `admin123`。

## 数据库

启动时自动执行 `backend/src/main/resources/db/schema.sql`，可重复运行，并保留默认管理员账号。聊天会话使用 `is_deleted` 字段做软删除。

## 关键文档

- 总体方案：`docs/rag-ai-chat-plan.md`
- v1.0 规格：`docs/specs/2026-10-26-rag-ai-chat-v1.0-spec.md`
