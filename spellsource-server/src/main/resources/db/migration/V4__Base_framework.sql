create schema if not exists spellsource;

create table spellsource.decks
(
  id text not null unique primary key,
  created_by character varying references keycloak.user_entity (id) not null,
  last_edited_by character varying references keycloak.user_entity (id) not null,
  name character varying,
  hero_class character varying,
  trashed boolean not null default false,
  format text,
  deck_type int not null,
  is_premade boolean not null default false,
  permitted_to_duplicate boolean not null default false
);

create index on spellsource.decks (created_by);
create index on spellsource.decks (is_premade) where decks.is_premade is true;
create index on spellsource.decks (trashed) where decks.is_premade is false;

comment on column spellsource.decks.created_by is 'who created this deck originally';
comment on column spellsource.decks.last_edited_by is 'who last edited this deck';
comment on column spellsource.decks.is_premade is 'premades always shared with all users by application logic';

create table spellsource.cards
(
  id text not null primary key unique,
  created_by character varying references keycloak.user_entity (id) not null,
  uri text null,
  blockly_workspace xml null,
  card_script jsonb null,
  created_at timestamptz not null default now(),
  last_modified timestamptz not null default now()
);

create table spellsource.cards_in_deck
(
  id bigint generated always as identity primary key unique,
  deck_id text not null references spellsource.decks (id) on delete cascade,
  card_id text not null references spellsource.cards (id) on delete cascade
);

create table spellsource.deck_player_attribute_tuples
(
  id bigint generated always as identity primary key unique,
  deck_id text not null references spellsource.decks (id) on delete cascade,
  attribute int not null,
  string_value text
);

comment on column spellsource.cards_in_deck.deck_id is 'deleting a deck deletes all its card references';
comment on column spellsource.cards_in_deck.card_id is 'cannot delete cards that are currently used in decks';

create table spellsource.deck_shares
(
  deck_id text not null references spellsource.decks (id) on delete cascade,
  share_recipient_id text references keycloak.user_entity (id) not null,
  trashed_by_recipient boolean not null default false,
  primary key (deck_id, share_recipient_id)
);

create index on spellsource.deck_shares (trashed_by_recipient) where deck_shares.trashed_by_recipient is false;

comment on table spellsource.deck_shares is 'indicates a deck shared to a player';

create type spellsource.game_state_enum as enum
  (
    'AWAITING_CONNECTIONS',
    'STARTED',
    'FINISHED'
    );

create type spellsource.game_user_victory_enum as enum
  (
    'UNKNOWN',
    'WON',
    'LOST',
    'DISCONNECTED',
    'CONCEDED'
    );

create table spellsource.games
(
  id bigint generated always as identity primary key unique,
  status spellsource.game_state_enum not null default 'AWAITING_CONNECTIONS'::spellsource.game_state_enum,
  git_hash text,
  trace jsonb,
  created_at timestamptz not null default now()
);

create table spellsource.game_users
(
  player_index int2 default 0,
  game_id bigint references spellsource.games (id) on delete cascade,
  user_id text references keycloak.user_entity (id) on delete cascade,
  deck_id text references spellsource.decks (id) on delete set null,
  victory_status spellsource.game_user_victory_enum not null default 'UNKNOWN'::spellsource.game_user_victory_enum,
  primary key (game_id, user_id)
);

create table spellsource.matchmaking_queues
(
  id text not null primary key unique,
  name text not null,
  bot_opponent boolean not null default false,
  private_lobby boolean not null default false,
  starts_automatically boolean not null default true,
  still_connected_timeout bigint not null default 2000,
  empty_lobby_timeout bigint not null default 0,
  awaiting_lobby_timeout bigint not null default 0,
  once boolean not null default false,
  automatically_close boolean not null default true,
  lobby_size int not null default 2 check (lobby_size <= 2 and lobby_size >= 0),
  queue_created_at timestamptz not null default now()
);

create table spellsource.matchmaking_tickets
(
  ticket_id bigint generated always as identity,
  queue_id text references spellsource.matchmaking_queues (id) on delete cascade,
  user_id text unique references keycloak.user_entity (id) on delete cascade,
  deck_id text references spellsource.decks (id) on delete cascade,
  bot_deck_id text null default null references spellsource.decks (id),
  created_at timestamptz not null default now(),
  primary key (user_id)
); /* partition by range (queue_id);*/

create index on spellsource.matchmaking_tickets (queue_id);

create table spellsource.bot_users
(
  id text references keycloak.user_entity (id) on delete cascade primary key
);

create table spellsource.user_entity_addons
(
  id text references keycloak.user_entity (id) on delete cascade primary key,
  privacy_token text default floor(1000 + random() * 8999),
  migrated boolean default false,
  show_premade_decks boolean default true
);

create table spellsource.friends
(
  id text references keycloak.user_entity (id) on delete cascade,
  friend text references keycloak.user_entity (id) on delete cascade,
  created_at timestamptz not null default now(),
  primary key (id, friend)
);

create table spellsource.guests
(
    id bigint generated always as identity primary key unique,
    user_id text default null references keycloak.user_entity (id) on delete cascade
);