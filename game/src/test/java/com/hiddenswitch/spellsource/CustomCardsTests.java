package com.hiddenswitch.spellsource;

import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.cards.WeaponCard;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.events.GameStartEvent;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.logic.GameStatus;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.tests.util.TestBase;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

public class CustomCardsTests extends TestBase {

	@Test
	public void testTheEmeraldDream() {
		runGym((context, player, opponent) -> {
			Minion emeraldDream = playMinionCard(context, player, "permanent_the_emerald_dream");
			int count = 0;
			Minion snowflipper;
			for (int i = 0; i < 5; i++) {
				snowflipper = playMinionCard(context, player, "minion_snowflipper_penguin");
				count++;
				Assert.assertEquals(snowflipper.getAttack(), snowflipper.getBaseAttack() + count);
				Assert.assertEquals(snowflipper.getHp(), snowflipper.getBaseHp() + count);
			}
		});
	}

	@Test
	public void testDiabologist() {
		runGym((context, player, opponent) -> {
			Card card1 = receiveCard(context, player, "minion_bloodfen_raptor");
			Card card2 = receiveCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_doomguard");
			Assert.assertTrue(card1.hasAttribute(Attribute.DISCARDED));
			Assert.assertTrue(card2.hasAttribute(Attribute.DISCARDED));
			CountDownLatch latch = new CountDownLatch(1);
			overrideDiscover(player, discoverActions -> {
				latch.countDown();
				Assert.assertEquals(discoverActions.size(), 1, "Should not show duplicate cards due to discover rules");
				Assert.assertEquals(discoverActions.get(0).getCard().getCardId(), "minion_bloodfen_raptor");
				return discoverActions.get(0);
			});
			playCard(context, player, "minion_diabologist");
			Assert.assertEquals(latch.getCount(), 0);
		});
	}

	@Test
	public void testDreadCaptainBones() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_wicked_knife");
			final Weapon weapon = player.getWeaponZone().get(0);
			Assert.assertEquals(weapon.getDurability(), weapon.getBaseDurability());
			playCard(context, player, "minion_dread_captain_bones");
			Assert.assertEquals(weapon.getDurability(), weapon.getBaseDurability() + 1);
		});
	}

	@Test
	public void testFarseerNobundo() {
		// Test that battlecries from the hand are triggered.
		runGym((context, player, opponent) -> {
			Minion onBoardBefore = playMinionCard(context, player, "token_searing_totem");
			MinionCard startedInDeck = putOnTopOfDeck(context, player, "token_searing_totem");
			MinionCard startedInHand = receiveCard(context, player, "token_searing_totem");
			Minion copyCard = playMinionCard(context, player, "minion_king_mukla");
			playMinionCardWithBattlecry(context, player, "minion_farseer_nobundo", copyCard);
			Assert.assertEquals(onBoardBefore.getAttack(), 1);
			Assert.assertEquals(onBoardBefore.getHp(), 1);
			Assert.assertEquals(opponent.getHand().size(), 2, "The opponent should have two bananas at the moment.");
			playCard(context, player, startedInHand);
			Assert.assertEquals(opponent.getHand().size(), 4, "The opponent should have four bananas.");
			context.endTurn();
			context.endTurn();
			Assert.assertEquals(startedInDeck.getZone(), Zones.HAND);
			playCard(context, player, startedInDeck);
			Assert.assertEquals(opponent.getHand().size(), 6);
		});

		// Test auras and triggers
		runGym((context, player, opponent) -> {
			int stormwinds = 0;
			Minion onBoardBefore = playMinionCard(context, player, "token_searing_totem");
			MinionCard startedInHand = receiveCard(context, player, "token_searing_totem");
			Minion copyCard = playMinionCard(context, player, "minion_stormwind_champion");
			stormwinds++;
			playMinionCardWithBattlecry(context, player, "minion_farseer_nobundo", copyCard);
			stormwinds++;
			Assert.assertEquals(onBoardBefore.getAttack(), onBoardBefore.getBaseAttack() + stormwinds - 1);
			Assert.assertEquals(onBoardBefore.getHp(), onBoardBefore.getBaseHp() + stormwinds - 1);
			playCard(context, player, startedInHand);
			stormwinds++;
			Assert.assertEquals(onBoardBefore.getAttack(), onBoardBefore.getBaseAttack() + stormwinds - 1);
			Assert.assertEquals(onBoardBefore.getHp(), onBoardBefore.getBaseHp() + stormwinds - 1);
		});

		runGym((context, player, opponent) -> {
			int clerics = 0;
			Minion onBoardBefore = playMinionCard(context, player, "token_searing_totem");
			MinionCard startedInHand = receiveCard(context, player, "token_searing_totem");
			Minion copyCard = playMinionCard(context, player, "minion_northshire_cleric");
			clerics++;
			Minion damaged = playMinionCardWithBattlecry(context, player, "minion_farseer_nobundo", copyCard);
			clerics++;
			playCard(context, player, startedInHand);
			clerics++;
			damaged.setHp(damaged.getHp() - 1);
			Assert.assertTrue(damaged.isWounded());
			for (int i = 0; i < 30; i++) {
				shuffleToDeck(context, player, "minion_bloodfen_raptor");
			}
			Assert.assertEquals(player.getHand().size(), 0);
			playCardWithTarget(context, player, "hero_power_heal", damaged);
			Assert.assertEquals(player.getHand().size(), clerics);
		});

		// Test deathrattle
		runGym((context, player, opponent) -> {
			int lootHoarders = 0;
			Minion onBoardBefore = playMinionCard(context, player, "token_searing_totem");
			MinionCard startedInHand = receiveCard(context, player, "token_searing_totem");
			Minion copyCard = playMinionCard(context, player, "minion_loot_hoarder");
			lootHoarders++;
			Minion damaged = playMinionCardWithBattlecry(context, player, "minion_farseer_nobundo", copyCard);
			lootHoarders++;
			playCard(context, player, startedInHand);
			lootHoarders++;
			for (int i = 0; i < 30; i++) {
				shuffleToDeck(context, player, "minion_bloodfen_raptor");
			}
			Assert.assertEquals(player.getHand().size(), 0);
			playCard(context, player, "spell_twisting_nether");
			Assert.assertEquals(player.getHand().size(), lootHoarders);
		});

		// Test copies text attribute of source card even when silenced
		runGym((context, player, opponent) -> {
			Minion onBoardBefore = playMinionCard(context, player, "token_searing_totem");
			Minion copyCard = playMinionCard(context, player, "minion_argent_Squire");
			playMinionCardWithBattlecry(context, player, "minion_farseer_nobundo", copyCard);
			Assert.assertTrue(onBoardBefore.hasAttribute(Attribute.DIVINE_SHIELD));
			playCardWithTarget(context, player, "spell_silence", copyCard);
			Assert.assertTrue(onBoardBefore.hasAttribute(Attribute.DIVINE_SHIELD));
		});

		runGym((context, player, opponent) -> {
			Minion onBoardBefore = playMinionCard(context, player, "token_searing_totem");
			Minion copyCard = playMinionCard(context, player, "minion_argent_Squire");
			playCardWithTarget(context, player, "spell_silence", copyCard);
			playMinionCardWithBattlecry(context, player, "minion_farseer_nobundo", copyCard);
			Assert.assertTrue(onBoardBefore.hasAttribute(Attribute.DIVINE_SHIELD));
		});

		// Test does not copy non-text attributes (buffs or whatever)
		runGym((context, player, opponent) -> {
			Minion onBoardBefore = playMinionCard(context, player, "token_searing_totem");
			Minion copyCard = playMinionCard(context, player, "minion_argent_Squire");
			playCardWithTarget(context, player, "spell_windfury", copyCard);
			playMinionCardWithBattlecry(context, player, "minion_farseer_nobundo", copyCard);
			Assert.assertFalse(onBoardBefore.hasAttribute(Attribute.WINDFURY));
		});
	}

	@Test
	public void testTheEndTime() {
		runGym((context, player, opponent) -> {
			Minion endTime = playMinionCard(context, player, "permanent_the_end_time");
			Assert.assertEquals(endTime.getAttributeValue(Attribute.RESERVED_INTEGER_1), 20);
			context.endTurn();
			Assert.assertEquals(endTime.getAttributeValue(Attribute.RESERVED_INTEGER_1), 19);
			context.endTurn();
			Assert.assertEquals(endTime.getAttributeValue(Attribute.RESERVED_INTEGER_1), 19);
			context.endTurn();
			Assert.assertEquals(endTime.getAttributeValue(Attribute.RESERVED_INTEGER_1), 17);
		});

		runGym((context, player, opponent) -> {
			Minion endTime = playMinionCard(context, player, "permanent_the_end_time");
			Assert.assertEquals(endTime.getAttributeValue(Attribute.RESERVED_INTEGER_1), 20);
			endTime.setAttribute(Attribute.RESERVED_INTEGER_1, 1);
			context.endTurn();
			Assert.assertEquals(context.getStatus(), GameStatus.WON);
			Assert.assertEquals(context.getWinningPlayerId(), player.getId());
		});
	}

	@Test
	public void testSpaceMoorine() {
		runGym((context, player, opponent) -> {
			Minion spaceMoorine = playMinionCard(context, player, "minion_space_moorine");
			Assert.assertFalse(spaceMoorine.hasAttribute(Attribute.AURA_TAUNT));
			playCard(context, player, "spell_iron_hide");
			Assert.assertTrue(spaceMoorine.hasAttribute(Attribute.AURA_TAUNT));
			context.endTurn();
			Minion wolfrider = playMinionCard(context, opponent, "minion_wolfrider");
			Assert.assertTrue(context.getValidActions().stream().filter(va -> va.getActionType() == ActionType.PHYSICAL_ATTACK)
					.allMatch(t -> t.getTargetReference().equals(spaceMoorine.getReference())));
		});
	}

	@Test
	public void testArmageddonVanguard() {
		runGym((context, player, opponent) -> {
			Minion bloodfen = playMinionCard(context, player, "minion_bloodfen_raptor");
			Minion armageddon = playMinionCard(context, player, "minion_armageddon_vanguard");
			context.endTurn();
			int opponentHp = opponent.getHero().getHp();
			playCardWithTarget(context, opponent, "spell_razorpetal", bloodfen);
			Assert.assertEquals(opponent.getHero().getHp(), opponentHp - 1);
		});
	}

	@Test
	public void testVindicatorMaraad() {
		runGym((context, player, opponent) -> {
			Card cost1Card = putOnTopOfDeck(context, player, "minion_argent_squire");
			playCard(context, player, "minion_vindicator_maraad");
			playCard(context, player, "spell_mirror_image");
			Assert.assertEquals(player.getHand().get(0), cost1Card);
		});

		runGym((context, player, opponent) -> {
			Card cost2Card = putOnTopOfDeck(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_vindicator_maraad");
			playCard(context, player, "spell_mirror_image");
			Assert.assertEquals(player.getHand().size(), 0);
		});

		runGym((context, player, opponent) -> {
			Card cost1Card = putOnTopOfDeck(context, player, "minion_argent_squire");
			playCard(context, player, "minion_vindicator_maraad");
			playCard(context, player, "minion_bloodfen_raptor");
			Assert.assertEquals(player.getHand().size(), 0);
		});

		runGym((context, player, opponent) -> {
			Card cost2Card = putOnTopOfDeck(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_vindicator_maraad");
			playCard(context, player, "minion_bloodfen_raptor");
			Assert.assertEquals(player.getHand().size(), 0);
		});
	}

	@Test
	public void testEscapeFromDurnholde() {
		runGym((context, player, opponent) -> {
			Card shouldntDraw = putOnTopOfDeck(context, player, "spell_the_coin");
			Card shouldDraw = putOnTopOfDeck(context, player, "spell_the_coin");
			Assert.assertEquals(shouldntDraw.getZone(), Zones.DECK);
			Assert.assertEquals(shouldDraw.getZone(), Zones.DECK);
			playCard(context, player, "permanent_escape_from_durnholde");
			context.endTurn();
			Assert.assertEquals(shouldntDraw.getZone(), Zones.DECK);
			Assert.assertEquals(shouldDraw.getZone(), Zones.DECK);
			context.endTurn();
			Assert.assertEquals(shouldDraw.getZone(), Zones.HAND);
			Assert.assertEquals(shouldntDraw.getZone(), Zones.DECK);
		});

		runGym((context, player, opponent) -> {
			Card shouldDraw1 = putOnTopOfDeck(context, player, "spell_the_coin");
			Card shouldDraw2 = putOnTopOfDeck(context, player, "spell_the_coin");
			playCard(context, player, "permanent_escape_from_durnholde");
			playMinionCard(context, player, "minion_bloodfen_raptor");
			context.endTurn();
			context.endTurn();
			Assert.assertEquals(shouldDraw1.getZone(), Zones.HAND);
			Assert.assertEquals(shouldDraw2.getZone(), Zones.HAND);
		});
	}

	@Test
	public void testHypnotist() {
		runGym((context, player, opponent) -> {
			MinionCard giantCard = receiveCard(context, player, "minion_molten_giant");
			// Reduce its effective cost
			playCardWithTarget(context, player, "spell_pyroblast", player.getHero());
			final int pyroblastDamage = 10;
			Assert.assertEquals(costOf(context, player, giantCard), giantCard.getBaseManaCost() - pyroblastDamage);
			Minion giant = playMinionCard(context, player, giantCard);
			playMinionCardWithBattlecry(context, player, "minion_hypnotist", giant);
			Assert.assertEquals(giant.getHp(), giant.getSourceCard().getBaseManaCost(), "Hypnotist should set hp to base cost.");
			Assert.assertEquals(giant.getAttack(), giant.getSourceCard().getBaseManaCost(), "Hypnotist should set attack to base cost.");
		});
	}

	@Test
	public void testDesolationOfKaresh() {
		// No combos played, should die
		runGym((context, player, opponent) -> {
			Minion desolation = playMinionCard(context, player, "permanent_desolation_of_karesh");
			context.endTurn();
			Assert.assertTrue(desolation.isDestroyed());
		});

		// Activated combo card played, should die in 2 turns
		runGym((context, player, opponent) -> {
			Minion desolation = playMinionCard(context, player, "permanent_desolation_of_karesh");
			playCard(context, player, "minion_defias_ringleader");
			context.endTurn();
			Assert.assertFalse(desolation.isDestroyed());
			context.endTurn();
			context.endTurn();
			Assert.assertTrue(desolation.isDestroyed());
		});

		// Not combo card played, should die next turn
		runGym((context, player, opponent) -> {
			Minion desolation = playMinionCard(context, player, "permanent_desolation_of_karesh");
			playCard(context, player, "minion_bloodfen_raptor");
			context.endTurn();
			Assert.assertTrue(desolation.isDestroyed());
		});

		// Activated combo card played, then not activated combo card played. Should die in 2 turns.
		runGym((context, player, opponent) -> {
			Minion desolation = playMinionCard(context, player, "permanent_desolation_of_karesh");
			playCard(context, player, "minion_defias_ringleader");
			context.endTurn();
			Assert.assertFalse(desolation.isDestroyed());
			context.endTurn();
			playCard(context, player, "minion_defias_ringleader");
			context.endTurn();
			Assert.assertTrue(desolation.isDestroyed());
		});

		// Activated combo card played, then not activated combo card played, then activated combo card played. Should die in 3 turns.
		runGym((context, player, opponent) -> {
			Minion desolation = playMinionCard(context, player, "permanent_desolation_of_karesh");
			playCard(context, player, "minion_defias_ringleader");
			context.endTurn();
			Assert.assertFalse(desolation.isDestroyed());
			context.endTurn();
			playCard(context, player, "minion_defias_ringleader");
			playCard(context, player, "minion_defias_ringleader");
			context.endTurn();
			Assert.assertFalse(desolation.isDestroyed());
			context.endTurn();
			context.endTurn();
			Assert.assertTrue(desolation.isDestroyed());
		});
	}

	@Test
	public void testShadowOfThePast() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			playCard(context, opponent, "minion_wolfrider");
			playCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			Minion shadow = playMinionCard(context, player, "minion_shadow_of_the_past");
			playCard(context, player, "minion_boulderfist_ogre");
			context.endTurn();
			playCardWithTarget(context, opponent, "spell_fireball", shadow);
			Assert.assertEquals(player.getHand().get(0).getCardId(), "spell_fireball");
		});
	}

	@Test
	public void testOwnWorstEnemey() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_own_worst_enemy");
			Minion target = playMinionCard(context, player, "minion_bloodfen_raptor");
			context.endTurn();
			Minion source = playMinionCard(context, opponent, "minion_wolfrider");
			attack(context, opponent, source, target);
			Assert.assertTrue(source.isDestroyed());
			Assert.assertFalse(target.isDestroyed());
			Assert.assertTrue(player.getGraveyard().stream().anyMatch(c -> c.getEntityType() == EntityType.MINION
					&& c.getSourceCard().getCardId().equals("minion_wolfrider")));
		});
	}

	@Test
	public void testInfiniteTimereaver() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			Card toDraw = putOnTopOfDeck(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_infinite_timereaver");
			playCardWithTarget(context, player, "spell_fireball", target);
			Assert.assertEquals(player.getHand().get(0), toDraw);
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			Card toDraw = putOnTopOfDeck(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_infinite_timereaver");
			playCard(context, player, "spell_flamestrike");
			Assert.assertEquals(player.getHand().get(0), toDraw);
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			putOnTopOfDeck(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_infinite_timereaver");
			playCardWithTarget(context, player, "spell_razorpetal", target);
			Assert.assertEquals(player.getHand().size(), 0);
			playCardWithTarget(context, player, "spell_razorpetal", target);
			Assert.assertEquals(player.getHand().size(), 0);
		});
	}

	@Test
	public void testFreya() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_freya");
			Minion nordrassil = player.getMinions().get(1);
			Assert.assertEquals(nordrassil.getSourceCard().getCardId(), "permanent_seed_of_nordrassil");
			Assert.assertEquals(nordrassil.getAttributeValue(Attribute.RESERVED_INTEGER_1), 0, "Freya should not trigger Seed");
			Minion bloodfen = playMinionCard(context, player, "minion_bloodfen_raptor");
			Assert.assertEquals(nordrassil.getAttributeValue(Attribute.RESERVED_INTEGER_1), bloodfen.getAttack() + bloodfen.getHp());
			for (int i = 0; i < 2; i++) {
				playCard(context, player, "minion_faceless_behemoth");
			}

			Assert.assertEquals(nordrassil.transformResolved(context).getSourceCard().getCardId(), "token_nordrassil", "Seed transformed into Nordrassil");
		});
	}

	@Test
	public void testTheBigGameHunt() {
		runGym((context, player, opponent) -> {
			Minion bigGameHunt = playMinionCard(context, player, "permanent_the_big_game_hunt");
			int elapsedLocalPlayerTurns = 0;
			Minion bloodfen1 = playMinionCard(context, player, "minion_bloodfen_raptor");
			Minion bloodfen2 = playMinionCard(context, player, "minion_bloodfen_raptor");
			Minion bloodfen3 = playMinionCard(context, player, "minion_bloodfen_raptor");
			context.endTurn();
			elapsedLocalPlayerTurns++;
			Minion bloodfen4 = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			Minion bloodfen5 = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			Minion bloodfen6 = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			// one point for player
			attack(context, player, bloodfen1, bloodfen4);
			context.endTurn();
			elapsedLocalPlayerTurns++;
			// two points for opponent
			attack(context, opponent, bloodfen5, bloodfen2);
			attack(context, opponent, bloodfen6, bloodfen3);
			context.endTurn();
			for (int i = elapsedLocalPlayerTurns; i < 4; i++) {
				context.endTurn();
				context.endTurn();
			}
			Assert.assertTrue(bigGameHunt.isDestroyed());
			// Should be a total of -1
			Assert.assertEquals(bigGameHunt.getAttributeValue(Attribute.RESERVED_INTEGER_1), -1);
			Minion kingBangalash1 = player.getMinions().get(0);
			Assert.assertEquals(kingBangalash1.getSourceCard().getCardId(), "minion_king_bangalash");
			Assert.assertEquals(kingBangalash1.getAttack(), kingBangalash1.getBaseAttack() - 1);
			Assert.assertEquals(kingBangalash1.getHp(), kingBangalash1.getBaseHp() - 1);

			// Play a King Bangalash from the hand, observe it has the same buffs.
			Minion kingBangalash2 = playMinionCard(context, player, "minion_king_bangalash");
			Assert.assertEquals(kingBangalash2.getAttack(), kingBangalash2.getBaseAttack() - 1);
			Assert.assertEquals(kingBangalash2.getHp(), kingBangalash2.getBaseHp() - 1);
		});
	}

	@Test
	public void testLieInWait() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_lie_in_wait");
			context.endTurn();
			Minion wolfrider = playMinionCard(context, opponent, "minion_wolfrider");
			attack(context, opponent, wolfrider, player.getHero());

			Assert.assertEquals(player.getWeaponZone().get(0).getDurability(),
					((WeaponCard) CardCatalogue.getCardById("weapon_eaglehorn_bow")).getBaseDurability() - 1,
					"Eaglehorn Bow loses durability because the secret triggered before it was in play.");
			Assert.assertTrue(wolfrider.isDestroyed());
		});
	}

	@Test
	public void testFifiFizzlewarp() {
		// Test that cards that have race-filtered battlecries work correctly after Fifi Fizzlewarp
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "minion_boulderfist_ogre");

			for (int i = 0; i < 2; i++) {
				putOnTopOfDeck(context, player, "minion_bloodfen_raptor");
			}

			OverrideHandle<Card> handle = overrideRandomCard(context, "minion_virmen_sensei");
			Card fifi = receiveCard(context, player, "minion_fifi_fizzlewarp");
			context.fireGameEvent(new GameStartEvent(context, player.getId()));
			handle.stop();

			context.getLogic().discardCard(player, fifi);

			for (int i = 0; i < 3; i++) {
				context.getLogic().drawCard(player.getId(), player);
			}

			for (Card card : player.getHand().subList(0, 2)) {
				Assert.assertEquals(card.getCardId(), "minion_virmen_sensei");
				Assert.assertEquals(card.getRace(), Race.BEAST);
			}

			final MinionCard boulderfist = (MinionCard) player.getHand().get(2);
			Assert.assertEquals(boulderfist.getCardId(), "minion_virmen_sensei");
			Assert.assertEquals(boulderfist.getRace(), Race.NONE);

			final MinionCard vermin1 = (MinionCard) player.getHand().get(0);
			final MinionCard vermin2 = (MinionCard) player.getHand().get(1);

			Minion target = playMinionCard(context, player, vermin1);
			Minion notTarget = playMinionCard(context, player, boulderfist);

			CountDownLatch latch = new CountDownLatch(1);
			// Checks that a Virmen Sensei can target the Beast Virmen Sensei on the board and not the Race.NONE
			// Virmen Sensei that was created from the Boulderfist Ogre
			overrideBattlecry(player, battlecryActions -> {
				Assert.assertEquals(battlecryActions.size(), 1);
				Assert.assertEquals(battlecryActions.get(0).getTargetReference(), target.getReference());
				latch.countDown();
				return battlecryActions.get(0);
			});

			playCard(context, player, vermin2);
			Assert.assertEquals(latch.getCount(), 0, "Should have requested battlecries");
		});

		// Tol'Vir Warden should interact correctly with cards transformed by Fifi Fizzlewarp
		runGym((context, player, opponent) -> {
			// Cost 1 card
			MinionCard shouldBeDrawn = putOnTopOfDeck(context, player, "minion_dire_mole");
			// Cost 2 card
			MinionCard shouldNotBeDrawn = putOnTopOfDeck(context, player, "minion_bloodfen_raptor");
			MinionCard tolvirToPlay = putOnTopOfDeck(context, player, "minion_dire_mole");

			OverrideHandle<Card> handle = overrideRandomCard(context, "minion_tolvir_warden");
			Card fifi = receiveCard(context, player, "minion_fifi_fizzlewarp");
			context.fireGameEvent(new GameStartEvent(context, player.getId()));
			handle.stop();

			context.getLogic().discardCard(player, fifi);
			Card drawnCard = context.getLogic().drawCard(player.getId(), player);
			Assert.assertEquals(drawnCard, tolvirToPlay.transformResolved(context));
			tolvirToPlay = (MinionCard) tolvirToPlay.transformResolved(context);
			shouldBeDrawn = (MinionCard) shouldBeDrawn.transformResolved(context);
			shouldNotBeDrawn = (MinionCard) shouldNotBeDrawn.transformResolved(context);

			playCard(context, player, tolvirToPlay);
			Assert.assertEquals(shouldBeDrawn.getZone(), Zones.HAND);
			Assert.assertEquals(shouldNotBeDrawn.getZone(), Zones.DECK);
		});

		// Getting Divine Shield minions from Fifi Fizzlewarp should work
		runGym((context, player, opponent) -> {
			MinionCard shouldBeDrawn = putOnTopOfDeck(context, player, "minion_dire_mole");
			OverrideHandle<Card> handle = overrideRandomCard(context, "minion_argent_squire");
			Card fifi = receiveCard(context, player, "minion_fifi_fizzlewarp");
			context.fireGameEvent(new GameStartEvent(context, player.getId()));
			handle.stop();
			context.getLogic().discardCard(player, fifi);
			Card drawnCard = context.getLogic().drawCard(player.getId(), player);
			Assert.assertEquals(drawnCard, shouldBeDrawn.transformResolved(context));
			shouldBeDrawn = (MinionCard) shouldBeDrawn.transformResolved(context);
			Minion argentSquire = playMinionCard(context, player, shouldBeDrawn);
			Assert.assertTrue(argentSquire.hasAttribute(Attribute.DIVINE_SHIELD));
		});

		// Test specifically Tirion's deathrattle
		runGym((context, player, opponent) -> {
			MinionCard shouldBeDrawn = putOnTopOfDeck(context, player, "minion_dire_mole");
			OverrideHandle<Card> handle = overrideRandomCard(context, "minion_tirion_fordring");
			Card fifi = receiveCard(context, player, "minion_fifi_fizzlewarp");
			context.fireGameEvent(new GameStartEvent(context, player.getId()));
			handle.stop();
			context.getLogic().discardCard(player, fifi);
			Card drawnCard = context.getLogic().drawCard(player.getId(), player);
			Assert.assertEquals(drawnCard, shouldBeDrawn.transformResolved(context));
			shouldBeDrawn = (MinionCard) shouldBeDrawn.transformResolved(context);
			Minion tirion = playMinionCard(context, player, shouldBeDrawn);
			playCardWithTarget(context, player, "spell_fireball", tirion);
			playCardWithTarget(context, player, "spell_fireball", tirion);
			Assert.assertTrue(tirion.isDestroyed());
			Assert.assertEquals(player.getHero().getWeapon().getSourceCard().getCardId(), "weapon_ashbringer");
		});

		// Test Leyline Manipulator doesn't reduce cost of fifi cards
		runGym((context, player, opponent) -> {
			MinionCard shouldBeDrawn = putOnTopOfDeck(context, player, "minion_dire_mole");
			OverrideHandle<Card> handle = overrideRandomCard(context, "minion_tirion_fordring");
			Card fifi = receiveCard(context, player, "minion_fifi_fizzlewarp");
			context.fireGameEvent(new GameStartEvent(context, player.getId()));
			handle.stop();
			context.getLogic().discardCard(player, fifi);
			Card drawnCard = context.getLogic().drawCard(player.getId(), player);
			Assert.assertEquals(drawnCard, shouldBeDrawn.transformResolved(context));
			shouldBeDrawn = (MinionCard) shouldBeDrawn.transformResolved(context);
			playCard(context, player, "minion_leyline_manipulator");
			Assert.assertEquals(costOf(context, player, shouldBeDrawn), CardCatalogue.getCardById("minion_dire_mole").getBaseManaCost());
		});
	}

	@Test
	public void testParadoxKingTogwaggleInteraction() {
		runGym((context, player, opponent) -> {
			Minion paradox = playMinionCard(context, player, "minion_paradox");
			playCard(context, player, "minion_king_togwaggle");
			Assert.assertEquals(player.getSetAsideZone().size(), 0);
			Assert.assertEquals(player.getHand().get(0).getCardId(), "minion_paradox");
		});
	}

	@Test
	public void testParadox() {
		runGym((context, player, opponent) -> {
			Minion paradox = playMinionCard(context, player, "minion_paradox");
			Assert.assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_paradox");
			Assert.assertEquals(player.getHand().size(), 0);
			playCard(context, player, "spell_the_coin");
			Assert.assertEquals(player.getMinions().size(), 0);
			Assert.assertEquals(player.getHand().get(0).getCardId(), "minion_paradox");
		});
	}

	@Test
	public void testEchoOfMalfurion() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_bloodfen_raptor");
			MinionCard boulderfist = (MinionCard) receiveCard(context, player, "minion_boulderfist_ogre");
			Minion echo = playMinionCard(context, player, "token_echo_of_malfurion");
			Assert.assertEquals(echo.getAttack(), boulderfist.getAttack() + echo.getBaseAttack());
			Assert.assertEquals(echo.getHp(), boulderfist.getHp() + echo.getBaseHp());
		});

		runGym((context, player, opponent) -> {
			Minion echo = playMinionCard(context, player, "token_echo_of_malfurion");
			Assert.assertEquals(echo.getAttack(), echo.getBaseAttack());
			Assert.assertEquals(echo.getHp(), echo.getBaseHp());
		});
	}

	@Test
	public void testChromie() {
		// Test no excess cards
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 6; i++) {
				shuffleToDeck(context, player, "minion_bloodfen_raptor");
				receiveCard(context, player, "spell_mirror_image");
			}

			playCard(context, player, "minion_chromie");

			Assert.assertTrue(player.getHand().stream().allMatch(c -> c.getCardId().equals("minion_bloodfen_raptor")));
			Assert.assertTrue(player.getDeck().stream().allMatch(c -> c.getCardId().equals("spell_mirror_image")));
			Assert.assertEquals(player.getHand().size(), 6);
			Assert.assertEquals(player.getDeck().size(), 6);
		});

		// Test excess cards
		runGym((context, player, opponent) -> {
			player.getGraveyard().clear();

			for (int i = 0; i < 20; i++) {
				shuffleToDeck(context, player, "minion_bloodfen_raptor");
			}

			for (int i = 0; i < 6; i++) {
				receiveCard(context, player, "spell_mirror_image");
			}

			playCard(context, player, "minion_chromie");

			Assert.assertTrue(player.getHand().stream().allMatch(c -> c.getCardId().equals("minion_bloodfen_raptor")));
			Assert.assertTrue(player.getDeck().stream().allMatch(c -> c.getCardId().equals("spell_mirror_image")));
			Assert.assertEquals(player.getHand().size(), 10);
			Assert.assertEquals(player.getDeck().size(), 6);
		});
	}

	@Test
	public void testHighmountainPrimalist() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 29; i++) {
				shuffleToDeck(context, player, "minion_bloodfen_raptor");
			}
			overrideDiscover(context, player, "spell_mirror_image");
			playCard(context, player, "minion_highmountain_primalist");
			playCard(context, player, "minion_novice_engineer");
			Assert.assertEquals(player.getHand().get(0).getCardId(), "spell_mirror_image");
		});
	}

	@Test
	public void testDimensionalCourier() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_dimensional_courier");
			Assert.assertEquals(player.getHand().get(0).getCardId(), "minion_bloodfen_raptor");
		});

		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_snowflipper_penguin");
			playCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_dimensional_courier");
			Assert.assertEquals(player.getHand().size(), 0);
		});
	}

	@Test
	public void testPermanentCallOfTheCrusade() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "permanent_call_of_the_crusade");
			Minion bloodfen = playMinionCard(context, player, "minion_bloodfen_raptor");
			for (int i = 0; i < 3; i++) {
				Assert.assertEquals(bloodfen.getAttack(), bloodfen.getBaseAttack() + 1);
				context.endTurn();
				context.endTurn();
			}
			Assert.assertEquals(bloodfen.getAttack(), bloodfen.getBaseAttack());
			Assert.assertEquals(player.getMinions().size(), 1);
		});
	}

	@Test
	public void testHandsOnHistorian() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_bloodfen_raptor");
			for (int i = 0; i < 3; i++) {
				receiveCard(context, player, "minion_bloodfen_raptor");
			}
			int size = player.getHand().size();
			playCard(context, player, "minion_hands_on_historian");
			Assert.assertEquals(player.getHand().size(), size);
		});

		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_bloodfen_raptor");
			for (int i = 0; i < 2; i++) {
				receiveCard(context, player, "minion_bloodfen_raptor");
			}
			int size = player.getHand().size();
			playCard(context, player, "minion_hands_on_historian");
			Assert.assertEquals(player.getHand().size(), size + 1);
		});
	}

	@Test
	public void testVampiricTouch() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion wolfrider = playMinionCard(context, opponent, "minion_wolfrider");
			context.endTurn();
			playCardWithTarget(context, player, "spell_vampiric_touch", wolfrider);
			Assert.assertTrue(wolfrider.isDestroyed());
			Assert.assertEquals(player.getMinions().get(0).getAttack(), 1);
			Assert.assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_wolfrider");
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion mindControlTech = playMinionCard(context, opponent, "minion_mind_control_tech");
			context.endTurn();
			playCardWithTarget(context, player, "spell_vampiric_touch", mindControlTech);
			Assert.assertFalse(mindControlTech.isDestroyed());
			Assert.assertEquals(player.getMinions().size(), 0);
		});
	}

	@Test
	public void testDivineIntervention() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_divine_intervention");
			Minion lightwarden = playMinionCard(context, player, "minion_lightwarden");
			player.getHero().setHp(5);
			context.endTurn();
			playCardWithTarget(context, opponent, "spell_fireball", player.getHero());
			Assert.assertEquals(player.getSecrets().size(), 0);
			Assert.assertEquals(player.getHero().getHp(), 11, "Should have healed for 6");
			Assert.assertEquals(lightwarden.getAttack(), lightwarden.getBaseAttack() + 2, "Lightwarden should have buffed");
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_divine_intervention");
			Minion lightwarden = playMinionCard(context, player, "minion_lightwarden");
			player.getHero().setHp(7);
			context.endTurn();
			playCardWithTarget(context, opponent, "spell_fireball", player.getHero());
			Assert.assertEquals(player.getSecrets().size(), 1);
			Assert.assertEquals(player.getHero().getHp(), 1, "Should not have healed.");
			Assert.assertEquals(lightwarden.getAttack(), lightwarden.getBaseAttack(), "Lightwarden should not have buffed");
		});
	}

	@Test
	public void testYrel() {
		runGym((context, player, opponent) -> {
			Minion yrel = playMinionCard(context, player, "minion_yrel");
			player.setMaxMana(4);
			player.setMana(4);
			playCardWithTarget(context, player, "spell_fireball", opponent.getHero());
			Assert.assertEquals(player.getMana(), 0);
			player.setMana(4);
			playCardWithTarget(context, player, "spell_fireball", yrel);
			Assert.assertEquals(player.getMana(), 0);
		});

		runGym((context, player, opponent) -> {
			Minion yrel = playMinionCard(context, player, "minion_yrel");
			player.setMaxMana(5);
			player.setMana(5);
			playCardWithTarget(context, player, "spell_power_word_tentacles", yrel);
			Assert.assertEquals(player.getMana(), 5);
		});
	}

	@Test
	public void testWorgenAmbusher() {
		runGym((context, player, opponent) -> {
			Minion worgen1 = playMinionCard(context, player, "minion_worgen_ambusher");
			Assert.assertEquals(worgen1.getAttack(), worgen1.getBaseAttack());
			Minion worgen2 = playMinionCard(context, player, "minion_worgen_ambusher");
			Assert.assertEquals(worgen2.getAttack(), worgen2.getBaseAttack() + 1);
		});
	}

	@Test
	public void testCriminologist() {
		final int MAGE = 0;
		final int HUNTER = 1;
		final int PALADIN = 2;
		final int ROGUE = 3;
		Stream.of(MAGE, HUNTER, PALADIN, ROGUE).forEach(heroClass -> {
			runGym((context, player, opponent) -> {
				playCard(context, player, "spell_the_coin");
				overrideDiscover(player, discoveries -> {
					Assert.assertEquals(discoveries.size(), 4);
					return discoveries.get(heroClass);
				});
				playCard(context, player, "minion_criminologist");
				HeroClass secretClass = player.getHand().get(0).getHeroClass();
				switch (heroClass) {
					case MAGE:
						Assert.assertEquals(secretClass, HeroClass.BLUE);
						break;
					case HUNTER:
						Assert.assertEquals(secretClass, HeroClass.GREEN);
						break;
					case PALADIN:
						Assert.assertEquals(secretClass, HeroClass.GOLD);
						break;
					case ROGUE:
						Assert.assertEquals(secretClass, HeroClass.BLACK);
						break;
				}
			});
		});
	}

	@Test
	public void testBlackLotus() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_black_lotus");
			playCardWithTarget(context, player, "spell_razorpetal", opponent.getHero());
			Assert.assertFalse(opponent.getHero().isDestroyed());
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_black_lotus");
			context.endTurn();
			Minion bloodfen = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			playCardWithTarget(context, player, "spell_razorpetal", bloodfen);
			Assert.assertTrue(bloodfen.isDestroyed());
		});
	}

	@Test
	public void testFrostBomb() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			Minion other = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			Minion friendly = playMinionCard(context, player, "minion_bloodfen_raptor");
			playCardWithTarget(context, player, "spell_frost_bomb", target);
			Assert.assertTrue(target.hasAttribute(Attribute.FROZEN));
			Assert.assertFalse(other.hasAttribute(Attribute.FROZEN));
			Assert.assertFalse(friendly.hasAttribute(Attribute.FROZEN));
			context.endTurn();
			Assert.assertTrue(target.hasAttribute(Attribute.FROZEN));
			Assert.assertFalse(other.hasAttribute(Attribute.FROZEN));
			Assert.assertFalse(friendly.hasAttribute(Attribute.FROZEN));
			context.endTurn();
			Assert.assertFalse(target.hasAttribute(Attribute.FROZEN));
			Assert.assertTrue(other.hasAttribute(Attribute.FROZEN));
			Assert.assertFalse(friendly.hasAttribute(Attribute.FROZEN));
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			Minion other = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			Minion friendly = playMinionCard(context, player, "minion_bloodfen_raptor");
			playCardWithTarget(context, player, "spell_frost_bomb", target);
			Assert.assertTrue(target.hasAttribute(Attribute.FROZEN));
			Assert.assertFalse(other.hasAttribute(Attribute.FROZEN));
			Assert.assertFalse(friendly.hasAttribute(Attribute.FROZEN));
			context.endTurn();
			playCardWithTarget(context, opponent, "spell_fireball", target);
			Assert.assertTrue(target.isDestroyed());
			Assert.assertFalse(other.hasAttribute(Attribute.FROZEN));
			Assert.assertFalse(friendly.hasAttribute(Attribute.FROZEN));
			context.endTurn();
			Assert.assertFalse(other.hasAttribute(Attribute.FROZEN));
			Assert.assertFalse(friendly.hasAttribute(Attribute.FROZEN));
		});
	}

	@Test
	public void testJadeAmbush() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_jade_idol_1");
			Minion originalJade = player.getMinions().get(0);
			playCard(context, player, "secret_jade_ambush");
			context.endTurn();
			Minion hound = playMinionCard(context, opponent, "token_hound");
			attack(context, opponent, hound, originalJade);
			Assert.assertEquals(player.getSecrets().size(), 0, "Jade Ambush should have triggered.");
			Assert.assertEquals(player.getMinions().size(), 2, "The player should have two jade golems");
			Minion newJade = player.getMinions().get(1);
			Assert.assertEquals(newJade.getHp(), 1, "The second jade should have 1 HP left.");
			Assert.assertEquals(newJade.getAttributeValue(Attribute.LAST_HIT), 1, "The second jade should have taken 1 damage");
			Assert.assertTrue(hound.isDestroyed());
			Assert.assertFalse(originalJade.isDestroyed());
		});
	}

	@Test
	public void testVereesaWindrunner() {
		GymFactory vareesaFactory = getGymFactory((context, player, opponent) -> {
			playCard(context, player, "minion_vereesa_windrunner");
		}, (context, player, opponent) -> {
			Assert.assertEquals(player.getSecrets().size(), 0);
		});

		GymFactory eaglehornBowFactory = getGymFactory((context, player, opponent) -> {
			playCard(context, player, "minion_vereesa_windrunner");
			playCard(context, player, "weapon_eaglehorn_bow");
		}, (context, player, opponent) -> {
			Assert.assertEquals(player.getSecrets().size(), 0);
			Assert.assertEquals(player.getWeaponZone().get(0).getDurability(), player.getWeaponZone().get(0).getBaseDurability() + 1);
		});

		Stream.of(vareesaFactory, eaglehornBowFactory).forEach(factory -> {
			Stream.of(
					"secret_freezing_trap",
					"secret_snipe",
					"secret_misdirection",
					"secret_corpse_explosion"
			).forEach(noEffectCardId -> {
				factory.run((context, player, opponent) -> {
					playCard(context, player, noEffectCardId);
				});
			});

			factory.run((context, player, opponent) -> {
				int opponentHp = opponent.getHero().getHp();
				playCard(context, player, "secret_explosive_trap");
				Assert.assertEquals(opponent.getHero().getHp(), opponentHp - 2);
			});

			factory.run((context, player, opponent) -> {
				playCard(context, player, "secret_cat_trick");
				Assert.assertEquals(player.getMinions().get(1).getSourceCard().getCardId(), "token_cat_in_a_hat");
			});

			factory.run((context, player, opponent) -> {
				MinionCard raptor = (MinionCard) receiveCard(context, player, "minion_bloodfen_raptor");
				playCard(context, player, "secret_hidden_cache");
				Minion raptorOnBoard = playMinionCard(context, player, raptor);
				Assert.assertEquals(raptorOnBoard.getAttack(), raptor.getBaseAttack() + 2);
				Assert.assertEquals(raptorOnBoard.getHp(), raptor.getBaseHp() + 2);
			});

			factory.run((context, player, opponent) -> {
				playCard(context, player, "secret_venomstrike_trap");
				Assert.assertEquals(player.getMinions().get(1).getSourceCard().getCardId(), "minion_emperor_cobra");
			});

			factory.run((context, player, opponent) -> {
				playCard(context, player, "secret_wandering_monster");
				Assert.assertEquals(player.getMinions().get(1).getSourceCard().getBaseManaCost(), 3);
			});

			factory.run((context, player, opponent) -> {
				playCard(context, player, "secret_lie_in_wait");
				Assert.assertEquals(player.getWeaponZone().get(0).getSourceCard().getCardId(), "weapon_eaglehorn_bow");
			});
		});
	}

	@Test
	public void testFleetfootedScout() {
		runGym((context, player, opponent) -> {
			Card card1 = receiveCard(context, player, "spell_barrage");
			Minion fleetfooted = playMinionCard(context, player, "minion_fleetfooted_scout");
			Card card2 = receiveCard(context, player, "spell_load_and_lock");
			Card card3 = receiveCard(context, player, "spell_mirror_image");
			Stream.of(card1, card2).forEach(c -> Assert.assertEquals(costOf(context, player, c), c.getBaseManaCost() - 1));
			Assert.assertEquals(costOf(context, player, card3), card3.getBaseManaCost());
			playCardWithTarget(context, player, "spell_fireball", fleetfooted);
			Stream.of(card1, card2).forEach(c -> Assert.assertEquals(costOf(context, player, c), c.getBaseManaCost()));
			Assert.assertEquals(costOf(context, player, card3), card3.getBaseManaCost());
		});
	}

	@Test
	public void testSecretGarden() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 30; i++) {
				shuffleToDeck(context, player, "minion_bloodfen_raptor");
			}
			playCard(context, player, "secret_secret_garden");
			context.endTurn();
			for (int i = 0; i < 30; i++) {
				shuffleToDeck(context, opponent, "minion_bloodfen_raptor");
			}
			playMinionCard(context, opponent, "minion_novice_engineer");
			Assert.assertEquals(player.getSecrets().size(), 1);
			Assert.assertEquals(player.getHand().size(), 0);
			playMinionCard(context, opponent, "minion_novice_engineer");
			Assert.assertEquals(player.getSecrets().size(), 0);
			Assert.assertEquals(player.getHand().size(), 3);
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_secret_garden");
			for (int i = 0; i < 30; i++) {
				shuffleToDeck(context, opponent, "minion_bloodfen_raptor");
			}
			context.endTurn();
			Assert.assertEquals(player.getSecrets().size(), 1);
			playMinionCard(context, opponent, "minion_novice_engineer");
			Assert.assertEquals(player.getSecrets().size(), 0);
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_secret_garden");
			for (int i = 0; i < 30; i++) {
				shuffleToDeck(context, opponent, "minion_bloodfen_raptor");
			}
			context.endTurn();
			Assert.assertEquals(player.getSecrets().size(), 1);
			Assert.assertEquals(player.getHand().size(), 0);
			context.endTurn();
			context.endTurn();
			Assert.assertEquals(player.getSecrets().size(), 1);
			playMinionCard(context, opponent, "minion_novice_engineer");
			Assert.assertEquals(player.getSecrets().size(), 0);
		});
	}

	@Test
	public void testMasterSorcerer() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_master_sorcerer");
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			shuffleToDeck(context, player, "minion_bloodfen_raptor");
			playCardWithTarget(context, player, "spell_fireball", target);
			Assert.assertEquals(player.getHand().size(), 1);
			Assert.assertEquals(player.getDeck().size(), 0);
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_master_sorcerer");
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_boulderfist_ogre");
			context.endTurn();
			shuffleToDeck(context, player, "minion_bloodfen_raptor");
			playCardWithTarget(context, player, "spell_fireball", target);
			Assert.assertEquals(player.getHand().size(), 0);
			Assert.assertEquals(player.getDeck().size(), 1);
		});
	}

	@Test
	public void testMetamagicTemporalFluxInteraction() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 3; i++) {
				shuffleToDeck(context, player, "spell_the_coin");
			}
			overrideDiscover(context, player, "spell_enhanced");
			playCard(context, player, "spell_metamagic");
			int opponentHp = opponent.getHero().getHp();
			playCardWithTarget(context, player, "spell_temporal_flux", opponent.getHero());
			Assert.assertEquals(opponent.getHero().getHp(), opponentHp - 3);
			Assert.assertEquals(player.getHand().size(), 3);
		});
	}

	@Test
	public void testMetamagic() {
		// Costs (2) less.
		runGym((context, player, opponent) -> {
			overrideDiscover(context, player, "spell_quickened");
			playCard(context, player, "spell_metamagic");
			player.setMaxMana(10);
			player.setMana(10);
			Card explosion = receiveCard(context, player, "spell_arcane_explosion");
			Assert.assertEquals(costOf(context, player, explosion), explosion.getBaseManaCost() - 2);
			playCard(context, player, explosion);
			explosion = receiveCard(context, player, "spell_arcane_explosion");
			Assert.assertEquals(costOf(context, player, explosion), explosion.getBaseManaCost());
		});

		// Deals 1 damage to all enemy minions.
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion villager = playMinionCard(context, opponent, "minion_possessed_villager");
			Minion bloodfen = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			overrideDiscover(context, player, "spell_unbounded");
			playCard(context, player, "spell_metamagic");
			Assert.assertFalse(villager.isDestroyed());
			Assert.assertEquals(bloodfen.getHp(), bloodfen.getBaseHp(), "Metamagic should not have triggered its own effect.");
			playCard(context, player, "spell_arcane_explosion");
			Assert.assertTrue(villager.isDestroyed());
			Assert.assertTrue(bloodfen.isDestroyed(), "Two damage should have been dealt in this sequence.");
			Assert.assertEquals(opponent.getMinions().size(), 1, "There should just be a shadowbeast, because the additional spell effect does not happen in its own sequence.");
			context.endTurn();
			bloodfen = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			Assert.assertEquals(opponent.getMinions().size(), 2, "There should be a shadowbeast and a bloodfen.");
			playCard(context, player, "spell_arcane_explosion");
			Assert.assertFalse(bloodfen.isDestroyed(), "The next arcane explosion should not have destroyed the bloodfen since it only dealt 1 damage");
			Assert.assertEquals(opponent.getMinions().size(), 1, "But the Shadowbeast should have been destroyed.");
		});

		// Returns to your deck after you cast it.
		runGym((context, player, opponent) -> {
			overrideDiscover(context, player, "spell_memorized");
			playCard(context, player, "spell_metamagic");
			playCard(context, player, "minion_bloodfen_raptor");
			Assert.assertEquals(player.getDeck().size(), 0, "We should not have shuffled a minion card into the deck.");
			context.endTurn();
			// We should still apply the effect to the next spell the player cast
			playCard(context, opponent, "spell_the_coin");
			Assert.assertEquals(player.getDeck().size(), 0, "The opponent's spell should not have been shuffled.");
			context.endTurn();
			playCard(context, player, "spell_arcane_explosion");
			Assert.assertEquals(player.getDeck().get(0).getCardId(), "spell_arcane_explosion");
			playCard(context, player, "spell_arcane_explosion");
			Assert.assertEquals(player.getDeck().size(), 1, "Only one copy of the card should have been shuffled.");
		});

		// Freezes two random enemies.
		runGym((context, player, opponent) -> {
			overrideDiscover(context, player, "spell_chilled");
			playCard(context, player, "spell_metamagic");
			context.endTurn();
			Minion minion1 = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			Minion minion2 = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			playCard(context, player, "spell_arcane_explosion");
			Assert.assertTrue(minion1.hasAttribute(Attribute.FROZEN));
			Assert.assertTrue(minion2.hasAttribute(Attribute.FROZEN));
			Assert.assertEquals(minion1.getHp(), minion1.getBaseHp() - 1);
			Assert.assertEquals(minion2.getHp(), minion1.getBaseHp() - 1);
		});

		// The next spell you cast costs (2) more and has Spell Damage +2.
		runGym((context, player, opponent) -> {
			overrideDiscover(context, player, "spell_enhanced");
			playCard(context, player, "spell_metamagic");
			Card fireball = receiveCard(context, player, "spell_fireball");
			Assert.assertEquals(costOf(context, player, fireball), fireball.getBaseManaCost() + 2);
			Assert.assertEquals(player.getAttributeValue(Attribute.SPELL_DAMAGE), 2);
			int opponentHp = opponent.getHero().getHp();
			playCardWithTarget(context, player, fireball, opponent.getHero());
			Assert.assertEquals(opponent.getHero().getHp(), opponentHp - 8);
			fireball = receiveCard(context, player, "spell_fireball");
			Assert.assertEquals(costOf(context, player, fireball), fireball.getBaseManaCost(), "The 2nd spell should not be more expensive");
			opponentHp = opponent.getHero().getHp();
			playCardWithTarget(context, player, fireball, opponent.getHero());
			Assert.assertEquals(opponent.getHero().getHp(), opponentHp - 6, "The 2nd spell should not have gotten spell damage +2.");
			opponentHp = opponent.getHero().getHp();
			playCardWithTarget(context, player, fireball, opponent.getHero());
			Assert.assertEquals(opponent.getHero().getHp(), opponentHp - 6, "The 3nd spell should not have gotten spell damage -2.");
		});

		// Deals 3 damage to a random enemy minion.
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion chillwind = playMinionCard(context, opponent, "minion_chillwind_yeti");
			context.endTurn();
			overrideDiscover(context, player, "spell_empowered");
			playCard(context, player, "spell_metamagic");
			Assert.assertEquals(chillwind.getHp(), chillwind.getBaseHp(), "Metamagic should not have triggered its own effect.");
			playCardWithTarget(context, player, "spell_fireball", opponent.getHero());
			Assert.assertEquals(chillwind.getHp(), chillwind.getBaseHp() - 3);
			playCardWithTarget(context, player, "spell_fireball", opponent.getHero());
			Assert.assertEquals(chillwind.getHp(), chillwind.getBaseHp() - 3, "The empowered effect should have expired");
		});
	}

	@Test
	public void testNexusKingSalhadaar() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_nexus_king_salhadaar");
			Assert.assertEquals(player.getMinions().size(), 1);
			Assert.assertTrue(player.getHand().stream().allMatch(c -> costOf(context, player, c) == 1));
		});
	}

	@Test
	public void testSageOfFoursight() {
		runGym((context, player, opponent) -> {
			Minion sage = playMinionCard(context, player, "minion_sage_of_foursight");
			Assert.assertEquals(sage.getAttack(), sage.getBaseAttack(), "Sage should not buff itself.");
			Assert.assertEquals(sage.getHp(), sage.getBaseHp(), "Sage should not buff itself.");
			Card bloodfenCard = CardCatalogue.getCardById("minion_bloodfen_raptor");
			context.getLogic().receiveCard(player.getId(), bloodfenCard);
			Assert.assertEquals(costOf(context, player, bloodfenCard), bloodfenCard.getBaseManaCost() + 4, "Bloodfen should cost more because it's the next card the player will play.");

			// It should work with a one turn gap in the middle
			context.endTurn();
			context.endTurn();

			Minion bloodfen = playMinionCard(context, player, (MinionCard) bloodfenCard);
			Assert.assertEquals(bloodfen.getAttack(), bloodfen.getBaseAttack() + 4, "Bloodfen should be buffed.");
			Assert.assertEquals(bloodfen.getHp(), bloodfen.getBaseHp() + 4, "Bloodfen should be buffed.");
			Card bloodfenCard2 = CardCatalogue.getCardById("minion_bloodfen_raptor");
			context.getLogic().receiveCard(player.getId(), bloodfenCard2);
			Assert.assertEquals(costOf(context, player, bloodfenCard), bloodfenCard.getBaseManaCost(), "Bloodfen 2 should not cost more.");
			Minion bloodfen2 = playMinionCard(context, player, (MinionCard) bloodfenCard2);
			Assert.assertEquals(bloodfen2.getAttack(), bloodfen2.getBaseAttack(), "The second bloodfen should not be buffed");
			Assert.assertEquals(bloodfen2.getHp(), bloodfen2.getBaseHp(), "The second bloodfen should not be buffed");
		});
	}

	public void testScorpidStinger() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_scorpid_stinger");
			context.endTurn();
			Minion flipper = playMinionCard(context, opponent, "minion_snowflipper_penguin");
			context.endTurn();
			attack(context, player, player.getHero(), flipper);
			Assert.assertTrue(player.getHand().containsCard("spell_inner_rage"));
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_scorpid_stinger");
			context.endTurn();
			Minion bloodfen = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			attack(context, player, player.getHero(), bloodfen);
			Assert.assertFalse(player.getHand().containsCard("spell_inner_rage"));
		});
	}

	@Test
	public void testPulseBomb() {
		// Test excess on adjacents
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion boulderfist1 = playMinionCard(context, opponent, "minion_boulderfist_ogre");
			Minion bloodfen = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			Minion boulderfist2 = playMinionCard(context, opponent, "minion_boulderfist_ogre");
			context.endTurn();
			playCardWithTarget(context, player, "spell_pulse_bomb", bloodfen);
			Assert.assertTrue(bloodfen.isDestroyed());
			// Up to 18 damage rule
			Assert.assertEquals(boulderfist1.getHp(), boulderfist1.getBaseHp() - 10 + bloodfen.getBaseHp());
			Assert.assertEquals(boulderfist2.getHp(), boulderfist2.getBaseHp() - 10 + bloodfen.getBaseHp());
		});

		// Test excess in event of divine shield using Explosive Runes rules
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion boulderfist1 = playMinionCard(context, opponent, "minion_boulderfist_ogre");
			Minion bloodfen = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			Minion boulderfist2 = playMinionCard(context, opponent, "minion_boulderfist_ogre");
			bloodfen.setAttribute(Attribute.DIVINE_SHIELD);
			context.endTurn();
			playCardWithTarget(context, player, "spell_pulse_bomb", bloodfen);
			Assert.assertFalse(bloodfen.isDestroyed());
			Assert.assertEquals(bloodfen.getHp(), bloodfen.getBaseHp());
			// Up to 18 damage rule
			Assert.assertEquals(boulderfist1.getHp(), boulderfist1.getBaseHp() - 10 + bloodfen.getBaseHp());
			Assert.assertEquals(boulderfist2.getHp(), boulderfist2.getBaseHp() - 10 + bloodfen.getBaseHp());
		});
	}

	@Test
	public void testArmaggedonVanguardBolfRamshieldInteraction() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_armageddon_vanguard");
			playCard(context, player, "minion_bolf_ramshield");
			context.endTurn();
			playCard(context, opponent, "minion_armageddon_vanguard");
			playCard(context, opponent, "minion_bolf_ramshield");
			context.endTurn();
			playCard(context, player, "spell_arcane_missiles");
		});
	}

	@Test
	public void testTerrorscaleStalkerBlinkDogInteraction() {
		runGym((context, player, opponent) -> {
			// Deathrattle: Give a random friendly Beast \"Deathrattle: Summon a Blink Dog\"
			Minion blinkDog = playMinionCard(context, player, "minion_blink_dog");
			playCard(context, player, "minion_terrorscale_stalker");
			// Now Blink Dog summons a blink dog and gives a randomly friendly beast an extra deathrattle
			playCardWithTarget(context, player, "spell_fireball", blinkDog);
			Assert.assertEquals(player.getMinions().stream().filter(m -> m.getSourceCard().getCardId().equals("minion_blink_dog")).count(), 1L);
		});
	}

	@Test
	public void testThinkFast() {
		runGym((context, player, opponent) -> {
			// TODO: This should still work if it's a different class
			playCard(context, player, "spell_mirror_image");
			int[] cost = new int[1];
			overrideDiscover(player, actions -> {
				cost[0] = actions.get(0).getCard().getBaseManaCost();
				return actions.get(0);
			});
			playCard(context, player, "spell_think_fast");
			Assert.assertEquals(costOf(context, player, player.getHand().get(0)), cost[0] - 1);
			context.endTurn();
			context.endTurn();
			Assert.assertEquals(costOf(context, player, player.getHand().get(0)), cost[0]);
		}, HeroClass.BLACK, HeroClass.BLACK);
	}

	@Test
	public void testDejaVu() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "spell_deja_vu");
			Assert.assertEquals(player.getMinions().size(), 2);
			Assert.assertTrue(player.getHand().stream().allMatch(c -> costOf(context, player, c) == 1));
			playCard(context, player, player.getHand().get(1));
			playCard(context, player, player.getHand().get(0));
			for (int i = 2; i < 4; i++) {
				Assert.assertEquals(player.getMinions().get(i).getAttack(), 1);
				Assert.assertEquals(player.getMinions().get(i).getHp(), 1);
			}
		});
	}

	@Test
	public void testForeverAStudent() {
		runGym((context, player, opponent) -> {
			Minion bloodfen = playMinionCard(context, player, "minion_bloodfen_raptor");
			playCardWithTarget(context, player, "spell_forever_a_student", bloodfen);
			Minion bloodfen2 = playMinionCard(context, player, "minion_bloodfen_raptor");
			Assert.assertEquals(bloodfen.getAttack(), bloodfen.getBaseAttack() + 1);
			Assert.assertEquals(bloodfen.getHp(), bloodfen.getBaseHp() + 1);
			Assert.assertEquals(bloodfen2.getAttack(), bloodfen2.getBaseAttack(), "The newly summoned minion should not be the benefit of the buff.");
			Assert.assertEquals(bloodfen2.getHp(), bloodfen2.getBaseHp());
			context.endTurn();
			playCard(context, opponent, "minion_bloodfen_raptor");
			Assert.assertEquals(bloodfen.getAttack(), bloodfen.getBaseAttack() + 1, "Opponent summoning a minion should not affect the stats of the enchanted minion.");
			Assert.assertEquals(bloodfen.getHp(), bloodfen.getBaseHp() + 1);
		});
	}

	@Test
	public void testNickOfTime() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			shuffleToDeck(context, player, "minion_nick_of_time");
			context.endTurn();
			Assert.assertEquals(player.getMinions().stream().map(Minion::getSourceCard).map(Card::getCardId).filter(cid -> cid.equals("token_silver_hand_recruit")).count(), 2L);
		});
	}

	@Test
	public void testAwakenTheAncients() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_awaken_the_ancients");
			player.setMaxMana(10);
			player.setMana(10);
			playCard(context, player, "minion_bloodfen_raptor");
			Assert.assertEquals(player.getMana(), 10);
			playCard(context, player, "minion_bloodfen_raptor");
			Assert.assertEquals(player.getMana(), 8);
		});
	}

	@Test
	public void testAcceleratedGrowth() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_bloodfen_raptor");
			shuffleToDeck(context, opponent, "minion_bloodfen_raptor");
			playCard(context, player, "spell_accelerated_growth");
			Assert.assertEquals(player.getHand().get(0).getCardId(), "minion_bloodfen_raptor");
			Assert.assertEquals(opponent.getHand().get(0).getCardId(), "minion_bloodfen_raptor", "Testing the TargetPlayer.BOTH attribute on DrawCardSpell");
		});
	}

	@Test
	public void testMysticSkull() {
		runGym((context, player, opponent) -> {
			Minion bloodfenRaptor = playMinionCard(context, player, "minion_bloodfen_raptor");
			playCardWithTarget(context, player, "spell_mystic_skull", bloodfenRaptor);
			Assert.assertEquals(player.getHand().get(0).getCardId(), "minion_bloodfen_raptor");
			Minion newBloodfenRaptor = playMinionCard(context, player, (MinionCard) player.getHand().get(0));
			Assert.assertEquals(newBloodfenRaptor.getAttack(), 5);
		});
	}

	@Test
	public void testGiantDisappointment() {
		runGym((context, player, opponent) -> {
			Card card = CardCatalogue.getCardById("minion_giant_disappointment");
			context.getLogic().receiveCard(player.getId(), card);
			Assert.assertEquals(costOf(context, player, card), 8);
		});
	}

	@Test
	public void testQuestGiver() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_bloodfen_raptor");
			playMinionCard(context, player, "minion_quest_giver");
			Assert.assertEquals(player.getDeck().get(0).getCardId(), "minion_bloodfen_raptor");
			Assert.assertEquals(player.getMinions().size(), 1);
			Assert.assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_quest_giver");
			context.endTurn();
			context.endTurn();
			Minion newBloodfen = playMinionCard(context, player, (MinionCard) player.getHand().get(0));
			Assert.assertEquals(newBloodfen.getAttack(), 6);
			Assert.assertEquals(newBloodfen.getHp(), 5);
		});
	}

	@Test
	public void testPowerTrip() {
		// We reach turn 10 so we have 10 mana, we die
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_power_trip");
			Assert.assertEquals(player.getQuests().get(0).getSourceCard().getCardId(), "spell_power_trip");
			for (int i = 0; i < 10; i++) {
				context.endTurn();
				context.endTurn();
			}
			Assert.assertTrue(context.getLogic().getMatchResult(player, opponent) != GameStatus.RUNNING);
		});

		// Our opponent gives us 10 mana somehow, we die
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_power_trip");
			Assert.assertEquals(player.getQuests().get(0).getSourceCard().getCardId(), "spell_power_trip");
			for (int i = 0; i < 2; i++) {
				context.endTurn();
				context.endTurn();
			}
			context.endTurn();
			Assert.assertEquals(player.getMaxMana(), 3);
			for (int i = 0; i < 7; i++) {
				playCard(context, opponent, "minion_arcane_golem");
				Assert.assertEquals(player.getMaxMana(), 3 + i + 1);
			}
			Assert.assertEquals(player.getMaxMana(), 10);
			Assert.assertTrue(context.getLogic().getMatchResult(player, opponent) != GameStatus.RUNNING);
		});

		// Check that minions have +1/+1
		runGym((context, player, opponent) -> {
			Minion bloodfenRaptor = playMinionCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "spell_power_trip");
			Assert.assertEquals(bloodfenRaptor.getAttack(), bloodfenRaptor.getBaseAttack() + 1);
			Assert.assertEquals(bloodfenRaptor.getHp(), bloodfenRaptor.getBaseHp() + 1);
			context.endTurn();
			Minion opponentMinion = playMinionCard(context, player, "minion_chillwind_yeti");
			context.endTurn();
			playCardWithTarget(context, player, "spell_mind_control", opponentMinion);
			Assert.assertEquals(opponentMinion.getAttack(), opponentMinion.getBaseAttack() + 1);
			Assert.assertEquals(opponentMinion.getHp(), opponentMinion.getBaseHp() + 1);
		});

		// Check that Saronite Chain Gang has correct stats
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_power_trip");
			playCard(context, player, "minion_saronite_chain_gang");
			player.getMinions().forEach(m -> Assert.assertEquals(m.getAttack(), 3));
			player.getMinions().forEach(m -> Assert.assertEquals(m.getHp(), 4));
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_saronite_chain_gang");
			playCard(context, player, "spell_power_trip");
			player.getMinions().forEach(m -> Assert.assertEquals(m.getAttack(), 3));
			player.getMinions().forEach(m -> Assert.assertEquals(m.getHp(), 4));
		});
	}

	@Test
	public void testDancemistress() {
		// When this minion is healed, check if Crazed Dancer is summoned
		runGym((context, player, opponent) -> {
			Minion dancemistress = playMinionCard(context, player, "minion_dancemistress");
			context.endTurn();
			// Damages minions by 1
			playCard(context, opponent, "spell_arcane_explosion");
			context.endTurn();
			// Heals the dancemistress Minion
			playCardWithTarget(context, player, "spell_ancestral_healing", dancemistress);
			Assert.assertEquals(player.getMinions().get(1).getSourceCard().getCardId(), "minion_crazed_dancer");
			// Check if the Crazed Dancer has attack and hp of 2
			Assert.assertEquals(player.getMinions().get(1).getBaseAttack(), 2);
			Assert.assertEquals(player.getMinions().get(1).getBaseHp(), 2);
		});

		// When a different minion is healed, Crazed Dancer is NOT summoned
		runGym((context, player, opponent) -> {
			Minion dancemistress = playMinionCard(context, player, "minion_dancemistress");
			Minion bloodfenRaptor = playMinionCard(context, player, "minion_bloodfen_raptor");
			context.endTurn();
			// Damages minions by 1
			playCard(context, opponent, "spell_arcane_explosion");
			context.endTurn();
			// Heals the dancemistress Minion
			playCardWithTarget(context, player, "spell_ancestral_healing", bloodfenRaptor);
			Assert.assertFalse(player.getMinions().stream().anyMatch(m -> m.getSourceCard().getCardId().equals("minion_crazed_dancer")));
		});
	}

	@Test
	public void testSpikeToedBooterang() {
		// Attacks a opponent's minion twice
		runGym((context, player, opponent) -> {
			Minion riverCrocolisk = playMinionCard(context, opponent, "minion_river_crocolisk");
			context.endTurn();
			playCardWithTarget(context, player, "spell_spike_toed_booterang", riverCrocolisk);
			Assert.assertEquals(opponent.getMinions().get(0).getHp(), 1);
		});

		// Attacks player's minion twice
		runGym((context, player, opponent) -> {
			Minion riverCrocolisk = playMinionCard(context, player, "minion_river_crocolisk");
			playCardWithTarget(context, player, "spell_spike_toed_booterang", riverCrocolisk);
			Assert.assertEquals(player.getMinions().get(0).getHp(), 1);
		});

		// Defeats a Divine Shield
		runGym((context, player, opponent) -> {
			Minion silvermoonGuardian = playMinionCard(context, opponent, "minion_silvermoon_guardian");
			context.endTurn();
			playCardWithTarget(context, player, "spell_spike_toed_booterang", silvermoonGuardian);
			Assert.assertEquals(opponent.getMinions().get(0).getHp(), 2);
		});

		// If attacking Imp Gang Boss, summons two 1/1 Imps for opponent
		runGym((context, player, opponent) -> {
			Minion impGangBoss = playMinionCard(context, opponent, "minion_imp_gang_boss");
			context.endTurn();
			playCardWithTarget(context, player, "spell_spike_toed_booterang", impGangBoss);
			Assert.assertEquals(opponent.getMinions().get(1).getSourceCard().getCardId(), "token_imp");
			Assert.assertEquals(opponent.getMinions().get(2).getSourceCard().getCardId(), "token_imp");
		});
	}

	@Test
	public void testStablePortal() {
		// Correctly adds a Beast to player's hand with a mana cost 2 less
		runGym((context, player, opponent) -> {
			GameLogic spiedLogic = Mockito.spy(context.getLogic());
			context.setLogic(spiedLogic);

			Mockito.doAnswer(invocation ->
					CardCatalogue.getCardById("minion_malorne"))
					.when(spiedLogic)
					.removeRandom(Mockito.anyList());

			playCard(context, player, "spell_stable_portal");
			Card card = player.getHand().get(0);
			Assert.assertEquals(card.getCardId(), "minion_malorne");
			int baseMana = card.getBaseManaCost();
			Assert.assertEquals(baseMana, 7);
			Assert.assertEquals(card.getRace(), Race.BEAST);
			Assert.assertEquals(costOf(context, player, card), baseMana - 2);
		});
	}

	@Test
	public void testWyrmrestSniper() {
		// Friendly Dragon survives damage so 3 damage is dealt to the opponent hero
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_sleepy_dragon");
			Minion wyrmrest = playMinionCard(context, player, "minion_wyrmrest_sniper");
			context.endTurn();
			int opponentHp = opponent.getHero().getHp();
			// Damages minions by 1
			playCard(context, opponent, "spell_arcane_explosion");
			Assert.assertEquals(opponent.getHero().getHp(), opponentHp - 3);
			Assert.assertFalse(wyrmrest.hasAttribute(Attribute.STEALTH));
		});

		// Friendly Dragon does not survive damage, no damage is dealt
		runGym((context, player, opponent) -> {
			Minion minion = playMinionCard(context, player, "minion_sleepy_dragon");
			// Set hp to 1 so it dies
			minion.setHp(1);
			Minion wyrmrest = playMinionCard(context, player, "minion_wyrmrest_sniper");
			context.endTurn();
			int opponentHp = opponent.getHero().getHp();
			// Damages minions by 1
			playCard(context, opponent, "spell_arcane_explosion");
			Assert.assertTrue(minion.isDestroyed());
			Assert.assertEquals(opponent.getHero().getHp(), opponentHp, "Opponent's HP should not have changed.");
			Assert.assertTrue(wyrmrest.hasAttribute(Attribute.STEALTH));
		});

		// Enemy Dragon survives damage, no damage is dealt to the opponent's hero
		runGym((context, player, opponent) -> {
			Minion wyrmrest = playMinionCard(context, player, "minion_wyrmrest_sniper");
			context.endTurn();
			int opponentHp = opponent.getHero().getHp();
			Minion minion = playMinionCard(context, opponent, "minion_sleepy_dragon");

			// Damages minions by 1
			context.endTurn();
			playCard(context, player, "spell_arcane_explosion");
			Assert.assertFalse(minion.isDestroyed());
			Assert.assertEquals(minion.getHp(), minion.getBaseHp() - 1);
			Assert.assertEquals(opponent.getHero().getHp(), opponentHp, "Opponent's HP should not have changed.");
			Assert.assertTrue(wyrmrest.hasAttribute(Attribute.STEALTH));
		});
	}
}
