package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.environment.Environment;
import net.demilich.metastone.game.environment.EnvironmentValue;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.cards.Attribute;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores a list of entities in the environment, instead of on an attribute on an entity.
 * <p>
 * This class behaves like a multimap of {@code source} entity keys and {@link EntityReference} values. Use {@link
 * #getList(GameContext)} to retrieve the general entity list, or use {@link #getList(GameContext, Environment)} for an
 * entity list named by a particular environment variable.
 * <p>
 * Use {@link #getCards(GameContext, Entity)} to get a list of cards associated with a specific {@code source} entity.
 */
public final class EnvironmentEntityList implements EnvironmentValue, Serializable {
	/**
	 * @param context
	 * @return
	 */
	public static EnvironmentEntityList getList(GameContext context) {
		return getList(context, Environment.ENTITY_LIST);
	}

	public static EnvironmentEntityList getList(GameContext context, Environment environmentVariable) {
		context.getEnvironment().putIfAbsent(environmentVariable, new EnvironmentEntityList());
		return (EnvironmentEntityList) context.getEnvironment().get(environmentVariable);
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

	public List<EntityReference> getReferences(GameContext context, Entity source) {
		return new ArrayList<>(data.getOrDefault(source.getReference(), new ArrayList<>()));
	}

	public CardList getCards(GameContext context, Entity source) {
		CardArrayList cards = new CardArrayList();
		if (!data.containsKey(source.getReference())) {
			return cards;
		}
		data.get(source.getReference())
				.stream()
				.map(ref -> context.resolveSingleTarget(ref, false))
				.map(e -> {
					if (e.hasAttribute(Attribute.CHOICE_SOURCE)) {
						return context.resolveSingleTarget((EntityReference) e.getAttribute(Attribute.CHOICE_SOURCE));
					}
					return e;
				})
				.map(Entity::getSourceCard)
				.map(e -> (Card) e.transformResolved(context))
				.forEach(cards::addCard);
		return cards;
	}
}

