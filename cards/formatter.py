#!/usr/bin/python
from collections import Counter
from json import dump, load
from os import walk, path

try:
    import objdict
except ImportError:
    import pip

    pip.main(['install', '--user', 'objdict'])

from objdict import ObjDict as OrderedDict
from objdict import JsonEncoder

ORDER = [
    'class',
    'target',
    'value',
    'id',
    'name',
    'heroPower',
    'baseManaCost',
    'type',
    'heroClass',
    'heroClasses',
    'group',
    'baseAttack',
    'damage',
    'durability',
    'baseHp',
    'rarity',
    'race',
    'description',
    'cardCostModifier',
    'trigger',
    'triggers',
    'quest',
    'passiveTrigger',
    'passiveTriggers',
    'deckTrigger',
    'countUntilCast',
    'aura',
    'condition',
    'battlecry',
    'onEquip',
    'deathrattle',
    'onUnequip',
    'targetSelection',
    'eventTrigger',
    'eventTriggers',
    'spell',
    'options',
    'bothOptions',
    'attributes',
    'collectible',
    'set',
    'fileFormatVersion'
]


def main():
    locations = Counter()

    for root, dirnames, filenames in walk(path.join(path.dirname(__file__), 'src/main/resources/cards')):
        for filename in filenames:
            if '.json' not in filename:
                continue
            filepath = path.join(root, filename)
            with open(filepath) as fp:
                try:
                    card = load(fp, object_pairs_hook=OrderedDict)
                except ValueError as ex:
                    print 'Parsing error in ', filepath
                    continue
                for i, (k, v) in enumerate(card.iteritems()):
                    locations.update([(k, i)])

                fixed_card = sort_dict(card)
            with open(filepath, 'w') as fp:
                dump(fixed_card, fp, indent=2, separators=(',', ': '), cls=JsonEncoder)

    print 'Frequencies observed:'
    print locations.__repr__().replace(':', '\n')


def sort_list(v):
    new_v = [None] * len(v)
    for i, x in enumerate(v):
        new_v[i] = fix_value(x)
    return new_v


def sort_dict(in_dict):
    new_dict = OrderedDict()
    for k in ORDER:
        if k not in in_dict:
            continue
        new_dict[k] = fix_value(in_dict[k])
    for k in sorted(in_dict.keys()):
        if k in new_dict:
            continue
        new_dict[k] = fix_value(in_dict[k])

    return new_dict


def fix_value(v):
    if isinstance(v, dict) or isinstance(v, OrderedDict):
        return sort_dict(v)
    if isinstance(v, list):
        return sort_list(v)
    else:
        return v


if __name__ == '__main__':
    main()
