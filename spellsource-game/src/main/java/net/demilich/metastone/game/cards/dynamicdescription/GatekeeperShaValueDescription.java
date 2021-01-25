package net.demilich.metastone.game.cards.dynamicdescription;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.valueprovider.GatekeeperShaValueProvider;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProviderDesc;

/**
 * Renders a {@link PluralDescription} with a {@link GatekeeperShaValueProvider}.
 */
public class GatekeeperShaValueDescription extends PluralDescription {
	private static GatekeeperShaValueProvider INSTANCE = (GatekeeperShaValueProvider) new ValueProviderDesc(GatekeeperShaValueProvider.class).create();

	public GatekeeperShaValueDescription(DynamicDescriptionDesc desc) {
		super(desc);
	}

	@Override
	protected int getValue(GameContext context, Player player, Entity entity) {
		return INSTANCE.getValue(context, player, entity, entity) + getDesc().getInt(DynamicDescriptionArg.VALUE);
	}
}
