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
  /**
   * A signed eight-byte integer. The upper big integer values are greater than the
   * max value for a JavaScript number. Therefore all big integers will be output as
   * strings and not numbers.
   */
  BigInt: any;
  /** A location in a connection that can be used for resuming pagination. */
  Cursor: any;
  /**
   * A point in time as described by the [ISO
   * 8601](https://en.wikipedia.org/wiki/ISO_8601) standard. May or may not include a timezone.
   */
  Datetime: any;
  /** The `JSON` scalar type represents JSON values as specified by [ECMA-404](http://www.ecma-international.org/publications/files/ECMA-ST/ECMA-404.pdf). */
  JSON: any;
};

/** All input for the `archiveCard` mutation. */
export type ArchiveCardInput = {
  cardId?: InputMaybe<Scalars['String']>;
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
};

/** The output of our `archiveCard` mutation. */
export type ArchiveCardPayload = {
  __typename?: 'ArchiveCardPayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};

export type BannedDraftCard = Node & {
  __typename?: 'BannedDraftCard';
  cardId: Scalars['String'];
  /** A globally unique identifier. Can be used in various places throughout the system to identify this single value. */
  nodeId: Scalars['ID'];
};

/**
 * A condition to be used against `BannedDraftCard` object types. All fields are
 * tested for equality and combined with a logical ‘and.’
 */
export type BannedDraftCardCondition = {
  /** Checks for equality with the object’s `cardId` field. */
  cardId?: InputMaybe<Scalars['String']>;
};

/** A filter to be used against `BannedDraftCard` object types. All fields are combined with a logical ‘and.’ */
export type BannedDraftCardFilter = {
  /** Checks for all expressions in this list. */
  and?: InputMaybe<Array<BannedDraftCardFilter>>;
  /** Filter by the object’s `cardId` field. */
  cardId?: InputMaybe<StringFilter>;
  /** Negates the expression. */
  not?: InputMaybe<BannedDraftCardFilter>;
  /** Checks for any expressions in this list. */
  or?: InputMaybe<Array<BannedDraftCardFilter>>;
};

/** An input for mutations affecting `BannedDraftCard` */
export type BannedDraftCardInput = {
  cardId: Scalars['String'];
};

/** Represents an update to a `BannedDraftCard`. Fields that are set will be updated. */
export type BannedDraftCardPatch = {
  cardId?: InputMaybe<Scalars['String']>;
};

/** A connection to a list of `BannedDraftCard` values. */
export type BannedDraftCardsConnection = {
  __typename?: 'BannedDraftCardsConnection';
  /** A list of edges which contains the `BannedDraftCard` and cursor to aid in pagination. */
  edges: Array<BannedDraftCardsEdge>;
  /** A list of `BannedDraftCard` objects. */
  nodes: Array<Maybe<BannedDraftCard>>;
  /** Information to aid in pagination. */
  pageInfo: PageInfo;
  /** The count of *all* `BannedDraftCard` you could get from the connection. */
  totalCount: Scalars['Int'];
};

/** A `BannedDraftCard` edge in the connection. */
export type BannedDraftCardsEdge = {
  __typename?: 'BannedDraftCardsEdge';
  /** A cursor for use in pagination. */
  cursor?: Maybe<Scalars['Cursor']>;
  /** The `BannedDraftCard` at the end of the edge. */
  node?: Maybe<BannedDraftCard>;
};

/** Methods to use when ordering `BannedDraftCard`. */
export const BannedDraftCardsOrderBy = {
  CardIdAsc: 'CARD_ID_ASC',
  CardIdDesc: 'CARD_ID_DESC',
  Natural: 'NATURAL',
  PrimaryKeyAsc: 'PRIMARY_KEY_ASC',
  PrimaryKeyDesc: 'PRIMARY_KEY_DESC'
} as const;

export type BannedDraftCardsOrderBy = typeof BannedDraftCardsOrderBy[keyof typeof BannedDraftCardsOrderBy];
/** A filter to be used against BigInt fields. All fields are combined with a logical ‘and.’ */
export type BigIntFilter = {
  /** Not equal to the specified value, treating null like an ordinary value. */
  distinctFrom?: InputMaybe<Scalars['BigInt']>;
  /** Equal to the specified value. */
  equalTo?: InputMaybe<Scalars['BigInt']>;
  /** Greater than the specified value. */
  greaterThan?: InputMaybe<Scalars['BigInt']>;
  /** Greater than or equal to the specified value. */
  greaterThanOrEqualTo?: InputMaybe<Scalars['BigInt']>;
  /** Included in the specified list. */
  in?: InputMaybe<Array<Scalars['BigInt']>>;
  /** Is null (if `true` is specified) or is not null (if `false` is specified). */
  isNull?: InputMaybe<Scalars['Boolean']>;
  /** Less than the specified value. */
  lessThan?: InputMaybe<Scalars['BigInt']>;
  /** Less than or equal to the specified value. */
  lessThanOrEqualTo?: InputMaybe<Scalars['BigInt']>;
  /** Equal to the specified value, treating null like an ordinary value. */
  notDistinctFrom?: InputMaybe<Scalars['BigInt']>;
  /** Not equal to the specified value. */
  notEqualTo?: InputMaybe<Scalars['BigInt']>;
  /** Not included in the specified list. */
  notIn?: InputMaybe<Array<Scalars['BigInt']>>;
};

/** A filter to be used against Boolean fields. All fields are combined with a logical ‘and.’ */
export type BooleanFilter = {
  /** Not equal to the specified value, treating null like an ordinary value. */
  distinctFrom?: InputMaybe<Scalars['Boolean']>;
  /** Equal to the specified value. */
  equalTo?: InputMaybe<Scalars['Boolean']>;
  /** Greater than the specified value. */
  greaterThan?: InputMaybe<Scalars['Boolean']>;
  /** Greater than or equal to the specified value. */
  greaterThanOrEqualTo?: InputMaybe<Scalars['Boolean']>;
  /** Included in the specified list. */
  in?: InputMaybe<Array<Scalars['Boolean']>>;
  /** Is null (if `true` is specified) or is not null (if `false` is specified). */
  isNull?: InputMaybe<Scalars['Boolean']>;
  /** Less than the specified value. */
  lessThan?: InputMaybe<Scalars['Boolean']>;
  /** Less than or equal to the specified value. */
  lessThanOrEqualTo?: InputMaybe<Scalars['Boolean']>;
  /** Equal to the specified value, treating null like an ordinary value. */
  notDistinctFrom?: InputMaybe<Scalars['Boolean']>;
  /** Not equal to the specified value. */
  notEqualTo?: InputMaybe<Scalars['Boolean']>;
  /** Not included in the specified list. */
  notIn?: InputMaybe<Array<Scalars['Boolean']>>;
};

export type BotUser = Node & {
  __typename?: 'BotUser';
  id: Scalars['String'];
  /** A globally unique identifier. Can be used in various places throughout the system to identify this single value. */
  nodeId: Scalars['ID'];
};

/** A condition to be used against `BotUser` object types. All fields are tested for equality and combined with a logical ‘and.’ */
export type BotUserCondition = {
  /** Checks for equality with the object’s `id` field. */
  id?: InputMaybe<Scalars['String']>;
};

/** A filter to be used against `BotUser` object types. All fields are combined with a logical ‘and.’ */
export type BotUserFilter = {
  /** Checks for all expressions in this list. */
  and?: InputMaybe<Array<BotUserFilter>>;
  /** Filter by the object’s `id` field. */
  id?: InputMaybe<StringFilter>;
  /** Negates the expression. */
  not?: InputMaybe<BotUserFilter>;
  /** Checks for any expressions in this list. */
  or?: InputMaybe<Array<BotUserFilter>>;
};

/** An input for mutations affecting `BotUser` */
export type BotUserInput = {
  id: Scalars['String'];
};

/** Represents an update to a `BotUser`. Fields that are set will be updated. */
export type BotUserPatch = {
  id?: InputMaybe<Scalars['String']>;
};

/** A connection to a list of `BotUser` values. */
export type BotUsersConnection = {
  __typename?: 'BotUsersConnection';
  /** A list of edges which contains the `BotUser` and cursor to aid in pagination. */
  edges: Array<BotUsersEdge>;
  /** A list of `BotUser` objects. */
  nodes: Array<Maybe<BotUser>>;
  /** Information to aid in pagination. */
  pageInfo: PageInfo;
  /** The count of *all* `BotUser` you could get from the connection. */
  totalCount: Scalars['Int'];
};

/** A `BotUser` edge in the connection. */
export type BotUsersEdge = {
  __typename?: 'BotUsersEdge';
  /** A cursor for use in pagination. */
  cursor?: Maybe<Scalars['Cursor']>;
  /** The `BotUser` at the end of the edge. */
  node?: Maybe<BotUser>;
};

/** Methods to use when ordering `BotUser`. */
export const BotUsersOrderBy = {
  IdAsc: 'ID_ASC',
  IdDesc: 'ID_DESC',
  Natural: 'NATURAL',
  PrimaryKeyAsc: 'PRIMARY_KEY_ASC',
  PrimaryKeyDesc: 'PRIMARY_KEY_DESC'
} as const;

export type BotUsersOrderBy = typeof BotUsersOrderBy[keyof typeof BotUsersOrderBy];
export type Card = {
  __typename?: 'Card';
  blocklyWorkspace?: Maybe<Scalars['String']>;
  cardScript?: Maybe<Scalars['JSON']>;
  collectible?: Maybe<Scalars['Boolean']>;
  cost?: Maybe<Scalars['Int']>;
  createdAt: Scalars['Datetime'];
  createdBy: Scalars['String'];
  id: Scalars['String'];
  isArchived: Scalars['Boolean'];
  isPublished: Scalars['Boolean'];
  lastModified: Scalars['Datetime'];
  succession: Scalars['BigInt'];
  type?: Maybe<Scalars['String']>;
  uri?: Maybe<Scalars['String']>;
};

/** All input for the `cardCatalogueFormats` mutation. */
export type CardCatalogueFormatsInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
};

/** The output of our `cardCatalogueFormats` mutation. */
export type CardCatalogueFormatsPayload = {
  __typename?: 'CardCatalogueFormatsPayload';
  cards?: Maybe<Array<Maybe<Card>>>;
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};

/** All input for the `cardCatalogueGetBannedDraftCards` mutation. */
export type CardCatalogueGetBannedDraftCardsInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
};

/** The output of our `cardCatalogueGetBannedDraftCards` mutation. */
export type CardCatalogueGetBannedDraftCardsPayload = {
  __typename?: 'CardCatalogueGetBannedDraftCardsPayload';
  cardIds?: Maybe<Array<Maybe<Scalars['String']>>>;
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};

/** All input for the `cardCatalogueGetBaseClasses` mutation. */
export type CardCatalogueGetBaseClassesInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  sets?: InputMaybe<Array<InputMaybe<Scalars['String']>>>;
};

/** The output of our `cardCatalogueGetBaseClasses` mutation. */
export type CardCatalogueGetBaseClassesPayload = {
  __typename?: 'CardCatalogueGetBaseClassesPayload';
  cards?: Maybe<Array<Maybe<Card>>>;
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};

/** All input for the `cardCatalogueGetCardById` mutation. */
export type CardCatalogueGetCardByIdInput = {
  cardId?: InputMaybe<Scalars['String']>;
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
};

/** The output of our `cardCatalogueGetCardById` mutation. */
export type CardCatalogueGetCardByIdPayload = {
  __typename?: 'CardCatalogueGetCardByIdPayload';
  cards?: Maybe<Array<Maybe<Card>>>;
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};

/** All input for the `cardCatalogueGetCardByNameAndClass` mutation. */
export type CardCatalogueGetCardByNameAndClassInput = {
  cardName?: InputMaybe<Scalars['String']>;
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  heroClass?: InputMaybe<Scalars['String']>;
};

/** The output of our `cardCatalogueGetCardByNameAndClass` mutation. */
export type CardCatalogueGetCardByNameAndClassPayload = {
  __typename?: 'CardCatalogueGetCardByNameAndClassPayload';
  card?: Maybe<Card>;
  /** An edge for our `Card`. May be used by Relay 1. */
  cardEdge?: Maybe<CardsEdge>;
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our `cardCatalogueGetCardByNameAndClass` mutation. */
export type CardCatalogueGetCardByNameAndClassPayloadCardEdgeArgs = {
  orderBy?: InputMaybe<Array<CardsOrderBy>>;
};

/** All input for the `cardCatalogueGetCardByName` mutation. */
export type CardCatalogueGetCardByNameInput = {
  cardName?: InputMaybe<Scalars['String']>;
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
};

/** The output of our `cardCatalogueGetCardByName` mutation. */
export type CardCatalogueGetCardByNamePayload = {
  __typename?: 'CardCatalogueGetCardByNamePayload';
  card?: Maybe<Card>;
  /** An edge for our `Card`. May be used by Relay 1. */
  cardEdge?: Maybe<CardsEdge>;
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our `cardCatalogueGetCardByName` mutation. */
export type CardCatalogueGetCardByNamePayloadCardEdgeArgs = {
  orderBy?: InputMaybe<Array<CardsOrderBy>>;
};

/** All input for the `cardCatalogueGetClassCards` mutation. */
export type CardCatalogueGetClassCardsInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
};

/** The output of our `cardCatalogueGetClassCards` mutation. */
export type CardCatalogueGetClassCardsPayload = {
  __typename?: 'CardCatalogueGetClassCardsPayload';
  cards?: Maybe<Array<Maybe<Card>>>;
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};

/** All input for the `cardCatalogueGetFormat` mutation. */
export type CardCatalogueGetFormatInput = {
  cardName?: InputMaybe<Scalars['String']>;
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
};

/** The output of our `cardCatalogueGetFormat` mutation. */
export type CardCatalogueGetFormatPayload = {
  __typename?: 'CardCatalogueGetFormatPayload';
  cards?: Maybe<Array<Maybe<Card>>>;
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};

/** All input for the `cardCatalogueGetHardRemovalCards` mutation. */
export type CardCatalogueGetHardRemovalCardsInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
};

/** The output of our `cardCatalogueGetHardRemovalCards` mutation. */
export type CardCatalogueGetHardRemovalCardsPayload = {
  __typename?: 'CardCatalogueGetHardRemovalCardsPayload';
  cardIds?: Maybe<Array<Maybe<Scalars['String']>>>;
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};

/** All input for the `cardCatalogueGetHeroCard` mutation. */
export type CardCatalogueGetHeroCardInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  heroClass?: InputMaybe<Scalars['String']>;
};

/** The output of our `cardCatalogueGetHeroCard` mutation. */
export type CardCatalogueGetHeroCardPayload = {
  __typename?: 'CardCatalogueGetHeroCardPayload';
  card?: Maybe<Card>;
  /** An edge for our `Card`. May be used by Relay 1. */
  cardEdge?: Maybe<CardsEdge>;
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our `cardCatalogueGetHeroCard` mutation. */
export type CardCatalogueGetHeroCardPayloadCardEdgeArgs = {
  orderBy?: InputMaybe<Array<CardsOrderBy>>;
};

/** All input for the `cardCatalogueQuery` mutation. */
export type CardCatalogueQueryInput = {
  attribute?: InputMaybe<Scalars['String']>;
  cardType?: InputMaybe<Scalars['String']>;
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  heroClass?: InputMaybe<Scalars['String']>;
  rarity?: InputMaybe<Scalars['String']>;
  sets?: InputMaybe<Array<InputMaybe<Scalars['String']>>>;
};

/** The output of our `cardCatalogueQuery` mutation. */
export type CardCatalogueQueryPayload = {
  __typename?: 'CardCatalogueQueryPayload';
  cards?: Maybe<Array<Maybe<Card>>>;
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};

/** A condition to be used against `Card` object types. All fields are tested for equality and combined with a logical ‘and.’ */
export type CardCondition = {
  /** Checks for equality with the object’s `blocklyWorkspace` field. */
  blocklyWorkspace?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `cardScript` field. */
  cardScript?: InputMaybe<Scalars['JSON']>;
  /** Checks for equality with the object’s `createdAt` field. */
  createdAt?: InputMaybe<Scalars['Datetime']>;
  /** Checks for equality with the object’s `createdBy` field. */
  createdBy?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `id` field. */
  id?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `isArchived` field. */
  isArchived?: InputMaybe<Scalars['Boolean']>;
  /** Checks for equality with the object’s `isPublished` field. */
  isPublished?: InputMaybe<Scalars['Boolean']>;
  /** Checks for equality with the object’s `lastModified` field. */
  lastModified?: InputMaybe<Scalars['Datetime']>;
  /** Checks for equality with the object’s `succession` field. */
  succession?: InputMaybe<Scalars['BigInt']>;
  /** Checks for equality with the object’s `uri` field. */
  uri?: InputMaybe<Scalars['String']>;
};

/** A filter to be used against `Card` object types. All fields are combined with a logical ‘and.’ */
export type CardFilter = {
  /** Checks for all expressions in this list. */
  and?: InputMaybe<Array<CardFilter>>;
  /** Filter by the object’s `cardScript` field. */
  cardScript?: InputMaybe<JsonFilter>;
  /** Filter by the object’s `collectible` field. */
  collectible?: InputMaybe<BooleanFilter>;
  /** Filter by the object’s `cost` field. */
  cost?: InputMaybe<IntFilter>;
  /** Filter by the object’s `createdAt` field. */
  createdAt?: InputMaybe<DatetimeFilter>;
  /** Filter by the object’s `createdBy` field. */
  createdBy?: InputMaybe<StringFilter>;
  /** Filter by the object’s `id` field. */
  id?: InputMaybe<StringFilter>;
  /** Filter by the object’s `isArchived` field. */
  isArchived?: InputMaybe<BooleanFilter>;
  /** Filter by the object’s `isPublished` field. */
  isPublished?: InputMaybe<BooleanFilter>;
  /** Filter by the object’s `lastModified` field. */
  lastModified?: InputMaybe<DatetimeFilter>;
  /** Negates the expression. */
  not?: InputMaybe<CardFilter>;
  /** Checks for any expressions in this list. */
  or?: InputMaybe<Array<CardFilter>>;
  /** Filter by the object’s `succession` field. */
  succession?: InputMaybe<BigIntFilter>;
  /** Filter by the object’s `type` field. */
  type?: InputMaybe<StringFilter>;
  /** Filter by the object’s `uri` field. */
  uri?: InputMaybe<StringFilter>;
};

/** An input for mutations affecting `Card` */
export type CardInput = {
  blocklyWorkspace?: InputMaybe<Scalars['String']>;
  cardScript?: InputMaybe<Scalars['JSON']>;
  createdAt?: InputMaybe<Scalars['Datetime']>;
  createdBy: Scalars['String'];
  id: Scalars['String'];
  isArchived?: InputMaybe<Scalars['Boolean']>;
  isPublished?: InputMaybe<Scalars['Boolean']>;
  lastModified?: InputMaybe<Scalars['Datetime']>;
  uri?: InputMaybe<Scalars['String']>;
};

/** A connection to a list of `Card` values. */
export type CardsConnection = {
  __typename?: 'CardsConnection';
  /** A list of edges which contains the `Card` and cursor to aid in pagination. */
  edges: Array<CardsEdge>;
  /** A list of `Card` objects. */
  nodes: Array<Maybe<Card>>;
  /** Information to aid in pagination. */
  pageInfo: PageInfo;
  /** The count of *all* `Card` you could get from the connection. */
  totalCount: Scalars['Int'];
};

/** A `Card` edge in the connection. */
export type CardsEdge = {
  __typename?: 'CardsEdge';
  /** A cursor for use in pagination. */
  cursor?: Maybe<Scalars['Cursor']>;
  /** The `Card` at the end of the edge. */
  node?: Maybe<Card>;
};

export type CardsInDeck = Node & {
  __typename?: 'CardsInDeck';
  cardByCardId?: Maybe<Card>;
  /** cannot delete cards that are currently used in decks */
  cardId: Scalars['String'];
  /** Reads a single `Deck` that is related to this `CardsInDeck`. */
  deckByDeckId?: Maybe<Deck>;
  /** deleting a deck deletes all its card references */
  deckId: Scalars['String'];
  id: Scalars['BigInt'];
  /** A globally unique identifier. Can be used in various places throughout the system to identify this single value. */
  nodeId: Scalars['ID'];
};

/**
 * A condition to be used against `CardsInDeck` object types. All fields are tested
 * for equality and combined with a logical ‘and.’
 */
export type CardsInDeckCondition = {
  /** Checks for equality with the object’s `cardId` field. */
  cardId?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `deckId` field. */
  deckId?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `id` field. */
  id?: InputMaybe<Scalars['BigInt']>;
};

/** A filter to be used against `CardsInDeck` object types. All fields are combined with a logical ‘and.’ */
export type CardsInDeckFilter = {
  /** Checks for all expressions in this list. */
  and?: InputMaybe<Array<CardsInDeckFilter>>;
  /** Filter by the object’s `cardId` field. */
  cardId?: InputMaybe<StringFilter>;
  /** Filter by the object’s `deckId` field. */
  deckId?: InputMaybe<StringFilter>;
  /** Filter by the object’s `id` field. */
  id?: InputMaybe<BigIntFilter>;
  /** Negates the expression. */
  not?: InputMaybe<CardsInDeckFilter>;
  /** Checks for any expressions in this list. */
  or?: InputMaybe<Array<CardsInDeckFilter>>;
};

/** An input for mutations affecting `CardsInDeck` */
export type CardsInDeckInput = {
  /** cannot delete cards that are currently used in decks */
  cardId: Scalars['String'];
  /** deleting a deck deletes all its card references */
  deckId: Scalars['String'];
};

/** Represents an update to a `CardsInDeck`. Fields that are set will be updated. */
export type CardsInDeckPatch = {
  /** cannot delete cards that are currently used in decks */
  cardId?: InputMaybe<Scalars['String']>;
  /** deleting a deck deletes all its card references */
  deckId?: InputMaybe<Scalars['String']>;
};

/** A connection to a list of `CardsInDeck` values. */
export type CardsInDecksConnection = {
  __typename?: 'CardsInDecksConnection';
  /** A list of edges which contains the `CardsInDeck` and cursor to aid in pagination. */
  edges: Array<CardsInDecksEdge>;
  /** A list of `CardsInDeck` objects. */
  nodes: Array<Maybe<CardsInDeck>>;
  /** Information to aid in pagination. */
  pageInfo: PageInfo;
  /** The count of *all* `CardsInDeck` you could get from the connection. */
  totalCount: Scalars['Int'];
};

/** A `CardsInDeck` edge in the connection. */
export type CardsInDecksEdge = {
  __typename?: 'CardsInDecksEdge';
  /** A cursor for use in pagination. */
  cursor?: Maybe<Scalars['Cursor']>;
  /** The `CardsInDeck` at the end of the edge. */
  node?: Maybe<CardsInDeck>;
};

/** Methods to use when ordering `CardsInDeck`. */
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
/** Methods to use when ordering `Card`. */
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
  IsArchivedAsc: 'IS_ARCHIVED_ASC',
  IsArchivedDesc: 'IS_ARCHIVED_DESC',
  IsPublishedAsc: 'IS_PUBLISHED_ASC',
  IsPublishedDesc: 'IS_PUBLISHED_DESC',
  LastModifiedAsc: 'LAST_MODIFIED_ASC',
  LastModifiedDesc: 'LAST_MODIFIED_DESC',
  Natural: 'NATURAL',
  SuccessionAsc: 'SUCCESSION_ASC',
  SuccessionDesc: 'SUCCESSION_DESC',
  UriAsc: 'URI_ASC',
  UriDesc: 'URI_DESC'
} as const;

export type CardsOrderBy = typeof CardsOrderBy[keyof typeof CardsOrderBy];
export type Class = {
  __typename?: 'Class';
  cardScript?: Maybe<Scalars['JSON']>;
  class?: Maybe<Scalars['String']>;
  collectible?: Maybe<Scalars['Boolean']>;
  createdBy?: Maybe<Scalars['String']>;
  id?: Maybe<Scalars['String']>;
  isPublished?: Maybe<Scalars['Boolean']>;
  name?: Maybe<Scalars['String']>;
};

/** A condition to be used against `Class` object types. All fields are tested for equality and combined with a logical ‘and.’ */
export type ClassCondition = {
  /** Checks for equality with the object’s `cardScript` field. */
  cardScript?: InputMaybe<Scalars['JSON']>;
  /** Checks for equality with the object’s `class` field. */
  class?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `collectible` field. */
  collectible?: InputMaybe<Scalars['Boolean']>;
  /** Checks for equality with the object’s `createdBy` field. */
  createdBy?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `id` field. */
  id?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `isPublished` field. */
  isPublished?: InputMaybe<Scalars['Boolean']>;
  /** Checks for equality with the object’s `name` field. */
  name?: InputMaybe<Scalars['String']>;
};

/** A filter to be used against `Class` object types. All fields are combined with a logical ‘and.’ */
export type ClassFilter = {
  /** Checks for all expressions in this list. */
  and?: InputMaybe<Array<ClassFilter>>;
  /** Filter by the object’s `cardScript` field. */
  cardScript?: InputMaybe<JsonFilter>;
  /** Filter by the object’s `class` field. */
  class?: InputMaybe<StringFilter>;
  /** Filter by the object’s `collectible` field. */
  collectible?: InputMaybe<BooleanFilter>;
  /** Filter by the object’s `createdBy` field. */
  createdBy?: InputMaybe<StringFilter>;
  /** Filter by the object’s `id` field. */
  id?: InputMaybe<StringFilter>;
  /** Filter by the object’s `isPublished` field. */
  isPublished?: InputMaybe<BooleanFilter>;
  /** Filter by the object’s `name` field. */
  name?: InputMaybe<StringFilter>;
  /** Negates the expression. */
  not?: InputMaybe<ClassFilter>;
  /** Checks for any expressions in this list. */
  or?: InputMaybe<Array<ClassFilter>>;
};

/** A connection to a list of `Class` values. */
export type ClassesConnection = {
  __typename?: 'ClassesConnection';
  /** A list of edges which contains the `Class` and cursor to aid in pagination. */
  edges: Array<ClassesEdge>;
  /** A list of `Class` objects. */
  nodes: Array<Maybe<Class>>;
  /** Information to aid in pagination. */
  pageInfo: PageInfo;
  /** The count of *all* `Class` you could get from the connection. */
  totalCount: Scalars['Int'];
};

/** A `Class` edge in the connection. */
export type ClassesEdge = {
  __typename?: 'ClassesEdge';
  /** A cursor for use in pagination. */
  cursor?: Maybe<Scalars['Cursor']>;
  /** The `Class` at the end of the edge. */
  node?: Maybe<Class>;
};

/** Methods to use when ordering `Class`. */
export const ClassesOrderBy = {
  CardScriptAsc: 'CARD_SCRIPT_ASC',
  CardScriptDesc: 'CARD_SCRIPT_DESC',
  ClassAsc: 'CLASS_ASC',
  ClassDesc: 'CLASS_DESC',
  CollectibleAsc: 'COLLECTIBLE_ASC',
  CollectibleDesc: 'COLLECTIBLE_DESC',
  CreatedByAsc: 'CREATED_BY_ASC',
  CreatedByDesc: 'CREATED_BY_DESC',
  IdAsc: 'ID_ASC',
  IdDesc: 'ID_DESC',
  IsPublishedAsc: 'IS_PUBLISHED_ASC',
  IsPublishedDesc: 'IS_PUBLISHED_DESC',
  NameAsc: 'NAME_ASC',
  NameDesc: 'NAME_DESC',
  Natural: 'NATURAL'
} as const;

export type ClassesOrderBy = typeof ClassesOrderBy[keyof typeof ClassesOrderBy];
/** All input for the `clusteredGamesUpdateGameAndUsers` mutation. */
export type ClusteredGamesUpdateGameAndUsersInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  pGameId?: InputMaybe<Scalars['BigInt']>;
  pTrace?: InputMaybe<Scalars['JSON']>;
  pUserIdLoser?: InputMaybe<Scalars['String']>;
  pUserIdWinner?: InputMaybe<Scalars['String']>;
};

/** The output of our `clusteredGamesUpdateGameAndUsers` mutation. */
export type ClusteredGamesUpdateGameAndUsersPayload = {
  __typename?: 'ClusteredGamesUpdateGameAndUsersPayload';
  boolean?: Maybe<Scalars['Boolean']>;
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};

export type CollectionCard = {
  __typename?: 'CollectionCard';
  blocklyWorkspace?: Maybe<Scalars['String']>;
  cardScript?: Maybe<Scalars['JSON']>;
  class?: Maybe<Scalars['String']>;
  collectible?: Maybe<Scalars['Boolean']>;
  cost?: Maybe<Scalars['Int']>;
  createdAt?: Maybe<Scalars['Datetime']>;
  createdBy?: Maybe<Scalars['String']>;
  id?: Maybe<Scalars['String']>;
  lastModified?: Maybe<Scalars['Datetime']>;
  name?: Maybe<Scalars['String']>;
  searchMessage?: Maybe<Scalars['String']>;
  type?: Maybe<Scalars['String']>;
};

/**
 * A condition to be used against `CollectionCard` object types. All fields are
 * tested for equality and combined with a logical ‘and.’
 */
export type CollectionCardCondition = {
  /** Checks for equality with the object’s `blocklyWorkspace` field. */
  blocklyWorkspace?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `cardScript` field. */
  cardScript?: InputMaybe<Scalars['JSON']>;
  /** Checks for equality with the object’s `class` field. */
  class?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `collectible` field. */
  collectible?: InputMaybe<Scalars['Boolean']>;
  /** Checks for equality with the object’s `cost` field. */
  cost?: InputMaybe<Scalars['Int']>;
  /** Checks for equality with the object’s `createdAt` field. */
  createdAt?: InputMaybe<Scalars['Datetime']>;
  /** Checks for equality with the object’s `createdBy` field. */
  createdBy?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `id` field. */
  id?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `lastModified` field. */
  lastModified?: InputMaybe<Scalars['Datetime']>;
  /** Checks for equality with the object’s `name` field. */
  name?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `searchMessage` field. */
  searchMessage?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `type` field. */
  type?: InputMaybe<Scalars['String']>;
};

/** A filter to be used against `CollectionCard` object types. All fields are combined with a logical ‘and.’ */
export type CollectionCardFilter = {
  /** Checks for all expressions in this list. */
  and?: InputMaybe<Array<CollectionCardFilter>>;
  /** Filter by the object’s `cardScript` field. */
  cardScript?: InputMaybe<JsonFilter>;
  /** Filter by the object’s `class` field. */
  class?: InputMaybe<StringFilter>;
  /** Filter by the object’s `collectible` field. */
  collectible?: InputMaybe<BooleanFilter>;
  /** Filter by the object’s `cost` field. */
  cost?: InputMaybe<IntFilter>;
  /** Filter by the object’s `createdAt` field. */
  createdAt?: InputMaybe<DatetimeFilter>;
  /** Filter by the object’s `createdBy` field. */
  createdBy?: InputMaybe<StringFilter>;
  /** Filter by the object’s `id` field. */
  id?: InputMaybe<StringFilter>;
  /** Filter by the object’s `lastModified` field. */
  lastModified?: InputMaybe<DatetimeFilter>;
  /** Filter by the object’s `name` field. */
  name?: InputMaybe<StringFilter>;
  /** Negates the expression. */
  not?: InputMaybe<CollectionCardFilter>;
  /** Checks for any expressions in this list. */
  or?: InputMaybe<Array<CollectionCardFilter>>;
  /** Filter by the object’s `searchMessage` field. */
  searchMessage?: InputMaybe<StringFilter>;
  /** Filter by the object’s `type` field. */
  type?: InputMaybe<StringFilter>;
};

/** A connection to a list of `CollectionCard` values. */
export type CollectionCardsConnection = {
  __typename?: 'CollectionCardsConnection';
  /** A list of edges which contains the `CollectionCard` and cursor to aid in pagination. */
  edges: Array<CollectionCardsEdge>;
  /** A list of `CollectionCard` objects. */
  nodes: Array<Maybe<CollectionCard>>;
  /** Information to aid in pagination. */
  pageInfo: PageInfo;
  /** The count of *all* `CollectionCard` you could get from the connection. */
  totalCount: Scalars['Int'];
};

/** A `CollectionCard` edge in the connection. */
export type CollectionCardsEdge = {
  __typename?: 'CollectionCardsEdge';
  /** A cursor for use in pagination. */
  cursor?: Maybe<Scalars['Cursor']>;
  /** The `CollectionCard` at the end of the edge. */
  node?: Maybe<CollectionCard>;
};

/** Methods to use when ordering `CollectionCard`. */
export const CollectionCardsOrderBy = {
  BlocklyWorkspaceAsc: 'BLOCKLY_WORKSPACE_ASC',
  BlocklyWorkspaceDesc: 'BLOCKLY_WORKSPACE_DESC',
  CardScriptAsc: 'CARD_SCRIPT_ASC',
  CardScriptDesc: 'CARD_SCRIPT_DESC',
  ClassAsc: 'CLASS_ASC',
  ClassDesc: 'CLASS_DESC',
  CollectibleAsc: 'COLLECTIBLE_ASC',
  CollectibleDesc: 'COLLECTIBLE_DESC',
  CostAsc: 'COST_ASC',
  CostDesc: 'COST_DESC',
  CreatedAtAsc: 'CREATED_AT_ASC',
  CreatedAtDesc: 'CREATED_AT_DESC',
  CreatedByAsc: 'CREATED_BY_ASC',
  CreatedByDesc: 'CREATED_BY_DESC',
  IdAsc: 'ID_ASC',
  IdDesc: 'ID_DESC',
  LastModifiedAsc: 'LAST_MODIFIED_ASC',
  LastModifiedDesc: 'LAST_MODIFIED_DESC',
  NameAsc: 'NAME_ASC',
  NameDesc: 'NAME_DESC',
  Natural: 'NATURAL',
  SearchMessageAsc: 'SEARCH_MESSAGE_ASC',
  SearchMessageDesc: 'SEARCH_MESSAGE_DESC',
  TypeAsc: 'TYPE_ASC',
  TypeDesc: 'TYPE_DESC'
} as const;

export type CollectionCardsOrderBy = typeof CollectionCardsOrderBy[keyof typeof CollectionCardsOrderBy];
/** All input for the create `BannedDraftCard` mutation. */
export type CreateBannedDraftCardInput = {
  /** The `BannedDraftCard` to be created by this mutation. */
  bannedDraftCard: BannedDraftCardInput;
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
};

/** The output of our create `BannedDraftCard` mutation. */
export type CreateBannedDraftCardPayload = {
  __typename?: 'CreateBannedDraftCardPayload';
  /** The `BannedDraftCard` that was created by this mutation. */
  bannedDraftCard?: Maybe<BannedDraftCard>;
  /** An edge for our `BannedDraftCard`. May be used by Relay 1. */
  bannedDraftCardEdge?: Maybe<BannedDraftCardsEdge>;
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our create `BannedDraftCard` mutation. */
export type CreateBannedDraftCardPayloadBannedDraftCardEdgeArgs = {
  orderBy?: InputMaybe<Array<BannedDraftCardsOrderBy>>;
};

/** All input for the create `BotUser` mutation. */
export type CreateBotUserInput = {
  /** The `BotUser` to be created by this mutation. */
  botUser: BotUserInput;
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
};

/** The output of our create `BotUser` mutation. */
export type CreateBotUserPayload = {
  __typename?: 'CreateBotUserPayload';
  /** The `BotUser` that was created by this mutation. */
  botUser?: Maybe<BotUser>;
  /** An edge for our `BotUser`. May be used by Relay 1. */
  botUserEdge?: Maybe<BotUsersEdge>;
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our create `BotUser` mutation. */
export type CreateBotUserPayloadBotUserEdgeArgs = {
  orderBy?: InputMaybe<Array<BotUsersOrderBy>>;
};

/** All input for the create `Card` mutation. */
export type CreateCardInput = {
  /** The `Card` to be created by this mutation. */
  card: CardInput;
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
};

/** The output of our create `Card` mutation. */
export type CreateCardPayload = {
  __typename?: 'CreateCardPayload';
  /** The `Card` that was created by this mutation. */
  card?: Maybe<Card>;
  /** An edge for our `Card`. May be used by Relay 1. */
  cardEdge?: Maybe<CardsEdge>;
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our create `Card` mutation. */
export type CreateCardPayloadCardEdgeArgs = {
  orderBy?: InputMaybe<Array<CardsOrderBy>>;
};

/** All input for the create `CardsInDeck` mutation. */
export type CreateCardsInDeckInput = {
  /** The `CardsInDeck` to be created by this mutation. */
  cardsInDeck: CardsInDeckInput;
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
};

/** The output of our create `CardsInDeck` mutation. */
export type CreateCardsInDeckPayload = {
  __typename?: 'CreateCardsInDeckPayload';
  /** The `CardsInDeck` that was created by this mutation. */
  cardsInDeck?: Maybe<CardsInDeck>;
  /** An edge for our `CardsInDeck`. May be used by Relay 1. */
  cardsInDeckEdge?: Maybe<CardsInDecksEdge>;
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Reads a single `Deck` that is related to this `CardsInDeck`. */
  deckByDeckId?: Maybe<Deck>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our create `CardsInDeck` mutation. */
export type CreateCardsInDeckPayloadCardsInDeckEdgeArgs = {
  orderBy?: InputMaybe<Array<CardsInDecksOrderBy>>;
};

/** All input for the create `Deck` mutation. */
export type CreateDeckInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The `Deck` to be created by this mutation. */
  deck: DeckInput;
};

/** The output of our create `Deck` mutation. */
export type CreateDeckPayload = {
  __typename?: 'CreateDeckPayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** The `Deck` that was created by this mutation. */
  deck?: Maybe<Deck>;
  /** An edge for our `Deck`. May be used by Relay 1. */
  deckEdge?: Maybe<DecksEdge>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our create `Deck` mutation. */
export type CreateDeckPayloadDeckEdgeArgs = {
  orderBy?: InputMaybe<Array<DecksOrderBy>>;
};

/** All input for the create `DeckPlayerAttributeTuple` mutation. */
export type CreateDeckPlayerAttributeTupleInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The `DeckPlayerAttributeTuple` to be created by this mutation. */
  deckPlayerAttributeTuple: DeckPlayerAttributeTupleInput;
};

/** The output of our create `DeckPlayerAttributeTuple` mutation. */
export type CreateDeckPlayerAttributeTuplePayload = {
  __typename?: 'CreateDeckPlayerAttributeTuplePayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Reads a single `Deck` that is related to this `DeckPlayerAttributeTuple`. */
  deckByDeckId?: Maybe<Deck>;
  /** The `DeckPlayerAttributeTuple` that was created by this mutation. */
  deckPlayerAttributeTuple?: Maybe<DeckPlayerAttributeTuple>;
  /** An edge for our `DeckPlayerAttributeTuple`. May be used by Relay 1. */
  deckPlayerAttributeTupleEdge?: Maybe<DeckPlayerAttributeTuplesEdge>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our create `DeckPlayerAttributeTuple` mutation. */
export type CreateDeckPlayerAttributeTuplePayloadDeckPlayerAttributeTupleEdgeArgs = {
  orderBy?: InputMaybe<Array<DeckPlayerAttributeTuplesOrderBy>>;
};

/** All input for the create `DeckShare` mutation. */
export type CreateDeckShareInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The `DeckShare` to be created by this mutation. */
  deckShare: DeckShareInput;
};

/** The output of our create `DeckShare` mutation. */
export type CreateDeckSharePayload = {
  __typename?: 'CreateDeckSharePayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Reads a single `Deck` that is related to this `DeckShare`. */
  deckByDeckId?: Maybe<Deck>;
  /** The `DeckShare` that was created by this mutation. */
  deckShare?: Maybe<DeckShare>;
  /** An edge for our `DeckShare`. May be used by Relay 1. */
  deckShareEdge?: Maybe<DeckSharesEdge>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our create `DeckShare` mutation. */
export type CreateDeckSharePayloadDeckShareEdgeArgs = {
  orderBy?: InputMaybe<Array<DeckSharesOrderBy>>;
};

/** All input for the `createDeckWithCards` mutation. */
export type CreateDeckWithCardsInput = {
  cardIds?: InputMaybe<Array<InputMaybe<Scalars['String']>>>;
  classHero?: InputMaybe<Scalars['String']>;
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  deckName?: InputMaybe<Scalars['String']>;
  formatName?: InputMaybe<Scalars['String']>;
};

/** The output of our `createDeckWithCards` mutation. */
export type CreateDeckWithCardsPayload = {
  __typename?: 'CreateDeckWithCardsPayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  deck?: Maybe<Deck>;
  /** An edge for our `Deck`. May be used by Relay 1. */
  deckEdge?: Maybe<DecksEdge>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our `createDeckWithCards` mutation. */
export type CreateDeckWithCardsPayloadDeckEdgeArgs = {
  orderBy?: InputMaybe<Array<DecksOrderBy>>;
};

/** All input for the create `Friend` mutation. */
export type CreateFriendInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The `Friend` to be created by this mutation. */
  friend: FriendInput;
};

/** The output of our create `Friend` mutation. */
export type CreateFriendPayload = {
  __typename?: 'CreateFriendPayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** The `Friend` that was created by this mutation. */
  friend?: Maybe<Friend>;
  /** An edge for our `Friend`. May be used by Relay 1. */
  friendEdge?: Maybe<FriendsEdge>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our create `Friend` mutation. */
export type CreateFriendPayloadFriendEdgeArgs = {
  orderBy?: InputMaybe<Array<FriendsOrderBy>>;
};

/** All input for the create `Game` mutation. */
export type CreateGameInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The `Game` to be created by this mutation. */
  game: GameInput;
};

/** The output of our create `Game` mutation. */
export type CreateGamePayload = {
  __typename?: 'CreateGamePayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** The `Game` that was created by this mutation. */
  game?: Maybe<Game>;
  /** An edge for our `Game`. May be used by Relay 1. */
  gameEdge?: Maybe<GamesEdge>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our create `Game` mutation. */
export type CreateGamePayloadGameEdgeArgs = {
  orderBy?: InputMaybe<Array<GamesOrderBy>>;
};

/** All input for the create `GameUser` mutation. */
export type CreateGameUserInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The `GameUser` to be created by this mutation. */
  gameUser: GameUserInput;
};

/** The output of our create `GameUser` mutation. */
export type CreateGameUserPayload = {
  __typename?: 'CreateGameUserPayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Reads a single `Deck` that is related to this `GameUser`. */
  deckByDeckId?: Maybe<Deck>;
  /** Reads a single `Game` that is related to this `GameUser`. */
  gameByGameId?: Maybe<Game>;
  /** The `GameUser` that was created by this mutation. */
  gameUser?: Maybe<GameUser>;
  /** An edge for our `GameUser`. May be used by Relay 1. */
  gameUserEdge?: Maybe<GameUsersEdge>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our create `GameUser` mutation. */
export type CreateGameUserPayloadGameUserEdgeArgs = {
  orderBy?: InputMaybe<Array<GameUsersOrderBy>>;
};

/** All input for the create `Guest` mutation. */
export type CreateGuestInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The `Guest` to be created by this mutation. */
  guest: GuestInput;
};

/** The output of our create `Guest` mutation. */
export type CreateGuestPayload = {
  __typename?: 'CreateGuestPayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** The `Guest` that was created by this mutation. */
  guest?: Maybe<Guest>;
  /** An edge for our `Guest`. May be used by Relay 1. */
  guestEdge?: Maybe<GuestsEdge>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our create `Guest` mutation. */
export type CreateGuestPayloadGuestEdgeArgs = {
  orderBy?: InputMaybe<Array<GuestsOrderBy>>;
};

/** All input for the create `HardRemovalCard` mutation. */
export type CreateHardRemovalCardInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The `HardRemovalCard` to be created by this mutation. */
  hardRemovalCard: HardRemovalCardInput;
};

/** The output of our create `HardRemovalCard` mutation. */
export type CreateHardRemovalCardPayload = {
  __typename?: 'CreateHardRemovalCardPayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** The `HardRemovalCard` that was created by this mutation. */
  hardRemovalCard?: Maybe<HardRemovalCard>;
  /** An edge for our `HardRemovalCard`. May be used by Relay 1. */
  hardRemovalCardEdge?: Maybe<HardRemovalCardsEdge>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our create `HardRemovalCard` mutation. */
export type CreateHardRemovalCardPayloadHardRemovalCardEdgeArgs = {
  orderBy?: InputMaybe<Array<HardRemovalCardsOrderBy>>;
};

/** All input for the create `MatchmakingQueue` mutation. */
export type CreateMatchmakingQueueInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The `MatchmakingQueue` to be created by this mutation. */
  matchmakingQueue: MatchmakingQueueInput;
};

/** The output of our create `MatchmakingQueue` mutation. */
export type CreateMatchmakingQueuePayload = {
  __typename?: 'CreateMatchmakingQueuePayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** The `MatchmakingQueue` that was created by this mutation. */
  matchmakingQueue?: Maybe<MatchmakingQueue>;
  /** An edge for our `MatchmakingQueue`. May be used by Relay 1. */
  matchmakingQueueEdge?: Maybe<MatchmakingQueuesEdge>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our create `MatchmakingQueue` mutation. */
export type CreateMatchmakingQueuePayloadMatchmakingQueueEdgeArgs = {
  orderBy?: InputMaybe<Array<MatchmakingQueuesOrderBy>>;
};

/** All input for the create `MatchmakingTicket` mutation. */
export type CreateMatchmakingTicketInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The `MatchmakingTicket` to be created by this mutation. */
  matchmakingTicket: MatchmakingTicketInput;
};

/** The output of our create `MatchmakingTicket` mutation. */
export type CreateMatchmakingTicketPayload = {
  __typename?: 'CreateMatchmakingTicketPayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Reads a single `Deck` that is related to this `MatchmakingTicket`. */
  deckByBotDeckId?: Maybe<Deck>;
  /** Reads a single `Deck` that is related to this `MatchmakingTicket`. */
  deckByDeckId?: Maybe<Deck>;
  /** Reads a single `MatchmakingQueue` that is related to this `MatchmakingTicket`. */
  matchmakingQueueByQueueId?: Maybe<MatchmakingQueue>;
  /** The `MatchmakingTicket` that was created by this mutation. */
  matchmakingTicket?: Maybe<MatchmakingTicket>;
  /** An edge for our `MatchmakingTicket`. May be used by Relay 1. */
  matchmakingTicketEdge?: Maybe<MatchmakingTicketsEdge>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our create `MatchmakingTicket` mutation. */
export type CreateMatchmakingTicketPayloadMatchmakingTicketEdgeArgs = {
  orderBy?: InputMaybe<Array<MatchmakingTicketsOrderBy>>;
};

/** All input for the create `UserEntityAddon` mutation. */
export type CreateUserEntityAddonInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The `UserEntityAddon` to be created by this mutation. */
  userEntityAddon: UserEntityAddonInput;
};

/** The output of our create `UserEntityAddon` mutation. */
export type CreateUserEntityAddonPayload = {
  __typename?: 'CreateUserEntityAddonPayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
  /** The `UserEntityAddon` that was created by this mutation. */
  userEntityAddon?: Maybe<UserEntityAddon>;
  /** An edge for our `UserEntityAddon`. May be used by Relay 1. */
  userEntityAddonEdge?: Maybe<UserEntityAddonsEdge>;
};


/** The output of our create `UserEntityAddon` mutation. */
export type CreateUserEntityAddonPayloadUserEntityAddonEdgeArgs = {
  orderBy?: InputMaybe<Array<UserEntityAddonsOrderBy>>;
};

export type CurrentCard = {
  __typename?: 'CurrentCard';
  blocklyWorkspace?: Maybe<Scalars['String']>;
  cardScript?: Maybe<Scalars['JSON']>;
  createdAt?: Maybe<Scalars['Datetime']>;
  createdBy?: Maybe<Scalars['String']>;
  id?: Maybe<Scalars['String']>;
  isArchived?: Maybe<Scalars['Boolean']>;
  isPublished?: Maybe<Scalars['Boolean']>;
  lastModified?: Maybe<Scalars['Datetime']>;
  succession?: Maybe<Scalars['BigInt']>;
  uri?: Maybe<Scalars['String']>;
};

/**
 * A condition to be used against `CurrentCard` object types. All fields are tested
 * for equality and combined with a logical ‘and.’
 */
export type CurrentCardCondition = {
  /** Checks for equality with the object’s `blocklyWorkspace` field. */
  blocklyWorkspace?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `cardScript` field. */
  cardScript?: InputMaybe<Scalars['JSON']>;
  /** Checks for equality with the object’s `createdAt` field. */
  createdAt?: InputMaybe<Scalars['Datetime']>;
  /** Checks for equality with the object’s `createdBy` field. */
  createdBy?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `id` field. */
  id?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `isArchived` field. */
  isArchived?: InputMaybe<Scalars['Boolean']>;
  /** Checks for equality with the object’s `isPublished` field. */
  isPublished?: InputMaybe<Scalars['Boolean']>;
  /** Checks for equality with the object’s `lastModified` field. */
  lastModified?: InputMaybe<Scalars['Datetime']>;
  /** Checks for equality with the object’s `succession` field. */
  succession?: InputMaybe<Scalars['BigInt']>;
  /** Checks for equality with the object’s `uri` field. */
  uri?: InputMaybe<Scalars['String']>;
};

/** A filter to be used against `CurrentCard` object types. All fields are combined with a logical ‘and.’ */
export type CurrentCardFilter = {
  /** Checks for all expressions in this list. */
  and?: InputMaybe<Array<CurrentCardFilter>>;
  /** Filter by the object’s `cardScript` field. */
  cardScript?: InputMaybe<JsonFilter>;
  /** Filter by the object’s `createdAt` field. */
  createdAt?: InputMaybe<DatetimeFilter>;
  /** Filter by the object’s `createdBy` field. */
  createdBy?: InputMaybe<StringFilter>;
  /** Filter by the object’s `id` field. */
  id?: InputMaybe<StringFilter>;
  /** Filter by the object’s `isArchived` field. */
  isArchived?: InputMaybe<BooleanFilter>;
  /** Filter by the object’s `isPublished` field. */
  isPublished?: InputMaybe<BooleanFilter>;
  /** Filter by the object’s `lastModified` field. */
  lastModified?: InputMaybe<DatetimeFilter>;
  /** Negates the expression. */
  not?: InputMaybe<CurrentCardFilter>;
  /** Checks for any expressions in this list. */
  or?: InputMaybe<Array<CurrentCardFilter>>;
  /** Filter by the object’s `succession` field. */
  succession?: InputMaybe<BigIntFilter>;
  /** Filter by the object’s `uri` field. */
  uri?: InputMaybe<StringFilter>;
};

/** A connection to a list of `CurrentCard` values. */
export type CurrentCardsConnection = {
  __typename?: 'CurrentCardsConnection';
  /** A list of edges which contains the `CurrentCard` and cursor to aid in pagination. */
  edges: Array<CurrentCardsEdge>;
  /** A list of `CurrentCard` objects. */
  nodes: Array<Maybe<CurrentCard>>;
  /** Information to aid in pagination. */
  pageInfo: PageInfo;
  /** The count of *all* `CurrentCard` you could get from the connection. */
  totalCount: Scalars['Int'];
};

/** A `CurrentCard` edge in the connection. */
export type CurrentCardsEdge = {
  __typename?: 'CurrentCardsEdge';
  /** A cursor for use in pagination. */
  cursor?: Maybe<Scalars['Cursor']>;
  /** The `CurrentCard` at the end of the edge. */
  node?: Maybe<CurrentCard>;
};

/** Methods to use when ordering `CurrentCard`. */
export const CurrentCardsOrderBy = {
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
  IsArchivedAsc: 'IS_ARCHIVED_ASC',
  IsArchivedDesc: 'IS_ARCHIVED_DESC',
  IsPublishedAsc: 'IS_PUBLISHED_ASC',
  IsPublishedDesc: 'IS_PUBLISHED_DESC',
  LastModifiedAsc: 'LAST_MODIFIED_ASC',
  LastModifiedDesc: 'LAST_MODIFIED_DESC',
  Natural: 'NATURAL',
  SuccessionAsc: 'SUCCESSION_ASC',
  SuccessionDesc: 'SUCCESSION_DESC',
  UriAsc: 'URI_ASC',
  UriDesc: 'URI_DESC'
} as const;

export type CurrentCardsOrderBy = typeof CurrentCardsOrderBy[keyof typeof CurrentCardsOrderBy];
/** A filter to be used against Datetime fields. All fields are combined with a logical ‘and.’ */
export type DatetimeFilter = {
  /** Not equal to the specified value, treating null like an ordinary value. */
  distinctFrom?: InputMaybe<Scalars['Datetime']>;
  /** Equal to the specified value. */
  equalTo?: InputMaybe<Scalars['Datetime']>;
  /** Greater than the specified value. */
  greaterThan?: InputMaybe<Scalars['Datetime']>;
  /** Greater than or equal to the specified value. */
  greaterThanOrEqualTo?: InputMaybe<Scalars['Datetime']>;
  /** Included in the specified list. */
  in?: InputMaybe<Array<Scalars['Datetime']>>;
  /** Is null (if `true` is specified) or is not null (if `false` is specified). */
  isNull?: InputMaybe<Scalars['Boolean']>;
  /** Less than the specified value. */
  lessThan?: InputMaybe<Scalars['Datetime']>;
  /** Less than or equal to the specified value. */
  lessThanOrEqualTo?: InputMaybe<Scalars['Datetime']>;
  /** Equal to the specified value, treating null like an ordinary value. */
  notDistinctFrom?: InputMaybe<Scalars['Datetime']>;
  /** Not equal to the specified value. */
  notEqualTo?: InputMaybe<Scalars['Datetime']>;
  /** Not included in the specified list. */
  notIn?: InputMaybe<Array<Scalars['Datetime']>>;
};

export type Deck = Node & {
  __typename?: 'Deck';
  /** Reads and enables pagination through a set of `CardsInDeck`. */
  cardsInDecksByDeckId: CardsInDecksConnection;
  /** who created this deck originally */
  createdBy: Scalars['String'];
  /** Reads and enables pagination through a set of `DeckPlayerAttributeTuple`. */
  deckPlayerAttributeTuplesByDeckId: DeckPlayerAttributeTuplesConnection;
  /** Reads and enables pagination through a set of `DeckShare`. */
  deckSharesByDeckId: DeckSharesConnection;
  deckType: Scalars['Int'];
  format?: Maybe<Scalars['String']>;
  /** Reads and enables pagination through a set of `GameUser`. */
  gameUsersByDeckId: GameUsersConnection;
  heroClass?: Maybe<Scalars['String']>;
  id: Scalars['String'];
  /** premades always shared with all users by application logic */
  isPremade: Scalars['Boolean'];
  /** who last edited this deck */
  lastEditedBy: Scalars['String'];
  /** Reads and enables pagination through a set of `MatchmakingTicket`. */
  matchmakingTicketsByBotDeckId: MatchmakingTicketsConnection;
  /** Reads and enables pagination through a set of `MatchmakingTicket`. */
  matchmakingTicketsByDeckId: MatchmakingTicketsConnection;
  name?: Maybe<Scalars['String']>;
  /** A globally unique identifier. Can be used in various places throughout the system to identify this single value. */
  nodeId: Scalars['ID'];
  permittedToDuplicate: Scalars['Boolean'];
  trashed: Scalars['Boolean'];
};


export type DeckCardsInDecksByDeckIdArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<CardsInDeckCondition>;
  filter?: InputMaybe<CardsInDeckFilter>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<CardsInDecksOrderBy>>;
};


export type DeckDeckPlayerAttributeTuplesByDeckIdArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<DeckPlayerAttributeTupleCondition>;
  filter?: InputMaybe<DeckPlayerAttributeTupleFilter>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<DeckPlayerAttributeTuplesOrderBy>>;
};


export type DeckDeckSharesByDeckIdArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<DeckShareCondition>;
  filter?: InputMaybe<DeckShareFilter>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<DeckSharesOrderBy>>;
};


export type DeckGameUsersByDeckIdArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<GameUserCondition>;
  filter?: InputMaybe<GameUserFilter>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<GameUsersOrderBy>>;
};


export type DeckMatchmakingTicketsByBotDeckIdArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<MatchmakingTicketCondition>;
  filter?: InputMaybe<MatchmakingTicketFilter>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<MatchmakingTicketsOrderBy>>;
};


export type DeckMatchmakingTicketsByDeckIdArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<MatchmakingTicketCondition>;
  filter?: InputMaybe<MatchmakingTicketFilter>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<MatchmakingTicketsOrderBy>>;
};

/** A condition to be used against `Deck` object types. All fields are tested for equality and combined with a logical ‘and.’ */
export type DeckCondition = {
  /** Checks for equality with the object’s `createdBy` field. */
  createdBy?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `deckType` field. */
  deckType?: InputMaybe<Scalars['Int']>;
  /** Checks for equality with the object’s `format` field. */
  format?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `heroClass` field. */
  heroClass?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `id` field. */
  id?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `isPremade` field. */
  isPremade?: InputMaybe<Scalars['Boolean']>;
  /** Checks for equality with the object’s `lastEditedBy` field. */
  lastEditedBy?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `name` field. */
  name?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `permittedToDuplicate` field. */
  permittedToDuplicate?: InputMaybe<Scalars['Boolean']>;
  /** Checks for equality with the object’s `trashed` field. */
  trashed?: InputMaybe<Scalars['Boolean']>;
};

/** A filter to be used against `Deck` object types. All fields are combined with a logical ‘and.’ */
export type DeckFilter = {
  /** Checks for all expressions in this list. */
  and?: InputMaybe<Array<DeckFilter>>;
  /** Filter by the object’s `createdBy` field. */
  createdBy?: InputMaybe<StringFilter>;
  /** Filter by the object’s `deckType` field. */
  deckType?: InputMaybe<IntFilter>;
  /** Filter by the object’s `format` field. */
  format?: InputMaybe<StringFilter>;
  /** Filter by the object’s `heroClass` field. */
  heroClass?: InputMaybe<StringFilter>;
  /** Filter by the object’s `id` field. */
  id?: InputMaybe<StringFilter>;
  /** Filter by the object’s `isPremade` field. */
  isPremade?: InputMaybe<BooleanFilter>;
  /** Filter by the object’s `lastEditedBy` field. */
  lastEditedBy?: InputMaybe<StringFilter>;
  /** Filter by the object’s `name` field. */
  name?: InputMaybe<StringFilter>;
  /** Negates the expression. */
  not?: InputMaybe<DeckFilter>;
  /** Checks for any expressions in this list. */
  or?: InputMaybe<Array<DeckFilter>>;
  /** Filter by the object’s `permittedToDuplicate` field. */
  permittedToDuplicate?: InputMaybe<BooleanFilter>;
  /** Filter by the object’s `trashed` field. */
  trashed?: InputMaybe<BooleanFilter>;
};

/** An input for mutations affecting `Deck` */
export type DeckInput = {
  /** who created this deck originally */
  createdBy: Scalars['String'];
  deckType: Scalars['Int'];
  format?: InputMaybe<Scalars['String']>;
  heroClass?: InputMaybe<Scalars['String']>;
  id: Scalars['String'];
  /** premades always shared with all users by application logic */
  isPremade?: InputMaybe<Scalars['Boolean']>;
  /** who last edited this deck */
  lastEditedBy: Scalars['String'];
  name?: InputMaybe<Scalars['String']>;
  permittedToDuplicate?: InputMaybe<Scalars['Boolean']>;
  trashed?: InputMaybe<Scalars['Boolean']>;
};

/** Represents an update to a `Deck`. Fields that are set will be updated. */
export type DeckPatch = {
  /** who created this deck originally */
  createdBy?: InputMaybe<Scalars['String']>;
  deckType?: InputMaybe<Scalars['Int']>;
  format?: InputMaybe<Scalars['String']>;
  heroClass?: InputMaybe<Scalars['String']>;
  id?: InputMaybe<Scalars['String']>;
  /** premades always shared with all users by application logic */
  isPremade?: InputMaybe<Scalars['Boolean']>;
  /** who last edited this deck */
  lastEditedBy?: InputMaybe<Scalars['String']>;
  name?: InputMaybe<Scalars['String']>;
  permittedToDuplicate?: InputMaybe<Scalars['Boolean']>;
  trashed?: InputMaybe<Scalars['Boolean']>;
};

export type DeckPlayerAttributeTuple = Node & {
  __typename?: 'DeckPlayerAttributeTuple';
  attribute: Scalars['Int'];
  /** Reads a single `Deck` that is related to this `DeckPlayerAttributeTuple`. */
  deckByDeckId?: Maybe<Deck>;
  deckId: Scalars['String'];
  id: Scalars['BigInt'];
  /** A globally unique identifier. Can be used in various places throughout the system to identify this single value. */
  nodeId: Scalars['ID'];
  stringValue?: Maybe<Scalars['String']>;
};

/**
 * A condition to be used against `DeckPlayerAttributeTuple` object types. All
 * fields are tested for equality and combined with a logical ‘and.’
 */
export type DeckPlayerAttributeTupleCondition = {
  /** Checks for equality with the object’s `attribute` field. */
  attribute?: InputMaybe<Scalars['Int']>;
  /** Checks for equality with the object’s `deckId` field. */
  deckId?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `id` field. */
  id?: InputMaybe<Scalars['BigInt']>;
  /** Checks for equality with the object’s `stringValue` field. */
  stringValue?: InputMaybe<Scalars['String']>;
};

/** A filter to be used against `DeckPlayerAttributeTuple` object types. All fields are combined with a logical ‘and.’ */
export type DeckPlayerAttributeTupleFilter = {
  /** Checks for all expressions in this list. */
  and?: InputMaybe<Array<DeckPlayerAttributeTupleFilter>>;
  /** Filter by the object’s `attribute` field. */
  attribute?: InputMaybe<IntFilter>;
  /** Filter by the object’s `deckId` field. */
  deckId?: InputMaybe<StringFilter>;
  /** Filter by the object’s `id` field. */
  id?: InputMaybe<BigIntFilter>;
  /** Negates the expression. */
  not?: InputMaybe<DeckPlayerAttributeTupleFilter>;
  /** Checks for any expressions in this list. */
  or?: InputMaybe<Array<DeckPlayerAttributeTupleFilter>>;
  /** Filter by the object’s `stringValue` field. */
  stringValue?: InputMaybe<StringFilter>;
};

/** An input for mutations affecting `DeckPlayerAttributeTuple` */
export type DeckPlayerAttributeTupleInput = {
  attribute: Scalars['Int'];
  deckId: Scalars['String'];
  stringValue?: InputMaybe<Scalars['String']>;
};

/** Represents an update to a `DeckPlayerAttributeTuple`. Fields that are set will be updated. */
export type DeckPlayerAttributeTuplePatch = {
  attribute?: InputMaybe<Scalars['Int']>;
  deckId?: InputMaybe<Scalars['String']>;
  stringValue?: InputMaybe<Scalars['String']>;
};

/** A connection to a list of `DeckPlayerAttributeTuple` values. */
export type DeckPlayerAttributeTuplesConnection = {
  __typename?: 'DeckPlayerAttributeTuplesConnection';
  /** A list of edges which contains the `DeckPlayerAttributeTuple` and cursor to aid in pagination. */
  edges: Array<DeckPlayerAttributeTuplesEdge>;
  /** A list of `DeckPlayerAttributeTuple` objects. */
  nodes: Array<Maybe<DeckPlayerAttributeTuple>>;
  /** Information to aid in pagination. */
  pageInfo: PageInfo;
  /** The count of *all* `DeckPlayerAttributeTuple` you could get from the connection. */
  totalCount: Scalars['Int'];
};

/** A `DeckPlayerAttributeTuple` edge in the connection. */
export type DeckPlayerAttributeTuplesEdge = {
  __typename?: 'DeckPlayerAttributeTuplesEdge';
  /** A cursor for use in pagination. */
  cursor?: Maybe<Scalars['Cursor']>;
  /** The `DeckPlayerAttributeTuple` at the end of the edge. */
  node?: Maybe<DeckPlayerAttributeTuple>;
};

/** Methods to use when ordering `DeckPlayerAttributeTuple`. */
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
/** indicates a deck shared to a player */
export type DeckShare = Node & {
  __typename?: 'DeckShare';
  /** Reads a single `Deck` that is related to this `DeckShare`. */
  deckByDeckId?: Maybe<Deck>;
  deckId: Scalars['String'];
  /** A globally unique identifier. Can be used in various places throughout the system to identify this single value. */
  nodeId: Scalars['ID'];
  shareRecipientId: Scalars['String'];
  trashedByRecipient: Scalars['Boolean'];
};

/**
 * A condition to be used against `DeckShare` object types. All fields are tested
 * for equality and combined with a logical ‘and.’
 */
export type DeckShareCondition = {
  /** Checks for equality with the object’s `deckId` field. */
  deckId?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `shareRecipientId` field. */
  shareRecipientId?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `trashedByRecipient` field. */
  trashedByRecipient?: InputMaybe<Scalars['Boolean']>;
};

/** A filter to be used against `DeckShare` object types. All fields are combined with a logical ‘and.’ */
export type DeckShareFilter = {
  /** Checks for all expressions in this list. */
  and?: InputMaybe<Array<DeckShareFilter>>;
  /** Filter by the object’s `deckId` field. */
  deckId?: InputMaybe<StringFilter>;
  /** Negates the expression. */
  not?: InputMaybe<DeckShareFilter>;
  /** Checks for any expressions in this list. */
  or?: InputMaybe<Array<DeckShareFilter>>;
  /** Filter by the object’s `shareRecipientId` field. */
  shareRecipientId?: InputMaybe<StringFilter>;
  /** Filter by the object’s `trashedByRecipient` field. */
  trashedByRecipient?: InputMaybe<BooleanFilter>;
};

/** An input for mutations affecting `DeckShare` */
export type DeckShareInput = {
  deckId: Scalars['String'];
  shareRecipientId: Scalars['String'];
  trashedByRecipient?: InputMaybe<Scalars['Boolean']>;
};

/** Represents an update to a `DeckShare`. Fields that are set will be updated. */
export type DeckSharePatch = {
  deckId?: InputMaybe<Scalars['String']>;
  shareRecipientId?: InputMaybe<Scalars['String']>;
  trashedByRecipient?: InputMaybe<Scalars['Boolean']>;
};

/** A connection to a list of `DeckShare` values. */
export type DeckSharesConnection = {
  __typename?: 'DeckSharesConnection';
  /** A list of edges which contains the `DeckShare` and cursor to aid in pagination. */
  edges: Array<DeckSharesEdge>;
  /** A list of `DeckShare` objects. */
  nodes: Array<Maybe<DeckShare>>;
  /** Information to aid in pagination. */
  pageInfo: PageInfo;
  /** The count of *all* `DeckShare` you could get from the connection. */
  totalCount: Scalars['Int'];
};

/** A `DeckShare` edge in the connection. */
export type DeckSharesEdge = {
  __typename?: 'DeckSharesEdge';
  /** A cursor for use in pagination. */
  cursor?: Maybe<Scalars['Cursor']>;
  /** The `DeckShare` at the end of the edge. */
  node?: Maybe<DeckShare>;
};

/** Methods to use when ordering `DeckShare`. */
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
/** A connection to a list of `Deck` values. */
export type DecksConnection = {
  __typename?: 'DecksConnection';
  /** A list of edges which contains the `Deck` and cursor to aid in pagination. */
  edges: Array<DecksEdge>;
  /** A list of `Deck` objects. */
  nodes: Array<Maybe<Deck>>;
  /** Information to aid in pagination. */
  pageInfo: PageInfo;
  /** The count of *all* `Deck` you could get from the connection. */
  totalCount: Scalars['Int'];
};

/** A `Deck` edge in the connection. */
export type DecksEdge = {
  __typename?: 'DecksEdge';
  /** A cursor for use in pagination. */
  cursor?: Maybe<Scalars['Cursor']>;
  /** The `Deck` at the end of the edge. */
  node?: Maybe<Deck>;
};

/** Methods to use when ordering `Deck`. */
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
/** All input for the `deleteBannedDraftCardByCardId` mutation. */
export type DeleteBannedDraftCardByCardIdInput = {
  cardId: Scalars['String'];
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
};

/** All input for the `deleteBannedDraftCard` mutation. */
export type DeleteBannedDraftCardInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The globally unique `ID` which will identify a single `BannedDraftCard` to be deleted. */
  nodeId: Scalars['ID'];
};

/** The output of our delete `BannedDraftCard` mutation. */
export type DeleteBannedDraftCardPayload = {
  __typename?: 'DeleteBannedDraftCardPayload';
  /** The `BannedDraftCard` that was deleted by this mutation. */
  bannedDraftCard?: Maybe<BannedDraftCard>;
  /** An edge for our `BannedDraftCard`. May be used by Relay 1. */
  bannedDraftCardEdge?: Maybe<BannedDraftCardsEdge>;
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  deletedBannedDraftCardId?: Maybe<Scalars['ID']>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our delete `BannedDraftCard` mutation. */
export type DeleteBannedDraftCardPayloadBannedDraftCardEdgeArgs = {
  orderBy?: InputMaybe<Array<BannedDraftCardsOrderBy>>;
};

/** All input for the `deleteBotUserById` mutation. */
export type DeleteBotUserByIdInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  id: Scalars['String'];
};

/** All input for the `deleteBotUser` mutation. */
export type DeleteBotUserInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The globally unique `ID` which will identify a single `BotUser` to be deleted. */
  nodeId: Scalars['ID'];
};

/** The output of our delete `BotUser` mutation. */
export type DeleteBotUserPayload = {
  __typename?: 'DeleteBotUserPayload';
  /** The `BotUser` that was deleted by this mutation. */
  botUser?: Maybe<BotUser>;
  /** An edge for our `BotUser`. May be used by Relay 1. */
  botUserEdge?: Maybe<BotUsersEdge>;
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  deletedBotUserId?: Maybe<Scalars['ID']>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our delete `BotUser` mutation. */
export type DeleteBotUserPayloadBotUserEdgeArgs = {
  orderBy?: InputMaybe<Array<BotUsersOrderBy>>;
};

/** All input for the `deleteCardsInDeckById` mutation. */
export type DeleteCardsInDeckByIdInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  id: Scalars['BigInt'];
};

/** All input for the `deleteCardsInDeck` mutation. */
export type DeleteCardsInDeckInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The globally unique `ID` which will identify a single `CardsInDeck` to be deleted. */
  nodeId: Scalars['ID'];
};

/** The output of our delete `CardsInDeck` mutation. */
export type DeleteCardsInDeckPayload = {
  __typename?: 'DeleteCardsInDeckPayload';
  /** The `CardsInDeck` that was deleted by this mutation. */
  cardsInDeck?: Maybe<CardsInDeck>;
  /** An edge for our `CardsInDeck`. May be used by Relay 1. */
  cardsInDeckEdge?: Maybe<CardsInDecksEdge>;
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Reads a single `Deck` that is related to this `CardsInDeck`. */
  deckByDeckId?: Maybe<Deck>;
  deletedCardsInDeckId?: Maybe<Scalars['ID']>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our delete `CardsInDeck` mutation. */
export type DeleteCardsInDeckPayloadCardsInDeckEdgeArgs = {
  orderBy?: InputMaybe<Array<CardsInDecksOrderBy>>;
};

/** All input for the `deleteDeckById` mutation. */
export type DeleteDeckByIdInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  id: Scalars['String'];
};

/** All input for the `deleteDeck` mutation. */
export type DeleteDeckInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The globally unique `ID` which will identify a single `Deck` to be deleted. */
  nodeId: Scalars['ID'];
};

/** The output of our delete `Deck` mutation. */
export type DeleteDeckPayload = {
  __typename?: 'DeleteDeckPayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** The `Deck` that was deleted by this mutation. */
  deck?: Maybe<Deck>;
  /** An edge for our `Deck`. May be used by Relay 1. */
  deckEdge?: Maybe<DecksEdge>;
  deletedDeckId?: Maybe<Scalars['ID']>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our delete `Deck` mutation. */
export type DeleteDeckPayloadDeckEdgeArgs = {
  orderBy?: InputMaybe<Array<DecksOrderBy>>;
};

/** All input for the `deleteDeckPlayerAttributeTupleById` mutation. */
export type DeleteDeckPlayerAttributeTupleByIdInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  id: Scalars['BigInt'];
};

/** All input for the `deleteDeckPlayerAttributeTuple` mutation. */
export type DeleteDeckPlayerAttributeTupleInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The globally unique `ID` which will identify a single `DeckPlayerAttributeTuple` to be deleted. */
  nodeId: Scalars['ID'];
};

/** The output of our delete `DeckPlayerAttributeTuple` mutation. */
export type DeleteDeckPlayerAttributeTuplePayload = {
  __typename?: 'DeleteDeckPlayerAttributeTuplePayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Reads a single `Deck` that is related to this `DeckPlayerAttributeTuple`. */
  deckByDeckId?: Maybe<Deck>;
  /** The `DeckPlayerAttributeTuple` that was deleted by this mutation. */
  deckPlayerAttributeTuple?: Maybe<DeckPlayerAttributeTuple>;
  /** An edge for our `DeckPlayerAttributeTuple`. May be used by Relay 1. */
  deckPlayerAttributeTupleEdge?: Maybe<DeckPlayerAttributeTuplesEdge>;
  deletedDeckPlayerAttributeTupleId?: Maybe<Scalars['ID']>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our delete `DeckPlayerAttributeTuple` mutation. */
export type DeleteDeckPlayerAttributeTuplePayloadDeckPlayerAttributeTupleEdgeArgs = {
  orderBy?: InputMaybe<Array<DeckPlayerAttributeTuplesOrderBy>>;
};

/** All input for the `deleteDeckShareByDeckIdAndShareRecipientId` mutation. */
export type DeleteDeckShareByDeckIdAndShareRecipientIdInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  deckId: Scalars['String'];
  shareRecipientId: Scalars['String'];
};

/** All input for the `deleteDeckShare` mutation. */
export type DeleteDeckShareInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The globally unique `ID` which will identify a single `DeckShare` to be deleted. */
  nodeId: Scalars['ID'];
};

/** The output of our delete `DeckShare` mutation. */
export type DeleteDeckSharePayload = {
  __typename?: 'DeleteDeckSharePayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Reads a single `Deck` that is related to this `DeckShare`. */
  deckByDeckId?: Maybe<Deck>;
  /** The `DeckShare` that was deleted by this mutation. */
  deckShare?: Maybe<DeckShare>;
  /** An edge for our `DeckShare`. May be used by Relay 1. */
  deckShareEdge?: Maybe<DeckSharesEdge>;
  deletedDeckShareId?: Maybe<Scalars['ID']>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our delete `DeckShare` mutation. */
export type DeleteDeckSharePayloadDeckShareEdgeArgs = {
  orderBy?: InputMaybe<Array<DeckSharesOrderBy>>;
};

/** All input for the `deleteFriendByIdAndFriend` mutation. */
export type DeleteFriendByIdAndFriendInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  friend: Scalars['String'];
  id: Scalars['String'];
};

/** All input for the `deleteFriend` mutation. */
export type DeleteFriendInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The globally unique `ID` which will identify a single `Friend` to be deleted. */
  nodeId: Scalars['ID'];
};

/** The output of our delete `Friend` mutation. */
export type DeleteFriendPayload = {
  __typename?: 'DeleteFriendPayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  deletedFriendId?: Maybe<Scalars['ID']>;
  /** The `Friend` that was deleted by this mutation. */
  friend?: Maybe<Friend>;
  /** An edge for our `Friend`. May be used by Relay 1. */
  friendEdge?: Maybe<FriendsEdge>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our delete `Friend` mutation. */
export type DeleteFriendPayloadFriendEdgeArgs = {
  orderBy?: InputMaybe<Array<FriendsOrderBy>>;
};

/** All input for the `deleteGameById` mutation. */
export type DeleteGameByIdInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  id: Scalars['BigInt'];
};

/** All input for the `deleteGame` mutation. */
export type DeleteGameInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The globally unique `ID` which will identify a single `Game` to be deleted. */
  nodeId: Scalars['ID'];
};

/** The output of our delete `Game` mutation. */
export type DeleteGamePayload = {
  __typename?: 'DeleteGamePayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  deletedGameId?: Maybe<Scalars['ID']>;
  /** The `Game` that was deleted by this mutation. */
  game?: Maybe<Game>;
  /** An edge for our `Game`. May be used by Relay 1. */
  gameEdge?: Maybe<GamesEdge>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our delete `Game` mutation. */
export type DeleteGamePayloadGameEdgeArgs = {
  orderBy?: InputMaybe<Array<GamesOrderBy>>;
};

/** All input for the `deleteGameUserByGameIdAndUserId` mutation. */
export type DeleteGameUserByGameIdAndUserIdInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  gameId: Scalars['BigInt'];
  userId: Scalars['String'];
};

/** All input for the `deleteGameUser` mutation. */
export type DeleteGameUserInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The globally unique `ID` which will identify a single `GameUser` to be deleted. */
  nodeId: Scalars['ID'];
};

/** The output of our delete `GameUser` mutation. */
export type DeleteGameUserPayload = {
  __typename?: 'DeleteGameUserPayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Reads a single `Deck` that is related to this `GameUser`. */
  deckByDeckId?: Maybe<Deck>;
  deletedGameUserId?: Maybe<Scalars['ID']>;
  /** Reads a single `Game` that is related to this `GameUser`. */
  gameByGameId?: Maybe<Game>;
  /** The `GameUser` that was deleted by this mutation. */
  gameUser?: Maybe<GameUser>;
  /** An edge for our `GameUser`. May be used by Relay 1. */
  gameUserEdge?: Maybe<GameUsersEdge>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our delete `GameUser` mutation. */
export type DeleteGameUserPayloadGameUserEdgeArgs = {
  orderBy?: InputMaybe<Array<GameUsersOrderBy>>;
};

/** All input for the `deleteGuestById` mutation. */
export type DeleteGuestByIdInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  id: Scalars['BigInt'];
};

/** All input for the `deleteGuest` mutation. */
export type DeleteGuestInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The globally unique `ID` which will identify a single `Guest` to be deleted. */
  nodeId: Scalars['ID'];
};

/** The output of our delete `Guest` mutation. */
export type DeleteGuestPayload = {
  __typename?: 'DeleteGuestPayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  deletedGuestId?: Maybe<Scalars['ID']>;
  /** The `Guest` that was deleted by this mutation. */
  guest?: Maybe<Guest>;
  /** An edge for our `Guest`. May be used by Relay 1. */
  guestEdge?: Maybe<GuestsEdge>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our delete `Guest` mutation. */
export type DeleteGuestPayloadGuestEdgeArgs = {
  orderBy?: InputMaybe<Array<GuestsOrderBy>>;
};

/** All input for the `deleteHardRemovalCardByCardId` mutation. */
export type DeleteHardRemovalCardByCardIdInput = {
  cardId: Scalars['String'];
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
};

/** All input for the `deleteHardRemovalCard` mutation. */
export type DeleteHardRemovalCardInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The globally unique `ID` which will identify a single `HardRemovalCard` to be deleted. */
  nodeId: Scalars['ID'];
};

/** The output of our delete `HardRemovalCard` mutation. */
export type DeleteHardRemovalCardPayload = {
  __typename?: 'DeleteHardRemovalCardPayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  deletedHardRemovalCardId?: Maybe<Scalars['ID']>;
  /** The `HardRemovalCard` that was deleted by this mutation. */
  hardRemovalCard?: Maybe<HardRemovalCard>;
  /** An edge for our `HardRemovalCard`. May be used by Relay 1. */
  hardRemovalCardEdge?: Maybe<HardRemovalCardsEdge>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our delete `HardRemovalCard` mutation. */
export type DeleteHardRemovalCardPayloadHardRemovalCardEdgeArgs = {
  orderBy?: InputMaybe<Array<HardRemovalCardsOrderBy>>;
};

/** All input for the `deleteMatchmakingQueueById` mutation. */
export type DeleteMatchmakingQueueByIdInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  id: Scalars['String'];
};

/** All input for the `deleteMatchmakingQueue` mutation. */
export type DeleteMatchmakingQueueInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The globally unique `ID` which will identify a single `MatchmakingQueue` to be deleted. */
  nodeId: Scalars['ID'];
};

/** The output of our delete `MatchmakingQueue` mutation. */
export type DeleteMatchmakingQueuePayload = {
  __typename?: 'DeleteMatchmakingQueuePayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  deletedMatchmakingQueueId?: Maybe<Scalars['ID']>;
  /** The `MatchmakingQueue` that was deleted by this mutation. */
  matchmakingQueue?: Maybe<MatchmakingQueue>;
  /** An edge for our `MatchmakingQueue`. May be used by Relay 1. */
  matchmakingQueueEdge?: Maybe<MatchmakingQueuesEdge>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our delete `MatchmakingQueue` mutation. */
export type DeleteMatchmakingQueuePayloadMatchmakingQueueEdgeArgs = {
  orderBy?: InputMaybe<Array<MatchmakingQueuesOrderBy>>;
};

/** All input for the `deleteMatchmakingTicketByUserId` mutation. */
export type DeleteMatchmakingTicketByUserIdInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  userId: Scalars['String'];
};

/** All input for the `deleteMatchmakingTicket` mutation. */
export type DeleteMatchmakingTicketInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The globally unique `ID` which will identify a single `MatchmakingTicket` to be deleted. */
  nodeId: Scalars['ID'];
};

/** The output of our delete `MatchmakingTicket` mutation. */
export type DeleteMatchmakingTicketPayload = {
  __typename?: 'DeleteMatchmakingTicketPayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Reads a single `Deck` that is related to this `MatchmakingTicket`. */
  deckByBotDeckId?: Maybe<Deck>;
  /** Reads a single `Deck` that is related to this `MatchmakingTicket`. */
  deckByDeckId?: Maybe<Deck>;
  deletedMatchmakingTicketId?: Maybe<Scalars['ID']>;
  /** Reads a single `MatchmakingQueue` that is related to this `MatchmakingTicket`. */
  matchmakingQueueByQueueId?: Maybe<MatchmakingQueue>;
  /** The `MatchmakingTicket` that was deleted by this mutation. */
  matchmakingTicket?: Maybe<MatchmakingTicket>;
  /** An edge for our `MatchmakingTicket`. May be used by Relay 1. */
  matchmakingTicketEdge?: Maybe<MatchmakingTicketsEdge>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our delete `MatchmakingTicket` mutation. */
export type DeleteMatchmakingTicketPayloadMatchmakingTicketEdgeArgs = {
  orderBy?: InputMaybe<Array<MatchmakingTicketsOrderBy>>;
};

/** All input for the `deleteUserEntityAddonById` mutation. */
export type DeleteUserEntityAddonByIdInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  id: Scalars['String'];
};

/** All input for the `deleteUserEntityAddon` mutation. */
export type DeleteUserEntityAddonInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The globally unique `ID` which will identify a single `UserEntityAddon` to be deleted. */
  nodeId: Scalars['ID'];
};

/** The output of our delete `UserEntityAddon` mutation. */
export type DeleteUserEntityAddonPayload = {
  __typename?: 'DeleteUserEntityAddonPayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  deletedUserEntityAddonId?: Maybe<Scalars['ID']>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
  /** The `UserEntityAddon` that was deleted by this mutation. */
  userEntityAddon?: Maybe<UserEntityAddon>;
  /** An edge for our `UserEntityAddon`. May be used by Relay 1. */
  userEntityAddonEdge?: Maybe<UserEntityAddonsEdge>;
};


/** The output of our delete `UserEntityAddon` mutation. */
export type DeleteUserEntityAddonPayloadUserEntityAddonEdgeArgs = {
  orderBy?: InputMaybe<Array<UserEntityAddonsOrderBy>>;
};

export type Friend = Node & {
  __typename?: 'Friend';
  createdAt: Scalars['Datetime'];
  friend: Scalars['String'];
  id: Scalars['String'];
  /** A globally unique identifier. Can be used in various places throughout the system to identify this single value. */
  nodeId: Scalars['ID'];
};

/** A condition to be used against `Friend` object types. All fields are tested for equality and combined with a logical ‘and.’ */
export type FriendCondition = {
  /** Checks for equality with the object’s `createdAt` field. */
  createdAt?: InputMaybe<Scalars['Datetime']>;
  /** Checks for equality with the object’s `friend` field. */
  friend?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `id` field. */
  id?: InputMaybe<Scalars['String']>;
};

/** A filter to be used against `Friend` object types. All fields are combined with a logical ‘and.’ */
export type FriendFilter = {
  /** Checks for all expressions in this list. */
  and?: InputMaybe<Array<FriendFilter>>;
  /** Filter by the object’s `createdAt` field. */
  createdAt?: InputMaybe<DatetimeFilter>;
  /** Filter by the object’s `friend` field. */
  friend?: InputMaybe<StringFilter>;
  /** Filter by the object’s `id` field. */
  id?: InputMaybe<StringFilter>;
  /** Negates the expression. */
  not?: InputMaybe<FriendFilter>;
  /** Checks for any expressions in this list. */
  or?: InputMaybe<Array<FriendFilter>>;
};

/** An input for mutations affecting `Friend` */
export type FriendInput = {
  createdAt?: InputMaybe<Scalars['Datetime']>;
  friend: Scalars['String'];
  id: Scalars['String'];
};

/** Represents an update to a `Friend`. Fields that are set will be updated. */
export type FriendPatch = {
  createdAt?: InputMaybe<Scalars['Datetime']>;
  friend?: InputMaybe<Scalars['String']>;
  id?: InputMaybe<Scalars['String']>;
};

/** A connection to a list of `Friend` values. */
export type FriendsConnection = {
  __typename?: 'FriendsConnection';
  /** A list of edges which contains the `Friend` and cursor to aid in pagination. */
  edges: Array<FriendsEdge>;
  /** A list of `Friend` objects. */
  nodes: Array<Maybe<Friend>>;
  /** Information to aid in pagination. */
  pageInfo: PageInfo;
  /** The count of *all* `Friend` you could get from the connection. */
  totalCount: Scalars['Int'];
};

/** A `Friend` edge in the connection. */
export type FriendsEdge = {
  __typename?: 'FriendsEdge';
  /** A cursor for use in pagination. */
  cursor?: Maybe<Scalars['Cursor']>;
  /** The `Friend` at the end of the edge. */
  node?: Maybe<Friend>;
};

/** Methods to use when ordering `Friend`. */
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
  /** Reads and enables pagination through a set of `GameUser`. */
  gameUsersByGameId: GameUsersConnection;
  gitHash?: Maybe<Scalars['String']>;
  id: Scalars['BigInt'];
  /** A globally unique identifier. Can be used in various places throughout the system to identify this single value. */
  nodeId: Scalars['ID'];
  status: GameStateEnum;
  trace?: Maybe<Scalars['JSON']>;
};


export type GameGameUsersByGameIdArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<GameUserCondition>;
  filter?: InputMaybe<GameUserFilter>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<GameUsersOrderBy>>;
};

/** A condition to be used against `Game` object types. All fields are tested for equality and combined with a logical ‘and.’ */
export type GameCondition = {
  /** Checks for equality with the object’s `createdAt` field. */
  createdAt?: InputMaybe<Scalars['Datetime']>;
  /** Checks for equality with the object’s `gitHash` field. */
  gitHash?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `id` field. */
  id?: InputMaybe<Scalars['BigInt']>;
  /** Checks for equality with the object’s `status` field. */
  status?: InputMaybe<GameStateEnum>;
  /** Checks for equality with the object’s `trace` field. */
  trace?: InputMaybe<Scalars['JSON']>;
};

/** A filter to be used against `Game` object types. All fields are combined with a logical ‘and.’ */
export type GameFilter = {
  /** Checks for all expressions in this list. */
  and?: InputMaybe<Array<GameFilter>>;
  /** Filter by the object’s `createdAt` field. */
  createdAt?: InputMaybe<DatetimeFilter>;
  /** Filter by the object’s `gitHash` field. */
  gitHash?: InputMaybe<StringFilter>;
  /** Filter by the object’s `id` field. */
  id?: InputMaybe<BigIntFilter>;
  /** Negates the expression. */
  not?: InputMaybe<GameFilter>;
  /** Checks for any expressions in this list. */
  or?: InputMaybe<Array<GameFilter>>;
  /** Filter by the object’s `status` field. */
  status?: InputMaybe<GameStateEnumFilter>;
  /** Filter by the object’s `trace` field. */
  trace?: InputMaybe<JsonFilter>;
};

/** An input for mutations affecting `Game` */
export type GameInput = {
  createdAt?: InputMaybe<Scalars['Datetime']>;
  gitHash?: InputMaybe<Scalars['String']>;
  status?: InputMaybe<GameStateEnum>;
  trace?: InputMaybe<Scalars['JSON']>;
};

/** Represents an update to a `Game`. Fields that are set will be updated. */
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
/** A filter to be used against GameStateEnum fields. All fields are combined with a logical ‘and.’ */
export type GameStateEnumFilter = {
  /** Not equal to the specified value, treating null like an ordinary value. */
  distinctFrom?: InputMaybe<GameStateEnum>;
  /** Equal to the specified value. */
  equalTo?: InputMaybe<GameStateEnum>;
  /** Greater than the specified value. */
  greaterThan?: InputMaybe<GameStateEnum>;
  /** Greater than or equal to the specified value. */
  greaterThanOrEqualTo?: InputMaybe<GameStateEnum>;
  /** Included in the specified list. */
  in?: InputMaybe<Array<GameStateEnum>>;
  /** Is null (if `true` is specified) or is not null (if `false` is specified). */
  isNull?: InputMaybe<Scalars['Boolean']>;
  /** Less than the specified value. */
  lessThan?: InputMaybe<GameStateEnum>;
  /** Less than or equal to the specified value. */
  lessThanOrEqualTo?: InputMaybe<GameStateEnum>;
  /** Equal to the specified value, treating null like an ordinary value. */
  notDistinctFrom?: InputMaybe<GameStateEnum>;
  /** Not equal to the specified value. */
  notEqualTo?: InputMaybe<GameStateEnum>;
  /** Not included in the specified list. */
  notIn?: InputMaybe<Array<GameStateEnum>>;
};

export type GameUser = Node & {
  __typename?: 'GameUser';
  /** Reads a single `Deck` that is related to this `GameUser`. */
  deckByDeckId?: Maybe<Deck>;
  deckId?: Maybe<Scalars['String']>;
  /** Reads a single `Game` that is related to this `GameUser`. */
  gameByGameId?: Maybe<Game>;
  gameId: Scalars['BigInt'];
  /** A globally unique identifier. Can be used in various places throughout the system to identify this single value. */
  nodeId: Scalars['ID'];
  playerIndex?: Maybe<Scalars['Int']>;
  userId: Scalars['String'];
  victoryStatus: GameUserVictoryEnum;
};

/**
 * A condition to be used against `GameUser` object types. All fields are tested
 * for equality and combined with a logical ‘and.’
 */
export type GameUserCondition = {
  /** Checks for equality with the object’s `deckId` field. */
  deckId?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `gameId` field. */
  gameId?: InputMaybe<Scalars['BigInt']>;
  /** Checks for equality with the object’s `playerIndex` field. */
  playerIndex?: InputMaybe<Scalars['Int']>;
  /** Checks for equality with the object’s `userId` field. */
  userId?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `victoryStatus` field. */
  victoryStatus?: InputMaybe<GameUserVictoryEnum>;
};

/** A filter to be used against `GameUser` object types. All fields are combined with a logical ‘and.’ */
export type GameUserFilter = {
  /** Checks for all expressions in this list. */
  and?: InputMaybe<Array<GameUserFilter>>;
  /** Filter by the object’s `deckId` field. */
  deckId?: InputMaybe<StringFilter>;
  /** Filter by the object’s `gameId` field. */
  gameId?: InputMaybe<BigIntFilter>;
  /** Negates the expression. */
  not?: InputMaybe<GameUserFilter>;
  /** Checks for any expressions in this list. */
  or?: InputMaybe<Array<GameUserFilter>>;
  /** Filter by the object’s `playerIndex` field. */
  playerIndex?: InputMaybe<IntFilter>;
  /** Filter by the object’s `userId` field. */
  userId?: InputMaybe<StringFilter>;
  /** Filter by the object’s `victoryStatus` field. */
  victoryStatus?: InputMaybe<GameUserVictoryEnumFilter>;
};

/** An input for mutations affecting `GameUser` */
export type GameUserInput = {
  deckId?: InputMaybe<Scalars['String']>;
  gameId: Scalars['BigInt'];
  playerIndex?: InputMaybe<Scalars['Int']>;
  userId: Scalars['String'];
  victoryStatus?: InputMaybe<GameUserVictoryEnum>;
};

/** Represents an update to a `GameUser`. Fields that are set will be updated. */
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
/** A filter to be used against GameUserVictoryEnum fields. All fields are combined with a logical ‘and.’ */
export type GameUserVictoryEnumFilter = {
  /** Not equal to the specified value, treating null like an ordinary value. */
  distinctFrom?: InputMaybe<GameUserVictoryEnum>;
  /** Equal to the specified value. */
  equalTo?: InputMaybe<GameUserVictoryEnum>;
  /** Greater than the specified value. */
  greaterThan?: InputMaybe<GameUserVictoryEnum>;
  /** Greater than or equal to the specified value. */
  greaterThanOrEqualTo?: InputMaybe<GameUserVictoryEnum>;
  /** Included in the specified list. */
  in?: InputMaybe<Array<GameUserVictoryEnum>>;
  /** Is null (if `true` is specified) or is not null (if `false` is specified). */
  isNull?: InputMaybe<Scalars['Boolean']>;
  /** Less than the specified value. */
  lessThan?: InputMaybe<GameUserVictoryEnum>;
  /** Less than or equal to the specified value. */
  lessThanOrEqualTo?: InputMaybe<GameUserVictoryEnum>;
  /** Equal to the specified value, treating null like an ordinary value. */
  notDistinctFrom?: InputMaybe<GameUserVictoryEnum>;
  /** Not equal to the specified value. */
  notEqualTo?: InputMaybe<GameUserVictoryEnum>;
  /** Not included in the specified list. */
  notIn?: InputMaybe<Array<GameUserVictoryEnum>>;
};

/** A connection to a list of `GameUser` values. */
export type GameUsersConnection = {
  __typename?: 'GameUsersConnection';
  /** A list of edges which contains the `GameUser` and cursor to aid in pagination. */
  edges: Array<GameUsersEdge>;
  /** A list of `GameUser` objects. */
  nodes: Array<Maybe<GameUser>>;
  /** Information to aid in pagination. */
  pageInfo: PageInfo;
  /** The count of *all* `GameUser` you could get from the connection. */
  totalCount: Scalars['Int'];
};

/** A `GameUser` edge in the connection. */
export type GameUsersEdge = {
  __typename?: 'GameUsersEdge';
  /** A cursor for use in pagination. */
  cursor?: Maybe<Scalars['Cursor']>;
  /** The `GameUser` at the end of the edge. */
  node?: Maybe<GameUser>;
};

/** Methods to use when ordering `GameUser`. */
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
/** A connection to a list of `Game` values. */
export type GamesConnection = {
  __typename?: 'GamesConnection';
  /** A list of edges which contains the `Game` and cursor to aid in pagination. */
  edges: Array<GamesEdge>;
  /** A list of `Game` objects. */
  nodes: Array<Maybe<Game>>;
  /** Information to aid in pagination. */
  pageInfo: PageInfo;
  /** The count of *all* `Game` you could get from the connection. */
  totalCount: Scalars['Int'];
};

/** A `Game` edge in the connection. */
export type GamesEdge = {
  __typename?: 'GamesEdge';
  /** A cursor for use in pagination. */
  cursor?: Maybe<Scalars['Cursor']>;
  /** The `Game` at the end of the edge. */
  node?: Maybe<Game>;
};

/** Methods to use when ordering `Game`. */
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
/** All input for the `getClasses` mutation. */
export type GetClassesInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
};

/** The output of our `getClasses` mutation. */
export type GetClassesPayload = {
  __typename?: 'GetClassesPayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
  results?: Maybe<Array<Maybe<GetClassesRecord>>>;
};

/** The return type of our `getClasses` mutation. */
export type GetClassesRecord = {
  __typename?: 'GetClassesRecord';
  cardScript?: Maybe<Scalars['JSON']>;
  class?: Maybe<Scalars['String']>;
  collectible?: Maybe<Scalars['Boolean']>;
  createdBy?: Maybe<Scalars['String']>;
  id?: Maybe<Scalars['String']>;
  isPublished?: Maybe<Scalars['Boolean']>;
  name?: Maybe<Scalars['String']>;
};

/** All input for the `getCollectionCards` mutation. */
export type GetCollectionCardsInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
};

/** The output of our `getCollectionCards` mutation. */
export type GetCollectionCardsPayload = {
  __typename?: 'GetCollectionCardsPayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
  results?: Maybe<Array<Maybe<GetCollectionCardsRecord>>>;
};

/** The return type of our `getCollectionCards` mutation. */
export type GetCollectionCardsRecord = {
  __typename?: 'GetCollectionCardsRecord';
  blocklyWorkspace?: Maybe<Scalars['String']>;
  cardScript?: Maybe<Scalars['JSON']>;
  class?: Maybe<Scalars['String']>;
  collectible?: Maybe<Scalars['Boolean']>;
  cost?: Maybe<Scalars['Int']>;
  createdAt?: Maybe<Scalars['Datetime']>;
  createdBy?: Maybe<Scalars['String']>;
  id?: Maybe<Scalars['String']>;
  lastModified?: Maybe<Scalars['Datetime']>;
  name?: Maybe<Scalars['String']>;
  searchMessage?: Maybe<Scalars['String']>;
  type?: Maybe<Scalars['String']>;
};

export type Guest = Node & {
  __typename?: 'Guest';
  id: Scalars['BigInt'];
  /** A globally unique identifier. Can be used in various places throughout the system to identify this single value. */
  nodeId: Scalars['ID'];
  userId?: Maybe<Scalars['String']>;
};

/** A condition to be used against `Guest` object types. All fields are tested for equality and combined with a logical ‘and.’ */
export type GuestCondition = {
  /** Checks for equality with the object’s `id` field. */
  id?: InputMaybe<Scalars['BigInt']>;
  /** Checks for equality with the object’s `userId` field. */
  userId?: InputMaybe<Scalars['String']>;
};

/** A filter to be used against `Guest` object types. All fields are combined with a logical ‘and.’ */
export type GuestFilter = {
  /** Checks for all expressions in this list. */
  and?: InputMaybe<Array<GuestFilter>>;
  /** Filter by the object’s `id` field. */
  id?: InputMaybe<BigIntFilter>;
  /** Negates the expression. */
  not?: InputMaybe<GuestFilter>;
  /** Checks for any expressions in this list. */
  or?: InputMaybe<Array<GuestFilter>>;
  /** Filter by the object’s `userId` field. */
  userId?: InputMaybe<StringFilter>;
};

/** An input for mutations affecting `Guest` */
export type GuestInput = {
  userId?: InputMaybe<Scalars['String']>;
};

/** Represents an update to a `Guest`. Fields that are set will be updated. */
export type GuestPatch = {
  userId?: InputMaybe<Scalars['String']>;
};

/** A connection to a list of `Guest` values. */
export type GuestsConnection = {
  __typename?: 'GuestsConnection';
  /** A list of edges which contains the `Guest` and cursor to aid in pagination. */
  edges: Array<GuestsEdge>;
  /** A list of `Guest` objects. */
  nodes: Array<Maybe<Guest>>;
  /** Information to aid in pagination. */
  pageInfo: PageInfo;
  /** The count of *all* `Guest` you could get from the connection. */
  totalCount: Scalars['Int'];
};

/** A `Guest` edge in the connection. */
export type GuestsEdge = {
  __typename?: 'GuestsEdge';
  /** A cursor for use in pagination. */
  cursor?: Maybe<Scalars['Cursor']>;
  /** The `Guest` at the end of the edge. */
  node?: Maybe<Guest>;
};

/** Methods to use when ordering `Guest`. */
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
export type HardRemovalCard = Node & {
  __typename?: 'HardRemovalCard';
  cardId: Scalars['String'];
  /** A globally unique identifier. Can be used in various places throughout the system to identify this single value. */
  nodeId: Scalars['ID'];
};

/**
 * A condition to be used against `HardRemovalCard` object types. All fields are
 * tested for equality and combined with a logical ‘and.’
 */
export type HardRemovalCardCondition = {
  /** Checks for equality with the object’s `cardId` field. */
  cardId?: InputMaybe<Scalars['String']>;
};

/** A filter to be used against `HardRemovalCard` object types. All fields are combined with a logical ‘and.’ */
export type HardRemovalCardFilter = {
  /** Checks for all expressions in this list. */
  and?: InputMaybe<Array<HardRemovalCardFilter>>;
  /** Filter by the object’s `cardId` field. */
  cardId?: InputMaybe<StringFilter>;
  /** Negates the expression. */
  not?: InputMaybe<HardRemovalCardFilter>;
  /** Checks for any expressions in this list. */
  or?: InputMaybe<Array<HardRemovalCardFilter>>;
};

/** An input for mutations affecting `HardRemovalCard` */
export type HardRemovalCardInput = {
  cardId: Scalars['String'];
};

/** Represents an update to a `HardRemovalCard`. Fields that are set will be updated. */
export type HardRemovalCardPatch = {
  cardId?: InputMaybe<Scalars['String']>;
};

/** A connection to a list of `HardRemovalCard` values. */
export type HardRemovalCardsConnection = {
  __typename?: 'HardRemovalCardsConnection';
  /** A list of edges which contains the `HardRemovalCard` and cursor to aid in pagination. */
  edges: Array<HardRemovalCardsEdge>;
  /** A list of `HardRemovalCard` objects. */
  nodes: Array<Maybe<HardRemovalCard>>;
  /** Information to aid in pagination. */
  pageInfo: PageInfo;
  /** The count of *all* `HardRemovalCard` you could get from the connection. */
  totalCount: Scalars['Int'];
};

/** A `HardRemovalCard` edge in the connection. */
export type HardRemovalCardsEdge = {
  __typename?: 'HardRemovalCardsEdge';
  /** A cursor for use in pagination. */
  cursor?: Maybe<Scalars['Cursor']>;
  /** The `HardRemovalCard` at the end of the edge. */
  node?: Maybe<HardRemovalCard>;
};

/** Methods to use when ordering `HardRemovalCard`. */
export const HardRemovalCardsOrderBy = {
  CardIdAsc: 'CARD_ID_ASC',
  CardIdDesc: 'CARD_ID_DESC',
  Natural: 'NATURAL',
  PrimaryKeyAsc: 'PRIMARY_KEY_ASC',
  PrimaryKeyDesc: 'PRIMARY_KEY_DESC'
} as const;

export type HardRemovalCardsOrderBy = typeof HardRemovalCardsOrderBy[keyof typeof HardRemovalCardsOrderBy];
export type ImageDef = {
  __typename?: 'ImageDef';
  height: Scalars['Int'];
  id: Scalars['String'];
  name: Scalars['String'];
  src: Scalars['String'];
  width: Scalars['Int'];
};

/** Indicates whether archived items should be included in the results or not. */
export const IncludeArchivedOption = {
  /** Only include archived items (i.e. exclude non-archived items). */
  Exclusively: 'EXCLUSIVELY',
  /** If there is a parent GraphQL record and it is archived then this is equivalent to YES, in all other cases this is equivalent to NO. */
  Inherit: 'INHERIT',
  /** Exclude archived items. */
  No: 'NO',
  /** Include archived items. */
  Yes: 'YES'
} as const;

export type IncludeArchivedOption = typeof IncludeArchivedOption[keyof typeof IncludeArchivedOption];
/** A filter to be used against Int fields. All fields are combined with a logical ‘and.’ */
export type IntFilter = {
  /** Not equal to the specified value, treating null like an ordinary value. */
  distinctFrom?: InputMaybe<Scalars['Int']>;
  /** Equal to the specified value. */
  equalTo?: InputMaybe<Scalars['Int']>;
  /** Greater than the specified value. */
  greaterThan?: InputMaybe<Scalars['Int']>;
  /** Greater than or equal to the specified value. */
  greaterThanOrEqualTo?: InputMaybe<Scalars['Int']>;
  /** Included in the specified list. */
  in?: InputMaybe<Array<Scalars['Int']>>;
  /** Is null (if `true` is specified) or is not null (if `false` is specified). */
  isNull?: InputMaybe<Scalars['Boolean']>;
  /** Less than the specified value. */
  lessThan?: InputMaybe<Scalars['Int']>;
  /** Less than or equal to the specified value. */
  lessThanOrEqualTo?: InputMaybe<Scalars['Int']>;
  /** Equal to the specified value, treating null like an ordinary value. */
  notDistinctFrom?: InputMaybe<Scalars['Int']>;
  /** Not equal to the specified value. */
  notEqualTo?: InputMaybe<Scalars['Int']>;
  /** Not included in the specified list. */
  notIn?: InputMaybe<Array<Scalars['Int']>>;
};

/** A filter to be used against JSON fields. All fields are combined with a logical ‘and.’ */
export type JsonFilter = {
  /** Contained by the specified JSON. */
  containedBy?: InputMaybe<Scalars['JSON']>;
  /** Contains the specified JSON. */
  contains?: InputMaybe<Scalars['JSON']>;
  /** Contains all of the specified keys. */
  containsAllKeys?: InputMaybe<Array<Scalars['String']>>;
  /** Contains any of the specified keys. */
  containsAnyKeys?: InputMaybe<Array<Scalars['String']>>;
  /** Contains the specified key. */
  containsKey?: InputMaybe<Scalars['String']>;
  /** Not equal to the specified value, treating null like an ordinary value. */
  distinctFrom?: InputMaybe<Scalars['JSON']>;
  /** Equal to the specified value. */
  equalTo?: InputMaybe<Scalars['JSON']>;
  /** Greater than the specified value. */
  greaterThan?: InputMaybe<Scalars['JSON']>;
  /** Greater than or equal to the specified value. */
  greaterThanOrEqualTo?: InputMaybe<Scalars['JSON']>;
  /** Included in the specified list. */
  in?: InputMaybe<Array<Scalars['JSON']>>;
  /** Is null (if `true` is specified) or is not null (if `false` is specified). */
  isNull?: InputMaybe<Scalars['Boolean']>;
  /** Less than the specified value. */
  lessThan?: InputMaybe<Scalars['JSON']>;
  /** Less than or equal to the specified value. */
  lessThanOrEqualTo?: InputMaybe<Scalars['JSON']>;
  /** Equal to the specified value, treating null like an ordinary value. */
  notDistinctFrom?: InputMaybe<Scalars['JSON']>;
  /** Not equal to the specified value. */
  notEqualTo?: InputMaybe<Scalars['JSON']>;
  /** Not included in the specified list. */
  notIn?: InputMaybe<Array<Scalars['JSON']>>;
};

export type MatchmakingQueue = Node & {
  __typename?: 'MatchmakingQueue';
  automaticallyClose: Scalars['Boolean'];
  awaitingLobbyTimeout: Scalars['BigInt'];
  botOpponent: Scalars['Boolean'];
  emptyLobbyTimeout: Scalars['BigInt'];
  id: Scalars['String'];
  lobbySize: Scalars['Int'];
  /** Reads and enables pagination through a set of `MatchmakingTicket`. */
  matchmakingTicketsByQueueId: MatchmakingTicketsConnection;
  name: Scalars['String'];
  /** A globally unique identifier. Can be used in various places throughout the system to identify this single value. */
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
  filter?: InputMaybe<MatchmakingTicketFilter>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<MatchmakingTicketsOrderBy>>;
};

/**
 * A condition to be used against `MatchmakingQueue` object types. All fields are
 * tested for equality and combined with a logical ‘and.’
 */
export type MatchmakingQueueCondition = {
  /** Checks for equality with the object’s `automaticallyClose` field. */
  automaticallyClose?: InputMaybe<Scalars['Boolean']>;
  /** Checks for equality with the object’s `awaitingLobbyTimeout` field. */
  awaitingLobbyTimeout?: InputMaybe<Scalars['BigInt']>;
  /** Checks for equality with the object’s `botOpponent` field. */
  botOpponent?: InputMaybe<Scalars['Boolean']>;
  /** Checks for equality with the object’s `emptyLobbyTimeout` field. */
  emptyLobbyTimeout?: InputMaybe<Scalars['BigInt']>;
  /** Checks for equality with the object’s `id` field. */
  id?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `lobbySize` field. */
  lobbySize?: InputMaybe<Scalars['Int']>;
  /** Checks for equality with the object’s `name` field. */
  name?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `once` field. */
  once?: InputMaybe<Scalars['Boolean']>;
  /** Checks for equality with the object’s `privateLobby` field. */
  privateLobby?: InputMaybe<Scalars['Boolean']>;
  /** Checks for equality with the object’s `queueCreatedAt` field. */
  queueCreatedAt?: InputMaybe<Scalars['Datetime']>;
  /** Checks for equality with the object’s `startsAutomatically` field. */
  startsAutomatically?: InputMaybe<Scalars['Boolean']>;
  /** Checks for equality with the object’s `stillConnectedTimeout` field. */
  stillConnectedTimeout?: InputMaybe<Scalars['BigInt']>;
};

/** A filter to be used against `MatchmakingQueue` object types. All fields are combined with a logical ‘and.’ */
export type MatchmakingQueueFilter = {
  /** Checks for all expressions in this list. */
  and?: InputMaybe<Array<MatchmakingQueueFilter>>;
  /** Filter by the object’s `automaticallyClose` field. */
  automaticallyClose?: InputMaybe<BooleanFilter>;
  /** Filter by the object’s `awaitingLobbyTimeout` field. */
  awaitingLobbyTimeout?: InputMaybe<BigIntFilter>;
  /** Filter by the object’s `botOpponent` field. */
  botOpponent?: InputMaybe<BooleanFilter>;
  /** Filter by the object’s `emptyLobbyTimeout` field. */
  emptyLobbyTimeout?: InputMaybe<BigIntFilter>;
  /** Filter by the object’s `id` field. */
  id?: InputMaybe<StringFilter>;
  /** Filter by the object’s `lobbySize` field. */
  lobbySize?: InputMaybe<IntFilter>;
  /** Filter by the object’s `name` field. */
  name?: InputMaybe<StringFilter>;
  /** Negates the expression. */
  not?: InputMaybe<MatchmakingQueueFilter>;
  /** Filter by the object’s `once` field. */
  once?: InputMaybe<BooleanFilter>;
  /** Checks for any expressions in this list. */
  or?: InputMaybe<Array<MatchmakingQueueFilter>>;
  /** Filter by the object’s `privateLobby` field. */
  privateLobby?: InputMaybe<BooleanFilter>;
  /** Filter by the object’s `queueCreatedAt` field. */
  queueCreatedAt?: InputMaybe<DatetimeFilter>;
  /** Filter by the object’s `startsAutomatically` field. */
  startsAutomatically?: InputMaybe<BooleanFilter>;
  /** Filter by the object’s `stillConnectedTimeout` field. */
  stillConnectedTimeout?: InputMaybe<BigIntFilter>;
};

/** An input for mutations affecting `MatchmakingQueue` */
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

/** Represents an update to a `MatchmakingQueue`. Fields that are set will be updated. */
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

/** A connection to a list of `MatchmakingQueue` values. */
export type MatchmakingQueuesConnection = {
  __typename?: 'MatchmakingQueuesConnection';
  /** A list of edges which contains the `MatchmakingQueue` and cursor to aid in pagination. */
  edges: Array<MatchmakingQueuesEdge>;
  /** A list of `MatchmakingQueue` objects. */
  nodes: Array<Maybe<MatchmakingQueue>>;
  /** Information to aid in pagination. */
  pageInfo: PageInfo;
  /** The count of *all* `MatchmakingQueue` you could get from the connection. */
  totalCount: Scalars['Int'];
};

/** A `MatchmakingQueue` edge in the connection. */
export type MatchmakingQueuesEdge = {
  __typename?: 'MatchmakingQueuesEdge';
  /** A cursor for use in pagination. */
  cursor?: Maybe<Scalars['Cursor']>;
  /** The `MatchmakingQueue` at the end of the edge. */
  node?: Maybe<MatchmakingQueue>;
};

/** Methods to use when ordering `MatchmakingQueue`. */
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
  /** Reads a single `Deck` that is related to this `MatchmakingTicket`. */
  deckByBotDeckId?: Maybe<Deck>;
  /** Reads a single `Deck` that is related to this `MatchmakingTicket`. */
  deckByDeckId?: Maybe<Deck>;
  deckId?: Maybe<Scalars['String']>;
  /** Reads a single `MatchmakingQueue` that is related to this `MatchmakingTicket`. */
  matchmakingQueueByQueueId?: Maybe<MatchmakingQueue>;
  /** A globally unique identifier. Can be used in various places throughout the system to identify this single value. */
  nodeId: Scalars['ID'];
  queueId?: Maybe<Scalars['String']>;
  ticketId: Scalars['BigInt'];
  userId: Scalars['String'];
};

/**
 * A condition to be used against `MatchmakingTicket` object types. All fields are
 * tested for equality and combined with a logical ‘and.’
 */
export type MatchmakingTicketCondition = {
  /** Checks for equality with the object’s `botDeckId` field. */
  botDeckId?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `createdAt` field. */
  createdAt?: InputMaybe<Scalars['Datetime']>;
  /** Checks for equality with the object’s `deckId` field. */
  deckId?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `queueId` field. */
  queueId?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `ticketId` field. */
  ticketId?: InputMaybe<Scalars['BigInt']>;
  /** Checks for equality with the object’s `userId` field. */
  userId?: InputMaybe<Scalars['String']>;
};

/** A filter to be used against `MatchmakingTicket` object types. All fields are combined with a logical ‘and.’ */
export type MatchmakingTicketFilter = {
  /** Checks for all expressions in this list. */
  and?: InputMaybe<Array<MatchmakingTicketFilter>>;
  /** Filter by the object’s `botDeckId` field. */
  botDeckId?: InputMaybe<StringFilter>;
  /** Filter by the object’s `createdAt` field. */
  createdAt?: InputMaybe<DatetimeFilter>;
  /** Filter by the object’s `deckId` field. */
  deckId?: InputMaybe<StringFilter>;
  /** Negates the expression. */
  not?: InputMaybe<MatchmakingTicketFilter>;
  /** Checks for any expressions in this list. */
  or?: InputMaybe<Array<MatchmakingTicketFilter>>;
  /** Filter by the object’s `queueId` field. */
  queueId?: InputMaybe<StringFilter>;
  /** Filter by the object’s `ticketId` field. */
  ticketId?: InputMaybe<BigIntFilter>;
  /** Filter by the object’s `userId` field. */
  userId?: InputMaybe<StringFilter>;
};

/** An input for mutations affecting `MatchmakingTicket` */
export type MatchmakingTicketInput = {
  botDeckId?: InputMaybe<Scalars['String']>;
  createdAt?: InputMaybe<Scalars['Datetime']>;
  deckId?: InputMaybe<Scalars['String']>;
  queueId?: InputMaybe<Scalars['String']>;
  userId: Scalars['String'];
};

/** Represents an update to a `MatchmakingTicket`. Fields that are set will be updated. */
export type MatchmakingTicketPatch = {
  botDeckId?: InputMaybe<Scalars['String']>;
  createdAt?: InputMaybe<Scalars['Datetime']>;
  deckId?: InputMaybe<Scalars['String']>;
  queueId?: InputMaybe<Scalars['String']>;
  userId?: InputMaybe<Scalars['String']>;
};

/** A connection to a list of `MatchmakingTicket` values. */
export type MatchmakingTicketsConnection = {
  __typename?: 'MatchmakingTicketsConnection';
  /** A list of edges which contains the `MatchmakingTicket` and cursor to aid in pagination. */
  edges: Array<MatchmakingTicketsEdge>;
  /** A list of `MatchmakingTicket` objects. */
  nodes: Array<Maybe<MatchmakingTicket>>;
  /** Information to aid in pagination. */
  pageInfo: PageInfo;
  /** The count of *all* `MatchmakingTicket` you could get from the connection. */
  totalCount: Scalars['Int'];
};

/** A `MatchmakingTicket` edge in the connection. */
export type MatchmakingTicketsEdge = {
  __typename?: 'MatchmakingTicketsEdge';
  /** A cursor for use in pagination. */
  cursor?: Maybe<Scalars['Cursor']>;
  /** The `MatchmakingTicket` at the end of the edge. */
  node?: Maybe<MatchmakingTicket>;
};

/** Methods to use when ordering `MatchmakingTicket`. */
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
/** The root mutation type which contains root level fields which mutate data. */
export type Mutation = {
  __typename?: 'Mutation';
  archiveCard?: Maybe<ArchiveCardPayload>;
  cardCatalogueFormats?: Maybe<CardCatalogueFormatsPayload>;
  cardCatalogueGetBannedDraftCards?: Maybe<CardCatalogueGetBannedDraftCardsPayload>;
  cardCatalogueGetBaseClasses?: Maybe<CardCatalogueGetBaseClassesPayload>;
  cardCatalogueGetCardById?: Maybe<CardCatalogueGetCardByIdPayload>;
  cardCatalogueGetCardByName?: Maybe<CardCatalogueGetCardByNamePayload>;
  cardCatalogueGetCardByNameAndClass?: Maybe<CardCatalogueGetCardByNameAndClassPayload>;
  cardCatalogueGetClassCards?: Maybe<CardCatalogueGetClassCardsPayload>;
  cardCatalogueGetFormat?: Maybe<CardCatalogueGetFormatPayload>;
  cardCatalogueGetHardRemovalCards?: Maybe<CardCatalogueGetHardRemovalCardsPayload>;
  cardCatalogueGetHeroCard?: Maybe<CardCatalogueGetHeroCardPayload>;
  cardCatalogueQuery?: Maybe<CardCatalogueQueryPayload>;
  clusteredGamesUpdateGameAndUsers?: Maybe<ClusteredGamesUpdateGameAndUsersPayload>;
  /** Creates a single `BannedDraftCard`. */
  createBannedDraftCard?: Maybe<CreateBannedDraftCardPayload>;
  /** Creates a single `BotUser`. */
  createBotUser?: Maybe<CreateBotUserPayload>;
  /** Creates a single `Card`. */
  createCard?: Maybe<CreateCardPayload>;
  /** Creates a single `CardsInDeck`. */
  createCardsInDeck?: Maybe<CreateCardsInDeckPayload>;
  /** Creates a single `Deck`. */
  createDeck?: Maybe<CreateDeckPayload>;
  /** Creates a single `DeckPlayerAttributeTuple`. */
  createDeckPlayerAttributeTuple?: Maybe<CreateDeckPlayerAttributeTuplePayload>;
  /** Creates a single `DeckShare`. */
  createDeckShare?: Maybe<CreateDeckSharePayload>;
  createDeckWithCards?: Maybe<CreateDeckWithCardsPayload>;
  /** Creates a single `Friend`. */
  createFriend?: Maybe<CreateFriendPayload>;
  /** Creates a single `Game`. */
  createGame?: Maybe<CreateGamePayload>;
  /** Creates a single `GameUser`. */
  createGameUser?: Maybe<CreateGameUserPayload>;
  /** Creates a single `Guest`. */
  createGuest?: Maybe<CreateGuestPayload>;
  /** Creates a single `HardRemovalCard`. */
  createHardRemovalCard?: Maybe<CreateHardRemovalCardPayload>;
  /** Creates a single `MatchmakingQueue`. */
  createMatchmakingQueue?: Maybe<CreateMatchmakingQueuePayload>;
  /** Creates a single `MatchmakingTicket`. */
  createMatchmakingTicket?: Maybe<CreateMatchmakingTicketPayload>;
  /** Creates a single `UserEntityAddon`. */
  createUserEntityAddon?: Maybe<CreateUserEntityAddonPayload>;
  /** Deletes a single `BannedDraftCard` using its globally unique id. */
  deleteBannedDraftCard?: Maybe<DeleteBannedDraftCardPayload>;
  /** Deletes a single `BannedDraftCard` using a unique key. */
  deleteBannedDraftCardByCardId?: Maybe<DeleteBannedDraftCardPayload>;
  /** Deletes a single `BotUser` using its globally unique id. */
  deleteBotUser?: Maybe<DeleteBotUserPayload>;
  /** Deletes a single `BotUser` using a unique key. */
  deleteBotUserById?: Maybe<DeleteBotUserPayload>;
  /** Deletes a single `CardsInDeck` using its globally unique id. */
  deleteCardsInDeck?: Maybe<DeleteCardsInDeckPayload>;
  /** Deletes a single `CardsInDeck` using a unique key. */
  deleteCardsInDeckById?: Maybe<DeleteCardsInDeckPayload>;
  /** Deletes a single `Deck` using its globally unique id. */
  deleteDeck?: Maybe<DeleteDeckPayload>;
  /** Deletes a single `Deck` using a unique key. */
  deleteDeckById?: Maybe<DeleteDeckPayload>;
  /** Deletes a single `DeckPlayerAttributeTuple` using its globally unique id. */
  deleteDeckPlayerAttributeTuple?: Maybe<DeleteDeckPlayerAttributeTuplePayload>;
  /** Deletes a single `DeckPlayerAttributeTuple` using a unique key. */
  deleteDeckPlayerAttributeTupleById?: Maybe<DeleteDeckPlayerAttributeTuplePayload>;
  /** Deletes a single `DeckShare` using its globally unique id. */
  deleteDeckShare?: Maybe<DeleteDeckSharePayload>;
  /** Deletes a single `DeckShare` using a unique key. */
  deleteDeckShareByDeckIdAndShareRecipientId?: Maybe<DeleteDeckSharePayload>;
  /** Deletes a single `Friend` using its globally unique id. */
  deleteFriend?: Maybe<DeleteFriendPayload>;
  /** Deletes a single `Friend` using a unique key. */
  deleteFriendByIdAndFriend?: Maybe<DeleteFriendPayload>;
  /** Deletes a single `Game` using its globally unique id. */
  deleteGame?: Maybe<DeleteGamePayload>;
  /** Deletes a single `Game` using a unique key. */
  deleteGameById?: Maybe<DeleteGamePayload>;
  /** Deletes a single `GameUser` using its globally unique id. */
  deleteGameUser?: Maybe<DeleteGameUserPayload>;
  /** Deletes a single `GameUser` using a unique key. */
  deleteGameUserByGameIdAndUserId?: Maybe<DeleteGameUserPayload>;
  /** Deletes a single `Guest` using its globally unique id. */
  deleteGuest?: Maybe<DeleteGuestPayload>;
  /** Deletes a single `Guest` using a unique key. */
  deleteGuestById?: Maybe<DeleteGuestPayload>;
  /** Deletes a single `HardRemovalCard` using its globally unique id. */
  deleteHardRemovalCard?: Maybe<DeleteHardRemovalCardPayload>;
  /** Deletes a single `HardRemovalCard` using a unique key. */
  deleteHardRemovalCardByCardId?: Maybe<DeleteHardRemovalCardPayload>;
  /** Deletes a single `MatchmakingQueue` using its globally unique id. */
  deleteMatchmakingQueue?: Maybe<DeleteMatchmakingQueuePayload>;
  /** Deletes a single `MatchmakingQueue` using a unique key. */
  deleteMatchmakingQueueById?: Maybe<DeleteMatchmakingQueuePayload>;
  /** Deletes a single `MatchmakingTicket` using its globally unique id. */
  deleteMatchmakingTicket?: Maybe<DeleteMatchmakingTicketPayload>;
  /** Deletes a single `MatchmakingTicket` using a unique key. */
  deleteMatchmakingTicketByUserId?: Maybe<DeleteMatchmakingTicketPayload>;
  /** Deletes a single `UserEntityAddon` using its globally unique id. */
  deleteUserEntityAddon?: Maybe<DeleteUserEntityAddonPayload>;
  /** Deletes a single `UserEntityAddon` using a unique key. */
  deleteUserEntityAddonById?: Maybe<DeleteUserEntityAddonPayload>;
  getClasses?: Maybe<GetClassesPayload>;
  getCollectionCards?: Maybe<GetCollectionCardsPayload>;
  publishCard?: Maybe<PublishCardPayload>;
  saveCard?: Maybe<SaveCardPayload>;
  setCardsInDeck?: Maybe<SetCardsInDeckPayload>;
  /** Updates a single `BannedDraftCard` using its globally unique id and a patch. */
  updateBannedDraftCard?: Maybe<UpdateBannedDraftCardPayload>;
  /** Updates a single `BannedDraftCard` using a unique key and a patch. */
  updateBannedDraftCardByCardId?: Maybe<UpdateBannedDraftCardPayload>;
  /** Updates a single `BotUser` using its globally unique id and a patch. */
  updateBotUser?: Maybe<UpdateBotUserPayload>;
  /** Updates a single `BotUser` using a unique key and a patch. */
  updateBotUserById?: Maybe<UpdateBotUserPayload>;
  /** Updates a single `CardsInDeck` using its globally unique id and a patch. */
  updateCardsInDeck?: Maybe<UpdateCardsInDeckPayload>;
  /** Updates a single `CardsInDeck` using a unique key and a patch. */
  updateCardsInDeckById?: Maybe<UpdateCardsInDeckPayload>;
  /** Updates a single `Deck` using its globally unique id and a patch. */
  updateDeck?: Maybe<UpdateDeckPayload>;
  /** Updates a single `Deck` using a unique key and a patch. */
  updateDeckById?: Maybe<UpdateDeckPayload>;
  /** Updates a single `DeckPlayerAttributeTuple` using its globally unique id and a patch. */
  updateDeckPlayerAttributeTuple?: Maybe<UpdateDeckPlayerAttributeTuplePayload>;
  /** Updates a single `DeckPlayerAttributeTuple` using a unique key and a patch. */
  updateDeckPlayerAttributeTupleById?: Maybe<UpdateDeckPlayerAttributeTuplePayload>;
  /** Updates a single `DeckShare` using its globally unique id and a patch. */
  updateDeckShare?: Maybe<UpdateDeckSharePayload>;
  /** Updates a single `DeckShare` using a unique key and a patch. */
  updateDeckShareByDeckIdAndShareRecipientId?: Maybe<UpdateDeckSharePayload>;
  /** Updates a single `Friend` using its globally unique id and a patch. */
  updateFriend?: Maybe<UpdateFriendPayload>;
  /** Updates a single `Friend` using a unique key and a patch. */
  updateFriendByIdAndFriend?: Maybe<UpdateFriendPayload>;
  /** Updates a single `Game` using its globally unique id and a patch. */
  updateGame?: Maybe<UpdateGamePayload>;
  /** Updates a single `Game` using a unique key and a patch. */
  updateGameById?: Maybe<UpdateGamePayload>;
  /** Updates a single `GameUser` using its globally unique id and a patch. */
  updateGameUser?: Maybe<UpdateGameUserPayload>;
  /** Updates a single `GameUser` using a unique key and a patch. */
  updateGameUserByGameIdAndUserId?: Maybe<UpdateGameUserPayload>;
  /** Updates a single `Guest` using its globally unique id and a patch. */
  updateGuest?: Maybe<UpdateGuestPayload>;
  /** Updates a single `Guest` using a unique key and a patch. */
  updateGuestById?: Maybe<UpdateGuestPayload>;
  /** Updates a single `HardRemovalCard` using its globally unique id and a patch. */
  updateHardRemovalCard?: Maybe<UpdateHardRemovalCardPayload>;
  /** Updates a single `HardRemovalCard` using a unique key and a patch. */
  updateHardRemovalCardByCardId?: Maybe<UpdateHardRemovalCardPayload>;
  /** Updates a single `MatchmakingQueue` using its globally unique id and a patch. */
  updateMatchmakingQueue?: Maybe<UpdateMatchmakingQueuePayload>;
  /** Updates a single `MatchmakingQueue` using a unique key and a patch. */
  updateMatchmakingQueueById?: Maybe<UpdateMatchmakingQueuePayload>;
  /** Updates a single `MatchmakingTicket` using its globally unique id and a patch. */
  updateMatchmakingTicket?: Maybe<UpdateMatchmakingTicketPayload>;
  /** Updates a single `MatchmakingTicket` using a unique key and a patch. */
  updateMatchmakingTicketByUserId?: Maybe<UpdateMatchmakingTicketPayload>;
  /** Updates a single `UserEntityAddon` using its globally unique id and a patch. */
  updateUserEntityAddon?: Maybe<UpdateUserEntityAddonPayload>;
  /** Updates a single `UserEntityAddon` using a unique key and a patch. */
  updateUserEntityAddonById?: Maybe<UpdateUserEntityAddonPayload>;
  /** Upserts a single `BannedDraftCard`. */
  upsertBannedDraftCard?: Maybe<UpsertBannedDraftCardPayload>;
  /** Upserts a single `BotUser`. */
  upsertBotUser?: Maybe<UpsertBotUserPayload>;
  /** Upserts a single `CardsInDeck`. */
  upsertCardsInDeck?: Maybe<UpsertCardsInDeckPayload>;
  /** Upserts a single `Deck`. */
  upsertDeck?: Maybe<UpsertDeckPayload>;
  /** Upserts a single `DeckPlayerAttributeTuple`. */
  upsertDeckPlayerAttributeTuple?: Maybe<UpsertDeckPlayerAttributeTuplePayload>;
  /** Upserts a single `DeckShare`. */
  upsertDeckShare?: Maybe<UpsertDeckSharePayload>;
  /** Upserts a single `Friend`. */
  upsertFriend?: Maybe<UpsertFriendPayload>;
  /** Upserts a single `Game`. */
  upsertGame?: Maybe<UpsertGamePayload>;
  /** Upserts a single `GameUser`. */
  upsertGameUser?: Maybe<UpsertGameUserPayload>;
  /** Upserts a single `Guest`. */
  upsertGuest?: Maybe<UpsertGuestPayload>;
  /** Upserts a single `HardRemovalCard`. */
  upsertHardRemovalCard?: Maybe<UpsertHardRemovalCardPayload>;
  /** Upserts a single `MatchmakingQueue`. */
  upsertMatchmakingQueue?: Maybe<UpsertMatchmakingQueuePayload>;
  /** Upserts a single `MatchmakingTicket`. */
  upsertMatchmakingTicket?: Maybe<UpsertMatchmakingTicketPayload>;
  /** Upserts a single `UserEntityAddon`. */
  upsertUserEntityAddon?: Maybe<UpsertUserEntityAddonPayload>;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationArchiveCardArgs = {
  input: ArchiveCardInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationCardCatalogueFormatsArgs = {
  input: CardCatalogueFormatsInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationCardCatalogueGetBannedDraftCardsArgs = {
  input: CardCatalogueGetBannedDraftCardsInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationCardCatalogueGetBaseClassesArgs = {
  input: CardCatalogueGetBaseClassesInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationCardCatalogueGetCardByIdArgs = {
  input: CardCatalogueGetCardByIdInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationCardCatalogueGetCardByNameArgs = {
  input: CardCatalogueGetCardByNameInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationCardCatalogueGetCardByNameAndClassArgs = {
  input: CardCatalogueGetCardByNameAndClassInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationCardCatalogueGetClassCardsArgs = {
  input: CardCatalogueGetClassCardsInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationCardCatalogueGetFormatArgs = {
  input: CardCatalogueGetFormatInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationCardCatalogueGetHardRemovalCardsArgs = {
  input: CardCatalogueGetHardRemovalCardsInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationCardCatalogueGetHeroCardArgs = {
  input: CardCatalogueGetHeroCardInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationCardCatalogueQueryArgs = {
  input: CardCatalogueQueryInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationClusteredGamesUpdateGameAndUsersArgs = {
  input: ClusteredGamesUpdateGameAndUsersInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationCreateBannedDraftCardArgs = {
  input: CreateBannedDraftCardInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationCreateBotUserArgs = {
  input: CreateBotUserInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationCreateCardArgs = {
  input: CreateCardInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationCreateCardsInDeckArgs = {
  input: CreateCardsInDeckInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationCreateDeckArgs = {
  input: CreateDeckInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationCreateDeckPlayerAttributeTupleArgs = {
  input: CreateDeckPlayerAttributeTupleInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationCreateDeckShareArgs = {
  input: CreateDeckShareInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationCreateDeckWithCardsArgs = {
  input: CreateDeckWithCardsInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationCreateFriendArgs = {
  input: CreateFriendInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationCreateGameArgs = {
  input: CreateGameInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationCreateGameUserArgs = {
  input: CreateGameUserInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationCreateGuestArgs = {
  input: CreateGuestInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationCreateHardRemovalCardArgs = {
  input: CreateHardRemovalCardInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationCreateMatchmakingQueueArgs = {
  input: CreateMatchmakingQueueInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationCreateMatchmakingTicketArgs = {
  input: CreateMatchmakingTicketInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationCreateUserEntityAddonArgs = {
  input: CreateUserEntityAddonInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationDeleteBannedDraftCardArgs = {
  input: DeleteBannedDraftCardInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationDeleteBannedDraftCardByCardIdArgs = {
  input: DeleteBannedDraftCardByCardIdInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationDeleteBotUserArgs = {
  input: DeleteBotUserInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationDeleteBotUserByIdArgs = {
  input: DeleteBotUserByIdInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationDeleteCardsInDeckArgs = {
  input: DeleteCardsInDeckInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationDeleteCardsInDeckByIdArgs = {
  input: DeleteCardsInDeckByIdInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationDeleteDeckArgs = {
  input: DeleteDeckInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationDeleteDeckByIdArgs = {
  input: DeleteDeckByIdInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationDeleteDeckPlayerAttributeTupleArgs = {
  input: DeleteDeckPlayerAttributeTupleInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationDeleteDeckPlayerAttributeTupleByIdArgs = {
  input: DeleteDeckPlayerAttributeTupleByIdInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationDeleteDeckShareArgs = {
  input: DeleteDeckShareInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationDeleteDeckShareByDeckIdAndShareRecipientIdArgs = {
  input: DeleteDeckShareByDeckIdAndShareRecipientIdInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationDeleteFriendArgs = {
  input: DeleteFriendInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationDeleteFriendByIdAndFriendArgs = {
  input: DeleteFriendByIdAndFriendInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationDeleteGameArgs = {
  input: DeleteGameInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationDeleteGameByIdArgs = {
  input: DeleteGameByIdInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationDeleteGameUserArgs = {
  input: DeleteGameUserInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationDeleteGameUserByGameIdAndUserIdArgs = {
  input: DeleteGameUserByGameIdAndUserIdInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationDeleteGuestArgs = {
  input: DeleteGuestInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationDeleteGuestByIdArgs = {
  input: DeleteGuestByIdInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationDeleteHardRemovalCardArgs = {
  input: DeleteHardRemovalCardInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationDeleteHardRemovalCardByCardIdArgs = {
  input: DeleteHardRemovalCardByCardIdInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationDeleteMatchmakingQueueArgs = {
  input: DeleteMatchmakingQueueInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationDeleteMatchmakingQueueByIdArgs = {
  input: DeleteMatchmakingQueueByIdInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationDeleteMatchmakingTicketArgs = {
  input: DeleteMatchmakingTicketInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationDeleteMatchmakingTicketByUserIdArgs = {
  input: DeleteMatchmakingTicketByUserIdInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationDeleteUserEntityAddonArgs = {
  input: DeleteUserEntityAddonInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationDeleteUserEntityAddonByIdArgs = {
  input: DeleteUserEntityAddonByIdInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationGetClassesArgs = {
  input: GetClassesInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationGetCollectionCardsArgs = {
  input: GetCollectionCardsInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationPublishCardArgs = {
  input: PublishCardInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationSaveCardArgs = {
  input: SaveCardInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationSetCardsInDeckArgs = {
  input: SetCardsInDeckInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpdateBannedDraftCardArgs = {
  input: UpdateBannedDraftCardInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpdateBannedDraftCardByCardIdArgs = {
  input: UpdateBannedDraftCardByCardIdInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpdateBotUserArgs = {
  input: UpdateBotUserInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpdateBotUserByIdArgs = {
  input: UpdateBotUserByIdInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpdateCardsInDeckArgs = {
  input: UpdateCardsInDeckInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpdateCardsInDeckByIdArgs = {
  input: UpdateCardsInDeckByIdInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpdateDeckArgs = {
  input: UpdateDeckInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpdateDeckByIdArgs = {
  input: UpdateDeckByIdInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpdateDeckPlayerAttributeTupleArgs = {
  input: UpdateDeckPlayerAttributeTupleInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpdateDeckPlayerAttributeTupleByIdArgs = {
  input: UpdateDeckPlayerAttributeTupleByIdInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpdateDeckShareArgs = {
  input: UpdateDeckShareInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpdateDeckShareByDeckIdAndShareRecipientIdArgs = {
  input: UpdateDeckShareByDeckIdAndShareRecipientIdInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpdateFriendArgs = {
  input: UpdateFriendInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpdateFriendByIdAndFriendArgs = {
  input: UpdateFriendByIdAndFriendInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpdateGameArgs = {
  input: UpdateGameInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpdateGameByIdArgs = {
  input: UpdateGameByIdInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpdateGameUserArgs = {
  input: UpdateGameUserInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpdateGameUserByGameIdAndUserIdArgs = {
  input: UpdateGameUserByGameIdAndUserIdInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpdateGuestArgs = {
  input: UpdateGuestInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpdateGuestByIdArgs = {
  input: UpdateGuestByIdInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpdateHardRemovalCardArgs = {
  input: UpdateHardRemovalCardInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpdateHardRemovalCardByCardIdArgs = {
  input: UpdateHardRemovalCardByCardIdInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpdateMatchmakingQueueArgs = {
  input: UpdateMatchmakingQueueInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpdateMatchmakingQueueByIdArgs = {
  input: UpdateMatchmakingQueueByIdInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpdateMatchmakingTicketArgs = {
  input: UpdateMatchmakingTicketInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpdateMatchmakingTicketByUserIdArgs = {
  input: UpdateMatchmakingTicketByUserIdInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpdateUserEntityAddonArgs = {
  input: UpdateUserEntityAddonInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpdateUserEntityAddonByIdArgs = {
  input: UpdateUserEntityAddonByIdInput;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpsertBannedDraftCardArgs = {
  input: UpsertBannedDraftCardInput;
  where?: InputMaybe<UpsertBannedDraftCardWhere>;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpsertBotUserArgs = {
  input: UpsertBotUserInput;
  where?: InputMaybe<UpsertBotUserWhere>;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpsertCardsInDeckArgs = {
  input: UpsertCardsInDeckInput;
  where?: InputMaybe<UpsertCardsInDeckWhere>;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpsertDeckArgs = {
  input: UpsertDeckInput;
  where?: InputMaybe<UpsertDeckWhere>;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpsertDeckPlayerAttributeTupleArgs = {
  input: UpsertDeckPlayerAttributeTupleInput;
  where?: InputMaybe<UpsertDeckPlayerAttributeTupleWhere>;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpsertDeckShareArgs = {
  input: UpsertDeckShareInput;
  where?: InputMaybe<UpsertDeckShareWhere>;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpsertFriendArgs = {
  input: UpsertFriendInput;
  where?: InputMaybe<UpsertFriendWhere>;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpsertGameArgs = {
  input: UpsertGameInput;
  where?: InputMaybe<UpsertGameWhere>;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpsertGameUserArgs = {
  input: UpsertGameUserInput;
  where?: InputMaybe<UpsertGameUserWhere>;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpsertGuestArgs = {
  input: UpsertGuestInput;
  where?: InputMaybe<UpsertGuestWhere>;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpsertHardRemovalCardArgs = {
  input: UpsertHardRemovalCardInput;
  where?: InputMaybe<UpsertHardRemovalCardWhere>;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpsertMatchmakingQueueArgs = {
  input: UpsertMatchmakingQueueInput;
  where?: InputMaybe<UpsertMatchmakingQueueWhere>;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpsertMatchmakingTicketArgs = {
  input: UpsertMatchmakingTicketInput;
  where?: InputMaybe<UpsertMatchmakingTicketWhere>;
};


/** The root mutation type which contains root level fields which mutate data. */
export type MutationUpsertUserEntityAddonArgs = {
  input: UpsertUserEntityAddonInput;
  where?: InputMaybe<UpsertUserEntityAddonWhere>;
};

/** An object with a globally unique `ID`. */
export type Node = {
  /** A globally unique identifier. Can be used in various places throughout the system to identify this single value. */
  nodeId: Scalars['ID'];
};

/** Information about pagination in a connection. */
export type PageInfo = {
  __typename?: 'PageInfo';
  /** When paginating forwards, the cursor to continue. */
  endCursor?: Maybe<Scalars['Cursor']>;
  /** When paginating forwards, are there more items? */
  hasNextPage: Scalars['Boolean'];
  /** When paginating backwards, are there more items? */
  hasPreviousPage: Scalars['Boolean'];
  /** When paginating backwards, the cursor to continue. */
  startCursor?: Maybe<Scalars['Cursor']>;
};

/** All input for the `publishCard` mutation. */
export type PublishCardInput = {
  cardId?: InputMaybe<Scalars['String']>;
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
};

/** The output of our `publishCard` mutation. */
export type PublishCardPayload = {
  __typename?: 'PublishCardPayload';
  bigInt?: Maybe<Scalars['BigInt']>;
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};

export type Query = Node & {
  __typename?: 'Query';
  allArt: Array<ImageDef>;
  /** Reads and enables pagination through a set of `BannedDraftCard`. */
  allBannedDraftCards?: Maybe<BannedDraftCardsConnection>;
  /** Reads and enables pagination through a set of `BotUser`. */
  allBotUsers?: Maybe<BotUsersConnection>;
  /** Reads and enables pagination through a set of `Card`. */
  allCards?: Maybe<CardsConnection>;
  /** Reads and enables pagination through a set of `CardsInDeck`. */
  allCardsInDecks?: Maybe<CardsInDecksConnection>;
  /** Reads and enables pagination through a set of `Class`. */
  allClasses?: Maybe<ClassesConnection>;
  /** Reads and enables pagination through a set of `CollectionCard`. */
  allCollectionCards?: Maybe<CollectionCardsConnection>;
  /** Reads and enables pagination through a set of `CurrentCard`. */
  allCurrentCards?: Maybe<CurrentCardsConnection>;
  /** Reads and enables pagination through a set of `DeckPlayerAttributeTuple`. */
  allDeckPlayerAttributeTuples?: Maybe<DeckPlayerAttributeTuplesConnection>;
  /** Reads and enables pagination through a set of `DeckShare`. */
  allDeckShares?: Maybe<DeckSharesConnection>;
  /** Reads and enables pagination through a set of `Deck`. */
  allDecks?: Maybe<DecksConnection>;
  /** Reads and enables pagination through a set of `Friend`. */
  allFriends?: Maybe<FriendsConnection>;
  /** Reads and enables pagination through a set of `GameUser`. */
  allGameUsers?: Maybe<GameUsersConnection>;
  /** Reads and enables pagination through a set of `Game`. */
  allGames?: Maybe<GamesConnection>;
  /** Reads and enables pagination through a set of `Guest`. */
  allGuests?: Maybe<GuestsConnection>;
  /** Reads and enables pagination through a set of `HardRemovalCard`. */
  allHardRemovalCards?: Maybe<HardRemovalCardsConnection>;
  /** Reads and enables pagination through a set of `MatchmakingQueue`. */
  allMatchmakingQueues?: Maybe<MatchmakingQueuesConnection>;
  /** Reads and enables pagination through a set of `MatchmakingTicket`. */
  allMatchmakingTickets?: Maybe<MatchmakingTicketsConnection>;
  /** Reads and enables pagination through a set of `UserEntityAddon`. */
  allUserEntityAddons?: Maybe<UserEntityAddonsConnection>;
  artById?: Maybe<ImageDef>;
  /** Reads a single `BannedDraftCard` using its globally unique `ID`. */
  bannedDraftCard?: Maybe<BannedDraftCard>;
  bannedDraftCardByCardId?: Maybe<BannedDraftCard>;
  /** Reads a single `BotUser` using its globally unique `ID`. */
  botUser?: Maybe<BotUser>;
  botUserById?: Maybe<BotUser>;
  /** Reads a single `CardsInDeck` using its globally unique `ID`. */
  cardsInDeck?: Maybe<CardsInDeck>;
  cardsInDeckById?: Maybe<CardsInDeck>;
  /** Reads a single `Deck` using its globally unique `ID`. */
  deck?: Maybe<Deck>;
  deckById?: Maybe<Deck>;
  /** Reads a single `DeckPlayerAttributeTuple` using its globally unique `ID`. */
  deckPlayerAttributeTuple?: Maybe<DeckPlayerAttributeTuple>;
  deckPlayerAttributeTupleById?: Maybe<DeckPlayerAttributeTuple>;
  /** Reads a single `DeckShare` using its globally unique `ID`. */
  deckShare?: Maybe<DeckShare>;
  deckShareByDeckIdAndShareRecipientId?: Maybe<DeckShare>;
  /** Reads a single `Friend` using its globally unique `ID`. */
  friend?: Maybe<Friend>;
  friendByIdAndFriend?: Maybe<Friend>;
  /** Reads a single `Game` using its globally unique `ID`. */
  game?: Maybe<Game>;
  gameById?: Maybe<Game>;
  /** Reads a single `GameUser` using its globally unique `ID`. */
  gameUser?: Maybe<GameUser>;
  gameUserByGameIdAndUserId?: Maybe<GameUser>;
  getLatestCard?: Maybe<Card>;
  getUserId?: Maybe<Scalars['String']>;
  /** Reads a single `Guest` using its globally unique `ID`. */
  guest?: Maybe<Guest>;
  guestById?: Maybe<Guest>;
  /** Reads a single `HardRemovalCard` using its globally unique `ID`. */
  hardRemovalCard?: Maybe<HardRemovalCard>;
  hardRemovalCardByCardId?: Maybe<HardRemovalCard>;
  /** Reads a single `MatchmakingQueue` using its globally unique `ID`. */
  matchmakingQueue?: Maybe<MatchmakingQueue>;
  matchmakingQueueById?: Maybe<MatchmakingQueue>;
  /** Reads a single `MatchmakingTicket` using its globally unique `ID`. */
  matchmakingTicket?: Maybe<MatchmakingTicket>;
  matchmakingTicketByUserId?: Maybe<MatchmakingTicket>;
  /** Fetches an object given its globally unique `ID`. */
  node?: Maybe<Node>;
  /** The root query type must be a `Node` to work well with Relay 1 mutations. This just resolves to `query`. */
  nodeId: Scalars['ID'];
  /**
   * Exposes the root query type nested one level down. This is helpful for Relay 1
   * which can only query top level fields if they are in a particular form.
   */
  query: Query;
  /** Reads a single `UserEntityAddon` using its globally unique `ID`. */
  userEntityAddon?: Maybe<UserEntityAddon>;
  userEntityAddonById?: Maybe<UserEntityAddon>;
};


export type QueryAllBannedDraftCardsArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<BannedDraftCardCondition>;
  filter?: InputMaybe<BannedDraftCardFilter>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<BannedDraftCardsOrderBy>>;
};


export type QueryAllBotUsersArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<BotUserCondition>;
  filter?: InputMaybe<BotUserFilter>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<BotUsersOrderBy>>;
};


export type QueryAllCardsArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<CardCondition>;
  filter?: InputMaybe<CardFilter>;
  first?: InputMaybe<Scalars['Int']>;
  includeArchived?: InputMaybe<IncludeArchivedOption>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<CardsOrderBy>>;
};


export type QueryAllCardsInDecksArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<CardsInDeckCondition>;
  filter?: InputMaybe<CardsInDeckFilter>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<CardsInDecksOrderBy>>;
};


export type QueryAllClassesArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<ClassCondition>;
  filter?: InputMaybe<ClassFilter>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<ClassesOrderBy>>;
};


export type QueryAllCollectionCardsArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<CollectionCardCondition>;
  filter?: InputMaybe<CollectionCardFilter>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<CollectionCardsOrderBy>>;
};


export type QueryAllCurrentCardsArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<CurrentCardCondition>;
  filter?: InputMaybe<CurrentCardFilter>;
  first?: InputMaybe<Scalars['Int']>;
  includeArchived?: InputMaybe<IncludeArchivedOption>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<CurrentCardsOrderBy>>;
};


export type QueryAllDeckPlayerAttributeTuplesArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<DeckPlayerAttributeTupleCondition>;
  filter?: InputMaybe<DeckPlayerAttributeTupleFilter>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<DeckPlayerAttributeTuplesOrderBy>>;
};


export type QueryAllDeckSharesArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<DeckShareCondition>;
  filter?: InputMaybe<DeckShareFilter>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<DeckSharesOrderBy>>;
};


export type QueryAllDecksArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<DeckCondition>;
  filter?: InputMaybe<DeckFilter>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<DecksOrderBy>>;
};


export type QueryAllFriendsArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<FriendCondition>;
  filter?: InputMaybe<FriendFilter>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<FriendsOrderBy>>;
};


export type QueryAllGameUsersArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<GameUserCondition>;
  filter?: InputMaybe<GameUserFilter>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<GameUsersOrderBy>>;
};


export type QueryAllGamesArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<GameCondition>;
  filter?: InputMaybe<GameFilter>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<GamesOrderBy>>;
};


export type QueryAllGuestsArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<GuestCondition>;
  filter?: InputMaybe<GuestFilter>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<GuestsOrderBy>>;
};


export type QueryAllHardRemovalCardsArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<HardRemovalCardCondition>;
  filter?: InputMaybe<HardRemovalCardFilter>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<HardRemovalCardsOrderBy>>;
};


export type QueryAllMatchmakingQueuesArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<MatchmakingQueueCondition>;
  filter?: InputMaybe<MatchmakingQueueFilter>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<MatchmakingQueuesOrderBy>>;
};


export type QueryAllMatchmakingTicketsArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<MatchmakingTicketCondition>;
  filter?: InputMaybe<MatchmakingTicketFilter>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<MatchmakingTicketsOrderBy>>;
};


export type QueryAllUserEntityAddonsArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<UserEntityAddonCondition>;
  filter?: InputMaybe<UserEntityAddonFilter>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<UserEntityAddonsOrderBy>>;
};


export type QueryArtByIdArgs = {
  id: Scalars['String'];
};


export type QueryBannedDraftCardArgs = {
  nodeId: Scalars['ID'];
};


export type QueryBannedDraftCardByCardIdArgs = {
  cardId: Scalars['String'];
};


export type QueryBotUserArgs = {
  nodeId: Scalars['ID'];
};


export type QueryBotUserByIdArgs = {
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


export type QueryGetLatestCardArgs = {
  cardId?: InputMaybe<Scalars['String']>;
  published?: InputMaybe<Scalars['Boolean']>;
};


export type QueryGuestArgs = {
  nodeId: Scalars['ID'];
};


export type QueryGuestByIdArgs = {
  id: Scalars['BigInt'];
};


export type QueryHardRemovalCardArgs = {
  nodeId: Scalars['ID'];
};


export type QueryHardRemovalCardByCardIdArgs = {
  cardId: Scalars['String'];
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

/** All input for the `saveCard` mutation. */
export type SaveCardInput = {
  cardId?: InputMaybe<Scalars['String']>;
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  json?: InputMaybe<Scalars['JSON']>;
  workspace?: InputMaybe<Scalars['String']>;
};

/** The output of our `saveCard` mutation. */
export type SaveCardPayload = {
  __typename?: 'SaveCardPayload';
  card?: Maybe<Card>;
  /** An edge for our `Card`. May be used by Relay 1. */
  cardEdge?: Maybe<CardsEdge>;
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our `saveCard` mutation. */
export type SaveCardPayloadCardEdgeArgs = {
  orderBy?: InputMaybe<Array<CardsOrderBy>>;
};

/** All input for the `setCardsInDeck` mutation. */
export type SetCardsInDeckInput = {
  cardIds?: InputMaybe<Array<InputMaybe<Scalars['String']>>>;
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  deck?: InputMaybe<Scalars['String']>;
};

/** The output of our `setCardsInDeck` mutation. */
export type SetCardsInDeckPayload = {
  __typename?: 'SetCardsInDeckPayload';
  cardsInDecks?: Maybe<Array<Maybe<CardsInDeck>>>;
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};

/** A filter to be used against String fields. All fields are combined with a logical ‘and.’ */
export type StringFilter = {
  /** Not equal to the specified value, treating null like an ordinary value. */
  distinctFrom?: InputMaybe<Scalars['String']>;
  /** Not equal to the specified value, treating null like an ordinary value (case-insensitive). */
  distinctFromInsensitive?: InputMaybe<Scalars['String']>;
  /** Ends with the specified string (case-sensitive). */
  endsWith?: InputMaybe<Scalars['String']>;
  /** Ends with the specified string (case-insensitive). */
  endsWithInsensitive?: InputMaybe<Scalars['String']>;
  /** Equal to the specified value. */
  equalTo?: InputMaybe<Scalars['String']>;
  /** Equal to the specified value (case-insensitive). */
  equalToInsensitive?: InputMaybe<Scalars['String']>;
  /** Greater than the specified value. */
  greaterThan?: InputMaybe<Scalars['String']>;
  /** Greater than the specified value (case-insensitive). */
  greaterThanInsensitive?: InputMaybe<Scalars['String']>;
  /** Greater than or equal to the specified value. */
  greaterThanOrEqualTo?: InputMaybe<Scalars['String']>;
  /** Greater than or equal to the specified value (case-insensitive). */
  greaterThanOrEqualToInsensitive?: InputMaybe<Scalars['String']>;
  /** Included in the specified list. */
  in?: InputMaybe<Array<Scalars['String']>>;
  /** Included in the specified list (case-insensitive). */
  inInsensitive?: InputMaybe<Array<Scalars['String']>>;
  /** Contains the specified string (case-sensitive). */
  includes?: InputMaybe<Scalars['String']>;
  /** Contains the specified string (case-insensitive). */
  includesInsensitive?: InputMaybe<Scalars['String']>;
  /** Is null (if `true` is specified) or is not null (if `false` is specified). */
  isNull?: InputMaybe<Scalars['Boolean']>;
  /** Less than the specified value. */
  lessThan?: InputMaybe<Scalars['String']>;
  /** Less than the specified value (case-insensitive). */
  lessThanInsensitive?: InputMaybe<Scalars['String']>;
  /** Less than or equal to the specified value. */
  lessThanOrEqualTo?: InputMaybe<Scalars['String']>;
  /** Less than or equal to the specified value (case-insensitive). */
  lessThanOrEqualToInsensitive?: InputMaybe<Scalars['String']>;
  /** Matches the specified pattern (case-sensitive). An underscore (_) matches any single character; a percent sign (%) matches any sequence of zero or more characters. */
  like?: InputMaybe<Scalars['String']>;
  /** Matches the specified pattern (case-insensitive). An underscore (_) matches any single character; a percent sign (%) matches any sequence of zero or more characters. */
  likeInsensitive?: InputMaybe<Scalars['String']>;
  /** Equal to the specified value, treating null like an ordinary value. */
  notDistinctFrom?: InputMaybe<Scalars['String']>;
  /** Equal to the specified value, treating null like an ordinary value (case-insensitive). */
  notDistinctFromInsensitive?: InputMaybe<Scalars['String']>;
  /** Does not end with the specified string (case-sensitive). */
  notEndsWith?: InputMaybe<Scalars['String']>;
  /** Does not end with the specified string (case-insensitive). */
  notEndsWithInsensitive?: InputMaybe<Scalars['String']>;
  /** Not equal to the specified value. */
  notEqualTo?: InputMaybe<Scalars['String']>;
  /** Not equal to the specified value (case-insensitive). */
  notEqualToInsensitive?: InputMaybe<Scalars['String']>;
  /** Not included in the specified list. */
  notIn?: InputMaybe<Array<Scalars['String']>>;
  /** Not included in the specified list (case-insensitive). */
  notInInsensitive?: InputMaybe<Array<Scalars['String']>>;
  /** Does not contain the specified string (case-sensitive). */
  notIncludes?: InputMaybe<Scalars['String']>;
  /** Does not contain the specified string (case-insensitive). */
  notIncludesInsensitive?: InputMaybe<Scalars['String']>;
  /** Does not match the specified pattern (case-sensitive). An underscore (_) matches any single character; a percent sign (%) matches any sequence of zero or more characters. */
  notLike?: InputMaybe<Scalars['String']>;
  /** Does not match the specified pattern (case-insensitive). An underscore (_) matches any single character; a percent sign (%) matches any sequence of zero or more characters. */
  notLikeInsensitive?: InputMaybe<Scalars['String']>;
  /** Does not start with the specified string (case-sensitive). */
  notStartsWith?: InputMaybe<Scalars['String']>;
  /** Does not start with the specified string (case-insensitive). */
  notStartsWithInsensitive?: InputMaybe<Scalars['String']>;
  /** Starts with the specified string (case-sensitive). */
  startsWith?: InputMaybe<Scalars['String']>;
  /** Starts with the specified string (case-insensitive). */
  startsWithInsensitive?: InputMaybe<Scalars['String']>;
};

/** All input for the `updateBannedDraftCardByCardId` mutation. */
export type UpdateBannedDraftCardByCardIdInput = {
  /** An object where the defined keys will be set on the `BannedDraftCard` being updated. */
  bannedDraftCardPatch: BannedDraftCardPatch;
  cardId: Scalars['String'];
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
};

/** All input for the `updateBannedDraftCard` mutation. */
export type UpdateBannedDraftCardInput = {
  /** An object where the defined keys will be set on the `BannedDraftCard` being updated. */
  bannedDraftCardPatch: BannedDraftCardPatch;
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The globally unique `ID` which will identify a single `BannedDraftCard` to be updated. */
  nodeId: Scalars['ID'];
};

/** The output of our update `BannedDraftCard` mutation. */
export type UpdateBannedDraftCardPayload = {
  __typename?: 'UpdateBannedDraftCardPayload';
  /** The `BannedDraftCard` that was updated by this mutation. */
  bannedDraftCard?: Maybe<BannedDraftCard>;
  /** An edge for our `BannedDraftCard`. May be used by Relay 1. */
  bannedDraftCardEdge?: Maybe<BannedDraftCardsEdge>;
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our update `BannedDraftCard` mutation. */
export type UpdateBannedDraftCardPayloadBannedDraftCardEdgeArgs = {
  orderBy?: InputMaybe<Array<BannedDraftCardsOrderBy>>;
};

/** All input for the `updateBotUserById` mutation. */
export type UpdateBotUserByIdInput = {
  /** An object where the defined keys will be set on the `BotUser` being updated. */
  botUserPatch: BotUserPatch;
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  id: Scalars['String'];
};

/** All input for the `updateBotUser` mutation. */
export type UpdateBotUserInput = {
  /** An object where the defined keys will be set on the `BotUser` being updated. */
  botUserPatch: BotUserPatch;
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The globally unique `ID` which will identify a single `BotUser` to be updated. */
  nodeId: Scalars['ID'];
};

/** The output of our update `BotUser` mutation. */
export type UpdateBotUserPayload = {
  __typename?: 'UpdateBotUserPayload';
  /** The `BotUser` that was updated by this mutation. */
  botUser?: Maybe<BotUser>;
  /** An edge for our `BotUser`. May be used by Relay 1. */
  botUserEdge?: Maybe<BotUsersEdge>;
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our update `BotUser` mutation. */
export type UpdateBotUserPayloadBotUserEdgeArgs = {
  orderBy?: InputMaybe<Array<BotUsersOrderBy>>;
};

/** All input for the `updateCardsInDeckById` mutation. */
export type UpdateCardsInDeckByIdInput = {
  /** An object where the defined keys will be set on the `CardsInDeck` being updated. */
  cardsInDeckPatch: CardsInDeckPatch;
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  id: Scalars['BigInt'];
};

/** All input for the `updateCardsInDeck` mutation. */
export type UpdateCardsInDeckInput = {
  /** An object where the defined keys will be set on the `CardsInDeck` being updated. */
  cardsInDeckPatch: CardsInDeckPatch;
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The globally unique `ID` which will identify a single `CardsInDeck` to be updated. */
  nodeId: Scalars['ID'];
};

/** The output of our update `CardsInDeck` mutation. */
export type UpdateCardsInDeckPayload = {
  __typename?: 'UpdateCardsInDeckPayload';
  /** The `CardsInDeck` that was updated by this mutation. */
  cardsInDeck?: Maybe<CardsInDeck>;
  /** An edge for our `CardsInDeck`. May be used by Relay 1. */
  cardsInDeckEdge?: Maybe<CardsInDecksEdge>;
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Reads a single `Deck` that is related to this `CardsInDeck`. */
  deckByDeckId?: Maybe<Deck>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our update `CardsInDeck` mutation. */
export type UpdateCardsInDeckPayloadCardsInDeckEdgeArgs = {
  orderBy?: InputMaybe<Array<CardsInDecksOrderBy>>;
};

/** All input for the `updateDeckById` mutation. */
export type UpdateDeckByIdInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** An object where the defined keys will be set on the `Deck` being updated. */
  deckPatch: DeckPatch;
  id: Scalars['String'];
};

/** All input for the `updateDeck` mutation. */
export type UpdateDeckInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** An object where the defined keys will be set on the `Deck` being updated. */
  deckPatch: DeckPatch;
  /** The globally unique `ID` which will identify a single `Deck` to be updated. */
  nodeId: Scalars['ID'];
};

/** The output of our update `Deck` mutation. */
export type UpdateDeckPayload = {
  __typename?: 'UpdateDeckPayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** The `Deck` that was updated by this mutation. */
  deck?: Maybe<Deck>;
  /** An edge for our `Deck`. May be used by Relay 1. */
  deckEdge?: Maybe<DecksEdge>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our update `Deck` mutation. */
export type UpdateDeckPayloadDeckEdgeArgs = {
  orderBy?: InputMaybe<Array<DecksOrderBy>>;
};

/** All input for the `updateDeckPlayerAttributeTupleById` mutation. */
export type UpdateDeckPlayerAttributeTupleByIdInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** An object where the defined keys will be set on the `DeckPlayerAttributeTuple` being updated. */
  deckPlayerAttributeTuplePatch: DeckPlayerAttributeTuplePatch;
  id: Scalars['BigInt'];
};

/** All input for the `updateDeckPlayerAttributeTuple` mutation. */
export type UpdateDeckPlayerAttributeTupleInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** An object where the defined keys will be set on the `DeckPlayerAttributeTuple` being updated. */
  deckPlayerAttributeTuplePatch: DeckPlayerAttributeTuplePatch;
  /** The globally unique `ID` which will identify a single `DeckPlayerAttributeTuple` to be updated. */
  nodeId: Scalars['ID'];
};

/** The output of our update `DeckPlayerAttributeTuple` mutation. */
export type UpdateDeckPlayerAttributeTuplePayload = {
  __typename?: 'UpdateDeckPlayerAttributeTuplePayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Reads a single `Deck` that is related to this `DeckPlayerAttributeTuple`. */
  deckByDeckId?: Maybe<Deck>;
  /** The `DeckPlayerAttributeTuple` that was updated by this mutation. */
  deckPlayerAttributeTuple?: Maybe<DeckPlayerAttributeTuple>;
  /** An edge for our `DeckPlayerAttributeTuple`. May be used by Relay 1. */
  deckPlayerAttributeTupleEdge?: Maybe<DeckPlayerAttributeTuplesEdge>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our update `DeckPlayerAttributeTuple` mutation. */
export type UpdateDeckPlayerAttributeTuplePayloadDeckPlayerAttributeTupleEdgeArgs = {
  orderBy?: InputMaybe<Array<DeckPlayerAttributeTuplesOrderBy>>;
};

/** All input for the `updateDeckShareByDeckIdAndShareRecipientId` mutation. */
export type UpdateDeckShareByDeckIdAndShareRecipientIdInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  deckId: Scalars['String'];
  /** An object where the defined keys will be set on the `DeckShare` being updated. */
  deckSharePatch: DeckSharePatch;
  shareRecipientId: Scalars['String'];
};

/** All input for the `updateDeckShare` mutation. */
export type UpdateDeckShareInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** An object where the defined keys will be set on the `DeckShare` being updated. */
  deckSharePatch: DeckSharePatch;
  /** The globally unique `ID` which will identify a single `DeckShare` to be updated. */
  nodeId: Scalars['ID'];
};

/** The output of our update `DeckShare` mutation. */
export type UpdateDeckSharePayload = {
  __typename?: 'UpdateDeckSharePayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Reads a single `Deck` that is related to this `DeckShare`. */
  deckByDeckId?: Maybe<Deck>;
  /** The `DeckShare` that was updated by this mutation. */
  deckShare?: Maybe<DeckShare>;
  /** An edge for our `DeckShare`. May be used by Relay 1. */
  deckShareEdge?: Maybe<DeckSharesEdge>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our update `DeckShare` mutation. */
export type UpdateDeckSharePayloadDeckShareEdgeArgs = {
  orderBy?: InputMaybe<Array<DeckSharesOrderBy>>;
};

/** All input for the `updateFriendByIdAndFriend` mutation. */
export type UpdateFriendByIdAndFriendInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  friend: Scalars['String'];
  /** An object where the defined keys will be set on the `Friend` being updated. */
  friendPatch: FriendPatch;
  id: Scalars['String'];
};

/** All input for the `updateFriend` mutation. */
export type UpdateFriendInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** An object where the defined keys will be set on the `Friend` being updated. */
  friendPatch: FriendPatch;
  /** The globally unique `ID` which will identify a single `Friend` to be updated. */
  nodeId: Scalars['ID'];
};

/** The output of our update `Friend` mutation. */
export type UpdateFriendPayload = {
  __typename?: 'UpdateFriendPayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** The `Friend` that was updated by this mutation. */
  friend?: Maybe<Friend>;
  /** An edge for our `Friend`. May be used by Relay 1. */
  friendEdge?: Maybe<FriendsEdge>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our update `Friend` mutation. */
export type UpdateFriendPayloadFriendEdgeArgs = {
  orderBy?: InputMaybe<Array<FriendsOrderBy>>;
};

/** All input for the `updateGameById` mutation. */
export type UpdateGameByIdInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** An object where the defined keys will be set on the `Game` being updated. */
  gamePatch: GamePatch;
  id: Scalars['BigInt'];
};

/** All input for the `updateGame` mutation. */
export type UpdateGameInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** An object where the defined keys will be set on the `Game` being updated. */
  gamePatch: GamePatch;
  /** The globally unique `ID` which will identify a single `Game` to be updated. */
  nodeId: Scalars['ID'];
};

/** The output of our update `Game` mutation. */
export type UpdateGamePayload = {
  __typename?: 'UpdateGamePayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** The `Game` that was updated by this mutation. */
  game?: Maybe<Game>;
  /** An edge for our `Game`. May be used by Relay 1. */
  gameEdge?: Maybe<GamesEdge>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our update `Game` mutation. */
export type UpdateGamePayloadGameEdgeArgs = {
  orderBy?: InputMaybe<Array<GamesOrderBy>>;
};

/** All input for the `updateGameUserByGameIdAndUserId` mutation. */
export type UpdateGameUserByGameIdAndUserIdInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  gameId: Scalars['BigInt'];
  /** An object where the defined keys will be set on the `GameUser` being updated. */
  gameUserPatch: GameUserPatch;
  userId: Scalars['String'];
};

/** All input for the `updateGameUser` mutation. */
export type UpdateGameUserInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** An object where the defined keys will be set on the `GameUser` being updated. */
  gameUserPatch: GameUserPatch;
  /** The globally unique `ID` which will identify a single `GameUser` to be updated. */
  nodeId: Scalars['ID'];
};

/** The output of our update `GameUser` mutation. */
export type UpdateGameUserPayload = {
  __typename?: 'UpdateGameUserPayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Reads a single `Deck` that is related to this `GameUser`. */
  deckByDeckId?: Maybe<Deck>;
  /** Reads a single `Game` that is related to this `GameUser`. */
  gameByGameId?: Maybe<Game>;
  /** The `GameUser` that was updated by this mutation. */
  gameUser?: Maybe<GameUser>;
  /** An edge for our `GameUser`. May be used by Relay 1. */
  gameUserEdge?: Maybe<GameUsersEdge>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our update `GameUser` mutation. */
export type UpdateGameUserPayloadGameUserEdgeArgs = {
  orderBy?: InputMaybe<Array<GameUsersOrderBy>>;
};

/** All input for the `updateGuestById` mutation. */
export type UpdateGuestByIdInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** An object where the defined keys will be set on the `Guest` being updated. */
  guestPatch: GuestPatch;
  id: Scalars['BigInt'];
};

/** All input for the `updateGuest` mutation. */
export type UpdateGuestInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** An object where the defined keys will be set on the `Guest` being updated. */
  guestPatch: GuestPatch;
  /** The globally unique `ID` which will identify a single `Guest` to be updated. */
  nodeId: Scalars['ID'];
};

/** The output of our update `Guest` mutation. */
export type UpdateGuestPayload = {
  __typename?: 'UpdateGuestPayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** The `Guest` that was updated by this mutation. */
  guest?: Maybe<Guest>;
  /** An edge for our `Guest`. May be used by Relay 1. */
  guestEdge?: Maybe<GuestsEdge>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our update `Guest` mutation. */
export type UpdateGuestPayloadGuestEdgeArgs = {
  orderBy?: InputMaybe<Array<GuestsOrderBy>>;
};

/** All input for the `updateHardRemovalCardByCardId` mutation. */
export type UpdateHardRemovalCardByCardIdInput = {
  cardId: Scalars['String'];
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** An object where the defined keys will be set on the `HardRemovalCard` being updated. */
  hardRemovalCardPatch: HardRemovalCardPatch;
};

/** All input for the `updateHardRemovalCard` mutation. */
export type UpdateHardRemovalCardInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** An object where the defined keys will be set on the `HardRemovalCard` being updated. */
  hardRemovalCardPatch: HardRemovalCardPatch;
  /** The globally unique `ID` which will identify a single `HardRemovalCard` to be updated. */
  nodeId: Scalars['ID'];
};

/** The output of our update `HardRemovalCard` mutation. */
export type UpdateHardRemovalCardPayload = {
  __typename?: 'UpdateHardRemovalCardPayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** The `HardRemovalCard` that was updated by this mutation. */
  hardRemovalCard?: Maybe<HardRemovalCard>;
  /** An edge for our `HardRemovalCard`. May be used by Relay 1. */
  hardRemovalCardEdge?: Maybe<HardRemovalCardsEdge>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our update `HardRemovalCard` mutation. */
export type UpdateHardRemovalCardPayloadHardRemovalCardEdgeArgs = {
  orderBy?: InputMaybe<Array<HardRemovalCardsOrderBy>>;
};

/** All input for the `updateMatchmakingQueueById` mutation. */
export type UpdateMatchmakingQueueByIdInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  id: Scalars['String'];
  /** An object where the defined keys will be set on the `MatchmakingQueue` being updated. */
  matchmakingQueuePatch: MatchmakingQueuePatch;
};

/** All input for the `updateMatchmakingQueue` mutation. */
export type UpdateMatchmakingQueueInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** An object where the defined keys will be set on the `MatchmakingQueue` being updated. */
  matchmakingQueuePatch: MatchmakingQueuePatch;
  /** The globally unique `ID` which will identify a single `MatchmakingQueue` to be updated. */
  nodeId: Scalars['ID'];
};

/** The output of our update `MatchmakingQueue` mutation. */
export type UpdateMatchmakingQueuePayload = {
  __typename?: 'UpdateMatchmakingQueuePayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** The `MatchmakingQueue` that was updated by this mutation. */
  matchmakingQueue?: Maybe<MatchmakingQueue>;
  /** An edge for our `MatchmakingQueue`. May be used by Relay 1. */
  matchmakingQueueEdge?: Maybe<MatchmakingQueuesEdge>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our update `MatchmakingQueue` mutation. */
export type UpdateMatchmakingQueuePayloadMatchmakingQueueEdgeArgs = {
  orderBy?: InputMaybe<Array<MatchmakingQueuesOrderBy>>;
};

/** All input for the `updateMatchmakingTicketByUserId` mutation. */
export type UpdateMatchmakingTicketByUserIdInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** An object where the defined keys will be set on the `MatchmakingTicket` being updated. */
  matchmakingTicketPatch: MatchmakingTicketPatch;
  userId: Scalars['String'];
};

/** All input for the `updateMatchmakingTicket` mutation. */
export type UpdateMatchmakingTicketInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** An object where the defined keys will be set on the `MatchmakingTicket` being updated. */
  matchmakingTicketPatch: MatchmakingTicketPatch;
  /** The globally unique `ID` which will identify a single `MatchmakingTicket` to be updated. */
  nodeId: Scalars['ID'];
};

/** The output of our update `MatchmakingTicket` mutation. */
export type UpdateMatchmakingTicketPayload = {
  __typename?: 'UpdateMatchmakingTicketPayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Reads a single `Deck` that is related to this `MatchmakingTicket`. */
  deckByBotDeckId?: Maybe<Deck>;
  /** Reads a single `Deck` that is related to this `MatchmakingTicket`. */
  deckByDeckId?: Maybe<Deck>;
  /** Reads a single `MatchmakingQueue` that is related to this `MatchmakingTicket`. */
  matchmakingQueueByQueueId?: Maybe<MatchmakingQueue>;
  /** The `MatchmakingTicket` that was updated by this mutation. */
  matchmakingTicket?: Maybe<MatchmakingTicket>;
  /** An edge for our `MatchmakingTicket`. May be used by Relay 1. */
  matchmakingTicketEdge?: Maybe<MatchmakingTicketsEdge>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our update `MatchmakingTicket` mutation. */
export type UpdateMatchmakingTicketPayloadMatchmakingTicketEdgeArgs = {
  orderBy?: InputMaybe<Array<MatchmakingTicketsOrderBy>>;
};

/** All input for the `updateUserEntityAddonById` mutation. */
export type UpdateUserEntityAddonByIdInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  id: Scalars['String'];
  /** An object where the defined keys will be set on the `UserEntityAddon` being updated. */
  userEntityAddonPatch: UserEntityAddonPatch;
};

/** All input for the `updateUserEntityAddon` mutation. */
export type UpdateUserEntityAddonInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The globally unique `ID` which will identify a single `UserEntityAddon` to be updated. */
  nodeId: Scalars['ID'];
  /** An object where the defined keys will be set on the `UserEntityAddon` being updated. */
  userEntityAddonPatch: UserEntityAddonPatch;
};

/** The output of our update `UserEntityAddon` mutation. */
export type UpdateUserEntityAddonPayload = {
  __typename?: 'UpdateUserEntityAddonPayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
  /** The `UserEntityAddon` that was updated by this mutation. */
  userEntityAddon?: Maybe<UserEntityAddon>;
  /** An edge for our `UserEntityAddon`. May be used by Relay 1. */
  userEntityAddonEdge?: Maybe<UserEntityAddonsEdge>;
};


/** The output of our update `UserEntityAddon` mutation. */
export type UpdateUserEntityAddonPayloadUserEntityAddonEdgeArgs = {
  orderBy?: InputMaybe<Array<UserEntityAddonsOrderBy>>;
};

/** All input for the upsert `BannedDraftCard` mutation. */
export type UpsertBannedDraftCardInput = {
  /** The `BannedDraftCard` to be upserted by this mutation. */
  bannedDraftCard: BannedDraftCardInput;
  /** An arbitrary string value with no semantic meaning. Will be included in the payload verbatim. May be used to track mutations by the client. */
  clientMutationId?: InputMaybe<Scalars['String']>;
};

/** The output of our upsert `BannedDraftCard` mutation. */
export type UpsertBannedDraftCardPayload = {
  __typename?: 'UpsertBannedDraftCardPayload';
  /** The `BannedDraftCard` that was upserted by this mutation. */
  bannedDraftCard?: Maybe<BannedDraftCard>;
  /** An edge for our `BannedDraftCard`. May be used by Relay 1. */
  bannedDraftCardEdge?: Maybe<BannedDraftCardsEdge>;
  /** The exact same `clientMutationId` that was provided in the mutation input, unchanged and unused. May be used by a client to track mutations. */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our upsert `BannedDraftCard` mutation. */
export type UpsertBannedDraftCardPayloadBannedDraftCardEdgeArgs = {
  orderBy?: InputMaybe<Array<BannedDraftCardsOrderBy>>;
};

/** Where conditions for the upsert `BannedDraftCard` mutation. */
export type UpsertBannedDraftCardWhere = {
  cardId?: InputMaybe<Scalars['String']>;
};

/** All input for the upsert `BotUser` mutation. */
export type UpsertBotUserInput = {
  /** The `BotUser` to be upserted by this mutation. */
  botUser: BotUserInput;
  /** An arbitrary string value with no semantic meaning. Will be included in the payload verbatim. May be used to track mutations by the client. */
  clientMutationId?: InputMaybe<Scalars['String']>;
};

/** The output of our upsert `BotUser` mutation. */
export type UpsertBotUserPayload = {
  __typename?: 'UpsertBotUserPayload';
  /** The `BotUser` that was upserted by this mutation. */
  botUser?: Maybe<BotUser>;
  /** An edge for our `BotUser`. May be used by Relay 1. */
  botUserEdge?: Maybe<BotUsersEdge>;
  /** The exact same `clientMutationId` that was provided in the mutation input, unchanged and unused. May be used by a client to track mutations. */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our upsert `BotUser` mutation. */
export type UpsertBotUserPayloadBotUserEdgeArgs = {
  orderBy?: InputMaybe<Array<BotUsersOrderBy>>;
};

/** Where conditions for the upsert `BotUser` mutation. */
export type UpsertBotUserWhere = {
  id?: InputMaybe<Scalars['String']>;
};

/** All input for the upsert `CardsInDeck` mutation. */
export type UpsertCardsInDeckInput = {
  /** The `CardsInDeck` to be upserted by this mutation. */
  cardsInDeck: CardsInDeckInput;
  /** An arbitrary string value with no semantic meaning. Will be included in the payload verbatim. May be used to track mutations by the client. */
  clientMutationId?: InputMaybe<Scalars['String']>;
};

/** The output of our upsert `CardsInDeck` mutation. */
export type UpsertCardsInDeckPayload = {
  __typename?: 'UpsertCardsInDeckPayload';
  /** The `CardsInDeck` that was upserted by this mutation. */
  cardsInDeck?: Maybe<CardsInDeck>;
  /** An edge for our `CardsInDeck`. May be used by Relay 1. */
  cardsInDeckEdge?: Maybe<CardsInDecksEdge>;
  /** The exact same `clientMutationId` that was provided in the mutation input, unchanged and unused. May be used by a client to track mutations. */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Reads a single `Deck` that is related to this `CardsInDeck`. */
  deckByDeckId?: Maybe<Deck>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our upsert `CardsInDeck` mutation. */
export type UpsertCardsInDeckPayloadCardsInDeckEdgeArgs = {
  orderBy?: InputMaybe<Array<CardsInDecksOrderBy>>;
};

/** Where conditions for the upsert `CardsInDeck` mutation. */
export type UpsertCardsInDeckWhere = {
  id?: InputMaybe<Scalars['BigInt']>;
};

/** All input for the upsert `Deck` mutation. */
export type UpsertDeckInput = {
  /** An arbitrary string value with no semantic meaning. Will be included in the payload verbatim. May be used to track mutations by the client. */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The `Deck` to be upserted by this mutation. */
  deck: DeckInput;
};

/** The output of our upsert `Deck` mutation. */
export type UpsertDeckPayload = {
  __typename?: 'UpsertDeckPayload';
  /** The exact same `clientMutationId` that was provided in the mutation input, unchanged and unused. May be used by a client to track mutations. */
  clientMutationId?: Maybe<Scalars['String']>;
  /** The `Deck` that was upserted by this mutation. */
  deck?: Maybe<Deck>;
  /** An edge for our `Deck`. May be used by Relay 1. */
  deckEdge?: Maybe<DecksEdge>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our upsert `Deck` mutation. */
export type UpsertDeckPayloadDeckEdgeArgs = {
  orderBy?: InputMaybe<Array<DecksOrderBy>>;
};

/** All input for the upsert `DeckPlayerAttributeTuple` mutation. */
export type UpsertDeckPlayerAttributeTupleInput = {
  /** An arbitrary string value with no semantic meaning. Will be included in the payload verbatim. May be used to track mutations by the client. */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The `DeckPlayerAttributeTuple` to be upserted by this mutation. */
  deckPlayerAttributeTuple: DeckPlayerAttributeTupleInput;
};

/** The output of our upsert `DeckPlayerAttributeTuple` mutation. */
export type UpsertDeckPlayerAttributeTuplePayload = {
  __typename?: 'UpsertDeckPlayerAttributeTuplePayload';
  /** The exact same `clientMutationId` that was provided in the mutation input, unchanged and unused. May be used by a client to track mutations. */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Reads a single `Deck` that is related to this `DeckPlayerAttributeTuple`. */
  deckByDeckId?: Maybe<Deck>;
  /** The `DeckPlayerAttributeTuple` that was upserted by this mutation. */
  deckPlayerAttributeTuple?: Maybe<DeckPlayerAttributeTuple>;
  /** An edge for our `DeckPlayerAttributeTuple`. May be used by Relay 1. */
  deckPlayerAttributeTupleEdge?: Maybe<DeckPlayerAttributeTuplesEdge>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our upsert `DeckPlayerAttributeTuple` mutation. */
export type UpsertDeckPlayerAttributeTuplePayloadDeckPlayerAttributeTupleEdgeArgs = {
  orderBy?: InputMaybe<Array<DeckPlayerAttributeTuplesOrderBy>>;
};

/** Where conditions for the upsert `DeckPlayerAttributeTuple` mutation. */
export type UpsertDeckPlayerAttributeTupleWhere = {
  id?: InputMaybe<Scalars['BigInt']>;
};

/** All input for the upsert `DeckShare` mutation. */
export type UpsertDeckShareInput = {
  /** An arbitrary string value with no semantic meaning. Will be included in the payload verbatim. May be used to track mutations by the client. */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The `DeckShare` to be upserted by this mutation. */
  deckShare: DeckShareInput;
};

/** The output of our upsert `DeckShare` mutation. */
export type UpsertDeckSharePayload = {
  __typename?: 'UpsertDeckSharePayload';
  /** The exact same `clientMutationId` that was provided in the mutation input, unchanged and unused. May be used by a client to track mutations. */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Reads a single `Deck` that is related to this `DeckShare`. */
  deckByDeckId?: Maybe<Deck>;
  /** The `DeckShare` that was upserted by this mutation. */
  deckShare?: Maybe<DeckShare>;
  /** An edge for our `DeckShare`. May be used by Relay 1. */
  deckShareEdge?: Maybe<DeckSharesEdge>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our upsert `DeckShare` mutation. */
export type UpsertDeckSharePayloadDeckShareEdgeArgs = {
  orderBy?: InputMaybe<Array<DeckSharesOrderBy>>;
};

/** Where conditions for the upsert `DeckShare` mutation. */
export type UpsertDeckShareWhere = {
  deckId?: InputMaybe<Scalars['String']>;
  shareRecipientId?: InputMaybe<Scalars['String']>;
};

/** Where conditions for the upsert `Deck` mutation. */
export type UpsertDeckWhere = {
  id?: InputMaybe<Scalars['String']>;
};

/** All input for the upsert `Friend` mutation. */
export type UpsertFriendInput = {
  /** An arbitrary string value with no semantic meaning. Will be included in the payload verbatim. May be used to track mutations by the client. */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The `Friend` to be upserted by this mutation. */
  friend: FriendInput;
};

/** The output of our upsert `Friend` mutation. */
export type UpsertFriendPayload = {
  __typename?: 'UpsertFriendPayload';
  /** The exact same `clientMutationId` that was provided in the mutation input, unchanged and unused. May be used by a client to track mutations. */
  clientMutationId?: Maybe<Scalars['String']>;
  /** The `Friend` that was upserted by this mutation. */
  friend?: Maybe<Friend>;
  /** An edge for our `Friend`. May be used by Relay 1. */
  friendEdge?: Maybe<FriendsEdge>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our upsert `Friend` mutation. */
export type UpsertFriendPayloadFriendEdgeArgs = {
  orderBy?: InputMaybe<Array<FriendsOrderBy>>;
};

/** Where conditions for the upsert `Friend` mutation. */
export type UpsertFriendWhere = {
  friend?: InputMaybe<Scalars['String']>;
  id?: InputMaybe<Scalars['String']>;
};

/** All input for the upsert `Game` mutation. */
export type UpsertGameInput = {
  /** An arbitrary string value with no semantic meaning. Will be included in the payload verbatim. May be used to track mutations by the client. */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The `Game` to be upserted by this mutation. */
  game: GameInput;
};

/** The output of our upsert `Game` mutation. */
export type UpsertGamePayload = {
  __typename?: 'UpsertGamePayload';
  /** The exact same `clientMutationId` that was provided in the mutation input, unchanged and unused. May be used by a client to track mutations. */
  clientMutationId?: Maybe<Scalars['String']>;
  /** The `Game` that was upserted by this mutation. */
  game?: Maybe<Game>;
  /** An edge for our `Game`. May be used by Relay 1. */
  gameEdge?: Maybe<GamesEdge>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our upsert `Game` mutation. */
export type UpsertGamePayloadGameEdgeArgs = {
  orderBy?: InputMaybe<Array<GamesOrderBy>>;
};

/** All input for the upsert `GameUser` mutation. */
export type UpsertGameUserInput = {
  /** An arbitrary string value with no semantic meaning. Will be included in the payload verbatim. May be used to track mutations by the client. */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The `GameUser` to be upserted by this mutation. */
  gameUser: GameUserInput;
};

/** The output of our upsert `GameUser` mutation. */
export type UpsertGameUserPayload = {
  __typename?: 'UpsertGameUserPayload';
  /** The exact same `clientMutationId` that was provided in the mutation input, unchanged and unused. May be used by a client to track mutations. */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Reads a single `Deck` that is related to this `GameUser`. */
  deckByDeckId?: Maybe<Deck>;
  /** Reads a single `Game` that is related to this `GameUser`. */
  gameByGameId?: Maybe<Game>;
  /** The `GameUser` that was upserted by this mutation. */
  gameUser?: Maybe<GameUser>;
  /** An edge for our `GameUser`. May be used by Relay 1. */
  gameUserEdge?: Maybe<GameUsersEdge>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our upsert `GameUser` mutation. */
export type UpsertGameUserPayloadGameUserEdgeArgs = {
  orderBy?: InputMaybe<Array<GameUsersOrderBy>>;
};

/** Where conditions for the upsert `GameUser` mutation. */
export type UpsertGameUserWhere = {
  gameId?: InputMaybe<Scalars['BigInt']>;
  userId?: InputMaybe<Scalars['String']>;
};

/** Where conditions for the upsert `Game` mutation. */
export type UpsertGameWhere = {
  id?: InputMaybe<Scalars['BigInt']>;
};

/** All input for the upsert `Guest` mutation. */
export type UpsertGuestInput = {
  /** An arbitrary string value with no semantic meaning. Will be included in the payload verbatim. May be used to track mutations by the client. */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The `Guest` to be upserted by this mutation. */
  guest: GuestInput;
};

/** The output of our upsert `Guest` mutation. */
export type UpsertGuestPayload = {
  __typename?: 'UpsertGuestPayload';
  /** The exact same `clientMutationId` that was provided in the mutation input, unchanged and unused. May be used by a client to track mutations. */
  clientMutationId?: Maybe<Scalars['String']>;
  /** The `Guest` that was upserted by this mutation. */
  guest?: Maybe<Guest>;
  /** An edge for our `Guest`. May be used by Relay 1. */
  guestEdge?: Maybe<GuestsEdge>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our upsert `Guest` mutation. */
export type UpsertGuestPayloadGuestEdgeArgs = {
  orderBy?: InputMaybe<Array<GuestsOrderBy>>;
};

/** Where conditions for the upsert `Guest` mutation. */
export type UpsertGuestWhere = {
  id?: InputMaybe<Scalars['BigInt']>;
};

/** All input for the upsert `HardRemovalCard` mutation. */
export type UpsertHardRemovalCardInput = {
  /** An arbitrary string value with no semantic meaning. Will be included in the payload verbatim. May be used to track mutations by the client. */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The `HardRemovalCard` to be upserted by this mutation. */
  hardRemovalCard: HardRemovalCardInput;
};

/** The output of our upsert `HardRemovalCard` mutation. */
export type UpsertHardRemovalCardPayload = {
  __typename?: 'UpsertHardRemovalCardPayload';
  /** The exact same `clientMutationId` that was provided in the mutation input, unchanged and unused. May be used by a client to track mutations. */
  clientMutationId?: Maybe<Scalars['String']>;
  /** The `HardRemovalCard` that was upserted by this mutation. */
  hardRemovalCard?: Maybe<HardRemovalCard>;
  /** An edge for our `HardRemovalCard`. May be used by Relay 1. */
  hardRemovalCardEdge?: Maybe<HardRemovalCardsEdge>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our upsert `HardRemovalCard` mutation. */
export type UpsertHardRemovalCardPayloadHardRemovalCardEdgeArgs = {
  orderBy?: InputMaybe<Array<HardRemovalCardsOrderBy>>;
};

/** Where conditions for the upsert `HardRemovalCard` mutation. */
export type UpsertHardRemovalCardWhere = {
  cardId?: InputMaybe<Scalars['String']>;
};

/** All input for the upsert `MatchmakingQueue` mutation. */
export type UpsertMatchmakingQueueInput = {
  /** An arbitrary string value with no semantic meaning. Will be included in the payload verbatim. May be used to track mutations by the client. */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The `MatchmakingQueue` to be upserted by this mutation. */
  matchmakingQueue: MatchmakingQueueInput;
};

/** The output of our upsert `MatchmakingQueue` mutation. */
export type UpsertMatchmakingQueuePayload = {
  __typename?: 'UpsertMatchmakingQueuePayload';
  /** The exact same `clientMutationId` that was provided in the mutation input, unchanged and unused. May be used by a client to track mutations. */
  clientMutationId?: Maybe<Scalars['String']>;
  /** The `MatchmakingQueue` that was upserted by this mutation. */
  matchmakingQueue?: Maybe<MatchmakingQueue>;
  /** An edge for our `MatchmakingQueue`. May be used by Relay 1. */
  matchmakingQueueEdge?: Maybe<MatchmakingQueuesEdge>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our upsert `MatchmakingQueue` mutation. */
export type UpsertMatchmakingQueuePayloadMatchmakingQueueEdgeArgs = {
  orderBy?: InputMaybe<Array<MatchmakingQueuesOrderBy>>;
};

/** Where conditions for the upsert `MatchmakingQueue` mutation. */
export type UpsertMatchmakingQueueWhere = {
  id?: InputMaybe<Scalars['String']>;
};

/** All input for the upsert `MatchmakingTicket` mutation. */
export type UpsertMatchmakingTicketInput = {
  /** An arbitrary string value with no semantic meaning. Will be included in the payload verbatim. May be used to track mutations by the client. */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The `MatchmakingTicket` to be upserted by this mutation. */
  matchmakingTicket: MatchmakingTicketInput;
};

/** The output of our upsert `MatchmakingTicket` mutation. */
export type UpsertMatchmakingTicketPayload = {
  __typename?: 'UpsertMatchmakingTicketPayload';
  /** The exact same `clientMutationId` that was provided in the mutation input, unchanged and unused. May be used by a client to track mutations. */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Reads a single `Deck` that is related to this `MatchmakingTicket`. */
  deckByBotDeckId?: Maybe<Deck>;
  /** Reads a single `Deck` that is related to this `MatchmakingTicket`. */
  deckByDeckId?: Maybe<Deck>;
  /** Reads a single `MatchmakingQueue` that is related to this `MatchmakingTicket`. */
  matchmakingQueueByQueueId?: Maybe<MatchmakingQueue>;
  /** The `MatchmakingTicket` that was upserted by this mutation. */
  matchmakingTicket?: Maybe<MatchmakingTicket>;
  /** An edge for our `MatchmakingTicket`. May be used by Relay 1. */
  matchmakingTicketEdge?: Maybe<MatchmakingTicketsEdge>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our upsert `MatchmakingTicket` mutation. */
export type UpsertMatchmakingTicketPayloadMatchmakingTicketEdgeArgs = {
  orderBy?: InputMaybe<Array<MatchmakingTicketsOrderBy>>;
};

/** Where conditions for the upsert `MatchmakingTicket` mutation. */
export type UpsertMatchmakingTicketWhere = {
  userId?: InputMaybe<Scalars['String']>;
};

/** All input for the upsert `UserEntityAddon` mutation. */
export type UpsertUserEntityAddonInput = {
  /** An arbitrary string value with no semantic meaning. Will be included in the payload verbatim. May be used to track mutations by the client. */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The `UserEntityAddon` to be upserted by this mutation. */
  userEntityAddon: UserEntityAddonInput;
};

/** The output of our upsert `UserEntityAddon` mutation. */
export type UpsertUserEntityAddonPayload = {
  __typename?: 'UpsertUserEntityAddonPayload';
  /** The exact same `clientMutationId` that was provided in the mutation input, unchanged and unused. May be used by a client to track mutations. */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
  /** The `UserEntityAddon` that was upserted by this mutation. */
  userEntityAddon?: Maybe<UserEntityAddon>;
  /** An edge for our `UserEntityAddon`. May be used by Relay 1. */
  userEntityAddonEdge?: Maybe<UserEntityAddonsEdge>;
};


/** The output of our upsert `UserEntityAddon` mutation. */
export type UpsertUserEntityAddonPayloadUserEntityAddonEdgeArgs = {
  orderBy?: InputMaybe<Array<UserEntityAddonsOrderBy>>;
};

/** Where conditions for the upsert `UserEntityAddon` mutation. */
export type UpsertUserEntityAddonWhere = {
  id?: InputMaybe<Scalars['String']>;
};

export type UserEntityAddon = Node & {
  __typename?: 'UserEntityAddon';
  id: Scalars['String'];
  migrated?: Maybe<Scalars['Boolean']>;
  /** A globally unique identifier. Can be used in various places throughout the system to identify this single value. */
  nodeId: Scalars['ID'];
  privacyToken?: Maybe<Scalars['String']>;
  showPremadeDecks?: Maybe<Scalars['Boolean']>;
};

/**
 * A condition to be used against `UserEntityAddon` object types. All fields are
 * tested for equality and combined with a logical ‘and.’
 */
export type UserEntityAddonCondition = {
  /** Checks for equality with the object’s `id` field. */
  id?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `migrated` field. */
  migrated?: InputMaybe<Scalars['Boolean']>;
  /** Checks for equality with the object’s `privacyToken` field. */
  privacyToken?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `showPremadeDecks` field. */
  showPremadeDecks?: InputMaybe<Scalars['Boolean']>;
};

/** A filter to be used against `UserEntityAddon` object types. All fields are combined with a logical ‘and.’ */
export type UserEntityAddonFilter = {
  /** Checks for all expressions in this list. */
  and?: InputMaybe<Array<UserEntityAddonFilter>>;
  /** Filter by the object’s `id` field. */
  id?: InputMaybe<StringFilter>;
  /** Filter by the object’s `migrated` field. */
  migrated?: InputMaybe<BooleanFilter>;
  /** Negates the expression. */
  not?: InputMaybe<UserEntityAddonFilter>;
  /** Checks for any expressions in this list. */
  or?: InputMaybe<Array<UserEntityAddonFilter>>;
  /** Filter by the object’s `privacyToken` field. */
  privacyToken?: InputMaybe<StringFilter>;
  /** Filter by the object’s `showPremadeDecks` field. */
  showPremadeDecks?: InputMaybe<BooleanFilter>;
};

/** An input for mutations affecting `UserEntityAddon` */
export type UserEntityAddonInput = {
  id: Scalars['String'];
  migrated?: InputMaybe<Scalars['Boolean']>;
  privacyToken?: InputMaybe<Scalars['String']>;
  showPremadeDecks?: InputMaybe<Scalars['Boolean']>;
};

/** Represents an update to a `UserEntityAddon`. Fields that are set will be updated. */
export type UserEntityAddonPatch = {
  id?: InputMaybe<Scalars['String']>;
  migrated?: InputMaybe<Scalars['Boolean']>;
  privacyToken?: InputMaybe<Scalars['String']>;
  showPremadeDecks?: InputMaybe<Scalars['Boolean']>;
};

/** A connection to a list of `UserEntityAddon` values. */
export type UserEntityAddonsConnection = {
  __typename?: 'UserEntityAddonsConnection';
  /** A list of edges which contains the `UserEntityAddon` and cursor to aid in pagination. */
  edges: Array<UserEntityAddonsEdge>;
  /** A list of `UserEntityAddon` objects. */
  nodes: Array<Maybe<UserEntityAddon>>;
  /** Information to aid in pagination. */
  pageInfo: PageInfo;
  /** The count of *all* `UserEntityAddon` you could get from the connection. */
  totalCount: Scalars['Int'];
};

/** A `UserEntityAddon` edge in the connection. */
export type UserEntityAddonsEdge = {
  __typename?: 'UserEntityAddonsEdge';
  /** A cursor for use in pagination. */
  cursor?: Maybe<Scalars['Cursor']>;
  /** The `UserEntityAddon` at the end of the edge. */
  node?: Maybe<UserEntityAddon>;
};

/** Methods to use when ordering `UserEntityAddon`. */
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
  ArchiveCardInput: ResolverTypeWrapper<Partial<ArchiveCardInput>>;
  ArchiveCardPayload: ResolverTypeWrapper<Partial<ArchiveCardPayload>>;
  BannedDraftCard: ResolverTypeWrapper<Partial<BannedDraftCard>>;
  BannedDraftCardCondition: ResolverTypeWrapper<Partial<BannedDraftCardCondition>>;
  BannedDraftCardFilter: ResolverTypeWrapper<Partial<BannedDraftCardFilter>>;
  BannedDraftCardInput: ResolverTypeWrapper<Partial<BannedDraftCardInput>>;
  BannedDraftCardPatch: ResolverTypeWrapper<Partial<BannedDraftCardPatch>>;
  BannedDraftCardsConnection: ResolverTypeWrapper<Partial<BannedDraftCardsConnection>>;
  BannedDraftCardsEdge: ResolverTypeWrapper<Partial<BannedDraftCardsEdge>>;
  BannedDraftCardsOrderBy: ResolverTypeWrapper<Partial<BannedDraftCardsOrderBy>>;
  BigInt: ResolverTypeWrapper<Partial<Scalars['BigInt']>>;
  BigIntFilter: ResolverTypeWrapper<Partial<BigIntFilter>>;
  Boolean: ResolverTypeWrapper<Partial<Scalars['Boolean']>>;
  BooleanFilter: ResolverTypeWrapper<Partial<BooleanFilter>>;
  BotUser: ResolverTypeWrapper<Partial<BotUser>>;
  BotUserCondition: ResolverTypeWrapper<Partial<BotUserCondition>>;
  BotUserFilter: ResolverTypeWrapper<Partial<BotUserFilter>>;
  BotUserInput: ResolverTypeWrapper<Partial<BotUserInput>>;
  BotUserPatch: ResolverTypeWrapper<Partial<BotUserPatch>>;
  BotUsersConnection: ResolverTypeWrapper<Partial<BotUsersConnection>>;
  BotUsersEdge: ResolverTypeWrapper<Partial<BotUsersEdge>>;
  BotUsersOrderBy: ResolverTypeWrapper<Partial<BotUsersOrderBy>>;
  Card: ResolverTypeWrapper<Partial<Card>>;
  CardCatalogueFormatsInput: ResolverTypeWrapper<Partial<CardCatalogueFormatsInput>>;
  CardCatalogueFormatsPayload: ResolverTypeWrapper<Partial<CardCatalogueFormatsPayload>>;
  CardCatalogueGetBannedDraftCardsInput: ResolverTypeWrapper<Partial<CardCatalogueGetBannedDraftCardsInput>>;
  CardCatalogueGetBannedDraftCardsPayload: ResolverTypeWrapper<Partial<CardCatalogueGetBannedDraftCardsPayload>>;
  CardCatalogueGetBaseClassesInput: ResolverTypeWrapper<Partial<CardCatalogueGetBaseClassesInput>>;
  CardCatalogueGetBaseClassesPayload: ResolverTypeWrapper<Partial<CardCatalogueGetBaseClassesPayload>>;
  CardCatalogueGetCardByIdInput: ResolverTypeWrapper<Partial<CardCatalogueGetCardByIdInput>>;
  CardCatalogueGetCardByIdPayload: ResolverTypeWrapper<Partial<CardCatalogueGetCardByIdPayload>>;
  CardCatalogueGetCardByNameAndClassInput: ResolverTypeWrapper<Partial<CardCatalogueGetCardByNameAndClassInput>>;
  CardCatalogueGetCardByNameAndClassPayload: ResolverTypeWrapper<Partial<CardCatalogueGetCardByNameAndClassPayload>>;
  CardCatalogueGetCardByNameInput: ResolverTypeWrapper<Partial<CardCatalogueGetCardByNameInput>>;
  CardCatalogueGetCardByNamePayload: ResolverTypeWrapper<Partial<CardCatalogueGetCardByNamePayload>>;
  CardCatalogueGetClassCardsInput: ResolverTypeWrapper<Partial<CardCatalogueGetClassCardsInput>>;
  CardCatalogueGetClassCardsPayload: ResolverTypeWrapper<Partial<CardCatalogueGetClassCardsPayload>>;
  CardCatalogueGetFormatInput: ResolverTypeWrapper<Partial<CardCatalogueGetFormatInput>>;
  CardCatalogueGetFormatPayload: ResolverTypeWrapper<Partial<CardCatalogueGetFormatPayload>>;
  CardCatalogueGetHardRemovalCardsInput: ResolverTypeWrapper<Partial<CardCatalogueGetHardRemovalCardsInput>>;
  CardCatalogueGetHardRemovalCardsPayload: ResolverTypeWrapper<Partial<CardCatalogueGetHardRemovalCardsPayload>>;
  CardCatalogueGetHeroCardInput: ResolverTypeWrapper<Partial<CardCatalogueGetHeroCardInput>>;
  CardCatalogueGetHeroCardPayload: ResolverTypeWrapper<Partial<CardCatalogueGetHeroCardPayload>>;
  CardCatalogueQueryInput: ResolverTypeWrapper<Partial<CardCatalogueQueryInput>>;
  CardCatalogueQueryPayload: ResolverTypeWrapper<Partial<CardCatalogueQueryPayload>>;
  CardCondition: ResolverTypeWrapper<Partial<CardCondition>>;
  CardFilter: ResolverTypeWrapper<Partial<CardFilter>>;
  CardInput: ResolverTypeWrapper<Partial<CardInput>>;
  CardsConnection: ResolverTypeWrapper<Partial<CardsConnection>>;
  CardsEdge: ResolverTypeWrapper<Partial<CardsEdge>>;
  CardsInDeck: ResolverTypeWrapper<Partial<CardsInDeck>>;
  CardsInDeckCondition: ResolverTypeWrapper<Partial<CardsInDeckCondition>>;
  CardsInDeckFilter: ResolverTypeWrapper<Partial<CardsInDeckFilter>>;
  CardsInDeckInput: ResolverTypeWrapper<Partial<CardsInDeckInput>>;
  CardsInDeckPatch: ResolverTypeWrapper<Partial<CardsInDeckPatch>>;
  CardsInDecksConnection: ResolverTypeWrapper<Partial<CardsInDecksConnection>>;
  CardsInDecksEdge: ResolverTypeWrapper<Partial<CardsInDecksEdge>>;
  CardsInDecksOrderBy: ResolverTypeWrapper<Partial<CardsInDecksOrderBy>>;
  CardsOrderBy: ResolverTypeWrapper<Partial<CardsOrderBy>>;
  Class: ResolverTypeWrapper<Partial<Class>>;
  ClassCondition: ResolverTypeWrapper<Partial<ClassCondition>>;
  ClassFilter: ResolverTypeWrapper<Partial<ClassFilter>>;
  ClassesConnection: ResolverTypeWrapper<Partial<ClassesConnection>>;
  ClassesEdge: ResolverTypeWrapper<Partial<ClassesEdge>>;
  ClassesOrderBy: ResolverTypeWrapper<Partial<ClassesOrderBy>>;
  ClusteredGamesUpdateGameAndUsersInput: ResolverTypeWrapper<Partial<ClusteredGamesUpdateGameAndUsersInput>>;
  ClusteredGamesUpdateGameAndUsersPayload: ResolverTypeWrapper<Partial<ClusteredGamesUpdateGameAndUsersPayload>>;
  CollectionCard: ResolverTypeWrapper<Partial<CollectionCard>>;
  CollectionCardCondition: ResolverTypeWrapper<Partial<CollectionCardCondition>>;
  CollectionCardFilter: ResolverTypeWrapper<Partial<CollectionCardFilter>>;
  CollectionCardsConnection: ResolverTypeWrapper<Partial<CollectionCardsConnection>>;
  CollectionCardsEdge: ResolverTypeWrapper<Partial<CollectionCardsEdge>>;
  CollectionCardsOrderBy: ResolverTypeWrapper<Partial<CollectionCardsOrderBy>>;
  CreateBannedDraftCardInput: ResolverTypeWrapper<Partial<CreateBannedDraftCardInput>>;
  CreateBannedDraftCardPayload: ResolverTypeWrapper<Partial<CreateBannedDraftCardPayload>>;
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
  CreateDeckWithCardsInput: ResolverTypeWrapper<Partial<CreateDeckWithCardsInput>>;
  CreateDeckWithCardsPayload: ResolverTypeWrapper<Partial<CreateDeckWithCardsPayload>>;
  CreateFriendInput: ResolverTypeWrapper<Partial<CreateFriendInput>>;
  CreateFriendPayload: ResolverTypeWrapper<Partial<CreateFriendPayload>>;
  CreateGameInput: ResolverTypeWrapper<Partial<CreateGameInput>>;
  CreateGamePayload: ResolverTypeWrapper<Partial<CreateGamePayload>>;
  CreateGameUserInput: ResolverTypeWrapper<Partial<CreateGameUserInput>>;
  CreateGameUserPayload: ResolverTypeWrapper<Partial<CreateGameUserPayload>>;
  CreateGuestInput: ResolverTypeWrapper<Partial<CreateGuestInput>>;
  CreateGuestPayload: ResolverTypeWrapper<Partial<CreateGuestPayload>>;
  CreateHardRemovalCardInput: ResolverTypeWrapper<Partial<CreateHardRemovalCardInput>>;
  CreateHardRemovalCardPayload: ResolverTypeWrapper<Partial<CreateHardRemovalCardPayload>>;
  CreateMatchmakingQueueInput: ResolverTypeWrapper<Partial<CreateMatchmakingQueueInput>>;
  CreateMatchmakingQueuePayload: ResolverTypeWrapper<Partial<CreateMatchmakingQueuePayload>>;
  CreateMatchmakingTicketInput: ResolverTypeWrapper<Partial<CreateMatchmakingTicketInput>>;
  CreateMatchmakingTicketPayload: ResolverTypeWrapper<Partial<CreateMatchmakingTicketPayload>>;
  CreateUserEntityAddonInput: ResolverTypeWrapper<Partial<CreateUserEntityAddonInput>>;
  CreateUserEntityAddonPayload: ResolverTypeWrapper<Partial<CreateUserEntityAddonPayload>>;
  CurrentCard: ResolverTypeWrapper<Partial<CurrentCard>>;
  CurrentCardCondition: ResolverTypeWrapper<Partial<CurrentCardCondition>>;
  CurrentCardFilter: ResolverTypeWrapper<Partial<CurrentCardFilter>>;
  CurrentCardsConnection: ResolverTypeWrapper<Partial<CurrentCardsConnection>>;
  CurrentCardsEdge: ResolverTypeWrapper<Partial<CurrentCardsEdge>>;
  CurrentCardsOrderBy: ResolverTypeWrapper<Partial<CurrentCardsOrderBy>>;
  Cursor: ResolverTypeWrapper<Partial<Scalars['Cursor']>>;
  Datetime: ResolverTypeWrapper<Partial<Scalars['Datetime']>>;
  DatetimeFilter: ResolverTypeWrapper<Partial<DatetimeFilter>>;
  Deck: ResolverTypeWrapper<Partial<Deck>>;
  DeckCondition: ResolverTypeWrapper<Partial<DeckCondition>>;
  DeckFilter: ResolverTypeWrapper<Partial<DeckFilter>>;
  DeckInput: ResolverTypeWrapper<Partial<DeckInput>>;
  DeckPatch: ResolverTypeWrapper<Partial<DeckPatch>>;
  DeckPlayerAttributeTuple: ResolverTypeWrapper<Partial<DeckPlayerAttributeTuple>>;
  DeckPlayerAttributeTupleCondition: ResolverTypeWrapper<Partial<DeckPlayerAttributeTupleCondition>>;
  DeckPlayerAttributeTupleFilter: ResolverTypeWrapper<Partial<DeckPlayerAttributeTupleFilter>>;
  DeckPlayerAttributeTupleInput: ResolverTypeWrapper<Partial<DeckPlayerAttributeTupleInput>>;
  DeckPlayerAttributeTuplePatch: ResolverTypeWrapper<Partial<DeckPlayerAttributeTuplePatch>>;
  DeckPlayerAttributeTuplesConnection: ResolverTypeWrapper<Partial<DeckPlayerAttributeTuplesConnection>>;
  DeckPlayerAttributeTuplesEdge: ResolverTypeWrapper<Partial<DeckPlayerAttributeTuplesEdge>>;
  DeckPlayerAttributeTuplesOrderBy: ResolverTypeWrapper<Partial<DeckPlayerAttributeTuplesOrderBy>>;
  DeckShare: ResolverTypeWrapper<Partial<DeckShare>>;
  DeckShareCondition: ResolverTypeWrapper<Partial<DeckShareCondition>>;
  DeckShareFilter: ResolverTypeWrapper<Partial<DeckShareFilter>>;
  DeckShareInput: ResolverTypeWrapper<Partial<DeckShareInput>>;
  DeckSharePatch: ResolverTypeWrapper<Partial<DeckSharePatch>>;
  DeckSharesConnection: ResolverTypeWrapper<Partial<DeckSharesConnection>>;
  DeckSharesEdge: ResolverTypeWrapper<Partial<DeckSharesEdge>>;
  DeckSharesOrderBy: ResolverTypeWrapper<Partial<DeckSharesOrderBy>>;
  DecksConnection: ResolverTypeWrapper<Partial<DecksConnection>>;
  DecksEdge: ResolverTypeWrapper<Partial<DecksEdge>>;
  DecksOrderBy: ResolverTypeWrapper<Partial<DecksOrderBy>>;
  DeleteBannedDraftCardByCardIdInput: ResolverTypeWrapper<Partial<DeleteBannedDraftCardByCardIdInput>>;
  DeleteBannedDraftCardInput: ResolverTypeWrapper<Partial<DeleteBannedDraftCardInput>>;
  DeleteBannedDraftCardPayload: ResolverTypeWrapper<Partial<DeleteBannedDraftCardPayload>>;
  DeleteBotUserByIdInput: ResolverTypeWrapper<Partial<DeleteBotUserByIdInput>>;
  DeleteBotUserInput: ResolverTypeWrapper<Partial<DeleteBotUserInput>>;
  DeleteBotUserPayload: ResolverTypeWrapper<Partial<DeleteBotUserPayload>>;
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
  DeleteHardRemovalCardByCardIdInput: ResolverTypeWrapper<Partial<DeleteHardRemovalCardByCardIdInput>>;
  DeleteHardRemovalCardInput: ResolverTypeWrapper<Partial<DeleteHardRemovalCardInput>>;
  DeleteHardRemovalCardPayload: ResolverTypeWrapper<Partial<DeleteHardRemovalCardPayload>>;
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
  FriendFilter: ResolverTypeWrapper<Partial<FriendFilter>>;
  FriendInput: ResolverTypeWrapper<Partial<FriendInput>>;
  FriendPatch: ResolverTypeWrapper<Partial<FriendPatch>>;
  FriendsConnection: ResolverTypeWrapper<Partial<FriendsConnection>>;
  FriendsEdge: ResolverTypeWrapper<Partial<FriendsEdge>>;
  FriendsOrderBy: ResolverTypeWrapper<Partial<FriendsOrderBy>>;
  Game: ResolverTypeWrapper<Partial<Game>>;
  GameCondition: ResolverTypeWrapper<Partial<GameCondition>>;
  GameFilter: ResolverTypeWrapper<Partial<GameFilter>>;
  GameInput: ResolverTypeWrapper<Partial<GameInput>>;
  GamePatch: ResolverTypeWrapper<Partial<GamePatch>>;
  GameStateEnum: ResolverTypeWrapper<Partial<GameStateEnum>>;
  GameStateEnumFilter: ResolverTypeWrapper<Partial<GameStateEnumFilter>>;
  GameUser: ResolverTypeWrapper<Partial<GameUser>>;
  GameUserCondition: ResolverTypeWrapper<Partial<GameUserCondition>>;
  GameUserFilter: ResolverTypeWrapper<Partial<GameUserFilter>>;
  GameUserInput: ResolverTypeWrapper<Partial<GameUserInput>>;
  GameUserPatch: ResolverTypeWrapper<Partial<GameUserPatch>>;
  GameUserVictoryEnum: ResolverTypeWrapper<Partial<GameUserVictoryEnum>>;
  GameUserVictoryEnumFilter: ResolverTypeWrapper<Partial<GameUserVictoryEnumFilter>>;
  GameUsersConnection: ResolverTypeWrapper<Partial<GameUsersConnection>>;
  GameUsersEdge: ResolverTypeWrapper<Partial<GameUsersEdge>>;
  GameUsersOrderBy: ResolverTypeWrapper<Partial<GameUsersOrderBy>>;
  GamesConnection: ResolverTypeWrapper<Partial<GamesConnection>>;
  GamesEdge: ResolverTypeWrapper<Partial<GamesEdge>>;
  GamesOrderBy: ResolverTypeWrapper<Partial<GamesOrderBy>>;
  GetClassesInput: ResolverTypeWrapper<Partial<GetClassesInput>>;
  GetClassesPayload: ResolverTypeWrapper<Partial<GetClassesPayload>>;
  GetClassesRecord: ResolverTypeWrapper<Partial<GetClassesRecord>>;
  GetCollectionCardsInput: ResolverTypeWrapper<Partial<GetCollectionCardsInput>>;
  GetCollectionCardsPayload: ResolverTypeWrapper<Partial<GetCollectionCardsPayload>>;
  GetCollectionCardsRecord: ResolverTypeWrapper<Partial<GetCollectionCardsRecord>>;
  Guest: ResolverTypeWrapper<Partial<Guest>>;
  GuestCondition: ResolverTypeWrapper<Partial<GuestCondition>>;
  GuestFilter: ResolverTypeWrapper<Partial<GuestFilter>>;
  GuestInput: ResolverTypeWrapper<Partial<GuestInput>>;
  GuestPatch: ResolverTypeWrapper<Partial<GuestPatch>>;
  GuestsConnection: ResolverTypeWrapper<Partial<GuestsConnection>>;
  GuestsEdge: ResolverTypeWrapper<Partial<GuestsEdge>>;
  GuestsOrderBy: ResolverTypeWrapper<Partial<GuestsOrderBy>>;
  HardRemovalCard: ResolverTypeWrapper<Partial<HardRemovalCard>>;
  HardRemovalCardCondition: ResolverTypeWrapper<Partial<HardRemovalCardCondition>>;
  HardRemovalCardFilter: ResolverTypeWrapper<Partial<HardRemovalCardFilter>>;
  HardRemovalCardInput: ResolverTypeWrapper<Partial<HardRemovalCardInput>>;
  HardRemovalCardPatch: ResolverTypeWrapper<Partial<HardRemovalCardPatch>>;
  HardRemovalCardsConnection: ResolverTypeWrapper<Partial<HardRemovalCardsConnection>>;
  HardRemovalCardsEdge: ResolverTypeWrapper<Partial<HardRemovalCardsEdge>>;
  HardRemovalCardsOrderBy: ResolverTypeWrapper<Partial<HardRemovalCardsOrderBy>>;
  ID: ResolverTypeWrapper<Partial<Scalars['ID']>>;
  ImageDef: ResolverTypeWrapper<Partial<ImageDef>>;
  IncludeArchivedOption: ResolverTypeWrapper<Partial<IncludeArchivedOption>>;
  Int: ResolverTypeWrapper<Partial<Scalars['Int']>>;
  IntFilter: ResolverTypeWrapper<Partial<IntFilter>>;
  JSON: ResolverTypeWrapper<Partial<Scalars['JSON']>>;
  JSONFilter: ResolverTypeWrapper<Partial<JsonFilter>>;
  MatchmakingQueue: ResolverTypeWrapper<Partial<MatchmakingQueue>>;
  MatchmakingQueueCondition: ResolverTypeWrapper<Partial<MatchmakingQueueCondition>>;
  MatchmakingQueueFilter: ResolverTypeWrapper<Partial<MatchmakingQueueFilter>>;
  MatchmakingQueueInput: ResolverTypeWrapper<Partial<MatchmakingQueueInput>>;
  MatchmakingQueuePatch: ResolverTypeWrapper<Partial<MatchmakingQueuePatch>>;
  MatchmakingQueuesConnection: ResolverTypeWrapper<Partial<MatchmakingQueuesConnection>>;
  MatchmakingQueuesEdge: ResolverTypeWrapper<Partial<MatchmakingQueuesEdge>>;
  MatchmakingQueuesOrderBy: ResolverTypeWrapper<Partial<MatchmakingQueuesOrderBy>>;
  MatchmakingTicket: ResolverTypeWrapper<Partial<MatchmakingTicket>>;
  MatchmakingTicketCondition: ResolverTypeWrapper<Partial<MatchmakingTicketCondition>>;
  MatchmakingTicketFilter: ResolverTypeWrapper<Partial<MatchmakingTicketFilter>>;
  MatchmakingTicketInput: ResolverTypeWrapper<Partial<MatchmakingTicketInput>>;
  MatchmakingTicketPatch: ResolverTypeWrapper<Partial<MatchmakingTicketPatch>>;
  MatchmakingTicketsConnection: ResolverTypeWrapper<Partial<MatchmakingTicketsConnection>>;
  MatchmakingTicketsEdge: ResolverTypeWrapper<Partial<MatchmakingTicketsEdge>>;
  MatchmakingTicketsOrderBy: ResolverTypeWrapper<Partial<MatchmakingTicketsOrderBy>>;
  Mutation: ResolverTypeWrapper<{}>;
  Node: ResolversTypes['BannedDraftCard'] | ResolversTypes['BotUser'] | ResolversTypes['CardsInDeck'] | ResolversTypes['Deck'] | ResolversTypes['DeckPlayerAttributeTuple'] | ResolversTypes['DeckShare'] | ResolversTypes['Friend'] | ResolversTypes['Game'] | ResolversTypes['GameUser'] | ResolversTypes['Guest'] | ResolversTypes['HardRemovalCard'] | ResolversTypes['MatchmakingQueue'] | ResolversTypes['MatchmakingTicket'] | ResolversTypes['Query'] | ResolversTypes['UserEntityAddon'];
  PageInfo: ResolverTypeWrapper<Partial<PageInfo>>;
  PublishCardInput: ResolverTypeWrapper<Partial<PublishCardInput>>;
  PublishCardPayload: ResolverTypeWrapper<Partial<PublishCardPayload>>;
  Query: ResolverTypeWrapper<{}>;
  SaveCardInput: ResolverTypeWrapper<Partial<SaveCardInput>>;
  SaveCardPayload: ResolverTypeWrapper<Partial<SaveCardPayload>>;
  SetCardsInDeckInput: ResolverTypeWrapper<Partial<SetCardsInDeckInput>>;
  SetCardsInDeckPayload: ResolverTypeWrapper<Partial<SetCardsInDeckPayload>>;
  String: ResolverTypeWrapper<Partial<Scalars['String']>>;
  StringFilter: ResolverTypeWrapper<Partial<StringFilter>>;
  UpdateBannedDraftCardByCardIdInput: ResolverTypeWrapper<Partial<UpdateBannedDraftCardByCardIdInput>>;
  UpdateBannedDraftCardInput: ResolverTypeWrapper<Partial<UpdateBannedDraftCardInput>>;
  UpdateBannedDraftCardPayload: ResolverTypeWrapper<Partial<UpdateBannedDraftCardPayload>>;
  UpdateBotUserByIdInput: ResolverTypeWrapper<Partial<UpdateBotUserByIdInput>>;
  UpdateBotUserInput: ResolverTypeWrapper<Partial<UpdateBotUserInput>>;
  UpdateBotUserPayload: ResolverTypeWrapper<Partial<UpdateBotUserPayload>>;
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
  UpdateHardRemovalCardByCardIdInput: ResolverTypeWrapper<Partial<UpdateHardRemovalCardByCardIdInput>>;
  UpdateHardRemovalCardInput: ResolverTypeWrapper<Partial<UpdateHardRemovalCardInput>>;
  UpdateHardRemovalCardPayload: ResolverTypeWrapper<Partial<UpdateHardRemovalCardPayload>>;
  UpdateMatchmakingQueueByIdInput: ResolverTypeWrapper<Partial<UpdateMatchmakingQueueByIdInput>>;
  UpdateMatchmakingQueueInput: ResolverTypeWrapper<Partial<UpdateMatchmakingQueueInput>>;
  UpdateMatchmakingQueuePayload: ResolverTypeWrapper<Partial<UpdateMatchmakingQueuePayload>>;
  UpdateMatchmakingTicketByUserIdInput: ResolverTypeWrapper<Partial<UpdateMatchmakingTicketByUserIdInput>>;
  UpdateMatchmakingTicketInput: ResolverTypeWrapper<Partial<UpdateMatchmakingTicketInput>>;
  UpdateMatchmakingTicketPayload: ResolverTypeWrapper<Partial<UpdateMatchmakingTicketPayload>>;
  UpdateUserEntityAddonByIdInput: ResolverTypeWrapper<Partial<UpdateUserEntityAddonByIdInput>>;
  UpdateUserEntityAddonInput: ResolverTypeWrapper<Partial<UpdateUserEntityAddonInput>>;
  UpdateUserEntityAddonPayload: ResolverTypeWrapper<Partial<UpdateUserEntityAddonPayload>>;
  UpsertBannedDraftCardInput: ResolverTypeWrapper<Partial<UpsertBannedDraftCardInput>>;
  UpsertBannedDraftCardPayload: ResolverTypeWrapper<Partial<UpsertBannedDraftCardPayload>>;
  UpsertBannedDraftCardWhere: ResolverTypeWrapper<Partial<UpsertBannedDraftCardWhere>>;
  UpsertBotUserInput: ResolverTypeWrapper<Partial<UpsertBotUserInput>>;
  UpsertBotUserPayload: ResolverTypeWrapper<Partial<UpsertBotUserPayload>>;
  UpsertBotUserWhere: ResolverTypeWrapper<Partial<UpsertBotUserWhere>>;
  UpsertCardsInDeckInput: ResolverTypeWrapper<Partial<UpsertCardsInDeckInput>>;
  UpsertCardsInDeckPayload: ResolverTypeWrapper<Partial<UpsertCardsInDeckPayload>>;
  UpsertCardsInDeckWhere: ResolverTypeWrapper<Partial<UpsertCardsInDeckWhere>>;
  UpsertDeckInput: ResolverTypeWrapper<Partial<UpsertDeckInput>>;
  UpsertDeckPayload: ResolverTypeWrapper<Partial<UpsertDeckPayload>>;
  UpsertDeckPlayerAttributeTupleInput: ResolverTypeWrapper<Partial<UpsertDeckPlayerAttributeTupleInput>>;
  UpsertDeckPlayerAttributeTuplePayload: ResolverTypeWrapper<Partial<UpsertDeckPlayerAttributeTuplePayload>>;
  UpsertDeckPlayerAttributeTupleWhere: ResolverTypeWrapper<Partial<UpsertDeckPlayerAttributeTupleWhere>>;
  UpsertDeckShareInput: ResolverTypeWrapper<Partial<UpsertDeckShareInput>>;
  UpsertDeckSharePayload: ResolverTypeWrapper<Partial<UpsertDeckSharePayload>>;
  UpsertDeckShareWhere: ResolverTypeWrapper<Partial<UpsertDeckShareWhere>>;
  UpsertDeckWhere: ResolverTypeWrapper<Partial<UpsertDeckWhere>>;
  UpsertFriendInput: ResolverTypeWrapper<Partial<UpsertFriendInput>>;
  UpsertFriendPayload: ResolverTypeWrapper<Partial<UpsertFriendPayload>>;
  UpsertFriendWhere: ResolverTypeWrapper<Partial<UpsertFriendWhere>>;
  UpsertGameInput: ResolverTypeWrapper<Partial<UpsertGameInput>>;
  UpsertGamePayload: ResolverTypeWrapper<Partial<UpsertGamePayload>>;
  UpsertGameUserInput: ResolverTypeWrapper<Partial<UpsertGameUserInput>>;
  UpsertGameUserPayload: ResolverTypeWrapper<Partial<UpsertGameUserPayload>>;
  UpsertGameUserWhere: ResolverTypeWrapper<Partial<UpsertGameUserWhere>>;
  UpsertGameWhere: ResolverTypeWrapper<Partial<UpsertGameWhere>>;
  UpsertGuestInput: ResolverTypeWrapper<Partial<UpsertGuestInput>>;
  UpsertGuestPayload: ResolverTypeWrapper<Partial<UpsertGuestPayload>>;
  UpsertGuestWhere: ResolverTypeWrapper<Partial<UpsertGuestWhere>>;
  UpsertHardRemovalCardInput: ResolverTypeWrapper<Partial<UpsertHardRemovalCardInput>>;
  UpsertHardRemovalCardPayload: ResolverTypeWrapper<Partial<UpsertHardRemovalCardPayload>>;
  UpsertHardRemovalCardWhere: ResolverTypeWrapper<Partial<UpsertHardRemovalCardWhere>>;
  UpsertMatchmakingQueueInput: ResolverTypeWrapper<Partial<UpsertMatchmakingQueueInput>>;
  UpsertMatchmakingQueuePayload: ResolverTypeWrapper<Partial<UpsertMatchmakingQueuePayload>>;
  UpsertMatchmakingQueueWhere: ResolverTypeWrapper<Partial<UpsertMatchmakingQueueWhere>>;
  UpsertMatchmakingTicketInput: ResolverTypeWrapper<Partial<UpsertMatchmakingTicketInput>>;
  UpsertMatchmakingTicketPayload: ResolverTypeWrapper<Partial<UpsertMatchmakingTicketPayload>>;
  UpsertMatchmakingTicketWhere: ResolverTypeWrapper<Partial<UpsertMatchmakingTicketWhere>>;
  UpsertUserEntityAddonInput: ResolverTypeWrapper<Partial<UpsertUserEntityAddonInput>>;
  UpsertUserEntityAddonPayload: ResolverTypeWrapper<Partial<UpsertUserEntityAddonPayload>>;
  UpsertUserEntityAddonWhere: ResolverTypeWrapper<Partial<UpsertUserEntityAddonWhere>>;
  UserEntityAddon: ResolverTypeWrapper<Partial<UserEntityAddon>>;
  UserEntityAddonCondition: ResolverTypeWrapper<Partial<UserEntityAddonCondition>>;
  UserEntityAddonFilter: ResolverTypeWrapper<Partial<UserEntityAddonFilter>>;
  UserEntityAddonInput: ResolverTypeWrapper<Partial<UserEntityAddonInput>>;
  UserEntityAddonPatch: ResolverTypeWrapper<Partial<UserEntityAddonPatch>>;
  UserEntityAddonsConnection: ResolverTypeWrapper<Partial<UserEntityAddonsConnection>>;
  UserEntityAddonsEdge: ResolverTypeWrapper<Partial<UserEntityAddonsEdge>>;
  UserEntityAddonsOrderBy: ResolverTypeWrapper<Partial<UserEntityAddonsOrderBy>>;
};

/** Mapping between all available schema types and the resolvers parents */
export type ResolversParentTypes = {
  ArchiveCardInput: Partial<ArchiveCardInput>;
  ArchiveCardPayload: Partial<ArchiveCardPayload>;
  BannedDraftCard: Partial<BannedDraftCard>;
  BannedDraftCardCondition: Partial<BannedDraftCardCondition>;
  BannedDraftCardFilter: Partial<BannedDraftCardFilter>;
  BannedDraftCardInput: Partial<BannedDraftCardInput>;
  BannedDraftCardPatch: Partial<BannedDraftCardPatch>;
  BannedDraftCardsConnection: Partial<BannedDraftCardsConnection>;
  BannedDraftCardsEdge: Partial<BannedDraftCardsEdge>;
  BigInt: Partial<Scalars['BigInt']>;
  BigIntFilter: Partial<BigIntFilter>;
  Boolean: Partial<Scalars['Boolean']>;
  BooleanFilter: Partial<BooleanFilter>;
  BotUser: Partial<BotUser>;
  BotUserCondition: Partial<BotUserCondition>;
  BotUserFilter: Partial<BotUserFilter>;
  BotUserInput: Partial<BotUserInput>;
  BotUserPatch: Partial<BotUserPatch>;
  BotUsersConnection: Partial<BotUsersConnection>;
  BotUsersEdge: Partial<BotUsersEdge>;
  Card: Partial<Card>;
  CardCatalogueFormatsInput: Partial<CardCatalogueFormatsInput>;
  CardCatalogueFormatsPayload: Partial<CardCatalogueFormatsPayload>;
  CardCatalogueGetBannedDraftCardsInput: Partial<CardCatalogueGetBannedDraftCardsInput>;
  CardCatalogueGetBannedDraftCardsPayload: Partial<CardCatalogueGetBannedDraftCardsPayload>;
  CardCatalogueGetBaseClassesInput: Partial<CardCatalogueGetBaseClassesInput>;
  CardCatalogueGetBaseClassesPayload: Partial<CardCatalogueGetBaseClassesPayload>;
  CardCatalogueGetCardByIdInput: Partial<CardCatalogueGetCardByIdInput>;
  CardCatalogueGetCardByIdPayload: Partial<CardCatalogueGetCardByIdPayload>;
  CardCatalogueGetCardByNameAndClassInput: Partial<CardCatalogueGetCardByNameAndClassInput>;
  CardCatalogueGetCardByNameAndClassPayload: Partial<CardCatalogueGetCardByNameAndClassPayload>;
  CardCatalogueGetCardByNameInput: Partial<CardCatalogueGetCardByNameInput>;
  CardCatalogueGetCardByNamePayload: Partial<CardCatalogueGetCardByNamePayload>;
  CardCatalogueGetClassCardsInput: Partial<CardCatalogueGetClassCardsInput>;
  CardCatalogueGetClassCardsPayload: Partial<CardCatalogueGetClassCardsPayload>;
  CardCatalogueGetFormatInput: Partial<CardCatalogueGetFormatInput>;
  CardCatalogueGetFormatPayload: Partial<CardCatalogueGetFormatPayload>;
  CardCatalogueGetHardRemovalCardsInput: Partial<CardCatalogueGetHardRemovalCardsInput>;
  CardCatalogueGetHardRemovalCardsPayload: Partial<CardCatalogueGetHardRemovalCardsPayload>;
  CardCatalogueGetHeroCardInput: Partial<CardCatalogueGetHeroCardInput>;
  CardCatalogueGetHeroCardPayload: Partial<CardCatalogueGetHeroCardPayload>;
  CardCatalogueQueryInput: Partial<CardCatalogueQueryInput>;
  CardCatalogueQueryPayload: Partial<CardCatalogueQueryPayload>;
  CardCondition: Partial<CardCondition>;
  CardFilter: Partial<CardFilter>;
  CardInput: Partial<CardInput>;
  CardsConnection: Partial<CardsConnection>;
  CardsEdge: Partial<CardsEdge>;
  CardsInDeck: Partial<CardsInDeck>;
  CardsInDeckCondition: Partial<CardsInDeckCondition>;
  CardsInDeckFilter: Partial<CardsInDeckFilter>;
  CardsInDeckInput: Partial<CardsInDeckInput>;
  CardsInDeckPatch: Partial<CardsInDeckPatch>;
  CardsInDecksConnection: Partial<CardsInDecksConnection>;
  CardsInDecksEdge: Partial<CardsInDecksEdge>;
  Class: Partial<Class>;
  ClassCondition: Partial<ClassCondition>;
  ClassFilter: Partial<ClassFilter>;
  ClassesConnection: Partial<ClassesConnection>;
  ClassesEdge: Partial<ClassesEdge>;
  ClusteredGamesUpdateGameAndUsersInput: Partial<ClusteredGamesUpdateGameAndUsersInput>;
  ClusteredGamesUpdateGameAndUsersPayload: Partial<ClusteredGamesUpdateGameAndUsersPayload>;
  CollectionCard: Partial<CollectionCard>;
  CollectionCardCondition: Partial<CollectionCardCondition>;
  CollectionCardFilter: Partial<CollectionCardFilter>;
  CollectionCardsConnection: Partial<CollectionCardsConnection>;
  CollectionCardsEdge: Partial<CollectionCardsEdge>;
  CreateBannedDraftCardInput: Partial<CreateBannedDraftCardInput>;
  CreateBannedDraftCardPayload: Partial<CreateBannedDraftCardPayload>;
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
  CreateDeckWithCardsInput: Partial<CreateDeckWithCardsInput>;
  CreateDeckWithCardsPayload: Partial<CreateDeckWithCardsPayload>;
  CreateFriendInput: Partial<CreateFriendInput>;
  CreateFriendPayload: Partial<CreateFriendPayload>;
  CreateGameInput: Partial<CreateGameInput>;
  CreateGamePayload: Partial<CreateGamePayload>;
  CreateGameUserInput: Partial<CreateGameUserInput>;
  CreateGameUserPayload: Partial<CreateGameUserPayload>;
  CreateGuestInput: Partial<CreateGuestInput>;
  CreateGuestPayload: Partial<CreateGuestPayload>;
  CreateHardRemovalCardInput: Partial<CreateHardRemovalCardInput>;
  CreateHardRemovalCardPayload: Partial<CreateHardRemovalCardPayload>;
  CreateMatchmakingQueueInput: Partial<CreateMatchmakingQueueInput>;
  CreateMatchmakingQueuePayload: Partial<CreateMatchmakingQueuePayload>;
  CreateMatchmakingTicketInput: Partial<CreateMatchmakingTicketInput>;
  CreateMatchmakingTicketPayload: Partial<CreateMatchmakingTicketPayload>;
  CreateUserEntityAddonInput: Partial<CreateUserEntityAddonInput>;
  CreateUserEntityAddonPayload: Partial<CreateUserEntityAddonPayload>;
  CurrentCard: Partial<CurrentCard>;
  CurrentCardCondition: Partial<CurrentCardCondition>;
  CurrentCardFilter: Partial<CurrentCardFilter>;
  CurrentCardsConnection: Partial<CurrentCardsConnection>;
  CurrentCardsEdge: Partial<CurrentCardsEdge>;
  Cursor: Partial<Scalars['Cursor']>;
  Datetime: Partial<Scalars['Datetime']>;
  DatetimeFilter: Partial<DatetimeFilter>;
  Deck: Partial<Deck>;
  DeckCondition: Partial<DeckCondition>;
  DeckFilter: Partial<DeckFilter>;
  DeckInput: Partial<DeckInput>;
  DeckPatch: Partial<DeckPatch>;
  DeckPlayerAttributeTuple: Partial<DeckPlayerAttributeTuple>;
  DeckPlayerAttributeTupleCondition: Partial<DeckPlayerAttributeTupleCondition>;
  DeckPlayerAttributeTupleFilter: Partial<DeckPlayerAttributeTupleFilter>;
  DeckPlayerAttributeTupleInput: Partial<DeckPlayerAttributeTupleInput>;
  DeckPlayerAttributeTuplePatch: Partial<DeckPlayerAttributeTuplePatch>;
  DeckPlayerAttributeTuplesConnection: Partial<DeckPlayerAttributeTuplesConnection>;
  DeckPlayerAttributeTuplesEdge: Partial<DeckPlayerAttributeTuplesEdge>;
  DeckShare: Partial<DeckShare>;
  DeckShareCondition: Partial<DeckShareCondition>;
  DeckShareFilter: Partial<DeckShareFilter>;
  DeckShareInput: Partial<DeckShareInput>;
  DeckSharePatch: Partial<DeckSharePatch>;
  DeckSharesConnection: Partial<DeckSharesConnection>;
  DeckSharesEdge: Partial<DeckSharesEdge>;
  DecksConnection: Partial<DecksConnection>;
  DecksEdge: Partial<DecksEdge>;
  DeleteBannedDraftCardByCardIdInput: Partial<DeleteBannedDraftCardByCardIdInput>;
  DeleteBannedDraftCardInput: Partial<DeleteBannedDraftCardInput>;
  DeleteBannedDraftCardPayload: Partial<DeleteBannedDraftCardPayload>;
  DeleteBotUserByIdInput: Partial<DeleteBotUserByIdInput>;
  DeleteBotUserInput: Partial<DeleteBotUserInput>;
  DeleteBotUserPayload: Partial<DeleteBotUserPayload>;
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
  DeleteHardRemovalCardByCardIdInput: Partial<DeleteHardRemovalCardByCardIdInput>;
  DeleteHardRemovalCardInput: Partial<DeleteHardRemovalCardInput>;
  DeleteHardRemovalCardPayload: Partial<DeleteHardRemovalCardPayload>;
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
  FriendFilter: Partial<FriendFilter>;
  FriendInput: Partial<FriendInput>;
  FriendPatch: Partial<FriendPatch>;
  FriendsConnection: Partial<FriendsConnection>;
  FriendsEdge: Partial<FriendsEdge>;
  Game: Partial<Game>;
  GameCondition: Partial<GameCondition>;
  GameFilter: Partial<GameFilter>;
  GameInput: Partial<GameInput>;
  GamePatch: Partial<GamePatch>;
  GameStateEnumFilter: Partial<GameStateEnumFilter>;
  GameUser: Partial<GameUser>;
  GameUserCondition: Partial<GameUserCondition>;
  GameUserFilter: Partial<GameUserFilter>;
  GameUserInput: Partial<GameUserInput>;
  GameUserPatch: Partial<GameUserPatch>;
  GameUserVictoryEnumFilter: Partial<GameUserVictoryEnumFilter>;
  GameUsersConnection: Partial<GameUsersConnection>;
  GameUsersEdge: Partial<GameUsersEdge>;
  GamesConnection: Partial<GamesConnection>;
  GamesEdge: Partial<GamesEdge>;
  GetClassesInput: Partial<GetClassesInput>;
  GetClassesPayload: Partial<GetClassesPayload>;
  GetClassesRecord: Partial<GetClassesRecord>;
  GetCollectionCardsInput: Partial<GetCollectionCardsInput>;
  GetCollectionCardsPayload: Partial<GetCollectionCardsPayload>;
  GetCollectionCardsRecord: Partial<GetCollectionCardsRecord>;
  Guest: Partial<Guest>;
  GuestCondition: Partial<GuestCondition>;
  GuestFilter: Partial<GuestFilter>;
  GuestInput: Partial<GuestInput>;
  GuestPatch: Partial<GuestPatch>;
  GuestsConnection: Partial<GuestsConnection>;
  GuestsEdge: Partial<GuestsEdge>;
  HardRemovalCard: Partial<HardRemovalCard>;
  HardRemovalCardCondition: Partial<HardRemovalCardCondition>;
  HardRemovalCardFilter: Partial<HardRemovalCardFilter>;
  HardRemovalCardInput: Partial<HardRemovalCardInput>;
  HardRemovalCardPatch: Partial<HardRemovalCardPatch>;
  HardRemovalCardsConnection: Partial<HardRemovalCardsConnection>;
  HardRemovalCardsEdge: Partial<HardRemovalCardsEdge>;
  ID: Partial<Scalars['ID']>;
  ImageDef: Partial<ImageDef>;
  Int: Partial<Scalars['Int']>;
  IntFilter: Partial<IntFilter>;
  JSON: Partial<Scalars['JSON']>;
  JSONFilter: Partial<JsonFilter>;
  MatchmakingQueue: Partial<MatchmakingQueue>;
  MatchmakingQueueCondition: Partial<MatchmakingQueueCondition>;
  MatchmakingQueueFilter: Partial<MatchmakingQueueFilter>;
  MatchmakingQueueInput: Partial<MatchmakingQueueInput>;
  MatchmakingQueuePatch: Partial<MatchmakingQueuePatch>;
  MatchmakingQueuesConnection: Partial<MatchmakingQueuesConnection>;
  MatchmakingQueuesEdge: Partial<MatchmakingQueuesEdge>;
  MatchmakingTicket: Partial<MatchmakingTicket>;
  MatchmakingTicketCondition: Partial<MatchmakingTicketCondition>;
  MatchmakingTicketFilter: Partial<MatchmakingTicketFilter>;
  MatchmakingTicketInput: Partial<MatchmakingTicketInput>;
  MatchmakingTicketPatch: Partial<MatchmakingTicketPatch>;
  MatchmakingTicketsConnection: Partial<MatchmakingTicketsConnection>;
  MatchmakingTicketsEdge: Partial<MatchmakingTicketsEdge>;
  Mutation: {};
  Node: ResolversParentTypes['BannedDraftCard'] | ResolversParentTypes['BotUser'] | ResolversParentTypes['CardsInDeck'] | ResolversParentTypes['Deck'] | ResolversParentTypes['DeckPlayerAttributeTuple'] | ResolversParentTypes['DeckShare'] | ResolversParentTypes['Friend'] | ResolversParentTypes['Game'] | ResolversParentTypes['GameUser'] | ResolversParentTypes['Guest'] | ResolversParentTypes['HardRemovalCard'] | ResolversParentTypes['MatchmakingQueue'] | ResolversParentTypes['MatchmakingTicket'] | ResolversParentTypes['Query'] | ResolversParentTypes['UserEntityAddon'];
  PageInfo: Partial<PageInfo>;
  PublishCardInput: Partial<PublishCardInput>;
  PublishCardPayload: Partial<PublishCardPayload>;
  Query: {};
  SaveCardInput: Partial<SaveCardInput>;
  SaveCardPayload: Partial<SaveCardPayload>;
  SetCardsInDeckInput: Partial<SetCardsInDeckInput>;
  SetCardsInDeckPayload: Partial<SetCardsInDeckPayload>;
  String: Partial<Scalars['String']>;
  StringFilter: Partial<StringFilter>;
  UpdateBannedDraftCardByCardIdInput: Partial<UpdateBannedDraftCardByCardIdInput>;
  UpdateBannedDraftCardInput: Partial<UpdateBannedDraftCardInput>;
  UpdateBannedDraftCardPayload: Partial<UpdateBannedDraftCardPayload>;
  UpdateBotUserByIdInput: Partial<UpdateBotUserByIdInput>;
  UpdateBotUserInput: Partial<UpdateBotUserInput>;
  UpdateBotUserPayload: Partial<UpdateBotUserPayload>;
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
  UpdateHardRemovalCardByCardIdInput: Partial<UpdateHardRemovalCardByCardIdInput>;
  UpdateHardRemovalCardInput: Partial<UpdateHardRemovalCardInput>;
  UpdateHardRemovalCardPayload: Partial<UpdateHardRemovalCardPayload>;
  UpdateMatchmakingQueueByIdInput: Partial<UpdateMatchmakingQueueByIdInput>;
  UpdateMatchmakingQueueInput: Partial<UpdateMatchmakingQueueInput>;
  UpdateMatchmakingQueuePayload: Partial<UpdateMatchmakingQueuePayload>;
  UpdateMatchmakingTicketByUserIdInput: Partial<UpdateMatchmakingTicketByUserIdInput>;
  UpdateMatchmakingTicketInput: Partial<UpdateMatchmakingTicketInput>;
  UpdateMatchmakingTicketPayload: Partial<UpdateMatchmakingTicketPayload>;
  UpdateUserEntityAddonByIdInput: Partial<UpdateUserEntityAddonByIdInput>;
  UpdateUserEntityAddonInput: Partial<UpdateUserEntityAddonInput>;
  UpdateUserEntityAddonPayload: Partial<UpdateUserEntityAddonPayload>;
  UpsertBannedDraftCardInput: Partial<UpsertBannedDraftCardInput>;
  UpsertBannedDraftCardPayload: Partial<UpsertBannedDraftCardPayload>;
  UpsertBannedDraftCardWhere: Partial<UpsertBannedDraftCardWhere>;
  UpsertBotUserInput: Partial<UpsertBotUserInput>;
  UpsertBotUserPayload: Partial<UpsertBotUserPayload>;
  UpsertBotUserWhere: Partial<UpsertBotUserWhere>;
  UpsertCardsInDeckInput: Partial<UpsertCardsInDeckInput>;
  UpsertCardsInDeckPayload: Partial<UpsertCardsInDeckPayload>;
  UpsertCardsInDeckWhere: Partial<UpsertCardsInDeckWhere>;
  UpsertDeckInput: Partial<UpsertDeckInput>;
  UpsertDeckPayload: Partial<UpsertDeckPayload>;
  UpsertDeckPlayerAttributeTupleInput: Partial<UpsertDeckPlayerAttributeTupleInput>;
  UpsertDeckPlayerAttributeTuplePayload: Partial<UpsertDeckPlayerAttributeTuplePayload>;
  UpsertDeckPlayerAttributeTupleWhere: Partial<UpsertDeckPlayerAttributeTupleWhere>;
  UpsertDeckShareInput: Partial<UpsertDeckShareInput>;
  UpsertDeckSharePayload: Partial<UpsertDeckSharePayload>;
  UpsertDeckShareWhere: Partial<UpsertDeckShareWhere>;
  UpsertDeckWhere: Partial<UpsertDeckWhere>;
  UpsertFriendInput: Partial<UpsertFriendInput>;
  UpsertFriendPayload: Partial<UpsertFriendPayload>;
  UpsertFriendWhere: Partial<UpsertFriendWhere>;
  UpsertGameInput: Partial<UpsertGameInput>;
  UpsertGamePayload: Partial<UpsertGamePayload>;
  UpsertGameUserInput: Partial<UpsertGameUserInput>;
  UpsertGameUserPayload: Partial<UpsertGameUserPayload>;
  UpsertGameUserWhere: Partial<UpsertGameUserWhere>;
  UpsertGameWhere: Partial<UpsertGameWhere>;
  UpsertGuestInput: Partial<UpsertGuestInput>;
  UpsertGuestPayload: Partial<UpsertGuestPayload>;
  UpsertGuestWhere: Partial<UpsertGuestWhere>;
  UpsertHardRemovalCardInput: Partial<UpsertHardRemovalCardInput>;
  UpsertHardRemovalCardPayload: Partial<UpsertHardRemovalCardPayload>;
  UpsertHardRemovalCardWhere: Partial<UpsertHardRemovalCardWhere>;
  UpsertMatchmakingQueueInput: Partial<UpsertMatchmakingQueueInput>;
  UpsertMatchmakingQueuePayload: Partial<UpsertMatchmakingQueuePayload>;
  UpsertMatchmakingQueueWhere: Partial<UpsertMatchmakingQueueWhere>;
  UpsertMatchmakingTicketInput: Partial<UpsertMatchmakingTicketInput>;
  UpsertMatchmakingTicketPayload: Partial<UpsertMatchmakingTicketPayload>;
  UpsertMatchmakingTicketWhere: Partial<UpsertMatchmakingTicketWhere>;
  UpsertUserEntityAddonInput: Partial<UpsertUserEntityAddonInput>;
  UpsertUserEntityAddonPayload: Partial<UpsertUserEntityAddonPayload>;
  UpsertUserEntityAddonWhere: Partial<UpsertUserEntityAddonWhere>;
  UserEntityAddon: Partial<UserEntityAddon>;
  UserEntityAddonCondition: Partial<UserEntityAddonCondition>;
  UserEntityAddonFilter: Partial<UserEntityAddonFilter>;
  UserEntityAddonInput: Partial<UserEntityAddonInput>;
  UserEntityAddonPatch: Partial<UserEntityAddonPatch>;
  UserEntityAddonsConnection: Partial<UserEntityAddonsConnection>;
  UserEntityAddonsEdge: Partial<UserEntityAddonsEdge>;
};

export type ArchiveCardPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['ArchiveCardPayload'] = ResolversParentTypes['ArchiveCardPayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type BannedDraftCardResolvers<ContextType = any, ParentType extends ResolversParentTypes['BannedDraftCard'] = ResolversParentTypes['BannedDraftCard']> = {
  cardId?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  nodeId?: Resolver<ResolversTypes['ID'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type BannedDraftCardsConnectionResolvers<ContextType = any, ParentType extends ResolversParentTypes['BannedDraftCardsConnection'] = ResolversParentTypes['BannedDraftCardsConnection']> = {
  edges?: Resolver<Array<ResolversTypes['BannedDraftCardsEdge']>, ParentType, ContextType>;
  nodes?: Resolver<Array<Maybe<ResolversTypes['BannedDraftCard']>>, ParentType, ContextType>;
  pageInfo?: Resolver<ResolversTypes['PageInfo'], ParentType, ContextType>;
  totalCount?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type BannedDraftCardsEdgeResolvers<ContextType = any, ParentType extends ResolversParentTypes['BannedDraftCardsEdge'] = ResolversParentTypes['BannedDraftCardsEdge']> = {
  cursor?: Resolver<Maybe<ResolversTypes['Cursor']>, ParentType, ContextType>;
  node?: Resolver<Maybe<ResolversTypes['BannedDraftCard']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
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
  collectible?: Resolver<Maybe<ResolversTypes['Boolean']>, ParentType, ContextType>;
  cost?: Resolver<Maybe<ResolversTypes['Int']>, ParentType, ContextType>;
  createdAt?: Resolver<ResolversTypes['Datetime'], ParentType, ContextType>;
  createdBy?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  id?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  isArchived?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  isPublished?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  lastModified?: Resolver<ResolversTypes['Datetime'], ParentType, ContextType>;
  succession?: Resolver<ResolversTypes['BigInt'], ParentType, ContextType>;
  type?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  uri?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type CardCatalogueFormatsPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['CardCatalogueFormatsPayload'] = ResolversParentTypes['CardCatalogueFormatsPayload']> = {
  cards?: Resolver<Maybe<Array<Maybe<ResolversTypes['Card']>>>, ParentType, ContextType>;
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type CardCatalogueGetBannedDraftCardsPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['CardCatalogueGetBannedDraftCardsPayload'] = ResolversParentTypes['CardCatalogueGetBannedDraftCardsPayload']> = {
  cardIds?: Resolver<Maybe<Array<Maybe<ResolversTypes['String']>>>, ParentType, ContextType>;
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type CardCatalogueGetBaseClassesPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['CardCatalogueGetBaseClassesPayload'] = ResolversParentTypes['CardCatalogueGetBaseClassesPayload']> = {
  cards?: Resolver<Maybe<Array<Maybe<ResolversTypes['Card']>>>, ParentType, ContextType>;
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type CardCatalogueGetCardByIdPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['CardCatalogueGetCardByIdPayload'] = ResolversParentTypes['CardCatalogueGetCardByIdPayload']> = {
  cards?: Resolver<Maybe<Array<Maybe<ResolversTypes['Card']>>>, ParentType, ContextType>;
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type CardCatalogueGetCardByNameAndClassPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['CardCatalogueGetCardByNameAndClassPayload'] = ResolversParentTypes['CardCatalogueGetCardByNameAndClassPayload']> = {
  card?: Resolver<Maybe<ResolversTypes['Card']>, ParentType, ContextType>;
  cardEdge?: Resolver<Maybe<ResolversTypes['CardsEdge']>, ParentType, ContextType, RequireFields<CardCatalogueGetCardByNameAndClassPayloadCardEdgeArgs, 'orderBy'>>;
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type CardCatalogueGetCardByNamePayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['CardCatalogueGetCardByNamePayload'] = ResolversParentTypes['CardCatalogueGetCardByNamePayload']> = {
  card?: Resolver<Maybe<ResolversTypes['Card']>, ParentType, ContextType>;
  cardEdge?: Resolver<Maybe<ResolversTypes['CardsEdge']>, ParentType, ContextType, RequireFields<CardCatalogueGetCardByNamePayloadCardEdgeArgs, 'orderBy'>>;
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type CardCatalogueGetClassCardsPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['CardCatalogueGetClassCardsPayload'] = ResolversParentTypes['CardCatalogueGetClassCardsPayload']> = {
  cards?: Resolver<Maybe<Array<Maybe<ResolversTypes['Card']>>>, ParentType, ContextType>;
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type CardCatalogueGetFormatPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['CardCatalogueGetFormatPayload'] = ResolversParentTypes['CardCatalogueGetFormatPayload']> = {
  cards?: Resolver<Maybe<Array<Maybe<ResolversTypes['Card']>>>, ParentType, ContextType>;
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type CardCatalogueGetHardRemovalCardsPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['CardCatalogueGetHardRemovalCardsPayload'] = ResolversParentTypes['CardCatalogueGetHardRemovalCardsPayload']> = {
  cardIds?: Resolver<Maybe<Array<Maybe<ResolversTypes['String']>>>, ParentType, ContextType>;
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type CardCatalogueGetHeroCardPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['CardCatalogueGetHeroCardPayload'] = ResolversParentTypes['CardCatalogueGetHeroCardPayload']> = {
  card?: Resolver<Maybe<ResolversTypes['Card']>, ParentType, ContextType>;
  cardEdge?: Resolver<Maybe<ResolversTypes['CardsEdge']>, ParentType, ContextType, RequireFields<CardCatalogueGetHeroCardPayloadCardEdgeArgs, 'orderBy'>>;
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type CardCatalogueQueryPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['CardCatalogueQueryPayload'] = ResolversParentTypes['CardCatalogueQueryPayload']> = {
  cards?: Resolver<Maybe<Array<Maybe<ResolversTypes['Card']>>>, ParentType, ContextType>;
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
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

export type ClassResolvers<ContextType = any, ParentType extends ResolversParentTypes['Class'] = ResolversParentTypes['Class']> = {
  cardScript?: Resolver<Maybe<ResolversTypes['JSON']>, ParentType, ContextType>;
  class?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  collectible?: Resolver<Maybe<ResolversTypes['Boolean']>, ParentType, ContextType>;
  createdBy?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  id?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  isPublished?: Resolver<Maybe<ResolversTypes['Boolean']>, ParentType, ContextType>;
  name?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type ClassesConnectionResolvers<ContextType = any, ParentType extends ResolversParentTypes['ClassesConnection'] = ResolversParentTypes['ClassesConnection']> = {
  edges?: Resolver<Array<ResolversTypes['ClassesEdge']>, ParentType, ContextType>;
  nodes?: Resolver<Array<Maybe<ResolversTypes['Class']>>, ParentType, ContextType>;
  pageInfo?: Resolver<ResolversTypes['PageInfo'], ParentType, ContextType>;
  totalCount?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type ClassesEdgeResolvers<ContextType = any, ParentType extends ResolversParentTypes['ClassesEdge'] = ResolversParentTypes['ClassesEdge']> = {
  cursor?: Resolver<Maybe<ResolversTypes['Cursor']>, ParentType, ContextType>;
  node?: Resolver<Maybe<ResolversTypes['Class']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type ClusteredGamesUpdateGameAndUsersPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['ClusteredGamesUpdateGameAndUsersPayload'] = ResolversParentTypes['ClusteredGamesUpdateGameAndUsersPayload']> = {
  boolean?: Resolver<Maybe<ResolversTypes['Boolean']>, ParentType, ContextType>;
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type CollectionCardResolvers<ContextType = any, ParentType extends ResolversParentTypes['CollectionCard'] = ResolversParentTypes['CollectionCard']> = {
  blocklyWorkspace?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  cardScript?: Resolver<Maybe<ResolversTypes['JSON']>, ParentType, ContextType>;
  class?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  collectible?: Resolver<Maybe<ResolversTypes['Boolean']>, ParentType, ContextType>;
  cost?: Resolver<Maybe<ResolversTypes['Int']>, ParentType, ContextType>;
  createdAt?: Resolver<Maybe<ResolversTypes['Datetime']>, ParentType, ContextType>;
  createdBy?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  id?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  lastModified?: Resolver<Maybe<ResolversTypes['Datetime']>, ParentType, ContextType>;
  name?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  searchMessage?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  type?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type CollectionCardsConnectionResolvers<ContextType = any, ParentType extends ResolversParentTypes['CollectionCardsConnection'] = ResolversParentTypes['CollectionCardsConnection']> = {
  edges?: Resolver<Array<ResolversTypes['CollectionCardsEdge']>, ParentType, ContextType>;
  nodes?: Resolver<Array<Maybe<ResolversTypes['CollectionCard']>>, ParentType, ContextType>;
  pageInfo?: Resolver<ResolversTypes['PageInfo'], ParentType, ContextType>;
  totalCount?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type CollectionCardsEdgeResolvers<ContextType = any, ParentType extends ResolversParentTypes['CollectionCardsEdge'] = ResolversParentTypes['CollectionCardsEdge']> = {
  cursor?: Resolver<Maybe<ResolversTypes['Cursor']>, ParentType, ContextType>;
  node?: Resolver<Maybe<ResolversTypes['CollectionCard']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type CreateBannedDraftCardPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['CreateBannedDraftCardPayload'] = ResolversParentTypes['CreateBannedDraftCardPayload']> = {
  bannedDraftCard?: Resolver<Maybe<ResolversTypes['BannedDraftCard']>, ParentType, ContextType>;
  bannedDraftCardEdge?: Resolver<Maybe<ResolversTypes['BannedDraftCardsEdge']>, ParentType, ContextType, RequireFields<CreateBannedDraftCardPayloadBannedDraftCardEdgeArgs, 'orderBy'>>;
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
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

export type CreateDeckWithCardsPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['CreateDeckWithCardsPayload'] = ResolversParentTypes['CreateDeckWithCardsPayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  deck?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  deckEdge?: Resolver<Maybe<ResolversTypes['DecksEdge']>, ParentType, ContextType, RequireFields<CreateDeckWithCardsPayloadDeckEdgeArgs, 'orderBy'>>;
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

export type CreateHardRemovalCardPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['CreateHardRemovalCardPayload'] = ResolversParentTypes['CreateHardRemovalCardPayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  hardRemovalCard?: Resolver<Maybe<ResolversTypes['HardRemovalCard']>, ParentType, ContextType>;
  hardRemovalCardEdge?: Resolver<Maybe<ResolversTypes['HardRemovalCardsEdge']>, ParentType, ContextType, RequireFields<CreateHardRemovalCardPayloadHardRemovalCardEdgeArgs, 'orderBy'>>;
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

export type CurrentCardResolvers<ContextType = any, ParentType extends ResolversParentTypes['CurrentCard'] = ResolversParentTypes['CurrentCard']> = {
  blocklyWorkspace?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  cardScript?: Resolver<Maybe<ResolversTypes['JSON']>, ParentType, ContextType>;
  createdAt?: Resolver<Maybe<ResolversTypes['Datetime']>, ParentType, ContextType>;
  createdBy?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  id?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  isArchived?: Resolver<Maybe<ResolversTypes['Boolean']>, ParentType, ContextType>;
  isPublished?: Resolver<Maybe<ResolversTypes['Boolean']>, ParentType, ContextType>;
  lastModified?: Resolver<Maybe<ResolversTypes['Datetime']>, ParentType, ContextType>;
  succession?: Resolver<Maybe<ResolversTypes['BigInt']>, ParentType, ContextType>;
  uri?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type CurrentCardsConnectionResolvers<ContextType = any, ParentType extends ResolversParentTypes['CurrentCardsConnection'] = ResolversParentTypes['CurrentCardsConnection']> = {
  edges?: Resolver<Array<ResolversTypes['CurrentCardsEdge']>, ParentType, ContextType>;
  nodes?: Resolver<Array<Maybe<ResolversTypes['CurrentCard']>>, ParentType, ContextType>;
  pageInfo?: Resolver<ResolversTypes['PageInfo'], ParentType, ContextType>;
  totalCount?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type CurrentCardsEdgeResolvers<ContextType = any, ParentType extends ResolversParentTypes['CurrentCardsEdge'] = ResolversParentTypes['CurrentCardsEdge']> = {
  cursor?: Resolver<Maybe<ResolversTypes['Cursor']>, ParentType, ContextType>;
  node?: Resolver<Maybe<ResolversTypes['CurrentCard']>, ParentType, ContextType>;
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

export type DeleteBannedDraftCardPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['DeleteBannedDraftCardPayload'] = ResolversParentTypes['DeleteBannedDraftCardPayload']> = {
  bannedDraftCard?: Resolver<Maybe<ResolversTypes['BannedDraftCard']>, ParentType, ContextType>;
  bannedDraftCardEdge?: Resolver<Maybe<ResolversTypes['BannedDraftCardsEdge']>, ParentType, ContextType, RequireFields<DeleteBannedDraftCardPayloadBannedDraftCardEdgeArgs, 'orderBy'>>;
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  deletedBannedDraftCardId?: Resolver<Maybe<ResolversTypes['ID']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
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

export type DeleteCardsInDeckPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['DeleteCardsInDeckPayload'] = ResolversParentTypes['DeleteCardsInDeckPayload']> = {
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

export type DeleteHardRemovalCardPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['DeleteHardRemovalCardPayload'] = ResolversParentTypes['DeleteHardRemovalCardPayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  deletedHardRemovalCardId?: Resolver<Maybe<ResolversTypes['ID']>, ParentType, ContextType>;
  hardRemovalCard?: Resolver<Maybe<ResolversTypes['HardRemovalCard']>, ParentType, ContextType>;
  hardRemovalCardEdge?: Resolver<Maybe<ResolversTypes['HardRemovalCardsEdge']>, ParentType, ContextType, RequireFields<DeleteHardRemovalCardPayloadHardRemovalCardEdgeArgs, 'orderBy'>>;
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

export type GetClassesPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['GetClassesPayload'] = ResolversParentTypes['GetClassesPayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  results?: Resolver<Maybe<Array<Maybe<ResolversTypes['GetClassesRecord']>>>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type GetClassesRecordResolvers<ContextType = any, ParentType extends ResolversParentTypes['GetClassesRecord'] = ResolversParentTypes['GetClassesRecord']> = {
  cardScript?: Resolver<Maybe<ResolversTypes['JSON']>, ParentType, ContextType>;
  class?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  collectible?: Resolver<Maybe<ResolversTypes['Boolean']>, ParentType, ContextType>;
  createdBy?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  id?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  isPublished?: Resolver<Maybe<ResolversTypes['Boolean']>, ParentType, ContextType>;
  name?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type GetCollectionCardsPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['GetCollectionCardsPayload'] = ResolversParentTypes['GetCollectionCardsPayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  results?: Resolver<Maybe<Array<Maybe<ResolversTypes['GetCollectionCardsRecord']>>>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type GetCollectionCardsRecordResolvers<ContextType = any, ParentType extends ResolversParentTypes['GetCollectionCardsRecord'] = ResolversParentTypes['GetCollectionCardsRecord']> = {
  blocklyWorkspace?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  cardScript?: Resolver<Maybe<ResolversTypes['JSON']>, ParentType, ContextType>;
  class?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  collectible?: Resolver<Maybe<ResolversTypes['Boolean']>, ParentType, ContextType>;
  cost?: Resolver<Maybe<ResolversTypes['Int']>, ParentType, ContextType>;
  createdAt?: Resolver<Maybe<ResolversTypes['Datetime']>, ParentType, ContextType>;
  createdBy?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  id?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  lastModified?: Resolver<Maybe<ResolversTypes['Datetime']>, ParentType, ContextType>;
  name?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  searchMessage?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  type?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
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

export type HardRemovalCardResolvers<ContextType = any, ParentType extends ResolversParentTypes['HardRemovalCard'] = ResolversParentTypes['HardRemovalCard']> = {
  cardId?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  nodeId?: Resolver<ResolversTypes['ID'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type HardRemovalCardsConnectionResolvers<ContextType = any, ParentType extends ResolversParentTypes['HardRemovalCardsConnection'] = ResolversParentTypes['HardRemovalCardsConnection']> = {
  edges?: Resolver<Array<ResolversTypes['HardRemovalCardsEdge']>, ParentType, ContextType>;
  nodes?: Resolver<Array<Maybe<ResolversTypes['HardRemovalCard']>>, ParentType, ContextType>;
  pageInfo?: Resolver<ResolversTypes['PageInfo'], ParentType, ContextType>;
  totalCount?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type HardRemovalCardsEdgeResolvers<ContextType = any, ParentType extends ResolversParentTypes['HardRemovalCardsEdge'] = ResolversParentTypes['HardRemovalCardsEdge']> = {
  cursor?: Resolver<Maybe<ResolversTypes['Cursor']>, ParentType, ContextType>;
  node?: Resolver<Maybe<ResolversTypes['HardRemovalCard']>, ParentType, ContextType>;
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
  archiveCard?: Resolver<Maybe<ResolversTypes['ArchiveCardPayload']>, ParentType, ContextType, RequireFields<MutationArchiveCardArgs, 'input'>>;
  cardCatalogueFormats?: Resolver<Maybe<ResolversTypes['CardCatalogueFormatsPayload']>, ParentType, ContextType, RequireFields<MutationCardCatalogueFormatsArgs, 'input'>>;
  cardCatalogueGetBannedDraftCards?: Resolver<Maybe<ResolversTypes['CardCatalogueGetBannedDraftCardsPayload']>, ParentType, ContextType, RequireFields<MutationCardCatalogueGetBannedDraftCardsArgs, 'input'>>;
  cardCatalogueGetBaseClasses?: Resolver<Maybe<ResolversTypes['CardCatalogueGetBaseClassesPayload']>, ParentType, ContextType, RequireFields<MutationCardCatalogueGetBaseClassesArgs, 'input'>>;
  cardCatalogueGetCardById?: Resolver<Maybe<ResolversTypes['CardCatalogueGetCardByIdPayload']>, ParentType, ContextType, RequireFields<MutationCardCatalogueGetCardByIdArgs, 'input'>>;
  cardCatalogueGetCardByName?: Resolver<Maybe<ResolversTypes['CardCatalogueGetCardByNamePayload']>, ParentType, ContextType, RequireFields<MutationCardCatalogueGetCardByNameArgs, 'input'>>;
  cardCatalogueGetCardByNameAndClass?: Resolver<Maybe<ResolversTypes['CardCatalogueGetCardByNameAndClassPayload']>, ParentType, ContextType, RequireFields<MutationCardCatalogueGetCardByNameAndClassArgs, 'input'>>;
  cardCatalogueGetClassCards?: Resolver<Maybe<ResolversTypes['CardCatalogueGetClassCardsPayload']>, ParentType, ContextType, RequireFields<MutationCardCatalogueGetClassCardsArgs, 'input'>>;
  cardCatalogueGetFormat?: Resolver<Maybe<ResolversTypes['CardCatalogueGetFormatPayload']>, ParentType, ContextType, RequireFields<MutationCardCatalogueGetFormatArgs, 'input'>>;
  cardCatalogueGetHardRemovalCards?: Resolver<Maybe<ResolversTypes['CardCatalogueGetHardRemovalCardsPayload']>, ParentType, ContextType, RequireFields<MutationCardCatalogueGetHardRemovalCardsArgs, 'input'>>;
  cardCatalogueGetHeroCard?: Resolver<Maybe<ResolversTypes['CardCatalogueGetHeroCardPayload']>, ParentType, ContextType, RequireFields<MutationCardCatalogueGetHeroCardArgs, 'input'>>;
  cardCatalogueQuery?: Resolver<Maybe<ResolversTypes['CardCatalogueQueryPayload']>, ParentType, ContextType, RequireFields<MutationCardCatalogueQueryArgs, 'input'>>;
  clusteredGamesUpdateGameAndUsers?: Resolver<Maybe<ResolversTypes['ClusteredGamesUpdateGameAndUsersPayload']>, ParentType, ContextType, RequireFields<MutationClusteredGamesUpdateGameAndUsersArgs, 'input'>>;
  createBannedDraftCard?: Resolver<Maybe<ResolversTypes['CreateBannedDraftCardPayload']>, ParentType, ContextType, RequireFields<MutationCreateBannedDraftCardArgs, 'input'>>;
  createBotUser?: Resolver<Maybe<ResolversTypes['CreateBotUserPayload']>, ParentType, ContextType, RequireFields<MutationCreateBotUserArgs, 'input'>>;
  createCard?: Resolver<Maybe<ResolversTypes['CreateCardPayload']>, ParentType, ContextType, RequireFields<MutationCreateCardArgs, 'input'>>;
  createCardsInDeck?: Resolver<Maybe<ResolversTypes['CreateCardsInDeckPayload']>, ParentType, ContextType, RequireFields<MutationCreateCardsInDeckArgs, 'input'>>;
  createDeck?: Resolver<Maybe<ResolversTypes['CreateDeckPayload']>, ParentType, ContextType, RequireFields<MutationCreateDeckArgs, 'input'>>;
  createDeckPlayerAttributeTuple?: Resolver<Maybe<ResolversTypes['CreateDeckPlayerAttributeTuplePayload']>, ParentType, ContextType, RequireFields<MutationCreateDeckPlayerAttributeTupleArgs, 'input'>>;
  createDeckShare?: Resolver<Maybe<ResolversTypes['CreateDeckSharePayload']>, ParentType, ContextType, RequireFields<MutationCreateDeckShareArgs, 'input'>>;
  createDeckWithCards?: Resolver<Maybe<ResolversTypes['CreateDeckWithCardsPayload']>, ParentType, ContextType, RequireFields<MutationCreateDeckWithCardsArgs, 'input'>>;
  createFriend?: Resolver<Maybe<ResolversTypes['CreateFriendPayload']>, ParentType, ContextType, RequireFields<MutationCreateFriendArgs, 'input'>>;
  createGame?: Resolver<Maybe<ResolversTypes['CreateGamePayload']>, ParentType, ContextType, RequireFields<MutationCreateGameArgs, 'input'>>;
  createGameUser?: Resolver<Maybe<ResolversTypes['CreateGameUserPayload']>, ParentType, ContextType, RequireFields<MutationCreateGameUserArgs, 'input'>>;
  createGuest?: Resolver<Maybe<ResolversTypes['CreateGuestPayload']>, ParentType, ContextType, RequireFields<MutationCreateGuestArgs, 'input'>>;
  createHardRemovalCard?: Resolver<Maybe<ResolversTypes['CreateHardRemovalCardPayload']>, ParentType, ContextType, RequireFields<MutationCreateHardRemovalCardArgs, 'input'>>;
  createMatchmakingQueue?: Resolver<Maybe<ResolversTypes['CreateMatchmakingQueuePayload']>, ParentType, ContextType, RequireFields<MutationCreateMatchmakingQueueArgs, 'input'>>;
  createMatchmakingTicket?: Resolver<Maybe<ResolversTypes['CreateMatchmakingTicketPayload']>, ParentType, ContextType, RequireFields<MutationCreateMatchmakingTicketArgs, 'input'>>;
  createUserEntityAddon?: Resolver<Maybe<ResolversTypes['CreateUserEntityAddonPayload']>, ParentType, ContextType, RequireFields<MutationCreateUserEntityAddonArgs, 'input'>>;
  deleteBannedDraftCard?: Resolver<Maybe<ResolversTypes['DeleteBannedDraftCardPayload']>, ParentType, ContextType, RequireFields<MutationDeleteBannedDraftCardArgs, 'input'>>;
  deleteBannedDraftCardByCardId?: Resolver<Maybe<ResolversTypes['DeleteBannedDraftCardPayload']>, ParentType, ContextType, RequireFields<MutationDeleteBannedDraftCardByCardIdArgs, 'input'>>;
  deleteBotUser?: Resolver<Maybe<ResolversTypes['DeleteBotUserPayload']>, ParentType, ContextType, RequireFields<MutationDeleteBotUserArgs, 'input'>>;
  deleteBotUserById?: Resolver<Maybe<ResolversTypes['DeleteBotUserPayload']>, ParentType, ContextType, RequireFields<MutationDeleteBotUserByIdArgs, 'input'>>;
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
  deleteHardRemovalCard?: Resolver<Maybe<ResolversTypes['DeleteHardRemovalCardPayload']>, ParentType, ContextType, RequireFields<MutationDeleteHardRemovalCardArgs, 'input'>>;
  deleteHardRemovalCardByCardId?: Resolver<Maybe<ResolversTypes['DeleteHardRemovalCardPayload']>, ParentType, ContextType, RequireFields<MutationDeleteHardRemovalCardByCardIdArgs, 'input'>>;
  deleteMatchmakingQueue?: Resolver<Maybe<ResolversTypes['DeleteMatchmakingQueuePayload']>, ParentType, ContextType, RequireFields<MutationDeleteMatchmakingQueueArgs, 'input'>>;
  deleteMatchmakingQueueById?: Resolver<Maybe<ResolversTypes['DeleteMatchmakingQueuePayload']>, ParentType, ContextType, RequireFields<MutationDeleteMatchmakingQueueByIdArgs, 'input'>>;
  deleteMatchmakingTicket?: Resolver<Maybe<ResolversTypes['DeleteMatchmakingTicketPayload']>, ParentType, ContextType, RequireFields<MutationDeleteMatchmakingTicketArgs, 'input'>>;
  deleteMatchmakingTicketByUserId?: Resolver<Maybe<ResolversTypes['DeleteMatchmakingTicketPayload']>, ParentType, ContextType, RequireFields<MutationDeleteMatchmakingTicketByUserIdArgs, 'input'>>;
  deleteUserEntityAddon?: Resolver<Maybe<ResolversTypes['DeleteUserEntityAddonPayload']>, ParentType, ContextType, RequireFields<MutationDeleteUserEntityAddonArgs, 'input'>>;
  deleteUserEntityAddonById?: Resolver<Maybe<ResolversTypes['DeleteUserEntityAddonPayload']>, ParentType, ContextType, RequireFields<MutationDeleteUserEntityAddonByIdArgs, 'input'>>;
  getClasses?: Resolver<Maybe<ResolversTypes['GetClassesPayload']>, ParentType, ContextType, RequireFields<MutationGetClassesArgs, 'input'>>;
  getCollectionCards?: Resolver<Maybe<ResolversTypes['GetCollectionCardsPayload']>, ParentType, ContextType, RequireFields<MutationGetCollectionCardsArgs, 'input'>>;
  publishCard?: Resolver<Maybe<ResolversTypes['PublishCardPayload']>, ParentType, ContextType, RequireFields<MutationPublishCardArgs, 'input'>>;
  saveCard?: Resolver<Maybe<ResolversTypes['SaveCardPayload']>, ParentType, ContextType, RequireFields<MutationSaveCardArgs, 'input'>>;
  setCardsInDeck?: Resolver<Maybe<ResolversTypes['SetCardsInDeckPayload']>, ParentType, ContextType, RequireFields<MutationSetCardsInDeckArgs, 'input'>>;
  updateBannedDraftCard?: Resolver<Maybe<ResolversTypes['UpdateBannedDraftCardPayload']>, ParentType, ContextType, RequireFields<MutationUpdateBannedDraftCardArgs, 'input'>>;
  updateBannedDraftCardByCardId?: Resolver<Maybe<ResolversTypes['UpdateBannedDraftCardPayload']>, ParentType, ContextType, RequireFields<MutationUpdateBannedDraftCardByCardIdArgs, 'input'>>;
  updateBotUser?: Resolver<Maybe<ResolversTypes['UpdateBotUserPayload']>, ParentType, ContextType, RequireFields<MutationUpdateBotUserArgs, 'input'>>;
  updateBotUserById?: Resolver<Maybe<ResolversTypes['UpdateBotUserPayload']>, ParentType, ContextType, RequireFields<MutationUpdateBotUserByIdArgs, 'input'>>;
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
  updateHardRemovalCard?: Resolver<Maybe<ResolversTypes['UpdateHardRemovalCardPayload']>, ParentType, ContextType, RequireFields<MutationUpdateHardRemovalCardArgs, 'input'>>;
  updateHardRemovalCardByCardId?: Resolver<Maybe<ResolversTypes['UpdateHardRemovalCardPayload']>, ParentType, ContextType, RequireFields<MutationUpdateHardRemovalCardByCardIdArgs, 'input'>>;
  updateMatchmakingQueue?: Resolver<Maybe<ResolversTypes['UpdateMatchmakingQueuePayload']>, ParentType, ContextType, RequireFields<MutationUpdateMatchmakingQueueArgs, 'input'>>;
  updateMatchmakingQueueById?: Resolver<Maybe<ResolversTypes['UpdateMatchmakingQueuePayload']>, ParentType, ContextType, RequireFields<MutationUpdateMatchmakingQueueByIdArgs, 'input'>>;
  updateMatchmakingTicket?: Resolver<Maybe<ResolversTypes['UpdateMatchmakingTicketPayload']>, ParentType, ContextType, RequireFields<MutationUpdateMatchmakingTicketArgs, 'input'>>;
  updateMatchmakingTicketByUserId?: Resolver<Maybe<ResolversTypes['UpdateMatchmakingTicketPayload']>, ParentType, ContextType, RequireFields<MutationUpdateMatchmakingTicketByUserIdArgs, 'input'>>;
  updateUserEntityAddon?: Resolver<Maybe<ResolversTypes['UpdateUserEntityAddonPayload']>, ParentType, ContextType, RequireFields<MutationUpdateUserEntityAddonArgs, 'input'>>;
  updateUserEntityAddonById?: Resolver<Maybe<ResolversTypes['UpdateUserEntityAddonPayload']>, ParentType, ContextType, RequireFields<MutationUpdateUserEntityAddonByIdArgs, 'input'>>;
  upsertBannedDraftCard?: Resolver<Maybe<ResolversTypes['UpsertBannedDraftCardPayload']>, ParentType, ContextType, RequireFields<MutationUpsertBannedDraftCardArgs, 'input'>>;
  upsertBotUser?: Resolver<Maybe<ResolversTypes['UpsertBotUserPayload']>, ParentType, ContextType, RequireFields<MutationUpsertBotUserArgs, 'input'>>;
  upsertCardsInDeck?: Resolver<Maybe<ResolversTypes['UpsertCardsInDeckPayload']>, ParentType, ContextType, RequireFields<MutationUpsertCardsInDeckArgs, 'input'>>;
  upsertDeck?: Resolver<Maybe<ResolversTypes['UpsertDeckPayload']>, ParentType, ContextType, RequireFields<MutationUpsertDeckArgs, 'input'>>;
  upsertDeckPlayerAttributeTuple?: Resolver<Maybe<ResolversTypes['UpsertDeckPlayerAttributeTuplePayload']>, ParentType, ContextType, RequireFields<MutationUpsertDeckPlayerAttributeTupleArgs, 'input'>>;
  upsertDeckShare?: Resolver<Maybe<ResolversTypes['UpsertDeckSharePayload']>, ParentType, ContextType, RequireFields<MutationUpsertDeckShareArgs, 'input'>>;
  upsertFriend?: Resolver<Maybe<ResolversTypes['UpsertFriendPayload']>, ParentType, ContextType, RequireFields<MutationUpsertFriendArgs, 'input'>>;
  upsertGame?: Resolver<Maybe<ResolversTypes['UpsertGamePayload']>, ParentType, ContextType, RequireFields<MutationUpsertGameArgs, 'input'>>;
  upsertGameUser?: Resolver<Maybe<ResolversTypes['UpsertGameUserPayload']>, ParentType, ContextType, RequireFields<MutationUpsertGameUserArgs, 'input'>>;
  upsertGuest?: Resolver<Maybe<ResolversTypes['UpsertGuestPayload']>, ParentType, ContextType, RequireFields<MutationUpsertGuestArgs, 'input'>>;
  upsertHardRemovalCard?: Resolver<Maybe<ResolversTypes['UpsertHardRemovalCardPayload']>, ParentType, ContextType, RequireFields<MutationUpsertHardRemovalCardArgs, 'input'>>;
  upsertMatchmakingQueue?: Resolver<Maybe<ResolversTypes['UpsertMatchmakingQueuePayload']>, ParentType, ContextType, RequireFields<MutationUpsertMatchmakingQueueArgs, 'input'>>;
  upsertMatchmakingTicket?: Resolver<Maybe<ResolversTypes['UpsertMatchmakingTicketPayload']>, ParentType, ContextType, RequireFields<MutationUpsertMatchmakingTicketArgs, 'input'>>;
  upsertUserEntityAddon?: Resolver<Maybe<ResolversTypes['UpsertUserEntityAddonPayload']>, ParentType, ContextType, RequireFields<MutationUpsertUserEntityAddonArgs, 'input'>>;
};

export type NodeResolvers<ContextType = any, ParentType extends ResolversParentTypes['Node'] = ResolversParentTypes['Node']> = {
  __resolveType: TypeResolveFn<'BannedDraftCard' | 'BotUser' | 'CardsInDeck' | 'Deck' | 'DeckPlayerAttributeTuple' | 'DeckShare' | 'Friend' | 'Game' | 'GameUser' | 'Guest' | 'HardRemovalCard' | 'MatchmakingQueue' | 'MatchmakingTicket' | 'Query' | 'UserEntityAddon', ParentType, ContextType>;
  nodeId?: Resolver<ResolversTypes['ID'], ParentType, ContextType>;
};

export type PageInfoResolvers<ContextType = any, ParentType extends ResolversParentTypes['PageInfo'] = ResolversParentTypes['PageInfo']> = {
  endCursor?: Resolver<Maybe<ResolversTypes['Cursor']>, ParentType, ContextType>;
  hasNextPage?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  hasPreviousPage?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  startCursor?: Resolver<Maybe<ResolversTypes['Cursor']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type PublishCardPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['PublishCardPayload'] = ResolversParentTypes['PublishCardPayload']> = {
  bigInt?: Resolver<Maybe<ResolversTypes['BigInt']>, ParentType, ContextType>;
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type QueryResolvers<ContextType = any, ParentType extends ResolversParentTypes['Query'] = ResolversParentTypes['Query']> = {
  allArt?: Resolver<Array<ResolversTypes['ImageDef']>, ParentType, ContextType>;
  allBannedDraftCards?: Resolver<Maybe<ResolversTypes['BannedDraftCardsConnection']>, ParentType, ContextType, RequireFields<QueryAllBannedDraftCardsArgs, 'orderBy'>>;
  allBotUsers?: Resolver<Maybe<ResolversTypes['BotUsersConnection']>, ParentType, ContextType, RequireFields<QueryAllBotUsersArgs, 'orderBy'>>;
  allCards?: Resolver<Maybe<ResolversTypes['CardsConnection']>, ParentType, ContextType, RequireFields<QueryAllCardsArgs, 'includeArchived' | 'orderBy'>>;
  allCardsInDecks?: Resolver<Maybe<ResolversTypes['CardsInDecksConnection']>, ParentType, ContextType, RequireFields<QueryAllCardsInDecksArgs, 'orderBy'>>;
  allClasses?: Resolver<Maybe<ResolversTypes['ClassesConnection']>, ParentType, ContextType, RequireFields<QueryAllClassesArgs, 'orderBy'>>;
  allCollectionCards?: Resolver<Maybe<ResolversTypes['CollectionCardsConnection']>, ParentType, ContextType, RequireFields<QueryAllCollectionCardsArgs, 'orderBy'>>;
  allCurrentCards?: Resolver<Maybe<ResolversTypes['CurrentCardsConnection']>, ParentType, ContextType, RequireFields<QueryAllCurrentCardsArgs, 'includeArchived' | 'orderBy'>>;
  allDeckPlayerAttributeTuples?: Resolver<Maybe<ResolversTypes['DeckPlayerAttributeTuplesConnection']>, ParentType, ContextType, RequireFields<QueryAllDeckPlayerAttributeTuplesArgs, 'orderBy'>>;
  allDeckShares?: Resolver<Maybe<ResolversTypes['DeckSharesConnection']>, ParentType, ContextType, RequireFields<QueryAllDeckSharesArgs, 'orderBy'>>;
  allDecks?: Resolver<Maybe<ResolversTypes['DecksConnection']>, ParentType, ContextType, RequireFields<QueryAllDecksArgs, 'orderBy'>>;
  allFriends?: Resolver<Maybe<ResolversTypes['FriendsConnection']>, ParentType, ContextType, RequireFields<QueryAllFriendsArgs, 'orderBy'>>;
  allGameUsers?: Resolver<Maybe<ResolversTypes['GameUsersConnection']>, ParentType, ContextType, RequireFields<QueryAllGameUsersArgs, 'orderBy'>>;
  allGames?: Resolver<Maybe<ResolversTypes['GamesConnection']>, ParentType, ContextType, RequireFields<QueryAllGamesArgs, 'orderBy'>>;
  allGuests?: Resolver<Maybe<ResolversTypes['GuestsConnection']>, ParentType, ContextType, RequireFields<QueryAllGuestsArgs, 'orderBy'>>;
  allHardRemovalCards?: Resolver<Maybe<ResolversTypes['HardRemovalCardsConnection']>, ParentType, ContextType, RequireFields<QueryAllHardRemovalCardsArgs, 'orderBy'>>;
  allMatchmakingQueues?: Resolver<Maybe<ResolversTypes['MatchmakingQueuesConnection']>, ParentType, ContextType, RequireFields<QueryAllMatchmakingQueuesArgs, 'orderBy'>>;
  allMatchmakingTickets?: Resolver<Maybe<ResolversTypes['MatchmakingTicketsConnection']>, ParentType, ContextType, RequireFields<QueryAllMatchmakingTicketsArgs, 'orderBy'>>;
  allUserEntityAddons?: Resolver<Maybe<ResolversTypes['UserEntityAddonsConnection']>, ParentType, ContextType, RequireFields<QueryAllUserEntityAddonsArgs, 'orderBy'>>;
  artById?: Resolver<Maybe<ResolversTypes['ImageDef']>, ParentType, ContextType, RequireFields<QueryArtByIdArgs, 'id'>>;
  bannedDraftCard?: Resolver<Maybe<ResolversTypes['BannedDraftCard']>, ParentType, ContextType, RequireFields<QueryBannedDraftCardArgs, 'nodeId'>>;
  bannedDraftCardByCardId?: Resolver<Maybe<ResolversTypes['BannedDraftCard']>, ParentType, ContextType, RequireFields<QueryBannedDraftCardByCardIdArgs, 'cardId'>>;
  botUser?: Resolver<Maybe<ResolversTypes['BotUser']>, ParentType, ContextType, RequireFields<QueryBotUserArgs, 'nodeId'>>;
  botUserById?: Resolver<Maybe<ResolversTypes['BotUser']>, ParentType, ContextType, RequireFields<QueryBotUserByIdArgs, 'id'>>;
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
  getLatestCard?: Resolver<Maybe<ResolversTypes['Card']>, ParentType, ContextType, Partial<QueryGetLatestCardArgs>>;
  getUserId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  guest?: Resolver<Maybe<ResolversTypes['Guest']>, ParentType, ContextType, RequireFields<QueryGuestArgs, 'nodeId'>>;
  guestById?: Resolver<Maybe<ResolversTypes['Guest']>, ParentType, ContextType, RequireFields<QueryGuestByIdArgs, 'id'>>;
  hardRemovalCard?: Resolver<Maybe<ResolversTypes['HardRemovalCard']>, ParentType, ContextType, RequireFields<QueryHardRemovalCardArgs, 'nodeId'>>;
  hardRemovalCardByCardId?: Resolver<Maybe<ResolversTypes['HardRemovalCard']>, ParentType, ContextType, RequireFields<QueryHardRemovalCardByCardIdArgs, 'cardId'>>;
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

export type SaveCardPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['SaveCardPayload'] = ResolversParentTypes['SaveCardPayload']> = {
  card?: Resolver<Maybe<ResolversTypes['Card']>, ParentType, ContextType>;
  cardEdge?: Resolver<Maybe<ResolversTypes['CardsEdge']>, ParentType, ContextType, RequireFields<SaveCardPayloadCardEdgeArgs, 'orderBy'>>;
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type SetCardsInDeckPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['SetCardsInDeckPayload'] = ResolversParentTypes['SetCardsInDeckPayload']> = {
  cardsInDecks?: Resolver<Maybe<Array<Maybe<ResolversTypes['CardsInDeck']>>>, ParentType, ContextType>;
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type UpdateBannedDraftCardPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['UpdateBannedDraftCardPayload'] = ResolversParentTypes['UpdateBannedDraftCardPayload']> = {
  bannedDraftCard?: Resolver<Maybe<ResolversTypes['BannedDraftCard']>, ParentType, ContextType>;
  bannedDraftCardEdge?: Resolver<Maybe<ResolversTypes['BannedDraftCardsEdge']>, ParentType, ContextType, RequireFields<UpdateBannedDraftCardPayloadBannedDraftCardEdgeArgs, 'orderBy'>>;
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type UpdateBotUserPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['UpdateBotUserPayload'] = ResolversParentTypes['UpdateBotUserPayload']> = {
  botUser?: Resolver<Maybe<ResolversTypes['BotUser']>, ParentType, ContextType>;
  botUserEdge?: Resolver<Maybe<ResolversTypes['BotUsersEdge']>, ParentType, ContextType, RequireFields<UpdateBotUserPayloadBotUserEdgeArgs, 'orderBy'>>;
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type UpdateCardsInDeckPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['UpdateCardsInDeckPayload'] = ResolversParentTypes['UpdateCardsInDeckPayload']> = {
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

export type UpdateHardRemovalCardPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['UpdateHardRemovalCardPayload'] = ResolversParentTypes['UpdateHardRemovalCardPayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  hardRemovalCard?: Resolver<Maybe<ResolversTypes['HardRemovalCard']>, ParentType, ContextType>;
  hardRemovalCardEdge?: Resolver<Maybe<ResolversTypes['HardRemovalCardsEdge']>, ParentType, ContextType, RequireFields<UpdateHardRemovalCardPayloadHardRemovalCardEdgeArgs, 'orderBy'>>;
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

export type UpsertBannedDraftCardPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['UpsertBannedDraftCardPayload'] = ResolversParentTypes['UpsertBannedDraftCardPayload']> = {
  bannedDraftCard?: Resolver<Maybe<ResolversTypes['BannedDraftCard']>, ParentType, ContextType>;
  bannedDraftCardEdge?: Resolver<Maybe<ResolversTypes['BannedDraftCardsEdge']>, ParentType, ContextType, RequireFields<UpsertBannedDraftCardPayloadBannedDraftCardEdgeArgs, 'orderBy'>>;
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type UpsertBotUserPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['UpsertBotUserPayload'] = ResolversParentTypes['UpsertBotUserPayload']> = {
  botUser?: Resolver<Maybe<ResolversTypes['BotUser']>, ParentType, ContextType>;
  botUserEdge?: Resolver<Maybe<ResolversTypes['BotUsersEdge']>, ParentType, ContextType, RequireFields<UpsertBotUserPayloadBotUserEdgeArgs, 'orderBy'>>;
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type UpsertCardsInDeckPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['UpsertCardsInDeckPayload'] = ResolversParentTypes['UpsertCardsInDeckPayload']> = {
  cardsInDeck?: Resolver<Maybe<ResolversTypes['CardsInDeck']>, ParentType, ContextType>;
  cardsInDeckEdge?: Resolver<Maybe<ResolversTypes['CardsInDecksEdge']>, ParentType, ContextType, RequireFields<UpsertCardsInDeckPayloadCardsInDeckEdgeArgs, 'orderBy'>>;
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  deckByDeckId?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type UpsertDeckPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['UpsertDeckPayload'] = ResolversParentTypes['UpsertDeckPayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  deck?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  deckEdge?: Resolver<Maybe<ResolversTypes['DecksEdge']>, ParentType, ContextType, RequireFields<UpsertDeckPayloadDeckEdgeArgs, 'orderBy'>>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type UpsertDeckPlayerAttributeTuplePayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['UpsertDeckPlayerAttributeTuplePayload'] = ResolversParentTypes['UpsertDeckPlayerAttributeTuplePayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  deckByDeckId?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  deckPlayerAttributeTuple?: Resolver<Maybe<ResolversTypes['DeckPlayerAttributeTuple']>, ParentType, ContextType>;
  deckPlayerAttributeTupleEdge?: Resolver<Maybe<ResolversTypes['DeckPlayerAttributeTuplesEdge']>, ParentType, ContextType, RequireFields<UpsertDeckPlayerAttributeTuplePayloadDeckPlayerAttributeTupleEdgeArgs, 'orderBy'>>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type UpsertDeckSharePayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['UpsertDeckSharePayload'] = ResolversParentTypes['UpsertDeckSharePayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  deckByDeckId?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  deckShare?: Resolver<Maybe<ResolversTypes['DeckShare']>, ParentType, ContextType>;
  deckShareEdge?: Resolver<Maybe<ResolversTypes['DeckSharesEdge']>, ParentType, ContextType, RequireFields<UpsertDeckSharePayloadDeckShareEdgeArgs, 'orderBy'>>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type UpsertFriendPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['UpsertFriendPayload'] = ResolversParentTypes['UpsertFriendPayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  friend?: Resolver<Maybe<ResolversTypes['Friend']>, ParentType, ContextType>;
  friendEdge?: Resolver<Maybe<ResolversTypes['FriendsEdge']>, ParentType, ContextType, RequireFields<UpsertFriendPayloadFriendEdgeArgs, 'orderBy'>>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type UpsertGamePayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['UpsertGamePayload'] = ResolversParentTypes['UpsertGamePayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  game?: Resolver<Maybe<ResolversTypes['Game']>, ParentType, ContextType>;
  gameEdge?: Resolver<Maybe<ResolversTypes['GamesEdge']>, ParentType, ContextType, RequireFields<UpsertGamePayloadGameEdgeArgs, 'orderBy'>>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type UpsertGameUserPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['UpsertGameUserPayload'] = ResolversParentTypes['UpsertGameUserPayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  deckByDeckId?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  gameByGameId?: Resolver<Maybe<ResolversTypes['Game']>, ParentType, ContextType>;
  gameUser?: Resolver<Maybe<ResolversTypes['GameUser']>, ParentType, ContextType>;
  gameUserEdge?: Resolver<Maybe<ResolversTypes['GameUsersEdge']>, ParentType, ContextType, RequireFields<UpsertGameUserPayloadGameUserEdgeArgs, 'orderBy'>>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type UpsertGuestPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['UpsertGuestPayload'] = ResolversParentTypes['UpsertGuestPayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  guest?: Resolver<Maybe<ResolversTypes['Guest']>, ParentType, ContextType>;
  guestEdge?: Resolver<Maybe<ResolversTypes['GuestsEdge']>, ParentType, ContextType, RequireFields<UpsertGuestPayloadGuestEdgeArgs, 'orderBy'>>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type UpsertHardRemovalCardPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['UpsertHardRemovalCardPayload'] = ResolversParentTypes['UpsertHardRemovalCardPayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  hardRemovalCard?: Resolver<Maybe<ResolversTypes['HardRemovalCard']>, ParentType, ContextType>;
  hardRemovalCardEdge?: Resolver<Maybe<ResolversTypes['HardRemovalCardsEdge']>, ParentType, ContextType, RequireFields<UpsertHardRemovalCardPayloadHardRemovalCardEdgeArgs, 'orderBy'>>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type UpsertMatchmakingQueuePayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['UpsertMatchmakingQueuePayload'] = ResolversParentTypes['UpsertMatchmakingQueuePayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  matchmakingQueue?: Resolver<Maybe<ResolversTypes['MatchmakingQueue']>, ParentType, ContextType>;
  matchmakingQueueEdge?: Resolver<Maybe<ResolversTypes['MatchmakingQueuesEdge']>, ParentType, ContextType, RequireFields<UpsertMatchmakingQueuePayloadMatchmakingQueueEdgeArgs, 'orderBy'>>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type UpsertMatchmakingTicketPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['UpsertMatchmakingTicketPayload'] = ResolversParentTypes['UpsertMatchmakingTicketPayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  deckByBotDeckId?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  deckByDeckId?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  matchmakingQueueByQueueId?: Resolver<Maybe<ResolversTypes['MatchmakingQueue']>, ParentType, ContextType>;
  matchmakingTicket?: Resolver<Maybe<ResolversTypes['MatchmakingTicket']>, ParentType, ContextType>;
  matchmakingTicketEdge?: Resolver<Maybe<ResolversTypes['MatchmakingTicketsEdge']>, ParentType, ContextType, RequireFields<UpsertMatchmakingTicketPayloadMatchmakingTicketEdgeArgs, 'orderBy'>>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type UpsertUserEntityAddonPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['UpsertUserEntityAddonPayload'] = ResolversParentTypes['UpsertUserEntityAddonPayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  userEntityAddon?: Resolver<Maybe<ResolversTypes['UserEntityAddon']>, ParentType, ContextType>;
  userEntityAddonEdge?: Resolver<Maybe<ResolversTypes['UserEntityAddonsEdge']>, ParentType, ContextType, RequireFields<UpsertUserEntityAddonPayloadUserEntityAddonEdgeArgs, 'orderBy'>>;
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
  ArchiveCardPayload?: ArchiveCardPayloadResolvers<ContextType>;
  BannedDraftCard?: BannedDraftCardResolvers<ContextType>;
  BannedDraftCardsConnection?: BannedDraftCardsConnectionResolvers<ContextType>;
  BannedDraftCardsEdge?: BannedDraftCardsEdgeResolvers<ContextType>;
  BigInt?: GraphQLScalarType;
  BotUser?: BotUserResolvers<ContextType>;
  BotUsersConnection?: BotUsersConnectionResolvers<ContextType>;
  BotUsersEdge?: BotUsersEdgeResolvers<ContextType>;
  Card?: CardResolvers<ContextType>;
  CardCatalogueFormatsPayload?: CardCatalogueFormatsPayloadResolvers<ContextType>;
  CardCatalogueGetBannedDraftCardsPayload?: CardCatalogueGetBannedDraftCardsPayloadResolvers<ContextType>;
  CardCatalogueGetBaseClassesPayload?: CardCatalogueGetBaseClassesPayloadResolvers<ContextType>;
  CardCatalogueGetCardByIdPayload?: CardCatalogueGetCardByIdPayloadResolvers<ContextType>;
  CardCatalogueGetCardByNameAndClassPayload?: CardCatalogueGetCardByNameAndClassPayloadResolvers<ContextType>;
  CardCatalogueGetCardByNamePayload?: CardCatalogueGetCardByNamePayloadResolvers<ContextType>;
  CardCatalogueGetClassCardsPayload?: CardCatalogueGetClassCardsPayloadResolvers<ContextType>;
  CardCatalogueGetFormatPayload?: CardCatalogueGetFormatPayloadResolvers<ContextType>;
  CardCatalogueGetHardRemovalCardsPayload?: CardCatalogueGetHardRemovalCardsPayloadResolvers<ContextType>;
  CardCatalogueGetHeroCardPayload?: CardCatalogueGetHeroCardPayloadResolvers<ContextType>;
  CardCatalogueQueryPayload?: CardCatalogueQueryPayloadResolvers<ContextType>;
  CardsConnection?: CardsConnectionResolvers<ContextType>;
  CardsEdge?: CardsEdgeResolvers<ContextType>;
  CardsInDeck?: CardsInDeckResolvers<ContextType>;
  CardsInDecksConnection?: CardsInDecksConnectionResolvers<ContextType>;
  CardsInDecksEdge?: CardsInDecksEdgeResolvers<ContextType>;
  Class?: ClassResolvers<ContextType>;
  ClassesConnection?: ClassesConnectionResolvers<ContextType>;
  ClassesEdge?: ClassesEdgeResolvers<ContextType>;
  ClusteredGamesUpdateGameAndUsersPayload?: ClusteredGamesUpdateGameAndUsersPayloadResolvers<ContextType>;
  CollectionCard?: CollectionCardResolvers<ContextType>;
  CollectionCardsConnection?: CollectionCardsConnectionResolvers<ContextType>;
  CollectionCardsEdge?: CollectionCardsEdgeResolvers<ContextType>;
  CreateBannedDraftCardPayload?: CreateBannedDraftCardPayloadResolvers<ContextType>;
  CreateBotUserPayload?: CreateBotUserPayloadResolvers<ContextType>;
  CreateCardPayload?: CreateCardPayloadResolvers<ContextType>;
  CreateCardsInDeckPayload?: CreateCardsInDeckPayloadResolvers<ContextType>;
  CreateDeckPayload?: CreateDeckPayloadResolvers<ContextType>;
  CreateDeckPlayerAttributeTuplePayload?: CreateDeckPlayerAttributeTuplePayloadResolvers<ContextType>;
  CreateDeckSharePayload?: CreateDeckSharePayloadResolvers<ContextType>;
  CreateDeckWithCardsPayload?: CreateDeckWithCardsPayloadResolvers<ContextType>;
  CreateFriendPayload?: CreateFriendPayloadResolvers<ContextType>;
  CreateGamePayload?: CreateGamePayloadResolvers<ContextType>;
  CreateGameUserPayload?: CreateGameUserPayloadResolvers<ContextType>;
  CreateGuestPayload?: CreateGuestPayloadResolvers<ContextType>;
  CreateHardRemovalCardPayload?: CreateHardRemovalCardPayloadResolvers<ContextType>;
  CreateMatchmakingQueuePayload?: CreateMatchmakingQueuePayloadResolvers<ContextType>;
  CreateMatchmakingTicketPayload?: CreateMatchmakingTicketPayloadResolvers<ContextType>;
  CreateUserEntityAddonPayload?: CreateUserEntityAddonPayloadResolvers<ContextType>;
  CurrentCard?: CurrentCardResolvers<ContextType>;
  CurrentCardsConnection?: CurrentCardsConnectionResolvers<ContextType>;
  CurrentCardsEdge?: CurrentCardsEdgeResolvers<ContextType>;
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
  DeleteBannedDraftCardPayload?: DeleteBannedDraftCardPayloadResolvers<ContextType>;
  DeleteBotUserPayload?: DeleteBotUserPayloadResolvers<ContextType>;
  DeleteCardsInDeckPayload?: DeleteCardsInDeckPayloadResolvers<ContextType>;
  DeleteDeckPayload?: DeleteDeckPayloadResolvers<ContextType>;
  DeleteDeckPlayerAttributeTuplePayload?: DeleteDeckPlayerAttributeTuplePayloadResolvers<ContextType>;
  DeleteDeckSharePayload?: DeleteDeckSharePayloadResolvers<ContextType>;
  DeleteFriendPayload?: DeleteFriendPayloadResolvers<ContextType>;
  DeleteGamePayload?: DeleteGamePayloadResolvers<ContextType>;
  DeleteGameUserPayload?: DeleteGameUserPayloadResolvers<ContextType>;
  DeleteGuestPayload?: DeleteGuestPayloadResolvers<ContextType>;
  DeleteHardRemovalCardPayload?: DeleteHardRemovalCardPayloadResolvers<ContextType>;
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
  GetClassesPayload?: GetClassesPayloadResolvers<ContextType>;
  GetClassesRecord?: GetClassesRecordResolvers<ContextType>;
  GetCollectionCardsPayload?: GetCollectionCardsPayloadResolvers<ContextType>;
  GetCollectionCardsRecord?: GetCollectionCardsRecordResolvers<ContextType>;
  Guest?: GuestResolvers<ContextType>;
  GuestsConnection?: GuestsConnectionResolvers<ContextType>;
  GuestsEdge?: GuestsEdgeResolvers<ContextType>;
  HardRemovalCard?: HardRemovalCardResolvers<ContextType>;
  HardRemovalCardsConnection?: HardRemovalCardsConnectionResolvers<ContextType>;
  HardRemovalCardsEdge?: HardRemovalCardsEdgeResolvers<ContextType>;
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
  PublishCardPayload?: PublishCardPayloadResolvers<ContextType>;
  Query?: QueryResolvers<ContextType>;
  SaveCardPayload?: SaveCardPayloadResolvers<ContextType>;
  SetCardsInDeckPayload?: SetCardsInDeckPayloadResolvers<ContextType>;
  UpdateBannedDraftCardPayload?: UpdateBannedDraftCardPayloadResolvers<ContextType>;
  UpdateBotUserPayload?: UpdateBotUserPayloadResolvers<ContextType>;
  UpdateCardsInDeckPayload?: UpdateCardsInDeckPayloadResolvers<ContextType>;
  UpdateDeckPayload?: UpdateDeckPayloadResolvers<ContextType>;
  UpdateDeckPlayerAttributeTuplePayload?: UpdateDeckPlayerAttributeTuplePayloadResolvers<ContextType>;
  UpdateDeckSharePayload?: UpdateDeckSharePayloadResolvers<ContextType>;
  UpdateFriendPayload?: UpdateFriendPayloadResolvers<ContextType>;
  UpdateGamePayload?: UpdateGamePayloadResolvers<ContextType>;
  UpdateGameUserPayload?: UpdateGameUserPayloadResolvers<ContextType>;
  UpdateGuestPayload?: UpdateGuestPayloadResolvers<ContextType>;
  UpdateHardRemovalCardPayload?: UpdateHardRemovalCardPayloadResolvers<ContextType>;
  UpdateMatchmakingQueuePayload?: UpdateMatchmakingQueuePayloadResolvers<ContextType>;
  UpdateMatchmakingTicketPayload?: UpdateMatchmakingTicketPayloadResolvers<ContextType>;
  UpdateUserEntityAddonPayload?: UpdateUserEntityAddonPayloadResolvers<ContextType>;
  UpsertBannedDraftCardPayload?: UpsertBannedDraftCardPayloadResolvers<ContextType>;
  UpsertBotUserPayload?: UpsertBotUserPayloadResolvers<ContextType>;
  UpsertCardsInDeckPayload?: UpsertCardsInDeckPayloadResolvers<ContextType>;
  UpsertDeckPayload?: UpsertDeckPayloadResolvers<ContextType>;
  UpsertDeckPlayerAttributeTuplePayload?: UpsertDeckPlayerAttributeTuplePayloadResolvers<ContextType>;
  UpsertDeckSharePayload?: UpsertDeckSharePayloadResolvers<ContextType>;
  UpsertFriendPayload?: UpsertFriendPayloadResolvers<ContextType>;
  UpsertGamePayload?: UpsertGamePayloadResolvers<ContextType>;
  UpsertGameUserPayload?: UpsertGameUserPayloadResolvers<ContextType>;
  UpsertGuestPayload?: UpsertGuestPayloadResolvers<ContextType>;
  UpsertHardRemovalCardPayload?: UpsertHardRemovalCardPayloadResolvers<ContextType>;
  UpsertMatchmakingQueuePayload?: UpsertMatchmakingQueuePayloadResolvers<ContextType>;
  UpsertMatchmakingTicketPayload?: UpsertMatchmakingTicketPayloadResolvers<ContextType>;
  UpsertUserEntityAddonPayload?: UpsertUserEntityAddonPayloadResolvers<ContextType>;
  UserEntityAddon?: UserEntityAddonResolvers<ContextType>;
  UserEntityAddonsConnection?: UserEntityAddonsConnectionResolvers<ContextType>;
  UserEntityAddonsEdge?: UserEntityAddonsEdgeResolvers<ContextType>;
};


export type CardFragment = { __typename?: 'Card', id: string, createdBy: string, cardScript?: any | null, blocklyWorkspace?: string | null };

export type ClassFragment = { __typename?: 'Class', class?: string | null, collectible?: boolean | null, isPublished?: boolean | null, cardScript?: any | null, id?: string | null, name?: string | null };

export type CollectionCardFragment = { __typename?: 'CollectionCard', id?: string | null, createdBy?: string | null, cardScript?: any | null, blocklyWorkspace?: string | null, collectible?: boolean | null, cost?: number | null, type?: string | null, lastModified?: any | null };

export type DeckFragment = { __typename?: 'Deck', id: string, name?: string | null, isPremade: boolean, createdBy: string, heroClass?: string | null, format?: string | null };

export type DeckCardIdsFragment = { __typename?: 'Deck', cardsInDecksByDeckId: { __typename?: 'CardsInDecksConnection', totalCount: number, nodes: Array<{ __typename?: 'CardsInDeck', cardId: string } | null> } };

export type DeckCardsFragment = { __typename?: 'Deck', cardsInDecksByDeckId: { __typename?: 'CardsInDecksConnection', totalCount: number, nodes: Array<{ __typename?: 'CardsInDeck', cardId: string, cardByCardId?: { __typename?: 'Card', id: string, createdBy: string, cardScript?: any | null, blocklyWorkspace?: string | null } | null } | null> } };

export type ImageFragment = { __typename?: 'ImageDef', id: string, name: string, height: number, width: number, src: string };

export type CreateDeckMutationVariables = Exact<{
  deckName: Scalars['String'];
  heroClass: Scalars['String'];
  cardIds?: InputMaybe<Array<Scalars['String']> | Scalars['String']>;
  format: Scalars['String'];
}>;


export type CreateDeckMutation = { __typename?: 'Mutation', createDeckWithCards?: { __typename?: 'CreateDeckWithCardsPayload', deck?: { __typename?: 'Deck', id: string, name?: string | null, isPremade: boolean, createdBy: string, heroClass?: string | null, format?: string | null, cardsInDecksByDeckId: { __typename?: 'CardsInDecksConnection', totalCount: number, nodes: Array<{ __typename?: 'CardsInDeck', cardId: string, cardByCardId?: { __typename?: 'Card', id: string, createdBy: string, cardScript?: any | null, blocklyWorkspace?: string | null } | null } | null> } } | null } | null };

export type DeleteCardMutationVariables = Exact<{
  cardId: Scalars['String'];
}>;


export type DeleteCardMutation = { __typename?: 'Mutation', archiveCard?: { __typename?: 'ArchiveCardPayload', clientMutationId?: string | null } | null };

export type DeleteDeckMutationVariables = Exact<{
  deckId: Scalars['String'];
}>;


export type DeleteDeckMutation = { __typename?: 'Mutation', updateDeckById?: { __typename?: 'UpdateDeckPayload', deck?: { __typename?: 'Deck', trashed: boolean } | null } | null };

export type PublishCardMutationVariables = Exact<{
  cardId: Scalars['String'];
}>;


export type PublishCardMutation = { __typename?: 'Mutation', publishCard?: { __typename?: 'PublishCardPayload', bigInt?: any | null } | null };

export type RenameDeckMutationVariables = Exact<{
  deckId: Scalars['String'];
  deckName: Scalars['String'];
}>;


export type RenameDeckMutation = { __typename?: 'Mutation', updateDeckById?: { __typename?: 'UpdateDeckPayload', deck?: { __typename?: 'Deck', id: string, name?: string | null } | null } | null };

export type SaveCardMutationVariables = Exact<{
  cardId: Scalars['String'];
  blocklyWorkspace: Scalars['String'];
  cardScript?: InputMaybe<Scalars['JSON']>;
}>;


export type SaveCardMutation = { __typename?: 'Mutation', saveCard?: { __typename?: 'SaveCardPayload', card?: { __typename?: 'Card', id: string, createdBy: string, cardScript?: any | null, blocklyWorkspace?: string | null } | null } | null };

export type SetCardsInDeckMutationVariables = Exact<{
  deckId: Scalars['String'];
  cardIds?: InputMaybe<Array<Scalars['String']> | Scalars['String']>;
}>;


export type SetCardsInDeckMutation = { __typename?: 'Mutation', setCardsInDeck?: { __typename?: 'SetCardsInDeckPayload', cardsInDecks?: Array<{ __typename?: 'CardsInDeck', id: any, cardId: string } | null> | null } | null };

export type GetAllArtQueryVariables = Exact<{ [key: string]: never; }>;


export type GetAllArtQuery = { __typename?: 'Query', allArt: Array<{ __typename?: 'ImageDef', id: string, name: string, height: number, width: number, src: string }> };

export type GetArtQueryVariables = Exact<{
  id: Scalars['String'];
}>;


export type GetArtQuery = { __typename?: 'Query', artById?: { __typename?: 'ImageDef', id: string, name: string, height: number, width: number, src: string } | null };

export type GetCardQueryVariables = Exact<{
  id: Scalars['String'];
}>;


export type GetCardQuery = { __typename?: 'Query', getLatestCard?: { __typename?: 'Card', id: string, createdBy: string, cardScript?: any | null, blocklyWorkspace?: string | null } | null };

export type GetCardsQueryVariables = Exact<{
  limit?: InputMaybe<Scalars['Int']>;
  filter?: InputMaybe<CardFilter>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<CardsOrderBy> | CardsOrderBy>;
}>;


export type GetCardsQuery = { __typename?: 'Query', allCards?: { __typename?: 'CardsConnection', totalCount: number, nodes: Array<{ __typename?: 'Card', id: string, createdBy: string, cardScript?: any | null, blocklyWorkspace?: string | null } | null> } | null };

export type GetClassesQueryVariables = Exact<{
  filter?: InputMaybe<ClassFilter>;
}>;


export type GetClassesQuery = { __typename?: 'Query', allClasses?: { __typename?: 'ClassesConnection', totalCount: number, nodes: Array<{ __typename?: 'Class', class?: string | null, collectible?: boolean | null, isPublished?: boolean | null, cardScript?: any | null, id?: string | null, name?: string | null } | null> } | null };

export type GetCollectionCardsQueryVariables = Exact<{
  limit?: InputMaybe<Scalars['Int']>;
  filter?: InputMaybe<CollectionCardFilter>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<CollectionCardsOrderBy> | CollectionCardsOrderBy>;
}>;


export type GetCollectionCardsQuery = { __typename?: 'Query', allCollectionCards?: { __typename?: 'CollectionCardsConnection', totalCount: number, nodes: Array<{ __typename?: 'CollectionCard', id?: string | null, createdBy?: string | null, cardScript?: any | null, blocklyWorkspace?: string | null, collectible?: boolean | null, cost?: number | null, type?: string | null, lastModified?: any | null } | null> } | null };

export type GetDeckQueryVariables = Exact<{
  deckId: Scalars['String'];
}>;


export type GetDeckQuery = { __typename?: 'Query', deckById?: { __typename?: 'Deck', id: string, name?: string | null, isPremade: boolean, createdBy: string, heroClass?: string | null, format?: string | null, cardsInDecksByDeckId: { __typename?: 'CardsInDecksConnection', totalCount: number, nodes: Array<{ __typename?: 'CardsInDeck', cardId: string, cardByCardId?: { __typename?: 'Card', id: string, createdBy: string, cardScript?: any | null, blocklyWorkspace?: string | null } | null } | null> } } | null };

export type GetDecksQueryVariables = Exact<{
  user?: InputMaybe<Scalars['String']>;
}>;


export type GetDecksQuery = { __typename?: 'Query', allDecks?: { __typename?: 'DecksConnection', nodes: Array<{ __typename?: 'Deck', id: string, name?: string | null, isPremade: boolean, createdBy: string, heroClass?: string | null, format?: string | null } | null> } | null, allDeckShares?: { __typename?: 'DeckSharesConnection', nodes: Array<{ __typename?: 'DeckShare', deckByDeckId?: { __typename?: 'Deck', id: string, name?: string | null, isPremade: boolean, createdBy: string, heroClass?: string | null, format?: string | null } | null } | null> } | null };

export const ClassFragmentDoc = gql`
    fragment class on Class {
  class
  collectible
  isPublished
  cardScript
  id
  name
}
    `;
export const CollectionCardFragmentDoc = gql`
    fragment collectionCard on CollectionCard {
  id
  createdBy
  cardScript
  blocklyWorkspace
  collectible
  cost
  type
  lastModified
  createdBy
}
    `;
export const DeckFragmentDoc = gql`
    fragment deck on Deck {
  id
  name
  isPremade
  createdBy
  heroClass
  format
}
    `;
export const DeckCardIdsFragmentDoc = gql`
    fragment deckCardIds on Deck {
  cardsInDecksByDeckId {
    nodes {
      cardId
    }
    totalCount
  }
}
    `;
export const CardFragmentDoc = gql`
    fragment card on Card {
  id
  createdBy
  cardScript
  blocklyWorkspace
}
    `;
export const DeckCardsFragmentDoc = gql`
    fragment deckCards on Deck {
  cardsInDecksByDeckId {
    nodes {
      cardId
      cardByCardId {
        ...card
      }
    }
    totalCount
  }
}
    ${CardFragmentDoc}`;
export const ImageFragmentDoc = gql`
    fragment image on ImageDef {
  id
  name
  height
  width
  src
}
    `;
export const CreateDeckDocument = gql`
    mutation createDeck($deckName: String!, $heroClass: String!, $cardIds: [String!], $format: String!) {
  createDeckWithCards(
    input: {deckName: $deckName, classHero: $heroClass, cardIds: $cardIds, formatName: $format}
  ) {
    deck {
      ...deck
      ...deckCards
    }
  }
}
    ${DeckFragmentDoc}
${DeckCardsFragmentDoc}`;
export type CreateDeckMutationFn = Apollo.MutationFunction<CreateDeckMutation, CreateDeckMutationVariables>;

/**
 * __useCreateDeckMutation__
 *
 * To run a mutation, you first call `useCreateDeckMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateDeckMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createDeckMutation, { data, loading, error }] = useCreateDeckMutation({
 *   variables: {
 *      deckName: // value for 'deckName'
 *      heroClass: // value for 'heroClass'
 *      cardIds: // value for 'cardIds'
 *      format: // value for 'format'
 *   },
 * });
 */
export function useCreateDeckMutation(baseOptions?: Apollo.MutationHookOptions<CreateDeckMutation, CreateDeckMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateDeckMutation, CreateDeckMutationVariables>(CreateDeckDocument, options);
      }
export type CreateDeckMutationHookResult = ReturnType<typeof useCreateDeckMutation>;
export type CreateDeckMutationResult = Apollo.MutationResult<CreateDeckMutation>;
export type CreateDeckMutationOptions = Apollo.BaseMutationOptions<CreateDeckMutation, CreateDeckMutationVariables>;
export const DeleteCardDocument = gql`
    mutation deleteCard($cardId: String!) {
  archiveCard(input: {cardId: $cardId}) {
    clientMutationId
  }
}
    `;
export type DeleteCardMutationFn = Apollo.MutationFunction<DeleteCardMutation, DeleteCardMutationVariables>;

/**
 * __useDeleteCardMutation__
 *
 * To run a mutation, you first call `useDeleteCardMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useDeleteCardMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [deleteCardMutation, { data, loading, error }] = useDeleteCardMutation({
 *   variables: {
 *      cardId: // value for 'cardId'
 *   },
 * });
 */
export function useDeleteCardMutation(baseOptions?: Apollo.MutationHookOptions<DeleteCardMutation, DeleteCardMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<DeleteCardMutation, DeleteCardMutationVariables>(DeleteCardDocument, options);
      }
export type DeleteCardMutationHookResult = ReturnType<typeof useDeleteCardMutation>;
export type DeleteCardMutationResult = Apollo.MutationResult<DeleteCardMutation>;
export type DeleteCardMutationOptions = Apollo.BaseMutationOptions<DeleteCardMutation, DeleteCardMutationVariables>;
export const DeleteDeckDocument = gql`
    mutation deleteDeck($deckId: String!) {
  updateDeckById(input: {id: $deckId, deckPatch: {trashed: true}}) {
    deck {
      trashed
    }
  }
}
    `;
export type DeleteDeckMutationFn = Apollo.MutationFunction<DeleteDeckMutation, DeleteDeckMutationVariables>;

/**
 * __useDeleteDeckMutation__
 *
 * To run a mutation, you first call `useDeleteDeckMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useDeleteDeckMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [deleteDeckMutation, { data, loading, error }] = useDeleteDeckMutation({
 *   variables: {
 *      deckId: // value for 'deckId'
 *   },
 * });
 */
export function useDeleteDeckMutation(baseOptions?: Apollo.MutationHookOptions<DeleteDeckMutation, DeleteDeckMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<DeleteDeckMutation, DeleteDeckMutationVariables>(DeleteDeckDocument, options);
      }
export type DeleteDeckMutationHookResult = ReturnType<typeof useDeleteDeckMutation>;
export type DeleteDeckMutationResult = Apollo.MutationResult<DeleteDeckMutation>;
export type DeleteDeckMutationOptions = Apollo.BaseMutationOptions<DeleteDeckMutation, DeleteDeckMutationVariables>;
export const PublishCardDocument = gql`
    mutation publishCard($cardId: String!) {
  publishCard(input: {cardId: $cardId}) {
    bigInt
  }
}
    `;
export type PublishCardMutationFn = Apollo.MutationFunction<PublishCardMutation, PublishCardMutationVariables>;

/**
 * __usePublishCardMutation__
 *
 * To run a mutation, you first call `usePublishCardMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `usePublishCardMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [publishCardMutation, { data, loading, error }] = usePublishCardMutation({
 *   variables: {
 *      cardId: // value for 'cardId'
 *   },
 * });
 */
export function usePublishCardMutation(baseOptions?: Apollo.MutationHookOptions<PublishCardMutation, PublishCardMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<PublishCardMutation, PublishCardMutationVariables>(PublishCardDocument, options);
      }
export type PublishCardMutationHookResult = ReturnType<typeof usePublishCardMutation>;
export type PublishCardMutationResult = Apollo.MutationResult<PublishCardMutation>;
export type PublishCardMutationOptions = Apollo.BaseMutationOptions<PublishCardMutation, PublishCardMutationVariables>;
export const RenameDeckDocument = gql`
    mutation renameDeck($deckId: String!, $deckName: String!) {
  updateDeckById(input: {id: $deckId, deckPatch: {name: $deckName}}) {
    deck {
      id
      name
    }
  }
}
    `;
export type RenameDeckMutationFn = Apollo.MutationFunction<RenameDeckMutation, RenameDeckMutationVariables>;

/**
 * __useRenameDeckMutation__
 *
 * To run a mutation, you first call `useRenameDeckMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useRenameDeckMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [renameDeckMutation, { data, loading, error }] = useRenameDeckMutation({
 *   variables: {
 *      deckId: // value for 'deckId'
 *      deckName: // value for 'deckName'
 *   },
 * });
 */
export function useRenameDeckMutation(baseOptions?: Apollo.MutationHookOptions<RenameDeckMutation, RenameDeckMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<RenameDeckMutation, RenameDeckMutationVariables>(RenameDeckDocument, options);
      }
export type RenameDeckMutationHookResult = ReturnType<typeof useRenameDeckMutation>;
export type RenameDeckMutationResult = Apollo.MutationResult<RenameDeckMutation>;
export type RenameDeckMutationOptions = Apollo.BaseMutationOptions<RenameDeckMutation, RenameDeckMutationVariables>;
export const SaveCardDocument = gql`
    mutation saveCard($cardId: String!, $blocklyWorkspace: String!, $cardScript: JSON) {
  saveCard(
    input: {cardId: $cardId, workspace: $blocklyWorkspace, json: $cardScript}
  ) {
    card {
      ...card
    }
  }
}
    ${CardFragmentDoc}`;
export type SaveCardMutationFn = Apollo.MutationFunction<SaveCardMutation, SaveCardMutationVariables>;

/**
 * __useSaveCardMutation__
 *
 * To run a mutation, you first call `useSaveCardMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useSaveCardMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [saveCardMutation, { data, loading, error }] = useSaveCardMutation({
 *   variables: {
 *      cardId: // value for 'cardId'
 *      blocklyWorkspace: // value for 'blocklyWorkspace'
 *      cardScript: // value for 'cardScript'
 *   },
 * });
 */
export function useSaveCardMutation(baseOptions?: Apollo.MutationHookOptions<SaveCardMutation, SaveCardMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<SaveCardMutation, SaveCardMutationVariables>(SaveCardDocument, options);
      }
export type SaveCardMutationHookResult = ReturnType<typeof useSaveCardMutation>;
export type SaveCardMutationResult = Apollo.MutationResult<SaveCardMutation>;
export type SaveCardMutationOptions = Apollo.BaseMutationOptions<SaveCardMutation, SaveCardMutationVariables>;
export const SetCardsInDeckDocument = gql`
    mutation setCardsInDeck($deckId: String!, $cardIds: [String!]) {
  setCardsInDeck(input: {deck: $deckId, cardIds: $cardIds}) {
    cardsInDecks {
      id
      cardId
    }
  }
}
    `;
export type SetCardsInDeckMutationFn = Apollo.MutationFunction<SetCardsInDeckMutation, SetCardsInDeckMutationVariables>;

/**
 * __useSetCardsInDeckMutation__
 *
 * To run a mutation, you first call `useSetCardsInDeckMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useSetCardsInDeckMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [setCardsInDeckMutation, { data, loading, error }] = useSetCardsInDeckMutation({
 *   variables: {
 *      deckId: // value for 'deckId'
 *      cardIds: // value for 'cardIds'
 *   },
 * });
 */
export function useSetCardsInDeckMutation(baseOptions?: Apollo.MutationHookOptions<SetCardsInDeckMutation, SetCardsInDeckMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<SetCardsInDeckMutation, SetCardsInDeckMutationVariables>(SetCardsInDeckDocument, options);
      }
export type SetCardsInDeckMutationHookResult = ReturnType<typeof useSetCardsInDeckMutation>;
export type SetCardsInDeckMutationResult = Apollo.MutationResult<SetCardsInDeckMutation>;
export type SetCardsInDeckMutationOptions = Apollo.BaseMutationOptions<SetCardsInDeckMutation, SetCardsInDeckMutationVariables>;
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
  getLatestCard(cardId: $id, published: true) {
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
export const GetCardsDocument = gql`
    query getCards($limit: Int, $filter: CardFilter, $offset: Int, $orderBy: [CardsOrderBy!]) {
  allCards(offset: $offset, filter: $filter, first: $limit, orderBy: $orderBy) {
    nodes {
      ...card
    }
    totalCount
  }
}
    ${CardFragmentDoc}`;

/**
 * __useGetCardsQuery__
 *
 * To run a query within a React component, call `useGetCardsQuery` and pass it any options that fit your needs.
 * When your component renders, `useGetCardsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useGetCardsQuery({
 *   variables: {
 *      limit: // value for 'limit'
 *      filter: // value for 'filter'
 *      offset: // value for 'offset'
 *      orderBy: // value for 'orderBy'
 *   },
 * });
 */
export function useGetCardsQuery(baseOptions?: Apollo.QueryHookOptions<GetCardsQuery, GetCardsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<GetCardsQuery, GetCardsQueryVariables>(GetCardsDocument, options);
      }
export function useGetCardsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<GetCardsQuery, GetCardsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<GetCardsQuery, GetCardsQueryVariables>(GetCardsDocument, options);
        }
export type GetCardsQueryHookResult = ReturnType<typeof useGetCardsQuery>;
export type GetCardsLazyQueryHookResult = ReturnType<typeof useGetCardsLazyQuery>;
export type GetCardsQueryResult = Apollo.QueryResult<GetCardsQuery, GetCardsQueryVariables>;
export const GetClassesDocument = gql`
    query getClasses($filter: ClassFilter) {
  allClasses(filter: $filter) {
    nodes {
      ...class
    }
    totalCount
  }
}
    ${ClassFragmentDoc}`;

/**
 * __useGetClassesQuery__
 *
 * To run a query within a React component, call `useGetClassesQuery` and pass it any options that fit your needs.
 * When your component renders, `useGetClassesQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useGetClassesQuery({
 *   variables: {
 *      filter: // value for 'filter'
 *   },
 * });
 */
export function useGetClassesQuery(baseOptions?: Apollo.QueryHookOptions<GetClassesQuery, GetClassesQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<GetClassesQuery, GetClassesQueryVariables>(GetClassesDocument, options);
      }
export function useGetClassesLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<GetClassesQuery, GetClassesQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<GetClassesQuery, GetClassesQueryVariables>(GetClassesDocument, options);
        }
export type GetClassesQueryHookResult = ReturnType<typeof useGetClassesQuery>;
export type GetClassesLazyQueryHookResult = ReturnType<typeof useGetClassesLazyQuery>;
export type GetClassesQueryResult = Apollo.QueryResult<GetClassesQuery, GetClassesQueryVariables>;
export const GetCollectionCardsDocument = gql`
    query getCollectionCards($limit: Int, $filter: CollectionCardFilter, $offset: Int, $orderBy: [CollectionCardsOrderBy!]) {
  allCollectionCards(
    offset: $offset
    filter: $filter
    first: $limit
    orderBy: $orderBy
  ) {
    nodes {
      ...collectionCard
    }
    totalCount
  }
}
    ${CollectionCardFragmentDoc}`;

/**
 * __useGetCollectionCardsQuery__
 *
 * To run a query within a React component, call `useGetCollectionCardsQuery` and pass it any options that fit your needs.
 * When your component renders, `useGetCollectionCardsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useGetCollectionCardsQuery({
 *   variables: {
 *      limit: // value for 'limit'
 *      filter: // value for 'filter'
 *      offset: // value for 'offset'
 *      orderBy: // value for 'orderBy'
 *   },
 * });
 */
export function useGetCollectionCardsQuery(baseOptions?: Apollo.QueryHookOptions<GetCollectionCardsQuery, GetCollectionCardsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<GetCollectionCardsQuery, GetCollectionCardsQueryVariables>(GetCollectionCardsDocument, options);
      }
export function useGetCollectionCardsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<GetCollectionCardsQuery, GetCollectionCardsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<GetCollectionCardsQuery, GetCollectionCardsQueryVariables>(GetCollectionCardsDocument, options);
        }
export type GetCollectionCardsQueryHookResult = ReturnType<typeof useGetCollectionCardsQuery>;
export type GetCollectionCardsLazyQueryHookResult = ReturnType<typeof useGetCollectionCardsLazyQuery>;
export type GetCollectionCardsQueryResult = Apollo.QueryResult<GetCollectionCardsQuery, GetCollectionCardsQueryVariables>;
export const GetDeckDocument = gql`
    query getDeck($deckId: String!) {
  deckById(id: $deckId) {
    ...deck
    ...deckCards
  }
}
    ${DeckFragmentDoc}
${DeckCardsFragmentDoc}`;

/**
 * __useGetDeckQuery__
 *
 * To run a query within a React component, call `useGetDeckQuery` and pass it any options that fit your needs.
 * When your component renders, `useGetDeckQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useGetDeckQuery({
 *   variables: {
 *      deckId: // value for 'deckId'
 *   },
 * });
 */
export function useGetDeckQuery(baseOptions: Apollo.QueryHookOptions<GetDeckQuery, GetDeckQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<GetDeckQuery, GetDeckQueryVariables>(GetDeckDocument, options);
      }
export function useGetDeckLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<GetDeckQuery, GetDeckQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<GetDeckQuery, GetDeckQueryVariables>(GetDeckDocument, options);
        }
export type GetDeckQueryHookResult = ReturnType<typeof useGetDeckQuery>;
export type GetDeckLazyQueryHookResult = ReturnType<typeof useGetDeckLazyQuery>;
export type GetDeckQueryResult = Apollo.QueryResult<GetDeckQuery, GetDeckQueryVariables>;
export const GetDecksDocument = gql`
    query getDecks($user: String) {
  allDecks(condition: {trashed: false}) {
    nodes {
      ...deck
    }
  }
  allDeckShares(condition: {shareRecipientId: $user, trashedByRecipient: false}) {
    nodes {
      deckByDeckId {
        ...deck
      }
    }
  }
}
    ${DeckFragmentDoc}`;

/**
 * __useGetDecksQuery__
 *
 * To run a query within a React component, call `useGetDecksQuery` and pass it any options that fit your needs.
 * When your component renders, `useGetDecksQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useGetDecksQuery({
 *   variables: {
 *      user: // value for 'user'
 *   },
 * });
 */
export function useGetDecksQuery(baseOptions?: Apollo.QueryHookOptions<GetDecksQuery, GetDecksQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<GetDecksQuery, GetDecksQueryVariables>(GetDecksDocument, options);
      }
export function useGetDecksLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<GetDecksQuery, GetDecksQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<GetDecksQuery, GetDecksQueryVariables>(GetDecksDocument, options);
        }
export type GetDecksQueryHookResult = ReturnType<typeof useGetDecksQuery>;
export type GetDecksLazyQueryHookResult = ReturnType<typeof useGetDecksLazyQuery>;
export type GetDecksQueryResult = Apollo.QueryResult<GetDecksQuery, GetDecksQueryVariables>;