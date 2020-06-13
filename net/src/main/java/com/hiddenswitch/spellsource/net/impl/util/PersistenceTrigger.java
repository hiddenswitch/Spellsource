package com.hiddenswitch.spellsource.net.impl.util;

import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import co.paralleluniverse.strands.SuspendableAction1;
import com.hiddenswitch.spellsource.net.Spellsource;
import com.hiddenswitch.spellsource.net.Logic;
import com.hiddenswitch.spellsource.common.Tracing;
import com.hiddenswitch.spellsource.net.impl.GameId;
import com.hiddenswitch.spellsource.net.impl.RpcClient;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import io.vertx.core.Handler;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;;
import net.demilich.metastone.game.spells.trigger.Trigger;
import net.demilich.metastone.game.targeting.EntityReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * A trigger that records persistent {@link Attribute} to a database. Think of it as analytics for {@link Entity}
 * objects where some analytics events have side effects on gameplay.
 * <p>
 * To implement a new persistence effect, see {@link Spellsource#persistAttribute(String, EventTypeEnum, Attribute,
 * SuspendableAction1)}
 * <p>
 * In games with persistence effects enabled, the {@link PersistenceTrigger} is added to a list of "other triggers" that
 * are just always running throughout a game. In Spellsource, this trigger is added by a {@code
 * ServerGameContext#enablePersistenceEffects()} when it is created. This trigger should not be added to non-networked
 * game contexts, like a regular {@link GameContext}, because other code (like the AI) may use a game context for a
 * purpose other than hosting a two-player multiplayer networked match.
 * <p>
 * There are performance impacts of using a database for saving pieces of game state. In order to improve performance of
 * responding to game events, this implementation relies on an {@link Entity#hasPersistentEffects()} function to decide
 * whether it must wait for the consequences of persistent effects or not. When it does not have to wait for the
 * consequences of a persistence effect, this implementation makes a non-blocking call to the database, much like an
 * ordinary analytics method call would.
 * <p>
 * This class requires a reference to an {@link RpcClient} for {@link Logic}. It relies on the different ways RPC calls
 * can be made, like {@link RpcClient#sync()} versus {@link RpcClient#async(Handler)}.
 */
public class PersistenceTrigger implements Trigger, Serializable {
	static Logger logger = LoggerFactory.getLogger(PersistenceTrigger.class);
	/**
	 * The {@link RpcClient} for the {@link Logic} service.
	 */
	private transient final GameContext context;
	private final GameId gameId;

	public PersistenceTrigger(GameContext context, GameId gameId) {
		this.gameId = gameId;
		this.context = context;
	}

	@Override
	@Suspendable
	public void onGameEvent(GameEvent event) {
		if (isExpired()) {
			return;
		}

		try {
			Spellsource.spellsource().persistence().persistenceTrigger(event);
		} catch (Throwable throwable) {
			if (Strand.isCurrentFiber()) {
				Tracer tracer = GlobalTracer.get();
				if (tracer.activeSpan() != null) {
					Tracing.error(throwable, tracer.activeSpan(), false);
				}
			}
			logger.error("onGameEvent: Failed a persistence call and silently continuing. {}", throwable);
		}
	}

	@Override
	public Trigger clone() {
		return new PersistenceTrigger(context, gameId);
	}

	@Override
	public boolean queues(GameEvent event) {
		return true;
	}

	@Override
	public EntityReference getHostReference() {
		return EntityReference.NONE;
	}

	@Override
	public int getOwner() {
		// This listener is owned by the currently playing player
		return context.getActivePlayerId();
	}

	@Override
	public boolean interestedIn(com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum eventType) {
		return true;
	}

	@Override
	public boolean isExpired() {
		return false;
	}

	@Override
	@Suspendable
	public void onAdd(GameContext context, Player player, Entity source, Entity host) {
	}

	@Override
	public void setHostReference(EntityReference entityReference) {
	}

	@Override
	public void setOwner(int playerIndex) {
	}

	@Override
	public boolean isPersistentOwner() {
		return true;
	}

	@Override
	public boolean oneTurnOnly() {
		return false;
	}

	@Override
	public void expire(GameContext context) {
	}

	@Override
	@Suspendable
	public boolean fires(GameEvent event) {
		return true;
	}

	@Override
	public boolean isActivated() {
		return true;
	}
}
