const fs = require('fs');

const catalogPath = process.argv[2];
const raw = fs.readFileSync(catalogPath, 'utf8');
const catalog = JSON.parse(raw);

for (const bundle of catalog.bundles ?? []) {
  const matches = (bundle.patches ?? []).filter((patch) =>
    (patch.name ?? '').toLowerCase().includes('ads') &&
    JSON.stringify(patch).toLowerCase().includes('picsart')
  );
  if (matches.length > 0) {
    console.log(JSON.stringify({ source: bundle.source, repo: bundle.repo, patches: matches }, null, 2));
  }
}

for (const bundle of catalog.bundles ?? []) {
  const matches = (bundle.patches ?? []).filter((patch) =>
    (patch.compatiblePackages ?? []).some((entry) =>
      JSON.stringify(entry).toLowerCase().includes('picsart')
    )
  );
  if (matches.length > 0) {
    console.log(JSON.stringify({ source: bundle.source, repo: bundle.repo, patches: matches }, null, 2));
  }
}
