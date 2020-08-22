package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.trigger.Aftermath;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Actors will be destroyed.
 * <p>
 * Specifies if an aftermath will be fired, and which one, based on the associated enchantments.
 */
public final class DestroyWillQueue implements Notification {
	public static class DestroyEvent {
		public DestroyEvent(Entity source, Entity target, List<Aftermath> aftermaths) {
			this.source = new WeakReference<>(source);
			this.target = new WeakReference<>(target);
			this.aftermaths = aftermaths.stream().map(WeakReference::new).collect(toList());
		}

		private final WeakReference<Entity> source;
		private final WeakReference<Entity> target;
		private final List<WeakReference<Aftermath>> aftermaths;

		public Entity getSource() {
			return source.get();
		}

		public Entity getTarget() {
			return target.get();
		}

		public List<Aftermath> getAftermaths() {
			return aftermaths.stream().map(Reference::get).collect(toList());
		}
	}

	private final List<DestroyEvent> destroys;

	public DestroyWillQueue(List<DestroyEvent> destroys) {
		this.destroys = destroys;
	}

	@Override
	public Entity getSource() {
		return null;
	}

	@Override
	public List<Entity> getTargets(GameContext context, int player) {
		return destroys.stream().map(DestroyEvent::getTarget).collect(toList());
	}

	@Override
	public boolean isPowerHistory() {
		return false;
	}

	@Override
	public String getDescription(GameContext context, int playerId) {
		return "Units were destroyed.";
	}

	@Override
	public boolean isClientInterested() {
		return true;
	}

	public List<DestroyEvent> getDestroys() {
		return destroys;
	}
}
