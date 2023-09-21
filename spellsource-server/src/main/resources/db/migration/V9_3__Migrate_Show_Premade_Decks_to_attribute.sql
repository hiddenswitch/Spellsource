insert into keycloak.user_attribute (id, name, value, user_id)
select uuid_generate_v4(),
       'showPremadeDecks',
       CASE
           WHEN show_premade_decks THEN 'With Premade Decks'
           ELSE 'Defaults'
           END,
       id
from spellsource.user_entity_addons;
drop table spellsource.user_entity_addons;