from spellsource.behaviour import Behaviour


class PlayRandomBehaviour(Behaviour):
    
    def __init__(self, state=None, seed=None):
        from random import Random
        
        super(PlayRandomBehaviour, self).__init__()
        self.random = Random(seed)
        if state is not None:
            self.random.setstate(state)
    
    def clone(self):
        state = self.random.getstate()
        return PlayRandomBehaviour(state=state)
    
    def get_name(self):
        return 'Python test behaviour'
    
    def mulligan(self, context, player, cards):
        return []
    
    def request_action(self, context, player, valid_actions):
        return self.random.choice(valid_actions)
