---
layout: page
title: What's New
permalink: /whats-new/
---
### 0.8.9-2.0.20 (Tuesday, January 8th, 2019)

Thanks to a contribution from @DeepKhaos (Dipsy), introducing the Blood Knight class!

And also, we get a great contribution from @AnterogradeNil: Introducing the Otherworlder class!

@Pircival contributed new Baron cards and fixes.

@Logovaz contributed new Witch Doctor cards.

@LyokoBarbossa also contributed fixes.

##### Content Additions:

 - The Blood Knight class, starting with 38 cards and tokens.
 - The Otherworlder class, starting with 34 cards and tokens.
 - The new Baron cards Pawshop Trader, Smug Smuggler, Trisword Swinger, and Vanal Petkiper.
 - The new Witch Doctor cards Aberration, Chant Leader, Fading Berserker, Raptor Rider Raider, Tainted Raven, Temple Watcher, Tiki Tokens, Wolpertinger, Zalmah, Awakening Ritual, Bottled Spirit, False Promise, Ghostly Essence, Haunted Happenings, Paranoia, Rotten Curse, Shadow Puppetry, Spirit Bind, Spiritball, Bloodshed Reaver, and Spirit Wand.

##### Content Fixes:

 - Healing in excess of the health that can actually be restored no longer incorrectly triggers Pearl Spellstones, Soup Vendor, Blackguard, Virtuous Vapor, or Azjol Visionary.
 - Cards that summon themselves on Deathrattle like Sacred Tombstone and Kil'jaeden, Deceiver no longer cause infinite loops.
 - Meat Construct no longer causes infinite loops.
 - Bronze Timekeeper now correctly triggers turn start and end enchantments that do something to their hosts.
 - Fight! now gives a friendly and random enemy minion +3/+3 (was +4 Attack) and now costs 2 (was 1).
 - One For All now gives the next minion you play this turn +2/+2 (was +3/+1).
 - Crone's Attendant is now a Beast (Instead of a Mech)
 - Ritual Dagger now correctly adds a random Voodoo spell to your hand instead of the Truesilver effect.
 - Shadow Puppetry now costs (3).
 - Divination and Spirit Bind tokens from Hero Power now give +2 Health and Restore 2 Health respectively.

### 0.8.8-2.0.20 (Friday, January 4th, 2019)

The Hidden Switch Launcher for Windows has been updated to 1.1.0 and should start on the latest updates to Windows 10 now.

Additionally, the Collection view on Windows clients now correctly shows cards again.

### 0.8.8-2.0.19 (Friday, January 4th, 2019)

Happy New Years to all Spellsource players!

The server has been migrated in this release.

###### Content Fixes:

 - Mass Hysteria now correctly causes minions to duel each other (like Duelmaster Fizzle).
 - Sugary Celebration is no longer collectible since it is a copy of Lackey Break.
 - Abomination's Might, previously mis-named Icy Talon, now has a new name.
 - Dancing Rune Weapon now draws a card.
 - The spell Rune Tap now costs (1).
 - Chains of Ice now draws a card correctly.
 - Dyn-o-Matic now correctly deals 5 damage to random targets.
 - Bwonsamdi from Witch Doctor no longer causes an infinite recursion when each player controls one.

###### Usability Fixes:

 - Attacks no longer animate twice.
 - All server communications are now compressed. This reduces your bandwidth usage by almost 10x in the typical case.
 - The server is now hosted locally in California instead of Oregon.
 - Server deployments will result in less downtime. Downtime is reduced from around 10 minutes to less than 20 seconds.
 - Patches can occur more frequently. In the previous infrastructure, patches could only be applied at most once per hour. Patches can now be applied as frequently as possible.

### 0.8.7-2.0.18 (Saturday, December 29th, 2018)

The Baron cards are now actually merged into the game! Thanks again to @Pircival for the great contribution.

### 0.8.6-2.0.18 (Saturday, December 29th, 2018)

Thanks to a great contribution from @Logovaz, we have a new Witch Doctor class!

###### Known Issues:

 - Cards like Roll and interactions with Kingsbane that put cards with buffed stats back into your hand do not correctly show the buffed stats. However, the buff itself is retained.
 - Poison Cloud, an Adventure hero power, can crash the game.
 - Immortal Prelate does not include its enchantments when it is shuffled into the deck.
 
###### Content Additions:

 - The Witch Doctor class, starting with 41 cards and tokens.

###### Content Fixes:

 - Minions with Rush and Charge can now correctly target heroes.
 - Stiches now correctly gains the attack and health of its target.
 - Shambling Horror is now 5/7, up from 5/6.
 - Howling Blast now costs (4), up from (3).
 - Corpse Explosion and Dark Ritual now both cost (2), both up from (1).
 - Harvest Soul now costs (7), up from (6).
 - Energetic Mentee now has 1 Attack.

### 0.8.5-2.0.17 (Friday, December 28th, 2018)

Thanks to a great contribution from @doombubbles and @Samswize, we have a new Lich class! Expect some flavor updates.

Thanks to a great contribution from @Pircival, there are new Baron cards!

Thanks to a great contribution from @The Invisible Man, there are updates to Summoner cards!


###### Content Additions:

 - The Lich class, starting with 34 cards and tokens.
 - The new and changed Baron cards: Backstreet Blowup, Battlerattler, Catta the Merciless, Converse Reverser, Corrupted Reaver, Delivery Dealer, Doomgunners, Doomwalker, Freelancer, General Drekthar, Kitesail Ravager, Magister Umbric, Mrrghost of Past, Murloc Fixpicker, Mystiva, Night Knight, Scarlet Inquisitor, Seven Shot Gunner, Stonecold Sergeant, Titanic Terror, Toot Hoarder, Underworld Keeper, Windup Offender, Worgen Petkeeper, Zandalari Zapper, Alternate Timeline, Army Recruits, Avenging Forces, Deepsea Duel, Double Down, Forces Unite, Immunize, One on One, Self Appoint, Swarms of Time.
 - The new Summoner card Lackey Break.
 - Unnerfed Gadgetzan Auctioneer.
 
###### Content Fixes:

 - Pint-Sized Summoner now interacts with Call to Arms correctly.
 - Abandoned Hatchling: Reworked to reflect Baby Gryphon changes.
 - Corrupt Forerunner: Invoke cost changed to 7.
 - Energetic Mentee: Reworked to increase token summoner potential in Basic/Classic.
 - Horned Jouster: Code changed to make the Beetles summon on either side.
 - Lackey Break: Just a name change due to being swapped with Savage Anger between sets.
 - Tick and Tock is now a Dragon.
 - Hive Guardian is now a Beast.
 - Tenacious Beetle is now a Beast.
 - It is now possible to play Hangover on an empty board.
 - Curvehorn Guardian is now a Dragon.
 - Oracle Trili now randomly chooses a mininion to shuffle into your deck rather than all other minions.
 - Sweet Strategy now correctly shuffles two copies of the highest cost card in your hand.
 - Scatterstorm now correctly swaps minions with their corresponding owner's deck minions.
 - Animation Surge now correctly summons minions.
 - Hagara the Stormbinder now correctly only activates if your deck only has class cards.
 - Shudderwock no longer targets itself with targeted battlecries.
 - Xuen now correctly deals 4 damage to enemies other than the one it attacked.
 - Yu Lon the Jade Serpent now correctly gains a +1/+1 buff for each Jade Serpent Statue controlled.
 - Light of Chi-Ji now only buffs the one card that is returned to zero cost.
 - Goblin Technosapper now costs 3.

### 0.8.4-2.0.16 (Saturday, December 22nd, 2018)

Choose ones now work on mobile devices.

### 0.8.4-2.0.15 (Saturday, December 22nd, 2018)

A longstanding issue where mulligans appear to bug out the game has been solved. You are also less likely to run into older cards.

###### Usability Fixes:

 - Mulligans no longer retain information from previous matches. This prevents an unrecoverable error during the mulligan phase on the server.
 - The game finds leftover cards and removes them from play after you finish or concede your current game.
