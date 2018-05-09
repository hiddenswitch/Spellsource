from setuptools import setup
import os
import subprocess


def build_net():
    if os.name == "nt":
        subprocess.run(["./gradlew.bat", "net:shadowJar"], shell=True)
    else:
        subprocess.run(["./gradlew", "net:shadowJar"], shell=True)


setup(name='pyspellsource',
      version='0.1',
      description='Python Spellsource library',
      url='http://github.com/hiddenswitch/Spellsource-Server',
      author='Benjamin Berman',
      author_email='ben@hiddenswitch.com',
      license='GPLv3',
      install_requires=['py4j'],
      packages=['pyspellsource'],
      zip_safe=False)
