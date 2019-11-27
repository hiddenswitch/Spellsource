import typing
from itertools import chain
from os import makedirs
from os.path import join, abspath
from typing import List

import click
from tqdm import tqdm

from .context import Context
from .ext.admin import Admin
from .ext.cardformatter import fix_cards, fix_card
from .ext.datasources import HSReplayMatchups
from .ext.fixpullrequest import PullRequestFixSession
from .ext.hearthcards import write_set_stubs
from .ext.populatedecklists import write_decklists
from .ext.updatedbf import write_dbf_json


@click.group()
def _cli():
    pass


@_cli.command()
@click.argument('username')
@click.option('--profile-name', default='default', help='the profile name to use in the printed credentials file',
              show_default=True)
def create_user(username, profile_name='default'):
    """
    Creates an AWS user named USERNAME.

    The user is created in the calling user's AWS account. Prints the credentials file that should go
    into ~/.aws/credentials.

    For example, this call will print:

    \b
    [default]
    aws_access_key_id = ABCDEFGHJI12345790
    aws_secret_access_key = abcdefghab+cdefghabcdefgh/00000000000==

    Thus, to create the user doctorpangloss and save the credentials for later use, run the command:

    \b
      spellsource create-user doctorpangloss --profile-name=spellsource >> ~/.aws/credentials

    Then, to use this profile for an aws command:

    \b
      aws s3api list-buckets --profile=spellsource

    The following policies are added by default: AWSElasticBeanstalkFullAccess, AmazonRekognitionFullAccess and
    AmazonS3FullAccess.
    """
    click.echo(Admin.create_user(username=username, profile_name=profile_name))


@_cli.command()
@click.option('--path', default='./cards/src/main/resources/cards',
              help='the filepath to walk for card JSON', show_default=True)
def format_cards(path):
    """
    Formats JSON card files.
    """
    fix_cards(path)


@_cli.command()
def hs_replay_matchups():
    """
    Prints a table of HSReplay matchups in TSV format.

    To save this output to "matchups.tsv", perform the command:

    \b
      spellsource hs-replay-matchups > matchups.tsv

    """
    click.echo(HSReplayMatchups().to_tsv())


@_cli.command()
@click.argument('set')
@click.option('--directory', default='./cards/src/main/resources/staging/hearthcards',
              help='the path to save the stubs to', show_default=True)
@click.option('--hero-class', default='ANY', help='the hero class to write onto the cards', show_default=True)
def hearthcards_stubs(set, directory, hero_class):
    """
    Creates stubs from the Hearthcards.

    The stubs will be generated from a set or hero class with the identifier SET_ID. For example, if the address bar
    for your set or hero class shows
    http://www.hearthcards.net/setsandclasses/#2249-Monk, the SET_ID will be 2249. You will then use the command:

    \b
      spellsource hearthcards-stubs 2249

    This will create a folder full of stubs at ./cards/src/main/resources/staging/hearthcards/set_2249. Remember to move
    this directory to the custom cards folder when you're ready to execute the cards against the server.

    You'll need to add your HERO_CLASS to HeroClass.java and modify that file to return the appropriate hero card for
    the class. The way to do that should be self-explanatory in the HeroClass.java file.
    """
    write_set_stubs(set, dest_dir=directory, hero_class=hero_class)


@_cli.command()
@click.argument('files_or_urls', nargs=-1)
@click.option('--directory', default='./cards/src/main/resources/staging/scraped',
              help='the directory to save the cards to', show_default=True)
@click.option('--hero-class', default='ANY', help='the hero class to put into the cards', show_default=True)
def image_stubs(files_or_urls: typing.Iterable[str], directory, hero_class):
    """
    Converts images to card stubs.

    Loads image or website located at each url or path in FILES_OR_URLS and parses it, creating stubs in DIRECTORY. If
    the path is a website, every relatively card-looking image will be parsed if possible. While any website can be
    used, the following are specially supported:

    \b
     - hearthpwn.com/forums: Only looks in the forum post bodies.

    Imgur albums are currently not supported.

    This call may take a while. Results are cached remotely.

    Make sure you have saved your AWS credentials to use this service. If you have not, or do not have credentials,
    this service will fail to convert cards. Contact the administrators on the Discord for a contributor's user
    account by visiting playspellsource.com.
    """
    from .ext import image2card
    from .ext.cards import name_to_id, write_card

    if 'imgur.com/a/' in files_or_urls:
        click.echo('Imgur albums are currently not supported.')
        return

    makedirs(directory, exist_ok=True)

    images = [url for url in files_or_urls if len(url) > 4 and url[-4:] in ('.png', '.jpg')]
    websites = [url for url in files_or_urls if len(url) > 4 and url[-4:] not in ('.png', '.jpg')]

    images_iterable = image2card.Enricher(
        *image2card.SpellsourceCardDescGenerator(*image2card.RekognitionGenerator(*images)),
        hero_class=hero_class)

    websites_iterable = image2card.Enricher(*image2card.SpellsourceCardDescGenerator(
        *tqdm(
            image2card.RekognitionGenerator(
                *image2card.PageToImages(*websites)))), hero_class=hero_class)
    for card_desc in chain(images_iterable, websites_iterable):
        id = name_to_id(card_desc['name'], card_desc['type'])
        write_card(fix_card(card_desc), join(directory, id + '.json'))


@_cli.command()
@click.argument('snap_num')
def update_decklists(snap_num):
    """
    Updates the deck lists from Tempostorm.

    Uses snapshot number SNAP_NUM. Visit http://tempostorm.com to find the latest snapshot number.

    This method should be called from the Spellsource-Server directory to ensure the deck list is written in the
    right place. To update the bot's decks, make sure to add an appropriate migration in Spellsource.java.
    """
    write_decklists(int(snap_num))


@_cli.command()
def update_dbf():
    """
    Updates Hearthstone IDs.

    Uses data from the community hearthstone_data Python package. The DBF IDs are used to convert deck strings into
    something readable.

    This method should be called from the Spellsource-Server directory to ensure the updated DBF file is saved in the
    right place.
    """
    write_dbf_json()


@_cli.command()
@click.argument('decks', nargs=-1)
@click.option('--number', default=1, show_default=True, type=click.INT, help='the number of games to simulate')
@click.option('--behaviours', type=click.Tuple([str, str]), default=('PlayRandomBehaviour', 'PlayRandomBehaviour'),
              show_default=True,
              help='the behaviours to use for this simulation, suggested choices are PlayRandomBehaviour and '
                   'GameStateValueBehaviour. If the behaviours differ, each matchup will be played with each '
                   'behaviour as each player (useful for comparing AIs to one another)')
@click.option('--mirrors', type=click.BOOL, default=False, show_default=True, help='whether to include mirror matchups')
@click.option('--reduce', type=click.BOOL, default=True, show_default=True,
              help='whether to add up the statistics for all matchups, otherwise report each individually')
def simulate(decks,
             number: int,
             behaviours=(),
             mirrors: bool = False,
             reduce: bool = True):
    """
    Run a simulation using AIs of a given deck matchup.

    DECKS can be a space-separated list of:

    \b
     - Hearthstone deck strings
     - Community deck strings
     - File paths, containing Hearthstone deck strings or community deck strings

    A community deck format string looks like this:

    \b
    Name: Big Druid
    Class: Druid
    Format: Standard
    2x Biology Project
    1x Lesser Jasper Spellstone
    2x Naturalize
    2x Wild Growth
    1x Drakkari Enchanter
    1x Greedy Sprite
    2x Branching Paths
    2x Bright-Eyed Scout
    2x Nourish
    2x Spreading Plague
    1x Malfurion the Pestilent
    1x Gloop Sprayer
    2x Primordial Drake
    1x The Lich King
    2x Dragonhatcher
    1x Hadronox
    1x Master Oakheart
    2x Sleepy Dragon
    1x Ysera
    1x Tyrantus

    This will simulate 10 games using the advanced GameStateValueBehaviour AI between two decks specified as files.

    \b
      spellsource simulate 'Aggro Mage.txt' 'Big Druid.txt' --number 10 --behaviours GameStateValueBehaviour
      GameStateValueBehaviour


    Returns the results as a JSON array. For example, for the result above:

    \b
    [
      {
        "decks":[
          "APM Priest",
          "Aggro Mage"
        ],
        "numberOfGames":10,
        "results":[
          {
            "WIN_RATE":0.5,
            "GAMES_WON":5,
            "GAMES_LOST":5,
            "DAMAGE_DEALT":376,
            "HEALING_DONE":277,
            "MANA_SPENT":669,
            "CARDS_PLAYED":346,
            "TURNS_TAKEN":150,
            "CARDS_DRAWN":197,
            "FATIGUE_DAMAGE":35,
            "MINIONS_PLAYED":243,
            "SPELLS_CAST":165,
            "HERO_POWER_USED":82
          },
          {
            "WIN_RATE":0.5,
            "GAMES_WON":5,
            "GAMES_LOST":5,
            "DAMAGE_DEALT":834,
            "HEALING_DONE":18,
            "MANA_SPENT":769,
            "CARDS_PLAYED":346,
            "TURNS_TAKEN":155,
            "CARDS_DRAWN":250,
            "FATIGUE_DAMAGE":188,
            "MINIONS_PLAYED":209,
            "SPELLS_CAST":141,
            "HERO_POWER_USED":95,
            "WEAPONS_EQUIPPED":7,
            "WEAPONS_PLAYED":7
          }
        ]
      }
    ]

    The first item (index 0) of the results array is always the first item in the decks array and corresponds to the
    first behaviour specified.
    """
    from .context import Context
    from .utils import simulate
    from os.path import isfile
    import sys

    decks = list(decks)
    for i, deck in enumerate(decks):
        if isfile(deck):
            with open(deck, 'r') as deck_file:
                decks[i] = deck_file.read()

    deck_strings = ', '.join(decks)
    click.echo(f'Using decks {deck_strings}', file=sys.stderr)
    behaviour_strings = ', '.join(behaviours)
    click.echo(f'Using behaviours {behaviour_strings}', file=sys.stderr)

    with Context() as context:
        sim = simulate(context, decks=decks, number=number, behaviours=behaviours, mirrors=mirrors, reduce=reduce)
        results = list(tqdm(sim, file=sys.stderr))

        from json import dumps
        click.echo(dumps(results))


@_cli.command()
@click.argument('path-to-pem-file', type=click.STRING)
@click.argument('remote-host', type=click.STRING)
@click.option('--db-path', type=click.STRING, default='.mongo', show_default=True,
              help='the path to restore the database to, i.e. as an argument for --db-path')
@click.option('--tmp-dir', type=click.STRING, default=None, show_default=True,
              help='the path to temporarily dump the database to')
def replicate_database(path_to_pem_file: str, remote_host: str, db_path: str = '.mongo', tmp_dir: str = None):
    """
    Replicates mongo databases.

    Connects to the database located at REMOTE_HOST and authenticated with PATH_TO_PEM_FILE.

    To start a database using the replicated one, use the following command:

    \b
      mongod --db-path DB_PATH
    """
    from tempfile import mkdtemp
    tmp_dir = tmp_dir or mkdtemp()
    Admin.replicate_mongo_db(abspath(path_to_pem_file), remote_host, abspath(db_path), tmp_dir=tmp_dir)
    click.echo(abspath(db_path))


@_cli.command()
@click.argument('username-or-email')
@click.argument('password')
@click.option('--db-uri', default='mongodb://localhost:27017', show_default=True,
              help='the URI to the mongo instance with the metastone database')
def change_password(username_or_email: str, password: str, db_uri: str):
    """
    Changes a Spellsource user's password.

    Connects to DB_URI, and changes password of the user found with USERNAME_OR_EMAIL to PASSWORD. Prints the email
    address and username of the user whose password was changed.
    """
    try:
        record = Admin.change_user_password(db_uri, username_or_email, password)
    except ConnectionRefusedError as ex:
        click.echo('Connection refused to mongo. Check your db_uri %s' % db_uri, err=True)
        raise SystemExit(1)
    if record is None:
        click.echo('User %s not found' % username_or_email, err=True)
        raise SystemExit(1)
    click.echo(record['emails'][0]['address'])
    click.echo(record['username'])


@_cli.command()
@click.argument('file', type=click.File())
@click.option('--front-matter', default=True, show_default=True, help='skips the front matter in the input file')
def markdown_to_textmesh(file: typing.TextIO, front_matter: bool = True):
    """
    Renders a Markdown file to TextMesh markup.
    """
    input = file.readlines()
    if front_matter:
        input = input[5:]
    from spellsource.ext.md2textmesh import TextMeshRenderer
    from mistletoe import Document
    with TextMeshRenderer() as renderer:
        # skips front matter
        rendered = renderer.render(Document(input))
    click.echo(rendered)


@_cli.command()
@click.argument('file', type=click.File(mode='w'))
def create_cards_db(file: typing.TextIO):
    """
    Saves a cards.json database for the client.

    This should be located on a path that the server can return to.
    """
    with Context() as ctx:
        cards = ctx.root_namespace().com.hiddenswitch.spellsource.client.models.GetCardsResponse()
        cards.cards(ctx.root_namespace().com.hiddenswitch.spellsource.Cards.getCards())
        cards.version("1")
        json_cards = ctx.root_namespace().com.hiddenswitch.spellsource.util.Serialization.serialize(cards)
    file.write(json_cards)


@_cli.command()
def fix_merge():
    """
    Fixes card catalogue merge issues.

    This will undo deletes staged in the git repo and mark those cards as not collectible instead. Then, it will undo
    true renames (file moves are allowed, as long as the filenames haven't changed). Finally it will fix missing
    extensions or adding too many .json extensions.
    """
    session = PullRequestFixSession('.')
    res = session.fix()
    click.echo(res)


@_cli.command()
@click.option('--mongo-url', required=False, default=None, show_default=True,
              help='when empty, starts a mongod, otherwise this is the db URL')
@click.option('--gradle-cmd', required=False, default='gradle', show_default=True,
              help='specifies the gradle command, if it is not installed use --default-gradle')
@click.option('--default-gradle', required=False, default=False, show_default=True,
              help='when TRUE, use the gradle distribution that comes with the source directory')
def run(mongo_url: str = None, gradle_cmd: str = 'gradle', default_gradle: bool = False):
    """
    Runs the server.

    Starts a mongod instance if installed and no MONGO_URL environment variable is specified.

    If gradle is not installed, uses the local distribution.
    """
    from subprocess import Popen, DEVNULL
    from shlex import split
    from os import environ, makedirs
    from platform import platform
    from contextlib import ExitStack
    import errno
    start_mongo = mongo_url is None
    mongo_url = mongo_url or 'mongodb://localhost/metastone'

    def get_gradle_cmd():
        if 'win' in platform():
            return './gradlew.bat'
        else:
            return './gradlew'

    def java_main():
        # No gradle, try running from jar
        from threading import Thread

        class _JavaMain:
            def __init__(self):
                self.thread = Thread(target=self.run)

            def __enter__(self):
                self.thread.start()

            def run(self):
                from .context import Context
                with Context() as context:
                    context.root_namespace().com.hiddenswitch.spellsource.applications.Local.main(
                        context.string_array(0))

            def __exit__(self, exc_type, exc_val, exc_tb):
                pass

            def wait(self):
                click.echo('Started server')
                self.thread.join()

        return _JavaMain()

    if default_gradle:
        gradle_cmd = get_gradle_cmd()

    cmds: List[Popen] = []

    if start_mongo and 'MONGO_URL' not in environ:
        makedirs('.mongo', exist_ok=True)
        cmds += [Popen(split('mongod --bind_ip_all --dbpath=.mongo/'), stdout=DEVNULL)]
        click.echo('Started mongod')
        environ['MONGO_URL'] = mongo_url

    import os.path
    if os.path.isfile('build.gradle'):
        try:
            java = Popen(split(f'{gradle_cmd} net:local'))
        except OSError as e:
            if e.errno == errno.ENOENT:
                gradle_cmd = get_gradle_cmd()
                try:
                    java = Popen(split(f'{gradle_cmd} net:local'))
                except OSError as e2:
                    if e2.errno == errno.ENOENT:
                        java = java_main()
                    else:
                        raise e2
    else:
        java = java_main()
    cmds += [java]

    with ExitStack() as stack:
        for cmd in cmds[::-1]:
            stack.enter_context(cmd)

        java.wait()


def main():
    _cli()
