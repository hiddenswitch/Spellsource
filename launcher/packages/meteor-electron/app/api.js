/* global ElectronImplementation:true */

/**
 * Since we've disabled Node integration in the browser window, we must selectively expose
 * main-process/Node modules via this script.
 *
 * @WARNING This file must take care not to leak the imported modules to the browser window!
 * In particular, do not save the following variables as properties of `ElectronImplementation`.
 * See https://github.com/atom/electron/issues/1753#issuecomment-104719851.
 */
var _ = require('underscore');
var {ipcRenderer: ipc, remote, shell} = require('electron');

/**
 * Defines methods with which to extend the `Electron` module defined in `client.js`.
 * This must be a global in order to escape the preload script and be available to `client.js`.
 */
ElectronImplementation = {
    /**
     * Open the given external protocol URL in the desktop's default manner. (For example, http(s):
     * URLs in the user's default browser.)
     *
     * @param {String} url - The URL to open.
     */
    openExternal: shell.openExternal,

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
        return remote.getCurrentWindow().isFullScreen();
    },

    app: function () {
        return remote.app;
    },

    /**
     * Invokes _callback_ when the specified `BrowserWindow` event is fired.
     *
     * This differs from `onEvent` in that it directs Electron to start emitting the relevant window
     * event.
     *
     * See https://github.com/atom/electron/blob/master/docs/api/browser-window.md#events for a list
     * of events.
     *
     * The implementation of this API, in particular the use of the `ipc` vs. `remote` modules, is
     * designed to avoid memory leaks as described by
     * https://github.com/atom/electron/blob/master/docs/api/remote.md#passing-callbacks-to-the-main-process.
     *
     * @param {String} event - The name of a `BrowserWindow` event.
     * @param {Function} callback - A function to invoke when `event` is triggered. Takes no arguments
     *   and returns no value.
     */
    onWindowEvent: function (event, callback) {
        this.onEvent(event, callback);
        ipc.send('observe-window-event', event);
    },

    /**
     * Invokes _callback_ when the specified IPC event is fired.
     *
     * @param {String} event - The name of an event.
     * @param {Function} callback - A function to invoke when `event` is triggered. Takes no arguments
     *   and returns no value.
     */
    onEvent: function (event, callback) {
        var listeners = this._eventListeners[event];
        if (!listeners) {
            listeners = this._eventListeners[event] = [];
            ipc.on(event, function () {
                _.invoke(listeners, 'call');
            });
        }
        listeners.push(callback);
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
        let thisHandler = (event, result) => {
            let parsed = JSON.parse(result);
            if (parsed.extracted) {
                ipc.removeListener('file-callback', thisHandler);
            }
            callback(parsed);
        };

        ipc.on('file-callback', thisHandler);
        ipc.send('download-file-to', JSON.stringify(options));
    },

    remove: function (paths) {
        ipc.send('remove-paths', JSON.stringify(paths));
    },

    exists: function (path, callback) {
        let thisHandler = (event, result) => {
            let parsed = JSON.parse(result);
            ipc.removeListener('on-file-exists', thisHandler);
            callback(parsed);
        };

        ipc.on('on-file-exists', thisHandler);
        ipc.send('file-exists', JSON.stringify(path));
    },

    _eventListeners: {}
};
