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

    @Test
    public void testBunglingBusker() {
        runGym((context, player, opponent) -> {
            Minion busker = playMinionCard(context, player, "minion_bungling_busker");
            Minion bad = playMinionCard(context, opponent, "minion_neutral_test");
            playCard(context, player, "spell_prestidigitation", bad);
            assertEquals(bad.getHp(), bad.getMaxHp() - 4);
            assertEquals(busker.getHp(), busker.getMaxHp() - 4);
            assertEquals(player.getMinions().size(), 2);
        });
    }

    @Test
    public void testRoadTrip() {
        runGym((context, player, opponent) -> {
            player.setAttribute(Attribute.SIGNATURE, "spell_road_trip");
            player.getHero().setHp(1);
            playCard(context, player, "spell_road_trip", player.getHero());
            assertEquals(player.getHero().getHp(), 1 + 4);
            playCard(context, player, "spell_road_trip", player.getHero());
            assertEquals(player.getHero().getHp(), 1 + 4 + 8);
            playCard(context, player, "spell_initial_act");
            assertEquals(player.getHero().getHp(), 1 + 4 + 8);
        });
    }

    @Test
    public void testSavageDancer() {
        runGym((context, player, opponent) -> {
            player.setAttribute(Attribute.SIGNATURE, "spell_meteor_spin");
            Minion leftLeft = playMinionCard(context, opponent, "minion_neutral_test");
            Minion left = playMinionCard(context, opponent, "minion_neutral_test");
            Minion middle = playMinionCard(context, opponent, "minion_neutral_test");
            Minion right = playMinionCard(context, opponent, "minion_neutral_test");
            Minion rightRight = playMinionCard(context, opponent, "minion_neutral_test");

            playCard(context, player, "minion_savage_dancer");
            playCard(context, player, "spell_show_planning");
            playCard(context, player, player.getHand().get(0), middle);

            assertEquals(leftLeft.getHp(), leftLeft.getMaxHp());
            assertEquals(left.getHp(), left.getMaxHp() - 6);
            assertEquals(middle.getHp(), leftLeft.getMaxHp() - 6);
            assertEquals(right.getHp(), right.getMaxHp() - 6);
            assertEquals(rightRight.getHp(), rightRight.getMaxHp());

            assertEquals(opponent.getHero().getHp(), opponent.getHero().getMaxHp() - 6);

            context.getLogic().drawCard(player.getId(), null);

            playCard(context, player, player.getHand().get(0), leftLeft);
            assertEquals(opponent.getHero().getHp(), opponent.getHero().getMaxHp() - 6);
        });
    }

    @Test
    public void testGreatShowman() {
        runGym((context, player, opponent) -> {
            player.setAttribute(Attribute.SIGNATURE, "spell_chain_dance");
            for (int i = 0; i < 10; i++) {
                shuffleToDeck(context, player, "minion_neutral_test");
            }
            assertEquals(player.getDeck().size(), 10);
            playCard(context, player, "minion_great_showman");
            assertEquals(player.getDeck().size(), 9);
            assertEquals(player.getHand().size(), 1);

            shuffleToDeck(context, player, "spell_chain_dance");
            playCard(context, player, "minion_great_showman");

            assertEquals(player.getDeck().size(), 9);
            assertEquals(player.getHand().size(), 2);
            assertEquals(player.getHand().get(1).getCardId(), "spell_chain_dance");
        });
    }

    @Test
    public void testFiredancer() {
        runGym((context, player, opponent) -> {
            player.setAttribute(Attribute.SIGNATURE, "spell_chain_dance");
            Minion leftLeft = playMinionCard(context, opponent, "minion_neutral_test");
            Minion left = playMinionCard(context, opponent, "minion_neutral_test");
            Minion middle = playMinionCard(context, opponent, "minion_neutral_test");
            Minion right = playMinionCard(context, opponent, "minion_neutral_test");
            Minion rightRight = playMinionCard(context, opponent, "minion_neutral_test");

            playCard(context, player, "minion_firedancer");
            playCard(context, player, "spell_show_planning");
            playCard(context, player, player.getHand().get(0), middle);

            assertEquals(leftLeft.getHp(), leftLeft.getMaxHp() - 1);
            assertEquals(left.getHp(), left.getMaxHp() - 4);
            assertEquals(middle.getHp(), leftLeft.getMaxHp() - 5);
            assertEquals(right.getHp(), right.getMaxHp() - 4);
            assertEquals(rightRight.getHp(), rightRight.getMaxHp() - 1);

            context.getLogic().drawCard(player.getId(), null);

            playCard(context, player, player.getHand().get(0), leftLeft);
            assertEquals(leftLeft.getHp(), leftLeft.getMaxHp() - 4);
            assertEquals(rightRight.getHp(), rightRight.getMaxHp() - 2);
        });
    }

    @Test
    public void testGazalTheGlorious() {
        runGym((context, player, opponent) -> {
            player.setAttribute(Attribute.SIGNATURE, "spell_jaunty_tune");
            playCard(context, player, "minion_gazal_the_glorious");
            playCard(context, player, "spell_jaunty_tune");

            for (int i = 0; i < 4; i++) {
                assertEquals(player.getMinions().get(i).getHp(), player.getMinions().get(i).getBaseHp() + 2);
            }

            playCard(context, player, "spell_jaunty_tune");

            for (int i = 0; i < 4; i++) {
                assertEquals(player.getMinions().get(i).getHp(), player.getMinions().get(i).getBaseHp() + 2);
            }
            for (int i = 4; i < 7; i++) {
                assertEquals(player.getMinions().get(i).getHp(), player.getMinions().get(i).getBaseHp());
            }
        });
    }

    @Test
    public void testTalentScout() {
        runGym((context, player, opponent) -> {
            Minion mine = playMinionCard(context, player, "minion_neutral_test_1");
            Minion yours = playMinionCard(context, opponent, "minion_neutral_test_1");
            playCard(context, player, "minion_talent_scout", mine);
            playCard(context, player, "minion_talent_scout", yours);
            assertEquals(opponent.getMinions().size(), 1);
            assertEquals(player.getMinions().size(), 5);
        });
    }
}
