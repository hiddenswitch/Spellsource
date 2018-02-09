from json import dump, load
from os import walk, path
from collections import deque

try:
    import objdict
except ImportError:
    import pip

    pip.main(['install', '--user', 'objdict'])

from objdict import ObjDict as OrderedDict
from objdict import JsonEncoder


def iter_cards(start_path=None):
    if start_path is None:
        start_path = path.join(path.dirname(__file__), 'src/main/resources/cards')

    for root, dirnames, filenames in walk(start_path):
        for filename in filenames:
            if '.json' not in filename:
                continue
            filepath = path.join(root, filename)
            with open(filepath) as fp:
                try:
                    card = load(fp, object_pairs_hook=OrderedDict)
                    yield (card, filepath)
                except ValueError as ex:
                    print 'Parsing error in ', filepath
                    continue


def walk_card(card):
    queue = deque([(card, {}, None, card)])

    while len(queue) > 0:
        (next_dict, parent, key, inherits) = queue.popleft()
        yield (next_dict, parent, key, inherits)
        for k, v in next_dict.iteritems():

            if isinstance(v, dict) or isinstance(v, OrderedDict):
                copy = inherits.copy()
                copy.update(v)
                queue.append((v, next_dict, k, copy))
            elif isinstance(v, list):
                for item in v:
                    if isinstance(item, dict) or isinstance(item, OrderedDict):
                        copy = inherits.copy()
                        copy.update(item)
                        queue.append((item, next_dict, k, copy))


def write_card(card, filepath):
    with open(filepath, 'w') as fp:
        dump(card, fp, indent=2, separators=(',', ': '), cls=JsonEncoder)
