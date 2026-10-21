#!/usr/bin/env python3
"""Manage a dedicated Spellsource Android emulator and smoke-test native APKs."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import sys
import time
import urllib.request
import zipfile

BUNDLETOOL_VERSION = "1.18.3"
BUNDLETOOL_SHA256 = "a099cfa1543f55593bc2ed16a70a7c67fe54b1747bb7301f37fdfd6d91028e29"


def run(*args, **kwargs):
    return subprocess.run([str(a) for a in args], check=True, **kwargs)


def output(*args):
    return run(*args, stdout=subprocess.PIPE, text=True).stdout.strip()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--sdk", type=Path, default=Path(os.environ.get("ANDROID_SDK_ROOT") or os.environ.get("ANDROID_HOME") or Path.home() / "Android/Sdk"))
    parser.add_argument("--avd", default="spellsource_api35")
    parser.add_argument("--port", type=int, default=5580)
    parser.add_argument("--output", type=Path, default=Path("spellsource-web/test-results/android"))
    commands = parser.add_subparsers(dest="command", required=True)
    commands.add_parser("setup")
    start = commands.add_parser("start")
    start.add_argument("--window", action="store_true")
    start.add_argument("--gpu", default="swiftshader")
    commands.add_parser("status")
    commands.add_parser("stop")
    native = commands.add_parser("native")
    native.add_argument("artifact", type=Path, help="APK or AAB; AABs are converted to a debug-signed universal APK")
    native.add_argument("--settle-seconds", type=int, default=20)
    args = parser.parse_args()
    if not re.fullmatch(r"[A-Za-z0-9_-]+", args.avd) or not 5554 <= args.port <= 5682 or args.port % 2:
        parser.error("Use an AVD name containing letters, digits, _ or -, and an even emulator port from 5554 to 5682")
    sdk = args.sdk.expanduser().resolve()
    adb = sdk / "platform-tools/adb"
    emulator = sdk / "emulator/emulator"
    cli = sdk / "cmdline-tools/latest/bin"
    serial = f"emulator-{args.port}"
    cache = Path(os.environ.get("XDG_CACHE_HOME", str(Path.home() / ".cache"))) / "spellsource/android"
    cache.mkdir(parents=True, exist_ok=True)
    env = dict(os.environ, ANDROID_HOME=str(sdk), ANDROID_SDK_ROOT=str(sdk))

    def device(*parts):
        return output(adb, "-s", serial, *parts)

    def verify_device():
        # Never infer a device from `adb devices`: the user's physical Pixel may be attached.
        actual = device("emu", "avd", "name").splitlines()[0]
        if actual != args.avd:
            raise RuntimeError(f"{serial} belongs to {actual}, expected {args.avd}; refusing to modify it")

    def ready():
        deadline = time.monotonic() + 240
        while time.monotonic() < deadline:
            try:
                if device("shell", "getprop", "sys.boot_completed") == "1":
                    verify_device()
                    return
            except subprocess.CalledProcessError:
                pass
            time.sleep(2)
        raise RuntimeError(f"{serial} did not boot within four minutes; inspect {cache / (args.avd + '.log')}")

    if args.command == "setup":
        image = "system-images;android-35;google_apis;x86_64"
        if not all(path.exists() for path in (adb, emulator, sdk / "build-tools/35.0.0/aapt",
                                                sdk / "system-images/android-35/google_apis/x86_64/system.img")):
            run(cli / "sdkmanager", f"--sdk_root={sdk}", "platform-tools", "emulator", "build-tools;35.0.0", image, env=env)
        avds = output(emulator, "-list-avds").splitlines()
        if args.avd not in avds:
            run(cli / "avdmanager", "create", "avd", "--name", args.avd, "--package", image, "--device", "pixel_6a", input="no\n", text=True, env=env)
        print(f"AVD ready: {args.avd}")
        return
    if args.command == "start":
        if serial not in output(adb, "devices"):
            command = [str(emulator), "-avd", args.avd, "-port", str(args.port), "-gpu", args.gpu,
                       "-no-audio", "-no-boot-anim", "-no-snapshot", "-memory", "4096", "-cores", "4"]
            if not args.window:
                command.append("-no-window")
            with (cache / f"{args.avd}.log").open("ab") as log:
                subprocess.Popen(command, stdout=log, stderr=subprocess.STDOUT, env=env, start_new_session=True)
        ready()
        device("shell", "settings", "put", "global", "window_animation_scale", "0")
        device("shell", "settings", "put", "global", "transition_animation_scale", "0")
        device("shell", "settings", "put", "global", "animator_duration_scale", "0")
        device("shell", "am", "set-debug-app", "--persistent", "com.android.chrome")
        print(f"Ready: ANDROID_SERIAL={serial} ANDROID_AVD={args.avd}")
        return
    verify_device()
    if args.command == "stop":
        print(device("emu", "kill"))
        return
    if args.command == "status":
        print(json.dumps({"serial": serial, "avd": args.avd, "booted": device("shell", "getprop", "sys.boot_completed"),
                          "abis": device("shell", "getprop", "ro.product.cpu.abilist"),
                          "nativeBridge": device("shell", "getprop", "ro.dalvik.vm.native.bridge")}, indent=2))
        return

    ready()
    args.output.mkdir(parents=True, exist_ok=True)
    artifact = args.artifact.resolve()
    if artifact.suffix == ".aab":
        bundletool = cache / f"bundletool-{BUNDLETOOL_VERSION}.jar"
        if not bundletool.exists():
            urllib.request.urlretrieve(f"https://github.com/google/bundletool/releases/download/{BUNDLETOOL_VERSION}/bundletool-all-{BUNDLETOOL_VERSION}.jar", bundletool)
        if hashlib.sha256(bundletool.read_bytes()).hexdigest() != BUNDLETOOL_SHA256:
            raise RuntimeError("bundletool checksum mismatch")
        archive = args.output / "native.apks"
        run("java", "-jar", bundletool, "build-apks", f"--bundle={artifact}", f"--output={archive}", "--mode=universal", "--overwrite")
        artifact = args.output / "universal.apk"
        with zipfile.ZipFile(archive) as archive_file:
            artifact.write_bytes(archive_file.read("universal.apk"))
    if artifact.suffix != ".apk":
        raise RuntimeError("Expected an .apk or .aab file")
    aapt = sdk / "build-tools/35.0.0/aapt"
    metadata = output(aapt, "dump", "badging", artifact)
    package = re.search(r"^package: name='([^']+)'", metadata, re.M).group(1)
    activity_match = re.search(r"^launchable-activity: name='([^']+)'", metadata, re.M)
    if not activity_match:
        raise RuntimeError("APK has no launchable activity")
    activity = activity_match.group(1)
    if package != "com.hiddenswitch.Spellsource" or not re.fullmatch(r"[A-Za-z0-9_.$]+", activity):
        raise RuntimeError(f"Expected the Spellsource package; found {package}")
    run(adb, "-s", serial, "install", "-r", artifact)
    device("shell", "am", "force-stop", package)
    device("logcat", "-c")
    print(device("shell", "am", "start", "-W", "-n", f"{package}/{activity}"))
    time.sleep(args.settle_seconds)
    logs = device("logcat", "-d", "-v", "threadtime")
    (args.output / "native-logcat.txt").write_text(logs)
    with (args.output / "native.png").open("wb") as screenshot:
        run(adb, "-s", serial, "exec-out", "screencap", "-p", stdout=screenshot)
    pid = device("shell", "pidof", package)
    if not pid or re.search(r"FATAL EXCEPTION|Fatal signal \d+", logs):
        raise RuntimeError("Native startup crashed; inspect native-logcat.txt and native.png")
    result = {"package": package, "pid": pid, "artifactSha256": hashlib.sha256(artifact.read_bytes()).hexdigest(),
              "deviceAbis": device("shell", "getprop", "ro.product.cpu.abilist"), "startupPassed": True}
    (args.output / "native-result.json").write_text(json.dumps(result, indent=2))
    print(json.dumps(result))


if __name__ == "__main__":
    try:
        main()
    except (RuntimeError, subprocess.CalledProcessError) as error:
        print(str(error), file=sys.stderr)
        sys.exit(1)
