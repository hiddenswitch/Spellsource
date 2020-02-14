from typing import List
from itertools import groupby, chain

from .cards import CardDesc


def generate_deck(sets: set = frozenset(['SPELLSOURCE_BASIC']),
                  hero_classes: set = frozenset(['ANY']),
                  card_types: set = frozenset(['MINION', 'SPELL'])):
    from .cards import db
    from random import sample
    # Midrange mana curve
    costs = [
        (0, 0),
        (1, 2),
        (2, 5),
        (3, 6),
        (4, 7),
        (5, 5),
        (6, 3),
        (7, 2)
    ]

    assert sum(quantity for (_, quantity) in costs) == 30

    mana_cost_keyer = lambda x: x['baseManaCost'] if 'baseManaCost' in x else 0
    spellsource_core = sorted(
        [card for card in db.values() if
         'set' in card and card['set'] in sets and 'collectible' in card and card['collectible'] and card['heroClass'] in hero_classes and card[
             'type'] in card_types],
        key=mana_cost_keyer)
    spellsource_core_by_cost = {cost: list(group) for (cost, group) in groupby(spellsource_core, key=mana_cost_keyer)}

    deck_size = 30
    cards = []
    for (cost, quantity) in costs[:-1]:
        if quantity == 0:
            continue
        cards += [card['id'] for card in sample(spellsource_core_by_cost[cost], quantity)]

    (max_cost, max_quantity) = costs[-1]
    assert len(cards) == deck_size - max_quantity
    last_group: List[CardDesc] = list(chain.from_iterable(
        group for cost, group in spellsource_core_by_cost.items() if cost >= max_cost))

    cards += [card['id'] for card in sample(last_group, max_quantity)]
    assert len(cards) == deck_size
    return cards
