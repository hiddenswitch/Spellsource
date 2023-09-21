insert into keycloak.user_attribute (id, name, value, user_id)
select gen_random_uuid()::text,
       'showPremadeDecks',
       case
           when show_premade_decks then 'With Premade Decks'
           else 'Defaults'
           end,
       id
from spellsource.user_entity_addons;
drop table if exists spellsource.user_entity_addons cascade;