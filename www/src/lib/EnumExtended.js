/**
 * Created by bberman on 1/17/17.
 */
import {Enum} from 'enumify';

export default class EnumExtended extends Enum {
    static toBlocklyArray() {
        return _.map(this.enumValues, (value) => {
            const string = value.toString().replace(/^Symbol\(/, '').replace(/\)$/, '');
            return [string, string]
        })
    }
}