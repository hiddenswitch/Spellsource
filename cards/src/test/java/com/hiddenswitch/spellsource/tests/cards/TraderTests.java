package com.hiddenswitch.spellsource.tests.cards;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.FixedCardsDeckFormat;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.events.GameStartEvent;
import net.demilich.metastone.game.targeting.Zones;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TraderTests extends TestBase {

	@Override
	public @NotNull String getDefaultHeroClass() {
		return HeroClass.MAGENTA;
	}

	@Test
	public void testUusyaiTheIllustrious() {
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "minion_uusyai_the_illustrious");
			context.fireGameEvent(new GameStartEvent(context, player.getId()));
			Card legendary = receiveCard(context, player, "minion_test_legendary");
			final int legendaryCost = costOf(context, player, legendary);
			assertEquals(legendary.getBaseManaCost() - 1, legendaryCost, "cost reduction");
			context.setDeckFormat(new FixedCardsDeckFormat("spell_test_gain_1_mana"));
			player.setMana(legendaryCost);
			playCard(context, player, legendary);
			assertEquals(1, player.getMana(), "gained 1 mana from randomly casting a card");
		});
	}

	@Test
	public void testBelligerentBouncer() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_gold_coin");
			Card discountPlatinum = receiveCard(context, player, "token_spell_platinum_coin");
			assertEquals(discountPlatinum.getBaseManaCost() - 1, costOf(context, player, discountPlatinum), "should be discounted");
			playCard(context, player, CardCatalogue.getOneOneNeutralMinionCardId());
			assertEquals(discountPlatinum.getBaseManaCost(), costOf(context, player, discountPlatinum), "playing card expires trigger");
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_belligerent_bouncer");
			playCard(context, player, "spell_gold_coin");
			Card notDiscountedPlatinum = receiveCard(context, player, "token_spell_platinum_coin");
			assertEquals(notDiscountedPlatinum.getBaseManaCost(), costOf(context, player, notDiscountedPlatinum), "should NOT be discounted");
			playCard(context, player, notDiscountedPlatinum);
			Card stackedDiscounts = receiveCard(context, player, "spell_test_cost_4");
			assertEquals(stackedDiscounts.getBaseManaCost() - 4, costOf(context, player, stackedDiscounts), "discounts should stack");
		});
	}

	@Test
	public void testRoheiTheBold() {
		runGym((context, player, opponent) -> {
			String[] cardIds = CardCatalogue.getCardById("spell_test_discover1").getDesc().getSpell().getCards();
			String[] selected = new String[1];
			overrideDiscover(context, player, discoverActions -> {
				selected[0] = discoverActions.get(0).getCard().getCardId();
				return discoverActions.get(0);
			});
			playCard(context, player, "minion_rohei_the_bold");
			playCard(context, player, "spell_test_discover1");
			assertEquals(1, player.getHand().size());
			assertEquals(selected[0], player.getHand().get(0).getCardId());
			assertEquals(2, player.getDeck().size());
			assertTrue(player.getDeck().containsCard(cardIds[1]));
			assertTrue(player.getDeck().containsCard(cardIds[2]));
		});
	}

	@Test
	public void testXiidaTheCurious() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_xiida_the_curious");
			Minion target = playMinionCard(context, player, 3, 3);
			playCard(context, player, "spell_test_deal_1", target);
			assertEquals(target.getMaxHp() - 2, target.getHp(), "spell played again on same target");
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_xiida_the_curious");
			Minion target = playMinionCard(context, player, 3, 3);
			playMinionCard(context, player, "minion_test_deal_1", target);
			assertEquals(target.getMaxHp() - 2, target.getHp(), "battlecry played again on same target");
		});
	}

	@Test
	public void testOooShiny() {
		runGym((context, player, opponent) -> {
			Card goldCoin = receiveCard(context, player, "spell_gold_coin");
			assertEquals("spell_gold_coin", goldCoin.getCardId());
			playCard(context, player, "spell_oooh_shiny");
			assertEquals("token_spell_platinum_coin", goldCoin.getCardId());
			player.setMana(1);
			playCard(context, player, (Card) goldCoin.transformResolved(context));
			assertEquals(0, player.getMana(), "platinum cost paid");
			Card shouldBeDiscounted = receiveCard(context, player, "minion_cost_4_test");
			assertEquals(shouldBeDiscounted.getBaseManaCost() - 3, costOf(context, player, shouldBeDiscounted), "should be platinum effects");
		});
	}

	@Test
	public void testHoardingWhelp() {
		runGym((context, player, opponent) -> {
			Card shouldDraw1 = putOnTopOfDeck(context, player, CardCatalogue.getOneOneNeutralMinionCardId());
			Card shouldDraw2 = putOnTopOfDeck(context, opponent, CardCatalogue.getOneOneNeutralMinionCardId());
			useHeroPower(context, player);
			assertEquals(Zones.HAND, shouldDraw1.getZone());
			assertEquals(Zones.HAND, shouldDraw2.getZone());
		});
		runGym((context, player, opponent) -> {
			Card shouldDraw = putOnTopOfDeck(context, player, CardCatalogue.getOneOneNeutralMinionCardId());
			Card shouldNotDraw = putOnTopOfDeck(context, opponent, CardCatalogue.getOneOneNeutralMinionCardId());
			Minion whelp = playMinionCard(context, player, "minion_hoarding_whelp");
			useHeroPower(context, player);
			assertEquals(Zones.DECK, shouldNotDraw.getZone());
			assertEquals(Zones.HAND, shouldDraw.getZone());
		});
	}

	@Test
	public void testMenagerieMogul() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_menagerie_mogul");
			Minion customer = player.getMinions().get(0);
			assertEquals("token_customer", customer.getSourceCard().getCardId());
			playCard(context, player, "spell_test_buff_dragons");
			assertEquals(customer.getBaseAttack() + 1, customer.getAttack(), "should buff customers counted as dragons");
		});
	}
}
