import contextlib
import gc
import os.path
import typing

import torch
from diffusers import DiffusionPipeline, IFPipeline
from optimum.bettertransformer import BetterTransformer
from transformers import BitsAndBytesConfig

import spellsource_common.huggingface
from comfy.model_management import throw_exception_if_processing_interrupted, get_torch_device, vram_state, VRAMState
from comfy.nodes.package_typing import CustomNode
from comfy.utils import ProgressBar

_model_base_path = spellsource_common.huggingface.model_base_path


def _find_files(directory: str, filename: str) -> typing.List[str]:
    return [os.path.join(root, file) for root, _, files in os.walk(directory) for file in files if file == filename]


class IFLoader(CustomNode):
    @classmethod
    def INPUT_TYPES(s):
        return {
            "required": {
                "model_name": (IFLoader._MODELS, {"default": "I-XL"}),
                "quantization": (list(IFLoader._QUANTIZATIONS.keys()), {"default": "16-bit"}),
            }
        }

    CATEGORY = "deepfloyd"
    FUNCTION = "process"
    RETURN_TYPES = ("IF_MODEL",)

    _MODELS = ["I-M", "I-L", "I-XL", "II-M", "II-L"]

    _QUANTIZATIONS = {
        "4-bit": BitsAndBytesConfig(
            load_in_4bit=True,
            bnb_4bit_use_double_quant=True,
        ),
        "8-bit": BitsAndBytesConfig(
            load_in_8bit=True,
        ),
        "16-bit": None,
    }

    def process(self, model_name: str, quantization: str):
        assert model_name in IFLoader._MODELS

        model_v: DiffusionPipeline
        model_path: str
        kwargs = {
            "variant": "fp16",
            "torch_dtype": torch.float16,
            "requires_safety_checker": False,
            "feature_extractor": None,
            "safety_checker": None,
            "watermarker": None,
            "device_map": None,
            "token": None
        }

        if IFLoader._QUANTIZATIONS[quantization] is not None:
            kwargs['quantization_config'] = IFLoader._QUANTIZATIONS[quantization]

        model_path = f"{_model_base_path}/IF-{model_name}-v1.0"

        if not os.path.exists(model_path):
            kwargs['cache_dir'] = os.path.abspath(_model_base_path)
            model_path = f"DeepFloyd/IF-{model_name}-v1.0"

        model_v: IFPipeline = DiffusionPipeline.from_pretrained(
            pretrained_model_name_or_path=model_path,
            **kwargs
        )

        device = get_torch_device()
        model_v = model_v.to(device)

        if vram_state == VRAMState.LOW_VRAM:
            model_v.enable_sequential_cpu_offload()
        else:
            model_v.enable_model_cpu_offload()

        try:
            model_v.text_encoder = BetterTransformer.transform(model_v.text_encoder)
        except:
            pass

        return (model_v,)


class IFEncoder(CustomNode):
    @classmethod
    def INPUT_TYPES(s):
        return {
            "required": {
                "model": ("IF_MODEL",),
                "positive": ("STRING", {"default": "", "multiline": True}),
                "negative": ("STRING", {"default": "", "multiline": True}),
            },
        }

    CATEGORY = "deepfloyd"
    FUNCTION = "process"
    RETURN_TYPES = ("POSITIVE", "NEGATIVE",)

    def process(self, model: IFPipeline, positive, negative):
        positive, negative = model.encode_prompt(
            prompt=positive,
            negative_prompt=negative,
        )

        if model.final_offload_hook is not None:
            model.final_offload_hook.offload()

        return (positive, negative,)


class IFStageI:
    @classmethod
    def INPUT_TYPES(s):
        return {
            "required": {
                "positive": ("POSITIVE",),
                "negative": ("NEGATIVE",),
                "model": ("IF_MODEL",),
                "width": ("INT", {"default": 64, "min": 8, "max": 128, "step": 8}),
                "height": ("INT", {"default": 64, "min": 8, "max": 128, "step": 8}),
                "batch_size": ("INT", {"default": 1, "min": 1, "max": 100}),
                "seed": ("INT", {"default": 0, "min": 0, "max": 0xffffffffffffffff}),
                "steps": ("INT", {"default": 20, "min": 1, "max": 10000}),
                "cfg": ("FLOAT", {"default": 8.0, "min": 0.0, "max": 100.0})
            },
        }

    CATEGORY = "deepfloyd"
    FUNCTION = "process"
    RETURN_TYPES = ("IMAGE",)

    def process(self, model: IFPipeline, positive, negative, width, height, batch_size, seed, steps, cfg):
        progress = ProgressBar(steps)

        def callback(step, time_step, latent):
            throw_exception_if_processing_interrupted()
            progress.update_absolute(step)

        gc.collect()
        with torch.backends.cuda.sdp_kernel(enable_flash=True,
                                            enable_math=False,
                                            enable_mem_efficient=vram_state == VRAMState.LOW_VRAM or vram_state == VRAMState.NO_VRAM) if 'cuda' in get_torch_device().type else contextlib.nullcontext():
            image = model(
                prompt_embeds=positive,
                negative_prompt_embeds=negative,
                width=width,
                height=height,
                generator=torch.manual_seed(seed),
                guidance_scale=cfg,
                num_images_per_prompt=batch_size,
                num_inference_steps=steps,
                callback=callback,
                output_type="pt",
            ).images

        image = (image / 2 + 0.5).clamp(0, 1)
        image = image.cpu().float().permute(0, 2, 3, 1)

        if model.final_offload_hook is not None:
            model.final_offload_hook.offload()

        return (image,)


class IFStageII:
    @classmethod
    def INPUT_TYPES(s):
        return {
            "required": {
                "positive": ("POSITIVE",),
                "negative": ("NEGATIVE",),
                "model": ("IF_MODEL",),
                "images": ("IMAGE",),
                "seed": ("INT", {"default": 0, "min": 0, "max": 0xffffffffffffffff}),
                "steps": ("INT", {"default": 20, "min": 1, "max": 10000}),
                "cfg": ("FLOAT", {"default": 8.0, "min": 0.0, "max": 100.0}),
            },
        }

    CATEGORY = "deepfloyd"
    FUNCTION = "process"
    RETURN_NAMES = ("IMAGES",)
    RETURN_TYPES = ("IMAGE",)

    def process(self, model: IFPipeline, images, positive, negative, seed, steps, cfg):
        images = images.permute(0, 3, 1, 2)
        progress = ProgressBar(steps)
        batch_size = images.shape[0]

        if batch_size > 1:
            positive = positive.repeat(batch_size, 1, 1)
            negative = negative.repeat(batch_size, 1, 1)

        def callback(step, time_step, latent):
            throw_exception_if_processing_interrupted()
            progress.update_absolute(step)

        with torch.backends.cuda.sdp_kernel(enable_flash=True,
                                            enable_math=False,
                                            enable_mem_efficient=vram_state == VRAMState.LOW_VRAM or vram_state == VRAMState.NO_VRAM) if 'cuda' in get_torch_device().type else contextlib.nullcontext():
            images = model(
                image=images,
                prompt_embeds=positive,
                negative_prompt_embeds=negative,
                height=images.shape[2] // 8 * 8 * 4,
                width=images.shape[3] // 8 * 8 * 4,
                generator=torch.manual_seed(seed),
                guidance_scale=cfg,
                num_inference_steps=steps,
                callback=callback,
                output_type="pt",
            ).images

        images = images.clamp(0, 1)
        images = images.permute(0, 2, 3, 1)
        images = images.to("cpu", torch.float32)

        if model.final_offload_hook is not None:
            model.final_offload_hook.offload()

        return (images,)
