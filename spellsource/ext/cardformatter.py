#!/usr/bin/python
"""
This script reformats all the cards specified inside the resources/cards directory.

The preferred formatting is two space indents, all elements on their own line. An order for the keys is specified below,
and is based on the historical ordering from metastone. Keys that are not in this order are otherwise appended to the
end in alphabetical order.

This script requires the objdict package to help it serialize to JSON  in the appropriate key order.
"""
import os
from typing import Optional

from objdict import ObjDict as OrderedDict

from .cards import write_card, iter_card_and_file_path

_VERSION = 1

_ORDER = [
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
    'secret',
    'spell',
    'chooseOneCardIds',
    'chooseBothCardId',
    'chooseOneBattlecries',
    'chooseBothBattlecry',
    'attributes',
    'collectible',
    'set',
    'fileFormatVersion'
]


def fix_cards(path: Optional[str] = None):
    if path is None:
        path = os.path.join(os.getcwd(), 'cards', 'src', 'main', 'resources', 'cards')
    for (card, filepath) in iter_card_and_file_path(start_path=path):
        if 'set' not in card:
            if 'witchwood' in filepath:
                card['set'] = 'WITCHWOOD'
        fixed_card = fix_card(card)

        write_card(fixed_card, filepath)


def fix_card(card):
    if 'race' in card:
        if card['race'] == 'MECHANICAL':
            card['race'] = 'MECH'
    if 'rarity' not in card:
        card['rarity'] = 'FREE'
    if 'id' in card:
        del card['id']
    fixed_card = fix_dict(card)
    if 'fileFormatVersion' not in fixed_card:
        fixed_card['fileFormatVersion'] = 1
    return fixed_card


def fix_list(v):
    new_v = [None] * len(v)
    for i, x in enumerate(v):
        new_v[i] = fix_value(x)
    return new_v


def fix_dict(in_dict):
    new_dict = OrderedDict()
    for k in _ORDER:
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
        return fix_dict(v)
    if isinstance(v, list):
        return fix_list(v)
    else:
        return v
