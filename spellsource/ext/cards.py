from json import dump, load
from os import walk, path
from collections import deque
import re
from typing import Generator, Tuple

from mypy_extensions import TypedDict
from objdict import ObjDict as OrderedDict
from objdict import JsonEncoder

CLASS_MAPPING = {
    'DRUID': 'BROWN',
    'HUNTER': 'GREEN',
    'MAGE': 'BLUE',
    'PALADIN': 'GOLD',
    'PRIEST': 'WHITE',
    'ROGUE': 'BLACK',
    'SHAMAN': 'SILVER',
    'WARLOCK': 'VIOLET',
    'WARRIOR': 'RED',
    'DEATHKNIGHT': 'SPIRIT',
    'NEUTRAL': 'ANY',
    'DREAM': 'ANY'
}

def iter_card_and_file_path(start_path: str = None) -> Generator[Tuple[dict, str], None, None]:
    if start_path is None:
        start_path = path.join(path.dirname(__file__), '../../cards/src/main/resources/cards')

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
                    print('Parsing error in %s' % (filepath))
                    continue


def iter_cards(start_path: str = None) -> Generator[dict, None, None]:
    for card, _ in iter_card_and_file_path(start_path):
        yield card


def walk_card(card: dict):
    queue = deque([(card, {}, None, card)])

    while len(queue) > 0:
        (next_dict, parent, key, inherits) = queue.popleft()
        yield (next_dict, parent, key, inherits)
        for k, v in next_dict.items():

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


def write_card(card: dict, filepath: str):
    with open(filepath, 'w') as fp:
        dump(card, fp, indent=2, separators=(',', ': '), cls=JsonEncoder)


def name_to_id(name='', card_type=''):
    return card_type.lower() + "_" + re.sub('[^a-zA-Z0-9]', '_', name.lower())
