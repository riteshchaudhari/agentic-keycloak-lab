# Agentic Keycloak Lab

A small shop (Customers order Products from a Catalog, Admins oversee) secured end to end with Keycloak. It exists to practise agentic engineering: see `BRIEF.md`, the spec in issue #1 and the tickets under it.

## Run it

```sh
docker compose up --build
```

Open http://localhost:4200 and log in as **alice / alice** (Customer). Other seed accounts are in `keycloak/README.md`. Keycloak's admin console is at http://localhost:8080 (admin / admin). All credentials are lab-only.

| Part | Where | Notes |
|---|---|---|
| Frontend (Angular 21, nginx) | `frontend/` | :4200, proxies `/api/` to Orders |
| Orders (Spring Boot 4.1) | `services/orders/` | :8081 |
| Catalog (Spring Boot 4.1) | `services/catalog/` | no host port: only Orders reaches it |
| Keycloak 26.8.1, realm `shop` | `keycloak/` | :8080, imported fresh on every start |

## Test it

```sh
(cd services/catalog && ./mvnw verify)   # needs Docker (real Keycloak via Testcontainers)
(cd services/orders && ./mvnw verify)
(cd e2e && npm ci && npx playwright install chromium && npx playwright test)   # needs the system running
```

## Develop the frontend live

```sh
docker compose up -d keycloak catalog orders
cd frontend && npm ci && npx ng serve   # http://localhost:4200, proxies /api/ to :8081
```

Use npm 11 (`npx -y npm@11.6.2 ci`) if your npm is 10.9.x: it crashes resolving this dependency tree.
