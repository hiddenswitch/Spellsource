/**
 * Defines useful client-side functionality.
 */
Electron = {
    /**
     * @return {Boolean} `true` if the app is running in Electron, `false` otherwise.
     */
    isDesktop: function () {
        return /Electron/.test(navigator.userAgent);
    },

    /**
     * @return {Boolean} `true` if the app is running in Windows, `false` otherwise.
     */
    isWindows: function () {
        return /Windows NT/.test(navigator.userAgent);
    },


    // When the app is running in Electron, the following methods will be implemented by `preload.js`.
    // Stub them out in case the client tries to call them even when not running in Electron.

    /**
     * Open the given external protocol URL in the desktop's default manner. (For example, http(s):
     * URLs in the user's default browser.)
     *
     * @param {String} url - The URL to open.
     */
    openExternal: function () {
    },

    /**
     * Determines if the browser window is currently in fullscreen mode.
     *
     * "Fullscreen" here refers to the state triggered by toggling the native controls, not that
     * toggled by the HTML API.
     *
     * To detect when the browser window changes fullscreen state, observe the 'enter-full-screen'
     * and 'leave-full-screen' events using `onWindowEvent`.
     *
     * @return {Boolean} `true` if the browser window is in fullscreen mode, `false` otherwise.
     */
    isFullScreen: function () {
    },

    /**
     * Invokes _callback_ when the specified `BrowserWindow` event is fired.
     *
     * See https://github.com/atom/electron/blob/master/docs/api/browser-window.md#events for a list
     * of events.
     *
     * @param {String} event - The name of a `BrowserWindow` event.
     * @param {Function} callback - A function to invoke when `event` is triggered. Takes no arguments
     *   and returns no value.
     */
    onWindowEvent: function () {
    },

    /**
     * Download a file.
     * @param options Options document
     * @param options.url {String} The URL to download. Should be a ZIP file.
     * @param options.destination {String} The place to save the extracted documents.
     * @param callback A callback that has a state property that looks like this for progress:
     *
     {
         percent: 0.5,               // Overall percent (between 0 to 1)
         speed: 554732,              // The download speed in bytes/sec
         size: {
             total: 90044871,        // The total payload size in bytes
             transferred: 27610959   // The transferred payload size in bytes
         },
         time: {
             elapsed: 36.235,        // The total elapsed seconds since the start (3 decimals)
             remaining: 81.403       // The remaining seconds to finish (3 decimals)
         }
     }

     * Or, it has an error property when a download error occurred, or a downloaded property when the
     * file has downloaded; or an extracted property when everything is ready to go.
     */
    download: function (options, callback) {
    },

    /**
     * Removes the array of given paths
     * @param paths {String[]} An array of string paths.
     */
    remove: function (paths) {},

    /**
     * Calls the callback with true if the file or directory exists at the given path
     * @param path The path to check
     * @param callback A callback
     */
    exists: function (path, callback) {}
};

// Read `ElectronImplementation` from the window vs. doing `typeof ElectronImplementation` because
// Meteor will shadow it with a local variable in the latter case.
if (!_.isUndefined(window.ElectronImplementation)) {
    // The app is running in Electron. Merge the implementations from `preload.js`.
    _.extend(Electron, window.ElectronImplementation);
}
