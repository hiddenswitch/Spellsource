#!/usr/bin/python
from utils import iter_cards


def main():
    print '### Wild Sands of Time'
    print '# Class: Warlock'
    print '# Format: Wild'
    cards = []
    i = 0
    for (card, path) in iter_cards():
        if 'the_sands_of_time' in path:
            if i >= 30:
                break

            if not card[u'collectible']:
                continue

            print '# (' + str(card[u'baseManaCost']) + ') ' + '1x ' + card[u'name']
            i += 1


if __name__ == '__main__':
    main()
