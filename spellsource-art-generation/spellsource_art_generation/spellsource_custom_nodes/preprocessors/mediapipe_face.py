import subprocess
import sys
import threading

from spellsource_preprocessors.utils import common_annotator_call


# Ref: https://github.com/ltdrdata/ComfyUI-Manager/blob/284e90dc8296a2e1e4f14b4b2d10fba2f52f0e53/__init__.py#L14
def handle_stream(stream, prefix):
    for line in stream:
        print(prefix, line, end="")


def run_script(cmd, cwd='.'):
    process = subprocess.Popen(cmd, cwd=cwd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, bufsize=1)

    stdout_thread = threading.Thread(target=handle_stream, args=(process.stdout, ""))
    stderr_thread = threading.Thread(target=handle_stream, args=(process.stderr, "[!]"))

    stdout_thread.start()
    stderr_thread.start()

    stdout_thread.join()
    stderr_thread.join()

    return process.wait()


class Media_Pipe_Face_Mesh_Preprocessor:
    @classmethod
    def INPUT_TYPES(s):
        return {"required": {"image": ("IMAGE",),
                             "max_faces": ("INT", {"default": 10, "min": 1, "max": 50, "step": 1}),
                             # Which image has more than 50 detectable faces?
                             "min_confidence": ("FLOAT", {"default": 0.5, "min": 0.01, "max": 1.0, "step": 0.01})
                             }}

    RETURN_TYPES = ("IMAGE",)
    FUNCTION = "detect"

    CATEGORY = "ControlNet Preprocessors/Faces and Poses"

    def detect(self, image, max_faces, min_confidence):
        try:
            import mediapipe
        except ImportError:
            run_script([sys.executable, '-s', '-m', 'pip', 'install', 'mediapipe'])

        # Ref: https://github.com/Fannovel16/comfy_controlnet_preprocessors/issues/70#issuecomment-1677967369
        from spellsource_preprocessors.controlnet_aux.mediapipe_face import MediapipeFaceDetector

        return (
            common_annotator_call(MediapipeFaceDetector(), image, max_faces=max_faces, min_confidence=min_confidence),)


NODE_CLASS_MAPPINGS = {
    "MediaPipe-FaceMeshPreprocessor": Media_Pipe_Face_Mesh_Preprocessor
}

NODE_CLASS_NAME_MAPPINGS = {
    "MediaPipe-FaceMeshPreprocessor": "MediaPipe Face Mesh"
}
