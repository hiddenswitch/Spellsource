import utils
from formatter import fix_dict

if __name__ == '__main__':
    for (card, filepath) in utils.iter_cards():
        card_type = card['type']
        if card_type == 'MINION' or card_type == 'WEAPON' or card_type == 'HERO':
            if 'options' in card:
                card['chooseOneBattlecries'] = card['options']
                del card['options']
            if 'bothOptions' in card:
                card['chooseBothBattlecry'] = card['bothOptions']
                del card['bothOptions']
        if card_type == 'SPELL' or card_type == 'CHOOSE_ONE' or card_type == 'HERO_POWER':
            if 'options' in card:
                card['chooseOneCardIds'] = card['options']
                del card['options']
            if 'bothOptions' in card:
                card['chooseBothCardId'] = card['bothOptions']
                del card['bothOptions']
        if card_type == 'SPELL':
            if 'trigger' in card:
                card['secret'] = card['trigger']
                del card['trigger']
        fixed_card = fix_dict(card)
        utils.write_card(fixed_card, filepath)
