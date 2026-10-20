---
status: accepted
---

# Browser holds tokens as a public OIDC client (Authorization Code + PKCE)

The frontend is a public OIDC client: it logs in via Authorization Code + PKCE, holds the access token in the browser and sends it as a `Bearer` header to Orders. This deliberately deviates from current guidance for browser apps (IETF OAuth Browser-Based Apps BCP), which prefers a backend-for-frontend so tokens never reach the browser. We chose it because this is a learning lab and the visible token flow is what we want to study; it is not a production recommendation.

## Considered Options

- **Backend-for-frontend (BFF)**: tokens stay server-side, browser gets a session cookie. More secure, but adds a third backend component and hides the token flow. Candidate for a later "harden it" ticket.
