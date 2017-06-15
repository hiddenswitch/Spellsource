import {Versions} from '../collections';
let LocalVersions = new Ground.Collection('localVersions');

Meteor.startup(() => {
    // On startup, clear all the local versions' that we might be downloading.
    LocalVersions.update({}, {$set: {downloading: false}}, {multi: true});
    // Delete all older versions
    let paths = LocalVersions.find({}, {sort: {createdAt: -1}, skip: 1}).fetch();

    if (paths.length > 0) {
        Electron.remove(paths);
    }

    // Get the latest version information.
    Meteor.subscribe('versions');
});

let downloadProgress = new ReactiveVar(0);

let state = () => {
    let localVersion = LocalVersions.find({}, {sort: {createdAt: -1}}).fetch()[0];

    // Get the latest version
    let serverVersion = Versions.find({}, {sort: {createdAt: -1}}).fetch()[0];

    if (serverVersion) {
        // We need to download
        if (!localVersion
            || localVersion._id !== serverVersion._id) {
            return {
                download: true,
                text: 'Download',
                serverVersion: serverVersion
            };
        } else if (localVersion) {
            // We are downloading
            if (localVersion.downloading) {
                return {downloading: true, text: 'Downloading', disabled: true};
            } else {
                // We're ready to go
                return {ready: true, text: 'Launch', path: localVersion.path};
            }
        }
    } else {
        return {updating: true, text: 'Updating', disabled: true};
    }
};


Template.launcher.helpers({
    state() {
        return state();
    },
    progress() {
        return downloadProgress.get() * 100;
    }
});

Template.launcher.events({
    'click #launch-button': () => {
        let currentState = state();


        // If we can download a new version, do a download
        if (currentState.download) {
            let versionId = currentState.serverVersion._id;
            let destination = Electron.app().getAppPath('userData') + '/' + versionId;
            // Construct a name for the build.
            let buildName = Meteor.settings.public.buildName[Electron.isWindows() ? 'exe' : 'mac'];
            let path = "file://" + destination + "/" + buildName;

            if (LocalVersions.findOne(versionId)) {
                LocalVersions.update(versionId, {
                    $set: _.extend(currentState.serverVersion, {
                        downloading: true,
                        ready: false
                    })
                });
            } else {
                // Save the server version document with this extra metadata.
                LocalVersions.insert(_.extend(currentState.serverVersion, {downloading: true, ready: false}));
            }

            // Initiate a download.
            Electron.download({
                url: currentState.serverVersion.url,
                destination: destination,
                chmodTarget: destination + '/' + buildName + '/Contents/MacOS/Minionate'
            }, (response) => {
                console.log(response);
                if (response.state) {
                    downloadProgress.set(response.state.percent);
                } else if (response.extracted) {
                    downloadProgress.set(1);
                    // Once it's extracted, set the path and mark the document as ready
                    LocalVersions.update(versionId, {$set: {ready: true, downloading: false, path: path}});
                }
            });
        } else if (currentState.downloading) {
            // If we're currently downloading, this is disabled.
        } else if (currentState.ready) {
            // Open the executable as directed
            Electron.openExternal(currentState.path);
        } else if (currentState.updating) {
            // If this is updating, this is disabled.
        }
    }
});