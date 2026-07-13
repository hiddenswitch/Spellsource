# GraphQL Production Rollout

This checklist covers the production cutover from the legacy gRPC-facing Unity client to the stitched GraphQL gateway.

## Rollout order

1. Merge the main repository changes that expose the Java GraphQL service port, publish the gateway image, and add the gateway readiness endpoint.
2. Confirm CI publishes `ghcr.io/hiddenswitch/spellsource/graphql` with a version newer than `0.10.4`.
3. Confirm Flux deploys the updated Helm chart and the `spellsource` service exposes TCP port `4000` as `graphql`.
4. Merge the private GitOps change that sets `SPELLSOURCE_HOST=spellsource`, `SPELLSOURCE_PORT=4000`, and probes `/readiness`.
5. Wait for the `spellsource-graphql` rollout and require its readiness probe to pass.
6. Run the gateway smoke tests below before publishing a Unity client that points at the gateway.
7. Commit and push the Unity repository changes, update the main repository's Unity submodule pointer, and build release players.

## Gateway smoke tests

- `GET https://spellsource-graphql.appmana.com/` returns HTTP 200.
- `GET https://spellsource-graphql.appmana.com/readiness` returns HTTP 200 only after schema stitching completes.
- Introspection at `/graphql` contains `login`, `createAccount`, `getConfiguration`, matchmaking/game mutations, and all expected subscriptions.
- An incorrect login returns promptly as an error and does not leave the client loading.
- A valid account can log in, restore a saved session, load cards, and load decks.
- Two clients can enter matchmaking, connect to a game, mulligan, exchange game actions, and reconnect.
- WebSocket connections to `/subscriptions` authenticate and deliver `gameMessages`, `matchFound`, `friendUpdated`, `inviteUpdated`, and `editableCardUpdated` events.
- Existing website card, deck, publishing, and rogue operations continue to work through the stitched endpoint.

## Rollback

- Keep the previous gateway image tag available.
- Roll the gateway deployment back independently if stitching or WebSocket forwarding fails; the Java gRPC service remains unchanged.
- Do not release the migrated Unity player until the public stitched schema and subscriptions pass smoke testing.
- Database changes run through the Helm pre-upgrade migration job. Take a database backup before any release that includes new Flyway migrations.

## Follow-up hardening

- Move credentials currently rendered into ConfigMaps or stored directly in GitOps manifests into Kubernetes Secrets or an external secret manager, then rotate them.
- Investigate recurring Redis Sentinel connectivity warnings before increasing production traffic.
- Add realistic CPU/memory requests and limits for the Java server after profiling its current high memory usage.
- Use immutable gateway image tags and ensure every schema-affecting change triggers the gateway image workflow.
