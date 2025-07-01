from comfy.model_downloader import KNOWN_CLIP_MODELS, add_known_models, KNOWN_HUGGINGFACE_MODEL_REPOS
from comfy.model_downloader_types import HuggingFile

add_known_models("clip", KNOWN_CLIP_MODELS, HuggingFile("zer0int/LongCLIP-GmP-ViT-L-14", "model.safetensors", save_with_filename="LongCLIP-GmP-ViT-L-14.safetensors"))
KNOWN_HUGGINGFACE_MODEL_REPOS.add("llava-hf/llava-onevision-qwen2-7b-ov-hf")