import { Subject } from 'rxjs'
import { MatchmakingClient } from '../../game/client/matchmaking-client'
import type { Match } from 'spellsource-protos/dist/experimental/client/spellsource'

const mockMatchmakingGet = jest.fn()
const mockMatchmakingDelete = jest.fn()
const mockSubscribeMatch = jest.fn()

jest.mock('spellsource-protos/dist/experimental/client/spellsource', () => {
  const actual = jest.requireActual('spellsource-protos/dist/experimental/client/spellsource')
  return {
    ...actual,
    MatchmakingClientImpl: jest.fn().mockImplementation(() => ({
      matchmakingGet: mockMatchmakingGet,
      matchmakingDelete: mockMatchmakingDelete,
    })),
    HiddenSwitchSpellsourceAPIServiceClientImpl: jest.fn().mockImplementation(() => ({
      subscribeMatch: mockSubscribeMatch,
    })),
  }
})

describe('MatchmakingClient', () => {
  let client: MatchmakingClient
  let token: string | null = 'test-token'

  beforeEach(() => {
    jest.clearAllMocks()
    token = 'test-token'
    client = new MatchmakingClient({ getToken: () => token })
  })

  afterEach(() => {
    client.dispose()
  })

  describe('getQueues', () => {
    it('returns available queues', async () => {
      const queues = {
        queues: [
          { queueId: 'normal', name: 'Normal', description: '', tooltip: '', requires: undefined },
        ],
      }
      mockMatchmakingGet.mockResolvedValue(queues)

      const result = await client.getQueues()

      expect(result).toEqual(queues)
      expect(mockMatchmakingGet).toHaveBeenCalledWith({}, expect.anything())
    })

    it('throws when not authenticated', async () => {
      token = null
      await expect(client.getQueues()).rejects.toThrow('Not authenticated')
    })
  })

  describe('cancelMatchmaking', () => {
    it('calls matchmakingDelete', async () => {
      mockMatchmakingDelete.mockResolvedValue({ isCanceled: true })

      const result = await client.cancelMatchmaking()

      expect(result.isCanceled).toBe(true)
    })
  })

  describe('subscribeMatch', () => {
    it('emits match when server assigns one', (done) => {
      const matchSubject = new Subject<Match>()
      mockSubscribeMatch.mockReturnValue(matchSubject.asObservable())

      const match$ = client.subscribeMatch()
      const received: Match[] = []

      match$.subscribe({
        next: (m) => received.push(m),
        complete: () => {
          expect(received).toHaveLength(1)
          expect(received[0].Id).toBe('match-1')
          done()
        },
      })

      matchSubject.next({ Id: 'match-1', createdAt: 1234567890 })
      matchSubject.complete()
    })

    it('completes when cancelMatchmaking is called', (done) => {
      const matchSubject = new Subject<Match>()
      mockSubscribeMatch.mockReturnValue(matchSubject.asObservable())
      mockMatchmakingDelete.mockResolvedValue({ isCanceled: true })

      const match$ = client.subscribeMatch()

      match$.subscribe({
        complete: () => done(),
      })

      client.cancelMatchmaking()
    })
  })
})
