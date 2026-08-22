#!/usr/bin/env python3
import json
from collections import Counter, defaultdict
from pathlib import Path

TARGET_PACKAGES = {
    "com.google.android.youtube": "YouTube",
    "com.google.android.apps.youtube.music": "YouTube Music",
    "com.reddit.frontpage": "Reddit",
    "com.google.android.apps.photos": "Google Photos",
    "com.microblink.photomath": "Photomath",
    "com.instagram.android": "Instagram",
    "com.instagram.barcelona": "Threads",
    "com.strava": "Strava",
    "com.alltrails.alltrails": "AllTrails",
}

root = json.loads(Path("bundles.json").read_text(encoding="utf-8"))
compatibilities = root.get("compatibilities", [])


def package_names(value):
    result = set()
    if isinstance(value, list):
        for child in value:
            result |= package_names(child)
    elif isinstance(value, dict):
        for key, child in value.items():
            if key.count(".") >= 1:
                result.add(key)
            result |= package_names(child)
    elif isinstance(value, str) and value.count(".") >= 1:
        result.add(value)
    return result

items = []
for bundle in root.get("bundles", []):
    source = bundle.get("source", "")
    repo = bundle.get("repo", "")
    for patch in bundle.get("patches", []):
        key = patch.get("compatiblePackagesKey")
        targets = package_names(compatibilities[key]) if isinstance(key, int) and 0 <= key < len(compatibilities) else set()
        supported = sorted(targets & TARGET_PACKAGES.keys())
        if supported:
            items.append({
                "source": source,
                "repo": repo,
                "bundle": bundle.get("name", repo),
                "patch": patch.get("name", ""),
                "description": patch.get("description", ""),
                "targets": supported,
                "options": patch.get("options", []),
                "default": patch.get("default", False),
            })

by_app = defaultdict(list)
for item in items:
    for target in item["targets"]:
        by_app[target].append(item)

lines = [
    "# Runtime catalog compatibility analysis",
    "",
    f"Total bundles: {len(root.get('bundles', []))}",
    f"Patches targeting supported LSPosed apps: {len(items)}",
    "",
]
for package, label in TARGET_PACKAGES.items():
    entries = by_app.get(package, [])
    lines.extend([
        f"## {label} (`{package}`)",
        f"Matching catalog patches: {len(entries)}",
        "",
    ])
    for item in entries:
        lines.extend([
            f"- **{item['patch']}** — `{item['repo']}` ({item['source']})",
            f"  - Description: {item['description'] or 'Not provided'}",
            f"  - Options: {len(item['options'])}; default: {item['default']}",
        ])
    lines.append("")

Path("catalog_analysis.md").write_text("\n".join(lines) + "\n", encoding="utf-8")
print("\n".join(lines))
