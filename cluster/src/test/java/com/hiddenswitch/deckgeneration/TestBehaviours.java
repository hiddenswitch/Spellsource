package com.hiddenswitch.deckgeneration;

import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.actions.PlaySpellCardAction;
import net.demilich.metastone.game.spells.DamageSpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertTrue;


public class TestBehaviours extends TestBase {
	@Test
	public static void testValidActionsFilterForDamagingSelf() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "spell_shock_0");
			PlayRandomWithoutSelfDamageBehaviour behaviour = new PlayRandomWithoutSelfDamageBehaviour();
			List<GameAction> actions = context.getValidActions();
			behaviour.filterActions(player, actions);
			for (GameAction action : actions) {
				if (action instanceof PlaySpellCardAction) {
					PlaySpellCardAction spellCardAction = (PlaySpellCardAction) action;
					SpellDesc spellDesc = spellCardAction.getSpell();
					if (spellDesc != null) {
						boolean isDamageSpell = DamageSpell.class.isAssignableFrom(spellDesc.getDescClass());
						boolean targetsFriendlyHero = action.getTargetReference() != null && action.getTargetReference().equals(player.getHero().getReference());
						assertTrue(!(targetsFriendlyHero && isDamageSpell));
					}
				}
			}
		});
	}
}
