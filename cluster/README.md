# Cluster

This module contains code for running large numbers of simulations to test artificial intelligence opponents of Hearthstone, evoluationary deck creation techniques, card strength testing and other analytic/theorycraft tests.

### Getting Started with Metagame Analysis

Our objective is to find the strongest deck in Hearthstone. This deck ought to be the most powerful in the hands of an average player, with an expected winrate greater than 50% against the most common decks played today.

At a high level, we want to **simulate deck matchups**, and find the deck with the **highest winrate against all other decks**.

The pseudocode for what we want to achieve is:

  1. For each pair of decks in a list of possible metagame matchups:
     1. Play many matches of Deck 1 versus Deck 2 with simulated opponents.
     2. Record the winrate of Deck 1 versus Deck 2.
  2. Find the deck with the highest average winrate against all other decks.

The code in the cluster module would help us answer this question.

##### Hypothesis

Suppose we had a hypothesis that the winrate of a deck versus another deck is somehow related to the difference in inherit strengths of the decks, the difference in player skill, and some amount of noise.

```
Win Rate(Deck 1 vs. Deck 2) ~ 
    Deck 1 Strength - Deck 2 Strength
    + Player 1 Skill - Player 2 Skill 
    + Noise
```

Solving for `Deck 1 Strength` strength, we obtain the following relationship:

```
Deck 1 Strength ~
    Win Rate(Deck 1 vs. Deck 2) 
    + Deck 2 Strength 
    + Player 2 Skill - Player 1 Skill
    - Noise
```

This makes intuitive sense.

 - If Deck 2's strength is high **and** the winrate of Deck 1 in this matchup is also high, **Deck 1** must be exceptionally strong.
 - If Deck 2's strength is high **and** the opponent's skill (Player 2's skill) is also high, **Deck 1** must be exceptionally strong.
 - We subtract out player 1's skill because we're trying to account for the strength of **just** the deck.

We see two problems right away:

 1. How do we model the skill of a player? It's unobservable.
 2. How can we tell the difference between noise and differences in skill?
 
In this example, we'll make an important assumption to solve these two problems. Instead of modeling the skill of a specific player, we'll model the **average difference in skills between the opponent and the player**. In other words, we want a process where, on average:

```
Average(Player 1 Skill) == Average(Player 2 Skill)
```

If the average skills are the same, they don't strictly speaking cancel out: they just reduce to a new variable, the **average difference in skill**. This allows  us to substitute out the difference in skills in our original formula.

```
Deck 1 Strength ~
    Win Rate(Deck 1 vs. Deck 2) 
    + Deck 2 Strength 
    + Average Difference in Skills
    - Noise
```

So what process are we going to use?

##### Random Play

What if our simulated players play randomly?

If you imagine all the possible actions you can do throughout a Hearthstone match, a **strong** deck could be described as a deck that, on average, presents more **good** actions than **bad** during a game. In this case, **good** means an action that will eventually lead to a **win** more often than a loss, while **bad** means an action that will eventually lead to a **loss** more often than a win.

Notice that in this formulation, the skill of the player doesn't matter. A skilled player is capable of identifying good versus bad actions, but that doesn't affect the frequency by which good actions are presented. Only a better deck can make good actions appear more frequently.

However, there's one big catch: many good actions are only accessible if the player chose good actions earlier in the match. In other words, the **density** of good actions isn't equally distributed amongst all actions a player can take, even if good decks produce more good actions on average than bad ones.

This makes intuitive sense. Good players can get high win rates out of decks with rare but powerful win conditions, like Quest Mage. But in the hands of a poorly skilled player, Quest Mage is very, very hard to play: one wrong move results in closing off the win condition forever.

Fortunately, there is a solution to this problem. As long as we simulate many matches, we will, on average, sample the **actual** distribution of good actions regardless of how they are distributed throughout a match.

In other words, if we simulate millions of games per matchup, sometimes, we will accidentally simulate a very talented player only through random play. And we will simulate that very talented player as frequently as really talented players actually exist in the real world.

##### Validating Our Hypothesis

Fortunately, we have data on winrates for actual players in Hearthstone. We will use the data from HSReplay on metagame matchups to get those winrates.

However, there's a big catch: HSReplay players tend to be quite a big more skilled than your average Hearthstone player. And even then, your average Hearthstone player is quite a bit more skilled than your average **player**, who might have quit playing Hearthstone because he's so bad.

![Skill Levels](./docs/Skill%20levels.png)

Unfortunately, this process measures the average player, not the average HSReplay player or even the average Hearthstone player. However, this limitation gives us a clearer answer on which decks are actually strong in a broad sense, in that we've completely controlled for player strength.

We'll now compare the simulation data to the actual data to get a sense for how much a deck's winrate depends on its underlying strength versus the player's skill, by comparing the winrates in simulations as compared to winrates from replay data.

### Getting Started With Simulations (Work in Progress)

 1. In the root of this module, you will find some scripts that are handy for performing this analysis.
    - [`retrievematchups.py`](retrievematchups.py), which downloads the current state of the meta from **HSReplay** and prints a TSV table of archetype winrates
    - [`runsims.sh`](runsims.sh), which runs 10,000 simulations of every matchup of meta decks in the [current decklists directory](../net/src/main/resources/decklists/current).
 2. Adapt these scripts to run simulations of different decks, including adding new decks to test.

### What belongs in this module

 - **Analysis and Data Retrieval Code**: This module must contain only analysis code. This means running and analyzing deck simulations, retrieving data from outside sources, and formatting output.

 - **Backend Clustering Code**: This module hosts the Amazon Elastic MapReduce and Spark implementations of running the simulations on a cluster.

### What does not belong in this module

 - **Unlicensed for Github:** Anything, regardless of how or if it is copyrighted, that is not licensed for redistribution in a public Git repository.

 - **Copyrighted material from other games**. This includes the titles and descriptions written on cards.
 
 - **Derivative work**: An automated or deterministic rewriting (derivation) of copyrighted content. For example, mass-substituting the Hearthstone card text by replacing and rewriting sentences in a mechanistic way is not acceptable for this module.
 
 - **Trademarks**: The use of **any** trademarked term, alive or dead, that appears in other media. For example: Even though the particular card title "Planeswalker Alchemist" doesn't appear in Magic the Gathering, Wizards of the Coast has a dead trademark for Planeswalkers.

 - **Confusing material**: Anything that may lead a reasonable person to believe that a piece of content originates from a commercial media property. This includes parodies, homages and other references. For example: Even though "Cartman Renounces Darkness" is a reasonable parody of two commercial media properties, it may lead a reasonable person to believe that Blizzard and South Park Studios worked together on a card set.