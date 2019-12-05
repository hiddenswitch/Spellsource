from setuptools import setup

setup(name='ddnsroute53',
      version='0.0.1',
      description='A simple command line application to update Route53 DNS with the originator\'s public IP address '
                  'on a specific interval',
      url='https://github.com/hiddenswitch/Spellsource-Server/tree/master/ddnsroute53',
      python_requires='>3.6',
      author='Benjamin Berman',
      author_email='ben@hiddenswitch.com',
      license='GPLv3',
      install_requires=['Click==7.0',
                        'autoboto==0.4.3',
                        'boto3',
                        'requests',
                        'tldextract'],
      entry_points={
          'console_scripts': [
              'ddnsroute53 = ddnsroute53.cmd:main'
          ]
      },
      classifiers=['Development Status :: 3 - Alpha',
                   'Intended Audience :: Developers',
                   'License :: OSI Approved :: GNU General Public License v3 (GPLv3)',
                   'Programming Language :: Python :: 3',
                   'Programming Language :: Python :: 3.6'])
