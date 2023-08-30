from spellsource_preprocessors.utils import common_annotator_call


class Color_Preprocessor:
    @classmethod
    def INPUT_TYPES(s):
        return {
            "required": {"image": ("IMAGE",)}
        }

    RETURN_TYPES = ("IMAGE",)
    FUNCTION = "execute"

    CATEGORY = "ControlNet Preprocessors/T2IAdapter-only"

    def execute(self, image, **kwargs):
        from spellsource_preprocessors.controlnet_aux.color import ColorDetector

        return (common_annotator_call(ColorDetector(), image),)


NODE_CLASS_MAPPINGS = {
    "ColorPreprocessor": Color_Preprocessor
}
NODE_DISPLAY_NAME_MAPPINGS = {
    "ColorPreprocessor": "Color Pallete"
}
