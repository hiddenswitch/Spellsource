/* tslint:disable */
/* eslint-disable */

// Generated using typescript-generator version 3.1.1185 on 2023-03-20 14:57:30.

export interface Card extends Entity, HasChooseOneActions {
  desc: CardDesc;
}

export interface CardCatalogue {
}

export interface CardCatalogueRecord extends Serializable {
  id: string;
  desc: CardDesc;
}

export interface CardParseException extends RuntimeException {
}

export interface CardParser {
}

export interface CardSet {
}

export interface HasChooseOneActions {
}

export interface HasDeathrattleEnchantments {
}

export interface CardCostModifier extends Enchantment, HasDesc<{ [P in CardCostModifierArg]?: any }> {
  targetReference: EntityReference;
  condition: Condition;
  desc: { [P in CardCostModifierArg]?: any };
}

export interface OneTurnCostModifier extends CardCostModifier {
}

export interface ToggleCostModifier extends CardCostModifier {
  toggleOnTrigger: EventTrigger;
  toggleOffTrigger: EventTrigger;
  ready: boolean;
}

export interface AuraDescDeserializer extends DescDeserializer<{ [P in AuraArg]?: any }, AuraArg, Aura> {
}

export interface BattlefieldEnchantmentDescConverter extends StdConverter<EnchantmentDesc, EnchantmentDesc> {
}

export interface CardCostModifierDescDeserializer extends DescDeserializer<{ [P in CardCostModifierArg]?: any }, CardCostModifierArg, CardCostModifier> {
}

export interface CardDesc extends Serializable, Cloneable, HasEntrySet<CardDescArg, any>, AbstractEnchantmentDesc<Enchantment> {
  id: string;
  name: string;
  heroPower: string;
  baseManaCost: number;
  type: CardType;
  heroClass: string;
  heroClasses: string[];
  baseAttack: number;
  baseHp: number;
  damage: number;
  durability: number;
  rarity: Rarity;
  race: string;
  description: string;
  targetSelection: TargetSelection;
  secret: { [P in EventTriggerArg]?: any };
  quest: { [P in EventTriggerArg]?: any };
  countUntilCast: number;
  countByValue: boolean;
  battlecry: OpenerDesc;
  deathrattle: { [P in SpellArg]?: any };
  trigger: EnchantmentDesc;
  triggers: EnchantmentDesc[];
  aura: { [P in AuraArg]?: any };
  auras: { [P in AuraArg]?: any }[];
  cardCostModifier: { [P in CardCostModifierArg]?: any };
  chooseOneBattlecries: OpenerDesc[];
  chooseBothBattlecry: OpenerDesc;
  chooseOneCardIds: string[];
  chooseBothCardId: string;
  onEquip: { [P in SpellArg]?: any };
  onUnequip: { [P in SpellArg]?: any };
  spell: { [P in SpellArg]?: any };
  condition: { [P in ConditionArg]?: any };
  group: { [P in SpellArg]?: any }[];
  passiveTrigger: EnchantmentDesc;
  passiveTriggers: EnchantmentDesc[];
  deckTrigger: EnchantmentDesc;
  deckTriggers: EnchantmentDesc[];
  gameTriggers: EnchantmentDesc[];
  manaCostModifier: { [P in ValueProviderArg]?: any };
  attributes: { [P in Attribute]?: any };
  author: string;
  flavor: string;
  wiki: string;
  collectible: boolean;
  set: string;
  sets: string[];
  fileFormatVersion: number;
  dynamicDescription: { [P in DynamicDescriptionArg]?: any }[];
  legacy: boolean;
  hero: string;
  secondPlayerBonusCards: string[];
  targetSelectionOverride: TargetSelection;
  targetSelectionCondition: { [P in ConditionArg]?: any };
  art: Art;
  tooltips: Tooltip[];
}

export interface CardSourceDescDeserializer extends DescDeserializer<{ [P in CardSourceArg]?: any }, CardSourceArg, CardSource> {
}

export interface ConditionDescDeserializer extends DescDeserializer<{ [P in ConditionArg]?: any }, ConditionArg, Condition> {
}

export interface DeckEnchantmentDescConverter extends StdConverter<EnchantmentDesc, EnchantmentDesc> {
}

export interface DescDeserializer<T, K, V> extends StdDeserializer<T> {
  interpreter: { [index: string]: ParseValueType };
}

export interface SerializationContext {
}

export interface DescSerializer extends StdSerializer<{ [index: string]: any }> {
}

export interface EnchantmentSerializer extends StdSerializer<Enchantment> {
}

export interface EntityFilterDescDeserializer extends DescDeserializer<{ [P in EntityFilterArg]?: any }, EntityFilterArg, EntityFilter> {
}

export interface EntityReferenceSerializer extends StdSerializer<EntityReference> {
}

export interface EventTriggerDescDeserializer extends DescDeserializer<{ [P in EventTriggerArg]?: any }, EventTriggerArg, EventTrigger> {
}

export interface GameEnchantmentDescConverter extends StdConverter<EnchantmentDesc, EnchantmentDesc> {
}

export interface HasDesc<T> {
}

export interface HasDescSerializer extends StdSerializer<HasDesc<any>> {
}

export interface HasEntrySet<T, V> {
}

export interface BfsNode<T, V> {
  key: T;
  value: V;
  parent: BfsNode<T, V>;
  depth: number;
}

export interface ParseUtils {
}

export interface PassiveEnchantmentDescConverter extends StdConverter<EnchantmentDesc, EnchantmentDesc> {
}

export interface SpellDescDeserializer extends DescDeserializer<{ [P in SpellArg]?: any }, SpellArg, Spell> {
}

export interface ValueProviderDescDeserializer extends DescDeserializer<{ [P in ValueProviderArg]?: any }, ValueProviderArg, ValueProvider> {
}

export interface ConditionalDescription extends DynamicDescription {
}

export interface DynamicDescription extends CustomCloneable, HasDesc<{ [P in DynamicDescriptionArg]?: any }> {
  desc: { [P in DynamicDescriptionArg]?: any };
}

export interface DynamicDescriptionDeserializer extends DescDeserializer<{ [P in DynamicDescriptionArg]?: any }, DynamicDescriptionArg, DynamicDescription> {
}

export interface GatekeeperShaValueDescription extends PluralDescription {
}

export interface MetaDescription extends DynamicDescription {
}

export interface PluralDescription extends ValueDescription {
}

export interface StringDescription extends DynamicDescription {
}

export interface ValueDescription extends DynamicDescription {
}

export interface Entity extends CustomCloneable, Serializable, HasCard, Comparable<Entity> {
  name: string;
  attributes: { [P in Attribute]?: any };
  sourceCard: Card;
  effectSource: Entity;
  id: number;
  ownerIndex: number;
  entityLocation: EntityLocation;
}

export interface EntityLocation extends Serializable {
  zone: Zones;
  player: number;
  index: number;
}

export interface Serializable {
}

export interface Throwable extends Serializable {
  detailMessage: string;
  cause: Throwable;
  stackTrace: StackTraceElement[];
  suppressedExceptions: Throwable[];
}

export interface RuntimeException extends Exception {
}

export interface EntityReference extends Serializable {
  id: number;
}

export interface Condition extends Serializable, HasDesc<{ [P in ConditionArg]?: any }> {
  desc: { [P in ConditionArg]?: any };
}

export interface Enchantment extends Entity, Trigger {
  entrySet: HasEntrySet<any, any>;
  triggers: EventTrigger[];
  expirationTriggers: EventTrigger[];
  activationTriggers: EventTrigger[];
  spell: { [P in SpellArg]?: any };
  hostReference: EntityReference;
  oneTurn: boolean;
  activated: boolean;
  expired: boolean;
  persistentOwner: boolean;
  maxFires: number;
  fires: number;
  keepAfterTransform: boolean;
  countUntilCast: number;
  countByValue: boolean;
  usesSpellTrigger: boolean;
  maxFiresPerSequence: number;
  firesThisSequence: number;
  copyToActor: boolean;
  zones: Zones[];
  added: boolean;
}

export interface EventTrigger extends CustomCloneable, Serializable, HasDesc<{ [P in EventTriggerArg]?: any }> {
  desc: { [P in EventTriggerArg]?: any };
}

export interface JavaType extends ResolvedType, Serializable, Type {
  _class: Class<any>;
  _hash: number;
  _valueHandler: any;
  _typeHandler: any;
  _asStatic: boolean;
}

export interface OpenerDesc extends Serializable, HasEntrySet<BattlecryDescArg, any>, Cloneable, AbstractEnchantmentDesc<Opener> {
  spell: { [P in SpellArg]?: any };
  targetSelection: TargetSelection;
  condition: { [P in ConditionArg]?: any };
  name: string;
  description: string;
  targetSelectionOverride: TargetSelection;
  targetSelectionCondition: { [P in ConditionArg]?: any };
}

export interface EnchantmentDesc extends Serializable, Cloneable, HasEntrySet<EnchantmentDescArg, any>, AbstractEnchantmentDesc<Enchantment> {
  eventTrigger: { [P in EventTriggerArg]?: any };
  spell: { [P in SpellArg]?: any };
  oneTurn: boolean;
  persistentOwner: boolean;
  keepAfterTransform: boolean;
  maxFires: number;
  maxFiresPerSequence: number;
  countUntilCast: number;
  countByValue: boolean;
  activationTriggers: { [P in EventTriggerArg]?: any }[];
  expirationTriggers: { [P in EventTriggerArg]?: any }[];
  name: string;
  description: string;
  zones: Zones[];
}

export interface Art extends GeneratedMessageV3, ArtOrBuilder {
  body_: Font;
  highlight_: Color;
  loop_: Prefab;
  missile_: Prefab;
  onCast_: Prefab;
  onHit_: Prefab;
  primary_: Color;
  secondary_: Color;
  shadow_: Color;
  spell_: Prefab;
  sprite_: Sprite;
  spriteShadow_: Sprite;
  memoizedIsInitialized: number;
}

export interface Cloneable {
}

export interface CustomCloneable extends Cloneable, Serializable {
}

export interface HasCard {
}

export interface StackTraceElement extends Serializable {
  classLoaderName: string;
  moduleName: string;
  moduleVersion: string;
  declaringClass: string;
  methodName: string;
  fileName: string;
  lineNumber: number;
  format: number;
}

export interface Exception extends Throwable {
}

export interface Trigger extends Serializable {
}

export interface Class<T> extends Serializable, GenericDeclaration, Type, AnnotatedElement, OfField<Class<any>>, Constable {
  componentType: Class<any>;
}

export interface ResolvedType {
}

export interface Type {
}

export interface Aura extends Enchantment, HasDesc<{ [P in AuraArg]?: any }> {
  affectedEntities: number[];
  desc: { [P in AuraArg]?: any };
}

export interface StdConverter<IN, OUT> extends Converter<IN, OUT> {
}

export interface Font extends GeneratedMessageV3, FontOrBuilder {
  vertex_: Color;
  memoizedIsInitialized: number;
}

export interface Color extends GeneratedMessageV3, ColorOrBuilder {
  a_: number;
  b_: number;
  g_: number;
  r_: number;
  memoizedIsInitialized: number;
}

export interface Prefab extends GeneratedMessageV3, PrefabOrBuilder {
  named_: any;
  memoizedIsInitialized: number;
}

export interface Sprite extends GeneratedMessageV3, SpriteOrBuilder {
  named_: any;
  pivot_: number;
  memoizedIsInitialized: number;
}

export interface UnknownFieldSet extends MessageLite {
  fields: { [index: string]: Field };
}

export interface GeneratedMessageV3 extends AbstractMessage, Serializable {
  unknownFields: UnknownFieldSet;
}

export interface ArtOrBuilder extends MessageOrBuilder {
}

export interface Tooltip extends GeneratedMessageV3, TooltipOrBuilder {
  keywords_: string[];
  text_: any;
  memoizedIsInitialized: number;
}

export interface AbstractEnchantmentDesc<T> {
}

export interface CardSource extends Serializable, HasDesc<{ [P in CardSourceArg]?: any }> {
  desc: { [P in CardSourceArg]?: any };
}

export interface StdDeserializer<T> extends JsonDeserializer<T>, Serializable, Gettable {
  _valueClass: Class<any>;
  _valueType: JavaType;
}

export interface StdSerializer<T> extends JsonSerializer<T>, JsonFormatVisitable, SchemaAware, Serializable {
  _handledType: Class<T>;
}

export interface EntityFilter extends Serializable, HasDesc<{ [P in EntityFilterArg]?: any }> {
  desc: { [P in EntityFilterArg]?: any };
}

export interface Spell extends Serializable, HasDesc<{ [P in SpellArg]?: any }> {
  desc: { [P in SpellArg]?: any };
}

export interface ValueProvider extends Serializable, HasDesc<{ [P in ValueProviderArg]?: any }> {
  desc: { [P in ValueProviderArg]?: any };
}

export interface Comparable<T> {
}

export interface GenericDeclaration extends AnnotatedElement {
}

export interface AnnotatedElement {
}

export interface Constable {
}

export interface Opener extends Enchantment {
  openerDesc: OpenerDesc;
}

export interface FontOrBuilder extends MessageOrBuilder {
}

export interface ColorOrBuilder extends MessageOrBuilder {
}

export interface PrefabOrBuilder extends MessageOrBuilder {
}

export interface SpriteOrBuilder extends MessageOrBuilder {
}

export interface MessageLite extends MessageLiteOrBuilder {
}

export interface AbstractMessage extends AbstractMessageLite<any, any>, Message {
  memoizedSize: number;
}

export interface MessageOrBuilder extends MessageLiteOrBuilder {
}

export interface TooltipOrBuilder extends MessageOrBuilder {
}

export interface Gettable {
}

export interface JsonFormatVisitable {
}

export interface SchemaAware {
}

export interface OfField<F> extends TypeDescriptor {
}

export interface Converter<IN, OUT> {
}

export interface Field {
  varint: number[];
  fixed32: number[];
  fixed64: number[];
  lengthDelimited: ByteString[];
  group: UnknownFieldSet[];
}

export interface MessageLiteOrBuilder {
}

export interface AbstractMessageLite<MessageType, BuilderType> extends MessageLite {
  memoizedHashCode: number;
}

export interface Message extends MessageLite, MessageOrBuilder {
}

export interface JsonDeserializer<T> extends NullValueProvider {
}

export interface JsonSerializer<T> extends JsonFormatVisitable {
}

export interface TypeDescriptor {
}

export interface NullValueProvider {
}

export interface ByteString extends Iterable<number>, Serializable {
  hash: number;
}

export interface Iterable<T> {
}

/**
 * Values:
 * - `BASE_MANA_COST`
 * - `COSTS_HEALTH_INSTEAD_OF_MANA`
 * - `AURA_COSTS_HEALTH_INSTEAD_OF_MANA`
 * - `HP`
 * - `INDEX`
 * - `INDEX_FROM_END`
 * - `STARTING_INDEX`
 * - `ATTACK`
 * - `ATTACK_BONUS`
 * - `MAX_HP`
 * - `ARMOR`
 * - `TEMPORARY_ATTACK_BONUS`
 * - `HP_BONUS`
 * - `AURA_ATTACK_BONUS`
 * - `AURA_HP_BONUS`
 * - `AURA_IMMUNE`
 * - `AURA_CARD_ID`
 * - `BASE_HP`
 * - `BASE_ATTACK`
 * - `CONDITIONAL_ATTACK_BONUS`
 * - `RACE`
 * - `DESTROYED`
 * - `FATIGUE`
 * - `FROZEN`
 * - `ENRAGABLE`
 * - `SILENCED`
 * - `WINDFURY`
 * - `AURA_WINDFURY`
 * - `MEGA_WINDFURY`
 * - `UNLIMITED_ATTACKS`
 * - `TAUNT`
 * - `AURA_TAUNT`
 * - `CARD_TAUNT`
 * - `SPELL_DAMAGE`
 * - `AURA_SPELL_DAMAGE`
 * - `HEALING_BONUS`
 * - `ENEMY_HEALING_BONUS`
 * - `AURA_HEALING_BONUS`
 * - `AURA_ENEMY_HEALING_BONUS`
 * - `OPPONENT_SPELL_DAMAGE`
 * - `CHARGE`
 * - `AURA_CHARGE`
 * - `NUMBER_OF_ATTACKS`
 * - `EXTRA_ATTACKS`
 * - `MAX_ATTACKS`
 * - `ENRAGED`
 * - `BATTLECRY`
 * - `DOUBLE_BATTLECRIES` - @deprecated
 * - `DEATHRATTLES`
 * - `DOUBLE_DEATHRATTLES` - @deprecated
 * - `IMMUNE`
 * - `IMMUNE_WHILE_ATTACKING`
 * - `AURA_IMMUNE_WHILE_ATTACKING`
 * - `DIVINE_SHIELD`
 * - `STEALTH`
 * - `AURA_STEALTH`
 * - `SECRET`
 * - `COMBO`
 * - `OVERLOAD`
 * - `OVERLOADED_THIS_GAME`
 * - `CHOOSE_ONE`
 * - `CHOICE`
 * - `CHOICES`
 * - `BOTH_CHOOSE_ONE_OPTIONS` - @deprecated
 * - `SUMMONING_SICKNESS`
 * - `UNTARGETABLE_BY_SPELLS`
 * - `UNTARGETABLE_BY_OPPONENT_SPELLS`
 * - `AURA_UNTARGETABLE_BY_SPELLS`
 * - `SPELL_DAMAGE_MULTIPLIER`
 * - `SPELL_DAMAGE_AMPLIFY_MULTIPLIER`
 * - `HERO_POWER_DAMAGE_AMPLIFY_MULTIPLIER`
 * - `HEAL_AMPLIFY_MULTIPLIER`
 * - `SPELL_HEAL_AMPLIFY_MULTIPLIER`
 * - `HERO_POWER_HEAL_AMPLIFY_MULTIPLIER`
 * - `ATTACK_EQUALS_HP`
 * - `AURA_ATTACK_EQUALS_HP`
 * - `CANNOT_ATTACK`
 * - `AURA_CANNOT_ATTACK`
 * - `CANNOT_ATTACK_HEROES`
 * - `AURA_CANNOT_ATTACK_HEROES`
 * - `INVERT_HEALING`
 * - `CANNOT_REDUCE_HP_BELOW_1`
 * - `COUNTERED`
 * - `DIED_ON_TURN`
 * - `HERO_POWER_FREEZES_TARGET` - @deprecated
 * - `HERO_POWERS_DISABLED`
 * - `LAST_HIT`
 * - `LAST_HEAL`
 * - `PASSIVE_TRIGGERS`
 * - `DECK_TRIGGERS`
 * - `GAME_TRIGGERS`
 * - `HERO_POWER_USAGES`
 * - `HERO_POWER_DAMAGE`
 * - `SHADOWFORM`
 * - `CTHUN_ATTACK_BUFF`
 * - `CTHUN_HEALTH_BUFF`
 * - `CTHUN_TAUNT`
 * - `SPELLS_COST_HEALTH`
 * - `MINIONS_COST_HEALTH`
 * - `MURLOCS_COST_HEALTH`
 * - `TAKE_DOUBLE_DAMAGE`
 * - `AURA_TAKE_DOUBLE_DAMAGE`
 * - `RUSH`
 * - `AURA_RUSH`
 * - `JADE_BUFF`
 * - `RANDOM_CHOICES`
 * - `QUEST`
 * - `PACT`
 * - `STARTED_IN_DECK`
 * - `STARTED_IN_HAND`
 * - `PERMANENT`
 * - `USER_ID`
 * - `ENTITY_INSTANCE_ID`
 * - `CARD_INVENTORY_ID`
 * - `DECK_ID`
 * - `DONOR_ID`
 * - `CHAMPION_ID`
 * - `COLLECTION_IDS`
 * - `ALLIANCE_ID`
 * - `UNIQUE_CHAMPION_IDS_SIZE`
 * - `UNIQUE_CHAMPION_IDS`
 * - `LAST_MINION_DESTROYED_CARD_ID`
 * - `LAST_MINION_DESTROYED_INVENTORY_ID`
 * - `TOTAL_DAMAGE_DEALT`
 * - `TOTAL_KILLS`
 * - `TOTAL_DAMAGE_RECEIVED`
 * - `HEALING_THIS_TURN`
 * - `EXCESS_HEALING_THIS_TURN`
 * - `TOTAL_HP_INCREASES`
 * - `DAMAGE_THIS_TURN`
 * - `MINIONS_SUMMONED_THIS_TURN`
 * - `TOTAL_MINIONS_SUMMONED_THIS_TURN`
 * - `WEAKEST_ON_BATTLEFIELD_WHEN_DESTROYED_COUNT`
 * - `POISONOUS`
 * - `AURA_POISONOUS`
 * - `LIFESTEAL`
 * - `AURA_LIFESTEAL`
 * - `DOUBLE_END_TURN_TRIGGERS`
 * - `PLAYED_FROM_HAND_OR_DECK`
 * - `MANA_SPENT`
 * - `COPIED_FROM`
 * - `NAME`
 * - `DESCRIPTION`
 * - `SPELLSOURCE_NAME`
 * - `MANA_COST_MODIFIER`
 * - `USED_THIS_TURN`
 * - `MANA_SPENT_THIS_TURN`
 * - `HERO_CLASS`
 * - `TARGET_SELECTION`
 * - `CARD_ID`
 * - `EXTRA_TURN`
 * - `TURN_TIME`
 * - `TURN_START_TIME_MILLIS`
 * - `GAME_START_TIME_MILLIS`
 * - `DISCARDED`
 * - `GAME_STARTED`
 * - `TRANSFORM_REFERENCE`
 * - `RECEIVED_ON_TURN`
 * - `KEEPS_ENCHANTMENTS`
 * - `NEVER_MULLIGANS`
 * - `ECHO`
 * - `AURA_ECHO`
 * - `DEFLECT`
 * - `INVOKE`
 * - `AURA_INVOKE`
 * - `INVOKED`
 * - `REMOVES_SELF_AT_END_OF_TURN`
 * - `LAST_TURN`
 * - `AI_OPPONENT`
 * - `UNCENSORED`
 * - `MAGNETIC`
 * - `MAGNETS`
 * - `HAND_INDEX`
 * - `ROASTED`
 * - `SUPREMACY`
 * - `SPELLS_CAST_TWICE`
 * - `SPELLS_CAST_THRICE`
 * - `ATTACK_MULTIPLIER`
 * - `AURA_ATTACK_MULTIPLIER`
 * - `ATTACK_BONUS_MULTIPLIER`
 * - `AURA_ATTACK_BONUS_MULTIPLIER`
 * - `CANT_GAIN_ENCHANTMENTS`
 * - `FREEZES_PERMANENTLY`
 * - `STEALTH_FOR_TURNS`
 * - `CASTED_ON_FRIENDLY_MINION`
 * - `ATTACKS_THIS_GAME`
 * - `BEING_PLAYED`
 * - `QUICK_DRAW`
 * - `RESERVED_INTEGER_1`
 * - `RESERVED_INTEGER_2`
 * - `RESERVED_INTEGER_3`
 * - `RESERVED_INTEGER_4`
 * - `RESERVED_INTEGER_5`
 * - `RESERVED_BOOLEAN_1`
 * - `RESERVED_BOOLEAN_2`
 * - `RESERVED_BOOLEAN_3`
 * - `RESERVED_BOOLEAN_4`
 * - `RESERVED_BOOLEAN_5`
 * - `SUPREMACIES_THIS_GAME`
 * - `CHOICE_SOURCE`
 * - `DISABLE_FATIGUE`
 * - `TIMES_HEALED`
 * - `SUMMONED_ON_TURN`
 * - `SUMMONED_BY_PLAYER`
 * - `ATTACKS_THIS_TURN`
 * - `DEMONIC_FORM`
 * - `WITHER`
 * - `WITHERED`
 * - `SCHEME`
 * - `LACKEY`
 * - `DECAY`
 * - `AURA_DECAY`
 * - `TREANT`
 * - `DRAINED_THIS_TURN`
 * - `TOTAL_DRAINED`
 * - `DRAINED_LAST_TURN`
 * - `SURGE`
 * - `DYNAMIC_DESCRIPTION`
 * - `PASSIVE_AURAS`
 * - `CURSE`
 * - `DRAIN`
 * - `TOTAL_MINION_DAMAGE_DEALT_THIS_GAME`
 * - `ATTACKS_LAST_TURN`
 * - `DISCOVER`
 * - `ARTIFACT`
 * - `TOTAL_DAMAGE_DEALT_THIS_GAME`
 * - `SIGNATURE`
 * - `STARTING_TURN`
 * - `CASTS_WHEN_DRAWN`
 * - `EIDOLON_RACE`
 * - `AURA_MIN_ATTACK`
 * - `AFTERMATH_COUNT`
 * - `IMBUE`
 * - `STARTING_HAND_DRAWN`
 * - `DESTROYED_BY`
 */
export type Attribute =
  "BASE_MANA_COST"
  | "COSTS_HEALTH_INSTEAD_OF_MANA"
  | "AURA_COSTS_HEALTH_INSTEAD_OF_MANA"
  | "HP"
  | "INDEX"
  | "INDEX_FROM_END"
  | "STARTING_INDEX"
  | "ATTACK"
  | "ATTACK_BONUS"
  | "MAX_HP"
  | "ARMOR"
  | "TEMPORARY_ATTACK_BONUS"
  | "HP_BONUS"
  | "AURA_ATTACK_BONUS"
  | "AURA_HP_BONUS"
  | "AURA_IMMUNE"
  | "AURA_CARD_ID"
  | "BASE_HP"
  | "BASE_ATTACK"
  | "CONDITIONAL_ATTACK_BONUS"
  | "RACE"
  | "DESTROYED"
  | "FATIGUE"
  | "FROZEN"
  | "ENRAGABLE"
  | "SILENCED"
  | "WINDFURY"
  | "AURA_WINDFURY"
  | "MEGA_WINDFURY"
  | "UNLIMITED_ATTACKS"
  | "TAUNT"
  | "AURA_TAUNT"
  | "CARD_TAUNT"
  | "SPELL_DAMAGE"
  | "AURA_SPELL_DAMAGE"
  | "HEALING_BONUS"
  | "ENEMY_HEALING_BONUS"
  | "AURA_HEALING_BONUS"
  | "AURA_ENEMY_HEALING_BONUS"
  | "OPPONENT_SPELL_DAMAGE"
  | "CHARGE"
  | "AURA_CHARGE"
  | "NUMBER_OF_ATTACKS"
  | "EXTRA_ATTACKS"
  | "MAX_ATTACKS"
  | "ENRAGED"
  | "BATTLECRY"
  | "DOUBLE_BATTLECRIES"
  | "DEATHRATTLES"
  | "DOUBLE_DEATHRATTLES"
  | "IMMUNE"
  | "IMMUNE_WHILE_ATTACKING"
  | "AURA_IMMUNE_WHILE_ATTACKING"
  | "DIVINE_SHIELD"
  | "STEALTH"
  | "AURA_STEALTH"
  | "SECRET"
  | "COMBO"
  | "OVERLOAD"
  | "OVERLOADED_THIS_GAME"
  | "CHOOSE_ONE"
  | "CHOICE"
  | "CHOICES"
  | "BOTH_CHOOSE_ONE_OPTIONS"
  | "SUMMONING_SICKNESS"
  | "UNTARGETABLE_BY_SPELLS"
  | "UNTARGETABLE_BY_OPPONENT_SPELLS"
  | "AURA_UNTARGETABLE_BY_SPELLS"
  | "SPELL_DAMAGE_MULTIPLIER"
  | "SPELL_DAMAGE_AMPLIFY_MULTIPLIER"
  | "HERO_POWER_DAMAGE_AMPLIFY_MULTIPLIER"
  | "HEAL_AMPLIFY_MULTIPLIER"
  | "SPELL_HEAL_AMPLIFY_MULTIPLIER"
  | "HERO_POWER_HEAL_AMPLIFY_MULTIPLIER"
  | "ATTACK_EQUALS_HP"
  | "AURA_ATTACK_EQUALS_HP"
  | "CANNOT_ATTACK"
  | "AURA_CANNOT_ATTACK"
  | "CANNOT_ATTACK_HEROES"
  | "AURA_CANNOT_ATTACK_HEROES"
  | "INVERT_HEALING"
  | "CANNOT_REDUCE_HP_BELOW_1"
  | "COUNTERED"
  | "DIED_ON_TURN"
  | "HERO_POWER_FREEZES_TARGET"
  | "HERO_POWERS_DISABLED"
  | "LAST_HIT"
  | "LAST_HEAL"
  | "PASSIVE_TRIGGERS"
  | "DECK_TRIGGERS"
  | "GAME_TRIGGERS"
  | "HERO_POWER_USAGES"
  | "HERO_POWER_DAMAGE"
  | "SHADOWFORM"
  | "CTHUN_ATTACK_BUFF"
  | "CTHUN_HEALTH_BUFF"
  | "CTHUN_TAUNT"
  | "SPELLS_COST_HEALTH"
  | "MINIONS_COST_HEALTH"
  | "MURLOCS_COST_HEALTH"
  | "TAKE_DOUBLE_DAMAGE"
  | "AURA_TAKE_DOUBLE_DAMAGE"
  | "RUSH"
  | "AURA_RUSH"
  | "JADE_BUFF"
  | "RANDOM_CHOICES"
  | "QUEST"
  | "PACT"
  | "STARTED_IN_DECK"
  | "STARTED_IN_HAND"
  | "PERMANENT"
  | "USER_ID"
  | "ENTITY_INSTANCE_ID"
  | "CARD_INVENTORY_ID"
  | "DECK_ID"
  | "DONOR_ID"
  | "CHAMPION_ID"
  | "COLLECTION_IDS"
  | "ALLIANCE_ID"
  | "UNIQUE_CHAMPION_IDS_SIZE"
  | "UNIQUE_CHAMPION_IDS"
  | "LAST_MINION_DESTROYED_CARD_ID"
  | "LAST_MINION_DESTROYED_INVENTORY_ID"
  | "TOTAL_DAMAGE_DEALT"
  | "TOTAL_KILLS"
  | "TOTAL_DAMAGE_RECEIVED"
  | "HEALING_THIS_TURN"
  | "EXCESS_HEALING_THIS_TURN"
  | "TOTAL_HP_INCREASES"
  | "DAMAGE_THIS_TURN"
  | "MINIONS_SUMMONED_THIS_TURN"
  | "TOTAL_MINIONS_SUMMONED_THIS_TURN"
  | "WEAKEST_ON_BATTLEFIELD_WHEN_DESTROYED_COUNT"
  | "POISONOUS"
  | "AURA_POISONOUS"
  | "LIFESTEAL"
  | "AURA_LIFESTEAL"
  | "DOUBLE_END_TURN_TRIGGERS"
  | "PLAYED_FROM_HAND_OR_DECK"
  | "MANA_SPENT"
  | "COPIED_FROM"
  | "NAME"
  | "DESCRIPTION"
  | "SPELLSOURCE_NAME"
  | "MANA_COST_MODIFIER"
  | "USED_THIS_TURN"
  | "MANA_SPENT_THIS_TURN"
  | "HERO_CLASS"
  | "TARGET_SELECTION"
  | "CARD_ID"
  | "EXTRA_TURN"
  | "TURN_TIME"
  | "TURN_START_TIME_MILLIS"
  | "GAME_START_TIME_MILLIS"
  | "DISCARDED"
  | "GAME_STARTED"
  | "TRANSFORM_REFERENCE"
  | "RECEIVED_ON_TURN"
  | "KEEPS_ENCHANTMENTS"
  | "NEVER_MULLIGANS"
  | "ECHO"
  | "AURA_ECHO"
  | "DEFLECT"
  | "INVOKE"
  | "AURA_INVOKE"
  | "INVOKED"
  | "REMOVES_SELF_AT_END_OF_TURN"
  | "LAST_TURN"
  | "AI_OPPONENT"
  | "UNCENSORED"
  | "MAGNETIC"
  | "MAGNETS"
  | "HAND_INDEX"
  | "ROASTED"
  | "SUPREMACY"
  | "SPELLS_CAST_TWICE"
  | "SPELLS_CAST_THRICE"
  | "ATTACK_MULTIPLIER"
  | "AURA_ATTACK_MULTIPLIER"
  | "ATTACK_BONUS_MULTIPLIER"
  | "AURA_ATTACK_BONUS_MULTIPLIER"
  | "CANT_GAIN_ENCHANTMENTS"
  | "FREEZES_PERMANENTLY"
  | "STEALTH_FOR_TURNS"
  | "CASTED_ON_FRIENDLY_MINION"
  | "ATTACKS_THIS_GAME"
  | "BEING_PLAYED"
  | "QUICK_DRAW"
  | "RESERVED_INTEGER_1"
  | "RESERVED_INTEGER_2"
  | "RESERVED_INTEGER_3"
  | "RESERVED_INTEGER_4"
  | "RESERVED_INTEGER_5"
  | "RESERVED_BOOLEAN_1"
  | "RESERVED_BOOLEAN_2"
  | "RESERVED_BOOLEAN_3"
  | "RESERVED_BOOLEAN_4"
  | "RESERVED_BOOLEAN_5"
  | "SUPREMACIES_THIS_GAME"
  | "CHOICE_SOURCE"
  | "DISABLE_FATIGUE"
  | "TIMES_HEALED"
  | "SUMMONED_ON_TURN"
  | "SUMMONED_BY_PLAYER"
  | "ATTACKS_THIS_TURN"
  | "DEMONIC_FORM"
  | "WITHER"
  | "WITHERED"
  | "SCHEME"
  | "LACKEY"
  | "DECAY"
  | "AURA_DECAY"
  | "TREANT"
  | "DRAINED_THIS_TURN"
  | "TOTAL_DRAINED"
  | "DRAINED_LAST_TURN"
  | "SURGE"
  | "DYNAMIC_DESCRIPTION"
  | "PASSIVE_AURAS"
  | "CURSE"
  | "DRAIN"
  | "TOTAL_MINION_DAMAGE_DEALT_THIS_GAME"
  | "ATTACKS_LAST_TURN"
  | "DISCOVER"
  | "ARTIFACT"
  | "TOTAL_DAMAGE_DEALT_THIS_GAME"
  | "SIGNATURE"
  | "STARTING_TURN"
  | "CASTS_WHEN_DRAWN"
  | "EIDOLON_RACE"
  | "AURA_MIN_ATTACK"
  | "AFTERMATH_COUNT"
  | "IMBUE"
  | "STARTING_HAND_DRAWN"
  | "DESTROYED_BY";

export type CardDescType =
  "AURA"
  | "ATTACK"
  | "ATTRIBUTES"
  | "BASE_ATTACK"
  | "BASE_HP"
  | "BATTLECRY"
  | "BOTH_OPTIONS"
  | "CARD_TYPE"
  | "DEATHRATTLE"
  | "DECK_TRIGGER"
  | "DESCRIPTION"
  | "HERO_CLASS"
  | "MAX_HP"
  | "OPTIONS"
  | "PASSIVE_TRIGGER"
  | "RARITY"
  | "RACE"
  | "SPELL"
  | "TRIGGER";

export type ChooseOneOverride = "NONE" | "ALWAYS_FIRST" | "ALWAYS_SECOND" | "BOTH_COMBINED";

export type CardDescArg =
  "ID"
  | "NAME"
  | "HERO_POWER"
  | "BASE_MANA_COST"
  | "TYPE"
  | "HERO_CLASS"
  | "HERO_CLASSES"
  | "BASE_ATTACK"
  | "BASE_HP"
  | "DAMAGE"
  | "DURABILITY"
  | "RARITY"
  | "RACE"
  | "DESCRIPTION"
  | "TARGET_SELECTION"
  | "SECRET"
  | "QUEST"
  | "COUNT_UNTIL_CAST"
  | "COUNT_BY_VALUE"
  | "BATTLECRY"
  | "DEATHRATTLE"
  | "TRIGGERS"
  | "AURAS"
  | "CARD_COST_MODIFIERS"
  | "CHOOSE_ONE_BATTLECRIES"
  | "CHOOSE_BOTH_BATTLECRY"
  | "CHOOSE_ONE_CARD_IDS"
  | "CHOOSE_BOTH_CARD_ID"
  | "ON_EQUIP"
  | "ON_UNEQUIP"
  | "SPELL"
  | "CONDITION"
  | "GROUP"
  | "PASSIVE_TRIGGERS"
  | "DECK_TRIGGERS"
  | "GAME_TRIGGERS"
  | "MANA_COST_MODIFIER"
  | "ATTRIBUTES"
  | "AUTHOR"
  | "FLAVOR"
  | "WIKI"
  | "COLLECTIBLE"
  | "SETS"
  | "DYNAMIC_DESCRIPTION"
  | "LEGACY"
  | "HERO"
  | "COLOR"
  | "BLACK_TEXT"
  | "SECOND_PLAYER_BONUS_CARDS"
  | "TARGET_SELECTION_OVERRIDE"
  | "TARGET_SELECTION_CONDITION";

export type BfsEnum = "SELF";

export type ParseValueType =
  "BOOLEAN"
  | "INTEGER"
  | "TARGET_SELECTION"
  | "TARGET_REFERENCE"
  | "TARGET_PLAYER"
  | "SPELL"
  | "SPELL_ARRAY"
  | "ATTRIBUTE"
  | "PLAYER_ATTRIBUTE"
  | "VALUE_PROVIDER"
  | "ENTITY_FILTER"
  | "ENTITY_FILTER_ARRAY"
  | "STRING"
  | "STRING_ARRAY"
  | "BOARD_POSITION_RELATIVE"
  | "CARD_LOCATION"
  | "OPERATION"
  | "ALGEBRAIC_OPERATION"
  | "CONDITION"
  | "CONDITION_ARRAY"
  | "CARD_TYPE"
  | "ENTITY_TYPE"
  | "ACTION_TYPE"
  | "TARGET_TYPE"
  | "TRIGGER"
  | "EVENT_TRIGGER"
  | "CARD_COST_MODIFIER"
  | "RARITY"
  | "VALUE"
  | "CARD_DESC_TYPE"
  | "CARD_SOURCE"
  | "CARD_SOURCE_ARRAY"
  | "INTEGER_ARRAY"
  | "GAME_VALUE"
  | "TRIGGERS"
  | "QUEST"
  | "AURA"
  | "SECRET"
  | "CHOOSE_ONE_OVERRIDE"
  | "BATTLECRY"
  | "DYNAMIC_DESCRIPTION"
  | "EVENT_TRIGGER_ARRAY"
  | "ZONES"
  | "DYNAMIC_DESCRIPTION_ARRAY";

export type DynamicDescriptionArg =
  "CLASS"
  | "STRING"
  | "CONDITION"
  | "VALUE"
  | "DESCRIPTION1"
  | "DESCRIPTION2"
  | "DESCRIPTIONS";

export type CardType =
  "HERO"
  | "MINION"
  | "SPELL"
  | "WEAPON"
  | "HERO_POWER"
  | "GROUP"
  | "CHOOSE_ONE"
  | "ENCHANTMENT"
  | "CLASS"
  | "FORMAT"
  | "UNRECOGNIZED";

export type Rarity = "FREE" | "COMMON" | "RARE" | "EPIC" | "LEGENDARY" | "ALLIANCE" | "UNRECOGNIZED";

export type TargetSelection =
  "NONE"
  | "ANY"
  | "MINIONS"
  | "ENEMY_CHARACTERS"
  | "FRIENDLY_CHARACTERS"
  | "ENEMY_MINIONS"
  | "FRIENDLY_MINIONS"
  | "FRIENDLY_HERO_AND_MINIONS"
  | "HEROES"
  | "ENEMY_HERO"
  | "FRIENDLY_HERO";

export type Zones =
  "NONE"
  | "HAND"
  | "DECK"
  | "GRAVEYARD"
  | "BATTLEFIELD"
  | "SECRET"
  | "QUEST"
  | "HERO_POWER"
  | "HERO"
  | "WEAPON"
  | "SET_ASIDE_ZONE"
  | "HIDDEN"
  | "DISCOVER"
  | "REMOVED_FROM_PLAY"
  | "PLAYER"
  | "ENCHANTMENT"
  | "UNRECOGNIZED";

export type CardCostModifierArg =
  "CLASS"
  | "CARD_TYPE"
  | "REQUIRED_ATTRIBUTE"
  | "EXPIRATION_TRIGGER"
  | "EXPIRATION_TRIGGERS"
  | "MIN_VALUE"
  | "VALUE"
  | "RACE"
  | "TARGET_PLAYER"
  | "TOGGLE_ON_TRIGGER"
  | "TOGGLE_OFF_TRIGGER"
  | "TARGET"
  | "FILTER"
  | "OPERATION"
  | "CONDITION";

export type SpellArg =
  "CLASS"
  | "ARMOR_BONUS"
  | "ATTACK_BONUS"
  | "ATTRIBUTE"
  | "AURA"
  | "BOARD_POSITION_ABSOLUTE"
  | "BOARD_POSITION_RELATIVE"
  | "CANNOT_RECEIVE_OWNED"
  | "CARD"
  | "CARD_COST_MODIFIER"
  | "CARD_DESC_TYPE"
  | "CARD_FILTER"
  | "CARD_FILTERS"
  | "CARD_LOCATION"
  | "CARD_SOURCE"
  | "CARD_SOURCES"
  | "CARD_TYPE"
  | "CARDS"
  | "CONDITION"
  | "CONDITIONS"
  | "DESCRIPTION"
  | "EXCLUSIVE"
  | "FILTER"
  | "FULL_MANA_CRYSTALS"
  | "HOW_MANY"
  | "HP_BONUS"
  | "IGNORE_SPELL_DAMAGE"
  | "MANA"
  | "NAME"
  | "OPERATION"
  | "PACT"
  | "RACE"
  | "QUEST"
  | "RANDOM_TARGET"
  | "REVERT_TRIGGER"
  | "SECRET"
  | "SECOND_REVERT_TRIGGER"
  | "SECONDARY_NAME"
  | "SECONDARY_TARGET"
  | "SECONDARY_VALUE"
  | "SPELL"
  | "SPELL1"
  | "SPELL2"
  | "SPELLS"
  | "SUMMON_BASE_HP"
  | "SUMMON_BASE_ATTACK"
  | "SUMMON_WINDFURY"
  | "SUMMON_TAUNT"
  | "SUMMON_CHARGE"
  | "SUMMON_DIVINE_SHIELD"
  | "SUMMON_STEALTH"
  | "SUMMON_TRIGGERS"
  | "SUMMON_BATTLECRY"
  | "SUMMON_DEATHRATTLE"
  | "SUMMON_AURA"
  | "TARGET"
  | "SOURCE"
  | "TARGET_PLAYER"
  | "TARGET_SELECTION"
  | "TRIGGER"
  | "GROUP"
  | "TRIGGERS"
  | "VALUE"
  | "BATTLECRY"
  | "AFTERMATH_ID"
  | "ZONES";

/**
 * Values:
 * - `CLASS`
 * - `FILTER`
 * - `TARGET`
 * - `SECONDARY_TARGET`
 * - `ATTRIBUTE`
 * - `VALUE`
 * - `ATTACK_BONUS`
 * - `HP_BONUS`
 * - `APPLY_EFFECT`
 * - `PAY_EFFECT`
 * - `SECONDARY_TRIGGER` - @deprecated since 2
 * - `TRIGGERS`
 * - `ALWAYS_APPLY` - @deprecated since 2
 * - `REVERT_TRIGGER`
 * - `SPELL_CONDITION`
 * - `CONDITION`
 * - `CARD`
 * - `RACES`
 * - `CAN_AFFORD_CONDITION`
 * - `AMOUNT_OF_CURRENCY`
 * - `TARGET_SELECTION`
 * - `CHOOSE_ONE_OVERRIDE`
 * - `PERSISTENT_OWNER`
 * - `SECONDARY_FILTER`
 * - `REMOVE_EFFECT`
 * - `DESCRIPTION`
 * - `NAME`
 * - `SPELL`
 * - `ZONES`
 */
export type AuraArg =
  "CLASS"
  | "FILTER"
  | "TARGET"
  | "SECONDARY_TARGET"
  | "ATTRIBUTE"
  | "VALUE"
  | "ATTACK_BONUS"
  | "HP_BONUS"
  | "APPLY_EFFECT"
  | "PAY_EFFECT"
  | "SECONDARY_TRIGGER"
  | "TRIGGERS"
  | "ALWAYS_APPLY"
  | "REVERT_TRIGGER"
  | "SPELL_CONDITION"
  | "CONDITION"
  | "CARD"
  | "RACES"
  | "CAN_AFFORD_CONDITION"
  | "AMOUNT_OF_CURRENCY"
  | "TARGET_SELECTION"
  | "CHOOSE_ONE_OVERRIDE"
  | "PERSISTENT_OWNER"
  | "SECONDARY_FILTER"
  | "REMOVE_EFFECT"
  | "DESCRIPTION"
  | "NAME"
  | "SPELL"
  | "ZONES";

export type EventTriggerArg =
  "CLASS"
  | "TARGET_PLAYER"
  | "SOURCE_PLAYER"
  | "CARD_TYPE"
  | "SOURCE_TYPE"
  | "SOURCE_ENTITY_TYPE"
  | "TARGET_ENTITY_TYPE"
  | "RACE"
  | "ACTION_TYPE"
  | "HOST_TARGET_TYPE"
  | "REQUIRED_ATTRIBUTE"
  | "TARGET"
  | "FIRE_CONDITION"
  | "QUEUE_CONDITION"
  | "TARGET_SELECTION"
  | "VALUE";

export type ConditionArg =
  "CLASS"
  | "RACE"
  | "VALUE"
  | "VALUE1"
  | "VALUE2"
  | "TARGET"
  | "SECONDARY_TARGET"
  | "TARGET_PLAYER"
  | "OPERATION"
  | "ATTRIBUTE"
  | "CARD_TYPE"
  | "CONDITIONS"
  | "CARD"
  | "CARDS"
  | "INVERT"
  | "CARD_FILTER"
  | "FILTER"
  | "HERO_CLASS"
  | "DESCRIPTION"
  | "RARITY";

export type ValueProviderArg =
  "CLASS"
  | "TARGET"
  | "ATTRIBUTE"
  | "PLAYER_ATTRIBUTE"
  | "VALUE"
  | "OFFSET"
  | "MULTIPLIER"
  | "RACE"
  | "TARGET_PLAYER"
  | "IF_TRUE"
  | "IF_FALSE"
  | "CONDITION"
  | "FILTER"
  | "OPERATION"
  | "VALUE1"
  | "VALUE2"
  | "GAME_VALUE"
  | "MIN"
  | "MAX"
  | "CARD_SOURCE"
  | "CARD_FILTER"
  | "EVALUATE_ONCE";

export type CardSourceArg =
  "CLASS"
  | "HERO_CLASS"
  | "TARGET_PLAYER"
  | "SOURCE"
  | "INVERT"
  | "DISTINCT"
  | "FORMAT"
  | "COLLECTION_NAME"
  | "CARD_SOURCES"
  | "VALUE";

export type EntityFilterArg =
  "CLASS"
  | "TARGET_PLAYER"
  | "VALUE"
  | "RACE"
  | "OPERATION"
  | "ATTRIBUTE"
  | "CARD_TYPE"
  | "RARITY"
  | "MANA_COST"
  | "HERO_CLASS"
  | "HERO_CLASSES"
  | "CARD"
  | "CARDS"
  | "FILTERS"
  | "INVERT"
  | "CARD_SET"
  | "TARGET"
  | "SECONDARY_TARGET"
  | "TARGET_SELECTION"
  | "AND_CONDITION"
  | "SPELL"
  | "ENTITY_TYPE";

export type BattlecryDescArg =
  "TARGET_SELECTION"
  | "CONDITION"
  | "NAME"
  | "DESCRIPTION"
  | "SPELL"
  | "TARGET_SELECTION_OVERRIDE"
  | "TARGET_SELECTION_CONDITION";

export type EnchantmentDescArg =
  "SPELL"
  | "ONE_TURN"
  | "PERSISTENT_OWNER"
  | "MAX_FIRES"
  | "COUNT_UNTIL_CAST"
  | "COUNT_BY_VALUE"
  | "EVENT_TRIGGER"
  | "MAX_FIRES_PER_SEQUENCE"
  | "EXPIRATION_TRIGGERS"
  | "DESCRIPTION"
  | "NAME"
  | "ZONES"
  | "ACTIVATION_TRIGGERS";
