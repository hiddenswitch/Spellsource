import abc
import json
from collections import deque
from math import factorial
from multiprocessing import Queue
from typing import Callable, Sequence, Union

from spellsource.behaviour import Behaviour, _WrapperBehaviour
from spellsource.context import Context


def simulate(context: Context, decks: Sequence[str] = (), number: int = 1,
             behaviours: Sequence[Union[str, Behaviour, _WrapperBehaviour, Callable[[], Behaviour]]] = (),
             mirrors: bool = False,
             reduce: bool = True):
    """
    Run a simulation using AIs of a given deck matchup.

    Deck lists are provided as a sequence of Hearthstone deck strings or community deck format strings.

    A community deck format string looks like this:

    Name: Big Druid
    Class: Druid
    Format: Standard
    2x Biology Project
    1x Lesser Jasper Spellstone
    2x Naturalize
    2x Wild Growth
    1x Drakkari Enchanter
    1x Greedy Sprite
    2x Branching Paths
    2x Bright-Eyed Scout
    2x Nourish
    2x Spreading Plague
    1x Malfurion the Pestilent
    1x Gloop Sprayer
    2x Primordial Drake
    1x The Lich King
    2x Dragonhatcher
    1x Hadronox
    1x Master Oakheart
    2x Sleepy Dragon
    1x Ysera
    1x Tyrantus

    This example uses the tqdm package to provide a progress bar. This will simulate 10 games using the advanced
    GameStateValueBehaviour AI between a Token Druid deck and Deathrattle Hunter.

    >>> from tqdm import tqdm
    >>> from spellsource.context import Context
    >>> TOKEN_DRUID = 'AAECAZICBPcD9fwCm8sCmdMCDZjSAuQI5gWe0gL9Ao7QAkDX7wLb0wJfoM0C4vgCh84CAA=='
    >>> DEATHRATTLE_HUNTER = 'AAECAR8Klwic4gLTxQKA8wKggAOG0wLL7ALGwgK26gL4CArh4wKNAZzNAqvCAt4FufgC2MIC3dICi+EC8vECAA=='
    >>> decks = [TOKEN_DRUID, DEATHRATTLE_HUNTER]
    >>> behaviours = ['GameStateValueBehaviour', 'GameStateValueBehaviour']
    >>> with Context() as context:
    >>>     simulation_results = list(tqdm(simulate(context=context, decks=decks, number=10, behaviours=behaviours)))

    :param context: A Spellsource context created with spellsource.context.Context()
    :param decks: A sequence of deck lists in a community format or a Hearthstone deck code.
    :param number: The number of each matchup (pair of decks) to play. Defaults to 1.
    :param behaviours: The behaviours to use to model the players, as a tuple of strings naming existing methods,
    Behaviour classes.
    To determine deck strength in the hands of any player, model random players:
    >>> ('PlayRandomBehaviour', 'PlayRandomBehaviour')
    Otherwise, to model a pretty strong aggressive player, use:
    >>> ('GameStateValueBehaviour', 'GameStateValueBehaviour')
    Note that GameStateValueBehaviour is very processor intensive. For some matches, it may take as long as one
    minute per game to resolve.
    :param mirrors: When True, indicates that mirror matchups should be played. Between AI opponents,
    this will almost always result in 50 percent winrates for these matchups. Defaults to False.
    :param reduce: When True, indicates that all the per-game data should be summed and collected together. When
    False, reports statistics from each match separately. Defaults to True.
    :return: Simulation results, containing win rates and various statistics about how the games in each match up
    were played out
    """
    ctx = context
    PythonBridge = ctx.PythonBridge
    ArrayList = ctx.ArrayList

    behaviours = list(behaviours)
    for i, behaviour in enumerate(behaviours):
        if isinstance(behaviour, str):
            behaviours[i] = PythonBridge.getBehaviourByName(behaviour)
            assert behaviours[i] is not None
        elif isinstance(behaviour, Behaviour):
            behaviours[i] = _Supplier(lambda x=behaviour: x.clone().wrap(ctx))
        elif isinstance(behaviour, _WrapperBehaviour):
            behaviours[i] = _Supplier(lambda x=behaviour: x.clone())
        elif isinstance(behaviour, Callable):
            behaviours[i] = _Supplier(lambda x=behaviour: x().wrap(ctx))

    f = factorial
    estimated_length = f(len(decks)) // f(2) // f(len(decks) - 2)
    if mirrors:
        estimated_length += len(decks)
    if not reduce:
        estimated_length *= number

    behaviours_java = ArrayList()
    for b in behaviours:
        behaviours_java.add(b)

    decks_java = ArrayList()
    for d in decks:
        decks_java.add(d)
    generator = SimulationGenerator(estimated_length, ctx)
    # Temporarily disable converters
    old_converters = ctx._gateway._gateway_client.converters
    try:
        ctx._gateway._gateway_client.converters = None
        generator.job_id = PythonBridge.simulate(generator, decks_java, number, behaviours_java, mirrors, reduce)
    finally:
        ctx._gateway._gateway_client.converters = old_converters

    return generator


class _JavaSupplier(abc.ABC):
    @abc.abstractmethod
    def get(self):
        pass

    class Java:
        implements = ['java.util.function.Supplier']


class _Supplier(_JavaSupplier):
    def __init__(self, constructor: Callable[[], Behaviour]):
        self.constructor = constructor

    def get(self):
        return self.constructor()


class _JavaSimulationResultGenerator(abc.ABC):
    @abc.abstractmethod
    def offer(self, obj: str):
        pass

    @abc.abstractmethod
    def stopIteration(self):
        pass

    class Java:
        implements = ['com.hiddenswitch.framework.impl.SimulationResultGenerator']


class SimulationGenerator(_JavaSimulationResultGenerator):
    def __init__(self, estimated_length: int, context: Context):
        self._length = estimated_length
        self._queue = Queue()
        self._retrieved = deque()
        self._ctx = context
        self.job_id = 0

    def offer(self, obj: str):
        self._queue.put(obj, block=False)

    def stopIteration(self):
        self._queue.put(StopIteration(), block=False)

    def __len__(self):
        return self._length

    def __iter__(self):
        if len(self._retrieved) > 0:
            for obj in self._retrieved:
                yield obj
            return
        try:
            while True:
                obj = self._queue.get()
                if isinstance(obj, StopIteration):
                    self.job_id = 0
                    return
                if isinstance(obj, str):
                    # Parse as json
                    obj = json.loads(obj)
                self._retrieved.append(obj)
                yield obj
        except InterruptedError as ex:
            self._kill()
            return
        except KeyboardInterrupt as ex:
            self._kill()
            return

    def _kill(self):
        if self._ctx is not None and self._ctx.is_open() and self.job_id != 0:
            self._ctx.PythonBridge.terminate(self.job_id)
        self.job_id = 0

    def __del__(self):
        self._kill()
