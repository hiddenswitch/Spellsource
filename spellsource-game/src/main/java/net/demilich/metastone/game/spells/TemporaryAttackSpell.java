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

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(LOGGER, context, source, desc, SpellArg.VALUE, SpellArg.ATTACK_BONUS);
		int attackBonus = desc.getValue(SpellArg.VALUE, context, player, target, source, 0);
		attackBonus += desc.getValue(SpellArg.ATTACK_BONUS, context, player, target, source, 0);
		Actor targetActor = (Actor) target;

		// Read auras
		List<ModifyTemporaryAttackSpellAura> auras = SpellUtils.getAuras(context, ModifyTemporaryAttackSpellAura.class, source);
		for (ModifyTemporaryAttackSpellAura aura : auras) {
			attackBonus += aura.getDesc().getValue(AuraArg.VALUE, context, player, target, source, 0);
		}

		if (attackBonus != 0) {
			targetActor.modifyAttribute(Attribute.TEMPORARY_ATTACK_BONUS, attackBonus);
		}
	}

}
