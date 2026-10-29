package net.demilich.metastone.game.cards.desc.compiler;

import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import com.hiddenswitch.spellsource.rpc.Spellsource.EntityTypeMessage.EntityType;
import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import com.hiddenswitch.spellsource.rpc.Spellsource.RarityMessage.Rarity;
import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.AttributeMap;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityLocation;
import net.demilich.metastone.game.entities.HasCard;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.environment.Environment;
import net.demilich.metastone.game.environment.EnvironmentDeque;
import net.demilich.metastone.game.environment.EnvironmentValue;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.HasValue;
import net.demilich.metastone.game.logic.CustomCloneable;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.AddAttributeSpell;
import net.demilich.metastone.game.spells.AddDeathrattleSpell;
import net.demilich.metastone.game.spells.AddEnchantmentSpell;
import net.demilich.metastone.game.spells.BuffSpell;
import net.demilich.metastone.game.spells.ModifyAttributeSpell;
import net.demilich.metastone.game.spells.RemoveAttributeSpell;
import net.demilich.metastone.game.spells.SetAttackSpell;
import net.demilich.metastone.game.spells.SetCardSpell;
import net.demilich.metastone.game.spells.SetHpSpell;
import net.demilich.metastone.game.spells.TemporaryAttackSpell;
import net.demilich.metastone.game.spells.aura.Aura;
import net.demilich.metastone.game.spells.custom.EnvironmentEntityList;
import net.demilich.metastone.game.spells.desc.OpenerDesc;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.Aftermath;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.spells.trigger.Opener;
import net.demilich.metastone.game.spells.trigger.Trigger;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.IdFactory;
import net.demilich.metastone.game.targeting.TargetNotFoundException;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.RandomAccess;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Unified Pure-Java Heap-Array ECS Arena backing a {@link GameContext} and both of its {@link Player}s.
 * <p>
 * Consolidates into a single Copy-on-Write arena per {@link GameContext}:
 * <ul>
 *   <li>Global entity table ({@link #entities}) indexed by dense entity ID across both players, with per-entity
 *       Copy-on-Write bitmasks ({@link #s0}, {@link #s1}, {@link #sMore}) so untouched entities in {@code DECK},
 *       {@code GRAVEYARD}, {@code REMOVED_FROM_PLAY}, {@code HAND}, {@code BATTLEFIELD}, and {@code ENCHANTMENT}
 *       are shared without cloning across {@link GameContext#clone()}.</li>
 *   <li>All 32 {@code (player, zone)} entity arrays ({@link #zoneArrays}) and sizes ({@link #zoneSizes}), with a
 *       single {@code long sharedZoneMask} for Copy-on-Write zone array sharing.</li>
 *   <li>All 8 hot {@link Environment} stacks packed into a single contiguous {@code int[] stackData} + {@code long stackTops}.</li>
 *   <li>Copy-on-Write {@link TriggerList} with a 128-bit per-host trigger presence bitmask ({@link #hostMask0}, {@link #hostMask1})
 *       for $O(1)$ negative checks in {@link GameLogic#getActiveTriggers}, {@link GameLogic#removeEnchantments}, and
 *       {@link GameLogic#getAftermaths}.</li>
 * </ul>
 */
public final class GameStateArena implements Serializable {
	public static final int NUM_ZONE_SLOTS = 32;

	public static final int STACK_SUMMON_REF = 0;
	public static final int STACK_EVENT_TARGET_REF = 1;
	public static final int STACK_SPELL_TARGET_REF = 2;
	public static final int STACK_OUTPUT_REF = 3;
	public static final int STACK_SPELL_VALUE = 4;
	public static final int STACK_EVENT_SOURCE_REF = 5;
	public static final int STACK_EVENT_VALUE = 6;
	public static final int STACK_TRIGGER_HOST_REF = 7;
	public static final int NUM_STACKS = 8;
	public static final int DEFAULT_STACK_CAPACITY = 16;

	public static final int KIND_AURA = 1 << 0;
	public static final int KIND_MODIFY_BUFF_AURA = 1 << 1;
	public static final int KIND_SPELL_OVERRIDE_OR_INCORRUPT_AURA = 1 << 2;
	public static final int KIND_PHYSICAL_ATTACK_OVERRIDE_AURA = 1 << 3;
	public static final int KIND_OPENER = 1 << 4;
	public static final int KIND_AFTERMATH = 1 << 5;
	public static final int KIND_TURN_TRIGGER = 1 << 6;
	public static final int KIND_EVENT_LISTENER = 1 << 7;
	public static final int KIND_CARD_COST_MODIFIER = 1 << 8;

	public static final String SYNTHESIZED_ENCHANTMENT_INFIX = "__ench_";

	private static final int REF_CACHE_MIN = -64;
	private static final int REF_CACHE_MAX = 512;
	private static final EntityReference[] REF_CACHE = new EntityReference[REF_CACHE_MAX - REF_CACHE_MIN];

	private static final Zones[] IN_PLAY_NON_ENCHANTMENT_ZONES = new Zones[]{
			Zones.PLAYER, Zones.HERO, Zones.HERO_POWER, Zones.WEAPON, Zones.BATTLEFIELD, Zones.SECRET, Zones.QUEST
	};

	private static final Zones[] ALL_PLAYER_ZONES = new Zones[]{
			Zones.PLAYER, Zones.BATTLEFIELD, Zones.DECK, Zones.GRAVEYARD, Zones.HAND, Zones.HERO,
			Zones.HERO_POWER, Zones.SET_ASIDE_ZONE, Zones.WEAPON, Zones.SECRET, Zones.DISCOVER,
			Zones.REMOVED_FROM_PLAY, Zones.QUEST, Zones.ENCHANTMENT
	};

	static {
		for (int id = REF_CACHE_MIN; id < REF_CACHE_MAX; id++) {
			REF_CACHE[id - REF_CACHE_MIN] = new EntityReference(id);
		}
	}

	public static EntityReference refOf(int id) {
		if (id == IdFactory.UNASSIGNED) {
			return EntityReference.NONE;
		}
		if (id >= REF_CACHE_MIN && id < REF_CACHE_MAX) {
			return REF_CACHE[id - REF_CACHE_MIN];
		}
		return new EntityReference(id);
	}

	public static int slot(int player, Zones zone) {
		int p = player == 1 ? 1 : 0;
		return (p << 4) + zone.ordinal();
	}

	private static int initialZoneCapacity(Zones zone) {
		return switch (zone) {
			case DECK -> 30;
			case BATTLEFIELD, ENCHANTMENT -> 8;
			case HAND, GRAVEYARD, REMOVED_FROM_PLAY -> 10;
			default -> 2;
		};
	}

	// 1. Global Entity Lookup Table + Per-Entity COW Bitmask
	public Entity[] entities;
	public long s0;
	public long s1;
	public long[] sMore;

	// 2. Unified Zone Table (32 slots: player 0 = 0..15, player 1 = 16..31)
	public Entity[][] zoneArrays;
	public int[] zoneSizes;
	public long sharedZoneMask;

	// 3. Packed Environment Stacks
	public int[] stackData;
	public int stackCapacity = DEFAULT_STACK_CAPACITY;
	public long stackTops;
	public boolean stackShared;

	private transient RefStackView[] refStackViews;
	private transient IntStackView[] intStackViews;

	// 4. Environment Map + Extras
	public EnumMap<Environment, Object> envExtras;
	public boolean envExtrasShared;
	public final EnvironmentMap envMap;

	// 5. COW TriggerList + Per-Host Bitmask
	public final TriggerList triggers;
	public long hostMask0;
	public long hostMask1;

	public GameStateArena() {
		this(128);
	}

	public GameStateArena(int initialEntityCapacity) {
		this.entities = new Entity[Math.max(initialEntityCapacity, 16)];
		this.zoneArrays = new Entity[NUM_ZONE_SLOTS][];
		this.zoneSizes = new int[NUM_ZONE_SLOTS];
		for (Zones z : Zones.values()) {
			if (z == Zones.UNRECOGNIZED) {
				continue;
			}
			int cap = initialZoneCapacity(z);
			this.zoneArrays[slot(0, z)] = new Entity[cap];
			this.zoneArrays[slot(1, z)] = new Entity[cap];
		}
		this.envMap = new EnvironmentMap();
		this.triggers = new TriggerList();
	}

	private GameStateArena(GameStateArena parent, boolean eagerUnshare) {
		this.entities = parent.entities.clone();
		this.s0 = ~3L;
		this.s1 = -1L;
		if (this.entities.length > 128) {
			int words = ((this.entities.length - 128) + 63) >>> 6;
			this.sMore = new long[words];
			Arrays.fill(this.sMore, -1L);
		}

		this.zoneArrays = parent.zoneArrays.clone();
		this.zoneSizes = parent.zoneSizes.clone();
		this.sharedZoneMask = -1L;

		this.stackTops = parent.stackTops;
		this.stackCapacity = parent.stackCapacity;
		if (parent.stackTops != 0L && parent.stackData != null) {
			this.stackData = parent.stackData;
			this.stackShared = true;
		}

		if (parent.envExtras != null && !parent.envExtras.isEmpty()) {
			this.envExtras = parent.envExtras;
			this.envExtrasShared = true;
		}
		this.envMap = new EnvironmentMap();

		this.triggers = new TriggerList(parent.triggers, false);
		this.hostMask0 = parent.hostMask0;
		this.hostMask1 = parent.hostMask1;
		if (eagerUnshare) {
			unshareAll();
		}
	}

	public GameStateArena cloneArena(boolean eagerUnshare) {
		return new GameStateArena(this, eagerUnshare);
	}

	public void unshareAll() {
		for (int p = 0; p < 2; p++) {
			for (Zones z : Zones.values()) {
				if (z == Zones.UNRECOGNIZED) {
					continue;
				}
				int s = slot(p, z);
				int size = zoneSizes[s];
				if (size > 0) {
					unshareZone(s);
					for (int i = 0; i < size; i++) {
						unshareAt(s, p, z, i);
					}
				}
			}
		}
		for (int id = 2, n = entities.length; id < n; id++) {
			if (entities[id] != null && isEntityShared(id)) {
				unshareById(id);
			}
		}
		triggers.unshare();
	}

	// =========================================================================
	// Woven Bridge Stubs (bodies woven by SpellsourceClassfileWeaver)
	// =========================================================================

	public static GameStateArena getContextArena(GameContext context) {
		return ((EntityLookupTemplate) (Object) context.getPlayer1().getLookup()).arena;
	}

	public static void setEnchantmentArena(Enchantment ench, GameStateArena arena) {
		// Woven by SpellsourceClassfileWeaver to: ench.arena = arena;
	}

	// =========================================================================
	// Per-Entity Copy-on-Write Bitmask & Global Lookup
	// =========================================================================

	public boolean isEntityShared(int id) {
		if (id < 64) {
			return (s0 & (1L << id)) != 0L;
		}
		if (id < 128) {
			return (s1 & (1L << (id - 64))) != 0L;
		}
		if (sMore != null) {
			int w = (id - 128) >>> 6;
			if (w < sMore.length) {
				return (sMore[w] & (1L << (id & 63))) != 0L;
			}
		}
		return false;
	}

	public void clearEntityShared(int id) {
		if (id < 64) {
			s0 &= ~(1L << id);
		} else if (id < 128) {
			s1 &= ~(1L << (id - 64));
		} else if (sMore != null) {
			int w = (id - 128) >>> 6;
			if (w < sMore.length) {
				sMore[w] &= ~(1L << (id & 63));
			}
		}
	}

	public void markAllEntitiesShared() {
		this.s0 = ~3L;
		this.s1 = -1L;
		if (entities.length > 128) {
			int words = ((entities.length - 128) + 63) >>> 6;
			this.sMore = new long[words];
			Arrays.fill(this.sMore, -1L);
		}
	}

	public void ensureEntityCapacity(int id) {
		if (id >= entities.length) {
			int newLen = Math.max(entities.length << 1, id + 16);
			entities = Arrays.copyOf(entities, newLen);
		}
	}

	public void putEntityDirect(int id, Entity entity) {
		if (id < 0) {
			return;
		}
		ensureEntityCapacity(id);
		entities[id] = entity;
		clearEntityShared(id);
	}

	public void removeEntityDirect(int id) {
		if (id < 0 || id >= entities.length) {
			return;
		}
		entities[id] = null;
		clearEntityShared(id);
	}

	public Entity getEntityById(int id) {
		if (id < 0 || id >= entities.length) {
			return null;
		}
		Entity e = entities[id];
		if (e == null) {
			return null;
		}
		if (id > 1 && isEntityShared(id)) {
			return unshareById(id);
		}
		return e;
	}

	public Entity getEntityByIdForPlayer(int player, int id) {
		if (id < 0 || id >= entities.length) {
			return null;
		}
		Entity e = entities[id];
		if (e == null) {
			return null;
		}
		int locPlayer = e.getEntityLocation().getPlayer();
		if (locPlayer != player && e.getOwner() != player && id != player) {
			return null;
		}
		if (id > 1 && isEntityShared(id)) {
			return unshareById(id);
		}
		return e;
	}

	public Entity unshareAt(int slot, int player, Zones zone, int index) {
		unshareZone(slot);
		Entity[] arr = zoneArrays[slot];
		Entity e = arr[index];
		if (e == null) {
			return null;
		}
		int id = e.getId();
		if (id == 0 || id == 1) {
			Entity pEnt = id < entities.length ? entities[id] : null;
			if (pEnt != null) {
				arr[index] = pEnt;
				return pEnt;
			}
			return e;
		}
		if (id >= 0 && !isEntityShared(id)) {
			return e;
		}
		Entity cloned = e.clone();
		cloned.setEntityLocation(EntityLocation.of(zone, player, index));
		arr[index] = cloned;
		if (id >= 0) {
			ensureEntityCapacity(id);
			entities[id] = cloned;
			clearEntityShared(id);
		}
		if (cloned instanceof Enchantment ench) {
			setEnchantmentArena(ench, this);
			syncUnsharedEnchantmentInTriggers(e, ench);
		}
		return cloned;
	}

	private void syncUnsharedEnchantmentInTriggers(Entity oldEnch, Enchantment newEnch) {
		if (triggers.isShared) {
			triggers.unshare();
		}
		Trigger[] arr = triggers.elements;
		int n = triggers.size;
		for (int i = 0; i < n; i++) {
			if (arr[i] == oldEnch) {
				arr[i] = newEnch;
			}
		}
	}

	public Entity unshareById(int id) {
		Entity e = entities[id];
		if (e == null) {
			return null;
		}
		if (id == 0 || id == 1 || !isEntityShared(id)) {
			return e;
		}
		EntityLocation loc = e.getEntityLocation();
		int p = loc.getPlayer();
		Zones z = loc.getZone();
		if ((p == 0 || p == 1) && z != null) {
			int slot = slot(p, z);
			int size = zoneSizes[slot];
			Entity[] arr = zoneArrays[slot];
			if (arr != null) {
				int idx = loc.getIndex();
				if (idx >= 0 && idx < size && arr[idx] == e) {
					return unshareAt(slot, p, z, idx);
				}
				for (int i = 0; i < size; i++) {
					if (arr[i] == e) {
						return unshareAt(slot, p, z, i);
					}
				}
			}
		}
		Entity cloned = e.clone();
		entities[id] = cloned;
		clearEntityShared(id);
		if (cloned instanceof Enchantment ench) {
			setEnchantmentArena(ench, this);
			syncUnsharedEnchantmentInTriggers(e, ench);
		}
		return cloned;
	}

	public int countEntitiesForPlayer(int player) {
		int count = 0;
		Entity[] tab = this.entities;
		for (int i = 0, n = tab.length; i < n; i++) {
			Entity e = tab[i];
			if (e != null && e.getZone() != Zones.ENCHANTMENT) {
				if (player < 0 || e.getEntityLocation().getPlayer() == player || i == player) {
					count++;
				}
			}
		}
		return count;
	}

	public Iterator<Entity> entityIteratorForPlayer(int player) {
		return new Iterator<>() {
			private int nextIdx = findNext(0);

			private int findNext(int from) {
				Entity[] tab = entities;
				for (int i = from, n = tab.length; i < n; i++) {
					Entity e = tab[i];
					if (e != null && e.getZone() != Zones.ENCHANTMENT) {
						if (player < 0 || e.getEntityLocation().getPlayer() == player || i == player) {
							return i;
						}
					}
				}
				return -1;
			}

			@Override
			public boolean hasNext() {
				return nextIdx >= 0;
			}

			@Override
			public Entity next() {
				if (nextIdx < 0) {
					throw new NoSuchElementException();
				}
				int cur = nextIdx;
				nextIdx = findNext(cur + 1);
				return getEntityById(cur);
			}
		};
	}

	// =========================================================================
	// Zone Operations (Per-Zone & Per-Entity Copy-on-Write)
	// =========================================================================

	public void unshareZone(int slot) {
		long bit = 1L << slot;
		if ((sharedZoneMask & bit) != 0L) {
			Entity[] old = zoneArrays[slot];
			int size = zoneSizes[slot];
			int cap = Math.max(old.length, size + 2);
			zoneArrays[slot] = Arrays.copyOf(old, cap);
			sharedZoneMask &= ~bit;
		}
	}

	public Entity zoneGet(int slot, int player, Zones zone, int index) {
		int size = zoneSizes[slot];
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
		}
		Entity e = zoneArrays[slot][index];
		int id = e.getId();
		if (id == 0 || id == 1) {
			Entity pEnt = id < entities.length ? entities[id] : null;
			return pEnt != null ? pEnt : e;
		}
		if (id >= 0 && isEntityShared(id)) {
			return unshareAt(slot, player, zone, index);
		}
		return e;
	}

	public Entity zoneGetReadOnly(int slot, int index) {
		return zoneArrays[slot][index];
	}

	public void zoneSetUnchecked(int slot, int player, Zones zone, int index, Entity element, boolean hasLookup) {
		int size = zoneSizes[slot];
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
		}
		unshareZone(slot);
		zoneArrays[slot][index] = element;
		element.setEntityLocation(EntityLocation.of(zone, player, index));
		if (hasLookup && element.getId() != IdFactory.UNASSIGNED) {
			putEntityDirect(element.getId(), element);
		}
	}

	public void zoneAdd(int slot, int player, Zones zone, int index, Entity element, boolean hasLookup) {
		int size = zoneSizes[slot];
		if (index < 0 || index > size) {
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
		}
		unshareZone(slot);
		Entity[] arr = zoneArrays[slot];
		if (size >= arr.length) {
			arr = Arrays.copyOf(arr, Math.max(arr.length << 1, size + 4));
			zoneArrays[slot] = arr;
		}
		if (index < size) {
			System.arraycopy(arr, index, arr, index + 1, size - index);
		}
		arr[index] = element;
		int newSize = size + 1;
		zoneSizes[slot] = newSize;
		element.setEntityLocation(EntityLocation.of(zone, player, index));
		for (int i = index + 1; i < newSize; i++) {
			Entity shifted = arr[i];
			int sId = shifted.getId();
			if (sId < 0 || !isEntityShared(sId)) {
				shifted.setEntityLocation(EntityLocation.of(zone, player, i));
			}
		}
		if (hasLookup && element.getId() != IdFactory.UNASSIGNED) {
			putEntityDirect(element.getId(), element);
		}
	}

	public Entity zoneRemove(int slot, int player, Zones zone, int index, boolean removeFromLookup) {
		int size = zoneSizes[slot];
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
		}
		unshareZone(slot);
		Entity[] arr = zoneArrays[slot];
		Entity old = arr[index];
		Entity result = old;
		int rId = result.getId();
		if (rId > 1 && isEntityShared(rId)) {
			result = result.clone();
			clearEntityShared(rId);
			if (rId < entities.length) {
				entities[rId] = result;
			}
			if (result instanceof Enchantment ench) {
				setEnchantmentArena(ench, this);
				syncUnsharedEnchantmentInTriggers(old, ench);
			}
		}
		int numMoved = size - index - 1;
		if (numMoved > 0) {
			System.arraycopy(arr, index + 1, arr, index, numMoved);
		}
		int newSize = size - 1;
		arr[newSize] = null;
		zoneSizes[slot] = newSize;
		result.setEntityLocation(EntityLocation.UNASSIGNED);
		if (rId >= 0 && rId < entities.length && entities[rId] != null) {
			entities[rId] = result;
		}
		for (int i = index; i < newSize; i++) {
			Entity shifted = arr[i];
			int sId = shifted.getId();
			if (sId < 0 || !isEntityShared(sId)) {
				shifted.setEntityLocation(EntityLocation.of(zone, player, i));
			}
		}
		return result;
	}

	public void zoneClear(int slot, int player, boolean removeFromLookup) {
		int size = zoneSizes[slot];
		if (size == 0) {
			return;
		}
		unshareZone(slot);
		Entity[] arr = zoneArrays[slot];
		for (int i = 0; i < size; i++) {
			Entity e = arr[i];
			if (e != null) {
				int id = e.getId();
				if (id > 1 && isEntityShared(id)) {
					Entity cloned = e.clone();
					clearEntityShared(id);
					if (id < entities.length) {
						entities[id] = cloned;
					}
					if (cloned instanceof Enchantment ench) {
						setEnchantmentArena(ench, this);
						syncUnsharedEnchantmentInTriggers(e, ench);
					}
					e = cloned;
				}
				e.setEntityLocation(EntityLocation.UNASSIGNED);
				arr[i] = null;
			}
		}
		zoneSizes[slot] = 0;
	}

	public int zoneIndexOf(int slot, int player, Zones zone, Object o) {
		if (o == null) {
			return -1;
		}
		int size = zoneSizes[slot];
		if (size == 0) {
			return -1;
		}
		Entity[] arr = zoneArrays[slot];
		if (o instanceof Entity entity) {
			EntityLocation loc = entity.getEntityLocation();
			if (loc.getZone() == zone && loc.getPlayer() == player) {
				int idx = loc.getIndex();
				if (idx >= 0 && idx < size) {
					Entity atIdx = arr[idx];
					if (atIdx == entity || (entity.getId() != IdFactory.UNASSIGNED && atIdx.getId() == entity.getId())) {
						return idx;
					}
				}
				for (int i = 0; i < size; i++) {
					Entity at = arr[i];
					if (at == entity || (entity.getId() != IdFactory.UNASSIGNED && at.getId() == entity.getId())) {
						return i;
					}
				}
			}
		}
		for (int i = 0; i < size; i++) {
			if ( o.equals(arr[i])) {
				return i;
			}
		}
		return -1;
	}

	public void zoneMove(
			int srcSlot, int srcPlayer, Zones srcZone, int index, boolean srcHasLookup,
			int dstSlot, int dstPlayer, Zones dstZone, int dstIndex, boolean dstHasLookup) {
		int srcSize = zoneSizes[srcSlot];
		if (index < 0 || index >= srcSize) {
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + srcSize);
		}
		unshareZone(srcSlot);
		Entity[] srcArr = zoneArrays[srcSlot];
		Entity old = srcArr[index];
		Entity result = old;
		int rId = result.getId();
		if (rId > 1 && isEntityShared(rId)) {
			result = result.clone();
			clearEntityShared(rId);
			if (rId < entities.length) {
				entities[rId] = result;
			}
			if (result instanceof Enchantment ench) {
				setEnchantmentArena(ench, this);
				syncUnsharedEnchantmentInTriggers(old, ench);
			}
		}
		int numMoved = srcSize - index - 1;
		if (numMoved > 0) {
			System.arraycopy(srcArr, index + 1, srcArr, index, numMoved);
		}
		int newSrcSize = srcSize - 1;
		srcArr[newSrcSize] = null;
		zoneSizes[srcSlot] = newSrcSize;
		for (int i = index; i < newSrcSize; i++) {
			Entity shifted = srcArr[i];
			int sId = shifted.getId();
			if (sId < 0 || !isEntityShared(sId)) {
				shifted.setEntityLocation(EntityLocation.of(srcZone, srcPlayer, i));
			}
		}
		zoneAdd(dstSlot, dstPlayer, dstZone, dstIndex, result, dstHasLookup);
	}

	public void zoneExchange(int slot, int player, Zones zone, Entity entity1, Entity entity2) {
		int i = zoneIndexOf(slot, player, zone, entity1);
		int j = zoneIndexOf(slot, player, zone, entity2);
		unshareZone(slot);
		Entity[] arr = zoneArrays[slot];
		arr[i] = entity2;
		arr[j] = entity1;
		entity2.setEntityLocation(EntityLocation.of(zone, player, i));
		entity1.setEntityLocation(EntityLocation.of(zone, player, j));
	}

	public void zoneShuffle(int slot, int player, Zones zone, Random random) {
		int size = zoneSizes[slot];
		if (size <= 1) {
			if (size == 1) {
				Entity e = zoneArrays[slot][0];
				if (e.getId() < 0 || !isEntityShared(e.getId())) {
					e.setEntityLocation(EntityLocation.of(zone, player, 0));
				}
			}
			return;
		}
		unshareZone(slot);
		Entity[] arr = zoneArrays[slot];
		for (int i = size; i > 1; i--) {
			int j = random.nextInt(i);
			Entity tmp = arr[i - 1];
			arr[i - 1] = arr[j];
			arr[j] = tmp;
		}
		for (int i = 0; i < size; i++) {
			Entity e = arr[i];
			int id = e.getId();
			if (id < 0 || !isEntityShared(id)) {
				e.setEntityLocation(EntityLocation.of(zone, player, i));
			}
		}
	}

	public void updateZonePlayer(int oldSlot, int newSlot, int newPlayer, Zones zone) {
		if (oldSlot != newSlot) {
			int size = zoneSizes[oldSlot];
			Entity[] arr = zoneArrays[oldSlot];
			zoneArrays[newSlot] = arr;
			zoneSizes[newSlot] = size;
			zoneArrays[oldSlot] = new Entity[initialZoneCapacity(zone)];
			zoneSizes[oldSlot] = 0;
		}
		int size = zoneSizes[newSlot];
		Entity[] arr = zoneArrays[newSlot];
		for (int i = 0; i < size; i++) {
			Entity e = arr[i];
			if (e != null) {
				e.setEntityLocation(EntityLocation.of(zone, newPlayer, i));
			}
		}
		if (zone == Zones.PLAYER && size > 0 && newPlayer >= 0 && arr[0] != null) {
			putEntityDirect(newPlayer, arr[0]);
		}
	}

	public void bindClonedPlayer(int pid, Player player) {
		int pSlot = slot(pid, Zones.PLAYER);
		if ((sharedZoneMask & (1L << pSlot)) != 0L) {
			zoneArrays[pSlot] = new Entity[]{player, null};
			sharedZoneMask &= ~(1L << pSlot);
		} else {
			zoneArrays[pSlot][0] = player;
		}
		zoneSizes[pSlot] = 1;
		if (pid >= 0 && pid < entities.length) {
			entities[pid] = player;
		} else if (pid >= 0) {
			putEntityDirect(pid, player);
		}
	}

	/**
	 * Binds a {@link Player} and all 14 of its zones to a {@link GameContext}'s unified {@link GameStateArena}.
	 */
	@SuppressWarnings("unchecked")
	public static void bindPlayerToArena(GameStateArena targetArena, int index, Player player) {
		if (player.getId() != index) {
			player.setId(index);
		}
		EntityLookupTemplate lookup = (EntityLookupTemplate) (Object) player.getLookup();
		lookup.player = index;
		GameStateArena srcArena = lookup.arena;
		if (srcArena == targetArena) {
			targetArena.putEntityDirect(index, player);
			int pSlot = slot(index, Zones.PLAYER);
			targetArena.unshareZone(pSlot);
			targetArena.zoneArrays[pSlot][0] = player;
			if (targetArena.zoneSizes[pSlot] == 0) {
				targetArena.zoneSizes[pSlot] = 1;
			}
			player.setEntityLocation(EntityLocation.of(Zones.PLAYER, index, 0));
			return;
		}

		// Migrate all zones for this player from srcArena into targetArena
		for (Zones z : ALL_PLAYER_ZONES) {
			int s = slot(index, z);
			int size = srcArena.zoneSizes[s];
			Entity[] srcArr = srcArena.zoneArrays[s];
			targetArena.sharedZoneMask &= ~(1L << s);
			targetArena.zoneArrays[s] = Arrays.copyOf(srcArr, Math.max(srcArr.length, size + 2));
			targetArena.zoneSizes[s] = size;
			for (int i = 0; i < size; i++) {
				Entity e = srcArr[i];
				if (e != null && e.getId() != IdFactory.UNASSIGNED) {
					int id = e.getId();
					targetArena.ensureEntityCapacity(id);
					targetArena.entities[id] = e;
					if (srcArena.isEntityShared(id)) {
						if (id < 64) {
							targetArena.s0 |= (1L << id);
						} else if (id < 128) {
							targetArena.s1 |= (1L << (id - 64));
						} else {
							int w = (id - 128) >>> 6;
							if (targetArena.sMore == null) {
								targetArena.sMore = new long[Math.max(w + 1, 2)];
							} else if (w >= targetArena.sMore.length) {
								targetArena.sMore = Arrays.copyOf(targetArena.sMore, Math.max(w + 1, targetArena.sMore.length << 1));
							}
							targetArena.sMore[w] |= (1L << (id & 63));
						}
					} else {
						targetArena.clearEntityShared(id);
					}
				}
			}
		}
		targetArena.putEntityDirect(index, player);
		int pSlot = slot(index, Zones.PLAYER);
		targetArena.zoneArrays[pSlot][0] = player;
		if (targetArena.zoneSizes[pSlot] == 0) {
			targetArena.zoneSizes[pSlot] = 1;
		}
		player.setEntityLocation(EntityLocation.of(Zones.PLAYER, index, 0));

		lookup.arena = targetArena;
		for (Zones z : ALL_PLAYER_ZONES) {
			EntityZoneTemplate<?> ez = (EntityZoneTemplate<?>) (Object) player.getZone(z);
			if (ez != null) {
				ez.arena = targetArena;
				ez.lookup = lookup;
				ez.player = index;
				ez.slot = slot(index, z);
			}
		}
	}

	// =========================================================================
	// Packed Environment Stacks (int[] stackData + long stackTops)
	// =========================================================================

	private void ensureStackWritable(int stackIdx, int requiredDepth) {
		if (stackData == null) {
			stackCapacity = Math.max(DEFAULT_STACK_CAPACITY, requiredDepth);
			stackData = new int[NUM_STACKS * stackCapacity];
			stackShared = false;
			return;
		}
		if (requiredDepth > stackCapacity) {
			int newCap = Math.max(stackCapacity << 1, requiredDepth);
			int[] next = new int[NUM_STACKS * newCap];
			for (int s = 0; s < NUM_STACKS; s++) {
				int sp = stackSize(s);
				if (sp > 0) {
					System.arraycopy(stackData, s * stackCapacity, next, s * newCap, sp);
				}
			}
			stackData = next;
			stackCapacity = newCap;
			stackShared = false;
			return;
		}
		if (stackShared) {
			stackData = stackData.clone();
			stackShared = false;
		}
	}

	public int stackSize(int stackIdx) {
		return (int) ((stackTops >>> (stackIdx << 3)) & 0xFFL);
	}

	public boolean isStackEmpty(int stackIdx) {
		return ((stackTops >>> (stackIdx << 3)) & 0xFFL) == 0L;
	}

	private void setStackSize(int stackIdx, int newSize) {
		int shift = stackIdx << 3;
		stackTops = (stackTops & ~(0xFFL << shift)) | ((newSize & 0xFFL) << shift);
	}

	public void pushInt(int stackIdx, int value) {
		int sp = stackSize(stackIdx);
		ensureStackWritable(stackIdx, sp + 1);
		stackData[stackIdx * stackCapacity + sp] = value;
		setStackSize(stackIdx, sp + 1);
	}

	public int popInt(int stackIdx) {
		int sp = stackSize(stackIdx);
		if (sp == 0) {
			throw new NoSuchElementException("Environment stack " + stackIdx + " underflow");
		}
		int nextSp = sp - 1;
		setStackSize(stackIdx, nextSp);
		return stackData[stackIdx * stackCapacity + nextSp];
	}

	public int peekInt(int stackIdx) {
		int sp = stackSize(stackIdx);
		if (sp == 0) {
			return 0;
		}
		return stackData[stackIdx * stackCapacity + sp - 1];
	}

	public void pushRef(int stackIdx, EntityReference ref) {
		pushInt(stackIdx, ref == null ? IdFactory.UNASSIGNED : ref.getId());
	}

	public EntityReference popRef(int stackIdx) {
		return refOf(popInt(stackIdx));
	}

	public EntityReference peekRef(int stackIdx) {
		int sp = stackSize(stackIdx);
		if (sp == 0) {
			return null;
		}
		return refOf(stackData[stackIdx * stackCapacity + sp - 1]);
	}

	public Deque<EntityReference> getRefStack(int stackIdx) {
		if (refStackViews == null) {
			refStackViews = new RefStackView[NUM_STACKS];
		}
		RefStackView view = refStackViews[stackIdx];
		if (view == null) {
			view = new RefStackView(stackIdx);
			refStackViews[stackIdx] = view;
		}
		return view;
	}

	public Deque<Integer> getIntStack(int stackIdx) {
		if (intStackViews == null) {
			intStackViews = new IntStackView[NUM_STACKS];
		}
		IntStackView view = intStackViews[stackIdx];
		if (view == null) {
			view = new IntStackView(stackIdx);
			intStackViews[stackIdx] = view;
		}
		return view;
	}

	public static int stackIndexForEnvironment(Environment key) {
		return switch (key) {
			case SUMMON_REFERENCE_STACK -> STACK_SUMMON_REF;
			case EVENT_TARGET_REFERENCE_STACK -> STACK_EVENT_TARGET_REF;
			case SPELL_TARGET -> STACK_SPELL_TARGET_REF;
			case OUTPUTS -> STACK_OUTPUT_REF;
			case SPELL_VALUE_STACK -> STACK_SPELL_VALUE;
			case EVENT_SOURCE_REFERENCE_STACK -> STACK_EVENT_SOURCE_REF;
			case EVENT_VALUE_STACK -> STACK_EVENT_VALUE;
			case TRIGGER_HOST_STACK -> STACK_TRIGGER_HOST_REF;
			default -> -1;
		};
	}

	public static boolean isIntStackEnvironment(Environment key) {
		return key == Environment.SPELL_VALUE_STACK || key == Environment.EVENT_VALUE_STACK;
	}

	public final class RefStackView extends EnvironmentDeque<EntityReference> {
		private final int stackIdx;

		RefStackView(int stackIdx) {
			this.stackIdx = stackIdx;
		}

		@Override
		public void push(EntityReference e) {
			pushRef(stackIdx, e);
		}

		@Override
		public void addFirst(EntityReference e) {
			pushRef(stackIdx, e);
		}

		@Override
		public boolean add(EntityReference e) {
			addLast(e);
			return true;
		}

		@Override
		public void addLast(EntityReference e) {
			int sp = stackSize(stackIdx);
			ensureStackWritable(stackIdx, sp + 1);
			int base = stackIdx * stackCapacity;
			if (sp > 0) {
				System.arraycopy(stackData, base, stackData, base + 1, sp);
			}
			stackData[base] = e == null ? IdFactory.UNASSIGNED : e.getId();
			setStackSize(stackIdx, sp + 1);
		}

		@Override
		public EntityReference pop() {
			return popRef(stackIdx);
		}

		@Override
		public EntityReference removeFirst() {
			return popRef(stackIdx);
		}

		@Override
		public EntityReference remove() {
			return popRef(stackIdx);
		}

		@Override
		public EntityReference poll() {
			return isStackEmpty(stackIdx) ? null : popRef(stackIdx);
		}

		@Override
		public EntityReference pollFirst() {
			return poll();
		}

		@Override
		public EntityReference removeLast() {
			int sp = stackSize(stackIdx);
			if (sp == 0) {
				throw new NoSuchElementException();
			}
			ensureStackWritable(stackIdx, sp);
			int base = stackIdx * stackCapacity;
			int val = stackData[base];
			if (sp > 1) {
				System.arraycopy(stackData, base + 1, stackData, base, sp - 1);
			}
			setStackSize(stackIdx, sp - 1);
			return refOf(val);
		}

		@Override
		public EntityReference pollLast() {
			return isStackEmpty(stackIdx) ? null : removeLast();
		}

		@Override
		public EntityReference peek() {
			return peekRef(stackIdx);
		}

		@Override
		public EntityReference peekFirst() {
			return peekRef(stackIdx);
		}

		@Override
		public EntityReference getFirst() {
			EntityReference r = peekRef(stackIdx);
			if (r == null) {
				throw new NoSuchElementException();
			}
			return r;
		}

		@Override
		public EntityReference peekLast() {
			int sp = stackSize(stackIdx);
			if (sp == 0) {
				return null;
			}
			return refOf(stackData[stackIdx * stackCapacity]);
		}

		@Override
		public EntityReference getLast() {
			EntityReference r = peekLast();
			if (r == null) {
				throw new NoSuchElementException();
			}
			return r;
		}

		@Override
		public boolean isEmpty() {
			return isStackEmpty(stackIdx);
		}

		@Override
		public int size() {
			return stackSize(stackIdx);
		}

		@Override
		public void clear() {
			setStackSize(stackIdx, 0);
		}

		@Override
		public boolean contains(Object o) {
			if (!(o instanceof EntityReference ref)) {
				return false;
			}
			int targetId = ref.getId();
			int sp = stackSize(stackIdx);
			int base = stackIdx * stackCapacity;
			for (int i = 0; i < sp; i++) {
				if (stackData[base + i] == targetId) {
					return true;
				}
			}
			return false;
		}

		@Override
		public Iterator<EntityReference> iterator() {
			return new Iterator<>() {
				private int idx = stackSize(stackIdx) - 1;

				@Override
				public boolean hasNext() {
					return idx >= 0;
				}

				@Override
				public EntityReference next() {
					if (idx < 0) {
						throw new NoSuchElementException();
					}
					return refOf(stackData[stackIdx * stackCapacity + (idx--)]);
				}
			};
		}
	}

	public final class IntStackView extends EnvironmentDeque<Integer> {
		private final int stackIdx;

		IntStackView(int stackIdx) {
			this.stackIdx = stackIdx;
		}

		@Override
		public void push(Integer e) {
			pushInt(stackIdx, e == null ? 0 : e);
		}

		@Override
		public void addFirst(Integer e) {
			pushInt(stackIdx, e == null ? 0 : e);
		}

		@Override
		public boolean add(Integer e) {
			addLast(e);
			return true;
		}

		@Override
		public void addLast(Integer e) {
			int sp = stackSize(stackIdx);
			ensureStackWritable(stackIdx, sp + 1);
			int base = stackIdx * stackCapacity;
			if (sp > 0) {
				System.arraycopy(stackData, base, stackData, base + 1, sp);
			}
			stackData[base] = e == null ? 0 : e;
			setStackSize(stackIdx, sp + 1);
		}

		@Override
		public Integer pop() {
			return popInt(stackIdx);
		}

		@Override
		public Integer removeFirst() {
			return popInt(stackIdx);
		}

		@Override
		public Integer remove() {
			return popInt(stackIdx);
		}

		@Override
		public Integer poll() {
			return isStackEmpty(stackIdx) ? null : popInt(stackIdx);
		}

		@Override
		public Integer pollFirst() {
			return poll();
		}

		@Override
		public Integer removeLast() {
			int sp = stackSize(stackIdx);
			if (sp == 0) {
				throw new NoSuchElementException();
			}
			ensureStackWritable(stackIdx, sp);
			int base = stackIdx * stackCapacity;
			int val = stackData[base];
			if (sp > 1) {
				System.arraycopy(stackData, base + 1, stackData, base, sp - 1);
			}
			setStackSize(stackIdx, sp - 1);
			return val;
		}

		@Override
		public Integer pollLast() {
			return isStackEmpty(stackIdx) ? null : removeLast();
		}

		@Override
		public Integer peek() {
			if (isStackEmpty(stackIdx)) {
				return null;
			}
			return peekInt(stackIdx);
		}

		@Override
		public Integer peekFirst() {
			return peek();
		}

		@Override
		public Integer getFirst() {
			if (isStackEmpty(stackIdx)) {
				throw new NoSuchElementException();
			}
			return peekInt(stackIdx);
		}

		@Override
		public Integer peekLast() {
			int sp = stackSize(stackIdx);
			if (sp == 0) {
				return null;
			}
			return stackData[stackIdx * stackCapacity];
		}

		@Override
		public Integer getLast() {
			Integer r = peekLast();
			if (r == null) {
				throw new NoSuchElementException();
			}
			return r;
		}

		@Override
		public boolean isEmpty() {
			return isStackEmpty(stackIdx);
		}

		@Override
		public int size() {
			return stackSize(stackIdx);
		}

		@Override
		public void clear() {
			setStackSize(stackIdx, 0);
		}

		@Override
		public boolean contains(Object o) {
			if (!(o instanceof Integer targetVal)) {
				return false;
			}
			int val = targetVal;
			int sp = stackSize(stackIdx);
			int base = stackIdx * stackCapacity;
			for (int i = 0; i < sp; i++) {
				if (stackData[base + i] == val) {
					return true;
				}
			}
			return false;
		}

		@Override
		public Iterator<Integer> iterator() {
			return new Iterator<>() {
				private int idx = stackSize(stackIdx) - 1;

				@Override
				public boolean hasNext() {
					return idx >= 0;
				}

				@Override
				public Integer next() {
					if (idx < 0) {
						throw new NoSuchElementException();
					}
					return stackData[stackIdx * stackCapacity + (idx--)];
				}
			};
		}
	}

	// =========================================================================
	// Copy-on-Write EnvironmentMap
	// =========================================================================

	private void unshareEnvExtras() {
		if (envExtras == null) {
			envExtras = new EnumMap<>(Environment.class);
			envExtrasShared = false;
		} else if (envExtrasShared) {
			EnumMap<Environment, Object> copy = new EnumMap<>(Environment.class);
			for (Map.Entry<Environment, Object> e : envExtras.entrySet()) {
				Object v = e.getValue();
				if (v instanceof EnvironmentValue ev) {
					copy.put(e.getKey(), ev.getCopy());
				} else {
					copy.put(e.getKey(), v);
				}
			}
			envExtras = copy;
			envExtrasShared = false;
		}
	}

	public void syncEnvironmentFrom(Map<Environment, Object> other) {
		if (other == null || other == this.envMap) {
			return;
		}
		this.stackTops = 0L;
		this.envExtras = null;
		this.envExtrasShared = false;
		for (Map.Entry<Environment, Object> entry : other.entrySet()) {
			this.envMap.put(entry.getKey(), entry.getValue());
		}
	}

	public final class EnvironmentMap extends AbstractMap<Environment, Object> implements Serializable {
		@Override
		public Object get(Object keyObj) {
			if (!(keyObj instanceof Environment key)) {
				return null;
			}
			int sIdx = stackIndexForEnvironment(key);
			if (sIdx >= 0) {
				return isIntStackEnvironment(key) ? getIntStack(sIdx) : getRefStack(sIdx);
			}
			if (envExtras == null) {
				return null;
			}
			if (envExtrasShared) {
				Object v = envExtras.get(key);
				if (v instanceof EnvironmentValue) {
					unshareEnvExtras();
					return envExtras.get(key);
				}
				return v;
			}
			return envExtras.get(key);
		}

		@Override
		public boolean containsKey(Object keyObj) {
			if (!(keyObj instanceof Environment key)) {
				return false;
			}
			int sIdx = stackIndexForEnvironment(key);
			if (sIdx >= 0) {
				return !isStackEmpty(sIdx);
			}
			return envExtras != null && envExtras.containsKey(key);
		}

		@Override
		@SuppressWarnings("unchecked")
		public Object put(Environment key, Object value) {
			int sIdx = stackIndexForEnvironment(key);
			if (sIdx >= 0) {
				if (value instanceof RefStackView || value instanceof IntStackView) {
					return value;
				}
				setStackSize(sIdx, 0);
				if (value instanceof Collection<?> col && !col.isEmpty()) {
					if (isIntStackEnvironment(key)) {
						Deque<Integer> st = getIntStack(sIdx);
						for (Object item : col) {
							st.addLast((Integer) item);
						}
						return st;
					} else {
						Deque<EntityReference> st = getRefStack(sIdx);
						for (Object item : col) {
							st.addLast((EntityReference) item);
						}
						return st;
					}
				}
				return isIntStackEnvironment(key) ? getIntStack(sIdx) : getRefStack(sIdx);
			}
			unshareEnvExtras();
			return envExtras.put(key, value);
		}

		@Override
		public Object remove(Object keyObj) {
			if (!(keyObj instanceof Environment key)) {
				return null;
			}
			int sIdx = stackIndexForEnvironment(key);
			if (sIdx >= 0) {
				setStackSize(sIdx, 0);
				return null;
			}
			if (envExtras == null || !envExtras.containsKey(key)) {
				return null;
			}
			unshareEnvExtras();
			return envExtras.remove(key);
		}

		@Override
		public void clear() {
			stackTops = 0L;
			if (envExtras != null) {
				envExtras = null;
				envExtrasShared = false;
			}
		}

		@Override
		public Set<Entry<Environment, Object>> entrySet() {
			if (envExtras == null || envExtras.isEmpty()) {
				return Collections.emptySet();
			}
			unshareEnvExtras();
			return envExtras.entrySet();
		}
	}

	// =========================================================================
	// COW TriggerList + Per-Host Bitmask
	// =========================================================================

	public void recordHostBit(int hostId) {
		if (hostId >= 0 && hostId < 64) {
			hostMask0 |= (1L << hostId);
		} else if (hostId >= 64 && hostId < 128) {
			hostMask1 |= (1L << (hostId - 64));
		}
	}

	public boolean hasHostBit(int hostId) {
		if (hostId < 0) {
			return true;
		}
		if (hostId < 64) {
			return (hostMask0 & (1L << hostId)) != 0L;
		}
		if (hostId < 128) {
			return (hostMask1 & (1L << (hostId - 64))) != 0L;
		}
		return true;
	}

	public void syncTriggersFrom(List<Trigger> other) {
		if (other == null || other == this.triggers) {
			return;
		}
		this.triggers.clear();
		for (int i = 0, n = other.size(); i < n; i++) {
			this.triggers.add(other.get(i));
		}
	}

	private static int classifyTrigger(Trigger t) {
		if (t == null) {
			return 0;
		}
		int mask = 0;
		if (t instanceof Aura) {
			mask |= KIND_AURA | KIND_EVENT_LISTENER;
			if (t instanceof net.demilich.metastone.game.spells.aura.ModifyBuffSpellAura) {
				mask |= KIND_MODIFY_BUFF_AURA;
			} else if (t instanceof net.demilich.metastone.game.spells.aura.SpellOverrideAura
					|| t instanceof net.demilich.metastone.game.spells.aura.IncorruptibilityAura) {
				mask |= KIND_SPELL_OVERRIDE_OR_INCORRUPT_AURA;
			} else if (t instanceof net.demilich.metastone.game.spells.aura.PhysicalAttackTargetOverrideAura) {
				mask |= KIND_PHYSICAL_ATTACK_OVERRIDE_AURA;
			}
			return mask;
		}
		if (t instanceof Opener) {
			return KIND_OPENER;
		}
		if (t instanceof Aftermath) {
			return KIND_AFTERMATH;
		}
		if (t instanceof Enchantment ench) {
			if (ench instanceof net.demilich.metastone.game.cards.costmodifier.CardCostModifier) {
				mask |= KIND_CARD_COST_MODIFIER;
			}
			if (ench.oneTurnOnly() || ench.getUsesSpellTrigger()
					|| !ench.getActivationTriggers().isEmpty()
					|| !ench.getExpirationTriggers().isEmpty()) {
				mask |= KIND_EVENT_LISTENER;
			}
			List<net.demilich.metastone.game.spells.trigger.EventTrigger> trigs = ench.getTriggers();
			for (int i = 0, n = trigs.size(); i < n; i++) {
				if (trigs.get(i) instanceof net.demilich.metastone.game.spells.trigger.TurnTrigger) {
					mask |= KIND_TURN_TRIGGER | KIND_EVENT_LISTENER;
					break;
				}
			}
			return mask;
		}
		return KIND_EVENT_LISTENER;
	}

	public final class TriggerList extends AbstractList<Trigger> implements RandomAccess, Serializable {
		public Trigger[] elements;
		public int size;
		public boolean isShared;
		public int kindMask;

		public TriggerList() {
			this.elements = new Trigger[8];
			this.size = 0;
			this.isShared = false;
			this.kindMask = 0;
		}

		public TriggerList(TriggerList parent, boolean alsoShareParent) {
			this.elements = parent.elements;
			this.size = parent.size;
			this.kindMask = parent.kindMask;
			if (parent.size > 0) {
				if (alsoShareParent) {
					parent.isShared = true;
				}
				this.isShared = true;
			}
		}

		public void unshare() {
			if (!isShared) {
				return;
			}
			this.isShared = false;
			int n = this.size;
			Trigger[] copy = new Trigger[Math.max(elements.length, n + 2)];
			for (int i = 0; i < n; i++) {
				Trigger t = elements[i];
				if (t instanceof Enchantment ench) {
					int id = ench.getId();
					if (id > 1 && id < entities.length && entities[id] != null) {
						copy[i] = ench;
					} else {
						Enchantment cloned = ench.clone();
						if (id >= 0) {
							putEntityDirect(id, cloned);
						}
						setEnchantmentArena(cloned, GameStateArena.this);
						copy[i] = cloned;
					}
				} else if (t != null) {
					copy[i] = t.clone();
				}
			}
			this.elements = copy;
		}

		public Trigger getUnshared(int index) {
			if (isShared) {
				unshare();
			}
			Trigger t = elements[index];
			if (t instanceof Enchantment ench) {
				int id = ench.getId();
				if (id > 1 && isEntityShared(id)) {
					Enchantment cloned = (Enchantment) getEntityById(id);
					if (cloned != null) {
						elements[index] = cloned;
						return cloned;
					}
				}
			}
			return t;
		}

		@Override
		public int size() {
			return size;
		}

		@Override
		public boolean isEmpty() {
			return size == 0;
		}

		@Override
		public Trigger get(int index) {
			if (index < 0 || index >= size) {
				throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
			}
			return getUnshared(index);
		}

		public Trigger getReadOnly(int index) {
			return elements[index];
		}

		@Override
		public boolean add(Trigger trigger) {
			if (isShared) {
				unshare();
			}
			if (size >= elements.length) {
				elements = Arrays.copyOf(elements, Math.max(elements.length << 1, size + 4));
			}
			elements[size++] = trigger;
			if (trigger != null) {
				kindMask |= classifyTrigger(trigger);
				EntityReference hostRef = trigger.getHostReference();
				if (hostRef != null) {
					recordHostBit(hostRef.getId());
				}
				if (trigger instanceof Enchantment ench) {
					setEnchantmentArena(ench, GameStateArena.this);
				}
			}
			return true;
		}

		@Override
		public Trigger set(int index, Trigger element) {
			if (index < 0 || index >= size) {
				throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
			}
			if (isShared) {
				unshare();
			}
			Trigger old = elements[index];
			elements[index] = element;
			if (element != null) {
				kindMask |= classifyTrigger(element);
				if (element.getHostReference() != null) {
					recordHostBit(element.getHostReference().getId());
				}
			}
			return old;
		}

		@Override
		public Trigger remove(int index) {
			if (index < 0 || index >= size) {
				throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
			}
			if (isShared) {
				unshare();
			}
			Trigger old = elements[index];
			int numMoved = size - index - 1;
			if (numMoved > 0) {
				System.arraycopy(elements, index + 1, elements, index, numMoved);
			}
			elements[--size] = null;
			return old;
		}

		@Override
		public void clear() {
			if (isShared) {
				elements = new Trigger[8];
				size = 0;
				isShared = false;
			} else {
				Arrays.fill(elements, 0, size, null);
				size = 0;
			}
			kindMask = 0;
			hostMask0 = 0L;
			hostMask1 = 0L;
		}
	}

	// =========================================================================
	// Fast GameLogic / TargetLogic Helpers
	// =========================================================================

	public static Entity findEntityFast(GameContext context, EntityReference targetKey) throws TargetNotFoundException {
		int targetId = targetKey.getId();
		GameStateArena arena = getContextArena(context);
		if (targetId == EntityReference.EVENT_TARGET.getId()) {
			if (!arena.isStackEmpty(STACK_EVENT_TARGET_REF)) {
				return context.resolveSingleTarget(arena.peekRef(STACK_EVENT_TARGET_REF));
			}
			if (!arena.isStackEmpty(STACK_EVENT_SOURCE_REF)) {
				return context.resolveSingleTarget(arena.peekRef(STACK_EVENT_SOURCE_REF));
			}
		}
		if (targetId >= 0) {
			Entity found = arena.getEntityById(targetId);
			if (found != null) {
				return found;
			}
			TriggerList trigs = arena.triggers;
			int n = trigs.size;
			if (n > 0) {
				Trigger[] raw = trigs.elements;
				for (int i = 0; i < n; i++) {
					Trigger t = raw[i];
					if (t instanceof Enchantment ench && ench.getId() == targetId) {
						return (Enchantment) trigs.getUnshared(i);
					}
				}
			}
		}
		throw new TargetNotFoundException("not found", targetKey);
	}

	public static List<Trigger> getActiveTriggersFast(GameContext context, EntityReference hostReference) {
		if (hostReference == null) {
			return Collections.emptyList();
		}
		GameStateArena arena = getContextArena(context);
		TriggerList trigs = arena.triggers;
		int n = trigs.size;
		if (n == 0) {
			return Collections.emptyList();
		}
		int hostId = hostReference.getId();
		if (!arena.hasHostBit(hostId)) {
			return Collections.emptyList();
		}
		// Read-only scan first before unsharing
		Trigger[] raw = trigs.elements;
		int matchCount = 0;
		for (int i = 0; i < n; i++) {
			Trigger t = raw[i];
			if (!t.isExpired() && t.isActivated()) {
				EntityReference hr = t.getHostReference();
				if (hr != null && hr.getId() == hostId) {
					matchCount++;
				}
			}
		}
		if (matchCount == 0) {
			return Collections.emptyList();
		}
		List<Trigger> active = new ArrayList<>(matchCount);
		for (int i = 0; i < n; i++) {
			Trigger t = trigs.elements[i];
			if (!t.isExpired() && t.isActivated()) {
				EntityReference hr = t.getHostReference();
				if (hr != null && hr.getId() == hostId) {
					active.add(trigs.getUnshared(i));
				}
			}
		}
		return active;
	}

	public static List<Aftermath> getAftermathsFast(GameContext context, Entity host) {
		if (host == null) {
			return Collections.emptyList();
		}
		SpellDesc baseCardDeathrattle = null;
		if (host instanceof Card card && !card.hasAttribute(Attribute.SILENCED)) {
			baseCardDeathrattle = card.getDesc().getDeathrattle();
		}
		GameStateArena arena = getContextArena(context);
		TriggerList trigs = arena.triggers;
		int n = trigs.size;
		int hostId = host.getId();
		int matchCount = 0;
		boolean hasBaseInTriggers = false;
		if (n > 0 && (trigs.kindMask & KIND_AFTERMATH) != 0 && hostId != IdFactory.UNASSIGNED && arena.hasHostBit(hostId)) {
			Trigger[] raw = trigs.elements;
			for (int i = 0; i < n; i++) {
				Trigger t = raw[i];
				if (t instanceof Aftermath a && !a.isExpired() && a.isActivated()) {
					EntityReference hr = a.getHostReference();
					if (hr != null && hr.getId() == hostId) {
						matchCount++;
						if (baseCardDeathrattle != null && a.getSpell() == baseCardDeathrattle) {
							hasBaseInTriggers = true;
						}
					}
				}
			}
		}
		boolean includeBase = baseCardDeathrattle != null && !hasBaseInTriggers;
		if (matchCount == 0) {
			if (includeBase) {
				Card card = (Card) host;
				return Collections.singletonList(new Aftermath(baseCardDeathrattle, card, card));
			}
			return Collections.emptyList();
		}
		List<Aftermath> result = new ArrayList<>(matchCount + (includeBase ? 1 : 0));
		if (includeBase) {
			Card card = (Card) host;
			result.add(new Aftermath(baseCardDeathrattle, card, card));
		}
		for (int i = 0; i < n; i++) {
			Trigger t = trigs.elements[i];
			if (t instanceof Aftermath a && !a.isExpired() && a.isActivated()) {
				EntityReference hr = a.getHostReference();
				if (hr != null && hr.getId() == hostId) {
					result.add((Aftermath) trigs.getUnshared(i));
				}
			}
		}
		return result;
	}

	public static List<Opener> getOpenersFast(GameContext context, Entity host) {
		if (host == null) {
			return Collections.emptyList();
		}
		Card sourceCard = host.getSourceCard();
		OpenerDesc baseOpener = (!host.hasAttribute(Attribute.SILENCED) && sourceCard != null)
				? sourceCard.getDesc().getBattlecry()
				: null;
		GameStateArena arena = getContextArena(context);
		TriggerList trigs = arena.triggers;
		int n = trigs.size;
		int hostId = host.getId();
		int matchCount = 0;
		boolean hasBaseInTriggers = false;
		if (n > 0 && (trigs.kindMask & KIND_OPENER) != 0 && hostId != IdFactory.UNASSIGNED && arena.hasHostBit(hostId)) {
			Trigger[] raw = trigs.elements;
			for (int i = 0; i < n; i++) {
				Trigger t = raw[i];
				if (t instanceof Opener o && !o.isExpired() && o.isActivated()) {
					EntityReference hr = o.getHostReference();
					if (hr != null && hr.getId() == hostId) {
						matchCount++;
						if (baseOpener != null && o.getOpenerDesc() == baseOpener) {
							hasBaseInTriggers = true;
						}
					}
				}
			}
		}
		boolean includeBase = baseOpener != null && !hasBaseInTriggers;
		if (matchCount == 0) {
			if (includeBase) {
				return Collections.singletonList(new Opener(baseOpener, sourceCard, host));
			}
			return Collections.emptyList();
		}
		List<Opener> result = new ArrayList<>(matchCount + (includeBase ? 1 : 0));
		if (includeBase) {
			result.add(new Opener(baseOpener, sourceCard, host));
		}
		for (int i = 0; i < n; i++) {
			Trigger t = trigs.elements[i];
			if (t instanceof Opener o && !o.isExpired() && o.isActivated()) {
				EntityReference hr = o.getHostReference();
				if (hr != null && hr.getId() == hostId) {
					result.add((Opener) trigs.getUnshared(i));
				}
			}
		}
		return result;
	}

	public static void removeEnchantmentsFast(GameContext context, Entity entity, Predicate<Trigger> predicate) {
		if (entity.getEntityType() == EntityType.CARD) {
			AttributeMap attrs = entity.getAttributes();
			for (Attribute attr : Attribute.getCardEnchantmentAttributes()) {
				attrs.remove(attr);
			}
		}
		GameStateArena arena = getContextArena(context);
		TriggerList trigs = arena.triggers;
		int n = trigs.size;
		if (n == 0) {
			return;
		}
		int hostId = entity.getId();
		if (!arena.hasHostBit(hostId)) {
			return;
		}
		for (int i = 0; i < trigs.size; i++) {
			Trigger t = trigs.elements[i];
			if (!t.isExpired()) {
				EntityReference hr = t.getHostReference();
				if (hr != null && hr.getId() == hostId) {
					t = trigs.getUnshared(i);
					if (predicate.test(t)) {
						if (t instanceof Enchantment ench) {
							ench.onRemovedBySilence(context, entity);
							ench.clearRevertableModification();
						}
						t.expire(context);
					}
				}
			}
		}
	}

	public static List<Enchantment> copyEnchantmentsFast(
			GameContext context,
			Player player,
			Entity effectSource,
			Entity source,
			Entity target,
			Predicate<Enchantment> predicate,
			boolean includeExpired) {
		AttributeMap srcAttrs = source.getAttributes();
		AttributeMap dstAttrs = target.getAttributes();
		for (Attribute a : Attribute.getEnchantmentLikeAttributes()) {
			if (srcAttrs.containsKey(a)) {
				dstAttrs.put(a, srcAttrs.get(a));
			}
		}

		GameStateArena arena = getContextArena(context);
		if (arena.envExtras != null && arena.envExtras.containsKey(Environment.ENTITY_LIST)) {
			EnvironmentEntityList list = EnvironmentEntityList.getList(context);
			for (EntityReference reference : list.getReferences(source)) {
				if (!reference.equals(EntityReference.NONE)) {
					list.add(target, context.resolveSingleTarget(reference));
				}
			}
		}

		TriggerList trigs = arena.triggers;
		int n = trigs.size;
		if (n == 0) {
			return Collections.emptyList();
		}
		int srcId = source.getId();
		if (!arena.hasHostBit(srcId)) {
			return Collections.emptyList();
		}
		Trigger[] raw = trigs.elements;
		List<Enchantment> copies = null;
		for (int i = 0; i < n; i++) {
			Trigger t = raw[i];
			if (t instanceof Enchantment ench && (includeExpired || !ench.isExpired())) {
				EntityReference hr = ench.getHostReference();
				if (hr != null && hr.getId() == srcId && (predicate == null || predicate.test(ench))) {
					if (copies == null) {
						copies = new ArrayList<>(2);
					}
					copies.add(ench.getCopy());
				}
			}
		}
		if (copies == null) {
			return Collections.emptyList();
		}
		GameLogic logic = context.getLogic();
		for (int i = 0, cSize = copies.size(); i < cSize; i++) {
			logic.addEnchantment(player, copies.get(i), effectSource, target);
		}
		return copies;
	}

	public static int getTotalAttributeValueFast(GameContext context, Player player, Attribute attr) {
		int value = player.getAttributeValue(attr);
		int pid = player.getId();
		GameStateArena arena = ((EntityLookupTemplate) (Object) player.getLookup()).arena;
		for (Zones z : IN_PLAY_NON_ENCHANTMENT_ZONES) {
			if (z == Zones.PLAYER) {
				continue;
			}
			int s = slot(pid, z);
			int sz = arena.zoneSizes[s];
			Entity[] arr = arena.zoneArrays[s];
			for (int i = 0; i < sz; i++) {
				value += arr[i].getAttributeValue(attr);
			}
		}
		if (context != null) {
			TriggerList trigs = getContextArena(context).triggers;
			int n = trigs.size;
			Trigger[] raw = trigs.elements;
			for (int i = 0; i < n; i++) {
				Trigger t = raw[i];
				if (t instanceof Enchantment ench && ench.getOwner() == pid) {
					value += ench.getAttributeValue(attr);
				}
			}
		}
		return value;
	}

	public static void resetEnchantmentSequenceCounters(GameContext context) {
		GameStateArena arena = getContextArena(context);
		if (arena.envExtras != null && arena.envExtras.containsKey(Environment.DESTROYED_THIS_SEQUENCE_COUNT)) {
			arena.envMap.put(Environment.DESTROYED_THIS_SEQUENCE_COUNT, 0);
		}
		TriggerList trigs = arena.triggers;
		for (int i = 0, n = trigs.size; i < n; i++) {
			if (trigs.elements[i] instanceof Enchantment ench && ench.getFiresThisSequence() != 0) {
				((Enchantment) trigs.getUnshared(i)).endOfSequence();
			}
		}
	}

	public static void checkTriggersValid(GameContext context, org.slf4j.Logger logger) {
		TriggerList trigs = getContextArena(context).triggers;
		int n = trigs.size;
		Trigger[] raw = trigs.elements;
		for (int i = 0; i < n; i++) {
			Trigger trigger = raw[i];
			if (trigger instanceof HasCard hc && hc.getSourceCard() == null) {
				logger.error("endOfSequence: Trigger {} is missing source card", trigger, new RuntimeException());
			}
			EntityReference hr = trigger.getHostReference();
			if (hr == null || hr.equals(EntityReference.NONE)) {
				logger.error("endOfSequence: Trigger has host reference of NONE on an enchantment from card {}", trigger,
						new RuntimeException());
			}
			if (trigger instanceof Enchantment ench && ench.getId() == IdFactory.UNASSIGNED) {
				logger.error("endOfSequence: Trigger {} is missing an ID", trigger);
			}
		}
	}

	public static List<Actor> getDestroyedCharactersFast(GameContext context) {
		GameStateArena arena = getContextArena(context);
		List<Actor> destroyList = null;
		for (int p = 0; p < 2; p++) {
			Player player = p == 0 ? context.getPlayer1() : context.getPlayer2();
			int heroSlot = slot(p, Zones.HERO);
			if (arena.zoneSizes[heroSlot] > 0) {
				Actor hero = (Actor) arena.zoneArrays[heroSlot][0];
				if ((hero.isDestroyed() || player.hasAttribute(Attribute.DESTROYED))
						&& hero.getZone() != Zones.GRAVEYARD) {
					if (destroyList == null) {
						destroyList = new ArrayList<>(2);
					}
					destroyList.add((Actor) arena.zoneGet(heroSlot, p, Zones.HERO, 0));
				}
			} else {
				Actor hero = player.getHero();
				if (hero != null && (hero.isDestroyed() || player.hasAttribute(Attribute.DESTROYED))
						&& hero.getZone() != Zones.GRAVEYARD) {
					if (destroyList == null) {
						destroyList = new ArrayList<>(2);
					}
					destroyList.add(hero);
				}
			}

			int bfSlot = slot(p, Zones.BATTLEFIELD);
			int bfSize = arena.zoneSizes[bfSlot];
			Entity[] bfArr = arena.zoneArrays[bfSlot];
			for (int i = 0; i < bfSize; i++) {
				Actor minion = (Actor) bfArr[i];
				if (minion.isDestroyed()) {
					if (destroyList == null) {
						destroyList = new ArrayList<>(4);
					}
					destroyList.add((Actor) arena.zoneGet(bfSlot, p, Zones.BATTLEFIELD, i));
				}
			}

			int saSlot = slot(p, Zones.SET_ASIDE_ZONE);
			int saSize = arena.zoneSizes[saSlot];
			if (saSize > 0) {
				Entity[] saArr = arena.zoneArrays[saSlot];
				for (int i = 0; i < saSize; i++) {
					Entity entity = saArr[i];
					if (entity instanceof Actor actor && actor.isDestroyed()) {
						if (destroyList == null) {
							destroyList = new ArrayList<>(2);
						}
						destroyList.add((Actor) arena.zoneGet(saSlot, p, Zones.SET_ASIDE_ZONE, i));
					}
				}
			}

			int wpSlot = slot(p, Zones.WEAPON);
			if (arena.zoneSizes[wpSlot] > 0) {
				Actor weapon = (Actor) arena.zoneArrays[wpSlot][0];
				if (weapon.isDestroyed()) {
					if (destroyList == null) {
						destroyList = new ArrayList<>(2);
					}
					destroyList.add((Actor) arena.zoneGet(wpSlot, p, Zones.WEAPON, 0));
				}
			}
		}
		return destroyList != null ? destroyList : Collections.emptyList();
	}

	private static final SpellDesc[] EMPTY_SPELL_DESC_ARRAY = new SpellDesc[0];

	public static <T extends Aura> boolean hasAuraFast(GameContext context, int playerId, Class<T> auraClass) {
		TriggerList trigs = getContextArena(context).triggers;
		int n = trigs.size;
		if (n == 0 || (trigs.kindMask & KIND_AURA) == 0) {
			return false;
		}
		Trigger[] raw = trigs.elements;
		for (int i = 0; i < n; i++) {
			Trigger t = raw[i];
			if (t.getOwner() == playerId && !t.isExpired() && auraClass.isInstance(t) && t.isActivated()) {
				Entity host = context.tryFind(t.getHostReference());
				if (host != null && host.getOwner() == playerId && host.isInPlay()) {
					return true;
				}
			}
		}
		return false;
	}

	public static <T extends Aura> List<T> getAurasByPlayerFast(GameContext context, int playerId, Class<T> auraClass) {
		TriggerList trigs = getContextArena(context).triggers;
		int n = trigs.size;
		if (n == 0 || (trigs.kindMask & KIND_AURA) == 0) {
			return Collections.emptyList();
		}
		Trigger[] raw = trigs.elements;
		int matchCount = 0;
		for (int i = 0; i < n; i++) {
			Trigger e = raw[i];
			if (e.getOwner() == playerId && !e.isExpired() && auraClass.isInstance(e) && e.isActivated()) {
				matchCount++;
			}
		}
		if (matchCount == 0) {
			return Collections.emptyList();
		}
		List<T> result = new ArrayList<>(matchCount);
		for (int i = 0; i < n; i++) {
			Trigger e = trigs.elements[i];
			if (e.getOwner() == playerId && !e.isExpired() && auraClass.isInstance(e) && e.isActivated()) {
				result.add(auraClass.cast(trigs.getUnshared(i)));
			}
		}
		if (result.size() > 1) {
			result.sort(java.util.Comparator.comparingInt(Entity::getId));
		}
		return result;
	}

	public static <T extends Aura> List<T> getAurasByTargetFast(GameContext context, Class<T> auraClass, Entity target) {
		if (target == null) {
			return Collections.emptyList();
		}
		TriggerList trigs = getContextArena(context).triggers;
		int n = trigs.size;
		if (n == 0 || (trigs.kindMask & KIND_AURA) == 0) {
			return Collections.emptyList();
		}
		if (auraClass == net.demilich.metastone.game.spells.aura.ModifyBuffSpellAura.class
				&& (trigs.kindMask & KIND_MODIFY_BUFF_AURA) == 0) {
			return Collections.emptyList();
		}
		int targetId = target.getId();
		Card sourceCard = target.getSourceCard();
		int sourceCardId = sourceCard != null ? sourceCard.getId() : IdFactory.UNASSIGNED;
		Trigger[] raw = trigs.elements;
		int matchCount = 0;
		for (int i = 0; i < n; i++) {
			Trigger e = raw[i];
			if (auraClass.isInstance(e) && !e.isExpired() && e.isActivated()) {
				Aura aura = (Aura) e;
				var affected = aura.getAffectedEntities();
				if (affected.contains(targetId) || (sourceCardId != IdFactory.UNASSIGNED && affected.contains(sourceCardId))) {
					matchCount++;
				}
			}
		}
		if (matchCount == 0) {
			return Collections.emptyList();
		}
		List<T> result = new ArrayList<>(matchCount);
		for (int i = 0; i < n; i++) {
			Trigger e = trigs.elements[i];
			if (auraClass.isInstance(e) && !e.isExpired() && e.isActivated()) {
				Aura aura = (Aura) e;
				var affected = aura.getAffectedEntities();
				if (affected.contains(targetId) || (sourceCardId != IdFactory.UNASSIGNED && affected.contains(sourceCardId))) {
					result.add(auraClass.cast(trigs.getUnshared(i)));
				}
			}
		}
		return result;
	}

	public static SpellDesc[] getBonusesFromAuraFast(
			GameContext context,
			int playerId,
			Class<? extends Aura> auraClass,
			Entity source,
			Entity target) {
		TriggerList trigs = getContextArena(context).triggers;
		int n = trigs.size;
		if (n == 0 || (trigs.kindMask & KIND_AURA) == 0) {
			return EMPTY_SPELL_DESC_ARRAY;
		}
		Trigger[] raw = trigs.elements;
		List<SpellDesc> result = null;
		Player player = null;
		for (int i = 0; i < n; i++) {
			Trigger t = raw[i];
			if (t.getOwner() == playerId && !t.isExpired() && auraClass.isInstance(t) && t.isActivated()) {
				Entity host = context.tryFind(t.getHostReference());
				if (host != null && host.getOwner() == playerId && host.isInPlay()) {
					Aura aura = (Aura) t;
					if (player == null) {
						player = context.getPlayer(playerId);
					}
					if (aura.getCondition() == null || aura.getCondition().isFulfilled(context, player, source, target)) {
						if (result == null) {
							result = new ArrayList<>(2);
						}
						result.add(aura.getDesc().getApplyEffect());
					}
				}
			}
		}
		return result == null ? EMPTY_SPELL_DESC_ARRAY : result.toArray(EMPTY_SPELL_DESC_ARRAY);
	}

	public static boolean hasTurnTriggers(GameContext context) {
		TriggerList trigs = getContextArena(context).triggers;
		return trigs.size > 0 && (trigs.kindMask & KIND_TURN_TRIGGER) != 0;
	}

	public static boolean hasAnyInterestedTrigger(GameContext context, GameEventType eventType, boolean isTurnEnd) {
		TriggerList trigs = getContextArena(context).triggers;
		int n = trigs.size;
		if (n == 0 || (trigs.kindMask & KIND_EVENT_LISTENER) == 0) {
			return false;
		}
		Trigger[] raw = trigs.elements;
		for (int i = 0; i < n; i++) {
			Trigger t = raw[i];
			if (!t.isExpired() && ((isTurnEnd && t.oneTurnOnly()) || t.interestedIn(eventType))) {
				return true;
			}
		}
		return false;
	}

	public static List<Trigger> getInterestedTriggersSnapshot(GameContext context, GameEventType eventType, boolean isTurnEnd) {
		GameStateArena arena = getContextArena(context);
		TriggerList trigs = arena.triggers;
		int n = trigs.size;
		if (n == 0 || (trigs.kindMask & KIND_EVENT_LISTENER) == 0) {
			return null;
		}
		Trigger[] raw = trigs.elements;
		int matchCount = 0;
		for (int i = 0; i < n; i++) {
			Trigger t = raw[i];
			if (!t.isExpired() && ((isTurnEnd && t.oneTurnOnly()) || t.interestedIn(eventType))) {
				matchCount++;
			}
		}
		if (matchCount == 0) {
			return null;
		}
		List<Trigger> snapshot = new ArrayList<>(matchCount);
		for (int i = 0; i < n; i++) {
			Trigger t = trigs.elements[i];
			if (!t.isExpired() && ((isTurnEnd && t.oneTurnOnly()) || t.interestedIn(eventType))) {
				snapshot.add(trigs.getUnshared(i));
			}
		}
		return snapshot;
	}

	public static int getModifiedManaCostFast(GameContext context, Player player, Card card) {
		int manaCost = card.getBaseManaCost();
		if (card.getEntityLocation().equals(EntityLocation.UNASSIGNED)) {
			return manaCost - card.getManaCostModification(context, player);
		}
		int minValue = 0;
		TriggerList trigs = getContextArena(context).triggers;
		int n = trigs.size;
		if (n > 0 && (trigs.kindMask & KIND_CARD_COST_MODIFIER) != 0) {
			for (int i = 0; i < n; i++) {
				Trigger trigger = trigs.elements[i];
				if (!(trigger instanceof net.demilich.metastone.game.cards.costmodifier.CardCostModifier costModifier)) {
					continue;
				}
				if (!costModifier.appliesTo(context, card, player)) {
					continue;
				}
				costModifier = (net.demilich.metastone.game.cards.costmodifier.CardCostModifier) trigs.getUnshared(i);
				Entity host = context.resolveSingleTarget(costModifier.getHostReference());
				manaCost = costModifier.process(context, host, card, manaCost, player);
				if (costModifier.getMinValue() > minValue) {
					minValue = costModifier.getMinValue();
				}
			}
		}
		manaCost -= card.getManaCostModification(context, player);
		return net.demilich.metastone.game.utils.MathUtils.clamp(manaCost, minValue, Integer.MAX_VALUE);
	}

	public static void copyCardEnchantmentsToActorFast(
			GameContext context, Player player, Entity effectSource, Actor actor) {
		Card sourceCard = actor.getSourceCard();
		if (sourceCard == null) {
			return;
		}
		int srcCardId = sourceCard.getId();
		if (srcCardId == IdFactory.UNASSIGNED) {
			return;
		}
		GameStateArena arena = getContextArena(context);
		TriggerList trigs = arena.triggers;
		int n = trigs.size;
		if (n == 0 || !arena.hasHostBit(srcCardId)) {
			return;
		}
		Trigger[] raw = trigs.elements;
		List<Enchantment> copies = null;
		for (int i = 0; i < n; i++) {
			Trigger t = raw[i];
			if (t instanceof Enchantment ench && !ench.isExpired() && ench.isCopyToActor()) {
				EntityReference hr = ench.getHostReference();
				if (hr != null && hr.getId() == srcCardId) {
					if (copies == null) {
						copies = new ArrayList<>(2);
					}
					copies.add(ench.getCopy());
				}
			}
		}
		if (copies != null) {
			GameLogic logic = context.getLogic();
			for (int i = 0, cSize = copies.size(); i < cSize; i++) {
				logic.addEnchantment(player, copies.get(i), effectSource, actor);
			}
		}
	}

	public static List<net.demilich.metastone.game.spells.aura.PhysicalAttackTargetOverrideAura> getPhysicalAttackTargetOverrideAurasFast(
			GameContext context, Actor minion) {
		GameStateArena arena = getContextArena(context);
		TriggerList trigs = arena.triggers;
		int n = trigs.size;
		if (n == 0 || (trigs.kindMask & KIND_PHYSICAL_ATTACK_OVERRIDE_AURA) == 0) {
			return null;
		}
		int minionId = minion.getId();
		if (!arena.hasHostBit(minionId)) {
			return null;
		}
		List<net.demilich.metastone.game.spells.aura.PhysicalAttackTargetOverrideAura> filters = null;
		for (int i = 0; i < n; i++) {
			Trigger trigger = trigs.elements[i];
			if (trigger instanceof net.demilich.metastone.game.spells.aura.PhysicalAttackTargetOverrideAura
					&& !trigger.isExpired()) {
				EntityReference hr = trigger.getHostReference();
				if (hr != null && hr.getId() == minionId) {
					if (filters == null) {
						filters = new ArrayList<>(2);
					}
					filters.add((net.demilich.metastone.game.spells.aura.PhysicalAttackTargetOverrideAura) trigs.getUnshared(i));
				}
			}
		}
		return filters;
	}

	public static List<Aura> getSpellOverrideAurasFast(GameContext context, int playerId, Class<?> spellClass) {
		TriggerList trigs = getContextArena(context).triggers;
		int n = trigs.size;
		if (n == 0 || (trigs.kindMask & KIND_SPELL_OVERRIDE_OR_INCORRUPT_AURA) == 0) {
			return null;
		}
		List<Aura> overrideAuras = null;
		for (int i = 0; i < n; i++) {
			Trigger t = trigs.elements[i];
			if (t instanceof net.demilich.metastone.game.spells.aura.SpellOverrideAura aura
					&& !aura.isExpired()
					&& aura.isActivated()
					&& aura.getDesc().getRemoveEffect().get(SpellArg.CLASS).equals(spellClass)
					&& aura.getAffectedEntities().contains(playerId)) {
				if (overrideAuras == null) {
					overrideAuras = new ArrayList<>(2);
				}
				overrideAuras.add((Aura) trigs.getUnshared(i));
			}
		}
		return overrideAuras;
	}

	public static void collectAdditionalOpenersFast(GameContext context, Actor actor, List<OpenerDesc> openers) {
		GameStateArena arena = getContextArena(context);
		TriggerList trigs = arena.triggers;
		int n = trigs.size;
		if (n == 0 || (trigs.kindMask & KIND_OPENER) == 0) {
			return;
		}
		Card sourceCard = actor.getSourceCard();
		int srcCardId = sourceCard != null ? sourceCard.getId() : IdFactory.UNASSIGNED;
		if (srcCardId != IdFactory.UNASSIGNED && arena.hasHostBit(srcCardId)) {
			Trigger[] raw = trigs.elements;
			for (int i = 0; i < n; i++) {
				Trigger t = raw[i];
				if (t instanceof Opener o && !o.isExpired() && o.isActivated()) {
					EntityReference hr = o.getHostReference();
					if (hr != null && hr.getId() == srcCardId) {
						openers.add(o.getOpenerDesc());
					}
				}
			}
		}
		if (!arena.isStackEmpty(STACK_SUMMON_REF)) {
			int lastSummonId = arena.stackData[STACK_SUMMON_REF * arena.stackCapacity];
			int actorId = actor.getId();
			if (lastSummonId == actorId && arena.hasHostBit(actorId)) {
				Trigger[] raw = trigs.elements;
				for (int i = 0; i < n; i++) {
					Trigger t = raw[i];
					if (t instanceof Opener o && !o.isExpired() && o.isActivated()) {
						EntityReference hr = o.getHostReference();
						if (hr != null && hr.getId() == actorId && !Objects.equals(o.getSourceCard(), sourceCard)) {
							openers.add(o.getOpenerDesc());
						}
					}
				}
			}
		}
	}

	public static void pushEventDataFast(GameContext context, GameEvent event) {
		GameStateArena arena = getContextArena(context);
		int val = (event instanceof HasValue hv) ? hv.getValue() : 0;
		arena.pushInt(STACK_EVENT_VALUE, val);
		Entity tgt = event.getTarget();
		arena.pushInt(STACK_EVENT_TARGET_REF, tgt != null ? tgt.getId() : IdFactory.UNASSIGNED);
		Entity src = event.getSource();
		arena.pushInt(STACK_EVENT_SOURCE_REF, src != null ? src.getId() : IdFactory.UNASSIGNED);
	}

	public static void popEventDataFast(GameContext context) {
		GameStateArena arena = getContextArena(context);
		arena.popInt(STACK_EVENT_VALUE);
		arena.popInt(STACK_EVENT_SOURCE_REF);
		arena.popInt(STACK_EVENT_TARGET_REF);
	}

	public static void pushHostReferenceFast(GameContext context, Trigger trigger) {
		GameStateArena arena = getContextArena(context);
		EntityReference hr = trigger.getHostReference();
		arena.pushInt(STACK_TRIGGER_HOST_REF, hr != null ? hr.getId() : IdFactory.UNASSIGNED);
	}

	public static void popHostReferenceFast(GameContext context) {
		GameStateArena arena = getContextArena(context);
		arena.popInt(STACK_TRIGGER_HOST_REF);
	}

	public static int countActiveTriggers(GameStateArena arena, int hostId) {
		TriggerList trigs = arena.triggers;
		int n = trigs.size;
		if (n == 0 || !arena.hasHostBit(hostId)) {
			return 0;
		}
		Trigger[] raw = trigs.elements;
		int count = 0;
		for (int i = 0; i < n; i++) {
			Trigger t = raw[i];
			if (!t.isExpired() && t.isActivated()) {
				EntityReference hr = t.getHostReference();
				if (hr != null && hr.getId() == hostId) {
					count++;
				}
			}
		}
		return count;
	}

	private static int mergeHashes(int hash1, int hash2) {
		int hash = 0;
		hash = hash * 33 ^ hash1;
		hash = hash * 33 ^ hash2;
		return hash;
	}

	private static int hashEntityReadOnly(Entity entity) {
		int hash = entity.getName().hashCode();
		AttributeMap attrs = entity.getAttributes();
		for (Attribute tag : attrs.unsafeKeySet()) {
			Object value = attrs.get(tag);
			if (!(value instanceof Integer)) {
				continue;
			}
			hash = mergeHashes(hash, entity.getAttribute(tag).hashCode());
		}
		return hash;
	}

	public static int hashGameContext(GameContext context) {
		GameStateArena arena = getContextArena(context);
		int hash = context.getActivePlayerId();
		for (int p = 0; p < 2; p++) {
			Player player = p == 0 ? context.getPlayer1() : context.getPlayer2();
			hash = mergeHashes(hash, player.getMana());
			hash = mergeHashes(hash, player.getMaxMana());
			int heroSlot = slot(p, Zones.HERO);
			Entity hero = arena.zoneSizes[heroSlot] > 0 ? arena.zoneArrays[heroSlot][0] : player.getHero();
			hash = mergeHashes(hash, hashEntityReadOnly(hero));

			int handSlot = slot(p, Zones.HAND);
			int handSize = arena.zoneSizes[handSlot];
			Entity[] handArr = arena.zoneArrays[handSlot];
			for (int i = 0; i < handSize; i++) {
				hash = mergeHashes(hash, hashEntityReadOnly(handArr[i]));
			}

			int deckSlot = slot(p, Zones.DECK);
			int deckSize = arena.zoneSizes[deckSlot];
			Entity[] deckArr = arena.zoneArrays[deckSlot];
			for (int i = 0; i < deckSize; i++) {
				hash = mergeHashes(hash, hashEntityReadOnly(deckArr[i]));
			}

			int bfSlot = slot(p, Zones.BATTLEFIELD);
			int bfSize = arena.zoneSizes[bfSlot];
			Entity[] bfArr = arena.zoneArrays[bfSlot];
			for (int i = 0; i < bfSize; i++) {
				Entity minion = bfArr[i];
				hash = mergeHashes(hash, hashEntityReadOnly(minion));
				hash = mergeHashes(hash, countActiveTriggers(arena, minion.getId()));
			}
		}
		return hash;
	}

	// =========================================================================
	// Auto-Synthesized Inline Enchantment CardDescs
	// =========================================================================

	public static boolean isSynthesizedEnchantmentCardId(String cardId) {
		return cardId != null && cardId.contains(SYNTHESIZED_ENCHANTMENT_INFIX);
	}

	public static void synthesizeEnchantmentCardsForCard(CardDesc cardDesc, Map<String, Card> catalogueCards) {
		DescBytecodeCompiler.drainPendingPrewarmIfNeeded();
		if (cardDesc == null || cardDesc.getId() == null || cardDesc.getType() == CardType.ENCHANTMENT) {
			return;
		}
		String baseId = cardDesc.getId();
		if (isSynthesizedEnchantmentCardId(baseId)) {
			return;
		}
		int[] counter = new int[]{0};
		if (cardDesc.getSpell() != null) {
			scanSpellForEnchantments(cardDesc, cardDesc.getSpell(), catalogueCards, counter);
		}
		if (cardDesc.getBattlecry() != null && cardDesc.getBattlecry().getSpell() != null) {
			scanSpellForEnchantments(cardDesc, cardDesc.getBattlecry().getSpell(), catalogueCards, counter);
		}
		if (cardDesc.getChooseOneBattlecries() != null) {
			for (var bc : cardDesc.getChooseOneBattlecries()) {
				if (bc != null && bc.getSpell() != null) {
					scanSpellForEnchantments(cardDesc, bc.getSpell(), catalogueCards, counter);
				}
			}
		}
		if (cardDesc.getChooseBothBattlecry() != null && cardDesc.getChooseBothBattlecry().getSpell() != null) {
			scanSpellForEnchantments(cardDesc, cardDesc.getChooseBothBattlecry().getSpell(), catalogueCards, counter);
		}
		if (cardDesc.getDeathrattle() != null) {
			scanSpellForEnchantments(cardDesc, cardDesc.getDeathrattle(), catalogueCards, counter);
		}
		if (cardDesc.getTrigger() != null && cardDesc.getTrigger().getSpell() != null) {
			scanSpellForEnchantments(cardDesc, cardDesc.getTrigger().getSpell(), catalogueCards, counter);
		}
		if (cardDesc.getTriggers() != null) {
			for (var t : cardDesc.getTriggers()) {
				if (t != null && t.getSpell() != null) {
					scanSpellForEnchantments(cardDesc, t.getSpell(), catalogueCards, counter);
				}
			}
		}
		if (cardDesc.getPassiveTrigger() != null && cardDesc.getPassiveTrigger().getSpell() != null) {
			scanSpellForEnchantments(cardDesc, cardDesc.getPassiveTrigger().getSpell(), catalogueCards, counter);
		}
		if (cardDesc.getPassiveTriggers() != null) {
			for (var t : cardDesc.getPassiveTriggers()) {
				if (t != null && t.getSpell() != null) {
					scanSpellForEnchantments(cardDesc, t.getSpell(), catalogueCards, counter);
				}
			}
		}
		if (cardDesc.getDeckTrigger() != null && cardDesc.getDeckTrigger().getSpell() != null) {
			scanSpellForEnchantments(cardDesc, cardDesc.getDeckTrigger().getSpell(), catalogueCards, counter);
		}
		if (cardDesc.getDeckTriggers() != null) {
			for (var t : cardDesc.getDeckTriggers()) {
				if (t != null && t.getSpell() != null) {
					scanSpellForEnchantments(cardDesc, t.getSpell(), catalogueCards, counter);
				}
			}
		}
		if (cardDesc.getGameTriggers() != null) {
			for (var t : cardDesc.getGameTriggers()) {
				if (t != null && t.getSpell() != null) {
					scanSpellForEnchantments(cardDesc, t.getSpell(), catalogueCards, counter);
				}
			}
		}
		if (cardDesc.getOnEquip() != null) {
			scanSpellForEnchantments(cardDesc, cardDesc.getOnEquip(), catalogueCards, counter);
		}
		if (cardDesc.getOnUnequip() != null) {
			scanSpellForEnchantments(cardDesc, cardDesc.getOnUnequip(), catalogueCards, counter);
		}
		if (cardDesc.getGroup() != null) {
			for (SpellDesc subSpell : cardDesc.getGroup()) {
				if (subSpell != null) {
					scanSpellForEnchantments(cardDesc, subSpell, catalogueCards, counter);
				}
			}
		}
	}

	private static void scanSpellForEnchantments(
			CardDesc ownerCard,
			SpellDesc spell,
			Map<String, Card> catalogueCards,
			int[] counter) {
		if (spell == null) {
			return;
		}
		Class<?> spellClass = spell.getDescClass();
		if (spellClass != null) {
			if (spellClass == BuffSpell.class
					|| spellClass == TemporaryAttackSpell.class
					|| spellClass == SetAttackSpell.class
					|| spellClass == SetHpSpell.class
					|| spellClass == AddAttributeSpell.class
					|| spellClass == RemoveAttributeSpell.class
					|| spellClass == ModifyAttributeSpell.class
					|| spellClass == SetCardSpell.class
					|| spellClass == AddDeathrattleSpell.class
					|| (spellClass == AddEnchantmentSpell.class && !spell.containsKey(SpellArg.CARD))) {
				int idx = counter[0]++;
				String enchId = ownerCard.getId() + SYNTHESIZED_ENCHANTMENT_INFIX + idx;
				Card enchCard = catalogueCards.get(enchId);
				if (enchCard == null) {
					CardDesc enchDesc = new CardDesc();
					enchDesc.setId(enchId);
					enchDesc.setType(CardType.ENCHANTMENT);
					enchDesc.setName(resolveInlineEnchantmentName(ownerCard, spell, spellClass, enchId));
					enchDesc.setDescription(describeInlineEnchantment(ownerCard, spell, spellClass));
					enchDesc.setHeroClass(ownerCard.getHeroClass() != null ? ownerCard.getHeroClass() : HeroClass.ANY);
					enchDesc.setRarity(Rarity.FREE);
					enchDesc.setSet(ownerCard.getSet() != null ? ownerCard.getSet() : "CUSTOM");
					enchDesc.setCollectible(false);
					enchDesc.setFileFormatVersion(ownerCard.getFileFormatVersion());
					var ownerArt = ownerCard.getArt();
					if (ownerArt == null && ownerCard.getName() != null && !ownerCard.getName().isEmpty()) {
						ownerArt = com.hiddenswitch.spellsource.rpc.Spellsource.Art.newBuilder()
								.setSprite(com.hiddenswitch.spellsource.rpc.Spellsource.Sprite.newBuilder()
										.setNamed(ownerCard.getName()))
								.build();
					}
					enchDesc.setArt(ownerArt);
					if (spellClass != AddEnchantmentSpell.class) {
						enchDesc.setSpell(spell);
					}
					enchDesc.freeze();
					enchCard = enchDesc.create();
					enchCard.freeze();
					catalogueCards.put(enchId, enchCard);
				}
				spell.setSynthesizedEnchantmentCard(enchCard);
			}
		}
		Object triggerObj = spell.get(SpellArg.TRIGGER);
		if (triggerObj instanceof net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc enchDesc
				&& enchDesc.getSpell() != null) {
			scanSpellForEnchantments(ownerCard, enchDesc.getSpell(), catalogueCards, counter);
		}
		Object triggersObj = spell.get(SpellArg.TRIGGERS);
		if (triggersObj instanceof net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc[] enchDescs) {
			for (var ed : enchDescs) {
				if (ed != null && ed.getSpell() != null) {
					scanSpellForEnchantments(ownerCard, ed.getSpell(), catalogueCards, counter);
				}
			}
		}
		SpellDesc subSpell = (SpellDesc) spell.get(SpellArg.SPELL);
		if (subSpell != null) {
			scanSpellForEnchantments(ownerCard, subSpell, catalogueCards, counter);
		}
		SpellDesc spell1 = (SpellDesc) spell.get(SpellArg.SPELL1);
		if (spell1 != null) {
			scanSpellForEnchantments(ownerCard, spell1, catalogueCards, counter);
		}
		SpellDesc spell2 = (SpellDesc) spell.get(SpellArg.SPELL2);
		if (spell2 != null) {
			scanSpellForEnchantments(ownerCard, spell2, catalogueCards, counter);
		}
		SpellDesc[] subSpells = (SpellDesc[]) spell.get(SpellArg.SPELLS);
		if (subSpells != null) {
			for (SpellDesc s : subSpells) {
				scanSpellForEnchantments(ownerCard, s, catalogueCards, counter);
			}
		}
	}

	private static String resolveInlineEnchantmentName(
			CardDesc ownerCard,
			SpellDesc spell,
			Class<?> spellClass,
			String fallbackId) {
		String explicitName = spell.getString(SpellArg.NAME);
		if (explicitName != null && !explicitName.isEmpty()) {
			return explicitName;
		}
		if (spellClass == AddEnchantmentSpell.class) {
			Object triggerObj = spell.get(SpellArg.TRIGGER);
			if (triggerObj instanceof net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc enchDesc
					&& enchDesc.getName() != null
					&& !enchDesc.getName().isEmpty()) {
				return enchDesc.getName();
			}
		} else if (spellClass == AddDeathrattleSpell.class) {
			SpellDesc subSpell = (SpellDesc) spell.get(SpellArg.SPELL);
			if (subSpell != null) {
				String subName = subSpell.getString(SpellArg.NAME);
				if (subName != null && !subName.isEmpty()) {
					return subName;
				}
			}
		}
		return ownerCard.getName() != null ? ownerCard.getName() : fallbackId;
	}

	private static String describeInlineEnchantment(CardDesc ownerCard, SpellDesc spell, Class<?> spellClass) {
		String explicitDesc = spell.getString(SpellArg.DESCRIPTION);
		if (explicitDesc != null && !explicitDesc.isEmpty()) {
			return explicitDesc;
		}
		if (spellClass == BuffSpell.class) {
			Object v = spell.get(SpellArg.VALUE);
			Object atk = spell.get(SpellArg.ATTACK_BONUS);
			Object hp = spell.get(SpellArg.HP_BONUS);
			Object arm = spell.get(SpellArg.ARMOR_BONUS);
			if (v instanceof Integer iv && iv != 0) {
				return (iv >= 0 ? "+" : "") + iv + "/" + (iv >= 0 ? "+" : "") + iv + ".";
			}
			int a = atk instanceof Integer ia ? ia : 0;
			int h = hp instanceof Integer ih ? ih : 0;
			int ar = arm instanceof Integer iar ? iar : 0;
			if (a != 0 && h != 0) {
				return (a >= 0 ? "+" : "") + a + "/" + (h >= 0 ? "+" : "") + h + ".";
			}
			if (a != 0) {
				return (a >= 0 ? "+" : "") + a + " Attack.";
			}
			if (h != 0) {
				return (h >= 0 ? "+" : "") + h + " Health.";
			}
			if (ar != 0) {
				return (ar >= 0 ? "+" : "") + ar + " Armor.";
			}
		} else if (spellClass == TemporaryAttackSpell.class) {
			Object v = spell.get(SpellArg.VALUE);
			Object atk = spell.get(SpellArg.ATTACK_BONUS);
			int iv = v instanceof Integer i1 ? i1 : (atk instanceof Integer i2 ? i2 : 0);
			if (iv != 0) {
				return (iv >= 0 ? "+" : "") + iv + " Attack this turn.";
			}
		} else if (spellClass == SetAttackSpell.class) {
			Object v = spell.get(SpellArg.VALUE);
			if (v instanceof Integer iv) {
				return "Attack set to " + iv + ".";
			}
		} else if (spellClass == SetHpSpell.class) {
			Object v = spell.get(SpellArg.VALUE);
			if (v instanceof Integer iv) {
				return "Health set to " + iv + ".";
			}
		} else if (spellClass == AddAttributeSpell.class
				|| spellClass == ModifyAttributeSpell.class
				|| spellClass == RemoveAttributeSpell.class) {
			Object attrObj = spell.get(SpellArg.ATTRIBUTE);
			if (attrObj instanceof Attribute attr) {
				String label = formatInlineAttributeDescription(attr);
				if (spellClass == ModifyAttributeSpell.class && spell.get(SpellArg.VALUE) instanceof Integer delta && delta != 0) {
					String cleanLabel = label.endsWith(".") ? label.substring(0, label.length() - 1) : label;
					return (delta >= 0 ? "+" : "") + delta + " " + cleanLabel + ".";
				}
				return label;
			} else if (attrObj != null) {
				return attrObj.toString();
			}
		} else if (spellClass == AddDeathrattleSpell.class) {
			SpellDesc subSpell = (SpellDesc) spell.get(SpellArg.SPELL);
			if (subSpell != null && subSpell.getString(SpellArg.DESCRIPTION) != null && !subSpell.getString(SpellArg.DESCRIPTION).isEmpty()) {
				String subDesc = subSpell.getString(SpellArg.DESCRIPTION);
				return subDesc.startsWith("Aftermath:") ? subDesc : "Aftermath: " + subDesc;
			}
			String ownerDesc = ownerCard.getDescription();
			if (ownerDesc != null && !ownerDesc.isEmpty()) {
				int idx = ownerDesc.indexOf("Aftermath:");
				if (idx >= 0) {
					return ownerDesc.substring(idx).replace("\"", "");
				}
				int drIdx = ownerDesc.indexOf("Deathrattle:");
				if (drIdx >= 0) {
					return "Aftermath:" + ownerDesc.substring(drIdx + "Deathrattle:".length()).replace("\"", "");
				}
				return "Aftermath: " + ownerDesc;
			}
			return "Aftermath.";
		} else if (spellClass == AddEnchantmentSpell.class) {
			Object triggerObj = spell.get(SpellArg.TRIGGER);
			if (triggerObj instanceof net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc enchDesc
					&& enchDesc.getDescription() != null
					&& !enchDesc.getDescription().isEmpty()) {
				return enchDesc.getDescription();
			}
		}
		return ownerCard.getDescription() != null ? ownerCard.getDescription() : "";
	}

	private static String formatInlineAttributeDescription(Attribute attr) {
		return switch (attr) {
			case TAUNT, AURA_TAUNT -> "Guard.";
			case DIVINE_SHIELD -> "Divine Shield.";
			case WINDFURY, AURA_WINDFURY -> "Windfury.";
			case MEGA_WINDFURY -> "Mega-Windfury.";
			case POISONOUS, AURA_POISONOUS -> "Poisonous.";
			case LIFESTEAL, AURA_LIFESTEAL -> "Lifesteal.";
			case STEALTH, AURA_STEALTH -> "Stealth.";
			case FROZEN -> "Frozen.";
			case RUSH, AURA_RUSH -> "Rush.";
			case CHARGE, AURA_CHARGE -> "Charge.";
			case IMMUNE, AURA_IMMUNE -> "Immune.";
			case DEFLECT -> "Deflect.";
			case UNTARGETABLE_BY_SPELLS, AURA_UNTARGETABLE_BY_SPELLS -> "Shroud.";
			case CANNOT_ATTACK, AURA_CANNOT_ATTACK -> "Can't Attack.";
			case CANNOT_ATTACK_HEROES -> "Can't Attack Heroes.";
			case SPELL_DAMAGE, AURA_SPELL_DAMAGE -> "Spell Damage.";
			case DECAY -> "Decay.";
			case SILENCED -> "Silenced.";
			case PERMANENT -> "Permanent.";
			default -> {
				String[] words = attr.name().toLowerCase().split("_");
				StringBuilder sb = new StringBuilder();
				for (String w : words) {
					if (w.isEmpty()) continue;
					if (!sb.isEmpty()) sb.append(' ');
					sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1));
				}
				yield sb.append('.').toString();
			}
		};
	}
}
