import copy
import json
import os
import urllib.request
from typing import Optional, Union, Mapping

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
    card_attack = card['attack']
    card_health = card['health']
    if 'type' in card:
        card_type = card['type'].upper()
    else:
        if card_attack == '0' and card_health == '0':
            card_type = 'SPELL'
        else:
            card_type = 'MINION'
    if card_type == 'HEROPOWER':
        card_type = 'HERO_POWER'
    out = {}
    out['name'] = card['cardname']
    out['set'] = set
    out['description'] = card['cardtext']
    description = out['description'].lower()
    out['type'] = card_type
    out['rarity'] = card['rarity'].upper()
    if card['class'].upper() in CLASS_MAPPING:
        out['heroClass'] = CLASS_MAPPING[card['class'].upper()]
    else:
        out['heroClass'] = hero_class
    out['baseManaCost'] = int(card['mana'])

    enrich_from_description(out, description, card_attack, card_health, card_type)
    out['collectible'] = True
    out['fileFormatVersion'] = 1
    if kwargs is not None:
        out.update(kwargs)
    return out


def enrich_from_description(card_dict: Mapping,
                            description: Optional[str] = None,
                            card_attack: Optional[Union[str, int]] = None,
                            card_health: Optional[Union[str, int]] = None,
                            card_type: Optional[str] = None,
                            hero_class: Optional[str] = None):
    if description is None:
        if 'description' in card_dict:
            description = card_dict['description']

    # Remove Hearthcards tags and newlines from description
    card_dict['description'] = description \
        .replace('[b]', '') \
        .replace('[/b]', '') \
        .replace('[i]', '') \
        .replace('[/i]', '') \
        .replace('\r\n', ' ') \
        .replace('\n', ' ')

    description = description.lower()

    if card_type is None:
        if 'type' in card_dict:
            card_type = card_dict['type']
        if 'cardType' in card_dict:
            card_type = card_dict['cardType']

    if card_attack is None:
        if 'baseAttack' in card_dict:
            card_attack = card_dict['baseAttack']
        elif 'damage' in card_dict:
            card_attack = card_dict['damage']

    if card_health is None:
        if 'baseHp' in card_dict:
            card_health = card_dict['baseHp']
        elif 'durability' in card_dict:
            card_health = card_dict['durability']

    card_dict['attributes'] = attributes = {}
    spell = {
        'class': 'BuffSpell',
        'target': 'SELF',
        'attackBonus': 0,
        'hpBonus': 0
    }
    spells = []
    if re.search(r'costs \(\d+\) (more)|(less)', description):
        card_dict['manaCostModifier'] = {
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
        if 'whenever you draw' not in description:
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
    if card_type == 'SPELL':
        if 'if' in description:
            spell['condition'] = condition
        if spell['class'] == 'DamageSpell' or spell['class'] == 'DestroySpell':
            card_dict['targetSelection'] = 'ANY'
        elif spell['class'] == 'BuffSpell':
            card_dict['targetSelection'] = 'MINIONS'
        else:
            card_dict['targetSelection'] = 'NONE'
        card_dict['spell'] = spell
    elif card_type is not None and card_attack is not None and card_health is not None:
        if card_type == 'MINION':
            card_dict['baseAttack'] = int(card_attack)
            card_dict['baseHp'] = int(card_health)
        elif card_type == 'WEAPON':
            card_dict['damage'] = int(card_attack)
            card_dict['durability'] = int(card_health)
    if card_type == 'MINION' or card_type == 'WEAPON':
        if 'deathrattle' in description:
            card_dict['deathrattle'] = spell
            attributes['DEATHRATTLES'] = True
        if 'battlecry' in description:
            card_dict['battlecry'] = {
                'targetSelection': 'NONE',
                'spell': spell
            }
            attributes['BATTLECRY'] = True
        if 'inspire' in description:
            card_dict['trigger'] = {
                'eventTrigger': {
                    'class': 'InspireTrigger',
                    'targetPlayer': 'SELF'
                },
                'spell': spell
            }
        if 'start of game' in description:
            card_dict['gameTriggers'] = [
                {
                    'eventTrigger': {
                        'class': 'GameStartTrigger',
                        'targetPlayer': 'SELF'
                    },
                    'spell': spell
                }
            ]
            if 'if' in description:
                card_dict['gameTriggers'][0]['eventTrigger']['spell']['condition'] = condition
        if 'whenever' in description \
                or 'after' in description \
                or 'at the end' in description \
                or 'at the start' in description \
                or 'after' in description \
                or 'each turn' in description:
            if 'you draw' in description:
                trigger_class = 'CardDrawnTrigger'
            elif 'you summon' in description:
                trigger_class = 'AfterMinionSummonedTrigger' if 'after' in description else 'MinionSummonedTrigger'
            elif 'you play a card' in description:
                trigger_class = 'AfterCardPlayedTrigger' if 'after' in description else 'CardPlayedTrigger'
            elif 'you play a minion' in description:
                trigger_class = 'AfterMinionPlayedTrigger' if 'after' in description else 'MinionPlayedTrigger'
            elif 'you cast' in description:
                trigger_class = 'AfterSpellCastedTrigger' if 'after' in description else 'SpellCastedTrigger'
            else:
                trigger_class = 'TurnEndTrigger'

            card_dict['trigger'] = {
                'eventTrigger': {
                    'class': trigger_class,
                    'targetPlayer': 'SELF' if 'you' in description else 'OPPONENT'
                },
                'spell': spell
            }
            if 'if' in description:
                card_dict['trigger']['eventTrigger']['fireCondition'] = condition
        elif 'battlecry' in card_dict and 'if' in description:
            card_dict['battlecry']['condition'] = condition
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
        card_dict['countUntilCast'] = 5
        card_dict['quest'] = {
            'class': 'CardPlayedTrigger',
            'targetPlayer': 'SELF'
        }
        card_dict['spell'] = {
            'class': 'ReceiveCardSpell',
            'card': 'spell_the_coin'
        }
    elif 'secret:' in description:
        card_dict['secret'] = {
            'class': 'CardPlayedTrigger',
            'targetPlayer': 'SELF'
        }
    if hero_class is not None:
        card_dict['heroClass'] = hero_class
    return card_dict


def write_set_stubs(set_id: int, dest_dir: str, hero_class: str):
    basedir = os.path.join(dest_dir, 'set_' + str(set_id))
    try:
        os.makedirs(basedir)
    except:
        pass
    for card in HearthcardsCardDownloader(set_num=set_id):
        out_card = fix_dict(from_hearthcard_to_spellsource(card, hero_class=hero_class))
        write_card(card=out_card,
                   filepath=os.path.join(basedir, name_to_id(
                       out_card['name'],
                       out_card['type']) + '.json'))
