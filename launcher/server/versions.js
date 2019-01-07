/**
 * Created by bberman on 6/15/17.
 */
import {Versions} from '../collections'

const crypto = require('crypto');
SimpleRest.configure({
    collections: []
});

Meteor.publish('versions', function () {
    return Versions.find({}, {sort: {createdAt: -1}}, {limit: 2});
});

Meteor.methods({
    latest(url, buildName, platform, apiKey) {
        const hash = crypto.createHash('sha512');
        hash.update(Meteor.settings.apiKey + ':' + apiKey);

        if (hash.digest('hex') !== Meteor.settings.apiKeyVerify) {
            throw new Meteor.Error(401, 'Permission denied.');
        }
        let windows = platform === 'windows';

        let buildNameObject = {
            "exe": buildName + ".exe",
            "mac": buildName + ".app"
        };

        Versions.insert({
            url: url, createdAt: new Date(), windows: windows,
            buildName: buildNameObject
        });

    }
});