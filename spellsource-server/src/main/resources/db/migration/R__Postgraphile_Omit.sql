do $$
    declare
        r record;
        existing_comment text;
    begin
        for r in
            select
                n.nspname as schema,
                p.proname as function,
                pg_get_function_identity_arguments(p.oid) as args,
                p.oid
            from pg_proc p
                     join pg_namespace n on p.pronamespace = n.oid
            where n.nspname = 'spellsource'
            loop
                -- Only operate if the website role does NOT have EXECUTE permission
                if not has_function_privilege('website', r.oid, 'EXECUTE') then
                    existing_comment := obj_description(r.oid, 'pg_proc');

                    -- Skip if comment already contains @omit
                    if existing_comment is null or position('@omit' in existing_comment) = 0 then
                        -- Compose the new comment: @omit\n<existing comment>
                        execute format(
                                'comment on function %I.%I(%s) is %L',
                                r.schema,
                                r.function,
                                r.args,
                                '@omit' || coalesce(E'\n' || existing_comment, '')
                                );
                    end if;
                end if;
            end loop;
    end
$$;

do $$
    declare
        r record;
        existing_comment text;
        object_type text;
    begin
        for r in
            select
                n.nspname as schema,
                c.relname as name,
                c.relkind,
                c.oid
            from pg_class c
                     join pg_namespace n on c.relnamespace = n.oid
            where c.relkind in ('r', 'v', 'm')  -- table, view, matview
              and n.nspname = 'spellsource'
            loop
                -- Skip if the website role has SELECT access
                if not has_table_privilege('website', r.oid, 'SELECT') then
                    existing_comment := obj_description(r.oid, 'pg_class');

                    if existing_comment is null or position('@omit' in existing_comment) = 0 then
                        -- Determine the correct object type for COMMENT ON
                        object_type := case r.relkind
                                           when 'r' then 'TABLE'
                                           when 'v' then 'VIEW'
                                           when 'm' then 'MATERIALIZED VIEW'
                            end;

                        execute format(
                                'COMMENT ON %s %I.%I IS %L',
                                object_type,
                                r.schema,
                                r.name,
                                '@omit' || coalesce(E'\n' || existing_comment, '')
                                );
                    end if;
                end if;
            end loop;
    end
$$;
