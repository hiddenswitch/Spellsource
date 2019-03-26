package com.blizzard.hearthstone;

import net.demilich.metastone.game.actions.DiscoverAction;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.targeting.TargetSelection;
import net.demilich.metastone.tests.util.TestBase;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.stream.Collectors.summarizingInt;
import static org.testng.Assert.*;

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
					final List<GameAction> gameActions = (List<GameAction>) invocation.getArguments()[2];
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
				discoverActions.stream().forEach(discoverAction -> assertFalse(discoverAction.getCard().hasHeroClass(player.getHero().getHeroClass())));
				return discoverActions.get(0);
			});
			playCard(context, player, "minion_hench_clan_burglar");
		});

	}
}
