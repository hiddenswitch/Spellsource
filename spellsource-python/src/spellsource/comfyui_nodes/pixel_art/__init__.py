import os

from comfy.cmd import folder_paths

if 'U2NET_HOME' not in os.environ:
    # make it less painful to pre-populate the model files
    os.environ['U2NET_HOME'] = os.path.join(folder_paths.models_dir, "rembg")

import rembg