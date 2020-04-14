package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.AuraBuffSpell;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.spells.desc.aura.AuraArg;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicOperation;
import net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicValueProvider;
import net.demilich.metastone.game.spells.trigger.WillEndSequenceTrigger;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * A buff aura applies the specified {@link AuraArg#ATTACK_BONUS} and {@link AuraArg#HP_BONUS} to the target entities as
 * long as the {@link AuraArg#CONDITION} is satisfied.
 * <p>
 * To support reevaluating the condition at different times, optionally specify an {@link AuraArg#SECONDARY_TRIGGER}
 * that fires when the condition ought to change. Otherwise, the condition is evaluated at the same time all other auras
 * are, which is whenever the board changes or a sequence ends. Updating auras when the sequence ends is almost always a
 * sufficient event to react to in order to implement an effect.
 * <p>
 * The underlying attributes used for this buff are {@link Attribute#AURA_ATTACK_BONUS}
 * and {@link Attribute#AURA_HP_BONUS}.
 * <p>
 * For example, to give all damaged Murlocs +3/+3:
 * <pre>
 *   {
 *     "class": "BuffAura",
 *     "attackBonus": 1,
 *     "hpBonus": 1,
 *     "target": "ALL_MINIONS"
 *     "filter": {
 *       "class": "RaceFilter",
 *       "race": "MURLOC"
 *     },
 *     "condition": {
 *       "class": "DamagedCondition"
 *     }
 *   }
 * </pre>
 *
 * @see Aura for a description of how the fields in the {@link AuraDesc} are interpreted.
 */
public class BuffAura extends Aura {

	public BuffAura(AuraDesc desc) {
		this(desc.get(AuraArg.ATTACK_BONUS), desc.get(AuraArg.HP_BONUS), desc.getTarget(), desc.getFilter());
		if (desc.getSecondaryTrigger() != null) {
			getTriggers().add(desc.getSecondaryTrigger().create());
		}
		includeExtraTriggers(desc);
		setCondition(desc.getCondition());
		setDesc(desc);
	}

	public BuffAura(Object attackBonus, Object hpBonus, EntityReference targetSelection, EntityFilter filter) {
		/*
		 Rule 4a: After the outermost Phase ends, Hearthstone does an Aura Update (Health/Attack), then does the Death
		 Creation Step (Looks for all mortally wounded (0 or less Health)/pending destroy (hit with a destroy effect)
		 Entities and kills them, removing them from play simultaneously), then does an Aura Update (Other). Entities
		 that have been removed from play cannot trigger, be triggered, or emit auras, and do not take up space.
		 */
		super(new WillEndSequenceTrigger(), AuraBuffSpell.create(attackBonus, hpBonus), AuraBuffSpell.create(
				AlgebraicValueProvider.create(attackBonus, null, AlgebraicOperation.NEGATE),
				AlgebraicValueProvider.create(hpBonus, null, AlgebraicOperation.NEGATE)), targetSelection);
		setEntityFilter(filter);
	}
}
