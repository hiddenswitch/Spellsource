# Spellsource gRPC Client Architecture

Reference for reimplementing the Unity C# client in TypeScript. Covers all gRPC services, message types, streaming patterns, authentication, and connection lifecycle.

## Source Files

| Component | Path |
|-----------|------|
| Main client | `spellsource-client/src/unity/Assets/Scripts/DefaultClient.cs` |
| Stream writer wrapper | `spellsource-client/src/unity/Assets/Scripts/ComponentModel/CancellableGrpcRequestStream.cs` |
| Stream reader observable | `spellsource-client/src/unity/Assets/Scripts/ComponentModel/GrpcStreamObservable.cs` |
| Stream subscription | `spellsource-client/src/unity/Assets/Scripts/ComponentModel/GrpcStreamSubscription.cs` |
| Proto: game + matchmaking | `spellsource-protos/src/main/proto/spellsource.proto` |
| Proto: auth + accounts | `spellsource-protos/src/main/proto/hiddenswitch.proto` |
| Generated TS (client) | `spellsource-protos/build/generated/source/proto/main/ts-client/` |
| Generated TS (server) | `spellsource-protos/build/generated/source/proto/main/ts-server/` |

---

## gRPC Services

### Unauthenticated (no token required)

| RPC | Type | Request | Response | Purpose |
|-----|------|---------|----------|---------|
| `CreateAccount` | Unary | `CreateAccountRequest` | `LoginOrCreateReply` | Register new user |
| `Login` | Unary | `LoginRequest` | `LoginOrCreateReply` | Login existing user |
| `VerifyToken` | Unary | `AccessTokenResponse` | `BoolValue` | Validate cached token |
| `GetConfiguration` | Unary | `Empty` | `ClientConfiguration` | Retrieve client config (GraphQL URL, etc.) |

### UnauthenticatedCards (no token required)

| RPC | Type | Request | Response | Purpose |
|-----|------|---------|----------|---------|
| `GetCards` | Unary | `GetCardsRequest` | `GetCardsResponse` | Full card catalogue with ETag caching |

### Accounts (authenticated)

| RPC | Type | Request | Response | Purpose |
|-----|------|---------|----------|---------|
| `GetAccount` | Unary | `Empty` | `GetAccountsReply` | Current user details |
| `GetAccounts` | Unary | `GetAccountsRequest` | `GetAccountsReply` | Lookup other users |
| `ChangePassword` | Unary | `ChangePasswordRequest` | `LoginOrCreateReply` | Password change |

### Games (authenticated)

| RPC | Type | Request | Response | Purpose |
|-----|------|---------|----------|---------|
| `IsInMatch` | Unary | `Empty` | `StringValue` | Returns game ID if in active match |

### Matchmaking (authenticated)

| RPC | Type | Request | Response | Purpose |
|-----|------|---------|----------|---------|
| `Enqueue` | **Bidi stream** | `stream MatchmakingQueuePutRequest` | `stream MatchmakingQueuePutResponse` | Queue for game, receive match result |
| `MatchmakingDelete` | Unary | `Empty` | `MatchCancelResponse` | Cancel matchmaking |
| `MatchmakingGet` | Unary | `Empty` | `MatchmakingQueuesResponse` | List available queues |

### HiddenSwitchSpellsourceAPIService (authenticated)

**Core game streaming:**

| RPC | Type | Request | Response | Purpose |
|-----|------|---------|----------|---------|
| `SubscribeGame` | **Bidi stream** | `stream ClientToServerMessage` | `stream ServerToClientMessage` | Main game loop |

**Reactive subscriptions (server streaming):**

| RPC | Type | Response stream | Purpose |
|-----|------|-----------------|---------|
| `SubscribeFriends` | Server stream | `Friend` | Live friends list |
| `SubscribeInvites` | Server stream | `Invite` | Incoming game invites |
| `SubscribeEditableCards` | Server stream | `EditableCard` | User's custom cards |
| `SubscribeMatch` | Server stream | `Match` | Match state changes |
| `PostInvite` | Server stream | `InviteResponse` | Send invite, stream status |

**Deck management (unary):**

| RPC | Request | Response |
|-----|---------|----------|
| `DecksGetAll` | `Empty` | `DecksGetAllResponse` |
| `DecksGet` | `DecksGetRequest` | `DecksGetResponse` |
| `DecksPut` | `DecksPutRequest` | `DecksPutResponse` |
| `DecksUpdate` | `DecksUpdateRequest` | `DecksGetResponse` |
| `DecksDelete` | `DecksDeleteRequest` | `Empty` |
| `DuplicateDeck` | `StringValue` | `DecksGetResponse` |

**Draft (unary):**

| RPC | Request | Response |
|-----|---------|----------|
| `DraftsGet` | `Empty` | `DraftState` |
| `DraftsPost` | `DraftsPostRequest` | `DraftState` |
| `DraftsChooseHero` | `DraftsChooseHeroRequest` | `DraftState` |
| `DraftsChooseCard` | `DraftsChooseCardRequest` | `DraftState` |

**Social (unary):**

| RPC | Request | Response |
|-----|---------|----------|
| `FriendPut` | `FriendPutRequest` | `FriendPutResponse` |
| `FriendDelete` | `FriendDeleteRequest` | `UnfriendResponse` |
| `AcceptInvite` | `AcceptInviteRequest` | `AcceptInviteResponse` |
| `DeleteInvite` | `DeleteInviteRequest` | `InviteResponse` |

**Card editing (unary):**

| RPC | Request | Response |
|-----|---------|----------|
| `PutCard` | `PutCardMessage` | `PutCardMessage` (result) |
| `DeleteCard` | `DeleteCardMessage` | `RemovedMessage` |

---

## Key Message Types

### Authentication

```proto
message LoginRequest {
    string usernameOrEmail = 1;
    string password = 2;
}

message CreateAccountRequest {
    string email = 1;
    string username = 2;
    string password = 3;
    bool decks = 4;    // auto-create starter decks
    bool guest = 5;    // guest account
}

message LoginOrCreateReply {
    AccessTokenResponse accessTokenResponse = 1;
    UserEntity userEntity = 2;
}

message AccessTokenResponse {
    string token = 1;
}

message UserEntity {
    string id = 1;
    string email = 2;
    string username = 10;
    string privacyToken = 11;
}
```

### Game Communication

```proto
message ClientToServerMessage {
    message FirstMessageMessage {
        string playerKey = 1;     // auth for game connection
        string playerSecret = 2;  // server-signed secret
    }

    int32 actionIndex = 1;                  // index of chosen action
    repeated int32 discardedCardIndices = 2; // mulligan discards
    Emote emote = 3;
    optional int32 entityTouch = 4;
    optional int32 entityUntouch = 5;
    FirstMessageMessage firstMessage = 6;
    MessageTypeMessage.MessageType messageType = 7;
    string repliesTo = 8;                   // ID of server message being replied to
}

message ServerToClientMessage {
    GameActions actions = 1;               // available actions for player
    EntityChangeSet changes = 2;           // entity updates
    Emote emote = 3;
    GameEvent event = 4;
    GameOver gameOver = 5;
    GameState gameState = 6;
    string id = 7;                         // message ID for repliesTo
    bool isReplayMessage = 8;
    int32 localPlayerId = 9;
    MessageTypeMessage.MessageType messageType = 10;
    repeated Entity startingCards = 11;    // mulligan options
    Timers timers = 12;
}
```

### MessageType enum

Used in both client and server messages:

- `ON_UPDATE` — game state update from server
- `ON_GAME_EVENT` — visual game event (damage, destroy, trigger, etc.)
- `ON_GAME_END` — game over
- `ON_MULLIGAN` — mulligan phase
- `ON_REQUEST_ACTION` — server requests player pick an action
- `ON_EMOTE` — emote from player
- `ON_TOUCH` / `ON_UNTOUCH` — entity hover/unhover
- `UPDATE_ACTION` — client sends chosen action
- `UPDATE_MULLIGAN` — client sends mulligan choices
- `FIRST_MESSAGE` — client sends initial connection handshake

### Game State

```proto
message GameState {
    repeated Entity entities = 1;
    bool isLocalPlayerTurn = 2;
    repeated GameEvent powerHistory = 3;
    int64 timestamp = 4;
    int32 turnNumber = 5;
    string turnState = 6;
    bool hasPowerHistory = 7;
}

message GameActions {
    repeated SpellAction all = 1;
    repeated int32 compatibility = 2;   // indices of valid actions
}

message SpellAction {
    int32 action = 1;                   // action index
    ActionTypeMessage.ActionType actionType = 2;
    repeated SpellAction choices = 3;   // choose-one sub-options
    string description = 4;
    Entity entity = 5;
    int32 sourceId = 6;
    repeated TargetActionPair targetKeyToActions = 7;
    string request = 8;
}

message TargetActionPair {
    int32 action = 1;
    int32 friendlyBattlefieldIndex = 2;
    int32 target = 3;
}
```

### Entity (73+ fields)

Core entity representation covering all game objects (heroes, minions, spells, weapons, hero powers). Key fields:

```
id, name, cardId, cardType, description, art
attack, baseAttack, hp, baseHp, maxHp, armor
manaCost, baseManaCost, mana, maxMana, overload, lockedMana
boardPosition, location, owner
destroyed, playable, summoningSickness, cannotAttack, frozen
battlecry, deathrattles, taunt, charge, rush, stealth
divineShield, deflect, lifesteal, poisonous, windfury, combo
chooseOne, silenced, gold, tooltips, heroClasses, tribes, rarity
```

### Matchmaking

```proto
message MatchmakingQueuePutRequest {
    string botDeckId = 1;    // bot opponent deck (for solo play)
    string deckId = 2;       // player's deck
    string queueId = 3;
    bool cancel = 4;         // send true to cancel
}

message MatchmakingQueuePutResponse {
    MatchmakingQueuePutRequest retry = 1;
    MatchmakingQueuePutResponseUnityConnection unityConnection = 2;
}

message MatchmakingQueuePutResponseUnityConnection {
    ClientToServerMessage firstMessage = 1;  // pre-built first message to send
    string url = 2;
    string gameId = 3;
}
```

---

## Connection Lifecycle

### 1. Channel Setup

```
Endpoint: https://spellsource-api-v0.appmana.com:443 (default)
Protocol: gRPC over HTTP/2
TLS: SslCredentials for https:// or h2:// endpoints, Insecure otherwise
```

Channel options:
- `grpc.keepalive_time_ms`: 400
- `grpc.keepalive_timeout_ms`: 8000
- `grpc.http2.max_pings_without_data`: 0
- `grpc.keepalive_permit_without_calls`: 1
- `grpc.max_receive_message_length`: -1 (unlimited)
- `grpc.max_send_message_length`: -1 (unlimited)

### 2. Endpoint Change

When the endpoint changes:
1. Gracefully shut down existing channel (`channel.ShutdownAsync()`)
2. Create new `Channel` with credentials and options
3. Instantiate fresh service clients from the channel
4. Call `channel.ConnectAsync()` to establish connection
5. Fire `serverConnectionChanged` event when ready

### 3. Authentication Flow

```
1. Check disk for cached account (account.pb)
2. If found → VerifyToken(cachedToken)
   - Valid → use token, proceed
   - Invalid → HandleLoginFailed() → clear all data, require re-login
3. If not found → prompt user for Login or CreateAccount
4. On success → persist LoginOrCreateReply to disk
5. Token used as: Authorization: Bearer {token} in gRPC metadata
```

### 4. Post-Auth Initialization

After authentication:
1. `GetCards()` with ETag caching (card catalogue)
2. `DecksGetAll()` (user's decks)
3. `GetAccount()` (user details)
4. Open reactive subscriptions: `SubscribeFriends()`, `SubscribeInvites()`, `SubscribeEditableCards()`

---

## Streaming Patterns

### Bidirectional: SubscribeGame

The core game loop. This is the most critical stream.

**Client side uses two reactive subjects:**
- `m_ClientToServerMessages` — outgoing message queue (Subject)
- `m_ServerToClientMessages` — incoming message relay (Subject)

**Flow:**

```
1. Call SubscribeGame(callOptions with cancellationToken)
   → returns bidirectional stream (requestStream, responseStream)

2. Wrap requestStream in CancellableGrpcRequestStream
   → subscribes to m_ClientToServerMessages Subject
   → each OnNext writes to the gRPC request stream

3. Read loop on responseStream:
   while (responseStream.MoveNext(cancellationToken)):
     push message to m_ServerToClientMessages Subject

4. UI/game logic subscribes to m_ServerToClientMessages
```

**Sending a game action:**

```
SendGameMessage(ClientToServerMessage {
    messageType: UPDATE_ACTION,
    actionIndex: <chosen action index>,
    repliesTo: <server message id>
})
→ pushes to m_ClientToServerMessages Subject
→ CancellableGrpcRequestStream writes to gRPC stream
```

**Starting a game:**

```
StartGame() sends:
ClientToServerMessage {
    messageType: FIRST_MESSAGE,
    firstMessage: {
        playerKey: <from matchmaking response>,
        playerSecret: <from matchmaking response>
    }
}
```

### Bidirectional: Matchmaking.Enqueue

```
1. Send MatchmakingQueuePutRequest { deckId, queueId, botDeckId }
2. Listen for MatchmakingQueuePutResponse
3. Filter for responses where unityConnection != null
4. Extract firstMessage from unityConnection → use to start game
5. To cancel: send MatchmakingQueuePutRequest { cancel: true }
```

### CancellableGrpcRequestStream wrapper

Wraps an `IClientStreamWriter<T>` with:
- Subscribes to a source observable
- On each item: `streamWriter.WriteAsync(item)`
- On complete: `streamWriter.CompleteAsync()`
- Accepts a `cancelMessage` — when disposed, writes the cancel message before completing
- Tracks `IsComplete` state to avoid writing after completion

### GrpcStreamSubscription wrapper

Wraps an `IAsyncStreamReader<T>` with:
- Reads in a loop: `while (await reader.MoveNext(token))`
- Switches to main thread before calling `observer.OnNext(reader.Current)`
- Catches `StatusCode.NotFound` and `StatusCode.Cancelled` → break (graceful end)
- Catches `OperationCanceledException` → break
- Other exceptions → `observer.OnError(e)`
- On loop end → `observer.OnCompleted()`

---

## Error Handling

### RPC Exception Handling

| StatusCode | Behavior |
|------------|----------|
| `Cancelled` | Graceful — stream was intentionally cancelled |
| `NotFound` | Graceful — resource gone, end stream |
| `Unavailable` | Set `needsGameRefresh = true`, reconnect |
| Other | Propagate as error to observers |

### Game Reconnection

When a game stream drops with `Unavailable`:
1. Set `needsGameRefresh = true`
2. Wait for channel to reconnect (keepalive/state monitoring)
3. Send synthetic `OnUpdate` message with empty changes to trigger UI refresh
4. Call `StartGame()` to re-send `FirstMessage` and re-sync state

### Token Invalidation

If `VerifyToken` returns false:
1. Clear all reactive collections (decks, friends, invites)
2. Delete `account.pb` from disk
3. Force re-login flow

---

## Data Persistence

| Data | Format | Path |
|------|--------|------|
| Account/token | Protobuf binary | `{persistentData}/account.pb` |
| Card catalogue | Protobuf binary | `{persistentData}/cards.pb` |

Cards use ETag caching: `GetCardsRequest.ifNoneMatch` set to the stored version. Server returns `cachedOk = true` if unchanged.

---

## Metadata / Authentication Header

All authenticated RPCs include this gRPC metadata:

```
Authorization: Bearer {accessTokenResponse.token}
```

Set via `CallOptions(new Metadata { { "Authorization", "Bearer " + token } })`.

---

## TypeScript Implementation Notes

When reimplementing in TypeScript:

1. **gRPC transport**: Use `grpc-web` for browser (the ts-client codegen already targets `grpc-web` via `outputClientImpl=grpc-web`). The generated client stubs are in `spellsource-protos/build/dist/experimental/client/`.

2. **Reactive model**: The C# client uses UniRx (ReactiveX). In TS, use RxJS or a simpler event emitter pattern. The key reactive streams to replicate:
   - `m_ClientToServerMessages` → Subject for outgoing game messages
   - `m_ServerToClientMessages` → Subject for incoming game messages
   - `m_Account` → ReactiveProperty for auth state
   - Channel state observable

3. **Bidirectional streaming in grpc-web**: grpc-web does not support true bidirectional streaming. The `Envelope` message type in the proto wraps game messages for WebSocket transport as an alternative. The `MatchmakingQueuePutResponseUnityConnection.url` field provides a WebSocket URL for game connections.

4. **Token persistence**: Use `localStorage` or `IndexedDB` instead of file system.

5. **Main thread concern**: Not applicable in browser JS (single-threaded). No need for the `SwitchToMainThread()` pattern.

6. **Optimistic updates**: The C# client applies deck modifications locally before server confirmation, then merges the authoritative response. Replicate this for responsive UI.
