from os import makedirs
from random import randint
from subprocess import Popen, DEVNULL

import pymongo
from autoboto.services import iam
from time import sleep

from ..context import Context


class Admin(object):
    _SPELLSOURCE_CONTRIBUTOR_GROUP_NAME = 'SpellsourceContributors'
    _DEFAULT_AWS_USER_POLICIES = (
        'arn:aws:iam::aws:policy/AWSElasticBeanstalkFullAccess',
        'arn:aws:iam::aws:policy/AmazonRekognitionFullAccess',
        'arn:aws:iam::aws:policy/AmazonS3FullAccess',
        'arn:aws:iam::aws:policy/CloudWatchLogsFullAccess'
    )

    @staticmethod
    def change_user_password(mongo_uri: str, username_or_email: str, password: str,
                             db_name: str = 'metastone') -> dict:
        """
        Starts a server and changes the user password, then closes the server
        :param mongo_uri:
        :param username_or_email:
        :param password:
        :param db_name:
        :return:
        """
        # Connect to mongo first to get an early connection failure message
        client = pymongo.MongoClient(mongo_uri, connectTimeoutMS=4000)
        with Context() as context:
            Accounts = context.spellsource.Accounts
            secured_password = Accounts.securedPassword(password)  # type: str
        collection = client.get_database(db_name).get_collection(
            'accounts.users')  # type: pymongo.collection.Collection
        return collection.find_one_and_update(
            {'$or': [{'username': {'$regex': f'^{username_or_email}$', '$options': 'i'}},
                     {'emails.address': {'$regex': f'^{username_or_email}$', '$options': 'i'}}]},
            {'$set': {'services.password.scrypt': secured_password}},
            return_document=pymongo.ReturnDocument.AFTER)

    @staticmethod
    def replicate_mongo_db(path_to_pem_file: str, remote_host: str, db_path: str, tmp_dir: str):
        """
        Retrieves the production databases from the specified remote host and restores them to a database stored at
        db_path.
        :param path_to_pem_file:
        :param remote_host:
        :param db_path:
        :param tmp_dir:
        :return:
        """
        makedirs(db_path, exist_ok=True)
        port = randint(28000, 60000)
        ssh = Popen(
            args=['ssh', '-N', '-l', 'ec2-user', '-i', path_to_pem_file, '-L', f'{port}:127.0.0.1:27017', remote_host],
            stderr=DEVNULL,
            stdout=DEVNULL)
        sleep(0.5)
        mongodump = Popen(args=['mongodump', '-h', f'localhost:{port}', '-o', tmp_dir, '--oplog'],
                          stderr=DEVNULL, stdout=DEVNULL)
        mongodump.wait()
        ssh.terminate()
        mongod = Popen(args=['mongod', '--dbpath', db_path],
                       stderr=DEVNULL, stdout=DEVNULL)
        sleep(1.5)
        mongorestore = Popen(
            args=['mongorestore', '--drop', '--oplogReplay', '--uri=mongodb://localhost:27017', tmp_dir],
            stderr=DEVNULL, stdout=DEVNULL)
        mongorestore.wait()
        mongod.terminate()

    @staticmethod
    def create_user(username: str, profile_name: str = 'default'):
        """
        Creates an AWS user for the calling user's AWS account. Prints the credentials file.

        The following policies are added by default:
         - AWSElasticBeanstalkFullAccess
         - AmazonRekognitionFullAccess
         - AmazonS3FullAccess
         - CloudWatchLogsFullAccess
        :param username: The user's name to create. If it already exists this will fail early.
        :param profile_name: The profile name to use for the emitted credentials file. Defaults to 'default'.
        :return: A string with newlines representing the ~/.aws/credentials file for that user
        """
        aws = iam.Client()
        if username in aws.list_users(path_prefix='/' + username).users:
            raise ValueError(f'The username {username} already exists')
        aws.create_user(user_name=username)
        aws.add_user_to_group(group_name=Admin._SPELLSOURCE_CONTRIBUTOR_GROUP_NAME, user_name=username)
        for arn in Admin._DEFAULT_AWS_USER_POLICIES:
            aws.attach_user_policy(user_name=username, policy_arn=arn)
        access_key = aws.create_access_key(user_name=username)
        return f'''[{profile_name}]
aws_access_key_id = {access_key.access_key.access_key_id}
aws_secret_access_key = {access_key.access_key.secret_access_key}
'''
