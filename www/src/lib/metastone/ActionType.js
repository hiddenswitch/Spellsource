/**
 * Created by chris on 1/17/17.
 */
import EnumExtended from '../EnumExtended';

export default class ActionType extends EnumExtended {
}

ActionType.initEnum([
    'SYSTEM',
    'END_TURN',
    'PHYSICAL_ATTACK',
    'SPELL',
    'SUMMON',
    'HERO_POWER',
    'BATTLECRY',
    'EQUIP_WEAPON',
    'DISCOVER',
]);