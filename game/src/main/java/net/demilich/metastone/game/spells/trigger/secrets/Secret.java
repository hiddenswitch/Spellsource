package net.demilich.metastone.game.spells.trigger.secrets;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import com.hiddenswitch.spellsource.client.models.EntityType;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.spells.trigger.EventTrigger;

public class Secret extends Enchantment {
	public Secret(EventTrigger trigger, SpellDesc spell, Card source) {
		super(trigger, spell);
		setSourceCard(source);
		setMaxFires(1);
		getAttributes().putAll(source.getAttributes());
	}

	public Secret(EnchantmentDesc desc, Card source) {
		this(desc.eventTrigger.create(), desc.spell, source);
		setCountUntilCast(desc.countUntilCast);
		if (desc.maxFires == null) {
			setMaxFires(1);
		} else {
			setMaxFires(desc.maxFires);
		}
		setKeepAfterTransform(desc.keepAfterTransform);
		setCountByValue(desc.countByValue);
		setPersistentOwner(desc.persistentOwner);
	}

	@Override
	@Suspendable
	protected boolean onFire(int ownerId, SpellDesc spell, GameEvent event) {
		boolean spellCasts = super.onFire(ownerId, spell, event);
		if (isInPlay() && spellCasts) {
			expire();
			Player owner = event.getGameContext().getPlayer(ownerId);
			event.getGameContext().getLogic().secretTriggered(owner, this);
		}
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
		clone.setSourceCard(getSourceCard());
		return clone;
	}
}
