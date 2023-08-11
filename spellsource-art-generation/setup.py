from setuptools import setup, find_packages

# Read the requirements.txt file
with open('requirements.txt') as f:
    requirements = f.read().splitlines()

setup(
    name='spellsource_art_generation',
    version='0.10.0',
    packages=find_packages(),
    install_requires=requirements,
    author='doctorpangloss',
    author_email='',
    description='',
    license='AGPLv3',
    keywords='spellsource art generation',
    url='https://github.com/hiddenswitch/spellsource/spellsource_art_generation',  # your project URL
)
