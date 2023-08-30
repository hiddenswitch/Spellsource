from spellsource_preprocessors.utils import common_annotator_call, annotator_ckpts_path, HF_MODEL_NAME
import comfy.model_management as model_management


class AnimeLineArt_Preprocessor:
    @classmethod
    def INPUT_TYPES(s):
        return {"required": {"image": ("IMAGE",)}}

    RETURN_TYPES = ("IMAGE",)
    FUNCTION = "execute"

    CATEGORY = "ControlNet Preprocessors/Line Extractors"

    def execute(self, image, **kwargs):
        from spellsource_preprocessors.controlnet_aux.lineart_anime import LineartAnimeDetector

        model = LineartAnimeDetector.from_pretrained(HF_MODEL_NAME, cache_dir=annotator_ckpts_path).to(
            model_management.get_torch_device())
        out = common_annotator_call(model, image)
        del model
        return (out,)


NODE_CLASS_MAPPINGS = {
    "AnimeLineArtPreprocessor": AnimeLineArt_Preprocessor
}
NODE_DISPLAY_NAME_MAPPINGS = {
    "AnimeLineArtPreprocessor": "Anime Lineart"
}
