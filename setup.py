from setuptools import setup
from setuptools.command.install import install
import os
import subprocess
import sys

if sys.version_info < (3, 6):
    sys.exit('Spellsource requires Python 3.6')


class CompileSpellsource(install):
    def run(self):
        # Compile Spellsource
        # This will throw an exception if compilation fails, which is exactly what we want
        
        src_path = os.getcwd()
        if os.name == "nt":
            subprocess.check_call(["./gradlew.bat net:shadowJar"], cwd=src_path, shell=True)
        else:
            subprocess.check_call(["./gradlew net:shadowJar"], cwd=src_path, shell=True)
        install.run(self)


setup(name='spellsource',
      version='0.4.13',
      description='The Spellsource card game engine, supports Hearthstone AI and simulation',
      long_description='''
A multiplayer, networked adaptation of ``metastone``. This is a
simulator and game server for community and official Hearthstone cards.

Download the Hidden Switch Launcher for `Mac OS
X <http://go.hiddenswitch.com/mac>`__ or
`Windows <http://go.hiddenswitch.com/win>`__ to get a copy of the game
client. Play online against others! No other installation required.

Quick Start Python
~~~~~~~~~~~~~~~~~~

The ``spellsource`` package creates a bridge with the Java-based
``Spellsource-Server`` engine. It provides a direct 1-to-1 mapping with
the Java API.

1. Install the Java 8 SDK (JDK) from Oracle’s website.
2. ``pip install spellsource`` to install the latest version of the
   package.
3. Start a game and play it with the specified bots:

   .. code:: python

      from spellsource.context import Context
      from spellsource.playrandombehaviour import PlayRandomBehaviour

      with Context() as ctx:
          game_context = ctx.game.GameContext.fromTwoRandomDecks()
          behaviour1 = PlayRandomBehaviour()
          behaviour2 = PlayRandomBehaviour()
          game_context.setBehaviour(0, behaviour1.wrap(ctx))
          game_context.setBehaviour(1, behaviour2.wrap(ctx))
          game_context.play()
          assert game_context.updateAndGetGameOver()


This package also supports simulators. Visit
`the example notebook <https://github.com/hiddenswitch/Spellsource-Server/blob/master/docs/simulation_example.ipynb>`__.

Visit
`GameStateValueBehaviour <https://github.com/hiddenswitch/Spellsource-Server/blob/master/spellsource/gamestatevaluebehaviour.py>`__
to see an implementation of a complex AI bot in Python. This is a direct
port of the Java code. Unfortunately, on the Python platform, remoting
(accessing the Java engine) in the particular way this bot does is slow.
To implement more sophisticated bots, consider adding a method to
``GameContext`` that will extract the exact data, in a binary format,
that you need in your Python implementation, to reduce the communication
overhead between Java and Python.
      ''',
      url='http://github.com/hiddenswitch/Spellsource-Server',
      python_requires='>3.6',
      author='Benjamin Berman',
      data_files=[
          ("share/spellsource", [os.path.join('net', 'build', 'libs', 'net-1.3.0-all.jar'),
                                 os.path.join('net', 'lib', 'quasar-core-0.7.9-jdk8.jar'),
                                 os.path.join('docs', 'hearthcards.pkl')]),
      ],
      include_package_data=True,
      author_email='ben@hiddenswitch.com',
      license='GPLv3',
      install_requires=['py4j', 'tqdm', 'objdict', 'msgpack', 'keras'],
      packages=['spellsource'],
      keywords=['hearthstone', 'artificial intelligence', 'ai', 'spellsource', 'cards', 'games', 'machine learning',
                'ml'],
      classifiers=['Development Status :: 3 - Alpha',
                   # Indicate who your project is intended for
                   'Intended Audience :: Developers',

                   # Pick your license as you wish (should match "license" above)
                   'License :: OSI Approved :: GNU General Public License v3 (GPLv3)',

                   # Specify the Python versions you support here. In particular, ensure
                   # that you indicate whether you support Python 2, Python 3 or both.
                   'Programming Language :: Python :: 3',
                   'Programming Language :: Python :: 3.6'],
      cmdclass={'install': CompileSpellsource},
      zip_safe=False)
