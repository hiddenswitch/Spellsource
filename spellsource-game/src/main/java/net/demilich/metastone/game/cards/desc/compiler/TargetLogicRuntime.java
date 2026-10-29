package net.demilich.metastone.game.cards.desc.compiler;

import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import com.hiddenswitch.spellsource.rpc.Spellsource.EntityTypeMessage.EntityType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.PhysicalAttackAction;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.environment.Environment;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.logic.TargetLogic;
import net.demilich.metastone.game.spells.aura.ModifyBuffSpellAura;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicOperation;
import net.demilich.metastone.game.spells.desc.valueprovider.AttributeValueProvider;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * O(1) {@link EntityReference} group lookup table woven into {@link TargetLogic#resolveTargetKey} by
 * {@link SpellsourceClassfileWeaver}.
 */
public final class TargetLogicRuntime {
	private TargetLogicRuntime() {
	}

	private static List<Entity> singleTargetAsList(Entity target) {
		if (target == null) {
			return new ArrayList<>();
		}
		ArrayList<Entity> list = new ArrayList<>(1);
		list.add(target);
		return list;
	}

	private static List<Entity> getEntities(TargetLogic logic, GameContext context, Player player, TargetSelection targetRequirement) {
		return getEntities(logic, context, player, targetRequirement, true);
	}

	public static <E extends Entity> List<E> withoutPermanents(List<E> in) {
		int size = in.size();
		ArrayList<E> out = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			E e = in.get(i);
			if (!e.hasAttribute(Attribute.PERMANENT)) {
				out.add(e);
			}
		}
		return out;
	}

	private static void addMinions(List<Entity> out, List<Minion> minions, boolean omitPermanents) {
		for (int i = 0, n = minions.size(); i < n; i++) {
			Minion m = minions.get(i);
			if (!omitPermanents || !m.hasAttribute(Attribute.PERMANENT)) {
				out.add(m);
			}
		}
	}

	public static List<Entity> getEntities(
			TargetLogic logic,
			GameContext context,
			Player player,
			TargetSelection targetRequirement,
			boolean omitPermanents) {
		Player opponent = context.getOpponent(player);
		ArrayList<Entity> entities = new ArrayList<>(16);
		if (targetRequirement == TargetSelection.ENEMY_HERO
				|| targetRequirement == TargetSelection.ENEMY_CHARACTERS
				|| targetRequirement == TargetSelection.ANY
				|| targetRequirement == TargetSelection.HEROES) {
			var hero = opponent.getHero();
			if (!omitPermanents || !hero.hasAttribute(Attribute.PERMANENT)) {
				entities.add(hero);
			}
		}
		if (targetRequirement == TargetSelection.ENEMY_MINIONS
				|| targetRequirement == TargetSelection.ENEMY_CHARACTERS
				|| targetRequirement == TargetSelection.MINIONS
				|| targetRequirement == TargetSelection.ANY
				|| targetRequirement == TargetSelection.FRIENDLY_HERO_AND_MINIONS) {
			addMinions(entities, opponent.getMinions(), omitPermanents);
		}
		if (targetRequirement == TargetSelection.FRIENDLY_HERO
				|| targetRequirement == TargetSelection.FRIENDLY_CHARACTERS
				|| targetRequirement == TargetSelection.ANY
				|| targetRequirement == TargetSelection.HEROES
				|| targetRequirement == TargetSelection.FRIENDLY_HERO_AND_MINIONS) {
			var hero = player.getHero();
			if (!omitPermanents || !hero.hasAttribute(Attribute.PERMANENT)) {
				entities.add(hero);
			}
		}
		if (targetRequirement == TargetSelection.FRIENDLY_MINIONS
				|| targetRequirement == TargetSelection.FRIENDLY_CHARACTERS
				|| targetRequirement == TargetSelection.MINIONS
				|| targetRequirement == TargetSelection.ANY
				|| targetRequirement == TargetSelection.FRIENDLY_HERO_AND_MINIONS) {
			addMinions(entities, player.getMinions(), omitPermanents);
		}
		return entities;
	}

	@FunctionalInterface
	private interface TargetGroupResolver {
		List<Entity> resolve(TargetLogic logic, GameContext context, Player player, Entity source);
	}

	private static final TargetGroupResolver[] TARGET_GROUP_RESOLVERS = new TargetGroupResolver[128];

	private static void register(EntityReference ref, TargetGroupResolver resolver) {
		int idx = -ref.getId();
		if (idx <= 1 || idx >= TARGET_GROUP_RESOLVERS.length) {
			throw new IllegalArgumentException("Invalid group EntityReference id for " + ref + ": " + ref.getId());
		}
		if (TARGET_GROUP_RESOLVERS[idx] != null) {
			throw new IllegalStateException("Duplicate group EntityReference id: " + ref.getId());
		}
		TARGET_GROUP_RESOLVERS[idx] = resolver;
	}

	static {
		register(EntityReference.ALL_CHARACTERS, (logic, context, player, source) ->
				getEntities(logic, context, player, TargetSelection.ANY));
		register(EntityReference.ALL_MINIONS, (logic, context, player, source) ->
				getEntities(logic, context, player, TargetSelection.MINIONS));
		register(EntityReference.ENEMY_CHARACTERS, (logic, context, player, source) ->
				getEntities(logic, context, player, TargetSelection.ENEMY_CHARACTERS));
		register(EntityReference.ENEMY_HERO, (logic, context, player, source) ->
				getEntities(logic, context, player, TargetSelection.ENEMY_HERO));
		register(EntityReference.ENEMY_MINIONS, (logic, context, player, source) ->
				getEntities(logic, context, player, TargetSelection.ENEMY_MINIONS));
		register(EntityReference.ENEMY_MINIONS_LEFT_TO_RIGHT, (logic, context, player, source) -> {
			List<Entity> enemyMinions = getEntities(logic, context, player, TargetSelection.ENEMY_MINIONS);
			enemyMinions.sort(Comparator.comparingInt(e -> e.getEntityLocation().getIndex()));
			return enemyMinions;
		});
		register(EntityReference.FRIENDLY_MINIONS_LEFT_TO_RIGHT, (logic, context, player, source) -> {
			List<Entity> friendlyMinions = getEntities(logic, context, player, TargetSelection.FRIENDLY_MINIONS);
			friendlyMinions.sort(Comparator.comparingInt(e -> e.getEntityLocation().getIndex()));
			return friendlyMinions;
		});
		register(EntityReference.FRIENDLY_CHARACTERS, (logic, context, player, source) ->
				getEntities(logic, context, player, TargetSelection.FRIENDLY_CHARACTERS));
		register(EntityReference.FRIENDLY_HERO, (logic, context, player, source) ->
				getEntities(logic, context, player, TargetSelection.FRIENDLY_HERO));
		register(EntityReference.FRIENDLY_MINIONS, (logic, context, player, source) ->
				getEntities(logic, context, player, TargetSelection.FRIENDLY_MINIONS));
		register(EntityReference.OTHER_FRIENDLY_MINIONS, (logic, context, player, source) -> {
			List<Entity> targets = getEntities(logic, context, player, TargetSelection.FRIENDLY_MINIONS);
			targets.remove(source);
			return targets;
		});
		register(EntityReference.OTHER_ENEMY_MINIONS, (logic, context, player, source) -> {
			List<Entity> targets = getEntities(logic, context, player, TargetSelection.ENEMY_MINIONS);
			targets.remove(source);
			return targets;
		});
		register(EntityReference.LEFTMOST_FRIENDLY_CARD_HAND, (logic, context, player, source) -> {
			if (player.getHand().size() == 0) {
				return new ArrayList<>();
			}
			return new ArrayList<>(player.getHand().subList(0, 1));
		});
		register(EntityReference.LEFTMOST_ENEMY_CARD_HAND, (logic, context, player, source) -> {
			Player opponent = context.getOpponent(player);
			if (opponent.getHand().size() == 0) {
				return new ArrayList<>();
			}
			return new ArrayList<>(opponent.getHand().subList(0, 1));
		});
		register(EntityReference.FRIENDLY_LAST_SPELL_PLAYED_THIS_TURN, (logic, context, player, source) -> {
			EntityReference value = context.getLastSpellPlayedThisTurnMap().getOrDefault(player.getId(), null);
			if (value == null) {
				return new ArrayList<>();
			}
			return singleTargetAsList(context.resolveSingleTarget(value));
		});
		register(EntityReference.RIGHTMOST_FRIENDLY_CARD_HAND, (logic, context, player, source) -> {
			if (player.getHand().size() == 0) {
				return new ArrayList<>();
			}
			ArrayList<Entity> entities = new ArrayList<>(1);
			entities.add(player.getHand().get(player.getHand().size() - 1));
			return entities;
		});
		register(EntityReference.ALL_OTHER_CHARACTERS, (logic, context, player, source) -> {
			List<Entity> targets = getEntities(logic, context, player, TargetSelection.ANY);
			targets.remove(source);
			return targets;
		});
		register(EntityReference.ALL_OTHER_MINIONS, (logic, context, player, source) -> {
			List<Entity> targets = getEntities(logic, context, player, TargetSelection.MINIONS);
			targets.remove(source);
			return targets;
		});
		register(EntityReference.ADJACENT_MINIONS, (logic, context, player, source) ->
				new ArrayList<>(context.getAdjacentMinions(source.getReference())));
		register(EntityReference.ADJACENT_TO_TARGET, (logic, context, player, source) -> {
			EntityReference targetKey1 = (EntityReference) context.getEnvironment().get(Environment.TARGET);
			if (targetKey1 == null) {
				return new ArrayList<>();
			}
			return new ArrayList<>(context.getAdjacentMinions(targetKey1));
		});
		register(EntityReference.ATTACKER_ADJACENT_MINIONS, (logic, context, player, source) ->
				new ArrayList<>(context.getAdjacentMinions(context.resolveSingleTarget(context.getAttackerReferenceStack().peek()).getReference())));
		register(EntityReference.OPPOSITE_MINIONS, (logic, context, player, source) ->
				new ArrayList<>(context.getOppositeMinions(source.getReference())));
		register(EntityReference.OPPOSITE_CHARACTERS, (logic, context, player, source) -> {
			List<Actor> oppositeMinions = context.getOppositeMinions(source.getReference());
			if (oppositeMinions.isEmpty()) {
				return singleTargetAsList(context.getOpponent(player).getHero());
			}
			return new ArrayList<>(oppositeMinions);
		});
		register(EntityReference.MINIONS_TO_LEFT, (logic, context, player, source) ->
				new ArrayList<>(context.getLeftMinions(source.getReference())));
		register(EntityReference.MINIONS_TO_RIGHT, (logic, context, player, source) ->
				new ArrayList<>(context.getRightMinions(source.getReference())));
		register(EntityReference.LEFTMOST_ENEMY_MINION, (logic, context, player, source) -> {
			List<Entity> minions = getEntities(logic, context, player, TargetSelection.ENEMY_MINIONS);
			if (minions.size() == 0) {
				return new ArrayList<>();
			}
			return singleTargetAsList(minions.get(0));
		});
		register(EntityReference.LEFTMOST_FRIENDLY_MINION, (logic, context, player, source) -> {
			List<Entity> minions = getEntities(logic, context, player, TargetSelection.FRIENDLY_MINIONS);
			if (minions.size() == 0) {
				return new ArrayList<>();
			}
			return singleTargetAsList(minions.get(0));
		});
		register(EntityReference.RIGHTMOST_ENEMY_MINION, (logic, context, player, source) -> {
			List<Entity> minions = getEntities(logic, context, player, TargetSelection.ENEMY_MINIONS);
			if (minions.size() == 0) {
				return new ArrayList<>();
			}
			return singleTargetAsList(minions.get(minions.size() - 1));
		});
		register(EntityReference.RIGHTMOST_FRIENDLY_MINION, (logic, context, player, source) -> {
			List<Entity> minions = getEntities(logic, context, player, TargetSelection.FRIENDLY_MINIONS);
			if (minions.size() == 0) {
				return new ArrayList<>();
			}
			return singleTargetAsList(minions.get(minions.size() - 1));
		});
		register(EntityReference.SELF, (logic, context, player, source) ->
				singleTargetAsList(source));
		register(EntityReference.EVENT_TARGET, (logic, context, player, source) -> {
			EntityReference target = context.getEventTargetStack().peek();
			if (target == null || target.equals(EntityReference.NONE)) {
				return new ArrayList<>();
			}
			return singleTargetAsList(context.resolveSingleTarget(target, false));
		});
		register(EntityReference.EVENT_SOURCE, (logic, context, player, source) -> {
			EntityReference target = context.getEventSourceStack().peek();
			if (target == null || target.equals(EntityReference.NONE)) {
				return new ArrayList<>();
			}
			return singleTargetAsList(context.resolveSingleTarget(target, false));
		});
		register(EntityReference.TARGET, (logic, context, player, source) -> {
			EntityReference targetKey1 = (EntityReference) context.getEnvironment().get(Environment.TARGET);
			if (targetKey1 == null) {
				return new ArrayList<>();
			}
			return singleTargetAsList(context.resolveSingleTarget(targetKey1));
		});
		register(EntityReference.SPELL_TARGET, (logic, context, player, source) ->
				singleTargetAsList(context.resolveSingleTarget(context.getSpellTargetStack().peek())));
		register(EntityReference.KILLED_MINION, (logic, context, player, source) ->
				singleTargetAsList(context.resolveSingleTarget((EntityReference) context.getEnvironment().get(Environment.KILLED_MINION))));
		register(EntityReference.ATTACKER, (logic, context, player, source) ->
				singleTargetAsList(context.resolveSingleTarget(context.getAttackerReferenceStack().peek())));
		register(EntityReference.OUTPUT, (logic, context, player, source) ->
				singleTargetAsList(context.resolveSingleTarget(context.getOutputStack().peek())));
		register(EntityReference.FRIENDLY_WEAPON, (logic, context, player, source) -> {
			if (!player.getWeaponZone().isEmpty()) {
				return singleTargetAsList(player.getWeaponZone().get(0));
			}
			return new ArrayList<>();
		});
		register(EntityReference.ENEMY_WEAPON, (logic, context, player, source) -> {
			Player opponent = context.getOpponent(player);
			if (!opponent.getWeaponZone().isEmpty()) {
				return singleTargetAsList(opponent.getWeaponZone().get(0));
			}
			return new ArrayList<>();
		});
		register(EntityReference.FRIENDLY_HAND, (logic, context, player, source) ->
				new ArrayList<>(player.getHand().toList()));
		register(EntityReference.ENEMY_HAND, (logic, context, player, source) ->
				new ArrayList<>(context.getOpponent(player).getHand().toList()));
		register(EntityReference.FRIENDLY_PLAYER, (logic, context, player, source) ->
				singleTargetAsList(player));
		register(EntityReference.ENEMY_PLAYER, (logic, context, player, source) ->
				singleTargetAsList(context.getOpponent(player)));
		register(EntityReference.FRIENDLY_DECK, (logic, context, player, source) ->
				new ArrayList<>(player.getDeck().toList()));
		register(EntityReference.FRIENDLY_DECK_FROM_TOP, (logic, context, player, source) -> {
			ArrayList<Entity> deck = new ArrayList<>(player.getDeck().toList());
			Collections.reverse(deck);
			return deck;
		});
		register(EntityReference.ENEMY_DECK, (logic, context, player, source) ->
				new ArrayList<>(context.getOpponent(player).getDeck().toList()));
		register(EntityReference.FRIENDLY_TOP_CARD, (logic, context, player, source) -> {
			if (player.getDeck().size() == 0) {
				return new ArrayList<>();
			}
			return singleTargetAsList(context.getLogic().getRandom().draw(player.getDeck()));
		});
		register(EntityReference.ENEMY_TOP_CARD, (logic, context, player, source) -> {
			Player opponent = context.getOpponent(player);
			if (opponent.getDeck().size() == 0) {
				return new ArrayList<>();
			}
			return singleTargetAsList(context.getLogic().getRandom().draw(opponent.getDeck()));
		});
		register(EntityReference.BOTH_DECKS, (logic, context, player, source) -> {
			ArrayList<Entity> friendly = new ArrayList<>(player.getDeck().toList());
			friendly.addAll(context.getOpponent(player).getDeck().toList());
			return friendly;
		});
		register(EntityReference.BOTH_HANDS, (logic, context, player, source) -> {
			ArrayList<Entity> friendly = new ArrayList<>(player.getHand().toList());
			friendly.addAll(context.getOpponent(player).getHand().toList());
			return friendly;
		});
		register(EntityReference.LAST_CARD_PLAYED, (logic, context, player, source) ->
				singleTargetAsList(context.resolveSingleTarget(context.getLastCardPlayed())));
		register(EntityReference.FRIENDLY_LAST_CARD_PLAYED, (logic, context, player, source) ->
				singleTargetAsList(context.resolveSingleTarget(context.getLastCardPlayed(player.getId()))));
		register(EntityReference.ENEMY_LAST_CARD_PLAYED, (logic, context, player, source) ->
				singleTargetAsList(context.resolveSingleTarget(context.getLastCardPlayed(context.getOpponent(player).getId()))));
		register(EntityReference.LAST_CARD_PLAYED_BEFORE_CURRENT_SEQUENCE, (logic, context, player, source) ->
				singleTargetAsList(context.resolveSingleTarget(context.getLastCardPlayedBeforeCurrentSequence())));
		register(EntityReference.FRIENDLY_LAST_CARD_PLAYED_BEFORE_CURRENT_SEQUENCE, (logic, context, player, source) ->
				singleTargetAsList(context.resolveSingleTarget(context.getLastCardPlayedBeforeCurrentSequence(player.getId()))));
		register(EntityReference.ENEMY_LAST_CARD_PLAYED_BEFORE_CURRENT_SEQUENCE, (logic, context, player, source) ->
				singleTargetAsList(context.resolveSingleTarget(context.getLastCardPlayedBeforeCurrentSequence(context.getOpponent(player).getId()))));
		//noinspection deprecation
		register(EntityReference.TRANSFORM_REFERENCE, (logic, context, player, source) ->
				singleTargetAsList(context.resolveSingleTarget((EntityReference) context.getEnvironment().get(Environment.TRANSFORM_REFERENCE))));
		register(EntityReference.FRIENDLY_SET_ASIDE, (logic, context, player, source) ->
				new ArrayList<>(player.getSetAsideZone()));
		register(EntityReference.ENEMY_SET_ASIDE, (logic, context, player, source) ->
				new ArrayList<>(context.getOpponent(player).getSetAsideZone()));
		register(EntityReference.FRIENDLY_GRAVEYARD, (logic, context, player, source) ->
				new ArrayList<>(player.getGraveyard()));
		register(EntityReference.ENEMY_GRAVEYARD, (logic, context, player, source) ->
				new ArrayList<>(context.getOpponent(player).getGraveyard()));
		register(EntityReference.FRIENDLY_HERO_POWER, (logic, context, player, source) ->
				singleTargetAsList(player.getHeroPowerZone().get(0)));
		register(EntityReference.ENEMY_HERO_POWER, (logic, context, player, source) ->
				singleTargetAsList(context.getOpponent(player).getHeroPowerZone().get(0)));
		register(EntityReference.ALL_ENTITIES, (logic, context, player, source) ->
				context.getEntities().collect(Collectors.toList()));
		register(EntityReference.TRIGGER_HOST, (logic, context, player, source) ->
				singleTargetAsList(context.resolveSingleTarget(context.getTriggerHostStack().peek())));
		register(EntityReference.PHYSICAL_ATTACK_TARGETS, (logic, context, player, source) ->
				logic.getValidTargets(context, player, new PhysicalAttackAction(source.getReference())));
		register(EntityReference.LEFT_ADJACENT_MINION, (logic, context, player, source) -> {
			List<Actor> leftMinions = context.getLeftMinions(source.getReference());
			if (leftMinions.isEmpty()) {
				return new ArrayList<>();
			}
			return singleTargetAsList(leftMinions.get(leftMinions.size() - 1));
		});
		register(EntityReference.RIGHT_ADJACENT_MINION, (logic, context, player, source) -> {
			List<Actor> rightMinions = context.getRightMinions(source.getReference());
			if (rightMinions.isEmpty()) {
				return new ArrayList<>();
			}
			return singleTargetAsList(rightMinions.get(0));
		});
		register(EntityReference.FRIENDLY_LAST_DIED_MINION, (logic, context, player, source) -> {
			for (int i = player.getGraveyard().size() - 1; i >= 0; i--) {
				if (player.getGraveyard().get(i).getEntityType() == EntityType.MINION) {
					return singleTargetAsList(player.getGraveyard().get(i));
				}
			}
			return new ArrayList<>();
		});
		register(EntityReference.FRIENDLY_CARDS, (logic, context, player, source) -> {
			List<Entity> friendlyCards = new ArrayList<>();
			friendlyCards.addAll(player.getHand());
			friendlyCards.addAll(player.getDeck());
			friendlyCards.addAll(player.getMinions());
			friendlyCards.addAll(player.getWeaponZone());
			friendlyCards.addAll(player.getSetAsideZone());
			friendlyCards.addAll(player.getHeroPowerZone());
			friendlyCards.add(player.getHero());
			for (Entity e : player.getGraveyard()) {
				if (e instanceof Card && e.hasAttribute(Attribute.BEING_PLAYED)) {
					friendlyCards.add(e);
				}
			}
			return friendlyCards;
		});
		register(EntityReference.ENEMY_CARDS, (logic, context, player, source) -> {
			Player opponent = context.getOpponent(player);
			List<Entity> enemyCards = new ArrayList<>();
			enemyCards.addAll(opponent.getHand());
			enemyCards.addAll(opponent.getDeck());
			enemyCards.addAll(opponent.getMinions());
			enemyCards.addAll(opponent.getWeaponZone());
			enemyCards.addAll(opponent.getSetAsideZone());
			enemyCards.addAll(opponent.getHeroPowerZone());
			enemyCards.add(opponent.getHero());
			for (Entity e : opponent.getGraveyard()) {
				if (e instanceof Card && e.hasAttribute(Attribute.BEING_PLAYED)) {
					enemyCards.add(e);
				}
			}
			return enemyCards;
		});
		register(EntityReference.CURRENT_SUMMONING_MINION, (logic, context, player, source) -> {
			if (context.getSummonReferenceStack().isEmpty()) {
				return new ArrayList<>();
			}
			return singleTargetAsList(logic.findEntity(context, context.getSummonReferenceStack().peekLast()));
		});
		register(EntityReference.ENEMY_MIDDLE_MINIONS, (logic, context, player, source) -> {
			Player opponent = context.getOpponent(player);
			List<Entity> matching = new ArrayList<>();
			if (opponent.getMinions().size() == 0) {
				return matching;
			}
			int skip = (opponent.getMinions().size() - 1) / 2;
			for (int i = skip; i < opponent.getMinions().size() - skip; i++) {
				matching.add(opponent.getMinions().get(i));
			}
			return matching;
		});
		register(EntityReference.FRIENDLY_LAST_MINION_PLAYED, (logic, context, player, source) -> {
			for (int i = player.getGraveyard().size() - 1; i >= 0; i--) {
				Entity e = player.getGraveyard().get(i);
				if (e.getEntityType() == EntityType.CARD
						&& e.getSourceCard().getCardType() == CardType.MINION
						&& e.hasAttribute(Attribute.PLAYED_FROM_HAND_OR_DECK)) {
					return singleTargetAsList(e);
				}
			}
			return new ArrayList<>();
		});
		register(EntityReference.OTHER_FRIENDLY_CHARACTERS, (logic, context, player, source) -> {
			List<Entity> targets = getEntities(logic, context, player, TargetSelection.FRIENDLY_CHARACTERS);
			targets.remove(source);
			return targets;
		});
		register(EntityReference.FRIENDLY_SIGNATURE, (logic, context, player, source) -> {
			if (player.getAttribute(Attribute.SIGNATURE) instanceof String sig) {
				return singleTargetAsList(context.getCardById(sig));
			}
			return singleTargetAsList(context.getCardById(GameLogic.DEFAULT_SIGNATURE));
		});
		register(EntityReference.ENEMY_SIGNATURE, (logic, context, player, source) -> {
			if (context.getOpponent(player).getAttribute(Attribute.SIGNATURE) instanceof String) {
				return singleTargetAsList(context.getCardById((String) player.getAttribute(Attribute.SIGNATURE)));
			}
			return singleTargetAsList(context.getCardById(GameLogic.DEFAULT_SIGNATURE));
		});
		register(EntityReference.FRIENDLY_SECRETS, (logic, context, player, source) ->
				new ArrayList<>(player.getSecrets()));
		register(EntityReference.ENEMY_PERMANENTS, (logic, context, player, source) -> {
			List<Entity> permanents = new ArrayList<>();
			for (Minion m : context.getOpponent(player).getMinions()) {
				if (m.hasAttribute(Attribute.PERMANENT)) {
					permanents.add(m);
				}
			}
			return permanents;
		});
		register(EntityReference.FRIENDLY_PERMANENTS, (logic, context, player, source) -> {
			List<Entity> permanents = new ArrayList<>();
			for (Minion m : player.getMinions()) {
				if (m.hasAttribute(Attribute.PERMANENT)) {
					permanents.add(m);
				}
			}
			return permanents;
		});
	}

	public static List<Entity> resolveTargetKey(TargetLogic logic, GameContext context, Player player, Entity source, EntityReference targetKey) {
		if (targetKey == null) {
			return null;
		}
		int id = targetKey.getId();
		if (id == -1) {
			return null;
		}
		if (id >= 0) {
			return singleTargetAsList(logic.findEntity(context, targetKey));
		}
		int idx = -id;
		if (idx < TARGET_GROUP_RESOLVERS.length) {
			TargetGroupResolver resolver = TARGET_GROUP_RESOLVERS[idx];
			if (resolver != null) {
				return resolver.resolve(logic, context, player, source);
			}
		}
		throw new NullPointerException("invalid spec");
	}

	private static int countMinionZone(
			GameContext context,
			Player player,
			Entity host,
			List<Minion> minions,
			Entity excluded,
			CompiledEntityFilter filter) {
		int count = 0;
		for (int i = 0, n = minions.size(); i < n; i++) {
			Minion m = minions.get(i);
			if (m == excluded || m.hasAttribute(Attribute.PERMANENT)) {
				continue;
			}
			Entity resolved = m.transformResolved(context);
			if (filter == null || filter.matches(context, player, resolved, host)) {
				count++;
			}
		}
		return count;
	}

	private static int countZoneList(
			GameContext context,
			Player player,
			Entity host,
			List<? extends Entity> zone,
			CompiledEntityFilter filter) {
		int count = 0;
		for (int i = 0, n = zone.size(); i < n; i++) {
			Entity resolved = zone.get(i).transformResolved(context);
			if (filter == null || filter.matches(context, player, resolved, host)) {
				count++;
			}
		}
		return count;
	}

	public static int countEntities(
			GameContext context,
			Player player,
			Entity target,
			Entity originalHost,
			EntityReference descTarget,
			CompiledEntityFilter filter) {
		Entity host = originalHost;
		if (originalHost == null && descTarget != null && !descTarget.isTargetGroup()) {
			host = context.resolveSingleTarget(descTarget);
		}
		EntityReference ref = descTarget != null ? descTarget : (target != null ? target.getReference() : null);
		if (ref == null || ref.equals(EntityReference.NONE)) {
			return 0;
		}
		if (ref == EntityReference.FRIENDLY_MINIONS) {
			return countMinionZone(context, player, host, player.getMinions(), null, filter);
		}
		if (ref == EntityReference.ENEMY_MINIONS) {
			return countMinionZone(context, player, host, context.getOpponent(player).getMinions(), null, filter);
		}
		if (ref == EntityReference.OTHER_FRIENDLY_MINIONS) {
			return countMinionZone(context, player, host, player.getMinions(), originalHost, filter);
		}
		if (ref == EntityReference.OTHER_ENEMY_MINIONS) {
			return countMinionZone(context, player, host, context.getOpponent(player).getMinions(), originalHost, filter);
		}
		if (ref == EntityReference.ALL_MINIONS) {
			return countMinionZone(context, player, host, context.getOpponent(player).getMinions(), null, filter)
					+ countMinionZone(context, player, host, player.getMinions(), null, filter);
		}
		if (ref == EntityReference.ALL_OTHER_MINIONS) {
			return countMinionZone(context, player, host, context.getOpponent(player).getMinions(), originalHost, filter)
					+ countMinionZone(context, player, host, player.getMinions(), originalHost, filter);
		}
		if (ref == EntityReference.FRIENDLY_HAND) {
			return filter == null ? player.getHand().size() : countZoneList(context, player, host, player.getHand(), filter);
		}
		if (ref == EntityReference.ENEMY_HAND) {
			Player opp = context.getOpponent(player);
			return filter == null ? opp.getHand().size() : countZoneList(context, player, host, opp.getHand(), filter);
		}
		if (ref == EntityReference.BOTH_HANDS) {
			Player opp = context.getOpponent(player);
			return filter == null
					? player.getHand().size() + opp.getHand().size()
					: countZoneList(context, player, host, player.getHand(), filter) + countZoneList(context, player, host, opp.getHand(), filter);
		}
		if (ref == EntityReference.FRIENDLY_DECK) {
			return filter == null ? player.getDeck().size() : countZoneList(context, player, host, player.getDeck(), filter);
		}
		if (ref == EntityReference.ENEMY_DECK) {
			Player opp = context.getOpponent(player);
			return filter == null ? opp.getDeck().size() : countZoneList(context, player, host, opp.getDeck(), filter);
		}
		if (ref == EntityReference.FRIENDLY_GRAVEYARD) {
			return filter == null ? player.getGraveyard().size() : countZoneList(context, player, host, player.getGraveyard(), filter);
		}
		if (ref == EntityReference.ENEMY_GRAVEYARD) {
			Player opp = context.getOpponent(player);
			return filter == null ? opp.getGraveyard().size() : countZoneList(context, player, host, opp.getGraveyard(), filter);
		}
		List<Entity> relevantEntities = context.resolveTarget(player, originalHost, ref);
		if (relevantEntities == null) {
			return 0;
		}
		if (filter == null) {
			return relevantEntities.size();
		}
		int count = 0;
		for (int i = 0, size = relevantEntities.size(); i < size; i++) {
			if (filter.matches(context, player, relevantEntities.get(i), host)) {
				count++;
			}
		}
		return count;
	}

	public static int countMinions(
			GameContext context,
			Player player,
			Entity source,
			CompiledEntityFilter filter) {
		List<Minion> minions = player.getMinions();
		int count = 0;
		for (int i = 0, n = minions.size(); i < n; i++) {
			Minion m = minions.get(i);
			if (!m.hasAttribute(Attribute.PERMANENT) && (filter == null || filter.matches(context, player, m, source))) {
				count++;
			}
		}
		return count;
	}

	public static int countGraveyard(
			GameContext context,
			Player player,
			Entity source,
			CompiledEntityFilter filter) {
		var graveyard = player.getGraveyard();
		int count = 0;
		for (int i = 0, n = graveyard.size(); i < n; i++) {
			Entity deadEntity = graveyard.get(i);
			if (deadEntity.diedOnBattlefield() && (filter == null || filter.matches(context, player, deadEntity, source))) {
				count++;
			}
		}
		return count;
	}

	public static int provideValueForAttack(GameContext context, Entity entity) {
		if (entity instanceof Actor source) {
			int value = 0;
			if (source.getEntityType() == EntityType.HERO) {
				Player player = context.getPlayer(source.getOwner());
				if (!player.getWeaponZone().isEmpty() && player.getWeaponZone().get(0).isActive()) {
					value += Math.max(0, player.getWeaponZone().get(0).getAttack());
				}
			}
			value += Math.max(0, source.getAttack());
			return value;
		}
		if (entity instanceof Card card) {
			if (card.getCardType() == CardType.MINION) {
				return card.getDesc().getBaseAttack() + card.getBonusAttack();
			}
			return card.getAttributeValue(Attribute.ATTACK);
		}
		return entity.getAttributeValue(Attribute.ATTACK);
	}

	public static int provideValueForHp(Entity entity) {
		if (entity instanceof Actor source) {
			return source.getHp();
		}
		if (entity instanceof Card card) {
			if (card.getCardType() == CardType.MINION) {
				return card.getDesc().getBaseHp() + card.getBonusHp();
			}
			return card.getAttributeValue(Attribute.HP);
		}
		return entity.getAttributeValue(Attribute.HP);
	}

	public static int evalSelfAttributeAttack(GameContext context, Entity host) {
		if (host == null) {
			return 0;
		}
		return provideValueForAttack(context, host.transformResolved(context));
	}

	public static int sumAttributeOnTarget(
			GameContext context,
			Player player,
			Entity host,
			EntityReference target,
			Attribute attribute,
			EntityFilter filter) {
		if (target == EntityReference.SELF) {
			if (host == null) {
				return 0;
			}
			Entity resolved = host.transformResolved(context);
			if (filter != null && !filter.matches(context, player, resolved, host)) {
				return 0;
			}
			if (attribute == Attribute.ATTACK) {
				return provideValueForAttack(context, resolved);
			}
			if (attribute == Attribute.HP) {
				return provideValueForHp(resolved);
			}
			return AttributeValueProvider.provideValueForAttribute(context, attribute, resolved);
		}
		if (target == EntityReference.FRIENDLY_MINIONS || target == EntityReference.ENEMY_MINIONS) {
			List<Minion> minions = (target == EntityReference.FRIENDLY_MINIONS ? player : context.getOpponent(player)).getMinions();
			int value = 0;
			for (int i = 0, n = minions.size(); i < n; i++) {
				Minion m = minions.get(i);
				if (m.hasAttribute(Attribute.PERMANENT)) {
					continue;
				}
				Entity resolved = m.transformResolved(context);
				if (filter == null || filter.matches(context, player, resolved, host)) {
					if (attribute == Attribute.ATTACK) {
						value += provideValueForAttack(context, resolved);
					} else if (attribute == Attribute.HP) {
						value += provideValueForHp(resolved);
					} else {
						value += AttributeValueProvider.provideValueForAttribute(context, attribute, resolved);
					}
				}
			}
			return value;
		}
		List<Entity> entities = context.resolveTarget(player, host, target);
		if (entities == null) {
			return 0;
		}
		int value = 0;
		for (int i = 0, size = entities.size(); i < size; i++) {
			Entity entity = entities.get(i);
			if (filter == null || filter.matches(context, player, entity, host)) {
				if (attribute == Attribute.ATTACK) {
					value += provideValueForAttack(context, entity);
				} else if (attribute == Attribute.HP) {
					value += provideValueForHp(entity);
				} else {
					value += AttributeValueProvider.provideValueForAttribute(context, attribute, entity);
				}
			}
		}
		return value;
	}

	public static int evalReduceVP(
			GameContext context,
			Player player,
			Entity targetEntity,
			Entity host,
			EntityReference target,
			Attribute attribute,
			AlgebraicOperation operation,
			CompiledEntityFilter filter,
			CompiledValueProvider value1Vp,
			int constValue1,
			boolean hasValue1,
			boolean hasValue2,
			int constValue2,
			CompiledValueProvider value2Vp) {
		int value;
		if (operation == AlgebraicOperation.MAXIMUM) {
			value = Integer.MIN_VALUE;
		} else if (operation == AlgebraicOperation.MINIMUM) {
			value = Integer.MAX_VALUE;
		} else if (operation == AlgebraicOperation.MULTIPLY) {
			value = 1;
		} else if (operation == AlgebraicOperation.DIVIDE
				|| operation == AlgebraicOperation.MODULO
				|| operation == AlgebraicOperation.NEGATE) {
			throw new UnsupportedOperationException("Cannot do a reduce with a DIVIDE, MODULO or NEGATE operator.");
		} else {
			value = 0;
		}

		boolean matchedAny = false;
		if (target == EntityReference.FRIENDLY_MINIONS || target == EntityReference.ENEMY_MINIONS) {
			List<Minion> minions = (target == EntityReference.FRIENDLY_MINIONS ? player : context.getOpponent(player)).getMinions();
			for (int i = 0, n = minions.size(); i < n; i++) {
				Minion m = minions.get(i);
				if (m.hasAttribute(Attribute.PERMANENT)) {
					continue;
				}
				Entity entity = m.transformResolved(context);
				if (filter != null && !filter.matches(context, player, entity, host)) {
					continue;
				}
				matchedAny = true;
				int itemVal;
				if (hasValue1) {
					itemVal = value1Vp != null ? value1Vp.compute(context, player, entity, targetEntity) : constValue1;
				} else if (attribute != null) {
					itemVal = attribute == Attribute.ATTACK
							? provideValueForAttack(context, entity)
							: (attribute == Attribute.HP ? provideValueForHp(entity) : AttributeValueProvider.provideValueForAttribute(context, attribute, entity));
				} else {
					continue;
				}
				value = operation == AlgebraicOperation.ADD ? (value + itemVal) : operation.performOperation(value, itemVal);
			}
		} else {
			List<Entity> entities = target != null
					? context.resolveTarget(player, host, target)
					: Collections.singletonList(targetEntity);
			if (entities == null) {
				return 0;
			}
			for (int i = 0, n = entities.size(); i < n; i++) {
				Entity entity = entities.get(i);
				if (filter != null && !filter.matches(context, player, entity, host)) {
					continue;
				}
				matchedAny = true;
				int itemVal;
				if (hasValue1) {
					itemVal = value1Vp != null ? value1Vp.compute(context, player, entity, targetEntity) : constValue1;
				} else if (attribute != null) {
					itemVal = attribute == Attribute.ATTACK
							? provideValueForAttack(context, entity)
							: (attribute == Attribute.HP ? provideValueForHp(entity) : AttributeValueProvider.provideValueForAttribute(context, attribute, entity));
				} else {
					continue;
				}
				value = operation == AlgebraicOperation.ADD ? (value + itemVal) : operation.performOperation(value, itemVal);
			}
		}

		if (!matchedAny) {
			if (hasValue2) {
				return value2Vp != null ? value2Vp.compute(context, player, targetEntity, host) : constValue2;
			}
			return 0;
		}
		return value;
	}

	public static void execFastBuffSpell(
			GameContext context,
			Player player,
			net.demilich.metastone.game.spells.desc.SpellDesc desc,
			Entity source,
			Entity target,
			int attackBonus,
			int hpBonus,
			boolean hasAttack,
			boolean hasHp) {
		if (target == null) {
			return;
		}
		GameStateArena.TriggerList trigs = GameStateArena.getContextArena(context).triggers;
		if ((trigs.kindMask & GameStateArena.KIND_MODIFY_BUFF_AURA) != 0) {
			for (int i = 0, n = trigs.size; i < n; i++) {
				if (trigs.elements[i] instanceof ModifyBuffSpellAura aura && !aura.isExpired() && aura.isActivated()) {
					DescBytecodeCompiler.execBuffSpell(context, player, desc, source, target, attackBonus, hpBonus, 0, 0, false, hasAttack, hasHp);
					return;
				}
			}
		}
		if (attackBonus != 0 || hpBonus != 0) {
			net.demilich.metastone.game.spells.trigger.Enchantment.attachBuffEnchantment(context, player, desc, source, target, attackBonus, hpBonus, 0);
		}
		if (attackBonus != 0) {
			if (target instanceof net.demilich.metastone.game.entities.heroes.Hero) {
				target.modifyAttribute(Attribute.TEMPORARY_ATTACK_BONUS, attackBonus);
			} else {
				target.modifyAttribute(Attribute.ATTACK_BONUS, attackBonus);
			}
		}
		if (hpBonus != 0) {
			if (target instanceof Weapon weapon) {
				context.getLogic().modifyDurability(weapon, hpBonus);
			} else {
				context.getLogic().modifyHpSpell(source, target, hpBonus);
			}
		}
	}
}
