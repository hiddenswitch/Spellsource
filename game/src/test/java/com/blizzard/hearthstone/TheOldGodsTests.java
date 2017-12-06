package com.blizzard.hearthstone;


import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.DiscoverAction;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.cards.desc.MinionCardDesc;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.BuffSpell;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.BattlecryDesc;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.source.CardSource;
import net.demilich.metastone.game.spells.desc.source.SourceArg;
import net.demilich.metastone.game.spells.desc.source.SourceDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.trigger.CardRevealedTrigger;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.tests.util.DebugContext;
import net.demilich.metastone.tests.util.TestBase;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static net.demilich.metastone.game.targeting.EntityReference.EVENT_TARGET;

public class TheOldGodsTests extends TestBase {
	@Test
	public void testYoggSaronHopesEnd() {
		// Test that yogg casts the expected number of spells.
		runGym((context, player, opponent) -> {
			final int expectedSpells = 3;
			final Map<SpellArg, Object> build = SpellDesc.build(YoggTestSpell1.class);
			build.put(SpellArg.TARGET, EVENT_TARGET);
			final Enchantment testEnchantment = new Enchantment(
					new CardRevealedTrigger(EventTriggerDesc.createEmpty(CardRevealedTrigger.class)),
					new SpellDesc(build));
			testEnchantment.setHost(player);
			testEnchantment.setOwner(player.getId());
			context.addTrigger(testEnchantment);
			for (int i = 0; i < expectedSpells; i++) {
				playCard(context, player, "spell_innervate");
			}
			// Modify yogg to only cast the coin
			MinionCard yoggCard = (MinionCard) CardCatalogue.getCardById("minion_yogg_saron_hopes_end");
			final BattlecryDesc battlecry = ((MinionCardDesc) yoggCard.getDesc()).battlecry;
			final SpellDesc originalSpell = battlecry.spell;
			Map<SourceArg, Object> cardSourceArgs = SourceDesc.build(CardSource.class);
			cardSourceArgs.put(SourceArg.TARGET_PLAYER, TargetPlayer.SELF);
			battlecry.spell = originalSpell.addArg(SpellArg.CARD_SOURCE, new CardSource(new SourceDesc(cardSourceArgs)) {
				@Override
				protected CardList match(GameContext context, Player player) {
					return new CardArrayList().addCard(CardCatalogue.getCardById("spell_the_coin"));
				}
			});
			playMinionCard(context, player, "minion_yogg_saron_hopes_end");
			Assert.assertEquals(YoggTestSpell1.counter.getCount(), 0, "The number of spells left to cast should be zero.");
			battlecry.spell = originalSpell;
		});

		// Test that if yogg destroys itself, the spell casting ends.
		runGym((context, player, opponent) -> {
			final int expectedSpells = 3;
			GameLogic spyLogic = Mockito.spy(context.getLogic());
			context.setLogic(spyLogic);
			Mockito.when(spyLogic.getRandom(Mockito.anyList()))
					.thenAnswer(invocation -> {
						List<Entity> targets = invocation.getArgument(0);
						Assert.assertTrue(targets.stream().anyMatch(e -> e.getSourceCard().getCardId().equals("minion_yogg_saron_hopes_end")));
						return targets.stream().filter(e -> e.getSourceCard().getCardId().equals("minion_yogg_saron_hopes_end")).findFirst().orElseThrow(AssertionError::new);
					});

			final Map<SpellArg, Object> build = SpellDesc.build(YoggTestSpell2.class);
			build.put(SpellArg.TARGET, EVENT_TARGET);
			final Enchantment testEnchantment = new Enchantment(
					new CardRevealedTrigger(EventTriggerDesc.createEmpty(CardRevealedTrigger.class)),
					new SpellDesc(build));
			testEnchantment.setHost(player);
			testEnchantment.setOwner(player.getId());
			context.addTrigger(testEnchantment);
			for (int i = 0; i < expectedSpells; i++) {
				playCard(context, player, "spell_innervate");
			}
			// Modify yogg to only cast the coin
			MinionCard yoggCard = (MinionCard) CardCatalogue.getCardById("minion_yogg_saron_hopes_end");
			final BattlecryDesc battlecry = ((MinionCardDesc) yoggCard.getDesc()).battlecry;
			final SpellDesc originalSpell = battlecry.spell;
			Map<SourceArg, Object> cardSourceArgs = SourceDesc.build(CardSource.class);
			cardSourceArgs.put(SourceArg.TARGET_PLAYER, TargetPlayer.SELF);
			battlecry.spell = originalSpell.addArg(SpellArg.CARD_SOURCE, new CardSource(new SourceDesc(cardSourceArgs)) {
				@Override
				protected CardList match(GameContext context, Player player) {
					return new CardArrayList().addCard(CardCatalogue.getCardById("spell_fireball"));
				}
			});
			playCard(context, player, "minion_yogg_saron_hopes_end");
			Assert.assertEquals(YoggTestSpell2.counter.getCount(), 2, "Since yogg fireballed itself, we expect two spells left uncasted.");
			battlecry.spell = originalSpell;
		});
	}

	@Test
	public void testVilefinInquisitor() {
		runGym((context, player, opponent) -> {
			playCard(context, player, CardCatalogue.getCardById("minion_vilefin_inquisitor"));
			Assert.assertEquals(player.getHero().getHeroPower().getCardId(), "hero_power_the_tidal_hand");
		});
	}


	@Test
	public void testCallInTheFinishers() {
		runGym((context, player, opponent) -> {
			playCard(context, player, CardCatalogue.getCardById("spell_call_in_the_finishers"));

			for (Minion minion : player.getMinions()) {
				Assert.assertEquals(minion.getSourceCard().getCardId(), "token_murloc_razorgill");
			}
		});
	}

	@Test
	public void testDarkshireCoucilman() {
		runGym((context, player, opponent) -> {
			Minion darkshireCouncilman = playMinionCard(context, player, (MinionCard) CardCatalogue.getCardById("minion_darkshire_councilman"));
			Assert.assertEquals(darkshireCouncilman.getAttack(), darkshireCouncilman.getBaseAttack());

			Minion darkshireCouncilman2 = playMinionCard(context, player, (MinionCard) CardCatalogue.getCardById("minion_darkshire_councilman"));
			Assert.assertEquals(darkshireCouncilman.getAttack(), darkshireCouncilman.getBaseAttack() + 1);
			Assert.assertEquals(darkshireCouncilman2.getAttack(), darkshireCouncilman2.getBaseAttack());

			context.getLogic().endTurn(player.getId());
			Minion opponentMinion = playMinionCard(context, opponent, (MinionCard) CardCatalogue.getCardById("minion_darkshire_councilman"));

			Assert.assertEquals(darkshireCouncilman.getAttack(), darkshireCouncilman.getBaseAttack() + 1);
			Assert.assertEquals(darkshireCouncilman2.getAttack(), darkshireCouncilman2.getBaseAttack());
			Assert.assertEquals(opponentMinion.getAttack(), opponentMinion.getBaseAttack());
		});
	}

	@Test
	public void testALightInTheDarkness() {
		GameContext context = createContext(HeroClass.SILVER, HeroClass.RED);
		Player player = context.getActivePlayer();
		final DiscoverAction[] action = {null};
		final Minion[] originalMinion = new Minion[1];
		final int[] handSize = new int[1];
		player.setBehaviour(new TestBehaviour() {
			boolean first = true;

			@Override
			public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
				if (first) {
					Assert.assertTrue(validActions.stream().allMatch(ga -> ga.getActionType() == ActionType.DISCOVER));
					action[0] = (DiscoverAction) validActions.get(0);
					MinionCard original = (MinionCard) action[0].getCard();
					originalMinion[0] = original.summon();
					handSize[0] = player.getHand().size();
				}
				first = false;
				return super.requestAction(context, player, validActions);
			}
		});
		SpellCard light = (SpellCard) CardCatalogue.getCardById("spell_a_light_in_the_darkness");
		playCard(context, player, light);
		Assert.assertEquals(player.getHand().size(), handSize[0] + 1);
		Card cardInHand = player.getHand().get(player.getHand().size() - 1);
		Assert.assertEquals(cardInHand.getCardId(), originalMinion[0].getSourceCard().getCardId());
		context.getLogic().performGameAction(player.getId(), cardInHand.play());
		int buff = light.getSpell().subSpells().filter(sd -> sd.getSpellClass().equals(BuffSpell.class)).findFirst().orElseThrow(AssertionError::new).getInt(SpellArg.VALUE, -999);
		Assert.assertEquals(player.getMinions().get(0).getAttack(), originalMinion[0].getAttack() + buff);
		Assert.assertEquals(player.getMinions().get(0).getHp(), originalMinion[0].getHp() + buff);
	}

	@Test
	public void testKingsDefenderHogger() {
		DebugContext context = createContext(HeroClass.RED, HeroClass.RED);
		Player player = context.getPlayer1();
		Hero hero = player.getHero();

		playCard(context, player, CardCatalogue.getCardById("weapon_deaths_bite"));
		Assert.assertEquals(hero.getWeapon().getAttack(), 4);
		Assert.assertEquals(hero.getWeapon().getDurability(), 2);
		playCard(context, player, CardCatalogue.getCardById("minion_hogger_doom_of_elwynn"));
		Assert.assertEquals(player.getMinions().size(), 1);
		playCard(context, player, CardCatalogue.getCardById("weapon_kings_defender"));
		Assert.assertEquals(hero.getWeapon().getAttack(), 3);
		Assert.assertEquals(hero.getWeapon().getDurability(), 2);
		Assert.assertEquals(player.getMinions().size(), 2);
	}


	@Test
	public void testRallyingBlade() {
		GameContext context = createContext(HeroClass.GOLD, HeroClass.BLACK);
		Player player = context.getPlayer1();
		MinionCard argentSquireCard = (MinionCard) CardCatalogue.getCardById("minion_argent_squire");
		Minion argentSquire = playMinionCard(context, player, argentSquireCard);
		Assert.assertEquals(argentSquire.getAttack(), 1);
		Assert.assertEquals(argentSquire.getHp(), 1);

		Card rallyingBladeCard = CardCatalogue.getCardById("weapon_rallying_blade");
		playCard(context, player, rallyingBladeCard);
		Assert.assertEquals(argentSquire.getAttack(), 2);
		Assert.assertEquals(argentSquire.getHp(), 2);
	}

	public static class YoggTestSpell1 extends Spell {
		private static final CountDownLatch counter = new CountDownLatch(3);

		public YoggTestSpell1() {
		}

		@Override
		@Suspendable
		protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
			Assert.assertEquals(target.getSourceCard().getCardId(), "spell_the_coin");
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
			Assert.assertEquals(target.getSourceCard().getCardId(), "spell_fireball");
			counter.countDown();
		}
	}
}

