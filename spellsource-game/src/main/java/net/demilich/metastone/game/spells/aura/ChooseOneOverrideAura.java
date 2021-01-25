package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.ChooseOneOverride;
import net.demilich.metastone.game.spells.desc.aura.AuraArg;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * Indicates that a choose one card's choices should be overridden by the {@link AuraArg#CHOOSE_ONE_OVERRIDE} {@link
 * ChooseOneOverride} value whenever the {@code target} card is affected by this aura or if this aura's {@code target}
 * is the {@link net.demilich.metastone.game.Player} entity ({@link EntityReference#FRIENDLY_PLAYER} or {@link
 * EntityReference#ENEMY_PLAYER}) that owns the card being override.
 * <p>
 * To make the player's choose one minions have both effects combined:
 * <pre>
 *   {
 *     "class": "ChooseOneOverrideAura"
 *     "target": "FRIENDLY_HAND",
 *     "filter": {
 *       "class": "CardFilter",
 *       "cardType": "MINION"
 *     },
 *     "chooseOneOverride": "BOTH_COMBINED"
 *   }
 * </pre>
 *
 * @see net.demilich.metastone.game.logic.GameLogic#getChooseOneAuraOverrides(Player, Card) to see the bulk of the logic
 * 		implementing this effect.
 */
public class ChooseOneOverrideAura extends AbstractFriendlyCardAura {
	public ChooseOneOverrideAura(AuraDesc desc) {
		super(desc);
	}

	public ChooseOneOverride getChooseOneOverride() {
		return (ChooseOneOverride) getDesc().getOrDefault(AuraArg.CHOOSE_ONE_OVERRIDE, ChooseOneOverride.NONE);
	}
}
