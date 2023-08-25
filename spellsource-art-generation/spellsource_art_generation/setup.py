import os.path
import platform

from pip._internal.index.collector import LinkCollector
from pip._internal.index.package_finder import PackageFinder
from pip._internal.models.search_scope import SearchScope
from pip._internal.models.selection_prefs import SelectionPreferences
from pip._internal.network.session import PipSession
from pip._internal.req import InstallRequirement
from pip._vendor.packaging.requirements import Requirement
from setuptools import setup, find_packages

"""
The name of the package.
"""
package_name = "spellsource_art_generation"

"""
The current version.
"""
version = '0.0.1'

"""
Packages that should have a specific option set when a GPU accelerator is present
"""
gpu_accelerated_packages = {"rembg": "rembg[gpu]"}

"""
The URL to the bitsandbytes package to use on Windows
"""
bitsandbytes_windows = "https://github.com/jllllll/bitsandbytes-windows-webui/releases/download/wheels/bitsandbytes-0.40.1.post1-py3-none-win_amd64.whl"


def dependencies() -> [str]:
    _dependencies = open(os.path.join(os.path.dirname(__file__), "requirements.txt")).readlines()
    # todo: also add all plugin dependencies
    session = PipSession()

    gpu_accelerated = False
    index_urls = ['https://pypi.org/simple']

    try:
        # pip 23
        finder = PackageFinder.create(LinkCollector(session, SearchScope([], index_urls, no_index=False)),
                                      SelectionPreferences(allow_yanked=False, prefer_binary=False,
                                                           allow_all_prereleases=True))
    except:
        try:
            # pip 22
            finder = PackageFinder.create(LinkCollector(session, SearchScope([], index_urls)),
                                          SelectionPreferences(allow_yanked=False, prefer_binary=False,
                                                               allow_all_prereleases=True)
                                          , use_deprecated_html5lib=False)
        except:
            raise Exception("upgrade pip with\npip install -U pip")
    for i, package in enumerate(_dependencies[:]):
        requirement = InstallRequirement(Requirement(package), comes_from=f"{package_name}=={version}")
        candidate = finder.find_best_candidate(requirement.name, requirement.specifier)
        if candidate.best_candidate is not None:
            if requirement.name == "bitsandbytes" and platform.system().lower() == 'windows':
                _dependencies[i] = f"{requirement.name} @ {bitsandbytes_windows}"
            if gpu_accelerated and requirement.name in gpu_accelerated_packages:
                _dependencies[i] = gpu_accelerated_packages[requirement.name]
    return _dependencies


setup(
    name=package_name,
    version=version,
    packages=find_packages(),
    install_requires=dependencies(),
    author='doctorpangloss',
    author_email='',
    description='',
    license='AGPLv3',
    keywords='spellsource art generation',
    url='https://github.com/hiddenswitch/spellsource/spellsource_art_generation',
    entry_points={
        'comfyui.custom_nodes': [
            'spellsource_art_generation = spellsource_custom_nodes',
        ],
    },
)
