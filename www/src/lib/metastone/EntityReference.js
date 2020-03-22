/**
 * Created by chris on 1/18/17.
 */
import EnumExtended from '../EnumExtended';

export default class EntityReference extends EnumExtended {
}

EntityReference.initEnum([
    'NONE',
    'ENEMY_CHARACTERS',
    'ENEMY_MINIONS',
    'ENEMY_HERO',
    'FRIENDLY_CHARACTERS',
    'FRIENDLY_MINIONS',
    'OTHER_FRIENDLY_MINIONS',
    'ADJACENT_MINIONS',
    'FRIENDLY_HERO',
    'ALL_MINIONS',
    'ALL_CHARACTERS',
    'ALL_OTHER_CHARACTERS',
    'ALL_OTHER_MINIONS',
    'FRIENDLY_WEAPON',
    'ENEMY_WEAPON',
    'FRIENDLY_HAND',
    'ENEMY_HAND',
    'OPPOSITE_MINIONS',
    'LEFTMOST_FRIENDLY_MINION',
    'LEFTMOST_ENEMY_MINION',
    'FRIENDLY_PLAYER',
    'ENEMY_PLAYER',
    'MINIONS_TO_LEFT',
    'MINIONS_TO_RIGHT',
    'TARGET',
    'SPELL_TARGET',
    'EVENT_TARGET',
    'SELF',
    'KILLED_MINION',
    'ATTACKER_REFERENCE',
    'PENDING_CARD',
    'EVENT_CARD'
]);