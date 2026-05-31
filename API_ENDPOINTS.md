# SOC 合规后端 — 全量 HTTP 接口清单

本文档根据 `src/main/java` 下全部 `@RestController` **自动生成语义梳理**，与运行时配置一致。

---

## 1. 全局约定

| 项 | 说明 |
|----|------|
| **Base URL** | `http://{host}:{port}/api`（默认端口见 `application.yml` 中 `server.port`，一般为 `18081`） |
| **上下文路径** | `server.servlet.context-path=/api`，下表路径均为 **去掉 `/api` 后的 Controller 相对路径**，调用时需前缀 **`/api`** |
| **认证** | 除下文「匿名接口」外，均需在 Header 携带：`Authorization: Bearer <JWT>` |
| **统一响应** | `ApiResponse<T>`：`code`（0 成功）、`message`、`data`。成功时 `message` 常由国际化填充（如「成功」） |
| **请求体 JSON** | 字段多为 **camelCase**；不少 DTO 使用 `@JsonAlias` 兼容 **snake_case**（如 `product_id`），具体见各节 |

### 1.1 匿名接口（无需 JWT）

以下路径在 `SocConstants.Security.PERMIT_ALL_PATHS` 中配置为放行：

- `POST /api/auth/login`
- `POST /api/auth/register`
- `GET /api/invitation-code/validate`（Query：`code`）
- `GET /api/product/list`
- `GET /api/product/detail/**`
- `GET /api/payment/query/**`
- `POST /api/payment/notify`
- `GET /api/payment/mock/success`

---

## 2. 认证 `AuthController` — `/auth`

| 方法 | 路径 | 概要 | 传参 |
|------|------|------|------|
| POST | `/auth/login` | 登录，返回 JWT 与用户信息 | Body：`LoginRequest`：`account`（邮箱或手机号）、`password` |
| POST | `/auth/register` | 注册并登录 | Body：`RegisterRequest`：`email`、`phone`、`companyName`、`roleCode`（别名 `role`）、`password`（6–64）；可选 `displayName` 或 `firstName`/`lastName`；可选 `invitationCode`/`invitation_code` |
| GET | `/auth/me` | 当前登录用户（JWT）资料与同登录结构 | 无 |

---

## 3. 商品 `ProductController` — `/product`

| 方法 | 路径 | 概要 | 传参 |
|------|------|------|------|
| GET | `/product/list` | 在售商品列表 | 无 |
| GET | `/product/detail/{productId}` | 按套餐维度返回详情卡片列表（审计类型影响展示价格） | Path：`productId`；Query：`auditType` 或 `audit_type`（可选） |
| PUT | `/product/package/{packageId}/price` | 更新套餐 Type1/Type2 价格与公司管理员权限校验 | Path：`packageId`；Body：`ProductPriceUpdateRequest`：`type1Price`、`type2Price`、`defaultType`（均有 snake_case 别名） |
| GET | `/product/my` | 当前用户订阅列表 | 无 |

---

## 4. 支付 `PaymentController` — `/payment`

| 方法 | 路径 | 概要 | 传参 |
|------|------|------|------|
| POST | `/payment/submit` | 提交支付（后端为准计价，`amount` 可忽略） | Body：`PaymentSubmitRequest`：`orderNo`/`order_no`、`productId`、`packageId`、`auditType`、`amount`、`paymentMethod`、`returnUrl`、`notifyUrl`（多项支持别名） |
| GET | `/payment/query/{orderNo}` | 查询订单支付状态 | Path：`orderNo` |
| POST | `/payment/notify` | 支付异步通知（网关字段名兼容多种 key） | Body：任意 JSON `Map`，从中解析订单号、状态、第三方流水号 |
| GET | `/payment/mock/success` | 模拟支付成功跳转提示 URL | Query：`orderNo` 或 `order_no`；`returnUrl` 或 `return_url`（可选） |

---

## 5. 订单 `OrderController` — `/order`

| 方法 | 路径 | 概要 | 传参 |
|------|------|------|------|
| GET | `/order/my` | 我的订单列表 | 无 |
| GET | `/order/detail/{orderNo}` | 订单详情 | Path：`orderNo` |
| DELETE | `/order/cancel/{orderNo}` | 取消订单 | Path：`orderNo` |
| PUT | `/order/cancel/{orderNo}` | 取消订单（兼容旧前端） | Path：`orderNo` |

---

## 6. 资料（新版）`ProfileController` — `/profile`

| 方法 | 路径 | 概要 | 传参 |
|------|------|------|------|
| GET | `/profile/me` | 当前用户资料（含公司摘要） | 无 |
| PUT | `/profile/me` | 更新个人资料 | Body：`ProfileUpdateRequest`：`displayName`、`email`、`phone`、`avatarUrl`、`jobTitle` |
| GET | `/profile/company` | 当前用户所属公司资料 | 无 |
| PUT | `/profile/company` | 更新公司资料 | Body：`CompanyProfileUpdateRequest`：`companyName`、`companyCode`、`industry`、`website`、`contactName`、`contactPhone`、`address` |

---

## 7. 邀请码 `InvitationCodeController` — `/invitation-code`

| 方法 | 路径 | 概要 | 传参 |
|------|------|------|------|
| POST | `/invitation-code/project/create` | 创建项目邀请码 | Body：`InvitationCreateRequest`：`projectId`、`memberRole`、`maxUses`、`expiresAt`、`remark` |
| GET | `/invitation-code/list` | 邀请码列表 | Query：`projectId` 或 `project_id`；`status`（可选） |
| GET | `/invitation-code/validate` | 校验邀请码是否可用（匿名） | Query：`code`（必填） |
| POST | `/invitation-code/revoke/{invitationId}` | 撤销邀请码 | Path：`invitationId` |

---

## 8. 项目 `ProjectController` — `/project`

| 方法 | 路径 | 概要 | 传参 |
|------|------|------|------|
| GET | `/project/list` | 本公司项目列表 | Query：`keyword`、`status`（可选） |
| GET | `/project/{projectId}` | 项目详情（含成员） | Path：`projectId` |
| GET | `/project/detail/{project_id}` | 同上（legacy 路径变量名） | Path：`project_id` |
| POST | `/project` | 创建项目 | Body：`ProjectCreateRequest`：`projectName`、`complianceType`、`auditType`、`status`、`startDate`、`endDate`（多项支持别名） |
| POST | `/project/create` | 同上（legacy） | Body：同上 |
| PUT | `/project/{projectId}` | 更新项目 | Path：`projectId`；Body：`ProjectUpdateRequest`（字段同类创建） |
| DELETE | `/project/{projectId}` | 删除项目 | Path：`projectId` |
| PUT | `/project/{projectId}/members` | 批量保存成员 | Path：`projectId`；Body：`ProjectMemberSaveRequest`：`members[]`：`userId`、`memberRole`、`displayName`、`email` |

---

## 9. 合规请求 `RequestController` — `/request`

| 方法 | 路径 | 概要 | 传参 |
|------|------|------|------|
| GET | `/request/list` | 请求列表 | Query：`projectId`/`project_id`；`documentStatus`/`status`；`ccCriteria`/`type` |
| GET | `/request/{requestId}` | 详情（含附件、版本） | Path：`requestId` |
| GET | `/request/detail/{request_id}` | 同上 legacy | Path：`request_id` |
| POST | `/request` | 创建请求 | Body：`RequestCreateRequest`：`projectId`、`ccCriteria`、`title` 及描述/状态等（多种别名） |
| POST | `/request/create` | 同上 legacy | Body：同上 |
| PUT | `/request/{requestId}` | 更新请求 | Path + Body：`RequestUpdateRequest`（含 `changeSummary` 等） |
| PUT | `/request/update/{request_id}` | legacy | Path + Body：同上 |
| POST | `/request/{requestId}/versions` | 保存版本快照 | Path + Body：`RequestVersionCreateRequest`：`changeSummary` |
| POST | `/request/{request_id}/save-version` | legacy | 同上 |
| PUT | `/request/save/{request_id}` | 等价更新请求 body | Path + Body：`RequestUpdateRequest` |
| POST | `/request/{requestId}/attachments` | 上传附件 | Path：`requestId`；`multipart/form-data` 字段 **`file`** |
| GET | `/request/{request_id}/documents` | 附件列表（来自详情） | Path：`request_id` |
| POST | `/request/{request_id}/document/upload` | legacy 上传 | Path + `file` |
| DELETE | `/request/{requestId}/attachments/{attachmentId}` | 删除附件 | Path |
| DELETE | `/request/document/{document_id}` | 按附件 id 删除（legacy） | Path：`document_id` |
| DELETE | `/request/{requestId}` | 删除请求 | Path：`requestId` |

---

## 10. RCM `RcmController` — `/rcm`

列表类接口均支持 **`RcmQueryRequest` 绑定**：`projectId`、`status`、`category`、`riskRating`、`stage`、`sourceRequestId`，并额外兼容 Query：`project_id`、`module`（映射 category）、`risk_rating`、`source_request_id`。`/manual/list`、`/final/list`、`/ai-generated/list` 会强制对应 `stage`。

| 方法 | 路径 | 概要 | 传参 |
|------|------|------|------|
| GET | `/rcm/list` | RCM 列表（可按 stage 筛选） | Query：见上 |
| GET | `/rcm/manual/list` | 手工阶段列表 | Query：同上 |
| GET | `/rcm/final/list` | 终稿阶段列表 | Query：同上 |
| GET | `/rcm/ai-generated/list` | AI 生成阶段列表 | Query：同上 |
| GET | `/rcm/{rcmId}` | 详情（含版本列表） | Path |
| GET | `/rcm/{rcm_id}/versions` | 仅版本列表（legacy） | Path |
| GET | `/rcm/ai/status` | AI/Ollama 状态探测 | 无 |
| POST | `/rcm` | 新建 RCM | Body：`RcmCreateRequest`（`projectId`、`controlCode`、`controlName` 必填；大量可选字段含别名） |
| POST | `/rcm/create` | legacy | Body：同上 |
| PUT | `/rcm/{rcmId}` | 更新 | Path + Body：`RcmUpdateRequest` |
| PUT | `/rcm/update/{rcm_id}` | legacy | 同上 |
| PUT | `/rcm/data-edit/{rcm_id}` | legacy 更新 | 同上 |
| POST | `/rcm/{rcmId}/versions` | 保存版本 | Path + Body：`RcmVersionCreateRequest`：`changeSummary` |
| POST | `/rcm/{rcm_id}/save-version` | legacy | 同上 |
| POST | `/rcm/upload` | Excel 导入 | `multipart`：`file`；Query：`projectId`/`project_id` |
| GET | `/rcm/export` | Excel 导出（二进制流） | Query：`projectId`/`project_id` |
| POST | `/rcm/ai/generate` | 按项目批量 AI 生成草稿 | Body：`RcmAiGenerateRequest`：`projectId`、`companyDescription`、`systemDescription`、`complianceFramework`、`sourceRequestId` |
| POST | `/rcm/{rcmId}/fill-by-ai` | 单行 AI 补全 | Path |
| POST | `/rcm/{rcmId}/upload-to-final` | 晋级 FINAL | Path |
| POST | `/rcm/{rcmId}/upload-to-manual` | 晋级 MANUAL | Path |
| POST | `/rcm/{rcm_id}/upload-to-mannual` | legacy 拼写 | Path |
| DELETE | `/rcm/{rcmId}` | 删除 | Path |

---

## 11. 控制测试 `ControlTestController` — `/control-testing`

| 方法 | 路径 | 概要 | 传参 |
|------|------|------|------|
| GET | `/control-testing/list` | 列表 | Query：`ControlTestQueryRequest`：`projectId`、`resultStatus`、`riskLevel` |
| GET | `/control-testing/{testId}` | 详情（含版本） | Path |
| POST | `/control-testing` | 创建 | Body：`ControlTestCreateRequest`：`projectId`、`title` 及其它可选字段 |
| PUT | `/control-testing/{testId}` | 更新 | Path + Body：`ControlTestUpdateRequest` |
| POST | `/control-testing/{testId}/versions` | 保存版本 | Path + Body：`ControlTestVersionCreateRequest`：`changeSummary` |
| DELETE | `/control-testing/{testId}` | 删除 | Path |

---

## 12. 差距分析 `GapAnalysisController` — `/gap-analysis`

| 方法 | 路径 | 概要 | 传参 |
|------|------|------|------|
| GET | `/gap-analysis/list` | 差距记录列表 | Query：`GapAnalysisQueryRequest`：`projectId`、`gapLevel`、`status` |
| POST | `/gap-analysis/regenerate` | 按项目重新生成差距分析 | Query：`projectId`（必填） |

---

## 13. 分析与报告 `AnalysisController` — `/analysis`

| 方法 | 路径 | 概要 | 传参 |
|------|------|------|------|
| GET | `/analysis/pass-rate/{projectId}` | 通过率摘要 | Path |
| GET | `/analysis/trend/{projectId}` | 趋势数据 | Path |
| GET | `/analysis/trend` | legacy：`project_id` | Query：`project_id` |
| GET | `/analysis/pass-rate/detail/{project_id}` | legacy 通过率 | Path |
| POST | `/analysis/generate-report` | 创建报告异步任务 | Body：`GenerateReportRequest`：`projectId`、`reportType`、`format`、`includeSections`、`language` |
| GET | `/analysis/report-status/{taskId}` | 任务状态 | Path |
| GET | `/analysis/download-report/{taskId}` | 下载报告文件（写响应流，非 JSON 信封） | Path |

---

## 14. 操作日志 `OperationLogController` — `/operation-log`

| 方法 | 路径 | 概要 | 传参 |
|------|------|------|------|
| GET | `/operation-log/list` | 日志列表 | Query：`OperationLogQueryRequest`：`projectId`、`moduleName`、`actionType` |
| GET | `/operation-log/statistics` | 按项目统计 | Query：`projectId`（必填） |

---

## 15. 兼容旧版路径

### 15.1 `LegacyUserController` — `/user`

| 方法 | 路径 | 概要 | 传参 |
|------|------|------|------|
| GET | `/user/info` | 等同 `GET /profile/me` | 无 |
| PUT | `/user/info` | 等同 `PUT /profile/me` | Body：`ProfileUpdateRequest` |
| GET | `/user/list` | 本公司用户简要列表 | Query：`keyword`（可选） |
| GET | `/user/roles` | 角色下拉（返回 `roleCode`/`roleName`） | 无 |

### 15.2 `LegacyAdminController` — `/admin`

| 方法 | 路径 | 概要 | 传参 |
|------|------|------|------|
| GET | `/admin/company` | 等同 `GET /profile/company` | 无 |

---

## 16. 统计说明

- **接口总数（按 Method + 路径区分）**：约 **95** 条（含 legacy 重复语义路径）。
- **响应字段颗粒度**：本文档仅列 Controller 声明的 **入参**；出参类型多为 `ApiResponse` 包装的 Entity/DTO，详见各类 Java 源码或 OpenAPI（若后续生成）。
- **维护**：新增 `@RestController` 或调整 `@*Mapping` 后请同步更新本文档。

---

*文档生成目录：`API_ENDPOINTS.md`（与 `API_Doc_v4.md` 并存，前者侧重「代码真实路由」，后者侧重产品原型契约）。*
