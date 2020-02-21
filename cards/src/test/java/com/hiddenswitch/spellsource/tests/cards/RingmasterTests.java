package com.hiddenswitch.spellsource.tests.cards;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

public class RingmasterTests extends TestBase {

    @NotNull
    @Override
    public String getDefaultHeroClass() {
        return HeroClass.CANDY;
    }

    @Test
    public void testOpeningActor() {
        runGym((context, player, opponent) -> {
            player.setAttribute(Attribute.SIGNATURE, "spell_chain_dance");
            Card signature = receiveCard(context, player, "spell_chain_dance");
            Card signature2 = receiveCard(context, player, "spell_chain_dance");
            Card nope = receiveCard(context, player, "spell_prestidigitation");
            assertEquals(3, costOf(context, player, signature));
            assertEquals(3, costOf(context, player, signature2));
            assertEquals(3, costOf(context, player, nope));
            Minion actor = playMinionCard(context, player, "minion_opening_actor");
            assertEquals(2, costOf(context, player, signature));
            assertEquals(2, costOf(context, player, signature2));
            assertEquals(3, costOf(context, player, nope));
            playCard(context, player, signature, actor);
            context.getLogic().endOfSequence();
            assertEquals(3, costOf(context, player, signature2));
            assertEquals(3, costOf(context, player, nope));
        });
    }
}
