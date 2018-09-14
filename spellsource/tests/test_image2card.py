import typing
import unittest
from collections import Counter
from os.path import join, dirname, abspath

from autoboto.services.rekognition.shapes import DetectTextResponse, TextTypes

from ..ext.image2card import RekognitionGenerator


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
