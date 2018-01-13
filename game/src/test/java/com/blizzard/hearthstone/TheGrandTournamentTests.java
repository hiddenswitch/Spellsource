package com.blizzard.hearthstone;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.tests.util.DebugContext;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.Assert;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

public class TheGrandTournamentTests extends TestBase {
	@Test
	@Ignore
	public void testVarianWrynn() {
		Assert.fail("Varian Wrynn needs to be tested.");
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
		DebugContext context = createContext(HeroClass.RED, HeroClass.RED);
		Player player = context.getPlayer1();
		Hero hero = player.getHero();

		playCard(context, player, CardCatalogue.getCardById("weapon_deaths_bite"));
		Assert.assertEquals(hero.getWeapon().getAttack(), 4);
		Assert.assertEquals(hero.getWeapon().getDurability(), 2);
		playCard(context, player, CardCatalogue.getCardById("minion_tournament_attendee"));
		Assert.assertEquals(player.getMinions().size(), 1);
		playCard(context, player, CardCatalogue.getCardById("weapon_kings_defender"));
		Assert.assertEquals(hero.getWeapon().getAttack(), 3);
		Assert.assertEquals(hero.getWeapon().getDurability(), 3);
		Assert.assertEquals(player.getMinions().size(), 0);
	}


	@Test
	public void testEydisDarkbane() {
		GameContext context = createContext(HeroClass.WHITE, HeroClass.RED);
		Player priest = context.getPlayer1();
		Player warrior = context.getPlayer2();

		MinionCard eydisDarkbaneCard = (MinionCard) CardCatalogue.getCardById("minion_eydis_darkbane");
		Minion eydisDarkbane = playMinionCard(context, priest, eydisDarkbaneCard);

		Card testSpellCard = CardCatalogue.getCardById("spell_power_word_shield");
		context.getLogic().receiveCard(priest.getId(), testSpellCard);
		GameAction spellAction = testSpellCard.play();
		spellAction.setTarget(eydisDarkbane);
		context.getLogic().performGameAction(priest.getId(), spellAction);

		// priest casted a spell on Eydis - warrior should be wounded
		Assert.assertEquals(warrior.getHero().getHp(), warrior.getHero().getMaxHp() - 3);

		testSpellCard = CardCatalogue.getCardById("spell_shield_slam");
		context.getLogic().receiveCard(warrior.getId(), testSpellCard);
		spellAction = testSpellCard.play();
		spellAction.setTarget(eydisDarkbane);
		context.getLogic().performGameAction(warrior.getId(), spellAction);

		// warrior casted a spell on Eydis - nothing should happen
		Assert.assertEquals(warrior.getHero().getHp(), warrior.getHero().getMaxHp() - 3);
	}

}
