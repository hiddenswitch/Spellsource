package com.blizzard.hearthstone;

import net.demilich.metastone.game.actions.PlayChooseOneCardAction;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.*;

public class RastakhansRumbleTests extends TestBase {

    @Test
    public void testSpiritOfTheShark() {
        runGym((context, player, opponent) -> {
            Minion spirit = playMinionCard(context, player, "minion_spirit_of_the_shark");
            Minion edwin = playMinionCard(context, player, "minion_edwin_vancleef");
            assertEquals(edwin.getAttack(), 6);
            playMinionCardWithBattlecry(context, player, "minion_si7_agent", edwin);
            assertEquals(edwin.getHp(), 2);
            assertTrue(spirit.hasAttribute(Attribute.STEALTH));
            context.endTurn();
            context.endTurn();
            assertTrue(!spirit.hasAttribute(Attribute.STEALTH));
        });

    }

    @Test
    public void testShirvallahTheTiger() {
        runGym((context, player, opponent) -> {
            Card tiger = receiveCard(context, player, "minion_shirvallah_the_tiger");
            assertEquals(costOf(context, player, tiger), 25);
            playCard(context, player, "spell_anyfin_can_happen");
            assertEquals(costOf(context, player, tiger), 15);
            playCard(context, player, "minion_ultrasaur");
            assertEquals(costOf(context, player, tiger), 15);
        });
    }

    @Test
    public void testHexLordMalacrass() {
        runGym((context, player, opponent) -> {
            playCard(context, player, "minion_hex_lord_malacrass");
        });

    }
}
