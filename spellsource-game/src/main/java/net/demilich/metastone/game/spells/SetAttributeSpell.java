package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProvider;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.cards.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Sets the {@code target}'s {@link SpellArg#ATTRIBUTE} to the specified {@link SpellArg#VALUE}.
 */
public class SetAttributeSpell extends Spell {
	private static Logger LOGGER = LoggerFactory.getLogger(SetAttributeSpell.class);

	public static SpellDesc create(EntityReference target, Attribute tag, Object value) {
		Map<SpellArg, Object> arguments = new SpellDesc(SetAttributeSpell.class);
		arguments.put(SpellArg.ATTRIBUTE, tag);
		arguments.put(SpellArg.VALUE, value);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(Attribute tag, Object value) {
		Map<SpellArg, Object> arguments = new SpellDesc(SetAttributeSpell.class);
		arguments.put(SpellArg.ATTRIBUTE, tag);
		arguments.put(SpellArg.VALUE, value);
		return new SpellDesc(arguments);
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(LOGGER, context, source, desc, SpellArg.ATTRIBUTE, SpellArg.VALUE);
		Attribute attribute = (Attribute) desc.get(SpellArg.ATTRIBUTE);
		Object value = desc.get(SpellArg.VALUE);
		if (ValueProvider.class.isAssignableFrom(value.getClass())) {
			value = desc.getValue(SpellArg.VALUE, context, player, target, source, 0);
		}
		target.setAttribute(attribute, value);
	}

}
