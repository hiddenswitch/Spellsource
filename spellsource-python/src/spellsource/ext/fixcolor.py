import os
from typing import Optional

from objdict import ObjDict as OrderedDict

from .cards import write_card, iter_card_and_file_path
from SecretColors.utils import text_color, get_complementary, color_in_between, hsl_to_hex, rgb_to_hex, hex_to_hsl, \
    hex_to_rgb, rgb_to_hsl, hsl_to_rgb

_STROKE_HSL = rgb_to_hsl(28.0 / 255.0, 30.0 / 255.0, 94.0 / 255.0)


def fix_colors(path: Optional[str] = None):
    if path is None:
        path = os.path.join(os.getcwd(), 'cards', 'src', 'main', 'resources', 'cards')
    for (card, filepath) in iter_card_and_file_path(start_path=path):
        fixed_card = fix_color(card)
        write_card(fixed_card, filepath)


def _clip(value, lower, upper):
    return lower if value < lower else upper if value > upper else value


def fix_color(card):
    if 'blackText' in card:
        del card['blackText']
    if 'art' in card:
        if 'body' in card['art']:
            if 'vertx' in card['art']['body']:
                card['art']['body']['vertex'] = card['art']['body']['vertx']
                del card['art']['body']['vertx']
    if 'color' in card:
        r255, g255, b255 = card['color']
        r = r255 / 255.0
        g = g255 / 255.0
        b = b255 / 255.0
        del card['color']
    elif 'art' in card:
        r = card['art']['primary']['r']
        g = card['art']['primary']['g']
        b = card['art']['primary']['b']
    else:
        return card

    primary_hex = rgb_to_hex(r, g, b)
    # complement = get_complementary(primary_hex)
    (h, s, l) = rgb_to_hsl(r, g, b)
    # (ch, cs, cl) = hex_to_hsl(complement)
    primary = (r, g, b)
    secondary, _, shadow = map(hex_to_rgb, color_in_between(primary_hex, hsl_to_hex(h, s, _STROKE_HSL[2]), 3))
    highlight = hex_to_rgb(color_in_between(primary_hex, hsl_to_hex(h, 1.0, 1.0), 2)[0])
    body_c = hex_to_rgb(text_color(primary_hex))

    if 'art' not in card:
        card['art'] = OrderedDict()
    art = card['art']

    for (name, rgb) in (
            ('primary', primary), ('secondary', secondary), ('shadow', shadow), ('highlight', highlight)):
        if name not in art:
            art[name] = OrderedDict()
        r, g, b = rgb
        a = 1.0
        art[name]['r'] = r
        art[name]['g'] = g
        art[name]['b'] = b
        art[name]['a'] = a
    if 'body' not in art:
        art['body'] = OrderedDict()
    body = art['body']
    if 'vertex' not in body:
        body['vertex'] = OrderedDict()
    vertex = body['vertex']
    vertex['r'] = body_c[0]
    vertex['g'] = body_c[1]
    vertex['b'] = body_c[2]
    vertex['a'] = 1.0
    return card
