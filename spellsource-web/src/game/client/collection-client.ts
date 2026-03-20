import {
  UnauthenticatedCardsClientImpl,
  AuthenticatedCardsClientImpl,
  type GetCardsResponse,
} from 'spellsource-protos/dist/experimental/client/hiddenswitch'
import {
  HiddenSwitchSpellsourceAPIServiceClientImpl,
  type DecksGetAllResponse,
  type DecksGetResponse,
  type DecksPutRequest,
  type DecksPutResponse,
  type DecksUpdateRequest,
  type DraftState,
  type DraftsChooseCardRequest,
  type DraftsChooseHeroRequest,
  type DraftsPostRequest,
} from 'spellsource-protos/dist/experimental/client/spellsource'
import { createHiddenswitchRpc, createSpellsourceRpc, createAuthMetadata, type TransportOptions } from './grpc-transport'

export interface CollectionClientOptions extends TransportOptions {
  getToken: () => string | null
}

export class CollectionClient {
  private unauthenticatedCards: UnauthenticatedCardsClientImpl
  private authenticatedCards: AuthenticatedCardsClientImpl
  private api: HiddenSwitchSpellsourceAPIServiceClientImpl
  private getToken: () => string | null

  constructor(options: CollectionClientOptions) {
    const hiddenswitchRpc = createHiddenswitchRpc(options)
    const spellsourceRpc = createSpellsourceRpc(options)
    this.unauthenticatedCards = new UnauthenticatedCardsClientImpl(hiddenswitchRpc)
    this.authenticatedCards = new AuthenticatedCardsClientImpl(hiddenswitchRpc)
    this.api = new HiddenSwitchSpellsourceAPIServiceClientImpl(spellsourceRpc)
    this.getToken = options.getToken
  }

  private requireAuth() {
    const token = this.getToken()
    if (!token) throw new Error('Not authenticated')
    return createAuthMetadata(token)
  }

  /**
   * Get the full card catalogue. Pass a previous version string for ETag caching.
   */
  async getCards(ifNoneMatch?: string): Promise<GetCardsResponse> {
    return this.unauthenticatedCards.getCards({ IfNoneMatch: ifNoneMatch ?? '', userId: '' })
  }

  /**
   * Get cards owned by the authenticated user.
   */
  async getCardsByUser(ifNoneMatch?: string): Promise<GetCardsResponse> {
    const metadata = this.requireAuth()
    return this.authenticatedCards.getCardsByUser({ IfNoneMatch: ifNoneMatch ?? '', userId: '' }, metadata)
  }

  /**
   * Get all decks for the authenticated user.
   */
  async getDecks(): Promise<DecksGetAllResponse> {
    const metadata = this.requireAuth()
    return this.api.decksGetAll({}, metadata)
  }

  /**
   * Get a specific deck by ID.
   */
  async getDeck(deckId: string): Promise<DecksGetResponse> {
    const metadata = this.requireAuth()
    return this.api.decksGet({ deckId }, metadata)
  }

  /**
   * Create a new deck.
   */
  async createDeck(request: Partial<DecksPutRequest>): Promise<DecksPutResponse> {
    const metadata = this.requireAuth()
    return this.api.decksPut(request, metadata)
  }

  /**
   * Update an existing deck.
   */
  async updateDeck(deckId: string, updateCommand: DecksUpdateRequest['updateCommand']): Promise<DecksGetResponse> {
    const metadata = this.requireAuth()
    return this.api.decksUpdate({ deckId, updateCommand }, metadata)
  }

  /**
   * Delete a deck by ID.
   */
  async deleteDeck(deckId: string): Promise<void> {
    const metadata = this.requireAuth()
    await this.api.decksDelete({ deckId }, metadata)
  }

  /**
   * Duplicate a deck by ID. Creates a copy for the caller.
   */
  async duplicateDeck(deckId: string): Promise<DecksGetResponse> {
    const metadata = this.requireAuth()
    return this.api.duplicateDeck({ value: deckId }, metadata)
  }

  /**
   * Get the current draft state.
   */
  async getDraft(): Promise<DraftState> {
    const metadata = this.requireAuth()
    return this.api.draftsGet({}, metadata)
  }

  /**
   * Start a new draft or retire early.
   */
  async postDraft(request: Partial<DraftsPostRequest>): Promise<DraftState> {
    const metadata = this.requireAuth()
    return this.api.draftsPost(request, metadata)
  }

  /**
   * Choose a hero for the draft.
   */
  async draftsChooseHero(heroIndex: number): Promise<DraftState> {
    const metadata = this.requireAuth()
    return this.api.draftsChooseHero({ heroIndex }, metadata)
  }

  /**
   * Choose a card for the draft.
   */
  async draftsChooseCard(cardIndex: number): Promise<DraftState> {
    const metadata = this.requireAuth()
    return this.api.draftsChooseCard({ cardIndex }, metadata)
  }
}
