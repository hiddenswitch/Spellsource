import type { LoginOrCreateReply } from 'spellsource-protos/dist/experimental/client/hiddenswitch'

const STORAGE_KEY = 'spellsource_auth'

export interface TokenStore {
  save(reply: LoginOrCreateReply): void
  load(): LoginOrCreateReply | null
  clear(): void
}

export function createTokenStore(storage: Storage = localStorage): TokenStore {
  return {
    save(reply: LoginOrCreateReply): void {
      storage.setItem(STORAGE_KEY, JSON.stringify(reply))
    },

    load(): LoginOrCreateReply | null {
      const raw = storage.getItem(STORAGE_KEY)
      if (!raw) return null
      try {
        return JSON.parse(raw) as LoginOrCreateReply
      } catch {
        return null
      }
    },

    clear(): void {
      storage.removeItem(STORAGE_KEY)
    },
  }
}
