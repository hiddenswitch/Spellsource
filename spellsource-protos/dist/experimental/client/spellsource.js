"use strict";
var __extends = (this && this.__extends) || (function () {
    var extendStatics = function (d, b) {
        extendStatics = Object.setPrototypeOf ||
            ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
            function (d, b) { for (var p in b) if (Object.prototype.hasOwnProperty.call(b, p)) d[p] = b[p]; };
        return extendStatics(d, b);
    };
    return function (d, b) {
        if (typeof b !== "function" && b !== null)
            throw new TypeError("Class extends value " + String(b) + " is not a constructor or null");
        extendStatics(d, b);
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
})();
var __assign = (this && this.__assign) || function () {
    __assign = Object.assign || function(t) {
        for (var s, i = 1, n = arguments.length; i < n; i++) {
            s = arguments[i];
            for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p))
                t[p] = s[p];
        }
        return t;
    };
    return __assign.apply(this, arguments);
};
var __values = (this && this.__values) || function(o) {
    var s = typeof Symbol === "function" && Symbol.iterator, m = s && o[s], i = 0;
    if (m) return m.call(o);
    if (o && typeof o.length === "number") return {
        next: function () {
            if (o && i >= o.length) o = void 0;
            return { value: o && o[i++], done: !o };
        }
    };
    throw new TypeError(s ? "Object is not iterable." : "Symbol.iterator is not defined.");
};
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.DecksPutResponse = exports.DecksPutRequest = exports.DecksGetResponse = exports.DecksGetRequest = exports.DecksGetAllResponse = exports.DecksDeleteRequest = exports.CreateAccountResponse = exports.CreateAccountRequest = exports.Color = exports.ClientToServerMessage_FirstMessageMessage = exports.ClientToServerMessage = exports.ChatMessage = exports.ChangePasswordResponse = exports.ChangePasswordRequest = exports.CardRecord = exports.CardEvent = exports.AttributeValueTuple = exports.Draft = exports.ArtificialIntelligence = exports.Art = exports.Account = exports.AcceptInviteResponse = exports.AcceptInviteRequest = exports.GameEventTypeMessage = exports.ZonesMessage = exports.RarityMessage = exports.PresenceMessage = exports.PlayerEntityAttributesMessage = exports.MessageTypeMessage = exports.EntityTypeMessage = exports.DamageTypeMessage = exports.CardTypeMessage = exports.ActionTypeMessage = exports.Sprite_SpritePivot = exports.Invite_InviteStatus = exports.InventoryCollection_InventoryCollectionType = exports.InventoryCollection_InventoryCollectionDeckType = exports.Emote_EmoteMessage = exports.DraftState_DraftStateStatus = exports.GameEventTypeMessage_GameEventType = exports.ZonesMessage_Zones = exports.RarityMessage_Rarity = exports.PresenceMessage_Presence = exports.PlayerEntityAttributesMessage_PlayerEntityAttributes = exports.MessageTypeMessage_MessageType = exports.EntityTypeMessage_EntityType = exports.DamageTypeMessage_DamageType = exports.CardTypeMessage_CardType = exports.ActionTypeMessage_ActionType = exports.protobufPackage = void 0;
exports.InventoryCollection = exports.GetInviteRequest = exports.GetGameRecordResponse = exports.GetGameRecordRequest = exports.GetGameRecordIdsResponse = exports.GetAccountsResponse = exports.GetAccountsRequest = exports.GetAccountRequest = exports.GameState = exports.GameOver = exports.GameEvent_TriggerFiredMessage = exports.GameEvent_PerformedGameActionMessage = exports.GameEvent_JoustMessage = exports.GameEvent_DestroyMessage = exports.GameEvent_DamageMessage = exports.GameEvent = exports.GameActions = exports.FriendPutResponse = exports.FriendPutRequest = exports.FriendDeleteRequest = exports.Friend = exports.Font = exports.Envelope_ResultMessage_SendMessageMessage = exports.Envelope_ResultMessage_PutCardMessage = exports.Envelope_ResultMessage = exports.Envelope_RemovedMessage = exports.Envelope_MethodMessage_SendMessageMessage = exports.Envelope_MethodMessage_PutCardMessage = exports.Envelope_MethodMessage_DequeueMessage = exports.Envelope_MethodMessage_DeleteCardMessage = exports.Envelope_MethodMessage = exports.Envelope_GameMessage = exports.Envelope = exports.EntityLocation = exports.EntityChangeSet = exports.Entity = exports.Emote = exports.EditableCard = exports.DraftsPostRequest = exports.DraftsChooseHeroRequest = exports.DraftsChooseCardRequest = exports.DraftState = exports.Destroy = exports.DeleteInviteRequest = exports.DefaultMethodResponse = exports.DecksUpdateRequest = exports.DecksUpdateCommand_SetPlayerEntityAttributeMessage = exports.DecksUpdateCommand_PushInventoryIdsMessage = exports.DecksUpdateCommand_PushCardIdsMessage = exports.DecksUpdateCommand = void 0;
exports.HiddenSwitchSpellsourceAPIServiceDecksGetDesc = exports.HiddenSwitchSpellsourceAPIServiceDecksDeleteDesc = exports.HiddenSwitchSpellsourceAPIServiceCreateAccountDesc = exports.HiddenSwitchSpellsourceAPIServiceChangePasswordDesc = exports.HiddenSwitchSpellsourceAPIServiceAcceptInviteDesc = exports.HiddenSwitchSpellsourceAPIServiceSubscribeMatchDesc = exports.HiddenSwitchSpellsourceAPIServiceSubscribeEditableCardsDesc = exports.HiddenSwitchSpellsourceAPIServiceSubscribeInvitesDesc = exports.HiddenSwitchSpellsourceAPIServiceSubscribeFriendsDesc = exports.HiddenSwitchSpellsourceAPIServiceDeleteCardDesc = exports.HiddenSwitchSpellsourceAPIServiceSendMessageDesc = exports.HiddenSwitchSpellsourceAPIServicePutCardDesc = exports.HiddenSwitchSpellsourceAPIServiceDesc = exports.HiddenSwitchSpellsourceAPIServiceClientImpl = exports.MatchmakingMatchmakingGetDesc = exports.MatchmakingMatchmakingDeleteDesc = exports.MatchmakingDesc = exports.MatchmakingClientImpl = exports.ValidationReport = exports.UnfriendResponse = exports.Tooltip = exports.Timers = exports.TargetActionPair = exports.Sprite = exports.SpellsourceException = exports.SpellAction = exports.SpanContext = exports.ServerToClientMessage = exports.ReplayGameStates = exports.ReplayDeltas = exports.Replay = exports.Prefab = exports.PostPasswordResetRequest = exports.PostInviteRequest = exports.PhysicalAttackEvent = exports.MatchmakingQueuesResponse = exports.MatchmakingQueuePutResponseUnityConnection = exports.MatchmakingQueuePutResponse = exports.MatchmakingQueuePutRequest = exports.MatchmakingQueueItem_RequiresMessage = exports.MatchmakingQueueItem = exports.MatchConcedeResponse = exports.MatchCancelResponse = exports.Match = exports.LoginResponse = exports.LoginRequest = exports.InviteResponse = exports.InvitePostRequest = exports.InviteGetResponse = exports.Invite = void 0;
exports.GrpcWebError = exports.GrpcWebImpl = exports.HiddenSwitchSpellsourceAPIServicePostPasswordResetDesc = exports.HiddenSwitchSpellsourceAPIServicePostInviteDesc = exports.HiddenSwitchSpellsourceAPIServiceLoginDesc = exports.HiddenSwitchSpellsourceAPIServiceGetInvitesDesc = exports.HiddenSwitchSpellsourceAPIServiceGetInviteDesc = exports.HiddenSwitchSpellsourceAPIServiceGetGameRecordIdsDesc = exports.HiddenSwitchSpellsourceAPIServiceGetGameRecordDesc = exports.HiddenSwitchSpellsourceAPIServiceGetAccountsDesc = exports.HiddenSwitchSpellsourceAPIServiceGetAccountDesc = exports.HiddenSwitchSpellsourceAPIServiceFriendPutDesc = exports.HiddenSwitchSpellsourceAPIServiceFriendDeleteDesc = exports.HiddenSwitchSpellsourceAPIServiceDraftsPostDesc = exports.HiddenSwitchSpellsourceAPIServiceDraftsGetDesc = exports.HiddenSwitchSpellsourceAPIServiceDraftsChooseHeroDesc = exports.HiddenSwitchSpellsourceAPIServiceDraftsChooseCardDesc = exports.HiddenSwitchSpellsourceAPIServiceDeleteInviteDesc = exports.HiddenSwitchSpellsourceAPIServiceDuplicateDeckDesc = exports.HiddenSwitchSpellsourceAPIServiceDecksUpdateDesc = exports.HiddenSwitchSpellsourceAPIServiceDecksPutDesc = exports.HiddenSwitchSpellsourceAPIServiceDecksGetAllDesc = void 0;
/* eslint-disable */
var grpc_web_1 = require("@improbable-eng/grpc-web");
var browser_headers_1 = require("browser-headers");
var long_1 = __importDefault(require("long"));
var minimal_1 = __importDefault(require("protobufjs/minimal"));
var rxjs_1 = require("rxjs");
var operators_1 = require("rxjs/operators");
var empty_1 = require("./google/protobuf/empty");
var wrappers_1 = require("./google/protobuf/wrappers");
var reactive_1 = require("./reactive");
exports.protobufPackage = "spellsource";
var ActionTypeMessage_ActionType;
(function (ActionTypeMessage_ActionType) {
    ActionTypeMessage_ActionType[ActionTypeMessage_ActionType["SYSTEM"] = 0] = "SYSTEM";
    ActionTypeMessage_ActionType[ActionTypeMessage_ActionType["END_TURN"] = 1] = "END_TURN";
    ActionTypeMessage_ActionType[ActionTypeMessage_ActionType["PHYSICAL_ATTACK"] = 2] = "PHYSICAL_ATTACK";
    ActionTypeMessage_ActionType[ActionTypeMessage_ActionType["SPELL"] = 3] = "SPELL";
    ActionTypeMessage_ActionType[ActionTypeMessage_ActionType["SUMMON"] = 4] = "SUMMON";
    ActionTypeMessage_ActionType[ActionTypeMessage_ActionType["HERO_POWER"] = 5] = "HERO_POWER";
    ActionTypeMessage_ActionType[ActionTypeMessage_ActionType["BATTLECRY"] = 6] = "BATTLECRY";
    ActionTypeMessage_ActionType[ActionTypeMessage_ActionType["EQUIP_WEAPON"] = 7] = "EQUIP_WEAPON";
    ActionTypeMessage_ActionType[ActionTypeMessage_ActionType["DISCOVER"] = 8] = "DISCOVER";
    ActionTypeMessage_ActionType[ActionTypeMessage_ActionType["HERO"] = 9] = "HERO";
    ActionTypeMessage_ActionType[ActionTypeMessage_ActionType["TAP"] = 10] = "TAP";
    ActionTypeMessage_ActionType[ActionTypeMessage_ActionType["UNRECOGNIZED"] = -1] = "UNRECOGNIZED";
})(ActionTypeMessage_ActionType || (exports.ActionTypeMessage_ActionType = ActionTypeMessage_ActionType = {}));
var CardTypeMessage_CardType;
(function (CardTypeMessage_CardType) {
    CardTypeMessage_CardType[CardTypeMessage_CardType["HERO"] = 0] = "HERO";
    CardTypeMessage_CardType[CardTypeMessage_CardType["MINION"] = 1] = "MINION";
    CardTypeMessage_CardType[CardTypeMessage_CardType["SPELL"] = 2] = "SPELL";
    CardTypeMessage_CardType[CardTypeMessage_CardType["WEAPON"] = 3] = "WEAPON";
    CardTypeMessage_CardType[CardTypeMessage_CardType["HERO_POWER"] = 4] = "HERO_POWER";
    CardTypeMessage_CardType[CardTypeMessage_CardType["GROUP"] = 5] = "GROUP";
    CardTypeMessage_CardType[CardTypeMessage_CardType["CHOOSE_ONE"] = 6] = "CHOOSE_ONE";
    CardTypeMessage_CardType[CardTypeMessage_CardType["ENCHANTMENT"] = 7] = "ENCHANTMENT";
    CardTypeMessage_CardType[CardTypeMessage_CardType["CLASS"] = 8] = "CLASS";
    CardTypeMessage_CardType[CardTypeMessage_CardType["FORMAT"] = 9] = "FORMAT";
    CardTypeMessage_CardType[CardTypeMessage_CardType["ROGUE_CHOICE"] = 10] = "ROGUE_CHOICE";
    CardTypeMessage_CardType[CardTypeMessage_CardType["UNRECOGNIZED"] = -1] = "UNRECOGNIZED";
})(CardTypeMessage_CardType || (exports.CardTypeMessage_CardType = CardTypeMessage_CardType = {}));
var DamageTypeMessage_DamageType;
(function (DamageTypeMessage_DamageType) {
    DamageTypeMessage_DamageType[DamageTypeMessage_DamageType["PHYSICAL"] = 0] = "PHYSICAL";
    DamageTypeMessage_DamageType[DamageTypeMessage_DamageType["FATIGUE"] = 1] = "FATIGUE";
    DamageTypeMessage_DamageType[DamageTypeMessage_DamageType["MAGICAL"] = 2] = "MAGICAL";
    DamageTypeMessage_DamageType[DamageTypeMessage_DamageType["DECAY"] = 3] = "DECAY";
    DamageTypeMessage_DamageType[DamageTypeMessage_DamageType["DEFLECT"] = 4] = "DEFLECT";
    DamageTypeMessage_DamageType[DamageTypeMessage_DamageType["DRAIN"] = 5] = "DRAIN";
    DamageTypeMessage_DamageType[DamageTypeMessage_DamageType["IGNORES_ARMOR"] = 6] = "IGNORES_ARMOR";
    DamageTypeMessage_DamageType[DamageTypeMessage_DamageType["SPLASH"] = 7] = "SPLASH";
    DamageTypeMessage_DamageType[DamageTypeMessage_DamageType["UNRECOGNIZED"] = -1] = "UNRECOGNIZED";
})(DamageTypeMessage_DamageType || (exports.DamageTypeMessage_DamageType = DamageTypeMessage_DamageType = {}));
var EntityTypeMessage_EntityType;
(function (EntityTypeMessage_EntityType) {
    EntityTypeMessage_EntityType[EntityTypeMessage_EntityType["ANY"] = 0] = "ANY";
    EntityTypeMessage_EntityType[EntityTypeMessage_EntityType["ACTOR"] = 1] = "ACTOR";
    EntityTypeMessage_EntityType[EntityTypeMessage_EntityType["HERO"] = 2] = "HERO";
    EntityTypeMessage_EntityType[EntityTypeMessage_EntityType["MINION"] = 3] = "MINION";
    EntityTypeMessage_EntityType[EntityTypeMessage_EntityType["WEAPON"] = 4] = "WEAPON";
    EntityTypeMessage_EntityType[EntityTypeMessage_EntityType["CARD"] = 5] = "CARD";
    EntityTypeMessage_EntityType[EntityTypeMessage_EntityType["PLAYER"] = 6] = "PLAYER";
    EntityTypeMessage_EntityType[EntityTypeMessage_EntityType["ENCHANTMENT"] = 7] = "ENCHANTMENT";
    EntityTypeMessage_EntityType[EntityTypeMessage_EntityType["QUEST"] = 8] = "QUEST";
    EntityTypeMessage_EntityType[EntityTypeMessage_EntityType["SECRET"] = 9] = "SECRET";
    EntityTypeMessage_EntityType[EntityTypeMessage_EntityType["UNRECOGNIZED"] = -1] = "UNRECOGNIZED";
})(EntityTypeMessage_EntityType || (exports.EntityTypeMessage_EntityType = EntityTypeMessage_EntityType = {}));
var MessageTypeMessage_MessageType;
(function (MessageTypeMessage_MessageType) {
    MessageTypeMessage_MessageType[MessageTypeMessage_MessageType["UPDATE_ACTION"] = 0] = "UPDATE_ACTION";
    MessageTypeMessage_MessageType[MessageTypeMessage_MessageType["ON_GAME_EVENT"] = 1] = "ON_GAME_EVENT";
    MessageTypeMessage_MessageType[MessageTypeMessage_MessageType["ON_GAME_END"] = 2] = "ON_GAME_END";
    MessageTypeMessage_MessageType[MessageTypeMessage_MessageType["ON_UPDATE"] = 3] = "ON_UPDATE";
    MessageTypeMessage_MessageType[MessageTypeMessage_MessageType["ON_REQUEST_ACTION"] = 4] = "ON_REQUEST_ACTION";
    MessageTypeMessage_MessageType[MessageTypeMessage_MessageType["FIRST_MESSAGE"] = 5] = "FIRST_MESSAGE";
    MessageTypeMessage_MessageType[MessageTypeMessage_MessageType["ON_MULLIGAN"] = 6] = "ON_MULLIGAN";
    MessageTypeMessage_MessageType[MessageTypeMessage_MessageType["UPDATE_MULLIGAN"] = 7] = "UPDATE_MULLIGAN";
    MessageTypeMessage_MessageType[MessageTypeMessage_MessageType["EMOTE"] = 8] = "EMOTE";
    MessageTypeMessage_MessageType[MessageTypeMessage_MessageType["TOUCH"] = 9] = "TOUCH";
    MessageTypeMessage_MessageType[MessageTypeMessage_MessageType["CONCEDE"] = 10] = "CONCEDE";
    MessageTypeMessage_MessageType[MessageTypeMessage_MessageType["PINGPONG"] = 11] = "PINGPONG";
    MessageTypeMessage_MessageType[MessageTypeMessage_MessageType["TIMER"] = 12] = "TIMER";
    MessageTypeMessage_MessageType[MessageTypeMessage_MessageType["UNRECOGNIZED"] = -1] = "UNRECOGNIZED";
})(MessageTypeMessage_MessageType || (exports.MessageTypeMessage_MessageType = MessageTypeMessage_MessageType = {}));
var PlayerEntityAttributesMessage_PlayerEntityAttributes;
(function (PlayerEntityAttributesMessage_PlayerEntityAttributes) {
    PlayerEntityAttributesMessage_PlayerEntityAttributes[PlayerEntityAttributesMessage_PlayerEntityAttributes["SIGNATURE"] = 0] = "SIGNATURE";
    PlayerEntityAttributesMessage_PlayerEntityAttributes[PlayerEntityAttributesMessage_PlayerEntityAttributes["UNRECOGNIZED"] = -1] = "UNRECOGNIZED";
})(PlayerEntityAttributesMessage_PlayerEntityAttributes || (exports.PlayerEntityAttributesMessage_PlayerEntityAttributes = PlayerEntityAttributesMessage_PlayerEntityAttributes = {}));
var PresenceMessage_Presence;
(function (PresenceMessage_Presence) {
    PresenceMessage_Presence[PresenceMessage_Presence["UNKNOWN"] = 0] = "UNKNOWN";
    PresenceMessage_Presence[PresenceMessage_Presence["OFFLINE"] = 1] = "OFFLINE";
    PresenceMessage_Presence[PresenceMessage_Presence["IN_GAME"] = 2] = "IN_GAME";
    PresenceMessage_Presence[PresenceMessage_Presence["ONLINE"] = 3] = "ONLINE";
    PresenceMessage_Presence[PresenceMessage_Presence["UNRECOGNIZED"] = -1] = "UNRECOGNIZED";
})(PresenceMessage_Presence || (exports.PresenceMessage_Presence = PresenceMessage_Presence = {}));
var RarityMessage_Rarity;
(function (RarityMessage_Rarity) {
    RarityMessage_Rarity[RarityMessage_Rarity["FREE"] = 0] = "FREE";
    RarityMessage_Rarity[RarityMessage_Rarity["COMMON"] = 1] = "COMMON";
    RarityMessage_Rarity[RarityMessage_Rarity["RARE"] = 2] = "RARE";
    RarityMessage_Rarity[RarityMessage_Rarity["EPIC"] = 3] = "EPIC";
    RarityMessage_Rarity[RarityMessage_Rarity["LEGENDARY"] = 4] = "LEGENDARY";
    RarityMessage_Rarity[RarityMessage_Rarity["ALLIANCE"] = 5] = "ALLIANCE";
    RarityMessage_Rarity[RarityMessage_Rarity["UNRECOGNIZED"] = -1] = "UNRECOGNIZED";
})(RarityMessage_Rarity || (exports.RarityMessage_Rarity = RarityMessage_Rarity = {}));
var ZonesMessage_Zones;
(function (ZonesMessage_Zones) {
    /** NONE - This zone specifies the entity belongs to no zone or the zone is not yet assigned. */
    ZonesMessage_Zones[ZonesMessage_Zones["NONE"] = 0] = "NONE";
    /** HAND - This zone is a player's hand. Only cards can be in this zone. */
    ZonesMessage_Zones[ZonesMessage_Zones["HAND"] = 1] = "HAND";
    /** DECK - This zone is a player's deck. Only {@link Card} entities can be in this zone. */
    ZonesMessage_Zones[ZonesMessage_Zones["DECK"] = 2] = "DECK";
    /**
     * GRAVEYARD - The graveyard is where a {@link Card} has been played with {@link GameLogic#playCard(int, EntityReference,
     * EntityReference)} goes; and where an {@link Actor} that has been destroyed with {@link GameLogic#destroy(Actor...)}
     * goes. A {@link net.demilich.metastone.game.spells.trigger.secrets.Secret} and other entities subclassing {@link
     * Enchantment} go to {@link #REMOVED_FROM_PLAY}.
     */
    ZonesMessage_Zones[ZonesMessage_Zones["GRAVEYARD"] = 3] = "GRAVEYARD";
    /** BATTLEFIELD - A {@link Minion} is typically summoned into this zone. Anything in this zone is targetable by physical attacks. */
    ZonesMessage_Zones[ZonesMessage_Zones["BATTLEFIELD"] = 4] = "BATTLEFIELD";
    /**
     * SECRET - This zone is where a {@link net.demilich.metastone.game.spells.trigger.secrets.Secret} entity goes. Its contents
     * are not visible to the opponent.
     */
    ZonesMessage_Zones[ZonesMessage_Zones["SECRET"] = 5] = "SECRET";
    /**
     * QUEST - This zone is  where {@link net.demilich.metastone.game.spells.trigger.secrets.Quest} entities go, which behave like
     * secrets that are visible to the opponent and do not go away the first time they  are triggered.
     */
    ZonesMessage_Zones[ZonesMessage_Zones["QUEST"] = 6] = "QUEST";
    /**
     * HERO_POWER - The hero power zone stores the hero power for a corresponding {@link net.demilich.metastone.game.entities.heroes.Hero}.
     * Only one such card can be in the zone at a time.
     */
    ZonesMessage_Zones[ZonesMessage_Zones["HERO_POWER"] = 7] = "HERO_POWER";
    /** HERO - The hero zone stores the {@link Hero} actor that represents a player's targetable avatar in the game. */
    ZonesMessage_Zones[ZonesMessage_Zones["HERO"] = 8] = "HERO";
    /**
     * WEAPON - The weapon zone stores the {@link net.demilich.metastone.game.entities.weapons.Weapon} that a {@link Hero} has
     * equipped.
     */
    ZonesMessage_Zones[ZonesMessage_Zones["WEAPON"] = 9] = "WEAPON";
    /**
     * SET_ASIDE_ZONE - The set aside zone holds an {@link Entity} existing in any intermediate or "not really on the board" state, like
     * the original minion after Recycle puts a new copy in the deck, the prior state of transformed minions and Lord
     * Jaraxxus the minion after his Battlecry occurs.
     *
     * Unlike the official game rules, the three cards presented to a player by Tracking go into the {@link #DISCOVER}
     * zone.
     */
    ZonesMessage_Zones[ZonesMessage_Zones["SET_ASIDE_ZONE"] = 10] = "SET_ASIDE_ZONE";
    ZonesMessage_Zones[ZonesMessage_Zones["HIDDEN"] = 11] = "HIDDEN";
    /**
     * DISCOVER - The discover zone has any cards that are being currently chosen by the player as part of a {@link
     * net.demilich.metastone.game.actions.DiscoverAction}.
     *
     * The opposing player can see the count, but not the contents, of cards the player is choosing between.
     */
    ZonesMessage_Zones[ZonesMessage_Zones["DISCOVER"] = 12] = "DISCOVER";
    /**
     * REMOVED_FROM_PLAY - An {@link Entity} in this zone is "deleted" in the sense that it will never appear in any {@link EntityFilter}
     * filters or targeting lists.
     */
    ZonesMessage_Zones[ZonesMessage_Zones["REMOVED_FROM_PLAY"] = 13] = "REMOVED_FROM_PLAY";
    /**
     * PLAYER - Metastone originally used the same object for what is now the {@link Player} and {@link Hero} entity. Since the
     * {@link Player} is still targetable (primarily by special buffing spells), it needs a {@link Zones} zone to belong
     * to. This zone is the zone a {@link Player} entity belongs to.
     */
    ZonesMessage_Zones[ZonesMessage_Zones["PLAYER"] = 14] = "PLAYER";
    /**
     * ENCHANTMENT - The enchantment zone corresponds to the player's list of {@link Enchantment} entities in the {@link
     * GameContext#getTriggers()} list.
     */
    ZonesMessage_Zones[ZonesMessage_Zones["ENCHANTMENT"] = 15] = "ENCHANTMENT";
    ZonesMessage_Zones[ZonesMessage_Zones["UNRECOGNIZED"] = -1] = "UNRECOGNIZED";
})(ZonesMessage_Zones || (exports.ZonesMessage_Zones = ZonesMessage_Zones = {}));
var GameEventTypeMessage_GameEventType;
(function (GameEventTypeMessage_GameEventType) {
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["ALL"] = 0] = "ALL";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["AFTER_PHYSICAL_ATTACK"] = 1] = "AFTER_PHYSICAL_ATTACK";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["AFTER_PLAY_CARD"] = 2] = "AFTER_PLAY_CARD";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["AFTER_SPELL_CASTED"] = 3] = "AFTER_SPELL_CASTED";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["AFTER_SUMMON"] = 4] = "AFTER_SUMMON";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["ATTRIBUTE_APPLIED"] = 5] = "ATTRIBUTE_APPLIED";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["ARMOR_GAINED"] = 6] = "ARMOR_GAINED";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["BEFORE_PHYSICAL_ATTACK"] = 7] = "BEFORE_PHYSICAL_ATTACK";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["BEFORE_SUMMON"] = 8] = "BEFORE_SUMMON";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["BOARD_CHANGED"] = 9] = "BOARD_CHANGED";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["CARD_ADDED_TO_DECK"] = 10] = "CARD_ADDED_TO_DECK";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["CARD_SHUFFLED"] = 11] = "CARD_SHUFFLED";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["DAMAGE"] = 12] = "DAMAGE";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["DECAY"] = 13] = "DECAY";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["DID_END_SEQUENCE"] = 14] = "DID_END_SEQUENCE";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["DISCARD"] = 15] = "DISCARD";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["DISCOVER"] = 16] = "DISCOVER";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["DRAIN"] = 17] = "DRAIN";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["DRAW_CARD"] = 18] = "DRAW_CARD";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["ENRAGE_CHANGED"] = 19] = "ENRAGE_CHANGED";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["ENTITY_TOUCHED"] = 20] = "ENTITY_TOUCHED";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["ENTITY_UNTOUCHED"] = 21] = "ENTITY_UNTOUCHED";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["EXCESS_HEAL"] = 22] = "EXCESS_HEAL";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["FATIGUE"] = 23] = "FATIGUE";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["GAME_START"] = 24] = "GAME_START";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["HEAL"] = 25] = "HEAL";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["HERO_POWER_USED"] = 26] = "HERO_POWER_USED";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["INVOKED"] = 27] = "INVOKED";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["JOUST"] = 28] = "JOUST";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["KILL"] = 29] = "KILL";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["LOSE_DIVINE_SHIELD"] = 30] = "LOSE_DIVINE_SHIELD";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["LOSE_DEFLECT"] = 31] = "LOSE_DEFLECT";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["LOSE_STEALTH"] = 32] = "LOSE_STEALTH";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["MAX_HP_INCREASED"] = 33] = "MAX_HP_INCREASED";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["MAX_MANA"] = 34] = "MAX_MANA";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["MANA_MODIFIED"] = 35] = "MANA_MODIFIED";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["MISSILE_FIRED"] = 36] = "MISSILE_FIRED";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["OVERLOAD"] = 37] = "OVERLOAD";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["PERFORMED_GAME_ACTION"] = 38] = "PERFORMED_GAME_ACTION";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["PHYSICAL_ATTACK"] = 39] = "PHYSICAL_ATTACK";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["PLAY_CARD"] = 40] = "PLAY_CARD";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["PRE_DAMAGE"] = 41] = "PRE_DAMAGE";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["PRE_GAME_START"] = 42] = "PRE_GAME_START";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["QUEST_PLAYED"] = 43] = "QUEST_PLAYED";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["QUEST_SUCCESSFUL"] = 44] = "QUEST_SUCCESSFUL";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["RETURNED_TO_HAND"] = 45] = "RETURNED_TO_HAND";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["ROASTED"] = 46] = "ROASTED";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["REVEAL_CARD"] = 47] = "REVEAL_CARD";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["SECRET_PLAYED"] = 48] = "SECRET_PLAYED";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["SECRET_REVEALED"] = 49] = "SECRET_REVEALED";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["SPELL_CASTED"] = 50] = "SPELL_CASTED";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["SUMMON"] = 51] = "SUMMON";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["TARGET_ACQUISITION"] = 52] = "TARGET_ACQUISITION";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["TRIGGER_FIRED"] = 53] = "TRIGGER_FIRED";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["TURN_END"] = 54] = "TURN_END";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["TURN_START"] = 55] = "TURN_START";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["SILENCE"] = 56] = "SILENCE";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["WEAPON_DESTROYED"] = 57] = "WEAPON_DESTROYED";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["WEAPON_EQUIPPED"] = 58] = "WEAPON_EQUIPPED";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["WILL_END_SEQUENCE"] = 59] = "WILL_END_SEQUENCE";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["DESTROY_WILL_QUEUE"] = 60] = "DESTROY_WILL_QUEUE";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["TAP_ACTIVATED"] = 61] = "TAP_ACTIVATED";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["HONORABLE_KILL"] = 62] = "HONORABLE_KILL";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["ROGUE_CHOICE"] = 63] = "ROGUE_CHOICE";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["GAME_INITIALIZED"] = 64] = "GAME_INITIALIZED";
    GameEventTypeMessage_GameEventType[GameEventTypeMessage_GameEventType["UNRECOGNIZED"] = -1] = "UNRECOGNIZED";
})(GameEventTypeMessage_GameEventType || (exports.GameEventTypeMessage_GameEventType = GameEventTypeMessage_GameEventType = {}));
var DraftState_DraftStateStatus;
(function (DraftState_DraftStateStatus) {
    DraftState_DraftStateStatus[DraftState_DraftStateStatus["IN_PROGRESS"] = 0] = "IN_PROGRESS";
    DraftState_DraftStateStatus[DraftState_DraftStateStatus["SELECT_HERO"] = 1] = "SELECT_HERO";
    DraftState_DraftStateStatus[DraftState_DraftStateStatus["COMPLETE"] = 2] = "COMPLETE";
    DraftState_DraftStateStatus[DraftState_DraftStateStatus["RETIRED"] = 3] = "RETIRED";
    DraftState_DraftStateStatus[DraftState_DraftStateStatus["UNRECOGNIZED"] = -1] = "UNRECOGNIZED";
})(DraftState_DraftStateStatus || (exports.DraftState_DraftStateStatus = DraftState_DraftStateStatus = {}));
var Emote_EmoteMessage;
(function (Emote_EmoteMessage) {
    Emote_EmoteMessage[Emote_EmoteMessage["HELLO"] = 0] = "HELLO";
    Emote_EmoteMessage[Emote_EmoteMessage["AMAZING"] = 1] = "AMAZING";
    Emote_EmoteMessage[Emote_EmoteMessage["WHOOPS"] = 2] = "WHOOPS";
    Emote_EmoteMessage[Emote_EmoteMessage["GOOD_GAME"] = 3] = "GOOD_GAME";
    Emote_EmoteMessage[Emote_EmoteMessage["FACE_MY_WRATH"] = 4] = "FACE_MY_WRATH";
    Emote_EmoteMessage[Emote_EmoteMessage["WELL_PLAYED"] = 5] = "WELL_PLAYED";
    Emote_EmoteMessage[Emote_EmoteMessage["UNRECOGNIZED"] = -1] = "UNRECOGNIZED";
})(Emote_EmoteMessage || (exports.Emote_EmoteMessage = Emote_EmoteMessage = {}));
var InventoryCollection_InventoryCollectionDeckType;
(function (InventoryCollection_InventoryCollectionDeckType) {
    InventoryCollection_InventoryCollectionDeckType[InventoryCollection_InventoryCollectionDeckType["DRAFT"] = 0] = "DRAFT";
    InventoryCollection_InventoryCollectionDeckType[InventoryCollection_InventoryCollectionDeckType["CONSTRUCTED"] = 1] = "CONSTRUCTED";
    InventoryCollection_InventoryCollectionDeckType[InventoryCollection_InventoryCollectionDeckType["ROGUE"] = 2] = "ROGUE";
    InventoryCollection_InventoryCollectionDeckType[InventoryCollection_InventoryCollectionDeckType["UNRECOGNIZED"] = -1] = "UNRECOGNIZED";
})(InventoryCollection_InventoryCollectionDeckType || (exports.InventoryCollection_InventoryCollectionDeckType = InventoryCollection_InventoryCollectionDeckType = {}));
var InventoryCollection_InventoryCollectionType;
(function (InventoryCollection_InventoryCollectionType) {
    InventoryCollection_InventoryCollectionType[InventoryCollection_InventoryCollectionType["USER"] = 0] = "USER";
    InventoryCollection_InventoryCollectionType[InventoryCollection_InventoryCollectionType["ALLIANCE"] = 1] = "ALLIANCE";
    InventoryCollection_InventoryCollectionType[InventoryCollection_InventoryCollectionType["DECK"] = 2] = "DECK";
    InventoryCollection_InventoryCollectionType[InventoryCollection_InventoryCollectionType["UNRECOGNIZED"] = -1] = "UNRECOGNIZED";
})(InventoryCollection_InventoryCollectionType || (exports.InventoryCollection_InventoryCollectionType = InventoryCollection_InventoryCollectionType = {}));
var Invite_InviteStatus;
(function (Invite_InviteStatus) {
    Invite_InviteStatus[Invite_InviteStatus["UNDELIVERED"] = 0] = "UNDELIVERED";
    Invite_InviteStatus[Invite_InviteStatus["PENDING"] = 1] = "PENDING";
    Invite_InviteStatus[Invite_InviteStatus["TIMEOUT"] = 2] = "TIMEOUT";
    Invite_InviteStatus[Invite_InviteStatus["ACCEPTED"] = 3] = "ACCEPTED";
    Invite_InviteStatus[Invite_InviteStatus["REJECTED"] = 4] = "REJECTED";
    Invite_InviteStatus[Invite_InviteStatus["CANCELLED"] = 5] = "CANCELLED";
    Invite_InviteStatus[Invite_InviteStatus["UNRECOGNIZED"] = -1] = "UNRECOGNIZED";
})(Invite_InviteStatus || (exports.Invite_InviteStatus = Invite_InviteStatus = {}));
var Sprite_SpritePivot;
(function (Sprite_SpritePivot) {
    Sprite_SpritePivot[Sprite_SpritePivot["BOTTOM"] = 0] = "BOTTOM";
    Sprite_SpritePivot[Sprite_SpritePivot["DIMETRIC_2_X1_FLOOR"] = 1] = "DIMETRIC_2_X1_FLOOR";
    Sprite_SpritePivot[Sprite_SpritePivot["CENTER"] = 2] = "CENTER";
    Sprite_SpritePivot[Sprite_SpritePivot["FLYING"] = 3] = "FLYING";
    Sprite_SpritePivot[Sprite_SpritePivot["UNRECOGNIZED"] = -1] = "UNRECOGNIZED";
})(Sprite_SpritePivot || (exports.Sprite_SpritePivot = Sprite_SpritePivot = {}));
function createBaseActionTypeMessage() {
    return {};
}
exports.ActionTypeMessage = {
    encode: function (_, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseActionTypeMessage();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.ActionTypeMessage.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (_) {
        var message = createBaseActionTypeMessage();
        return message;
    },
};
function createBaseCardTypeMessage() {
    return {};
}
exports.CardTypeMessage = {
    encode: function (_, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseCardTypeMessage();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.CardTypeMessage.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (_) {
        var message = createBaseCardTypeMessage();
        return message;
    },
};
function createBaseDamageTypeMessage() {
    return {};
}
exports.DamageTypeMessage = {
    encode: function (_, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseDamageTypeMessage();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.DamageTypeMessage.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (_) {
        var message = createBaseDamageTypeMessage();
        return message;
    },
};
function createBaseEntityTypeMessage() {
    return {};
}
exports.EntityTypeMessage = {
    encode: function (_, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseEntityTypeMessage();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.EntityTypeMessage.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (_) {
        var message = createBaseEntityTypeMessage();
        return message;
    },
};
function createBaseMessageTypeMessage() {
    return {};
}
exports.MessageTypeMessage = {
    encode: function (_, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseMessageTypeMessage();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.MessageTypeMessage.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (_) {
        var message = createBaseMessageTypeMessage();
        return message;
    },
};
function createBasePlayerEntityAttributesMessage() {
    return {};
}
exports.PlayerEntityAttributesMessage = {
    encode: function (_, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBasePlayerEntityAttributesMessage();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.PlayerEntityAttributesMessage.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (_) {
        var message = createBasePlayerEntityAttributesMessage();
        return message;
    },
};
function createBasePresenceMessage() {
    return {};
}
exports.PresenceMessage = {
    encode: function (_, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBasePresenceMessage();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.PresenceMessage.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (_) {
        var message = createBasePresenceMessage();
        return message;
    },
};
function createBaseRarityMessage() {
    return {};
}
exports.RarityMessage = {
    encode: function (_, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseRarityMessage();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.RarityMessage.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (_) {
        var message = createBaseRarityMessage();
        return message;
    },
};
function createBaseZonesMessage() {
    return {};
}
exports.ZonesMessage = {
    encode: function (_, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseZonesMessage();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.ZonesMessage.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (_) {
        var message = createBaseZonesMessage();
        return message;
    },
};
function createBaseGameEventTypeMessage() {
    return {};
}
exports.GameEventTypeMessage = {
    encode: function (_, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseGameEventTypeMessage();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.GameEventTypeMessage.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (_) {
        var message = createBaseGameEventTypeMessage();
        return message;
    },
};
function createBaseAcceptInviteRequest() {
    return { inviteId: "", awaitGameStart: false, match: undefined };
}
exports.AcceptInviteRequest = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.inviteId !== "") {
            writer.uint32(10).string(message.inviteId);
        }
        if (message.awaitGameStart === true) {
            writer.uint32(16).bool(message.awaitGameStart);
        }
        if (message.match !== undefined) {
            exports.MatchmakingQueuePutRequest.encode(message.match, writer.uint32(26).fork()).ldelim();
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseAcceptInviteRequest();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
                    message.match = exports.MatchmakingQueuePutRequest.decode(reader, reader.uint32());
                    continue;
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.AcceptInviteRequest.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b;
        var message = createBaseAcceptInviteRequest();
        message.inviteId = (_a = object.inviteId) !== null && _a !== void 0 ? _a : "";
        message.awaitGameStart = (_b = object.awaitGameStart) !== null && _b !== void 0 ? _b : false;
        message.match = object.match !== undefined && object.match !== null ? exports.MatchmakingQueuePutRequest.fromPartial(object.match) : undefined;
        return message;
    },
};
function createBaseAcceptInviteResponse() {
    return { friend: undefined, invite: undefined, match: undefined };
}
exports.AcceptInviteResponse = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.friend !== undefined) {
            exports.FriendPutResponse.encode(message.friend, writer.uint32(10).fork()).ldelim();
        }
        if (message.invite !== undefined) {
            exports.Invite.encode(message.invite, writer.uint32(18).fork()).ldelim();
        }
        if (message.match !== undefined) {
            exports.MatchmakingQueuePutResponse.encode(message.match, writer.uint32(26).fork()).ldelim();
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseAcceptInviteResponse();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
                case 1:
                    if (tag !== 10) {
                        break;
                    }
                    message.friend = exports.FriendPutResponse.decode(reader, reader.uint32());
                    continue;
                case 2:
                    if (tag !== 18) {
                        break;
                    }
                    message.invite = exports.Invite.decode(reader, reader.uint32());
                    continue;
                case 3:
                    if (tag !== 26) {
                        break;
                    }
                    message.match = exports.MatchmakingQueuePutResponse.decode(reader, reader.uint32());
                    continue;
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.AcceptInviteResponse.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var message = createBaseAcceptInviteResponse();
        message.friend = object.friend !== undefined && object.friend !== null ? exports.FriendPutResponse.fromPartial(object.friend) : undefined;
        message.invite = object.invite !== undefined && object.invite !== null ? exports.Invite.fromPartial(object.invite) : undefined;
        message.match = object.match !== undefined && object.match !== null ? exports.MatchmakingQueuePutResponse.fromPartial(object.match) : undefined;
        return message;
    },
};
function createBaseAccount() {
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
exports.Account = {
    encode: function (message, writer) {
        var e_1, _a, e_2, _b;
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.Id !== "") {
            writer.uint32(10).string(message.Id);
        }
        try {
            for (var _c = __values(message.decks), _d = _c.next(); !_d.done; _d = _c.next()) {
                var v = _d.value;
                exports.InventoryCollection.encode(v, writer.uint32(18).fork()).ldelim();
            }
        }
        catch (e_1_1) { e_1 = { error: e_1_1 }; }
        finally {
            try {
                if (_d && !_d.done && (_a = _c.return)) _a.call(_c);
            }
            finally { if (e_1) throw e_1.error; }
        }
        if (message.email !== "") {
            writer.uint32(26).string(message.email);
        }
        try {
            for (var _e = __values(message.friends), _f = _e.next(); !_f.done; _f = _e.next()) {
                var v = _f.value;
                exports.Friend.encode(v, writer.uint32(34).fork()).ldelim();
            }
        }
        catch (e_2_1) { e_2 = { error: e_2_1 }; }
        finally {
            try {
                if (_f && !_f.done && (_b = _e.return)) _b.call(_e);
            }
            finally { if (e_2) throw e_2.error; }
        }
        if (message.inMatch === true) {
            writer.uint32(40).bool(message.inMatch);
        }
        if (message.name !== "") {
            writer.uint32(50).string(message.name);
        }
        if (message.personalCollection !== undefined) {
            exports.InventoryCollection.encode(message.personalCollection, writer.uint32(58).fork()).ldelim();
        }
        if (message.privacyToken !== "") {
            writer.uint32(66).string(message.privacyToken);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseAccount();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
                    message.decks.push(exports.InventoryCollection.decode(reader, reader.uint32()));
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
                    message.friends.push(exports.Friend.decode(reader, reader.uint32()));
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
                    message.personalCollection = exports.InventoryCollection.decode(reader, reader.uint32());
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
    create: function (base) {
        return exports.Account.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b, _c, _d, _e, _f, _g;
        var message = createBaseAccount();
        message.Id = (_a = object.Id) !== null && _a !== void 0 ? _a : "";
        message.decks = ((_b = object.decks) === null || _b === void 0 ? void 0 : _b.map(function (e) { return exports.InventoryCollection.fromPartial(e); })) || [];
        message.email = (_c = object.email) !== null && _c !== void 0 ? _c : "";
        message.friends = ((_d = object.friends) === null || _d === void 0 ? void 0 : _d.map(function (e) { return exports.Friend.fromPartial(e); })) || [];
        message.inMatch = (_e = object.inMatch) !== null && _e !== void 0 ? _e : false;
        message.name = (_f = object.name) !== null && _f !== void 0 ? _f : "";
        message.personalCollection = object.personalCollection !== undefined && object.personalCollection !== null ? exports.InventoryCollection.fromPartial(object.personalCollection) : undefined;
        message.privacyToken = (_g = object.privacyToken) !== null && _g !== void 0 ? _g : "";
        return message;
    },
};
function createBaseArt() {
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
exports.Art = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.body !== undefined) {
            exports.Font.encode(message.body, writer.uint32(10).fork()).ldelim();
        }
        if (message.highlight !== undefined) {
            exports.Color.encode(message.highlight, writer.uint32(18).fork()).ldelim();
        }
        if (message.loop !== undefined) {
            exports.Prefab.encode(message.loop, writer.uint32(26).fork()).ldelim();
        }
        if (message.missile !== undefined) {
            exports.Prefab.encode(message.missile, writer.uint32(34).fork()).ldelim();
        }
        if (message.onCast !== undefined) {
            exports.Prefab.encode(message.onCast, writer.uint32(42).fork()).ldelim();
        }
        if (message.onHit !== undefined) {
            exports.Prefab.encode(message.onHit, writer.uint32(50).fork()).ldelim();
        }
        if (message.primary !== undefined) {
            exports.Color.encode(message.primary, writer.uint32(58).fork()).ldelim();
        }
        if (message.secondary !== undefined) {
            exports.Color.encode(message.secondary, writer.uint32(66).fork()).ldelim();
        }
        if (message.shadow !== undefined) {
            exports.Color.encode(message.shadow, writer.uint32(74).fork()).ldelim();
        }
        if (message.spell !== undefined) {
            exports.Prefab.encode(message.spell, writer.uint32(82).fork()).ldelim();
        }
        if (message.sprite !== undefined) {
            exports.Sprite.encode(message.sprite, writer.uint32(90).fork()).ldelim();
        }
        if (message.spriteShadow !== undefined) {
            exports.Sprite.encode(message.spriteShadow, writer.uint32(98).fork()).ldelim();
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseArt();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
                case 1:
                    if (tag !== 10) {
                        break;
                    }
                    message.body = exports.Font.decode(reader, reader.uint32());
                    continue;
                case 2:
                    if (tag !== 18) {
                        break;
                    }
                    message.highlight = exports.Color.decode(reader, reader.uint32());
                    continue;
                case 3:
                    if (tag !== 26) {
                        break;
                    }
                    message.loop = exports.Prefab.decode(reader, reader.uint32());
                    continue;
                case 4:
                    if (tag !== 34) {
                        break;
                    }
                    message.missile = exports.Prefab.decode(reader, reader.uint32());
                    continue;
                case 5:
                    if (tag !== 42) {
                        break;
                    }
                    message.onCast = exports.Prefab.decode(reader, reader.uint32());
                    continue;
                case 6:
                    if (tag !== 50) {
                        break;
                    }
                    message.onHit = exports.Prefab.decode(reader, reader.uint32());
                    continue;
                case 7:
                    if (tag !== 58) {
                        break;
                    }
                    message.primary = exports.Color.decode(reader, reader.uint32());
                    continue;
                case 8:
                    if (tag !== 66) {
                        break;
                    }
                    message.secondary = exports.Color.decode(reader, reader.uint32());
                    continue;
                case 9:
                    if (tag !== 74) {
                        break;
                    }
                    message.shadow = exports.Color.decode(reader, reader.uint32());
                    continue;
                case 10:
                    if (tag !== 82) {
                        break;
                    }
                    message.spell = exports.Prefab.decode(reader, reader.uint32());
                    continue;
                case 11:
                    if (tag !== 90) {
                        break;
                    }
                    message.sprite = exports.Sprite.decode(reader, reader.uint32());
                    continue;
                case 12:
                    if (tag !== 98) {
                        break;
                    }
                    message.spriteShadow = exports.Sprite.decode(reader, reader.uint32());
                    continue;
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.Art.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var message = createBaseArt();
        message.body = object.body !== undefined && object.body !== null ? exports.Font.fromPartial(object.body) : undefined;
        message.highlight = object.highlight !== undefined && object.highlight !== null ? exports.Color.fromPartial(object.highlight) : undefined;
        message.loop = object.loop !== undefined && object.loop !== null ? exports.Prefab.fromPartial(object.loop) : undefined;
        message.missile = object.missile !== undefined && object.missile !== null ? exports.Prefab.fromPartial(object.missile) : undefined;
        message.onCast = object.onCast !== undefined && object.onCast !== null ? exports.Prefab.fromPartial(object.onCast) : undefined;
        message.onHit = object.onHit !== undefined && object.onHit !== null ? exports.Prefab.fromPartial(object.onHit) : undefined;
        message.primary = object.primary !== undefined && object.primary !== null ? exports.Color.fromPartial(object.primary) : undefined;
        message.secondary = object.secondary !== undefined && object.secondary !== null ? exports.Color.fromPartial(object.secondary) : undefined;
        message.shadow = object.shadow !== undefined && object.shadow !== null ? exports.Color.fromPartial(object.shadow) : undefined;
        message.spell = object.spell !== undefined && object.spell !== null ? exports.Prefab.fromPartial(object.spell) : undefined;
        message.sprite = object.sprite !== undefined && object.sprite !== null ? exports.Sprite.fromPartial(object.sprite) : undefined;
        message.spriteShadow = object.spriteShadow !== undefined && object.spriteShadow !== null ? exports.Sprite.fromPartial(object.spriteShadow) : undefined;
        return message;
    },
};
function createBaseArtificialIntelligence() {
    return { hardRemoval: false };
}
exports.ArtificialIntelligence = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.hardRemoval === true) {
            writer.uint32(8).bool(message.hardRemoval);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseArtificialIntelligence();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.ArtificialIntelligence.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBaseArtificialIntelligence();
        message.hardRemoval = (_a = object.hardRemoval) !== null && _a !== void 0 ? _a : false;
        return message;
    },
};
function createBaseDraft() {
    return { banned: false };
}
exports.Draft = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.banned === true) {
            writer.uint32(8).bool(message.banned);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseDraft();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.Draft.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBaseDraft();
        message.banned = (_a = object.banned) !== null && _a !== void 0 ? _a : false;
        return message;
    },
};
function createBaseAttributeValueTuple() {
    return { attribute: 0, stringValue: "" };
}
exports.AttributeValueTuple = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.attribute !== 0) {
            writer.uint32(8).int32(message.attribute);
        }
        if (message.stringValue !== "") {
            writer.uint32(18).string(message.stringValue);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseAttributeValueTuple();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
                case 1:
                    if (tag !== 8) {
                        break;
                    }
                    message.attribute = reader.int32();
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
    create: function (base) {
        return exports.AttributeValueTuple.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b;
        var message = createBaseAttributeValueTuple();
        message.attribute = (_a = object.attribute) !== null && _a !== void 0 ? _a : 0;
        message.stringValue = (_b = object.stringValue) !== null && _b !== void 0 ? _b : "";
        return message;
    },
};
function createBaseCardEvent() {
    return { card: undefined, showLocal: false };
}
exports.CardEvent = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.card !== undefined) {
            exports.Entity.encode(message.card, writer.uint32(10).fork()).ldelim();
        }
        if (message.showLocal === true) {
            writer.uint32(16).bool(message.showLocal);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseCardEvent();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
                case 1:
                    if (tag !== 10) {
                        break;
                    }
                    message.card = exports.Entity.decode(reader, reader.uint32());
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
    create: function (base) {
        return exports.CardEvent.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBaseCardEvent();
        message.card = object.card !== undefined && object.card !== null ? exports.Entity.fromPartial(object.card) : undefined;
        message.showLocal = (_a = object.showLocal) !== null && _a !== void 0 ? _a : false;
        return message;
    },
};
function createBaseCardRecord() {
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
exports.CardRecord = {
    encode: function (message, writer) {
        var e_3, _a;
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.id !== 0) {
            writer.uint32(8).int64(message.id);
        }
        if (message.allianceId !== "") {
            writer.uint32(18).string(message.allianceId);
        }
        if (message.borrowedByUserId !== "") {
            writer.uint32(26).string(message.borrowedByUserId);
        }
        try {
            for (var _b = __values(message.collectionIds), _c = _b.next(); !_c.done; _c = _b.next()) {
                var v = _c.value;
                writer.uint32(34).string(v);
            }
        }
        catch (e_3_1) { e_3 = { error: e_3_1 }; }
        finally {
            try {
                if (_c && !_c.done && (_a = _b.return)) _a.call(_b);
            }
            finally { if (e_3) throw e_3.error; }
        }
        if (message.donorUserId !== "") {
            writer.uint32(42).string(message.donorUserId);
        }
        if (message.entity !== undefined) {
            exports.Entity.encode(message.entity, writer.uint32(50).fork()).ldelim();
        }
        if (message.userId !== "") {
            writer.uint32(58).string(message.userId);
        }
        if (message.count !== 0) {
            writer.uint32(64).int32(message.count);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseCardRecord();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
                case 1:
                    if (tag !== 8) {
                        break;
                    }
                    message.id = longToNumber(reader.int64());
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
                    message.entity = exports.Entity.decode(reader, reader.uint32());
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
    create: function (base) {
        return exports.CardRecord.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b, _c, _d, _e, _f, _g;
        var message = createBaseCardRecord();
        message.id = (_a = object.id) !== null && _a !== void 0 ? _a : 0;
        message.allianceId = (_b = object.allianceId) !== null && _b !== void 0 ? _b : "";
        message.borrowedByUserId = (_c = object.borrowedByUserId) !== null && _c !== void 0 ? _c : "";
        message.collectionIds = ((_d = object.collectionIds) === null || _d === void 0 ? void 0 : _d.map(function (e) { return e; })) || [];
        message.donorUserId = (_e = object.donorUserId) !== null && _e !== void 0 ? _e : "";
        message.entity = object.entity !== undefined && object.entity !== null ? exports.Entity.fromPartial(object.entity) : undefined;
        message.userId = (_f = object.userId) !== null && _f !== void 0 ? _f : "";
        message.count = (_g = object.count) !== null && _g !== void 0 ? _g : 0;
        return message;
    },
};
function createBaseChangePasswordRequest() {
    return { password: "" };
}
exports.ChangePasswordRequest = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.password !== "") {
            writer.uint32(10).string(message.password);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseChangePasswordRequest();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.ChangePasswordRequest.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBaseChangePasswordRequest();
        message.password = (_a = object.password) !== null && _a !== void 0 ? _a : "";
        return message;
    },
};
function createBaseChangePasswordResponse() {
    return {};
}
exports.ChangePasswordResponse = {
    encode: function (_, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseChangePasswordResponse();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.ChangePasswordResponse.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (_) {
        var message = createBaseChangePasswordResponse();
        return message;
    },
};
function createBaseChatMessage() {
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
exports.ChatMessage = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
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
            reactive_1.AddedChangedRemoved.encode(message.notification, writer.uint32(66).fork()).ldelim();
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseChatMessage();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
                    message.timestamp = longToNumber(reader.int64());
                    continue;
                case 8:
                    if (tag !== 66) {
                        break;
                    }
                    message.notification = reactive_1.AddedChangedRemoved.decode(reader, reader.uint32());
                    continue;
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.ChatMessage.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b, _c, _d, _e, _f, _g;
        var message = createBaseChatMessage();
        message.conversationId = (_a = object.conversationId) !== null && _a !== void 0 ? _a : "";
        message.dateLabel = (_b = object.dateLabel) !== null && _b !== void 0 ? _b : "";
        message.message = (_c = object.message) !== null && _c !== void 0 ? _c : "";
        message.messageId = (_d = object.messageId) !== null && _d !== void 0 ? _d : "";
        message.senderName = (_e = object.senderName) !== null && _e !== void 0 ? _e : "";
        message.senderUserId = (_f = object.senderUserId) !== null && _f !== void 0 ? _f : "";
        message.timestamp = (_g = object.timestamp) !== null && _g !== void 0 ? _g : 0;
        message.notification = object.notification !== undefined && object.notification !== null ? reactive_1.AddedChangedRemoved.fromPartial(object.notification) : undefined;
        return message;
    },
};
function createBaseClientToServerMessage() {
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
exports.ClientToServerMessage = {
    encode: function (message, writer) {
        var e_4, _a;
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.actionIndex !== 0) {
            writer.uint32(8).int32(message.actionIndex);
        }
        writer.uint32(18).fork();
        try {
            for (var _b = __values(message.discardedCardIndices), _c = _b.next(); !_c.done; _c = _b.next()) {
                var v = _c.value;
                writer.int32(v);
            }
        }
        catch (e_4_1) { e_4 = { error: e_4_1 }; }
        finally {
            try {
                if (_c && !_c.done && (_a = _b.return)) _a.call(_b);
            }
            finally { if (e_4) throw e_4.error; }
        }
        writer.ldelim();
        if (message.emote !== undefined) {
            exports.Emote.encode(message.emote, writer.uint32(26).fork()).ldelim();
        }
        if (message.entityTouch !== undefined) {
            writer.uint32(32).int32(message.entityTouch);
        }
        if (message.entityUntouch !== undefined) {
            writer.uint32(40).int32(message.entityUntouch);
        }
        if (message.firstMessage !== undefined) {
            exports.ClientToServerMessage_FirstMessageMessage.encode(message.firstMessage, writer.uint32(50).fork()).ldelim();
        }
        if (message.messageType !== 0) {
            writer.uint32(56).int32(message.messageType);
        }
        if (message.repliesTo !== "") {
            writer.uint32(66).string(message.repliesTo);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseClientToServerMessage();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
                        var end2 = reader.uint32() + reader.pos;
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
                    message.emote = exports.Emote.decode(reader, reader.uint32());
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
                    message.firstMessage = exports.ClientToServerMessage_FirstMessageMessage.decode(reader, reader.uint32());
                    continue;
                case 7:
                    if (tag !== 56) {
                        break;
                    }
                    message.messageType = reader.int32();
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
    create: function (base) {
        return exports.ClientToServerMessage.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b, _c, _d, _e, _f;
        var message = createBaseClientToServerMessage();
        message.actionIndex = (_a = object.actionIndex) !== null && _a !== void 0 ? _a : 0;
        message.discardedCardIndices = ((_b = object.discardedCardIndices) === null || _b === void 0 ? void 0 : _b.map(function (e) { return e; })) || [];
        message.emote = object.emote !== undefined && object.emote !== null ? exports.Emote.fromPartial(object.emote) : undefined;
        message.entityTouch = (_c = object.entityTouch) !== null && _c !== void 0 ? _c : undefined;
        message.entityUntouch = (_d = object.entityUntouch) !== null && _d !== void 0 ? _d : undefined;
        message.firstMessage = object.firstMessage !== undefined && object.firstMessage !== null ? exports.ClientToServerMessage_FirstMessageMessage.fromPartial(object.firstMessage) : undefined;
        message.messageType = (_e = object.messageType) !== null && _e !== void 0 ? _e : 0;
        message.repliesTo = (_f = object.repliesTo) !== null && _f !== void 0 ? _f : "";
        return message;
    },
};
function createBaseClientToServerMessage_FirstMessageMessage() {
    return { playerKey: "", playerSecret: "" };
}
exports.ClientToServerMessage_FirstMessageMessage = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.playerKey !== "") {
            writer.uint32(10).string(message.playerKey);
        }
        if (message.playerSecret !== "") {
            writer.uint32(18).string(message.playerSecret);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseClientToServerMessage_FirstMessageMessage();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.ClientToServerMessage_FirstMessageMessage.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b;
        var message = createBaseClientToServerMessage_FirstMessageMessage();
        message.playerKey = (_a = object.playerKey) !== null && _a !== void 0 ? _a : "";
        message.playerSecret = (_b = object.playerSecret) !== null && _b !== void 0 ? _b : "";
        return message;
    },
};
function createBaseColor() {
    return { a: 0, b: 0, g: 0, r: 0 };
}
exports.Color = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
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
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseColor();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.Color.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b, _c, _d;
        var message = createBaseColor();
        message.a = (_a = object.a) !== null && _a !== void 0 ? _a : 0;
        message.b = (_b = object.b) !== null && _b !== void 0 ? _b : 0;
        message.g = (_c = object.g) !== null && _c !== void 0 ? _c : 0;
        message.r = (_d = object.r) !== null && _d !== void 0 ? _d : 0;
        return message;
    },
};
function createBaseCreateAccountRequest() {
    return { email: "", name: "", password: "" };
}
exports.CreateAccountRequest = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
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
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseCreateAccountRequest();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.CreateAccountRequest.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b, _c;
        var message = createBaseCreateAccountRequest();
        message.email = (_a = object.email) !== null && _a !== void 0 ? _a : "";
        message.name = (_b = object.name) !== null && _b !== void 0 ? _b : "";
        message.password = (_c = object.password) !== null && _c !== void 0 ? _c : "";
        return message;
    },
};
function createBaseCreateAccountResponse() {
    return { account: undefined, loginToken: "" };
}
exports.CreateAccountResponse = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.account !== undefined) {
            exports.Account.encode(message.account, writer.uint32(10).fork()).ldelim();
        }
        if (message.loginToken !== "") {
            writer.uint32(18).string(message.loginToken);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseCreateAccountResponse();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
                case 1:
                    if (tag !== 10) {
                        break;
                    }
                    message.account = exports.Account.decode(reader, reader.uint32());
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
    create: function (base) {
        return exports.CreateAccountResponse.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBaseCreateAccountResponse();
        message.account = object.account !== undefined && object.account !== null ? exports.Account.fromPartial(object.account) : undefined;
        message.loginToken = (_a = object.loginToken) !== null && _a !== void 0 ? _a : "";
        return message;
    },
};
function createBaseDecksDeleteRequest() {
    return { deckId: "" };
}
exports.DecksDeleteRequest = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.deckId !== "") {
            writer.uint32(10).string(message.deckId);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseDecksDeleteRequest();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.DecksDeleteRequest.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBaseDecksDeleteRequest();
        message.deckId = (_a = object.deckId) !== null && _a !== void 0 ? _a : "";
        return message;
    },
};
function createBaseDecksGetAllResponse() {
    return { decks: [] };
}
exports.DecksGetAllResponse = {
    encode: function (message, writer) {
        var e_5, _a;
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        try {
            for (var _b = __values(message.decks), _c = _b.next(); !_c.done; _c = _b.next()) {
                var v = _c.value;
                exports.DecksGetResponse.encode(v, writer.uint32(10).fork()).ldelim();
            }
        }
        catch (e_5_1) { e_5 = { error: e_5_1 }; }
        finally {
            try {
                if (_c && !_c.done && (_a = _b.return)) _a.call(_b);
            }
            finally { if (e_5) throw e_5.error; }
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseDecksGetAllResponse();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
                case 1:
                    if (tag !== 10) {
                        break;
                    }
                    message.decks.push(exports.DecksGetResponse.decode(reader, reader.uint32()));
                    continue;
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.DecksGetAllResponse.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBaseDecksGetAllResponse();
        message.decks = ((_a = object.decks) === null || _a === void 0 ? void 0 : _a.map(function (e) { return exports.DecksGetResponse.fromPartial(e); })) || [];
        return message;
    },
};
function createBaseDecksGetRequest() {
    return { deckId: "" };
}
exports.DecksGetRequest = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.deckId !== "") {
            writer.uint32(10).string(message.deckId);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseDecksGetRequest();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.DecksGetRequest.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBaseDecksGetRequest();
        message.deckId = (_a = object.deckId) !== null && _a !== void 0 ? _a : "";
        return message;
    },
};
function createBaseDecksGetResponse() {
    return { collection: undefined, inventoryIdsSize: 0 };
}
exports.DecksGetResponse = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.collection !== undefined) {
            exports.InventoryCollection.encode(message.collection, writer.uint32(10).fork()).ldelim();
        }
        if (message.inventoryIdsSize !== 0) {
            writer.uint32(16).int32(message.inventoryIdsSize);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseDecksGetResponse();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
                case 1:
                    if (tag !== 10) {
                        break;
                    }
                    message.collection = exports.InventoryCollection.decode(reader, reader.uint32());
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
    create: function (base) {
        return exports.DecksGetResponse.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBaseDecksGetResponse();
        message.collection = object.collection !== undefined && object.collection !== null ? exports.InventoryCollection.fromPartial(object.collection) : undefined;
        message.inventoryIdsSize = (_a = object.inventoryIdsSize) !== null && _a !== void 0 ? _a : 0;
        return message;
    },
};
function createBaseDecksPutRequest() {
    return { deckList: "", format: "", heroClass: "", inventoryIds: [], name: "", cardIds: [] };
}
exports.DecksPutRequest = {
    encode: function (message, writer) {
        var e_6, _a, e_7, _b;
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.deckList !== "") {
            writer.uint32(10).string(message.deckList);
        }
        if (message.format !== "") {
            writer.uint32(18).string(message.format);
        }
        if (message.heroClass !== "") {
            writer.uint32(26).string(message.heroClass);
        }
        try {
            for (var _c = __values(message.inventoryIds), _d = _c.next(); !_d.done; _d = _c.next()) {
                var v = _d.value;
                writer.uint32(34).string(v);
            }
        }
        catch (e_6_1) { e_6 = { error: e_6_1 }; }
        finally {
            try {
                if (_d && !_d.done && (_a = _c.return)) _a.call(_c);
            }
            finally { if (e_6) throw e_6.error; }
        }
        if (message.name !== "") {
            writer.uint32(42).string(message.name);
        }
        try {
            for (var _e = __values(message.cardIds), _f = _e.next(); !_f.done; _f = _e.next()) {
                var v = _f.value;
                writer.uint32(50).string(v);
            }
        }
        catch (e_7_1) { e_7 = { error: e_7_1 }; }
        finally {
            try {
                if (_f && !_f.done && (_b = _e.return)) _b.call(_e);
            }
            finally { if (e_7) throw e_7.error; }
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseDecksPutRequest();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.DecksPutRequest.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b, _c, _d, _e, _f;
        var message = createBaseDecksPutRequest();
        message.deckList = (_a = object.deckList) !== null && _a !== void 0 ? _a : "";
        message.format = (_b = object.format) !== null && _b !== void 0 ? _b : "";
        message.heroClass = (_c = object.heroClass) !== null && _c !== void 0 ? _c : "";
        message.inventoryIds = ((_d = object.inventoryIds) === null || _d === void 0 ? void 0 : _d.map(function (e) { return e; })) || [];
        message.name = (_e = object.name) !== null && _e !== void 0 ? _e : "";
        message.cardIds = ((_f = object.cardIds) === null || _f === void 0 ? void 0 : _f.map(function (e) { return e; })) || [];
        return message;
    },
};
function createBaseDecksPutResponse() {
    return { collection: undefined, deckId: "" };
}
exports.DecksPutResponse = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.collection !== undefined) {
            exports.InventoryCollection.encode(message.collection, writer.uint32(10).fork()).ldelim();
        }
        if (message.deckId !== "") {
            writer.uint32(18).string(message.deckId);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseDecksPutResponse();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
                case 1:
                    if (tag !== 10) {
                        break;
                    }
                    message.collection = exports.InventoryCollection.decode(reader, reader.uint32());
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
    create: function (base) {
        return exports.DecksPutResponse.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBaseDecksPutResponse();
        message.collection = object.collection !== undefined && object.collection !== null ? exports.InventoryCollection.fromPartial(object.collection) : undefined;
        message.deckId = (_a = object.deckId) !== null && _a !== void 0 ? _a : "";
        return message;
    },
};
function createBaseDecksUpdateCommand() {
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
exports.DecksUpdateCommand = {
    encode: function (message, writer) {
        var e_8, _a, e_9, _b, e_10, _c;
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        try {
            for (var _d = __values(message.pullAllCardIds), _e = _d.next(); !_e.done; _e = _d.next()) {
                var v = _e.value;
                writer.uint32(10).string(v);
            }
        }
        catch (e_8_1) { e_8 = { error: e_8_1 }; }
        finally {
            try {
                if (_e && !_e.done && (_a = _d.return)) _a.call(_d);
            }
            finally { if (e_8) throw e_8.error; }
        }
        writer.uint32(18).fork();
        try {
            for (var _f = __values(message.pullAllInventoryIds), _g = _f.next(); !_g.done; _g = _f.next()) {
                var v = _g.value;
                writer.int64(v);
            }
        }
        catch (e_9_1) { e_9 = { error: e_9_1 }; }
        finally {
            try {
                if (_g && !_g.done && (_b = _f.return)) _b.call(_f);
            }
            finally { if (e_9) throw e_9.error; }
        }
        writer.ldelim();
        if (message.pushCardIds !== undefined) {
            exports.DecksUpdateCommand_PushCardIdsMessage.encode(message.pushCardIds, writer.uint32(26).fork()).ldelim();
        }
        if (message.pushInventoryIds !== undefined) {
            exports.DecksUpdateCommand_PushInventoryIdsMessage.encode(message.pushInventoryIds, writer.uint32(34).fork()).ldelim();
        }
        if (message.setHeroClass !== "") {
            writer.uint32(42).string(message.setHeroClass);
        }
        writer.uint32(50).fork();
        try {
            for (var _h = __values(message.setInventoryIds), _j = _h.next(); !_j.done; _j = _h.next()) {
                var v = _j.value;
                writer.int64(v);
            }
        }
        catch (e_10_1) { e_10 = { error: e_10_1 }; }
        finally {
            try {
                if (_j && !_j.done && (_c = _h.return)) _c.call(_h);
            }
            finally { if (e_10) throw e_10.error; }
        }
        writer.ldelim();
        if (message.setName !== "") {
            writer.uint32(58).string(message.setName);
        }
        if (message.setPlayerEntityAttribute !== undefined) {
            exports.DecksUpdateCommand_SetPlayerEntityAttributeMessage.encode(message.setPlayerEntityAttribute, writer.uint32(66).fork()).ldelim();
        }
        if (message.unsetPlayerEntityAttribute !== "") {
            writer.uint32(74).string(message.unsetPlayerEntityAttribute);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseDecksUpdateCommand();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
                case 1:
                    if (tag !== 10) {
                        break;
                    }
                    message.pullAllCardIds.push(reader.string());
                    continue;
                case 2:
                    if (tag === 16) {
                        message.pullAllInventoryIds.push(longToNumber(reader.int64()));
                        continue;
                    }
                    if (tag === 18) {
                        var end2 = reader.uint32() + reader.pos;
                        while (reader.pos < end2) {
                            message.pullAllInventoryIds.push(longToNumber(reader.int64()));
                        }
                        continue;
                    }
                    break;
                case 3:
                    if (tag !== 26) {
                        break;
                    }
                    message.pushCardIds = exports.DecksUpdateCommand_PushCardIdsMessage.decode(reader, reader.uint32());
                    continue;
                case 4:
                    if (tag !== 34) {
                        break;
                    }
                    message.pushInventoryIds = exports.DecksUpdateCommand_PushInventoryIdsMessage.decode(reader, reader.uint32());
                    continue;
                case 5:
                    if (tag !== 42) {
                        break;
                    }
                    message.setHeroClass = reader.string();
                    continue;
                case 6:
                    if (tag === 48) {
                        message.setInventoryIds.push(longToNumber(reader.int64()));
                        continue;
                    }
                    if (tag === 50) {
                        var end2 = reader.uint32() + reader.pos;
                        while (reader.pos < end2) {
                            message.setInventoryIds.push(longToNumber(reader.int64()));
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
                    message.setPlayerEntityAttribute = exports.DecksUpdateCommand_SetPlayerEntityAttributeMessage.decode(reader, reader.uint32());
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
    create: function (base) {
        return exports.DecksUpdateCommand.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b, _c, _d, _e, _f;
        var message = createBaseDecksUpdateCommand();
        message.pullAllCardIds = ((_a = object.pullAllCardIds) === null || _a === void 0 ? void 0 : _a.map(function (e) { return e; })) || [];
        message.pullAllInventoryIds = ((_b = object.pullAllInventoryIds) === null || _b === void 0 ? void 0 : _b.map(function (e) { return e; })) || [];
        message.pushCardIds = object.pushCardIds !== undefined && object.pushCardIds !== null ? exports.DecksUpdateCommand_PushCardIdsMessage.fromPartial(object.pushCardIds) : undefined;
        message.pushInventoryIds = object.pushInventoryIds !== undefined && object.pushInventoryIds !== null ? exports.DecksUpdateCommand_PushInventoryIdsMessage.fromPartial(object.pushInventoryIds) : undefined;
        message.setHeroClass = (_c = object.setHeroClass) !== null && _c !== void 0 ? _c : "";
        message.setInventoryIds = ((_d = object.setInventoryIds) === null || _d === void 0 ? void 0 : _d.map(function (e) { return e; })) || [];
        message.setName = (_e = object.setName) !== null && _e !== void 0 ? _e : "";
        message.setPlayerEntityAttribute = object.setPlayerEntityAttribute !== undefined && object.setPlayerEntityAttribute !== null ? exports.DecksUpdateCommand_SetPlayerEntityAttributeMessage.fromPartial(object.setPlayerEntityAttribute) : undefined;
        message.unsetPlayerEntityAttribute = (_f = object.unsetPlayerEntityAttribute) !== null && _f !== void 0 ? _f : "";
        return message;
    },
};
function createBaseDecksUpdateCommand_PushCardIdsMessage() {
    return { Each: [] };
}
exports.DecksUpdateCommand_PushCardIdsMessage = {
    encode: function (message, writer) {
        var e_11, _a;
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        try {
            for (var _b = __values(message.Each), _c = _b.next(); !_c.done; _c = _b.next()) {
                var v = _c.value;
                writer.uint32(10).string(v);
            }
        }
        catch (e_11_1) { e_11 = { error: e_11_1 }; }
        finally {
            try {
                if (_c && !_c.done && (_a = _b.return)) _a.call(_b);
            }
            finally { if (e_11) throw e_11.error; }
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseDecksUpdateCommand_PushCardIdsMessage();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.DecksUpdateCommand_PushCardIdsMessage.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBaseDecksUpdateCommand_PushCardIdsMessage();
        message.Each = ((_a = object.Each) === null || _a === void 0 ? void 0 : _a.map(function (e) { return e; })) || [];
        return message;
    },
};
function createBaseDecksUpdateCommand_PushInventoryIdsMessage() {
    return { Each: [] };
}
exports.DecksUpdateCommand_PushInventoryIdsMessage = {
    encode: function (message, writer) {
        var e_12, _a;
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        try {
            for (var _b = __values(message.Each), _c = _b.next(); !_c.done; _c = _b.next()) {
                var v = _c.value;
                writer.uint32(10).string(v);
            }
        }
        catch (e_12_1) { e_12 = { error: e_12_1 }; }
        finally {
            try {
                if (_c && !_c.done && (_a = _b.return)) _a.call(_b);
            }
            finally { if (e_12) throw e_12.error; }
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseDecksUpdateCommand_PushInventoryIdsMessage();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.DecksUpdateCommand_PushInventoryIdsMessage.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBaseDecksUpdateCommand_PushInventoryIdsMessage();
        message.Each = ((_a = object.Each) === null || _a === void 0 ? void 0 : _a.map(function (e) { return e; })) || [];
        return message;
    },
};
function createBaseDecksUpdateCommand_SetPlayerEntityAttributeMessage() {
    return { attribute: 0, stringValue: "" };
}
exports.DecksUpdateCommand_SetPlayerEntityAttributeMessage = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.attribute !== 0) {
            writer.uint32(8).int32(message.attribute);
        }
        if (message.stringValue !== "") {
            writer.uint32(18).string(message.stringValue);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseDecksUpdateCommand_SetPlayerEntityAttributeMessage();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
                case 1:
                    if (tag !== 8) {
                        break;
                    }
                    message.attribute = reader.int32();
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
    create: function (base) {
        return exports.DecksUpdateCommand_SetPlayerEntityAttributeMessage.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b;
        var message = createBaseDecksUpdateCommand_SetPlayerEntityAttributeMessage();
        message.attribute = (_a = object.attribute) !== null && _a !== void 0 ? _a : 0;
        message.stringValue = (_b = object.stringValue) !== null && _b !== void 0 ? _b : "";
        return message;
    },
};
function createBaseDecksUpdateRequest() {
    return { deckId: "", updateCommand: undefined };
}
exports.DecksUpdateRequest = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.deckId !== "") {
            writer.uint32(10).string(message.deckId);
        }
        if (message.updateCommand !== undefined) {
            exports.DecksUpdateCommand.encode(message.updateCommand, writer.uint32(18).fork()).ldelim();
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseDecksUpdateRequest();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
                    message.updateCommand = exports.DecksUpdateCommand.decode(reader, reader.uint32());
                    continue;
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.DecksUpdateRequest.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBaseDecksUpdateRequest();
        message.deckId = (_a = object.deckId) !== null && _a !== void 0 ? _a : "";
        message.updateCommand = object.updateCommand !== undefined && object.updateCommand !== null ? exports.DecksUpdateCommand.fromPartial(object.updateCommand) : undefined;
        return message;
    },
};
function createBaseDefaultMethodResponse() {
    return {};
}
exports.DefaultMethodResponse = {
    encode: function (_, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseDefaultMethodResponse();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.DefaultMethodResponse.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (_) {
        var message = createBaseDefaultMethodResponse();
        return message;
    },
};
function createBaseDeleteInviteRequest() {
    return { inviteId: "" };
}
exports.DeleteInviteRequest = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.inviteId !== "") {
            writer.uint32(10).string(message.inviteId);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseDeleteInviteRequest();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.DeleteInviteRequest.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBaseDeleteInviteRequest();
        message.inviteId = (_a = object.inviteId) !== null && _a !== void 0 ? _a : "";
        return message;
    },
};
function createBaseDestroy() {
    return { aftermaths: [], source: undefined, target: undefined };
}
exports.Destroy = {
    encode: function (message, writer) {
        var e_13, _a;
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        try {
            for (var _b = __values(message.aftermaths), _c = _b.next(); !_c.done; _c = _b.next()) {
                var v = _c.value;
                exports.Entity.encode(v, writer.uint32(10).fork()).ldelim();
            }
        }
        catch (e_13_1) { e_13 = { error: e_13_1 }; }
        finally {
            try {
                if (_c && !_c.done && (_a = _b.return)) _a.call(_b);
            }
            finally { if (e_13) throw e_13.error; }
        }
        if (message.source !== undefined) {
            exports.Entity.encode(message.source, writer.uint32(18).fork()).ldelim();
        }
        if (message.target !== undefined) {
            exports.Entity.encode(message.target, writer.uint32(26).fork()).ldelim();
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseDestroy();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
                case 1:
                    if (tag !== 10) {
                        break;
                    }
                    message.aftermaths.push(exports.Entity.decode(reader, reader.uint32()));
                    continue;
                case 2:
                    if (tag !== 18) {
                        break;
                    }
                    message.source = exports.Entity.decode(reader, reader.uint32());
                    continue;
                case 3:
                    if (tag !== 26) {
                        break;
                    }
                    message.target = exports.Entity.decode(reader, reader.uint32());
                    continue;
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.Destroy.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBaseDestroy();
        message.aftermaths = ((_a = object.aftermaths) === null || _a === void 0 ? void 0 : _a.map(function (e) { return exports.Entity.fromPartial(e); })) || [];
        message.source = object.source !== undefined && object.source !== null ? exports.Entity.fromPartial(object.source) : undefined;
        message.target = object.target !== undefined && object.target !== null ? exports.Entity.fromPartial(object.target) : undefined;
        return message;
    },
};
function createBaseDraftState() {
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
exports.DraftState = {
    encode: function (message, writer) {
        var e_14, _a, e_15, _b, e_16, _c;
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.cardsRemaining !== 0) {
            writer.uint32(8).int32(message.cardsRemaining);
        }
        try {
            for (var _d = __values(message.currentCardChoices), _e = _d.next(); !_e.done; _e = _d.next()) {
                var v = _e.value;
                exports.Entity.encode(v, writer.uint32(18).fork()).ldelim();
            }
        }
        catch (e_14_1) { e_14 = { error: e_14_1 }; }
        finally {
            try {
                if (_e && !_e.done && (_a = _d.return)) _a.call(_d);
            }
            finally { if (e_14) throw e_14.error; }
        }
        if (message.deckId !== "") {
            writer.uint32(26).string(message.deckId);
        }
        if (message.draftIndex !== 0) {
            writer.uint32(32).int32(message.draftIndex);
        }
        if (message.heroClass !== undefined) {
            exports.Entity.encode(message.heroClass, writer.uint32(42).fork()).ldelim();
        }
        try {
            for (var _f = __values(message.heroClassChoices), _g = _f.next(); !_g.done; _g = _f.next()) {
                var v = _g.value;
                exports.Entity.encode(v, writer.uint32(50).fork()).ldelim();
            }
        }
        catch (e_15_1) { e_15 = { error: e_15_1 }; }
        finally {
            try {
                if (_g && !_g.done && (_b = _f.return)) _b.call(_f);
            }
            finally { if (e_15) throw e_15.error; }
        }
        if (message.losses !== 0) {
            writer.uint32(56).int32(message.losses);
        }
        try {
            for (var _h = __values(message.selectedCardIds), _j = _h.next(); !_j.done; _j = _h.next()) {
                var v = _j.value;
                writer.uint32(66).string(v);
            }
        }
        catch (e_16_1) { e_16 = { error: e_16_1 }; }
        finally {
            try {
                if (_j && !_j.done && (_c = _h.return)) _c.call(_h);
            }
            finally { if (e_16) throw e_16.error; }
        }
        if (message.status !== 0) {
            writer.uint32(72).int32(message.status);
        }
        if (message.wins !== 0) {
            writer.uint32(80).int32(message.wins);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseDraftState();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
                    message.currentCardChoices.push(exports.Entity.decode(reader, reader.uint32()));
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
                    message.heroClass = exports.Entity.decode(reader, reader.uint32());
                    continue;
                case 6:
                    if (tag !== 50) {
                        break;
                    }
                    message.heroClassChoices.push(exports.Entity.decode(reader, reader.uint32()));
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
                    message.status = reader.int32();
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
    create: function (base) {
        return exports.DraftState.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b, _c, _d, _e, _f, _g, _h, _j;
        var message = createBaseDraftState();
        message.cardsRemaining = (_a = object.cardsRemaining) !== null && _a !== void 0 ? _a : 0;
        message.currentCardChoices = ((_b = object.currentCardChoices) === null || _b === void 0 ? void 0 : _b.map(function (e) { return exports.Entity.fromPartial(e); })) || [];
        message.deckId = (_c = object.deckId) !== null && _c !== void 0 ? _c : "";
        message.draftIndex = (_d = object.draftIndex) !== null && _d !== void 0 ? _d : 0;
        message.heroClass = object.heroClass !== undefined && object.heroClass !== null ? exports.Entity.fromPartial(object.heroClass) : undefined;
        message.heroClassChoices = ((_e = object.heroClassChoices) === null || _e === void 0 ? void 0 : _e.map(function (e) { return exports.Entity.fromPartial(e); })) || [];
        message.losses = (_f = object.losses) !== null && _f !== void 0 ? _f : 0;
        message.selectedCardIds = ((_g = object.selectedCardIds) === null || _g === void 0 ? void 0 : _g.map(function (e) { return e; })) || [];
        message.status = (_h = object.status) !== null && _h !== void 0 ? _h : 0;
        message.wins = (_j = object.wins) !== null && _j !== void 0 ? _j : 0;
        return message;
    },
};
function createBaseDraftsChooseCardRequest() {
    return { cardIndex: 0 };
}
exports.DraftsChooseCardRequest = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.cardIndex !== 0) {
            writer.uint32(8).int32(message.cardIndex);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseDraftsChooseCardRequest();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.DraftsChooseCardRequest.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBaseDraftsChooseCardRequest();
        message.cardIndex = (_a = object.cardIndex) !== null && _a !== void 0 ? _a : 0;
        return message;
    },
};
function createBaseDraftsChooseHeroRequest() {
    return { heroIndex: 0 };
}
exports.DraftsChooseHeroRequest = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.heroIndex !== 0) {
            writer.uint32(8).int32(message.heroIndex);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseDraftsChooseHeroRequest();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.DraftsChooseHeroRequest.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBaseDraftsChooseHeroRequest();
        message.heroIndex = (_a = object.heroIndex) !== null && _a !== void 0 ? _a : 0;
        return message;
    },
};
function createBaseDraftsPostRequest() {
    return { retireEarly: false, startDraft: false };
}
exports.DraftsPostRequest = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.retireEarly === true) {
            writer.uint32(8).bool(message.retireEarly);
        }
        if (message.startDraft === true) {
            writer.uint32(16).bool(message.startDraft);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseDraftsPostRequest();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.DraftsPostRequest.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b;
        var message = createBaseDraftsPostRequest();
        message.retireEarly = (_a = object.retireEarly) !== null && _a !== void 0 ? _a : false;
        message.startDraft = (_b = object.startDraft) !== null && _b !== void 0 ? _b : false;
        return message;
    },
};
function createBaseEditableCard() {
    return { Id: "", ownerUserId: "", source: "", notification: undefined };
}
exports.EditableCard = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
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
            reactive_1.AddedChangedRemoved.encode(message.notification, writer.uint32(34).fork()).ldelim();
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseEditableCard();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
                    message.notification = reactive_1.AddedChangedRemoved.decode(reader, reader.uint32());
                    continue;
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.EditableCard.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b, _c;
        var message = createBaseEditableCard();
        message.Id = (_a = object.Id) !== null && _a !== void 0 ? _a : "";
        message.ownerUserId = (_b = object.ownerUserId) !== null && _b !== void 0 ? _b : "";
        message.source = (_c = object.source) !== null && _c !== void 0 ? _c : "";
        message.notification = object.notification !== undefined && object.notification !== null ? reactive_1.AddedChangedRemoved.fromPartial(object.notification) : undefined;
        return message;
    },
};
function createBaseEmote() {
    return { entityId: 0, message: 0 };
}
exports.Emote = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.entityId !== 0) {
            writer.uint32(8).int32(message.entityId);
        }
        if (message.message !== 0) {
            writer.uint32(16).int32(message.message);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseEmote();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
                    message.message = reader.int32();
                    continue;
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.Emote.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b;
        var message = createBaseEmote();
        message.entityId = (_a = object.entityId) !== null && _a !== void 0 ? _a : 0;
        message.message = (_b = object.message) !== null && _b !== void 0 ? _b : 0;
        return message;
    },
};
function createBaseEntity() {
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
exports.Entity = {
    encode: function (message, writer) {
        var e_17, _a, e_18, _b, e_19, _c, e_20, _d;
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.id !== 0) {
            writer.uint32(8).int32(message.id);
        }
        if (message.armor !== undefined) {
            writer.uint32(328).int32(message.armor);
        }
        if (message.art !== undefined) {
            exports.Art.encode(message.art, writer.uint32(18).fork()).ldelim();
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
        try {
            for (var _e = __values(message.cardSets), _f = _e.next(); !_f.done; _f = _e.next()) {
                var v = _f.value;
                writer.uint32(98).string(v);
            }
        }
        catch (e_17_1) { e_17 = { error: e_17_1 }; }
        finally {
            try {
                if (_f && !_f.done && (_a = _e.return)) _a.call(_e);
            }
            finally { if (e_17) throw e_17.error; }
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
        try {
            for (var _g = __values(message.heroClasses), _h = _g.next(); !_h.done; _h = _g.next()) {
                var v = _h.value;
                writer.uint32(586).string(v);
            }
        }
        catch (e_18_1) { e_18 = { error: e_18_1 }; }
        finally {
            try {
                if (_h && !_h.done && (_b = _g.return)) _b.call(_g);
            }
            finally { if (e_18) throw e_18.error; }
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
            exports.EntityLocation.encode(message.location, writer.uint32(354).fork()).ldelim();
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
        try {
            for (var _j = __values(message.tooltips), _k = _j.next(); !_k.done; _k = _j.next()) {
                var v = _k.value;
                exports.Tooltip.encode(v, writer.uint32(530).fork()).ldelim();
            }
        }
        catch (e_19_1) { e_19 = { error: e_19_1 }; }
        finally {
            try {
                if (_k && !_k.done && (_c = _j.return)) _c.call(_j);
            }
            finally { if (e_19) throw e_19.error; }
        }
        try {
            for (var _l = __values(message.tribes), _m = _l.next(); !_m.done; _m = _l.next()) {
                var v = _m.value;
                writer.uint32(594).string(v);
            }
        }
        catch (e_20_1) { e_20 = { error: e_20_1 }; }
        finally {
            try {
                if (_m && !_m.done && (_d = _l.return)) _d.call(_l);
            }
            finally { if (e_20) throw e_20.error; }
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
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseEntity();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
                    message.art = exports.Art.decode(reader, reader.uint32());
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
                    message.cardType = reader.int32();
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
                    message.entityType = reader.int32();
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
                    message.location = exports.EntityLocation.decode(reader, reader.uint32());
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
                    message.rarity = reader.int32();
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
                    message.tooltips.push(exports.Tooltip.decode(reader, reader.uint32()));
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
    create: function (base) {
        return exports.Entity.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b, _c, _d, _e, _f, _g, _h, _j, _k, _l, _m, _o, _p, _q, _r, _s, _t, _u, _v, _w, _x, _y, _z, _0, _1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12, _13, _14, _15, _16, _17, _18, _19, _20, _21, _22, _23, _24, _25, _26, _27, _28, _29, _30, _31, _32, _33, _34, _35, _36, _37, _38, _39, _40, _41, _42, _43;
        var message = createBaseEntity();
        message.id = (_a = object.id) !== null && _a !== void 0 ? _a : 0;
        message.armor = (_b = object.armor) !== null && _b !== void 0 ? _b : undefined;
        message.art = object.art !== undefined && object.art !== null ? exports.Art.fromPartial(object.art) : undefined;
        message.attack = (_c = object.attack) !== null && _c !== void 0 ? _c : undefined;
        message.baseAttack = (_d = object.baseAttack) !== null && _d !== void 0 ? _d : undefined;
        message.baseHp = (_e = object.baseHp) !== null && _e !== void 0 ? _e : undefined;
        message.baseManaCost = (_f = object.baseManaCost) !== null && _f !== void 0 ? _f : undefined;
        message.battlecry = (_g = object.battlecry) !== null && _g !== void 0 ? _g : false;
        message.boardPosition = (_h = object.boardPosition) !== null && _h !== void 0 ? _h : 0;
        message.cannotAttack = (_j = object.cannotAttack) !== null && _j !== void 0 ? _j : false;
        message.cardId = (_k = object.cardId) !== null && _k !== void 0 ? _k : "";
        message.cardSet = (_l = object.cardSet) !== null && _l !== void 0 ? _l : "";
        message.cardSets = ((_m = object.cardSets) === null || _m === void 0 ? void 0 : _m.map(function (e) { return e; })) || [];
        message.cardType = (_o = object.cardType) !== null && _o !== void 0 ? _o : 0;
        message.charge = (_p = object.charge) !== null && _p !== void 0 ? _p : false;
        message.charges = (_q = object.charges) !== null && _q !== void 0 ? _q : undefined;
        message.chooseOne = (_r = object.chooseOne) !== null && _r !== void 0 ? _r : false;
        message.collectible = (_s = object.collectible) !== null && _s !== void 0 ? _s : false;
        message.combo = (_t = object.combo) !== null && _t !== void 0 ? _t : false;
        message.conditionMet = (_u = object.conditionMet) !== null && _u !== void 0 ? _u : false;
        message.countUntilCast = (_v = object.countUntilCast) !== null && _v !== void 0 ? _v : undefined;
        message.deathrattles = (_w = object.deathrattles) !== null && _w !== void 0 ? _w : false;
        message.deflect = (_x = object.deflect) !== null && _x !== void 0 ? _x : false;
        message.description = (_y = object.description) !== null && _y !== void 0 ? _y : "";
        message.destroyed = (_z = object.destroyed) !== null && _z !== void 0 ? _z : false;
        message.discarded = (_0 = object.discarded) !== null && _0 !== void 0 ? _0 : false;
        message.divineShield = (_1 = object.divineShield) !== null && _1 !== void 0 ? _1 : false;
        message.durability = (_2 = object.durability) !== null && _2 !== void 0 ? _2 : undefined;
        message.enchantmentType = (_3 = object.enchantmentType) !== null && _3 !== void 0 ? _3 : "";
        message.enraged = (_4 = object.enraged) !== null && _4 !== void 0 ? _4 : false;
        message.entityType = (_5 = object.entityType) !== null && _5 !== void 0 ? _5 : 0;
        message.fires = (_6 = object.fires) !== null && _6 !== void 0 ? _6 : undefined;
        message.frozen = (_7 = object.frozen) !== null && _7 !== void 0 ? _7 : false;
        message.gameStarted = (_8 = object.gameStarted) !== null && _8 !== void 0 ? _8 : false;
        message.gold = (_9 = object.gold) !== null && _9 !== void 0 ? _9 : false;
        message.heroClasses = ((_10 = object.heroClasses) === null || _10 === void 0 ? void 0 : _10.map(function (e) { return e; })) || [];
        message.host = (_11 = object.host) !== null && _11 !== void 0 ? _11 : 0;
        message.hostsTrigger = (_12 = object.hostsTrigger) !== null && _12 !== void 0 ? _12 : false;
        message.hp = (_13 = object.hp) !== null && _13 !== void 0 ? _13 : undefined;
        message.immune = (_14 = object.immune) !== null && _14 !== void 0 ? _14 : false;
        message.isStartingTurn = (_15 = object.isStartingTurn) !== null && _15 !== void 0 ? _15 : false;
        message.location = object.location !== undefined && object.location !== null ? exports.EntityLocation.fromPartial(object.location) : undefined;
        message.lifesteal = (_16 = object.lifesteal) !== null && _16 !== void 0 ? _16 : false;
        message.lockedMana = (_17 = object.lockedMana) !== null && _17 !== void 0 ? _17 : 0;
        message.mana = (_18 = object.mana) !== null && _18 !== void 0 ? _18 : 0;
        message.manaCost = (_19 = object.manaCost) !== null && _19 !== void 0 ? _19 : undefined;
        message.maxHp = (_20 = object.maxHp) !== null && _20 !== void 0 ? _20 : undefined;
        message.maxMana = (_21 = object.maxMana) !== null && _21 !== void 0 ? _21 : 0;
        message.name = (_22 = object.name) !== null && _22 !== void 0 ? _22 : "";
        message.note = (_23 = object.note) !== null && _23 !== void 0 ? _23 : "";
        message.overload = (_24 = object.overload) !== null && _24 !== void 0 ? _24 : undefined;
        message.owner = (_25 = object.owner) !== null && _25 !== void 0 ? _25 : 0;
        message.permanent = (_26 = object.permanent) !== null && _26 !== void 0 ? _26 : false;
        message.playable = (_27 = object.playable) !== null && _27 !== void 0 ? _27 : false;
        message.poisonous = (_28 = object.poisonous) !== null && _28 !== void 0 ? _28 : false;
        message.rarity = (_29 = object.rarity) !== null && _29 !== void 0 ? _29 : 0;
        message.roasted = (_30 = object.roasted) !== null && _30 !== void 0 ? _30 : false;
        message.rush = (_31 = object.rush) !== null && _31 !== void 0 ? _31 : false;
        message.silenced = (_32 = object.silenced) !== null && _32 !== void 0 ? _32 : false;
        message.spellDamage = (_33 = object.spellDamage) !== null && _33 !== void 0 ? _33 : undefined;
        message.stealth = (_34 = object.stealth) !== null && _34 !== void 0 ? _34 : false;
        message.summoningSickness = (_35 = object.summoningSickness) !== null && _35 !== void 0 ? _35 : false;
        message.taunt = (_36 = object.taunt) !== null && _36 !== void 0 ? _36 : false;
        message.tooltips = ((_37 = object.tooltips) === null || _37 === void 0 ? void 0 : _37.map(function (e) { return exports.Tooltip.fromPartial(e); })) || [];
        message.tribes = ((_38 = object.tribes) === null || _38 === void 0 ? void 0 : _38.map(function (e) { return e; })) || [];
        message.uncensored = (_39 = object.uncensored) !== null && _39 !== void 0 ? _39 : false;
        message.underAura = (_40 = object.underAura) !== null && _40 !== void 0 ? _40 : false;
        message.untargetableBySpells = (_41 = object.untargetableBySpells) !== null && _41 !== void 0 ? _41 : false;
        message.windfury = (_42 = object.windfury) !== null && _42 !== void 0 ? _42 : false;
        message.extraAttack = (_43 = object.extraAttack) !== null && _43 !== void 0 ? _43 : undefined;
        return message;
    },
};
function createBaseEntityChangeSet() {
    return { ids: [] };
}
exports.EntityChangeSet = {
    encode: function (message, writer) {
        var e_21, _a;
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        writer.uint32(10).fork();
        try {
            for (var _b = __values(message.ids), _c = _b.next(); !_c.done; _c = _b.next()) {
                var v = _c.value;
                writer.int32(v);
            }
        }
        catch (e_21_1) { e_21 = { error: e_21_1 }; }
        finally {
            try {
                if (_c && !_c.done && (_a = _b.return)) _a.call(_b);
            }
            finally { if (e_21) throw e_21.error; }
        }
        writer.ldelim();
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseEntityChangeSet();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
                case 1:
                    if (tag === 8) {
                        message.ids.push(reader.int32());
                        continue;
                    }
                    if (tag === 10) {
                        var end2 = reader.uint32() + reader.pos;
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
    create: function (base) {
        return exports.EntityChangeSet.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBaseEntityChangeSet();
        message.ids = ((_a = object.ids) === null || _a === void 0 ? void 0 : _a.map(function (e) { return e; })) || [];
        return message;
    },
};
function createBaseEntityLocation() {
    return { index: 0, zone: 0, player: 0 };
}
exports.EntityLocation = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
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
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseEntityLocation();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
                    message.zone = reader.int32();
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
    create: function (base) {
        return exports.EntityLocation.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b, _c;
        var message = createBaseEntityLocation();
        message.index = (_a = object.index) !== null && _a !== void 0 ? _a : 0;
        message.zone = (_b = object.zone) !== null && _b !== void 0 ? _b : 0;
        message.player = (_c = object.player) !== null && _c !== void 0 ? _c : 0;
        return message;
    },
};
function createBaseEnvelope() {
    return {};
}
exports.Envelope = {
    encode: function (_, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseEnvelope();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.Envelope.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (_) {
        var message = createBaseEnvelope();
        return message;
    },
};
function createBaseEnvelope_GameMessage() {
    return { clientToServer: undefined, serverToClient: undefined };
}
exports.Envelope_GameMessage = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.clientToServer !== undefined) {
            exports.ClientToServerMessage.encode(message.clientToServer, writer.uint32(10).fork()).ldelim();
        }
        if (message.serverToClient !== undefined) {
            exports.ServerToClientMessage.encode(message.serverToClient, writer.uint32(18).fork()).ldelim();
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseEnvelope_GameMessage();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
                case 1:
                    if (tag !== 10) {
                        break;
                    }
                    message.clientToServer = exports.ClientToServerMessage.decode(reader, reader.uint32());
                    continue;
                case 2:
                    if (tag !== 18) {
                        break;
                    }
                    message.serverToClient = exports.ServerToClientMessage.decode(reader, reader.uint32());
                    continue;
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.Envelope_GameMessage.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var message = createBaseEnvelope_GameMessage();
        message.clientToServer = object.clientToServer !== undefined && object.clientToServer !== null ? exports.ClientToServerMessage.fromPartial(object.clientToServer) : undefined;
        message.serverToClient = object.serverToClient !== undefined && object.serverToClient !== null ? exports.ServerToClientMessage.fromPartial(object.serverToClient) : undefined;
        return message;
    },
};
function createBaseEnvelope_MethodMessage() {
    return {
        deleteCard: undefined,
        dequeue: undefined,
        enqueue: undefined,
        methodId: "",
        putCard: undefined,
        sendMessage: undefined,
    };
}
exports.Envelope_MethodMessage = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.deleteCard !== undefined) {
            exports.Envelope_MethodMessage_DeleteCardMessage.encode(message.deleteCard, writer.uint32(10).fork()).ldelim();
        }
        if (message.dequeue !== undefined) {
            exports.Envelope_MethodMessage_DequeueMessage.encode(message.dequeue, writer.uint32(18).fork()).ldelim();
        }
        if (message.enqueue !== undefined) {
            exports.MatchmakingQueuePutRequest.encode(message.enqueue, writer.uint32(26).fork()).ldelim();
        }
        if (message.methodId !== "") {
            writer.uint32(34).string(message.methodId);
        }
        if (message.putCard !== undefined) {
            exports.Envelope_MethodMessage_PutCardMessage.encode(message.putCard, writer.uint32(42).fork()).ldelim();
        }
        if (message.sendMessage !== undefined) {
            exports.Envelope_MethodMessage_SendMessageMessage.encode(message.sendMessage, writer.uint32(50).fork()).ldelim();
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseEnvelope_MethodMessage();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
                case 1:
                    if (tag !== 10) {
                        break;
                    }
                    message.deleteCard = exports.Envelope_MethodMessage_DeleteCardMessage.decode(reader, reader.uint32());
                    continue;
                case 2:
                    if (tag !== 18) {
                        break;
                    }
                    message.dequeue = exports.Envelope_MethodMessage_DequeueMessage.decode(reader, reader.uint32());
                    continue;
                case 3:
                    if (tag !== 26) {
                        break;
                    }
                    message.enqueue = exports.MatchmakingQueuePutRequest.decode(reader, reader.uint32());
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
                    message.putCard = exports.Envelope_MethodMessage_PutCardMessage.decode(reader, reader.uint32());
                    continue;
                case 6:
                    if (tag !== 50) {
                        break;
                    }
                    message.sendMessage = exports.Envelope_MethodMessage_SendMessageMessage.decode(reader, reader.uint32());
                    continue;
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.Envelope_MethodMessage.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBaseEnvelope_MethodMessage();
        message.deleteCard = object.deleteCard !== undefined && object.deleteCard !== null ? exports.Envelope_MethodMessage_DeleteCardMessage.fromPartial(object.deleteCard) : undefined;
        message.dequeue = object.dequeue !== undefined && object.dequeue !== null ? exports.Envelope_MethodMessage_DequeueMessage.fromPartial(object.dequeue) : undefined;
        message.enqueue = object.enqueue !== undefined && object.enqueue !== null ? exports.MatchmakingQueuePutRequest.fromPartial(object.enqueue) : undefined;
        message.methodId = (_a = object.methodId) !== null && _a !== void 0 ? _a : "";
        message.putCard = object.putCard !== undefined && object.putCard !== null ? exports.Envelope_MethodMessage_PutCardMessage.fromPartial(object.putCard) : undefined;
        message.sendMessage = object.sendMessage !== undefined && object.sendMessage !== null ? exports.Envelope_MethodMessage_SendMessageMessage.fromPartial(object.sendMessage) : undefined;
        return message;
    },
};
function createBaseEnvelope_MethodMessage_DeleteCardMessage() {
    return { editableCardId: "" };
}
exports.Envelope_MethodMessage_DeleteCardMessage = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.editableCardId !== "") {
            writer.uint32(10).string(message.editableCardId);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseEnvelope_MethodMessage_DeleteCardMessage();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.Envelope_MethodMessage_DeleteCardMessage.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBaseEnvelope_MethodMessage_DeleteCardMessage();
        message.editableCardId = (_a = object.editableCardId) !== null && _a !== void 0 ? _a : "";
        return message;
    },
};
function createBaseEnvelope_MethodMessage_DequeueMessage() {
    return { queueId: "" };
}
exports.Envelope_MethodMessage_DequeueMessage = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.queueId !== "") {
            writer.uint32(10).string(message.queueId);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseEnvelope_MethodMessage_DequeueMessage();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.Envelope_MethodMessage_DequeueMessage.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBaseEnvelope_MethodMessage_DequeueMessage();
        message.queueId = (_a = object.queueId) !== null && _a !== void 0 ? _a : "";
        return message;
    },
};
function createBaseEnvelope_MethodMessage_PutCardMessage() {
    return { draw: false, editableCardId: "", source: "" };
}
exports.Envelope_MethodMessage_PutCardMessage = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
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
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseEnvelope_MethodMessage_PutCardMessage();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.Envelope_MethodMessage_PutCardMessage.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b, _c;
        var message = createBaseEnvelope_MethodMessage_PutCardMessage();
        message.draw = (_a = object.draw) !== null && _a !== void 0 ? _a : false;
        message.editableCardId = (_b = object.editableCardId) !== null && _b !== void 0 ? _b : "";
        message.source = (_c = object.source) !== null && _c !== void 0 ? _c : "";
        return message;
    },
};
function createBaseEnvelope_MethodMessage_SendMessageMessage() {
    return { conversationId: "", message: "" };
}
exports.Envelope_MethodMessage_SendMessageMessage = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.conversationId !== "") {
            writer.uint32(10).string(message.conversationId);
        }
        if (message.message !== "") {
            writer.uint32(18).string(message.message);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseEnvelope_MethodMessage_SendMessageMessage();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.Envelope_MethodMessage_SendMessageMessage.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b;
        var message = createBaseEnvelope_MethodMessage_SendMessageMessage();
        message.conversationId = (_a = object.conversationId) !== null && _a !== void 0 ? _a : "";
        message.message = (_b = object.message) !== null && _b !== void 0 ? _b : "";
        return message;
    },
};
function createBaseEnvelope_RemovedMessage() {
    return { id: undefined };
}
exports.Envelope_RemovedMessage = {
    encode: function (message, writer) {
        var _a;
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        switch ((_a = message.id) === null || _a === void 0 ? void 0 : _a.$case) {
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
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseEnvelope_RemovedMessage();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.Envelope_RemovedMessage.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b, _c, _d, _e, _f, _g, _h, _j, _k, _l, _m;
        var message = createBaseEnvelope_RemovedMessage();
        if (((_a = object.id) === null || _a === void 0 ? void 0 : _a.$case) === "editableCardId" && ((_b = object.id) === null || _b === void 0 ? void 0 : _b.editableCardId) !== undefined && ((_c = object.id) === null || _c === void 0 ? void 0 : _c.editableCardId) !== null) {
            message.id = { $case: "editableCardId", editableCardId: object.id.editableCardId };
        }
        if (((_d = object.id) === null || _d === void 0 ? void 0 : _d.$case) === "friendId" && ((_e = object.id) === null || _e === void 0 ? void 0 : _e.friendId) !== undefined && ((_f = object.id) === null || _f === void 0 ? void 0 : _f.friendId) !== null) {
            message.id = { $case: "friendId", friendId: object.id.friendId };
        }
        if (((_g = object.id) === null || _g === void 0 ? void 0 : _g.$case) === "inviteId" && ((_h = object.id) === null || _h === void 0 ? void 0 : _h.inviteId) !== undefined && ((_j = object.id) === null || _j === void 0 ? void 0 : _j.inviteId) !== null) {
            message.id = { $case: "inviteId", inviteId: object.id.inviteId };
        }
        if (((_k = object.id) === null || _k === void 0 ? void 0 : _k.$case) === "matchId" && ((_l = object.id) === null || _l === void 0 ? void 0 : _l.matchId) !== undefined && ((_m = object.id) === null || _m === void 0 ? void 0 : _m.matchId) !== null) {
            message.id = { $case: "matchId", matchId: object.id.matchId };
        }
        return message;
    },
};
function createBaseEnvelope_ResultMessage() {
    return {};
}
exports.Envelope_ResultMessage = {
    encode: function (_, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseEnvelope_ResultMessage();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.Envelope_ResultMessage.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (_) {
        var message = createBaseEnvelope_ResultMessage();
        return message;
    },
};
function createBaseEnvelope_ResultMessage_PutCardMessage() {
    return { cardId: "", cardScriptErrors: [], editableCardId: "" };
}
exports.Envelope_ResultMessage_PutCardMessage = {
    encode: function (message, writer) {
        var e_22, _a;
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.cardId !== "") {
            writer.uint32(10).string(message.cardId);
        }
        try {
            for (var _b = __values(message.cardScriptErrors), _c = _b.next(); !_c.done; _c = _b.next()) {
                var v = _c.value;
                writer.uint32(18).string(v);
            }
        }
        catch (e_22_1) { e_22 = { error: e_22_1 }; }
        finally {
            try {
                if (_c && !_c.done && (_a = _b.return)) _a.call(_b);
            }
            finally { if (e_22) throw e_22.error; }
        }
        if (message.editableCardId !== "") {
            writer.uint32(26).string(message.editableCardId);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseEnvelope_ResultMessage_PutCardMessage();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.Envelope_ResultMessage_PutCardMessage.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b, _c;
        var message = createBaseEnvelope_ResultMessage_PutCardMessage();
        message.cardId = (_a = object.cardId) !== null && _a !== void 0 ? _a : "";
        message.cardScriptErrors = ((_b = object.cardScriptErrors) === null || _b === void 0 ? void 0 : _b.map(function (e) { return e; })) || [];
        message.editableCardId = (_c = object.editableCardId) !== null && _c !== void 0 ? _c : "";
        return message;
    },
};
function createBaseEnvelope_ResultMessage_SendMessageMessage() {
    return { messageId: "" };
}
exports.Envelope_ResultMessage_SendMessageMessage = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.messageId !== "") {
            writer.uint32(10).string(message.messageId);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseEnvelope_ResultMessage_SendMessageMessage();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.Envelope_ResultMessage_SendMessageMessage.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBaseEnvelope_ResultMessage_SendMessageMessage();
        message.messageId = (_a = object.messageId) !== null && _a !== void 0 ? _a : "";
        return message;
    },
};
function createBaseFont() {
    return { vertex: undefined };
}
exports.Font = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.vertex !== undefined) {
            exports.Color.encode(message.vertex, writer.uint32(10).fork()).ldelim();
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseFont();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
                case 1:
                    if (tag !== 10) {
                        break;
                    }
                    message.vertex = exports.Color.decode(reader, reader.uint32());
                    continue;
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.Font.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var message = createBaseFont();
        message.vertex = object.vertex !== undefined && object.vertex !== null ? exports.Color.fromPartial(object.vertex) : undefined;
        return message;
    },
};
function createBaseFriend() {
    return { friendId: "", friendName: "", presence: 0, since: 0, notification: undefined };
}
exports.Friend = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
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
            reactive_1.AddedChangedRemoved.encode(message.notification, writer.uint32(42).fork()).ldelim();
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseFriend();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
                    message.presence = reader.int32();
                    continue;
                case 4:
                    if (tag !== 32) {
                        break;
                    }
                    message.since = longToNumber(reader.int64());
                    continue;
                case 5:
                    if (tag !== 42) {
                        break;
                    }
                    message.notification = reactive_1.AddedChangedRemoved.decode(reader, reader.uint32());
                    continue;
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.Friend.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b, _c, _d;
        var message = createBaseFriend();
        message.friendId = (_a = object.friendId) !== null && _a !== void 0 ? _a : "";
        message.friendName = (_b = object.friendName) !== null && _b !== void 0 ? _b : "";
        message.presence = (_c = object.presence) !== null && _c !== void 0 ? _c : 0;
        message.since = (_d = object.since) !== null && _d !== void 0 ? _d : 0;
        message.notification = object.notification !== undefined && object.notification !== null ? reactive_1.AddedChangedRemoved.fromPartial(object.notification) : undefined;
        return message;
    },
};
function createBaseFriendDeleteRequest() {
    return { friendId: "" };
}
exports.FriendDeleteRequest = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.friendId !== "") {
            writer.uint32(10).string(message.friendId);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseFriendDeleteRequest();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.FriendDeleteRequest.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBaseFriendDeleteRequest();
        message.friendId = (_a = object.friendId) !== null && _a !== void 0 ? _a : "";
        return message;
    },
};
function createBaseFriendPutRequest() {
    return { friendId: "", usernameWithToken: "" };
}
exports.FriendPutRequest = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.friendId !== "") {
            writer.uint32(10).string(message.friendId);
        }
        if (message.usernameWithToken !== "") {
            writer.uint32(18).string(message.usernameWithToken);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseFriendPutRequest();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.FriendPutRequest.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b;
        var message = createBaseFriendPutRequest();
        message.friendId = (_a = object.friendId) !== null && _a !== void 0 ? _a : "";
        message.usernameWithToken = (_b = object.usernameWithToken) !== null && _b !== void 0 ? _b : "";
        return message;
    },
};
function createBaseFriendPutResponse() {
    return { friend: undefined };
}
exports.FriendPutResponse = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.friend !== undefined) {
            exports.Friend.encode(message.friend, writer.uint32(10).fork()).ldelim();
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseFriendPutResponse();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
                case 1:
                    if (tag !== 10) {
                        break;
                    }
                    message.friend = exports.Friend.decode(reader, reader.uint32());
                    continue;
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.FriendPutResponse.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var message = createBaseFriendPutResponse();
        message.friend = object.friend !== undefined && object.friend !== null ? exports.Friend.fromPartial(object.friend) : undefined;
        return message;
    },
};
function createBaseGameActions() {
    return { all: [], compatibility: [] };
}
exports.GameActions = {
    encode: function (message, writer) {
        var e_23, _a, e_24, _b;
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        try {
            for (var _c = __values(message.all), _d = _c.next(); !_d.done; _d = _c.next()) {
                var v = _d.value;
                exports.SpellAction.encode(v, writer.uint32(10).fork()).ldelim();
            }
        }
        catch (e_23_1) { e_23 = { error: e_23_1 }; }
        finally {
            try {
                if (_d && !_d.done && (_a = _c.return)) _a.call(_c);
            }
            finally { if (e_23) throw e_23.error; }
        }
        writer.uint32(18).fork();
        try {
            for (var _e = __values(message.compatibility), _f = _e.next(); !_f.done; _f = _e.next()) {
                var v = _f.value;
                writer.int32(v);
            }
        }
        catch (e_24_1) { e_24 = { error: e_24_1 }; }
        finally {
            try {
                if (_f && !_f.done && (_b = _e.return)) _b.call(_e);
            }
            finally { if (e_24) throw e_24.error; }
        }
        writer.ldelim();
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseGameActions();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
                case 1:
                    if (tag !== 10) {
                        break;
                    }
                    message.all.push(exports.SpellAction.decode(reader, reader.uint32()));
                    continue;
                case 2:
                    if (tag === 16) {
                        message.compatibility.push(reader.int32());
                        continue;
                    }
                    if (tag === 18) {
                        var end2 = reader.uint32() + reader.pos;
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
    create: function (base) {
        return exports.GameActions.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b;
        var message = createBaseGameActions();
        message.all = ((_a = object.all) === null || _a === void 0 ? void 0 : _a.map(function (e) { return exports.SpellAction.fromPartial(e); })) || [];
        message.compatibility = ((_b = object.compatibility) === null || _b === void 0 ? void 0 : _b.map(function (e) { return e; })) || [];
        return message;
    },
};
function createBaseGameEvent() {
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
exports.GameEvent = {
    encode: function (message, writer) {
        var e_25, _a;
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.cardEvent !== undefined) {
            exports.CardEvent.encode(message.cardEvent, writer.uint32(10).fork()).ldelim();
        }
        if (message.damage !== undefined) {
            exports.GameEvent_DamageMessage.encode(message.damage, writer.uint32(18).fork()).ldelim();
        }
        if (message.description !== "") {
            writer.uint32(26).string(message.description);
        }
        if (message.destroy !== undefined) {
            exports.GameEvent_DestroyMessage.encode(message.destroy, writer.uint32(34).fork()).ldelim();
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
            exports.GameEvent_JoustMessage.encode(message.joust, writer.uint32(98).fork()).ldelim();
        }
        if (message.performedGameAction !== undefined) {
            exports.GameEvent_PerformedGameActionMessage.encode(message.performedGameAction, writer.uint32(106).fork()).ldelim();
        }
        if (message.source !== undefined) {
            exports.Entity.encode(message.source, writer.uint32(114).fork()).ldelim();
        }
        if (message.target !== undefined) {
            exports.Entity.encode(message.target, writer.uint32(122).fork()).ldelim();
        }
        try {
            for (var _b = __values(message.targets), _c = _b.next(); !_c.done; _c = _b.next()) {
                var v = _c.value;
                exports.Entity.encode(v, writer.uint32(130).fork()).ldelim();
            }
        }
        catch (e_25_1) { e_25 = { error: e_25_1 }; }
        finally {
            try {
                if (_c && !_c.done && (_a = _b.return)) _a.call(_b);
            }
            finally { if (e_25) throw e_25.error; }
        }
        if (message.triggerFired !== undefined) {
            exports.GameEvent_TriggerFiredMessage.encode(message.triggerFired, writer.uint32(138).fork()).ldelim();
        }
        if (message.value !== undefined) {
            writer.uint32(144).int32(message.value);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseGameEvent();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
                case 1:
                    if (tag !== 10) {
                        break;
                    }
                    message.cardEvent = exports.CardEvent.decode(reader, reader.uint32());
                    continue;
                case 2:
                    if (tag !== 18) {
                        break;
                    }
                    message.damage = exports.GameEvent_DamageMessage.decode(reader, reader.uint32());
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
                    message.destroy = exports.GameEvent_DestroyMessage.decode(reader, reader.uint32());
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
                    message.eventType = reader.int32();
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
                    message.joust = exports.GameEvent_JoustMessage.decode(reader, reader.uint32());
                    continue;
                case 13:
                    if (tag !== 106) {
                        break;
                    }
                    message.performedGameAction = exports.GameEvent_PerformedGameActionMessage.decode(reader, reader.uint32());
                    continue;
                case 14:
                    if (tag !== 114) {
                        break;
                    }
                    message.source = exports.Entity.decode(reader, reader.uint32());
                    continue;
                case 15:
                    if (tag !== 122) {
                        break;
                    }
                    message.target = exports.Entity.decode(reader, reader.uint32());
                    continue;
                case 16:
                    if (tag !== 130) {
                        break;
                    }
                    message.targets.push(exports.Entity.decode(reader, reader.uint32()));
                    continue;
                case 17:
                    if (tag !== 138) {
                        break;
                    }
                    message.triggerFired = exports.GameEvent_TriggerFiredMessage.decode(reader, reader.uint32());
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
    create: function (base) {
        return exports.GameEvent.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b, _c, _d, _e, _f, _g, _h, _j, _k;
        var message = createBaseGameEvent();
        message.cardEvent = object.cardEvent !== undefined && object.cardEvent !== null ? exports.CardEvent.fromPartial(object.cardEvent) : undefined;
        message.damage = object.damage !== undefined && object.damage !== null ? exports.GameEvent_DamageMessage.fromPartial(object.damage) : undefined;
        message.description = (_a = object.description) !== null && _a !== void 0 ? _a : "";
        message.destroy = object.destroy !== undefined && object.destroy !== null ? exports.GameEvent_DestroyMessage.fromPartial(object.destroy) : undefined;
        message.entityTouched = (_b = object.entityTouched) !== null && _b !== void 0 ? _b : 0;
        message.entityUntouched = (_c = object.entityUntouched) !== null && _c !== void 0 ? _c : 0;
        message.eventType = (_d = object.eventType) !== null && _d !== void 0 ? _d : 0;
        message.id = (_e = object.id) !== null && _e !== void 0 ? _e : 0;
        message.isPowerHistory = (_f = object.isPowerHistory) !== null && _f !== void 0 ? _f : false;
        message.isSourcePlayerLocal = (_g = object.isSourcePlayerLocal) !== null && _g !== void 0 ? _g : false;
        message.isTargetPlayerLocal = (_h = object.isTargetPlayerLocal) !== null && _h !== void 0 ? _h : false;
        message.joust = object.joust !== undefined && object.joust !== null ? exports.GameEvent_JoustMessage.fromPartial(object.joust) : undefined;
        message.performedGameAction = object.performedGameAction !== undefined && object.performedGameAction !== null ? exports.GameEvent_PerformedGameActionMessage.fromPartial(object.performedGameAction) : undefined;
        message.source = object.source !== undefined && object.source !== null ? exports.Entity.fromPartial(object.source) : undefined;
        message.target = object.target !== undefined && object.target !== null ? exports.Entity.fromPartial(object.target) : undefined;
        message.targets = ((_j = object.targets) === null || _j === void 0 ? void 0 : _j.map(function (e) { return exports.Entity.fromPartial(e); })) || [];
        message.triggerFired = object.triggerFired !== undefined && object.triggerFired !== null ? exports.GameEvent_TriggerFiredMessage.fromPartial(object.triggerFired) : undefined;
        message.value = (_k = object.value) !== null && _k !== void 0 ? _k : undefined;
        return message;
    },
};
function createBaseGameEvent_DamageMessage() {
    return { damageType: 0 };
}
exports.GameEvent_DamageMessage = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.damageType !== 0) {
            writer.uint32(8).int32(message.damageType);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseGameEvent_DamageMessage();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
                case 1:
                    if (tag !== 8) {
                        break;
                    }
                    message.damageType = reader.int32();
                    continue;
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.GameEvent_DamageMessage.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBaseGameEvent_DamageMessage();
        message.damageType = (_a = object.damageType) !== null && _a !== void 0 ? _a : 0;
        return message;
    },
};
function createBaseGameEvent_DestroyMessage() {
    return { objects: [] };
}
exports.GameEvent_DestroyMessage = {
    encode: function (message, writer) {
        var e_26, _a;
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        try {
            for (var _b = __values(message.objects), _c = _b.next(); !_c.done; _c = _b.next()) {
                var v = _c.value;
                exports.Destroy.encode(v, writer.uint32(10).fork()).ldelim();
            }
        }
        catch (e_26_1) { e_26 = { error: e_26_1 }; }
        finally {
            try {
                if (_c && !_c.done && (_a = _b.return)) _a.call(_b);
            }
            finally { if (e_26) throw e_26.error; }
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseGameEvent_DestroyMessage();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
                case 1:
                    if (tag !== 10) {
                        break;
                    }
                    message.objects.push(exports.Destroy.decode(reader, reader.uint32()));
                    continue;
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.GameEvent_DestroyMessage.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBaseGameEvent_DestroyMessage();
        message.objects = ((_a = object.objects) === null || _a === void 0 ? void 0 : _a.map(function (e) { return exports.Destroy.fromPartial(e); })) || [];
        return message;
    },
};
function createBaseGameEvent_JoustMessage() {
    return { opponentCard: undefined, ownCard: undefined, won: false };
}
exports.GameEvent_JoustMessage = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.opponentCard !== undefined) {
            exports.Entity.encode(message.opponentCard, writer.uint32(10).fork()).ldelim();
        }
        if (message.ownCard !== undefined) {
            exports.Entity.encode(message.ownCard, writer.uint32(18).fork()).ldelim();
        }
        if (message.won === true) {
            writer.uint32(24).bool(message.won);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseGameEvent_JoustMessage();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
                case 1:
                    if (tag !== 10) {
                        break;
                    }
                    message.opponentCard = exports.Entity.decode(reader, reader.uint32());
                    continue;
                case 2:
                    if (tag !== 18) {
                        break;
                    }
                    message.ownCard = exports.Entity.decode(reader, reader.uint32());
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
    create: function (base) {
        return exports.GameEvent_JoustMessage.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBaseGameEvent_JoustMessage();
        message.opponentCard = object.opponentCard !== undefined && object.opponentCard !== null ? exports.Entity.fromPartial(object.opponentCard) : undefined;
        message.ownCard = object.ownCard !== undefined && object.ownCard !== null ? exports.Entity.fromPartial(object.ownCard) : undefined;
        message.won = (_a = object.won) !== null && _a !== void 0 ? _a : false;
        return message;
    },
};
function createBaseGameEvent_PerformedGameActionMessage() {
    return { actionType: 0 };
}
exports.GameEvent_PerformedGameActionMessage = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.actionType !== 0) {
            writer.uint32(8).int32(message.actionType);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseGameEvent_PerformedGameActionMessage();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
                case 1:
                    if (tag !== 8) {
                        break;
                    }
                    message.actionType = reader.int32();
                    continue;
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.GameEvent_PerformedGameActionMessage.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBaseGameEvent_PerformedGameActionMessage();
        message.actionType = (_a = object.actionType) !== null && _a !== void 0 ? _a : 0;
        return message;
    },
};
function createBaseGameEvent_TriggerFiredMessage() {
    return { triggerSourceId: 0, triggerTargetIds: [] };
}
exports.GameEvent_TriggerFiredMessage = {
    encode: function (message, writer) {
        var e_27, _a;
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.triggerSourceId !== 0) {
            writer.uint32(8).int32(message.triggerSourceId);
        }
        writer.uint32(18).fork();
        try {
            for (var _b = __values(message.triggerTargetIds), _c = _b.next(); !_c.done; _c = _b.next()) {
                var v = _c.value;
                writer.int32(v);
            }
        }
        catch (e_27_1) { e_27 = { error: e_27_1 }; }
        finally {
            try {
                if (_c && !_c.done && (_a = _b.return)) _a.call(_b);
            }
            finally { if (e_27) throw e_27.error; }
        }
        writer.ldelim();
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseGameEvent_TriggerFiredMessage();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
                        var end2 = reader.uint32() + reader.pos;
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
    create: function (base) {
        return exports.GameEvent_TriggerFiredMessage.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b;
        var message = createBaseGameEvent_TriggerFiredMessage();
        message.triggerSourceId = (_a = object.triggerSourceId) !== null && _a !== void 0 ? _a : 0;
        message.triggerTargetIds = ((_b = object.triggerTargetIds) === null || _b === void 0 ? void 0 : _b.map(function (e) { return e; })) || [];
        return message;
    },
};
function createBaseGameOver() {
    return { localPlayerWon: false, winningPlayerId: undefined };
}
exports.GameOver = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.localPlayerWon === true) {
            writer.uint32(8).bool(message.localPlayerWon);
        }
        if (message.winningPlayerId !== undefined) {
            writer.uint32(16).int32(message.winningPlayerId);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseGameOver();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.GameOver.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b;
        var message = createBaseGameOver();
        message.localPlayerWon = (_a = object.localPlayerWon) !== null && _a !== void 0 ? _a : false;
        message.winningPlayerId = (_b = object.winningPlayerId) !== null && _b !== void 0 ? _b : undefined;
        return message;
    },
};
function createBaseGameState() {
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
exports.GameState = {
    encode: function (message, writer) {
        var e_28, _a, e_29, _b;
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        try {
            for (var _c = __values(message.entities), _d = _c.next(); !_d.done; _d = _c.next()) {
                var v = _d.value;
                exports.Entity.encode(v, writer.uint32(10).fork()).ldelim();
            }
        }
        catch (e_28_1) { e_28 = { error: e_28_1 }; }
        finally {
            try {
                if (_d && !_d.done && (_a = _c.return)) _a.call(_c);
            }
            finally { if (e_28) throw e_28.error; }
        }
        if (message.isLocalPlayerTurn === true) {
            writer.uint32(16).bool(message.isLocalPlayerTurn);
        }
        try {
            for (var _e = __values(message.powerHistory), _f = _e.next(); !_f.done; _f = _e.next()) {
                var v = _f.value;
                exports.GameEvent.encode(v, writer.uint32(26).fork()).ldelim();
            }
        }
        catch (e_29_1) { e_29 = { error: e_29_1 }; }
        finally {
            try {
                if (_f && !_f.done && (_b = _e.return)) _b.call(_e);
            }
            finally { if (e_29) throw e_29.error; }
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
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseGameState();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
                case 1:
                    if (tag !== 10) {
                        break;
                    }
                    message.entities.push(exports.Entity.decode(reader, reader.uint32()));
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
                    message.powerHistory.push(exports.GameEvent.decode(reader, reader.uint32()));
                    continue;
                case 4:
                    if (tag !== 32) {
                        break;
                    }
                    message.timestamp = longToNumber(reader.int64());
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
    create: function (base) {
        return exports.GameState.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b, _c, _d, _e, _f, _g;
        var message = createBaseGameState();
        message.entities = ((_a = object.entities) === null || _a === void 0 ? void 0 : _a.map(function (e) { return exports.Entity.fromPartial(e); })) || [];
        message.isLocalPlayerTurn = (_b = object.isLocalPlayerTurn) !== null && _b !== void 0 ? _b : false;
        message.powerHistory = ((_c = object.powerHistory) === null || _c === void 0 ? void 0 : _c.map(function (e) { return exports.GameEvent.fromPartial(e); })) || [];
        message.timestamp = (_d = object.timestamp) !== null && _d !== void 0 ? _d : 0;
        message.turnNumber = (_e = object.turnNumber) !== null && _e !== void 0 ? _e : 0;
        message.turnState = (_f = object.turnState) !== null && _f !== void 0 ? _f : "";
        message.hasPowerHistory = (_g = object.hasPowerHistory) !== null && _g !== void 0 ? _g : false;
        return message;
    },
};
function createBaseGetAccountRequest() {
    return { targetUserId: "" };
}
exports.GetAccountRequest = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.targetUserId !== "") {
            writer.uint32(10).string(message.targetUserId);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseGetAccountRequest();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.GetAccountRequest.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBaseGetAccountRequest();
        message.targetUserId = (_a = object.targetUserId) !== null && _a !== void 0 ? _a : "";
        return message;
    },
};
function createBaseGetAccountsRequest() {
    return { userIds: [] };
}
exports.GetAccountsRequest = {
    encode: function (message, writer) {
        var e_30, _a;
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        try {
            for (var _b = __values(message.userIds), _c = _b.next(); !_c.done; _c = _b.next()) {
                var v = _c.value;
                writer.uint32(10).string(v);
            }
        }
        catch (e_30_1) { e_30 = { error: e_30_1 }; }
        finally {
            try {
                if (_c && !_c.done && (_a = _b.return)) _a.call(_b);
            }
            finally { if (e_30) throw e_30.error; }
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseGetAccountsRequest();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.GetAccountsRequest.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBaseGetAccountsRequest();
        message.userIds = ((_a = object.userIds) === null || _a === void 0 ? void 0 : _a.map(function (e) { return e; })) || [];
        return message;
    },
};
function createBaseGetAccountsResponse() {
    return { accounts: [] };
}
exports.GetAccountsResponse = {
    encode: function (message, writer) {
        var e_31, _a;
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        try {
            for (var _b = __values(message.accounts), _c = _b.next(); !_c.done; _c = _b.next()) {
                var v = _c.value;
                exports.Account.encode(v, writer.uint32(10).fork()).ldelim();
            }
        }
        catch (e_31_1) { e_31 = { error: e_31_1 }; }
        finally {
            try {
                if (_c && !_c.done && (_a = _b.return)) _a.call(_b);
            }
            finally { if (e_31) throw e_31.error; }
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseGetAccountsResponse();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
                case 1:
                    if (tag !== 10) {
                        break;
                    }
                    message.accounts.push(exports.Account.decode(reader, reader.uint32()));
                    continue;
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.GetAccountsResponse.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBaseGetAccountsResponse();
        message.accounts = ((_a = object.accounts) === null || _a === void 0 ? void 0 : _a.map(function (e) { return exports.Account.fromPartial(e); })) || [];
        return message;
    },
};
function createBaseGetGameRecordIdsResponse() {
    return { gameIds: [] };
}
exports.GetGameRecordIdsResponse = {
    encode: function (message, writer) {
        var e_32, _a;
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        try {
            for (var _b = __values(message.gameIds), _c = _b.next(); !_c.done; _c = _b.next()) {
                var v = _c.value;
                writer.uint32(10).string(v);
            }
        }
        catch (e_32_1) { e_32 = { error: e_32_1 }; }
        finally {
            try {
                if (_c && !_c.done && (_a = _b.return)) _a.call(_b);
            }
            finally { if (e_32) throw e_32.error; }
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseGetGameRecordIdsResponse();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.GetGameRecordIdsResponse.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBaseGetGameRecordIdsResponse();
        message.gameIds = ((_a = object.gameIds) === null || _a === void 0 ? void 0 : _a.map(function (e) { return e; })) || [];
        return message;
    },
};
function createBaseGetGameRecordRequest() {
    return { gameId: "" };
}
exports.GetGameRecordRequest = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.gameId !== "") {
            writer.uint32(10).string(message.gameId);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseGetGameRecordRequest();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.GetGameRecordRequest.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBaseGetGameRecordRequest();
        message.gameId = (_a = object.gameId) !== null && _a !== void 0 ? _a : "";
        return message;
    },
};
function createBaseGetGameRecordResponse() {
    return { completedAt: 0, completedAtLocalized: "", isBotGame: false, playerNames: [], replay: undefined };
}
exports.GetGameRecordResponse = {
    encode: function (message, writer) {
        var e_33, _a;
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.completedAt !== 0) {
            writer.uint32(8).int64(message.completedAt);
        }
        if (message.completedAtLocalized !== "") {
            writer.uint32(18).string(message.completedAtLocalized);
        }
        if (message.isBotGame === true) {
            writer.uint32(24).bool(message.isBotGame);
        }
        try {
            for (var _b = __values(message.playerNames), _c = _b.next(); !_c.done; _c = _b.next()) {
                var v = _c.value;
                writer.uint32(34).string(v);
            }
        }
        catch (e_33_1) { e_33 = { error: e_33_1 }; }
        finally {
            try {
                if (_c && !_c.done && (_a = _b.return)) _a.call(_b);
            }
            finally { if (e_33) throw e_33.error; }
        }
        if (message.replay !== undefined) {
            exports.Replay.encode(message.replay, writer.uint32(42).fork()).ldelim();
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseGetGameRecordResponse();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
                case 1:
                    if (tag !== 8) {
                        break;
                    }
                    message.completedAt = longToNumber(reader.int64());
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
                    message.replay = exports.Replay.decode(reader, reader.uint32());
                    continue;
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.GetGameRecordResponse.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b, _c, _d;
        var message = createBaseGetGameRecordResponse();
        message.completedAt = (_a = object.completedAt) !== null && _a !== void 0 ? _a : 0;
        message.completedAtLocalized = (_b = object.completedAtLocalized) !== null && _b !== void 0 ? _b : "";
        message.isBotGame = (_c = object.isBotGame) !== null && _c !== void 0 ? _c : false;
        message.playerNames = ((_d = object.playerNames) === null || _d === void 0 ? void 0 : _d.map(function (e) { return e; })) || [];
        message.replay = object.replay !== undefined && object.replay !== null ? exports.Replay.fromPartial(object.replay) : undefined;
        return message;
    },
};
function createBaseGetInviteRequest() {
    return { inviteId: "" };
}
exports.GetInviteRequest = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.inviteId !== "") {
            writer.uint32(10).string(message.inviteId);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseGetInviteRequest();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.GetInviteRequest.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBaseGetInviteRequest();
        message.inviteId = (_a = object.inviteId) !== null && _a !== void 0 ? _a : "";
        return message;
    },
};
function createBaseInventoryCollection() {
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
exports.InventoryCollection = {
    encode: function (message, writer) {
        var e_34, _a, e_35, _b;
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
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
        try {
            for (var _c = __values(message.inventory), _d = _c.next(); !_d.done; _d = _c.next()) {
                var v = _d.value;
                exports.CardRecord.encode(v, writer.uint32(42).fork()).ldelim();
            }
        }
        catch (e_34_1) { e_34 = { error: e_34_1 }; }
        finally {
            try {
                if (_d && !_d.done && (_a = _c.return)) _a.call(_c);
            }
            finally { if (e_34) throw e_34.error; }
        }
        if (message.isStandardDeck === true) {
            writer.uint32(48).bool(message.isStandardDeck);
        }
        if (message.name !== "") {
            writer.uint32(58).string(message.name);
        }
        try {
            for (var _e = __values(message.playerEntityAttributes), _f = _e.next(); !_f.done; _f = _e.next()) {
                var v = _f.value;
                exports.AttributeValueTuple.encode(v, writer.uint32(66).fork()).ldelim();
            }
        }
        catch (e_35_1) { e_35 = { error: e_35_1 }; }
        finally {
            try {
                if (_f && !_f.done && (_b = _e.return)) _b.call(_e);
            }
            finally { if (e_35) throw e_35.error; }
        }
        if (message.type !== 0) {
            writer.uint32(72).int32(message.type);
        }
        if (message.userId !== "") {
            writer.uint32(82).string(message.userId);
        }
        if (message.validationReport !== undefined) {
            exports.ValidationReport.encode(message.validationReport, writer.uint32(90).fork()).ldelim();
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseInventoryCollection();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
                    message.deckType = reader.int32();
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
                    message.inventory.push(exports.CardRecord.decode(reader, reader.uint32()));
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
                    message.playerEntityAttributes.push(exports.AttributeValueTuple.decode(reader, reader.uint32()));
                    continue;
                case 9:
                    if (tag !== 72) {
                        break;
                    }
                    message.type = reader.int32();
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
                    message.validationReport = exports.ValidationReport.decode(reader, reader.uint32());
                    continue;
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.InventoryCollection.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b, _c, _d, _e, _f, _g, _h, _j, _k;
        var message = createBaseInventoryCollection();
        message.Id = (_a = object.Id) !== null && _a !== void 0 ? _a : "";
        message.deckType = (_b = object.deckType) !== null && _b !== void 0 ? _b : 0;
        message.format = (_c = object.format) !== null && _c !== void 0 ? _c : "";
        message.heroClass = (_d = object.heroClass) !== null && _d !== void 0 ? _d : "";
        message.inventory = ((_e = object.inventory) === null || _e === void 0 ? void 0 : _e.map(function (e) { return exports.CardRecord.fromPartial(e); })) || [];
        message.isStandardDeck = (_f = object.isStandardDeck) !== null && _f !== void 0 ? _f : false;
        message.name = (_g = object.name) !== null && _g !== void 0 ? _g : "";
        message.playerEntityAttributes = ((_h = object.playerEntityAttributes) === null || _h === void 0 ? void 0 : _h.map(function (e) { return exports.AttributeValueTuple.fromPartial(e); })) || [];
        message.type = (_j = object.type) !== null && _j !== void 0 ? _j : 0;
        message.userId = (_k = object.userId) !== null && _k !== void 0 ? _k : "";
        message.validationReport = object.validationReport !== undefined && object.validationReport !== null ? exports.ValidationReport.fromPartial(object.validationReport) : undefined;
        return message;
    },
};
function createBaseInvite() {
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
exports.Invite = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
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
            reactive_1.AddedChangedRemoved.encode(message.notification, writer.uint32(90).fork()).ldelim();
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseInvite();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
                    message.expiresAt = longToNumber(reader.int64());
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
                    message.status = reader.int32();
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
                    message.notification = reactive_1.AddedChangedRemoved.decode(reader, reader.uint32());
                    continue;
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.Invite.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b, _c, _d, _e, _f, _g, _h, _j, _k;
        var message = createBaseInvite();
        message.Id = (_a = object.Id) !== null && _a !== void 0 ? _a : "";
        message.expiresAt = (_b = object.expiresAt) !== null && _b !== void 0 ? _b : 0;
        message.friendId = (_c = object.friendId) !== null && _c !== void 0 ? _c : "";
        message.fromName = (_d = object.fromName) !== null && _d !== void 0 ? _d : "";
        message.fromUserId = (_e = object.fromUserId) !== null && _e !== void 0 ? _e : "";
        message.message = (_f = object.message) !== null && _f !== void 0 ? _f : "";
        message.queueId = (_g = object.queueId) !== null && _g !== void 0 ? _g : "";
        message.status = (_h = object.status) !== null && _h !== void 0 ? _h : 0;
        message.toName = (_j = object.toName) !== null && _j !== void 0 ? _j : "";
        message.toUserId = (_k = object.toUserId) !== null && _k !== void 0 ? _k : "";
        message.notification = object.notification !== undefined && object.notification !== null ? reactive_1.AddedChangedRemoved.fromPartial(object.notification) : undefined;
        return message;
    },
};
function createBaseInviteGetResponse() {
    return { invites: [] };
}
exports.InviteGetResponse = {
    encode: function (message, writer) {
        var e_36, _a;
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        try {
            for (var _b = __values(message.invites), _c = _b.next(); !_c.done; _c = _b.next()) {
                var v = _c.value;
                exports.Invite.encode(v, writer.uint32(10).fork()).ldelim();
            }
        }
        catch (e_36_1) { e_36 = { error: e_36_1 }; }
        finally {
            try {
                if (_c && !_c.done && (_a = _b.return)) _a.call(_b);
            }
            finally { if (e_36) throw e_36.error; }
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseInviteGetResponse();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
                case 1:
                    if (tag !== 10) {
                        break;
                    }
                    message.invites.push(exports.Invite.decode(reader, reader.uint32()));
                    continue;
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.InviteGetResponse.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBaseInviteGetResponse();
        message.invites = ((_a = object.invites) === null || _a === void 0 ? void 0 : _a.map(function (e) { return exports.Invite.fromPartial(e); })) || [];
        return message;
    },
};
function createBaseInvitePostRequest() {
    return { deckId: "", friend: false, message: "", queueId: "", toUserId: "", toUserNameWithToken: "" };
}
exports.InvitePostRequest = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
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
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseInvitePostRequest();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.InvitePostRequest.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b, _c, _d, _e, _f;
        var message = createBaseInvitePostRequest();
        message.deckId = (_a = object.deckId) !== null && _a !== void 0 ? _a : "";
        message.friend = (_b = object.friend) !== null && _b !== void 0 ? _b : false;
        message.message = (_c = object.message) !== null && _c !== void 0 ? _c : "";
        message.queueId = (_d = object.queueId) !== null && _d !== void 0 ? _d : "";
        message.toUserId = (_e = object.toUserId) !== null && _e !== void 0 ? _e : "";
        message.toUserNameWithToken = (_f = object.toUserNameWithToken) !== null && _f !== void 0 ? _f : "";
        return message;
    },
};
function createBaseInviteResponse() {
    return { results: undefined };
}
exports.InviteResponse = {
    encode: function (message, writer) {
        var _a;
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        switch ((_a = message.results) === null || _a === void 0 ? void 0 : _a.$case) {
            case "invite":
                exports.Invite.encode(message.results.invite, writer.uint32(10).fork()).ldelim();
                break;
            case "match":
                exports.MatchmakingQueuePutResponse.encode(message.results.match, writer.uint32(18).fork()).ldelim();
                break;
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseInviteResponse();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
                case 1:
                    if (tag !== 10) {
                        break;
                    }
                    message.results = { $case: "invite", invite: exports.Invite.decode(reader, reader.uint32()) };
                    continue;
                case 2:
                    if (tag !== 18) {
                        break;
                    }
                    message.results = { $case: "match", match: exports.MatchmakingQueuePutResponse.decode(reader, reader.uint32()) };
                    continue;
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.InviteResponse.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b, _c, _d, _e, _f;
        var message = createBaseInviteResponse();
        if (((_a = object.results) === null || _a === void 0 ? void 0 : _a.$case) === "invite" && ((_b = object.results) === null || _b === void 0 ? void 0 : _b.invite) !== undefined && ((_c = object.results) === null || _c === void 0 ? void 0 : _c.invite) !== null) {
            message.results = { $case: "invite", invite: exports.Invite.fromPartial(object.results.invite) };
        }
        if (((_d = object.results) === null || _d === void 0 ? void 0 : _d.$case) === "match" && ((_e = object.results) === null || _e === void 0 ? void 0 : _e.match) !== undefined && ((_f = object.results) === null || _f === void 0 ? void 0 : _f.match) !== null) {
            message.results = { $case: "match", match: exports.MatchmakingQueuePutResponse.fromPartial(object.results.match) };
        }
        return message;
    },
};
function createBaseLoginRequest() {
    return { email: "", password: "" };
}
exports.LoginRequest = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.email !== "") {
            writer.uint32(10).string(message.email);
        }
        if (message.password !== "") {
            writer.uint32(18).string(message.password);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseLoginRequest();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.LoginRequest.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b;
        var message = createBaseLoginRequest();
        message.email = (_a = object.email) !== null && _a !== void 0 ? _a : "";
        message.password = (_b = object.password) !== null && _b !== void 0 ? _b : "";
        return message;
    },
};
function createBaseLoginResponse() {
    return { account: undefined, loginToken: "" };
}
exports.LoginResponse = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.account !== undefined) {
            exports.Account.encode(message.account, writer.uint32(10).fork()).ldelim();
        }
        if (message.loginToken !== "") {
            writer.uint32(18).string(message.loginToken);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseLoginResponse();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
                case 1:
                    if (tag !== 10) {
                        break;
                    }
                    message.account = exports.Account.decode(reader, reader.uint32());
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
    create: function (base) {
        return exports.LoginResponse.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBaseLoginResponse();
        message.account = object.account !== undefined && object.account !== null ? exports.Account.fromPartial(object.account) : undefined;
        message.loginToken = (_a = object.loginToken) !== null && _a !== void 0 ? _a : "";
        return message;
    },
};
function createBaseMatch() {
    return { Id: "", createdAt: 0 };
}
exports.Match = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.Id !== "") {
            writer.uint32(10).string(message.Id);
        }
        if (message.createdAt !== 0) {
            writer.uint32(16).int64(message.createdAt);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseMatch();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
                    message.createdAt = longToNumber(reader.int64());
                    continue;
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.Match.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b;
        var message = createBaseMatch();
        message.Id = (_a = object.Id) !== null && _a !== void 0 ? _a : "";
        message.createdAt = (_b = object.createdAt) !== null && _b !== void 0 ? _b : 0;
        return message;
    },
};
function createBaseMatchCancelResponse() {
    return { isCanceled: false };
}
exports.MatchCancelResponse = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.isCanceled === true) {
            writer.uint32(8).bool(message.isCanceled);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseMatchCancelResponse();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.MatchCancelResponse.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBaseMatchCancelResponse();
        message.isCanceled = (_a = object.isCanceled) !== null && _a !== void 0 ? _a : false;
        return message;
    },
};
function createBaseMatchConcedeResponse() {
    return { isConceded: false };
}
exports.MatchConcedeResponse = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.isConceded === true) {
            writer.uint32(8).bool(message.isConceded);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseMatchConcedeResponse();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.MatchConcedeResponse.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBaseMatchConcedeResponse();
        message.isConceded = (_a = object.isConceded) !== null && _a !== void 0 ? _a : false;
        return message;
    },
};
function createBaseMatchmakingQueueItem() {
    return { description: "", name: "", queueId: "", requires: undefined, tooltip: "" };
}
exports.MatchmakingQueueItem = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
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
            exports.MatchmakingQueueItem_RequiresMessage.encode(message.requires, writer.uint32(34).fork()).ldelim();
        }
        if (message.tooltip !== "") {
            writer.uint32(42).string(message.tooltip);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseMatchmakingQueueItem();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
                    message.requires = exports.MatchmakingQueueItem_RequiresMessage.decode(reader, reader.uint32());
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
    create: function (base) {
        return exports.MatchmakingQueueItem.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b, _c, _d;
        var message = createBaseMatchmakingQueueItem();
        message.description = (_a = object.description) !== null && _a !== void 0 ? _a : "";
        message.name = (_b = object.name) !== null && _b !== void 0 ? _b : "";
        message.queueId = (_c = object.queueId) !== null && _c !== void 0 ? _c : "";
        message.requires = object.requires !== undefined && object.requires !== null ? exports.MatchmakingQueueItem_RequiresMessage.fromPartial(object.requires) : undefined;
        message.tooltip = (_d = object.tooltip) !== null && _d !== void 0 ? _d : "";
        return message;
    },
};
function createBaseMatchmakingQueueItem_RequiresMessage() {
    return { deck: false, deckChoices: [], deckIdChoices: [], heroClass: false };
}
exports.MatchmakingQueueItem_RequiresMessage = {
    encode: function (message, writer) {
        var e_37, _a, e_38, _b;
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.deck === true) {
            writer.uint32(8).bool(message.deck);
        }
        try {
            for (var _c = __values(message.deckChoices), _d = _c.next(); !_d.done; _d = _c.next()) {
                var v = _d.value;
                exports.InventoryCollection.encode(v, writer.uint32(18).fork()).ldelim();
            }
        }
        catch (e_37_1) { e_37 = { error: e_37_1 }; }
        finally {
            try {
                if (_d && !_d.done && (_a = _c.return)) _a.call(_c);
            }
            finally { if (e_37) throw e_37.error; }
        }
        try {
            for (var _e = __values(message.deckIdChoices), _f = _e.next(); !_f.done; _f = _e.next()) {
                var v = _f.value;
                writer.uint32(26).string(v);
            }
        }
        catch (e_38_1) { e_38 = { error: e_38_1 }; }
        finally {
            try {
                if (_f && !_f.done && (_b = _e.return)) _b.call(_e);
            }
            finally { if (e_38) throw e_38.error; }
        }
        if (message.heroClass === true) {
            writer.uint32(32).bool(message.heroClass);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseMatchmakingQueueItem_RequiresMessage();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
                    message.deckChoices.push(exports.InventoryCollection.decode(reader, reader.uint32()));
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
    create: function (base) {
        return exports.MatchmakingQueueItem_RequiresMessage.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b, _c, _d;
        var message = createBaseMatchmakingQueueItem_RequiresMessage();
        message.deck = (_a = object.deck) !== null && _a !== void 0 ? _a : false;
        message.deckChoices = ((_b = object.deckChoices) === null || _b === void 0 ? void 0 : _b.map(function (e) { return exports.InventoryCollection.fromPartial(e); })) || [];
        message.deckIdChoices = ((_c = object.deckIdChoices) === null || _c === void 0 ? void 0 : _c.map(function (e) { return e; })) || [];
        message.heroClass = (_d = object.heroClass) !== null && _d !== void 0 ? _d : false;
        return message;
    },
};
function createBaseMatchmakingQueuePutRequest() {
    return { botDeckId: "", deckId: "", queueId: "", cancel: false };
}
exports.MatchmakingQueuePutRequest = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
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
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseMatchmakingQueuePutRequest();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.MatchmakingQueuePutRequest.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b, _c, _d;
        var message = createBaseMatchmakingQueuePutRequest();
        message.botDeckId = (_a = object.botDeckId) !== null && _a !== void 0 ? _a : "";
        message.deckId = (_b = object.deckId) !== null && _b !== void 0 ? _b : "";
        message.queueId = (_c = object.queueId) !== null && _c !== void 0 ? _c : "";
        message.cancel = (_d = object.cancel) !== null && _d !== void 0 ? _d : false;
        return message;
    },
};
function createBaseMatchmakingQueuePutResponse() {
    return { retry: undefined, unityConnection: undefined };
}
exports.MatchmakingQueuePutResponse = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.retry !== undefined) {
            exports.MatchmakingQueuePutRequest.encode(message.retry, writer.uint32(10).fork()).ldelim();
        }
        if (message.unityConnection !== undefined) {
            exports.MatchmakingQueuePutResponseUnityConnection.encode(message.unityConnection, writer.uint32(18).fork()).ldelim();
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseMatchmakingQueuePutResponse();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
                case 1:
                    if (tag !== 10) {
                        break;
                    }
                    message.retry = exports.MatchmakingQueuePutRequest.decode(reader, reader.uint32());
                    continue;
                case 2:
                    if (tag !== 18) {
                        break;
                    }
                    message.unityConnection = exports.MatchmakingQueuePutResponseUnityConnection.decode(reader, reader.uint32());
                    continue;
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.MatchmakingQueuePutResponse.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var message = createBaseMatchmakingQueuePutResponse();
        message.retry = object.retry !== undefined && object.retry !== null ? exports.MatchmakingQueuePutRequest.fromPartial(object.retry) : undefined;
        message.unityConnection = object.unityConnection !== undefined && object.unityConnection !== null ? exports.MatchmakingQueuePutResponseUnityConnection.fromPartial(object.unityConnection) : undefined;
        return message;
    },
};
function createBaseMatchmakingQueuePutResponseUnityConnection() {
    return { firstMessage: undefined, url: "", gameId: "" };
}
exports.MatchmakingQueuePutResponseUnityConnection = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.firstMessage !== undefined) {
            exports.ClientToServerMessage.encode(message.firstMessage, writer.uint32(10).fork()).ldelim();
        }
        if (message.url !== "") {
            writer.uint32(18).string(message.url);
        }
        if (message.gameId !== "") {
            writer.uint32(26).string(message.gameId);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseMatchmakingQueuePutResponseUnityConnection();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
                case 1:
                    if (tag !== 10) {
                        break;
                    }
                    message.firstMessage = exports.ClientToServerMessage.decode(reader, reader.uint32());
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
    create: function (base) {
        return exports.MatchmakingQueuePutResponseUnityConnection.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b;
        var message = createBaseMatchmakingQueuePutResponseUnityConnection();
        message.firstMessage = object.firstMessage !== undefined && object.firstMessage !== null ? exports.ClientToServerMessage.fromPartial(object.firstMessage) : undefined;
        message.url = (_a = object.url) !== null && _a !== void 0 ? _a : "";
        message.gameId = (_b = object.gameId) !== null && _b !== void 0 ? _b : "";
        return message;
    },
};
function createBaseMatchmakingQueuesResponse() {
    return { queues: [] };
}
exports.MatchmakingQueuesResponse = {
    encode: function (message, writer) {
        var e_39, _a;
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        try {
            for (var _b = __values(message.queues), _c = _b.next(); !_c.done; _c = _b.next()) {
                var v = _c.value;
                exports.MatchmakingQueueItem.encode(v, writer.uint32(10).fork()).ldelim();
            }
        }
        catch (e_39_1) { e_39 = { error: e_39_1 }; }
        finally {
            try {
                if (_c && !_c.done && (_a = _b.return)) _a.call(_b);
            }
            finally { if (e_39) throw e_39.error; }
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseMatchmakingQueuesResponse();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
                case 1:
                    if (tag !== 10) {
                        break;
                    }
                    message.queues.push(exports.MatchmakingQueueItem.decode(reader, reader.uint32()));
                    continue;
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.MatchmakingQueuesResponse.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBaseMatchmakingQueuesResponse();
        message.queues = ((_a = object.queues) === null || _a === void 0 ? void 0 : _a.map(function (e) { return exports.MatchmakingQueueItem.fromPartial(e); })) || [];
        return message;
    },
};
function createBasePhysicalAttackEvent() {
    return { attacker: undefined, damageDealt: 0, defender: undefined };
}
exports.PhysicalAttackEvent = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.attacker !== undefined) {
            exports.Entity.encode(message.attacker, writer.uint32(10).fork()).ldelim();
        }
        if (message.damageDealt !== 0) {
            writer.uint32(16).int32(message.damageDealt);
        }
        if (message.defender !== undefined) {
            exports.Entity.encode(message.defender, writer.uint32(26).fork()).ldelim();
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBasePhysicalAttackEvent();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
                case 1:
                    if (tag !== 10) {
                        break;
                    }
                    message.attacker = exports.Entity.decode(reader, reader.uint32());
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
                    message.defender = exports.Entity.decode(reader, reader.uint32());
                    continue;
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.PhysicalAttackEvent.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBasePhysicalAttackEvent();
        message.attacker = object.attacker !== undefined && object.attacker !== null ? exports.Entity.fromPartial(object.attacker) : undefined;
        message.damageDealt = (_a = object.damageDealt) !== null && _a !== void 0 ? _a : 0;
        message.defender = object.defender !== undefined && object.defender !== null ? exports.Entity.fromPartial(object.defender) : undefined;
        return message;
    },
};
function createBasePostInviteRequest() {
    return { request: undefined };
}
exports.PostInviteRequest = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.request !== undefined) {
            exports.InvitePostRequest.encode(message.request, writer.uint32(10).fork()).ldelim();
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBasePostInviteRequest();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
                case 1:
                    if (tag !== 10) {
                        break;
                    }
                    message.request = exports.InvitePostRequest.decode(reader, reader.uint32());
                    continue;
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.PostInviteRequest.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var message = createBasePostInviteRequest();
        message.request = object.request !== undefined && object.request !== null ? exports.InvitePostRequest.fromPartial(object.request) : undefined;
        return message;
    },
};
function createBasePostPasswordResetRequest() {
    return { password1: "", password2: "", token: "" };
}
exports.PostPasswordResetRequest = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
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
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBasePostPasswordResetRequest();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.PostPasswordResetRequest.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b, _c;
        var message = createBasePostPasswordResetRequest();
        message.password1 = (_a = object.password1) !== null && _a !== void 0 ? _a : "";
        message.password2 = (_b = object.password2) !== null && _b !== void 0 ? _b : "";
        message.token = (_c = object.token) !== null && _c !== void 0 ? _c : "";
        return message;
    },
};
function createBasePrefab() {
    return { named: "" };
}
exports.Prefab = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.named !== "") {
            writer.uint32(10).string(message.named);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBasePrefab();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.Prefab.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBasePrefab();
        message.named = (_a = object.named) !== null && _a !== void 0 ? _a : "";
        return message;
    },
};
function createBaseReplay() {
    return { deltas: [], gameStates: [] };
}
exports.Replay = {
    encode: function (message, writer) {
        var e_40, _a, e_41, _b;
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        try {
            for (var _c = __values(message.deltas), _d = _c.next(); !_d.done; _d = _c.next()) {
                var v = _d.value;
                exports.ReplayDeltas.encode(v, writer.uint32(10).fork()).ldelim();
            }
        }
        catch (e_40_1) { e_40 = { error: e_40_1 }; }
        finally {
            try {
                if (_d && !_d.done && (_a = _c.return)) _a.call(_c);
            }
            finally { if (e_40) throw e_40.error; }
        }
        try {
            for (var _e = __values(message.gameStates), _f = _e.next(); !_f.done; _f = _e.next()) {
                var v = _f.value;
                exports.ReplayGameStates.encode(v, writer.uint32(18).fork()).ldelim();
            }
        }
        catch (e_41_1) { e_41 = { error: e_41_1 }; }
        finally {
            try {
                if (_f && !_f.done && (_b = _e.return)) _b.call(_e);
            }
            finally { if (e_41) throw e_41.error; }
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseReplay();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
                case 1:
                    if (tag !== 10) {
                        break;
                    }
                    message.deltas.push(exports.ReplayDeltas.decode(reader, reader.uint32()));
                    continue;
                case 2:
                    if (tag !== 18) {
                        break;
                    }
                    message.gameStates.push(exports.ReplayGameStates.decode(reader, reader.uint32()));
                    continue;
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.Replay.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b;
        var message = createBaseReplay();
        message.deltas = ((_a = object.deltas) === null || _a === void 0 ? void 0 : _a.map(function (e) { return exports.ReplayDeltas.fromPartial(e); })) || [];
        message.gameStates = ((_b = object.gameStates) === null || _b === void 0 ? void 0 : _b.map(function (e) { return exports.ReplayGameStates.fromPartial(e); })) || [];
        return message;
    },
};
function createBaseReplayDeltas() {
    return { backward: undefined, forward: undefined };
}
exports.ReplayDeltas = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.backward !== undefined) {
            exports.EntityChangeSet.encode(message.backward, writer.uint32(10).fork()).ldelim();
        }
        if (message.forward !== undefined) {
            exports.EntityChangeSet.encode(message.forward, writer.uint32(18).fork()).ldelim();
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseReplayDeltas();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
                case 1:
                    if (tag !== 10) {
                        break;
                    }
                    message.backward = exports.EntityChangeSet.decode(reader, reader.uint32());
                    continue;
                case 2:
                    if (tag !== 18) {
                        break;
                    }
                    message.forward = exports.EntityChangeSet.decode(reader, reader.uint32());
                    continue;
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.ReplayDeltas.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var message = createBaseReplayDeltas();
        message.backward = object.backward !== undefined && object.backward !== null ? exports.EntityChangeSet.fromPartial(object.backward) : undefined;
        message.forward = object.forward !== undefined && object.forward !== null ? exports.EntityChangeSet.fromPartial(object.forward) : undefined;
        return message;
    },
};
function createBaseReplayGameStates() {
    return { first: undefined, second: undefined };
}
exports.ReplayGameStates = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.first !== undefined) {
            exports.GameState.encode(message.first, writer.uint32(10).fork()).ldelim();
        }
        if (message.second !== undefined) {
            exports.GameState.encode(message.second, writer.uint32(18).fork()).ldelim();
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseReplayGameStates();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
                case 1:
                    if (tag !== 10) {
                        break;
                    }
                    message.first = exports.GameState.decode(reader, reader.uint32());
                    continue;
                case 2:
                    if (tag !== 18) {
                        break;
                    }
                    message.second = exports.GameState.decode(reader, reader.uint32());
                    continue;
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.ReplayGameStates.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var message = createBaseReplayGameStates();
        message.first = object.first !== undefined && object.first !== null ? exports.GameState.fromPartial(object.first) : undefined;
        message.second = object.second !== undefined && object.second !== null ? exports.GameState.fromPartial(object.second) : undefined;
        return message;
    },
};
function createBaseServerToClientMessage() {
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
exports.ServerToClientMessage = {
    encode: function (message, writer) {
        var e_42, _a;
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.actions !== undefined) {
            exports.GameActions.encode(message.actions, writer.uint32(10).fork()).ldelim();
        }
        if (message.changes !== undefined) {
            exports.EntityChangeSet.encode(message.changes, writer.uint32(18).fork()).ldelim();
        }
        if (message.emote !== undefined) {
            exports.Emote.encode(message.emote, writer.uint32(26).fork()).ldelim();
        }
        if (message.event !== undefined) {
            exports.GameEvent.encode(message.event, writer.uint32(34).fork()).ldelim();
        }
        if (message.gameOver !== undefined) {
            exports.GameOver.encode(message.gameOver, writer.uint32(42).fork()).ldelim();
        }
        if (message.gameState !== undefined) {
            exports.GameState.encode(message.gameState, writer.uint32(50).fork()).ldelim();
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
        try {
            for (var _b = __values(message.startingCards), _c = _b.next(); !_c.done; _c = _b.next()) {
                var v = _c.value;
                exports.Entity.encode(v, writer.uint32(90).fork()).ldelim();
            }
        }
        catch (e_42_1) { e_42 = { error: e_42_1 }; }
        finally {
            try {
                if (_c && !_c.done && (_a = _b.return)) _a.call(_b);
            }
            finally { if (e_42) throw e_42.error; }
        }
        if (message.timers !== undefined) {
            exports.Timers.encode(message.timers, writer.uint32(98).fork()).ldelim();
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseServerToClientMessage();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
                case 1:
                    if (tag !== 10) {
                        break;
                    }
                    message.actions = exports.GameActions.decode(reader, reader.uint32());
                    continue;
                case 2:
                    if (tag !== 18) {
                        break;
                    }
                    message.changes = exports.EntityChangeSet.decode(reader, reader.uint32());
                    continue;
                case 3:
                    if (tag !== 26) {
                        break;
                    }
                    message.emote = exports.Emote.decode(reader, reader.uint32());
                    continue;
                case 4:
                    if (tag !== 34) {
                        break;
                    }
                    message.event = exports.GameEvent.decode(reader, reader.uint32());
                    continue;
                case 5:
                    if (tag !== 42) {
                        break;
                    }
                    message.gameOver = exports.GameOver.decode(reader, reader.uint32());
                    continue;
                case 6:
                    if (tag !== 50) {
                        break;
                    }
                    message.gameState = exports.GameState.decode(reader, reader.uint32());
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
                    message.messageType = reader.int32();
                    continue;
                case 11:
                    if (tag !== 90) {
                        break;
                    }
                    message.startingCards.push(exports.Entity.decode(reader, reader.uint32()));
                    continue;
                case 12:
                    if (tag !== 98) {
                        break;
                    }
                    message.timers = exports.Timers.decode(reader, reader.uint32());
                    continue;
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.ServerToClientMessage.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b, _c, _d, _e;
        var message = createBaseServerToClientMessage();
        message.actions = object.actions !== undefined && object.actions !== null ? exports.GameActions.fromPartial(object.actions) : undefined;
        message.changes = object.changes !== undefined && object.changes !== null ? exports.EntityChangeSet.fromPartial(object.changes) : undefined;
        message.emote = object.emote !== undefined && object.emote !== null ? exports.Emote.fromPartial(object.emote) : undefined;
        message.event = object.event !== undefined && object.event !== null ? exports.GameEvent.fromPartial(object.event) : undefined;
        message.gameOver = object.gameOver !== undefined && object.gameOver !== null ? exports.GameOver.fromPartial(object.gameOver) : undefined;
        message.gameState = object.gameState !== undefined && object.gameState !== null ? exports.GameState.fromPartial(object.gameState) : undefined;
        message.id = (_a = object.id) !== null && _a !== void 0 ? _a : "";
        message.isReplayMessage = (_b = object.isReplayMessage) !== null && _b !== void 0 ? _b : false;
        message.localPlayerId = (_c = object.localPlayerId) !== null && _c !== void 0 ? _c : 0;
        message.messageType = (_d = object.messageType) !== null && _d !== void 0 ? _d : 0;
        message.startingCards = ((_e = object.startingCards) === null || _e === void 0 ? void 0 : _e.map(function (e) { return exports.Entity.fromPartial(e); })) || [];
        message.timers = object.timers !== undefined && object.timers !== null ? exports.Timers.fromPartial(object.timers) : undefined;
        return message;
    },
};
function createBaseSpanContext() {
    return { data: "" };
}
exports.SpanContext = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.data !== "") {
            writer.uint32(10).string(message.data);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseSpanContext();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.SpanContext.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBaseSpanContext();
        message.data = (_a = object.data) !== null && _a !== void 0 ? _a : "";
        return message;
    },
};
function createBaseSpellAction() {
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
exports.SpellAction = {
    encode: function (message, writer) {
        var e_43, _a, e_44, _b;
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.action !== 0) {
            writer.uint32(8).int32(message.action);
        }
        if (message.actionType !== 0) {
            writer.uint32(16).int32(message.actionType);
        }
        try {
            for (var _c = __values(message.choices), _d = _c.next(); !_d.done; _d = _c.next()) {
                var v = _d.value;
                exports.SpellAction.encode(v, writer.uint32(26).fork()).ldelim();
            }
        }
        catch (e_43_1) { e_43 = { error: e_43_1 }; }
        finally {
            try {
                if (_d && !_d.done && (_a = _c.return)) _a.call(_c);
            }
            finally { if (e_43) throw e_43.error; }
        }
        if (message.description !== "") {
            writer.uint32(34).string(message.description);
        }
        if (message.entity !== undefined) {
            exports.Entity.encode(message.entity, writer.uint32(42).fork()).ldelim();
        }
        if (message.sourceId !== 0) {
            writer.uint32(48).int32(message.sourceId);
        }
        try {
            for (var _e = __values(message.targetKeyToActions), _f = _e.next(); !_f.done; _f = _e.next()) {
                var v = _f.value;
                exports.TargetActionPair.encode(v, writer.uint32(58).fork()).ldelim();
            }
        }
        catch (e_44_1) { e_44 = { error: e_44_1 }; }
        finally {
            try {
                if (_f && !_f.done && (_b = _e.return)) _b.call(_e);
            }
            finally { if (e_44) throw e_44.error; }
        }
        if (message.request !== "") {
            writer.uint32(66).string(message.request);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseSpellAction();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
                    message.actionType = reader.int32();
                    continue;
                case 3:
                    if (tag !== 26) {
                        break;
                    }
                    message.choices.push(exports.SpellAction.decode(reader, reader.uint32()));
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
                    message.entity = exports.Entity.decode(reader, reader.uint32());
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
                    message.targetKeyToActions.push(exports.TargetActionPair.decode(reader, reader.uint32()));
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
    create: function (base) {
        return exports.SpellAction.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b, _c, _d, _e, _f, _g;
        var message = createBaseSpellAction();
        message.action = (_a = object.action) !== null && _a !== void 0 ? _a : 0;
        message.actionType = (_b = object.actionType) !== null && _b !== void 0 ? _b : 0;
        message.choices = ((_c = object.choices) === null || _c === void 0 ? void 0 : _c.map(function (e) { return exports.SpellAction.fromPartial(e); })) || [];
        message.description = (_d = object.description) !== null && _d !== void 0 ? _d : "";
        message.entity = object.entity !== undefined && object.entity !== null ? exports.Entity.fromPartial(object.entity) : undefined;
        message.sourceId = (_e = object.sourceId) !== null && _e !== void 0 ? _e : 0;
        message.targetKeyToActions = ((_f = object.targetKeyToActions) === null || _f === void 0 ? void 0 : _f.map(function (e) { return exports.TargetActionPair.fromPartial(e); })) || [];
        message.request = (_g = object.request) !== null && _g !== void 0 ? _g : "";
        return message;
    },
};
function createBaseSpellsourceException() {
    return { message: "" };
}
exports.SpellsourceException = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.message !== "") {
            writer.uint32(10).string(message.message);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseSpellsourceException();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.SpellsourceException.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBaseSpellsourceException();
        message.message = (_a = object.message) !== null && _a !== void 0 ? _a : "";
        return message;
    },
};
function createBaseSprite() {
    return { named: "", pivot: 0 };
}
exports.Sprite = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.named !== "") {
            writer.uint32(10).string(message.named);
        }
        if (message.pivot !== 0) {
            writer.uint32(16).int32(message.pivot);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseSprite();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
                    message.pivot = reader.int32();
                    continue;
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.Sprite.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b;
        var message = createBaseSprite();
        message.named = (_a = object.named) !== null && _a !== void 0 ? _a : "";
        message.pivot = (_b = object.pivot) !== null && _b !== void 0 ? _b : 0;
        return message;
    },
};
function createBaseTargetActionPair() {
    return { action: 0, friendlyBattlefieldIndex: 0, target: 0 };
}
exports.TargetActionPair = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
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
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseTargetActionPair();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.TargetActionPair.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b, _c;
        var message = createBaseTargetActionPair();
        message.action = (_a = object.action) !== null && _a !== void 0 ? _a : 0;
        message.friendlyBattlefieldIndex = (_b = object.friendlyBattlefieldIndex) !== null && _b !== void 0 ? _b : 0;
        message.target = (_c = object.target) !== null && _c !== void 0 ? _c : 0;
        return message;
    },
};
function createBaseTimers() {
    return { millisRemaining: 0 };
}
exports.Timers = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.millisRemaining !== 0) {
            writer.uint32(8).int64(message.millisRemaining);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseTimers();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
                case 1:
                    if (tag !== 8) {
                        break;
                    }
                    message.millisRemaining = longToNumber(reader.int64());
                    continue;
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.Timers.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a;
        var message = createBaseTimers();
        message.millisRemaining = (_a = object.millisRemaining) !== null && _a !== void 0 ? _a : 0;
        return message;
    },
};
function createBaseTooltip() {
    return { keywords: [], text: "" };
}
exports.Tooltip = {
    encode: function (message, writer) {
        var e_45, _a;
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        try {
            for (var _b = __values(message.keywords), _c = _b.next(); !_c.done; _c = _b.next()) {
                var v = _c.value;
                writer.uint32(10).string(v);
            }
        }
        catch (e_45_1) { e_45 = { error: e_45_1 }; }
        finally {
            try {
                if (_c && !_c.done && (_a = _b.return)) _a.call(_b);
            }
            finally { if (e_45) throw e_45.error; }
        }
        if (message.text !== "") {
            writer.uint32(18).string(message.text);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseTooltip();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.Tooltip.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b;
        var message = createBaseTooltip();
        message.keywords = ((_a = object.keywords) === null || _a === void 0 ? void 0 : _a.map(function (e) { return e; })) || [];
        message.text = (_b = object.text) !== null && _b !== void 0 ? _b : "";
        return message;
    },
};
function createBaseUnfriendResponse() {
    return { deletedFriend: undefined };
}
exports.UnfriendResponse = {
    encode: function (message, writer) {
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        if (message.deletedFriend !== undefined) {
            exports.Friend.encode(message.deletedFriend, writer.uint32(10).fork()).ldelim();
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseUnfriendResponse();
        while (reader.pos < end) {
            var tag = reader.uint32();
            switch (tag >>> 3) {
                case 1:
                    if (tag !== 10) {
                        break;
                    }
                    message.deletedFriend = exports.Friend.decode(reader, reader.uint32());
                    continue;
            }
            if ((tag & 7) === 4 || tag === 0) {
                break;
            }
            reader.skipType(tag & 7);
        }
        return message;
    },
    create: function (base) {
        return exports.UnfriendResponse.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var message = createBaseUnfriendResponse();
        message.deletedFriend = object.deletedFriend !== undefined && object.deletedFriend !== null ? exports.Friend.fromPartial(object.deletedFriend) : undefined;
        return message;
    },
};
function createBaseValidationReport() {
    return { errors: [], valid: false };
}
exports.ValidationReport = {
    encode: function (message, writer) {
        var e_46, _a;
        if (writer === void 0) { writer = minimal_1.default.Writer.create(); }
        try {
            for (var _b = __values(message.errors), _c = _b.next(); !_c.done; _c = _b.next()) {
                var v = _c.value;
                writer.uint32(10).string(v);
            }
        }
        catch (e_46_1) { e_46 = { error: e_46_1 }; }
        finally {
            try {
                if (_c && !_c.done && (_a = _b.return)) _a.call(_b);
            }
            finally { if (e_46) throw e_46.error; }
        }
        if (message.valid === true) {
            writer.uint32(16).bool(message.valid);
        }
        return writer;
    },
    decode: function (input, length) {
        var reader = input instanceof minimal_1.default.Reader ? input : minimal_1.default.Reader.create(input);
        var end = length === undefined ? reader.len : reader.pos + length;
        var message = createBaseValidationReport();
        while (reader.pos < end) {
            var tag = reader.uint32();
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
    create: function (base) {
        return exports.ValidationReport.fromPartial(base !== null && base !== void 0 ? base : {});
    },
    fromPartial: function (object) {
        var _a, _b;
        var message = createBaseValidationReport();
        message.errors = ((_a = object.errors) === null || _a === void 0 ? void 0 : _a.map(function (e) { return e; })) || [];
        message.valid = (_b = object.valid) !== null && _b !== void 0 ? _b : false;
        return message;
    },
};
// @ts-ignore
var MatchmakingClientImpl = /** @class */ (function () {
    function MatchmakingClientImpl(rpc) {
        this.rpc = rpc;
        this.matchmakingDelete = this.matchmakingDelete.bind(this);
        this.matchmakingGet = this.matchmakingGet.bind(this);
    }
    MatchmakingClientImpl.prototype.matchmakingDelete = function (request, metadata) {
        return this.rpc.unary(exports.MatchmakingMatchmakingDeleteDesc, empty_1.Empty.fromPartial(request), metadata);
    };
    MatchmakingClientImpl.prototype.matchmakingGet = function (request, metadata) {
        return this.rpc.unary(exports.MatchmakingMatchmakingGetDesc, empty_1.Empty.fromPartial(request), metadata);
    };
    return MatchmakingClientImpl;
}());
exports.MatchmakingClientImpl = MatchmakingClientImpl;
exports.MatchmakingDesc = { serviceName: "spellsource.Matchmaking" };
exports.MatchmakingMatchmakingDeleteDesc = {
    methodName: "MatchmakingDelete",
    service: exports.MatchmakingDesc,
    requestStream: false,
    responseStream: false,
    requestType: {
        serializeBinary: function () {
            return empty_1.Empty.encode(this).finish();
        },
    },
    responseType: {
        deserializeBinary: function (data) {
            var value = exports.MatchCancelResponse.decode(data);
            return __assign(__assign({}, value), { toObject: function () {
                    return value;
                } });
        },
    },
};
exports.MatchmakingMatchmakingGetDesc = {
    methodName: "MatchmakingGet",
    service: exports.MatchmakingDesc,
    requestStream: false,
    responseStream: false,
    requestType: {
        serializeBinary: function () {
            return empty_1.Empty.encode(this).finish();
        },
    },
    responseType: {
        deserializeBinary: function (data) {
            var value = exports.MatchmakingQueuesResponse.decode(data);
            return __assign(__assign({}, value), { toObject: function () {
                    return value;
                } });
        },
    },
};
// @ts-ignore
var HiddenSwitchSpellsourceAPIServiceClientImpl = /** @class */ (function () {
    function HiddenSwitchSpellsourceAPIServiceClientImpl(rpc) {
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
    HiddenSwitchSpellsourceAPIServiceClientImpl.prototype.putCard = function (request, metadata) {
        return this.rpc.unary(exports.HiddenSwitchSpellsourceAPIServicePutCardDesc, exports.Envelope_MethodMessage_PutCardMessage.fromPartial(request), metadata);
    };
    HiddenSwitchSpellsourceAPIServiceClientImpl.prototype.sendMessage = function (request, metadata) {
        return this.rpc.unary(exports.HiddenSwitchSpellsourceAPIServiceSendMessageDesc, exports.Envelope_MethodMessage_SendMessageMessage.fromPartial(request), metadata);
    };
    HiddenSwitchSpellsourceAPIServiceClientImpl.prototype.deleteCard = function (request, metadata) {
        return this.rpc.unary(exports.HiddenSwitchSpellsourceAPIServiceDeleteCardDesc, exports.Envelope_MethodMessage_DeleteCardMessage.fromPartial(request), metadata);
    };
    HiddenSwitchSpellsourceAPIServiceClientImpl.prototype.subscribeFriends = function (request, metadata) {
        return this.rpc.invoke(exports.HiddenSwitchSpellsourceAPIServiceSubscribeFriendsDesc, empty_1.Empty.fromPartial(request), metadata);
    };
    HiddenSwitchSpellsourceAPIServiceClientImpl.prototype.subscribeInvites = function (request, metadata) {
        return this.rpc.invoke(exports.HiddenSwitchSpellsourceAPIServiceSubscribeInvitesDesc, empty_1.Empty.fromPartial(request), metadata);
    };
    HiddenSwitchSpellsourceAPIServiceClientImpl.prototype.subscribeEditableCards = function (request, metadata) {
        return this.rpc.invoke(exports.HiddenSwitchSpellsourceAPIServiceSubscribeEditableCardsDesc, empty_1.Empty.fromPartial(request), metadata);
    };
    HiddenSwitchSpellsourceAPIServiceClientImpl.prototype.subscribeMatch = function (request, metadata) {
        return this.rpc.invoke(exports.HiddenSwitchSpellsourceAPIServiceSubscribeMatchDesc, empty_1.Empty.fromPartial(request), metadata);
    };
    HiddenSwitchSpellsourceAPIServiceClientImpl.prototype.acceptInvite = function (request, metadata) {
        return this.rpc.unary(exports.HiddenSwitchSpellsourceAPIServiceAcceptInviteDesc, exports.AcceptInviteRequest.fromPartial(request), metadata);
    };
    HiddenSwitchSpellsourceAPIServiceClientImpl.prototype.changePassword = function (request, metadata) {
        return this.rpc.unary(exports.HiddenSwitchSpellsourceAPIServiceChangePasswordDesc, exports.ChangePasswordRequest.fromPartial(request), metadata);
    };
    HiddenSwitchSpellsourceAPIServiceClientImpl.prototype.createAccount = function (request, metadata) {
        return this.rpc.unary(exports.HiddenSwitchSpellsourceAPIServiceCreateAccountDesc, exports.CreateAccountRequest.fromPartial(request), metadata);
    };
    HiddenSwitchSpellsourceAPIServiceClientImpl.prototype.decksDelete = function (request, metadata) {
        return this.rpc.unary(exports.HiddenSwitchSpellsourceAPIServiceDecksDeleteDesc, exports.DecksDeleteRequest.fromPartial(request), metadata);
    };
    HiddenSwitchSpellsourceAPIServiceClientImpl.prototype.decksGet = function (request, metadata) {
        return this.rpc.unary(exports.HiddenSwitchSpellsourceAPIServiceDecksGetDesc, exports.DecksGetRequest.fromPartial(request), metadata);
    };
    HiddenSwitchSpellsourceAPIServiceClientImpl.prototype.decksGetAll = function (request, metadata) {
        return this.rpc.unary(exports.HiddenSwitchSpellsourceAPIServiceDecksGetAllDesc, empty_1.Empty.fromPartial(request), metadata);
    };
    HiddenSwitchSpellsourceAPIServiceClientImpl.prototype.decksPut = function (request, metadata) {
        return this.rpc.unary(exports.HiddenSwitchSpellsourceAPIServiceDecksPutDesc, exports.DecksPutRequest.fromPartial(request), metadata);
    };
    HiddenSwitchSpellsourceAPIServiceClientImpl.prototype.decksUpdate = function (request, metadata) {
        return this.rpc.unary(exports.HiddenSwitchSpellsourceAPIServiceDecksUpdateDesc, exports.DecksUpdateRequest.fromPartial(request), metadata);
    };
    HiddenSwitchSpellsourceAPIServiceClientImpl.prototype.duplicateDeck = function (request, metadata) {
        return this.rpc.unary(exports.HiddenSwitchSpellsourceAPIServiceDuplicateDeckDesc, request, metadata);
    };
    HiddenSwitchSpellsourceAPIServiceClientImpl.prototype.deleteInvite = function (request, metadata) {
        return this.rpc.unary(exports.HiddenSwitchSpellsourceAPIServiceDeleteInviteDesc, exports.DeleteInviteRequest.fromPartial(request), metadata);
    };
    HiddenSwitchSpellsourceAPIServiceClientImpl.prototype.draftsChooseCard = function (request, metadata) {
        return this.rpc.unary(exports.HiddenSwitchSpellsourceAPIServiceDraftsChooseCardDesc, exports.DraftsChooseCardRequest.fromPartial(request), metadata);
    };
    HiddenSwitchSpellsourceAPIServiceClientImpl.prototype.draftsChooseHero = function (request, metadata) {
        return this.rpc.unary(exports.HiddenSwitchSpellsourceAPIServiceDraftsChooseHeroDesc, exports.DraftsChooseHeroRequest.fromPartial(request), metadata);
    };
    HiddenSwitchSpellsourceAPIServiceClientImpl.prototype.draftsGet = function (request, metadata) {
        return this.rpc.unary(exports.HiddenSwitchSpellsourceAPIServiceDraftsGetDesc, empty_1.Empty.fromPartial(request), metadata);
    };
    HiddenSwitchSpellsourceAPIServiceClientImpl.prototype.draftsPost = function (request, metadata) {
        return this.rpc.unary(exports.HiddenSwitchSpellsourceAPIServiceDraftsPostDesc, exports.DraftsPostRequest.fromPartial(request), metadata);
    };
    HiddenSwitchSpellsourceAPIServiceClientImpl.prototype.friendDelete = function (request, metadata) {
        return this.rpc.unary(exports.HiddenSwitchSpellsourceAPIServiceFriendDeleteDesc, exports.FriendDeleteRequest.fromPartial(request), metadata);
    };
    HiddenSwitchSpellsourceAPIServiceClientImpl.prototype.friendPut = function (request, metadata) {
        return this.rpc.unary(exports.HiddenSwitchSpellsourceAPIServiceFriendPutDesc, exports.FriendPutRequest.fromPartial(request), metadata);
    };
    HiddenSwitchSpellsourceAPIServiceClientImpl.prototype.getAccount = function (request, metadata) {
        return this.rpc.unary(exports.HiddenSwitchSpellsourceAPIServiceGetAccountDesc, exports.GetAccountRequest.fromPartial(request), metadata);
    };
    HiddenSwitchSpellsourceAPIServiceClientImpl.prototype.getAccounts = function (request, metadata) {
        return this.rpc.unary(exports.HiddenSwitchSpellsourceAPIServiceGetAccountsDesc, exports.GetAccountsRequest.fromPartial(request), metadata);
    };
    HiddenSwitchSpellsourceAPIServiceClientImpl.prototype.getGameRecord = function (request, metadata) {
        return this.rpc.unary(exports.HiddenSwitchSpellsourceAPIServiceGetGameRecordDesc, exports.GetGameRecordRequest.fromPartial(request), metadata);
    };
    HiddenSwitchSpellsourceAPIServiceClientImpl.prototype.getGameRecordIds = function (request, metadata) {
        return this.rpc.unary(exports.HiddenSwitchSpellsourceAPIServiceGetGameRecordIdsDesc, empty_1.Empty.fromPartial(request), metadata);
    };
    HiddenSwitchSpellsourceAPIServiceClientImpl.prototype.getInvite = function (request, metadata) {
        return this.rpc.unary(exports.HiddenSwitchSpellsourceAPIServiceGetInviteDesc, exports.GetInviteRequest.fromPartial(request), metadata);
    };
    HiddenSwitchSpellsourceAPIServiceClientImpl.prototype.getInvites = function (request, metadata) {
        return this.rpc.unary(exports.HiddenSwitchSpellsourceAPIServiceGetInvitesDesc, empty_1.Empty.fromPartial(request), metadata);
    };
    HiddenSwitchSpellsourceAPIServiceClientImpl.prototype.login = function (request, metadata) {
        return this.rpc.unary(exports.HiddenSwitchSpellsourceAPIServiceLoginDesc, exports.LoginRequest.fromPartial(request), metadata);
    };
    HiddenSwitchSpellsourceAPIServiceClientImpl.prototype.postInvite = function (request, metadata) {
        return this.rpc.invoke(exports.HiddenSwitchSpellsourceAPIServicePostInviteDesc, exports.PostInviteRequest.fromPartial(request), metadata);
    };
    HiddenSwitchSpellsourceAPIServiceClientImpl.prototype.postPasswordReset = function (request, metadata) {
        return this.rpc.unary(exports.HiddenSwitchSpellsourceAPIServicePostPasswordResetDesc, exports.PostPasswordResetRequest.fromPartial(request), metadata);
    };
    return HiddenSwitchSpellsourceAPIServiceClientImpl;
}());
exports.HiddenSwitchSpellsourceAPIServiceClientImpl = HiddenSwitchSpellsourceAPIServiceClientImpl;
exports.HiddenSwitchSpellsourceAPIServiceDesc = { serviceName: "spellsource.HiddenSwitchSpellsourceAPIService" };
exports.HiddenSwitchSpellsourceAPIServicePutCardDesc = {
    methodName: "PutCard",
    service: exports.HiddenSwitchSpellsourceAPIServiceDesc,
    requestStream: false,
    responseStream: false,
    requestType: {
        serializeBinary: function () {
            return exports.Envelope_MethodMessage_PutCardMessage.encode(this).finish();
        },
    },
    responseType: {
        deserializeBinary: function (data) {
            var value = exports.Envelope_ResultMessage_PutCardMessage.decode(data);
            return __assign(__assign({}, value), { toObject: function () {
                    return value;
                } });
        },
    },
};
exports.HiddenSwitchSpellsourceAPIServiceSendMessageDesc = {
    methodName: "SendMessage",
    service: exports.HiddenSwitchSpellsourceAPIServiceDesc,
    requestStream: false,
    responseStream: false,
    requestType: {
        serializeBinary: function () {
            return exports.Envelope_MethodMessage_SendMessageMessage.encode(this).finish();
        },
    },
    responseType: {
        deserializeBinary: function (data) {
            var value = exports.Envelope_ResultMessage_SendMessageMessage.decode(data);
            return __assign(__assign({}, value), { toObject: function () {
                    return value;
                } });
        },
    },
};
exports.HiddenSwitchSpellsourceAPIServiceDeleteCardDesc = {
    methodName: "DeleteCard",
    service: exports.HiddenSwitchSpellsourceAPIServiceDesc,
    requestStream: false,
    responseStream: false,
    requestType: {
        serializeBinary: function () {
            return exports.Envelope_MethodMessage_DeleteCardMessage.encode(this).finish();
        },
    },
    responseType: {
        deserializeBinary: function (data) {
            var value = exports.Envelope_RemovedMessage.decode(data);
            return __assign(__assign({}, value), { toObject: function () {
                    return value;
                } });
        },
    },
};
exports.HiddenSwitchSpellsourceAPIServiceSubscribeFriendsDesc = {
    methodName: "SubscribeFriends",
    service: exports.HiddenSwitchSpellsourceAPIServiceDesc,
    requestStream: false,
    responseStream: true,
    requestType: {
        serializeBinary: function () {
            return empty_1.Empty.encode(this).finish();
        },
    },
    responseType: {
        deserializeBinary: function (data) {
            var value = exports.Friend.decode(data);
            return __assign(__assign({}, value), { toObject: function () {
                    return value;
                } });
        },
    },
};
exports.HiddenSwitchSpellsourceAPIServiceSubscribeInvitesDesc = {
    methodName: "SubscribeInvites",
    service: exports.HiddenSwitchSpellsourceAPIServiceDesc,
    requestStream: false,
    responseStream: true,
    requestType: {
        serializeBinary: function () {
            return empty_1.Empty.encode(this).finish();
        },
    },
    responseType: {
        deserializeBinary: function (data) {
            var value = exports.Invite.decode(data);
            return __assign(__assign({}, value), { toObject: function () {
                    return value;
                } });
        },
    },
};
exports.HiddenSwitchSpellsourceAPIServiceSubscribeEditableCardsDesc = {
    methodName: "SubscribeEditableCards",
    service: exports.HiddenSwitchSpellsourceAPIServiceDesc,
    requestStream: false,
    responseStream: true,
    requestType: {
        serializeBinary: function () {
            return empty_1.Empty.encode(this).finish();
        },
    },
    responseType: {
        deserializeBinary: function (data) {
            var value = exports.EditableCard.decode(data);
            return __assign(__assign({}, value), { toObject: function () {
                    return value;
                } });
        },
    },
};
exports.HiddenSwitchSpellsourceAPIServiceSubscribeMatchDesc = {
    methodName: "SubscribeMatch",
    service: exports.HiddenSwitchSpellsourceAPIServiceDesc,
    requestStream: false,
    responseStream: true,
    requestType: {
        serializeBinary: function () {
            return empty_1.Empty.encode(this).finish();
        },
    },
    responseType: {
        deserializeBinary: function (data) {
            var value = exports.Match.decode(data);
            return __assign(__assign({}, value), { toObject: function () {
                    return value;
                } });
        },
    },
};
exports.HiddenSwitchSpellsourceAPIServiceAcceptInviteDesc = {
    methodName: "AcceptInvite",
    service: exports.HiddenSwitchSpellsourceAPIServiceDesc,
    requestStream: false,
    responseStream: false,
    requestType: {
        serializeBinary: function () {
            return exports.AcceptInviteRequest.encode(this).finish();
        },
    },
    responseType: {
        deserializeBinary: function (data) {
            var value = exports.AcceptInviteResponse.decode(data);
            return __assign(__assign({}, value), { toObject: function () {
                    return value;
                } });
        },
    },
};
exports.HiddenSwitchSpellsourceAPIServiceChangePasswordDesc = {
    methodName: "ChangePassword",
    service: exports.HiddenSwitchSpellsourceAPIServiceDesc,
    requestStream: false,
    responseStream: false,
    requestType: {
        serializeBinary: function () {
            return exports.ChangePasswordRequest.encode(this).finish();
        },
    },
    responseType: {
        deserializeBinary: function (data) {
            var value = exports.ChangePasswordResponse.decode(data);
            return __assign(__assign({}, value), { toObject: function () {
                    return value;
                } });
        },
    },
};
exports.HiddenSwitchSpellsourceAPIServiceCreateAccountDesc = {
    methodName: "CreateAccount",
    service: exports.HiddenSwitchSpellsourceAPIServiceDesc,
    requestStream: false,
    responseStream: false,
    requestType: {
        serializeBinary: function () {
            return exports.CreateAccountRequest.encode(this).finish();
        },
    },
    responseType: {
        deserializeBinary: function (data) {
            var value = exports.CreateAccountResponse.decode(data);
            return __assign(__assign({}, value), { toObject: function () {
                    return value;
                } });
        },
    },
};
exports.HiddenSwitchSpellsourceAPIServiceDecksDeleteDesc = {
    methodName: "DecksDelete",
    service: exports.HiddenSwitchSpellsourceAPIServiceDesc,
    requestStream: false,
    responseStream: false,
    requestType: {
        serializeBinary: function () {
            return exports.DecksDeleteRequest.encode(this).finish();
        },
    },
    responseType: {
        deserializeBinary: function (data) {
            var value = empty_1.Empty.decode(data);
            return __assign(__assign({}, value), { toObject: function () {
                    return value;
                } });
        },
    },
};
exports.HiddenSwitchSpellsourceAPIServiceDecksGetDesc = {
    methodName: "DecksGet",
    service: exports.HiddenSwitchSpellsourceAPIServiceDesc,
    requestStream: false,
    responseStream: false,
    requestType: {
        serializeBinary: function () {
            return exports.DecksGetRequest.encode(this).finish();
        },
    },
    responseType: {
        deserializeBinary: function (data) {
            var value = exports.DecksGetResponse.decode(data);
            return __assign(__assign({}, value), { toObject: function () {
                    return value;
                } });
        },
    },
};
exports.HiddenSwitchSpellsourceAPIServiceDecksGetAllDesc = {
    methodName: "DecksGetAll",
    service: exports.HiddenSwitchSpellsourceAPIServiceDesc,
    requestStream: false,
    responseStream: false,
    requestType: {
        serializeBinary: function () {
            return empty_1.Empty.encode(this).finish();
        },
    },
    responseType: {
        deserializeBinary: function (data) {
            var value = exports.DecksGetAllResponse.decode(data);
            return __assign(__assign({}, value), { toObject: function () {
                    return value;
                } });
        },
    },
};
exports.HiddenSwitchSpellsourceAPIServiceDecksPutDesc = {
    methodName: "DecksPut",
    service: exports.HiddenSwitchSpellsourceAPIServiceDesc,
    requestStream: false,
    responseStream: false,
    requestType: {
        serializeBinary: function () {
            return exports.DecksPutRequest.encode(this).finish();
        },
    },
    responseType: {
        deserializeBinary: function (data) {
            var value = exports.DecksPutResponse.decode(data);
            return __assign(__assign({}, value), { toObject: function () {
                    return value;
                } });
        },
    },
};
exports.HiddenSwitchSpellsourceAPIServiceDecksUpdateDesc = {
    methodName: "DecksUpdate",
    service: exports.HiddenSwitchSpellsourceAPIServiceDesc,
    requestStream: false,
    responseStream: false,
    requestType: {
        serializeBinary: function () {
            return exports.DecksUpdateRequest.encode(this).finish();
        },
    },
    responseType: {
        deserializeBinary: function (data) {
            var value = exports.DecksGetResponse.decode(data);
            return __assign(__assign({}, value), { toObject: function () {
                    return value;
                } });
        },
    },
};
exports.HiddenSwitchSpellsourceAPIServiceDuplicateDeckDesc = {
    methodName: "DuplicateDeck",
    service: exports.HiddenSwitchSpellsourceAPIServiceDesc,
    requestStream: false,
    responseStream: false,
    requestType: {
        serializeBinary: function () {
            return wrappers_1.StringValue.encode(this).finish();
        },
    },
    responseType: {
        deserializeBinary: function (data) {
            var value = exports.DecksGetResponse.decode(data);
            return __assign(__assign({}, value), { toObject: function () {
                    return value;
                } });
        },
    },
};
exports.HiddenSwitchSpellsourceAPIServiceDeleteInviteDesc = {
    methodName: "DeleteInvite",
    service: exports.HiddenSwitchSpellsourceAPIServiceDesc,
    requestStream: false,
    responseStream: false,
    requestType: {
        serializeBinary: function () {
            return exports.DeleteInviteRequest.encode(this).finish();
        },
    },
    responseType: {
        deserializeBinary: function (data) {
            var value = exports.InviteResponse.decode(data);
            return __assign(__assign({}, value), { toObject: function () {
                    return value;
                } });
        },
    },
};
exports.HiddenSwitchSpellsourceAPIServiceDraftsChooseCardDesc = {
    methodName: "DraftsChooseCard",
    service: exports.HiddenSwitchSpellsourceAPIServiceDesc,
    requestStream: false,
    responseStream: false,
    requestType: {
        serializeBinary: function () {
            return exports.DraftsChooseCardRequest.encode(this).finish();
        },
    },
    responseType: {
        deserializeBinary: function (data) {
            var value = exports.DraftState.decode(data);
            return __assign(__assign({}, value), { toObject: function () {
                    return value;
                } });
        },
    },
};
exports.HiddenSwitchSpellsourceAPIServiceDraftsChooseHeroDesc = {
    methodName: "DraftsChooseHero",
    service: exports.HiddenSwitchSpellsourceAPIServiceDesc,
    requestStream: false,
    responseStream: false,
    requestType: {
        serializeBinary: function () {
            return exports.DraftsChooseHeroRequest.encode(this).finish();
        },
    },
    responseType: {
        deserializeBinary: function (data) {
            var value = exports.DraftState.decode(data);
            return __assign(__assign({}, value), { toObject: function () {
                    return value;
                } });
        },
    },
};
exports.HiddenSwitchSpellsourceAPIServiceDraftsGetDesc = {
    methodName: "DraftsGet",
    service: exports.HiddenSwitchSpellsourceAPIServiceDesc,
    requestStream: false,
    responseStream: false,
    requestType: {
        serializeBinary: function () {
            return empty_1.Empty.encode(this).finish();
        },
    },
    responseType: {
        deserializeBinary: function (data) {
            var value = exports.DraftState.decode(data);
            return __assign(__assign({}, value), { toObject: function () {
                    return value;
                } });
        },
    },
};
exports.HiddenSwitchSpellsourceAPIServiceDraftsPostDesc = {
    methodName: "DraftsPost",
    service: exports.HiddenSwitchSpellsourceAPIServiceDesc,
    requestStream: false,
    responseStream: false,
    requestType: {
        serializeBinary: function () {
            return exports.DraftsPostRequest.encode(this).finish();
        },
    },
    responseType: {
        deserializeBinary: function (data) {
            var value = exports.DraftState.decode(data);
            return __assign(__assign({}, value), { toObject: function () {
                    return value;
                } });
        },
    },
};
exports.HiddenSwitchSpellsourceAPIServiceFriendDeleteDesc = {
    methodName: "FriendDelete",
    service: exports.HiddenSwitchSpellsourceAPIServiceDesc,
    requestStream: false,
    responseStream: false,
    requestType: {
        serializeBinary: function () {
            return exports.FriendDeleteRequest.encode(this).finish();
        },
    },
    responseType: {
        deserializeBinary: function (data) {
            var value = exports.UnfriendResponse.decode(data);
            return __assign(__assign({}, value), { toObject: function () {
                    return value;
                } });
        },
    },
};
exports.HiddenSwitchSpellsourceAPIServiceFriendPutDesc = {
    methodName: "FriendPut",
    service: exports.HiddenSwitchSpellsourceAPIServiceDesc,
    requestStream: false,
    responseStream: false,
    requestType: {
        serializeBinary: function () {
            return exports.FriendPutRequest.encode(this).finish();
        },
    },
    responseType: {
        deserializeBinary: function (data) {
            var value = exports.FriendPutResponse.decode(data);
            return __assign(__assign({}, value), { toObject: function () {
                    return value;
                } });
        },
    },
};
exports.HiddenSwitchSpellsourceAPIServiceGetAccountDesc = {
    methodName: "GetAccount",
    service: exports.HiddenSwitchSpellsourceAPIServiceDesc,
    requestStream: false,
    responseStream: false,
    requestType: {
        serializeBinary: function () {
            return exports.GetAccountRequest.encode(this).finish();
        },
    },
    responseType: {
        deserializeBinary: function (data) {
            var value = exports.GetAccountsResponse.decode(data);
            return __assign(__assign({}, value), { toObject: function () {
                    return value;
                } });
        },
    },
};
exports.HiddenSwitchSpellsourceAPIServiceGetAccountsDesc = {
    methodName: "GetAccounts",
    service: exports.HiddenSwitchSpellsourceAPIServiceDesc,
    requestStream: false,
    responseStream: false,
    requestType: {
        serializeBinary: function () {
            return exports.GetAccountsRequest.encode(this).finish();
        },
    },
    responseType: {
        deserializeBinary: function (data) {
            var value = exports.GetAccountsResponse.decode(data);
            return __assign(__assign({}, value), { toObject: function () {
                    return value;
                } });
        },
    },
};
exports.HiddenSwitchSpellsourceAPIServiceGetGameRecordDesc = {
    methodName: "GetGameRecord",
    service: exports.HiddenSwitchSpellsourceAPIServiceDesc,
    requestStream: false,
    responseStream: false,
    requestType: {
        serializeBinary: function () {
            return exports.GetGameRecordRequest.encode(this).finish();
        },
    },
    responseType: {
        deserializeBinary: function (data) {
            var value = exports.GetGameRecordResponse.decode(data);
            return __assign(__assign({}, value), { toObject: function () {
                    return value;
                } });
        },
    },
};
exports.HiddenSwitchSpellsourceAPIServiceGetGameRecordIdsDesc = {
    methodName: "GetGameRecordIds",
    service: exports.HiddenSwitchSpellsourceAPIServiceDesc,
    requestStream: false,
    responseStream: false,
    requestType: {
        serializeBinary: function () {
            return empty_1.Empty.encode(this).finish();
        },
    },
    responseType: {
        deserializeBinary: function (data) {
            var value = exports.GetGameRecordIdsResponse.decode(data);
            return __assign(__assign({}, value), { toObject: function () {
                    return value;
                } });
        },
    },
};
exports.HiddenSwitchSpellsourceAPIServiceGetInviteDesc = {
    methodName: "GetInvite",
    service: exports.HiddenSwitchSpellsourceAPIServiceDesc,
    requestStream: false,
    responseStream: false,
    requestType: {
        serializeBinary: function () {
            return exports.GetInviteRequest.encode(this).finish();
        },
    },
    responseType: {
        deserializeBinary: function (data) {
            var value = exports.InviteResponse.decode(data);
            return __assign(__assign({}, value), { toObject: function () {
                    return value;
                } });
        },
    },
};
exports.HiddenSwitchSpellsourceAPIServiceGetInvitesDesc = {
    methodName: "GetInvites",
    service: exports.HiddenSwitchSpellsourceAPIServiceDesc,
    requestStream: false,
    responseStream: false,
    requestType: {
        serializeBinary: function () {
            return empty_1.Empty.encode(this).finish();
        },
    },
    responseType: {
        deserializeBinary: function (data) {
            var value = exports.InviteGetResponse.decode(data);
            return __assign(__assign({}, value), { toObject: function () {
                    return value;
                } });
        },
    },
};
exports.HiddenSwitchSpellsourceAPIServiceLoginDesc = {
    methodName: "Login",
    service: exports.HiddenSwitchSpellsourceAPIServiceDesc,
    requestStream: false,
    responseStream: false,
    requestType: {
        serializeBinary: function () {
            return exports.LoginRequest.encode(this).finish();
        },
    },
    responseType: {
        deserializeBinary: function (data) {
            var value = exports.LoginResponse.decode(data);
            return __assign(__assign({}, value), { toObject: function () {
                    return value;
                } });
        },
    },
};
exports.HiddenSwitchSpellsourceAPIServicePostInviteDesc = {
    methodName: "PostInvite",
    service: exports.HiddenSwitchSpellsourceAPIServiceDesc,
    requestStream: false,
    responseStream: true,
    requestType: {
        serializeBinary: function () {
            return exports.PostInviteRequest.encode(this).finish();
        },
    },
    responseType: {
        deserializeBinary: function (data) {
            var value = exports.InviteResponse.decode(data);
            return __assign(__assign({}, value), { toObject: function () {
                    return value;
                } });
        },
    },
};
exports.HiddenSwitchSpellsourceAPIServicePostPasswordResetDesc = {
    methodName: "PostPasswordReset",
    service: exports.HiddenSwitchSpellsourceAPIServiceDesc,
    requestStream: false,
    responseStream: false,
    requestType: {
        serializeBinary: function () {
            return exports.PostPasswordResetRequest.encode(this).finish();
        },
    },
    responseType: {
        deserializeBinary: function (data) {
            var value = empty_1.Empty.decode(data);
            return __assign(__assign({}, value), { toObject: function () {
                    return value;
                } });
        },
    },
};
var GrpcWebImpl = /** @class */ (function () {
    function GrpcWebImpl(host, options) {
        this.host = host;
        this.options = options;
    }
    GrpcWebImpl.prototype.unary = function (methodDesc, _request, metadata) {
        var _this = this;
        var _a;
        var request = __assign(__assign({}, _request), methodDesc.requestType);
        var maybeCombinedMetadata = metadata && this.options.metadata ? new browser_headers_1.BrowserHeaders(__assign(__assign({}, (_a = this.options) === null || _a === void 0 ? void 0 : _a.metadata.headersMap), metadata === null || metadata === void 0 ? void 0 : metadata.headersMap)) : (metadata !== null && metadata !== void 0 ? metadata : this.options.metadata);
        return new Promise(function (resolve, reject) {
            var _a;
            grpc_web_1.grpc.unary(methodDesc, __assign(__assign({ request: request, host: _this.host, metadata: maybeCombinedMetadata !== null && maybeCombinedMetadata !== void 0 ? maybeCombinedMetadata : {} }, (_this.options.transport !== undefined ? { transport: _this.options.transport } : {})), { debug: (_a = _this.options.debug) !== null && _a !== void 0 ? _a : false, onEnd: function (response) {
                    if (response.status === grpc_web_1.grpc.Code.OK) {
                        resolve(response.message.toObject());
                    }
                    else {
                        var err = new GrpcWebError(response.statusMessage, response.status, response.trailers);
                        reject(err);
                    }
                } }));
        });
    };
    GrpcWebImpl.prototype.invoke = function (methodDesc, _request, metadata) {
        var _this = this;
        var _a, _b, _c;
        var upStreamCodes = (_a = this.options.upStreamRetryCodes) !== null && _a !== void 0 ? _a : [];
        var DEFAULT_TIMEOUT_TIME = 3000;
        var request = __assign(__assign({}, _request), methodDesc.requestType);
        var transport = (_b = this.options.streamingTransport) !== null && _b !== void 0 ? _b : this.options.transport;
        var maybeCombinedMetadata = metadata && this.options.metadata ? new browser_headers_1.BrowserHeaders(__assign(__assign({}, (_c = this.options) === null || _c === void 0 ? void 0 : _c.metadata.headersMap), metadata === null || metadata === void 0 ? void 0 : metadata.headersMap)) : (metadata !== null && metadata !== void 0 ? metadata : this.options.metadata);
        return new rxjs_1.Observable(function (observer) {
            var upStream = function () {
                var _a;
                var client = grpc_web_1.grpc.invoke(methodDesc, __assign(__assign({ host: _this.host, request: request }, (transport !== undefined ? { transport: transport } : {})), { metadata: maybeCombinedMetadata !== null && maybeCombinedMetadata !== void 0 ? maybeCombinedMetadata : {}, debug: (_a = _this.options.debug) !== null && _a !== void 0 ? _a : false, onMessage: function (next) { return observer.next(next); }, onEnd: function (code, message, trailers) {
                        if (code === 0) {
                            observer.complete();
                        }
                        else if (upStreamCodes.includes(code)) {
                            setTimeout(upStream, DEFAULT_TIMEOUT_TIME);
                        }
                        else {
                            var err = new Error(message);
                            err.code = code;
                            err.metadata = trailers;
                            observer.error(err);
                        }
                    } }));
                observer.add(function () { return client.close(); });
            };
            upStream();
        }).pipe((0, operators_1.share)());
    };
    return GrpcWebImpl;
}());
exports.GrpcWebImpl = GrpcWebImpl;
function longToNumber(long) {
    if (long.gt(globalThis.Number.MAX_SAFE_INTEGER)) {
        throw new globalThis.Error("Value is larger than Number.MAX_SAFE_INTEGER");
    }
    return long.toNumber();
}
if (minimal_1.default.util.Long !== long_1.default) {
    minimal_1.default.util.Long = long_1.default;
    minimal_1.default.configure();
}
var GrpcWebError = /** @class */ (function (_super) {
    __extends(GrpcWebError, _super);
    function GrpcWebError(message, code, metadata) {
        var _this = _super.call(this, message) || this;
        _this.code = code;
        _this.metadata = metadata;
        return _this;
    }
    return GrpcWebError;
}(globalThis.Error));
exports.GrpcWebError = GrpcWebError;
//# sourceMappingURL=spellsource.js.map