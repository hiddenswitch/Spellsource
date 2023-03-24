drop function if exists spellsource.get_user_id();
create function spellsource.get_user_id() returns text as
$$
select current_setting('user.id', true)::text;
$$ language sql stable;


create role website;
grant usage on schema spellsource to website;
grant select, insert, update on spellsource.cards to website;
grant select, insert, update, delete on spellsource.decks to website;


alter table spellsource.cards
    enable row level security;
create policy website_view on spellsource.cards for select to website
    using (true);
create policy website_insert on spellsource.cards for insert to website
    with check (created_by = spellsource.get_user_id());
create policy website_update on spellsource.cards for update to website
    using (created_by = spellsource.get_user_id())
    with check (created_by = spellsource.get_user_id());
create policy website_delete on spellsource.cards for delete to website
    using (created_by = spellsource.get_user_id());


alter table spellsource.decks
    enable row level security;
create policy website_view on spellsource.decks for select to website
    using (true);
create policy website_insert on spellsource.decks for insert to website
    with check (created_by = spellsource.get_user_id());
create policy website_update on spellsource.decks for update to website
    using (created_by = spellsource.get_user_id())
    with check (created_by = spellsource.get_user_id() and last_edited_by = spellsource.get_user_id());
create policy website_delete on spellsource.decks for delete to website
    using (created_by = spellsource.get_user_id());
