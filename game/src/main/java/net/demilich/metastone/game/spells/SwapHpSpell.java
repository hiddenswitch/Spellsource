package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.client.models.EntityType;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.cards.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

/**
 * Swaps the {@code target}'s hitpoints with the {@code source} actor's hitpoints.
 * <p>
 * If a {@link SpellArg#SECONDARY_TARGET} is specified, use it as the {@code source} instead.
 */
public class SwapHpSpell extends Spell {

	private static Logger LOGGER = LoggerFactory.getLogger(SwapHpSpell.class);

	public static SpellDesc create() {
		Map<SpellArg, Object> arguments = new SpellDesc(SwapHpSpell.class);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Actor sourceActor;
		if (desc.getSecondaryTarget() != null) {
			sourceActor = context.resolveSingleTarget(player, source, desc.getSecondaryTarget());
		} else {
			if (context.getSummonReferenceStack().isEmpty()) {
				if (source.getEntityType() == EntityType.MINION || source.getEntityType() == EntityType.HERO) {
					sourceActor = (Actor) source;
				} else {
					LOGGER.warn("onCast {} {}: Could not resolve valid source", context.getGameId(), source);
					return;
				}
			} else {
				sourceActor = (Actor) context.resolveSingleTarget(context.getSummonReferenceStack().peek());
			}
			if (sourceActor == null && source.isInPlay() && source instanceof Actor) {
				sourceActor = (Actor) source;
			}
			if (!Objects.equals(sourceActor, source)) {
				LOGGER.debug("onCast {} {}: sourceActor {} does not equal source", context.getGameId(), source, sourceActor);
			}
		}

		if (sourceActor == null) {
			return;
		}

		Actor targetActor = (Actor) target;
		int sourceHp = sourceActor.getHp();
		int targetHp = targetActor.getHp();
		context.getLogic().setHpAndMaxHp(sourceActor, targetHp);
		sourceActor.setAttribute(Attribute.HP_BONUS, 0);
		context.getLogic().setHpAndMaxHp(targetActor, sourceHp);
		targetActor.setAttribute(Attribute.HP_BONUS, 0);
	}

}

