package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.AddAttributeSpell;
import net.demilich.metastone.game.spells.RemoveAttributeSpell;
import net.demilich.metastone.game.spells.desc.aura.AuraArg;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.cards.Attribute;

/**
 * Grants an {@link AuraArg#ATTRIBUTE} to the specified targets.
 * <p>
 * Always use an attribute prefixed with {@code AURA_}, like {@link Attribute#AURA_LIFESTEAL}, to prevent inadvertent
 * interactions with other cards that <b>permanently</b> grant an entity an attribute.
 * <p>
 * For example, to give all friendly beasts charge:
 * <pre>
 *    {
 *     "class": "AttributeAura",
 *     "target": "FRIENDLY_MINIONS",
 *     "attribute": "AURA_CHARGE",
 *     "filter": {
 *       "class": "RaceFilter",
 *       "race": "BEAST"
 *     }
 *   }
 * </pre>
 *
 * @see Aura for a description of how the fields in the {@link AuraDesc} are interpreted.
 */
public final class AttributeAura extends SpellAura {

	public AttributeAura(AuraDesc desc) {
		super(desc);
		setApplyAuraEffect(AddAttributeSpell.create(desc.getAttribute()));
		setRemoveAuraEffect(RemoveAttributeSpell.create(desc.getAttribute()));
	}

	@Override
	protected boolean notApplied(Entity target) {
		if (Attribute.getAuraAttributes().contains(getDesc().getAttribute())) {
			return !target.hasAttribute(getDesc().getAttribute());
		} else {
			return super.notApplied(target);
		}
	}
}

