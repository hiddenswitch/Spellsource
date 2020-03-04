/**
 * Created by bberman on 1/17/17.
 */
import EnumExtended from '../EnumExtended';

export default class HeroClass extends EnumExtended {
}

HeroClass.initEnum([
    'ANY',
    'DECK_COLLECTION',
    'NEUTRAL',
    'DRUID',
    'HUNTER',
    'MAGE',
    'PALADIN',
    'PRIEST',
    'ROGUE',
    'SHAMAN',
    'WARLOCK',
    'WARRIOR',
    'SELF',
    'OPPONENT',
    'BOSS'
]);