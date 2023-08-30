from spellsource_preprocessors.utils import common_annotator_call, annotator_ckpts_path, HF_MODEL_NAME, \
    DWPOSE_MODEL_NAME
import comfy.model_management as model_management


class DWPose_Preprocessor:
    @classmethod
    def INPUT_TYPES(s):
        return {
            "required": {
                "image": ("IMAGE",),
                "detect_hand": (["enable", "disable"], {"default": "enable"}),
                "detect_body": (["enable", "disable"], {"default": "enable"}),
                "detect_face": (["enable", "disable"], {"default": "enable"}),
            }
        }

    RETURN_TYPES = ("IMAGE",)
    FUNCTION = "estimate_pose"

    CATEGORY = "ControlNet Preprocessors/Faces and Poses"

    def estimate_pose(self, image, detect_hand, detect_body, detect_face, **kwargs):
        from spellsource_preprocessors.controlnet_aux.dwpose import DwposeDetector

        detect_hand = detect_hand == "enable"
        detect_body = detect_body == "enable"
        detect_face = detect_face == "enable"

        model = DwposeDetector.from_pretrained(DWPOSE_MODEL_NAME, cache_dir=annotator_ckpts_path).to(
            model_management.get_torch_device())
        out = common_annotator_call(model, image, include_hand=detect_hand, include_face=detect_face,
                                    include_body=detect_body)
        del model
        return (out,)


NODE_CLASS_MAPPINGS = {
    "DWPreprocessor": DWPose_Preprocessor
}
NODE_DISPLAY_CLASS_MAPPINGS = {
    "DWPreprocessor": "DWPose Pose Recognition"
}
