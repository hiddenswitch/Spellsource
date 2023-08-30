import cv2
import numpy as np
import torch

import spellsource_common.huggingface

annotator_ckpts_path = spellsource_common.huggingface.model_base_path

HF_MODEL_NAME = "lllyasviel/Annotators"
DWPOSE_MODEL_NAME = "yzd-v/DWPose"


def common_annotator_call(model, tensor_image, **kwargs):
    out_list = []
    for image in tensor_image:
        H, W, C = image.shape
        np_image = np.asarray(image * 255., dtype=np.uint8)
        np_result = model(np_image, output_type="np", **kwargs)
        np_result = cv2.resize(np_result, (W, H), interpolation=cv2.INTER_AREA)
        out_list.append(torch.from_numpy(np_result.astype(np.float32) / 255.0))
    return torch.stack(out_list, dim=0)
