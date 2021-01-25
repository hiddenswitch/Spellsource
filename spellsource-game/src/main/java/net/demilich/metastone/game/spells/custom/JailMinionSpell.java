package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.rpc.Spellsource.EntityTypeMessage.EntityType;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.*;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Destroys the {@code target} minion, and gives the {@code source} the aftermath, "Resummon that minion."
 * <p>
 * If a {@link SpellArg#REVERT_TRIGGER} is specified, creates a one-fire enchantment hosted by {@link
 * SpellArg#SECONDARY_TARGET} (defaults to {@code source}) whose spell will resummon the original targeted minion. Does
 * <b>not</b> use an aftermath in this situation.
 * <p>
 * Summons {@link SpellArg#VALUE} copies, defaulting to 1.
 * <p>
 * Implements Moat Lurker and Carnivorous Cube.
 */
public final class JailMinionSpell extends Spell {
	private static Logger LOGGER = LoggerFactory.getLogger(JailMinionSpell.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(LOGGER, context, source, desc, SpellArg.SECONDARY_TARGET, SpellArg.REVERT_TRIGGER, SpellArg.VALUE);
		if (target.getEntityType() != EntityType.MINION) {
			return;
		}
		Minion minion = (Minion) target;
		TargetPlayer targetPlayer = TargetPlayer.SELF;
		if (minion.getOwner() != source.getOwner()) {
			targetPlayer = TargetPlayer.OPPONENT;
		}
		int copies = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);

		SpellDesc resummon = SummonSpell.create(targetPlayer, minion.getSourceCard());
		resummon.put(SpellArg.VALUE, copies);
		SpellDesc enchant;

		if (desc.containsKey(SpellArg.SECONDARY_TARGET)) {
			source = context.resolveSingleTarget(player, source, desc.getSecondaryTarget());
		} else {
			// TODO: Check if we actually need to do this
			source.getAttributes().remove(Attribute.DEATHRATTLES);
		}

		if (desc.containsKey(SpellArg.REVERT_TRIGGER)) {
			EnchantmentDesc enchantmentDesc = new EnchantmentDesc();
			enchantmentDesc.setEventTrigger((EventTriggerDesc) desc.get(SpellArg.REVERT_TRIGGER));
			enchantmentDesc.setMaxFires(1);
			enchantmentDesc.setSpell(resummon);
			enchant = AddEnchantmentSpell.create(enchantmentDesc);
		} else {
			enchant = AddDeathrattleSpell.create(resummon);
		}

		SpellDesc destroySpell = DestroySpell.create(target.getReference());
		SpellUtils.castChildSpell(context, player, destroySpell, source, target);
		SpellUtils.castChildSpell(context, player, enchant.clone(), source, source);
	}
}
