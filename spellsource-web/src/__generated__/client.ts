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
  /** A location in a connection that can be used for resuming pagination. */
  Cursor: any;
  /**
   * A point in time as described by the [ISO
   * 8601](https://en.wikipedia.org/wiki/ISO_8601) and, if it has a timezone, [RFC
   * 3339](https://datatracker.ietf.org/doc/html/rfc3339) standards. Input values
   * that do not conform to both ISO 8601 and RFC 3339 may be coerced, which may lead
   * to unexpected results.
   */
  Datetime: any;
  /** Represents JSON values as specified by [ECMA-404](http://www.ecma-international.org/publications/files/ECMA-ST/ECMA-404.pdf). */
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

export type Card = Node & {
  __typename?: 'Card';
  blocklyWorkspace?: Maybe<Scalars['JSON']>;
  cardScript?: Maybe<Scalars['JSON']>;
  createdAt: Scalars['Datetime'];
  createdBy: Scalars['String'];
  id: Scalars['String'];
  isArchived: Scalars['Boolean'];
  isPublished: Scalars['Boolean'];
  lastModified: Scalars['Datetime'];
  /** A globally unique identifier. Can be used in various places throughout the system to identify this single value. */
  nodeId: Scalars['ID'];
  /** Reads and enables pagination through a set of `PublishedCard`. */
  publishedCardsBySuccession: PublishedCardsConnection;
  succession: Scalars['BigInt'];
  /**
   * The URI of the application that created this card. The git URL by default represents cards that came from the
   *     Spellsource git repository. https://www.getspellsource.com/cards/editor or similar represents cards authored in the
   *     web interface
   */
  uri?: Maybe<Scalars['String']>;
};


export type CardPublishedCardsBySuccessionArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<PublishedCardCondition>;
  filter?: InputMaybe<PublishedCardFilter>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<PublishedCardsOrderBy>>;
};

/** A condition to be used against `Card` object types. All fields are tested for equality and combined with a logical ‘and.’ */
export type CardCondition = {
  /** Checks for equality with the object’s `blocklyWorkspace` field. */
  blocklyWorkspace?: InputMaybe<Scalars['JSON']>;
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
  /** Filter by the object’s `blocklyWorkspace` field. */
  blocklyWorkspace?: InputMaybe<JsonFilter>;
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
  not?: InputMaybe<CardFilter>;
  /** Checks for any expressions in this list. */
  or?: InputMaybe<Array<CardFilter>>;
  /** Filter by the object’s `publishedCardsBySuccession` relation. */
  publishedCardsBySuccession?: InputMaybe<CardToManyPublishedCardFilter>;
  /** Some related `publishedCardsBySuccession` exist. */
  publishedCardsBySuccessionExist?: InputMaybe<Scalars['Boolean']>;
  /** Filter by the object’s `succession` field. */
  succession?: InputMaybe<BigIntFilter>;
  /** Filter by the object’s `uri` field. */
  uri?: InputMaybe<StringFilter>;
};

/** An input for mutations affecting `Card` */
export type CardInput = {
  blocklyWorkspace?: InputMaybe<Scalars['JSON']>;
  cardScript?: InputMaybe<Scalars['JSON']>;
  createdAt?: InputMaybe<Scalars['Datetime']>;
  createdBy: Scalars['String'];
  id: Scalars['String'];
  isArchived?: InputMaybe<Scalars['Boolean']>;
  isPublished?: InputMaybe<Scalars['Boolean']>;
  lastModified?: InputMaybe<Scalars['Datetime']>;
  /**
   * The URI of the application that created this card. The git URL by default represents cards that came from the
   *     Spellsource git repository. https://www.getspellsource.com/cards/editor or similar represents cards authored in the
   *     web interface
   */
  uri?: InputMaybe<Scalars['String']>;
};

/** Represents an update to a `Card`. Fields that are set will be updated. */
export type CardPatch = {
  blocklyWorkspace?: InputMaybe<Scalars['JSON']>;
  cardScript?: InputMaybe<Scalars['JSON']>;
  createdAt?: InputMaybe<Scalars['Datetime']>;
  createdBy?: InputMaybe<Scalars['String']>;
  id?: InputMaybe<Scalars['String']>;
  isArchived?: InputMaybe<Scalars['Boolean']>;
  isPublished?: InputMaybe<Scalars['Boolean']>;
  lastModified?: InputMaybe<Scalars['Datetime']>;
  /**
   * The URI of the application that created this card. The git URL by default represents cards that came from the
   *     Spellsource git repository. https://www.getspellsource.com/cards/editor or similar represents cards authored in the
   *     web interface
   */
  uri?: InputMaybe<Scalars['String']>;
};

/** A filter to be used against many `PublishedCard` object types. All fields are combined with a logical ‘and.’ */
export type CardToManyPublishedCardFilter = {
  /** Every related `PublishedCard` matches the filter criteria. All fields are combined with a logical ‘and.’ */
  every?: InputMaybe<PublishedCardFilter>;
  /** No related `PublishedCard` matches the filter criteria. All fields are combined with a logical ‘and.’ */
  none?: InputMaybe<PublishedCardFilter>;
  /** Some related `PublishedCard` matches the filter criteria. All fields are combined with a logical ‘and.’ */
  some?: InputMaybe<PublishedCardFilter>;
};

/** A connection to a list of `Card` values. */
export type CardsConnection = {
  __typename?: 'CardsConnection';
  /** A list of edges which contains the `Card` and cursor to aid in pagination. */
  edges: Array<Maybe<CardsEdge>>;
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
  /** cannot delete cards that are currently used in decks */
  cardId: Scalars['String'];
  /** Reads a single `Deck` that is related to this `CardsInDeck`. */
  deckByDeckId?: Maybe<Deck>;
  /** deleting a deck deletes all its card references */
  deckId: Scalars['String'];
  id: Scalars['BigInt'];
  /** A globally unique identifier. Can be used in various places throughout the system to identify this single value. */
  nodeId: Scalars['ID'];
  /** Reads a single `PublishedCard` that is related to this `CardsInDeck`. */
  publishedCardByCardId?: Maybe<PublishedCard>;
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
  /** Filter by the object’s `deckByDeckId` relation. */
  deckByDeckId?: InputMaybe<DeckFilter>;
  /** Filter by the object’s `deckId` field. */
  deckId?: InputMaybe<StringFilter>;
  /** Filter by the object’s `id` field. */
  id?: InputMaybe<BigIntFilter>;
  /** Negates the expression. */
  not?: InputMaybe<CardsInDeckFilter>;
  /** Checks for any expressions in this list. */
  or?: InputMaybe<Array<CardsInDeckFilter>>;
  /** Filter by the object’s `publishedCardByCardId` relation. */
  publishedCardByCardId?: InputMaybe<PublishedCardFilter>;
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
  edges: Array<Maybe<CardsInDecksEdge>>;
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
  PrimaryKeyAsc: 'PRIMARY_KEY_ASC',
  PrimaryKeyDesc: 'PRIMARY_KEY_DESC',
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
  edges: Array<Maybe<ClassesEdge>>;
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
export type CollectionCard = {
  __typename?: 'CollectionCard';
  blocklyWorkspace?: Maybe<Scalars['JSON']>;
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
  blocklyWorkspace?: InputMaybe<Scalars['JSON']>;
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
  /** Filter by the object’s `blocklyWorkspace` field. */
  blocklyWorkspace?: InputMaybe<JsonFilter>;
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
  edges: Array<Maybe<CollectionCardsEdge>>;
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
  orderBy?: Array<CardsOrderBy>;
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
  /** Reads a single `PublishedCard` that is related to this `CardsInDeck`. */
  publishedCardByCardId?: Maybe<PublishedCard>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our create `CardsInDeck` mutation. */
export type CreateCardsInDeckPayloadCardsInDeckEdgeArgs = {
  orderBy?: Array<CardsInDecksOrderBy>;
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
  orderBy?: Array<DecksOrderBy>;
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
  orderBy?: Array<DecksOrderBy>;
};

/** All input for the create `GeneratedArt` mutation. */
export type CreateGeneratedArtInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The `GeneratedArt` to be created by this mutation. */
  generatedArt: GeneratedArtInput;
};

/** The output of our create `GeneratedArt` mutation. */
export type CreateGeneratedArtPayload = {
  __typename?: 'CreateGeneratedArtPayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** The `GeneratedArt` that was created by this mutation. */
  generatedArt?: Maybe<GeneratedArt>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};

/** All input for the create `PublishedCard` mutation. */
export type CreatePublishedCardInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The `PublishedCard` to be created by this mutation. */
  publishedCard: PublishedCardInput;
};

/** The output of our create `PublishedCard` mutation. */
export type CreatePublishedCardPayload = {
  __typename?: 'CreatePublishedCardPayload';
  /** Reads a single `Card` that is related to this `PublishedCard`. */
  cardBySuccession?: Maybe<Card>;
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** The `PublishedCard` that was created by this mutation. */
  publishedCard?: Maybe<PublishedCard>;
  /** An edge for our `PublishedCard`. May be used by Relay 1. */
  publishedCardEdge?: Maybe<PublishedCardsEdge>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our create `PublishedCard` mutation. */
export type CreatePublishedCardPayloadPublishedCardEdgeArgs = {
  orderBy?: Array<PublishedCardsOrderBy>;
};

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
  /** Reads and enables pagination through a set of `DeckShare`. */
  deckSharesByDeckId: DeckSharesConnection;
  deckType: Scalars['Int'];
  format?: Maybe<Scalars['String']>;
  heroClass?: Maybe<Scalars['String']>;
  id: Scalars['String'];
  /** premades always shared with all users by application logic */
  isPremade: Scalars['Boolean'];
  /** who last edited this deck */
  lastEditedBy: Scalars['String'];
  name?: Maybe<Scalars['String']>;
  /** A globally unique identifier. Can be used in various places throughout the system to identify this single value. */
  nodeId: Scalars['ID'];
  permittedToDuplicate: Scalars['Boolean'];
  /** Reads a single `RogueRun` that is related to this `Deck`. */
  rogueRunByDeck?: Maybe<RogueRun>;
  /** Reads and enables pagination through a set of `RogueRun`. */
  rogueRunsByOpponentDeck: RogueRunsConnection;
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


export type DeckRogueRunsByOpponentDeckArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<RogueRunCondition>;
  filter?: InputMaybe<RogueRunFilter>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<RogueRunsOrderBy>>;
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
  /** Filter by the object’s `cardsInDecksByDeckId` relation. */
  cardsInDecksByDeckId?: InputMaybe<DeckToManyCardsInDeckFilter>;
  /** Some related `cardsInDecksByDeckId` exist. */
  cardsInDecksByDeckIdExist?: InputMaybe<Scalars['Boolean']>;
  /** Filter by the object’s `createdBy` field. */
  createdBy?: InputMaybe<StringFilter>;
  /** Filter by the object’s `deckSharesByDeckId` relation. */
  deckSharesByDeckId?: InputMaybe<DeckToManyDeckShareFilter>;
  /** Some related `deckSharesByDeckId` exist. */
  deckSharesByDeckIdExist?: InputMaybe<Scalars['Boolean']>;
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
  /** Filter by the object’s `rogueRunByDeck` relation. */
  rogueRunByDeck?: InputMaybe<RogueRunFilter>;
  /** A related `rogueRunByDeck` exists. */
  rogueRunByDeckExists?: InputMaybe<Scalars['Boolean']>;
  /** Filter by the object’s `rogueRunsByOpponentDeck` relation. */
  rogueRunsByOpponentDeck?: InputMaybe<DeckToManyRogueRunFilter>;
  /** Some related `rogueRunsByOpponentDeck` exist. */
  rogueRunsByOpponentDeckExist?: InputMaybe<Scalars['Boolean']>;
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
  /** Filter by the object’s `deckByDeckId` relation. */
  deckByDeckId?: InputMaybe<DeckFilter>;
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

/** A connection to a list of `DeckShare` values. */
export type DeckSharesConnection = {
  __typename?: 'DeckSharesConnection';
  /** A list of edges which contains the `DeckShare` and cursor to aid in pagination. */
  edges: Array<Maybe<DeckSharesEdge>>;
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
/** A filter to be used against many `CardsInDeck` object types. All fields are combined with a logical ‘and.’ */
export type DeckToManyCardsInDeckFilter = {
  /** Every related `CardsInDeck` matches the filter criteria. All fields are combined with a logical ‘and.’ */
  every?: InputMaybe<CardsInDeckFilter>;
  /** No related `CardsInDeck` matches the filter criteria. All fields are combined with a logical ‘and.’ */
  none?: InputMaybe<CardsInDeckFilter>;
  /** Some related `CardsInDeck` matches the filter criteria. All fields are combined with a logical ‘and.’ */
  some?: InputMaybe<CardsInDeckFilter>;
};

/** A filter to be used against many `DeckShare` object types. All fields are combined with a logical ‘and.’ */
export type DeckToManyDeckShareFilter = {
  /** Every related `DeckShare` matches the filter criteria. All fields are combined with a logical ‘and.’ */
  every?: InputMaybe<DeckShareFilter>;
  /** No related `DeckShare` matches the filter criteria. All fields are combined with a logical ‘and.’ */
  none?: InputMaybe<DeckShareFilter>;
  /** Some related `DeckShare` matches the filter criteria. All fields are combined with a logical ‘and.’ */
  some?: InputMaybe<DeckShareFilter>;
};

/** A filter to be used against many `RogueRun` object types. All fields are combined with a logical ‘and.’ */
export type DeckToManyRogueRunFilter = {
  /** Every related `RogueRun` matches the filter criteria. All fields are combined with a logical ‘and.’ */
  every?: InputMaybe<RogueRunFilter>;
  /** No related `RogueRun` matches the filter criteria. All fields are combined with a logical ‘and.’ */
  none?: InputMaybe<RogueRunFilter>;
  /** Some related `RogueRun` matches the filter criteria. All fields are combined with a logical ‘and.’ */
  some?: InputMaybe<RogueRunFilter>;
};

/** A connection to a list of `Deck` values. */
export type DecksConnection = {
  __typename?: 'DecksConnection';
  /** A list of edges which contains the `Deck` and cursor to aid in pagination. */
  edges: Array<Maybe<DecksEdge>>;
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
  /** Reads a single `PublishedCard` that is related to this `CardsInDeck`. */
  publishedCardByCardId?: Maybe<PublishedCard>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our delete `CardsInDeck` mutation. */
export type DeleteCardsInDeckPayloadCardsInDeckEdgeArgs = {
  orderBy?: Array<CardsInDecksOrderBy>;
};

/** All input for the `deletePublishedCardById` mutation. */
export type DeletePublishedCardByIdInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  id: Scalars['String'];
};

/** All input for the `deletePublishedCard` mutation. */
export type DeletePublishedCardInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The globally unique `ID` which will identify a single `PublishedCard` to be deleted. */
  nodeId: Scalars['ID'];
};

/** The output of our delete `PublishedCard` mutation. */
export type DeletePublishedCardPayload = {
  __typename?: 'DeletePublishedCardPayload';
  /** Reads a single `Card` that is related to this `PublishedCard`. */
  cardBySuccession?: Maybe<Card>;
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  deletedPublishedCardId?: Maybe<Scalars['ID']>;
  /** The `PublishedCard` that was deleted by this mutation. */
  publishedCard?: Maybe<PublishedCard>;
  /** An edge for our `PublishedCard`. May be used by Relay 1. */
  publishedCardEdge?: Maybe<PublishedCardsEdge>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our delete `PublishedCard` mutation. */
export type DeletePublishedCardPayloadPublishedCardEdgeArgs = {
  orderBy?: Array<PublishedCardsOrderBy>;
};

export type GeneratedArt = {
  __typename?: 'GeneratedArt';
  hash: Scalars['String'];
  info?: Maybe<Scalars['JSON']>;
  isArchived: Scalars['Boolean'];
  owner: Scalars['String'];
  urls: Array<Maybe<Scalars['String']>>;
};

/**
 * A condition to be used against `GeneratedArt` object types. All fields are
 * tested for equality and combined with a logical ‘and.’
 */
export type GeneratedArtCondition = {
  /** Checks for equality with the object’s `hash` field. */
  hash?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `info` field. */
  info?: InputMaybe<Scalars['JSON']>;
  /** Checks for equality with the object’s `isArchived` field. */
  isArchived?: InputMaybe<Scalars['Boolean']>;
  /** Checks for equality with the object’s `owner` field. */
  owner?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `urls` field. */
  urls?: InputMaybe<Array<InputMaybe<Scalars['String']>>>;
};

/** A filter to be used against `GeneratedArt` object types. All fields are combined with a logical ‘and.’ */
export type GeneratedArtFilter = {
  /** Checks for all expressions in this list. */
  and?: InputMaybe<Array<GeneratedArtFilter>>;
  /** Filter by the object’s `hash` field. */
  hash?: InputMaybe<StringFilter>;
  /** Filter by the object’s `info` field. */
  info?: InputMaybe<JsonFilter>;
  /** Filter by the object’s `isArchived` field. */
  isArchived?: InputMaybe<BooleanFilter>;
  /** Negates the expression. */
  not?: InputMaybe<GeneratedArtFilter>;
  /** Checks for any expressions in this list. */
  or?: InputMaybe<Array<GeneratedArtFilter>>;
  /** Filter by the object’s `owner` field. */
  owner?: InputMaybe<StringFilter>;
  /** Filter by the object’s `urls` field. */
  urls?: InputMaybe<StringListFilter>;
};

/** An input for mutations affecting `GeneratedArt` */
export type GeneratedArtInput = {
  hash: Scalars['String'];
  info?: InputMaybe<Scalars['JSON']>;
  isArchived?: InputMaybe<Scalars['Boolean']>;
  owner?: InputMaybe<Scalars['String']>;
  urls: Array<InputMaybe<Scalars['String']>>;
};

/** Represents an update to a `GeneratedArt`. Fields that are set will be updated. */
export type GeneratedArtPatch = {
  hash?: InputMaybe<Scalars['String']>;
  info?: InputMaybe<Scalars['JSON']>;
  isArchived?: InputMaybe<Scalars['Boolean']>;
  owner?: InputMaybe<Scalars['String']>;
  urls?: InputMaybe<Array<InputMaybe<Scalars['String']>>>;
};

/** A connection to a list of `GeneratedArt` values. */
export type GeneratedArtsConnection = {
  __typename?: 'GeneratedArtsConnection';
  /** A list of edges which contains the `GeneratedArt` and cursor to aid in pagination. */
  edges: Array<Maybe<GeneratedArtsEdge>>;
  /** A list of `GeneratedArt` objects. */
  nodes: Array<Maybe<GeneratedArt>>;
  /** Information to aid in pagination. */
  pageInfo: PageInfo;
  /** The count of *all* `GeneratedArt` you could get from the connection. */
  totalCount: Scalars['Int'];
};

/** A `GeneratedArt` edge in the connection. */
export type GeneratedArtsEdge = {
  __typename?: 'GeneratedArtsEdge';
  /** A cursor for use in pagination. */
  cursor?: Maybe<Scalars['Cursor']>;
  /** The `GeneratedArt` at the end of the edge. */
  node?: Maybe<GeneratedArt>;
};

/** Methods to use when ordering `GeneratedArt`. */
export const GeneratedArtsOrderBy = {
  HashAsc: 'HASH_ASC',
  HashDesc: 'HASH_DESC',
  InfoAsc: 'INFO_ASC',
  InfoDesc: 'INFO_DESC',
  IsArchivedAsc: 'IS_ARCHIVED_ASC',
  IsArchivedDesc: 'IS_ARCHIVED_DESC',
  Natural: 'NATURAL',
  OwnerAsc: 'OWNER_ASC',
  OwnerDesc: 'OWNER_DESC'
} as const;

export type GeneratedArtsOrderBy = typeof GeneratedArtsOrderBy[keyof typeof GeneratedArtsOrderBy];
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

export type GetCollectionCardsRecord = {
  __typename?: 'GetCollectionCardsRecord';
  blocklyWorkspace?: Maybe<Scalars['JSON']>;
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

export type Mutation = {
  __typename?: 'Mutation';
  archiveCard?: Maybe<ArchiveCardPayload>;
  /** Creates a single `Card`. */
  createCard?: Maybe<CreateCardPayload>;
  /** Creates a single `CardsInDeck`. */
  createCardsInDeck?: Maybe<CreateCardsInDeckPayload>;
  /** Creates a single `Deck`. */
  createDeck?: Maybe<CreateDeckPayload>;
  createDeckWithCards?: Maybe<CreateDeckWithCardsPayload>;
  /** Creates a single `GeneratedArt`. */
  createGeneratedArt?: Maybe<CreateGeneratedArtPayload>;
  /** Creates a single `PublishedCard`. */
  createPublishedCard?: Maybe<CreatePublishedCardPayload>;
  /** Deletes a single `CardsInDeck` using its globally unique id. */
  deleteCardsInDeck?: Maybe<DeleteCardsInDeckPayload>;
  /** Deletes a single `CardsInDeck` using a unique key. */
  deleteCardsInDeckById?: Maybe<DeleteCardsInDeckPayload>;
  /** Deletes a single `PublishedCard` using its globally unique id. */
  deletePublishedCard?: Maybe<DeletePublishedCardPayload>;
  /** Deletes a single `PublishedCard` using a unique key. */
  deletePublishedCardById?: Maybe<DeletePublishedCardPayload>;
  getClasses?: Maybe<GetClassesPayload>;
  getCollectionCards?: Maybe<GetCollectionCardsPayload>;
  /** TODO can we return as RogueRun object? */
  makeRogueChoice: RogueRun;
  publishCard?: Maybe<PublishCardPayload>;
  saveCard?: Maybe<SaveCardPayload>;
  saveGeneratedArt?: Maybe<SaveGeneratedArtPayload>;
  setCardsInDeck?: Maybe<SetCardsInDeckPayload>;
  startRogueRun: RogueRun;
  /** Updates a single `Card` using its globally unique id and a patch. */
  updateCard?: Maybe<UpdateCardPayload>;
  /** Updates a single `Card` using a unique key and a patch. */
  updateCardBySuccession?: Maybe<UpdateCardPayload>;
  /** Updates a single `CardsInDeck` using its globally unique id and a patch. */
  updateCardsInDeck?: Maybe<UpdateCardsInDeckPayload>;
  /** Updates a single `CardsInDeck` using a unique key and a patch. */
  updateCardsInDeckById?: Maybe<UpdateCardsInDeckPayload>;
  /** Updates a single `Deck` using its globally unique id and a patch. */
  updateDeck?: Maybe<UpdateDeckPayload>;
  /** Updates a single `Deck` using a unique key and a patch. */
  updateDeckById?: Maybe<UpdateDeckPayload>;
  /** Updates a single `GeneratedArt` using a unique key and a patch. */
  updateGeneratedArtByHashAndOwner?: Maybe<UpdateGeneratedArtPayload>;
  /** Updates a single `PublishedCard` using its globally unique id and a patch. */
  updatePublishedCard?: Maybe<UpdatePublishedCardPayload>;
  /** Updates a single `PublishedCard` using a unique key and a patch. */
  updatePublishedCardById?: Maybe<UpdatePublishedCardPayload>;
};


export type MutationArchiveCardArgs = {
  input: ArchiveCardInput;
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


export type MutationCreateDeckWithCardsArgs = {
  input: CreateDeckWithCardsInput;
};


export type MutationCreateGeneratedArtArgs = {
  input: CreateGeneratedArtInput;
};


export type MutationCreatePublishedCardArgs = {
  input: CreatePublishedCardInput;
};


export type MutationDeleteCardsInDeckArgs = {
  input: DeleteCardsInDeckInput;
};


export type MutationDeleteCardsInDeckByIdArgs = {
  input: DeleteCardsInDeckByIdInput;
};


export type MutationDeletePublishedCardArgs = {
  input: DeletePublishedCardInput;
};


export type MutationDeletePublishedCardByIdArgs = {
  input: DeletePublishedCardByIdInput;
};


export type MutationGetClassesArgs = {
  input: GetClassesInput;
};


export type MutationGetCollectionCardsArgs = {
  input: GetCollectionCardsInput;
};


export type MutationMakeRogueChoiceArgs = {
  choiceIndex: Scalars['Int'];
  rogueId: Scalars['BigInt'];
};


export type MutationPublishCardArgs = {
  input: PublishCardInput;
};


export type MutationSaveCardArgs = {
  input: SaveCardInput;
};


export type MutationSaveGeneratedArtArgs = {
  input: SaveGeneratedArtInput;
};


export type MutationSetCardsInDeckArgs = {
  input: SetCardsInDeckInput;
};


export type MutationStartRogueRunArgs = {
  heroClass: Scalars['String'];
  seed?: InputMaybe<Scalars['BigInt']>;
};


export type MutationUpdateCardArgs = {
  input: UpdateCardInput;
};


export type MutationUpdateCardBySuccessionArgs = {
  input: UpdateCardBySuccessionInput;
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


export type MutationUpdateGeneratedArtByHashAndOwnerArgs = {
  input: UpdateGeneratedArtByHashAndOwnerInput;
};


export type MutationUpdatePublishedCardArgs = {
  input: UpdatePublishedCardInput;
};


export type MutationUpdatePublishedCardByIdArgs = {
  input: UpdatePublishedCardByIdInput;
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

export type PublishedCard = Node & {
  __typename?: 'PublishedCard';
  /** Reads a single `Card` that is related to this `PublishedCard`. */
  cardBySuccession?: Maybe<Card>;
  /** Reads and enables pagination through a set of `CardsInDeck`. */
  cardsInDecksByCardId: CardsInDecksConnection;
  id: Scalars['String'];
  /** A globally unique identifier. Can be used in various places throughout the system to identify this single value. */
  nodeId: Scalars['ID'];
  succession: Scalars['BigInt'];
};


export type PublishedCardCardsInDecksByCardIdArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<CardsInDeckCondition>;
  filter?: InputMaybe<CardsInDeckFilter>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<CardsInDecksOrderBy>>;
};

/**
 * A condition to be used against `PublishedCard` object types. All fields are
 * tested for equality and combined with a logical ‘and.’
 */
export type PublishedCardCondition = {
  /** Checks for equality with the object’s `id` field. */
  id?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `succession` field. */
  succession?: InputMaybe<Scalars['BigInt']>;
};

/** A filter to be used against `PublishedCard` object types. All fields are combined with a logical ‘and.’ */
export type PublishedCardFilter = {
  /** Checks for all expressions in this list. */
  and?: InputMaybe<Array<PublishedCardFilter>>;
  /** Filter by the object’s `cardBySuccession` relation. */
  cardBySuccession?: InputMaybe<CardFilter>;
  /** Filter by the object’s `cardsInDecksByCardId` relation. */
  cardsInDecksByCardId?: InputMaybe<PublishedCardToManyCardsInDeckFilter>;
  /** Some related `cardsInDecksByCardId` exist. */
  cardsInDecksByCardIdExist?: InputMaybe<Scalars['Boolean']>;
  /** Filter by the object’s `id` field. */
  id?: InputMaybe<StringFilter>;
  /** Negates the expression. */
  not?: InputMaybe<PublishedCardFilter>;
  /** Checks for any expressions in this list. */
  or?: InputMaybe<Array<PublishedCardFilter>>;
  /** Filter by the object’s `succession` field. */
  succession?: InputMaybe<BigIntFilter>;
};

/** An input for mutations affecting `PublishedCard` */
export type PublishedCardInput = {
  id: Scalars['String'];
  succession: Scalars['BigInt'];
};

/** Represents an update to a `PublishedCard`. Fields that are set will be updated. */
export type PublishedCardPatch = {
  id?: InputMaybe<Scalars['String']>;
  succession?: InputMaybe<Scalars['BigInt']>;
};

/** A filter to be used against many `CardsInDeck` object types. All fields are combined with a logical ‘and.’ */
export type PublishedCardToManyCardsInDeckFilter = {
  /** Every related `CardsInDeck` matches the filter criteria. All fields are combined with a logical ‘and.’ */
  every?: InputMaybe<CardsInDeckFilter>;
  /** No related `CardsInDeck` matches the filter criteria. All fields are combined with a logical ‘and.’ */
  none?: InputMaybe<CardsInDeckFilter>;
  /** Some related `CardsInDeck` matches the filter criteria. All fields are combined with a logical ‘and.’ */
  some?: InputMaybe<CardsInDeckFilter>;
};

/** A connection to a list of `PublishedCard` values. */
export type PublishedCardsConnection = {
  __typename?: 'PublishedCardsConnection';
  /** A list of edges which contains the `PublishedCard` and cursor to aid in pagination. */
  edges: Array<Maybe<PublishedCardsEdge>>;
  /** A list of `PublishedCard` objects. */
  nodes: Array<Maybe<PublishedCard>>;
  /** Information to aid in pagination. */
  pageInfo: PageInfo;
  /** The count of *all* `PublishedCard` you could get from the connection. */
  totalCount: Scalars['Int'];
};

/** A `PublishedCard` edge in the connection. */
export type PublishedCardsEdge = {
  __typename?: 'PublishedCardsEdge';
  /** A cursor for use in pagination. */
  cursor?: Maybe<Scalars['Cursor']>;
  /** The `PublishedCard` at the end of the edge. */
  node?: Maybe<PublishedCard>;
};

/** Methods to use when ordering `PublishedCard`. */
export const PublishedCardsOrderBy = {
  IdAsc: 'ID_ASC',
  IdDesc: 'ID_DESC',
  Natural: 'NATURAL',
  PrimaryKeyAsc: 'PRIMARY_KEY_ASC',
  PrimaryKeyDesc: 'PRIMARY_KEY_DESC',
  SuccessionAsc: 'SUCCESSION_ASC',
  SuccessionDesc: 'SUCCESSION_DESC'
} as const;

export type PublishedCardsOrderBy = typeof PublishedCardsOrderBy[keyof typeof PublishedCardsOrderBy];
export type Query = Node & {
  __typename?: 'Query';
  /** Reads and enables pagination through a set of `Card`. */
  allCards?: Maybe<CardsConnection>;
  /** Reads and enables pagination through a set of `CardsInDeck`. */
  allCardsInDecks?: Maybe<CardsInDecksConnection>;
  /** Reads and enables pagination through a set of `Class`. */
  allClasses?: Maybe<ClassesConnection>;
  /** Reads and enables pagination through a set of `CollectionCard`. */
  allCollectionCards?: Maybe<CollectionCardsConnection>;
  /** Reads and enables pagination through a set of `DeckShare`. */
  allDeckShares?: Maybe<DeckSharesConnection>;
  /** Reads and enables pagination through a set of `Deck`. */
  allDecks?: Maybe<DecksConnection>;
  /** Reads and enables pagination through a set of `GeneratedArt`. */
  allGeneratedArts?: Maybe<GeneratedArtsConnection>;
  /** Reads and enables pagination through a set of `PublishedCard`. */
  allPublishedCards?: Maybe<PublishedCardsConnection>;
  /** Reads and enables pagination through a set of `RogueRun`. */
  allRogueRuns?: Maybe<RogueRunsConnection>;
  canSeeDeck?: Maybe<Scalars['Boolean']>;
  /** Reads a single `Card` using its globally unique `ID`. */
  card?: Maybe<Card>;
  /** Get a single `Card`. */
  cardBySuccession?: Maybe<Card>;
  /** Reads a single `CardsInDeck` using its globally unique `ID`. */
  cardsInDeck?: Maybe<CardsInDeck>;
  /** Get a single `CardsInDeck`. */
  cardsInDeckById?: Maybe<CardsInDeck>;
  currentUserId?: Maybe<Scalars['String']>;
  /** Reads a single `Deck` using its globally unique `ID`. */
  deck?: Maybe<Deck>;
  /** Get a single `Deck`. */
  deckById?: Maybe<Deck>;
  /** Reads a single `DeckShare` using its globally unique `ID`. */
  deckShare?: Maybe<DeckShare>;
  /** Get a single `DeckShare`. */
  deckShareByDeckIdAndShareRecipientId?: Maybe<DeckShare>;
  /** Get a single `GeneratedArt`. */
  generatedArtByHashAndOwner?: Maybe<GeneratedArt>;
  getLatestCard?: Maybe<Card>;
  getUserId?: Maybe<Scalars['String']>;
  /** Fetches an object given its globally unique `ID`. */
  node?: Maybe<Node>;
  /** The root query type must be a `Node` to work well with Relay 1 mutations. This just resolves to `query`. */
  nodeId: Scalars['ID'];
  /** Reads a single `PublishedCard` using its globally unique `ID`. */
  publishedCard?: Maybe<PublishedCard>;
  /** Get a single `PublishedCard`. */
  publishedCardById?: Maybe<PublishedCard>;
  /**
   * Exposes the root query type nested one level down. This is helpful for Relay 1
   * which can only query top level fields if they are in a particular form.
   */
  query: Query;
  /** Reads a single `RogueRun` using its globally unique `ID`. */
  rogueRun?: Maybe<RogueRun>;
  /** Get a single `RogueRun`. */
  rogueRunByDeck?: Maybe<RogueRun>;
  /** Get a single `RogueRun`. */
  rogueRunByGame?: Maybe<RogueRun>;
  /** Get a single `RogueRun`. */
  rogueRunById?: Maybe<RogueRun>;
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


export type QueryAllGeneratedArtsArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<GeneratedArtCondition>;
  filter?: InputMaybe<GeneratedArtFilter>;
  first?: InputMaybe<Scalars['Int']>;
  includeArchived?: InputMaybe<IncludeArchivedOption>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<GeneratedArtsOrderBy>>;
};


export type QueryAllPublishedCardsArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<PublishedCardCondition>;
  filter?: InputMaybe<PublishedCardFilter>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<PublishedCardsOrderBy>>;
};


export type QueryAllRogueRunsArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<RogueRunCondition>;
  filter?: InputMaybe<RogueRunFilter>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<RogueRunsOrderBy>>;
};


export type QueryCanSeeDeckArgs = {
  deck?: InputMaybe<DeckInput>;
  userId?: InputMaybe<Scalars['String']>;
};


export type QueryCardArgs = {
  nodeId: Scalars['ID'];
};


export type QueryCardBySuccessionArgs = {
  succession: Scalars['BigInt'];
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


export type QueryDeckShareArgs = {
  nodeId: Scalars['ID'];
};


export type QueryDeckShareByDeckIdAndShareRecipientIdArgs = {
  deckId: Scalars['String'];
  shareRecipientId: Scalars['String'];
};


export type QueryGeneratedArtByHashAndOwnerArgs = {
  hash: Scalars['String'];
  owner: Scalars['String'];
};


export type QueryGetLatestCardArgs = {
  cardId?: InputMaybe<Scalars['String']>;
  published?: InputMaybe<Scalars['Boolean']>;
};


export type QueryNodeArgs = {
  nodeId: Scalars['ID'];
};


export type QueryPublishedCardArgs = {
  nodeId: Scalars['ID'];
};


export type QueryPublishedCardByIdArgs = {
  id: Scalars['String'];
};


export type QueryRogueRunArgs = {
  nodeId: Scalars['ID'];
};


export type QueryRogueRunByDeckArgs = {
  deck: Scalars['String'];
};


export type QueryRogueRunByGameArgs = {
  game: Scalars['BigInt'];
};


export type QueryRogueRunByIdArgs = {
  id: Scalars['BigInt'];
};

export type RogueRun = Node & {
  __typename?: 'RogueRun';
  bossesDefeated: Scalars['Int'];
  choices?: Maybe<Array<Maybe<Scalars['String']>>>;
  deck: Scalars['String'];
  /** Reads a single `Deck` that is related to this `RogueRun`. */
  deckByDeck?: Maybe<Deck>;
  /** Reads a single `Deck` that is related to this `RogueRun`. */
  deckByOpponentDeck?: Maybe<Deck>;
  endedAt?: Maybe<Scalars['Datetime']>;
  game?: Maybe<Scalars['BigInt']>;
  id: Scalars['BigInt'];
  /** A globally unique identifier. Can be used in various places throughout the system to identify this single value. */
  nodeId: Scalars['ID'];
  opponentDeck?: Maybe<Scalars['String']>;
  player: Scalars['String'];
  seed: Scalars['BigInt'];
  startedAt: Scalars['Datetime'];
  state: RogueRunState;
};

/**
 * A condition to be used against `RogueRun` object types. All fields are tested
 * for equality and combined with a logical ‘and.’
 */
export type RogueRunCondition = {
  /** Checks for equality with the object’s `bossesDefeated` field. */
  bossesDefeated?: InputMaybe<Scalars['Int']>;
  /** Checks for equality with the object’s `choices` field. */
  choices?: InputMaybe<Array<InputMaybe<Scalars['String']>>>;
  /** Checks for equality with the object’s `deck` field. */
  deck?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `endedAt` field. */
  endedAt?: InputMaybe<Scalars['Datetime']>;
  /** Checks for equality with the object’s `game` field. */
  game?: InputMaybe<Scalars['BigInt']>;
  /** Checks for equality with the object’s `id` field. */
  id?: InputMaybe<Scalars['BigInt']>;
  /** Checks for equality with the object’s `opponentDeck` field. */
  opponentDeck?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `player` field. */
  player?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `seed` field. */
  seed?: InputMaybe<Scalars['BigInt']>;
  /** Checks for equality with the object’s `startedAt` field. */
  startedAt?: InputMaybe<Scalars['Datetime']>;
  /** Checks for equality with the object’s `state` field. */
  state?: InputMaybe<RogueRunState>;
};

/** A filter to be used against `RogueRun` object types. All fields are combined with a logical ‘and.’ */
export type RogueRunFilter = {
  /** Checks for all expressions in this list. */
  and?: InputMaybe<Array<RogueRunFilter>>;
  /** Filter by the object’s `bossesDefeated` field. */
  bossesDefeated?: InputMaybe<IntFilter>;
  /** Filter by the object’s `choices` field. */
  choices?: InputMaybe<StringListFilter>;
  /** Filter by the object’s `deck` field. */
  deck?: InputMaybe<StringFilter>;
  /** Filter by the object’s `deckByDeck` relation. */
  deckByDeck?: InputMaybe<DeckFilter>;
  /** Filter by the object’s `deckByOpponentDeck` relation. */
  deckByOpponentDeck?: InputMaybe<DeckFilter>;
  /** A related `deckByOpponentDeck` exists. */
  deckByOpponentDeckExists?: InputMaybe<Scalars['Boolean']>;
  /** Filter by the object’s `endedAt` field. */
  endedAt?: InputMaybe<DatetimeFilter>;
  /** Filter by the object’s `game` field. */
  game?: InputMaybe<BigIntFilter>;
  /** Filter by the object’s `id` field. */
  id?: InputMaybe<BigIntFilter>;
  /** Negates the expression. */
  not?: InputMaybe<RogueRunFilter>;
  /** Filter by the object’s `opponentDeck` field. */
  opponentDeck?: InputMaybe<StringFilter>;
  /** Checks for any expressions in this list. */
  or?: InputMaybe<Array<RogueRunFilter>>;
  /** Filter by the object’s `player` field. */
  player?: InputMaybe<StringFilter>;
  /** Filter by the object’s `seed` field. */
  seed?: InputMaybe<BigIntFilter>;
  /** Filter by the object’s `startedAt` field. */
  startedAt?: InputMaybe<DatetimeFilter>;
  /** Filter by the object’s `state` field. */
  state?: InputMaybe<RogueRunStateFilter>;
};

export const RogueRunState = {
  Choice: 'CHOICE',
  Finished: 'FINISHED',
  Initial: 'INITIAL',
  InMatch: 'IN_MATCH',
  PreMatch: 'PRE_MATCH'
} as const;

export type RogueRunState = typeof RogueRunState[keyof typeof RogueRunState];
/** A filter to be used against RogueRunState fields. All fields are combined with a logical ‘and.’ */
export type RogueRunStateFilter = {
  /** Not equal to the specified value, treating null like an ordinary value. */
  distinctFrom?: InputMaybe<RogueRunState>;
  /** Equal to the specified value. */
  equalTo?: InputMaybe<RogueRunState>;
  /** Greater than the specified value. */
  greaterThan?: InputMaybe<RogueRunState>;
  /** Greater than or equal to the specified value. */
  greaterThanOrEqualTo?: InputMaybe<RogueRunState>;
  /** Included in the specified list. */
  in?: InputMaybe<Array<RogueRunState>>;
  /** Is null (if `true` is specified) or is not null (if `false` is specified). */
  isNull?: InputMaybe<Scalars['Boolean']>;
  /** Less than the specified value. */
  lessThan?: InputMaybe<RogueRunState>;
  /** Less than or equal to the specified value. */
  lessThanOrEqualTo?: InputMaybe<RogueRunState>;
  /** Equal to the specified value, treating null like an ordinary value. */
  notDistinctFrom?: InputMaybe<RogueRunState>;
  /** Not equal to the specified value. */
  notEqualTo?: InputMaybe<RogueRunState>;
  /** Not included in the specified list. */
  notIn?: InputMaybe<Array<RogueRunState>>;
};

/** A connection to a list of `RogueRun` values. */
export type RogueRunsConnection = {
  __typename?: 'RogueRunsConnection';
  /** A list of edges which contains the `RogueRun` and cursor to aid in pagination. */
  edges: Array<Maybe<RogueRunsEdge>>;
  /** A list of `RogueRun` objects. */
  nodes: Array<Maybe<RogueRun>>;
  /** Information to aid in pagination. */
  pageInfo: PageInfo;
  /** The count of *all* `RogueRun` you could get from the connection. */
  totalCount: Scalars['Int'];
};

/** A `RogueRun` edge in the connection. */
export type RogueRunsEdge = {
  __typename?: 'RogueRunsEdge';
  /** A cursor for use in pagination. */
  cursor?: Maybe<Scalars['Cursor']>;
  /** The `RogueRun` at the end of the edge. */
  node?: Maybe<RogueRun>;
};

/** Methods to use when ordering `RogueRun`. */
export const RogueRunsOrderBy = {
  BossesDefeatedAsc: 'BOSSES_DEFEATED_ASC',
  BossesDefeatedDesc: 'BOSSES_DEFEATED_DESC',
  DeckAsc: 'DECK_ASC',
  DeckDesc: 'DECK_DESC',
  EndedAtAsc: 'ENDED_AT_ASC',
  EndedAtDesc: 'ENDED_AT_DESC',
  GameAsc: 'GAME_ASC',
  GameDesc: 'GAME_DESC',
  IdAsc: 'ID_ASC',
  IdDesc: 'ID_DESC',
  Natural: 'NATURAL',
  OpponentDeckAsc: 'OPPONENT_DECK_ASC',
  OpponentDeckDesc: 'OPPONENT_DECK_DESC',
  PlayerAsc: 'PLAYER_ASC',
  PlayerDesc: 'PLAYER_DESC',
  PrimaryKeyAsc: 'PRIMARY_KEY_ASC',
  PrimaryKeyDesc: 'PRIMARY_KEY_DESC',
  SeedAsc: 'SEED_ASC',
  SeedDesc: 'SEED_DESC',
  StartedAtAsc: 'STARTED_AT_ASC',
  StartedAtDesc: 'STARTED_AT_DESC',
  StateAsc: 'STATE_ASC',
  StateDesc: 'STATE_DESC'
} as const;

export type RogueRunsOrderBy = typeof RogueRunsOrderBy[keyof typeof RogueRunsOrderBy];
/** All input for the `saveCard` mutation. */
export type SaveCardInput = {
  cardId?: InputMaybe<Scalars['String']>;
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  json?: InputMaybe<Scalars['JSON']>;
  workspace?: InputMaybe<Scalars['JSON']>;
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
  orderBy?: Array<CardsOrderBy>;
};

/** All input for the `saveGeneratedArt` mutation. */
export type SaveGeneratedArtInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  digest?: InputMaybe<Scalars['String']>;
  extraInfo?: InputMaybe<Scalars['JSON']>;
  links?: InputMaybe<Array<InputMaybe<Scalars['String']>>>;
};

/** The output of our `saveGeneratedArt` mutation. */
export type SaveGeneratedArtPayload = {
  __typename?: 'SaveGeneratedArtPayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  generatedArt?: Maybe<GeneratedArt>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
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

/** A filter to be used against String List fields. All fields are combined with a logical ‘and.’ */
export type StringListFilter = {
  /** Any array item is equal to the specified value. */
  anyEqualTo?: InputMaybe<Scalars['String']>;
  /** Any array item is greater than the specified value. */
  anyGreaterThan?: InputMaybe<Scalars['String']>;
  /** Any array item is greater than or equal to the specified value. */
  anyGreaterThanOrEqualTo?: InputMaybe<Scalars['String']>;
  /** Any array item is less than the specified value. */
  anyLessThan?: InputMaybe<Scalars['String']>;
  /** Any array item is less than or equal to the specified value. */
  anyLessThanOrEqualTo?: InputMaybe<Scalars['String']>;
  /** Any array item is not equal to the specified value. */
  anyNotEqualTo?: InputMaybe<Scalars['String']>;
  /** Contained by the specified list of values. */
  containedBy?: InputMaybe<Array<InputMaybe<Scalars['String']>>>;
  /** Contains the specified list of values. */
  contains?: InputMaybe<Array<InputMaybe<Scalars['String']>>>;
  /** Not equal to the specified value, treating null like an ordinary value. */
  distinctFrom?: InputMaybe<Array<InputMaybe<Scalars['String']>>>;
  /** Equal to the specified value. */
  equalTo?: InputMaybe<Array<InputMaybe<Scalars['String']>>>;
  /** Greater than the specified value. */
  greaterThan?: InputMaybe<Array<InputMaybe<Scalars['String']>>>;
  /** Greater than or equal to the specified value. */
  greaterThanOrEqualTo?: InputMaybe<Array<InputMaybe<Scalars['String']>>>;
  /** Is null (if `true` is specified) or is not null (if `false` is specified). */
  isNull?: InputMaybe<Scalars['Boolean']>;
  /** Less than the specified value. */
  lessThan?: InputMaybe<Array<InputMaybe<Scalars['String']>>>;
  /** Less than or equal to the specified value. */
  lessThanOrEqualTo?: InputMaybe<Array<InputMaybe<Scalars['String']>>>;
  /** Equal to the specified value, treating null like an ordinary value. */
  notDistinctFrom?: InputMaybe<Array<InputMaybe<Scalars['String']>>>;
  /** Not equal to the specified value. */
  notEqualTo?: InputMaybe<Array<InputMaybe<Scalars['String']>>>;
  /** Overlaps the specified list of values. */
  overlaps?: InputMaybe<Array<InputMaybe<Scalars['String']>>>;
};

/** All input for the `updateCardBySuccession` mutation. */
export type UpdateCardBySuccessionInput = {
  /** An object where the defined keys will be set on the `Card` being updated. */
  cardPatch: CardPatch;
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  succession: Scalars['BigInt'];
};

/** All input for the `updateCard` mutation. */
export type UpdateCardInput = {
  /** An object where the defined keys will be set on the `Card` being updated. */
  cardPatch: CardPatch;
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The globally unique `ID` which will identify a single `Card` to be updated. */
  nodeId: Scalars['ID'];
};

/** The output of our update `Card` mutation. */
export type UpdateCardPayload = {
  __typename?: 'UpdateCardPayload';
  /** The `Card` that was updated by this mutation. */
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


/** The output of our update `Card` mutation. */
export type UpdateCardPayloadCardEdgeArgs = {
  orderBy?: Array<CardsOrderBy>;
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
  /** Reads a single `PublishedCard` that is related to this `CardsInDeck`. */
  publishedCardByCardId?: Maybe<PublishedCard>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our update `CardsInDeck` mutation. */
export type UpdateCardsInDeckPayloadCardsInDeckEdgeArgs = {
  orderBy?: Array<CardsInDecksOrderBy>;
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
  orderBy?: Array<DecksOrderBy>;
};

/** All input for the `updateGeneratedArtByHashAndOwner` mutation. */
export type UpdateGeneratedArtByHashAndOwnerInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** An object where the defined keys will be set on the `GeneratedArt` being updated. */
  generatedArtPatch: GeneratedArtPatch;
  hash: Scalars['String'];
  owner: Scalars['String'];
};

/** The output of our update `GeneratedArt` mutation. */
export type UpdateGeneratedArtPayload = {
  __typename?: 'UpdateGeneratedArtPayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** The `GeneratedArt` that was updated by this mutation. */
  generatedArt?: Maybe<GeneratedArt>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};

/** All input for the `updatePublishedCardById` mutation. */
export type UpdatePublishedCardByIdInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  id: Scalars['String'];
  /** An object where the defined keys will be set on the `PublishedCard` being updated. */
  publishedCardPatch: PublishedCardPatch;
};

/** All input for the `updatePublishedCard` mutation. */
export type UpdatePublishedCardInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  /** The globally unique `ID` which will identify a single `PublishedCard` to be updated. */
  nodeId: Scalars['ID'];
  /** An object where the defined keys will be set on the `PublishedCard` being updated. */
  publishedCardPatch: PublishedCardPatch;
};

/** The output of our update `PublishedCard` mutation. */
export type UpdatePublishedCardPayload = {
  __typename?: 'UpdatePublishedCardPayload';
  /** Reads a single `Card` that is related to this `PublishedCard`. */
  cardBySuccession?: Maybe<Card>;
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** The `PublishedCard` that was updated by this mutation. */
  publishedCard?: Maybe<PublishedCard>;
  /** An edge for our `PublishedCard`. May be used by Relay 1. */
  publishedCardEdge?: Maybe<PublishedCardsEdge>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};


/** The output of our update `PublishedCard` mutation. */
export type UpdatePublishedCardPayloadPublishedCardEdgeArgs = {
  orderBy?: Array<PublishedCardsOrderBy>;
};



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
  BigInt: ResolverTypeWrapper<Partial<Scalars['BigInt']>>;
  BigIntFilter: ResolverTypeWrapper<Partial<BigIntFilter>>;
  Boolean: ResolverTypeWrapper<Partial<Scalars['Boolean']>>;
  BooleanFilter: ResolverTypeWrapper<Partial<BooleanFilter>>;
  Card: ResolverTypeWrapper<Partial<Card>>;
  CardCondition: ResolverTypeWrapper<Partial<CardCondition>>;
  CardFilter: ResolverTypeWrapper<Partial<CardFilter>>;
  CardInput: ResolverTypeWrapper<Partial<CardInput>>;
  CardPatch: ResolverTypeWrapper<Partial<CardPatch>>;
  CardToManyPublishedCardFilter: ResolverTypeWrapper<Partial<CardToManyPublishedCardFilter>>;
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
  CollectionCard: ResolverTypeWrapper<Partial<CollectionCard>>;
  CollectionCardCondition: ResolverTypeWrapper<Partial<CollectionCardCondition>>;
  CollectionCardFilter: ResolverTypeWrapper<Partial<CollectionCardFilter>>;
  CollectionCardsConnection: ResolverTypeWrapper<Partial<CollectionCardsConnection>>;
  CollectionCardsEdge: ResolverTypeWrapper<Partial<CollectionCardsEdge>>;
  CollectionCardsOrderBy: ResolverTypeWrapper<Partial<CollectionCardsOrderBy>>;
  CreateCardInput: ResolverTypeWrapper<Partial<CreateCardInput>>;
  CreateCardPayload: ResolverTypeWrapper<Partial<CreateCardPayload>>;
  CreateCardsInDeckInput: ResolverTypeWrapper<Partial<CreateCardsInDeckInput>>;
  CreateCardsInDeckPayload: ResolverTypeWrapper<Partial<CreateCardsInDeckPayload>>;
  CreateDeckInput: ResolverTypeWrapper<Partial<CreateDeckInput>>;
  CreateDeckPayload: ResolverTypeWrapper<Partial<CreateDeckPayload>>;
  CreateDeckWithCardsInput: ResolverTypeWrapper<Partial<CreateDeckWithCardsInput>>;
  CreateDeckWithCardsPayload: ResolverTypeWrapper<Partial<CreateDeckWithCardsPayload>>;
  CreateGeneratedArtInput: ResolverTypeWrapper<Partial<CreateGeneratedArtInput>>;
  CreateGeneratedArtPayload: ResolverTypeWrapper<Partial<CreateGeneratedArtPayload>>;
  CreatePublishedCardInput: ResolverTypeWrapper<Partial<CreatePublishedCardInput>>;
  CreatePublishedCardPayload: ResolverTypeWrapper<Partial<CreatePublishedCardPayload>>;
  Cursor: ResolverTypeWrapper<Partial<Scalars['Cursor']>>;
  Datetime: ResolverTypeWrapper<Partial<Scalars['Datetime']>>;
  DatetimeFilter: ResolverTypeWrapper<Partial<DatetimeFilter>>;
  Deck: ResolverTypeWrapper<Partial<Deck>>;
  DeckCondition: ResolverTypeWrapper<Partial<DeckCondition>>;
  DeckFilter: ResolverTypeWrapper<Partial<DeckFilter>>;
  DeckInput: ResolverTypeWrapper<Partial<DeckInput>>;
  DeckPatch: ResolverTypeWrapper<Partial<DeckPatch>>;
  DeckShare: ResolverTypeWrapper<Partial<DeckShare>>;
  DeckShareCondition: ResolverTypeWrapper<Partial<DeckShareCondition>>;
  DeckShareFilter: ResolverTypeWrapper<Partial<DeckShareFilter>>;
  DeckSharesConnection: ResolverTypeWrapper<Partial<DeckSharesConnection>>;
  DeckSharesEdge: ResolverTypeWrapper<Partial<DeckSharesEdge>>;
  DeckSharesOrderBy: ResolverTypeWrapper<Partial<DeckSharesOrderBy>>;
  DeckToManyCardsInDeckFilter: ResolverTypeWrapper<Partial<DeckToManyCardsInDeckFilter>>;
  DeckToManyDeckShareFilter: ResolverTypeWrapper<Partial<DeckToManyDeckShareFilter>>;
  DeckToManyRogueRunFilter: ResolverTypeWrapper<Partial<DeckToManyRogueRunFilter>>;
  DecksConnection: ResolverTypeWrapper<Partial<DecksConnection>>;
  DecksEdge: ResolverTypeWrapper<Partial<DecksEdge>>;
  DecksOrderBy: ResolverTypeWrapper<Partial<DecksOrderBy>>;
  DeleteCardsInDeckByIdInput: ResolverTypeWrapper<Partial<DeleteCardsInDeckByIdInput>>;
  DeleteCardsInDeckInput: ResolverTypeWrapper<Partial<DeleteCardsInDeckInput>>;
  DeleteCardsInDeckPayload: ResolverTypeWrapper<Partial<DeleteCardsInDeckPayload>>;
  DeletePublishedCardByIdInput: ResolverTypeWrapper<Partial<DeletePublishedCardByIdInput>>;
  DeletePublishedCardInput: ResolverTypeWrapper<Partial<DeletePublishedCardInput>>;
  DeletePublishedCardPayload: ResolverTypeWrapper<Partial<DeletePublishedCardPayload>>;
  GeneratedArt: ResolverTypeWrapper<Partial<GeneratedArt>>;
  GeneratedArtCondition: ResolverTypeWrapper<Partial<GeneratedArtCondition>>;
  GeneratedArtFilter: ResolverTypeWrapper<Partial<GeneratedArtFilter>>;
  GeneratedArtInput: ResolverTypeWrapper<Partial<GeneratedArtInput>>;
  GeneratedArtPatch: ResolverTypeWrapper<Partial<GeneratedArtPatch>>;
  GeneratedArtsConnection: ResolverTypeWrapper<Partial<GeneratedArtsConnection>>;
  GeneratedArtsEdge: ResolverTypeWrapper<Partial<GeneratedArtsEdge>>;
  GeneratedArtsOrderBy: ResolverTypeWrapper<Partial<GeneratedArtsOrderBy>>;
  GetClassesInput: ResolverTypeWrapper<Partial<GetClassesInput>>;
  GetClassesPayload: ResolverTypeWrapper<Partial<GetClassesPayload>>;
  GetClassesRecord: ResolverTypeWrapper<Partial<GetClassesRecord>>;
  GetCollectionCardsInput: ResolverTypeWrapper<Partial<GetCollectionCardsInput>>;
  GetCollectionCardsPayload: ResolverTypeWrapper<Partial<GetCollectionCardsPayload>>;
  GetCollectionCardsRecord: ResolverTypeWrapper<Partial<GetCollectionCardsRecord>>;
  ID: ResolverTypeWrapper<Partial<Scalars['ID']>>;
  IncludeArchivedOption: ResolverTypeWrapper<Partial<IncludeArchivedOption>>;
  Int: ResolverTypeWrapper<Partial<Scalars['Int']>>;
  IntFilter: ResolverTypeWrapper<Partial<IntFilter>>;
  JSON: ResolverTypeWrapper<Partial<Scalars['JSON']>>;
  JSONFilter: ResolverTypeWrapper<Partial<JsonFilter>>;
  Mutation: ResolverTypeWrapper<{}>;
  Node: ResolversTypes['Card'] | ResolversTypes['CardsInDeck'] | ResolversTypes['Deck'] | ResolversTypes['DeckShare'] | ResolversTypes['PublishedCard'] | ResolversTypes['Query'] | ResolversTypes['RogueRun'];
  PageInfo: ResolverTypeWrapper<Partial<PageInfo>>;
  PublishCardInput: ResolverTypeWrapper<Partial<PublishCardInput>>;
  PublishCardPayload: ResolverTypeWrapper<Partial<PublishCardPayload>>;
  PublishedCard: ResolverTypeWrapper<Partial<PublishedCard>>;
  PublishedCardCondition: ResolverTypeWrapper<Partial<PublishedCardCondition>>;
  PublishedCardFilter: ResolverTypeWrapper<Partial<PublishedCardFilter>>;
  PublishedCardInput: ResolverTypeWrapper<Partial<PublishedCardInput>>;
  PublishedCardPatch: ResolverTypeWrapper<Partial<PublishedCardPatch>>;
  PublishedCardToManyCardsInDeckFilter: ResolverTypeWrapper<Partial<PublishedCardToManyCardsInDeckFilter>>;
  PublishedCardsConnection: ResolverTypeWrapper<Partial<PublishedCardsConnection>>;
  PublishedCardsEdge: ResolverTypeWrapper<Partial<PublishedCardsEdge>>;
  PublishedCardsOrderBy: ResolverTypeWrapper<Partial<PublishedCardsOrderBy>>;
  Query: ResolverTypeWrapper<{}>;
  RogueRun: ResolverTypeWrapper<Partial<RogueRun>>;
  RogueRunCondition: ResolverTypeWrapper<Partial<RogueRunCondition>>;
  RogueRunFilter: ResolverTypeWrapper<Partial<RogueRunFilter>>;
  RogueRunState: ResolverTypeWrapper<Partial<RogueRunState>>;
  RogueRunStateFilter: ResolverTypeWrapper<Partial<RogueRunStateFilter>>;
  RogueRunsConnection: ResolverTypeWrapper<Partial<RogueRunsConnection>>;
  RogueRunsEdge: ResolverTypeWrapper<Partial<RogueRunsEdge>>;
  RogueRunsOrderBy: ResolverTypeWrapper<Partial<RogueRunsOrderBy>>;
  SaveCardInput: ResolverTypeWrapper<Partial<SaveCardInput>>;
  SaveCardPayload: ResolverTypeWrapper<Partial<SaveCardPayload>>;
  SaveGeneratedArtInput: ResolverTypeWrapper<Partial<SaveGeneratedArtInput>>;
  SaveGeneratedArtPayload: ResolverTypeWrapper<Partial<SaveGeneratedArtPayload>>;
  SetCardsInDeckInput: ResolverTypeWrapper<Partial<SetCardsInDeckInput>>;
  SetCardsInDeckPayload: ResolverTypeWrapper<Partial<SetCardsInDeckPayload>>;
  String: ResolverTypeWrapper<Partial<Scalars['String']>>;
  StringFilter: ResolverTypeWrapper<Partial<StringFilter>>;
  StringListFilter: ResolverTypeWrapper<Partial<StringListFilter>>;
  UpdateCardBySuccessionInput: ResolverTypeWrapper<Partial<UpdateCardBySuccessionInput>>;
  UpdateCardInput: ResolverTypeWrapper<Partial<UpdateCardInput>>;
  UpdateCardPayload: ResolverTypeWrapper<Partial<UpdateCardPayload>>;
  UpdateCardsInDeckByIdInput: ResolverTypeWrapper<Partial<UpdateCardsInDeckByIdInput>>;
  UpdateCardsInDeckInput: ResolverTypeWrapper<Partial<UpdateCardsInDeckInput>>;
  UpdateCardsInDeckPayload: ResolverTypeWrapper<Partial<UpdateCardsInDeckPayload>>;
  UpdateDeckByIdInput: ResolverTypeWrapper<Partial<UpdateDeckByIdInput>>;
  UpdateDeckInput: ResolverTypeWrapper<Partial<UpdateDeckInput>>;
  UpdateDeckPayload: ResolverTypeWrapper<Partial<UpdateDeckPayload>>;
  UpdateGeneratedArtByHashAndOwnerInput: ResolverTypeWrapper<Partial<UpdateGeneratedArtByHashAndOwnerInput>>;
  UpdateGeneratedArtPayload: ResolverTypeWrapper<Partial<UpdateGeneratedArtPayload>>;
  UpdatePublishedCardByIdInput: ResolverTypeWrapper<Partial<UpdatePublishedCardByIdInput>>;
  UpdatePublishedCardInput: ResolverTypeWrapper<Partial<UpdatePublishedCardInput>>;
  UpdatePublishedCardPayload: ResolverTypeWrapper<Partial<UpdatePublishedCardPayload>>;
};

/** Mapping between all available schema types and the resolvers parents */
export type ResolversParentTypes = {
  ArchiveCardInput: Partial<ArchiveCardInput>;
  ArchiveCardPayload: Partial<ArchiveCardPayload>;
  BigInt: Partial<Scalars['BigInt']>;
  BigIntFilter: Partial<BigIntFilter>;
  Boolean: Partial<Scalars['Boolean']>;
  BooleanFilter: Partial<BooleanFilter>;
  Card: Partial<Card>;
  CardCondition: Partial<CardCondition>;
  CardFilter: Partial<CardFilter>;
  CardInput: Partial<CardInput>;
  CardPatch: Partial<CardPatch>;
  CardToManyPublishedCardFilter: Partial<CardToManyPublishedCardFilter>;
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
  CollectionCard: Partial<CollectionCard>;
  CollectionCardCondition: Partial<CollectionCardCondition>;
  CollectionCardFilter: Partial<CollectionCardFilter>;
  CollectionCardsConnection: Partial<CollectionCardsConnection>;
  CollectionCardsEdge: Partial<CollectionCardsEdge>;
  CreateCardInput: Partial<CreateCardInput>;
  CreateCardPayload: Partial<CreateCardPayload>;
  CreateCardsInDeckInput: Partial<CreateCardsInDeckInput>;
  CreateCardsInDeckPayload: Partial<CreateCardsInDeckPayload>;
  CreateDeckInput: Partial<CreateDeckInput>;
  CreateDeckPayload: Partial<CreateDeckPayload>;
  CreateDeckWithCardsInput: Partial<CreateDeckWithCardsInput>;
  CreateDeckWithCardsPayload: Partial<CreateDeckWithCardsPayload>;
  CreateGeneratedArtInput: Partial<CreateGeneratedArtInput>;
  CreateGeneratedArtPayload: Partial<CreateGeneratedArtPayload>;
  CreatePublishedCardInput: Partial<CreatePublishedCardInput>;
  CreatePublishedCardPayload: Partial<CreatePublishedCardPayload>;
  Cursor: Partial<Scalars['Cursor']>;
  Datetime: Partial<Scalars['Datetime']>;
  DatetimeFilter: Partial<DatetimeFilter>;
  Deck: Partial<Deck>;
  DeckCondition: Partial<DeckCondition>;
  DeckFilter: Partial<DeckFilter>;
  DeckInput: Partial<DeckInput>;
  DeckPatch: Partial<DeckPatch>;
  DeckShare: Partial<DeckShare>;
  DeckShareCondition: Partial<DeckShareCondition>;
  DeckShareFilter: Partial<DeckShareFilter>;
  DeckSharesConnection: Partial<DeckSharesConnection>;
  DeckSharesEdge: Partial<DeckSharesEdge>;
  DeckToManyCardsInDeckFilter: Partial<DeckToManyCardsInDeckFilter>;
  DeckToManyDeckShareFilter: Partial<DeckToManyDeckShareFilter>;
  DeckToManyRogueRunFilter: Partial<DeckToManyRogueRunFilter>;
  DecksConnection: Partial<DecksConnection>;
  DecksEdge: Partial<DecksEdge>;
  DeleteCardsInDeckByIdInput: Partial<DeleteCardsInDeckByIdInput>;
  DeleteCardsInDeckInput: Partial<DeleteCardsInDeckInput>;
  DeleteCardsInDeckPayload: Partial<DeleteCardsInDeckPayload>;
  DeletePublishedCardByIdInput: Partial<DeletePublishedCardByIdInput>;
  DeletePublishedCardInput: Partial<DeletePublishedCardInput>;
  DeletePublishedCardPayload: Partial<DeletePublishedCardPayload>;
  GeneratedArt: Partial<GeneratedArt>;
  GeneratedArtCondition: Partial<GeneratedArtCondition>;
  GeneratedArtFilter: Partial<GeneratedArtFilter>;
  GeneratedArtInput: Partial<GeneratedArtInput>;
  GeneratedArtPatch: Partial<GeneratedArtPatch>;
  GeneratedArtsConnection: Partial<GeneratedArtsConnection>;
  GeneratedArtsEdge: Partial<GeneratedArtsEdge>;
  GetClassesInput: Partial<GetClassesInput>;
  GetClassesPayload: Partial<GetClassesPayload>;
  GetClassesRecord: Partial<GetClassesRecord>;
  GetCollectionCardsInput: Partial<GetCollectionCardsInput>;
  GetCollectionCardsPayload: Partial<GetCollectionCardsPayload>;
  GetCollectionCardsRecord: Partial<GetCollectionCardsRecord>;
  ID: Partial<Scalars['ID']>;
  Int: Partial<Scalars['Int']>;
  IntFilter: Partial<IntFilter>;
  JSON: Partial<Scalars['JSON']>;
  JSONFilter: Partial<JsonFilter>;
  Mutation: {};
  Node: ResolversParentTypes['Card'] | ResolversParentTypes['CardsInDeck'] | ResolversParentTypes['Deck'] | ResolversParentTypes['DeckShare'] | ResolversParentTypes['PublishedCard'] | ResolversParentTypes['Query'] | ResolversParentTypes['RogueRun'];
  PageInfo: Partial<PageInfo>;
  PublishCardInput: Partial<PublishCardInput>;
  PublishCardPayload: Partial<PublishCardPayload>;
  PublishedCard: Partial<PublishedCard>;
  PublishedCardCondition: Partial<PublishedCardCondition>;
  PublishedCardFilter: Partial<PublishedCardFilter>;
  PublishedCardInput: Partial<PublishedCardInput>;
  PublishedCardPatch: Partial<PublishedCardPatch>;
  PublishedCardToManyCardsInDeckFilter: Partial<PublishedCardToManyCardsInDeckFilter>;
  PublishedCardsConnection: Partial<PublishedCardsConnection>;
  PublishedCardsEdge: Partial<PublishedCardsEdge>;
  Query: {};
  RogueRun: Partial<RogueRun>;
  RogueRunCondition: Partial<RogueRunCondition>;
  RogueRunFilter: Partial<RogueRunFilter>;
  RogueRunStateFilter: Partial<RogueRunStateFilter>;
  RogueRunsConnection: Partial<RogueRunsConnection>;
  RogueRunsEdge: Partial<RogueRunsEdge>;
  SaveCardInput: Partial<SaveCardInput>;
  SaveCardPayload: Partial<SaveCardPayload>;
  SaveGeneratedArtInput: Partial<SaveGeneratedArtInput>;
  SaveGeneratedArtPayload: Partial<SaveGeneratedArtPayload>;
  SetCardsInDeckInput: Partial<SetCardsInDeckInput>;
  SetCardsInDeckPayload: Partial<SetCardsInDeckPayload>;
  String: Partial<Scalars['String']>;
  StringFilter: Partial<StringFilter>;
  StringListFilter: Partial<StringListFilter>;
  UpdateCardBySuccessionInput: Partial<UpdateCardBySuccessionInput>;
  UpdateCardInput: Partial<UpdateCardInput>;
  UpdateCardPayload: Partial<UpdateCardPayload>;
  UpdateCardsInDeckByIdInput: Partial<UpdateCardsInDeckByIdInput>;
  UpdateCardsInDeckInput: Partial<UpdateCardsInDeckInput>;
  UpdateCardsInDeckPayload: Partial<UpdateCardsInDeckPayload>;
  UpdateDeckByIdInput: Partial<UpdateDeckByIdInput>;
  UpdateDeckInput: Partial<UpdateDeckInput>;
  UpdateDeckPayload: Partial<UpdateDeckPayload>;
  UpdateGeneratedArtByHashAndOwnerInput: Partial<UpdateGeneratedArtByHashAndOwnerInput>;
  UpdateGeneratedArtPayload: Partial<UpdateGeneratedArtPayload>;
  UpdatePublishedCardByIdInput: Partial<UpdatePublishedCardByIdInput>;
  UpdatePublishedCardInput: Partial<UpdatePublishedCardInput>;
  UpdatePublishedCardPayload: Partial<UpdatePublishedCardPayload>;
};

export type ArchiveCardPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['ArchiveCardPayload'] = ResolversParentTypes['ArchiveCardPayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export interface BigIntScalarConfig extends GraphQLScalarTypeConfig<ResolversTypes['BigInt'], any> {
  name: 'BigInt';
}

export type CardResolvers<ContextType = any, ParentType extends ResolversParentTypes['Card'] = ResolversParentTypes['Card']> = {
  blocklyWorkspace?: Resolver<Maybe<ResolversTypes['JSON']>, ParentType, ContextType>;
  cardScript?: Resolver<Maybe<ResolversTypes['JSON']>, ParentType, ContextType>;
  createdAt?: Resolver<ResolversTypes['Datetime'], ParentType, ContextType>;
  createdBy?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  id?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  isArchived?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  isPublished?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  lastModified?: Resolver<ResolversTypes['Datetime'], ParentType, ContextType>;
  nodeId?: Resolver<ResolversTypes['ID'], ParentType, ContextType>;
  publishedCardsBySuccession?: Resolver<ResolversTypes['PublishedCardsConnection'], ParentType, ContextType, RequireFields<CardPublishedCardsBySuccessionArgs, 'orderBy'>>;
  succession?: Resolver<ResolversTypes['BigInt'], ParentType, ContextType>;
  uri?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type CardsConnectionResolvers<ContextType = any, ParentType extends ResolversParentTypes['CardsConnection'] = ResolversParentTypes['CardsConnection']> = {
  edges?: Resolver<Array<Maybe<ResolversTypes['CardsEdge']>>, ParentType, ContextType>;
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
  cardId?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  deckByDeckId?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  deckId?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  id?: Resolver<ResolversTypes['BigInt'], ParentType, ContextType>;
  nodeId?: Resolver<ResolversTypes['ID'], ParentType, ContextType>;
  publishedCardByCardId?: Resolver<Maybe<ResolversTypes['PublishedCard']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type CardsInDecksConnectionResolvers<ContextType = any, ParentType extends ResolversParentTypes['CardsInDecksConnection'] = ResolversParentTypes['CardsInDecksConnection']> = {
  edges?: Resolver<Array<Maybe<ResolversTypes['CardsInDecksEdge']>>, ParentType, ContextType>;
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
  edges?: Resolver<Array<Maybe<ResolversTypes['ClassesEdge']>>, ParentType, ContextType>;
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

export type CollectionCardResolvers<ContextType = any, ParentType extends ResolversParentTypes['CollectionCard'] = ResolversParentTypes['CollectionCard']> = {
  blocklyWorkspace?: Resolver<Maybe<ResolversTypes['JSON']>, ParentType, ContextType>;
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
  edges?: Resolver<Array<Maybe<ResolversTypes['CollectionCardsEdge']>>, ParentType, ContextType>;
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
  publishedCardByCardId?: Resolver<Maybe<ResolversTypes['PublishedCard']>, ParentType, ContextType>;
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

export type CreateDeckWithCardsPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['CreateDeckWithCardsPayload'] = ResolversParentTypes['CreateDeckWithCardsPayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  deck?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  deckEdge?: Resolver<Maybe<ResolversTypes['DecksEdge']>, ParentType, ContextType, RequireFields<CreateDeckWithCardsPayloadDeckEdgeArgs, 'orderBy'>>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type CreateGeneratedArtPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['CreateGeneratedArtPayload'] = ResolversParentTypes['CreateGeneratedArtPayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  generatedArt?: Resolver<Maybe<ResolversTypes['GeneratedArt']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type CreatePublishedCardPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['CreatePublishedCardPayload'] = ResolversParentTypes['CreatePublishedCardPayload']> = {
  cardBySuccession?: Resolver<Maybe<ResolversTypes['Card']>, ParentType, ContextType>;
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  publishedCard?: Resolver<Maybe<ResolversTypes['PublishedCard']>, ParentType, ContextType>;
  publishedCardEdge?: Resolver<Maybe<ResolversTypes['PublishedCardsEdge']>, ParentType, ContextType, RequireFields<CreatePublishedCardPayloadPublishedCardEdgeArgs, 'orderBy'>>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
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
  deckSharesByDeckId?: Resolver<ResolversTypes['DeckSharesConnection'], ParentType, ContextType, RequireFields<DeckDeckSharesByDeckIdArgs, 'orderBy'>>;
  deckType?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  format?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  heroClass?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  id?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  isPremade?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  lastEditedBy?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  name?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  nodeId?: Resolver<ResolversTypes['ID'], ParentType, ContextType>;
  permittedToDuplicate?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  rogueRunByDeck?: Resolver<Maybe<ResolversTypes['RogueRun']>, ParentType, ContextType>;
  rogueRunsByOpponentDeck?: Resolver<ResolversTypes['RogueRunsConnection'], ParentType, ContextType, RequireFields<DeckRogueRunsByOpponentDeckArgs, 'orderBy'>>;
  trashed?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
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
  edges?: Resolver<Array<Maybe<ResolversTypes['DeckSharesEdge']>>, ParentType, ContextType>;
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
  edges?: Resolver<Array<Maybe<ResolversTypes['DecksEdge']>>, ParentType, ContextType>;
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

export type DeleteCardsInDeckPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['DeleteCardsInDeckPayload'] = ResolversParentTypes['DeleteCardsInDeckPayload']> = {
  cardsInDeck?: Resolver<Maybe<ResolversTypes['CardsInDeck']>, ParentType, ContextType>;
  cardsInDeckEdge?: Resolver<Maybe<ResolversTypes['CardsInDecksEdge']>, ParentType, ContextType, RequireFields<DeleteCardsInDeckPayloadCardsInDeckEdgeArgs, 'orderBy'>>;
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  deckByDeckId?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  deletedCardsInDeckId?: Resolver<Maybe<ResolversTypes['ID']>, ParentType, ContextType>;
  publishedCardByCardId?: Resolver<Maybe<ResolversTypes['PublishedCard']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type DeletePublishedCardPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['DeletePublishedCardPayload'] = ResolversParentTypes['DeletePublishedCardPayload']> = {
  cardBySuccession?: Resolver<Maybe<ResolversTypes['Card']>, ParentType, ContextType>;
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  deletedPublishedCardId?: Resolver<Maybe<ResolversTypes['ID']>, ParentType, ContextType>;
  publishedCard?: Resolver<Maybe<ResolversTypes['PublishedCard']>, ParentType, ContextType>;
  publishedCardEdge?: Resolver<Maybe<ResolversTypes['PublishedCardsEdge']>, ParentType, ContextType, RequireFields<DeletePublishedCardPayloadPublishedCardEdgeArgs, 'orderBy'>>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type GeneratedArtResolvers<ContextType = any, ParentType extends ResolversParentTypes['GeneratedArt'] = ResolversParentTypes['GeneratedArt']> = {
  hash?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  info?: Resolver<Maybe<ResolversTypes['JSON']>, ParentType, ContextType>;
  isArchived?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  owner?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  urls?: Resolver<Array<Maybe<ResolversTypes['String']>>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type GeneratedArtsConnectionResolvers<ContextType = any, ParentType extends ResolversParentTypes['GeneratedArtsConnection'] = ResolversParentTypes['GeneratedArtsConnection']> = {
  edges?: Resolver<Array<Maybe<ResolversTypes['GeneratedArtsEdge']>>, ParentType, ContextType>;
  nodes?: Resolver<Array<Maybe<ResolversTypes['GeneratedArt']>>, ParentType, ContextType>;
  pageInfo?: Resolver<ResolversTypes['PageInfo'], ParentType, ContextType>;
  totalCount?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type GeneratedArtsEdgeResolvers<ContextType = any, ParentType extends ResolversParentTypes['GeneratedArtsEdge'] = ResolversParentTypes['GeneratedArtsEdge']> = {
  cursor?: Resolver<Maybe<ResolversTypes['Cursor']>, ParentType, ContextType>;
  node?: Resolver<Maybe<ResolversTypes['GeneratedArt']>, ParentType, ContextType>;
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
  blocklyWorkspace?: Resolver<Maybe<ResolversTypes['JSON']>, ParentType, ContextType>;
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

export interface JsonScalarConfig extends GraphQLScalarTypeConfig<ResolversTypes['JSON'], any> {
  name: 'JSON';
}

export type MutationResolvers<ContextType = any, ParentType extends ResolversParentTypes['Mutation'] = ResolversParentTypes['Mutation']> = {
  archiveCard?: Resolver<Maybe<ResolversTypes['ArchiveCardPayload']>, ParentType, ContextType, RequireFields<MutationArchiveCardArgs, 'input'>>;
  createCard?: Resolver<Maybe<ResolversTypes['CreateCardPayload']>, ParentType, ContextType, RequireFields<MutationCreateCardArgs, 'input'>>;
  createCardsInDeck?: Resolver<Maybe<ResolversTypes['CreateCardsInDeckPayload']>, ParentType, ContextType, RequireFields<MutationCreateCardsInDeckArgs, 'input'>>;
  createDeck?: Resolver<Maybe<ResolversTypes['CreateDeckPayload']>, ParentType, ContextType, RequireFields<MutationCreateDeckArgs, 'input'>>;
  createDeckWithCards?: Resolver<Maybe<ResolversTypes['CreateDeckWithCardsPayload']>, ParentType, ContextType, RequireFields<MutationCreateDeckWithCardsArgs, 'input'>>;
  createGeneratedArt?: Resolver<Maybe<ResolversTypes['CreateGeneratedArtPayload']>, ParentType, ContextType, RequireFields<MutationCreateGeneratedArtArgs, 'input'>>;
  createPublishedCard?: Resolver<Maybe<ResolversTypes['CreatePublishedCardPayload']>, ParentType, ContextType, RequireFields<MutationCreatePublishedCardArgs, 'input'>>;
  deleteCardsInDeck?: Resolver<Maybe<ResolversTypes['DeleteCardsInDeckPayload']>, ParentType, ContextType, RequireFields<MutationDeleteCardsInDeckArgs, 'input'>>;
  deleteCardsInDeckById?: Resolver<Maybe<ResolversTypes['DeleteCardsInDeckPayload']>, ParentType, ContextType, RequireFields<MutationDeleteCardsInDeckByIdArgs, 'input'>>;
  deletePublishedCard?: Resolver<Maybe<ResolversTypes['DeletePublishedCardPayload']>, ParentType, ContextType, RequireFields<MutationDeletePublishedCardArgs, 'input'>>;
  deletePublishedCardById?: Resolver<Maybe<ResolversTypes['DeletePublishedCardPayload']>, ParentType, ContextType, RequireFields<MutationDeletePublishedCardByIdArgs, 'input'>>;
  getClasses?: Resolver<Maybe<ResolversTypes['GetClassesPayload']>, ParentType, ContextType, RequireFields<MutationGetClassesArgs, 'input'>>;
  getCollectionCards?: Resolver<Maybe<ResolversTypes['GetCollectionCardsPayload']>, ParentType, ContextType, RequireFields<MutationGetCollectionCardsArgs, 'input'>>;
  makeRogueChoice?: Resolver<ResolversTypes['RogueRun'], ParentType, ContextType, RequireFields<MutationMakeRogueChoiceArgs, 'choiceIndex' | 'rogueId'>>;
  publishCard?: Resolver<Maybe<ResolversTypes['PublishCardPayload']>, ParentType, ContextType, RequireFields<MutationPublishCardArgs, 'input'>>;
  saveCard?: Resolver<Maybe<ResolversTypes['SaveCardPayload']>, ParentType, ContextType, RequireFields<MutationSaveCardArgs, 'input'>>;
  saveGeneratedArt?: Resolver<Maybe<ResolversTypes['SaveGeneratedArtPayload']>, ParentType, ContextType, RequireFields<MutationSaveGeneratedArtArgs, 'input'>>;
  setCardsInDeck?: Resolver<Maybe<ResolversTypes['SetCardsInDeckPayload']>, ParentType, ContextType, RequireFields<MutationSetCardsInDeckArgs, 'input'>>;
  startRogueRun?: Resolver<ResolversTypes['RogueRun'], ParentType, ContextType, RequireFields<MutationStartRogueRunArgs, 'heroClass'>>;
  updateCard?: Resolver<Maybe<ResolversTypes['UpdateCardPayload']>, ParentType, ContextType, RequireFields<MutationUpdateCardArgs, 'input'>>;
  updateCardBySuccession?: Resolver<Maybe<ResolversTypes['UpdateCardPayload']>, ParentType, ContextType, RequireFields<MutationUpdateCardBySuccessionArgs, 'input'>>;
  updateCardsInDeck?: Resolver<Maybe<ResolversTypes['UpdateCardsInDeckPayload']>, ParentType, ContextType, RequireFields<MutationUpdateCardsInDeckArgs, 'input'>>;
  updateCardsInDeckById?: Resolver<Maybe<ResolversTypes['UpdateCardsInDeckPayload']>, ParentType, ContextType, RequireFields<MutationUpdateCardsInDeckByIdArgs, 'input'>>;
  updateDeck?: Resolver<Maybe<ResolversTypes['UpdateDeckPayload']>, ParentType, ContextType, RequireFields<MutationUpdateDeckArgs, 'input'>>;
  updateDeckById?: Resolver<Maybe<ResolversTypes['UpdateDeckPayload']>, ParentType, ContextType, RequireFields<MutationUpdateDeckByIdArgs, 'input'>>;
  updateGeneratedArtByHashAndOwner?: Resolver<Maybe<ResolversTypes['UpdateGeneratedArtPayload']>, ParentType, ContextType, RequireFields<MutationUpdateGeneratedArtByHashAndOwnerArgs, 'input'>>;
  updatePublishedCard?: Resolver<Maybe<ResolversTypes['UpdatePublishedCardPayload']>, ParentType, ContextType, RequireFields<MutationUpdatePublishedCardArgs, 'input'>>;
  updatePublishedCardById?: Resolver<Maybe<ResolversTypes['UpdatePublishedCardPayload']>, ParentType, ContextType, RequireFields<MutationUpdatePublishedCardByIdArgs, 'input'>>;
};

export type NodeResolvers<ContextType = any, ParentType extends ResolversParentTypes['Node'] = ResolversParentTypes['Node']> = {
  __resolveType: TypeResolveFn<'Card' | 'CardsInDeck' | 'Deck' | 'DeckShare' | 'PublishedCard' | 'Query' | 'RogueRun', ParentType, ContextType>;
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

export type PublishedCardResolvers<ContextType = any, ParentType extends ResolversParentTypes['PublishedCard'] = ResolversParentTypes['PublishedCard']> = {
  cardBySuccession?: Resolver<Maybe<ResolversTypes['Card']>, ParentType, ContextType>;
  cardsInDecksByCardId?: Resolver<ResolversTypes['CardsInDecksConnection'], ParentType, ContextType, RequireFields<PublishedCardCardsInDecksByCardIdArgs, 'orderBy'>>;
  id?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  nodeId?: Resolver<ResolversTypes['ID'], ParentType, ContextType>;
  succession?: Resolver<ResolversTypes['BigInt'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type PublishedCardsConnectionResolvers<ContextType = any, ParentType extends ResolversParentTypes['PublishedCardsConnection'] = ResolversParentTypes['PublishedCardsConnection']> = {
  edges?: Resolver<Array<Maybe<ResolversTypes['PublishedCardsEdge']>>, ParentType, ContextType>;
  nodes?: Resolver<Array<Maybe<ResolversTypes['PublishedCard']>>, ParentType, ContextType>;
  pageInfo?: Resolver<ResolversTypes['PageInfo'], ParentType, ContextType>;
  totalCount?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type PublishedCardsEdgeResolvers<ContextType = any, ParentType extends ResolversParentTypes['PublishedCardsEdge'] = ResolversParentTypes['PublishedCardsEdge']> = {
  cursor?: Resolver<Maybe<ResolversTypes['Cursor']>, ParentType, ContextType>;
  node?: Resolver<Maybe<ResolversTypes['PublishedCard']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type QueryResolvers<ContextType = any, ParentType extends ResolversParentTypes['Query'] = ResolversParentTypes['Query']> = {
  allCards?: Resolver<Maybe<ResolversTypes['CardsConnection']>, ParentType, ContextType, RequireFields<QueryAllCardsArgs, 'includeArchived' | 'orderBy'>>;
  allCardsInDecks?: Resolver<Maybe<ResolversTypes['CardsInDecksConnection']>, ParentType, ContextType, RequireFields<QueryAllCardsInDecksArgs, 'orderBy'>>;
  allClasses?: Resolver<Maybe<ResolversTypes['ClassesConnection']>, ParentType, ContextType, RequireFields<QueryAllClassesArgs, 'orderBy'>>;
  allCollectionCards?: Resolver<Maybe<ResolversTypes['CollectionCardsConnection']>, ParentType, ContextType, RequireFields<QueryAllCollectionCardsArgs, 'orderBy'>>;
  allDeckShares?: Resolver<Maybe<ResolversTypes['DeckSharesConnection']>, ParentType, ContextType, RequireFields<QueryAllDeckSharesArgs, 'orderBy'>>;
  allDecks?: Resolver<Maybe<ResolversTypes['DecksConnection']>, ParentType, ContextType, RequireFields<QueryAllDecksArgs, 'orderBy'>>;
  allGeneratedArts?: Resolver<Maybe<ResolversTypes['GeneratedArtsConnection']>, ParentType, ContextType, RequireFields<QueryAllGeneratedArtsArgs, 'includeArchived' | 'orderBy'>>;
  allPublishedCards?: Resolver<Maybe<ResolversTypes['PublishedCardsConnection']>, ParentType, ContextType, RequireFields<QueryAllPublishedCardsArgs, 'orderBy'>>;
  allRogueRuns?: Resolver<Maybe<ResolversTypes['RogueRunsConnection']>, ParentType, ContextType, RequireFields<QueryAllRogueRunsArgs, 'orderBy'>>;
  canSeeDeck?: Resolver<Maybe<ResolversTypes['Boolean']>, ParentType, ContextType, Partial<QueryCanSeeDeckArgs>>;
  card?: Resolver<Maybe<ResolversTypes['Card']>, ParentType, ContextType, RequireFields<QueryCardArgs, 'nodeId'>>;
  cardBySuccession?: Resolver<Maybe<ResolversTypes['Card']>, ParentType, ContextType, RequireFields<QueryCardBySuccessionArgs, 'succession'>>;
  cardsInDeck?: Resolver<Maybe<ResolversTypes['CardsInDeck']>, ParentType, ContextType, RequireFields<QueryCardsInDeckArgs, 'nodeId'>>;
  cardsInDeckById?: Resolver<Maybe<ResolversTypes['CardsInDeck']>, ParentType, ContextType, RequireFields<QueryCardsInDeckByIdArgs, 'id'>>;
  currentUserId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  deck?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType, RequireFields<QueryDeckArgs, 'nodeId'>>;
  deckById?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType, RequireFields<QueryDeckByIdArgs, 'id'>>;
  deckShare?: Resolver<Maybe<ResolversTypes['DeckShare']>, ParentType, ContextType, RequireFields<QueryDeckShareArgs, 'nodeId'>>;
  deckShareByDeckIdAndShareRecipientId?: Resolver<Maybe<ResolversTypes['DeckShare']>, ParentType, ContextType, RequireFields<QueryDeckShareByDeckIdAndShareRecipientIdArgs, 'deckId' | 'shareRecipientId'>>;
  generatedArtByHashAndOwner?: Resolver<Maybe<ResolversTypes['GeneratedArt']>, ParentType, ContextType, RequireFields<QueryGeneratedArtByHashAndOwnerArgs, 'hash' | 'owner'>>;
  getLatestCard?: Resolver<Maybe<ResolversTypes['Card']>, ParentType, ContextType, Partial<QueryGetLatestCardArgs>>;
  getUserId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  node?: Resolver<Maybe<ResolversTypes['Node']>, ParentType, ContextType, RequireFields<QueryNodeArgs, 'nodeId'>>;
  nodeId?: Resolver<ResolversTypes['ID'], ParentType, ContextType>;
  publishedCard?: Resolver<Maybe<ResolversTypes['PublishedCard']>, ParentType, ContextType, RequireFields<QueryPublishedCardArgs, 'nodeId'>>;
  publishedCardById?: Resolver<Maybe<ResolversTypes['PublishedCard']>, ParentType, ContextType, RequireFields<QueryPublishedCardByIdArgs, 'id'>>;
  query?: Resolver<ResolversTypes['Query'], ParentType, ContextType>;
  rogueRun?: Resolver<Maybe<ResolversTypes['RogueRun']>, ParentType, ContextType, RequireFields<QueryRogueRunArgs, 'nodeId'>>;
  rogueRunByDeck?: Resolver<Maybe<ResolversTypes['RogueRun']>, ParentType, ContextType, RequireFields<QueryRogueRunByDeckArgs, 'deck'>>;
  rogueRunByGame?: Resolver<Maybe<ResolversTypes['RogueRun']>, ParentType, ContextType, RequireFields<QueryRogueRunByGameArgs, 'game'>>;
  rogueRunById?: Resolver<Maybe<ResolversTypes['RogueRun']>, ParentType, ContextType, RequireFields<QueryRogueRunByIdArgs, 'id'>>;
};

export type RogueRunResolvers<ContextType = any, ParentType extends ResolversParentTypes['RogueRun'] = ResolversParentTypes['RogueRun']> = {
  bossesDefeated?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  choices?: Resolver<Maybe<Array<Maybe<ResolversTypes['String']>>>, ParentType, ContextType>;
  deck?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  deckByDeck?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  deckByOpponentDeck?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  endedAt?: Resolver<Maybe<ResolversTypes['Datetime']>, ParentType, ContextType>;
  game?: Resolver<Maybe<ResolversTypes['BigInt']>, ParentType, ContextType>;
  id?: Resolver<ResolversTypes['BigInt'], ParentType, ContextType>;
  nodeId?: Resolver<ResolversTypes['ID'], ParentType, ContextType>;
  opponentDeck?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  player?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  seed?: Resolver<ResolversTypes['BigInt'], ParentType, ContextType>;
  startedAt?: Resolver<ResolversTypes['Datetime'], ParentType, ContextType>;
  state?: Resolver<ResolversTypes['RogueRunState'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type RogueRunsConnectionResolvers<ContextType = any, ParentType extends ResolversParentTypes['RogueRunsConnection'] = ResolversParentTypes['RogueRunsConnection']> = {
  edges?: Resolver<Array<Maybe<ResolversTypes['RogueRunsEdge']>>, ParentType, ContextType>;
  nodes?: Resolver<Array<Maybe<ResolversTypes['RogueRun']>>, ParentType, ContextType>;
  pageInfo?: Resolver<ResolversTypes['PageInfo'], ParentType, ContextType>;
  totalCount?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type RogueRunsEdgeResolvers<ContextType = any, ParentType extends ResolversParentTypes['RogueRunsEdge'] = ResolversParentTypes['RogueRunsEdge']> = {
  cursor?: Resolver<Maybe<ResolversTypes['Cursor']>, ParentType, ContextType>;
  node?: Resolver<Maybe<ResolversTypes['RogueRun']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type SaveCardPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['SaveCardPayload'] = ResolversParentTypes['SaveCardPayload']> = {
  card?: Resolver<Maybe<ResolversTypes['Card']>, ParentType, ContextType>;
  cardEdge?: Resolver<Maybe<ResolversTypes['CardsEdge']>, ParentType, ContextType, RequireFields<SaveCardPayloadCardEdgeArgs, 'orderBy'>>;
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type SaveGeneratedArtPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['SaveGeneratedArtPayload'] = ResolversParentTypes['SaveGeneratedArtPayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  generatedArt?: Resolver<Maybe<ResolversTypes['GeneratedArt']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type SetCardsInDeckPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['SetCardsInDeckPayload'] = ResolversParentTypes['SetCardsInDeckPayload']> = {
  cardsInDecks?: Resolver<Maybe<Array<Maybe<ResolversTypes['CardsInDeck']>>>, ParentType, ContextType>;
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
  cardsInDeck?: Resolver<Maybe<ResolversTypes['CardsInDeck']>, ParentType, ContextType>;
  cardsInDeckEdge?: Resolver<Maybe<ResolversTypes['CardsInDecksEdge']>, ParentType, ContextType, RequireFields<UpdateCardsInDeckPayloadCardsInDeckEdgeArgs, 'orderBy'>>;
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  deckByDeckId?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  publishedCardByCardId?: Resolver<Maybe<ResolversTypes['PublishedCard']>, ParentType, ContextType>;
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

export type UpdateGeneratedArtPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['UpdateGeneratedArtPayload'] = ResolversParentTypes['UpdateGeneratedArtPayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  generatedArt?: Resolver<Maybe<ResolversTypes['GeneratedArt']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type UpdatePublishedCardPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['UpdatePublishedCardPayload'] = ResolversParentTypes['UpdatePublishedCardPayload']> = {
  cardBySuccession?: Resolver<Maybe<ResolversTypes['Card']>, ParentType, ContextType>;
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  publishedCard?: Resolver<Maybe<ResolversTypes['PublishedCard']>, ParentType, ContextType>;
  publishedCardEdge?: Resolver<Maybe<ResolversTypes['PublishedCardsEdge']>, ParentType, ContextType, RequireFields<UpdatePublishedCardPayloadPublishedCardEdgeArgs, 'orderBy'>>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type Resolvers<ContextType = any> = {
  ArchiveCardPayload?: ArchiveCardPayloadResolvers<ContextType>;
  BigInt?: GraphQLScalarType;
  Card?: CardResolvers<ContextType>;
  CardsConnection?: CardsConnectionResolvers<ContextType>;
  CardsEdge?: CardsEdgeResolvers<ContextType>;
  CardsInDeck?: CardsInDeckResolvers<ContextType>;
  CardsInDecksConnection?: CardsInDecksConnectionResolvers<ContextType>;
  CardsInDecksEdge?: CardsInDecksEdgeResolvers<ContextType>;
  Class?: ClassResolvers<ContextType>;
  ClassesConnection?: ClassesConnectionResolvers<ContextType>;
  ClassesEdge?: ClassesEdgeResolvers<ContextType>;
  CollectionCard?: CollectionCardResolvers<ContextType>;
  CollectionCardsConnection?: CollectionCardsConnectionResolvers<ContextType>;
  CollectionCardsEdge?: CollectionCardsEdgeResolvers<ContextType>;
  CreateCardPayload?: CreateCardPayloadResolvers<ContextType>;
  CreateCardsInDeckPayload?: CreateCardsInDeckPayloadResolvers<ContextType>;
  CreateDeckPayload?: CreateDeckPayloadResolvers<ContextType>;
  CreateDeckWithCardsPayload?: CreateDeckWithCardsPayloadResolvers<ContextType>;
  CreateGeneratedArtPayload?: CreateGeneratedArtPayloadResolvers<ContextType>;
  CreatePublishedCardPayload?: CreatePublishedCardPayloadResolvers<ContextType>;
  Cursor?: GraphQLScalarType;
  Datetime?: GraphQLScalarType;
  Deck?: DeckResolvers<ContextType>;
  DeckShare?: DeckShareResolvers<ContextType>;
  DeckSharesConnection?: DeckSharesConnectionResolvers<ContextType>;
  DeckSharesEdge?: DeckSharesEdgeResolvers<ContextType>;
  DecksConnection?: DecksConnectionResolvers<ContextType>;
  DecksEdge?: DecksEdgeResolvers<ContextType>;
  DeleteCardsInDeckPayload?: DeleteCardsInDeckPayloadResolvers<ContextType>;
  DeletePublishedCardPayload?: DeletePublishedCardPayloadResolvers<ContextType>;
  GeneratedArt?: GeneratedArtResolvers<ContextType>;
  GeneratedArtsConnection?: GeneratedArtsConnectionResolvers<ContextType>;
  GeneratedArtsEdge?: GeneratedArtsEdgeResolvers<ContextType>;
  GetClassesPayload?: GetClassesPayloadResolvers<ContextType>;
  GetClassesRecord?: GetClassesRecordResolvers<ContextType>;
  GetCollectionCardsPayload?: GetCollectionCardsPayloadResolvers<ContextType>;
  GetCollectionCardsRecord?: GetCollectionCardsRecordResolvers<ContextType>;
  JSON?: GraphQLScalarType;
  Mutation?: MutationResolvers<ContextType>;
  Node?: NodeResolvers<ContextType>;
  PageInfo?: PageInfoResolvers<ContextType>;
  PublishCardPayload?: PublishCardPayloadResolvers<ContextType>;
  PublishedCard?: PublishedCardResolvers<ContextType>;
  PublishedCardsConnection?: PublishedCardsConnectionResolvers<ContextType>;
  PublishedCardsEdge?: PublishedCardsEdgeResolvers<ContextType>;
  Query?: QueryResolvers<ContextType>;
  RogueRun?: RogueRunResolvers<ContextType>;
  RogueRunsConnection?: RogueRunsConnectionResolvers<ContextType>;
  RogueRunsEdge?: RogueRunsEdgeResolvers<ContextType>;
  SaveCardPayload?: SaveCardPayloadResolvers<ContextType>;
  SaveGeneratedArtPayload?: SaveGeneratedArtPayloadResolvers<ContextType>;
  SetCardsInDeckPayload?: SetCardsInDeckPayloadResolvers<ContextType>;
  UpdateCardPayload?: UpdateCardPayloadResolvers<ContextType>;
  UpdateCardsInDeckPayload?: UpdateCardsInDeckPayloadResolvers<ContextType>;
  UpdateDeckPayload?: UpdateDeckPayloadResolvers<ContextType>;
  UpdateGeneratedArtPayload?: UpdateGeneratedArtPayloadResolvers<ContextType>;
  UpdatePublishedCardPayload?: UpdatePublishedCardPayloadResolvers<ContextType>;
};


export type CardFragment = { __typename?: 'Card', id: string, createdBy: string, cardScript?: any | null, blocklyWorkspace?: any | null };

export type ClassFragment = { __typename?: 'Class', class?: string | null, collectible?: boolean | null, isPublished?: boolean | null, cardScript?: any | null, id?: string | null, name?: string | null };

export type CollectionCardFragment = { __typename?: 'CollectionCard', id?: string | null, createdBy?: string | null, cardScript?: any | null, blocklyWorkspace?: any | null, collectible?: boolean | null, cost?: number | null, type?: string | null, lastModified?: any | null };

export type DeckFragment = { __typename?: 'Deck', id: string, name?: string | null, isPremade: boolean, createdBy: string, heroClass?: string | null, format?: string | null, deckType: number };

export type DeckCardsFragment = { __typename?: 'Deck', cardsInDecksByDeckId: { __typename?: 'CardsInDecksConnection', totalCount: number, nodes: Array<{ __typename?: 'CardsInDeck', cardId: string, publishedCardByCardId?: { __typename?: 'PublishedCard', cardBySuccession?: { __typename?: 'Card', id: string, createdBy: string, cardScript?: any | null, blocklyWorkspace?: any | null } | null } | null } | null> } };

export type RogueRunFragment = { __typename?: 'RogueRun', id: any, player: string, seed: any, choices?: Array<string | null> | null, bossesDefeated: number, deckByDeck?: { __typename?: 'Deck', id: string, name?: string | null, isPremade: boolean, createdBy: string, heroClass?: string | null, format?: string | null, deckType: number, cardsInDecksByDeckId: { __typename?: 'CardsInDecksConnection', totalCount: number, nodes: Array<{ __typename?: 'CardsInDeck', cardId: string, publishedCardByCardId?: { __typename?: 'PublishedCard', cardBySuccession?: { __typename?: 'Card', id: string, createdBy: string, cardScript?: any | null, blocklyWorkspace?: any | null } | null } | null } | null> } } | null };

export type CreateDeckMutationVariables = Exact<{
  deckName: Scalars['String'];
  heroClass: Scalars['String'];
  cardIds?: InputMaybe<Array<Scalars['String']> | Scalars['String']>;
  format: Scalars['String'];
}>;


export type CreateDeckMutation = { __typename?: 'Mutation', createDeckWithCards?: { __typename?: 'CreateDeckWithCardsPayload', deck?: { __typename?: 'Deck', id: string, name?: string | null, isPremade: boolean, createdBy: string, heroClass?: string | null, format?: string | null, deckType: number, cardsInDecksByDeckId: { __typename?: 'CardsInDecksConnection', totalCount: number, nodes: Array<{ __typename?: 'CardsInDeck', cardId: string, publishedCardByCardId?: { __typename?: 'PublishedCard', cardBySuccession?: { __typename?: 'Card', id: string, createdBy: string, cardScript?: any | null, blocklyWorkspace?: any | null } | null } | null } | null> } } | null } | null };

export type DeleteCardMutationVariables = Exact<{
  cardId: Scalars['String'];
}>;


export type DeleteCardMutation = { __typename?: 'Mutation', archiveCard?: { __typename?: 'ArchiveCardPayload', clientMutationId?: string | null } | null };

export type DeleteDeckMutationVariables = Exact<{
  deckId: Scalars['String'];
}>;


export type DeleteDeckMutation = { __typename?: 'Mutation', updateDeckById?: { __typename?: 'UpdateDeckPayload', deck?: { __typename?: 'Deck', trashed: boolean } | null } | null };

export type SetCardsInDeckMutationVariables = Exact<{
  deckId: Scalars['String'];
  cardIds?: InputMaybe<Array<Scalars['String']> | Scalars['String']>;
}>;


export type SetCardsInDeckMutation = { __typename?: 'Mutation', setCardsInDeck?: { __typename?: 'SetCardsInDeckPayload', cardsInDecks?: Array<{ __typename?: 'CardsInDeck', id: any, cardId: string } | null> | null } | null };

export type StartRogueRunMutationVariables = Exact<{
  heroClass: Scalars['String'];
  seed: Scalars['BigInt'];
}>;


export type StartRogueRunMutation = { __typename?: 'Mutation', startRogueRun: { __typename?: 'RogueRun', id: any, player: string, seed: any, choices?: Array<string | null> | null, bossesDefeated: number, deckByDeck?: { __typename?: 'Deck', id: string, name?: string | null, isPremade: boolean, createdBy: string, heroClass?: string | null, format?: string | null, deckType: number, cardsInDecksByDeckId: { __typename?: 'CardsInDecksConnection', totalCount: number, nodes: Array<{ __typename?: 'CardsInDeck', cardId: string, publishedCardByCardId?: { __typename?: 'PublishedCard', cardBySuccession?: { __typename?: 'Card', id: string, createdBy: string, cardScript?: any | null, blocklyWorkspace?: any | null } | null } | null } | null> } } | null } };

export type GetCardQueryVariables = Exact<{
  id: Scalars['String'];
}>;


export type GetCardQuery = { __typename?: 'Query', getLatestCard?: { __typename?: 'Card', id: string, createdBy: string, cardScript?: any | null, blocklyWorkspace?: any | null } | null };

export type GetCardsQueryVariables = Exact<{
  limit?: InputMaybe<Scalars['Int']>;
  filter?: InputMaybe<CardFilter>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<CardsOrderBy> | CardsOrderBy>;
}>;


export type GetCardsQuery = { __typename?: 'Query', allCards?: { __typename?: 'CardsConnection', totalCount: number, nodes: Array<{ __typename?: 'Card', id: string, createdBy: string, cardScript?: any | null, blocklyWorkspace?: any | null } | null> } | null };

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


export type GetCollectionCardsQuery = { __typename?: 'Query', allCollectionCards?: { __typename?: 'CollectionCardsConnection', totalCount: number, nodes: Array<{ __typename?: 'CollectionCard', id?: string | null, createdBy?: string | null, cardScript?: any | null, blocklyWorkspace?: any | null, collectible?: boolean | null, cost?: number | null, type?: string | null, lastModified?: any | null } | null> } | null };

export type GetDeckQueryVariables = Exact<{
  deckId: Scalars['String'];
}>;


export type GetDeckQuery = { __typename?: 'Query', deckById?: { __typename?: 'Deck', id: string, name?: string | null, isPremade: boolean, createdBy: string, heroClass?: string | null, format?: string | null, deckType: number, cardsInDecksByDeckId: { __typename?: 'CardsInDecksConnection', totalCount: number, nodes: Array<{ __typename?: 'CardsInDeck', cardId: string, publishedCardByCardId?: { __typename?: 'PublishedCard', cardBySuccession?: { __typename?: 'Card', id: string, createdBy: string, cardScript?: any | null, blocklyWorkspace?: any | null } | null } | null } | null> } } | null };

export type GetDecksQueryVariables = Exact<{
  user?: InputMaybe<Scalars['String']>;
}>;


export type GetDecksQuery = { __typename?: 'Query', allDecks?: { __typename?: 'DecksConnection', nodes: Array<{ __typename?: 'Deck', id: string, name?: string | null, isPremade: boolean, createdBy: string, heroClass?: string | null, format?: string | null, deckType: number } | null> } | null, allDeckShares?: { __typename?: 'DeckSharesConnection', nodes: Array<{ __typename?: 'DeckShare', deckByDeckId?: { __typename?: 'Deck', id: string, name?: string | null, isPremade: boolean, createdBy: string, heroClass?: string | null, format?: string | null, deckType: number } | null } | null> } | null };

export type GeneratedArtFragment = { __typename?: 'GeneratedArt', hash: string, owner: string, urls: Array<string | null>, info?: any | null, isArchived: boolean };

export type DeleteArtMutationVariables = Exact<{
  hash: Scalars['String'];
  owner: Scalars['String'];
}>;


export type DeleteArtMutation = { __typename?: 'Mutation', updateGeneratedArtByHashAndOwner?: { __typename?: 'UpdateGeneratedArtPayload', generatedArt?: { __typename?: 'GeneratedArt', hash: string, owner: string, urls: Array<string | null>, info?: any | null, isArchived: boolean } | null } | null };

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
  blocklyWorkspace?: InputMaybe<Scalars['JSON']>;
  cardScript?: InputMaybe<Scalars['JSON']>;
}>;


export type SaveCardMutation = { __typename?: 'Mutation', saveCard?: { __typename?: 'SaveCardPayload', card?: { __typename?: 'Card', id: string, createdBy: string, cardScript?: any | null, blocklyWorkspace?: any | null } | null } | null };

export type SaveGeneratedArtMutationVariables = Exact<{
  hash: Scalars['String'];
  urls: Array<Scalars['String']> | Scalars['String'];
  info?: InputMaybe<Scalars['JSON']>;
}>;


export type SaveGeneratedArtMutation = { __typename?: 'Mutation', saveGeneratedArt?: { __typename?: 'SaveGeneratedArtPayload', generatedArt?: { __typename?: 'GeneratedArt', hash: string, owner: string, urls: Array<string | null>, info?: any | null, isArchived: boolean } | null } | null };

export type GetGeneratedArtQueryVariables = Exact<{ [key: string]: never; }>;


export type GetGeneratedArtQuery = { __typename?: 'Query', allGeneratedArts?: { __typename?: 'GeneratedArtsConnection', nodes: Array<{ __typename?: 'GeneratedArt', hash: string, owner: string, urls: Array<string | null>, info?: any | null, isArchived: boolean } | null> } | null };

export type GetUserIdTestQueryVariables = Exact<{ [key: string]: never; }>;


export type GetUserIdTestQuery = { __typename?: 'Query', currentUserId?: string | null };

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
  deckType
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
      publishedCardByCardId {
        cardBySuccession {
          ...card
        }
      }
    }
    totalCount
  }
}
    ${CardFragmentDoc}`;
export const RogueRunFragmentDoc = gql`
    fragment rogueRun on RogueRun {
  id
  player
  seed
  choices
  bossesDefeated
  deckByDeck {
    ...deck
    ...deckCards
  }
}
    ${DeckFragmentDoc}
${DeckCardsFragmentDoc}`;
export const GeneratedArtFragmentDoc = gql`
    fragment generatedArt on GeneratedArt {
  hash
  owner
  urls
  info
  isArchived
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
export const StartRogueRunDocument = gql`
    mutation startRogueRun($heroClass: String!, $seed: BigInt!) {
  startRogueRun(heroClass: $heroClass, seed: $seed) {
    ...rogueRun
  }
}
    ${RogueRunFragmentDoc}`;
export type StartRogueRunMutationFn = Apollo.MutationFunction<StartRogueRunMutation, StartRogueRunMutationVariables>;

/**
 * __useStartRogueRunMutation__
 *
 * To run a mutation, you first call `useStartRogueRunMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useStartRogueRunMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [startRogueRunMutation, { data, loading, error }] = useStartRogueRunMutation({
 *   variables: {
 *      heroClass: // value for 'heroClass'
 *      seed: // value for 'seed'
 *   },
 * });
 */
export function useStartRogueRunMutation(baseOptions?: Apollo.MutationHookOptions<StartRogueRunMutation, StartRogueRunMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<StartRogueRunMutation, StartRogueRunMutationVariables>(StartRogueRunDocument, options);
      }
export type StartRogueRunMutationHookResult = ReturnType<typeof useStartRogueRunMutation>;
export type StartRogueRunMutationResult = Apollo.MutationResult<StartRogueRunMutation>;
export type StartRogueRunMutationOptions = Apollo.BaseMutationOptions<StartRogueRunMutation, StartRogueRunMutationVariables>;
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
export const DeleteArtDocument = gql`
    mutation deleteArt($hash: String!, $owner: String!) {
  updateGeneratedArtByHashAndOwner(
    input: {owner: $owner, hash: $hash, generatedArtPatch: {isArchived: true}}
  ) {
    generatedArt {
      ...generatedArt
    }
  }
}
    ${GeneratedArtFragmentDoc}`;
export type DeleteArtMutationFn = Apollo.MutationFunction<DeleteArtMutation, DeleteArtMutationVariables>;

/**
 * __useDeleteArtMutation__
 *
 * To run a mutation, you first call `useDeleteArtMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useDeleteArtMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [deleteArtMutation, { data, loading, error }] = useDeleteArtMutation({
 *   variables: {
 *      hash: // value for 'hash'
 *      owner: // value for 'owner'
 *   },
 * });
 */
export function useDeleteArtMutation(baseOptions?: Apollo.MutationHookOptions<DeleteArtMutation, DeleteArtMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<DeleteArtMutation, DeleteArtMutationVariables>(DeleteArtDocument, options);
      }
export type DeleteArtMutationHookResult = ReturnType<typeof useDeleteArtMutation>;
export type DeleteArtMutationResult = Apollo.MutationResult<DeleteArtMutation>;
export type DeleteArtMutationOptions = Apollo.BaseMutationOptions<DeleteArtMutation, DeleteArtMutationVariables>;
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
    mutation saveCard($cardId: String!, $blocklyWorkspace: JSON, $cardScript: JSON) {
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
export const SaveGeneratedArtDocument = gql`
    mutation saveGeneratedArt($hash: String!, $urls: [String!]!, $info: JSON) {
  saveGeneratedArt(input: {digest: $hash, links: $urls, extraInfo: $info}) {
    generatedArt {
      ...generatedArt
    }
  }
}
    ${GeneratedArtFragmentDoc}`;
export type SaveGeneratedArtMutationFn = Apollo.MutationFunction<SaveGeneratedArtMutation, SaveGeneratedArtMutationVariables>;

/**
 * __useSaveGeneratedArtMutation__
 *
 * To run a mutation, you first call `useSaveGeneratedArtMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useSaveGeneratedArtMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [saveGeneratedArtMutation, { data, loading, error }] = useSaveGeneratedArtMutation({
 *   variables: {
 *      hash: // value for 'hash'
 *      urls: // value for 'urls'
 *      info: // value for 'info'
 *   },
 * });
 */
export function useSaveGeneratedArtMutation(baseOptions?: Apollo.MutationHookOptions<SaveGeneratedArtMutation, SaveGeneratedArtMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<SaveGeneratedArtMutation, SaveGeneratedArtMutationVariables>(SaveGeneratedArtDocument, options);
      }
export type SaveGeneratedArtMutationHookResult = ReturnType<typeof useSaveGeneratedArtMutation>;
export type SaveGeneratedArtMutationResult = Apollo.MutationResult<SaveGeneratedArtMutation>;
export type SaveGeneratedArtMutationOptions = Apollo.BaseMutationOptions<SaveGeneratedArtMutation, SaveGeneratedArtMutationVariables>;
export const GetGeneratedArtDocument = gql`
    query getGeneratedArt {
  allGeneratedArts {
    nodes {
      ...generatedArt
    }
  }
}
    ${GeneratedArtFragmentDoc}`;

/**
 * __useGetGeneratedArtQuery__
 *
 * To run a query within a React component, call `useGetGeneratedArtQuery` and pass it any options that fit your needs.
 * When your component renders, `useGetGeneratedArtQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useGetGeneratedArtQuery({
 *   variables: {
 *   },
 * });
 */
export function useGetGeneratedArtQuery(baseOptions?: Apollo.QueryHookOptions<GetGeneratedArtQuery, GetGeneratedArtQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<GetGeneratedArtQuery, GetGeneratedArtQueryVariables>(GetGeneratedArtDocument, options);
      }
export function useGetGeneratedArtLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<GetGeneratedArtQuery, GetGeneratedArtQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<GetGeneratedArtQuery, GetGeneratedArtQueryVariables>(GetGeneratedArtDocument, options);
        }
export type GetGeneratedArtQueryHookResult = ReturnType<typeof useGetGeneratedArtQuery>;
export type GetGeneratedArtLazyQueryHookResult = ReturnType<typeof useGetGeneratedArtLazyQuery>;
export type GetGeneratedArtQueryResult = Apollo.QueryResult<GetGeneratedArtQuery, GetGeneratedArtQueryVariables>;
export const GetUserIdTestDocument = gql`
    query getUserIdTest {
  currentUserId
}
    `;

/**
 * __useGetUserIdTestQuery__
 *
 * To run a query within a React component, call `useGetUserIdTestQuery` and pass it any options that fit your needs.
 * When your component renders, `useGetUserIdTestQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useGetUserIdTestQuery({
 *   variables: {
 *   },
 * });
 */
export function useGetUserIdTestQuery(baseOptions?: Apollo.QueryHookOptions<GetUserIdTestQuery, GetUserIdTestQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<GetUserIdTestQuery, GetUserIdTestQueryVariables>(GetUserIdTestDocument, options);
      }
export function useGetUserIdTestLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<GetUserIdTestQuery, GetUserIdTestQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<GetUserIdTestQuery, GetUserIdTestQueryVariables>(GetUserIdTestDocument, options);
        }
export type GetUserIdTestQueryHookResult = ReturnType<typeof useGetUserIdTestQuery>;
export type GetUserIdTestLazyQueryHookResult = ReturnType<typeof useGetUserIdTestLazyQuery>;
export type GetUserIdTestQueryResult = Apollo.QueryResult<GetUserIdTestQuery, GetUserIdTestQueryVariables>;