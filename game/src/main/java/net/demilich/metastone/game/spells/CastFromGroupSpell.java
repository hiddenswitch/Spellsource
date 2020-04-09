package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.DiscoverAction;
import com.hiddenswitch.spellsource.client.models.CardType;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.client.models.EntityType;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.cards.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Use a {@link DiscoverSpell} instead, since groups were never well thought-out anyway.
 * <p>
 * Prompts the player to make choices from the specified {@link CardType#GROUP} card, and casts the choice.
 * <p>
 * If {@link SpellArg#EXCLUSIVE} is specified, allows choices to be copied from the {@code source} even when the source
 * is not a card.
 * <p>
 * If a {@link SpellArg#SECONDARY_TARGET} is specified, use it as the {@code source} of this spell instead. This is
 * useful to repeat choices that are stored on the {@code source}.
 * <p>
 * If a {@link SpellArg#SPELL} sub-spell is specified, it is cast after the choices are made with the {@code source} as
 * the {@link EntityReference#OUTPUT}. There, the choices can be recovered on the source's {@link Attribute#CHOICES}
 * attribute.
 * <p>
 * In order to use this spell, first create a group card. A group file may look like:
 * <pre>
 *  {
 * 		"name": "Plan Your Attack!",
 * 		"baseManaCost": 0,
 * 		"type": "GROUP",
 * 		"heroClass": "BROWN",
 * 		"group": [
 *      {
 * 		    "class": "MetaSpell",
 * 		    "target": "SELF",
 * 		    "name": "Stalk Your Prey",
 * 		    "description": "Gain +2/+1 and Stealth.",
 * 		    "spells": [
 *          {
 * 		        "class": "BuffSpell",
 * 		        "target": "SELF",
 * 		        "attackBonus": 2,
 * 		        "hpBonus": 1
 *          },
 *          {
 * 		        "class": "AddAttributeSpell",
 * 		        "target": "SELF",
 * 		        "attribute": "STEALTH"
 *          }
 * 		    ]
 *      },
 *      {
 * 		    "class": "SummonSpell",
 * 		    "target": "NONE",
 * 		    "value": 2,
 * 		    "name": "Consult the Trees",
 * 		    "description": "Summon two 1/1 Saplings.",
 * 		    "card": "token_sapling"
 *      },
 *      {
 * 		    "class": "DamageSpell",
 * 		    "target": "SPELL_TARGET",
 * 		    "value": 2,
 * 		    "name": "Leap From the Canopy",
 * 		    "description": "Deal 2 damage."
 *      }
 * 		],
 * 		"rarity": "FREE",
 * 		"collectible": false,
 * 		"set": "CUSTOM",
 * 		"fileFormatVersion": 1
 *  }
 * 						</pre>
 * Observe that the {@link CardDesc#type} is {@link CardType#GROUP}; each choice is represented by a {@link SpellDesc}
 * in group; and that the {@link SpellDesc} in {@link CardDesc#group} have the {@link SpellArg#NAME} and {@link
 * SpellArg#DESCRIPTION} specified.
 *
 * @see CardDesc#group for the choices.
 */
public class CastFromGroupSpell extends Spell {

	private static Logger logger = LoggerFactory.getLogger(CastFromGroupSpell.class);

	public static SpellDesc create(EntityReference target, SpellDesc spell) {
		Map<SpellArg, Object> arguments = new SpellDesc(CastFromGroupSpell.class);
		arguments.put(SpellArg.TARGET, target);
		arguments.put(SpellArg.SPELL, spell);
		return new SpellDesc(arguments);
	}

	@Suspendable
	public void cast(GameContext context, Player player, SpellDesc desc, Entity source, final List<Entity> originalTargets) {
		Entity originalSource = source;
		if (desc.containsKey(SpellArg.SECONDARY_TARGET)) {
			source = context.resolveSingleTarget(player, source, desc.getSecondaryTarget());
		}
		final List<Entity> targets;
		if (originalTargets == null) {
			targets = Collections.emptyList();
		} else {
			targets = originalTargets;
		}
		EntityFilter targetFilter = desc.getEntityFilter();
		List<Entity> validTargets = SpellUtils.getValidTargets(context, player, targets, targetFilter, source);
		Entity randomTarget = null;
		if (validTargets.size() > 0 && desc.getBool(SpellArg.RANDOM_TARGET)) {
			randomTarget = context.getLogic().getRandom(validTargets);
		}

		List<SpellDesc> group = Arrays.asList(SpellUtils.getGroup(context, desc));
		int howMany = desc.getValue(SpellArg.HOW_MANY, context, player, null, source, 3);
		int count = desc.getValue(SpellArg.VALUE, context, player, null, source, 1);
		boolean exclusive = (boolean) desc.getOrDefault(SpellArg.EXCLUSIVE, false);
		List<SpellDesc> allChoices = new ArrayList<SpellDesc>();
		allChoices.addAll(group);

		int[] choices;
		if (source.getAttributes().containsKey(Attribute.CHOICES)
				&& (source.getEntityType() == EntityType.CARD || exclusive)) {
			choices = (int[]) source.getAttributes().get(Attribute.CHOICES);

			for (int i = 0; i < choices.length; i++) {
				SpellDesc chosen = allChoices.get(choices[i]);
				subCast(context, player, desc, source, originalTargets, validTargets, randomTarget, chosen);
			}
		} else {
			choices = new int[count];

			for (int j = 0; j < count; j++) {
				List<SpellDesc> thisRoundsPossibleChoices = new ArrayList<SpellDesc>(allChoices);
				List<SpellDesc> thisRoundsChoices = new ArrayList<SpellDesc>();
				for (int i = 0; i < howMany; i++) {
					SpellDesc spell;
					spell = context.getLogic().removeRandom(thisRoundsPossibleChoices);
					thisRoundsChoices.add(spell);
				}

				if (thisRoundsChoices.isEmpty()) {
					return;
				}

				DiscoverAction discover = SpellUtils.getSpellDiscover(context, player, desc, thisRoundsChoices, source);
				context.getLogic().revealCard(player, discover.getCard());
				SpellDesc chosen = discover.getSpell();

				int chosenIndex = -1;
				for (int k = 0; k < allChoices.size(); k++) {
					if (allChoices.get(k) == chosen) {
						chosenIndex = k;
						break;
					}
				}
				if (chosenIndex == -1) {
					logger.warn("cast {} {}: Could not find SpellDesc {} in choices", context.getGameId(), source, chosen);
				}
				choices[j] = chosenIndex;

				subCast(context, player, desc, source, originalTargets, validTargets, randomTarget, chosen);
			}
			source.getAttributes().put(Attribute.CHOICES, choices);
		}

		if (desc.containsKey(SpellArg.SPELL)) {
			SpellUtils.castChildSpell(context, player, desc.getSpell(), originalSource, null, source);
		}
	}

	@Suspendable
	protected void subCast(GameContext context, Player player, SpellDesc desc, Entity source, List<Entity> originalTargets, List<Entity> validTargets, Entity randomTarget, SpellDesc chosen) {
		if (validTargets.size() > 0 && desc.getBool(SpellArg.RANDOM_TARGET)) {
			onCast(context, player, chosen, source, randomTarget);
		} else if (validTargets.size() == 0 && originalTargets == null) {
			onCast(context, player, chosen, source, null);
		} else {
			// there is at least one target and RANDOM_TARGET flag is not set,
			// cast in on all targets
			for (Entity target : validTargets) {
				context.getSpellTargetStack().push(target.getReference());
				onCast(context, player, chosen, source, target);
				context.getSpellTargetStack().pop();
			}
		}
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		SpellUtils.castChildSpell(context, player, desc, source, target);
	}

}
