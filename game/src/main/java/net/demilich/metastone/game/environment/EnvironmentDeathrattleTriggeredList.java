package net.demilich.metastone.game.environment;

import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Keeps tracks of which deathrattles were triggered this game.
 */
public final class EnvironmentDeathrattleTriggeredList implements EnvironmentValue, Serializable {

	private List<EnvironmentDeathrattleTriggeredItem> deathrattles = new ArrayList<>();

	/**
	 * Describes a particular triggering of a deathrattle
	 */
	public static class EnvironmentDeathrattleTriggeredItem implements Cloneable, Serializable {
		// This is immutable here so we do not have to deep clone
		private SpellDesc spell;
		private int playerId;
		private EntityReference source;

		public EnvironmentDeathrattleTriggeredItem(int playerId, EntityReference source, SpellDesc spell) {
			this.spell = spell;
			this.playerId = playerId;
			this.source = source;
		}

		/**
		 * The spell, including its deathrattle ID.
		 *
		 * @return
		 */
		public SpellDesc getSpell() {
			return spell;
		}

		public EnvironmentDeathrattleTriggeredItem setSpell(SpellDesc spell) {
			this.spell = spell;
			return this;
		}

		/**
		 * The player from whose point of view the deathrattle was triggered
		 *
		 * @return
		 */
		public int getPlayerId() {
			return playerId;
		}

		public EnvironmentDeathrattleTriggeredItem setPlayerId(int playerId) {
			this.playerId = playerId;
			return this;
		}

		/**
		 * A reference to the source.
		 *
		 * @return
		 */
		public EntityReference getSource() {
			return source;
		}

		public EnvironmentDeathrattleTriggeredItem setSource(EntityReference source) {
			this.source = source;
			return this;
		}

		@Override
		protected EnvironmentDeathrattleTriggeredItem clone() {
			try {
				return (EnvironmentDeathrattleTriggeredItem) super.clone();
			} catch (CloneNotSupportedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public EnvironmentDeathrattleTriggeredList getCopy() {
		EnvironmentDeathrattleTriggeredList list = new EnvironmentDeathrattleTriggeredList();
		// The items are immutable so do not deep clone
		list.deathrattles.addAll(this.deathrattles);
		return list;
	}

	/**
	 * Records a deathrattle as triggered. The {@code spell} should be immutable because it is not cloned.
	 *
	 * @param playerId
	 * @param source
	 * @param spell
	 */
	public void addDeathrattle(int playerId, EntityReference source, SpellDesc spell) {
		deathrattles.add(new EnvironmentDeathrattleTriggeredItem(playerId, source, spell));
	}

	/**
	 * Gets all the deathrattles triggered this game
	 *
	 * @return
	 */
	public List<EnvironmentDeathrattleTriggeredItem> getDeathrattles() {
		return Collections.unmodifiableList(deathrattles);
	}
}
