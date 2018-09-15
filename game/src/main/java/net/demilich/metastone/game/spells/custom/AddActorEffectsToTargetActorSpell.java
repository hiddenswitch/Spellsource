package net.demilich.metastone.game.spells.custom;

import com.github.fromage.quasi.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.utils.AttributeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

public class AddActorEffectsToTargetActorSpell extends Spell {

	private static Logger logger = LoggerFactory.getLogger(AddActorEffectsToTargetActorSpell.class);

	public static SpellDesc create(EntityReference sourceActor, EntityReference targetActor) {
		SpellDesc arguments = new SpellDesc(AddActorEffectsToTargetActorSpell.class);
		arguments.put(SpellArg.SECONDARY_TARGET, sourceActor);
		if (targetActor != null) {
			arguments.put(SpellArg.TARGET, targetActor);
		}
		return arguments;
	}

	public static SpellDesc create(EntityReference sourceActor) {
		return create(sourceActor, null);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Entity sourceEntity =
				context.resolveTarget(player, source, (EntityReference) desc.get(SpellArg.SECONDARY_TARGET)).get(0);
		Card sourceCard = sourceEntity.getSourceCard();
		// Restore the race after it is changed
		Actor targetActor = (Actor) target;
		if (targetActor.hasAttribute(Attribute.BATTLECRY)
				&& sourceCard.hasBattlecry()) {
			logger.warn("onCast {} {}: The source card {} is going to overwrite the target {} battlecry.", context.getGameId(), source, sourceCard, targetActor);
		}

		Race originalRace = targetActor.getRace();
		if (originalRace == null) {
			originalRace = Race.NONE;
		}
		// Copy the attributes onto the actor
		AttributeMap sourceAttributes = new AttributeMap();
		for (Attribute key : sourceCard.getAttributes().unsafeKeySet()) {
			sourceAttributes.put(key, sourceCard.getAttributes().get(key));
		}
		// Copy "text" attributes onto the actor by excluding the non-text ones
		Stream.of(Attribute.AURA_ATTACK_BONUS, Attribute.AURA_HP_BONUS, Attribute.AURA_TAUNT, Attribute.AURA_UNTARGETABLE_BY_SPELLS,
				Attribute.BASE_ATTACK, Attribute.BASE_HP, Attribute.BASE_MANA_COST, Attribute.HP, Attribute.MAX_HP,
				Attribute.HP_BONUS, Attribute.ATTACK, Attribute.ATTACK_BONUS, Attribute.CONDITIONAL_ATTACK_BONUS,
				Attribute.COPIED_FROM, Attribute.TRANSFORM_REFERENCE, Attribute.PLAYED_FROM_HAND_OR_DECK, Attribute.NAME,
				Attribute.DESCRIPTION, Attribute.RACE, Attribute.COUNTERED).forEach(sourceAttributes::remove);
		targetActor.getAttributes().putAll(sourceAttributes);
		// Now apply the actual text
		sourceCard.applyText(targetActor);
		targetActor.setRace(originalRace);
	}
}
