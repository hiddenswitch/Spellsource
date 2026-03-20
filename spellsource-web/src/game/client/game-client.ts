import { Observable, Subject } from 'rxjs'
import {
  ClientToServerMessage,
  type ClientToServerMessage_FirstMessageMessage,
  ServerToClientMessage,
  type Emote,
  MessageTypeMessage_MessageType,
} from 'spellsource-protos/dist/experimental/client/spellsource'
import { DEFAULT_ENDPOINT } from './grpc-transport'

export interface GameClientOptions {
  /** WebSocket URL for the game server. Derived from endpoint if not provided. */
  wsUrl?: string
  /** The gRPC endpoint, used to derive wsUrl if wsUrl isn't set. */
  endpoint?: string
  getToken: () => string | null
}

/**
 * Game client using WebSocket with protobuf-encoded messages.
 *
 * grpc-web doesn't support bidi streaming (required by SubscribeGame),
 * so we use a direct WebSocket connection with protobuf serialization.
 */
export class GameClient {
  private wsUrl: string
  private getToken: () => string | null
  private ws: WebSocket | null = null
  private incoming$ = new Subject<ServerToClientMessage>()
  private lastFirstMessage: ClientToServerMessage_FirstMessageMessage | null = null

  /** Stream of incoming server messages. */
  readonly messages$: Observable<ServerToClientMessage> = this.incoming$.asObservable()

  constructor(options: GameClientOptions) {
    this.wsUrl = options.wsUrl ?? deriveWsUrl(options.endpoint ?? DEFAULT_ENDPOINT)
    this.getToken = options.getToken
  }

  /**
   * Open the game WebSocket and send the first message with player credentials.
   */
  startGame(firstMessage: ClientToServerMessage_FirstMessageMessage): void {
    this.lastFirstMessage = firstMessage
    this.closeSocket()

    const token = this.getToken()
    if (!token) throw new Error('Not authenticated')

    const url = `${this.wsUrl}?token=${encodeURIComponent(token)}`
    this.ws = new WebSocket(url)
    this.ws.binaryType = 'arraybuffer'

    this.ws.onopen = () => {
      this.send({
        firstMessage,
        messageType: MessageTypeMessage_MessageType.FIRST_MESSAGE,
        actionIndex: -1,
        discardedCardIndices: [],
        repliesTo: '',
      })
    }

    this.ws.onmessage = (event: MessageEvent) => {
      const data = new Uint8Array(event.data as ArrayBuffer)
      const msg = ServerToClientMessage.decode(data)
      this.incoming$.next(msg)
    }

    this.ws.onerror = () => {
      // Attempt reconnect if we have credentials
      if (this.lastFirstMessage) {
        setTimeout(() => {
          if (this.lastFirstMessage) {
            this.startGame(this.lastFirstMessage)
          }
        }, 1000)
      }
    }

    this.ws.onclose = () => {
      // Normal close, no action needed
    }
  }

  /**
   * Send a game action.
   */
  sendAction(actionIndex: number, repliesTo: string): void {
    this.send({
      actionIndex,
      repliesTo,
      messageType: MessageTypeMessage_MessageType.UPDATE_ACTION,
      discardedCardIndices: [],
    })
  }

  /**
   * Send mulligan choice.
   */
  sendMulligan(discardedCardIndices: number[], repliesTo: string): void {
    this.send({
      discardedCardIndices,
      repliesTo,
      messageType: MessageTypeMessage_MessageType.UPDATE_MULLIGAN,
      actionIndex: -1,
    })
  }

  /**
   * Send an emote.
   */
  sendEmote(emote: Emote): void {
    this.send({
      emote,
      messageType: MessageTypeMessage_MessageType.EMOTE,
      actionIndex: -1,
      discardedCardIndices: [],
      repliesTo: '',
    })
  }

  /**
   * Send a concede message.
   */
  sendConcede(): void {
    this.send({
      messageType: MessageTypeMessage_MessageType.CONCEDE,
      actionIndex: -1,
      discardedCardIndices: [],
      repliesTo: '',
    })
  }

  /**
   * Close the game WebSocket.
   */
  disconnect(): void {
    this.closeSocket()
    this.lastFirstMessage = null
  }

  /**
   * Clean up all resources.
   */
  dispose(): void {
    this.disconnect()
    this.incoming$.complete()
  }

  private send(message: Partial<ClientToServerMessage>): void {
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
      return
    }
    const encoded = ClientToServerMessage.encode(
      ClientToServerMessage.fromPartial(message)
    ).finish()
    this.ws.send(encoded)
  }

  private closeSocket(): void {
    if (this.ws) {
      this.ws.onopen = null
      this.ws.onmessage = null
      this.ws.onerror = null
      this.ws.onclose = null
      if (this.ws.readyState === WebSocket.OPEN || this.ws.readyState === WebSocket.CONNECTING) {
        this.ws.close()
      }
      this.ws = null
    }
  }
}

/**
 * Derive a WebSocket URL from an HTTP endpoint.
 */
function deriveWsUrl(endpoint: string): string {
  return endpoint
    .replace(/^https:/, 'wss:')
    .replace(/^http:/, 'ws:')
    + '/game'
}
