#!/usr/bin/env bash
# Fails if any version in the repo floats. Every image, framework, library and tool is pinned exactly
# (BRIEF.md definition of done). Run from anywhere: ./scripts/check-pins.sh
set -euo pipefail
cd "$(dirname "$0")/.."

failures=()
fail() { failures+=("$1"); }

# 1. Container images: Dockerfile FROM lines, BuildKit syntax lines, compose image: lines.
#    An exact tag contains at least two dots in its version (e.g. 26.8.1, 21.0.12_8-jre-noble).
image_refs() {
  # "file:line:FROM image [AS stage]": the image is always the second field.
  grep -HnE '^\s*FROM\s+' $(git ls-files '*Dockerfile') | awk '{sub(/FROM$/, "", $1); print $1" "$2}'
  grep -HnE '^# syntax=' $(git ls-files '*Dockerfile') | sed -E 's/# syntax=/ /'
  grep -HnE '^\s*image:\s*' $(git ls-files 'docker-compose*.yml' 'compose*.yml') | sed -E 's/image:\s*/ /'
}
while read -r where ref; do
  tag="${ref##*:}"
  if [[ "$ref" != *:* || "$tag" == "latest" || ! "$tag" =~ ^[0-9]+\.[0-9]+\.[0-9]+ ]]; then
    fail "$where floating image: $ref"
  fi
done < <(image_refs)

# 2. npm: every dependency in every package.json is an exact version (no ^, ~, *, x, ranges, tags).
for pkg in $(git ls-files '*package.json'); do
  while read -r line; do fail "$pkg floating npm version: $line"; done < <(
    python3 - "$pkg" <<'EOF'
import json, re, sys
d = json.load(open(sys.argv[1]))
for section in ("dependencies", "devDependencies", "peerDependencies", "optionalDependencies"):
    for name, version in d.get(section, {}).items():
        if not re.fullmatch(r"\d+\.\d+\.\d+(-[0-9A-Za-z.-]+)?", version):
            print(f"{name}@{version}")
if "packageManager" in d and not re.fullmatch(r"[a-z]+@\d+\.\d+\.\d+", d["packageManager"]):
    print(f"packageManager={d['packageManager']}")
EOF
  )
done

# 3. Maven: explicit <version> values are exact; no ranges, LATEST/RELEASE or SNAPSHOT dependencies.
for pom in $(git ls-files '*pom.xml'); do
  while read -r line; do fail "$pom floating Maven version: $line"; done < <(
    grep -nE '<version>\s*(\[|\(|LATEST|RELEASE)|<version>[^<]*,[^<]*</version>' "$pom" || true
  )
done

# 4. Maven wrapper distribution is an exact version.
for props in $(git ls-files '*maven-wrapper.properties'); do
  grep -qE 'apache-maven-[0-9]+\.[0-9]+\.[0-9]+-bin' "$props" || fail "$props Maven wrapper version not exact"
done

if ((${#failures[@]})); then
  printf 'Unpinned versions found:\n' >&2
  printf '  %s\n' "${failures[@]}" >&2
  exit 1
fi
echo "All versions pinned."
