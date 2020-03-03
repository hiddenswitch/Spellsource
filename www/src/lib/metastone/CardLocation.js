/**
 * Created by chris on 1/17/17.
 */
import EnumExtended from '../EnumExtended';

export default class CardLocation extends EnumExtended {
}

CardLocation.initEnum([
    'NONE',
    'GRAVEYARD',
    'HAND',
    'DECK',
    'SET_ASIDE_ZONE',
    'HERO_POWER',
    'PENDING',
    'EVENT',
    'CATALOGUE'
]);