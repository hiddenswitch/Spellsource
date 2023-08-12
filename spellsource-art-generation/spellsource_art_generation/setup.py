import typing

from setuptools import setup, find_packages
import os
import sys

"""
Indicates if we're installing an editable (develop) mode package
"""
is_editable = '--editable' in sys.argv or '-e' in sys.argv or (
        'python' in sys.argv and 'setup.py' in sys.argv and 'develop' in sys.argv)

requirements: typing.List[str] = []

# Read the requirements.txt file
with open('requirements.txt') as f:
    requirements = f.read().splitlines()

setup(
    name='spellsource_art_generation',
    version='0.10.0',
    packages=find_packages(exclude="comfyui"),
    install_requires=requirements,
    author='doctorpangloss',
    author_email='',
    description='',
    license='AGPLv3',
    keywords='spellsource art generation',
    url='https://github.com/hiddenswitch/spellsource/spellsource_art_generation',  # your project URL
)
