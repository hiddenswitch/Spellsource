package net.demilich.metastone.tests;

import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.decks.FixedCardsDeckFormat;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.tests.util.TestBase;
import net.demilich.metastone.tests.util.TestMinionCard;
import net.demilich.metastone.tests.util.TestSecretCard;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class SecretTest extends TestBase {

	@Test
	public void testKillingStopsAttack() {
		runGym((context, player, opponent) -> {
			final int SECRET_DAMAGE = 2;
			playCard(context, player, new TestSecretCard(SECRET_DAMAGE));
			playCard(context, opponent, new TestMinionCard(2, 3));

			context.setActivePlayerId(opponent.getId());
			Actor minion = getSingleMinion(opponent.getMinions());
			attack(context, opponent, minion, player.getHero());
			assertEquals(player.getHero().getHp(), player.getHero().getMaxHp() - minion.getAttack());
			assertEquals(minion.getHp(), minion.getMaxHp() - SECRET_DAMAGE);

			playCard(context, player, new TestSecretCard(SECRET_DAMAGE));
			attack(context, opponent, minion, player.getHero());
			assertTrue(minion.isDestroyed());
			assertEquals(player.getHero().getHp(), player.getHero().getMaxHp() - minion.getAttack());
		}, HeroClass.BLUE, HeroClass.RED);
	}

	@Test
	public void testNewSpellTarget() {
		runGym((context, player, opponent) -> {
			int fullHp = 10;
			playCard(context, opponent, new TestMinionCard(2, fullHp));

			Actor minion = getSingleMinion(opponent.getMinions());
			context.getLogic().endTurn(player.getId());

			for (int i = 0; i < 2; i++) {
				playCard(context, player, "secret_spellbender");
				assertEquals(player.getSecrets().size(), 1);

				Card testSpellCard = receiveCard(context, opponent, "spell_frostbolt");
				GameAction spellAttackAction = testSpellCard.play();
				spellAttackAction.setTarget(minion);

				context.setActivePlayerId(opponent.getId());
				context.getLogic().performGameAction(opponent.getId(), spellAttackAction);

				assertEquals(minion.getHp(), fullHp);
				assertEquals(opponent.getMinions().size(), 1);

				attack(context, opponent, minion, player.getHero());
			}
		});
	}

	@Test
	public void testPlayOnlyOnce() {
		runGym((context, player, opponent) -> {
			Card secretCard = new TestSecretCard();
			context.getLogic().receiveCard(player.getId(), secretCard);
			assertTrue(context.getLogic().canPlaySecret(player, secretCard));
			context.getLogic().performGameAction(player.getId(), secretCard.play());

			Card secretCard2 = new TestSecretCard();
			context.getLogic().receiveCard(player.getId(), secretCard2);
			assertFalse(context.getLogic().canPlaySecret(player, secretCard2));

			Card otherSecret = receiveCard(context, player, "secret_explosive_trap");
			assertTrue(context.getLogic().canPlaySecret(player, otherSecret));
		});
	}

	@Test
	public void testDuplicate() {
		runGym((context, player, opponent) -> {
			context.setDeckFormat(new FixedCardsDeckFormat("minion_novice_engineer", "secret_duplicate", "weapon_fiery_war_axe"));
			playCard(context, player, "secret_duplicate");

			Minion novice = playMinionCard(context, player, "minion_novice_engineer");
			while (player.getHand().getCount() < GameLogic.MAX_HAND_CARDS) {
				receiveCard(context, player, "minion_novice_engineer");
			}
			assertEquals(player.getHand().getCount(), GameLogic.MAX_HAND_CARDS);
			context.endTurn();
			playCard(context, opponent, "weapon_fiery_war_axe");

			attack(context, opponent, opponent.getHero(), novice);
			// player has full hand, therefor Duplicate should not have triggered
			assertEquals(player.getSecrets().size(), 1);
		});
	}

	@Test
	public void testExplosivePlusFreezingTrap() {
		runGym((context, player, opponent) -> {
			Minion minion = playMinionCard(context, player, "minion_wisp");
			context.endTurn();
			playCard(context, opponent, "secret_explosive_trap");
			playCard(context, opponent, "secret_freezing_trap");
			context.endTurn();

			assertEquals(player.getMinions().size(), 1);
			assertEquals(opponent.getSecrets().size(), 2);

			attack(context, player, minion, opponent.getHero());
			assertEquals(player.getMinions().size(), 0);
			assertEquals(opponent.getSecrets().size(), 1);
		});
	}

	@Test
	public void testFreezingPlusBearTrap() {
		runGym((context, player, opponent) -> {
			Minion minion = playMinionCard(context, player, "minion_wisp");
			context.endTurn();
			playCard(context, opponent, "secret_freezing_trap");
			playCard(context, opponent, "secret_bear_trap");

			context.endTurn();

			assertEquals(player.getMinions().size(), 1);
			assertEquals(opponent.getSecrets().size(), 2);

			attack(context, player, minion, opponent.getHero());
			assertEquals(player.getMinions().size(), 0);
			assertEquals(opponent.getSecrets().size(), 1);
		});
	}

	@Test
	public void testIceBlockWithArmor() {
		runGym((context, player, opponent) -> {
			player.getHero().setHp(3);
			player.getHero().setAttribute(Attribute.ARMOR, 10);
			playCard(context, player, "secret_ice_block");
			context.endTurn();

			playCard(context, opponent, "spell_bash", player);
			assertEquals(player.getSecrets().size(), 1, "Ice block should not have triggered, as the Mage had enough armor to prevent fatal damage");
			assertFalse(player.getHero().hasAttribute(Attribute.IMMUNE));
		});
	}

	@Test
	public void testAvenge() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_avenge");
			playCard(context, player, "minion_murloc_raider");
			Minion minion = playMinionCard(context, player, "minion_murloc_raider");
			assertEquals(player.getSecrets().size(), 1);
			context.endTurn();
			playCard(context, opponent, "spell_bash", minion);
			assertEquals(player.getSecrets().size(), 0);
		});
	}
}
