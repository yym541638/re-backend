# -*- coding: utf-8 -*-
"""Import request criteria catalog via mysql client (file stdin, supports multiline)."""
import subprocess
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SQL_FILE = ROOT / "src/main/resources/sql/009_request_criteria_catalog.sql"
DB = "ai_shenji_v2"
USER = "root"
PASSWORD = "123456"


def run_sql_file(path: Path) -> str:
    cmd = [
        "mysql",
        f"-u{USER}",
        f"-p{PASSWORD}",
        f"-D{DB}",
        "--default-character-set=utf8mb4",
    ]
    with path.open("rb") as f:
        result = subprocess.run(cmd, stdin=f, capture_output=True)
    out = (result.stdout or b"").decode("utf-8", errors="replace")
    err = (result.stderr or b"").decode("utf-8", errors="replace")
    # mysql prints password warning to stderr
    if result.returncode != 0:
        raise RuntimeError(err or out or f"mysql exit {result.returncode}")
    return out + err


def main() -> None:
    if not SQL_FILE.exists():
        raise FileNotFoundError(SQL_FILE)

    # Ensure ALTER on soc_request won't fail if table missing: skip via temp patch? Keep original.
    print(f"Importing {SQL_FILE} ...")
    msg = run_sql_file(SQL_FILE)
    if msg.strip():
        print(msg.strip())

    verify = """
SELECT module_name, COUNT(*) AS cnt
FROM soc_request_criteria_catalog
WHERE deleted = 0
GROUP BY module_name
ORDER BY module_name;
SELECT COUNT(*) AS total FROM soc_request_criteria_catalog WHERE deleted = 0;
"""
    with tempfile.NamedTemporaryFile("w", suffix=".sql", delete=False, encoding="utf-8") as tf:
        tf.write(verify)
        verify_path = Path(tf.name)
    try:
        print(run_sql_file(verify_path))
    finally:
        verify_path.unlink(missing_ok=True)
    print("Import done.")


if __name__ == "__main__":
    main()
