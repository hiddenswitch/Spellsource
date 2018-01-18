from json import dump, load
from os import walk, path

try:
    import objdict
except ImportError:
    import pip

    pip.main(['install', '--user', 'objdict'])

from objdict import ObjDict as OrderedDict
from objdict import JsonEncoder


def iter_cards(start_path=None):
    if start_path is None:
        start_path = path.join(path.dirname(__file__), 'src/main/resources/cards')

    for root, dirnames, filenames in walk(start_path):
        for filename in filenames:
            if '.json' not in filename:
                continue
            filepath = path.join(root, filename)
            with open(filepath) as fp:
                try:
                    card = load(fp, object_pairs_hook=OrderedDict)
                    yield (card, filepath)
                except ValueError as ex:
                    print 'Parsing error in ', filepath
                    continue


def write_card(card, filepath):
    with open(filepath, 'w') as fp:
        dump(card, fp, indent=2, separators=(',', ': '), cls=JsonEncoder)
