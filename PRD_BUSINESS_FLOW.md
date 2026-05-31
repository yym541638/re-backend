# SOC 合规自动化软件 — 业务流程图

## 1. 系统全景

```mermaid
flowchart TB
    subgraph DOMAIN_A["域 A：门户与账号"]
        A1["2.1.1 首页下拉导航"]
        A2["2.1.2 登录"]
        A3["2.1.3 注册"]
        A4["2.4 Profile<br/>个人/公司资料"]
    end

    subgraph DOMAIN_B["域 B：商业化 / SOC2 准入"]
        B1["2.2.1 选择产品<br/>已购用户入口"]
        B2["2.2.2 购买产品<br/>定价与套餐"]
        B3["2.3 Checkout<br/>支付"]
    end

    subgraph DOMAIN_C["域 C：项目合规作业"]
        C1["2.5.1 项目信息编辑"]
        C2["2.5.2 Request Master"]
        C3["2.5.3 单项目询问管理"]
        C4["2.5.4 Individual Request"]
        C5["RCM 2.5.7 / 2.5.6 / 2.5.5"]
        C6["2.5.8 Control Testing"]
        C7["2.5.9 Gap Analysis"]
        C8["2.5.10 Passing Scores"]
        C9["2.5.11 Operation Log"]
        C10["2.5.12 Project Settings"]
    end

    A1 --> A2
    A1 --> A3
    A2 --> B1
    A2 --> B2
    A2 --> A4
    A3 --> B2
    B2 --> B3
    B3 --> B1
    B1 --> C1
    C1 --> C2
```

## 2. 新用户 / 未购用户

```mermaid
flowchart LR
    START((访客)) --> NAV["2.1.1 首页下拉"]
    NAV --> LOGIN["2.1.2 登录"]
    NAV --> REG["2.1.3 注册"]

    REG -->|"注册成功"| CHECKOUT["2.3 Checkout"]
    LOGIN -->|"未购买 SOC2"| PRICING["2.2.2 购买产品"]
    PRICING -->|"Buy"| CHECKOUT

    CHECKOUT -->|"短信验证码"| CHECKOUT
    CHECKOUT -->|"PayPal / QR Code"| PAY["支付网关"]
    PAY -->|"支付成功"| SOC_HOME["2.2.1 选择产品"]

    LOGIN -->|"已购买"| SOC_HOME
```

## 3. 已购用户

```mermaid
flowchart LR
    LOGIN2["2.1.2 登录"] --> SOC2["2.2.1 选择产品"]
    SOC2 -->|"Enter"| PROJ["2.5.1 项目信息编辑"]
    SOC2 -->|"Promote"| PRICING2["2.2.2 购买产品"]
    PRICING2 --> CHECKOUT2["2.3 Checkout"]
```

## 4. Profile

```mermaid
flowchart TB
    AVATAR["登录后头像"] --> PROFILE["2.4 Profile"]
    PROFILE --> UP["User Profile"]
    PROFILE --> CP["Company Profile<br/>Comp Admin"]
```

## 5. SOC2 与项目主线

```mermaid
flowchart TB
    SOC["2.2.1 选择产品"] --> PO["2.5.1 项目信息编辑"]

    PO -->|"New / Edit / Delete"| PO
    PO -->|"Enter Project"| RM["2.5.2 Request Master"]

    PO -.-> PS["2.5.12 Project Settings"]
    PO -.-> OL["2.5.11 Operation Log"]

    RM -->|"Enter"| IND["2.5.4 Individual Request"]
    RM -->|"user invite"| INV["邀请码"]
    INV -.-> REG2["2.1.3 注册"]

    RM --> RCM_FLOW["RCM → Control → Gap → Score"]
```

## 6. 合规核心流水线

```mermaid
flowchart TB
    subgraph INPUT["上游：询问与证据"]
        RM["2.5.2 Request Master"]
        R3["2.5.3 单项目询问"]
        IR["2.5.4 Individual Request"]
        RM --> R3
        R3 -->|"Enter"| IR
        RM -->|"Enter"| IR
    end

    subgraph RCM_STAGE["中游：RCM"]
        AI["2.5.7 AI Generate RCM"]
        MAN["2.5.6 RCM Manual"]
        FIN["2.5.5 RCM Final"]
        AI <-->|"Upload to Manual"| MAN
        MAN <-->|"Upload to Final"| FIN
        AI <-->|"back"| FIN
    end

    subgraph OUTPUT["下游：测试与结论"]
        CT["2.5.8 Control Testing"]
        GA["2.5.9 Gap Analysis"]
        PSC["2.5.10 Passing Scores"]
    end

    IR -->|"【自动】资料更新"| AI
    AI --> MAN
    MAN --> FIN

    FIN -->|"【自动】RCM 变更"| CT
    CT -->|"【自动】"| GA
    CT --> PSC
    GA --> PSC
    FIN -->|"【自动】"| GA

    INPUT --> RCM_STAGE
```

## 7. Individual Request → RCM 时序

```mermaid
sequenceDiagram
    participant U as 用户
    participant IR as 2.5.4 Individual Request
    participant BE as 后端
    participant AI as 2.5.7 AI Generate RCM
    participant MAN as 2.5.6 RCM Manual
    participant FIN as 2.5.5 RCM Final

    U->>IR: 编辑 / 上传附件 / 版本
    IR->>BE: 保存
    BE-->>AI: 【自动】刷新 AI RCM

    U->>AI: Upload to Manual
    AI->>MAN: 进入 Manual

    U->>MAN: Edit / Fill by AI / Upload to Final
    MAN->>FIN: 终稿

    U->>FIN: Upload from Manual
    U->>FIN: 查看 Control Testing 状态
```

## 8. RCM 三阶段

```mermaid
stateDiagram-v2
    [*] --> AI_Generated: Request 资料变更

    state AI_Generated {
        [*] --> List_AI: 2.5.7
        List_AI --> Version_AI: 版本
        List_AI --> To_Manual: Upload to Manual
    }

    state Manual {
        [*] --> List_Man: 2.5.6
        List_Man --> NewRow: New
        List_Man --> EditRow: Edit
        List_Man --> FillAI: Fill by AI
        List_Man --> UploadRCM: Upload RCM
        List_Man --> To_Final: Upload to Final
        List_Man --> Jump_AI: → AI Generate
        List_Man --> Jump_Fin: → RCM Final
    }

    state Final {
        [*] --> List_Fin: 2.5.5
        List_Fin --> Version_Fin: 版本
        List_Fin --> UploadRCM_F: Upload RCM
        List_Fin --> From_Manual: from Manual
        List_Fin --> Jump_Man: → Manual
        List_Fin --> Jump_AI: → AI Generate
        List_Fin --> Show_CT: Control Testing 状态
    }

    AI_Generated --> Manual: Upload to Manual
    Manual --> Final: Upload to Final
    Final --> Manual: from Manual
    Final --> [*]: 更新 Control Testing
```

## 9. 数据级联

```mermaid
flowchart LR
    REQ["Request<br/>2.5.2/3/4"] -->|"增删改/附件"| RCM["RCM<br/>2.5.7/6/5"]
    RCM -->|"增删改"| CT["Control Testing<br/>2.5.8"]
    RCM -->|"增删改"| GAP["Gap Analysis<br/>2.5.9"]
    CT -->|"增删改"| GAP
    CT --> SCORE["Passing Scores<br/>2.5.10"]
    GAP --> SCORE

    REQ -.-> LOG["Operation Log<br/>2.5.11"]
    RCM -.-> LOG
    CT -.-> LOG
```

## 10. 页面跳转总图

```mermaid
flowchart TB
    HOME["2.1.1 首页"] --> LOGIN["2.1.2 登录"]
    HOME --> REG["2.1.3 注册"]
    LOGIN --> PROFILE["2.4 Profile"]

    LOGIN --> SOC_SEL["2.2.1 选择产品"]
    SOC_SEL -->|"Promote"| BUY["2.2.2 购买产品"]
    BUY -->|"Buy"| CHK["2.3 Checkout"]
    CHK --> SOC_SEL

    SOC_SEL -->|"Enter"| PO["2.5.1 项目信息编辑"]
    PO -->|"Enter Project"| RM["2.5.2 Request Master"]
    PO --> PS["2.5.12 Project Settings"]

    RM -->|"Enter"| IR["2.5.4 Individual Request"]
    RM --> R3["2.5.3 单项目询问"]
    R3 -->|"Enter"| IR

    RM --> AI["2.5.7 AI Generate"]
    RM --> MAN["2.5.6 RCM Manual"]
    RM --> FIN["2.5.5 RCM Final"]
    RM --> CT["2.5.8 Control Testing"]
    RM --> GAP["2.5.9 Gap Analysis"]
    RM --> SC["2.5.10 Passing Scores"]
    RM --> OL["2.5.11 Operation Log"]

    IR --> AI

    AI --> FIN
    AI --> MAN
    AI -->|"Upload to Manual"| MAN

    MAN --> FIN
    MAN --> AI

    FIN --> MAN
    FIN --> AI
    FIN -->|"from Manual"| FIN

    FIN --> CT
    CT --> GAP
    CT --> SC
    GAP --> SC
```

## 11. 角色与横切

```mermaid
flowchart TB
    CA["Company Administrator"] --> PO_EDIT["2.5.1 项目 CRUD"]
    CA --> REQ_DEL["2.5.2/3 删除询问"]
    CA --> CP["2.4 Company Profile"]
    CA --> PS["2.5.12 Project Settings"]

    PU["Project Owner 等"] --> REQ_EDIT["询问编辑"]
    PU --> RCM_EDIT["RCM 编辑"]
    PU --> CT_EDIT["Control Testing 编辑"]

    GU["General User"] --> REQ_FILL["填写询问/附件"]
    GU --> VIEW["只读查看"]

    PO_EDIT --> LOG["2.5.11 Operation Log"]
    REQ_EDIT --> LOG
    RCM_EDIT --> LOG
    CT_EDIT --> LOG
```

## 12. 端到端作业顺序

```mermaid
flowchart LR
    S1["① 购买<br/>2.2→2.3"] --> S2["② 建项目<br/>2.5.1"]
    S2 --> S3["③ 成员<br/>2.5.12"]
    S3 --> S4["④ 询问<br/>2.5.2→2.5.4"]
    S4 --> S5["⑤ AI RCM<br/>2.5.7"]
    S5 --> S6["⑥ 定稿<br/>2.5.6→2.5.5"]
    S6 --> S7["⑦ 测试<br/>2.5.8"]
    S7 --> S8["⑧ 差距<br/>2.5.9"]
    S8 --> S9["⑨ 分数<br/>2.5.10"]
    S9 --> S10["⑩ 日志<br/>2.5.11"]
```
