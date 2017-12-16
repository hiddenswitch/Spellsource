package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.environment.Environment;
import net.demilich.metastone.game.environment.EnvironmentValue;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnvironmentEntityList implements EnvironmentValue {
	public static EnvironmentEntityList getList(GameContext context) {
		return getList(context, Environment.ENTITY_LIST);
	}

	public static EnvironmentEntityList getList(GameContext context, Environment environmentVariable) {
		context.getEnvironment().putIfAbsent(environmentVariable, new EnvironmentEntityList());
		EnvironmentEntityList list = (EnvironmentEntityList) context.getEnvironment().get(environmentVariable);
		return list;
	}

	Map<EntityReference, List<EntityReference>> data = new HashMap<>();

	@Override
	public EnvironmentValue getCopy() {
		EnvironmentEntityList copy = new EnvironmentEntityList();
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

	public CardList getCards(GameContext context, Entity source) {
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
