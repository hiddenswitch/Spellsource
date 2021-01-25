package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.EntityZone;
import net.demilich.metastone.game.spells.trigger.secrets.Secret;

public final class SecretsContainsFilter extends ZoneContainsFilter {

	public SecretsContainsFilter(EntityFilterDesc desc) {
		super(desc);
	}

	@Override
	protected EntityZone<Secret> getZone(Player player) {
		return player.getSecrets();
	}
}
