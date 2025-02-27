--
-- PostgreSQL database dump
--

-- Dumped from database version 13.11 (Debian 13.11-1.pgdg110+1)
-- Dumped by pg_dump version 17.2 (Debian 17.2-1.pgdg120+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: hiddenswitch; Type: SCHEMA; Schema: -; Owner: admin
--

CREATE SCHEMA hiddenswitch;


ALTER SCHEMA hiddenswitch OWNER TO admin;

--
-- Name: keycloak; Type: SCHEMA; Schema: -; Owner: admin
--

CREATE SCHEMA keycloak;


ALTER SCHEMA keycloak OWNER TO admin;

--
-- Name: public; Type: SCHEMA; Schema: -; Owner: admin
--

-- *not* creating schema, since initdb creates it


ALTER SCHEMA public OWNER TO admin;

--
-- Name: spellsource; Type: SCHEMA; Schema: -; Owner: admin
--

CREATE SCHEMA spellsource;


ALTER SCHEMA spellsource OWNER TO admin;

--
-- Name: uuid-ossp; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA hiddenswitch;


--
-- Name: EXTENSION "uuid-ossp"; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION "uuid-ossp" IS 'generate universally unique identifiers (UUIDs)';


--
-- Name: game_state_enum; Type: TYPE; Schema: spellsource; Owner: admin
--

CREATE TYPE spellsource.game_state_enum AS ENUM (
    'AWAITING_CONNECTIONS',
    'STARTED',
    'FINISHED'
);


ALTER TYPE spellsource.game_state_enum OWNER TO admin;

--
-- Name: game_user_victory_enum; Type: TYPE; Schema: spellsource; Owner: admin
--

CREATE TYPE spellsource.game_user_victory_enum AS ENUM (
    'UNKNOWN',
    'WON',
    'LOST',
    'DISCONNECTED',
    'CONCEDED'
);


ALTER TYPE spellsource.game_user_victory_enum OWNER TO admin;

--
-- Name: archive_card(text); Type: FUNCTION; Schema: spellsource; Owner: admin
--

CREATE FUNCTION spellsource.archive_card(card_id text) RETURNS void
    LANGUAGE plpgsql
    AS $$
begin
    update spellsource.cards set is_archived = true where id = card_id and is_published = false;
end;
$$;


ALTER FUNCTION spellsource.archive_card(card_id text) OWNER TO admin;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: decks; Type: TABLE; Schema: spellsource; Owner: admin
--

CREATE TABLE spellsource.decks (
    id text NOT NULL,
    created_by character varying NOT NULL,
    last_edited_by character varying NOT NULL,
    name character varying,
    hero_class character varying,
    trashed boolean DEFAULT false NOT NULL,
    format text,
    deck_type integer NOT NULL,
    is_premade boolean DEFAULT false NOT NULL,
    permitted_to_duplicate boolean DEFAULT false NOT NULL
);


ALTER TABLE spellsource.decks OWNER TO admin;

--
-- Name: COLUMN decks.created_by; Type: COMMENT; Schema: spellsource; Owner: admin
--

COMMENT ON COLUMN spellsource.decks.created_by IS 'who created this deck originally';


--
-- Name: COLUMN decks.last_edited_by; Type: COMMENT; Schema: spellsource; Owner: admin
--

COMMENT ON COLUMN spellsource.decks.last_edited_by IS 'who last edited this deck';


--
-- Name: COLUMN decks.is_premade; Type: COMMENT; Schema: spellsource; Owner: admin
--

COMMENT ON COLUMN spellsource.decks.is_premade IS 'premades always shared with all users by application logic';


--
-- Name: can_see_deck(text, spellsource.decks); Type: FUNCTION; Schema: spellsource; Owner: admin
--

CREATE FUNCTION spellsource.can_see_deck(user_id text, deck spellsource.decks) RETURNS boolean
    LANGUAGE plpgsql STABLE
    AS $$
begin
    return deck.created_by = user_id
        or deck.is_premade
        or exists(select *
                  from spellsource.deck_shares
                  where deck_id = deck.id
                    and share_recipient_id = spellsource.get_user_id()
                    and not deck.trashed);
end;
$$;


ALTER FUNCTION spellsource.can_see_deck(user_id text, deck spellsource.decks) OWNER TO admin;

--
-- Name: cards; Type: TABLE; Schema: spellsource; Owner: admin
--

CREATE TABLE spellsource.cards (
    id text NOT NULL,
    created_by character varying NOT NULL,
    uri text,
    blockly_workspace jsonb,
    card_script jsonb,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    last_modified timestamp with time zone DEFAULT now() NOT NULL,
    is_archived boolean DEFAULT false NOT NULL,
    is_published boolean DEFAULT false NOT NULL,
    succession bigint NOT NULL
);


ALTER TABLE spellsource.cards OWNER TO admin;

--
-- Name: COLUMN cards.uri; Type: COMMENT; Schema: spellsource; Owner: admin
--

COMMENT ON COLUMN spellsource.cards.uri IS 'The URI of the application that created this card. The git URL by default represents cards that came from the
    Spellsource git repository. https://www.getspellsource.com/cards/editor or similar represents cards authored in the
    web interface';


--
-- Name: card_catalogue_formats(); Type: FUNCTION; Schema: spellsource; Owner: admin
--

CREATE FUNCTION spellsource.card_catalogue_formats() RETURNS SETOF spellsource.cards
    LANGUAGE plpgsql
    AS $$
begin
    return query
        select *
        from spellsource.cards
        where card_script ->> 'type' = 'FORMAT'
          and is_published
          and not is_archived;
end;
$$;


ALTER FUNCTION spellsource.card_catalogue_formats() OWNER TO admin;

--
-- Name: card_catalogue_get_banned_draft_cards(); Type: FUNCTION; Schema: spellsource; Owner: admin
--

CREATE FUNCTION spellsource.card_catalogue_get_banned_draft_cards() RETURNS TABLE(card_id text)
    LANGUAGE plpgsql
    AS $$
begin
    return query
        (select card_id
         from spellsource.banned_draft_cards
         union
         select id
         from spellsource.cards
         where card_script -> 'draft' ->> 'banned' = 'true'
           and is_published
           and not is_archived);
end;
$$;


ALTER FUNCTION spellsource.card_catalogue_get_banned_draft_cards() OWNER TO admin;

--
-- Name: card_catalogue_get_base_classes(text[]); Type: FUNCTION; Schema: spellsource; Owner: admin
--

CREATE FUNCTION spellsource.card_catalogue_get_base_classes(sets text[]) RETURNS SETOF spellsource.cards
    LANGUAGE plpgsql
    AS $$
begin
    return query
        select *
        from spellsource.cards
        where card_script ->> 'type' = 'CLASS'
          and (card_script ->> 'set') = any (sets)
          and is_published
          and not is_archived;
end;
$$;


ALTER FUNCTION spellsource.card_catalogue_get_base_classes(sets text[]) OWNER TO admin;

--
-- Name: card_catalogue_get_card_by_id(text); Type: FUNCTION; Schema: spellsource; Owner: admin
--

CREATE FUNCTION spellsource.card_catalogue_get_card_by_id(card_id text) RETURNS SETOF spellsource.cards
    LANGUAGE plpgsql
    AS $$
begin
    return query
        select *
        from spellsource.cards
        where id = card_id
          and is_published
          and not is_archived;
end;
$$;


ALTER FUNCTION spellsource.card_catalogue_get_card_by_id(card_id text) OWNER TO admin;

--
-- Name: card_catalogue_get_card_by_name(text); Type: FUNCTION; Schema: spellsource; Owner: admin
--

CREATE FUNCTION spellsource.card_catalogue_get_card_by_name(card_name text) RETURNS spellsource.cards
    LANGUAGE plpgsql
    AS $$
declare
    result_record spellsource.cards%rowtype;
begin
    select *
    into result_record
    from spellsource.cards
    where card_script ->> 'name' = card_name
      and is_published
      and not is_archived
    limit 1;

    return result_record;
exception
    when NO_DATA_FOUND then
        return null;
end;
$$;


ALTER FUNCTION spellsource.card_catalogue_get_card_by_name(card_name text) OWNER TO admin;

--
-- Name: card_catalogue_get_card_by_name_and_class(text, text); Type: FUNCTION; Schema: spellsource; Owner: admin
--

CREATE FUNCTION spellsource.card_catalogue_get_card_by_name_and_class(card_name text, hero_class text) RETURNS spellsource.cards
    LANGUAGE plpgsql
    AS $$
declare
    result_record spellsource.cards%rowtype;
begin
    select *
    into result_record
    from spellsource.cards
    where card_script ->> 'name' = card_name
      and card_script ->> 'heroClass' = hero_class
      and is_published
      and not is_archived
    limit 1;

    return result_record;
exception
    when NO_DATA_FOUND then
        return null;
end;
$$;


ALTER FUNCTION spellsource.card_catalogue_get_card_by_name_and_class(card_name text, hero_class text) OWNER TO admin;

--
-- Name: card_catalogue_get_class_cards(); Type: FUNCTION; Schema: spellsource; Owner: admin
--

CREATE FUNCTION spellsource.card_catalogue_get_class_cards() RETURNS SETOF spellsource.cards
    LANGUAGE plpgsql
    AS $$
begin
    return query
        select *
        from spellsource.cards
        where card_script ->> 'type' = 'CLASS'
          and is_published
          and not is_archived;
end;
$$;


ALTER FUNCTION spellsource.card_catalogue_get_class_cards() OWNER TO admin;

--
-- Name: card_catalogue_get_format(text); Type: FUNCTION; Schema: spellsource; Owner: admin
--

CREATE FUNCTION spellsource.card_catalogue_get_format(card_name text) RETURNS SETOF spellsource.cards
    LANGUAGE plpgsql
    AS $$
begin
    return query
        select *
        from spellsource.cards
        where card_script ->> 'type' = 'FORMAT'
          and card_script ->> 'name' = card_name
          and is_published
          and not is_archived;
end;
$$;


ALTER FUNCTION spellsource.card_catalogue_get_format(card_name text) OWNER TO admin;

--
-- Name: card_catalogue_get_hard_removal_cards(); Type: FUNCTION; Schema: spellsource; Owner: admin
--

CREATE FUNCTION spellsource.card_catalogue_get_hard_removal_cards() RETURNS TABLE(card_id text)
    LANGUAGE plpgsql
    AS $$
begin
    return query
        (select card_id
         from spellsource.hard_removal_cards
         union
         select id
         from spellsource.cards
         where card_script -> 'artificialIntelligence' ->> 'hardRemoval' = 'true'
           and is_published
           and not is_archived);
end;
$$;


ALTER FUNCTION spellsource.card_catalogue_get_hard_removal_cards() OWNER TO admin;

--
-- Name: card_catalogue_get_hero_card(text); Type: FUNCTION; Schema: spellsource; Owner: admin
--

CREATE FUNCTION spellsource.card_catalogue_get_hero_card(hero_class text) RETURNS spellsource.cards
    LANGUAGE plpgsql
    AS $$
declare
    result_record spellsource.cards%rowtype;
begin
    select *
    into result_record
    from spellsource.cards
    where card_script ->> 'heroClass' = hero_class
      and card_script ->> 'type' = 'HERO'
      and is_published
      and not is_archived
    limit 1;

    return result_record;
exception
    when NO_DATA_FOUND then
        return null;
end;
$$;


ALTER FUNCTION spellsource.card_catalogue_get_hero_card(hero_class text) OWNER TO admin;

--
-- Name: card_catalogue_query(text[], text, text, text, text); Type: FUNCTION; Schema: spellsource; Owner: admin
--

CREATE FUNCTION spellsource.card_catalogue_query(sets text[], card_type text, rarity text, hero_class text, attribute text) RETURNS SETOF spellsource.cards
    LANGUAGE plpgsql
    AS $$
begin
    return query
        select *
        from spellsource.cards
        where (sets is null or card_script ->> 'set' = any (sets))
          and (card_type is null or card_script ->> 'type' = card_type)
          and (rarity is null or card_script ->> 'rarity' = rarity)
          and (hero_class is null or card_script ->> 'heroClass' = hero_class)
          and (attribute is null or card_script -> 'attributes' ? attribute)
          and is_published
          and not is_archived;
end;
$$;


ALTER FUNCTION spellsource.card_catalogue_query(sets text[], card_type text, rarity text, hero_class text, attribute text) OWNER TO admin;

--
-- Name: card_change_notify_event(); Type: FUNCTION; Schema: spellsource; Owner: admin
--

CREATE FUNCTION spellsource.card_change_notify_event() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
declare
    payload json;
begin
    payload := json_build_object(
            '__table', tg_table_name,
            'id', new.id,
            'createdBy', new.created_by
        );
    perform pg_notify('spellsource_cards_changes_v0', payload::text);
    return new;
end;
$$;


ALTER FUNCTION spellsource.card_change_notify_event() OWNER TO admin;

--
-- Name: FUNCTION card_change_notify_event(); Type: COMMENT; Schema: spellsource; Owner: admin
--

COMMENT ON FUNCTION spellsource.card_change_notify_event() IS 'Whenever one of the cards change, this will be fired for
    the purposes of invalidating caches. This is a JSON object with fields id for the card ID and createdBy for the user
    that created the card.';


--
-- Name: get_classes(); Type: FUNCTION; Schema: spellsource; Owner: admin
--

CREATE FUNCTION spellsource.get_classes() RETURNS TABLE(created_by character varying, class text, is_published boolean, collectible boolean, card_script jsonb, id text, name text)
    LANGUAGE sql
    AS $$
select distinct created_by,
                card_script ->> 'heroClass'                           as class,
                is_published,
                coalesce(card_script ->> 'collectible', 'true')::bool as collectible,
                card_script,
                id,
                card_script ->> 'name'                                as name
from spellsource.cards
where card_script ->> 'type' = 'CLASS'
  and is_published
  and not is_archived;
$$;


ALTER FUNCTION spellsource.get_classes() OWNER TO admin;

--
-- Name: classes; Type: VIEW; Schema: spellsource; Owner: admin
--

CREATE VIEW spellsource.classes AS
 SELECT get_classes.created_by,
    get_classes.class,
    get_classes.is_published,
    get_classes.collectible,
    get_classes.card_script,
    get_classes.id,
    get_classes.name
   FROM spellsource.get_classes() get_classes(created_by, class, is_published, collectible, card_script, id, name);


ALTER VIEW spellsource.classes OWNER TO admin;

--
-- Name: card_message(spellsource.cards, spellsource.classes); Type: FUNCTION; Schema: spellsource; Owner: admin
--

CREATE FUNCTION spellsource.card_message(card spellsource.cards, cl spellsource.classes) RETURNS text
    LANGUAGE plpgsql STABLE
    AS $_$
begin
    return coalesce(card.card_script ->> 'baseManaCost', '0') || ' ' || coalesce(card.card_script ->> 'name', '') ||
           ' ' || cl.name || ' ' ||
           replace(replace(coalesce(card.card_script ->> 'description', ''), '$', ''), '#', '') || ' ' ||
           coalesce(card.card_script ->> 'race', '') || ' ' || coalesce(card.card_script ->> 'set', 'CUSTOM');
end;
$_$;


ALTER FUNCTION spellsource.card_message(card spellsource.cards, cl spellsource.classes) OWNER TO admin;

--
-- Name: cards_collectible(spellsource.cards); Type: FUNCTION; Schema: spellsource; Owner: admin
--

CREATE FUNCTION spellsource.cards_collectible(card spellsource.cards) RETURNS boolean
    LANGUAGE plpgsql STABLE
    AS $$
begin
    return coalesce(card.card_script ->> 'collectible', 'true')::bool;
end;
$$;


ALTER FUNCTION spellsource.cards_collectible(card spellsource.cards) OWNER TO admin;

--
-- Name: cards_cost(spellsource.cards); Type: FUNCTION; Schema: spellsource; Owner: admin
--

CREATE FUNCTION spellsource.cards_cost(card spellsource.cards) RETURNS integer
    LANGUAGE plpgsql STABLE
    AS $$
begin
    return coalesce(card.card_script ->> 'baseManaCost', '0')::int;
end;
$$;


ALTER FUNCTION spellsource.cards_cost(card spellsource.cards) OWNER TO admin;

--
-- Name: cards_in_deck; Type: TABLE; Schema: spellsource; Owner: admin
--

CREATE TABLE spellsource.cards_in_deck (
    id bigint NOT NULL,
    deck_id text NOT NULL,
    card_id text NOT NULL
);


ALTER TABLE spellsource.cards_in_deck OWNER TO admin;

--
-- Name: COLUMN cards_in_deck.deck_id; Type: COMMENT; Schema: spellsource; Owner: admin
--

COMMENT ON COLUMN spellsource.cards_in_deck.deck_id IS 'deleting a deck deletes all its card references';


--
-- Name: COLUMN cards_in_deck.card_id; Type: COMMENT; Schema: spellsource; Owner: admin
--

COMMENT ON COLUMN spellsource.cards_in_deck.card_id IS 'cannot delete cards that are currently used in decks';


--
-- Name: cards_in_deck_card_by_card_id(spellsource.cards_in_deck); Type: FUNCTION; Schema: spellsource; Owner: admin
--

CREATE FUNCTION spellsource.cards_in_deck_card_by_card_id(cards_in_deck spellsource.cards_in_deck) RETURNS spellsource.cards
    LANGUAGE plpgsql STABLE
    AS $$
begin
    return spellsource.get_latest_card(cards_in_deck.card_id, true);
end;
$$;


ALTER FUNCTION spellsource.cards_in_deck_card_by_card_id(cards_in_deck spellsource.cards_in_deck) OWNER TO admin;

--
-- Name: cards_type(spellsource.cards); Type: FUNCTION; Schema: spellsource; Owner: admin
--

CREATE FUNCTION spellsource.cards_type(card spellsource.cards) RETURNS text
    LANGUAGE plpgsql STABLE
    AS $$
begin
    return card.card_script ->> 'type';
end;
$$;


ALTER FUNCTION spellsource.cards_type(card spellsource.cards) OWNER TO admin;

--
-- Name: clustered_games_update_game_and_users(text, text, bigint, json); Type: FUNCTION; Schema: spellsource; Owner: admin
--

CREATE FUNCTION spellsource.clustered_games_update_game_and_users(p_user_id_winner text, p_user_id_loser text, p_game_id bigint, p_trace json) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION spellsource.clustered_games_update_game_and_users(p_user_id_winner text, p_user_id_loser text, p_game_id bigint, p_trace json) OWNER TO admin;

--
-- Name: create_deck_with_cards(text, text, text, text[]); Type: FUNCTION; Schema: spellsource; Owner: admin
--

CREATE FUNCTION spellsource.create_deck_with_cards(deck_name text, class_hero text, format_name text, card_ids text[]) RETURNS spellsource.decks
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION spellsource.create_deck_with_cards(deck_name text, class_hero text, format_name text, card_ids text[]) OWNER TO admin;

--
-- Name: get_collection_cards(); Type: FUNCTION; Schema: spellsource; Owner: admin
--

CREATE FUNCTION spellsource.get_collection_cards() RETURNS TABLE(id text, created_by character varying, card_script jsonb, blockly_workspace jsonb, name text, type text, class text, cost integer, collectible boolean, search_message text, last_modified timestamp with time zone, created_at timestamp with time zone)
    LANGUAGE sql
    AS $$
select card.id,
       card.created_by,
       card.card_script,
       card.blockly_workspace,
       card.card_script ->> 'name'                                         as name,
       card.card_script ->> 'type'                                         as type,
       card.card_script ->> 'heroClass'                                    as class,
       coalesce(nullif(card.card_script ->> 'baseManaCost', ''), '0')::int as cost,
       (coalesce(card.card_script ->> 'collectible', 'true')::bool and
        (card.card_script ->> 'heroClass' = 'ANY' or cl.collectible))      as collectible,
       spellsource.card_message(card::spellsource.cards, cl)               as search_message,
       last_modified,
       created_at
from spellsource.cards card
         join spellsource.classes cl on card.card_script ->> 'heroClass' = cl.class
where card.card_script ->> 'set' != 'TEST'
  and card.is_published
  and not card.is_archived;
$$;


ALTER FUNCTION spellsource.get_collection_cards() OWNER TO admin;

--
-- Name: get_latest_card(text, boolean); Type: FUNCTION; Schema: spellsource; Owner: admin
--

CREATE FUNCTION spellsource.get_latest_card(card_id text, published boolean) RETURNS spellsource.cards
    LANGUAGE plpgsql STABLE
    AS $_$
declare
    result spellsource.cards;
begin
    if published
    then
        select c.*
        from spellsource.cards c
                 inner join spellsource.published_cards pc on c.succession = pc.succession
        where c.id = card_id
        limit 1
        into result;
    else
        select *
        from spellsource.cards
        where id = $1
          and not is_published
          and not is_archived
        order by succession desc
        limit 1
        into result;
    end if;

    return result;
end;
$_$;


ALTER FUNCTION spellsource.get_latest_card(card_id text, published boolean) OWNER TO admin;

--
-- Name: get_user_attribute(text, text, text); Type: FUNCTION; Schema: spellsource; Owner: admin
--

CREATE FUNCTION spellsource.get_user_attribute(id_user text, attribute text, or_default text DEFAULT 'null'::text) RETURNS text
    LANGUAGE plpgsql
    AS $$
declare
    result text;
begin
    result := or_default;
    select value from keycloak.user_attribute where user_id = id_user and name = attribute limit 1 into result;
    return result;
end;
$$;


ALTER FUNCTION spellsource.get_user_attribute(id_user text, attribute text, or_default text) OWNER TO admin;

--
-- Name: get_user_id(); Type: FUNCTION; Schema: spellsource; Owner: admin
--

CREATE FUNCTION spellsource.get_user_id() RETURNS text
    LANGUAGE sql STABLE
    AS $$
select current_setting('user.id', true)::text;
$$;


ALTER FUNCTION spellsource.get_user_id() OWNER TO admin;

--
-- Name: on_card_published(); Type: FUNCTION; Schema: spellsource; Owner: admin
--

CREATE FUNCTION spellsource.on_card_published() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
declare
begin
    update spellsource.cards set is_archived = true where id = new.id and is_published and not is_archived;

    insert into spellsource.published_cards
    values (new.id, new.succession)
    on conflict (id) do update set succession = new.succession;

    return new;
end;
$$;


ALTER FUNCTION spellsource.on_card_published() OWNER TO admin;

--
-- Name: publish_card(text); Type: FUNCTION; Schema: spellsource; Owner: admin
--

CREATE FUNCTION spellsource.publish_card(card_id text) RETURNS bigint
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION spellsource.publish_card(card_id text) OWNER TO admin;

--
-- Name: publish_git_card(text, jsonb, character varying); Type: FUNCTION; Schema: spellsource; Owner: admin
--

CREATE FUNCTION spellsource.publish_git_card(card_id text, json jsonb, creator character varying) RETURNS spellsource.cards
    LANGUAGE plpgsql
    AS $$
declare
    card          spellsource.cards;
    created_time  timestamptz;
    const_git_uri text := 'git@github.com:hiddenswitch/Spellsource.git';
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
    insert into spellsource.cards (id, created_by, card_script, created_at, is_published, uri)
    values (card_id, creator, json, created_time, true, const_git_uri)
    returning * into card;

    return card;
end;
$$;


ALTER FUNCTION spellsource.publish_git_card(card_id text, json jsonb, creator character varying) OWNER TO admin;

--
-- Name: save_card(text, jsonb, jsonb); Type: FUNCTION; Schema: spellsource; Owner: admin
--

CREATE FUNCTION spellsource.save_card(card_id text, workspace jsonb, json jsonb) RETURNS spellsource.cards
    LANGUAGE plpgsql
    AS $$
declare
    card              spellsource.cards;
    -- todo: we probably want the website itself to set this value
    const_website_uri text := 'https://playspellsource.com/card-editor';
begin
    card := spellsource.get_latest_card(card_id, false);

    if (card is null) then
        insert into spellsource.cards (id, created_by, blockly_workspace, card_script, uri)
        values (card_id, spellsource.get_user_id(), workspace, json, const_website_uri)
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
$$;


ALTER FUNCTION spellsource.save_card(card_id text, workspace jsonb, json jsonb) OWNER TO admin;

--
-- Name: generated_art; Type: TABLE; Schema: spellsource; Owner: admin
--

CREATE TABLE spellsource.generated_art (
    hash text NOT NULL,
    owner character varying(36) DEFAULT spellsource.get_user_id() NOT NULL,
    urls text[] NOT NULL,
    info jsonb,
    is_archived boolean DEFAULT false NOT NULL
);


ALTER TABLE spellsource.generated_art OWNER TO admin;

--
-- Name: save_generated_art(text, text[], jsonb); Type: FUNCTION; Schema: spellsource; Owner: admin
--

CREATE FUNCTION spellsource.save_generated_art(digest text, links text[], extra_info jsonb) RETURNS spellsource.generated_art
    LANGUAGE plpgsql
    AS $$
declare
    art spellsource.generated_art;
begin
    insert into spellsource.generated_art(hash, urls, info)
    values (digest, links, extra_info)
    on conflict (hash,owner) do update set urls = links, info = extra_info
    returning * into art;

    return art;
end
$$;


ALTER FUNCTION spellsource.save_generated_art(digest text, links text[], extra_info jsonb) OWNER TO admin;

--
-- Name: set_cards_in_deck(text, text[]); Type: FUNCTION; Schema: spellsource; Owner: admin
--

CREATE FUNCTION spellsource.set_cards_in_deck(deck text, card_ids text[]) RETURNS SETOF spellsource.cards_in_deck
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION spellsource.set_cards_in_deck(deck text, card_ids text[]) OWNER TO admin;

--
-- Name: set_user_attribute(text, text, text); Type: FUNCTION; Schema: spellsource; Owner: admin
--

CREATE FUNCTION spellsource.set_user_attribute(id_user text, attribute text, val text) RETURNS void
    LANGUAGE plpgsql
    AS $$
begin
    delete from keycloak.user_attribute where user_id = id_user and attribute = name;
    insert into keycloak.user_attribute values (attribute, val, id_user, gen_random_uuid()::text);
end;
$$;


ALTER FUNCTION spellsource.set_user_attribute(id_user text, attribute text, val text) OWNER TO admin;

--
-- Name: flyway_schema_history; Type: TABLE; Schema: hiddenswitch; Owner: admin
--

CREATE TABLE hiddenswitch.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE hiddenswitch.flyway_schema_history OWNER TO admin;

--
-- Name: admin_event_entity; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.admin_event_entity (
    id character varying(36) NOT NULL,
    admin_event_time bigint,
    realm_id character varying(255),
    operation_type character varying(255),
    auth_realm_id character varying(255),
    auth_client_id character varying(255),
    auth_user_id character varying(255),
    ip_address character varying(255),
    resource_path character varying(2550),
    representation text,
    error character varying(255),
    resource_type character varying(64)
);


ALTER TABLE keycloak.admin_event_entity OWNER TO admin;

--
-- Name: associated_policy; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.associated_policy (
    policy_id character varying(36) NOT NULL,
    associated_policy_id character varying(36) NOT NULL
);


ALTER TABLE keycloak.associated_policy OWNER TO admin;

--
-- Name: authentication_execution; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.authentication_execution (
    id character varying(36) NOT NULL,
    alias character varying(255),
    authenticator character varying(36),
    realm_id character varying(36),
    flow_id character varying(36),
    requirement integer,
    priority integer,
    authenticator_flow boolean DEFAULT false NOT NULL,
    auth_flow_id character varying(36),
    auth_config character varying(36)
);


ALTER TABLE keycloak.authentication_execution OWNER TO admin;

--
-- Name: authentication_flow; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.authentication_flow (
    id character varying(36) NOT NULL,
    alias character varying(255),
    description character varying(255),
    realm_id character varying(36),
    provider_id character varying(36) DEFAULT 'basic-flow'::character varying NOT NULL,
    top_level boolean DEFAULT false NOT NULL,
    built_in boolean DEFAULT false NOT NULL
);


ALTER TABLE keycloak.authentication_flow OWNER TO admin;

--
-- Name: authenticator_config; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.authenticator_config (
    id character varying(36) NOT NULL,
    alias character varying(255),
    realm_id character varying(36)
);


ALTER TABLE keycloak.authenticator_config OWNER TO admin;

--
-- Name: authenticator_config_entry; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.authenticator_config_entry (
    authenticator_id character varying(36) NOT NULL,
    value text,
    name character varying(255) NOT NULL
);


ALTER TABLE keycloak.authenticator_config_entry OWNER TO admin;

--
-- Name: broker_link; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.broker_link (
    identity_provider character varying(255) NOT NULL,
    storage_provider_id character varying(255),
    realm_id character varying(36) NOT NULL,
    broker_user_id character varying(255),
    broker_username character varying(255),
    token text,
    user_id character varying(255) NOT NULL
);


ALTER TABLE keycloak.broker_link OWNER TO admin;

--
-- Name: client; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.client (
    id character varying(36) NOT NULL,
    enabled boolean DEFAULT false NOT NULL,
    full_scope_allowed boolean DEFAULT false NOT NULL,
    client_id character varying(255),
    not_before integer,
    public_client boolean DEFAULT false NOT NULL,
    secret character varying(255),
    base_url character varying(255),
    bearer_only boolean DEFAULT false NOT NULL,
    management_url character varying(255),
    surrogate_auth_required boolean DEFAULT false NOT NULL,
    realm_id character varying(36),
    protocol character varying(255),
    node_rereg_timeout integer DEFAULT 0,
    frontchannel_logout boolean DEFAULT false NOT NULL,
    consent_required boolean DEFAULT false NOT NULL,
    name character varying(255),
    service_accounts_enabled boolean DEFAULT false NOT NULL,
    client_authenticator_type character varying(255),
    root_url character varying(255),
    description character varying(255),
    registration_token character varying(255),
    standard_flow_enabled boolean DEFAULT true NOT NULL,
    implicit_flow_enabled boolean DEFAULT false NOT NULL,
    direct_access_grants_enabled boolean DEFAULT false NOT NULL,
    always_display_in_console boolean DEFAULT false NOT NULL
);


ALTER TABLE keycloak.client OWNER TO admin;

--
-- Name: client_attributes; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.client_attributes (
    client_id character varying(36) NOT NULL,
    name character varying(255) NOT NULL,
    value text
);


ALTER TABLE keycloak.client_attributes OWNER TO admin;

--
-- Name: client_auth_flow_bindings; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.client_auth_flow_bindings (
    client_id character varying(36) NOT NULL,
    flow_id character varying(36),
    binding_name character varying(255) NOT NULL
);


ALTER TABLE keycloak.client_auth_flow_bindings OWNER TO admin;

--
-- Name: client_initial_access; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.client_initial_access (
    id character varying(36) NOT NULL,
    realm_id character varying(36) NOT NULL,
    "timestamp" integer,
    expiration integer,
    count integer,
    remaining_count integer
);


ALTER TABLE keycloak.client_initial_access OWNER TO admin;

--
-- Name: client_node_registrations; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.client_node_registrations (
    client_id character varying(36) NOT NULL,
    value integer,
    name character varying(255) NOT NULL
);


ALTER TABLE keycloak.client_node_registrations OWNER TO admin;

--
-- Name: client_scope; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.client_scope (
    id character varying(36) NOT NULL,
    name character varying(255),
    realm_id character varying(36),
    description character varying(255),
    protocol character varying(255)
);


ALTER TABLE keycloak.client_scope OWNER TO admin;

--
-- Name: client_scope_attributes; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.client_scope_attributes (
    scope_id character varying(36) NOT NULL,
    value character varying(2048),
    name character varying(255) NOT NULL
);


ALTER TABLE keycloak.client_scope_attributes OWNER TO admin;

--
-- Name: client_scope_client; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.client_scope_client (
    client_id character varying(255) NOT NULL,
    scope_id character varying(255) NOT NULL,
    default_scope boolean DEFAULT false NOT NULL
);


ALTER TABLE keycloak.client_scope_client OWNER TO admin;

--
-- Name: client_scope_role_mapping; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.client_scope_role_mapping (
    scope_id character varying(36) NOT NULL,
    role_id character varying(36) NOT NULL
);


ALTER TABLE keycloak.client_scope_role_mapping OWNER TO admin;

--
-- Name: client_session; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.client_session (
    id character varying(36) NOT NULL,
    client_id character varying(36),
    redirect_uri character varying(255),
    state character varying(255),
    "timestamp" integer,
    session_id character varying(36),
    auth_method character varying(255),
    realm_id character varying(255),
    auth_user_id character varying(36),
    current_action character varying(36)
);


ALTER TABLE keycloak.client_session OWNER TO admin;

--
-- Name: client_session_auth_status; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.client_session_auth_status (
    authenticator character varying(36) NOT NULL,
    status integer,
    client_session character varying(36) NOT NULL
);


ALTER TABLE keycloak.client_session_auth_status OWNER TO admin;

--
-- Name: client_session_note; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.client_session_note (
    name character varying(255) NOT NULL,
    value character varying(255),
    client_session character varying(36) NOT NULL
);


ALTER TABLE keycloak.client_session_note OWNER TO admin;

--
-- Name: client_session_prot_mapper; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.client_session_prot_mapper (
    protocol_mapper_id character varying(36) NOT NULL,
    client_session character varying(36) NOT NULL
);


ALTER TABLE keycloak.client_session_prot_mapper OWNER TO admin;

--
-- Name: client_session_role; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.client_session_role (
    role_id character varying(255) NOT NULL,
    client_session character varying(36) NOT NULL
);


ALTER TABLE keycloak.client_session_role OWNER TO admin;

--
-- Name: client_user_session_note; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.client_user_session_note (
    name character varying(255) NOT NULL,
    value character varying(2048),
    client_session character varying(36) NOT NULL
);


ALTER TABLE keycloak.client_user_session_note OWNER TO admin;

--
-- Name: component; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.component (
    id character varying(36) NOT NULL,
    name character varying(255),
    parent_id character varying(36),
    provider_id character varying(36),
    provider_type character varying(255),
    realm_id character varying(36),
    sub_type character varying(255)
);


ALTER TABLE keycloak.component OWNER TO admin;

--
-- Name: component_config; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.component_config (
    id character varying(36) NOT NULL,
    component_id character varying(36) NOT NULL,
    name character varying(255) NOT NULL,
    value character varying(4000)
);


ALTER TABLE keycloak.component_config OWNER TO admin;

--
-- Name: composite_role; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.composite_role (
    composite character varying(36) NOT NULL,
    child_role character varying(36) NOT NULL
);


ALTER TABLE keycloak.composite_role OWNER TO admin;

--
-- Name: credential; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.credential (
    id character varying(36) NOT NULL,
    salt bytea,
    type character varying(255),
    user_id character varying(36),
    created_date bigint,
    user_label character varying(255),
    secret_data text,
    credential_data text,
    priority integer
);


ALTER TABLE keycloak.credential OWNER TO admin;

--
-- Name: databasechangelog; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.databasechangelog (
    id character varying(255) NOT NULL,
    author character varying(255) NOT NULL,
    filename character varying(255) NOT NULL,
    dateexecuted timestamp without time zone NOT NULL,
    orderexecuted integer NOT NULL,
    exectype character varying(10) NOT NULL,
    md5sum character varying(35),
    description character varying(255),
    comments character varying(255),
    tag character varying(255),
    liquibase character varying(20),
    contexts character varying(255),
    labels character varying(255),
    deployment_id character varying(10)
);


ALTER TABLE keycloak.databasechangelog OWNER TO admin;

--
-- Name: databasechangeloglock; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.databasechangeloglock (
    id integer NOT NULL,
    locked boolean NOT NULL,
    lockgranted timestamp without time zone,
    lockedby character varying(255)
);


ALTER TABLE keycloak.databasechangeloglock OWNER TO admin;

--
-- Name: default_client_scope; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.default_client_scope (
    realm_id character varying(36) NOT NULL,
    scope_id character varying(36) NOT NULL,
    default_scope boolean DEFAULT false NOT NULL
);


ALTER TABLE keycloak.default_client_scope OWNER TO admin;

--
-- Name: event_entity; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.event_entity (
    id character varying(36) NOT NULL,
    client_id character varying(255),
    details_json character varying(2550),
    error character varying(255),
    ip_address character varying(255),
    realm_id character varying(255),
    session_id character varying(255),
    event_time bigint,
    type character varying(255),
    user_id character varying(255)
);


ALTER TABLE keycloak.event_entity OWNER TO admin;

--
-- Name: fed_user_attribute; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.fed_user_attribute (
    id character varying(36) NOT NULL,
    name character varying(255) NOT NULL,
    user_id character varying(255) NOT NULL,
    realm_id character varying(36) NOT NULL,
    storage_provider_id character varying(36),
    value character varying(2024)
);


ALTER TABLE keycloak.fed_user_attribute OWNER TO admin;

--
-- Name: fed_user_consent; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.fed_user_consent (
    id character varying(36) NOT NULL,
    client_id character varying(255),
    user_id character varying(255) NOT NULL,
    realm_id character varying(36) NOT NULL,
    storage_provider_id character varying(36),
    created_date bigint,
    last_updated_date bigint,
    client_storage_provider character varying(36),
    external_client_id character varying(255)
);


ALTER TABLE keycloak.fed_user_consent OWNER TO admin;

--
-- Name: fed_user_consent_cl_scope; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.fed_user_consent_cl_scope (
    user_consent_id character varying(36) NOT NULL,
    scope_id character varying(36) NOT NULL
);


ALTER TABLE keycloak.fed_user_consent_cl_scope OWNER TO admin;

--
-- Name: fed_user_credential; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.fed_user_credential (
    id character varying(36) NOT NULL,
    salt bytea,
    type character varying(255),
    created_date bigint,
    user_id character varying(255) NOT NULL,
    realm_id character varying(36) NOT NULL,
    storage_provider_id character varying(36),
    user_label character varying(255),
    secret_data text,
    credential_data text,
    priority integer
);


ALTER TABLE keycloak.fed_user_credential OWNER TO admin;

--
-- Name: fed_user_group_membership; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.fed_user_group_membership (
    group_id character varying(36) NOT NULL,
    user_id character varying(255) NOT NULL,
    realm_id character varying(36) NOT NULL,
    storage_provider_id character varying(36)
);


ALTER TABLE keycloak.fed_user_group_membership OWNER TO admin;

--
-- Name: fed_user_required_action; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.fed_user_required_action (
    required_action character varying(255) DEFAULT ' '::character varying NOT NULL,
    user_id character varying(255) NOT NULL,
    realm_id character varying(36) NOT NULL,
    storage_provider_id character varying(36)
);


ALTER TABLE keycloak.fed_user_required_action OWNER TO admin;

--
-- Name: fed_user_role_mapping; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.fed_user_role_mapping (
    role_id character varying(36) NOT NULL,
    user_id character varying(255) NOT NULL,
    realm_id character varying(36) NOT NULL,
    storage_provider_id character varying(36)
);


ALTER TABLE keycloak.fed_user_role_mapping OWNER TO admin;

--
-- Name: federated_identity; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.federated_identity (
    identity_provider character varying(255) NOT NULL,
    realm_id character varying(36),
    federated_user_id character varying(255),
    federated_username character varying(255),
    token text,
    user_id character varying(36) NOT NULL
);


ALTER TABLE keycloak.federated_identity OWNER TO admin;

--
-- Name: federated_user; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.federated_user (
    id character varying(255) NOT NULL,
    storage_provider_id character varying(255),
    realm_id character varying(36) NOT NULL
);


ALTER TABLE keycloak.federated_user OWNER TO admin;

--
-- Name: group_attribute; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.group_attribute (
    id character varying(36) DEFAULT 'sybase-needs-something-here'::character varying NOT NULL,
    name character varying(255) NOT NULL,
    value character varying(255),
    group_id character varying(36) NOT NULL
);


ALTER TABLE keycloak.group_attribute OWNER TO admin;

--
-- Name: group_role_mapping; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.group_role_mapping (
    role_id character varying(36) NOT NULL,
    group_id character varying(36) NOT NULL
);


ALTER TABLE keycloak.group_role_mapping OWNER TO admin;

--
-- Name: identity_provider; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.identity_provider (
    internal_id character varying(36) NOT NULL,
    enabled boolean DEFAULT false NOT NULL,
    provider_alias character varying(255),
    provider_id character varying(255),
    store_token boolean DEFAULT false NOT NULL,
    authenticate_by_default boolean DEFAULT false NOT NULL,
    realm_id character varying(36),
    add_token_role boolean DEFAULT true NOT NULL,
    trust_email boolean DEFAULT false NOT NULL,
    first_broker_login_flow_id character varying(36),
    post_broker_login_flow_id character varying(36),
    provider_display_name character varying(255),
    link_only boolean DEFAULT false NOT NULL
);


ALTER TABLE keycloak.identity_provider OWNER TO admin;

--
-- Name: identity_provider_config; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.identity_provider_config (
    identity_provider_id character varying(36) NOT NULL,
    value text,
    name character varying(255) NOT NULL
);


ALTER TABLE keycloak.identity_provider_config OWNER TO admin;

--
-- Name: identity_provider_mapper; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.identity_provider_mapper (
    id character varying(36) NOT NULL,
    name character varying(255) NOT NULL,
    idp_alias character varying(255) NOT NULL,
    idp_mapper_name character varying(255) NOT NULL,
    realm_id character varying(36) NOT NULL
);


ALTER TABLE keycloak.identity_provider_mapper OWNER TO admin;

--
-- Name: idp_mapper_config; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.idp_mapper_config (
    idp_mapper_id character varying(36) NOT NULL,
    value text,
    name character varying(255) NOT NULL
);


ALTER TABLE keycloak.idp_mapper_config OWNER TO admin;

--
-- Name: keycloak_group; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.keycloak_group (
    id character varying(36) NOT NULL,
    name character varying(255),
    parent_group character varying(36) NOT NULL,
    realm_id character varying(36)
);


ALTER TABLE keycloak.keycloak_group OWNER TO admin;

--
-- Name: keycloak_role; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.keycloak_role (
    id character varying(36) NOT NULL,
    client_realm_constraint character varying(255),
    client_role boolean DEFAULT false NOT NULL,
    description character varying(255),
    name character varying(255),
    realm_id character varying(255),
    client character varying(36),
    realm character varying(36)
);


ALTER TABLE keycloak.keycloak_role OWNER TO admin;

--
-- Name: migration_model; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.migration_model (
    id character varying(36) NOT NULL,
    version character varying(36),
    update_time bigint DEFAULT 0 NOT NULL
);


ALTER TABLE keycloak.migration_model OWNER TO admin;

--
-- Name: offline_client_session; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.offline_client_session (
    user_session_id character varying(36) NOT NULL,
    client_id character varying(255) NOT NULL,
    offline_flag character varying(4) NOT NULL,
    "timestamp" integer,
    data text,
    client_storage_provider character varying(36) DEFAULT 'local'::character varying NOT NULL,
    external_client_id character varying(255) DEFAULT 'local'::character varying NOT NULL
);


ALTER TABLE keycloak.offline_client_session OWNER TO admin;

--
-- Name: offline_user_session; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.offline_user_session (
    user_session_id character varying(36) NOT NULL,
    user_id character varying(255) NOT NULL,
    realm_id character varying(36) NOT NULL,
    created_on integer NOT NULL,
    offline_flag character varying(4) NOT NULL,
    data text,
    last_session_refresh integer DEFAULT 0 NOT NULL
);


ALTER TABLE keycloak.offline_user_session OWNER TO admin;

--
-- Name: policy_config; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.policy_config (
    policy_id character varying(36) NOT NULL,
    name character varying(255) NOT NULL,
    value text
);


ALTER TABLE keycloak.policy_config OWNER TO admin;

--
-- Name: protocol_mapper; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.protocol_mapper (
    id character varying(36) NOT NULL,
    name character varying(255) NOT NULL,
    protocol character varying(255) NOT NULL,
    protocol_mapper_name character varying(255) NOT NULL,
    client_id character varying(36),
    client_scope_id character varying(36)
);


ALTER TABLE keycloak.protocol_mapper OWNER TO admin;

--
-- Name: protocol_mapper_config; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.protocol_mapper_config (
    protocol_mapper_id character varying(36) NOT NULL,
    value text,
    name character varying(255) NOT NULL
);


ALTER TABLE keycloak.protocol_mapper_config OWNER TO admin;

--
-- Name: realm; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.realm (
    id character varying(36) NOT NULL,
    access_code_lifespan integer,
    user_action_lifespan integer,
    access_token_lifespan integer,
    account_theme character varying(255),
    admin_theme character varying(255),
    email_theme character varying(255),
    enabled boolean DEFAULT false NOT NULL,
    events_enabled boolean DEFAULT false NOT NULL,
    events_expiration bigint,
    login_theme character varying(255),
    name character varying(255),
    not_before integer,
    password_policy character varying(2550),
    registration_allowed boolean DEFAULT false NOT NULL,
    remember_me boolean DEFAULT false NOT NULL,
    reset_password_allowed boolean DEFAULT false NOT NULL,
    social boolean DEFAULT false NOT NULL,
    ssl_required character varying(255),
    sso_idle_timeout integer,
    sso_max_lifespan integer,
    update_profile_on_soc_login boolean DEFAULT false NOT NULL,
    verify_email boolean DEFAULT false NOT NULL,
    master_admin_client character varying(36),
    login_lifespan integer,
    internationalization_enabled boolean DEFAULT false NOT NULL,
    default_locale character varying(255),
    reg_email_as_username boolean DEFAULT false NOT NULL,
    admin_events_enabled boolean DEFAULT false NOT NULL,
    admin_events_details_enabled boolean DEFAULT false NOT NULL,
    edit_username_allowed boolean DEFAULT false NOT NULL,
    otp_policy_counter integer DEFAULT 0,
    otp_policy_window integer DEFAULT 1,
    otp_policy_period integer DEFAULT 30,
    otp_policy_digits integer DEFAULT 6,
    otp_policy_alg character varying(36) DEFAULT 'HmacSHA1'::character varying,
    otp_policy_type character varying(36) DEFAULT 'totp'::character varying,
    browser_flow character varying(36),
    registration_flow character varying(36),
    direct_grant_flow character varying(36),
    reset_credentials_flow character varying(36),
    client_auth_flow character varying(36),
    offline_session_idle_timeout integer DEFAULT 0,
    revoke_refresh_token boolean DEFAULT false NOT NULL,
    access_token_life_implicit integer DEFAULT 0,
    login_with_email_allowed boolean DEFAULT true NOT NULL,
    duplicate_emails_allowed boolean DEFAULT false NOT NULL,
    docker_auth_flow character varying(36),
    refresh_token_max_reuse integer DEFAULT 0,
    allow_user_managed_access boolean DEFAULT false NOT NULL,
    sso_max_lifespan_remember_me integer DEFAULT 0 NOT NULL,
    sso_idle_timeout_remember_me integer DEFAULT 0 NOT NULL,
    default_role character varying(255)
);


ALTER TABLE keycloak.realm OWNER TO admin;

--
-- Name: realm_attribute; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.realm_attribute (
    name character varying(255) NOT NULL,
    realm_id character varying(36) NOT NULL,
    value text
);


ALTER TABLE keycloak.realm_attribute OWNER TO admin;

--
-- Name: realm_default_groups; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.realm_default_groups (
    realm_id character varying(36) NOT NULL,
    group_id character varying(36) NOT NULL
);


ALTER TABLE keycloak.realm_default_groups OWNER TO admin;

--
-- Name: realm_enabled_event_types; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.realm_enabled_event_types (
    realm_id character varying(36) NOT NULL,
    value character varying(255) NOT NULL
);


ALTER TABLE keycloak.realm_enabled_event_types OWNER TO admin;

--
-- Name: realm_events_listeners; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.realm_events_listeners (
    realm_id character varying(36) NOT NULL,
    value character varying(255) NOT NULL
);


ALTER TABLE keycloak.realm_events_listeners OWNER TO admin;

--
-- Name: realm_localizations; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.realm_localizations (
    realm_id character varying(255) NOT NULL,
    locale character varying(255) NOT NULL,
    texts text NOT NULL
);


ALTER TABLE keycloak.realm_localizations OWNER TO admin;

--
-- Name: realm_required_credential; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.realm_required_credential (
    type character varying(255) NOT NULL,
    form_label character varying(255),
    input boolean DEFAULT false NOT NULL,
    secret boolean DEFAULT false NOT NULL,
    realm_id character varying(36) NOT NULL
);


ALTER TABLE keycloak.realm_required_credential OWNER TO admin;

--
-- Name: realm_smtp_config; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.realm_smtp_config (
    realm_id character varying(36) NOT NULL,
    value character varying(255),
    name character varying(255) NOT NULL
);


ALTER TABLE keycloak.realm_smtp_config OWNER TO admin;

--
-- Name: realm_supported_locales; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.realm_supported_locales (
    realm_id character varying(36) NOT NULL,
    value character varying(255) NOT NULL
);


ALTER TABLE keycloak.realm_supported_locales OWNER TO admin;

--
-- Name: redirect_uris; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.redirect_uris (
    client_id character varying(36) NOT NULL,
    value character varying(255) NOT NULL
);


ALTER TABLE keycloak.redirect_uris OWNER TO admin;

--
-- Name: required_action_config; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.required_action_config (
    required_action_id character varying(36) NOT NULL,
    value text,
    name character varying(255) NOT NULL
);


ALTER TABLE keycloak.required_action_config OWNER TO admin;

--
-- Name: required_action_provider; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.required_action_provider (
    id character varying(36) NOT NULL,
    alias character varying(255),
    name character varying(255),
    realm_id character varying(36),
    enabled boolean DEFAULT false NOT NULL,
    default_action boolean DEFAULT false NOT NULL,
    provider_id character varying(255),
    priority integer
);


ALTER TABLE keycloak.required_action_provider OWNER TO admin;

--
-- Name: resource_attribute; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.resource_attribute (
    id character varying(36) DEFAULT 'sybase-needs-something-here'::character varying NOT NULL,
    name character varying(255) NOT NULL,
    value character varying(255),
    resource_id character varying(36) NOT NULL
);


ALTER TABLE keycloak.resource_attribute OWNER TO admin;

--
-- Name: resource_policy; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.resource_policy (
    resource_id character varying(36) NOT NULL,
    policy_id character varying(36) NOT NULL
);


ALTER TABLE keycloak.resource_policy OWNER TO admin;

--
-- Name: resource_scope; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.resource_scope (
    resource_id character varying(36) NOT NULL,
    scope_id character varying(36) NOT NULL
);


ALTER TABLE keycloak.resource_scope OWNER TO admin;

--
-- Name: resource_server; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.resource_server (
    id character varying(36) NOT NULL,
    allow_rs_remote_mgmt boolean DEFAULT false NOT NULL,
    policy_enforce_mode smallint NOT NULL,
    decision_strategy smallint DEFAULT 1 NOT NULL
);


ALTER TABLE keycloak.resource_server OWNER TO admin;

--
-- Name: resource_server_perm_ticket; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.resource_server_perm_ticket (
    id character varying(36) NOT NULL,
    owner character varying(255) NOT NULL,
    requester character varying(255) NOT NULL,
    created_timestamp bigint NOT NULL,
    granted_timestamp bigint,
    resource_id character varying(36) NOT NULL,
    scope_id character varying(36),
    resource_server_id character varying(36) NOT NULL,
    policy_id character varying(36)
);


ALTER TABLE keycloak.resource_server_perm_ticket OWNER TO admin;

--
-- Name: resource_server_policy; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.resource_server_policy (
    id character varying(36) NOT NULL,
    name character varying(255) NOT NULL,
    description character varying(255),
    type character varying(255) NOT NULL,
    decision_strategy smallint,
    logic smallint,
    resource_server_id character varying(36) NOT NULL,
    owner character varying(255)
);


ALTER TABLE keycloak.resource_server_policy OWNER TO admin;

--
-- Name: resource_server_resource; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.resource_server_resource (
    id character varying(36) NOT NULL,
    name character varying(255) NOT NULL,
    type character varying(255),
    icon_uri character varying(255),
    owner character varying(255) NOT NULL,
    resource_server_id character varying(36) NOT NULL,
    owner_managed_access boolean DEFAULT false NOT NULL,
    display_name character varying(255)
);


ALTER TABLE keycloak.resource_server_resource OWNER TO admin;

--
-- Name: resource_server_scope; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.resource_server_scope (
    id character varying(36) NOT NULL,
    name character varying(255) NOT NULL,
    icon_uri character varying(255),
    resource_server_id character varying(36) NOT NULL,
    display_name character varying(255)
);


ALTER TABLE keycloak.resource_server_scope OWNER TO admin;

--
-- Name: resource_uris; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.resource_uris (
    resource_id character varying(36) NOT NULL,
    value character varying(255) NOT NULL
);


ALTER TABLE keycloak.resource_uris OWNER TO admin;

--
-- Name: role_attribute; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.role_attribute (
    id character varying(36) NOT NULL,
    role_id character varying(36) NOT NULL,
    name character varying(255) NOT NULL,
    value character varying(255)
);


ALTER TABLE keycloak.role_attribute OWNER TO admin;

--
-- Name: scope_mapping; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.scope_mapping (
    client_id character varying(36) NOT NULL,
    role_id character varying(36) NOT NULL
);


ALTER TABLE keycloak.scope_mapping OWNER TO admin;

--
-- Name: scope_policy; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.scope_policy (
    scope_id character varying(36) NOT NULL,
    policy_id character varying(36) NOT NULL
);


ALTER TABLE keycloak.scope_policy OWNER TO admin;

--
-- Name: user_attribute; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.user_attribute (
    name character varying(255) NOT NULL,
    value character varying(255),
    user_id character varying(36) NOT NULL,
    id character varying(36) DEFAULT 'sybase-needs-something-here'::character varying NOT NULL
);


ALTER TABLE keycloak.user_attribute OWNER TO admin;

--
-- Name: user_consent; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.user_consent (
    id character varying(36) NOT NULL,
    client_id character varying(255),
    user_id character varying(36) NOT NULL,
    created_date bigint,
    last_updated_date bigint,
    client_storage_provider character varying(36),
    external_client_id character varying(255)
);


ALTER TABLE keycloak.user_consent OWNER TO admin;

--
-- Name: user_consent_client_scope; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.user_consent_client_scope (
    user_consent_id character varying(36) NOT NULL,
    scope_id character varying(36) NOT NULL
);


ALTER TABLE keycloak.user_consent_client_scope OWNER TO admin;

--
-- Name: user_entity; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.user_entity (
    id character varying(36) NOT NULL,
    email character varying(255),
    email_constraint character varying(255),
    email_verified boolean DEFAULT false NOT NULL,
    enabled boolean DEFAULT false NOT NULL,
    federation_link character varying(255),
    first_name character varying(255),
    last_name character varying(255),
    realm_id character varying(255),
    username character varying(255),
    created_timestamp bigint,
    service_account_client_link character varying(255),
    not_before integer DEFAULT 0 NOT NULL
);


ALTER TABLE keycloak.user_entity OWNER TO admin;

--
-- Name: user_federation_config; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.user_federation_config (
    user_federation_provider_id character varying(36) NOT NULL,
    value character varying(255),
    name character varying(255) NOT NULL
);


ALTER TABLE keycloak.user_federation_config OWNER TO admin;

--
-- Name: user_federation_mapper; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.user_federation_mapper (
    id character varying(36) NOT NULL,
    name character varying(255) NOT NULL,
    federation_provider_id character varying(36) NOT NULL,
    federation_mapper_type character varying(255) NOT NULL,
    realm_id character varying(36) NOT NULL
);


ALTER TABLE keycloak.user_federation_mapper OWNER TO admin;

--
-- Name: user_federation_mapper_config; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.user_federation_mapper_config (
    user_federation_mapper_id character varying(36) NOT NULL,
    value character varying(255),
    name character varying(255) NOT NULL
);


ALTER TABLE keycloak.user_federation_mapper_config OWNER TO admin;

--
-- Name: user_federation_provider; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.user_federation_provider (
    id character varying(36) NOT NULL,
    changed_sync_period integer,
    display_name character varying(255),
    full_sync_period integer,
    last_sync integer,
    priority integer,
    provider_name character varying(255),
    realm_id character varying(36)
);


ALTER TABLE keycloak.user_federation_provider OWNER TO admin;

--
-- Name: user_group_membership; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.user_group_membership (
    group_id character varying(36) NOT NULL,
    user_id character varying(36) NOT NULL
);


ALTER TABLE keycloak.user_group_membership OWNER TO admin;

--
-- Name: user_required_action; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.user_required_action (
    user_id character varying(36) NOT NULL,
    required_action character varying(255) DEFAULT ' '::character varying NOT NULL
);


ALTER TABLE keycloak.user_required_action OWNER TO admin;

--
-- Name: user_role_mapping; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.user_role_mapping (
    role_id character varying(255) NOT NULL,
    user_id character varying(36) NOT NULL
);


ALTER TABLE keycloak.user_role_mapping OWNER TO admin;

--
-- Name: user_session; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.user_session (
    id character varying(36) NOT NULL,
    auth_method character varying(255),
    ip_address character varying(255),
    last_session_refresh integer,
    login_username character varying(255),
    realm_id character varying(255),
    remember_me boolean DEFAULT false NOT NULL,
    started integer,
    user_id character varying(255),
    user_session_state integer,
    broker_session_id character varying(255),
    broker_user_id character varying(255)
);


ALTER TABLE keycloak.user_session OWNER TO admin;

--
-- Name: user_session_note; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.user_session_note (
    user_session character varying(36) NOT NULL,
    name character varying(255) NOT NULL,
    value character varying(2048)
);


ALTER TABLE keycloak.user_session_note OWNER TO admin;

--
-- Name: username_login_failure; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.username_login_failure (
    realm_id character varying(36) NOT NULL,
    username character varying(255) NOT NULL,
    failed_login_not_before integer,
    last_failure bigint,
    last_ip_failure character varying(255),
    num_failures integer
);


ALTER TABLE keycloak.username_login_failure OWNER TO admin;

--
-- Name: web_origins; Type: TABLE; Schema: keycloak; Owner: admin
--

CREATE TABLE keycloak.web_origins (
    client_id character varying(36) NOT NULL,
    value character varying(255) NOT NULL
);


ALTER TABLE keycloak.web_origins OWNER TO admin;

--
-- Name: banned_draft_cards; Type: TABLE; Schema: spellsource; Owner: admin
--

CREATE TABLE spellsource.banned_draft_cards (
    card_id text NOT NULL
);


ALTER TABLE spellsource.banned_draft_cards OWNER TO admin;

--
-- Name: bot_users; Type: TABLE; Schema: spellsource; Owner: admin
--

CREATE TABLE spellsource.bot_users (
    id text NOT NULL
);


ALTER TABLE spellsource.bot_users OWNER TO admin;

--
-- Name: cards_in_deck_id_seq; Type: SEQUENCE; Schema: spellsource; Owner: admin
--

ALTER TABLE spellsource.cards_in_deck ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME spellsource.cards_in_deck_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: cards_succession_seq; Type: SEQUENCE; Schema: spellsource; Owner: admin
--

ALTER TABLE spellsource.cards ALTER COLUMN succession ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME spellsource.cards_succession_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: collection_cards; Type: VIEW; Schema: spellsource; Owner: admin
--

CREATE VIEW spellsource.collection_cards AS
 SELECT get_collection_cards.id,
    get_collection_cards.created_by,
    get_collection_cards.card_script,
    get_collection_cards.blockly_workspace,
    get_collection_cards.name,
    get_collection_cards.type,
    get_collection_cards.class,
    get_collection_cards.cost,
    get_collection_cards.collectible,
    get_collection_cards.search_message,
    get_collection_cards.last_modified,
    get_collection_cards.created_at
   FROM spellsource.get_collection_cards() get_collection_cards(id, created_by, card_script, blockly_workspace, name, type, class, cost, collectible, search_message, last_modified, created_at);


ALTER VIEW spellsource.collection_cards OWNER TO admin;

--
-- Name: deck_player_attribute_tuples; Type: TABLE; Schema: spellsource; Owner: admin
--

CREATE TABLE spellsource.deck_player_attribute_tuples (
    id bigint NOT NULL,
    deck_id text NOT NULL,
    attribute integer NOT NULL,
    string_value text
);


ALTER TABLE spellsource.deck_player_attribute_tuples OWNER TO admin;

--
-- Name: deck_player_attribute_tuples_id_seq; Type: SEQUENCE; Schema: spellsource; Owner: admin
--

ALTER TABLE spellsource.deck_player_attribute_tuples ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME spellsource.deck_player_attribute_tuples_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: deck_shares; Type: TABLE; Schema: spellsource; Owner: admin
--

CREATE TABLE spellsource.deck_shares (
    deck_id text NOT NULL,
    share_recipient_id text NOT NULL,
    trashed_by_recipient boolean DEFAULT false NOT NULL
);


ALTER TABLE spellsource.deck_shares OWNER TO admin;

--
-- Name: TABLE deck_shares; Type: COMMENT; Schema: spellsource; Owner: admin
--

COMMENT ON TABLE spellsource.deck_shares IS 'indicates a deck shared to a player';


--
-- Name: friends; Type: TABLE; Schema: spellsource; Owner: admin
--

CREATE TABLE spellsource.friends (
    id text NOT NULL,
    friend text NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


ALTER TABLE spellsource.friends OWNER TO admin;

--
-- Name: game_users; Type: TABLE; Schema: spellsource; Owner: admin
--

CREATE TABLE spellsource.game_users (
    player_index smallint DEFAULT 0,
    game_id bigint NOT NULL,
    user_id text NOT NULL,
    deck_id text,
    victory_status spellsource.game_user_victory_enum DEFAULT 'UNKNOWN'::spellsource.game_user_victory_enum NOT NULL
);


ALTER TABLE spellsource.game_users OWNER TO admin;

--
-- Name: games; Type: TABLE; Schema: spellsource; Owner: admin
--

CREATE TABLE spellsource.games (
    id bigint NOT NULL,
    status spellsource.game_state_enum DEFAULT 'AWAITING_CONNECTIONS'::spellsource.game_state_enum NOT NULL,
    git_hash text,
    trace jsonb,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


ALTER TABLE spellsource.games OWNER TO admin;

--
-- Name: games_id_seq; Type: SEQUENCE; Schema: spellsource; Owner: admin
--

ALTER TABLE spellsource.games ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME spellsource.games_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: guests; Type: TABLE; Schema: spellsource; Owner: admin
--

CREATE TABLE spellsource.guests (
    id bigint NOT NULL,
    user_id text
);


ALTER TABLE spellsource.guests OWNER TO admin;

--
-- Name: guests_id_seq; Type: SEQUENCE; Schema: spellsource; Owner: admin
--

ALTER TABLE spellsource.guests ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME spellsource.guests_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: hard_removal_cards; Type: TABLE; Schema: spellsource; Owner: admin
--

CREATE TABLE spellsource.hard_removal_cards (
    card_id text NOT NULL
);


ALTER TABLE spellsource.hard_removal_cards OWNER TO admin;

--
-- Name: matchmaking_queues; Type: TABLE; Schema: spellsource; Owner: admin
--

CREATE TABLE spellsource.matchmaking_queues (
    id text NOT NULL,
    name text NOT NULL,
    bot_opponent boolean DEFAULT false NOT NULL,
    private_lobby boolean DEFAULT false NOT NULL,
    starts_automatically boolean DEFAULT true NOT NULL,
    still_connected_timeout bigint DEFAULT 2000 NOT NULL,
    empty_lobby_timeout bigint DEFAULT 0 NOT NULL,
    awaiting_lobby_timeout bigint DEFAULT 0 NOT NULL,
    once boolean DEFAULT false NOT NULL,
    automatically_close boolean DEFAULT true NOT NULL,
    lobby_size integer DEFAULT 2 NOT NULL,
    queue_created_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT matchmaking_queues_lobby_size_check CHECK (((lobby_size <= 2) AND (lobby_size >= 0)))
);


ALTER TABLE spellsource.matchmaking_queues OWNER TO admin;

--
-- Name: matchmaking_tickets; Type: TABLE; Schema: spellsource; Owner: admin
--

CREATE TABLE spellsource.matchmaking_tickets (
    ticket_id bigint NOT NULL,
    queue_id text,
    user_id text NOT NULL,
    deck_id text,
    bot_deck_id text,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


ALTER TABLE spellsource.matchmaking_tickets OWNER TO admin;

--
-- Name: matchmaking_tickets_ticket_id_seq; Type: SEQUENCE; Schema: spellsource; Owner: admin
--

ALTER TABLE spellsource.matchmaking_tickets ALTER COLUMN ticket_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME spellsource.matchmaking_tickets_ticket_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: published_cards; Type: TABLE; Schema: spellsource; Owner: admin
--

CREATE TABLE spellsource.published_cards (
    id text NOT NULL,
    succession bigint NOT NULL
);


ALTER TABLE spellsource.published_cards OWNER TO admin;

--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: hiddenswitch; Owner: admin
--

ALTER TABLE ONLY hiddenswitch.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: username_login_failure CONSTRAINT_17-2; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.username_login_failure
    ADD CONSTRAINT "CONSTRAINT_17-2" PRIMARY KEY (realm_id, username);


--
-- Name: keycloak_role UK_J3RWUVD56ONTGSUHOGM184WW2-2; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.keycloak_role
    ADD CONSTRAINT "UK_J3RWUVD56ONTGSUHOGM184WW2-2" UNIQUE (name, client_realm_constraint);


--
-- Name: client_auth_flow_bindings c_cli_flow_bind; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.client_auth_flow_bindings
    ADD CONSTRAINT c_cli_flow_bind PRIMARY KEY (client_id, binding_name);


--
-- Name: client_scope_client c_cli_scope_bind; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.client_scope_client
    ADD CONSTRAINT c_cli_scope_bind PRIMARY KEY (client_id, scope_id);


--
-- Name: client_initial_access cnstr_client_init_acc_pk; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.client_initial_access
    ADD CONSTRAINT cnstr_client_init_acc_pk PRIMARY KEY (id);


--
-- Name: realm_default_groups con_group_id_def_groups; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.realm_default_groups
    ADD CONSTRAINT con_group_id_def_groups UNIQUE (group_id);


--
-- Name: broker_link constr_broker_link_pk; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.broker_link
    ADD CONSTRAINT constr_broker_link_pk PRIMARY KEY (identity_provider, user_id);


--
-- Name: client_user_session_note constr_cl_usr_ses_note; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.client_user_session_note
    ADD CONSTRAINT constr_cl_usr_ses_note PRIMARY KEY (client_session, name);


--
-- Name: component_config constr_component_config_pk; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.component_config
    ADD CONSTRAINT constr_component_config_pk PRIMARY KEY (id);


--
-- Name: component constr_component_pk; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.component
    ADD CONSTRAINT constr_component_pk PRIMARY KEY (id);


--
-- Name: fed_user_required_action constr_fed_required_action; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.fed_user_required_action
    ADD CONSTRAINT constr_fed_required_action PRIMARY KEY (required_action, user_id);


--
-- Name: fed_user_attribute constr_fed_user_attr_pk; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.fed_user_attribute
    ADD CONSTRAINT constr_fed_user_attr_pk PRIMARY KEY (id);


--
-- Name: fed_user_consent constr_fed_user_consent_pk; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.fed_user_consent
    ADD CONSTRAINT constr_fed_user_consent_pk PRIMARY KEY (id);


--
-- Name: fed_user_credential constr_fed_user_cred_pk; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.fed_user_credential
    ADD CONSTRAINT constr_fed_user_cred_pk PRIMARY KEY (id);


--
-- Name: fed_user_group_membership constr_fed_user_group; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.fed_user_group_membership
    ADD CONSTRAINT constr_fed_user_group PRIMARY KEY (group_id, user_id);


--
-- Name: fed_user_role_mapping constr_fed_user_role; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.fed_user_role_mapping
    ADD CONSTRAINT constr_fed_user_role PRIMARY KEY (role_id, user_id);


--
-- Name: federated_user constr_federated_user; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.federated_user
    ADD CONSTRAINT constr_federated_user PRIMARY KEY (id);


--
-- Name: realm_default_groups constr_realm_default_groups; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.realm_default_groups
    ADD CONSTRAINT constr_realm_default_groups PRIMARY KEY (realm_id, group_id);


--
-- Name: realm_enabled_event_types constr_realm_enabl_event_types; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.realm_enabled_event_types
    ADD CONSTRAINT constr_realm_enabl_event_types PRIMARY KEY (realm_id, value);


--
-- Name: realm_events_listeners constr_realm_events_listeners; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.realm_events_listeners
    ADD CONSTRAINT constr_realm_events_listeners PRIMARY KEY (realm_id, value);


--
-- Name: realm_supported_locales constr_realm_supported_locales; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.realm_supported_locales
    ADD CONSTRAINT constr_realm_supported_locales PRIMARY KEY (realm_id, value);


--
-- Name: identity_provider constraint_2b; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.identity_provider
    ADD CONSTRAINT constraint_2b PRIMARY KEY (internal_id);


--
-- Name: client_attributes constraint_3c; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.client_attributes
    ADD CONSTRAINT constraint_3c PRIMARY KEY (client_id, name);


--
-- Name: event_entity constraint_4; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.event_entity
    ADD CONSTRAINT constraint_4 PRIMARY KEY (id);


--
-- Name: federated_identity constraint_40; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.federated_identity
    ADD CONSTRAINT constraint_40 PRIMARY KEY (identity_provider, user_id);


--
-- Name: realm constraint_4a; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.realm
    ADD CONSTRAINT constraint_4a PRIMARY KEY (id);


--
-- Name: client_session_role constraint_5; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.client_session_role
    ADD CONSTRAINT constraint_5 PRIMARY KEY (client_session, role_id);


--
-- Name: user_session constraint_57; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.user_session
    ADD CONSTRAINT constraint_57 PRIMARY KEY (id);


--
-- Name: user_federation_provider constraint_5c; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.user_federation_provider
    ADD CONSTRAINT constraint_5c PRIMARY KEY (id);


--
-- Name: client_session_note constraint_5e; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.client_session_note
    ADD CONSTRAINT constraint_5e PRIMARY KEY (client_session, name);


--
-- Name: client constraint_7; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.client
    ADD CONSTRAINT constraint_7 PRIMARY KEY (id);


--
-- Name: client_session constraint_8; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.client_session
    ADD CONSTRAINT constraint_8 PRIMARY KEY (id);


--
-- Name: scope_mapping constraint_81; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.scope_mapping
    ADD CONSTRAINT constraint_81 PRIMARY KEY (client_id, role_id);


--
-- Name: client_node_registrations constraint_84; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.client_node_registrations
    ADD CONSTRAINT constraint_84 PRIMARY KEY (client_id, name);


--
-- Name: realm_attribute constraint_9; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.realm_attribute
    ADD CONSTRAINT constraint_9 PRIMARY KEY (name, realm_id);


--
-- Name: realm_required_credential constraint_92; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.realm_required_credential
    ADD CONSTRAINT constraint_92 PRIMARY KEY (realm_id, type);


--
-- Name: keycloak_role constraint_a; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.keycloak_role
    ADD CONSTRAINT constraint_a PRIMARY KEY (id);


--
-- Name: admin_event_entity constraint_admin_event_entity; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.admin_event_entity
    ADD CONSTRAINT constraint_admin_event_entity PRIMARY KEY (id);


--
-- Name: authenticator_config_entry constraint_auth_cfg_pk; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.authenticator_config_entry
    ADD CONSTRAINT constraint_auth_cfg_pk PRIMARY KEY (authenticator_id, name);


--
-- Name: authentication_execution constraint_auth_exec_pk; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.authentication_execution
    ADD CONSTRAINT constraint_auth_exec_pk PRIMARY KEY (id);


--
-- Name: authentication_flow constraint_auth_flow_pk; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.authentication_flow
    ADD CONSTRAINT constraint_auth_flow_pk PRIMARY KEY (id);


--
-- Name: authenticator_config constraint_auth_pk; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.authenticator_config
    ADD CONSTRAINT constraint_auth_pk PRIMARY KEY (id);


--
-- Name: client_session_auth_status constraint_auth_status_pk; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.client_session_auth_status
    ADD CONSTRAINT constraint_auth_status_pk PRIMARY KEY (client_session, authenticator);


--
-- Name: user_role_mapping constraint_c; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.user_role_mapping
    ADD CONSTRAINT constraint_c PRIMARY KEY (role_id, user_id);


--
-- Name: composite_role constraint_composite_role; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.composite_role
    ADD CONSTRAINT constraint_composite_role PRIMARY KEY (composite, child_role);


--
-- Name: client_session_prot_mapper constraint_cs_pmp_pk; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.client_session_prot_mapper
    ADD CONSTRAINT constraint_cs_pmp_pk PRIMARY KEY (client_session, protocol_mapper_id);


--
-- Name: identity_provider_config constraint_d; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.identity_provider_config
    ADD CONSTRAINT constraint_d PRIMARY KEY (identity_provider_id, name);


--
-- Name: policy_config constraint_dpc; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.policy_config
    ADD CONSTRAINT constraint_dpc PRIMARY KEY (policy_id, name);


--
-- Name: realm_smtp_config constraint_e; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.realm_smtp_config
    ADD CONSTRAINT constraint_e PRIMARY KEY (realm_id, name);


--
-- Name: credential constraint_f; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.credential
    ADD CONSTRAINT constraint_f PRIMARY KEY (id);


--
-- Name: user_federation_config constraint_f9; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.user_federation_config
    ADD CONSTRAINT constraint_f9 PRIMARY KEY (user_federation_provider_id, name);


--
-- Name: resource_server_perm_ticket constraint_fapmt; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.resource_server_perm_ticket
    ADD CONSTRAINT constraint_fapmt PRIMARY KEY (id);


--
-- Name: resource_server_resource constraint_farsr; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.resource_server_resource
    ADD CONSTRAINT constraint_farsr PRIMARY KEY (id);


--
-- Name: resource_server_policy constraint_farsrp; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.resource_server_policy
    ADD CONSTRAINT constraint_farsrp PRIMARY KEY (id);


--
-- Name: associated_policy constraint_farsrpap; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.associated_policy
    ADD CONSTRAINT constraint_farsrpap PRIMARY KEY (policy_id, associated_policy_id);


--
-- Name: resource_policy constraint_farsrpp; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.resource_policy
    ADD CONSTRAINT constraint_farsrpp PRIMARY KEY (resource_id, policy_id);


--
-- Name: resource_server_scope constraint_farsrs; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.resource_server_scope
    ADD CONSTRAINT constraint_farsrs PRIMARY KEY (id);


--
-- Name: resource_scope constraint_farsrsp; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.resource_scope
    ADD CONSTRAINT constraint_farsrsp PRIMARY KEY (resource_id, scope_id);


--
-- Name: scope_policy constraint_farsrsps; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.scope_policy
    ADD CONSTRAINT constraint_farsrsps PRIMARY KEY (scope_id, policy_id);


--
-- Name: user_entity constraint_fb; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.user_entity
    ADD CONSTRAINT constraint_fb PRIMARY KEY (id);


--
-- Name: user_federation_mapper_config constraint_fedmapper_cfg_pm; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.user_federation_mapper_config
    ADD CONSTRAINT constraint_fedmapper_cfg_pm PRIMARY KEY (user_federation_mapper_id, name);


--
-- Name: user_federation_mapper constraint_fedmapperpm; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.user_federation_mapper
    ADD CONSTRAINT constraint_fedmapperpm PRIMARY KEY (id);


--
-- Name: fed_user_consent_cl_scope constraint_fgrntcsnt_clsc_pm; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.fed_user_consent_cl_scope
    ADD CONSTRAINT constraint_fgrntcsnt_clsc_pm PRIMARY KEY (user_consent_id, scope_id);


--
-- Name: user_consent_client_scope constraint_grntcsnt_clsc_pm; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.user_consent_client_scope
    ADD CONSTRAINT constraint_grntcsnt_clsc_pm PRIMARY KEY (user_consent_id, scope_id);


--
-- Name: user_consent constraint_grntcsnt_pm; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.user_consent
    ADD CONSTRAINT constraint_grntcsnt_pm PRIMARY KEY (id);


--
-- Name: keycloak_group constraint_group; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.keycloak_group
    ADD CONSTRAINT constraint_group PRIMARY KEY (id);


--
-- Name: group_attribute constraint_group_attribute_pk; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.group_attribute
    ADD CONSTRAINT constraint_group_attribute_pk PRIMARY KEY (id);


--
-- Name: group_role_mapping constraint_group_role; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.group_role_mapping
    ADD CONSTRAINT constraint_group_role PRIMARY KEY (role_id, group_id);


--
-- Name: identity_provider_mapper constraint_idpm; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.identity_provider_mapper
    ADD CONSTRAINT constraint_idpm PRIMARY KEY (id);


--
-- Name: idp_mapper_config constraint_idpmconfig; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.idp_mapper_config
    ADD CONSTRAINT constraint_idpmconfig PRIMARY KEY (idp_mapper_id, name);


--
-- Name: migration_model constraint_migmod; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.migration_model
    ADD CONSTRAINT constraint_migmod PRIMARY KEY (id);


--
-- Name: offline_client_session constraint_offl_cl_ses_pk3; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.offline_client_session
    ADD CONSTRAINT constraint_offl_cl_ses_pk3 PRIMARY KEY (user_session_id, client_id, client_storage_provider, external_client_id, offline_flag);


--
-- Name: offline_user_session constraint_offl_us_ses_pk2; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.offline_user_session
    ADD CONSTRAINT constraint_offl_us_ses_pk2 PRIMARY KEY (user_session_id, offline_flag);


--
-- Name: protocol_mapper constraint_pcm; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.protocol_mapper
    ADD CONSTRAINT constraint_pcm PRIMARY KEY (id);


--
-- Name: protocol_mapper_config constraint_pmconfig; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.protocol_mapper_config
    ADD CONSTRAINT constraint_pmconfig PRIMARY KEY (protocol_mapper_id, name);


--
-- Name: redirect_uris constraint_redirect_uris; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.redirect_uris
    ADD CONSTRAINT constraint_redirect_uris PRIMARY KEY (client_id, value);


--
-- Name: required_action_config constraint_req_act_cfg_pk; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.required_action_config
    ADD CONSTRAINT constraint_req_act_cfg_pk PRIMARY KEY (required_action_id, name);


--
-- Name: required_action_provider constraint_req_act_prv_pk; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.required_action_provider
    ADD CONSTRAINT constraint_req_act_prv_pk PRIMARY KEY (id);


--
-- Name: user_required_action constraint_required_action; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.user_required_action
    ADD CONSTRAINT constraint_required_action PRIMARY KEY (required_action, user_id);


--
-- Name: resource_uris constraint_resour_uris_pk; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.resource_uris
    ADD CONSTRAINT constraint_resour_uris_pk PRIMARY KEY (resource_id, value);


--
-- Name: role_attribute constraint_role_attribute_pk; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.role_attribute
    ADD CONSTRAINT constraint_role_attribute_pk PRIMARY KEY (id);


--
-- Name: user_attribute constraint_user_attribute_pk; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.user_attribute
    ADD CONSTRAINT constraint_user_attribute_pk PRIMARY KEY (id);


--
-- Name: user_group_membership constraint_user_group; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.user_group_membership
    ADD CONSTRAINT constraint_user_group PRIMARY KEY (group_id, user_id);


--
-- Name: user_session_note constraint_usn_pk; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.user_session_note
    ADD CONSTRAINT constraint_usn_pk PRIMARY KEY (user_session, name);


--
-- Name: web_origins constraint_web_origins; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.web_origins
    ADD CONSTRAINT constraint_web_origins PRIMARY KEY (client_id, value);


--
-- Name: databasechangeloglock databasechangeloglock_pkey; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.databasechangeloglock
    ADD CONSTRAINT databasechangeloglock_pkey PRIMARY KEY (id);


--
-- Name: client_scope_attributes pk_cl_tmpl_attr; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.client_scope_attributes
    ADD CONSTRAINT pk_cl_tmpl_attr PRIMARY KEY (scope_id, name);


--
-- Name: client_scope pk_cli_template; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.client_scope
    ADD CONSTRAINT pk_cli_template PRIMARY KEY (id);


--
-- Name: resource_server pk_resource_server; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.resource_server
    ADD CONSTRAINT pk_resource_server PRIMARY KEY (id);


--
-- Name: client_scope_role_mapping pk_template_scope; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.client_scope_role_mapping
    ADD CONSTRAINT pk_template_scope PRIMARY KEY (scope_id, role_id);


--
-- Name: default_client_scope r_def_cli_scope_bind; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.default_client_scope
    ADD CONSTRAINT r_def_cli_scope_bind PRIMARY KEY (realm_id, scope_id);


--
-- Name: realm_localizations realm_localizations_pkey; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.realm_localizations
    ADD CONSTRAINT realm_localizations_pkey PRIMARY KEY (realm_id, locale);


--
-- Name: resource_attribute res_attr_pk; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.resource_attribute
    ADD CONSTRAINT res_attr_pk PRIMARY KEY (id);


--
-- Name: keycloak_group sibling_names; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.keycloak_group
    ADD CONSTRAINT sibling_names UNIQUE (realm_id, parent_group, name);


--
-- Name: identity_provider uk_2daelwnibji49avxsrtuf6xj33; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.identity_provider
    ADD CONSTRAINT uk_2daelwnibji49avxsrtuf6xj33 UNIQUE (provider_alias, realm_id);


--
-- Name: client uk_b71cjlbenv945rb6gcon438at; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.client
    ADD CONSTRAINT uk_b71cjlbenv945rb6gcon438at UNIQUE (realm_id, client_id);


--
-- Name: client_scope uk_cli_scope; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.client_scope
    ADD CONSTRAINT uk_cli_scope UNIQUE (realm_id, name);


--
-- Name: user_entity uk_dykn684sl8up1crfei6eckhd7; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.user_entity
    ADD CONSTRAINT uk_dykn684sl8up1crfei6eckhd7 UNIQUE (realm_id, email_constraint);


--
-- Name: resource_server_resource uk_frsr6t700s9v50bu18ws5ha6; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.resource_server_resource
    ADD CONSTRAINT uk_frsr6t700s9v50bu18ws5ha6 UNIQUE (name, owner, resource_server_id);


--
-- Name: resource_server_perm_ticket uk_frsr6t700s9v50bu18ws5pmt; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.resource_server_perm_ticket
    ADD CONSTRAINT uk_frsr6t700s9v50bu18ws5pmt UNIQUE (owner, requester, resource_server_id, resource_id, scope_id);


--
-- Name: resource_server_policy uk_frsrpt700s9v50bu18ws5ha6; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.resource_server_policy
    ADD CONSTRAINT uk_frsrpt700s9v50bu18ws5ha6 UNIQUE (name, resource_server_id);


--
-- Name: resource_server_scope uk_frsrst700s9v50bu18ws5ha6; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.resource_server_scope
    ADD CONSTRAINT uk_frsrst700s9v50bu18ws5ha6 UNIQUE (name, resource_server_id);


--
-- Name: user_consent uk_jkuwuvd56ontgsuhogm8uewrt; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.user_consent
    ADD CONSTRAINT uk_jkuwuvd56ontgsuhogm8uewrt UNIQUE (client_id, client_storage_provider, external_client_id, user_id);


--
-- Name: realm uk_orvsdmla56612eaefiq6wl5oi; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.realm
    ADD CONSTRAINT uk_orvsdmla56612eaefiq6wl5oi UNIQUE (name);


--
-- Name: user_entity uk_ru8tt6t700s9v50bu18ws5ha6; Type: CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.user_entity
    ADD CONSTRAINT uk_ru8tt6t700s9v50bu18ws5ha6 UNIQUE (realm_id, username);


--
-- Name: banned_draft_cards banned_draft_cards_pkey; Type: CONSTRAINT; Schema: spellsource; Owner: admin
--

ALTER TABLE ONLY spellsource.banned_draft_cards
    ADD CONSTRAINT banned_draft_cards_pkey PRIMARY KEY (card_id);


--
-- Name: bot_users bot_users_pkey; Type: CONSTRAINT; Schema: spellsource; Owner: admin
--

ALTER TABLE ONLY spellsource.bot_users
    ADD CONSTRAINT bot_users_pkey PRIMARY KEY (id);


--
-- Name: cards_in_deck cards_in_deck_pkey; Type: CONSTRAINT; Schema: spellsource; Owner: admin
--

ALTER TABLE ONLY spellsource.cards_in_deck
    ADD CONSTRAINT cards_in_deck_pkey PRIMARY KEY (id);


--
-- Name: cards cards_pkey; Type: CONSTRAINT; Schema: spellsource; Owner: admin
--

ALTER TABLE ONLY spellsource.cards
    ADD CONSTRAINT cards_pkey PRIMARY KEY (succession);


--
-- Name: deck_player_attribute_tuples deck_player_attribute_tuples_pkey; Type: CONSTRAINT; Schema: spellsource; Owner: admin
--

ALTER TABLE ONLY spellsource.deck_player_attribute_tuples
    ADD CONSTRAINT deck_player_attribute_tuples_pkey PRIMARY KEY (id);


--
-- Name: deck_shares deck_shares_pkey; Type: CONSTRAINT; Schema: spellsource; Owner: admin
--

ALTER TABLE ONLY spellsource.deck_shares
    ADD CONSTRAINT deck_shares_pkey PRIMARY KEY (deck_id, share_recipient_id);


--
-- Name: decks decks_pkey; Type: CONSTRAINT; Schema: spellsource; Owner: admin
--

ALTER TABLE ONLY spellsource.decks
    ADD CONSTRAINT decks_pkey PRIMARY KEY (id);


--
-- Name: friends friends_pkey; Type: CONSTRAINT; Schema: spellsource; Owner: admin
--

ALTER TABLE ONLY spellsource.friends
    ADD CONSTRAINT friends_pkey PRIMARY KEY (id, friend);


--
-- Name: game_users game_users_pkey; Type: CONSTRAINT; Schema: spellsource; Owner: admin
--

ALTER TABLE ONLY spellsource.game_users
    ADD CONSTRAINT game_users_pkey PRIMARY KEY (game_id, user_id);


--
-- Name: games games_pkey; Type: CONSTRAINT; Schema: spellsource; Owner: admin
--

ALTER TABLE ONLY spellsource.games
    ADD CONSTRAINT games_pkey PRIMARY KEY (id);


--
-- Name: generated_art generated_art_hash_owner_key; Type: CONSTRAINT; Schema: spellsource; Owner: admin
--

ALTER TABLE ONLY spellsource.generated_art
    ADD CONSTRAINT generated_art_hash_owner_key UNIQUE (hash, owner);


--
-- Name: guests guests_pkey; Type: CONSTRAINT; Schema: spellsource; Owner: admin
--

ALTER TABLE ONLY spellsource.guests
    ADD CONSTRAINT guests_pkey PRIMARY KEY (id);


--
-- Name: hard_removal_cards hard_removal_cards_pkey; Type: CONSTRAINT; Schema: spellsource; Owner: admin
--

ALTER TABLE ONLY spellsource.hard_removal_cards
    ADD CONSTRAINT hard_removal_cards_pkey PRIMARY KEY (card_id);


--
-- Name: matchmaking_queues matchmaking_queues_pkey; Type: CONSTRAINT; Schema: spellsource; Owner: admin
--

ALTER TABLE ONLY spellsource.matchmaking_queues
    ADD CONSTRAINT matchmaking_queues_pkey PRIMARY KEY (id);


--
-- Name: matchmaking_tickets matchmaking_tickets_pkey; Type: CONSTRAINT; Schema: spellsource; Owner: admin
--

ALTER TABLE ONLY spellsource.matchmaking_tickets
    ADD CONSTRAINT matchmaking_tickets_pkey PRIMARY KEY (user_id);


--
-- Name: published_cards published_cards_pkey; Type: CONSTRAINT; Schema: spellsource; Owner: admin
--

ALTER TABLE ONLY spellsource.published_cards
    ADD CONSTRAINT published_cards_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: hiddenswitch; Owner: admin
--

CREATE INDEX flyway_schema_history_s_idx ON hiddenswitch.flyway_schema_history USING btree (success);


--
-- Name: idx_admin_event_time; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_admin_event_time ON keycloak.admin_event_entity USING btree (realm_id, admin_event_time);


--
-- Name: idx_assoc_pol_assoc_pol_id; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_assoc_pol_assoc_pol_id ON keycloak.associated_policy USING btree (associated_policy_id);


--
-- Name: idx_auth_config_realm; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_auth_config_realm ON keycloak.authenticator_config USING btree (realm_id);


--
-- Name: idx_auth_exec_flow; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_auth_exec_flow ON keycloak.authentication_execution USING btree (flow_id);


--
-- Name: idx_auth_exec_realm_flow; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_auth_exec_realm_flow ON keycloak.authentication_execution USING btree (realm_id, flow_id);


--
-- Name: idx_auth_flow_realm; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_auth_flow_realm ON keycloak.authentication_flow USING btree (realm_id);


--
-- Name: idx_cl_clscope; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_cl_clscope ON keycloak.client_scope_client USING btree (scope_id);


--
-- Name: idx_client_id; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_client_id ON keycloak.client USING btree (client_id);


--
-- Name: idx_client_init_acc_realm; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_client_init_acc_realm ON keycloak.client_initial_access USING btree (realm_id);


--
-- Name: idx_client_session_session; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_client_session_session ON keycloak.client_session USING btree (session_id);


--
-- Name: idx_clscope_attrs; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_clscope_attrs ON keycloak.client_scope_attributes USING btree (scope_id);


--
-- Name: idx_clscope_cl; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_clscope_cl ON keycloak.client_scope_client USING btree (client_id);


--
-- Name: idx_clscope_protmap; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_clscope_protmap ON keycloak.protocol_mapper USING btree (client_scope_id);


--
-- Name: idx_clscope_role; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_clscope_role ON keycloak.client_scope_role_mapping USING btree (scope_id);


--
-- Name: idx_compo_config_compo; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_compo_config_compo ON keycloak.component_config USING btree (component_id);


--
-- Name: idx_component_provider_type; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_component_provider_type ON keycloak.component USING btree (provider_type);


--
-- Name: idx_component_realm; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_component_realm ON keycloak.component USING btree (realm_id);


--
-- Name: idx_composite; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_composite ON keycloak.composite_role USING btree (composite);


--
-- Name: idx_composite_child; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_composite_child ON keycloak.composite_role USING btree (child_role);


--
-- Name: idx_defcls_realm; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_defcls_realm ON keycloak.default_client_scope USING btree (realm_id);


--
-- Name: idx_defcls_scope; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_defcls_scope ON keycloak.default_client_scope USING btree (scope_id);


--
-- Name: idx_event_time; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_event_time ON keycloak.event_entity USING btree (realm_id, event_time);


--
-- Name: idx_fedidentity_feduser; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_fedidentity_feduser ON keycloak.federated_identity USING btree (federated_user_id);


--
-- Name: idx_fedidentity_user; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_fedidentity_user ON keycloak.federated_identity USING btree (user_id);


--
-- Name: idx_fu_attribute; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_fu_attribute ON keycloak.fed_user_attribute USING btree (user_id, realm_id, name);


--
-- Name: idx_fu_cnsnt_ext; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_fu_cnsnt_ext ON keycloak.fed_user_consent USING btree (user_id, client_storage_provider, external_client_id);


--
-- Name: idx_fu_consent; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_fu_consent ON keycloak.fed_user_consent USING btree (user_id, client_id);


--
-- Name: idx_fu_consent_ru; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_fu_consent_ru ON keycloak.fed_user_consent USING btree (realm_id, user_id);


--
-- Name: idx_fu_credential; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_fu_credential ON keycloak.fed_user_credential USING btree (user_id, type);


--
-- Name: idx_fu_credential_ru; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_fu_credential_ru ON keycloak.fed_user_credential USING btree (realm_id, user_id);


--
-- Name: idx_fu_group_membership; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_fu_group_membership ON keycloak.fed_user_group_membership USING btree (user_id, group_id);


--
-- Name: idx_fu_group_membership_ru; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_fu_group_membership_ru ON keycloak.fed_user_group_membership USING btree (realm_id, user_id);


--
-- Name: idx_fu_required_action; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_fu_required_action ON keycloak.fed_user_required_action USING btree (user_id, required_action);


--
-- Name: idx_fu_required_action_ru; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_fu_required_action_ru ON keycloak.fed_user_required_action USING btree (realm_id, user_id);


--
-- Name: idx_fu_role_mapping; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_fu_role_mapping ON keycloak.fed_user_role_mapping USING btree (user_id, role_id);


--
-- Name: idx_fu_role_mapping_ru; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_fu_role_mapping_ru ON keycloak.fed_user_role_mapping USING btree (realm_id, user_id);


--
-- Name: idx_group_att_by_name_value; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_group_att_by_name_value ON keycloak.group_attribute USING btree (name, ((value)::character varying(250)));


--
-- Name: idx_group_attr_group; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_group_attr_group ON keycloak.group_attribute USING btree (group_id);


--
-- Name: idx_group_role_mapp_group; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_group_role_mapp_group ON keycloak.group_role_mapping USING btree (group_id);


--
-- Name: idx_id_prov_mapp_realm; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_id_prov_mapp_realm ON keycloak.identity_provider_mapper USING btree (realm_id);


--
-- Name: idx_ident_prov_realm; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_ident_prov_realm ON keycloak.identity_provider USING btree (realm_id);


--
-- Name: idx_keycloak_role_client; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_keycloak_role_client ON keycloak.keycloak_role USING btree (client);


--
-- Name: idx_keycloak_role_realm; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_keycloak_role_realm ON keycloak.keycloak_role USING btree (realm);


--
-- Name: idx_offline_css_preload; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_offline_css_preload ON keycloak.offline_client_session USING btree (client_id, offline_flag);


--
-- Name: idx_offline_uss_by_user; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_offline_uss_by_user ON keycloak.offline_user_session USING btree (user_id, realm_id, offline_flag);


--
-- Name: idx_offline_uss_by_usersess; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_offline_uss_by_usersess ON keycloak.offline_user_session USING btree (realm_id, offline_flag, user_session_id);


--
-- Name: idx_offline_uss_createdon; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_offline_uss_createdon ON keycloak.offline_user_session USING btree (created_on);


--
-- Name: idx_offline_uss_preload; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_offline_uss_preload ON keycloak.offline_user_session USING btree (offline_flag, created_on, user_session_id);


--
-- Name: idx_protocol_mapper_client; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_protocol_mapper_client ON keycloak.protocol_mapper USING btree (client_id);


--
-- Name: idx_realm_attr_realm; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_realm_attr_realm ON keycloak.realm_attribute USING btree (realm_id);


--
-- Name: idx_realm_clscope; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_realm_clscope ON keycloak.client_scope USING btree (realm_id);


--
-- Name: idx_realm_def_grp_realm; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_realm_def_grp_realm ON keycloak.realm_default_groups USING btree (realm_id);


--
-- Name: idx_realm_evt_list_realm; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_realm_evt_list_realm ON keycloak.realm_events_listeners USING btree (realm_id);


--
-- Name: idx_realm_evt_types_realm; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_realm_evt_types_realm ON keycloak.realm_enabled_event_types USING btree (realm_id);


--
-- Name: idx_realm_master_adm_cli; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_realm_master_adm_cli ON keycloak.realm USING btree (master_admin_client);


--
-- Name: idx_realm_supp_local_realm; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_realm_supp_local_realm ON keycloak.realm_supported_locales USING btree (realm_id);


--
-- Name: idx_redir_uri_client; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_redir_uri_client ON keycloak.redirect_uris USING btree (client_id);


--
-- Name: idx_req_act_prov_realm; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_req_act_prov_realm ON keycloak.required_action_provider USING btree (realm_id);


--
-- Name: idx_res_policy_policy; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_res_policy_policy ON keycloak.resource_policy USING btree (policy_id);


--
-- Name: idx_res_scope_scope; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_res_scope_scope ON keycloak.resource_scope USING btree (scope_id);


--
-- Name: idx_res_serv_pol_res_serv; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_res_serv_pol_res_serv ON keycloak.resource_server_policy USING btree (resource_server_id);


--
-- Name: idx_res_srv_res_res_srv; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_res_srv_res_res_srv ON keycloak.resource_server_resource USING btree (resource_server_id);


--
-- Name: idx_res_srv_scope_res_srv; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_res_srv_scope_res_srv ON keycloak.resource_server_scope USING btree (resource_server_id);


--
-- Name: idx_role_attribute; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_role_attribute ON keycloak.role_attribute USING btree (role_id);


--
-- Name: idx_role_clscope; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_role_clscope ON keycloak.client_scope_role_mapping USING btree (role_id);


--
-- Name: idx_scope_mapping_role; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_scope_mapping_role ON keycloak.scope_mapping USING btree (role_id);


--
-- Name: idx_scope_policy_policy; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_scope_policy_policy ON keycloak.scope_policy USING btree (policy_id);


--
-- Name: idx_update_time; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_update_time ON keycloak.migration_model USING btree (update_time);


--
-- Name: idx_us_sess_id_on_cl_sess; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_us_sess_id_on_cl_sess ON keycloak.offline_client_session USING btree (user_session_id);


--
-- Name: idx_usconsent_clscope; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_usconsent_clscope ON keycloak.user_consent_client_scope USING btree (user_consent_id);


--
-- Name: idx_user_attribute; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_user_attribute ON keycloak.user_attribute USING btree (user_id);


--
-- Name: idx_user_attribute_name; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_user_attribute_name ON keycloak.user_attribute USING btree (name, value);


--
-- Name: idx_user_consent; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_user_consent ON keycloak.user_consent USING btree (user_id);


--
-- Name: idx_user_credential; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_user_credential ON keycloak.credential USING btree (user_id);


--
-- Name: idx_user_email; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_user_email ON keycloak.user_entity USING btree (email);


--
-- Name: idx_user_group_mapping; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_user_group_mapping ON keycloak.user_group_membership USING btree (user_id);


--
-- Name: idx_user_reqactions; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_user_reqactions ON keycloak.user_required_action USING btree (user_id);


--
-- Name: idx_user_role_mapping; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_user_role_mapping ON keycloak.user_role_mapping USING btree (user_id);


--
-- Name: idx_user_service_account; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_user_service_account ON keycloak.user_entity USING btree (realm_id, service_account_client_link);


--
-- Name: idx_usr_fed_map_fed_prv; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_usr_fed_map_fed_prv ON keycloak.user_federation_mapper USING btree (federation_provider_id);


--
-- Name: idx_usr_fed_map_realm; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_usr_fed_map_realm ON keycloak.user_federation_mapper USING btree (realm_id);


--
-- Name: idx_usr_fed_prv_realm; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_usr_fed_prv_realm ON keycloak.user_federation_provider USING btree (realm_id);


--
-- Name: idx_web_orig_client; Type: INDEX; Schema: keycloak; Owner: admin
--

CREATE INDEX idx_web_orig_client ON keycloak.web_origins USING btree (client_id);


--
-- Name: deck_shares_trashed_by_recipient_idx; Type: INDEX; Schema: spellsource; Owner: admin
--

CREATE INDEX deck_shares_trashed_by_recipient_idx ON spellsource.deck_shares USING btree (trashed_by_recipient) WHERE (trashed_by_recipient IS FALSE);


--
-- Name: decks_created_by_idx; Type: INDEX; Schema: spellsource; Owner: admin
--

CREATE INDEX decks_created_by_idx ON spellsource.decks USING btree (created_by);


--
-- Name: decks_is_premade_idx; Type: INDEX; Schema: spellsource; Owner: admin
--

CREATE INDEX decks_is_premade_idx ON spellsource.decks USING btree (is_premade) WHERE (is_premade IS TRUE);


--
-- Name: decks_trashed_idx; Type: INDEX; Schema: spellsource; Owner: admin
--

CREATE INDEX decks_trashed_idx ON spellsource.decks USING btree (trashed) WHERE (is_premade IS FALSE);


--
-- Name: idx_card_created_by; Type: INDEX; Schema: spellsource; Owner: admin
--

CREATE INDEX idx_card_created_by ON spellsource.cards USING btree (created_by);


--
-- Name: idx_card_id; Type: INDEX; Schema: spellsource; Owner: admin
--

CREATE INDEX idx_card_id ON spellsource.cards USING btree (id);


--
-- Name: idx_card_id_succession; Type: INDEX; Schema: spellsource; Owner: admin
--

CREATE INDEX idx_card_id_succession ON spellsource.cards USING btree (id, succession);


--
-- Name: idx_card_script_ai_hardremoval; Type: INDEX; Schema: spellsource; Owner: admin
--

CREATE INDEX idx_card_script_ai_hardremoval ON spellsource.cards USING btree ((((card_script -> 'artificialIntelligence'::text) ->> 'hardRemoval'::text)));


--
-- Name: idx_card_script_attributes; Type: INDEX; Schema: spellsource; Owner: admin
--

CREATE INDEX idx_card_script_attributes ON spellsource.cards USING gin (((card_script -> 'attributes'::text)));


--
-- Name: idx_card_script_draft_banned; Type: INDEX; Schema: spellsource; Owner: admin
--

CREATE INDEX idx_card_script_draft_banned ON spellsource.cards USING btree ((((card_script -> 'draft'::text) ->> 'banned'::text)));


--
-- Name: idx_card_script_heroclass; Type: INDEX; Schema: spellsource; Owner: admin
--

CREATE INDEX idx_card_script_heroclass ON spellsource.cards USING btree (((card_script ->> 'heroClass'::text)));


--
-- Name: idx_card_script_name; Type: INDEX; Schema: spellsource; Owner: admin
--

CREATE INDEX idx_card_script_name ON spellsource.cards USING btree (((card_script ->> 'name'::text)));


--
-- Name: idx_card_script_rarity; Type: INDEX; Schema: spellsource; Owner: admin
--

CREATE INDEX idx_card_script_rarity ON spellsource.cards USING btree (((card_script ->> 'rarity'::text)));


--
-- Name: idx_card_script_set; Type: INDEX; Schema: spellsource; Owner: admin
--

CREATE INDEX idx_card_script_set ON spellsource.cards USING btree (((card_script ->> 'set'::text)));


--
-- Name: idx_card_script_type; Type: INDEX; Schema: spellsource; Owner: admin
--

CREATE INDEX idx_card_script_type ON spellsource.cards USING btree (((card_script ->> 'type'::text)));


--
-- Name: matchmaking_tickets_queue_id_idx; Type: INDEX; Schema: spellsource; Owner: admin
--

CREATE INDEX matchmaking_tickets_queue_id_idx ON spellsource.matchmaking_tickets USING btree (queue_id);


--
-- Name: spellsource_cards_unique_id; Type: INDEX; Schema: spellsource; Owner: admin
--

CREATE UNIQUE INDEX spellsource_cards_unique_id ON spellsource.cards USING btree (id) WHERE (is_published AND (NOT is_archived));


--
-- Name: cards card_changes_trigger; Type: TRIGGER; Schema: spellsource; Owner: admin
--

CREATE TRIGGER card_changes_trigger AFTER INSERT OR DELETE OR UPDATE ON spellsource.cards FOR EACH ROW EXECUTE FUNCTION spellsource.card_change_notify_event();


--
-- Name: cards card_published_insert; Type: TRIGGER; Schema: spellsource; Owner: admin
--

CREATE TRIGGER card_published_insert BEFORE INSERT ON spellsource.cards FOR EACH ROW WHEN ((new.is_published AND (NOT new.is_archived))) EXECUTE FUNCTION spellsource.on_card_published();


--
-- Name: cards card_published_update; Type: TRIGGER; Schema: spellsource; Owner: admin
--

CREATE TRIGGER card_published_update BEFORE UPDATE ON spellsource.cards FOR EACH ROW WHEN (((NOT (old.is_published AND (NOT old.is_archived))) AND (new.is_published AND (NOT new.is_archived)))) EXECUTE FUNCTION spellsource.on_card_published();


--
-- Name: client_session_auth_status auth_status_constraint; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.client_session_auth_status
    ADD CONSTRAINT auth_status_constraint FOREIGN KEY (client_session) REFERENCES keycloak.client_session(id);


--
-- Name: identity_provider fk2b4ebc52ae5c3b34; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.identity_provider
    ADD CONSTRAINT fk2b4ebc52ae5c3b34 FOREIGN KEY (realm_id) REFERENCES keycloak.realm(id);


--
-- Name: client_attributes fk3c47c64beacca966; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.client_attributes
    ADD CONSTRAINT fk3c47c64beacca966 FOREIGN KEY (client_id) REFERENCES keycloak.client(id);


--
-- Name: federated_identity fk404288b92ef007a6; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.federated_identity
    ADD CONSTRAINT fk404288b92ef007a6 FOREIGN KEY (user_id) REFERENCES keycloak.user_entity(id);


--
-- Name: client_node_registrations fk4129723ba992f594; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.client_node_registrations
    ADD CONSTRAINT fk4129723ba992f594 FOREIGN KEY (client_id) REFERENCES keycloak.client(id);


--
-- Name: client_session_note fk5edfb00ff51c2736; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.client_session_note
    ADD CONSTRAINT fk5edfb00ff51c2736 FOREIGN KEY (client_session) REFERENCES keycloak.client_session(id);


--
-- Name: user_session_note fk5edfb00ff51d3472; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.user_session_note
    ADD CONSTRAINT fk5edfb00ff51d3472 FOREIGN KEY (user_session) REFERENCES keycloak.user_session(id);


--
-- Name: client_session_role fk_11b7sgqw18i532811v7o2dv76; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.client_session_role
    ADD CONSTRAINT fk_11b7sgqw18i532811v7o2dv76 FOREIGN KEY (client_session) REFERENCES keycloak.client_session(id);


--
-- Name: redirect_uris fk_1burs8pb4ouj97h5wuppahv9f; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.redirect_uris
    ADD CONSTRAINT fk_1burs8pb4ouj97h5wuppahv9f FOREIGN KEY (client_id) REFERENCES keycloak.client(id);


--
-- Name: user_federation_provider fk_1fj32f6ptolw2qy60cd8n01e8; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.user_federation_provider
    ADD CONSTRAINT fk_1fj32f6ptolw2qy60cd8n01e8 FOREIGN KEY (realm_id) REFERENCES keycloak.realm(id);


--
-- Name: client_session_prot_mapper fk_33a8sgqw18i532811v7o2dk89; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.client_session_prot_mapper
    ADD CONSTRAINT fk_33a8sgqw18i532811v7o2dk89 FOREIGN KEY (client_session) REFERENCES keycloak.client_session(id);


--
-- Name: realm_required_credential fk_5hg65lybevavkqfki3kponh9v; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.realm_required_credential
    ADD CONSTRAINT fk_5hg65lybevavkqfki3kponh9v FOREIGN KEY (realm_id) REFERENCES keycloak.realm(id);


--
-- Name: resource_attribute fk_5hrm2vlf9ql5fu022kqepovbr; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.resource_attribute
    ADD CONSTRAINT fk_5hrm2vlf9ql5fu022kqepovbr FOREIGN KEY (resource_id) REFERENCES keycloak.resource_server_resource(id);


--
-- Name: user_attribute fk_5hrm2vlf9ql5fu043kqepovbr; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.user_attribute
    ADD CONSTRAINT fk_5hrm2vlf9ql5fu043kqepovbr FOREIGN KEY (user_id) REFERENCES keycloak.user_entity(id);


--
-- Name: user_required_action fk_6qj3w1jw9cvafhe19bwsiuvmd; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.user_required_action
    ADD CONSTRAINT fk_6qj3w1jw9cvafhe19bwsiuvmd FOREIGN KEY (user_id) REFERENCES keycloak.user_entity(id);


--
-- Name: keycloak_role fk_6vyqfe4cn4wlq8r6kt5vdsj5c; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.keycloak_role
    ADD CONSTRAINT fk_6vyqfe4cn4wlq8r6kt5vdsj5c FOREIGN KEY (realm) REFERENCES keycloak.realm(id);


--
-- Name: realm_smtp_config fk_70ej8xdxgxd0b9hh6180irr0o; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.realm_smtp_config
    ADD CONSTRAINT fk_70ej8xdxgxd0b9hh6180irr0o FOREIGN KEY (realm_id) REFERENCES keycloak.realm(id);


--
-- Name: realm_attribute fk_8shxd6l3e9atqukacxgpffptw; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.realm_attribute
    ADD CONSTRAINT fk_8shxd6l3e9atqukacxgpffptw FOREIGN KEY (realm_id) REFERENCES keycloak.realm(id);


--
-- Name: composite_role fk_a63wvekftu8jo1pnj81e7mce2; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.composite_role
    ADD CONSTRAINT fk_a63wvekftu8jo1pnj81e7mce2 FOREIGN KEY (composite) REFERENCES keycloak.keycloak_role(id);


--
-- Name: authentication_execution fk_auth_exec_flow; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.authentication_execution
    ADD CONSTRAINT fk_auth_exec_flow FOREIGN KEY (flow_id) REFERENCES keycloak.authentication_flow(id);


--
-- Name: authentication_execution fk_auth_exec_realm; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.authentication_execution
    ADD CONSTRAINT fk_auth_exec_realm FOREIGN KEY (realm_id) REFERENCES keycloak.realm(id);


--
-- Name: authentication_flow fk_auth_flow_realm; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.authentication_flow
    ADD CONSTRAINT fk_auth_flow_realm FOREIGN KEY (realm_id) REFERENCES keycloak.realm(id);


--
-- Name: authenticator_config fk_auth_realm; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.authenticator_config
    ADD CONSTRAINT fk_auth_realm FOREIGN KEY (realm_id) REFERENCES keycloak.realm(id);


--
-- Name: client_session fk_b4ao2vcvat6ukau74wbwtfqo1; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.client_session
    ADD CONSTRAINT fk_b4ao2vcvat6ukau74wbwtfqo1 FOREIGN KEY (session_id) REFERENCES keycloak.user_session(id);


--
-- Name: user_role_mapping fk_c4fqv34p1mbylloxang7b1q3l; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.user_role_mapping
    ADD CONSTRAINT fk_c4fqv34p1mbylloxang7b1q3l FOREIGN KEY (user_id) REFERENCES keycloak.user_entity(id);


--
-- Name: client_scope_attributes fk_cl_scope_attr_scope; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.client_scope_attributes
    ADD CONSTRAINT fk_cl_scope_attr_scope FOREIGN KEY (scope_id) REFERENCES keycloak.client_scope(id);


--
-- Name: client_scope_role_mapping fk_cl_scope_rm_scope; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.client_scope_role_mapping
    ADD CONSTRAINT fk_cl_scope_rm_scope FOREIGN KEY (scope_id) REFERENCES keycloak.client_scope(id);


--
-- Name: client_user_session_note fk_cl_usr_ses_note; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.client_user_session_note
    ADD CONSTRAINT fk_cl_usr_ses_note FOREIGN KEY (client_session) REFERENCES keycloak.client_session(id);


--
-- Name: protocol_mapper fk_cli_scope_mapper; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.protocol_mapper
    ADD CONSTRAINT fk_cli_scope_mapper FOREIGN KEY (client_scope_id) REFERENCES keycloak.client_scope(id);


--
-- Name: client_initial_access fk_client_init_acc_realm; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.client_initial_access
    ADD CONSTRAINT fk_client_init_acc_realm FOREIGN KEY (realm_id) REFERENCES keycloak.realm(id);


--
-- Name: component_config fk_component_config; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.component_config
    ADD CONSTRAINT fk_component_config FOREIGN KEY (component_id) REFERENCES keycloak.component(id);


--
-- Name: component fk_component_realm; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.component
    ADD CONSTRAINT fk_component_realm FOREIGN KEY (realm_id) REFERENCES keycloak.realm(id);


--
-- Name: realm_default_groups fk_def_groups_realm; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.realm_default_groups
    ADD CONSTRAINT fk_def_groups_realm FOREIGN KEY (realm_id) REFERENCES keycloak.realm(id);


--
-- Name: user_federation_mapper_config fk_fedmapper_cfg; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.user_federation_mapper_config
    ADD CONSTRAINT fk_fedmapper_cfg FOREIGN KEY (user_federation_mapper_id) REFERENCES keycloak.user_federation_mapper(id);


--
-- Name: user_federation_mapper fk_fedmapperpm_fedprv; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.user_federation_mapper
    ADD CONSTRAINT fk_fedmapperpm_fedprv FOREIGN KEY (federation_provider_id) REFERENCES keycloak.user_federation_provider(id);


--
-- Name: user_federation_mapper fk_fedmapperpm_realm; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.user_federation_mapper
    ADD CONSTRAINT fk_fedmapperpm_realm FOREIGN KEY (realm_id) REFERENCES keycloak.realm(id);


--
-- Name: associated_policy fk_frsr5s213xcx4wnkog82ssrfy; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.associated_policy
    ADD CONSTRAINT fk_frsr5s213xcx4wnkog82ssrfy FOREIGN KEY (associated_policy_id) REFERENCES keycloak.resource_server_policy(id);


--
-- Name: scope_policy fk_frsrasp13xcx4wnkog82ssrfy; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.scope_policy
    ADD CONSTRAINT fk_frsrasp13xcx4wnkog82ssrfy FOREIGN KEY (policy_id) REFERENCES keycloak.resource_server_policy(id);


--
-- Name: resource_server_perm_ticket fk_frsrho213xcx4wnkog82sspmt; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.resource_server_perm_ticket
    ADD CONSTRAINT fk_frsrho213xcx4wnkog82sspmt FOREIGN KEY (resource_server_id) REFERENCES keycloak.resource_server(id);


--
-- Name: resource_server_resource fk_frsrho213xcx4wnkog82ssrfy; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.resource_server_resource
    ADD CONSTRAINT fk_frsrho213xcx4wnkog82ssrfy FOREIGN KEY (resource_server_id) REFERENCES keycloak.resource_server(id);


--
-- Name: resource_server_perm_ticket fk_frsrho213xcx4wnkog83sspmt; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.resource_server_perm_ticket
    ADD CONSTRAINT fk_frsrho213xcx4wnkog83sspmt FOREIGN KEY (resource_id) REFERENCES keycloak.resource_server_resource(id);


--
-- Name: resource_server_perm_ticket fk_frsrho213xcx4wnkog84sspmt; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.resource_server_perm_ticket
    ADD CONSTRAINT fk_frsrho213xcx4wnkog84sspmt FOREIGN KEY (scope_id) REFERENCES keycloak.resource_server_scope(id);


--
-- Name: associated_policy fk_frsrpas14xcx4wnkog82ssrfy; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.associated_policy
    ADD CONSTRAINT fk_frsrpas14xcx4wnkog82ssrfy FOREIGN KEY (policy_id) REFERENCES keycloak.resource_server_policy(id);


--
-- Name: scope_policy fk_frsrpass3xcx4wnkog82ssrfy; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.scope_policy
    ADD CONSTRAINT fk_frsrpass3xcx4wnkog82ssrfy FOREIGN KEY (scope_id) REFERENCES keycloak.resource_server_scope(id);


--
-- Name: resource_server_perm_ticket fk_frsrpo2128cx4wnkog82ssrfy; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.resource_server_perm_ticket
    ADD CONSTRAINT fk_frsrpo2128cx4wnkog82ssrfy FOREIGN KEY (policy_id) REFERENCES keycloak.resource_server_policy(id);


--
-- Name: resource_server_policy fk_frsrpo213xcx4wnkog82ssrfy; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.resource_server_policy
    ADD CONSTRAINT fk_frsrpo213xcx4wnkog82ssrfy FOREIGN KEY (resource_server_id) REFERENCES keycloak.resource_server(id);


--
-- Name: resource_scope fk_frsrpos13xcx4wnkog82ssrfy; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.resource_scope
    ADD CONSTRAINT fk_frsrpos13xcx4wnkog82ssrfy FOREIGN KEY (resource_id) REFERENCES keycloak.resource_server_resource(id);


--
-- Name: resource_policy fk_frsrpos53xcx4wnkog82ssrfy; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.resource_policy
    ADD CONSTRAINT fk_frsrpos53xcx4wnkog82ssrfy FOREIGN KEY (resource_id) REFERENCES keycloak.resource_server_resource(id);


--
-- Name: resource_policy fk_frsrpp213xcx4wnkog82ssrfy; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.resource_policy
    ADD CONSTRAINT fk_frsrpp213xcx4wnkog82ssrfy FOREIGN KEY (policy_id) REFERENCES keycloak.resource_server_policy(id);


--
-- Name: resource_scope fk_frsrps213xcx4wnkog82ssrfy; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.resource_scope
    ADD CONSTRAINT fk_frsrps213xcx4wnkog82ssrfy FOREIGN KEY (scope_id) REFERENCES keycloak.resource_server_scope(id);


--
-- Name: resource_server_scope fk_frsrso213xcx4wnkog82ssrfy; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.resource_server_scope
    ADD CONSTRAINT fk_frsrso213xcx4wnkog82ssrfy FOREIGN KEY (resource_server_id) REFERENCES keycloak.resource_server(id);


--
-- Name: composite_role fk_gr7thllb9lu8q4vqa4524jjy8; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.composite_role
    ADD CONSTRAINT fk_gr7thllb9lu8q4vqa4524jjy8 FOREIGN KEY (child_role) REFERENCES keycloak.keycloak_role(id);


--
-- Name: user_consent_client_scope fk_grntcsnt_clsc_usc; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.user_consent_client_scope
    ADD CONSTRAINT fk_grntcsnt_clsc_usc FOREIGN KEY (user_consent_id) REFERENCES keycloak.user_consent(id);


--
-- Name: user_consent fk_grntcsnt_user; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.user_consent
    ADD CONSTRAINT fk_grntcsnt_user FOREIGN KEY (user_id) REFERENCES keycloak.user_entity(id);


--
-- Name: group_attribute fk_group_attribute_group; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.group_attribute
    ADD CONSTRAINT fk_group_attribute_group FOREIGN KEY (group_id) REFERENCES keycloak.keycloak_group(id);


--
-- Name: group_role_mapping fk_group_role_group; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.group_role_mapping
    ADD CONSTRAINT fk_group_role_group FOREIGN KEY (group_id) REFERENCES keycloak.keycloak_group(id);


--
-- Name: realm_enabled_event_types fk_h846o4h0w8epx5nwedrf5y69j; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.realm_enabled_event_types
    ADD CONSTRAINT fk_h846o4h0w8epx5nwedrf5y69j FOREIGN KEY (realm_id) REFERENCES keycloak.realm(id);


--
-- Name: realm_events_listeners fk_h846o4h0w8epx5nxev9f5y69j; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.realm_events_listeners
    ADD CONSTRAINT fk_h846o4h0w8epx5nxev9f5y69j FOREIGN KEY (realm_id) REFERENCES keycloak.realm(id);


--
-- Name: identity_provider_mapper fk_idpm_realm; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.identity_provider_mapper
    ADD CONSTRAINT fk_idpm_realm FOREIGN KEY (realm_id) REFERENCES keycloak.realm(id);


--
-- Name: idp_mapper_config fk_idpmconfig; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.idp_mapper_config
    ADD CONSTRAINT fk_idpmconfig FOREIGN KEY (idp_mapper_id) REFERENCES keycloak.identity_provider_mapper(id);


--
-- Name: web_origins fk_lojpho213xcx4wnkog82ssrfy; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.web_origins
    ADD CONSTRAINT fk_lojpho213xcx4wnkog82ssrfy FOREIGN KEY (client_id) REFERENCES keycloak.client(id);


--
-- Name: scope_mapping fk_ouse064plmlr732lxjcn1q5f1; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.scope_mapping
    ADD CONSTRAINT fk_ouse064plmlr732lxjcn1q5f1 FOREIGN KEY (client_id) REFERENCES keycloak.client(id);


--
-- Name: protocol_mapper fk_pcm_realm; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.protocol_mapper
    ADD CONSTRAINT fk_pcm_realm FOREIGN KEY (client_id) REFERENCES keycloak.client(id);


--
-- Name: credential fk_pfyr0glasqyl0dei3kl69r6v0; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.credential
    ADD CONSTRAINT fk_pfyr0glasqyl0dei3kl69r6v0 FOREIGN KEY (user_id) REFERENCES keycloak.user_entity(id);


--
-- Name: protocol_mapper_config fk_pmconfig; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.protocol_mapper_config
    ADD CONSTRAINT fk_pmconfig FOREIGN KEY (protocol_mapper_id) REFERENCES keycloak.protocol_mapper(id);


--
-- Name: default_client_scope fk_r_def_cli_scope_realm; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.default_client_scope
    ADD CONSTRAINT fk_r_def_cli_scope_realm FOREIGN KEY (realm_id) REFERENCES keycloak.realm(id);


--
-- Name: required_action_provider fk_req_act_realm; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.required_action_provider
    ADD CONSTRAINT fk_req_act_realm FOREIGN KEY (realm_id) REFERENCES keycloak.realm(id);


--
-- Name: resource_uris fk_resource_server_uris; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.resource_uris
    ADD CONSTRAINT fk_resource_server_uris FOREIGN KEY (resource_id) REFERENCES keycloak.resource_server_resource(id);


--
-- Name: role_attribute fk_role_attribute_id; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.role_attribute
    ADD CONSTRAINT fk_role_attribute_id FOREIGN KEY (role_id) REFERENCES keycloak.keycloak_role(id);


--
-- Name: realm_supported_locales fk_supported_locales_realm; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.realm_supported_locales
    ADD CONSTRAINT fk_supported_locales_realm FOREIGN KEY (realm_id) REFERENCES keycloak.realm(id);


--
-- Name: user_federation_config fk_t13hpu1j94r2ebpekr39x5eu5; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.user_federation_config
    ADD CONSTRAINT fk_t13hpu1j94r2ebpekr39x5eu5 FOREIGN KEY (user_federation_provider_id) REFERENCES keycloak.user_federation_provider(id);


--
-- Name: user_group_membership fk_user_group_user; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.user_group_membership
    ADD CONSTRAINT fk_user_group_user FOREIGN KEY (user_id) REFERENCES keycloak.user_entity(id);


--
-- Name: policy_config fkdc34197cf864c4e43; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.policy_config
    ADD CONSTRAINT fkdc34197cf864c4e43 FOREIGN KEY (policy_id) REFERENCES keycloak.resource_server_policy(id);


--
-- Name: identity_provider_config fkdc4897cf864c4e43; Type: FK CONSTRAINT; Schema: keycloak; Owner: admin
--

ALTER TABLE ONLY keycloak.identity_provider_config
    ADD CONSTRAINT fkdc4897cf864c4e43 FOREIGN KEY (identity_provider_id) REFERENCES keycloak.identity_provider(internal_id);


--
-- Name: bot_users bot_users_id_fkey; Type: FK CONSTRAINT; Schema: spellsource; Owner: admin
--

ALTER TABLE ONLY spellsource.bot_users
    ADD CONSTRAINT bot_users_id_fkey FOREIGN KEY (id) REFERENCES keycloak.user_entity(id) ON DELETE CASCADE;


--
-- Name: cards cards_created_by_fkey; Type: FK CONSTRAINT; Schema: spellsource; Owner: admin
--

ALTER TABLE ONLY spellsource.cards
    ADD CONSTRAINT cards_created_by_fkey FOREIGN KEY (created_by) REFERENCES keycloak.user_entity(id);


--
-- Name: cards_in_deck cards_in_deck_card_id_fkey; Type: FK CONSTRAINT; Schema: spellsource; Owner: admin
--

ALTER TABLE ONLY spellsource.cards_in_deck
    ADD CONSTRAINT cards_in_deck_card_id_fkey FOREIGN KEY (card_id) REFERENCES spellsource.published_cards(id);


--
-- Name: cards_in_deck cards_in_deck_deck_id_fkey; Type: FK CONSTRAINT; Schema: spellsource; Owner: admin
--

ALTER TABLE ONLY spellsource.cards_in_deck
    ADD CONSTRAINT cards_in_deck_deck_id_fkey FOREIGN KEY (deck_id) REFERENCES spellsource.decks(id) ON DELETE CASCADE;


--
-- Name: deck_player_attribute_tuples deck_player_attribute_tuples_deck_id_fkey; Type: FK CONSTRAINT; Schema: spellsource; Owner: admin
--

ALTER TABLE ONLY spellsource.deck_player_attribute_tuples
    ADD CONSTRAINT deck_player_attribute_tuples_deck_id_fkey FOREIGN KEY (deck_id) REFERENCES spellsource.decks(id) ON DELETE CASCADE;


--
-- Name: deck_shares deck_shares_deck_id_fkey; Type: FK CONSTRAINT; Schema: spellsource; Owner: admin
--

ALTER TABLE ONLY spellsource.deck_shares
    ADD CONSTRAINT deck_shares_deck_id_fkey FOREIGN KEY (deck_id) REFERENCES spellsource.decks(id) ON DELETE CASCADE;


--
-- Name: deck_shares deck_shares_share_recipient_id_fkey; Type: FK CONSTRAINT; Schema: spellsource; Owner: admin
--

ALTER TABLE ONLY spellsource.deck_shares
    ADD CONSTRAINT deck_shares_share_recipient_id_fkey FOREIGN KEY (share_recipient_id) REFERENCES keycloak.user_entity(id);


--
-- Name: decks decks_created_by_fkey; Type: FK CONSTRAINT; Schema: spellsource; Owner: admin
--

ALTER TABLE ONLY spellsource.decks
    ADD CONSTRAINT decks_created_by_fkey FOREIGN KEY (created_by) REFERENCES keycloak.user_entity(id);


--
-- Name: decks decks_last_edited_by_fkey; Type: FK CONSTRAINT; Schema: spellsource; Owner: admin
--

ALTER TABLE ONLY spellsource.decks
    ADD CONSTRAINT decks_last_edited_by_fkey FOREIGN KEY (last_edited_by) REFERENCES keycloak.user_entity(id);


--
-- Name: friends friends_friend_fkey; Type: FK CONSTRAINT; Schema: spellsource; Owner: admin
--

ALTER TABLE ONLY spellsource.friends
    ADD CONSTRAINT friends_friend_fkey FOREIGN KEY (friend) REFERENCES keycloak.user_entity(id) ON DELETE CASCADE;


--
-- Name: friends friends_id_fkey; Type: FK CONSTRAINT; Schema: spellsource; Owner: admin
--

ALTER TABLE ONLY spellsource.friends
    ADD CONSTRAINT friends_id_fkey FOREIGN KEY (id) REFERENCES keycloak.user_entity(id) ON DELETE CASCADE;


--
-- Name: game_users game_users_deck_id_fkey; Type: FK CONSTRAINT; Schema: spellsource; Owner: admin
--

ALTER TABLE ONLY spellsource.game_users
    ADD CONSTRAINT game_users_deck_id_fkey FOREIGN KEY (deck_id) REFERENCES spellsource.decks(id) ON DELETE SET NULL;


--
-- Name: game_users game_users_game_id_fkey; Type: FK CONSTRAINT; Schema: spellsource; Owner: admin
--

ALTER TABLE ONLY spellsource.game_users
    ADD CONSTRAINT game_users_game_id_fkey FOREIGN KEY (game_id) REFERENCES spellsource.games(id) ON DELETE CASCADE;


--
-- Name: game_users game_users_user_id_fkey; Type: FK CONSTRAINT; Schema: spellsource; Owner: admin
--

ALTER TABLE ONLY spellsource.game_users
    ADD CONSTRAINT game_users_user_id_fkey FOREIGN KEY (user_id) REFERENCES keycloak.user_entity(id) ON DELETE CASCADE;


--
-- Name: generated_art generated_art_owner_fkey; Type: FK CONSTRAINT; Schema: spellsource; Owner: admin
--

ALTER TABLE ONLY spellsource.generated_art
    ADD CONSTRAINT generated_art_owner_fkey FOREIGN KEY (owner) REFERENCES keycloak.user_entity(id);


--
-- Name: guests guests_user_id_fkey; Type: FK CONSTRAINT; Schema: spellsource; Owner: admin
--

ALTER TABLE ONLY spellsource.guests
    ADD CONSTRAINT guests_user_id_fkey FOREIGN KEY (user_id) REFERENCES keycloak.user_entity(id) ON DELETE CASCADE;


--
-- Name: matchmaking_tickets matchmaking_tickets_bot_deck_id_fkey; Type: FK CONSTRAINT; Schema: spellsource; Owner: admin
--

ALTER TABLE ONLY spellsource.matchmaking_tickets
    ADD CONSTRAINT matchmaking_tickets_bot_deck_id_fkey FOREIGN KEY (bot_deck_id) REFERENCES spellsource.decks(id);


--
-- Name: matchmaking_tickets matchmaking_tickets_deck_id_fkey; Type: FK CONSTRAINT; Schema: spellsource; Owner: admin
--

ALTER TABLE ONLY spellsource.matchmaking_tickets
    ADD CONSTRAINT matchmaking_tickets_deck_id_fkey FOREIGN KEY (deck_id) REFERENCES spellsource.decks(id) ON DELETE CASCADE;


--
-- Name: matchmaking_tickets matchmaking_tickets_queue_id_fkey; Type: FK CONSTRAINT; Schema: spellsource; Owner: admin
--

ALTER TABLE ONLY spellsource.matchmaking_tickets
    ADD CONSTRAINT matchmaking_tickets_queue_id_fkey FOREIGN KEY (queue_id) REFERENCES spellsource.matchmaking_queues(id) ON DELETE CASCADE;


--
-- Name: matchmaking_tickets matchmaking_tickets_user_id_fkey; Type: FK CONSTRAINT; Schema: spellsource; Owner: admin
--

ALTER TABLE ONLY spellsource.matchmaking_tickets
    ADD CONSTRAINT matchmaking_tickets_user_id_fkey FOREIGN KEY (user_id) REFERENCES keycloak.user_entity(id) ON DELETE CASCADE;


--
-- Name: published_cards published_cards_succession_fkey; Type: FK CONSTRAINT; Schema: spellsource; Owner: admin
--

ALTER TABLE ONLY spellsource.published_cards
    ADD CONSTRAINT published_cards_succession_fkey FOREIGN KEY (succession) REFERENCES spellsource.cards(succession) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: user_attribute; Type: ROW SECURITY; Schema: keycloak; Owner: admin
--

ALTER TABLE keycloak.user_attribute ENABLE ROW LEVEL SECURITY;

--
-- Name: user_attribute users; Type: POLICY; Schema: keycloak; Owner: admin
--

CREATE POLICY users ON keycloak.user_attribute USING (((user_id)::text = spellsource.get_user_id())) WITH CHECK (((user_id)::text = spellsource.get_user_id()));


--
-- Name: cards; Type: ROW SECURITY; Schema: spellsource; Owner: admin
--

ALTER TABLE spellsource.cards ENABLE ROW LEVEL SECURITY;

--
-- Name: cards_in_deck; Type: ROW SECURITY; Schema: spellsource; Owner: admin
--

ALTER TABLE spellsource.cards_in_deck ENABLE ROW LEVEL SECURITY;

--
-- Name: deck_shares; Type: ROW SECURITY; Schema: spellsource; Owner: admin
--

ALTER TABLE spellsource.deck_shares ENABLE ROW LEVEL SECURITY;

--
-- Name: decks; Type: ROW SECURITY; Schema: spellsource; Owner: admin
--

ALTER TABLE spellsource.decks ENABLE ROW LEVEL SECURITY;

--
-- Name: generated_art; Type: ROW SECURITY; Schema: spellsource; Owner: admin
--

ALTER TABLE spellsource.generated_art ENABLE ROW LEVEL SECURITY;

--
-- Name: generated_art insert_art; Type: POLICY; Schema: spellsource; Owner: admin
--

CREATE POLICY insert_art ON spellsource.generated_art FOR INSERT TO website WITH CHECK ((((owner)::text = spellsource.get_user_id()) AND (NOT is_archived)));


--
-- Name: cards public_view; Type: POLICY; Schema: spellsource; Owner: admin
--

CREATE POLICY public_view ON spellsource.cards FOR SELECT TO website USING (is_published);


--
-- Name: decks public_view; Type: POLICY; Schema: spellsource; Owner: admin
--

CREATE POLICY public_view ON spellsource.decks FOR SELECT USING (is_premade);


--
-- Name: published_cards; Type: ROW SECURITY; Schema: spellsource; Owner: admin
--

ALTER TABLE spellsource.published_cards ENABLE ROW LEVEL SECURITY;

--
-- Name: generated_art update_art; Type: POLICY; Schema: spellsource; Owner: admin
--

CREATE POLICY update_art ON spellsource.generated_art FOR UPDATE TO website USING (((owner)::text = spellsource.get_user_id())) WITH CHECK (((owner)::text = spellsource.get_user_id()));


--
-- Name: generated_art view_art; Type: POLICY; Schema: spellsource; Owner: admin
--

CREATE POLICY view_art ON spellsource.generated_art FOR SELECT TO website USING (((owner)::text = spellsource.get_user_id()));


--
-- Name: cards_in_deck website_delete; Type: POLICY; Schema: spellsource; Owner: admin
--

CREATE POLICY website_delete ON spellsource.cards_in_deck FOR DELETE TO website USING ((EXISTS ( SELECT decks.id,
    decks.created_by,
    decks.last_edited_by,
    decks.name,
    decks.hero_class,
    decks.trashed,
    decks.format,
    decks.deck_type,
    decks.is_premade,
    decks.permitted_to_duplicate
   FROM spellsource.decks
  WHERE ((decks.id = cards_in_deck.deck_id) AND ((decks.created_by)::text = spellsource.get_user_id())))));


--
-- Name: published_cards website_edit; Type: POLICY; Schema: spellsource; Owner: admin
--

CREATE POLICY website_edit ON spellsource.published_cards USING (starts_with(id, spellsource.get_user_id())) WITH CHECK (starts_with(id, spellsource.get_user_id()));


--
-- Name: cards website_insert; Type: POLICY; Schema: spellsource; Owner: admin
--

CREATE POLICY website_insert ON spellsource.cards FOR INSERT TO website WITH CHECK ((((created_by)::text = spellsource.get_user_id()) AND starts_with(id, spellsource.get_user_id())));


--
-- Name: cards_in_deck website_insert; Type: POLICY; Schema: spellsource; Owner: admin
--

CREATE POLICY website_insert ON spellsource.cards_in_deck FOR INSERT TO website WITH CHECK ((EXISTS ( SELECT decks.id,
    decks.created_by,
    decks.last_edited_by,
    decks.name,
    decks.hero_class,
    decks.trashed,
    decks.format,
    decks.deck_type,
    decks.is_premade,
    decks.permitted_to_duplicate
   FROM spellsource.decks
  WHERE ((decks.id = cards_in_deck.deck_id) AND ((decks.created_by)::text = spellsource.get_user_id())))));


--
-- Name: decks website_insert; Type: POLICY; Schema: spellsource; Owner: admin
--

CREATE POLICY website_insert ON spellsource.decks FOR INSERT TO website WITH CHECK (((created_by)::text = spellsource.get_user_id()));


--
-- Name: cards website_update; Type: POLICY; Schema: spellsource; Owner: admin
--

CREATE POLICY website_update ON spellsource.cards FOR UPDATE TO website USING ((((created_by)::text = spellsource.get_user_id()) AND starts_with(id, spellsource.get_user_id()))) WITH CHECK ((((created_by)::text = spellsource.get_user_id()) AND starts_with(id, spellsource.get_user_id())));


--
-- Name: cards_in_deck website_update; Type: POLICY; Schema: spellsource; Owner: admin
--

CREATE POLICY website_update ON spellsource.cards_in_deck FOR UPDATE TO website USING ((EXISTS ( SELECT decks.id,
    decks.created_by,
    decks.last_edited_by,
    decks.name,
    decks.hero_class,
    decks.trashed,
    decks.format,
    decks.deck_type,
    decks.is_premade,
    decks.permitted_to_duplicate
   FROM spellsource.decks
  WHERE ((decks.id = cards_in_deck.deck_id) AND ((decks.created_by)::text = spellsource.get_user_id()))))) WITH CHECK ((EXISTS ( SELECT decks.id,
    decks.created_by,
    decks.last_edited_by,
    decks.name,
    decks.hero_class,
    decks.trashed,
    decks.format,
    decks.deck_type,
    decks.is_premade,
    decks.permitted_to_duplicate
   FROM spellsource.decks
  WHERE ((decks.id = cards_in_deck.deck_id) AND ((decks.created_by)::text = spellsource.get_user_id())))));


--
-- Name: decks website_update; Type: POLICY; Schema: spellsource; Owner: admin
--

CREATE POLICY website_update ON spellsource.decks FOR UPDATE TO website USING (((created_by)::text = spellsource.get_user_id())) WITH CHECK ((((created_by)::text = spellsource.get_user_id()) AND ((last_edited_by)::text = spellsource.get_user_id())));


--
-- Name: cards website_view; Type: POLICY; Schema: spellsource; Owner: admin
--

CREATE POLICY website_view ON spellsource.cards FOR SELECT TO website USING ((((created_by)::text = spellsource.get_user_id()) OR is_published));


--
-- Name: cards_in_deck website_view; Type: POLICY; Schema: spellsource; Owner: admin
--

CREATE POLICY website_view ON spellsource.cards_in_deck FOR SELECT TO website USING ((EXISTS ( SELECT decks.id,
    decks.created_by,
    decks.last_edited_by,
    decks.name,
    decks.hero_class,
    decks.trashed,
    decks.format,
    decks.deck_type,
    decks.is_premade,
    decks.permitted_to_duplicate
   FROM spellsource.decks
  WHERE spellsource.can_see_deck(spellsource.get_user_id(), decks.*))));


--
-- Name: deck_shares website_view; Type: POLICY; Schema: spellsource; Owner: admin
--

CREATE POLICY website_view ON spellsource.deck_shares FOR SELECT TO website USING ((share_recipient_id = spellsource.get_user_id()));


--
-- Name: decks website_view; Type: POLICY; Schema: spellsource; Owner: admin
--

CREATE POLICY website_view ON spellsource.decks FOR SELECT TO website USING (spellsource.can_see_deck(spellsource.get_user_id(), decks.*));


--
-- Name: published_cards website_view; Type: POLICY; Schema: spellsource; Owner: admin
--

CREATE POLICY website_view ON spellsource.published_cards FOR SELECT USING (true);


--
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: admin
--

REVOKE USAGE ON SCHEMA public FROM PUBLIC;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- Name: SCHEMA spellsource; Type: ACL; Schema: -; Owner: admin
--

GRANT USAGE ON SCHEMA spellsource TO website;


--
-- Name: TABLE decks; Type: ACL; Schema: spellsource; Owner: admin
--

GRANT SELECT,INSERT,UPDATE ON TABLE spellsource.decks TO website;
GRANT SELECT ON TABLE spellsource.decks TO PUBLIC;


--
-- Name: TABLE cards; Type: ACL; Schema: spellsource; Owner: admin
--

GRANT SELECT,INSERT,UPDATE ON TABLE spellsource.cards TO website;
GRANT SELECT ON TABLE spellsource.cards TO PUBLIC;


--
-- Name: TABLE classes; Type: ACL; Schema: spellsource; Owner: admin
--

GRANT SELECT ON TABLE spellsource.classes TO website;


--
-- Name: TABLE cards_in_deck; Type: ACL; Schema: spellsource; Owner: admin
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE spellsource.cards_in_deck TO website;


--
-- Name: FUNCTION create_deck_with_cards(deck_name text, class_hero text, format_name text, card_ids text[]); Type: ACL; Schema: spellsource; Owner: admin
--

GRANT ALL ON FUNCTION spellsource.create_deck_with_cards(deck_name text, class_hero text, format_name text, card_ids text[]) TO website;


--
-- Name: TABLE generated_art; Type: ACL; Schema: spellsource; Owner: admin
--

GRANT SELECT,INSERT,UPDATE ON TABLE spellsource.generated_art TO website;


--
-- Name: FUNCTION set_cards_in_deck(deck text, card_ids text[]); Type: ACL; Schema: spellsource; Owner: admin
--

GRANT ALL ON FUNCTION spellsource.set_cards_in_deck(deck text, card_ids text[]) TO website;


--
-- Name: TABLE user_attribute; Type: ACL; Schema: keycloak; Owner: admin
--

GRANT SELECT,INSERT,REFERENCES,DELETE,TRIGGER,TRUNCATE,UPDATE ON TABLE keycloak.user_attribute TO website;


--
-- Name: TABLE collection_cards; Type: ACL; Schema: spellsource; Owner: admin
--

GRANT SELECT ON TABLE spellsource.collection_cards TO website;


--
-- Name: TABLE deck_shares; Type: ACL; Schema: spellsource; Owner: admin
--

GRANT SELECT ON TABLE spellsource.deck_shares TO website;


--
-- Name: TABLE published_cards; Type: ACL; Schema: spellsource; Owner: admin
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE spellsource.published_cards TO website;


--
-- PostgreSQL database dump complete
--

