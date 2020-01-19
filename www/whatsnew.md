---
layout: page
title: Updates
permalink: /whats-new/
---

### 0.8.64-3.0.8 (In Progress)

 - Introducing the Trader champion!
 - Bloodlord Goa now reads, Opener: Gain the aftermath, "Repeat all other friendly aftermaths."
 - Minions can now have a maximum of 16 active aftermaths.
 - Aftermaths on a minion that were not in its text are removed after they process.
 - All cards that triggered aftermaths, like Vein Burster, now correctly trigger aftermaths that were added by effects other than the target minion's text.
 - Haunted Berserker now correctly deals Toxic and Lifedrain damage (i.e. splash damage) with its effect.
 - Playing cards randomly is not recursive.
 - Bloodlord Goa now shows the source cards of cast Aftermaths. (1488)
 - Mosshorn now interacts more favorably with Revelation, and also has Guard as it was originally supposed to. ([1492](https://i.pinimg.com/originals/cf/8d/62/cf8d6265db8c31180c9fb3c458152581.jpg))
 - Auras that gave minions the same attribute now stack.
 - Elven Woundsealer, Edlritchampion and Ushibasu the Vigilante no longer interact to cause an infinite loop.
 - The Overlord's Skill Dark Rule now functions properly. (1494)
 - Fixed a typo with Hired Berserker and made it properly summon a 2/3 with Guard. (1495)
 - All for One's description now matches its actual effect. (1484)

The following changes since 0.8.63
 - Silver Timekeeper now properly filters selectable targets. (1482)
 - Brain Stun now properly says "Stunned" in its text. (1483)
 - Slave-Trader Yorn is now "Yorn th Recruiter" and Fairy Whipmaster is now "Fairy Enforcer". (1487)
 - Bloodlord Goa now properly recasts Aftermaths, even ones triggered by cards like Soulscream. (1489)
 - The 4th Ring Warden now correctly only affects your own minions. (1490)

### 0.8.63-3.0.8 (January 14, 2020)

 - New Oni Queen cards and updates. (since 0.8.62)
 - Deft Familiar now correctly upgrades your hero power.
 - Dark Rule now correctly selects from different Overlord tokens.
 - Fifi Fizzlewarp no longer interacts specially with cards that also trigger at the start of the game.
 - Zilch, God of Nothing correctly does not count permanents for its effect.

### 0.8.62-3.0.8 (January 13, 2020)

 - New Vampire Lord cards and updates. (1444)
 - Wraith has been updated as the Overlord. (1445)
 - Adding Steel Smash ("Deal damage equal to your Armor to a minion.") to Archaeologist. (1446)
 - Champion of Death costs 7, up from 6. (1447)
 - Changes to the Occultist champion: 20 cards rebalanced and the introduction of Ancient Blood and changes to Jikr, Occult One. (1450)
 - Crystal Giant now costs 16, up from 12. (1459)
 - Bonecrusher Brute now destroys Lun instead of discard cards. (1461)
 - Rowdy Fire Goblin now has 0 Attack. (1461)
 - Bluefist Trainer now costs 3. (1461)
 - Redhide Saboteur is now an Opener instead of an Aftermath. (1461)
 - Sluggish Brute is now "Dash. Has +2 Attack while damaged." (1461)
 - Waygate Commander now reads: "Your Demons have Dash." (1461)
 - General Hun'zho now affects hand as well as board. (1461)
 - Rallying Speech now costs 1. (1461)

### 0.8.61-3.0.8 (January 9, 2020)

 - Faceless Nightmare, Totemic Split, Ichor Conversion, Felstalker, Shadow Satyr, Nightmare Portal, and Nemsy, Awakened Calamity have been moved out of the Spellsource set. Eternal Time Eater is now in the Timeweaver class. (server fix #1441)
 - Fae Flyrider, Forest Get-Together, and Tall Tales are now correctly in the Spellsource format. (server fix #1460)
 - Brawler Bushi now swaps the health and attack of a minion before it deals damage with the attack, not after. (server fix #1455)
 - The friends list now renders wider. (client fix #23)
 - Scaling on Android should be improved. Please report issues for your specific device.
 - Negative attack and health values will now be clipped to zero. (server fix #404, client fix #10)
 - Secrets and quests now render on the battlefield.
 - The champion now shows when it is stunned. (server fix #368)
 - Thousand Year Hatred now destroys all minions wherever they are, not just if they are on the battlefield. (server fix #1478)
 - Yokai Fire now deals 4 damage only when a Demon is destroyed that turn. (server fix #1479)
 - The AI has been reverted to a higher performance level and should no longer heal its opponent. However, there may be another underlying issue that is causing the AI to be confused about which point of view it should play from. (server fix #1477)
 - Discards on the first turn now occur. (server fix #1470, server fix #1070)
 - Other effects that rely on the turn when an event occurred now track correctly on the first turn. This includes roasting, discarding, the turn a card was played from the hand or the deck, the last turn a player played (for the purposes of extra turns), the turn a card was put into the hand, the turn a minion was summoned, and the turn a minion died.
 - When playing Fifi, a card given Timebidding Magi's text will now reduce the cost of the card by the host card's base cost, i.e. down to 0. (server fix #1474)
 - Formless cards are now correctly labeled in the card text as being Permanents. (server fix #1467)
 - Permanents now no longer have an attack and HP value shown. (server fix #1467)
 - The spacing between minions on the battlefield has been tightened.
 - Weapons now have a default sprite shown.
 - Doodles now refreshes your skill when she comes into play. (server fix #1465)
 - Duplifairy now correctly references itself. (server fix #1458)
 - Putting cards from the deck to the hand (as opposed to drawing them from the top of the deck) now correctly increments the count of cards the player has drawn.
 - Gather in the Shadows now correctly reduces the cost of Undertide Terror. (server fix #1456)
 - Terrified Tanuki is now a 1/3, down from 1/4. (server fix #1454)
 - Insurgency Captain Krika now correctly activates every time its owner's hero attacks. (server fix #1442)
 - Bluefist Trainer, Liver Eater, and Bogovey Traitor now have their appropriate tribes. (server fix #1442)

### 0.8.60-3.0.7 (January 7, 2020)

 - Lesser Old One, Doomed Expedition and Raise the Dead are now correctly in the collection. (server fix #1475)
 - Phanton Advisor no longer has text for the purposes of Fifi Fizzlewarp. (server fix #1469, server fix #1466)
 - Changes to the way games are kept alive via "ping pong" style messages should prevent old matches from lingering and corrupting the battlefield. (server fix #1473, server fix #1464)
 - Hive Mind and other force attacks will no longer expend attacks despite not attacking if the target is already destroyed. (server fix #1472)
 - Deepsea Duel no longer accepts targets. (server fix #1471)
 - Buffeting Elemental is now an elemental. (server fix #1468)
 - Stun is now correctly used on Spellsource cards. (server fix #1443)
 - Gravekeeper Gallows no longer gives its aftermath to non-weapon cards. (server fix #1463)
 - Many statuses are now visualized on the battlefield. Tooltips will be added soon.
 - You can now attack as quickly as you can issue commands.
 - Surveyor Skag no longer puts the same card copied multiple times at the bottom of your deck. 
 - Destroying minions that go dormant during their summoning phase no longer crashes the game. For example, if Paven, Elemental of Surprise gained Miserable Conclusion (a destroy effect) on its Opener due to Alagard's Infusion and then destroys itself in the Opener, it correctly transforms into its permanent.

### 0.8.59-3.0.6.2 (December 29th, 2019)

Hotfixes to the client and server.

 - The end game screen now appears correctly. (client fix #21)
 - Commands now correctly issue on the first turn when the player starts. (client fix #22)
 - Damage indicators now properly go away and their pacing is improved overall. (client fix #13, server fix #1457)
 - Pacing has been improved, making it easier to notice card draws.
 
##### Known Issues

 - There is no power history.
 - There are no status indicators for minions on the battlefield, like Dodge.
 - The friends list incorrectly shows many duplicate entries for the same friends.
 - Sometimes restoring health is rendered as a damage effect instead.
 - Discards, roasts and draws are not visualized.
 - The draft view is no longer accessible.
 - All minions have the same temporary art.
 - All champions have the same temporary art.
 - When connected in multiple places, the latest connected client will not render the battlefield.
 - On mobile devices, the game will not rotate to landscape if portrait orientation lock is enabled.
 - The notch cutout obscures part of the end turn button on iPhone X devices when oriented landscape right.

### 0.8.58-3.0.6.1 (December 29th, 2019)

Hotfixes to the client and server.

 - Some issues with selection have been addressed.
 - Sometimes, games would linger and not end. The most common cause of this issue has been addressed.

### 0.8.57-3.0.6 (December 28th, 2019)

Improvements to the client and server.

 - Icons throughout the interface are hot-fixed and cards in your deck can be deleted again. (client fix #8)
 - When your opponent reconnects during your turn, you no longer lose the ability to take actions. (client fix #16, client fix #17, server fix #1464)
 - The turn timer is visible again and should be more reliable.
 - The cards database is now loaded in the background, preventing a common freeze at the beginning of loading. (server fix #1453)
 - Openers and discovers no longer prompt you with a panel rendered off-screen, allowing you to correctly perform an action.
 - When you complete a mulligan, your mulligan prompt correctly goes away; when your opponent completes a mulligan, you can again safely mulligan; spamming the mulligan button no longer crashes the game.

### 0.8.56-3.0.5 (December 18th, 2019)

Improvements to the client.

 - The hand now expands to the width of the screen on desktops again.
 - Better aspect ratio / zooming management on desktops.
 - Some exceptions should occur less frequently.
 - Remove unneeded assets
 - Cards can now be cancelled in the click activation event patttern.
 - On non-mobile devices, the hand tray no longer needs to open and close. Your cards will raise automatically when you hover over them.

### 0.8.56-3.0.4 (December 15th, 2019)

Android release.

##### Known Issues:
 
 - If you're upgrading from the previous version, the collection on macOS and Windows clients is missing cards.

### 0.8.56-3.0.3 (December 14th, 2019)

Maintenance iOS release.

### 0.8.56-3.0.2 (December 12th, 2019)

Fixes to the client.

##### Usability Fixes:

 - There are no longer lines between the tiles of the background grid. (client fix #7)
 - The entire battlefield is visible on mobile, and portrait mode has been enabled.
 - Tapping a targetable card to play it now works.

### 0.8.56-3.0.1 (December 11th, 2019)

Fixes to the client.

##### Usability Fixes:

 - Spells without targets are now easier to play and are easier to cancel. (client fix #6)
 - Mulligan button spam should no longer cause a crash. (client fix #5)
 - You can no longer drag non-targeted skills. (client fix #4)
 - Skills should no longer cause glitches when played. (client fix #3)
 - Double digit values are now visible on cards. (client fix #2)
 - Cards in the collection show descriptions again. (client fix #1)
 - The hand no longer obscures the skill when you are holding more than 8 cards.
 - Summoning into the rightmost position is easier.
 - Canceling a no-target spell or card is easier.
 - The opponent's play card animations are disabled for now, the played card visualization should appear much more reliably.

### 0.8.55-3.0.0 (December 10th, 2019)

The Version 3 client is now live and quite buggy.

##### Content Fixes:

 - Obvious Ambush now reads, "Destroy a friendly minion. Summon a 3/3 Worm." (1437)

### 0.8.54-2.0.38 (October 27th, 2019)

Bug fixes.

Giving yourself more than 10 Lun (but not Lun stones) during your turn now preserves that lun. This means you can use the Lunstone while you have 10 lun, and you will correctly have 11 lun. Other effects which spend all your lun will scale correctly to that 11th lun.

Using a skill now spends its lun before the skill effects are evaluating, making skills behave like cards played from the hand.

##### Content Fixes:

 - Yagan Lifetaker now correctly buffs minions randomly. (1438)
 - Flame Burp now deals the correct amount of damage, 1. (1436)
 - Livid Zealot and other text copying effects now correctly copy triggers and auras onto actors already in play. (1435)
 - Council Meeting is a spell now. (1434)
 - Dinosoul now shows distinct Beasts. (1433)
 - Bellringer Juriso now correctly triggers distinct aftermaths. (1433)
 - General Hun'zho now only transforms other friendly minions. (1432)
 - Thousand Year Hatred is now an Oni Queen card. (1431)
 - Fae-wraith Caroline is now a Wraith card. (1431)
 - Skill refreshing effects like Ghatanothoa's switching now work. (1430)
 - Obsigon, Bounty Sorcerer now has Dash. (1426)
 - Chokehold is now named correctly. (1425)
 - Fassnu Oathbreaker now has the Dragon tag. (1424)
 - Revelation should no longer break the game. (1423)
 - Tainted Sight now only draws minions. (1422)
 - Alpha Raptor now has a Beast tag. (1421)
 - Conjure Club and Conjure Kanobo now correctly buff the champion. (1420)
 - Spritely Scamp now correctly receives spells casted by the opponent during the opponent's last turn. (1419)
 - Vohkrovanis now replaces the deck and draws correctly. (1418)
 - Devour now correctly drains to the friendly champion. (1417)
 - The Reiri minion token now correctly shows how much damage is left for it to activate. (1417)

### 0.8.53-2.0.38 (October 25th, 2019)

Bug fixes, balance changes and massive content additions.

Please note that the client is currently in maintenance mode while the new build is being developed!

##### Content Additions:

 - Vohkrovanis, "Start of Game: Replace your deck with 30 random cards. Discard your hand, and draw that many cards." (1371)
 - Introducing 4 Archaeologist legendaries: Archivist Krag, Farseer Kethaan, Jerra the Deforger and Thrakul the Armorhulk. (1393)
 - Introducing the Exile class, the Oni-Queen class, and new Witch Doctor cards! (1414)
 - New Storyteller cards. (1394)
 - New Occultist cards. (1398)
 - The Vampire Lord has been reworked. (1413)
 - Bard has been reworked. (1416)
 
##### Content Fixes:

 - Imperfect Duplicate now correctly summons a minion based on the target's stats. (1404)
 - Sneaky Kaeru now returns to the hand when its health is 1. (1401)
 - Bloodmoon Ritual is now in the collection. (1399)
 - Quick Chant is now in the collection. (1400)
 - Secrets that fire missiles are now affected by spellpower. (1402)
 - Stubborn Stegodon is now a Beast. (1415)
 - Champion of Death now correctly destroys minions.
 - Catta the Merciless, Terrain Devourer, Ravenous Lookout, Broodmother Narvina, Sylas Fate's Hand, Northot Necromaster and Eternal Steed are no longer collectible due to infinite loop interactions.
 - Effects which target friendly cards now also target Skills if the effects did not already.
 - Effects which play cards randomly may now play cards of the same base card randomly. However, during the invocation of a specific card's random play effect, it will never be able to play itself randomly recursively.
 - One on One no longer causes a crash if it is randomly played in such a way to select an enemy minion when it's the only minion on the board.
 - Some effects which resurrect or discover destroyed minions now correctly only consider minions in cases that they did not before.
 - Fleeting Firebug no longer interacts with War Trebuchet to cause an infinite loop. Fleeting Firebug now summons at the end of its owner's turn.
 - Two Commander Gareths on the battlefield no longer cause an infinite loop. It now reads, "At most once."
 - Storyteller buffs and nerfs. (1394)
 - Unnatural Restoration now restores 4 Health. (1378)
 - Cryptlady Zara is no longer collectible. (1373)
 - Anobii, the Trapper is now known as Crypto, the Trapper. (1373)
 - Locust Swarm now costs 8. (1373)
 - Tiger Ant now costs 11. (1373)
 - Harden now gives +4 Health instead of +4/+4 and costs 4. (1373)
 - Reclamation now reads, "Gain 0 Armor. (Increases for each Beast in your graveyard)" (1373)
 - The Prophet now reads, "Opener: Discover and summon two minions that died this game." (1373)
 - Lil Wormy is now a 1/3 Beast. (1373)
 - Nanny now reads, "At the end of your turn, give a random minion in your hand +1/+1." (1373)
 - Dream of Kingship now reads, "Discover a Beast, a Dragon and a Fae. Summon them all." (1373)
 - Other changes to Hive Queen descriptions. (1373) 
 - Archaeologist has several reworked cards. (1385)
 - Wicked Smite is removed for a new basic minion, "Peeling Zombie." (1388)
 - Blood Golem is changed to be a 5 mana 4/5 so that it can be played more flexibly. (1388)
 - Large Spiderling is buffed to now deal 4 damage to the friendly champion instead of 5. (1388)
 - Ancient Curse is buffed to cost 3 mana instead of 4 mana,. (1388)
 - Bone Shield now only does 1 damage to a friendly minion. (1388)
 - Some neutrals have been removed. (1403)
 - Some Sands of Time neutrals have been buffed and nerfed. (1403)
 - Archivist Jerrard now looks for a deck in your collection called The Archive. (1403)
 - Rektan, Warrior of the Wild now reads, "Opener: Your champion has +1 Attack for the rest of the game." (1403)
 - Stonecold Sergeant now has 5 Health. (1407)
 - Stormwind Commander now has 4 Health. (1407)
 - Forces Unite now costs 6. (1407)

### 0.8.52-2.0.38 (September 20th, 2019)

Thanks to @Logovaz for a great contribution to Witch Doctor.

Bug fixes.

##### Content Additions:

 - Quick Chant - 1 Lun Basic Spell "Restore a friendly minion to full Health. Give it +3 Health."
 - Jungle Survivalist - 2/1/2 Common Minion "Opener: Discover a spell."
 - Cemetary Party - 4 Lun Rare Spell "Resurrect a friendly Aftermath minion. Add a copy of it to your hand."
 - Story Collector - 3/1/5 Epic Minion "Opener: If you cast a spell this turn, add two random Witch Doctor spells to your hand."

##### Content Fixes:

 - Lifedrain now does not apply if the source shares an owner with the target, the source is a spell, and the target is a hero. In other words, "Take X damage" effects no longer are influenced by Lifedrain.
 - Auto-Cannibalism can now be cast with less than 16 health. (1383)
 - Vermancer is now called Hive Queen (1397)
 - Tournament Trial can now target any minion. (1392)
 - Mollusk Meister now reads, "Opener: Give a friendly minion +8 Health. Gain Armor equal to its Health." (1391)
 - Death's Messenger is no longer collectible. (1390)
 - The Mercenary champion and class are no longer collectible. (1389)
 - Ushibasu is now a 7/4/7 from 7/5/8.
 - Split Personality, Devilry Flare, and Sliver of Silver all now cost 3 Lun from 2 Lun.
 - Shanga's Spirit Brew now costs 3 Lun from 4 Lun.
 - Chant Leader and Hexcrazed Vessel both now cost 5 Lun from 6 Lun.
 - Titanic Fanatic now costs 7 Lun from 8 Lun.
 - High Shaman Mawliki now costs 9 Lun from 10 Lun.
 - Gaitha the Protector is now a 7/4/8 from 7/4/6.
 - Entranced Dancer is now a 3/4/3 from 3/4/2.
 - Bloodseeker is now a 2/2/3 from 2/2/2.
 - Ritual Shaman is now "Opener: Play a Secret from your deck." from "Opener: If you played a spell this turn, play a Secret from your deck."
 - Legendary hero card Puppeteer Senzaku and Basic minion Spiritcaller have been removed.
 - Ptero Max has fixed text "Your Beasts gain an extra attack after attacking and killing minions." from "Your Beasts can attack again after they attack and kill a minion." No change in interaction.

### 0.8.51-2.0.38 (September 2nd, 2019)

Tribes are now visible on cards again. (1358)

### 0.8.51-2.0.37 (September 1st, 2019)

Content additions and bug fixes.

Thanks @Bromora for the contribution to Wraith!

##### Usability Fixes:

 - Deleting a deck no longer glitches out the client on non-Windows platforms. (1372)
 - Legibility of buffs has been improved, showing a darkened rectangle when a buff is applied. Buffs will be visualized differently in the new client. (1367)

##### Content Additions:

 - Wraith Verdant Dreams cards. Thanks @Bromora!

##### Content Fixes:

 - Some Fae cards have been renamed. (1362)
 - Wither now reads: "Wither (X): Whenever this hits a minion, its attack is reduced by X next time it could attack."
 - Changing skills with skills, like Ghanathoa's effects, no longer cause a draw. (1370)
 - Bioweaponize now works on minions that have not previously had a Wither effect. (1369)
 - Witherdrake's Opener now correctly applies Wither. (1368)
 - A variety of other incorrectly themed cards have been renamed and their descriptions updated. (1361)
 - Defensive Position's description is no longer missing a space. (1360)
 - Mandron now correctly specifies Skill. (1359)
 - Servant Wasp and Dark Order Knight are no longer collectible since their mechanic is deprecated. (1361)
 - Wicked Smite now deals 4 damage to both champions.
 - Recipe for Power now only gives an armor bonus. (1355)
 - Treasure Trove now counts the owner's cards correctly.
 - Terrain Devourer no longer interacts with Undying Tentacles in a way that causes a crash.
 - Plague of Flesh and other negative-HP buffs now reduce the health of wounded minions. The behaviour for aura-based buffs has not changed. (1354)
 - Spells that are not castable when the board is full, like many summoning spells, no longer incorrectly count permanents (regression).
 - Shuffling Unearthed Horrors while Xitalu is in play will now correctly shuffle in buffed cards. Additionally, other shuffle trigger effects now have synergy with Xitalu in the expected way. (1338)
 - Jae'kilden now references itself in its description correctly. (1374)

### 0.8.50-2.0.36 (August 21st, 2019)

Quality of life improvements for the client.

##### Usability Fixes:

 - It should be easier to drag your own hero for a physical attack. Be careful not to cast a spell on yourself!
 - Memory usage should be lower on web.
 - Deck lists now show the card counts again. (1352)
 - Popups are less jarring and more legible.
 - Text is more legible.
 - The appearance of lun, attack and health values is more consistent.

### 0.8.50-2.0.35 (August 20th, 2019)

Bug fixes and new cards.

##### Content Additions:

 - New cards for Otherworlder (thanks @AnterogradeNil!)
 
##### Content Fixes:

 - Flamerunner now reads "At the end of your turn, if you restored Health this turn, summon a 2/2  Elemental." (1323)
 - The Glutton now correctly eats enemy minions. (1351)
 - Effects which count minions no longer count permanents.
 - Conditions that change when evaluated from a different player's point of view work more reliably. 
 - Conditions that count the number of cards in your hand, graveyard or battlefield work more reliably.
 - Bloodshard Elemental no longer has Guard. (1349)
 - Plague of Flesh no longer gives 0-Attack minions 1 Attack. (1348) 
 - Broodmother Narvina no longer interacts with Terrain Devourer to cause an infinite loop when the board is full.
 - Pacts no longer appear as playable in the hand when a pact of the same source card is already in play. (1347)
 - Lord Henryk now deals 10 damage, costs (10) and is an 8/8. (1345)

### 0.8.49-2.0.35 (August 19th, 2019)

Bug fixes and networked performance improvements.

##### Known Issues:

 - The client will not always render damage values above the cards. It sometimes renders them behind.

##### Usability Fixes:

 - The server sends significantly less redundant data to the client.
 - The client no longer shows cost zero and a missing card type for every card in a deck (regression).

##### Content Fixes:

 - Curse of Aim no longer causes a game crash.
 - Eternal Steed and Rending Curse no longer interact to cause a crash.

### 0.8.48-2.0.35 (August 17th, 2019)

Introducing the Wraith class! Big thanks to @Bromara for the contribution.

The Spellsource community would also like to show its immense gratitude to Sam (Fermata) and @lilumani for their wonderful contributions to code, art and other important creative tasks.

Bug fixes.
New Otherworlder cards thanks to @AnterogradeNil!

##### Content Additions:

 - Many Otherworlder cards have been added to the game thanks to @AnterogradeNil!

##### Content Fixes:

 - Iron Preserver, Paleontologist, Dig Up Shovel, Excalibur, Rustblade, Sentry Orb and Translife Mirror now appear in the collection. (1319)
 - The Wraith haunting its way into Spellsource thanks to @Bromara! (1313)
 - Bloody Blow now properly marked under "BASIC" and "CUSTOM" sets. (1322) 
 - Bromediad Pup now correctly renamed to Bromeliad Pup. (1325)
 - Garbasu Monster now reads, "After this minion survives damage, gain +1/+1."
 - Catta the Merciless and Garbasu Monster no longer interact to cause an infinite loop when Garbasu faces off a 1-attack immune minion.
 - Replacing weapons no longer sometimes keeps their enchantments, like Decay, in play.
 - Fixed a bug where Hired Gunsmith was no longer visible in the collection. (1328)
 - Excalibur no longer loses durability from Decay if Rejan, Last Defender is alive. (1332)
 - Decayed minions from Master Eroder now properly decay at the end of the owner's turn rather than the end of Master Eroder's owner's turn. (1333)
 - A number of cards that trigger off of shuffling effects, such as Xitalu, now properly work. (1334)
 - Sot Mountain Excavation no longer deals damage to the player champion and instead just removes the armor.
 - Bloody Reconstruction now appears in the Collection again. (1342)

### 0.8.47-2.0.35 (August 5th, 2019)

Welcome to the Soulseeker class with a big thanks to @birb for the contribution!

Also, Archaeologist is digging is way into the game, thanks to @Pircival! 

##### Content Additions:

 - Archaeologist class added with Basic/Core cards. (1263)
 - New Soulseeker class courtesy of @birb and a new spooky keyword Soulbind: Summon one of four 0/1 Spirits with special aftermaths.

##### Content Fixes:

 - The Occultist card Seeker of Knowledge has been removed from the game.
 - Flaxen Whelp now reads "Opener: If there are at least 3 minions on the board, draw a card."  (1263)

### 0.8.46-2.0.35 (August 1st, 2019)

Hotfix for an issue related to closing games introduced with metrics we do not use.

### 0.8.45-2.0.35 (August 1st, 2019)

Bug fixes.

New Monk cards thanks to @Grand! (1290)

##### Content Fixes:

 - Many cards that before referenced Mana now refer to Lun instead. (1306)
 - Berry Hoarder now correctly draws all copies of 0-cost cards in the owner's decks instead of just one copy of each. (1307)
 - Blessed Koi Statue is no longer an Elemental. (1290)
 - Fiery Kitsune Punch renamed to Lunging Jab. (1290)
 - Fortifying Prayer renamed to Inner Peace. (1290)
 - Enchanted Tapestry cost increased from 2 to 3 and is no longer an Elemental. (1290)
 - Mark of Despair renamed to Touch of Sorrow. (1290)
 - White Tiger Statue renamed to Imbuing Fountain and health decreased from 4 to 3. (1290)
 - Disciple of Shitakiri health lowered from 7 to 6. (1290)
 - Remaining previous Verdant Dreams Monk cards have been removed. (1290)
 - Booty Bay Backup now reads "Opener: Give your other Pirates +1 Attack." (1284)
 - Malicious Magpie changed from a 1-mana 1/1 to a 2-mana 1/2. (1284)
 - Description for Soulcaller Roten now dynamically updates. (1276)
 - Dragon Caretaker now correctly can only target friendly Dragons. (1278)
 - Musician Skill now reads "Give a minion +2 health." (1275)
 - Defenders with zero attack, Catta the Merciless, and Holdover Lich no longer interact to cause an infinite recursion.
 - Stealing a Formless Rancour no longer causes a crash.
 - If you create a Deep Borer from a Chimera, it will harmlessly shuffle itself inside your deck.

##### Other Fixes:

 - Accepting friend invites works again. (1303)

### 0.8.44-2.0.35 (July 31st, 2019)

Spellpower in the hand no longer causes game-crashing issues.

### 0.8.43-2.0.35 (July 31st, 2019)

Disconnects and other forms of game interruption do not cause unusual effects in game (a regression since 0.8.41).

Additional bug fixes.

##### Content Fixes:

 - The neutral and test champions no longer appear in the draft.
 - Birdbrain is now a Witchdoctor card.
 - Distortoise now correctly does 3 damage to the owner instead of 2. (1294)
 - Prized Boar Aftermath now properly reflects the printed effect. (1293) 
 - Monolith of Doom now does not update its description while on the board and now properly reflects its effect. (1291)

### 0.8.42-2.0.35 (July 30th, 2019)

Hotfix decks missing hero classes.

### 0.8.41-2.0.35 (July 29th, 2019)

Non-Spellsource content is removed from the game.

New Storyteller cards thanks to @Skurleton!

The bot will now play more new decks in its rotation.

Many other bug fixes.

##### Content Additions:

 - New Storyteller cards. (1237)

##### Content Fixes:

 - Non-Spellsource content has been migrated to a "Do Nothing" card. (1180, 1285)
 - All Monk cards, except for Core and Basic ones, are no longer collectible in order to begin rebuilding the class from the ground up. (1238)
 - Minion Cadenza is now properly a Musician card instead of a Neutral card. (1234)
 - Strengthen Core (Honed Potion) now gives a minion deflect, draws a card and costs (2). (1245)
 - Enchanted Springs (Springs of Ebisu) now costs (0). (1245)
 - Fassnu Avenger no longer gains every deathrattle twice. (1248)
 - Trigger Happy Rebel changed from a 2/4 to a 2/3. (1251)
 - Dustbowl Vigilante changed from a 3/6 to a 3/4. (1251)
 - Outlaw's skill now deals 1 targeted damage. The upgraded skill deals 2 targeted damage. (1251)
 - Warden Saihan's ability no longer triggers off of itself taking damage. (1250)
 - Dramatic Entrance now processed before a minion's Opener instead of after. (1252)
 - Plucky Pilgrim changed from a 1/2 to a 2/1. (1260)
 - Terrified Tanooki renamed to Terrified Tanuki. (1261)
 - Some Baron cards have had their rarities changed. (1257)
 - Darkshard Duelist is now a 7/7, up from 5/5. (1257)
 - Might Mufunga now costs (9), up from (8). (1257)
 - Guild Guard now costs (5), up from (4). (1257)
 - Miserable Conclusion now correctly destroys all minions except the opponent's highest attack minion.
 - Hooded Ritualist now reads "After you cast a spell, summon a 2/1 Demon right before the end of your turn." (1264)
 - The Omega Rune now correctly no longer gives lifesteal on spell casts to both players. (1265)
 - Matriarch Aiiran no longer dynamically updates its damage number while on board. (1249)
 - Manly Mountaineer now properly has Dash. (1233)
 - Tome of Secrets now works properly. (1233)
 - Dramatic Playwright now only changes the attack of one enemy minion. (1233)

### 0.8.40-2.0.35 (July 16th, 2019)

Bug fixes for network lag.

### 0.8.39-2.0.34 (July 15th, 2019)

Some forms of network lag should be addressed in this update, because the server and client now exchange as little as 20% of the data they did previously.

### 0.8.38-2.0.33 (July 14th, 2019)

Hot fixing the removal of the bot decklists that contain old cards.

### 0.8.37-2.0.33 (July 14th, 2019)

New content and bug fixes!

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
