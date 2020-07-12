---
title: "Contribute"
date: "2020-02-01"
path: /contribute
header: false
---
## Contribute  
### Table of Contents

 1. [How to Use This Document](#1-how-to-use-this-document)
 2. [Basic Engineering Rules](#2-basic-engineering-rules)
 3. [Contribution Guidelines](#3-contribution-guidelines)
    1. [Priority of Accepting Contributions based on Engineering](#31-priority-of-accepting-contributions-based-on-engineering)
    2. [Priority of Accepting Contributions based on Gameplay Quality](#32-priority-of-accepting-contributions-based-on-gameplay-quality)
 4. [Card Merging Process](#4-card-merging-process)
    1. [Addressing Engineering Problems with Existing Cards](#41-addressing-engineering-problems-with-existing-cards)
    2. [Addressing Other Problems with Cards](#42-addressing-other-problems-with-cards)
 5. [Adding Wiki Pages](#5-adding-wiki-pages)

### 1. How to Use This Document

If you're making a contribution to Spellsource, please skim this document for what you need to know. Use its categories and quote it to help yourself, the maintainers and others keep up to standards for contributions and ensure everything goes smoothly.

### 2. Basic Engineering Rules

 - Do not rename already existing card files, even if the `"name"` changes.
 - Do not delete existing card files. Instead, set `"collectible": false`.
 - Make sure each file ends in a `.json` extension.
 - Card file names should be all lowercase and alphanumerics with their type first and separated by underscores. For example, `spell_card_name.json`.
 - Use Spellsource keywords. See the [Keywords](/keywords) section for a complete list of keywords.
 - Store cards in the `cards/src/main/java/resources/cards/custom/` directories in this repository.

### 3. Contribution Guidelines

Visit our [Discord](https://discord.gg/HmbESh2) to chat about these guidelines and what cards you'd like to add.

Spellsource welcomes custom cards from the community. Almost all contributions are accepted, but they tend to be accepted at different speeds depending on a few characteristics of the contribution. The better the **engineering** and the better the **gameplay quality**, the more likely your cards will be available in the public servers sooner rather than later.

The long term goal of accepting contributions from the community is to make an **innovative and original game**. This means new, fresh-feeling mechanics and original flavor. This document uses these guidelines to encourage the things experienced game designers do to find innovative and fresh gameplay.

With regards to **engineering**, maintainers prioritize content based on its engineering quality in this order:

#### 3.1 Priority of Accepting Contributions based on Engineering

 1. Fully coded in JSON, passing tests, tests for specific cards and placed in a Pull Request.
 2. Fully coded in JSON, passing tests and placed in a Pull Request.
 3. Fully coded in JSON and placed in a Pull Request.
 4. Mostly coded in JSON and placed in a Pull Request. This means the author did not know how to implement the cards in code 100%, but wrote outlines of easy functionality like `baseAttack` and `baseHp`.
 5. Code of any kind that's attached as a zip file in an Issue.
 6. Code of any kind sent to the maintainers in the Discord.
 7. Hearthcards gallery of content linked in an Issue.
 8. Spreadsheet of content linked in an Issue.
 9. Card text in an ordinary text format in the Discord `#suggestions` channel.
 10. Direct messaging the maintainers in Discord non-coded (i.e. image or text) content of any kind.
 11. Card images in the Discord `#suggestions` channel.
 
Observe that the way you may be used to sharing a card--as an image with carefully selected art--is actually the least helpful way for the Spellsource maintainers to integrate new content.

With regards to **gameplay quality**, maintainers prioritize content based on its gameplay quality in this order:

#### 3.2 Priority of Accepting Contributions based on Gameplay Quality

 1. Original hero classes (60+ cards) with some evidence of being judged positively by the community (e.g. Hearthpwn competitions, weekly /r/customhearthstone competitions).
 2. Original cards with some evidence of being judged positively by the community.
 3. Original cards using the Legacy mechanic: cards that remember things that have happened to them in prior games, like how much damage they dealt, what minions they destroyed, etc.
 4. Original cards with great new mechanics.
 5. Original cards for custom classes.
 6. Original cards for the Neutral class.
 7. Original cards for Hearthstone classes.
 8. Cards that adapt obscure copyrighted content. Use your best judgement for obscurity. For example, any English language television or movie content with a sequel is not considered obscure, becuase it was worth making a sequel.
 9. Hero classes that comprehensively adapt mainstream copyrighted content. For example, a complete House of Stark hero class adaptation for Game of Thrones.
 10. Cards that are adaptations of cards from all card games except Hearthstone (e.g., Magic: The Gathering).
 11. Cards that are adaptations of existing Hearthstone cards.
 12. Hearthstone uncollectible content, like adventure content.
 13. All other Hearthstone content.
 14. Individual cards that use copyrighted mainstream content, like mainstream Marvel and DC superheroes, big HBO TV shows, etc. Consider making a whole Hero Class instead.
 15. Any obsolete content, like cards prior to being "nerfed," beta cards, etc.
 16. Joke and parody cards.
 
The way to read this chart is to think about how your content can meet the qualifications to be as close to #1 for both measures. Fully coded original hero classes with tests will get the most QA attention and will be integrated the fastest. However, since some people are great designers and not so great programmers and vice versa, the maintainers are committed to code your content or improve your designs on the Discord.

As a general policy, except in very rare instances, we say a hard no to joke cards.

### 4. Card Merging Process

The current maintainer, **@doctorpangloss**, merges the highest quality (engineering and gameplay) cards on a first-come, first-serve basis. Merging means taking work someone else did and reconciling it with work that happened simultaneously by others. In practice, card merging is done one-group-of-contributions-at-a-time, and a certain amount of quality assurance and automated testing is performed.

Concretely, the maintainers perform the following steps:

 1. Receive content from the contributors, either in the form of card text, JSON or a pull request.
 2. `git checkout` the code representing the contribution. This means creating a "branch," or a series of programming steps in a code editor representing this contribution.
 3. Examine each `.json` file for issues. Common gameplay issues include confusing card text, and the maintainer typically reaches out to the author on Discord to clarify those issues. Common engineering issues include:
    - Bad syntax, like missing commas, curly braces or square brackets. Make sure to look carefully at JSON syntax rules to save the maintainers time fixing small issues.
    - Bad file names (missing the `.json` extension, misspelling `minion`, etc.)
    - Incorrect `heroClass` names.
    - Incorrect spell `class` tags.
    - Bad capitalization of case sensitive values, like hero class names.
    - Incorrectly written deathrattle objects (incorrect: `"deathrattle": {"spell": ...}}`, correct: `"deathrattle": { ... }`).
    - Incorrectly written battlecry objects. Typically the `spell` key is missing or the contents of it are at the same level as the `battlecry`.
    - Incorrect or non-existent `TargetSelection`.
    - Wrong `collectible` (e.g. `true` when it should be `false`).
    - Bad formatting (too much or too little whitespace).
 4. Make changes to address these common issues file by file.
 5. Flag complex cards for needing tests. Tests are a sequence of reproducible steps written in code that simulate gameplay rather than doing it in the client, and represent the state-of-the-art in reliable game programming practice. Cards are typically complex when their text is highly original and interacts with many gameplay elements.
 6. Author tests.
 7. Run an automated test using `./gradlew game:test`. This includes playing 100,000 random games with random decks, which statistically causes every collectible card in the game to be played at least once.
 8. Fix syntax and other related errors reported by the test system.
 9. Examine traces that are generated by automated tests showing errors. A trace is a sequence of steps that a virtual player takes in a game that caused the game to crash. The maintainer runs these traces to reproduce the crash, and fixes the underlying cause inside a code editor and debugger like IntelliJ.
 10. Commit the changes with `git`.
 11. Run a full suite of tests including the multiplayer networking test using `./gradlew test`.
 12. Fix issues reported by the test.
 13. Merge the finalized code. When this occurs, a message appears in the Discord `#bugs` channel.
 14. Gather more changes (repeating steps 1-13) from other contributors.
 15. Update the release notes on the website.
 16. Deploy to the production servers. This means the code is now live and available for people to actually use. Note that deployment happens after a message appears in the Discord `#bugs` channel.
 
In practice, deployment occurs as frequently as once a day and as infrequently as once a week. It depends on the work load and the number of players currently playing Spellsource. Doing a deployment ends all currently running games, so it can be disruptive to do it too frequently.

#### 4.1 Addressing Engineering Problems with Existing Cards

To report issues, use the GitHub Issues functionality and follow the template there. The template emphasizes reproduction steps. In particular, a great issue contains the word **should** in its title, indicating what **should** be happening in the game but isn't.

Issues reported in the `#bugs` channel will still be addressed, but at a lower priority than the issues reported correctly in GitHub.

You can also request changes to gameplay by making the change using a code editor of your choice, editing the card file and submitting a pull request.

#### 4.2 Addressing Other Problems with Cards

To address other problems with cards, consider if the problem falls into any of the categories below. Each of these categories has a process for addressing the card's issue:

 - **The card is unbalanced**: Create a GitHub issue or a code pull request explaining the balance issue. Changes submitted by the author of the card are generally accepted, while changes submitted by others will be discussed briefly in the Discord. Changes to stats, like the attack, health or cost of a card, are generally given lower scrutiny than changes to the effects and text written on the card. Since Spellsource is a living card game, every community card is *subject to change*, regardless if its author approves or does not approve of the change.
 - **The card encourages a bad design practice**: Some contributions may be original but introduce bad design practices. Some common bad design practices include but are not limited to:
   - A mechanic that's too complicated. These are mechanics that require too much text to explain or require too much arithmetic. As a rule of thumb, effects that are clearer when delivered as multiple cards instead of being written on one card are typically too complicated.
   - A new keyword when a keyword isn't necessary. Keywords are only necessary if they are (1) a key part of a custom class's identity, (2) save space on at least three cards such that they no longer exceed four lines of text (about 25 words), (3) are interacted with as keywords instead of as effects. "Interacted with" means text like, "Your **Battlecries** trigger twice": that card interacts with **Battlecry** as a keyword, rather than **Battlecry** as an effect. Keywords both make the game feel new, by introducing new mechanics, and make the game feel derivative and complicated to new players. This tension needs to be played out in favor of new players, so new keywords that meet these guidelines will generally be accepted.
   - Text that is too long. Text should generally be fewer than 6 lines of text in Spellsource, 4 in Hearthstone, or about 25 words.
   - Too much arithmetic. Cards that introduce a variable, like "This deals damage equal to the number of cards in your hand," are generally the limit. Cards that require arithmetic against a variable, like "This deals damage equal to the total cost of the cards in your hand," is generally a bad design practice and will need to be changed.
 - **The card has bad flavor, like its title copies another card**: Unique names for cards are not required, but highly encouraged. Cards that duplicate existing cards fail the contribution gameplay guidelines and are rarely merged. If a card has repetitive theming compared to other custom cards, content will *not be removed* for the sole reason of having an unoriginal name. However, every community card's flavor text (its title and exact wording) is *subject to change*, regardless if its author approves or does not approve of the change. Most flavor changes that do not affect gameplay will be accepted. Flavor changes that make cards confusing or duplicating other card's flavor will be denied. Changing the names of cards that currently copy other cards' titles is *strongly encouraged* and will generally be accepted.
 - **The content doesn't fit well with other content**: Contributions sometimes feel out of place. This is a highly subjective form of feedback, and unfortunately having too many rules about what is and is not desired flavor or gameplay usually reduces innovation, not increases it. Please discuss on the Discord if you feel that a specific contribution isn't addressed by the gameplay guidelines above and should not belong in Spellsource for this reason.
 - **I no longer want my card(s) in Spellsource**: Contributions to Spellsource are made *without limitation to their use, in the present or the future*. Requests to remove cards will be considered on a case-by-case basis, but because quality assurance and work went into merging the cards, such requests will generally be rejected. Although Spellsource does not require contributors to sign a "Contributor License Agreement," like many commercial software projects do, Spellsource is governed by the AGPLv3 whose rules and norms are well understood by the software community. By contributing to Spellsource, you agree to be bound by those rules and norms, which generally exclude "moral rights of the author," i.e., the maintainers do not recognize the inalienable ability of authors to decide what happens with the content they author regardless of other community and legal norms. Please reach out on the Discord or create an issue to surface an issue in this category, but generally removing content for no good reason will be denied.
 
Some considerations go into whether or not changes are accepted for these problems:

 - **Motivation of the author**: Since contributing to Spellsource is difficult, and authoring large sets of cards like hero classes is a lot of work, it is in the interest of the maintainers that contributors remain motivated to contribute. This means that even when guidelines are violated, changes will be rejected or delayed if the result will be that the whole body of a work in progress, like a class, is abandoned by its author. However, such accomodations are made on a case-by-case basis. Overall, Spellsource wants to respect the wishes of authors as far as it encourages them to make the necessary changes to their content instead of abandoning contribution altogether. Many communities experience this tension, and Spellsource is no exception. Please visit the Discord to discuss considerations like these.
 - **The existence of constructive feedback**: If the change is made with constructive feedback, it favors acceptance of the change. Constructive feedback includes:
   - Gut reaction. This means a simple "good" or "bad" judgement. Gut reactions are important because they abstract away the details of why a card or hero class is good or bad, and shows to authors that there is some problem that is the author's responsibility to fix. An author's position is seen in a good light if the author received gut reactions well and seemed to respond to them, typically with a suggested improvement.
   - Suggested improvements. This means identifying a concrete problem with a card (e.g., it's unbalanced) with a concrete fix (e.g., try reducing its cost). This is a form of brainstorming. An author's position is seen in a good light if the author was responsive to suggested improvements or tried their own. 
 - **Positive interactions with the community**: If the change is otherwise brought on by a commitment to improve the community's experience, it will be viewed favorably. There is a tension between an enthusiastic player, who may have been authoring custom cards and making contributions for a long time, and the player who *isn't playing yet*, like anonymous Internet users. Generally, the maintainers want to support users who are not yet playing, because they cannot voice their concerns and they help keep the game alive. For example, while flavor text is *always subject to change*, having original flavor for the time being is preferred to something unoriginal, because it's important that new players feel like they are playing something fresh and new. Or, for example, treating others with respect is always viewed *favorably*, because being disrespectful to new players or contributors will discourage their first match or contribution.
 - **Issues that improve the accessibility of the game or promote it positively**: Violating a contribution guideline in a way that encourages others to play or meets a specific, clear demand from players, especially new players, will generally be accepted.
 
### 5. Adding Wiki Pages

Our Wiki pages are built using Markdown files. Go to our [lore folder](https://github.com/hiddenswitch/Spellsource/tree/master/www/src/lore) on Github; to make changes, make sure you're signed in. Before writing anything, check and follow the already-existing Markdown files for formatting. 

If you want to create a new Wiki page, click `Add File` on the upper right corner of the viewer. You can either choose to create a new file on the site or upload a Markdown file. 

If you want to edit an existing Wiki page, navigate to that page on the Github repo and click on the pen symbol on the upper right side of the viewer to make changes.