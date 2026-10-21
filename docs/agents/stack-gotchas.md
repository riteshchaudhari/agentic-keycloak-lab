# Stack gotchas

Facts about this stack that no config file confesses. Each one cost real debugging time once.

## Spring Boot 4.1 / Spring Security 7.1

- **`RestClient.Builder` lives in its own starter.** Without `spring-boot-starter-restclient` there is no `RestClient.Builder` bean. Boot 4 also renamed `starter-web` → `starter-webmvc` and `starter-oauth2-resource-server` → `starter-security-oauth2-resource-server`.
- **Keycloak realm roles map by config, no converter.** Both services use:
  `authorities-claim-expressions=['realm_access']?.get('roles') ?: {}` with `authority-prefix=ROLE_`. The expression is SpEL over the claims map.
- **Mutually exclusive properties.** `authorities-claim-expressions` cannot be combined with `authorities-claim-name` or `authorities-claim-delimiter`: startup fails.
- **Issuer and JWKS URLs differ on purpose** (ADR-0002). With both set, Boot fetches keys from `jwk-set-uri` and only checks `iss` against `issuer-uri`.
- **Jackson 3.** Packages are `tools.jackson.*`, e.g. `tools.jackson.databind.json.JsonMapper`.

## Testing

- **`jwt()` skips the service's role mapping.** `SecurityMockMvcRequestPostProcessors.jwt()` sets authorities directly. Route them through the app's mapping, or tests prove nothing about `realm_access.roles`:
  `jwt().jwt(t -> t.claim("realm_access", ...)).authorities(t -> roleMapping.convert(t).getAuthorities())` with an injected `JwtAuthenticationConverter roleMapping`.
- **The Orders service token is cached across tests.** One Spring context is shared, so its `OAuth2AuthorizedClientService` keeps the client-credentials token between tests. Reset it before counting token requests: `removeAuthorizedClient("catalog", "orders-service")`.
- **Customer tokens in real-Keycloak tests** come from a password-grant client the test creates through the admin API (`KeycloakTokens`). The committed realm never allows the password grant.
- **The realm file reaches tests as a test resource.** Each `pom.xml` adds `../../keycloak/shop-realm.json` to `testResources`, so tests and compose import the same file.

## Keycloak 26.8

- **No curl in the image.** The compose healthcheck probes `/health/ready` on management port 9000 with bash `/dev/tcp`.
- **Import skips existing realms.** Keycloak has no volume on purpose, so realm edits take effect on every start.
- **Client-credentials tokens carry no `aud` claim.** Add an audience mapper before ever enabling `jwt.audiences`.
- **Redirect URIs are exact.** `shop-frontend` allows exactly `http://localhost:4200` (login and post-logout). A new frontend route that needs its own callback URL needs a realm change.

## Angular 21 + angular-auth-oidc-client 22

- **A guard on `/` hides the login button.** `autoLoginPartialRoutesGuard` on the default route redirects to Keycloak before the logged-out view renders. Guard only routes that are never the landing page.
- **Refresh tokens without `offline_access`.** Keycloak issues refresh tokens for normal sessions; `disableRefreshTokenOfflineAccessScopeWarning: true` silences the library's warning without requesting offline sessions.

## Toolchain

- **npm 10.9.x crashes** resolving the frontend tree (`Cannot read properties of null (reading 'edgesOut')`). Use `npx -y npm@11.6.2 <command>`.
- **Mutation checks must assert the build ran.** When you break code on purpose to prove a test can fail, check for test failures, not just "no green": a mutant that doesn't compile also produces no passing tests.
