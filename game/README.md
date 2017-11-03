# Game

This module contains a card game engine.

### What belongs in this module

 - **Engine interfaces and implementation**: [GameContext](src/main/java/net/demilich/metastone/game/GameContext.java) is the core object that executes a single match of Spellsource. The best AI is implemented by [GameStateValueBehaviour](src/main/java/net/demilich/metastone/game/shared/threat/GameStateValueBehaviour.java).

 - **Clean room implementations**: Code must be written as a "clean room" implementation: the author had only seen a behaviour and never a reverse engineered piece of game code from another game.

 - **Card-specific spells**: Some spells require a code implementation to deal with complicated behaviour. 
 
 - **Extensions for more gameplay**: Any additional game play mechanics for new cards should go here. Mechanics that require persistence between matches belong in the [net](../net) module.

### What does not belong in this module

 - **Unlicensed for Github:** Anything, regardless of how or if it is copyrighted, that is not licensed for redistribution in a public Git repository.

 - **Copyrighted material from other games**. This includes the titles and descriptions written on cards.
 
 - **Derivative work**: An automated or deterministic rewriting (derivation) of copyrighted content. For example, mass-substituting the Hearthstone card text by replacing and rewriting sentences in a mechanistic way is not acceptable for this module.
 
 - **Trademarks**: The use of **any** trademarked term, alive or dead, that appears in other media. For example: Even though the particular card title "Planeswalker Alchemist" doesn't appear in Magic the Gathering, Wizards of the Coast has a dead trademark for Planeswalkers.

 - **Confusing material**: Anything that may lead a reasonable person to believe that a piece of content originates from a commercial media property. This includes parodies, homages and other references. For example: Even though "Cartman Renounces Darkness" is a reasonable parody of two commercial media properties, it may lead a reasonable person to believe that Blizzard and South Park Studios worked together on a card set.