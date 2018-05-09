from setuptools import setup
import os
import subprocess


def build_net():
    if os.name == "nt":
        subprocess.run(["./gradlew.bat", "net:shadowJar"], shell=True)
    else:
        subprocess.run(["./gradlew", "net:shadowJar"], shell=True)

build_net()

setup(name='pyspellsource',
      version='0.2.0',
      description='Python Spellsource library',
      url='http://github.com/hiddenswitch/Spellsource-Server',
      author='Benjamin Berman',
      data_files=[("share/pyspellsource", ['net/build/libs/net-1.3.0-all.jar'])],
      include_package_data=True,
      author_email='ben@hiddenswitch.com',
      license='GPLv3',
      install_requires=['py4j'],
      packages=['pyspellsource'],
      zip_safe=False)
