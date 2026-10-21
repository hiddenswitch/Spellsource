# Spellsource Web

This is the package for the HTML and related backend server content for Spellsource. This includes the online collections UI.

## Android emulator

The emulator exercises Android Chrome itself, rather than desktop Chrome with a
mobile viewport. It also supports installing the native Spellsource APK. Run the
following from the repository root with Node 22, Yarn, Java, Python 3, and the
Android SDK command-line tools installed. Linux needs access to `/dev/kvm`.
`ANDROID_SDK_ROOT` or `ANDROID_HOME` selects the SDK; the local default is
`~/Android/Sdk`.

```sh
python3 tools/android/emulator.py setup
python3 tools/android/emulator.py start
python3 tools/android/emulator.py status
yarn workspace spellsource-web test-android
python3 tools/android/emulator.py stop
```

This creates `spellsource_api35` (Pixel 6a, Android 15/API 35, Google APIs,
x86_64), listening as `emulator-5580`. It preserves the AVD's data between boots
and never chooses an attached physical phone. Each WebGL run resets Chrome's
test profile on this dedicated emulator (native app data is retained), then checks guest match
creation, offline recovery, Home/return, screen sleep/wake, rotation, and closing
and reopening the tab. Traces and screenshots go to `test-results/android-webgl`.
Use `SPELLSOURCE_BASE_URL` for a different site and
`SPELLSOURCE_EXPECTED_BUILD_ID` to require an exact published build.

Add `--window` to `start` for a visible emulator, or `--gpu host` to use the host
GPU. The default uses software rendering and needs no desktop session. Global
options such as `--port 5582` and `--avd another_spellsource_avd` go before the
command; set matching `ANDROID_SERIAL` and `ANDROID_AVD` for the browser test.

Native testing accepts an APK or the AAB produced by Unity CI:

```sh
python3 tools/android/emulator.py native /path/to/Spellsource.apk
python3 tools/android/emulator.py native /path/to/Spellsource.aab
```

AABs are converted with a checksum-pinned Google bundletool into a **debug-signed
test APK**, not a store release. The API 35 Google APIs image provides ARM64
translation for the ARM64 Unity build. The native check installs and launches
`com.hiddenswitch.Spellsource`, checks that the process survives startup, and
retains a screenshot, logcat, and artifact checksum in `test-results/android`.
This is a startup smoke check, not native gameplay acceptance. It does not
uninstall an existing package to bypass signing or downgrade errors.

The manually dispatched **Android Emulator Smoke** GitHub workflow runs the
same WebGL test. Supply a native APK/AAB HTTPS URL and SHA-256 to run native
startup afterward. Native and browser tests run sequentially because they share
the emulator's foreground application. Existing browser publication gates remain
independent of this optional emulator workflow.

## Generating Images from PSD/PSBs

```shell script
# get python 3.7+
brew install python
# create a python environment if it doesn't already exist
if [[ ! -f .venv/bin/activate ]]; then
  pip3 install -U virtualenv >/dev/null
  virtualenv -p python3 .venv
fi

source .venv/bin/activate
# install the spellsource python package
pip install -e python
spellsource psb-2-png-layers --help
```

For example, to generate a single image of the map and save it, 3x scale, to Map.png:
```shell script
spellsource psb-2-png-layers --merge=TRUE --scale=3 ~/Dropbox/Spellsource\ Art/Spellsource\ World\ Map.psd Map.png
```
