import { BehaviorSubject, Observable } from 'rxjs'
import type { LoginOrCreateReply, ClientConfiguration } from 'spellsource-protos/dist/experimental/client/hiddenswitch'
import type { TransportOptions } from './grpc-transport'
import type { TokenStore } from './token-store'
import { AuthClient } from './auth-client'
import { CollectionClient } from './collection-client'
import { MatchmakingClient } from './matchmaking-client'
import { GameClient } from './game-client'
import { GameStateManager } from '../state/game-state-manager'

export interface ClientOptions extends TransportOptions {
  tokenStore?: TokenStore
  /** WebSocket URL for the game server (derived from endpoint if not set) */
  wsUrl?: string
}

/**
 * Facade combining all sub-clients into a single entry point.
 *
 * Usage:
 *   const client = new Client({ endpoint: 'https://...' })
 *   await client.login('user', 'pass')
 *   const decks = await client.collection.getDecks()
 *   client.startGame(firstMessage)
 *   client.stateManager.managedState$.subscribe(state => render(state))
 *   client.disconnect()
 */
export class Client {
  readonly auth: AuthClient
  readonly collection: CollectionClient
  readonly matchmaking: MatchmakingClient
  readonly game: GameClient
  readonly stateManager: GameStateManager

  private authenticated$ = new BehaviorSubject<boolean>(false)

  /** Observable that emits when authentication state changes */
  readonly isAuthenticated$: Observable<boolean> = this.authenticated$.asObservable()

  constructor(options: ClientOptions = {}) {
    const getToken = () => this.auth.getToken()

    this.auth = new AuthClient({
      endpoint: options.endpoint,
      debug: options.debug,
      metadata: options.metadata,
      tokenStore: options.tokenStore,
    })

    this.collection = new CollectionClient({
      endpoint: options.endpoint,
      debug: options.debug,
      metadata: options.metadata,
      getToken,
    })

    this.matchmaking = new MatchmakingClient({
      endpoint: options.endpoint,
      debug: options.debug,
      metadata: options.metadata,
      getToken,
    })

    this.game = new GameClient({
      endpoint: options.endpoint,
      wsUrl: options.wsUrl,
      getToken,
    })

    this.stateManager = new GameStateManager()
  }

  /**
   * Login and update auth state.
   */
  async login(usernameOrEmail: string, password: string): Promise<LoginOrCreateReply> {
    const reply = await this.auth.login(usernameOrEmail, password)
    this.authenticated$.next(true)
    return reply
  }

  /**
   * Create an account and update auth state.
   */
  async createAccount(
    email: string,
    username: string,
    password: string,
    options?: { decks?: boolean; guest?: boolean }
  ): Promise<LoginOrCreateReply> {
    const reply = await this.auth.createAccount(email, username, password, options)
    this.authenticated$.next(true)
    return reply
  }

  /**
   * Try to restore a previous session from stored token.
   * Returns true if restoration succeeded.
   */
  async restoreSession(): Promise<boolean> {
    const reply = await this.auth.restoreSession()
    const ok = reply !== null
    this.authenticated$.next(ok)
    return ok
  }

  /**
   * Get the server configuration.
   */
  async getConfiguration(): Promise<ClientConfiguration> {
    return this.auth.getConfiguration()
  }

  /**
   * Start a game: open the WebSocket and subscribe the state manager.
   */
  startGame(firstMessage: { playerKey: string; playerSecret: string }): void {
    this.stateManager.subscribe(this.game.messages$)
    this.game.startGame(firstMessage)
  }

  /**
   * Logout and tear down all active connections.
   */
  logout(): void {
    this.disconnect()
    this.auth.logout()
    this.authenticated$.next(false)
  }

  /**
   * Disconnect game and matchmaking streams without logging out.
   */
  disconnect(): void {
    this.game.disconnect()
    this.matchmaking.dispose()
    this.stateManager.unsubscribe()
  }

  /**
   * Clean up all resources. Call when the client is no longer needed.
   */
  dispose(): void {
    this.disconnect()
    this.game.dispose()
    this.stateManager.dispose()
    this.authenticated$.complete()
  }
}
