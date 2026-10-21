package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.aura.ModifyTemporaryAttackSpellAura;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.aura.AuraArg;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.cards.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Gives the {@code target} actor {@link SpellArg#VALUE} bonus attack until the end of the current turn.
 */
public class TemporaryAttackSpell extends Spell {

	private static Logger LOGGER = LoggerFactory.getLogger(TemporaryAttackSpell.class);

	public static SpellDesc create(EntityReference target, int attackBonus) {
		Map<SpellArg, Object> arguments = new SpellDesc(TemporaryAttackSpell.class);
		arguments.put(SpellArg.VALUE, attackBonus);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(int attackBonus) {
		return create(null, attackBonus);
	}

	protected void onCast(
			GameContext context,
			Player player,
			Entity source,
			Entity target,
			int value,
			int attackBonus) {
		int bonus = value + attackBonus;
		Actor targetActor = (Actor) target;

		List<ModifyTemporaryAttackSpellAura> auras = SpellUtils.getAuras(context, ModifyTemporaryAttackSpellAura.class, source);
		for (int i = 0, n = auras.size(); i < n; i++) {
			bonus += auras.get(i).getDesc().getValue(AuraArg.VALUE, context, player, target, source, 0);
		}

		if (bonus != 0) {
			targetActor.modifyAttribute(Attribute.TEMPORARY_ATTACK_BONUS, bonus);
		}
	}

}
