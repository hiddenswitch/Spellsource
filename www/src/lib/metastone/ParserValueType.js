/**
 * Created by bberman on 1/17/17.
 */
import EnumExtended from '../EnumExtended';

export default class ParserValueType extends EnumExtended {
}

ParserValueType.initEnum([
    'BOOLEAN',
    'INTEGER',
    'TARGET_SELECTION',
    'TARGET_REFERENCE',
    'TARGET_PLAYER',
    'RACE',
    'SPELL',
    'SPELL_ARRAY',
    'ATTRIBUTE',
    'PLAYER_ATTRIBUTE',
    'VALUE_PROVIDER',
    'ENTITY_FILTER',
    'ENTITY_FILTER_ARRAY',
    'STRING',
    'STRING_ARRAY',
    'BOARD_POSITION_RELATIVE',
    'CARD_LOCATION',
    'OPERATION',
    'ALGEBRAIC_OPERATION',
    'CONDITION',
    'CONDITION_ARRAY',
    'CARD_TYPE',
    'ENTITY_TYPE',
    'ACTION_TYPE',
    'TARGET_TYPE',
    'TRIGGER',
    'EVENT_TRIGGER',
    'CARD_COST_MODIFIER',
    'RARITY',
    'HERO_CLASS',
    'HERO_CLASS_ARRAY',
    'VALUE',
    'CARD_DESC_TYPE',
    'CARD_SOURCE'
]);