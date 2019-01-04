export const Versions = new Mongo.Collection('versions');
if (Meteor.isServer) {
    Versions._ensureIndex({createdAt: -1});
}