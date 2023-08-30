from spellsource_preprocessors.utils import common_annotator_call, annotator_ckpts_path, HF_MODEL_NAME
import comfy.model_management as model_management


class Shuffle_Preprocessor:
    @classmethod
    def INPUT_TYPES(s):
        return {"required": {"image": ("IMAGE",)}}

    RETURN_TYPES = ("IMAGE",)
    FUNCTION = "preprocess"

    CATEGORY = "ControlNet Preprocessors/T2IAdapter-only"

    def preprocess(self, image):
        from spellsource_preprocessors.controlnet_aux.shuffle import ContentShuffleDetector

        return (common_annotator_call(ContentShuffleDetector(), image),)


NODE_CLASS_MAPPINGS = {
    "ShufflePreprocessor": Shuffle_Preprocessor
}

NODE_DISPLAY_CLASS_MAPPINGS = {
    "ShufflePreprocessor": "Content Shuffle"
}
