import importlib.util,json,tempfile,unittest
from pathlib import Path
s=importlib.util.spec_from_file_location('publish','tools/webgl/publish.py');m=importlib.util.module_from_spec(s);s.loader.exec_module(m)
class DescriptorTests(unittest.TestCase):
 def test_immutable_paths_and_provenance(self):
  with tempfile.TemporaryDirectory() as d:
   p=Path(d);(p/'Build').mkdir()
   names={'loaderUrl':'client.loader.js','dataUrl':'data.br','frameworkUrl':'framework.js.br','codeUrl':'code.wasm.br'}
   for n in names.values():(p/'Build'/n).write_bytes(b'asset')
   (p/'index.html').write_text('<script src="Build/client.loader.js"></script>'+''.join(k+': "Build/'+v+'"' for k,v in names.items() if k!='loaderUrl'))
   manifest={'buildId':'unique-build','commit':'a'*40,'unityVersion':'6000.4.10f1','projectVersion':'4.0.7'}
   result=m.descriptor(p,manifest,'b'*40)
   self.assertEqual(result['sourceRevision'],'b'*40);self.assertIn('/unique-build/',result['codeUrl']);self.assertEqual(len(result['checksums']),5)
   (p/'index.html').write_text((p/'index.html').read_text().replace('Build/code.wasm.br','../outside.wasm.br'))
   with self.assertRaises(ValueError):m.descriptor(p,manifest,'b'*40)
unittest.main()
