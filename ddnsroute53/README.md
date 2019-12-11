# Route53 DynDNS Script for Docker

Updates Route53 with the current machine's IP address with a specified list of domains. Supports an easy way to install itself into CRON.

```
Usage: ddnsroute53 update [OPTIONS] [IN_DOMAINS]...

  Updates each domain in the list IN_DOMAINS in Route53.

  Requires AWS credentials to be configured. Uses the AWS_ACCESS_KEY_ID and
  AWS_SECRET_ACCESS_KEY environment vars, for example.

  Uses the public IP address of the executing machine by default. Or, set to
  the string PUBLIC to use the public IP address.

  Expects full domains, i.e. if the hosted zone is example.com and you want
  to route to *, specify *.example.com.

  Instead of providing these arguments, you can use environment variables
  corresponding to the argument names prefixed with DDNSROUTE53:

   - DDNSROUTE53_IN_DOMAINS (space separated list)  - DDNSROUTE53_TTL (in
   seconds)  - DDNSROUTE53_IP_ADDRESS (an IPv4 address, or PUBLIC to use the
   public IP address at runtime)

Options:
  --ttl INTEGER      the TTL  [default: 300]
  --ip-address TEXT  the IP address to use  [default: 157.131.201.94]
  --help             Show this message and exit.
```

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