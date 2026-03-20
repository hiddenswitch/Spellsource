import { BrowserHeaders } from 'browser-headers'
import {
  UnauthenticatedClientImpl,
  type LoginOrCreateReply,
  type ClientConfiguration,
} from 'spellsource-protos/dist/experimental/client/hiddenswitch'
import { createHiddenswitchRpc, createAuthMetadata, type TransportOptions } from './grpc-transport'
import { createTokenStore, type TokenStore } from './token-store'

export interface AuthClientOptions extends TransportOptions {
  tokenStore?: TokenStore
}

export class AuthClient {
  private unauthenticated: UnauthenticatedClientImpl
  private tokenStore: TokenStore
  private currentToken: string | null = null

  constructor(options: AuthClientOptions = {}) {
    const rpc = createHiddenswitchRpc(options)
    this.unauthenticated = new UnauthenticatedClientImpl(rpc)
    this.tokenStore = options.tokenStore ?? createTokenStore()
  }

  /**
   * Login with email/username and password.
   * Persists the token on success.
   */
  async login(usernameOrEmail: string, password: string): Promise<LoginOrCreateReply> {
    const reply = await this.unauthenticated.login({ usernameOrEmail, password })
    this.handleAuthReply(reply)
    return reply
  }

  /**
   * Create a new account.
   * Persists the token on success.
   */
  async createAccount(
    email: string,
    username: string,
    password: string,
    options?: { decks?: boolean; guest?: boolean }
  ): Promise<LoginOrCreateReply> {
    const reply = await this.unauthenticated.createAccount({
      email,
      username,
      password,
      decks: options?.decks ?? true,
      guest: options?.guest ?? false,
    })
    this.handleAuthReply(reply)
    return reply
  }

  /**
   * Attempt to restore a session from a stored token.
   * Returns the stored reply if the token is still valid, null otherwise.
   */
  async restoreSession(): Promise<LoginOrCreateReply | null> {
    const stored = this.tokenStore.load()
    if (!stored?.accessTokenResponse?.token) return null

    const valid = await this.verifyToken(stored.accessTokenResponse.token)
    if (valid) {
      this.currentToken = stored.accessTokenResponse.token
      return stored
    }

    this.tokenStore.clear()
    this.currentToken = null
    return null
  }

  /**
   * Verify that a token is still valid.
   */
  async verifyToken(token: string): Promise<boolean> {
    const result = await this.unauthenticated.verifyToken({ token })
    return result.value === true
  }

  /**
   * Get server configuration.
   */
  async getConfiguration(): Promise<ClientConfiguration> {
    return this.unauthenticated.getConfiguration({})
  }

  /**
   * Get the current auth token, or null if not authenticated.
   */
  getToken(): string | null {
    return this.currentToken
  }

  /**
   * Build gRPC metadata with the current auth token.
   * Throws if not authenticated.
   */
  getAuthMetadata(): BrowserHeaders {
    if (!this.currentToken) {
      throw new Error('Not authenticated')
    }
    return createAuthMetadata(this.currentToken)
  }

  /**
   * Clear the stored token and current session.
   */
  logout(): void {
    this.tokenStore.clear()
    this.currentToken = null
  }

  /**
   * Whether the client currently holds a token.
   */
  isAuthenticated(): boolean {
    return this.currentToken !== null
  }

  private handleAuthReply(reply: LoginOrCreateReply): void {
    if (reply.accessTokenResponse?.token) {
      this.currentToken = reply.accessTokenResponse.token
      this.tokenStore.save(reply)
    }
  }
}
