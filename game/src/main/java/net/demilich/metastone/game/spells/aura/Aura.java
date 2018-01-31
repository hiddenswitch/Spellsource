package net.demilich.metastone.game.spells.aura;

import java.util.*;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.trigger.BoardChangedTrigger;
import net.demilich.metastone.game.spells.trigger.EventTrigger;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.Zones;

public class Aura extends Enchantment {
	private EntityReference targets;
	private SpellDesc applyAuraEffect;
	private SpellDesc removeAuraEffect;
	private EntityFilter entityFilter;

	private SortedSet<Integer> affectedEntities = new TreeSet<>();

	public Aura(AuraDesc desc) {
		this(desc.getApplyEffect(), desc.getRemoveEffect(), desc.getTarget());
		setEntityFilter(desc.getFilter());
	}

	public Aura(EventTrigger secondaryTrigger, SpellDesc applyAuraEffect, SpellDesc removeAuraEffect, EntityReference targetSelection, EntityFilter entityFilter) {
		super(new BoardChangedTrigger(), secondaryTrigger, applyAuraEffect, false);
		this.applyAuraEffect = applyAuraEffect;
		this.removeAuraEffect = removeAuraEffect;
		this.targets = targetSelection;
		this.entityFilter = entityFilter;
	}

	public Aura(EventTrigger secondaryTrigger, SpellDesc applyAuraEffect, SpellDesc removeAuraEffect, EntityReference targetSelection) {
		this(secondaryTrigger, applyAuraEffect, removeAuraEffect, targetSelection, null);
	}

	public Aura(SpellDesc applyAuraEffect, SpellDesc removeAuraEffect, EntityReference targetSelection) {
		this(null, applyAuraEffect, removeAuraEffect, targetSelection);
	}

	protected boolean affects(GameContext context, Player player, Entity target, List<Entity> resolvedTargets) {
		if (getEntityFilter() != null && !getEntityFilter().matches(context, player, target, context.resolveSingleTarget(getHostReference()))) {
			return false;
		}

		return resolvedTargets.contains(target);
	}

	@Override
	public Aura clone() {
		Aura clone = (Aura) super.clone();
		clone.targets = this.targets;
		clone.applyAuraEffect = this.applyAuraEffect.clone();
		clone.removeAuraEffect = this.removeAuraEffect.clone();
		clone.affectedEntities = new TreeSet<>(this.affectedEntities);
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
}

