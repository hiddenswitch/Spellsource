FROM docker.io/postgres:13.6
COPY 99-hiddenswitch-customizations.sql /docker-entrypoint-initdb.d/
COPY plugins.sh /
RUN /plugins.sh

CMD [ "postgres", "-c", "wal_level=logical", "-c", "max_wal_senders=10", "-c", "max_replication_slots=10" ]
