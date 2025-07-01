from comfy.comfy_types import IO
from comfy.nodes.package_typing import CustomNode, InputTypes


class SimpleImageComposite(CustomNode):
    @classmethod
    def INPUT_TYPES(cls) -> InputTypes:
        return {
            "required": {
                "below": (IO.IMAGE, {}),
                "above": (IO.IMAGE, {}),
                "output_channels": ([3, 4], {"default": 3})
            }
        }
