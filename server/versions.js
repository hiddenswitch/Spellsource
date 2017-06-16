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
//
// Meteor.startup(() => {
//     if (!Versions.findOne("test11")) {
//         Versions.insert({_id: "test11", url: "https://s3.us-east-2.amazonaws.com/minionate/builds/latest_mac.zip", createdAt: new Date()});
//     }
// });