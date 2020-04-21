package com.hiddenswitch.spellsource.tests.cards;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.ChangeHeroPowerSpell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.TargetSelection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.CONCURRENT)
public class MonkTests extends TestBase {

	// Hone Reflexes: "Gain +3 Attack this turn.",
	@Test
	public void testHoneReflexes() {
		runGym((context, player, opponent) -> {
			SpellDesc spell = new SpellDesc(ChangeHeroPowerSpell.class);
			spell.put(SpellArg.CARD, "hero_power_hone_reflexes");
			context.getLogic().castSpell(player.getId(), spell, player.getReference(), null, TargetSelection.NONE, false, null);
			context.getLogic().endOfSequence();
			assertEquals(player.getHeroPowerZone().get(0).getCardId(), "hero_power_hone_reflexes");
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
			assertTrue(player.getHero().canAttackThisTurn(context));
			context.endTurn();
			context.endTurn();
			attack(context, player, player.getHero(), target);
			assertFalse(player.getHero().canAttackThisTurn(context));
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
			assertTrue(kirrin.canAttackThisTurn(context));
			assertTrue(player.getHand().get(0).getCardId().contains("secret_secret_of"));
			assertEquals(player.getHero().getHp(), 10 - enemy.getBaseAttack());
		});
	}

	// Jhu Zho: "Opener: Swap each 1-Cost spell in your hand with a minion from your deck."
	@Test
	public void testJhuZho() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "spell_pay_respects");
			Card minionInDeck = shuffleToDeck(context, player, "minion_blastflame_dragon");
			playMinionCard(context, player, "minion_jhu_zho");
			assertEquals(player.getHand().size(), 1);
			assertEquals(player.getHand().get(0), minionInDeck);
		});
		runGym((context, player, opponent) -> {
			Card inHand = receiveCard(context, player, "spell_pay_respects");
			shuffleToDeck(context, player, "spell_test_deal_6");
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
			playCard(context, opponent, "spell_test_deal_6", lake);
			assertEquals(lake.getHp(), lake.getBaseHp());
			assertEquals(player.getHero().getHp(), 4);
			context.endTurn();
			context.endTurn();
			playCard(context, opponent, "spell_test_deal_6", lake);
			assertTrue(lake.isDestroyed());
		}));
	}

	// River Spirit: "Battlecry: If you control another Elemental, summon an Elemental from your hand.",
	@Test
	public void testRiverSpirit() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_lake_elemental");
			Card elemental = receiveCard(context, player, "minion_crystal_giant");
			receiveCard(context, player, "minion_rapier_rodent");
			playMinionCard(context, player, "minion_river_spirit");
			assertEquals(player.getMinions().size(), 3);
			assertEquals(player.getMinions().get(2).getSourceCard(), elemental);
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
			context.getLogic().castSpell(player.getId(), spell, player.getReference(), null, TargetSelection.NONE, false, null);
			context.getLogic().endOfSequence();
			Card heroPower = player.getHeroPowerZone().get(0);
			assertEquals(heroPower.getCardId(), "hero_power_hone_reflexes");
			player.setMana(20);
			assertTrue(context.getLogic().canPlayCard(player.getId(), heroPower.getReference()));
			useHeroPower(context, player);
			assertFalse(context.getLogic().canPlayCard(player.getId(), heroPower.getReference()));
			Minion sprite = playMinionCard(context, player, "minion_shy_sprite");
			context.endTurn();
			player.getHero().setHp(10);
			playCard(context, opponent, "spell_test_deal_6", sprite);
			assertFalse(sprite.isDestroyed());
			assertEquals(player.getHero().getHp(), 4);
			context.endTurn();
			player.setMana(20);
			assertTrue(context.getLogic().canPlayCard(player.getId(), heroPower.getReference()));
			useHeroPower(context, player);
			assertTrue(context.getLogic().canPlayCard(player.getId(), heroPower.getReference()));
			useHeroPower(context, player);
			assertFalse(context.getLogic().canPlayCard(player.getId(), heroPower.getReference()));
		});
	}

	// The Uncasked: "Battlecry: Draw Elementals from your deck until your hand is full.",
	@Test
	public void testTheUncasked() {
		runGym(((context, player, opponent) -> {
			assertEquals(player.getHand().size(), 0);
			shuffleToDeck(context, player, "minion_lake_elemental");
			shuffleToDeck(context, player, "minion_rapier_rodent");
			shuffleToDeck(context, player, "minion_alemental");
			playMinionCard(context, player, "minion_the_uncasked");
			assertEquals(player.getHand().size(), 2);
			assertEquals(player.getHand().get(0).getRace(), "ELEMENTAL");
			assertEquals(player.getHand().get(1).getRace(), "ELEMENTAL");
		}));
	}

	// Spell Hone Reflexes: "Your Hero Power becomes 'Gain +3 Attack this turn."
	@Test
	public void testSpellHoneReflexes() {
		runGym(((context, player, opponent) -> {
			assertNotEquals(player.getHeroPowerZone().get(0).getCardId(), "hero_power_hone_reflexes");
			playCard(context, player, "spell_hone_reflexes");
			assertEquals(player.getHeroPowerZone().get(0).getCardId(), "hero_power_hone_reflexes");
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
		// test in case of spellpower
		runGym(((context, player, opponent) -> {
			context.endTurn();
			Minion target1 = playMinionCard(context, opponent, "minion_neutral_test_14");
			context.endTurn();
			opponent.getHero().setHp(opponent.getHero().getBaseHp());
			player.getHero().setAttack(2);
			playMinionCard(context, player, "minion_floating_crystal");
			playCard(context, player, "spell_touch_of_death");
			assertEquals(opponent.getHero().getHp() + target1.getHp(), opponent.getHero().getBaseHp() + target1.getBaseHp() - 6);
		}));
	}

	// Zen Pilgrimage: "Shuffle a friendly minion into your deck. Add Emerald Secrets to your hand equal to its cost."
	@Test
	public void testZenPilgrimage() {
		runGym(((context, player, opponent) -> {
			Minion friend1 = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "spell_zen_pilgrimage", friend1);
			assertEquals(player.getHand().size(), 2);
			assertTrue(player.getHand().get(0).getCardId().contains("secret_secret_of"));
			assertTrue(player.getHand().get(1).getCardId().contains("secret_secret_of"));
			assertEquals(player.getMinions().size(), 0);
		}));
		runGym(((context, player, opponent) -> {
			Minion friend1 = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "spell_zen_pilgrimage", friend1);
			assertEquals(player.getHand().size(), 2);
			assertTrue(player.getHand().get(0).getCardId().contains("secret_secret_of"));
			assertTrue(player.getHand().get(1).getCardId().contains("secret_secret_of"));
			assertEquals(player.getMinions().size(), 0);
		}));
	}

	// Starlight Shawl: "After a friendly minion Deflects, prevent the damage and lose 1 Durability."
	@Test
	public void testStarlightShawl() {
		runGym(((context, player, opponent) -> {
			playCard(context, player, "weapon_starlight_shawl");
			Minion deflectMinion = playMinionCard(context, player, "minion_test_deflect");
			playCard(context, player, "spell_test_deal_6", deflectMinion);
			assertFalse(deflectMinion.isDestroyed());
			assertEquals(player.getHero().getHp(), player.getHero().getMaxHp());
			assertEquals(player.getWeaponZone().get(0).getDurability(), player.getWeaponZone().get(0).getMaxDurability() - 1);
		}));

		runGym(((context, player, opponent) -> {
			playCard(context, player, "weapon_starlight_shawl");
			Minion deflectMinion = playMinionCard(context, opponent, "minion_test_deflect");
			playCard(context, player, "spell_test_deal_6", deflectMinion);
			assertFalse(deflectMinion.isDestroyed());
			assertEquals(player.getHero().getHp(), player.getHero().getMaxHp());
			assertEquals(player.getWeaponZone().get(0).getDurability(), player.getWeaponZone().get(0).getMaxDurability());
			assertEquals(opponent.getHero().getHp(), opponent.getHero().getMaxHp() - 6);
		}));
	}
}
