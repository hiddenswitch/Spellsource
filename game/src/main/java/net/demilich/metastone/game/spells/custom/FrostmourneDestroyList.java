package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.Environment;
import net.demilich.metastone.game.EnvironmentValue;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class FrostmourneDestroyList implements EnvironmentValue {
	public static FrostmourneDestroyList getList(GameContext context) {
		context.getEnvironment().putIfAbsent(Environment.FROSTMOURNE_DESTROY_LIST, new FrostmourneDestroyList());
		FrostmourneDestroyList list = (FrostmourneDestroyList) context.getEnvironment().get(Environment.FROSTMOURNE_DESTROY_LIST);
		return list;
	}

	Map<EntityReference, List<EntityReference>> data = new HashMap<>();

	@Override
	public EnvironmentValue getCopy() {
		FrostmourneDestroyList copy = new FrostmourneDestroyList();
		for (Map.Entry<EntityReference, List<EntityReference>> kv : data.entrySet()) {
			ArrayList<EntityReference> value = new ArrayList<>();
			value.addAll(kv.getValue());
			copy.data.put(kv.getKey(), value);
		}
		return copy;
	}

	public void add(Entity source, Entity target) {
		data.putIfAbsent(source.getReference(), new ArrayList<>());
		data.get(source.getReference()).add(target.getReference());
	}

	public void clear(Entity source) {
		data.remove(source.getReference());
	}

	public CardList resurrectable(GameContext context, Entity source) {
		CardArrayList cards = new CardArrayList();
		if (!data.containsKey(source.getReference())) {
			return cards;
		}
		data.get(source.getReference())
				.stream()
				.map(context::resolveSingleTarget)
				.map(Entity::getSourceCard)
				.forEach(cards::addCard);
		return cards;
	}
}
