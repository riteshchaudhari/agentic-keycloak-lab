# Coding standards

Read at review time (`code-review`). Mechanical rules live in checks instead: `scripts/check-pins.sh`, Prettier, and the CI workflow. Everything here is a judgement call a reviewer applies to the diff.

## Tests

- **Tests go through a seam.** HTTP API or browser, as agreed in the spec. Assertions cover status, body, Problem Details, outgoing HTTP requests or what's on screen; never internal classes or private state.
- **Fake tokens run through the service's own role mapping.** A `jwt()` test that sets authorities directly proves nothing about how roles are read (see `docs/agents/stack-gotchas.md`).
- **Counts are exact and start from known state.** A test that counts calls (token requests, Catalog requests) resets any shared cache first and asserts an exact number. "At most" or "at least" bounds pass when the behaviour is missing.
- **Every new rule is seen red once.** A new authorization rule or boundary behaviour has a test that was observed failing against code with the rule broken. Mention the check in the PR or ticket.
- **Expected values come from an independent source.** A literal, a worked example, or the spec; never recomputed the way the code computes them.

## Domain language

- **Glossary terms, exactly.** Names, test names, UI text and API fields use `GLOSSARY.md` terms; a word from an _Avoid_ list is a finding.
- **ADR conflicts are explicit.** Code that contradicts an ADR either changes the ADR in the same change or says why in the PR.

## Security

- **The server enforces every rule.** Hiding UI is a convenience; each rule the UI hides has a server-side check and a test at the HTTP seam.
- **Tokens and secrets never reach logs.** No logging of headers, tokens or client secrets, at any level.
- **Lab-only shortcuts are labelled where they live.** A plaintext secret or permissive setting carries a "lab-only" comment or README note beside it.

## Services

- **No shared code between services.** Duplicating a small piece of security config or a test helper is deliberate (it avoids coupling services). Duplication *within* one service is a normal finding.
