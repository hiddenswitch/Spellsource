import {Versions} from '../collections';
let LocalVersions = new Ground.Collection('localVersions');

Tracker.autorun(() => {
    // For every local version that claims it is ready, check if we have the actual file directory
    LocalVersions.find({ready: true}).forEach(function (version) {
        let versionId = version._id;
        let destination = Electron.app().getPath('userData') + '/' + versionId;
        // Construct a name for the build.
        let buildName = Meteor.settings.public.buildName[Electron.isWindows() ? 'exe' : 'mac'];
        let executablePath = destination + "/" + buildName;
        Electron.exists(executablePath, function (doesExist) {
            if (!doesExist) {
                LocalVersions.update({_id: versionId}, {$set: {ready: false, downloading: false}});
            }
        });
    });
});

Meteor.startup(() => {
    // On startup, clear all the local versions' that we might be downloading.
    LocalVersions.update({}, {$set: {downloading: false}}, {multi: true});

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
            } else if (localVersion.ready) {
                // We're ready to go
                return {ready: true, text: 'Launch', path: localVersion.path};
            } else {
                return {
                    download: true,
                    text: 'Download',
                    serverVersion: serverVersion
                };
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
    },
    connected() {
        return Meteor.status().connected;
    }
});

let deleteOldVersions = () => {
    // Delete all older versions
    let paths = _.compact(_.map(LocalVersions.find({}, {
        sort: {createdAt: -1},
        skip: 1
    }).fetch(), (version) => {
        if (_.isUndefined(version.path)) {
            LocalVersions.remove(version._id);
        } else {
            return version.path.replace('file://', '');
        }
    }));

    if (paths.length > 0) {
        Electron.remove(paths);
    }
};

Template.launcher.events({
    'click #launch-button': () => {
        let currentState = state();


        // If we can download a new version, do a download
        if (currentState.download) {
            let versionId = currentState.serverVersion._id;
            let destination = Electron.app().getPath('userData') + '/' + versionId;
            // Construct a name for the build.
            let buildName = Meteor.settings.public.buildName[Electron.isWindows() ? 'exe' : 'mac'];
            let executablePath = destination + "/" + buildName;
            let urlPath = "file://" + executablePath;

            if (LocalVersions.findOne(versionId)) {
                LocalVersions.update(versionId, {
                    $set: _.extend(_.omit(currentState.serverVersion, '_id'), {
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
                if (response.state) {
                    downloadProgress.set(response.state.percent);
                } else if (response.extracted) {
                    downloadProgress.set(1);
                    // Once it's extracted, set the urlPath and mark the document as ready
                    LocalVersions.update(versionId, {$set: {ready: true, downloading: false, path: urlPath}});

                    deleteOldVersions();
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