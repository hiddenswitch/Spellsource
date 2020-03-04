/**
 * Created by chris on 1/17/17.
 */
import EnumExtended from '../EnumExtended';

export default class CardLocation extends EnumExtended {
}

CardLocation.initEnum([
    'HAS',
    'EQUAL',
    'LESS',
    'LESS_OR_EQUAL',
    'GREATER',
    'GREATER_OR_EQUAL'
]);