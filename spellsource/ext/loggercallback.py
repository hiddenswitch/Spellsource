import time

from keras.callbacks import Callback

from spellsource.ext.workspace import Workspace


class LoggerCallback(Callback):
    """
    callback to log information.
    generates text at the end of each epoch.
    """
    
    def __init__(self, workspace: Workspace):
        super(LoggerCallback, self).__init__()
        self.workspace = workspace
        # build inference model using config from learning model
        self.time_train = self.time_epoch = time.time()
    
    def on_epoch_begin(self, epoch, logs=None):
        self.time_epoch = time.time()
    
    def on_epoch_end(self, epoch, logs=None):
        duration_epoch = time.time() - self.time_epoch
        print("epoch: %s, duration: %ds, loss: %.6g." % (
            epoch, duration_epoch, logs["loss"]))
        # transfer weights from learning model
        self.workspace.get_inference_model().set_weights(self.model.get_weights())
        
        # generate text
        seed = self.workspace.generate_seed()
        print(self.workspace.generate_text(seed))
    
    def on_train_begin(self, logs=None):
        print("start of training.")
        self.time_train = time.time()
    
    def on_train_end(self, logs=None):
        duration_train = time.time() - self.time_train
        print("end of training, duration: %ds." % duration_train)
        # transfer weights from learning model
        self.workspace.get_inference_model().set_weights(self.model.get_weights())
        
        # generate text
        seed = self.workspace.generate_seed()
        print(self.workspace.generate_text(seed))