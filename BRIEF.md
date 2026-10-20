# Brief: Agentic Keycloak Lab

## Why this project exists

This is a **learning project for agentic engineering**. The goal is not the software itself
but to observe, verify and validate how a coding agent — driven by
[mattpocock/skills](https://github.com/mattpocock/skills) — takes an idea from brief to
working, tested code.

The domain is deliberately small; the integration points are deliberately real.

## What we want to build

A minimal distributed system:

- **Frontend (SPA)** — user logs in via Keycloak, sees data from the backend.
- **Keycloak** — identity provider; one realm, imported from JSON at startup.
- **Service A** — public-facing API, protected by JWT. Calls Service B.
- **Service B** — internal API, protected by JWT. Only reachable via Service A.
- **docker-compose** — the whole system starts with one command from a clean clone.

```
Frontend ──(OIDC + PKCE)──▶ Keycloak
   │                           │ JWKS
   ▼ Bearer token              ▼
Service A ──(service-to-service)──▶ Service B
```

## Scope

**In scope**
- Login / logout / token refresh in the frontend
- Two roles (e.g. `user`, `admin`) with at least one admin-only endpoint
- One meaningful call from Service A to Service B
- In-memory data (no database)
- Automated tests, including auth integration tests against a real Keycloak

**Out of scope (for now)**
- Databases, messaging, API gateway, service mesh
- Production deployment, Kubernetes, CI/CD
- User registration flows, social login, MFA

## Open decisions (for the grilling session to resolve)

_Resolved in the grilling session on 2026-10-20: see `GLOSSARY.md`, `docs/adr/` and the spec._

- Business domain for A and B (e.g. Orders → Catalog)? Keep it trivial.
- Backend stack? (Available locally: Java 21, Node 22, Docker.)
- Frontend stack?
- Service-to-service auth: **token relay** (forward user token) vs **client credentials**?
- Keycloak version and how the realm is provisioned
- Test strategy: what is unit-tested vs integration-tested (Testcontainers?)

## Definition of done (Validate against this)

- [ ] `docker compose up` from a clean clone works with no manual steps
- [ ] User can log in, see data, log out; token refresh works
- [ ] Request without token → **401**
- [ ] Valid token, missing role → **403**
- [ ] Expired / tampered token → **401**
- [ ] Service A → Service B call succeeds with the chosen auth method and fails without it
- [ ] Service B is not usable directly by the frontend
- [ ] All tests pass; auth paths are tested, not just happy paths
- [ ] All versions (Keycloak, frameworks, images) are pinned

## Working agreement with the agent

- First ticket is a **walking skeleton** through the riskiest path: a Customer logs in and sees
  the Product list, served by Orders from Catalog via a client-credentials token, end-to-end in
  docker-compose. No ordering yet. Then grow by vertical slices.
- Tests first (`tdd`), one slice at a time.
- Ask, don't assume — especially on auth decisions. Record decisions as ADRs.
- Keycloak config differs between versions: verify against the docs for the pinned version.

## Planned skill workflow

1. `/setup-matt-pocock-skills`
2. `/grill-with-docs` — resolve the open decisions above, build `GLOSSARY.md` + ADRs
3. `/to-spec`
4. `/to-tickets`
5. `/implement` (uses `tdd`, ends with `code-review`) — one ticket at a time
6. `/retro` — after every session

## Observation log

After each step, note:

| Step / skill | What the agent asked | What it assumed | What I corrected | Lesson |
|---|---|---|---|---|
|  |  |  |  |  |
