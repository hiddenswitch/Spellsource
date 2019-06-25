package com.hiddenswitch.spellsource;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.spells.ChangeHeroPowerSpell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static org.testng.Assert.*;

public class WitchDoctorTests extends TestBase {

	@Test
	public void testGurubashiBloodletter() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_gurubashi_bloodletter");
			context.endTurn();
			playCard(context, player, "spell_2_missiles");
			assertEquals(opponent.getMinions().size(), 1);
			assertEquals(opponent.getMinions().get(0).getSourceCard().getCardId(), "token_rage_behemoth");
			assertEquals(target.transformResolved(context), opponent.getMinions().get(0));
		});
	}

	@Test
	public void testHexlordZixxis() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			playCard(context, opponent, "spell_mirror_image");
			context.endTurn();
			context.endTurn();
			playCard(context, opponent, "spell_mind_blast");
			context.endTurn();
			player.getHero().setAttribute(Attribute.IMMUNE);
			int opponentHp = opponent.getHero().getHp();
			playCard(context, player, "minion_hexlord_zixxis");
			assertEquals(opponent.getHero().getHp(), opponentHp - 5);
			assertEquals(player.getMinions().size(), 1, "No Mirror Images");
		});
	}

	@Test
	public void testTheliaSilentdreamer() {
		runGym((context, player, opponent) -> {
			Minion thelia = playMinionCard(context, player, "minion_thelia_silentdreamer");
			playCard(context, player, "spell_test_cost_3_buff", thelia);
			assertEquals(thelia.getAttack(), thelia.getBaseAttack() + 1);
			playCard(context, player, "spell_test_cost_6_buff", thelia);
			assertEquals(thelia.getAttack(), thelia.getBaseAttack() + 3);
		});
	}

	@Test
	public void testSpiritualDiffusion() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target1 = playMinionCard(context, opponent, "minion_neutral_test");
			Minion target2 = playMinionCard(context, opponent, "minion_neutral_test");
			Minion target3 = playMinionCard(context, opponent, "minion_neutral_test");
			Minion target4 = playMinionCard(context, opponent, "minion_neutral_test");
			context.endTurn();
			Minion shouldNotBeDestroyed = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "spell_spiritual_diffusion");

			assertEquals(opponent.getMinions().size(), 4);
			assertEquals(opponent.getMinions().stream().filter(c -> c.getSourceCard().getCardId().equals("token_voodoo_spirit")).count(), 3L);
			assertFalse(shouldNotBeDestroyed.isDestroyed());
			assertEquals(shouldNotBeDestroyed.getSourceCard().getCardId(), "minion_neutral_test");
			assertEquals(Stream.of(target1, target2, target3, target4).filter(Actor::isDestroyed).count(), 3L);
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target1 = playMinionCard(context, opponent, "minion_neutral_test");
			Minion target2 = playMinionCard(context, opponent, "minion_neutral_test");
			context.endTurn();
			Minion shouldNotBeDestroyed = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "spell_spiritual_diffusion");
			assertEquals(opponent.getMinions().size(), 2);
			assertEquals(opponent.getMinions().stream()
					.filter(c -> c.getSourceCard().getCardId().equals("token_voodoo_spirit")).count(), 2L);
			assertFalse(shouldNotBeDestroyed.isDestroyed());
			assertEquals(shouldNotBeDestroyed.getSourceCard().getCardId(), "minion_neutral_test");
			assertEquals(Stream.of(target1, target2).filter(Actor::isDestroyed).count(), 2L);
		});
	}

	@Test
	public void testDreamshaker() {
		runGym((context, player, opponent) -> {
			Card played1 = receiveCard(context, player, "spell_the_coin");
			Card notPlayed1 = receiveCard(context, player, "minion_test_mech");
			Card played2 = receiveCard(context, opponent, "minion_neutral_test");
			Card notPlayed2 = receiveCard(context, opponent, "minion_test_mech");
			playCard(context, player, "minion_neutral_test");
			context.endTurn();
			playCard(context, opponent, "spell_the_coin");
			context.endTurn();
			playCard(context, player, "minion_dreamshaker");
			assertEquals(played1.getZone(), Zones.GRAVEYARD);
			assertTrue(played1.hasAttribute(Attribute.DISCARDED));
			assertEquals(played2.getZone(), Zones.GRAVEYARD);
			assertTrue(played2.hasAttribute(Attribute.DISCARDED));
			assertEquals(notPlayed1.getZone(), Zones.HAND);
			assertEquals(notPlayed2.getZone(), Zones.HAND);
		});
	}

	@Test
	public void testTaintedBloodPupeteerSenzaku() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "hero_puppeteer_senzaku");
			context.endTurn();
			Minion charger = playMinionCard(context, opponent, "minion_charge_test");
			attack(context, opponent, charger, target);
			assertEquals(player.getMinions().size(), 0, "Should not summon charger because not Puppeteer's turn.");
		});

		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "hero_puppeteer_senzaku");
			context.endTurn();
			Minion charger = playMinionCard(context, opponent, "minion_charge_test");
			context.endTurn();
			attack(context, player, target, charger);
			assertEquals(player.getMinions().size(), 1, "Should summon charger because Puppeteer's turn.");
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_charge_test", "Should summon charger because Puppeteer's turn.");
		});
	}

	@Test
	public void testDobyMick() {
		runGym((context, player, opponent) -> {
			Minion test1 = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "minion_doby_mick");
			playCard(context, player, "spell_test_deal_6", test1);
			assertEquals(test1.getHp(), 1);
			Minion test2 = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "spell_test_deal_6", test2);
			assertEquals(test2.getHp(), 1);
			context.endTurn();
			playCard(context, opponent, "spell_test_deal_6", test2);
			assertEquals(test2.getHp(), 1);
			Minion shouldBeDestroyed = playMinionCard(context, opponent, "minion_neutral_test");
			playCard(context, opponent, "spell_test_deal_6", shouldBeDestroyed);
			assertTrue(shouldBeDestroyed.isDestroyed());
			// Summon a minion for the opponent, and assert it cannot be destroyed
			playCard(context, opponent, "spell_summon_for_opponent");
			Minion test3 = player.getMinions().get(3);
			assertNotEquals(test1, test3);
			assertNotEquals(test2, test3);
			playCard(context, opponent, "spell_test_deal_6", test3);
			assertEquals(test3.getHp(), 1);
			context.endTurn();
			for (Minion minion : new Minion[]{test1, test2, test3}) {
				playCard(context, player, "spell_test_deal_6", minion);
				assertTrue(minion.isDestroyed());
			}
		});
	}

	@Test
	public void testDoctosaur() {
		runGym((context, player, opponent) -> {
			Minion doctosaur = playMinionCard(context, player, "minion_doctosaur");
			Minion mech = playMinionCard(context, player, "minion_test_mech");
			Minion notMech = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "weapon_test_3_2");
			int startingHp = opponent.getHero().getHp();
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(opponent.getHero().getHp(), startingHp - 3 /*weapon damange*/ - mech.getAttack());
		});
	}

	@Test
	public void testMindControlledMech() {
		runGym((context, player, opponent) -> {
			Minion mech1 = playMinionCard(context, player, "minion_mind_controlled_mech");
			Minion mech2 = playMinionCard(context, player, "minion_mind_controlled_mech");
			assertEquals(player.getHeroPowerZone().get(0).getCardId(), "hero_power_mental_command");
			int startingHp = opponent.getHero().getHp();
			useHeroPower(context, player, opponent.getHero().getReference());
			assertEquals(opponent.getHero().getHp(), startingHp - mech1.getAttack() - mech2.getAttack());
			destroy(context, mech1);
			assertEquals(player.getHeroPowerZone().get(0).getCardId(), "hero_power_mental_command");
		});
	}

	@Test
	public void testWitchingTraveler() {
		runGym((context, player, opponent) -> {
			Card coin = receiveCard(context, player, "spell_the_coin");
			assertEquals(costOf(context, player, coin), 0);
			playCard(context, player, "spell_the_coin");
			playCard(context, player, "minion_witching_traveler");
			assertEquals(costOf(context, player, coin), 0);
			playCard(context, player, "spell_the_coin");
			playCard(context, player, "minion_witching_traveler");
			assertEquals(costOf(context, player, coin), 0);
			playCard(context, player, "spell_the_coin");
			playCard(context, player, "minion_witching_traveler");
			assertEquals(costOf(context, player, coin), 1);
		});
	}

	// Secretive Chanter: "Opener: Transform all Voodoo spells in your hand into Emerald Secrets."
	@Test
	public void testSecretiveChanter() {
		runGym((context, player, opponent) -> {
			Card v1 = receiveCard(context, player, "spell_divination");
			Card v2 = receiveCard(context, player, "spell_frenzy");
			Card v3 = receiveCard(context, player, "spell_hex_bolt");
			Card v4 = receiveCard(context, player, "spell_spirit_bind");
			Card v5 = receiveCard(context, player, "spell_spirit_bind");
			Card nonv = receiveCard(context, player, "spell_fireball");
			playMinionCard(context, player, "minion_secretive_chanter");
			// Check whether all Voodoo spells have been transformed
			assertTrue(player.getHand().get(0).getCardId().contains("secret_secret_of"));
			assertTrue(player.getHand().get(1).getCardId().contains("secret_secret_of"));
			assertTrue(player.getHand().get(2).getCardId().contains("secret_secret_of"));
			assertTrue(player.getHand().get(3).getCardId().contains("secret_secret_of"));
			assertTrue(player.getHand().get(4).getCardId().contains("secret_secret_of"));
			assertEquals(player.getHand().get(5).getCardId(), "spell_fireball");
		});
	}

	// High Shaman Mawliki: "Your spells have Toxic. Your healing is doubled. Your other minions have Elusive and Guard."
	@Test
	public void testHighShamanMawliki() {
		runGym((context, player, opponent) -> {
			Minion friend1 = playMinionCard(context, player, "minion_neutral_test");
			Minion friend2 = playMinionCard(context, player, "minion_neutral_test_14");
			playMinionCard(context, player, "minion_high_shaman_mawliki");
			// Check for guard and elusive
			assertTrue(friend1.hasAttribute(Attribute.TAUNT));
			assertTrue(friend2.hasAttribute(Attribute.TAUNT));
			assertTrue(friend1.hasAttribute(Attribute.UNTARGETABLE_BY_SPELLS));
			assertTrue(friend2.hasAttribute(Attribute.UNTARGETABLE_BY_SPELLS));
			context.endTurn();
			Minion enemy1 = playMinionCard(context, opponent, "minion_armageddon_wyvern");
			Minion enemy2 = playMinionCard(context, opponent, "minion_armageddon_wyvern");
			Minion enemy3 = playMinionCard(context, opponent, "minion_armageddon_wyvern");
			context.endTurn();
			// Check for toxic spells
			playCard(context, player, "spell_fireball", enemy1);
			playCard(context, player, "spell_fireball", enemy2);
			assertTrue(enemy1.isDestroyed());
			assertTrue(enemy2.isDestroyed());
			assertEquals(opponent.getMinions().size(), 1);
			context.endTurn();
			attack(context, opponent, enemy3, friend2);
			assertEquals(friend2.getHp(), 4);
			context.endTurn();
			// Check for double healing effect
			playCard(context, player, "spell_crystal_power_2", friend2);
			assertEquals(friend2.getHp(), 14);
		});
	}

	// Hone Reflexes: "Gain +3 Attack this turn.",
	@Test
	public void testHoneReflexes() {
		runGym((context, player, opponent) -> {
			SpellDesc spell = new SpellDesc(ChangeHeroPowerSpell.class);
			spell.put(SpellArg.CARD, "hero_power_hone_reflexes");
			context.getLogic().castSpell(player.getId(), spell, player.getReference(), null, false);
			context.getLogic().endOfSequence();
			assertEquals(player.getHero().getHeroPower().getCardId(), "hero_power_hone_reflexes");
			useHeroPower(context, player);
			assertEquals(player.getHero().getAttack(), player.getHero().getBaseAttack() + 3);
			context.endTurn();
			context.endTurn();
			assertEquals(player.getHero().getAttack(), player.getHero().getBaseAttack());
		});
	}

	// Breezecatcher: "Battlecry: If you're holding an Emerald Secret, give your hero Windfury this turn.",
	@Test
	public void testBreezecatcher() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, player, "minion_neutral_test_14");
			context.endTurn();
			player.getHero().setAttack(2);
			receiveCard(context, player, "secret_secret_of_winter");
			playMinionCard(context, player, "minion_breezecatcher");
			attack(context, player, player.getHero(), target);
			assertTrue(player.getHero().canAttackThisTurn());
			context.endTurn();
			context.endTurn();
			attack(context, player, player.getHero(), target);
			assertFalse(player.getHero().canAttackThisTurn());
		});
	}

	// Curious Kirrin: "Deflect. Extra Strike. Whenever this minion attacks, add an Emerald Secret to your hand."
	@Test
	public void testCuriousKirrin() {
		runGym((context, player, opponent) -> {
			Minion kirrin = playMinionCard(context, player, "minion_curious_kirrin");
			context.endTurn();
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test_14");
			context.endTurn();
			player.getHero().setHp(10);
			attack(context, player, kirrin, enemy);
			assertTrue(kirrin.canAttackThisTurn());
			assertTrue(player.getHand().get(0).getCardId().contains("secret_secret_of"));
			assertEquals(player.getHero().getHp(), 10 - enemy.getBaseAttack());
		});
	}

	// Jhu Zho: "Opener: Swap each 1-Cost spell in your hand with a minion from your deck."
	@Test
	public void testJhuZho() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "spell_pay_respects");
			Card minion_indeck = shuffleToDeck(context, player, "minion_blastflame_dragon");
			playMinionCard(context, player, "minion_jhu_zho");
			assertEquals(player.getHand().size(), 1);
			assertEquals(player.getHand().get(0), minion_indeck);
		});
		runGym((context, player, opponent) -> {
			Card inHand = receiveCard(context, player, "spell_pay_respects");
			shuffleToDeck(context, player, "spell_fireball");
			playMinionCard(context, player, "minion_jhu_zho");
			assertEquals(player.getHand().size(), 1);
			assertEquals(player.getHand().get(0), inHand);
		});
	}

	// Lake Elemental: "Deflect."
	@Test
	public void testLakeElemental() {
		runGym(((context, player, opponent) -> {
			Minion lake = playMinionCard(context, player, "minion_lake_elemental");
			context.endTurn();
			player.getHero().setHp(10);
			playCard(context, opponent, "spell_fireball", lake);
			assertEquals(lake.getHp(), lake.getBaseHp());
			assertEquals(player.getHero().getHp(), 4);
			context.endTurn();
			context.endTurn();
			playCard(context, opponent, "spell_fireball", lake);
			assertTrue(lake.isDestroyed());
		}));
	}

	// River Spirit: "Battlecry: If you control another Elemental, summon an Elemental from your hand.",
	@Test
	public void testRiverSpirit() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_lake_elemental");
			Card ele = receiveCard(context, player, "minion_crystal_giant");
			Card wisp = receiveCard(context, player, "minion_wisp");
			playMinionCard(context, player, "minion_river_spirit");
			assertEquals(player.getMinions().size(), 3);
			assertEquals(player.getMinions().get(2).getSourceCard(), ele);
			assertEquals(player.getHand().size(), 1);
		});
	}

	// Shy Sprite: "Deflect. You can use your Hero Power twice per turn."
	@Test
	public void testShySprite() {
		runGym((context, player, opponent) -> {
			// Setting it to a specific hero power since some require a target some don't for useHeroPower()
			SpellDesc spell = new SpellDesc(ChangeHeroPowerSpell.class);
			spell.put(SpellArg.CARD, "hero_power_hone_reflexes");
			context.getLogic().castSpell(player.getId(), spell, player.getReference(), null, false);
			context.getLogic().endOfSequence();
			Card heropower = player.getHero().getHeroPower();
			assertEquals(heropower.getCardId(), "hero_power_hone_reflexes");
			player.setMana(20);
			assertTrue(context.getLogic().canPlayCard(player.getId(), heropower.getReference()));
			useHeroPower(context, player);
			assertFalse(context.getLogic().canPlayCard(player.getId(), heropower.getReference()));
			Minion sprite = playMinionCard(context, player, "minion_shy_sprite");
			context.endTurn();
			player.getHero().setHp(10);
			playCard(context, opponent, "spell_fireball", sprite);
			assertFalse(sprite.isDestroyed());
			assertEquals(player.getHero().getHp(), 4);
			context.endTurn();
			player.setMana(20);
			assertTrue(context.getLogic().canPlayCard(player.getId(), heropower.getReference()));
			useHeroPower(context, player);
			assertTrue(context.getLogic().canPlayCard(player.getId(), heropower.getReference()));
			useHeroPower(context, player);
			assertFalse(context.getLogic().canPlayCard(player.getId(), heropower.getReference()));
		});
	}

	// The Uncasked: "Battlecry: Draw Elementals from your deck until your hand is full.",
	@Test
	public void testTheUncasked() {
		runGym(((context, player, opponent) -> {
			assertEquals(player.getHand().size(), 0);
			shuffleToDeck(context, player, "minion_lake_elemental");
			shuffleToDeck(context, player, "minion_wisp");
			shuffleToDeck(context, player, "minion_thunderhead");
			playMinionCard(context, player, "minion_the_uncasked");
			assertEquals(player.getHand().size(), 2);
			assertEquals(player.getHand().get(0).getRace(), Race.ELEMENTAL);
			assertEquals(player.getHand().get(1).getRace(), Race.ELEMENTAL);
		}));
	}

	// Spell Hone Reflexes: "Your Hero Power becomes 'Gain +3 Attack this turn."
	@Test
	public void testSpellHoneReflexes() {
		runGym(((context, player, opponent) -> {
			assertNotEquals(player.getHero().getHeroPower().getCardId(), "hero_power_hone_reflexes");
			playCard(context, player, "spell_hone_reflexes");
			assertEquals(player.getHero().getHeroPower().getCardId(), "hero_power_hone_reflexes");
		}));
	}

	// Touch of Death: "Shoot missiles equal to your hero's Attack that each deal as much damage."
	@Test
	public void testTouchofDeath() {
		runGym(((context, player, opponent) -> {
			context.endTurn();
			Minion target1 = playMinionCard(context, opponent, "minion_neutral_test_14");
			context.endTurn();
			opponent.getHero().setHp(opponent.getHero().getBaseHp());
			player.getHero().setAttack(2);
			playCard(context, player, "spell_touch_of_death");
			assertEquals(opponent.getHero().getHp() + target1.getHp(), opponent.getHero().getBaseHp() + target1.getBaseHp() - 4);
		}));
	}

	// Zen Pilgrimage: "Shuffle a friendly minion into your deck. Add Emerald Secrets to your hand equal to its cost."
	@Test
	public void testZenPilgrimage() {
		runGym(((context, player, opponent) -> {
			Minion friend = playMinionCard(context, player, "minion_blood_knight");
			playCard(context, player, "spell_zen_pilgrimage", friend);
			assertEquals(player.getHand().size(), 3);
			assertTrue(player.getHand().get(0).getCardId().contains("secret_secret_of"));
			assertTrue(player.getHand().get(1).getCardId().contains("secret_secret_of"));
			assertTrue(player.getHand().get(2).getCardId().contains("secret_secret_of"));
		}));
	}

	// Revisit zen_pilgrimage card, not correct yet for howMany

}