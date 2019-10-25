import urllib.request as urllib
import json


class HSReplayMatchups(object):
    def __init__(self):
        self._headers = {
            'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_2) AppleWebKit/604.4.7 (KHTML, like Gecko) '
                          'Version/11.0.2 Safari/604.4.7',
            'Referrer': 'https://hsreplay.net/meta/'}
        self._archetypes_request = urllib.Request(url='https://hsreplay.net/api/v1/archetypes/', headers=self._headers)
        self._archetypes_list = json.loads(urllib.urlopen(self._archetypes_request).read())
        self._data_request = urllib.Request(
            url='https://hsreplay.net/analytics/query/head_to_head_archetype_matchups/?GameType=RANKED_STANDARD'
                '&RankRange=LEGEND_THROUGH_TWENTY&Region=ALL&TimeRange=LAST_7_DAYS',
            headers=self._headers)

        self._matchups_dict = json.loads(urllib.urlopen(self._data_request).read())
        self._archetypes = {str(x['id']): x for x in self._archetypes_list}
        self.data = self._matchups_dict['series']['data']

    def __len__(self):
        return len(self.data)

    def __iter__(self):
        for player1_id, matchup in self.data.items():
            if player1_id not in self._archetypes:
                continue
            archetype1 = self._archetypes[player1_id]['name']
            for player2_id, result in matchup.items():
                if player2_id not in self._archetypes:
                    continue
                archetype2 = self._archetypes[player2_id]['name']
                yield (archetype1, archetype2, result['total_games'], result['win_rate'])

    def to_tsv(self):
        rows = list(iter(self))
        return '\n'.join(map(lambda row: '\t'.join(map(str, row)), rows))
