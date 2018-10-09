package net.demilich.metastone.game.spells;

import com.github.fromage.quasi.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.aura.Aura;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.spells.trigger.Trigger;
import net.demilich.metastone.game.targeting.EntityReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Adds an {@link SpellArg#AURA} ({@link Aura}) or a {@link Enchantment} (in the {@link SpellArg#TRIGGER}) to the
 * specified {@code target} and immediately puts that aura/enchantment into play (i.e., activates it).
 * <p>
 * If a {@link SpellArg#CARD} is specified, the spell will interpret the card as an {@link
 * net.demilich.metastone.game.cards.CardType#ENCHANTMENT}, adding each of its triggers as specified to the {@code
 * target }and the deathrattle as a {@link net.demilich.metastone.game.spells.trigger.MinionDeathTrigger} whose {@link
 * net.demilich.metastone.game.targeting.TargetType} is {@link net.demilich.metastone.game.targeting.TargetType#IGNORE_OTHER_TARGETS}.
 * If no triggers are present on the card, a dummy enchantment is created for later use in the {@link
 * RemoveEnchantmentSpell} and {@link net.demilich.metastone.game.spells.desc.filter.HasEnchantmentFilter}.
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
 * <p>
 * This powerful spell can be chained together to do counting, to put auras into play, etc. Browse cards that use the
 * spell for more complete examples.
 *
 * @see AddDeathrattleSpell for a simple way to add a deathrattle to a minion/card.
 * @see Aura for more about what auras should look like.
 * @see Enchantment for more about what enchantments should look like.
 */
public final class AddEnchantmentSpell extends Spell {

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
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		if (target == null) {
			if (desc.containsKey(SpellArg.TARGET_PLAYER)) {
				target = player;
			} else {
				logger.error("onCast {} {}: Target cannot be null.", context.getGameId(), source);
				throw new NullPointerException("target");
			}
		}
		checkArguments(logger, context, source, desc, SpellArg.AURA, SpellArg.TRIGGER, SpellArg.CARD, SpellArg.EXCLUSIVE);
		EnchantmentDesc enchantmentDesc = (EnchantmentDesc) desc.get(SpellArg.TRIGGER);
		Aura aura = (Aura) desc.get(SpellArg.AURA);
		Card enchantmentCard = SpellUtils.getCard(context, desc);

		if (enchantmentDesc != null) {
			Enchantment enchantment = enchantmentDesc.create();
			enchantment.setOwner(player.getId());
			enchantment.setSourceCard(source.getSourceCard());
			context.getLogic().addGameEventListener(player, enchantment, target);
		}

		if (aura != null) {
			aura = aura.clone();
			aura.setOwner(player.getId());
			aura.setSourceCard(source.getSourceCard());
			// Enchantments added this way don't trigger a board changed event. They come into play immediately if the owning
			// entity is Entity#isInPlay().
			context.getLogic().addGameEventListener(player, aura, target);
		}

		if (enchantmentCard != null) {
			List<Enchantment> enchantmentList = enchantmentCard.createEnchantments();
			for (Enchantment enchantment : enchantmentList) {
				enchantment.setOwner(player.getId());
				context.getLogic().addGameEventListener(player, enchantment, target);
			}
		}
	}
}

