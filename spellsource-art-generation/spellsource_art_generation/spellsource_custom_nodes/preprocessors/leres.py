from spellsource_preprocessors.utils import common_annotator_call, annotator_ckpts_path, HF_MODEL_NAME
import comfy.model_management as model_management


class LERES_Depth_Map_Preprocessor:
    @classmethod
    def INPUT_TYPES(s):
        return {
            "required": {
                "image": ("IMAGE",),
                "rm_nearest": ("FLOAT", {"default": 0.0, "min": 0.0, "max": 100, "step": 0.1}),
                "rm_background": ("FLOAT", {"default": 0.0, "min": 0.0, "max": 100, "step": 0.1})
            },
            "optional": {
                "boost": (["enable", "disable"], {"default": "disable"})
            }
        }

    RETURN_TYPES = ("IMAGE",)
    FUNCTION = "execute"

    CATEGORY = "ControlNet Preprocessors/Normal and Depth Map"

    def execute(self, image, rm_nearest, rm_background, **kwargs):
        from spellsource_preprocessors.controlnet_aux.leres import LeresDetector

        model = LeresDetector.from_pretrained(HF_MODEL_NAME, cache_dir=annotator_ckpts_path).to(
            model_management.get_torch_device())
        out = common_annotator_call(model, image, thr_a=rm_nearest, thr_b=rm_background,
                                    boost=kwargs["boost"] == "enable")
        del model
        return (out,)


NODE_CLASS_MAPPINGS = {
    "LeReS-DepthMapPreprocessor": LERES_Depth_Map_Preprocessor
}
NODE_DISPLAY_NAME_MAPPINGS = {
    "LeReS-DepthMapPreprocessor": "LeReS - Depth Map (enable boost for leres++)"
}
