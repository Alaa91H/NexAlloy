#!/usr/bin/env python3
import json
from collections import defaultdict
from pathlib import Path

PRIORITY = {
    "Telegram": {"org.telegram.messenger", "org.thunderdog.challegram"},
    "TeraBox": {"com.dubox.drive"},
    "Duolingo": {"com.duolingo"},
    "Facebook": {"com.facebook.katana", "com.facebook.lite"},
    "Google Phone": {"com.google.android.dialer"},
    "Instagram": {"com.instagram.android"},
    "Messenger": {"com.facebook.orca"},
    "PicsArt": {"com.picsart.studio"},
    "Proton VPN": {"ch.protonvpn.android"},
    "SD Maid SE": {"eu.darken.myperm"},
    "Samsung Keyboard": {"com.samsung.android.honeyboard"},
    "SnapTube": {"com.snaptube.premium"},
    "Adobe Acrobat": {"com.adobe.reader"},
    "Adobe Scan": {"com.adobe.scan.android"},
    "Busuu": {"com.busuu.android.enc"},
    "CamScanner": {"com.cambyte.okenscan"},
    "Gboard": {"com.google.android.inputmethod.latin"},
    "TikTok": {"com.zhiliaoapp.musically", "com.ss.android.ugc.trill"},
    "Truecaller": {"com.truecaller"},
    "Xodo": {"com.xodo.pdf.reader"},
    "X": {"com.twitter.android"},
}

root = json.loads(Path("bundles.json").read_text(encoding="utf-8"))
compat = root.get("compatibilities", [])

def packages(value):
    if isinstance(value, str):
        return {value} if value.count(".") >= 1 else set()
    if isinstance(value, list):
        return set().union(*(packages(x) for x in value)) if value else set()
    if isinstance(value, dict):
        found = set()
        for key, child in value.items():
            found |= packages(key)
            found |= packages(child)
        return found
    return set()

hits = defaultdict(list)
for bundle in root.get("bundles", []):
    for patch in bundle.get("patches", []):
        key = patch.get("compatiblePackagesKey")
        targets = packages(compat[key]) if isinstance(key, int) and 0 <= key < len(compat) else set()
        for label, expected in PRIORITY.items():
            matched = targets & expected
            if matched:
                hits[label].append({
                    "repo": bundle.get("repo", ""),
                    "source": bundle.get("source", ""),
                    "name": patch.get("name", ""),
                    "description": patch.get("description", "") or "",
                    "packages": sorted(matched),
                    "options": len(patch.get("options", [])),
                    "default": patch.get("default", False),
                })

lines = ["# Priority Runtime catalog analysis", ""]
for label in PRIORITY:
    entries = sorted(hits[label], key=lambda entry: (entry["repo"], entry["name"].lower()))
    lines.extend([f"## {label}", f"Matches: {len(entries)}", ""])
    for entry in entries:
        lines.extend([
            f"- **{entry['name']}** — `{entry['repo']}`",
            f"  - Packages: {', '.join(entry['packages'])}; options: {entry['options']}; default: {entry['default']}",
            f"  - {entry['description'] or 'No description'}",
        ])
    lines.append("")

Path("priority_catalog_analysis.md").write_text("\n".join(lines) + "\n", encoding="utf-8")
print("\n".join(lines))
