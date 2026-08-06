# Risk table 接口说明

基路径：`/api/risk-table`（需 JWT；需先选定项目）

## 列表（分页）

- **GET** `/api/risk-table/list`

| 参数 | 必填 | 说明 |
|------|------|------|
| `projectId` | 是 | 项目 ID |
| `ccCriteria` | 否 | CC 条款筛选 |
| `riskLevel` | 否 | `HIGH` / `MEDIUM` / `LOW` |
| `riskSource` | 否 | `MANUAL` / `UPLOAD` / `AI_GENERATION` |
| `keyword` | 否 | 搜索条款名 / 子风险 / Points of Focus / 补充描述 |
| `pageNum` | 否 | 默认 1 |
| `pageSize` | 否 | 默认 10 |

## 详情

- **GET** `/api/risk-table/{riskId}`

## 新建（New）

- **POST** `/api/risk-table`

```json
{
  "projectId": 10,
  "ccCriteria": "CC 6.1",
  "cycleName": "FY2026",
  "modulesId": "SEC",
  "modulesName": "Security",
  "ccCriteriaName": "Logical Access",
  "subRiskName": "Unauthorized access",
  "pointsOfFocusName": "...",
  "riskLevel": "MEDIUM",
  "riskSource": "MANUAL",
  "additionalRiskProfileDescription": "..."
}
```

未传 `riskLevel` 默认 `MEDIUM`；未传 `riskSource` 默认 `MANUAL`（手工新建）。

## 编辑（Operation · Edit）

- **PUT** `/api/risk-table/{riskId}`
- Body 字段同新建（不含 `projectId`）；只更新传入字段。

## 删除（Operation · Delete）

- **DELETE** `/api/risk-table/{riskId}`
- 软删除

## 建表 / 种子数据

- 建表：`src/main/resources/sql/011_risk_table.sql`
- AICPA 2017 TSC 映射导入（Excel 前三列）：`src/main/resources/sql/012_risk_table_seed_aicpa_tsc.sql`
  - `TSC Ref. #` → `cc_criteria`
  - `Criteria` → `cc_criteria_name`
  - `Points of Focus` → `points_of_focus_name`
  - 其余列（Cycle / Modules / Sub-risk / Risk Level 等）留空，页面通过 **Edit** 补充
  - 重新生成：`python scripts/seed_risk_from_aicpa_excel.py --apply --project-id <项目ID>`

