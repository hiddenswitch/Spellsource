package com.hiddenswitch.spellsource.tests.hearthstone;


import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import com.hiddenswitch.spellsource.client.models.ActionType;
import net.demilich.metastone.game.actions.DiscoverAction;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.logic.XORShiftRandom;
import net.demilich.metastone.game.spells.BuffSpell;
import net.demilich.metastone.game.spells.CastFromGroupSpell;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.BattlecryDesc;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.source.CardSource;
import net.demilich.metastone.game.spells.desc.source.CardSourceArg;
import net.demilich.metastone.game.spells.desc.source.CardSourceDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.trigger.CardRevealedTrigger;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.tests.util.DebugContext;
import net.demilich.metastone.tests.util.TestBehaviour;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.demilich.metastone.game.targeting.EntityReference.EVENT_TARGET;
import static org.junit.jupiter.api.Assertions.*;

public class TheOldGodsTests extends TestBase {

	@Test
	public void testThingFromBelow() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_mana_tide_totem");
			playCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_bloodfen_raptor");
			Card thing = receiveCard(context, player, "minion_thing_from_below");
			assertEquals(costOf(context, player, thing), thing.getBaseManaCost() - 1);
		});
	}

	@Test
	public void testShadowcaster() {
		runGym((context, player, opponent) -> {
			Minion bloodfenRaptor = playMinionCard(context, player, "minion_bloodfen_raptor");
			playMinionCard(context, player, "minion_shadowcaster", bloodfenRaptor);
			Card miniCopy = player.getHand().get(0);
			assertEquals(miniCopy.getCardId(), "minion_bloodfen_raptor");
			assertEquals(miniCopy.getAttack(), 1);
			assertEquals(miniCopy.getHp(), 1);
			Minion mini = playMinionCard(context, player, miniCopy);
			assertEquals(mini.getAttack(), 1);
			assertEquals(mini.getHp(), 1);
			playCard(context, player, "spell_silence", mini);
			assertEquals(mini.getAttack(), 3);
			assertEquals(mini.getHp(), 2);
		});
	}

	@Test
	public void testRenounceDarkness() {
		runGym((context, player, opponent) -> {
			Map<String, Card> testCard = Stream.of(
					"minion_black_test",
					"minion_blue_test",
					"minion_brown_test",
					"minion_gold_test",
					"minion_green_test",
					"minion_red_test",
					"minion_silver_test",
					"minion_violet_test",
					"minion_white_test")
					.map(CardCatalogue::getCardById)
					.collect(Collectors.toMap(Card::getHeroClass, Function.identity()));

			final int count = 29;

			shuffleToDeck(context, player, "minion_bloodfen_raptor");

			for (int i = 0; i < count; i++) {
				shuffleToDeck(context, player, "minion_voidwalker");
			}

			receiveCard(context, player, "minion_bloodfen_raptor");
			receiveCard(context, player, "minion_voidwalker");
			GameLogic spyLogic = Mockito.spy(context.getLogic());
			context.setLogic(spyLogic);
			AtomicInteger invocationCount = new AtomicInteger(0);
			AtomicReference<String> chosenHeroClass = new AtomicReference<>("VIOLET");
			Answer answer = invocation -> {
				if (invocationCount.getAndIncrement() == 0) {
					// We're choosing the hero power
					Card chosen = (Card) invocation.callRealMethod();
					chosenHeroClass.set(chosen.getHeroClass());
					return chosen;
				}

				final List<Card> replacementCards = new ArrayList<>(invocation.getArgument(0));
				assertTrue(replacementCards.stream().allMatch(card -> card.hasHeroClass(chosenHeroClass.get())));
				// Return a test card with the appropriate hero class to validate the card cost modification
				return testCard.get(chosenHeroClass.get()).clone();

			};
			Mockito.doAnswer(answer).when(spyLogic).getRandom(Mockito.anyList());
			Mockito.doAnswer(answer).when(spyLogic).removeRandom(Mockito.anyList());
			playCard(context, player, "spell_renounce_darkness");

			assertTrue(player.getDeck().containsCard("minion_bloodfen_raptor"));
			assertTrue(player.getHand().containsCard("minion_bloodfen_raptor"));
			assertFalse(player.getHero().getHeroPower().hasHeroClass("VIOLET"));
			assertEquals(Stream.concat(player.getHand().stream(), player.getDeck().stream())
					.filter(card -> !card.hasHeroClass(HeroClass.ANY))
					.filter(card -> !card.hasHeroClass("VIOLET"))
					.filter(card -> costOf(context, player, card) == 1)
					.count(), count + 1);
		}, "VIOLET", "VIOLET");
	}

	@Test
	public void testMarkOfYshaarj() {
		// Test with beast
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "spell_mirror_image");
			Minion raven = playMinionCard(context, player, "minion_enchanted_raven");
			playCard(context, player, "spell_mark_of_yshaarj", raven);
			assertEquals(raven.getAttack(), 4);
			assertEquals(raven.getHp(), 4);
			assertEquals(player.getHand().get(0).getCardId(), "spell_mirror_image");
		});

		// Test with beast
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "spell_mirror_image");
			Minion notBeast = playMinionCard(context, player, "token_steward");
			playCard(context, player, "spell_mark_of_yshaarj", notBeast);
			assertEquals(notBeast.getAttack(), 3);
			assertEquals(notBeast.getHp(), 3);
			assertEquals(player.getHand().size(), 0);
		});
	}

	@Test
	public void testYshaarjRecruitsItself() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_yshaarj_rage_unbound");
			playCard(context, player, "minion_end_of_turn_trigger");
			shuffleToDeck(context, player, "minion_yshaarj_rage_unbound");
			context.endTurn();
			assertEquals(player.getDeck().size(), 1);
			assertEquals(player.getDeck().get(0).getCardId(), "minion_neutral_test");
			assertEquals(player.getMinions().stream().filter(c -> c.getSourceCard().getCardId().equals("minion_yshaarj_rage_unbound")).count(), 2L);
		});
	}

	@Test
	public void testYshaarjRageUnboundShadowEssence() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_yshaarj_rage_unbound");
			shuffleToDeck(context, player, "minion_bloodfen_raptor");
			context.endTurn();
			assertEquals(player.getMinions().get(1).getSourceCard().getCardId(), "minion_bloodfen_raptor");
		});

		runGym((context, player, opponent) -> {
			Card rageUnboundCard = CardCatalogue.getCardById("minion_yshaarj_rage_unbound");
			context.getLogic().shuffleToDeck(player, rageUnboundCard);
			playCard(context, player, "spell_shadow_essence");
			context.getLogic().removeCard(rageUnboundCard);
			shuffleToDeck(context, player, "minion_bloodfen_raptor");
			context.endTurn();
			assertEquals(player.getMinions().get(1).getSourceCard().getCardId(), "minion_bloodfen_raptor");
		});
	}

	@Test
	public void testMasterOfEvolutionBrann() {
		runGym((context, player, opponent) -> {
			context.setDeckFormat(new DeckFormat().withCardSets("BASIC", "CLASSIC"));
			Minion bloodfen = playMinionCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_brann_bronzebeard");
			playCard(context, player, "minion_master_of_evolution");
			assertEquals(bloodfen.transformResolved(context).getSourceCard().getBaseManaCost(), 4);
		});
	}

	@Test
	public void testHeraldVolazj() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_water_elemental");
			playCard(context, player, "minion_herald_volazj");
			assertEquals(player.getMinions().size(), 5);
			assertEquals(player.getMinions().stream().filter(m -> m.getAttack() == 1).count(), 2L);
			assertEquals(player.getMinions().stream().filter(m -> m.getHp() == 1).count(), 2L);
		});
	}

	@Test()
	public void testYoggSaronHopesEnd() {
		// Test that yogg casts the expected number of spells.
		runGym((context, player, opponent) -> {
			final int expectedSpells = 3;
			final Map<SpellArg, Object> build = new SpellDesc(YoggTestSpell1.class);
			build.put(SpellArg.TARGET, EVENT_TARGET);
			final Enchantment testEnchantment = new Enchantment(
					new CardRevealedTrigger(new EventTriggerDesc(CardRevealedTrigger.class)),
					new SpellDesc(build));
			testEnchantment.setHost(player);
			testEnchantment.setOwner(player.getId());
			context.addTrigger(testEnchantment);
			for (int i = 0; i < expectedSpells; i++) {
				playCard(context, player, "spell_innervate");
			}
			// Modify yogg to only cast the coin
			Card yoggCard = CardCatalogue.getCardById("minion_yogg_saron_hopes_end");
			final BattlecryDesc battlecry = ((CardDesc) yoggCard.getDesc()).getBattlecry();
			final SpellDesc originalSpell = battlecry.getSpell();
			Map<CardSourceArg, Object> cardSourceArgs = new CardSourceDesc(CardSource.class);
			cardSourceArgs.put(CardSourceArg.TARGET_PLAYER, TargetPlayer.SELF);
			battlecry.setSpell(originalSpell.addArg(SpellArg.CARD_SOURCE, new CardSource(new CardSourceDesc(cardSourceArgs)) {
				@Override
				protected CardList match(GameContext context, Entity source, Player player) {
					return new CardArrayList().addCard(CardCatalogue.getCardById("spell_the_coin"));
				}
			}));
			playMinionCard(context, player, "minion_yogg_saron_hopes_end");
			assertEquals(YoggTestSpell1.counter.getCount(), 0, "The number of spells left to cast should be zero.");
			battlecry.setSpell(originalSpell);
		});

		// Test that if yogg destroys itself, the spell casting ends.
		runGym((context, player, opponent) -> {
			final int expectedSpells = 3;
			GameLogic spyLogic = Mockito.spy(context.getLogic());
			context.setLogic(spyLogic);
			Mockito.when(spyLogic.getRandom(Mockito.anyList()))
					.thenAnswer(invocation -> {
						List<Entity> targets = invocation.getArgument(0);
						if (targets.stream().anyMatch(e -> e.getSourceCard().getCardId().equals("minion_yogg_saron_hopes_end"))) {
							return targets.stream().filter(e -> e.getSourceCard().getCardId().equals("minion_yogg_saron_hopes_end")).findFirst().orElseThrow(AssertionError::new);
						} else {
							return invocation.callRealMethod();
						}
					});

			final Map<SpellArg, Object> build = new SpellDesc(YoggTestSpell2.class);
			build.put(SpellArg.TARGET, EVENT_TARGET);
			final Enchantment testEnchantment = new Enchantment(
					new CardRevealedTrigger(new EventTriggerDesc(CardRevealedTrigger.class)),
					new SpellDesc(build));
			testEnchantment.setHost(player);
			testEnchantment.setOwner(player.getId());
			context.addTrigger(testEnchantment);
			for (int i = 0; i < expectedSpells; i++) {
				playCard(context, player, "spell_innervate");
			}
			// Modify yogg to only cast the coin
			Card yoggCard = CardCatalogue.getCardById("minion_yogg_saron_hopes_end");
			final BattlecryDesc battlecry = ((CardDesc) yoggCard.getDesc()).getBattlecry();
			final SpellDesc originalSpell = battlecry.getSpell();
			Map<CardSourceArg, Object> cardSourceArgs = new CardSourceDesc(CardSource.class);
			cardSourceArgs.put(CardSourceArg.TARGET_PLAYER, TargetPlayer.SELF);
			battlecry.setSpell(originalSpell.addArg(SpellArg.CARD_SOURCE, new CardSource(new CardSourceDesc(cardSourceArgs)) {
				@Override
				protected CardList match(GameContext context, Entity source, Player player) {
					return new CardArrayList().addCard(CardCatalogue.getCardById("spell_fireball"));
				}
			}));
			playCard(context, player, "minion_yogg_saron_hopes_end");
			assertEquals(YoggTestSpell2.counter.getCount(), 2, "Since yogg fireballed itself, we expect two spells left uncasted.");
			battlecry.setSpell(originalSpell);
		});
	}

	@Test
	public void testVilefinInquisitor() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_vilefin_inquisitor");
			assertEquals(player.getHero().getHeroPower().getCardId(), "hero_power_the_tidal_hand");
		});
	}


	@Test
	public void testCallInTheFinishers() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_call_in_the_finishers");

			for (Minion minion : player.getMinions()) {
				assertEquals(minion.getSourceCard().getCardId(), "token_murloc_razorgill");
			}
		});
	}

	@Test
	public void testDarkshireCoucilman() {
		runGym((context, player, opponent) -> {
			Minion darkshireCouncilman = playMinionCard(context, player, CardCatalogue.getCardById("minion_darkshire_councilman"));
			assertEquals(darkshireCouncilman.getAttack(), darkshireCouncilman.getBaseAttack());

			Minion darkshireCouncilman2 = playMinionCard(context, player, CardCatalogue.getCardById("minion_darkshire_councilman"));
			assertEquals(darkshireCouncilman.getAttack(), darkshireCouncilman.getBaseAttack() + 1);
			assertEquals(darkshireCouncilman2.getAttack(), darkshireCouncilman2.getBaseAttack());

			context.getLogic().endTurn(player.getId());
			Minion opponentMinion = playMinionCard(context, opponent, CardCatalogue.getCardById("minion_darkshire_councilman"));

			assertEquals(darkshireCouncilman.getAttack(), darkshireCouncilman.getBaseAttack() + 1);
			assertEquals(darkshireCouncilman2.getAttack(), darkshireCouncilman2.getBaseAttack());
			assertEquals(opponentMinion.getAttack(), opponentMinion.getBaseAttack());
		});

		runGym((context, player, opponent) -> {
			Minion darkshireCouncilman = playMinionCard(context, player, "minion_darkshire_councilman");
			assertEquals(darkshireCouncilman.getAttack(), darkshireCouncilman.getBaseAttack());

			playMinionCard(context, player, "permanent_test");
			assertEquals(darkshireCouncilman.getAttack(), darkshireCouncilman.getBaseAttack());
		});
	}

	@Test
	public void testALightInTheDarkness() {
		GameContext context = createContext("SILVER", "RED");
		context.getLogic().setRandom(new XORShiftRandom(101010L));
		Player player = context.getActivePlayer();
		final DiscoverAction[] action = {null};
		final Minion[] originalMinion = new Minion[1];
		final int[] handSize = new int[1];
		context.setBehaviour(player.getId(), new TestBehaviour() {
			boolean first = true;

			@Override
			public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
				if (first) {
					assertTrue(validActions.stream().allMatch(ga -> ga.getActionType() == ActionType.DISCOVER));
					assertEquals(validActions.size(), 3);
					final DiscoverAction discoverAction = (DiscoverAction) validActions.get(0);
					action[0] = discoverAction;
					Card original = action[0].getCard();
					originalMinion[0] = original.summon();
					handSize[0] = player.getHand().size();
					final Card minionOverride = CardCatalogue.getCardById("minion_bloodfen_raptor");
					minionOverride.setId(context.getLogic().generateId());
					minionOverride.setOwner(player.getId());
					minionOverride.moveOrAddTo(context, Zones.DISCOVER);
					discoverAction.setCard(minionOverride);
				}
				first = false;
				return super.requestAction(context, player, validActions);
			}
		});
		Card light = CardCatalogue.getCardById("spell_a_light_in_the_darkness");
		playCard(context, player, light);
		assertEquals(player.getHand().size(), handSize[0] + 1);
		Card cardInHand = player.getHand().get(player.getHand().size() - 1);
		assertEquals(cardInHand.getCardId(), originalMinion[0].getSourceCard().getCardId());
		context.performAction(player.getId(), cardInHand.play());
		int buff = light.getSpell().subSpells().stream().filter(sd -> sd.getDescClass().equals(BuffSpell.class)).findFirst().orElseThrow(AssertionError::new).getInt(SpellArg.VALUE, -999);
		// Find the minion that was created with the specified card, because minions like Dr. Boom put unexpected cards into play into the first position.
		Minion targetMinion = player.getMinions().stream().filter(m -> m.getSourceCard().getCardId().equals(cardInHand.getCardId())).findFirst().orElseThrow(AssertionError::new);
		// Some minions, like Fireguard Destroyer, increase their stats by a random amount, so handle that here.
		Card sourceCard = targetMinion.getSourceCard();
		boolean hasRandomStatsIncrease = Stream.of(
				"minion_fireguard_destroyer",
				"minion_injured_blademaster",
				"minion_injured_kvaldir",
				"minion_midnight_drake",
				"minion_sly_conquistador",
				"minion_twilight_drake").anyMatch(c -> {
			return sourceCard.getCardId().equals(c);
		});

		boolean buffsSelf = false;
		// LISP to the rescue
		if (sourceCard.getDesc().getBattlecry() != null) {
			buffsSelf = Stream.concat(Stream.of(sourceCard.getDesc().getBattlecry().getSpell()), sourceCard.getDesc().getBattlecry().getSpell().subSpells().stream())
					.anyMatch(spellDesc -> (BuffSpell.class.isAssignableFrom(spellDesc.getDescClass())
							|| CastFromGroupSpell.class.isAssignableFrom(spellDesc.getDescClass())) && spellDesc.getTarget() != null
							&& spellDesc.getTarget().equals(EntityReference.SELF));
		}

		if (hasRandomStatsIncrease || buffsSelf) {
			assertTrue(targetMinion.getAttributeValue(Attribute.ATTACK_BONUS) >= 1);
			assertTrue(targetMinion.getAttributeValue(Attribute.HP_BONUS) >= 1);
		} else {
			assertEquals(targetMinion.getAttack(), originalMinion[0].getAttack() + buff);
			assertEquals(targetMinion.getHp(), originalMinion[0].getHp() + buff);
		}
	}

	@Test
	public void testKingsDefenderHogger() {
		DebugContext context = createContext("RED", "RED");
		Player player = context.getPlayer1();
		Hero hero = player.getHero();

		playCard(context, player, "weapon_deaths_bite");
		assertEquals(hero.getWeapon().getAttack(), 4);
		assertEquals(hero.getWeapon().getDurability(), 2);
		playCard(context, player, "minion_hogger_doom_of_elwynn");
		assertEquals(player.getMinions().size(), 1);
		playCard(context, player, "weapon_kings_defender");
		assertEquals(hero.getWeapon().getAttack(), 3);
		assertEquals(hero.getWeapon().getDurability(), 2);
		assertEquals(player.getMinions().size(), 2);
	}

	@Test
	public void testRallyingBlade() {
		GameContext context = createContext("GOLD", "BLACK");
		Player player = context.getPlayer1();
		Card argentSquireCard = CardCatalogue.getCardById("minion_argent_squire");
		Minion argentSquire = playMinionCard(context, player, argentSquireCard);
		assertEquals(argentSquire.getAttack(), 1);
		assertEquals(argentSquire.getHp(), 1);

		Card rallyingBladeCard = CardCatalogue.getCardById("weapon_rallying_blade");
		playCard(context, player, rallyingBladeCard);
		assertEquals(argentSquire.getAttack(), 2);
		assertEquals(argentSquire.getHp(), 2);
	}

	public static class YoggTestSpell1 extends Spell {
		private static final CountDownLatch counter = new CountDownLatch(3);

		public YoggTestSpell1() {
		}

		@Override
		@Suspendable
		protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
			assertEquals(target.getSourceCard().getCardId(), "spell_the_coin");
			counter.countDown();
		}
	}

	public static class YoggTestSpell2 extends Spell {
		private static final CountDownLatch counter = new CountDownLatch(3);

		public YoggTestSpell2() {
		}

		@Override
		@Suspendable
		protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
			assertEquals(target.getSourceCard().getCardId(), "spell_fireball");
			counter.countDown();
		}
	}
}

