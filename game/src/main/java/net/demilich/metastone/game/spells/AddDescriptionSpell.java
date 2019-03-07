package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.dynamicdescription.DynamicDescriptionDesc;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Adds the {@link net.demilich.metastone.game.spells.desc.SpellArg#SECONDARY_TARGET} card's text to the {@code
 * target}'s description, concatenating with a space.
 * <p>
 * Since the target may contain dynamic description entries, we have to copy those too.
 */
public class AddDescriptionSpell extends SetDescriptionSpell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Entity secondaryTarget = context.resolveSingleTarget(player, source, desc.getSecondaryTarget());
		String description = secondaryTarget.getDescription();

		target.setAttribute(Attribute.DESCRIPTION, target.getDescription() + " " + description);

		DynamicDescriptionDesc[] dynamicDescription = secondaryTarget.getSourceCard().getDynamicDescription();
		if (dynamicDescription != null && dynamicDescription.length > 0) {
			target.setAttribute(Attribute.DYNAMIC_DESCRIPTION,
					Stream.concat(
							Arrays.stream((DynamicDescriptionDesc[]) target.getAttributes().getOrDefault(Attribute.DYNAMIC_DESCRIPTION,
									new DynamicDescriptionDesc[0])), Arrays.stream(dynamicDescription)).toArray(DynamicDescriptionDesc[]::new));
		}
	}
}
