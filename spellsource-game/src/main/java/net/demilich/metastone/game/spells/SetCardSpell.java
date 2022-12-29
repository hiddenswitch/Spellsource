package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.cards.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sets the {@code target} {@link Card}'s {@link Attribute#CARD_ID} or {@link Attribute#AURA_CARD_ID} to the card ID
 * specified in {@link SpellArg#CARD}. If a {@link SpellArg#SECONDARY_TARGET} is specified, the {@code target} is
 * instead the <b>source</b> of the card ID: the {@code target}'s card ID will be copied to the secondary target
 * argument.
 * <p>
 * This spell does <b>not</b> put the enchantments, like triggers and auras, into play from the newly converted card
 * ID.
 * <p>
 * This spell is a {@link RevertableSpell}, so it is suitable for use in auras.
 * <p>
 * For <b>example,</b> to set the enemy's hero power's card to yours:
 * <pre>
 *     {
 *         "class": "SetCardSpell",
 *         "secondaryTarget": "ENEMY_HERO_POWER",
 *         "target": "FRIENDLY_HERO_POWER"
 *     }
 * </pre>
 */
public final class SetCardSpell extends RevertableSpell {

	private static Logger logger = LoggerFactory.getLogger(SetCardSpell.class);

	public static SpellDesc create(String cardId, boolean isAura) {
		SpellDesc desc = new SpellDesc(SetCardSpell.class);
		desc.put(SpellArg.CARD, cardId);
		if (isAura) {
			desc.put(SpellArg.ATTRIBUTE, Attribute.AURA_CARD_ID);
		} else {
			desc.put(SpellArg.ATTRIBUTE, Attribute.CARD_ID);
		}
		return desc;
	}

	public static SpellDesc revert(boolean isAura) {
		return RemoveAttributeSpell.create(isAura ? Attribute.AURA_CARD_ID : Attribute.CARD_ID);
	}

	@Override
	protected SpellDesc getReverseSpell(GameContext context, Player player, Entity source, SpellDesc desc, EntityReference target) {
		Entity copyTo = context.resolveSingleTarget(player, source, desc.getSecondaryTarget());
		if (copyTo != null) {
			// Bake in the copy to reference for the revert spell
			target = copyTo.getReference();
		}
		return RemoveAttributeSpell.create(target, (Attribute) desc.getOrDefault(SpellArg.ATTRIBUTE, Attribute.CARD_ID));
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(logger, context, source, desc, SpellArg.ATTRIBUTE, SpellArg.CARD, SpellArg.SECONDARY_TARGET, SpellArg.REVERT_TRIGGER, SpellArg.SECOND_REVERT_TRIGGER);

		Attribute attr = (Attribute) desc.getOrDefault(SpellArg.ATTRIBUTE, Attribute.CARD_ID);
		if (!(attr == Attribute.CARD_ID || attr == Attribute.AURA_CARD_ID)) {
			logger.error("onCast {} {}: Only CARD_ID and AURA_CARD_ID are valid attributes, {} was specified.",
					context.getGameId(), source, attr);
			return;
		}
		// If it contains a secondary target, it's a copy from target to secondary target
		Entity copyTo = context.resolveSingleTarget(player, source, desc.getSecondaryTarget());
		String cardId;
		if (copyTo != null) {
			cardId = target.getSourceCard().getCardId();
			target = copyTo;
		} else {
			cardId = (String) desc.get(SpellArg.CARD);
		}

		if (cardId == null) {
			logger.error("onCast {} {}: A null card was given for the card spell.", context.getGameId(), source);
			return;
		}

		if (target instanceof Card) {
			logger.debug("onCast {} {}: Setting {}'s card ID to {}", context.getGameId(), source, target, cardId);
			target.getAttributes().put(attr, cardId);
		} else {
			logger.error("onCast {} {}: Trying to set {}'s {} attribute to {}. It is not a card, which is not supported.",
					context.getGameId(), source, target, attr, cardId);
			return;
		}

		super.onCast(context, player, desc, source, target);
	}
}
