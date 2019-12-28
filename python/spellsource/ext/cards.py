import re
from collections import deque
from json import dump, load
from os import walk, path
from typing import Generator, Tuple, Dict, List

try:
    from typing import TypedDict
except ImportError:
    from mypy_extensions import TypedDict

from objdict import JsonEncoder
from objdict import ObjDict as OrderedDict

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
    'WHIZBANG': 'ANY',
    'NEUTRAL': 'ANY',
    'DREAM': 'ANY'
}


class CardDesc(TypedDict, total=False):
    id: str
    name: str
    heroPower: str
    baseManaCost: int
    type: str  # CardType
    heroClass: str
    heroClasses: List[str]
    baseAttack: int
    baseHp: int
    damage: int
    durability: int
    rarity: str  # Rarity
    race: str
    description: str
    targetSelection: str  # TargetSelection
    secret: dict
    quest: dict
    countUntilCast: int
    countByValue: bool
    battlecry: dict
    deathrattle: dict
    trigger: dict
    triggers: List[dict]
    aura: dict
    auras: List[dict]
    passiveAuras: List[dict]
    cardCostModifier: dict
    chooseOneBattlecries: List[dict]
    chooseBothBattlecry: dict
    chooseOneCardIds: List[str]
    chooseBothCardId: str
    onEquip: dict
    condition: dict
    group: List[dict]
    passiveTrigger: dict
    passiveTriggers: List[dict]
    deckTrigger: dict
    deckTriggers: List[dict]
    gameTriggers: List[dict]
    manaCostModifier: dict
    attributes: dict
    author: str
    flavor: str
    wiki: str
    collectible: bool
    set: str
    sets: List[str]
    fileFormatVersion: int
    dynamicDescription: List[dict]
    legacy: bool
    hero: str
    color: List[int]
    blackText: bool
    secondPlayerBonusCards: List[str]


def iter_card_and_file_path(start_path: str = None) -> Generator[Tuple[CardDesc, str], None, None]:
    if start_path is None:
        start_path = path.join(path.dirname(__file__), '..', '..', '..', 'cards', 'src', 'main', 'resources', 'cards')

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


def iter_cards(start_path: str = None) -> Generator[CardDesc, None, None]:
    for card, _ in iter_card_and_file_path(start_path):
        yield card


def walk_card(card: dict) -> Generator[Tuple[dict, dict, str, dict], None, None]:
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


def filepath_to_id(filepath: str) -> str:
    return path.basename(filepath).replace('.json', '')


db: Dict[str, CardDesc] = {filepath_to_id(filepath): {'id': filepath_to_id(filepath), **card} for (card, filepath) in
                           iter_card_and_file_path()}
