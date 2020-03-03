/**
 * Created by chris on 1/17/17.
 */
import EnumExtended from '../EnumExtended';

export default class TargetSelection extends EnumExtended {
}

TargetSelection.initEnum([
    'NONE',
    'AUTO',
    'ANY',
    'MINIONS',
    'ENEMY_CHARACTERS',
    'FRIENDLY_CHARACTERS',
    'ENEMY_MINIONS',
    'FRIENDLY_MINIONS',
    'HEROES',
    'ENEMY_HERO',
    'FRIENDLY_HERO'
]);