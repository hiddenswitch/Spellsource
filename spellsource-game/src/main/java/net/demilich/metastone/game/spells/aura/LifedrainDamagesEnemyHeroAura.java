package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.desc.aura.AuraDesc;

/**
 * When in play, lifesteal from source cards affected by this aura will deal damage to the enemy hero instead of healing
 * the friendly hero.
 *
 * @see net.demilich.metastone.game.logic.GameLogic for where lifesteal is processed.
 */
public final class LifedrainDamagesEnemyHeroAura extends AbstractFriendlyCardAura {

	public LifedrainDamagesEnemyHeroAura(AuraDesc desc) {
		super(desc);
	}
}
