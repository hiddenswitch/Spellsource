---
title: "Whats New"
date: "2020-02-01"
path: "/whats-new"
header: true
---

### Upcoming Changes
 - Cards shuffled by Gold Rush now properly cost (0) lun. (1683) 

### 0.8.72-3.1.2 (April 7, 2020)

 - The full screen resolution is set to the display's desktop resolution, fixing odd rendering issues on Windows devices. (1644)
 - Your playable cards and units no longer flicker a green outline at the end of your turn in Quick Play and other situations where your opponent completes their turn very quickly. (1557)
 - Sourceborn Aelin's Creation spell had an unintended bug where it would copy 1 card three times when intended behavior is copy 3 cards 1 time. Thanks @Logovazz!
 - Artifact generator cards, thanks @DeepKhaos!
 - Spirit Saber now always shows you the exact card that had the aftermath when it triggered, even if it was transformed later. (1657)
 - Archaeologist's Taletellers now correctly looks for minions played this turn, not minions that were played and killed this turn. (1652)
 - The Monk class has been retired. Its class and cards have been made uncollectible by default. "So long and thanks for all deflect." (1635, 1636)
 - Storyteller's Overflowing Energy now correctly only targets one instance of the lowest-cost card in your hand. (1642)
 - Welcome Skoppu, the Shovelbot.
 - New Outlaw cards, thanks @DeepKhaos!
 - Quick Draw (X): Whenever your cards draw a card...
 - Exile has been renamed Rebel, and more changes, thanks @Logovazz!
 - Improvements to language, thanks @DeepKhaos!
 - New updates to Skyward, thanks @Nix!
 - New updates to Archaeologist, thanks @Pircival!
 - Xii'da the Curious now only affects cards whose base cost is odd (previously in-hand or modified cost was used).
 - New updates to Otherworlder, thanks @AnterogradeNil!
 - Allied indicates your friendly targetable actors like the minions and Champion.
 - New updates to Hive Queen, thanks @Phyley!
 - Updates to Occultist, thanks @Muhframos!
 - Effects that read "Both players" and interact with targets now correctly resolve those targets from the correct point of view.
 - Draconic Doubler is now correctly an Oni-Queen card. (1638)
 - Obsigon, Bounty Sorcerer has been renamed to Battlemage Zhou (1634)
 - Pacts played by Debt Collector are now correctly displayed. (1615)
 - Overlord's Destroy the Strong now properly doesn't damage Underlings, and multiple cards should now better recognize Captives with Guard. (1614)
 - Overlord's The Oathbreaker now correctly reduces the cost of only the next Pact you play. (1613)
 - Archaeologist's Dynoblow no longer hits already-dead minions. (1612)
 - Frontline Coward is now correctly an Overlord card. (1671)
 - Oni Queen's Enenra now actually gains its Stealth. (1670)
 
#### Notes from Logovazz about Rebel and Oni Queen

**Oni Queen Update**
 - Cloak and Dagger now reads "Give your Hidden minions Toxic."
 - Gather in the Shadows is now 1 Lun.
 - Bullhorn is now 3 Lun.
 - Nurse Wounds is now 5 Lun "Give all damaged allies Hidden. Draw a card for each."

**Rebel Update**
Exile has been renamed to Rebel.

**Card Changes**
 - Back Fist is now 4 Lun "Deal 8 damage to a minion."
 - Intimidate is now 2 Lun from 3 Lun.
 - Rules of Nature is no longer collectible.
 - Armored Scoundrel is now a 4/4/5 from 4/4/4.
 - Watchful Bruisner now reads "Can only attack if another minion attacked this turn."
 - Scavenge is now 2 Lun from 3 Lun.
 - Jeering Bodyguard is now 5/5/8 from 5/6/6. Gained Dragon tag.
 - Frontline Cutthroat is now a Demon.
 - Fassnu Oathbreaker is now 4 Lun from 5 Lun.
 - Ragged Phalanx now reads "Give your minions +1/+2 and Guard. They can't attack."
 - Dreamscape Scythe is now 5/3/3 "After your champion attacks, give a random allied Dragon and Demon +3 Attack."
 - Warmage Zhou now reads "Dash. Opener: Deal 2 damage to a random enemy. Repeat for each ally that died this turn. (0 Times!)"
 - Reserve Corps is now 3 Lun "Draw a Demon and Dragon."
 - Amalgam of War now reads "Dash. Opener: Gain +2 Attack if you have a Demon. Repeat with +2 Health for Dragons."

**New Cards**
 - Warrior's Death: 1 Lun "Destroy a damaged minion."
 - From the Burrows: 4 Lun "Summon four 1/1 Rioters with Dash."
 - Vasz the Sellsword: 7/5/5 Legendary Dragon "Opener: Deal 1 damage to all other minions. Repeat until one dies."
 - General Yokura: 7/3/6 Legendary Demon "Guard. Opener: Gain +1/+1 for each ally that died this turn. Then, attack the enemy champion."

**Skyward**
 - Updated Class Color to be less similar to Neutral's color. 

#### Notes from DeepKhaos about Language Improvements

 - Discover has been renamed to Source.
 - Effects that state "Add X to your hand" are now reworded as "Receive X".
 - Unnecessary words have been culled from "this" on certain cards ("Whenever this minion", etc). Cards that still need the clarification, eg "Deal damage to X equal to this minion's Attack", have kept it.
 - Some cards that talk about turns have been given more clarity. "Turn" now specifies one player's time to act until they hit the End Turn button, and "round" specifies one cycle of this. For example: "Give a minion +2 attack this turn" provides Attack until you hit End Turn, but "Give a minion +2 attack for a round" will last until your opponent does.
 - A few cards and effects that are random, but don't say so, have had their randomness specified. As a result, since (hopefully) all the random effects in the game are now actually labelled as random, a few cards have had their wording changed so that choice is implied when randomness isn't. "Discard a card" now means choosing a card from hand and discarding it, for example.
 - "At the start/end of your turn" has been changed to "at the start/end of your turns" because current wording suggests a singular occurrence.
 - Cards that provide an aura effect for a turn that activates each time the player does something now all read "each time you do X this turn" for consistency (some used to say "whenever").
 - Cleared up some wording confusions with "whenever" and "after". Very few actual mechanical changes here
 - Just making sure that, based on their code, effects that are intended to activate whenever or before something happens correctly read "whenever", and cards that are intended to activate after the event has concluded correctly read "after". Also swapped some `PhysicalAttackTriggers` to `TargetAcquisitionTriggers` to make sure "whenever" triggers for minions attacking all actually go off before the attack.
 - Cards now all consistently use ", instead of some using quotation marks and some using apostrophes.
 - Skill, Cost and Lun are now correctly capitalised across all cards. Champion is now correctly not capitalised across all cards.
 - Also fixed some general grammatical issues I stumbled across, like missing full stops.
 
### 0.8.71-3.1.1 (March 28, 2020)

 - Both versions of Doodles now work. (1632)
 - Cards will not be played randomly more than once per reading of the text "play cards randomly." Concretely, a card is marked as being played randomly and is not eligible to be played randomly again, until all the cards in the selected list of cards are done being played randomly. Fixes issues related to Titan Feast playing Titan Feast in the deck.
 - Fixed a complex interation with Okanaka the Lifebender, Midsummer Mirage and Gravtisk the Ancient. (1606)
 - Fassnu Avenger now properly copies copied Aftermaths. (1569)
 - Fixed a few miscellaneous card descriptions to properly say "Opener". (1590)
 - Development card "Signature Chooser" is now correctly uncollectible. (1592)
 - Fixed old Vampire Lord stuff still being collectible. (1593)
 - Ringside Impresario no longer triggers itself, and now correctly shuffles to deck, not adding to hand. (1594)
 - Vaudeville Hook now correctly reduces in cost, not increases. (1595)
 - The "Berry" token (1 Lun restore 3 Health) is now the default Signature spell if one wasn't chosen for a deck. (1596)
 - Made some Ringmaster cards collectible that were mistakenly uncollectible. (1597)
 - Added Ringmaster card Finale Architect. (1597)
 - Oni Queen's Banishment can actually be played, and now costs (0) down from (1). (159)
 - Klive Icetooth will now still heal you even if there are no other minions to Stun. (1588)
 - Crazed Cultist now interacts more intuitively with Paven, Elemental of Surprise. (1578) 
 - Collectible Permanents should no longer be found by minion-generating effects. (1591)
 - Primordial Pebble now correctly only discovers Elementals. (1580)
 - Mysterious Questgiver now properly makes 3 Discoveries. (1579)
 - Nilfheim Needlegunner no longer triggers off of itself. (1610)
 - Herald of Fate no longer just gives you double openers forever. (1577)

### 0.8.70-3.1.1 (March 27, 2020)

 - Introducing 30 new Overlord cards, thanks @Bromora! (1560)
 - Storyteller has been buffed, thanks @skurleton! (1571)
 - Archaelogist has been buffed and has one rework, thanks @Pircival! (1574)
 - Introducing the first 30 cards of the Skyward class, thanks @Nixolium! (1575)
 
### Additional Balance Updates from @Logovazz

A huge thanks to @Logovazz for coding up a variety of contributed nerfs and buffs from the community!

#### Updates List
Plenty of changes this time around. It's been a long time coming for the classes that are being removed. Two of them needed to be removed due to theming, Ice Knight was requested to be deleted ages ago, and Mercenary and Timeweaver have both become defunct. Mercenary has a successor in the form of Outlaw and Timeweaver is just a mess. Big neutral changes as well, with 7 cards being removed, 2 being added, and whooole lotta reworks. Some changes to Witch Doctor and new cards for Oni Queen. Enjoy.

#### Removed Classes
 - Sea Witch, Ice Knight, Mercenary, Deathknight, and Timeweaver

#### Neutral

*New Minions for Core*
 - Glimmer Dancer - 4/3/3 rare fae "Opener: If you played another card this turn, give your other Fae +2 Health."
 - Lunstone Rumbler - 3/3/4 rare elemental "Opener: Destroy your weapon to gain a Lun."

*Cards removed from Collection*
 - Redundant cards: Gustbreaker, Throat Cutter, Lightning Elemental
 - Unused cards: Hypnotic Beetle, Zealous Deckhand, Stowaway Scoundrel, Faefellow Crumble
 - Problematic cards: Stick

*Rebalance / Rework*
 - Novice Enchantress is now 4/2/2 Fae "Opener: Give a friendly minion +4 Health and Elusive."
 - Jungle Wanderer is now 4/2/2 from 4/1/1.
 - Hooded Ritualist is now 4/2/4 "Opener: If you cast a spell this turn, summon two 2/1 Demons."
 - Hoffis the Dunewalker now reads "Opener: Give all minions in your hand 'Opener: Summon a 0/2 Sandpile with Guard.'"
 - Rektan, Warrior of the Wild is now 4/3/3 Beast "Opener: Your champion has +1 Attack while it has a weapon this game."
 - Unnerving Spectre is now 4/4/2 Spirit "Dodge. At the start of your turn, gain Dodge."
 - Ironplate Samurai is now 5/5/3 from 5/3/3.
 - Haunted Armor is now 6/6/4 Spirit "Dodge. After an allied minion loses Dodge, gain Dodge."
 - Holdover Lich is now 5/5/6 from 6/5/6
 - Inora, Spellbound is now a Fae.
 - Oppressor Defender is now 6/3/3/ from 6/2/2.
 - Crystal Golem is now 7/7/7 Elemental "Elusive. Aftermath: Summon two 2/2 Golems with Elusive."
 - Blue Nestwatcher now has Guard.
 - Sourceborn Aelin is now 10/10/10 "Opener: Add a random Source spell to your hand."
 - Only 3 Source Spells: Apotheosis, Creation, and Worldsculpt
 - Apotheosis is now "Refresh your Lun, draw 2 cards."
 - Creation is now "Copy 3 random cards from your deck. They cost (0)."
 - Redhide Butcher is now 2/4/3 Demon "Hidden. Aftermath: Discard the leftmost card in your hand."
 - Doodles now reads "Opener: Your skill becomes 'Draw a card' until you play a minion."

*Renaming / Reflavoring*
 - Alemental is now Doro the Rebuilder
 - Skeletower is now Gashadokuro
 - Holy Bulwark is now Miasma Warder
 - Nice Cream is now Seafoam Cleanser
 - Jabberwocky is now Socialite Fox
 - Dante the Outlaw is now Kidnapper Dante
 - Streetwise Specialist is now Gramlot Enforcer
 - Roadblock Pufferfish is now Vilewing Patriarch
 - Divine Cleric is now Royal Healer and is a Dragon
 - Urson the Smort is now Caster Illiura and lost Beast Tag

*Pirates Reflavoring*

Pirates seemed off as a one-off tribe in Spellsource when the Tribes represent different races. So, neutral Pirates have become Spirits and Spirit Related.

 - Chaotic Cutthroat is now Manifested Tantrum
 - Hold Hunter is now Vengeful Spirit
 - Doomed Diver is now Yadokai
 - Gold Picaroon is now Spectral Thug
 - Crew Commander is now Three Faced Spook and gained Lifedrain
 - Treasure Hunter is now Offering Coveter
 - Captain Stashin is now Tiolan the Restless and a 5/4/6
 - Chainwrecker Corsair is now Fallen Warrior and a 5/5/4

#### Witch Doctor Updates
 - Spirit Bind and Hex Bolt can target any minion now.
 - Removed Subject to Sacrifice, Undergrowth Spirit, Underbrush Protector from Collection
 - Bottled Spirit is now 2 Lun "Give a minion +2/+2. Restore 4 Health to your Champion."
 - Ritual Dagger had its nerf reverted. It is now 2/2/2 "After your champion attacks, add a random Voodoo Spell to your hand."
 - Crone's Attendant is now (3/4/2) from (3/5/1).
 - Fading Berserker is now 2/1/4 (Opener: Gain +3 Attack until your next turn.)
 - Shimmerscale now reads ("Dodge. Hidden for 1 Turn. Elusive to enemy spells.")
 - Vilewing Broodmother is now (3/1/3) from (4/1/4).
 - Prolific Tamer is now (3/3/3) from (3/3/2).

#### Oni Queen Updates

*New Cards*
 - Draconic Doubler, Shortrun Stockpiler, Valiant Medic, Coordinated Assault, Migraine, Rally, Enenra

### 0.8.69-3.1.1 (March 21, 2020)

 - Introducing the Ringmaster class! When building a deck, make sure to choose a Signature spell!
 - Screen layout on the client has been improved.
 - Vampire Lord has been reworked.
 - Dragoon has been reworked.
 - Trader has had a few of its multi-discover cards improved.
 - Effects which cause random cards to be played should be more reliable.
 - Improve the reliability of editing and make it harder to accidentally orphan a game.
 - Automatic reconnecting on mobile devices after backgrounding the application now works.
 - Improve the reliability of Spirit from Long Past and other interactions with the graveyard as a source of cards.
 - Soulscream no longer concedes a match and now has correct behaviour. (1584)
 - Various Hive Queen balance changes. (1568)

### 0.8.68-3.1.0 (March 13, 2020)

 - A text-based card editor is now active during Quick Play. Access it by clicking the pencil icon in the top tray of the battlefield. You will not be able to draw cards during a multiplayer game.
 - Moon Gladiator now has the correct effect. (1563)
 - Freezing Over is now On Play. (1561)
 - Skills are now shown in the client when played.
 - Sunken Terror's text now reflects how it plays a copy of shuffled cards, not the originals. (1547)
 - Otherwordly Truth now correctly shuffles all 3 cards and not 1 at random. (1547)
 - Crate of Dynamite can now only be cast on friendly minions for simplicity's sake (1356)

### 0.8.67-3.0.10 (February 14, 2020)

 - End turn button just says the text for now in a tweak of its appearance.
 - No longer show the full screen button on mobile platforms. (fix client 29)
 - Account creation should be easier on portrait mobile since the fields aren't obscured anymore.
 - Android devices now correctly show three columns in the hand tray instead of two.
 - Power history is now visible. It will be moved to make it easier to read on mobile devices.
 - Pacing of many events has improved.
 - Fewer issues should occur trying to connect to games for the first time on mobile. The issue was related to mulligan timings.
 - Many popups now show in game, like You Go Second!
 - Playing a targeted card from the highest row of 10 cards and cancelling playing a card that takes no targets is now easier. (fix client #29)
 - The Council of Fate's spells are no longer mistakenly collectible and are also now all named properly. (1539)
 - Xii'da the Curious now correctly doubles the minions as well as their openers. (1544)
 - Exchange Wares now actually does its effect. (1544)
 - Sabotage! now properly shuffles the opponent's card back into their deck. (1544)
 - Revelation no longer permanently makes all cards in hand cost (1). (1545)
 - Cosmic Apparitions now correctly only discovers minions and also adds a copy to your hand. (1534)
 - Yig's Mastermind now only plays 3 cards instead of 5, and also properly removes them from your deck. (1536)
 - Freezing Over now properly buffs the minion only when playing minions, not summoning them. (1561)

### 0.8.66-3.0.9 (February 6, 2020)

 - The client now shows colors on cards.
 - Inspecting a card on mobile in the collection interface no longer leaves it stuck on screen.
 - Issues in the client related to some kinds of targeting have been fixed.
 - Star Sculptor now correctly gives the summoned minion Guard/Can't Attack and not the original. (1518)
 - Fixed an interaction when General Hun'Zho summons Lumina, Light of the Forest. (1516)
 - Necronomicon is now an Occultist card like it was supposed to be. (1523)
 - Forge Suit now properly only gives Health and not Attack. (1525)
 - Sand Filter now actually Decays, and is properly now only active on your own turn. (1525)
 - Flood the Market now properly casts the spells when your hand is full. (1532)
 
In addition, extensive edits by @Logovaz summarized here:

#### Uncollectible Neutrals
 - Jek the Bonelord, Firefoxie, Sunbender, Timebidding Magi, Toothpick Fighter, Pawshop Trader, Vanal Petkiper

#### Neutral Changes

 - Freying Faimiliar is now a Fae from a Beast and called "Forest Familiar"
 - Stone Sentinel (10/9/9 -> 10/10/10), Stalwart Sculptor (4/3/2 -> 4/3/4)
 - Skeletower (Costs 8 -> Costs 7)
 - Siege Elephant is now ("Whenever this minion takes damage, gain 2 Armor.") from 1 Armor.
 - Macabre Samaritan is now ("Whenever this minion takes damage, restore 4 Health to your champion.") from 2 Health.
 - Hunchback Supplier now has Lifedrain
 - Forest Magistrate (Opener: Deals 2 damage -> Opener: Deals 3 damage)
 - Floating Crystal (2/0/4 -> 2/0/6), Fae Horncaster (2/1/2 -> 2/1/3), Burst Bug (4/2/1 -> 4/3/1)
 - Bone Guard is now (5/3/5 "Guard. Opener: Draw a card.")
 - Mind Controlled Mech reflavored to be Blind Acolyte. Also: (4/2/8 -> 4/3/8). The skill has been renamed to Echolocation from Mental Command.
 - Warclub Ruffian is now a (4/3/2 Demon, Dodge, Guard)
 - Mindswapper (3/3/2 -> 3/4/2) and is now a Fae.
 - Lightning Elemental is now ("Whenever this takes damage, deal 1 damage to all other minions.") from "After this..."
 - Hold Hunter is now a (1/1/2 Pirate "After this attacks and kills a minion, gain Dodge.")
 - Giant Serpent now has Hidden for 1 turn and is a Common.
 - Captain of the Guard (4/3/3 -> 3/3/3)
 - Mrs. Merria is now (4/4/4 Fae "Opener: If you played 4 other cards this turn, take control of an enemy minion.") from (7/6/6) and 5 cards.
 - Fixed text on Marie Annette
 - Corona, Fae Defender's aura is now ("Your other Fae have +2/+1 and Elusive.") from +1/+1.
 - Wolfcrier (4/3/7 -> 3/3/7 and summons a 3/3 wolf instead of a 2/3 wolf)
 - Uccian Hydra (5/2/2 -> 4/3/3 and has Hidden for 1 turn) 
 - Sonorous Spectre is now (6/4/4 Spirit "Opener and Aftermath: Silence all other minions.") from just Opener.
 - Oni's Eye (2/0/2 -> 1/0/2), Sleepy Scarecrow (1/0/2 -> 1/0/3)
 - Crew Commander properly has Pirate Tag
 - Blackboar is now (3/3/3 Beast "Opener: Your next Dash minion this turn costs (3) less.")
 - Hybreeder is now (3/1/4 "At the end of your turn, summon a 1/1 Mutt with Guard.")
 - Nice Cream is now (3/3/3 Elemental "Opener: Restore 3 Health to all other characters.")
 - Holdout Soldier is now a Rare (4/2/6 "Opener: If your opponent has 4 or more minions, draw 3 cards.") from 2 and 1 respectively.
 - Added missing minion: Henchman Helper (5/5/2 "Opener: Give a friendly Guard minion +4/+4.")
 - Disco Inferno (4/4/3 -> 3/4/3), Alien Ravager now says ("Extra Strike")
 - Timewalker Initiate renamed to Patient Initiate

#### Oni Queen Changes
 - Lead by Example now costs 3 and does not draw a card.
 - Insurgency Captain Krika now requires 4 Champion Attacks (from 3).
 - Ruffian Shiro (2/2/2 -> 3/2/2)
 - 4th Ring Warden's effect should no longer stack.
 - Bogovey Jester is now an Aftermath from an Opener.
 - Darkfire Blaster (5/3/6 -> 6/3/7)
 - Warmongering is now ("Give a minion 'After this attacks, gain +2/+2'.")
 - Thousand Year Hatred is now ("Choose an enemy minion. Destroy all minions on the battlefield and in hand with the same name.")

#### Witch Doc Changes
 
 - Witching Traveler now sets cost of card to 0 from 1.
 
### Soulseeker Changes

 - Major changes to Soulseeker: “Obvious nerfs," card deletions, reflavoring and set changes, new cards, in particular Secrets archetype.
 - Reverse Volition: 1 mana common spell "Secret: When your opponent plays a minion, return it to their hand." (Replaces Dormant Spirits)
 - Rebirth: 0 mana common spell "Destroy a friendly minion. Summon a random minion with the same Cost."
 - Seeker of Mysteries: 2 mana 2/3 common "Opener: If you control a secret, Soulbind twice."
 - Render Imaginary: 2 mana common spell "Destroy a minion. Its owner draws 2 cards."
 - Transmute Life: 1 mana rare spell "Secret: When a friendly minion dies, give your other minions +1/+1."
 - Mirror Phantom: 1 mana 1/3 rare Spirit "While in your hand, this is a copy of the last Spirit you played."
 - Split Soul: 2 mana epic spell "Destroy a friendly minion. Add two 1/1 copies of it to your hand that cost (1)."
 - Soul Warden: 4 mana 3/4 Spirit "Opener: Summon 1/1 copies of your minions that died this turn."
 - Knowledge Feeder: 4 mana 3/3 epic Spirit "Opener: Destroy a friendly minion to put three Secrets from your deck into the battlefield."
 - Ranabosh, in Memory: 3 mana 4/2 Legendary Spirit "Aftermath: Go dormant. Trigger two friendly Secrets to revive this minion."
 - Prophet Elenthris: 4 mana 4/5 Legendary "Opener: Add five Soulstones to your hand. Play them all to summon Magoria."
 - Soulstone (Token): 2 mana spell "Destroy a friendly minion. If you've played all five of these, awaken Magoria!"
 - Mother Magoria: 10 mana 10/10 "At the end of your turn, fill your board with random minions."
 - Channeler Initiate costs (3).
 - Dominant Will now costs (3).
 - Extract Nightmare costs (6).
 - Domineer costs (7).
 - Spectral Host no longer gives guard.
 - Essence Harvester’s heal reduced to 8.
 - Rite of Promise costs (1) and only Soulbinds once.
 - Rite of Pain costs (2) and only Soulbinds once.
 - Rite of Passage costs (3) and only Soulbinds once.
 - Stranded Memory is reworked into “Twisted Pathology”, a 5 mana 3/4 common spirit with “Dash. Opener: Each player draws 2 cards.”
 - Ravenous Soul has been reworked into Devoted Denizen,  2 mana 2/2 Spirit with Guard and "Aftermath: Soulbind."
 - Osiris is now a 5 mana 3/6 with "Opener: Return your other minions to your hand. Summon random minions with the same Costs."
 - Wailing Banshee is deleted.
 - Delve Into Memory is deleted.
 - Vaash Trinity is deleted.
 - Dormant Spirits is deleted.
 - Otherworld Wayfinder is deleted. 
 - Olivia, the Successor is now a 4/3.
 - Channeler Initiate is now called "Seeker Initiate"
 - Jekk Trinity is now called "Lost Legionnaire"
 - Stranded Thought is now called “Afterthought”
 - Tortured Soul is now called "Dormant Soul"
 - Brink of Reality is now called "Brink of Existence"
 - Awaken is now a basic card (was core/classic).
 - Essence Harvester is now a core (classic) card (was setless).
 - Lost Legionnaire (formerly Jekk Trinity) is now a core (classic) card (was setless)

### 0.8.65-3.0.8 (January 24th, 2020)

 - Large Spiderling now correctly states that it deals 4 damage and not 3. (1497)
 - Necronomicon now correctly inflicts 3 damage instead of 2. (1503)
 - Lost City's Guardian now summons the actual minion you chose. (1405)
 - Mantis Lady no longer mistakenly treats certain tokens as Commons. (865)
 - Ruffian Shiro now properly only buffs Demons in your hand. (1496)
 - Indoctrination and Dream of Death both now properly Drain health. (1501)
 - Default Occultist decks have been updated! (1500)
 - Cultist's Vision: "Play minions with big stats/aftermaths and copy them."
 - The Grandmage's Grandcult: "Generate cards/spells and your spells are more powerful."
 - The Imperial Ritual: "Combo deck which needs you to have a setup turn, playing Imperial Soul and Grasp of God, then on the next turn, copying imperial soul with Spawn of Evil, the second one being affected by Forgotten Science, then you play Freying Familiar, and then you cast Doom Project for the win."
 - New Archaeologist Decks: Elemental Arch, Discover Archaeologist and Armor-Hulk's Reign! (1499)

### 0.8.64-3.0.8 (January 19th, 2020)

 - Introducing the Trader champion!
 - Adding new Archaeologist cards!
 - Bloodlord Goa now reads, Opener: Gain the aftermath, "Repeat all other friendly aftermaths."
 - Minions can now have a maximum of 16 active aftermaths.
 - Aftermaths on a minion that were not in its text are removed after they process.
 - All cards that triggered aftermaths, like Vein Burster, now correctly trigger aftermaths that were added by effects other than the target minion's text.
 - Haunted Berserker now correctly deals Toxic and Lifedrain damage (i.e. splash damage) with its effect.
 - Playing cards randomly is not recursive.
 - Bloodlord Goa now shows the source cards of cast Aftermaths. (1488)
 - Mosshorn now interacts more favorably with Revelation, and also has Guard as it was originally supposed to. (1492)
 - Auras that gave minions the same attribute now stack.
 - Elven Woundsealer, Edlritchampion and Ushibasu the Vigilante no longer interact to cause an infinite loop.
 - The Overlord's Skill Dark Rule now functions properly. (1494)
 - Fixed a typo with Hired Berserker and made it properly summon a 2/3 with Guard. (1495)
 - All for One's description now matches its actual effect. (1484)
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
