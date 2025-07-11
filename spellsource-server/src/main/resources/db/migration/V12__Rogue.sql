create type spellsource.rogue_run_state as enum (
    'INITIAL', -- Run has just started
    'FINISHED', -- Run has completed
    'PRE_MATCH', -- Match is available for them to start
    'IN_MATCH', -- Match is currently being played
    'CHOICE' -- Choosing rewards to add to deck
    );

create table if not exists spellsource.rogue_run
(
    id              bigint                      not null primary key generated always as identity,
    player          varchar(36)                 not null references keycloak.user_entity (id),
    started_at      timestamptz                 not null default now(),
    ended_at        timestamptz,
    hero_class      text                        not null,
    deck            text unique                 not null references spellsource.decks (id),
    bosses_defeated int                         not null default 0,
    state           spellsource.rogue_run_state not null default 'INITIAL',
    game            bigint unique references spellsource.games (id),
    opponent_deck   text references spellsource.decks (id),
    seed            bigint                      not null default (random() * 1e10)::bigint
);
grant select on spellsource.rogue_run to website;
alter table spellsource.rogue_run
    enable row level security;
create policy rls on spellsource.rogue_run for select
    using (spellsource.get_user_id() = player);


create table spellsource.rogue_choice
(
    id        bigint not null primary key generated always as identity,
    rogue_run bigint not null references spellsource.rogue_run (id),
    cards     text[] not null,
    can_pick  int    not null default 1,
    index     int    not null default 0
);
grant select on spellsource.rogue_choice to website;
alter table spellsource.rogue_choice
    enable row level security;
create policy rls on spellsource.rogue_choice for select
    using (exists(select *
                  from spellsource.rogue_run
                  where id = spellsource.rogue_choice.rogue_run
                    and player = spellsource.get_user_id()));


create or replace function spellsource.start_rogue_run(class_hero text, use_seed bigint) returns spellsource.rogue_run as
$$
declare
    id_deck   text;
    user_id   varchar(36);
    rogue_run spellsource.rogue_run%rowtype;
    deck_id   text;
begin
    user_id := spellsource.get_user_id();

    if user_id is null or user_id = '' then
        raise exception 'User not logged in';
    end if;

    id_deck := gen_random_uuid();

    insert into spellsource.decks (id, created_by, last_edited_by, name, hero_class, deck_type, format)
    values (id_deck::text, user_id, user_id, 'Rogue Deck', class_hero, 2, 'Rogue')
    returning (id) into deck_id;


    insert into spellsource.rogue_run (player, started_at, deck, seed, hero_class)
    values (user_id, now(), id_deck, use_seed, class_hero)
    returning * into rogue_run;

    return rogue_run;
end;
$$
    volatile
    language plpgsql
    security definer
    set search_path = spellsource, pg_temp;
-- grant execute on function spellsource.start_rogue_run to website;


create or replace function spellsource.resign_rogue_run(rogue_id bigint) returns void as
$$
declare
begin
    update spellsource.rogue_run set ended_at = now(), state = 'FINISHED' where id = rogue_id;
end;
$$ volatile language plpgsql
   security definer
   set search_path = spellsource, pg_temp;
-- grant execute on function spellsource.resign_rogue_run to website;


create or replace function spellsource.check_rogue_game_start(deck_id text, game_id bigint) returns void as
$$
declare
    deck      spellsource.decks%rowtype;
    rogue_run spellsource.rogue_run%rowtype;
begin
    select * from spellsource.decks where id = deck_id and deck_type = 2 into deck;

    if deck is null then
        return;
    end if;

    update spellsource.rogue_run set game = game_id, state = 'IN_MATCH' where deck = deck_id returning * into rogue_run;
end;
$$ volatile language plpgsql;


create or replace function spellsource.check_rogue_game_end(game_id bigint, winning_user varchar(36)) returns void as
$$
declare
    rogue_run spellsource.rogue_run%rowtype;
begin
    select * from spellsource.rogue_run where game = game_id into rogue_run;

    if rogue_run is null then
        return;
    end if;


    if winning_user != rogue_run.player then
        update spellsource.rogue_run as r set ended_at = now(), state = 'FINISHED' where id = rogue_run.id;
        return;
    end if;

    update spellsource.rogue_run as r set bosses_defeated = r.bosses_defeated + 1 where id = rogue_run.id;
end;
$$ volatile language plpgsql;


create or replace function spellsource.current_rogue_choice(rogue_id bigint) returns spellsource.rogue_choice as
$$
declare
    choice spellsource.rogue_choice%rowtype;
begin
    select *
    from spellsource.rogue_choice as rc
    where rc.rogue_run = rogue_id
    order by index
    limit 1
    into choice;

    return choice;
end;
$$
    language plpgsql
    stable;
grant execute on function spellsource.current_rogue_choice to website;


create or replace function spellsource.rogue_run_current_choice(rr spellsource.rogue_run) returns spellsource.rogue_choice as
$$
begin
    return spellsource.current_rogue_choice(rr.id);
end;
$$
    language plpgsql
    stable;
grant execute on function spellsource.rogue_run_current_choice to website;


create or replace function spellsource.get_cards_in_deck(deck text) returns setof text as
$$
select card_id
from spellsource.cards_in_deck
where deck_id = deck;
$$ language sql stable;