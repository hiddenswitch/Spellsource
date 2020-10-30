create schema if not exists spellsource;

create table spellsource.decks
(
  id text not null unique primary key,
  created_by character varying references keycloak.user_entity (id) not null,
  last_edited_by character varying references keycloak.user_entity (id) not null,
  name character varying,
  hero_class character varying,
  trashed boolean not null default false,
  format text
);

comment on column spellsource.decks.created_by is 'who created this deck originally';
comment on column spellsource.decks.last_edited_by is 'who last edited this deck';

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
  deck_id text not null references spellsource.decks (id) on delete cascade not null,
  card_id text not null references spellsource.cards (id)
);

comment on column spellsource.cards_in_deck.deck_id is 'deleting a deck deletes all its card references';
comment on column spellsource.cards_in_deck.card_id is 'cannot delete cards that are currently used in decks';