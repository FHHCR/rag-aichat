# Rag-AiChat Days Plan

更新时间：2026-10-24

## 目标

用 6 天完成 Rag-AiChat 第一版：SpringBoot 后端、Vue 管理后台/聊天页、PostgreSQL + pgvector、Redis、Apache Tika、AgentScope Java 2.0 RAG 主链路，以及 v1.0 交付说明。

## 总体拆分

- Day 1：`v0.1-v0.4`，项目骨架、登录、数据库基础设施。
- Day 2：`v0.5-v0.6`，角色鉴权、知识库管理。
- Day 3：`v0.7`，文件上传和 Apache Tika 解析。
- Day 4：`v0.8`，文本切分和 pgvector 入库。
- Day 5：`v0.9`，AgentScope Java 2.0 RAG 问答主链路。
- Day 6：`v1.0`，管理完善、测试、启动文档和发布准备。

---

## Day 1: `v0.1-v0.4`

Status: completed on 2026-10-24.

目标：跑通前后端骨架和登录闭环。

交付物：

- SpringBoot 后端项目启动。
- Vue 前端项目启动。
- `/api/health` 可用。
- PostgreSQL、Redis、pgvector 基础配置完成。
- 基础表结构创建完成。
- 开发默认管理员账号创建完成。
- 登录接口签发 JWT。
- `/api/auth/me` 返回当前用户。
- 前端登录页可登录并显示当前用户。

关键文件：

- `backend/pom.xml`
- `backend/src/main/resources/application.yml`
- `backend/src/main/resources/db/migration/V1__init_schema.sql`
- `backend/src/main/resources/db/migration/V2__init_default_admin.sql`
- `backend/src/main/java/com/rag/aichat/`
- `frontend/src/`

验收：

- 后端可启动。
- 前端可启动。
- 默认管理员可登录。
- 不返回密码或密码哈希。

## Day 2: `v0.5-v0.6`

目标：完成管理后台入口和知识库管理基础能力。

交付物：

- USER/ADMIN 角色鉴权完成。
- ADMIN 可访问 `/api/admin/**`。
- USER 不能访问管理接口。
- 前端后台路由受保护。
- 知识库创建、查询、更新、删除接口完成。
- 管理后台知识库页面完成。

验收：

- ADMIN 登录后可进入后台管理页。
- USER 登录只能访问聊天页。
- 管理员可以维护知识库列表。

## Day 3: `v0.7`

目标：完成文件上传和 Apache Tika 解析链路。

交付物：

- 文件上传接口完成。
- 原始文件保存到本地目录。
- 文件元数据写入 `document_files`。
- 使用 Apache Tika 提取文本。
- 解析状态包括 `UPLOADED`、`PARSING`、`PARSED`、`FAILED`。
- 管理后台可上传文件和查看解析状态。

验收：

- ADMIN 可以上传支持的文件。
- 文件解析成功或失败有明确状态。
- 解析失败原因可查看。

## Day 4: `v0.8`

目标：完成文本切分和向量入库。

交付物：

- 已解析文本切分为 chunk。
- 为 chunk 调用 embedding API。
- 向量写入 `document_chunks.embedding`。
- pgvector 索引可用。
- 文件状态更新为可检索。

验收：

- 上传并解析后的文件可以完成向量化。
- `document_chunks` 有内容、来源文件、embedding 模型和向量。
- 向量化失败可记录错误状态。

## Day 5: `v0.9`

目标：基于 AgentScope Java 2.0 完成 RAG 问答主链路。

交付物：

- 用户聊天会话和消息接口完成。
- 问题 embedding 完成。
- pgvector 检索相关片段完成。
- AgentScope Java 2.0 接入检索/生成链路。
- LLM 回答生成完成。
- 引用来源保存并返回前端。
- 用户聊天页完成提问、回答、引用展示。

验收：

- USER 可以基于知识库提问。
- 回答附带引用来源。
- 消息历史可保存和展示。
- 未选知识库或知识库为空时有明确提示。

## Day 6: `v1.0`

目标：完成发布前管理能力和文档。

交付物：

- 文件重新索引功能完成。
- 用户管理完成。
- 管理员管理完成。
- 系统状态页面完成。
- 后端核心测试补充完成。
- 前端关键页面冒烟检查完成。
- README 启动说明、环境变量和常见问题更新完成。

验收：

- 管理员可以重新索引失败文件。
- 管理员可以禁用用户或调整角色。
- 系统状态页能展示服务、数据库、Redis、向量化和 LLM 配置状态。
- README 可以让新环境按步骤启动项目。

## 每日收尾要求

- 每天只提交当天版本相关内容。
- 每天完成后更新 README 版本记录 checkbox。
- 每天完成后运行对应检查：后端 `mvn test`，前端 `npm run build`。
- 出现阻塞时先记录到当天计划末尾，不扩大到无关版本。
