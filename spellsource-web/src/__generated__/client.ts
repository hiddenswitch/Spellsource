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

export type AcceptInviteInput = {
  deckId?: InputMaybe<Scalars['String']>;
  inviteId: Scalars['String'];
  queueId?: InputMaybe<Scalars['String']>;
};

export type AccessToken = {
  __typename?: 'AccessToken';
  token: Scalars['String'];
};

export const ActionType = {
  Battlecry: 'BATTLECRY',
  Discover: 'DISCOVER',
  EndTurn: 'END_TURN',
  EquipWeapon: 'EQUIP_WEAPON',
  Hero: 'HERO',
  HeroPower: 'HERO_POWER',
  PhysicalAttack: 'PHYSICAL_ATTACK',
  Spell: 'SPELL',
  Summon: 'SUMMON',
  System: 'SYSTEM',
  Tap: 'TAP'
} as const;

export type ActionType = typeof ActionType[keyof typeof ActionType];
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

export type AttributeValueInput = {
  attribute: PlayerEntityAttribute;
  stringValue: Scalars['String'];
};

/** ─── Deck types ─────────────────────────────────────────────── */
export type AttributeValueTuple = {
  __typename?: 'AttributeValueTuple';
  attribute: PlayerEntityAttribute;
  stringValue: Scalars['String'];
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

/** ─── Card catalogue ─────────────────────────────────────────── */
export type CardRecord = {
  __typename?: 'CardRecord';
  cardId?: Maybe<Scalars['String']>;
  collectionIds: Array<Scalars['String']>;
  count: Scalars['Int'];
  entity: Entity;
  id: Scalars['BigInt'];
  userId?: Maybe<Scalars['String']>;
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

/** ─── Enums ──────────────────────────────────────────────────── */
export const CardType = {
  ChooseOne: 'CHOOSE_ONE',
  Class: 'CLASS',
  Enchantment: 'ENCHANTMENT',
  Format: 'FORMAT',
  Group: 'GROUP',
  Hero: 'HERO',
  HeroPower: 'HERO_POWER',
  Minion: 'MINION',
  RogueChoice: 'ROGUE_CHOICE',
  Spell: 'SPELL',
  Weapon: 'WEAPON'
} as const;

export type CardType = typeof CardType[keyof typeof CardType];
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
export type ClientConfiguration = {
  __typename?: 'ClientConfiguration';
  graphQlUrl?: Maybe<Scalars['String']>;
  keycloakAccountManagementUrl?: Maybe<Scalars['String']>;
  keycloakResetPasswordUrl?: Maybe<Scalars['String']>;
};

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
export const CollectionType = {
  Alliance: 'ALLIANCE',
  Deck: 'DECK',
  User: 'USER'
} as const;

export type CollectionType = typeof CollectionType[keyof typeof CollectionType];
/** ─── Input types ────────────────────────────────────────────── */
export type CreateAccountInput = {
  decks?: InputMaybe<Scalars['Boolean']>;
  email: Scalars['String'];
  guest?: InputMaybe<Scalars['Boolean']>;
  password: Scalars['String'];
  username: Scalars['String'];
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

export const DeckType = {
  Constructed: 'CONSTRUCTED',
  Draft: 'DRAFT',
  Rogue: 'ROGUE'
} as const;

export type DeckType = typeof DeckType[keyof typeof DeckType];
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

export type DecksGetResponse = {
  __typename?: 'DecksGetResponse';
  collection?: Maybe<InventoryCollection>;
  inventoryIdsSize: Scalars['Int'];
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
export type DecksPutInput = {
  cardIds?: InputMaybe<Array<Scalars['String']>>;
  deckList?: InputMaybe<Scalars['String']>;
  format?: InputMaybe<Scalars['String']>;
  heroClass: Scalars['String'];
  name: Scalars['String'];
};

export type DecksPutResponse = {
  __typename?: 'DecksPutResponse';
  collection?: Maybe<InventoryCollection>;
  deckId: Scalars['String'];
};

export type DecksUpdateInput = {
  deckId: Scalars['String'];
  pullAllCardIds?: InputMaybe<Array<Scalars['String']>>;
  pushCardIds?: InputMaybe<Array<Scalars['String']>>;
  setHeroClass?: InputMaybe<Scalars['String']>;
  setName?: InputMaybe<Scalars['String']>;
  setPlayerEntityAttribute?: InputMaybe<AttributeValueInput>;
  unsetPlayerEntityAttribute?: InputMaybe<Scalars['String']>;
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

/** ─── Draft types ────────────────────────────────────────────── */
export type DraftState = {
  __typename?: 'DraftState';
  cardsRemaining: Scalars['Int'];
  currentCardChoices: Array<Entity>;
  deckId: Scalars['String'];
  draftIndex: Scalars['Int'];
  heroClass?: Maybe<Entity>;
  heroClassChoices: Array<Entity>;
  losses: Scalars['Int'];
  selectedCardIds: Array<Scalars['String']>;
  status: DraftStatus;
  wins: Scalars['Int'];
};

export const DraftStatus = {
  Complete: 'COMPLETE',
  InProgress: 'IN_PROGRESS',
  Retired: 'RETIRED',
  SelectHero: 'SELECT_HERO'
} as const;

export type DraftStatus = typeof DraftStatus[keyof typeof DraftStatus];
export type DraftsPostInput = {
  retireEarly?: InputMaybe<Scalars['Boolean']>;
  startDraft?: InputMaybe<Scalars['Boolean']>;
};

/** ─── Editable card types ───────────────────────────────────── */
export type EditableCard = {
  __typename?: 'EditableCard';
  id: Scalars['String'];
  ownerUserId: Scalars['String'];
  source: Scalars['String'];
};

export type Emote = {
  __typename?: 'Emote';
  entityId: Scalars['Int'];
  message: EmoteType;
};

export const EmoteType = {
  Amazing: 'AMAZING',
  FaceMyWrath: 'FACE_MY_WRATH',
  GoodGame: 'GOOD_GAME',
  Hello: 'HELLO',
  WellPlayed: 'WELL_PLAYED',
  Whoops: 'WHOOPS'
} as const;

export type EmoteType = typeof EmoteType[keyof typeof EmoteType];
/** All input for the `endRogueRun` mutation. */
export type EndRogueRunInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']>;
  rogueId?: InputMaybe<Scalars['BigInt']>;
};

/** The output of our `endRogueRun` mutation. */
export type EndRogueRunPayload = {
  __typename?: 'EndRogueRunPayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']>;
  /** Reads a single `Deck` that is related to this `RogueRun`. */
  deckByDeck?: Maybe<Deck>;
  /** Reads a single `Deck` that is related to this `RogueRun`. */
  deckByOpponentDeck?: Maybe<Deck>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
  rogueRun?: Maybe<RogueRun>;
  /** An edge for our `RogueRun`. May be used by Relay 1. */
  rogueRunEdge?: Maybe<RogueRunsEdge>;
};


/** The output of our `endRogueRun` mutation. */
export type EndRogueRunPayloadRogueRunEdgeArgs = {
  orderBy?: Array<RogueRunsOrderBy>;
};

export type Entity = {
  __typename?: 'Entity';
  armor?: Maybe<Scalars['Int']>;
  /** stats */
  attack?: Maybe<Scalars['Int']>;
  baseAttack?: Maybe<Scalars['Int']>;
  baseHp?: Maybe<Scalars['Int']>;
  baseManaCost?: Maybe<Scalars['Int']>;
  /** boolean flags */
  battlecry: Scalars['Boolean'];
  boardPosition: Scalars['Int'];
  cannotAttack: Scalars['Boolean'];
  cardId: Scalars['String'];
  cardSet: Scalars['String'];
  cardSets: Array<Scalars['String']>;
  cardType: CardType;
  charge: Scalars['Boolean'];
  /** misc */
  charges?: Maybe<Scalars['Int']>;
  chooseOne: Scalars['Boolean'];
  collectible: Scalars['Boolean'];
  combo: Scalars['Boolean'];
  conditionMet: Scalars['Boolean'];
  countUntilCast?: Maybe<Scalars['Int']>;
  deathrattles: Scalars['Boolean'];
  deflect: Scalars['Boolean'];
  description: Scalars['String'];
  destroyed: Scalars['Boolean'];
  discarded: Scalars['Boolean'];
  divineShield: Scalars['Boolean'];
  durability?: Maybe<Scalars['Int']>;
  enchantmentType: Scalars['String'];
  enraged: Scalars['Boolean'];
  entityType: EntityType;
  extraAttack?: Maybe<Scalars['Int']>;
  fires?: Maybe<Scalars['Int']>;
  frozen: Scalars['Boolean'];
  gameStarted: Scalars['Boolean'];
  gold: Scalars['Boolean'];
  heroClasses: Array<Scalars['String']>;
  host: Scalars['Int'];
  hostsTrigger: Scalars['Boolean'];
  hp?: Maybe<Scalars['Int']>;
  id: Scalars['Int'];
  immune: Scalars['Boolean'];
  isStartingTurn: Scalars['Boolean'];
  lifesteal: Scalars['Boolean'];
  location?: Maybe<EntityLocation>;
  lockedMana: Scalars['Int'];
  /** player resource fields */
  mana: Scalars['Int'];
  manaCost?: Maybe<Scalars['Int']>;
  maxHp?: Maybe<Scalars['Int']>;
  maxMana: Scalars['Int'];
  name: Scalars['String'];
  note: Scalars['String'];
  overload?: Maybe<Scalars['Int']>;
  owner: Scalars['Int'];
  permanent: Scalars['Boolean'];
  playable: Scalars['Boolean'];
  poisonous: Scalars['Boolean'];
  rarity: Rarity;
  roasted: Scalars['Boolean'];
  rush: Scalars['Boolean'];
  silenced: Scalars['Boolean'];
  spellDamage?: Maybe<Scalars['Int']>;
  stealth: Scalars['Boolean'];
  summoningSickness: Scalars['Boolean'];
  taunt: Scalars['Boolean'];
  tooltips: Array<Tooltip>;
  tribes: Array<Scalars['String']>;
  uncensored: Scalars['Boolean'];
  underAura: Scalars['Boolean'];
  untargetableBySpells: Scalars['Boolean'];
  windfury: Scalars['Boolean'];
};

/** ─── Core game entity types ────────────────────────────────── */
export type EntityLocation = {
  __typename?: 'EntityLocation';
  index: Scalars['Int'];
  player: Scalars['Int'];
  zone: Zone;
};

export const EntityType = {
  Actor: 'ACTOR',
  Any: 'ANY',
  Card: 'CARD',
  Enchantment: 'ENCHANTMENT',
  Hero: 'HERO',
  Minion: 'MINION',
  Player: 'PLAYER',
  Quest: 'QUEST',
  Secret: 'SECRET',
  Weapon: 'WEAPON'
} as const;

export type EntityType = typeof EntityType[keyof typeof EntityType];
export type Friend = {
  __typename?: 'Friend';
  friendId: Scalars['String'];
  friendName: Scalars['String'];
  presence: Presence;
  since: Scalars['BigInt'];
};

export type GameActions = {
  __typename?: 'GameActions';
  all: Array<SpellAction>;
  compatibility: Array<Scalars['Int']>;
};

export type GameEvent = {
  __typename?: 'GameEvent';
  description: Scalars['String'];
  eventType: GameEventType;
  id: Scalars['Int'];
  isPowerHistory: Scalars['Boolean'];
  isSourcePlayerLocal: Scalars['Boolean'];
  isTargetPlayerLocal: Scalars['Boolean'];
  source?: Maybe<Entity>;
  target?: Maybe<Entity>;
  targets: Array<Entity>;
  value?: Maybe<Scalars['Int']>;
};

export const GameEventType = {
  AfterPhysicalAttack: 'AFTER_PHYSICAL_ATTACK',
  AfterPlayCard: 'AFTER_PLAY_CARD',
  AfterSpellCasted: 'AFTER_SPELL_CASTED',
  AfterSummon: 'AFTER_SUMMON',
  All: 'ALL',
  ArmorGained: 'ARMOR_GAINED',
  AttributeApplied: 'ATTRIBUTE_APPLIED',
  BeforePhysicalAttack: 'BEFORE_PHYSICAL_ATTACK',
  BeforeSummon: 'BEFORE_SUMMON',
  BoardChanged: 'BOARD_CHANGED',
  CardAddedToDeck: 'CARD_ADDED_TO_DECK',
  CardShuffled: 'CARD_SHUFFLED',
  Damage: 'DAMAGE',
  Decay: 'DECAY',
  DestroyWillQueue: 'DESTROY_WILL_QUEUE',
  DidEndSequence: 'DID_END_SEQUENCE',
  Discard: 'DISCARD',
  Discover: 'DISCOVER',
  Drain: 'DRAIN',
  DrawCard: 'DRAW_CARD',
  EnrageChanged: 'ENRAGE_CHANGED',
  EntityTouched: 'ENTITY_TOUCHED',
  EntityUntouched: 'ENTITY_UNTOUCHED',
  ExcessHeal: 'EXCESS_HEAL',
  Fatigue: 'FATIGUE',
  GameInitialized: 'GAME_INITIALIZED',
  GameStart: 'GAME_START',
  Heal: 'HEAL',
  HeroPowerUsed: 'HERO_POWER_USED',
  HonorableKill: 'HONORABLE_KILL',
  Invoked: 'INVOKED',
  Joust: 'JOUST',
  Kill: 'KILL',
  LoseDeflect: 'LOSE_DEFLECT',
  LoseDivineShield: 'LOSE_DIVINE_SHIELD',
  LoseStealth: 'LOSE_STEALTH',
  ManaModified: 'MANA_MODIFIED',
  MaxHpIncreased: 'MAX_HP_INCREASED',
  MaxMana: 'MAX_MANA',
  MissileFired: 'MISSILE_FIRED',
  Overload: 'OVERLOAD',
  PerformedGameAction: 'PERFORMED_GAME_ACTION',
  PhysicalAttack: 'PHYSICAL_ATTACK',
  PlayCard: 'PLAY_CARD',
  PreDamage: 'PRE_DAMAGE',
  PreGameStart: 'PRE_GAME_START',
  QuestPlayed: 'QUEST_PLAYED',
  QuestSuccessful: 'QUEST_SUCCESSFUL',
  ReturnedToHand: 'RETURNED_TO_HAND',
  RevealCard: 'REVEAL_CARD',
  Roasted: 'ROASTED',
  RogueChoice: 'ROGUE_CHOICE',
  SecretPlayed: 'SECRET_PLAYED',
  SecretRevealed: 'SECRET_REVEALED',
  Silence: 'SILENCE',
  SpellCasted: 'SPELL_CASTED',
  Summon: 'SUMMON',
  TapActivated: 'TAP_ACTIVATED',
  TargetAcquisition: 'TARGET_ACQUISITION',
  TriggerFired: 'TRIGGER_FIRED',
  TurnEnd: 'TURN_END',
  TurnStart: 'TURN_START',
  WeaponDestroyed: 'WEAPON_DESTROYED',
  WeaponEquipped: 'WEAPON_EQUIPPED',
  WillEndSequence: 'WILL_END_SEQUENCE'
} as const;

export type GameEventType = typeof GameEventType[keyof typeof GameEventType];
export type GameOver = {
  __typename?: 'GameOver';
  localPlayerWon: Scalars['Boolean'];
  winningPlayerId?: Maybe<Scalars['Int']>;
};

/** ─── Game record types ──────────────────────────────────────── */
export type GameRecord = {
  __typename?: 'GameRecord';
  completedAt: Scalars['BigInt'];
  completedAtLocalized?: Maybe<Scalars['String']>;
  isBotGame: Scalars['Boolean'];
  playerNames: Array<Scalars['String']>;
};

export type GameState = {
  __typename?: 'GameState';
  entities: Array<Entity>;
  isLocalPlayerTurn: Scalars['Boolean'];
  timestamp: Scalars['BigInt'];
  turnNumber: Scalars['Int'];
  turnState: Scalars['String'];
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
export type GetCardsResponse = {
  __typename?: 'GetCardsResponse';
  cachedOk: Scalars['Boolean'];
  cards: Array<CardRecord>;
  version: Scalars['String'];
};

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

export type InventoryCollection = {
  __typename?: 'InventoryCollection';
  collectionType: CollectionType;
  deckType: DeckType;
  format: Scalars['String'];
  heroClass: Scalars['String'];
  id: Scalars['String'];
  inventory: Array<CardRecord>;
  isStandardDeck: Scalars['Boolean'];
  name: Scalars['String'];
  playerEntityAttributes: Array<AttributeValueTuple>;
  userId?: Maybe<Scalars['String']>;
  validationReport?: Maybe<ValidationReport>;
};

/** ─── Invite types ───────────────────────────────────────────── */
export type Invite = {
  __typename?: 'Invite';
  expiresAt?: Maybe<Scalars['BigInt']>;
  friendId?: Maybe<Scalars['String']>;
  fromName?: Maybe<Scalars['String']>;
  fromUserId: Scalars['String'];
  id: Scalars['String'];
  message?: Maybe<Scalars['String']>;
  queueId?: Maybe<Scalars['String']>;
  status: InviteStatus;
  toName?: Maybe<Scalars['String']>;
  toUserId: Scalars['String'];
};

export type InvitePostInput = {
  deckId?: InputMaybe<Scalars['String']>;
  friend?: InputMaybe<Scalars['Boolean']>;
  message?: InputMaybe<Scalars['String']>;
  queueId?: InputMaybe<Scalars['String']>;
  toUserId?: InputMaybe<Scalars['String']>;
  toUserNameWithToken?: InputMaybe<Scalars['String']>;
};

export type InviteResponse = {
  __typename?: 'InviteResponse';
  invite?: Maybe<Invite>;
};

export const InviteStatus = {
  Accepted: 'ACCEPTED',
  Cancelled: 'CANCELLED',
  Pending: 'PENDING',
  Rejected: 'REJECTED',
  Timeout: 'TIMEOUT',
  Undelivered: 'UNDELIVERED'
} as const;

export type InviteStatus = typeof InviteStatus[keyof typeof InviteStatus];
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

export type LoginInput = {
  password: Scalars['String'];
  usernameOrEmail: Scalars['String'];
};

export type LoginOrCreateReply = {
  __typename?: 'LoginOrCreateReply';
  accessToken?: Maybe<AccessToken>;
  userEntity?: Maybe<UserEntity>;
};

export type MatchFound = {
  __typename?: 'MatchFound';
  gameId: Scalars['String'];
  playerKey: Scalars['String'];
  playerSecret: Scalars['String'];
  url: Scalars['String'];
};

export type MatchmakingEnqueueInput = {
  botDeckId?: InputMaybe<Scalars['String']>;
  deckId: Scalars['String'];
  queueId: Scalars['String'];
};

export type MatchmakingQueue = {
  __typename?: 'MatchmakingQueue';
  description: Scalars['String'];
  name: Scalars['String'];
  queueId: Scalars['String'];
  requires?: Maybe<MatchmakingQueueRequires>;
  tooltip: Scalars['String'];
};

/** ─── Matchmaking types ──────────────────────────────────────── */
export type MatchmakingQueueRequires = {
  __typename?: 'MatchmakingQueueRequires';
  deck: Scalars['Boolean'];
  deckIdChoices: Array<Scalars['String']>;
  heroClass: Scalars['Boolean'];
};

export const MessageType = {
  Concede: 'CONCEDE',
  Emote: 'EMOTE',
  FirstMessage: 'FIRST_MESSAGE',
  OnGameEnd: 'ON_GAME_END',
  OnGameEvent: 'ON_GAME_EVENT',
  OnMulligan: 'ON_MULLIGAN',
  OnRequestAction: 'ON_REQUEST_ACTION',
  OnUpdate: 'ON_UPDATE',
  Pingpong: 'PINGPONG',
  Timer: 'TIMER',
  Touch: 'TOUCH',
  UpdateAction: 'UPDATE_ACTION',
  UpdateMulligan: 'UPDATE_MULLIGAN'
} as const;

export type MessageType = typeof MessageType[keyof typeof MessageType];
/** ─── Mutation ───────────────────────────────────────────────── */
export type Mutation = {
  __typename?: 'Mutation';
  acceptInvite: InviteResponse;
  /** friends */
  addFriend: Friend;
  archiveCard?: Maybe<ArchiveCardPayload>;
  cancelMatchmaking: Scalars['Boolean'];
  changePassword: LoginOrCreateReply;
  /** Concede the current game. */
  concedeGame: Scalars['Boolean'];
  /** Connect to an active game. Must be called before gameMessages subscription will emit. */
  connectToGame: Scalars['Boolean'];
  /** auth */
  createAccount: LoginOrCreateReply;
  /** Creates a single `Card`. */
  createCard?: Maybe<CreateCardPayload>;
  /** Creates a single `CardsInDeck`. */
  createCardsInDeck?: Maybe<CreateCardsInDeckPayload>;
  /** decks */
  createDeck: DecksPutResponse;
  createDeckWithCards?: Maybe<CreateDeckWithCardsPayload>;
  /** Creates a single `GeneratedArt`. */
  createGeneratedArt?: Maybe<CreateGeneratedArtPayload>;
  /** Creates a single `PublishedCard`. */
  createPublishedCard?: Maybe<CreatePublishedCardPayload>;
  deleteCard: Scalars['Boolean'];
  /** Deletes a single `CardsInDeck` using its globally unique id. */
  deleteCardsInDeck?: Maybe<DeleteCardsInDeckPayload>;
  /** Deletes a single `CardsInDeck` using a unique key. */
  deleteCardsInDeckById?: Maybe<DeleteCardsInDeckPayload>;
  deleteDeck: Scalars['Boolean'];
  deleteInvite: InviteResponse;
  /** Deletes a single `PublishedCard` using its globally unique id. */
  deletePublishedCard?: Maybe<DeletePublishedCardPayload>;
  /** Deletes a single `PublishedCard` using a unique key. */
  deletePublishedCardById?: Maybe<DeletePublishedCardPayload>;
  draftsChooseCard: DraftState;
  draftsChooseHero: DraftState;
  duplicateDeck: DecksGetResponse;
  endRogueRun?: Maybe<EndRogueRunPayload>;
  /** matchmaking */
  enqueueMatchmaking: Scalars['Boolean'];
  getClasses?: Maybe<GetClassesPayload>;
  getCollectionCards?: Maybe<GetCollectionCardsPayload>;
  login: LoginOrCreateReply;
  makeRogueChoice: RogueRun;
  publishCard?: Maybe<PublishCardPayload>;
  /** cards (editable) */
  putCard: PutCardResult;
  removeFriend: Scalars['Boolean'];
  requestPasswordResetEmail: Scalars['Boolean'];
  reroll: RogueRun;
  saveCard?: Maybe<SaveCardPayload>;
  saveGeneratedArt?: Maybe<SaveGeneratedArtPayload>;
  /** Send an emote during a game. */
  sendEmote: Scalars['Boolean'];
  /** Send a game action in response to an ON_REQUEST_ACTION message. */
  sendGameAction: Scalars['Boolean'];
  /** invites */
  sendInvite: InviteResponse;
  /** Send a mulligan response in response to an ON_MULLIGAN message. */
  sendMulligan: Scalars['Boolean'];
  setCardsInDeck?: Maybe<SetCardsInDeckPayload>;
  skipBoss: RogueRun;
  /** drafts */
  startOrModifyDraft: DraftState;
  /** rogue (existing) */
  startRogueRun: RogueRun;
  trashCard: RogueRun;
  /** Updates a single `Card` using its globally unique id and a patch. */
  updateCard?: Maybe<UpdateCardPayload>;
  /** Updates a single `Card` using a unique key and a patch. */
  updateCardBySuccession?: Maybe<UpdateCardPayload>;
  /** Updates a single `CardsInDeck` using its globally unique id and a patch. */
  updateCardsInDeck?: Maybe<UpdateCardsInDeckPayload>;
  /** Updates a single `CardsInDeck` using a unique key and a patch. */
  updateCardsInDeckById?: Maybe<UpdateCardsInDeckPayload>;
  updateDeck: DecksGetResponse;
  /** Updates a single `Deck` using a unique key and a patch. */
  updateDeckById?: Maybe<UpdateDeckPayload>;
  /** Updates a single `GeneratedArt` using a unique key and a patch. */
  updateGeneratedArtByHashAndOwner?: Maybe<UpdateGeneratedArtPayload>;
  /** Updates a single `PublishedCard` using its globally unique id and a patch. */
  updatePublishedCard?: Maybe<UpdatePublishedCardPayload>;
  /** Updates a single `PublishedCard` using a unique key and a patch. */
  updatePublishedCardById?: Maybe<UpdatePublishedCardPayload>;
  upgradeCard: RogueRun;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationAcceptInviteArgs = {
  input: AcceptInviteInput;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationAddFriendArgs = {
  friendId?: InputMaybe<Scalars['String']>;
  usernameWithToken?: InputMaybe<Scalars['String']>;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationArchiveCardArgs = {
  input: ArchiveCardInput;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationChangePasswordArgs = {
  newPassword: Scalars['String'];
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationConnectToGameArgs = {
  playerKey: Scalars['String'];
  playerSecret: Scalars['String'];
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationCreateAccountArgs = {
  input: CreateAccountInput;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationCreateCardArgs = {
  input: CreateCardInput;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationCreateCardsInDeckArgs = {
  input: CreateCardsInDeckInput;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationCreateDeckArgs = {
  input: DecksPutInput;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationCreateDeckWithCardsArgs = {
  input: CreateDeckWithCardsInput;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationCreateGeneratedArtArgs = {
  input: CreateGeneratedArtInput;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationCreatePublishedCardArgs = {
  input: CreatePublishedCardInput;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationDeleteCardArgs = {
  editableCardId: Scalars['String'];
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationDeleteCardsInDeckArgs = {
  input: DeleteCardsInDeckInput;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationDeleteCardsInDeckByIdArgs = {
  input: DeleteCardsInDeckByIdInput;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationDeleteDeckArgs = {
  deckId: Scalars['String'];
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationDeleteInviteArgs = {
  inviteId: Scalars['String'];
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationDeletePublishedCardArgs = {
  input: DeletePublishedCardInput;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationDeletePublishedCardByIdArgs = {
  input: DeletePublishedCardByIdInput;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationDraftsChooseCardArgs = {
  cardIndex: Scalars['Int'];
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationDraftsChooseHeroArgs = {
  heroIndex: Scalars['Int'];
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationDuplicateDeckArgs = {
  deckId: Scalars['String'];
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationEndRogueRunArgs = {
  input: EndRogueRunInput;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationEnqueueMatchmakingArgs = {
  input: MatchmakingEnqueueInput;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationGetClassesArgs = {
  input: GetClassesInput;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationGetCollectionCardsArgs = {
  input: GetCollectionCardsInput;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationLoginArgs = {
  input: LoginInput;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationMakeRogueChoiceArgs = {
  choiceId: Scalars['BigInt'];
  choices: Array<Scalars['Int']>;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationPublishCardArgs = {
  input: PublishCardInput;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationPutCardArgs = {
  input: PutCardInput;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationRemoveFriendArgs = {
  friendId: Scalars['String'];
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationRerollArgs = {
  choiceId: Scalars['BigInt'];
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationSaveCardArgs = {
  input: SaveCardInput;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationSaveGeneratedArtArgs = {
  input: SaveGeneratedArtInput;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationSendEmoteArgs = {
  entityId: Scalars['Int'];
  message: EmoteType;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationSendGameActionArgs = {
  actionIndex: Scalars['Int'];
  repliesTo: Scalars['String'];
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationSendInviteArgs = {
  input: InvitePostInput;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationSendMulliganArgs = {
  discardedCardIndices: Array<Scalars['Int']>;
  repliesTo: Scalars['String'];
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationSetCardsInDeckArgs = {
  input: SetCardsInDeckInput;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationSkipBossArgs = {
  rogueId: Scalars['BigInt'];
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationStartOrModifyDraftArgs = {
  input: DraftsPostInput;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationStartRogueRunArgs = {
  heroClass: Scalars['String'];
  seed?: InputMaybe<Scalars['BigInt']>;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationTrashCardArgs = {
  cardId: Scalars['String'];
  rogueId: Scalars['BigInt'];
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationUpdateCardArgs = {
  input: UpdateCardInput;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationUpdateCardBySuccessionArgs = {
  input: UpdateCardBySuccessionInput;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationUpdateCardsInDeckArgs = {
  input: UpdateCardsInDeckInput;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationUpdateCardsInDeckByIdArgs = {
  input: UpdateCardsInDeckByIdInput;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationUpdateDeckArgs = {
  input: DecksUpdateInput;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationUpdateDeckByIdArgs = {
  input: UpdateDeckByIdInput;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationUpdateGeneratedArtByHashAndOwnerArgs = {
  input: UpdateGeneratedArtByHashAndOwnerInput;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationUpdatePublishedCardArgs = {
  input: UpdatePublishedCardInput;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationUpdatePublishedCardByIdArgs = {
  input: UpdatePublishedCardByIdInput;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationUpgradeCardArgs = {
  cardId: Scalars['String'];
  rogueId: Scalars['BigInt'];
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

export const PlayerEntityAttribute = {
  Signature: 'SIGNATURE'
} as const;

export type PlayerEntityAttribute = typeof PlayerEntityAttribute[keyof typeof PlayerEntityAttribute];
export const Presence = {
  InGame: 'IN_GAME',
  Offline: 'OFFLINE',
  Online: 'ONLINE',
  Unknown: 'UNKNOWN'
} as const;

export type Presence = typeof Presence[keyof typeof Presence];
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
export type PutCardInput = {
  draw?: InputMaybe<Scalars['Boolean']>;
  editableCardId?: InputMaybe<Scalars['String']>;
  source: Scalars['String'];
};

export type PutCardResult = {
  __typename?: 'PutCardResult';
  cardId: Scalars['String'];
  cardScriptErrors: Array<Scalars['String']>;
  editableCardId: Scalars['String'];
};

/** ─── Query ──────────────────────────────────────────────────── */
export type Query = Node & {
  __typename?: 'Query';
  /** account */
  account?: Maybe<UserEntity>;
  accounts: Array<UserEntity>;
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
  /** Reads and enables pagination through a set of `RogueChoice`. */
  allRogueChoices?: Maybe<RogueChoicesConnection>;
  /** Reads and enables pagination through a set of `RogueRun`. */
  allRogueRuns?: Maybe<RogueRunsConnection>;
  canSeeDeck?: Maybe<Scalars['Boolean']>;
  /** Reads a single `Card` using its globally unique `ID`. */
  card?: Maybe<Card>;
  /** Get a single `Card`. */
  cardBySuccession?: Maybe<Card>;
  /** cards */
  cards: GetCardsResponse;
  cardsByUser: GetCardsResponse;
  /** Reads a single `CardsInDeck` using its globally unique `ID`. */
  cardsInDeck?: Maybe<CardsInDeck>;
  /** Get a single `CardsInDeck`. */
  cardsInDeckById?: Maybe<CardsInDeck>;
  configuration: ClientConfiguration;
  currentRogueChoice?: Maybe<RogueChoice>;
  /** rogue (existing) */
  currentRogueClasses: Array<Scalars['String']>;
  currentRogueRun?: Maybe<RogueRun>;
  /** auth */
  currentUserId?: Maybe<Scalars['String']>;
  /** decks */
  deck: DecksGetResponse;
  /** Get a single `Deck`. */
  deckById?: Maybe<Deck>;
  /** Reads a single `DeckShare` using its globally unique `ID`. */
  deckShare?: Maybe<DeckShare>;
  /** Get a single `DeckShare`. */
  deckShareByDeckIdAndShareRecipientId?: Maybe<DeckShare>;
  decks: Array<DecksGetResponse>;
  /** drafts */
  draft: DraftState;
  /** friends (snapshot, use subscription for live updates) */
  friends: Array<Friend>;
  /** game records */
  gameRecord?: Maybe<GameRecord>;
  gameRecordIds: Array<Scalars['String']>;
  /** Get a single `GeneratedArt`. */
  generatedArtByHashAndOwner?: Maybe<GeneratedArt>;
  getLatestCard?: Maybe<Card>;
  getUserId?: Maybe<Scalars['String']>;
  /** invites */
  invite: InviteResponse;
  invites: Array<Invite>;
  isInMatch?: Maybe<Scalars['String']>;
  /** matchmaking */
  matchmakingQueues: Array<MatchmakingQueue>;
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
  rerollCost: Scalars['Int'];
  /** Reads a single `RogueChoice` using its globally unique `ID`. */
  rogueChoice?: Maybe<RogueChoice>;
  /** Get a single `RogueChoice`. */
  rogueChoiceById?: Maybe<RogueChoice>;
  /** Reads a single `RogueRun` using its globally unique `ID`. */
  rogueRun?: Maybe<RogueRun>;
  /** Get a single `RogueRun`. */
  rogueRunByDeck?: Maybe<RogueRun>;
  /** Get a single `RogueRun`. */
  rogueRunByGame?: Maybe<RogueRun>;
  /** Get a single `RogueRun`. */
  rogueRunById?: Maybe<RogueRun>;
  trashCardCost: Scalars['Int'];
  upgradeCardCost: Scalars['Int'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryAccountsArgs = {
  userIds: Array<Scalars['String']>;
};


/** ─── Query ──────────────────────────────────────────────────── */
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


/** ─── Query ──────────────────────────────────────────────────── */
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


/** ─── Query ──────────────────────────────────────────────────── */
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


/** ─── Query ──────────────────────────────────────────────────── */
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


/** ─── Query ──────────────────────────────────────────────────── */
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


/** ─── Query ──────────────────────────────────────────────────── */
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


/** ─── Query ──────────────────────────────────────────────────── */
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


/** ─── Query ──────────────────────────────────────────────────── */
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


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryAllRogueChoicesArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<RogueChoiceCondition>;
  filter?: InputMaybe<RogueChoiceFilter>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<RogueChoicesOrderBy>>;
};


/** ─── Query ──────────────────────────────────────────────────── */
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


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryCanSeeDeckArgs = {
  deck?: InputMaybe<DeckInput>;
  userId?: InputMaybe<Scalars['String']>;
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryCardArgs = {
  nodeId: Scalars['ID'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryCardBySuccessionArgs = {
  succession: Scalars['BigInt'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryCardsArgs = {
  ifNoneMatch?: InputMaybe<Scalars['String']>;
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryCardsByUserArgs = {
  ifNoneMatch?: InputMaybe<Scalars['String']>;
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryCardsInDeckArgs = {
  nodeId: Scalars['ID'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryCardsInDeckByIdArgs = {
  id: Scalars['BigInt'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryCurrentRogueChoiceArgs = {
  rogueId?: InputMaybe<Scalars['BigInt']>;
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryDeckArgs = {
  deckId: Scalars['String'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryDeckByIdArgs = {
  id: Scalars['String'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryDeckShareArgs = {
  nodeId: Scalars['ID'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryDeckShareByDeckIdAndShareRecipientIdArgs = {
  deckId: Scalars['String'];
  shareRecipientId: Scalars['String'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryGameRecordArgs = {
  gameId: Scalars['String'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryGeneratedArtByHashAndOwnerArgs = {
  hash: Scalars['String'];
  owner: Scalars['String'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryGetLatestCardArgs = {
  cardId?: InputMaybe<Scalars['String']>;
  published?: InputMaybe<Scalars['Boolean']>;
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryInviteArgs = {
  inviteId: Scalars['String'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryNodeArgs = {
  nodeId: Scalars['ID'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryPublishedCardArgs = {
  nodeId: Scalars['ID'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryPublishedCardByIdArgs = {
  id: Scalars['String'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryRerollCostArgs = {
  rogueId: Scalars['BigInt'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryRogueChoiceArgs = {
  nodeId: Scalars['ID'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryRogueChoiceByIdArgs = {
  id: Scalars['BigInt'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryRogueRunArgs = {
  nodeId: Scalars['ID'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryRogueRunByDeckArgs = {
  deck: Scalars['String'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryRogueRunByGameArgs = {
  game: Scalars['BigInt'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryRogueRunByIdArgs = {
  id: Scalars['BigInt'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryTrashCardCostArgs = {
  cardId: Scalars['String'];
  rogueId: Scalars['BigInt'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryUpgradeCardCostArgs = {
  cardId: Scalars['String'];
  rogueId: Scalars['BigInt'];
};

export const Rarity = {
  Alliance: 'ALLIANCE',
  Common: 'COMMON',
  Epic: 'EPIC',
  Free: 'FREE',
  Legendary: 'LEGENDARY',
  Rare: 'RARE'
} as const;

export type Rarity = typeof Rarity[keyof typeof Rarity];
export type RogueChoice = Node & {
  __typename?: 'RogueChoice';
  canPick: Scalars['Int'];
  canReroll: Scalars['Boolean'];
  cards: Array<Maybe<Scalars['String']>>;
  id: Scalars['BigInt'];
  index: Scalars['Int'];
  /** A globally unique identifier. Can be used in various places throughout the system to identify this single value. */
  nodeId: Scalars['ID'];
  repopulate: Scalars['Boolean'];
  rogueRun: Scalars['BigInt'];
  /** Reads a single `RogueRun` that is related to this `RogueChoice`. */
  rogueRunByRogueRun?: Maybe<RogueRun>;
  type: RogueChoiceType;
};

/**
 * A condition to be used against `RogueChoice` object types. All fields are tested
 * for equality and combined with a logical ‘and.’
 */
export type RogueChoiceCondition = {
  /** Checks for equality with the object’s `canPick` field. */
  canPick?: InputMaybe<Scalars['Int']>;
  /** Checks for equality with the object’s `canReroll` field. */
  canReroll?: InputMaybe<Scalars['Boolean']>;
  /** Checks for equality with the object’s `cards` field. */
  cards?: InputMaybe<Array<InputMaybe<Scalars['String']>>>;
  /** Checks for equality with the object’s `id` field. */
  id?: InputMaybe<Scalars['BigInt']>;
  /** Checks for equality with the object’s `index` field. */
  index?: InputMaybe<Scalars['Int']>;
  /** Checks for equality with the object’s `repopulate` field. */
  repopulate?: InputMaybe<Scalars['Boolean']>;
  /** Checks for equality with the object’s `rogueRun` field. */
  rogueRun?: InputMaybe<Scalars['BigInt']>;
  /** Checks for equality with the object’s `type` field. */
  type?: InputMaybe<RogueChoiceType>;
};

/** A filter to be used against `RogueChoice` object types. All fields are combined with a logical ‘and.’ */
export type RogueChoiceFilter = {
  /** Checks for all expressions in this list. */
  and?: InputMaybe<Array<RogueChoiceFilter>>;
  /** Filter by the object’s `canPick` field. */
  canPick?: InputMaybe<IntFilter>;
  /** Filter by the object’s `canReroll` field. */
  canReroll?: InputMaybe<BooleanFilter>;
  /** Filter by the object’s `cards` field. */
  cards?: InputMaybe<StringListFilter>;
  /** Filter by the object’s `id` field. */
  id?: InputMaybe<BigIntFilter>;
  /** Filter by the object’s `index` field. */
  index?: InputMaybe<IntFilter>;
  /** Negates the expression. */
  not?: InputMaybe<RogueChoiceFilter>;
  /** Checks for any expressions in this list. */
  or?: InputMaybe<Array<RogueChoiceFilter>>;
  /** Filter by the object’s `repopulate` field. */
  repopulate?: InputMaybe<BooleanFilter>;
  /** Filter by the object’s `rogueRun` field. */
  rogueRun?: InputMaybe<BigIntFilter>;
  /** Filter by the object’s `rogueRunByRogueRun` relation. */
  rogueRunByRogueRun?: InputMaybe<RogueRunFilter>;
  /** Filter by the object’s `type` field. */
  type?: InputMaybe<RogueChoiceTypeFilter>;
};

export const RogueChoiceType = {
  Equipment: 'EQUIPMENT',
  Standard: 'STANDARD'
} as const;

export type RogueChoiceType = typeof RogueChoiceType[keyof typeof RogueChoiceType];
/** A filter to be used against RogueChoiceType fields. All fields are combined with a logical ‘and.’ */
export type RogueChoiceTypeFilter = {
  /** Not equal to the specified value, treating null like an ordinary value. */
  distinctFrom?: InputMaybe<RogueChoiceType>;
  /** Equal to the specified value. */
  equalTo?: InputMaybe<RogueChoiceType>;
  /** Greater than the specified value. */
  greaterThan?: InputMaybe<RogueChoiceType>;
  /** Greater than or equal to the specified value. */
  greaterThanOrEqualTo?: InputMaybe<RogueChoiceType>;
  /** Included in the specified list. */
  in?: InputMaybe<Array<RogueChoiceType>>;
  /** Is null (if `true` is specified) or is not null (if `false` is specified). */
  isNull?: InputMaybe<Scalars['Boolean']>;
  /** Less than the specified value. */
  lessThan?: InputMaybe<RogueChoiceType>;
  /** Less than or equal to the specified value. */
  lessThanOrEqualTo?: InputMaybe<RogueChoiceType>;
  /** Equal to the specified value, treating null like an ordinary value. */
  notDistinctFrom?: InputMaybe<RogueChoiceType>;
  /** Not equal to the specified value. */
  notEqualTo?: InputMaybe<RogueChoiceType>;
  /** Not included in the specified list. */
  notIn?: InputMaybe<Array<RogueChoiceType>>;
};

/** A connection to a list of `RogueChoice` values. */
export type RogueChoicesConnection = {
  __typename?: 'RogueChoicesConnection';
  /** A list of edges which contains the `RogueChoice` and cursor to aid in pagination. */
  edges: Array<Maybe<RogueChoicesEdge>>;
  /** A list of `RogueChoice` objects. */
  nodes: Array<Maybe<RogueChoice>>;
  /** Information to aid in pagination. */
  pageInfo: PageInfo;
  /** The count of *all* `RogueChoice` you could get from the connection. */
  totalCount: Scalars['Int'];
};

/** A `RogueChoice` edge in the connection. */
export type RogueChoicesEdge = {
  __typename?: 'RogueChoicesEdge';
  /** A cursor for use in pagination. */
  cursor?: Maybe<Scalars['Cursor']>;
  /** The `RogueChoice` at the end of the edge. */
  node?: Maybe<RogueChoice>;
};

/** Methods to use when ordering `RogueChoice`. */
export const RogueChoicesOrderBy = {
  CanPickAsc: 'CAN_PICK_ASC',
  CanPickDesc: 'CAN_PICK_DESC',
  CanRerollAsc: 'CAN_REROLL_ASC',
  CanRerollDesc: 'CAN_REROLL_DESC',
  IdAsc: 'ID_ASC',
  IdDesc: 'ID_DESC',
  IndexAsc: 'INDEX_ASC',
  IndexDesc: 'INDEX_DESC',
  Natural: 'NATURAL',
  PrimaryKeyAsc: 'PRIMARY_KEY_ASC',
  PrimaryKeyDesc: 'PRIMARY_KEY_DESC',
  RepopulateAsc: 'REPOPULATE_ASC',
  RepopulateDesc: 'REPOPULATE_DESC',
  RogueRunAsc: 'ROGUE_RUN_ASC',
  RogueRunDesc: 'ROGUE_RUN_DESC',
  TypeAsc: 'TYPE_ASC',
  TypeDesc: 'TYPE_DESC'
} as const;

export type RogueChoicesOrderBy = typeof RogueChoicesOrderBy[keyof typeof RogueChoicesOrderBy];
/** ─── Rogue types (existing) ────────────────────────────────── */
export type RogueRun = Node & {
  __typename?: 'RogueRun';
  bossesDefeated: Scalars['Int'];
  currentChoice?: Maybe<RogueChoice>;
  deck: Scalars['String'];
  /** Reads a single `Deck` that is related to this `RogueRun`. */
  deckByDeck?: Maybe<Deck>;
  /** Reads a single `Deck` that is related to this `RogueRun`. */
  deckByOpponentDeck?: Maybe<Deck>;
  endedAt?: Maybe<Scalars['Datetime']>;
  game?: Maybe<Scalars['BigInt']>;
  gold: Scalars['Int'];
  heroClass: Scalars['String'];
  id: Scalars['BigInt'];
  lives: Scalars['Int'];
  /** A globally unique identifier. Can be used in various places throughout the system to identify this single value. */
  nodeId: Scalars['ID'];
  opponentDeck?: Maybe<Scalars['String']>;
  opponentInfo?: Maybe<Scalars['String']>;
  player: Scalars['String'];
  rerollsThisRound: Scalars['Int'];
  /** Reads and enables pagination through a set of `RogueChoice`. */
  rogueChoicesByRogueRun: RogueChoicesConnection;
  seed: Scalars['BigInt'];
  seedState: Scalars['BigInt'];
  startedAt: Scalars['Datetime'];
  state: RogueRunState;
};


/** ─── Rogue types (existing) ────────────────────────────────── */
export type RogueRunRogueChoicesByRogueRunArgs = {
  after?: InputMaybe<Scalars['Cursor']>;
  before?: InputMaybe<Scalars['Cursor']>;
  condition?: InputMaybe<RogueChoiceCondition>;
  filter?: InputMaybe<RogueChoiceFilter>;
  first?: InputMaybe<Scalars['Int']>;
  last?: InputMaybe<Scalars['Int']>;
  offset?: InputMaybe<Scalars['Int']>;
  orderBy?: InputMaybe<Array<RogueChoicesOrderBy>>;
};

/**
 * A condition to be used against `RogueRun` object types. All fields are tested
 * for equality and combined with a logical ‘and.’
 */
export type RogueRunCondition = {
  /** Checks for equality with the object’s `bossesDefeated` field. */
  bossesDefeated?: InputMaybe<Scalars['Int']>;
  /** Checks for equality with the object’s `deck` field. */
  deck?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `endedAt` field. */
  endedAt?: InputMaybe<Scalars['Datetime']>;
  /** Checks for equality with the object’s `game` field. */
  game?: InputMaybe<Scalars['BigInt']>;
  /** Checks for equality with the object’s `gold` field. */
  gold?: InputMaybe<Scalars['Int']>;
  /** Checks for equality with the object’s `heroClass` field. */
  heroClass?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `id` field. */
  id?: InputMaybe<Scalars['BigInt']>;
  /** Checks for equality with the object’s `lives` field. */
  lives?: InputMaybe<Scalars['Int']>;
  /** Checks for equality with the object’s `opponentDeck` field. */
  opponentDeck?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `opponentInfo` field. */
  opponentInfo?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `player` field. */
  player?: InputMaybe<Scalars['String']>;
  /** Checks for equality with the object’s `rerollsThisRound` field. */
  rerollsThisRound?: InputMaybe<Scalars['Int']>;
  /** Checks for equality with the object’s `seed` field. */
  seed?: InputMaybe<Scalars['BigInt']>;
  /** Checks for equality with the object’s `seedState` field. */
  seedState?: InputMaybe<Scalars['BigInt']>;
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
  /** Filter by the object’s `gold` field. */
  gold?: InputMaybe<IntFilter>;
  /** Filter by the object’s `heroClass` field. */
  heroClass?: InputMaybe<StringFilter>;
  /** Filter by the object’s `id` field. */
  id?: InputMaybe<BigIntFilter>;
  /** Filter by the object’s `lives` field. */
  lives?: InputMaybe<IntFilter>;
  /** Negates the expression. */
  not?: InputMaybe<RogueRunFilter>;
  /** Filter by the object’s `opponentDeck` field. */
  opponentDeck?: InputMaybe<StringFilter>;
  /** Filter by the object’s `opponentInfo` field. */
  opponentInfo?: InputMaybe<StringFilter>;
  /** Checks for any expressions in this list. */
  or?: InputMaybe<Array<RogueRunFilter>>;
  /** Filter by the object’s `player` field. */
  player?: InputMaybe<StringFilter>;
  /** Filter by the object’s `rerollsThisRound` field. */
  rerollsThisRound?: InputMaybe<IntFilter>;
  /** Filter by the object’s `rogueChoicesByRogueRun` relation. */
  rogueChoicesByRogueRun?: InputMaybe<RogueRunToManyRogueChoiceFilter>;
  /** Some related `rogueChoicesByRogueRun` exist. */
  rogueChoicesByRogueRunExist?: InputMaybe<Scalars['Boolean']>;
  /** Filter by the object’s `seed` field. */
  seed?: InputMaybe<BigIntFilter>;
  /** Filter by the object’s `seedState` field. */
  seedState?: InputMaybe<BigIntFilter>;
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

/** A filter to be used against many `RogueChoice` object types. All fields are combined with a logical ‘and.’ */
export type RogueRunToManyRogueChoiceFilter = {
  /** Every related `RogueChoice` matches the filter criteria. All fields are combined with a logical ‘and.’ */
  every?: InputMaybe<RogueChoiceFilter>;
  /** No related `RogueChoice` matches the filter criteria. All fields are combined with a logical ‘and.’ */
  none?: InputMaybe<RogueChoiceFilter>;
  /** Some related `RogueChoice` matches the filter criteria. All fields are combined with a logical ‘and.’ */
  some?: InputMaybe<RogueChoiceFilter>;
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
  GoldAsc: 'GOLD_ASC',
  GoldDesc: 'GOLD_DESC',
  HeroClassAsc: 'HERO_CLASS_ASC',
  HeroClassDesc: 'HERO_CLASS_DESC',
  IdAsc: 'ID_ASC',
  IdDesc: 'ID_DESC',
  LivesAsc: 'LIVES_ASC',
  LivesDesc: 'LIVES_DESC',
  Natural: 'NATURAL',
  OpponentDeckAsc: 'OPPONENT_DECK_ASC',
  OpponentDeckDesc: 'OPPONENT_DECK_DESC',
  PlayerAsc: 'PLAYER_ASC',
  PlayerDesc: 'PLAYER_DESC',
  PrimaryKeyAsc: 'PRIMARY_KEY_ASC',
  PrimaryKeyDesc: 'PRIMARY_KEY_DESC',
  RerollsThisRoundAsc: 'REROLLS_THIS_ROUND_ASC',
  RerollsThisRoundDesc: 'REROLLS_THIS_ROUND_DESC',
  SeedAsc: 'SEED_ASC',
  SeedDesc: 'SEED_DESC',
  SeedStateAsc: 'SEED_STATE_ASC',
  SeedStateDesc: 'SEED_STATE_DESC',
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

/**
 * 
 * A message from the server during a game. The messageType field indicates which
 * fields are populated. Clients should switch on messageType to process the message.
 */
export type ServerGameMessage = {
  __typename?: 'ServerGameMessage';
  /** Present on ON_REQUEST_ACTION messages. The actions the player can take. */
  actions?: Maybe<GameActions>;
  /**
   * 
   * IDs of entities that changed in this message. Present on ON_UPDATE and
   * ON_REQUEST_ACTION messages. Clients use this to diff zones efficiently
   * instead of comparing the full entity list.
   */
  changedEntityIds?: Maybe<Array<Scalars['Int']>>;
  /** Present on EMOTE messages. */
  emote?: Maybe<Emote>;
  /** Present on ON_GAME_EVENT messages. */
  event?: Maybe<GameEvent>;
  /** Present on ON_GAME_END messages. */
  gameOver?: Maybe<GameOver>;
  /** Present on ON_UPDATE and ON_REQUEST_ACTION messages. */
  gameState?: Maybe<GameState>;
  id?: Maybe<Scalars['String']>;
  isReplayMessage: Scalars['Boolean'];
  localPlayerId: Scalars['Int'];
  messageType: MessageType;
  /** Present on ON_MULLIGAN messages. The starting cards to consider discarding. */
  startingCards?: Maybe<Array<Entity>>;
  /** Present on TIMER messages. */
  timers?: Maybe<Timers>;
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

export type SpellAction = {
  __typename?: 'SpellAction';
  action: Scalars['Int'];
  actionType: ActionType;
  choices: Array<SpellAction>;
  description: Scalars['String'];
  entity?: Maybe<Entity>;
  sourceId: Scalars['Int'];
  targetKeyToActions: Array<TargetActionPair>;
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

/** ─── Subscription ───────────────────────────────────────────── */
export type Subscription = {
  __typename?: 'Subscription';
  /** Emits when an editable card is added, changed, or removed. */
  editableCardUpdated: EditableCard;
  /** Emits friend list changes (added, removed, presence updates). */
  friendUpdated: Friend;
  /**
   * 
   * Streams game messages for the authenticated user's active game.
   * Call connectToGame mutation first to initiate the game connection.
   * Emits ServerGameMessage for each game state change, action request,
   * mulligan, event, and game over.
   */
  gameMessages: ServerGameMessage;
  /** Emits when an invite is received or its status changes. */
  inviteUpdated: Invite;
  /** Emits when the current user is matched into a game. */
  matchFound: MatchFound;
};

/** ─── Game types (real-time game state) ──────────────────────── */
export type TargetActionPair = {
  __typename?: 'TargetActionPair';
  action: Scalars['Int'];
  friendlyBattlefieldIndex: Scalars['Int'];
  target: Scalars['Int'];
};

export type Timers = {
  __typename?: 'Timers';
  millisRemaining: Scalars['BigInt'];
};

export type Tooltip = {
  __typename?: 'Tooltip';
  keywords: Array<Scalars['String']>;
  text: Scalars['String'];
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

/** ─── Account / Auth types ───────────────────────────────────── */
export type UserEntity = {
  __typename?: 'UserEntity';
  email: Scalars['String'];
  id: Scalars['String'];
  privacyToken: Scalars['String'];
  username: Scalars['String'];
};

export type ValidationReport = {
  __typename?: 'ValidationReport';
  errors: Array<Scalars['String']>;
  valid: Scalars['Boolean'];
};

export const Zone = {
  Battlefield: 'BATTLEFIELD',
  Deck: 'DECK',
  Discover: 'DISCOVER',
  Enchantment: 'ENCHANTMENT',
  Graveyard: 'GRAVEYARD',
  Hand: 'HAND',
  Hero: 'HERO',
  HeroPower: 'HERO_POWER',
  Hidden: 'HIDDEN',
  None: 'NONE',
  Player: 'PLAYER',
  Quest: 'QUEST',
  RemovedFromPlay: 'REMOVED_FROM_PLAY',
  Secret: 'SECRET',
  SetAsideZone: 'SET_ASIDE_ZONE',
  Weapon: 'WEAPON'
} as const;

export type Zone = typeof Zone[keyof typeof Zone];


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
  AcceptInviteInput: ResolverTypeWrapper<Partial<AcceptInviteInput>>;
  AccessToken: ResolverTypeWrapper<Partial<AccessToken>>;
  ActionType: ResolverTypeWrapper<Partial<ActionType>>;
  ArchiveCardInput: ResolverTypeWrapper<Partial<ArchiveCardInput>>;
  ArchiveCardPayload: ResolverTypeWrapper<Partial<ArchiveCardPayload>>;
  AttributeValueInput: ResolverTypeWrapper<Partial<AttributeValueInput>>;
  AttributeValueTuple: ResolverTypeWrapper<Partial<AttributeValueTuple>>;
  BigInt: ResolverTypeWrapper<Partial<Scalars['BigInt']>>;
  BigIntFilter: ResolverTypeWrapper<Partial<BigIntFilter>>;
  Boolean: ResolverTypeWrapper<Partial<Scalars['Boolean']>>;
  BooleanFilter: ResolverTypeWrapper<Partial<BooleanFilter>>;
  Card: ResolverTypeWrapper<Partial<Card>>;
  CardCondition: ResolverTypeWrapper<Partial<CardCondition>>;
  CardFilter: ResolverTypeWrapper<Partial<CardFilter>>;
  CardInput: ResolverTypeWrapper<Partial<CardInput>>;
  CardPatch: ResolverTypeWrapper<Partial<CardPatch>>;
  CardRecord: ResolverTypeWrapper<Partial<CardRecord>>;
  CardToManyPublishedCardFilter: ResolverTypeWrapper<Partial<CardToManyPublishedCardFilter>>;
  CardType: ResolverTypeWrapper<Partial<CardType>>;
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
  ClientConfiguration: ResolverTypeWrapper<Partial<ClientConfiguration>>;
  CollectionCard: ResolverTypeWrapper<Partial<CollectionCard>>;
  CollectionCardCondition: ResolverTypeWrapper<Partial<CollectionCardCondition>>;
  CollectionCardFilter: ResolverTypeWrapper<Partial<CollectionCardFilter>>;
  CollectionCardsConnection: ResolverTypeWrapper<Partial<CollectionCardsConnection>>;
  CollectionCardsEdge: ResolverTypeWrapper<Partial<CollectionCardsEdge>>;
  CollectionCardsOrderBy: ResolverTypeWrapper<Partial<CollectionCardsOrderBy>>;
  CollectionType: ResolverTypeWrapper<Partial<CollectionType>>;
  CreateAccountInput: ResolverTypeWrapper<Partial<CreateAccountInput>>;
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
  DeckType: ResolverTypeWrapper<Partial<DeckType>>;
  DecksConnection: ResolverTypeWrapper<Partial<DecksConnection>>;
  DecksEdge: ResolverTypeWrapper<Partial<DecksEdge>>;
  DecksGetResponse: ResolverTypeWrapper<Partial<DecksGetResponse>>;
  DecksOrderBy: ResolverTypeWrapper<Partial<DecksOrderBy>>;
  DecksPutInput: ResolverTypeWrapper<Partial<DecksPutInput>>;
  DecksPutResponse: ResolverTypeWrapper<Partial<DecksPutResponse>>;
  DecksUpdateInput: ResolverTypeWrapper<Partial<DecksUpdateInput>>;
  DeleteCardsInDeckByIdInput: ResolverTypeWrapper<Partial<DeleteCardsInDeckByIdInput>>;
  DeleteCardsInDeckInput: ResolverTypeWrapper<Partial<DeleteCardsInDeckInput>>;
  DeleteCardsInDeckPayload: ResolverTypeWrapper<Partial<DeleteCardsInDeckPayload>>;
  DeletePublishedCardByIdInput: ResolverTypeWrapper<Partial<DeletePublishedCardByIdInput>>;
  DeletePublishedCardInput: ResolverTypeWrapper<Partial<DeletePublishedCardInput>>;
  DeletePublishedCardPayload: ResolverTypeWrapper<Partial<DeletePublishedCardPayload>>;
  DraftState: ResolverTypeWrapper<Partial<DraftState>>;
  DraftStatus: ResolverTypeWrapper<Partial<DraftStatus>>;
  DraftsPostInput: ResolverTypeWrapper<Partial<DraftsPostInput>>;
  EditableCard: ResolverTypeWrapper<Partial<EditableCard>>;
  Emote: ResolverTypeWrapper<Partial<Emote>>;
  EmoteType: ResolverTypeWrapper<Partial<EmoteType>>;
  EndRogueRunInput: ResolverTypeWrapper<Partial<EndRogueRunInput>>;
  EndRogueRunPayload: ResolverTypeWrapper<Partial<EndRogueRunPayload>>;
  Entity: ResolverTypeWrapper<Partial<Entity>>;
  EntityLocation: ResolverTypeWrapper<Partial<EntityLocation>>;
  EntityType: ResolverTypeWrapper<Partial<EntityType>>;
  Friend: ResolverTypeWrapper<Partial<Friend>>;
  GameActions: ResolverTypeWrapper<Partial<GameActions>>;
  GameEvent: ResolverTypeWrapper<Partial<GameEvent>>;
  GameEventType: ResolverTypeWrapper<Partial<GameEventType>>;
  GameOver: ResolverTypeWrapper<Partial<GameOver>>;
  GameRecord: ResolverTypeWrapper<Partial<GameRecord>>;
  GameState: ResolverTypeWrapper<Partial<GameState>>;
  GeneratedArt: ResolverTypeWrapper<Partial<GeneratedArt>>;
  GeneratedArtCondition: ResolverTypeWrapper<Partial<GeneratedArtCondition>>;
  GeneratedArtFilter: ResolverTypeWrapper<Partial<GeneratedArtFilter>>;
  GeneratedArtInput: ResolverTypeWrapper<Partial<GeneratedArtInput>>;
  GeneratedArtPatch: ResolverTypeWrapper<Partial<GeneratedArtPatch>>;
  GeneratedArtsConnection: ResolverTypeWrapper<Partial<GeneratedArtsConnection>>;
  GeneratedArtsEdge: ResolverTypeWrapper<Partial<GeneratedArtsEdge>>;
  GeneratedArtsOrderBy: ResolverTypeWrapper<Partial<GeneratedArtsOrderBy>>;
  GetCardsResponse: ResolverTypeWrapper<Partial<GetCardsResponse>>;
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
  InventoryCollection: ResolverTypeWrapper<Partial<InventoryCollection>>;
  Invite: ResolverTypeWrapper<Partial<Invite>>;
  InvitePostInput: ResolverTypeWrapper<Partial<InvitePostInput>>;
  InviteResponse: ResolverTypeWrapper<Partial<InviteResponse>>;
  InviteStatus: ResolverTypeWrapper<Partial<InviteStatus>>;
  JSON: ResolverTypeWrapper<Partial<Scalars['JSON']>>;
  JSONFilter: ResolverTypeWrapper<Partial<JsonFilter>>;
  LoginInput: ResolverTypeWrapper<Partial<LoginInput>>;
  LoginOrCreateReply: ResolverTypeWrapper<Partial<LoginOrCreateReply>>;
  MatchFound: ResolverTypeWrapper<Partial<MatchFound>>;
  MatchmakingEnqueueInput: ResolverTypeWrapper<Partial<MatchmakingEnqueueInput>>;
  MatchmakingQueue: ResolverTypeWrapper<Partial<MatchmakingQueue>>;
  MatchmakingQueueRequires: ResolverTypeWrapper<Partial<MatchmakingQueueRequires>>;
  MessageType: ResolverTypeWrapper<Partial<MessageType>>;
  Mutation: ResolverTypeWrapper<{}>;
  Node: ResolversTypes['Card'] | ResolversTypes['CardsInDeck'] | ResolversTypes['Deck'] | ResolversTypes['DeckShare'] | ResolversTypes['PublishedCard'] | ResolversTypes['Query'] | ResolversTypes['RogueChoice'] | ResolversTypes['RogueRun'];
  PageInfo: ResolverTypeWrapper<Partial<PageInfo>>;
  PlayerEntityAttribute: ResolverTypeWrapper<Partial<PlayerEntityAttribute>>;
  Presence: ResolverTypeWrapper<Partial<Presence>>;
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
  PutCardInput: ResolverTypeWrapper<Partial<PutCardInput>>;
  PutCardResult: ResolverTypeWrapper<Partial<PutCardResult>>;
  Query: ResolverTypeWrapper<{}>;
  Rarity: ResolverTypeWrapper<Partial<Rarity>>;
  RogueChoice: ResolverTypeWrapper<Partial<RogueChoice>>;
  RogueChoiceCondition: ResolverTypeWrapper<Partial<RogueChoiceCondition>>;
  RogueChoiceFilter: ResolverTypeWrapper<Partial<RogueChoiceFilter>>;
  RogueChoiceType: ResolverTypeWrapper<Partial<RogueChoiceType>>;
  RogueChoiceTypeFilter: ResolverTypeWrapper<Partial<RogueChoiceTypeFilter>>;
  RogueChoicesConnection: ResolverTypeWrapper<Partial<RogueChoicesConnection>>;
  RogueChoicesEdge: ResolverTypeWrapper<Partial<RogueChoicesEdge>>;
  RogueChoicesOrderBy: ResolverTypeWrapper<Partial<RogueChoicesOrderBy>>;
  RogueRun: ResolverTypeWrapper<Partial<RogueRun>>;
  RogueRunCondition: ResolverTypeWrapper<Partial<RogueRunCondition>>;
  RogueRunFilter: ResolverTypeWrapper<Partial<RogueRunFilter>>;
  RogueRunState: ResolverTypeWrapper<Partial<RogueRunState>>;
  RogueRunStateFilter: ResolverTypeWrapper<Partial<RogueRunStateFilter>>;
  RogueRunToManyRogueChoiceFilter: ResolverTypeWrapper<Partial<RogueRunToManyRogueChoiceFilter>>;
  RogueRunsConnection: ResolverTypeWrapper<Partial<RogueRunsConnection>>;
  RogueRunsEdge: ResolverTypeWrapper<Partial<RogueRunsEdge>>;
  RogueRunsOrderBy: ResolverTypeWrapper<Partial<RogueRunsOrderBy>>;
  SaveCardInput: ResolverTypeWrapper<Partial<SaveCardInput>>;
  SaveCardPayload: ResolverTypeWrapper<Partial<SaveCardPayload>>;
  SaveGeneratedArtInput: ResolverTypeWrapper<Partial<SaveGeneratedArtInput>>;
  SaveGeneratedArtPayload: ResolverTypeWrapper<Partial<SaveGeneratedArtPayload>>;
  ServerGameMessage: ResolverTypeWrapper<Partial<ServerGameMessage>>;
  SetCardsInDeckInput: ResolverTypeWrapper<Partial<SetCardsInDeckInput>>;
  SetCardsInDeckPayload: ResolverTypeWrapper<Partial<SetCardsInDeckPayload>>;
  SpellAction: ResolverTypeWrapper<Partial<SpellAction>>;
  String: ResolverTypeWrapper<Partial<Scalars['String']>>;
  StringFilter: ResolverTypeWrapper<Partial<StringFilter>>;
  StringListFilter: ResolverTypeWrapper<Partial<StringListFilter>>;
  Subscription: ResolverTypeWrapper<{}>;
  TargetActionPair: ResolverTypeWrapper<Partial<TargetActionPair>>;
  Timers: ResolverTypeWrapper<Partial<Timers>>;
  Tooltip: ResolverTypeWrapper<Partial<Tooltip>>;
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
  UserEntity: ResolverTypeWrapper<Partial<UserEntity>>;
  ValidationReport: ResolverTypeWrapper<Partial<ValidationReport>>;
  Zone: ResolverTypeWrapper<Partial<Zone>>;
};

/** Mapping between all available schema types and the resolvers parents */
export type ResolversParentTypes = {
  AcceptInviteInput: Partial<AcceptInviteInput>;
  AccessToken: Partial<AccessToken>;
  ArchiveCardInput: Partial<ArchiveCardInput>;
  ArchiveCardPayload: Partial<ArchiveCardPayload>;
  AttributeValueInput: Partial<AttributeValueInput>;
  AttributeValueTuple: Partial<AttributeValueTuple>;
  BigInt: Partial<Scalars['BigInt']>;
  BigIntFilter: Partial<BigIntFilter>;
  Boolean: Partial<Scalars['Boolean']>;
  BooleanFilter: Partial<BooleanFilter>;
  Card: Partial<Card>;
  CardCondition: Partial<CardCondition>;
  CardFilter: Partial<CardFilter>;
  CardInput: Partial<CardInput>;
  CardPatch: Partial<CardPatch>;
  CardRecord: Partial<CardRecord>;
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
  ClientConfiguration: Partial<ClientConfiguration>;
  CollectionCard: Partial<CollectionCard>;
  CollectionCardCondition: Partial<CollectionCardCondition>;
  CollectionCardFilter: Partial<CollectionCardFilter>;
  CollectionCardsConnection: Partial<CollectionCardsConnection>;
  CollectionCardsEdge: Partial<CollectionCardsEdge>;
  CreateAccountInput: Partial<CreateAccountInput>;
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
  DecksGetResponse: Partial<DecksGetResponse>;
  DecksPutInput: Partial<DecksPutInput>;
  DecksPutResponse: Partial<DecksPutResponse>;
  DecksUpdateInput: Partial<DecksUpdateInput>;
  DeleteCardsInDeckByIdInput: Partial<DeleteCardsInDeckByIdInput>;
  DeleteCardsInDeckInput: Partial<DeleteCardsInDeckInput>;
  DeleteCardsInDeckPayload: Partial<DeleteCardsInDeckPayload>;
  DeletePublishedCardByIdInput: Partial<DeletePublishedCardByIdInput>;
  DeletePublishedCardInput: Partial<DeletePublishedCardInput>;
  DeletePublishedCardPayload: Partial<DeletePublishedCardPayload>;
  DraftState: Partial<DraftState>;
  DraftsPostInput: Partial<DraftsPostInput>;
  EditableCard: Partial<EditableCard>;
  Emote: Partial<Emote>;
  EndRogueRunInput: Partial<EndRogueRunInput>;
  EndRogueRunPayload: Partial<EndRogueRunPayload>;
  Entity: Partial<Entity>;
  EntityLocation: Partial<EntityLocation>;
  Friend: Partial<Friend>;
  GameActions: Partial<GameActions>;
  GameEvent: Partial<GameEvent>;
  GameOver: Partial<GameOver>;
  GameRecord: Partial<GameRecord>;
  GameState: Partial<GameState>;
  GeneratedArt: Partial<GeneratedArt>;
  GeneratedArtCondition: Partial<GeneratedArtCondition>;
  GeneratedArtFilter: Partial<GeneratedArtFilter>;
  GeneratedArtInput: Partial<GeneratedArtInput>;
  GeneratedArtPatch: Partial<GeneratedArtPatch>;
  GeneratedArtsConnection: Partial<GeneratedArtsConnection>;
  GeneratedArtsEdge: Partial<GeneratedArtsEdge>;
  GetCardsResponse: Partial<GetCardsResponse>;
  GetClassesInput: Partial<GetClassesInput>;
  GetClassesPayload: Partial<GetClassesPayload>;
  GetClassesRecord: Partial<GetClassesRecord>;
  GetCollectionCardsInput: Partial<GetCollectionCardsInput>;
  GetCollectionCardsPayload: Partial<GetCollectionCardsPayload>;
  GetCollectionCardsRecord: Partial<GetCollectionCardsRecord>;
  ID: Partial<Scalars['ID']>;
  Int: Partial<Scalars['Int']>;
  IntFilter: Partial<IntFilter>;
  InventoryCollection: Partial<InventoryCollection>;
  Invite: Partial<Invite>;
  InvitePostInput: Partial<InvitePostInput>;
  InviteResponse: Partial<InviteResponse>;
  JSON: Partial<Scalars['JSON']>;
  JSONFilter: Partial<JsonFilter>;
  LoginInput: Partial<LoginInput>;
  LoginOrCreateReply: Partial<LoginOrCreateReply>;
  MatchFound: Partial<MatchFound>;
  MatchmakingEnqueueInput: Partial<MatchmakingEnqueueInput>;
  MatchmakingQueue: Partial<MatchmakingQueue>;
  MatchmakingQueueRequires: Partial<MatchmakingQueueRequires>;
  Mutation: {};
  Node: ResolversParentTypes['Card'] | ResolversParentTypes['CardsInDeck'] | ResolversParentTypes['Deck'] | ResolversParentTypes['DeckShare'] | ResolversParentTypes['PublishedCard'] | ResolversParentTypes['Query'] | ResolversParentTypes['RogueChoice'] | ResolversParentTypes['RogueRun'];
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
  PutCardInput: Partial<PutCardInput>;
  PutCardResult: Partial<PutCardResult>;
  Query: {};
  RogueChoice: Partial<RogueChoice>;
  RogueChoiceCondition: Partial<RogueChoiceCondition>;
  RogueChoiceFilter: Partial<RogueChoiceFilter>;
  RogueChoiceTypeFilter: Partial<RogueChoiceTypeFilter>;
  RogueChoicesConnection: Partial<RogueChoicesConnection>;
  RogueChoicesEdge: Partial<RogueChoicesEdge>;
  RogueRun: Partial<RogueRun>;
  RogueRunCondition: Partial<RogueRunCondition>;
  RogueRunFilter: Partial<RogueRunFilter>;
  RogueRunStateFilter: Partial<RogueRunStateFilter>;
  RogueRunToManyRogueChoiceFilter: Partial<RogueRunToManyRogueChoiceFilter>;
  RogueRunsConnection: Partial<RogueRunsConnection>;
  RogueRunsEdge: Partial<RogueRunsEdge>;
  SaveCardInput: Partial<SaveCardInput>;
  SaveCardPayload: Partial<SaveCardPayload>;
  SaveGeneratedArtInput: Partial<SaveGeneratedArtInput>;
  SaveGeneratedArtPayload: Partial<SaveGeneratedArtPayload>;
  ServerGameMessage: Partial<ServerGameMessage>;
  SetCardsInDeckInput: Partial<SetCardsInDeckInput>;
  SetCardsInDeckPayload: Partial<SetCardsInDeckPayload>;
  SpellAction: Partial<SpellAction>;
  String: Partial<Scalars['String']>;
  StringFilter: Partial<StringFilter>;
  StringListFilter: Partial<StringListFilter>;
  Subscription: {};
  TargetActionPair: Partial<TargetActionPair>;
  Timers: Partial<Timers>;
  Tooltip: Partial<Tooltip>;
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
  UserEntity: Partial<UserEntity>;
  ValidationReport: Partial<ValidationReport>;
};

export type AccessTokenResolvers<ContextType = any, ParentType extends ResolversParentTypes['AccessToken'] = ResolversParentTypes['AccessToken']> = {
  token?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type ArchiveCardPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['ArchiveCardPayload'] = ResolversParentTypes['ArchiveCardPayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type AttributeValueTupleResolvers<ContextType = any, ParentType extends ResolversParentTypes['AttributeValueTuple'] = ResolversParentTypes['AttributeValueTuple']> = {
  attribute?: Resolver<ResolversTypes['PlayerEntityAttribute'], ParentType, ContextType>;
  stringValue?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
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

export type CardRecordResolvers<ContextType = any, ParentType extends ResolversParentTypes['CardRecord'] = ResolversParentTypes['CardRecord']> = {
  cardId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  collectionIds?: Resolver<Array<ResolversTypes['String']>, ParentType, ContextType>;
  count?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  entity?: Resolver<ResolversTypes['Entity'], ParentType, ContextType>;
  id?: Resolver<ResolversTypes['BigInt'], ParentType, ContextType>;
  userId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
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

export type ClientConfigurationResolvers<ContextType = any, ParentType extends ResolversParentTypes['ClientConfiguration'] = ResolversParentTypes['ClientConfiguration']> = {
  graphQlUrl?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  keycloakAccountManagementUrl?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  keycloakResetPasswordUrl?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
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

export type DecksGetResponseResolvers<ContextType = any, ParentType extends ResolversParentTypes['DecksGetResponse'] = ResolversParentTypes['DecksGetResponse']> = {
  collection?: Resolver<Maybe<ResolversTypes['InventoryCollection']>, ParentType, ContextType>;
  inventoryIdsSize?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type DecksPutResponseResolvers<ContextType = any, ParentType extends ResolversParentTypes['DecksPutResponse'] = ResolversParentTypes['DecksPutResponse']> = {
  collection?: Resolver<Maybe<ResolversTypes['InventoryCollection']>, ParentType, ContextType>;
  deckId?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
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

export type DraftStateResolvers<ContextType = any, ParentType extends ResolversParentTypes['DraftState'] = ResolversParentTypes['DraftState']> = {
  cardsRemaining?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  currentCardChoices?: Resolver<Array<ResolversTypes['Entity']>, ParentType, ContextType>;
  deckId?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  draftIndex?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  heroClass?: Resolver<Maybe<ResolversTypes['Entity']>, ParentType, ContextType>;
  heroClassChoices?: Resolver<Array<ResolversTypes['Entity']>, ParentType, ContextType>;
  losses?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  selectedCardIds?: Resolver<Array<ResolversTypes['String']>, ParentType, ContextType>;
  status?: Resolver<ResolversTypes['DraftStatus'], ParentType, ContextType>;
  wins?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type EditableCardResolvers<ContextType = any, ParentType extends ResolversParentTypes['EditableCard'] = ResolversParentTypes['EditableCard']> = {
  id?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  ownerUserId?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  source?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type EmoteResolvers<ContextType = any, ParentType extends ResolversParentTypes['Emote'] = ResolversParentTypes['Emote']> = {
  entityId?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  message?: Resolver<ResolversTypes['EmoteType'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type EndRogueRunPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['EndRogueRunPayload'] = ResolversParentTypes['EndRogueRunPayload']> = {
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  deckByDeck?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  deckByOpponentDeck?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  rogueRun?: Resolver<Maybe<ResolversTypes['RogueRun']>, ParentType, ContextType>;
  rogueRunEdge?: Resolver<Maybe<ResolversTypes['RogueRunsEdge']>, ParentType, ContextType, RequireFields<EndRogueRunPayloadRogueRunEdgeArgs, 'orderBy'>>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type EntityResolvers<ContextType = any, ParentType extends ResolversParentTypes['Entity'] = ResolversParentTypes['Entity']> = {
  armor?: Resolver<Maybe<ResolversTypes['Int']>, ParentType, ContextType>;
  attack?: Resolver<Maybe<ResolversTypes['Int']>, ParentType, ContextType>;
  baseAttack?: Resolver<Maybe<ResolversTypes['Int']>, ParentType, ContextType>;
  baseHp?: Resolver<Maybe<ResolversTypes['Int']>, ParentType, ContextType>;
  baseManaCost?: Resolver<Maybe<ResolversTypes['Int']>, ParentType, ContextType>;
  battlecry?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  boardPosition?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  cannotAttack?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  cardId?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  cardSet?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  cardSets?: Resolver<Array<ResolversTypes['String']>, ParentType, ContextType>;
  cardType?: Resolver<ResolversTypes['CardType'], ParentType, ContextType>;
  charge?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  charges?: Resolver<Maybe<ResolversTypes['Int']>, ParentType, ContextType>;
  chooseOne?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  collectible?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  combo?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  conditionMet?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  countUntilCast?: Resolver<Maybe<ResolversTypes['Int']>, ParentType, ContextType>;
  deathrattles?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  deflect?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  description?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  destroyed?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  discarded?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  divineShield?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  durability?: Resolver<Maybe<ResolversTypes['Int']>, ParentType, ContextType>;
  enchantmentType?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  enraged?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  entityType?: Resolver<ResolversTypes['EntityType'], ParentType, ContextType>;
  extraAttack?: Resolver<Maybe<ResolversTypes['Int']>, ParentType, ContextType>;
  fires?: Resolver<Maybe<ResolversTypes['Int']>, ParentType, ContextType>;
  frozen?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  gameStarted?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  gold?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  heroClasses?: Resolver<Array<ResolversTypes['String']>, ParentType, ContextType>;
  host?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  hostsTrigger?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  hp?: Resolver<Maybe<ResolversTypes['Int']>, ParentType, ContextType>;
  id?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  immune?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  isStartingTurn?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  lifesteal?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  location?: Resolver<Maybe<ResolversTypes['EntityLocation']>, ParentType, ContextType>;
  lockedMana?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  mana?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  manaCost?: Resolver<Maybe<ResolversTypes['Int']>, ParentType, ContextType>;
  maxHp?: Resolver<Maybe<ResolversTypes['Int']>, ParentType, ContextType>;
  maxMana?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  name?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  note?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  overload?: Resolver<Maybe<ResolversTypes['Int']>, ParentType, ContextType>;
  owner?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  permanent?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  playable?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  poisonous?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  rarity?: Resolver<ResolversTypes['Rarity'], ParentType, ContextType>;
  roasted?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  rush?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  silenced?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  spellDamage?: Resolver<Maybe<ResolversTypes['Int']>, ParentType, ContextType>;
  stealth?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  summoningSickness?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  taunt?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  tooltips?: Resolver<Array<ResolversTypes['Tooltip']>, ParentType, ContextType>;
  tribes?: Resolver<Array<ResolversTypes['String']>, ParentType, ContextType>;
  uncensored?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  underAura?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  untargetableBySpells?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  windfury?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type EntityLocationResolvers<ContextType = any, ParentType extends ResolversParentTypes['EntityLocation'] = ResolversParentTypes['EntityLocation']> = {
  index?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  player?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  zone?: Resolver<ResolversTypes['Zone'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type FriendResolvers<ContextType = any, ParentType extends ResolversParentTypes['Friend'] = ResolversParentTypes['Friend']> = {
  friendId?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  friendName?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  presence?: Resolver<ResolversTypes['Presence'], ParentType, ContextType>;
  since?: Resolver<ResolversTypes['BigInt'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type GameActionsResolvers<ContextType = any, ParentType extends ResolversParentTypes['GameActions'] = ResolversParentTypes['GameActions']> = {
  all?: Resolver<Array<ResolversTypes['SpellAction']>, ParentType, ContextType>;
  compatibility?: Resolver<Array<ResolversTypes['Int']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type GameEventResolvers<ContextType = any, ParentType extends ResolversParentTypes['GameEvent'] = ResolversParentTypes['GameEvent']> = {
  description?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  eventType?: Resolver<ResolversTypes['GameEventType'], ParentType, ContextType>;
  id?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  isPowerHistory?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  isSourcePlayerLocal?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  isTargetPlayerLocal?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  source?: Resolver<Maybe<ResolversTypes['Entity']>, ParentType, ContextType>;
  target?: Resolver<Maybe<ResolversTypes['Entity']>, ParentType, ContextType>;
  targets?: Resolver<Array<ResolversTypes['Entity']>, ParentType, ContextType>;
  value?: Resolver<Maybe<ResolversTypes['Int']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type GameOverResolvers<ContextType = any, ParentType extends ResolversParentTypes['GameOver'] = ResolversParentTypes['GameOver']> = {
  localPlayerWon?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  winningPlayerId?: Resolver<Maybe<ResolversTypes['Int']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type GameRecordResolvers<ContextType = any, ParentType extends ResolversParentTypes['GameRecord'] = ResolversParentTypes['GameRecord']> = {
  completedAt?: Resolver<ResolversTypes['BigInt'], ParentType, ContextType>;
  completedAtLocalized?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  isBotGame?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  playerNames?: Resolver<Array<ResolversTypes['String']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type GameStateResolvers<ContextType = any, ParentType extends ResolversParentTypes['GameState'] = ResolversParentTypes['GameState']> = {
  entities?: Resolver<Array<ResolversTypes['Entity']>, ParentType, ContextType>;
  isLocalPlayerTurn?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  timestamp?: Resolver<ResolversTypes['BigInt'], ParentType, ContextType>;
  turnNumber?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  turnState?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
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

export type GetCardsResponseResolvers<ContextType = any, ParentType extends ResolversParentTypes['GetCardsResponse'] = ResolversParentTypes['GetCardsResponse']> = {
  cachedOk?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  cards?: Resolver<Array<ResolversTypes['CardRecord']>, ParentType, ContextType>;
  version?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
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

export type InventoryCollectionResolvers<ContextType = any, ParentType extends ResolversParentTypes['InventoryCollection'] = ResolversParentTypes['InventoryCollection']> = {
  collectionType?: Resolver<ResolversTypes['CollectionType'], ParentType, ContextType>;
  deckType?: Resolver<ResolversTypes['DeckType'], ParentType, ContextType>;
  format?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  heroClass?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  id?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  inventory?: Resolver<Array<ResolversTypes['CardRecord']>, ParentType, ContextType>;
  isStandardDeck?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  name?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  playerEntityAttributes?: Resolver<Array<ResolversTypes['AttributeValueTuple']>, ParentType, ContextType>;
  userId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  validationReport?: Resolver<Maybe<ResolversTypes['ValidationReport']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type InviteResolvers<ContextType = any, ParentType extends ResolversParentTypes['Invite'] = ResolversParentTypes['Invite']> = {
  expiresAt?: Resolver<Maybe<ResolversTypes['BigInt']>, ParentType, ContextType>;
  friendId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  fromName?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  fromUserId?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  id?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  message?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  queueId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  status?: Resolver<ResolversTypes['InviteStatus'], ParentType, ContextType>;
  toName?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  toUserId?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type InviteResponseResolvers<ContextType = any, ParentType extends ResolversParentTypes['InviteResponse'] = ResolversParentTypes['InviteResponse']> = {
  invite?: Resolver<Maybe<ResolversTypes['Invite']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export interface JsonScalarConfig extends GraphQLScalarTypeConfig<ResolversTypes['JSON'], any> {
  name: 'JSON';
}

export type LoginOrCreateReplyResolvers<ContextType = any, ParentType extends ResolversParentTypes['LoginOrCreateReply'] = ResolversParentTypes['LoginOrCreateReply']> = {
  accessToken?: Resolver<Maybe<ResolversTypes['AccessToken']>, ParentType, ContextType>;
  userEntity?: Resolver<Maybe<ResolversTypes['UserEntity']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type MatchFoundResolvers<ContextType = any, ParentType extends ResolversParentTypes['MatchFound'] = ResolversParentTypes['MatchFound']> = {
  gameId?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  playerKey?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  playerSecret?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  url?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type MatchmakingQueueResolvers<ContextType = any, ParentType extends ResolversParentTypes['MatchmakingQueue'] = ResolversParentTypes['MatchmakingQueue']> = {
  description?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  name?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  queueId?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  requires?: Resolver<Maybe<ResolversTypes['MatchmakingQueueRequires']>, ParentType, ContextType>;
  tooltip?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type MatchmakingQueueRequiresResolvers<ContextType = any, ParentType extends ResolversParentTypes['MatchmakingQueueRequires'] = ResolversParentTypes['MatchmakingQueueRequires']> = {
  deck?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  deckIdChoices?: Resolver<Array<ResolversTypes['String']>, ParentType, ContextType>;
  heroClass?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type MutationResolvers<ContextType = any, ParentType extends ResolversParentTypes['Mutation'] = ResolversParentTypes['Mutation']> = {
  acceptInvite?: Resolver<ResolversTypes['InviteResponse'], ParentType, ContextType, RequireFields<MutationAcceptInviteArgs, 'input'>>;
  addFriend?: Resolver<ResolversTypes['Friend'], ParentType, ContextType, Partial<MutationAddFriendArgs>>;
  archiveCard?: Resolver<Maybe<ResolversTypes['ArchiveCardPayload']>, ParentType, ContextType, RequireFields<MutationArchiveCardArgs, 'input'>>;
  cancelMatchmaking?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  changePassword?: Resolver<ResolversTypes['LoginOrCreateReply'], ParentType, ContextType, RequireFields<MutationChangePasswordArgs, 'newPassword'>>;
  concedeGame?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  connectToGame?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType, RequireFields<MutationConnectToGameArgs, 'playerKey' | 'playerSecret'>>;
  createAccount?: Resolver<ResolversTypes['LoginOrCreateReply'], ParentType, ContextType, RequireFields<MutationCreateAccountArgs, 'input'>>;
  createCard?: Resolver<Maybe<ResolversTypes['CreateCardPayload']>, ParentType, ContextType, RequireFields<MutationCreateCardArgs, 'input'>>;
  createCardsInDeck?: Resolver<Maybe<ResolversTypes['CreateCardsInDeckPayload']>, ParentType, ContextType, RequireFields<MutationCreateCardsInDeckArgs, 'input'>>;
  createDeck?: Resolver<ResolversTypes['DecksPutResponse'], ParentType, ContextType, RequireFields<MutationCreateDeckArgs, 'input'>>;
  createDeckWithCards?: Resolver<Maybe<ResolversTypes['CreateDeckWithCardsPayload']>, ParentType, ContextType, RequireFields<MutationCreateDeckWithCardsArgs, 'input'>>;
  createGeneratedArt?: Resolver<Maybe<ResolversTypes['CreateGeneratedArtPayload']>, ParentType, ContextType, RequireFields<MutationCreateGeneratedArtArgs, 'input'>>;
  createPublishedCard?: Resolver<Maybe<ResolversTypes['CreatePublishedCardPayload']>, ParentType, ContextType, RequireFields<MutationCreatePublishedCardArgs, 'input'>>;
  deleteCard?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType, RequireFields<MutationDeleteCardArgs, 'editableCardId'>>;
  deleteCardsInDeck?: Resolver<Maybe<ResolversTypes['DeleteCardsInDeckPayload']>, ParentType, ContextType, RequireFields<MutationDeleteCardsInDeckArgs, 'input'>>;
  deleteCardsInDeckById?: Resolver<Maybe<ResolversTypes['DeleteCardsInDeckPayload']>, ParentType, ContextType, RequireFields<MutationDeleteCardsInDeckByIdArgs, 'input'>>;
  deleteDeck?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType, RequireFields<MutationDeleteDeckArgs, 'deckId'>>;
  deleteInvite?: Resolver<ResolversTypes['InviteResponse'], ParentType, ContextType, RequireFields<MutationDeleteInviteArgs, 'inviteId'>>;
  deletePublishedCard?: Resolver<Maybe<ResolversTypes['DeletePublishedCardPayload']>, ParentType, ContextType, RequireFields<MutationDeletePublishedCardArgs, 'input'>>;
  deletePublishedCardById?: Resolver<Maybe<ResolversTypes['DeletePublishedCardPayload']>, ParentType, ContextType, RequireFields<MutationDeletePublishedCardByIdArgs, 'input'>>;
  draftsChooseCard?: Resolver<ResolversTypes['DraftState'], ParentType, ContextType, RequireFields<MutationDraftsChooseCardArgs, 'cardIndex'>>;
  draftsChooseHero?: Resolver<ResolversTypes['DraftState'], ParentType, ContextType, RequireFields<MutationDraftsChooseHeroArgs, 'heroIndex'>>;
  duplicateDeck?: Resolver<ResolversTypes['DecksGetResponse'], ParentType, ContextType, RequireFields<MutationDuplicateDeckArgs, 'deckId'>>;
  endRogueRun?: Resolver<Maybe<ResolversTypes['EndRogueRunPayload']>, ParentType, ContextType, RequireFields<MutationEndRogueRunArgs, 'input'>>;
  enqueueMatchmaking?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType, RequireFields<MutationEnqueueMatchmakingArgs, 'input'>>;
  getClasses?: Resolver<Maybe<ResolversTypes['GetClassesPayload']>, ParentType, ContextType, RequireFields<MutationGetClassesArgs, 'input'>>;
  getCollectionCards?: Resolver<Maybe<ResolversTypes['GetCollectionCardsPayload']>, ParentType, ContextType, RequireFields<MutationGetCollectionCardsArgs, 'input'>>;
  login?: Resolver<ResolversTypes['LoginOrCreateReply'], ParentType, ContextType, RequireFields<MutationLoginArgs, 'input'>>;
  makeRogueChoice?: Resolver<ResolversTypes['RogueRun'], ParentType, ContextType, RequireFields<MutationMakeRogueChoiceArgs, 'choiceId' | 'choices'>>;
  publishCard?: Resolver<Maybe<ResolversTypes['PublishCardPayload']>, ParentType, ContextType, RequireFields<MutationPublishCardArgs, 'input'>>;
  putCard?: Resolver<ResolversTypes['PutCardResult'], ParentType, ContextType, RequireFields<MutationPutCardArgs, 'input'>>;
  removeFriend?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType, RequireFields<MutationRemoveFriendArgs, 'friendId'>>;
  requestPasswordResetEmail?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  reroll?: Resolver<ResolversTypes['RogueRun'], ParentType, ContextType, RequireFields<MutationRerollArgs, 'choiceId'>>;
  saveCard?: Resolver<Maybe<ResolversTypes['SaveCardPayload']>, ParentType, ContextType, RequireFields<MutationSaveCardArgs, 'input'>>;
  saveGeneratedArt?: Resolver<Maybe<ResolversTypes['SaveGeneratedArtPayload']>, ParentType, ContextType, RequireFields<MutationSaveGeneratedArtArgs, 'input'>>;
  sendEmote?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType, RequireFields<MutationSendEmoteArgs, 'entityId' | 'message'>>;
  sendGameAction?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType, RequireFields<MutationSendGameActionArgs, 'actionIndex' | 'repliesTo'>>;
  sendInvite?: Resolver<ResolversTypes['InviteResponse'], ParentType, ContextType, RequireFields<MutationSendInviteArgs, 'input'>>;
  sendMulligan?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType, RequireFields<MutationSendMulliganArgs, 'discardedCardIndices' | 'repliesTo'>>;
  setCardsInDeck?: Resolver<Maybe<ResolversTypes['SetCardsInDeckPayload']>, ParentType, ContextType, RequireFields<MutationSetCardsInDeckArgs, 'input'>>;
  skipBoss?: Resolver<ResolversTypes['RogueRun'], ParentType, ContextType, RequireFields<MutationSkipBossArgs, 'rogueId'>>;
  startOrModifyDraft?: Resolver<ResolversTypes['DraftState'], ParentType, ContextType, RequireFields<MutationStartOrModifyDraftArgs, 'input'>>;
  startRogueRun?: Resolver<ResolversTypes['RogueRun'], ParentType, ContextType, RequireFields<MutationStartRogueRunArgs, 'heroClass'>>;
  trashCard?: Resolver<ResolversTypes['RogueRun'], ParentType, ContextType, RequireFields<MutationTrashCardArgs, 'cardId' | 'rogueId'>>;
  updateCard?: Resolver<Maybe<ResolversTypes['UpdateCardPayload']>, ParentType, ContextType, RequireFields<MutationUpdateCardArgs, 'input'>>;
  updateCardBySuccession?: Resolver<Maybe<ResolversTypes['UpdateCardPayload']>, ParentType, ContextType, RequireFields<MutationUpdateCardBySuccessionArgs, 'input'>>;
  updateCardsInDeck?: Resolver<Maybe<ResolversTypes['UpdateCardsInDeckPayload']>, ParentType, ContextType, RequireFields<MutationUpdateCardsInDeckArgs, 'input'>>;
  updateCardsInDeckById?: Resolver<Maybe<ResolversTypes['UpdateCardsInDeckPayload']>, ParentType, ContextType, RequireFields<MutationUpdateCardsInDeckByIdArgs, 'input'>>;
  updateDeck?: Resolver<ResolversTypes['DecksGetResponse'], ParentType, ContextType, RequireFields<MutationUpdateDeckArgs, 'input'>>;
  updateDeckById?: Resolver<Maybe<ResolversTypes['UpdateDeckPayload']>, ParentType, ContextType, RequireFields<MutationUpdateDeckByIdArgs, 'input'>>;
  updateGeneratedArtByHashAndOwner?: Resolver<Maybe<ResolversTypes['UpdateGeneratedArtPayload']>, ParentType, ContextType, RequireFields<MutationUpdateGeneratedArtByHashAndOwnerArgs, 'input'>>;
  updatePublishedCard?: Resolver<Maybe<ResolversTypes['UpdatePublishedCardPayload']>, ParentType, ContextType, RequireFields<MutationUpdatePublishedCardArgs, 'input'>>;
  updatePublishedCardById?: Resolver<Maybe<ResolversTypes['UpdatePublishedCardPayload']>, ParentType, ContextType, RequireFields<MutationUpdatePublishedCardByIdArgs, 'input'>>;
  upgradeCard?: Resolver<ResolversTypes['RogueRun'], ParentType, ContextType, RequireFields<MutationUpgradeCardArgs, 'cardId' | 'rogueId'>>;
};

export type NodeResolvers<ContextType = any, ParentType extends ResolversParentTypes['Node'] = ResolversParentTypes['Node']> = {
  __resolveType: TypeResolveFn<'Card' | 'CardsInDeck' | 'Deck' | 'DeckShare' | 'PublishedCard' | 'Query' | 'RogueChoice' | 'RogueRun', ParentType, ContextType>;
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

export type PutCardResultResolvers<ContextType = any, ParentType extends ResolversParentTypes['PutCardResult'] = ResolversParentTypes['PutCardResult']> = {
  cardId?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  cardScriptErrors?: Resolver<Array<ResolversTypes['String']>, ParentType, ContextType>;
  editableCardId?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type QueryResolvers<ContextType = any, ParentType extends ResolversParentTypes['Query'] = ResolversParentTypes['Query']> = {
  account?: Resolver<Maybe<ResolversTypes['UserEntity']>, ParentType, ContextType>;
  accounts?: Resolver<Array<ResolversTypes['UserEntity']>, ParentType, ContextType, RequireFields<QueryAccountsArgs, 'userIds'>>;
  allCards?: Resolver<Maybe<ResolversTypes['CardsConnection']>, ParentType, ContextType, RequireFields<QueryAllCardsArgs, 'includeArchived' | 'orderBy'>>;
  allCardsInDecks?: Resolver<Maybe<ResolversTypes['CardsInDecksConnection']>, ParentType, ContextType, RequireFields<QueryAllCardsInDecksArgs, 'orderBy'>>;
  allClasses?: Resolver<Maybe<ResolversTypes['ClassesConnection']>, ParentType, ContextType, RequireFields<QueryAllClassesArgs, 'orderBy'>>;
  allCollectionCards?: Resolver<Maybe<ResolversTypes['CollectionCardsConnection']>, ParentType, ContextType, RequireFields<QueryAllCollectionCardsArgs, 'orderBy'>>;
  allDeckShares?: Resolver<Maybe<ResolversTypes['DeckSharesConnection']>, ParentType, ContextType, RequireFields<QueryAllDeckSharesArgs, 'orderBy'>>;
  allDecks?: Resolver<Maybe<ResolversTypes['DecksConnection']>, ParentType, ContextType, RequireFields<QueryAllDecksArgs, 'orderBy'>>;
  allGeneratedArts?: Resolver<Maybe<ResolversTypes['GeneratedArtsConnection']>, ParentType, ContextType, RequireFields<QueryAllGeneratedArtsArgs, 'includeArchived' | 'orderBy'>>;
  allPublishedCards?: Resolver<Maybe<ResolversTypes['PublishedCardsConnection']>, ParentType, ContextType, RequireFields<QueryAllPublishedCardsArgs, 'orderBy'>>;
  allRogueChoices?: Resolver<Maybe<ResolversTypes['RogueChoicesConnection']>, ParentType, ContextType, RequireFields<QueryAllRogueChoicesArgs, 'orderBy'>>;
  allRogueRuns?: Resolver<Maybe<ResolversTypes['RogueRunsConnection']>, ParentType, ContextType, RequireFields<QueryAllRogueRunsArgs, 'orderBy'>>;
  canSeeDeck?: Resolver<Maybe<ResolversTypes['Boolean']>, ParentType, ContextType, Partial<QueryCanSeeDeckArgs>>;
  card?: Resolver<Maybe<ResolversTypes['Card']>, ParentType, ContextType, RequireFields<QueryCardArgs, 'nodeId'>>;
  cardBySuccession?: Resolver<Maybe<ResolversTypes['Card']>, ParentType, ContextType, RequireFields<QueryCardBySuccessionArgs, 'succession'>>;
  cards?: Resolver<ResolversTypes['GetCardsResponse'], ParentType, ContextType, Partial<QueryCardsArgs>>;
  cardsByUser?: Resolver<ResolversTypes['GetCardsResponse'], ParentType, ContextType, Partial<QueryCardsByUserArgs>>;
  cardsInDeck?: Resolver<Maybe<ResolversTypes['CardsInDeck']>, ParentType, ContextType, RequireFields<QueryCardsInDeckArgs, 'nodeId'>>;
  cardsInDeckById?: Resolver<Maybe<ResolversTypes['CardsInDeck']>, ParentType, ContextType, RequireFields<QueryCardsInDeckByIdArgs, 'id'>>;
  configuration?: Resolver<ResolversTypes['ClientConfiguration'], ParentType, ContextType>;
  currentRogueChoice?: Resolver<Maybe<ResolversTypes['RogueChoice']>, ParentType, ContextType, Partial<QueryCurrentRogueChoiceArgs>>;
  currentRogueClasses?: Resolver<Array<ResolversTypes['String']>, ParentType, ContextType>;
  currentRogueRun?: Resolver<Maybe<ResolversTypes['RogueRun']>, ParentType, ContextType>;
  currentUserId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  deck?: Resolver<ResolversTypes['DecksGetResponse'], ParentType, ContextType, RequireFields<QueryDeckArgs, 'deckId'>>;
  deckById?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType, RequireFields<QueryDeckByIdArgs, 'id'>>;
  deckShare?: Resolver<Maybe<ResolversTypes['DeckShare']>, ParentType, ContextType, RequireFields<QueryDeckShareArgs, 'nodeId'>>;
  deckShareByDeckIdAndShareRecipientId?: Resolver<Maybe<ResolversTypes['DeckShare']>, ParentType, ContextType, RequireFields<QueryDeckShareByDeckIdAndShareRecipientIdArgs, 'deckId' | 'shareRecipientId'>>;
  decks?: Resolver<Array<ResolversTypes['DecksGetResponse']>, ParentType, ContextType>;
  draft?: Resolver<ResolversTypes['DraftState'], ParentType, ContextType>;
  friends?: Resolver<Array<ResolversTypes['Friend']>, ParentType, ContextType>;
  gameRecord?: Resolver<Maybe<ResolversTypes['GameRecord']>, ParentType, ContextType, RequireFields<QueryGameRecordArgs, 'gameId'>>;
  gameRecordIds?: Resolver<Array<ResolversTypes['String']>, ParentType, ContextType>;
  generatedArtByHashAndOwner?: Resolver<Maybe<ResolversTypes['GeneratedArt']>, ParentType, ContextType, RequireFields<QueryGeneratedArtByHashAndOwnerArgs, 'hash' | 'owner'>>;
  getLatestCard?: Resolver<Maybe<ResolversTypes['Card']>, ParentType, ContextType, Partial<QueryGetLatestCardArgs>>;
  getUserId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  invite?: Resolver<ResolversTypes['InviteResponse'], ParentType, ContextType, RequireFields<QueryInviteArgs, 'inviteId'>>;
  invites?: Resolver<Array<ResolversTypes['Invite']>, ParentType, ContextType>;
  isInMatch?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  matchmakingQueues?: Resolver<Array<ResolversTypes['MatchmakingQueue']>, ParentType, ContextType>;
  node?: Resolver<Maybe<ResolversTypes['Node']>, ParentType, ContextType, RequireFields<QueryNodeArgs, 'nodeId'>>;
  nodeId?: Resolver<ResolversTypes['ID'], ParentType, ContextType>;
  publishedCard?: Resolver<Maybe<ResolversTypes['PublishedCard']>, ParentType, ContextType, RequireFields<QueryPublishedCardArgs, 'nodeId'>>;
  publishedCardById?: Resolver<Maybe<ResolversTypes['PublishedCard']>, ParentType, ContextType, RequireFields<QueryPublishedCardByIdArgs, 'id'>>;
  query?: Resolver<ResolversTypes['Query'], ParentType, ContextType>;
  rerollCost?: Resolver<ResolversTypes['Int'], ParentType, ContextType, RequireFields<QueryRerollCostArgs, 'rogueId'>>;
  rogueChoice?: Resolver<Maybe<ResolversTypes['RogueChoice']>, ParentType, ContextType, RequireFields<QueryRogueChoiceArgs, 'nodeId'>>;
  rogueChoiceById?: Resolver<Maybe<ResolversTypes['RogueChoice']>, ParentType, ContextType, RequireFields<QueryRogueChoiceByIdArgs, 'id'>>;
  rogueRun?: Resolver<Maybe<ResolversTypes['RogueRun']>, ParentType, ContextType, RequireFields<QueryRogueRunArgs, 'nodeId'>>;
  rogueRunByDeck?: Resolver<Maybe<ResolversTypes['RogueRun']>, ParentType, ContextType, RequireFields<QueryRogueRunByDeckArgs, 'deck'>>;
  rogueRunByGame?: Resolver<Maybe<ResolversTypes['RogueRun']>, ParentType, ContextType, RequireFields<QueryRogueRunByGameArgs, 'game'>>;
  rogueRunById?: Resolver<Maybe<ResolversTypes['RogueRun']>, ParentType, ContextType, RequireFields<QueryRogueRunByIdArgs, 'id'>>;
  trashCardCost?: Resolver<ResolversTypes['Int'], ParentType, ContextType, RequireFields<QueryTrashCardCostArgs, 'cardId' | 'rogueId'>>;
  upgradeCardCost?: Resolver<ResolversTypes['Int'], ParentType, ContextType, RequireFields<QueryUpgradeCardCostArgs, 'cardId' | 'rogueId'>>;
};

export type RogueChoiceResolvers<ContextType = any, ParentType extends ResolversParentTypes['RogueChoice'] = ResolversParentTypes['RogueChoice']> = {
  canPick?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  canReroll?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  cards?: Resolver<Array<Maybe<ResolversTypes['String']>>, ParentType, ContextType>;
  id?: Resolver<ResolversTypes['BigInt'], ParentType, ContextType>;
  index?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  nodeId?: Resolver<ResolversTypes['ID'], ParentType, ContextType>;
  repopulate?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  rogueRun?: Resolver<ResolversTypes['BigInt'], ParentType, ContextType>;
  rogueRunByRogueRun?: Resolver<Maybe<ResolversTypes['RogueRun']>, ParentType, ContextType>;
  type?: Resolver<ResolversTypes['RogueChoiceType'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type RogueChoicesConnectionResolvers<ContextType = any, ParentType extends ResolversParentTypes['RogueChoicesConnection'] = ResolversParentTypes['RogueChoicesConnection']> = {
  edges?: Resolver<Array<Maybe<ResolversTypes['RogueChoicesEdge']>>, ParentType, ContextType>;
  nodes?: Resolver<Array<Maybe<ResolversTypes['RogueChoice']>>, ParentType, ContextType>;
  pageInfo?: Resolver<ResolversTypes['PageInfo'], ParentType, ContextType>;
  totalCount?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type RogueChoicesEdgeResolvers<ContextType = any, ParentType extends ResolversParentTypes['RogueChoicesEdge'] = ResolversParentTypes['RogueChoicesEdge']> = {
  cursor?: Resolver<Maybe<ResolversTypes['Cursor']>, ParentType, ContextType>;
  node?: Resolver<Maybe<ResolversTypes['RogueChoice']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type RogueRunResolvers<ContextType = any, ParentType extends ResolversParentTypes['RogueRun'] = ResolversParentTypes['RogueRun']> = {
  bossesDefeated?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  currentChoice?: Resolver<Maybe<ResolversTypes['RogueChoice']>, ParentType, ContextType>;
  deck?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  deckByDeck?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  deckByOpponentDeck?: Resolver<Maybe<ResolversTypes['Deck']>, ParentType, ContextType>;
  endedAt?: Resolver<Maybe<ResolversTypes['Datetime']>, ParentType, ContextType>;
  game?: Resolver<Maybe<ResolversTypes['BigInt']>, ParentType, ContextType>;
  gold?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  heroClass?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  id?: Resolver<ResolversTypes['BigInt'], ParentType, ContextType>;
  lives?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  nodeId?: Resolver<ResolversTypes['ID'], ParentType, ContextType>;
  opponentDeck?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  opponentInfo?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  player?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  rerollsThisRound?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  rogueChoicesByRogueRun?: Resolver<ResolversTypes['RogueChoicesConnection'], ParentType, ContextType, RequireFields<RogueRunRogueChoicesByRogueRunArgs, 'orderBy'>>;
  seed?: Resolver<ResolversTypes['BigInt'], ParentType, ContextType>;
  seedState?: Resolver<ResolversTypes['BigInt'], ParentType, ContextType>;
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

export type ServerGameMessageResolvers<ContextType = any, ParentType extends ResolversParentTypes['ServerGameMessage'] = ResolversParentTypes['ServerGameMessage']> = {
  actions?: Resolver<Maybe<ResolversTypes['GameActions']>, ParentType, ContextType>;
  changedEntityIds?: Resolver<Maybe<Array<ResolversTypes['Int']>>, ParentType, ContextType>;
  emote?: Resolver<Maybe<ResolversTypes['Emote']>, ParentType, ContextType>;
  event?: Resolver<Maybe<ResolversTypes['GameEvent']>, ParentType, ContextType>;
  gameOver?: Resolver<Maybe<ResolversTypes['GameOver']>, ParentType, ContextType>;
  gameState?: Resolver<Maybe<ResolversTypes['GameState']>, ParentType, ContextType>;
  id?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  isReplayMessage?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  localPlayerId?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  messageType?: Resolver<ResolversTypes['MessageType'], ParentType, ContextType>;
  startingCards?: Resolver<Maybe<Array<ResolversTypes['Entity']>>, ParentType, ContextType>;
  timers?: Resolver<Maybe<ResolversTypes['Timers']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type SetCardsInDeckPayloadResolvers<ContextType = any, ParentType extends ResolversParentTypes['SetCardsInDeckPayload'] = ResolversParentTypes['SetCardsInDeckPayload']> = {
  cardsInDecks?: Resolver<Maybe<Array<Maybe<ResolversTypes['CardsInDeck']>>>, ParentType, ContextType>;
  clientMutationId?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  query?: Resolver<Maybe<ResolversTypes['Query']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type SpellActionResolvers<ContextType = any, ParentType extends ResolversParentTypes['SpellAction'] = ResolversParentTypes['SpellAction']> = {
  action?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  actionType?: Resolver<ResolversTypes['ActionType'], ParentType, ContextType>;
  choices?: Resolver<Array<ResolversTypes['SpellAction']>, ParentType, ContextType>;
  description?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  entity?: Resolver<Maybe<ResolversTypes['Entity']>, ParentType, ContextType>;
  sourceId?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  targetKeyToActions?: Resolver<Array<ResolversTypes['TargetActionPair']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type SubscriptionResolvers<ContextType = any, ParentType extends ResolversParentTypes['Subscription'] = ResolversParentTypes['Subscription']> = {
  editableCardUpdated?: SubscriptionResolver<ResolversTypes['EditableCard'], "editableCardUpdated", ParentType, ContextType>;
  friendUpdated?: SubscriptionResolver<ResolversTypes['Friend'], "friendUpdated", ParentType, ContextType>;
  gameMessages?: SubscriptionResolver<ResolversTypes['ServerGameMessage'], "gameMessages", ParentType, ContextType>;
  inviteUpdated?: SubscriptionResolver<ResolversTypes['Invite'], "inviteUpdated", ParentType, ContextType>;
  matchFound?: SubscriptionResolver<ResolversTypes['MatchFound'], "matchFound", ParentType, ContextType>;
};

export type TargetActionPairResolvers<ContextType = any, ParentType extends ResolversParentTypes['TargetActionPair'] = ResolversParentTypes['TargetActionPair']> = {
  action?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  friendlyBattlefieldIndex?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  target?: Resolver<ResolversTypes['Int'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type TimersResolvers<ContextType = any, ParentType extends ResolversParentTypes['Timers'] = ResolversParentTypes['Timers']> = {
  millisRemaining?: Resolver<ResolversTypes['BigInt'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type TooltipResolvers<ContextType = any, ParentType extends ResolversParentTypes['Tooltip'] = ResolversParentTypes['Tooltip']> = {
  keywords?: Resolver<Array<ResolversTypes['String']>, ParentType, ContextType>;
  text?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
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

export type UserEntityResolvers<ContextType = any, ParentType extends ResolversParentTypes['UserEntity'] = ResolversParentTypes['UserEntity']> = {
  email?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  id?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  privacyToken?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  username?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type ValidationReportResolvers<ContextType = any, ParentType extends ResolversParentTypes['ValidationReport'] = ResolversParentTypes['ValidationReport']> = {
  errors?: Resolver<Array<ResolversTypes['String']>, ParentType, ContextType>;
  valid?: Resolver<ResolversTypes['Boolean'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type Resolvers<ContextType = any> = {
  AccessToken?: AccessTokenResolvers<ContextType>;
  ArchiveCardPayload?: ArchiveCardPayloadResolvers<ContextType>;
  AttributeValueTuple?: AttributeValueTupleResolvers<ContextType>;
  BigInt?: GraphQLScalarType;
  Card?: CardResolvers<ContextType>;
  CardRecord?: CardRecordResolvers<ContextType>;
  CardsConnection?: CardsConnectionResolvers<ContextType>;
  CardsEdge?: CardsEdgeResolvers<ContextType>;
  CardsInDeck?: CardsInDeckResolvers<ContextType>;
  CardsInDecksConnection?: CardsInDecksConnectionResolvers<ContextType>;
  CardsInDecksEdge?: CardsInDecksEdgeResolvers<ContextType>;
  Class?: ClassResolvers<ContextType>;
  ClassesConnection?: ClassesConnectionResolvers<ContextType>;
  ClassesEdge?: ClassesEdgeResolvers<ContextType>;
  ClientConfiguration?: ClientConfigurationResolvers<ContextType>;
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
  DecksGetResponse?: DecksGetResponseResolvers<ContextType>;
  DecksPutResponse?: DecksPutResponseResolvers<ContextType>;
  DeleteCardsInDeckPayload?: DeleteCardsInDeckPayloadResolvers<ContextType>;
  DeletePublishedCardPayload?: DeletePublishedCardPayloadResolvers<ContextType>;
  DraftState?: DraftStateResolvers<ContextType>;
  EditableCard?: EditableCardResolvers<ContextType>;
  Emote?: EmoteResolvers<ContextType>;
  EndRogueRunPayload?: EndRogueRunPayloadResolvers<ContextType>;
  Entity?: EntityResolvers<ContextType>;
  EntityLocation?: EntityLocationResolvers<ContextType>;
  Friend?: FriendResolvers<ContextType>;
  GameActions?: GameActionsResolvers<ContextType>;
  GameEvent?: GameEventResolvers<ContextType>;
  GameOver?: GameOverResolvers<ContextType>;
  GameRecord?: GameRecordResolvers<ContextType>;
  GameState?: GameStateResolvers<ContextType>;
  GeneratedArt?: GeneratedArtResolvers<ContextType>;
  GeneratedArtsConnection?: GeneratedArtsConnectionResolvers<ContextType>;
  GeneratedArtsEdge?: GeneratedArtsEdgeResolvers<ContextType>;
  GetCardsResponse?: GetCardsResponseResolvers<ContextType>;
  GetClassesPayload?: GetClassesPayloadResolvers<ContextType>;
  GetClassesRecord?: GetClassesRecordResolvers<ContextType>;
  GetCollectionCardsPayload?: GetCollectionCardsPayloadResolvers<ContextType>;
  GetCollectionCardsRecord?: GetCollectionCardsRecordResolvers<ContextType>;
  InventoryCollection?: InventoryCollectionResolvers<ContextType>;
  Invite?: InviteResolvers<ContextType>;
  InviteResponse?: InviteResponseResolvers<ContextType>;
  JSON?: GraphQLScalarType;
  LoginOrCreateReply?: LoginOrCreateReplyResolvers<ContextType>;
  MatchFound?: MatchFoundResolvers<ContextType>;
  MatchmakingQueue?: MatchmakingQueueResolvers<ContextType>;
  MatchmakingQueueRequires?: MatchmakingQueueRequiresResolvers<ContextType>;
  Mutation?: MutationResolvers<ContextType>;
  Node?: NodeResolvers<ContextType>;
  PageInfo?: PageInfoResolvers<ContextType>;
  PublishCardPayload?: PublishCardPayloadResolvers<ContextType>;
  PublishedCard?: PublishedCardResolvers<ContextType>;
  PublishedCardsConnection?: PublishedCardsConnectionResolvers<ContextType>;
  PublishedCardsEdge?: PublishedCardsEdgeResolvers<ContextType>;
  PutCardResult?: PutCardResultResolvers<ContextType>;
  Query?: QueryResolvers<ContextType>;
  RogueChoice?: RogueChoiceResolvers<ContextType>;
  RogueChoicesConnection?: RogueChoicesConnectionResolvers<ContextType>;
  RogueChoicesEdge?: RogueChoicesEdgeResolvers<ContextType>;
  RogueRun?: RogueRunResolvers<ContextType>;
  RogueRunsConnection?: RogueRunsConnectionResolvers<ContextType>;
  RogueRunsEdge?: RogueRunsEdgeResolvers<ContextType>;
  SaveCardPayload?: SaveCardPayloadResolvers<ContextType>;
  SaveGeneratedArtPayload?: SaveGeneratedArtPayloadResolvers<ContextType>;
  ServerGameMessage?: ServerGameMessageResolvers<ContextType>;
  SetCardsInDeckPayload?: SetCardsInDeckPayloadResolvers<ContextType>;
  SpellAction?: SpellActionResolvers<ContextType>;
  Subscription?: SubscriptionResolvers<ContextType>;
  TargetActionPair?: TargetActionPairResolvers<ContextType>;
  Timers?: TimersResolvers<ContextType>;
  Tooltip?: TooltipResolvers<ContextType>;
  UpdateCardPayload?: UpdateCardPayloadResolvers<ContextType>;
  UpdateCardsInDeckPayload?: UpdateCardsInDeckPayloadResolvers<ContextType>;
  UpdateDeckPayload?: UpdateDeckPayloadResolvers<ContextType>;
  UpdateGeneratedArtPayload?: UpdateGeneratedArtPayloadResolvers<ContextType>;
  UpdatePublishedCardPayload?: UpdatePublishedCardPayloadResolvers<ContextType>;
  UserEntity?: UserEntityResolvers<ContextType>;
  ValidationReport?: ValidationReportResolvers<ContextType>;
};


export type CardFragment = { __typename?: 'Card', id: string, createdBy: string, cardScript?: any | null, blocklyWorkspace?: any | null };

export type CardRecordFragment = { __typename?: 'CardRecord', id: any, cardId?: string | null, userId?: string | null, count: number, collectionIds: Array<string>, entity: { __typename?: 'Entity', id: number, name: string, description: string, cardId: string, cardType: CardType, entityType: EntityType, rarity: Rarity, owner: number, manaCost?: number | null, attack?: number | null, hp?: number | null, maxHp?: number | null, armor?: number | null, playable: boolean, taunt: boolean, charge: boolean, rush: boolean, divineShield: boolean, stealth: boolean, lifesteal: boolean, poisonous: boolean, deathrattles: boolean, battlecry: boolean, windfury: boolean, collectible: boolean, heroClasses: Array<string>, tribes: Array<string> } };

export type ClassFragment = { __typename?: 'Class', class?: string | null, collectible?: boolean | null, isPublished?: boolean | null, cardScript?: any | null, id?: string | null, name?: string | null };

export type CollectionCardFragment = { __typename?: 'CollectionCard', id?: string | null, createdBy?: string | null, cardScript?: any | null, blocklyWorkspace?: any | null, collectible?: boolean | null, cost?: number | null, type?: string | null, lastModified?: any | null };

export type DeckFragment = { __typename?: 'Deck', id: string, name?: string | null, isPremade: boolean, createdBy: string, heroClass?: string | null, format?: string | null, deckType: number };

export type DeckCardsFragment = { __typename?: 'Deck', cardsInDecksByDeckId: { __typename?: 'CardsInDecksConnection', totalCount: number, nodes: Array<{ __typename?: 'CardsInDeck', cardId: string, publishedCardByCardId?: { __typename?: 'PublishedCard', cardBySuccession?: { __typename?: 'Card', id: string, createdBy: string, cardScript?: any | null, blocklyWorkspace?: any | null } | null } | null } | null> } };

export type EntityFragment = { __typename?: 'Entity', id: number, name: string, description: string, cardId: string, cardType: CardType, entityType: EntityType, rarity: Rarity, owner: number, boardPosition: number, attack?: number | null, baseAttack?: number | null, hp?: number | null, baseHp?: number | null, maxHp?: number | null, armor?: number | null, manaCost?: number | null, baseManaCost?: number | null, durability?: number | null, spellDamage?: number | null, overload?: number | null, extraAttack?: number | null, mana: number, maxMana: number, lockedMana: number, battlecry: boolean, cannotAttack: boolean, charge: boolean, chooseOne: boolean, collectible: boolean, combo: boolean, conditionMet: boolean, deathrattles: boolean, deflect: boolean, destroyed: boolean, discarded: boolean, divineShield: boolean, enraged: boolean, frozen: boolean, gameStarted: boolean, gold: boolean, hostsTrigger: boolean, immune: boolean, isStartingTurn: boolean, lifesteal: boolean, permanent: boolean, playable: boolean, poisonous: boolean, roasted: boolean, rush: boolean, silenced: boolean, stealth: boolean, summoningSickness: boolean, taunt: boolean, uncensored: boolean, underAura: boolean, untargetableBySpells: boolean, windfury: boolean, charges?: number | null, countUntilCast?: number | null, fires?: number | null, host: number, heroClasses: Array<string>, cardSet: string, cardSets: Array<string>, enchantmentType: string, tribes: Array<string>, note: string, location?: { __typename?: 'EntityLocation', index: number, zone: Zone, player: number } | null, tooltips: Array<{ __typename?: 'Tooltip', text: string, keywords: Array<string> }> };

export type EntitySummaryFragment = { __typename?: 'Entity', id: number, name: string, description: string, cardId: string, cardType: CardType, entityType: EntityType, rarity: Rarity, owner: number, manaCost?: number | null, attack?: number | null, hp?: number | null, maxHp?: number | null, armor?: number | null, playable: boolean, taunt: boolean, charge: boolean, rush: boolean, divineShield: boolean, stealth: boolean, lifesteal: boolean, poisonous: boolean, deathrattles: boolean, battlecry: boolean, windfury: boolean, collectible: boolean, heroClasses: Array<string>, tribes: Array<string> };

export type GameStateFragment = { __typename?: 'GameState', isLocalPlayerTurn: boolean, turnNumber: number, turnState: string, timestamp: any, entities: Array<{ __typename?: 'Entity', id: number, name: string, description: string, cardId: string, cardType: CardType, entityType: EntityType, rarity: Rarity, owner: number, boardPosition: number, attack?: number | null, baseAttack?: number | null, hp?: number | null, baseHp?: number | null, maxHp?: number | null, armor?: number | null, manaCost?: number | null, baseManaCost?: number | null, durability?: number | null, spellDamage?: number | null, overload?: number | null, extraAttack?: number | null, mana: number, maxMana: number, lockedMana: number, battlecry: boolean, cannotAttack: boolean, charge: boolean, chooseOne: boolean, collectible: boolean, combo: boolean, conditionMet: boolean, deathrattles: boolean, deflect: boolean, destroyed: boolean, discarded: boolean, divineShield: boolean, enraged: boolean, frozen: boolean, gameStarted: boolean, gold: boolean, hostsTrigger: boolean, immune: boolean, isStartingTurn: boolean, lifesteal: boolean, permanent: boolean, playable: boolean, poisonous: boolean, roasted: boolean, rush: boolean, silenced: boolean, stealth: boolean, summoningSickness: boolean, taunt: boolean, uncensored: boolean, underAura: boolean, untargetableBySpells: boolean, windfury: boolean, charges?: number | null, countUntilCast?: number | null, fires?: number | null, host: number, heroClasses: Array<string>, cardSet: string, cardSets: Array<string>, enchantmentType: string, tribes: Array<string>, note: string, location?: { __typename?: 'EntityLocation', index: number, zone: Zone, player: number } | null, tooltips: Array<{ __typename?: 'Tooltip', text: string, keywords: Array<string> }> }> };

export type GeneratedArtFragment = { __typename?: 'GeneratedArt', hash: string, owner: string, urls: Array<string | null>, info?: any | null, isArchived: boolean };

export type InventoryCollectionFragment = { __typename?: 'InventoryCollection', id: string, name: string, heroClass: string, format: string, deckType: DeckType, collectionType: CollectionType, userId?: string | null, isStandardDeck: boolean, inventory: Array<{ __typename?: 'CardRecord', id: any, cardId?: string | null, userId?: string | null, count: number, collectionIds: Array<string>, entity: { __typename?: 'Entity', id: number, name: string, description: string, cardId: string, cardType: CardType, entityType: EntityType, rarity: Rarity, owner: number, manaCost?: number | null, attack?: number | null, hp?: number | null, maxHp?: number | null, armor?: number | null, playable: boolean, taunt: boolean, charge: boolean, rush: boolean, divineShield: boolean, stealth: boolean, lifesteal: boolean, poisonous: boolean, deathrattles: boolean, battlecry: boolean, windfury: boolean, collectible: boolean, heroClasses: Array<string>, tribes: Array<string> } }>, playerEntityAttributes: Array<{ __typename?: 'AttributeValueTuple', attribute: PlayerEntityAttribute, stringValue: string }>, validationReport?: { __typename?: 'ValidationReport', valid: boolean, errors: Array<string> } | null };

export type LoginOrCreateReplyFragment = { __typename?: 'LoginOrCreateReply', accessToken?: { __typename?: 'AccessToken', token: string } | null, userEntity?: { __typename?: 'UserEntity', id: string, email: string, username: string, privacyToken: string } | null };

export type RogueChoiceFragment = { __typename?: 'RogueChoice', id: any, cards: Array<string | null>, canPick: number, canReroll: boolean };

export type RogueRunFragment = { __typename?: 'RogueRun', id: any, player: string, state: RogueRunState, bossesDefeated: number, heroClass: string, startedAt: any, gold: number, deck: string, lives: number, opponentDeck?: string | null, opponentInfo?: string | null, currentChoice?: { __typename?: 'RogueChoice', id: any, cards: Array<string | null>, canPick: number, canReroll: boolean } | null };

export type ServerGameMessageFragment = { __typename?: 'ServerGameMessage', messageType: MessageType, id?: string | null, localPlayerId: number, isReplayMessage: boolean, gameState?: { __typename?: 'GameState', isLocalPlayerTurn: boolean, turnNumber: number, turnState: string, timestamp: any, entities: Array<{ __typename?: 'Entity', id: number, name: string, description: string, cardId: string, cardType: CardType, entityType: EntityType, rarity: Rarity, owner: number, boardPosition: number, attack?: number | null, baseAttack?: number | null, hp?: number | null, baseHp?: number | null, maxHp?: number | null, armor?: number | null, manaCost?: number | null, baseManaCost?: number | null, durability?: number | null, spellDamage?: number | null, overload?: number | null, extraAttack?: number | null, mana: number, maxMana: number, lockedMana: number, battlecry: boolean, cannotAttack: boolean, charge: boolean, chooseOne: boolean, collectible: boolean, combo: boolean, conditionMet: boolean, deathrattles: boolean, deflect: boolean, destroyed: boolean, discarded: boolean, divineShield: boolean, enraged: boolean, frozen: boolean, gameStarted: boolean, gold: boolean, hostsTrigger: boolean, immune: boolean, isStartingTurn: boolean, lifesteal: boolean, permanent: boolean, playable: boolean, poisonous: boolean, roasted: boolean, rush: boolean, silenced: boolean, stealth: boolean, summoningSickness: boolean, taunt: boolean, uncensored: boolean, underAura: boolean, untargetableBySpells: boolean, windfury: boolean, charges?: number | null, countUntilCast?: number | null, fires?: number | null, host: number, heroClasses: Array<string>, cardSet: string, cardSets: Array<string>, enchantmentType: string, tribes: Array<string>, note: string, location?: { __typename?: 'EntityLocation', index: number, zone: Zone, player: number } | null, tooltips: Array<{ __typename?: 'Tooltip', text: string, keywords: Array<string> }> }> } | null, actions?: { __typename?: 'GameActions', compatibility: Array<number>, all: Array<{ __typename?: 'SpellAction', action: number, actionType: ActionType, sourceId: number, description: string, entity?: { __typename?: 'Entity', id: number, name: string, description: string, cardId: string, cardType: CardType, entityType: EntityType, rarity: Rarity, owner: number, manaCost?: number | null, attack?: number | null, hp?: number | null, maxHp?: number | null, armor?: number | null, playable: boolean, taunt: boolean, charge: boolean, rush: boolean, divineShield: boolean, stealth: boolean, lifesteal: boolean, poisonous: boolean, deathrattles: boolean, battlecry: boolean, windfury: boolean, collectible: boolean, heroClasses: Array<string>, tribes: Array<string> } | null, targetKeyToActions: Array<{ __typename?: 'TargetActionPair', action: number, target: number, friendlyBattlefieldIndex: number }> }> } | null, startingCards?: Array<{ __typename?: 'Entity', id: number, name: string, description: string, cardId: string, cardType: CardType, entityType: EntityType, rarity: Rarity, owner: number, manaCost?: number | null, attack?: number | null, hp?: number | null, maxHp?: number | null, armor?: number | null, playable: boolean, taunt: boolean, charge: boolean, rush: boolean, divineShield: boolean, stealth: boolean, lifesteal: boolean, poisonous: boolean, deathrattles: boolean, battlecry: boolean, windfury: boolean, collectible: boolean, heroClasses: Array<string>, tribes: Array<string> }> | null, event?: { __typename?: 'GameEvent', eventType: GameEventType, id: number, description: string, isPowerHistory: boolean, isSourcePlayerLocal: boolean, isTargetPlayerLocal: boolean, value?: number | null, source?: { __typename?: 'Entity', id: number, name: string, description: string, cardId: string, cardType: CardType, entityType: EntityType, rarity: Rarity, owner: number, manaCost?: number | null, attack?: number | null, hp?: number | null, maxHp?: number | null, armor?: number | null, playable: boolean, taunt: boolean, charge: boolean, rush: boolean, divineShield: boolean, stealth: boolean, lifesteal: boolean, poisonous: boolean, deathrattles: boolean, battlecry: boolean, windfury: boolean, collectible: boolean, heroClasses: Array<string>, tribes: Array<string> } | null, target?: { __typename?: 'Entity', id: number, name: string, description: string, cardId: string, cardType: CardType, entityType: EntityType, rarity: Rarity, owner: number, manaCost?: number | null, attack?: number | null, hp?: number | null, maxHp?: number | null, armor?: number | null, playable: boolean, taunt: boolean, charge: boolean, rush: boolean, divineShield: boolean, stealth: boolean, lifesteal: boolean, poisonous: boolean, deathrattles: boolean, battlecry: boolean, windfury: boolean, collectible: boolean, heroClasses: Array<string>, tribes: Array<string> } | null, targets: Array<{ __typename?: 'Entity', id: number, name: string, description: string, cardId: string, cardType: CardType, entityType: EntityType, rarity: Rarity, owner: number, manaCost?: number | null, attack?: number | null, hp?: number | null, maxHp?: number | null, armor?: number | null, playable: boolean, taunt: boolean, charge: boolean, rush: boolean, divineShield: boolean, stealth: boolean, lifesteal: boolean, poisonous: boolean, deathrattles: boolean, battlecry: boolean, windfury: boolean, collectible: boolean, heroClasses: Array<string>, tribes: Array<string> }> } | null, gameOver?: { __typename?: 'GameOver', localPlayerWon: boolean, winningPlayerId?: number | null } | null, emote?: { __typename?: 'Emote', entityId: number, message: EmoteType } | null, timers?: { __typename?: 'Timers', millisRemaining: any } | null };

export type SpellActionFragment = { __typename?: 'SpellAction', action: number, actionType: ActionType, sourceId: number, description: string, entity?: { __typename?: 'Entity', id: number, name: string, description: string, cardId: string, cardType: CardType, entityType: EntityType, rarity: Rarity, owner: number, manaCost?: number | null, attack?: number | null, hp?: number | null, maxHp?: number | null, armor?: number | null, playable: boolean, taunt: boolean, charge: boolean, rush: boolean, divineShield: boolean, stealth: boolean, lifesteal: boolean, poisonous: boolean, deathrattles: boolean, battlecry: boolean, windfury: boolean, collectible: boolean, heroClasses: Array<string>, tribes: Array<string> } | null, targetKeyToActions: Array<{ __typename?: 'TargetActionPair', action: number, target: number, friendlyBattlefieldIndex: number }> };

export type UserEntityFragment = { __typename?: 'UserEntity', id: string, email: string, username: string, privacyToken: string };

export type CancelMatchmakingMutationVariables = Exact<{ [key: string]: never; }>;


export type CancelMatchmakingMutation = { __typename?: 'Mutation', cancelMatchmaking: boolean };

export type ChangePasswordMutationVariables = Exact<{
  newPassword: Scalars['String'];
}>;


export type ChangePasswordMutation = { __typename?: 'Mutation', changePassword: { __typename?: 'LoginOrCreateReply', accessToken?: { __typename?: 'AccessToken', token: string } | null, userEntity?: { __typename?: 'UserEntity', id: string, email: string, username: string, privacyToken: string } | null } };

export type ConcedeGameMutationVariables = Exact<{ [key: string]: never; }>;


export type ConcedeGameMutation = { __typename?: 'Mutation', concedeGame: boolean };

export type ConnectToGameMutationVariables = Exact<{
  playerKey: Scalars['String'];
  playerSecret: Scalars['String'];
}>;


export type ConnectToGameMutation = { __typename?: 'Mutation', connectToGame: boolean };

export type CreateAccountMutationVariables = Exact<{
  input: CreateAccountInput;
}>;


export type CreateAccountMutation = { __typename?: 'Mutation', createAccount: { __typename?: 'LoginOrCreateReply', accessToken?: { __typename?: 'AccessToken', token: string } | null, userEntity?: { __typename?: 'UserEntity', id: string, email: string, username: string, privacyToken: string } | null } };

export type CreateDeckMutationVariables = Exact<{
  deckName: Scalars['String'];
  heroClass: Scalars['String'];
  cardIds?: InputMaybe<Array<Scalars['String']> | Scalars['String']>;
  format: Scalars['String'];
}>;


export type CreateDeckMutation = { __typename?: 'Mutation', createDeckWithCards?: { __typename?: 'CreateDeckWithCardsPayload', deck?: { __typename?: 'Deck', id: string, name?: string | null, isPremade: boolean, createdBy: string, heroClass?: string | null, format?: string | null, deckType: number, cardsInDecksByDeckId: { __typename?: 'CardsInDecksConnection', totalCount: number, nodes: Array<{ __typename?: 'CardsInDeck', cardId: string, publishedCardByCardId?: { __typename?: 'PublishedCard', cardBySuccession?: { __typename?: 'Card', id: string, createdBy: string, cardScript?: any | null, blocklyWorkspace?: any | null } | null } | null } | null> } } | null } | null };

export type CreateGameDeckMutationVariables = Exact<{
  input: DecksPutInput;
}>;


export type CreateGameDeckMutation = { __typename?: 'Mutation', createDeck: { __typename?: 'DecksPutResponse', deckId: string, collection?: { __typename?: 'InventoryCollection', id: string, name: string, heroClass: string, format: string, deckType: DeckType, collectionType: CollectionType, userId?: string | null, isStandardDeck: boolean, inventory: Array<{ __typename?: 'CardRecord', id: any, cardId?: string | null, userId?: string | null, count: number, collectionIds: Array<string>, entity: { __typename?: 'Entity', id: number, name: string, description: string, cardId: string, cardType: CardType, entityType: EntityType, rarity: Rarity, owner: number, manaCost?: number | null, attack?: number | null, hp?: number | null, maxHp?: number | null, armor?: number | null, playable: boolean, taunt: boolean, charge: boolean, rush: boolean, divineShield: boolean, stealth: boolean, lifesteal: boolean, poisonous: boolean, deathrattles: boolean, battlecry: boolean, windfury: boolean, collectible: boolean, heroClasses: Array<string>, tribes: Array<string> } }>, playerEntityAttributes: Array<{ __typename?: 'AttributeValueTuple', attribute: PlayerEntityAttribute, stringValue: string }>, validationReport?: { __typename?: 'ValidationReport', valid: boolean, errors: Array<string> } | null } | null } };

export type DeleteArtMutationVariables = Exact<{
  hash: Scalars['String'];
  owner: Scalars['String'];
}>;


export type DeleteArtMutation = { __typename?: 'Mutation', updateGeneratedArtByHashAndOwner?: { __typename?: 'UpdateGeneratedArtPayload', generatedArt?: { __typename?: 'GeneratedArt', hash: string, owner: string, urls: Array<string | null>, info?: any | null, isArchived: boolean } | null } | null };

export type DeleteCardMutationVariables = Exact<{
  cardId: Scalars['String'];
}>;


export type DeleteCardMutation = { __typename?: 'Mutation', archiveCard?: { __typename?: 'ArchiveCardPayload', clientMutationId?: string | null } | null };

export type DeleteDeckMutationVariables = Exact<{
  deckId: Scalars['String'];
}>;


export type DeleteDeckMutation = { __typename?: 'Mutation', updateDeckById?: { __typename?: 'UpdateDeckPayload', deck?: { __typename?: 'Deck', trashed: boolean } | null } | null };

export type DeleteGameDeckMutationVariables = Exact<{
  deckId: Scalars['String'];
}>;


export type DeleteGameDeckMutation = { __typename?: 'Mutation', deleteDeck: boolean };

export type DuplicateGameDeckMutationVariables = Exact<{
  deckId: Scalars['String'];
}>;


export type DuplicateGameDeckMutation = { __typename?: 'Mutation', duplicateDeck: { __typename?: 'DecksGetResponse', inventoryIdsSize: number, collection?: { __typename?: 'InventoryCollection', id: string, name: string, heroClass: string, format: string, deckType: DeckType, collectionType: CollectionType, userId?: string | null, isStandardDeck: boolean, inventory: Array<{ __typename?: 'CardRecord', id: any, cardId?: string | null, userId?: string | null, count: number, collectionIds: Array<string>, entity: { __typename?: 'Entity', id: number, name: string, description: string, cardId: string, cardType: CardType, entityType: EntityType, rarity: Rarity, owner: number, manaCost?: number | null, attack?: number | null, hp?: number | null, maxHp?: number | null, armor?: number | null, playable: boolean, taunt: boolean, charge: boolean, rush: boolean, divineShield: boolean, stealth: boolean, lifesteal: boolean, poisonous: boolean, deathrattles: boolean, battlecry: boolean, windfury: boolean, collectible: boolean, heroClasses: Array<string>, tribes: Array<string> } }>, playerEntityAttributes: Array<{ __typename?: 'AttributeValueTuple', attribute: PlayerEntityAttribute, stringValue: string }>, validationReport?: { __typename?: 'ValidationReport', valid: boolean, errors: Array<string> } | null } | null } };

export type EnqueueMatchmakingMutationVariables = Exact<{
  input: MatchmakingEnqueueInput;
}>;


export type EnqueueMatchmakingMutation = { __typename?: 'Mutation', enqueueMatchmaking: boolean };

export type LoginMutationVariables = Exact<{
  input: LoginInput;
}>;


export type LoginMutation = { __typename?: 'Mutation', login: { __typename?: 'LoginOrCreateReply', accessToken?: { __typename?: 'AccessToken', token: string } | null, userEntity?: { __typename?: 'UserEntity', id: string, email: string, username: string, privacyToken: string } | null } };

export type MakeRogueChoiceMutationVariables = Exact<{
  choiceId: Scalars['BigInt'];
  choices: Array<Scalars['Int']> | Scalars['Int'];
}>;


export type MakeRogueChoiceMutation = { __typename?: 'Mutation', makeRogueChoice: { __typename?: 'RogueRun', id: any, player: string, state: RogueRunState, bossesDefeated: number, heroClass: string, startedAt: any, gold: number, deck: string, lives: number, opponentDeck?: string | null, opponentInfo?: string | null, currentChoice?: { __typename?: 'RogueChoice', id: any, cards: Array<string | null>, canPick: number, canReroll: boolean } | null } };

export type PublishCardMutationVariables = Exact<{
  cardId: Scalars['String'];
}>;


export type PublishCardMutation = { __typename?: 'Mutation', publishCard?: { __typename?: 'PublishCardPayload', bigInt?: any | null } | null };

export type RenameDeckMutationVariables = Exact<{
  deckId: Scalars['String'];
  deckName: Scalars['String'];
}>;


export type RenameDeckMutation = { __typename?: 'Mutation', updateDeckById?: { __typename?: 'UpdateDeckPayload', deck?: { __typename?: 'Deck', id: string, name?: string | null } | null } | null };

export type RequestPasswordResetEmailMutationVariables = Exact<{ [key: string]: never; }>;


export type RequestPasswordResetEmailMutation = { __typename?: 'Mutation', requestPasswordResetEmail: boolean };

export type RerollRogueMutationVariables = Exact<{
  choiceId: Scalars['BigInt'];
}>;


export type RerollRogueMutation = { __typename?: 'Mutation', reroll: { __typename?: 'RogueRun', id: any } };

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

export type SendEmoteMutationVariables = Exact<{
  entityId: Scalars['Int'];
  message: EmoteType;
}>;


export type SendEmoteMutation = { __typename?: 'Mutation', sendEmote: boolean };

export type SendGameActionMutationVariables = Exact<{
  actionIndex: Scalars['Int'];
  repliesTo: Scalars['String'];
}>;


export type SendGameActionMutation = { __typename?: 'Mutation', sendGameAction: boolean };

export type SendMulliganMutationVariables = Exact<{
  discardedCardIndices: Array<Scalars['Int']> | Scalars['Int'];
  repliesTo: Scalars['String'];
}>;


export type SendMulliganMutation = { __typename?: 'Mutation', sendMulligan: boolean };

export type SetCardsInDeckMutationVariables = Exact<{
  deckId: Scalars['String'];
  cardIds?: InputMaybe<Array<Scalars['String']> | Scalars['String']>;
}>;


export type SetCardsInDeckMutation = { __typename?: 'Mutation', setCardsInDeck?: { __typename?: 'SetCardsInDeckPayload', cardsInDecks?: Array<{ __typename?: 'CardsInDeck', id: any, cardId: string } | null> | null } | null };

export type SkipRogueBossMutationVariables = Exact<{
  rogueId: Scalars['BigInt'];
}>;


export type SkipRogueBossMutation = { __typename?: 'Mutation', skipBoss: { __typename?: 'RogueRun', id: any } };

export type StartRogueRunMutationVariables = Exact<{
  heroClass: Scalars['String'];
  seed?: InputMaybe<Scalars['BigInt']>;
}>;


export type StartRogueRunMutation = { __typename?: 'Mutation', startRogueRun: { __typename?: 'RogueRun', id: any, player: string, state: RogueRunState, bossesDefeated: number, heroClass: string, startedAt: any, gold: number, deck: string, lives: number, opponentDeck?: string | null, opponentInfo?: string | null, currentChoice?: { __typename?: 'RogueChoice', id: any, cards: Array<string | null>, canPick: number, canReroll: boolean } | null } };

export type TrashRogueCardMutationVariables = Exact<{
  rogueId: Scalars['BigInt'];
  cardId: Scalars['String'];
}>;


export type TrashRogueCardMutation = { __typename?: 'Mutation', trashCard: { __typename?: 'RogueRun', id: any } };

export type UpdateGameDeckMutationVariables = Exact<{
  input: DecksUpdateInput;
}>;


export type UpdateGameDeckMutation = { __typename?: 'Mutation', updateDeck: { __typename?: 'DecksGetResponse', inventoryIdsSize: number, collection?: { __typename?: 'InventoryCollection', id: string, name: string, heroClass: string, format: string, deckType: DeckType, collectionType: CollectionType, userId?: string | null, isStandardDeck: boolean, inventory: Array<{ __typename?: 'CardRecord', id: any, cardId?: string | null, userId?: string | null, count: number, collectionIds: Array<string>, entity: { __typename?: 'Entity', id: number, name: string, description: string, cardId: string, cardType: CardType, entityType: EntityType, rarity: Rarity, owner: number, manaCost?: number | null, attack?: number | null, hp?: number | null, maxHp?: number | null, armor?: number | null, playable: boolean, taunt: boolean, charge: boolean, rush: boolean, divineShield: boolean, stealth: boolean, lifesteal: boolean, poisonous: boolean, deathrattles: boolean, battlecry: boolean, windfury: boolean, collectible: boolean, heroClasses: Array<string>, tribes: Array<string> } }>, playerEntityAttributes: Array<{ __typename?: 'AttributeValueTuple', attribute: PlayerEntityAttribute, stringValue: string }>, validationReport?: { __typename?: 'ValidationReport', valid: boolean, errors: Array<string> } | null } | null } };

export type UpgradeRogueCardMutationVariables = Exact<{
  rogueId: Scalars['BigInt'];
  cardId: Scalars['String'];
}>;


export type UpgradeRogueCardMutation = { __typename?: 'Mutation', upgradeCard: { __typename?: 'RogueRun', id: any } };

export type GetAccountQueryVariables = Exact<{ [key: string]: never; }>;


export type GetAccountQuery = { __typename?: 'Query', account?: { __typename?: 'UserEntity', id: string, email: string, username: string, privacyToken: string } | null };

export type GetAccountsQueryVariables = Exact<{
  userIds: Array<Scalars['String']> | Scalars['String'];
}>;


export type GetAccountsQuery = { __typename?: 'Query', accounts: Array<{ __typename?: 'UserEntity', id: string, email: string, username: string, privacyToken: string }> };

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

export type GetConfigurationQueryVariables = Exact<{ [key: string]: never; }>;


export type GetConfigurationQuery = { __typename?: 'Query', configuration: { __typename?: 'ClientConfiguration', keycloakResetPasswordUrl?: string | null, keycloakAccountManagementUrl?: string | null, graphQlUrl?: string | null } };

export type GetCurrentRogueClassesQueryVariables = Exact<{ [key: string]: never; }>;


export type GetCurrentRogueClassesQuery = { __typename?: 'Query', currentRogueClasses: Array<string> };

export type GetDeckQueryVariables = Exact<{
  deckId: Scalars['String'];
}>;


export type GetDeckQuery = { __typename?: 'Query', deckById?: { __typename?: 'Deck', id: string, name?: string | null, isPremade: boolean, createdBy: string, heroClass?: string | null, format?: string | null, deckType: number, cardsInDecksByDeckId: { __typename?: 'CardsInDecksConnection', totalCount: number, nodes: Array<{ __typename?: 'CardsInDeck', cardId: string, publishedCardByCardId?: { __typename?: 'PublishedCard', cardBySuccession?: { __typename?: 'Card', id: string, createdBy: string, cardScript?: any | null, blocklyWorkspace?: any | null } | null } | null } | null> } } | null };

export type GetDecksQueryVariables = Exact<{
  user?: InputMaybe<Scalars['String']>;
}>;


export type GetDecksQuery = { __typename?: 'Query', allDecks?: { __typename?: 'DecksConnection', nodes: Array<{ __typename?: 'Deck', id: string, name?: string | null, isPremade: boolean, createdBy: string, heroClass?: string | null, format?: string | null, deckType: number } | null> } | null, allDeckShares?: { __typename?: 'DeckSharesConnection', nodes: Array<{ __typename?: 'DeckShare', deckByDeckId?: { __typename?: 'Deck', id: string, name?: string | null, isPremade: boolean, createdBy: string, heroClass?: string | null, format?: string | null, deckType: number } | null } | null> } | null };

export type GetGameCardsQueryVariables = Exact<{
  ifNoneMatch?: InputMaybe<Scalars['String']>;
}>;


export type GetGameCardsQuery = { __typename?: 'Query', cards: { __typename?: 'GetCardsResponse', version: string, cachedOk: boolean, cards: Array<{ __typename?: 'CardRecord', id: any, cardId?: string | null, userId?: string | null, count: number, collectionIds: Array<string>, entity: { __typename?: 'Entity', id: number, name: string, description: string, cardId: string, cardType: CardType, entityType: EntityType, rarity: Rarity, owner: number, manaCost?: number | null, attack?: number | null, hp?: number | null, maxHp?: number | null, armor?: number | null, playable: boolean, taunt: boolean, charge: boolean, rush: boolean, divineShield: boolean, stealth: boolean, lifesteal: boolean, poisonous: boolean, deathrattles: boolean, battlecry: boolean, windfury: boolean, collectible: boolean, heroClasses: Array<string>, tribes: Array<string> } }> } };

export type GetGameCardsByUserQueryVariables = Exact<{
  ifNoneMatch?: InputMaybe<Scalars['String']>;
}>;


export type GetGameCardsByUserQuery = { __typename?: 'Query', cardsByUser: { __typename?: 'GetCardsResponse', version: string, cachedOk: boolean, cards: Array<{ __typename?: 'CardRecord', id: any, cardId?: string | null, userId?: string | null, count: number, collectionIds: Array<string>, entity: { __typename?: 'Entity', id: number, name: string, description: string, cardId: string, cardType: CardType, entityType: EntityType, rarity: Rarity, owner: number, manaCost?: number | null, attack?: number | null, hp?: number | null, maxHp?: number | null, armor?: number | null, playable: boolean, taunt: boolean, charge: boolean, rush: boolean, divineShield: boolean, stealth: boolean, lifesteal: boolean, poisonous: boolean, deathrattles: boolean, battlecry: boolean, windfury: boolean, collectible: boolean, heroClasses: Array<string>, tribes: Array<string> } }> } };

export type GetGameDeckQueryVariables = Exact<{
  deckId: Scalars['String'];
}>;


export type GetGameDeckQuery = { __typename?: 'Query', deck: { __typename?: 'DecksGetResponse', inventoryIdsSize: number, collection?: { __typename?: 'InventoryCollection', id: string, name: string, heroClass: string, format: string, deckType: DeckType, collectionType: CollectionType, userId?: string | null, isStandardDeck: boolean, inventory: Array<{ __typename?: 'CardRecord', id: any, cardId?: string | null, userId?: string | null, count: number, collectionIds: Array<string>, entity: { __typename?: 'Entity', id: number, name: string, description: string, cardId: string, cardType: CardType, entityType: EntityType, rarity: Rarity, owner: number, manaCost?: number | null, attack?: number | null, hp?: number | null, maxHp?: number | null, armor?: number | null, playable: boolean, taunt: boolean, charge: boolean, rush: boolean, divineShield: boolean, stealth: boolean, lifesteal: boolean, poisonous: boolean, deathrattles: boolean, battlecry: boolean, windfury: boolean, collectible: boolean, heroClasses: Array<string>, tribes: Array<string> } }>, playerEntityAttributes: Array<{ __typename?: 'AttributeValueTuple', attribute: PlayerEntityAttribute, stringValue: string }>, validationReport?: { __typename?: 'ValidationReport', valid: boolean, errors: Array<string> } | null } | null } };

export type GetGameDecksQueryVariables = Exact<{ [key: string]: never; }>;


export type GetGameDecksQuery = { __typename?: 'Query', decks: Array<{ __typename?: 'DecksGetResponse', inventoryIdsSize: number, collection?: { __typename?: 'InventoryCollection', id: string, name: string, heroClass: string, format: string, deckType: DeckType, collectionType: CollectionType, userId?: string | null, isStandardDeck: boolean, inventory: Array<{ __typename?: 'CardRecord', id: any, cardId?: string | null, userId?: string | null, count: number, collectionIds: Array<string>, entity: { __typename?: 'Entity', id: number, name: string, description: string, cardId: string, cardType: CardType, entityType: EntityType, rarity: Rarity, owner: number, manaCost?: number | null, attack?: number | null, hp?: number | null, maxHp?: number | null, armor?: number | null, playable: boolean, taunt: boolean, charge: boolean, rush: boolean, divineShield: boolean, stealth: boolean, lifesteal: boolean, poisonous: boolean, deathrattles: boolean, battlecry: boolean, windfury: boolean, collectible: boolean, heroClasses: Array<string>, tribes: Array<string> } }>, playerEntityAttributes: Array<{ __typename?: 'AttributeValueTuple', attribute: PlayerEntityAttribute, stringValue: string }>, validationReport?: { __typename?: 'ValidationReport', valid: boolean, errors: Array<string> } | null } | null }> };

export type GetGeneratedArtQueryVariables = Exact<{ [key: string]: never; }>;


export type GetGeneratedArtQuery = { __typename?: 'Query', allGeneratedArts?: { __typename?: 'GeneratedArtsConnection', nodes: Array<{ __typename?: 'GeneratedArt', hash: string, owner: string, urls: Array<string | null>, info?: any | null, isArchived: boolean } | null> } | null };

export type GetMatchmakingQueuesQueryVariables = Exact<{ [key: string]: never; }>;


export type GetMatchmakingQueuesQuery = { __typename?: 'Query', matchmakingQueues: Array<{ __typename?: 'MatchmakingQueue', queueId: string, name: string, description: string, tooltip: string, requires?: { __typename?: 'MatchmakingQueueRequires', deck: boolean, heroClass: boolean, deckIdChoices: Array<string> } | null }> };

export type GetRerollCostQueryVariables = Exact<{
  rogueId: Scalars['BigInt'];
}>;


export type GetRerollCostQuery = { __typename?: 'Query', rerollCost: number };

export type GetTrashCardCostQueryVariables = Exact<{
  rogueId: Scalars['BigInt'];
  cardId: Scalars['String'];
}>;


export type GetTrashCardCostQuery = { __typename?: 'Query', trashCardCost: number };

export type GetUpgradeCardCostQueryVariables = Exact<{
  rogueId: Scalars['BigInt'];
  cardId: Scalars['String'];
}>;


export type GetUpgradeCardCostQuery = { __typename?: 'Query', upgradeCardCost: number };

export type GetUserIdTestQueryVariables = Exact<{ [key: string]: never; }>;


export type GetUserIdTestQuery = { __typename?: 'Query', currentUserId?: string | null };

export type IsInMatchQueryVariables = Exact<{ [key: string]: never; }>;


export type IsInMatchQuery = { __typename?: 'Query', isInMatch?: string | null };

export type EditableCardUpdatedSubscriptionVariables = Exact<{ [key: string]: never; }>;


export type EditableCardUpdatedSubscription = { __typename?: 'Subscription', editableCardUpdated: { __typename?: 'EditableCard', id: string, ownerUserId: string, source: string } };

export type FriendUpdatedSubscriptionVariables = Exact<{ [key: string]: never; }>;


export type FriendUpdatedSubscription = { __typename?: 'Subscription', friendUpdated: { __typename?: 'Friend', friendId: string, friendName: string, presence: Presence, since: any } };

export type GameMessagesSubscriptionVariables = Exact<{ [key: string]: never; }>;


export type GameMessagesSubscription = { __typename?: 'Subscription', gameMessages: { __typename?: 'ServerGameMessage', messageType: MessageType, id?: string | null, localPlayerId: number, isReplayMessage: boolean, gameState?: { __typename?: 'GameState', isLocalPlayerTurn: boolean, turnNumber: number, turnState: string, timestamp: any, entities: Array<{ __typename?: 'Entity', id: number, name: string, description: string, cardId: string, cardType: CardType, entityType: EntityType, rarity: Rarity, owner: number, boardPosition: number, attack?: number | null, baseAttack?: number | null, hp?: number | null, baseHp?: number | null, maxHp?: number | null, armor?: number | null, manaCost?: number | null, baseManaCost?: number | null, durability?: number | null, spellDamage?: number | null, overload?: number | null, extraAttack?: number | null, mana: number, maxMana: number, lockedMana: number, battlecry: boolean, cannotAttack: boolean, charge: boolean, chooseOne: boolean, collectible: boolean, combo: boolean, conditionMet: boolean, deathrattles: boolean, deflect: boolean, destroyed: boolean, discarded: boolean, divineShield: boolean, enraged: boolean, frozen: boolean, gameStarted: boolean, gold: boolean, hostsTrigger: boolean, immune: boolean, isStartingTurn: boolean, lifesteal: boolean, permanent: boolean, playable: boolean, poisonous: boolean, roasted: boolean, rush: boolean, silenced: boolean, stealth: boolean, summoningSickness: boolean, taunt: boolean, uncensored: boolean, underAura: boolean, untargetableBySpells: boolean, windfury: boolean, charges?: number | null, countUntilCast?: number | null, fires?: number | null, host: number, heroClasses: Array<string>, cardSet: string, cardSets: Array<string>, enchantmentType: string, tribes: Array<string>, note: string, location?: { __typename?: 'EntityLocation', index: number, zone: Zone, player: number } | null, tooltips: Array<{ __typename?: 'Tooltip', text: string, keywords: Array<string> }> }> } | null, actions?: { __typename?: 'GameActions', compatibility: Array<number>, all: Array<{ __typename?: 'SpellAction', action: number, actionType: ActionType, sourceId: number, description: string, entity?: { __typename?: 'Entity', id: number, name: string, description: string, cardId: string, cardType: CardType, entityType: EntityType, rarity: Rarity, owner: number, manaCost?: number | null, attack?: number | null, hp?: number | null, maxHp?: number | null, armor?: number | null, playable: boolean, taunt: boolean, charge: boolean, rush: boolean, divineShield: boolean, stealth: boolean, lifesteal: boolean, poisonous: boolean, deathrattles: boolean, battlecry: boolean, windfury: boolean, collectible: boolean, heroClasses: Array<string>, tribes: Array<string> } | null, targetKeyToActions: Array<{ __typename?: 'TargetActionPair', action: number, target: number, friendlyBattlefieldIndex: number }> }> } | null, startingCards?: Array<{ __typename?: 'Entity', id: number, name: string, description: string, cardId: string, cardType: CardType, entityType: EntityType, rarity: Rarity, owner: number, manaCost?: number | null, attack?: number | null, hp?: number | null, maxHp?: number | null, armor?: number | null, playable: boolean, taunt: boolean, charge: boolean, rush: boolean, divineShield: boolean, stealth: boolean, lifesteal: boolean, poisonous: boolean, deathrattles: boolean, battlecry: boolean, windfury: boolean, collectible: boolean, heroClasses: Array<string>, tribes: Array<string> }> | null, event?: { __typename?: 'GameEvent', eventType: GameEventType, id: number, description: string, isPowerHistory: boolean, isSourcePlayerLocal: boolean, isTargetPlayerLocal: boolean, value?: number | null, source?: { __typename?: 'Entity', id: number, name: string, description: string, cardId: string, cardType: CardType, entityType: EntityType, rarity: Rarity, owner: number, manaCost?: number | null, attack?: number | null, hp?: number | null, maxHp?: number | null, armor?: number | null, playable: boolean, taunt: boolean, charge: boolean, rush: boolean, divineShield: boolean, stealth: boolean, lifesteal: boolean, poisonous: boolean, deathrattles: boolean, battlecry: boolean, windfury: boolean, collectible: boolean, heroClasses: Array<string>, tribes: Array<string> } | null, target?: { __typename?: 'Entity', id: number, name: string, description: string, cardId: string, cardType: CardType, entityType: EntityType, rarity: Rarity, owner: number, manaCost?: number | null, attack?: number | null, hp?: number | null, maxHp?: number | null, armor?: number | null, playable: boolean, taunt: boolean, charge: boolean, rush: boolean, divineShield: boolean, stealth: boolean, lifesteal: boolean, poisonous: boolean, deathrattles: boolean, battlecry: boolean, windfury: boolean, collectible: boolean, heroClasses: Array<string>, tribes: Array<string> } | null, targets: Array<{ __typename?: 'Entity', id: number, name: string, description: string, cardId: string, cardType: CardType, entityType: EntityType, rarity: Rarity, owner: number, manaCost?: number | null, attack?: number | null, hp?: number | null, maxHp?: number | null, armor?: number | null, playable: boolean, taunt: boolean, charge: boolean, rush: boolean, divineShield: boolean, stealth: boolean, lifesteal: boolean, poisonous: boolean, deathrattles: boolean, battlecry: boolean, windfury: boolean, collectible: boolean, heroClasses: Array<string>, tribes: Array<string> }> } | null, gameOver?: { __typename?: 'GameOver', localPlayerWon: boolean, winningPlayerId?: number | null } | null, emote?: { __typename?: 'Emote', entityId: number, message: EmoteType } | null, timers?: { __typename?: 'Timers', millisRemaining: any } | null } };

export type InviteUpdatedSubscriptionVariables = Exact<{ [key: string]: never; }>;


export type InviteUpdatedSubscription = { __typename?: 'Subscription', inviteUpdated: { __typename?: 'Invite', id: string, expiresAt?: any | null, fromName?: string | null, fromUserId: string, toName?: string | null, toUserId: string, friendId?: string | null, message?: string | null, queueId?: string | null, status: InviteStatus } };

export type MatchFoundSubscriptionVariables = Exact<{ [key: string]: never; }>;


export type MatchFoundSubscription = { __typename?: 'Subscription', matchFound: { __typename?: 'MatchFound', gameId: string, url: string, playerKey: string, playerSecret: string } };

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
export const GeneratedArtFragmentDoc = gql`
    fragment generatedArt on GeneratedArt {
  hash
  owner
  urls
  info
  isArchived
}
    `;
export const EntitySummaryFragmentDoc = gql`
    fragment entitySummary on Entity {
  id
  name
  description
  cardId
  cardType
  entityType
  rarity
  owner
  manaCost
  attack
  hp
  maxHp
  armor
  playable
  taunt
  charge
  rush
  divineShield
  stealth
  lifesteal
  poisonous
  deathrattles
  battlecry
  windfury
  collectible
  heroClasses
  tribes
}
    `;
export const CardRecordFragmentDoc = gql`
    fragment cardRecord on CardRecord {
  id
  cardId
  userId
  count
  collectionIds
  entity {
    ...entitySummary
  }
}
    ${EntitySummaryFragmentDoc}`;
export const InventoryCollectionFragmentDoc = gql`
    fragment inventoryCollection on InventoryCollection {
  id
  name
  heroClass
  format
  deckType
  collectionType
  userId
  isStandardDeck
  inventory {
    ...cardRecord
  }
  playerEntityAttributes {
    attribute
    stringValue
  }
  validationReport {
    valid
    errors
  }
}
    ${CardRecordFragmentDoc}`;
export const UserEntityFragmentDoc = gql`
    fragment userEntity on UserEntity {
  id
  email
  username
  privacyToken
}
    `;
export const LoginOrCreateReplyFragmentDoc = gql`
    fragment loginOrCreateReply on LoginOrCreateReply {
  accessToken {
    token
  }
  userEntity {
    ...userEntity
  }
}
    ${UserEntityFragmentDoc}`;
export const RogueChoiceFragmentDoc = gql`
    fragment rogueChoice on RogueChoice {
  id
  cards
  canPick
  canReroll
}
    `;
export const RogueRunFragmentDoc = gql`
    fragment rogueRun on RogueRun {
  id
  player
  state
  bossesDefeated
  heroClass
  startedAt
  gold
  deck
  lives
  currentChoice {
    ...rogueChoice
  }
  opponentDeck
  opponentInfo
}
    ${RogueChoiceFragmentDoc}`;
export const EntityFragmentDoc = gql`
    fragment entity on Entity {
  id
  name
  description
  cardId
  cardType
  entityType
  rarity
  location {
    index
    zone
    player
  }
  owner
  boardPosition
  attack
  baseAttack
  hp
  baseHp
  maxHp
  armor
  manaCost
  baseManaCost
  durability
  spellDamage
  overload
  extraAttack
  mana
  maxMana
  lockedMana
  battlecry
  cannotAttack
  charge
  chooseOne
  collectible
  combo
  conditionMet
  deathrattles
  deflect
  destroyed
  discarded
  divineShield
  enraged
  frozen
  gameStarted
  gold
  hostsTrigger
  immune
  isStartingTurn
  lifesteal
  permanent
  playable
  poisonous
  roasted
  rush
  silenced
  stealth
  summoningSickness
  taunt
  uncensored
  underAura
  untargetableBySpells
  windfury
  charges
  countUntilCast
  fires
  host
  heroClasses
  cardSet
  cardSets
  enchantmentType
  tribes
  tooltips {
    text
    keywords
  }
  note
}
    `;
export const GameStateFragmentDoc = gql`
    fragment gameState on GameState {
  entities {
    ...entity
  }
  isLocalPlayerTurn
  turnNumber
  turnState
  timestamp
}
    ${EntityFragmentDoc}`;
export const SpellActionFragmentDoc = gql`
    fragment spellAction on SpellAction {
  action
  actionType
  sourceId
  description
  entity {
    ...entitySummary
  }
  targetKeyToActions {
    action
    target
    friendlyBattlefieldIndex
  }
}
    ${EntitySummaryFragmentDoc}`;
export const ServerGameMessageFragmentDoc = gql`
    fragment serverGameMessage on ServerGameMessage {
  messageType
  id
  localPlayerId
  isReplayMessage
  gameState {
    ...gameState
  }
  actions {
    all {
      ...spellAction
    }
    compatibility
  }
  startingCards {
    ...entitySummary
  }
  event {
    eventType
    id
    description
    isPowerHistory
    isSourcePlayerLocal
    isTargetPlayerLocal
    source {
      ...entitySummary
    }
    target {
      ...entitySummary
    }
    targets {
      ...entitySummary
    }
    value
  }
  gameOver {
    localPlayerWon
    winningPlayerId
  }
  emote {
    entityId
    message
  }
  timers {
    millisRemaining
  }
}
    ${GameStateFragmentDoc}
${SpellActionFragmentDoc}
${EntitySummaryFragmentDoc}`;
export const CancelMatchmakingDocument = gql`
    mutation cancelMatchmaking {
  cancelMatchmaking
}
    `;
export type CancelMatchmakingMutationFn = Apollo.MutationFunction<CancelMatchmakingMutation, CancelMatchmakingMutationVariables>;

/**
 * __useCancelMatchmakingMutation__
 *
 * To run a mutation, you first call `useCancelMatchmakingMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCancelMatchmakingMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [cancelMatchmakingMutation, { data, loading, error }] = useCancelMatchmakingMutation({
 *   variables: {
 *   },
 * });
 */
export function useCancelMatchmakingMutation(baseOptions?: Apollo.MutationHookOptions<CancelMatchmakingMutation, CancelMatchmakingMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CancelMatchmakingMutation, CancelMatchmakingMutationVariables>(CancelMatchmakingDocument, options);
      }
export type CancelMatchmakingMutationHookResult = ReturnType<typeof useCancelMatchmakingMutation>;
export type CancelMatchmakingMutationResult = Apollo.MutationResult<CancelMatchmakingMutation>;
export type CancelMatchmakingMutationOptions = Apollo.BaseMutationOptions<CancelMatchmakingMutation, CancelMatchmakingMutationVariables>;
export const ChangePasswordDocument = gql`
    mutation changePassword($newPassword: String!) {
  changePassword(newPassword: $newPassword) {
    ...loginOrCreateReply
  }
}
    ${LoginOrCreateReplyFragmentDoc}`;
export type ChangePasswordMutationFn = Apollo.MutationFunction<ChangePasswordMutation, ChangePasswordMutationVariables>;

/**
 * __useChangePasswordMutation__
 *
 * To run a mutation, you first call `useChangePasswordMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useChangePasswordMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [changePasswordMutation, { data, loading, error }] = useChangePasswordMutation({
 *   variables: {
 *      newPassword: // value for 'newPassword'
 *   },
 * });
 */
export function useChangePasswordMutation(baseOptions?: Apollo.MutationHookOptions<ChangePasswordMutation, ChangePasswordMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<ChangePasswordMutation, ChangePasswordMutationVariables>(ChangePasswordDocument, options);
      }
export type ChangePasswordMutationHookResult = ReturnType<typeof useChangePasswordMutation>;
export type ChangePasswordMutationResult = Apollo.MutationResult<ChangePasswordMutation>;
export type ChangePasswordMutationOptions = Apollo.BaseMutationOptions<ChangePasswordMutation, ChangePasswordMutationVariables>;
export const ConcedeGameDocument = gql`
    mutation concedeGame {
  concedeGame
}
    `;
export type ConcedeGameMutationFn = Apollo.MutationFunction<ConcedeGameMutation, ConcedeGameMutationVariables>;

/**
 * __useConcedeGameMutation__
 *
 * To run a mutation, you first call `useConcedeGameMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useConcedeGameMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [concedeGameMutation, { data, loading, error }] = useConcedeGameMutation({
 *   variables: {
 *   },
 * });
 */
export function useConcedeGameMutation(baseOptions?: Apollo.MutationHookOptions<ConcedeGameMutation, ConcedeGameMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<ConcedeGameMutation, ConcedeGameMutationVariables>(ConcedeGameDocument, options);
      }
export type ConcedeGameMutationHookResult = ReturnType<typeof useConcedeGameMutation>;
export type ConcedeGameMutationResult = Apollo.MutationResult<ConcedeGameMutation>;
export type ConcedeGameMutationOptions = Apollo.BaseMutationOptions<ConcedeGameMutation, ConcedeGameMutationVariables>;
export const ConnectToGameDocument = gql`
    mutation connectToGame($playerKey: String!, $playerSecret: String!) {
  connectToGame(playerKey: $playerKey, playerSecret: $playerSecret)
}
    `;
export type ConnectToGameMutationFn = Apollo.MutationFunction<ConnectToGameMutation, ConnectToGameMutationVariables>;

/**
 * __useConnectToGameMutation__
 *
 * To run a mutation, you first call `useConnectToGameMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useConnectToGameMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [connectToGameMutation, { data, loading, error }] = useConnectToGameMutation({
 *   variables: {
 *      playerKey: // value for 'playerKey'
 *      playerSecret: // value for 'playerSecret'
 *   },
 * });
 */
export function useConnectToGameMutation(baseOptions?: Apollo.MutationHookOptions<ConnectToGameMutation, ConnectToGameMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<ConnectToGameMutation, ConnectToGameMutationVariables>(ConnectToGameDocument, options);
      }
export type ConnectToGameMutationHookResult = ReturnType<typeof useConnectToGameMutation>;
export type ConnectToGameMutationResult = Apollo.MutationResult<ConnectToGameMutation>;
export type ConnectToGameMutationOptions = Apollo.BaseMutationOptions<ConnectToGameMutation, ConnectToGameMutationVariables>;
export const CreateAccountDocument = gql`
    mutation createAccount($input: CreateAccountInput!) {
  createAccount(input: $input) {
    ...loginOrCreateReply
  }
}
    ${LoginOrCreateReplyFragmentDoc}`;
export type CreateAccountMutationFn = Apollo.MutationFunction<CreateAccountMutation, CreateAccountMutationVariables>;

/**
 * __useCreateAccountMutation__
 *
 * To run a mutation, you first call `useCreateAccountMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateAccountMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createAccountMutation, { data, loading, error }] = useCreateAccountMutation({
 *   variables: {
 *      input: // value for 'input'
 *   },
 * });
 */
export function useCreateAccountMutation(baseOptions?: Apollo.MutationHookOptions<CreateAccountMutation, CreateAccountMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateAccountMutation, CreateAccountMutationVariables>(CreateAccountDocument, options);
      }
export type CreateAccountMutationHookResult = ReturnType<typeof useCreateAccountMutation>;
export type CreateAccountMutationResult = Apollo.MutationResult<CreateAccountMutation>;
export type CreateAccountMutationOptions = Apollo.BaseMutationOptions<CreateAccountMutation, CreateAccountMutationVariables>;
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
export const CreateGameDeckDocument = gql`
    mutation createGameDeck($input: DecksPutInput!) {
  createDeck(input: $input) {
    deckId
    collection {
      ...inventoryCollection
    }
  }
}
    ${InventoryCollectionFragmentDoc}`;
export type CreateGameDeckMutationFn = Apollo.MutationFunction<CreateGameDeckMutation, CreateGameDeckMutationVariables>;

/**
 * __useCreateGameDeckMutation__
 *
 * To run a mutation, you first call `useCreateGameDeckMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useCreateGameDeckMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [createGameDeckMutation, { data, loading, error }] = useCreateGameDeckMutation({
 *   variables: {
 *      input: // value for 'input'
 *   },
 * });
 */
export function useCreateGameDeckMutation(baseOptions?: Apollo.MutationHookOptions<CreateGameDeckMutation, CreateGameDeckMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<CreateGameDeckMutation, CreateGameDeckMutationVariables>(CreateGameDeckDocument, options);
      }
export type CreateGameDeckMutationHookResult = ReturnType<typeof useCreateGameDeckMutation>;
export type CreateGameDeckMutationResult = Apollo.MutationResult<CreateGameDeckMutation>;
export type CreateGameDeckMutationOptions = Apollo.BaseMutationOptions<CreateGameDeckMutation, CreateGameDeckMutationVariables>;
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
export const DeleteGameDeckDocument = gql`
    mutation deleteGameDeck($deckId: String!) {
  deleteDeck(deckId: $deckId)
}
    `;
export type DeleteGameDeckMutationFn = Apollo.MutationFunction<DeleteGameDeckMutation, DeleteGameDeckMutationVariables>;

/**
 * __useDeleteGameDeckMutation__
 *
 * To run a mutation, you first call `useDeleteGameDeckMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useDeleteGameDeckMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [deleteGameDeckMutation, { data, loading, error }] = useDeleteGameDeckMutation({
 *   variables: {
 *      deckId: // value for 'deckId'
 *   },
 * });
 */
export function useDeleteGameDeckMutation(baseOptions?: Apollo.MutationHookOptions<DeleteGameDeckMutation, DeleteGameDeckMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<DeleteGameDeckMutation, DeleteGameDeckMutationVariables>(DeleteGameDeckDocument, options);
      }
export type DeleteGameDeckMutationHookResult = ReturnType<typeof useDeleteGameDeckMutation>;
export type DeleteGameDeckMutationResult = Apollo.MutationResult<DeleteGameDeckMutation>;
export type DeleteGameDeckMutationOptions = Apollo.BaseMutationOptions<DeleteGameDeckMutation, DeleteGameDeckMutationVariables>;
export const DuplicateGameDeckDocument = gql`
    mutation duplicateGameDeck($deckId: String!) {
  duplicateDeck(deckId: $deckId) {
    collection {
      ...inventoryCollection
    }
    inventoryIdsSize
  }
}
    ${InventoryCollectionFragmentDoc}`;
export type DuplicateGameDeckMutationFn = Apollo.MutationFunction<DuplicateGameDeckMutation, DuplicateGameDeckMutationVariables>;

/**
 * __useDuplicateGameDeckMutation__
 *
 * To run a mutation, you first call `useDuplicateGameDeckMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useDuplicateGameDeckMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [duplicateGameDeckMutation, { data, loading, error }] = useDuplicateGameDeckMutation({
 *   variables: {
 *      deckId: // value for 'deckId'
 *   },
 * });
 */
export function useDuplicateGameDeckMutation(baseOptions?: Apollo.MutationHookOptions<DuplicateGameDeckMutation, DuplicateGameDeckMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<DuplicateGameDeckMutation, DuplicateGameDeckMutationVariables>(DuplicateGameDeckDocument, options);
      }
export type DuplicateGameDeckMutationHookResult = ReturnType<typeof useDuplicateGameDeckMutation>;
export type DuplicateGameDeckMutationResult = Apollo.MutationResult<DuplicateGameDeckMutation>;
export type DuplicateGameDeckMutationOptions = Apollo.BaseMutationOptions<DuplicateGameDeckMutation, DuplicateGameDeckMutationVariables>;
export const EnqueueMatchmakingDocument = gql`
    mutation enqueueMatchmaking($input: MatchmakingEnqueueInput!) {
  enqueueMatchmaking(input: $input)
}
    `;
export type EnqueueMatchmakingMutationFn = Apollo.MutationFunction<EnqueueMatchmakingMutation, EnqueueMatchmakingMutationVariables>;

/**
 * __useEnqueueMatchmakingMutation__
 *
 * To run a mutation, you first call `useEnqueueMatchmakingMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useEnqueueMatchmakingMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [enqueueMatchmakingMutation, { data, loading, error }] = useEnqueueMatchmakingMutation({
 *   variables: {
 *      input: // value for 'input'
 *   },
 * });
 */
export function useEnqueueMatchmakingMutation(baseOptions?: Apollo.MutationHookOptions<EnqueueMatchmakingMutation, EnqueueMatchmakingMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<EnqueueMatchmakingMutation, EnqueueMatchmakingMutationVariables>(EnqueueMatchmakingDocument, options);
      }
export type EnqueueMatchmakingMutationHookResult = ReturnType<typeof useEnqueueMatchmakingMutation>;
export type EnqueueMatchmakingMutationResult = Apollo.MutationResult<EnqueueMatchmakingMutation>;
export type EnqueueMatchmakingMutationOptions = Apollo.BaseMutationOptions<EnqueueMatchmakingMutation, EnqueueMatchmakingMutationVariables>;
export const LoginDocument = gql`
    mutation login($input: LoginInput!) {
  login(input: $input) {
    ...loginOrCreateReply
  }
}
    ${LoginOrCreateReplyFragmentDoc}`;
export type LoginMutationFn = Apollo.MutationFunction<LoginMutation, LoginMutationVariables>;

/**
 * __useLoginMutation__
 *
 * To run a mutation, you first call `useLoginMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useLoginMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [loginMutation, { data, loading, error }] = useLoginMutation({
 *   variables: {
 *      input: // value for 'input'
 *   },
 * });
 */
export function useLoginMutation(baseOptions?: Apollo.MutationHookOptions<LoginMutation, LoginMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<LoginMutation, LoginMutationVariables>(LoginDocument, options);
      }
export type LoginMutationHookResult = ReturnType<typeof useLoginMutation>;
export type LoginMutationResult = Apollo.MutationResult<LoginMutation>;
export type LoginMutationOptions = Apollo.BaseMutationOptions<LoginMutation, LoginMutationVariables>;
export const MakeRogueChoiceDocument = gql`
    mutation makeRogueChoice($choiceId: BigInt!, $choices: [Int!]!) {
  makeRogueChoice(choiceId: $choiceId, choices: $choices) {
    ...rogueRun
  }
}
    ${RogueRunFragmentDoc}`;
export type MakeRogueChoiceMutationFn = Apollo.MutationFunction<MakeRogueChoiceMutation, MakeRogueChoiceMutationVariables>;

/**
 * __useMakeRogueChoiceMutation__
 *
 * To run a mutation, you first call `useMakeRogueChoiceMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useMakeRogueChoiceMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [makeRogueChoiceMutation, { data, loading, error }] = useMakeRogueChoiceMutation({
 *   variables: {
 *      choiceId: // value for 'choiceId'
 *      choices: // value for 'choices'
 *   },
 * });
 */
export function useMakeRogueChoiceMutation(baseOptions?: Apollo.MutationHookOptions<MakeRogueChoiceMutation, MakeRogueChoiceMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<MakeRogueChoiceMutation, MakeRogueChoiceMutationVariables>(MakeRogueChoiceDocument, options);
      }
export type MakeRogueChoiceMutationHookResult = ReturnType<typeof useMakeRogueChoiceMutation>;
export type MakeRogueChoiceMutationResult = Apollo.MutationResult<MakeRogueChoiceMutation>;
export type MakeRogueChoiceMutationOptions = Apollo.BaseMutationOptions<MakeRogueChoiceMutation, MakeRogueChoiceMutationVariables>;
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
export const RequestPasswordResetEmailDocument = gql`
    mutation requestPasswordResetEmail {
  requestPasswordResetEmail
}
    `;
export type RequestPasswordResetEmailMutationFn = Apollo.MutationFunction<RequestPasswordResetEmailMutation, RequestPasswordResetEmailMutationVariables>;

/**
 * __useRequestPasswordResetEmailMutation__
 *
 * To run a mutation, you first call `useRequestPasswordResetEmailMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useRequestPasswordResetEmailMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [requestPasswordResetEmailMutation, { data, loading, error }] = useRequestPasswordResetEmailMutation({
 *   variables: {
 *   },
 * });
 */
export function useRequestPasswordResetEmailMutation(baseOptions?: Apollo.MutationHookOptions<RequestPasswordResetEmailMutation, RequestPasswordResetEmailMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<RequestPasswordResetEmailMutation, RequestPasswordResetEmailMutationVariables>(RequestPasswordResetEmailDocument, options);
      }
export type RequestPasswordResetEmailMutationHookResult = ReturnType<typeof useRequestPasswordResetEmailMutation>;
export type RequestPasswordResetEmailMutationResult = Apollo.MutationResult<RequestPasswordResetEmailMutation>;
export type RequestPasswordResetEmailMutationOptions = Apollo.BaseMutationOptions<RequestPasswordResetEmailMutation, RequestPasswordResetEmailMutationVariables>;
export const RerollRogueDocument = gql`
    mutation rerollRogue($choiceId: BigInt!) {
  reroll(choiceId: $choiceId) {
    id
  }
}
    `;
export type RerollRogueMutationFn = Apollo.MutationFunction<RerollRogueMutation, RerollRogueMutationVariables>;

/**
 * __useRerollRogueMutation__
 *
 * To run a mutation, you first call `useRerollRogueMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useRerollRogueMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [rerollRogueMutation, { data, loading, error }] = useRerollRogueMutation({
 *   variables: {
 *      choiceId: // value for 'choiceId'
 *   },
 * });
 */
export function useRerollRogueMutation(baseOptions?: Apollo.MutationHookOptions<RerollRogueMutation, RerollRogueMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<RerollRogueMutation, RerollRogueMutationVariables>(RerollRogueDocument, options);
      }
export type RerollRogueMutationHookResult = ReturnType<typeof useRerollRogueMutation>;
export type RerollRogueMutationResult = Apollo.MutationResult<RerollRogueMutation>;
export type RerollRogueMutationOptions = Apollo.BaseMutationOptions<RerollRogueMutation, RerollRogueMutationVariables>;
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
export const SendEmoteDocument = gql`
    mutation sendEmote($entityId: Int!, $message: EmoteType!) {
  sendEmote(entityId: $entityId, message: $message)
}
    `;
export type SendEmoteMutationFn = Apollo.MutationFunction<SendEmoteMutation, SendEmoteMutationVariables>;

/**
 * __useSendEmoteMutation__
 *
 * To run a mutation, you first call `useSendEmoteMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useSendEmoteMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [sendEmoteMutation, { data, loading, error }] = useSendEmoteMutation({
 *   variables: {
 *      entityId: // value for 'entityId'
 *      message: // value for 'message'
 *   },
 * });
 */
export function useSendEmoteMutation(baseOptions?: Apollo.MutationHookOptions<SendEmoteMutation, SendEmoteMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<SendEmoteMutation, SendEmoteMutationVariables>(SendEmoteDocument, options);
      }
export type SendEmoteMutationHookResult = ReturnType<typeof useSendEmoteMutation>;
export type SendEmoteMutationResult = Apollo.MutationResult<SendEmoteMutation>;
export type SendEmoteMutationOptions = Apollo.BaseMutationOptions<SendEmoteMutation, SendEmoteMutationVariables>;
export const SendGameActionDocument = gql`
    mutation sendGameAction($actionIndex: Int!, $repliesTo: String!) {
  sendGameAction(actionIndex: $actionIndex, repliesTo: $repliesTo)
}
    `;
export type SendGameActionMutationFn = Apollo.MutationFunction<SendGameActionMutation, SendGameActionMutationVariables>;

/**
 * __useSendGameActionMutation__
 *
 * To run a mutation, you first call `useSendGameActionMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useSendGameActionMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [sendGameActionMutation, { data, loading, error }] = useSendGameActionMutation({
 *   variables: {
 *      actionIndex: // value for 'actionIndex'
 *      repliesTo: // value for 'repliesTo'
 *   },
 * });
 */
export function useSendGameActionMutation(baseOptions?: Apollo.MutationHookOptions<SendGameActionMutation, SendGameActionMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<SendGameActionMutation, SendGameActionMutationVariables>(SendGameActionDocument, options);
      }
export type SendGameActionMutationHookResult = ReturnType<typeof useSendGameActionMutation>;
export type SendGameActionMutationResult = Apollo.MutationResult<SendGameActionMutation>;
export type SendGameActionMutationOptions = Apollo.BaseMutationOptions<SendGameActionMutation, SendGameActionMutationVariables>;
export const SendMulliganDocument = gql`
    mutation sendMulligan($discardedCardIndices: [Int!]!, $repliesTo: String!) {
  sendMulligan(discardedCardIndices: $discardedCardIndices, repliesTo: $repliesTo)
}
    `;
export type SendMulliganMutationFn = Apollo.MutationFunction<SendMulliganMutation, SendMulliganMutationVariables>;

/**
 * __useSendMulliganMutation__
 *
 * To run a mutation, you first call `useSendMulliganMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useSendMulliganMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [sendMulliganMutation, { data, loading, error }] = useSendMulliganMutation({
 *   variables: {
 *      discardedCardIndices: // value for 'discardedCardIndices'
 *      repliesTo: // value for 'repliesTo'
 *   },
 * });
 */
export function useSendMulliganMutation(baseOptions?: Apollo.MutationHookOptions<SendMulliganMutation, SendMulliganMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<SendMulliganMutation, SendMulliganMutationVariables>(SendMulliganDocument, options);
      }
export type SendMulliganMutationHookResult = ReturnType<typeof useSendMulliganMutation>;
export type SendMulliganMutationResult = Apollo.MutationResult<SendMulliganMutation>;
export type SendMulliganMutationOptions = Apollo.BaseMutationOptions<SendMulliganMutation, SendMulliganMutationVariables>;
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
export const SkipRogueBossDocument = gql`
    mutation skipRogueBoss($rogueId: BigInt!) {
  skipBoss(rogueId: $rogueId) {
    id
  }
}
    `;
export type SkipRogueBossMutationFn = Apollo.MutationFunction<SkipRogueBossMutation, SkipRogueBossMutationVariables>;

/**
 * __useSkipRogueBossMutation__
 *
 * To run a mutation, you first call `useSkipRogueBossMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useSkipRogueBossMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [skipRogueBossMutation, { data, loading, error }] = useSkipRogueBossMutation({
 *   variables: {
 *      rogueId: // value for 'rogueId'
 *   },
 * });
 */
export function useSkipRogueBossMutation(baseOptions?: Apollo.MutationHookOptions<SkipRogueBossMutation, SkipRogueBossMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<SkipRogueBossMutation, SkipRogueBossMutationVariables>(SkipRogueBossDocument, options);
      }
export type SkipRogueBossMutationHookResult = ReturnType<typeof useSkipRogueBossMutation>;
export type SkipRogueBossMutationResult = Apollo.MutationResult<SkipRogueBossMutation>;
export type SkipRogueBossMutationOptions = Apollo.BaseMutationOptions<SkipRogueBossMutation, SkipRogueBossMutationVariables>;
export const StartRogueRunDocument = gql`
    mutation startRogueRun($heroClass: String!, $seed: BigInt) {
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
export const TrashRogueCardDocument = gql`
    mutation trashRogueCard($rogueId: BigInt!, $cardId: String!) {
  trashCard(rogueId: $rogueId, cardId: $cardId) {
    id
  }
}
    `;
export type TrashRogueCardMutationFn = Apollo.MutationFunction<TrashRogueCardMutation, TrashRogueCardMutationVariables>;

/**
 * __useTrashRogueCardMutation__
 *
 * To run a mutation, you first call `useTrashRogueCardMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useTrashRogueCardMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [trashRogueCardMutation, { data, loading, error }] = useTrashRogueCardMutation({
 *   variables: {
 *      rogueId: // value for 'rogueId'
 *      cardId: // value for 'cardId'
 *   },
 * });
 */
export function useTrashRogueCardMutation(baseOptions?: Apollo.MutationHookOptions<TrashRogueCardMutation, TrashRogueCardMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<TrashRogueCardMutation, TrashRogueCardMutationVariables>(TrashRogueCardDocument, options);
      }
export type TrashRogueCardMutationHookResult = ReturnType<typeof useTrashRogueCardMutation>;
export type TrashRogueCardMutationResult = Apollo.MutationResult<TrashRogueCardMutation>;
export type TrashRogueCardMutationOptions = Apollo.BaseMutationOptions<TrashRogueCardMutation, TrashRogueCardMutationVariables>;
export const UpdateGameDeckDocument = gql`
    mutation updateGameDeck($input: DecksUpdateInput!) {
  updateDeck(input: $input) {
    collection {
      ...inventoryCollection
    }
    inventoryIdsSize
  }
}
    ${InventoryCollectionFragmentDoc}`;
export type UpdateGameDeckMutationFn = Apollo.MutationFunction<UpdateGameDeckMutation, UpdateGameDeckMutationVariables>;

/**
 * __useUpdateGameDeckMutation__
 *
 * To run a mutation, you first call `useUpdateGameDeckMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useUpdateGameDeckMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [updateGameDeckMutation, { data, loading, error }] = useUpdateGameDeckMutation({
 *   variables: {
 *      input: // value for 'input'
 *   },
 * });
 */
export function useUpdateGameDeckMutation(baseOptions?: Apollo.MutationHookOptions<UpdateGameDeckMutation, UpdateGameDeckMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<UpdateGameDeckMutation, UpdateGameDeckMutationVariables>(UpdateGameDeckDocument, options);
      }
export type UpdateGameDeckMutationHookResult = ReturnType<typeof useUpdateGameDeckMutation>;
export type UpdateGameDeckMutationResult = Apollo.MutationResult<UpdateGameDeckMutation>;
export type UpdateGameDeckMutationOptions = Apollo.BaseMutationOptions<UpdateGameDeckMutation, UpdateGameDeckMutationVariables>;
export const UpgradeRogueCardDocument = gql`
    mutation upgradeRogueCard($rogueId: BigInt!, $cardId: String!) {
  upgradeCard(rogueId: $rogueId, cardId: $cardId) {
    id
  }
}
    `;
export type UpgradeRogueCardMutationFn = Apollo.MutationFunction<UpgradeRogueCardMutation, UpgradeRogueCardMutationVariables>;

/**
 * __useUpgradeRogueCardMutation__
 *
 * To run a mutation, you first call `useUpgradeRogueCardMutation` within a React component and pass it any options that fit your needs.
 * When your component renders, `useUpgradeRogueCardMutation` returns a tuple that includes:
 * - A mutate function that you can call at any time to execute the mutation
 * - An object with fields that represent the current status of the mutation's execution
 *
 * @param baseOptions options that will be passed into the mutation, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options-2;
 *
 * @example
 * const [upgradeRogueCardMutation, { data, loading, error }] = useUpgradeRogueCardMutation({
 *   variables: {
 *      rogueId: // value for 'rogueId'
 *      cardId: // value for 'cardId'
 *   },
 * });
 */
export function useUpgradeRogueCardMutation(baseOptions?: Apollo.MutationHookOptions<UpgradeRogueCardMutation, UpgradeRogueCardMutationVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useMutation<UpgradeRogueCardMutation, UpgradeRogueCardMutationVariables>(UpgradeRogueCardDocument, options);
      }
export type UpgradeRogueCardMutationHookResult = ReturnType<typeof useUpgradeRogueCardMutation>;
export type UpgradeRogueCardMutationResult = Apollo.MutationResult<UpgradeRogueCardMutation>;
export type UpgradeRogueCardMutationOptions = Apollo.BaseMutationOptions<UpgradeRogueCardMutation, UpgradeRogueCardMutationVariables>;
export const GetAccountDocument = gql`
    query getAccount {
  account {
    ...userEntity
  }
}
    ${UserEntityFragmentDoc}`;

/**
 * __useGetAccountQuery__
 *
 * To run a query within a React component, call `useGetAccountQuery` and pass it any options that fit your needs.
 * When your component renders, `useGetAccountQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useGetAccountQuery({
 *   variables: {
 *   },
 * });
 */
export function useGetAccountQuery(baseOptions?: Apollo.QueryHookOptions<GetAccountQuery, GetAccountQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<GetAccountQuery, GetAccountQueryVariables>(GetAccountDocument, options);
      }
export function useGetAccountLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<GetAccountQuery, GetAccountQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<GetAccountQuery, GetAccountQueryVariables>(GetAccountDocument, options);
        }
export type GetAccountQueryHookResult = ReturnType<typeof useGetAccountQuery>;
export type GetAccountLazyQueryHookResult = ReturnType<typeof useGetAccountLazyQuery>;
export type GetAccountQueryResult = Apollo.QueryResult<GetAccountQuery, GetAccountQueryVariables>;
export const GetAccountsDocument = gql`
    query getAccounts($userIds: [String!]!) {
  accounts(userIds: $userIds) {
    ...userEntity
  }
}
    ${UserEntityFragmentDoc}`;

/**
 * __useGetAccountsQuery__
 *
 * To run a query within a React component, call `useGetAccountsQuery` and pass it any options that fit your needs.
 * When your component renders, `useGetAccountsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useGetAccountsQuery({
 *   variables: {
 *      userIds: // value for 'userIds'
 *   },
 * });
 */
export function useGetAccountsQuery(baseOptions: Apollo.QueryHookOptions<GetAccountsQuery, GetAccountsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<GetAccountsQuery, GetAccountsQueryVariables>(GetAccountsDocument, options);
      }
export function useGetAccountsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<GetAccountsQuery, GetAccountsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<GetAccountsQuery, GetAccountsQueryVariables>(GetAccountsDocument, options);
        }
export type GetAccountsQueryHookResult = ReturnType<typeof useGetAccountsQuery>;
export type GetAccountsLazyQueryHookResult = ReturnType<typeof useGetAccountsLazyQuery>;
export type GetAccountsQueryResult = Apollo.QueryResult<GetAccountsQuery, GetAccountsQueryVariables>;
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
export const GetConfigurationDocument = gql`
    query getConfiguration {
  configuration {
    keycloakResetPasswordUrl
    keycloakAccountManagementUrl
    graphQlUrl
  }
}
    `;

/**
 * __useGetConfigurationQuery__
 *
 * To run a query within a React component, call `useGetConfigurationQuery` and pass it any options that fit your needs.
 * When your component renders, `useGetConfigurationQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useGetConfigurationQuery({
 *   variables: {
 *   },
 * });
 */
export function useGetConfigurationQuery(baseOptions?: Apollo.QueryHookOptions<GetConfigurationQuery, GetConfigurationQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<GetConfigurationQuery, GetConfigurationQueryVariables>(GetConfigurationDocument, options);
      }
export function useGetConfigurationLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<GetConfigurationQuery, GetConfigurationQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<GetConfigurationQuery, GetConfigurationQueryVariables>(GetConfigurationDocument, options);
        }
export type GetConfigurationQueryHookResult = ReturnType<typeof useGetConfigurationQuery>;
export type GetConfigurationLazyQueryHookResult = ReturnType<typeof useGetConfigurationLazyQuery>;
export type GetConfigurationQueryResult = Apollo.QueryResult<GetConfigurationQuery, GetConfigurationQueryVariables>;
export const GetCurrentRogueClassesDocument = gql`
    query getCurrentRogueClasses {
  currentRogueClasses
}
    `;

/**
 * __useGetCurrentRogueClassesQuery__
 *
 * To run a query within a React component, call `useGetCurrentRogueClassesQuery` and pass it any options that fit your needs.
 * When your component renders, `useGetCurrentRogueClassesQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useGetCurrentRogueClassesQuery({
 *   variables: {
 *   },
 * });
 */
export function useGetCurrentRogueClassesQuery(baseOptions?: Apollo.QueryHookOptions<GetCurrentRogueClassesQuery, GetCurrentRogueClassesQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<GetCurrentRogueClassesQuery, GetCurrentRogueClassesQueryVariables>(GetCurrentRogueClassesDocument, options);
      }
export function useGetCurrentRogueClassesLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<GetCurrentRogueClassesQuery, GetCurrentRogueClassesQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<GetCurrentRogueClassesQuery, GetCurrentRogueClassesQueryVariables>(GetCurrentRogueClassesDocument, options);
        }
export type GetCurrentRogueClassesQueryHookResult = ReturnType<typeof useGetCurrentRogueClassesQuery>;
export type GetCurrentRogueClassesLazyQueryHookResult = ReturnType<typeof useGetCurrentRogueClassesLazyQuery>;
export type GetCurrentRogueClassesQueryResult = Apollo.QueryResult<GetCurrentRogueClassesQuery, GetCurrentRogueClassesQueryVariables>;
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
  allDecks(condition: {trashed: false, deckType: 1}) {
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
export const GetGameCardsDocument = gql`
    query getGameCards($ifNoneMatch: String) {
  cards(ifNoneMatch: $ifNoneMatch) {
    cards {
      ...cardRecord
    }
    version
    cachedOk
  }
}
    ${CardRecordFragmentDoc}`;

/**
 * __useGetGameCardsQuery__
 *
 * To run a query within a React component, call `useGetGameCardsQuery` and pass it any options that fit your needs.
 * When your component renders, `useGetGameCardsQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useGetGameCardsQuery({
 *   variables: {
 *      ifNoneMatch: // value for 'ifNoneMatch'
 *   },
 * });
 */
export function useGetGameCardsQuery(baseOptions?: Apollo.QueryHookOptions<GetGameCardsQuery, GetGameCardsQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<GetGameCardsQuery, GetGameCardsQueryVariables>(GetGameCardsDocument, options);
      }
export function useGetGameCardsLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<GetGameCardsQuery, GetGameCardsQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<GetGameCardsQuery, GetGameCardsQueryVariables>(GetGameCardsDocument, options);
        }
export type GetGameCardsQueryHookResult = ReturnType<typeof useGetGameCardsQuery>;
export type GetGameCardsLazyQueryHookResult = ReturnType<typeof useGetGameCardsLazyQuery>;
export type GetGameCardsQueryResult = Apollo.QueryResult<GetGameCardsQuery, GetGameCardsQueryVariables>;
export const GetGameCardsByUserDocument = gql`
    query getGameCardsByUser($ifNoneMatch: String) {
  cardsByUser(ifNoneMatch: $ifNoneMatch) {
    cards {
      ...cardRecord
    }
    version
    cachedOk
  }
}
    ${CardRecordFragmentDoc}`;

/**
 * __useGetGameCardsByUserQuery__
 *
 * To run a query within a React component, call `useGetGameCardsByUserQuery` and pass it any options that fit your needs.
 * When your component renders, `useGetGameCardsByUserQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useGetGameCardsByUserQuery({
 *   variables: {
 *      ifNoneMatch: // value for 'ifNoneMatch'
 *   },
 * });
 */
export function useGetGameCardsByUserQuery(baseOptions?: Apollo.QueryHookOptions<GetGameCardsByUserQuery, GetGameCardsByUserQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<GetGameCardsByUserQuery, GetGameCardsByUserQueryVariables>(GetGameCardsByUserDocument, options);
      }
export function useGetGameCardsByUserLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<GetGameCardsByUserQuery, GetGameCardsByUserQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<GetGameCardsByUserQuery, GetGameCardsByUserQueryVariables>(GetGameCardsByUserDocument, options);
        }
export type GetGameCardsByUserQueryHookResult = ReturnType<typeof useGetGameCardsByUserQuery>;
export type GetGameCardsByUserLazyQueryHookResult = ReturnType<typeof useGetGameCardsByUserLazyQuery>;
export type GetGameCardsByUserQueryResult = Apollo.QueryResult<GetGameCardsByUserQuery, GetGameCardsByUserQueryVariables>;
export const GetGameDeckDocument = gql`
    query getGameDeck($deckId: String!) {
  deck(deckId: $deckId) {
    collection {
      ...inventoryCollection
    }
    inventoryIdsSize
  }
}
    ${InventoryCollectionFragmentDoc}`;

/**
 * __useGetGameDeckQuery__
 *
 * To run a query within a React component, call `useGetGameDeckQuery` and pass it any options that fit your needs.
 * When your component renders, `useGetGameDeckQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useGetGameDeckQuery({
 *   variables: {
 *      deckId: // value for 'deckId'
 *   },
 * });
 */
export function useGetGameDeckQuery(baseOptions: Apollo.QueryHookOptions<GetGameDeckQuery, GetGameDeckQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<GetGameDeckQuery, GetGameDeckQueryVariables>(GetGameDeckDocument, options);
      }
export function useGetGameDeckLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<GetGameDeckQuery, GetGameDeckQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<GetGameDeckQuery, GetGameDeckQueryVariables>(GetGameDeckDocument, options);
        }
export type GetGameDeckQueryHookResult = ReturnType<typeof useGetGameDeckQuery>;
export type GetGameDeckLazyQueryHookResult = ReturnType<typeof useGetGameDeckLazyQuery>;
export type GetGameDeckQueryResult = Apollo.QueryResult<GetGameDeckQuery, GetGameDeckQueryVariables>;
export const GetGameDecksDocument = gql`
    query getGameDecks {
  decks {
    collection {
      ...inventoryCollection
    }
    inventoryIdsSize
  }
}
    ${InventoryCollectionFragmentDoc}`;

/**
 * __useGetGameDecksQuery__
 *
 * To run a query within a React component, call `useGetGameDecksQuery` and pass it any options that fit your needs.
 * When your component renders, `useGetGameDecksQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useGetGameDecksQuery({
 *   variables: {
 *   },
 * });
 */
export function useGetGameDecksQuery(baseOptions?: Apollo.QueryHookOptions<GetGameDecksQuery, GetGameDecksQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<GetGameDecksQuery, GetGameDecksQueryVariables>(GetGameDecksDocument, options);
      }
export function useGetGameDecksLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<GetGameDecksQuery, GetGameDecksQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<GetGameDecksQuery, GetGameDecksQueryVariables>(GetGameDecksDocument, options);
        }
export type GetGameDecksQueryHookResult = ReturnType<typeof useGetGameDecksQuery>;
export type GetGameDecksLazyQueryHookResult = ReturnType<typeof useGetGameDecksLazyQuery>;
export type GetGameDecksQueryResult = Apollo.QueryResult<GetGameDecksQuery, GetGameDecksQueryVariables>;
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
export const GetMatchmakingQueuesDocument = gql`
    query getMatchmakingQueues {
  matchmakingQueues {
    queueId
    name
    description
    tooltip
    requires {
      deck
      heroClass
      deckIdChoices
    }
  }
}
    `;

/**
 * __useGetMatchmakingQueuesQuery__
 *
 * To run a query within a React component, call `useGetMatchmakingQueuesQuery` and pass it any options that fit your needs.
 * When your component renders, `useGetMatchmakingQueuesQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useGetMatchmakingQueuesQuery({
 *   variables: {
 *   },
 * });
 */
export function useGetMatchmakingQueuesQuery(baseOptions?: Apollo.QueryHookOptions<GetMatchmakingQueuesQuery, GetMatchmakingQueuesQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<GetMatchmakingQueuesQuery, GetMatchmakingQueuesQueryVariables>(GetMatchmakingQueuesDocument, options);
      }
export function useGetMatchmakingQueuesLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<GetMatchmakingQueuesQuery, GetMatchmakingQueuesQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<GetMatchmakingQueuesQuery, GetMatchmakingQueuesQueryVariables>(GetMatchmakingQueuesDocument, options);
        }
export type GetMatchmakingQueuesQueryHookResult = ReturnType<typeof useGetMatchmakingQueuesQuery>;
export type GetMatchmakingQueuesLazyQueryHookResult = ReturnType<typeof useGetMatchmakingQueuesLazyQuery>;
export type GetMatchmakingQueuesQueryResult = Apollo.QueryResult<GetMatchmakingQueuesQuery, GetMatchmakingQueuesQueryVariables>;
export const GetRerollCostDocument = gql`
    query getRerollCost($rogueId: BigInt!) {
  rerollCost(rogueId: $rogueId)
}
    `;

/**
 * __useGetRerollCostQuery__
 *
 * To run a query within a React component, call `useGetRerollCostQuery` and pass it any options that fit your needs.
 * When your component renders, `useGetRerollCostQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useGetRerollCostQuery({
 *   variables: {
 *      rogueId: // value for 'rogueId'
 *   },
 * });
 */
export function useGetRerollCostQuery(baseOptions: Apollo.QueryHookOptions<GetRerollCostQuery, GetRerollCostQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<GetRerollCostQuery, GetRerollCostQueryVariables>(GetRerollCostDocument, options);
      }
export function useGetRerollCostLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<GetRerollCostQuery, GetRerollCostQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<GetRerollCostQuery, GetRerollCostQueryVariables>(GetRerollCostDocument, options);
        }
export type GetRerollCostQueryHookResult = ReturnType<typeof useGetRerollCostQuery>;
export type GetRerollCostLazyQueryHookResult = ReturnType<typeof useGetRerollCostLazyQuery>;
export type GetRerollCostQueryResult = Apollo.QueryResult<GetRerollCostQuery, GetRerollCostQueryVariables>;
export const GetTrashCardCostDocument = gql`
    query getTrashCardCost($rogueId: BigInt!, $cardId: String!) {
  trashCardCost(rogueId: $rogueId, cardId: $cardId)
}
    `;

/**
 * __useGetTrashCardCostQuery__
 *
 * To run a query within a React component, call `useGetTrashCardCostQuery` and pass it any options that fit your needs.
 * When your component renders, `useGetTrashCardCostQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useGetTrashCardCostQuery({
 *   variables: {
 *      rogueId: // value for 'rogueId'
 *      cardId: // value for 'cardId'
 *   },
 * });
 */
export function useGetTrashCardCostQuery(baseOptions: Apollo.QueryHookOptions<GetTrashCardCostQuery, GetTrashCardCostQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<GetTrashCardCostQuery, GetTrashCardCostQueryVariables>(GetTrashCardCostDocument, options);
      }
export function useGetTrashCardCostLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<GetTrashCardCostQuery, GetTrashCardCostQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<GetTrashCardCostQuery, GetTrashCardCostQueryVariables>(GetTrashCardCostDocument, options);
        }
export type GetTrashCardCostQueryHookResult = ReturnType<typeof useGetTrashCardCostQuery>;
export type GetTrashCardCostLazyQueryHookResult = ReturnType<typeof useGetTrashCardCostLazyQuery>;
export type GetTrashCardCostQueryResult = Apollo.QueryResult<GetTrashCardCostQuery, GetTrashCardCostQueryVariables>;
export const GetUpgradeCardCostDocument = gql`
    query getUpgradeCardCost($rogueId: BigInt!, $cardId: String!) {
  upgradeCardCost(rogueId: $rogueId, cardId: $cardId)
}
    `;

/**
 * __useGetUpgradeCardCostQuery__
 *
 * To run a query within a React component, call `useGetUpgradeCardCostQuery` and pass it any options that fit your needs.
 * When your component renders, `useGetUpgradeCardCostQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useGetUpgradeCardCostQuery({
 *   variables: {
 *      rogueId: // value for 'rogueId'
 *      cardId: // value for 'cardId'
 *   },
 * });
 */
export function useGetUpgradeCardCostQuery(baseOptions: Apollo.QueryHookOptions<GetUpgradeCardCostQuery, GetUpgradeCardCostQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<GetUpgradeCardCostQuery, GetUpgradeCardCostQueryVariables>(GetUpgradeCardCostDocument, options);
      }
export function useGetUpgradeCardCostLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<GetUpgradeCardCostQuery, GetUpgradeCardCostQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<GetUpgradeCardCostQuery, GetUpgradeCardCostQueryVariables>(GetUpgradeCardCostDocument, options);
        }
export type GetUpgradeCardCostQueryHookResult = ReturnType<typeof useGetUpgradeCardCostQuery>;
export type GetUpgradeCardCostLazyQueryHookResult = ReturnType<typeof useGetUpgradeCardCostLazyQuery>;
export type GetUpgradeCardCostQueryResult = Apollo.QueryResult<GetUpgradeCardCostQuery, GetUpgradeCardCostQueryVariables>;
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
export const IsInMatchDocument = gql`
    query isInMatch {
  isInMatch
}
    `;

/**
 * __useIsInMatchQuery__
 *
 * To run a query within a React component, call `useIsInMatchQuery` and pass it any options that fit your needs.
 * When your component renders, `useIsInMatchQuery` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the query, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useIsInMatchQuery({
 *   variables: {
 *   },
 * });
 */
export function useIsInMatchQuery(baseOptions?: Apollo.QueryHookOptions<IsInMatchQuery, IsInMatchQueryVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useQuery<IsInMatchQuery, IsInMatchQueryVariables>(IsInMatchDocument, options);
      }
export function useIsInMatchLazyQuery(baseOptions?: Apollo.LazyQueryHookOptions<IsInMatchQuery, IsInMatchQueryVariables>) {
          const options = {...defaultOptions, ...baseOptions}
          return Apollo.useLazyQuery<IsInMatchQuery, IsInMatchQueryVariables>(IsInMatchDocument, options);
        }
export type IsInMatchQueryHookResult = ReturnType<typeof useIsInMatchQuery>;
export type IsInMatchLazyQueryHookResult = ReturnType<typeof useIsInMatchLazyQuery>;
export type IsInMatchQueryResult = Apollo.QueryResult<IsInMatchQuery, IsInMatchQueryVariables>;
export const EditableCardUpdatedDocument = gql`
    subscription editableCardUpdated {
  editableCardUpdated {
    id
    ownerUserId
    source
  }
}
    `;

/**
 * __useEditableCardUpdatedSubscription__
 *
 * To run a query within a React component, call `useEditableCardUpdatedSubscription` and pass it any options that fit your needs.
 * When your component renders, `useEditableCardUpdatedSubscription` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the subscription, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useEditableCardUpdatedSubscription({
 *   variables: {
 *   },
 * });
 */
export function useEditableCardUpdatedSubscription(baseOptions?: Apollo.SubscriptionHookOptions<EditableCardUpdatedSubscription, EditableCardUpdatedSubscriptionVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useSubscription<EditableCardUpdatedSubscription, EditableCardUpdatedSubscriptionVariables>(EditableCardUpdatedDocument, options);
      }
export type EditableCardUpdatedSubscriptionHookResult = ReturnType<typeof useEditableCardUpdatedSubscription>;
export type EditableCardUpdatedSubscriptionResult = Apollo.SubscriptionResult<EditableCardUpdatedSubscription>;
export const FriendUpdatedDocument = gql`
    subscription friendUpdated {
  friendUpdated {
    friendId
    friendName
    presence
    since
  }
}
    `;

/**
 * __useFriendUpdatedSubscription__
 *
 * To run a query within a React component, call `useFriendUpdatedSubscription` and pass it any options that fit your needs.
 * When your component renders, `useFriendUpdatedSubscription` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the subscription, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useFriendUpdatedSubscription({
 *   variables: {
 *   },
 * });
 */
export function useFriendUpdatedSubscription(baseOptions?: Apollo.SubscriptionHookOptions<FriendUpdatedSubscription, FriendUpdatedSubscriptionVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useSubscription<FriendUpdatedSubscription, FriendUpdatedSubscriptionVariables>(FriendUpdatedDocument, options);
      }
export type FriendUpdatedSubscriptionHookResult = ReturnType<typeof useFriendUpdatedSubscription>;
export type FriendUpdatedSubscriptionResult = Apollo.SubscriptionResult<FriendUpdatedSubscription>;
export const GameMessagesDocument = gql`
    subscription gameMessages {
  gameMessages {
    ...serverGameMessage
  }
}
    ${ServerGameMessageFragmentDoc}`;

/**
 * __useGameMessagesSubscription__
 *
 * To run a query within a React component, call `useGameMessagesSubscription` and pass it any options that fit your needs.
 * When your component renders, `useGameMessagesSubscription` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the subscription, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useGameMessagesSubscription({
 *   variables: {
 *   },
 * });
 */
export function useGameMessagesSubscription(baseOptions?: Apollo.SubscriptionHookOptions<GameMessagesSubscription, GameMessagesSubscriptionVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useSubscription<GameMessagesSubscription, GameMessagesSubscriptionVariables>(GameMessagesDocument, options);
      }
export type GameMessagesSubscriptionHookResult = ReturnType<typeof useGameMessagesSubscription>;
export type GameMessagesSubscriptionResult = Apollo.SubscriptionResult<GameMessagesSubscription>;
export const InviteUpdatedDocument = gql`
    subscription inviteUpdated {
  inviteUpdated {
    id
    expiresAt
    fromName
    fromUserId
    toName
    toUserId
    friendId
    message
    queueId
    status
  }
}
    `;

/**
 * __useInviteUpdatedSubscription__
 *
 * To run a query within a React component, call `useInviteUpdatedSubscription` and pass it any options that fit your needs.
 * When your component renders, `useInviteUpdatedSubscription` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the subscription, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useInviteUpdatedSubscription({
 *   variables: {
 *   },
 * });
 */
export function useInviteUpdatedSubscription(baseOptions?: Apollo.SubscriptionHookOptions<InviteUpdatedSubscription, InviteUpdatedSubscriptionVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useSubscription<InviteUpdatedSubscription, InviteUpdatedSubscriptionVariables>(InviteUpdatedDocument, options);
      }
export type InviteUpdatedSubscriptionHookResult = ReturnType<typeof useInviteUpdatedSubscription>;
export type InviteUpdatedSubscriptionResult = Apollo.SubscriptionResult<InviteUpdatedSubscription>;
export const MatchFoundDocument = gql`
    subscription matchFound {
  matchFound {
    gameId
    url
    playerKey
    playerSecret
  }
}
    `;

/**
 * __useMatchFoundSubscription__
 *
 * To run a query within a React component, call `useMatchFoundSubscription` and pass it any options that fit your needs.
 * When your component renders, `useMatchFoundSubscription` returns an object from Apollo Client that contains loading, error, and data properties
 * you can use to render your UI.
 *
 * @param baseOptions options that will be passed into the subscription, supported options are listed on: https://www.apollographql.com/docs/react/api/react-hooks/#options;
 *
 * @example
 * const { data, loading, error } = useMatchFoundSubscription({
 *   variables: {
 *   },
 * });
 */
export function useMatchFoundSubscription(baseOptions?: Apollo.SubscriptionHookOptions<MatchFoundSubscription, MatchFoundSubscriptionVariables>) {
        const options = {...defaultOptions, ...baseOptions}
        return Apollo.useSubscription<MatchFoundSubscription, MatchFoundSubscriptionVariables>(MatchFoundDocument, options);
      }
export type MatchFoundSubscriptionHookResult = ReturnType<typeof useMatchFoundSubscription>;
export type MatchFoundSubscriptionResult = Apollo.SubscriptionResult<MatchFoundSubscription>;