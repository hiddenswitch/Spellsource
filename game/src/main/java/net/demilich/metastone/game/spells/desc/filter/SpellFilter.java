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

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
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
		SpellDesc testForSpell = (SpellDesc) getDesc().get(EntityFilterArg.SPELL);
		for (SpellDesc spellDesc : spellDescs) {
			boolean test = true;
			for (Map.Entry<SpellArg, Object> spellArgObjectEntry : testForSpell.entrySet()) {
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
