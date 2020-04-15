package com.hiddenswitch.spellsource.tests.cards;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

public class SummonerTests extends TestBase {

    @NotNull
    @Override
    public String getDefaultHeroClass() {
        return "DARKMAGENTA";
    }

    @Test
    public void testStoneshaper() {
        runGym((context, player, opponent) -> {
            player.setMana(10);
            overrideDiscover(context, player, "invoke_minion_stoneshaper_dont_invoke");
            playCard(context, player, "minion_stoneshaper");
            assertEquals(1, player.getMinions().size());
        });

        runGym((context, player, opponent) -> {
            player.setMana(10);
            overrideDiscover(context, player, "invoke_minion_stoneshaper_shape_some_stone");
            playCard(context, player, "minion_stoneshaper");
            assertEquals(3, player.getMinions().size());
        });

        runGym((context, player, opponent) -> {
            overrideDiscover(context, player, lol -> {
                throw new AssertionError();
            });
            playCard(context, player, "minion_stoneshaper");
            assertEquals(1, player.getMinions().size());
        });
    }

    @Test
    public void testBroodcaller() {
        runGym((context, player, opponent) -> {
            player.setMana(10);
            overrideDiscover(context, player, "invoke_minion_broodcaller_dont_invoke");
            playCard(context, player, "minion_broodcaller");
            assertEquals(3, player.getMinions().size());
            assertFalse(player.getMinions().get(0).hasAttribute(Attribute.CHARGE));
            assertFalse(player.getMinions().get(2).hasAttribute(Attribute.CHARGE));
        });

        runGym((context, player, opponent) -> {
            player.setMana(10);
            overrideDiscover(context, player, "invoke_minion_broodcaller_brood_awakening");
            playCard(context, player, "minion_broodcaller");
            assertEquals(3, player.getMinions().size());
            assertTrue(player.getMinions().get(0).hasAttribute(Attribute.CHARGE));
            assertTrue(player.getMinions().get(2).hasAttribute(Attribute.CHARGE));
        });

        runGym((context, player, opponent) -> {
            player.setMana(2);
            overrideDiscover(context, player, lol -> {
                throw new AssertionError();
            });
            playCard(context, player, "minion_broodcaller");
            assertEquals(3, player.getMinions().size());
            assertFalse(player.getMinions().get(0).hasAttribute(Attribute.CHARGE));
            assertFalse(player.getMinions().get(2).hasAttribute(Attribute.CHARGE));
        });
    }

    @Test
    public void testTwintailedFox() {
        runGym((context, player, opponent) -> {
            player.setMana(10);
            overrideDiscover(context, player, "invoke_minion_twintailed_fox_dont_invoke");
            Minion guy = playMinionCard(context, opponent, "minion_neutral_test");
            playCard(context, player, "minion_twintailed_fox", guy);
            assertEquals(0, player.getHand().size());
            assertEquals(6, player.getMana());
        });

        runGym((context, player, opponent) -> {
            player.setMana(10);
            overrideDiscover(context, player, "invoke_minion_twintailed_fox_twin_tails");
            Minion guy = playMinionCard(context, opponent, "minion_neutral_test");
            playCard(context, player, "minion_twintailed_fox", guy);
            assertEquals(1, player.getHand().size());
            assertEquals(4, player.getMana());
        });

        runGym((context, player, opponent) -> {
            overrideDiscover(context, player, lol -> {
                throw new AssertionError();
            });
            playCard(context, player, "minion_twintailed_fox");
            assertEquals(0, player.getHand().size());
        });

    }

}
