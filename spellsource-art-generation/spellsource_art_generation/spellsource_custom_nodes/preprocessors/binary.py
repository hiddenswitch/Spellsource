from spellsource_preprocessors.utils import common_annotator_call


class Binary_Preprocessor:
    @classmethod
    def INPUT_TYPES(s):
        return {
            "required": {
                "image": ("IMAGE",),
                "bin_threshold": ("INT", {"default": 100, "min": 0, "max": 255, "step": 1}),
            }
        }

    RETURN_TYPES = ("IMAGE",)
    FUNCTION = "execute"

    CATEGORY = "ControlNet Preprocessors/Line Extractors"

    def execute(self, image, bin_threshold, **kwargs):
        from spellsource_preprocessors.controlnet_aux.binary import BinaryDetector

        return (common_annotator_call(BinaryDetector(), image, bin_threshold=bin_threshold), )



NODE_CLASS_MAPPINGS = {
    "BinaryPreprocessor": Binary_Preprocessor
}
NODE_DISPLAY_NAME_MAPPINGS = {
    "BinaryPreprocessor": "Binary Lines"
}