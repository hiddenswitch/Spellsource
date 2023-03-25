create role website;

grant usage on schema spellsource to website;
grant select, insert, update on spellsource.cards to website;
grant select, insert, update, delete on spellsource.decks to website;
grant select on spellsource.deck_shares to website;
grant select, update, insert, delete on spellsource.cards_in_deck to website;

create or replace function spellsource.get_user_id() returns text as
$$
select current_setting('user.id', true)::text;
$$ language sql stable;


alter table spellsource.cards
    enable row level security;
drop policy if exists website_view on spellsource.cards;
create policy website_view on spellsource.cards for select to website
    using (true);
drop policy if exists website_insert on spellsource.cards;
create policy website_insert on spellsource.cards for insert to website
    with check (created_by = spellsource.get_user_id());
drop policy if exists website_update on spellsource.cards;
create policy website_update on spellsource.cards for update to website
    using (created_by = spellsource.get_user_id())
    with check (created_by = spellsource.get_user_id());
drop policy if exists website_delete on spellsource.cards;
create policy website_delete on spellsource.cards for delete to website
    using (created_by = spellsource.get_user_id());

create or replace function spellsource.can_see_deck(user_id text, deck spellsource.decks) returns boolean as
$$
begin
    return deck.created_by = user_id
        or deck.is_premade
        or exists(select *
                  from spellsource.deck_shares
                  where deck_id = deck.id
                    and share_recipient_id = spellsource.get_user_id()
                    and not deck.trashed);
end;
$$ language plpgsql stable;


alter table spellsource.decks
    enable row level security;
drop policy if exists website_view on spellsource.decks;
create policy website_view on spellsource.decks for select to website
    using (spellsource.can_see_deck(spellsource.get_user_id(), decks));
drop policy if exists website_insert on spellsource.decks;
create policy website_insert on spellsource.decks for insert to website
    with check (created_by = spellsource.get_user_id());
drop policy if exists website_update on spellsource.decks;
create policy website_update on spellsource.decks for update to website
    using (created_by = spellsource.get_user_id())
    with check (created_by = spellsource.get_user_id() and last_edited_by = spellsource.get_user_id());
drop policy if exists website_delete on spellsource.decks;
create policy website_delete on spellsource.decks for delete to website
    using (created_by = spellsource.get_user_id());


alter table spellsource.deck_shares
    enable row level security;
drop policy if exists website_view on spellsource.deck_shares;
create policy website_view on spellsource.deck_shares for select to website
    using (share_recipient_id = spellsource.get_user_id());


alter table spellsource.cards_in_deck
    enable row level security;
drop policy if exists website_view on spellsource.cards_in_deck;
create policy website_view on spellsource.cards_in_deck for select to website
    using (exists(select * from spellsource.decks where spellsource.can_see_deck(spellsource.get_user_id(), decks)));
drop policy if exists website_insert on spellsource.cards_in_deck;
create policy website_insert on spellsource.cards_in_deck for insert to website
    with check (exists(select * from spellsource.decks where id = deck_id and created_by = spellsource.get_user_id()));
drop policy if exists website_update on spellsource.cards_in_deck;
create policy website_update on spellsource.cards_in_deck for update to website
    using (exists(select * from spellsource.decks where id = deck_id and created_by = spellsource.get_user_id()))
    with check (exists(select * from spellsource.decks where id = deck_id and created_by = spellsource.get_user_id()));
drop policy if exists website_delete on spellsource.cards_in_deck;
create policy website_delete on spellsource.cards_in_deck for delete to website
    using (exists(select * from spellsource.decks where id = deck_id and created_by = spellsource.get_user_id()));



create or replace function spellsource.cards_type(card spellsource.cards) returns text as
$$
begin
    return card.card_script ->> 'type';
end;
$$ language plpgsql stable;

create or replace function spellsource.cards_cost(card spellsource.cards) returns int as
$$
begin
    return coalesce(card.card_script ->> 'baseManaCost', '0')::int;
end;
$$ language plpgsql stable;
