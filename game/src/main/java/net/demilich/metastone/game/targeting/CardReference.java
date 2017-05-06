package net.demilich.metastone.game.targeting;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

public class CardReference implements Serializable {
	private int playerId;
	private Zones zone;
	private int cardId;
	private String cardName;

	protected CardReference() {
		zone = Zones.NONE;
	}

	public CardReference(int playerId, Zones zone, int cardId, String cardName) {
		this.playerId = playerId;
		this.zone = zone;
		this.cardId = cardId;
		this.cardName = cardName;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CardReference)) {
			return false;
		}
		CardReference rhs = (CardReference) obj;
		return new EqualsBuilder()
				.append(getCardId(), rhs.getCardId())
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(getCardId())
				.toHashCode();
	}

	public int getCardId() {
		return cardId;
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
		return String.format("[CardReference playerId:%d cardName:%s cardLocation:%s cardId:%d]", playerId, cardName, zone.toString(),
				cardId);
	}

}
