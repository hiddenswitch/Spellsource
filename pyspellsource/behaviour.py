import abc
from typing import List
from .context import Context


# Stubs
class _JavaBehaviour:
    pass


class _WrapperBehaviour(_JavaBehaviour):
    pass


class GameAction:
    pass


class Entity():
    def getAttributeValue(self, attribute) -> int:
        pass


class Card(Entity):
    pass


class Actor(Entity):
    def getAttack(self) -> int:
        pass


class Minion(Actor):
    pass


class Weapon(Actor):
    def getWeaponDamage(self) -> int:
        pass


class Hero(Actor):
    def getEffectiveHp(self) -> int:
        pass
    
    def getHeroClass(self):
        pass
    
    def getWeapon(self) -> Weapon:
        pass


class Player:
    def getBehaviour(self) -> _JavaBehaviour:
        pass
    
    def setBehaviour(self, behaviour: _WrapperBehaviour):
        pass
    
    def getMinions(self) -> List[Minion]:
        pass
    
    def getHero(self) -> Hero:
        pass


class GameContext:
    def getPlayer1(self) -> Player:
        pass
    
    def getPlayer2(self) -> Player:
        pass
    
    def getPlayer(self, idx: int) -> Player:
        pass
    
    def getPlayers(self) -> List[Player]:
        pass
    
    def getOpponent(self, player: Player) -> Player:
        pass


class Behaviour:
    pass


class _JavaBehaviour(abc.ABC):
    @abc.abstractmethod
    def clone(self):
        pass
    
    @abc.abstractmethod
    def getName(self):
        pass
    
    @abc.abstractmethod
    def mulligan(self, context, player, cards):
        pass
    
    @abc.abstractmethod
    def onGameOver(self, context, playerId, winningPlayerId):
        pass
    
    @abc.abstractmethod
    def requestAction(self, context, player, validActions):
        pass
    
    class Java:
        implements = ['net.demilich.metastone.game.behaviour.Behaviour']


class _WrapperBehaviour(_JavaBehaviour):
    def __init__(self, python_behaviour: Behaviour, context: Context):
        self.delegate = python_behaviour
        self.context = context
    
    def clone(self):
        return _WrapperBehaviour(self.delegate.clone(), self.context)
    
    def getName(self):
        return self.delegate.get_name()
    
    def mulligan(self, context, player, cards):
        java_list = self.context._gateway.jvm.java.util.ArrayList()
        for v in self.delegate.mulligan(context, player, cards):
            java_list.add(v)
        return java_list
    
    def requestAction(self, context, player, validActions):
        return self.delegate.request_action(context, player, validActions)
    
    def onGameOver(self, context, playerId, winningPlayerId):
        pass


class Behaviour(abc.ABC):
    @abc.abstractmethod
    def clone(self) -> Behaviour:
        pass
    
    @abc.abstractmethod
    def get_name(self) -> str:
        pass
    
    @abc.abstractmethod
    def mulligan(self, context: GameContext, player: Player, cards: List[Card]) -> List[Card]:
        pass
    
    @abc.abstractmethod
    def request_action(self, context: GameContext, player: Player, valid_actions: List[GameAction]) -> GameAction:
        pass
    
    def wrap(self, context: Context) -> _WrapperBehaviour:
        '''
        Returns a specially-wrapped instance of this behaviour that can be passed as an argument to
        setBehaviour. For example:
        
        inst = MyBehaviour()
        with Context() as ctx:
            game_context = ctx.GameContext.fromTwoRandomDecks()
            game_context.getPlayer(0).setBehaviour(inst.wrap(ctx))
            game_context.play()
            assert game_context.updateAndGetGameOver()
            
        :param context: The Context object hosting a Spellsource game engine.
        :return: A wrapped behaviour object.
        '''
        return _WrapperBehaviour(context=context, python_behaviour=self)
