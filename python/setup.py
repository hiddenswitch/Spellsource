from setuptools import setup
from setuptools.command.install import install
import os
import subprocess
import sys

if sys.version_info < (3, 6):
    sys.exit('Spellsource requires at least Python 3.6\n  Visit https://www.python.org/downloads/ to download it.')

try:
    java_version = subprocess.check_output(["java", "-version"], stderr=subprocess.STDOUT).decode('utf-8')
    if java_version == '':
        java_version = subprocess.check_output(["java", "-version"]).decode('utf-8')
    if '11' not in java_version and '12' not in java_version and '13' not in java_version:
        raise ValueError
except:
    sys.exit('Spellsource requires Java 11 or later.\n  Visit https://adoptopenjdk.net to download it.')

SRC_PATH = '../'
with open(os.path.join(SRC_PATH, 'README.md'), 'r') as readme_file:
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
            gradle_cmd = './gradlew.bat'
        else:
            gradle_cmd = './gradlew'
        subprocess.check_call([f"{gradle_cmd} net:shadowJar"], cwd=SRC_PATH, shell=True)
        subprocess.check_call([f"{gradle_cmd} hearthstone:jar"], cwd=SRC_PATH, shell=True)
        install.run(self)


setup(name='spellsource',
      version='0.8.77',
      description='The Spellsource card game engine, supports Hearthstone AI and simulation',
      long_description=README,
      long_description_content_type="text/markdown",
      url='http://github.com/hiddenswitch/Spellsource-Server',
      python_requires='>3.6',
      author='Benjamin Berman',
      data_files=[
          ("share/spellsource/cards",
           list(_cards_in_directory(os.path.join(SRC_PATH, 'cards', 'src', 'main', 'resources', 'cards')))),
          ("share/spellsource", [os.path.join(SRC_PATH, 'net', 'build', 'libs', 'net-0.8.77-all.jar'),
                                 os.path.join(SRC_PATH, 'hearthstone', 'build', 'libs', 'internalcontent-0.8.77.jar'),
                                 os.path.join(SRC_PATH, 'docs', 'hearthcards.pkl')]),
      ],
      include_package_data=True,
      author_email='ben@hiddenswitch.com',
      license='AGPLv3',
      install_requires=['py4j==0.10.8.1',
                        'tqdm>=4',
                        'objdict',
                        'msgpack',
                        'Click==7.0',
                        'autoboto==0.4.3',
                        'scrapy',
                        'boto3',
                        'hearthstone',
                        'pymongo',
                        'mistletoe',
                        'GitPython',
                        'SecretColors==1.1.0'],
      extras_require={
          'ext': ['numpy', 'h5py', 'keras', 'hearthstone_data', 'nltk', 'gitpython']
      },
      entry_points={
          'console_scripts': [
              'spellsource = spellsource.cmd:main'
          ]
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
