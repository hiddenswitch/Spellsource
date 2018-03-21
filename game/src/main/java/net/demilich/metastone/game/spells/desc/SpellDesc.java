package net.demilich.metastone.game.spells.desc;

import co.paralleluniverse.fibers.Suspendable;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.desc.Desc;
import net.demilich.metastone.game.cards.desc.SpellDescDeserializer;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.MetaSpell;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.filter.*;
import net.demilich.metastone.game.spells.desc.source.CardSource;
import net.demilich.metastone.game.spells.desc.source.CatalogueSource;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.utils.Attribute;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * A definition for a spell.
 * <p>
 * A spell description has a variety of arguments of type {@link SpellArg}. Each {@link SpellArg} is transformed into a
 * "camelCase" form and become the keys of the JSON version of an instance.
 * <p>
 * For example, the following JSON implements the spell effect, "Summon a Bloodfen Raptor. Summon an extra one for each
 * attack your Hero has.":
 * <pre>
 *     {
 *         "class": "SummonSpell",
 *         "card": "minion_bloodfen_raptor",
 *         "value": {
 *             "class": "AttributeValueProvider",
 *             "target": "FRIENDLY_HERO",
 *             "attribute": "ATTACK",
 *             "offset": 1
 *         }
 *     }
 * </pre>
 * This JSON would deserialize into a {@link SpellDesc} instance that would equal the following code:
 * <p>
 * <pre>
 *      final Map<SpellArg, Object> arguments = SpellDesc.build(SummonSpell.class);
 *      arguments.put(SpellArg.CARD, "minion_bloodfen_raptor");
 *      final Map<ValueProviderArg, Object> valueProvider = ValueProviderDesc.build(AttributeValueProvider.class);
 *      valueProvider.put(ValueProviderArg.TARGET, EntityReference.FRIENDLY_HERO);
 *      valueProvider.put(ValueProviderArg.ATTRIBUTE, Attribute.ATTACK);
 *      valueProvider.put(ValueProviderArg.OFFSET, 1);
 *      arguments.put(SpellArg.VALUE, new ValueProviderDesc(valueProvider).createInstance());
 *      SpellDesc spellDesc = new SpellDesc(arguments);
 * </pre>
 * Notice that the keys of the objects in the JSON are transformed, "camelCase", from the names in the {@code enum} in
 * {@link SpellArg}.
 * <p>
 * <h3>Deathrattles</h3>
 * <p>
 * This class also describes an actor's deathrattle.
 * <p>
 * The spell here is cast with the dying minion as the {@code source} (e.g., {@link
 * net.demilich.metastone.game.targeting.EntityReference#SELF} will refer to the now-destroyed minion).
 * <p>
 * Deathrattles are resolved whenever an actor is destroyed before an {@link GameLogic#endOfSequence()} occurs, which is
 * generally at the end of any action besides discovering.
 *
 * @see SpellDescDeserializer for the official interpretation of each of the attributes (how they are converted from
 * JSON to a concrete value in the game).
 */
@JsonDeserialize(using = SpellDescDeserializer.class)
public class SpellDesc extends Desc<SpellArg, Spell> {

	public SpellDesc() {
		super();
	}

	public SpellDesc(Map<SpellArg, Object> arguments) {
		super(arguments);
	}

	@Override
	protected Class<? extends Desc> getDescImplClass() {
		return SpellDesc.class;
	}

	@Override
	public SpellArg getClassArg() {
		return SpellArg.CLASS;
	}

	public SpellDesc(Class<? extends Spell> spellClass) {
		super(spellClass);
	}

	public SpellDesc addArg(SpellArg spellArg, Object value) {
		SpellDesc clone = clone();
		clone.put(spellArg, value);
		return clone;
	}

	public SpellDesc removeArg(SpellArg spellArg) {
		SpellDesc clone = clone();
		clone.remove(spellArg);
		return clone;
	}

	@Override
	public SpellDesc clone() {
		return (SpellDesc) copyTo(new SpellDesc(getDescClass()));
	}

	public EntityFilter getEntityFilter() {
		return (EntityFilter) get(SpellArg.FILTER);
	}

	public int getInt(SpellArg spellArg, int defaultValue) {
		return containsKey(spellArg) ? (int) get(spellArg) : defaultValue;
	}

	public EntityReference getTarget() {
		return (EntityReference) get(SpellArg.TARGET);
	}

	public TargetPlayer getTargetPlayer() {
		return (TargetPlayer) get(SpellArg.TARGET_PLAYER);
	}

	public boolean hasPredefinedTarget() {
		return get(SpellArg.TARGET) != null;
	}

	@Override
	public String toString() {
		String result = "[SpellDesc arguments= {\n";
		for (SpellArg spellArg : keySet()) {
			result += "\t" + spellArg + ": " + get(spellArg) + "\n";
		}
		result += "}";
		return result;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		return super.equals(other);
	}

	public void setTarget(EntityReference target) {
		put(SpellArg.TARGET, target);
	}

	public List<SpellDesc> subSpells(final int depth) {
		Stream<SpellDesc> spells;
		SpellDesc[] spellsArray = (SpellDesc[]) get(SpellArg.SPELLS);
		if (spellsArray != null && spellsArray.length > 0) {
			spells = Stream.concat(Stream.of(spellsArray),
					Stream.of(spellsArray).flatMap(s -> s.subSpells().stream())
			);
		} else {
			spells = Stream.empty();
		}
		List<SpellDesc> units = Stream.of(SpellArg.SPELL, SpellArg.SPELL1, SpellArg.SPELL2)
				.map(this::get)
				.filter(Objects::nonNull)
				.map(o -> (SpellDesc) o).collect(toList());

		Stream<SpellDesc> unitSpells;
		if (depth == 0) {
			unitSpells = units.stream();
		} else {
			unitSpells = Stream.concat(units.stream(), units.stream().flatMap(u -> u.subSpells(depth - 1).stream()));
		}

		return Stream.concat(spells, unitSpells).collect(Collectors.toList());
	}

	public List<SpellDesc> subSpells() {
		return subSpells(20);
	}

	/**
	 * Joins a spell description with another spell using a {@link MetaSpell}.
	 *
	 * @param masterSpell The spell from which to inherit the {@link EntityFilter}, {@link TargetPlayer}, {@link
	 *                    SpellArg#TARGET} and {@link SpellArg#RANDOM_TARGET} attributes to put into the {@link
	 *                    MetaSpell}.
	 * @param childSpells The spells that will occur after the {@code masterSpell} is casted.
	 * @return A new {@link SpellDesc}.
	 */
	public static SpellDesc join(SpellDesc masterSpell, SpellDesc... childSpells) {
		// Remove nulls
		childSpells = Arrays.stream(childSpells).filter(Objects::nonNull).toArray(SpellDesc[]::new);

		if (masterSpell == null) {
			if (childSpells == null || childSpells.length == 0) {
				return null;
			} else if (childSpells.length == 1) {
				return childSpells[0].clone();
			} else {
				return SpellDesc.join(childSpells[0], Arrays.copyOfRange(childSpells, 1, childSpells.length));
			}
		} else if (childSpells == null || childSpells.length == 0) {
			return masterSpell.clone();
		}

		SpellDesc[] descs = new SpellDesc[childSpells.length + 1];
		descs[0] = masterSpell;
		System.arraycopy(childSpells, 0, descs, 1, childSpells.length);
		SpellDesc newDesc = join(masterSpell.getTarget(), masterSpell.getBool(SpellArg.RANDOM_TARGET), descs);
		if (masterSpell.getEntityFilter() != null) {
			newDesc.put(SpellArg.FILTER, masterSpell.getEntityFilter());
		}
		if (masterSpell.getTargetPlayer() != null) {
			newDesc.put(SpellArg.TARGET_PLAYER, masterSpell.getTargetPlayer());
		}
		return newDesc;
	}

	private static SpellDesc join(EntityReference target, boolean randomTarget, SpellDesc... descs) {
		return MetaSpell.create(target, randomTarget, descs);
	}

	public EntityFilter getCardFilter() {
		return (EntityFilter) get(SpellArg.CARD_FILTER);
	}

	public CardSource getCardSource() {
		return (CardSource) get(SpellArg.CARD_SOURCE);
	}

	@Suspendable
	public CardList getFilteredCards(GameContext context, Player player, Entity host) {
		CardSource source = getCardSource();
		final EntityFilter filter;
		if (source == null) {
			source = CatalogueSource.create();
		}
		if (containsKey(SpellArg.CARD_FILTER)) {
			filter = getCardFilter();
		} else {
			filter = AndFilter.create();
		}
		return source.getCards(context, host, player).filtered(c -> filter.matches(context, player, c, host));
	}

	public Attribute getAttribute() {
		return (Attribute) get(SpellArg.ATTRIBUTE);
	}

	public EntityReference getSecondaryTarget() {
		return (EntityReference) get(SpellArg.SECONDARY_TARGET);
	}
}
