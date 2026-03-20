import { AuthClient } from '../../game/client/auth-client'
import { createTokenStore, type TokenStore } from '../../game/client/token-store'
import type { LoginOrCreateReply } from 'spellsource-protos/dist/experimental/client/hiddenswitch'

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

const sampleReply: LoginOrCreateReply = {
  accessTokenResponse: { token: 'test-token-123' },
  userEntity: { id: 'user-1', email: 'test@example.com', username: 'testuser', privacyToken: '' },
}

// Mock the UnauthenticatedClientImpl
jest.mock('spellsource-protos/dist/experimental/client/hiddenswitch', () => {
  const actual = jest.requireActual('spellsource-protos/dist/experimental/client/hiddenswitch')
  return {
    ...actual,
    UnauthenticatedClientImpl: jest.fn().mockImplementation(() => ({
      login: jest.fn(),
      createAccount: jest.fn(),
      verifyToken: jest.fn(),
      getConfiguration: jest.fn(),
    })),
  }
})

// Get the mock constructor to access instances
import { UnauthenticatedClientImpl } from 'spellsource-protos/dist/experimental/client/hiddenswitch'
const MockUnauthenticated = UnauthenticatedClientImpl as jest.MockedClass<typeof UnauthenticatedClientImpl>

describe('AuthClient (unit)', () => {
  let tokenStore: TokenStore
  let client: AuthClient

  beforeEach(() => {
    MockUnauthenticated.mockClear()
    tokenStore = createTokenStore(mockStorage())
    client = new AuthClient({ tokenStore })
  })

  function getMockInstance() {
    return MockUnauthenticated.mock.results[0].value
  }

  describe('login', () => {
    it('calls unauthenticated.login and stores the token', async () => {
      const mock = getMockInstance()
      mock.login.mockResolvedValue(sampleReply)

      const result = await client.login('test@example.com', 'password123')

      expect(mock.login).toHaveBeenCalledWith({
        usernameOrEmail: 'test@example.com',
        password: 'password123',
      })
      expect(result).toEqual(sampleReply)
      expect(client.isAuthenticated()).toBe(true)
      expect(client.getToken()).toBe('test-token-123')
      expect(tokenStore.load()).toEqual(sampleReply)
    })

    it('propagates errors from the rpc call', async () => {
      const mock = getMockInstance()
      mock.login.mockRejectedValue(new Error('Invalid credentials'))

      await expect(client.login('bad@example.com', 'wrong')).rejects.toThrow('Invalid credentials')
      expect(client.isAuthenticated()).toBe(false)
    })
  })

  describe('createAccount', () => {
    it('calls unauthenticated.createAccount with defaults and stores the token', async () => {
      const mock = getMockInstance()
      mock.createAccount.mockResolvedValue(sampleReply)

      const result = await client.createAccount('test@example.com', 'testuser', 'pass123')

      expect(mock.createAccount).toHaveBeenCalledWith({
        email: 'test@example.com',
        username: 'testuser',
        password: 'pass123',
        decks: true,
        guest: false,
      })
      expect(result).toEqual(sampleReply)
      expect(client.isAuthenticated()).toBe(true)
    })
  })

  describe('restoreSession', () => {
    it('returns the stored reply when the token is valid', async () => {
      const mock = getMockInstance()
      mock.verifyToken.mockResolvedValue({ value: true })
      tokenStore.save(sampleReply)

      const result = await client.restoreSession()

      expect(result).toEqual(sampleReply)
      expect(client.isAuthenticated()).toBe(true)
    })

    it('clears and returns null when the token is invalid', async () => {
      const mock = getMockInstance()
      mock.verifyToken.mockResolvedValue({ value: false })
      tokenStore.save(sampleReply)

      const result = await client.restoreSession()

      expect(result).toBeNull()
      expect(client.isAuthenticated()).toBe(false)
      expect(tokenStore.load()).toBeNull()
    })

    it('returns null when no token is stored', async () => {
      const result = await client.restoreSession()
      expect(result).toBeNull()
    })
  })

  describe('getAuthMetadata', () => {
    it('returns metadata with Bearer token when authenticated', async () => {
      const mock = getMockInstance()
      mock.login.mockResolvedValue(sampleReply)
      await client.login('test@example.com', 'pass')

      const metadata = client.getAuthMetadata()
      expect(metadata.get('Authorization')).toEqual(['Bearer test-token-123'])
    })

    it('throws when not authenticated', () => {
      expect(() => client.getAuthMetadata()).toThrow('Not authenticated')
    })
  })

  describe('logout', () => {
    it('clears token and storage', async () => {
      const mock = getMockInstance()
      mock.login.mockResolvedValue(sampleReply)
      await client.login('test@example.com', 'pass')

      client.logout()

      expect(client.isAuthenticated()).toBe(false)
      expect(client.getToken()).toBeNull()
      expect(tokenStore.load()).toBeNull()
    })
  })
})
