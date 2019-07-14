---
layout: page
title: What's New
permalink: /whats-new/
---

### 0.8.37-2.0.32 (In Progress)

Bug fixes.

##### Content Additions:

 - Introducing Storyteller's Basic and Classic cards, with a big thanks to @Skurleton! (1221)
 - Introducing Musician, with a big thanks to @Samswize! (1210, 1218)
 - Introducing Outlaw, with a big thanks to @JDude60! His keyword, Quick Draw, means: Whenever a card is drawn...

##### Content Fixes:

 - Roll and Immortal Prelate no longer crash shuffling minions that have gained Auras into the hand or deck. (1124)
 - Rafaam, Archivist can now discover cards from other classes that are in 'The Supreme Archive'. (1225)
 - Warden Saihan now correctly labeled as a Monk Legendary. (1227)
 - Effects which remove minions from the board no longer cause crashes when other summon triggers attempt to remove those minions.
 - Catta the Merciless no longer causes an infinite loop with minions that deal zero damage.
 - Spike, Pet Whelp's ability now triggers if the owner's deck only contains dragons instead of whether it contains exactly 30 dragons. (1220)
 - Icecream Lich and the Technician are now removed from your collections.

### 0.8.36-2.0.32 (July 6th, 2019)

Bug fixes and new Verdant Dreams cards.

For Python users, the build is now compatible with Java 11.

##### Content Additions:

- Thanks to another contribution from @Grand, more Verdant Dreams cards have been introduced! (1099)

##### Context Fixes:

 - Thanks to contributions from @Grand, Monk has been updated. (1219)
 - When you have a Lifesteal weapon equipped and get dealt fatigue damage, your champion no longer restores health.
 - Teaching a card a return-to-hand spell and playing that card twice no longer causes a crash. Other cards with card creation effects no longer have unusual side effects related to returning minions to hand.
 - Corrupted Blood, Hallazeal the Ascended and Soup Vendor now correctly kill you without causing an infinite loop.
 - Beetle Bash is no longer targeted and now properly gives 4 armor instead of 5. (1212)
 - Thitazov now no longer buffs minions that have died. (1027)
 - Shigaraki Elder now correctly labeled as a common card. (1211)
 - Silverbone Claw now buffs a dragon in hand even if it is the only dragon in the player's hand. (1208)
 - Living Mana no longer puts you into negative mana. (1026)
 - Mollusk Meister now has the correct effects. (1115)

### 0.8.35-2.0.31 (July 1st, 2019)

Fixing issues with cards not being found or detected after their IDs were changed.

Spike, Pet Whelp's ability now triggers if the owner's deck only contains dragons instead of whether it contains exactly 30 dragons. (1220)

### 0.8.34-2.0.31 (June 30th, 2019)

Bug fixes and content additions.

The 0.8 series will be the last to contain content external from Spellsource.

##### Content Additions:

 - Thanks to contributions from @Grand, there are now Monk class updates! (1189)
 - Thanks to contributions from @Logovaz, there are now Senzaku updates! (1197)
 - Thanks to contributions from @Muhframos, Occultist updates! (1198)

##### Content Fixes:

 - Yig's Mastermind and other cards that interact with deck cards would incorrectly cast pre-transformation versions of those cards. It no longer crashes the game.
 - Mari Anette and Irena, Dragon Knight no longer interact in a way that crashes the game.
 - Fairy Fixpicker no longer causes crashes when it attempts to replace cards that are immediately roasted or discarded.
 - Failed Experiment now has the correct stats. (1128)
 - Firegate Commander now provides a Dash aura instead of a Blitz aura. (1131)
 - Kahl of the Deep is now a 3/5, cost 5, that draws 3 cards for the opponent instead of 8. (1182)
 - Yig's Mastermind cost increased from 8 to 10. (1184)
 - Ahn'quiraj Portal renamed to Ancient Waygate and reads "Deal 2 damage to all minions. Summon a random 2-Cost minion." (1185)
 - Stealing a card when a deck is full no longer causes a crash.
 - Wavering Diabolist correctly indicates that it does not have Guard. (1203)
 - Timewalker Strider now reads "After this minion attacks, it gains +2 Attack." (1201)
 - Energetic Mentee now specifies that it's first abilitiy is an opener. (1202)
 - Fireguard Bulwark now properly has Guard. (1200)

### Basic Rules

This is a summary of the basic rules of Spellsource.

#### Gameplay

Players combat each other using cards in a one-versus-one matchup until one of the player's champions is destroyed.

Each player starts the match by choosing which cards to discard from their initial hand (the Mulligan). Then, one of the two players takes his first turn, while the other receives a bonus Mana card called the coin.

At the start of each turn, players gain 1 mana, up to 10. This mana is spent playing cards.

Some cards can put minions on the board. Minions have an attack value (the lower left number) and a health (the lower right number). At the start of the next turn, minions can attack opposing minions or the opposing hero, dealing and taking damage. Minions whose health goes below zero are destroyed.

Some minions and spells accept targets when they are played. For a minion, this is typically part of an "Opener," or action played from the hand.

Text comes "into play" as soon as both players see it. This is typically on the battlefield, or the two rows (yours and your opponent's) of minions, the champions, the weapons they have equipped, and the Skill.

The weapon is a bonus item that grants your champion the ability to attack.

The Skill is a spell that is playable once per turn specific to your champion.

#### Keywords:

 - Aftermath: Occurs whenever a minion dies (horribly) on the battlefield.
 - Armor: Instead of losing health, lose this instead.
 - Blitz: This can attack enemies even if it is exhausted.
 - Champion: Your player character. When this dies, you lose the game.
 - Dash: This can attack enemy minions even if it is exhausted.
 - Deflect: The first time this minion takes damage, deal it to its Champion instead.
 - Dodge: The first time this minion takes damage, it takes zero instead.
 - Drain (X): Deals this much damage to the target, then heals the source by this amount. Any excess is given as extra health instead.
 - Elusive: Cannot be targeted by spells or skills.
 - Enchantment: Occurs as long as the target this is attached to is in play.
 - Exhausted: During the turn it comes into play or changes sides, this minion is exhausted and cannot attack this turn. It loses exhaustion at the start of next turn.
 - Extra Strike: A character can attack twice.
 - Guard: This must be destroyed before minions can attack its champion.
 - Hidden: This minion cannot be targeted by enemies until it deals damage.
 - Litedrain: Restores health for each damage dealt.
 - Opener: Occurs whenever the card is played from the hand.
 - Quad Strike: A character can attack four times.
 - Quick Draw: This happens whenever a card is drawn.
 - Reservoir (X): Occurs when your deck is larger than this amount.
 - Roast: Remove a card from the top of your deck.
 - Sacrifice: Occurs whenever a friendly minion dies.
 - Silence: The text on this card and any enchantments added later are no longer in play.
 - Skill: A spell your Champion can play every turn.
 - Spellpower (X): Your Spells deal this much more damage.
 - Stunned: A character that cannot attack this turn and the next.
 - Supremacy: Occurs whenever this minion attacks and kills another.
 - Surge: Occurs when a card is played from the hand on the same turn it was added to the hand.
 - Toxic: This kills minions it damages.
 - Wither (X): This reduces its target's health by this amount, and restores the health at the start of the caster's turn.
