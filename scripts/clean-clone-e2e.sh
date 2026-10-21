#!/usr/bin/env bash
# Definition-of-done check: the committed files alone start the whole system, and the browser journeys pass.
# Copies tracked + unignored files to a temp dir (like a fresh clone, including uncommitted work),
# runs `docker compose up --build` there, runs Playwright, then tears everything down.
# Needs Docker and ports 4200/8080/8081 free. Run from anywhere: ./scripts/clean-clone-e2e.sh
set -euo pipefail
repo="$(cd "$(dirname "$0")/.." && pwd)"
work="$(mktemp -d)"
compose=(docker compose --project-directory "$work" -f "$work/docker-compose.yml")

cleanup() { "${compose[@]}" down --remove-orphans >/dev/null 2>&1 || true; rm -rf "$work"; }
trap cleanup EXIT

(cd "$repo" && git ls-files --cached --others --exclude-standard -z | tar --null -T - -cf -) | tar -xf - -C "$work"

"${compose[@]}" up -d --build --wait --wait-timeout 300

cd "$repo/e2e"
if [[ ! -d node_modules ]]; then npx -y npm@11.6.2 ci; fi
npx playwright install chromium >/dev/null
npx playwright test
