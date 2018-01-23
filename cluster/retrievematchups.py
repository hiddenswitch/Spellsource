#!/usr/bin/python
import urllib2
import json


def retrieve_matchups_rows():
    _headers = {
        'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_2) AppleWebKit/604.4.7 (KHTML, like Gecko) Version/11.0.2 Safari/604.4.7',
        'Referrer': 'https://hsreplay.net/meta/'}
    _archetypes_request = urllib2.Request(url='https://hsreplay.net/api/v1/archetypes/', headers=_headers)
    _archetypes_list = json.loads(urllib2.urlopen(_archetypes_request).read())
    _data_request = urllib2.Request(
        url='https://hsreplay.net/analytics/query/head_to_head_archetype_matchups/?GameType=RANKED_STANDARD&RankRange=LEGEND_THROUGH_TWENTY&Region=ALL&TimeRange=LAST_7_DAYS',
        headers=_headers)

    _matchups_dict = json.loads(urllib2.urlopen(_data_request).read())
    _archetypes = {str(x['id']): x for x in _archetypes_list}

    def _iter_matchup(series_data):
        for player1_id, matchup in series_data.iteritems():
            if player1_id not in _archetypes:
                continue
            archetype1 = _archetypes[player1_id]['name']
            for player2_id, result in matchup.iteritems():
                if player2_id not in _archetypes:
                    continue
                archetype2 = _archetypes[player2_id]['name']
                yield (archetype1, archetype2, result['total_games'], result['win_rate'])

    return list(_iter_matchup(_matchups_dict['series']['data']))


def to_tsv(rows):
    return '\n'.join(map(lambda row: '\t'.join(map(str, row)), rows))
    pass


if __name__ == '__main__':
    print to_tsv(retrieve_matchups_rows())
