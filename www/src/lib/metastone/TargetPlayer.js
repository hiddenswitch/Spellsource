/**
 * Created by chris on 1/17/17.
 */
import EnumExtended from '../EnumExtended';

export default class TargetPlayer extends EnumExtended {
}

TargetPlayer.initEnum([
    'SELF',
    'OPPONENT',
    'BOTH',
    'OWNER',
    'ACTIVE',
    'INACTIVE'
]);