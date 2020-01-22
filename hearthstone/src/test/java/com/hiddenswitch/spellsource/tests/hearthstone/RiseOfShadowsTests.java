package com.hiddenswitch.spellsource.tests.hearthstone;

import net.demilich.metastone.game.actions.DiscoverAction;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.aura.SecretsTriggerTwiceAura;
import net.demilich.metastone.game.targeting.TargetSelection;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;


public class RiseOfShadowsTests extends TestBase {

	@Test
	public void testTwinSpell() {
		runGym((context, player, opponent) -> {
			Card aid = receiveCard(context, player, "spell_the_forests_aid");
			playCard(context, player, aid);
			assertEquals(player.getHand().size(), 1);
			aid = player.getHand().get(0);
			assertFalse(aid.getDescription().contains("Twinspell"));
			playCard(context, player, aid);
			assertEquals(player.getHand().size(), 0);
		});
	}

	@Test
	public void testHagathasScheme() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 4; i++) {
				receiveCard(context, player, "spell_hagathas_scheme");
			}
			Minion gargoyle = playMinionCard(context, opponent, "minion_stoneskin_gargoyle");
			for (int i = 1; i <= 4; i++) {
				Card card = player.getHand().get(0);
				assertTrue(card.getDescription(context, player).contains("Deal " + i + " damage"));
				playCard(context, player, card);
				assertEquals(gargoyle.getHp(), 4 - i);
				context.endTurn();
				context.endTurn();
			}
		});
	}

	@Test
	public void testTogwagglesScheme() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 4; i++) {
				receiveCard(context, player, "spell_togwaggles_scheme");
			}
			Minion wisp = playMinionCard(context, player, "minion_wisp");
			for (int i = 1; i <= 4; i++) {
				Card card = player.getHand().get(0);
				assertTrue(card.getDescription(context, player).contains("Shuffle " + i));
				int deckCount = player.getDeck().getCount();
				playCard(context, player, card, wisp);
				assertEquals(player.getDeck().size(), deckCount + i);
				context.endTurn();
				context.endTurn();
			}
		});

	}

	@Test
	public void testEvilMiscreant() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_evil_miscreant");
			assertEquals(player.getHand().size(), 0);
			playCard(context, player, "minion_evil_miscreant");
			assertEquals(player.getHand().size(), 2);
			for (int i = 0; i < 1; i++) {
				assertTrue(player.getHand().get(i).hasAttribute(Attribute.LACKEY));
			}
		});

	}

	@Test
	public void testForbiddenWords() {
		runGym((context, player, opponent) -> {
			Card words = receiveCard(context, player, "spell_forbidden_words");
			player.setMana(3);
			Minion theAntiPriest = playMinionCard(context, opponent, "minion_twilight_drake");
			assertFalse(words.canBeCastOn(context, player, theAntiPriest));
			player.setMana(4);
			assertTrue(words.canBeCastOn(context, player, theAntiPriest));
		});
	}

	@Test
	public void testKalecgos() {
		runGym((context, player, opponent) -> {
			Card pyro = receiveCard(context, player, "spell_pyroblast");
			assertFalse(context.getLogic().canPlayCard(player, pyro));
			playMinionCard(context, player, "minion_kalecgos");
			assertEquals(player.getHand().size(), 2);
			assertTrue(context.getLogic().canPlayCard(player, pyro));
		});
	}

	@Test
	public void testSpellwardJeweler() {
		runGym((context, player, opponent) -> {
			Card pyro = receiveCard(context, opponent, "spell_pyroblast");
			assertTrue(context.getLogic().getValidTargets(opponent.getId(), pyro.play()).contains(player.getHero()));
			playCard(context, player, "minion_spellward_jeweler");
			assertTrue(player.getHero().hasAttribute(Attribute.UNTARGETABLE_BY_SPELLS));
			assertFalse(context.getLogic().getValidTargets(opponent.getId(), pyro.play()).contains(player.getHero()));
			context.endTurn();
			context.endTurn();
			assertTrue(context.getLogic().getValidTargets(opponent.getId(), pyro.play()).contains(player.getHero()));
		});
	}

	@Test
	public void testSwampqueenHagatha() {
		runGym((context, player, opponent) -> {
			Behaviour spiedBehavior = Mockito.spy(context.getBehaviours().get(player.getId()));
			context.setBehaviour(player.getId(), spiedBehavior);
			List<Card> cards = new ArrayList<>();
			AtomicBoolean isTeachingHorror = new AtomicBoolean(false);
			final Answer<GameAction> answer = invocation -> {

				if (isTeachingHorror.get()) {
					@SuppressWarnings("unchecked") final List<GameAction> gameActions = (List<GameAction>) invocation.getArguments()[2];
					final DiscoverAction discoverAction = (DiscoverAction) gameActions.get(0);
					cards.add(discoverAction.getCard());
					return discoverAction;
				}
				return (GameAction) invocation.callRealMethod();
			};

			Mockito.doAnswer(answer)
					.when(spiedBehavior)
					.requestAction(Mockito.any(), Mockito.any(), Mockito.anyList());

			isTeachingHorror.set(true);
			playCard(context, player, "minion_swampqueen_hagatha");
			isTeachingHorror.set(false);
			Card cardInHand = player.getHand().get(0);
			assertEquals(cardInHand.getBaseHp(), 5);
			assertEquals(cardInHand.getBaseAttack(), 5);
			assertEquals(cardInHand.getBaseManaCost(), 5);
			for (Card card : cards) {
				assertTrue(cardInHand.getDescription().contains(card.getName()));
				if (card.getTargetSelection() != TargetSelection.NONE) {
					assertTrue(cardInHand.getDesc().getBattlecry().getTargetSelection() == card.getTargetSelection());
				}
			}


		});
	}

	@Test
	public void testOblivitron() {
		runGym((context, player, opponent) -> {
			Minion tron = playMinionCard(context, player, "minion_oblivitron");
			receiveCard(context, player, "minion_goblin_bomb");
			destroy(context, tron);
			assertEquals(opponent.getHero().getHp(), 28);
		});
	}

	@Test
	public void testLazulsScheme() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 4; i++) {
				receiveCard(context, player, "spell_lazuls_scheme");
			}
			Minion dino = playMinionCard(context, opponent, "minion_ultrasaur");
			for (int i = 1; i <= 4; i++) {
				Card card = player.getHand().get(0);
				assertTrue(card.getDescription(context, player).contains("by " + i));
				assertEquals(dino.getAttack(), 7);
				playCard(context, player, card, dino);
				assertEquals(dino.getAttack(), 7 - i);
				context.endTurn();
				assertEquals(dino.getAttack(), 7 - i);
				context.endTurn();
			}
		});
	}

	@Test
	public void testRafaamsScheme() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 7; i++) {
				receiveCard(context, player, "spell_rafaams_scheme");
			}
			for (int i = 1; i <= 7; i++) {
				Card card = player.getHand().get(0);
				assertTrue(card.getDescription(context, player).contains("Summon " + i));
				playCard(context, player, card);
				assertEquals(player.getMinions().size(), i);
				context.endTurn();
				playCard(context, opponent, "spell_twisting_nether");
				context.endTurn();
			}
		});
	}

	@Test
	public void testMadameLazul() {
		runGym((context, player, opponent) -> {
			receiveCard(context, opponent, "minion_wisp");
			receiveCard(context, opponent, "minion_ultrasaur");
			receiveCard(context, opponent, "spell_pyroblast");

			overrideDiscover(context, player, discoverActions -> {
				assertTrue(discoverActions.stream().anyMatch(discoverAction -> discoverAction.getCard().getCardId().equals("minion_wisp")));
				assertTrue(discoverActions.stream().anyMatch(discoverAction -> discoverAction.getCard().getCardId().equals("minion_ultrasaur")));
				assertTrue(discoverActions.stream().anyMatch(discoverAction -> discoverAction.getCard().getCardId().equals("spell_pyroblast")));
				return discoverActions.stream().filter(discoverAction -> discoverAction.getCard().getCardId().equals("spell_pyroblast")).findFirst().get();
			});
			playCard(context, player, "minion_madame_lazul");

			assertEquals(player.getHand().size(), 1);
			assertEquals(player.getHand().get(0).getCardId(), "spell_pyroblast");
		});
	}

	@Test
	public void testBombCards() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_clockwork_goblin");
			assertEquals(opponent.getDeck().size(), 1);
			playCard(context, player, "weapon_wrenchcalibur");
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(opponent.getDeck().size(), 2);
			playCard(context, player, "minion_iron_juggernaut");
			assertEquals(opponent.getDeck().size(), 3);
			playCard(context, player, "spell_twisting_nether");
			playMinionCard(context, player, "minion_blastmaster_boom");
			assertEquals(player.getMinions().size(), 5);
		});
	}

	@Test
	public void testHeistbaronTogwaggle() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_heistbaron_togwaggle");
			assertEquals(player.getHand().size(), 0);
			playCard(context, player, "token_kobold_lackey", opponent.getHero());
			overrideDiscover(context, player, discoverActions -> {
				assertTrue(discoverActions.stream().anyMatch(dA -> dA.getCard().getCardId().equals("spell_tolins_goblet")));
				assertTrue(discoverActions.stream().anyMatch(dA -> dA.getCard().getCardId().equals("spell_zarogs_crown")));
				assertTrue(discoverActions.stream().anyMatch(dA -> dA.getCard().getCardId().equals("spell_wondrous_wand")));
				assertTrue(discoverActions.stream().anyMatch(dA -> dA.getCard().getCardId().equals("token_golden_kobold")));
				return discoverActions.get(0);
			});

			playCard(context, player, "minion_heistbaron_togwaggle");
			assertEquals(player.getHand().size(), 1);
		});
	}

	@Test
	public void testKhadgar() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_khadgar");
			playCard(context, player, "minion_saronite_chain_gang");
			assertEquals(player.getMinions().size(), 4);
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_khadgar");
			playCard(context, player, "spell_mirror_image");
			assertEquals(player.getMinions().size(), 5);
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_khadgar");
			playCard(context, player, "spell_kara_kazham");
			assertEquals(player.getMinions().size(), 7);
		});
	}

	@Test
	public void testHenchClanBurglar() {
		runGym((context, player, opponent) -> {
			overrideDiscover(context, player, discoverActions -> {
				discoverActions.stream().forEach(discoverAction -> {
					assertNotEquals(HeroClass.SELF, discoverAction.getCard().getHeroClass());
					assertNotEquals(HeroClass.ANY, discoverAction.getCard().getHeroClass());
				});
				return discoverActions.get(0);
			});
			playCard(context, player, "minion_hench_clan_burglar");
			assertEquals(player.getHand().size(), 1);
		});

	}

	@Test
	public void testKeeperStalladris() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_keeper_stalladris");
			playChooseOneCard(context, player, "spell_power_of_the_wild", "spell_power_of_the_wild_2");
			assertEquals(player.getHand().get(0).getCardId(), "spell_power_of_the_wild_1");
			assertEquals(player.getHand().get(1).getCardId(), "spell_power_of_the_wild_2");
		});
	}

	@Test
	public void testCrystalStag() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_crystal_stag");
			assertEquals(player.getMinions().size(), 1);
			playCard(context, player, "spell_fireball", player.getHero());
			playChooseOneCard(context, player, "spell_crystal_power", "spell_crystal_power_2", player.getHero());

			playMinionCard(context, player, "minion_crystal_stag");
			assertEquals(player.getMinions().size(), 3);
		});

	}

	@Test
	public void testMuckmorpher() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_malygos");
			for (int i = 0; i < 10; i++) {
				shuffleToDeck(context, player, "minion_muckmorpher");
			}

			playCard(context, player, "minion_muckmorpher");
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_malygos");
			assertEquals(player.getMinions().get(0).getHp(), 4);


		});
	}

	@Test
	public void testCommanderRhyssa() {
		runGym((context, player, opponent) -> {
			assertFalse(SpellUtils.hasAura(context, player.getId(), SecretsTriggerTwiceAura.class));
			Minion rhyssa = playMinionCard(context, player, "minion_commander_rhyssa");
			assertTrue(SpellUtils.hasAura(context, player.getId(), SecretsTriggerTwiceAura.class));


			playCard(context, player, "secret_competitive_spirit");
			playCard(context, player, "secret_noble_sacrifice");
			context.endTurn();
			playCard(context, opponent, "spell_claw");
			attack(context, opponent, opponent.getHero(), rhyssa);
			assertEquals(player.getMinions().size(), 2);
			context.endTurn();
			assertEquals(rhyssa.getHp(), rhyssa.getBaseHp() + 2);


		});
	}

	@Test
	public void testManaCyclone() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_elemental_evocation");
			playCard(context, player, "spell_pyroblast", opponent.getHero());
			playCard(context, player, "minion_mana_cyclone");
			assertEquals(player.getHand().size(), 2);
		});
	}

	@Test
	public void testUnseenSaboteur() {
		runGym((context, player, opponent) -> {
			receiveCard(context, opponent, "spell_fiendish_circle");
			playCard(context, player, "minion_unseen_saboteur");
			assertEquals(opponent.getHand().size(), 0);
			assertEquals(opponent.getMinions().size(), 4);
		});
	}

	@Test
	public void testDuel() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_violet_wurm");
			shuffleToDeck(context, opponent, "minion_violet_wurm");
			playCard(context, player, "spell_duel");
			assertEquals(player.getMinions().size(), 7);
			assertEquals(opponent.getMinions().size(), 7);
		});
	}

	@Test
	public void testLucentBark() {
		runGym((context, player, opponent) -> {
			Minion lucentbark = playMinionCard(context, player, "minion_lucentbark");
			playCard(context, player, "spell_pyroblast", player.getHero());
			playCard(context, player, "spell_assassinate", lucentbark);

			assertEquals(player.getMinions().size(), 1);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "permanent_spirit_of_lucentbark");

			Minion minion = player.getMinions().get(0);

			playCard(context, player, "spell_regenerate", player.getHero());
			//System.out.println(minion.getDescription(context, player));
			//assertTrue(minion.getDescription(context, player).contains("2 more"));
			playCard(context, player, "minion_voodoo_doctor", player.getHero());


			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_lucentbark");
		});
	}

	@Test
	public void testTakNozwhisker() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_tak_nozwhisker");
			playCard(context, player, "minion_elise_trailblazer");
			assertEquals(player.getHand().size(), 1);
			assertEquals(player.getDeck().size(), 1);
			assertEquals(player.getHand().get(0).getCardId(), "spell_ungoro_pack");
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_tak_nozwhisker");
			playCard(context, player, "spell_academic_espionage");
			assertEquals(player.getHand().size(), 10);
			assertEquals(player.getDeck().size(), 10);

			for (Card card : player.getHand()) {
				assertEquals(costOf(context, player, card), 1);
			}
		}, HeroClass.ANY, HeroClass.ANY);
	}

	@Test
	public void testCrytalsongPortal() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_crystalsong_portal");
			assertEquals(player.getHand().size(), 3);
			playCard(context, player, "spell_crystalsong_portal");
			assertEquals(player.getHand().size(), 4);
		});
	}

	@Test
	public void testNineLives() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_savannah_highmane");
			playCard(context, player, "spell_twisting_nether");
			playCard(context, player, "spell_twisting_nether");

			playCard(context, player, "spell_nine_lives");
			assertEquals(player.getMinions().size(), 2);
		});
	}

	@Test
	public void testArchmageVargoth() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_archmage_vargoth");
			playCard(context, player, "spell_muster_for_battle");
			assertEquals(player.getMinions().size(), 4);
			context.endTurn();
			assertEquals(player.getMinions().size(), 7);
		});
	}

	@Test
	public void testVendetta() {
		runGym((context, player, opponent) -> {
			Card vendetta = receiveCard(context, player, "spell_vendetta");
			assertEquals(costOf(context, player, vendetta), 4);
			receiveCard(context, player, "spell_pyroblast");
			assertEquals(costOf(context, player, vendetta), 0);
		}, "BLACK", "BLACK");
	}

	@Test
	public void testConjurersCalling() {
		runGym((context, player, opponent) -> {
			Minion dino = playMinionCard(context, player, "minion_ultrasaur");
			playCard(context, player, "spell_conjurers_calling", dino);
			assertEquals(player.getMinions().size(), 2);
			for (int i = 0; i < 2; i++) {
				assertEquals(player.getMinions().get(i).getSourceCard().getBaseManaCost(), 10);
			}
		});

		runGym((context, player, opponent) -> {
			Minion dino = playMinionCard(context, opponent, "minion_ultrasaur");
			playCard(context, player, "spell_conjurers_calling", dino);
			assertEquals(opponent.getMinions().size(), 2);
			for (int i = 0; i < 2; i++) {
				assertEquals(opponent.getMinions().get(i).getSourceCard().getBaseManaCost(), 10);
			}
		});
	}

	@Test
	public void testSweepingStrikes() {
		runGym((context, player, opponent) -> {
			Minion left = playMinionCard(context, opponent, "minion_wisp");
			Minion middle = playMinionCard(context, opponent, "minion_wisp");
			Minion right = playMinionCard(context, opponent, "minion_wisp");
			assertEquals(opponent.getMinions().size(), 3);
			Minion overkiller = playMinionCard(context, player, "minion_half_time_scavenger");
			playCard(context, player, "spell_sweeping_strikes", overkiller);
			attack(context, player, overkiller, middle);
			assertTrue(left.isDestroyed());
			assertTrue(middle.isDestroyed());
			assertTrue(right.isDestroyed());
			assertEquals(opponent.getMinions().size(), 0);
			assertEquals(player.getHero().getArmor(), 9);
		});
	}

	@Test
	public void testDarkestHour() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 10; i++) {
				shuffleToDeck(context, player, "minion_ultrasaur");
			}

			for (int i = 0; i < 5; i++) {
				playCard(context, player, "minion_wisp");
			}

			playCard(context, player, "spell_darkest_hour");

			assertEquals(player.getMinions().size(), 5);
		});
	}

	@Test
	public void testWhirlwindTempest() {
		runGym((context, player, opponent) -> {
			Minion harpy = playMinionCard(context, player, "minion_windfury_harpy");
			assertEquals(harpy.getMaxNumberOfAttacks(), 2);
			Minion tempest = playMinionCard(context, player, "minion_whirlwind_tempest");
			assertEquals(harpy.getMaxNumberOfAttacks(), 4);
		});
	}

}
