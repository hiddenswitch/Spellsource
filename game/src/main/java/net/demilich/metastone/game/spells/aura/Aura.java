package net.demilich.metastone.game.spells.aura;

import com.github.fromage.quasi.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.desc.Desc;
import net.demilich.metastone.game.cards.desc.HasDesc;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.events.BoardChangedEvent;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.WillEndSequenceEvent;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.aura.AuraArg;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.desc.condition.Condition;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.trigger.BoardChangedTrigger;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.spells.trigger.EventTrigger;
import net.demilich.metastone.game.spells.trigger.WillEndSequenceTrigger;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.Zones;

import java.util.*;

/**
 * Auras represent ongoing effects applied to certain entities and is updated whenever (1) the board changes, (2) a
 * sequence ends, (3) a special secondary trigger is fired, or (4) a condition is changed during these earlier events.
 * <p>
 * Because auras evaluate which entities they affect on board changes and sequence endings, they aren't affecting
 * entities the moment they "come into play" (are attached to a host that is {@link Entity#isInPlay()}). However, since
 * a {@link BoardChangedEvent} is fired right after a minion is put on the {@link Zones#BATTLEFIELD} during a {@link
 * net.demilich.metastone.game.logic.GameLogic#summon(int, Minion, Card, int, boolean)} call, in practice auras come
 * into play immediately. Specifically, a minion with an aura written on it will not be affecting entities by the {@link
 * net.demilich.metastone.game.events.BeforeSummonEvent} event, but only by the first {@link BoardChangedEvent} (which
 * comes before any battlecries are resolved or before control is given back to the player).
 * <p>
 * Auras have the following format (corresponding to {@link AuraDesc}):
 * <pre>
 *   {
 *                "class": An Aura class. When the class is Aura, the apply and remove effects below are used. Otherwise,
 *                         for classes like BuffAura, the apply and remove effects are provided by the class.
 *               "target": An {@link EntityReference} for all the entities affected by this aura
 *               "filter": A filter on those entities. Evaluated against every {@code target} entity in the
 *                         {@code "target"} specified.
 *          "applyEffect": A {@link SpellDesc} that corresponds to the spell to cast on a given entity when it transitions
 *                         from being unaffected to affected by this aura. The {@code target} is the entity newly under
 *                         the influence of the aura and {@code source} is the {@link EntityReference#TRIGGER_HOST} /
 *                         host of the aura.
 *         "removeEffect": A {@link SpellDesc} that corresponds to the spell to cast on an entity when it was previously
 *                         affected and it is now transitioning into not being affected. The {@code target} is the
 *                         entity newly under the influence of the aura and {@code source} is the
 *                         {@link EntityReference#TRIGGER_HOST} / host of the aura.
 *            "condition": A condition that is evaluated whenever the {@link BoardChangedEvent} is raised; the
 *                         {@link WillEndSequenceEvent} is raised; or "secondaryTrigger" is fired, against every entity that
 *                         could be or currently is affected by this aura. When the condition is true, the entity that is
 *                         affected remains affected; the entity that could not be affected is affected. When false, the entity
 *                         that is affected stops being affected, and an entity that is not yet affected will still not be
 *                         affected. {@code target} in the condition will be the entity, and {@code source} will be the
 *                         {@link EntityReference#TRIGGER_HOST} / host of the aura.
 *     "secondaryTrigger": Another trigger that, when fired, will cause this aura to reevaluate which entities are
 *                         affected. {@link EntityReference#EVENT_TARGET} will correspond to the
 *                         {@link GameEvent#getTarget()} processed by that trigger.
 *   }
 * </pre>
 * <p>
 * An aura is not an {@code abstract} class; it can be directly specified using an {@link AuraDesc} with a specific
 * {@link AuraArg#APPLY_EFFECT} and {@link AuraArg#REMOVE_EFFECT}. The remove effect should reverse the consequences of
 * the add effect. Since this is challenging to come up with without a background in software engineering, the {@link
 * BuffAura} and {@link AttributeAura} should cover most cases of ongoing effects correctly.
 * <p>
 * The {@link #onGameEvent(GameEvent)} method actually implements the evaluation of the condition, the filter, the
 * target and the add/remove effects. Observe that unlike an {@link Enchantment}, which it inherits, auras do not
 * respect configuration features like {@link #maxFires}. It is unclear how such features should be interpreted.
 *
 * @see BuffAura for an aura that increases stats.
 * @see AttributeAura for an aura that adds an attribute
 * @see CardAura that temporarily makes one card behave like another
 */
public class Aura extends Enchantment implements HasDesc<AuraDesc> {
	private EntityReference targets;
	protected SpellDesc applyAuraEffect;
	protected SpellDesc removeAuraEffect;
	private EntityFilter entityFilter;
	private Condition condition;
	private SortedSet<Integer> affectedEntities = new TreeSet<>();
	private AuraDesc desc;

	public Aura(AuraDesc desc) {
		this(desc.getSecondaryTrigger() == null ? new WillEndSequenceTrigger() : desc.getSecondaryTrigger().create(), desc.getApplyEffect(), desc.getRemoveEffect(), desc.getTarget());
		setEntityFilter(desc.getFilter());
		setCondition(desc.getCondition());
		setDesc(desc);
	}

	public Aura(EventTrigger secondaryTrigger, SpellDesc applyAuraEffect, SpellDesc removeAuraEffect, EntityReference targetSelection, EntityFilter entityFilter, Condition condition) {
		super(new BoardChangedTrigger(), secondaryTrigger, applyAuraEffect, false);
		this.applyAuraEffect = applyAuraEffect;
		this.removeAuraEffect = removeAuraEffect;
		this.targets = targetSelection;
		this.entityFilter = entityFilter;
		this.condition = condition;
	}

	public Aura(EventTrigger secondaryTrigger, SpellDesc applyAuraEffect, SpellDesc removeAuraEffect, EntityReference targetSelection, EntityFilter entityFilter) {
		this(secondaryTrigger, applyAuraEffect, removeAuraEffect, targetSelection, entityFilter, null);
	}

	public Aura(EventTrigger secondaryTrigger, SpellDesc applyAuraEffect, SpellDesc removeAuraEffect, EntityReference targetSelection) {
		this(secondaryTrigger, applyAuraEffect, removeAuraEffect, targetSelection, null);
	}

	public Aura(SpellDesc applyAuraEffect, SpellDesc removeAuraEffect, EntityReference targetSelection) {
		this(null, applyAuraEffect, removeAuraEffect, targetSelection);
	}

	protected Aura(EventTrigger primaryTrigger, EventTrigger secondaryTrigger, SpellDesc spell) {
		super(primaryTrigger, secondaryTrigger, spell, false);
	}

	protected boolean affects(GameContext context, Player player, Entity target, List<Entity> resolvedTargets) {
		Entity source = context.resolveSingleTarget(getHostReference());
		if (getEntityFilter() != null && !getEntityFilter().matches(context, player, target, source)) {
			return false;
		}

		boolean conditionFulfilled = getCondition() == null || getCondition().isFulfilled(context, player, target, source);
		return conditionFulfilled && resolvedTargets.contains(target);
	}

	@Override
	public Aura clone() {
		Aura clone = (Aura) super.clone();
		clone.targets = this.targets;
		if (applyAuraEffect != null) {
			clone.applyAuraEffect = this.applyAuraEffect.clone();
		}
		if (removeAuraEffect != null) {
			clone.removeAuraEffect = this.removeAuraEffect.clone();
		}
		if (affectedEntities != null) {
			clone.affectedEntities = new TreeSet<>(this.affectedEntities);
		}
		return clone;
	}

	@Override
	public void onAdd(GameContext context) {
		super.onAdd(context);
		affectedEntities.clear();
	}

	@Suspendable
	public void onGameEvent(GameEvent event) {
		GameContext context = event.getGameContext();
		Player owner = context.getPlayer(getOwner());
		Entity source = context.resolveSingleTarget(getHostReference());
		List<Entity> resolvedTargets = context.resolveTarget(owner, source, targets);
		List<Entity> relevantTargets = new ArrayList<Entity>(resolvedTargets);
		for (Iterator<Integer> iterator = affectedEntities.iterator(); iterator.hasNext(); ) {
			int entityId = iterator.next();

			EntityReference entityReference = new EntityReference(entityId);
			Entity affectedEntity = context.tryFind(entityReference);
			if (affectedEntity == null) {
				// It was removed from play or otherwise could not be found.
				iterator.remove();
			} else {
				relevantTargets.add(affectedEntity);
			}
		}

		boolean alwaysApply = getDesc() != null && getDesc().getBool(AuraArg.ALWAYS_APPLY);

		for (Entity target : relevantTargets) {
			if (affects(context, owner, target, resolvedTargets) && (!affectedEntities.contains(target.getId()) || alwaysApply)) {
				affectedEntities.add(target.getId());
				context.getLogic().castSpell(getOwner(), applyAuraEffect, getHostReference(), target.getReference(), true);
				// target is not affected anymore, remove effect
			} else if (!affects(context, owner, target, resolvedTargets) && affectedEntities.contains(target.getId())) {
				affectedEntities.remove(target.getId());
				if (target.getZone().equals(Zones.REMOVED_FROM_PLAY)) {
					continue;
				}
				context.getLogic().castSpell(getOwner(), removeAuraEffect, getHostReference(), target.getReference(), true);
			}
		}
	}

	@Override
	@Suspendable
	public void onRemove(GameContext context) {
		for (int targetId : affectedEntities) {
			EntityReference targetKey = new EntityReference(targetId);
			Entity target = context.tryFind(targetKey);
			if (target != null) {
				context.getLogic().castSpell(getOwner(), removeAuraEffect, getHostReference(), target.getReference(), true);
			}
		}
		affectedEntities.clear();
		super.onRemove(context);
	}

	public EntityFilter getEntityFilter() {
		return entityFilter;
	}

	public void setEntityFilter(EntityFilter entityFilter) {
		this.entityFilter = entityFilter;
	}

	public Condition getCondition() {
		return condition;
	}

	protected void setCondition(Condition condition) {
		this.condition = condition;
	}

	@Override
	public AuraDesc getDesc() {
		return desc;
	}

	@Override
	public void setDesc(Desc<?, ?> desc) {
		this.desc = (AuraDesc) desc;
	}

	public SortedSet<Integer> getAffectedEntities() {
		return affectedEntities;
	}

	public SpellDesc getApplyEffect() {
		return applyAuraEffect;
	}
}

