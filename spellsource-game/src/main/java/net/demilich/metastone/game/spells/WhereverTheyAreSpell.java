package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.filter.SpecificCardFilter;
import net.demilich.metastone.game.spells.trigger.WhereverTheyAreEnchantment;
import net.demilich.metastone.game.targeting.EntityReference;
import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Casts a spell on a target and its copies "wherever they are" in the {@code player} (casting player's) zones.
 * <p>
 * The underlying implementation uses enchantments to apply the effects for all the non-removed game zones ({@link
 * Zones#BATTLEFIELD}, {@link Zones#DECK}, {@link Zones#HAND}, {@link Zones#HERO}, {@link Zones#HERO_POWER} and {@link
 * Zones#WEAPON}). IF {@link net.demilich.metastone.game.spells.desc.SpellArg#ZONES} is specified, use those instead.
 * <p>
 * If the {@link Zones#GRAVEYARD} is included, which ordinarily wipes enchantments and buffs, an enchantment is put into
 * play that will cast the specified spell on minions entering the owner's battlefield. This will include minions
 * summoned by all other effects, like discovers or battlecries or from the hand.
 * <p>
 * If the {@link SpellArg#TARGET} is {@link net.demilich.metastone.game.targeting.EntityReference#OUTPUT} in any of the
 * sub-spells, the output is resolved now rather than at the time the spell is cast.
 * <p>
 * A card is considered a copy "wherever they are" if they are the same card ID.
 * <p>
 * To implement a buff, like "Give a minion and all its copies +1/+1 (wherever they are):"
 * <pre>
 *   {
 *     "class": "WhereverTheyAreSpell",
 *     "spell": {
 *       "class": "BuffSpell",
 *       "value": 1
 *     },
 *     "zones": ["BATTLEFIELD", "GRAVEYARD"]
 *   }
 * </pre>
 * Observe that the zones are specified to just be {@link Zones#BATTLEFIELD} and {@link Zones#GRAVEYARD}. This indicates
 * that the spell should be cast on all the existing copies of the {@code target} on the caster's battlefield; then, an
 * enchantment will ensure that minions summoned from the hand, deck, graveyard, discovers, etc. will also get this
 * bonus cast on them.
 * <p>
 * The client communications code special-cases the enchantment provided here to look for buffs to render in the
 * client's hand.
 */
public final class WhereverTheyAreSpell extends MetaSpell {

	private static final Zones[] DEFAULT_ZONES = new Zones[]{Zones.BATTLEFIELD, Zones.GRAVEYARD};

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		EntityFilter filter;
		if (target == null && desc.containsKey(SpellArg.CARD)) {
			filter = SpecificCardFilter.create(SpellUtils.getCard(context, desc).getCardId()).create();
		} else if (desc.containsKey(SpellArg.FILTER)) {
			filter = desc.getEntityFilter();
		} else {
			filter = SpecificCardFilter.create(target.getSourceCard().getCardId()).create();
		}
		Zones[] zones;
		if (desc.containsKey(SpellArg.ZONES)) {
			zones = (Zones[]) desc.get(SpellArg.ZONES);
		} else {
			zones = DEFAULT_ZONES;
		}

		if (!context.getOutputStack().isEmpty()) {
			desc = desc.clone();
			desc.spellStream(1, false)
					.forEach(subSpellDesc -> {
						if (subSpellDesc.getTarget() != null && subSpellDesc.getTarget().equals(EntityReference.OUTPUT)) {
							subSpellDesc.put(SpellArg.TARGET, context.resolveSingleTarget(player, source, EntityReference.OUTPUT).getReference());
						}
						if (subSpellDesc.getSecondaryTarget() != null && subSpellDesc.getSecondaryTarget().equals(EntityReference.OUTPUT)) {
							subSpellDesc.put(SpellArg.SECONDARY_TARGET, context.resolveSingleTarget(player, source, EntityReference.OUTPUT).getReference());
						}
					});
		}


		if (Arrays.stream(zones).anyMatch(Predicate.isEqual(Zones.GRAVEYARD))) {
			// Cast on minions brought into play, however they are
			context.getLogic().addEnchantment(player, new WhereverTheyAreEnchantment(filter, desc, source.getSourceCard()), source, player);
		}

		List<Entity> targets = context.getEntities()
				.filter(e -> {
					boolean zoneMatches = false;
					for (Zones zone : zones) {
						if (e.getZone() == zone) {
							zoneMatches = true;
							break;
						}
					}
					return zoneMatches && e.getOwner() == player.getId();
				})
				.filter(e -> filter.matches(context, player, e, source))
				.collect(Collectors.toList());
		for (Entity copyTarget : targets) {
			super.onCast(context, player, desc, source, copyTarget);
		}
	}
}
