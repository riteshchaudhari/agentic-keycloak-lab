---
status: accepted
---

# Tokens are issued for localhost:8080; services fetch keys from keycloak:8080

Browsers reach Keycloak at `http://localhost:8080`, containers at `http://keycloak:8080`, yet the `iss` claim must be identical for both. We pin Keycloak's frontend hostname to `http://localhost:8080` (`KC_HOSTNAME`) with a dynamic backchannel (`KC_HOSTNAME_BACKCHANNEL_DYNAMIC=true`). Each service validates `iss` against `http://localhost:8080/realms/<realm>` but fetches signing keys from `http://keycloak:8080/realms/<realm>/protocol/openid-connect/certs`. The mismatched URLs in service config are deliberate: don't "fix" them to match.

## Considered Options

- **`/etc/hosts` entry** mapping `keycloak` to `127.0.0.1` on every developer machine: works, but adds a manual step and breaks "clean clone just works".
- **`host.docker.internal`**: platform-dependent, and keycloak-js needs a secure context (HTTPS or `localhost`), so browsers can't use it over plain HTTP.
- **Disabling issuer validation**: removes a real security check.

## Consequences

- Unverified: that client-credentials tokens Orders requests via `keycloak:8080` still carry `iss=http://localhost:8080/...`. The walking-skeleton ticket must prove it.
