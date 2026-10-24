ALTER TABLE spellsource.cards_in_deck
    DROP CONSTRAINT IF EXISTS cards_in_deck_card_id_fkey;

CREATE OR REPLACE FUNCTION spellsource.card_change_notify_event() RETURNS trigger AS
$$
DECLARE
    payload json;
BEGIN
    payload := json_build_object(
            '__table', tg_table_name,
            'id', coalesce(new.id, old.id),
            'createdBy', coalesce(new.created_by, old.created_by)
        );
    PERFORM pg_notify('spellsource_cards_changes_v0', payload::text);
    RETURN coalesce(new, old);
END;
$$ LANGUAGE plpgsql;

ALTER TABLE spellsource.cards DISABLE TRIGGER card_changes_trigger;

DELETE FROM spellsource.published_cards
USING spellsource.cards
LEFT JOIN keycloak.user_entity ON spellsource.cards.created_by = keycloak.user_entity.id
WHERE spellsource.published_cards.succession = spellsource.cards.succession
  AND (spellsource.cards.created_by = '4286293d-f538-401f-997b-d95623d135f5'
       OR keycloak.user_entity.username = 'Spellsource'
       OR spellsource.cards.uri = 'git@github.com:hiddenswitch/Spellsource.git');

DELETE FROM spellsource.cards
USING keycloak.user_entity
WHERE spellsource.cards.created_by = keycloak.user_entity.id
  AND keycloak.user_entity.username = 'Spellsource';

DELETE FROM spellsource.cards
WHERE created_by = '4286293d-f538-401f-997b-d95623d135f5'
   OR uri = 'git@github.com:hiddenswitch/Spellsource.git';

-- Finish deferred foreign-key checks from the deletes before altering the table.
SET CONSTRAINTS ALL IMMEDIATE;

ALTER TABLE spellsource.cards ENABLE TRIGGER card_changes_trigger;
