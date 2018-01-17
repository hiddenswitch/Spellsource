package com.blizzard.hearthstone;

import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.actions.PhysicalAttackAction;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.DamageSpell;
import net.demilich.metastone.game.spells.DestroySpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;
import net.demilich.metastone.tests.util.TestMinionCard;
import net.demilich.metastone.tests.util.TestSpellCard;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.tests.util.TestBase;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;

public class ClassicTests extends TestBase {
	@Test
	public void testNourish() {
		runGym((context, player, opponent) -> {
			player.setMana(5);
			player.setMaxMana(5);
			ChooseOneCard nourish = (ChooseOneCard) CardCatalogue.getCardById("spell_nourish");
			playCard(context, player, nourish.getChoiceCards()[0]);
			Assert.assertEquals(player.getMaxMana(), 7);
			Assert.assertEquals(player.getMana(), 2);
		});

		runGym((context, player, opponent) -> {
			for (int i = 0; i < 3; i++) {
				context.getLogic().shuffleToDeck(player, CardCatalogue.getCardById("minion_bloodfen_raptor"));
			}
			player.setMana(5);
			player.setMaxMana(5);
			ChooseOneCard nourish = (ChooseOneCard) CardCatalogue.getCardById("spell_nourish");
			playCard(context, player, nourish.getChoiceCards()[1]);
			Assert.assertEquals(player.getMaxMana(), 5);
			Assert.assertEquals(player.getMana(), 0);
			Assert.assertEquals(player.getHand().size(), 3);
		});

		runGym((context, player, opponent) -> {
			for (int i = 0; i < 3; i++) {
				context.getLogic().shuffleToDeck(player, CardCatalogue.getCardById("minion_bloodfen_raptor"));
			}
			player.setMana(5);
			player.setMaxMana(5);
			ChooseOneCard nourish = (ChooseOneCard) CardCatalogue.getCardById("spell_nourish");
			playCard(context, player, nourish.getBothChoicesCard());
			Assert.assertEquals(player.getMaxMana(), 7);
			Assert.assertEquals(player.getMana(), 2);
			Assert.assertEquals(player.getHand().size(), 3);
		});
	}

	@Test
	public void testTracking() {
		for (int i = 0; i <= 3; i++) {
			final int i1 = i;
			runGym((context, player, opponent) -> {
				Collections.nCopies(i1, "minion_bloodfen_raptor")
						.forEach(cid -> context.getLogic().shuffleToDeck(player, CardCatalogue.getCardById(cid)));

				playCard(context, player, "spell_tracking");
				Assert.assertEquals(player.getDeck().size(), 0);
				if (i1 > 0) {
					Assert.assertEquals(player.getHand().get(0).getCardId(), "minion_bloodfen_raptor");
				} else {
					Assert.assertEquals(player.getHand().size(), 0);
				}
			});
		}
	}

	@Test
	public void testMisdirection() {
		// Opponent's face gets hit always by misdirection
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_misdirection");
			context.endTurn();
			int startingHp = opponent.getHero().getHp();
			Minion boar = playMinionCard(context, player, "minion_stonetusk_boar");
			attack(context, opponent, boar, player.getHero());
			Assert.assertEquals(player.getSecrets().size(), 0);
			Assert.assertEquals(opponent.getHero().getHp(), startingHp - 1);
		});

		// Adjacent minion can get hit by misdirection
		runGym((context, player, opponent) -> {
			context.setLogic(spy(context.getLogic()));

			playCard(context, player, "secret_misdirection");
			context.endTurn();
			Minion boar = playMinionCard(context, player, "minion_stonetusk_boar");
			Minion boar2 = playMinionCard(context, player, "minion_stonetusk_boar");
			Mockito.doReturn(boar2)
					.when(context.getLogic())
					.getAnotherRandomTarget(any(), any(), any(), any());
			attack(context, opponent, boar, player.getHero());
			Assert.assertEquals(player.getSecrets().size(), 0);
			Assert.assertEquals(opponent.getMinions().size(), 0);
		});
	}

	@Test
	public void testBlizzard() {
		GameContext context = createContext(HeroClass.BLUE, HeroClass.VIOLET);
		Player player = context.getPlayer1();
		Player opponent = context.getPlayer2();

		context.endTurn();
		Minion impGangBoss = playMinionCard(context, opponent, (MinionCard) CardCatalogue.getCardById("minion_imp_gang_boss"));
		context.endTurn();

		playCard(context, player, CardCatalogue.getCardById("spell_blizzard"));

		Assert.assertEquals(impGangBoss.getHp(), impGangBoss.getMaxHp() - 2);
		for (Minion minion : opponent.getMinions()) {
			Assert.assertEquals(minion.hasAttribute(Attribute.FROZEN), true);
		}
	}

	@Test
	public void testHauntedCreeperHarvestGolem() {
		GameContext context = createContext(HeroClass.BLUE, HeroClass.RED);
		Player player = context.getPlayer1();
		Player opponent = context.getPlayer2();

		context.endTurn();
		playMinionCard(context, opponent, (MinionCard) CardCatalogue.getCardById("minion_haunted_creeper"));
		playMinionCard(context, opponent, (MinionCard) CardCatalogue.getCardById("minion_harvest_golem"));
		Assert.assertEquals(opponent.getMinions().size(), 2);
		context.endTurn();

		playCard(context, player, CardCatalogue.getCardById("spell_flamestrike"));
		Assert.assertEquals(opponent.getMinions().size(), 3);
		final int HARVEST_GOLEM = 1;
		for (int i = 0; i < opponent.getMinions().size(); i++) {
			Minion minion = opponent.getMinions().get(i);
			if (i == HARVEST_GOLEM) {
				Assert.assertEquals(minion.getAttack(), 2);
				Assert.assertEquals(minion.getHp(), 1);
				Assert.assertEquals(minion.getRace(), Race.MECH);
			} else {
				Assert.assertEquals(minion.getAttack(), 1);
				Assert.assertEquals(minion.getHp(), 1);
			}
		}
	}


	@Test
	public void testElvenArcher() {
		GameContext context = createContext(HeroClass.RED, HeroClass.SILVER);
		Player player = context.getPlayer1();
		Player opponent = context.getPlayer2();

		Assert.assertEquals(opponent.getHero().getHp(), GameLogic.MAX_HERO_HP);
		MinionCard elvenArcherCard = (MinionCard) CardCatalogue.getCardById("minion_elven_archer");
		playCardWithTarget(context, player, elvenArcherCard, opponent.getHero());
		Assert.assertEquals(opponent.getHero().getHp(), GameLogic.MAX_HERO_HP - 1);
	}

	@Test
	public void testNoviceEngineer() {
		GameContext context = createContext(HeroClass.RED, HeroClass.SILVER);
		Player player = context.getPlayer1();

		int cardCount = player.getHand().getCount();
		Assert.assertEquals(player.getHand().getCount(), cardCount);
		Card noviceEngineerCard = CardCatalogue.getCardById("minion_novice_engineer");
		playCard(context, player, noviceEngineerCard);
		Assert.assertEquals(player.getHand().getCount(), cardCount + 1);
	}

	@Test
	public void testKoboldGeomancer() {
		GameContext context = createContext(HeroClass.BLUE, HeroClass.RED);
		Player player = context.getPlayer1();
		Player opponent = context.getPlayer2();

		Assert.assertEquals(opponent.getHero().getHp(), GameLogic.MAX_HERO_HP);
		playCard(context, player, CardCatalogue.getCardById("spell_arcane_missiles"));
		Assert.assertEquals(opponent.getHero().getHp(), GameLogic.MAX_HERO_HP - 3);
		playCardWithTarget(context, player, CardCatalogue.getCardById("spell_fireball"), opponent.getHero());
		Assert.assertEquals(opponent.getHero().getHp(), GameLogic.MAX_HERO_HP - 3 - 6);

		playCard(context, player, CardCatalogue.getCardById("minion_kobold_geomancer"));

		playCard(context, player, CardCatalogue.getCardById("spell_arcane_missiles"));
		Assert.assertEquals(opponent.getHero().getHp(), GameLogic.MAX_HERO_HP - 3 - 6 - 4);
		playCardWithTarget(context, player, CardCatalogue.getCardById("spell_fireball"), opponent.getHero());
		Assert.assertEquals(opponent.getHero().getHp(), GameLogic.MAX_HERO_HP - 3 - 6 - 4 - 7);
	}

	@Test
	public void testAcidicSwampOoze() {
		GameContext context = createContext(HeroClass.BLUE, HeroClass.RED);
		Player player = context.getPlayer1();
		Player opponent = context.getPlayer2();

		context.endTurn();
		playCard(context, opponent, CardCatalogue.getCardById("weapon_fiery_war_axe"));
		Assert.assertNotNull(opponent.getHero().getWeapon());
		context.endTurn();

		playCard(context, player, CardCatalogue.getCardById("minion_acidic_swamp_ooze"));
		Assert.assertNull(opponent.getHero().getWeapon());
	}

	@Test
	public void testWildPyromancer() {
		GameContext context = createContext(HeroClass.WHITE, HeroClass.RED);
		Player warrior = context.getPlayer2();
		Card hauntedCreeper = CardCatalogue.getCardById("minion_haunted_creeper");
		playCard(context, warrior, hauntedCreeper);

		Player priest = context.getPlayer1();
		Card wildPyromancer = CardCatalogue.getCardById("minion_wild_pyromancer");
		playCard(context, priest, wildPyromancer);

		Assert.assertEquals(warrior.getMinions().size(), 1);

		Card holyNova = CardCatalogue.getCardById("spell_holy_nova");
		playCard(context, priest, holyNova);

		// the warriors board should be completely wiped, as the Holy Nova
		// should kill the
		// first body of Haunted Creeper, the Deathrattle resolves and then Wild
		// Pyromancer
		// triggers, clearing the two 1/1 Spectral Spiders
		Assert.assertEquals(warrior.getMinions().size(), 0);
	}

	@Test
	public void testBetrayal() {
		GameContext context = createContext(HeroClass.GOLD, HeroClass.BLACK);
		Player paladin = context.getPlayer1();

		MinionCard adjacentMinionCard1 = new TestMinionCard(1, 5, 0);
		Minion adjacentMinion1 = playMinionCard(context, paladin, adjacentMinionCard1);

		MinionCard targetMinionCard = new TestMinionCard(3, 1, 0);
		Minion targetMinion = playMinionCard(context, paladin, targetMinionCard);

		MinionCard adjacentMinionCard2 = new TestMinionCard(1, 5, 0);
		Minion adjacentMinion2 = playMinionCard(context, paladin, adjacentMinionCard2);

		context.getLogic().endTurn(paladin.getId());

		Assert.assertEquals(paladin.getMinions().size(), 3);

		Player rogue = context.getPlayer2();
		Card betrayal = CardCatalogue.getCardById("spell_betrayal");

		context.getLogic().receiveCard(rogue.getId(), betrayal);
		GameAction action = betrayal.play();
		action.setTarget(targetMinion);
		context.getLogic().performGameAction(rogue.getId(), action);
		Assert.assertEquals(targetMinion.getAttack(), 3);
		Assert.assertEquals(targetMinion.getHp(), 1);

		Assert.assertEquals(adjacentMinion1.getHp(), 2);
		Assert.assertEquals(adjacentMinion2.getHp(), 2);

		Assert.assertEquals(paladin.getMinions().size(), 3);
	}

	@Test
	public void testBetrayalOnEmperorCobraDestroysAdjacentMinions() {
		GameContext context = createContext(HeroClass.GOLD, HeroClass.BLACK);
		Player paladin = context.getPlayer1();

		MinionCard adjacentMinionCard1 = new TestMinionCard(1, 5, 0);
		playMinionCard(context, paladin, adjacentMinionCard1);

		MinionCard targetMinionCard = (MinionCard) CardCatalogue.getCardById("minion_emperor_cobra");
		Minion targetMinion = playMinionCard(context, paladin, targetMinionCard);

		MinionCard adjacentMinionCard2 = new TestMinionCard(1, 5, 0);
		playMinionCard(context, paladin, adjacentMinionCard2);

		context.getLogic().endTurn(paladin.getId());

		Assert.assertEquals(paladin.getMinions().size(), 3);

		Player rogue = context.getPlayer2();

		Card betrayal = CardCatalogue.getCardById("spell_betrayal");

		context.getLogic().receiveCard(rogue.getId(), betrayal);
		GameAction action = betrayal.play();
		action.setTarget(targetMinion);
		context.getLogic().performGameAction(rogue.getId(), action);

		Assert.assertEquals(paladin.getMinions().size(), 1);
	}


	@Test
	public void testBetrayalNotAffectedBySpellDamage() {
		GameContext context = createContext(HeroClass.GOLD, HeroClass.BLACK);
		Player paladin = context.getPlayer1();

		MinionCard adjacentMinionCard1 = new TestMinionCard(1, 5, 0);
		Minion adjacentMinion1 = playMinionCard(context, paladin, adjacentMinionCard1);

		MinionCard targetMinionCard = new TestMinionCard(3, 1, 0);
		Minion targetMinion = playMinionCard(context, paladin, targetMinionCard);

		MinionCard adjacentMinionCard2 = new TestMinionCard(1, 5, 0);
		Minion adjacentMinion2 = playMinionCard(context, paladin, adjacentMinionCard2);

		context.getLogic().endTurn(paladin.getId());

		Player rogue = context.getPlayer2();

		MinionCard azureDrakeCard = (MinionCard) CardCatalogue.getCardById("minion_azure_drake");
		playMinionCard(context, rogue, azureDrakeCard);

		Card betrayal = CardCatalogue.getCardById("spell_betrayal");

		context.getLogic().receiveCard(rogue.getId(), betrayal);
		GameAction action = betrayal.play();
		action.setTarget(targetMinion);
		context.getLogic().performGameAction(rogue.getId(), action);
		Assert.assertEquals(targetMinion.getAttack(), 3);
		Assert.assertEquals(targetMinion.getHp(), 1);

		Assert.assertEquals(adjacentMinion1.getHp(), 2);
		Assert.assertEquals(adjacentMinion2.getHp(), 2);
	}

	@Test
	public void testSummoningPortal() {
		GameContext context = createContext(HeroClass.VIOLET, HeroClass.RED);
		Player player = context.getPlayer1();
		player.setMana(10);

		Card summoningPortal1 = CardCatalogue.getCardById("minion_summoning_portal");
		context.getLogic().receiveCard(player.getId(), summoningPortal1);
		Card summoningPortal2 = CardCatalogue.getCardById("minion_summoning_portal");
		context.getLogic().receiveCard(player.getId(), summoningPortal2);

		MinionCard testMinionCard = new TestMinionCard(1, 1, 4);
		context.getLogic().receiveCard(player.getId(), testMinionCard);
		Assert.assertEquals(player.getMana(), 10);

		// first summoning portal costs full 4 mana
		context.getLogic().performGameAction(player.getId(), summoningPortal1.play());
		Assert.assertEquals(player.getMana(), 6);

		// second summoning portal affected by first one, costs only 2 mana
		context.getLogic().performGameAction(player.getId(), summoningPortal2.play());
		Assert.assertEquals(player.getMana(), 4);

		// base cost of minion card is 4, reduced by both summoning portals, but
		// not below 1
		context.getLogic().performGameAction(player.getId(), testMinionCard.play());
		Assert.assertEquals(player.getMana(), 3);

	}

	@Test
	public void testSpitefulSmith() {
		GameContext context = createContext(HeroClass.RED, HeroClass.RED);
		Player player = context.getPlayer1();
		player.setMana(10);

		Card fieryWarAxe = CardCatalogue.getCardById("weapon_fiery_war_axe");
		playCard(context, player, fieryWarAxe);

		Assert.assertTrue(player.getHero().getWeapon() != null);
		Assert.assertEquals(player.getHero().getWeapon().getWeaponDamage(), 3);

		MinionCard spitefulSmithCard = (MinionCard) CardCatalogue.getCardById("minion_spiteful_smith");
		Minion spitefulSmith = playMinionCard(context, player, spitefulSmithCard);
		// Smith has been played, but is not enraged yet, so weapon damage
		// should still be unaltered
		Assert.assertEquals(player.getHero().getWeapon().getWeaponDamage(), 3);

		SpellCard damageSpell = new TestSpellCard(DamageSpell.create(1));
		damageSpell.setTargetRequirement(TargetSelection.ANY);
		context.getLogic().receiveCard(player.getId(), damageSpell);
		GameAction spellAction = damageSpell.play();
		spellAction.setTarget(spitefulSmith);
		context.getLogic().performGameAction(player.getId(), spellAction);

		// Smith is damaged now, so weapon should be buffed
		Assert.assertEquals(player.getHero().getWeapon().getWeaponDamage(), 5);

		// equip a new weapon; this one should get buffed too
		fieryWarAxe = CardCatalogue.getCardById("weapon_fiery_war_axe");
		playCard(context, player, fieryWarAxe);
		Assert.assertEquals(player.getHero().getWeapon().getWeaponDamage(), 5);

		// wipe everything
		SpellDesc wipeSpell = DestroySpell.create(EntityReference.ALL_MINIONS);
		SpellCard wipe = new TestSpellCard(wipeSpell);
		playCard(context, player, wipe);

		// Smith is destroyed, weapon power should be back to normal
		Assert.assertEquals(player.getHero().getWeapon().getWeaponDamage(), 3);
	}


	@Test
	public void testFaerieDragon() {
		GameContext context = createContext(HeroClass.BLUE, HeroClass.RED);
		Player mage = context.getPlayer1();
		mage.setMana(10);
		Player warrior = context.getPlayer2();
		warrior.setMana(10);

		MinionCard faerieDragonCard = (MinionCard) CardCatalogue.getCardById("minion_faerie_dragon");
		context.getLogic().receiveCard(warrior.getId(), faerieDragonCard);
		context.getLogic().performGameAction(warrior.getId(), faerieDragonCard.play());

		MinionCard devMonsterCard = new TestMinionCard(1, 1);
		context.getLogic().receiveCard(mage.getId(), devMonsterCard);
		context.getLogic().performGameAction(mage.getId(), devMonsterCard.play());

		Entity attacker = getSingleMinion(mage.getMinions());
		Actor elusiveOne = getSingleMinion(warrior.getMinions());

		GameAction attackAction = new PhysicalAttackAction(attacker.getReference());
		List<Entity> validTargets = context.getLogic().getValidTargets(warrior.getId(), attackAction);
		// should be two valid targets: enemy hero and faerie dragon
		Assert.assertEquals(validTargets.size(), 2);

		GameAction useFireblast = mage.getHero().getHeroPower().play();
		validTargets = context.getLogic().getValidTargets(mage.getId(), useFireblast);
		// should be three valid targets, both heroes + minion which is not the
		// faerie dragon
		Assert.assertEquals(validTargets.size(), 3);
		Assert.assertFalse(validTargets.contains(elusiveOne));

		Card arcaneExplosionCard = CardCatalogue.getCardById("spell_arcane_explosion");
		context.getLogic().receiveCard(mage.getId(), arcaneExplosionCard);
		int faerieDragonHp = elusiveOne.getHp();
		context.getLogic().performGameAction(mage.getId(), arcaneExplosionCard.play());
		// hp should been affected after playing area of effect spell
		Assert.assertNotEquals(faerieDragonHp, elusiveOne.getHp());
	}

	@Test
	public void testGurubashiBerserker() {
		GameContext context = createContext(HeroClass.BLUE, HeroClass.RED);
		Player mage = context.getPlayer1();
		mage.setMana(10);
		Player warrior = context.getPlayer2();
		warrior.setMana(10);

		final int BASE_ATTACK = 2;
		final int ATTACK_BONUS = 3;

		MinionCard gurubashiBerserkerCard = (MinionCard) CardCatalogue.getCardById("minion_gurubashi_berserker");
		playCard(context, warrior, gurubashiBerserkerCard);

		MinionCard oasisSnapjawCard = (MinionCard) CardCatalogue.getCardById("minion_oasis_snapjaw");
		playCard(context, mage, oasisSnapjawCard);

		Actor attacker = getSingleMinion(mage.getMinions());
		Actor defender = getSingleMinion(warrior.getMinions());

		// Gurubashi Berserker should start with just his base attack
		Assert.assertEquals(defender.getAttack(), BASE_ATTACK);

		// first attack, Gurubashi Berserker should have increased attack
		GameAction attackAction = new PhysicalAttackAction(attacker.getReference());
		attackAction.setTarget(defender);
		context.getLogic().performGameAction(mage.getId(), attackAction);

		Assert.assertEquals(attacker.getHp(), attacker.getMaxHp() - BASE_ATTACK);
		Assert.assertEquals(defender.getHp(), defender.getMaxHp() - attacker.getAttack());
		Assert.assertEquals(defender.getAttack(), BASE_ATTACK + ATTACK_BONUS);

		// second attack, Gurubashi Berserker should become even stronger
		context.getLogic().performGameAction(mage.getId(), attackAction);
		Assert.assertEquals(attacker.getHp(), attacker.getMaxHp() - 2 * BASE_ATTACK - ATTACK_BONUS);
		Assert.assertEquals(defender.getHp(), defender.getMaxHp() - 2 * attacker.getAttack());
		Assert.assertEquals(defender.getAttack(), BASE_ATTACK + 2 * ATTACK_BONUS);
	}

	@Test
	public void testSavageRoar() {
		GameContext context = createContext(HeroClass.BROWN, HeroClass.RED);
		Player player = context.getPlayer1();
		Hero druid = player.getHero();

		player.setMana(10);
		Player warrior = context.getPlayer2();
		warrior.setMana(10);

		MinionCard devMonsterCard = new TestMinionCard(1, 1);
		context.getLogic().receiveCard(player.getId(), devMonsterCard);
		context.getLogic().performGameAction(player.getId(), devMonsterCard.play());

		Actor minion = getSingleMinion(player.getMinions());

		context.getLogic().performGameAction(player.getId(), druid.getHeroPower().play());
		Assert.assertEquals(druid.getAttack(), 1);
		Assert.assertEquals(minion.getAttack(), 1);

		Card savageRoar = CardCatalogue.getCardById("spell_savage_roar");
		context.getLogic().receiveCard(player.getId(), savageRoar);
		context.getLogic().performGameAction(player.getId(), savageRoar.play());
		Assert.assertEquals(druid.getAttack(), 3);
		Assert.assertEquals(minion.getAttack(), 3);

		context.getLogic().endTurn(player.getId());
		Assert.assertEquals(druid.getAttack(), 0);
		Assert.assertEquals(minion.getAttack(), 1);

		context.getLogic().endTurn(player.getId());
		Assert.assertEquals(druid.getAttack(), 0);
		Assert.assertEquals(minion.getAttack(), 1);
	}
}
