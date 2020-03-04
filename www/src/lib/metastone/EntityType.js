/**
 * Created by chris on 1/17/17.
 */
import EnumExtended from '../EnumExtended';

export default class EntityType extends EnumExtended {
}

EntityType.initEnum([
    'ANY',
    'HERO',
    'MINION',
    'WEAPON',
    'CARD',
    'PLAYER',
]);