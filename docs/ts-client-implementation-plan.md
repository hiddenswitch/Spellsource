# TypeScript gRPC Client Implementation Plan

Rewrite of the Unity C# `DefaultClient` in TypeScript for `spellsource-web/src/game/`. Reference: [grpc-client-architecture.md](./grpc-client-architecture.md).

---

## Folder Structure

```
src/game/
├── client/                     # gRPC client layer
│   ├── grpc-transport.ts       # Channel/transport setup, metadata, keepalive
│   ├── auth-client.ts          # Login, register, token verify, token storage
│   ├── matchmaking-client.ts   # Enqueue bidi stream, cancel, list queues
│   ├── game-client.ts          # SubscribeGame bidi stream, send/receive messages
│   ├── collection-client.ts    # Decks CRUD, cards catalogue, drafts
│   ├── social-client.ts        # Friends, invites, chat
│   ├── client.ts               # Facade combining all sub-clients (the "DefaultClient")
│   └── token-store.ts          # localStorage persistence for auth token
│
├── state/                      # Client-side game state management
│   ├── game-state-manager.ts   # Processes ServerToClientMessage into renderable state
│   ├── action-resolver.ts      # Maps player interactions to ClientToServerMessage
│   └── entity-adapter.ts       # Converts proto Entity to renderer-friendly format
│
├── renderer/                   # Three.js rendering (existing files, reorganized)
│   ├── game-engine.ts
│   ├── game-scene.tsx
│   ├── board-layout.ts
│   ├── mesh-factory.ts
│   ├── input-handler.ts
│   └── constants.ts
│
├── types.ts                    # Shared types bridging client ↔ state ↔ renderer
└── index.ts                    # Barrel exports
```

The current `src/game/*.ts` renderer files move into `src/game/renderer/`. The prototype `types.ts` is replaced with one that uses the real proto-generated types.

---

## Phases

### Phase 1: Foundation — Transport, Auth, Token Storage

**Goal**: Connect to the server, authenticate, persist tokens. Fully testable without a running server via mocked gRPC stubs.

**Dependencies to install**:
- `@improbable-eng/grpc-web` — gRPC-Web client
- `browser-headers` — used by generated stubs
- `rxjs` — reactive streams (used by generated stubs and throughout client)
- `protobufjs` — protobuf runtime (used by generated stubs)
- `long` — 64-bit integer support (used by generated stubs)

**Files**:

1. **`client/token-store.ts`**
   - `saveToken(reply: LoginOrCreateReply): void` — persist to localStorage
   - `loadToken(): LoginOrCreateReply | null` — read from localStorage
   - `clearToken(): void` — delete
   - Simple wrapper, easily mockable

2. **`client/grpc-transport.ts`**
   - `createTransport(endpoint: string): grpc.TransportFactory` — configure grpc-web transport
   - `createMetadata(token: string): BrowserHeaders` — `Authorization: Bearer {token}`
   - `DEFAULT_ENDPOINT` constant
   - Encapsulates transport config so other clients don't touch grpc internals directly

3. **`client/auth-client.ts`**
   - `login(email: string, password: string): Promise<LoginOrCreateReply>`
   - `createAccount(email: string, username: string, password: string): Promise<LoginOrCreateReply>`
   - `verifyToken(token: AccessTokenResponse): Promise<boolean>`
   - `getConfiguration(): Promise<ClientConfiguration>`
   - Uses `Unauthenticated` service stubs
   - On success, calls `tokenStore.saveToken()`

**Tests** (`src/__tests__/game/`):

- `token-store.test.ts` — save/load/clear with mocked localStorage
- `auth-client.test.ts` — mock gRPC unary calls, verify correct metadata, token persistence on success, error propagation on failure

---

### Phase 2: Collection — Cards, Decks, Drafts

**Goal**: Load card catalogue, manage decks. These are all unary RPCs, straightforward to implement and test.

**Files**:

4. **`client/collection-client.ts`**
   - `getCards(etag?: string): Promise<GetCardsResponse>` — with ETag caching
   - `getDecks(): Promise<DecksGetAllResponse>`
   - `getDeck(id: string): Promise<DecksGetResponse>`
   - `createDeck(req: DecksPutRequest): Promise<DecksPutResponse>`
   - `updateDeck(req: DecksUpdateRequest): Promise<DecksGetResponse>`
   - `deleteDeck(id: string): Promise<void>`
   - `duplicateDeck(id: string): Promise<DecksGetResponse>`
   - Draft methods: `getDraft`, `startDraft`, `chooseHero`, `chooseCard`
   - All require auth token from `grpc-transport.ts`

**Tests**:

- `collection-client.test.ts` — mock unary RPCs, verify request shapes, ETag header passthrough, error handling

---

### Phase 3: Matchmaking — Bidi Stream

**Goal**: Queue for a game and receive match assignment. First streaming implementation.

**Files**:

5. **`client/matchmaking-client.ts`**
   - `enqueue(deckId: string, queueId: string, botDeckId?: string): Observable<MatchmakingQueuePutResponse>` — returns observable that emits match results
   - `cancelMatchmaking(): void` — sends `{ cancel: true }` on the request stream then completes
   - `getQueues(): Promise<MatchmakingQueuesResponse>`
   - Internally manages the bidi stream lifecycle
   - Filters responses to only emit when `unityConnection` is present
   - Exposes `isMatchmaking: boolean` state

**Tests**:

- `matchmaking-client.test.ts` — mock bidi stream, verify:
  - Request message shape sent on enqueue
  - Cancel message sent on cancel
  - Observable emits only valid responses (non-null unityConnection)
  - Observable completes on stream end
  - Error propagation

---

### Phase 4: Game Client — Core Bidi Stream

**Goal**: Implement `SubscribeGame` — the main game communication channel.

**Files**:

6. **`client/game-client.ts`**
   - `startGame(firstMessage: ClientToServerMessage_FirstMessageMessage): void` — opens bidi stream, sends first message
   - `sendAction(actionIndex: number, repliesTo: string): void` — send game action
   - `sendMulligan(discardedIndices: number[], repliesTo: string): void` — send mulligan choice
   - `sendEmote(emote: Emote): void`
   - `messages$: Observable<ServerToClientMessage>` — stream of incoming messages
   - `disconnect(): void` — close stream
   - Reconnection logic: on `Unavailable`, re-open stream and re-send first message
   - Internal `Subject` pair mirroring C# pattern:
     - `outgoing$` — subject for client→server messages
     - `incoming$` — subject for server→client messages

**Tests**:

- `game-client.test.ts` — mock bidi stream, verify:
  - First message sent correctly on startGame
  - Actions/mulligan formatted correctly
  - Incoming messages relayed to observable
  - Reconnect behavior on stream error
  - Clean disconnect

---

### Phase 5: State Manager — Proto→Renderer Bridge

**Goal**: Process `ServerToClientMessage` stream into a format the Three.js renderer can consume. This replaces the prototype `types.ts`.

**Files**:

7. **`types.ts`** (rewrite)
   - `RendererState` — the shape the renderer consumes (derived from proto `GameState`/`Entity`)
   - `PlayerSide` = `'top' | 'bottom'`
   - `RendererEntity` — renderer-friendly entity (position, stats, visual state)
   - `RendererHand`, `RendererBattlefield`, `RendererPlayer`
   - `AvailableAction` — simplified action for UI targeting
   - Keep imports from proto types, re-export what the renderer needs

8. **`state/entity-adapter.ts`**
   - `adaptEntity(proto: Entity, localPlayerId: number): RendererEntity` — maps the 73-field proto Entity to the renderer's simplified format
   - `determineSide(entity: Entity, localPlayerId: number): PlayerSide`
   - `groupEntities(entities: Entity[], localPlayerId: number): { bottom: GroupedEntities, top: GroupedEntities }` — partition entities by zone and player

9. **`state/action-resolver.ts`**
   - `resolveActions(actions: GameActions): AvailableAction[]` — flatten SpellAction tree into targetable actions
   - `buildActionMessage(actionIndex: number, repliesTo: string): ClientToServerMessage`
   - `buildMulliganMessage(discardedIndices: number[], repliesTo: string): ClientToServerMessage`

10. **`state/game-state-manager.ts`**
    - Subscribes to `gameClient.messages$`
    - Maintains current `RendererState`
    - Processes each `ServerToClientMessage` by `messageType`:
      - `ON_UPDATE` → apply entity changes, update state
      - `ON_REQUEST_ACTION` → update available actions
      - `ON_MULLIGAN` → set mulligan options
      - `ON_GAME_EVENT` → queue visual event for renderer
      - `ON_GAME_END` → set game over state
    - Exposes `state$: Observable<RendererState>` for the renderer to subscribe to
    - Exposes `availableActions$: Observable<AvailableAction[]>`

**Tests**:

- `entity-adapter.test.ts` — given proto Entity fixtures, verify correct RendererEntity output, side determination, zone grouping
- `action-resolver.test.ts` — given GameActions fixtures, verify flattened action list, correct message building
- `game-state-manager.test.ts` — feed sequence of ServerToClientMessage, verify RendererState transitions:
  - Initial game state populates correctly
  - Entity updates applied incrementally
  - Mulligan phase sets starting cards
  - Game over sets final state
  - Action request updates available actions

---

### Phase 6: Renderer Integration

**Goal**: Wire the state manager into the Three.js renderer. Move existing renderer files, replace prototype types.

**Changes**:

11. **Move renderer files** into `src/game/renderer/`
    - `board-layout.ts`, `constants.ts`, `game-engine.ts`, `game-scene.tsx`, `input-handler.ts`, `mesh-factory.ts`

12. **Update `renderer/game-engine.ts`**
    - Accept `RendererState` instead of the old `GameState`
    - Map `RendererEntity` fields to mesh creation
    - Handle `AvailableAction[]` for highlighting valid targets

13. **Update `renderer/game-scene.tsx`**
    - Subscribe to `gameStateManager.state$`
    - On card click → resolve to action index → `gameClient.sendAction()`
    - On mulligan selection → `gameClient.sendMulligan()`
    - On end turn → find END_TURN action → send it

14. **Update `src/pages/game.tsx`**
    - Initialize `Client` facade
    - Auth flow (login/register or restore token)
    - Matchmaking flow
    - Pass state manager to `GameScene`

**Tests**:

- `renderer-integration.test.ts` — verify `GameEngine` accepts `RendererState` and doesn't throw, mock canvas context

---

### Phase 7: Client Facade & Social

**Goal**: Tie everything together in a single entry point. Add social features.

**Files**:

15. **`client/social-client.ts`**
    - `subscribeFriends(): Observable<Friend>`
    - `subscribeInvites(): Observable<Invite>`
    - `sendInvite(req: PostInviteRequest): Observable<InviteResponse>`
    - `acceptInvite(id: string): Promise<AcceptInviteResponse>`
    - `deleteFriend(id: string): Promise<void>`
    - `addFriend(id: string): Promise<FriendPutResponse>`

16. **`client/client.ts`** (facade)
    - Composes: `AuthClient`, `CollectionClient`, `MatchmakingClient`, `GameClient`, `SocialClient`
    - `connect(endpoint?: string): void` — set up transport
    - `login(...)` / `register(...)` / `restoreSession(): Promise<boolean>`
    - `isAuthenticated$: Observable<boolean>`
    - `disconnect(): void` — tear down all streams and transport
    - Single entry point for the page to use

**Tests**:

- `client-facade.test.ts` — verify lifecycle: connect → auth → matchmake → game → disconnect

---

## Dependency Graph

```
Phase 1: token-store → grpc-transport → auth-client
Phase 2: auth-client → collection-client
Phase 3: auth-client → matchmaking-client
Phase 4: auth-client → game-client
Phase 5: game-client → entity-adapter → game-state-manager
                        action-resolver ↗
Phase 6: game-state-manager → renderer (game-engine, game-scene)
Phase 7: all clients → client facade
```

Each phase only depends on previous phases. Tests at each phase verify the layer in isolation with mocks for the layer below.

---

## Test Strategy

**Framework**: Jest with ts-jest (already configured).

**Test directory**: `src/__tests__/game/` — mirrors `src/game/` structure.

**Mocking approach**:
- gRPC calls: mock the generated service client methods
- Streams: mock with RxJS `Subject` to manually push messages
- localStorage: jest mock (`jest.spyOn(Storage.prototype, ...)`)
- Three.js: mock canvas/WebGL context for renderer tests

**Test file naming**: `{module-name}.test.ts`

**Fixtures**: `src/__tests__/game/fixtures/` — reusable proto message fixtures:
- `entities.ts` — sample Entity objects for different card types
- `server-messages.ts` — sample ServerToClientMessage for each messageType
- `game-actions.ts` — sample GameActions with various action types

---

## Package Dependencies to Add

```bash
yarn add @improbable-eng/grpc-web browser-headers rxjs protobufjs long
```

These are required by the generated proto stubs in `spellsource-protos/build/dist/experimental/client/`.

---

## Proto Stub Integration

The generated stubs live in `spellsource-protos/build/dist/experimental/client/`. Options:

1. **Import directly via relative path** — simplest, but fragile path
2. **Yarn workspace symlink** — add `spellsource-protos` as a workspace dependency pointing at `build/dist/experimental/client`
3. **Copy into `src/game/proto/`** — decouple from build artifact location

Recommendation: **option 2** (workspace dependency) if the monorepo workspace already covers it, otherwise **option 3** with a copy task. The existing `copyAndModifyProto` + `compileTypescript` pipeline in `spellsource-protos/build.gradle` already produces a `dist/` output — wire that as the package entry point.

---

## Implementation Order Summary

| Phase | Files | Tests | Can verify with |
|-------|-------|-------|-----------------|
| 1 | token-store, grpc-transport, auth-client | 2 test files | `yarn jest --testPathPattern game` |
| 2 | collection-client | 1 test file | Same |
| 3 | matchmaking-client | 1 test file | Same |
| 4 | game-client | 1 test file | Same |
| 5 | types, entity-adapter, action-resolver, game-state-manager | 3 test files | Same |
| 6 | Move renderer, update engine/scene/page | 1 test file | `yarn dev` → `/game` page |
| 7 | social-client, client facade | 2 test files | Full integration |
