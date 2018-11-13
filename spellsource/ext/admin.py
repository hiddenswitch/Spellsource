from autoboto.services import iam


class Admin(object):
    _SPELLSOURCE_CONTRIBUTOR_GROUP_NAME = 'SpellsourceContributors'
    _DEFAULT_AWS_USER_POLICIES = (
        'arn:aws:iam::aws:policy/AWSElasticBeanstalkFullAccess',
        'arn:aws:iam::aws:policy/AmazonRekognitionFullAccess',
        'arn:aws:iam::aws:policy/AmazonS3FullAccess'
    )

    @staticmethod
    def create_user(username: str, profile_name: str = 'default'):
        """
        Creates an AWS user for the calling user's AWS account. Prints the credentials file.

        The following policies are added by default:
         - AWSElasticBeanstalkFullAccess
         - AmazonRekognitionFullAccess
         - AmazonS3FullAccess
        :param username: The user's name to create. If it already exists this will fail early.
        :param profile_name: The profile name to use for the emitted credentials file. Defaults to 'default'.
        :return: A string with newlines representing the ~/.aws/credentials file for that user
        """
        aws = iam.Client()
        if username in aws.list_users(path_prefix=username).users:
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
