from comfy.model_downloader import add_known_models, KNOWN_LORAS
from comfy.model_downloader_types import CivitFile

add_known_models("loras", KNOWN_LORAS,
                 CivitFile(model_id=945266, model_version_id=1058316, filename="dvr-pixel-flux.safetensors",
                           trigger_words=("dvr-pixel-flux",)),
                 CivitFile(model_id=681332, model_version_id=839447, filename="px-hard-v2.safetensors",
                           trigger_words=("pixel art",)),
                 CivitFile(model_id=681332, model_version_id=839447, filename="px-hard-v2.safetensors",
                           trigger_words=("pixel art",)),
                 CivitFile(model_id=120096, model_version_id=135931, filename="pixel-art-xl-v1.1.safetensors"))
