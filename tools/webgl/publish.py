#!/usr/bin/env python3
"""Stage immutable WebGL players; promote only a tested develop revision."""
import argparse
import hashlib
import json
import mimetypes
import os
from pathlib import Path
import re
import subprocess
import tempfile
import urllib.request

ROOT = "https://s3-builds.appmana.com/spellsource-build-artifacts/spellsource-client/webgl/"
BUCKET = "s3://spellsource-build-artifacts/spellsource-client/webgl/"


def aws(*args):
    subprocess.run(["aws", "--endpoint-url", os.environ.get("ARTIFACT_ENDPOINT", "https://s3-builds.appmana.com"), "s3", *args], check=True)


def upload(path, key, mutable=False):
    content = mimetypes.guess_type(str(path).removesuffix(".br"))[0] or "application/octet-stream"
    if str(path).endswith(".wasm.br"):
        content = "application/wasm"
    args = ["cp", str(path), BUCKET + key, "--acl", "public-read", "--content-type", content,
            "--cache-control", "no-store" if mutable else "public,max-age=31536000,immutable"]
    if str(path).endswith(".br"):
        args += ["--content-encoding", "br"]
    aws(*args)


def descriptor(directory, manifest, source_revision):
    build_id = manifest["buildId"]
    if not re.fullmatch(r"[A-Za-z0-9][A-Za-z0-9_-]{0,159}", build_id):
        raise ValueError("Invalid build ID")
    if not re.fullmatch(r"[a-f0-9]{40}", source_revision):
        raise ValueError("Expected an exact source revision")
    html = (directory / "index.html").read_text()
    base = ROOT + build_id + "/"
    result = dict(schemaVersion=1, buildId=build_id, sourceRevision=source_revision,
                  unityRevision=manifest["commit"], unityVersion=manifest["unityVersion"],
                  productVersion=manifest["projectVersion"], baseUrl=base, checksums={})
    patterns = {"loaderUrl": r'<script src="([^"]+\.loader\.js)"'}
    patterns.update({key: key + r':\s*"([^"]+)"' for key in ["dataUrl", "frameworkUrl", "codeUrl", "symbolsUrl"]})
    for key, pattern in patterns.items():
        match = re.search(pattern, html)
        if not match:
            if key == "symbolsUrl":
                continue
            raise ValueError("Missing asset: " + key)
        relative = match[1]
        path = (directory / relative).resolve()
        if not path.is_relative_to(directory.resolve()) or not path.is_file():
            raise ValueError("Invalid asset: " + relative)
        result[key] = base + relative
    for path in sorted(directory.rglob("*")):
        if path.is_file():
            if path.is_symlink():
                raise ValueError("Symbolic link in build")
            result["checksums"][path.relative_to(directory).as_posix()] = hashlib.sha256(path.read_bytes()).hexdigest()
    return result


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("operation", choices=["stage", "promote"])
    parser.add_argument("--directory", type=Path, required=True)
    parser.add_argument("--manifest", type=Path, required=True)
    parser.add_argument("--source-revision", required=True)
    args = parser.parse_args()
    manifest = json.loads(args.manifest.read_text())
    build = descriptor(args.directory, manifest, args.source_revision)
    with tempfile.TemporaryDirectory() as temp:
        output = Path(temp) / "build.json"
        output.write_text(json.dumps(build, indent=2) + "\n")
        if args.operation == "stage":
            for path in sorted(args.directory.rglob("*")):
                if path.is_file():
                    upload(path, build["buildId"] + "/" + path.relative_to(args.directory).as_posix())
            upload(output, build["buildId"] + "/build.json")
        else:
            request = urllib.request.Request("https://api.github.com/repos/hiddenswitch/Spellsource/git/ref/heads/develop",
                                             headers={"Accept": "application/vnd.github+json"})
            with urllib.request.urlopen(request, timeout=30) as response:
                current_revision = json.load(response)["object"]["sha"]
            if current_revision != args.source_revision:
                raise RuntimeError("Superseded develop revision; refusing promotion")
            with urllib.request.urlopen(ROOT + build["buildId"] + "/build.json", timeout=30) as response:
                if json.load(response) != build:
                    raise RuntimeError("Staged descriptor differs from the tested artifact")
            previous = Path(temp) / "previous.json"
            try:
                with urllib.request.urlopen(ROOT + "current.json", timeout=30) as response:
                    previous.write_bytes(response.read())
            except urllib.error.HTTPError as error:
                if error.code != 404:
                    raise
            if previous.exists():
                upload(previous, "previous.json", mutable=True)
            upload(output, "current.json", mutable=True)


if __name__ == "__main__":
    main()
