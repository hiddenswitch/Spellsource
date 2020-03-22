/**
 * Created by chris on 1/17/17.
 */
import EnumExtended from '../EnumExtended';

export default class TargetType extends EnumExtended {
}

TargetType.initEnum([
    'IGNORE_AS_TARGET',
    'IGNORE_AS_SOURCE',
    'IGNORE_OTHER_SOURCES',
    'IGNORE_OTHER_TARGETS',
]);