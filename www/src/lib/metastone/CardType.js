/**
 * Created by chris on 1/17/17.
 */
import EnumExtended from '../EnumExtended';

export default class CardType extends EnumExtended {
}

CardType.initEnum([
    'HERO',
    'MINION',
    'SPELL',
    'WEAPON',
    'HERO_POWER',
    'CHOOSE_ONE'
]);