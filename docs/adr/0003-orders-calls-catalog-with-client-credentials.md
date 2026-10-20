---
status: accepted
---

# Orders calls Catalog with its own client-credentials token, not the Customer's

Orders authenticates to Catalog as itself (OIDC client-credentials grant on its own confidential client), and Catalog accepts only tokens that carry the right to read the Catalog, which no Customer or Admin has. We chose this over relaying the Customer's token because relay would let any logged-in Customer call Catalog directly, breaking the rule that only Orders may use Catalog. Catalog also has no host port, so the boundary holds at both the network and the token level.

## Considered Options

- **Token relay** (forward the Customer's access token): simpler and carries the Customer's identity downstream, but Catalog doesn't need that identity and would accept any Customer's token.

## Consequences

- Orders serves the Product list to the frontend by fetching it from Catalog; the frontend never talks to Catalog.
- Orders holds a client secret, which must come from the environment, never from source code.
