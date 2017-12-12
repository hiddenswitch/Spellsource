package net.demilich.metastone.game.spells.trigger.secrets;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.EventTrigger;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.utils.AttributeMap;

public class Secret extends Enchantment {

	private Card source;

	public Secret(EventTrigger trigger, SpellDesc spell, Card source) {
		super(trigger, spell);
		this.source = source;
		setAttributes((AttributeMap) source.getAttributes().clone());
	}

	@Override
	public Card getSourceCard() {
		return source;
	}

	@Override
	@Suspendable
	protected boolean onFire(int ownerId, SpellDesc spell, GameEvent event) {
		Player owner = event.getGameContext().getPlayer(ownerId);
		event.getGameContext().getLogic().secretTriggered(owner, this);
		boolean spellCasts = super.onFire(ownerId, spell, event);
		expire();
		return spellCasts;
	}

	@Override
	@Suspendable
	public void onGameEvent(GameEvent event) {
		if (event.getGameContext().getActivePlayerId() == getOwner()) {
			return;
		}
		super.onGameEvent(event);
	}

	@Override
	public String getName() {
		return getSourceCard().getName();
	}

	@Override
	public EntityType getEntityType() {
		return EntityType.SECRET;
	}

	@Override
	public Secret clone() {
		Secret clone = (Secret) super.clone();
		clone.source = source;
		return clone;
	}
}
