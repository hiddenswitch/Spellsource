try:
    from hearthstone import cardxml
    from os import path
    from json import dump


    def write_dbf_json():
        dbf = cardxml.load_dbf()[0]
        new_json = {str(id): card.name for id, card in dbf.items() if card.collectible}
        f = open(path.join(path.dirname(__file__), '..', '..', 'game', 'src', 'main', 'resources', 'dbf.json'), 'w')
        dump(new_json, f, indent=2)
        f.close()
except ImportError:
    def write_dbf_json():
        pass
