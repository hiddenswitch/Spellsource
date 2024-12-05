import urllib.parse
import urllib.request
import json
import copy
import os.path as path

_FILTER = {
    "where": {
        "snapNum": 0,
        "snapshotType": "standard"
    },
    "include": [
        {
            "relation": "deckTiers",
            "scope": {
                "include": [
                    {
                        "relation": "deck",
                        "scope": {
                            "fields": [
                                "name",
                                "playerClass"
                            ],
                            "include": {
                                "relation": "cards",
                                "scope": {
                                    "include": [
                                        {
                                            "relation": "card",
                                            "fields": [
                                                "dbfId"
                                            ]
                                        }
                                    ]
                                }
                            }
                        }
                    }
                ]
            }
        }
    ]
}

_TEMPOSTORM_URL = 'https://tempostorm.com/api/snapshots/findOne?'


def _get_snapshot_url(snap_num=61):
    filter = copy.deepcopy(_FILTER)
    filter['where']['snapNum'] = snap_num
    filter_str = urllib.parse.urlencode({"filter": json.dumps(filter, indent=None, separators=[',', ':'])})
    return _TEMPOSTORM_URL + filter_str


def write_decklists(snap_num=61):
    print('Updating the deck lists...')
    for deck in TempostormDecklists(snap_num=61):
        filename = deck['name'] + '.txt'
        with open(
                path.join(path.dirname(__file__), '..', '..', 'net', 'src', 'main', 'resources', 'decklists', 'current',
                          filename), 'w') as f:
            lines = _to_deck_list(deck)
            f.write(lines)


def _to_deck_list(deck):
    lines = ['Name: ' + deck['name'], 'Class: ' + deck['heroClass'], 'Format: Standard']
    lines += [str.format('{0}x {1}', card['quantity'], card['name']) for card in deck['cards']]
    lines = '\n'.join(lines)
    return lines


class TempostormDecklists(object):
    def __init__(self, snap_num=61):
        self.request = urllib.request.Request(url=_get_snapshot_url(snap_num),
                                              headers={
                                                  'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_2) '
                                                                'AppleWebKit/604.4.7 (KHTML, like Gecko) '
                                                                'Version/11.0.2 '
                                                                'Safari/604.4.7'
                                              })
        self.snapshot = json.load(urllib.request.urlopen(self.request))
        self.decks = [{'name': deck['name'],
                       'heroClass': deck['deck']['playerClass'],
                       'cards': [{'name': card['card']['name'], 'quantity': card['cardQuantity']}
                                 for card in deck['deck']['cards']]} for deck in self.snapshot['deckTiers']]

    def __iter__(self):
        for deck in self.decks:
            yield deck

    def __len__(self):
        return len(self.decks)

    @staticmethod
    def to_deck_list(deck):
        return _to_deck_list(deck)
