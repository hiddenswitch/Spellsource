---
layout: page
title: What's New
permalink: /whats-new/
---

### 0.8.36-2.0.31 (In Progress)

Bug fixes.

##### Context Fixes:

 - When you have a Lifesteal weapon equipped and get dealt fatigue damage, your champion no longer restores health.
 - Teaching a card a return-to-hand spell and playing that card twice no longer causes a crash. Other cards with card creation effects no longer have unusual side effects related to returning minions to hand.
 - Corrupted Blood, Hallazeal the Ascended and Soup Vendor now correctly kill you without causing an infinite loop.
 - Beetle Bash is no longer targeted and now properly gives 4 armor instead of 5. (1212)
 - Thanks to contributions from @Grand, Monk has been updated. (1219)
 
### 0.8.35-2.0.31 (July 1st, 2019)

Fixing issues with cards not being found or detected after their IDs were changed.

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

### 0.8.33-2.0.31 (Tuesday, June 18th, 2019)

Bug fixes.

Visit the new Windows development guide at [http://playspellsource.com/windows-development/](http://playspellsource.com/windows-development/) for more about editing and authoring your own cards using the tools the developers use.

##### Usability Fixes:

 - Secrets and quests appear in the client again. (1029)
 - Weapons now disappear when they are destroyed. (910)
 - Challenges to play a match or make a friend invite now correctly pop up. 

##### Content Additions:

 - 14 new cards in the Dragoon class. (1118, 1172)

##### Content Fixes:

 - Oppressor Defender now costs 6 Mana, a 2/2 minion. (1112)
 - Unnerving Spectre now costs 4 Mana, a 2/2 minion. (1112)
 - Abholos now correctly destroys only friendly minions and summons another Abholos. (1133)
 - Hypnotic Beetle now costs 4 mana. (1114)
 - Sharper Claws (Upgraded Skill) now renamed to Hone Claws. (1118)
 - Both Hero Powers no longer restore Health now. (1118)
 - Majestic Fennec is now a 4/6 Beast that restores 4 Health. (1118)
 - Alder, Death Baron's Skill deals only 3 damage now. (1118)
 - Venom Breath now costs 11 Mana. (1118)
 - Conflagration now only deals 3 damage to a minion and the minions next to it. (1118)

### 0.8.32-2.0.30 (Wednesday, June 5th, 2019)

Bug fixes.

##### Content Fixes:

 - Klive Icetooth now correctly plays its Opener. (1124)
 - The End now applies to both player's hands and the stats effect applies before the Opener is evaluated. (1126)
 - Uccian Hydra now correctly triggers. (1125)
 - Yokai Bonder now reads, "Dash. Whenever this attacks, draw a Beast from your deck." (1123)
 - Bogovanis now puts the Sourcestone into the opponent's deck. (1120)

### 0.8.31-2.0.30 (Tuesday, May 28th, 2019)

Updates from @Logovaz for Witch Doctor.

New cards from @Muhframos for Occultist too!

##### Content Additions:

 - Significant additions to Occultist. (1057)

##### Content Fixes:

 - Changes to Witch Doctor and its Verdant Dreams set cards. (1113)
 - Hatches the Dragon is no longer collectible. (1094)
 - Changes to Aegwynn, Dragonling Pet and The End Time to make them more balanced. (1094)
 - Nature Rager now gains +1 Health instead of +1/+1. (1077)
 - Ending the turn with an Infinite Warden on the board without an End Time on the board no longer crashes the game.

### 0.8.30-2.0.30 (Monday, May 27th, 2019)

Introducing the Spellsource Legendaries!

Bug fixes.

##### Content Additions:

 - New Spellsource Legendaries. (1074)

##### Content Fixes:

 - Daring Duelist now only draws a card when it survives damage. (1111)
 - Icecrown Lich, Assimilator and Scarlet Thirst on the board no longer produces an infinite loop.
 - Copying minions that have jailed other minions no longer causes errors.

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
