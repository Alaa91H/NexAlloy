#!/usr/bin/env python3
from pathlib import Path

include_terms = ("hide", "disable", "remove", "block", "open", "sanitize", "allow", "anti", "prevent")
exclude_terms = ("premium", "pro", "plus", "unlock", "subscription", "vip", "membership", "license", "bypass", "package name")

lines = Path("priority_catalog_analysis.md").read_text(encoding="utf-8").splitlines()
current = None
results = []
for index, line in enumerate(lines):
    if line.startswith("## "):
        current = line[3:]
    elif line.startswith("- **") and " — `" in line:
        name = line.split("**", 2)[1]
        repo = line.split("`", 2)[1]
        desc = lines[index + 2].strip("  - ") if index + 2 < len(lines) else ""
        text = f"{name} {desc}".lower()
        if any(term in text for term in include_terms) and not any(term in text for term in exclude_terms):
            results.append((current, name, repo, desc))

output = ["# Safe-priority conversion candidates", ""]
for app, name, repo, desc in results:
    output.extend([f"## {app}", f"- **{name}** — `{repo}`", f"  - {desc}", ""])
Path("safe_priority_candidates.md").write_text("\n".join(output), encoding="utf-8")
print("\n".join(output))
