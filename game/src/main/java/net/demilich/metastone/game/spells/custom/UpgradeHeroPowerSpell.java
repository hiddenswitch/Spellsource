package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.ChangeHeroPowerSpell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.List;

/**
 * Upgrades the player's hero power as specified in its {@link net.demilich.metastone.game.cards.desc.CardDesc#heroPower}.
 */
public class UpgradeHeroPowerSpell extends ChangeHeroPowerSpell {

	@Override
	public void cast(GameContext context, Player player, SpellDesc desc, Entity source, List<Entity> targets) {
		boolean alternate = desc.getBool(SpellArg.EXCLUSIVE);
		Card heroPower = player.getHeroPowerZone().get(0);
		if (heroPower.getDesc().heroPower != null) {
			String cardId = heroPower.getDesc().heroPower;
			if (alternate) {
				try {
					String alternateCardId = cardId.replace("hero_power_", "hero_power_alternate_");
					context.getCardById(alternateCardId);
					//if it doesn't find the alternate card it won't go past here
					cardId = alternateCardId;
				} catch (NullPointerException e) {
				}
			}
			desc = desc.addArg(SpellArg.CARD, cardId);
			super.cast(context, player, desc, source, targets);
		}
	}
}
