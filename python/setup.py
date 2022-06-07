import os
import subprocess
import sys
import pathlib
from os.path import relpath, join

from setuptools import setup
from setuptools.command.install import install

DIR = pathlib.Path(__file__).parent.absolute()

if sys.version_info < (3, 8):
    sys.exit('Spellsource requires at least Python 3.8\n  Visit https://www.python.org/downloads/ to download it.')

try:
    java_version = subprocess.check_output(["java", "-version"], stderr=subprocess.STDOUT).decode('utf-8')
    if java_version == '':
        java_version = subprocess.check_output(["java", "-version"]).decode('utf-8')
    if all(str(x) + '.' not in java_version for x in range(11, 100)):
        raise ValueError
except:
    sys.exit('Spellsource requires Java 11 or later.\n  Visit https://adoptopenjdk.net to download it.')

SRC_PATH = join(DIR, '../')
with open(join(SRC_PATH, 'README.md'), 'r') as readme_file:
    README = readme_file.read()


def _cards_in_directory(directory):
    for (path, directories, filenames) in os.walk(directory):
        for filename in filenames:
            if '.json' in filename:
                yield join(path, filename)


class CompileSpellsource(install):
    def run(self):
        # Compile Spellsource
        # This will throw an exception if compilation fails, which is exactly what we want
        if os.name == "nt":
            gradle_cmd = 'gradlew.bat'
        else:
            gradle_cmd = './gradlew'
        subprocess.check_call([f"{gradle_cmd}", '--no-daemon', "spellsource-server:shadowJar"], cwd=SRC_PATH, shell=True)
        subprocess.check_call([f"{gradle_cmd}", '--no-daemon', "spellsource-cards-private:jar"], cwd=SRC_PATH, shell=True)
        install.run(self)


setup(name='spellsource',
      version='0.9.0',
      description='The Spellsource card game engine for card game AI and simulation',
      long_description=README,
      long_description_content_type="text/markdown",
      url='http://github.com/hiddenswitch/Spellsource-Server',
      python_requires='>3.6',
      author='Benjamin Berman',
      data_files=[
          ("share/spellsource/cards",
           # paths to the cards relative to the setup.py file
           [relpath(card_path, DIR) for card_path in
            _cards_in_directory(join(SRC_PATH, 'spellsource-cards-git', 'src', 'main', 'resources', 'cards'))]),
          ("share/spellsource",
           [relpath(card_path, DIR) for card_path in
            [join(SRC_PATH, 'spellsource-server', 'build', 'libs', 'spellsource-server-0.9.0-all.jar'),
             join(SRC_PATH, 'spellsource-cards-private', 'build', 'libs',
                  'spellsource-cards-private-0.9.0.jar')]]),
      ],
      include_package_data=True,
      author_email='ben@hiddenswitch.com',
      license='AGPLv3',
      install_requires=['py4j==0.10.9.5',
                        'tqdm>=4',
                        'objdict',
                        'msgpack',
                        'Click==7.0',
                        'autoboto==0.4.3',
                        'scrapy',
                        'boto3',
                        'pymongo',
                        'mistletoe',
                        'GitPython',
                        'psd-tools',
                        'SecretColors==1.1.0'],
      extras_require={
          'ext': ['numpy', 'h5py', 'keras', 'nltk', 'gitpython']
      },
      entry_points={
          'console_scripts': [
              'spellsource = spellsource.cmd:main'
          ]
      },
      packages=['spellsource', 'spellsource.ext', 'spellsource.tests'],
      keywords=['artificial intelligence', 'ai', 'spellsource', 'cards', 'games', 'machine learning',
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
