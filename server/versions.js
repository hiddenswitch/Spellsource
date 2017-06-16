/**
 * Created by bberman on 6/15/17.
 */
import {Versions} from '../collections'

SimpleRest.configure({
    collections: []
});

Meteor.publish('versions', function () {
    return Versions.find({}, {sort: {createdAt: -1}}, {limit: 2});
});

Meteor.methods({
    latest(url, apiKey) {
        if (apiKey !== Meteor.settings.apiKey) {
            return;
        }

        Versions.insert({url: url, createdAt: new Date()});
    }
});