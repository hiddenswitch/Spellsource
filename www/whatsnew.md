---
layout: page
title: What's New
permalink: /whats-new/
---
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

### 0.8.9-2.0.21 (Tuesday, January 8th, 2019)

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