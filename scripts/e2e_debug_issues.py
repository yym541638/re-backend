#!/usr/bin/env python3
import json, urllib.request, urllib.error

BASE = "http://localhost:8089/api"

def call(method, path, token=None, body=None, file_bytes=None, filename="e2e.pdf"):
    headers = {"Accept": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    data = None
    if file_bytes is not None:
        boundary = "----B"
        headers["Content-Type"] = f"multipart/form-data; boundary={boundary}"
        data = (
            f"--{boundary}\r\nContent-Disposition: form-data; name=\"file\"; filename=\"{filename}\"\r\n"
            f"Content-Type: application/pdf\r\n\r\n"
        ).encode() + file_bytes + f"\r\n--{boundary}--\r\n".encode()
    elif body is not None:
        headers["Content-Type"] = "application/json"
        data = json.dumps(body).encode()
    req = urllib.request.Request(BASE + path, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=60) as resp:
            raw = resp.read().decode()
            return resp.status, json.loads(raw or "null")
    except urllib.error.HTTPError as e:
        raw = e.read().decode("utf-8", "replace")
        try:
            return e.code, json.loads(raw)
        except Exception:
            return e.code, raw

st, login = call("POST", "/auth/login", body={"account": "83261582@qq.com", "password": "Test@123456"})
token = login["data"]["token"]
print("ORDER", call("GET", "/order/my", token=token))

st, ms = call("GET", "/request-master/list?projectId=4", token=token)
m0 = ms["data"][0]
mid = m0.get("request_master_id") or m0.get("requestMasterId")
print("MASTER", mid, list(m0.keys())[:12])
st, inds = call("GET", f"/request/individual/list?requestMasterId={mid}", token=token)
i0 = inds["data"][0]
rid = i0.get("request_id") or i0.get("requestId")
print("RID", rid)
pdf = b"%PDF-1.1\n1 0 obj<<>>endobj\ntrailer<<>>\n%%EOF\n"
print("UPLOAD", call("POST", f"/request/individual/{rid}/attachments", token=token, file_bytes=pdf))

# members put with same membership
st, det = call("GET", "/project/4", token=token)
print("PROJECT_DETAIL_CODE", det.get("code"), "keys", list((det.get("data") or {}).keys())[:20])
