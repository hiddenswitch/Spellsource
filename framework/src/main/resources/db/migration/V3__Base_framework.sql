create schema if not exists spellsource;

create table spellsource.decks
(
  id bigint generated always as identity primary key,
  created_by character varying references keycloak.user_entity (id) not null,
  last_edited_by character varying references keycloak.user_entity (id) not null,
  name character varying,
  hero_class character varying,
  trashed boolean not null default false
);

comment on column spellsource.decks.created_by is 'who created this deck originally';
comment on column spellsource.decks.last_edited_by is 'who last edited this deck';

create table spellsource.card_desc
(
  id bigint generated always as identity primary key unique,
  uri text null,
  blockly_workspace xml null,
  card_script jsonb null
);

comment on table spellsource.card_desc is 'a particular instance of a card desc, which may be referenced by a card';

create table spellsource.cards
(
  id bigint generated always as identity,
  card_id text not null,
  card_desc bigint references spellsource.card_desc (id) on delete restrict not null,
  primary key (card_id, id)
);

comment on table spellsource.cards is 'references a card at a particular point in time';
comment on column spellsource.cards.card_desc is 'mutable reference to a card_desc allowing the author to dy';

create table spellsource.cards_in_deck
(
  id bigint generated always as identity primary key unique,
  deck_id bigint references spellsource.decks (id) on delete cascade not null,
  card_id text not null,
  version bigint not null,
  foreign key (card_id, version) references spellsource.cards (card_id, id)
);

comment on column spellsource.cards_in_deck.deck_id is 'deleting a deck deletes all its card references';
comment on column spellsource.cards_in_deck.card_id is 'cannot delete cards that are currently used in decks';