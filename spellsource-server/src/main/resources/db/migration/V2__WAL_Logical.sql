do
$do$
  begin
    alter system set wal_level = logical;
  exception
    when others then
      raise notice 'not permitted to change wal_level';
  end
$do$