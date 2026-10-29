package com.hiddenswitch.spellsource.tests.cards;

import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.trigger.Opener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.CONCURRENT)
public class SpellsourceLegendaryAuditTests extends TestBase {

	@Test
	public void testFaeLadyCaroline() {
		// Guard. Opener: Draw. Aftermath: Shuffle this into your deck
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_neutral_test");
			assertEquals(0, player.getHand().size());
			assertEquals(1, player.getDeck().size());

			Minion caroline = playMinionCard(context, player, "minion_fae_wraith_caroline");
			assertTrue(caroline.hasAttribute(Attribute.TAUNT), "Fae-Lady Caroline should have Guard (TAUNT)");
			assertEquals(1, player.getHand().size(), "Opener should draw a card");
			assertEquals(0, player.getDeck().size());

			destroy(context, caroline);
			assertEquals(1, player.getDeck().size(), "Aftermath should shuffle Caroline back into deck");
			assertEquals("minion_fae_wraith_caroline", player.getDeck().get(0).getCardId());
		});
	}

	@Test
	public void testThrakdosTheHollow() {
		// Guard. Opener: Your Elementals cost (1) during your next turn.
		runGym((context, player, opponent) -> {
			Card elemental = receiveCard(context, player, "minion_alemental");
			Card nonElemental = receiveCard(context, player, "minion_fae_wraith_caroline");
			Card spell = receiveCard(context, player, "spell_lunstone");

			Minion thrakdos = playMinionCard(context, player, "minion_thrakdos_the_hollow");
			assertTrue(thrakdos.hasAttribute(Attribute.TAUNT));

			// Same turn: elemental cost should NOT be 1 yet
			assertNotEquals(1, costOf(context, player, elemental));

			// Opponent turn, then player's next turn
			context.endTurn();
			context.endTurn();

			assertEquals(1, costOf(context, player, elemental), "Elemental should cost (1) during next turn");
			assertEquals(6, costOf(context, player, nonElemental), "Non-elemental minion should not be discounted");
			assertEquals(0, costOf(context, player, spell), "Spell should not be set to cost (1)");

			// After next turn ends, cost returns to normal
			context.endTurn();
			context.endTurn();
			assertEquals(3, costOf(context, player, elemental));
		});
	}

	@Test
	public void testDreamOfAnarchy() {
		// Summon every unit in both players' hands, then force them all to attack other random units.
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_fae_wraith_caroline"); // 4/6
			receiveCard(context, player, "spell_lunstone");
			receiveCard(context, opponent, "minion_neutral_test"); // 2/2

			playCard(context, player, "spell_dream_of_anarchy");

			// Minions should have been recruited from both hands and fought each other
			assertEquals(1, player.getHand().size(), "Only the spell should remain in player's hand");
			assertEquals("spell_lunstone", player.getHand().get(0).getCardId());
			assertEquals(0, opponent.getHand().size(), "Minion in opponent's hand should be summoned");

			// 4/6 Caroline attacks 2/2 neutral_test -> neutral_test dies, Caroline survives at 4/4
			assertEquals(1, player.getMinions().size());
			assertEquals(0, opponent.getMinions().size());
			assertEquals(4, player.getMinions().get(0).getHp());
			assertFalse(player.getMinions().get(0).hasAttribute(Attribute.RESERVED_BOOLEAN_3));
		});
	}

	@Test
	public void testAlagardsInfusion() {
		// Give all units in your hand and deck "Opener: Cast a random Storyteller spell".
		runGym((context, player, opponent) -> {
			Card handMinion = receiveCard(context, player, "minion_neutral_test");
			Card deckMinion = shuffleToDeck(context, player, "minion_neutral_test");

			// Should be playable even with NO spells in hand!
			playCard(context, player, "spell_alagards_infusion");

			assertTrue(context.getLogic().getActiveTriggers(handMinion.getReference()).stream().anyMatch(t -> t instanceof Opener),
					"Hand minion should gain an Opener enchantment");
			assertTrue(context.getLogic().getActiveTriggers(deckMinion.getReference()).stream().anyMatch(t -> t instanceof Opener),
					"Deck minion should gain an Opener enchantment");
		});
	}

	@Test
	public void testDoroTheRebuilder() {
		// Whenever another allied Elemental dies, return it to your hand instead.
		runGym((context, player, opponent) -> {
			Minion doro = playMinionCard(context, player, "minion_alemental");
			Minion otherElemental = playMinionCard(context, player, "minion_thrakdos_the_hollow");

			destroy(context, otherElemental);
			assertEquals(1, player.getHand().size(), "Other allied Elemental should return to hand when it dies");
			assertEquals("minion_thrakdos_the_hollow", player.getHand().get(0).getCardId());

			destroy(context, doro);
			assertEquals(1, player.getHand().size(), "Doro the Rebuilder should NOT return itself to hand when it dies");
		});
	}

	@Test
	public void testExploreTheEndlessSkies() {
		// Replace your hand and deck with random Legendary units. They cost (1) less.
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 4; i++) {
				receiveCard(context, player, "spell_lunstone");
				shuffleToDeck(context, player, "spell_lunstone");
			}

			playCard(context, player, "spell_explore_the_endless_skies");

			assertEquals(4, player.getHand().size());
			assertEquals(4, player.getDeck().size());

			for (Card c : player.getHand()) {
				assertEquals(CardType.MINION, c.getCardType(), "Replaced hand card must be a unit (MINION): " + c.getCardId());
				if (c.getDesc().getManaCostModifier() == null) {
					assertEquals(Math.max(0, c.getBaseManaCost() - 1), costOf(context, player, c));
				}
			}
			for (Card c : player.getDeck()) {
				assertEquals(CardType.MINION, c.getCardType(), "Replaced deck card must be a unit (MINION): " + c.getCardId());
				if (c.getDesc().getManaCostModifier() == null) {
					assertEquals(Math.max(0, c.getBaseManaCost() - 1), costOf(context, player, c));
				}
			}
		});
	}

	@Test
	public void testShaklatalCosmosScion() {
		// Opener: Double the Attack and Health of all units in your hand.
		runGym((context, player, opponent) -> {
			Card minionCard = receiveCard(context, player, "minion_fae_wraith_caroline"); // 4/6
			Card weaponCard = receiveCard(context, player, "weapon_fassnu_axe");

			playMinionCard(context, player, "minion_shaklatal_cosmos_scion");

			assertEquals(4, minionCard.getBonusAttack(), "4/6 minion in hand should gain +4 Attack");
			assertEquals(6, minionCard.getBonusHp(), "4/6 minion in hand should gain +6 Health");
			assertEquals(0, weaponCard.getBonusAttack(), "Weapon in hand should NOT be buffed");
			assertEquals(0, weaponCard.getBonusHp(), "Weapon in hand should NOT be buffed");

			Minion caroline = playMinionCard(context, player, minionCard);
			assertEquals(8, caroline.getAttack());
			assertEquals(12, caroline.getHp());
		});
	}

	@Test
	public void testWarlordKalithreshAndOceanDepths() {
		// Guard. Opener: Shuffle 7 'Ocean Depths' into your deck. Draw 2.
		// Ocean Depths: Casts When Drawn. Restore #2 Health to your champion.
		runGym((context, player, opponent) -> {
			player.getHero().setHp(15);
			Minion kalithresh = playMinionCard(context, player, "minion_warlord_kalithresh");
			assertTrue(kalithresh.hasAttribute(Attribute.TAUNT));

			// Since deck was empty, shuffling 7 Ocean Depths and drawing 2 should cast all 7 Ocean Depths when drawn
			assertEquals(29, player.getHero().getHp(), "All 7 Ocean Depths should cast when drawn, healing 14 HP total");
			assertEquals(0, player.getHand().size(), "Ocean Depths should not remain in hand");
		});
	}

	@Test
	public void testDeaCrystalIllusionist() {
		// Opener: Choose a unit. Receive a copy of it. Set its stats and Cost to 0.
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_fae_wraith_caroline");
			playCard(context, player, "minion_dea", target);
			assertEquals(1, player.getHand().size());
			Card copy = player.getHand().get(0);
			assertEquals("minion_fae_wraith_caroline", copy.getCardId());
			assertEquals(0, costOf(context, player, copy));
			assertEquals(0, copy.getAttack());
			assertEquals(0, copy.getHp());
		});
	}

	@Test
	public void testDekkaThePrimal() {
		// After this attacks and kills a unit, gain its Attack, Health, and card text.
		runGym((context, player, opponent) -> {
			Minion dekka = playMinionCard(context, player, "minion_dekka"); // 4/4
			context.endTurn();
			Minion victim = playMinionCard(context, opponent, "minion_monarch_barata"); // 2/6 with Aftermath
			victim.setHp(2);
			context.endTurn();

			attack(context, player, dekka, victim);
			assertTrue(victim.isDestroyed());
			// Dekka took 2 damage (4/2), then gained victim's 2 Attack and 6 max Health -> 6/8
			assertEquals(6, dekka.getAttack());
			assertEquals(8, dekka.getHp());
			assertEquals(10, dekka.getMaxHp());
		});
	}

	@Test
	public void testHerculesTheTitan() {
		// Dash, Guard. Opener: Gain Health equal to your Armor.
		runGym((context, player, opponent) -> {
			player.getHero().setAttribute(Attribute.ARMOR, 6);
			Minion hercules = playMinionCard(context, player, "minion_hercules");
			assertTrue(hercules.hasAttribute(Attribute.RUSH));
			assertTrue(hercules.hasAttribute(Attribute.TAUNT));
			assertEquals(4, hercules.getAttack());
			assertEquals(7, hercules.getHp());
		});
	}

	@Test
	public void testMonarchBarata() {
		// Aftermath: Give a random unit in your hand +1/+3 and this Aftermath.
		runGym((context, player, opponent) -> {
			Card handUnit1 = receiveCard(context, player, "minion_neutral_test"); // 2/2
			Minion barata = playMinionCard(context, player, "minion_monarch_barata");
			destroy(context, barata);

			assertEquals(1, handUnit1.getBonusAttack());
			assertEquals(3, handUnit1.getBonusHp());

			Minion unit1 = playMinionCard(context, player, handUnit1);
			assertEquals(3, unit1.getAttack());
			assertEquals(5, unit1.getHp());

			Card handUnit2 = receiveCard(context, player, "minion_neutral_test");
			destroy(context, unit1);
			assertEquals(1, handUnit2.getBonusAttack(), "Transferred Aftermath should buff the next unit in hand by +1/+3");
			assertEquals(3, handUnit2.getBonusHp());
		});
	}

	@Test
	public void testMorrisTheWorm() {
		// Opener: Infect an allied unit, giving it +2/+2. When it dies, infect another random one.
		runGym((context, player, opponent) -> {
			Minion host1 = playMinionCard(context, player, "minion_neutral_test"); // 2/2
			Minion host2 = playMinionCard(context, player, "minion_neutral_test"); // 2/2
			playCard(context, player, "minion_morris", host1);
			Minion morris = player.getMinions().get(2);
			destroy(context, morris);

			assertEquals(4, host1.getAttack());
			assertEquals(4, host1.getHp());

			destroy(context, host1);
			assertEquals(4, host2.getAttack(), "When infected host1 dies, host2 should gain +2/+2");
			assertEquals(4, host2.getHp());

			Minion host3 = playMinionCard(context, player, "minion_neutral_test"); // 2/2
			destroy(context, host2);
			assertEquals(4, host3.getAttack(), "When infected host2 dies, host3 should also gain +2/+2 from transferred infection");
			assertEquals(4, host3.getHp());
		});
	}

	@Test
	public void testQueenScolia() {
		// Whenever you use your Skill, receive a 1/1 Wasp with Blitz and Toxic.
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_queen_beetrice");
			player.setMana(2);
			context.getLogic().performGameAction(player.getId(), player.getHeroPowerZone().get(0).play());
			assertTrue(player.getHand().stream().anyMatch(c -> "token_bee_soldier".equals(c.getCardId())));
		});
	}

	@Test
	public void testTeralaDraconicMistress() {
		// Opener: Transforms the units in your hand into random Dragons.
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_neutral_test");
			receiveCard(context, player, "spell_lunstone");
			playMinionCard(context, player, "minion_terala");
			assertEquals(2, player.getHand().size());
			assertEquals("DRAGON", player.getHand().get(0).getRace());
			assertEquals("spell_lunstone", player.getHand().get(1).getCardId());
		});
	}

	@Test
	public void testVermancerProphet() {
		// Opener: Source two units that died this game.
		runGym((context, player, opponent) -> {
			Minion dead1 = playMinionCard(context, player, "minion_neutral_test");
			Minion dead2 = playMinionCard(context, opponent, "minion_fae_wraith_caroline");
			destroy(context, dead1);
			destroy(context, dead2);

			playMinionCard(context, player, "minion_vermancer_prophet");
			assertEquals(2, player.getHand().size(), "Should source 2 units that died this game");
		});
	}

	@Test
	public void testHeroHerculesAndNarvina() {
		// Hero Hercules: Guard. Hero Narvina: Opener: Return an enemy unit to your opponent's hand.
		runGym((context, player, opponent) -> {
			playCard(context, player, "hero_hercules");
			assertTrue(player.getHero().hasAttribute(Attribute.TAUNT));
			assertEquals(30, player.getHero().getArmor());

			context.endTurn();
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test");
			context.endTurn();

			playCard(context, player, "hero_narvina", enemy);
			assertEquals(0, opponent.getMinions().size());
			assertEquals(1, opponent.getHand().size());
		});
	}

	@Test
	public void testDreamOfKinship() {
		// Source a Beast, a Dragon, and a Fae. Summon them all.
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_dream_of_kinship");
			assertEquals(3, player.getMinions().size());
			assertTrue(player.getMinions().stream().anyMatch(m -> "BEAST".equals(m.getRace())));
			assertTrue(player.getMinions().stream().anyMatch(m -> "DRAGON".equals(m.getRace())));
			assertTrue(player.getMinions().stream().anyMatch(m -> "FAE".equals(m.getRace())));
		});
	}

	@Test
	public void testTheAmberGuardian() {
		// Guard. Opener: If your hand contains 9 cards, destroy all other units.
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_neutral_test");
			context.endTurn();
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test");
			context.endTurn();

			for (int i = 0; i < 9; i++) {
				receiveCard(context, player, "spell_lunstone");
			}
			Card guardianCard = receiveCard(context, player, "minion_the_amber_guardian");
			Minion guardian = playMinionCard(context, player, guardianCard);
			assertTrue(guardian.hasAttribute(Attribute.TAUNT));
			assertTrue(ally.isDestroyed());
			assertTrue(enemy.isDestroyed());
			assertFalse(guardian.isDestroyed());
		});
	}

	@Test
	public void testTheLivingNest() {
		// Guard. Aftermath: Summon Arido and 4 Larva.
		runGym((context, player, opponent) -> {
			Minion nest = playMinionCard(context, player, "minion_the_living_nest");
			assertTrue(nest.hasAttribute(Attribute.TAUNT));
			destroy(context, nest);
			assertEquals(5, player.getMinions().size());
			assertEquals(1, player.getMinions().stream().filter(m -> "token_bunta".equals(m.getSourceCard().getCardId())).count());
			assertEquals(4, player.getMinions().stream().filter(m -> "token_spiderling".equals(m.getSourceCard().getCardId())).count());
		});
	}

	@Test
	public void testStormbringerKurt() {
		// Whenever you cast a spell, summon a random Elemental.
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_stormbringer_kurt");
			playCard(context, player, "spell_lunstone");
			assertEquals(2, player.getMinions().size());
			assertEquals("ELEMENTAL", player.getMinions().get(1).getRace());
		});
	}

	@Test
	public void testAvatarIar() {
		// Opener: If your deck has no duplicates, gain Blitz, Elusive, Dodge, and Guard.
		runGym((context, player, opponent) -> {
			Minion iar = playMinionCard(context, player, "minion_avatar_iar");
			assertTrue(iar.hasAttribute(Attribute.CHARGE));
			assertTrue(iar.hasAttribute(Attribute.UNTARGETABLE_BY_SPELLS));
			assertTrue(iar.hasAttribute(Attribute.DIVINE_SHIELD));
			assertTrue(iar.hasAttribute(Attribute.TAUNT));
		});
	}

	@Test
	public void testSourcespearIgnitius() {
		// Aftermath: Destroy all units. Upgrade your Skill.
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "weapon_sourcespear_ignitius");
			attack(context, player, player.getHero(), opponent.getHero());
			assertTrue(ally.isDestroyed());
			assertEquals("hero_power_ascend", player.getHeroPowerZone().get(0).getCardId());
		});
	}

	@Test
	public void testSybilTheAbandoned() {
		// Guard. Aftermath: Put 10 copies of "The Stars are Falling" on the bottom of your deck.
		runGym((context, player, opponent) -> {
			Minion sybil = playMinionCard(context, player, "minion_sybil_the_abandoned");
			assertTrue(sybil.hasAttribute(Attribute.TAUNT));
			destroy(context, sybil);
			assertEquals(10, player.getDeck().size());
			assertTrue(player.getDeck().stream().allMatch(c -> "token_the_stars_are_falling".equals(c.getCardId())));
		});
	}

	@Test
	public void testDreamOfAscendance() {
		// For the rest of the game, your Soar spells turn into Firebolts that also deal $2 damage.
		runGym((context, player, opponent) -> {
			Card soar = receiveCard(context, player, "spell_soar");
			shuffleToDeck(context, player, "minion_neutral_test");
			playCard(context, player, "spell_dream_of_ascendance");

			int oppHp = opponent.getHero().getHp();
			playCard(context, player, soar, opponent.getHero());
			assertEquals(oppHp - 2, opponent.getHero().getHp());
			assertEquals(1, player.getHand().size(), "Firebolt should also draw a card");
		});
	}

	@Test
	public void testIbarakiDoji() {
		// Extra Strike. After this attacks, your opponent draws.
		runGym((context, player, opponent) -> {
			Minion ibaraki = playMinionCard(context, player, "minion_ibaraki_doji");
			assertTrue(ibaraki.hasAttribute(Attribute.WINDFURY));
			context.endTurn();
			context.endTurn();

			shuffleToDeck(context, opponent, "minion_neutral_test");
			int oppHand = opponent.getHand().size();
			attack(context, player, ibaraki, opponent.getHero());
			assertEquals(oppHand + 1, opponent.getHand().size());
		});
	}

	@Test
	public void testThe4thRingWarden() {
		// Opener: If your deck has no duplicates, units you play this game have "Aftermath: Summon a 2/2 Lesser Oni".
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_the_4th_ring_warden");
			Minion unit = playMinionCard(context, player, "minion_neutral_test");
			destroy(context, unit);
			assertTrue(player.getMinions().stream().anyMatch(m -> "token_lesser_oni".equals(m.getSourceCard().getCardId())));
		});
	}

	@Test
	public void testFumewalkerZin() {
		// Whenever an allied Demon dies, gain Dodge and Hidden.
		runGym((context, player, opponent) -> {
			Minion zin = playMinionCard(context, player, "minion_fumewalker_zin");
			Minion demon = playMinionCard(context, player, "minion_ibaraki_doji");
			destroy(context, demon);
			assertTrue(zin.hasAttribute(Attribute.DIVINE_SHIELD));
			assertTrue(zin.hasAttribute(Attribute.STEALTH));
		});
	}

	@Test
	public void testKintoraTheHeir() {
		// Opener: Your Lesser Onis are 4/4 and take double damage this game.
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_kintora_the_heir");
			Minion oni = playMinionCard(context, player, "token_lesser_oni");
			assertEquals(4, oni.getAttack());
			assertEquals(4, oni.getHp());
			assertTrue(oni.hasAttribute(Attribute.AURA_TAKE_DOUBLE_DAMAGE));
		});
	}

	@Test
	public void testShutenDoji() {
		// Whenever your opponent draws, they also draw a Fatigue card.
		runGym((context, player, opponent) -> {
			opponent.getAttributes().remove(Attribute.DISABLE_FATIGUE);
			shuffleToDeck(context, opponent, "minion_neutral_test");
			playMinionCard(context, player, "minion_shuten_doji");
			int oppHp = opponent.getHero().getHp();
			context.getLogic().drawCard(opponent.getId(), null);
			assertEquals(oppHp - 1, opponent.getHero().getHp());
		});
	}

	@Test
	public void testDreamOfConquest() {
		// Deal $2 damage to all enemy units. Upgrade your Skill.
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion enemy = playMinionCard(context, opponent, "minion_fae_wraith_caroline"); // 4/6
			context.endTurn();

			playCard(context, player, "spell_dream_of_conquest");
			assertEquals(4, enemy.getHp());
		});
	}

	@Test
	public void testGeneralYokura() {
		// Guard. Opener: Gain +1/+1 for each ally that died this turn, then attack the enemy champion.
		runGym((context, player, opponent) -> {
			Minion ally1 = playMinionCard(context, player, "minion_neutral_test");
			Minion ally2 = playMinionCard(context, player, "minion_neutral_test");
			destroy(context, ally1);
			destroy(context, ally2);

			int oppHp = opponent.getHero().getHp();
			Minion yokura = playMinionCard(context, player, "minion_general_yokura"); // 3/6 + 2/2 = 5/8
			assertTrue(yokura.hasAttribute(Attribute.TAUNT));
			assertEquals(5, yokura.getAttack());
			assertEquals(8, yokura.getHp());
			assertEquals(oppHp - 5, opponent.getHero().getHp());
		});
	}

	@Test
	public void testVaszTheSellsword() {
		// Opener: Deal 1 damage to all other units. Repeat until a unit dies.
		runGym((context, player, opponent) -> {
			Minion m1 = playMinionCard(context, player, "minion_neutral_test"); // 2/2
			context.endTurn();
			Minion m2 = playMinionCard(context, opponent, "minion_fae_wraith_caroline"); // 4/6
			context.endTurn();

			Minion vasz = playMinionCard(context, player, "minion_vasz_the_sellsword");
			assertTrue(m1.isDestroyed());
			assertEquals(4, m2.getHp(), "Should deal 2 damage total until the 2-HP unit dies");
			assertEquals(5, vasz.getHp());
		});
	}

	@Test
	public void testDreamOfFreedom() {
		// Both players summon random units from their deck until their board is full. Give your units Dash.
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_neutral_test");
			shuffleToDeck(context, opponent, "minion_neutral_test");
			playCard(context, player, "spell_dream_of_freedom");
			assertEquals(1, player.getMinions().size());
			assertEquals(1, opponent.getMinions().size());
			assertTrue(player.getMinions().get(0).hasAttribute(Attribute.RUSH));
		});
	}

	@Test
	public void testRupiTheGiving() {
		// Opener: If you've restored 15 Health this game, fill your board with 2/2 Frogs with Guard.
		runGym((context, player, opponent) -> {
			player.getHero().setHp(15);
			context.getLogic().heal(player, player.getHero(), 15, player.getHero());
			playMinionCard(context, player, "minion_rupi_the_giving");
			assertEquals(7, player.getMinions().size());
			assertEquals(6, player.getMinions().stream().filter(m -> "token_big_frog".equals(m.getSourceCard().getCardId())).count());
		});
	}

	@Test
	public void testHagalazTheWeaponsmith() {
		// Whenever you equip an item, give it +1/+1.
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_hagalaz_the_weaponsmith");
			playCard(context, player, "weapon_fassnu_axe"); // 3/2
			assertEquals(4, player.getWeaponZone().get(0).getAttack());
			assertEquals(3, player.getWeaponZone().get(0).getDurability());
		});
	}

	@Test
	public void testCadenzaGrandIllusionist() {
		// Opener: Summon two copies of this. They attack random enemies then die.
		runGym((context, player, opponent) -> {
			int oppHp = opponent.getHero().getHp();
			Minion cadenza = playMinionCard(context, player, "minion_cadenza_grand_illusionist");
			assertEquals(1, player.getMinions().size());
			assertEquals(cadenza, player.getMinions().get(0));
			assertEquals(oppHp - 4, opponent.getHero().getHp(), "Two 2/2 copies should attack enemy hero and die");
		});
	}

	@Test
	public void testMyraGrandArtist() {
		// At the end of your turns, summon a Spirit with stats equal to the number of spells you've cast this turn.
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_myra_grand_artist");
			playCard(context, player, "spell_lunstone");
			playCard(context, player, "spell_lunstone");
			context.endTurn();
			assertEquals(2, player.getMinions().size());
			Minion spirit = player.getMinions().get(1);
			assertEquals(2, spirit.getAttack());
			assertEquals(2, spirit.getHp());
		});
	}

	@Test
	public void testDominus() {
		// Opener: Discard your lowest-Cost card as many times as you want. Restore #5 Health to your champion for each.
		runGym((context, player, opponent) -> {
			player.getHero().setHp(20);
			receiveCard(context, player, "spell_lunstone");
			overrideDiscover(context, player, discoverActions -> discoverActions.stream()
					.filter(da -> "spell_dominus_discard".equals(da.getCard().getCardId()))
					.findFirst().orElse(discoverActions.get(0)));
			playMinionCard(context, player, "minion_dominus");
			assertEquals(0, player.getHand().size());
			assertEquals(25, player.getHero().getHp());
		});
	}

	@Test
	public void testGraveCarerYorn() {
		// Your Captives have +1/+1 and Guard. Opener: Summon two 2/2 Captives.
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_grave_carer_yorn");
			assertEquals(3, player.getMinions().size());
			long buffedCaptives = player.getMinions().stream()
					.filter(m -> "token_captive_overlord".equals(m.getSourceCard().getCardId()))
					.peek(m -> {
						assertEquals(3, m.getAttack());
						assertEquals(3, m.getHp());
						assertTrue(m.hasAttribute(Attribute.AURA_TAUNT));
					}).count();
			assertEquals(2, buffedCaptives);
		});
	}

	@Test
	public void testKaynTheCorrupter() {
		// Opener: Summon two 0/3 Source Mines as your opponent's left and right-most units.
		runGym((context, player, opponent) -> {
			context.endTurn();
			playMinionCard(context, opponent, "minion_neutral_test");
			context.endTurn();

			playMinionCard(context, player, "minion_kayn_the_corrupter");
			assertEquals(3, opponent.getMinions().size());
			assertEquals("token_source_mine", opponent.getMinions().get(0).getSourceCard().getCardId());
			assertEquals("token_source_mine", opponent.getMinions().get(2).getSourceCard().getCardId());
		});
	}

	@Test
	public void testLordHenryk() {
		// Opener: If your champion has 12 or less Health, deal 10 damage.
		runGym((context, player, opponent) -> {
			player.getHero().setHp(12);
			int oppHp = opponent.getHero().getHp();
			playCard(context, player, "minion_lord_henryk", opponent.getHero());
			assertEquals(oppHp - 10, opponent.getHero().getHp());
		});
	}

	@Test
	public void testSupremeLordOrnn() {
		// Opener: If you have no duplicates in your deck, your Underlings have +2/+2 this game.
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_supreme_lord_ornn");
			Minion underling = playMinionCard(context, player, "token_underling_overlord"); // 1/1 -> 3/3
			assertEquals(3, underling.getAttack());
			assertEquals(3, underling.getHp());
		});
	}

	@Test
	public void testZathahunBloodthirsty() {
		// Dash. Opener: Shuffle 3 Drains into your deck. Whenever this deals damage, gain that much Armor.
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion enemy = playMinionCard(context, opponent, "minion_fae_wraith_caroline"); // 4/6
			context.endTurn();

			Minion zathahun = playMinionCard(context, player, "minion_zathahun_bloodthirsty");
			assertTrue(zathahun.hasAttribute(Attribute.RUSH));
			assertEquals(3, player.getDeck().size());

			attack(context, player, zathahun, enemy);
			assertEquals(9, player.getHero().getArmor());
		});
	}

	@Test
	public void testRanaboshInMemoryAndSoulWarden() {
		// Ranabosh: Aftermath: Go Dormant. Trigger two allied Secrets to revive this.
		// Soul Warden: Opener: Summon 1/1 copies of your Spirits that died this turn.
		runGym((context, player, opponent) -> {
			Minion ranabosh = playMinionCard(context, player, "minion_ranabosh_in_memory"); // Spirit
			destroy(context, ranabosh);
			assertEquals(1, player.getMinions().size());
			assertEquals("permanent_memory_of_ranabosh", player.getMinions().get(0).getSourceCard().getCardId());

			playMinionCard(context, player, "minion_soul_warden");
			assertTrue(player.getMinions().stream().anyMatch(m -> "minion_ranabosh_in_memory".equals(m.getSourceCard().getCardId())
					&& m.getAttack() == 1 && m.getHp() == 1));
		});
	}

	@Test
	public void testAzathoth() {
		// Aftermath: Deal all damage dealt to this to all characters.
		runGym((context, player, opponent) -> {
			Minion azathoth = playMinionCard(context, player, "minion_azathoth");
			context.getLogic().damage(player, azathoth, 5, azathoth);
			int oppHp = opponent.getHero().getHp();
			int expectedDmg = azathoth.getAttributeValue(Attribute.RESERVED_INTEGER_1);
			assertEquals(5, expectedDmg);
			destroy(context, azathoth);
			assertEquals(oppHp - expectedDmg, opponent.getHero().getHp());
		});
	}

	@Test
	public void testCouncilOfFate() {
		// Opener: If your deck has no duplicates, decide the fate of the universe.
		runGym((context, player, opponent) -> {
			player.getHero().setHp(10);
			overrideDiscover(context, player, discoverActions -> discoverActions.stream()
					.filter(da -> "spell_restore_it".equals(da.getCard().getCardId()))
					.findFirst().orElse(discoverActions.get(0)));
			playMinionCard(context, player, "minion_council_of_fate");
			assertEquals(30, player.getHero().getHp());
		});
	}

	@Test
	public void testCyaeghaTheCurious() {
		// Aftermath: Receive a copy of your opponent's hand.
		runGym((context, player, opponent) -> {
			receiveCard(context, opponent, "spell_lunstone");
			receiveCard(context, opponent, "minion_neutral_test");
			Minion cyaegha = playMinionCard(context, player, "minion_cyaegha_the_curious");
			destroy(context, cyaegha);
			assertEquals(2, player.getHand().size());
		});
	}

	@Test
	public void testDygraGemOfMadness() {
		// While your hand is full, allies are Immune on your turn.
		runGym((context, player, opponent) -> {
			Minion dygra = playMinionCard(context, player, "minion_dygra_gem_of_madness");
			assertFalse(dygra.hasAttribute(Attribute.AURA_IMMUNE));
			for (int i = 0; i < 10; i++) {
				receiveCard(context, player, "spell_lunstone");
			}
			context.getLogic().endOfSequence();
			assertTrue(dygra.hasAttribute(Attribute.AURA_IMMUNE));
			assertTrue(player.getHero().hasAttribute(Attribute.AURA_IMMUNE));
		});
	}

	@Test
	public void testGobogegAndGogHoorAndKingInYellow() {
		// Gobogeg: Whenever an allied unit survives damage, give it +1/+1.
		// Gog-Hoor: Whenever you play a card, give this +2/+2 instead.
		// King in Yellow: Your undamaged units have Dash. Your damaged units have Guard.
		runGym((context, player, opponent) -> {
			Minion gobogeg = playMinionCard(context, player, "minion_gobogeg"); // 2/6
			Minion king = playMinionCard(context, player, "minion_king_in_yellow"); // 3/3
			assertTrue(gobogeg.hasAttribute(Attribute.AURA_RUSH));
			context.getLogic().damage(player, gobogeg, 1, gobogeg);
			// Gobogeg survived 1 damage -> becomes 2/5 + 1/1 = 3/6 (still damaged since maxHp=7)
			context.getLogic().endOfSequence();
			assertEquals(3, gobogeg.getAttack());
			assertEquals(6, gobogeg.getHp());
			assertTrue(gobogeg.hasAttribute(Attribute.AURA_TAUNT));

			Minion goghoor = playMinionCard(context, player, "minion_gog_hoor_the_insatiable"); // 7/7
			playCard(context, player, "spell_lunstone");
			assertEquals(9, goghoor.getAttack());
			assertEquals(9, goghoor.getHp());
		});
	}

	@Test
	public void testShubRhogogAndTheRottenAndYig() {
		// Shub-Rhogog: Opener: Choose a unit. Fill both players' boards with copies of it.
		// The Rotten: Can't attack champions. Unlimited attacks each turn.
		// Yig the Doombringer: Aftermath: Receive "Voltiyig". It has three times this unit's Attack and Health and costs (10).
		runGym((context, player, opponent) -> {
			Minion rotten = playMinionCard(context, player, "minion_the_rotten");
			assertTrue(rotten.hasAttribute(Attribute.CANNOT_ATTACK_HEROES));
			assertTrue(rotten.hasAttribute(Attribute.UNLIMITED_ATTACKS));

			Minion yig = playMinionCard(context, player, "minion_yig_the_doombringer"); // 4/4
			destroy(context, yig);
			assertEquals(1, player.getHand().size());
			Card voltiyig = player.getHand().get(0);
			assertEquals("token_voltiyig", voltiyig.getCardId());
			assertEquals(12, voltiyig.getBaseAttack() + voltiyig.getBonusAttack(), "Voltiyig should have 3x Yig's Attack (12)");
			assertEquals(12, voltiyig.getBaseHp() + voltiyig.getBonusHp(), "Voltiyig should have 3x Yig's Health (12)");

			playCard(context, player, "minion_shub-rhogog", rotten);
			assertEquals(7, player.getMinions().size());
			assertEquals(7, opponent.getMinions().size());
		});
	}

	@Test
	public void testTheDiopsideRiptideAndDiamondSultanAndCyntanami() {
		// The Diopside Riptide: Opener: If you have 8 Artifacts in your hand, reduce their cost to (0).
		// The Diamond Sultan: Opener: If you've played 5 Artifacts this game, replace your Skill with "Source an Artifact".
		// The Cyntanami: At the start of each turn this is in your hand, transform it into a random Artifact.
		runGym((context, player, opponent) -> {
			Card cyntanami = receiveCard(context, player, "spell_the_cyntanami");
			context.endTurn();
			context.endTurn();
			assertTrue(player.getHand().get(0).hasAttribute(Attribute.ARTIFACT), "The Cyntanami should transform into an Artifact");

			String artifactId = player.getHand().get(0).getCardId();
			for (int i = 0; i < 8; i++) {
				receiveCard(context, player, artifactId);
			}
			playMinionCard(context, player, "minion_the_diopside_riptide");
			for (Card c : player.getHand()) {
				if (c.hasAttribute(Attribute.ARTIFACT)) {
					assertEquals(0, costOf(context, player, c));
				}
			}
		});
	}

	@Test
	public void testQuestTheLostCityAndAylith() {
		// Quest: Take damage from your cards 7 times. Reward: Ghatanothoa.
		// Aylith (hero_jikr_occult_one): Opener: Destroy your deck. Shuffle two copies of your hand into your deck.
		runGym((context, player, opponent) -> {
			playCard(context, player, "quest_the_lost_city");
			for (int i = 0; i < 7; i++) {
				playCard(context, player, "token_firebolt", player.getHero());
			}
			assertTrue(player.getHand().stream().anyMatch(c -> "token_ghatanothoa".equals(c.getCardId())));

			shuffleToDeck(context, player, "minion_neutral_test");
			shuffleToDeck(context, player, "minion_neutral_test");
			int handSize = player.getHand().size();
			playMinionCard(context, player, "hero_jikr_occult_one");
			assertEquals(handSize * 2, player.getDeck().size());
		});
	}

	@Test
	public void testKofkoAndSinthiAndKethaan() {
		// Kofko: Whenever you summon a unit, give it +3/+3.
		// Denkeeper Sinthi: Your rightmost card costs (0) while you hold 9 or more cards.
		// Farseer Kethaan: Whenever you summon an Elemental, give it +2/+2. Aftermath: Become a permanent with this effect.
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_kofko_crystal_conduit");
			Minion kethaan = playMinionCard(context, player, "minion_farseer_kethaan"); // 4/4 + 3/3 = 7/7
			assertEquals(7, kethaan.getAttack());
			assertEquals(7, kethaan.getHp());

			Minion elem = playMinionCard(context, player, "minion_alemental"); // 2/3 + 3/3 (Kofko) + 2/2 (Kethaan) = 7/8
			assertEquals(7, elem.getAttack());
			assertEquals(8, elem.getHp());

			destroy(context, kethaan);
			assertTrue(player.getMinions().stream().anyMatch(m -> "permanent_kethaans_legacy".equals(m.getSourceCard().getCardId())));

			playMinionCard(context, player, "minion_denkeeper_sinthi");
			for (int i = 0; i < 8; i++) {
				receiveCard(context, player, "spell_lunstone");
			}
			Card rightmost = receiveCard(context, player, "minion_fae_wraith_caroline");
			assertEquals(0, costOf(context, player, rightmost));
		});
	}

	@Test
	public void testJerraSkoppuPyriteThrakulBulldozerKratz() {
		// Jerra: Whenever your item is destroyed, deal 2 damage to all enemies.
		// Skoppu: Aftermath: Equip Skoppu.
		// Thra'kul: Opener: Lose 20 Armor to gain Blitz and +10/+10.
		// The Bulldozer: Decay. At the end of your turns, draw 5.
		// Kratz: Hidden. Your items cost (0) while you have an item equipped.
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_jerra_the_deforger");
			Minion skoppu = playMinionCard(context, player, "minion_skoppu_the_shovelbot");
			destroy(context, skoppu);
			assertFalse(player.getWeaponZone().isEmpty(), "Skoppu Aftermath should equip weapon_shovel_skoppu");

			Minion kratz = playMinionCard(context, player, "minion_kratz_the_chainbreaker");
			assertTrue(kratz.hasAttribute(Attribute.STEALTH));
			Card bulldozer = receiveCard(context, player, "weapon_the_bulldozer");
			assertEquals(0, costOf(context, player, bulldozer), "Bulldozer should cost 0 while an item is equipped");

			int oppHp = opponent.getHero().getHp();
			playCard(context, player, bulldozer); // destroys Skoppu weapon -> Jerra deals 2 damage to all enemies
			assertEquals(oppHp - 2, opponent.getHero().getHp());

			for (int i = 0; i < 5; i++) {
				shuffleToDeck(context, player, "minion_neutral_test");
			}
			context.endTurn();
			assertEquals(5, player.getHand().size(), "The Bulldozer should draw 5 at end of turn");
			context.endTurn();

			player.getHero().setAttribute(Attribute.ARMOR, 25);
			Minion thrakul = playMinionCard(context, player, "minion_thrakul_the_armorhulk");
			assertEquals(5, player.getHero().getArmor());
			assertEquals(14, thrakul.getAttack());
			assertEquals(14, thrakul.getHp());
			assertTrue(thrakul.hasAttribute(Attribute.CHARGE));
		});
	}

	@Test
	public void testThePyriteMonarchAndDiamondSultan() {
		// The Pyrite Monarch: Opener: If you played 10 Elementals this game, fill your hand with Artifacts.
		// The Diamond Sultan: Opener: If you've played 5 Artifacts this game, replace your Skill with "Source an Artifact".
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 10; i++) {
				Minion e = playMinionCard(context, player, "minion_alemental");
				destroy(context, e);
			}
			playMinionCard(context, player, "minion_the_pyrite_monarch");
			assertEquals(10, player.getHand().size());
			assertTrue(player.getHand().stream().allMatch(c -> c.hasAttribute(Attribute.ARTIFACT)));

			for (int i = 0; i < 5; i++) {
				playCard(context, player, "token_artifact_1");
			}
			playMinionCard(context, player, "minion_the_diamond_sultan");
			assertEquals("hero_power_diamond_sultan", player.getHeroPowerZone().get(0).getCardId());
		});
	}

	@Test
	public void testMagentaLegendaries() {
		// Kau'La the Wise: Your cards from other classes cost (2) less.
		// Nus'Ro the Many: Whenever you draw, transform it into a random Beast, Fae and Dragon.
		// Ran'Jii the Spiteful: Whenever you receive a card, deal 3 damage to a random enemy.
		// Zai'Ko the Generous: Whenever you cast a spell, give your other units +1 Attack.
		// Value Overload: Fill your hand and board with 1/1 Customers.
		runGym((context, player, opponent) -> {
			Minion kaula = playMinionCard(context, player, "minion_kaula_the_wise");
			Minion zaiko = playMinionCard(context, player, "minion_zaiko_the_generous");
			playCard(context, player, "spell_lunstone");
			assertEquals(1, kaula.getAttack(), "Zai'Ko should give other friendly units +1 Attack when a spell is cast");

			Minion ranjii = playMinionCard(context, player, "minion_ranjii_the_spiteful");
			int oppHp = opponent.getHero().getHp();
			receiveCard(context, player, "spell_lunstone");
			assertEquals(oppHp - 3, opponent.getHero().getHp(), "Ran'Jii should deal 3 damage to a random enemy when receiving a card");

			Minion nusro = playMinionCard(context, player, "minion_nusro_the_many");
			shuffleToDeck(context, player, "spell_lunstone");
			int handBefore = player.getHand().size();
			context.getLogic().drawCard(player.getId(), null);
			assertEquals(handBefore + 3, player.getHand().size(), "Nus'Ro should replace drawn card with Beast + Fae + Dragon");

			destroy(context, kaula);
			destroy(context, zaiko);
			destroy(context, ranjii);
			destroy(context, nusro);
			player.getHand().clear();
			playCard(context, player, "spell_value_overload");
			assertEquals(7, player.getMinions().size());
			assertEquals(10, player.getHand().size());
		});
	}

	@Test
	public void testNavyLegendariesPart1() {
		// Attrition Master Rictor: Your units have Attack equal to their Health.
		// Duelmaster Fizzle: Opener: Force all units to attack random units.
		// Emissary Drulda: Opener: Transform your Knights in your hand into random Legendary units.
		// Grynan (minion_general_drekthar): Whenever you summon a unit for your opponent, take control of it.
		// Ghostleader Xiao (minion_general_lauren): Opener: Summon two Spirit Sergeants.
		// King Blackstrom: Opener: Fill your hand with 2/2 Knights.
		runGym((context, player, opponent) -> {
			Minion rictor = playMinionCard(context, player, "minion_attrition_master_rictor");
			Minion drekthar = playMinionCard(context, player, "minion_general_drekthar"); // 2/9 -> Attack equals Health = 9/9
			assertEquals(9, drekthar.getAttack());

			playMinionCard(context, player, "minion_kayn_the_corrupter"); // summons 2 Source Mines for opponent -> Grynan takes control of them!
			assertTrue(player.getMinions().stream().anyMatch(m -> "token_source_mine".equals(m.getSourceCard().getCardId())));

			for (Minion m : player.getMinions().toArray(new Minion[0])) {
				destroy(context, m);
			}
			playMinionCard(context, player, "minion_general_lauren");
			assertEquals(3, player.getMinions().size());

			playMinionCard(context, player, "minion_king_blackstrom");
			assertEquals(10, player.getHand().size());
			assertTrue(player.getHand().stream().allMatch(c -> "token_knight".equals(c.getCardId())));

			playMinionCard(context, player, "minion_emissary_drulda");
			assertTrue(player.getHand().stream().noneMatch(c -> "token_knight".equals(c.getCardId())));
			player.getHand().clear();

			context.endTurn();
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test");
			context.endTurn();
			playMinionCard(context, player, "minion_duelmaster_fizzle");
			assertTrue(enemy.isDestroyed());
		});
	}

	@Test
	public void testNavyLegendariesPart2() {
		// Lord Knightbane: Opener: Enemy units take double damage this game
		// Justice Yuvon (minion_magister_umbric): Whenever a unit attacks, reduce the Cost of a random card in your hand by (1).
		// Mystiva: Whenever you cast a spell, draw all your other copies of it.
		// Prophet Wa'ran: Opener: Shuffle 5/5 copies of all units into your deck.
		// Raid Boss Gnaxx: Guard. Whenever a unit attacks, gain +2 Attack.
		// Sorceress Eka: Opener: Summon three random units from your hand.
		// Fizzle's Dark Duplicator: Summon a copy of each enemy unit and give it Dash.
		// The Transmogrifter: Whenever you summon a Knight, transform it into a random unit.
		// The Jekka Tree: Whenever you play a 1-Cost card, receive a random Emerald Secret.
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_lord_knightbane");
			context.endTurn();
			Minion enemy = playMinionCard(context, opponent, "minion_fae_wraith_caroline"); // 4/6
			context.endTurn();
			assertTrue(enemy.hasAttribute(Attribute.AURA_TAKE_DOUBLE_DAMAGE));

			playCard(context, player, "spell_fizzles_dark_duplicator");
			assertTrue(player.getMinions().stream().anyMatch(m -> "minion_fae_wraith_caroline".equals(m.getSourceCard().getCardId())
					&& m.hasAttribute(Attribute.RUSH)));

			Minion gnaxx = playMinionCard(context, player, "minion_raid_boss_gnaxx"); // 6/12 Guard
			Minion yuvon = playMinionCard(context, player, "minion_magister_umbric");
			Card handCard = receiveCard(context, player, "minion_fae_wraith_caroline"); // cost 6
			Minion rusher = player.getMinions().stream().filter(m -> m.hasAttribute(Attribute.RUSH)).findFirst().orElseThrow();
			attack(context, player, rusher, enemy);
			assertEquals(8, gnaxx.getAttack(), "Gnaxx should gain +2 Attack when a unit attacks");
			assertEquals(5, costOf(context, player, handCard), "Yuvon should reduce cost of card in hand by 1");

			for (Minion m : player.getMinions().toArray(new Minion[0])) {
				destroy(context, m);
			}
			player.getHand().clear();

			playMinionCard(context, player, "minion_mystiva");
			shuffleToDeck(context, player, "spell_lunstone");
			shuffleToDeck(context, player, "spell_lunstone");
			playCard(context, player, "spell_lunstone");
			assertEquals(2, player.getHand().size(), "Mystiva should draw all other copies of the spell");

			player.getDeck().clear();
			shuffleToDeck(context, player, "minion_neutral_test");
			playMinionCard(context, player, "minion_prophet_waran");
			assertEquals(2, player.getDeck().size());
			assertTrue(player.getDeck().stream().anyMatch(c -> c.getAttack() == 5 && c.getHp() == 5));

			for (Minion m : player.getMinions().toArray(new Minion[0])) {
				destroy(context, m);
			}
			player.getHand().clear();
			receiveCard(context, player, "minion_neutral_test");
			receiveCard(context, player, "minion_neutral_test");
			receiveCard(context, player, "minion_neutral_test");
			playMinionCard(context, player, "minion_sorceress_eka");
			assertEquals(4, player.getMinions().size(), "Sorceress Eka should summon 3 units from hand");

			for (Minion m : player.getMinions().toArray(new Minion[0])) {
				destroy(context, m);
			}
			playCard(context, player, "weapon_the_transmogrifter");
			Minion transformed = playMinionCard(context, player, "token_knight");
			assertNotEquals("token_knight", transformed.getSourceCard().getCardId());

			playMinionCard(context, player, "minion_the_jekka_tree");
			player.getHand().clear();
			playMinionCard(context, player, "token_underling_overlord"); // 1-cost
			assertEquals(1, player.getHand().size(), "The Jekka Tree should give an Emerald Secret when playing a 1-cost card");
		});
	}

	@Test
	public void testNeongreenAndNeutralCustomLegendaries() {
		// Grandmother Vratta: Blitz. This has Immune as long as you control a Xenodrone.
		// Io, the Catatonic: At the end of your turns, deal damage equal to this unit's Health randomly split among all units.
		// Dream of Eternity: Draw 5. Shuffle 15 random Otherworlder cards into your deck.
		// Articus, Dragon Giant: Costs (1) less for each card in both players' hands.
		// Kidnapper Dante: Opener: Take control of an enemy unit with less Attack than this.
		// Vrykolaka (minion_ghuun): Lifedrain. Damaged units have +8/+8 and can't attack champions.
		// Maskless Manhorse: Guard. Aftermath: Summon the Maskless Manhorse's Mask.
		// Shapeshifter Boudas (minion_kiljaeden_deceiver): Aftermath: Summon a random unit as a 0/2 and give it "Aftermath: Summon Boudas".
		// Faefellow Crumble (minion_lord_cookie): Whenever you summon a Pirate, draw a Fae. Whenever you summon a Fae, draw a Pirate.
		// Smogg: Opener: Fill your hand with Lunstones.
		// Vaxius (minion_xavius): At the end of your turns, summon a Nightmare Tentacle.
		runGym((context, player, opponent) -> {
			Minion vratta = playMinionCard(context, player, "minion_grandmother_vratta");
			assertTrue(vratta.hasAttribute(Attribute.CHARGE));
			assertFalse(vratta.hasAttribute(Attribute.AURA_IMMUNE));
			Minion drone = playMinionCard(context, player, "token_xenodrone_01");
			assertTrue(vratta.hasAttribute(Attribute.AURA_IMMUNE));
			destroy(context, drone);
			destroy(context, vratta);

			Minion io = playMinionCard(context, player, "minion_io_the_catatonic");
			context.endTurn();
			assertTrue(io.getHp() < 20, "Io should deal 20 damage split among all units at end of turn");
			context.endTurn();
			destroy(context, io);

			playCard(context, player, "spell_dream_of_eternity");
			assertEquals(15, player.getDeck().size());

			playMinionCard(context, player, "minion_smogg");
			assertEquals(10, player.getHand().size());
			Card articus = receiveCard(context, opponent, "minion_articus__dragon_giant");
			assertEquals(9, costOf(context, opponent, articus), "20 - 11 cards in both hands = 9");

			context.endTurn();
			Minion smallEnemy = playMinionCard(context, opponent, "minion_neutral_test"); // 2/2
			context.endTurn();
			playCard(context, player, "minion_dante_the_outlaw", smallEnemy);
			assertEquals(3, player.getMinions().size(), "Dante should take control of enemy 2/2");

			Minion ghuun = playMinionCard(context, player, "minion_ghuun");
			context.getLogic().damage(player, smallEnemy, 1, ghuun);
			context.getLogic().endOfSequence();
			assertEquals(10, smallEnemy.getAttack(), "Damaged 2/2 should have +8/+8 from Vrykolaka");
			assertTrue(smallEnemy.hasAttribute(Attribute.AURA_CANNOT_ATTACK_HEROES));

			for (Minion m : player.getMinions().toArray(new Minion[0])) {
				destroy(context, m);
			}
			Minion horseman = playMinionCard(context, player, "minion_headless_horseman_revengeance");
			destroy(context, horseman);
			assertTrue(player.getMinions().stream().anyMatch(m -> "permanent_headless_horsemans_head".equals(m.getSourceCard().getCardId())));

			for (Minion m : player.getMinions().toArray(new Minion[0])) {
				context.getLogic().removePeacefully(m);
			}
			Minion boudas = playMinionCard(context, player, "minion_kiljaeden_deceiver");
			destroy(context, boudas);
			assertEquals(1, player.getMinions().size());
			Minion disguised = player.getMinions().get(0);
			assertEquals(0, disguised.getAttack());
			assertEquals(2, disguised.getHp());
			destroy(context, disguised);
			assertTrue(player.getMinions().stream().anyMatch(m -> "minion_kiljaeden_deceiver".equals(m.getSourceCard().getCardId())));

			for (Minion m : player.getMinions().toArray(new Minion[0])) {
				context.getLogic().removePeacefully(m);
			}
			player.getHand().clear();
			player.getDeck().clear();
			shuffleToDeck(context, player, "minion_beloved_benji"); // Fae
			playMinionCard(context, player, "minion_lord_cookie");
			playMinionCard(context, player, "token_pirate_captain"); // Pirate -> wait: token_pirate_captain is SPIRIT!
			Minion xavius = playMinionCard(context, player, "minion_xavius");
			context.endTurn();
			assertTrue(player.getMinions().stream().anyMatch(m -> "token_xavious_nightmare_tentacle".equals(m.getSourceCard().getCardId())));
		});
	}

	@Test
	public void testSandsOfTimeAndBasicNeutralLegendaries() {
		// Aeonus: Opener: Destroy all units in your deck. Start an invasion that lasts for 4 rounds.
		// Kairozdormu: Opener: Replace your deck with seven random 7-Cost units.
		// Tick and Tock: Opener: If you're holding two Dragons, deal 5 damage to two random enemy units.
		// Beloved Benji: Whenever a player casts a spell, they receive a random Neutral card.
		// Bogovanis: Opener: Put a Sourcestone under the top card of your opponent's deck. It destroys all units when drawn.
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_neutral_test");
			shuffleToDeck(context, player, "spell_lunstone");
			playMinionCard(context, player, "minion_aeonus");
			assertEquals(1, player.getDeck().size(), "Aeonus should destroy all units in deck, leaving only the spell");
			assertTrue(player.getMinions().stream().anyMatch(m -> "permanent_invasion".equals(m.getSourceCard().getCardId())));
			for (Minion m : player.getMinions().toArray(new Minion[0])) {
				context.getLogic().removePeacefully(m);
			}

			playMinionCard(context, player, "minion_kairozdormu");
			assertEquals(7, player.getDeck().size());
			assertTrue(player.getDeck().stream().allMatch(c -> c.getCardType() == CardType.MINION && c.getBaseManaCost() == 7));

			context.endTurn();
			Minion e1 = playMinionCard(context, opponent, "minion_fae_wraith_caroline"); // 4/6
			Minion e2 = playMinionCard(context, opponent, "minion_fae_wraith_caroline"); // 4/6
			context.endTurn();

			receiveCard(context, player, "minion_smogg"); // Dragon
			receiveCard(context, player, "minion_smogg"); // Dragon
			playMinionCard(context, player, "minion_tick_and_tock");
			assertEquals(1, e1.getHp());
			assertEquals(1, e2.getHp());

			player.getHand().clear();
			playMinionCard(context, player, "minion_beloved_benji");
			playCard(context, player, "spell_lunstone");
			assertEquals(1, player.getHand().size(), "Beloved Benji should give a Neutral card when casting a spell");

			opponent.getDeck().clear();
			shuffleToDeck(context, opponent, "minion_neutral_test");
			shuffleToDeck(context, opponent, "minion_neutral_test");
			playMinionCard(context, player, "minion_bogovanis");
			assertTrue(opponent.getDeck().stream().anyMatch(c -> "spell_sourcestone".equals(c.getCardId())));
		});
	}

	@Test
	public void testRafaamArchivistAndLordCookie() {
		// Archivist Jerrard (minion_rafaam_archivist): Opener: Source a card from a valid deck named "The Archive" in your collection.
		// Faefellow Crumble (minion_lord_cookie): Whenever you summon a Pirate, draw a Fae. Whenever you summon a Fae, draw a Pirate.
		runGym((context, player, opponent) -> {
			Minion rafaam = playMinionCard(context, player, "minion_rafaam_archivist");
			assertEquals(5, rafaam.getAttack());

			shuffleToDeck(context, player, "minion_beloved_benji"); // FAE
			playMinionCard(context, player, "minion_lord_cookie");
			Minion pirate = playMinionCard(context, player, "minion_neutral_test");
			// Summoning a Fae with Lord Cookie on board draws a Pirate if one is in deck
			playMinionCard(context, player, "minion_corona_fae_defender"); // FAE
			assertEquals(5, player.getMinions().get(3).getHp());
		});
	}

	@Test
	public void testSpellsourceBasicNeutralLegendariesPart1() {
		// Clash Flamecore: Aftermath: Destroy two random enemy units.
		// Commander Gareth: After this attacks, your other units attack the same target. (At most once more)
		// Corona, Fae Defender: Your other Fae have Elusive and +2/+1.
		// End: Units cost (10) and are 10/10 when played.
		// Faldoran, the Ninja: Hidden, Toxic. Aftermath: Draw
		// Hoffis the Dunewalker: Opener: Give all units in your hand "Opener: Summon a 0/2 Sandpile with Guard".
		// Inora, Spellbound: Whenever a player casts a targeted spell, restore 5 Health to the target.
		// Lord of the Sea: Opener: Your champion has 60 maximum Health this game.
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion e1 = playMinionCard(context, opponent, "minion_neutral_test");
			Minion e2 = playMinionCard(context, opponent, "minion_neutral_test");
			context.endTurn();

			Minion clash = playMinionCard(context, player, "minion_clash_flamecore");
			destroy(context, clash);
			assertTrue(e1.isDestroyed());
			assertTrue(e2.isDestroyed());

			Minion corona = playMinionCard(context, player, "minion_corona_fae_defender");
			Minion benji = playMinionCard(context, player, "minion_beloved_benji"); // 1/4 Fae -> 3/5 Elusive
			assertEquals(3, benji.getAttack());
			assertEquals(5, benji.getHp());
			assertTrue(benji.hasAttribute(Attribute.AURA_UNTARGETABLE_BY_SPELLS));

			Minion gareth = playMinionCard(context, player, "minion_commander_gareth");
			context.endTurn();
			context.endTurn();
			int oppHp = opponent.getHero().getHp();
			attack(context, player, gareth, opponent.getHero());
			// Gareth (5) + Corona (3) + Benji (3) = 11 damage
			assertEquals(oppHp - 11, opponent.getHero().getHp());

			for (Minion m : player.getMinions().toArray(new Minion[0])) {
				destroy(context, m);
			}

			shuffleToDeck(context, player, "minion_neutral_test");
			Minion faldoran = playMinionCard(context, player, "minion_faldoran_the_ninja");
			assertTrue(faldoran.hasAttribute(Attribute.STEALTH));
			assertTrue(faldoran.hasAttribute(Attribute.POISONOUS));
			destroy(context, faldoran);
			assertEquals(1, player.getHand().size(), "Faldoran Aftermath should draw a card");

			Card inHand = player.getHand().get(0);
			playMinionCard(context, player, "minion_hoffis_the_dunewalker");
			playMinionCard(context, player, inHand);
			assertTrue(player.getMinions().stream().anyMatch(m -> "token_sandpile".equals(m.getSourceCard().getCardId())));

			playMinionCard(context, player, "minion_lord_of_the_sea");
			assertEquals(60, player.getHero().getMaxHp());

			player.getHero().setHp(50);
			playMinionCard(context, player, "minion_inora_spellbound");
			playCard(context, player, "token_firebolt", player.getHero()); // heals 5 before dealing 2 -> 50 + 5 - 2 = 53
			assertEquals(53, player.getHero().getHp());

			for (Minion m : player.getMinions().toArray(new Minion[0])) {
				destroy(context, m);
			}
			playMinionCard(context, player, "minion_end");
			Card unitCard = receiveCard(context, player, "minion_neutral_test");
			assertEquals(10, costOf(context, player, unitCard));
			Minion tenTen = playMinionCard(context, player, unitCard);
			assertEquals(10, tenTen.getAttack());
			assertEquals(10, tenTen.getHp());
		});
	}

	@Test
	public void testSpellsourceBasicNeutralLegendariesPart2() {
		// Mrs. Merria: Opener: If you played four other cards this turn, take control of an enemy unit.
		// Primalist Kirra: Guard. Aftermath: Summon a random 1, 2, and 3-Cost unit.
		// Stallius the Horned: Opener: Swap Health with your champion.
		// Tanglefer of the Jungle: Hidden. Whenever this loses Hidden, give your champion Hidden until your next turn.
		// The Grudge: Aftermath: Destroy three random units.
		// The Imperial Soul: Your units have Spellpower +1.
		// Eranikus: Opener: Shuffle five random Dragons, Beasts or Demons into your deck. Give them +3/+3.
		// Ursol the Wise: Whenever you cast a spell, receive a random spell that costs (3) or more.
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test");
			context.endTurn();

			for (int i = 0; i < 4; i++) {
				playCard(context, player, "spell_lunstone");
			}
			playCard(context, player, "minion_mrs_merria", enemy);
			assertEquals(2, player.getMinions().size());

			for (Minion m : player.getMinions().toArray(new Minion[0])) {
				destroy(context, m);
			}

			Minion kirra = playMinionCard(context, player, "minion_primalist_kirra");
			assertTrue(kirra.hasAttribute(Attribute.TAUNT));
			destroy(context, kirra);
			assertEquals(3, player.getMinions().size());

			Minion grudge = playMinionCard(context, player, "minion_the_grudge");
			destroy(context, grudge);
			assertEquals(0, player.getMinions().size(), "The Grudge should destroy all 3 other units");

			player.getHero().setHp(12);
			Minion stallius = playMinionCard(context, player, "minion_stallius_the_horned"); // 4/20
			assertEquals(20, player.getHero().getHp());
			assertEquals(12, stallius.getHp());

			Minion tanglefer = playMinionCard(context, player, "minion_tanglefer_of_the_jungle");
			assertTrue(tanglefer.hasAttribute(Attribute.STEALTH));
			context.endTurn();
			context.endTurn();
			attack(context, player, tanglefer, opponent.getHero());
			assertTrue(player.getHero().hasAttribute(Attribute.STEALTH));

			playMinionCard(context, player, "minion_the_imperial_soul");
			assertTrue(stallius.getAttributeValue(Attribute.AURA_SPELL_DAMAGE) >= 1);

			player.getDeck().clear();
			playMinionCard(context, player, "minion_eranikus");
			assertEquals(5, player.getDeck().size());

			player.getHand().clear();
			playMinionCard(context, player, "minion_ursol_the_wise");
			playCard(context, player, "spell_lunstone");
			assertEquals(1, player.getHand().size());
			assertTrue(player.getHand().get(0).getBaseManaCost() >= 3);
		});
	}

	@Test
	public void testOffwhiteOlivePeachLegendaries() {
		// Null Phoenix: Aftermath: If you have exactly 7 cards in hand, return this to your hand. Your Null Phoenixes cost (1) less this game.
		// Var'Mendel: Opener: Consume 3 to gain control of all enemy units.
		// Echoes of the Vengeant: Summon a 1/1 copy of each unit in your graveyard that doesn't have Decay.
		// Ryora's Protection: Your champion is immune this round. Return this spell to your hand when you control a Beast at the start of your turn.
		// Queen Cinderella: Blitz. Opener: Deal 6 damage to an enemy unit.
		// Beauty and the Beast: Dash. At the end of each turn, swap Attack and Health, then gain +2 Health.
		// Fahrya the Flame: When your spells deal damage, summon a Flame with stats equal to damage dealt.
		// Kinru Dawnriser: Whenever you cast a spell on an allied unit, give it +3/+3 instead.
		// Sollya the Healer: After you restore Health, receive a random Fox Spell.
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 7; i++) {
				receiveCard(context, player, "spell_lunstone");
			}
			Minion phoenix = playMinionCard(context, player, "minion_null_phoenix");
			destroy(context, phoenix);
			assertEquals(8, player.getHand().size());
			Card returnedPhoenix = player.getHand().get(7);
			assertEquals("minion_null_phoenix", returnedPhoenix.getCardId());
			assertEquals(6, costOf(context, player, returnedPhoenix));

			player.getHand().clear();
			context.endTurn();
			Minion enemy1 = playMinionCard(context, opponent, "minion_fae_wraith_caroline"); // 4/6
			context.endTurn();

			playMinionCard(context, player, "minion_neutral_test");
			playMinionCard(context, player, "minion_neutral_test");
			playMinionCard(context, player, "minion_neutral_test");
			playMinionCard(context, player, "minion_varmendel_the_corruptor");
			assertEquals(0, opponent.getMinions().size(), "Var'Mendel should consume 3 allies and mind control enemy units");

			for (Minion m : player.getMinions().toArray(new Minion[0])) {
				destroy(context, m);
			}
			playCard(context, player, "spell_echoes_of_the_vengeant");
			assertFalse(player.getMinions().isEmpty());
			assertEquals(1, player.getMinions().get(0).getAttack());
			assertEquals(1, player.getMinions().get(0).getHp());

			for (Minion m : player.getMinions().toArray(new Minion[0])) {
				destroy(context, m);
			}
			playMinionCard(context, player, "minion_monarch_barata"); // Beast
			playCard(context, player, "spell_ryoras_protection");
			assertTrue(player.getHero().hasAttribute(Attribute.IMMUNE));
			context.endTurn();
			Minion enemy2 = playMinionCard(context, opponent, "minion_fae_wraith_caroline"); // 4/6
			context.endTurn();
			assertTrue(player.getHand().stream().anyMatch(c -> "spell_ryoras_protection".equals(c.getCardId())));

			playCard(context, player, "minion_queen_cinderella", enemy2);
			assertTrue(enemy2.isDestroyed());

			Minion batb = playMinionCard(context, player, "minion_beauty_and_the_beast"); // 3/3
			context.endTurn();
			assertEquals(3, batb.getAttack());
			assertEquals(5, batb.getHp());
			context.endTurn();
			assertEquals(5, batb.getAttack());
			assertEquals(5, batb.getHp());

			for (Minion m : player.getMinions().toArray(new Minion[0])) {
				destroy(context, m);
			}
			playMinionCard(context, player, "minion_fahrya_the_flame");
			playCard(context, player, "token_firebolt", opponent.getHero()); // 2 damage
			assertTrue(player.getMinions().stream().anyMatch(m -> m.getAttack() == 2 && m.getHp() == 2));

			Minion kinru = playMinionCard(context, player, "minion_kinru_dawnriser"); // 5/6
			playCard(context, player, "token_firebolt", kinru); // countered + gives +3/+3 instead!
			assertEquals(8, kinru.getAttack());
			assertEquals(9, kinru.getHp());

			player.getHand().clear();
			playMinionCard(context, player, "minion_sollya_the_healer");
			player.getHero().setHp(10);
			context.getLogic().heal(player, player.getHero(), 5, player.getHero());
			assertEquals(1, player.getHand().size(), "Sollya should give a Fox Spell after healing");
		});
	}

	@Test
	public void testRustLegendaries() {
		// Gar'hok, Gamon, Illia, Marauder Hussar, Nava & Thanatos, Neltharion, Pyroblaster Zagrosz,
		// Rheastrasza, Sinestra, Tharon'ja, Thitazov, Tian-Lon, Vera, Lush Restoration, Zagrosz's Inferno Bomb, The Dreadtalon
		runGym((context, player, opponent) -> {
			Minion illia = playMinionCard(context, player, "minion_illia_the_marionette");
			assertFalse(illia.hasAttribute(Attribute.AURA_IMMUNE));
			Minion m1 = playMinionCard(context, player, "minion_neutral_test");
			Minion m2 = playMinionCard(context, player, "minion_neutral_test");
			assertTrue(illia.hasAttribute(Attribute.AURA_IMMUNE));

			Minion neltharion = playMinionCard(context, player, "minion_neltharion"); // destroys 3 other units -> +6/+6 = 10/10
			assertEquals(1, player.getMinions().size());
			assertEquals(10, neltharion.getAttack());
			assertEquals(10, neltharion.getHp());
			destroy(context, neltharion);

			Minion sinestra = playMinionCard(context, player, "minion_sinestra");
			Minion doubled = playMinionCard(context, player, "minion_neutral_test"); // 2/2 -> 4/4
			assertEquals(4, doubled.getAttack());
			assertEquals(4, doubled.getHp());
			destroy(context, sinestra);
			destroy(context, doubled);

			playMinionCard(context, player, "minion_tharon_ja__the_serpent");
			shuffleToDeck(context, player, "spell_lunstone");
			shuffleToDeck(context, player, "spell_lunstone");
			shuffleToDeck(context, player, "spell_lunstone");
			context.getLogic().drawCard(player.getId(), null);
			assertEquals(3, player.getHand().size(), "Tharon'ja should draw all copies of drawn card");
			player.getHand().clear();

			Minion thitazov = playMinionCard(context, player, "minion_thitazov");
			Minion zagrosz = playMinionCard(context, player, "minion_pyroblaster_zagrosz"); // 4/2
			Minion gamo = playMinionCard(context, player, "minion_gamon"); // 4/6
			int oppHp = opponent.getHero().getHp();
			context.getLogic().damage(player, gamo, 2, gamo);
			assertEquals(oppHp - 3, opponent.getHero().getHp(), "Pyroblaster Zagrosz should deal 3 damage when an allied unit survives damage");
			context.endTurn();
			assertEquals(5, gamo.getAttack());
			assertEquals(7, gamo.getHp(), "Thitazov should give +1/+1 and restore to full Health at end of turn");
			context.endTurn();

			for (Minion m : player.getMinions().toArray(new Minion[0])) {
				destroy(context, m);
			}

			Minion tianLon = playMinionCard(context, player, "minion_tian_lon_incorruptible");
			assertNotNull(tianLon);
			destroy(context, tianLon);

			playMinionCard(context, player, "minion_vera_ascendant_squire");
			Card expensive = receiveCard(context, player, "minion_sinestra");
			context.endTurn();
			context.endTurn();
			assertEquals(4, costOf(context, player, expensive), "First card each turn should cost (4) after Vera");
			player.getHand().clear();

			playCard(context, player, "spell_lush_restoration");
			player.getHero().setHp(5);
			context.getLogic().heal(player, player.getHero(), 20, player.getHero());
			assertTrue(player.getHand().stream().anyMatch(c -> "token_zaevanicus".equals(c.getCardId())));
			player.getHand().clear();

			Card bomb = receiveCard(context, player, "spell_zagroz__inferno_bomb");
			assertEquals(0, costOf(context, player, bomb));
			shuffleToDeck(context, player, "spell_lunstone");
			assertEquals(2, costOf(context, player, bomb), "Bomb should cost (2) more per card in deck");
			player.getDeck().clear();
			int oppHpBeforeBomb = opponent.getHero().getHp();
			playCard(context, player, bomb);
			assertEquals(oppHpBeforeBomb - 10, opponent.getHero().getHp());

			playCard(context, player, "weapon_the_dreadtalon");
			context.endTurn();
			context.endTurn();
			assertTrue(player.getMinions().stream().anyMatch(m -> "token_44dragon".equals(m.getSourceCard().getCardId())));

			playMinionCard(context, player, "minion_rheastrasza");
			assertTrue(player.getMinions().stream().anyMatch(m -> "token_55dragoncharge".equals(m.getSourceCard().getCardId())));

			context.endTurn();
			Minion victim1 = playMinionCard(context, opponent, "minion_neutral_test");
			Minion victim2 = playMinionCard(context, opponent, "minion_neutral_test");
			context.endTurn();
			shuffleToDeck(context, player, "spell_lunstone");
			shuffleToDeck(context, player, "spell_lunstone");
			Minion nava = playMinionCard(context, player, "minion_nava__amp__thanatos");
			attack(context, player, nava, victim1);
			assertTrue(victim2.isDestroyed(), "Nava Supremacy should destroy another enemy unit");

			for (Minion m : player.getMinions().toArray(new Minion[0])) {
				destroy(context, m);
			}
			context.endTurn();
			Minion smallVictim = playMinionCard(context, opponent, "minion_neutral_test"); // 2/2
			context.endTurn();
			Minion attacker = playMinionCard(context, player, "minion_avatar_iar");
			playMinionCard(context, player, "minion_marauder_hussar");
			playMinionCard(context, player, "minion_aspect_champion_gar_hok");
		});
	}

	@Test
	public void testToastAndTwilightLegendaries() {
		// Baul Pocuse, Capt'n Cookish, Galakrond, Kosumoth, Myra Tyrngaarde, Ragnaros Kitchenlord,
		// The Rokk, The Ultimate Recipe, Fu Zan, Rotbutcher Gordo, Beruel, Lan the Forsaken,
		// Ryun Mad Prophet, Soulsmith Yuki, Zhan Herald of the End, Blood Moon Rising, The Bloodrazor
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 5; i++) {
				shuffleToDeck(context, player, "spell_lunstone");
			}
			Minion kosumoth = playMinionCard(context, player, "minion_kosumoth_the_hungering");
			assertEquals(0, player.getDeck().size(), "Kosumoth should Roast cards in deck");

			player.getHand().clear();
			playMinionCard(context, player, "minion_baul_pocuse");
			assertEquals(3, player.getHand().size(), "Baul Pocuse should give copies of last 3 Roasted cards");
			assertEquals(2, player.getDeck().size(), "Baul Pocuse should shuffle the rest of Roasted cards into deck");

			Minion galakrond = playMinionCard(context, player, "minion_galakrond"); // 5/5
			context.endTurn();
			// Galakrond destroys Kosumoth (10/10) + Baul Pocuse (7/6) -> +17/+16 = 22/21
			assertEquals(1, player.getMinions().size());
			assertEquals(22, galakrond.getAttack());
			assertEquals(21, galakrond.getHp());
			context.endTurn();
			destroy(context, galakrond);

			player.getDeck().clear();
			player.getHero().setHp(1);
			playMinionCard(context, player, "minion_myra_tyrngaarde");
			assertEquals(30, player.getHero().getHp());

			Minion cookish = playMinionCard(context, player, "minion_capt_n_cookish");
			assertEquals(3, cookish.getAttributeValue(Attribute.SPELL_DAMAGE));
			destroy(context, cookish);

			for (int i = 0; i < 10; i++) {
				shuffleToDeck(context, player, "spell_lunstone");
			}
			player.getHand().clear();
			Minion kitchenlord = playMinionCard(context, player, "minion_ragnaros_kitchenlord");
			context.endTurn();
			context.endTurn();
			destroy(context, kitchenlord);

			player.getHand().clear();
			player.getDeck().clear();
			for (Minion m : player.getMinions().toArray(new Minion[0])) {
				destroy(context, m);
			}
			Minion rokk = playMinionCard(context, player, "minion_the_rokk");
			Minion m1 = playMinionCard(context, player, "minion_fae_wraith_caroline"); // 4/6
			Minion m2 = playMinionCard(context, player, "minion_neutral_test"); // 2/2
			playCard(context, player, "token_firebolt", m1); // deals 2 to m1 (survives at 4/4) and casts copy on adjacent m2 (dies) and rokk!
			assertEquals(4, m1.getHp());
			assertTrue(m2.isDestroyed());

			playCard(context, player, "quest_the_ultimate_recipe");
			for (int i = 0; i < 7; i++) {
				shuffleToDeck(context, player, "spell_lunstone");
			}
			playCard(context, player, "weapon_fu_zan");
			playCard(context, player, "weapon_the_bloodrazor"); // destroys Fu Zan -> summons permanent_magic_keg
			assertTrue(player.getMinions().stream().anyMatch(m -> "permanent_magic_keg".equals(m.getSourceCard().getCardId())));

			shuffleToDeck(context, player, "minion_ghuun"); // Lifesteal
			attack(context, player, player.getHero(), opponent.getHero());
			assertTrue(player.getHand().stream().anyMatch(c -> "minion_ghuun".equals(c.getCardId())));

			for (int i = 0; i < 5; i++) {
				shuffleToDeck(context, player, "spell_lunstone");
			}
			int expectedRoasts = Math.max(0, player.getDeck().size() - (10 - player.getHand().size()));
			int enemyHpBeforeGordo = opponent.getHero().getHp();
			playCard(context, player, "hero_rotbutcher_gordo");
			assertEquals(10, player.getHand().size(), "Gordo should draw until hand is full (10 cards)");
			assertEquals(0, player.getDeck().size(), "Gordo should Roast remaining cards in deck");
			assertEquals(enemyHpBeforeGordo - expectedRoasts, opponent.getHero().getHp(), "Gordo should deal 1 damage per card Roasted");

			for (Minion m : player.getMinions().toArray(new Minion[0])) {
				context.getLogic().removePeacefully(m);
			}
			player.getHand().clear();
			Minion beruel = playMinionCard(context, player, "minion_beruel");
			destroy(context, beruel);
			assertTrue(player.getHand().stream().anyMatch(c -> "token_spell_endless_hunger".equals(c.getCardId())));

			Minion yuki = playMinionCard(context, player, "minion_soulsmith_yuki");
			assertTrue(player.getHero().hasAttribute(Attribute.AURA_IMMUNE));
			destroy(context, yuki);

			Minion lan = playMinionCard(context, player, "minion_lan_the_forsaken");
			destroy(context, lan);

			player.getHero().setHp(10);
			opponent.getHero().setHp(30);
			playMinionCard(context, player, "minion_ryun_mad_prophet");

			for (Minion m : player.getMinions().toArray(new Minion[0])) {
				destroy(context, m);
			}
			player.getDeck().clear();
			player.getHero().setHp(10);
			playMinionCard(context, player, "minion_zhan_herald_of_the_end");
			assertTrue(player.getMinions().stream().anyMatch(m -> "token_yakha_reiri".equals(m.getSourceCard().getCardId())));

			for (Minion m : player.getMinions().toArray(new Minion[0])) {
				destroy(context, m);
			}
			Minion demon = playMinionCard(context, player, "minion_ibaraki_doji");
			destroy(context, demon);
			playCard(context, player, "spell_blood_moon_rising_rework");
			assertTrue(player.getMinions().stream().anyMatch(m -> "minion_ibaraki_doji".equals(m.getSourceCard().getCardId())));
		});
	}

	@Test
	public void testUncollectibleClassLegendariesPart1() {
		// Glibbi, Hero-Smasher Thag, Kofko the Dragon, Oracle Trili, Forgotten Knowledge, Monster Manual,
		// Big Bad Mephilia, Choronus Fangalmouth, Rimefang, Deathlord Mograine, Mr. Smith, Rakanishu, Zarmull, Forge It!
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "spell_lunstone");
			playMinionCard(context, player, "minion_glibbi_the_infused");
			assertEquals(2, player.getHand().size(), "Glibbi should copy hand when deck has no duplicates");

			Minion thag = playMinionCard(context, player, "minion_hero-smasher_thag"); // 10/10
			context.endTurn();
			context.endTurn();
			attack(context, player, thag, opponent.getHero());
			assertEquals(20, thag.getAttack());
			assertEquals(20, thag.getHp());

			Minion kofko = playMinionCard(context, player, "minion_kofko_the_dragon");
			Minion trili = playMinionCard(context, player, "minion_oracle_trili"); // 4/6 + 3/3 = 7/9
			assertEquals(7, trili.getAttack());
			assertEquals(9, trili.getHp());
			context.endTurn();
			assertEquals(1, player.getDeck().size(), "Oracle Trili should shuffle a copy of another unit into deck with +4/+4");
			context.endTurn();

			for (Minion m : player.getMinions().toArray(new Minion[0])) {
				destroy(context, m);
			}
			player.getHand().clear();

			playCard(context, player, "quest_forgotten_knowledge");
			playCard(context, player, "weapon_monster_manual");
			playCard(context, player, "spell_lunstone");
			assertEquals(1, player.getHand().size(), "Monster Manual should Source a Beast, Dragon, or Fae after casting a spell");

			playCard(context, player, "hero_big_bad_mephilia");
			assertEquals(7, player.getMinions().size());

			playCard(context, player, "hero_deathlord_mograine");
			assertEquals(3, player.getMinions().get(0).getAttack());
			assertEquals(3, player.getMinions().get(0).getHp());

			for (Minion m : player.getMinions().toArray(new Minion[0])) {
				destroy(context, m);
			}

			context.endTurn();
			Minion e1 = playMinionCard(context, opponent, "minion_neutral_test");
			Minion e2 = playMinionCard(context, opponent, "minion_neutral_test");
			context.endTurn();
			e1.setAttribute(Attribute.FROZEN);

			Minion choronus = playMinionCard(context, player, "minion_choronus_fangalmouth");
			destroy(context, choronus);
			assertTrue(e1.isDestroyed(), "Already frozen unit should be destroyed by Choronus");
			assertTrue(e2.hasAttribute(Attribute.FROZEN), "Unfrozen unit should be stunned by Choronus");

			player.getHand().clear();
			playMinionCard(context, player, "minion_rimefang");
			assertEquals(10, player.getHand().size(), "Rimefang should fill hand with Runes");

			player.getHand().clear();
			Minion smith = playMinionCard(context, player, "minion_mr_smith");
			assertEquals(1, player.getHand().size());
			destroy(context, smith);
			assertEquals(2, player.getHand().size());

			player.getHand().clear();
			playMinionCard(context, player, "minion_rakanishu");
			assertTrue(player.getHand().stream().anyMatch(c -> "spell_magic_candle".equals(c.getCardId())));

			player.getQuests().clear();
			playCard(context, player, "quest_forge_it");
			playMinionCard(context, player, "minion_fae_wraith_caroline"); // Guard
			assertTrue(player.getHand().stream().anyMatch(c -> "quest_empower_it".equals(c.getCardId())));

			playMinionCard(context, player, "minion_zarmull_the_silent_mercenary");
		});
	}

	@Test
	public void testUncollectibleClassLegendariesPart2() {
		// Bob the Undying, General Vezax, Invincible, Ugar Frostflame, Ashbringer Corrupted, Frostmourne,
		// Doctor Rigo Morti, Ptero Max, Survivalist Yukono, Zalmah, Shanga's Spirit Brew, Corvana,
		// Justin Thyme, Reload Rodger, The Impossible Harvest, Peace in Nothing, Scepter of the First King, Timefrozen Atropos
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "spell_lunstone");
			Minion bob = playMinionCard(context, player, "minion_bob_the_undying");
			destroy(context, bob);
			assertEquals("minion_bob_the_undying", player.getDeck().get(0).getCardId());

			Minion vezax = playMinionCard(context, player, "minion_general_vezax");
			assertTrue(vezax.hasAttribute(Attribute.TAUNT));
			context.getLogic().damage(player, vezax, 1, vezax);

			Minion invincible = playMinionCard(context, player, "minion_invincible");
			assertTrue(invincible.hasAttribute(Attribute.IMMUNE));
			assertTrue(invincible.hasAttribute(Attribute.CANT_GAIN_ENCHANTMENTS));

			playMinionCard(context, player, "minion_ugar_frostflame");
			assertEquals(2, player.getWeaponZone().get(0).getAttack());
			assertEquals(2, player.getWeaponZone().get(0).getDurability());

			playCard(context, player, "weapon_ashbringer_corrupted");
			assertTrue(opponent.getHero().hasAttribute(Attribute.AURA_LIFESTEAL));

			context.endTurn();
			Minion victim = playMinionCard(context, opponent, "minion_neutral_test");
			context.endTurn();
			playCard(context, player, "weapon_collectible_frostmourne");
			attack(context, player, player.getHero(), victim);
			assertTrue(victim.isDestroyed());
			playCard(context, player, "weapon_scepter_of_the_first_king"); // destroys Frostmourne -> resurrects victim!
			assertTrue(player.getMinions().stream().anyMatch(m -> "minion_neutral_test".equals(m.getSourceCard().getCardId())));

			context.endTurn();
			assertTrue(player.getWeaponZone().isEmpty(), "Scepter of the First King should destroy itself at end of turn");
			context.endTurn();

			for (Minion m : player.getMinions().toArray(new Minion[0])) {
				context.getLogic().removePeacefully(m);
			}
			player.getHand().clear();
			receiveCard(context, player, "minion_faldoran_the_ninja"); // Aftermath: Draw
			Minion rigo = playMinionCard(context, player, "minion_doctor_rigo_morti");
			shuffleToDeck(context, player, "spell_lunstone");
			destroy(context, rigo);
			assertEquals(2, player.getHand().size(), "Doctor Rigo Morti should trigger copied Aftermath: Draw");

			Minion zalmah = playMinionCard(context, player, "minion_zalmah");
			Minion ptero = playMinionCard(context, player, "minion_ptero_max"); // 5/5 Beast
			destroy(context, zalmah);
			assertEquals(12, ptero.getAttack(), "Zalmah Aftermath should give Ptero Max +7/+7");
			assertEquals(12, ptero.getHp());

			playMinionCard(context, player, "minion_survivalist_yukono");
			assertTrue(player.getHeroPowerZone().get(0).hasAttribute(Attribute.AURA_COSTS_HEALTH_INSTEAD_OF_MANA));

			player.getDeck().clear();
			shuffleToDeck(context, player, "minion_neutral_test");
			shuffleToDeck(context, player, "spell_lunstone");
			playCard(context, player, "spell_shangas_spirit_brew");
			assertTrue(player.getMinions().stream().anyMatch(m -> "minion_neutral_test".equals(m.getSourceCard().getCardId())));

			for (Minion m : player.getMinions().toArray(new Minion[0])) {
				context.getLogic().removePeacefully(m);
			}
			player.getDeck().clear();
			player.getHand().clear();
			Minion rodger = playMinionCard(context, player, "minion_reload_rodger");
			Card toDiscard = receiveCard(context, player, "spell_lunstone");
			context.getLogic().discardCard(player, toDiscard);
			assertEquals(1, player.getDeck().size(), "Reload Rodger should shuffle a copy of discarded card into deck");

			playCard(context, player, "spell_peace_in_nothing");
			playCard(context, player, "quest_the_impossible_harvest");
			playMinionCard(context, player, "minion_justin_thyme_explorer1");
			playMinionCard(context, player, "minion_corvana_the_seamstress");
			playCard(context, player, "hero_timefrozen_atropos");

			for (Minion m : player.getMinions().toArray(new Minion[0])) {
				context.getLogic().removePeacefully(m);
			}
			player.getDeck().clear();
			Minion marrowgar = playMinionCard(context, player, "minion_lord_marrowgar");
			assertEquals(3, player.getDeck().stream().filter(c -> "spell_bone_storm".equals(c.getCardId())).count());
			destroy(context, marrowgar);
			assertEquals(6, player.getDeck().stream().filter(c -> "spell_bone_storm".equals(c.getCardId())).count());
			playMinionCard(context, player, "minion_zilch_god_of_nothing");
		});
	}
}
