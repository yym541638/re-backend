# SOC 合规自动化软件 — PRD 页面模块与后台接口映射文档

> **依据文档**：`prd/SOC合规自动化软件-v1.0.0.1-PRD-20260206-George Yao(1).pdf`  
> **代码基准**：`src/main/java/com/compliancemind/soc/controller/**`（扫描日期：2026-05-24）  
> **说明**：2.5.4「项目单次信息询问页面」按需求排除在 PRD 统计之外；其相关接口在附录 C 中标注。本文档仅收录各业务的**唯一主路径**，重复功能的别名路径不列出。

---

## 目录

1. [全局约定](#1-全局约定)
2. [PRD 页面模块接口映射总览](#2-prd-页面模块接口映射总览)
3. [分模块详细映射](#3-分模块详细映射)
4. [附录 A：全量接口清单](#附录-a全量接口清单)
5. [附录 B：缺失接口汇总](#附录-b缺失接口汇总)
6. [附录 C：2.5.4 相关接口（参考）](#附录-c254-相关接口参考)

---

## 1. 全局约定

| 项 | 说明 |
|----|------|
| **Base URL** | `http://{host}:{port}/api`（默认端口 `18081`，见 `application.yml`） |
| **Context Path** | `/api` — 下文路径均需加此前缀，完整地址示例：`http://127.0.0.1:18081/api/auth/login` |
| **认证方式** | Header：`Authorization: Bearer <JWT>` |
| **统一响应** | `ApiResponse<T>`：`{ "code": 0, "message": "...", "data": {...} }`，`code=0` 表示成功 |
| **JSON 字段** | 默认 camelCase；多数 DTO 支持 `@JsonAlias` 兼容 snake_case 请求字段 |
| **软删除** | PRD 全局要求删除均为软删除，后端已实现 |
| **分页** | 当前列表接口均无分页，返回全量数据 |

### 1.1 匿名接口（无需 JWT）

| 方法 | 完整路径 |
|------|---------|
| POST | `/api/auth/login` |
| POST | `/api/auth/register` |
| GET | `/api/invitation-code/validate` |
| GET | `/api/product/list` |
| GET | `/api/product/detail/**` |
| GET | `/api/payment/query/**` |
| POST | `/api/payment/notify` |
| GET | `/api/payment/mock/success` |

### 1.2 实现状态图例

| 标记 | 含义 |
|------|------|
| ✅ | 已实现 |
| ⚠️ | 部分实现（功能有缺口） |
| ❌ | 未实现 |

---

## 2. PRD 页面模块接口映射总览

| PRD 章节 | 页面模块 | 所需接口数 | 已实现 | 缺失 | 主要 Controller |
|---------|---------|-----------|--------|------|----------------|
| 2.1.1 | 首页下拉导航 | 0 | — | — | 纯前端 |
| 2.1.2 | 登录 | 1 | 1 | 0 | `AuthController` |
| 2.1.3 | 注册 | 2 | 2 | 0 | `AuthController` |
| 2.2.1 | SOC2 选择产品 | 1 | 1 | 0 | `ProductController` |
| 2.2.2 | 购买产品（定价页） | 2 | 2 | 0 | `ProductController` |
| 2.3 | Checkout 支付页 | 5 | 4 | 1 | `PaymentController` / `OrderController` |
| 2.4 | Profile 个人/公司资料 | 4 | 4 | 0 | `ProfileController` |
| 2.5.1 | 项目信息编辑 | 5 | 5 | 0 | `ProjectController` |
| 2.5.2 | Request Master | 6 | 6 | 0 | `RequestController` / `InvitationCodeController` |
| 2.5.3 | 单项目询问管理 | 3 | 3 | 0 | `RequestController`（复用） |
| ~~2.5.4~~ | ~~单次询问详情~~ | — | — | — | 不纳入统计 |
| 2.5.5 | RCM Final | 8 | 7 | 1 | `RcmController` |
| 2.5.6 | RCM Manual | 10 | 10 | 0 | `RcmController` |
| 2.5.7 | AI Generate RCM | 6 | 5 | 1 | `RcmController` |
| 2.5.8 | Control Testing | 7 | 6 | 1 | `ControlTestController` |
| 2.5.9 | Gap Analysis | 1~2 | 2 | 0 | `GapAnalysisController` |
| 2.5.10 | Passing Scores | 2~5 | 5 | 0 | `AnalysisController` |
| 2.5.11 | Operation Log | 2 | 1 | 1 | `OperationLogController` |
| 2.5.12 | Project Settings | 7 | 7 | 0 | `ProjectController` / `LegacyUserController` / `InvitationCodeController` |
| 2.6/2.7 | 角色权限 / 菜单 | 0 | — | — | 鉴权中间件 + 前端菜单 |

**合计（不含 2.5.4）**：约 **58~62** 个独立业务接口；**已实现 ~55**，**明确缺失 4~5 组**。

---

## 3. 分模块详细映射

---

### 3.1 2.1 首页

#### 3.1.1 2.1.1 下拉框

| 项 | 内容 |
|----|------|
| PRD 交互 | 点击下拉选项跳转对应页面 |
| 后台接口 | **无需** — 纯前端路由 |

---

#### 3.1.2 2.1.2 登录

| # | 业务 | 状态 | 方法 | 路径 | 认证 |
|---|------|------|------|------|------|
| 1 | 用户登录 | ✅ | POST | `/api/auth/login` | 匿名 |

**请求体** `LoginRequest`（JSON）：

```json
{
  "account": "test@test.com",
  "password": "Test@123456"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| account | string | 是 | 邮箱或手机号 |
| password | string | 是 | 密码 |

**响应** `data`：`LoginResponse`（含 `token`、`expire_in`、`purchase_status`、`redirect_to`、`user_info`）

**Controller**：`AuthController#login`

---

#### 3.1.3 2.1.3 注册

| # | 业务 | 状态 | 方法 | 路径 | 认证 |
|---|------|------|------|------|------|
| 1 | 用户注册 | ✅ | POST | `/api/auth/register` | 匿名 |
| 2 | 登录态校验 | ✅ | GET | `/api/auth/me` | JWT |

**注册请求体** `RegisterRequest`（JSON）：

```json
{
  "displayName": "George Yao",
  "email": "test@test.com",
  "phone": "13800000000",
  "companyName": "Demo Company",
  "roleCode": "GENERAL_USER",
  "password": "Test@123456",
  "invitationCode": "可选"
}
```

| 字段 | 类型 | 必填 | 别名 | 说明 |
|------|------|------|------|------|
| displayName | string | 否 | — | 显示名（或用 firstName/lastName） |
| email | string | 是 | — | 邮箱 |
| phone | string | 是 | — | 手机号 |
| companyName | string | 是 | — | 公司名称 |
| roleCode | string | 是 | role | 角色代码 |
| password | string | 是 | — | 6~64 位 |
| invitationCode | string | 否 | invitation_code | 邀请码 |

**Controller**：`AuthController#register` / `AuthController#me`

---

### 3.2 2.2 SOC2 界面

#### 3.2.1 2.2.1 选择产品

| # | 业务 | 状态 | 方法 | 路径 | 认证 |
|---|------|------|------|------|------|
| 1 | 已购产品/模块列表 | ✅ | GET | `/api/product/my` | JWT |

**Query 参数**：无

**响应** `data`：`List<UserProductItem>`（product_id、package_name、audit_type、status 等）

**Controller**：`ProductController#myProducts`

---

#### 3.2.2 2.2.2 购买产品（定价页）

| # | 业务 | 状态 | 方法 | 路径 | 认证 |
|---|------|------|------|------|------|
| 1 | 在售产品列表 | ✅ | GET | `/api/product/list` | 匿名 |
| 2 | 套餐详情与动态计价 | ✅ | GET | `/api/product/detail/{productId}` | 匿名 |

**产品详情 Query**：

| 参数 | 类型 | 必填 | 别名 | 说明 |
|------|------|------|------|------|
| auditType | string | 否 | audit_type | `Type1` / `Type2`，影响展示价格 |

**Controller**：`ProductController#list` / `ProductController#detail`

**扩展（管理员改价，非 PRD 页面必需）**：

| 业务 | 状态 | 方法 | 路径 |
|------|------|------|------|
| 更新套餐价格 | ✅ | PUT | `/api/product/package/{packageId}/price` |

---

### 3.3 2.3 购买界面 Checkout

| # | 业务 | 状态 | 方法 | 路径 | 认证 |
|---|------|------|------|------|------|
| 1 | 提交支付（含创建订单） | ✅ | POST | `/api/payment/submit` | JWT |
| 2 | 查询支付状态 | ✅ | GET | `/api/payment/query/{orderNo}` | 匿名 |
| 3 | 支付异步通知 | ✅ | POST | `/api/payment/notify` | 匿名 |
| 4 | 模拟支付成功跳转 | ✅ | GET | `/api/payment/mock/success` | 匿名 |
| 5 | 发送短信验证码 | ❌ | — | — | PRD 要求，未实现 |
| 6 | 订单详情（支付完成后） | ✅ | GET | `/api/order/detail/{orderNo}` | JWT |

**提交支付请求体** `PaymentSubmitRequest`（JSON）：

```json
{
  "orderNo": "ORD123",
  "productId": 1,
  "packageId": 1,
  "auditType": "Type1",
  "amount": 5999,
  "paymentMethod": "PAYPAL",
  "returnUrl": "http://localhost:8080/order",
  "notifyUrl": "http://localhost:18081/api/payment/notify"
}
```

| 字段 | 类型 | 必填 | 别名 | 说明 |
|------|------|------|------|------|
| orderNo | string | 否 | order_no | 不传则后端生成 |
| productId | integer | 是 | product_id | 产品 ID |
| packageId | integer | 是 | package_id | 套餐 ID |
| auditType | string | 否 | audit_type | Type1/Type2 |
| amount | integer | 否 | — | **后端为准，可忽略** |
| paymentMethod | string | 否 | payment_method | PAYPAL / QRCODE 等 |
| returnUrl | string | 否 | return_url | 支付完成跳转 |
| notifyUrl | string | 否 | notify_url | 异步通知地址 |

**模拟支付成功 Query**：

| 参数 | 别名 |
|------|------|
| orderNo | order_no |
| returnUrl | return_url |

**Controller**：`PaymentController` / `OrderController`

---

### 3.4 2.4 个人信息 Profile

| # | 业务 | 状态 | 方法 | 路径 | 认证 | 权限 |
|---|------|------|------|------|------|------|
| 1 | 读取个人资料 | ✅ | GET | `/api/profile/me` | JWT | 全部用户 |
| 2 | 更新个人资料 | ✅ | PUT | `/api/profile/me` | JWT | 全部用户 |
| 3 | 读取公司资料 | ✅ | GET | `/api/profile/company` | JWT | 公司管理员 |
| 4 | 更新公司资料 | ✅ | PUT | `/api/profile/company` | JWT | 公司管理员 |

**更新个人资料请求体** `ProfileUpdateRequest`：

| 字段 | 类型 | 说明 |
|------|------|------|
| displayName | string | 显示名 |
| email | string | 邮箱 |
| phone | string | 手机 |
| avatarUrl | string | 头像 URL |
| jobTitle | string | 职位 |

**更新公司资料请求体** `CompanyProfileUpdateRequest`：

| 字段 | 类型 | 说明 |
|------|------|------|
| companyName | string | 公司名称 |
| companyCode | string | 公司编码 |
| industry | string | 行业 |
| website | string | 网站 |
| contactName | string | 联系人 |
| contactPhone | string | 联系电话 |
| address | string | 地址 |

**Controller**：`ProfileController`

---

### 3.5.1 2.5.1 项目信息编辑 Project Overview

| # | 业务 | 状态 | 方法 | 路径 | 认证 | 权限 |
|---|------|------|------|------|------|------|
| 1 | 项目列表 | ✅ | GET | `/api/project/list` | JWT | 本公司 |
| 2 | 项目详情 | ✅ | GET | `/api/project/{projectId}` | JWT | 项目读 |
| 3 | 新建项目 | ✅ | POST | `/api/project` | JWT | 公司管理员 |
| 4 | 编辑项目 | ✅ | PUT | `/api/project/{projectId}` | JWT | 公司管理员 |
| 5 | 删除项目 | ✅ | DELETE | `/api/project/{projectId}` | JWT | 公司管理员 |

**列表 Query**：

| 参数 | 类型 | 说明 |
|------|------|------|
| keyword | string | 关键字搜索 |
| status | string | 状态筛选 |

**创建项目请求体** `ProjectCreateRequest`：

| 字段 | 必填 | 别名 | 说明 |
|------|------|------|------|
| projectName | 是 | project_name | 项目名称 |
| complianceType | 是 | compliance_type, type | 合规类型 |
| auditType | 是 | audit_type | Type1/Type2 |
| status | 是 | — | 项目状态 |
| startDate | 否 | start_date | 开始日期 |
| endDate | 否 | end_date | 结束日期 |

**Controller**：`ProjectController`

---

### 3.5.2 2.5.2 项目信息询问管理 Request Master

| # | 业务 | 状态 | 方法 | 路径 | 认证 |
|---|------|------|------|------|------|
| 1 | 询问列表 | ✅ | GET | `/api/request/list` | JWT |
| 2 | 新建询问行 | ✅ | POST | `/api/request` | JWT |
| 3 | 编辑询问行 | ✅ | PUT | `/api/request/{requestId}` | JWT |
| 4 | 删除询问行 | ✅ | DELETE | `/api/request/{requestId}` | JWT |
| 5 | 发送邀请码 | ✅ | POST | `/api/invitation-code/project/create` | JWT |
| 6 | 邀请码列表 | ✅ | GET | `/api/invitation-code/list` | JWT |

**列表 Query**：

| 参数 | 别名 | 说明 |
|------|------|------|
| projectId | project_id | **必填**（项目 ID） |
| documentStatus | status | 文档状态 |
| ccCriteria | type | CC 标准筛选 |

**创建请求体** `RequestCreateRequest`：

| 字段 | 必填 | 别名 |
|------|------|------|
| projectId | 是 | project_id |
| ccCriteria | 是 | cc_criteria, type |
| title | 是 | title, name |
| requestDescription | 否 | request_description |
| pointsOfFocus | 否 | points_of_focus |
| documentStatus | 否 | document_status |
| documentOwner | 否 | document_owner |
| implementationDate | 否 | implementation_date |
| notes / requestor / comments | 否 | — |

**邀请码创建** `InvitationCreateRequest`：

| 字段 | 必填 | 说明 |
|------|------|------|
| projectId | 是 | 项目 ID |
| memberRole | 是 | 成员角色 |
| maxUses | 否 | 最大使用次数 |
| expiresAt | 否 | 过期时间 |
| remark | 否 | 备注 |

**Controller**：`RequestController` / `InvitationCodeController`

---

### 3.5.3 2.5.3 单项目信息询问管理

与 2.5.2 共用 `RequestController`，通过 `projectId` 筛选同一项目下的询问记录。

| # | 业务 | 状态 | 方法 | 路径 |
|---|------|------|------|------|
| 1 | 询问列表 | ✅ | GET | `/api/request/list?projectId={id}` |
| 2 | 编辑行 | ✅ | PUT | `/api/request/{requestId}` |
| 3 | 删除行 | ✅ | DELETE | `/api/request/{requestId}` |

Enter 进入 2.5.4 详情页 — 按需求不展开。

---

### 3.5.5 2.5.5 RCM Final

| # | 业务 | 状态 | 方法 | 路径 | 认证 |
|---|------|------|------|------|------|
| 1 | Final 阶段列表 | ✅ | GET | `/api/rcm/final/list` | JWT |
| 2 | RCM 详情 | ✅ | GET | `/api/rcm/{rcmId}` | JWT |
| 3 | 版本列表（Version 下拉） | ✅ | GET | `/api/rcm/{rcmId}/versions` | JWT |
| 4 | 编辑行 | ✅ | PUT | `/api/rcm/{rcmId}` | JWT |
| 5 | 上传 RCM（Excel） | ✅ | POST | `/api/rcm/upload` | JWT |
| 6 | 从 Manual 批量导入 | ⚠️ | — | — | 仅单行晋级，缺项目级批量 |
| 7 | 导出 Excel | ✅ | GET | `/api/rcm/export` | JWT |
| 8 | 删除行 | ✅ | DELETE | `/api/rcm/{rcmId}` | JWT |
| 9 | 保存版本 | ✅ | POST | `/api/rcm/{rcmId}/versions` | JWT |

**单行从 Manual 晋级到 Final（部分满足 PRD）**：

| 业务 | 状态 | 方法 | 路径 |
|------|------|------|------|
| 单行 Upload to Final | ✅ | POST | `/api/rcm/{rcmId}/upload-to-final` |

**列表 Query**（manual/final/ai-generated/list 通用）：

| 参数 | 别名 | 说明 |
|------|------|------|
| projectId | project_id | **必填** |
| status | — | 状态 |
| category | module | 模块/CC |
| riskRating | risk_rating | 风险等级 |
| sourceRequestId | source_request_id | 来源 Request |

**Excel 上传**：`multipart/form-data`，字段 `file`；Query `projectId`

**Excel 导出**：返回二进制流（非 JSON 信封），Content-Type: `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`

**Controller**：`RcmController`

---

### 3.5.6 2.5.6 RCM Manual

| # | 业务 | 状态 | 方法 | 路径 | 认证 |
|---|------|------|------|------|------|
| 1 | Manual 阶段列表 | ✅ | GET | `/api/rcm/manual/list` | JWT |
| 2 | RCM 详情 | ✅ | GET | `/api/rcm/{rcmId}` | JWT |
| 3 | 版本列表 | ✅ | GET | `/api/rcm/{rcmId}/versions` | JWT |
| 4 | 新建行（New） | ✅ | POST | `/api/rcm` | JWT |
| 5 | 编辑行（Edit） | ✅ | PUT | `/api/rcm/{rcmId}` | JWT |
| 6 | 删除行 | ✅ | DELETE | `/api/rcm/{rcmId}` | JWT |
| 7 | 单行 AI 填充（Fill by AI） | ✅ | POST | `/api/rcm/{rcmId}/fill-by-ai` | JWT |
| 8 | 上传至 Final | ✅ | POST | `/api/rcm/{rcmId}/upload-to-final` | JWT |
| 9 | 上传 RCM（Excel） | ✅ | POST | `/api/rcm/upload` | JWT |
| 10 | 保存版本 | ✅ | POST | `/api/rcm/{rcmId}/versions` | JWT |

**创建请求体** `RcmCreateRequest` 主要字段：projectId、controlCode、controlName、description、category、moduleName、stage 等。

**Controller**：`RcmController`

---

### 3.5.7 2.5.7 AI Generate RCM

| # | 业务 | 状态 | 方法 | 路径 | 认证 |
|---|------|------|------|------|------|
| 1 | AI 生成阶段列表 | ✅ | GET | `/api/rcm/ai-generated/list` | JWT |
| 2 | RCM 详情 | ✅ | GET | `/api/rcm/{rcmId}` | JWT |
| 3 | 版本列表 | ✅ | GET | `/api/rcm/{rcmId}/versions` | JWT |
| 4 | 触发 AI 批量生成 | ✅ | POST | `/api/rcm/ai/generate` | JWT |
| 5 | AI 服务状态 | ✅ | GET | `/api/rcm/ai/status` | JWT |
| 6 | 批量 Upload to Manual | ⚠️ | — | — | 仅单行晋级 |
| 7 | 单行 Upload to Manual | ✅ | POST | `/api/rcm/{rcmId}/upload-to-manual` | JWT |

**AI 生成请求体** `RcmAiGenerateRequest`：

| 字段 | 必填 | 说明 |
|------|------|------|
| projectId | 是 | 项目 ID |
| companyDescription | 是 | 公司描述 |
| systemDescription | 是 | 系统描述 |
| complianceFramework | 是 | 合规框架 |
| sourceRequestId | 否 | 来源 Request |

**Controller**：`RcmController`

---

### 3.5.8 2.5.8 Control Testing

| # | 业务 | 状态 | 方法 | 路径 | 认证 |
|---|------|------|------|------|------|
| 1 | 控制测试列表 | ✅ | GET | `/api/control-testing/list` | JWT |
| 2 | 控制测试详情 | ✅ | GET | `/api/control-testing/{testId}` | JWT |
| 3 | 编辑行 | ✅ | PUT | `/api/control-testing/{testId}` | JWT |
| 4 | 删除行 | ✅ | DELETE | `/api/control-testing/{testId}` | JWT |
| 5 | 保存版本 | ✅ | POST | `/api/control-testing/{testId}/versions` | JWT |
| 6 | 新建（自动生成后可编辑） | ✅ | POST | `/api/control-testing` | JWT |
| 7 | 上传文件（PDF/Word） | ❌ | — | — | PRD 要求，未实现 |
| 8 | 附件列表 | ❌ | — | — | 未实现 |
| 9 | 删除附件 | ❌ | — | — | 未实现 |

**列表 Query** `ControlTestQueryRequest`：

| 参数 | 说明 |
|------|------|
| projectId | **必填** |
| resultStatus | 结果状态 |
| riskLevel | 风险等级 |

**创建/更新主要字段**：projectId、title、description、riskLevel、controlProcedure、resultStatus 等。

**Controller**：`ControlTestController`

---

### 3.5.9 2.5.9 Gap Analysis

PRD 描述为自动生成、页面无交互；后端提供列表与手动重算。

| # | 业务 | 状态 | 方法 | 路径 | 认证 |
|---|------|------|------|------|------|
| 1 | 差距分析列表 | ✅ | GET | `/api/gap-analysis/list` | JWT |
| 2 | 重新生成（扩展） | ✅ | POST | `/api/gap-analysis/regenerate?projectId={id}` | JWT |

**列表 Query** `GapAnalysisQueryRequest`：projectId、gapLevel、status

**Controller**：`GapAnalysisController`

---

### 3.5.10 2.5.10 Passing Scores

PRD 描述为只读展示；后端额外提供报告导出能力。

| # | 业务 | 状态 | 方法 | 路径 | 认证 |
|---|------|------|------|------|------|
| 1 | 通过率（Passing Scores） | ✅ | GET | `/api/analysis/pass-rate/{projectId}` | JWT |
| 2 | 分数趋势 | ✅ | GET | `/api/analysis/trend/{projectId}` | JWT |
| 3 | 生成报告任务（扩展） | ✅ | POST | `/api/analysis/generate-report` | JWT |
| 4 | 报告任务状态（扩展） | ✅ | GET | `/api/analysis/report-status/{taskId}` | JWT |
| 5 | 下载报告（扩展） | ✅ | GET | `/api/analysis/download-report/{taskId}` | JWT |

**生成报告请求体** `GenerateReportRequest`：projectId、reportType、format、includeSections、language

**Controller**：`AnalysisController`

---

### 3.5.11 2.5.11 Operation Log

| # | 业务 | 状态 | 方法 | 路径 | 认证 |
|---|------|------|------|------|------|
| 1 | 操作日志列表 | ✅ | GET | `/api/operation-log/list` | JWT |
| 2 | 版本回退 | ❌ | — | — | PRD 要求，未实现 |
| 3 | 日志统计（扩展） | ✅ | GET | `/api/operation-log/statistics?projectId={id}` | JWT |

**列表 Query** `OperationLogQueryRequest`：

| 参数 | 说明 |
|------|------|
| projectId | **必填** |
| moduleName | 模块名（RCM / REQUEST 等） |
| actionType | 动作类型（CREATE / UPDATE / DELETE 等） |

**Controller**：`OperationLogController`

---

### 3.5.12 2.5.12 Project Settings

| # | 业务 | 状态 | 方法 | 路径 | 认证 | 权限 |
|---|------|------|------|------|------|------|
| 1 | 项目详情（含成员） | ✅ | GET | `/api/project/{projectId}` | JWT | 项目读 |
| 2 | 保存项目成员 | ✅ | PUT | `/api/project/{projectId}/members` | JWT | 公司管理员 |
| 3 | 本公司用户搜索 | ✅ | GET | `/api/user/list` | JWT | 公司管理员 |
| 4 | 角色下拉列表 | ✅ | GET | `/api/user/roles` | JWT | 公司管理员 |
| 5 | 创建邀请码 | ✅ | POST | `/api/invitation-code/project/create` | JWT | 公司管理员 |
| 6 | 邀请码列表 | ✅ | GET | `/api/invitation-code/list` | JWT | 公司管理员 |
| 7 | 撤销邀请码 | ✅ | POST | `/api/invitation-code/revoke/{invitationId}` | JWT | 公司管理员 |

**成员保存请求体** `ProjectMemberSaveRequest`：

```json
{
  "members": [
    {
      "userId": 1001,
      "memberRole": "MANAGER",
      "displayName": "George Yao",
      "email": "test@test.com"
    }
  ]
}
```

**用户列表 Query**：`keyword`（可选）

**邀请码校验（注册用，匿名）**：`GET /api/invitation-code/validate?code={code}`

**Controller**：`ProjectController` / `LegacyUserController` / `InvitationCodeController`

---

## 附录 A：全量接口清单

> 按 Controller 分组，每条业务仅保留一个主路径。完整 URL = `/api` + 路径。

### A.1 AuthController — `/auth`

| 状态 | 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|------|
| ✅ | POST | `/auth/login` | 登录 | 匿名 |
| ✅ | POST | `/auth/register` | 注册 | 匿名 |
| ✅ | GET | `/auth/me` | 当前用户 | JWT |

### A.2 ProductController — `/product`

| 状态 | 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|------|
| ✅ | GET | `/product/list` | 产品列表 | 匿名 |
| ✅ | GET | `/product/detail/{productId}` | 产品详情 | 匿名 |
| ✅ | PUT | `/product/package/{packageId}/price` | 套餐改价 | JWT |
| ✅ | GET | `/product/my` | 我的订阅 | JWT |

### A.3 PaymentController — `/payment`

| 状态 | 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|------|
| ✅ | POST | `/payment/submit` | 提交支付 | JWT |
| ✅ | GET | `/payment/query/{orderNo}` | 查询状态 | 匿名 |
| ✅ | POST | `/payment/notify` | 异步通知 | 匿名 |
| ✅ | GET | `/payment/mock/success` | 模拟成功 | 匿名 |
| ❌ | POST | `/payment/send-validation-code` | 短信验证码 | — |

### A.4 OrderController — `/order`

| 状态 | 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|------|
| ✅ | GET | `/order/my` | 我的订单 | JWT |
| ✅ | GET | `/order/detail/{orderNo}` | 订单详情 | JWT |
| ✅ | DELETE | `/order/cancel/{orderNo}` | 取消订单 | JWT |

### A.5 ProfileController — `/profile`

| 状态 | 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|------|
| ✅ | GET | `/profile/me` | 个人资料 | JWT |
| ✅ | PUT | `/profile/me` | 更新个人资料 | JWT |
| ✅ | GET | `/profile/company` | 公司资料 | JWT |
| ✅ | PUT | `/profile/company` | 更新公司资料 | JWT |

### A.6 InvitationCodeController — `/invitation-code`

| 状态 | 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|------|
| ✅ | POST | `/invitation-code/project/create` | 创建邀请码 | JWT |
| ✅ | GET | `/invitation-code/list` | 邀请码列表 | JWT |
| ✅ | GET | `/invitation-code/validate` | 校验邀请码 | 匿名 |
| ✅ | POST | `/invitation-code/revoke/{invitationId}` | 撤销邀请码 | JWT |

### A.7 ProjectController — `/project`

| 状态 | 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|------|
| ✅ | GET | `/project/list` | 项目列表 | JWT |
| ✅ | GET | `/project/{projectId}` | 项目详情 | JWT |
| ✅ | POST | `/project` | 创建项目 | JWT |
| ✅ | PUT | `/project/{projectId}` | 更新项目 | JWT |
| ✅ | DELETE | `/project/{projectId}` | 删除项目 | JWT |
| ✅ | PUT | `/project/{projectId}/members` | 保存成员 | JWT |

### A.8 RequestController — `/request`

| 状态 | 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|------|
| ✅ | GET | `/request/list` | 请求列表 | JWT |
| ✅ | GET | `/request/{requestId}` | 请求详情（含附件、版本） | JWT |
| ✅ | POST | `/request` | 创建请求 | JWT |
| ✅ | PUT | `/request/{requestId}` | 更新请求 | JWT |
| ✅ | POST | `/request/{requestId}/versions` | 保存版本 | JWT |
| ✅ | POST | `/request/{requestId}/attachments` | 上传附件 | JWT |
| ✅ | DELETE | `/request/{requestId}/attachments/{attachmentId}` | 删除附件 | JWT |
| ✅ | DELETE | `/request/{requestId}` | 删除请求 | JWT |
| ❌ | POST | `/request/{requestId}/versions/{versionId}/restore` | 版本回退 | — |

### A.9 RcmController — `/rcm`

| 状态 | 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|------|
| ✅ | GET | `/rcm/list` | RCM 列表 | JWT |
| ✅ | GET | `/rcm/manual/list` | Manual 列表 | JWT |
| ✅ | GET | `/rcm/final/list` | Final 列表 | JWT |
| ✅ | GET | `/rcm/ai-generated/list` | AI 列表 | JWT |
| ✅ | GET | `/rcm/{rcmId}` | RCM 详情 | JWT |
| ✅ | GET | `/rcm/{rcmId}/versions` | 版本列表 | JWT |
| ✅ | GET | `/rcm/ai/status` | AI 状态 | JWT |
| ✅ | POST | `/rcm` | 创建 RCM | JWT |
| ✅ | PUT | `/rcm/{rcmId}` | 更新 RCM | JWT |
| ✅ | POST | `/rcm/{rcmId}/versions` | 保存版本 | JWT |
| ✅ | POST | `/rcm/upload` | Excel 导入 | JWT |
| ✅ | GET | `/rcm/export` | Excel 导出 | JWT |
| ✅ | POST | `/rcm/ai/generate` | AI 批量生成 | JWT |
| ✅ | POST | `/rcm/{rcmId}/fill-by-ai` | 单行 AI 填充 | JWT |
| ✅ | POST | `/rcm/{rcmId}/upload-to-final` | 晋级 Final | JWT |
| ✅ | POST | `/rcm/{rcmId}/upload-to-manual` | 晋级 Manual | JWT |
| ✅ | DELETE | `/rcm/{rcmId}` | 删除 RCM | JWT |
| ❌ | POST | `/rcm/ai/upload-to-manual` | 项目级批量晋级 Manual | — |
| ❌ | POST | `/rcm/import-from-manual` | 项目级从 Manual 导入 Final | — |
| ❌ | POST | `/rcm/{rcmId}/versions/{versionId}/restore` | 版本回退 | — |

### A.10 ControlTestController — `/control-testing`

| 状态 | 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|------|
| ✅ | GET | `/control-testing/list` | 列表 | JWT |
| ✅ | GET | `/control-testing/{testId}` | 详情 | JWT |
| ✅ | POST | `/control-testing` | 创建 | JWT |
| ✅ | PUT | `/control-testing/{testId}` | 更新 | JWT |
| ✅ | POST | `/control-testing/{testId}/versions` | 保存版本 | JWT |
| ✅ | DELETE | `/control-testing/{testId}` | 删除 | JWT |
| ❌ | POST | `/control-testing/{testId}/attachments` | 上传附件 | — |
| ❌ | GET | `/control-testing/{testId}/documents` | 附件列表 | — |
| ❌ | DELETE | `/control-testing/{testId}/attachments/{attachmentId}` | 删除附件 | — |
| ❌ | POST | `/control-testing/{testId}/versions/{versionId}/restore` | 版本回退 | — |

### A.11 GapAnalysisController — `/gap-analysis`

| 状态 | 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|------|
| ✅ | GET | `/gap-analysis/list` | 差距列表 | JWT |
| ✅ | POST | `/gap-analysis/regenerate` | 重新生成 | JWT |

### A.12 AnalysisController — `/analysis`

| 状态 | 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|------|
| ✅ | GET | `/analysis/pass-rate/{projectId}` | 通过率 | JWT |
| ✅ | GET | `/analysis/trend/{projectId}` | 趋势 | JWT |
| ✅ | POST | `/analysis/generate-report` | 创建报告任务 | JWT |
| ✅ | GET | `/analysis/report-status/{taskId}` | 任务状态 | JWT |
| ✅ | GET | `/analysis/download-report/{taskId}` | 下载报告 | JWT |

### A.13 OperationLogController — `/operation-log`

| 状态 | 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|------|
| ✅ | GET | `/operation-log/list` | 日志列表 | JWT |
| ✅ | GET | `/operation-log/statistics` | 日志统计 | JWT |

### A.14 LegacyUserController — `/user`

| 状态 | 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|------|
| ✅ | GET | `/user/list` | 本公司用户列表 | JWT |
| ✅ | GET | `/user/roles` | 角色下拉列表 | JWT |

**接口统计**：已实现 **68 条**；明确缺失 **7 条**。

---

## 附录 B：缺失接口汇总

| 优先级 | 建议路径 | 方法 | 对应 PRD | 说明 |
|--------|---------|------|---------|------|
| P0 | `/api/payment/send-validation-code` | POST | 2.3 | Checkout 发送短信验证码 |
| P0 | `/api/control-testing/{testId}/attachments` | POST | 2.5.8 | 上传 PDF/Word 附件 |
| P0 | `/api/control-testing/{testId}/documents` | GET | 2.5.8 | 附件列表 |
| P0 | `/api/control-testing/{testId}/attachments/{attachmentId}` | DELETE | 2.5.8 | 删除附件 |
| P1 | `/api/request/{requestId}/versions/{versionId}/restore` | POST | 2.5.11 | Request 版本回退 |
| P1 | `/api/rcm/{rcmId}/versions/{versionId}/restore` | POST | 2.5.11 | RCM 版本回退 |
| P1 | `/api/control-testing/{testId}/versions/{versionId}/restore` | POST | 2.5.11 | Control Test 版本回退 |
| P1 | `/api/rcm/ai/upload-to-manual?projectId=` | POST | 2.5.7 | AI 结果批量晋级 Manual |
| P1 | `/api/rcm/import-from-manual?projectId=` | POST | 2.5.5 | Manual 批量导入 Final |

---

## 附录 C：2.5.4 相关接口（参考）

> 按需求不纳入 PRD 统计，但代码中已实现，供联调参考。

| 业务 | 方法 | 路径 |
|------|------|------|
| 单次询问详情（含附件、版本） | GET | `/api/request/{requestId}` |
| 保存版本 | POST | `/api/request/{requestId}/versions` |
| 上传附件（pdf/excel/word） | POST | `/api/request/{requestId}/attachments` |
| 删除附件 | DELETE | `/api/request/{requestId}/attachments/{attachmentId}` |

> 附件列表包含在 `GET /api/request/{requestId}` 的响应 `data.attachments` 中，无独立列表接口。

---

*文档维护：新增或修改 `@RestController` 后请同步更新本文档。*
