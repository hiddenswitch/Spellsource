/**
 * Created by chris on 1/17/17.
 */
import EnumExtended from '../EnumExtended';

export default class PlayerAttribute extends EnumExtended {
}

PlayerAttribute.initEnum([
    'MANA',
    'MAX_MANA',
    'HAND_COUNT',
    'HERO_POWER_USED',
    'DECK_COUNT',
    'LAST_MANA_COST',
    'SECRET_COUNT',
    'SPELLS_CAST',
]);