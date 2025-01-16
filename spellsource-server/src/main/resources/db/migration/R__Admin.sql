do
$$
    begin
        create role admin with createdb createrole login bypassrls;
    exception
        when duplicate_object then
            null; -- do nothing, role already exists
    end
$$;

grant all on schema spellsource to admin;
grant all on all tables in schema spellsource to admin;
grant all on all functions in schema spellsource to admin;
grant all on all routines in schema spellsource to admin;
grant all on all sequences in schema spellsource to admin;

grant all on schema keycloak to admin;
grant all on table keycloak.user_entity to admin;