# Route53 DynDNS Provider through Serverless

Configures a Lambda function that a networking appliance can use to update its IP address corresponding to a specific host name. Works with Route53.

The required parameters are:
 
 - A `HOSTED_ZONE_ID`, which you can retrieve by using the command line:

```$sh
➜  ~ aws route53 list-hosted-zones
{
    "HostedZones": [
        {
            "Id": "/hostedzone/HOSTED_ZONE_ID",
            "Name": "fully.qualified.domain.name.com.",
            "CallerReference": "XYZ",
            "Config": {
                "PrivateZone": false
            },
            "ResourceRecordSetCount": 4
        }
    ]
}

➜  ~ aws route53 list-hosted-zones | jq --raw-output ".HostedZones[0].Id | split(\"/\") | .[2]"
HOSTED_ZONE_ID
```

 - A `SHARED_SECRET` which **must** contain a colon in the middle. For example the value could be `SHARED:SECRET`.

To deploy, use `deploy.sh`.