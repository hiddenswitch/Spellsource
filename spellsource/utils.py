import abc
import json
from collections import deque
from math import factorial
from multiprocessing import Queue
from typing import Callable, Sequence, Union

from spellsource.behaviour import Behaviour
from spellsource.context import Context


def simulate(context: Context, decks: Sequence[str] = (), number: int = 1,
             behaviours: Sequence[Union[str, Behaviour, Callable[[], Behaviour]]] = (), mirrors: bool = False,
             reduce: bool = True):
    ctx = context
    PythonBridge = ctx.PythonBridge
    ArrayList = ctx.ArrayList
    
    behaviours = list(behaviours)
    for i, behaviour in enumerate(behaviours):
        if isinstance(behaviour, str):
            behaviours[i] = PythonBridge.getBehaviourByName(behaviour)
            assert behaviours[i] is not None
        elif isinstance(behaviour, Behaviour):
            behaviours[i] = _Supplier(lambda: behaviour.clone().wrap(ctx))
        elif isinstance(behaviour, Callable):
            behaviours[i] = _Supplier(lambda: behaviour().wrap(ctx))
    
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
        implements = ['com.hiddenswitch.spellsource.impl.util.SimulationResultGenerator']


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
