export type Maybe<T> = T | null;
export type InputMaybe<T> = Maybe<T>;
/** All built-in and custom scalars, mapped to their actual values */
export type Scalars = {
  ID: { input: string; output: string; }
  String: { input: string; output: string; }
  Boolean: { input: boolean; output: boolean; }
  Int: { input: number; output: number; }
  Float: { input: number; output: number; }
  /** A 64-bit integer serialized as a string for JavaScript compatibility */
  BigInt: { input: string; output: string; }
  /** A location in a connection that can be used for resuming pagination. */
  Cursor: { input: any; output: any; }
  /**
   * A point in time as described by the [ISO
   * 8601](https://en.wikipedia.org/wiki/ISO_8601) and, if it has a timezone, [RFC
   * 3339](https://datatracker.ietf.org/doc/html/rfc3339) standards. Input values
   * that do not conform to both ISO 8601 and RFC 3339 may be coerced, which may lead
   * to unexpected results.
   */
  Datetime: { input: any; output: any; }
  /** A JSON scalar */
  JSON: { input: any; output: any; }
};

export type AcceptInviteInput = {
  deckId?: InputMaybe<Scalars['String']['input']>;
  inviteId: Scalars['String']['input'];
  queueId?: InputMaybe<Scalars['String']['input']>;
};

export type AccessToken = {
  __typename?: 'AccessToken';
  token: Scalars['String']['output'];
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
  cardId?: InputMaybe<Scalars['String']['input']>;
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']['input']>;
};

/** The output of our `archiveCard` mutation. */
export type ArchiveCardPayload = {
  __typename?: 'ArchiveCardPayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']['output']>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};

export type Art = {
  __typename?: 'Art';
  body?: Maybe<ArtFont>;
  highlight?: Maybe<ArtColor>;
  loop?: Maybe<ArtPrefab>;
  missile?: Maybe<ArtPrefab>;
  onCast?: Maybe<ArtPrefab>;
  onHit?: Maybe<ArtPrefab>;
  primary?: Maybe<ArtColor>;
  secondary?: Maybe<ArtColor>;
  shadow?: Maybe<ArtColor>;
  spell?: Maybe<ArtPrefab>;
  sprite?: Maybe<ArtSprite>;
  spriteShadow?: Maybe<ArtSprite>;
};

/** ─── Art types ──────────────────────────────────────────────── */
export type ArtColor = {
  __typename?: 'ArtColor';
  a: Scalars['Float']['output'];
  b: Scalars['Float']['output'];
  g: Scalars['Float']['output'];
  r: Scalars['Float']['output'];
};

export type ArtFont = {
  __typename?: 'ArtFont';
  vertex?: Maybe<ArtColor>;
};

export type ArtPrefab = {
  __typename?: 'ArtPrefab';
  named?: Maybe<Scalars['String']['output']>;
};

export type ArtSprite = {
  __typename?: 'ArtSprite';
  named?: Maybe<Scalars['String']['output']>;
};

export type AttributeValueInput = {
  attribute: PlayerEntityAttribute;
  stringValue: Scalars['String']['input'];
};

/** ─── Deck types ─────────────────────────────────────────────── */
export type AttributeValueTuple = {
  __typename?: 'AttributeValueTuple';
  attribute: PlayerEntityAttribute;
  stringValue: Scalars['String']['output'];
};

/** A filter to be used against BigInt fields. All fields are combined with a logical ‘and.’ */
export type BigIntFilter = {
  /** Not equal to the specified value, treating null like an ordinary value. */
  distinctFrom?: InputMaybe<Scalars['BigInt']['input']>;
  /** Equal to the specified value. */
  equalTo?: InputMaybe<Scalars['BigInt']['input']>;
  /** Greater than the specified value. */
  greaterThan?: InputMaybe<Scalars['BigInt']['input']>;
  /** Greater than or equal to the specified value. */
  greaterThanOrEqualTo?: InputMaybe<Scalars['BigInt']['input']>;
  /** Included in the specified list. */
  in?: InputMaybe<Array<Scalars['BigInt']['input']>>;
  /** Is null (if `true` is specified) or is not null (if `false` is specified). */
  isNull?: InputMaybe<Scalars['Boolean']['input']>;
  /** Less than the specified value. */
  lessThan?: InputMaybe<Scalars['BigInt']['input']>;
  /** Less than or equal to the specified value. */
  lessThanOrEqualTo?: InputMaybe<Scalars['BigInt']['input']>;
  /** Equal to the specified value, treating null like an ordinary value. */
  notDistinctFrom?: InputMaybe<Scalars['BigInt']['input']>;
  /** Not equal to the specified value. */
  notEqualTo?: InputMaybe<Scalars['BigInt']['input']>;
  /** Not included in the specified list. */
  notIn?: InputMaybe<Array<Scalars['BigInt']['input']>>;
};

/** A filter to be used against Boolean fields. All fields are combined with a logical ‘and.’ */
export type BooleanFilter = {
  /** Not equal to the specified value, treating null like an ordinary value. */
  distinctFrom?: InputMaybe<Scalars['Boolean']['input']>;
  /** Equal to the specified value. */
  equalTo?: InputMaybe<Scalars['Boolean']['input']>;
  /** Greater than the specified value. */
  greaterThan?: InputMaybe<Scalars['Boolean']['input']>;
  /** Greater than or equal to the specified value. */
  greaterThanOrEqualTo?: InputMaybe<Scalars['Boolean']['input']>;
  /** Included in the specified list. */
  in?: InputMaybe<Array<Scalars['Boolean']['input']>>;
  /** Is null (if `true` is specified) or is not null (if `false` is specified). */
  isNull?: InputMaybe<Scalars['Boolean']['input']>;
  /** Less than the specified value. */
  lessThan?: InputMaybe<Scalars['Boolean']['input']>;
  /** Less than or equal to the specified value. */
  lessThanOrEqualTo?: InputMaybe<Scalars['Boolean']['input']>;
  /** Equal to the specified value, treating null like an ordinary value. */
  notDistinctFrom?: InputMaybe<Scalars['Boolean']['input']>;
  /** Not equal to the specified value. */
  notEqualTo?: InputMaybe<Scalars['Boolean']['input']>;
  /** Not included in the specified list. */
  notIn?: InputMaybe<Array<Scalars['Boolean']['input']>>;
};

export type Card = Node & {
  __typename?: 'Card';
  blocklyWorkspace?: Maybe<Scalars['JSON']['output']>;
  cardScript?: Maybe<Scalars['JSON']['output']>;
  createdAt: Scalars['Datetime']['output'];
  createdBy: Scalars['String']['output'];
  id: Scalars['String']['output'];
  isArchived: Scalars['Boolean']['output'];
  isPrivate: Scalars['Boolean']['output'];
  isPublished: Scalars['Boolean']['output'];
  lastModified: Scalars['Datetime']['output'];
  /** A globally unique identifier. Can be used in various places throughout the system to identify this single value. */
  nodeId: Scalars['ID']['output'];
  /** Reads and enables pagination through a set of `PublishedCard`. */
  publishedCardsBySuccession: PublishedCardsConnection;
  succession: Scalars['BigInt']['output'];
  /**
   * The URI of the application that created this card. The git URL by default represents cards that came from the
   *     Spellsource git repository. https://www.getspellsource.com/cards/editor or similar represents cards authored in the
   *     web interface
   */
  uri?: Maybe<Scalars['String']['output']>;
};


export type CardPublishedCardsBySuccessionArgs = {
  after?: InputMaybe<Scalars['Cursor']['input']>;
  before?: InputMaybe<Scalars['Cursor']['input']>;
  condition?: InputMaybe<PublishedCardCondition>;
  filter?: InputMaybe<PublishedCardFilter>;
  first?: InputMaybe<Scalars['Int']['input']>;
  last?: InputMaybe<Scalars['Int']['input']>;
  offset?: InputMaybe<Scalars['Int']['input']>;
  orderBy?: InputMaybe<Array<PublishedCardsOrderBy>>;
};

/** A condition to be used against `Card` object types. All fields are tested for equality and combined with a logical ‘and.’ */
export type CardCondition = {
  /** Checks for equality with the object’s `blocklyWorkspace` field. */
  blocklyWorkspace?: InputMaybe<Scalars['JSON']['input']>;
  /** Checks for equality with the object’s `cardScript` field. */
  cardScript?: InputMaybe<Scalars['JSON']['input']>;
  /** Checks for equality with the object’s `createdAt` field. */
  createdAt?: InputMaybe<Scalars['Datetime']['input']>;
  /** Checks for equality with the object’s `createdBy` field. */
  createdBy?: InputMaybe<Scalars['String']['input']>;
  /** Checks for equality with the object’s `id` field. */
  id?: InputMaybe<Scalars['String']['input']>;
  /** Checks for equality with the object’s `isArchived` field. */
  isArchived?: InputMaybe<Scalars['Boolean']['input']>;
  /** Checks for equality with the object’s `isPrivate` field. */
  isPrivate?: InputMaybe<Scalars['Boolean']['input']>;
  /** Checks for equality with the object’s `isPublished` field. */
  isPublished?: InputMaybe<Scalars['Boolean']['input']>;
  /** Checks for equality with the object’s `lastModified` field. */
  lastModified?: InputMaybe<Scalars['Datetime']['input']>;
  /** Checks for equality with the object’s `succession` field. */
  succession?: InputMaybe<Scalars['BigInt']['input']>;
  /** Checks for equality with the object’s `uri` field. */
  uri?: InputMaybe<Scalars['String']['input']>;
};

export type CardEvent = {
  __typename?: 'CardEvent';
  card?: Maybe<Entity>;
  showLocal: Scalars['Boolean']['output'];
};

/** A filter to be used against `Card` object types. All fields are combined with a logical ‘and.’ */
export type CardFilter = {
  /** Checks for all expressions in this list. */
  and?: InputMaybe<Array<CardFilter>>;
  /** Filter by the object’s `createdAt` field. */
  createdAt?: InputMaybe<DatetimeFilter>;
  /** Filter by the object’s `createdBy` field. */
  createdBy?: InputMaybe<StringFilter>;
  /** Filter by the object’s `id` field. */
  id?: InputMaybe<StringFilter>;
  /** Filter by the object’s `isArchived` field. */
  isArchived?: InputMaybe<BooleanFilter>;
  /** Filter by the object’s `isPrivate` field. */
  isPrivate?: InputMaybe<BooleanFilter>;
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
  publishedCardsBySuccessionExist?: InputMaybe<Scalars['Boolean']['input']>;
  /** Filter by the object’s `succession` field. */
  succession?: InputMaybe<BigIntFilter>;
  /** Filter by the object’s `uri` field. */
  uri?: InputMaybe<StringFilter>;
};

/** An input for mutations affecting `Card` */
export type CardInput = {
  blocklyWorkspace?: InputMaybe<Scalars['JSON']['input']>;
  cardScript?: InputMaybe<Scalars['JSON']['input']>;
  createdAt?: InputMaybe<Scalars['Datetime']['input']>;
  createdBy: Scalars['String']['input'];
  id: Scalars['String']['input'];
  isArchived?: InputMaybe<Scalars['Boolean']['input']>;
  isPrivate?: InputMaybe<Scalars['Boolean']['input']>;
  isPublished?: InputMaybe<Scalars['Boolean']['input']>;
  lastModified?: InputMaybe<Scalars['Datetime']['input']>;
  /**
   * The URI of the application that created this card. The git URL by default represents cards that came from the
   *     Spellsource git repository. https://www.getspellsource.com/cards/editor or similar represents cards authored in the
   *     web interface
   */
  uri?: InputMaybe<Scalars['String']['input']>;
};

/** Represents an update to a `Card`. Fields that are set will be updated. */
export type CardPatch = {
  blocklyWorkspace?: InputMaybe<Scalars['JSON']['input']>;
  cardScript?: InputMaybe<Scalars['JSON']['input']>;
  createdAt?: InputMaybe<Scalars['Datetime']['input']>;
  createdBy?: InputMaybe<Scalars['String']['input']>;
  id?: InputMaybe<Scalars['String']['input']>;
  isArchived?: InputMaybe<Scalars['Boolean']['input']>;
  isPrivate?: InputMaybe<Scalars['Boolean']['input']>;
  isPublished?: InputMaybe<Scalars['Boolean']['input']>;
  lastModified?: InputMaybe<Scalars['Datetime']['input']>;
  /**
   * The URI of the application that created this card. The git URL by default represents cards that came from the
   *     Spellsource git repository. https://www.getspellsource.com/cards/editor or similar represents cards authored in the
   *     web interface
   */
  uri?: InputMaybe<Scalars['String']['input']>;
};

export type CardRecord = {
  __typename?: 'CardRecord';
  cardId?: Maybe<Scalars['String']['output']>;
  collectionIds: Array<Scalars['String']['output']>;
  count: Scalars['Int']['output'];
  entity: Entity;
  id: Scalars['BigInt']['output'];
  userId?: Maybe<Scalars['String']['output']>;
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
  totalCount: Scalars['Int']['output'];
};

/** A `Card` edge in the connection. */
export type CardsEdge = {
  __typename?: 'CardsEdge';
  /** A cursor for use in pagination. */
  cursor?: Maybe<Scalars['Cursor']['output']>;
  /** The `Card` at the end of the edge. */
  node?: Maybe<Card>;
};

export type CardsInDeck = Node & {
  __typename?: 'CardsInDeck';
  /** cannot delete cards that are currently used in decks */
  cardId: Scalars['String']['output'];
  /** Reads a single `Deck` that is related to this `CardsInDeck`. */
  deckByDeckId?: Maybe<Deck>;
  /** deleting a deck deletes all its card references */
  deckId: Scalars['String']['output'];
  id: Scalars['BigInt']['output'];
  /** A globally unique identifier. Can be used in various places throughout the system to identify this single value. */
  nodeId: Scalars['ID']['output'];
};

/**
 * A condition to be used against `CardsInDeck` object types. All fields are tested
 * for equality and combined with a logical ‘and.’
 */
export type CardsInDeckCondition = {
  /** Checks for equality with the object’s `cardId` field. */
  cardId?: InputMaybe<Scalars['String']['input']>;
  /** Checks for equality with the object’s `deckId` field. */
  deckId?: InputMaybe<Scalars['String']['input']>;
  /** Checks for equality with the object’s `id` field. */
  id?: InputMaybe<Scalars['BigInt']['input']>;
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
};

/** An input for mutations affecting `CardsInDeck` */
export type CardsInDeckInput = {
  /** cannot delete cards that are currently used in decks */
  cardId: Scalars['String']['input'];
  /** deleting a deck deletes all its card references */
  deckId: Scalars['String']['input'];
};

/** Represents an update to a `CardsInDeck`. Fields that are set will be updated. */
export type CardsInDeckPatch = {
  /** cannot delete cards that are currently used in decks */
  cardId?: InputMaybe<Scalars['String']['input']>;
  /** deleting a deck deletes all its card references */
  deckId?: InputMaybe<Scalars['String']['input']>;
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
  totalCount: Scalars['Int']['output'];
};

/** A `CardsInDeck` edge in the connection. */
export type CardsInDecksEdge = {
  __typename?: 'CardsInDecksEdge';
  /** A cursor for use in pagination. */
  cursor?: Maybe<Scalars['Cursor']['output']>;
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
  IsPrivateAsc: 'IS_PRIVATE_ASC',
  IsPrivateDesc: 'IS_PRIVATE_DESC',
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
/** Published card definitions from the combined Git and database catalogue. */
export type CatalogueCard = {
  __typename?: 'CatalogueCard';
  cardScript: Scalars['JSON']['output'];
  createdBy?: Maybe<Scalars['String']['output']>;
  id: Scalars['String']['output'];
  lastModified?: Maybe<Scalars['String']['output']>;
};

export type Class = {
  __typename?: 'Class';
  cardScript?: Maybe<Scalars['JSON']['output']>;
  class?: Maybe<Scalars['String']['output']>;
  collectible?: Maybe<Scalars['Boolean']['output']>;
  createdBy?: Maybe<Scalars['String']['output']>;
  id?: Maybe<Scalars['String']['output']>;
  isPublished?: Maybe<Scalars['Boolean']['output']>;
  name?: Maybe<Scalars['String']['output']>;
};

/** A condition to be used against `Class` object types. All fields are tested for equality and combined with a logical ‘and.’ */
export type ClassCondition = {
  /** Checks for equality with the object’s `cardScript` field. */
  cardScript?: InputMaybe<Scalars['JSON']['input']>;
  /** Checks for equality with the object’s `class` field. */
  class?: InputMaybe<Scalars['String']['input']>;
  /** Checks for equality with the object’s `collectible` field. */
  collectible?: InputMaybe<Scalars['Boolean']['input']>;
  /** Checks for equality with the object’s `createdBy` field. */
  createdBy?: InputMaybe<Scalars['String']['input']>;
  /** Checks for equality with the object’s `id` field. */
  id?: InputMaybe<Scalars['String']['input']>;
  /** Checks for equality with the object’s `isPublished` field. */
  isPublished?: InputMaybe<Scalars['Boolean']['input']>;
  /** Checks for equality with the object’s `name` field. */
  name?: InputMaybe<Scalars['String']['input']>;
};

/** A filter to be used against `Class` object types. All fields are combined with a logical ‘and.’ */
export type ClassFilter = {
  /** Checks for all expressions in this list. */
  and?: InputMaybe<Array<ClassFilter>>;
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
  totalCount: Scalars['Int']['output'];
};

/** A `Class` edge in the connection. */
export type ClassesEdge = {
  __typename?: 'ClassesEdge';
  /** A cursor for use in pagination. */
  cursor?: Maybe<Scalars['Cursor']['output']>;
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
  graphQlUrl?: Maybe<Scalars['String']['output']>;
  keycloakAccountManagementUrl?: Maybe<Scalars['String']['output']>;
  keycloakResetPasswordUrl?: Maybe<Scalars['String']['output']>;
};

export type CollectionCard = {
  __typename?: 'CollectionCard';
  blocklyWorkspace?: Maybe<Scalars['JSON']['output']>;
  cardScript?: Maybe<Scalars['JSON']['output']>;
  class?: Maybe<Scalars['String']['output']>;
  collectible?: Maybe<Scalars['Boolean']['output']>;
  cost?: Maybe<Scalars['Int']['output']>;
  createdAt?: Maybe<Scalars['Datetime']['output']>;
  createdBy?: Maybe<Scalars['String']['output']>;
  id?: Maybe<Scalars['String']['output']>;
  lastModified?: Maybe<Scalars['Datetime']['output']>;
  name?: Maybe<Scalars['String']['output']>;
  searchMessage?: Maybe<Scalars['String']['output']>;
  type?: Maybe<Scalars['String']['output']>;
};

/**
 * A condition to be used against `CollectionCard` object types. All fields are
 * tested for equality and combined with a logical ‘and.’
 */
export type CollectionCardCondition = {
  /** Checks for equality with the object’s `blocklyWorkspace` field. */
  blocklyWorkspace?: InputMaybe<Scalars['JSON']['input']>;
  /** Checks for equality with the object’s `cardScript` field. */
  cardScript?: InputMaybe<Scalars['JSON']['input']>;
  /** Checks for equality with the object’s `class` field. */
  class?: InputMaybe<Scalars['String']['input']>;
  /** Checks for equality with the object’s `collectible` field. */
  collectible?: InputMaybe<Scalars['Boolean']['input']>;
  /** Checks for equality with the object’s `cost` field. */
  cost?: InputMaybe<Scalars['Int']['input']>;
  /** Checks for equality with the object’s `createdAt` field. */
  createdAt?: InputMaybe<Scalars['Datetime']['input']>;
  /** Checks for equality with the object’s `createdBy` field. */
  createdBy?: InputMaybe<Scalars['String']['input']>;
  /** Checks for equality with the object’s `id` field. */
  id?: InputMaybe<Scalars['String']['input']>;
  /** Checks for equality with the object’s `lastModified` field. */
  lastModified?: InputMaybe<Scalars['Datetime']['input']>;
  /** Checks for equality with the object’s `name` field. */
  name?: InputMaybe<Scalars['String']['input']>;
  /** Checks for equality with the object’s `searchMessage` field. */
  searchMessage?: InputMaybe<Scalars['String']['input']>;
  /** Checks for equality with the object’s `type` field. */
  type?: InputMaybe<Scalars['String']['input']>;
};

/** A filter to be used against `CollectionCard` object types. All fields are combined with a logical ‘and.’ */
export type CollectionCardFilter = {
  /** Checks for all expressions in this list. */
  and?: InputMaybe<Array<CollectionCardFilter>>;
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
  totalCount: Scalars['Int']['output'];
};

/** A `CollectionCard` edge in the connection. */
export type CollectionCardsEdge = {
  __typename?: 'CollectionCardsEdge';
  /** A cursor for use in pagination. */
  cursor?: Maybe<Scalars['Cursor']['output']>;
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
  decks?: InputMaybe<Scalars['Boolean']['input']>;
  email: Scalars['String']['input'];
  guest?: InputMaybe<Scalars['Boolean']['input']>;
  password: Scalars['String']['input'];
  username: Scalars['String']['input'];
};

/** All input for the create `Card` mutation. */
export type CreateCardInput = {
  /** The `Card` to be created by this mutation. */
  card: CardInput;
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']['input']>;
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
  clientMutationId?: Maybe<Scalars['String']['output']>;
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
  clientMutationId?: InputMaybe<Scalars['String']['input']>;
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
  clientMutationId?: Maybe<Scalars['String']['output']>;
  /** Reads a single `Deck` that is related to this `CardsInDeck`. */
  deckByDeckId?: Maybe<Deck>;
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
  clientMutationId?: InputMaybe<Scalars['String']['input']>;
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
  clientMutationId?: Maybe<Scalars['String']['output']>;
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
  cardIds?: InputMaybe<Array<InputMaybe<Scalars['String']['input']>>>;
  classHero?: InputMaybe<Scalars['String']['input']>;
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']['input']>;
  deckName?: InputMaybe<Scalars['String']['input']>;
  formatName?: InputMaybe<Scalars['String']['input']>;
};

/** The output of our `createDeckWithCards` mutation. */
export type CreateDeckWithCardsPayload = {
  __typename?: 'CreateDeckWithCardsPayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']['output']>;
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
  clientMutationId?: InputMaybe<Scalars['String']['input']>;
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
  clientMutationId?: Maybe<Scalars['String']['output']>;
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
  clientMutationId?: InputMaybe<Scalars['String']['input']>;
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
  clientMutationId?: Maybe<Scalars['String']['output']>;
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
  distinctFrom?: InputMaybe<Scalars['Datetime']['input']>;
  /** Equal to the specified value. */
  equalTo?: InputMaybe<Scalars['Datetime']['input']>;
  /** Greater than the specified value. */
  greaterThan?: InputMaybe<Scalars['Datetime']['input']>;
  /** Greater than or equal to the specified value. */
  greaterThanOrEqualTo?: InputMaybe<Scalars['Datetime']['input']>;
  /** Included in the specified list. */
  in?: InputMaybe<Array<Scalars['Datetime']['input']>>;
  /** Is null (if `true` is specified) or is not null (if `false` is specified). */
  isNull?: InputMaybe<Scalars['Boolean']['input']>;
  /** Less than the specified value. */
  lessThan?: InputMaybe<Scalars['Datetime']['input']>;
  /** Less than or equal to the specified value. */
  lessThanOrEqualTo?: InputMaybe<Scalars['Datetime']['input']>;
  /** Equal to the specified value, treating null like an ordinary value. */
  notDistinctFrom?: InputMaybe<Scalars['Datetime']['input']>;
  /** Not equal to the specified value. */
  notEqualTo?: InputMaybe<Scalars['Datetime']['input']>;
  /** Not included in the specified list. */
  notIn?: InputMaybe<Array<Scalars['Datetime']['input']>>;
};

export type Deck = Node & {
  __typename?: 'Deck';
  /** Reads and enables pagination through a set of `CardsInDeck`. */
  cardsInDecksByDeckId: CardsInDecksConnection;
  /** who created this deck originally */
  createdBy: Scalars['String']['output'];
  /** Reads and enables pagination through a set of `DeckShare`. */
  deckSharesByDeckId: DeckSharesConnection;
  deckType: Scalars['Int']['output'];
  format?: Maybe<Scalars['String']['output']>;
  heroClass?: Maybe<Scalars['String']['output']>;
  id: Scalars['String']['output'];
  /** premades always shared with all users by application logic */
  isPremade: Scalars['Boolean']['output'];
  /** who last edited this deck */
  lastEditedBy: Scalars['String']['output'];
  name?: Maybe<Scalars['String']['output']>;
  /** A globally unique identifier. Can be used in various places throughout the system to identify this single value. */
  nodeId: Scalars['ID']['output'];
  permittedToDuplicate: Scalars['Boolean']['output'];
  /** Reads a single `RogueRun` that is related to this `Deck`. */
  rogueRunByDeck?: Maybe<RogueRun>;
  /** Reads and enables pagination through a set of `RogueRun`. */
  rogueRunsByOpponentDeck: RogueRunsConnection;
  trashed: Scalars['Boolean']['output'];
};


export type DeckCardsInDecksByDeckIdArgs = {
  after?: InputMaybe<Scalars['Cursor']['input']>;
  before?: InputMaybe<Scalars['Cursor']['input']>;
  condition?: InputMaybe<CardsInDeckCondition>;
  filter?: InputMaybe<CardsInDeckFilter>;
  first?: InputMaybe<Scalars['Int']['input']>;
  last?: InputMaybe<Scalars['Int']['input']>;
  offset?: InputMaybe<Scalars['Int']['input']>;
  orderBy?: InputMaybe<Array<CardsInDecksOrderBy>>;
};


export type DeckDeckSharesByDeckIdArgs = {
  after?: InputMaybe<Scalars['Cursor']['input']>;
  before?: InputMaybe<Scalars['Cursor']['input']>;
  condition?: InputMaybe<DeckShareCondition>;
  filter?: InputMaybe<DeckShareFilter>;
  first?: InputMaybe<Scalars['Int']['input']>;
  last?: InputMaybe<Scalars['Int']['input']>;
  offset?: InputMaybe<Scalars['Int']['input']>;
  orderBy?: InputMaybe<Array<DeckSharesOrderBy>>;
};


export type DeckRogueRunsByOpponentDeckArgs = {
  after?: InputMaybe<Scalars['Cursor']['input']>;
  before?: InputMaybe<Scalars['Cursor']['input']>;
  condition?: InputMaybe<RogueRunCondition>;
  filter?: InputMaybe<RogueRunFilter>;
  first?: InputMaybe<Scalars['Int']['input']>;
  last?: InputMaybe<Scalars['Int']['input']>;
  offset?: InputMaybe<Scalars['Int']['input']>;
  orderBy?: InputMaybe<Array<RogueRunsOrderBy>>;
};

/** A condition to be used against `Deck` object types. All fields are tested for equality and combined with a logical ‘and.’ */
export type DeckCondition = {
  /** Checks for equality with the object’s `createdBy` field. */
  createdBy?: InputMaybe<Scalars['String']['input']>;
  /** Checks for equality with the object’s `deckType` field. */
  deckType?: InputMaybe<Scalars['Int']['input']>;
  /** Checks for equality with the object’s `format` field. */
  format?: InputMaybe<Scalars['String']['input']>;
  /** Checks for equality with the object’s `heroClass` field. */
  heroClass?: InputMaybe<Scalars['String']['input']>;
  /** Checks for equality with the object’s `id` field. */
  id?: InputMaybe<Scalars['String']['input']>;
  /** Checks for equality with the object’s `isPremade` field. */
  isPremade?: InputMaybe<Scalars['Boolean']['input']>;
  /** Checks for equality with the object’s `lastEditedBy` field. */
  lastEditedBy?: InputMaybe<Scalars['String']['input']>;
  /** Checks for equality with the object’s `name` field. */
  name?: InputMaybe<Scalars['String']['input']>;
  /** Checks for equality with the object’s `permittedToDuplicate` field. */
  permittedToDuplicate?: InputMaybe<Scalars['Boolean']['input']>;
  /** Checks for equality with the object’s `trashed` field. */
  trashed?: InputMaybe<Scalars['Boolean']['input']>;
};

/** A filter to be used against `Deck` object types. All fields are combined with a logical ‘and.’ */
export type DeckFilter = {
  /** Checks for all expressions in this list. */
  and?: InputMaybe<Array<DeckFilter>>;
  /** Filter by the object’s `cardsInDecksByDeckId` relation. */
  cardsInDecksByDeckId?: InputMaybe<DeckToManyCardsInDeckFilter>;
  /** Some related `cardsInDecksByDeckId` exist. */
  cardsInDecksByDeckIdExist?: InputMaybe<Scalars['Boolean']['input']>;
  /** Filter by the object’s `createdBy` field. */
  createdBy?: InputMaybe<StringFilter>;
  /** Filter by the object’s `deckSharesByDeckId` relation. */
  deckSharesByDeckId?: InputMaybe<DeckToManyDeckShareFilter>;
  /** Some related `deckSharesByDeckId` exist. */
  deckSharesByDeckIdExist?: InputMaybe<Scalars['Boolean']['input']>;
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
  rogueRunByDeckExists?: InputMaybe<Scalars['Boolean']['input']>;
  /** Filter by the object’s `rogueRunsByOpponentDeck` relation. */
  rogueRunsByOpponentDeck?: InputMaybe<DeckToManyRogueRunFilter>;
  /** Some related `rogueRunsByOpponentDeck` exist. */
  rogueRunsByOpponentDeckExist?: InputMaybe<Scalars['Boolean']['input']>;
  /** Filter by the object’s `trashed` field. */
  trashed?: InputMaybe<BooleanFilter>;
};

/** An input for mutations affecting `Deck` */
export type DeckInput = {
  /** who created this deck originally */
  createdBy: Scalars['String']['input'];
  deckType: Scalars['Int']['input'];
  format?: InputMaybe<Scalars['String']['input']>;
  heroClass?: InputMaybe<Scalars['String']['input']>;
  id: Scalars['String']['input'];
  /** premades always shared with all users by application logic */
  isPremade?: InputMaybe<Scalars['Boolean']['input']>;
  /** who last edited this deck */
  lastEditedBy: Scalars['String']['input'];
  name?: InputMaybe<Scalars['String']['input']>;
  permittedToDuplicate?: InputMaybe<Scalars['Boolean']['input']>;
  trashed?: InputMaybe<Scalars['Boolean']['input']>;
};

/** Represents an update to a `Deck`. Fields that are set will be updated. */
export type DeckPatch = {
  /** who created this deck originally */
  createdBy?: InputMaybe<Scalars['String']['input']>;
  deckType?: InputMaybe<Scalars['Int']['input']>;
  format?: InputMaybe<Scalars['String']['input']>;
  heroClass?: InputMaybe<Scalars['String']['input']>;
  id?: InputMaybe<Scalars['String']['input']>;
  /** premades always shared with all users by application logic */
  isPremade?: InputMaybe<Scalars['Boolean']['input']>;
  /** who last edited this deck */
  lastEditedBy?: InputMaybe<Scalars['String']['input']>;
  name?: InputMaybe<Scalars['String']['input']>;
  permittedToDuplicate?: InputMaybe<Scalars['Boolean']['input']>;
  trashed?: InputMaybe<Scalars['Boolean']['input']>;
};

export type DeckShare = Node & {
  __typename?: 'DeckShare';
  /** Reads a single `Deck` that is related to this `DeckShare`. */
  deckByDeckId?: Maybe<Deck>;
  deckId: Scalars['String']['output'];
  /** A globally unique identifier. Can be used in various places throughout the system to identify this single value. */
  nodeId: Scalars['ID']['output'];
  shareRecipientId: Scalars['String']['output'];
  trashedByRecipient: Scalars['Boolean']['output'];
};

/**
 * A condition to be used against `DeckShare` object types. All fields are tested
 * for equality and combined with a logical ‘and.’
 */
export type DeckShareCondition = {
  /** Checks for equality with the object’s `deckId` field. */
  deckId?: InputMaybe<Scalars['String']['input']>;
  /** Checks for equality with the object’s `shareRecipientId` field. */
  shareRecipientId?: InputMaybe<Scalars['String']['input']>;
  /** Checks for equality with the object’s `trashedByRecipient` field. */
  trashedByRecipient?: InputMaybe<Scalars['Boolean']['input']>;
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
  totalCount: Scalars['Int']['output'];
};

/** A `DeckShare` edge in the connection. */
export type DeckSharesEdge = {
  __typename?: 'DeckSharesEdge';
  /** A cursor for use in pagination. */
  cursor?: Maybe<Scalars['Cursor']['output']>;
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
  totalCount: Scalars['Int']['output'];
};

/** A `Deck` edge in the connection. */
export type DecksEdge = {
  __typename?: 'DecksEdge';
  /** A cursor for use in pagination. */
  cursor?: Maybe<Scalars['Cursor']['output']>;
  /** The `Deck` at the end of the edge. */
  node?: Maybe<Deck>;
};

export type DecksGetResponse = {
  __typename?: 'DecksGetResponse';
  collection?: Maybe<InventoryCollection>;
  inventoryIdsSize: Scalars['Int']['output'];
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
  cardIds?: InputMaybe<Array<Scalars['String']['input']>>;
  deckList?: InputMaybe<Scalars['String']['input']>;
  format?: InputMaybe<Scalars['String']['input']>;
  heroClass: Scalars['String']['input'];
  name: Scalars['String']['input'];
};

export type DecksPutResponse = {
  __typename?: 'DecksPutResponse';
  collection?: Maybe<InventoryCollection>;
  deckId: Scalars['String']['output'];
};

export type DecksUpdateInput = {
  deckId: Scalars['String']['input'];
  pullAllCardIds?: InputMaybe<Array<Scalars['String']['input']>>;
  pushCardIds?: InputMaybe<Array<Scalars['String']['input']>>;
  setHeroClass?: InputMaybe<Scalars['String']['input']>;
  setName?: InputMaybe<Scalars['String']['input']>;
  setPlayerEntityAttribute?: InputMaybe<AttributeValueInput>;
  unsetPlayerEntityAttribute?: InputMaybe<Scalars['String']['input']>;
};

/** All input for the `deleteCardsInDeckById` mutation. */
export type DeleteCardsInDeckByIdInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']['input']>;
  id: Scalars['BigInt']['input'];
};

/** All input for the `deleteCardsInDeck` mutation. */
export type DeleteCardsInDeckInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']['input']>;
  /** The globally unique `ID` which will identify a single `CardsInDeck` to be deleted. */
  nodeId: Scalars['ID']['input'];
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
  clientMutationId?: Maybe<Scalars['String']['output']>;
  /** Reads a single `Deck` that is related to this `CardsInDeck`. */
  deckByDeckId?: Maybe<Deck>;
  deletedCardsInDeckId?: Maybe<Scalars['ID']['output']>;
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
  clientMutationId?: InputMaybe<Scalars['String']['input']>;
  id: Scalars['String']['input'];
};

/** All input for the `deletePublishedCard` mutation. */
export type DeletePublishedCardInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']['input']>;
  /** The globally unique `ID` which will identify a single `PublishedCard` to be deleted. */
  nodeId: Scalars['ID']['input'];
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
  clientMutationId?: Maybe<Scalars['String']['output']>;
  deletedPublishedCardId?: Maybe<Scalars['ID']['output']>;
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
  cardsRemaining: Scalars['Int']['output'];
  currentCardChoices: Array<Entity>;
  deckId: Scalars['String']['output'];
  draftIndex: Scalars['Int']['output'];
  heroClass?: Maybe<Entity>;
  heroClassChoices: Array<Entity>;
  losses: Scalars['Int']['output'];
  selectedCardIds: Array<Scalars['String']['output']>;
  status: DraftStatus;
  wins: Scalars['Int']['output'];
};

export const DraftStatus = {
  Complete: 'COMPLETE',
  InProgress: 'IN_PROGRESS',
  Retired: 'RETIRED',
  SelectHero: 'SELECT_HERO'
} as const;

export type DraftStatus = typeof DraftStatus[keyof typeof DraftStatus];
export type DraftsPostInput = {
  retireEarly?: InputMaybe<Scalars['Boolean']['input']>;
  startDraft?: InputMaybe<Scalars['Boolean']['input']>;
};

/** ─── Editable card types ───────────────────────────────────── */
export type EditableCard = {
  __typename?: 'EditableCard';
  id: Scalars['String']['output'];
  ownerUserId: Scalars['String']['output'];
  source: Scalars['String']['output'];
};

export type Emote = {
  __typename?: 'Emote';
  entityId: Scalars['Int']['output'];
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
  clientMutationId?: InputMaybe<Scalars['String']['input']>;
  rogueId?: InputMaybe<Scalars['BigInt']['input']>;
};

/** The output of our `endRogueRun` mutation. */
export type EndRogueRunPayload = {
  __typename?: 'EndRogueRunPayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']['output']>;
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
  armor?: Maybe<Scalars['Int']['output']>;
  art?: Maybe<Art>;
  /** stats */
  attack?: Maybe<Scalars['Int']['output']>;
  baseAttack?: Maybe<Scalars['Int']['output']>;
  baseHp?: Maybe<Scalars['Int']['output']>;
  baseManaCost?: Maybe<Scalars['Int']['output']>;
  /** boolean flags */
  battlecry: Scalars['Boolean']['output'];
  boardPosition: Scalars['Int']['output'];
  cannotAttack: Scalars['Boolean']['output'];
  cardId: Scalars['String']['output'];
  cardSet: Scalars['String']['output'];
  cardSets: Array<Scalars['String']['output']>;
  cardType: CardType;
  charge: Scalars['Boolean']['output'];
  /** misc */
  charges?: Maybe<Scalars['Int']['output']>;
  chooseOne: Scalars['Boolean']['output'];
  collectible: Scalars['Boolean']['output'];
  combo: Scalars['Boolean']['output'];
  conditionMet: Scalars['Boolean']['output'];
  countUntilCast?: Maybe<Scalars['Int']['output']>;
  deathrattles: Scalars['Boolean']['output'];
  deflect: Scalars['Boolean']['output'];
  description: Scalars['String']['output'];
  destroyed: Scalars['Boolean']['output'];
  discarded: Scalars['Boolean']['output'];
  divineShield: Scalars['Boolean']['output'];
  durability?: Maybe<Scalars['Int']['output']>;
  enchantmentType: Scalars['String']['output'];
  enraged: Scalars['Boolean']['output'];
  entityType: EntityType;
  extraAttack?: Maybe<Scalars['Int']['output']>;
  fires?: Maybe<Scalars['Int']['output']>;
  frozen: Scalars['Boolean']['output'];
  gameStarted: Scalars['Boolean']['output'];
  gold: Scalars['Boolean']['output'];
  heroClasses: Array<Scalars['String']['output']>;
  host: Scalars['Int']['output'];
  hostsTrigger: Scalars['Boolean']['output'];
  hp?: Maybe<Scalars['Int']['output']>;
  id: Scalars['Int']['output'];
  immune: Scalars['Boolean']['output'];
  isStartingTurn: Scalars['Boolean']['output'];
  lifesteal: Scalars['Boolean']['output'];
  location?: Maybe<EntityLocation>;
  lockedMana: Scalars['Int']['output'];
  /** player resource fields */
  mana: Scalars['Int']['output'];
  manaCost?: Maybe<Scalars['Int']['output']>;
  maxHp?: Maybe<Scalars['Int']['output']>;
  maxMana: Scalars['Int']['output'];
  name: Scalars['String']['output'];
  note: Scalars['String']['output'];
  overload?: Maybe<Scalars['Int']['output']>;
  owner: Scalars['Int']['output'];
  permanent: Scalars['Boolean']['output'];
  playable: Scalars['Boolean']['output'];
  poisonous: Scalars['Boolean']['output'];
  rarity: Rarity;
  roasted: Scalars['Boolean']['output'];
  rush: Scalars['Boolean']['output'];
  silenced: Scalars['Boolean']['output'];
  spellDamage?: Maybe<Scalars['Int']['output']>;
  stealth: Scalars['Boolean']['output'];
  summoningSickness: Scalars['Boolean']['output'];
  taunt: Scalars['Boolean']['output'];
  tooltips: Array<Tooltip>;
  tribes: Array<Scalars['String']['output']>;
  uncensored: Scalars['Boolean']['output'];
  underAura: Scalars['Boolean']['output'];
  untargetableBySpells: Scalars['Boolean']['output'];
  windfury: Scalars['Boolean']['output'];
};

/** ─── Core game entity types ────────────────────────────────── */
export type EntityLocation = {
  __typename?: 'EntityLocation';
  index: Scalars['Int']['output'];
  player: Scalars['Int']['output'];
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
  friendId: Scalars['String']['output'];
  friendName: Scalars['String']['output'];
  presence: Presence;
  since: Scalars['BigInt']['output'];
};

export type GameActions = {
  __typename?: 'GameActions';
  all: Array<SpellAction>;
  compatibility: Array<Scalars['Int']['output']>;
};

export type GameEvent = {
  __typename?: 'GameEvent';
  cardEvent?: Maybe<CardEvent>;
  description: Scalars['String']['output'];
  entityTouched?: Maybe<Scalars['Int']['output']>;
  entityUntouched?: Maybe<Scalars['Int']['output']>;
  eventType: GameEventType;
  id: Scalars['Int']['output'];
  isPowerHistory: Scalars['Boolean']['output'];
  isSourcePlayerLocal: Scalars['Boolean']['output'];
  isTargetPlayerLocal: Scalars['Boolean']['output'];
  performedGameAction?: Maybe<PerformedGameActionEvent>;
  source?: Maybe<Entity>;
  target?: Maybe<Entity>;
  targets: Array<Entity>;
  value?: Maybe<Scalars['Int']['output']>;
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
  localPlayerWon: Scalars['Boolean']['output'];
  winningPlayerId?: Maybe<Scalars['Int']['output']>;
};

/** ─── Game record types ──────────────────────────────────────── */
export type GameRecord = {
  __typename?: 'GameRecord';
  completedAt: Scalars['BigInt']['output'];
  completedAtLocalized?: Maybe<Scalars['String']['output']>;
  isBotGame: Scalars['Boolean']['output'];
  playerNames: Array<Scalars['String']['output']>;
};

export type GameState = {
  __typename?: 'GameState';
  entities: Array<Entity>;
  hasPowerHistory: Scalars['Boolean']['output'];
  isLocalPlayerTurn: Scalars['Boolean']['output'];
  powerHistory?: Maybe<Array<GameEvent>>;
  timestamp: Scalars['BigInt']['output'];
  turnNumber: Scalars['Int']['output'];
  turnState: Scalars['String']['output'];
};

export type GeneratedArt = {
  __typename?: 'GeneratedArt';
  hash: Scalars['String']['output'];
  info?: Maybe<Scalars['JSON']['output']>;
  isArchived: Scalars['Boolean']['output'];
  owner: Scalars['String']['output'];
  urls: Array<Maybe<Scalars['String']['output']>>;
};

/**
 * A condition to be used against `GeneratedArt` object types. All fields are
 * tested for equality and combined with a logical ‘and.’
 */
export type GeneratedArtCondition = {
  /** Checks for equality with the object’s `hash` field. */
  hash?: InputMaybe<Scalars['String']['input']>;
  /** Checks for equality with the object’s `info` field. */
  info?: InputMaybe<Scalars['JSON']['input']>;
  /** Checks for equality with the object’s `isArchived` field. */
  isArchived?: InputMaybe<Scalars['Boolean']['input']>;
  /** Checks for equality with the object’s `owner` field. */
  owner?: InputMaybe<Scalars['String']['input']>;
  /** Checks for equality with the object’s `urls` field. */
  urls?: InputMaybe<Array<InputMaybe<Scalars['String']['input']>>>;
};

/** A filter to be used against `GeneratedArt` object types. All fields are combined with a logical ‘and.’ */
export type GeneratedArtFilter = {
  /** Checks for all expressions in this list. */
  and?: InputMaybe<Array<GeneratedArtFilter>>;
  /** Filter by the object’s `hash` field. */
  hash?: InputMaybe<StringFilter>;
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
  hash: Scalars['String']['input'];
  info?: InputMaybe<Scalars['JSON']['input']>;
  isArchived?: InputMaybe<Scalars['Boolean']['input']>;
  owner?: InputMaybe<Scalars['String']['input']>;
  urls: Array<InputMaybe<Scalars['String']['input']>>;
};

/** Represents an update to a `GeneratedArt`. Fields that are set will be updated. */
export type GeneratedArtPatch = {
  hash?: InputMaybe<Scalars['String']['input']>;
  info?: InputMaybe<Scalars['JSON']['input']>;
  isArchived?: InputMaybe<Scalars['Boolean']['input']>;
  owner?: InputMaybe<Scalars['String']['input']>;
  urls?: InputMaybe<Array<InputMaybe<Scalars['String']['input']>>>;
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
  totalCount: Scalars['Int']['output'];
};

/** A `GeneratedArt` edge in the connection. */
export type GeneratedArtsEdge = {
  __typename?: 'GeneratedArtsEdge';
  /** A cursor for use in pagination. */
  cursor?: Maybe<Scalars['Cursor']['output']>;
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
  cachedOk: Scalars['Boolean']['output'];
  cards: Array<CardRecord>;
  version: Scalars['String']['output'];
};

/** All input for the `getClasses` mutation. */
export type GetClassesInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']['input']>;
};

/** The output of our `getClasses` mutation. */
export type GetClassesPayload = {
  __typename?: 'GetClassesPayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']['output']>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
  results?: Maybe<Array<Maybe<GetClassesRecord>>>;
};

export type GetClassesRecord = {
  __typename?: 'GetClassesRecord';
  cardScript?: Maybe<Scalars['JSON']['output']>;
  class?: Maybe<Scalars['String']['output']>;
  collectible?: Maybe<Scalars['Boolean']['output']>;
  createdBy?: Maybe<Scalars['String']['output']>;
  id?: Maybe<Scalars['String']['output']>;
  isPublished?: Maybe<Scalars['Boolean']['output']>;
  name?: Maybe<Scalars['String']['output']>;
};

/** All input for the `getCollectionCards` mutation. */
export type GetCollectionCardsInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']['input']>;
};

/** The output of our `getCollectionCards` mutation. */
export type GetCollectionCardsPayload = {
  __typename?: 'GetCollectionCardsPayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']['output']>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
  results?: Maybe<Array<Maybe<GetCollectionCardsRecord>>>;
};

export type GetCollectionCardsRecord = {
  __typename?: 'GetCollectionCardsRecord';
  blocklyWorkspace?: Maybe<Scalars['JSON']['output']>;
  cardScript?: Maybe<Scalars['JSON']['output']>;
  class?: Maybe<Scalars['String']['output']>;
  collectible?: Maybe<Scalars['Boolean']['output']>;
  cost?: Maybe<Scalars['Int']['output']>;
  createdAt?: Maybe<Scalars['Datetime']['output']>;
  createdBy?: Maybe<Scalars['String']['output']>;
  id?: Maybe<Scalars['String']['output']>;
  lastModified?: Maybe<Scalars['Datetime']['output']>;
  name?: Maybe<Scalars['String']['output']>;
  searchMessage?: Maybe<Scalars['String']['output']>;
  type?: Maybe<Scalars['String']['output']>;
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
  distinctFrom?: InputMaybe<Scalars['Int']['input']>;
  /** Equal to the specified value. */
  equalTo?: InputMaybe<Scalars['Int']['input']>;
  /** Greater than the specified value. */
  greaterThan?: InputMaybe<Scalars['Int']['input']>;
  /** Greater than or equal to the specified value. */
  greaterThanOrEqualTo?: InputMaybe<Scalars['Int']['input']>;
  /** Included in the specified list. */
  in?: InputMaybe<Array<Scalars['Int']['input']>>;
  /** Is null (if `true` is specified) or is not null (if `false` is specified). */
  isNull?: InputMaybe<Scalars['Boolean']['input']>;
  /** Less than the specified value. */
  lessThan?: InputMaybe<Scalars['Int']['input']>;
  /** Less than or equal to the specified value. */
  lessThanOrEqualTo?: InputMaybe<Scalars['Int']['input']>;
  /** Equal to the specified value, treating null like an ordinary value. */
  notDistinctFrom?: InputMaybe<Scalars['Int']['input']>;
  /** Not equal to the specified value. */
  notEqualTo?: InputMaybe<Scalars['Int']['input']>;
  /** Not included in the specified list. */
  notIn?: InputMaybe<Array<Scalars['Int']['input']>>;
};

export type InventoryCollection = {
  __typename?: 'InventoryCollection';
  collectionType: CollectionType;
  deckType: DeckType;
  format: Scalars['String']['output'];
  heroClass: Scalars['String']['output'];
  id: Scalars['String']['output'];
  inventory: Array<CardRecord>;
  isStandardDeck: Scalars['Boolean']['output'];
  name: Scalars['String']['output'];
  playerEntityAttributes: Array<AttributeValueTuple>;
  userId?: Maybe<Scalars['String']['output']>;
  validationReport?: Maybe<ValidationReport>;
};

/** ─── Invite types ───────────────────────────────────────────── */
export type Invite = {
  __typename?: 'Invite';
  expiresAt?: Maybe<Scalars['BigInt']['output']>;
  friendId?: Maybe<Scalars['String']['output']>;
  fromName?: Maybe<Scalars['String']['output']>;
  fromUserId: Scalars['String']['output'];
  id: Scalars['String']['output'];
  message?: Maybe<Scalars['String']['output']>;
  queueId?: Maybe<Scalars['String']['output']>;
  status: InviteStatus;
  toName?: Maybe<Scalars['String']['output']>;
  toUserId: Scalars['String']['output'];
};

export type InvitePostInput = {
  deckId?: InputMaybe<Scalars['String']['input']>;
  friend?: InputMaybe<Scalars['Boolean']['input']>;
  message?: InputMaybe<Scalars['String']['input']>;
  queueId?: InputMaybe<Scalars['String']['input']>;
  toUserId?: InputMaybe<Scalars['String']['input']>;
  toUserNameWithToken?: InputMaybe<Scalars['String']['input']>;
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
export type LoginInput = {
  password: Scalars['String']['input'];
  usernameOrEmail: Scalars['String']['input'];
};

export type LoginOrCreateReply = {
  __typename?: 'LoginOrCreateReply';
  accessToken?: Maybe<AccessToken>;
  userEntity?: Maybe<UserEntity>;
};

export type MatchFound = {
  __typename?: 'MatchFound';
  gameId: Scalars['String']['output'];
  playerKey: Scalars['String']['output'];
  playerSecret: Scalars['String']['output'];
  url: Scalars['String']['output'];
};

export type MatchmakingEnqueueInput = {
  botDeckId?: InputMaybe<Scalars['String']['input']>;
  deckId: Scalars['String']['input'];
  queueId: Scalars['String']['input'];
};

export type MatchmakingQueue = {
  __typename?: 'MatchmakingQueue';
  description: Scalars['String']['output'];
  name: Scalars['String']['output'];
  queueId: Scalars['String']['output'];
  requires?: Maybe<MatchmakingQueueRequires>;
  tooltip: Scalars['String']['output'];
};

/** ─── Matchmaking types ──────────────────────────────────────── */
export type MatchmakingQueueRequires = {
  __typename?: 'MatchmakingQueueRequires';
  deck: Scalars['Boolean']['output'];
  deckIdChoices: Array<Scalars['String']['output']>;
  heroClass: Scalars['Boolean']['output'];
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
  cancelMatchmaking: Scalars['Boolean']['output'];
  changePassword: LoginOrCreateReply;
  /** Concede the current game. */
  concedeGame: Scalars['Boolean']['output'];
  /** Connect to an active game after opening the gameMessages subscription. */
  connectToGame: Scalars['Boolean']['output'];
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
  deleteCard: Scalars['Boolean']['output'];
  /** Deletes a single `CardsInDeck` using its globally unique id. */
  deleteCardsInDeck?: Maybe<DeleteCardsInDeckPayload>;
  /** Deletes a single `CardsInDeck` using a unique key. */
  deleteCardsInDeckById?: Maybe<DeleteCardsInDeckPayload>;
  deleteDeck: Scalars['Boolean']['output'];
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
  enqueueMatchmaking: Scalars['Boolean']['output'];
  /** Probe the game engine; a matching PINGPONG message arrives on gameMessages. */
  gameHeartbeat: Scalars['Boolean']['output'];
  getClasses?: Maybe<GetClassesPayload>;
  getCollectionCards?: Maybe<GetCollectionCardsPayload>;
  login: LoginOrCreateReply;
  makeRogueChoice: RogueRun;
  publishCard?: Maybe<PublishCardPayload>;
  /** cards (editable) */
  putCard: PutCardResult;
  removeFriend: Scalars['Boolean']['output'];
  requestPasswordResetEmail: Scalars['Boolean']['output'];
  reroll: RogueRun;
  saveCard?: Maybe<SaveCardPayload>;
  saveGeneratedArt?: Maybe<SaveGeneratedArtPayload>;
  /** Send an emote during a game. */
  sendEmote: Scalars['Boolean']['output'];
  /** Send a game action in response to an ON_REQUEST_ACTION message. */
  sendGameAction: Scalars['Boolean']['output'];
  /** invites */
  sendInvite: InviteResponse;
  /** Send a mulligan response in response to an ON_MULLIGAN message. */
  sendMulligan: Scalars['Boolean']['output'];
  setCardsInDeck?: Maybe<SetCardsInDeckPayload>;
  skipBoss: RogueRun;
  /** drafts */
  startOrModifyDraft: DraftState;
  /** rogue (existing) */
  startRogueRun: RogueRun;
  /** Notify the opponent that a local entity is being hovered. */
  touchEntity: Scalars['Boolean']['output'];
  trashCard: RogueRun;
  /** Notify the opponent that a local entity is no longer being hovered. */
  untouchEntity: Scalars['Boolean']['output'];
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
  friendId?: InputMaybe<Scalars['String']['input']>;
  usernameWithToken?: InputMaybe<Scalars['String']['input']>;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationArchiveCardArgs = {
  input: ArchiveCardInput;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationChangePasswordArgs = {
  newPassword: Scalars['String']['input'];
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationConnectToGameArgs = {
  connectionId?: InputMaybe<Scalars['String']['input']>;
  playerKey?: InputMaybe<Scalars['String']['input']>;
  playerSecret?: InputMaybe<Scalars['String']['input']>;
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
  editableCardId: Scalars['String']['input'];
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
  deckId: Scalars['String']['input'];
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationDeleteInviteArgs = {
  inviteId: Scalars['String']['input'];
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
  cardIndex: Scalars['Int']['input'];
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationDraftsChooseHeroArgs = {
  heroIndex: Scalars['Int']['input'];
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationDuplicateDeckArgs = {
  deckId: Scalars['String']['input'];
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
export type MutationGameHeartbeatArgs = {
  nonce: Scalars['String']['input'];
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
  choiceId: Scalars['BigInt']['input'];
  choices: Array<Scalars['Int']['input']>;
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
  friendId: Scalars['String']['input'];
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationRerollArgs = {
  choiceId: Scalars['BigInt']['input'];
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
  entityId: Scalars['Int']['input'];
  message: EmoteType;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationSendGameActionArgs = {
  actionIndex: Scalars['Int']['input'];
  repliesTo: Scalars['String']['input'];
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationSendInviteArgs = {
  input: InvitePostInput;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationSendMulliganArgs = {
  discardedCardIndices: Array<Scalars['Int']['input']>;
  repliesTo: Scalars['String']['input'];
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationSetCardsInDeckArgs = {
  input: SetCardsInDeckInput;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationSkipBossArgs = {
  rogueId: Scalars['BigInt']['input'];
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationStartOrModifyDraftArgs = {
  input: DraftsPostInput;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationStartRogueRunArgs = {
  heroClass: Scalars['String']['input'];
  seed?: InputMaybe<Scalars['BigInt']['input']>;
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationTouchEntityArgs = {
  entityId: Scalars['Int']['input'];
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationTrashCardArgs = {
  cardId: Scalars['String']['input'];
  rogueId: Scalars['BigInt']['input'];
};


/** ─── Mutation ───────────────────────────────────────────────── */
export type MutationUntouchEntityArgs = {
  entityId: Scalars['Int']['input'];
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
  cardId: Scalars['String']['input'];
  rogueId: Scalars['BigInt']['input'];
};

/** An object with a globally unique `ID`. */
export type Node = {
  /** A globally unique identifier. Can be used in various places throughout the system to identify this single value. */
  nodeId: Scalars['ID']['output'];
};

/** Information about pagination in a connection. */
export type PageInfo = {
  __typename?: 'PageInfo';
  /** When paginating forwards, the cursor to continue. */
  endCursor?: Maybe<Scalars['Cursor']['output']>;
  /** When paginating forwards, are there more items? */
  hasNextPage: Scalars['Boolean']['output'];
  /** When paginating backwards, are there more items? */
  hasPreviousPage: Scalars['Boolean']['output'];
  /** When paginating backwards, the cursor to continue. */
  startCursor?: Maybe<Scalars['Cursor']['output']>;
};

export type PerformedGameActionEvent = {
  __typename?: 'PerformedGameActionEvent';
  actionType: ActionType;
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
  cardId?: InputMaybe<Scalars['String']['input']>;
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']['input']>;
};

/** The output of our `publishCard` mutation. */
export type PublishCardPayload = {
  __typename?: 'PublishCardPayload';
  bigInt?: Maybe<Scalars['BigInt']['output']>;
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']['output']>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};

export type PublishedCard = Node & {
  __typename?: 'PublishedCard';
  /** Reads a single `Card` that is related to this `PublishedCard`. */
  cardBySuccession?: Maybe<Card>;
  id: Scalars['String']['output'];
  /** A globally unique identifier. Can be used in various places throughout the system to identify this single value. */
  nodeId: Scalars['ID']['output'];
  succession: Scalars['BigInt']['output'];
};

/**
 * A condition to be used against `PublishedCard` object types. All fields are
 * tested for equality and combined with a logical ‘and.’
 */
export type PublishedCardCondition = {
  /** Checks for equality with the object’s `id` field. */
  id?: InputMaybe<Scalars['String']['input']>;
  /** Checks for equality with the object’s `succession` field. */
  succession?: InputMaybe<Scalars['BigInt']['input']>;
};

/** A filter to be used against `PublishedCard` object types. All fields are combined with a logical ‘and.’ */
export type PublishedCardFilter = {
  /** Checks for all expressions in this list. */
  and?: InputMaybe<Array<PublishedCardFilter>>;
  /** Filter by the object’s `cardBySuccession` relation. */
  cardBySuccession?: InputMaybe<CardFilter>;
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
  id: Scalars['String']['input'];
  succession: Scalars['BigInt']['input'];
};

/** Represents an update to a `PublishedCard`. Fields that are set will be updated. */
export type PublishedCardPatch = {
  id?: InputMaybe<Scalars['String']['input']>;
  succession?: InputMaybe<Scalars['BigInt']['input']>;
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
  totalCount: Scalars['Int']['output'];
};

/** A `PublishedCard` edge in the connection. */
export type PublishedCardsEdge = {
  __typename?: 'PublishedCardsEdge';
  /** A cursor for use in pagination. */
  cursor?: Maybe<Scalars['Cursor']['output']>;
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
  draw?: InputMaybe<Scalars['Boolean']['input']>;
  editableCardId?: InputMaybe<Scalars['String']['input']>;
  source: Scalars['String']['input'];
};

export type PutCardResult = {
  __typename?: 'PutCardResult';
  cardId: Scalars['String']['output'];
  cardScriptErrors: Array<Scalars['String']['output']>;
  editableCardId: Scalars['String']['output'];
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
  canSeeDeck?: Maybe<Scalars['Boolean']['output']>;
  /** Reads a single `Card` using its globally unique `ID`. */
  card?: Maybe<Card>;
  /** Get a single `Card`. */
  cardBySuccession?: Maybe<Card>;
  cards: GetCardsResponse;
  cardsByUser: GetCardsResponse;
  /** Reads a single `CardsInDeck` using its globally unique `ID`. */
  cardsInDeck?: Maybe<CardsInDeck>;
  /** Get a single `CardsInDeck`. */
  cardsInDeckById?: Maybe<CardsInDeck>;
  /** cards */
  catalogueCards: Array<CatalogueCard>;
  configuration: ClientConfiguration;
  currentRogueChoice?: Maybe<RogueChoice>;
  /** rogue (existing) */
  currentRogueClasses: Array<Scalars['String']['output']>;
  currentRogueRun?: Maybe<RogueRun>;
  /** auth */
  currentUserId?: Maybe<Scalars['String']['output']>;
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
  gameRecordIds: Array<Scalars['String']['output']>;
  /** Get a single `GeneratedArt`. */
  generatedArtByHashAndOwner?: Maybe<GeneratedArt>;
  getLatestCard?: Maybe<Card>;
  getUserId?: Maybe<Scalars['String']['output']>;
  /** invites */
  invite: InviteResponse;
  invites: Array<Invite>;
  isInMatch?: Maybe<Scalars['String']['output']>;
  /** matchmaking */
  matchmakingQueues: Array<MatchmakingQueue>;
  /** Fetches an object given its globally unique `ID`. */
  node?: Maybe<Node>;
  /** The root query type must be a `Node` to work well with Relay 1 mutations. This just resolves to `query`. */
  nodeId: Scalars['ID']['output'];
  /** Reads a single `PublishedCard` using its globally unique `ID`. */
  publishedCard?: Maybe<PublishedCard>;
  /** Get a single `PublishedCard`. */
  publishedCardById?: Maybe<PublishedCard>;
  /**
   * Exposes the root query type nested one level down. This is helpful for Relay 1
   * which can only query top level fields if they are in a particular form.
   */
  query: Query;
  rerollCost: Scalars['Int']['output'];
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
  trashCardCost: Scalars['Int']['output'];
  upgradeCardCost: Scalars['Int']['output'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryAccountsArgs = {
  userIds: Array<Scalars['String']['input']>;
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryAllCardsArgs = {
  after?: InputMaybe<Scalars['Cursor']['input']>;
  before?: InputMaybe<Scalars['Cursor']['input']>;
  condition?: InputMaybe<CardCondition>;
  filter?: InputMaybe<CardFilter>;
  first?: InputMaybe<Scalars['Int']['input']>;
  includeArchived?: InputMaybe<IncludeArchivedOption>;
  last?: InputMaybe<Scalars['Int']['input']>;
  offset?: InputMaybe<Scalars['Int']['input']>;
  orderBy?: InputMaybe<Array<CardsOrderBy>>;
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryAllCardsInDecksArgs = {
  after?: InputMaybe<Scalars['Cursor']['input']>;
  before?: InputMaybe<Scalars['Cursor']['input']>;
  condition?: InputMaybe<CardsInDeckCondition>;
  filter?: InputMaybe<CardsInDeckFilter>;
  first?: InputMaybe<Scalars['Int']['input']>;
  last?: InputMaybe<Scalars['Int']['input']>;
  offset?: InputMaybe<Scalars['Int']['input']>;
  orderBy?: InputMaybe<Array<CardsInDecksOrderBy>>;
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryAllClassesArgs = {
  after?: InputMaybe<Scalars['Cursor']['input']>;
  before?: InputMaybe<Scalars['Cursor']['input']>;
  condition?: InputMaybe<ClassCondition>;
  filter?: InputMaybe<ClassFilter>;
  first?: InputMaybe<Scalars['Int']['input']>;
  last?: InputMaybe<Scalars['Int']['input']>;
  offset?: InputMaybe<Scalars['Int']['input']>;
  orderBy?: InputMaybe<Array<ClassesOrderBy>>;
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryAllCollectionCardsArgs = {
  after?: InputMaybe<Scalars['Cursor']['input']>;
  before?: InputMaybe<Scalars['Cursor']['input']>;
  condition?: InputMaybe<CollectionCardCondition>;
  filter?: InputMaybe<CollectionCardFilter>;
  first?: InputMaybe<Scalars['Int']['input']>;
  last?: InputMaybe<Scalars['Int']['input']>;
  offset?: InputMaybe<Scalars['Int']['input']>;
  orderBy?: InputMaybe<Array<CollectionCardsOrderBy>>;
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryAllDeckSharesArgs = {
  after?: InputMaybe<Scalars['Cursor']['input']>;
  before?: InputMaybe<Scalars['Cursor']['input']>;
  condition?: InputMaybe<DeckShareCondition>;
  filter?: InputMaybe<DeckShareFilter>;
  first?: InputMaybe<Scalars['Int']['input']>;
  last?: InputMaybe<Scalars['Int']['input']>;
  offset?: InputMaybe<Scalars['Int']['input']>;
  orderBy?: InputMaybe<Array<DeckSharesOrderBy>>;
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryAllDecksArgs = {
  after?: InputMaybe<Scalars['Cursor']['input']>;
  before?: InputMaybe<Scalars['Cursor']['input']>;
  condition?: InputMaybe<DeckCondition>;
  filter?: InputMaybe<DeckFilter>;
  first?: InputMaybe<Scalars['Int']['input']>;
  last?: InputMaybe<Scalars['Int']['input']>;
  offset?: InputMaybe<Scalars['Int']['input']>;
  orderBy?: InputMaybe<Array<DecksOrderBy>>;
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryAllGeneratedArtsArgs = {
  after?: InputMaybe<Scalars['Cursor']['input']>;
  before?: InputMaybe<Scalars['Cursor']['input']>;
  condition?: InputMaybe<GeneratedArtCondition>;
  filter?: InputMaybe<GeneratedArtFilter>;
  first?: InputMaybe<Scalars['Int']['input']>;
  includeArchived?: InputMaybe<IncludeArchivedOption>;
  last?: InputMaybe<Scalars['Int']['input']>;
  offset?: InputMaybe<Scalars['Int']['input']>;
  orderBy?: InputMaybe<Array<GeneratedArtsOrderBy>>;
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryAllPublishedCardsArgs = {
  after?: InputMaybe<Scalars['Cursor']['input']>;
  before?: InputMaybe<Scalars['Cursor']['input']>;
  condition?: InputMaybe<PublishedCardCondition>;
  filter?: InputMaybe<PublishedCardFilter>;
  first?: InputMaybe<Scalars['Int']['input']>;
  last?: InputMaybe<Scalars['Int']['input']>;
  offset?: InputMaybe<Scalars['Int']['input']>;
  orderBy?: InputMaybe<Array<PublishedCardsOrderBy>>;
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryAllRogueChoicesArgs = {
  after?: InputMaybe<Scalars['Cursor']['input']>;
  before?: InputMaybe<Scalars['Cursor']['input']>;
  condition?: InputMaybe<RogueChoiceCondition>;
  filter?: InputMaybe<RogueChoiceFilter>;
  first?: InputMaybe<Scalars['Int']['input']>;
  last?: InputMaybe<Scalars['Int']['input']>;
  offset?: InputMaybe<Scalars['Int']['input']>;
  orderBy?: InputMaybe<Array<RogueChoicesOrderBy>>;
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryAllRogueRunsArgs = {
  after?: InputMaybe<Scalars['Cursor']['input']>;
  before?: InputMaybe<Scalars['Cursor']['input']>;
  condition?: InputMaybe<RogueRunCondition>;
  filter?: InputMaybe<RogueRunFilter>;
  first?: InputMaybe<Scalars['Int']['input']>;
  last?: InputMaybe<Scalars['Int']['input']>;
  offset?: InputMaybe<Scalars['Int']['input']>;
  orderBy?: InputMaybe<Array<RogueRunsOrderBy>>;
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryCanSeeDeckArgs = {
  deck?: InputMaybe<DeckInput>;
  userId?: InputMaybe<Scalars['String']['input']>;
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryCardArgs = {
  nodeId: Scalars['ID']['input'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryCardBySuccessionArgs = {
  succession: Scalars['BigInt']['input'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryCardsArgs = {
  ifNoneMatch?: InputMaybe<Scalars['String']['input']>;
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryCardsByUserArgs = {
  ifNoneMatch?: InputMaybe<Scalars['String']['input']>;
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryCardsInDeckArgs = {
  nodeId: Scalars['ID']['input'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryCardsInDeckByIdArgs = {
  id: Scalars['BigInt']['input'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryCurrentRogueChoiceArgs = {
  rogueId?: InputMaybe<Scalars['BigInt']['input']>;
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryDeckArgs = {
  deckId: Scalars['String']['input'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryDeckByIdArgs = {
  id: Scalars['String']['input'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryDeckShareArgs = {
  nodeId: Scalars['ID']['input'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryDeckShareByDeckIdAndShareRecipientIdArgs = {
  deckId: Scalars['String']['input'];
  shareRecipientId: Scalars['String']['input'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryGameRecordArgs = {
  gameId: Scalars['String']['input'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryGeneratedArtByHashAndOwnerArgs = {
  hash: Scalars['String']['input'];
  owner: Scalars['String']['input'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryGetLatestCardArgs = {
  cardId?: InputMaybe<Scalars['String']['input']>;
  published?: InputMaybe<Scalars['Boolean']['input']>;
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryInviteArgs = {
  inviteId: Scalars['String']['input'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryNodeArgs = {
  nodeId: Scalars['ID']['input'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryPublishedCardArgs = {
  nodeId: Scalars['ID']['input'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryPublishedCardByIdArgs = {
  id: Scalars['String']['input'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryRerollCostArgs = {
  rogueId: Scalars['BigInt']['input'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryRogueChoiceArgs = {
  nodeId: Scalars['ID']['input'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryRogueChoiceByIdArgs = {
  id: Scalars['BigInt']['input'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryRogueRunArgs = {
  nodeId: Scalars['ID']['input'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryRogueRunByDeckArgs = {
  deck: Scalars['String']['input'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryRogueRunByGameArgs = {
  game: Scalars['BigInt']['input'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryRogueRunByIdArgs = {
  id: Scalars['BigInt']['input'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryTrashCardCostArgs = {
  cardId: Scalars['String']['input'];
  rogueId: Scalars['BigInt']['input'];
};


/** ─── Query ──────────────────────────────────────────────────── */
export type QueryUpgradeCardCostArgs = {
  cardId: Scalars['String']['input'];
  rogueId: Scalars['BigInt']['input'];
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
  canPick: Scalars['Int']['output'];
  canReroll: Scalars['Boolean']['output'];
  cards: Array<Maybe<Scalars['String']['output']>>;
  id: Scalars['BigInt']['output'];
  index: Scalars['Int']['output'];
  /** A globally unique identifier. Can be used in various places throughout the system to identify this single value. */
  nodeId: Scalars['ID']['output'];
  repopulate: Scalars['Boolean']['output'];
  rogueRun: Scalars['BigInt']['output'];
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
  canPick?: InputMaybe<Scalars['Int']['input']>;
  /** Checks for equality with the object’s `canReroll` field. */
  canReroll?: InputMaybe<Scalars['Boolean']['input']>;
  /** Checks for equality with the object’s `cards` field. */
  cards?: InputMaybe<Array<InputMaybe<Scalars['String']['input']>>>;
  /** Checks for equality with the object’s `id` field. */
  id?: InputMaybe<Scalars['BigInt']['input']>;
  /** Checks for equality with the object’s `index` field. */
  index?: InputMaybe<Scalars['Int']['input']>;
  /** Checks for equality with the object’s `repopulate` field. */
  repopulate?: InputMaybe<Scalars['Boolean']['input']>;
  /** Checks for equality with the object’s `rogueRun` field. */
  rogueRun?: InputMaybe<Scalars['BigInt']['input']>;
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
  isNull?: InputMaybe<Scalars['Boolean']['input']>;
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
  totalCount: Scalars['Int']['output'];
};

/** A `RogueChoice` edge in the connection. */
export type RogueChoicesEdge = {
  __typename?: 'RogueChoicesEdge';
  /** A cursor for use in pagination. */
  cursor?: Maybe<Scalars['Cursor']['output']>;
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
  bossesDefeated: Scalars['Int']['output'];
  currentChoice?: Maybe<RogueChoice>;
  deck: Scalars['String']['output'];
  /** Reads a single `Deck` that is related to this `RogueRun`. */
  deckByDeck?: Maybe<Deck>;
  /** Reads a single `Deck` that is related to this `RogueRun`. */
  deckByOpponentDeck?: Maybe<Deck>;
  endedAt?: Maybe<Scalars['Datetime']['output']>;
  game?: Maybe<Scalars['BigInt']['output']>;
  gold: Scalars['Int']['output'];
  heroClass: Scalars['String']['output'];
  id: Scalars['BigInt']['output'];
  lives: Scalars['Int']['output'];
  /** A globally unique identifier. Can be used in various places throughout the system to identify this single value. */
  nodeId: Scalars['ID']['output'];
  opponentDeck?: Maybe<Scalars['String']['output']>;
  opponentInfo?: Maybe<Scalars['String']['output']>;
  player: Scalars['String']['output'];
  rerollsThisRound: Scalars['Int']['output'];
  /** Reads and enables pagination through a set of `RogueChoice`. */
  rogueChoicesByRogueRun: RogueChoicesConnection;
  seed: Scalars['BigInt']['output'];
  seedState: Scalars['BigInt']['output'];
  startedAt: Scalars['Datetime']['output'];
  state: RogueRunState;
};


/** ─── Rogue types (existing) ────────────────────────────────── */
export type RogueRunRogueChoicesByRogueRunArgs = {
  after?: InputMaybe<Scalars['Cursor']['input']>;
  before?: InputMaybe<Scalars['Cursor']['input']>;
  condition?: InputMaybe<RogueChoiceCondition>;
  filter?: InputMaybe<RogueChoiceFilter>;
  first?: InputMaybe<Scalars['Int']['input']>;
  last?: InputMaybe<Scalars['Int']['input']>;
  offset?: InputMaybe<Scalars['Int']['input']>;
  orderBy?: InputMaybe<Array<RogueChoicesOrderBy>>;
};

/**
 * A condition to be used against `RogueRun` object types. All fields are tested
 * for equality and combined with a logical ‘and.’
 */
export type RogueRunCondition = {
  /** Checks for equality with the object’s `bossesDefeated` field. */
  bossesDefeated?: InputMaybe<Scalars['Int']['input']>;
  /** Checks for equality with the object’s `deck` field. */
  deck?: InputMaybe<Scalars['String']['input']>;
  /** Checks for equality with the object’s `endedAt` field. */
  endedAt?: InputMaybe<Scalars['Datetime']['input']>;
  /** Checks for equality with the object’s `game` field. */
  game?: InputMaybe<Scalars['BigInt']['input']>;
  /** Checks for equality with the object’s `gold` field. */
  gold?: InputMaybe<Scalars['Int']['input']>;
  /** Checks for equality with the object’s `heroClass` field. */
  heroClass?: InputMaybe<Scalars['String']['input']>;
  /** Checks for equality with the object’s `id` field. */
  id?: InputMaybe<Scalars['BigInt']['input']>;
  /** Checks for equality with the object’s `lives` field. */
  lives?: InputMaybe<Scalars['Int']['input']>;
  /** Checks for equality with the object’s `opponentDeck` field. */
  opponentDeck?: InputMaybe<Scalars['String']['input']>;
  /** Checks for equality with the object’s `player` field. */
  player?: InputMaybe<Scalars['String']['input']>;
  /** Checks for equality with the object’s `rerollsThisRound` field. */
  rerollsThisRound?: InputMaybe<Scalars['Int']['input']>;
  /** Checks for equality with the object’s `seed` field. */
  seed?: InputMaybe<Scalars['BigInt']['input']>;
  /** Checks for equality with the object’s `seedState` field. */
  seedState?: InputMaybe<Scalars['BigInt']['input']>;
  /** Checks for equality with the object’s `startedAt` field. */
  startedAt?: InputMaybe<Scalars['Datetime']['input']>;
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
  deckByOpponentDeckExists?: InputMaybe<Scalars['Boolean']['input']>;
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
  rogueChoicesByRogueRunExist?: InputMaybe<Scalars['Boolean']['input']>;
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
  isNull?: InputMaybe<Scalars['Boolean']['input']>;
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
  totalCount: Scalars['Int']['output'];
};

/** A `RogueRun` edge in the connection. */
export type RogueRunsEdge = {
  __typename?: 'RogueRunsEdge';
  /** A cursor for use in pagination. */
  cursor?: Maybe<Scalars['Cursor']['output']>;
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
  cardId?: InputMaybe<Scalars['String']['input']>;
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']['input']>;
  json?: InputMaybe<Scalars['JSON']['input']>;
  workspace?: InputMaybe<Scalars['JSON']['input']>;
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
  clientMutationId?: Maybe<Scalars['String']['output']>;
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
  clientMutationId?: InputMaybe<Scalars['String']['input']>;
  digest?: InputMaybe<Scalars['String']['input']>;
  extraInfo?: InputMaybe<Scalars['JSON']['input']>;
  links?: InputMaybe<Array<InputMaybe<Scalars['String']['input']>>>;
};

/** The output of our `saveGeneratedArt` mutation. */
export type SaveGeneratedArtPayload = {
  __typename?: 'SaveGeneratedArtPayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']['output']>;
  generatedArt?: Maybe<GeneratedArt>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};

/**
 * A message from the server during a game. The messageType field indicates which
 * fields are populated. Clients should switch on messageType to process the message.
 */
export type ServerGameMessage = {
  __typename?: 'ServerGameMessage';
  /** Present on ON_REQUEST_ACTION messages. The actions the player can take. */
  actions?: Maybe<GameActions>;
  /**
   * IDs of entities that changed in this message. Present on ON_UPDATE and
   * ON_REQUEST_ACTION messages. Clients use this to diff zones efficiently
   * instead of comparing the full entity list.
   */
  changedEntityIds?: Maybe<Array<Scalars['Int']['output']>>;
  /** Present on EMOTE messages. */
  emote?: Maybe<Emote>;
  /** Present on ON_GAME_EVENT messages. */
  event?: Maybe<GameEvent>;
  /** Present on ON_GAME_END messages. */
  gameOver?: Maybe<GameOver>;
  /** Present on ON_UPDATE and ON_REQUEST_ACTION messages. */
  gameState?: Maybe<GameState>;
  id?: Maybe<Scalars['String']['output']>;
  isReplayMessage: Scalars['Boolean']['output'];
  localPlayerId: Scalars['Int']['output'];
  messageType: MessageType;
  /** Present on ON_MULLIGAN messages. The starting cards to consider discarding. */
  startingCards?: Maybe<Array<Entity>>;
  /** Present on TIMER messages. */
  timers?: Maybe<Timers>;
};

/** All input for the `setCardsInDeck` mutation. */
export type SetCardsInDeckInput = {
  cardIds?: InputMaybe<Array<InputMaybe<Scalars['String']['input']>>>;
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']['input']>;
  deck?: InputMaybe<Scalars['String']['input']>;
};

/** The output of our `setCardsInDeck` mutation. */
export type SetCardsInDeckPayload = {
  __typename?: 'SetCardsInDeckPayload';
  cardsInDecks?: Maybe<Array<Maybe<CardsInDeck>>>;
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']['output']>;
  /** Our root query field type. Allows us to run any query from our mutation payload. */
  query?: Maybe<Query>;
};

export type SpellAction = {
  __typename?: 'SpellAction';
  action: Scalars['Int']['output'];
  actionType: ActionType;
  choices: Array<SpellAction>;
  description: Scalars['String']['output'];
  entity?: Maybe<Entity>;
  sourceId: Scalars['Int']['output'];
  targetKeyToActions: Array<TargetActionPair>;
};

/** A filter to be used against String fields. All fields are combined with a logical ‘and.’ */
export type StringFilter = {
  /** Not equal to the specified value, treating null like an ordinary value. */
  distinctFrom?: InputMaybe<Scalars['String']['input']>;
  /** Not equal to the specified value, treating null like an ordinary value (case-insensitive). */
  distinctFromInsensitive?: InputMaybe<Scalars['String']['input']>;
  /** Ends with the specified string (case-sensitive). */
  endsWith?: InputMaybe<Scalars['String']['input']>;
  /** Ends with the specified string (case-insensitive). */
  endsWithInsensitive?: InputMaybe<Scalars['String']['input']>;
  /** Equal to the specified value. */
  equalTo?: InputMaybe<Scalars['String']['input']>;
  /** Equal to the specified value (case-insensitive). */
  equalToInsensitive?: InputMaybe<Scalars['String']['input']>;
  /** Greater than the specified value. */
  greaterThan?: InputMaybe<Scalars['String']['input']>;
  /** Greater than the specified value (case-insensitive). */
  greaterThanInsensitive?: InputMaybe<Scalars['String']['input']>;
  /** Greater than or equal to the specified value. */
  greaterThanOrEqualTo?: InputMaybe<Scalars['String']['input']>;
  /** Greater than or equal to the specified value (case-insensitive). */
  greaterThanOrEqualToInsensitive?: InputMaybe<Scalars['String']['input']>;
  /** Included in the specified list. */
  in?: InputMaybe<Array<Scalars['String']['input']>>;
  /** Included in the specified list (case-insensitive). */
  inInsensitive?: InputMaybe<Array<Scalars['String']['input']>>;
  /** Contains the specified string (case-sensitive). */
  includes?: InputMaybe<Scalars['String']['input']>;
  /** Contains the specified string (case-insensitive). */
  includesInsensitive?: InputMaybe<Scalars['String']['input']>;
  /** Is null (if `true` is specified) or is not null (if `false` is specified). */
  isNull?: InputMaybe<Scalars['Boolean']['input']>;
  /** Less than the specified value. */
  lessThan?: InputMaybe<Scalars['String']['input']>;
  /** Less than the specified value (case-insensitive). */
  lessThanInsensitive?: InputMaybe<Scalars['String']['input']>;
  /** Less than or equal to the specified value. */
  lessThanOrEqualTo?: InputMaybe<Scalars['String']['input']>;
  /** Less than or equal to the specified value (case-insensitive). */
  lessThanOrEqualToInsensitive?: InputMaybe<Scalars['String']['input']>;
  /** Matches the specified pattern (case-sensitive). An underscore (_) matches any single character; a percent sign (%) matches any sequence of zero or more characters. */
  like?: InputMaybe<Scalars['String']['input']>;
  /** Matches the specified pattern (case-insensitive). An underscore (_) matches any single character; a percent sign (%) matches any sequence of zero or more characters. */
  likeInsensitive?: InputMaybe<Scalars['String']['input']>;
  /** Equal to the specified value, treating null like an ordinary value. */
  notDistinctFrom?: InputMaybe<Scalars['String']['input']>;
  /** Equal to the specified value, treating null like an ordinary value (case-insensitive). */
  notDistinctFromInsensitive?: InputMaybe<Scalars['String']['input']>;
  /** Does not end with the specified string (case-sensitive). */
  notEndsWith?: InputMaybe<Scalars['String']['input']>;
  /** Does not end with the specified string (case-insensitive). */
  notEndsWithInsensitive?: InputMaybe<Scalars['String']['input']>;
  /** Not equal to the specified value. */
  notEqualTo?: InputMaybe<Scalars['String']['input']>;
  /** Not equal to the specified value (case-insensitive). */
  notEqualToInsensitive?: InputMaybe<Scalars['String']['input']>;
  /** Not included in the specified list. */
  notIn?: InputMaybe<Array<Scalars['String']['input']>>;
  /** Not included in the specified list (case-insensitive). */
  notInInsensitive?: InputMaybe<Array<Scalars['String']['input']>>;
  /** Does not contain the specified string (case-sensitive). */
  notIncludes?: InputMaybe<Scalars['String']['input']>;
  /** Does not contain the specified string (case-insensitive). */
  notIncludesInsensitive?: InputMaybe<Scalars['String']['input']>;
  /** Does not match the specified pattern (case-sensitive). An underscore (_) matches any single character; a percent sign (%) matches any sequence of zero or more characters. */
  notLike?: InputMaybe<Scalars['String']['input']>;
  /** Does not match the specified pattern (case-insensitive). An underscore (_) matches any single character; a percent sign (%) matches any sequence of zero or more characters. */
  notLikeInsensitive?: InputMaybe<Scalars['String']['input']>;
  /** Does not start with the specified string (case-sensitive). */
  notStartsWith?: InputMaybe<Scalars['String']['input']>;
  /** Does not start with the specified string (case-insensitive). */
  notStartsWithInsensitive?: InputMaybe<Scalars['String']['input']>;
  /** Starts with the specified string (case-sensitive). */
  startsWith?: InputMaybe<Scalars['String']['input']>;
  /** Starts with the specified string (case-insensitive). */
  startsWithInsensitive?: InputMaybe<Scalars['String']['input']>;
};

/** A filter to be used against String List fields. All fields are combined with a logical ‘and.’ */
export type StringListFilter = {
  /** Any array item is equal to the specified value. */
  anyEqualTo?: InputMaybe<Scalars['String']['input']>;
  /** Any array item is greater than the specified value. */
  anyGreaterThan?: InputMaybe<Scalars['String']['input']>;
  /** Any array item is greater than or equal to the specified value. */
  anyGreaterThanOrEqualTo?: InputMaybe<Scalars['String']['input']>;
  /** Any array item is less than the specified value. */
  anyLessThan?: InputMaybe<Scalars['String']['input']>;
  /** Any array item is less than or equal to the specified value. */
  anyLessThanOrEqualTo?: InputMaybe<Scalars['String']['input']>;
  /** Any array item is not equal to the specified value. */
  anyNotEqualTo?: InputMaybe<Scalars['String']['input']>;
  /** Contained by the specified list of values. */
  containedBy?: InputMaybe<Array<InputMaybe<Scalars['String']['input']>>>;
  /** Contains the specified list of values. */
  contains?: InputMaybe<Array<InputMaybe<Scalars['String']['input']>>>;
  /** Not equal to the specified value, treating null like an ordinary value. */
  distinctFrom?: InputMaybe<Array<InputMaybe<Scalars['String']['input']>>>;
  /** Equal to the specified value. */
  equalTo?: InputMaybe<Array<InputMaybe<Scalars['String']['input']>>>;
  /** Greater than the specified value. */
  greaterThan?: InputMaybe<Array<InputMaybe<Scalars['String']['input']>>>;
  /** Greater than or equal to the specified value. */
  greaterThanOrEqualTo?: InputMaybe<Array<InputMaybe<Scalars['String']['input']>>>;
  /** Is null (if `true` is specified) or is not null (if `false` is specified). */
  isNull?: InputMaybe<Scalars['Boolean']['input']>;
  /** Less than the specified value. */
  lessThan?: InputMaybe<Array<InputMaybe<Scalars['String']['input']>>>;
  /** Less than or equal to the specified value. */
  lessThanOrEqualTo?: InputMaybe<Array<InputMaybe<Scalars['String']['input']>>>;
  /** Equal to the specified value, treating null like an ordinary value. */
  notDistinctFrom?: InputMaybe<Array<InputMaybe<Scalars['String']['input']>>>;
  /** Not equal to the specified value. */
  notEqualTo?: InputMaybe<Array<InputMaybe<Scalars['String']['input']>>>;
  /** Overlaps the specified list of values. */
  overlaps?: InputMaybe<Array<InputMaybe<Scalars['String']['input']>>>;
};

/** ─── Subscription ───────────────────────────────────────────── */
export type Subscription = {
  __typename?: 'Subscription';
  /** Emits when an editable card is added, changed, or removed. */
  editableCardUpdated: EditableCard;
  /** Emits friend list changes (added, removed, presence updates). */
  friendUpdated: Friend;
  /**
   * Streams game messages for the authenticated user's active game.
   * Open this subscription before calling connectToGame. The mutation waits until
   * this subscription is ready before initiating the game connection.
   * Emits ServerGameMessage for each game state change, action request,
   * mulligan, event, and game over.
   */
  gameMessages: ServerGameMessage;
  /** Emits when an invite is received or its status changes. */
  inviteUpdated: Invite;
  /** Emits when the current user is matched into a game. */
  matchFound: MatchFound;
};


/** ─── Subscription ───────────────────────────────────────────── */
export type SubscriptionGameMessagesArgs = {
  connectionId?: InputMaybe<Scalars['String']['input']>;
};

/** ─── Game types (real-time game state) ──────────────────────── */
export type TargetActionPair = {
  __typename?: 'TargetActionPair';
  action: Scalars['Int']['output'];
  friendlyBattlefieldIndex: Scalars['Int']['output'];
  target: Scalars['Int']['output'];
};

export type Timers = {
  __typename?: 'Timers';
  millisRemaining: Scalars['BigInt']['output'];
};

export type Tooltip = {
  __typename?: 'Tooltip';
  keywords: Array<Scalars['String']['output']>;
  text: Scalars['String']['output'];
};

/** All input for the `updateCardBySuccession` mutation. */
export type UpdateCardBySuccessionInput = {
  /** An object where the defined keys will be set on the `Card` being updated. */
  cardPatch: CardPatch;
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']['input']>;
  succession: Scalars['BigInt']['input'];
};

/** All input for the `updateCard` mutation. */
export type UpdateCardInput = {
  /** An object where the defined keys will be set on the `Card` being updated. */
  cardPatch: CardPatch;
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']['input']>;
  /** The globally unique `ID` which will identify a single `Card` to be updated. */
  nodeId: Scalars['ID']['input'];
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
  clientMutationId?: Maybe<Scalars['String']['output']>;
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
  clientMutationId?: InputMaybe<Scalars['String']['input']>;
  id: Scalars['BigInt']['input'];
};

/** All input for the `updateCardsInDeck` mutation. */
export type UpdateCardsInDeckInput = {
  /** An object where the defined keys will be set on the `CardsInDeck` being updated. */
  cardsInDeckPatch: CardsInDeckPatch;
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']['input']>;
  /** The globally unique `ID` which will identify a single `CardsInDeck` to be updated. */
  nodeId: Scalars['ID']['input'];
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
  clientMutationId?: Maybe<Scalars['String']['output']>;
  /** Reads a single `Deck` that is related to this `CardsInDeck`. */
  deckByDeckId?: Maybe<Deck>;
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
  clientMutationId?: InputMaybe<Scalars['String']['input']>;
  /** An object where the defined keys will be set on the `Deck` being updated. */
  deckPatch: DeckPatch;
  id: Scalars['String']['input'];
};

/** All input for the `updateDeck` mutation. */
export type UpdateDeckInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']['input']>;
  /** An object where the defined keys will be set on the `Deck` being updated. */
  deckPatch: DeckPatch;
  /** The globally unique `ID` which will identify a single `Deck` to be updated. */
  nodeId: Scalars['ID']['input'];
};

/** The output of our update `Deck` mutation. */
export type UpdateDeckPayload = {
  __typename?: 'UpdateDeckPayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']['output']>;
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
  clientMutationId?: InputMaybe<Scalars['String']['input']>;
  /** An object where the defined keys will be set on the `GeneratedArt` being updated. */
  generatedArtPatch: GeneratedArtPatch;
  hash: Scalars['String']['input'];
  owner: Scalars['String']['input'];
};

/** The output of our update `GeneratedArt` mutation. */
export type UpdateGeneratedArtPayload = {
  __typename?: 'UpdateGeneratedArtPayload';
  /**
   * The exact same `clientMutationId` that was provided in the mutation input,
   * unchanged and unused. May be used by a client to track mutations.
   */
  clientMutationId?: Maybe<Scalars['String']['output']>;
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
  clientMutationId?: InputMaybe<Scalars['String']['input']>;
  id: Scalars['String']['input'];
  /** An object where the defined keys will be set on the `PublishedCard` being updated. */
  publishedCardPatch: PublishedCardPatch;
};

/** All input for the `updatePublishedCard` mutation. */
export type UpdatePublishedCardInput = {
  /**
   * An arbitrary string value with no semantic meaning. Will be included in the
   * payload verbatim. May be used to track mutations by the client.
   */
  clientMutationId?: InputMaybe<Scalars['String']['input']>;
  /** The globally unique `ID` which will identify a single `PublishedCard` to be updated. */
  nodeId: Scalars['ID']['input'];
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
  clientMutationId?: Maybe<Scalars['String']['output']>;
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
  email: Scalars['String']['output'];
  id: Scalars['String']['output'];
  privacyToken: Scalars['String']['output'];
  username: Scalars['String']['output'];
};

export type ValidationReport = {
  __typename?: 'ValidationReport';
  errors: Array<Scalars['String']['output']>;
  valid: Scalars['Boolean']['output'];
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