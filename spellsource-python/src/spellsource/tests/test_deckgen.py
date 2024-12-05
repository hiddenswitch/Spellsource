import unittest

from ..ext.deckgen import generate_deck


class DeckGenTest(unittest.TestCase):
    def test_deck_gen(self):
        assert len(generate_deck()) == 30
