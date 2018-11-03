package com.blizzard.hearthstone;

import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.actions.PhysicalAttackAction;
import net.demilich.metastone.game.actions.PlaySpellCardAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.DamageSpell;
import net.demilich.metastone.game.spells.DestroySpell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.tests.util.TestBase;
import net.demilich.metastone.tests.util.TestMinionCard;
import net.demilich.metastone.tests.util.TestSpellCard;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;

public class ClassicTests extends TestBase {

	@Test
	public void testGadgetzanAuctioneer() {
		runGym((context, player, opponent) -> {
			Card shouldBeDrawn = putOnTopOfDeck(context, player, "minion_bloodfen_raptor");
			Minion auctioneer = playMinionCard(context, player, "minion_gadgetzan_auctioneer");
			playCardWithTarget(context, player, "spell_polymorph", auctioneer);
			assertEquals(shouldBeDrawn.getZone(), Zones.HAND);
			Card shouldNotBeDrawn = putOnTopOfDeck(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "spell_mirror_image");
			assertEquals(shouldNotBeDrawn.getZone(), Zones.DECK);
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			playCard(context, opponent, "secret_potion_of_polymorph");
			context.endTurn();
			Minion transformed = (Minion) playMinionCard(context, player, "minion_gadgetzan_auctioneer").transformResolved(context);
			Card shouldNotBeDrawn = putOnTopOfDeck(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "spell_mirror_image");
			assertEquals(shouldNotBeDrawn.getZone(), Zones.DECK);
		});

		runGym((context, player, opponent) -> {
			Card shouldBeDrawn = putOnTopOfDeck(context, player, "minion_bloodfen_raptor");
			Minion auctioneer = playMinionCard(context, player, "minion_gadgetzan_auctioneer");
			playCardWithTarget(context, player, "spell_assassinate", auctioneer);
			assertEquals(shouldBeDrawn.getZone(), Zones.HAND);
		});
	}

	@Test
	public void testSouthseaDeckhand() {
		runGym((context, player, opponent) -> {
			Minion deckhand = playMinionCard(context, player, "minion_southsea_deckhand");
			Assert.assertFalse(deckhand.canAttackThisTurn());
			playCard(context, player, "weapon_wicked_knife");
			Assert.assertTrue(deckhand.canAttackThisTurn());
			destroy(context, player.getHero().getWeapon());
			assertEquals(player.getHero().getWeapon(), null);
			Assert.assertFalse(deckhand.canAttackThisTurn());
		});
	}

	@Test
	public void testColdlightOracle() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 2; i++) {
				putOnTopOfDeck(context, player, "minion_bloodfen_raptor");
			}

			for (int i = 0; i < 10; i++) {
				receiveCard(context, player, "minion_bloodfen_raptor");
			}

			GameLogic spyLogic = spy(context.getLogic());
			context.setLogic(spyLogic);

			doAnswer(answer -> {
				final Card card = answer.getArgument(1);
				if (card.getSourceCard().getCardId().equals("minion_coldlight_oracle")) {
					return answer.callRealMethod();
				}
				assertEquals(card.getZone(), Zones.DECK);
				return answer.callRealMethod();
			}).when(spyLogic).discardCard(any(), any());
			playCard(context, player, "minion_coldlight_oracle");
		});
	}

	@Test
	public void testDivineShieldZeroAttackDefenderInteraction() {
		runGym((context, player, opponent) -> {
			Minion attacker = playMinionCard(context, player, "minion_shielded_minibot");
			context.endTurn();
			Minion defender = playMinionCard(context, opponent, "minion_flametongue_totem");
			context.endTurn();
			attack(context, player, attacker, defender);
			Assert.assertTrue(attacker.hasAttribute(Attribute.DIVINE_SHIELD));
		});
	}

	@Test
	public void testExplosiveTrapWeaponSituation() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_explosive_trap");
			Minion taunt = playMinionCard(context, player, "minion_sleepy_dragon");
			context.endTurn();
			// Start of opponent's turn
			int opponentHpBefore = opponent.getHero().getHp();
			assertEquals(player.getSecrets().size(), 1);

			playCard(context, opponent, "weapon_wicked_knife");
			playCardWithTarget(context, opponent, "spell_sap", taunt);
			attack(context, opponent, opponent.getHero(), player.getHero());
			assertEquals(player.getSecrets().size(), 0);
			assertEquals(opponent.getHero().getHp(), opponentHpBefore - 2);
		});
	}

	@Test
	public void testPerditionsBlade() {
		runGym((context, player, opponent) -> {
			AtomicBoolean cried = new AtomicBoolean(false);
			overrideBattlecry(context, player, battlecryActions -> {
				assertEquals(battlecryActions.size(), 2);
				assertEquals(battlecryActions.stream().filter(ba -> ba.getTargetReference().equals(player.getHero().getReference())).count(), 1L);
				assertEquals(battlecryActions.stream().filter(ba -> ba.getTargetReference().equals(opponent.getHero().getReference())).count(), 1L);
				cried.set(true);
				return battlecryActions.get(0);
			});

			playCard(context, player, "weapon_perditions_blade");
			Assert.assertTrue(cried.get());
			assertEquals(player.getWeaponZone().get(0).getSourceCard().getCardId(), "weapon_perditions_blade");
		});

		runGym((context, player, opponent) -> {
			AtomicBoolean cried = new AtomicBoolean(false);
			overrideBattlecry(context, player, battlecryActions -> {
				assertEquals(battlecryActions.stream().filter(ba -> ba.getTargetReference().equals(player.getHero().getReference())).count(), 1L);
				assertEquals(battlecryActions.stream().filter(ba -> ba.getTargetReference().equals(opponent.getHero().getReference())).count(), 1L);
				cried.set(true);
				return battlecryActions.get(0);
			});

			playCard(context, player, "spell_the_coin");
			playCard(context, player, "weapon_perditions_blade");
			Assert.assertTrue(cried.get());
			assertEquals(player.getWeaponZone().get(0).getSourceCard().getCardId(), "weapon_perditions_blade");
		});
	}

	@Test
	public void testSpellbender() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_spellbender");
			context.endTurn();
			playCardWithTarget(context, opponent, "spell_fireball", player.getHero());
			assertEquals(player.getSecrets().size(), 1);
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_spellbender");
			Minion bloodfen = playMinionCard(context, player, "minion_bloodfen_raptor");
			context.endTurn();
			playCardWithTarget(context, opponent, "spell_fireball", bloodfen);
			assertEquals(player.getSecrets().size(), 0);
			assertEquals(player.getMinions().size(), 1);
			Assert.assertFalse(bloodfen.isDestroyed());
			Assert.assertTrue(player.getGraveyard().stream().anyMatch(e -> e.getSourceCard().getCardId().equals("token_spellbender")));
		});
	}

	@Test
	public void testYsera() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_ysera");
			overrideRandomCard(context, "spell_dream");
			context.endTurn();
			assertEquals(player.getHand().get(0).getCardId(), "spell_dream");
			assertEquals(player.getHand().size(), 1);
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_ysera");
			Stream<String> yseraCards = Arrays.stream((String[]) ((CardDesc) CardCatalogue.getRecords().get("minion_ysera")
					.getDesc()).getTrigger().spell.get(SpellArg.CARDS));
			context.endTurn();
			Assert.assertTrue(yseraCards.anyMatch(c -> c.equals(player.getHand().get(0).getCardId())));
			assertEquals(player.getHand().size(), 1);
		});
	}

	@Test
	public void testShadowstep() {
		runGym((context, player, opponent) -> {
			Minion bloodfen = playMinionCard(context, player, "minion_bloodfen_raptor");
			playCardWithTarget(context, player, "spell_shadowstep", bloodfen);
			assertEquals(costOf(context, player, player.getHand().get(0)), 0);
		});
	}

	@Test
	public void testPreparation() {
		runGym((context, player, opponent) -> {
			Card inHand1 = receiveCard(context, player, "spell_fireball");
			Card inHand2 = receiveCard(context, player, "spell_fireball");
			playCard(context, player, "spell_preparation");
			assertEquals(costOf(context, player, inHand1), 1);
			assertEquals(costOf(context, player, inHand2), 1);
			playCardWithTarget(context, player, inHand1, opponent.getHero());
			assertEquals(costOf(context, player, inHand2), 4);
		});
	}

	@Test
	public void testMillhouseManastorm() {
		runGym((context, player, opponent) -> {
			Card inHand = receiveCard(context, opponent, "spell_fireball");
			playCard(context, player, "minion_millhouse_manastorm");
			context.endTurn();
			Card fireball = receiveCard(context, opponent, "spell_fireball");
			assertEquals(costOf(context, opponent, inHand), 0);
			assertEquals(costOf(context, opponent, fireball), 0);
			context.endTurn();
			assertEquals(costOf(context, opponent, inHand), 4);
			assertEquals(costOf(context, opponent, fireball), 4);
			context.endTurn();
			assertEquals(costOf(context, opponent, inHand), 4);
			assertEquals(costOf(context, opponent, fireball), 4);
		});
	}

	@Test
	public void testFreezingTrap() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_freezing_trap");
			context.endTurn();
			Minion bloodfen = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			context.endTurn();
			int startHp = player.getHero().getHp();
			attack(context, opponent, bloodfen, player.getHero());
			assertEquals(player.getHero().getHp(), startHp);
			assertEquals(opponent.getMinions().size(), 0);
			assertEquals(costOf(context, opponent, opponent.getHand().get(0)), 4);
		});
	}

	@Test
	public void testShadowWordConditions() {
		runGym((context, player, opponent) -> {
			final Card pain = CardCatalogue.getCardById("spell_shadow_word_pain");
			context.getLogic().receiveCard(player.getId(), pain);
			final Card death = CardCatalogue.getCardById("spell_shadow_word_death");
			context.getLogic().receiveCard(player.getId(), death);
			player.setMaxMana(5);
			player.setMana(5);
			Assert.assertFalse(context.getValidActions().stream().anyMatch(ga ->
					ga instanceof PlaySpellCardAction
							&& ((PlaySpellCardAction) ga).getSourceCardEntityId().equals(pain.getReference())));
			Assert.assertFalse(context.getValidActions().stream().anyMatch(ga ->
					ga instanceof PlaySpellCardAction
							&& ((PlaySpellCardAction) ga).getSourceCardEntityId().equals(death.getReference())));

			context.endTurn();
			playCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			Assert.assertTrue(context.getValidActions().stream().anyMatch(ga ->
					ga instanceof PlaySpellCardAction
							&& ((PlaySpellCardAction) ga).getSourceCardEntityId().equals(pain.getReference())));
			Assert.assertFalse(context.getValidActions().stream().anyMatch(ga ->
					ga instanceof PlaySpellCardAction
							&& ((PlaySpellCardAction) ga).getSourceCardEntityId().equals(death.getReference())));
			context.endTurn();
			playCard(context, opponent, "minion_sea_giant");
			context.endTurn();
			Assert.assertTrue(context.getValidActions().stream().anyMatch(ga ->
					ga instanceof PlaySpellCardAction
							&& ((PlaySpellCardAction) ga).getSourceCardEntityId().equals(pain.getReference())));
			Assert.assertTrue(context.getValidActions().stream().anyMatch(ga ->
					ga instanceof PlaySpellCardAction
							&& ((PlaySpellCardAction) ga).getSourceCardEntityId().equals(death.getReference())));
		});
	}

	@Test
	public void testNourish() {
		runGym((context, player, opponent) -> {
			player.setMana(5);
			player.setMaxMana(5);
			playChooseOneCard(context, player, "spell_nourish", "spell_nourish_1");
			assertEquals(player.getMaxMana(), 7);
			assertEquals(player.getMana(), 2);
		});

		runGym((context, player, opponent) -> {
			for (int i = 0; i < 3; i++) {
				shuffleToDeck(context, player, "minion_bloodfen_raptor");
			}
			player.setMana(5);
			player.setMaxMana(5);
			playChooseOneCard(context, player, "spell_nourish", "spell_nourish_2");
			assertEquals(player.getMaxMana(), 5);
			assertEquals(player.getMana(), 0);
			assertEquals(player.getHand().size(), 3);
		});

		runGym((context, player, opponent) -> {
			for (int i = 0; i < 3; i++) {
				shuffleToDeck(context, player, "minion_bloodfen_raptor");
			}
			player.setMana(5);
			player.setMaxMana(5);
			Card nourish = CardCatalogue.getCardById("spell_nourish");
			playCard(context, player, nourish.getChooseBothCardId());
			assertEquals(player.getMaxMana(), 7);
			assertEquals(player.getMana(), 2);
			assertEquals(player.getHand().size(), 3);
		});
	}

	@Test
	public void testTracking() {
		for (int i = 0; i <= 3; i++) {
			final int i1 = i;
			runGym((context, player, opponent) -> {
				Collections.nCopies(i1, "minion_bloodfen_raptor")
						.forEach(cid -> context.getLogic().shuffleToDeck(player, CardCatalogue.getCardById(cid)));

				playCard(context, player, "spell_tracking");
				assertEquals(player.getDeck().size(), 0);
				if (i1 > 0) {
					assertEquals(player.getHand().get(0).getCardId(), "minion_bloodfen_raptor");
				} else {
					assertEquals(player.getHand().size(), 0);
				}
			});
		}
	}

	@Test
	public void testMisdirection() {
		// Opponent's face gets hit always by misdirection
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_misdirection");
			context.endTurn();
			int startingHp = opponent.getHero().getHp();
			Minion boar = playMinionCard(context, opponent, "minion_stonetusk_boar");
			attack(context, opponent, boar, player.getHero());
			assertEquals(player.getSecrets().size(), 0);
			assertEquals(opponent.getHero().getHp(), startingHp - 1);
		});

		// Adjacent minion can get hit by misdirection
		runGym((context, player, opponent) -> {
			context.setLogic(spy(context.getLogic()));

			playCard(context, player, "secret_misdirection");
			context.endTurn();
			Minion boar = playMinionCard(context, opponent, "minion_stonetusk_boar");
			Minion boar2 = playMinionCard(context, opponent, "minion_stonetusk_boar");
			Mockito.doReturn(boar2)
					.when(context.getLogic())
					.getAnotherRandomTarget(any(), any(), any(), any());
			attack(context, opponent, boar, player.getHero());
			assertEquals(player.getSecrets().size(), 0);
			assertEquals(opponent.getMinions().size(), 0);
		});
	}

	@Test
	public void testBlizzard() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion impGangBoss = playMinionCard(context, opponent, CardCatalogue.getCardById("minion_imp_gang_boss"));
			context.endTurn();

			playCard(context, player, "spell_blizzard");

			assertEquals(impGangBoss.getHp(), impGangBoss.getMaxHp() - 2);
			for (Minion minion : opponent.getMinions()) {
				assertEquals(minion.hasAttribute(Attribute.FROZEN), true);
			}
		}, HeroClass.BLUE, HeroClass.VIOLET);
	}

	@Test
	public void testHauntedCreeperHarvestGolem() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			playMinionCard(context, opponent, CardCatalogue.getCardById("minion_haunted_creeper"));
			playMinionCard(context, opponent, CardCatalogue.getCardById("minion_harvest_golem"));
			assertEquals(opponent.getMinions().size(), 2);
			context.endTurn();

			playCard(context, player, "spell_flamestrike");
			assertEquals(opponent.getMinions().size(), 3);
			final int HARVEST_GOLEM = 1;
			for (int i = 0; i < opponent.getMinions().size(); i++) {
				Minion minion = opponent.getMinions().get(i);
				if (i == HARVEST_GOLEM) {
					assertEquals(minion.getAttack(), 2);
					assertEquals(minion.getHp(), 1);
					assertEquals(minion.getRace(), Race.MECH);
				} else {
					assertEquals(minion.getAttack(), 1);
					assertEquals(minion.getHp(), 1);
				}
			}
		}, HeroClass.BLUE, HeroClass.RED);
	}


	@Test
	public void testElvenArcher() {
		runGym((context, player, opponent) -> {
			assertEquals(opponent.getHero().getHp(), GameLogic.MAX_HERO_HP);
			playCardWithTarget(context, player, "minion_elven_archer", opponent.getHero());
			assertEquals(opponent.getHero().getHp(), GameLogic.MAX_HERO_HP - 1);
		}, HeroClass.RED, HeroClass.SILVER);
	}

	@Test
	public void testNoviceEngineer() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_bloodfen_raptor");
			int cardCount = player.getHand().getCount();
			assertEquals(player.getHand().getCount(), cardCount);
			playCard(context, player, "minion_novice_engineer");
			assertEquals(player.getHand().getCount(), cardCount + 1);
		}, HeroClass.RED, HeroClass.SILVER);
	}

	@Test
	public void testKoboldGeomancer() {
		runGym((context, player, opponent) -> {
			assertEquals(opponent.getHero().getHp(), GameLogic.MAX_HERO_HP);
			playCard(context, player, "spell_arcane_missiles");
			assertEquals(opponent.getHero().getHp(), GameLogic.MAX_HERO_HP - 3);
			playCardWithTarget(context, player, "spell_fireball", opponent.getHero());
			assertEquals(opponent.getHero().getHp(), GameLogic.MAX_HERO_HP - 3 - 6);

			playCard(context, player, "minion_kobold_geomancer");

			playCard(context, player, "spell_arcane_missiles");
			assertEquals(opponent.getHero().getHp(), GameLogic.MAX_HERO_HP - 3 - 6 - 4);
			playCardWithTarget(context, player, "spell_fireball", opponent.getHero());
			assertEquals(opponent.getHero().getHp(), GameLogic.MAX_HERO_HP - 3 - 6 - 4 - 7);
		}, HeroClass.BLUE, HeroClass.RED);
	}

	@Test
	public void testAcidicSwampOoze() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			playCard(context, opponent, "weapon_fiery_war_axe");
			Assert.assertNotNull(opponent.getHero().getWeapon());
			context.endTurn();

			playCard(context, player, "minion_acidic_swamp_ooze");
			Assert.assertNull(opponent.getHero().getWeapon());
		}, HeroClass.BLUE, HeroClass.RED);
	}

	@Test
	public void testWildPyromancer() {
		runGym((context, player, opponent) -> {
			playCard(context, opponent, "minion_haunted_creeper");
			playCard(context, player, "minion_wild_pyromancer");
			assertEquals(opponent.getMinions().size(), 1);
			playCard(context, player, "spell_holy_nova");

			// the warriors board should be completely wiped, as the Holy Nova
			// should kill the
			// first body of Haunted Creeper, the Deathrattle resolves and then Wild
			// Pyromancer
			// triggers, clearing the two 1/1 Spectral Spiders
			assertEquals(opponent.getMinions().size(), 0);
		}, HeroClass.WHITE, HeroClass.RED);
	}

	@Test
	public void testBetrayal() {
		runGym((context, player, opponent) -> {
			Card adjacentCard1 = new TestMinionCard(1, 5, 0);
			Minion adjacentMinion1 = playMinionCard(context, player, adjacentCard1);

			Card targetCard = new TestMinionCard(3, 1, 0);
			Minion targetMinion = playMinionCard(context, player, targetCard);

			Card adjacentCard2 = new TestMinionCard(1, 5, 0);
			Minion adjacentMinion2 = playMinionCard(context, player, adjacentCard2);

			context.getLogic().endTurn(player.getId());

			assertEquals(player.getMinions().size(), 3);

			Player rogue = context.getPlayer2();
			Card betrayal = CardCatalogue.getCardById("spell_betrayal");

			context.getLogic().receiveCard(rogue.getId(), betrayal);
			GameAction action = betrayal.play();
			action.setTarget(targetMinion);
			context.getLogic().performGameAction(rogue.getId(), action);
			assertEquals(targetMinion.getAttack(), 3);
			assertEquals(targetMinion.getHp(), 1);

			assertEquals(adjacentMinion1.getHp(), 2);
			assertEquals(adjacentMinion2.getHp(), 2);

			assertEquals(player.getMinions().size(), 3);
		}, HeroClass.GOLD, HeroClass.BLACK);

	}

	@Test
	public void testBetrayalOnEmperorCobraDestroysAdjacentMinions() {
		runGym((context, player, opponent) -> {
			Card adjacentCard1 = new TestMinionCard(1, 5, 0);
			playMinionCard(context, player, adjacentCard1);

			Card targetCard = CardCatalogue.getCardById("minion_emperor_cobra");
			Minion targetMinion = playMinionCard(context, player, targetCard);

			Card adjacentCard2 = new TestMinionCard(1, 5, 0);
			playMinionCard(context, player, adjacentCard2);

			context.getLogic().endTurn(player.getId());

			assertEquals(player.getMinions().size(), 3);

			Player rogue = context.getPlayer2();

			Card betrayal = CardCatalogue.getCardById("spell_betrayal");

			context.getLogic().receiveCard(rogue.getId(), betrayal);
			GameAction action = betrayal.play();
			action.setTarget(targetMinion);
			context.getLogic().performGameAction(rogue.getId(), action);

			assertEquals(player.getMinions().size(), 1);
		}, HeroClass.GOLD, HeroClass.BLACK);
	}


	@Test
	public void testBetrayalNotAffectedBySpellDamage() {
		runGym((context, player, opponent) -> {

			Card adjacentCard1 = new TestMinionCard(1, 5, 0);
			Minion adjacentMinion1 = playMinionCard(context, player, adjacentCard1);

			Card targetCard = new TestMinionCard(3, 1, 0);
			Minion targetMinion = playMinionCard(context, player, targetCard);

			Card adjacentCard2 = new TestMinionCard(1, 5, 0);
			Minion adjacentMinion2 = playMinionCard(context, player, adjacentCard2);

			context.getLogic().endTurn(player.getId());

			Player rogue = context.getPlayer2();

			playMinionCard(context, rogue, "minion_azure_drake");

			Card betrayal = CardCatalogue.getCardById("spell_betrayal");

			context.getLogic().receiveCard(rogue.getId(), betrayal);
			GameAction action = betrayal.play();
			action.setTarget(targetMinion);
			context.getLogic().performGameAction(rogue.getId(), action);
			assertEquals(targetMinion.getAttack(), 3);
			assertEquals(targetMinion.getHp(), 1);

			assertEquals(adjacentMinion1.getHp(), 2);
			assertEquals(adjacentMinion2.getHp(), 2);
		}, HeroClass.GOLD, HeroClass.BLACK);
	}

	@Test
	public void testSummoningPortal() {
		runGym((context, player, opponent) -> {
			player.setMana(10);

			Card summoningPortal1 = CardCatalogue.getCardById("minion_summoning_portal");
			context.getLogic().receiveCard(player.getId(), summoningPortal1);
			Card summoningPortal2 = CardCatalogue.getCardById("minion_summoning_portal");
			context.getLogic().receiveCard(player.getId(), summoningPortal2);

			Card testCard = new TestMinionCard(1, 1, 4);
			context.getLogic().receiveCard(player.getId(), testCard);
			assertEquals(player.getMana(), 10);

			// first summoning portal costs full 4 mana
			context.getLogic().performGameAction(player.getId(), summoningPortal1.play());
			assertEquals(player.getMana(), 6);

			// second summoning portal affected by first one, costs only 2 mana
			context.getLogic().performGameAction(player.getId(), summoningPortal2.play());
			assertEquals(player.getMana(), 4);

			// base cost of minion card is 4, reduced by both summoning portals, but
			// not below 1
			context.getLogic().performGameAction(player.getId(), testCard.play());
			assertEquals(player.getMana(), 3);
		}, HeroClass.VIOLET, HeroClass.RED);
	}

	@Test
	public void testSpitefulSmith() {
		runGym((context, player, opponent) -> {
			player.setMana(10);

			Card fieryWarAxe = CardCatalogue.getCardById("weapon_fiery_war_axe");
			playCard(context, player, fieryWarAxe);

			Assert.assertTrue(player.getHero().getWeapon() != null);
			assertEquals(player.getHero().getWeapon().getWeaponDamage(), 3);

			Card spitefulSmithCard = CardCatalogue.getCardById("minion_spiteful_smith");
			Minion spitefulSmith = playMinionCard(context, player, spitefulSmithCard);
			// Smith has been played, but is not enraged yet, so weapon damage
			// should still be unaltered
			assertEquals(player.getHero().getWeapon().getWeaponDamage(), 3);

			Card damageSpell = new TestSpellCard(DamageSpell.create(1));
			damageSpell.setTargetRequirement(TargetSelection.ANY);
			context.getLogic().receiveCard(player.getId(), damageSpell);
			GameAction spellAction = damageSpell.play();
			spellAction.setTarget(spitefulSmith);
			context.getLogic().performGameAction(player.getId(), spellAction);

			// Smith is damaged now, so weapon should be buffed
			assertEquals(player.getHero().getWeapon().getWeaponDamage(), 5);

			// equip a new weapon; this one should get buffed too
			fieryWarAxe = CardCatalogue.getCardById("weapon_fiery_war_axe");
			playCard(context, player, fieryWarAxe);
			assertEquals(player.getHero().getWeapon().getWeaponDamage(), 5);

			// wipe everything
			SpellDesc wipeSpell = DestroySpell.create(EntityReference.ALL_MINIONS);
			Card wipe = new TestSpellCard(wipeSpell);
			playCard(context, player, wipe);

			// Smith is destroyed, weapon power should be back to normal
			assertEquals(player.getHero().getWeapon().getWeaponDamage(), 3);
		}, HeroClass.RED, HeroClass.RED);
	}


	@Test
	public void testFaerieDragon() {
		runGym((context, player, opponent) -> {
			player.setMana(10);
			opponent.setMana(10);

			Card faerieDragonCard = CardCatalogue.getCardById("minion_faerie_dragon");
			context.getLogic().receiveCard(opponent.getId(), faerieDragonCard);
			context.getLogic().performGameAction(opponent.getId(), faerieDragonCard.play());

			Card devMonsterCard = new TestMinionCard(1, 1);
			context.getLogic().receiveCard(player.getId(), devMonsterCard);
			context.getLogic().performGameAction(player.getId(), devMonsterCard.play());

			Entity attacker = getSingleMinion(player.getMinions());
			Actor elusiveOne = getSingleMinion(opponent.getMinions());

			GameAction attackAction = new PhysicalAttackAction(attacker.getReference());
			List<Entity> validTargets = context.getLogic().getValidTargets(opponent.getId(), attackAction);
			// should be two valid targets: enemy hero and faerie dragon
			assertEquals(validTargets.size(), 2);

			GameAction useFireblast = player.getHero().getHeroPower().play();
			validTargets = context.getLogic().getValidTargets(player.getId(), useFireblast);
			// should be three valid targets, both heroes + minion which is not the
			// faerie dragon
			assertEquals(validTargets.size(), 3);
			Assert.assertFalse(validTargets.contains(elusiveOne));

			Card arcaneExplosionCard = CardCatalogue.getCardById("spell_arcane_explosion");
			context.getLogic().receiveCard(player.getId(), arcaneExplosionCard);
			int faerieDragonHp = elusiveOne.getHp();
			context.getLogic().performGameAction(player.getId(), arcaneExplosionCard.play());
			// hp should been affected after playing area of effect spell
			Assert.assertNotEquals(faerieDragonHp, elusiveOne.getHp());
		}, HeroClass.BLUE, HeroClass.RED);

	}

	@Test
	public void testGurubashiBerserker() {
		runGym((context, player, opponent) -> {
			player.setMana(10);
			opponent.setMana(10);

			final int BASE_ATTACK = 2;
			final int ATTACK_BONUS = 3;

			playCard(context, opponent, "minion_gurubashi_berserker");

			playCard(context, player, "minion_oasis_snapjaw");

			Actor attacker = getSingleMinion(player.getMinions());
			Actor defender = getSingleMinion(opponent.getMinions());

			// Gurubashi Berserker should start with just his base attack
			assertEquals(defender.getAttack(), BASE_ATTACK);

			// first attack, Gurubashi Berserker should have increased attack
			GameAction attackAction = new PhysicalAttackAction(attacker.getReference());
			attackAction.setTarget(defender);
			context.getLogic().performGameAction(player.getId(), attackAction);

			assertEquals(attacker.getHp(), attacker.getMaxHp() - BASE_ATTACK);
			assertEquals(defender.getHp(), defender.getMaxHp() - attacker.getAttack());
			assertEquals(defender.getAttack(), BASE_ATTACK + ATTACK_BONUS);

			// second attack, Gurubashi Berserker should become even stronger
			context.getLogic().performGameAction(player.getId(), attackAction);
			assertEquals(attacker.getHp(), attacker.getMaxHp() - 2 * BASE_ATTACK - ATTACK_BONUS);
			assertEquals(defender.getHp(), defender.getMaxHp() - 2 * attacker.getAttack());
			assertEquals(defender.getAttack(), BASE_ATTACK + 2 * ATTACK_BONUS);
		}, HeroClass.BLUE, HeroClass.RED);

	}

	@Test
	public void testSavageRoar() {
		runGym((context, player, opponent) -> {
			Hero druid = player.getHero();

			player.setMana(10);
			Player warrior = context.getPlayer2();
			warrior.setMana(10);

			Card devMonsterCard = new TestMinionCard(1, 1);
			context.getLogic().receiveCard(player.getId(), devMonsterCard);
			context.getLogic().performGameAction(player.getId(), devMonsterCard.play());

			Actor minion = getSingleMinion(player.getMinions());

			context.getLogic().performGameAction(player.getId(), druid.getHeroPower().play());
			assertEquals(druid.getAttack(), 1);
			assertEquals(minion.getAttack(), 1);

			Card savageRoar = CardCatalogue.getCardById("spell_savage_roar");
			context.getLogic().receiveCard(player.getId(), savageRoar);
			context.getLogic().performGameAction(player.getId(), savageRoar.play());
			assertEquals(druid.getAttack(), 3);
			assertEquals(minion.getAttack(), 3);

			context.getLogic().endTurn(player.getId());
			assertEquals(druid.getAttack(), 0);
			assertEquals(minion.getAttack(), 1);

			context.getLogic().endTurn(player.getId());
			assertEquals(druid.getAttack(), 0);
			assertEquals(minion.getAttack(), 1);
		}, HeroClass.BROWN, HeroClass.RED);
	}
}
