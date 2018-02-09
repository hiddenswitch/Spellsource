import utils

if __name__ == '__main__':
    for (card, card_path) in utils.iter_cards():
        for (component, parent, key, inherits) in utils.walk_card(card):
            # Check for accidental use of HP bonus instead of armor bonus
            if ('class' in component and
                    component['class'] == 'BuffSpell' and
                    'hpBonus' in component and
                    'target' in inherits and
                    inherits['target'] == 'FRIENDLY_HERO'):
                print 'Probable hpBonus instead of armorBonus issue in ' + str(card['name'])
