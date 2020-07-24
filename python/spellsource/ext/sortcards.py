from .cards import iter_card_and_file_path
from os import path, rename, getcwd, makedirs


def sort_cards():
    base_paths: [str] = [getcwd(), 'cards', 'src', 'main', 'resources', 'cards']
    collectible_classes = {x['heroClass']: x['collectible'] for (x, _) in
                           iter_card_and_file_path() if
                           x['type'] == 'CLASS'}
    collectible_classes['ANY'] = True
    for (card_desc, file_path) in iter_card_and_file_path():
        file_name: str = path.basename(file_path)
        paths = base_paths.copy()
        if file_name.startswith('format_'):
            paths += ['formats']
        else:
            card_set: str = (card_desc['sets'][0] if 'sets' in card_desc else card_desc[
                'set'] if 'set' in card_desc else 'missing_set').lower()
            hero_class: str = (card_desc['heroClass'] if 'heroClass' in card_desc else 'missing_class').lower()

            if card_desc['heroClass'] in collectible_classes and collectible_classes[card_desc['heroClass']]:
                paths += ['collectible']
            else:
                paths += ['uncollectible']

            if hero_class == 'any':
                hero_class = 'neutral'
            paths += [hero_class]
            if not any(file_name.startswith(x) for x in ('class_', 'hero_', 'format_')):
                paths += [card_set]
        paths += [file_name]
        destination_path = path.join(*paths)
        makedirs(path.dirname(destination_path), exist_ok=True)
        rename(file_path, destination_path)
