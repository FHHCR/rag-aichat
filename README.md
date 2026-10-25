# Rag-AiChat

更新时间：2026-10-24

Rag-AiChat 是一个基于 RAG 的 AI 问答系统，使用 SpringBoot 后端和 Vue 前端实现。项目分为用户聊天页面和管理后台两部分：普通用户只使用聊天页面，管理员负责上传文件、维护知识库和管理账号。

## 几天计划

- Day 1：`v0.1-v0.4`，已完成后端脚手架、前端脚手架、数据库基础设施和登录 JWT。
- Day 2：`v0.5-v0.6`，完成角色鉴权和管理员知识库管理。
- Day 3：`v0.7`，完成文件上传、Apache Tika 解析和解析状态管理。
- Day 4：`v0.8`，完成文本切分、embedding 调用和 pgvector 入库。
- Day 5：`v0.9`，基于 AgentScope Java 2.0 完成向量检索、LLM 回答、引用来源和用户聊天页。
- Day 6：`v1.0`，完成重新索引、用户管理、管理员管理、系统状态、测试和启动说明。

## 版本记录

- [x] v0.1：初始化 SpringBoot 后端项目、基础配置和统一响应结构。
- [x] v0.2：初始化 Vue 前端项目、路由、布局、Axios 封装和基础页面骨架。
- [x] v0.3：接入 PostgreSQL、Redis 和 pgvector，完成基础表结构。
- [x] v0.4：完成用户实体、登录接口、JWT 签发和登录页面。
- [ ] v0.5：完成 USER、ADMIN 角色鉴权和管理后台访问控制。
- [ ] v0.6：完成知识库实体、管理员知识库 CRUD 和前端知识库页面。
- [ ] v0.7：完成文件上传、文件元数据、Apache Tika 解析和解析状态展示。
- [ ] v0.8：完成文本切分、embedding 调用和 PostgreSQL + pgvector 向量入库。
- [ ] v0.9：基于 AgentScope Java 2.0 完成向量检索、LLM 回答、引用来源保存和用户聊天页面。
- [ ] v1.0：完成文件重新索引、用户管理、管理员管理、系统状态、测试和启动说明。

## 开发环境要求

当前设计需要：

- JDK 17+
- Maven 3.8+
- Node.js 18+
- PostgreSQL 14+
- pgvector 扩展
- Redis 6+

## 启动方式

启动方式如下：

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

实际端口、数据库连接、Redis 地址、大模型 API 和 embedding API 配置以后端 `application.yml` 为准。默认开发管理员账号为 `admin` / `admin123`。

## 技术栈

- 后端：SpringBoot
- 前端：Vue 3 + Vite + TypeScript
- 认证：Spring Security + JWT
- 文件解析：Apache Tika
- 业务数据库：PostgreSQL
- 向量数据库：PostgreSQL + pgvector
- Java RAG 框架：AgentScope Java 2.0
- 缓存：Redis

## 当前功能设计

### 用户端

- 登录
- 新建聊天会话
- 查看历史会话
- 基于知识库提问
- 查看模型回答
- 查看引用来源
- 删除自己的历史会话
- 选择知识库范围

普通用户不能上传文件、管理知识库、管理用户或进入管理后台。

### 管理后台

- 管理知识库
- 上传文件
- 查看文件解析状态
- 删除文件
- 重新索引文件
- 管理用户
- 管理管理员
- 查看基础系统状态

## RAG 流程

### 文件入库

1. 管理员上传文件。
2. 后端保存原始文件。
3. 写入文件元数据。
4. 使用 Apache Tika 提取文本。
5. 将文本切分为片段。
6. 为片段生成 embedding。
7. 将片段、来源信息和向量写入 PostgreSQL + pgvector。
8. 更新文件状态为可检索。

### 用户提问

1. 用户发送问题。
2. 后端保存用户消息。
3. 将问题生成 embedding。
4. 使用 pgvector 检索相关片段。
5. 将问题和片段交给大模型。
6. 生成回答。
7. 保存回答和引用来源。
8. 返回给前端展示。

## 关键文档

- 总体方案：`docs/rag-ai-chat-plan.md`
- 多天计划：`docs/plans/2026-10-24-rag-ai-chat-days-plan.md`
- Day 1 规格说明：`docs/specs/2026-10-24-rag-ai-chat-spec.md`
- Day 1 实现计划：`docs/plans/2026-10-24-rag-ai-chat-implementation-plan.md`
