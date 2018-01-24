package net.demilich.metastone.game.spells.trigger;

import co.paralleluniverse.fibers.Suspendable;
import com.google.gson.annotations.Expose;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.events.HasValue;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

import static net.demilich.metastone.game.GameContext.PLAYER_1;
import static net.demilich.metastone.game.GameContext.PLAYER_2;

public class Enchantment extends Entity implements Trigger {
	private final static Logger logger = LoggerFactory.getLogger(Enchantment.class);
	private EventTrigger primaryTrigger;
	private EventTrigger secondaryTrigger;
	@Expose(serialize = false, deserialize = false)
	private SpellDesc spell;
	private EntityReference hostReference;
	private final boolean oneTurn;
	private boolean expired;
	private boolean persistentOwner;
	private int turnDelay;
	private Integer maxFires;
	private int fires;
	private boolean keepAfterTransform;
	private Card sourceCard;
	private Integer countUntilCast;
	private boolean countByValue;
	private boolean activatesImmediately = true;

	protected Enchantment(EventTrigger primaryTrigger, EventTrigger secondaryTrigger, SpellDesc spell, boolean oneTurn, int turnDelay) {
		this.primaryTrigger = primaryTrigger;
		this.secondaryTrigger = secondaryTrigger;
		this.spell = spell;
		this.oneTurn = oneTurn;
		this.turnDelay = turnDelay;
	}

	public Enchantment(EventTrigger primaryTrigger, EventTrigger secondaryTrigger, SpellDesc spell, boolean oneTurn) {
		this(primaryTrigger, secondaryTrigger, spell, oneTurn, 0);
	}

	public Enchantment(EventTrigger trigger, SpellDesc spell) {
		this(trigger, spell, false, 0);
	}

	public Enchantment(EventTrigger trigger, SpellDesc spell, boolean oneTime, int turnDelay) {
		this(trigger, null, spell, oneTime, turnDelay);
	}

	@Override
	public Enchantment clone() {
		Enchantment clone = (Enchantment) super.clone();
		clone.primaryTrigger = (EventTrigger) primaryTrigger.clone();
		if (secondaryTrigger != null) {
			clone.secondaryTrigger = (EventTrigger) secondaryTrigger.clone();
		}
		clone.spell = getSpell().clone();
		return clone;
	}

	@Override
	public EntityType getEntityType() {
		return EntityType.ENCHANTMENT;
	}

	public void expire() {
		expired = true;
	}

	@Override
	public EntityReference getHostReference() {
		return hostReference;
	}

	@Override
	public int getOwner() {
		return primaryTrigger.getOwner();
	}

	public SpellDesc getSpell() {
		return spell;
	}

	@Override
	public boolean interestedIn(GameEventType eventType) {
		boolean result = primaryTrigger.interestedIn() == eventType || primaryTrigger.interestedIn() == GameEventType.ALL;
		if (secondaryTrigger != null) {
			result |= secondaryTrigger.interestedIn() == eventType || secondaryTrigger.interestedIn() == GameEventType.ALL;
		}
		return result;
	}

	@Override
	public boolean isExpired() {
		return expired;
	}

	@Override
	public void onAdd(GameContext context) {
	}

	/**
	 * Processes a firing. If the
	 *
	 * @param ownerId
	 * @param spell
	 * @param event
	 * @return
	 */
	@Suspendable
	protected boolean onFire(int ownerId, SpellDesc spell, GameEvent event) {
		if (countByValue && event instanceof HasValue) {
			fires += ((HasValue) event).getValue();
		} else {
			fires++;
		}

		boolean spellCasts = true;
		if (countUntilCast != null && fires < countUntilCast) {
			spellCasts = false;
		}
		if (spellCasts) {
			event.getGameContext().getLogic().castSpell(ownerId, spell, hostReference, null, true);
		}
		if (maxFires != null
				&& fires >= maxFires) {
			expire();
		}
		return spellCasts;
	}

	@Override
	@Suspendable
	public void onGameEvent(GameEvent event) {
		if (expired) {
			return;
		}

		int ownerId = primaryTrigger.getOwner();

		// Expire the trigger beforehand, in case of copying minion (Echoing
		// Ooze). Since this method should only be called
		// after being checked to be played, copying one-turn triggers should no
		// longer matter.
		// But let's check to make sure we don't accidentally expire something
		// that's still using it.
		if (oneTurn && (event.getEventType() == GameEventType.TURN_END || event.getEventType() == GameEventType.TURN_START)) {
			expire();
		}

		// Notify the game context that a spell trigger was successfully fired, as long as it wasn't due to a
		// board changed event.
		if (event.getEventType() != GameEventType.BOARD_CHANGED
				&& primaryTrigger.interestedIn() != GameEventType.ALL
				&& !(hostReference.equals(new EntityReference(PLAYER_1))
				|| hostReference.equals(new EntityReference(PLAYER_2)))) {
			event.getGameContext().onEnchantmentFired(this);
		}
		onFire(ownerId, getSpell(), event);
	}

	@Override
	@Suspendable
	public void onRemove(GameContext context) {
		expire();
	}

	@Override
	public void setHost(Entity host) {
		this.hostReference = host.getReference();
	}

	@Override
	public void setOwner(int playerIndex) {
		primaryTrigger.setOwner(playerIndex);
		if (secondaryTrigger != null) {
			secondaryTrigger.setOwner(playerIndex);
		}
	}

	@Override
	public Enchantment getCopy() {
		return this.clone();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("id", getId())
				.append("sourceCard", getSourceCard() == null ? null : getSourceCard().getCardId())
				.toString();
	}

	@Override
	public boolean canFire(GameEvent event) {
		if (turnDelay > 0) {
			return false;
		}

		Entity host = event.getGameContext().resolveSingleTarget(hostReference);
		return (triggerFires(primaryTrigger, event, host) || triggerFires(secondaryTrigger, event, host));
	}

	private boolean triggerFires(EventTrigger trigger, GameEvent event, Entity host) {
		if (trigger == null) {
			return false;
		}
		if (trigger.interestedIn() != event.getEventType() && trigger.interestedIn() != GameEventType.ALL) {
			return false;
		}
		return trigger.fires(event, host);
	}

	public boolean hasPersistentOwner() {
		return persistentOwner;
	}

	public void setPersistentOwner(boolean persistentOwner) {
		this.persistentOwner = persistentOwner;
	}

	public void delayTimeDown() {
		if (turnDelay > 0) {
			turnDelay--;
		}
	}

	public boolean isDelayed() {
		return turnDelay > 0;
	}

	public boolean oneTurnOnly() {
		return oneTurn;
	}

	public boolean canFireCondition(GameEvent event) {
		if (primaryTrigger.canFireCondition(event) || (secondaryTrigger != null && secondaryTrigger.canFireCondition(event))) {
			return true;
		}
		return false;
	}

	public Card getSourceCard() {
		return sourceCard;
	}

	public int getFires() {
		return fires;
	}

	public Integer getMaxFires() {
		return maxFires;
	}

	public void setMaxFires(Integer maxFires) {
		this.maxFires = maxFires;
	}

	public void setKeepAfterTransform(boolean keepAfterTransform) {
		this.keepAfterTransform = keepAfterTransform;
	}

	public boolean isKeptAfterTransform() {
		return keepAfterTransform;
	}

	public void setSourceCard(Card sourceCard) {
		this.sourceCard = sourceCard;
	}

	public void setCountUntilCast(Integer countUntilCast) {
		this.countUntilCast = countUntilCast;
	}

	public Integer getCountUntilCast() {
		return countUntilCast;
	}

	public void setSpell(SpellDesc spell) {
		this.spell = spell;
	}

	public void setCountByValue(boolean countByValue) {
		this.countByValue = countByValue;
	}

	public boolean isCountByValue() {
		return countByValue;
	}
}
