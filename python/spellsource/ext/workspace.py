from abc import abstractmethod, ABC

import numpy as np
from keras import Model


class Workspace(ABC):
    @abstractmethod
    def get_inference_model(self) -> Model:
        pass
    
    @abstractmethod
    def generate_seed(self) -> np.ndarray:
        pass
    
    @abstractmethod
    def generate_text(self, seed: np.ndarray) -> str:
        pass

    @abstractmethod
    def train(self):
        pass