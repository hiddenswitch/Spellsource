---
layout: page
title: History
permalink: /history/
---

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

### 0.8.4-2.0.14 (Thursday, December 20th, 2018)

Thanks to great contributions from @Beatdoof and @TheFlawlessWalrus, new Dragoon and Summoner cards have been added! @doombubbles has also authored some fixes and updates to balance changes in H.S. cards.

Also a big shout out to @bdg for a very kind Christmas gift. Spellsource will always be free, and everyone's contributions -- playing, inviting players, testing, QA, writing cards, coding, card design and other debate -- is amazingly helpful.

And finally, a shout out to @doombubbles for quietly fixing bugs and adding features in the past, like our dynamic card text system.

###### Content Additions:

 - New and changed cards for Dragoon (using their new names): Aspect Champion, Exotic Menace, Flamewarper, Frangiblesaur, Glacial Wyvern, Headhunter, Hornmender, Sapphiron (formerly Lady Deathwhisper), Mountain Whelp, Pyroblaster Zagrosz, Savage Ogreseer, Sarclet Magiserpent, Spirit of the Serpent, Tharon Ja the Serpent, Tol'Vir Vitalizer, Twilight Redbane, Draconic Insight, Exhilarate, Gobble (formerly Fissure), Garnet Spellstones, Manufacturing, Explosive Ride! (formerly Explosive Rune!), United Retaliation, Venom Breath, Hemocannon (formerly Anemic Cannon), Cleaver of Glory, Marrow Cleaver, The Dreadtalon and a few tokens.
 - New cards for Summoner: Berry Hoarder, Bogtoe Challenger, Buddybot, Champion Tangler, Clamouring Crusher, Crowdcodile, Horned Jouster, Oracle Trili, Proudjaw Lackey, Steelwing Rider, Isolate, Animation Surge, Blacksteel Shell, Scatterstorm, Sugary Celebration, Sweet Strategy and three tokens.
 - Unnerfed versions of 12/18 balanced cards.

###### Content Fixes:

 - Nerfs to H.S. cards introduced on 12/18 have been implemented.
 - Sunlance uses Overkill now.
 - Captain Hooktusk no longer gives itself Rush.
 - Big Ol' Beetle, Elementium Shell and Tweamwork have been adjusted. 

### 0.8.3-2.0.14 (Wednesday, December 19th, 2018)

This release fixes issues with starting games due to changes to tracing.

###### Context Fixes:

 - Fleeting Firebug no longer causes infinite loops when interacting with Defile.
 - Elortha no Shandra no longer causes infinite loops in some situations.

### 0.8.2-2.0.14 (Monday, December 17th, 2018)

Thanks to a great contribution from @<BK>LyokoBarbossa, new Monk cards have been added and tweaked. Test improvements have led to other minor content fixes.

###### Content Additions:

 - Grandmaster Stormstout, Artificial Life Coccoon, Aysa Cloudsinger, Enigmatic Brewmaster, Life Cocoon, Monastery Scroll-Keeper, Ox School Instructor, Red Crane Statue, Student of the Crane, Thundering Crimson Seprent, Thundering Golden Serpent, Thundering Jade Serpent, Thundering Onyx Serpent, Sapphire Serpent, Uncle Gao, White Tiger Statue, Zen-noy-o-tron, and Sleight of Hand.  

###### Content Fixes:

 - Niuzao now has Deflect.
 - Xuen, Chi-Ji and Yu'Lon have been changed.
 - Lord Jaraxxus now correctly removes himself from the battlefield after being played.
 - Corrupted Blood now shuffles copies of itself into your deck after the sequence ends. This prevents you from taking infinite damage when the last card in your deck is a Corrupted Blood, and an effect causes you to draw a card at the end of your turn.
 - Flesheating Ghoul and other death trigger minions no longer trigger from the removal of permanents.
 - Shudderwock now correctly stops casting battlecries if it is removed from the board by dealing damage to itself until death.
 - Armageddon Vanguard will trigger off at most 14 minions.

### 0.8.1-2.0.14 (Friday, December 14th, 2018)

This release includes bug fixes reported by the community.

###### Client Fixes:

 - Choose Ones like Wrath no longer immediately cast on the minion that happened to be under the cursor when the selection was made.
 - Creating a deck a second time no longer requires reselecting a format and class. The deck name is also automatically populated with a useful default.
 - The Hero Power no longer has attack and health values.
 
###### Content Fixes:

 - Sorrowstone now works correctly.
 - Mutating Boa, Rhunok the Bear and Sea Stowaway are now Baron cards instead of Neutrals.

### 0.8.0-2.0.13 (Thursday, December 13th, 2018)

This release includes bug fixes reported by the community.

Random numbers are generated differently and the interface for game actions has changed, making this a backwards-incompatible change with some AIs.

###### Usability Fixes:

 - The bot's overall reliability is improved.
 - The bot no longer has issues interacting with Shadow Visions.

### 0.7.10-2.0.13 (Wednesday, December 12th, 2018)

This release includes bug fixes reported by the community.

###### Known Issues:

 - Choose One cards do not work correctly in the client.

###### Content Fixes:

 - Soul Infusion now works.
 - I think I will take it!, the Rafaam, Supreme Thief hero power, now correctly shuffles the target minion into the caster's deck.
 - Skeletal Smith now has proper capitalization.
 - Ninjarcher now deals 2 damage instead of 1 when cards are drawn.
 - Da Undatakah now shows you which Deathrattles it gained.
 - Lesser Pearl Spellstone and other effects that trigger based on who is doing the health restoring (as opposed to what is getting its health restored) now work.
 - Dilute Soul now interacts with Scepter of Sargeras.
 - Tracking and Ancestral Plane now look at the top three cards of your deck, instead of random ones.
 - Soulwarden now copies discarded cards instead of moving them out of the graveyard.
 - Idiot Sandwich now works.

### 0.7.9-2.0.13 (Wednesday, December 12th, 2018)

This release fixes a critical issue where the bot is capable of corrupting your game.

###### Usability Fixes:

 - Quest counters now increase when playing against the bot. A great deal of other subtle issues playing against the bot should also be resolved.
 - The client now handles resolution changes more elegantly.

### 0.7.8-2.0.12 (Monday, December 10th, 2018)

This release includes bug fixes reported by the community, new Classic cards and fixes to card names.

###### Content Additions:

 - The classic cards Icicle, Tome of Intellect, Pilfer and Call of the Void have been added.

###### Usability Fixes:

 - Using a deck string that includes R.R. cards now correctly matches cards.

###### Content Fixes:

 - Prince Malchezaar no longer shuffles in legendaries that are already in your hand or deck. This also fixes an interaction with Reno Jackson.
 - Arcane Keysmith no longer discovers secrets that you already have.
 - Little Helper is now officially a battlecry minion.
 - Darius Crowley no longer survives lethal damage from its effect. Student of the Ox, which has an effect with a similar consequence, **does** survive lethal damage from its effect on purpose.
 - Shudderwock and other random card casting effects now stop casting when they are removed from play due to a transform effect, like Thrall Deathseer.
 - Ugar Frostflame, Deepholm Portal, and Fists of Fury no longer prompt the player for targets.
 - Fifi Fizzlewarp no longer writes a Permanent's text on a minion.
 - Permanents more robustly do not trigger any kind of summoning-based card text.
 - Echo of Thrall, War Trebuchet, Ship's Cannon, Spiritsinger Umbra, and Unlicensed Apothecary no longer trigger off themselves.
 - Breath of Fire now ignores spell damage when damaging the Hero.
 - Untamed Beastmaster, Rumbletusk Shaker, Haunting Visions, Shriek, and Poisoned Dagger now have correct names.

### 0.7.7-2.0.12 (Saturday, December 8th, 2018)

This release has server improvements that should reduce the number of dropped games.

Please report any issues you experience in the Discord, especially when playing against the bot.

### 0.7.6-2.0.12 (Wednesday, December 5th, 2018)

This release has various bug fixes helpfully reported by the community. It also includes some overall improvements to common game effects.

###### Content Fixes:

 - Discovers that use class weightings (most discovers) no longer show duplicates. This affects between 100-160 cards.
 - Unlicensed Apothecary now triggers for all minions being played, not just deathrattle minions as it was incorrectly coded.
 - Ancestral Plane and other cards that discover cards inside decks and do stuff with them now interacts correctly with Psychic Scream and other cards that shuffle specific targets into the deck. 
 - Paradox, a Sands of Time minion, behaves correctly, removing itself after a card has been played, rather than before.
 - Ancestral Legacy now only discovers from Minions you actually played.
 - Resurrection and graveyard counting effects now don't count minions that have been removed peacefully. This fixes reported issues with Psychic Scream and Twilight's Call, for example. Other affected cards include Slain Party, Fallen Champions hero power, Anyfin Can Happen, Swarm, Titanstrike, Truthguard, Commander Garrosh, Echo of Anduin, Inquisitor Deilana, Resurrect, Endless Army, Hadronox, Bloodreaver Guldan, the Diamond spellstones, Kazakus tokens, Onyx Bishop, N'Zoth the Curroptor, Witching Hour, Reanimate, Pay Respects, Feugen, Stalagg, and Doomcaller.
 - Leyline Manipulator now reduces minions cost that are copied by Simulacrum, or by any card-copying effect. All card copy effects now correctly mark the copies as not started in the deck or hand. This affects about 58 additional cards.
 - Card sets were specified after attribute lookups for discover and card receiving effects. This affected A New Challenger, Average Joe and the Ungoro Pack.
 - Coffin Crasher now correctly recruits from hand. Some cards also written like Coffin Crasher work now.
 - Lumina, Light of the Forest now discovers only minions and does not trigger off herself.
 
###### Other Fixes:

 - Wandering Monster now uses Misdirect's exact effect and its test is improved.

### 0.7.5-2.0.12 (Tuesday, December 4th, 2018)

Thanks to a great contribution from @Pircival, we have new Baron cards!

The following cards have been added or changed: Bog Mutant; Deadeyed Ravager; Faceless Duplicator; Fleeting Firebug; Gorthal the Ravager; Hunting Terradon; Hypnomental; Landsieged Drake; Moon Gliadiator; Mutating Boa; Ninjarcher; Prophet Wa'Ran; Refracting Golem; Rhunok the Bear; Royal Protector; Sea Stowaway; Sorceress Eka; Soulclencher; Sparkfuser; Tower Titan; Unstable Artifact; Battlefield; Birds of a Feather; Black Plague; Call for Backup; Clash; Duplimancy; Ensare; Fend Off; Loa's Blessing; Scurvy Sights; Victory Royale; Weak Point; and Spirit of the Bear.

###### Content Additions:

 - New Neutral card: Lord Cookie
 - New Baron cards.
 
###### Content Fixes:

 - Balance changes to older Baron cards.
 - Duelmaster Fizzle (and the similar, new effect written on Fend Off) now works.
 - Effects that take place after sequences are more reliable.

### 0.7.4-2.0.12 (Monday, December 3rd, 2018, 2018)

Important improvements to the server fix issues introduced in September.

###### Content Additions:

 - Deathstalker Morgl, a Shaman hero with Build-A-Beast.

###### Usability Fixes:

 - Trying to matchmake twice or more during a Spellsource session will be more reliable.
 - First connections to a newly started match should be more reliable.
 - The client now has 12 seconds to connect to a game, instead of just 10 seconds.

###### Content Fixes:

 - Cards that reveal cards but do not supply a target, like Spiteful Summoner, no longer cause the game to crash.
 - Grave Shambler now gains +1/+1 (thanks @doombubbles)
 - High Priestess Jeklik now keeps hand buffs (thanks @doombubbles)
 - Pearl Spellstone now has the right cost (thanks @doombubbles)

### 0.7.3-2.0.12 (#2 for Saturday, December 1st, 2018, 2018)

Thanks to a contribution from @Jdude and @bdg, new cards have been added to Spellsource!

###### Content Additions:

 - The Hero card Rafaam, Supreme Thief and its power, I think I will take it!
 - The "Giants" series containing Articus, the Giant; Castle Giant; Crystal Giant; Fiendish Giant; Gentleman's Giant; Holy Giant; Mossy Giant; Reckless Giant; Sand Giant; Ticking Giant; Tree Giant
 - The custom cards A Second Heart; Arcane Construct; Automedic Androne; Death's Messenger; and Golemuxx, Forest Guardian.
 - Second Heart is now a Warlock card.
 
###### Content Fixes:

 - Wartbringer can now select a target for its damage.
 - Thanks @doombubbles: Akali the Rhino now buffs the card it draws.
 - A Second Heart is now a weapon.
 - Supreme Thief now reads, "Battlecry: Replace your deck with a copy of your opponent's."

### 0.7.2-2.0.12 (#1 for Saturday, December 1st, 2018)

Server improvements should lead to even more reliable connections.

### 0.7.1-2.0.12 (Friday November 30th, 2018)

Thanks again to a contribution from @doombubbles, the remaining Rastkhan's Rumble cards have been completed.

Immortal Prelate is still under development.

###### Content Additions:

 - All Rastakhan's Rumble cards except Immortal Prelate have been added.

### 0.7.0-2.0.12 (Friday, November 30th, 2018)

Server improvements should lead to greater reliability of connections.

In the next release, signing on in a new session will disconnect the previous session. 

###### Usability Fixes:

 - Adding and removing cards from decks works again. 2.0.11 and earlier, like the iOS and Android builds, still have this issue. Your patience for a new build, which requires a review process from Apple, is appreciated.

###### Content Fixes:

 - Rotbutcher Gordo and about a dozen other custom cards are no longer appear incorrectly in the Wild filter in your Collection view.
 - Hopesteed is collectible.

### 0.6.6-2.0.11 (Wednesay, November 28th, 2018)

Thanks to a great contribution from @doombubbles, new custom cards and a significant number of Rastakhan's Rumble cards have been added to the game.

###### Content Additions:

 - The custom cards Owlkin Hatchling, Overclock, Bittertide Crab, Bronze Timekeeper, Dr. Doom, Finley the Explorer, Jastor Gallywyx, Miniature Gunner, Southsea Giant, Trophy Huntress, Whizbang the Plunderful, T'Paartos the Lightforged, and the much loathed Hopesteed.
 - 97 Rastakhan's Rumble cards. The remainder will be released soon.
 
###### Content Fixes:

 - King Togwaggle now puts Ransom in the opponent's hand.
 - Elortha no Shanda now does not cause infinite deathrattle loops.
 - Dinosaur Chops is now uncollectible.
 - Thunderfury now is immune and does not reduce durability while you are overloaded.
 - Shudderwock now triggers Hagatha the Witch and other hero battlecries.

### 0.6.5-2.0.11 (Tuesday, November 27th, 2018)

Thanks to a great contribution from @tehgdogg, replay functionality has been added for debugging and development purposes. Your games will be recorded and used to fix bugs when they occur. Currently, it is not possible to retrieve a replay from the client. Please reach out on Discord if you want a replay of a specific game.

###### Usability Fixes:

 - The Stats button (the eyeball iconed button in the battlefield) now renders the deck and hand sizes correctly.
 - May have fixed some crashes related to missing null checks on weapon loading.

###### Known Issues:

 - Some situations with drawing cards from the deck can cause the game to hang. If this happens to you, please note what you were doing and report it in the Discord.
 - Repeatedly playing King Togwaggle will cause the game to hang.

For an ongoing list of issues, see https://github.com/hiddenswitch/Spellsource-Server/issues

### 0.6.4-2.0.10 (Monday, November 26th, 2018)

Overall server performance has been improved by better using computer resources.

Some connectivity issues ought to be improved. Please report "Networking" errors in the Discord.

###### Content Fixes:

 - Chromie now draws cards instead of moving them directly from the deck. Thus, Chromie's swap counts as a true draw.
 - Lord Stormsong's deathrattle is less buggy.
 - Purrfect Tracker and other filtered deck draw effects interact with "Cast this when drawn" effects should no longer cause crashes.
 - Bright Eyed Scout and other card cost modification effects that occur on draw should no longer cause crashes.
 - Totemic Slam now interacts with Fandral Staghelm, for fun.

### 0.6.3-2.0.9 (Saturday, November 24th, 2018):

###### Content Fixes:

 - Reckless Flurry now works. Previously, it did not deduct armor.
 - Giant Barbecue now deals damage to the target of the spell.
 - Crackling Arrows now works when the player has no secrets.
 - Little Helper now interacts with Vashj's Plot correctly.
 - Lady Vashj now puts a Serpent Spine Bow into play.
 - Elaborate Scheme no longer triggers Gloat if Scheme puts Gloat onto the battlefield with Scheme's effect.
 - Ravenous Lasher now triggers.
 - Sorrowstone now triggers when more than three minions die at a time.
 - Conjured Assistance only triggers if it can actually pull anything from your hand.

###### Usability Fixes:

 - Milling and discarding cards now appear in the Power History.
 - Any triggers whose side effects cause a card to be revealed will appear in the power history. For example, Dragonling Pet ("Start of Game: If your deck has no tribal minions except Dragons, draw this card from your deck.") will now appear in the power history at the start of the game.
 - UI elements overlap less.
 - The number of power history elements has been reduced to 8 from 10.
 - Power history from a previous match is no longer present.
 - The Edit button in the Quick Play / Matchmaking views is now a pencil icon button.
 - The enemy hero and minions will correctly get highlighted when you begin a physical attack. Previously, the highlighting only appeared once you hovered over a valid target.
 - The enemy hero can be targeted by battlecries on your second and later matches.