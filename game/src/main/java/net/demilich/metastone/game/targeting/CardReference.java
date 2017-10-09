package net.demilich.metastone.game.targeting;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

public class CardReference implements Serializable {
	private int playerId;
	private Zones zone;
	private int entityId;
	private String cardName;

	protected CardReference() {
		zone = Zones.NONE;
	}

	public CardReference(int playerId, Zones zone, int entityId, String cardName) {
		this.playerId = playerId;
		this.zone = zone;
		this.entityId = entityId;
		this.cardName = cardName;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CardReference)) {
			return false;
		}
		CardReference rhs = (CardReference) obj;
		return new EqualsBuilder()
				.append(getEntityId(), rhs.getEntityId())
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(getEntityId())
				.toHashCode();
	}

	public int getEntityId() {
		return entityId;
	}

	public String getCardName() {
		return cardName;
	}

	public Zones getZone() {
		return zone;
	}

	public int getPlayerId() {
		return playerId;
	}

	@Override
	public String toString() {
		return String.format("[CardReference playerId:%d cardName:%s cardLocation:%s entityId:%d]", playerId, cardName, zone.toString(),
				entityId);
	}

}
