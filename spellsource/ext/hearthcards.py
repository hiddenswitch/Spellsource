import copy
import json
import os
import urllib.request

from .cardformatter import fix_dict
from .cards import *


class HearthcardsCardDownloader(object):
    def __init__(self, set_num=None):
        """
        Downloads all the cards from the Hearthcards gallery, or downloads a specific set.
        
        Behaves like a generator.
        :param set_num: When set, downloads a specific set.
        """
        self._item_id = set_num
        if set_num is not None:
            self._getter = self._get_set_page
        else:
            self._getter = self._get_gallery_page
        self._pages = {}
        self._get_sizes()

    def __iter__(self):
        for x in range(1, self._max_pages + 1):
            if x not in self._pages:
                self._pages[x] = self._getter(page=x)
            for card in self._pages[x]['cards']:
                yield card

    def __len__(self):
        return self._max_cards

    def _get_sizes(self):
        data = self._getter(page=1)
        self._pages[1] = data
        self._max_pages = data['main']['maxpage'] if 'maxpage' in data['main'] else 1
        self._max_cards = int(data['main']['total']) \
            if 'total' in data['main'] \
            else int(data['main']['cardsAmount'])

    def _get_gallery_page(self, page=1):
        link = 'http://www.hearthcards.net/gallery/json_custom.php?class=&mana=all&page=' + str(
            page) + '&collection=0&sort=date&deckid=&language=en&search=&f=1'
        f = urllib.request.urlopen(link)
        data = json.loads(f.read())
        return data

    def _get_set_page(self, page=1):
        link = 'http://www.hearthcards.net/setsandclasses/ajax/ajax_set_hero.php?setid=' + str(
            self._item_id)
        f = urllib.request.urlopen(link)
        data = json.loads(f.read())
        return data


def from_hearthcard_to_spellsource(card, set='CUSTOM', hero_class='WHITE', **kwargs):
    """
    Converts a Hearthcard card dict to a Spellsource card,
    :param card: The Hearthcards card dict to convert
    :param set: The set, defaulting to 'CUSTOM'
    :param hero_class: The hero class override
    :param kwargs: All other overrides
    :return:
    """
    if 'type' in card:
        card_type = card['type'].upper()
    else:
        if card['attack'] == '0' and card['health'] == '0':
            card_type = 'SPELL'
        else:
            card_type = 'MINION'
    if card_type == 'HEROPOWER':
        card_type = 'HERO_POWER'
    out = {}
    out['name'] = card['cardname']
    out['set'] = set
    out['description'] = card['cardtext']
    out['type'] = card_type
    out['attributes'] = attributes = {}

    out['rarity'] = card['rarity'].upper()
    if card['class'].upper() in CLASS_MAPPING:
        out['heroClass'] = CLASS_MAPPING[card['class'].upper()]
    else:
        out['heroClass'] = hero_class
    out['baseManaCost'] = int(card['mana'])
    description = out['description'].lower()

    spell = {
        'class': 'BuffSpell',
        'target': 'SELF',
        'attackBonus': 0,
        'hpBonus': 0
    }
    spells = []
    if re.search(r'costs \(\d+\) (more)|(less)', description):
        out['manaCostModifier'] = {
            'class': 'PlayerAttributeValueProvider',
            'playerAttribute': 'HAND_COUNT'
        }
    if 'give' in description or 'gain' in description:
        spell = {
            'class': 'BuffSpell',
            'target': 'SELF',
            'attackBonus': 0,
            'hpBonus': 0
        }
        spells += [copy.deepcopy(spell)]
    if 'deal' in description and 'damage' in description:
        spell = {
            'class': 'DamageSpell',
            'value': 1
        }
        spells += [copy.deepcopy(spell)]
    if 'recruit' in description and 'silver hand' not in description:
        spell = {
            'class': 'RecruitSpell',
            'cardLocation': 'DECK',
            'cardFilter': {
                'class': 'CardFilter',
                'cardType': 'MINION',
                'manaCost': 1
            }
        }
        spells += [copy.deepcopy(spell)]
    if 'discard' in description:
        spell = {
            'class': 'DiscardSpell',
            'value': 1
        }
        spells += [copy.deepcopy(spell)]
    if 'add' in description and 'to' in description and 'hand' in description:
        spell = {
            'class': 'ReceiveCardSpell',
            'card': 'spell_mirror_image'
        }
        spells += [copy.deepcopy(spell)]
    if 'summon' in description:
        spell = {
            'class': 'SummonSpell',
            'card': 'minion_bloodfen_raptor'
        }
        spells += [copy.deepcopy(spell)]
    if 'draw' in description:
        spell = {
            'class': 'DrawCardSpell',
            'value': 1
        }
        spells += [copy.deepcopy(spell)]
    if 'shuffle' in description:
        spell = {
            'class': 'ShuffleToDeckSpell',
            'value': 1,
            'card': 'minion_bloodfen_raptor'
        }
        spells += [copy.deepcopy(spell)]
    if 'discover' in description:
        spell = {
            'class': 'DiscoverSpell',
            'cardSource': {'class': 'CatalogueSource'},
            'cardFilter': {
                'class': 'CardFilter',
                'cardType': 'MINION'
            },
            'spell': {
                'class': 'ReceiveCardSpell'
            }
        }
        spells += [copy.deepcopy(spell)]
    if 'equip' in description:
        spell = {
            'class': 'EquipWeaponSpell',
            'card': 'weapon_fiery_war_axe'
        }
        spells += [copy.deepcopy(spell)]
    if 'destroy' in description:
        spell = {
            'class': 'DestroySpell'
        }
        spells += [copy.deepcopy(spell)]
    if 'transform' in description:
        spell = {
            'class': 'TransformToRandomMinionSpell',
            'cardSource': {
                'class': 'CatalogueSource'
            },
            'cardFilter': {
                'class': 'CardFilter',
                'cardType': 'MINION'
            }
        }
        spells += [copy.deepcopy(spell)]
    if 'freeze' in description:
        spell = {
            'class': 'AddAttributeSpell',
            'attribute': 'FROZEN'
        }
        spells += [copy.deepcopy(spell)]
    if 'random' in description:
        spell['randomTarget'] = True
    target_example = None
    if 'friendly' in description or 'your' in description:
        target_example = 'FRIENDLY_'
    elif 'enemy' in description:
        target_example = 'ENEMY_'
    if target_example is not None:
        if 'minion' in description:
            target_example += 'MINIONS'
        elif 'hero' in description:
            target_example += 'HERO'
        else:
            target_example += 'CHARACTERS'
        spell['target'] = target_example

    if len(spells) > 1:
        spell = {
            'class': 'MetaSpell',
            'spells': spells
        }
    condition = {
        'class': 'PlayedLastTurnCondition'
    }

    if 'combo' in description:
        condition = {
            'class': 'ComboCondition'
        }

    if card_type == 'MINION':
        out['baseAttack'] = int(card['attack'])
        out['baseHp'] = int(card['health'])
    elif card_type == 'WEAPON':
        out['damage'] = int(card['attack'])
        out['durability'] = int(card['health'])
    elif card_type == 'SPELL':
        if 'if' in description:
            spell['condition'] = condition
        if spell['class'] == 'DamageSpell' or spell['class'] == 'DestroySpell':
            out['targetSelection'] = 'ANY'
        elif spell['class'] == 'BuffSpell':
            out['targetSelection'] = 'MINIONS'
        else:
            out['targetSelection'] = 'NONE'
        out['spell'] = spell

    if card_type == 'MINION' or card_type == 'WEAPON':
        if 'deathrattle' in description:
            out['deathrattle'] = spell
            attributes['DEATHRATTLES'] = True
        if 'battlecry' in description:
            out['battlecry'] = {
                'targetSelection': 'NONE',
                'spell': spell
            }
            attributes['BATTLECRY'] = True
        if 'inspire' in description:
            out['trigger'] = {
                'eventTrigger': {
                    'class': 'InspireTrigger',
                    'targetPlayer': 'SELF'
                },
                'spell': spell
            }
        if 'start of game' in description:
            out['gameTriggers'] = [
                {
                    'eventTrigger': {
                        'class': 'GameStartTrigger',
                        'targetPlayer': 'SELF'
                    },
                    'spell': spell
                }
            ]
            if 'if' in description:
                out['gameTriggers'][0]['eventTrigger']['spell']['condition'] = condition
        if 'whenever' in description \
                or 'at the end' in description \
                or 'at the start' in description \
                or 'after' in description \
                or 'each turn' in description:
            out['trigger'] = {
                'eventTrigger': {
                    'class': 'TurnEndTrigger',
                    'targetPlayer': 'SELF'
                },
                'spell': spell
            }
            if 'if' in description:
                out['trigger']['eventTrigger']['fireCondition'] = condition
        elif 'battlecry' in out and 'if' in description:
            out['battlecry']['condition'] = condition
        if 'taunt' in description:
            attributes['TAUNT'] = True
        if 'stealth' in description:
            attributes['STEALTH'] = True
        if 'charge' in description:
            attributes['CHARGE'] = True
        if 'divine shield' in description:
            attributes['DIVINE_SHIELD'] = True
        if 'windfury' in description:
            attributes['WINDFURY'] = True
        if 'spell damage' in description:
            attributes['SPELL_DAMAGE'] = 1
        if 'rush' in description:
            attributes['RUSH'] = True
        if 'poisonous' in description:
            attributes['POISONOUS'] = True
        if 'lifesteal' in description:
            attributes['LIFESTEAL'] = True
    if 'overload' in description:
        attributes['OVERLOAD'] = 2
    if 'echo' in description:
        attributes['ECHO'] = True
    if 'quest:' in description:
        out['countUntilCast'] = 5
        out['quest'] = {
            'class': 'CardPlayedTrigger',
            'targetPlayer': 'SELF'
        }
        out['spell'] = {
            'class': 'ReceiveCardSpell',
            'card': 'spell_the_coin'
        }
    elif 'secret:' in description:
        out['secret'] = {
            'class': 'CardPlayedTrigger',
            'targetPlayer': 'SELF'
        }
    out['collectible'] = True
    out['fileFormatVersion'] = 1
    if kwargs is not None:
        out.update(kwargs)
    return out


def write_set_stubs(set_id: int, dest_dir: str):
    basedir = os.path.join(dest_dir, 'set_' + str(set_id))
    try:
        os.makedirs(basedir)
    except:
        pass
    for card in HearthcardsCardDownloader(set_num=set_id):
        out_card = fix_dict(from_hearthcard_to_spellsource(card, hero_class='SILVER'))
        write_card(card=out_card,
                   filepath=os.path.join(basedir, name_to_id(
                       out_card['name'],
                       out_card['type']) + '.json'))


if __name__ == '__main__':
    import argparse

    parser = argparse.ArgumentParser()
    parser.add_argument('-s', '--set', required=True,
                        help='The set to generate stubs for based on the Hearthcards set ID')
    parser.add_argument('-d', '--directory', required=False,
                        default='./cards/src/main/resources/staging/hearthcards',
                        help='The directory to save the cards to')
    args = parser.parse_args()
    assert 'set' in args
    set_id = int(args.set)
    assert set_id is not None and set_id != 0
    write_set_stubs(set_id, dest_dir=args.directory)
