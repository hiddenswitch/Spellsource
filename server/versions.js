/**
 * Created by bberman on 6/15/17.
 */
import {Versions} from '../collections'

Meteor.publish('versions', function () {
    return Versions.find({});
});