#!/usr/bin/env python3
import json, urllib.request, urllib.error

BASE = "http://localhost:8089/api"

def call(method, path, token=None, body=None, file_bytes=None, filename="e2e.txt", ctype="text/plain"):
    headers = {"Accept": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    data = None
    if file_bytes is not None:
        boundary = "----B"
        headers["Content-Type"] = f"multipart/form-data; boundary={boundary}"
        data = (
            f"--{boundary}\r\nContent-Disposition: form-data; name=\"file\"; filename=\"{filename}\"\r\n"
            f"Content-Type: {ctype}\r\n\r\n"
        ).encode() + file_bytes + f"\r\n--{boundary}--\r\n".encode()
    elif body is not None:
        headers["Content-Type"] = "application/json"
        data = json.dumps(body).encode()
    req = urllib.request.Request(BASE + path, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=60) as resp:
            return resp.status, json.loads(resp.read().decode() or "null")
    except urllib.error.HTTPError as e:
        raw = e.read().decode("utf-8", "replace")
        try:
            return e.code, json.loads(raw)
        except Exception:
            return e.code, raw

_, login = call("POST", "/auth/login", body={"account": "83261582@qq.com", "password": "Test@123456"})
token = login["data"]["token"]

print("1 order/my before pay", call("GET", "/order/my", token=token)[1].get("code"),
      "count", len(call("GET", "/order/my", token=token)[1].get("data") or []))

print("2 payment submit", call("POST", "/payment/submit", token=token, body={
    "productId": 3, "type_switch": True,
    "selectFeatures": "Security,Availability,Privacy,Processing Integrity,Confidentiality",
    "paymentMethod": "Paypal"
})[1].get("code"))

st, orders = call("GET", "/order/my", token=token)
print("3 order/my after pay", orders.get("code"), "count", len(orders.get("data") or []),
      "latest", (orders.get("data") or [{}])[0].get("order_no"), (orders.get("data") or [{}])[0].get("status"))

print("4 company users no param", call("GET", "/project/company/users", token=token)[1].get("code"),
      "n", len(call("GET", "/project/company/users", token=token)[1].get("data") or []))

# force a known missing required param path - invitation validate needs code; use a fake required
# MissingServletRequestParameter: projectId on risk list
st, miss = call("GET", "/risk-table/list", token=token)
print("5 missing projectId", miss.get("code"), miss.get("message"))

_, ms = call("GET", "/request-master/list?projectId=4", token=token)
mid = ms["data"][0]["request_master_id"]
_, inds = call("GET", f"/request/individual/list?requestMasterId={mid}", token=token)
rid = inds["data"][0]["request_id"]
print("6 txt upload", call("POST", f"/request/individual/{rid}/attachments", token=token,
                           file_bytes=b"hello evidence", filename="note.txt")[1].get("code"))
print("7 png upload", call("POST", f"/request/individual/{rid}/attachments", token=token,
                           file_bytes=b"\x89PNG\r\n\x1a\n", filename="shot.png", ctype="image/png")[1].get("code"))
