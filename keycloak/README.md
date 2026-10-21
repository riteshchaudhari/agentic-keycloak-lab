# Keycloak realm `shop`

`shop-realm.json` is imported fresh on every start (in compose and in the services' real-Keycloak tests).

**Lab-only:** the client secret and the passwords below are committed in plain text on purpose so a clean clone works. Never reuse this pattern outside the lab.

| Account | Password | Roles |
|---|---|---|
| alice | alice | customer |
| bob | bob | customer |
| ada | ada | admin |
| Keycloak admin console (master realm) | admin / admin | — |
