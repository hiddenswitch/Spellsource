package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.AddAttributeSpell;
import net.demilich.metastone.game.spells.RemoveAttributeSpell;
import net.demilich.metastone.game.spells.desc.aura.AuraArg;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * When in play, lifesteal from source cards affected by this aura will deal damage to the enemy hero instead of healing
 * the friendly hero.
 *
 * @see net.demilich.metastone.game.logic.GameLogic for where lifesteal is processed.
 */
public final class LifedrainDamagesEnemyHeroAura extends SpellAura {

	public LifedrainDamagesEnemyHeroAura(AuraDesc desc) {
		super(desc);
		setApplyAuraEffect(AddAttributeSpell.create(Attribute.AURA_LIFEDRAIN_DAMAGES_ENEMY_HERO));
		setRemoveAuraEffect(RemoveAttributeSpell.create(Attribute.AURA_LIFEDRAIN_DAMAGES_ENEMY_HERO));
	}

	@Override
	protected EntityReference getTargets() {
		return (EntityReference) getDesc().getOrDefault(AuraArg.TARGET, EntityReference.FRIENDLY_PLAYER);
	}

	@Override
	protected boolean notApplied(Entity target) {
		return !target.hasAttribute(Attribute.AURA_LIFEDRAIN_DAMAGES_ENEMY_HERO);
	}
}
