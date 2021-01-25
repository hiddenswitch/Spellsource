package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;

/**
 * Performs the effect in the {@link net.demilich.metastone.game.spells.desc.aura.AuraArg#APPLY_EFFECT} on cards that
 * were not selected in a {@link net.demilich.metastone.game.spells.SpellUtils#discoverCard(GameContext, Player, Entity,
 * SpellDesc, CardList)} effect.
 * <p>
 * Only applies to discovers that call {@link net.demilich.metastone.game.spells.ReceiveCardSpell} on the card that was
 * selected.
 */
public final class DiscoverNotSelectedSpellBonusAura extends AbstractFriendlyCardAura {

	public DiscoverNotSelectedSpellBonusAura(AuraDesc desc) {
		super(desc);
	}
}
