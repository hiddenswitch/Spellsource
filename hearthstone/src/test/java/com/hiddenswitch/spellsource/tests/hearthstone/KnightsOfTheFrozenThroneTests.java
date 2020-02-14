package com.hiddenswitch.spellsource.tests.hearthstone;


import com.google.common.collect.Sets;
import com.hiddenswitch.spellsource.client.models.ActionType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.*;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.targeting.TargetSelection;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.game.cards.Attribute;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.summarizingInt;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class KnightsOfTheFrozenThroneTests extends TestBase {

	@Test
	public void testGlacialMysteries() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_bloodfen_raptor");
			shuffleToDeck(context, player, "secret_cat_trick");
			shuffleToDeck(context, player, "secret_cat_trick");
			shuffleToDeck(context, player, "secret_cheat_death");
			playCard(context, player, "spell_glacial_mysteries");
			assertEquals(player.getSecretCardIds(), Sets.newHashSet("secret_cat_trick", "secret_cheat_death"));
			assertEquals(player.getDeck().size(), 2);
			assertEquals(player.getDeck().stream().filter(c -> c.getCardId().equals("secret_cat_trick")).count(), 1L);
		});
	}

	@Test
	public void testShadowmourne() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_shadowmourne");
			context.endTurn();
			Minion left = playMinionCard(context, opponent, "minion_boulderfist_ogre");
			Minion target = playMinionCard(context, opponent, "minion_boulderfist_ogre");
			Minion right = playMinionCard(context, opponent, "minion_boulderfist_ogre");
			context.endTurn();
			context.performAction(player.getId(), player.getHeroPowerZone().get(0).play());
			attack(context, player, player.getHero(), target);
			Stream.of(left, target, right).forEach(minion -> {
				assertEquals(minion.getHp(), minion.getBaseHp() - CardCatalogue.getCardById("weapon_shadowmourne").getBaseDamage() - 1);
			});
		}, "BROWN", "BROWN");
	}

	@Test
	public void testStitchedTracker() {
		runGym((context, player, opponent) -> {
			Card card1 = shuffleToDeck(context, player, "minion_bloodfen_raptor");
			Card card2 = shuffleToDeck(context, player, "minion_bloodfen_raptor");
			overrideDiscover(context, player, discoverActions -> {
				assertEquals(discoverActions.size(), 1, "Discovers should be distinct");
				return discoverActions.get(0);
			});
			playCard(context, player, "minion_stitched_tracker");
			assertEquals(player.getDeck().size(), 2);
			assertEquals(player.getHand().size(), 1);
			assertNotEquals(player.getHand().get(0), card1);
			assertNotEquals(player.getHand().get(0), card2);
		});
	}

	@Test
	public void testAnimatedBerserker() {
		runGym((context, player, opponent) -> {
			Minion minion = playMinionCard(context, player, "minion_animated_berserker");
			assertEquals(minion.getHp(), minion.getBaseHp());
		});

		runGym((context, player, opponent) -> {
			Minion minion = playMinionCard(context, player, "minion_animated_berserker");
			Minion damaged = playMinionCard(context, player, "minion_bloodfen_raptor");
			assertEquals(minion.getHp(), minion.getBaseHp());
			assertEquals(damaged.getHp(), damaged.getBaseHp() - 1);
		});
	}

	@Test
	public void testEvolveHowlfiendInteraction() {
		for (int i = 0; i < 100; i++) {
			runGym((context, player, opponent) -> {
				context.setDeckFormat(DeckFormat.getFormat("Wild"));
				Minion howlfiend = playMinionCard(context, player, "minion_howlfiend");
				playCard(context, player, "spell_evolve");
				assertEquals(howlfiend.transformResolved(context).getSourceCard().getBaseManaCost(), CardCatalogue.getCardById("minion_howlfiend").getBaseManaCost() + 1);
			});
		}

		for (int i = 0; i < 100; i++) {
			runGym((context, player, opponent) -> {
				context.setDeckFormat(DeckFormat.getFormat("Wild"));
				Card howlfiendCard = receiveCard(context, player, "minion_howlfiend");
				playCard(context, player, "minion_emperor_thaurissan");
				for (int j = 0; j < 3; j++) {
					context.endTurn();
					context.endTurn();
				}
				assertEquals(costOf(context, player, howlfiendCard), 0);
				Minion howlfiend = playMinionCard(context, player, howlfiendCard);
				playCard(context, player, "spell_evolve");
				assertEquals(howlfiend.transformResolved(context).getSourceCard().getBaseManaCost(), CardCatalogue.getCardById("minion_howlfiend").getBaseManaCost() + 1);
			});
		}
	}

	@Test
	public void testDoomedApprentice() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_doomed_apprentice");
			context.endTurn();
			Card fireball = receiveCard(context, opponent, "spell_fireball");
			assertEquals(costOf(context, player, fireball), fireball.getBaseManaCost() + 1);
		});
	}

	@Test
	public void testShadowblade() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion bloodfen = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			playCard(context, player, "weapon_shadowblade");
			int hp = player.getHero().getHp();
			attack(context, player, player.getHero(), bloodfen);
			assertEquals(player.getHero().getHp(), hp);
		});
	}

	@Test
	public void testRollTheBones() {
		runGym((context, player, opponent) -> {
			GameLogic spyLogic = spy(context.getLogic());
			context.setLogic(spyLogic);

			AtomicInteger counter = new AtomicInteger(3);
			doAnswer(invocation -> {
				if (counter.getAndDecrement() > 0) {
					shuffleToDeck(context, player, "minion_loot_hoarder");
				} else {
					shuffleToDeck(context, player, "spell_the_coin");
				}
				return invocation.callRealMethod();
			}).when(spyLogic).drawCard(anyInt(), any());

			playCard(context, player, "spell_roll_the_bones");
			assertEquals(player.getHand().size(), 4);
		});
	}

	@Test
	public void testCannotAttackTwiceWithHero() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_wicked_knife");
			assertTrue(context.getValidActions().stream().anyMatch(ga ->
					ga.getActionType().equals(ActionType.PHYSICAL_ATTACK)
							&& ga.getSourceReference().equals(player.getHero().getReference())
							&& ga.getTargetReference().equals(opponent.getHero().getReference())));
			attack(context, player, player.getHero(), opponent.getHero());
			assertFalse(context.getValidActions().stream().anyMatch(ga ->
					ga.getActionType().equals(ActionType.PHYSICAL_ATTACK)
							&& ga.getSourceReference().equals(player.getHero().getReference())
							&& ga.getTargetReference().equals(opponent.getHero().getReference())));
			playCard(context, player, "hero_scourgelord_garrosh");
			assertFalse(context.getValidActions().stream().anyMatch(ga ->
					ga.getActionType().equals(ActionType.PHYSICAL_ATTACK)
							&& ga.getSourceReference().equals(player.getHero().getReference())
							&& ga.getTargetReference().equals(opponent.getHero().getReference())));
		});
	}

	@Test
	public void testSkelemancer() {
		runGym((context, player, opponent) -> {
			Minion skelemancer = playMinionCard(context, player, "minion_skelemancer");
			playCard(context, player, "spell_fireball", skelemancer);
			assertEquals(opponent.getMinions().size(), 0);
			assertEquals(player.getMinions().size(), 0);
		});

		runGym((context, player, opponent) -> {
			Minion skelemancer = playMinionCard(context, player, "minion_skelemancer");
			context.endTurn();
			playCard(context, opponent, "spell_fireball", skelemancer);
			assertEquals(opponent.getMinions().size(), 0);
			assertEquals(player.getMinions().size(), 1);
			assertTrue(skelemancer.isDestroyed());
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "token_skeletal_flayer");
		});
	}

	@Test
	public void testObsidianStatueAOEInteraction() {
		runGym((context, player, opponent) -> {
			Minion obsidian = playMinionCard(context, player, "minion_obsidian_statue");
			obsidian.setHp(1);
			Minion other = playMinionCard(context, player, "minion_bloodfen_raptor");
			context.endTurn();
			Minion shouldBeDestroyed = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			playCard(context, opponent, "spell_swipe", other);
			assertTrue(obsidian.isDestroyed());
			assertTrue(other.isDestroyed());
			assertTrue(shouldBeDestroyed.isDestroyed());
		});
	}

	@Test
	public void testIceBreaker() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion bloodfen = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			playCard(context, player, "weapon_ice_breaker");
			attack(context, player, player.getHero(), bloodfen);
			assertFalse(bloodfen.isDestroyed());
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion bloodfen = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			playCard(context, player, "spell_ice_lance", bloodfen);
			playCard(context, player, "weapon_ice_breaker");
			attack(context, player, player.getHero(), bloodfen);
			assertTrue(bloodfen.isDestroyed());
		});
	}

	@Test
	public void testPrinceTaldaramMalganisInteraction() {
		runGym((context, player, opponent) -> {
			Minion malganis = playMinionCard(context, player, "minion_malganis");
			Minion princeTaldaram = playMinionCard(context, player, "minion_prince_taldaram");
			assertEquals(malganis.getAttack(), malganis.getBaseAttack() + 2);
			assertEquals(malganis.getHp(), malganis.getBaseHp() + 2);
			assertEquals(princeTaldaram.getAttack(), 5);
			assertEquals(princeTaldaram.getHp(), 5);
		});
	}

	@Test
	public void testBringItOn() {
		runGym((context, player, opponent) -> {
			receiveCard(context, opponent, "minion_bloodfen_raptor");
			receiveCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "spell_bring_it_on");
			assertEquals(player.getHero().getArmor(), 10);
			context.endTurn();
			assertEquals(opponent.getHand().get(0).getCardId(), "minion_bloodfen_raptor");
			assertEquals(costOf(context, opponent, opponent.getHand().get(0)), 0);
			receiveCard(context, opponent, "minion_bloodfen_raptor");
			assertEquals(costOf(context, opponent, opponent.getHand().get(1)), 2);
			assertEquals(costOf(context, player, player.getHand().get(0)), 2, "The player's copy of Bloodfen Raptor should not have reduced cost.");
		});
	}

	@Test
	public void testArmyOfTheDead() {
		// 4 cards, no exception
		runGym((context, player, opponent) -> {
			int hp = player.getHero().getHp();
			Stream.generate(() -> "minion_bloodfen_raptor").map(CardCatalogue::getCardById).limit(4)
					.forEach(card -> context.getLogic().shuffleToDeck(player, card));
			playCard(context, player, "spell_army_of_the_dead");
			assertEquals(player.getMinions().size(), 4);
			assertEquals(player.getDeck().size(), 0, "All cards should have been put into play.");
			assertEquals(player.getHero().getHp(), hp, "Player should not have taken fatigue damage.");
		});
	}

	@Test
	public void testLeechingPoison() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion cho = playMinionCard(context, player, "minion_lorewalker_cho");
			context.endTurn();
			playCard(context, player, "spell_fireball", player.getHero());
			int startHp = player.getHero().getHp();
			playCard(context, player, "weapon_wicked_knife");
			playCard(context, player, "spell_leeching_poison");
			attack(context, player, player.getHero(), cho);
			assertEquals(player.getHero().getHp(), startHp + player.getHero().getWeapon().getAttack());
		});
	}

	@Test
	public void testFrostLichJaina() {
		runGym((context, player, opponent) -> {
			Minion tarCreeper = playMinionCard(context, player, "minion_tar_creeper");
			Minion bloodfen = playMinionCard(context, player, "minion_bloodfen_raptor");
			assertFalse(tarCreeper.hasAttribute(Attribute.LIFESTEAL));
			assertFalse(bloodfen.hasAttribute(Attribute.LIFESTEAL));
			playCard(context, player, "hero_frost_lich_jaina");
			assertTrue(player.getMinions().stream().anyMatch(c -> c.getSourceCard().getCardId().equals("minion_water_elemental")));
			assertTrue(tarCreeper.hasAttribute(Attribute.LIFESTEAL));
			assertFalse(bloodfen.hasAttribute(Attribute.LIFESTEAL));
			bloodfen.setHp(1);
			context.performAction(player.getId(), player.getHeroPowerZone().get(0).play().withTargetReference(bloodfen.getReference()));
			assertTrue(bloodfen.isDestroyed());
			final Minion waterElemental = player.getMinions().get(1);
			assertEquals(waterElemental.getSourceCard().getCardId(), "minion_water_elemental");
			assertTrue(waterElemental.hasAttribute(Attribute.LIFESTEAL));
			context.endTurn();
			Minion toSteal = playMinionCard(context, opponent, "minion_tar_creeper");
			assertFalse(toSteal.hasAttribute(Attribute.LIFESTEAL));
			context.endTurn();
			playCard(context, player, "spell_mind_control", toSteal);
			assertTrue(toSteal.hasAttribute(Attribute.LIFESTEAL));
		});
	}

	@Test
	public void testDeathGrip() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			shuffleToDeck(context, opponent, "minion_acolyte_of_pain");
			playCard(context, opponent, "minion_prince_keleseth");
			context.endTurn();
			playCard(context, player, "spell_death_grip");
			Minion acolyte = playMinionCard(context, player, player.getHand().get(0));
			assertEquals(acolyte.getAttack(), 2, "The stolen Acolyte should still have the Prince Keleseth buff applied to it.");
		});
	}

	@Test
	public void testStubbornGatropodObsidianStatueInteraction() {
		// Tests that a mysterious exception due to a minion already being dead doesn't occur
		runGym((context, player, opponent) -> {
			Minion obsidianStatue = playMinionCard(context, player, "minion_obsidian_statue");
			context.endTurn();
			Minion stubbornGastropod = playMinionCard(context, opponent, "minion_stubborn_gastropod");
			context.endTurn();
			attack(context, player, obsidianStatue, stubbornGastropod);
			assertEquals(player.getMinions().size(), 0);
			assertEquals(opponent.getMinions().size(), 0);
		});

		runGym((context, player, opponent) -> {
			Minion obsidianStatue = playMinionCard(context, player, "minion_obsidian_statue");
			obsidianStatue.setHp(1);
			context.endTurn();
			Minion stubbornGastropod = playMinionCard(context, opponent, "minion_stubborn_gastropod");
			context.endTurn();
			attack(context, player, obsidianStatue, stubbornGastropod);
			assertEquals(player.getMinions().size(), 0);
			assertEquals(opponent.getMinions().size(), 0);
		});

		runGym((context, player, opponent) -> {
			Minion stubbornGastropod = playMinionCard(context, player, "minion_stubborn_gastropod");
			context.endTurn();
			Minion obsidianStatue = playMinionCard(context, opponent, "minion_obsidian_statue");
			context.endTurn();
			attack(context, player, stubbornGastropod, obsidianStatue);
			assertEquals(player.getMinions().size(), 0);
			assertEquals(opponent.getMinions().size(), 0);
		});

		runGym((context, player, opponent) -> {
			Minion stubbornGastropod = playMinionCard(context, player, "minion_stubborn_gastropod");
			context.endTurn();
			Minion obsidianStatue = playMinionCard(context, opponent, "minion_obsidian_statue");
			obsidianStatue.setHp(1);
			context.endTurn();
			attack(context, player, stubbornGastropod, obsidianStatue);
			assertEquals(player.getMinions().size(), 0);
			assertEquals(opponent.getMinions().size(), 0);
		});
	}

	@Test
	@Disabled("inconsistent")
	@SuppressWarnings("unchecked")
	public void testDeathstalkerRexxar() {
		runGym((GameContext context, Player player, Player opponent) -> {
			Behaviour spiedBehavior = Mockito.spy(context.getBehaviours().get(player.getId()));
			context.setBehaviour(player.getId(), spiedBehavior);
			List<Card> cards = new ArrayList<>();
			AtomicBoolean isBuildingBeast = new AtomicBoolean(false);
			final Answer<GameAction> answer = invocation -> {

				if (isBuildingBeast.get()) {
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

			playCard(context, player, "hero_deathstalker_rexxar");
			isBuildingBeast.set(true);
			context.performAction(player.getId(), player.getHero().getHeroPower().play());
			isBuildingBeast.set(false);
			Card cardInHand = player.getHand().get(0);
			assertEquals(cardInHand.getBaseHp(), cards.stream().collect(summarizingInt(Card::getBaseHp)).getSum());
			assertEquals(cardInHand.getBaseAttack(), cards.stream().collect(summarizingInt(Card::getBaseAttack)).getSum());
			assertEquals(cardInHand.getBaseManaCost(), cards.stream().collect(summarizingInt(Card::getBaseManaCost)).getSum());
			playMinionCard(context, player, cardInHand);
			Minion playedCard = player.getMinions().stream().filter(c -> c.getSourceCard().getCardId().equals(cardInHand.getCardId())).findFirst().orElseThrow(AssertionError::new);
			assertEquals(playedCard.getBaseAttack(), cards.stream().collect(summarizingInt(Card::getBaseAttack)).getSum());
			assertEquals(playedCard.getBaseHp(), cards.stream().collect(summarizingInt(Card::getBaseHp)).getSum());
		});
	}

	@Test
	public void testLichKing() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_the_lich_king");
			context.endTurn();
			assertEquals(player.getHand().get(0).getHeroClass(), "SPIRIT");
		});
	}

	@Test
	public void testDefile() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion bloodfenRaptor = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			playCard(context, player, "spell_defile");
			assertEquals(bloodfenRaptor.getHp(), 1);
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion bloodfenRaptor = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			Minion patches = playMinionCard(context, opponent, "minion_patches_the_pirate");
			context.endTurn();
			playCard(context, player, "spell_defile");
			assertEquals(opponent.getMinions().size(), 0);
		});

		// If a minion is summoned mid-Defile, the defile should still continue
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion possessedVillager = playMinionCard(context, player, "minion_possessed_villager");
			context.endTurn();
			playCard(context, player, "spell_defile");
			assertTrue(possessedVillager.isDestroyed());
			assertEquals(opponent.getMinions().size(), 0);
		});

		// If defile causes the number of minions on the board to INCREASE, it should still continue as long as minions died
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion oneTwo = playMinionCard(context, player, "token_silver_hand_recruit");
			oneTwo.setHp(2);
			Minion oneToThree = playMinionCard(context, player, "minion_sated_threshadon");
			oneToThree.setHp(1);
			context.endTurn();

			// Starts with 2 minions, Thresh has 1 HP, SHR has 2 HP
			// Threshadon dies, -1 minion + 3 = 4 total minions. SHR and 3 murlocs have 1HP.
			// Should cast again, all minions should die

			playCard(context, player, "spell_defile");
			assertTrue(oneToThree.isDestroyed());
			assertTrue(oneTwo.isDestroyed());
			assertEquals(opponent.getMinions().size(), 0);
		});
	}

	@Test
	public void testMoorabi() {
		runGym((context, player, opponent) -> {
			Minion moorabi = playMinionCard(context, player, "minion_moorabi");
			Minion bloodfenRaptor = playMinionCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "spell_freezing_potion", bloodfenRaptor);
			assertEquals(player.getHand().size(), 1);
			assertEquals(player.getHand().get(0).getCardId(), "minion_bloodfen_raptor");
			playCard(context, player, "spell_freezing_potion", moorabi);
			assertEquals(player.getHand().size(), 1,
					"Freezing Moorabi should not put a copy of Moorabi into your hand.");
			context.endTurn();
			Minion noviceEngineer = playMinionCard(context, player, "minion_novice_engineer");
			playCard(context, player, "spell_freezing_potion", noviceEngineer);
			assertEquals(player.getHand().size(), 2);
			assertEquals(player.getHand().get(1).getCardId(), "minion_novice_engineer");
		});
	}

	@Test
	public void testValeeraTheHollow() {
		runGym((context, player, opponent) -> {
			player.setMaxMana(10);
			player.setMana(10);
			playCard(context, player, "hero_valeera_the_hollow");
			assertTrue(player.getHand().containsCard("token_shadow_reflection"));
			assertFalse(context.getLogic().canPlayCard(player.getId(),
					player.getHand().get(0).getReference()),
					"You should not be able to play the Shadow Reflection because it doesn't do anything until a card is played.");
			playCard(context, player, "minion_wisp");
			assertTrue(context.getLogic().canPlayCard(player.getId(), player.getHand().get(0).getReference()),
					"Since you have 1 mana left and we last played a Wisp, the Shadow Reflection should have transformed into the Wisp and it should be playable.");
			context.endTurn();
			assertEquals(player.getHand().size(), 0, "The Shadow Reflection-as-Wisp should have removed itself from the player's hand");
			Minion bluegillWarrior = playMinionCard(context, opponent, "minion_bluegill_warrior");
			List<GameAction> validActions = context.getValidActions();
			assertFalse(validActions.stream().anyMatch(ga -> ga.getActionType() == ActionType.PHYSICAL_ATTACK
							&& ga.getTargetReference().equals(player.getHero().getReference())),
					"Valeera has STEALTH, so she should not be targetable by the Bluegill Warrior");
			context.endTurn();
			assertTrue(player.getHand().containsCard("token_shadow_reflection"));
			playCard(context, player, "minion_water_elemental");
			playCard(context, player, "minion_wisp");
			assertEquals(player.getHand().get(0).getCardId(), "minion_wisp",
					"Since Wisp was the last card the player played, Shadow Reflection should be a Wisp");
			context.endTurn();
			playCard(context, opponent, "minion_mindbreaker");
			context.endTurn();
			assertEquals(player.getHand().size(), 0, "The presence of Mindbreaker should prevent Shadow Reflection from entering the player's hand.");
		});
	}

	@Test
	public void testDoomerang() {
		runGym((context, player, opponent) -> {
			// 4/2, Deathrattle deals 1 damage to all minions
			playCard(context, player, "weapon_deaths_bite");

			assertTrue(player.getHero().getWeapon().isActive());
			context.endTurn();
			Minion tarCreeper1 = playMinionCard(context, player, CardCatalogue.getCardById("minion_tar_creeper"));
			Minion tarCreeper2 = playMinionCard(context, player, CardCatalogue.getCardById("minion_tar_creeper"));
			context.endTurn();
			context.performAction(player.getId(), new PhysicalAttackAction(player.getHero().getReference()).withTargetReference(tarCreeper1.getReference()));
			assertEquals(tarCreeper1.getHp(), 1);
			playCard(context, player, CardCatalogue.getCardById("spell_doomerang"), tarCreeper2);
			assertEquals(tarCreeper1.getHp(), 1, "Deathrattle should not have triggered and should not have killed the first Tar Creeper.");
			assertEquals(tarCreeper2.getHp(), 1, "The second Tar Creeper should have been damaged by the Doomerang");
			Card card = player.getHand().get(player.getHand().getCount() - 1);
			assertEquals(card.getSourceCard().getCardId(), "weapon_deaths_bite", "Death's Bite should now be in the player's hand.");
			assertEquals(player.getWeaponZone().size(), 0);
			context.performAction(player.getId(), card.play());
			assertEquals(player.getHero().getWeapon().getDurability(), 2, "Death's Bite should have 2 durability, not 1, since it was played fresh from the hand.");
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_spectral_cutlass");

			Minion tarCreeper1 = playMinionCard(context, player, CardCatalogue.getCardById("minion_tar_creeper"));
			playCard(context, player, "spell_fireball", player.getHero());
			playCard(context, player, "spell_envenom_weapon");

			assertEquals(player.getHero().getHp(), 24);
			playCard(context, player, "spell_doomerang", tarCreeper1);
			assertEquals(player.getHero().getHp(), 26);
			assertTrue(tarCreeper1.isDestroyed());
		});
	}

	@Test
	public void testEternalServitude() {
		runGym((context, player, opponent) -> {
			Minion friendlyMinion = playMinionCard(context, player, "minion_bloodfen_raptor");
			context.endTurn();
			Minion opposingMinion = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			attack(context, player, friendlyMinion, opposingMinion);
			assertEquals(player.getMinions().size(), 0);
			playCard(context, player, "spell_eternal_servitude");
			assertEquals(player.getMinions().size(), 1);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_bloodfen_raptor");
		});

		runGym((context, player, opponent) -> {
			Minion target1 = playMinionCard(context, player, "minion_bloodfen_raptor");
			Minion target2 = playMinionCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "spell_fireball", target1);
			playCard(context, player, "spell_fireball", target2);
			assertEquals(player.getMinions().size(), 0);
			overrideDiscover(context, player, discoverActions -> {
				assertEquals(discoverActions.size(), 1, "Discover actions should be distinct");
				return discoverActions.get(0);
			});
			playCard(context, player, "spell_eternal_servitude");
			assertEquals(player.getMinions().size(), 1);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_bloodfen_raptor");
		});
	}

	@Test
	public void testSpiritLash() {
		runGym((context, player, opponent) -> {
			int originalHp = 1;
			player.getHero().setHp(originalHp);
			int expectedHealing = 5;
			for (int i = 0; i < expectedHealing; i++) {
				playMinionCard(context, player, "minion_bloodfen_raptor");
			}
			playCard(context, player, "spell_spirit_lash");
			assertEquals(player.getHero().getHp(), originalHp + expectedHealing);
		});
	}

	@Test
	public void testShadowEssence() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_bloodfen_raptor");

			playCard(context, player, "spell_shadow_essence");

			Minion minion = player.getMinions().get(0);
			assertEquals(minion.getAttack(), 5);
			assertEquals(minion.getHp(), 5);
		});
	}

	@Test
	public void testEmbraceDarkness() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion bloodfenRaptor = playMinionCard(context, opponent, CardCatalogue.getCardById("minion_bloodfen_raptor"));
			context.endTurn();
			playCard(context, player, CardCatalogue.getCardById("spell_embrace_darkness"), bloodfenRaptor);
			assertEquals(bloodfenRaptor.getOwner(), opponent.getId());
			context.endTurn();
			assertEquals(bloodfenRaptor.getOwner(), opponent.getId());
			for (int i = 0; i < 4; i++) {
				context.endTurn();
				assertEquals(bloodfenRaptor.getOwner(), player.getId());
			}
		});
	}

	@Test
	public void testArchibishopBenedictus() {
		runGym((context, player, opponent) -> {
			Stream.of("minion_water_elemental", "minion_bloodfen_raptor")
					.map(CardCatalogue::getCardById)
					.forEach(c -> context.getLogic().shuffleToDeck(opponent, c));
			playCard(context, player, "minion_archbishop_benedictus");
			assertEquals(player.getDeck().size(), 2);
			assertEquals(opponent.getDeck().size(), 2);
			assertTrue(player.getDeck().containsCard("minion_water_elemental"));
			assertTrue(player.getDeck().containsCard("minion_bloodfen_raptor"));
		});

	}

	@Test
	public void testPrinceKeleseth() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_prince_keleseth");
			context.endTurn();
			context.endTurn();
			Minion bloodfen = playMinionCard(context, player, player.getHand().get(0));
			assertEquals(bloodfen.getAttack(), bloodfen.getBaseAttack());
			assertEquals(bloodfen.getHp(), bloodfen.getBaseHp());
		});

		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "token_barnabus_the_stomper");
			playCard(context, player, "minion_prince_keleseth");
			context.endTurn();
			context.endTurn();
			Minion bloodfen = playMinionCard(context, player, player.getHand().get(0));
			assertEquals(bloodfen.getAttack(), bloodfen.getBaseAttack() + 1);
			assertEquals(bloodfen.getHp(), bloodfen.getBaseHp() + 1);
		});
	}

	@Test
	public void testPrinceTaldaram() {
		runGym((context, player, opponent) -> {
			Minion waterElemental = playMinionCard(context, player, CardCatalogue.getCardById("minion_water_elemental"));
			Minion princeTaldaram = playMinionCard(context, player, CardCatalogue.getCardById("minion_prince_taldaram"));
			assertEquals(princeTaldaram.getZone(), Zones.BATTLEFIELD);
			assertEquals(princeTaldaram.getAttack(), 3);
			assertEquals(princeTaldaram.getHp(), 3);
			context.endTurn();
			Minion arcaneGiant = playMinionCard(context, opponent, CardCatalogue.getCardById("minion_arcane_giant"));
			context.endTurn();
			context.getLogic().fight(player, princeTaldaram, arcaneGiant, null);
			assertTrue(arcaneGiant.hasAttribute(Attribute.FROZEN));
		});
	}

	@Test
	public void testMeatWagon() {
		runGym((context, player, opponent) -> {
			Stream.of("minion_dragon_egg" /*0*/, "minion_voidwalker" /*1*/, "minion_bloodfen_raptor" /*3*/)
					.map(CardCatalogue::getCardById)
					.forEach(c -> context.getLogic().shuffleToDeck(player, c));

			Minion meatWagon = playMinionCard(context, player, CardCatalogue.getCardById("minion_meat_wagon"));
			context.getLogic().destroy(meatWagon);
			assertEquals(player.getMinions().size(), 1);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_dragon_egg");

			// Remove dragon egg
			player.getDeck().stream().filter(c -> c.getCardId().equals("minion_dragon_egg"))
					.findFirst()
					.orElseThrow(AssertionError::new).moveOrAddTo(context, Zones.GRAVEYARD);

			meatWagon = playMinionCard(context, player, CardCatalogue.getCardById("minion_meat_wagon"));
			playCard(context, player, CardCatalogue.getCardById("spell_divine_strength" /*+1/+2*/), meatWagon);
			assertEquals(meatWagon.getAttack(), 2);
			context.getLogic().destroy(meatWagon);
			assertEquals(player.getMinions().size(), 2);
			assertEquals(player.getMinions().get(1).getSourceCard().getCardId(), "minion_voidwalker");
		});
	}

	@Test
	public void testFurnacefireColossus() {
		runGym((context, player, opponent) -> {
			Stream.of("weapon_arcanite_reaper" /*5/2*/, "weapon_coghammer" /*2/3*/, "minion_bloodfen_raptor")
					.map(CardCatalogue::getCardById)
					.forEach(c -> context.getLogic().receiveCard(player.getId(), c));
			Minion furnacefireColossus = playMinionCard(context, player, CardCatalogue.getCardById("minion_furnacefire_colossus"));
			assertEquals(furnacefireColossus.getAttack(), 6 + 5 + 2);
			assertEquals(furnacefireColossus.getHp(), 6 + 2 + 3);
			assertEquals(player.getHand().size(), 1);
		});

	}

	@Test
	public void testFrostmourne() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_frostmourne");
			Minion leeroyJenkins = playMinionCard(context, player, CardCatalogue.getCardById("minion_leeroy_jenkins"));

			Minion bloodfenRaptor = playMinionCard(context, opponent, CardCatalogue.getCardById("minion_bloodfen_raptor"));
			Minion waterElemental = playMinionCard(context, opponent, CardCatalogue.getCardById("minion_water_elemental"));

			PhysicalAttackAction heroAttack = new PhysicalAttackAction(player.getHero().getReference());
			heroAttack.setTarget(bloodfenRaptor);

			Weapon frostmourne = player.getHero().getWeapon();
			frostmourne.setAttribute(Attribute.HP, 1);

			PhysicalAttackAction leeroyAttack = new PhysicalAttackAction(leeroyJenkins.getReference());
			leeroyAttack.setTarget(waterElemental);
			context.performAction(player.getId(), leeroyAttack);
			context.performAction(player.getId(), heroAttack);

			assertEquals(player.getMinions().size(), 1);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_bloodfen_raptor");
		});
	}

	@Test
	public void testSimulacrum() {
		runGym((context, player, opponent) -> {
			// TODO: Should Simulacrum always copy the most recently drawn card when there are multiple cards of the same
			// mana cost?
			Stream.of("minion_water_elemental" /*4*/,
					"spell_the_coin"/*0*/,
					"minion_acolyte_of_pain"/*3*/)
					.map(CardCatalogue::getCardById)
					.forEach(c -> context.getLogic().receiveCard(player.getId(), c));

			playMinionCard(context, player, CardCatalogue.getCardById("minion_emperor_thaurissan"));
			context.endTurn();
			context.endTurn();
			context.endTurn();
			context.endTurn();
			// Now the minions in the hand are 2, 1
			receiveCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "spell_simulacrum");
			assertEquals(player.getHand().stream().filter(c -> c.getCardId().equals("minion_acolyte_of_pain")).count(), 2L);
		});


		// Test simulacrum with no minion cards
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_simulacrum");
			assertEquals(player.getHand().size(), 0);
		});
	}

	@Test
	public void testIceWalker() {
		// Notes on interaction from https://twitter.com/ThePlaceMatt/status/891810684551311361
		// Any Hero Power that targets will also freeze that target. So it does work with Frost Lich Jaina. (And even
		// works with Anduin's basic HP!)
		checkIceWalker(true, new String[]{
				"BLUE",
				"WHITE"
		});
		checkIceWalker(false, new String[]{
				"BLACK",
				"VIOLET",
				"BROWN",
				"GOLD",
				"SILVER",
				"RED",
				"GREEN"
		});
	}

	private void checkIceWalker(final boolean expected, final String[] classes) {
		for (String heroClass : classes) {
			runGym((context, player, opponent) -> {
				player.setMaxMana(2);
				player.setMana(2);

				Minion icyVeins = playMinionCard(context, player, CardCatalogue.getCardById("minion_ice_walker"));
				PlayCardAction play = player.getHero().getHeroPower().play();
				Entity target;

				if (play.getTargetRequirement() != TargetSelection.NONE) {
					target = opponent.getHero();
				} else {
					List<Entity> entities = context.resolveTarget(player, player.getHero().getHeroPower(), player.getHero().getHeroPower().getSpell().getTarget());
					if (entities == null || entities.size() == 0) {
						target = null;
					} else {
						target = entities.get(0);
					}
				}

				play.setTarget(target);
				context.performAction(player.getId(), play);
				if (target == null) {
					assertFalse(expected);
				} else {
					assertEquals(target.hasAttribute(Attribute.FROZEN), expected);
				}
				context.endTurn();
				context.endTurn();
				context.getLogic().destroy(icyVeins);
				play = player.getHero().getHeroPower().play();
				play.setTarget(target);
				context.performAction(player.getId(), play);
				if (target == null) {
					assertFalse(expected);
				} else {
					assertFalse(target.hasAttribute(Attribute.FROZEN));
				}
			}, heroClass, "BLUE");
		}
	}

	@Test
	public void testSpreadingPlague() {
		Stream.of(0, 3, 7).forEach(minionCount -> {
			GameContext context = createContext("BROWN", "BROWN");
			Player player = context.getActivePlayer();
			Player opponent = context.getOpponent(player);
			context.endTurn();
			for (int i = 0; i < minionCount; i++) {
				playCard(context, opponent, "minion_wisp");
			}
			context.endTurn();
			player.setMaxMana(6);
			player.setMana(6);
			playCard(context, player, "spell_spreading_plague");
			// Should summon at least one minion
			assertEquals(player.getMinions().size(), Math.max(minionCount, 1));
		});
	}

	@Test
	public void testMalfurionThePestilent() {
		BiFunction<GameContext, String, GameContext> joinHeroPowerCardId = (context, heroPowerCardId) -> {
			Player player = context.getPlayer1();

			HeroPowerAction action = context.getValidActions().stream().filter(ga -> ga.getActionType() == ActionType.HERO_POWER)
					.map(ga -> (HeroPowerAction) ga)
					.filter(ga -> ga.getChoiceCardId().equals(heroPowerCardId))
					.findFirst().orElseThrow(AssertionError::new);

			context.performAction(player.getId(), action);

			return context;
		};

		Stream<Consumer<GameContext>> heroPowerChecks = Stream.of(
				(context) -> {
					assertEquals(context.getPlayer1().getHero().getAttack(), 3);
				},
				(context) -> {
					// Expect 5 armor + additional  3 from hero power
					assertEquals(context.getPlayer1().getHero().getArmor(), 8);
				}
		);

		Stream<String> heroPowers = Stream.of("hero_power_plague_lord1", "hero_power_plague_lord2");

		List<Object> allChecked = zip(heroPowers, heroPowerChecks, (heroPowerCardId, heroPowerChecker) -> {
			Stream<GameContext> contexts = Stream.of((Function<Card, PlayCardAction>) (card -> card.playOptions()[0]),
					card -> card.playOptions()[1],
					Card::playBothOptions)
					.map(actionGetter -> {
						Card malfurion = (Card) CardCatalogue.getCardById("hero_malfurion_the_pestilent");

						GameContext context1 =createContext(HeroClass.BROWN, HeroClass.RED, true, DeckFormat.getFormat("Wild"));
						Player player = context1.getPlayer1();
						clearHand(context1, player);
						clearZone(context1, player.getDeck());

						context1.getLogic().receiveCard(player.getId(), malfurion);
						// Mana = 7 for Hero + 2 for the hero power
						player.setMaxMana(9);
						player.setMana(9);

						PlayCardAction action = actionGetter.apply(malfurion);
						context1.performAction(player.getId(), action);
						assertEquals(player.getHero().getArmor(), 5);

						// Assert that the player has both choose one hero powers present
						assertEquals(context1.getValidActions().stream()
								.filter(ga -> ga.getActionType() == ActionType.HERO_POWER)
								.count(), 2L);

						return context1;
					});

			Stream<Function<GameContext, GameContext>> battlecryChecks = Stream.of((context1) -> {
						Player player = context1.getPlayer1();

						assertEquals(player.getMinions().size(), 2);
						assertTrue(player.getMinions().stream().allMatch(m -> m.getSourceCard().getCardId().equals("token_frost_widow")));
						return context1;
					},
					(context11) -> {
						Player player = context11.getPlayer1();

						assertEquals(player.getMinions().size(), 2);
						assertTrue(player.getMinions().stream().allMatch(m -> m.getSourceCard().getCardId().equals("token_scarab_beetle")));
						return context11;
					},
					(context12) -> {
						Player player = context12.getPlayer1();

						assertEquals(player.getMinions().size(), 4);
						int scarabs = 0;
						int frosts = 0;
						for (Minion minion : player.getMinions()) {
							if (minion.getSourceCard().getCardId().equals("token_frost_widow")) {
								frosts += 1;
							}
							if (minion.getSourceCard().getCardId().equals("token_scarab_beetle")) {
								scarabs += 1;
							}
						}
						assertEquals(scarabs, 2);
						assertEquals(frosts, 2);
						return context12;
					});

			// Do everything: zip the contexts with the battlecry checks, which executes the battlecry
			// then try the hero power specified hero and check that it worked
			zip(contexts, battlecryChecks, (context, checker) -> checker.apply(context))
					.map(context -> joinHeroPowerCardId.apply(context, heroPowerCardId))
					.forEach(heroPowerChecker);

			return null;
		}).collect(toList());
	}


	@Test
	public void testUtherOfTheEbonBlade() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "hero_uther_of_the_ebon_blade");
			//doombubbles here, reworking the hero power based on a trigger rather than use, so playing all these guys from hand should win the game
			playCard(context, player, "token_deathlord_nazgrim");
			playCard(context, player, "token_darion_mograine");
			playCard(context, player, "token_inquisitor_whitemane");
			playCard(context, player, "token_thoras_trollbane");
			assertTrue(opponent.getHero().isDestroyed(), "yay");
		});
	}

	@Test
	public void testFrostlichJainaRestOfGame() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "hero_frost_lich_jaina");
			playCard(context, player, "hero_scourgelord_garrosh");
			playCard(context, player, "minion_water_elemental");
			for (Minion minion : player.getMinions()) {
				assertTrue(minion.hasAttribute(Attribute.LIFESTEAL) || minion.hasAttribute(Attribute.AURA_LIFESTEAL),
						"These guys should have lifesteal even after we're not Jaina anymore");
			}
		});
	}
}
