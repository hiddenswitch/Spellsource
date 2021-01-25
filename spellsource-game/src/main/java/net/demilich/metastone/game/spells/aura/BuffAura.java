package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.AuraBuffSpell;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.spells.desc.aura.AuraArg;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicOperation;
import net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicValueProvider;
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
 * The underlying attributes used for this buff are {@link Attribute#AURA_ATTACK_BONUS} and {@link
 * Attribute#AURA_HP_BONUS}.
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
public class BuffAura extends SpellAura {

	public BuffAura(AuraDesc desc) {
		super(desc);
		var attackBonus = desc.get(AuraArg.ATTACK_BONUS);
		var hpBonus = desc.get(AuraArg.HP_BONUS);
		var buff = AuraBuffSpell.create(attackBonus, hpBonus);
		var debuff = AuraBuffSpell.create(
				AlgebraicValueProvider.create(attackBonus, null, AlgebraicOperation.NEGATE),
				AlgebraicValueProvider.create(hpBonus, null, AlgebraicOperation.NEGATE));
		setApplyAuraEffect(buff);
		setRemoveAuraEffect(debuff);
	}

	public static AuraDesc create(int attackBonus, int hpBonus, EntityReference target) {
		var desc = new AuraDesc(BuffAura.class);
		desc.put(AuraArg.ATTACK_BONUS,attackBonus);
		desc.put(AuraArg.HP_BONUS, hpBonus);
		desc.put(AuraArg.TARGET,target);
		return desc;
	}
}
