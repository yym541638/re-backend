# Project / Request 接口文档



---

## 1. 通用约定

### 1.1 统一响应

```json
{
  "code": 0,
  "message": null,
  "data": {},
  "total": null
}
```

| 字段 | 说明 |
|------|------|
| `code` | `0` 表示成功；非 0 为业务/系统错误 |
| `message` | 失败时的错误信息；成功通常为 `null` |
| `data` | 业务数据 |
| `total` | 分页接口可选，与 `data.totalCount` 一致 |

### 1.2 分页结构（`PageResponse`）

```json
{
  "totalCount": 100,
  "total": 100,
  "pageNum": 1,
  "page": 1,
  "pageSize": 10,
  "list": []
}
```

### 1.3 模块划分

| Controller | 前缀 | 职责 |
|------------|------|------|
| `ProjectController` | `/project` | 项目 CRUD、成员、Access Management |
| `RequestMasterController` | `/request-master` | Request Master 列表/CRUD、模板文件、版本 |
| `RequestIndividualController` | `/request/individual` | Individual 列表/生成/CRUD/发送/证据 |

## 2. Project（`/project`）

### 2.1 新建项目

- **POST** `/api/project/create`
- **Content-Type**：`application/json`
- **权限**：仅公司管理员可创建
- **说明**：成功仅返回 `project_id`；详情请再调 `GET /project/{projectId}`。创建人自动写入项目成员；其余角色请在 Access Management 通过 `PUT /project/{projectId}/members` 分配，创建接口不再接收 `members`。

**Body**：

| 字段 | 类型 | 必填 | 别名 | 说明 |
|------|------|------|------|------|
| `projectName` | String | 是 | `project_name` | 项目名称 |
| `projectInfo` | String | 否 | `project_info` | 项目说明 |
| `startDate` | DateTime | 是 | `start_date` | 开始时间 |
| `endDate` | DateTime | 否 | `end_date` | 结束时间 |

```json
{
  "projectName": "SOC2 FY2026",
  "projectInfo": "Annual SOC 2 Type II",
  "startDate": "2026-01-01T00:00:00",
  "endDate": "2026-12-31T23:59:59"
}
```

**响应 `data`**：

```json
{ "project_id": 10 }
```

---

### 2.2 项目列表（分页）

- **GET** `/api/project/list`

| 参数 | 位置 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| `keyword` | Query | 否 | | 关键字 |
| `pageNum` | Query | 否 | `1` | 页码 |
| `pageSize` | Query | 否 | `10` | 每页条数 |

**响应 `data`**：`PageResponse<ProjectListItem>`

```json
{
  "totalCount": 1,
  "pageNum": 1,
  "pageSize": 10,
  "list": [
    {
      "project_id": 10,
      "project_name": "SOC2 FY2026",
      "project_info": "Annual SOC 2 Type II",
      "start_date": "2026-01-01T00:00:00",
      "end_date": "2026-12-31T23:59:59",
      "last_modified_date": "2026-07-25T08:00:00"
    }
  ]
}
```

---

### 2.3 Access Management（访问矩阵）

- **GET** `/api/project/access-management`
- **说明**：项目行 × 角色列用户。同一角色可返回多条（每用户一条），同一用户也可出现在多个角色下。Invite 用 `POST /invitation-code/project/create`；兑换用 `POST /invitation-code/redeem`（属邀请码模块）

| 参数 | 位置 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| `keyword` | Query | 否 | | 关键字 |
| `pageNum` | Query | 否 | `1` | 页码 |
| `pageSize` | Query | 否 | `10` | 每页条数 |

**响应 `data`**：`PageResponse<ProjectAccessMatrixItem>`

```json
{
  "totalCount": 1,
  "pageNum": 1,
  "pageSize": 10,
  "list": [
    {
      "project_id": 10,
      "project_name": "SOC2 FY2026",
      "role_slots": [
        {
          "role_code": "PROJECT_OWNER",
          "role_name": "Project Owner",
          "user_id": 1,
          "display_name": "Alice",
          "email": "alice@example.com"
        },
        {
          "role_code": "DOCUMENT_OWNER",
          "role_name": "Document Owner",
          "user_id": 1,
          "display_name": "Alice",
          "email": "alice@example.com"
        },
        {
          "role_code": "GENERAL_USER",
          "role_name": "General User",
          "user_id": 2,
          "display_name": "Bob",
          "email": "bob@example.com"
        },
        {
          "role_code": "GENERAL_USER",
          "role_name": "General User",
          "user_id": 3,
          "display_name": "Carol",
          "email": "carol@example.com"
        }
      ]
    }
  ]
}
```

未分配用户的角色仍返回一条空槽位（`user_id` / `display_name` / `email` 为空），便于前端渲染。

---

### 2.4 角色槽位定义

- **GET** `/api/project/role-slots`
- **说明**：供 Access Management 渲染六个固定角色行

**响应 `data`**：

| role_code | role_name |
|-----------|-----------|
| `COMP_ADMIN` | Administrator |
| `PROJECT_OWNER` | Project Owner |
| `DOCUMENT_OWNER` | Document Owner |
| `GENERAL_USER` | General User |
| `MANAGER` | 1st tier Manager User |
| `MANAGER_2` | 2nd tier Manager User 1 |

未分配用户时 `user_id` / `display_name` / `email` 为空。

---

### 2.5 公司用户列表（成员选择）

- **GET** `/api/project/company/users`
- **权限**：仅公司管理员
- **说明**：`companyName` 须与当前登录用户所属公司一致

| 参数 | 位置 | 必填 | 说明 |
|------|------|------|------|
| `companyName` | Query | 是 | 公司名称 |
| `keyword` | Query | 否 | 搜索关键字 |

**响应 `data`**：

```json
[
  {
    "user_id": 1,
    "username": "Alice",
    "email": "alice@example.com",
    "phone": "13800000000",
    "permission": "COMP_ADMIN",
    "user_type": "Clients"
  }
]
```

---

### 2.6 项目详情

- **GET** `/api/project/{projectId}`

**响应 `data`**：

```json
{
  "project": {
    "project_id": 10,
    "project_name": "SOC2 FY2026",
    "project_info": "...",
    "start_date": "2026-01-01T00:00:00",
    "end_date": "2026-12-31T23:59:59",
    "last_modified_date": "2026-07-25T08:00:00"
  },
  "members": [
    {
      "memberId": 1,
      "projectId": 10,
      "userId": 1,
      "memberRole": "COMP_ADMIN",
      "displayName": "Alice",
      "email": "alice@example.com",
      "deleted": 0,
      "createdBy": 1,
      "updatedBy": 1,
      "createdAt": "2026-07-25T08:00:00",
      "updatedAt": "2026-07-25T08:00:00"
    }
  ],
  "roleSlots": [
    {
      "role_code": "COMP_ADMIN",
      "role_name": "Administrator",
      "user_id": 1,
      "display_name": "Alice",
      "email": "alice@example.com"
    }
  ]
}
```

---

### 2.7 编辑项目

- **PUT** `/api/project/{projectId}`
- **说明**：可更新名称/说明/起止日期；传入 `members` 时全量覆盖项目成员，不传则保留现有成员

**Body**：

| 字段 | 类型 | 必填 | 别名 |
|------|------|------|------|
| `projectName` | String | 是 | `project_name` |
| `projectInfo` | String | 否 | `project_info` |
| `startDate` | DateTime | 否 | `start_date` |
| `endDate` | DateTime | 否 | `end_date` |
| `members` | MemberItem[] | 否 | `project_members`, `roleAssignments`, `members` |

**响应 `data`**：同 [2.6 详情](#26-项目详情)

---

### 2.8 删除项目

- **DELETE** `/api/project/{projectId}`
- **说明**：软删除
- **响应 `data`**：`null`

---

### 2.9 保存项目成员

- **PUT** `/api/project/{projectId}/members`
- **说明**：全量覆盖项目成员。允许同一 `userId` 绑定多个 `memberRole`，也允许同一角色分配给多名用户；仅禁止完全相同的 `(userId, memberRole)` 重复。

**Body**：

```json
{
  "members": [
    { "userId": 1, "memberRole": "PROJECT_OWNER", "displayName": "Alice" },
    { "userId": 1, "memberRole": "DOCUMENT_OWNER", "displayName": "Alice" },
    { "userId": 2, "memberRole": "GENERAL_USER", "displayName": "Bob" },
    { "userId": 3, "memberRole": "GENERAL_USER", "displayName": "Carol" }
  ]
}
```

| 字段 | 类型 | 必填 |
|------|------|------|
| `members` | MemberItem[] | 是（非空） |

**响应 `data`**：`ProjectMember[]`

---

## 3. Request Master（`/request-master`）

### 3.1 列表

- **GET** `/api/request-master/list`

| 参数 | 位置 | 必填 | 说明 |
|------|------|------|------|
| `projectId` | Query | 否 | 项目 ID（推荐） |
| `project_id` | Query | 否 | `projectId` 别名 |

**响应 `data`**：

```json
[
  {
    "request_master_id": 1,
    "request_id": "ReqM000001",
    "request_master_name": "SOC2 Evidence Request",
    "request_master_create_date": "2026-07-25T08:00:00",
    "request_master_status": "INACTIVE"
  }
]
```

---

### 3.2 状态选项

- **GET** `/api/request-master/status-options`

| status | label |
|--------|--------|
| `COMPLETED` | Completed - completed request (no further action allowed) |
| `CANCELLED` | Cancelled - unable to modify |
| `ACTIVE` | Active - sent invitation email to document owner |
| `INACTIVE` | Inactive - haven't sent invitation email to document owner |

---

### 3.3 下载 Individual 批量导入 CSV 模板

- **GET** `/api/request-master/template-files/download-template`
- **说明**：非 `ApiResponse` 包装
- **响应**：
  - `Content-Type: text/csv`
  - `Content-Disposition: attachment; filename="request_individual_template.csv"`
  - Body：文件二进制

---

### 3.4 详情

- **GET** `/api/request-master/{requestMasterId}`

```json
{
  "request_master_id": 1,
  "project_id": 10,
  "request_id": "ReqM000001",
  "request_master_name": "SOC2 Evidence Request",
  "create_date": "2026-07-25T08:00:00",
  "request_master_status": "INACTIVE"
}
```

> `create_date` 仅展示，不可修改。

---

### 3.5 新建

- **POST** `/api/request-master`

| 字段 | 类型 | 必填 | 别名 |
|------|------|------|------|
| `projectId` | Long | 是 | `project_id` |
| `requestMasterName` | String | 是 | `request_master_name`, `name` |
| `requestMasterStatus` | String | 否 | `request_master_status`, `status`, `RequestMasterStatus` |

```json
{
  "projectId": 10,
  "requestMasterName": "SOC2 Evidence Request",
  "requestMasterStatus": "INACTIVE"
}
```

**响应 `data`**：同详情

---

### 3.6 编辑

- **PUT** `/api/request-master/{requestMasterId}`
- **说明**：`COMPLETED` / `CANCELLED` 状态不可修改

| 字段 | 类型 | 必填 | 别名 |
|------|------|------|------|
| `requestMasterName` | String | 是 | `request_master_name`, `name` |
| `requestMasterStatus` | String | 否 | `request_master_status`, `status`, `RequestMasterStatus` |

---

### 3.7 删除

- **DELETE** `/api/request-master/{requestMasterId}`
- **说明**：软删除
- **响应 `data`**：`null`

---

### 3.8 模板文件列表（File management，分页）

- **GET** `/api/request-master/{requestMasterId}/template-files`
- **说明**：展示已上传文件及其关联条约（`relevant_criteria`，如 `CC1.1` / `Unrelevant`）

| 参数 | 位置 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| `pageNum` | Query | 否 | `1` | 页码 |
| `pageSize` | Query | 否 | `10` | 每页条数 |
| `relevantCriteria` | Query | 否 | | 按条约筛选（别名 `relevant_criteria`） |

```json
{
  "totalCount": 1,
  "pageNum": 1,
  "pageSize": 10,
  "list": [
    {
      "template_file_id": 1,
      "file_no": 1,
      "files": "template.xlsx",
      "relevant_criteria": "CC1.1"
    }
  ]
}
```

---

### 3.9 上传模板文件（File management · Upload）

- **POST** `/api/request-master/{requestMasterId}/template-files`
- **Content-Type**：`multipart/form-data`

| 参数 | 位置 | 必填 | 别名 |
|------|------|------|------|
| `file` | Form | 是 | |
| `relevantCriteria` | Form | 否 | `relevant_criteria` |

---

### 3.10 查看/下载已上传文件（File management · View）

- **GET** `/api/request-master/{requestMasterId}/template-files/{templateFileId}/download`
- **说明**：下载已上传文件二进制流

---

### 3.11 删除模板文件（File management · delete）

- **DELETE** `/api/request-master/{requestMasterId}/template-files/{templateFileId}`
- **说明**：软删除

---

### 3.12 版本列表

- **GET** `/api/request-master/{requestMasterId}/versions`

```json
[
  {
    "version_id": 1,
    "version_label": "2026/07/25 08:00",
    "is_latest": true,
    "created_at": "2026-07-25T08:00:00"
  }
]
```

---

### 3.13 保存版本

- **POST** `/api/request-master/{requestMasterId}/versions/save`
- **说明**：将当前 Individual 快照保存为新版本
- **Body**：无

---

### 3.14 版本详情

- **GET** `/api/request-master/{requestMasterId}/versions/{versionId}`

```json
{
  "version_id": 1,
  "version_label": "2026/07/25 08:00",
  "individuals": [ { "request_id": 100, "request_code": "REQ000001", "request_name": "...", "cc_criteria": "CC1.1" } ]
}
```

`individuals` 元素结构见 [4.1](#41-individual-列表)。

---

## 4. Request Individual（`/request/individual`）

### 4.1 Individual 列表

- **GET** `/api/request/individual/list`

| 参数 | 位置 | 必填 |
|------|------|------|
| `requestMasterId` | Query | 是 |

| 字段 | 说明 |
|------|------|
| `request_id` | Individual 主键 |
| `request_code` | 编码（如 REQ000001） |
| `request_name` | 名称 |
| `cc_criteria` | CC 条款 |
| `points_of_focus` | Points of Focus |
| `request_description` | 描述 |
| `request_creation_date` | 创建时间 |
| `request_assignee` | 指派人 |
| `document_owner_name` | Document Owner 名称 |
| `upload_evidence` | 证据摘要展示 |
| `upload_evidence_date_time` | 证据上传时间 |
| `comment_content` | 用户备注 |
| `upload_evidence_manual_status` | 证据人工状态 |
| `request_send_date` | 发送时间 |
| `request_evidence_review_ai` | Review AI 列：`not right` / `need attention` / `all good`（send 后由 AI 的 red/yellow/green 映射；未 send 为 null） |
| `request_individual_review_status` | 同 `request_evidence_review_ai`（兼容旧字段） |
| `request_individual_review_comment` | AI 审核意见 |

---

### 4.2 新建 Individual

- **POST** `/api/request/individual`

| 字段 | 类型 | 必填 | 别名 | 说明 |
|------|------|------|------|------|
| `requestMasterId` | Long | 是 | `request_master_id` | 所属 Master |
| `requestName` | String | 否 | `request_name`, `title`, `name` | 为空时后台自动生成唯一名 |
| `catalogId` | Long | 否 | `catalog_id` | 标准条款目录行 ID；Generate 灌入时使用 |
| `ccCriteria` | String | 否 | `cc_criteria`, `type` | |
| `pointsOfFocus` | String | 否 | `points_of_focus` | |
| `requestDescription` | String | 否 | `request_description`, `description` | |
| `documentOwnerName` | String | 否 | `document_owner_name`, `document_owner` | |
| `documentOwnerUserId` | Integer | 否 | `document_owner_user_id` | |
| `requestAssignee` | String | 否 | `request_assignee` | |
| `commentContent` | String | 否 | `comment_content`, `user_comment` | |

**响应 `data`**：见 [4.5 详情](#45-详情)

---

### 4.3 按已购套餐生成材料任务

- **POST** `/api/request/individual/generate`

| 参数 | 位置 | 必填 |
|------|------|------|
| `requestMasterId` | Query | 是 |

**说明**：按公司已购套餐范围，从标准条款库灌入 Individual。

**常见错误**：

| 场景 | 提示 |
|------|------|
| 未开通套餐 | 公司尚未开通可用套餐能力，无法生成材料清单 |
| 目录为空 | 已购范围内没有可生成的标准条款 |

---

### 4.4 Document Owner 候选

- **GET** `/api/request/individual/document-owners`

| 参数 | 位置 | 必填 |
|------|------|------|
| `projectId` | Query | 是 |
| `keyword` | Query | 否 |

```json
[
  { "user_id": 1, "display_name": "Alice", "email": "alice@example.com" }
]
```

---

### 4.5 详情

- **GET** `/api/request/individual/{requestId}`

```json
{
  "request_id": 100,
  "request_master_id": 1,
  "project_id": 10,
  "request_code": "REQ000001",
  "request_name": "Access Control Policy",
  "cc_criteria": "CC6.1",
  "points_of_focus": "...",
  "request_description": "...",
  "request_creation_date": "2026-07-25T08:00:00",
  "document_owner_name": "Alice",
  "document_owner_user_id": 1,
  "request_assignee": "Bob",
  "upload_evidence_manual_status": "PENDING",
  "request_send_date": null,
  "request_evidence_review_ai": null,
  "request_evidence_review_ai_color": null,
  "request_evidence_review_ai_status": null,
  "ai_comment_content": null,
  "comment_content": null,
  "evidences": [
    { "attachment_id": 1, "file": "policy.pdf", "time": "2026-07-25T09:00:00" }
  ]
}
```

---

### 4.6 更新

- **PUT** `/api/request/individual/{requestId}`

| 字段 | 类型 | 必填 | 别名 |
|------|------|------|------|
| `requestName` | String | 是 | `request_name`, `title`, `name` |
| `ccCriteria` | String | 否 | `cc_criteria`, `type` |
| `pointsOfFocus` | String | 否 | `points_of_focus` |
| `requestDescription` | String | 否 | `request_description`, `description` |
| `documentOwnerName` | String | 否 | `document_owner_name`, `document_owner` |
| `documentOwnerUserId` | Integer | 否 | `document_owner_user_id` |
| `requestAssignee` | String | 否 | `request_assignee` |
| `commentContent` | String | 否 | `comment_content`, `user_comment` |
| `uploadEvidenceManualStatus` | String | 否 | `upload_evidence_manual_status`, `evidence_manual_status` |

---

### 4.7 删除

- **DELETE** `/api/request/individual/{requestId}`
- **说明**：软删除

---

### 4.8 发送

- **POST** `/api/request/individual/{requestId}/send`
- **Body**：无
- **说明**：触发证据 AI 审核；AI 内部颜色码 `red`/`yellow`/`green` 映射为页面文案：
  - `red` → `not right`
  - `yellow` → `need attention`
  - `green` → `all good`
- **响应 `data`**：同详情，重点字段：
  - `request_evidence_review_ai`：展示文案（列表 Review AI 列同源）
  - `request_evidence_review_ai_color`：`red` / `yellow` / `green`
  - `ai_comment_content`：AI 意见
  - `request_send_date`：发送时间

---

### 4.9 上传证据附件

- **POST** `/api/request/individual/{requestId}/attachments`
- **Content-Type**：`multipart/form-data`
- **参数**：`file`（必填）

```json
{
  "attachmentId": 1,
  "requestId": 100,
  "fileName": "policy.pdf",
  "filePath": "...",
  "fileType": "pdf",
  "contentType": "application/pdf",
  "fileSize": 12345,
  "deleted": 0,
  "createdBy": 1,
  "updatedBy": 1,
  "createdAt": "2026-07-25T09:00:00",
  "updatedAt": "2026-07-25T09:00:00"
}
```

---

### 4.10 重命名证据附件

- **PUT** `/api/request/individual/{requestId}/attachments/{attachmentId}`

| 字段 | 类型 | 必填 | 别名 |
|------|------|------|------|
| `fileName` | String | 是 | `file`, `file_name` |

```json
{ "attachment_id": 1, "file": "renamed-policy.pdf", "time": "2026-07-25T09:00:00" }
```

---

### 4.11 删除证据附件

- **DELETE** `/api/request/individual/{requestId}/attachments/{attachmentId}`
- **说明**：软删除

---

## 5. 接口速查表

### 5.1 Project

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/project/create` | 新建项目 |
| GET | `/api/project/list` | 项目列表 |
| GET | `/api/project/access-management` | 访问矩阵 |
| GET | `/api/project/role-slots` | 角色槽位定义 |
| GET | `/api/project/company/users` | 公司用户列表 |
| GET | `/api/project/{projectId}` | 项目详情 |
| PUT | `/api/project/{projectId}` | 编辑项目 |
| DELETE | `/api/project/{projectId}` | 删除项目 |
| PUT | `/api/project/{projectId}/members` | 保存项目成员 |

### 5.2 Request Master

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/request-master/list` | 列表 |
| GET | `/api/request-master/status-options` | 状态下拉 |
| GET | `/api/request-master/template-files/download-template` | 下载 CSV 模板 |
| GET | `/api/request-master/{requestMasterId}` | 详情 |
| POST | `/api/request-master` | 新建 |
| PUT | `/api/request-master/{requestMasterId}` | 编辑 |
| DELETE | `/api/request-master/{requestMasterId}` | 删除 |
| GET | `/api/request-master/{requestMasterId}/template-files` | File management 列表（含条约） |
| POST | `/api/request-master/{requestMasterId}/template-files` | 上传模板（可带 relevantCriteria） |
| GET | `/api/request-master/{requestMasterId}/template-files/{templateFileId}/download` | View 下载已上传文件 |
| DELETE | `/api/request-master/{requestMasterId}/template-files/{templateFileId}` | 删除模板 |
| GET | `/api/request-master/{requestMasterId}/versions` | 版本列表 |
| POST | `/api/request-master/{requestMasterId}/versions/save` | 保存版本 |
| GET | `/api/request-master/{requestMasterId}/versions/{versionId}` | 版本详情 |

### 5.3 Request Individual

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/request/individual/list` | Individual 列表 |
| POST | `/api/request/individual` | 新建 |
| POST | `/api/request/individual/generate` | 按套餐生成 |
| GET | `/api/request/individual/document-owners` | Document Owner 候选 |
| GET | `/api/request/individual/{requestId}` | 详情 |
| PUT | `/api/request/individual/{requestId}` | 更新 |
| DELETE | `/api/request/individual/{requestId}` | 删除 |
| POST | `/api/request/individual/{requestId}/send` | 发送 |
| POST | `/api/request/individual/{requestId}/attachments` | 上传证据 |
| PUT | `/api/request/individual/{requestId}/attachments/{attachmentId}` | 重命名证据 |
| DELETE | `/api/request/individual/{requestId}/attachments/{attachmentId}` | 删除证据 |


```
