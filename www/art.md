---
layout: page
title: Art
permalink: /art/
---

# Spellsource Art Specification (1.2)

This document contains the basic art specifications and needs for Spellsource.

### Changes Since 1.1

 - Added Blockly/Scratch block spec.

### Changes Since 1.0

 - Clarified that status icons for minions can be auras / outline changes to the base card, and which statuses appear most frequently with others.
 - Clarified values that appear sometimes next to champions.
 - Added leftmost and rightmost card icons.
 - In-hand card can now be a scaled-down version of a mobile full-screen, natural sized card.
 - Targetability/selectability is now programmatically rendered as an outline.
 - Added Card Creator spec for a **screenshot** instead of individual elements.

### Key Facts

 - **Genre:** Spellsource is a 2-player collectible card game for mobile, PC and web (all Unity3D platforms supported).
 - **Pixel Art:** It is a pixel art style game, where 6 iPhone X display pixels correspond to 1 art pixel (about 58 DPI, or a "dppx" value of 0.5). A portrait iPhone X is 188 art pixels wide, 375 "responsive" (viewport) pixels wide, and 1125 display pixels wide.
 - **2D Game:** The UI should be 2D, although the current UI is a hybrid 3D layout of 2D elements.
 - **Narrative:** A fantasy mystery with Chinese warring states period technology. An explorer discovers an island chain inhabited by mystical creatures--the source of all magic. The creatures are vying for control of the islands. Read more at [wiki.hiddenswitch.com](https://wiki.hiddenswitch.com).
 - **How to Play:** Visit [http://playspellsource.com/game] and create an account. Choose Quick Play to enter a match against a bot. The rules are very similar to [Hearthstone](https://playhearthstone.com/en-us/game-guide/), and a brief written overview can be found [here](/whats-new/#Gameplay).
 
### Glossary

 - **Token**: A sprite, character or small touchable icon with at most a small number of indicators and no text.
 - **Card**: A playing card. May have text and indicator icons.
 - **Champion**: Player's in-game avatar.
 - **Minion**: Character on battlefield typically represented by token.
 - **Spell**: Card in hand that sometimes takes a target character in game to play.
 - **Skill**: Card-like (can be represented by token on battlefield) that works like a spell (takes a target). 
 - **Weapon**: Item equipped by the champion.
 - **Touch-held**: Press and holding with a finger on an icon for a short period of time, typically to do popups/tool tips.
 - **Mana**: Resource spent in game to do actions.
 - **Card Back**: A card turned over whose values aren't visible.
 
### Deliverables

These are the art products needed in order of priority.

#### 1. Battlefield

Spellsource needs a mobile-ready portrait and landscape supporting battlefield where the main gameplay takes place.

 - Accommodates 7 minion tokens on each side of the battlefield with a base card, character sprite and shadow.
    - Tokens need to show their attack and hitpoint values at a glance.
    - Should accommodate statuses.
        - See the [Buffs/debuffs/statuses](#buffsdebuffsstatuses) section for the different statuses. 
        - At most **one** icon.
        - Whenever favorable for portrait layout, render the status as an overlaid particle effect, stationary graphic aura, or distinct outline / shape change for the base card.
        - These often stack!
            - Guard is the most often stacked with others, so it's a good candidate for a base card shape change. It indicates that it must be targeted first when doing physical attacks.
            - Trigger host is the second most often stacked with others and is also used to communicate gameplay effects.
            - Finally, Aftermath is the third most often stacked with others. This communicates to players that a character does something important when it dies. Currently rendered as a "giftbox."
            - Everything else is very rarely stacking with each other so it's okay if it overlaps.
    - Clear which side of the battlefield is friendly versus opposing.
    - Needs to be in a line.
    - Can be touch held to show the card corresponding to the minion. This is also a chance to provide tooltips for all the statuses/indicators in effect for the character, so that in case the statuses are crowded players still know what's going on.
 - A champion token, a weapon token and a skill token on each side.
    - Can be touch held to show the card corresponding to the token.
    - Champions can have buffs/debuffs/statuses like the minions.
    - Both have hitpoint values.
    - Weapon has an attack value.
    - **Sometimes values**:
        - Champion sometimes has an attack value.
        - Champion sometimes has an armor value.
 - Both player's hands, up to ten cards.
    - Should be clear which card is the "leftmost" and "rightmost" using a **simple icon** (like a left arrow or a right arrow).
    - During the opponent's turn, opponent's hand position changes and draws should be clear.
    - Each card should have a mana cost, title, portrait and when applicable, attack and health values. See the card deliverable for more detail.
    - Has a "zoomed out" view, which should show some small suggestion of the image, the title and cost at minimum, and a "zoomed in" view, which allows the player to drag and drop any of the cards easily. "zoomed in" may also expand into a traditional scrollable grid instead of obscuring elements on screen.
 - An End Turn button.
 - A menu button that leads to an options screen
    - Concede button
    - Chat button
    - Report an issue button
    - Music and sound effects mute buttons
 - An Overlay eye button that toggles between the following overlays:
    - No overlay
    - Info overlay
        - number of cards in both player's hands
        - number of cards in both player's decks
        - total bonus spellpower
        - number of cards discarded
        - number of cards roasted (cards that were forcibly lost due to overdraw or discarding from deck)
        - current turn
        - full number of available mana versus total mana
        - Power history: A scrollable column/row of icons that can be touch-held that show what has happened inside the game
    - Chat overlay:
        - Emote button
        - online friends list
        - conventional chat view (list of text bubbles + input text field) when a friend is selected
        - exit button
 - Battlefield illustration:
    - First battlefield based on the Spellsource lore
    - Large, rectangular, croppable
    - Could be tileset

##### Buffs/Debuffs/Statuses

 - Playability/targetability auras: **Programmatically** rendered borders that indicate the status of the character
     - None (default unplayable)
     - Green: playable
     - White: targetable
 - **Condition Playable**: A particle effect aura overlaid on the card or character  that indicates the card's condition, like its opener condition, is met. For example, some cards gain a bonus if you hold another card in your hand. This aura communicates the condition is met.
 - Aftermath: indicates when the character dies something happens
 - Trigger host: indicates the character's text is listening to events on the battlefield and may cause other effects. The indicator or aura will "activate" whenever the trigger fires, during game events, to communicate to the player that the trigger has fired.
 - Guard: indicates the character must be targeted by physical attacks before other characters. May be indicated by using two rows, an icon, or some kind of change to the base card.
 - Stunned: A character that has a no-attack debuff.
 - Hidden: A character that cannot be targeted until it attacks first.
 - Extra strike: A character that can attack twice
 - Dodge: A character which takes no damage the first time it is hit. Dodge is lost after the first hit.
 - Deflect: A character whose champion takes damage instead the first time it is hit. Deflect is lost after the first hit.
 - Elusive: Cannot be targeted by spells or skills.
 - Toxic: A minion or weapon that kills whatever it hits.
 - Silenced: Indicates a character whose text is no longer in play.
 - Drain: Indicates a minion restores and gains health for each damage dealt.
 - Litedrain: Indicates minion or weapon only restores health for each damage deal
 - **Wounded:** Indicates the character has taken damage and is no longer at maximum health.

#### 2. Card

A visual representation of a card at multiple scales.

##### Elements:

 - **Portrait**: Art representing the card
 - **Name**: Name of card (should be one line, should scroll horizontally on touch-holding for overflow)
 - **Description**: Text on card that says what it does (may be multiple lines, may or may not scroll depending on scale)
 - **Mana Cost value**: Number written on almost all cards indicating how much mana the player needs to use to play the card. Should be visible at all scales.
 - **Card Type**: The type of card. Rendered as an icon, a crown on the card, or border / outline decorations. Sometimes a visible label on the card.
    - Spell
    - Minion (the only type that does not need label)
    - Weapon
    - Champion (card representation of champion)
    - Skill (bonus spell belonging to champion)
    - Permanent (cannot be targeted)
 - **Rarity**: Indicates relative power / specialness of card. Should be visible at all scales.
    - Free/Uncollectible: No special rarity
    - Common
    - Rare
    - Epic
    - Mythic: One copy per deck, most powerful
    - Legacy: Changes over time and has networked / multi-match effects
 - **Champion Alignment**: Indicates which champion this card belongs to, or neutral. Should be visible at all scales.
 - **Attack value**: Amount of damage dealt by minions.
 - **Hitpoints value** Amount of hitpoints that a minion has.
 - **Buff/debuff indicators**
    - Values (mana cost, attack, and hitpoints) should support a buff and debuff effect that signals to the player if the card/token has a buff/debuffed value.
    - Tokens and cards may be under the influence of an aura (a different card's temporary effect). For example, minions may have a temporary attack bonus that goes away when the minion or effect granting that bonus ends. This is in addition to a buff when appropriate.
    - Hitpoints can be "damaged" which is different than debuffed. This may also be represented by a "wounded" effect for the whole minion/champion token (only minions and champions can be wounded).
 - **Playability Auras**: Animated borders that signal various statuses of the card:
    - None (default unplayable)
    - Green playable
    - Blue activated (when released on blue, the card is played)
    - Yellow condition playable (indicates the card's condition, like its opener condition, is met)

##### Scales:

 - **Tiny**: Front with single mana cost card back scale (for the opposing hand).
    - When front of card (i.e. visible card) May consist of a rectangle whose color corresponds to the champion alignment, + card type, a colored gem or outline that corresponds to rarity, and the mana cost.
    - Back of card is just a small-scale card back.
    - Used to represent opponent's hand on the battlefield and potentially touchables in the power history.
 - **Small**: In-hand card zoomed out.
    - Can be a **scaled down** version of a full card, but the following elements should be legible in scaled-down version:
    - Front of card shows visible title and Thumbnail scale of portrait (see portrait elements).
    - Mana cost, attack, health, and a card type easily visible.
 - **Full**: Card at natural screen size (close to 3.5x2.5 in, or 204x145 pixels, exact size TBD)
    - All elements from tiny and small.
    - Full description.
    - Full portrait

#### 3. Portrait elements

Spellsource will standardize around a few assets of the same creative material for its illustrations of cards.

 - Sprite: A character/sprite/token version of the art. Exact size TDB. Spells that target/affect characters on the board may have an animated FX instead for this asset. In the 16-40 square range (TDB).
 - Thumbnail: ~50x50 crop or scale of full-sized portrait art used on **Small** card scale. Exact size TDB.
 - Full: 150x150 (up to 450x150) pixel art image with background representing the card. This is the key art.

#### 4. User Interface Elements

User interface elements for the menus / card collection / other screens.

##### Colors:

 - Light: UI elements in light will have dark text on them.
 - Dark: UI elements in dark will have light text on them.
 - Brand: The brand color from the mark (TBD)
 - Alert: A single alert color
 
##### Items:

 - **Fonts**: Sizes TDB.
    - 1 creative font full-sized card titles, player names and other branded elements.
    - Multiple sizes of screen fonts. Needs to support Small card size title, Tiny numbers and symbols, full size with upper and lower case text for full sized cards and other text in game, and a heading size for various UI elements.
 - **Panels**: Body of panel: Light, dark, 9 slice. Header and footers for panels in light and dark. 
 - **Wells**: Dark and light wells for adding some depth to panels, 9 slice
 - **Buttons**:
    - Icon frame (mini raised) light, dark, brand and alert. Fixed size.
    - Normal (raised), light dark brand and alert. 9 slice.
    - Jumbo, for elements like End Turn Button and Start match. Only brand color. 9 slice or fixed size, creator's choice. Will only need to accommodate very short strings (~12 characters)
    - Activated aura / FX animation in 9 slice (can we even do that?). Like a magical aura animated border.
 - **Dropdown**: Light and dark dropdowns. Input frame, down arrow, lower frame.
 - **Checkbox**: Light and dark checkbox elements.
 - **Input field**: Light and dark input field elements.
 - **Collection-specific elements**:
    - Deck grid item. Represents a player's deck in the decks grid. Needs a text label and suggestion of what champion alignment it has.
    - Deck row item. Represents a player's deck in the right panel in the Quick Play/Matchmaking view's deck list. Text label and suggestion of champion alignment.
    - Card row item. Represents a card in the player's deck. Should have portrait crop, title, cost, rarity and number of duplicates suggested.
 - **Chat-specific elements:**
    - Presence icons
        - In-game
        - Offline
        - Pending 1v1 challenge in two colors (one for received, another for send)
        - Pending friend invite in two colors (one for received, another for send)
    - Chat bubbles in light and brand (representing you and other conversants), 9 slice with arrrows on appropriate size.
    - Send / mail button.
 - **Currency icons:**
    - Mana
    - Dragonbreath (magical fire substance)
    - Gold
    - Runes (language like Sylvan and a premium currency)
 - **Game mode icons:**
    - Ranked
    - Versus Bot
    - Friends
 - **Miscellaneous icons**
    - Sound mute / unmute
    - Question mark
    - Info
    - Delete/trash
    - Options gear
    - Back
    
#### 5. Marks, Web and Design Collateral

These elements will be used for app stores, icons, trademarks, the wiki and the main website.

 - Common game icons: 16x16, 32x32, 64x64, 128x128, 256x256, 512x512, 1024x1024 for all platforms! Do not precomp for iOS but test it with automatic recomposition into rounded corners.

##### Stores

 - **Steamworks**: See [this link](https://www.dropbox.com/sh/lafahgngktojmum/AABieaZxc7Ra2-TITBJJVkgya?dl=0) for templates, examples and a PDF of the Steamwork's guidelines. 
    - Large and header capsules, which can be crops of the same thing (mark + key art):
        - Header Capsule at the top of the store page, recommended for you, grid view in steam client, etc. 460x215px exact, scaled down automatically to 292x136 so test it.
        - Large capsule at top of genre and publisher pages. 467x181, is not scaled down.
    - Small Capsule that features mark, very little key art, used for lists, like search results. 231x87px exact and will be scaled down automatically for 120x45 and 184x69 so test it.
    - Main Capsule, large illustration with mark in lower center. 616x353.
    - Community icon: 32x32 icon, basically the app and client icon.
    - Community capsule similar to store page capsule, 184x69. Used for client library list view.
    - Screenshots in 1920x1080 (widescreen, PC):
        - Key art, "The World's Best Community Card Game"
        - Widescreen battlefield, "Never pay for loot again"
        - Collection view, "All content unlocked"
        - Card editor, "Write your own the cards!"
        - Key characters, "Discover the source of all magic"
    - Page background ambient, 1438x810 "does not compete with content on the page", patterned background of some kind
    - Package Header Image, key art with mark, 707x232
 - **iTunes**: See [this link](https://help.apple.com/app-store-connect/#/devd274dd925) for more on iTunes screenshot requirements. We will only need to do portrait screenshots here.
    - Screenshots like for Steamworks except in **portrait** in the following sizes:
        - Phones:
            - 1242 x 2688 pixels (portrait)
            - 1125 x 2436 pixels (portrait)
            - 1242 x 2208 pixels (portrait)
            - 750 x 1334 pixels (portrait)
            - 640 x 1096 pixels (portrait without status bar)
        - iPad: 2048 x 2732 pixels (portrait)
 - **Google Play**: See [this link](https://support.google.com/googleplay/android-developer/answer/1078870?hl=en) for more on Google Play requirements. We will only do a feature graphic.
    - Feature graphic, like the steamworks capsules having key art and the mark: 1024x500

##### PlaySpellsource.com and HiddenSwitch.com

 - Hidden Switch mark (keep it simple and wide)
 - Icons:
    - Pixel art globe. 
    - Pixel art Steam, iOS, Google Play and web icons.
 - **Sketches** of collateral art to be commissioned
    - For example, a pixel art iPhone would be a waste of time for you to do so we should just commission it.
    - Scrolls, weapons, objects and other miscellaneous pieces to break up the monotony of a bunch of text.
    
##### Discord and GitHub

 - 10 Discord emotes for premium users (32x32 to upload, 16x16 canvas)
 - Status icons:
    - King icon for admins
    - Hidden Switch icon for people like you and me
    - Programmer / Contributor icon for people who do engineering
    - Artist / Contributor icon for people who do art
    - Class Maker / Contributor icon for people who write champions and cards
    - Lore Writer / Contributor icon for people who write to the wiki

#### 6. Card Creator

The card creator is a block based programming interface for coding Spellsource cards.

##### Elements

Use the spec from the [Scratch 3.0 design materials](https://github.com/LLK/scratch-blocks/wiki/Design). This spec will be used 

 - 9 slice "blocks" that support different elements in each of the 9 corners and shows an icon in the center.
 - Follows a lego-piece like pattern of notches.
 - The blocks almost always have type.
 - Sometimes the blocks contain input fields
 - They have a vertical and horizontal notch pattern.
 - They sometimes contain empty puzzle piece cutouts inside for doing code loops.
 - They support a rectangle, diamond, capsule and "start" (hero) border, and a small, wide, and tall shape.

##### Screenshot

Screenshot for reference for a [Blockly](https://developers.google.com/blockly/) based code editor for creating cards in the game. This screenshot will be the reference image for communications about the feature, especially for funders.

 - [Use this demo to understand what this is](https://developers.google.com/blockly/)
 - [Use this screenshot for reference](https://www.dropbox.com/s/wxtn5bqwmlgdklq/Blockly%20Screenshot.png?dl=1)
 - Variety of blocks and notches should be rendered in pixel art with thematic text.
 - Right pane shows Spellsource card with editable text fields.
 - Left pane should show list of blocks.
 - Does **not** need to be sliced
 - Include the play button. This would hide the editor and take you into a battlefield with the card in your hand.
 - Keep it **landscape iPhone sized**. If you have an innovative idea on how to make this portrait go for it, but it may be too hard! There are other block based programming tools for phones to reference.s