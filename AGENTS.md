# Agentic Keycloak Lab

See `BRIEF.md` for intent, scope and definition of done.

- **Run, test, toolchain caveats** (npm 11, Docker for tests): `README.md`.
- **Before debugging Spring, Keycloak, Angular OIDC or test setup**: `docs/agents/stack-gotchas.md`.
- **Guardrails** CI runs on every PR: `scripts/check-pins.sh`, `scripts/clean-clone-e2e.sh`, `.github/workflows/ci.yml`.

## Agent skills

### Issue tracker

GitHub Issues via the `gh` CLI. See `docs/agents/issue-tracker.md`.

### Triage labels

Default five canonical roles (`needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`). See `docs/agents/triage-labels.md`.

### Domain docs

Single-context: `GLOSSARY.md` + `docs/adr/` at the repo root. See `docs/agents/domain.md`.
