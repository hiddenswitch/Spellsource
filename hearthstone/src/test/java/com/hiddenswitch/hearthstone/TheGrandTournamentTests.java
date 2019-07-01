package com.hiddenswitch.hearthstone;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.ChangeHeroPowerSpell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.tests.util.DebugContext;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.Assert;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class TheGrandTournamentTests extends TestBase {

	@Test
	public void testSaboteur() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_saboteur");
			Card heroPower = opponent.getHeroPowerZone().get(0);
			Assert.assertEquals(costOf(context, opponent, heroPower), 2);
			context.endTurn();
			Assert.assertEquals(costOf(context, opponent, heroPower), 7);
			context.endTurn();
			context.endTurn();
			Assert.assertEquals(costOf(context, opponent, heroPower), 2);
		});
	}

	@Test
	public void testKnightOfTheWild() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_bloodfen_raptor");
			Card knight = receiveCard(context, player, "minion_knight_of_the_wild");
			playCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_bloodfen_raptor");
			Assert.assertEquals(costOf(context, player, knight), knight.getBaseManaCost() - 2);
		});
	}

	@Test
	public void testSideshowSpelleater() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_sideshow_spelleater");
			Assert.assertEquals(player.getHero().getHeroPower().getHeroClass(), opponent.getHero().getHeroPower().getHeroClass());
		}, HeroClass.BLACK, HeroClass.VIOLET);
	}

	@Test
	public void testDefileDreadsteedInteraction() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_dreadsteed");
			try {
				playCard(context, player, "spell_defile");
			} catch (Throwable ex) {
				Assert.fail("Should not throw exception", ex);
			}
		});
	}

	@Test
	public void testKingsDefenderAttendee() {
		runGym((context, player, opponent) -> {
			Hero hero = player.getHero();

			playCard(context, player, "weapon_deaths_bite");
			Assert.assertEquals(hero.getWeapon().getAttack(), 4);
			Assert.assertEquals(hero.getWeapon().getDurability(), 2);
			playCard(context, player, "minion_tournament_attendee");
			Assert.assertEquals(player.getMinions().size(), 1);
			playCard(context, player, "weapon_kings_defender");
			Assert.assertEquals(hero.getWeapon().getAttack(), 3);
			Assert.assertEquals(hero.getWeapon().getDurability(), 3);
			Assert.assertEquals(player.getMinions().size(), 0);
		});
	}


	@Test
	public void testEydisDarkbane() {
		runGym((context, priest, warrior) -> {
			Card eydisDarkbaneCard = CardCatalogue.getCardById("minion_eydis_darkbane");
			Minion eydisDarkbane = playMinionCard(context, priest, eydisDarkbaneCard);

			Card testSpellCard = CardCatalogue.getCardById("spell_power_word_shield");
			context.getLogic().receiveCard(priest.getId(), testSpellCard);
			GameAction spellAction = testSpellCard.play();
			spellAction.setTarget(eydisDarkbane);
			context.performAction(priest.getId(), spellAction);

			// priest casted a spell on Eydis - warrior should be wounded
			Assert.assertEquals(warrior.getHero().getHp(), warrior.getHero().getMaxHp() - 3);

			testSpellCard = CardCatalogue.getCardById("spell_shield_slam");
			context.getLogic().receiveCard(warrior.getId(), testSpellCard);
			spellAction = testSpellCard.play();
			spellAction.setTarget(eydisDarkbane);
			context.performAction(warrior.getId(), spellAction);

			// warrior casted a spell on Eydis - nothing should happen
			Assert.assertEquals(warrior.getHero().getHp(), warrior.getHero().getMaxHp() - 3);
		}, HeroClass.WHITE, HeroClass.RED);
	}

	@Test
	public void testNewJusticar() {
		CardCatalogue.getAll().filtered(Card::isHeroPower).forEach(heroPower -> {
			runGym((context, player, opponent) -> {
				SpellDesc spell = new SpellDesc(ChangeHeroPowerSpell.class);
				spell.put(SpellArg.CARD, heroPower.getCardId());
				context.getLogic().castSpell(player.getId(), spell, player.getReference(), null, false);

				playCard(context, player, "minion_justicar_trueheart");
				if (heroPower.getDesc().getHeroPower() != null) {
					assertEquals(player.getHeroPowerZone().get(0).getCardId(), heroPower.getDesc().getHeroPower());
				} else {
					assertEquals(player.getHeroPowerZone().get(0).getCardId(), heroPower.getCardId());
				}
			});
		});
	}
}
