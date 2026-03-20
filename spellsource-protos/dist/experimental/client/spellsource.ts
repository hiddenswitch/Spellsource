/* eslint-disable */
import { grpc } from "@improbable-eng/grpc-web";
import { BrowserHeaders } from "browser-headers";
import Long from "long";
import _m0 from "protobufjs/minimal";
import { Observable } from "rxjs";
import { share } from "rxjs/operators";
import { Empty } from "./google/protobuf/empty";
import { StringValue } from "./google/protobuf/wrappers";
import { AddedChangedRemoved } from "./reactive";

export const protobufPackage = "spellsource";

export interface ActionTypeMessage {}

export enum ActionTypeMessage_ActionType {
  SYSTEM = 0,
  END_TURN = 1,
  PHYSICAL_ATTACK = 2,
  SPELL = 3,
  SUMMON = 4,
  HERO_POWER = 5,
  BATTLECRY = 6,
  EQUIP_WEAPON = 7,
  DISCOVER = 8,
  HERO = 9,
  TAP = 10,
  UNRECOGNIZED = -1,
}

export interface CardTypeMessage {}

export enum CardTypeMessage_CardType {
  HERO = 0,
  MINION = 1,
  SPELL = 2,
  WEAPON = 3,
  HERO_POWER = 4,
  GROUP = 5,
  CHOOSE_ONE = 6,
  ENCHANTMENT = 7,
  CLASS = 8,
  FORMAT = 9,
  ROGUE_CHOICE = 10,
  UNRECOGNIZED = -1,
}

export interface DamageTypeMessage {}

export enum DamageTypeMessage_DamageType {
  PHYSICAL = 0,
  FATIGUE = 1,
  MAGICAL = 2,
  DECAY = 3,
  DEFLECT = 4,
  DRAIN = 5,
  IGNORES_ARMOR = 6,
  SPLASH = 7,
  UNRECOGNIZED = -1,
}

export interface EntityTypeMessage {}

export enum EntityTypeMessage_EntityType {
  ANY = 0,
  ACTOR = 1,
  HERO = 2,
  MINION = 3,
  WEAPON = 4,
  CARD = 5,
  PLAYER = 6,
  ENCHANTMENT = 7,
  QUEST = 8,
  SECRET = 9,
  UNRECOGNIZED = -1,
}

export interface MessageTypeMessage {}

export enum MessageTypeMessage_MessageType {
  UPDATE_ACTION = 0,
  ON_GAME_EVENT = 1,
  ON_GAME_END = 2,
  ON_UPDATE = 3,
  ON_REQUEST_ACTION = 4,
  FIRST_MESSAGE = 5,
  ON_MULLIGAN = 6,
  UPDATE_MULLIGAN = 7,
  EMOTE = 8,
  TOUCH = 9,
  CONCEDE = 10,
  PINGPONG = 11,
  TIMER = 12,
  UNRECOGNIZED = -1,
}

export interface PlayerEntityAttributesMessage {}

export enum PlayerEntityAttributesMessage_PlayerEntityAttributes {
  SIGNATURE = 0,
  UNRECOGNIZED = -1,
}

export interface PresenceMessage {}

export enum PresenceMessage_Presence {
  UNKNOWN = 0,
  OFFLINE = 1,
  IN_GAME = 2,
  ONLINE = 3,
  UNRECOGNIZED = -1,
}

export interface RarityMessage {}

export enum RarityMessage_Rarity {
  FREE = 0,
  COMMON = 1,
  RARE = 2,
  EPIC = 3,
  LEGENDARY = 4,
  ALLIANCE = 5,
  UNRECOGNIZED = -1,
}

/**
 * Zones describe the different locations for entities in the game.
 *
 * In a standard game, the local player can see their {@link #HAND}, their {@link #SECRET} zone, and both player's
 * {@link #BATTLEFIELD} zones. They know the count of the number of entities in the opponent's {@link #HAND}, opponent's
 * {@link #DECK}, opponent's {@link #SECRET} zone and their own {@link #DECK}. While neither player can browse through
 * the {@link #GRAVEYARD} the information inside of it is not considered secret.
 *
 * Many effects interact with zones in special ways. For example, a {@link GameLogic#summon(int, Minion, Entity, int,
 * boolean)} performs the consequences of playing a {@link Card}; the card is moved to the {@link #GRAVEYARD} and a new
 * {@link Minion} is created by {@link Card#minion()} and placed into the {@link #BATTLEFIELD}.
 */
export interface ZonesMessage {}

export enum ZonesMessage_Zones {
  /** NONE - This zone specifies the entity belongs to no zone or the zone is not yet assigned. */
  NONE = 0,
  /** HAND - This zone is a player's hand. Only cards can be in this zone. */
  HAND = 1,
  /** DECK - This zone is a player's deck. Only {@link Card} entities can be in this zone. */
  DECK = 2,
  /**
   * GRAVEYARD - The graveyard is where a {@link Card} has been played with {@link GameLogic#playCard(int, EntityReference,
   * EntityReference)} goes; and where an {@link Actor} that has been destroyed with {@link GameLogic#destroy(Actor...)}
   * goes. A {@link net.demilich.metastone.game.spells.trigger.secrets.Secret} and other entities subclassing {@link
   * Enchantment} go to {@link #REMOVED_FROM_PLAY}.
   */
  GRAVEYARD = 3,
  /** BATTLEFIELD - A {@link Minion} is typically summoned into this zone. Anything in this zone is targetable by physical attacks. */
  BATTLEFIELD = 4,
  /**
   * SECRET - This zone is where a {@link net.demilich.metastone.game.spells.trigger.secrets.Secret} entity goes. Its contents
   * are not visible to the opponent.
   */
  SECRET = 5,
  /**
   * QUEST - This zone is  where {@link net.demilich.metastone.game.spells.trigger.secrets.Quest} entities go, which behave like
   * secrets that are visible to the opponent and do not go away the first time they  are triggered.
   */
  QUEST = 6,
  /**
   * HERO_POWER - The hero power zone stores the hero power for a corresponding {@link net.demilich.metastone.game.entities.heroes.Hero}.
   * Only one such card can be in the zone at a time.
   */
  HERO_POWER = 7,
  /** HERO - The hero zone stores the {@link Hero} actor that represents a player's targetable avatar in the game. */
  HERO = 8,
  /**
   * WEAPON - The weapon zone stores the {@link net.demilich.metastone.game.entities.weapons.Weapon} that a {@link Hero} has
   * equipped.
   */
  WEAPON = 9,
  /**
   * SET_ASIDE_ZONE - The set aside zone holds an {@link Entity} existing in any intermediate or "not really on the board" state, like
   * the original minion after Recycle puts a new copy in the deck, the prior state of transformed minions and Lord
   * Jaraxxus the minion after his Battlecry occurs.
   *
   * Unlike the official game rules, the three cards presented to a player by Tracking go into the {@link #DISCOVER}
   * zone.
   */
  SET_ASIDE_ZONE = 10,
  HIDDEN = 11,
  /**
   * DISCOVER - The discover zone has any cards that are being currently chosen by the player as part of a {@link
   * net.demilich.metastone.game.actions.DiscoverAction}.
   *
   * The opposing player can see the count, but not the contents, of cards the player is choosing between.
   */
  DISCOVER = 12,
  /**
   * REMOVED_FROM_PLAY - An {@link Entity} in this zone is "deleted" in the sense that it will never appear in any {@link EntityFilter}
   * filters or targeting lists.
   */
  REMOVED_FROM_PLAY = 13,
  /**
   * PLAYER - Metastone originally used the same object for what is now the {@link Player} and {@link Hero} entity. Since the
   * {@link Player} is still targetable (primarily by special buffing spells), it needs a {@link Zones} zone to belong
   * to. This zone is the zone a {@link Player} entity belongs to.
   */
  PLAYER = 14,
  /**
   * ENCHANTMENT - The enchantment zone corresponds to the player's list of {@link Enchantment} entities in the {@link
   * GameContext#getTriggers()} list.
   */
  ENCHANTMENT = 15,
  UNRECOGNIZED = -1,
}

export interface GameEventTypeMessage {}

export enum GameEventTypeMessage_GameEventType {
  ALL = 0,
  AFTER_PHYSICAL_ATTACK = 1,
  AFTER_PLAY_CARD = 2,
  AFTER_SPELL_CASTED = 3,
  AFTER_SUMMON = 4,
  ATTRIBUTE_APPLIED = 5,
  ARMOR_GAINED = 6,
  BEFORE_PHYSICAL_ATTACK = 7,
  BEFORE_SUMMON = 8,
  BOARD_CHANGED = 9,
  CARD_ADDED_TO_DECK = 10,
  CARD_SHUFFLED = 11,
  DAMAGE = 12,
  DECAY = 13,
  DID_END_SEQUENCE = 14,
  DISCARD = 15,
  DISCOVER = 16,
  DRAIN = 17,
  DRAW_CARD = 18,
  ENRAGE_CHANGED = 19,
  ENTITY_TOUCHED = 20,
  ENTITY_UNTOUCHED = 21,
  EXCESS_HEAL = 22,
  FATIGUE = 23,
  GAME_START = 24,
  HEAL = 25,
  HERO_POWER_USED = 26,
  INVOKED = 27,
  JOUST = 28,
  KILL = 29,
  LOSE_DIVINE_SHIELD = 30,
  LOSE_DEFLECT = 31,
  LOSE_STEALTH = 32,
  MAX_HP_INCREASED = 33,
  MAX_MANA = 34,
  MANA_MODIFIED = 35,
  MISSILE_FIRED = 36,
  OVERLOAD = 37,
  PERFORMED_GAME_ACTION = 38,
  PHYSICAL_ATTACK = 39,
  PLAY_CARD = 40,
  PRE_DAMAGE = 41,
  PRE_GAME_START = 42,
  QUEST_PLAYED = 43,
  QUEST_SUCCESSFUL = 44,
  RETURNED_TO_HAND = 45,
  ROASTED = 46,
  REVEAL_CARD = 47,
  SECRET_PLAYED = 48,
  SECRET_REVEALED = 49,
  SPELL_CASTED = 50,
  SUMMON = 51,
  TARGET_ACQUISITION = 52,
  TRIGGER_FIRED = 53,
  TURN_END = 54,
  TURN_START = 55,
  SILENCE = 56,
  WEAPON_DESTROYED = 57,
  WEAPON_EQUIPPED = 58,
  WILL_END_SEQUENCE = 59,
  DESTROY_WILL_QUEUE = 60,
  TAP_ACTIVATED = 61,
  HONORABLE_KILL = 62,
  ROGUE_CHOICE = 63,
  GAME_INITIALIZED = 64,
  UNRECOGNIZED = -1,
}

/** Accepts an invite to a match or a friend invite. */
export interface AcceptInviteRequest {
  inviteId: string;
  /** When true, specifies that the method call should only return when the game is actually ready to join */
  awaitGameStart: boolean;
  match: MatchmakingQueuePutRequest | undefined;
}

export interface AcceptInviteResponse {
  friend: FriendPutResponse | undefined;
  invite: Invite | undefined;
  match: MatchmakingQueuePutResponse | undefined;
}

export interface Account {
  /** The user ID */
  Id: string;
  /** A list of decks belonging to the player */
  decks: InventoryCollection[];
  /** The user's email address */
  email: string;
  /**
   * The user's friends at the moment of receiving this account document. This may be out of date as the latest
   * friends information will come from receiving friend documents.
   */
  friends: Friend[];
  /** True if the client should attempt to connect to a match with its token. */
  inMatch: boolean;
  /** The username that is displayed to toher players */
  name: string;
  personalCollection: InventoryCollection | undefined;
  /** The token that is appended to the end of the user's name to allow friending without sharing an e-mail address. */
  privacyToken: string;
}

/** Contains information the client needs for an art asset. */
export interface Art {
  body: Font | undefined;
  highlight: Color | undefined;
  loop: Prefab | undefined;
  missile: Prefab | undefined;
  onCast: Prefab | undefined;
  onHit: Prefab | undefined;
  primary: Color | undefined;
  secondary: Color | undefined;
  shadow: Color | undefined;
  spell: Prefab | undefined;
  sprite: Sprite | undefined;
  spriteShadow: Sprite | undefined;
}

/** This stores extra human-annotated data for the GameStateValueBehaviour bot */
export interface ArtificialIntelligence {
  /** when true, the bot will treat this card as hard removal */
  hardRemoval: boolean;
}

/** Stores extra human-annotated data for draft (Arena) mode */
export interface Draft {
  banned: boolean;
}

/** A tuple of attribute, attribute-value. */
export interface AttributeValueTuple {
  attribute: PlayerEntityAttributesMessage_PlayerEntityAttributes;
  stringValue: string;
}

export interface CardEvent {
  card: Entity | undefined;
  /** Forces this card event to be shown locally */
  showLocal: boolean;
}

export interface CardRecord {
  id: number;
  allianceId: string;
  borrowedByUserId: string;
  collectionIds: string[];
  donorUserId: string;
  entity: Entity | undefined;
  userId: string;
  count: number;
}

export interface ChangePasswordRequest {
  /** The new password */
  password: string;
}

/** An empty response signifying your password was correctly changed */
export interface ChangePasswordResponse {}

/** A chat message. */
export interface ChatMessage {
  /**
   * A conversation ID looks like userId1,userId2 where the first user ID is the one that comes first
   * lexicographically.
   */
  conversationId: string;
  /** The text that should be used to render when this message was sent. */
  dateLabel: string;
  /** The contents of this message. */
  message: string;
  /** The ID of the message. */
  messageId: string;
  /** The text to render in the sender name field. */
  senderName: string;
  /** The user ID of the sender. Useful for looking up against the presence notifications. */
  senderUserId: string;
  /** The timestamp of the message. */
  timestamp: number;
  notification: AddedChangedRemoved | undefined;
}

export interface ClientToServerMessage {
  /** The index of the available actions to use. */
  actionIndex: number;
  /** The indices of cards to discard in a mulligan. */
  discardedCardIndices: number[];
  emote: Emote | undefined;
  /** When specified with an entity ID, indicates the client is "touching" this entity. */
  entityTouch?: number | undefined;
  /** When specified with an entity ID, indicates the client is no longer touching the specified entity. */
  entityUntouch?: number | undefined;
  firstMessage: ClientToServerMessage_FirstMessageMessage | undefined;
  messageType: MessageTypeMessage_MessageType;
  /** The ID of the server message this client message is replying to. */
  repliesTo: string;
}

export interface ClientToServerMessage_FirstMessageMessage {
  /** A key authenticating this connection. Used only for the first message. */
  playerKey: string;
  /** A server-signed secret that authenticates this player for this match. Used only for the first message. */
  playerSecret: string;
}

/** Describes a Unity color. */
export interface Color {
  /** Alpha in units of 0.0-1.0 */
  a: number;
  /** Blue channel in units of 0.0-1.0 */
  b: number;
  /** Green channel in units of 0.0-1.0 */
  g: number;
  /** Red channel in units of 0.0-1.0 */
  r: number;
}

export interface CreateAccountRequest {
  /** An email account for password resets. */
  email: string;
  /** A display name for the user. */
  name: string;
  /** A password used to login to Spellsource. */
  password: string;
}

export interface CreateAccountResponse {
  account: Account | undefined;
  /** A string containing the token to login with via the standard Spellsource authentication method. */
  loginToken: string;
}

export interface DecksDeleteRequest {
  /** The Deck ID to delete. */
  deckId: string;
}

export interface DecksGetAllResponse {
  decks: DecksGetResponse[];
}

export interface DecksGetRequest {
  /** The Deck ID to get. */
  deckId: string;
}

export interface DecksGetResponse {
  collection: InventoryCollection | undefined;
  /** The current number of cards in this deck. */
  inventoryIdsSize: number;
}

/**
 * This request allows a user to specify a decklist or deck properies for creating a new deck. Whenever a deck list
 * is specified (non-null and not equal to the empty string), the deck list will be preferred. Decks created without
 * a deck list may have no properties specified, and the deck will still be successfully created.
 */
export interface DecksPutRequest {
  /** A community-standard decklist. */
  deckList: string;
  /**
   * The format of this deck. Format specifies which cards are allowable in this deck for validation. It also
   * specifies which cards will appear in discovers during matchmaking.
   *
   * Currenly, matchmaking occurs between decks of all formats, regardless of your choice of format. The smallest
   * possible format encompassing both decks in a match is selected when the formats of the decks do not match.
   *
   * Certain queues only support certain formats. Typically, when requesting the listing of queues with
   * matchmakingGet, the queues will specify which current decks can be chosen.
   */
  format: string;
  /**
   * A valid hero class for creating the deck. The appropriate hero card will be chosen for this deck unless
   * otherwise specified.
   */
  heroClass: string;
  /** @deprecated */
  inventoryIds: string[];
  /**
   * The name of the deck as it will appear in the collections view. Typically, your opponent will not be able to
   * see this name.
   *
   * Some custom cards interact with specific named decks in your collection. For those purposes, the deck names
   * are case sensitive. When multiple decks share a name, one will be chosen arbitrarily (not at random).
   */
  name: string;
  cardIds: string[];
}

/** The deck that was created by a deck put request. */
export interface DecksPutResponse {
  collection: InventoryCollection | undefined;
  deckId: string;
}

/**
 * This command contains a variable number of changes to apply to a deck. Whenever multiple fields are set, the
 * server will try to resolve their effects in the least surprising way possible.
 *
 * Specifically, if setInventoryIds is set, it will override all other changes to the inventory. Otherwise, removals
 * will be evaluated first, preferring inventory ID removals over card ID removals, followed by adds.
 */
export interface DecksUpdateCommand {
  /**
   * Removes all the specified card IDs from the user's deck. Does nothing if the deck does not contain any of the
   * specified card IDs. This method will still succeed for deck IDs that are found.
   */
  pullAllCardIds: string[];
  /**
   * Removes all the specified inventory IDs from the user's deck. Does nothing if the deck does not contain any of
   * the specified inventory IDs. This method will still succeed for inventory IDs that are found.
   */
  pullAllInventoryIds: number[];
  /**
   * Adds the specified card IDs to the deck with this command. If the player doesn't own the card IDs, the current
   * Spellsource inventory rules will grant the cards to the user. Duplicates are allowed. Under standard rules,
   * the deck becomes invalid if the number of duplicates exceeds 2; or, if the hero class isn't neutral or the
   * same as the deck's hero class.
   */
  pushCardIds: DecksUpdateCommand_PushCardIdsMessage | undefined;
  /**
   * Adds the specified inventory IDs to the deck in this command. Duplicate inventory IDs will cause the update to
   * be rejected. If the user does not own these inventory IDs, the deck becomes invalid. Under standard rules,
   * duplicate card IDs also make the deck invalid. Finally, adding cards whose hero class isn't neutral or the
   * same as the deck's hero class marks the deck as invalid.
   */
  pushInventoryIds: DecksUpdateCommand_PushInventoryIdsMessage | undefined;
  /**
   * Sets the hero class of the deck in this command. If the deck now contains cards that no longer belong to this
   * hero class, the deck becomes invalid under standard rules.
   */
  setHeroClass: string;
  /**
   * Sets the entire deck's inventory IDs in this command. Duplicate inventory IDs will cause the update to
   * be rejected. If the user does not own these inventory IDs, the deck becomes invalid. Under standard rules,
   * duplicate card IDs also make the deck invalid. Finally, adding cards whose hero class isn't neutral or the
   * same as the deck's hero class marks the deck as invalid.
   */
  setInventoryIds: number[];
  /** Sets the name of the deck in this command. If the name is null, the deck becomes invalid. */
  setName: string;
  /** Sets a player entity attribute in CAMEL_CASE, like SIGNATURE. */
  setPlayerEntityAttribute: DecksUpdateCommand_SetPlayerEntityAttributeMessage | undefined;
  /** Unsets (clears) the player entity attribute specified here. */
  unsetPlayerEntityAttribute: string;
}

export interface DecksUpdateCommand_PushCardIdsMessage {
  /** The items in this array specify which card IDs should be added. */
  Each: string[];
}

export interface DecksUpdateCommand_PushInventoryIdsMessage {
  /** The items in this array specify which inventory IDs should be added. */
  Each: string[];
}

export interface DecksUpdateCommand_SetPlayerEntityAttributeMessage {
  attribute: PlayerEntityAttributesMessage_PlayerEntityAttributes;
  /** The string value of the attribute. */
  stringValue: string;
}

export interface DecksUpdateRequest {
  /** The Deck ID to update. */
  deckId: string;
  /** An update command modifying specified properties of the deck. */
  updateCommand: DecksUpdateCommand | undefined;
}

/** Indicates a default, successful response. */
export interface DefaultMethodResponse {}

export interface DeleteInviteRequest {
  inviteId: string;
}

/** Contains information about an actor being destroyed. */
export interface Destroy {
  /** Aftermaths that will fire due to this actor being destroyed. */
  aftermaths: Entity[];
  source: Entity | undefined;
  target: Entity | undefined;
}

/**
 * Describes the current state of a draft, including the deck in progress, the new choices,
 * and the hero choices.
 */
export interface DraftState {
  /** Gets the number of card choices remaining to make. */
  cardsRemaining: number;
  /** When not null, contains the cards that correspond to your choices for the next draft selection. */
  currentCardChoices: Entity[];
  /** The deck that corresponds to your finished draft deck. */
  deckId: string;
  /** Gets the current draft index. */
  draftIndex: number;
  heroClass: Entity | undefined;
  /** When not null, contains three choices you should reply with to choose the hero of your draft. */
  heroClassChoices: Entity[];
  /** The number of losses you have suffered with your current draft deck. */
  losses: number;
  selectedCardIds: string[];
  /** Gets the status of the draft. */
  status: DraftState_DraftStateStatus;
  /** The number of wins you have achieved with your current draft deck. */
  wins: number;
}

export enum DraftState_DraftStateStatus {
  IN_PROGRESS = 0,
  SELECT_HERO = 1,
  COMPLETE = 2,
  RETIRED = 3,
  UNRECOGNIZED = -1,
}

export interface DraftsChooseCardRequest {
  cardIndex: number;
}

export interface DraftsChooseHeroRequest {
  heroIndex: number;
}

export interface DraftsPostRequest {
  /** Retires a draft early. Typically this costs some number of lives. */
  retireEarly: boolean;
  /** Starts a new draft. */
  startDraft: boolean;
}

/**
 * Stores data about a card currently being edited.
 *
 * For now, the text-editable view of the card is just stored as a string.
 */
export interface EditableCard {
  /** The ID of the card in the database. */
  Id: string;
  /** The user ID of the owner of this card, i.e. its creator. */
  ownerUserId: string;
  /** The CardScript source code of the card. */
  source: string;
  notification: AddedChangedRemoved | undefined;
}

/** An emote that should play from the specified entity. */
export interface Emote {
  entityId: number;
  message: Emote_EmoteMessage;
}

export enum Emote_EmoteMessage {
  HELLO = 0,
  AMAZING = 1,
  WHOOPS = 2,
  GOOD_GAME = 3,
  FACE_MY_WRATH = 4,
  WELL_PLAYED = 5,
  UNRECOGNIZED = -1,
}

export interface Entity {
  /** The entity's ID in the game. */
  id: number;
  /** The entity's armor. Conventionally, this value should be rendered on a hero entity's armor token. */
  armor?: number | undefined;
  art: Art | undefined;
  /** The entity's current attack value. Conventionally, this value should be rendered on the attack token. */
  attack?: number | undefined;
  /** The entity's base attack value. */
  baseAttack?: number | undefined;
  /** The base hitpoints of the entity. */
  baseHp?: number | undefined;
  /** The entity's base mana cost. */
  baseManaCost?: number | undefined;
  /** When true, this entity has an effect that gets triggered when it is played from the hand. */
  battlecry: boolean;
  /** The index of the entity in its zone. */
  boardPosition: number;
  /** When true, indicates this minion cannot attack, even though it normally can. */
  cannotAttack: boolean;
  /** The entity's Card ID. When null, it typically should not be rendered. */
  cardId: string;
  /** The card expansion set this entity belongs to. */
  cardSet: string;
  /** The card sets listed by the card */
  cardSets: string[];
  cardType: CardTypeMessage_CardType;
  /** When true, the entity can attack the same turn it is summoned. */
  charge: boolean;
  /** An integer number of glowing orbs to render above the entity. */
  charges?: number | undefined;
  /** Indicates this card has a choose-one effect. */
  chooseOne: boolean;
  /** Indicates the card is collectible - valid for putting into decks. */
  collectible: boolean;
  /** Indicates this minion has a combo effect. */
  combo: boolean;
  /** When true, indicates that a condition written on the card is met and the player should be informed. */
  conditionMet: boolean;
  /**
   * The number of times this enchantment (secret, quest or trigger on card) must fire before its spell effect is
   * triggered.
   */
  countUntilCast?: number | undefined;
  /** When true, this entity has an effect that gets triggered when it is destroyed. */
  deathrattles: boolean;
  /** When true, the entity's first incoming hit will hit its owner rather than itself. */
  deflect: boolean;
  /** The text that would go into the entity's description field. */
  description: string;
  /**
   * When true, indicates that this entity is destroyed. During event evaluation, an entity can be destroyed but
   * still in a zone other than the graveyard; render a death icon over the entity when it is so marked.
   */
  destroyed: boolean;
  /** Indicates the entity was discarded from the hand. */
  discarded: boolean;
  /** When true, the entity will take no loss in hitpoints the first time it would ordinarily take damage. */
  divineShield: boolean;
  /** The durability (number of uses) that the weapon still has. */
  durability?: number | undefined;
  /** The class hierarchy of this enchantment */
  enchantmentType: string;
  /** When true, this entity is under the influence of "enrage," or a bonus when it takes damage the first time. */
  enraged: boolean;
  entityType: EntityTypeMessage_EntityType;
  /** The number of times this enchantment (secret, quest or trigger on card) has fired. */
  fires?: number | undefined;
  /**
   * When true, the entity cannot attack because a spell casted on it prevents it so, until the next turn when
   * it would normally be able to attack.
   */
  frozen: boolean;
  /**
   * For player entities, indicates whether or not the player has finished the mulligan phase and is awaiting the
   * other player to finish mulligan or, if both players have this field as true, indicates the game has begun on
   * turn 0.
   */
  gameStarted: boolean;
  /** Render this entity with a "gold" effect. */
  gold: boolean;
  /** The hero class(es) of this entity. Dual-class cards (e.g. Scholomance Academy) will have multiple entries. */
  heroClasses: string[];
  /** An integer corresponding to the enchantment's host */
  host: number;
  /** When true, indicates this entity has an effect that triggers on game events. */
  hostsTrigger: boolean;
  /** The current hitpoints of the entity. Conventionally, this value should be rendered on the hitpoints token. */
  hp?: number | undefined;
  /** Indicates the entity does not take damage. */
  immune: boolean;
  /** When set on the player entity, indicates the effects occuring now are during the player's turn start phase. */
  isStartingTurn: boolean;
  location: EntityLocation | undefined;
  /** When true, the entity heals its owner when it deals damage. */
  lifesteal: boolean;
  /** The amount of mana that was locked due to overload. */
  lockedMana: number;
  /** The player's current mana. */
  mana: number;
  /** The entity's current mana cost. Conventionally, this value should be rendered on the mana token. */
  manaCost?: number | undefined;
  /** The maximum number of hitpoints this entity can have. */
  maxHp?: number | undefined;
  /** The player's maximum amount of mana. */
  maxMana: number;
  /** The text that would go into the entity's name field. */
  name: string;
  /** A renderable note attached to this entity. */
  note: string;
  /** Indicates the amount of mana that would be locked if this card were played. */
  overload?: number | undefined;
  /** An integer corresponding to the entity's owner. */
  owner: number;
  /** Indicates the entity is an on-battlefield permanent. */
  permanent: boolean;
  /** When true, indicates the card can be played, or the hero / minion can initiate a physical attack. */
  playable: boolean;
  /** When true, the entity will destroy any target it damages. */
  poisonous: boolean;
  rarity: RarityMessage_Rarity;
  /** Indicates the entity was roasted (removed due to excess cards or otherwise discarded from the deck). */
  roasted: boolean;
  /** When true, the entity can attack a minion the same turn it is summoned. */
  rush: boolean;
  /** Indicates that the entity was silenced. */
  silenced: boolean;
  /** Indicates the amount of additional spell damage this entity gives its owning player. */
  spellDamage?: number | undefined;
  /** When true, the minion cannot be targeted by the opponent until the entity attacks for the first time. */
  stealth: boolean;
  /**
   * When true, the entity cannot attack this turn because it has "summoning sickness," or a disability related
   * to the first turn the entity came into play. Typically rendered with snooze icons.
   */
  summoningSickness: boolean;
  /**
   * Indicates the entity and other taunt entities must be targeted by enemy actors first during an opponent's
   * physical attack action targeting.
   */
  taunt: boolean;
  tooltips: Tooltip[];
  /** The tribes (minion types) of this entity. Multi-tribe entities will have multiple entries (e.g. Beast and Dragon). */
  tribes: string[];
  /**
   * When true, indicates that this entity that is ordinarily censored to this user is not. It can be "flipped" and
   * shown to the opponent.
   */
  uncensored: boolean;
  /** When true, indicates this minion is benefiting from the aura of another effect. */
  underAura: boolean;
  /** Indicates this entity cannot be targeted by spells. */
  untargetableBySpells: boolean;
  /** Indicates the entity can attack twice a turn. */
  windfury: boolean;
  extraAttack?: number | undefined;
}

/**
 * An array that corresponds to the visible entities in this game state notification. The client is responsible for
 * determining which indices have been added, changed or removed.
 */
export interface EntityChangeSet {
  ids: number[];
}

/** Encodes the location of the entity. Its index should be ordered in the entity change set. */
export interface EntityLocation {
  /** The index of the entity inside its zone. */
  index: number;
  zone: ZonesMessage_Zones;
  player: number;
}

/**
 * A container for data to and from the server. This envelope is the type of every message sent through the
 * /realtime websocket endpoint.
 */
export interface Envelope {}

export interface Envelope_GameMessage {
  clientToServer: ClientToServerMessage | undefined;
  serverToClient: ServerToClientMessage | undefined;
}

export interface Envelope_MethodMessage {
  /** Removes the card with the specified record ID from the editable cards list. */
  deleteCard: Envelope_MethodMessage_DeleteCardMessage | undefined;
  /** Leave the specified queue */
  dequeue: Envelope_MethodMessage_DequeueMessage | undefined;
  enqueue: MatchmakingQueuePutRequest | undefined;
  /** The client-specified ID that will be used to mark the reply (the result) of this method call. */
  methodId: string;
  /**
   * Upserts and draws a card with the specified JSON representation.
   *
   * Only available in bot games.
   */
  putCard: Envelope_MethodMessage_PutCardMessage | undefined;
  /** Send a change message to the indicated conversationId. */
  sendMessage: Envelope_MethodMessage_SendMessageMessage | undefined;
}

export interface Envelope_MethodMessage_DeleteCardMessage {
  /** The editable card record ID, or null if one should be created. */
  editableCardId: string;
}

export interface Envelope_MethodMessage_DequeueMessage {
  /** The queue to which the server should direct this request. */
  queueId: string;
}

export interface Envelope_MethodMessage_PutCardMessage {
  /** When true, indicates that the editor should draw the card in a live game, if there is one. */
  draw: boolean;
  /** The editable card record ID, or null if one should be created. */
  editableCardId: string;
  /**
   * A JSON-formatted specification for the card.
   *
   * The ID is auto-generated and ignored, which means drawing tokens at the moment is not supported.
   */
  source: string;
}

export interface Envelope_MethodMessage_SendMessageMessage {
  /**
   * A conversation ID looks like userId1,userId2 where the first user ID is the one that comes first
   * lexicographically.
   */
  conversationId: string;
  /** The contents of the message to send to the conversation. */
  message: string;
}

export interface Envelope_RemovedMessage {
  id?: { $case: "editableCardId"; editableCardId: string } | { $case: "friendId"; friendId: string } | { $case: "inviteId"; inviteId: string } | { $case: "matchId"; matchId: string } | undefined;
}

export interface Envelope_ResultMessage {}

export interface Envelope_ResultMessage_PutCardMessage {
  /** The card ID that was put into the game. */
  cardId: string;
  /** An array of errors with the card, if applicable. */
  cardScriptErrors: string[];
  /** When not null, contains the editable card ID that was created by putting a record */
  editableCardId: string;
}

export interface Envelope_ResultMessage_SendMessageMessage {
  /** The new message ID. */
  messageId: string;
}

/** Contains a font (type, size, styling, etc.) specification */
export interface Font {
  vertex: Color | undefined;
}

export interface Friend {
  friendId: string;
  friendName: string;
  presence: PresenceMessage_Presence;
  since: number;
  notification: AddedChangedRemoved | undefined;
}

export interface FriendDeleteRequest {
  /** id of friend to unfriend. */
  friendId: string;
}

/** Adds two users to each other's friends list. */
export interface FriendPutRequest {
  /** Not supported. Throws an error if this is specified on the client. */
  friendId: string;
  /** The username with the privacy token, like "username#1234". */
  usernameWithToken: string;
}

export interface FriendPutResponse {
  friend: Friend | undefined;
}

/** An object representing all valid game actions in this action request. */
export interface GameActions {
  /** An array of actions containing source, target, type and a short description of the action. Some actions make come with additional display data like a specific card, card ID or other content. */
  all: SpellAction[];
  /**
   * An array of game action indices. Choose one at random for compatibility purposes until the client can support
   * all actions
   */
  compatibility: number[];
}

export interface GameEvent {
  cardEvent: CardEvent | undefined;
  damage: GameEvent_DamageMessage | undefined;
  /** A plain text description of this event that should be shown to the user. */
  description: string;
  destroy: GameEvent_DestroyMessage | undefined;
  /** The ID of the entity that has starting being touched by the opponent. */
  entityTouched: number;
  /** The ID of the entity that is no longer being touched by the opponent. */
  entityUntouched: number;
  /** The game event type corresponding to this game event. */
  eventType: GameEventTypeMessage_GameEventType;
  /** An integer ID corresponding to the order of this event from the client's point of view. */
  id: number;
  /** Should this event be rendered in the power history? */
  isPowerHistory: boolean;
  /**
   * Stores the source player according to the game event data. Typically this is the player who is casting the
   * card or otherwise the source of an event.
   */
  isSourcePlayerLocal: boolean;
  /** Stores the target player according to the game event data. */
  isTargetPlayerLocal: boolean;
  joust: GameEvent_JoustMessage | undefined;
  performedGameAction: GameEvent_PerformedGameActionMessage | undefined;
  source: Entity | undefined;
  target: Entity | undefined;
  /** An array of targets */
  targets: Entity[];
  triggerFired: GameEvent_TriggerFiredMessage | undefined;
  /**
   * When not null, indicates this game event comes with a value. This is typically the damage dealt, the amount of
   * healing, etc.
   */
  value?: number | undefined;
}

export interface GameEvent_DamageMessage {
  damageType: DamageTypeMessage_DamageType;
}

export interface GameEvent_DestroyMessage {
  objects: Destroy[];
}

export interface GameEvent_JoustMessage {
  opponentCard: Entity | undefined;
  ownCard: Entity | undefined;
  won: boolean;
}

export interface GameEvent_PerformedGameActionMessage {
  actionType: ActionTypeMessage_ActionType;
}

export interface GameEvent_TriggerFiredMessage {
  /** The entity ID corresponding to the source of the trigger that got fired. */
  triggerSourceId: number;
  /** The targets of the trigger's effect. */
  triggerTargetIds: number[];
}

/** An object that describes the winner and loser of a game */
export interface GameOver {
  /** True when the local player has won. */
  localPlayerWon: boolean;
  /** The ID of the player who has won. Null if no player has won. */
  winningPlayerId?: number | undefined;
}

export interface GameState {
  entities: Entity[];
  /** When true, it is the local player's turn. */
  isLocalPlayerTurn: boolean;
  /** The last ten game event objects with isPowerHistory == true. */
  powerHistory: GameEvent[];
  timestamp: number;
  turnNumber: number;
  turnState: string;
  hasPowerHistory: boolean;
}

export interface GetAccountRequest {
  targetUserId: string;
}

export interface GetAccountsRequest {
  /** An array of user IDs to fetch. */
  userIds: string[];
}

export interface GetAccountsResponse {
  accounts: Account[];
}

/** Retrieves an array of game IDs played by this player. Eventually will require paging. */
export interface GetGameRecordIdsResponse {
  /** All the game IDs ever played by this player */
  gameIds: string[];
}

export interface GetGameRecordRequest {
  gameId: string;
}

/**
 * Information about a game.
 *
 * Statistics about the game will be stored at a later point in time.
 */
export interface GetGameRecordResponse {
  /** A timestamp for when this game was finished (approximate). */
  completedAt: number;
  /** The date and time when this game was finished in the client's local time. */
  completedAtLocalized: string;
  /** True if this game was played against a bot (or was played entirely by bots) */
  isBotGame: boolean;
  /** The names of the players in this game, without their privacy tokens */
  playerNames: string[];
  replay: Replay | undefined;
}

export interface GetInviteRequest {
  inviteId: string;
}

/** A collection of cards. */
export interface InventoryCollection {
  /** The identifier of this collection. Corresponds to a deckId when this is a deck collection. */
  Id: string;
  /** Indicates whether this is a deck meant for draft, constructed play, or a rogue run. */
  deckType: InventoryCollection_InventoryCollectionDeckType;
  /** The format when this is a deck collection. */
  format: string;
  /** The hero class when this is a deck collection. */
  heroClass: string;
  inventory: CardRecord[];
  /** When true, indicates this is a standard deck provided by the server. */
  isStandardDeck: boolean;
  /** The name of this collection. Corresponds to the deck name when this is a deck collection. */
  name: string;
  /**
   * A list of player entity attributes associated with this deck.
   *
   * A player entity attribute is an attribute that comes into play before the game starts. It is used to implement
   * the Signature spell of the Ringmaster class.
   */
  playerEntityAttributes: AttributeValueTuple[];
  /** The type of collection this object is. A user's personal collection is of type USER. A deck is of type DECK. */
  type: InventoryCollection_InventoryCollectionType;
  /** The owner of this collection. */
  userId: string;
  validationReport: ValidationReport | undefined;
}

export enum InventoryCollection_InventoryCollectionDeckType {
  DRAFT = 0,
  CONSTRUCTED = 1,
  ROGUE = 2,
  UNRECOGNIZED = -1,
}

export enum InventoryCollection_InventoryCollectionType {
  USER = 0,
  ALLIANCE = 1,
  DECK = 2,
  UNRECOGNIZED = -1,
}

/** An object that indicates the player has a pending invitation to a game. */
export interface Invite {
  /** The ID of the invite. */
  Id: string;
  /** An expiration timestamp. */
  expiresAt: number;
  /** When set, indicates this is an invitation to become friends. */
  friendId: string;
  /** The user from whom the invite originates */
  fromName: string;
  /** The user ID from whom the invite originates */
  fromUserId: string;
  /** The description of this invite. Typically includes the queue contents and possibly a note from the user. */
  message: string;
  /**
   * When set, indicates this is an invitation to play a game. The queue ID to put into the matchmaking request to
   * fulfill this invite.
   */
  queueId: string;
  /**
   * Indicates the status of the invite.
   *  * UNDELIVERED: The invitation was created and is awaiting delivery, either due to ordinary networking delay
   *    or because the recipient is not yet online.
   *  * PENDING: The invitation is delivered and awaiting a response.
   *  * TIMEOUT: The recipient did not respond by the expiration time and the invitation expired.
   *  * ACCEPTED: The recipient accepted the invitation. The sender should enter the queue if they haven't already
   *    done so.
   *  * REJECTED: The recipient rejected the invitation.
   *  * CANCELLED: The sender cancelled the invitation.
   */
  status: Invite_InviteStatus;
  /** The name of the user to whom the invite is addressed */
  toName: string;
  /** The user ID to whom the invite is addressed */
  toUserId: string;
  notification: AddedChangedRemoved | undefined;
}

export enum Invite_InviteStatus {
  UNDELIVERED = 0,
  PENDING = 1,
  TIMEOUT = 2,
  ACCEPTED = 3,
  REJECTED = 4,
  CANCELLED = 5,
  UNRECOGNIZED = -1,
}

/** The invites where this user is either the sender or recipient. */
export interface InviteGetResponse {
  invites: Invite[];
}

/** Requests to send an invite to play a 1v1 match, to friend a player, or both. */
export interface InvitePostRequest {
  /**
   * The deck the user is creating this invite with. Used for 1v1 queues. If this is specified, the user is
   * automatically enqueued.
   */
  deckId: string;
  /** When true, indicates that this request is a friend invitation. */
  friend: boolean;
  /** An optional message to add to the invite request */
  message: string;
  /** The queue that the player would like to 1v1 inside of. These may differ from the competitive queues. */
  queueId: string;
  /** The user who should receive the invite */
  toUserId: string;
  /** The username and privacy token (#1234 part) to send the request to */
  toUserNameWithToken: string;
}

/**
 * The invitation that was sent, or the updated invite. If it is the kind of invite that results in matchmaking, returns
 * the match if the other player accepted the challenge.
 */
export interface InviteResponse {
  results?: { $case: "invite"; invite: Invite } | { $case: "match"; match: MatchmakingQueuePutResponse } | undefined;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  account: Account | undefined;
  loginToken: string;
}

/** A document that describes an awaiting match. */
export interface Match {
  /** The ID of the match. */
  Id: string;
  /** A timestamp for when this game was created (approximate). */
  createdAt: number;
}

export interface MatchCancelResponse {
  isCanceled: boolean;
}

export interface MatchConcedeResponse {
  isConceded: boolean;
}

/** A queue the user can enter to play a match in. */
export interface MatchmakingQueueItem {
  /** A detailed description for this queue. */
  description: string;
  /** The renderable name of the queue */
  name: string;
  /** The ID of the queue the user should put a MatchmakingQueuePutRequest into. */
  queueId: string;
  /** The arguments required for the matchmaking request. */
  requires: MatchmakingQueueItem_RequiresMessage | undefined;
  /** A tooltip for this queue. */
  tooltip: string;
}

export interface MatchmakingQueueItem_RequiresMessage {
  /** Indicates that a deck choice is required. */
  deck: boolean;
  /** Indicates that the player must choose from the specified decks. */
  deckChoices: InventoryCollection[];
  /** Indicates that the player must choose from the specified deck IDs in the player's account. */
  deckIdChoices: string[];
  /**
   * Indicates that a hero class choice is required. When a deck choice is not required, the user only
   * picks a hero.
   */
  heroClass: boolean;
}

export interface MatchmakingQueuePutRequest {
  /** When set, specifies that the bot should play the provided deck. */
  botDeckId: string;
  /** When set, specifies the deck for this queue. Some queues do not accept deck IDs. */
  deckId: string;
  /** Indicates which queue this request is for. */
  queueId: string;
  /** Set to true if this is a request to cancel any queues the user is currently in */
  cancel: boolean;
}

export interface MatchmakingQueuePutResponse {
  retry: MatchmakingQueuePutRequest | undefined;
  unityConnection: MatchmakingQueuePutResponseUnityConnection | undefined;
}

export interface MatchmakingQueuePutResponseUnityConnection {
  firstMessage: ClientToServerMessage | undefined;
  /** The websocket URL to connect to. */
  url: string;
  gameId: string;
}

/** Represents a list of queues. */
export interface MatchmakingQueuesResponse {
  /** The available queues. */
  queues: MatchmakingQueueItem[];
}

export interface PhysicalAttackEvent {
  attacker: Entity | undefined;
  damageDealt: number;
  defender: Entity | undefined;
}

export interface PostInviteRequest {
  request: InvitePostRequest | undefined;
}

export interface PostPasswordResetRequest {
  password1: string;
  password2: string;
  token: string;
}

/** Specifies a prefab (game object) */
export interface Prefab {
  /** The name of the prefab. */
  named: string;
}

/**
 * Description of a (possibly partially complete) match. Useful for viewing said match in retrospect.
 * Note: If there are `n` elements in `gameStates` then there should be `n-1` elements in `deltas`.
 */
export interface Replay {
  deltas: ReplayDeltas[];
  gameStates: ReplayGameStates[];
}

/** The forward and backward deltas (change sets) required (along with player `GameState`s) to transition the client battlefield. */
export interface ReplayDeltas {
  /** Backward delta. */
  backward: EntityChangeSet | undefined;
  /** Forward delta. */
  forward: EntityChangeSet | undefined;
}

/** A pair of game states. Used to capture a game from each player's point of view (useful for example in replays). */
export interface ReplayGameStates {
  first: GameState | undefined;
  second: GameState | undefined;
}

/** An envelope for messages from the server during gameplay. */
export interface ServerToClientMessage {
  actions: GameActions | undefined;
  changes: EntityChangeSet | undefined;
  emote: Emote | undefined;
  event: GameEvent | undefined;
  gameOver: GameOver | undefined;
  gameState: GameState | undefined;
  /**
   * An optional ID used to disambiguate multiple client replies. Include this ID in the repliesTo field of your
   * ClientToServerMessage if this field is not null.
   */
  id: string;
  /** True iff this message is a part of a replay. */
  isReplayMessage: boolean;
  /** The ID of the player that corresponds to the local player (the recipient). */
  localPlayerId: number;
  messageType: MessageTypeMessage_MessageType;
  /** Used for a mulligan request. An array of entities representing the cards you may mulligan. */
  startingCards: Entity[];
  timers: Timers | undefined;
}

/** A piece of data that assists in OpenTracing from the client. */
export interface SpanContext {
  /** Binary carried opentracing span context */
  data: string;
}

/** A spell action describes a possible action the player can take. The list of SpellAction objects in the ServerToClientMessage is exhaustive and represents every possible action. */
export interface SpellAction {
  /**
   * The action index corresponding to this action.
   *
   * If targetKeyToActions is length zero or null, the action is valid and set, corresponding to an action that
   * does not take a user-specified target. This includes all DISCOVER actions, ENDTURN, but *never* a summon,
   * even if no minions are on the board.
   */
  action: number;
  actionType: ActionTypeMessage_ActionType;
  /**
   * When set, represents a choose one action with entities set to render.
   * Those entities' id property corresponds to the choices's sourceId property. The parent/root action's sourceId corresponds to the actual entity that reveals the choices.
   */
  choices: SpellAction[];
  /** A user-readable description of this action. This is typically not rendered in the client except in logs. */
  description: string;
  entity: Entity | undefined;
  /**
   * The ID of the entity (minion or card) that is the source of the action.
   * The client is guaranteed to have this entity in its entities array.
   * For a SpellAction whose actionType is DISCOVER, the source is the entity in the acting player's discover zone. In the engine, the source is the entity that is prompting the discover (e.g. a minion if an opener is causing the discover, or the spell card being played).
   * Sometimes ENDTURN will not be available, this is because some actions like DISCOVER and BATTLECRY cannot be interrupted.
   * Running out of time will result in ENDTURN being chosen or a random DISCOVER or BATTLECRY action. This will occur on the server, not the client.
   */
  sourceId: number;
  /**
   * An array of entity ID-action pairs that let you convert a valid target to an action index to respond with.
   * Defined if this spell is targetable.
   *
   * This is null or length zero if the target does not have targeted actions. Use the action property instead for
   * that situation.
   *
   * A SpellAction with actionType SUMMON will have a targetKeyToActions entry with a target of -1 corresponding to
   * the *last* (rightmost) minion position to summon, while all other targets correspond to minions on the board.
   */
  targetKeyToActions: TargetActionPair[];
  /** Allows the client to set a request field on the SpellAction */
  request: string;
}

/** A server-side exception with content renderable to the client. */
export interface SpellsourceException {
  /** A user-renderable message explaining the source of the error. */
  message: string;
}

/** Specifies a sprite or image */
export interface Sprite {
  /**
   * The address to retrieve the sprite.
   * When this is a plain string, this corresponds to a sprite name inside the Unity client.
   * Otherwise, this is treated as a URL.
   * The server will reject art in untrusted domains.
   */
  named: string;
  /**
   * The sprite's pivot point.
   *
   *  - BOTTOM: The center bottom of the sprite (i.e. 0.5, 1.0)
   *  - DIMETRIC_2X1_FLOOR: Calculates the pivot point by ascending one pixel from the bottom for every four pixels
   *    of width, as though the pivot point is the middle of the rectangle formed at the bottom of the sprite.
   *  - CENTER: The center middle of the sprite (i.e. 0.5, 0.5)
   *  - FLYING: Beyond the bottom of the sprite (i.e. 0.5, 2.0)
   */
  pivot: Sprite_SpritePivot;
}

export enum Sprite_SpritePivot {
  BOTTOM = 0,
  DIMETRIC_2_X1_FLOOR = 1,
  CENTER = 2,
  FLYING = 3,
  UNRECOGNIZED = -1,
}

/** A pair combining a target (entity ID) and the correponding action for that target. */
export interface TargetActionPair {
  action: number;
  /**
   * The corresponding index on the friendly side of the battlefield for a summon action.
   * The minion will be summoned to the left of this index.
   */
  friendlyBattlefieldIndex: number;
  /**
   * The corresponding target for this action.
   * When -1, this indicates there is no target for this particular pair. This is relevant for summon actions which would ordinarily have a null value. A -1 here indicates the rightmost position on the battlefield.
   */
  target: number;
}

/** Information about timers. This helps the client render the countdown clock. */
export interface Timers {
  /**
   * The number of milliseconds remaining before the server will end the mulligan or turn.
   * When null or less than zero, no timer is set. This property will be valid with respect to the
   * last timestamped message from the server. Since typically emotes and touches are not timestamped,
   * while other game state messages are, this property will be updated with actions and data. It is
   * the responsibility of the client to lerp the millis-remaining values with the actual animated
   * timer to prevent choppy animation.
   */
  millisRemaining: number;
}

/**
 * Provides an explanation for an effect or keyword that will appear beside the card.
 *
 * Only delivered in the Entities from the CardRecords.
 */
export interface Tooltip {
  /**
   * When set, this tooltip will only show if cards that have the same class as the card this tooltip appears on
   * has the specified keyword appearing in its description. Typically used on the class card to provide tooltips
   * for its entire class.
   */
  keywords: string[];
  /** The text that should appear on the tooltip. */
  text: string;
}

export interface UnfriendResponse {
  deletedFriend: Friend | undefined;
}

export interface ValidationReport {
  errors: string[];
  valid: boolean;
}

function createBaseActionTypeMessage(): ActionTypeMessage {
  return {};
}

export const ActionTypeMessage = {
  encode(_: ActionTypeMessage, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ActionTypeMessage {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseActionTypeMessage();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<ActionTypeMessage>, I>>(base?: I): ActionTypeMessage {
    return ActionTypeMessage.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ActionTypeMessage>, I>>(_: I): ActionTypeMessage {
    const message = createBaseActionTypeMessage();
    return message;
  },
};

function createBaseCardTypeMessage(): CardTypeMessage {
  return {};
}

export const CardTypeMessage = {
  encode(_: CardTypeMessage, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): CardTypeMessage {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseCardTypeMessage();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<CardTypeMessage>, I>>(base?: I): CardTypeMessage {
    return CardTypeMessage.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<CardTypeMessage>, I>>(_: I): CardTypeMessage {
    const message = createBaseCardTypeMessage();
    return message;
  },
};

function createBaseDamageTypeMessage(): DamageTypeMessage {
  return {};
}

export const DamageTypeMessage = {
  encode(_: DamageTypeMessage, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): DamageTypeMessage {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseDamageTypeMessage();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<DamageTypeMessage>, I>>(base?: I): DamageTypeMessage {
    return DamageTypeMessage.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<DamageTypeMessage>, I>>(_: I): DamageTypeMessage {
    const message = createBaseDamageTypeMessage();
    return message;
  },
};

function createBaseEntityTypeMessage(): EntityTypeMessage {
  return {};
}

export const EntityTypeMessage = {
  encode(_: EntityTypeMessage, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): EntityTypeMessage {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseEntityTypeMessage();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<EntityTypeMessage>, I>>(base?: I): EntityTypeMessage {
    return EntityTypeMessage.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<EntityTypeMessage>, I>>(_: I): EntityTypeMessage {
    const message = createBaseEntityTypeMessage();
    return message;
  },
};

function createBaseMessageTypeMessage(): MessageTypeMessage {
  return {};
}

export const MessageTypeMessage = {
  encode(_: MessageTypeMessage, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): MessageTypeMessage {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseMessageTypeMessage();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<MessageTypeMessage>, I>>(base?: I): MessageTypeMessage {
    return MessageTypeMessage.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<MessageTypeMessage>, I>>(_: I): MessageTypeMessage {
    const message = createBaseMessageTypeMessage();
    return message;
  },
};

function createBasePlayerEntityAttributesMessage(): PlayerEntityAttributesMessage {
  return {};
}

export const PlayerEntityAttributesMessage = {
  encode(_: PlayerEntityAttributesMessage, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): PlayerEntityAttributesMessage {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBasePlayerEntityAttributesMessage();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<PlayerEntityAttributesMessage>, I>>(base?: I): PlayerEntityAttributesMessage {
    return PlayerEntityAttributesMessage.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<PlayerEntityAttributesMessage>, I>>(_: I): PlayerEntityAttributesMessage {
    const message = createBasePlayerEntityAttributesMessage();
    return message;
  },
};

function createBasePresenceMessage(): PresenceMessage {
  return {};
}

export const PresenceMessage = {
  encode(_: PresenceMessage, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): PresenceMessage {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBasePresenceMessage();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<PresenceMessage>, I>>(base?: I): PresenceMessage {
    return PresenceMessage.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<PresenceMessage>, I>>(_: I): PresenceMessage {
    const message = createBasePresenceMessage();
    return message;
  },
};

function createBaseRarityMessage(): RarityMessage {
  return {};
}

export const RarityMessage = {
  encode(_: RarityMessage, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): RarityMessage {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseRarityMessage();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<RarityMessage>, I>>(base?: I): RarityMessage {
    return RarityMessage.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<RarityMessage>, I>>(_: I): RarityMessage {
    const message = createBaseRarityMessage();
    return message;
  },
};

function createBaseZonesMessage(): ZonesMessage {
  return {};
}

export const ZonesMessage = {
  encode(_: ZonesMessage, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ZonesMessage {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseZonesMessage();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<ZonesMessage>, I>>(base?: I): ZonesMessage {
    return ZonesMessage.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ZonesMessage>, I>>(_: I): ZonesMessage {
    const message = createBaseZonesMessage();
    return message;
  },
};

function createBaseGameEventTypeMessage(): GameEventTypeMessage {
  return {};
}

export const GameEventTypeMessage = {
  encode(_: GameEventTypeMessage, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): GameEventTypeMessage {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseGameEventTypeMessage();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<GameEventTypeMessage>, I>>(base?: I): GameEventTypeMessage {
    return GameEventTypeMessage.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<GameEventTypeMessage>, I>>(_: I): GameEventTypeMessage {
    const message = createBaseGameEventTypeMessage();
    return message;
  },
};

function createBaseAcceptInviteRequest(): AcceptInviteRequest {
  return { inviteId: "", awaitGameStart: false, match: undefined };
}

export const AcceptInviteRequest = {
  encode(message: AcceptInviteRequest, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.inviteId !== "") {
      writer.uint32(10).string(message.inviteId);
    }
    if (message.awaitGameStart === true) {
      writer.uint32(16).bool(message.awaitGameStart);
    }
    if (message.match !== undefined) {
      MatchmakingQueuePutRequest.encode(message.match, writer.uint32(26).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): AcceptInviteRequest {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseAcceptInviteRequest();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.inviteId = reader.string();
          continue;
        case 2:
          if (tag !== 16) {
            break;
          }

          message.awaitGameStart = reader.bool();
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.match = MatchmakingQueuePutRequest.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<AcceptInviteRequest>, I>>(base?: I): AcceptInviteRequest {
    return AcceptInviteRequest.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<AcceptInviteRequest>, I>>(object: I): AcceptInviteRequest {
    const message = createBaseAcceptInviteRequest();
    message.inviteId = object.inviteId ?? "";
    message.awaitGameStart = object.awaitGameStart ?? false;
    message.match = object.match !== undefined && object.match !== null ? MatchmakingQueuePutRequest.fromPartial(object.match) : undefined;
    return message;
  },
};

function createBaseAcceptInviteResponse(): AcceptInviteResponse {
  return { friend: undefined, invite: undefined, match: undefined };
}

export const AcceptInviteResponse = {
  encode(message: AcceptInviteResponse, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.friend !== undefined) {
      FriendPutResponse.encode(message.friend, writer.uint32(10).fork()).ldelim();
    }
    if (message.invite !== undefined) {
      Invite.encode(message.invite, writer.uint32(18).fork()).ldelim();
    }
    if (message.match !== undefined) {
      MatchmakingQueuePutResponse.encode(message.match, writer.uint32(26).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): AcceptInviteResponse {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseAcceptInviteResponse();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.friend = FriendPutResponse.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.invite = Invite.decode(reader, reader.uint32());
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.match = MatchmakingQueuePutResponse.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<AcceptInviteResponse>, I>>(base?: I): AcceptInviteResponse {
    return AcceptInviteResponse.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<AcceptInviteResponse>, I>>(object: I): AcceptInviteResponse {
    const message = createBaseAcceptInviteResponse();
    message.friend = object.friend !== undefined && object.friend !== null ? FriendPutResponse.fromPartial(object.friend) : undefined;
    message.invite = object.invite !== undefined && object.invite !== null ? Invite.fromPartial(object.invite) : undefined;
    message.match = object.match !== undefined && object.match !== null ? MatchmakingQueuePutResponse.fromPartial(object.match) : undefined;
    return message;
  },
};

function createBaseAccount(): Account {
  return {
    Id: "",
    decks: [],
    email: "",
    friends: [],
    inMatch: false,
    name: "",
    personalCollection: undefined,
    privacyToken: "",
  };
}

export const Account = {
  encode(message: Account, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.Id !== "") {
      writer.uint32(10).string(message.Id);
    }
    for (const v of message.decks) {
      InventoryCollection.encode(v!, writer.uint32(18).fork()).ldelim();
    }
    if (message.email !== "") {
      writer.uint32(26).string(message.email);
    }
    for (const v of message.friends) {
      Friend.encode(v!, writer.uint32(34).fork()).ldelim();
    }
    if (message.inMatch === true) {
      writer.uint32(40).bool(message.inMatch);
    }
    if (message.name !== "") {
      writer.uint32(50).string(message.name);
    }
    if (message.personalCollection !== undefined) {
      InventoryCollection.encode(message.personalCollection, writer.uint32(58).fork()).ldelim();
    }
    if (message.privacyToken !== "") {
      writer.uint32(66).string(message.privacyToken);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): Account {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseAccount();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.Id = reader.string();
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.decks.push(InventoryCollection.decode(reader, reader.uint32()));
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.email = reader.string();
          continue;
        case 4:
          if (tag !== 34) {
            break;
          }

          message.friends.push(Friend.decode(reader, reader.uint32()));
          continue;
        case 5:
          if (tag !== 40) {
            break;
          }

          message.inMatch = reader.bool();
          continue;
        case 6:
          if (tag !== 50) {
            break;
          }

          message.name = reader.string();
          continue;
        case 7:
          if (tag !== 58) {
            break;
          }

          message.personalCollection = InventoryCollection.decode(reader, reader.uint32());
          continue;
        case 8:
          if (tag !== 66) {
            break;
          }

          message.privacyToken = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<Account>, I>>(base?: I): Account {
    return Account.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<Account>, I>>(object: I): Account {
    const message = createBaseAccount();
    message.Id = object.Id ?? "";
    message.decks = object.decks?.map((e) => InventoryCollection.fromPartial(e)) || [];
    message.email = object.email ?? "";
    message.friends = object.friends?.map((e) => Friend.fromPartial(e)) || [];
    message.inMatch = object.inMatch ?? false;
    message.name = object.name ?? "";
    message.personalCollection = object.personalCollection !== undefined && object.personalCollection !== null ? InventoryCollection.fromPartial(object.personalCollection) : undefined;
    message.privacyToken = object.privacyToken ?? "";
    return message;
  },
};

function createBaseArt(): Art {
  return {
    body: undefined,
    highlight: undefined,
    loop: undefined,
    missile: undefined,
    onCast: undefined,
    onHit: undefined,
    primary: undefined,
    secondary: undefined,
    shadow: undefined,
    spell: undefined,
    sprite: undefined,
    spriteShadow: undefined,
  };
}

export const Art = {
  encode(message: Art, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.body !== undefined) {
      Font.encode(message.body, writer.uint32(10).fork()).ldelim();
    }
    if (message.highlight !== undefined) {
      Color.encode(message.highlight, writer.uint32(18).fork()).ldelim();
    }
    if (message.loop !== undefined) {
      Prefab.encode(message.loop, writer.uint32(26).fork()).ldelim();
    }
    if (message.missile !== undefined) {
      Prefab.encode(message.missile, writer.uint32(34).fork()).ldelim();
    }
    if (message.onCast !== undefined) {
      Prefab.encode(message.onCast, writer.uint32(42).fork()).ldelim();
    }
    if (message.onHit !== undefined) {
      Prefab.encode(message.onHit, writer.uint32(50).fork()).ldelim();
    }
    if (message.primary !== undefined) {
      Color.encode(message.primary, writer.uint32(58).fork()).ldelim();
    }
    if (message.secondary !== undefined) {
      Color.encode(message.secondary, writer.uint32(66).fork()).ldelim();
    }
    if (message.shadow !== undefined) {
      Color.encode(message.shadow, writer.uint32(74).fork()).ldelim();
    }
    if (message.spell !== undefined) {
      Prefab.encode(message.spell, writer.uint32(82).fork()).ldelim();
    }
    if (message.sprite !== undefined) {
      Sprite.encode(message.sprite, writer.uint32(90).fork()).ldelim();
    }
    if (message.spriteShadow !== undefined) {
      Sprite.encode(message.spriteShadow, writer.uint32(98).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): Art {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseArt();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.body = Font.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.highlight = Color.decode(reader, reader.uint32());
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.loop = Prefab.decode(reader, reader.uint32());
          continue;
        case 4:
          if (tag !== 34) {
            break;
          }

          message.missile = Prefab.decode(reader, reader.uint32());
          continue;
        case 5:
          if (tag !== 42) {
            break;
          }

          message.onCast = Prefab.decode(reader, reader.uint32());
          continue;
        case 6:
          if (tag !== 50) {
            break;
          }

          message.onHit = Prefab.decode(reader, reader.uint32());
          continue;
        case 7:
          if (tag !== 58) {
            break;
          }

          message.primary = Color.decode(reader, reader.uint32());
          continue;
        case 8:
          if (tag !== 66) {
            break;
          }

          message.secondary = Color.decode(reader, reader.uint32());
          continue;
        case 9:
          if (tag !== 74) {
            break;
          }

          message.shadow = Color.decode(reader, reader.uint32());
          continue;
        case 10:
          if (tag !== 82) {
            break;
          }

          message.spell = Prefab.decode(reader, reader.uint32());
          continue;
        case 11:
          if (tag !== 90) {
            break;
          }

          message.sprite = Sprite.decode(reader, reader.uint32());
          continue;
        case 12:
          if (tag !== 98) {
            break;
          }

          message.spriteShadow = Sprite.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<Art>, I>>(base?: I): Art {
    return Art.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<Art>, I>>(object: I): Art {
    const message = createBaseArt();
    message.body = object.body !== undefined && object.body !== null ? Font.fromPartial(object.body) : undefined;
    message.highlight = object.highlight !== undefined && object.highlight !== null ? Color.fromPartial(object.highlight) : undefined;
    message.loop = object.loop !== undefined && object.loop !== null ? Prefab.fromPartial(object.loop) : undefined;
    message.missile = object.missile !== undefined && object.missile !== null ? Prefab.fromPartial(object.missile) : undefined;
    message.onCast = object.onCast !== undefined && object.onCast !== null ? Prefab.fromPartial(object.onCast) : undefined;
    message.onHit = object.onHit !== undefined && object.onHit !== null ? Prefab.fromPartial(object.onHit) : undefined;
    message.primary = object.primary !== undefined && object.primary !== null ? Color.fromPartial(object.primary) : undefined;
    message.secondary = object.secondary !== undefined && object.secondary !== null ? Color.fromPartial(object.secondary) : undefined;
    message.shadow = object.shadow !== undefined && object.shadow !== null ? Color.fromPartial(object.shadow) : undefined;
    message.spell = object.spell !== undefined && object.spell !== null ? Prefab.fromPartial(object.spell) : undefined;
    message.sprite = object.sprite !== undefined && object.sprite !== null ? Sprite.fromPartial(object.sprite) : undefined;
    message.spriteShadow = object.spriteShadow !== undefined && object.spriteShadow !== null ? Sprite.fromPartial(object.spriteShadow) : undefined;
    return message;
  },
};

function createBaseArtificialIntelligence(): ArtificialIntelligence {
  return { hardRemoval: false };
}

export const ArtificialIntelligence = {
  encode(message: ArtificialIntelligence, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.hardRemoval === true) {
      writer.uint32(8).bool(message.hardRemoval);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ArtificialIntelligence {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseArtificialIntelligence();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.hardRemoval = reader.bool();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<ArtificialIntelligence>, I>>(base?: I): ArtificialIntelligence {
    return ArtificialIntelligence.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ArtificialIntelligence>, I>>(object: I): ArtificialIntelligence {
    const message = createBaseArtificialIntelligence();
    message.hardRemoval = object.hardRemoval ?? false;
    return message;
  },
};

function createBaseDraft(): Draft {
  return { banned: false };
}

export const Draft = {
  encode(message: Draft, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.banned === true) {
      writer.uint32(8).bool(message.banned);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): Draft {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseDraft();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.banned = reader.bool();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<Draft>, I>>(base?: I): Draft {
    return Draft.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<Draft>, I>>(object: I): Draft {
    const message = createBaseDraft();
    message.banned = object.banned ?? false;
    return message;
  },
};

function createBaseAttributeValueTuple(): AttributeValueTuple {
  return { attribute: 0, stringValue: "" };
}

export const AttributeValueTuple = {
  encode(message: AttributeValueTuple, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.attribute !== 0) {
      writer.uint32(8).int32(message.attribute);
    }
    if (message.stringValue !== "") {
      writer.uint32(18).string(message.stringValue);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): AttributeValueTuple {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseAttributeValueTuple();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.attribute = reader.int32() as any;
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.stringValue = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<AttributeValueTuple>, I>>(base?: I): AttributeValueTuple {
    return AttributeValueTuple.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<AttributeValueTuple>, I>>(object: I): AttributeValueTuple {
    const message = createBaseAttributeValueTuple();
    message.attribute = object.attribute ?? 0;
    message.stringValue = object.stringValue ?? "";
    return message;
  },
};

function createBaseCardEvent(): CardEvent {
  return { card: undefined, showLocal: false };
}

export const CardEvent = {
  encode(message: CardEvent, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.card !== undefined) {
      Entity.encode(message.card, writer.uint32(10).fork()).ldelim();
    }
    if (message.showLocal === true) {
      writer.uint32(16).bool(message.showLocal);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): CardEvent {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseCardEvent();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.card = Entity.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 16) {
            break;
          }

          message.showLocal = reader.bool();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<CardEvent>, I>>(base?: I): CardEvent {
    return CardEvent.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<CardEvent>, I>>(object: I): CardEvent {
    const message = createBaseCardEvent();
    message.card = object.card !== undefined && object.card !== null ? Entity.fromPartial(object.card) : undefined;
    message.showLocal = object.showLocal ?? false;
    return message;
  },
};

function createBaseCardRecord(): CardRecord {
  return {
    id: 0,
    allianceId: "",
    borrowedByUserId: "",
    collectionIds: [],
    donorUserId: "",
    entity: undefined,
    userId: "",
    count: 0,
  };
}

export const CardRecord = {
  encode(message: CardRecord, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.id !== 0) {
      writer.uint32(8).int64(message.id);
    }
    if (message.allianceId !== "") {
      writer.uint32(18).string(message.allianceId);
    }
    if (message.borrowedByUserId !== "") {
      writer.uint32(26).string(message.borrowedByUserId);
    }
    for (const v of message.collectionIds) {
      writer.uint32(34).string(v!);
    }
    if (message.donorUserId !== "") {
      writer.uint32(42).string(message.donorUserId);
    }
    if (message.entity !== undefined) {
      Entity.encode(message.entity, writer.uint32(50).fork()).ldelim();
    }
    if (message.userId !== "") {
      writer.uint32(58).string(message.userId);
    }
    if (message.count !== 0) {
      writer.uint32(64).int32(message.count);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): CardRecord {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseCardRecord();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.id = longToNumber(reader.int64() as Long);
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.allianceId = reader.string();
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.borrowedByUserId = reader.string();
          continue;
        case 4:
          if (tag !== 34) {
            break;
          }

          message.collectionIds.push(reader.string());
          continue;
        case 5:
          if (tag !== 42) {
            break;
          }

          message.donorUserId = reader.string();
          continue;
        case 6:
          if (tag !== 50) {
            break;
          }

          message.entity = Entity.decode(reader, reader.uint32());
          continue;
        case 7:
          if (tag !== 58) {
            break;
          }

          message.userId = reader.string();
          continue;
        case 8:
          if (tag !== 64) {
            break;
          }

          message.count = reader.int32();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<CardRecord>, I>>(base?: I): CardRecord {
    return CardRecord.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<CardRecord>, I>>(object: I): CardRecord {
    const message = createBaseCardRecord();
    message.id = object.id ?? 0;
    message.allianceId = object.allianceId ?? "";
    message.borrowedByUserId = object.borrowedByUserId ?? "";
    message.collectionIds = object.collectionIds?.map((e) => e) || [];
    message.donorUserId = object.donorUserId ?? "";
    message.entity = object.entity !== undefined && object.entity !== null ? Entity.fromPartial(object.entity) : undefined;
    message.userId = object.userId ?? "";
    message.count = object.count ?? 0;
    return message;
  },
};

function createBaseChangePasswordRequest(): ChangePasswordRequest {
  return { password: "" };
}

export const ChangePasswordRequest = {
  encode(message: ChangePasswordRequest, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.password !== "") {
      writer.uint32(10).string(message.password);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ChangePasswordRequest {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseChangePasswordRequest();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.password = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<ChangePasswordRequest>, I>>(base?: I): ChangePasswordRequest {
    return ChangePasswordRequest.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ChangePasswordRequest>, I>>(object: I): ChangePasswordRequest {
    const message = createBaseChangePasswordRequest();
    message.password = object.password ?? "";
    return message;
  },
};

function createBaseChangePasswordResponse(): ChangePasswordResponse {
  return {};
}

export const ChangePasswordResponse = {
  encode(_: ChangePasswordResponse, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ChangePasswordResponse {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseChangePasswordResponse();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<ChangePasswordResponse>, I>>(base?: I): ChangePasswordResponse {
    return ChangePasswordResponse.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ChangePasswordResponse>, I>>(_: I): ChangePasswordResponse {
    const message = createBaseChangePasswordResponse();
    return message;
  },
};

function createBaseChatMessage(): ChatMessage {
  return {
    conversationId: "",
    dateLabel: "",
    message: "",
    messageId: "",
    senderName: "",
    senderUserId: "",
    timestamp: 0,
    notification: undefined,
  };
}

export const ChatMessage = {
  encode(message: ChatMessage, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.conversationId !== "") {
      writer.uint32(10).string(message.conversationId);
    }
    if (message.dateLabel !== "") {
      writer.uint32(18).string(message.dateLabel);
    }
    if (message.message !== "") {
      writer.uint32(26).string(message.message);
    }
    if (message.messageId !== "") {
      writer.uint32(34).string(message.messageId);
    }
    if (message.senderName !== "") {
      writer.uint32(42).string(message.senderName);
    }
    if (message.senderUserId !== "") {
      writer.uint32(50).string(message.senderUserId);
    }
    if (message.timestamp !== 0) {
      writer.uint32(56).int64(message.timestamp);
    }
    if (message.notification !== undefined) {
      AddedChangedRemoved.encode(message.notification, writer.uint32(66).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ChatMessage {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseChatMessage();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.conversationId = reader.string();
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.dateLabel = reader.string();
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.message = reader.string();
          continue;
        case 4:
          if (tag !== 34) {
            break;
          }

          message.messageId = reader.string();
          continue;
        case 5:
          if (tag !== 42) {
            break;
          }

          message.senderName = reader.string();
          continue;
        case 6:
          if (tag !== 50) {
            break;
          }

          message.senderUserId = reader.string();
          continue;
        case 7:
          if (tag !== 56) {
            break;
          }

          message.timestamp = longToNumber(reader.int64() as Long);
          continue;
        case 8:
          if (tag !== 66) {
            break;
          }

          message.notification = AddedChangedRemoved.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<ChatMessage>, I>>(base?: I): ChatMessage {
    return ChatMessage.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ChatMessage>, I>>(object: I): ChatMessage {
    const message = createBaseChatMessage();
    message.conversationId = object.conversationId ?? "";
    message.dateLabel = object.dateLabel ?? "";
    message.message = object.message ?? "";
    message.messageId = object.messageId ?? "";
    message.senderName = object.senderName ?? "";
    message.senderUserId = object.senderUserId ?? "";
    message.timestamp = object.timestamp ?? 0;
    message.notification = object.notification !== undefined && object.notification !== null ? AddedChangedRemoved.fromPartial(object.notification) : undefined;
    return message;
  },
};

function createBaseClientToServerMessage(): ClientToServerMessage {
  return {
    actionIndex: 0,
    discardedCardIndices: [],
    emote: undefined,
    entityTouch: undefined,
    entityUntouch: undefined,
    firstMessage: undefined,
    messageType: 0,
    repliesTo: "",
  };
}

export const ClientToServerMessage = {
  encode(message: ClientToServerMessage, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.actionIndex !== 0) {
      writer.uint32(8).int32(message.actionIndex);
    }
    writer.uint32(18).fork();
    for (const v of message.discardedCardIndices) {
      writer.int32(v);
    }
    writer.ldelim();
    if (message.emote !== undefined) {
      Emote.encode(message.emote, writer.uint32(26).fork()).ldelim();
    }
    if (message.entityTouch !== undefined) {
      writer.uint32(32).int32(message.entityTouch);
    }
    if (message.entityUntouch !== undefined) {
      writer.uint32(40).int32(message.entityUntouch);
    }
    if (message.firstMessage !== undefined) {
      ClientToServerMessage_FirstMessageMessage.encode(message.firstMessage, writer.uint32(50).fork()).ldelim();
    }
    if (message.messageType !== 0) {
      writer.uint32(56).int32(message.messageType);
    }
    if (message.repliesTo !== "") {
      writer.uint32(66).string(message.repliesTo);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ClientToServerMessage {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseClientToServerMessage();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.actionIndex = reader.int32();
          continue;
        case 2:
          if (tag === 16) {
            message.discardedCardIndices.push(reader.int32());

            continue;
          }

          if (tag === 18) {
            const end2 = reader.uint32() + reader.pos;
            while (reader.pos < end2) {
              message.discardedCardIndices.push(reader.int32());
            }

            continue;
          }

          break;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.emote = Emote.decode(reader, reader.uint32());
          continue;
        case 4:
          if (tag !== 32) {
            break;
          }

          message.entityTouch = reader.int32();
          continue;
        case 5:
          if (tag !== 40) {
            break;
          }

          message.entityUntouch = reader.int32();
          continue;
        case 6:
          if (tag !== 50) {
            break;
          }

          message.firstMessage = ClientToServerMessage_FirstMessageMessage.decode(reader, reader.uint32());
          continue;
        case 7:
          if (tag !== 56) {
            break;
          }

          message.messageType = reader.int32() as any;
          continue;
        case 8:
          if (tag !== 66) {
            break;
          }

          message.repliesTo = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<ClientToServerMessage>, I>>(base?: I): ClientToServerMessage {
    return ClientToServerMessage.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ClientToServerMessage>, I>>(object: I): ClientToServerMessage {
    const message = createBaseClientToServerMessage();
    message.actionIndex = object.actionIndex ?? 0;
    message.discardedCardIndices = object.discardedCardIndices?.map((e) => e) || [];
    message.emote = object.emote !== undefined && object.emote !== null ? Emote.fromPartial(object.emote) : undefined;
    message.entityTouch = object.entityTouch ?? undefined;
    message.entityUntouch = object.entityUntouch ?? undefined;
    message.firstMessage = object.firstMessage !== undefined && object.firstMessage !== null ? ClientToServerMessage_FirstMessageMessage.fromPartial(object.firstMessage) : undefined;
    message.messageType = object.messageType ?? 0;
    message.repliesTo = object.repliesTo ?? "";
    return message;
  },
};

function createBaseClientToServerMessage_FirstMessageMessage(): ClientToServerMessage_FirstMessageMessage {
  return { playerKey: "", playerSecret: "" };
}

export const ClientToServerMessage_FirstMessageMessage = {
  encode(message: ClientToServerMessage_FirstMessageMessage, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.playerKey !== "") {
      writer.uint32(10).string(message.playerKey);
    }
    if (message.playerSecret !== "") {
      writer.uint32(18).string(message.playerSecret);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ClientToServerMessage_FirstMessageMessage {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseClientToServerMessage_FirstMessageMessage();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.playerKey = reader.string();
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.playerSecret = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<ClientToServerMessage_FirstMessageMessage>, I>>(base?: I): ClientToServerMessage_FirstMessageMessage {
    return ClientToServerMessage_FirstMessageMessage.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ClientToServerMessage_FirstMessageMessage>, I>>(object: I): ClientToServerMessage_FirstMessageMessage {
    const message = createBaseClientToServerMessage_FirstMessageMessage();
    message.playerKey = object.playerKey ?? "";
    message.playerSecret = object.playerSecret ?? "";
    return message;
  },
};

function createBaseColor(): Color {
  return { a: 0, b: 0, g: 0, r: 0 };
}

export const Color = {
  encode(message: Color, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.a !== 0) {
      writer.uint32(13).float(message.a);
    }
    if (message.b !== 0) {
      writer.uint32(21).float(message.b);
    }
    if (message.g !== 0) {
      writer.uint32(29).float(message.g);
    }
    if (message.r !== 0) {
      writer.uint32(37).float(message.r);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): Color {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseColor();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 13) {
            break;
          }

          message.a = reader.float();
          continue;
        case 2:
          if (tag !== 21) {
            break;
          }

          message.b = reader.float();
          continue;
        case 3:
          if (tag !== 29) {
            break;
          }

          message.g = reader.float();
          continue;
        case 4:
          if (tag !== 37) {
            break;
          }

          message.r = reader.float();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<Color>, I>>(base?: I): Color {
    return Color.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<Color>, I>>(object: I): Color {
    const message = createBaseColor();
    message.a = object.a ?? 0;
    message.b = object.b ?? 0;
    message.g = object.g ?? 0;
    message.r = object.r ?? 0;
    return message;
  },
};

function createBaseCreateAccountRequest(): CreateAccountRequest {
  return { email: "", name: "", password: "" };
}

export const CreateAccountRequest = {
  encode(message: CreateAccountRequest, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.email !== "") {
      writer.uint32(10).string(message.email);
    }
    if (message.name !== "") {
      writer.uint32(18).string(message.name);
    }
    if (message.password !== "") {
      writer.uint32(26).string(message.password);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): CreateAccountRequest {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseCreateAccountRequest();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.email = reader.string();
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.name = reader.string();
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.password = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<CreateAccountRequest>, I>>(base?: I): CreateAccountRequest {
    return CreateAccountRequest.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<CreateAccountRequest>, I>>(object: I): CreateAccountRequest {
    const message = createBaseCreateAccountRequest();
    message.email = object.email ?? "";
    message.name = object.name ?? "";
    message.password = object.password ?? "";
    return message;
  },
};

function createBaseCreateAccountResponse(): CreateAccountResponse {
  return { account: undefined, loginToken: "" };
}

export const CreateAccountResponse = {
  encode(message: CreateAccountResponse, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.account !== undefined) {
      Account.encode(message.account, writer.uint32(10).fork()).ldelim();
    }
    if (message.loginToken !== "") {
      writer.uint32(18).string(message.loginToken);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): CreateAccountResponse {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseCreateAccountResponse();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.account = Account.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.loginToken = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<CreateAccountResponse>, I>>(base?: I): CreateAccountResponse {
    return CreateAccountResponse.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<CreateAccountResponse>, I>>(object: I): CreateAccountResponse {
    const message = createBaseCreateAccountResponse();
    message.account = object.account !== undefined && object.account !== null ? Account.fromPartial(object.account) : undefined;
    message.loginToken = object.loginToken ?? "";
    return message;
  },
};

function createBaseDecksDeleteRequest(): DecksDeleteRequest {
  return { deckId: "" };
}

export const DecksDeleteRequest = {
  encode(message: DecksDeleteRequest, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.deckId !== "") {
      writer.uint32(10).string(message.deckId);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): DecksDeleteRequest {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseDecksDeleteRequest();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.deckId = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<DecksDeleteRequest>, I>>(base?: I): DecksDeleteRequest {
    return DecksDeleteRequest.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<DecksDeleteRequest>, I>>(object: I): DecksDeleteRequest {
    const message = createBaseDecksDeleteRequest();
    message.deckId = object.deckId ?? "";
    return message;
  },
};

function createBaseDecksGetAllResponse(): DecksGetAllResponse {
  return { decks: [] };
}

export const DecksGetAllResponse = {
  encode(message: DecksGetAllResponse, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    for (const v of message.decks) {
      DecksGetResponse.encode(v!, writer.uint32(10).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): DecksGetAllResponse {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseDecksGetAllResponse();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.decks.push(DecksGetResponse.decode(reader, reader.uint32()));
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<DecksGetAllResponse>, I>>(base?: I): DecksGetAllResponse {
    return DecksGetAllResponse.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<DecksGetAllResponse>, I>>(object: I): DecksGetAllResponse {
    const message = createBaseDecksGetAllResponse();
    message.decks = object.decks?.map((e) => DecksGetResponse.fromPartial(e)) || [];
    return message;
  },
};

function createBaseDecksGetRequest(): DecksGetRequest {
  return { deckId: "" };
}

export const DecksGetRequest = {
  encode(message: DecksGetRequest, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.deckId !== "") {
      writer.uint32(10).string(message.deckId);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): DecksGetRequest {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseDecksGetRequest();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.deckId = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<DecksGetRequest>, I>>(base?: I): DecksGetRequest {
    return DecksGetRequest.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<DecksGetRequest>, I>>(object: I): DecksGetRequest {
    const message = createBaseDecksGetRequest();
    message.deckId = object.deckId ?? "";
    return message;
  },
};

function createBaseDecksGetResponse(): DecksGetResponse {
  return { collection: undefined, inventoryIdsSize: 0 };
}

export const DecksGetResponse = {
  encode(message: DecksGetResponse, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.collection !== undefined) {
      InventoryCollection.encode(message.collection, writer.uint32(10).fork()).ldelim();
    }
    if (message.inventoryIdsSize !== 0) {
      writer.uint32(16).int32(message.inventoryIdsSize);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): DecksGetResponse {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseDecksGetResponse();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.collection = InventoryCollection.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 16) {
            break;
          }

          message.inventoryIdsSize = reader.int32();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<DecksGetResponse>, I>>(base?: I): DecksGetResponse {
    return DecksGetResponse.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<DecksGetResponse>, I>>(object: I): DecksGetResponse {
    const message = createBaseDecksGetResponse();
    message.collection = object.collection !== undefined && object.collection !== null ? InventoryCollection.fromPartial(object.collection) : undefined;
    message.inventoryIdsSize = object.inventoryIdsSize ?? 0;
    return message;
  },
};

function createBaseDecksPutRequest(): DecksPutRequest {
  return { deckList: "", format: "", heroClass: "", inventoryIds: [], name: "", cardIds: [] };
}

export const DecksPutRequest = {
  encode(message: DecksPutRequest, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.deckList !== "") {
      writer.uint32(10).string(message.deckList);
    }
    if (message.format !== "") {
      writer.uint32(18).string(message.format);
    }
    if (message.heroClass !== "") {
      writer.uint32(26).string(message.heroClass);
    }
    for (const v of message.inventoryIds) {
      writer.uint32(34).string(v!);
    }
    if (message.name !== "") {
      writer.uint32(42).string(message.name);
    }
    for (const v of message.cardIds) {
      writer.uint32(50).string(v!);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): DecksPutRequest {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseDecksPutRequest();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.deckList = reader.string();
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.format = reader.string();
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.heroClass = reader.string();
          continue;
        case 4:
          if (tag !== 34) {
            break;
          }

          message.inventoryIds.push(reader.string());
          continue;
        case 5:
          if (tag !== 42) {
            break;
          }

          message.name = reader.string();
          continue;
        case 6:
          if (tag !== 50) {
            break;
          }

          message.cardIds.push(reader.string());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<DecksPutRequest>, I>>(base?: I): DecksPutRequest {
    return DecksPutRequest.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<DecksPutRequest>, I>>(object: I): DecksPutRequest {
    const message = createBaseDecksPutRequest();
    message.deckList = object.deckList ?? "";
    message.format = object.format ?? "";
    message.heroClass = object.heroClass ?? "";
    message.inventoryIds = object.inventoryIds?.map((e) => e) || [];
    message.name = object.name ?? "";
    message.cardIds = object.cardIds?.map((e) => e) || [];
    return message;
  },
};

function createBaseDecksPutResponse(): DecksPutResponse {
  return { collection: undefined, deckId: "" };
}

export const DecksPutResponse = {
  encode(message: DecksPutResponse, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.collection !== undefined) {
      InventoryCollection.encode(message.collection, writer.uint32(10).fork()).ldelim();
    }
    if (message.deckId !== "") {
      writer.uint32(18).string(message.deckId);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): DecksPutResponse {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseDecksPutResponse();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.collection = InventoryCollection.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.deckId = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<DecksPutResponse>, I>>(base?: I): DecksPutResponse {
    return DecksPutResponse.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<DecksPutResponse>, I>>(object: I): DecksPutResponse {
    const message = createBaseDecksPutResponse();
    message.collection = object.collection !== undefined && object.collection !== null ? InventoryCollection.fromPartial(object.collection) : undefined;
    message.deckId = object.deckId ?? "";
    return message;
  },
};

function createBaseDecksUpdateCommand(): DecksUpdateCommand {
  return {
    pullAllCardIds: [],
    pullAllInventoryIds: [],
    pushCardIds: undefined,
    pushInventoryIds: undefined,
    setHeroClass: "",
    setInventoryIds: [],
    setName: "",
    setPlayerEntityAttribute: undefined,
    unsetPlayerEntityAttribute: "",
  };
}

export const DecksUpdateCommand = {
  encode(message: DecksUpdateCommand, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    for (const v of message.pullAllCardIds) {
      writer.uint32(10).string(v!);
    }
    writer.uint32(18).fork();
    for (const v of message.pullAllInventoryIds) {
      writer.int64(v);
    }
    writer.ldelim();
    if (message.pushCardIds !== undefined) {
      DecksUpdateCommand_PushCardIdsMessage.encode(message.pushCardIds, writer.uint32(26).fork()).ldelim();
    }
    if (message.pushInventoryIds !== undefined) {
      DecksUpdateCommand_PushInventoryIdsMessage.encode(message.pushInventoryIds, writer.uint32(34).fork()).ldelim();
    }
    if (message.setHeroClass !== "") {
      writer.uint32(42).string(message.setHeroClass);
    }
    writer.uint32(50).fork();
    for (const v of message.setInventoryIds) {
      writer.int64(v);
    }
    writer.ldelim();
    if (message.setName !== "") {
      writer.uint32(58).string(message.setName);
    }
    if (message.setPlayerEntityAttribute !== undefined) {
      DecksUpdateCommand_SetPlayerEntityAttributeMessage.encode(message.setPlayerEntityAttribute, writer.uint32(66).fork()).ldelim();
    }
    if (message.unsetPlayerEntityAttribute !== "") {
      writer.uint32(74).string(message.unsetPlayerEntityAttribute);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): DecksUpdateCommand {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseDecksUpdateCommand();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.pullAllCardIds.push(reader.string());
          continue;
        case 2:
          if (tag === 16) {
            message.pullAllInventoryIds.push(longToNumber(reader.int64() as Long));

            continue;
          }

          if (tag === 18) {
            const end2 = reader.uint32() + reader.pos;
            while (reader.pos < end2) {
              message.pullAllInventoryIds.push(longToNumber(reader.int64() as Long));
            }

            continue;
          }

          break;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.pushCardIds = DecksUpdateCommand_PushCardIdsMessage.decode(reader, reader.uint32());
          continue;
        case 4:
          if (tag !== 34) {
            break;
          }

          message.pushInventoryIds = DecksUpdateCommand_PushInventoryIdsMessage.decode(reader, reader.uint32());
          continue;
        case 5:
          if (tag !== 42) {
            break;
          }

          message.setHeroClass = reader.string();
          continue;
        case 6:
          if (tag === 48) {
            message.setInventoryIds.push(longToNumber(reader.int64() as Long));

            continue;
          }

          if (tag === 50) {
            const end2 = reader.uint32() + reader.pos;
            while (reader.pos < end2) {
              message.setInventoryIds.push(longToNumber(reader.int64() as Long));
            }

            continue;
          }

          break;
        case 7:
          if (tag !== 58) {
            break;
          }

          message.setName = reader.string();
          continue;
        case 8:
          if (tag !== 66) {
            break;
          }

          message.setPlayerEntityAttribute = DecksUpdateCommand_SetPlayerEntityAttributeMessage.decode(reader, reader.uint32());
          continue;
        case 9:
          if (tag !== 74) {
            break;
          }

          message.unsetPlayerEntityAttribute = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<DecksUpdateCommand>, I>>(base?: I): DecksUpdateCommand {
    return DecksUpdateCommand.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<DecksUpdateCommand>, I>>(object: I): DecksUpdateCommand {
    const message = createBaseDecksUpdateCommand();
    message.pullAllCardIds = object.pullAllCardIds?.map((e) => e) || [];
    message.pullAllInventoryIds = object.pullAllInventoryIds?.map((e) => e) || [];
    message.pushCardIds = object.pushCardIds !== undefined && object.pushCardIds !== null ? DecksUpdateCommand_PushCardIdsMessage.fromPartial(object.pushCardIds) : undefined;
    message.pushInventoryIds = object.pushInventoryIds !== undefined && object.pushInventoryIds !== null ? DecksUpdateCommand_PushInventoryIdsMessage.fromPartial(object.pushInventoryIds) : undefined;
    message.setHeroClass = object.setHeroClass ?? "";
    message.setInventoryIds = object.setInventoryIds?.map((e) => e) || [];
    message.setName = object.setName ?? "";
    message.setPlayerEntityAttribute = object.setPlayerEntityAttribute !== undefined && object.setPlayerEntityAttribute !== null ? DecksUpdateCommand_SetPlayerEntityAttributeMessage.fromPartial(object.setPlayerEntityAttribute) : undefined;
    message.unsetPlayerEntityAttribute = object.unsetPlayerEntityAttribute ?? "";
    return message;
  },
};

function createBaseDecksUpdateCommand_PushCardIdsMessage(): DecksUpdateCommand_PushCardIdsMessage {
  return { Each: [] };
}

export const DecksUpdateCommand_PushCardIdsMessage = {
  encode(message: DecksUpdateCommand_PushCardIdsMessage, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    for (const v of message.Each) {
      writer.uint32(10).string(v!);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): DecksUpdateCommand_PushCardIdsMessage {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseDecksUpdateCommand_PushCardIdsMessage();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.Each.push(reader.string());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<DecksUpdateCommand_PushCardIdsMessage>, I>>(base?: I): DecksUpdateCommand_PushCardIdsMessage {
    return DecksUpdateCommand_PushCardIdsMessage.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<DecksUpdateCommand_PushCardIdsMessage>, I>>(object: I): DecksUpdateCommand_PushCardIdsMessage {
    const message = createBaseDecksUpdateCommand_PushCardIdsMessage();
    message.Each = object.Each?.map((e) => e) || [];
    return message;
  },
};

function createBaseDecksUpdateCommand_PushInventoryIdsMessage(): DecksUpdateCommand_PushInventoryIdsMessage {
  return { Each: [] };
}

export const DecksUpdateCommand_PushInventoryIdsMessage = {
  encode(message: DecksUpdateCommand_PushInventoryIdsMessage, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    for (const v of message.Each) {
      writer.uint32(10).string(v!);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): DecksUpdateCommand_PushInventoryIdsMessage {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseDecksUpdateCommand_PushInventoryIdsMessage();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.Each.push(reader.string());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<DecksUpdateCommand_PushInventoryIdsMessage>, I>>(base?: I): DecksUpdateCommand_PushInventoryIdsMessage {
    return DecksUpdateCommand_PushInventoryIdsMessage.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<DecksUpdateCommand_PushInventoryIdsMessage>, I>>(object: I): DecksUpdateCommand_PushInventoryIdsMessage {
    const message = createBaseDecksUpdateCommand_PushInventoryIdsMessage();
    message.Each = object.Each?.map((e) => e) || [];
    return message;
  },
};

function createBaseDecksUpdateCommand_SetPlayerEntityAttributeMessage(): DecksUpdateCommand_SetPlayerEntityAttributeMessage {
  return { attribute: 0, stringValue: "" };
}

export const DecksUpdateCommand_SetPlayerEntityAttributeMessage = {
  encode(message: DecksUpdateCommand_SetPlayerEntityAttributeMessage, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.attribute !== 0) {
      writer.uint32(8).int32(message.attribute);
    }
    if (message.stringValue !== "") {
      writer.uint32(18).string(message.stringValue);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): DecksUpdateCommand_SetPlayerEntityAttributeMessage {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseDecksUpdateCommand_SetPlayerEntityAttributeMessage();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.attribute = reader.int32() as any;
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.stringValue = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<DecksUpdateCommand_SetPlayerEntityAttributeMessage>, I>>(base?: I): DecksUpdateCommand_SetPlayerEntityAttributeMessage {
    return DecksUpdateCommand_SetPlayerEntityAttributeMessage.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<DecksUpdateCommand_SetPlayerEntityAttributeMessage>, I>>(object: I): DecksUpdateCommand_SetPlayerEntityAttributeMessage {
    const message = createBaseDecksUpdateCommand_SetPlayerEntityAttributeMessage();
    message.attribute = object.attribute ?? 0;
    message.stringValue = object.stringValue ?? "";
    return message;
  },
};

function createBaseDecksUpdateRequest(): DecksUpdateRequest {
  return { deckId: "", updateCommand: undefined };
}

export const DecksUpdateRequest = {
  encode(message: DecksUpdateRequest, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.deckId !== "") {
      writer.uint32(10).string(message.deckId);
    }
    if (message.updateCommand !== undefined) {
      DecksUpdateCommand.encode(message.updateCommand, writer.uint32(18).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): DecksUpdateRequest {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseDecksUpdateRequest();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.deckId = reader.string();
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.updateCommand = DecksUpdateCommand.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<DecksUpdateRequest>, I>>(base?: I): DecksUpdateRequest {
    return DecksUpdateRequest.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<DecksUpdateRequest>, I>>(object: I): DecksUpdateRequest {
    const message = createBaseDecksUpdateRequest();
    message.deckId = object.deckId ?? "";
    message.updateCommand = object.updateCommand !== undefined && object.updateCommand !== null ? DecksUpdateCommand.fromPartial(object.updateCommand) : undefined;
    return message;
  },
};

function createBaseDefaultMethodResponse(): DefaultMethodResponse {
  return {};
}

export const DefaultMethodResponse = {
  encode(_: DefaultMethodResponse, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): DefaultMethodResponse {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseDefaultMethodResponse();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<DefaultMethodResponse>, I>>(base?: I): DefaultMethodResponse {
    return DefaultMethodResponse.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<DefaultMethodResponse>, I>>(_: I): DefaultMethodResponse {
    const message = createBaseDefaultMethodResponse();
    return message;
  },
};

function createBaseDeleteInviteRequest(): DeleteInviteRequest {
  return { inviteId: "" };
}

export const DeleteInviteRequest = {
  encode(message: DeleteInviteRequest, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.inviteId !== "") {
      writer.uint32(10).string(message.inviteId);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): DeleteInviteRequest {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseDeleteInviteRequest();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.inviteId = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<DeleteInviteRequest>, I>>(base?: I): DeleteInviteRequest {
    return DeleteInviteRequest.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<DeleteInviteRequest>, I>>(object: I): DeleteInviteRequest {
    const message = createBaseDeleteInviteRequest();
    message.inviteId = object.inviteId ?? "";
    return message;
  },
};

function createBaseDestroy(): Destroy {
  return { aftermaths: [], source: undefined, target: undefined };
}

export const Destroy = {
  encode(message: Destroy, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    for (const v of message.aftermaths) {
      Entity.encode(v!, writer.uint32(10).fork()).ldelim();
    }
    if (message.source !== undefined) {
      Entity.encode(message.source, writer.uint32(18).fork()).ldelim();
    }
    if (message.target !== undefined) {
      Entity.encode(message.target, writer.uint32(26).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): Destroy {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseDestroy();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.aftermaths.push(Entity.decode(reader, reader.uint32()));
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.source = Entity.decode(reader, reader.uint32());
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.target = Entity.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<Destroy>, I>>(base?: I): Destroy {
    return Destroy.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<Destroy>, I>>(object: I): Destroy {
    const message = createBaseDestroy();
    message.aftermaths = object.aftermaths?.map((e) => Entity.fromPartial(e)) || [];
    message.source = object.source !== undefined && object.source !== null ? Entity.fromPartial(object.source) : undefined;
    message.target = object.target !== undefined && object.target !== null ? Entity.fromPartial(object.target) : undefined;
    return message;
  },
};

function createBaseDraftState(): DraftState {
  return {
    cardsRemaining: 0,
    currentCardChoices: [],
    deckId: "",
    draftIndex: 0,
    heroClass: undefined,
    heroClassChoices: [],
    losses: 0,
    selectedCardIds: [],
    status: 0,
    wins: 0,
  };
}

export const DraftState = {
  encode(message: DraftState, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.cardsRemaining !== 0) {
      writer.uint32(8).int32(message.cardsRemaining);
    }
    for (const v of message.currentCardChoices) {
      Entity.encode(v!, writer.uint32(18).fork()).ldelim();
    }
    if (message.deckId !== "") {
      writer.uint32(26).string(message.deckId);
    }
    if (message.draftIndex !== 0) {
      writer.uint32(32).int32(message.draftIndex);
    }
    if (message.heroClass !== undefined) {
      Entity.encode(message.heroClass, writer.uint32(42).fork()).ldelim();
    }
    for (const v of message.heroClassChoices) {
      Entity.encode(v!, writer.uint32(50).fork()).ldelim();
    }
    if (message.losses !== 0) {
      writer.uint32(56).int32(message.losses);
    }
    for (const v of message.selectedCardIds) {
      writer.uint32(66).string(v!);
    }
    if (message.status !== 0) {
      writer.uint32(72).int32(message.status);
    }
    if (message.wins !== 0) {
      writer.uint32(80).int32(message.wins);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): DraftState {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseDraftState();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.cardsRemaining = reader.int32();
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.currentCardChoices.push(Entity.decode(reader, reader.uint32()));
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.deckId = reader.string();
          continue;
        case 4:
          if (tag !== 32) {
            break;
          }

          message.draftIndex = reader.int32();
          continue;
        case 5:
          if (tag !== 42) {
            break;
          }

          message.heroClass = Entity.decode(reader, reader.uint32());
          continue;
        case 6:
          if (tag !== 50) {
            break;
          }

          message.heroClassChoices.push(Entity.decode(reader, reader.uint32()));
          continue;
        case 7:
          if (tag !== 56) {
            break;
          }

          message.losses = reader.int32();
          continue;
        case 8:
          if (tag !== 66) {
            break;
          }

          message.selectedCardIds.push(reader.string());
          continue;
        case 9:
          if (tag !== 72) {
            break;
          }

          message.status = reader.int32() as any;
          continue;
        case 10:
          if (tag !== 80) {
            break;
          }

          message.wins = reader.int32();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<DraftState>, I>>(base?: I): DraftState {
    return DraftState.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<DraftState>, I>>(object: I): DraftState {
    const message = createBaseDraftState();
    message.cardsRemaining = object.cardsRemaining ?? 0;
    message.currentCardChoices = object.currentCardChoices?.map((e) => Entity.fromPartial(e)) || [];
    message.deckId = object.deckId ?? "";
    message.draftIndex = object.draftIndex ?? 0;
    message.heroClass = object.heroClass !== undefined && object.heroClass !== null ? Entity.fromPartial(object.heroClass) : undefined;
    message.heroClassChoices = object.heroClassChoices?.map((e) => Entity.fromPartial(e)) || [];
    message.losses = object.losses ?? 0;
    message.selectedCardIds = object.selectedCardIds?.map((e) => e) || [];
    message.status = object.status ?? 0;
    message.wins = object.wins ?? 0;
    return message;
  },
};

function createBaseDraftsChooseCardRequest(): DraftsChooseCardRequest {
  return { cardIndex: 0 };
}

export const DraftsChooseCardRequest = {
  encode(message: DraftsChooseCardRequest, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.cardIndex !== 0) {
      writer.uint32(8).int32(message.cardIndex);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): DraftsChooseCardRequest {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseDraftsChooseCardRequest();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.cardIndex = reader.int32();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<DraftsChooseCardRequest>, I>>(base?: I): DraftsChooseCardRequest {
    return DraftsChooseCardRequest.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<DraftsChooseCardRequest>, I>>(object: I): DraftsChooseCardRequest {
    const message = createBaseDraftsChooseCardRequest();
    message.cardIndex = object.cardIndex ?? 0;
    return message;
  },
};

function createBaseDraftsChooseHeroRequest(): DraftsChooseHeroRequest {
  return { heroIndex: 0 };
}

export const DraftsChooseHeroRequest = {
  encode(message: DraftsChooseHeroRequest, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.heroIndex !== 0) {
      writer.uint32(8).int32(message.heroIndex);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): DraftsChooseHeroRequest {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseDraftsChooseHeroRequest();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.heroIndex = reader.int32();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<DraftsChooseHeroRequest>, I>>(base?: I): DraftsChooseHeroRequest {
    return DraftsChooseHeroRequest.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<DraftsChooseHeroRequest>, I>>(object: I): DraftsChooseHeroRequest {
    const message = createBaseDraftsChooseHeroRequest();
    message.heroIndex = object.heroIndex ?? 0;
    return message;
  },
};

function createBaseDraftsPostRequest(): DraftsPostRequest {
  return { retireEarly: false, startDraft: false };
}

export const DraftsPostRequest = {
  encode(message: DraftsPostRequest, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.retireEarly === true) {
      writer.uint32(8).bool(message.retireEarly);
    }
    if (message.startDraft === true) {
      writer.uint32(16).bool(message.startDraft);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): DraftsPostRequest {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseDraftsPostRequest();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.retireEarly = reader.bool();
          continue;
        case 2:
          if (tag !== 16) {
            break;
          }

          message.startDraft = reader.bool();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<DraftsPostRequest>, I>>(base?: I): DraftsPostRequest {
    return DraftsPostRequest.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<DraftsPostRequest>, I>>(object: I): DraftsPostRequest {
    const message = createBaseDraftsPostRequest();
    message.retireEarly = object.retireEarly ?? false;
    message.startDraft = object.startDraft ?? false;
    return message;
  },
};

function createBaseEditableCard(): EditableCard {
  return { Id: "", ownerUserId: "", source: "", notification: undefined };
}

export const EditableCard = {
  encode(message: EditableCard, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.Id !== "") {
      writer.uint32(10).string(message.Id);
    }
    if (message.ownerUserId !== "") {
      writer.uint32(18).string(message.ownerUserId);
    }
    if (message.source !== "") {
      writer.uint32(26).string(message.source);
    }
    if (message.notification !== undefined) {
      AddedChangedRemoved.encode(message.notification, writer.uint32(34).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): EditableCard {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseEditableCard();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.Id = reader.string();
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.ownerUserId = reader.string();
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.source = reader.string();
          continue;
        case 4:
          if (tag !== 34) {
            break;
          }

          message.notification = AddedChangedRemoved.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<EditableCard>, I>>(base?: I): EditableCard {
    return EditableCard.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<EditableCard>, I>>(object: I): EditableCard {
    const message = createBaseEditableCard();
    message.Id = object.Id ?? "";
    message.ownerUserId = object.ownerUserId ?? "";
    message.source = object.source ?? "";
    message.notification = object.notification !== undefined && object.notification !== null ? AddedChangedRemoved.fromPartial(object.notification) : undefined;
    return message;
  },
};

function createBaseEmote(): Emote {
  return { entityId: 0, message: 0 };
}

export const Emote = {
  encode(message: Emote, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.entityId !== 0) {
      writer.uint32(8).int32(message.entityId);
    }
    if (message.message !== 0) {
      writer.uint32(16).int32(message.message);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): Emote {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseEmote();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.entityId = reader.int32();
          continue;
        case 2:
          if (tag !== 16) {
            break;
          }

          message.message = reader.int32() as any;
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<Emote>, I>>(base?: I): Emote {
    return Emote.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<Emote>, I>>(object: I): Emote {
    const message = createBaseEmote();
    message.entityId = object.entityId ?? 0;
    message.message = object.message ?? 0;
    return message;
  },
};

function createBaseEntity(): Entity {
  return {
    id: 0,
    armor: undefined,
    art: undefined,
    attack: undefined,
    baseAttack: undefined,
    baseHp: undefined,
    baseManaCost: undefined,
    battlecry: false,
    boardPosition: 0,
    cannotAttack: false,
    cardId: "",
    cardSet: "",
    cardSets: [],
    cardType: 0,
    charge: false,
    charges: undefined,
    chooseOne: false,
    collectible: false,
    combo: false,
    conditionMet: false,
    countUntilCast: undefined,
    deathrattles: false,
    deflect: false,
    description: "",
    destroyed: false,
    discarded: false,
    divineShield: false,
    durability: undefined,
    enchantmentType: "",
    enraged: false,
    entityType: 0,
    fires: undefined,
    frozen: false,
    gameStarted: false,
    gold: false,
    heroClasses: [],
    host: 0,
    hostsTrigger: false,
    hp: undefined,
    immune: false,
    isStartingTurn: false,
    location: undefined,
    lifesteal: false,
    lockedMana: 0,
    mana: 0,
    manaCost: undefined,
    maxHp: undefined,
    maxMana: 0,
    name: "",
    note: "",
    overload: undefined,
    owner: 0,
    permanent: false,
    playable: false,
    poisonous: false,
    rarity: 0,
    roasted: false,
    rush: false,
    silenced: false,
    spellDamage: undefined,
    stealth: false,
    summoningSickness: false,
    taunt: false,
    tooltips: [],
    tribes: [],
    uncensored: false,
    underAura: false,
    untargetableBySpells: false,
    windfury: false,
    extraAttack: undefined,
  };
}

export const Entity = {
  encode(message: Entity, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.id !== 0) {
      writer.uint32(8).int32(message.id);
    }
    if (message.armor !== undefined) {
      writer.uint32(328).int32(message.armor);
    }
    if (message.art !== undefined) {
      Art.encode(message.art, writer.uint32(18).fork()).ldelim();
    }
    if (message.attack !== undefined) {
      writer.uint32(24).int32(message.attack);
    }
    if (message.baseAttack !== undefined) {
      writer.uint32(32).int32(message.baseAttack);
    }
    if (message.baseHp !== undefined) {
      writer.uint32(40).int32(message.baseHp);
    }
    if (message.baseManaCost !== undefined) {
      writer.uint32(48).int32(message.baseManaCost);
    }
    if (message.battlecry === true) {
      writer.uint32(56).bool(message.battlecry);
    }
    if (message.boardPosition !== 0) {
      writer.uint32(64).int32(message.boardPosition);
    }
    if (message.cannotAttack === true) {
      writer.uint32(72).bool(message.cannotAttack);
    }
    if (message.cardId !== "") {
      writer.uint32(82).string(message.cardId);
    }
    if (message.cardSet !== "") {
      writer.uint32(90).string(message.cardSet);
    }
    for (const v of message.cardSets) {
      writer.uint32(98).string(v!);
    }
    if (message.cardType !== 0) {
      writer.uint32(104).int32(message.cardType);
    }
    if (message.charge === true) {
      writer.uint32(112).bool(message.charge);
    }
    if (message.charges !== undefined) {
      writer.uint32(120).int32(message.charges);
    }
    if (message.chooseOne === true) {
      writer.uint32(128).bool(message.chooseOne);
    }
    if (message.collectible === true) {
      writer.uint32(136).bool(message.collectible);
    }
    if (message.combo === true) {
      writer.uint32(144).bool(message.combo);
    }
    if (message.conditionMet === true) {
      writer.uint32(152).bool(message.conditionMet);
    }
    if (message.countUntilCast !== undefined) {
      writer.uint32(160).int32(message.countUntilCast);
    }
    if (message.deathrattles === true) {
      writer.uint32(184).bool(message.deathrattles);
    }
    if (message.deflect === true) {
      writer.uint32(192).bool(message.deflect);
    }
    if (message.description !== "") {
      writer.uint32(202).string(message.description);
    }
    if (message.destroyed === true) {
      writer.uint32(208).bool(message.destroyed);
    }
    if (message.discarded === true) {
      writer.uint32(216).bool(message.discarded);
    }
    if (message.divineShield === true) {
      writer.uint32(224).bool(message.divineShield);
    }
    if (message.durability !== undefined) {
      writer.uint32(232).int32(message.durability);
    }
    if (message.enchantmentType !== "") {
      writer.uint32(242).string(message.enchantmentType);
    }
    if (message.enraged === true) {
      writer.uint32(248).bool(message.enraged);
    }
    if (message.entityType !== 0) {
      writer.uint32(256).int32(message.entityType);
    }
    if (message.fires !== undefined) {
      writer.uint32(264).int32(message.fires);
    }
    if (message.frozen === true) {
      writer.uint32(272).bool(message.frozen);
    }
    if (message.gameStarted === true) {
      writer.uint32(280).bool(message.gameStarted);
    }
    if (message.gold === true) {
      writer.uint32(288).bool(message.gold);
    }
    for (const v of message.heroClasses) {
      writer.uint32(586).string(v!);
    }
    if (message.host !== 0) {
      writer.uint32(304).int32(message.host);
    }
    if (message.hostsTrigger === true) {
      writer.uint32(312).bool(message.hostsTrigger);
    }
    if (message.hp !== undefined) {
      writer.uint32(320).int32(message.hp);
    }
    if (message.immune === true) {
      writer.uint32(336).bool(message.immune);
    }
    if (message.isStartingTurn === true) {
      writer.uint32(344).bool(message.isStartingTurn);
    }
    if (message.location !== undefined) {
      EntityLocation.encode(message.location, writer.uint32(354).fork()).ldelim();
    }
    if (message.lifesteal === true) {
      writer.uint32(360).bool(message.lifesteal);
    }
    if (message.lockedMana !== 0) {
      writer.uint32(368).int32(message.lockedMana);
    }
    if (message.mana !== 0) {
      writer.uint32(376).int32(message.mana);
    }
    if (message.manaCost !== undefined) {
      writer.uint32(384).int32(message.manaCost);
    }
    if (message.maxHp !== undefined) {
      writer.uint32(392).int32(message.maxHp);
    }
    if (message.maxMana !== 0) {
      writer.uint32(400).int32(message.maxMana);
    }
    if (message.name !== "") {
      writer.uint32(410).string(message.name);
    }
    if (message.note !== "") {
      writer.uint32(418).string(message.note);
    }
    if (message.overload !== undefined) {
      writer.uint32(424).int32(message.overload);
    }
    if (message.owner !== 0) {
      writer.uint32(432).int32(message.owner);
    }
    if (message.permanent === true) {
      writer.uint32(440).bool(message.permanent);
    }
    if (message.playable === true) {
      writer.uint32(448).bool(message.playable);
    }
    if (message.poisonous === true) {
      writer.uint32(456).bool(message.poisonous);
    }
    if (message.rarity !== 0) {
      writer.uint32(464).int32(message.rarity);
    }
    if (message.roasted === true) {
      writer.uint32(472).bool(message.roasted);
    }
    if (message.rush === true) {
      writer.uint32(480).bool(message.rush);
    }
    if (message.silenced === true) {
      writer.uint32(488).bool(message.silenced);
    }
    if (message.spellDamage !== undefined) {
      writer.uint32(496).int32(message.spellDamage);
    }
    if (message.stealth === true) {
      writer.uint32(504).bool(message.stealth);
    }
    if (message.summoningSickness === true) {
      writer.uint32(512).bool(message.summoningSickness);
    }
    if (message.taunt === true) {
      writer.uint32(520).bool(message.taunt);
    }
    for (const v of message.tooltips) {
      Tooltip.encode(v!, writer.uint32(530).fork()).ldelim();
    }
    for (const v of message.tribes) {
      writer.uint32(594).string(v!);
    }
    if (message.uncensored === true) {
      writer.uint32(544).bool(message.uncensored);
    }
    if (message.underAura === true) {
      writer.uint32(552).bool(message.underAura);
    }
    if (message.untargetableBySpells === true) {
      writer.uint32(560).bool(message.untargetableBySpells);
    }
    if (message.windfury === true) {
      writer.uint32(568).bool(message.windfury);
    }
    if (message.extraAttack !== undefined) {
      writer.uint32(576).int32(message.extraAttack);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): Entity {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseEntity();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.id = reader.int32();
          continue;
        case 41:
          if (tag !== 328) {
            break;
          }

          message.armor = reader.int32();
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.art = Art.decode(reader, reader.uint32());
          continue;
        case 3:
          if (tag !== 24) {
            break;
          }

          message.attack = reader.int32();
          continue;
        case 4:
          if (tag !== 32) {
            break;
          }

          message.baseAttack = reader.int32();
          continue;
        case 5:
          if (tag !== 40) {
            break;
          }

          message.baseHp = reader.int32();
          continue;
        case 6:
          if (tag !== 48) {
            break;
          }

          message.baseManaCost = reader.int32();
          continue;
        case 7:
          if (tag !== 56) {
            break;
          }

          message.battlecry = reader.bool();
          continue;
        case 8:
          if (tag !== 64) {
            break;
          }

          message.boardPosition = reader.int32();
          continue;
        case 9:
          if (tag !== 72) {
            break;
          }

          message.cannotAttack = reader.bool();
          continue;
        case 10:
          if (tag !== 82) {
            break;
          }

          message.cardId = reader.string();
          continue;
        case 11:
          if (tag !== 90) {
            break;
          }

          message.cardSet = reader.string();
          continue;
        case 12:
          if (tag !== 98) {
            break;
          }

          message.cardSets.push(reader.string());
          continue;
        case 13:
          if (tag !== 104) {
            break;
          }

          message.cardType = reader.int32() as any;
          continue;
        case 14:
          if (tag !== 112) {
            break;
          }

          message.charge = reader.bool();
          continue;
        case 15:
          if (tag !== 120) {
            break;
          }

          message.charges = reader.int32();
          continue;
        case 16:
          if (tag !== 128) {
            break;
          }

          message.chooseOne = reader.bool();
          continue;
        case 17:
          if (tag !== 136) {
            break;
          }

          message.collectible = reader.bool();
          continue;
        case 18:
          if (tag !== 144) {
            break;
          }

          message.combo = reader.bool();
          continue;
        case 19:
          if (tag !== 152) {
            break;
          }

          message.conditionMet = reader.bool();
          continue;
        case 20:
          if (tag !== 160) {
            break;
          }

          message.countUntilCast = reader.int32();
          continue;
        case 23:
          if (tag !== 184) {
            break;
          }

          message.deathrattles = reader.bool();
          continue;
        case 24:
          if (tag !== 192) {
            break;
          }

          message.deflect = reader.bool();
          continue;
        case 25:
          if (tag !== 202) {
            break;
          }

          message.description = reader.string();
          continue;
        case 26:
          if (tag !== 208) {
            break;
          }

          message.destroyed = reader.bool();
          continue;
        case 27:
          if (tag !== 216) {
            break;
          }

          message.discarded = reader.bool();
          continue;
        case 28:
          if (tag !== 224) {
            break;
          }

          message.divineShield = reader.bool();
          continue;
        case 29:
          if (tag !== 232) {
            break;
          }

          message.durability = reader.int32();
          continue;
        case 30:
          if (tag !== 242) {
            break;
          }

          message.enchantmentType = reader.string();
          continue;
        case 31:
          if (tag !== 248) {
            break;
          }

          message.enraged = reader.bool();
          continue;
        case 32:
          if (tag !== 256) {
            break;
          }

          message.entityType = reader.int32() as any;
          continue;
        case 33:
          if (tag !== 264) {
            break;
          }

          message.fires = reader.int32();
          continue;
        case 34:
          if (tag !== 272) {
            break;
          }

          message.frozen = reader.bool();
          continue;
        case 35:
          if (tag !== 280) {
            break;
          }

          message.gameStarted = reader.bool();
          continue;
        case 36:
          if (tag !== 288) {
            break;
          }

          message.gold = reader.bool();
          continue;
        case 73:
          if (tag !== 586) {
            break;
          }

          message.heroClasses.push(reader.string());
          continue;
        case 38:
          if (tag !== 304) {
            break;
          }

          message.host = reader.int32();
          continue;
        case 39:
          if (tag !== 312) {
            break;
          }

          message.hostsTrigger = reader.bool();
          continue;
        case 40:
          if (tag !== 320) {
            break;
          }

          message.hp = reader.int32();
          continue;
        case 42:
          if (tag !== 336) {
            break;
          }

          message.immune = reader.bool();
          continue;
        case 43:
          if (tag !== 344) {
            break;
          }

          message.isStartingTurn = reader.bool();
          continue;
        case 44:
          if (tag !== 354) {
            break;
          }

          message.location = EntityLocation.decode(reader, reader.uint32());
          continue;
        case 45:
          if (tag !== 360) {
            break;
          }

          message.lifesteal = reader.bool();
          continue;
        case 46:
          if (tag !== 368) {
            break;
          }

          message.lockedMana = reader.int32();
          continue;
        case 47:
          if (tag !== 376) {
            break;
          }

          message.mana = reader.int32();
          continue;
        case 48:
          if (tag !== 384) {
            break;
          }

          message.manaCost = reader.int32();
          continue;
        case 49:
          if (tag !== 392) {
            break;
          }

          message.maxHp = reader.int32();
          continue;
        case 50:
          if (tag !== 400) {
            break;
          }

          message.maxMana = reader.int32();
          continue;
        case 51:
          if (tag !== 410) {
            break;
          }

          message.name = reader.string();
          continue;
        case 52:
          if (tag !== 418) {
            break;
          }

          message.note = reader.string();
          continue;
        case 53:
          if (tag !== 424) {
            break;
          }

          message.overload = reader.int32();
          continue;
        case 54:
          if (tag !== 432) {
            break;
          }

          message.owner = reader.int32();
          continue;
        case 55:
          if (tag !== 440) {
            break;
          }

          message.permanent = reader.bool();
          continue;
        case 56:
          if (tag !== 448) {
            break;
          }

          message.playable = reader.bool();
          continue;
        case 57:
          if (tag !== 456) {
            break;
          }

          message.poisonous = reader.bool();
          continue;
        case 58:
          if (tag !== 464) {
            break;
          }

          message.rarity = reader.int32() as any;
          continue;
        case 59:
          if (tag !== 472) {
            break;
          }

          message.roasted = reader.bool();
          continue;
        case 60:
          if (tag !== 480) {
            break;
          }

          message.rush = reader.bool();
          continue;
        case 61:
          if (tag !== 488) {
            break;
          }

          message.silenced = reader.bool();
          continue;
        case 62:
          if (tag !== 496) {
            break;
          }

          message.spellDamage = reader.int32();
          continue;
        case 63:
          if (tag !== 504) {
            break;
          }

          message.stealth = reader.bool();
          continue;
        case 64:
          if (tag !== 512) {
            break;
          }

          message.summoningSickness = reader.bool();
          continue;
        case 65:
          if (tag !== 520) {
            break;
          }

          message.taunt = reader.bool();
          continue;
        case 66:
          if (tag !== 530) {
            break;
          }

          message.tooltips.push(Tooltip.decode(reader, reader.uint32()));
          continue;
        case 74:
          if (tag !== 594) {
            break;
          }

          message.tribes.push(reader.string());
          continue;
        case 68:
          if (tag !== 544) {
            break;
          }

          message.uncensored = reader.bool();
          continue;
        case 69:
          if (tag !== 552) {
            break;
          }

          message.underAura = reader.bool();
          continue;
        case 70:
          if (tag !== 560) {
            break;
          }

          message.untargetableBySpells = reader.bool();
          continue;
        case 71:
          if (tag !== 568) {
            break;
          }

          message.windfury = reader.bool();
          continue;
        case 72:
          if (tag !== 576) {
            break;
          }

          message.extraAttack = reader.int32();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<Entity>, I>>(base?: I): Entity {
    return Entity.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<Entity>, I>>(object: I): Entity {
    const message = createBaseEntity();
    message.id = object.id ?? 0;
    message.armor = object.armor ?? undefined;
    message.art = object.art !== undefined && object.art !== null ? Art.fromPartial(object.art) : undefined;
    message.attack = object.attack ?? undefined;
    message.baseAttack = object.baseAttack ?? undefined;
    message.baseHp = object.baseHp ?? undefined;
    message.baseManaCost = object.baseManaCost ?? undefined;
    message.battlecry = object.battlecry ?? false;
    message.boardPosition = object.boardPosition ?? 0;
    message.cannotAttack = object.cannotAttack ?? false;
    message.cardId = object.cardId ?? "";
    message.cardSet = object.cardSet ?? "";
    message.cardSets = object.cardSets?.map((e) => e) || [];
    message.cardType = object.cardType ?? 0;
    message.charge = object.charge ?? false;
    message.charges = object.charges ?? undefined;
    message.chooseOne = object.chooseOne ?? false;
    message.collectible = object.collectible ?? false;
    message.combo = object.combo ?? false;
    message.conditionMet = object.conditionMet ?? false;
    message.countUntilCast = object.countUntilCast ?? undefined;
    message.deathrattles = object.deathrattles ?? false;
    message.deflect = object.deflect ?? false;
    message.description = object.description ?? "";
    message.destroyed = object.destroyed ?? false;
    message.discarded = object.discarded ?? false;
    message.divineShield = object.divineShield ?? false;
    message.durability = object.durability ?? undefined;
    message.enchantmentType = object.enchantmentType ?? "";
    message.enraged = object.enraged ?? false;
    message.entityType = object.entityType ?? 0;
    message.fires = object.fires ?? undefined;
    message.frozen = object.frozen ?? false;
    message.gameStarted = object.gameStarted ?? false;
    message.gold = object.gold ?? false;
    message.heroClasses = object.heroClasses?.map((e) => e) || [];
    message.host = object.host ?? 0;
    message.hostsTrigger = object.hostsTrigger ?? false;
    message.hp = object.hp ?? undefined;
    message.immune = object.immune ?? false;
    message.isStartingTurn = object.isStartingTurn ?? false;
    message.location = object.location !== undefined && object.location !== null ? EntityLocation.fromPartial(object.location) : undefined;
    message.lifesteal = object.lifesteal ?? false;
    message.lockedMana = object.lockedMana ?? 0;
    message.mana = object.mana ?? 0;
    message.manaCost = object.manaCost ?? undefined;
    message.maxHp = object.maxHp ?? undefined;
    message.maxMana = object.maxMana ?? 0;
    message.name = object.name ?? "";
    message.note = object.note ?? "";
    message.overload = object.overload ?? undefined;
    message.owner = object.owner ?? 0;
    message.permanent = object.permanent ?? false;
    message.playable = object.playable ?? false;
    message.poisonous = object.poisonous ?? false;
    message.rarity = object.rarity ?? 0;
    message.roasted = object.roasted ?? false;
    message.rush = object.rush ?? false;
    message.silenced = object.silenced ?? false;
    message.spellDamage = object.spellDamage ?? undefined;
    message.stealth = object.stealth ?? false;
    message.summoningSickness = object.summoningSickness ?? false;
    message.taunt = object.taunt ?? false;
    message.tooltips = object.tooltips?.map((e) => Tooltip.fromPartial(e)) || [];
    message.tribes = object.tribes?.map((e) => e) || [];
    message.uncensored = object.uncensored ?? false;
    message.underAura = object.underAura ?? false;
    message.untargetableBySpells = object.untargetableBySpells ?? false;
    message.windfury = object.windfury ?? false;
    message.extraAttack = object.extraAttack ?? undefined;
    return message;
  },
};

function createBaseEntityChangeSet(): EntityChangeSet {
  return { ids: [] };
}

export const EntityChangeSet = {
  encode(message: EntityChangeSet, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    writer.uint32(10).fork();
    for (const v of message.ids) {
      writer.int32(v);
    }
    writer.ldelim();
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): EntityChangeSet {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseEntityChangeSet();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag === 8) {
            message.ids.push(reader.int32());

            continue;
          }

          if (tag === 10) {
            const end2 = reader.uint32() + reader.pos;
            while (reader.pos < end2) {
              message.ids.push(reader.int32());
            }

            continue;
          }

          break;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<EntityChangeSet>, I>>(base?: I): EntityChangeSet {
    return EntityChangeSet.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<EntityChangeSet>, I>>(object: I): EntityChangeSet {
    const message = createBaseEntityChangeSet();
    message.ids = object.ids?.map((e) => e) || [];
    return message;
  },
};

function createBaseEntityLocation(): EntityLocation {
  return { index: 0, zone: 0, player: 0 };
}

export const EntityLocation = {
  encode(message: EntityLocation, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.index !== 0) {
      writer.uint32(8).int32(message.index);
    }
    if (message.zone !== 0) {
      writer.uint32(16).int32(message.zone);
    }
    if (message.player !== 0) {
      writer.uint32(24).int32(message.player);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): EntityLocation {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseEntityLocation();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.index = reader.int32();
          continue;
        case 2:
          if (tag !== 16) {
            break;
          }

          message.zone = reader.int32() as any;
          continue;
        case 3:
          if (tag !== 24) {
            break;
          }

          message.player = reader.int32();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<EntityLocation>, I>>(base?: I): EntityLocation {
    return EntityLocation.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<EntityLocation>, I>>(object: I): EntityLocation {
    const message = createBaseEntityLocation();
    message.index = object.index ?? 0;
    message.zone = object.zone ?? 0;
    message.player = object.player ?? 0;
    return message;
  },
};

function createBaseEnvelope(): Envelope {
  return {};
}

export const Envelope = {
  encode(_: Envelope, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): Envelope {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseEnvelope();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<Envelope>, I>>(base?: I): Envelope {
    return Envelope.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<Envelope>, I>>(_: I): Envelope {
    const message = createBaseEnvelope();
    return message;
  },
};

function createBaseEnvelope_GameMessage(): Envelope_GameMessage {
  return { clientToServer: undefined, serverToClient: undefined };
}

export const Envelope_GameMessage = {
  encode(message: Envelope_GameMessage, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.clientToServer !== undefined) {
      ClientToServerMessage.encode(message.clientToServer, writer.uint32(10).fork()).ldelim();
    }
    if (message.serverToClient !== undefined) {
      ServerToClientMessage.encode(message.serverToClient, writer.uint32(18).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): Envelope_GameMessage {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseEnvelope_GameMessage();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.clientToServer = ClientToServerMessage.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.serverToClient = ServerToClientMessage.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<Envelope_GameMessage>, I>>(base?: I): Envelope_GameMessage {
    return Envelope_GameMessage.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<Envelope_GameMessage>, I>>(object: I): Envelope_GameMessage {
    const message = createBaseEnvelope_GameMessage();
    message.clientToServer = object.clientToServer !== undefined && object.clientToServer !== null ? ClientToServerMessage.fromPartial(object.clientToServer) : undefined;
    message.serverToClient = object.serverToClient !== undefined && object.serverToClient !== null ? ServerToClientMessage.fromPartial(object.serverToClient) : undefined;
    return message;
  },
};

function createBaseEnvelope_MethodMessage(): Envelope_MethodMessage {
  return {
    deleteCard: undefined,
    dequeue: undefined,
    enqueue: undefined,
    methodId: "",
    putCard: undefined,
    sendMessage: undefined,
  };
}

export const Envelope_MethodMessage = {
  encode(message: Envelope_MethodMessage, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.deleteCard !== undefined) {
      Envelope_MethodMessage_DeleteCardMessage.encode(message.deleteCard, writer.uint32(10).fork()).ldelim();
    }
    if (message.dequeue !== undefined) {
      Envelope_MethodMessage_DequeueMessage.encode(message.dequeue, writer.uint32(18).fork()).ldelim();
    }
    if (message.enqueue !== undefined) {
      MatchmakingQueuePutRequest.encode(message.enqueue, writer.uint32(26).fork()).ldelim();
    }
    if (message.methodId !== "") {
      writer.uint32(34).string(message.methodId);
    }
    if (message.putCard !== undefined) {
      Envelope_MethodMessage_PutCardMessage.encode(message.putCard, writer.uint32(42).fork()).ldelim();
    }
    if (message.sendMessage !== undefined) {
      Envelope_MethodMessage_SendMessageMessage.encode(message.sendMessage, writer.uint32(50).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): Envelope_MethodMessage {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseEnvelope_MethodMessage();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.deleteCard = Envelope_MethodMessage_DeleteCardMessage.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.dequeue = Envelope_MethodMessage_DequeueMessage.decode(reader, reader.uint32());
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.enqueue = MatchmakingQueuePutRequest.decode(reader, reader.uint32());
          continue;
        case 4:
          if (tag !== 34) {
            break;
          }

          message.methodId = reader.string();
          continue;
        case 5:
          if (tag !== 42) {
            break;
          }

          message.putCard = Envelope_MethodMessage_PutCardMessage.decode(reader, reader.uint32());
          continue;
        case 6:
          if (tag !== 50) {
            break;
          }

          message.sendMessage = Envelope_MethodMessage_SendMessageMessage.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<Envelope_MethodMessage>, I>>(base?: I): Envelope_MethodMessage {
    return Envelope_MethodMessage.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<Envelope_MethodMessage>, I>>(object: I): Envelope_MethodMessage {
    const message = createBaseEnvelope_MethodMessage();
    message.deleteCard = object.deleteCard !== undefined && object.deleteCard !== null ? Envelope_MethodMessage_DeleteCardMessage.fromPartial(object.deleteCard) : undefined;
    message.dequeue = object.dequeue !== undefined && object.dequeue !== null ? Envelope_MethodMessage_DequeueMessage.fromPartial(object.dequeue) : undefined;
    message.enqueue = object.enqueue !== undefined && object.enqueue !== null ? MatchmakingQueuePutRequest.fromPartial(object.enqueue) : undefined;
    message.methodId = object.methodId ?? "";
    message.putCard = object.putCard !== undefined && object.putCard !== null ? Envelope_MethodMessage_PutCardMessage.fromPartial(object.putCard) : undefined;
    message.sendMessage = object.sendMessage !== undefined && object.sendMessage !== null ? Envelope_MethodMessage_SendMessageMessage.fromPartial(object.sendMessage) : undefined;
    return message;
  },
};

function createBaseEnvelope_MethodMessage_DeleteCardMessage(): Envelope_MethodMessage_DeleteCardMessage {
  return { editableCardId: "" };
}

export const Envelope_MethodMessage_DeleteCardMessage = {
  encode(message: Envelope_MethodMessage_DeleteCardMessage, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.editableCardId !== "") {
      writer.uint32(10).string(message.editableCardId);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): Envelope_MethodMessage_DeleteCardMessage {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseEnvelope_MethodMessage_DeleteCardMessage();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.editableCardId = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<Envelope_MethodMessage_DeleteCardMessage>, I>>(base?: I): Envelope_MethodMessage_DeleteCardMessage {
    return Envelope_MethodMessage_DeleteCardMessage.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<Envelope_MethodMessage_DeleteCardMessage>, I>>(object: I): Envelope_MethodMessage_DeleteCardMessage {
    const message = createBaseEnvelope_MethodMessage_DeleteCardMessage();
    message.editableCardId = object.editableCardId ?? "";
    return message;
  },
};

function createBaseEnvelope_MethodMessage_DequeueMessage(): Envelope_MethodMessage_DequeueMessage {
  return { queueId: "" };
}

export const Envelope_MethodMessage_DequeueMessage = {
  encode(message: Envelope_MethodMessage_DequeueMessage, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.queueId !== "") {
      writer.uint32(10).string(message.queueId);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): Envelope_MethodMessage_DequeueMessage {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseEnvelope_MethodMessage_DequeueMessage();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.queueId = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<Envelope_MethodMessage_DequeueMessage>, I>>(base?: I): Envelope_MethodMessage_DequeueMessage {
    return Envelope_MethodMessage_DequeueMessage.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<Envelope_MethodMessage_DequeueMessage>, I>>(object: I): Envelope_MethodMessage_DequeueMessage {
    const message = createBaseEnvelope_MethodMessage_DequeueMessage();
    message.queueId = object.queueId ?? "";
    return message;
  },
};

function createBaseEnvelope_MethodMessage_PutCardMessage(): Envelope_MethodMessage_PutCardMessage {
  return { draw: false, editableCardId: "", source: "" };
}

export const Envelope_MethodMessage_PutCardMessage = {
  encode(message: Envelope_MethodMessage_PutCardMessage, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.draw === true) {
      writer.uint32(8).bool(message.draw);
    }
    if (message.editableCardId !== "") {
      writer.uint32(18).string(message.editableCardId);
    }
    if (message.source !== "") {
      writer.uint32(26).string(message.source);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): Envelope_MethodMessage_PutCardMessage {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseEnvelope_MethodMessage_PutCardMessage();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.draw = reader.bool();
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.editableCardId = reader.string();
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.source = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<Envelope_MethodMessage_PutCardMessage>, I>>(base?: I): Envelope_MethodMessage_PutCardMessage {
    return Envelope_MethodMessage_PutCardMessage.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<Envelope_MethodMessage_PutCardMessage>, I>>(object: I): Envelope_MethodMessage_PutCardMessage {
    const message = createBaseEnvelope_MethodMessage_PutCardMessage();
    message.draw = object.draw ?? false;
    message.editableCardId = object.editableCardId ?? "";
    message.source = object.source ?? "";
    return message;
  },
};

function createBaseEnvelope_MethodMessage_SendMessageMessage(): Envelope_MethodMessage_SendMessageMessage {
  return { conversationId: "", message: "" };
}

export const Envelope_MethodMessage_SendMessageMessage = {
  encode(message: Envelope_MethodMessage_SendMessageMessage, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.conversationId !== "") {
      writer.uint32(10).string(message.conversationId);
    }
    if (message.message !== "") {
      writer.uint32(18).string(message.message);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): Envelope_MethodMessage_SendMessageMessage {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseEnvelope_MethodMessage_SendMessageMessage();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.conversationId = reader.string();
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.message = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<Envelope_MethodMessage_SendMessageMessage>, I>>(base?: I): Envelope_MethodMessage_SendMessageMessage {
    return Envelope_MethodMessage_SendMessageMessage.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<Envelope_MethodMessage_SendMessageMessage>, I>>(object: I): Envelope_MethodMessage_SendMessageMessage {
    const message = createBaseEnvelope_MethodMessage_SendMessageMessage();
    message.conversationId = object.conversationId ?? "";
    message.message = object.message ?? "";
    return message;
  },
};

function createBaseEnvelope_RemovedMessage(): Envelope_RemovedMessage {
  return { id: undefined };
}

export const Envelope_RemovedMessage = {
  encode(message: Envelope_RemovedMessage, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    switch (message.id?.$case) {
      case "editableCardId":
        writer.uint32(10).string(message.id.editableCardId);
        break;
      case "friendId":
        writer.uint32(18).string(message.id.friendId);
        break;
      case "inviteId":
        writer.uint32(26).string(message.id.inviteId);
        break;
      case "matchId":
        writer.uint32(34).string(message.id.matchId);
        break;
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): Envelope_RemovedMessage {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseEnvelope_RemovedMessage();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.id = { $case: "editableCardId", editableCardId: reader.string() };
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.id = { $case: "friendId", friendId: reader.string() };
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.id = { $case: "inviteId", inviteId: reader.string() };
          continue;
        case 4:
          if (tag !== 34) {
            break;
          }

          message.id = { $case: "matchId", matchId: reader.string() };
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<Envelope_RemovedMessage>, I>>(base?: I): Envelope_RemovedMessage {
    return Envelope_RemovedMessage.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<Envelope_RemovedMessage>, I>>(object: I): Envelope_RemovedMessage {
    const message = createBaseEnvelope_RemovedMessage();
    if (object.id?.$case === "editableCardId" && object.id?.editableCardId !== undefined && object.id?.editableCardId !== null) {
      message.id = { $case: "editableCardId", editableCardId: object.id.editableCardId };
    }
    if (object.id?.$case === "friendId" && object.id?.friendId !== undefined && object.id?.friendId !== null) {
      message.id = { $case: "friendId", friendId: object.id.friendId };
    }
    if (object.id?.$case === "inviteId" && object.id?.inviteId !== undefined && object.id?.inviteId !== null) {
      message.id = { $case: "inviteId", inviteId: object.id.inviteId };
    }
    if (object.id?.$case === "matchId" && object.id?.matchId !== undefined && object.id?.matchId !== null) {
      message.id = { $case: "matchId", matchId: object.id.matchId };
    }
    return message;
  },
};

function createBaseEnvelope_ResultMessage(): Envelope_ResultMessage {
  return {};
}

export const Envelope_ResultMessage = {
  encode(_: Envelope_ResultMessage, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): Envelope_ResultMessage {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseEnvelope_ResultMessage();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<Envelope_ResultMessage>, I>>(base?: I): Envelope_ResultMessage {
    return Envelope_ResultMessage.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<Envelope_ResultMessage>, I>>(_: I): Envelope_ResultMessage {
    const message = createBaseEnvelope_ResultMessage();
    return message;
  },
};

function createBaseEnvelope_ResultMessage_PutCardMessage(): Envelope_ResultMessage_PutCardMessage {
  return { cardId: "", cardScriptErrors: [], editableCardId: "" };
}

export const Envelope_ResultMessage_PutCardMessage = {
  encode(message: Envelope_ResultMessage_PutCardMessage, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.cardId !== "") {
      writer.uint32(10).string(message.cardId);
    }
    for (const v of message.cardScriptErrors) {
      writer.uint32(18).string(v!);
    }
    if (message.editableCardId !== "") {
      writer.uint32(26).string(message.editableCardId);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): Envelope_ResultMessage_PutCardMessage {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseEnvelope_ResultMessage_PutCardMessage();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.cardId = reader.string();
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.cardScriptErrors.push(reader.string());
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.editableCardId = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<Envelope_ResultMessage_PutCardMessage>, I>>(base?: I): Envelope_ResultMessage_PutCardMessage {
    return Envelope_ResultMessage_PutCardMessage.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<Envelope_ResultMessage_PutCardMessage>, I>>(object: I): Envelope_ResultMessage_PutCardMessage {
    const message = createBaseEnvelope_ResultMessage_PutCardMessage();
    message.cardId = object.cardId ?? "";
    message.cardScriptErrors = object.cardScriptErrors?.map((e) => e) || [];
    message.editableCardId = object.editableCardId ?? "";
    return message;
  },
};

function createBaseEnvelope_ResultMessage_SendMessageMessage(): Envelope_ResultMessage_SendMessageMessage {
  return { messageId: "" };
}

export const Envelope_ResultMessage_SendMessageMessage = {
  encode(message: Envelope_ResultMessage_SendMessageMessage, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.messageId !== "") {
      writer.uint32(10).string(message.messageId);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): Envelope_ResultMessage_SendMessageMessage {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseEnvelope_ResultMessage_SendMessageMessage();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.messageId = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<Envelope_ResultMessage_SendMessageMessage>, I>>(base?: I): Envelope_ResultMessage_SendMessageMessage {
    return Envelope_ResultMessage_SendMessageMessage.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<Envelope_ResultMessage_SendMessageMessage>, I>>(object: I): Envelope_ResultMessage_SendMessageMessage {
    const message = createBaseEnvelope_ResultMessage_SendMessageMessage();
    message.messageId = object.messageId ?? "";
    return message;
  },
};

function createBaseFont(): Font {
  return { vertex: undefined };
}

export const Font = {
  encode(message: Font, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.vertex !== undefined) {
      Color.encode(message.vertex, writer.uint32(10).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): Font {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseFont();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.vertex = Color.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<Font>, I>>(base?: I): Font {
    return Font.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<Font>, I>>(object: I): Font {
    const message = createBaseFont();
    message.vertex = object.vertex !== undefined && object.vertex !== null ? Color.fromPartial(object.vertex) : undefined;
    return message;
  },
};

function createBaseFriend(): Friend {
  return { friendId: "", friendName: "", presence: 0, since: 0, notification: undefined };
}

export const Friend = {
  encode(message: Friend, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.friendId !== "") {
      writer.uint32(10).string(message.friendId);
    }
    if (message.friendName !== "") {
      writer.uint32(18).string(message.friendName);
    }
    if (message.presence !== 0) {
      writer.uint32(24).int32(message.presence);
    }
    if (message.since !== 0) {
      writer.uint32(32).int64(message.since);
    }
    if (message.notification !== undefined) {
      AddedChangedRemoved.encode(message.notification, writer.uint32(42).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): Friend {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseFriend();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.friendId = reader.string();
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.friendName = reader.string();
          continue;
        case 3:
          if (tag !== 24) {
            break;
          }

          message.presence = reader.int32() as any;
          continue;
        case 4:
          if (tag !== 32) {
            break;
          }

          message.since = longToNumber(reader.int64() as Long);
          continue;
        case 5:
          if (tag !== 42) {
            break;
          }

          message.notification = AddedChangedRemoved.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<Friend>, I>>(base?: I): Friend {
    return Friend.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<Friend>, I>>(object: I): Friend {
    const message = createBaseFriend();
    message.friendId = object.friendId ?? "";
    message.friendName = object.friendName ?? "";
    message.presence = object.presence ?? 0;
    message.since = object.since ?? 0;
    message.notification = object.notification !== undefined && object.notification !== null ? AddedChangedRemoved.fromPartial(object.notification) : undefined;
    return message;
  },
};

function createBaseFriendDeleteRequest(): FriendDeleteRequest {
  return { friendId: "" };
}

export const FriendDeleteRequest = {
  encode(message: FriendDeleteRequest, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.friendId !== "") {
      writer.uint32(10).string(message.friendId);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): FriendDeleteRequest {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseFriendDeleteRequest();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.friendId = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<FriendDeleteRequest>, I>>(base?: I): FriendDeleteRequest {
    return FriendDeleteRequest.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<FriendDeleteRequest>, I>>(object: I): FriendDeleteRequest {
    const message = createBaseFriendDeleteRequest();
    message.friendId = object.friendId ?? "";
    return message;
  },
};

function createBaseFriendPutRequest(): FriendPutRequest {
  return { friendId: "", usernameWithToken: "" };
}

export const FriendPutRequest = {
  encode(message: FriendPutRequest, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.friendId !== "") {
      writer.uint32(10).string(message.friendId);
    }
    if (message.usernameWithToken !== "") {
      writer.uint32(18).string(message.usernameWithToken);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): FriendPutRequest {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseFriendPutRequest();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.friendId = reader.string();
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.usernameWithToken = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<FriendPutRequest>, I>>(base?: I): FriendPutRequest {
    return FriendPutRequest.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<FriendPutRequest>, I>>(object: I): FriendPutRequest {
    const message = createBaseFriendPutRequest();
    message.friendId = object.friendId ?? "";
    message.usernameWithToken = object.usernameWithToken ?? "";
    return message;
  },
};

function createBaseFriendPutResponse(): FriendPutResponse {
  return { friend: undefined };
}

export const FriendPutResponse = {
  encode(message: FriendPutResponse, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.friend !== undefined) {
      Friend.encode(message.friend, writer.uint32(10).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): FriendPutResponse {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseFriendPutResponse();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.friend = Friend.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<FriendPutResponse>, I>>(base?: I): FriendPutResponse {
    return FriendPutResponse.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<FriendPutResponse>, I>>(object: I): FriendPutResponse {
    const message = createBaseFriendPutResponse();
    message.friend = object.friend !== undefined && object.friend !== null ? Friend.fromPartial(object.friend) : undefined;
    return message;
  },
};

function createBaseGameActions(): GameActions {
  return { all: [], compatibility: [] };
}

export const GameActions = {
  encode(message: GameActions, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    for (const v of message.all) {
      SpellAction.encode(v!, writer.uint32(10).fork()).ldelim();
    }
    writer.uint32(18).fork();
    for (const v of message.compatibility) {
      writer.int32(v);
    }
    writer.ldelim();
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): GameActions {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseGameActions();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.all.push(SpellAction.decode(reader, reader.uint32()));
          continue;
        case 2:
          if (tag === 16) {
            message.compatibility.push(reader.int32());

            continue;
          }

          if (tag === 18) {
            const end2 = reader.uint32() + reader.pos;
            while (reader.pos < end2) {
              message.compatibility.push(reader.int32());
            }

            continue;
          }

          break;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<GameActions>, I>>(base?: I): GameActions {
    return GameActions.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<GameActions>, I>>(object: I): GameActions {
    const message = createBaseGameActions();
    message.all = object.all?.map((e) => SpellAction.fromPartial(e)) || [];
    message.compatibility = object.compatibility?.map((e) => e) || [];
    return message;
  },
};

function createBaseGameEvent(): GameEvent {
  return {
    cardEvent: undefined,
    damage: undefined,
    description: "",
    destroy: undefined,
    entityTouched: 0,
    entityUntouched: 0,
    eventType: 0,
    id: 0,
    isPowerHistory: false,
    isSourcePlayerLocal: false,
    isTargetPlayerLocal: false,
    joust: undefined,
    performedGameAction: undefined,
    source: undefined,
    target: undefined,
    targets: [],
    triggerFired: undefined,
    value: undefined,
  };
}

export const GameEvent = {
  encode(message: GameEvent, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.cardEvent !== undefined) {
      CardEvent.encode(message.cardEvent, writer.uint32(10).fork()).ldelim();
    }
    if (message.damage !== undefined) {
      GameEvent_DamageMessage.encode(message.damage, writer.uint32(18).fork()).ldelim();
    }
    if (message.description !== "") {
      writer.uint32(26).string(message.description);
    }
    if (message.destroy !== undefined) {
      GameEvent_DestroyMessage.encode(message.destroy, writer.uint32(34).fork()).ldelim();
    }
    if (message.entityTouched !== 0) {
      writer.uint32(40).int32(message.entityTouched);
    }
    if (message.entityUntouched !== 0) {
      writer.uint32(48).int32(message.entityUntouched);
    }
    if (message.eventType !== 0) {
      writer.uint32(56).int32(message.eventType);
    }
    if (message.id !== 0) {
      writer.uint32(64).int32(message.id);
    }
    if (message.isPowerHistory === true) {
      writer.uint32(72).bool(message.isPowerHistory);
    }
    if (message.isSourcePlayerLocal === true) {
      writer.uint32(80).bool(message.isSourcePlayerLocal);
    }
    if (message.isTargetPlayerLocal === true) {
      writer.uint32(88).bool(message.isTargetPlayerLocal);
    }
    if (message.joust !== undefined) {
      GameEvent_JoustMessage.encode(message.joust, writer.uint32(98).fork()).ldelim();
    }
    if (message.performedGameAction !== undefined) {
      GameEvent_PerformedGameActionMessage.encode(message.performedGameAction, writer.uint32(106).fork()).ldelim();
    }
    if (message.source !== undefined) {
      Entity.encode(message.source, writer.uint32(114).fork()).ldelim();
    }
    if (message.target !== undefined) {
      Entity.encode(message.target, writer.uint32(122).fork()).ldelim();
    }
    for (const v of message.targets) {
      Entity.encode(v!, writer.uint32(130).fork()).ldelim();
    }
    if (message.triggerFired !== undefined) {
      GameEvent_TriggerFiredMessage.encode(message.triggerFired, writer.uint32(138).fork()).ldelim();
    }
    if (message.value !== undefined) {
      writer.uint32(144).int32(message.value);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): GameEvent {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseGameEvent();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.cardEvent = CardEvent.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.damage = GameEvent_DamageMessage.decode(reader, reader.uint32());
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.description = reader.string();
          continue;
        case 4:
          if (tag !== 34) {
            break;
          }

          message.destroy = GameEvent_DestroyMessage.decode(reader, reader.uint32());
          continue;
        case 5:
          if (tag !== 40) {
            break;
          }

          message.entityTouched = reader.int32();
          continue;
        case 6:
          if (tag !== 48) {
            break;
          }

          message.entityUntouched = reader.int32();
          continue;
        case 7:
          if (tag !== 56) {
            break;
          }

          message.eventType = reader.int32() as any;
          continue;
        case 8:
          if (tag !== 64) {
            break;
          }

          message.id = reader.int32();
          continue;
        case 9:
          if (tag !== 72) {
            break;
          }

          message.isPowerHistory = reader.bool();
          continue;
        case 10:
          if (tag !== 80) {
            break;
          }

          message.isSourcePlayerLocal = reader.bool();
          continue;
        case 11:
          if (tag !== 88) {
            break;
          }

          message.isTargetPlayerLocal = reader.bool();
          continue;
        case 12:
          if (tag !== 98) {
            break;
          }

          message.joust = GameEvent_JoustMessage.decode(reader, reader.uint32());
          continue;
        case 13:
          if (tag !== 106) {
            break;
          }

          message.performedGameAction = GameEvent_PerformedGameActionMessage.decode(reader, reader.uint32());
          continue;
        case 14:
          if (tag !== 114) {
            break;
          }

          message.source = Entity.decode(reader, reader.uint32());
          continue;
        case 15:
          if (tag !== 122) {
            break;
          }

          message.target = Entity.decode(reader, reader.uint32());
          continue;
        case 16:
          if (tag !== 130) {
            break;
          }

          message.targets.push(Entity.decode(reader, reader.uint32()));
          continue;
        case 17:
          if (tag !== 138) {
            break;
          }

          message.triggerFired = GameEvent_TriggerFiredMessage.decode(reader, reader.uint32());
          continue;
        case 18:
          if (tag !== 144) {
            break;
          }

          message.value = reader.int32();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<GameEvent>, I>>(base?: I): GameEvent {
    return GameEvent.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<GameEvent>, I>>(object: I): GameEvent {
    const message = createBaseGameEvent();
    message.cardEvent = object.cardEvent !== undefined && object.cardEvent !== null ? CardEvent.fromPartial(object.cardEvent) : undefined;
    message.damage = object.damage !== undefined && object.damage !== null ? GameEvent_DamageMessage.fromPartial(object.damage) : undefined;
    message.description = object.description ?? "";
    message.destroy = object.destroy !== undefined && object.destroy !== null ? GameEvent_DestroyMessage.fromPartial(object.destroy) : undefined;
    message.entityTouched = object.entityTouched ?? 0;
    message.entityUntouched = object.entityUntouched ?? 0;
    message.eventType = object.eventType ?? 0;
    message.id = object.id ?? 0;
    message.isPowerHistory = object.isPowerHistory ?? false;
    message.isSourcePlayerLocal = object.isSourcePlayerLocal ?? false;
    message.isTargetPlayerLocal = object.isTargetPlayerLocal ?? false;
    message.joust = object.joust !== undefined && object.joust !== null ? GameEvent_JoustMessage.fromPartial(object.joust) : undefined;
    message.performedGameAction = object.performedGameAction !== undefined && object.performedGameAction !== null ? GameEvent_PerformedGameActionMessage.fromPartial(object.performedGameAction) : undefined;
    message.source = object.source !== undefined && object.source !== null ? Entity.fromPartial(object.source) : undefined;
    message.target = object.target !== undefined && object.target !== null ? Entity.fromPartial(object.target) : undefined;
    message.targets = object.targets?.map((e) => Entity.fromPartial(e)) || [];
    message.triggerFired = object.triggerFired !== undefined && object.triggerFired !== null ? GameEvent_TriggerFiredMessage.fromPartial(object.triggerFired) : undefined;
    message.value = object.value ?? undefined;
    return message;
  },
};

function createBaseGameEvent_DamageMessage(): GameEvent_DamageMessage {
  return { damageType: 0 };
}

export const GameEvent_DamageMessage = {
  encode(message: GameEvent_DamageMessage, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.damageType !== 0) {
      writer.uint32(8).int32(message.damageType);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): GameEvent_DamageMessage {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseGameEvent_DamageMessage();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.damageType = reader.int32() as any;
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<GameEvent_DamageMessage>, I>>(base?: I): GameEvent_DamageMessage {
    return GameEvent_DamageMessage.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<GameEvent_DamageMessage>, I>>(object: I): GameEvent_DamageMessage {
    const message = createBaseGameEvent_DamageMessage();
    message.damageType = object.damageType ?? 0;
    return message;
  },
};

function createBaseGameEvent_DestroyMessage(): GameEvent_DestroyMessage {
  return { objects: [] };
}

export const GameEvent_DestroyMessage = {
  encode(message: GameEvent_DestroyMessage, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    for (const v of message.objects) {
      Destroy.encode(v!, writer.uint32(10).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): GameEvent_DestroyMessage {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseGameEvent_DestroyMessage();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.objects.push(Destroy.decode(reader, reader.uint32()));
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<GameEvent_DestroyMessage>, I>>(base?: I): GameEvent_DestroyMessage {
    return GameEvent_DestroyMessage.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<GameEvent_DestroyMessage>, I>>(object: I): GameEvent_DestroyMessage {
    const message = createBaseGameEvent_DestroyMessage();
    message.objects = object.objects?.map((e) => Destroy.fromPartial(e)) || [];
    return message;
  },
};

function createBaseGameEvent_JoustMessage(): GameEvent_JoustMessage {
  return { opponentCard: undefined, ownCard: undefined, won: false };
}

export const GameEvent_JoustMessage = {
  encode(message: GameEvent_JoustMessage, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.opponentCard !== undefined) {
      Entity.encode(message.opponentCard, writer.uint32(10).fork()).ldelim();
    }
    if (message.ownCard !== undefined) {
      Entity.encode(message.ownCard, writer.uint32(18).fork()).ldelim();
    }
    if (message.won === true) {
      writer.uint32(24).bool(message.won);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): GameEvent_JoustMessage {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseGameEvent_JoustMessage();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.opponentCard = Entity.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.ownCard = Entity.decode(reader, reader.uint32());
          continue;
        case 3:
          if (tag !== 24) {
            break;
          }

          message.won = reader.bool();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<GameEvent_JoustMessage>, I>>(base?: I): GameEvent_JoustMessage {
    return GameEvent_JoustMessage.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<GameEvent_JoustMessage>, I>>(object: I): GameEvent_JoustMessage {
    const message = createBaseGameEvent_JoustMessage();
    message.opponentCard = object.opponentCard !== undefined && object.opponentCard !== null ? Entity.fromPartial(object.opponentCard) : undefined;
    message.ownCard = object.ownCard !== undefined && object.ownCard !== null ? Entity.fromPartial(object.ownCard) : undefined;
    message.won = object.won ?? false;
    return message;
  },
};

function createBaseGameEvent_PerformedGameActionMessage(): GameEvent_PerformedGameActionMessage {
  return { actionType: 0 };
}

export const GameEvent_PerformedGameActionMessage = {
  encode(message: GameEvent_PerformedGameActionMessage, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.actionType !== 0) {
      writer.uint32(8).int32(message.actionType);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): GameEvent_PerformedGameActionMessage {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseGameEvent_PerformedGameActionMessage();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.actionType = reader.int32() as any;
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<GameEvent_PerformedGameActionMessage>, I>>(base?: I): GameEvent_PerformedGameActionMessage {
    return GameEvent_PerformedGameActionMessage.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<GameEvent_PerformedGameActionMessage>, I>>(object: I): GameEvent_PerformedGameActionMessage {
    const message = createBaseGameEvent_PerformedGameActionMessage();
    message.actionType = object.actionType ?? 0;
    return message;
  },
};

function createBaseGameEvent_TriggerFiredMessage(): GameEvent_TriggerFiredMessage {
  return { triggerSourceId: 0, triggerTargetIds: [] };
}

export const GameEvent_TriggerFiredMessage = {
  encode(message: GameEvent_TriggerFiredMessage, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.triggerSourceId !== 0) {
      writer.uint32(8).int32(message.triggerSourceId);
    }
    writer.uint32(18).fork();
    for (const v of message.triggerTargetIds) {
      writer.int32(v);
    }
    writer.ldelim();
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): GameEvent_TriggerFiredMessage {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseGameEvent_TriggerFiredMessage();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.triggerSourceId = reader.int32();
          continue;
        case 2:
          if (tag === 16) {
            message.triggerTargetIds.push(reader.int32());

            continue;
          }

          if (tag === 18) {
            const end2 = reader.uint32() + reader.pos;
            while (reader.pos < end2) {
              message.triggerTargetIds.push(reader.int32());
            }

            continue;
          }

          break;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<GameEvent_TriggerFiredMessage>, I>>(base?: I): GameEvent_TriggerFiredMessage {
    return GameEvent_TriggerFiredMessage.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<GameEvent_TriggerFiredMessage>, I>>(object: I): GameEvent_TriggerFiredMessage {
    const message = createBaseGameEvent_TriggerFiredMessage();
    message.triggerSourceId = object.triggerSourceId ?? 0;
    message.triggerTargetIds = object.triggerTargetIds?.map((e) => e) || [];
    return message;
  },
};

function createBaseGameOver(): GameOver {
  return { localPlayerWon: false, winningPlayerId: undefined };
}

export const GameOver = {
  encode(message: GameOver, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.localPlayerWon === true) {
      writer.uint32(8).bool(message.localPlayerWon);
    }
    if (message.winningPlayerId !== undefined) {
      writer.uint32(16).int32(message.winningPlayerId);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): GameOver {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseGameOver();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.localPlayerWon = reader.bool();
          continue;
        case 2:
          if (tag !== 16) {
            break;
          }

          message.winningPlayerId = reader.int32();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<GameOver>, I>>(base?: I): GameOver {
    return GameOver.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<GameOver>, I>>(object: I): GameOver {
    const message = createBaseGameOver();
    message.localPlayerWon = object.localPlayerWon ?? false;
    message.winningPlayerId = object.winningPlayerId ?? undefined;
    return message;
  },
};

function createBaseGameState(): GameState {
  return {
    entities: [],
    isLocalPlayerTurn: false,
    powerHistory: [],
    timestamp: 0,
    turnNumber: 0,
    turnState: "",
    hasPowerHistory: false,
  };
}

export const GameState = {
  encode(message: GameState, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    for (const v of message.entities) {
      Entity.encode(v!, writer.uint32(10).fork()).ldelim();
    }
    if (message.isLocalPlayerTurn === true) {
      writer.uint32(16).bool(message.isLocalPlayerTurn);
    }
    for (const v of message.powerHistory) {
      GameEvent.encode(v!, writer.uint32(26).fork()).ldelim();
    }
    if (message.timestamp !== 0) {
      writer.uint32(32).int64(message.timestamp);
    }
    if (message.turnNumber !== 0) {
      writer.uint32(40).int32(message.turnNumber);
    }
    if (message.turnState !== "") {
      writer.uint32(50).string(message.turnState);
    }
    if (message.hasPowerHistory === true) {
      writer.uint32(56).bool(message.hasPowerHistory);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): GameState {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseGameState();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.entities.push(Entity.decode(reader, reader.uint32()));
          continue;
        case 2:
          if (tag !== 16) {
            break;
          }

          message.isLocalPlayerTurn = reader.bool();
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.powerHistory.push(GameEvent.decode(reader, reader.uint32()));
          continue;
        case 4:
          if (tag !== 32) {
            break;
          }

          message.timestamp = longToNumber(reader.int64() as Long);
          continue;
        case 5:
          if (tag !== 40) {
            break;
          }

          message.turnNumber = reader.int32();
          continue;
        case 6:
          if (tag !== 50) {
            break;
          }

          message.turnState = reader.string();
          continue;
        case 7:
          if (tag !== 56) {
            break;
          }

          message.hasPowerHistory = reader.bool();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<GameState>, I>>(base?: I): GameState {
    return GameState.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<GameState>, I>>(object: I): GameState {
    const message = createBaseGameState();
    message.entities = object.entities?.map((e) => Entity.fromPartial(e)) || [];
    message.isLocalPlayerTurn = object.isLocalPlayerTurn ?? false;
    message.powerHistory = object.powerHistory?.map((e) => GameEvent.fromPartial(e)) || [];
    message.timestamp = object.timestamp ?? 0;
    message.turnNumber = object.turnNumber ?? 0;
    message.turnState = object.turnState ?? "";
    message.hasPowerHistory = object.hasPowerHistory ?? false;
    return message;
  },
};

function createBaseGetAccountRequest(): GetAccountRequest {
  return { targetUserId: "" };
}

export const GetAccountRequest = {
  encode(message: GetAccountRequest, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.targetUserId !== "") {
      writer.uint32(10).string(message.targetUserId);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): GetAccountRequest {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseGetAccountRequest();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.targetUserId = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<GetAccountRequest>, I>>(base?: I): GetAccountRequest {
    return GetAccountRequest.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<GetAccountRequest>, I>>(object: I): GetAccountRequest {
    const message = createBaseGetAccountRequest();
    message.targetUserId = object.targetUserId ?? "";
    return message;
  },
};

function createBaseGetAccountsRequest(): GetAccountsRequest {
  return { userIds: [] };
}

export const GetAccountsRequest = {
  encode(message: GetAccountsRequest, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    for (const v of message.userIds) {
      writer.uint32(10).string(v!);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): GetAccountsRequest {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseGetAccountsRequest();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.userIds.push(reader.string());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<GetAccountsRequest>, I>>(base?: I): GetAccountsRequest {
    return GetAccountsRequest.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<GetAccountsRequest>, I>>(object: I): GetAccountsRequest {
    const message = createBaseGetAccountsRequest();
    message.userIds = object.userIds?.map((e) => e) || [];
    return message;
  },
};

function createBaseGetAccountsResponse(): GetAccountsResponse {
  return { accounts: [] };
}

export const GetAccountsResponse = {
  encode(message: GetAccountsResponse, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    for (const v of message.accounts) {
      Account.encode(v!, writer.uint32(10).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): GetAccountsResponse {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseGetAccountsResponse();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.accounts.push(Account.decode(reader, reader.uint32()));
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<GetAccountsResponse>, I>>(base?: I): GetAccountsResponse {
    return GetAccountsResponse.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<GetAccountsResponse>, I>>(object: I): GetAccountsResponse {
    const message = createBaseGetAccountsResponse();
    message.accounts = object.accounts?.map((e) => Account.fromPartial(e)) || [];
    return message;
  },
};

function createBaseGetGameRecordIdsResponse(): GetGameRecordIdsResponse {
  return { gameIds: [] };
}

export const GetGameRecordIdsResponse = {
  encode(message: GetGameRecordIdsResponse, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    for (const v of message.gameIds) {
      writer.uint32(10).string(v!);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): GetGameRecordIdsResponse {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseGetGameRecordIdsResponse();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.gameIds.push(reader.string());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<GetGameRecordIdsResponse>, I>>(base?: I): GetGameRecordIdsResponse {
    return GetGameRecordIdsResponse.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<GetGameRecordIdsResponse>, I>>(object: I): GetGameRecordIdsResponse {
    const message = createBaseGetGameRecordIdsResponse();
    message.gameIds = object.gameIds?.map((e) => e) || [];
    return message;
  },
};

function createBaseGetGameRecordRequest(): GetGameRecordRequest {
  return { gameId: "" };
}

export const GetGameRecordRequest = {
  encode(message: GetGameRecordRequest, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.gameId !== "") {
      writer.uint32(10).string(message.gameId);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): GetGameRecordRequest {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseGetGameRecordRequest();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.gameId = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<GetGameRecordRequest>, I>>(base?: I): GetGameRecordRequest {
    return GetGameRecordRequest.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<GetGameRecordRequest>, I>>(object: I): GetGameRecordRequest {
    const message = createBaseGetGameRecordRequest();
    message.gameId = object.gameId ?? "";
    return message;
  },
};

function createBaseGetGameRecordResponse(): GetGameRecordResponse {
  return { completedAt: 0, completedAtLocalized: "", isBotGame: false, playerNames: [], replay: undefined };
}

export const GetGameRecordResponse = {
  encode(message: GetGameRecordResponse, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.completedAt !== 0) {
      writer.uint32(8).int64(message.completedAt);
    }
    if (message.completedAtLocalized !== "") {
      writer.uint32(18).string(message.completedAtLocalized);
    }
    if (message.isBotGame === true) {
      writer.uint32(24).bool(message.isBotGame);
    }
    for (const v of message.playerNames) {
      writer.uint32(34).string(v!);
    }
    if (message.replay !== undefined) {
      Replay.encode(message.replay, writer.uint32(42).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): GetGameRecordResponse {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseGetGameRecordResponse();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.completedAt = longToNumber(reader.int64() as Long);
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.completedAtLocalized = reader.string();
          continue;
        case 3:
          if (tag !== 24) {
            break;
          }

          message.isBotGame = reader.bool();
          continue;
        case 4:
          if (tag !== 34) {
            break;
          }

          message.playerNames.push(reader.string());
          continue;
        case 5:
          if (tag !== 42) {
            break;
          }

          message.replay = Replay.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<GetGameRecordResponse>, I>>(base?: I): GetGameRecordResponse {
    return GetGameRecordResponse.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<GetGameRecordResponse>, I>>(object: I): GetGameRecordResponse {
    const message = createBaseGetGameRecordResponse();
    message.completedAt = object.completedAt ?? 0;
    message.completedAtLocalized = object.completedAtLocalized ?? "";
    message.isBotGame = object.isBotGame ?? false;
    message.playerNames = object.playerNames?.map((e) => e) || [];
    message.replay = object.replay !== undefined && object.replay !== null ? Replay.fromPartial(object.replay) : undefined;
    return message;
  },
};

function createBaseGetInviteRequest(): GetInviteRequest {
  return { inviteId: "" };
}

export const GetInviteRequest = {
  encode(message: GetInviteRequest, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.inviteId !== "") {
      writer.uint32(10).string(message.inviteId);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): GetInviteRequest {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseGetInviteRequest();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.inviteId = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<GetInviteRequest>, I>>(base?: I): GetInviteRequest {
    return GetInviteRequest.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<GetInviteRequest>, I>>(object: I): GetInviteRequest {
    const message = createBaseGetInviteRequest();
    message.inviteId = object.inviteId ?? "";
    return message;
  },
};

function createBaseInventoryCollection(): InventoryCollection {
  return {
    Id: "",
    deckType: 0,
    format: "",
    heroClass: "",
    inventory: [],
    isStandardDeck: false,
    name: "",
    playerEntityAttributes: [],
    type: 0,
    userId: "",
    validationReport: undefined,
  };
}

export const InventoryCollection = {
  encode(message: InventoryCollection, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.Id !== "") {
      writer.uint32(10).string(message.Id);
    }
    if (message.deckType !== 0) {
      writer.uint32(16).int32(message.deckType);
    }
    if (message.format !== "") {
      writer.uint32(26).string(message.format);
    }
    if (message.heroClass !== "") {
      writer.uint32(34).string(message.heroClass);
    }
    for (const v of message.inventory) {
      CardRecord.encode(v!, writer.uint32(42).fork()).ldelim();
    }
    if (message.isStandardDeck === true) {
      writer.uint32(48).bool(message.isStandardDeck);
    }
    if (message.name !== "") {
      writer.uint32(58).string(message.name);
    }
    for (const v of message.playerEntityAttributes) {
      AttributeValueTuple.encode(v!, writer.uint32(66).fork()).ldelim();
    }
    if (message.type !== 0) {
      writer.uint32(72).int32(message.type);
    }
    if (message.userId !== "") {
      writer.uint32(82).string(message.userId);
    }
    if (message.validationReport !== undefined) {
      ValidationReport.encode(message.validationReport, writer.uint32(90).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): InventoryCollection {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseInventoryCollection();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.Id = reader.string();
          continue;
        case 2:
          if (tag !== 16) {
            break;
          }

          message.deckType = reader.int32() as any;
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.format = reader.string();
          continue;
        case 4:
          if (tag !== 34) {
            break;
          }

          message.heroClass = reader.string();
          continue;
        case 5:
          if (tag !== 42) {
            break;
          }

          message.inventory.push(CardRecord.decode(reader, reader.uint32()));
          continue;
        case 6:
          if (tag !== 48) {
            break;
          }

          message.isStandardDeck = reader.bool();
          continue;
        case 7:
          if (tag !== 58) {
            break;
          }

          message.name = reader.string();
          continue;
        case 8:
          if (tag !== 66) {
            break;
          }

          message.playerEntityAttributes.push(AttributeValueTuple.decode(reader, reader.uint32()));
          continue;
        case 9:
          if (tag !== 72) {
            break;
          }

          message.type = reader.int32() as any;
          continue;
        case 10:
          if (tag !== 82) {
            break;
          }

          message.userId = reader.string();
          continue;
        case 11:
          if (tag !== 90) {
            break;
          }

          message.validationReport = ValidationReport.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<InventoryCollection>, I>>(base?: I): InventoryCollection {
    return InventoryCollection.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<InventoryCollection>, I>>(object: I): InventoryCollection {
    const message = createBaseInventoryCollection();
    message.Id = object.Id ?? "";
    message.deckType = object.deckType ?? 0;
    message.format = object.format ?? "";
    message.heroClass = object.heroClass ?? "";
    message.inventory = object.inventory?.map((e) => CardRecord.fromPartial(e)) || [];
    message.isStandardDeck = object.isStandardDeck ?? false;
    message.name = object.name ?? "";
    message.playerEntityAttributes = object.playerEntityAttributes?.map((e) => AttributeValueTuple.fromPartial(e)) || [];
    message.type = object.type ?? 0;
    message.userId = object.userId ?? "";
    message.validationReport = object.validationReport !== undefined && object.validationReport !== null ? ValidationReport.fromPartial(object.validationReport) : undefined;
    return message;
  },
};

function createBaseInvite(): Invite {
  return {
    Id: "",
    expiresAt: 0,
    friendId: "",
    fromName: "",
    fromUserId: "",
    message: "",
    queueId: "",
    status: 0,
    toName: "",
    toUserId: "",
    notification: undefined,
  };
}

export const Invite = {
  encode(message: Invite, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.Id !== "") {
      writer.uint32(10).string(message.Id);
    }
    if (message.expiresAt !== 0) {
      writer.uint32(16).int64(message.expiresAt);
    }
    if (message.friendId !== "") {
      writer.uint32(26).string(message.friendId);
    }
    if (message.fromName !== "") {
      writer.uint32(34).string(message.fromName);
    }
    if (message.fromUserId !== "") {
      writer.uint32(42).string(message.fromUserId);
    }
    if (message.message !== "") {
      writer.uint32(50).string(message.message);
    }
    if (message.queueId !== "") {
      writer.uint32(58).string(message.queueId);
    }
    if (message.status !== 0) {
      writer.uint32(64).int32(message.status);
    }
    if (message.toName !== "") {
      writer.uint32(74).string(message.toName);
    }
    if (message.toUserId !== "") {
      writer.uint32(82).string(message.toUserId);
    }
    if (message.notification !== undefined) {
      AddedChangedRemoved.encode(message.notification, writer.uint32(90).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): Invite {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseInvite();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.Id = reader.string();
          continue;
        case 2:
          if (tag !== 16) {
            break;
          }

          message.expiresAt = longToNumber(reader.int64() as Long);
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.friendId = reader.string();
          continue;
        case 4:
          if (tag !== 34) {
            break;
          }

          message.fromName = reader.string();
          continue;
        case 5:
          if (tag !== 42) {
            break;
          }

          message.fromUserId = reader.string();
          continue;
        case 6:
          if (tag !== 50) {
            break;
          }

          message.message = reader.string();
          continue;
        case 7:
          if (tag !== 58) {
            break;
          }

          message.queueId = reader.string();
          continue;
        case 8:
          if (tag !== 64) {
            break;
          }

          message.status = reader.int32() as any;
          continue;
        case 9:
          if (tag !== 74) {
            break;
          }

          message.toName = reader.string();
          continue;
        case 10:
          if (tag !== 82) {
            break;
          }

          message.toUserId = reader.string();
          continue;
        case 11:
          if (tag !== 90) {
            break;
          }

          message.notification = AddedChangedRemoved.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<Invite>, I>>(base?: I): Invite {
    return Invite.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<Invite>, I>>(object: I): Invite {
    const message = createBaseInvite();
    message.Id = object.Id ?? "";
    message.expiresAt = object.expiresAt ?? 0;
    message.friendId = object.friendId ?? "";
    message.fromName = object.fromName ?? "";
    message.fromUserId = object.fromUserId ?? "";
    message.message = object.message ?? "";
    message.queueId = object.queueId ?? "";
    message.status = object.status ?? 0;
    message.toName = object.toName ?? "";
    message.toUserId = object.toUserId ?? "";
    message.notification = object.notification !== undefined && object.notification !== null ? AddedChangedRemoved.fromPartial(object.notification) : undefined;
    return message;
  },
};

function createBaseInviteGetResponse(): InviteGetResponse {
  return { invites: [] };
}

export const InviteGetResponse = {
  encode(message: InviteGetResponse, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    for (const v of message.invites) {
      Invite.encode(v!, writer.uint32(10).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): InviteGetResponse {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseInviteGetResponse();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.invites.push(Invite.decode(reader, reader.uint32()));
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<InviteGetResponse>, I>>(base?: I): InviteGetResponse {
    return InviteGetResponse.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<InviteGetResponse>, I>>(object: I): InviteGetResponse {
    const message = createBaseInviteGetResponse();
    message.invites = object.invites?.map((e) => Invite.fromPartial(e)) || [];
    return message;
  },
};

function createBaseInvitePostRequest(): InvitePostRequest {
  return { deckId: "", friend: false, message: "", queueId: "", toUserId: "", toUserNameWithToken: "" };
}

export const InvitePostRequest = {
  encode(message: InvitePostRequest, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.deckId !== "") {
      writer.uint32(10).string(message.deckId);
    }
    if (message.friend === true) {
      writer.uint32(16).bool(message.friend);
    }
    if (message.message !== "") {
      writer.uint32(26).string(message.message);
    }
    if (message.queueId !== "") {
      writer.uint32(34).string(message.queueId);
    }
    if (message.toUserId !== "") {
      writer.uint32(42).string(message.toUserId);
    }
    if (message.toUserNameWithToken !== "") {
      writer.uint32(50).string(message.toUserNameWithToken);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): InvitePostRequest {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseInvitePostRequest();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.deckId = reader.string();
          continue;
        case 2:
          if (tag !== 16) {
            break;
          }

          message.friend = reader.bool();
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.message = reader.string();
          continue;
        case 4:
          if (tag !== 34) {
            break;
          }

          message.queueId = reader.string();
          continue;
        case 5:
          if (tag !== 42) {
            break;
          }

          message.toUserId = reader.string();
          continue;
        case 6:
          if (tag !== 50) {
            break;
          }

          message.toUserNameWithToken = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<InvitePostRequest>, I>>(base?: I): InvitePostRequest {
    return InvitePostRequest.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<InvitePostRequest>, I>>(object: I): InvitePostRequest {
    const message = createBaseInvitePostRequest();
    message.deckId = object.deckId ?? "";
    message.friend = object.friend ?? false;
    message.message = object.message ?? "";
    message.queueId = object.queueId ?? "";
    message.toUserId = object.toUserId ?? "";
    message.toUserNameWithToken = object.toUserNameWithToken ?? "";
    return message;
  },
};

function createBaseInviteResponse(): InviteResponse {
  return { results: undefined };
}

export const InviteResponse = {
  encode(message: InviteResponse, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    switch (message.results?.$case) {
      case "invite":
        Invite.encode(message.results.invite, writer.uint32(10).fork()).ldelim();
        break;
      case "match":
        MatchmakingQueuePutResponse.encode(message.results.match, writer.uint32(18).fork()).ldelim();
        break;
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): InviteResponse {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseInviteResponse();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.results = { $case: "invite", invite: Invite.decode(reader, reader.uint32()) };
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.results = { $case: "match", match: MatchmakingQueuePutResponse.decode(reader, reader.uint32()) };
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<InviteResponse>, I>>(base?: I): InviteResponse {
    return InviteResponse.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<InviteResponse>, I>>(object: I): InviteResponse {
    const message = createBaseInviteResponse();
    if (object.results?.$case === "invite" && object.results?.invite !== undefined && object.results?.invite !== null) {
      message.results = { $case: "invite", invite: Invite.fromPartial(object.results.invite) };
    }
    if (object.results?.$case === "match" && object.results?.match !== undefined && object.results?.match !== null) {
      message.results = { $case: "match", match: MatchmakingQueuePutResponse.fromPartial(object.results.match) };
    }
    return message;
  },
};

function createBaseLoginRequest(): LoginRequest {
  return { email: "", password: "" };
}

export const LoginRequest = {
  encode(message: LoginRequest, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.email !== "") {
      writer.uint32(10).string(message.email);
    }
    if (message.password !== "") {
      writer.uint32(18).string(message.password);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): LoginRequest {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseLoginRequest();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.email = reader.string();
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.password = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<LoginRequest>, I>>(base?: I): LoginRequest {
    return LoginRequest.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<LoginRequest>, I>>(object: I): LoginRequest {
    const message = createBaseLoginRequest();
    message.email = object.email ?? "";
    message.password = object.password ?? "";
    return message;
  },
};

function createBaseLoginResponse(): LoginResponse {
  return { account: undefined, loginToken: "" };
}

export const LoginResponse = {
  encode(message: LoginResponse, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.account !== undefined) {
      Account.encode(message.account, writer.uint32(10).fork()).ldelim();
    }
    if (message.loginToken !== "") {
      writer.uint32(18).string(message.loginToken);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): LoginResponse {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseLoginResponse();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.account = Account.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.loginToken = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<LoginResponse>, I>>(base?: I): LoginResponse {
    return LoginResponse.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<LoginResponse>, I>>(object: I): LoginResponse {
    const message = createBaseLoginResponse();
    message.account = object.account !== undefined && object.account !== null ? Account.fromPartial(object.account) : undefined;
    message.loginToken = object.loginToken ?? "";
    return message;
  },
};

function createBaseMatch(): Match {
  return { Id: "", createdAt: 0 };
}

export const Match = {
  encode(message: Match, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.Id !== "") {
      writer.uint32(10).string(message.Id);
    }
    if (message.createdAt !== 0) {
      writer.uint32(16).int64(message.createdAt);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): Match {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseMatch();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.Id = reader.string();
          continue;
        case 2:
          if (tag !== 16) {
            break;
          }

          message.createdAt = longToNumber(reader.int64() as Long);
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<Match>, I>>(base?: I): Match {
    return Match.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<Match>, I>>(object: I): Match {
    const message = createBaseMatch();
    message.Id = object.Id ?? "";
    message.createdAt = object.createdAt ?? 0;
    return message;
  },
};

function createBaseMatchCancelResponse(): MatchCancelResponse {
  return { isCanceled: false };
}

export const MatchCancelResponse = {
  encode(message: MatchCancelResponse, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.isCanceled === true) {
      writer.uint32(8).bool(message.isCanceled);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): MatchCancelResponse {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseMatchCancelResponse();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.isCanceled = reader.bool();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<MatchCancelResponse>, I>>(base?: I): MatchCancelResponse {
    return MatchCancelResponse.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<MatchCancelResponse>, I>>(object: I): MatchCancelResponse {
    const message = createBaseMatchCancelResponse();
    message.isCanceled = object.isCanceled ?? false;
    return message;
  },
};

function createBaseMatchConcedeResponse(): MatchConcedeResponse {
  return { isConceded: false };
}

export const MatchConcedeResponse = {
  encode(message: MatchConcedeResponse, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.isConceded === true) {
      writer.uint32(8).bool(message.isConceded);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): MatchConcedeResponse {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseMatchConcedeResponse();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.isConceded = reader.bool();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<MatchConcedeResponse>, I>>(base?: I): MatchConcedeResponse {
    return MatchConcedeResponse.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<MatchConcedeResponse>, I>>(object: I): MatchConcedeResponse {
    const message = createBaseMatchConcedeResponse();
    message.isConceded = object.isConceded ?? false;
    return message;
  },
};

function createBaseMatchmakingQueueItem(): MatchmakingQueueItem {
  return { description: "", name: "", queueId: "", requires: undefined, tooltip: "" };
}

export const MatchmakingQueueItem = {
  encode(message: MatchmakingQueueItem, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.description !== "") {
      writer.uint32(10).string(message.description);
    }
    if (message.name !== "") {
      writer.uint32(18).string(message.name);
    }
    if (message.queueId !== "") {
      writer.uint32(26).string(message.queueId);
    }
    if (message.requires !== undefined) {
      MatchmakingQueueItem_RequiresMessage.encode(message.requires, writer.uint32(34).fork()).ldelim();
    }
    if (message.tooltip !== "") {
      writer.uint32(42).string(message.tooltip);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): MatchmakingQueueItem {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseMatchmakingQueueItem();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.description = reader.string();
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.name = reader.string();
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.queueId = reader.string();
          continue;
        case 4:
          if (tag !== 34) {
            break;
          }

          message.requires = MatchmakingQueueItem_RequiresMessage.decode(reader, reader.uint32());
          continue;
        case 5:
          if (tag !== 42) {
            break;
          }

          message.tooltip = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<MatchmakingQueueItem>, I>>(base?: I): MatchmakingQueueItem {
    return MatchmakingQueueItem.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<MatchmakingQueueItem>, I>>(object: I): MatchmakingQueueItem {
    const message = createBaseMatchmakingQueueItem();
    message.description = object.description ?? "";
    message.name = object.name ?? "";
    message.queueId = object.queueId ?? "";
    message.requires = object.requires !== undefined && object.requires !== null ? MatchmakingQueueItem_RequiresMessage.fromPartial(object.requires) : undefined;
    message.tooltip = object.tooltip ?? "";
    return message;
  },
};

function createBaseMatchmakingQueueItem_RequiresMessage(): MatchmakingQueueItem_RequiresMessage {
  return { deck: false, deckChoices: [], deckIdChoices: [], heroClass: false };
}

export const MatchmakingQueueItem_RequiresMessage = {
  encode(message: MatchmakingQueueItem_RequiresMessage, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.deck === true) {
      writer.uint32(8).bool(message.deck);
    }
    for (const v of message.deckChoices) {
      InventoryCollection.encode(v!, writer.uint32(18).fork()).ldelim();
    }
    for (const v of message.deckIdChoices) {
      writer.uint32(26).string(v!);
    }
    if (message.heroClass === true) {
      writer.uint32(32).bool(message.heroClass);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): MatchmakingQueueItem_RequiresMessage {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseMatchmakingQueueItem_RequiresMessage();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.deck = reader.bool();
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.deckChoices.push(InventoryCollection.decode(reader, reader.uint32()));
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.deckIdChoices.push(reader.string());
          continue;
        case 4:
          if (tag !== 32) {
            break;
          }

          message.heroClass = reader.bool();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<MatchmakingQueueItem_RequiresMessage>, I>>(base?: I): MatchmakingQueueItem_RequiresMessage {
    return MatchmakingQueueItem_RequiresMessage.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<MatchmakingQueueItem_RequiresMessage>, I>>(object: I): MatchmakingQueueItem_RequiresMessage {
    const message = createBaseMatchmakingQueueItem_RequiresMessage();
    message.deck = object.deck ?? false;
    message.deckChoices = object.deckChoices?.map((e) => InventoryCollection.fromPartial(e)) || [];
    message.deckIdChoices = object.deckIdChoices?.map((e) => e) || [];
    message.heroClass = object.heroClass ?? false;
    return message;
  },
};

function createBaseMatchmakingQueuePutRequest(): MatchmakingQueuePutRequest {
  return { botDeckId: "", deckId: "", queueId: "", cancel: false };
}

export const MatchmakingQueuePutRequest = {
  encode(message: MatchmakingQueuePutRequest, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.botDeckId !== "") {
      writer.uint32(10).string(message.botDeckId);
    }
    if (message.deckId !== "") {
      writer.uint32(18).string(message.deckId);
    }
    if (message.queueId !== "") {
      writer.uint32(26).string(message.queueId);
    }
    if (message.cancel === true) {
      writer.uint32(32).bool(message.cancel);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): MatchmakingQueuePutRequest {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseMatchmakingQueuePutRequest();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.botDeckId = reader.string();
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.deckId = reader.string();
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.queueId = reader.string();
          continue;
        case 4:
          if (tag !== 32) {
            break;
          }

          message.cancel = reader.bool();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<MatchmakingQueuePutRequest>, I>>(base?: I): MatchmakingQueuePutRequest {
    return MatchmakingQueuePutRequest.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<MatchmakingQueuePutRequest>, I>>(object: I): MatchmakingQueuePutRequest {
    const message = createBaseMatchmakingQueuePutRequest();
    message.botDeckId = object.botDeckId ?? "";
    message.deckId = object.deckId ?? "";
    message.queueId = object.queueId ?? "";
    message.cancel = object.cancel ?? false;
    return message;
  },
};

function createBaseMatchmakingQueuePutResponse(): MatchmakingQueuePutResponse {
  return { retry: undefined, unityConnection: undefined };
}

export const MatchmakingQueuePutResponse = {
  encode(message: MatchmakingQueuePutResponse, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.retry !== undefined) {
      MatchmakingQueuePutRequest.encode(message.retry, writer.uint32(10).fork()).ldelim();
    }
    if (message.unityConnection !== undefined) {
      MatchmakingQueuePutResponseUnityConnection.encode(message.unityConnection, writer.uint32(18).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): MatchmakingQueuePutResponse {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseMatchmakingQueuePutResponse();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.retry = MatchmakingQueuePutRequest.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.unityConnection = MatchmakingQueuePutResponseUnityConnection.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<MatchmakingQueuePutResponse>, I>>(base?: I): MatchmakingQueuePutResponse {
    return MatchmakingQueuePutResponse.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<MatchmakingQueuePutResponse>, I>>(object: I): MatchmakingQueuePutResponse {
    const message = createBaseMatchmakingQueuePutResponse();
    message.retry = object.retry !== undefined && object.retry !== null ? MatchmakingQueuePutRequest.fromPartial(object.retry) : undefined;
    message.unityConnection = object.unityConnection !== undefined && object.unityConnection !== null ? MatchmakingQueuePutResponseUnityConnection.fromPartial(object.unityConnection) : undefined;
    return message;
  },
};

function createBaseMatchmakingQueuePutResponseUnityConnection(): MatchmakingQueuePutResponseUnityConnection {
  return { firstMessage: undefined, url: "", gameId: "" };
}

export const MatchmakingQueuePutResponseUnityConnection = {
  encode(message: MatchmakingQueuePutResponseUnityConnection, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.firstMessage !== undefined) {
      ClientToServerMessage.encode(message.firstMessage, writer.uint32(10).fork()).ldelim();
    }
    if (message.url !== "") {
      writer.uint32(18).string(message.url);
    }
    if (message.gameId !== "") {
      writer.uint32(26).string(message.gameId);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): MatchmakingQueuePutResponseUnityConnection {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseMatchmakingQueuePutResponseUnityConnection();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.firstMessage = ClientToServerMessage.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.url = reader.string();
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.gameId = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<MatchmakingQueuePutResponseUnityConnection>, I>>(base?: I): MatchmakingQueuePutResponseUnityConnection {
    return MatchmakingQueuePutResponseUnityConnection.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<MatchmakingQueuePutResponseUnityConnection>, I>>(object: I): MatchmakingQueuePutResponseUnityConnection {
    const message = createBaseMatchmakingQueuePutResponseUnityConnection();
    message.firstMessage = object.firstMessage !== undefined && object.firstMessage !== null ? ClientToServerMessage.fromPartial(object.firstMessage) : undefined;
    message.url = object.url ?? "";
    message.gameId = object.gameId ?? "";
    return message;
  },
};

function createBaseMatchmakingQueuesResponse(): MatchmakingQueuesResponse {
  return { queues: [] };
}

export const MatchmakingQueuesResponse = {
  encode(message: MatchmakingQueuesResponse, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    for (const v of message.queues) {
      MatchmakingQueueItem.encode(v!, writer.uint32(10).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): MatchmakingQueuesResponse {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseMatchmakingQueuesResponse();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.queues.push(MatchmakingQueueItem.decode(reader, reader.uint32()));
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<MatchmakingQueuesResponse>, I>>(base?: I): MatchmakingQueuesResponse {
    return MatchmakingQueuesResponse.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<MatchmakingQueuesResponse>, I>>(object: I): MatchmakingQueuesResponse {
    const message = createBaseMatchmakingQueuesResponse();
    message.queues = object.queues?.map((e) => MatchmakingQueueItem.fromPartial(e)) || [];
    return message;
  },
};

function createBasePhysicalAttackEvent(): PhysicalAttackEvent {
  return { attacker: undefined, damageDealt: 0, defender: undefined };
}

export const PhysicalAttackEvent = {
  encode(message: PhysicalAttackEvent, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.attacker !== undefined) {
      Entity.encode(message.attacker, writer.uint32(10).fork()).ldelim();
    }
    if (message.damageDealt !== 0) {
      writer.uint32(16).int32(message.damageDealt);
    }
    if (message.defender !== undefined) {
      Entity.encode(message.defender, writer.uint32(26).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): PhysicalAttackEvent {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBasePhysicalAttackEvent();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.attacker = Entity.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 16) {
            break;
          }

          message.damageDealt = reader.int32();
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.defender = Entity.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<PhysicalAttackEvent>, I>>(base?: I): PhysicalAttackEvent {
    return PhysicalAttackEvent.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<PhysicalAttackEvent>, I>>(object: I): PhysicalAttackEvent {
    const message = createBasePhysicalAttackEvent();
    message.attacker = object.attacker !== undefined && object.attacker !== null ? Entity.fromPartial(object.attacker) : undefined;
    message.damageDealt = object.damageDealt ?? 0;
    message.defender = object.defender !== undefined && object.defender !== null ? Entity.fromPartial(object.defender) : undefined;
    return message;
  },
};

function createBasePostInviteRequest(): PostInviteRequest {
  return { request: undefined };
}

export const PostInviteRequest = {
  encode(message: PostInviteRequest, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.request !== undefined) {
      InvitePostRequest.encode(message.request, writer.uint32(10).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): PostInviteRequest {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBasePostInviteRequest();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.request = InvitePostRequest.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<PostInviteRequest>, I>>(base?: I): PostInviteRequest {
    return PostInviteRequest.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<PostInviteRequest>, I>>(object: I): PostInviteRequest {
    const message = createBasePostInviteRequest();
    message.request = object.request !== undefined && object.request !== null ? InvitePostRequest.fromPartial(object.request) : undefined;
    return message;
  },
};

function createBasePostPasswordResetRequest(): PostPasswordResetRequest {
  return { password1: "", password2: "", token: "" };
}

export const PostPasswordResetRequest = {
  encode(message: PostPasswordResetRequest, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.password1 !== "") {
      writer.uint32(10).string(message.password1);
    }
    if (message.password2 !== "") {
      writer.uint32(18).string(message.password2);
    }
    if (message.token !== "") {
      writer.uint32(26).string(message.token);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): PostPasswordResetRequest {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBasePostPasswordResetRequest();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.password1 = reader.string();
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.password2 = reader.string();
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.token = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<PostPasswordResetRequest>, I>>(base?: I): PostPasswordResetRequest {
    return PostPasswordResetRequest.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<PostPasswordResetRequest>, I>>(object: I): PostPasswordResetRequest {
    const message = createBasePostPasswordResetRequest();
    message.password1 = object.password1 ?? "";
    message.password2 = object.password2 ?? "";
    message.token = object.token ?? "";
    return message;
  },
};

function createBasePrefab(): Prefab {
  return { named: "" };
}

export const Prefab = {
  encode(message: Prefab, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.named !== "") {
      writer.uint32(10).string(message.named);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): Prefab {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBasePrefab();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.named = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<Prefab>, I>>(base?: I): Prefab {
    return Prefab.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<Prefab>, I>>(object: I): Prefab {
    const message = createBasePrefab();
    message.named = object.named ?? "";
    return message;
  },
};

function createBaseReplay(): Replay {
  return { deltas: [], gameStates: [] };
}

export const Replay = {
  encode(message: Replay, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    for (const v of message.deltas) {
      ReplayDeltas.encode(v!, writer.uint32(10).fork()).ldelim();
    }
    for (const v of message.gameStates) {
      ReplayGameStates.encode(v!, writer.uint32(18).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): Replay {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseReplay();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.deltas.push(ReplayDeltas.decode(reader, reader.uint32()));
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.gameStates.push(ReplayGameStates.decode(reader, reader.uint32()));
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<Replay>, I>>(base?: I): Replay {
    return Replay.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<Replay>, I>>(object: I): Replay {
    const message = createBaseReplay();
    message.deltas = object.deltas?.map((e) => ReplayDeltas.fromPartial(e)) || [];
    message.gameStates = object.gameStates?.map((e) => ReplayGameStates.fromPartial(e)) || [];
    return message;
  },
};

function createBaseReplayDeltas(): ReplayDeltas {
  return { backward: undefined, forward: undefined };
}

export const ReplayDeltas = {
  encode(message: ReplayDeltas, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.backward !== undefined) {
      EntityChangeSet.encode(message.backward, writer.uint32(10).fork()).ldelim();
    }
    if (message.forward !== undefined) {
      EntityChangeSet.encode(message.forward, writer.uint32(18).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ReplayDeltas {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseReplayDeltas();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.backward = EntityChangeSet.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.forward = EntityChangeSet.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<ReplayDeltas>, I>>(base?: I): ReplayDeltas {
    return ReplayDeltas.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ReplayDeltas>, I>>(object: I): ReplayDeltas {
    const message = createBaseReplayDeltas();
    message.backward = object.backward !== undefined && object.backward !== null ? EntityChangeSet.fromPartial(object.backward) : undefined;
    message.forward = object.forward !== undefined && object.forward !== null ? EntityChangeSet.fromPartial(object.forward) : undefined;
    return message;
  },
};

function createBaseReplayGameStates(): ReplayGameStates {
  return { first: undefined, second: undefined };
}

export const ReplayGameStates = {
  encode(message: ReplayGameStates, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.first !== undefined) {
      GameState.encode(message.first, writer.uint32(10).fork()).ldelim();
    }
    if (message.second !== undefined) {
      GameState.encode(message.second, writer.uint32(18).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ReplayGameStates {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseReplayGameStates();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.first = GameState.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.second = GameState.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<ReplayGameStates>, I>>(base?: I): ReplayGameStates {
    return ReplayGameStates.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ReplayGameStates>, I>>(object: I): ReplayGameStates {
    const message = createBaseReplayGameStates();
    message.first = object.first !== undefined && object.first !== null ? GameState.fromPartial(object.first) : undefined;
    message.second = object.second !== undefined && object.second !== null ? GameState.fromPartial(object.second) : undefined;
    return message;
  },
};

function createBaseServerToClientMessage(): ServerToClientMessage {
  return {
    actions: undefined,
    changes: undefined,
    emote: undefined,
    event: undefined,
    gameOver: undefined,
    gameState: undefined,
    id: "",
    isReplayMessage: false,
    localPlayerId: 0,
    messageType: 0,
    startingCards: [],
    timers: undefined,
  };
}

export const ServerToClientMessage = {
  encode(message: ServerToClientMessage, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.actions !== undefined) {
      GameActions.encode(message.actions, writer.uint32(10).fork()).ldelim();
    }
    if (message.changes !== undefined) {
      EntityChangeSet.encode(message.changes, writer.uint32(18).fork()).ldelim();
    }
    if (message.emote !== undefined) {
      Emote.encode(message.emote, writer.uint32(26).fork()).ldelim();
    }
    if (message.event !== undefined) {
      GameEvent.encode(message.event, writer.uint32(34).fork()).ldelim();
    }
    if (message.gameOver !== undefined) {
      GameOver.encode(message.gameOver, writer.uint32(42).fork()).ldelim();
    }
    if (message.gameState !== undefined) {
      GameState.encode(message.gameState, writer.uint32(50).fork()).ldelim();
    }
    if (message.id !== "") {
      writer.uint32(58).string(message.id);
    }
    if (message.isReplayMessage === true) {
      writer.uint32(64).bool(message.isReplayMessage);
    }
    if (message.localPlayerId !== 0) {
      writer.uint32(72).int32(message.localPlayerId);
    }
    if (message.messageType !== 0) {
      writer.uint32(80).int32(message.messageType);
    }
    for (const v of message.startingCards) {
      Entity.encode(v!, writer.uint32(90).fork()).ldelim();
    }
    if (message.timers !== undefined) {
      Timers.encode(message.timers, writer.uint32(98).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ServerToClientMessage {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseServerToClientMessage();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.actions = GameActions.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.changes = EntityChangeSet.decode(reader, reader.uint32());
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.emote = Emote.decode(reader, reader.uint32());
          continue;
        case 4:
          if (tag !== 34) {
            break;
          }

          message.event = GameEvent.decode(reader, reader.uint32());
          continue;
        case 5:
          if (tag !== 42) {
            break;
          }

          message.gameOver = GameOver.decode(reader, reader.uint32());
          continue;
        case 6:
          if (tag !== 50) {
            break;
          }

          message.gameState = GameState.decode(reader, reader.uint32());
          continue;
        case 7:
          if (tag !== 58) {
            break;
          }

          message.id = reader.string();
          continue;
        case 8:
          if (tag !== 64) {
            break;
          }

          message.isReplayMessage = reader.bool();
          continue;
        case 9:
          if (tag !== 72) {
            break;
          }

          message.localPlayerId = reader.int32();
          continue;
        case 10:
          if (tag !== 80) {
            break;
          }

          message.messageType = reader.int32() as any;
          continue;
        case 11:
          if (tag !== 90) {
            break;
          }

          message.startingCards.push(Entity.decode(reader, reader.uint32()));
          continue;
        case 12:
          if (tag !== 98) {
            break;
          }

          message.timers = Timers.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<ServerToClientMessage>, I>>(base?: I): ServerToClientMessage {
    return ServerToClientMessage.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ServerToClientMessage>, I>>(object: I): ServerToClientMessage {
    const message = createBaseServerToClientMessage();
    message.actions = object.actions !== undefined && object.actions !== null ? GameActions.fromPartial(object.actions) : undefined;
    message.changes = object.changes !== undefined && object.changes !== null ? EntityChangeSet.fromPartial(object.changes) : undefined;
    message.emote = object.emote !== undefined && object.emote !== null ? Emote.fromPartial(object.emote) : undefined;
    message.event = object.event !== undefined && object.event !== null ? GameEvent.fromPartial(object.event) : undefined;
    message.gameOver = object.gameOver !== undefined && object.gameOver !== null ? GameOver.fromPartial(object.gameOver) : undefined;
    message.gameState = object.gameState !== undefined && object.gameState !== null ? GameState.fromPartial(object.gameState) : undefined;
    message.id = object.id ?? "";
    message.isReplayMessage = object.isReplayMessage ?? false;
    message.localPlayerId = object.localPlayerId ?? 0;
    message.messageType = object.messageType ?? 0;
    message.startingCards = object.startingCards?.map((e) => Entity.fromPartial(e)) || [];
    message.timers = object.timers !== undefined && object.timers !== null ? Timers.fromPartial(object.timers) : undefined;
    return message;
  },
};

function createBaseSpanContext(): SpanContext {
  return { data: "" };
}

export const SpanContext = {
  encode(message: SpanContext, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.data !== "") {
      writer.uint32(10).string(message.data);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): SpanContext {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseSpanContext();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.data = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<SpanContext>, I>>(base?: I): SpanContext {
    return SpanContext.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<SpanContext>, I>>(object: I): SpanContext {
    const message = createBaseSpanContext();
    message.data = object.data ?? "";
    return message;
  },
};

function createBaseSpellAction(): SpellAction {
  return {
    action: 0,
    actionType: 0,
    choices: [],
    description: "",
    entity: undefined,
    sourceId: 0,
    targetKeyToActions: [],
    request: "",
  };
}

export const SpellAction = {
  encode(message: SpellAction, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.action !== 0) {
      writer.uint32(8).int32(message.action);
    }
    if (message.actionType !== 0) {
      writer.uint32(16).int32(message.actionType);
    }
    for (const v of message.choices) {
      SpellAction.encode(v!, writer.uint32(26).fork()).ldelim();
    }
    if (message.description !== "") {
      writer.uint32(34).string(message.description);
    }
    if (message.entity !== undefined) {
      Entity.encode(message.entity, writer.uint32(42).fork()).ldelim();
    }
    if (message.sourceId !== 0) {
      writer.uint32(48).int32(message.sourceId);
    }
    for (const v of message.targetKeyToActions) {
      TargetActionPair.encode(v!, writer.uint32(58).fork()).ldelim();
    }
    if (message.request !== "") {
      writer.uint32(66).string(message.request);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): SpellAction {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseSpellAction();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.action = reader.int32();
          continue;
        case 2:
          if (tag !== 16) {
            break;
          }

          message.actionType = reader.int32() as any;
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.choices.push(SpellAction.decode(reader, reader.uint32()));
          continue;
        case 4:
          if (tag !== 34) {
            break;
          }

          message.description = reader.string();
          continue;
        case 5:
          if (tag !== 42) {
            break;
          }

          message.entity = Entity.decode(reader, reader.uint32());
          continue;
        case 6:
          if (tag !== 48) {
            break;
          }

          message.sourceId = reader.int32();
          continue;
        case 7:
          if (tag !== 58) {
            break;
          }

          message.targetKeyToActions.push(TargetActionPair.decode(reader, reader.uint32()));
          continue;
        case 8:
          if (tag !== 66) {
            break;
          }

          message.request = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<SpellAction>, I>>(base?: I): SpellAction {
    return SpellAction.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<SpellAction>, I>>(object: I): SpellAction {
    const message = createBaseSpellAction();
    message.action = object.action ?? 0;
    message.actionType = object.actionType ?? 0;
    message.choices = object.choices?.map((e) => SpellAction.fromPartial(e)) || [];
    message.description = object.description ?? "";
    message.entity = object.entity !== undefined && object.entity !== null ? Entity.fromPartial(object.entity) : undefined;
    message.sourceId = object.sourceId ?? 0;
    message.targetKeyToActions = object.targetKeyToActions?.map((e) => TargetActionPair.fromPartial(e)) || [];
    message.request = object.request ?? "";
    return message;
  },
};

function createBaseSpellsourceException(): SpellsourceException {
  return { message: "" };
}

export const SpellsourceException = {
  encode(message: SpellsourceException, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.message !== "") {
      writer.uint32(10).string(message.message);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): SpellsourceException {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseSpellsourceException();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.message = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<SpellsourceException>, I>>(base?: I): SpellsourceException {
    return SpellsourceException.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<SpellsourceException>, I>>(object: I): SpellsourceException {
    const message = createBaseSpellsourceException();
    message.message = object.message ?? "";
    return message;
  },
};

function createBaseSprite(): Sprite {
  return { named: "", pivot: 0 };
}

export const Sprite = {
  encode(message: Sprite, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.named !== "") {
      writer.uint32(10).string(message.named);
    }
    if (message.pivot !== 0) {
      writer.uint32(16).int32(message.pivot);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): Sprite {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseSprite();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.named = reader.string();
          continue;
        case 2:
          if (tag !== 16) {
            break;
          }

          message.pivot = reader.int32() as any;
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<Sprite>, I>>(base?: I): Sprite {
    return Sprite.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<Sprite>, I>>(object: I): Sprite {
    const message = createBaseSprite();
    message.named = object.named ?? "";
    message.pivot = object.pivot ?? 0;
    return message;
  },
};

function createBaseTargetActionPair(): TargetActionPair {
  return { action: 0, friendlyBattlefieldIndex: 0, target: 0 };
}

export const TargetActionPair = {
  encode(message: TargetActionPair, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.action !== 0) {
      writer.uint32(8).int32(message.action);
    }
    if (message.friendlyBattlefieldIndex !== 0) {
      writer.uint32(16).int32(message.friendlyBattlefieldIndex);
    }
    if (message.target !== 0) {
      writer.uint32(24).int32(message.target);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): TargetActionPair {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseTargetActionPair();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.action = reader.int32();
          continue;
        case 2:
          if (tag !== 16) {
            break;
          }

          message.friendlyBattlefieldIndex = reader.int32();
          continue;
        case 3:
          if (tag !== 24) {
            break;
          }

          message.target = reader.int32();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<TargetActionPair>, I>>(base?: I): TargetActionPair {
    return TargetActionPair.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<TargetActionPair>, I>>(object: I): TargetActionPair {
    const message = createBaseTargetActionPair();
    message.action = object.action ?? 0;
    message.friendlyBattlefieldIndex = object.friendlyBattlefieldIndex ?? 0;
    message.target = object.target ?? 0;
    return message;
  },
};

function createBaseTimers(): Timers {
  return { millisRemaining: 0 };
}

export const Timers = {
  encode(message: Timers, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.millisRemaining !== 0) {
      writer.uint32(8).int64(message.millisRemaining);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): Timers {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseTimers();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.millisRemaining = longToNumber(reader.int64() as Long);
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<Timers>, I>>(base?: I): Timers {
    return Timers.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<Timers>, I>>(object: I): Timers {
    const message = createBaseTimers();
    message.millisRemaining = object.millisRemaining ?? 0;
    return message;
  },
};

function createBaseTooltip(): Tooltip {
  return { keywords: [], text: "" };
}

export const Tooltip = {
  encode(message: Tooltip, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    for (const v of message.keywords) {
      writer.uint32(10).string(v!);
    }
    if (message.text !== "") {
      writer.uint32(18).string(message.text);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): Tooltip {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseTooltip();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.keywords.push(reader.string());
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.text = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<Tooltip>, I>>(base?: I): Tooltip {
    return Tooltip.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<Tooltip>, I>>(object: I): Tooltip {
    const message = createBaseTooltip();
    message.keywords = object.keywords?.map((e) => e) || [];
    message.text = object.text ?? "";
    return message;
  },
};

function createBaseUnfriendResponse(): UnfriendResponse {
  return { deletedFriend: undefined };
}

export const UnfriendResponse = {
  encode(message: UnfriendResponse, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.deletedFriend !== undefined) {
      Friend.encode(message.deletedFriend, writer.uint32(10).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): UnfriendResponse {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseUnfriendResponse();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.deletedFriend = Friend.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<UnfriendResponse>, I>>(base?: I): UnfriendResponse {
    return UnfriendResponse.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<UnfriendResponse>, I>>(object: I): UnfriendResponse {
    const message = createBaseUnfriendResponse();
    message.deletedFriend = object.deletedFriend !== undefined && object.deletedFriend !== null ? Friend.fromPartial(object.deletedFriend) : undefined;
    return message;
  },
};

function createBaseValidationReport(): ValidationReport {
  return { errors: [], valid: false };
}

export const ValidationReport = {
  encode(message: ValidationReport, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    for (const v of message.errors) {
      writer.uint32(10).string(v!);
    }
    if (message.valid === true) {
      writer.uint32(16).bool(message.valid);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ValidationReport {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseValidationReport();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.errors.push(reader.string());
          continue;
        case 2:
          if (tag !== 16) {
            break;
          }

          message.valid = reader.bool();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<ValidationReport>, I>>(base?: I): ValidationReport {
    return ValidationReport.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ValidationReport>, I>>(object: I): ValidationReport {
    const message = createBaseValidationReport();
    message.errors = object.errors?.map((e) => e) || [];
    message.valid = object.valid ?? false;
    return message;
  },
};

export interface Matchmaking {
  enqueue(request: DeepPartial<MatchmakingQueuePutRequest>, metadata?: grpc.Metadata): Observable<MatchmakingQueuePutResponse>;
  /** Removes your client from the matchmaking queue, regardless of which queue it is in. */
  matchmakingDelete(request: DeepPartial<Empty>, metadata?: grpc.Metadata): Promise<MatchCancelResponse>;
  /** Gets a list of queues available for matchmaking. */
  matchmakingGet(request: DeepPartial<Empty>, metadata?: grpc.Metadata): Promise<MatchmakingQueuesResponse>;
}

// @ts-ignore
export class MatchmakingClientImpl implements Matchmaking {
  private readonly rpc: Rpc;

  constructor(rpc: Rpc) {
    this.rpc = rpc;
    this.matchmakingDelete = this.matchmakingDelete.bind(this);
    this.matchmakingGet = this.matchmakingGet.bind(this);
  }

  matchmakingDelete(request: DeepPartial<Empty>, metadata?: grpc.Metadata): Promise<MatchCancelResponse> {
    return this.rpc.unary(MatchmakingMatchmakingDeleteDesc, Empty.fromPartial(request), metadata);
  }

  matchmakingGet(request: DeepPartial<Empty>, metadata?: grpc.Metadata): Promise<MatchmakingQueuesResponse> {
    return this.rpc.unary(MatchmakingMatchmakingGetDesc, Empty.fromPartial(request), metadata);
  }
}

export const MatchmakingDesc = { serviceName: "spellsource.Matchmaking" };

export const MatchmakingMatchmakingDeleteDesc: UnaryMethodDefinitionish = {
  methodName: "MatchmakingDelete",
  service: MatchmakingDesc,
  requestStream: false,
  responseStream: false,
  requestType: {
    serializeBinary() {
      return Empty.encode(this).finish();
    },
  } as any,
  responseType: {
    deserializeBinary(data: Uint8Array) {
      const value = MatchCancelResponse.decode(data);
      return {
        ...value,
        toObject() {
          return value;
        },
      };
    },
  } as any,
};

export const MatchmakingMatchmakingGetDesc: UnaryMethodDefinitionish = {
  methodName: "MatchmakingGet",
  service: MatchmakingDesc,
  requestStream: false,
  responseStream: false,
  requestType: {
    serializeBinary() {
      return Empty.encode(this).finish();
    },
  } as any,
  responseType: {
    deserializeBinary(data: Uint8Array) {
      const value = MatchmakingQueuesResponse.decode(data);
      return {
        ...value,
        toObject() {
          return value;
        },
      };
    },
  } as any,
};

export interface HiddenSwitchSpellsourceAPIService {
  putCard(request: DeepPartial<Envelope_MethodMessage_PutCardMessage>, metadata?: grpc.Metadata): Promise<Envelope_ResultMessage_PutCardMessage>;
  sendMessage(request: DeepPartial<Envelope_MethodMessage_SendMessageMessage>, metadata?: grpc.Metadata): Promise<Envelope_ResultMessage_SendMessageMessage>;
  deleteCard(request: DeepPartial<Envelope_MethodMessage_DeleteCardMessage>, metadata?: grpc.Metadata): Promise<Envelope_RemovedMessage>;
  subscribeFriends(request: DeepPartial<Empty>, metadata?: grpc.Metadata): Observable<Friend>;
  subscribeInvites(request: DeepPartial<Empty>, metadata?: grpc.Metadata): Observable<Invite>;
  subscribeEditableCards(request: DeepPartial<Empty>, metadata?: grpc.Metadata): Observable<EditableCard>;
  subscribeMatch(request: DeepPartial<Empty>, metadata?: grpc.Metadata): Observable<Match>;
  subscribeGame(request: DeepPartial<ClientToServerMessage>, metadata?: grpc.Metadata): Observable<ServerToClientMessage>;
  /**
   * Accepts the invite. If this is an invite to friend the user, this method will perform the friending path for
   * you. If this is an invite to play a match and a matchmaking queue put is specified (with the deck ID), this
   * method will enter you into the special invite matchmaking queue.
   */
  acceptInvite(request: DeepPartial<AcceptInviteRequest>, metadata?: grpc.Metadata): Promise<AcceptInviteResponse>;
  /** Changes your password. Does not log you out after the password is changed. */
  changePassword(request: DeepPartial<ChangePasswordRequest>, metadata?: grpc.Metadata): Promise<ChangePasswordResponse>;
  /** Create an account with Spellsource. */
  createAccount(request: DeepPartial<CreateAccountRequest>, metadata?: grpc.Metadata): Promise<CreateAccountResponse>;
  /** Deletes the specified deck by ID. */
  decksDelete(request: DeepPartial<DecksDeleteRequest>, metadata?: grpc.Metadata): Promise<Empty>;
  /** Gets a deck. Only viewable for the owner of the deck or players in the alliance. */
  decksGet(request: DeepPartial<DecksGetRequest>, metadata?: grpc.Metadata): Promise<DecksGetResponse>;
  /** Gets all the user's decks. */
  decksGetAll(request: DeepPartial<Empty>, metadata?: grpc.Metadata): Promise<DecksGetAllResponse>;
  /** Creates a new deck with optionally specified inventory IDs, a name and a hero class. */
  decksPut(request: DeepPartial<DecksPutRequest>, metadata?: grpc.Metadata): Promise<DecksPutResponse>;
  /**
   * Updates the deck by adding or removing cards, changing the hero class, or renaming the deck.
   *
   * Also gives players the ability to set special gameplay attributes (like the Signature) for the deck.
   */
  decksUpdate(request: DeepPartial<DecksUpdateRequest>, metadata?: grpc.Metadata): Promise<DecksGetResponse>;
  /** Duplicates a deck. Creates a copy for the caller, not the owner */
  duplicateDeck(request: DeepPartial<StringValue>, metadata?: grpc.Metadata): Promise<DecksGetResponse>;
  /** When this user is the sender, cancels the invite. When this user is the recipient, rejects the specified invite. */
  deleteInvite(request: DeepPartial<DeleteInviteRequest>, metadata?: grpc.Metadata): Promise<InviteResponse>;
  /** Make a selection for the given draft index. */
  draftsChooseCard(request: DeepPartial<DraftsChooseCardRequest>, metadata?: grpc.Metadata): Promise<DraftState>;
  /** Choose a hero from your hero selection. */
  draftsChooseHero(request: DeepPartial<DraftsChooseHeroRequest>, metadata?: grpc.Metadata): Promise<DraftState>;
  /** Gets your latest state of the draft. */
  draftsGet(request: DeepPartial<Empty>, metadata?: grpc.Metadata): Promise<DraftState>;
  /** Starts a draft, or make a change to your draft, like retiring early. */
  draftsPost(request: DeepPartial<DraftsPostRequest>, metadata?: grpc.Metadata): Promise<DraftState>;
  /** Removes the friend relationship between two users. */
  friendDelete(request: DeepPartial<FriendDeleteRequest>, metadata?: grpc.Metadata): Promise<UnfriendResponse>;
  /** Adds a specified user to your friend list. */
  friendPut(request: DeepPartial<FriendPutRequest>, metadata?: grpc.Metadata): Promise<FriendPutResponse>;
  /** Get a specific account. Contains more information if the userId matches the requesting user. */
  getAccount(request: DeepPartial<GetAccountRequest>, metadata?: grpc.Metadata): Promise<GetAccountsResponse>;
  /** Get a list of accounts including user profile information. */
  getAccounts(request: DeepPartial<GetAccountsRequest>, metadata?: grpc.Metadata): Promise<GetAccountsResponse>;
  /**
   * Retrieves a record of a game this player played. Games against bots retrieve a complete game record, while games
   * against other players only receive this player's point of view.
   */
  getGameRecord(request: DeepPartial<GetGameRecordRequest>, metadata?: grpc.Metadata): Promise<GetGameRecordResponse>;
  /** Retrieves a list of game IDs corresponding to all the games this player played. */
  getGameRecordIds(request: DeepPartial<Empty>, metadata?: grpc.Metadata): Promise<GetGameRecordIdsResponse>;
  /** Retrieves information about a specific invite, as long as this user is either the sender or recipient. */
  getInvite(request: DeepPartial<GetInviteRequest>, metadata?: grpc.Metadata): Promise<InviteResponse>;
  /** Retrieve all invites where this user is either the sender or recipient. */
  getInvites(request: DeepPartial<Empty>, metadata?: grpc.Metadata): Promise<InviteGetResponse>;
  /** Login with a username and password, receiving an authentication token to use for future sessions. */
  login(request: DeepPartial<LoginRequest>, metadata?: grpc.Metadata): Promise<LoginResponse>;
  /** Send an invite. */
  postInvite(request: DeepPartial<PostInviteRequest>, metadata?: grpc.Metadata): Observable<InviteResponse>;
  /** Provided a valid reset token, resets a user's password. */
  postPasswordReset(request: DeepPartial<PostPasswordResetRequest>, metadata?: grpc.Metadata): Promise<Empty>;
}

// @ts-ignore
export class HiddenSwitchSpellsourceAPIServiceClientImpl implements HiddenSwitchSpellsourceAPIService {
  private readonly rpc: Rpc;

  constructor(rpc: Rpc) {
    this.rpc = rpc;
    this.putCard = this.putCard.bind(this);
    this.sendMessage = this.sendMessage.bind(this);
    this.deleteCard = this.deleteCard.bind(this);
    this.subscribeFriends = this.subscribeFriends.bind(this);
    this.subscribeInvites = this.subscribeInvites.bind(this);
    this.subscribeEditableCards = this.subscribeEditableCards.bind(this);
    this.subscribeMatch = this.subscribeMatch.bind(this);
    this.acceptInvite = this.acceptInvite.bind(this);
    this.changePassword = this.changePassword.bind(this);
    this.createAccount = this.createAccount.bind(this);
    this.decksDelete = this.decksDelete.bind(this);
    this.decksGet = this.decksGet.bind(this);
    this.decksGetAll = this.decksGetAll.bind(this);
    this.decksPut = this.decksPut.bind(this);
    this.decksUpdate = this.decksUpdate.bind(this);
    this.duplicateDeck = this.duplicateDeck.bind(this);
    this.deleteInvite = this.deleteInvite.bind(this);
    this.draftsChooseCard = this.draftsChooseCard.bind(this);
    this.draftsChooseHero = this.draftsChooseHero.bind(this);
    this.draftsGet = this.draftsGet.bind(this);
    this.draftsPost = this.draftsPost.bind(this);
    this.friendDelete = this.friendDelete.bind(this);
    this.friendPut = this.friendPut.bind(this);
    this.getAccount = this.getAccount.bind(this);
    this.getAccounts = this.getAccounts.bind(this);
    this.getGameRecord = this.getGameRecord.bind(this);
    this.getGameRecordIds = this.getGameRecordIds.bind(this);
    this.getInvite = this.getInvite.bind(this);
    this.getInvites = this.getInvites.bind(this);
    this.login = this.login.bind(this);
    this.postInvite = this.postInvite.bind(this);
    this.postPasswordReset = this.postPasswordReset.bind(this);
  }

  putCard(request: DeepPartial<Envelope_MethodMessage_PutCardMessage>, metadata?: grpc.Metadata): Promise<Envelope_ResultMessage_PutCardMessage> {
    return this.rpc.unary(HiddenSwitchSpellsourceAPIServicePutCardDesc, Envelope_MethodMessage_PutCardMessage.fromPartial(request), metadata);
  }

  sendMessage(request: DeepPartial<Envelope_MethodMessage_SendMessageMessage>, metadata?: grpc.Metadata): Promise<Envelope_ResultMessage_SendMessageMessage> {
    return this.rpc.unary(HiddenSwitchSpellsourceAPIServiceSendMessageDesc, Envelope_MethodMessage_SendMessageMessage.fromPartial(request), metadata);
  }

  deleteCard(request: DeepPartial<Envelope_MethodMessage_DeleteCardMessage>, metadata?: grpc.Metadata): Promise<Envelope_RemovedMessage> {
    return this.rpc.unary(HiddenSwitchSpellsourceAPIServiceDeleteCardDesc, Envelope_MethodMessage_DeleteCardMessage.fromPartial(request), metadata);
  }

  subscribeFriends(request: DeepPartial<Empty>, metadata?: grpc.Metadata): Observable<Friend> {
    return this.rpc.invoke(HiddenSwitchSpellsourceAPIServiceSubscribeFriendsDesc, Empty.fromPartial(request), metadata);
  }

  subscribeInvites(request: DeepPartial<Empty>, metadata?: grpc.Metadata): Observable<Invite> {
    return this.rpc.invoke(HiddenSwitchSpellsourceAPIServiceSubscribeInvitesDesc, Empty.fromPartial(request), metadata);
  }

  subscribeEditableCards(request: DeepPartial<Empty>, metadata?: grpc.Metadata): Observable<EditableCard> {
    return this.rpc.invoke(HiddenSwitchSpellsourceAPIServiceSubscribeEditableCardsDesc, Empty.fromPartial(request), metadata);
  }

  subscribeMatch(request: DeepPartial<Empty>, metadata?: grpc.Metadata): Observable<Match> {
    return this.rpc.invoke(HiddenSwitchSpellsourceAPIServiceSubscribeMatchDesc, Empty.fromPartial(request), metadata);
  }

  acceptInvite(request: DeepPartial<AcceptInviteRequest>, metadata?: grpc.Metadata): Promise<AcceptInviteResponse> {
    return this.rpc.unary(HiddenSwitchSpellsourceAPIServiceAcceptInviteDesc, AcceptInviteRequest.fromPartial(request), metadata);
  }

  changePassword(request: DeepPartial<ChangePasswordRequest>, metadata?: grpc.Metadata): Promise<ChangePasswordResponse> {
    return this.rpc.unary(HiddenSwitchSpellsourceAPIServiceChangePasswordDesc, ChangePasswordRequest.fromPartial(request), metadata);
  }

  createAccount(request: DeepPartial<CreateAccountRequest>, metadata?: grpc.Metadata): Promise<CreateAccountResponse> {
    return this.rpc.unary(HiddenSwitchSpellsourceAPIServiceCreateAccountDesc, CreateAccountRequest.fromPartial(request), metadata);
  }

  decksDelete(request: DeepPartial<DecksDeleteRequest>, metadata?: grpc.Metadata): Promise<Empty> {
    return this.rpc.unary(HiddenSwitchSpellsourceAPIServiceDecksDeleteDesc, DecksDeleteRequest.fromPartial(request), metadata);
  }

  decksGet(request: DeepPartial<DecksGetRequest>, metadata?: grpc.Metadata): Promise<DecksGetResponse> {
    return this.rpc.unary(HiddenSwitchSpellsourceAPIServiceDecksGetDesc, DecksGetRequest.fromPartial(request), metadata);
  }

  decksGetAll(request: DeepPartial<Empty>, metadata?: grpc.Metadata): Promise<DecksGetAllResponse> {
    return this.rpc.unary(HiddenSwitchSpellsourceAPIServiceDecksGetAllDesc, Empty.fromPartial(request), metadata);
  }

  decksPut(request: DeepPartial<DecksPutRequest>, metadata?: grpc.Metadata): Promise<DecksPutResponse> {
    return this.rpc.unary(HiddenSwitchSpellsourceAPIServiceDecksPutDesc, DecksPutRequest.fromPartial(request), metadata);
  }

  decksUpdate(request: DeepPartial<DecksUpdateRequest>, metadata?: grpc.Metadata): Promise<DecksGetResponse> {
    return this.rpc.unary(HiddenSwitchSpellsourceAPIServiceDecksUpdateDesc, DecksUpdateRequest.fromPartial(request), metadata);
  }

  duplicateDeck(request: DeepPartial<StringValue>, metadata?: grpc.Metadata): Promise<DecksGetResponse> {
    return this.rpc.unary(HiddenSwitchSpellsourceAPIServiceDuplicateDeckDesc, request, metadata);
  }

  deleteInvite(request: DeepPartial<DeleteInviteRequest>, metadata?: grpc.Metadata): Promise<InviteResponse> {
    return this.rpc.unary(HiddenSwitchSpellsourceAPIServiceDeleteInviteDesc, DeleteInviteRequest.fromPartial(request), metadata);
  }

  draftsChooseCard(request: DeepPartial<DraftsChooseCardRequest>, metadata?: grpc.Metadata): Promise<DraftState> {
    return this.rpc.unary(HiddenSwitchSpellsourceAPIServiceDraftsChooseCardDesc, DraftsChooseCardRequest.fromPartial(request), metadata);
  }

  draftsChooseHero(request: DeepPartial<DraftsChooseHeroRequest>, metadata?: grpc.Metadata): Promise<DraftState> {
    return this.rpc.unary(HiddenSwitchSpellsourceAPIServiceDraftsChooseHeroDesc, DraftsChooseHeroRequest.fromPartial(request), metadata);
  }

  draftsGet(request: DeepPartial<Empty>, metadata?: grpc.Metadata): Promise<DraftState> {
    return this.rpc.unary(HiddenSwitchSpellsourceAPIServiceDraftsGetDesc, Empty.fromPartial(request), metadata);
  }

  draftsPost(request: DeepPartial<DraftsPostRequest>, metadata?: grpc.Metadata): Promise<DraftState> {
    return this.rpc.unary(HiddenSwitchSpellsourceAPIServiceDraftsPostDesc, DraftsPostRequest.fromPartial(request), metadata);
  }

  friendDelete(request: DeepPartial<FriendDeleteRequest>, metadata?: grpc.Metadata): Promise<UnfriendResponse> {
    return this.rpc.unary(HiddenSwitchSpellsourceAPIServiceFriendDeleteDesc, FriendDeleteRequest.fromPartial(request), metadata);
  }

  friendPut(request: DeepPartial<FriendPutRequest>, metadata?: grpc.Metadata): Promise<FriendPutResponse> {
    return this.rpc.unary(HiddenSwitchSpellsourceAPIServiceFriendPutDesc, FriendPutRequest.fromPartial(request), metadata);
  }

  getAccount(request: DeepPartial<GetAccountRequest>, metadata?: grpc.Metadata): Promise<GetAccountsResponse> {
    return this.rpc.unary(HiddenSwitchSpellsourceAPIServiceGetAccountDesc, GetAccountRequest.fromPartial(request), metadata);
  }

  getAccounts(request: DeepPartial<GetAccountsRequest>, metadata?: grpc.Metadata): Promise<GetAccountsResponse> {
    return this.rpc.unary(HiddenSwitchSpellsourceAPIServiceGetAccountsDesc, GetAccountsRequest.fromPartial(request), metadata);
  }

  getGameRecord(request: DeepPartial<GetGameRecordRequest>, metadata?: grpc.Metadata): Promise<GetGameRecordResponse> {
    return this.rpc.unary(HiddenSwitchSpellsourceAPIServiceGetGameRecordDesc, GetGameRecordRequest.fromPartial(request), metadata);
  }

  getGameRecordIds(request: DeepPartial<Empty>, metadata?: grpc.Metadata): Promise<GetGameRecordIdsResponse> {
    return this.rpc.unary(HiddenSwitchSpellsourceAPIServiceGetGameRecordIdsDesc, Empty.fromPartial(request), metadata);
  }

  getInvite(request: DeepPartial<GetInviteRequest>, metadata?: grpc.Metadata): Promise<InviteResponse> {
    return this.rpc.unary(HiddenSwitchSpellsourceAPIServiceGetInviteDesc, GetInviteRequest.fromPartial(request), metadata);
  }

  getInvites(request: DeepPartial<Empty>, metadata?: grpc.Metadata): Promise<InviteGetResponse> {
    return this.rpc.unary(HiddenSwitchSpellsourceAPIServiceGetInvitesDesc, Empty.fromPartial(request), metadata);
  }

  login(request: DeepPartial<LoginRequest>, metadata?: grpc.Metadata): Promise<LoginResponse> {
    return this.rpc.unary(HiddenSwitchSpellsourceAPIServiceLoginDesc, LoginRequest.fromPartial(request), metadata);
  }

  postInvite(request: DeepPartial<PostInviteRequest>, metadata?: grpc.Metadata): Observable<InviteResponse> {
    return this.rpc.invoke(HiddenSwitchSpellsourceAPIServicePostInviteDesc, PostInviteRequest.fromPartial(request), metadata);
  }

  postPasswordReset(request: DeepPartial<PostPasswordResetRequest>, metadata?: grpc.Metadata): Promise<Empty> {
    return this.rpc.unary(HiddenSwitchSpellsourceAPIServicePostPasswordResetDesc, PostPasswordResetRequest.fromPartial(request), metadata);
  }
}

export const HiddenSwitchSpellsourceAPIServiceDesc = { serviceName: "spellsource.HiddenSwitchSpellsourceAPIService" };

export const HiddenSwitchSpellsourceAPIServicePutCardDesc: UnaryMethodDefinitionish = {
  methodName: "PutCard",
  service: HiddenSwitchSpellsourceAPIServiceDesc,
  requestStream: false,
  responseStream: false,
  requestType: {
    serializeBinary() {
      return Envelope_MethodMessage_PutCardMessage.encode(this).finish();
    },
  } as any,
  responseType: {
    deserializeBinary(data: Uint8Array) {
      const value = Envelope_ResultMessage_PutCardMessage.decode(data);
      return {
        ...value,
        toObject() {
          return value;
        },
      };
    },
  } as any,
};

export const HiddenSwitchSpellsourceAPIServiceSendMessageDesc: UnaryMethodDefinitionish = {
  methodName: "SendMessage",
  service: HiddenSwitchSpellsourceAPIServiceDesc,
  requestStream: false,
  responseStream: false,
  requestType: {
    serializeBinary() {
      return Envelope_MethodMessage_SendMessageMessage.encode(this).finish();
    },
  } as any,
  responseType: {
    deserializeBinary(data: Uint8Array) {
      const value = Envelope_ResultMessage_SendMessageMessage.decode(data);
      return {
        ...value,
        toObject() {
          return value;
        },
      };
    },
  } as any,
};

export const HiddenSwitchSpellsourceAPIServiceDeleteCardDesc: UnaryMethodDefinitionish = {
  methodName: "DeleteCard",
  service: HiddenSwitchSpellsourceAPIServiceDesc,
  requestStream: false,
  responseStream: false,
  requestType: {
    serializeBinary() {
      return Envelope_MethodMessage_DeleteCardMessage.encode(this).finish();
    },
  } as any,
  responseType: {
    deserializeBinary(data: Uint8Array) {
      const value = Envelope_RemovedMessage.decode(data);
      return {
        ...value,
        toObject() {
          return value;
        },
      };
    },
  } as any,
};

export const HiddenSwitchSpellsourceAPIServiceSubscribeFriendsDesc: UnaryMethodDefinitionish = {
  methodName: "SubscribeFriends",
  service: HiddenSwitchSpellsourceAPIServiceDesc,
  requestStream: false,
  responseStream: true,
  requestType: {
    serializeBinary() {
      return Empty.encode(this).finish();
    },
  } as any,
  responseType: {
    deserializeBinary(data: Uint8Array) {
      const value = Friend.decode(data);
      return {
        ...value,
        toObject() {
          return value;
        },
      };
    },
  } as any,
};

export const HiddenSwitchSpellsourceAPIServiceSubscribeInvitesDesc: UnaryMethodDefinitionish = {
  methodName: "SubscribeInvites",
  service: HiddenSwitchSpellsourceAPIServiceDesc,
  requestStream: false,
  responseStream: true,
  requestType: {
    serializeBinary() {
      return Empty.encode(this).finish();
    },
  } as any,
  responseType: {
    deserializeBinary(data: Uint8Array) {
      const value = Invite.decode(data);
      return {
        ...value,
        toObject() {
          return value;
        },
      };
    },
  } as any,
};

export const HiddenSwitchSpellsourceAPIServiceSubscribeEditableCardsDesc: UnaryMethodDefinitionish = {
  methodName: "SubscribeEditableCards",
  service: HiddenSwitchSpellsourceAPIServiceDesc,
  requestStream: false,
  responseStream: true,
  requestType: {
    serializeBinary() {
      return Empty.encode(this).finish();
    },
  } as any,
  responseType: {
    deserializeBinary(data: Uint8Array) {
      const value = EditableCard.decode(data);
      return {
        ...value,
        toObject() {
          return value;
        },
      };
    },
  } as any,
};

export const HiddenSwitchSpellsourceAPIServiceSubscribeMatchDesc: UnaryMethodDefinitionish = {
  methodName: "SubscribeMatch",
  service: HiddenSwitchSpellsourceAPIServiceDesc,
  requestStream: false,
  responseStream: true,
  requestType: {
    serializeBinary() {
      return Empty.encode(this).finish();
    },
  } as any,
  responseType: {
    deserializeBinary(data: Uint8Array) {
      const value = Match.decode(data);
      return {
        ...value,
        toObject() {
          return value;
        },
      };
    },
  } as any,
};

export const HiddenSwitchSpellsourceAPIServiceAcceptInviteDesc: UnaryMethodDefinitionish = {
  methodName: "AcceptInvite",
  service: HiddenSwitchSpellsourceAPIServiceDesc,
  requestStream: false,
  responseStream: false,
  requestType: {
    serializeBinary() {
      return AcceptInviteRequest.encode(this).finish();
    },
  } as any,
  responseType: {
    deserializeBinary(data: Uint8Array) {
      const value = AcceptInviteResponse.decode(data);
      return {
        ...value,
        toObject() {
          return value;
        },
      };
    },
  } as any,
};

export const HiddenSwitchSpellsourceAPIServiceChangePasswordDesc: UnaryMethodDefinitionish = {
  methodName: "ChangePassword",
  service: HiddenSwitchSpellsourceAPIServiceDesc,
  requestStream: false,
  responseStream: false,
  requestType: {
    serializeBinary() {
      return ChangePasswordRequest.encode(this).finish();
    },
  } as any,
  responseType: {
    deserializeBinary(data: Uint8Array) {
      const value = ChangePasswordResponse.decode(data);
      return {
        ...value,
        toObject() {
          return value;
        },
      };
    },
  } as any,
};

export const HiddenSwitchSpellsourceAPIServiceCreateAccountDesc: UnaryMethodDefinitionish = {
  methodName: "CreateAccount",
  service: HiddenSwitchSpellsourceAPIServiceDesc,
  requestStream: false,
  responseStream: false,
  requestType: {
    serializeBinary() {
      return CreateAccountRequest.encode(this).finish();
    },
  } as any,
  responseType: {
    deserializeBinary(data: Uint8Array) {
      const value = CreateAccountResponse.decode(data);
      return {
        ...value,
        toObject() {
          return value;
        },
      };
    },
  } as any,
};

export const HiddenSwitchSpellsourceAPIServiceDecksDeleteDesc: UnaryMethodDefinitionish = {
  methodName: "DecksDelete",
  service: HiddenSwitchSpellsourceAPIServiceDesc,
  requestStream: false,
  responseStream: false,
  requestType: {
    serializeBinary() {
      return DecksDeleteRequest.encode(this).finish();
    },
  } as any,
  responseType: {
    deserializeBinary(data: Uint8Array) {
      const value = Empty.decode(data);
      return {
        ...value,
        toObject() {
          return value;
        },
      };
    },
  } as any,
};

export const HiddenSwitchSpellsourceAPIServiceDecksGetDesc: UnaryMethodDefinitionish = {
  methodName: "DecksGet",
  service: HiddenSwitchSpellsourceAPIServiceDesc,
  requestStream: false,
  responseStream: false,
  requestType: {
    serializeBinary() {
      return DecksGetRequest.encode(this).finish();
    },
  } as any,
  responseType: {
    deserializeBinary(data: Uint8Array) {
      const value = DecksGetResponse.decode(data);
      return {
        ...value,
        toObject() {
          return value;
        },
      };
    },
  } as any,
};

export const HiddenSwitchSpellsourceAPIServiceDecksGetAllDesc: UnaryMethodDefinitionish = {
  methodName: "DecksGetAll",
  service: HiddenSwitchSpellsourceAPIServiceDesc,
  requestStream: false,
  responseStream: false,
  requestType: {
    serializeBinary() {
      return Empty.encode(this).finish();
    },
  } as any,
  responseType: {
    deserializeBinary(data: Uint8Array) {
      const value = DecksGetAllResponse.decode(data);
      return {
        ...value,
        toObject() {
          return value;
        },
      };
    },
  } as any,
};

export const HiddenSwitchSpellsourceAPIServiceDecksPutDesc: UnaryMethodDefinitionish = {
  methodName: "DecksPut",
  service: HiddenSwitchSpellsourceAPIServiceDesc,
  requestStream: false,
  responseStream: false,
  requestType: {
    serializeBinary() {
      return DecksPutRequest.encode(this).finish();
    },
  } as any,
  responseType: {
    deserializeBinary(data: Uint8Array) {
      const value = DecksPutResponse.decode(data);
      return {
        ...value,
        toObject() {
          return value;
        },
      };
    },
  } as any,
};

export const HiddenSwitchSpellsourceAPIServiceDecksUpdateDesc: UnaryMethodDefinitionish = {
  methodName: "DecksUpdate",
  service: HiddenSwitchSpellsourceAPIServiceDesc,
  requestStream: false,
  responseStream: false,
  requestType: {
    serializeBinary() {
      return DecksUpdateRequest.encode(this).finish();
    },
  } as any,
  responseType: {
    deserializeBinary(data: Uint8Array) {
      const value = DecksGetResponse.decode(data);
      return {
        ...value,
        toObject() {
          return value;
        },
      };
    },
  } as any,
};

export const HiddenSwitchSpellsourceAPIServiceDuplicateDeckDesc: UnaryMethodDefinitionish = {
  methodName: "DuplicateDeck",
  service: HiddenSwitchSpellsourceAPIServiceDesc,
  requestStream: false,
  responseStream: false,
  requestType: {
    serializeBinary() {
      return StringValue.encode(this).finish();
    },
  } as any,
  responseType: {
    deserializeBinary(data: Uint8Array) {
      const value = DecksGetResponse.decode(data);
      return {
        ...value,
        toObject() {
          return value;
        },
      };
    },
  } as any,
};

export const HiddenSwitchSpellsourceAPIServiceDeleteInviteDesc: UnaryMethodDefinitionish = {
  methodName: "DeleteInvite",
  service: HiddenSwitchSpellsourceAPIServiceDesc,
  requestStream: false,
  responseStream: false,
  requestType: {
    serializeBinary() {
      return DeleteInviteRequest.encode(this).finish();
    },
  } as any,
  responseType: {
    deserializeBinary(data: Uint8Array) {
      const value = InviteResponse.decode(data);
      return {
        ...value,
        toObject() {
          return value;
        },
      };
    },
  } as any,
};

export const HiddenSwitchSpellsourceAPIServiceDraftsChooseCardDesc: UnaryMethodDefinitionish = {
  methodName: "DraftsChooseCard",
  service: HiddenSwitchSpellsourceAPIServiceDesc,
  requestStream: false,
  responseStream: false,
  requestType: {
    serializeBinary() {
      return DraftsChooseCardRequest.encode(this).finish();
    },
  } as any,
  responseType: {
    deserializeBinary(data: Uint8Array) {
      const value = DraftState.decode(data);
      return {
        ...value,
        toObject() {
          return value;
        },
      };
    },
  } as any,
};

export const HiddenSwitchSpellsourceAPIServiceDraftsChooseHeroDesc: UnaryMethodDefinitionish = {
  methodName: "DraftsChooseHero",
  service: HiddenSwitchSpellsourceAPIServiceDesc,
  requestStream: false,
  responseStream: false,
  requestType: {
    serializeBinary() {
      return DraftsChooseHeroRequest.encode(this).finish();
    },
  } as any,
  responseType: {
    deserializeBinary(data: Uint8Array) {
      const value = DraftState.decode(data);
      return {
        ...value,
        toObject() {
          return value;
        },
      };
    },
  } as any,
};

export const HiddenSwitchSpellsourceAPIServiceDraftsGetDesc: UnaryMethodDefinitionish = {
  methodName: "DraftsGet",
  service: HiddenSwitchSpellsourceAPIServiceDesc,
  requestStream: false,
  responseStream: false,
  requestType: {
    serializeBinary() {
      return Empty.encode(this).finish();
    },
  } as any,
  responseType: {
    deserializeBinary(data: Uint8Array) {
      const value = DraftState.decode(data);
      return {
        ...value,
        toObject() {
          return value;
        },
      };
    },
  } as any,
};

export const HiddenSwitchSpellsourceAPIServiceDraftsPostDesc: UnaryMethodDefinitionish = {
  methodName: "DraftsPost",
  service: HiddenSwitchSpellsourceAPIServiceDesc,
  requestStream: false,
  responseStream: false,
  requestType: {
    serializeBinary() {
      return DraftsPostRequest.encode(this).finish();
    },
  } as any,
  responseType: {
    deserializeBinary(data: Uint8Array) {
      const value = DraftState.decode(data);
      return {
        ...value,
        toObject() {
          return value;
        },
      };
    },
  } as any,
};

export const HiddenSwitchSpellsourceAPIServiceFriendDeleteDesc: UnaryMethodDefinitionish = {
  methodName: "FriendDelete",
  service: HiddenSwitchSpellsourceAPIServiceDesc,
  requestStream: false,
  responseStream: false,
  requestType: {
    serializeBinary() {
      return FriendDeleteRequest.encode(this).finish();
    },
  } as any,
  responseType: {
    deserializeBinary(data: Uint8Array) {
      const value = UnfriendResponse.decode(data);
      return {
        ...value,
        toObject() {
          return value;
        },
      };
    },
  } as any,
};

export const HiddenSwitchSpellsourceAPIServiceFriendPutDesc: UnaryMethodDefinitionish = {
  methodName: "FriendPut",
  service: HiddenSwitchSpellsourceAPIServiceDesc,
  requestStream: false,
  responseStream: false,
  requestType: {
    serializeBinary() {
      return FriendPutRequest.encode(this).finish();
    },
  } as any,
  responseType: {
    deserializeBinary(data: Uint8Array) {
      const value = FriendPutResponse.decode(data);
      return {
        ...value,
        toObject() {
          return value;
        },
      };
    },
  } as any,
};

export const HiddenSwitchSpellsourceAPIServiceGetAccountDesc: UnaryMethodDefinitionish = {
  methodName: "GetAccount",
  service: HiddenSwitchSpellsourceAPIServiceDesc,
  requestStream: false,
  responseStream: false,
  requestType: {
    serializeBinary() {
      return GetAccountRequest.encode(this).finish();
    },
  } as any,
  responseType: {
    deserializeBinary(data: Uint8Array) {
      const value = GetAccountsResponse.decode(data);
      return {
        ...value,
        toObject() {
          return value;
        },
      };
    },
  } as any,
};

export const HiddenSwitchSpellsourceAPIServiceGetAccountsDesc: UnaryMethodDefinitionish = {
  methodName: "GetAccounts",
  service: HiddenSwitchSpellsourceAPIServiceDesc,
  requestStream: false,
  responseStream: false,
  requestType: {
    serializeBinary() {
      return GetAccountsRequest.encode(this).finish();
    },
  } as any,
  responseType: {
    deserializeBinary(data: Uint8Array) {
      const value = GetAccountsResponse.decode(data);
      return {
        ...value,
        toObject() {
          return value;
        },
      };
    },
  } as any,
};

export const HiddenSwitchSpellsourceAPIServiceGetGameRecordDesc: UnaryMethodDefinitionish = {
  methodName: "GetGameRecord",
  service: HiddenSwitchSpellsourceAPIServiceDesc,
  requestStream: false,
  responseStream: false,
  requestType: {
    serializeBinary() {
      return GetGameRecordRequest.encode(this).finish();
    },
  } as any,
  responseType: {
    deserializeBinary(data: Uint8Array) {
      const value = GetGameRecordResponse.decode(data);
      return {
        ...value,
        toObject() {
          return value;
        },
      };
    },
  } as any,
};

export const HiddenSwitchSpellsourceAPIServiceGetGameRecordIdsDesc: UnaryMethodDefinitionish = {
  methodName: "GetGameRecordIds",
  service: HiddenSwitchSpellsourceAPIServiceDesc,
  requestStream: false,
  responseStream: false,
  requestType: {
    serializeBinary() {
      return Empty.encode(this).finish();
    },
  } as any,
  responseType: {
    deserializeBinary(data: Uint8Array) {
      const value = GetGameRecordIdsResponse.decode(data);
      return {
        ...value,
        toObject() {
          return value;
        },
      };
    },
  } as any,
};

export const HiddenSwitchSpellsourceAPIServiceGetInviteDesc: UnaryMethodDefinitionish = {
  methodName: "GetInvite",
  service: HiddenSwitchSpellsourceAPIServiceDesc,
  requestStream: false,
  responseStream: false,
  requestType: {
    serializeBinary() {
      return GetInviteRequest.encode(this).finish();
    },
  } as any,
  responseType: {
    deserializeBinary(data: Uint8Array) {
      const value = InviteResponse.decode(data);
      return {
        ...value,
        toObject() {
          return value;
        },
      };
    },
  } as any,
};

export const HiddenSwitchSpellsourceAPIServiceGetInvitesDesc: UnaryMethodDefinitionish = {
  methodName: "GetInvites",
  service: HiddenSwitchSpellsourceAPIServiceDesc,
  requestStream: false,
  responseStream: false,
  requestType: {
    serializeBinary() {
      return Empty.encode(this).finish();
    },
  } as any,
  responseType: {
    deserializeBinary(data: Uint8Array) {
      const value = InviteGetResponse.decode(data);
      return {
        ...value,
        toObject() {
          return value;
        },
      };
    },
  } as any,
};

export const HiddenSwitchSpellsourceAPIServiceLoginDesc: UnaryMethodDefinitionish = {
  methodName: "Login",
  service: HiddenSwitchSpellsourceAPIServiceDesc,
  requestStream: false,
  responseStream: false,
  requestType: {
    serializeBinary() {
      return LoginRequest.encode(this).finish();
    },
  } as any,
  responseType: {
    deserializeBinary(data: Uint8Array) {
      const value = LoginResponse.decode(data);
      return {
        ...value,
        toObject() {
          return value;
        },
      };
    },
  } as any,
};

export const HiddenSwitchSpellsourceAPIServicePostInviteDesc: UnaryMethodDefinitionish = {
  methodName: "PostInvite",
  service: HiddenSwitchSpellsourceAPIServiceDesc,
  requestStream: false,
  responseStream: true,
  requestType: {
    serializeBinary() {
      return PostInviteRequest.encode(this).finish();
    },
  } as any,
  responseType: {
    deserializeBinary(data: Uint8Array) {
      const value = InviteResponse.decode(data);
      return {
        ...value,
        toObject() {
          return value;
        },
      };
    },
  } as any,
};

export const HiddenSwitchSpellsourceAPIServicePostPasswordResetDesc: UnaryMethodDefinitionish = {
  methodName: "PostPasswordReset",
  service: HiddenSwitchSpellsourceAPIServiceDesc,
  requestStream: false,
  responseStream: false,
  requestType: {
    serializeBinary() {
      return PostPasswordResetRequest.encode(this).finish();
    },
  } as any,
  responseType: {
    deserializeBinary(data: Uint8Array) {
      const value = Empty.decode(data);
      return {
        ...value,
        toObject() {
          return value;
        },
      };
    },
  } as any,
};

interface UnaryMethodDefinitionishR extends grpc.UnaryMethodDefinition<any, any> {
  requestStream: any;
  responseStream: any;
}

type UnaryMethodDefinitionish = UnaryMethodDefinitionishR;

interface Rpc {
  unary<T extends UnaryMethodDefinitionish>(methodDesc: T, request: any, metadata: grpc.Metadata | undefined): Promise<any>;
  invoke<T extends UnaryMethodDefinitionish>(methodDesc: T, request: any, metadata: grpc.Metadata | undefined): Observable<any>;
}

export class GrpcWebImpl {
  private host: string;
  private options: {
    transport?: grpc.TransportFactory;
    streamingTransport?: grpc.TransportFactory;
    debug?: boolean;
    metadata?: grpc.Metadata;
    upStreamRetryCodes?: number[];
  };

  constructor(
    host: string,
    options: {
      transport?: grpc.TransportFactory;
      streamingTransport?: grpc.TransportFactory;
      debug?: boolean;
      metadata?: grpc.Metadata;
      upStreamRetryCodes?: number[];
    },
  ) {
    this.host = host;
    this.options = options;
  }

  unary<T extends UnaryMethodDefinitionish>(methodDesc: T, _request: any, metadata: grpc.Metadata | undefined): Promise<any> {
    const request = { ..._request, ...methodDesc.requestType };
    const maybeCombinedMetadata = metadata && this.options.metadata ? new BrowserHeaders({ ...this.options?.metadata.headersMap, ...metadata?.headersMap }) : (metadata ?? this.options.metadata);
    return new Promise((resolve, reject) => {
      grpc.unary(methodDesc, {
        request,
        host: this.host,
        metadata: maybeCombinedMetadata ?? {},
        ...(this.options.transport !== undefined ? { transport: this.options.transport } : {}),
        debug: this.options.debug ?? false,
        onEnd: function (response) {
          if (response.status === grpc.Code.OK) {
            resolve(response.message!.toObject());
          } else {
            const err = new GrpcWebError(response.statusMessage, response.status, response.trailers);
            reject(err);
          }
        },
      });
    });
  }

  invoke<T extends UnaryMethodDefinitionish>(methodDesc: T, _request: any, metadata: grpc.Metadata | undefined): Observable<any> {
    const upStreamCodes = this.options.upStreamRetryCodes ?? [];
    const DEFAULT_TIMEOUT_TIME: number = 3_000;
    const request = { ..._request, ...methodDesc.requestType };
    const transport = this.options.streamingTransport ?? this.options.transport;
    const maybeCombinedMetadata = metadata && this.options.metadata ? new BrowserHeaders({ ...this.options?.metadata.headersMap, ...metadata?.headersMap }) : (metadata ?? this.options.metadata);
    return new Observable((observer) => {
      const upStream = () => {
        const client = grpc.invoke(methodDesc, {
          host: this.host,
          request,
          ...(transport !== undefined ? { transport } : {}),
          metadata: maybeCombinedMetadata ?? {},
          debug: this.options.debug ?? false,
          onMessage: (next) => observer.next(next),
          onEnd: (code: grpc.Code, message: string, trailers: grpc.Metadata) => {
            if (code === 0) {
              observer.complete();
            } else if (upStreamCodes.includes(code)) {
              setTimeout(upStream, DEFAULT_TIMEOUT_TIME);
            } else {
              const err = new Error(message) as any;
              err.code = code;
              err.metadata = trailers;
              observer.error(err);
            }
          },
        });
        observer.add(() => client.close());
      };
      upStream();
    }).pipe(share());
  }
}

type Builtin = Date | Function | Uint8Array | string | number | boolean | undefined;

export type DeepPartial<T> = T extends Builtin ? T : T extends globalThis.Array<infer U> ? globalThis.Array<DeepPartial<U>> : T extends ReadonlyArray<infer U> ? ReadonlyArray<DeepPartial<U>> : T extends { $case: string } ? { [K in keyof Omit<T, "$case">]?: DeepPartial<T[K]> } & { $case: T["$case"] } : T extends {} ? { [K in keyof T]?: DeepPartial<T[K]> } : Partial<T>;

type KeysOfUnion<T> = T extends T ? keyof T : never;
export type Exact<P, I extends P> = P extends Builtin ? P : P & { [K in keyof P]: Exact<P[K], I[K]> } & { [K in Exclude<keyof I, KeysOfUnion<P>>]: never };

function longToNumber(long: Long): number {
  if (long.gt(globalThis.Number.MAX_SAFE_INTEGER)) {
    throw new globalThis.Error("Value is larger than Number.MAX_SAFE_INTEGER");
  }
  return long.toNumber();
}

if (_m0.util.Long !== Long) {
  _m0.util.Long = Long as any;
  _m0.configure();
}

export class GrpcWebError extends globalThis.Error {
  constructor(
    message: string,
    public code: grpc.Code,
    public metadata: grpc.Metadata,
  ) {
    super(message);
  }
}
