package com.hiddenswitch.spellsource;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.logic.GameStatus;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CustomHearthstoneTests extends TestBase {
    @Test
    public void testMysticSkull() {
        runGym((context, player, opponent) -> {
            Minion bloodfenRaptor = playMinionCard(context, player, "minion_bloodfen_raptor");
            playCardWithTarget(context, player, "spell_mystic_skull", bloodfenRaptor);
            Assert.assertEquals(player.getHand().get(0).getCardId(), "minion_bloodfen_raptor");
            Minion newBloodfenRaptor = playMinionCard(context, player, (MinionCard) player.getHand().get(0));
            Assert.assertEquals(newBloodfenRaptor.getAttack(), 5);
        });
    }

    @Test
    public void testGiantDisappointment() {
        runGym((context, player, opponent) -> {
            Card card = CardCatalogue.getCardById("minion_giant_disappointment");
            context.getLogic().receiveCard(player.getId(), card);
            Assert.assertEquals(context.getLogic().getModifiedManaCost(player, card), 8);
        });
    }

    @Test
    public void testPowerTrip() {
        // We reach turn 10 so we have 10 mana, we die
        runGym((context, player, opponent) -> {
            playCard(context, player, "spell_power_trip");
            Assert.assertEquals(player.getQuests().get(0).getSourceCard().getCardId(), "spell_power_trip");
            for (int i = 0; i < 10; i++) {
                context.endTurn();
                context.endTurn();
            }
            Assert.assertTrue(context.getLogic().getMatchResult(player, opponent) != GameStatus.RUNNING);
        });

        // Our opponent gives us 10 mana somehow, we die
        runGym((context, player, opponent) -> {
            playCard(context, player, "spell_power_trip");
            Assert.assertEquals(player.getQuests().get(0).getSourceCard().getCardId(), "spell_power_trip");
            for (int i = 0; i < 2; i++) {
                context.endTurn();
                context.endTurn();
            }
            context.endTurn();
            Assert.assertEquals(player.getMaxMana(), 3);
            for (int i = 0; i < 7; i++) {
                playCard(context, opponent, "minion_arcane_golem");
                Assert.assertEquals(player.getMaxMana(), 3 + i + 1);
            }
            Assert.assertEquals(player.getMaxMana(), 10);
            Assert.assertTrue(context.getLogic().getMatchResult(player, opponent) != GameStatus.RUNNING);
        });

        // Check that minions have +1/+1
        runGym((context, player, opponent) -> {
            playCard(context, player, "spell_power_trip");
            Minion bloodfenRaptor = playMinionCard(context, player, "minion_bloodfen_raptor");
            Assert.assertEquals(bloodfenRaptor.getAttack(), bloodfenRaptor.getBaseAttack() + 1);
            Assert.assertEquals(bloodfenRaptor.getHp(), bloodfenRaptor.getBaseHp() + 1);
            // Broken with Mind Control...
            // TODO: Implement an AddAuraSpell
        });
    }
}
