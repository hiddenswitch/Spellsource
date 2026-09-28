from comfy.model_downloader import KNOWN_CLIP_MODELS, KNOWN_LORAS, add_known_models, KNOWN_HUGGINGFACE_MODEL_REPOS
from comfy.model_downloader_types import CivitFile, HuggingFile

add_known_models("clip", KNOWN_CLIP_MODELS, HuggingFile("zer0int/LongCLIP-GmP-ViT-L-14", "model.safetensors", save_with_filename="LongCLIP-GmP-ViT-L-14.safetensors"))
KNOWN_HUGGINGFACE_MODEL_REPOS.add("llava-hf/llava-onevision-qwen2-7b-ov-hf")

# Pixel art LoRAs, formerly registered by the vendored pixel_art nodes. The nodes
# themselves now come from the pixel-art package (its own comfyui.custom_nodes
# entry point).
add_known_models("loras", KNOWN_LORAS,
                 CivitFile(model_id=945266, model_version_id=1058316, filename="dvr-pixel-flux.safetensors",
                           trigger_words=("dvr-pixel-flux",)),
                 CivitFile(model_id=681332, model_version_id=839447, filename="px-hard-v2.safetensors",
                           trigger_words=("pixel art",)),
                 CivitFile(model_id=120096, model_version_id=135931, filename="pixel-art-xl-v1.1.safetensors"))
