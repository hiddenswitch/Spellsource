create type spellsource.rogue_run_state as enum (
    'INITIAL', -- Run has just started
    'FINISHED', -- Run has completed
    'PRE_MATCH', -- Match is available for them to start
    'IN_MATCH', -- Match is currently being played
    'CHOICE' -- Choosing rewards to add to deck
    );

/*create type spellsource.rogue_choice as
(
    cards    text[],
    can_pick int
);*/

create table if not exists spellsource.rogue_run
(
    id              bigint                      not null primary key generated always as identity,
    player          varchar(36)                 not null references keycloak.user_entity (id),
    started_at      timestamptz                 not null default now(),
    ended_at        timestamptz,
    deck            text unique                 not null references spellsource.decks (id),
    bosses_defeated int                         not null default 0,
    state           spellsource.rogue_run_state not null default 'INITIAL',
    choices         text[],
    game            bigint unique references spellsource.games (id),
    opponent_deck   text references spellsource.decks (id),
    seed            bigint                      not null default (random() * 1e10)::bigint
);
alter table spellsource.rogue_run
    enable row level security;
create policy rls on spellsource.rogue_run for select
    using (spellsource.get_user_id() = player);


-- the payload field names within SpellsourceRogueUpdate within RogueManager
create type spellsource.rogue_payload_type as enum (
    'start',
    'choice',
    'matchStart',
    'matchEnd',
    'resign'
    );

create or replace function spellsource.rogue_notify(rogue_run_id bigint, type spellsource.rogue_payload_type,
                                                    payload jsonb) returns void as
$$
begin
    perform pg_notify('spellsource_rogue_updates_v0',
                      jsonb_build_object
                      ('id', rogue_run_id,
                       type::text, payload
                      )::text);
end;
$$ volatile language plpgsql;


/* Called from client to begin a new Rogue Run */
create or replace function spellsource.start_rogue_run(class_hero text, use_seed bigint) returns spellsource.rogue_run as
$$
declare
    id_deck   text;
    user_id   varchar(36);
    rogue_run spellsource.rogue_run%rowtype;
    deck_id   text;
begin
    user_id := spellsource.get_user_id();

    id_deck := gen_random_uuid();

    insert into spellsource.decks (id, created_by, last_edited_by, name, hero_class, deck_type, format)
    values (id_deck::text, spellsource.get_user_id(), spellsource.get_user_id(), 'Rogue Deck', class_hero, 2, 'rogue')
    returning (id) into deck_id;


    insert into spellsource.rogue_run (player, started_at, deck, seed)
    values (user_id, now(), id_deck, use_seed)
    returning * into rogue_run;


    perform spellsource.rogue_notify(rogue_run.id, 'start',
                                     jsonb_build_object
                                     (
                                     )
            );

    return rogue_run;
end;
$$
    volatile
    language plpgsql
    security definer
    set search_path = spellsource, pg_temp;
grant execute on function spellsource.start_rogue_run to website;


create or replace function spellsource.make_rogue_choice(rogue_id bigint, choice_index int) returns void as
$$
declare
begin
    perform spellsource.rogue_notify(rogue_id, 'choice',
                                     jsonb_build_object
                                     ('index', choice_index
                                     )
            );
end;
$$ volatile language plpgsql
   security definer
   set search_path = spellsource, pg_temp;
grant execute on function spellsource.make_rogue_choice to website;


create or replace function spellsource.resign_rogue_run(rogue_id bigint) returns void as
$$
declare
begin
    perform spellsource.rogue_notify(rogue_id, 'resign', jsonb_build_object());
end;
$$ volatile language plpgsql
   security definer
   set search_path = spellsource, pg_temp;
grant execute on function spellsource.resign_rogue_run to website;


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


    perform spellsource.rogue_notify(rogue_run.id, 'matchStart', jsonb_build_object());
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


    perform spellsource.rogue_notify(rogue_run.id, 'matchEnd',
                                     jsonb_build_object
                                     ('gameId', game_id,
                                      'won', winning_user = rogue_run.player
                                     )
            );
end;
$$ volatile language plpgsql;