from spellsource_preprocessors.utils import common_annotator_call, annotator_ckpts_path, HF_MODEL_NAME
import comfy.model_management as model_management
import numpy as np


class MLSD_Preprocessor:
    @classmethod
    def INPUT_TYPES(s):
        return {
            "required": {
                "image": ("IMAGE",),
                "score_threshold": ("FLOAT", {"default": 0.1, "min": 0.01, "max": 2.0, "step": 0.01}),
                "dist_threshold": ("FLOAT", {"default": 0.1, "min": 0.01, "max": 20.0, "step": 0.01})
            }
        }

    RETURN_TYPES = ("IMAGE",)
    FUNCTION = "execute"

    CATEGORY = "ControlNet Preprocessors/Line Extractors"

    def execute(self, image, score_threshold, dist_threshold, **kwargs):
        from spellsource_preprocessors.controlnet_aux.mlsd import MLSDdetector

        model = MLSDdetector.from_pretrained(HF_MODEL_NAME, cache_dir=annotator_ckpts_path).to(
            model_management.get_torch_device())
        out = common_annotator_call(model, image, thr_v=score_threshold, thr_d=dist_threshold)
        return (out,)


NODE_CLASS_MAPPINGS = {
    "M-LSDPreprocessor": MLSD_Preprocessor
}
NODE_DISPLAY_NAME_MAPPINGS = {
    "M-LSDPreprocessor": "M-LSD Lines"
}
