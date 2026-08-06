#!/usr/bin/env python3
"""Import AICPA TSC Excel (first 3 columns) into soc_risk seed SQL + optional DB load."""

from __future__ import annotations

import argparse
import re
import subprocess
from pathlib import Path

import openpyxl

ROOT = Path(__file__).resolve().parents[1]
EXCEL = ROOT / (
    "prd/AI documents -20260204T184250Z-3-001/AI documents/Risk Table/"
    "AICPA - mapping-final-2017-tsc-to-extant-2016-tspc.xlsx"
)
OUT_SQL = ROOT / "src/main/resources/sql/012_risk_table_seed_aicpa_tsc.sql"


def norm_cc(ref: object) -> str:
    text = str(ref).strip()
    match = re.match(r"^(CC)\s*(\d+(?:\.\d+)?)$", text, re.I)
    if match:
        return f"{match.group(1).upper()} {match.group(2)}"
    return text


def sql_str(value: str | None) -> str:
    if value is None:
        return "NULL"
    return "'" + str(value).replace("\\", "\\\\").replace("'", "''") + "'"


def parse_rows() -> list[tuple[str, str, str]]:
    workbook = openpyxl.load_workbook(EXCEL, read_only=True, data_only=True)
    sheet = workbook["Sheet1"]
    rows = list(sheet.iter_rows(values_only=True))

    data: list[tuple[str, str, str]] = []
    current_ref: str | None = None
    current_criteria: str | None = None

    for row in rows[3:]:
        ref = row[0] if len(row) > 0 else None
        criteria = row[1] if len(row) > 1 else None
        points = row[2] if len(row) > 2 else None

        # Section titles such as CONTROL ENVIRONMENT
        if ref is None and criteria and (points is None or not str(points).strip()):
            continue

        if ref is not None and str(ref).strip():
            current_ref = norm_cc(ref)
            if criteria is not None and str(criteria).strip():
                current_criteria = str(criteria).strip()
            if points is not None and str(points).strip() and current_ref:
                data.append((current_ref, current_criteria or "", str(points).strip()))
            continue

        if points is not None and str(points).strip() and current_ref:
            data.append((current_ref, current_criteria or "", str(points).strip()))

    return data


def write_sql(data: list[tuple[str, str, str]], project_ids: list[int]) -> None:
    lines: list[str] = [
        "-- Risk table seed from AICPA 2017 TSC mapping Excel (first 3 columns)",
        "-- Col1 TSC Ref. #        -> cc_criteria",
        "-- Col2 Criteria          -> cc_criteria_name",
        "-- Col3 Points of Focus   -> points_of_focus_name",
        "-- Remaining columns are left empty for Edit on Risk table page.",
        "SET NAMES utf8mb4;",
        "TRUNCATE TABLE `soc_risk`;",
        "",
    ]

    for project_id in project_ids:
        lines.append(f"-- project_id={project_id}")
        lines.append(
            "INSERT INTO `soc_risk` "
            "(`project_id`, `cc_criteria`, `cc_criteria_name`, `points_of_focus_name`, "
            "`risk_level`, `risk_source`, `deleted`) VALUES"
        )
        values = [
            f"({project_id}, {sql_str(cc)}, {sql_str(name)}, {sql_str(pof)}, 'MEDIUM', 'UPLOAD', 0)"
            for cc, name, pof in data
        ]
        lines.append(",\n".join(values) + ";")
        lines.append("")

    OUT_SQL.write_text("\n".join(lines), encoding="utf-8")
    print(f"wrote {OUT_SQL} rows={len(data) * len(project_ids)}")


def apply_sql(mysql_args: list[str]) -> None:
    command = ["mysql", *mysql_args]
    with OUT_SQL.open("rb") as handle:
        completed = subprocess.run(command, stdin=handle, check=False, capture_output=True)
    if completed.returncode != 0:
        raise SystemExit(completed.stderr.decode("utf-8", errors="replace"))
    print("applied SQL to database")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--project-id",
        type=int,
        action="append",
        dest="project_ids",
        help="Project id to seed (repeatable). Default: 4",
    )
    parser.add_argument(
        "--apply",
        action="store_true",
        help="Also execute the generated SQL against local MySQL",
    )
    parser.add_argument("--mysql-user", default="root")
    parser.add_argument("--mysql-password", default="123456")
    parser.add_argument("--mysql-db", default="ai_shenji_v2")
    args = parser.parse_args()

    project_ids = args.project_ids or [4]
    data = parse_rows()
    if not data:
        raise SystemExit("no rows parsed from excel")
    write_sql(data, project_ids)

    if args.apply:
        apply_sql(
            [
                f"-u{args.mysql_user}",
                f"-p{args.mysql_password}",
                args.mysql_db,
            ]
        )


if __name__ == "__main__":
    main()
