Meteor.startup(() => {
    if (Electron.isDesktop()) {
        // Only run on desktop
    }
});