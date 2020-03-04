package com.hiddenswitch.spellsource.tests.hearthstone;


import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import com.hiddenswitch.spellsource.client.models.ActionType;
import net.demilich.metastone.game.actions.DiscoverAction;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.trigger.secrets.Quest;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.tests.util.GymFactory;
import net.demilich.metastone.tests.util.OverrideDiscoverBehaviour;
import net.demilich.metastone.tests.util.TestBehaviour;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.*;

public class JourneyToUngoroTests extends TestBase {

	@Test
	public void testStonehillDefender() {
		for (int i = 0; i < 1000; i++) {
			runGym((context, player, opponent) -> {
				context.setDeckFormat(DeckFormat.getFormat("All"));
				// Check that stonehill never gets a spellsource minion
				overrideDiscoverChoice(da -> {
					assertTrue(da.stream().noneMatch(c -> c.getCard().getCardSet() == "SPELLSOURCE" && c.getCard().getCardSet() == "TEST"));
					return da.get(0);
				});
				playMinionCard(context, player, "minion_stonehill_defender");
			});
		}
	}

	@Test
	public void testBittertideHydraVolcanoInteraction() {
		runGym((context, player, opponent) -> {
			Minion bittertide = playMinionCard(context, player, "minion_bittertide_hydra");
			context.endTurn();
			int playerHp = player.getHero().getHp();
			playCard(context, opponent, "spell_volcano");
			assertEquals(player.getHero().getHp(), playerHp - bittertide.getBaseHp() * 3);
		});
	}

	@Test
	public void testViciousFledgling() {
		runGym((context, player, opponent) -> {
			Minion fledgling = playMinionCard(context, player, "minion_vicious_fledgling");
			context.endTurn();
			context.endTurn();
			AtomicInteger count = new AtomicInteger(0);
			OverrideDiscoverBehaviour beh = overrideDiscoverChoice(discoverActions -> {
				count.incrementAndGet();
				return discoverActions.get(0);
			});
			context.setBehaviour(player.getId(), beh);
			attack(context, player, fledgling, opponent.getHero());
			attack(context, player, fledgling, opponent.getHero());
			assertEquals(count.get(), 2);
		});
	}

	@Test
	public void testLakkariSacrifice() {
		// Deathwing discards should count towards Lakkari Sacrifice
		runGym((context, player, opponent) -> {
			playCard(context, player, "quest_lakkari_sacrifice");
			List<Card> discardedCards = Stream
					.generate(() -> receiveCard(context, player, "minion_bloodfen_raptor"))
					.map(o -> (Card) o)
					.limit(9)
					.collect(toList());

			final Quest quest = player.getQuests().get(0);
			playCard(context, player, "minion_deathwing");
			discardedCards.forEach(c -> assertTrue(c.hasAttribute(Attribute.DISCARDED)));
			assertTrue(quest.isExpired());
			assertEquals(player.getHand().get(0).getCardId(), "spell_nether_portal");
		});
	}

	@Test
	public void testCruelDinomancer() {
		runGym((context, player, opponent) -> {
			Card bloodfen = receiveCard(context, player, "minion_bloodfen_raptor");
			// Ensure that cards that merely died don't get summoned
			Minion target = playMinionCard(context, player, "minion_doomguard");
			assertTrue(bloodfen.hasAttribute(Attribute.DISCARDED));
			playCard(context, player, "spell_pyroblast", target);
			assertTrue(target.isDestroyed());
			Minion dinomancer = playMinionCard(context, player, "minion_cruel_dinomancer");
			playCard(context, player, "spell_fireball", dinomancer);
			assertTrue(player.getMinions().stream().anyMatch(m -> m.getSourceCard().getCardId().equals("minion_bloodfen_raptor")));
		});
	}

	@Test
	public void testCavernsBelowChooseOneInteraction() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "quest_the_caverns_below");
			player.setMaxMana(10);
			player.setMana(10);
			playChooseOneCard(context, player, "spell_dark_wispers", "spell_dark_wispers_1");
			playChooseOneCard(context, player, "spell_dark_wispers", "spell_dark_wispers_2");
			playChooseOneCard(context, player, "spell_dark_wispers", "spell_dark_wispers_1");
			playChooseOneCard(context, player, "spell_dark_wispers", "spell_dark_wispers_2");
		});
	}

	@Test
	public void testChargedDevlisaur() {
		runGym((context, player, opponent) -> {
			Minion devilsaur = playMinionCard(context, player, "minion_charged_devilsaur");
			assertFalse(context.getValidActions().stream().anyMatch(ga ->
					ga.getActionType() == ActionType.PHYSICAL_ATTACK
							&& ga.getSourceReference().equals(devilsaur.getReference())
							&& ga.getTargetReference().equals(opponent.getHero().getReference())));
		});

		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_charged_devilsaur");
			playCard(context, player, "spell_gather_your_party");
			Minion devilsaur = player.getMinions().get(0);
			assertTrue(context.getValidActions().stream().anyMatch(ga ->
					ga.getActionType() == ActionType.PHYSICAL_ATTACK
							&& ga.getSourceReference().equals(devilsaur.getReference())
							&& ga.getTargetReference().equals(opponent.getHero().getReference())));
		});
	}

	@Test
	public void testPoisonousKillsHeroes() {
		runGym((context, player, opponent) -> {
			Minion cobra = playMinionCard(context, player, "minion_emperor_cobra");
			int opponentHp = opponent.getHero().getHp();
			attack(context, player, cobra, opponent.getHero());
			assertEquals(opponent.getHero().getHp(), opponentHp - cobra.getAttack());
			assertFalse(opponent.isDestroyed());
		});
	}

	@Test
	public void testNestingRoc() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_bloodfen_raptor");
			Minion nestingRoc = playMinionCard(context, player, "minion_nesting_roc");
			assertTrue(nestingRoc.hasAttribute(Attribute.TAUNT));
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_bloodfen_raptor");
			Minion nestingRoc = playMinionCard(context, player, "minion_nesting_roc");
			assertFalse(nestingRoc.hasAttribute(Attribute.TAUNT));
		});
	}

	@Test
	public void testMeteor() {
		runGym((context, player, opponent) -> {
			Minion minion1 = playMinionCard(context, player, "minion_bloodfen_raptor");
			Minion minion2 = playMinionCard(context, player, "minion_bloodfen_raptor");
			Minion minion3 = playMinionCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "spell_meteor", minion2);
			assertTrue(minion1.isDestroyed());
			assertTrue(minion2.isDestroyed());
			assertTrue(minion3.isDestroyed());
		});

		for (int j0 = 0; j0 < 3; j0++) {
			final int j = j0;
			runGym((context, player, opponent) -> {
				context.endTurn();
				List<Minion> minions = IntStream.range(0, 3).mapToObj(i -> playMinionCard(context, opponent, "minion_argent_squire")).collect(toList());
				context.endTurn();
				playCard(context, player, "spell_meteor", minions.get(j));
				for (int k = 0; k < 3; k++) {
					assertFalse(opponent.getMinions().get(k).isDestroyed());
					assertEquals(opponent.getMinions().get(k).hasAttribute(Attribute.DIVINE_SHIELD), k < j - 1 || k > j + 1);
				}
			});
		}
	}

	@Test
	public void testCrystalCore() {
		Consumer<Minion> checkMinion = (Minion minion) -> {
			assertEquals(minion.getAttack(), 4);
			assertEquals(minion.getHp(), 4);
		};

		// Check regular summoning from hand and mind control
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_crystal_core");
			Minion minion1 = playMinionCard(context, player, "minion_bloodfen_raptor");
			checkMinion.accept(minion1);
			context.endTurn();
			Minion minion2 = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			playCard(context, player, "spell_mind_control", minion2);
			checkMinion.accept(minion2);
		});

		// Check resurrection
		runGym((context, player, opponent) -> {
			Minion minion1 = playMinionCard(context, player, "minion_bloodfen_raptor");
			context.endTurn();
			playCard(context, opponent, "spell_fireball", minion1);
			context.endTurn();
			playCard(context, player, "spell_crystal_core");
			playCard(context, player, "spell_diamond_spellstone");
			checkMinion.accept(player.getMinions().get(0));
		});

		// Check that silencing a minion doesn't remove the crystal core effect
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_crystal_core");
			Minion minion1 = playMinionCard(context, player, "minion_bloodfen_raptor");
			checkMinion.accept(minion1);
			context.endTurn();
			playCard(context, opponent, "spell_silence", minion1);
			context.endTurn();
			checkMinion.accept(minion1);
		});
	}

	@Test
	public void testCrystalCoreExistingMinionsInteraction() {
		Consumer<Minion> checkMinion = (Minion minion) -> {
			assertEquals(minion.getAttack(), 4);
			assertEquals(minion.getHp(), 4);
		};

		// Check that existing minions on the board get buffed
		runGym((context, player, opponent) -> {
			Minion minion1 = playMinionCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "spell_crystal_core");
			checkMinion.accept(minion1);
		});
	}

	@Test
	public void testTheCavernsBelowReturnTargetToHandInteraction() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "quest_the_caverns_below");
			Minion returnTarget = playMinionCard(context, player, "minion_neutral_test");
			Quest caverns = player.getQuests().get(0);
			assertEquals(caverns.getAttributeValue(Attribute.RESERVED_INTEGER_1), 1);
			assertEquals(player.getHand().size(), 0);
			playMinionCard(context, player, "minion_ancient_brewmaster", returnTarget);
			assertEquals(player.getMinions().size(), 1);
			assertEquals(player.getHand().size(), 1);
			assertEquals(caverns.getAttributeValue(Attribute.RESERVED_INTEGER_1), 1);
			playCard(context, player, player.getHand().get(0));
			assertEquals(caverns.getAttributeValue(Attribute.RESERVED_INTEGER_1), 2, "Two neutral tests played.");
			// Bounce again
			playMinionCard(context, player, "minion_ancient_brewmaster", player.getMinions().get(1));
			playCard(context, player, player.getHand().get(0));
			assertEquals(caverns.getAttributeValue(Attribute.RESERVED_INTEGER_1), 3, "Three neutral tests played.");
			assertEquals(caverns.getFires(), 3, "Fires should be reported accurately.");
		});
	}

	@Test
	public void testTheCavernsBelow() {
		// Plain test
		runGym((context, player, opponent) -> {
			playCard(context, player, "quest_the_caverns_below");
			Stream.of("minion_bloodfen_raptor", "minion_bloodfen_raptor", "minion_bloodfen_raptor", "minion_bloodfen_raptor",
					"spell_mirror_image", "spell_mirror_image", "spell_mirror_image", "spell_mirror_image")
					.peek(cid -> playCard(context, player, cid)).peek(ignored -> assertEquals(player.getHand().size(), 0))
					.collect(toList());

			playCard(context, player, "spell_mirror_image");
			// Mirror image should not count
			assertEquals(player.getHand().size(), 0);

			playCard(context, player, "spell_twisting_nether");
			playCard(context, player, "minion_bloodfen_raptor");
			assertEquals(player.getHand().get(0).getCardId(), "spell_crystal_core");
		});

		// Tokens summoned by other cards shouldn't count
		runGym((context, player, opponent) -> {
			playCard(context, player, "quest_the_caverns_below");
			playCard(context, player, "minion_moroes");
			for (int i = 0; i < 5; i++) {
				context.endTurn();
				context.endTurn();
			}
			assertEquals(player.getMinions().size(), 6);
			assertEquals(player.getMinions().stream()
					.map(Minion::getSourceCard)
					.map(Card::getCardId)
					.filter(cid -> cid.equals("token_steward")).count(), 5L);

			assertEquals(player.getHand().size(), 0);
		});

		// Cards of the same name but different source card should count
		runGym((context, player, opponent) -> {
			playCard(context, player, "quest_the_caverns_below");
			for (int i = 0; i < 3; i++) {
				playCard(context, player, "token_treant_taunt");
				playCard(context, player, "token_treant");
			}
			assertEquals(player.getHand().get(0).getCardId(), "spell_crystal_core");
		});

		// Cards summoned by your opponent should not count
		runGym((context, player, opponent) -> {
			playCard(context, player, "quest_the_caverns_below");
			Stream.of("minion_bloodfen_raptor", "minion_bloodfen_raptor", "minion_bloodfen_raptor", "minion_bloodfen_raptor")
					.peek(cid -> playCard(context, player, cid)).peek(ignored -> assertEquals(player.getHand().size(), 0))
					.collect(toList());

			assertEquals(player.getHand().size(), 0);
			context.endTurn();
			playCard(context, opponent, "minion_bloodfen_raptor");
			assertEquals(player.getHand().size(), 0);
			context.endTurn();
			playCard(context, player, "minion_bloodfen_raptor");
			assertEquals(player.getHand().get(0).getCardId(), "spell_crystal_core");
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "quest_the_caverns_below");
			for (int i = 0; i < 10; i++) {
				shuffleToDeck(context, player, "minion_wisp");
				receiveCard(context, player, "minion_novice_engineer");
			}
			for (int i = 0; i < 5; i++) {
				playCard(context, player, player.getHand().get(0));
			}
			assertTrue(player.getHand().filtered(c -> c.getCardId().equals("spell_crystal_core")).isEmpty());
		});
	}

	@Test
	public void testTarCreeper() {
		runGym((context, player, opponent) -> {
			Minion tarCreeper = playMinionCard(context, player, "minion_tar_creeper");
			assertEquals(tarCreeper.getAttack(), 1);
			context.endTurn();
			assertEquals(tarCreeper.getAttack(), 3);
			context.endTurn();
			assertEquals(tarCreeper.getAttack(), 1);
		});

		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_tar_creeper");
			context.endTurn();
			playCard(context, opponent, "minion_dirty_rat");
			Minion tarCreeper = player.getMinions().get(0);
			assertEquals(tarCreeper.getAttack(), 3);
			context.endTurn();
			assertEquals(tarCreeper.getAttack(), 1);
		});
	}

	@Test
	public void testGastropodTirionFordringInteraction() {
		// Bug happened due to postEquipWeapon performing a checkForDeadEntities in the middle of
		runGym((context, player, opponent) -> {
			Minion tirionFordring = playMinionCard(context, player, "minion_tirion_fordring");
			context.endTurn();
			Minion stubbornGastropod = playMinionCard(context, opponent, "minion_stubborn_gastropod");
			context.endTurn();
			tirionFordring.setAttribute(Attribute.POISONOUS);
			stubbornGastropod.setAttack(10);
			tirionFordring.setAttack(10);
			// Clear Tirion's divine shield and equip a weapon with deathrattle
			playCard(context, player, "spell_fireball", tirionFordring);
			assertFalse(tirionFordring.hasAttribute(Attribute.DIVINE_SHIELD));
			playCard(context, player, "weapon_tentacles_for_arms");
			attack(context, player, tirionFordring, stubbornGastropod);
		});

		runGym((context, player, opponent) -> {
			Minion stubbornGastropod = playMinionCard(context, player, "minion_stubborn_gastropod");
			context.endTurn();
			Minion tirionFordring = playMinionCard(context, opponent, "minion_tirion_fordring");
			tirionFordring.setAttribute(Attribute.POISONOUS);
			stubbornGastropod.setAttack(10);
			tirionFordring.setAttack(10);
			playCard(context, opponent, "weapon_tentacles_for_arms");
			context.endTurn();
			// Clear Tirion's divine shield and equip a weapon with deathrattle
			playCard(context, player, "spell_fireball", tirionFordring);
			assertFalse(tirionFordring.hasAttribute(Attribute.DIVINE_SHIELD));
			attack(context, player, stubbornGastropod, tirionFordring);
		});
	}

	@Test
	public void testTwoPoisonousMinionsKillingEachOther() {
		runGym((context, player, opponent) -> {
			Minion stubbornGastropod1 = playMinionCard(context, player, "minion_stubborn_gastropod");
			context.endTurn();
			Minion stubbornGastropod2 = playMinionCard(context, opponent, "minion_stubborn_gastropod");
			context.endTurn();
			attack(context, player, stubbornGastropod1, stubbornGastropod2);
			assertEquals(player.getMinions().size() + opponent.getMinions().size(), 0);
		});

		runGym((context, player, opponent) -> {
			Minion stubbornGastropod1 = playMinionCard(context, player, "minion_stubborn_gastropod");
			context.endTurn();
			Minion stubbornGastropod2 = playMinionCard(context, opponent, "minion_stubborn_gastropod");
			context.endTurn();
			stubbornGastropod1.setAttack(3);
			stubbornGastropod2.setAttack(3);
			attack(context, player, stubbornGastropod1, stubbornGastropod2);
			assertEquals(player.getMinions().size() + opponent.getMinions().size(), 0);
		});
	}

	@Test
	public void testStubbornGatropodFlesheatingGhoulInteraction() {
		// Tests that a mysterious exception due to a minion already being dead doesn't occur
		runGym((context, player, opponent) -> {
			Minion flesheatingGhoul = playMinionCard(context, player, "minion_flesheating_ghoul");
			context.endTurn();
			Minion stubbornGastropod = playMinionCard(context, opponent, "minion_stubborn_gastropod");
			context.endTurn();
			attack(context, player, flesheatingGhoul, stubbornGastropod);
			assertEquals(player.getMinions().size(), 0);
			assertEquals(opponent.getMinions().size(), 0);
		});

		runGym((context, player, opponent) -> {
			Minion flesheatingGhoul = playMinionCard(context, player, "minion_flesheating_ghoul");
			flesheatingGhoul.setHp(1);
			context.endTurn();
			Minion stubbornGastropod = playMinionCard(context, opponent, "minion_stubborn_gastropod");
			context.endTurn();
			attack(context, player, flesheatingGhoul, stubbornGastropod);
			assertEquals(player.getMinions().size(), 0);
			assertEquals(opponent.getMinions().size(), 0);
		});

		runGym((context, player, opponent) -> {
			Minion stubbornGastropod = playMinionCard(context, player, "minion_stubborn_gastropod");
			context.endTurn();
			Minion flesheatingGhoul = playMinionCard(context, opponent, "minion_flesheating_ghoul");
			context.endTurn();
			attack(context, player, stubbornGastropod, flesheatingGhoul);
			assertEquals(player.getMinions().size(), 0);
			assertEquals(opponent.getMinions().size(), 0);
		});

		runGym((context, player, opponent) -> {
			Minion stubbornGastropod = playMinionCard(context, player, "minion_stubborn_gastropod");
			context.endTurn();
			Minion flesheatingGhoul = playMinionCard(context, opponent, "minion_flesheating_ghoul");
			flesheatingGhoul.setHp(1);
			context.endTurn();
			attack(context, player, stubbornGastropod, flesheatingGhoul);
			assertEquals(player.getMinions().size(), 0);
			assertEquals(opponent.getMinions().size(), 0);
		});
	}

	@Test
	public void testCuriousGlimmerroot() {
		// Right choice
		runGym((context, player, opponent) -> {
			context.getEntities().forEach(e -> e.getAttributes().remove(Attribute.STARTED_IN_DECK));
			String rightCardId = "minion_shade_of_naxxramas";
			putOnTopOfDeck(context, opponent, rightCardId);
			putOnTopOfDeck(context, opponent, "spell_lunstone");
			opponent.getDeck().get(0).getAttributes().put(Attribute.STARTED_IN_DECK, true);
			opponent.getDeck().get(1).getAttributes().put(Attribute.STARTED_IN_DECK, false);
			OverrideDiscoverBehaviour override = overrideDiscoverChoice((List<DiscoverAction> choices) ->
					choices.stream()
							.filter(da -> da.getCard().getCardId().equals(rightCardId))
							.findFirst()
							.orElseThrow(AssertionError::new));
			context.setBehaviour(player.getId(), override);
			playMinionCard(context, player, "minion_curious_glimmerroot");
			assertEquals(player.getHand().size(), 1);
			assertEquals(player.getHand().get(0).getCardId(), rightCardId);
		});

		// Wrong choice
		runGym((context, player, opponent) -> {
			context.getEntities().forEach(e -> e.getAttributes().remove(Attribute.STARTED_IN_DECK));
			String rightCardId = "minion_shade_of_naxxramas";
			putOnTopOfDeck(context, opponent, rightCardId);
			putOnTopOfDeck(context, opponent, "spell_lunstone");
			opponent.getDeck().get(0).getAttributes().put(Attribute.STARTED_IN_DECK, true);
			opponent.getDeck().get(1).getAttributes().put(Attribute.STARTED_IN_DECK, false);
			OverrideDiscoverBehaviour override = overrideDiscoverChoice((List<DiscoverAction> choices) ->
					choices.stream()
							.filter(da -> !da.getCard().getCardId().equals(rightCardId))
							.findFirst()
							.orElseThrow(AssertionError::new));
			context.setBehaviour(player.getId(), override);
			playMinionCard(context, player, "minion_curious_glimmerroot");
			assertEquals(player.getHand().size(), 0);
		});
	}

	private OverrideDiscoverBehaviour overrideDiscoverChoice(Function<List<DiscoverAction>, GameAction> chooser) {
		return new OverrideDiscoverBehaviour(chooser);
	}

	@Test
	public void testClutchmotherZavas() {
		runGym((context, player, opponent) -> {
			Card clutchmotherBase = receiveCard(context, player, "minion_clutchmother_zavas");
			playMinionCard(context, player, "minion_succubus");
			playMinionCard(context, player, "minion_succubus");

			Minion clutchmother = playMinionCard(context, player, clutchmotherBase);
			assertEquals(clutchmother.getAttack(), 6);
			assertEquals(clutchmother.getHp(), 6);
		});
	}

	@Test
	public void testPermanents() {
		GymFactory factory = getGymFactory((context, player, opponent) -> {
			Minion flower = playMinionCard(context, player, "minion_sherazin_corpse_flower");
			context.endTurn();
			playCard(context, opponent, "spell_assassinate", flower);
			// Permanents can be affected by their own effects. For example, Sherazin, Seed is immune to all
			// outside effects, but can transform itself into Sherazin, Corpse Flower.
			assertEquals(player.getMinions().size(), 1);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "permanent_sherazin_seed");
			// Currently on the opponent's turn
			opponent.setMaxMana(10);
			opponent.setMana(10);
		});


		// Let's start the gym

		// Permanents cannot be targeted, either by attacks or effects, including random target effects, such as Mad
		// Bomber or Mind Control Tech
		factory.run((c, p, o) -> {
			Minion corpse = p.getMinions().get(0);
			// TargetSelection.ANY
			Stream.of("spell_fireball", // TargetSelection.ANY
					"spell_inner_rage", // TargetSelection.MINIONS
					"spell_swipe", // TargetSelection.ENEMY_CHARACTERS from the opponent's point of view
					"spell_mind_control" // TargetSelection.ENEMY_MINIONS  from the opponent's point of view
			).forEach(cId -> {
				receiveCard(c, o, cId);
			});

			assertFalse(c.getValidActions().stream().anyMatch(ga -> ga.getTargetReference() != null
					&& ga.getTargetReference().equals(corpse.getReference())));

			// Play Aracane Missiles 8x and confirm that corpse flower is never hit.
			// The only actors on the board right now should be the heroes and the corpse flower, so the total damage
			// 8 * 3 = 24 should have only hit the player's face

			int startingHp = p.getHero().getHp();
			for (int i = 0; i < 8; i++) {
				playCard(c, o, "spell_arcane_missiles");
			}
			assertEquals(p.getHero().getHp(), startingHp - 8 * 3);
		});

		// Permanents do not count as eligible targets for triggered effects such as Blood Imp. If a triggered effect
		// requires a minion target, it will not activate due to the presence of a permanent alone.[3] If a Deathrattle
		// such as Zealous Initiate that requires a target is activated with only a permanent on the board, it will have
		// no effect.
		factory.run((c, p, o) -> {
			Minion corpse = p.getMinions().get(0);
			c.endTurn();
			playMinionCard(c, p, "minion_blood_imp");
			c.endTurn();
			assertEquals(corpse.getAttributeValue(Attribute.HP_BONUS), 0);
			c.endTurn();
			playMinionCard(c, p, "minion_zealous_initiate");
			c.endTurn();
			assertEquals(corpse.getAttributeValue(Attribute.HP_BONUS), 0);
		});

		// Does turning into the seed count as a summoning effect? It shouldn't, because it would make no sense for
		// Swamp King Dread to attack him
		runGym((c, p, o) -> {
			Minion flower = playMinionCard(c, p, "minion_sherazin_corpse_flower");
			Minion darkshireCouncilman = playMinionCard(c, p, "minion_darkshire_councilman");
			c.endTurn();
			playCard(c, o, "spell_assassinate", flower);
			assertEquals(darkshireCouncilman.getAttributeValue(Attribute.ATTACK_BONUS), 0);
		});

		// Permanents are not susceptible to any kind of effect, including Area of Effect, such as Deathwing, DOOM! or
		// Brawl.
		factory.run((c, p, o) -> {
			Minion corpse = p.getMinions().get(0);
			Minion raptor = playMinionCard(c, o, "minion_bloodfen_raptor");
			playMinionCard(c, o, "minion_deathwing");
			assertEquals(corpse.getZone(), Zones.BATTLEFIELD);
			assertEquals(raptor.getZone(), Zones.GRAVEYARD);
			playCard(c, o, "spell_doom");
			assertEquals(corpse.getZone(), Zones.BATTLEFIELD);
			playCard(c, o, "spell_brawl");
			assertEquals(corpse.getZone(), Zones.BATTLEFIELD);
			playCard(c, o, "spell_flamestrike");
			assertEquals(corpse.getZone(), Zones.BATTLEFIELD);
			c.endTurn();
			Minion raptor2 = playMinionCard(c, p, "minion_bloodfen_raptor");

			// Permanents are not affected by positional effects such as Flametongue Totem or Cone of Cold, but still take
			// up a spot for the purposes of determining adjacent minions, effectively blocking their effects without consequence.

			// Play flametongue to the left of the corpse
			playCard(c, p, "minion_flametongue_totem", corpse);
			assertEquals(raptor2.getAttributeValue(Attribute.AURA_ATTACK_BONUS), 0, "Flametongue is to the left of the corpse, so there should be no buff.");
			assertEquals(corpse.getAttributeValue(Attribute.AURA_ATTACK_BONUS), 0, "Flametongue is to the left of the corpse, so there should be no buff.");
			playCard(c, p, "minion_flametongue_totem", raptor2);
			assertEquals(raptor2.getAttributeValue(Attribute.AURA_ATTACK_BONUS), 2, "Flametongue is to the left of the Raptor, so there a buff.");
			assertEquals(corpse.getAttributeValue(Attribute.AURA_ATTACK_BONUS), 0, "Flametongue shouldn't buff a corpse.");
		});

		// Because they cannot be affected by outside effects, permanents as a rule cannot be destroyed, damaged or
		// transformed.
		factory.run((c, p, o) -> {
			Minion corpse = p.getMinions().get(0);
			c.endTurn();
			playCard(c, p, "hero_thrall_deathseer");
			assertEquals(corpse.getSourceCard().getCardId(), "permanent_sherazin_seed");
		});

		// Permanents take up a place on the battlefield like regular minions, and count toward the 7 minion limit.
		factory.run((c, p, o) -> {
			c.endTurn();
			for (int i = 0; i < 6; i++) {
				playCard(c, p, "minion_bloodfen_raptor");
				c.endTurn();
				c.endTurn();
			}
			assertFalse(c.getLogic().canSummonMoreMinions(p));
		});

		// Reliquary Seeker's Battlecry activates with 5 other minions and a permanent on the
		// battlefield, despite requiring "6 other minions."
		factory.run((c, p, o) -> {
			c.endTurn();
			for (int i = 0; i < 5; i++) {
				playCard(c, p, "minion_bloodfen_raptor");
				// Don't accidentally trigger Corpse Flower!
				c.endTurn();
				c.endTurn();
			}
			assertTrue(c.getLogic().canSummonMoreMinions(p));
			Minion seeker = playMinionCard(c, p, "minion_reliquary_seeker");
			assertEquals(seeker.getHp(), 5);
		});

		// Effects that simply scale per minion do not count permanents.
		// Example: Frostwolf Warlord is played with four minions and a permanent in play. Its Battlecry gives it +4/+4
		// for the minions, but nothing for the permanent.
		factory.run((c, p, o) -> {
			c.endTurn();
			for (int i = 0; i < 5; i++) {
				// Don't accidentally trigger Corpse Flower!
				playCard(c, p, "minion_bloodfen_raptor");
				c.endTurn();
				c.endTurn();
			}
			assertTrue(c.getLogic().canSummonMoreMinions(p));
			Minion frostwolfWarlord = playMinionCard(c, p, "minion_frostwolf_warlord");
			assertEquals(frostwolfWarlord.getHp(), 9);
		});

		// N'Zoth should resurrect Corpse Flower (we just need to check that it's in the graveyard)
		factory.run((c, p, o) -> {
			c.endTurn();
			assertTrue(p.getGraveyard().stream().anyMatch(e ->
					e.getEntityType() == EntityType.MINION
							&& e.getSourceCard().getCardId().equals("minion_sherazin_corpse_flower")));
		});
	}

	@Test
	public void testGalvadon() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "quest_the_last_kaleidosaur");

			Minion target = playMinionCard(context, player, "minion_bloodfen_raptor");

			for (int i = 0; i < 5; i++) {
				playCard(context, player, "spell_adaptation", target);
			}

			// Only spells that target a specific friendly minion will count towards the quest, meaning that randomly
			// targeted and AoE spells such as Smuggler's Run, Competitive Spirit and Avenge will not count.

			playCard(context, player, "spell_savage_roar");
			assertFalse(player.getHand().containsCard("token_galvadon"));

			context.endTurn();
			Minion opponentTarget = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			playCard(context, opponent, "spell_adaptation", opponentTarget);
			assertFalse(player.getHand().containsCard("token_galvadon"));
			context.endTurn();
			playCard(context, player, "spell_adaptation", opponentTarget);
			assertFalse(player.getHand().containsCard("token_galvadon"));
			playCard(context, player, "spell_adaptation", target);
			assertTrue(player.getHand().containsCard("token_galvadon"));
		});
	}

	@Test()
	public void testTimeWarp() {
		runGym(((context, player, opponent) -> {
			playCard(context, player, "quest_open_the_waygate");
			// TODO: Test stolen cards from the opponent's deck.

			// Didn't start in the deck.
			for (int i = 0; i < 6; i++) {
				playCard(context, player, "spell_arcane_explosion");
			}

			assertTrue(player.getHand().containsCard("spell_time_warp"));
			// Multiple Time Warps stack - you take that many extra turns in a row.

			playCard(context, player, "spell_time_warp");
			playCard(context, player, "spell_time_warp");
			context.endTurn();
			assertEquals(context.getActivePlayer(), player);
			context.endTurn();
			assertEquals(context.getActivePlayer(), player);
			context.endTurn();
			assertEquals(context.getActivePlayer(), opponent);
		}));

	}

	@Test()
	public void testPrimalfinChampion() {
		runGym((context, player, opponent) -> {
			Minion primalfinChampion = playMinionCard(context, player, "minion_primalfin_champion");
			Minion bloodfenRaptor = playMinionCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "spell_adaptation", primalfinChampion);
			playCard(context, player, "spell_adaptation", primalfinChampion);
			playCard(context, player, "spell_bananas", bloodfenRaptor);
			context.endTurn();
			Minion bloodfenRaptor2 = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			playCard(context, opponent, "spell_bananas", bloodfenRaptor2);
			playCard(context, opponent, "spell_assassinate", primalfinChampion);
			assertEquals(player.getHand().size(), 2);
			assertTrue(player.getHand().containsCard("spell_adaptation"));
			assertFalse(player.getHand().containsCard("spell_bananas"));
		});

	}

	@Test
	public void testTheVoraxx() {
		runGym((context, player, opponent) -> {
			Minion voraxx = playMinionCard(context, player, "minion_the_voraxx");
			playCard(context, player, "spell_bananas", voraxx);
			assertEquals(player.getMinions().size(), 2);
			assertEquals(voraxx.getAttack(), 4, "The Voraxx should have been buffed by 1. ");
			assertEquals(player.getMinions().get(1).getAttack(), 2, "The plant should be buffed");
		});

		runGym((context, player, opponent) -> {
			Minion voraxx = playMinionCard(context, player, "minion_the_voraxx");
			DiscoverAction[] discoverAction = new DiscoverAction[1];
			AtomicInteger count = new AtomicInteger(0);
			OverrideDiscoverBehaviour behaviour = overrideDiscoverChoice(discoverActions -> {
				discoverAction[0] = discoverActions.get(0);
				count.incrementAndGet();
				return discoverActions.get(0);
			});
			context.setBehaviour(player.getId(), behaviour);
			playCard(context, player, "spell_adaptation", voraxx);
			Card card = discoverAction[0].getCard();
			assertEquals(player.getMinions().size(), 2);
			assertEquals(count.get(), 1, "Should only prompt for an Adaptation once");
			String name = card.getName();
			Minion plant = player.getMinions().get(1);
			Stream.of(voraxx, plant).forEach(minion -> {
				assertAdapted(name, minion);
			});
		});
	}

	@Test
	public void testSteamSurger() {
		runGym((context, player, opponent) -> {
			player.setMaxMana(10);
			player.setMana(10);
			playCard(context, player, "minion_pyros");
			playCard(context, player, "minion_steam_surger");
			assertFalse(player.getHand().containsCard("spell_flame_geyser"));
			context.endTurn();
			context.endTurn();
			playCard(context, player, "minion_steam_surger");
			assertTrue(player.getHand().containsCard("spell_flame_geyser"));
			context.endTurn();
			context.endTurn();
			context.endTurn();
			context.endTurn();
			clearHand(context, player);
			playCard(context, player, "minion_steam_surger");
			assertFalse(player.getHand().containsCard("spell_flame_geyser"));
		});
	}

	@Test
	public void testJungleGiants() {
		runGym(((context, player, opponent) -> {
			playCard(context, player, "quest_jungle_giants");
			assertEquals(player.getQuests().size(), 1);
			player.setMaxMana(10);
			player.setMana(10);
			receiveCard(context, player, "quest_jungle_giants");
			assertFalse(context.getLogic().canPlayCard(player.getId(), player.getHand().get(0).getReference()),
					"Since we already have a quest in play, we should not be able to play another quest.");

			// Play 5 minions with 5 or more attack.
			for (int i = 0; i < 5; i++) {
				assertFalse(player.getHand().containsCard("token_barnabus_the_stomper"));
				playMinionCard(context, player, "minion_leeroy_jenkins");
			}
			assertTrue(player.getHand().containsCard("token_barnabus_the_stomper"));
			assertEquals(player.getQuests().size(), 0);
			player.setMana(1);
			assertTrue(context.getLogic().canPlayCard(player.getId(), player.getHand().get(0).getReference()));
		}));
	}

	@Test
	public void testLivingMana() {
		// Check correct summon count
		zip(Stream.of(5, 6, 7, 8, 9, 10), Stream.of(5, 6, 7, 7, 7, 7), (mana, maxMinionsSummoned) -> {
			for (int i = 0; i <= 7; i++) {
				int finalI = i;
				runGym(((context, player, opponent) -> {
					for (int j = 0; j < finalI; j++) {
						playMinionCard(context, player, "minion_wisp");
					}
					player.setMaxMana(mana);
					player.setMana(mana);
					playCard(context, player, "spell_living_mana");
					int minionsOnBoard = Math.min(maxMinionsSummoned + finalI, 7);
					int minionsSummonedByLivingMana = Math.min(7, minionsOnBoard - finalI);
					assertEquals(player.getMinions().size(), minionsOnBoard);
					assertEquals(player.getMaxMana(), mana - minionsSummonedByLivingMana,
							String.format("Prior max mana: %d, prior minions on  board: %d", mana, finalI));
				}));

			}
			return null;
		}).collect(toList());
	}

	@Test
	public void testMoltenBladeAndShifterZerus() {
		for (String cardId : new String[]{"weapon_molten_blade", "minion_shifter_zerus"}) {
			runGym((context, player, opponent) -> {
				player.setMana(10);
				player.setMaxMana(10);
				receiveCard(context, player, cardId);
				int oldId = player.getHand().get(0).getId();
				assertEquals(player.getHand().get(0).getCardId(), cardId, String.format("%s should not have transformed yet: ", cardId));
				context.endTurn();
				context.endTurn();
				int oldId1 = player.getHand().get(0).getId();
				assertNotEquals(oldId1, oldId);
				context.endTurn();
				context.endTurn();
				int oldId2 = player.getHand().get(0).getId();
				assertNotEquals(oldId2, oldId1);
				Card card = player.getHand().get(0);
				if (card.isChooseOne()) {
					context.performAction(player.getId(), card.playOptions()[0]);
				} else {
					context.performAction(player.getId(), card.play());
				}
				// I suppose there might be a situation where a card gets shuffled into a deck and things glitch out,
				// or the battlecry puts a card into the hand. So we're just testing to see there was no exception for
				// now.
			});
		}

	}

	@Test
	public void testEarthenScales() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "token_sapling");
			Minion sapling = player.getMinions().get(0);
			assertEquals(sapling.getAttack(), 1);
			playCard(context, player, CardCatalogue.getCardById("spell_earthen_scales"), sapling);
			assertEquals(player.getHero().getArmor(), 2);
		});
	}

	@Test
	public void testBarnabusTheStomper() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "token_sapling");
			playCard(context, player, "token_barnabus_the_stomper");
			context.getLogic().drawCard(player.getId(), null);
			Card sapling = player.getHand().get(0);
			assertEquals(sapling.getCardId(), "token_sapling");
			assertEquals(costOf(context, player, sapling), 0);
		});
	}

	@Test
	public void testBarnabusTheStomperTolinsGobletInteraction() {
		// Tolin's Goblet interaction
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "token_sapling");
			playCard(context, player, "token_barnabus_the_stomper");
			playCard(context, player, "spell_tolins_goblet");
			assertTrue(player.getHand().stream().allMatch(card -> costOf(context, player, card) == 0));
		});
	}

	@Test
	public void testManaBind() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_mana_bind");
			context.endTurn();
			playCard(context, opponent, "spell_fireball", player.getHero());
			Card copiedFireball = player.getHand().get(0);
			assertEquals(copiedFireball.getCardId(), "spell_fireball");
			Card graveyardFireball = (Card) opponent.getGraveyard().get(opponent.getGraveyard().size() - 1);
			assertEquals(graveyardFireball.getCardId(), "spell_fireball");
			assertNotEquals(copiedFireball.getId(), graveyardFireball.getId());
			assertEquals(costOf(context, player, copiedFireball), 0);
		});
	}

	@Test
	public void testFreeFromAmber() {
		GameContext context = createContext("WHITE", "WHITE");
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
					action[0] = (DiscoverAction) validActions.get(0);
					Card original = action[0].getCard();
					originalMinion[0] = original.summon();
					handSize[0] = player.getHand().size();
					return action[0];
				}
				first = false;
				return super.requestAction(context, player, validActions);
			}
		});
		Card freeFromAmber = CardCatalogue.getCardById("spell_free_from_amber");
		playCard(context, player, freeFromAmber);
		assertEquals(player.getHand().size(), handSize[0]);
		assertEquals(player.getDiscoverZone().size(), 0);
		// TODO: Should the player really receive the card and then summon it?
		assertEquals(player.getGraveyard().size(), 1, "The graveyard should only contain Free From Amber");
		assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), originalMinion[0].getSourceCard().getCardId());
	}
}
