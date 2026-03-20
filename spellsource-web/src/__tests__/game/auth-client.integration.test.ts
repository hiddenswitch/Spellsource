/**
 * Integration tests for AuthClient against the real Spellsource server.
 *
 * These tests require network access to the Spellsource API.
 * Run with: yarn jest --testPathPattern auth-client.integration
 *
 * Set SPELLSOURCE_ENDPOINT to override the default server.
 * Set SPELLSOURCE_TEST_EMAIL and SPELLSOURCE_TEST_PASSWORD to use an existing account.
 */
import { AuthClient } from '../../game/client/auth-client'
import { createTokenStore } from '../../game/client/token-store'

// Skip these tests in CI or when no network is available
const ENDPOINT = process.env.SPELLSOURCE_ENDPOINT ?? 'https://spellsource-api-v0.appmana.com:443'
const RUN_INTEGRATION = process.env.SPELLSOURCE_INTEGRATION === 'true'

function mockStorage(): Storage {
  const store = new Map<string, string>()
  return {
    getItem: (key: string) => store.get(key) ?? null,
    setItem: (key: string, value: string) => { store.set(key, value) },
    removeItem: (key: string) => { store.delete(key) },
    clear: () => store.clear(),
    get length() { return store.size },
    key: (index: number) => [...store.keys()][index] ?? null,
  }
}

const describeIntegration = RUN_INTEGRATION ? describe : describe.skip

describeIntegration('AuthClient (integration)', () => {
  let client: AuthClient
  const testEmail = process.env.SPELLSOURCE_TEST_EMAIL
  const testPassword = process.env.SPELLSOURCE_TEST_PASSWORD

  beforeEach(() => {
    client = new AuthClient({
      endpoint: ENDPOINT,
      tokenStore: createTokenStore(mockStorage()),
    })
  })

  it('gets server configuration', async () => {
    const config = await client.getConfiguration()
    expect(config).toBeDefined()
  }, 15000)

  it('creates a guest account', async () => {
    const uniqueEmail = `test-${Date.now()}@integration-test.example.com`
    const reply = await client.createAccount(
      uniqueEmail,
      `testuser_${Date.now()}`,
      'testpassword123',
      { guest: true }
    )

    expect(reply.accessTokenResponse?.token).toBeTruthy()
    expect(reply.userEntity?.id).toBeTruthy()
    expect(client.isAuthenticated()).toBe(true)
  }, 15000)

  if (testEmail && testPassword) {
    it('logs in with existing credentials', async () => {
      const reply = await client.login(testEmail, testPassword)

      expect(reply.accessTokenResponse?.token).toBeTruthy()
      expect(client.isAuthenticated()).toBe(true)
    }, 15000)

    it('verifies a valid token', async () => {
      await client.login(testEmail, testPassword)
      const token = client.getToken()!

      const valid = await client.verifyToken(token)
      expect(valid).toBe(true)
    }, 15000)

    it('restores a session from stored token', async () => {
      // Login first to populate the store
      await client.login(testEmail, testPassword)

      // Create a new client with the same store
      const storage = mockStorage()
      const store = createTokenStore(storage)
      const loginReply = await client.login(testEmail, testPassword)
      store.save(loginReply)

      const client2 = new AuthClient({
        endpoint: ENDPOINT,
        tokenStore: store,
      })

      const restored = await client2.restoreSession()
      expect(restored).not.toBeNull()
      expect(client2.isAuthenticated()).toBe(true)
    }, 15000)
  }

  it('rejects login with bad credentials', async () => {
    await expect(
      client.login('nonexistent@fake.example.com', 'wrongpassword')
    ).rejects.toThrow()

    expect(client.isAuthenticated()).toBe(false)
  }, 15000)

  it('verifyToken returns false for an invalid token', async () => {
    const valid = await client.verifyToken('definitely-not-a-real-token')
    expect(valid).toBe(false)
  }, 15000)
})
