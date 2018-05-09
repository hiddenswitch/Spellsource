from setuptools import setup
import os
import subprocess
import sys

if sys.version_info < (3, 6):
    sys.exit('Spellsource requires Python 3.6')


def build_net():
    if os.name == "nt":
        subprocess.run(["./gradlew.bat", "net:shadowJar"], shell=True)
    else:
        subprocess.run(["./gradlew", "net:shadowJar"], shell=True)


try:
    print('Building the Spellsource engine dependencies. This make take a while.')
    build_net()
except Exception as ex:
    print('An error occurred while trying to build the engine. Make sure you have Java installed.')
    raise ex

setup(name='spellsource',
      version='0.2.0',
      description='The Spellsource card game engine, supports Hearthstone AI and simulation',
      url='http://github.com/hiddenswitch/Spellsource-Server',
      python_requires='>3.6',
      author='Benjamin Berman',
      data_files=[("share/spellsource", [os.path.join('net', 'build', 'libs', 'net-1.3.0-all.jar')])],
      include_package_data=True,
      author_email='ben@hiddenswitch.com',
      license='GPLv3',
      install_requires=['py4j'],
      packages=['spellsource'],
      keywords=[''],
      classifiers=['Development Status :: 3 - Alpha',
                   # Indicate who your project is intended for
                   'Intended Audience :: Developers',

                   # Pick your license as you wish (should match "license" above)
                   'License :: OSI Approved :: GNU General Public License v3 (GPLv3)',

                   # Specify the Python versions you support here. In particular, ensure
                   # that you indicate whether you support Python 2, Python 3 or both.
                   'Programming Language :: Python :: 3',
                   'Programming Language :: Python :: 3.6',
                   'Programming Language :: Python :: 3.6.5'],
      zip_safe=False)
