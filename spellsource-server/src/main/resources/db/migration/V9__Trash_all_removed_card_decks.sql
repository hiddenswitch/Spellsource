update spellsource.decks
set trashed = true
where spellsource.decks.id in (
  select spellsource.decks.id
  from spellsource.decks
         join spellsource.cards_in_deck on spellsource.decks.id = spellsource.cards_in_deck.deck_id
  group by decks.id, cards_in_deck.id
  having count(distinct (cards_in_deck.card_id)) = 1
     and cards_in_deck.card_id = 'spell_removed_card')