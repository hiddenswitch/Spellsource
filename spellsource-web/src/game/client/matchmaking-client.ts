import { Observable, Subject, Subscription, filter, takeUntil } from 'rxjs'
import {
  MatchmakingClientImpl,
  HiddenSwitchSpellsourceAPIServiceClientImpl,
  type MatchmakingQueuesResponse,
  type MatchCancelResponse,
  type Match,
} from 'spellsource-protos/dist/experimental/client/spellsource'
import { createSpellsourceRpc, createAuthMetadata, type TransportOptions } from './grpc-transport'

export interface MatchmakingClientOptions extends TransportOptions {
  getToken: () => string | null
}

export class MatchmakingClient {
  private matchmaking: MatchmakingClientImpl
  private api: HiddenSwitchSpellsourceAPIServiceClientImpl
  private getToken: () => string | null
  private matchSubscription: Subscription | null = null
  private cancel$ = new Subject<void>()

  constructor(options: MatchmakingClientOptions) {
    const rpc = createSpellsourceRpc(options)
    this.matchmaking = new MatchmakingClientImpl(rpc)
    this.api = new HiddenSwitchSpellsourceAPIServiceClientImpl(rpc)
    this.getToken = options.getToken
  }

  private requireAuth() {
    const token = this.getToken()
    if (!token) throw new Error('Not authenticated')
    return createAuthMetadata(token)
  }

  /**
   * Get available matchmaking queues.
   */
  async getQueues(): Promise<MatchmakingQueuesResponse> {
    const metadata = this.requireAuth()
    return this.matchmaking.matchmakingGet({}, metadata)
  }

  /**
   * Cancel current matchmaking.
   */
  async cancelMatchmaking(): Promise<MatchCancelResponse> {
    const metadata = this.requireAuth()
    this.cancel$.next()
    return this.matchmaking.matchmakingDelete({}, metadata)
  }

  /**
   * Subscribe to match assignments. Emits when the server assigns this player to a match.
   * The observable completes when the match is found or when cancelMatchmaking() is called.
   *
   * Note: The actual enqueue (joining a queue) happens through the envelope-based websocket
   * layer or REST API, not through grpc-web (which doesn't support bidi streaming).
   * Use this subscription to listen for the resulting match assignment.
   */
  subscribeMatch(): Observable<Match> {
    const metadata = this.requireAuth()
    return this.api.subscribeMatch({}, metadata).pipe(
      takeUntil(this.cancel$)
    )
  }

  /**
   * Clean up subscriptions.
   */
  dispose(): void {
    this.cancel$.next()
    this.cancel$.complete()
    if (this.matchSubscription) {
      this.matchSubscription.unsubscribe()
      this.matchSubscription = null
    }
  }
}
