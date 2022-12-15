package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.Arrays;
import java.util.Map;

/**
 * A class that defines a collection of spells that should be executed one after another in the {@link SpellArg#SPELLS}
 * argument.
 * <p>
 * If a {@link SpellArg#VALUE} is provided, its value will be calculated before the sub spells are evaluated and that
 * value will be stored as the result of a {@link net.demilich.metastone.game.spells.desc.valueprovider.GameValueProvider}
 * set to provide the {@link net.demilich.metastone.game.spells.desc.valueprovider.ValueProviderArg#GAME_VALUE} of
 * {@link GameValue#SPELL_VALUE}. This is useful for calculating a value before effects occur.
 * <p>
 * For <b>example</b>, to implement the text, "Destroy all minions. Draw a card for each," it's important to destroy the
 * minions first and then draw cards, because a deathrattle may have shuffled cards into your deck. A naive
 * implementation would draw the card first based on how many minions are on the board. But by using the {@code
 * GameValueProvider}, we can do things in the right order:
 * <pre>
 *   {
 *     "class": "MetaSpell",
 *     "value": {
 *       "class": "EntityCountValueProvider",
 *       "target": "ALL_MINIONS",
 *       "filter": {
 *         "class": "AttributeFilter",
 *         "attribute": "IMMUNE",
 *         "invert": true
 *       }
 *     },
 *     "spells": [
 *       {
 *         "class": "DestroySpell",
 *         "target": "ALL_MINIONS"
 *       },
 *       {
 *         "class": "DrawCardSpell",
 *         "value": {
 *           "class": "GameValueProvider",
 *           "gameValue": "SPELL_VALUE"
 *         }
 *       }
 *     ]
 *   }
 * </pre>
 *
 * @see GameContext#getSpellValueStack() for more about the spell value stack.
 */
public class MetaSpell extends Spell {
	public static SpellDesc create(EntityReference target, boolean randomTarget, SpellDesc... spells) {
		Map<SpellArg, Object> arguments = new SpellDesc(MetaSpell.class);
		arguments.put(SpellArg.TARGET, target);
		arguments.put(SpellArg.SPELLS, spells);
		arguments.put(SpellArg.RANDOM_TARGET, randomTarget);
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(SpellDesc... spells) {
		Map<SpellArg, Object> arguments = new SpellDesc(MetaSpell.class);
		arguments.put(SpellArg.SPELLS, spells);
		return new SpellDesc(arguments);
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		if (desc.containsKey(SpellArg.VALUE)) {
			context.getSpellValueStack().addLast(desc.getValue(SpellArg.VALUE, context, player, target, source, 0));
		}
		// Manually obtain sub spells for performance reasons, this is accessed very often
		SpellDesc spell = (SpellDesc) desc.get(SpellArg.SPELL);
		// We mutate an array here so we must clone the desc's spells array
		SpellDesc[] originalSpellDescs = (SpellDesc[]) desc.get(SpellArg.SPELLS);
		SpellDesc[] spells;
		if (originalSpellDescs != null) {
			spells = Arrays.copyOf(originalSpellDescs, originalSpellDescs.length);
		} else {
			spells = new SpellDesc[0];
		}
		SpellDesc spell1 = (SpellDesc) desc.get(SpellArg.SPELL1);
		SpellDesc spell2 = (SpellDesc) desc.get(SpellArg.SPELL2);
		int howMany = desc.getValue(SpellArg.HOW_MANY, context, player, target, source, 1);
		// Pass down the CARD arg if it's specified on this desc. This allows DiscoverSpells to correctly use MetaSpell.
		if (desc.containsKey(SpellArg.CARD)) {
			if (spell != null) {
				spell = spell.addArg(SpellArg.CARD, desc.get(SpellArg.CARD));
			}
			for (int i = 0; i < spells.length; i++) {
				spells[i] = spells[i].addArg(SpellArg.CARD, desc.get(SpellArg.CARD));
			}
			if (spell1 != null) {
				spell1 = spell1.addArg(SpellArg.CARD, desc.get(SpellArg.CARD));
			}
			if (spell2 != null) {
				spell2 = spell2.addArg(SpellArg.CARD, desc.get(SpellArg.CARD));
			}
		}
		for (int i = 0; i < howMany; i++) {
			if (spell != null) {
				each(context, player, source, target, spell);
			}
			for (SpellDesc subSpell : spells) {
				each(context, player, source, target, subSpell);
			}
			if (spell1 != null) {
				each(context, player, source, target, spell1);
			}
			if (spell2 != null) {
				each(context, player, source, target, spell2);
			}
		}
		if (desc.containsKey(SpellArg.VALUE)) {
			context.getSpellValueStack().pollLast();
		}
	}

	/**
	 * Override to augment each effect.
	 *
	 * @param context
	 * @param player
	 * @param source
	 * @param target
	 * @param spell
	 */
	protected void each(GameContext context, Player player, Entity source, Entity target, SpellDesc spell) {
		SpellUtils.castChildSpell(context, player, spell, source, target);
	}
}
