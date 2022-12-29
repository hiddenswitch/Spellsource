package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.OpenerDesc;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.AttributeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

/**
 * Copies the text written on the actor card pointed to by {@link SpellArg#SECONDARY_TARGET} to the {@code target}.
 * <p>
 * If a {@link SpellArg#SECONDARY_TARGET} is not specified, retrieves a random card from the specified {@link
 * SpellArg#CARD_SOURCE} and {@link SpellArg#CARD_FILTER} or {@link SpellArg#CARDS}.
 * <p>
 * Casts {@link SpellArg#SPELL} with the {@link EntityReference#OUTPUT} set to the card the effects were copied from.
 */
public final class AddActorEffectsToTargetActorSpell extends Spell {

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
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Entity sourceEntity;
		if (desc.containsKey(SpellArg.SECONDARY_TARGET)) {
			sourceEntity = context.resolveSingleTarget(player, source, (EntityReference) desc.get(SpellArg.SECONDARY_TARGET));
			if (sourceEntity == null) {
				return;
			}
		} else {
			CardList cards = SpellUtils.getCards(context, player, target, source, desc, 1);
			if (cards.isEmpty()) {
				return;
			}
			sourceEntity = cards.get(0);
		}

		Card sourceCard = sourceEntity.getSourceCard();
		// Restore the race after it is changed
		Actor targetActor = (Actor) target;
		String originalRace = targetActor.getRace();
		// Copy the attributes onto the actor
		AttributeMap sourceAttributes = new AttributeMap();
		for (Attribute key : sourceCard.getAttributes().unsafeKeySet()) {
			sourceAttributes.put(key, sourceCard.getAttributes().get(key));
		}
		// Copy "text" attributes onto the actor by excluding the non-text ones
		Stream.concat(Attribute.getAuraAttributes().stream(),
				Stream.concat(Stream.of(
						Attribute.BASE_ATTACK,
						Attribute.BASE_HP,
						Attribute.BASE_MANA_COST,
						Attribute.HP,
						Attribute.MAX_HP,
						Attribute.HP_BONUS,
						Attribute.ATTACK,
						Attribute.ATTACK_BONUS,
						Attribute.CONDITIONAL_ATTACK_BONUS,
						Attribute.COPIED_FROM,
						Attribute.TRANSFORM_REFERENCE,
						Attribute.PLAYED_FROM_HAND_OR_DECK,
						Attribute.NAME,
						Attribute.DESCRIPTION,
						Attribute.RACE,
						Attribute.COUNTERED,
						Attribute.BATTLECRY,
						Attribute.DEATHRATTLES), Card.IGNORED_MINION_ATTRIBUTES.stream())).forEach(sourceAttributes::remove);
		targetActor.getAttributes().putAll(sourceAttributes);
		// Now apply the actual text

		sourceCard.applyRace(targetActor);
		targetActor.setRace(originalRace);
		context.getLogic().addEnchantments(player, source, sourceCard, targetActor);
		// Include openers this one time. It's otherwise very strange to include
		context.getLogic().addEnchantments(player, source, sourceCard, targetActor, true, abstractEnchantmentDesc -> abstractEnchantmentDesc instanceof OpenerDesc);

		for (SpellDesc subSpell : desc.subSpells(0)) {
			SpellUtils.castChildSpell(context, player, subSpell, source, target, sourceCard);
		}
	}
}
