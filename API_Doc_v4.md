# Prototype API Mapping v4 (All Endpoints + JSON + Page Mapping)

## 1. Base

- Base URL: `http://127.0.0.1:18081/api`
- Auth: `Authorization: Bearer <token>`
- Common Response:

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

## 2. Auth

### 2.1 Login (Page: Homepage + login + signup)
- Method: `POST`
- Path: `/auth/login`

Request:
```json
{
  "account": "test@test.com",
  "password": "Test@123456"
}
```

Response:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "token": "jwt-token",
    "expire_in": 7200,
    "purchase_status": 1,
    "redirect_to": "order",
    "user_info": {
      "id": 1001,
      "company_id": 2001,
      "company_name": "Demo Company",
      "username": "George Yao",
      "email": "test@test.com",
      "phone": "13800000000",
      "avatar_url": "",
      "job_title": "Auditor",
      "role": "GENERAL_USER"
    }
  }
}
```

### 2.2 Register (Page: Homepage + login + signup)
- Method: `POST`
- Path: `/auth/register`

Request (legacy supported):
```json
{
  "firstName": "George",
  "lastName": "Yao",
  "email": "test@test.com",
  "password": "Test@123456",
  "phone": "13800000000",
  "companyName": "Demo Company",
  "role": "GENERAL_USER"
}
```

Request (alt):
```json
{
  "displayName": "George Yao",
  "email": "test@test.com",
  "password": "Test@123456",
  "phone": "13800000000",
  "companyName": "Demo Company",
  "roleCode": "GENERAL_USER"
}
```

Response:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "token": "jwt-token",
    "expire_in": 7200,
    "purchase_status": 0,
    "redirect_to": "payment",
    "user_info": {
      "id": 1002,
      "company_id": 2001,
      "company_name": "Demo Company",
      "username": "George Yao",
      "email": "test@test.com",
      "phone": "13800000000",
      "avatar_url": "",
      "job_title": "",
      "role": "GENERAL_USER"
    }
  }
}
```

## 3. Product / Pricing / Checkout

### 3.1 Product List (Page: User product select page)
- Method: `GET`
- Path: `/product/list`

Response:
```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "product_id": 1,
      "product_name": "SOC2",
      "product_code": "SOC2",
      "intro_text": "SOC2 introduction",
      "logo_url": "https://example.com/logo.png",
      "trust_principles": ["Security", "Availability"]
    }
  ]
}
```

### 3.2 Product Detail (Page: SOC2 Pricing / Products)
- Method: `GET`
- Path: `/product/detail/{productId}`
- Query: `auditType` or `audit_type` (`Type1` / `Type2`)
- Rule: `annual_price` returns the price for the selected audit type.

Response:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "product_id": 1,
    "product_name": "SOC2",
    "product_code": "SOC2",
    "introduction_title": "Introduction of SOC2",
    "introduction_text": "SOC2 product details",
    "logo_url": "https://example.com/logo.png",
    "trust_principles": ["Security", "Availability"],
    "all_features": ["Security", "Availability", "Privacy"],
    "packages": [
      {
        "package_id": 1,
        "package_name": "Basic 3",
        "annual_price": 5999,
        "type1_price": 5999,
        "type2_price": 7999,
        "included_features": ["Security"],
        "supported_types": ["Type1", "Type2"],
        "default_type": "Type1"
      }
    ],
    "products": [
      {
        "id": 1,
        "name": "Basic 3",
        "features": {
          "security": true,
          "availability": false,
          "privacy": false,
          "processing": false,
          "confidentiality": false
        },
        "typeSwitch": false,
        "price": 5999,
        "type1Price": 5999,
        "type2Price": 7999
      }
    ]
  }
}
```

### 3.3 My Products (Page: SOC2)
- Method: `GET`
- Path: `/product/my`

Response:
```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "product_id": 1,
      "product_name": "SOC2",
      "package_id": 10,
      "package_name": "Basic3",
      "audit_type": "Type1",
      "status": "ACTIVE",
      "source_order_no": "ORD123",
      "start_time": "2026-03-27 10:00:00",
      "end_time": "2027-03-26 23:59:59"
    }
  ]
}
```

### 3.4 Payment Submit (Page: Checkout)
- Method: `POST`
- Path: `/payment/submit`
- Rule: backend is the pricing authority; `amount` is ignored.

Request:
```json
{
  "order_no": "ORD123",
  "product_id": 1,
  "package_id": 1,
  "audit_type": "Type1",
  "amount": 1,
  "payment_method": "PAYPAL",
  "return_url": "http://localhost:8080/order",
  "notify_url": "http://localhost:18081/api/payment/notify"
}
```

Response:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "order_no": "ORD123",
    "payment_url": "/api/payment/mock/success?orderNo=ORD123",
    "qr_code": "data:image/png;base64,...",
    "expire_time": "2026-03-27 12:00:00",
    "amount": 5999,
    "annual_price": 5999,
    "audit_type": "Type1"
  }
}
```

### 3.5 Payment Query (Page: Checkout)
- Method: `GET`
- Path: `/payment/query/{orderNo}`

Response:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "order_no": "ORD123",
    "status": "PAID",
    "amount": 5999,
    "transaction_id": "MOCK123",
    "pay_time": "2026-03-27 11:00:00"
  }
}
```

### 3.6 Payment Notify (Page: Checkout)
- Method: `POST`
- Path: `/payment/notify`

Response:
```json
{
  "code": 0,
  "message": "success",
  "data": null
}
```

### 3.7 Payment Mock Success (Page: Checkout)
- Method: `GET`
- Path: `/payment/mock/success`

Response:
```json
{
  "code": 0,
  "message": "success",
  "data": "/api/order/detail/ORD123?orderNo=ORD123&payStatus=success"
}
```

### 3.8 My Orders (Page: Order)
- Method: `GET`
- Path: `/order/my`
- Note: no pagination.

Response:
```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "order_id": 1,
      "order_no": "ORD123",
      "user_id": 1001,
      "product_id": 1,
      "package_id": 10,
      "product_name": "SOC2",
      "package_name": "Basic3",
      "audit_type": "Type1",
      "amount": 5999,
      "payment_method": "PAYPAL",
      "status": "PAID",
      "transaction_id": "MOCK123",
      "return_url": "http://localhost:8080/order",
      "notify_url": "http://localhost:18081/api/payment/notify",
      "pay_time": "2026-03-27T11:00:00",
      "created_at": "2026-03-27T10:50:00",
      "updated_at": "2026-03-27T11:00:00"
    }
  ]
}
```

### 3.9 Order Detail (Page: Order)
- Method: `GET`
- Path: `/order/detail/{orderNo}`

Response:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "order_id": 1,
    "order_no": "ORD123",
    "user_id": 1001,
    "product_id": 1,
    "package_id": 10,
    "product_name": "SOC2",
    "package_name": "Basic3",
    "audit_type": "Type1",
    "amount": 5999,
    "payment_method": "PAYPAL",
    "status": "PAID",
    "transaction_id": "MOCK123",
    "return_url": "http://localhost:8080/order",
    "notify_url": "http://localhost:18081/api/payment/notify",
    "pay_time": "2026-03-27T11:00:00",
    "created_at": "2026-03-27T10:50:00",
    "updated_at": "2026-03-27T11:00:00"
  }
}
```

### 3.10 Cancel Order (Page: Order)
- Method: `PUT` or `DELETE`
- Path: `/order/cancel/{orderNo}`

Response:
```json
{
  "code": 0,
  "message": "success",
  "data": null
}
```

## 4. Profile

### 4.1 User Profile (Page: User Profile)
- Method: `GET`
- Path: `/user/info`

Response:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "user_id": 1001,
    "username": "George Yao",
    "email": "test@test.com",
    "phone": "13800000000",
    "avatar_url": "",
    "job_title": "Auditor",
    "role": "GENERAL_USER",
    "company": {
      "company_id": 2001,
      "company_name": "Demo Company",
      "company_code": "COMP001",
      "industry": "SaaS",
      "website": "https://example.com",
      "contact_name": "George",
      "contact_phone": "13800000000",
      "address": "Shanghai"
    }
  }
}
```

### 4.2 Update Profile (Page: User Profile)
- Method: `PUT`
- Path: `/user/info`

Request:
```json
{
  "displayName": "George Yao",
  "email": "test@test.com",
  "phone": "13800000000",
  "avatarUrl": "",
  "jobTitle": "Auditor"
}
```

Response: same as 4.1

### 4.3 User Search (Page: Project settings / user management)
- Method: `GET`
- Path: `/user/list`
- Note: no pagination.

Response:
```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "user_id": 1001,
      "username": "George Yao",
      "email": "test@test.com",
      "phone": "13800000000",
      "role": "GENERAL_USER"
    }
  ]
}
```

### 4.4 Role List (Page: Project settings / user management)
- Method: `GET`
- Path: `/user/roles`

Response:
```json
{
  "code": 0,
  "message": "success",
  "data": [
    { "role_code": "COMP_ADMIN", "role_name": "Comp Admin" },
    { "role_code": "DOCUMENT_OWNER", "role_name": "Document owner" },
    { "role_code": "GENERAL_USER", "role_name": "General User" },
    { "role_code": "MANAGER", "role_name": "Manager" },
    { "role_code": "MANAGER_2", "role_name": "Manager 2" },
    { "role_code": "PROJECT_OWNER", "role_name": "Project owner" }
  ]
}
```

### 4.5 Company Profile (Page: Company)
- Method: `GET`
- Path: `/admin/company`

Response:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "company_id": 2001,
    "company_name": "Demo Company",
    "company_code": "COMP001",
    "industry": "SaaS",
    "website": "https://example.com",
    "contact_name": "George",
    "contact_phone": "13800000000",
    "address": "Shanghai"
  }
}
```

## 5. Project

### 5.1 Project List (Page: project overview)
- Method: `GET`
- Path: `/project/list`
- Note: no pagination.

Response:
```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "project_id": 1,
      "company_id": 2001,
      "project_code": "PRJ202603270001",
      "project_name": "SOC2 Project",
      "compliance_type": "SOC2",
      "audit_type": "Type1",
      "current_version": "V1",
      "gap_count": 0,
      "status": "IN_PROGRESS",
      "start_date": "2026-03-27",
      "end_date": "2026-06-30",
      "created_by": 1001,
      "updated_by": 1001,
      "created_at": "2026-03-27T10:00:00",
      "updated_at": "2026-03-27T10:00:00"
    }
  ]
}
```

### 5.2 Project Detail (Page: project overview)
- Method: `GET`
- Path: `/project/detail/{project_id}` or `/project/{projectId}`

Response:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "project": {
      "project_id": 1,
      "company_id": 2001,
      "project_code": "PRJ202603270001",
      "project_name": "SOC2 Project",
      "compliance_type": "SOC2",
      "audit_type": "Type1",
      "current_version": "V1",
      "gap_count": 0,
      "status": "IN_PROGRESS",
      "start_date": "2026-03-27",
      "end_date": "2026-06-30",
      "created_by": 1001,
      "updated_by": 1001,
      "created_at": "2026-03-27T10:00:00",
      "updated_at": "2026-03-27T10:00:00"
    },
    "members": [
      {
        "member_id": 1,
        "project_id": 1,
        "user_id": 1001,
        "member_role": "MANAGER",
        "display_name": "George Yao",
        "email": "test@test.com",
        "created_at": "2026-03-27T10:00:00"
      }
    ]
  }
}
```

### 5.3 Project Create / Update / Delete (Page: project overview)
- Create: `POST /project/create`
- Update: `PUT /project/{projectId}`
- Delete: `DELETE /project/{projectId}`

Response (create/update):
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "project_id": 1,
    "company_id": 2001,
    "project_code": "PRJ202603270001",
    "project_name": "SOC2 Project",
    "compliance_type": "SOC2",
    "audit_type": "Type1",
    "current_version": "V1",
    "gap_count": 0,
    "status": "IN_PROGRESS",
    "start_date": "2026-03-27",
    "end_date": "2026-06-30",
    "created_by": 1001,
    "updated_by": 1001,
    "created_at": "2026-03-27T10:00:00",
    "updated_at": "2026-03-27T10:00:00"
  }
}
```

Response (delete):
```json
{
  "code": 0,
  "message": "success",
  "data": null
}
```

### 5.4 Project Members (Page: project settings)
- Method: `PUT`
- Path: `/project/{projectId}/members`

Response:
```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "member_id": 1,
      "project_id": 1,
      "user_id": 1001,
      "member_role": "MANAGER",
      "display_name": "George Yao",
      "email": "test@test.com"
    }
  ]
}
```

## 6. Request

### 6.1 Request List (Page: Request Master)
- Method: `GET`
- Path: `/request/list`
- Required: `projectId` or `project_id`
- Note: no pagination.

Response:
```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "request_id": 1,
      "project_id": 1,
      "request_code": "REQ202603270001",
      "cc_criteria": "CC1.1",
      "title": "Access review evidence",
      "request_description": "Provide quarterly review evidence",
      "points_of_focus": "Privileged users",
      "document_status": "PENDING",
      "document_owner": "IT Manager",
      "implementation_date": "2026-03-27",
      "last_update_at": "2026-03-27T10:00:00",
      "notes": "",
      "requestor": "Auditor",
      "comments": "",
      "current_version": "V1"
    }
  ]
}
```

### 6.2 Request Detail (Page: Request individual)
- Method: `GET`
- Path: `/request/detail/{request_id}` or `/request/{requestId}`

Response:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "request": {
      "request_id": 1,
      "project_id": 1,
      "request_code": "REQ202603270001",
      "cc_criteria": "CC1.1",
      "title": "Access review evidence",
      "request_description": "Provide quarterly review evidence",
      "points_of_focus": "Privileged users",
      "document_status": "PENDING",
      "document_owner": "IT Manager",
      "implementation_date": "2026-03-27",
      "last_update_at": "2026-03-27T10:00:00",
      "notes": "",
      "requestor": "Auditor",
      "comments": "",
      "current_version": "V1"
    },
    "attachments": [
      {
        "attachment_id": 1,
        "request_id": 1,
        "file_name": "evidence.pdf",
        "file_path": "request/1/2026-03-27/uuid.pdf",
        "file_type": "pdf",
        "content_type": "application/pdf",
        "file_size": 123456
      }
    ],
    "versions": [
      {
        "version_id": 1,
        "request_id": 1,
        "version_no": "V1",
        "snapshot_json": "{}",
        "change_summary": "Initial version",
        "created_at": "2026-03-27T10:00:00"
      }
    ]
  }
}
```

### 6.3 Request Create / Update / Save Version / Delete (Page: Request Master)
- Create: `POST /request/create`
- Update: `PUT /request/update/{request_id}`
- Save version: `POST /request/{request_id}/save-version`
- Delete: `DELETE /request/{requestId}`

### 6.4 Request Attachments (Page: Request individual)
- List: `GET /request/{request_id}/documents`
- Upload: `POST /request/{request_id}/document/upload` (`multipart/form-data`, field `file`)
- Delete: `DELETE /request/document/{document_id}`

## 7. RCM

### 7.1 RCM List (Page: RCM Manual / AI generated / RCM Final)
- Method: `GET`
- Paths: `/rcm/list` / `/rcm/manual/list` / `/rcm/ai-generated/list` / `/rcm/final/list`
- Query:
  `projectId` or `project_id` is required.
  Optional filters: `status`, `category`, `module`, `riskRating`, `risk_rating`, `sourceRequestId`, `source_request_id`
- Note: no pagination.

Response:
```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "rcm_id": 8,
      "project_id": 2,
      "control_code": "REQ-2",
      "control_name": "Access Review Control",
      "description": "Review privileged user access regularly",
      "category": "CC1.1",
      "module_name": "Security",
      "risk_description": "Privileged access may not be reviewed in time",
      "status": "DRAFT",
      "stage": "AI_GENERATED",
      "ai_generated": true,
      "source_request_id": 2,
      "source_rcm_id": null,
      "control_objective": "Ensure privileged access is reviewed and approved",
      "implementation_method": "Quarterly access review with approval workflow",
      "evidence_requirement": "Review records, approval screenshots, exported user list",
      "control_performer": "IT Manager",
      "control_reviewer": "Compliance Manager",
      "additional_owner": "",
      "control_risk_rating": "HIGH",
      "current_version": "V2",
      "created_at": "2026-03-27T10:20:00",
      "updated_at": "2026-03-27T10:45:00"
    }
  ]
}
```

### 7.2 RCM Detail / Versions (Page: RCM Manual / AI generated / RCM Final)
- Detail: `GET /rcm/{rcmId}`
- Versions: `GET /rcm/{rcm_id}/versions`

Detail response:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "rcm": {
      "rcm_id": 8,
      "project_id": 2,
      "control_code": "REQ-2",
      "control_name": "Access Review Control",
      "description": "Review privileged user access regularly",
      "category": "CC1.1",
      "module_name": "Security",
      "risk_description": "Privileged access may not be reviewed in time",
      "status": "DRAFT",
      "stage": "AI_GENERATED",
      "ai_generated": true,
      "source_request_id": 2,
      "source_rcm_id": null,
      "control_objective": "Ensure privileged access is reviewed and approved",
      "implementation_method": "Quarterly access review with approval workflow",
      "evidence_requirement": "Review records, approval screenshots, exported user list",
      "control_performer": "IT Manager",
      "control_reviewer": "Compliance Manager",
      "additional_owner": "",
      "control_risk_rating": "HIGH",
      "current_version": "V2",
      "created_at": "2026-03-27T10:20:00",
      "updated_at": "2026-03-27T10:45:00"
    },
    "versions": [
      {
        "version_id": 3,
        "rcm_id": 8,
        "version_no": "V2",
        "snapshot_json": "{...}",
        "change_summary": "Fill by AI",
        "created_at": "2026-03-27T10:45:00"
      }
    ]
  }
}
```

Version list response:
```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "version_id": 3,
      "rcm_id": 8,
      "version_no": "V2",
      "snapshot_json": "{...}",
      "change_summary": "Fill by AI",
      "created_at": "2026-03-27T10:45:00"
    }
  ]
}
```

### 7.3 RCM Excel Import / Export (Page: RCM Manual / AI generated / RCM Final)
- Import: `POST /rcm/upload` (`multipart/form-data`, field `file`)
- Export: `GET /rcm/export`

## 8. Control Testing

### 8.1 Control Test List (Page: Control testing)
- Method: `GET`
- Path: `/control-testing/list`
- Required: `projectId`
- Optional filters: `resultStatus`, `riskLevel`
- Note: no pagination.

Response:
```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "test_id": 1,
      "project_id": 2,
      "title": "Access Control Test",
      "description": "Verify quarterly access review",
      "risk_level": "HIGH",
      "risk_description": "Unauthorized access",
      "coso_principle": "Control Activities",
      "control_procedure": "Quarterly user access review",
      "result_status": "FAIL",
      "current_version": "V1",
      "created_at": "2026-03-27T11:00:00",
      "updated_at": "2026-03-27T11:00:00"
    }
  ]
}
```

### 8.2 Control Test Detail (Page: Control testing)
- Method: `GET`
- Path: `/control-testing/{testId}`

Response:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "control_test": {
      "test_id": 1,
      "project_id": 2,
      "title": "Access Control Test",
      "description": "Verify quarterly access review",
      "risk_level": "HIGH",
      "risk_description": "Unauthorized access",
      "coso_principle": "Control Activities",
      "control_procedure": "Quarterly user access review",
      "result_status": "FAIL",
      "current_version": "V1",
      "created_at": "2026-03-27T11:00:00",
      "updated_at": "2026-03-27T11:00:00"
    },
    "versions": [
      {
        "version_id": 1,
        "test_id": 1,
        "version_no": "V1",
        "snapshot_json": "{...}",
        "change_summary": "Initial version",
        "created_at": "2026-03-27T11:00:00"
      }
    ]
  }
}
```

## 9. Gap / Score / Report

### 9.1 Gap Analysis List (Page: Gap analysis)
- Method: `GET`
- Path: `/gap-analysis/list`

### 9.2 Passing Scores (Page: Passing Scores)
- Method: `GET`
- Path: `/analysis/pass-rate/{projectId}`

### 9.3 Trend (Page: Passing Scores)
- Method: `GET`
- Path: `/analysis/trend` or `/analysis/trend/{projectId}`

### 9.4 Report Task (Page: Passing Scores)
- Create: `POST /analysis/generate-report`
- Status: `GET /analysis/report-status/{taskId}`
- Download: `GET /analysis/download-report/{taskId}`

## 10. Operation Log

### 10.1 Log List (Page: operation log)
- Method: `GET`
- Path: `/operation-log/list`
- Required: `projectId`
- Optional filters: `moduleName`, `actionType`
- Note: no pagination.

Response:
```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "log_id": 11,
      "user_id": 1001,
      "username": "George Yao",
      "module_name": "RCM",
      "action_type": "UPDATE",
      "resource_type": "RCM",
      "resource_id": "8",
      "resource_name": "Access Review Control",
      "project_id": 2,
      "action_detail": "Fill single RCM by AI",
      "created_at": "2026-03-27T10:45:00"
    }
  ]
}
```

### 10.2 Log Stats (Page: operation log)
- Method: `GET`
- Path: `/operation-log/statistics`

## 11. Invitation Code (Page: project settings / user management)

### 11.1 Create Invitation
- Method: `POST`
- Path: `/invitation-code/create`

### 11.2 Validate Invitation
- Method: `GET`
- Path: `/invitation-code/validate/{code}`

### 11.3 Invitation List
- Method: `GET`
- Path: `/invitation-code/list`

### 11.4 Revoke Invitation
- Method: `PUT`
- Path: `/invitation-code/revoke/{id}`

## 12. Pricing Management (Admin)

### 12.1 Update Package Price
- Method: `PUT`
- Path: `/product/package/{packageId}/price`
- Permission: company admin (`COMP_ADMIN`)

Request:
```json
{
  "type1Price": 5999,
  "type2Price": 8999,
  "defaultType": "Type2"
}
```

Response:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "package_id": 1,
    "package_name": "Basic 3",
    "annual_price": 8999,
    "type1_price": 5999,
    "type2_price": 8999,
    "included_features": ["Security"],
    "supported_types": ["Type1", "Type2"],
    "default_type": "Type2"
  }
}
```
