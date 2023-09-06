package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.DiscoverAction;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @deprecated The discover option spell is fairly brittle. You should use {@link DiscoverSpell} instead, discovering a
 * 		sequence of cards and playing one by casting or summoning it.
 */
@Deprecated
public class DiscoverOptionSpell extends Spell {

	Logger logger = LoggerFactory.getLogger(DiscoverOptionSpell.class);

	public static SpellDesc create(EntityReference target, SpellDesc spell) {
		Map<SpellArg, Object> arguments = new SpellDesc(DiscoverOptionSpell.class);
		arguments.put(SpellArg.TARGET, target);
		arguments.put(SpellArg.SPELL, spell);
		return new SpellDesc(arguments);
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		List<SpellDesc> spells = new Vector<SpellDesc>();
		SpellDesc[] spellArray = (SpellDesc[]) desc.get(SpellArg.SPELLS);
		spells.addAll(Arrays.asList(spellArray));

		Map<String, Integer> spellOrder = new LinkedHashMap<>();
		for (int i = 0; i < spells.size(); i++) {
			SpellDesc spell = spells.get(i);
			spellOrder.put(spell.getString(SpellArg.NAME), i);
		}

		int count = desc.getValue(SpellArg.HOW_MANY, context, player, target, source, 3);
		int value = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
		boolean exclusive = desc.getBool(SpellArg.EXCLUSIVE);
		List<Integer> chosenSpellInts = new Vector<Integer>();
		List<SpellDesc> shuffledSpells = new Vector<>(spells);

		for (int i = 0; i < value; i++) {
			List<SpellDesc> spellChoices;
			// Don't shuffle the spell choices unless we have to
			if (shuffledSpells.size() > count) {
				Collections.shuffle(shuffledSpells, context.getLogic().getRandom());
				spellChoices = shuffledSpells.stream().limit(count).collect(Collectors.toList());
			} else {
				spellChoices = shuffledSpells;
			}

			if (spellChoices.isEmpty()) {
				continue;
			}

			final DiscoverAction spellDiscover = SpellUtils.getSpellDiscover(context, player, desc, spellChoices, source);
			String chosenSpell = spellDiscover.getSpell().getString(SpellArg.NAME);
			chosenSpellInts.add(spellOrder.get(chosenSpell));

			if (exclusive) {
				shuffledSpells.removeIf(f -> f.getString(SpellArg.NAME).equals(chosenSpell));
			}
		}

		Collections.sort(chosenSpellInts);
		SpellDesc[] chosenSpells = new SpellDesc[chosenSpellInts.size()];
		for (int i = 0; i < chosenSpellInts.size(); i++) {
			chosenSpells[i] = spellArray[chosenSpellInts.get(i)];
		}
		if (chosenSpellInts.size() > 0) {
			SpellDesc metaSpell = MetaSpell.create(target != null ? target.getReference() : null, false, chosenSpells);
			SpellUtils.castChildSpell(context, player, metaSpell, source, target);
		}
	}

}
