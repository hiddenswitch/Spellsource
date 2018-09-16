from setuptools import setup
from setuptools.command.install import install
import os
import subprocess
import sys

if sys.version_info < (3, 6):
    sys.exit('Spellsource requires Python 3.6')

SRC_PATH = './'
with open('README.md', 'r') as readme_file:
    README = readme_file.read()


def _cards_in_directory(directory):
    for (path, directories, filenames) in os.walk(directory):
        for filename in filenames:
            if '.json' in filename:
                yield os.path.join(path, filename)


class CompileSpellsource(install):
    def run(self):
        # Compile Spellsource
        # This will throw an exception if compilation fails, which is exactly what we want
        if os.name == "nt":
            subprocess.check_call(["./gradlew.bat net:shadowJar"], cwd=SRC_PATH, shell=True)
        else:
            subprocess.check_call(["./gradlew net:shadowJar"], cwd=SRC_PATH, shell=True)
        install.run(self)


setup(name='spellsource',
      version='0.5.9',
      description='The Spellsource card game engine, supports Hearthstone AI and simulation',
      long_description=README,
      long_description_content_type="text/markdown",
      url='http://github.com/hiddenswitch/Spellsource-Server',
      python_requires='>3.6',
      author='Benjamin Berman',
      data_files=[
          ("share/spellsource/cards",
           list(_cards_in_directory(os.path.join(SRC_PATH, 'cards', 'src', 'main', 'resources', 'cards')))),
          ("share/spellsource", [os.path.join(SRC_PATH, 'net', 'build', 'libs', 'net-1.3.0-all.jar'),
                                 os.path.join(SRC_PATH, 'net', 'lib', 'quasar-core-0.7.9-jdk8.jar'),
                                 os.path.join(SRC_PATH, 'docs', 'hearthcards.pkl')]),
      ],
      include_package_data=True,
      author_email='ben@hiddenswitch.com',
      license='GPLv3',
      install_requires=['py4j', 'tqdm', 'objdict', 'msgpack'],
      extras_require={
          'ext': ['numpy', 'h5py', 'keras', 'hearthstone', 'hearthstone_data', 'autoboto', 'boto3', 'nltk', 'scrapy']
      },
      packages=['spellsource', 'spellsource.ext', 'spellsource.tests'],
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
