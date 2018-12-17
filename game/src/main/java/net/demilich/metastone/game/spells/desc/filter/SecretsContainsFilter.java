package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityZone;
import net.demilich.metastone.game.spells.trigger.secrets.Secret;

public final class SecretsContainsFilter extends ZoneContainsFilter {

	private static final long serialVersionUID = 7120852704677447407L;

	public SecretsContainsFilter(EntityFilterDesc desc) {
		super(desc);
	}

	@Override
	protected EntityZone<Secret> getZone(Player player) {
		return player.getSecrets();
	}
}
