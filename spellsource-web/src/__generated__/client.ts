import { GraphQLResolveInfo, GraphQLScalarType, GraphQLScalarTypeConfig } from 'graphql';
import { gql } from '@apollo/client';
import * as Apollo from '@apollo/client';
export type Maybe<T> = T | null;
export type InputMaybe<T> = Maybe<T>;
export type Exact<T extends { [key: string]: unknown }> = { [K in keyof T]: T[K] };
export type MakeOptional<T, K extends keyof T> = Omit<T, K> & { [SubKey in K]?: Maybe<T[SubKey]> };
export type MakeMaybe<T, K extends keyof T> = Omit<T, K> & { [SubKey in K]: Maybe<T[SubKey]> };
export type RequireFields<T, K extends keyof T> = Omit<T, K> & { [P in K]-?: NonNullable<T[P]> };
const defaultOptions = {} as const;
/** All built-in and custom scalars, mapped to their actual values */
export type Scalars = {
  ID: string;
  String: string;
  Boolean: boolean;
  Int: number;
  Float: number;
  BigInt: any;
  Cursor: any;
  Datetime: any;
  JSON: any;
};

export type BotUser = Node & {
  __typename?: 'BotUser';
  id: Scalars['String'];
  nodeId: Scalars['ID'];
};

export type BotUserCondition = {
  id?: InputMaybe<Scalars['String']>;
};

export type BotUserInput = {
  id: Scalars['String'];
};

export type BotUserPatch = {
  id?: InputMaybe<Scalars['String']>;
};

export type BotUsersConnection = {
  __typename?: 'BotUsersConnection';
  edges: Array<BotUsersEdge>;
  nodes: Array<Maybe<BotUser>>;
  pageInfo: PageInfo;
  totalCount: Scalars['Int'];
};

export type BotUsersEdge = {
  __typename?: 'BotUsersEdge';
  cursor?: Maybe<Scalars['Cursor']>;
  node?: Maybe<BotUser>;
};

export const BotUsersOrderBy = {
  IdAsc: 'ID_ASC',
  IdDesc: 'ID_DESC',
  Natural: 'NATURAL',
  PrimaryKeyAsc: 'PRIMARY_KEY_ASC',
  PrimaryKeyDesc: 'PRIMARY_KEY_DESC'
} as const;

export type BotUsersOrderBy = typeof BotUsersOrderBy[keyof typeof BotUsersOrderBy];
export type Card = Node & {
  __typename?: 'Card';
  blocklyWorkspace?: Maybe<Scalars['String']>;
  cardScript?: Maybe<Scalars['JSON']>;
  cardsInDecksByCardId: CardsInDecksConnection;
  createdAt: Scalars['Datetime'];
  createdBy: Scalars['String'];
  id: Scalars['String'];
  lastModified: Scalars['Datetime'];
  nodeId: Scalars['ID'];
  uri?: Maybe<Scalars['String']>;
};


export type CardCardsInDecksByCardIdArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<CardsInDeckCondition>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<CardsInDecksOrderBy>>;
};

export type CardCondition = {
  blocklyWorkspace?: InputMaybe<Scalars['String']>;
  cardScript?: InputMaybe<Scalars['JSON']>;
  createdAt?: InputMaybe<Scalars['Datetime']>;
  createdBy?: InputMaybe<Scalars['String']>;
  id?: InputMaybe<Scalars['String']>;
  lastModified?: InputMaybe<Scalars['Datetime']>;
  uri?: InputMaybe<Scalars['String']>;
};

export type CardInput = {
  blocklyWorkspace?: InputMaybe<Scalars['String']>;
  cardScript?: InputMaybe<Scalars['JSON']>;
  createdAt?: InputMaybe<Scalars['Datetime']>;
  createdBy: Scalars['String'];
  id: Scalars['String'];
  lastModified?: InputMaybe<Scalars['Datetime']>;
  uri?: InputMaybe<Scalars['String']>;
};

export type CardPatch = {
  blocklyWorkspace?: InputMaybe<Scalars['String']>;
  cardScript?: InputMaybe<Scalars['JSON']>;
  createdAt?: InputMaybe<Scalars['Datetime']>;
  createdBy?: InputMaybe<Scalars['String']>;
  id?: InputMaybe<Scalars['String']>;
  lastModified?: InputMaybe<Scalars['Datetime']>;
  uri?: InputMaybe<Scalars['String']>;
};

export type CardsConnection = {
  __typename?: 'CardsConnection';
  edges: Array<CardsEdge>;
  nodes: Array<Maybe<Card>>;
  pageInfo: PageInfo;
  totalCount: Scalars['Int'];
};

export type CardsEdge = {
  __typename?: 'CardsEdge';
  cursor?: Maybe<Scalars['Cursor']>;
  node?: Maybe<Card>;
};

export type CardsInDeck = Node & {
  __typename?: 'CardsInDeck';
  cardByCardId?: Maybe<Card>;
  cardId: Scalars['String'];
  deckByDeckId?: Maybe<Deck>;
  deckId: Scalars['String'];
  id: Scalars['BigInt'];
  nodeId: Scalars['ID'];
};

export type CardsInDeckCondition = {
  cardId?: InputMaybe<Scalars['String']>;
  deckId?: InputMaybe<Scalars['String']>;
  id?: InputMaybe<Scalars['BigInt']>;
};

export type CardsInDeckInput = {
  cardId: Scalars['String'];
  deckId: Scalars['String'];
};

export type CardsInDeckPatch = {
  cardId?: InputMaybe<Scalars['String']>;
  deckId?: InputMaybe<Scalars['String']>;
};

export type CardsInDecksConnection = {
  __typename?: 'CardsInDecksConnection';
  edges: Array<CardsInDecksEdge>;
  nodes: Array<Maybe<CardsInDeck>>;
  pageInfo: PageInfo;
  totalCount: Scalars['Int'];
};

export type CardsInDecksEdge = {
  __typename?: 'CardsInDecksEdge';
  cursor?: Maybe<Scalars['Cursor']>;
  node?: Maybe<CardsInDeck>;
};

export const CardsInDecksOrderBy = {
  CardIdAsc: 'CARD_ID_ASC',
  CardIdDesc: 'CARD_ID_DESC',
  DeckIdAsc: 'DECK_ID_ASC',
  DeckIdDesc: 'DECK_ID_DESC',
  IdAsc: 'ID_ASC',
  IdDesc: 'ID_DESC',
  Natural: 'NATURAL',
  PrimaryKeyAsc: 'PRIMARY_KEY_ASC',
  PrimaryKeyDesc: 'PRIMARY_KEY_DESC'
} as const;

export type CardsInDecksOrderBy = typeof CardsInDecksOrderBy[keyof typeof CardsInDecksOrderBy];
export const CardsOrderBy = {
  BlocklyWorkspaceAsc: 'BLOCKLY_WORKSPACE_ASC',
  BlocklyWorkspaceDesc: 'BLOCKLY_WORKSPACE_DESC',
  CardScriptAsc: 'CARD_SCRIPT_ASC',
  CardScriptDesc: 'CARD_SCRIPT_DESC',
  CreatedAtAsc: 'CREATED_AT_ASC',
  CreatedAtDesc: 'CREATED_AT_DESC',
  CreatedByAsc: 'CREATED_BY_ASC',
  CreatedByDesc: 'CREATED_BY_DESC',
  IdAsc: 'ID_ASC',
  IdDesc: 'ID_DESC',
  LastModifiedAsc: 'LAST_MODIFIED_ASC',
  LastModifiedDesc: 'LAST_MODIFIED_DESC',
  Natural: 'NATURAL',
  PrimaryKeyAsc: 'PRIMARY_KEY_ASC',
  PrimaryKeyDesc: 'PRIMARY_KEY_DESC',
  UriAsc: 'URI_ASC',
  UriDesc: 'URI_DESC'
} as const;

export type CardsOrderBy = typeof CardsOrderBy[keyof typeof CardsOrderBy];
export type CreateBotUserInput = {
  botUser: BotUserInput;
  clientMutationId?: InputMaybe<Scalars['String']>;
};

export type CreateBotUserPayload = {
  __typename?: 'CreateBotUserPayload';
  botUser?: Maybe<BotUser>;
  botUserEdge?: Maybe<BotUsersEdge>;
  clientMutationId?: Maybe<Scalars['String']>;
  query?: Maybe<Query>;
};


export type CreateBotUserPayloadBotUserEdgeArgs = {
  orderBy?: InputMaybe<Array<BotUsersOrderBy>>;
};

export type CreateCardInput = {
  card: CardInput;
  clientMutationId?: InputMaybe<Scalars['String']>;
};

export type CreateCardPayload = {
  __typename?: 'CreateCardPayload';
  card?: Maybe<Card>;
  cardEdge?: Maybe<CardsEdge>;
  clientMutationId?: Maybe<Scalars['String']>;
  query?: Maybe<Query>;
};


export type CreateCardPayloadCardEdgeArgs = {
  orderBy?: InputMaybe<Array<CardsOrderBy>>;
};

export type CreateCardsInDeckInput = {
  cardsInDeck: CardsInDeckInput;
  clientMutationId?: InputMaybe<Scalars['String']>;
};

export type CreateCardsInDeckPayload = {
  __typename?: 'CreateCardsInDeckPayload';
  cardByCardId?: Maybe<Card>;
  cardsInDeck?: Maybe<CardsInDeck>;
  cardsInDeckEdge?: Maybe<CardsInDecksEdge>;
  clientMutationId?: Maybe<Scalars['String']>;
  deckByDeckId?: Maybe<Deck>;
  query?: Maybe<Query>;
};


export type CreateCardsInDeckPayloadCardsInDeckEdgeArgs = {
  orderBy?: InputMaybe<Array<CardsInDecksOrderBy>>;
};

export type CreateDeckInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  deck: DeckInput;
};

export type CreateDeckPayload = {
  __typename?: 'CreateDeckPayload';
  clientMutationId?: Maybe<Scalars['String']>;
  deck?: Maybe<Deck>;
  deckEdge?: Maybe<DecksEdge>;
  query?: Maybe<Query>;
};


export type CreateDeckPayloadDeckEdgeArgs = {
  orderBy?: InputMaybe<Array<DecksOrderBy>>;
};

export type CreateDeckPlayerAttributeTupleInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  deckPlayerAttributeTuple: DeckPlayerAttributeTupleInput;
};

export type CreateDeckPlayerAttributeTuplePayload = {
  __typename?: 'CreateDeckPlayerAttributeTuplePayload';
  clientMutationId?: Maybe<Scalars['String']>;
  deckByDeckId?: Maybe<Deck>;
  deckPlayerAttributeTuple?: Maybe<DeckPlayerAttributeTuple>;
  deckPlayerAttributeTupleEdge?: Maybe<DeckPlayerAttributeTuplesEdge>;
  query?: Maybe<Query>;
};


export type CreateDeckPlayerAttributeTuplePayloadDeckPlayerAttributeTupleEdgeArgs = {
  orderBy?: InputMaybe<Array<DeckPlayerAttributeTuplesOrderBy>>;
};

export type CreateDeckShareInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  deckShare: DeckShareInput;
};

export type CreateDeckSharePayload = {
  __typename?: 'CreateDeckSharePayload';
  clientMutationId?: Maybe<Scalars['String']>;
  deckByDeckId?: Maybe<Deck>;
  deckShare?: Maybe<DeckShare>;
  deckShareEdge?: Maybe<DeckSharesEdge>;
  query?: Maybe<Query>;
};


export type CreateDeckSharePayloadDeckShareEdgeArgs = {
  orderBy?: InputMaybe<Array<DeckSharesOrderBy>>;
};

export type CreateFriendInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  friend: FriendInput;
};

export type CreateFriendPayload = {
  __typename?: 'CreateFriendPayload';
  clientMutationId?: Maybe<Scalars['String']>;
  friend?: Maybe<Friend>;
  friendEdge?: Maybe<FriendsEdge>;
  query?: Maybe<Query>;
};


export type CreateFriendPayloadFriendEdgeArgs = {
  orderBy?: InputMaybe<Array<FriendsOrderBy>>;
};

export type CreateGameInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  game: GameInput;
};

export type CreateGamePayload = {
  __typename?: 'CreateGamePayload';
  clientMutationId?: Maybe<Scalars['String']>;
  game?: Maybe<Game>;
  gameEdge?: Maybe<GamesEdge>;
  query?: Maybe<Query>;
};


export type CreateGamePayloadGameEdgeArgs = {
  orderBy?: InputMaybe<Array<GamesOrderBy>>;
};

export type CreateGameUserInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  gameUser: GameUserInput;
};

export type CreateGameUserPayload = {
  __typename?: 'CreateGameUserPayload';
  clientMutationId?: Maybe<Scalars['String']>;
  deckByDeckId?: Maybe<Deck>;
  gameByGameId?: Maybe<Game>;
  gameUser?: Maybe<GameUser>;
  gameUserEdge?: Maybe<GameUsersEdge>;
  query?: Maybe<Query>;
};


export type CreateGameUserPayloadGameUserEdgeArgs = {
  orderBy?: InputMaybe<Array<GameUsersOrderBy>>;
};

export type CreateGuestInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  guest: GuestInput;
};

export type CreateGuestPayload = {
  __typename?: 'CreateGuestPayload';
  clientMutationId?: Maybe<Scalars['String']>;
  guest?: Maybe<Guest>;
  guestEdge?: Maybe<GuestsEdge>;
  query?: Maybe<Query>;
};


export type CreateGuestPayloadGuestEdgeArgs = {
  orderBy?: InputMaybe<Array<GuestsOrderBy>>;
};

export type CreateMatchmakingQueueInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  matchmakingQueue: MatchmakingQueueInput;
};

export type CreateMatchmakingQueuePayload = {
  __typename?: 'CreateMatchmakingQueuePayload';
  clientMutationId?: Maybe<Scalars['String']>;
  matchmakingQueue?: Maybe<MatchmakingQueue>;
  matchmakingQueueEdge?: Maybe<MatchmakingQueuesEdge>;
  query?: Maybe<Query>;
};


export type CreateMatchmakingQueuePayloadMatchmakingQueueEdgeArgs = {
  orderBy?: InputMaybe<Array<MatchmakingQueuesOrderBy>>;
};

export type CreateMatchmakingTicketInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  matchmakingTicket: MatchmakingTicketInput;
};

export type CreateMatchmakingTicketPayload = {
  __typename?: 'CreateMatchmakingTicketPayload';
  clientMutationId?: Maybe<Scalars['String']>;
  deckByBotDeckId?: Maybe<Deck>;
  deckByDeckId?: Maybe<Deck>;
  matchmakingQueueByQueueId?: Maybe<MatchmakingQueue>;
  matchmakingTicket?: Maybe<MatchmakingTicket>;
  matchmakingTicketEdge?: Maybe<MatchmakingTicketsEdge>;
  query?: Maybe<Query>;
};


export type CreateMatchmakingTicketPayloadMatchmakingTicketEdgeArgs = {
  orderBy?: InputMaybe<Array<MatchmakingTicketsOrderBy>>;
};

export type CreateUserEntityAddonInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  userEntityAddon: UserEntityAddonInput;
};

export type CreateUserEntityAddonPayload = {
  __typename?: 'CreateUserEntityAddonPayload';
  clientMutationId?: Maybe<Scalars['String']>;
  query?: Maybe<Query>;
  userEntityAddon?: Maybe<UserEntityAddon>;
  userEntityAddonEdge?: Maybe<UserEntityAddonsEdge>;
};


export type CreateUserEntityAddonPayloadUserEntityAddonEdgeArgs = {
  orderBy?: InputMaybe<Array<UserEntityAddonsOrderBy>>;
};

export type Deck = Node & {
  __typename?: 'Deck';
  cardsInDecksByDeckId: CardsInDecksConnection;
  createdBy: Scalars['String'];
  deckPlayerAttributeTuplesByDeckId: DeckPlayerAttributeTuplesConnection;
  deckSharesByDeckId: DeckSharesConnection;
  deckType: Scalars['Int'];
  format?: Maybe<Scalars['String']>;
  gameUsersByDeckId: GameUsersConnection;
  heroClass?: Maybe<Scalars['String']>;
  id: Scalars['String'];
  isPremade: Scalars['Boolean'];
  lastEditedBy: Scalars['String'];
  matchmakingTicketsByBotDeckId: MatchmakingTicketsConnection;
  matchmakingTicketsByDeckId: MatchmakingTicketsConnection;
  name?: Maybe<Scalars['String']>;
  nodeId: Scalars['ID'];
  permittedToDuplicate: Scalars['Boolean'];
  trashed: Scalars['Boolean'];
};


export type DeckCardsInDecksByDeckIdArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<CardsInDeckCondition>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<CardsInDecksOrderBy>>;
};


export type DeckDeckPlayerAttributeTuplesByDeckIdArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<DeckPlayerAttributeTupleCondition>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<DeckPlayerAttributeTuplesOrderBy>>;
};


export type DeckDeckSharesByDeckIdArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<DeckShareCondition>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<DeckSharesOrderBy>>;
};


export type DeckGameUsersByDeckIdArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<GameUserCondition>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<GameUsersOrderBy>>;
};


export type DeckMatchmakingTicketsByBotDeckIdArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<MatchmakingTicketCondition>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<MatchmakingTicketsOrderBy>>;
};


export type DeckMatchmakingTicketsByDeckIdArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<MatchmakingTicketCondition>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<MatchmakingTicketsOrderBy>>;
};

export type DeckCondition = {
  createdBy?: InputMaybe<Scalars['String']>;
  deckType?: InputMaybe<Scalars['Int']>;
  format?: InputMaybe<Scalars['String']>;
  heroClass?: InputMaybe<Scalars['String']>;
  id?: InputMaybe<Scalars['String']>;
  isPremade?: InputMaybe<Scalars['Boolean']>;
  lastEditedBy?: InputMaybe<Scalars['String']>;
  name?: InputMaybe<Scalars['String']>;
  permittedToDuplicate?: InputMaybe<Scalars['Boolean']>;
  trashed?: InputMaybe<Scalars['Boolean']>;
};

export type DeckInput = {
  createdBy: Scalars['String'];
  deckType: Scalars['Int'];
  format?: InputMaybe<Scalars['String']>;
  heroClass?: InputMaybe<Scalars['String']>;
  id: Scalars['String'];
  isPremade?: InputMaybe<Scalars['Boolean']>;
  lastEditedBy: Scalars['String'];
  name?: InputMaybe<Scalars['String']>;
  permittedToDuplicate?: InputMaybe<Scalars['Boolean']>;
  trashed?: InputMaybe<Scalars['Boolean']>;
};

export type DeckPatch = {
  createdBy?: InputMaybe<Scalars['String']>;
  deckType?: InputMaybe<Scalars['Int']>;
  format?: InputMaybe<Scalars['String']>;
  heroClass?: InputMaybe<Scalars['String']>;
  id?: InputMaybe<Scalars['String']>;
  isPremade?: InputMaybe<Scalars['Boolean']>;
  lastEditedBy?: InputMaybe<Scalars['String']>;
  name?: InputMaybe<Scalars['String']>;
  permittedToDuplicate?: InputMaybe<Scalars['Boolean']>;
  trashed?: InputMaybe<Scalars['Boolean']>;
};

export type DeckPlayerAttributeTuple = Node & {
  __typename?: 'DeckPlayerAttributeTuple';
  attribute: Scalars['Int'];
  deckByDeckId?: Maybe<Deck>;
  deckId: Scalars['String'];
  id: Scalars['BigInt'];
  nodeId: Scalars['ID'];
  stringValue?: Maybe<Scalars['String']>;
};

export type DeckPlayerAttributeTupleCondition = {
  attribute?: InputMaybe<Scalars['Int']>;
  deckId?: InputMaybe<Scalars['String']>;
  id?: InputMaybe<Scalars['BigInt']>;
  stringValue?: InputMaybe<Scalars['String']>;
};

export type DeckPlayerAttributeTupleInput = {
  attribute: Scalars['Int'];
  deckId: Scalars['String'];
  stringValue?: InputMaybe<Scalars['String']>;
};

export type DeckPlayerAttributeTuplePatch = {
  attribute?: InputMaybe<Scalars['Int']>;
  deckId?: InputMaybe<Scalars['String']>;
  stringValue?: InputMaybe<Scalars['String']>;
};

export type DeckPlayerAttributeTuplesConnection = {
  __typename?: 'DeckPlayerAttributeTuplesConnection';
  edges: Array<DeckPlayerAttributeTuplesEdge>;
  nodes: Array<Maybe<DeckPlayerAttributeTuple>>;
  pageInfo: PageInfo;
  totalCount: Scalars['Int'];
};

export type DeckPlayerAttributeTuplesEdge = {
  __typename?: 'DeckPlayerAttributeTuplesEdge';
  cursor?: Maybe<Scalars['Cursor']>;
  node?: Maybe<DeckPlayerAttributeTuple>;
};

export const DeckPlayerAttributeTuplesOrderBy = {
  AttributeAsc: 'ATTRIBUTE_ASC',
  AttributeDesc: 'ATTRIBUTE_DESC',
  DeckIdAsc: 'DECK_ID_ASC',
  DeckIdDesc: 'DECK_ID_DESC',
  IdAsc: 'ID_ASC',
  IdDesc: 'ID_DESC',
  Natural: 'NATURAL',
  PrimaryKeyAsc: 'PRIMARY_KEY_ASC',
  PrimaryKeyDesc: 'PRIMARY_KEY_DESC',
  StringValueAsc: 'STRING_VALUE_ASC',
  StringValueDesc: 'STRING_VALUE_DESC'
} as const;

export type DeckPlayerAttributeTuplesOrderBy = typeof DeckPlayerAttributeTuplesOrderBy[keyof typeof DeckPlayerAttributeTuplesOrderBy];
export type DeckShare = Node & {
  __typename?: 'DeckShare';
  deckByDeckId?: Maybe<Deck>;
  deckId: Scalars['String'];
  nodeId: Scalars['ID'];
  shareRecipientId: Scalars['String'];
  trashedByRecipient: Scalars['Boolean'];
};

export type DeckShareCondition = {
  deckId?: InputMaybe<Scalars['String']>;
  shareRecipientId?: InputMaybe<Scalars['String']>;
  trashedByRecipient?: InputMaybe<Scalars['Boolean']>;
};

export type DeckShareInput = {
  deckId: Scalars['String'];
  shareRecipientId: Scalars['String'];
  trashedByRecipient?: InputMaybe<Scalars['Boolean']>;
};

export type DeckSharePatch = {
  deckId?: InputMaybe<Scalars['String']>;
  shareRecipientId?: InputMaybe<Scalars['String']>;
  trashedByRecipient?: InputMaybe<Scalars['Boolean']>;
};

export type DeckSharesConnection = {
  __typename?: 'DeckSharesConnection';
  edges: Array<DeckSharesEdge>;
  nodes: Array<Maybe<DeckShare>>;
  pageInfo: PageInfo;
  totalCount: Scalars['Int'];
};

export type DeckSharesEdge = {
  __typename?: 'DeckSharesEdge';
  cursor?: Maybe<Scalars['Cursor']>;
  node?: Maybe<DeckShare>;
};

export const DeckSharesOrderBy = {
  DeckIdAsc: 'DECK_ID_ASC',
  DeckIdDesc: 'DECK_ID_DESC',
  Natural: 'NATURAL',
  PrimaryKeyAsc: 'PRIMARY_KEY_ASC',
  PrimaryKeyDesc: 'PRIMARY_KEY_DESC',
  ShareRecipientIdAsc: 'SHARE_RECIPIENT_ID_ASC',
  ShareRecipientIdDesc: 'SHARE_RECIPIENT_ID_DESC',
  TrashedByRecipientAsc: 'TRASHED_BY_RECIPIENT_ASC',
  TrashedByRecipientDesc: 'TRASHED_BY_RECIPIENT_DESC'
} as const;

export type DeckSharesOrderBy = typeof DeckSharesOrderBy[keyof typeof DeckSharesOrderBy];
export type DecksConnection = {
  __typename?: 'DecksConnection';
  edges: Array<DecksEdge>;
  nodes: Array<Maybe<Deck>>;
  pageInfo: PageInfo;
  totalCount: Scalars['Int'];
};

export type DecksEdge = {
  __typename?: 'DecksEdge';
  cursor?: Maybe<Scalars['Cursor']>;
  node?: Maybe<Deck>;
};

export const DecksOrderBy = {
  CreatedByAsc: 'CREATED_BY_ASC',
  CreatedByDesc: 'CREATED_BY_DESC',
  DeckTypeAsc: 'DECK_TYPE_ASC',
  DeckTypeDesc: 'DECK_TYPE_DESC',
  FormatAsc: 'FORMAT_ASC',
  FormatDesc: 'FORMAT_DESC',
  HeroClassAsc: 'HERO_CLASS_ASC',
  HeroClassDesc: 'HERO_CLASS_DESC',
  IdAsc: 'ID_ASC',
  IdDesc: 'ID_DESC',
  IsPremadeAsc: 'IS_PREMADE_ASC',
  IsPremadeDesc: 'IS_PREMADE_DESC',
  LastEditedByAsc: 'LAST_EDITED_BY_ASC',
  LastEditedByDesc: 'LAST_EDITED_BY_DESC',
  NameAsc: 'NAME_ASC',
  NameDesc: 'NAME_DESC',
  Natural: 'NATURAL',
  PermittedToDuplicateAsc: 'PERMITTED_TO_DUPLICATE_ASC',
  PermittedToDuplicateDesc: 'PERMITTED_TO_DUPLICATE_DESC',
  PrimaryKeyAsc: 'PRIMARY_KEY_ASC',
  PrimaryKeyDesc: 'PRIMARY_KEY_DESC',
  TrashedAsc: 'TRASHED_ASC',
  TrashedDesc: 'TRASHED_DESC'
} as const;

export type DecksOrderBy = typeof DecksOrderBy[keyof typeof DecksOrderBy];
export type DeleteBotUserByIdInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  id: Scalars['String'];
};

export type DeleteBotUserInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  nodeId: Scalars['ID'];
};

export type DeleteBotUserPayload = {
  __typename?: 'DeleteBotUserPayload';
  botUser?: Maybe<BotUser>;
  botUserEdge?: Maybe<BotUsersEdge>;
  clientMutationId?: Maybe<Scalars['String']>;
  deletedBotUserId?: Maybe<Scalars['ID']>;
  query?: Maybe<Query>;
};


export type DeleteBotUserPayloadBotUserEdgeArgs = {
  orderBy?: InputMaybe<Array<BotUsersOrderBy>>;
};

export type DeleteCardByIdInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  id: Scalars['String'];
};

export type DeleteCardInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  nodeId: Scalars['ID'];
};

export type DeleteCardPayload = {
  __typename?: 'DeleteCardPayload';
  card?: Maybe<Card>;
  cardEdge?: Maybe<CardsEdge>;
  clientMutationId?: Maybe<Scalars['String']>;
  deletedCardId?: Maybe<Scalars['ID']>;
  query?: Maybe<Query>;
};


export type DeleteCardPayloadCardEdgeArgs = {
  orderBy?: InputMaybe<Array<CardsOrderBy>>;
};

export type DeleteCardsInDeckByIdInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  id: Scalars['BigInt'];
};

export type DeleteCardsInDeckInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  nodeId: Scalars['ID'];
};

export type DeleteCardsInDeckPayload = {
  __typename?: 'DeleteCardsInDeckPayload';
  cardByCardId?: Maybe<Card>;
  cardsInDeck?: Maybe<CardsInDeck>;
  cardsInDeckEdge?: Maybe<CardsInDecksEdge>;
  clientMutationId?: Maybe<Scalars['String']>;
  deckByDeckId?: Maybe<Deck>;
  deletedCardsInDeckId?: Maybe<Scalars['ID']>;
  query?: Maybe<Query>;
};


export type DeleteCardsInDeckPayloadCardsInDeckEdgeArgs = {
  orderBy?: InputMaybe<Array<CardsInDecksOrderBy>>;
};

export type DeleteDeckByIdInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  id: Scalars['String'];
};

export type DeleteDeckInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  nodeId: Scalars['ID'];
};

export type DeleteDeckPayload = {
  __typename?: 'DeleteDeckPayload';
  clientMutationId?: Maybe<Scalars['String']>;
  deck?: Maybe<Deck>;
  deckEdge?: Maybe<DecksEdge>;
  deletedDeckId?: Maybe<Scalars['ID']>;
  query?: Maybe<Query>;
};


export type DeleteDeckPayloadDeckEdgeArgs = {
  orderBy?: InputMaybe<Array<DecksOrderBy>>;
};

export type DeleteDeckPlayerAttributeTupleByIdInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  id: Scalars['BigInt'];
};

export type DeleteDeckPlayerAttributeTupleInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  nodeId: Scalars['ID'];
};

export type DeleteDeckPlayerAttributeTuplePayload = {
  __typename?: 'DeleteDeckPlayerAttributeTuplePayload';
  clientMutationId?: Maybe<Scalars['String']>;
  deckByDeckId?: Maybe<Deck>;
  deckPlayerAttributeTuple?: Maybe<DeckPlayerAttributeTuple>;
  deckPlayerAttributeTupleEdge?: Maybe<DeckPlayerAttributeTuplesEdge>;
  deletedDeckPlayerAttributeTupleId?: Maybe<Scalars['ID']>;
  query?: Maybe<Query>;
};


export type DeleteDeckPlayerAttributeTuplePayloadDeckPlayerAttributeTupleEdgeArgs = {
  orderBy?: InputMaybe<Array<DeckPlayerAttributeTuplesOrderBy>>;
};

export type DeleteDeckShareByDeckIdAndShareRecipientIdInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  deckId: Scalars['String'];
  shareRecipientId: Scalars['String'];
};

export type DeleteDeckShareInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  nodeId: Scalars['ID'];
};

export type DeleteDeckSharePayload = {
  __typename?: 'DeleteDeckSharePayload';
  clientMutationId?: Maybe<Scalars['String']>;
  deckByDeckId?: Maybe<Deck>;
  deckShare?: Maybe<DeckShare>;
  deckShareEdge?: Maybe<DeckSharesEdge>;
  deletedDeckShareId?: Maybe<Scalars['ID']>;
  query?: Maybe<Query>;
};


export type DeleteDeckSharePayloadDeckShareEdgeArgs = {
  orderBy?: InputMaybe<Array<DeckSharesOrderBy>>;
};

export type DeleteFriendByIdAndFriendInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  friend: Scalars['String'];
  id: Scalars['String'];
};

export type DeleteFriendInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  nodeId: Scalars['ID'];
};

export type DeleteFriendPayload = {
  __typename?: 'DeleteFriendPayload';
  clientMutationId?: Maybe<Scalars['String']>;
  deletedFriendId?: Maybe<Scalars['ID']>;
  friend?: Maybe<Friend>;
  friendEdge?: Maybe<FriendsEdge>;
  query?: Maybe<Query>;
};


export type DeleteFriendPayloadFriendEdgeArgs = {
  orderBy?: InputMaybe<Array<FriendsOrderBy>>;
};

export type DeleteGameByIdInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  id: Scalars['BigInt'];
};

export type DeleteGameInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  nodeId: Scalars['ID'];
};

export type DeleteGamePayload = {
  __typename?: 'DeleteGamePayload';
  clientMutationId?: Maybe<Scalars['String']>;
  deletedGameId?: Maybe<Scalars['ID']>;
  game?: Maybe<Game>;
  gameEdge?: Maybe<GamesEdge>;
  query?: Maybe<Query>;
};


export type DeleteGamePayloadGameEdgeArgs = {
  orderBy?: InputMaybe<Array<GamesOrderBy>>;
};

export type DeleteGameUserByGameIdAndUserIdInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  gameId: Scalars['BigInt'];
  userId: Scalars['String'];
};

export type DeleteGameUserInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  nodeId: Scalars['ID'];
};

export type DeleteGameUserPayload = {
  __typename?: 'DeleteGameUserPayload';
  clientMutationId?: Maybe<Scalars['String']>;
  deckByDeckId?: Maybe<Deck>;
  deletedGameUserId?: Maybe<Scalars['ID']>;
  gameByGameId?: Maybe<Game>;
  gameUser?: Maybe<GameUser>;
  gameUserEdge?: Maybe<GameUsersEdge>;
  query?: Maybe<Query>;
};


export type DeleteGameUserPayloadGameUserEdgeArgs = {
  orderBy?: InputMaybe<Array<GameUsersOrderBy>>;
};

export type DeleteGuestByIdInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  id: Scalars['BigInt'];
};

export type DeleteGuestInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  nodeId: Scalars['ID'];
};

export type DeleteGuestPayload = {
  __typename?: 'DeleteGuestPayload';
  clientMutationId?: Maybe<Scalars['String']>;
  deletedGuestId?: Maybe<Scalars['ID']>;
  guest?: Maybe<Guest>;
  guestEdge?: Maybe<GuestsEdge>;
  query?: Maybe<Query>;
};


export type DeleteGuestPayloadGuestEdgeArgs = {
  orderBy?: InputMaybe<Array<GuestsOrderBy>>;
};

export type DeleteMatchmakingQueueByIdInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  id: Scalars['String'];
};

export type DeleteMatchmakingQueueInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  nodeId: Scalars['ID'];
};

export type DeleteMatchmakingQueuePayload = {
  __typename?: 'DeleteMatchmakingQueuePayload';
  clientMutationId?: Maybe<Scalars['String']>;
  deletedMatchmakingQueueId?: Maybe<Scalars['ID']>;
  matchmakingQueue?: Maybe<MatchmakingQueue>;
  matchmakingQueueEdge?: Maybe<MatchmakingQueuesEdge>;
  query?: Maybe<Query>;
};


export type DeleteMatchmakingQueuePayloadMatchmakingQueueEdgeArgs = {
  orderBy?: InputMaybe<Array<MatchmakingQueuesOrderBy>>;
};

export type DeleteMatchmakingTicketByUserIdInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  userId: Scalars['String'];
};

export type DeleteMatchmakingTicketInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  nodeId: Scalars['ID'];
};

export type DeleteMatchmakingTicketPayload = {
  __typename?: 'DeleteMatchmakingTicketPayload';
  clientMutationId?: Maybe<Scalars['String']>;
  deckByBotDeckId?: Maybe<Deck>;
  deckByDeckId?: Maybe<Deck>;
  deletedMatchmakingTicketId?: Maybe<Scalars['ID']>;
  matchmakingQueueByQueueId?: Maybe<MatchmakingQueue>;
  matchmakingTicket?: Maybe<MatchmakingTicket>;
  matchmakingTicketEdge?: Maybe<MatchmakingTicketsEdge>;
  query?: Maybe<Query>;
};


export type DeleteMatchmakingTicketPayloadMatchmakingTicketEdgeArgs = {
  orderBy?: InputMaybe<Array<MatchmakingTicketsOrderBy>>;
};

export type DeleteUserEntityAddonByIdInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  id: Scalars['String'];
};

export type DeleteUserEntityAddonInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  nodeId: Scalars['ID'];
};

export type DeleteUserEntityAddonPayload = {
  __typename?: 'DeleteUserEntityAddonPayload';
  clientMutationId?: Maybe<Scalars['String']>;
  deletedUserEntityAddonId?: Maybe<Scalars['ID']>;
  query?: Maybe<Query>;
  userEntityAddon?: Maybe<UserEntityAddon>;
  userEntityAddonEdge?: Maybe<UserEntityAddonsEdge>;
};


export type DeleteUserEntityAddonPayloadUserEntityAddonEdgeArgs = {
  orderBy?: InputMaybe<Array<UserEntityAddonsOrderBy>>;
};

export type Friend = Node & {
  __typename?: 'Friend';
  createdAt: Scalars['Datetime'];
  friend: Scalars['String'];
  id: Scalars['String'];
  nodeId: Scalars['ID'];
};

export type FriendCondition = {
  createdAt?: InputMaybe<Scalars['Datetime']>;
  friend?: InputMaybe<Scalars['String']>;
  id?: InputMaybe<Scalars['String']>;
};

export type FriendInput = {
  createdAt?: InputMaybe<Scalars['Datetime']>;
  friend: Scalars['String'];
  id: Scalars['String'];
};

export type FriendPatch = {
  createdAt?: InputMaybe<Scalars['Datetime']>;
  friend?: InputMaybe<Scalars['String']>;
  id?: InputMaybe<Scalars['String']>;
};

export type FriendsConnection = {
  __typename?: 'FriendsConnection';
  edges: Array<FriendsEdge>;
  nodes: Array<Maybe<Friend>>;
  pageInfo: PageInfo;
  totalCount: Scalars['Int'];
};

export type FriendsEdge = {
  __typename?: 'FriendsEdge';
  cursor?: Maybe<Scalars['Cursor']>;
  node?: Maybe<Friend>;
};

export const FriendsOrderBy = {
  CreatedAtAsc: 'CREATED_AT_ASC',
  CreatedAtDesc: 'CREATED_AT_DESC',
  FriendAsc: 'FRIEND_ASC',
  FriendDesc: 'FRIEND_DESC',
  IdAsc: 'ID_ASC',
  IdDesc: 'ID_DESC',
  Natural: 'NATURAL',
  PrimaryKeyAsc: 'PRIMARY_KEY_ASC',
  PrimaryKeyDesc: 'PRIMARY_KEY_DESC'
} as const;

export type FriendsOrderBy = typeof FriendsOrderBy[keyof typeof FriendsOrderBy];
export type Game = Node & {
  __typename?: 'Game';
  createdAt: Scalars['Datetime'];
  gameUsersByGameId: GameUsersConnection;
  gitHash?: Maybe<Scalars['String']>;
  id: Scalars['BigInt'];
  nodeId: Scalars['ID'];
  status: GameStateEnum;
  trace?: Maybe<Scalars['JSON']>;
};


export type GameGameUsersByGameIdArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<GameUserCondition>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<GameUsersOrderBy>>;
};

export type GameCondition = {
  createdAt?: InputMaybe<Scalars['Datetime']>;
  gitHash?: InputMaybe<Scalars['String']>;
  id?: InputMaybe<Scalars['BigInt']>;
  status?: InputMaybe<GameStateEnum>;
  trace?: InputMaybe<Scalars['JSON']>;
};

export type GameInput = {
  createdAt?: InputMaybe<Scalars['Datetime']>;
  gitHash?: InputMaybe<Scalars['String']>;
  status?: InputMaybe<GameStateEnum>;
  trace?: InputMaybe<Scalars['JSON']>;
};

export type GamePatch = {
  createdAt?: InputMaybe<Scalars['Datetime']>;
  gitHash?: InputMaybe<Scalars['String']>;
  status?: InputMaybe<GameStateEnum>;
  trace?: InputMaybe<Scalars['JSON']>;
};

export const GameStateEnum = {
  AwaitingConnections: 'AWAITING_CONNECTIONS',
  Finished: 'FINISHED',
  Started: 'STARTED'
} as const;

export type GameStateEnum = typeof GameStateEnum[keyof typeof GameStateEnum];
export type GameUser = Node & {
  __typename?: 'GameUser';
  deckByDeckId?: Maybe<Deck>;
  deckId?: Maybe<Scalars['String']>;
  gameByGameId?: Maybe<Game>;
  gameId: Scalars['BigInt'];
  nodeId: Scalars['ID'];
  playerIndex?: Maybe<Scalars['Int']>;
  userId: Scalars['String'];
  victoryStatus: GameUserVictoryEnum;
};

export type GameUserCondition = {
  deckId?: InputMaybe<Scalars['String']>;
  gameId?: InputMaybe<Scalars['BigInt']>;
  playerIndex?: InputMaybe<Scalars['Int']>;
  userId?: InputMaybe<Scalars['String']>;
  victoryStatus?: InputMaybe<GameUserVictoryEnum>;
};

export type GameUserInput = {
  deckId?: InputMaybe<Scalars['String']>;
  gameId: Scalars['BigInt'];
  playerIndex?: InputMaybe<Scalars['Int']>;
  userId: Scalars['String'];
  victoryStatus?: InputMaybe<GameUserVictoryEnum>;
};

export type GameUserPatch = {
  deckId?: InputMaybe<Scalars['String']>;
  gameId?: InputMaybe<Scalars['BigInt']>;
  playerIndex?: InputMaybe<Scalars['Int']>;
  userId?: InputMaybe<Scalars['String']>;
  victoryStatus?: InputMaybe<GameUserVictoryEnum>;
};

export const GameUserVictoryEnum = {
  Conceded: 'CONCEDED',
  Disconnected: 'DISCONNECTED',
  Lost: 'LOST',
  Unknown: 'UNKNOWN',
  Won: 'WON'
} as const;

export type GameUserVictoryEnum = typeof GameUserVictoryEnum[keyof typeof GameUserVictoryEnum];
export type GameUsersConnection = {
  __typename?: 'GameUsersConnection';
  edges: Array<GameUsersEdge>;
  nodes: Array<Maybe<GameUser>>;
  pageInfo: PageInfo;
  totalCount: Scalars['Int'];
};

export type GameUsersEdge = {
  __typename?: 'GameUsersEdge';
  cursor?: Maybe<Scalars['Cursor']>;
  node?: Maybe<GameUser>;
};

export const GameUsersOrderBy = {
  DeckIdAsc: 'DECK_ID_ASC',
  DeckIdDesc: 'DECK_ID_DESC',
  GameIdAsc: 'GAME_ID_ASC',
  GameIdDesc: 'GAME_ID_DESC',
  Natural: 'NATURAL',
  PlayerIndexAsc: 'PLAYER_INDEX_ASC',
  PlayerIndexDesc: 'PLAYER_INDEX_DESC',
  PrimaryKeyAsc: 'PRIMARY_KEY_ASC',
  PrimaryKeyDesc: 'PRIMARY_KEY_DESC',
  UserIdAsc: 'USER_ID_ASC',
  UserIdDesc: 'USER_ID_DESC',
  VictoryStatusAsc: 'VICTORY_STATUS_ASC',
  VictoryStatusDesc: 'VICTORY_STATUS_DESC'
} as const;

export type GameUsersOrderBy = typeof GameUsersOrderBy[keyof typeof GameUsersOrderBy];
export type GamesConnection = {
  __typename?: 'GamesConnection';
  edges: Array<GamesEdge>;
  nodes: Array<Maybe<Game>>;
  pageInfo: PageInfo;
  totalCount: Scalars['Int'];
};

export type GamesEdge = {
  __typename?: 'GamesEdge';
  cursor?: Maybe<Scalars['Cursor']>;
  node?: Maybe<Game>;
};

export const GamesOrderBy = {
  CreatedAtAsc: 'CREATED_AT_ASC',
  CreatedAtDesc: 'CREATED_AT_DESC',
  GitHashAsc: 'GIT_HASH_ASC',
  GitHashDesc: 'GIT_HASH_DESC',
  IdAsc: 'ID_ASC',
  IdDesc: 'ID_DESC',
  Natural: 'NATURAL',
  PrimaryKeyAsc: 'PRIMARY_KEY_ASC',
  PrimaryKeyDesc: 'PRIMARY_KEY_DESC',
  StatusAsc: 'STATUS_ASC',
  StatusDesc: 'STATUS_DESC',
  TraceAsc: 'TRACE_ASC',
  TraceDesc: 'TRACE_DESC'
} as const;

export type GamesOrderBy = typeof GamesOrderBy[keyof typeof GamesOrderBy];
export type Guest = Node & {
  __typename?: 'Guest';
  id: Scalars['BigInt'];
  nodeId: Scalars['ID'];
  userId?: Maybe<Scalars['String']>;
};

export type GuestCondition = {
  id?: InputMaybe<Scalars['BigInt']>;
  userId?: InputMaybe<Scalars['String']>;
};

export type GuestInput = {
  userId?: InputMaybe<Scalars['String']>;
};

export type GuestPatch = {
  userId?: InputMaybe<Scalars['String']>;
};

export type GuestsConnection = {
  __typename?: 'GuestsConnection';
  edges: Array<GuestsEdge>;
  nodes: Array<Maybe<Guest>>;
  pageInfo: PageInfo;
  totalCount: Scalars['Int'];
};

export type GuestsEdge = {
  __typename?: 'GuestsEdge';
  cursor?: Maybe<Scalars['Cursor']>;
  node?: Maybe<Guest>;
};

export const GuestsOrderBy = {
  IdAsc: 'ID_ASC',
  IdDesc: 'ID_DESC',
  Natural: 'NATURAL',
  PrimaryKeyAsc: 'PRIMARY_KEY_ASC',
  PrimaryKeyDesc: 'PRIMARY_KEY_DESC',
  UserIdAsc: 'USER_ID_ASC',
  UserIdDesc: 'USER_ID_DESC'
} as const;

export type GuestsOrderBy = typeof GuestsOrderBy[keyof typeof GuestsOrderBy];
export type ImageDef = {
  __typename?: 'ImageDef';
  height: Scalars['Int'];
  id: Scalars['String'];
  name: Scalars['String'];
  src: Scalars['String'];
  width: Scalars['Int'];
};

export type MatchmakingQueue = Node & {
  __typename?: 'MatchmakingQueue';
  automaticallyClose: Scalars['Boolean'];
  awaitingLobbyTimeout: Scalars['BigInt'];
  botOpponent: Scalars['Boolean'];
  emptyLobbyTimeout: Scalars['BigInt'];
  id: Scalars['String'];
  lobbySize: Scalars['Int'];
  matchmakingTicketsByQueueId: MatchmakingTicketsConnection;
  name: Scalars['String'];
  nodeId: Scalars['ID'];
  once: Scalars['Boolean'];
  privateLobby: Scalars['Boolean'];
  queueCreatedAt: Scalars['Datetime'];
  startsAutomatically: Scalars['Boolean'];
  stillConnectedTimeout: Scalars['BigInt'];
};


export type MatchmakingQueueMatchmakingTicketsByQueueIdArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<MatchmakingTicketCondition>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<MatchmakingTicketsOrderBy>>;
};

export type MatchmakingQueueCondition = {
  automaticallyClose?: InputMaybe<Scalars['Boolean']>;
  awaitingLobbyTimeout?: InputMaybe<Scalars['BigInt']>;
  botOpponent?: InputMaybe<Scalars['Boolean']>;
  emptyLobbyTimeout?: InputMaybe<Scalars['BigInt']>;
  id?: InputMaybe<Scalars['String']>;
  lobbySize?: InputMaybe<Scalars['Int']>;
  name?: InputMaybe<Scalars['String']>;
  once?: InputMaybe<Scalars['Boolean']>;
  privateLobby?: InputMaybe<Scalars['Boolean']>;
  queueCreatedAt?: InputMaybe<Scalars['Datetime']>;
  startsAutomatically?: InputMaybe<Scalars['Boolean']>;
  stillConnectedTimeout?: InputMaybe<Scalars['BigInt']>;
};

export type MatchmakingQueueInput = {
  automaticallyClose?: InputMaybe<Scalars['Boolean']>;
  awaitingLobbyTimeout?: InputMaybe<Scalars['BigInt']>;
  botOpponent?: InputMaybe<Scalars['Boolean']>;
  emptyLobbyTimeout?: InputMaybe<Scalars['BigInt']>;
  id: Scalars['String'];
  lobbySize?: InputMaybe<Scalars['Int']>;
  name: Scalars['String'];
  once?: InputMaybe<Scalars['Boolean']>;
  privateLobby?: InputMaybe<Scalars['Boolean']>;
  queueCreatedAt?: InputMaybe<Scalars['Datetime']>;
  startsAutomatically?: InputMaybe<Scalars['Boolean']>;
  stillConnectedTimeout?: InputMaybe<Scalars['BigInt']>;
};

export type MatchmakingQueuePatch = {
  automaticallyClose?: InputMaybe<Scalars['Boolean']>;
  awaitingLobbyTimeout?: InputMaybe<Scalars['BigInt']>;
  botOpponent?: InputMaybe<Scalars['Boolean']>;
  emptyLobbyTimeout?: InputMaybe<Scalars['BigInt']>;
  id?: InputMaybe<Scalars['String']>;
  lobbySize?: InputMaybe<Scalars['Int']>;
  name?: InputMaybe<Scalars['String']>;
  once?: InputMaybe<Scalars['Boolean']>;
  privateLobby?: InputMaybe<Scalars['Boolean']>;
  queueCreatedAt?: InputMaybe<Scalars['Datetime']>;
  startsAutomatically?: InputMaybe<Scalars['Boolean']>;
  stillConnectedTimeout?: InputMaybe<Scalars['BigInt']>;
};

export type MatchmakingQueuesConnection = {
  __typename?: 'MatchmakingQueuesConnection';
  edges: Array<MatchmakingQueuesEdge>;
  nodes: Array<Maybe<MatchmakingQueue>>;
  pageInfo: PageInfo;
  totalCount: Scalars['Int'];
};

export type MatchmakingQueuesEdge = {
  __typename?: 'MatchmakingQueuesEdge';
  cursor?: Maybe<Scalars['Cursor']>;
  node?: Maybe<MatchmakingQueue>;
};

export const MatchmakingQueuesOrderBy = {
  AutomaticallyCloseAsc: 'AUTOMATICALLY_CLOSE_ASC',
  AutomaticallyCloseDesc: 'AUTOMATICALLY_CLOSE_DESC',
  AwaitingLobbyTimeoutAsc: 'AWAITING_LOBBY_TIMEOUT_ASC',
  AwaitingLobbyTimeoutDesc: 'AWAITING_LOBBY_TIMEOUT_DESC',
  BotOpponentAsc: 'BOT_OPPONENT_ASC',
  BotOpponentDesc: 'BOT_OPPONENT_DESC',
  EmptyLobbyTimeoutAsc: 'EMPTY_LOBBY_TIMEOUT_ASC',
  EmptyLobbyTimeoutDesc: 'EMPTY_LOBBY_TIMEOUT_DESC',
  IdAsc: 'ID_ASC',
  IdDesc: 'ID_DESC',
  LobbySizeAsc: 'LOBBY_SIZE_ASC',
  LobbySizeDesc: 'LOBBY_SIZE_DESC',
  NameAsc: 'NAME_ASC',
  NameDesc: 'NAME_DESC',
  Natural: 'NATURAL',
  OnceAsc: 'ONCE_ASC',
  OnceDesc: 'ONCE_DESC',
  PrimaryKeyAsc: 'PRIMARY_KEY_ASC',
  PrimaryKeyDesc: 'PRIMARY_KEY_DESC',
  PrivateLobbyAsc: 'PRIVATE_LOBBY_ASC',
  PrivateLobbyDesc: 'PRIVATE_LOBBY_DESC',
  QueueCreatedAtAsc: 'QUEUE_CREATED_AT_ASC',
  QueueCreatedAtDesc: 'QUEUE_CREATED_AT_DESC',
  StartsAutomaticallyAsc: 'STARTS_AUTOMATICALLY_ASC',
  StartsAutomaticallyDesc: 'STARTS_AUTOMATICALLY_DESC',
  StillConnectedTimeoutAsc: 'STILL_CONNECTED_TIMEOUT_ASC',
  StillConnectedTimeoutDesc: 'STILL_CONNECTED_TIMEOUT_DESC'
} as const;

export type MatchmakingQueuesOrderBy = typeof MatchmakingQueuesOrderBy[keyof typeof MatchmakingQueuesOrderBy];
export type MatchmakingTicket = Node & {
  __typename?: 'MatchmakingTicket';
  botDeckId?: Maybe<Scalars['String']>;
  createdAt: Scalars['Datetime'];
  deckByBotDeckId?: Maybe<Deck>;
  deckByDeckId?: Maybe<Deck>;
  deckId?: Maybe<Scalars['String']>;
  matchmakingQueueByQueueId?: Maybe<MatchmakingQueue>;
  nodeId: Scalars['ID'];
  queueId?: Maybe<Scalars['String']>;
  ticketId: Scalars['BigInt'];
  userId: Scalars['String'];
};

export type MatchmakingTicketCondition = {
  botDeckId?: InputMaybe<Scalars['String']>;
  createdAt?: InputMaybe<Scalars['Datetime']>;
  deckId?: InputMaybe<Scalars['String']>;
  queueId?: InputMaybe<Scalars['String']>;
  ticketId?: InputMaybe<Scalars['BigInt']>;
  userId?: InputMaybe<Scalars['String']>;
};

export type MatchmakingTicketInput = {
  botDeckId?: InputMaybe<Scalars['String']>;
  createdAt?: InputMaybe<Scalars['Datetime']>;
  deckId?: InputMaybe<Scalars['String']>;
  queueId?: InputMaybe<Scalars['String']>;
  userId: Scalars['String'];
};

export type MatchmakingTicketPatch = {
  botDeckId?: InputMaybe<Scalars['String']>;
  createdAt?: InputMaybe<Scalars['Datetime']>;
  deckId?: InputMaybe<Scalars['String']>;
  queueId?: InputMaybe<Scalars['String']>;
  userId?: InputMaybe<Scalars['String']>;
};

export type MatchmakingTicketsConnection = {
  __typename?: 'MatchmakingTicketsConnection';
  edges: Array<MatchmakingTicketsEdge>;
  nodes: Array<Maybe<MatchmakingTicket>>;
  pageInfo: PageInfo;
  totalCount: Scalars['Int'];
};

export type MatchmakingTicketsEdge = {
  __typename?: 'MatchmakingTicketsEdge';
  cursor?: Maybe<Scalars['Cursor']>;
  node?: Maybe<MatchmakingTicket>;
};

export const MatchmakingTicketsOrderBy = {
  BotDeckIdAsc: 'BOT_DECK_ID_ASC',
  BotDeckIdDesc: 'BOT_DECK_ID_DESC',
  CreatedAtAsc: 'CREATED_AT_ASC',
  CreatedAtDesc: 'CREATED_AT_DESC',
  DeckIdAsc: 'DECK_ID_ASC',
  DeckIdDesc: 'DECK_ID_DESC',
  Natural: 'NATURAL',
  PrimaryKeyAsc: 'PRIMARY_KEY_ASC',
  PrimaryKeyDesc: 'PRIMARY_KEY_DESC',
  QueueIdAsc: 'QUEUE_ID_ASC',
  QueueIdDesc: 'QUEUE_ID_DESC',
  TicketIdAsc: 'TICKET_ID_ASC',
  TicketIdDesc: 'TICKET_ID_DESC',
  UserIdAsc: 'USER_ID_ASC',
  UserIdDesc: 'USER_ID_DESC'
} as const;

export type MatchmakingTicketsOrderBy = typeof MatchmakingTicketsOrderBy[keyof typeof MatchmakingTicketsOrderBy];
export type Mutation = {
  __typename?: 'Mutation';
  createBotUser?: Maybe<CreateBotUserPayload>;
  createCard?: Maybe<CreateCardPayload>;
  createCardsInDeck?: Maybe<CreateCardsInDeckPayload>;
  createDeck?: Maybe<CreateDeckPayload>;
  createDeckPlayerAttributeTuple?: Maybe<CreateDeckPlayerAttributeTuplePayload>;
  createDeckShare?: Maybe<CreateDeckSharePayload>;
  createFriend?: Maybe<CreateFriendPayload>;
  createGame?: Maybe<CreateGamePayload>;
  createGameUser?: Maybe<CreateGameUserPayload>;
  createGuest?: Maybe<CreateGuestPayload>;
  createMatchmakingQueue?: Maybe<CreateMatchmakingQueuePayload>;
  createMatchmakingTicket?: Maybe<CreateMatchmakingTicketPayload>;
  createUserEntityAddon?: Maybe<CreateUserEntityAddonPayload>;
  deleteBotUser?: Maybe<DeleteBotUserPayload>;
  deleteBotUserById?: Maybe<DeleteBotUserPayload>;
  deleteCard?: Maybe<DeleteCardPayload>;
  deleteCardById?: Maybe<DeleteCardPayload>;
  deleteCardsInDeck?: Maybe<DeleteCardsInDeckPayload>;
  deleteCardsInDeckById?: Maybe<DeleteCardsInDeckPayload>;
  deleteDeck?: Maybe<DeleteDeckPayload>;
  deleteDeckById?: Maybe<DeleteDeckPayload>;
  deleteDeckPlayerAttributeTuple?: Maybe<DeleteDeckPlayerAttributeTuplePayload>;
  deleteDeckPlayerAttributeTupleById?: Maybe<DeleteDeckPlayerAttributeTuplePayload>;
  deleteDeckShare?: Maybe<DeleteDeckSharePayload>;
  deleteDeckShareByDeckIdAndShareRecipientId?: Maybe<DeleteDeckSharePayload>;
  deleteFriend?: Maybe<DeleteFriendPayload>;
  deleteFriendByIdAndFriend?: Maybe<DeleteFriendPayload>;
  deleteGame?: Maybe<DeleteGamePayload>;
  deleteGameById?: Maybe<DeleteGamePayload>;
  deleteGameUser?: Maybe<DeleteGameUserPayload>;
  deleteGameUserByGameIdAndUserId?: Maybe<DeleteGameUserPayload>;
  deleteGuest?: Maybe<DeleteGuestPayload>;
  deleteGuestById?: Maybe<DeleteGuestPayload>;
  deleteMatchmakingQueue?: Maybe<DeleteMatchmakingQueuePayload>;
  deleteMatchmakingQueueById?: Maybe<DeleteMatchmakingQueuePayload>;
  deleteMatchmakingTicket?: Maybe<DeleteMatchmakingTicketPayload>;
  deleteMatchmakingTicketByUserId?: Maybe<DeleteMatchmakingTicketPayload>;
  deleteUserEntityAddon?: Maybe<DeleteUserEntityAddonPayload>;
  deleteUserEntityAddonById?: Maybe<DeleteUserEntityAddonPayload>;
  updateBotUser?: Maybe<UpdateBotUserPayload>;
  updateBotUserById?: Maybe<UpdateBotUserPayload>;
  updateCard?: Maybe<UpdateCardPayload>;
  updateCardById?: Maybe<UpdateCardPayload>;
  updateCardsInDeck?: Maybe<UpdateCardsInDeckPayload>;
  updateCardsInDeckById?: Maybe<UpdateCardsInDeckPayload>;
  updateDeck?: Maybe<UpdateDeckPayload>;
  updateDeckById?: Maybe<UpdateDeckPayload>;
  updateDeckPlayerAttributeTuple?: Maybe<UpdateDeckPlayerAttributeTuplePayload>;
  updateDeckPlayerAttributeTupleById?: Maybe<UpdateDeckPlayerAttributeTuplePayload>;
  updateDeckShare?: Maybe<UpdateDeckSharePayload>;
  updateDeckShareByDeckIdAndShareRecipientId?: Maybe<UpdateDeckSharePayload>;
  updateFriend?: Maybe<UpdateFriendPayload>;
  updateFriendByIdAndFriend?: Maybe<UpdateFriendPayload>;
  updateGame?: Maybe<UpdateGamePayload>;
  updateGameById?: Maybe<UpdateGamePayload>;
  updateGameUser?: Maybe<UpdateGameUserPayload>;
  updateGameUserByGameIdAndUserId?: Maybe<UpdateGameUserPayload>;
  updateGuest?: Maybe<UpdateGuestPayload>;
  updateGuestById?: Maybe<UpdateGuestPayload>;
  updateMatchmakingQueue?: Maybe<UpdateMatchmakingQueuePayload>;
  updateMatchmakingQueueById?: Maybe<UpdateMatchmakingQueuePayload>;
  updateMatchmakingTicket?: Maybe<UpdateMatchmakingTicketPayload>;
  updateMatchmakingTicketByUserId?: Maybe<UpdateMatchmakingTicketPayload>;
  updateUserEntityAddon?: Maybe<UpdateUserEntityAddonPayload>;
  updateUserEntityAddonById?: Maybe<UpdateUserEntityAddonPayload>;
};


export type MutationCreateBotUserArgs = {
  input: CreateBotUserInput;
};


export type MutationCreateCardArgs = {
  input: CreateCardInput;
};


export type MutationCreateCardsInDeckArgs = {
  input: CreateCardsInDeckInput;
};


export type MutationCreateDeckArgs = {
  input: CreateDeckInput;
};


export type MutationCreateDeckPlayerAttributeTupleArgs = {
  input: CreateDeckPlayerAttributeTupleInput;
};


export type MutationCreateDeckShareArgs = {
  input: CreateDeckShareInput;
};


export type MutationCreateFriendArgs = {
  input: CreateFriendInput;
};


export type MutationCreateGameArgs = {
  input: CreateGameInput;
};


export type MutationCreateGameUserArgs = {
  input: CreateGameUserInput;
};


export type MutationCreateGuestArgs = {
  input: CreateGuestInput;
};


export type MutationCreateMatchmakingQueueArgs = {
  input: CreateMatchmakingQueueInput;
};


export type MutationCreateMatchmakingTicketArgs = {
  input: CreateMatchmakingTicketInput;
};


export type MutationCreateUserEntityAddonArgs = {
  input: CreateUserEntityAddonInput;
};


export type MutationDeleteBotUserArgs = {
  input: DeleteBotUserInput;
};


export type MutationDeleteBotUserByIdArgs = {
  input: DeleteBotUserByIdInput;
};


export type MutationDeleteCardArgs = {
  input: DeleteCardInput;
};


export type MutationDeleteCardByIdArgs = {
  input: DeleteCardByIdInput;
};


export type MutationDeleteCardsInDeckArgs = {
  input: DeleteCardsInDeckInput;
};


export type MutationDeleteCardsInDeckByIdArgs = {
  input: DeleteCardsInDeckByIdInput;
};


export type MutationDeleteDeckArgs = {
  input: DeleteDeckInput;
};


export type MutationDeleteDeckByIdArgs = {
  input: DeleteDeckByIdInput;
};


export type MutationDeleteDeckPlayerAttributeTupleArgs = {
  input: DeleteDeckPlayerAttributeTupleInput;
};


export type MutationDeleteDeckPlayerAttributeTupleByIdArgs = {
  input: DeleteDeckPlayerAttributeTupleByIdInput;
};


export type MutationDeleteDeckShareArgs = {
  input: DeleteDeckShareInput;
};


export type MutationDeleteDeckShareByDeckIdAndShareRecipientIdArgs = {
  input: DeleteDeckShareByDeckIdAndShareRecipientIdInput;
};


export type MutationDeleteFriendArgs = {
  input: DeleteFriendInput;
};


export type MutationDeleteFriendByIdAndFriendArgs = {
  input: DeleteFriendByIdAndFriendInput;
};


export type MutationDeleteGameArgs = {
  input: DeleteGameInput;
};


export type MutationDeleteGameByIdArgs = {
  input: DeleteGameByIdInput;
};


export type MutationDeleteGameUserArgs = {
  input: DeleteGameUserInput;
};


export type MutationDeleteGameUserByGameIdAndUserIdArgs = {
  input: DeleteGameUserByGameIdAndUserIdInput;
};


export type MutationDeleteGuestArgs = {
  input: DeleteGuestInput;
};


export type MutationDeleteGuestByIdArgs = {
  input: DeleteGuestByIdInput;
};


export type MutationDeleteMatchmakingQueueArgs = {
  input: DeleteMatchmakingQueueInput;
};


export type MutationDeleteMatchmakingQueueByIdArgs = {
  input: DeleteMatchmakingQueueByIdInput;
};


export type MutationDeleteMatchmakingTicketArgs = {
  input: DeleteMatchmakingTicketInput;
};


export type MutationDeleteMatchmakingTicketByUserIdArgs = {
  input: DeleteMatchmakingTicketByUserIdInput;
};


export type MutationDeleteUserEntityAddonArgs = {
  input: DeleteUserEntityAddonInput;
};


export type MutationDeleteUserEntityAddonByIdArgs = {
  input: DeleteUserEntityAddonByIdInput;
};


export type MutationUpdateBotUserArgs = {
  input: UpdateBotUserInput;
};


export type MutationUpdateBotUserByIdArgs = {
  input: UpdateBotUserByIdInput;
};


export type MutationUpdateCardArgs = {
  input: UpdateCardInput;
};


export type MutationUpdateCardByIdArgs = {
  input: UpdateCardByIdInput;
};


export type MutationUpdateCardsInDeckArgs = {
  input: UpdateCardsInDeckInput;
};


export type MutationUpdateCardsInDeckByIdArgs = {
  input: UpdateCardsInDeckByIdInput;
};


export type MutationUpdateDeckArgs = {
  input: UpdateDeckInput;
};


export type MutationUpdateDeckByIdArgs = {
  input: UpdateDeckByIdInput;
};


export type MutationUpdateDeckPlayerAttributeTupleArgs = {
  input: UpdateDeckPlayerAttributeTupleInput;
};


export type MutationUpdateDeckPlayerAttributeTupleByIdArgs = {
  input: UpdateDeckPlayerAttributeTupleByIdInput;
};


export type MutationUpdateDeckShareArgs = {
  input: UpdateDeckShareInput;
};


export type MutationUpdateDeckShareByDeckIdAndShareRecipientIdArgs = {
  input: UpdateDeckShareByDeckIdAndShareRecipientIdInput;
};


export type MutationUpdateFriendArgs = {
  input: UpdateFriendInput;
};


export type MutationUpdateFriendByIdAndFriendArgs = {
  input: UpdateFriendByIdAndFriendInput;
};


export type MutationUpdateGameArgs = {
  input: UpdateGameInput;
};


export type MutationUpdateGameByIdArgs = {
  input: UpdateGameByIdInput;
};


export type MutationUpdateGameUserArgs = {
  input: UpdateGameUserInput;
};


export type MutationUpdateGameUserByGameIdAndUserIdArgs = {
  input: UpdateGameUserByGameIdAndUserIdInput;
};


export type MutationUpdateGuestArgs = {
  input: UpdateGuestInput;
};


export type MutationUpdateGuestByIdArgs = {
  input: UpdateGuestByIdInput;
};


export type MutationUpdateMatchmakingQueueArgs = {
  input: UpdateMatchmakingQueueInput;
};


export type MutationUpdateMatchmakingQueueByIdArgs = {
  input: UpdateMatchmakingQueueByIdInput;
};


export type MutationUpdateMatchmakingTicketArgs = {
  input: UpdateMatchmakingTicketInput;
};


export type MutationUpdateMatchmakingTicketByUserIdArgs = {
  input: UpdateMatchmakingTicketByUserIdInput;
};


export type MutationUpdateUserEntityAddonArgs = {
  input: UpdateUserEntityAddonInput;
};


export type MutationUpdateUserEntityAddonByIdArgs = {
  input: UpdateUserEntityAddonByIdInput;
};

export type Node = {
  nodeId: Scalars['ID'];
};

export type PageInfo = {
  __typename?: 'PageInfo';
  endCursor?: Maybe<Scalars['Cursor']>;
  hasNextPage: Scalars['Boolean'];
  hasPreviousPage: Scalars['Boolean'];
  startCursor?: Maybe<Scalars['Cursor']>;
};

export type Query = Node & {
  __typename?: 'Query';
  allArt: Array<ImageDef>;
  allBotUsers?: Maybe<BotUsersConnection>;
  allCards?: Maybe<CardsConnection>;
  allCardsInDecks?: Maybe<CardsInDecksConnection>;
  allDeckPlayerAttributeTuples?: Maybe<DeckPlayerAttributeTuplesConnection>;
  allDeckShares?: Maybe<DeckSharesConnection>;
  allDecks?: Maybe<DecksConnection>;
  allFriends?: Maybe<FriendsConnection>;
  allGameUsers?: Maybe<GameUsersConnection>;
  allGames?: Maybe<GamesConnection>;
  allGuests?: Maybe<GuestsConnection>;
  allMatchmakingQueues?: Maybe<MatchmakingQueuesConnection>;
  allMatchmakingTickets?: Maybe<MatchmakingTicketsConnection>;
  allUserEntityAddons?: Maybe<UserEntityAddonsConnection>;
  artById?: Maybe<ImageDef>;
  botUser?: Maybe<BotUser>;
  botUserById?: Maybe<BotUser>;
  card?: Maybe<Card>;
  cardById?: Maybe<Card>;
  cardsInDeck?: Maybe<CardsInDeck>;
  cardsInDeckById?: Maybe<CardsInDeck>;
  deck?: Maybe<Deck>;
  deckById?: Maybe<Deck>;
  deckPlayerAttributeTuple?: Maybe<DeckPlayerAttributeTuple>;
  deckPlayerAttributeTupleById?: Maybe<DeckPlayerAttributeTuple>;
  deckShare?: Maybe<DeckShare>;
  deckShareByDeckIdAndShareRecipientId?: Maybe<DeckShare>;
  friend?: Maybe<Friend>;
  friendByIdAndFriend?: Maybe<Friend>;
  game?: Maybe<Game>;
  gameById?: Maybe<Game>;
  gameUser?: Maybe<GameUser>;
  gameUserByGameIdAndUserId?: Maybe<GameUser>;
  guest?: Maybe<Guest>;
  guestById?: Maybe<Guest>;
  matchmakingQueue?: Maybe<MatchmakingQueue>;
  matchmakingQueueById?: Maybe<MatchmakingQueue>;
  matchmakingTicket?: Maybe<MatchmakingTicket>;
  matchmakingTicketByUserId?: Maybe<MatchmakingTicket>;
  node?: Maybe<Node>;
  nodeId: Scalars['ID'];
  query: Query;
  userEntityAddon?: Maybe<UserEntityAddon>;
  userEntityAddonById?: Maybe<UserEntityAddon>;
};


export type QueryAllBotUsersArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<BotUserCondition>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<BotUsersOrderBy>>;
};


export type QueryAllCardsArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<CardCondition>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<CardsOrderBy>>;
};


export type QueryAllCardsInDecksArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<CardsInDeckCondition>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<CardsInDecksOrderBy>>;
};


export type QueryAllDeckPlayerAttributeTuplesArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<DeckPlayerAttributeTupleCondition>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<DeckPlayerAttributeTuplesOrderBy>>;
};


export type QueryAllDeckSharesArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<DeckShareCondition>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<DeckSharesOrderBy>>;
};


export type QueryAllDecksArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<DeckCondition>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<DecksOrderBy>>;
};


export type QueryAllFriendsArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<FriendCondition>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<FriendsOrderBy>>;
};


export type QueryAllGameUsersArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<GameUserCondition>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<GameUsersOrderBy>>;
};


export type QueryAllGamesArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<GameCondition>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<GamesOrderBy>>;
};


export type QueryAllGuestsArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<GuestCondition>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<GuestsOrderBy>>;
};


export type QueryAllMatchmakingQueuesArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<MatchmakingQueueCondition>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<MatchmakingQueuesOrderBy>>;
};


export type QueryAllMatchmakingTicketsArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<MatchmakingTicketCondition>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<MatchmakingTicketsOrderBy>>;
};


export type QueryAllUserEntityAddonsArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<UserEntityAddonCondition>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<UserEntityAddonsOrderBy>>;
};


export type QueryArtByIdArgs = {
  id: Scalars['String'];
};


export type QueryBotUserArgs = {
  nodeId: Scalars['ID'];
};


export type QueryBotUserByIdArgs = {
  id: Scalars['String'];
};


export type QueryCardArgs = {
  nodeId: Scalars['ID'];
};


export type QueryCardByIdArgs = {
  id: Scalars['String'];
};


export type QueryCardsInDeckArgs = {
  nodeId: Scalars['ID'];
};


export type QueryCardsInDeckByIdArgs = {
  id: Scalars['BigInt'];
};


export type QueryDeckArgs = {
  nodeId: Scalars['ID'];
};


export type QueryDeckByIdArgs = {
  id: Scalars['String'];
};


export type QueryDeckPlayerAttributeTupleArgs = {
  nodeId: Scalars['ID'];
};


export type QueryDeckPlayerAttributeTupleByIdArgs = {
  id: Scalars['BigInt'];
};


export type QueryDeckShareArgs = {
  nodeId: Scalars['ID'];
};


export type QueryDeckShareByDeckIdAndShareRecipientIdArgs = {
  deckId: Scalars['String'];
  shareRecipientId: Scalars['String'];
};


export type QueryFriendArgs = {
  nodeId: Scalars['ID'];
};


export type QueryFriendByIdAndFriendArgs = {
  friend: Scalars['String'];
  id: Scalars['String'];
};


export type QueryGameArgs = {
  nodeId: Scalars['ID'];
};


export type QueryGameByIdArgs = {
  id: Scalars['BigInt'];
};


export type QueryGameUserArgs = {
  nodeId: Scalars['ID'];
};


export type QueryGameUserByGameIdAndUserIdArgs = {
  gameId: Scalars['BigInt'];
  userId: Scalars['String'];
};


export type QueryGuestArgs = {
  nodeId: Scalars['ID'];
};


export type QueryGuestByIdArgs = {
  id: Scalars['BigInt'];
};


export type QueryMatchmakingQueueArgs = {
  nodeId: Scalars['ID'];
};


export type QueryMatchmakingQueueByIdArgs = {
  id: Scalars['String'];
};


export type QueryMatchmakingTicketArgs = {
  nodeId: Scalars['ID'];
};


export type QueryMatchmakingTicketByUserIdArgs = {
  userId: Scalars['String'];
};


export type QueryNodeArgs = {
  nodeId: Scalars['ID'];
};


export type QueryUserEntityAddonArgs = {
  nodeId: Scalars['ID'];
};


export type QueryUserEntityAddonByIdArgs = {
  id: Scalars['String'];
};

export type UpdateBotUserByIdInput = {
  botUserPatch: BotUserPatch;
  clientMutationId?: InputMaybe<Scalars['String']>;
  id: Scalars['String'];
};

export type UpdateBotUserInput = {
  botUserPatch: BotUserPatch;
  clientMutationId?: InputMaybe<Scalars['String']>;
  nodeId: Scalars['ID'];
};

export type UpdateBotUserPayload = {
  __typename?: 'UpdateBotUserPayload';
  botUser?: Maybe<BotUser>;
  botUserEdge?: Maybe<BotUsersEdge>;
  clientMutationId?: Maybe<Scalars['String']>;
  query?: Maybe<Query>;
};


export type UpdateBotUserPayloadBotUserEdgeArgs = {
  orderBy?: InputMaybe<Array<BotUsersOrderBy>>;
};

export type UpdateCardByIdInput = {
  cardPatch: CardPatch;
  clientMutationId?: InputMaybe<Scalars['String']>;
  id: Scalars['String'];
};

export type UpdateCardInput = {
  cardPatch: CardPatch;
  clientMutationId?: InputMaybe<Scalars['String']>;
  nodeId: Scalars['ID'];
};

export type UpdateCardPayload = {
  __typename?: 'UpdateCardPayload';
  card?: Maybe<Card>;
  cardEdge?: Maybe<CardsEdge>;
  clientMutationId?: Maybe<Scalars['String']>;
  query?: Maybe<Query>;
};


export type UpdateCardPayloadCardEdgeArgs = {
  orderBy?: InputMaybe<Array<CardsOrderBy>>;
};

export type UpdateCardsInDeckByIdInput = {
  cardsInDeckPatch: CardsInDeckPatch;
  clientMutationId?: InputMaybe<Scalars['String']>;
  id: Scalars['BigInt'];
};

export type UpdateCardsInDeckInput = {
  cardsInDeckPatch: CardsInDeckPatch;
  clientMutationId?: InputMaybe<Scalars['String']>;
  nodeId: Scalars['ID'];
};

export type UpdateCardsInDeckPayload = {
  __typename?: 'UpdateCardsInDeckPayload';
  cardByCardId?: Maybe<Card>;
  cardsInDeck?: Maybe<CardsInDeck>;
  cardsInDeckEdge?: Maybe<CardsInDecksEdge>;
  clientMutationId?: Maybe<Scalars['String']>;
  deckByDeckId?: Maybe<Deck>;
  query?: Maybe<Query>;
};


export type UpdateCardsInDeckPayloadCardsInDeckEdgeArgs = {
  orderBy?: InputMaybe<Array<CardsInDecksOrderBy>>;
};

export type UpdateDeckByIdInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  deckPatch: DeckPatch;
  id: Scalars['String'];
};

export type UpdateDeckInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  deckPatch: DeckPatch;
  nodeId: Scalars['ID'];
};

export type UpdateDeckPayload = {
  __typename?: 'UpdateDeckPayload';
  clientMutationId?: Maybe<Scalars['String']>;
  deck?: Maybe<Deck>;
  deckEdge?: Maybe<DecksEdge>;
  query?: Maybe<Query>;
};


export type UpdateDeckPayloadDeckEdgeArgs = {
  orderBy?: InputMaybe<Array<DecksOrderBy>>;
};

export type UpdateDeckPlayerAttributeTupleByIdInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  deckPlayerAttributeTuplePatch: DeckPlayerAttributeTuplePatch;
  id: Scalars['BigInt'];
};

export type UpdateDeckPlayerAttributeTupleInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  deckPlayerAttributeTuplePatch: DeckPlayerAttributeTuplePatch;
  nodeId: Scalars['ID'];
};

export type UpdateDeckPlayerAttributeTuplePayload = {
  __typename?: 'UpdateDeckPlayerAttributeTuplePayload';
  clientMutationId?: Maybe<Scalars['String']>;
  deckByDeckId?: Maybe<Deck>;
  deckPlayerAttributeTuple?: Maybe<DeckPlayerAttributeTuple>;
  deckPlayerAttributeTupleEdge?: Maybe<DeckPlayerAttributeTuplesEdge>;
  query?: Maybe<Query>;
};


export type UpdateDeckPlayerAttributeTuplePayloadDeckPlayerAttributeTupleEdgeArgs = {
  orderBy?: InputMaybe<Array<DeckPlayerAttributeTuplesOrderBy>>;
};

export type UpdateDeckShareByDeckIdAndShareRecipientIdInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  deckId: Scalars['String'];
  deckSharePatch: DeckSharePatch;
  shareRecipientId: Scalars['String'];
};

export type UpdateDeckShareInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  deckSharePatch: DeckSharePatch;
  nodeId: Scalars['ID'];
};

export type UpdateDeckSharePayload = {
  __typename?: 'UpdateDeckSharePayload';
  clientMutationId?: Maybe<Scalars['String']>;
  deckByDeckId?: Maybe<Deck>;
  deckShare?: Maybe<DeckShare>;
  deckShareEdge?: Maybe<DeckSharesEdge>;
  query?: Maybe<Query>;
};


export type UpdateDeckSharePayloadDeckShareEdgeArgs = {
  orderBy?: InputMaybe<Array<DeckSharesOrderBy>>;
};

export type UpdateFriendByIdAndFriendInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  friend: Scalars['String'];
  friendPatch: FriendPatch;
  id: Scalars['String'];
};

export type UpdateFriendInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  friendPatch: FriendPatch;
  nodeId: Scalars['ID'];
};

export type UpdateFriendPayload = {
  __typename?: 'UpdateFriendPayload';
  clientMutationId?: Maybe<Scalars['String']>;
  friend?: Maybe<Friend>;
  friendEdge?: Maybe<FriendsEdge>;
  query?: Maybe<Query>;
};


export type UpdateFriendPayloadFriendEdgeArgs = {
  orderBy?: InputMaybe<Array<FriendsOrderBy>>;
};

export type UpdateGameByIdInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  gamePatch: GamePatch;
  id: Scalars['BigInt'];
};

export type UpdateGameInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  gamePatch: GamePatch;
  nodeId: Scalars['ID'];
};

export type UpdateGamePayload = {
  __typename?: 'UpdateGamePayload';
  clientMutationId?: Maybe<Scalars['String']>;
  game?: Maybe<Game>;
  gameEdge?: Maybe<GamesEdge>;
  query?: Maybe<Query>;
};


export type UpdateGamePayloadGameEdgeArgs = {
  orderBy?: InputMaybe<Array<GamesOrderBy>>;
};

export type UpdateGameUserByGameIdAndUserIdInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  gameId: Scalars['BigInt'];
  gameUserPatch: GameUserPatch;
  userId: Scalars['String'];
};

export type UpdateGameUserInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  gameUserPatch: GameUserPatch;
  nodeId: Scalars['ID'];
};

export type UpdateGameUserPayload = {
  __typename?: 'UpdateGameUserPayload';
  clientMutationId?: Maybe<Scalars['String']>;
  deckByDeckId?: Maybe<Deck>;
  gameByGameId?: Maybe<Game>;
  gameUser?: Maybe<GameUser>;
  gameUserEdge?: Maybe<GameUsersEdge>;
  query?: Maybe<Query>;
};


export type UpdateGameUserPayloadGameUserEdgeArgs = {
  orderBy?: InputMaybe<Array<GameUsersOrderBy>>;
};

export type UpdateGuestByIdInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  guestPatch: GuestPatch;
  id: Scalars['BigInt'];
};

export type UpdateGuestInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  guestPatch: GuestPatch;
  nodeId: Scalars['ID'];
};

export type UpdateGuestPayload = {
  __typename?: 'UpdateGuestPayload';
  clientMutationId?: Maybe<Scalars['String']>;
  guest?: Maybe<Guest>;
  guestEdge?: Maybe<GuestsEdge>;
  query?: Maybe<Query>;
};


export type UpdateGuestPayloadGuestEdgeArgs = {
  orderBy?: InputMaybe<Array<GuestsOrderBy>>;
};

export type UpdateMatchmakingQueueByIdInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  id: Scalars['String'];
  matchmakingQueuePatch: MatchmakingQueuePatch;
};

export type UpdateMatchmakingQueueInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  matchmakingQueuePatch: MatchmakingQueuePatch;
  nodeId: Scalars['ID'];
};

export type UpdateMatchmakingQueuePayload = {
  __typename?: 'UpdateMatchmakingQueuePayload';
  clientMutationId?: Maybe<Scalars['String']>;
  matchmakingQueue?: Maybe<MatchmakingQueue>;
  matchmakingQueueEdge?: Maybe<MatchmakingQueuesEdge>;
  query?: Maybe<Query>;
};


export type UpdateMatchmakingQueuePayloadMatchmakingQueueEdgeArgs = {
  orderBy?: InputMaybe<Array<MatchmakingQueuesOrderBy>>;
};

export type UpdateMatchmakingTicketByUserIdInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  matchmakingTicketPatch: MatchmakingTicketPatch;
  userId: Scalars['String'];
};

export type UpdateMatchmakingTicketInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  matchmakingTicketPatch: MatchmakingTicketPatch;
  nodeId: Scalars['ID'];
};

export type UpdateMatchmakingTicketPayload = {
  __typename?: 'UpdateMatchmakingTicketPayload';
  clientMutationId?: Maybe<Scalars['String']>;
  deckByBotDeckId?: Maybe<Deck>;
  deckByDeckId?: Maybe<Deck>;
  matchmakingQueueByQueueId?: Maybe<MatchmakingQueue>;
  matchmakingTicket?: Maybe<MatchmakingTicket>;
  matchmakingTicketEdge?: Maybe<MatchmakingTicketsEdge>;
  query?: Maybe<Query>;
};


export type UpdateMatchmakingTicketPayloadMatchmakingTicketEdgeArgs = {
  orderBy?: InputMaybe<Array<MatchmakingTicketsOrderBy>>;
};

export type UpdateUserEntityAddonByIdInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  id: Scalars['String'];
  userEntityAddonPatch: UserEntityAddonPatch;
};

export type UpdateUserEntityAddonInput = {
  clientMutationId?: InputMaybe<Scalars['String']>;
  nodeId: Scalars['ID'];
  userEntityAddonPatch: UserEntityAddonPatch;
};

export type UpdateUserEntityAddonPayload = {
  __typename?: 'UpdateUserEntityAddonPayload';
  clientMutationId?: Maybe<Scalars['String']>;
  query?: Maybe<Query>;
  userEntityAddon?: Maybe<UserEntityAddon>;
  userEntityAddonEdge?: Maybe<UserEntityAddonsEdge>;
};


export type UpdateUserEntityAddonPayloadUserEntityAddonEdgeArgs = {
  orderBy?: InputMaybe<Array<UserEntityAddonsOrderBy>>;
};

export type UserEntityAddon = Node & {
  __typename?: 'UserEntityAddon';
  id: Scalars['String'];
  migrated?: Maybe<Scalars['Boolean']>;
  nodeId: Scalars['ID'];
  privacyToken?: Maybe<Scalars['String']>;
  showPremadeDecks?: Maybe<Scalars['Boolean']>;
};

export type UserEntityAddonCondition = {
  id?: InputMaybe<Scalars['String']>;
  migrated?: InputMaybe<Scalars['Boolean']>;
  privacyToken?: InputMaybe<Scalars['String']>;
  showPremadeDecks?: InputMaybe<Scalars['Boolean']>;
};

export type UserEntityAddonInput = {
  id: Scalars['String'];
  migrated?: InputMaybe<Scalars['Boolean']>;
  privacyToken?: InputMaybe<Scalars['String']>;
  showPremadeDecks?: InputMaybe<Scalars['Boolean']>;
};

export type UserEntityAddonPatch = {
  id?: InputMaybe<Scalars['String']>;
  migrated?: InputMaybe<Scalars['Boolean']>;
  privacyToken?: InputMaybe<Scalars['String']>;
  showPremadeDecks?: InputMaybe<Scalars['Boolean']>;
};

export type UserEntityAddonsConnection = {
  __typename?: 'UserEntityAddonsConnection';
  edges: Array<UserEntityAddonsEdge>;
  nodes: Array<Maybe<UserEntityAddon>>;
  pageInfo: PageInfo;
  totalCount: Scalars['Int'];
};

export type UserEntityAddonsEdge = {
  __typename?: 'UserEntityAddonsEdge';
  cursor?: Maybe<Scalars['Cursor']>;
  node?: Maybe<UserEntityAddon>;
};

export const UserEntityAddonsOrderBy = {
  IdAsc: 'ID_ASC',
  IdDesc: 'ID_DESC',
  MigratedAsc: 'MIGRATED_ASC',
  MigratedDesc: 'MIGRATED_DESC',
  Natural: 'NATURAL',
  PrimaryKeyAsc: 'PRIMARY_KEY_ASC',
  PrimaryKeyDesc: 'PRIMARY_KEY_DESC',
  PrivacyTokenAsc: 'PRIVACY_TOKEN_ASC',
  PrivacyTokenDesc: 'PRIVACY_TOKEN_DESC',
  ShowPremadeDecksAsc: 'SHOW_PREMADE_DECKS_ASC',
  ShowPremadeDecksDesc: 'SHOW_PREMADE_DECKS_DESC'
} as const;

export type UserEntityAddonsOrderBy = typeof UserEntityAddonsOrderBy[keyof typeof UserEntityAddonsOrderBy];


export type ResolverTypeWrapper<T> = Promise<T> | T;


export type ResolverWithResolve<TResult, TParent, TContext, TArgs> = {
  resolve: ResolverFn<TResult, TParent, TContext, TArgs>;
};
export type Resolver<TResult, TParent = {}, TContext = {}, TArgs = {}> = ResolverFn<TResult, TParent, TContext, TArgs> | ResolverWithResolve<TResult, TParent, TContext, TArgs>;

export type ResolverFn<TResult, TParent, TContext, TArgs> = (
  parent: TParent,
  args: TArgs,
  context: TContext,
  info: GraphQLResolveInfo
) => Promise<TResult> | TResult;

export type SubscriptionSubscribeFn<TResult, TParent, TContext, TArgs> = (
  parent: TParent,
  args: TArgs,
  context: TContext,
  info: GraphQLResolveInfo
) => AsyncIterable<TResult> | Promise<AsyncIterable<TResult>>;

export type SubscriptionResolveFn<TResult, TParent, TContext, TArgs> = (
  parent: TParent,
  args: TArgs,
  context: TContext,
  info: GraphQLResolveInfo
) => TResult | Promise<TResult>;

export interface SubscriptionSubscriberObject<TResult, TKey extends string, TParent, TContext, TArgs> {
  subscribe: SubscriptionSubscribeFn<{ [key in TKey]: TResult }, TParent, TContext, TArgs>;
  resolve?: SubscriptionResolveFn<TResult, { [key in TKey]: TResult }, TContext, TArgs>;
}

export interface SubscriptionResolverObject<TResult, TParent, TContext, TArgs> {
  subscribe: SubscriptionSubscribeFn<any, TParent, TContext, TArgs>;
  resolve: SubscriptionResolveFn<TResult, any, TContext, TArgs>;
}

export type SubscriptionObject<TResult, TKey extends string, TParent, TContext, TArgs> =
  | SubscriptionSubscriberObject<TResult, TKey, TParent, TContext, TArgs>
  | SubscriptionResolverObject<TResult, TParent, TContext, TArgs>;

export type SubscriptionResolver<TResult, TKey extends string, TParent = {}, TContext = {}, TArgs = {}> =
  | ((...args: any[]) => SubscriptionObject<TResult, TKey, TParent, TContext, TArgs>)
  | SubscriptionObject<TResult, TKey, TParent, TContext, TArgs>;

export type TypeResolveFn<TTypes, TParent = {}, TContext = {}> = (
  parent: TParent,
  context: TContext,
  info: GraphQLResolveInfo
) => Maybe<TTypes> | Promise<Maybe<TTypes>>;

export type IsTypeOfResolverFn<T = {}, TContext = {}> = (obj: T, context: TContext, info: GraphQLResolveInfo) => boolean | Promise<boolean>;

export type NextResolverFn<T> = () => Promise<T>;

export type DirectiveResolverFn<TResult = {}, TParent = {}, TContext = {}, TArgs = {}> = (
  next: NextResolverFn<TResult>,
  parent: TParent,
  args: TArgs,
  context: TContext,
  info: GraphQLResolveInfo
) => TResult | Promise<TResult>;


/** Mapping between all available schema types and the resolvers types */
export type ResolversTypes = {
  BigInt: ResolverTypeWrapper<Partial<Scalars['BigInt']>>;
  Boolean: ResolverTypeWrapper<Partial<Scalars['Boolean']>>;
  BotUser: ResolverTypeWrapper<Partial<BotUser>>;
  BotUserCondition: ResolverTypeWrapper<Partial<BotUserCondition>>;
  BotUserInput: ResolverTypeWrapper<Partial<BotUserInput>>;
  BotUserPatch: ResolverTypeWrapper<Partial<BotUserPatch>>;
  BotUsersConnection: ResolverTypeWrapper<Partial<BotUsersConnection>>;
  BotUsersEdge: ResolverTypeWrapper<Partial<BotUsersEdge>>;
  BotUsersOrderBy: ResolverTypeWrapper<Partial<BotUsersOrderBy>>;
  Card: ResolverTypeWrapper<Partial<Card>>;
  CardCondition: ResolverTypeWrapper<Partial<CardCondition>>;
  CardInput: ResolverTypeWrapper<Partial<CardInput>>;
  CardPatch: ResolverTypeWrapper<Partial<CardPatch>>;
  CardsConnection: ResolverTypeWrapper<Partial<CardsConnection>>;
  CardsEdge: ResolverTypeWrapper<Partial<CardsEdge>>;
  CardsInDeck: ResolverTypeWrapper<Partial<CardsInDeck>>;
  CardsInDeckCondition: ResolverTypeWrapper<Partial<CardsInDeckCondition>>;
  CardsInDeckInput: ResolverTypeWrapper<Partial<CardsInDeckInput>>;
  CardsInDeckPatch: ResolverTypeWrapper<Partial<CardsInDeckPatch>>;
  CardsInDecksConnection: ResolverTypeWrapper<Partial<CardsInDecksConnection>>;
  CardsInDecksEdge: ResolverTypeWrapper<Partial<CardsInDecksEdge>>;
  CardsInDecksOrderBy: ResolverTypeWrapper<Partial<CardsInDecksOrderBy>>;
  CardsOrderBy: ResolverTypeWrapper<Partial<CardsOrderBy>>;
  CreateBotUserInput: ResolverTypeWrapper<Partial<CreateBotUserInput>>;
  CreateBotUserPayload: ResolverTypeWrapper<Partial<CreateBotUserPayload>>;
  CreateCardInput: ResolverTypeWrapper<Partial<CreateCardInput>>;
  CreateCardPayload: ResolverTypeWrapper<Partial<CreateCardPayload>>;
  CreateCardsInDeckInput: ResolverTypeWrapper<Partial<CreateCardsInDeckInput>>;
  CreateCardsInDeckPayload: ResolverTypeWrapper<Partial<CreateCardsInDeckPayload>>;
  CreateDeckInput: ResolverTypeWrapper<Partial<CreateDeckInput>>;
  CreateDeckPayload: ResolverTypeWrapper<Partial<CreateDeckPayload>>;
  CreateDeckPlayerAttributeTupleInput: ResolverTypeWrapper<Partial<CreateDeckPlayerAttributeTupleInput>>;
  CreateDeckPlayerAttributeTuplePayload: ResolverTypeWrapper<Partial<CreateDeckPlayerAttributeTuplePayload>>;
  CreateDeckShareInput: ResolverTypeWrapper<Partial<CreateDeckShareInput>>;
  CreateDeckSharePayload: ResolverTypeWrapper<Partial<CreateDeckSharePayload>>;
  CreateFriendInput: ResolverTypeWrapper<Partial<CreateFriendInput>>;
  CreateFriendPayload: ResolverTypeWrapper<Partial<CreateFriendPayload>>;
  CreateGameInput: ResolverTypeWrapper<Partial<CreateGameInput>>;
  CreateGamePayload: ResolverTypeWrapper<Partial<CreateGamePayload>>;
  CreateGameUserInput: ResolverTypeWrapper<Partial<CreateGameUserInput>>;
  CreateGameUserPayload: ResolverTypeWrapper<Partial<CreateGameUserPayload>>;
  CreateGuestInput: ResolverTypeWrapper<Partial<CreateGuestInput>>;
  CreateGuestPayload: ResolverTypeWrapper<Partial<CreateGuestPayload>>;
  CreateMatchmakingQueueInput: ResolverTypeWrapper<Partial<CreateMatchmakingQueueInput>>;
  CreateMatchmakingQueuePayload: ResolverTypeWrapper<Partial<CreateMatchmakingQueuePayload>>;
  CreateMatchmakingTicketInput: ResolverTypeWrapper<Partial<CreateMatchmakingTicketInput>>;
  CreateMatchmakingTicketPayload: ResolverTypeWrapper<Partial<CreateMatchmakingTicketPayload>>;
  CreateUserEntityAddonInput: ResolverTypeWrapper<Partial<CreateUserEntityAddonInput>>;
  CreateUserEntityAddonPayload: ResolverTypeWrapper<Partial<CreateUserEntityAddonPayload>>;
  Cursor: ResolverTypeWrapper<Partial<Scalars['Cursor']>>;
  Datetime: ResolverTypeWrapper<Partial<Scalars['Datetime']>>;
  Deck: ResolverTypeWrapper<Partial<Deck>>;
  DeckCondition: ResolverTypeWrapper<Partial<DeckCondition>>;
  DeckInput: ResolverTypeWrapper<Partial<DeckInput>>;
  DeckPatch: ResolverTypeWrapper<Partial<DeckPatch>>;
  DeckPlayerAttributeTuple: ResolverTypeWrapper<Partial<DeckPlayerAttributeTuple>>;
  DeckPlayerAttributeTupleCondition: ResolverTypeWrapper<Partial<DeckPlayerAttributeTupleCondition>>;
  DeckPlayerAttributeTupleInput: ResolverTypeWrapper<Partial<DeckPlayerAttributeTupleInput>>;
  DeckPlayerAttributeTuplePatch: ResolverTypeWrapper<Partial<DeckPlayerAttributeTuplePatch>>;
  DeckPlayerAttributeTuplesConnection: ResolverTypeWrapper<Partial<DeckPlayerAttributeTuplesConnection>>;
  DeckPlayerAttributeTuplesEdge: ResolverTypeWrapper<Partial<DeckPlayerAttributeTuplesEdge>>;
  DeckPlayerAttributeTuplesOrderBy: ResolverTypeWrapper<Partial<DeckPlayerAttributeTuplesOrderBy>>;
  DeckShare: ResolverTypeWrapper<Partial<DeckShare>>;
  DeckShareCondition: ResolverTypeWrapper<Partial<DeckShareCondition>>;
  DeckShareInput: ResolverTypeWrapper<Partial<DeckShareInput>>;
  DeckSharePatch: ResolverTypeWrapper<Partial<DeckSharePatch>>;
  DeckSharesConnection: ResolverTypeWrapper<Partial<DeckSharesConnection>>;
  DeckSharesEdge: ResolverTypeWrapper<Partial<DeckSharesEdge>>;
  DeckSharesOrderBy: ResolverTypeWrapper<Partial<DeckSharesOrderBy>>;
  DecksConnection: ResolverTypeWrapper<Partial<DecksConnection>>;
  DecksEdge: ResolverTypeWrapper<Partial<DecksEdge>>;
  DecksOrderBy: ResolverTypeWrapper<Partial<DecksOrderBy>>;
  DeleteBotUserByIdInput: ResolverTypeWrapper<Partial<DeleteBotUserByIdInput>>;
  DeleteBotUserInput: ResolverTypeWrapper<Partial<DeleteBotUserInput>>;
  DeleteBotUserPayload: ResolverTypeWrapper<Partial<DeleteBotUserPayload>>;
  DeleteCardByIdInput: ResolverTypeWrapper<Partial<DeleteCardByIdInput>>;
  DeleteCardInput: ResolverTypeWrapper<Partial<DeleteCardInput>>;
  DeleteCardPayload: ResolverTypeWrapper<Partial<DeleteCardPayload>>;
  DeleteCardsInDeckByIdInput: ResolverTypeWrapper<Partial<DeleteCardsInDeckByIdInput>>;
  DeleteCardsInDeckInput: ResolverTypeWrapper<Partial<DeleteCardsInDeckInput>>;
  DeleteCardsInDeckPayload: ResolverTypeWrapper<Partial<DeleteCardsInDeckPayload>>;
  DeleteDeckByIdInput: ResolverTypeWrapper<Partial<DeleteDeckByIdInput>>;
  DeleteDeckInput: ResolverTypeWrapper<Partial<DeleteDeckInput>>;
  DeleteDeckPayload: ResolverTypeWrapper<Partial<DeleteDeckPayload>>;
  DeleteDeckPlayerAttributeTupleByIdInput: ResolverTypeWrapper<Partial<DeleteDeckPlayerAttributeTupleByIdInput>>;
  DeleteDeckPlayerAttributeTupleInput: ResolverTypeWrapper<Partial<DeleteDeckPlayerAttributeTupleInput>>;
  DeleteDeckPlayerAttributeTuplePayload: ResolverTypeWrapper<Partial<DeleteDeckPlayerAttributeTuplePayload>>;
  DeleteDeckShareByDeckIdAndShareRecipientIdInput: ResolverTypeWrapper<Partial<DeleteDeckShareByDeckIdAndShareRecipientIdInput>>;
  DeleteDeckShareInput: ResolverTypeWrapper<Partial<DeleteDeckShareInput>>;
  DeleteDeckSharePayload: ResolverTypeWrapper<Partial<DeleteDeckSharePayload>>;
  DeleteFriendByIdAndFriendInput: ResolverTypeWrapper<Partial<DeleteFriendByIdAndFriendInput>>;
  DeleteFriendInput: ResolverTypeWrapper<Partial<DeleteFriendInput>>;
  DeleteFriendPayload: ResolverTypeWrapper<Partial<DeleteFriendPayload>>;
  DeleteGameByIdInput: ResolverTypeWrapper<Partial<DeleteGameByIdInput>>;
  DeleteGameInput: ResolverTypeWrapper<Partial<DeleteGameInput>>;
  DeleteGamePayload: ResolverTypeWrapper<Partial<DeleteGamePayload>>;
  DeleteGameUserByGameIdAndUserIdInput: ResolverTypeWrapper<Partial<DeleteGameUserByGameIdAndUserIdInput>>;
  DeleteGameUserInput: ResolverTypeWrapper<Partial<DeleteGameUserInput>>;
  DeleteGameUserPayload: ResolverTypeWrapper<Partial<DeleteGameUserPayload>>;
  DeleteGuestByIdInput: ResolverTypeWrapper<Partial<DeleteGuestByIdInput>>;
  DeleteGuestInput: ResolverTypeWrapper<Partial<DeleteGuestInput>>;
  DeleteGuestPayload: ResolverTypeWrapper<Partial<DeleteGuestPayload>>;
  DeleteMatchmakingQueueByIdInput: ResolverTypeWrapper<Partial<DeleteMatchmakingQueueByIdInput>>;
  DeleteMatchmakingQueueInput: ResolverTypeWrapper<Partial<DeleteMatchmakingQueueInput>>;
  DeleteMatchmakingQueuePayload: ResolverTypeWrapper<Partial<DeleteMatchmakingQueuePayload>>;
  DeleteMatchmakingTicketByUserIdInput: ResolverTypeWrapper<Partial<DeleteMatchmakingTicketByUserIdInput>>;
  DeleteMatchmakingTicketInput: ResolverTypeWrapper<Partial<DeleteMatchmakingTicketInput>>;
  DeleteMatchmakingTicketPayload: ResolverTypeWrapper<Partial<DeleteMatchmakingTicketPayload>>;
  DeleteUserEntityAddonByIdInput: ResolverTypeWrapper<Partial<DeleteUserEntityAddonByIdInput>>;
  DeleteUserEntityAddonInput: ResolverTypeWrapper<Partial<DeleteUserEntityAddonInput>>;
  DeleteUserEntityAddonPayload: ResolverTypeWrapper<Partial<DeleteUserEntityAddonPayload>>;
  Friend: ResolverTypeWrapper<Partial<Friend>>;
  FriendCondition: ResolverTypeWrapper<Partial<FriendCondition>>;
  FriendInput: ResolverTypeWrapper<Partial<FriendInput>>;
  FriendPatch: ResolverTypeWrapper<Partial<FriendPatch>>;
  FriendsConnection: ResolverTypeWrapper<Partial<FriendsConnection>>;
  FriendsEdge: ResolverTypeWrapper<Partial<FriendsEdge>>;
  FriendsOrderBy: ResolverTypeWrapper<Partial<FriendsOrderBy>>;
  Game: ResolverTypeWrapper<Partial<Game>>;
  GameCondition: ResolverTypeWrapper<Partial<GameCondition>>;
  GameInput: ResolverTypeWrapper<Partial<GameInput>>;
  GamePatch: ResolverTypeWrapper<Partial<GamePatch>>;
  GameStateEnum: ResolverTypeWrapper<Partial<GameStateEnum>>;
  GameUser: ResolverTypeWrapper<Partial<GameUser>>;
  GameUserCondition: ResolverTypeWrapper<Partial<GameUserCondition>>;
  GameUserInput: ResolverTypeWrapper<Partial<GameUserInput>>;
  GameUserPatch: ResolverTypeWrapper<Partial<GameUserPatch>>;
  GameUserVictoryEnum: ResolverTypeWrapper<Partial<GameUserVictoryEnum>>;
  GameUsersConnection: ResolverTypeWrapper<Partial<GameUsersConnection>>;
  GameUsersEdge: ResolverTypeWrapper<Partial<GameUsersEdge>>;
  GameUsersOrderBy: ResolverTypeWrapper<Partial<GameUsersOrderBy>>;
  GamesConnection: ResolverTypeWrapper<Partial<GamesConnection>>;
  GamesEdge: ResolverTypeWrapper<Partial<GamesEdge>>;
  GamesOrderBy: ResolverTypeWrapper<Partial<GamesOrderBy>>;
  Guest: ResolverTypeWrapper<Partial<Guest>>;
  GuestCondition: ResolverTypeWrapper<Partial<GuestCondition>>;
  GuestInput: ResolverTypeWrapper<Partial<GuestInput>>;
  GuestPatch: ResolverTypeWrapper<Partial<GuestPatch>>;
  GuestsConnection: ResolverTypeWrapper<Partial<GuestsConnection>>;
  GuestsEdge: ResolverTypeWrapper<Partial<GuestsEdge>>;
  GuestsOrderBy: ResolverTypeWrapper<Partial<GuestsOrderBy>>;
  ID: ResolverTypeWrapper<Partial<Scalars['ID']>>;
  ImageDef: ResolverTypeWrapper<Partial<ImageDef>>;
  Int: ResolverTypeWrapper<Partial<Scalars['Int']>>;
  JSON: ResolverTypeWrapper<Partial<Scalars['JSON']>>;
  MatchmakingQueue: ResolverTypeWrapper<Partial<MatchmakingQueue>>;
  MatchmakingQueueCondition: ResolverTypeWrapper<Partial<MatchmakingQueueCondition>>;
  MatchmakingQueueInput: ResolverTypeWrapper<Partial<MatchmakingQueueInput>>;
  MatchmakingQueuePatch: ResolverTypeWrapper<Partial<MatchmakingQueuePatch>>;
  MatchmakingQueuesConnection: ResolverTypeWrapper<Partial<MatchmakingQueuesConnection>>;
  MatchmakingQueuesEdge: ResolverTypeWrapper<Partial<MatchmakingQueuesEdge>>;
  MatchmakingQueuesOrderBy: ResolverTypeWrapper<Partial<MatchmakingQueuesOrderBy>>;
  MatchmakingTicket: ResolverTypeWrapper<Partial<MatchmakingTicket>>;
  MatchmakingTicketCondition: ResolverTypeWrapper<Partial<MatchmakingTicketCondition>>;
  MatchmakingTicketInput: ResolverTypeWrapper<Partial<MatchmakingTicketInput>>;
  MatchmakingTicketPatch: ResolverTypeWrapper<Partial<MatchmakingTicketPatch>>;
  MatchmakingTicketsConnection: ResolverTypeWrapper<Partial<MatchmakingTicketsConnection>>;
  MatchmakingTicketsEdge: ResolverTypeWrapper<Partial<MatchmakingTicketsEdge>>;
  MatchmakingTicketsOrderBy: ResolverTypeWrapper<Partial<MatchmakingTicketsOrderBy>>;
  Mutation: ResolverTypeWrapper<{}>;
  Node: ResolversTypes['BotUser'] | ResolversTypes['Card'] | ResolversTypes['CardsInDeck'] | ResolversTypes['Deck'] | ResolversTypes['DeckPlayerAttributeTuple'] | ResolversTypes['DeckShare'] | ResolversTypes['Friend'] | ResolversTypes['Game'] | ResolversTypes['GameUser'] | ResolversTypes['Guest'] | ResolversTypes['MatchmakingQueue'] | ResolversTypes['MatchmakingTicket'] | ResolversTypes['Query'] | ResolversTypes['UserEntityAddon'];
  PageInfo: ResolverTypeWrapper<Partial<PageInfo>>;
  Query: ResolverTypeWrapper<{}>;
  String: ResolverTypeWrapper<Partial<Scalars['String']>>;
  UpdateBotUserByIdInput: ResolverTypeWrapper<Partial<UpdateBotUserByIdInput>>;
  UpdateBotUserInput: ResolverTypeWrapper<Partial<UpdateBotUserInput>>;
  UpdateBotUserPayload: ResolverTypeWrapper<Partial<UpdateBotUserPayload>>;
  UpdateCardByIdInput: ResolverTypeWrapper<Partial<UpdateCardByIdInput>>;
  UpdateCardInput: ResolverTypeWrapper<Partial<UpdateCardInput>>;
  UpdateCardPayload: ResolverTypeWrapper<Partial<UpdateCardPayload>>;
  UpdateCardsInDeckByIdInput: ResolverTypeWrapper<Partial<UpdateCardsInDeckByIdInput>>;
  UpdateCardsInDeckInput: ResolverTypeWrapper<Partial<UpdateCardsInDeckInput>>;
  UpdateCardsInDeckPayload: ResolverTypeWrapper<Partial<UpdateCardsInDeckPayload>>;
  UpdateDeckByIdInput: ResolverTypeWrapper<Partial<UpdateDeckByIdInput>>;
  UpdateDeckInput: ResolverTypeWrapper<Partial<UpdateDeckInput>>;
  UpdateDeckPayload: ResolverTypeWrapper<Partial<UpdateDeckPayload>>;
  UpdateDeckPlayerAttributeTupleByIdInput: ResolverTypeWrapper<Partial<UpdateDeckPlayerAttributeTupleByIdInput>>;
  UpdateDeckPlayerAttributeTupleInput: ResolverTypeWrapper<Partial<UpdateDeckPlayerAttributeTupleInput>>;
  UpdateDeckPlayerAttributeTuplePayload: ResolverTypeWrapper<Partial<UpdateDeckPlayerAttributeTuplePayload>>;
  UpdateDeckShareByDeckIdAndShareRecipientIdInput: ResolverTypeWrapper<Partial<UpdateDeckShareByDeckIdAndShareRecipientIdInput>>;
  UpdateDeckShareInput: ResolverTypeWrapper<Partial<UpdateDeckShareInput>>;
  UpdateDeckSharePayload: ResolverTypeWrapper<Partial<UpdateDeckSharePayload>>;
  UpdateFriendByIdAndFriendInput: ResolverTypeWrapper<Partial<UpdateFriendByIdAndFriendInput>>;
  UpdateFriendInput: ResolverTypeWrapper<Partial<UpdateFriendInput>>;
  UpdateFriendPayload: ResolverTypeWrapper<Partial<UpdateFriendPayload>>;
  UpdateGameByIdInput: ResolverTypeWrapper<Partial<UpdateGameByIdInput>>;
  UpdateGameInput: ResolverTypeWrapper<Partial<UpdateGameInput>>;
  UpdateGamePayload: ResolverTypeWrapper<Partial<UpdateGamePayload>>;
  UpdateGameUserByGameIdAndUserIdInput: ResolverTypeWrapper<Partial<UpdateGameUserByGameIdAndUserIdInput>>;
  UpdateGameUserInput: ResolverTypeWrapper<Partial<UpdateGameUserInput>>;
  UpdateGameUserPayload: ResolverTypeWrapper<Partial<UpdateGameUserPayload>>;
  UpdateGuestByIdInput: ResolverTypeWrapper<Partial<UpdateGuestByIdInput>>;
  UpdateGuestInput: ResolverTypeWrapper<Partial<UpdateGuestInput>>;
  UpdateGuestPayload: ResolverTypeWrapper<Partial<UpdateGuestPayload>>;
  UpdateMatchmakingQueueByIdInput: ResolverTypeWrapper<Partial<UpdateMatchmakingQueueByIdInput>>;
  UpdateMatchmakingQueueInput: ResolverTypeWrapper<Partial<UpdateMatchmakingQueueInput>>;
  UpdateMatchmakingQueuePayload: ResolverTypeWrapper<Partial<UpdateMatchmakingQueuePayload>>;
  UpdateMatchmakingTicketByUserIdInput: ResolverTypeWrapper<Partial<UpdateMatchmakingTicketByUserIdInput>>;
  UpdateMatchmakingTicketInput: ResolverTypeWrapper<Partial<UpdateMatchmakingTicketInput>>;
  UpdateMatchmakingTicketPayload: ResolverTypeWrapper<Partial<UpdateMatchmakingTicketPayload>>;
  UpdateUserEntityAddonByIdInput: ResolverTypeWrapper<Partial<UpdateUserEntityAddonByIdInput>>;
  UpdateUserEntityAddonInput: ResolverTypeWrapper<Partial<UpdateUserEntityAddonInput>>;
  UpdateUserEntityAddonPayload: ResolverTypeWrapper<Partial<UpdateUserEntityAddonPayload>>;
  UserEntityAddon: ResolverTypeWrapper<Partial<UserEntityAddon>>;
  UserEntityAddonCondition: ResolverTypeWrapper<Partial<UserEntityAddonCondition>>;
  UserEntityAddonInput: ResolverTypeWrapper<Partial<UserEntityAddonInput>>;
  UserEntityAddonPatch: ResolverTypeWrapper<Partial<UserEntityAddonPatch>>;
  UserEntityAddonsConnection: ResolverTypeWrapper<Partial<UserEntityAddonsConnection>>;
  UserEntityAddonsEdge: ResolverTypeWrapper<Partial<UserEntityAddonsEdge>>;
  UserEntityAddonsOrderBy: ResolverTypeWrapper<Partial<UserEntityAddonsOrderBy>>;
};

/** Mapping between all available schema types and the resolvers parents */
export type ResolversParentTypes = {
  BigInt: Partial<Scalars['BigInt']>;
  Boolean: Partial<Scalars['Boolean']>;
  BotUser: Partial<BotUser>;
  BotUserCondition: Partial<BotUserCondition>;
  BotUserInput: Partial<BotUserInput>;
  BotUserPatch: Partial<BotUserPatch>;
  BotUsersConnection: Partial<BotUsersConnection>;
  BotUsersEdge: Partial<BotUsersEdge>;
  Card: Partial<Card>;
  CardCondition: Partial<CardCondition>;
  CardInput: Partial<CardInput>;
  CardPatch: Partial<CardPatch>;
  CardsConnection: Partial<CardsConnection>;
  CardsEdge: Partial<CardsEdge>;
  CardsInDeck: Partial<CardsInDeck>;
  CardsInDeckCondition: Partial<CardsInDeckCondition>;
  CardsInDeckInput: Partial<CardsInDeckInput>;
  CardsInDeckPatch: Partial<CardsInDeckPatch>;
  CardsInDecksConnection: Partial<CardsInDecksConnection>;
  CardsInDecksEdge: Partial<CardsInDecksEdge>;
  CreateBotUserInput: Partial<CreateBotUserInput>;
  CreateBotUserPayload: Partial<CreateBotUserPayload>;
  CreateCardInput: Partial<CreateCardInput>;
  CreateCardPayload: Partial<CreateCardPayload>;
  CreateCardsInDeckInput: Partial<CreateCardsInDeckInput>;
  CreateCardsInDeckPayload: Partial<CreateCardsInDeckPayload>;
  CreateDeckInput: Partial<CreateDeckInput>;
  CreateDeckPayload: Partial<CreateDeckPayload>;
  CreateDeckPlayerAttributeTupleInput: Partial<CreateDeckPlayerAttributeTupleInput>;
  CreateDeckPlayerAttributeTuplePayload: Partial<CreateDeckPlayerAttributeTuplePayload>;
  CreateDeckShareInput: Partial<CreateDeckShareInput>;
  CreateDeckSharePayload: Partial<CreateDeckSharePayload>;
  CreateFriendInput: Partial<CreateFriendInput>;
  CreateFriendPayload: Partial<CreateFriendPayload>;
  CreateGameInput: Partial<CreateGameInput>;
  CreateGamePayload: Partial<CreateGamePayload>;
  CreateGameUserInput: Partial<CreateGameUserInput>;
  CreateGameUserPayload: Partial<CreateGameUserPayload>;
  CreateGuestInput: Partial<CreateGuestInput>;
  CreateGuestPayload: Partial<CreateGuestPayload>;
  CreateMatchmakingQueueInput: Partial<CreateMatchmakingQueueInput>;
  CreateMatchmakingQueuePayload: Partial<CreateMatchmakingQueuePayload>;
  CreateMatchmakingTicketInput: Partial<CreateMatchmakingTicketInput>;
  CreateMatchmakingTicketPayload: Partial<CreateMatchmakingTicketPayload>;
  CreateUserEntityAddonInput: Partial<CreateUserEntityAddonInput>;
  CreateUserEntityAddonPayload: Partial<CreateUserEntityAddonPayload>;
  Cursor: Partial<Scalars['Cursor']>;
  Datetime: Partial<Scalars['Datetime']>;
  Deck: Partial<Deck>;
  DeckCondition: Partial<DeckCondition>;
  DeckInput: Partial<DeckInput>;
  DeckPatch: Partial<DeckPatch>;
  DeckPlayerAttributeTuple: Partial<DeckPlayerAttributeTuple>;
  DeckPlayerAttributeTupleCondition: Partial<DeckPlayerAttributeTupleCondition>;
  DeckPlayerAttributeTupleInput: Partial<DeckPlayerAttributeTupleInput>;
  DeckPlayerAttributeTuplePatch: Partial<DeckPlayerAttributeTuplePatch>;
  DeckPlayerAttributeTuplesConnection: Partial<DeckPlayerAttributeTuplesConnection>;
  DeckPlayerAttributeTuplesEdge: Partial<DeckPlayerAttributeTuplesEdge>;
  DeckShare: Partial<DeckShare>;
  DeckShareCondition: Partial<DeckShareCondition>;
  DeckShareInput: Partial<DeckShareInput>;
  DeckSharePatch: Partial<DeckSharePatch>;
  DeckSharesConnection: Partial<DeckSharesConnection>;
  DeckSharesEdge: Partial<DeckSharesEdge>;
  DecksConnection: Partial<DecksConnection>;
  DecksEdge: Partial<DecksEdge>;
  DeleteBotUserByIdInput: Partial<DeleteBotUserByIdInput>;
  DeleteBotUserInput: Partial<DeleteBotUserInput>;
  DeleteBotUserPayload: Partial<DeleteBotUserPayload>;
  DeleteCardByIdInput: Partial<DeleteCardByIdInput>;
  DeleteCardInput: Partial<DeleteCardInput>;
  DeleteCardPayload: Partial<DeleteCardPayload>;
  DeleteCardsInDeckByIdInput: Partial<DeleteCardsInDeckByIdInput>;
  DeleteCardsInDeckInput: Partial<DeleteCardsInDeckInput>;
  DeleteCardsInDeckPayload: Partial<DeleteCardsInDeckPayload>;
  DeleteDeckByIdInput: Partial<DeleteDeckByIdInput>;
  DeleteDeckInput: Partial<DeleteDeckInput>;
  DeleteDeckPayload: Partial<DeleteDeckPayload>;
  DeleteDeckPlayerAttributeTupleByIdInput: Partial<DeleteDeckPlayerAttributeTupleByIdInput>;
  DeleteDeckPlayerAttributeTupleInput: Partial<DeleteDeckPlayerAttributeTupleInput>;
  DeleteDeckPlayerAttributeTuplePayload: Partial<DeleteDeckPlayerAttributeTuplePayload>;
  DeleteDeckShareByDeckIdAndShareRecipientIdInput: Partial<DeleteDeckShareByDeckIdAndShareRecipientIdInput>;
  DeleteDeckShareInput: Partial<DeleteDeckShareInput>;
  DeleteDeckSharePayload: Partial<DeleteDeckSharePayload>;
  DeleteFriendByIdAndFriendInput: Partial<DeleteFriendByIdAndFriendInput>;
  DeleteFriendInput: Partial<DeleteFriendInput>;
  DeleteFriendPayload: Partial<DeleteFriendPayload>;
  DeleteGameByIdInput: Partial<DeleteGameByIdInput>;
  DeleteGameInput: Partial<DeleteGameInput>;
  DeleteGamePayload: Partial<DeleteGamePayload>;
  DeleteGameUserByGameIdAndUserIdInput: Partial<DeleteGameUserByGameIdAndUserIdInput>;
  DeleteGameUserInput: Partial<DeleteGameUserInput>;
  DeleteGameUserPayload: Partial<DeleteGameUserPayload>;
  DeleteGuestByIdInput: Partial<DeleteGuestByIdInput>;
  DeleteGuestInput: Partial<DeleteGuestInput>;
  DeleteGuestPayload: Partial<DeleteGuestPayload>;
  DeleteMatchmakingQueueByIdInput: Partial<DeleteMatchmakingQueueByIdInput>;
  DeleteMatchmakingQueueInput: Partial<DeleteMatchmakingQueueInput>;
  DeleteMatchmakingQueuePayload: Partial<DeleteMatchmakingQueuePayload>;
  DeleteMatchmakingTicketByUserIdInput: Partial<DeleteMatchmakingTicketByUserIdInput>;
  DeleteMatchmakingTicketInput: Partial<DeleteMatchmakingTicketInput>;
  DeleteMatchmakingTicketPayload: Partial<DeleteMatchmakingTicketPayload>;
  DeleteUserEntityAddonByIdInput: Partial<DeleteUserEntityAddonByIdInput>;
  DeleteUserEntityAddonInput: Partial<DeleteUserEntityAddonInput>;
  DeleteUserEntityAddonPayload: Partial<DeleteUserEntityAddonPayload>;
  Friend: Partial<Friend>;
  FriendCondition: Partial<FriendCondition>;
  FriendInput: Partial<FriendInput>;
  FriendPatch: Partial<FriendPatch>;
  FriendsConnection: Partial<FriendsConnection>;
  FriendsEdge: Partial<FriendsEdge>;
  Game: Partial<Game>;
  GameCondition: Partial<GameCondition>;
  GameInput: Partial<GameInput>;
  GamePatch: Partial<GamePatch>;
  GameUser: Partial<GameUser>;
  GameUserCondition: Partial<GameUserCondition>;
  GameUserInput: Partial<GameUserInput>;
  GameUserPatch: Partial<GameUserPatch>;
  GameUsersConnection: Partial<GameUsersConnection>;
  GameUsersEdge: Partial<GameUsersEdge>;
  GamesConnection: Partial<GamesConnection>;
  GamesEdge: Partial<GamesEdge>;
  Guest: Partial<Guest>;
  GuestCondition: Partial<GuestCondition>;
  GuestInput: Partial<GuestInput>;
  GuestPatch: Partial<GuestPatch>;
  GuestsConnection: Partial<GuestsConnection>;
  GuestsEdge: Partial<GuestsEdge>;
  ID: Partial<Scalars['ID']>;
  ImageDef: Partial<ImageDef>;
  Int: Partial<Scalars['Int']>;
  JSON: Partial<Scalars['JSON']>;
  MatchmakingQueue: Partial<MatchmakingQueue>;
  MatchmakingQueueCondition: Partial<MatchmakingQueueCondition>;
  MatchmakingQueueInput: Partial<MatchmakingQueueInput>;
  MatchmakingQueuePatch: Partial<MatchmakingQueuePatch>;
  MatchmakingQueuesConnection: Partial<MatchmakingQueuesConnection>;
  MatchmakingQueuesEdge: Partial<MatchmakingQueuesEdge>;
  MatchmakingTicket: Partial<MatchmakingTicket>;
  MatchmakingTicketCondition: Partial<MatchmakingTicketCondition>;
  MatchmakingTicketInput: Partial<MatchmakingTicketInput>;
  MatchmakingTicketPatch: Partial<MatchmakingTicketPatch>;
  MatchmakingTicketsConnection: Partial<MatchmakingTicketsConnection>;
  MatchmakingTicketsEdge: Partial<MatchmakingTicketsEdge>;
  Mutation: {};
  Node: ResolversParentTypes['BotUser'] | ResolversParentTypes['Card'] | ResolversParentTypes['CardsInDeck'] | ResolversParentTypes['Deck'] | ResolversParentTypes['DeckPlayerAttributeTuple'] | ResolversParentTypes['DeckShare'] | ResolversParentTypes['Friend'] | ResolversParentTypes['Game'] | ResolversParentTypes['GameUser'] | ResolversParentTypes['Guest'] | ResolversParentTypes['MatchmakingQueue'] | ResolversParentTypes['MatchmakingTicket'] | ResolversParentTypes['Query'] | ResolversParentTypes['UserEntityAddon'];
  PageInfo: Partial<PageInfo>;
  Query: {};
  String: Partial<Scalars['String']>;
  UpdateBotUserByIdInput: Partial<UpdateBotUserByIdInput>;
  UpdateBotUserInput: Partial<UpdateBotUserInput>;
  UpdateBotUserPayload: Partial<UpdateBotUserPayload>;
  UpdateCardByIdInput: Partial<UpdateCardByIdInput>;
  UpdateCardInput: Partial<UpdateCardInput>;
  UpdateCardPayload: Partial<UpdateCardPayload>;
  UpdateCardsInDeckByIdInput: Partial<UpdateCardsInDeckByIdInput>;
  UpdateCardsInDeckInput: Partial<UpdateCardsInDeckInput>;
  UpdateCardsInDeckPayload: Partial<UpdateCardsInDeckPayload>;
  UpdateDeckByIdInput: Partial<UpdateDeckByIdInput>;
  UpdateDeckInput: Partial<UpdateDeckInput>;
  UpdateDeckPayload: Partial<UpdateDeckPayload>;
  UpdateDeckPlayerAttributeTupleByIdInput: Partial<UpdateDeckPlayerAttributeTupleByIdInput>;
  UpdateDeckPlayerAttributeTupleInput: Partial<UpdateDeckPlayerAttributeTupleInput>;
  UpdateDeckPlayerAttributeTuplePayload: Partial<UpdateDeckPlayerAttributeTuplePayload>;
  UpdateDeckShareByDeckIdAndShareRecipientIdInput: Partial<UpdateDeckShareByDeckIdAndShareRecipientIdInput>;
  UpdateDeckShareInput: Partial<UpdateDeckShareInput>;
  UpdateDeckSharePayload: Partial<UpdateDeckSharePayload>;
  UpdateFriendByIdAndFriendInput: Partial<UpdateFriendByIdAndFriendInput>;
  UpdateFriendInput: Partial<UpdateFriendInput>;
  UpdateFriendPayload: Partial<UpdateFriendPayload>;
  UpdateGameByIdInput: Partial<UpdateGameByIdInput>;
  UpdateGameInput: Partial<UpdateGameInput>;
  UpdateGamePayload: Partial<UpdateGamePayload>;
  UpdateGameUserByGameIdAndUserIdInput: Partial<UpdateGameUserByGameIdAndUserIdInput>;
  UpdateGameUserInput: Partial<UpdateGameUserInput>;
  UpdateGameUserPayload: Partial<UpdateGameUserPayload>;
  UpdateGuestByIdInput: Partial<UpdateGuestByIdInput>;
  UpdateGuestInput: Partial<UpdateGuestInput>;
  UpdateGuestPayload: Partial<UpdateGuestPayload>;
  UpdateMatchmakingQueueByIdInput: Partial<UpdateMatchmakingQueueByIdInput>;
  UpdateMatchmakingQueueInput: Partial<UpdateMatchmakingQueueInput>;
  UpdateMatchmakingQueuePayload: Partial<UpdateMatchmakingQueuePayload>;
  UpdateMatchmakingTicketByUserIdInput: Partial<UpdateMatchmakingTicketByUserIdInput>;
  UpdateMatchmakingTicketInput: Partial<UpdateMatchmakingTicketInput>;
  UpdateMatchmakingTicketPayload: Partial<UpdateMatchmakingTicketPayload>;
  UpdateUserEntityAddonByIdInput: Partial<UpdateUserEntityAddonByIdInput>;
  UpdateUserEntityAddonInput: Partial<UpdateUserEntityAddonInput>;
  UpdateUserEntityAddonPayload: Partial<UpdateUserEntityAddonPayload>;
  UserEntityAddon: Partial<UserEntityAddon>;
  UserEntityAddonCondition: Partial<UserEntityAddonCondition>;
  UserEntityAddonInput: Partial<UserEntityAddonInput>;
  UserEntityAddonPatch: Partial<UserEntityAddonPatch>;
  UserEntityAddonsConnection: Partial<UserEntityAddonsConnection>;
  UserEntityAddonsEdge: Partial<UserEntityAddonsEdge>;
};

export interface BigIntScalarConfig extends GraphQLScalarTypeConfig<ResolversTypes['BigInt'], any> {
  name: 'BigInt';
}

export type BotUserResolvers<ContextType = any, ParentType extends ResolversParentTypes['BotUser'] = ResolversParentTypes['BotUser']> = {
  id?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  nodeId?: Resolver<ResolversTypes['ID'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type BotUsersConnectionResolvers<ContextType = any, ParentType extends ResolversParentTypes['BotUsersConnection'] = ResolversParentTypes['BotUsersConnection']> = {
  edges?: Resolver<Array<ResolversTypes['BotUsersEdge']>, ParentType, ContextType>;
  nodes?: Resolver<Array<Maybe<ResolversTypes['BotUser']>>, ParentType, ContextType>;
  pageInfo?: Resolver<ResolversTypes['PageInfo'], ParentType, ContextType>;
  totalCount?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type BotUsersEdgeResolvers<ContextType = any, ParentType extends ResolversParentTypes['BotUsersEdge'] = ResolversParentTypes['BotUsersEdge']> = {
  cursor?: Resolver<Maybe<ResolversTypes['Cursor']>, ParentType, ContextType>;
  node?: Resolver<Maybe<ResolversTypes['BotUser']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type CardResolvers<ContextType = any, ParentType extends ResolversParentTypes['Card'] = ResolversParentTypes['Card']> = {
  blocklyWorkspace?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  cardScript?: Resolver<Maybe<ResolversTypes['JSON']>, ParentType, ContextType>;
  cardsInDecksByCardId?: Resolver<ResolversTypes['CardsInDecksConnection'], ParentType, ContextType, RequireFields<CardCardsInDecksByCardIdArgs, 'orderBy'>>;
  createdAt?: Resolver<ResolversTypes['Datetime'], ParentType, ContextType>;
  createdBy?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  id?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  lastModified?: Resolver<ResolversTypes['Datetime'], ParentType, ContextType>;
  nodeId?: Resolver<ResolversTypes['ID'], ParentType, ContextType>;
  uri?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type CardsConnectionResolvers<ContextType = any, ParentType extends ResolversParentTypes['CardsConnection'] = ResolversParentTypes['CardsConnection']> = {
  edges?: Resolver<Array<ResolversTypes['CardsEdge']>, ParentType, ContextType>;
  nodes?: Resolver<Array<Maybe<ResolversTypes['Card']>>, ParentType, ContextType>;
  pageInfo?: Resolver<ResolversTypes['PageInfo'], ParentType, ContextType>;
  totalCount?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type CardsEdgeResolvers<ContextType = any, ParentType extends ResolversParentTypes['CardsEdge'] = ResolversParentTypes['CardsEdge']> = {
  cursor?: Resolver<Maybe<ResolversTypes['Cursor']>, ParentType, ContextType>;
  node?: Resolver<Maybe<ResolversTypes['Card']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type CardsInDeckResolvers<ContextType = any, ParentType extends ResolversParentTypes['CardsInDeck'] = ResolversParentTypes['CardsInDeck']> = {
  cardByCardId?: Resolver<Maybe<ResolversTypes['Card']>, ParentType, ContextType>;
  cardId?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  deckByDeckId?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  deckId?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  id?: Resolver<ResolversTypes['BigInt'], ParentType, ContextType>;
  nodeId?: Resolver<ResolversTypes['ID'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type CardsInDecksConnectionResolvers<ContextType = any, ParentType extends ResolversParentTypes['CardsInDecksConnection'] = ResolversParentTypes['CardsInDecksConnection']> = {
  edges?: Resolver<Array<ResolversTypes['CardsInDecksEdge']>, ParentType, ContextType>;
  nodes?: Resolver<Array<Maybe<ResolversTypes['CardsInDeck']>>, ParentType, ContextType>;
  pageInfo?: Resolver<ResolversTypes['PageInfo'], ParentType, ContextType>;
  totalCount?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type CardsInDecksEdgeResolvers<ContextType = any, ParentType extends ResolversParentTypes['CardsInDecksEdge'] = ResolversParentTypes['CardsInDecksEdge']> = {
  cursor?: Resolver<Maybe<ResolversTypes['Cursor']>, ParentType, ContextType>;
  node?: Resolver<Maybe<ResolversTypes['CardsInDeck']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type CreateBotUserPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['CreateBotUserPayload'] = ResolversParentTypes['CreateBotUserPayload']> = {
  botUser?: Resolver<Maybe<ResolversTypes['BotUser']>, ParentType, ContextType>;
  botUserEdge?: Resolver<Maybe<ResolversTypes['BotUsersEdge']>, ParentType, ContextType, RequireFields<CreateBotUserPayloadBotUserEdgeArgs, 'orderBy'>>;
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type CreateCardPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['CreateCardPayload'] = ResolversParentTypes['CreateCardPayload']> = {
  card?: Resolver<Maybe<ResolversTypes['Card']>, ParentType, ContextType>;
  cardEdge?: Resolver<Maybe<ResolversTypes['CardsEdge']>, ParentType, ContextType, RequireFields<CreateCardPayloadCardEdgeArgs, 'orderBy'>>;
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type CreateCardsInDeckPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['CreateCardsInDeckPayload'] = ResolversParentTypes['CreateCardsInDeckPayload']> = {
  cardByCardId?: Resolver<Maybe<ResolversTypes['Card']>, ParentType, ContextType>;
  cardsInDeck?: Resolver<Maybe<ResolversTypes['CardsInDeck']>, ParentType, ContextType>;
  cardsInDeckEdge?: Resolver<Maybe<ResolversTypes['CardsInDecksEdge']>, ParentType, ContextType, RequireFields<CreateCardsInDeckPayloadCardsInDeckEdgeArgs, 'orderBy'>>;
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  deckByDeckId?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type CreateDeckPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['CreateDeckPayload'] = ResolversParentTypes['CreateDeckPayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  deck?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  deckEdge?: Resolver<Maybe<ResolversTypes['DecksEdge']>, ParentType, ContextType, RequireFields<CreateDeckPayloadDeckEdgeArgs, 'orderBy'>>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type CreateDeckPlayerAttributeTuplePayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['CreateDeckPlayerAttributeTuplePayload'] = ResolversParentTypes['CreateDeckPlayerAttributeTuplePayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  deckByDeckId?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  deckPlayerAttributeTuple?: Resolver<Maybe<ResolversTypes['DeckPlayerAttributeTuple']>, ParentType, ContextType>;
  deckPlayerAttributeTupleEdge?: Resolver<Maybe<ResolversTypes['DeckPlayerAttributeTuplesEdge']>, ParentType, ContextType, RequireFields<CreateDeckPlayerAttributeTuplePayloadDeckPlayerAttributeTupleEdgeArgs, 'orderBy'>>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type CreateDeckSharePayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['CreateDeckSharePayload'] = ResolversParentTypes['CreateDeckSharePayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  deckByDeckId?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  deckShare?: Resolver<Maybe<ResolversTypes['DeckShare']>, ParentType, ContextType>;
  deckShareEdge?: Resolver<Maybe<ResolversTypes['DeckSharesEdge']>, ParentType, ContextType, RequireFields<CreateDeckSharePayloadDeckShareEdgeArgs, 'orderBy'>>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type CreateFriendPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['CreateFriendPayload'] = ResolversParentTypes['CreateFriendPayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  friend?: Resolver<Maybe<ResolversTypes['Friend']>, ParentType, ContextType>;
  friendEdge?: Resolver<Maybe<ResolversTypes['FriendsEdge']>, ParentType, ContextType, RequireFields<CreateFriendPayloadFriendEdgeArgs, 'orderBy'>>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type CreateGamePayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['CreateGamePayload'] = ResolversParentTypes['CreateGamePayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  game?: Resolver<Maybe<ResolversTypes['Game']>, ParentType, ContextType>;
  gameEdge?: Resolver<Maybe<ResolversTypes['GamesEdge']>, ParentType, ContextType, RequireFields<CreateGamePayloadGameEdgeArgs, 'orderBy'>>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type CreateGameUserPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['CreateGameUserPayload'] = ResolversParentTypes['CreateGameUserPayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  deckByDeckId?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  gameByGameId?: Resolver<Maybe<ResolversTypes['Game']>, ParentType, ContextType>;
  gameUser?: Resolver<Maybe<ResolversTypes['GameUser']>, ParentType, ContextType>;
  gameUserEdge?: Resolver<Maybe<ResolversTypes['GameUsersEdge']>, ParentType, ContextType, RequireFields<CreateGameUserPayloadGameUserEdgeArgs, 'orderBy'>>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type CreateGuestPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['CreateGuestPayload'] = ResolversParentTypes['CreateGuestPayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  guest?: Resolver<Maybe<ResolversTypes['Guest']>, ParentType, ContextType>;
  guestEdge?: Resolver<Maybe<ResolversTypes['GuestsEdge']>, ParentType, ContextType, RequireFields<CreateGuestPayloadGuestEdgeArgs, 'orderBy'>>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type CreateMatchmakingQueuePayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['CreateMatchmakingQueuePayload'] = ResolversParentTypes['CreateMatchmakingQueuePayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  matchmakingQueue?: Resolver<Maybe<ResolversTypes['MatchmakingQueue']>, ParentType, ContextType>;
  matchmakingQueueEdge?: Resolver<Maybe<ResolversTypes['MatchmakingQueuesEdge']>, ParentType, ContextType, RequireFields<CreateMatchmakingQueuePayloadMatchmakingQueueEdgeArgs, 'orderBy'>>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type CreateMatchmakingTicketPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['CreateMatchmakingTicketPayload'] = ResolversParentTypes['CreateMatchmakingTicketPayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  deckByBotDeckId?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  deckByDeckId?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  matchmakingQueueByQueueId?: Resolver<Maybe<ResolversTypes['MatchmakingQueue']>, ParentType, ContextType>;
  matchmakingTicket?: Resolver<Maybe<ResolversTypes['MatchmakingTicket']>, ParentType, ContextType>;
  matchmakingTicketEdge?: Resolver<Maybe<ResolversTypes['MatchmakingTicketsEdge']>, ParentType, ContextType, RequireFields<CreateMatchmakingTicketPayloadMatchmakingTicketEdgeArgs, 'orderBy'>>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type CreateUserEntityAddonPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['CreateUserEntityAddonPayload'] = ResolversParentTypes['CreateUserEntityAddonPayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  userEntityAddon?: Resolver<Maybe<ResolversTypes['UserEntityAddon']>, ParentType, ContextType>;
  userEntityAddonEdge?: Resolver<Maybe<ResolversTypes['UserEntityAddonsEdge']>, ParentType, ContextType, RequireFields<CreateUserEntityAddonPayloadUserEntityAddonEdgeArgs, 'orderBy'>>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export interface CursorScalarConfig extends GraphQLScalarTypeConfig<ResolversTypes['Cursor'], any> {
  name: 'Cursor';
}

export interface DatetimeScalarConfig extends GraphQLScalarTypeConfig<ResolversTypes['Datetime'], any> {
  name: 'Datetime';
}

export type DeckResolvers<ContextType = any, ParentType extends ResolversParentTypes['Deck'] = ResolversParentTypes['Deck']> = {
  cardsInDecksByDeckId?: Resolver<ResolversTypes['CardsInDecksConnection'], ParentType, ContextType, RequireFields<DeckCardsInDecksByDeckIdArgs, 'orderBy'>>;
  createdBy?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  deckPlayerAttributeTuplesByDeckId?: Resolver<ResolversTypes['DeckPlayerAttributeTuplesConnection'], ParentType, ContextType, RequireFields<DeckDeckPlayerAttributeTuplesByDeckIdArgs, 'orderBy'>>;
  deckSharesByDeckId?: Resolver<ResolversTypes['DeckSharesConnection'], ParentType, ContextType, RequireFields<DeckDeckSharesByDeckIdArgs, 'orderBy'>>;
  deckType?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  format?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  gameUsersByDeckId?: Resolver<ResolversTypes['GameUsersConnection'], ParentType, ContextType, RequireFields<DeckGameUsersByDeckIdArgs, 'orderBy'>>;
  heroClass?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  id?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  isPremade?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  lastEditedBy?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  matchmakingTicketsByBotDeckId?: Resolver<ResolversTypes['MatchmakingTicketsConnection'], ParentType, ContextType, RequireFields<DeckMatchmakingTicketsByBotDeckIdArgs, 'orderBy'>>;
  matchmakingTicketsByDeckId?: Resolver<ResolversTypes['MatchmakingTicketsConnection'], ParentType, ContextType, RequireFields<DeckMatchmakingTicketsByDeckIdArgs, 'orderBy'>>;
  name?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  nodeId?: Resolver<ResolversTypes['ID'], ParentType, ContextType>;
  permittedToDuplicate?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  trashed?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type DeckPlayerAttributeTupleResolvers<ContextType = any, ParentType extends ResolversParentTypes['DeckPlayerAttributeTuple'] = ResolversParentTypes['DeckPlayerAttributeTuple']> = {
  attribute?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  deckByDeckId?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  deckId?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  id?: Resolver<ResolversTypes['BigInt'], ParentType, ContextType>;
  nodeId?: Resolver<ResolversTypes['ID'], ParentType, ContextType>;
  stringValue?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type DeckPlayerAttributeTuplesConnectionResolvers<ContextType = any, ParentType extends ResolversParentTypes['DeckPlayerAttributeTuplesConnection'] = ResolversParentTypes['DeckPlayerAttributeTuplesConnection']> = {
  edges?: Resolver<Array<ResolversTypes['DeckPlayerAttributeTuplesEdge']>, ParentType, ContextType>;
  nodes?: Resolver<Array<Maybe<ResolversTypes['DeckPlayerAttributeTuple']>>, ParentType, ContextType>;
  pageInfo?: Resolver<ResolversTypes['PageInfo'], ParentType, ContextType>;
  totalCount?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type DeckPlayerAttributeTuplesEdgeResolvers<ContextType = any, ParentType extends ResolversParentTypes['DeckPlayerAttributeTuplesEdge'] = ResolversParentTypes['DeckPlayerAttributeTuplesEdge']> = {
  cursor?: Resolver<Maybe<ResolversTypes['Cursor']>, ParentType, ContextType>;
  node?: Resolver<Maybe<ResolversTypes['DeckPlayerAttributeTuple']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type DeckShareResolvers<ContextType = any, ParentType extends ResolversParentTypes['DeckShare'] = ResolversParentTypes['DeckShare']> = {
  deckByDeckId?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  deckId?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  nodeId?: Resolver<ResolversTypes['ID'], ParentType, ContextType>;
  shareRecipientId?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  trashedByRecipient?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type DeckSharesConnectionResolvers<ContextType = any, ParentType extends ResolversParentTypes['DeckSharesConnection'] = ResolversParentTypes['DeckSharesConnection']> = {
  edges?: Resolver<Array<ResolversTypes['DeckSharesEdge']>, ParentType, ContextType>;
  nodes?: Resolver<Array<Maybe<ResolversTypes['DeckShare']>>, ParentType, ContextType>;
  pageInfo?: Resolver<ResolversTypes['PageInfo'], ParentType, ContextType>;
  totalCount?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type DeckSharesEdgeResolvers<ContextType = any, ParentType extends ResolversParentTypes['DeckSharesEdge'] = ResolversParentTypes['DeckSharesEdge']> = {
  cursor?: Resolver<Maybe<ResolversTypes['Cursor']>, ParentType, ContextType>;
  node?: Resolver<Maybe<ResolversTypes['DeckShare']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type DecksConnectionResolvers<ContextType = any, ParentType extends ResolversParentTypes['DecksConnection'] = ResolversParentTypes['DecksConnection']> = {
  edges?: Resolver<Array<ResolversTypes['DecksEdge']>, ParentType, ContextType>;
  nodes?: Resolver<Array<Maybe<ResolversTypes['Deck']>>, ParentType, ContextType>;
  pageInfo?: Resolver<ResolversTypes['PageInfo'], ParentType, ContextType>;
  totalCount?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type DecksEdgeResolvers<ContextType = any, ParentType extends ResolversParentTypes['DecksEdge'] = ResolversParentTypes['DecksEdge']> = {
  cursor?: Resolver<Maybe<ResolversTypes['Cursor']>, ParentType, ContextType>;
  node?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type DeleteBotUserPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['DeleteBotUserPayload'] = ResolversParentTypes['DeleteBotUserPayload']> = {
  botUser?: Resolver<Maybe<ResolversTypes['BotUser']>, ParentType, ContextType>;
  botUserEdge?: Resolver<Maybe<ResolversTypes['BotUsersEdge']>, ParentType, ContextType, RequireFields<DeleteBotUserPayloadBotUserEdgeArgs, 'orderBy'>>;
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  deletedBotUserId?: Resolver<Maybe<ResolversTypes['ID']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type DeleteCardPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['DeleteCardPayload'] = ResolversParentTypes['DeleteCardPayload']> = {
  card?: Resolver<Maybe<ResolversTypes['Card']>, ParentType, ContextType>;
  cardEdge?: Resolver<Maybe<ResolversTypes['CardsEdge']>, ParentType, ContextType, RequireFields<DeleteCardPayloadCardEdgeArgs, 'orderBy'>>;
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  deletedCardId?: Resolver<Maybe<ResolversTypes['ID']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type DeleteCardsInDeckPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['DeleteCardsInDeckPayload'] = ResolversParentTypes['DeleteCardsInDeckPayload']> = {
  cardByCardId?: Resolver<Maybe<ResolversTypes['Card']>, ParentType, ContextType>;
  cardsInDeck?: Resolver<Maybe<ResolversTypes['CardsInDeck']>, ParentType, ContextType>;
  cardsInDeckEdge?: Resolver<Maybe<ResolversTypes['CardsInDecksEdge']>, ParentType, ContextType, RequireFields<DeleteCardsInDeckPayloadCardsInDeckEdgeArgs, 'orderBy'>>;
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  deckByDeckId?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  deletedCardsInDeckId?: Resolver<Maybe<ResolversTypes['ID']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type DeleteDeckPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['DeleteDeckPayload'] = ResolversParentTypes['DeleteDeckPayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  deck?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  deckEdge?: Resolver<Maybe<ResolversTypes['DecksEdge']>, ParentType, ContextType, RequireFields<DeleteDeckPayloadDeckEdgeArgs, 'orderBy'>>;
  deletedDeckId?: Resolver<Maybe<ResolversTypes['ID']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type DeleteDeckPlayerAttributeTuplePayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['DeleteDeckPlayerAttributeTuplePayload'] = ResolversParentTypes['DeleteDeckPlayerAttributeTuplePayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  deckByDeckId?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  deckPlayerAttributeTuple?: Resolver<Maybe<ResolversTypes['DeckPlayerAttributeTuple']>, ParentType, ContextType>;
  deckPlayerAttributeTupleEdge?: Resolver<Maybe<ResolversTypes['DeckPlayerAttributeTuplesEdge']>, ParentType, ContextType, RequireFields<DeleteDeckPlayerAttributeTuplePayloadDeckPlayerAttributeTupleEdgeArgs, 'orderBy'>>;
  deletedDeckPlayerAttributeTupleId?: Resolver<Maybe<ResolversTypes['ID']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type DeleteDeckSharePayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['DeleteDeckSharePayload'] = ResolversParentTypes['DeleteDeckSharePayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  deckByDeckId?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  deckShare?: Resolver<Maybe<ResolversTypes['DeckShare']>, ParentType, ContextType>;
  deckShareEdge?: Resolver<Maybe<ResolversTypes['DeckSharesEdge']>, ParentType, ContextType, RequireFields<DeleteDeckSharePayloadDeckShareEdgeArgs, 'orderBy'>>;
  deletedDeckShareId?: Resolver<Maybe<ResolversTypes['ID']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type DeleteFriendPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['DeleteFriendPayload'] = ResolversParentTypes['DeleteFriendPayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  deletedFriendId?: Resolver<Maybe<ResolversTypes['ID']>, ParentType, ContextType>;
  friend?: Resolver<Maybe<ResolversTypes['Friend']>, ParentType, ContextType>;
  friendEdge?: Resolver<Maybe<ResolversTypes['FriendsEdge']>, ParentType, ContextType, RequireFields<DeleteFriendPayloadFriendEdgeArgs, 'orderBy'>>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type DeleteGamePayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['DeleteGamePayload'] = ResolversParentTypes['DeleteGamePayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  deletedGameId?: Resolver<Maybe<ResolversTypes['ID']>, ParentType, ContextType>;
  game?: Resolver<Maybe<ResolversTypes['Game']>, ParentType, ContextType>;
  gameEdge?: Resolver<Maybe<ResolversTypes['GamesEdge']>, ParentType, ContextType, RequireFields<DeleteGamePayloadGameEdgeArgs, 'orderBy'>>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type DeleteGameUserPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['DeleteGameUserPayload'] = ResolversParentTypes['DeleteGameUserPayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  deckByDeckId?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  deletedGameUserId?: Resolver<Maybe<ResolversTypes['ID']>, ParentType, ContextType>;
  gameByGameId?: Resolver<Maybe<ResolversTypes['Game']>, ParentType, ContextType>;
  gameUser?: Resolver<Maybe<ResolversTypes['GameUser']>, ParentType, ContextType>;
  gameUserEdge?: Resolver<Maybe<ResolversTypes['GameUsersEdge']>, ParentType, ContextType, RequireFields<DeleteGameUserPayloadGameUserEdgeArgs, 'orderBy'>>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type DeleteGuestPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['DeleteGuestPayload'] = ResolversParentTypes['DeleteGuestPayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  deletedGuestId?: Resolver<Maybe<ResolversTypes['ID']>, ParentType, ContextType>;
  guest?: Resolver<Maybe<ResolversTypes['Guest']>, ParentType, ContextType>;
  guestEdge?: Resolver<Maybe<ResolversTypes['GuestsEdge']>, ParentType, ContextType, RequireFields<DeleteGuestPayloadGuestEdgeArgs, 'orderBy'>>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type DeleteMatchmakingQueuePayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['DeleteMatchmakingQueuePayload'] = ResolversParentTypes['DeleteMatchmakingQueuePayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  deletedMatchmakingQueueId?: Resolver<Maybe<ResolversTypes['ID']>, ParentType, ContextType>;
  matchmakingQueue?: Resolver<Maybe<ResolversTypes['MatchmakingQueue']>, ParentType, ContextType>;
  matchmakingQueueEdge?: Resolver<Maybe<ResolversTypes['MatchmakingQueuesEdge']>, ParentType, ContextType, RequireFields<DeleteMatchmakingQueuePayloadMatchmakingQueueEdgeArgs, 'orderBy'>>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type DeleteMatchmakingTicketPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['DeleteMatchmakingTicketPayload'] = ResolversParentTypes['DeleteMatchmakingTicketPayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  deckByBotDeckId?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  deckByDeckId?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  deletedMatchmakingTicketId?: Resolver<Maybe<ResolversTypes['ID']>, ParentType, ContextType>;
  matchmakingQueueByQueueId?: Resolver<Maybe<ResolversTypes['MatchmakingQueue']>, ParentType, ContextType>;
  matchmakingTicket?: Resolver<Maybe<ResolversTypes['MatchmakingTicket']>, ParentType, ContextType>;
  matchmakingTicketEdge?: Resolver<Maybe<ResolversTypes['MatchmakingTicketsEdge']>, ParentType, ContextType, RequireFields<DeleteMatchmakingTicketPayloadMatchmakingTicketEdgeArgs, 'orderBy'>>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type DeleteUserEntityAddonPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['DeleteUserEntityAddonPayload'] = ResolversParentTypes['DeleteUserEntityAddonPayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  deletedUserEntityAddonId?: Resolver<Maybe<ResolversTypes['ID']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  userEntityAddon?: Resolver<Maybe<ResolversTypes['UserEntityAddon']>, ParentType, ContextType>;
  userEntityAddonEdge?: Resolver<Maybe<ResolversTypes['UserEntityAddonsEdge']>, ParentType, ContextType, RequireFields<DeleteUserEntityAddonPayloadUserEntityAddonEdgeArgs, 'orderBy'>>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type FriendResolvers<ContextType = any, ParentType extends ResolversParentTypes['Friend'] = ResolversParentTypes['Friend']> = {
  createdAt?: Resolver<ResolversTypes['Datetime'], ParentType, ContextType>;
  friend?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  id?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  nodeId?: Resolver<ResolversTypes['ID'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type FriendsConnectionResolvers<ContextType = any, ParentType extends ResolversParentTypes['FriendsConnection'] = ResolversParentTypes['FriendsConnection']> = {
  edges?: Resolver<Array<ResolversTypes['FriendsEdge']>, ParentType, ContextType>;
  nodes?: Resolver<Array<Maybe<ResolversTypes['Friend']>>, ParentType, ContextType>;
  pageInfo?: Resolver<ResolversTypes['PageInfo'], ParentType, ContextType>;
  totalCount?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type FriendsEdgeResolvers<ContextType = any, ParentType extends ResolversParentTypes['FriendsEdge'] = ResolversParentTypes['FriendsEdge']> = {
  cursor?: Resolver<Maybe<ResolversTypes['Cursor']>, ParentType, ContextType>;
  node?: Resolver<Maybe<ResolversTypes['Friend']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type GameResolvers<ContextType = any, ParentType extends ResolversParentTypes['Game'] = ResolversParentTypes['Game']> = {
  createdAt?: Resolver<ResolversTypes['Datetime'], ParentType, ContextType>;
  gameUsersByGameId?: Resolver<ResolversTypes['GameUsersConnection'], ParentType, ContextType, RequireFields<GameGameUsersByGameIdArgs, 'orderBy'>>;
  gitHash?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  id?: Resolver<ResolversTypes['BigInt'], ParentType, ContextType>;
  nodeId?: Resolver<ResolversTypes['ID'], ParentType, ContextType>;
  status?: Resolver<ResolversTypes['GameStateEnum'], ParentType, ContextType>;
  trace?: Resolver<Maybe<ResolversTypes['JSON']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type GameUserResolvers<ContextType = any, ParentType extends ResolversParentTypes['GameUser'] = ResolversParentTypes['GameUser']> = {
  deckByDeckId?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  deckId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  gameByGameId?: Resolver<Maybe<ResolversTypes['Game']>, ParentType, ContextType>;
  gameId?: Resolver<ResolversTypes['BigInt'], ParentType, ContextType>;
  nodeId?: Resolver<ResolversTypes['ID'], ParentType, ContextType>;
  playerIndex?: Resolver<Maybe<ResolversTypes['Int']>, ParentType, ContextType>;
  userId?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  victoryStatus?: Resolver<ResolversTypes['GameUserVictoryEnum'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type GameUsersConnectionResolvers<ContextType = any, ParentType extends ResolversParentTypes['GameUsersConnection'] = ResolversParentTypes['GameUsersConnection']> = {
  edges?: Resolver<Array<ResolversTypes['GameUsersEdge']>, ParentType, ContextType>;
  nodes?: Resolver<Array<Maybe<ResolversTypes['GameUser']>>, ParentType, ContextType>;
  pageInfo?: Resolver<ResolversTypes['PageInfo'], ParentType, ContextType>;
  totalCount?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type GameUsersEdgeResolvers<ContextType = any, ParentType extends ResolversParentTypes['GameUsersEdge'] = ResolversParentTypes['GameUsersEdge']> = {
  cursor?: Resolver<Maybe<ResolversTypes['Cursor']>, ParentType, ContextType>;
  node?: Resolver<Maybe<ResolversTypes['GameUser']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type GamesConnectionResolvers<ContextType = any, ParentType extends ResolversParentTypes['GamesConnection'] = ResolversParentTypes['GamesConnection']> = {
  edges?: Resolver<Array<ResolversTypes['GamesEdge']>, ParentType, ContextType>;
  nodes?: Resolver<Array<Maybe<ResolversTypes['Game']>>, ParentType, ContextType>;
  pageInfo?: Resolver<ResolversTypes['PageInfo'], ParentType, ContextType>;
  totalCount?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type GamesEdgeResolvers<ContextType = any, ParentType extends ResolversParentTypes['GamesEdge'] = ResolversParentTypes['GamesEdge']> = {
  cursor?: Resolver<Maybe<ResolversTypes['Cursor']>, ParentType, ContextType>;
  node?: Resolver<Maybe<ResolversTypes['Game']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type GuestResolvers<ContextType = any, ParentType extends ResolversParentTypes['Guest'] = ResolversParentTypes['Guest']> = {
  id?: Resolver<ResolversTypes['BigInt'], ParentType, ContextType>;
  nodeId?: Resolver<ResolversTypes['ID'], ParentType, ContextType>;
  userId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type GuestsConnectionResolvers<ContextType = any, ParentType extends ResolversParentTypes['GuestsConnection'] = ResolversParentTypes['GuestsConnection']> = {
  edges?: Resolver<Array<ResolversTypes['GuestsEdge']>, ParentType, ContextType>;
  nodes?: Resolver<Array<Maybe<ResolversTypes['Guest']>>, ParentType, ContextType>;
  pageInfo?: Resolver<ResolversTypes['PageInfo'], ParentType, ContextType>;
  totalCount?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type GuestsEdgeResolvers<ContextType = any, ParentType extends ResolversParentTypes['GuestsEdge'] = ResolversParentTypes['GuestsEdge']> = {
  cursor?: Resolver<Maybe<ResolversTypes['Cursor']>, ParentType, ContextType>;
  node?: Resolver<Maybe<ResolversTypes['Guest']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type ImageDefResolvers<ContextType = any, ParentType extends ResolversParentTypes['ImageDef'] = ResolversParentTypes['ImageDef']> = {
  height?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  id?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  name?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  src?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  width?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export interface JsonScalarConfig extends GraphQLScalarTypeConfig<ResolversTypes['JSON'], any> {
  name: 'JSON';
}

export type MatchmakingQueueResolvers<ContextType = any, ParentType extends ResolversParentTypes['MatchmakingQueue'] = ResolversParentTypes['MatchmakingQueue']> = {
  automaticallyClose?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  awaitingLobbyTimeout?: Resolver<ResolversTypes['BigInt'], ParentType, ContextType>;
  botOpponent?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  emptyLobbyTimeout?: Resolver<ResolversTypes['BigInt'], ParentType, ContextType>;
  id?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  lobbySize?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  matchmakingTicketsByQueueId?: Resolver<ResolversTypes['MatchmakingTicketsConnection'], ParentType, ContextType, RequireFields<MatchmakingQueueMatchmakingTicketsByQueueIdArgs, 'orderBy'>>;
  name?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  nodeId?: Resolver<ResolversTypes['ID'], ParentType, ContextType>;
  once?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  privateLobby?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  queueCreatedAt?: Resolver<ResolversTypes['Datetime'], ParentType, ContextType>;
  startsAutomatically?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  stillConnectedTimeout?: Resolver<ResolversTypes['BigInt'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type MatchmakingQueuesConnectionResolvers<ContextType = any, ParentType extends ResolversParentTypes['MatchmakingQueuesConnection'] = ResolversParentTypes['MatchmakingQueuesConnection']> = {
  edges?: Resolver<Array<ResolversTypes['MatchmakingQueuesEdge']>, ParentType, ContextType>;
  nodes?: Resolver<Array<Maybe<ResolversTypes['MatchmakingQueue']>>, ParentType, ContextType>;
  pageInfo?: Resolver<ResolversTypes['PageInfo'], ParentType, ContextType>;
  totalCount?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type MatchmakingQueuesEdgeResolvers<ContextType = any, ParentType extends ResolversParentTypes['MatchmakingQueuesEdge'] = ResolversParentTypes['MatchmakingQueuesEdge']> = {
  cursor?: Resolver<Maybe<ResolversTypes['Cursor']>, ParentType, ContextType>;
  node?: Resolver<Maybe<ResolversTypes['MatchmakingQueue']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type MatchmakingTicketResolvers<ContextType = any, ParentType extends ResolversParentTypes['MatchmakingTicket'] = ResolversParentTypes['MatchmakingTicket']> = {
  botDeckId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  createdAt?: Resolver<ResolversTypes['Datetime'], ParentType, ContextType>;
  deckByBotDeckId?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  deckByDeckId?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  deckId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  matchmakingQueueByQueueId?: Resolver<Maybe<ResolversTypes['MatchmakingQueue']>, ParentType, ContextType>;
  nodeId?: Resolver<ResolversTypes['ID'], ParentType, ContextType>;
  queueId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  ticketId?: Resolver<ResolversTypes['BigInt'], ParentType, ContextType>;
  userId?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type MatchmakingTicketsConnectionResolvers<ContextType = any, ParentType extends ResolversParentTypes['MatchmakingTicketsConnection'] = ResolversParentTypes['MatchmakingTicketsConnection']> = {
  edges?: Resolver<Array<ResolversTypes['MatchmakingTicketsEdge']>, ParentType, ContextType>;
  nodes?: Resolver<Array<Maybe<ResolversTypes['MatchmakingTicket']>>, ParentType, ContextType>;
  pageInfo?: Resolver<ResolversTypes['PageInfo'], ParentType, ContextType>;
  totalCount?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type MatchmakingTicketsEdgeResolvers<ContextType = any, ParentType extends ResolversParentTypes['MatchmakingTicketsEdge'] = ResolversParentTypes['MatchmakingTicketsEdge']> = {
  cursor?: Resolver<Maybe<ResolversTypes['Cursor']>, ParentType, ContextType>;
  node?: Resolver<Maybe<ResolversTypes['MatchmakingTicket']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type MutationResolvers<ContextType = any, ParentType extends ResolversParentTypes['Mutation'] = ResolversParentTypes['Mutation']> = {
  createBotUser?: Resolver<Maybe<ResolversTypes['CreateBotUserPayload']>, ParentType, ContextType, RequireFields<MutationCreateBotUserArgs, 'input'>>;
  createCard?: Resolver<Maybe<ResolversTypes['CreateCardPayload']>, ParentType, ContextType, RequireFields<MutationCreateCardArgs, 'input'>>;
  createCardsInDeck?: Resolver<Maybe<ResolversTypes['CreateCardsInDeckPayload']>, ParentType, ContextType, RequireFields<MutationCreateCardsInDeckArgs, 'input'>>;
  createDeck?: Resolver<Maybe<ResolversTypes['CreateDeckPayload']>, ParentType, ContextType, RequireFields<MutationCreateDeckArgs, 'input'>>;
  createDeckPlayerAttributeTuple?: Resolver<Maybe<ResolversTypes['CreateDeckPlayerAttributeTuplePayload']>, ParentType, ContextType, RequireFields<MutationCreateDeckPlayerAttributeTupleArgs, 'input'>>;
  createDeckShare?: Resolver<Maybe<ResolversTypes['CreateDeckSharePayload']>, ParentType, ContextType, RequireFields<MutationCreateDeckShareArgs, 'input'>>;
  createFriend?: Resolver<Maybe<ResolversTypes['CreateFriendPayload']>, ParentType, ContextType, RequireFields<MutationCreateFriendArgs, 'input'>>;
  createGame?: Resolver<Maybe<ResolversTypes['CreateGamePayload']>, ParentType, ContextType, RequireFields<MutationCreateGameArgs, 'input'>>;
  createGameUser?: Resolver<Maybe<ResolversTypes['CreateGameUserPayload']>, ParentType, ContextType, RequireFields<MutationCreateGameUserArgs, 'input'>>;
  createGuest?: Resolver<Maybe<ResolversTypes['CreateGuestPayload']>, ParentType, ContextType, RequireFields<MutationCreateGuestArgs, 'input'>>;
  createMatchmakingQueue?: Resolver<Maybe<ResolversTypes['CreateMatchmakingQueuePayload']>, ParentType, ContextType, RequireFields<MutationCreateMatchmakingQueueArgs, 'input'>>;
  createMatchmakingTicket?: Resolver<Maybe<ResolversTypes['CreateMatchmakingTicketPayload']>, ParentType, ContextType, RequireFields<MutationCreateMatchmakingTicketArgs, 'input'>>;
  createUserEntityAddon?: Resolver<Maybe<ResolversTypes['CreateUserEntityAddonPayload']>, ParentType, ContextType, RequireFields<MutationCreateUserEntityAddonArgs, 'input'>>;
  deleteBotUser?: Resolver<Maybe<ResolversTypes['DeleteBotUserPayload']>, ParentType, ContextType, RequireFields<MutationDeleteBotUserArgs, 'input'>>;
  deleteBotUserById?: Resolver<Maybe<ResolversTypes['DeleteBotUserPayload']>, ParentType, ContextType, RequireFields<MutationDeleteBotUserByIdArgs, 'input'>>;
  deleteCard?: Resolver<Maybe<ResolversTypes['DeleteCardPayload']>, ParentType, ContextType, RequireFields<MutationDeleteCardArgs, 'input'>>;
  deleteCardById?: Resolver<Maybe<ResolversTypes['DeleteCardPayload']>, ParentType, ContextType, RequireFields<MutationDeleteCardByIdArgs, 'input'>>;
  deleteCardsInDeck?: Resolver<Maybe<ResolversTypes['DeleteCardsInDeckPayload']>, ParentType, ContextType, RequireFields<MutationDeleteCardsInDeckArgs, 'input'>>;
  deleteCardsInDeckById?: Resolver<Maybe<ResolversTypes['DeleteCardsInDeckPayload']>, ParentType, ContextType, RequireFields<MutationDeleteCardsInDeckByIdArgs, 'input'>>;
  deleteDeck?: Resolver<Maybe<ResolversTypes['DeleteDeckPayload']>, ParentType, ContextType, RequireFields<MutationDeleteDeckArgs, 'input'>>;
  deleteDeckById?: Resolver<Maybe<ResolversTypes['DeleteDeckPayload']>, ParentType, ContextType, RequireFields<MutationDeleteDeckByIdArgs, 'input'>>;
  deleteDeckPlayerAttributeTuple?: Resolver<Maybe<ResolversTypes['DeleteDeckPlayerAttributeTuplePayload']>, ParentType, ContextType, RequireFields<MutationDeleteDeckPlayerAttributeTupleArgs, 'input'>>;
  deleteDeckPlayerAttributeTupleById?: Resolver<Maybe<ResolversTypes['DeleteDeckPlayerAttributeTuplePayload']>, ParentType, ContextType, RequireFields<MutationDeleteDeckPlayerAttributeTupleByIdArgs, 'input'>>;
  deleteDeckShare?: Resolver<Maybe<ResolversTypes['DeleteDeckSharePayload']>, ParentType, ContextType, RequireFields<MutationDeleteDeckShareArgs, 'input'>>;
  deleteDeckShareByDeckIdAndShareRecipientId?: Resolver<Maybe<ResolversTypes['DeleteDeckSharePayload']>, ParentType, ContextType, RequireFields<MutationDeleteDeckShareByDeckIdAndShareRecipientIdArgs, 'input'>>;
  deleteFriend?: Resolver<Maybe<ResolversTypes['DeleteFriendPayload']>, ParentType, ContextType, RequireFields<MutationDeleteFriendArgs, 'input'>>;
  deleteFriendByIdAndFriend?: Resolver<Maybe<ResolversTypes['DeleteFriendPayload']>, ParentType, ContextType, RequireFields<MutationDeleteFriendByIdAndFriendArgs, 'input'>>;
  deleteGame?: Resolver<Maybe<ResolversTypes['DeleteGamePayload']>, ParentType, ContextType, RequireFields<MutationDeleteGameArgs, 'input'>>;
  deleteGameById?: Resolver<Maybe<ResolversTypes['DeleteGamePayload']>, ParentType, ContextType, RequireFields<MutationDeleteGameByIdArgs, 'input'>>;
  deleteGameUser?: Resolver<Maybe<ResolversTypes['DeleteGameUserPayload']>, ParentType, ContextType, RequireFields<MutationDeleteGameUserArgs, 'input'>>;
  deleteGameUserByGameIdAndUserId?: Resolver<Maybe<ResolversTypes['DeleteGameUserPayload']>, ParentType, ContextType, RequireFields<MutationDeleteGameUserByGameIdAndUserIdArgs, 'input'>>;
  deleteGuest?: Resolver<Maybe<ResolversTypes['DeleteGuestPayload']>, ParentType, ContextType, RequireFields<MutationDeleteGuestArgs, 'input'>>;
  deleteGuestById?: Resolver<Maybe<ResolversTypes['DeleteGuestPayload']>, ParentType, ContextType, RequireFields<MutationDeleteGuestByIdArgs, 'input'>>;
  deleteMatchmakingQueue?: Resolver<Maybe<ResolversTypes['DeleteMatchmakingQueuePayload']>, ParentType, ContextType, RequireFields<MutationDeleteMatchmakingQueueArgs, 'input'>>;
  deleteMatchmakingQueueById?: Resolver<Maybe<ResolversTypes['DeleteMatchmakingQueuePayload']>, ParentType, ContextType, RequireFields<MutationDeleteMatchmakingQueueByIdArgs, 'input'>>;
  deleteMatchmakingTicket?: Resolver<Maybe<ResolversTypes['DeleteMatchmakingTicketPayload']>, ParentType, ContextType, RequireFields<MutationDeleteMatchmakingTicketArgs, 'input'>>;
  deleteMatchmakingTicketByUserId?: Resolver<Maybe<ResolversTypes['DeleteMatchmakingTicketPayload']>, ParentType, ContextType, RequireFields<MutationDeleteMatchmakingTicketByUserIdArgs, 'input'>>;
  deleteUserEntityAddon?: Resolver<Maybe<ResolversTypes['DeleteUserEntityAddonPayload']>, ParentType, ContextType, RequireFields<MutationDeleteUserEntityAddonArgs, 'input'>>;
  deleteUserEntityAddonById?: Resolver<Maybe<ResolversTypes['DeleteUserEntityAddonPayload']>, ParentType, ContextType, RequireFields<MutationDeleteUserEntityAddonByIdArgs, 'input'>>;
  updateBotUser?: Resolver<Maybe<ResolversTypes['UpdateBotUserPayload']>, ParentType, ContextType, RequireFields<MutationUpdateBotUserArgs, 'input'>>;
  updateBotUserById?: Resolver<Maybe<ResolversTypes['UpdateBotUserPayload']>, ParentType, ContextType, RequireFields<MutationUpdateBotUserByIdArgs, 'input'>>;
  updateCard?: Resolver<Maybe<ResolversTypes['UpdateCardPayload']>, ParentType, ContextType, RequireFields<MutationUpdateCardArgs, 'input'>>;
  updateCardById?: Resolver<Maybe<ResolversTypes['UpdateCardPayload']>, ParentType, ContextType, RequireFields<MutationUpdateCardByIdArgs, 'input'>>;
  updateCardsInDeck?: Resolver<Maybe<ResolversTypes['UpdateCardsInDeckPayload']>, ParentType, ContextType, RequireFields<MutationUpdateCardsInDeckArgs, 'input'>>;
  updateCardsInDeckById?: Resolver<Maybe<ResolversTypes['UpdateCardsInDeckPayload']>, ParentType, ContextType, RequireFields<MutationUpdateCardsInDeckByIdArgs, 'input'>>;
  updateDeck?: Resolver<Maybe<ResolversTypes['UpdateDeckPayload']>, ParentType, ContextType, RequireFields<MutationUpdateDeckArgs, 'input'>>;
  updateDeckById?: Resolver<Maybe<ResolversTypes['UpdateDeckPayload']>, ParentType, ContextType, RequireFields<MutationUpdateDeckByIdArgs, 'input'>>;
  updateDeckPlayerAttributeTuple?: Resolver<Maybe<ResolversTypes['UpdateDeckPlayerAttributeTuplePayload']>, ParentType, ContextType, RequireFields<MutationUpdateDeckPlayerAttributeTupleArgs, 'input'>>;
  updateDeckPlayerAttributeTupleById?: Resolver<Maybe<ResolversTypes['UpdateDeckPlayerAttributeTuplePayload']>, ParentType, ContextType, RequireFields<MutationUpdateDeckPlayerAttributeTupleByIdArgs, 'input'>>;
  updateDeckShare?: Resolver<Maybe<ResolversTypes['UpdateDeckSharePayload']>, ParentType, ContextType, RequireFields<MutationUpdateDeckShareArgs, 'input'>>;
  updateDeckShareByDeckIdAndShareRecipientId?: Resolver<Maybe<ResolversTypes['UpdateDeckSharePayload']>, ParentType, ContextType, RequireFields<MutationUpdateDeckShareByDeckIdAndShareRecipientIdArgs, 'input'>>;
  updateFriend?: Resolver<Maybe<ResolversTypes['UpdateFriendPayload']>, ParentType, ContextType, RequireFields<MutationUpdateFriendArgs, 'input'>>;
  updateFriendByIdAndFriend?: Resolver<Maybe<ResolversTypes['UpdateFriendPayload']>, ParentType, ContextType, RequireFields<MutationUpdateFriendByIdAndFriendArgs, 'input'>>;
  updateGame?: Resolver<Maybe<ResolversTypes['UpdateGamePayload']>, ParentType, ContextType, RequireFields<MutationUpdateGameArgs, 'input'>>;
  updateGameById?: Resolver<Maybe<ResolversTypes['UpdateGamePayload']>, ParentType, ContextType, RequireFields<MutationUpdateGameByIdArgs, 'input'>>;
  updateGameUser?: Resolver<Maybe<ResolversTypes['UpdateGameUserPayload']>, ParentType, ContextType, RequireFields<MutationUpdateGameUserArgs, 'input'>>;
  updateGameUserByGameIdAndUserId?: Resolver<Maybe<ResolversTypes['UpdateGameUserPayload']>, ParentType, ContextType, RequireFields<MutationUpdateGameUserByGameIdAndUserIdArgs, 'input'>>;
  updateGuest?: Resolver<Maybe<ResolversTypes['UpdateGuestPayload']>, ParentType, ContextType, RequireFields<MutationUpdateGuestArgs, 'input'>>;
  updateGuestById?: Resolver<Maybe<ResolversTypes['UpdateGuestPayload']>, ParentType, ContextType, RequireFields<MutationUpdateGuestByIdArgs, 'input'>>;
  updateMatchmakingQueue?: Resolver<Maybe<ResolversTypes['UpdateMatchmakingQueuePayload']>, ParentType, ContextType, RequireFields<MutationUpdateMatchmakingQueueArgs, 'input'>>;
  updateMatchmakingQueueById?: Resolver<Maybe<ResolversTypes['UpdateMatchmakingQueuePayload']>, ParentType, ContextType, RequireFields<MutationUpdateMatchmakingQueueByIdArgs, 'input'>>;
  updateMatchmakingTicket?: Resolver<Maybe<ResolversTypes['UpdateMatchmakingTicketPayload']>, ParentType, ContextType, RequireFields<MutationUpdateMatchmakingTicketArgs, 'input'>>;
  updateMatchmakingTicketByUserId?: Resolver<Maybe<ResolversTypes['UpdateMatchmakingTicketPayload']>, ParentType, ContextType, RequireFields<MutationUpdateMatchmakingTicketByUserIdArgs, 'input'>>;
  updateUserEntityAddon?: Resolver<Maybe<ResolversTypes['UpdateUserEntityAddonPayload']>, ParentType, ContextType, RequireFields<MutationUpdateUserEntityAddonArgs, 'input'>>;
  updateUserEntityAddonById?: Resolver<Maybe<ResolversTypes['UpdateUserEntityAddonPayload']>, ParentType, ContextType, RequireFields<MutationUpdateUserEntityAddonByIdArgs, 'input'>>;
};

export type NodeResolvers<ContextType = any, ParentType extends ResolversParentTypes['Node'] = ResolversParentTypes['Node']> = {
  __resolveType: TypeResolveFn<'BotUser' | 'Card' | 'CardsInDeck' | 'Deck' | 'DeckPlayerAttributeTuple' | 'DeckShare' | 'Friend' | 'Game' | 'GameUser' | 'Guest' | 'MatchmakingQueue' | 'MatchmakingTicket' | 'Query' | 'UserEntityAddon', ParentType, ContextType>;
  nodeId?: Resolver<ResolversTypes['ID'], ParentType, ContextType>;
};

export type PageInfoResolvers<ContextType = any, ParentType extends ResolversParentTypes['PageInfo'] = ResolversParentTypes['PageInfo']> = {
  endCursor?: Resolver<Maybe<ResolversTypes['Cursor']>, ParentType, ContextType>;
  hasNextPage?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  hasPreviousPage?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  startCursor?: Resolver<Maybe<ResolversTypes['Cursor']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type QueryResolvers<ContextType = any, ParentType extends ResolversParentTypes['Query'] = ResolversParentTypes['Query']> = {
  allArt?: Resolver<Array<ResolversTypes['ImageDef']>, ParentType, ContextType>;
  allBotUsers?: Resolver<Maybe<ResolversTypes['BotUsersConnection']>, ParentType, ContextType, RequireFields<QueryAllBotUsersArgs, 'orderBy'>>;
  allCards?: Resolver<Maybe<ResolversTypes['CardsConnection']>, ParentType, ContextType, RequireFields<QueryAllCardsArgs, 'orderBy'>>;
  allCardsInDecks?: Resolver<Maybe<ResolversTypes['CardsInDecksConnection']>, ParentType, ContextType, RequireFields<QueryAllCardsInDecksArgs, 'orderBy'>>;
  allDeckPlayerAttributeTuples?: Resolver<Maybe<ResolversTypes['DeckPlayerAttributeTuplesConnection']>, ParentType, ContextType, RequireFields<QueryAllDeckPlayerAttributeTuplesArgs, 'orderBy'>>;
  allDeckShares?: Resolver<Maybe<ResolversTypes['DeckSharesConnection']>, ParentType, ContextType, RequireFields<QueryAllDeckSharesArgs, 'orderBy'>>;
  allDecks?: Resolver<Maybe<ResolversTypes['DecksConnection']>, ParentType, ContextType, RequireFields<QueryAllDecksArgs, 'orderBy'>>;
  allFriends?: Resolver<Maybe<ResolversTypes['FriendsConnection']>, ParentType, ContextType, RequireFields<QueryAllFriendsArgs, 'orderBy'>>;
  allGameUsers?: Resolver<Maybe<ResolversTypes['GameUsersConnection']>, ParentType, ContextType, RequireFields<QueryAllGameUsersArgs, 'orderBy'>>;
  allGames?: Resolver<Maybe<ResolversTypes['GamesConnection']>, ParentType, ContextType, RequireFields<QueryAllGamesArgs, 'orderBy'>>;
  allGuests?: Resolver<Maybe<ResolversTypes['GuestsConnection']>, ParentType, ContextType, RequireFields<QueryAllGuestsArgs, 'orderBy'>>;
  allMatchmakingQueues?: Resolver<Maybe<ResolversTypes['MatchmakingQueuesConnection']>, ParentType, ContextType, RequireFields<QueryAllMatchmakingQueuesArgs, 'orderBy'>>;
  allMatchmakingTickets?: Resolver<Maybe<ResolversTypes['MatchmakingTicketsConnection']>, ParentType, ContextType, RequireFields<QueryAllMatchmakingTicketsArgs, 'orderBy'>>;
  allUserEntityAddons?: Resolver<Maybe<ResolversTypes['UserEntityAddonsConnection']>, ParentType, ContextType, RequireFields<QueryAllUserEntityAddonsArgs, 'orderBy'>>;
  artById?: Resolver<Maybe<ResolversTypes['ImageDef']>, ParentType, ContextType, RequireFields<QueryArtByIdArgs, 'id'>>;
  botUser?: Resolver<Maybe<ResolversTypes['BotUser']>, ParentType, ContextType, RequireFields<QueryBotUserArgs, 'nodeId'>>;
  botUserById?: Resolver<Maybe<ResolversTypes['BotUser']>, ParentType, ContextType, RequireFields<QueryBotUserByIdArgs, 'id'>>;
  card?: Resolver<Maybe<ResolversTypes['Card']>, ParentType, ContextType, RequireFields<QueryCardArgs, 'nodeId'>>;
  cardById?: Resolver<Maybe<ResolversTypes['Card']>, ParentType, ContextType, RequireFields<QueryCardByIdArgs, 'id'>>;
  cardsInDeck?: Resolver<Maybe<ResolversTypes['CardsInDeck']>, ParentType, ContextType, RequireFields<QueryCardsInDeckArgs, 'nodeId'>>;
  cardsInDeckById?: Resolver<Maybe<ResolversTypes['CardsInDeck']>, ParentType, ContextType, RequireFields<QueryCardsInDeckByIdArgs, 'id'>>;
  deck?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType, RequireFields<QueryDeckArgs, 'nodeId'>>;
  deckById?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType, RequireFields<QueryDeckByIdArgs, 'id'>>;
  deckPlayerAttributeTuple?: Resolver<Maybe<ResolversTypes['DeckPlayerAttributeTuple']>, ParentType, ContextType, RequireFields<QueryDeckPlayerAttributeTupleArgs, 'nodeId'>>;
  deckPlayerAttributeTupleById?: Resolver<Maybe<ResolversTypes['DeckPlayerAttributeTuple']>, ParentType, ContextType, RequireFields<QueryDeckPlayerAttributeTupleByIdArgs, 'id'>>;
  deckShare?: Resolver<Maybe<ResolversTypes['DeckShare']>, ParentType, ContextType, RequireFields<QueryDeckShareArgs, 'nodeId'>>;
  deckShareByDeckIdAndShareRecipientId?: Resolver<Maybe<ResolversTypes['DeckShare']>, ParentType, ContextType, RequireFields<QueryDeckShareByDeckIdAndShareRecipientIdArgs, 'deckId' | 'shareRecipientId'>>;
  friend?: Resolver<Maybe<ResolversTypes['Friend']>, ParentType, ContextType, RequireFields<QueryFriendArgs, 'nodeId'>>;
  friendByIdAndFriend?: Resolver<Maybe<ResolversTypes['Friend']>, ParentType, ContextType, RequireFields<QueryFriendByIdAndFriendArgs, 'friend' | 'id'>>;
  game?: Resolver<Maybe<ResolversTypes['Game']>, ParentType, ContextType, RequireFields<QueryGameArgs, 'nodeId'>>;
  gameById?: Resolver<Maybe<ResolversTypes['Game']>, ParentType, ContextType, RequireFields<QueryGameByIdArgs, 'id'>>;
  gameUser?: Resolver<Maybe<ResolversTypes['GameUser']>, ParentType, ContextType, RequireFields<QueryGameUserArgs, 'nodeId'>>;
  gameUserByGameIdAndUserId?: Resolver<Maybe<ResolversTypes['GameUser']>, ParentType, ContextType, RequireFields<QueryGameUserByGameIdAndUserIdArgs, 'gameId' | 'userId'>>;
  guest?: Resolver<Maybe<ResolversTypes['Guest']>, ParentType, ContextType, RequireFields<QueryGuestArgs, 'nodeId'>>;
  guestById?: Resolver<Maybe<ResolversTypes['Guest']>, ParentType, ContextType, RequireFields<QueryGuestByIdArgs, 'id'>>;
  matchmakingQueue?: Resolver<Maybe<ResolversTypes['MatchmakingQueue']>, ParentType, ContextType, RequireFields<QueryMatchmakingQueueArgs, 'nodeId'>>;
  matchmakingQueueById?: Resolver<Maybe<ResolversTypes['MatchmakingQueue']>, ParentType, ContextType, RequireFields<QueryMatchmakingQueueByIdArgs, 'id'>>;
  matchmakingTicket?: Resolver<Maybe<ResolversTypes['MatchmakingTicket']>, ParentType, ContextType, RequireFields<QueryMatchmakingTicketArgs, 'nodeId'>>;
  matchmakingTicketByUserId?: Resolver<Maybe<ResolversTypes['MatchmakingTicket']>, ParentType, ContextType, RequireFields<QueryMatchmakingTicketByUserIdArgs, 'userId'>>;
  node?: Resolver<Maybe<ResolversTypes['Node']>, ParentType, ContextType, RequireFields<QueryNodeArgs, 'nodeId'>>;
  nodeId?: Resolver<ResolversTypes['ID'], ParentType, ContextType>;
  query?: Resolver<ResolversTypes['Query'], ParentType, ContextType>;
  userEntityAddon?: Resolver<Maybe<ResolversTypes['UserEntityAddon']>, ParentType, ContextType, RequireFields<QueryUserEntityAddonArgs, 'nodeId'>>;
  userEntityAddonById?: Resolver<Maybe<ResolversTypes['UserEntityAddon']>, ParentType, ContextType, RequireFields<QueryUserEntityAddonByIdArgs, 'id'>>;
};

export type UpdateBotUserPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['UpdateBotUserPayload'] = ResolversParentTypes['UpdateBotUserPayload']> = {
  botUser?: Resolver<Maybe<ResolversTypes['BotUser']>, ParentType, ContextType>;
  botUserEdge?: Resolver<Maybe<ResolversTypes['BotUsersEdge']>, ParentType, ContextType, RequireFields<UpdateBotUserPayloadBotUserEdgeArgs, 'orderBy'>>;
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type UpdateCardPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['UpdateCardPayload'] = ResolversParentTypes['UpdateCardPayload']> = {
  card?: Resolver<Maybe<ResolversTypes['Card']>, ParentType, ContextType>;
  cardEdge?: Resolver<Maybe<ResolversTypes['CardsEdge']>, ParentType, ContextType, RequireFields<UpdateCardPayloadCardEdgeArgs, 'orderBy'>>;
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type UpdateCardsInDeckPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['UpdateCardsInDeckPayload'] = ResolversParentTypes['UpdateCardsInDeckPayload']> = {
  cardByCardId?: Resolver<Maybe<ResolversTypes['Card']>, ParentType, ContextType>;
  cardsInDeck?: Resolver<Maybe<ResolversTypes['CardsInDeck']>, ParentType, ContextType>;
  cardsInDeckEdge?: Resolver<Maybe<ResolversTypes['CardsInDecksEdge']>, ParentType, ContextType, RequireFields<UpdateCardsInDeckPayloadCardsInDeckEdgeArgs, 'orderBy'>>;
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  deckByDeckId?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type UpdateDeckPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['UpdateDeckPayload'] = ResolversParentTypes['UpdateDeckPayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  deck?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  deckEdge?: Resolver<Maybe<ResolversTypes['DecksEdge']>, ParentType, ContextType, RequireFields<UpdateDeckPayloadDeckEdgeArgs, 'orderBy'>>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type UpdateDeckPlayerAttributeTuplePayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['UpdateDeckPlayerAttributeTuplePayload'] = ResolversParentTypes['UpdateDeckPlayerAttributeTuplePayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  deckByDeckId?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  deckPlayerAttributeTuple?: Resolver<Maybe<ResolversTypes['DeckPlayerAttributeTuple']>, ParentType, ContextType>;
  deckPlayerAttributeTupleEdge?: Resolver<Maybe<ResolversTypes['DeckPlayerAttributeTuplesEdge']>, ParentType, ContextType, RequireFields<UpdateDeckPlayerAttributeTuplePayloadDeckPlayerAttributeTupleEdgeArgs, 'orderBy'>>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type UpdateDeckSharePayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['UpdateDeckSharePayload'] = ResolversParentTypes['UpdateDeckSharePayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  deckByDeckId?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  deckShare?: Resolver<Maybe<ResolversTypes['DeckShare']>, ParentType, ContextType>;
  deckShareEdge?: Resolver<Maybe<ResolversTypes['DeckSharesEdge']>, ParentType, ContextType, RequireFields<UpdateDeckSharePayloadDeckShareEdgeArgs, 'orderBy'>>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type UpdateFriendPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['UpdateFriendPayload'] = ResolversParentTypes['UpdateFriendPayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  friend?: Resolver<Maybe<ResolversTypes['Friend']>, ParentType, ContextType>;
  friendEdge?: Resolver<Maybe<ResolversTypes['FriendsEdge']>, ParentType, ContextType, RequireFields<UpdateFriendPayloadFriendEdgeArgs, 'orderBy'>>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type UpdateGamePayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['UpdateGamePayload'] = ResolversParentTypes['UpdateGamePayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  game?: Resolver<Maybe<ResolversTypes['Game']>, ParentType, ContextType>;
  gameEdge?: Resolver<Maybe<ResolversTypes['GamesEdge']>, ParentType, ContextType, RequireFields<UpdateGamePayloadGameEdgeArgs, 'orderBy'>>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type UpdateGameUserPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['UpdateGameUserPayload'] = ResolversParentTypes['UpdateGameUserPayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  deckByDeckId?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  gameByGameId?: Resolver<Maybe<ResolversTypes['Game']>, ParentType, ContextType>;
  gameUser?: Resolver<Maybe<ResolversTypes['GameUser']>, ParentType, ContextType>;
  gameUserEdge?: Resolver<Maybe<ResolversTypes['GameUsersEdge']>, ParentType, ContextType, RequireFields<UpdateGameUserPayloadGameUserEdgeArgs, 'orderBy'>>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type UpdateGuestPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['UpdateGuestPayload'] = ResolversParentTypes['UpdateGuestPayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  guest?: Resolver<Maybe<ResolversTypes['Guest']>, ParentType, ContextType>;
  guestEdge?: Resolver<Maybe<ResolversTypes['GuestsEdge']>, ParentType, ContextType, RequireFields<UpdateGuestPayloadGuestEdgeArgs, 'orderBy'>>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type UpdateMatchmakingQueuePayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['UpdateMatchmakingQueuePayload'] = ResolversParentTypes['UpdateMatchmakingQueuePayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  matchmakingQueue?: Resolver<Maybe<ResolversTypes['MatchmakingQueue']>, ParentType, ContextType>;
  matchmakingQueueEdge?: Resolver<Maybe<ResolversTypes['MatchmakingQueuesEdge']>, ParentType, ContextType, RequireFields<UpdateMatchmakingQueuePayloadMatchmakingQueueEdgeArgs, 'orderBy'>>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type UpdateMatchmakingTicketPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['UpdateMatchmakingTicketPayload'] = ResolversParentTypes['UpdateMatchmakingTicketPayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  deckByBotDeckId?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  deckByDeckId?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  matchmakingQueueByQueueId?: Resolver<Maybe<ResolversTypes['MatchmakingQueue']>, ParentType, ContextType>;
  matchmakingTicket?: Resolver<Maybe<ResolversTypes['MatchmakingTicket']>, ParentType, ContextType>;
  matchmakingTicketEdge?: Resolver<Maybe<ResolversTypes['MatchmakingTicketsEdge']>, ParentType, ContextType, RequireFields<UpdateMatchmakingTicketPayloadMatchmakingTicketEdgeArgs, 'orderBy'>>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type UpdateUserEntityAddonPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['UpdateUserEntityAddonPayload'] = ResolversParentTypes['UpdateUserEntityAddonPayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  userEntityAddon?: Resolver<Maybe<ResolversTypes['UserEntityAddon']>, ParentType, ContextType>;
  userEntityAddonEdge?: Resolver<Maybe<ResolversTypes['UserEntityAddonsEdge']>, ParentType, ContextType, RequireFields<UpdateUserEntityAddonPayloadUserEntityAddonEdgeArgs, 'orderBy'>>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type UserEntityAddonResolvers<ContextType = any, ParentType extends ResolversParentTypes['UserEntityAddon'] = ResolversParentTypes['UserEntityAddon']> = {
  id?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  migrated?: Resolver<Maybe<ResolversTypes['Boolean']>, ParentType, ContextType>;
  nodeId?: Resolver<ResolversTypes['ID'], ParentType, ContextType>;
  privacyToken?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  showPremadeDecks?: Resolver<Maybe<ResolversTypes['Boolean']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type UserEntityAddonsConnectionResolvers<ContextType = any, ParentType extends ResolversParentTypes['UserEntityAddonsConnection'] = ResolversParentTypes['UserEntityAddonsConnection']> = {
  edges?: Resolver<Array<ResolversTypes['UserEntityAddonsEdge']>, ParentType, ContextType>;
  nodes?: Resolver<Array<Maybe<ResolversTypes['UserEntityAddon']>>, ParentType, ContextType>;
  pageInfo?: Resolver<ResolversTypes['PageInfo'], ParentType, ContextType>;
  totalCount?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type UserEntityAddonsEdgeResolvers<ContextType = any, ParentType extends ResolversParentTypes['UserEntityAddonsEdge'] = ResolversParentTypes['UserEntityAddonsEdge']> = {
  cursor?: Resolver<Maybe<ResolversTypes['Cursor']>, ParentType, ContextType>;
  node?: Resolver<Maybe<ResolversTypes['UserEntityAddon']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type Resolvers<ContextType = any> = {
  BigInt?: GraphQLScalarType;
  BotUser?: BotUserResolvers<ContextType>;
  BotUsersConnection?: BotUsersConnectionResolvers<ContextType>;
  BotUsersEdge?: BotUsersEdgeResolvers<ContextType>;
  Card?: CardResolvers<ContextType>;
  CardsConnection?: CardsConnectionResolvers<ContextType>;
  CardsEdge?: CardsEdgeResolvers<ContextType>;
  CardsInDeck?: CardsInDeckResolvers<ContextType>;
  CardsInDecksConnection?: CardsInDecksConnectionResolvers<ContextType>;
  CardsInDecksEdge?: CardsInDecksEdgeResolvers<ContextType>;
  CreateBotUserPayload?: CreateBotUserPayloadResolvers<ContextType>;
  CreateCardPayload?: CreateCardPayloadResolvers<ContextType>;
  CreateCardsInDeckPayload?: CreateCardsInDeckPayloadResolvers<ContextType>;
  CreateDeckPayload?: CreateDeckPayloadResolvers<ContextType>;
  CreateDeckPlayerAttributeTuplePayload?: CreateDeckPlayerAttributeTuplePayloadResolvers<ContextType>;
  CreateDeckSharePayload?: CreateDeckSharePayloadResolvers<ContextType>;
  CreateFriendPayload?: CreateFriendPayloadResolvers<ContextType>;
  CreateGamePayload?: CreateGamePayloadResolvers<ContextType>;
  CreateGameUserPayload?: CreateGameUserPayloadResolvers<ContextType>;
  CreateGuestPayload?: CreateGuestPayloadResolvers<ContextType>;
  CreateMatchmakingQueuePayload?: CreateMatchmakingQueuePayloadResolvers<ContextType>;
  CreateMatchmakingTicketPayload?: CreateMatchmakingTicketPayloadResolvers<ContextType>;
  CreateUserEntityAddonPayload?: CreateUserEntityAddonPayloadResolvers<ContextType>;
  Cursor?: GraphQLScalarType;
  Datetime?: GraphQLScalarType;
  Deck?: DeckResolvers<ContextType>;
  DeckPlayerAttributeTuple?: DeckPlayerAttributeTupleResolvers<ContextType>;
  DeckPlayerAttributeTuplesConnection?: DeckPlayerAttributeTuplesConnectionResolvers<ContextType>;
  DeckPlayerAttributeTuplesEdge?: DeckPlayerAttributeTuplesEdgeResolvers<ContextType>;
  DeckShare?: DeckShareResolvers<ContextType>;
  DeckSharesConnection?: DeckSharesConnectionResolvers<ContextType>;
  DeckSharesEdge?: DeckSharesEdgeResolvers<ContextType>;
  DecksConnection?: DecksConnectionResolvers<ContextType>;
  DecksEdge?: DecksEdgeResolvers<ContextType>;
  DeleteBotUserPayload?: DeleteBotUserPayloadResolvers<ContextType>;
  DeleteCardPayload?: DeleteCardPayloadResolvers<ContextType>;
  DeleteCardsInDeckPayload?: DeleteCardsInDeckPayloadResolvers<ContextType>;
  DeleteDeckPayload?: DeleteDeckPayloadResolvers<ContextType>;
  DeleteDeckPlayerAttributeTuplePayload?: DeleteDeckPlayerAttributeTuplePayloadResolvers<ContextType>;
  DeleteDeckSharePayload?: DeleteDeckSharePayloadResolvers<ContextType>;
  DeleteFriendPayload?: DeleteFriendPayloadResolvers<ContextType>;
  DeleteGamePayload?: DeleteGamePayloadResolvers<ContextType>;
  DeleteGameUserPayload?: DeleteGameUserPayloadResolvers<ContextType>;
  DeleteGuestPayload?: DeleteGuestPayloadResolvers<ContextType>;
  DeleteMatchmakingQueuePayload?: DeleteMatchmakingQueuePayloadResolvers<ContextType>;
  DeleteMatchmakingTicketPayload?: DeleteMatchmakingTicketPayloadResolvers<ContextType>;
  DeleteUserEntityAddonPayload?: DeleteUserEntityAddonPayloadResolvers<ContextType>;
  Friend?: FriendResolvers<ContextType>;
  FriendsConnection?: FriendsConnectionResolvers<ContextType>;
  FriendsEdge?: FriendsEdgeResolvers<ContextType>;
  Game?: GameResolvers<ContextType>;
  GameUser?: GameUserResolvers<ContextType>;
  GameUsersConnection?: GameUsersConnectionResolvers<ContextType>;
  GameUsersEdge?: GameUsersEdgeResolvers<ContextType>;
  GamesConnection?: GamesConnectionResolvers<ContextType>;
  GamesEdge?: GamesEdgeResolvers<ContextType>;
  Guest?: GuestResolvers<ContextType>;
  GuestsConnection?: GuestsConnectionResolvers<ContextType>;
  GuestsEdge?: GuestsEdgeResolvers<ContextType>;
  ImageDef?: ImageDefResolvers<ContextType>;
  JSON?: GraphQLScalarType;
  MatchmakingQueue?: MatchmakingQueueResolvers<ContextType>;
  MatchmakingQueuesConnection?: MatchmakingQueuesConnectionResolvers<ContextType>;
  MatchmakingQueuesEdge?: MatchmakingQueuesEdgeResolvers<ContextType>;
  MatchmakingTicket?: MatchmakingTicketResolvers<ContextType>;
  MatchmakingTicketsConnection?: MatchmakingTicketsConnectionResolvers<ContextType>;
  MatchmakingTicketsEdge?: MatchmakingTicketsEdgeResolvers<ContextType>;
  Mutation?: MutationResolvers<ContextType>;
  Node?: NodeResolvers<ContextType>;
  PageInfo?: PageInfoResolvers<ContextType>;
  Query?: QueryResolvers<ContextType>;
  UpdateBotUserPayload?: UpdateBotUserPayloadResolvers<ContextType>;
  UpdateCardPayload?: UpdateCardPayloadResolvers<ContextType>;
  UpdateCardsInDeckPayload?: UpdateCardsInDeckPayloadResolvers<ContextType>;
  UpdateDeckPayload?: UpdateDeckPayloadResolvers<ContextType>;
  UpdateDeckPlayerAttributeTuplePayload?: UpdateDeckPlayerAttributeTuplePayloadResolvers<ContextType>;
  UpdateDeckSharePayload?: UpdateDeckSharePayloadResolvers<ContextType>;
  UpdateFriendPayload?: UpdateFriendPayloadResolvers<ContextType>;
  UpdateGamePayload?: UpdateGamePayloadResolvers<ContextType>;
  UpdateGameUserPayload?: UpdateGameUserPayloadResolvers<ContextType>;
  UpdateGuestPayload?: UpdateGuestPayloadResolvers<ContextType>;
  UpdateMatchmakingQueuePayload?: UpdateMatchmakingQueuePayloadResolvers<ContextType>;
  UpdateMatchmakingTicketPayload?: UpdateMatchmakingTicketPayloadResolvers<ContextType>;
  UpdateUserEntityAddonPayload?: UpdateUserEntityAddonPayloadResolvers<ContextType>;
  UserEntityAddon?: UserEntityAddonResolvers<ContextType>;
  UserEntityAddonsConnection?: UserEntityAddonsConnectionResolvers<ContextType>;
  UserEntityAddonsEdge?: UserEntityAddonsEdgeResolvers<ContextType>;
};


export type CardFragment = { __typename?: 'Card', id: string, createdBy: string, cardScript?: any | null, uri?: string | null };

export type ImageFragment = { __typename?: 'ImageDef', id: string, name: string, height: number, width: number, src: string };

export type GetAllArtQueryVariables = Exact<{ [key: string]: never; }>;


export type GetAllArtQuery = { __typename?: 'Query', allArt: Array<{ __typename?: 'ImageDef', id: string, name: string, height: number, width: number, src: string }> };

export type GetAllCardsQueryVariables = Exact<{ [key: string]: never; }>;


export type GetAllCardsQuery = { __typename?: 'Query', allCards?: { __typename?: 'CardsConnection', totalCount: number, nodes: Array<{ __typename?: 'Card', id: string, createdBy: string, cardScript?: any | null, uri?: string | null } | null> } | null };

export type GetArtQueryVariables = Exact<{
  id: Scalars['String'];
}>;


export type GetArtQuery = { __typename?: 'Query', artById?: { __typename?: 'ImageDef', id: string, name: string, height: number, width: number, src: string } | null };

export type GetCardQueryVariables = Exact<{
  id: Scalars['String'];
}>;


export type GetCardQuery = { __typename?: 'Query', cardById?: { __typename?: 'Card', id: string, createdBy: string, cardScript?: any | null, uri?: string | null } | null };

export const CardFragmentDoc = gql`
    fragment card on Card {
  id
  createdBy
  cardScript
  uri
}
    `;
export const ImageFragmentDoc = gql`
    fragment image on ImageDef {
  id
  name
  height
  width
  src
}
    `;
export const GetAllArtDocument = gql`
    query getAllArt {
  allArt {
    ...image
  }
}
    ${ImageFragmentDoc}`;

/**
 * __useGetAllArtQuery__
 *
 * To run a query within a React component, call `useGetAllArtQuery` and pass it any options that fit your needs.
 * When your component renders, `useGetAllArtQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useGetAllArtQuery({
 *   variables: {
 *   },
 * });
 */
export function useGetAllArtQuery(baseOptions?: Apollo.QueryHookOptions<GetAllArtQuery, GetAllArtQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<GetAllArtQuery, GetAllArtQueryVariables>(GetAllArtDocument, options);
      }
export function useGetAllArtLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<GetAllArtQuery, GetAllArtQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<GetAllArtQuery, GetAllArtQueryVariables>(GetAllArtDocument, options);
        }
export type GetAllArtQueryHookResult = ReturnType<typeof useGetAllArtQuery>;
export type GetAllArtLazyQueryHookResult = ReturnType<typeof useGetAllArtLazyQuery>;
export type GetAllArtQueryResult = Apollo.QueryResult<GetAllArtQuery, GetAllArtQueryVariables>;
export const GetAllCardsDocument = gql`
    query getAllCards {
  allCards {
    nodes {
      ...card
    }
    totalCount
  }
}
    ${CardFragmentDoc}`;

/**
 * __useGetAllCardsQuery__
 *
 * To run a query within a React component, call `useGetAllCardsQuery` and pass it any options that fit your needs.
 * When your component renders, `useGetAllCardsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useGetAllCardsQuery({
 *   variables: {
 *   },
 * });
 */
export function useGetAllCardsQuery(baseOptions?: Apollo.QueryHookOptions<GetAllCardsQuery, GetAllCardsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<GetAllCardsQuery, GetAllCardsQueryVariables>(GetAllCardsDocument, options);
      }
export function useGetAllCardsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<GetAllCardsQuery, GetAllCardsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<GetAllCardsQuery, GetAllCardsQueryVariables>(GetAllCardsDocument, options);
        }
export type GetAllCardsQueryHookResult = ReturnType<typeof useGetAllCardsQuery>;
export type GetAllCardsLazyQueryHookResult = ReturnType<typeof useGetAllCardsLazyQuery>;
export type GetAllCardsQueryResult = Apollo.QueryResult<GetAllCardsQuery, GetAllCardsQueryVariables>;
export const GetArtDocument = gql`
    query getArt($id: String!) {
  artById(id: $id) {
    ...image
  }
}
    ${ImageFragmentDoc}`;

/**
 * __useGetArtQuery__
 *
 * To run a query within a React component, call `useGetArtQuery` and pass it any options that fit your needs.
 * When your component renders, `useGetArtQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useGetArtQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useGetArtQuery(baseOptions: Apollo.QueryHookOptions<GetArtQuery, GetArtQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<GetArtQuery, GetArtQueryVariables>(GetArtDocument, options);
      }
export function useGetArtLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<GetArtQuery, GetArtQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<GetArtQuery, GetArtQueryVariables>(GetArtDocument, options);
        }
export type GetArtQueryHookResult = ReturnType<typeof useGetArtQuery>;
export type GetArtLazyQueryHookResult = ReturnType<typeof useGetArtLazyQuery>;
export type GetArtQueryResult = Apollo.QueryResult<GetArtQuery, GetArtQueryVariables>;
export const GetCardDocument = gql`
    query getCard($id: String!) {
  cardById(id: $id) {
    ...card
  }
}
    ${CardFragmentDoc}`;

/**
 * __useGetCardQuery__
 *
 * To run a query within a React component, call `useGetCardQuery` and pass it any options that fit your needs.
 * When your component renders, `useGetCardQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useGetCardQuery({
 *   variables: {
 *      id: // value for 'id'
 *   },
 * });
 */
export function useGetCardQuery(baseOptions: Apollo.QueryHookOptions<GetCardQuery, GetCardQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<GetCardQuery, GetCardQueryVariables>(GetCardDocument, options);
      }
export function useGetCardLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<GetCardQuery, GetCardQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<GetCardQuery, GetCardQueryVariables>(GetCardDocument, options);
        }
export type GetCardQueryHookResult = ReturnType<typeof useGetCardQuery>;
export type GetCardLazyQueryHookResult = ReturnType<typeof useGetCardLazyQuery>;
export type GetCardQueryResult = Apollo.QueryResult<GetCardQuery, GetCardQueryVariables>;