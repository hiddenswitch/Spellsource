import { GameClient } from '../../game/client/game-client'
import {
  ClientToServerMessage,
  ServerToClientMessage,
  MessageTypeMessage_MessageType,
} from 'spellsource-protos/dist/experimental/client/spellsource'

// Mock WebSocket
class MockWebSocket {
  static instances: MockWebSocket[] = []

  url: string
  binaryType: string = 'blob'
  readyState: number = 0 // CONNECTING
  onopen: ((ev: Event) => void) | null = null
  onmessage: ((ev: MessageEvent) => void) | null = null
  onerror: ((ev: Event) => void) | null = null
  onclose: ((ev: CloseEvent) => void) | null = null
  sentMessages: Uint8Array[] = []

  constructor(url: string) {
    this.url = url
    MockWebSocket.instances.push(this)
  }

  send(data: Uint8Array) {
    this.sentMessages.push(data)
  }

  close() {
    this.readyState = 3 // CLOSED
  }

  // Test helpers
  simulateOpen() {
    this.readyState = 1 // OPEN
    this.onopen?.(new Event('open'))
  }

  simulateMessage(msg: Partial<typeof ServerToClientMessage extends { encode: (m: infer M, ...args: any[]) => any } ? M : never>) {
    const serverMsg = ServerToClientMessage.fromPartial(msg)
    const encoded = ServerToClientMessage.encode(serverMsg).finish()
    // Copy to a fresh ArrayBuffer to avoid byteOffset issues with protobufjs Writer buffer reuse
    const buf = new ArrayBuffer(encoded.byteLength)
    new Uint8Array(buf).set(encoded)
    this.onmessage?.(new MessageEvent('message', { data: buf }))
  }

  simulateError() {
    this.onerror?.(new Event('error'))
  }

  static get OPEN() { return 1 }
  static get CONNECTING() { return 0 }
  static get CLOSING() { return 2 }
  static get CLOSED() { return 3 }
}

// Install mock globally
;(global as any).WebSocket = MockWebSocket

describe('GameClient', () => {
  let client: GameClient
  let token: string | null = 'test-token'

  beforeEach(() => {
    MockWebSocket.instances = []
    token = 'test-token'
    client = new GameClient({ wsUrl: 'wss://test.example.com/game', getToken: () => token })
  })

  afterEach(() => {
    client.dispose()
  })

  function getLastSocket(): MockWebSocket {
    return MockWebSocket.instances[MockWebSocket.instances.length - 1]
  }

  describe('startGame', () => {
    it('opens a WebSocket with the token in the URL', () => {
      client.startGame({ playerKey: 'key-1', playerSecret: 'secret-1' })

      const ws = getLastSocket()
      expect(ws.url).toBe('wss://test.example.com/game?token=test-token')
      expect(ws.binaryType).toBe('arraybuffer')
    })

    it('sends the first message on open', () => {
      client.startGame({ playerKey: 'key-1', playerSecret: 'secret-1' })
      const ws = getLastSocket()
      ws.simulateOpen()

      expect(ws.sentMessages).toHaveLength(1)
      const decoded = ClientToServerMessage.decode(ws.sentMessages[0])
      expect(decoded.messageType).toBe(MessageTypeMessage_MessageType.FIRST_MESSAGE)
      expect(decoded.firstMessage?.playerKey).toBe('key-1')
      expect(decoded.firstMessage?.playerSecret).toBe('secret-1')
    })

    it('relays incoming messages to messages$', (done) => {
      const received: any[] = []
      client.messages$.subscribe({
        next: (msg) => {
          received.push(msg)
          if (received.length === 2) {
            expect(received[0].messageType).toBe(MessageTypeMessage_MessageType.ON_UPDATE)
            expect(received[1].messageType).toBe(MessageTypeMessage_MessageType.ON_REQUEST_ACTION)
            done()
          }
        },
      })

      client.startGame({ playerKey: 'k', playerSecret: 's' })
      const ws = getLastSocket()
      ws.simulateOpen()

      ws.simulateMessage({ messageType: MessageTypeMessage_MessageType.ON_UPDATE })
      ws.simulateMessage({ messageType: MessageTypeMessage_MessageType.ON_REQUEST_ACTION, id: 'req-1' })
    })

    it('throws when not authenticated', () => {
      token = null
      expect(() => client.startGame({ playerKey: 'k', playerSecret: 's' }))
        .toThrow('Not authenticated')
    })
  })

  describe('sendAction', () => {
    it('sends an action message via WebSocket', () => {
      client.startGame({ playerKey: 'k', playerSecret: 's' })
      const ws = getLastSocket()
      ws.simulateOpen()

      client.sendAction(3, 'req-1')

      expect(ws.sentMessages).toHaveLength(2) // first message + action
      const decoded = ClientToServerMessage.decode(ws.sentMessages[1])
      expect(decoded.actionIndex).toBe(3)
      expect(decoded.repliesTo).toBe('req-1')
      expect(decoded.messageType).toBe(MessageTypeMessage_MessageType.UPDATE_ACTION)
    })
  })

  describe('sendMulligan', () => {
    it('sends mulligan with discarded indices', () => {
      client.startGame({ playerKey: 'k', playerSecret: 's' })
      const ws = getLastSocket()
      ws.simulateOpen()

      client.sendMulligan([0, 2], 'req-mulligan')

      const decoded = ClientToServerMessage.decode(ws.sentMessages[1])
      expect(decoded.discardedCardIndices).toEqual([0, 2])
      expect(decoded.repliesTo).toBe('req-mulligan')
      expect(decoded.messageType).toBe(MessageTypeMessage_MessageType.UPDATE_MULLIGAN)
    })
  })

  describe('sendConcede', () => {
    it('sends a concede message', () => {
      client.startGame({ playerKey: 'k', playerSecret: 's' })
      const ws = getLastSocket()
      ws.simulateOpen()

      client.sendConcede()

      const decoded = ClientToServerMessage.decode(ws.sentMessages[1])
      expect(decoded.messageType).toBe(MessageTypeMessage_MessageType.CONCEDE)
    })
  })

  describe('disconnect', () => {
    it('closes the WebSocket and stops receiving messages', () => {
      client.startGame({ playerKey: 'k', playerSecret: 's' })
      const ws = getLastSocket()
      ws.simulateOpen()

      const received: any[] = []
      client.messages$.subscribe({ next: (msg) => received.push(msg) })

      ws.simulateMessage({ messageType: MessageTypeMessage_MessageType.ON_UPDATE })
      expect(received).toHaveLength(1)

      client.disconnect()

      // onmessage should be cleared
      expect(ws.onmessage).toBeNull()
      expect(ws.readyState).toBe(3) // CLOSED
    })
  })
})
