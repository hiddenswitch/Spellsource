/**
 * Spells contain all the functional pieces of the card JSON (Click on description to learn more about how to use this).
 * <p>
 * Each of these classes correspond to the value part of a spell's {@code "class"} key in the card JSON. For
 * <b>example</b>, if you saw the following in the card JSON:
 * <pre>
 *   {
 *     "class": "DrawCardSpell"
 *   }
 * </pre>
 * You would look up {@link net.demilich.metastone.game.spells.DrawCardSpell} here.
 * <p>
 * Then, {@link net.demilich.metastone.game.spells.desc.SpellArg} referenced in the documentation correspond to keys in
 * the JSON. For example, if you saw the following in the card JSON:
 * <pre>
 *   {
 *     "class": "DrawCardSpell",
 *     "value": 1
 *   }
 * </pre>
 * You'll see that {@link net.demilich.metastone.game.spells.DrawCardSpell} says that {@link
 * net.demilich.metastone.game.spells.desc.SpellArg#VALUE} is the number of cards to draw. That arg refers to the key
 * {@code "value"} in the JSON. We refer to this change in casing as "camelCase"; each spell arg corresponds to a
 * camelCased version in the JSON, where you remove the underscores and start the first letter lowercase, subsequent
 * starts of words uppercase.
 *
 * @see net.demilich.metastone.game.spells.desc.SpellArg for a rough idea of what the args generally mean.
 * @see net.demilich.metastone.game.spells.Spell for the base class of all spell effects (which includes stuff like
 * 		battlecries).
 */
package net.demilich.metastone.game.spells;