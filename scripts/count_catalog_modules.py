# -*- coding: utf-8 -*-
import re
from collections import Counter
from pathlib import Path

text = Path("src/main/resources/sql/009_request_criteria_catalog.sql").read_text(encoding="utf-8")
mods = re.findall(r"\('([^']*)',\s*'([^']*)',", text)
print("modules", dict(Counter(m[1] for m in mods)))
print("total", len(mods))
