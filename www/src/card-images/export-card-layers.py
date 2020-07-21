from psd_tools import PSDImage

psd = PSDImage.open('Spellsource-Large-Cards.psb')

for layer in psd:
    if ("Large Card Rare" in layer.name and "Large Card Rare A" not in layer.name):
        image = layer.composite()
        image.save(layer.name + '.png')
