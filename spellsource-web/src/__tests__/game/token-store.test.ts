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

describe('TokenStore', () => {
  let storage: Storage
  let store: TokenStore

  beforeEach(() => {
    storage = mockStorage()
    store = createTokenStore(storage)
  })

  it('returns null when no token is stored', () => {
    expect(store.load()).toBeNull()
  })

  it('saves and loads a token', () => {
    store.save(sampleReply)
    const loaded = store.load()
    expect(loaded).toEqual(sampleReply)
  })

  it('clears the stored token', () => {
    store.save(sampleReply)
    store.clear()
    expect(store.load()).toBeNull()
  })

  it('returns null for corrupted storage data', () => {
    storage.setItem('spellsource_auth', '{invalid json')
    expect(store.load()).toBeNull()
  })

  it('overwrites a previously saved token', () => {
    store.save(sampleReply)
    const updated: LoginOrCreateReply = {
      ...sampleReply,
      accessTokenResponse: { token: 'new-token-456' },
    }
    store.save(updated)
    expect(store.load()?.accessTokenResponse?.token).toBe('new-token-456')
  })
})
