import click
from typing import List, Dict
from json import dumps

import requests

PUBLIC_IP: str = requests.get('https://checkip.amazonaws.com').text.strip()


@click.group()
def _cli():
    pass


@_cli.command()
@click.argument('in_domains', nargs=-1)
@click.option('--ttl', required=False, help='the TTL', type=int, show_default=True, default=300)
@click.option('--ip-address', required=False, help='the IP address to use', type=str, show_default=True,
              default=PUBLIC_IP)
def update(in_domains: List[str], ttl: int = 300, ip_address: str = PUBLIC_IP):
    """
    Updates the IN_DOMAINS in Route53.

    Uses the public IP address of the executing machine by default.

    Expects full domains, i.e. if the hosted zone is example.com and you want to route to *, specify *.example.com.
    """
    if len(in_domains) == 0:
        return
    from autoboto.services import route53
    from autoboto.services.route53.shapes import ResourceRecord, ResourceRecordSet, Change, ChangeBatch
    from tldextract import extract
    from itertools import groupby

    client = route53.Client()

    tld_domain: Dict[str, List[str]] = {k: list(g) for k, g in
                                        groupby(in_domains, lambda d: extract(d).registered_domain)}
    tld_hosted_zone_id: Dict[str, str] = {extract(hosted_zone.name).registered_domain: hosted_zone.id for hosted_zone in
                                          client.list_hosted_zones().hosted_zones}
    for tld, domains in tld_domain.items():
        if tld not in tld_hosted_zone_id:
            click.echo(f'{tld} not found in hosted zones', err=True)
            raise click.Abort()
        hosted_zone_id = tld_hosted_zone_id[tld]
        batch = ChangeBatch(
            comment='ddns update',
            changes=[Change(action='UPSERT', resource_record_set=ResourceRecordSet(name=name, type='A', ttl=ttl,
                                                                                   resource_records=[ResourceRecord(
                                                                                       value=ip_address)]))
                     for name in domains])
        res = client.change_resource_record_sets(hosted_zone_id=hosted_zone_id,
                                                 change_batch=batch)
        click.echo(dumps(res.to_boto()))


def main():
    _cli()
