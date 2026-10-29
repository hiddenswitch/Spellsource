package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SpellFilter extends EntityFilter {
	public SpellFilter(EntityFilterDesc desc) {
		super(desc);
	}

	protected boolean test(GameContext context, Player player, Entity entity, Entity host, SpellDesc spell) {
		SpellDesc topLevelSpell = null;
		if (entity instanceof Card) {
			Card card = (Card) entity;
			if (card.isSpell()) {
				topLevelSpell = card.getSpell();
			}
		}
		if (topLevelSpell == null) {
			return false;
		}
		List<SpellDesc> spellDescs = topLevelSpell.spellStream(true).collect(Collectors.toList());
		for (SpellDesc spellDesc : spellDescs) {
			boolean test = true;
			for (Map.Entry<SpellArg, Object> spellArgObjectEntry : spell.entrySet()) {
				if (spellDesc.get(spellArgObjectEntry.getKey()) != spellArgObjectEntry.getValue()) {
					test = false;
				}
			}
			if (test) {
				return true;
			}
		}

		return false;
	}
}
