# Route53 DynDNS Script for Docker

**Installation**:

Installs the package and its crontab entry.

```
python setup.py install install_cron
```

Uses the following environment variables for configuration:

**Required:**

 - `DDNSROUTE53_IN_DOMAINS`: A space-separated list of domains to update.
 - `AWS_ACCESS_KEY_ID`: The AWS access key ID to use.
 - `AWS_SECRET_ACCESS_KEY`: The AWS secret access key.
 
**Optional:**

 - `DDNSROUTE53_TTL`: The time to live for the configured DNS entries.
 - `DDNSROUTE53_IP_ADDRESS`: The IP address to set. By default or when set to the string "PUBLIC", uses the public IP address (as visible from the wider Internet) of the machine that is executing the script.