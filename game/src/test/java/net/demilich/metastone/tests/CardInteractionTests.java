package net.demilich.metastone.tests;

import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.SilenceSpell;
import net.demilich.metastone.game.spells.SwapAttackAndHpSpell;
import net.demilich.metastone.game.spells.TemporaryAttackSpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;
import net.demilich.metastone.tests.util.TestBase;
import net.demilich.metastone.tests.util.TestMinionCard;
import net.demilich.metastone.tests.util.TestSpellCard;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class CardInteractionTests extends TestBase {

	@Test
	public void testAttackBuffStacking() {
		runGym((context, player, opponent) -> {
			// summon Ghaz'rilla
			Card gahzrillaCard = CardCatalogue.getCardById("minion_gahzrilla");
			Minion gahzrilla = playMinionCard(context, player, gahzrillaCard);
			assertEquals(gahzrilla.getAttack(), 6);
			assertEquals(gahzrilla.getHp(), 9);

			// buff it with 'Abusive Sergeant' spell
			// This temporary Attack boost should be doubled and removed after the turn
			Card abusiveSergeant = CardCatalogue.getCardById("minion_abusive_sergeant");
			context.getLogic().receiveCard(player.getId(), abusiveSergeant);
			GameAction action = abusiveSergeant.play();
			action.setTarget(gahzrilla);
			context.getLogic().performGameAction(player.getId(), action);
			assertEquals(gahzrilla.getAttack(), 8);
			assertEquals(gahzrilla.getHp(), 9);

			context.getLogic().destroy((Actor) find(context, "minion_abusive_sergeant"));

			// buff it with 'Cruel Taskmaster' spell
			Card cruelTaskmasterCard = CardCatalogue.getCardById("minion_cruel_taskmaster");
			context.getLogic().receiveCard(player.getId(), cruelTaskmasterCard);
			action = cruelTaskmasterCard.play();
			action.setTarget(gahzrilla);
			context.getLogic().performGameAction(player.getId(), action);
			assertEquals(gahzrilla.getAttack(), 20);
			assertEquals(gahzrilla.getHp(), 8);

			context.getLogic().destroy((Actor) find(context, "minion_cruel_taskmaster"));

			// buff it again with 'Abusive Sergeant' spell
			abusiveSergeant = CardCatalogue.getCardById("minion_abusive_sergeant");
			context.getLogic().receiveCard(player.getId(), abusiveSergeant);
			action = abusiveSergeant.play();
			action.setTarget(gahzrilla);
			context.getLogic().performGameAction(player.getId(), action);
			assertEquals(gahzrilla.getAttack(), 22);
			assertEquals(gahzrilla.getHp(), 8);

			context.endTurn();
			context.endTurn();
			assertEquals(gahzrilla.getAttack(), 16);
			assertEquals(gahzrilla.getHp(), 8);
		});
	}

	@Test
	public void testKnifeJugglerPlusStealth() {
		runGym((context, player, opponent) -> {
			Minion knifeJuggler = playMinionCard(context, player, "minion_knife_juggler");
			playCard(context, player, "spell_conceal");
			// knife juggler should be stealthed
			Assert.assertTrue(knifeJuggler.hasAttribute(Attribute.STEALTH));
			// knife juggler should be unstealthed as soon as another minion is
			// played and his trigger fires
			playCard(context, player, new TestMinionCard(1, 1));
			Assert.assertFalse(knifeJuggler.hasAttribute(Attribute.STEALTH));
		});
	}

	@Test
	public void testSilenceWithBuffs() {
		runGym((context, player, opponent) -> {
			// summon attack target
			context.endTurn();
			playCard(context, opponent, new TestMinionCard(4, 4, 0));
			context.endTurn();

			// summon test minion
			player.setMana(10);
			TestMinionCard Card = new TestMinionCard(6, 6, 0);
			playCard(context, player, Card);

			Actor minion = getSingleMinion(player.getMinions());

			// buff test minion
			Card buffCard = CardCatalogue.getCardById("spell_bananas");
			context.getLogic().receiveCard(player.getId(), buffCard);
			GameAction action = buffCard.play();
			action.setTarget(minion);
			context.getLogic().performGameAction(player.getId(), action);

			assertEquals(minion.getAttack(), 7);
			assertEquals(minion.getHp(), 7);

			// attack target to get test minion wounded
			attack(context, player, minion, getSingleMinion(opponent.getMinions()));
			assertEquals(minion.getAttack(), 7);
			assertEquals(minion.getHp(), 3);

			// swap hp and attack of wounded test minion
			SpellDesc swapHpAttackSpell = SwapAttackAndHpSpell.create(EntityReference.FRIENDLY_MINIONS);
			Card swapCard = new TestSpellCard(swapHpAttackSpell);
			buffCard.setTargetRequirement(TargetSelection.NONE);
			playCard(context, player, swapCard);
			assertEquals(minion.getAttack(), 3);
			assertEquals(minion.getHp(), 7);

			// silence minion and check if it regains original stats
			SpellDesc silenceSpell = SilenceSpell.create(EntityReference.FRIENDLY_MINIONS);
			Card silenceCard = new TestSpellCard(silenceSpell);
			silenceCard.setTargetRequirement(TargetSelection.NONE);
			playCard(context, player, silenceCard);
			assertEquals(minion.getAttack(), 6);
			assertEquals(minion.getHp(), 6);
		});
	}

	@Test
	public void testSwapWithBuffs() {
		runGym((context, player, opponent) -> {
			// summon test minion
			player.setMana(10);
			TestMinionCard Card = new TestMinionCard(1, 3, 0);
			playCard(context, player, Card);

			// buff test minion with temporary buff
			SpellDesc buffSpell = TemporaryAttackSpell.create(EntityReference.FRIENDLY_MINIONS, +4);
			Card buffCard = new TestSpellCard(buffSpell);
			buffCard.setTargetRequirement(TargetSelection.NONE);
			playCard(context, player, buffCard);

			Actor minion = getSingleMinion(player.getMinions());
			assertEquals(minion.getAttack(), 5);
			assertEquals(minion.getHp(), 3);

			// swap hp and attack of wounded test minion
			SpellDesc swapHpAttackSpell = SwapAttackAndHpSpell.create(EntityReference.FRIENDLY_MINIONS);
			Card swapCard = new TestSpellCard(swapHpAttackSpell);
			buffCard.setTargetRequirement(TargetSelection.NONE);
			playCard(context, player, swapCard);
			assertEquals(minion.getAttack(), 3);
			assertEquals(minion.getHp(), 5);

			// end turn; temporary buff wears off, but stats should still be the
			// same
			context.endTurn();
			assertEquals(minion.getAttack(), 3);
			assertEquals(minion.getHp(), 5);
		});
	}

	@Test
	public void testBloodsailRaider() {
		runGym((context, warrior, opponent) -> {
			warrior.setMana(10);

			playCard(context, warrior, "weapon_arcanite_reaper");
			playCard(context, warrior, new TestMinionCard(2, 1, 0));

			Minion bloodsailRaider = playMinionCard(context, warrior, "minion_bloodsail_raider");
			assertEquals(bloodsailRaider.getAttack(), 7);
		});
	}

	@Test
	public void testWildPyroPlusEquality() {
		runGym((context, player, opponent) -> {
			playCard(context, player, new TestMinionCard(3, 2, 0));
			playCard(context, player, new TestMinionCard(4, 4, 0));
			context.getLogic().endTurn(player.getId());

			playCard(context, opponent, new TestMinionCard(5, 5, 0));
			playCard(context, opponent, new TestMinionCard(1, 2, 0));
			playCard(context, opponent, new TestMinionCard(8, 8, 0));
			playCard(context, opponent, new TestMinionCard(2, 1, 0));
			context.getLogic().endTurn(opponent.getId());

			assertEquals(player.getMinions().size(), 2);
			assertEquals(opponent.getMinions().size(), 4);

			playCard(context, player, "minion_wild_pyromancer");
			playCard(context, player, "spell_equality");

			// wild pyromancer + equality should wipe the board if there no
			// deathrattles
			assertEquals(player.getMinions().size(), 0);
			assertEquals(opponent.getMinions().size(), 0);
		});
	}

	@Test
	public void testLordJaraxxusMirrorEntityInteraction() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_mirror_entity");
			context.endTurn();
			playCard(context, opponent, "minion_lord_jaraxxus");
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_lord_jaraxxus");
		});
	}

	@Test
	public void testLordJaraxxus() {
		runGym((context, player, opponent) -> {
			Card jaraxxus = CardCatalogue.getCardById("minion_lord_jaraxxus");
			// first, just play Jaraxxus on an empty board
			playCard(context, player, jaraxxus);
			assertEquals(player.getHero().getRace(), Race.DEMON);
			assertEquals(player.getHero().getHp(), 15);
			assertNotNull(player.getHero().getWeapon());
			// There should be no minions on the board in this case
			assertEquals(player.getMinions().size(), 0);

			// start a new game
			context = createContext(HeroClass.VIOLET, HeroClass.VIOLET);
			// opponent plays Repentance, which triggers on Lord Jaraxxus play
			player = context.getActivePlayer();
			context.endTurn();
			Card repentance = CardCatalogue.getCardById("secret_repentance");
			playCard(context, opponent, repentance);
			context.endTurn();
			jaraxxus = CardCatalogue.getCardById("minion_lord_jaraxxus");
			playCard(context, player, jaraxxus);
			assertEquals(player.getHero().getRace(), Race.DEMON);
			// Jaraxxus should be affected by Repentance, bringing him down to 1 hp
			assertEquals(player.getHero().getHp(), 1);
			assertNotNull(player.getHero().getWeapon());
		}, HeroClass.VIOLET, HeroClass.VIOLET);
	}

	@Test
	public void testBlessingOfWisdomMindControl() {
		runGym((context, player, opponent) -> {
			int cardCount = player.getHand().getCount();
			shuffleToDeck(context, player, "spell_the_coin");
			shuffleToDeck(context, player, "spell_the_coin");
			shuffleToDeck(context, opponent, "spell_the_coin");
			Minion minion = playMinionCard(context, player, "minion_chillwind_yeti");
			playCard(context, player, "spell_blessing_of_wisdom", minion);
			assertEquals(cardCount, player.getHand().getCount());

			attack(context, opponent, minion, opponent.getHero());
			assertEquals(player.getHand().getCount(), cardCount + 1);

			context.getLogic().mindControl(opponent, minion);
			attack(context, opponent, minion, player.getHero());
			assertEquals(player.getHand().getCount(), cardCount + 2);
		});
	}

	@Test
	public void testImpFlamestrike() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			for (int i = 0; i < GameLogic.MAX_MINIONS; i++) {
				playMinionCard(context, opponent, "minion_imp_gang_boss");
			}

			assertEquals(opponent.getMinions().size(), GameLogic.MAX_MINIONS);
			context.endTurn();

			playCard(context, player, "spell_flamestrike");
			assertEquals(opponent.getMinions().size(), 0);
		});
	}

	@Test
	public void testHarvestGolemFlamestrike() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			for (int i = 0; i < GameLogic.MAX_MINIONS; i++) {
				playMinionCard(context, opponent, "minion_harvest_golem");
			}

			assertEquals(opponent.getMinions().size(), GameLogic.MAX_MINIONS);
			context.endTurn();

			playCard(context, player, "spell_flamestrike");
			assertEquals(opponent.getMinions().size(), 7);
		});
	}

	@Test
	public void testGrimPatrons() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			for (int i = 0; i < 4; i++) {
				playMinionCard(context, opponent, "minion_grim_patron");
			}

			assertEquals(opponent.getMinions().size(), 4);
			playCard(context, opponent, "spell_whirlwind");
			assertEquals(opponent.getMinions().size(), 7);
			context.endTurn();

			playCard(context, player, "spell_consecration");
			assertEquals(opponent.getMinions().size(), 3);
		});
	}

	@Test
	public void testWobblingRunts() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			playMinionCard(context, opponent, "minion_wobbling_runts");
			for (int i = 0; i < GameLogic.MAX_MINIONS - 1; i++) {
				playMinionCard(context, opponent, "minion_wisp");
			}

			assertEquals(opponent.getMinions().size(), GameLogic.MAX_MINIONS);
			context.endTurn();

			playCard(context, player, "minion_malygos");
			playCard(context, player, "spell_flamestrike");
			assertEquals(opponent.getMinions().size(), 3);
		});
	}
}

