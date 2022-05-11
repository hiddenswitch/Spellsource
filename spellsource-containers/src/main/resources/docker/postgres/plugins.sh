#!/usr/bin/env bash
set -eux
apt-get update
apt-get install --no-install-recommends -y make gcc postgresql-server-dev-13 git ca-certificates

# wal2json https://www.graphile.org/postgraphile/live-queries/#graphilesubscriptions-lds
cp /usr/share/postgresql/postgresql.conf.sample /etc/postgresql/postgresql.conf

echo "wal_level = logical
max_wal_senders = 10
max_replication_slots = 10" >> /etc/postgresql/postgresql.conf

git clone https://github.com/eulerto/wal2json.git
cd wal2json || exit
USE_PGXS=1 make
USE_PGXS=1 make install