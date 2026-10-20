import importlib.util
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

spec = importlib.util.spec_from_file_location("publish", Path(__file__).with_name("publish.py"))
publish = importlib.util.module_from_spec(spec)
spec.loader.exec_module(publish)


class DescriptorTests(unittest.TestCase):
    def test_immutable_paths_and_provenance(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "Build").mkdir()
            names = {"loaderUrl": "client.loader.js", "dataUrl": "data.br",
                     "frameworkUrl": "framework.js.br", "codeUrl": "code.wasm.br"}
            for name in names.values():
                (root / "Build" / name).write_bytes(b"asset")
            (root / "index.html").write_text(
                '<script src="Build/client.loader.js"></script>' +
                ''.join(key + ': "Build/' + value + '"' for key, value in names.items() if key != "loaderUrl"))
            manifest = {"buildId": "unique-build", "commit": "a" * 40,
                        "unityVersion": "6000.4.10f1", "projectVersion": "4.0.7"}
            result = publish.descriptor(root, manifest, "b" * 40)
            self.assertEqual(result["sourceRevision"], "b" * 40)
            self.assertIn("/unique-build/", result["codeUrl"])
            self.assertEqual(len(result["checksums"]), 5)
            (root / "index.html").write_text((root / "index.html").read_text()
                                           .replace("Build/code.wasm.br", "../outside.wasm.br"))
            with self.assertRaises(ValueError):
                publish.descriptor(root, manifest, "b" * 40)

    def test_retries_cannot_replace_a_published_build(self):
        build = {"buildId": "same-id", "checksums": {"file": "original"}}
        with patch.object(publish, "read_descriptor", return_value=build), patch.object(publish, "upload") as upload:
            publish.stage(Path("unused"), build, Path("unused"))
            upload.assert_not_called()
            with self.assertRaisesRegex(RuntimeError, "different immutable assets"):
                publish.stage(Path("unused"), {**build, "checksums": {"file": "changed"}}, Path("unused"))
            upload.assert_not_called()


if __name__ == "__main__":
    unittest.main()
