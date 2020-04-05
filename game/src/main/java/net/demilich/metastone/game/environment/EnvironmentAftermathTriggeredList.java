package net.demilich.metastone.game.environment;

import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Keeps tracks of which aftermaths were triggered this game.
 */
public final class EnvironmentAftermathTriggeredList implements EnvironmentValue, Serializable {

	private List<EnvironmentAftermathTriggeredItem> aftermaths = new ArrayList<>();

	/**
	 * Describes a particular triggering of an aftermath
	 */
	public static class EnvironmentAftermathTriggeredItem implements Cloneable, Serializable {
		// This is immutable here so we do not have to deep clone
		private SpellDesc spell;
		private int playerId;
		private EntityReference source;
		private String cardId;

		public EnvironmentAftermathTriggeredItem(int playerId, EntityReference source, SpellDesc spell, String cardId) {
			this.spell = spell;
			this.playerId = playerId;
			this.source = source;
			this.cardId = cardId;
		}

		/**
		 * The spell, including its aftermath ID.
		 *
		 * @return
		 */
		public SpellDesc getSpell() {
			return spell;
		}

		public EnvironmentAftermathTriggeredItem setSpell(SpellDesc spell) {
			this.spell = spell;
			return this;
		}

		/**
		 * The exact card id of the source at the time the aftermath was triggered
		 * @return
		 */
		public String getCardId() {
			return cardId;
		}

		/**
		 * The player from whose point of view the aftermath was triggered
		 *
		 * @return
		 */
		public int getPlayerId() {
			return playerId;
		}

		public EnvironmentAftermathTriggeredItem setPlayerId(int playerId) {
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

		public EnvironmentAftermathTriggeredItem setSource(EntityReference source) {
			this.source = source;
			return this;
		}

		@Override
		protected EnvironmentAftermathTriggeredItem clone() {
			try {
				return (EnvironmentAftermathTriggeredItem) super.clone();
			} catch (CloneNotSupportedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public EnvironmentAftermathTriggeredList getCopy() {
		EnvironmentAftermathTriggeredList list = new EnvironmentAftermathTriggeredList();
		// The items are immutable so do not deep clone
		list.aftermaths.addAll(this.aftermaths);
		return list;
	}

	/**
	 * Records a aftermath as triggered. The {@code spell} should be immutable because it is not cloned.
	 *
	 * @param playerId
	 * @param source
	 * @param spell
	 */
	public void addAftermath(int playerId, EntityReference source, SpellDesc spell, String cardId) {
		aftermaths.add(new EnvironmentAftermathTriggeredItem(playerId, source, spell, cardId));
	}

	/**
	 * Gets all the aftermaths triggered this game
	 *
	 * @return
	 */
	public List<EnvironmentAftermathTriggeredItem> getAftermaths() {
		return Collections.unmodifiableList(aftermaths);
	}
}
