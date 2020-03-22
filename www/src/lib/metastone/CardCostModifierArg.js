/**
 * Created by chris on 1/18/17.
 */
import EnumExtended from '../EnumExtended';

export default class CardCostModifierArg extends EnumExtended {
}

CardCostModifierArg.initEnum([
    'CLASS',
    'CARD_TYPE',
    'REQUIRED_ATTRIBUTE',
    'EXPIRATION_TRIGGER',
    'MIN_VALUE',
    'VALUE',
    'RACE',
    'TARGET_PLAYER',
    'TOGGLE_ON_TRIGGER',
    'TOGGLE_OFF_TRIGGER',
    'TARGET',
    'OPERATION',

    // Internal use only
    'CARD_IDS',
]);