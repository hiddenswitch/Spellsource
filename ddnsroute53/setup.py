from setuptools import setup
from setuptools.command.install import install

CRONTAB_COMMENT_ID = 'ddnsroute53 set IP address'


class InstallCrontab(install):
    def run(self):
        install.run(self)
        try:
            from crontab import CronTab
        except:
            try:
                from pip import main as pipmain
            except:
                from pip._internal.main import main as pipmain

            def pip_install(package):
                pipmain(['install', package])

            pip_install('python-crontab')
            from crontab import CronTab
        system_cron = CronTab(tabfile='/etc/crontab', user='root')
        if len(list(system_cron.find_comment(CRONTAB_COMMENT_ID))) == 0:
            # Automatically reads from environment variables with the DDNSROUTE53 prefix.
            job = system_cron.new('ddnsroute53 update', comment=CRONTAB_COMMENT_ID, user='root')
            job.minute.every(5)
            system_cron.write()


setup(name='ddnsroute53',
      version='0.0.3',
      description='Update Route53 DNS with the originator\'s public IP address, install to crontab using install_cron',
      url='https://github.com/hiddenswitch/Spellsource-Server/tree/master/ddnsroute53',
      python_requires='>3.7',
      author='Benjamin Berman',
      author_email='ben@hiddenswitch.com',
      license='AGPLv3',
      packages=['ddnsroute53'],
      install_requires=['Click==7.0',
                        'autoboto==0.4.3',
                        'python-dateutil==2.8.0',
                        'boto3',
                        'requests',
                        'tldextract',
                        'python-crontab'],
      entry_points={
          'console_scripts': [
              'ddnsroute53 = ddnsroute53.cmd:main'
          ]
      },
      zip_safe=True,
      cmdclass={'install_cron': InstallCrontab},
      classifiers=['Development Status :: 3 - Alpha',
                   'Intended Audience :: Developers',
                   'License :: OSI Approved :: GNU General Public License v3 (GPLv3)',
                   'Programming Language :: Python :: 3',
                   'Programming Language :: Python :: 3.7'])
