#!/usr/bin/env python3
"""End-to-end smoke test against local re-backend (/api)."""

from __future__ import annotations

import json
import sys
import tempfile
import time
import urllib.error
import urllib.parse
import urllib.request
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any

BASE = "http://localhost:8089/api"
ACCOUNT = "83261582@qq.com"
PASSWORD = "Test@123456"


@dataclass
class Finding:
    severity: str  # BLOCKER / MAJOR / MINOR / INFO
    module: str
    title: str
    detail: str


@dataclass
class Result:
    ok: list[str] = field(default_factory=list)
    findings: list[Finding] = field(default_factory=list)

    def pass_(self, msg: str) -> None:
        self.ok.append(msg)
        print(f"  [PASS] {msg}")

    def fail(self, severity: str, module: str, title: str, detail: str) -> None:
        self.findings.append(Finding(severity, module, title, detail))
        print(f"  [{severity}] {module}: {title} | {detail}")


def request(
    method: str,
    path: str,
    token: str | None = None,
    body: Any = None,
    form: dict[str, Any] | None = None,
    files: dict[str, tuple[str, bytes, str]] | None = None,
    timeout: int = 30,
) -> tuple[int, Any, dict[str, str]]:
    url = BASE + path
    headers: dict[str, str] = {"Accept": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    data = None
    if files or form:
        boundary = f"----CursorBoundary{int(time.time()*1000)}"
        headers["Content-Type"] = f"multipart/form-data; boundary={boundary}"
        parts: list[bytes] = []
        if form:
            for k, v in form.items():
                parts.append(
                    f"--{boundary}\r\nContent-Disposition: form-data; name=\"{k}\"\r\n\r\n{v}\r\n".encode()
                )
        if files:
            for name, (filename, content, ctype) in files.items():
                parts.append(
                    (
                        f"--{boundary}\r\n"
                        f'Content-Disposition: form-data; name="{name}"; filename="{filename}"\r\n'
                        f"Content-Type: {ctype}\r\n\r\n"
                    ).encode()
                    + content
                    + b"\r\n"
                )
        parts.append(f"--{boundary}--\r\n".encode())
        data = b"".join(parts)
    elif body is not None:
        headers["Content-Type"] = "application/json"
        data = json.dumps(body, ensure_ascii=False).encode("utf-8")

    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            raw = resp.read()
            ctype = resp.headers.get("Content-Type", "")
            hdrs = {k: v for k, v in resp.headers.items()}
            if "application/json" in ctype or raw[:1] in (b"{", b"["):
                return resp.status, json.loads(raw.decode("utf-8") or "null"), hdrs
            return resp.status, raw, hdrs
    except urllib.error.HTTPError as e:
        raw = e.read()
        try:
            payload = json.loads(raw.decode("utf-8") or "null")
        except Exception:
            payload = raw.decode("utf-8", errors="replace")
        return e.code, payload, {k: v for k, v in e.headers.items()}


def code_of(payload: Any) -> int | None:
    if isinstance(payload, dict):
        return payload.get("code")
    return None


def data_of(payload: Any) -> Any:
    if isinstance(payload, dict):
        return payload.get("data")
    return None


def expect_ok(r: Result, module: str, name: str, status: int, payload: Any) -> Any:
    if status >= 500:
        r.fail("BLOCKER", module, name, f"HTTP {status}: {payload}")
        return None
    if status >= 400:
        r.fail("MAJOR", module, name, f"HTTP {status}: {payload}")
        return None
    c = code_of(payload)
    if c is not None and c != 0:
        r.fail("MAJOR", module, name, f"biz code={c} payload={payload}")
        return None
    r.pass_(f"{module} {name}")
    return data_of(payload)


def main() -> int:
    r = Result()
    print("=== E2E smoke start ===")

    # 0. Auth
    print("\n[1] Auth / Profile")
    st, payload, _ = request("POST", "/auth/login", body={"account": ACCOUNT, "password": PASSWORD})
    login = expect_ok(r, "Auth", "login", st, payload)
    if not login or not login.get("token"):
        print("Cannot continue without login")
        return 1
    token = login["token"]
    user = login.get("user_info") or {}
    company_id = user.get("company_id")
    purchase_status = login.get("purchase_status")
    if purchase_status in (0, "0", False, None):
        r.fail("MAJOR", "Auth", "purchase_status after prior buy", f"purchase_status={purchase_status}")
    else:
        r.pass_(f"purchase_status={purchase_status}")

    st, payload, _ = request("GET", "/auth/me", token=token)
    expect_ok(r, "Auth", "me", st, payload)

    st, payload, _ = request("GET", "/profile/me", token=token)
    expect_ok(r, "Profile", "me", st, payload)
    st, payload, _ = request("GET", "/profile/company", token=token)
    expect_ok(r, "Profile", "company", st, payload)

    # 1. Commerce
    print("\n[2] Product / Payment / Order")
    st, payload, _ = request("GET", "/product/packages")
    packages = expect_ok(r, "Product", "packages(anonymous)", st, payload) or []
    if packages:
        # business: product_id currently equals package_id (compat) — document as known quirk if still true
        p0 = packages[0]
        if p0.get("product_id") == p0.get("package_id"):
            r.fail(
                "MINOR",
                "Product",
                "packages 返回的 product_id 实际是 package_id",
                f"product_id={p0.get('product_id')} package_id={p0.get('package_id')}（支付侧已做兼容，但字段语义易误导前端）",
            )
        suite = next((p for p in packages if p.get("product_name") == "Product Suite"), packages[-1])
        # purchased type echo with token
        st, payload, _ = request("GET", "/product/packages", token=token)
        pkgs2 = expect_ok(r, "Product", "packages(authed)", st, payload) or []
        suite2 = next((p for p in pkgs2 if p.get("product_name") == "Product Suite"), None)
        if suite2 is not None:
            r.pass_(f"purchased type_switch echo={suite2.get('type_switch')}")

    st, payload, _ = request("GET", "/product/my", token=token)
    my_products = expect_ok(r, "Product", "my", st, payload) or []
    if not my_products:
        r.fail("MAJOR", "Product", "已购为空", "用户应已有 SOC2 订阅（历史购买），my 为空会影响 Generate")
    else:
        r.pass_(f"my products count={len(my_products)} features={my_products[0].get('included_features')}")

    # payment smoke with package id as productId (legacy frontend)
    st, payload, _ = request(
        "POST",
        "/payment/submit",
        token=token,
        body={
            "productId": 3,
            "type_switch": True,
            "selectFeatures": "Security,Availability,Privacy,Processing Integrity,Confidentiality",
            "paymentMethod": "Paypal",
        },
    )
    expect_ok(r, "Payment", "submit Product Suite all features Type2", st, payload)

    st, payload, _ = request("GET", "/order/my", token=token)
    orders = expect_ok(r, "Order", "my", st, payload)
    # Demo payment may not create order rows — check
    if isinstance(orders, list) and len(orders) == 0:
        r.fail(
            "MAJOR",
            "Order",
            "支付成功后订单列表为空",
            "当前 payment/submit 只写 sys_user_product，不落 sys_order；业务上无法在「我的订单」核对支付记录",
        )

    # 2. Project
    print("\n[3] Project / Access")
    st, payload, _ = request("GET", "/project/list", token=token)
    projects = expect_ok(r, "Project", "list", st, payload) or []
    project_id = None
    if projects:
        # list may be wrapped
        if isinstance(projects, dict):
            projects = projects.get("list") or projects.get("records") or []
        if projects:
            project_id = projects[0].get("project_id") or projects[0].get("projectId")
            r.pass_(f"use existing project_id={project_id}")
    if project_id is None:
        st, payload, _ = request(
            "POST",
            "/project",
            token=token,
            body={
                "projectName": f"E2E项目-{int(time.time())}",
                "complianceType": "SOC2",
                "auditType": "Type2",
                "status": "ACTIVE",
            },
        )
        created = expect_ok(r, "Project", "create", st, payload)
        if created:
            project_id = created.get("project_id") or created.get("projectId")

    if not project_id:
        r.fail("BLOCKER", "Project", "无可用项目", "后续合规流程无法继续")
        _print_summary(r)
        return 1

    st, payload, _ = request("GET", f"/project/{project_id}", token=token)
    detail = expect_ok(r, "Project", "detail", st, payload)
    members = []
    if detail:
        members = detail.get("members") or detail.get("role_slots") or []
        r.pass_(f"project members/slots present={bool(members)}")

    # invitation create
    st, payload, _ = request(
        "POST",
        "/invitation-code/project/create",
        token=token,
        body={"projectId": project_id, "memberRole": "GENERAL_USER", "maxUses": 3, "remark": "e2e"},
    )
    inv = expect_ok(r, "Invitation", "create", st, payload)
    inv_code = None
    if inv:
        inv_code = inv.get("code") or inv.get("invitation_code") or inv.get("invitationCode")
        if inv_code:
            st, payload, _ = request("GET", f"/invitation-code/validate?code={urllib.parse.quote(inv_code)}")
            expect_ok(r, "Invitation", "validate(anonymous)", st, payload)
        st, payload, _ = request("GET", f"/invitation-code/list?projectId={project_id}", token=token)
        expect_ok(r, "Invitation", "list", st, payload)

    # 3. Request Master / Individual
    print("\n[4] Request Master / Individual / Files")
    st, payload, _ = request("GET", f"/request-master/list?projectId={project_id}", token=token)
    masters = expect_ok(r, "RequestMaster", "list", st, payload) or []
    master_id = None
    if masters:
        master_id = masters[0].get("request_master_id") or masters[0].get("requestMasterId") or masters[0].get("id")
    if master_id is None:
        st, payload, _ = request(
            "POST",
            "/request-master",
            token=token,
            body={
                "projectId": project_id,
                "requestMasterName": f"E2E总单-{int(time.time())}",
                "requestMasterStatus": "IN_PROGRESS",
            },
        )
        m = expect_ok(r, "RequestMaster", "create", st, payload)
        if m:
            master_id = (
                m.get("request_master_id")
                or m.get("requestMasterId")
                or m.get("id")
                or (m.get("master") or {}).get("request_master_id")
            )

    st, payload, _ = request("GET", "/request-master/status-options", token=token)
    expect_ok(r, "RequestMaster", "status-options", st, payload)

    st, payload, hdrs = request("GET", "/request-master/template-files/download-template", token=token)
    if st == 200 and isinstance(payload, (bytes, bytearray)) and len(payload) > 1000:
        r.pass_(f"RequestMaster download-template bytes={len(payload)}")
    elif st == 200 and isinstance(payload, dict) and code_of(payload) == 0:
        r.fail("MAJOR", "RequestMaster", "模板下载返回 JSON 而非文件流", str(payload)[:200])
    else:
        if st == 200:
            r.pass_("RequestMaster download-template HTTP 200")
        else:
            r.fail("MAJOR", "RequestMaster", "download-template", f"HTTP {st}")

    if master_id:
        st, payload, _ = request("GET", f"/request-master/{master_id}", token=token)
        expect_ok(r, "RequestMaster", "detail", st, payload)

        st, payload, _ = request(
            "GET", f"/request-master/{master_id}/template-files", token=token
        )
        expect_ok(r, "RequestMaster", "template-files list", st, payload)

        # generate individuals (correct path)
        st, payload, _ = request(
            "POST",
            f"/request/individual/generate?requestMasterId={master_id}",
            token=token,
            body={},
        )
        if st >= 500:
            r.fail("BLOCKER", "RequestIndividual", "generate", f"HTTP {st} {payload}")
        elif code_of(payload) not in (0, None):
            r.fail("MAJOR", "RequestIndividual", "generate", str(payload)[:300])
        else:
            gen = data_of(payload) or []
            r.pass_(f"RequestIndividual generate count={len(gen) if isinstance(gen, list) else gen}")

        st, payload, _ = request(
            "GET", f"/request/individual/list?requestMasterId={master_id}", token=token
        )
        individuals = expect_ok(r, "RequestIndividual", "list", st, payload)
        if isinstance(individuals, dict):
            individuals = (
                individuals.get("list")
                or individuals.get("records")
                or individuals.get("items")
                or []
            )
        individuals = individuals or []
        if not individuals:
            r.fail(
                "MAJOR",
                "RequestIndividual",
                "Generate 后清单为空",
                "业务上应按已购 features 从条款库灌入；空清单会导致后续交材料链路不可用",
            )
        else:
            r.pass_(f"individuals count={len(individuals)}")
            req_id = (
                individuals[0].get("request_id")
                or individuals[0].get("requestId")
                or individuals[0].get("id")
            )
            if req_id:
                st, payload, _ = request("GET", f"/request/individual/{req_id}", token=token)
                expect_ok(r, "RequestIndividual", "detail", st, payload)

                st, payload, _ = request(
                    "GET",
                    f"/request/individual/document-owners?projectId={project_id}",
                    token=token,
                )
                expect_ok(r, "RequestIndividual", "document-owners", st, payload)

                content = b"E2E evidence sample"
                st, payload, _ = request(
                    "POST",
                    f"/request/individual/{req_id}/attachments",
                    token=token,
                    files={"file": ("e2e-evidence.txt", content, "text/plain")},
                )
                expect_ok(r, "RequestIndividual", "upload attachment", st, payload)

                st, payload, _ = request(
                    "POST", f"/request/individual/{req_id}/send", token=token, body={}
                )
                expect_ok(r, "RequestIndividual", "send", st, payload)
    else:
        r.fail("BLOCKER", "RequestMaster", "无 Master", "无法测 Individual")

    # 4. RCM / Control / Gap / Analysis / Risk
    print("\n[5] RCM / Control Testing / Gap / Analysis / Risk")
    st, payload, _ = request("GET", f"/rcm/list?projectId={project_id}", token=token)
    rcm_list = expect_ok(r, "RCM", "list", st, payload)
    st, payload, _ = request("GET", "/rcm/ai/status", token=token)
    ai = expect_ok(r, "RCM", "ai/status", st, payload)
    if isinstance(ai, dict) and ai.get("available") is False:
        r.fail("INFO", "RCM", "Ollama/AI 不可用", str(ai))

    st, payload, _ = request(
        "POST",
        "/rcm",
        token=token,
        body={
            "projectId": project_id,
            "controlCode": f"E2E-CT-{int(time.time()) % 100000}",
            "controlName": "E2E Control",
            "riskDescription": "E2E risk desc",
            "category": "Security",
        },
    )
    rcm = expect_ok(r, "RCM", "create", st, payload)
    rcm_id = None
    if rcm:
        rcm_id = rcm.get("rcm_id") or rcm.get("rcmId") or rcm.get("id")

    st, payload, _ = request("GET", f"/control-testing/list?projectId={project_id}", token=token)
    expect_ok(r, "ControlTesting", "list", st, payload)
    st, payload, _ = request(
        "POST",
        "/control-testing",
        token=token,
        body={
            "projectId": project_id,
            "title": f"E2E Control Test {int(time.time()) % 100000}",
            "description": "e2e control test",
            "resultStatus": "FAIL",
            "riskLevel": "HIGH",
            "riskDescription": "e2e fail risk",
        },
    )
    ct = expect_ok(r, "ControlTesting", "create", st, payload)

    st, payload, _ = request("GET", f"/gap-analysis/list?projectId={project_id}", token=token)
    expect_ok(r, "GapAnalysis", "list", st, payload)
    st, payload, _ = request(
        "POST",
        f"/gap-analysis?projectId={project_id}",
        token=token,
        body={
            "controlTitle": "E2E Gap Control",
            "gapDescription": "created by e2e",
            "gapLevel": "HIGH",
            "remediationYesNo": "NO",
            "remediationSuggestion": "fix it",
        },
    )
    expect_ok(r, "GapAnalysis", "create", st, payload)

    st, payload, _ = request(
        "POST", f"/gap-analysis/regenerate?projectId={project_id}", token=token, body={}
    )
    expect_ok(r, "GapAnalysis", "regenerate", st, payload)

    st, payload, _ = request("GET", f"/analysis/pass-rate/{project_id}", token=token)
    expect_ok(r, "Analysis", "pass-rate", st, payload)
    st, payload, _ = request("GET", f"/analysis/trend/{project_id}", token=token)
    if st == 404:
        st, payload, _ = request(
            "GET", f"/analysis/trend?projectId={project_id}", token=token
        )
    if st < 500:
        if code_of(payload) == 0 or st == 200:
            r.pass_("Analysis trend")
        else:
            r.fail("MINOR", "Analysis", "trend", str(payload)[:200])
    else:
        r.fail("MAJOR", "Analysis", "trend", f"HTTP {st}")

    st, payload, _ = request(
        "GET", f"/risk-table/list?projectId={project_id}&pageNum=1&pageSize=20", token=token
    )
    risk_page = expect_ok(r, "Risk", "list", st, payload)
    total = None
    if isinstance(risk_page, dict):
        total = risk_page.get("total") or risk_page.get("totalCount")
        items = risk_page.get("list") or risk_page.get("records") or risk_page.get("items") or []
    else:
        items = risk_page or []
    if total is not None:
        r.pass_(f"risk total={total} page_items={len(items)}")
        if int(total) == 0:
            r.fail("MAJOR", "Risk", "Risk table 无数据", f"project_id={project_id}；AICPA 种子可能只导入了 project 4")
        elif int(total) > 0:
            # sample first row completeness
            row = items[0] if items else None
            if row and not row.get("cycle_name") and not row.get("cycleName"):
                r.fail(
                    "INFO",
                    "Risk",
                    "Excel 导入后 Cycle/Modules 为空",
                    "符合当前设计（Edit 补全），页面需能接受空值展示",
                )
    st, payload, _ = request(
        "POST",
        "/risk-table",
        token=token,
        body={
            "projectId": project_id,
            "ccCriteria": "CC 9.9",
            "ccCriteriaName": "E2E criteria",
            "pointsOfFocusName": "E2E POF",
            "riskLevel": "MEDIUM",
            "riskSource": "MANUAL",
        },
    )
    created_risk = expect_ok(r, "Risk", "create", st, payload)
    if created_risk:
        rid = created_risk.get("risk_id") or created_risk.get("riskId")
        if rid:
            st, payload, _ = request(
                "PUT",
                f"/risk-table/{rid}",
                token=token,
                body={"cycleName": "FY2026-Q1", "modulesName": "Security", "riskLevel": "HIGH"},
            )
            expect_ok(r, "Risk", "update/edit", st, payload)
            st, payload, _ = request("DELETE", f"/risk-table/{rid}", token=token)
            expect_ok(r, "Risk", "delete", st, payload)

    # 5. Operation log / system users
    print("\n[6] Operation log / System users")
    st, payload, _ = request("GET", f"/operation-log/list?projectId={project_id}", token=token)
    if st >= 400:
        st, payload, _ = request("GET", "/operation-log/list", token=token)
    expect_ok(r, "OperationLog", "list", st, payload)

    # legacy admin / users if present
    for path in ("/admin/users", "/system-user/list", "/user/list", "/legacy/users"):
        st, payload, _ = request("GET", path, token=token)
        if st == 404:
            continue
        if st < 500 and (code_of(payload) == 0 or st == 200):
            r.pass_(f"SystemUsers via {path}")
            break
        if st >= 500:
            r.fail("MAJOR", "SystemUsers", path, f"HTTP {st} {payload}")
            break

    # Doc inconsistency sample
    st, payload, _ = request("GET", "/product/list")
    if st == 404 or (isinstance(payload, dict) and code_of(payload) not in (0, None) and st >= 400):
        r.fail(
            "MINOR",
            "Docs",
            "API_ENDPOINTS.md 与实现不一致",
            "/product/list 文档仍写匿名可用，实现可能已下线；实际购买页用 /product/packages",
        )

    _print_summary(r)
    blockers = [f for f in r.findings if f.severity == "BLOCKER"]
    majors = [f for f in r.findings if f.severity == "MAJOR"]
    return 1 if blockers or majors else 0


def _print_summary(r: Result) -> None:
    print("\n=== SUMMARY ===")
    print(f"PASS: {len(r.ok)}")
    for sev in ("BLOCKER", "MAJOR", "MINOR", "INFO"):
        items = [f for f in r.findings if f.severity == sev]
        print(f"{sev}: {len(items)}")
        for f in items:
            print(f"  - [{f.module}] {f.title}: {f.detail}")


if __name__ == "__main__":
    sys.exit(main())
