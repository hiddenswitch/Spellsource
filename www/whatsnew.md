---
layout: page
title: What's New
permalink: /whats-new/
---
### 0.8.15-2.0.24 (Friday, January 25th, 2019)

Important usability fixes for the WebGL build.

##### Usability Fixes:

 - Matchmaking now works correctly when the applications do not have focus, in all builds. This was a regression.
 - Draft retire button now enabled at the right times.
 - Copying and pasting deck lists are supported, in a primitive way, in the WebGL build.
 - Battlecry power history entries have the correct text now.
 - Hovering over cards in the card side panel while browsing your collection will correctly show the card.
 - Bots now play a rotation of custom cards decks.
 - In your Quick Play view, you can turn off standard decks by hitting the Stack of Books icon toggle.
 - In the future, standard decks will be updated as more are added to the collection.

##### Content Fixes:

 - Bone Whelp now correctly only returns itself to hand when there are other dragons under your control.
 - Boss Harambo now correctly adds Bananas, instead of Coins, to your hand.
 - Roasting cards now appears in the power history.
 - Playing Play Dead on Spawn of N'Zoth now correctly buffs it.

### 0.8.14-2.0.23 (Tuesday, January 22nd, 2019)

Bug fixes and draft improvements.

##### Usability Fixes:

 - Draft mode now allows you to retire at any time, choose any hero, and only serves community cards.

##### Content Additions:

 - The Dragoon cards Runt Leader and Cult Reverend.
 
##### Content Fixes:

 - Wither now hits shields, like Divine Shield and Deflect. If Wither is blocked by the shield, the Wither effect does not occur and the shield is lost. When Wither hits a Deflect, the hero takes damage equal to the Wither amount instead.
 - Night Knight and Underworld Keeper are now correctly Baron cards.
 - Kahl of the Deep and Cult Promoter are not correctly Occultist cards.
 - Acherus Deathgate now correctly draws a card from your deck.
 - Lich's Phylactery is now called Phylactery and costs 4.
 - Poison Cloud no longer crashes the game.
 - The Rafaam Staff Pieces have better names now.

### 0.8.13-2.0.22 (Sunday, January 20th, 2019)

Thanks to a contribution from @Pircival, @Walrus, @Logovaz and @Muhframos, introducing a Spellsource Basic set.

Thanks to a contribution from @Muhframos, introducing the Occultist class.

And thanks to a contribution from @Rumu11, we have more Verdant Dreams cards.

Use the **Spellsource** format to use the new basic set and only custom classes and cards.

Additional bug fixes.

##### Content Additions:

 - Additional Verdant Dreams expansion cards.
 - A Spellsource Basic set, with 55 neutral minions.
 - The new Occultist class, with 67 cards and tokens.
 - The Basic Spellsource set.

##### Content Fixes:

 - Teamwork now costs 7 mana.
 - Elementium Shell now costs 3 mana, does not draw, and only buffs health.
 - Maexnna's Femur now costs 8 mana and has 2 durability.
 - Swarm Shield now costs 2 mana and gives a multiplier of 3 armor.
 - Breath of Fire now reads, "Deal $1 damage to all enemy minions. Then deal $[] damage to the enemy hero. (Increases for each enemy minion)," which means all its damage effects gain spell power.
 - Wild Pyromancer, Flamewaker and other spell casted triggering effects no longer trigger off Scroll of Wonders, Tess Greymane or other force casts that reuse cards from the graveyard.
 - Immortal Prelate is now available for play.
 - Magic Keg now correctly puts Chef spells into your hand.
 - The Sands of Time Rifts (permanents) now show the number of turns left until they are removed. Their rules are simplified.
 - Sherazin, Seed now shows the number of additional cards you need to play to revive it.
 - Gobble is now 2 Mana and reads "Destroy the lowest Attack enemy minion on the battlefield."
 - Rheastrasza is now 10 Mana.
 - Blade of Eventide now reads "Deathrattle: If your deck has only even-Cost cards, destroy the lowest Attack enemy minion."
 - Molten Whelp is now replaced with Vermillion Glider. 

### 0.8.12-2.0.22 (Saturday, January 19th, 2019)

Bug fixes and Verdant Dreams.

##### Content Additions:

 - @Rumu's Verdant Dreams expansion.
 - The Paladin card Echo! Echo! Echo!.

##### Content Fixes:

 - Skulking Geist now only destroys cost 1 spells instead of cost 1 cards.
 - Dueling effects like Mass Hysteria and Duelmaster Fizzle no longer consume attacks. This includes situations where an attack may trigger a secret that prevents the attack from finishing.
 - Tainted Raven now interacts with silence correctly.
 - Vanal Petkiper now creates 1 buff when targeting Nightmare Amalgam instead of 7. 
 - Sideline Coach now buffs before the minion attacks.
 - Nebulous Entity now deals 2 damage instead of giving a -2 HP buff.
 - Grandmother Vratta now has 7 HP (was 10).
 - Distort's attack buff is no longer affected by spell damage.
 - Spirit of the Dragonhawk now interacts with Ice Walker correctly.
 - Chi-Ji now restores a flat 3 Health when triggered.
 - Yu'lon is now a 8/6 and only summons two statues.
 - Xuen now only has Charge and Deflect.
 - Soothing Mists now heals 3 (was 5).
 - Assimilator and Broodmother Narvina both in play no longer causes an infinite loop.

### 0.8.11-2.0.22 (Friday, January 11th, 2019)

Bug fixes.

##### Content Fixes:

 - Wither no longer affects Heroes.
 - Primordial Supremacy now buffs Titans, Xenodrones OR Wither minions, rather than Titans and Xenodrones WITH Wither.
 - Cards that rely on player statistics like number of minions summoned (Castle Giant, Thing From Below) now correctly show their costs to the client.
 - Cards that receive HP hand buffs now correctly show their HP in the client. For example, casting Roll on a minion should now show the correct stats in the client.
 - Distort now reads "Give a minion +3 Attack. Deal 3 damage to it. (Improved by Spell Damage)."
 - Card cost modifiers are no longer duplicated twice when gained via a minion transform-copy effect. (Fixes interactions between Faceless Manipulator, Prince Taldaram and cards like Radiant Elemental).
 - Otherworlder's Vicious Progeny upgraded hero power now correctly summons a 0/2 Xenodrone.
 - Mind Harvester only draws cards at the end of its owner's turn.
 - Berserk now causes the targeted minion to attack all others.

### 0.8.10-2.0.22 (Wednesday, January 9th, 2019)

Thanks to a contribution from @Samswize, we have new Lich cards!

Thanks to a contribution from @DeepKhaos (@Dipsy), we have new Blood Knight Cards!

Additionally, the client has been upgraded for better performance.

##### Content Additions:

 - The new Lich cards Lord Marrowgar, Scrap Scavenger, Blood Boil, Chains of Ice, Path Frost, and Ebonchill.
 - The new Blood Knight cards Scarlet Thirst, Veindrake, Smoldering Husk, Assimilator, Goblin Harpooner, The Blood Engine, Blood to Iron, Siphoning Strike, Sweet Sustenance, and Voltsaber.
 
##### Content Fixes:

 - Rendering Curse no longer gives minions marked for death its deathrattle.
 - Blood Moon Rising and Scarlet Thirst in play with an empty deck no longer interact to cause an infinite loop due to fatigue.
 - The Blood Knight hero powers now summon a Xenodrone after the buff.
 - Rotten Curse no longer gives Immune to your hero.
 - Overdrive now only affects friendly minions.
 - Heartpiercer now only drains from minions it hit.