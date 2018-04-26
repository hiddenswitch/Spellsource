package net.demilich.metastone.game.spells.aura;

import java.util.*;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.desc.Desc;
import net.demilich.metastone.game.cards.desc.HasDesc;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.aura.AuraArg;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.desc.condition.Condition;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.trigger.BoardChangedTrigger;
import net.demilich.metastone.game.spells.trigger.EventTrigger;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.Zones;

/**
 * Auras represent ongoing effects applied to certain entities and is updated whenever (1) the board changes, (2) a
 * sequence ends, (3) a special secondary trigger is fired, or (4) a condition is changed during these earlier events.
 * <p>
 * An aura is not an {@code abstract} class; it can be directly specified using an {@link AuraDesc} with a specific
 * {@link AuraArg#APPLY_EFFECT} and {@link AuraArg#REMOVE_EFFECT}. The remove effect should reverse the consequences of
 * the add effect. Since this is challenging to come up with without a background in software engineering, the {@link
 * BuffAura} and {@link AttributeAura} should cover most cases of ongoing effects correctly.
 * <p>
 * The {@link #onGameEvent(GameEvent)} method actually implements the evaluation of the condition, the filter, the
 * target and the add/remove effects. Observe that unlike an {@link Enchantment}, which it inherits, auras do not
 * respect configuration features like {@link #maxFires}. It is unclear how such features should be interpreted.
 */
public class Aura extends Enchantment implements HasDesc<AuraDesc> {
	private EntityReference targets;
	private SpellDesc applyAuraEffect;
	private SpellDesc removeAuraEffect;
	private EntityFilter entityFilter;
	private Condition condition;
	private SortedSet<Integer> affectedEntities = new TreeSet<>();
	private AuraDesc desc;

	public Aura(AuraDesc desc) {
		this(desc.getSecondaryTrigger() == null ? null : desc.getSecondaryTrigger().create(), desc.getApplyEffect(), desc.getRemoveEffect(), desc.getTarget());
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

		for (Entity target : relevantTargets) {
			if (affects(context, owner, target, resolvedTargets) && !affectedEntities.contains(target.getId())) {
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
		this.desc = (AuraDesc)desc;
	}
}

