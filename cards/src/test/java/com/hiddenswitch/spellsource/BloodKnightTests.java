package com.hiddenswitch.spellsource;

import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.minions.Minion;
import org.testng.annotations.Test;

public class BloodKnightTests extends TestBase {

	@Test
	public void testRendingCurseEternalSteedInteraction() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, CardCatalogue.getOneOneNeutralMinionCardId());
			playMinionCard(context, player, "minion_eternal_steed");
			playCard(context, player, "spell_rending_curse", target);
			destroy(context, target);
		});

		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_eternal_steed");
			Minion other = playMinionCard(context, player, "minion_eternal_steed");
			playCard(context, player, "spell_rending_curse", target);
			playCard(context, player, "spell_rending_curse", other);
			destroy(context, target);
		});

		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_eternal_steed");
			Minion other = playMinionCard(context, player, "minion_eternal_steed");
			playMinionCard(context, player, "minion_eternal_steed");
			playMinionCard(context, player, "minion_eternal_steed");
			playCard(context, player, "spell_rending_curse", target);
			playCard(context, player, "spell_rending_curse", other);
			destroy(context, target);
		});
	}
}
