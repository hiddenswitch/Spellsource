/**
 * @jest-environment node
 */

/**
 * Integration tests against a running Spellsource server.
 *
 * These tests use @grpc/grpc-js to talk native gRPC (h2c) to the server,
 * exercising auth, cards, decks, and matchmaking.
 *
 * Run with:
 *   SPELLSOURCE_ENDPOINT=192.168.0.69:8081 npx jest --testPathPattern integration --no-coverage --forceExit
 */
import { ChannelCredentials, Metadata } from '@grpc/grpc-js'
import {
  UnauthenticatedClient,
  UnauthenticatedCardsClient,
  AccountsClient,
} from 'spellsource-protos/dist/experimental/server/hiddenswitch'
import type {
  UnauthenticatedClient as UnauthenticatedClientType,
  UnauthenticatedCardsClient as UnauthenticatedCardsClientType,
  AccountsClient as AccountsClientType,
} from 'spellsource-protos/dist/experimental/server/hiddenswitch'
import {
  HiddenSwitchSpellsourceAPIServiceClient,
  MatchmakingClient,
} from 'spellsource-protos/dist/experimental/server/spellsource'
import type {
  HiddenSwitchSpellsourceAPIServiceClient as ApiClientType,
  MatchmakingClient as MatchmakingClientType,
} from 'spellsource-protos/dist/experimental/server/spellsource'

jest.setTimeout(30000)

const ENDPOINT = process.env.SPELLSOURCE_ENDPOINT ?? '192.168.0.69:8081'
const creds = ChannelCredentials.createInsecure()

/** Promisify a grpc-js unary call */
function unary<Req, Res>(
  client: any,
  method: string,
  request: Req,
  metadata?: Metadata
): Promise<Res> {
  return new Promise((resolve, reject) => {
    const args: any[] = [request]
    if (metadata) args.push(metadata)
    args.push((err: any, res: Res) => {
      if (err) reject(err)
      else resolve(res)
    })
    ;(client as any)[method](...args)
  })
}

function authMetadata(token: string): Metadata {
  const meta = new Metadata()
  meta.set('authorization', `Bearer ${token}`)
  return meta
}

describe('Integration: Spellsource server', () => {
  let unauthenticated: UnauthenticatedClientType
  let cardsClient: UnauthenticatedCardsClientType
  let accountsClient: AccountsClientType
  let apiClient: ApiClientType
  let matchmakingClient: MatchmakingClientType

  // Shared auth state — populated in beforeAll
  let token: string
  let userId: string
  let username: string

  beforeAll(async () => {
    unauthenticated = new UnauthenticatedClient(ENDPOINT, creds)
    cardsClient = new UnauthenticatedCardsClient(ENDPOINT, creds)
    accountsClient = new AccountsClient(ENDPOINT, creds)
    apiClient = new HiddenSwitchSpellsourceAPIServiceClient(ENDPOINT, creds)
    matchmakingClient = new MatchmakingClient(ENDPOINT, creds)

    // Create a test account for all authenticated tests
    username = `test_${Date.now()}`
    const email = `${username}@integration.test`

    const reply = await unary<any, any>(unauthenticated, 'createAccount', {
      email,
      username,
      password: 'testpass123',
      decks: true,
      guest: false,
    })

    token = reply.accessTokenResponse!.token
    userId = reply.userEntity!.id
  })

  afterAll(() => {
    unauthenticated.close()
    cardsClient.close()
    accountsClient.close()
    apiClient.close()
    matchmakingClient.close()
  })

  // ── Auth ──────────────────────────────────────────────────

  describe('auth', () => {
    it('gets server configuration', async () => {
      const config = await unary(unauthenticated, 'getConfiguration', {})
      expect(config).toBeDefined()
    })

    it('has a valid token from account creation', () => {
      expect(token).toBeTruthy()
      expect(userId).toBeTruthy()
    })

    it('verifies the token', async () => {
      const result = await unary<any, any>(unauthenticated, 'verifyToken', { token })
      expect(result).toBeDefined()
    })

    it('logs in with the created account', async () => {
      const reply = await unary<any, any>(unauthenticated, 'login', {
        usernameOrEmail: username,
        password: 'testpass123',
      })

      expect(reply.accessTokenResponse?.token).toBeTruthy()
    })

    it('rejects login with wrong password', async () => {
      await expect(
        unary(unauthenticated, 'login', {
          usernameOrEmail: username,
          password: 'wrongpassword',
        })
      ).rejects.toThrow()
    })
  })

  // ── Cards ─────────────────────────────────────────────────

  describe('cards', () => {
    it('gets the card catalogue', async () => {
      const response = await unary<any, any>(cardsClient, 'getCards', {
        IfNoneMatch: '',
        userId: '',
      })

      expect(response).toBeDefined()
      if (response.content?.cards) {
        expect(response.content.cards.length).toBeGreaterThan(0)
      }
      expect(response.version).toBeDefined()
    })
  })

  // ── Account ───────────────────────────────────────────────

  describe('account', () => {
    it('gets the current account', async () => {
      const meta = authMetadata(token)
      const reply = await unary<any, any>(accountsClient, 'getAccount', {}, meta)
      expect(reply).toBeDefined()
    })
  })

  // ── Decks ─────────────────────────────────────────────────

  describe('decks', () => {
    it('gets all decks (should have starter decks)', async () => {
      const meta = authMetadata(token)
      const reply = await unary<any, any>(apiClient, 'decksGetAll', {}, meta)

      expect(reply).toBeDefined()
      expect(reply.decks).toBeDefined()
    })

    it('creates and deletes a deck', async () => {
      const meta = authMetadata(token)

      // Create
      const createReply = await unary<any, any>(apiClient, 'decksPut', {
        name: 'Integration Test Deck',
        heroClass: 'ANY',
        deckList: '',
        format: '',
        inventoryIds: [],
        cardIds: [],
      }, meta)

      expect(createReply).toBeDefined()
      expect(createReply.deckId).toBeTruthy()

      // Get the created deck
      const getReply = await unary<any, any>(apiClient, 'decksGet', {
        deckId: createReply.deckId,
      }, meta)
      expect(getReply).toBeDefined()

      // Delete
      await unary(apiClient, 'decksDelete', { deckId: createReply.deckId }, meta)
    })
  })

  // ── Matchmaking ───────────────────────────────────────────

  describe('matchmaking', () => {
    it('gets available queues', async () => {
      const meta = authMetadata(token)
      const reply = await unary<any, any>(matchmakingClient, 'matchmakingGet', {}, meta)

      expect(reply).toBeDefined()
      expect(reply.queues).toBeDefined()
    })
  })
})
