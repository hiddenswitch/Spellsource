package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.Logic;
import com.hiddenswitch.proto3.net.models.EventLogicRequest;
import com.hiddenswitch.proto3.net.models.LogicResponse;
import com.hiddenswitch.proto3.net.util.Broker;
import com.hiddenswitch.proto3.net.util.ServiceProxy;
import io.vertx.core.Vertx;
import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.AfterSummonEvent;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.Map;

/**
 * Created by bberman on 2/19/17.
 */
public class AllianceSpell extends Spell {
	private ServiceProxy<Logic> logic;

	protected AllianceSpell() {
		super();
		logic = Broker.proxy(Logic.class, Vertx.currentContext().owner().eventBus());
	}

	public static SpellDesc create(String gameId) {
		// Put the gameId into name because it is a string
		Map<SpellArg, Object> arguments = SpellDesc.build(AllianceSpell.class);
		arguments.put(SpellArg.NAME, gameId);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		final String gameId = desc.getString(SpellArg.NAME);
		final String userId = (String) player.getAttributes().get(Attribute.USER_ID);

		GameEvent event = context.getCurrentEvent();
		if (event == null) {
			// Game state change
			return;
		}

		// For now, we only handle a few events
		switch (event.getEventType()) {
			case BEFORE_SUMMON:
				AfterSummonEvent event1 = (AfterSummonEvent) event;

				final EventLogicRequest request = new EventLogicRequest();
				final int entityId = event1.getMinion().getId();
				request.setGameId(context.getGameId());
				request.setUserId((String) player.getAttribute(Attribute.USER_ID));
				request.setEntityId(entityId);
				request.setCardInstanceId((String) event1.getSource().getAttribute(Attribute.CARD_INSTANCE_ID));

				LogicResponse response = logic.uncheckedSync().beforeSummon(request);
				if (response.getEntityIdsAffected().contains(entityId)) {
					// Perform the updates specified in the logic response
					event1.getMinion().modifyAttribute(Attribute.FIRST_TIME_PLAYS, +1);
				}
				break;
			default:
		}
	}
}
