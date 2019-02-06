import typing
import unittest
from collections import Counter
from os.path import join, dirname, abspath

from autoboto.services.rekognition.shapes import DetectTextResponse, TextTypes
from nltk.translate.bleu_score import sentence_bleu

from ..ext.image2card import RekognitionGenerator, SpellsourceCardDescGenerator, PageToImages


def _base_test(test_case: unittest.TestCase, cases: typing.Mapping[str, typing.Any]):
    actuals = list(SpellsourceCardDescGenerator(*RekognitionGenerator(*cases.keys())))
    for actual, expected in zip(actuals, cases.values()):
        actual_description = actual['description']
        expected_description = expected['description']
        del actual['description']
        del expected['description']
        test_case.assertEqual(actual, expected)
        bleu_4_score = sentence_bleu([expected_description.split(' ')], actual_description.split(' '),
                                     weights=(0.25, 0.25, 0.25, 0.25))
        test_case.assertGreaterEqual(bleu_4_score, 0.7)


class Image2CardTest(unittest.TestCase):
    def test_rekognition_generator(self):
        local_file = join(dirname(abspath(__file__)), 'test_image.png')
        remote_file = 'http://www.hearthcards.net/cards_thumb/dc309bff.png'
        (local_result, remote_result) = tuple(
            RekognitionGenerator(local_file, remote_file))  # type: DetectTextResponse, DetectTextResponse
        # Check that 100% of the words appear somewhere in the response
        local_text = '0 1 1 Hoardling of Tolin Invoke (3): Add two copies of a card from your deck to your hand. Dragon'
        remote_text = '2 1 1 Candle Sprite Battlecry: Deal 1 damage. (Play an Elemental to upgrade.) Elemental'
        for actual, expected in ((local_result, local_text), (remote_result, remote_text)):
            actual_words = Counter()  # type: typing.Counter[str]
            for text_detect in actual.text_detections:
                if text_detect.type == TextTypes.WORD:
                    text = text_detect.detected_text
                    if text == 'O':
                        # There are very rarely O's by themselves here
                        # TODO: Do this conversion somewhere else
                        text = '0'
                    actual_words.update([text])
            expected_words = Counter(expected.split(' '))
            self.assertEqual(sorted(actual_words.elements()), sorted(expected_words.elements()))

    def test_spellsource_card_desc_weapon(self):
        _base_test(self, {
            'http://www.hearthcards.net/cards_thumb/af429c73.png': {
                "name": "Motlic Claws",
                "baseManaCost": 2,
                "type": "WEAPON",
                "description": "Overload: (1) Battlecry: If this weapon replaced your weapon, gain +1 Attack.",
                "damage": 2,
                "durability": 2,
                "collectible": True,
                "set": "CUSTOM",
                "fileFormatVersion": 1
            }
        })

    def test_spellsource_card_desc_base_generator(self):
        _base_test(self, {
            join(dirname(abspath(__file__)), 'test_image.png'): {
                "name": "Hoardling of Tolin",
                "baseManaCost": 0,
                "type": "MINION",
                "baseAttack": 1,
                "baseHp": 1,
                "race": "DRAGON",
                "description": "Invoke (3): Add two copies of a card from your deck to your hand.",
                "collectible": True,
                "set": "CUSTOM",
                "fileFormatVersion": 1
            },
            'http://www.hearthcards.net/cards_thumb/dc309bff.png': {
                "name": "Candle Sprite",
                "baseManaCost": 2,
                "type": "MINION",
                "baseAttack": 1,
                "baseHp": 1,
                "race": "ELEMENTAL",
                "description": "Battlecry: Deal 1 damage. (Play an Elemental to upgrade.)",
                "collectible": True,
                "set": "CUSTOM",
                "fileFormatVersion": 1
            },
            'http://www.hearthcards.net/cards_thumb/caded63e.png': {
                "name": "Mad Science",
                "baseManaCost": 4,
                "type": "SPELL",
                "description": "Craft a custom Zombeast. (Play two secrets to upgrade)",
                "collectible": True,
                "set": "CUSTOM",
                "fileFormatVersion": 1
            }
        })

    def test_gets_correct_image_urls(self):
        url = 'https://www.hearthpwn.com/forums/hearthstone-general/fan-creations/223735-weekly-card-design' \
              '-competition-8-11-final-poll'
        res = list(PageToImages(url))
        self.assertEqual(len(res), 17)
