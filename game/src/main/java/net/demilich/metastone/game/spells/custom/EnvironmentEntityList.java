package net.demilich.metastone.game.spells.custom;

import com.google.common.collect.Lists;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.environment.Environment;
import net.demilich.metastone.game.environment.EnvironmentValue;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.cards.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

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
	private static Logger LOGGER = LoggerFactory.getLogger(EnvironmentEntityList.class);

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
		if (Objects.equals(target.getReference(), EntityReference.NONE)) {
			throw new NullPointerException(String.format("%s tried to store %s which has a NONE reference!", source, target));
		}

		data.putIfAbsent(source.getReference(), new ArrayList<>());
		data.get(source.getReference()).add(target.getReference());
	}

	public void clear(Entity source) {
		data.remove(source.getReference());
	}

	/**
	 * Gets a list of referenced cards. This reference will always reflect the underlying list as it gets changed, but is
	 * not mutable
	 *
	 * @param source
	 * @return
	 * @see #add(Entity, Entity) to mutate this list.
	 */
	public List<EntityReference> getReferences(Entity source) {
		return Collections.unmodifiableList(data.computeIfAbsent(source.getReference(), (k) -> new ArrayList<>()));
	}

	/**
	 * Retrieves a read-only view of a list of cards from this list.
	 *
	 * @param context
	 * @param source
	 * @return
	 */
	public List<Card> getCards(GameContext context, Entity source) {
		return Lists.transform(data.computeIfAbsent(source.getReference(), k -> new ArrayList<>()), ref -> {
			if (Objects.equals(ref, EntityReference.NONE) || ref.isTargetGroup()) {
				var message = String.format("%s %s: stored a NONE or target group reference: %s", context.getGameId(), source, ref);
				throw new NullPointerException(message);
			}
			var e = context.resolveSingleTarget(ref, false);
			Entity e1;
			if (e.hasAttribute(Attribute.CHOICE_SOURCE)) {
				e1 = context.resolveSingleTarget((EntityReference) e.getAttribute(Attribute.CHOICE_SOURCE));
			} else {
				e1 = e;
			}
			var sourceCard = e1.getSourceCard();
			return (Card) sourceCard.transformResolved(context);
		});
	}
}

