package net.demilich.metastone.game.spells;

import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.aura.Aura;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.targeting.EntityReference;
import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Adds an {@link SpellArg#AURA} ({@link Aura}) or a {@link Enchantment} (in the {@link SpellArg#TRIGGER}) to the
 * specified {@code target} and immediately puts that aura/enchantment into play (i.e., activates it).
 * <p>
 * If a {@link SpellArg#CARD} is specified, the spell will interpret the card as an {@link CardType#ENCHANTMENT}, adding
 * each of its triggers as specified to the {@code target }and the deathrattle as a {@link
 * net.demilich.metastone.game.spells.trigger.MinionDeathTrigger} whose {@link net.demilich.metastone.game.targeting.TargetType}
 * is {@link net.demilich.metastone.game.targeting.TargetType#IGNORE_OTHER_TARGETS}. If no triggers are present on the
 * card, a dummy enchantment is created for later use in the {@link RemoveEnchantmentSpell} and {@link
 * net.demilich.metastone.game.spells.desc.filter.HasEnchantmentFilter}.
 * <p>
 * If a {@link SpellArg#REVERT_TRIGGER} is specified, creates an enchantment on the casting player's {@link Player}
 * entity that triggers with the specified {@link net.demilich.metastone.game.spells.trigger.EventTrigger} and removes
 * trigger, aura and enchantment cards added by this spell. To model more sophisticated effects, consider creating a
 * dedicate trigger with a card ID corresponding to the enchantment card with the core effects.
 * <p>
 * Enchantments and auras are only in play if their {@link Enchantment#getHostReference()} is {@link Entity#isInPlay()}.
 * Currently, auras and enchantments are immediately active and listening to events. However, auras only evaluate which
 * entities they affect on the next event they react to, which is typically a {@link
 * net.demilich.metastone.game.events.BoardChangedEvent} or {@link net.demilich.metastone.game.spells.trigger.WillEndSequenceTrigger}.
 * Thus, auras aren't really in play until those triggers fire (or the aura's {@link
 * net.demilich.metastone.game.spells.desc.aura.AuraArg#SECONDARY_TRIGGER} fires).
 * <p>
 * This example implements the text, "At the start of your next turn, summon four 1/1 Silver Hand Recruits." Notice that
 * {@link EntityReference#FRIENDLY_PLAYER} is used as the target of an enchantment for effects that don't really belong
 * to specific actors like minions.
 * <pre>
 *   {
 *     "class": "AddEnchantmentSpell",
 *     "target": "FRIENDLY_PLAYER",
 *     "trigger": {
 *       "eventTrigger": {
 *         "class": "TurnStartTrigger",
 *         "targetPlayer": "SELF"
 *       },
 *       "spell": {
 *         "class": "SummonSpell",
 *         "cards": [
 *           "token_silver_hand_recruit",
 *           "token_silver_hand_recruit",
 *           "token_silver_hand_recruit",
 *           "token_silver_hand_recruit"
 *         ]
 *       },
 *       "oneTurn": true
 *     }
 *   }
 * </pre>
 * This example shows an easy way to remove the trigger added this way. This implements the text, "Give +1/+1 to all
 * minions summoned until the start of your next turn."
 * <pre>
 *   {
 *     "class": "AddEnchantmentSpell",
 *     "target": "FRIENDLY_PLAYER",
 *     "trigger": {
 *       "eventTrigger": {
 *         "class": "MinionSummonedTrigger",
 *         "targetPlayer": "BOTH"
 *       },
 *       "spell": {
 *         "class": "BuffSpell",
 *         "value": 1,
 *         "target": "EVENT_TARGET"
 *       }
 *     },
 *     "revertTrigger": {
 *       "class": "TurnStartTrigger",
 *       "targetPlayer": "SELF"
 *     }
 *   }
 * </pre>
 * <p>
 * This powerful spell can be chained together to do counting, to put auras into play, etc. Browse cards that use the
 * spell for more complete examples.
 *
 * @see AddDeathrattleSpell for a simple way to add a deathrattle to a minion/card.
 * @see Aura for more about what auras should look like.
 * @see Enchantment for more about what enchantments should look like.
 */
public class AddEnchantmentSpell extends Spell {

	private static Logger logger = LoggerFactory.getLogger(AddEnchantmentSpell.class);

	public static SpellDesc create(EntityReference target, EnchantmentDesc trigger) {
		Map<SpellArg, Object> arguments = new SpellDesc(AddEnchantmentSpell.class);
		arguments.put(SpellArg.TRIGGER, trigger);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(EntityReference target, Aura aura) {
		Map<SpellArg, Object> arguments = new SpellDesc(AddEnchantmentSpell.class);
		arguments.put(SpellArg.AURA, aura);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(EnchantmentDesc trigger) {
		return create(null, trigger);
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		if (target == null) {
			if (desc.containsKey(SpellArg.TARGET_PLAYER)) {
				target = player;
			} else {
				logger.error("onCast {} {}: Target cannot be null.", context.getGameId(), source);
				throw new NullPointerException("target");
			}
		}
		checkArguments(logger, context, source, desc, SpellArg.AURA, SpellArg.TRIGGER, SpellArg.CARD, SpellArg.EXCLUSIVE, SpellArg.REVERT_TRIGGER);
		EnchantmentDesc enchantmentDesc = (EnchantmentDesc) desc.get(SpellArg.TRIGGER);
		AuraDesc auraDesc = (AuraDesc) desc.get(SpellArg.AURA);
		Card enchantmentCard = SpellUtils.getCard(context, desc);
		List<Enchantment> added = new ArrayList<>();

		if (enchantmentDesc != null) {
			var enchantment = context.getLogic().addEnchantment(player, source, source.getSourceCard(), target, enchantmentDesc, true);
			// For compatibility reasons, if the target (i.e. the enchantment's host) is in the opponent's deck or hand, deck
			// enchantments will also be active in the hand. It will be the responsibility of the card author to make them
			// expire when drawn using the RemoveEnchantmentsSpell
			if (source.getSourceCard().getDesc().getFileFormatVersion() <= 1
					&& target.getZone() == Zones.HAND || target.getZone() == Zones.DECK) {
				enchantment.ifPresent(e -> {
					e.setZones(new Zones[]{Zones.HAND, Zones.DECK});
				});
			}
			enchantment.ifPresent(added::add);
		}

		if (auraDesc != null) {
			var aura = context.getLogic().addEnchantment(player, source, source.getSourceCard(), target, auraDesc, true);
			aura.ifPresent(added::add);
		}

		if (enchantmentCard != null) {
			context.getLogic().addEnchantments(player, source, enchantmentCard, target, true);
		}

		for (SpellArg arg : new SpellArg[]{SpellArg.REVERT_TRIGGER, SpellArg.SECOND_REVERT_TRIGGER}) {
			if (desc.containsKey(arg) && added.size() > 0) {
				// Convenience method for removing the enchantments added this way
				EnchantmentDesc revertDesc = new EnchantmentDesc();
				revertDesc.setEventTrigger((EventTriggerDesc) desc.get(arg));
				revertDesc.setSpell(MetaSpell.create(added.stream().map(RemoveEnchantmentSpell::create).toArray(SpellDesc[]::new)));
				revertDesc.setMaxFires(1);
				context.getLogic().addEnchantment(player, revertDesc.create(), source, player);
			}
		}
	}
}

