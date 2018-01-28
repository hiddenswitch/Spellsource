package com.blizzard.hearthstone;


import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.DiscoverAction;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.cards.SpellCard;
import net.demilich.metastone.game.cards.desc.MinionCardDesc;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.tests.util.OverrideDiscoverBehaviour;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.Assert;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class JourneyToUngoroTests extends TestBase {

	@Test
	public void testNestingRoc() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_bloodfen_raptor");
			Minion nestingRoc = playMinionCard(context, player, "minion_nesting_roc");
			Assert.assertTrue(nestingRoc.hasAttribute(Attribute.TAUNT));
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_bloodfen_raptor");
			Minion nestingRoc = playMinionCard(context, player, "minion_nesting_roc");
			Assert.assertFalse(nestingRoc.hasAttribute(Attribute.TAUNT));
		});
	}

	@Test
	public void testMeteor() {
		for (int j0 = 0; j0 < 3; j0++) {
			final int j = j0;
			runGym((context, player, opponent) -> {
				context.endTurn();
				List<Minion> minions = IntStream.range(0, 3).mapToObj(i -> playMinionCard(context, opponent, "minion_argent_squire")).collect(Collectors.toList());
				context.endTurn();
				playCardWithTarget(context, player, "spell_meteor", minions.get(j));
				for (int k = 0; k < 3; k++) {
					Assert.assertFalse(opponent.getMinions().get(k).isDestroyed());
					Assert.assertEquals(opponent.getMinions().get(k).hasAttribute(Attribute.DIVINE_SHIELD), k < j - 1 || k > j + 1);
				}
			});
		}
	}

	@Test
	public void testCrystalCore() {
		Consumer<Minion> checkMinion = (Minion minion) -> {
			Assert.assertEquals(minion.getAttack(), 5);
			Assert.assertEquals(minion.getHp(), 5);
		};

		// Check regular summoning from hand and mind control
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_crystal_core");
			Minion minion1 = playMinionCard(context, player, "minion_bloodfen_raptor");
			checkMinion.accept(minion1);
			context.endTurn();
			Minion minion2 = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			playCardWithTarget(context, player, "spell_mind_control", minion2);
			checkMinion.accept(minion2);
		});

		// Check resurrection
		runGym((context, player, opponent) -> {
			Minion minion1 = playMinionCard(context, player, "minion_bloodfen_raptor");
			context.endTurn();
			playCardWithTarget(context, opponent, "spell_fireball", minion1);
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
			playCardWithTarget(context, opponent, "spell_silence", minion1);
			context.endTurn();
			checkMinion.accept(minion1);
		});
	}

	@Test
	public void testCrystalCoreExistingMinionsInteraction() {
		Consumer<Minion> checkMinion = (Minion minion) -> {
			Assert.assertEquals(minion.getAttack(), 5);
			Assert.assertEquals(minion.getHp(), 5);
		};

		// Check that existing minions on the board get buffed
		runGym((context, player, opponent) -> {
			Minion minion1 = playMinionCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "spell_crystal_core");
			checkMinion.accept(minion1);
		});
	}

	@Test
	public void testTheCavernsBelow() {
		// Plain test
		runGym((context, player, opponent) -> {
			playCard(context, player, "quest_the_caverns_below");
			Stream.of("minion_bloodfen_raptor", "minion_bloodfen_raptor", "minion_bloodfen_raptor", "minion_bloodfen_raptor",
					"spell_mirror_image", "spell_mirror_image", "spell_mirror_image", "spell_mirror_image")
					.peek(cid -> playCard(context, player, cid)).peek(ignored -> Assert.assertEquals(player.getHand().size(), 0))
					.collect(Collectors.toList());

			playCard(context, player, "spell_mirror_image");
			// Mirror image should not count
			Assert.assertEquals(player.getHand().size(), 0);

			playCard(context, player, "spell_twisting_nether");
			playCard(context, player, "minion_bloodfen_raptor");
			Assert.assertEquals(player.getHand().get(0).getCardId(), "spell_crystal_core");
		});

		// Tokens summoned by other cards shouldn't count
		runGym((context, player, opponent) -> {
			playCard(context, player, "quest_the_caverns_below");
			playCard(context, player, "minion_moroes");
			for (int i = 0; i < 5; i++) {
				context.endTurn();
				context.endTurn();
			}
			Assert.assertEquals(player.getMinions().size(), 6);
			Assert.assertEquals(player.getMinions().stream()
					.map(Minion::getSourceCard)
					.map(Card::getCardId)
					.filter(cid -> cid.equals("token_steward")).count(), 5L);

			Assert.assertEquals(player.getHand().size(), 0);
		});

		// Cards of the same name but different source card should count
		runGym((context, player, opponent) -> {
			playCard(context, player, "quest_the_caverns_below");
			for (int i = 0; i < 3; i++) {
				playCard(context, player, "token_treant_taunt");
				playCard(context, player, "token_treant");
			}
			Assert.assertEquals(player.getHand().get(0).getCardId(), "spell_crystal_core");
		});
	}

	@Test
	public void testTarCreeper() {
		runGym((context, player, opponent) -> {
			Minion tarCreeper = playMinionCard(context, player, "minion_tar_creeper");
			Assert.assertEquals(tarCreeper.getAttack(), 1);
			context.endTurn();
			Assert.assertEquals(tarCreeper.getAttack(), 3);
			context.endTurn();
			Assert.assertEquals(tarCreeper.getAttack(), 1);
		});

		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_tar_creeper");
			context.endTurn();
			playCard(context, opponent, "minion_dirty_rat");
			Minion tarCreeper = player.getMinions().get(0);
			Assert.assertEquals(tarCreeper.getAttack(), 3);
			context.endTurn();
			Assert.assertEquals(tarCreeper.getAttack(), 1);
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
			playCardWithTarget(context, player, "spell_fireball", tirionFordring);
			Assert.assertFalse(tirionFordring.hasAttribute(Attribute.DIVINE_SHIELD));
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
			playCardWithTarget(context, player, "spell_fireball", tirionFordring);
			Assert.assertFalse(tirionFordring.hasAttribute(Attribute.DIVINE_SHIELD));
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
			Assert.assertEquals(player.getMinions().size() + opponent.getMinions().size(), 0);
		});

		runGym((context, player, opponent) -> {
			Minion stubbornGastropod1 = playMinionCard(context, player, "minion_stubborn_gastropod");
			context.endTurn();
			Minion stubbornGastropod2 = playMinionCard(context, opponent, "minion_stubborn_gastropod");
			context.endTurn();
			stubbornGastropod1.setAttack(3);
			stubbornGastropod2.setAttack(3);
			attack(context, player, stubbornGastropod1, stubbornGastropod2);
			Assert.assertEquals(player.getMinions().size() + opponent.getMinions().size(), 0);
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
			Assert.assertEquals(player.getMinions().size(), 0);
			Assert.assertEquals(opponent.getMinions().size(), 0);
		});

		runGym((context, player, opponent) -> {
			Minion flesheatingGhoul = playMinionCard(context, player, "minion_flesheating_ghoul");
			flesheatingGhoul.setHp(1);
			context.endTurn();
			Minion stubbornGastropod = playMinionCard(context, opponent, "minion_stubborn_gastropod");
			context.endTurn();
			attack(context, player, flesheatingGhoul, stubbornGastropod);
			Assert.assertEquals(player.getMinions().size(), 0);
			Assert.assertEquals(opponent.getMinions().size(), 0);
		});

		runGym((context, player, opponent) -> {
			Minion stubbornGastropod = playMinionCard(context, player, "minion_stubborn_gastropod");
			context.endTurn();
			Minion flesheatingGhoul = playMinionCard(context, opponent, "minion_flesheating_ghoul");
			context.endTurn();
			attack(context, player, stubbornGastropod, flesheatingGhoul);
			Assert.assertEquals(player.getMinions().size(), 0);
			Assert.assertEquals(opponent.getMinions().size(), 0);
		});

		runGym((context, player, opponent) -> {
			Minion stubbornGastropod = playMinionCard(context, player, "minion_stubborn_gastropod");
			context.endTurn();
			Minion flesheatingGhoul = playMinionCard(context, opponent, "minion_flesheating_ghoul");
			flesheatingGhoul.setHp(1);
			context.endTurn();
			attack(context, player, stubbornGastropod, flesheatingGhoul);
			Assert.assertEquals(player.getMinions().size(), 0);
			Assert.assertEquals(opponent.getMinions().size(), 0);
		});
	}

	@Test
	public void testCuriousGlimmerroot() {
		// Right choice
		runGym((context, player, opponent) -> {
			context.getEntities().forEach(e -> e.getAttributes().remove(Attribute.STARTED_IN_DECK));
			String rightCardId = "minion_shade_of_naxxramas";
			shuffleToDeck(context, opponent, rightCardId);
			opponent.getDeck().get(0).getAttributes().put(Attribute.STARTED_IN_DECK, true);
			OverrideDiscoverBehaviour override = overrideDiscoverChoice((List<DiscoverAction> choices) ->
					choices.stream()
							.filter(da -> da.getCard().getCardId().equals(rightCardId))
							.findFirst()
							.orElseThrow(AssertionError::new));
			player.setBehaviour(override);
			playMinionCard(context, player, "minion_curious_glimmerroot");
			Assert.assertEquals(player.getHand().size(), 1);
			Assert.assertEquals(player.getHand().get(0).getCardId(), rightCardId);
		});

		// Wrong choice
		runGym((context, player, opponent) -> {
			context.getEntities().forEach(e -> e.getAttributes().remove(Attribute.STARTED_IN_DECK));
			String rightCardId = "minion_shade_of_naxxramas";
			shuffleToDeck(context, opponent, rightCardId);
			opponent.getDeck().get(0).getAttributes().put(Attribute.STARTED_IN_DECK, true);
			OverrideDiscoverBehaviour override = overrideDiscoverChoice((List<DiscoverAction> choices) ->
					choices.stream()
							.filter(da -> !da.getCard().getCardId().equals(rightCardId))
							.findFirst()
							.orElseThrow(AssertionError::new));
			player.setBehaviour(override);
			playMinionCard(context, player, "minion_curious_glimmerroot");
			Assert.assertEquals(player.getHand().size(), 0);
		});
	}

	private OverrideDiscoverBehaviour overrideDiscoverChoice(Function<List<DiscoverAction>, GameAction> chooser) {
		return new OverrideDiscoverBehaviour(chooser);
	}

	@Test
	public void testClutchmotherZavas() {
		runGym((c, p, o) -> {
			receiveCard(c, p, "minion_clutchmother_zavas");
			playMinionCard(c, p, "minion_succubus");
			c.getLogic().performGameAction(p.getId(), p.getHand().get(0).play());

			Minion clutchmother = getSummonedMinion(p.getMinions());
			Assert.assertEquals(clutchmother.getAttack(), 4);
			Assert.assertEquals(clutchmother.getHp(), 4);
		});
	}

	@Test
	public void testPermanents() {
		GymFactory factory = getGymFactory((context, player, opponent) -> {
			Minion flower = playMinionCard(context, player, "minion_sherazin_corpse_flower");
			context.endTurn();
			playCardWithTarget(context, opponent, "spell_assassinate", flower);
			// Permanents can be affected by their own effects. For example, Sherazin, Seed is immune to all
			// outside effects, but can transform itself into Sherazin, Corpse Flower.
			Assert.assertEquals(player.getMinions().size(), 1);
			Assert.assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "permanent_sherazin_seed");
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

			Assert.assertFalse(c.getValidActions().stream().anyMatch(ga -> ga.getTargetReference() != null
					&& ga.getTargetReference().equals(corpse.getReference())));

			// Play Aracane Missiles 8x and confirm that corpse flower is never hit.
			// The only actors on the board right now should be the heroes and the corpse flower, so the total damage
			// 8 * 3 = 24 should have only hit the player's face

			int startingHp = p.getHero().getHp();
			for (int i = 0; i < 8; i++) {
				playCard(c, o, "spell_arcane_missiles");
			}
			Assert.assertEquals(p.getHero().getHp(), startingHp - 8 * 3);

			// Test mind control tech
			c.endTurn();
			Minion raptor = playMinionCard(c, p, "minion_bloodfen_raptor");
			c.endTurn();
			MinionCardDesc custom = (MinionCardDesc) CardCatalogue.getRecords().get("minion_mind_control_tech").getDesc();
			custom.battlecry.condition = null;
			MinionCard customControl = new MinionCard(custom);
			playCard(c, o, customControl);
			Assert.assertEquals(o.getMinions().size(), 2, "Raptor + Mind Control Tech");
			Assert.assertFalse(o.getMinions().stream().map(Minion::getSourceCard).anyMatch(c1 -> c1.getCardId().equals("permanent_sherazin_seed")));
			Assert.assertEquals(o.getMinions().get(1), raptor);
			Assert.assertEquals(p.getMinions().size(), 1, "Just Sherazin Seed");
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
			Assert.assertEquals(corpse.getAttributeValue(Attribute.HP_BONUS), 0);
			c.endTurn();
			playMinionCard(c, p, "minion_zealous_initiate");
			c.endTurn();
			Assert.assertEquals(corpse.getAttributeValue(Attribute.HP_BONUS), 0);
		});

		// Does turning into the seed count as a summoning effect? It shouldn't, because it would make no sense for
		// Swamp King Dread to attack him
		runGym((c, p, o) -> {
			Minion flower = playMinionCard(c, p, "minion_sherazin_corpse_flower");
			Minion darkshireCouncilman = playMinionCard(c, p, "minion_darkshire_councilman");
			c.endTurn();
			playCardWithTarget(c, o, "spell_assassinate", flower);
			Assert.assertEquals(darkshireCouncilman.getAttributeValue(Attribute.ATTACK_BONUS), 0);
		});

		// Permanents are not susceptible to any kind of effect, including Area of Effect, such as Deathwing, DOOM! or
		// Brawl.
		factory.run((c, p, o) -> {
			Minion corpse = p.getMinions().get(0);
			Minion raptor = playMinionCard(c, o, "minion_bloodfen_raptor");
			playMinionCard(c, o, "minion_deathwing");
			Assert.assertEquals(corpse.getZone(), Zones.BATTLEFIELD);
			Assert.assertEquals(raptor.getZone(), Zones.GRAVEYARD);
			playCard(c, o, "spell_doom");
			Assert.assertEquals(corpse.getZone(), Zones.BATTLEFIELD);
			playCard(c, o, "spell_brawl");
			Assert.assertEquals(corpse.getZone(), Zones.BATTLEFIELD);
			playCard(c, o, "spell_flamestrike");
			Assert.assertEquals(corpse.getZone(), Zones.BATTLEFIELD);
			c.endTurn();
			Minion raptor2 = playMinionCard(c, p, "minion_bloodfen_raptor");

			// Permanents are not affected by positional effects such as Flametongue Totem or Cone of Cold, but still take
			// up a spot for the purposes of determining adjacent minions, effectively blocking their effects without consequence.

			// Play flametongue to the left of the corpse
			playCardWithTarget(c, p, "minion_flametongue_totem", corpse);
			Assert.assertEquals(raptor2.getAttributeValue(Attribute.AURA_ATTACK_BONUS), 0, "Flametongue is to the left of the corpse, so there should be no buff.");
			Assert.assertEquals(corpse.getAttributeValue(Attribute.AURA_ATTACK_BONUS), 0, "Flametongue is to the left of the corpse, so there should be no buff.");
			playCardWithTarget(c, p, "minion_flametongue_totem", raptor2);
			Assert.assertEquals(raptor2.getAttributeValue(Attribute.AURA_ATTACK_BONUS), 2, "Flametongue is to the left of the Raptor, so there a buff.");
			Assert.assertEquals(corpse.getAttributeValue(Attribute.AURA_ATTACK_BONUS), 0, "Flametongue shouldn't buff a corpse.");
		});

		// Because they cannot be affected by outside effects, permanents as a rule cannot be destroyed, damaged or
		// transformed.
		factory.run((c, p, o) -> {
			Minion corpse = p.getMinions().get(0);
			c.endTurn();
			playCard(c, p, "hero_thrall_deathseer");
			Assert.assertEquals(corpse.getSourceCard().getCardId(), "permanent_sherazin_seed");
		});

		// Permanents take up a place on the battlefield like regular minions, and count toward the 7 minion limit.
		factory.run((c, p, o) -> {
			c.endTurn();
			for (int i = 0; i < 6; i++) {
				playCard(c, p, "minion_bloodfen_raptor");
				c.endTurn();
				c.endTurn();
			}
			Assert.assertFalse(c.getLogic().canSummonMoreMinions(p));
		});

		// Example: Reliquary Seeker's Battlecry activates with 5 other minions and a permanent on the battlefield,
		// despite requiring "6 other minions"
		factory.run((c, p, o) -> {
			c.endTurn();
			for (int i = 0; i < 5; i++) {
				playCard(c, p, "minion_bloodfen_raptor");
				// Don't accidentally trigger Corpse Flower!
				c.endTurn();
				c.endTurn();
			}
			Assert.assertTrue(c.getLogic().canSummonMoreMinions(p));
			Minion seeker = playMinionCard(c, p, "minion_reliquary_seeker");
			Assert.assertEquals(seeker.getHp(), 5);
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
			Assert.assertTrue(c.getLogic().canSummonMoreMinions(p));
			Minion frostwolfWarlord = playMinionCard(c, p, "minion_frostwolf_warlord");
			Assert.assertEquals(frostwolfWarlord.getHp(), 9);
		});

		// N'Zoth should resurrect Corpse Flower (we just need to check that it's in the graveyard)
		factory.run((c, p, o) -> {
			c.endTurn();
			Assert.assertTrue(p.getGraveyard().stream().anyMatch(e ->
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
				playCardWithTarget(context, player, "spell_adaptation", target);
			}

			// Only spells that target a specific friendly minion will count towards the quest, meaning that randomly
			// targeted and AoE spells such as Smuggler's Run, Competitive Spirit and Avenge will not count.

			playCard(context, player, "spell_savage_roar");
			Assert.assertFalse(player.getHand().containsCard("token_galvadon"));

			context.endTurn();
			Minion opponentTarget = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			playCardWithTarget(context, opponent, "spell_adaptation", opponentTarget);
			Assert.assertFalse(player.getHand().containsCard("token_galvadon"));
			context.endTurn();
			playCardWithTarget(context, player, "spell_adaptation", opponentTarget);
			Assert.assertFalse(player.getHand().containsCard("token_galvadon"));
			playCardWithTarget(context, player, "spell_adaptation", target);
			Assert.assertTrue(player.getHand().containsCard("token_galvadon"));
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

			Assert.assertTrue(player.getHand().containsCard("spell_time_warp"));
			// Multiple Time Warps stack - you take that many extra turns in a row.

			playCard(context, player, "spell_time_warp");
			playCard(context, player, "spell_time_warp");
			context.endTurn();
			Assert.assertEquals(context.getActivePlayer(), player);
			context.endTurn();
			Assert.assertEquals(context.getActivePlayer(), player);
			context.endTurn();
			Assert.assertEquals(context.getActivePlayer(), opponent);
		}));

	}

	@Test()
	public void testPrimalfinChampion() {
		runGym((context, player, opponent) -> {
			Minion primalfinChampion = playMinionCard(context, player, "minion_primalfin_champion");
			Minion bloodfenRaptor = playMinionCard(context, player, "minion_bloodfen_raptor");
			playCardWithTarget(context, player, "spell_adaptation", primalfinChampion);
			playCardWithTarget(context, player, "spell_adaptation", primalfinChampion);
			playCardWithTarget(context, player, "spell_bananas", bloodfenRaptor);
			context.endTurn();
			Minion bloodfenRaptor2 = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			playCardWithTarget(context, opponent, "spell_bananas", bloodfenRaptor2);
			playCardWithTarget(context, opponent, "spell_assassinate", primalfinChampion);
			Assert.assertEquals(player.getHand().size(), 2);
			Assert.assertTrue(player.getHand().containsCard("spell_adaptation"));
			Assert.assertFalse(player.getHand().containsCard("spell_bananas"));
		});

	}

	@Test
	public void testTheVoraxx() {
		runGym((context, player, opponent) -> {
			Minion voraxx = playMinionCard(context, player, "minion_the_voraxx");
			playCardWithTarget(context, player, "spell_bananas", voraxx);
			Assert.assertEquals(player.getMinions().size(), 2);
			Assert.assertEquals(voraxx.getAttack(), 4, "The Voraxx should have been buffed by 1. ");
			Assert.assertEquals(player.getMinions().get(1).getAttack(), 2, "The plant should be buffed");
		});
	}

	@Test
	public void testSteamSurger() {
		runGym((context, player, opponent) -> {
			player.setMaxMana(10);
			player.setMana(10);
			playCard(context, player, "minion_pyros");
			playCard(context, player, "minion_steam_surger");
			Assert.assertFalse(player.getHand().containsCard("spell_flame_geyser"));
			context.endTurn();
			context.endTurn();
			playCard(context, player, "minion_steam_surger");
			Assert.assertTrue(player.getHand().containsCard("spell_flame_geyser"));
			context.endTurn();
			context.endTurn();
			context.endTurn();
			context.endTurn();
			clearHand(context, player);
			playCard(context, player, "minion_steam_surger");
			Assert.assertFalse(player.getHand().containsCard("spell_flame_geyser"));
		});
	}

	@Test
	public void testJungleGiants() {
		runGym(((context, player, opponent) -> {
			playCard(context, player, "quest_jungle_giants");
			Assert.assertEquals(player.getQuests().size(), 1);
			player.setMaxMana(10);
			player.setMana(10);
			receiveCard(context, player, "quest_jungle_giants");
			Assert.assertFalse(context.getLogic().canPlayCard(player.getId(), player.getHand().get(0).getReference()),
					"Since we already have a quest in play, we should not be able to play another quest.");

			// Play 5 minions with 5 or more attack.
			for (int i = 0; i < 5; i++) {
				Assert.assertFalse(player.getHand().containsCard("token_barnabus_the_stomper"));
				playMinionCard(context, player, "minion_leeroy_jenkins");
			}
			Assert.assertTrue(player.getHand().containsCard("token_barnabus_the_stomper"));
			Assert.assertEquals(player.getQuests().size(), 0);
			player.setMana(1);
			Assert.assertTrue(context.getLogic().canPlayCard(player.getId(), player.getHand().get(0).getReference()));
		}));
	}

	@Test
	public void testLivingMana() {
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
					int minionsOnBoard = Math.min((int) maxMinionsSummoned + finalI, 7);
					int minionsSummonedByLivingMana = Math.min(7, minionsOnBoard - finalI);
					Assert.assertEquals(player.getMinions().size(), minionsOnBoard);
					Assert.assertEquals(player.getMaxMana(), mana - minionsSummonedByLivingMana,
							String.format("Prior max mana: %d, prior minions on  board: %d", mana, finalI));
				}));
			}


			return null;
		}).collect(Collectors.toList());
	}

	@Test
	public void testMoltenBladeAndShifterZerus() {
		for (String cardId : new String[]{"weapon_molten_blade", "minion_shifter_zerus"}) {
			runGym((context, player, opponent) -> {
				player.setMana(10);
				player.setMaxMana(10);
				receiveCard(context, player, cardId);
				int oldId = player.getHand().get(0).getId();
				Assert.assertEquals(player.getHand().get(0).getCardId(), cardId, String.format("%s should not have transformed yet: ", cardId));
				context.endTurn();
				context.endTurn();
				int oldId1 = player.getHand().get(0).getId();
				Assert.assertNotEquals(oldId1, oldId);
				context.endTurn();
				context.endTurn();
				int oldId2 = player.getHand().get(0).getId();
				Assert.assertNotEquals(oldId2, oldId1);
				Card card = player.getHand().get(0);
				context.getLogic().performGameAction(player.getId(), card.play());
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
			Assert.assertEquals(sapling.getAttack(), 1);
			playCardWithTarget(context, player, CardCatalogue.getCardById("spell_earthen_scales"), sapling);
			Assert.assertEquals(player.getHero().getArmor(), 2);
		});
	}

	@Test
	public void testBarnabusTheStomper() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "token_sapling");
			playCard(context, player, "token_barnabus_the_stomper");
			context.getLogic().drawCard(player.getId(), null);
			Card sapling = player.getHand().get(0);
			Assert.assertEquals(sapling.getCardId(), "token_sapling");
			Assert.assertEquals(costOf(context, player, sapling), 0);
		});
	}

	@Test
	public void testBarnabusTheStomperTolinsGobletInteraction() {
		// Tolin's Goblet interaction
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "token_sapling");
			playCard(context, player, "token_barnabus_the_stomper");
			playCard(context, player, "spell_tolins_goblet");
			Assert.assertTrue(player.getHand().stream().allMatch(card -> costOf(context, player, card) == 0));
		});
	}

	@Test
	public void testManaBind() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_mana_bind");
			context.endTurn();
			playCardWithTarget(context, opponent, "spell_fireball", player.getHero());
			Card copiedFireball = player.getHand().get(0);
			Assert.assertEquals(copiedFireball.getCardId(), "spell_fireball");
			SpellCard graveyardFireball = (SpellCard) opponent.getGraveyard().get(opponent.getGraveyard().size() - 1);
			Assert.assertEquals(graveyardFireball.getCardId(), "spell_fireball");
			Assert.assertNotEquals(copiedFireball.getId(), graveyardFireball.getId());
			Assert.assertEquals(costOf(context, player, copiedFireball), 0);
		});
	}

	@Test
	public void testFreeFromAmber() {
		GameContext context = createContext(HeroClass.WHITE, HeroClass.WHITE);
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
					return action[0];
				}
				first = false;
				return super.requestAction(context, player, validActions);
			}
		});
		SpellCard freeFromAmber = (SpellCard) CardCatalogue.getCardById("spell_free_from_amber");
		playCard(context, player, freeFromAmber);
		Assert.assertEquals(player.getHand().size(), handSize[0]);
		Assert.assertEquals(player.getDiscoverZone().size(), 0);
		// TODO: Should the player really receive the card and then summon it?
		Assert.assertEquals(player.getGraveyard().size(), 1, "The graveyard should only contain Free From Amber");
		Assert.assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), originalMinion[0].getSourceCard().getCardId());
	}
}
