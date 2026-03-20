import { CollectionClient } from '../../game/client/collection-client'

// Mock the proto client impls
jest.mock('spellsource-protos/dist/experimental/client/hiddenswitch', () => {
  const actual = jest.requireActual('spellsource-protos/dist/experimental/client/hiddenswitch')
  return {
    ...actual,
    UnauthenticatedCardsClientImpl: jest.fn().mockImplementation(() => ({
      getCards: jest.fn(),
    })),
    AuthenticatedCardsClientImpl: jest.fn().mockImplementation(() => ({
      getCardsByUser: jest.fn(),
    })),
  }
})

jest.mock('spellsource-protos/dist/experimental/client/spellsource', () => {
  const actual = jest.requireActual('spellsource-protos/dist/experimental/client/spellsource')
  return {
    ...actual,
    HiddenSwitchSpellsourceAPIServiceClientImpl: jest.fn().mockImplementation(() => ({
      decksGetAll: jest.fn(),
      decksGet: jest.fn(),
      decksPut: jest.fn(),
      decksUpdate: jest.fn(),
      decksDelete: jest.fn(),
      duplicateDeck: jest.fn(),
      draftsGet: jest.fn(),
      draftsPost: jest.fn(),
      draftsChooseHero: jest.fn(),
      draftsChooseCard: jest.fn(),
    })),
  }
})

import { UnauthenticatedCardsClientImpl, AuthenticatedCardsClientImpl } from 'spellsource-protos/dist/experimental/client/hiddenswitch'
import { HiddenSwitchSpellsourceAPIServiceClientImpl } from 'spellsource-protos/dist/experimental/client/spellsource'

const MockUnauthCards = UnauthenticatedCardsClientImpl as jest.MockedClass<typeof UnauthenticatedCardsClientImpl>
const MockAuthCards = AuthenticatedCardsClientImpl as jest.MockedClass<typeof AuthenticatedCardsClientImpl>
const MockApi = HiddenSwitchSpellsourceAPIServiceClientImpl as jest.MockedClass<typeof HiddenSwitchSpellsourceAPIServiceClientImpl>

describe('CollectionClient', () => {
  let client: CollectionClient
  let token: string | null = 'test-token'

  beforeEach(() => {
    MockUnauthCards.mockClear()
    MockAuthCards.mockClear()
    MockApi.mockClear()
    token = 'test-token'
    client = new CollectionClient({ getToken: () => token })
  })

  function getUnauthCardsMock() {
    return MockUnauthCards.mock.results[0].value
  }

  function getAuthCardsMock() {
    return MockAuthCards.mock.results[0].value
  }

  function getApiMock() {
    return MockApi.mock.results[0].value
  }

  describe('getCards', () => {
    it('calls unauthenticated getCards without auth', async () => {
      const mock = getUnauthCardsMock()
      const response = { content: { cards: [] }, version: 'v1', cachedOk: false }
      mock.getCards.mockResolvedValue(response)

      const result = await client.getCards()

      expect(mock.getCards).toHaveBeenCalledWith({ IfNoneMatch: '', userId: '' })
      expect(result).toEqual(response)
    })

    it('passes ETag via IfNoneMatch', async () => {
      const mock = getUnauthCardsMock()
      mock.getCards.mockResolvedValue({ content: undefined, version: 'v1', cachedOk: true })

      await client.getCards('etag-123')

      expect(mock.getCards).toHaveBeenCalledWith({ IfNoneMatch: 'etag-123', userId: '' })
    })
  })

  describe('getCardsByUser', () => {
    it('calls authenticated getCardsByUser with auth metadata', async () => {
      const mock = getAuthCardsMock()
      const response = { content: { cards: [] }, version: 'v1', cachedOk: false }
      mock.getCardsByUser.mockResolvedValue(response)

      const result = await client.getCardsByUser()

      expect(mock.getCardsByUser).toHaveBeenCalledWith(
        { IfNoneMatch: '', userId: '' },
        expect.objectContaining({})
      )
      expect(result).toEqual(response)
    })

    it('throws when not authenticated', async () => {
      token = null
      await expect(client.getCardsByUser()).rejects.toThrow('Not authenticated')
    })
  })

  describe('deck operations', () => {
    it('getDecks calls decksGetAll with auth', async () => {
      const mock = getApiMock()
      mock.decksGetAll.mockResolvedValue({ decks: [] })

      const result = await client.getDecks()

      expect(mock.decksGetAll).toHaveBeenCalledWith({}, expect.anything())
      expect(result).toEqual({ decks: [] })
    })

    it('getDeck calls decksGet with deckId', async () => {
      const mock = getApiMock()
      const response = { collection: undefined, inventoryIdsSize: 0 }
      mock.decksGet.mockResolvedValue(response)

      await client.getDeck('deck-1')

      expect(mock.decksGet).toHaveBeenCalledWith({ deckId: 'deck-1' }, expect.anything())
    })

    it('createDeck calls decksPut', async () => {
      const mock = getApiMock()
      mock.decksPut.mockResolvedValue({ collection: undefined, deckId: 'new-deck' })

      const result = await client.createDeck({ name: 'My Deck', heroClass: 'RED' })

      expect(mock.decksPut).toHaveBeenCalledWith(
        { name: 'My Deck', heroClass: 'RED' },
        expect.anything()
      )
      expect(result.deckId).toBe('new-deck')
    })

    it('updateDeck calls decksUpdate', async () => {
      const mock = getApiMock()
      mock.decksUpdate.mockResolvedValue({ collection: undefined, inventoryIdsSize: 30 })

      const updateCmd = {
        pushCardIds: { Each: ['card_1'] },
        pullAllCardIds: [],
        pullAllInventoryIds: [],
        pushInventoryIds: undefined,
        setHeroClass: '',
        setInventoryIds: [],
        setName: '',
        setPlayerEntityAttribute: undefined,
        unsetPlayerEntityAttribute: '',
      }

      await client.updateDeck('deck-1', updateCmd)

      expect(mock.decksUpdate).toHaveBeenCalledWith(
        { deckId: 'deck-1', updateCommand: updateCmd },
        expect.anything()
      )
    })

    it('deleteDeck calls decksDelete', async () => {
      const mock = getApiMock()
      mock.decksDelete.mockResolvedValue({})

      await client.deleteDeck('deck-1')

      expect(mock.decksDelete).toHaveBeenCalledWith({ deckId: 'deck-1' }, expect.anything())
    })

    it('duplicateDeck calls duplicateDeck with StringValue', async () => {
      const mock = getApiMock()
      mock.duplicateDeck.mockResolvedValue({ collection: undefined, inventoryIdsSize: 30 })

      await client.duplicateDeck('deck-1')

      expect(mock.duplicateDeck).toHaveBeenCalledWith({ value: 'deck-1' }, expect.anything())
    })

    it('all deck operations throw when not authenticated', async () => {
      token = null
      await expect(client.getDecks()).rejects.toThrow('Not authenticated')
      await expect(client.getDeck('x')).rejects.toThrow('Not authenticated')
      await expect(client.createDeck({})).rejects.toThrow('Not authenticated')
      await expect(client.updateDeck('x', undefined)).rejects.toThrow('Not authenticated')
      await expect(client.deleteDeck('x')).rejects.toThrow('Not authenticated')
      await expect(client.duplicateDeck('x')).rejects.toThrow('Not authenticated')
    })
  })

  describe('draft operations', () => {
    it('getDraft calls draftsGet', async () => {
      const mock = getApiMock()
      const draftState = {
        cardsRemaining: 10,
        currentCardChoices: [],
        deckId: 'draft-deck',
        draftIndex: 0,
        heroClass: undefined,
        heroClassChoices: [],
        losses: 0,
        selectedCardIds: [],
        status: 0,
        wins: 0,
      }
      mock.draftsGet.mockResolvedValue(draftState)

      const result = await client.getDraft()
      expect(result).toEqual(draftState)
    })

    it('postDraft calls draftsPost with startDraft', async () => {
      const mock = getApiMock()
      mock.draftsPost.mockResolvedValue({ status: 1 })

      await client.postDraft({ startDraft: true })

      expect(mock.draftsPost).toHaveBeenCalledWith({ startDraft: true }, expect.anything())
    })

    it('draftsChooseHero calls with heroIndex', async () => {
      const mock = getApiMock()
      mock.draftsChooseHero.mockResolvedValue({ status: 0 })

      await client.draftsChooseHero(2)

      expect(mock.draftsChooseHero).toHaveBeenCalledWith({ heroIndex: 2 }, expect.anything())
    })

    it('draftsChooseCard calls with cardIndex', async () => {
      const mock = getApiMock()
      mock.draftsChooseCard.mockResolvedValue({ status: 0 })

      await client.draftsChooseCard(1)

      expect(mock.draftsChooseCard).toHaveBeenCalledWith({ cardIndex: 1 }, expect.anything())
    })
  })
})
