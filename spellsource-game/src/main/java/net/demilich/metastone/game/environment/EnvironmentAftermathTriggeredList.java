package net.demilich.metastone.game.environment;

import net.demilich.metastone.game.spells.trigger.Aftermath;
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
	public record EnvironmentAftermathTriggeredItem(
			int playerId,
			Aftermath aftermath,
			EntityReference source,
			String cardId,
			int boardPosition,
			int aftermathId) implements Serializable {

	}

	@Override
	public EnvironmentAftermathTriggeredList getCopy() {
		EnvironmentAftermathTriggeredList list = new EnvironmentAftermathTriggeredList();
		// The items are immutable so do not deep clone
		list.aftermaths.addAll(this.aftermaths);
		return list;
	}

	/**
	 * Records an aftermath was triggered
	 *
	 * @param playerId              the player that triggered it
	 * @param aftermath             the aftermath
	 * @param source                the source of the aftermath
	 * @param cardId                the card ID that put the content on the aftermath
	 * @param boardPositionAbsolute the board position of the actor that died, or -1 if no board position
	 * @param aftermathId           an id during execution
	 */
	public void addAftermath(int playerId, Aftermath aftermath, EntityReference source, String cardId, int boardPositionAbsolute, int aftermathId) {
		aftermaths.add(new EnvironmentAftermathTriggeredItem(playerId, aftermath, source, cardId, boardPositionAbsolute, aftermathId));
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
