do
$$
    begin
        create role website;
    exception
        when duplicate_object then
            null; -- do nothing, role already exists
    end
$$;

grant usage on schema spellsource to website;
grant select, insert, update on spellsource.cards to website;
grant select, insert, update on spellsource.decks to website;
grant select on spellsource.deck_shares to website;
grant select, update, insert, delete on spellsource.cards_in_deck to website;

create or replace function spellsource.get_user_id() returns text as
$$
select current_setting('user.id', true)::text;
$$ language sql stable;

alter table spellsource.cards_in_deck
    drop constraint if exists cards_in_deck_card_id_fkey;

alter table spellsource.cards
    add if not exists is_archived  bool not null default false,
    add if not exists is_published bool not null default true,
    alter is_published set default false,
    drop constraint if exists cards_pkey cascade,
    add if not exists succession   bigint primary key generated always as identity;
;

/*
alter table spellsource.cards_in_deck
    add column card bigint references spellsource.cards (succession);
*/

-- TODO using is_archived may mean we don't need to do any group by joining
drop materialized view if exists spellsource.current_cards cascade;
create materialized view spellsource.current_cards as
select c.*
from spellsource.cards c
         join (select id, max(succession) as max_succession
               from spellsource.cards
               where is_published
                 and not is_archived
               group by id) m
              on c.id = m.id and c.succession = m.max_succession;

/*
-- noinspection SqlWithoutWhere
update spellsource.cards_in_deck
set card = (select succession from spellsource.current_cards where id = card_id limit 1);
*/


alter table spellsource.cards
    enable row level security;
drop policy if exists website_view on spellsource.cards;
create policy website_view on spellsource.cards for select to website
    using (created_by = spellsource.get_user_id() or is_published);
drop policy if exists website_insert on spellsource.cards;
create policy website_insert on spellsource.cards for insert to website
    with check (created_by = spellsource.get_user_id() and
                starts_with(id, spellsource.get_user_id()) and
                (starts_with(card_script ->> 'heroClass', spellsource.get_user_id()) or
                 card_script ->> 'heroClass' = 'ANY')); -- TODO specific hero classes that anyone can publish for?
drop policy if exists website_update on spellsource.cards;
create policy website_update on spellsource.cards for update to website
    using (created_by = spellsource.get_user_id() and starts_with(id, spellsource.get_user_id()))
    with check (created_by = spellsource.get_user_id() and starts_with(id, spellsource.get_user_id()));

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


alter table spellsource.deck_shares
    enable row level security;
drop policy if exists website_view on spellsource.deck_shares;
create policy website_view on spellsource.deck_shares for select to website
    using (share_recipient_id = spellsource.get_user_id());


alter table spellsource.cards_in_deck
    enable row level security;
drop policy if exists website_view on spellsource.cards_in_deck;
create policy website_view on spellsource.cards_in_deck for select to website
    using (exists(select *
                  from spellsource.decks
                  where spellsource.can_see_deck(spellsource.get_user_id(), decks)));
drop policy if exists website_insert on spellsource.cards_in_deck;
create policy website_insert on spellsource.cards_in_deck for insert to website
    with check (exists(select *
                       from spellsource.decks
                       where id = deck_id
                         and created_by = spellsource.get_user_id()));
drop policy if exists website_update on spellsource.cards_in_deck;
create policy website_update on spellsource.cards_in_deck for update to website
    using (exists(select *
                  from spellsource.decks
                  where id = deck_id
                    and created_by = spellsource.get_user_id()))
    with check (exists(select *
                       from spellsource.decks
                       where id = deck_id
                         and created_by = spellsource.get_user_id()));
drop policy if exists website_delete on spellsource.cards_in_deck;
create policy website_delete on spellsource.cards_in_deck for delete to website
    using (exists(select *
                  from spellsource.decks
                  where id = deck_id
                    and created_by = spellsource.get_user_id()));

-- Policies for public
grant select on spellsource.decks to public;
drop policy if exists public_view on spellsource.decks;
create policy public_view on spellsource.decks for select to public using (is_premade);

grant select on spellsource.cards to public;
drop policy if exists public_view on spellsource.cards;
create policy public_view on spellsource.cards for select to website
    using (is_published);

create or replace function spellsource.get_latest_card(card_id text, published bool)
    returns spellsource.cards as
$$
select *
from spellsource.cards
where id = $1
  and is_published = $2
  and not is_archived
order by succession desc
limit 1
$$ language sql stable;

create or replace function spellsource.cards_in_deck_card_by_card_id(cards_in_deck spellsource.cards_in_deck) returns spellsource.cards as
$$
begin
    return spellsource.get_latest_card(cards_in_deck.card_id, true);
end;
$$ language plpgsql stable;

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

create or replace function spellsource.cards_collectible(card spellsource.cards) returns bool as
$$
begin
    return coalesce(card.card_script ->> 'collectible', 'true')::bool;
end;
$$ language plpgsql stable;

create or replace function spellsource.archive_card(card_id text) returns void as
$$
begin
    update spellsource.cards set is_archived = true where id = card_id and is_published = false;
end;
$$ language plpgsql volatile;

create or replace function spellsource.save_card(card_id text, workspace xml, json jsonb) returns spellsource.cards as
$$
declare
    card spellsource.cards;
begin
    card := spellsource.get_latest_card(card_id, false);

    if (card is null) then
        insert into spellsource.cards (id, created_by, blockly_workspace, card_script)
        values (card_id, spellsource.get_user_id(), workspace, json)
        returning * into card;
    else
        update spellsource.cards
        set blockly_workspace = workspace,
            card_script       = json,
            last_modified     = now()
        where cards.succession = card.succession;
    end if;

    return card;
end;
$$ language plpgsql volatile;

create or replace function spellsource.refresh_current_cards() returns void
as
$$
begin
    refresh materialized view spellsource.current_cards;
end
$$ language plpgsql security definer;

grant execute on function spellsource.refresh_current_cards() to website;

create or replace function spellsource.publish_card(card_id text) returns bigint as
$$
declare
    card   spellsource.cards;
    result bigint;
begin
    -- assumes that save has already been called
    card := spellsource.get_latest_card(card_id, false);

    if (card is null) then
        raise exception 'Trying to publish card % that has never been saved', card_id;
    end if;

    -- create the new published card; triggers will handle side effects
    insert into spellsource.cards (id, created_by, blockly_workspace, card_script, created_at, is_published)
    values (card.id, card.created_by, card.blockly_workspace, card.card_script, card.created_at, true)
    returning succession into result;

    -- TODO update decks?

    return result;
end;
$$ language plpgsql volatile;

-- Publish a card from git, or don't if it's already up to date
create or replace function spellsource.publish_git_card(card_id text, json jsonb, creator varchar) returns spellsource.cards as
$$
declare
    card         spellsource.cards;
    created_time timestamptz;
begin
    created_time := now();
    
    select * from spellsource.cards where id = card_id and is_published and not is_archived into card;

    if (not (card is null)) then
        if (card.card_script @> json and json @> card.card_script) then
            -- no changes needed
            return card;
        end if;

        created_time := card.created_at;
    end if;

    -- create the new published card; triggers will handle side effects
    insert into spellsource.cards (id, created_by, card_script, created_at, is_published)
    values (card_id, creator, json, created_time, true)
    returning * into card;
    
    return card;
end;
$$ language plpgsql volatile;

create or replace function spellsource.before_card_published() returns trigger as
$$
declare
begin
    update spellsource.cards set is_archived = true where id = new.id and is_published;

    return new;
end;
$$ language plpgsql;

drop trigger if exists before_card_published on spellsource.cards;
create trigger before_card_published
    before insert or update
    on spellsource.cards
    for each row
    when ( new.is_published and not new.is_archived )
execute procedure spellsource.before_card_published();


create or replace function spellsource.after_card_published() returns trigger as
$$
declare
begin
    execute spellsource.refresh_current_cards();

    return new;
end;
$$ language plpgsql;

drop trigger if exists after_card_published on spellsource.cards;
create trigger after_card_published
    before insert or update
    on spellsource.cards
    for each row
    when ( new.is_published and not new.is_archived )
execute procedure spellsource.after_card_published();

--- Making the view as a function so it respects RLS
drop function if exists spellsource.get_classes() cascade;
create function spellsource.get_classes()
    returns table
            (
                created_by   varchar,
                class        text,
                is_published bool,
                collectible  bool,
                card_script  jsonb,
                id           text,
                name         text
            )
    language sql
    security invoker
as
$$
select distinct created_by,
                card_script ->> 'heroClass'                           as class,
                is_published,
                coalesce(card_script ->> 'collectible', 'true')::bool as collectible,
                card_script,
                id,
                card_script ->> 'name'                                as name
from spellsource.cards
where card_script ->> 'type' = 'CLASS';
$$;
create or replace view spellsource.classes as
select *
from spellsource.get_classes();
grant select on spellsource.classes to website;

create or replace function spellsource.card_message(card spellsource.cards, cl spellsource.classes) returns text as
$$
begin
    return coalesce(card.card_script ->> 'baseManaCost', '0') || ' ' || coalesce(card.card_script ->> 'name', '') ||
           ' ' || cl.name || ' ' || coalesce(card.card_script ->> 'description', '') || ' ' ||
           coalesce(card.card_script ->> 'race', '') || ' ' || coalesce(card.card_script ->> 'set', 'CUSTOM');
end;
$$ language plpgsql stable;


--- Making the view as a function so it respects RLS
drop function if exists spellsource.get_collection_cards() cascade;
create or replace function spellsource.get_collection_cards()
    returns table
            (
                id                text,
                created_by        varchar,
                card_script       jsonb,
                blockly_workspace xml,
                name              text,
                type              text,
                class             text,
                cost              int,
                collectible       bool,
                search_message    text,
                last_modified     timestamptz,
                created_at        timestamptz
            )
    language sql
    security invoker
as
$$
select card.id,
       card.created_by,
       card.card_script,
       card.blockly_workspace,
       card.card_script ->> 'name'                                    as name,
       card.card_script ->> 'type'                                    as type,
       card.card_script ->> 'heroClass'                               as class,
       coalesce(card.card_script ->> 'baseManaCost', '0')::int        as cost,
       (coalesce(card.card_script ->> 'collectible', 'true')::bool and
        (card.card_script ->> 'heroClass' = 'ANY' or cl.collectible)) as collectible,
       spellsource.card_message(card::spellsource.cards, cl)          as search_message,
       last_modified,
       created_at
from spellsource.cards card
         join spellsource.classes cl on card.card_script ->> 'heroClass' = cl.class
where card.card_script ->> 'set' != 'TEST'
  and not card.is_archived
  and card.is_published;
$$;
create or replace view spellsource.collection_cards as
select *
from spellsource.get_collection_cards();
grant select on spellsource.collection_cards to website;


create or replace function spellsource.set_cards_in_deck(deck text, card_ids text[]) returns setof spellsource.cards_in_deck as
$$
declare
    card text;
begin
    delete from spellsource.cards_in_deck where deck_id = deck;

    foreach card in array card_ids
        loop
            insert into spellsource.cards_in_deck (deck_id, card_id) values (deck, card);
        end loop;

    return query select * from spellsource.cards_in_deck where deck_id = deck;
end
$$ language plpgsql volatile;
grant execute on function spellsource.set_cards_in_deck(text, text[]) to website;

create or replace function spellsource.create_deck_with_cards(deck_name text, class_hero text, format_name text, card_ids text[]) returns spellsource.decks as
$$
declare
    id_deck text;
    card    text;
    deck    spellsource.decks;
begin
    id_deck := gen_random_uuid();

    insert into spellsource.decks (id, created_by, last_edited_by, name, hero_class, deck_type, format)
    values (id_deck::text, spellsource.get_user_id(), spellsource.get_user_id(), deck_name, class_hero, 1, format_name)
    returning * into deck;

    if (card_ids is not null) then
        foreach card in array card_ids
            loop
                insert into spellsource.cards_in_deck (deck_id, card_id) values (id_deck, card);
            end loop;
    end if;

    return deck;
end
$$ language plpgsql volatile;
grant execute on function spellsource.create_deck_with_cards(text, text, text, text[]) to website;

--- adding indices for querying the card script efficiently
create index if not exists idx_card_script_name
    on spellsource.cards ((card_script ->> 'name'));

create index if not exists idx_card_script_type
    on spellsource.cards ((card_script ->> 'type'));

create index if not exists idx_card_script_heroclass
    on spellsource.cards ((card_script ->> 'heroClass'));

create index if not exists idx_card_script_set
    on spellsource.cards ((card_script ->> 'set'));

create index if not exists idx_card_script_rarity
    on spellsource.cards ((card_script ->> 'rarity'));

create index if not exists idx_card_script_attributes
    on spellsource.cards using gin ((card_script -> 'attributes'));

create index if not exists idx_card_script_draft_banned
    on spellsource.cards ((card_script -> 'draft' ->> 'banned'));

create index if not exists idx_card_script_ai_hardremoval
    on spellsource.cards ((card_script -> 'artificialIntelligence' ->> 'hardRemoval'));

create index if not exists idx_card_id
    on spellsource.cards (id);

create index if not exists idx_card_id_succession
    on spellsource.cards (id, succession);

create unique index spellsource_cards_unique_id on spellsource.cards (id)
    where is_published and not is_archived;

--- formats()
create or replace function spellsource.card_catalogue_formats() returns setof spellsource.cards as
$$
begin
    return query
        select *
        from spellsource.cards
        where card_script ->> 'type' = 'FORMAT';
end;
$$ language plpgsql volatile;

--- getFormat(String name)
create or replace function spellsource.card_catalogue_get_format(card_name text)
    returns setof spellsource.cards as
$$
begin
    return query
        select *
        from spellsource.cards
        where card_script ->> 'type' = 'FORMAT'
          and card_script ->> 'name' = card_name;
end;
$$ language plpgsql volatile;

--- getBannedDraftCards()
create table if not exists spellsource.banned_draft_cards
(
    card_id text not null unique primary key
);

create or replace function spellsource.card_catalogue_get_banned_draft_cards()
    returns table
            (
                card_id text
            )
as
$$
begin
    return query
        (select card_id
         from spellsource.banned_draft_cards
         union
         select id
         from spellsource.cards
         where card_script -> 'draft' ->> 'banned' = 'true');
end;
$$ language plpgsql;


--- getHardRemovalCardIds()
create table if not exists spellsource.hard_removal_cards
(
    card_id text not null unique primary key
);

create or replace function spellsource.card_catalogue_get_hard_removal_cards()
    returns table
            (
                card_id text
            )
as
$$
begin
    return query
        (select card_id
         from spellsource.hard_removal_cards
         union
         select id
         from spellsource.cards
         where card_script -> 'artificialIntelligence' ->> 'hardRemoval' = 'true');
end;
$$ language plpgsql;

--- getCardById
create or replace function spellsource.card_catalogue_get_card_by_id(card_id text)
    returns setof spellsource.cards as
$$
begin
    return query
        select *
        from spellsource.cards
        where id = card_id;
end;
$$ language plpgsql;

--- getCardByName(String name)
create or replace function spellsource.card_catalogue_get_card_by_name(card_name text)
    returns spellsource.cards as
$$
declare
    result_record spellsource.cards%rowtype;
begin
    select *
    into result_record
    from spellsource.cards
    where card_script ->> 'name' = card_name
    limit 1;

    return result_record;
exception
    when NO_DATA_FOUND then
        return null;
end;
$$ language plpgsql;

--- getCardByName(String name, String heroClass)
create or replace function spellsource.card_catalogue_get_card_by_name_and_class(card_name text, hero_class text)
    returns spellsource.cards as
$$
declare
    result_record spellsource.cards%rowtype;
begin
    select *
    into result_record
    from spellsource.cards
    where card_script ->> 'name' = card_name
      and card_script ->> 'heroClass' = hero_class
    limit 1;

    return result_record;
exception
    when NO_DATA_FOUND then
        return null;
end;
$$ language plpgsql;

--- query
create or replace function spellsource.card_catalogue_query(
    sets text[],
    card_type text,
    rarity text,
    hero_class text,
    attribute text
)
    returns setof spellsource.cards as
$$
begin
    return query
        select *
        from spellsource.cards
        where (sets is null or card_script ->> 'set' = any (sets))
          and (card_type is null or card_script ->> 'type' = card_type)
          and (rarity is null or card_script ->> 'rarity' = rarity)
          and (hero_class is null or card_script ->> 'heroClass' = hero_class)
          and (attribute is null or card_script -> 'attributes' ? attribute);
end;
$$ language plpgsql;



--- getHeroCard
create or replace function spellsource.card_catalogue_get_hero_card(hero_class text)
    returns spellsource.cards as
$$
declare
    result_record spellsource.cards%rowtype;
begin
    select *
    into result_record
    from spellsource.cards
    where card_script ->> 'heroClass' = hero_class
      and card_script ->> 'type' = 'HERO'
    limit 1;

    return result_record;
exception
    when NO_DATA_FOUND then
        return null;
end;
$$ language plpgsql;

--- getClassCards
create or replace function spellsource.card_catalogue_get_class_cards()
    returns setof spellsource.cards as
$$
begin
    return query
        select *
        from spellsource.cards
        where card_script ->> 'type' = 'CLASS';
end;
$$ language plpgsql;

-- getBaseClasses
create or replace function spellsource.card_catalogue_get_base_classes(sets text[])
    returns setof spellsource.cards as
$$
begin
    return query
        select *
        from spellsource.cards
        where card_script ->> 'type' = 'CLASS'
          and (card_script ->> 'set') = any (sets);
end;
$$ language plpgsql;

--- whenever one of the cards change, this will be fired for the purposes of invalidating caches
create or replace function spellsource.card_change_notify_event() returns trigger as
$$
declare
    payload json;
begin
    payload := json_build_object(
            '__table', tg_table_name,
            'id', new.id
        );
    perform pg_notify('spellsource_cards_changes_v0', payload::text);
    return new;
end;
$$ language plpgsql;

create trigger card_changes_trigger
    after insert or update or delete
    on spellsource.cards
    for each row
execute procedure spellsource.card_change_notify_event();

--- helps record replays
create or replace function spellsource.clustered_games_update_game_and_users(
    p_user_id_winner text,
    p_user_id_loser text,
    p_game_id bigint,
    p_trace json
)
    returns boolean as
$$
begin
    -- equivalent of the first jooq update query
    update game_users
    set victory_status = 'WON'::spellsource.game_user_victory_enum
    where user_id = p_user_id_winner
      and game_id = p_game_id;

    -- equivalent of the second jooq update query
    update game_users
    set victory_status = 'LOST'::spellsource.game_user_victory_enum
    where user_id = p_user_id_loser
      and game_id = p_game_id;

    -- equivalent of the third jooq update query
    update games
    set status = 'FINISHED'::spellsource.game_state_enum,
        trace  = p_trace
    where id = p_game_id;

    return true;
exception
    when others then
        return false;
end;
$$ language plpgsql;


--- todo: implement draft queries. the draft logic may be entirely replaced by a sql function, where it will be easier to author
--- todo: create corresponding views and grant execution privileges to the card builder for these catalogue queries

select *
from spellsource.cards
where is_published = false;