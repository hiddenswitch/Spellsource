alter default privileges revoke execute on functions from public;
revoke execute on all functions in schema spellsource from public;

revoke all on all tables in schema spellsource from public;

grant select on spellsource.decks to public;
grant select on spellsource.cards to public;

grant execute on function spellsource.can_see_deck(text, spellsource.decks) to public;
grant execute on function spellsource.get_user_id() to public;
grant execute on function spellsource.get_classes() to public;
grant execute on function spellsource.get_collection_cards() to public;
grant execute on function spellsource.card_message(spellsource.cards, spellsource.classes) to public;



alter default privileges revoke execute on functions from website;
revoke execute on all functions in schema spellsource from website;
revoke all on all tables in schema spellsource from website;

grant execute on function spellsource.can_see_deck(text, spellsource.decks) to website;
grant execute on function spellsource.get_user_id() to website;
grant execute on function spellsource.get_classes() to website;
grant execute on function spellsource.get_collection_cards() to website;
grant execute on function spellsource.card_message(spellsource.cards, spellsource.classes) to website;

grant execute on function spellsource.publish_card(text) to website;
grant execute on function spellsource.save_card(text, jsonb, jsonb) to website;
grant execute on function spellsource.set_cards_in_deck(text, text[]) to website;
grant execute on function spellsource.create_deck_with_cards(text, text, text, text[]) to website;
grant execute on function spellsource.save_generated_art(text, text[], jsonb) to website;
grant execute on function spellsource.archive_card(text) to website;
grant execute on function spellsource.get_latest_card(text, bool) to website;




grant select, insert, update on spellsource.cards to website;
grant select, insert, update on spellsource.decks to website;
grant select on spellsource.deck_shares to website;
grant select, update, insert, delete on spellsource.cards_in_deck to website;

grant select, insert, update, delete on spellsource.published_cards to website;

grant select on spellsource.classes to website;
grant select on spellsource.collection_cards to website;

grant select, update, insert on spellsource.generated_art to website;