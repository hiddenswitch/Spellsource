import torch
from comfy.component_model.tensor_types import RGBImageBatch, MaskBatch
from comfy.language.transformers_model_management import TransformersManagedModel
from comfy.model_downloader import get_or_download_huggingface_repo
from comfy.model_management import load_models_gpu
from comfy.node_helpers import export_custom_nodes
from comfy.nodes.package_typing import CustomNode, InputTypes
from einops import rearrange
from torchvision import transforms
from transformers import AutoModelForImageSegmentation

class BiRefNetLoader(CustomNode):
    @classmethod
    def INPUT_TYPES(cls) -> InputTypes:
        return {
            "required": {
                "repo_id": (['ZhengPeng7/BiRefNet', 'ZhengPeng7/BiRefNet_HR'],)
            }
        }

    FUNCTION = "execute"
    CATEGORY = "birefnet"
    RETURN_TYPES = ("MODEL",)

    def execute(self, repo_id: str = 'ZhengPeng7/BiRefNet') -> tuple[TransformersManagedModel]:
        weights = get_or_download_huggingface_repo(repo_id)
        transformers_model = AutoModelForImageSegmentation.from_pretrained(weights, trust_remote_code=True)
        transformers_model.eval().half()
        return TransformersManagedModel(repo_id=repo_id, model=transformers_model, tokenizer=None, config_dict={}, processor=None),


class PrepareBiRefNetInput(CustomNode):
    @classmethod
    def INPUT_TYPES(cls) -> InputTypes:
        return {
            "required": {
                "image": ("IMAGE", {})
            }
        }

    FUNCTION = "execute"
    CATEGORY = "birefnet"
    RETURN_TYPES = ("IMAGE",)

    transform_image = transforms.Compose([
        transforms.Resize((1024, 1024)),
        transforms.Normalize([0.485, 0.456, 0.406], [0.229, 0.224, 0.225])
    ])

    def execute(self, image: RGBImageBatch) -> tuple[RGBImageBatch]:
        channels_first = rearrange(image, "b h w c -> b c h w")
        return rearrange(PrepareBiRefNetInput.transform_image(channels_first), "b c h w -> b h w c"),


class BiRefNet(CustomNode):
    @classmethod
    def INPUT_TYPES(cls) -> InputTypes:
        return {
            "required": {
                "model": ("MODEL", {}),
                "image": ("IMAGE", {})
            }
        }

    FUNCTION = "execute"
    CATEGORY = "birefnet"
    RETURN_TYPES = ("MASK",)

    def execute(self, model: TransformersManagedModel, image: RGBImageBatch) -> tuple[MaskBatch]:
        load_models_gpu([model])
        # rearrange to channels first
        image = image.movedim(-1, -3).to(model.current_device, dtype=model.model_dtype())
        with torch.inference_mode():
            res: MaskBatch = model.model(image)[-1].sigmoid().float()

        return res.cpu(),


export_custom_nodes()
