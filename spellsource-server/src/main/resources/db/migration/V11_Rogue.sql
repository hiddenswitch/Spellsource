create type spellsource.rogue_run_state as enum (
    'INITIAL', -- Run has just started
    'FINISHED', -- Run has completed
    'PRE_MATCH', -- Match is available for them to start
    'IN_MATCH', -- Match is currently being played
    'CHOICE' -- Choosing rewards to add to deck
    );


create table if not exists spellsource.rogue_runs
(
    id              bigint                      not null primary key generated always as identity,
    player          varchar(36)                 not null references keycloak.user_entity (id),
    started_at      timestamptz                 not null default now(),
    ended_at        timestamptz,
    deck            text                        not null references spellsource.decks (id),
    bosses_defeated int                         not null default 0,
    state           spellsource.rogue_run_state not null default 'INITIAL',
    data        jsonb
);
alter table spellsource.rogue_runs
    enable row level security;
create policy rls on spellsource.rogue_runs for all
    using (spellsource.get_user_id() = player)
    with check (spellsource.get_user_id() = player);