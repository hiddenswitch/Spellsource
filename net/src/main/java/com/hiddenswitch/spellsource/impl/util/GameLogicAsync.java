package com.hiddenswitch.spellsource.impl.util;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.common.NetworkBehaviour;
import com.hiddenswitch.spellsource.common.NullResult;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.sync.Sync;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.BattlecryAction;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.WeaponCard;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.logic.SummonResult;
import net.demilich.metastone.game.targeting.TargetSelection;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static io.vertx.ext.sync.Sync.fiberHandler;

/**
 * Created by bberman on 11/23/16.
 */
public class GameLogicAsync extends GameLogic {
	private boolean mulliganEnabled = true;

	@Override
	@Suspendable
	protected void mulligan(Player player, boolean begins) throws UnsupportedOperationException {
		Object ignored = Sync.awaitFiber(r -> mulliganAsync(player, begins, r1 -> r.handle(Future.succeededFuture(r1))));
	}

	@Override
	@Suspendable
	protected void mulliganAsync(Player player, boolean begins, Handler<Object> callback) {
		FirstHand firstHand = new FirstHand(player, begins).invoke();

		NetworkBehaviour networkBehaviour = (NetworkBehaviour) player.getBehaviour();

		if (mulliganEnabled) {
			networkBehaviour.mulliganAsync(context, player, firstHand.getStarterCards(), (List<Card> discardedCards) -> {
				logger.debug("Discarded cards from {}: {}", player.getName(), discardedCards.stream().map(Card::toString).reduce((a, b) -> a + ", " + b));
				handleMulligan(player, begins, firstHand, discardedCards);
				callback.handle(discardedCards);
			});
		} else {
			handleMulligan(player, begins, firstHand, Collections.emptyList());
			callback.handle(Collections.emptyList());
		}
	}

	@Override
	@Suspendable
	public void init(int playerId, boolean begins) throws UnsupportedOperationException {
		Player ignored = Sync.awaitFiber(r -> initAsync(playerId, begins, r1 -> r.handle(Future.succeededFuture(r1))));
	}

	@Override
	@Suspendable
	public void initAsync(int playerId, boolean begins, Handler<Player> callback) {
		Player player = context.getPlayer(playerId);

		mulliganAsync(player, begins, fiberHandler(o -> {
			startGameForPlayer(player);
			callback.handle(player);
		}));
	}

	@Override
	@Suspendable
	protected void resolveBattlecry(int playerId, Actor actor) {
		logger.debug("AsyncDebug {} successfully called resolveBattlecry.", this.context);
		Boolean result = Sync.awaitFiber(new BattlecryResult(playerId, actor));
	}

	@Override
	@Suspendable
	protected void resolveBattlecryAsync(int playerId, Actor actor, Handler<AsyncResult<Boolean>> result) {
		BattlecryAction battlecry = actor.getBattlecry();

		Player player = context.getPlayer(playerId);
		if (!battlecry.canBeExecuted(context, player)) {
			if (result != null) {
				result.handle(NullResult.SUCESSS);
			}
			return;
		}

		battlecry.setSource(actor.getReference());

		if (battlecry.getTargetRequirement() != TargetSelection.NONE) {
			List<GameAction> battlecryActions = getTargetedBattlecryGameActions(battlecry, player);

			if (battlecryActions == null
					|| battlecryActions.size() == 0) {
				result.handle(NullResult.SUCESSS);
				return;
			}

			NetworkBehaviour networkBehaviour = (NetworkBehaviour) player.getBehaviour();
			networkBehaviour.requestActionAsync(context, player, battlecryActions, action -> {
				BattlecryAction battlecryAction = (BattlecryAction) action;
				performBattlecryAction(playerId, actor, player, battlecryAction);
				logger.debug("AsyncDebug {} successfully called resolveBattlecryAsync", this.context);
				if (result != null) {
					result.handle(NullResult.SUCESSS);
				}
			});
		} else {
			performBattlecryAction(playerId, actor, player, battlecry);
			if (result != null) {
				result.handle(NullResult.SUCESSS);
			}
		}
	}

	@Override
	@Suspendable
	public void equipWeapon(int playerId, Weapon weapon, WeaponCard weaponCard, boolean resolveBattlecry) {
		logger.debug("AsyncDebug {} successfully called equipWeapon.", this.context);
		if (!resolveBattlecry) {
			super.equipWeapon(playerId, weapon, weaponCard, false);
		} else {
			Boolean result = Sync.awaitFiber(new EquipWeaponResult(playerId, weapon, weaponCard, resolveBattlecry));
		}
	}

	@Override
	@Suspendable
	public void equipWeaponAsync(int playerId, Weapon weapon, WeaponCard weaponCard, Handler<AsyncResult<Boolean>> result, boolean resolveBattlecry) {
		logger.debug("AsyncDebug {} successfully called equipWeaponAsync.", this.context);
		PreEquipWeapon preEquipWeapon = new PreEquipWeapon(playerId, weapon).invoke();
		Weapon currentWeapon = preEquipWeapon.getCurrentWeapon();
		Player player = preEquipWeapon.getPlayer();

		if (resolveBattlecry
				&& weapon.getBattlecry() != null) {
			resolveBattlecryAsync(playerId, weapon, action -> {
				postEquipWeapon(playerId, weapon, currentWeapon, player, weaponCard);
				if (result != null) {
					result.handle(NullResult.SUCESSS);
				}
			});
		} else {
			postEquipWeapon(playerId, weapon, currentWeapon, player, weaponCard);
			if (result != null) {
				result.handle(NullResult.SUCESSS);
			}
		}
	}

	@Override
	@Suspendable
	public boolean summon(int playerId, Minion minion, Card source, int index, boolean resolveBattlecry) {
		if (!resolveBattlecry) {
			logger.debug("AsyncDebug {} successfully called regular summon.", this.context.toString());
			return super.summon(playerId, minion, source, index, false);
		}

		return Sync.awaitFiber(done -> summonAsync(playerId, minion, source, index, true, done));
	}

	@Override
	@Suspendable
	protected void summonAsync(int playerId, Minion minion, Card source, int index, boolean resolveBattlecry, Handler<AsyncResult<Boolean>> summoned) {
		PreSummon preSummon = new PreSummon(playerId, minion, index, source).invoke();
		if (preSummon.failed()) {
			if (summoned != null) {
				summoned.handle(SummonResult.NOT_SUMMONED);
			}
			return;
		}

		Player player = preSummon.getPlayer();

		Handler<AsyncResult<Boolean>> postSummonHandler = result -> {
			checkForDeadEntities();

			postSummon(minion, source, player, false);
			if (summoned != null) {
				summoned.handle(SummonResult.SUMMONED);
			}
		};

		if (resolveBattlecry && minion.getBattlecry() != null) {
			resolveBattlecryAsync(player.getId(), minion, fiberHandler((o) -> {
				postSummonHandler.handle(SummonResult.SUMMONED);
			}));
		} else {
			postSummonHandler.handle(SummonResult.SUMMONED);
		}
	}

	private class EquipWeaponResult implements Consumer<Handler<AsyncResult<Boolean>>> {
		private final int playerId;
		private final Weapon weapon;
		private final WeaponCard weaponCard;
		private final boolean resolveBattlecry;

		public EquipWeaponResult(int playerId, Weapon weapon, WeaponCard weaponCard, boolean resolveBattlecry) {
			this.playerId = playerId;
			this.weapon = weapon;
			this.weaponCard = weaponCard;
			this.resolveBattlecry = resolveBattlecry;
		}

		@Override
		@Suspendable
		public void accept(Handler<AsyncResult<Boolean>> done) {
			if (done == null) {
				logger.error("A handler was null!");
			}
			GameLogicAsync.this.equipWeaponAsync(playerId, weapon, weaponCard, done, resolveBattlecry);
		}
	}

	private class BattlecryResult implements Consumer<Handler<AsyncResult<Boolean>>> {
		private final int playerId;
		private final Actor actor;

		public BattlecryResult(int playerId, Actor actor) {
			this.playerId = playerId;
			this.actor = actor;
		}

		@Override
		@Suspendable
		public void accept(Handler<AsyncResult<Boolean>> done) {
			if (done == null) {
				logger.error("A handler was null!");
			}
			GameLogicAsync.this.resolveBattlecryAsync(playerId, actor, done);
		}
	}

	public boolean isMulliganEnabled() {
		return mulliganEnabled;
	}

	public void setMulliganEnabled(boolean mulliganEnabled) {
		this.mulliganEnabled = mulliganEnabled;
	}
}
