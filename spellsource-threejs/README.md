# Spellsource Three.js client

A React/Three.js client using Spellsource's GraphQL API. The website hosts it at
`/game-3js`; `/game-3js?demo=true` runs the offline board. `/game` remains the Unity client.

From the repository root:

```sh
yarn install --immutable
yarn workspace spellsource-threejs codegen
yarn workspace spellsource-threejs typecheck
yarn workspace spellsource-threejs test
yarn workspace spellsource-threejs build
```

The workspace exports `SpellsourceThreeClient`. Supply `graphqlUrl`,
`subscriptionsUrl`, `accessToken` for live play, `mode` (`live` or `demo`), and
`onExit`. The hosting application owns sign-in and routing. Render it only in a
browser; the Next.js wrapper uses a dynamic import with SSR disabled.

GraphQL types are generated from the gateway schema and shared operations. Open
the game subscription before connecting to a match. Action and mulligan replies
must carry the server request ID; never retry an action under a new request ID.
