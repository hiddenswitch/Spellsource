package com.hiddenswitch.spellsource.tests.cards;

import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.behaviour.GameStateValueBehaviour;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;
import net.demilich.metastone.game.cards.desc.compiler.CompiledCondition;
import net.demilich.metastone.game.cards.desc.compiler.CompiledEntityFilter;
import net.demilich.metastone.game.cards.desc.compiler.CompiledSpell;
import net.demilich.metastone.game.cards.desc.compiler.CompiledValueProvider;
import net.demilich.metastone.game.cards.desc.compiler.DescBytecodeCompiler;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.BuffSpell;
import net.demilich.metastone.game.spells.ModifyAttributeSpell;
import net.demilich.metastone.game.spells.PlayerAttribute;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.condition.AttributeCondition;
import net.demilich.metastone.game.spells.desc.condition.Condition;
import net.demilich.metastone.game.spells.desc.condition.ConditionArg;
import net.demilich.metastone.game.spells.desc.condition.ConditionDesc;
import net.demilich.metastone.game.spells.desc.filter.AttributeFilter;
import net.demilich.metastone.game.spells.desc.filter.CardFilter;
import net.demilich.metastone.game.spells.desc.filter.ComparisonOperation;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.filter.EntityFilterArg;
import net.demilich.metastone.game.spells.desc.filter.EntityFilterDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicOperation;
import net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicValueProvider;
import net.demilich.metastone.game.spells.desc.valueprovider.AttributeValueProvider;
import net.demilich.metastone.game.spells.desc.valueprovider.PlayerAttributeValueProvider;
import net.demilich.metastone.game.spells.desc.valueprovider.ReduceValueProvider;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProvider;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProviderArg;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProviderDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.Collections;
import java.util.List;

@Execution(ExecutionMode.SAME_THREAD)
public class Option1LoweringBenchmarkTest extends TestBase {

	private static ValueProvider createVp(Class<? extends ValueProvider> cls, Object... kv) {
		ValueProviderDesc d = new ValueProviderDesc(cls);
		for (int i = 0; i < kv.length; i += 2) {
			d.put((ValueProviderArg) kv[i], kv[i + 1]);
		}
		d.setReadOnly(true);
		return d.create();
	}

	@Test
	public void runOption1LoweringBenchmark() {
		// 1. Microbenchmarks: Option 1 Unlowered Bridge (ENABLE_OPTION1_LOWERING = false)
		//    vs. Option 1 DescBytecodeCompiler Lowered Hidden Class (ENABLE_OPTION1_LOWERING = true)
		runGym((context, player, opponent) -> {
			Minion minion = playMinionCard(context, player, 3, 2);
			minion.setRace("BEAST");
			minion.setAttribute(Attribute.ATTACK, 3);
			minion.setAttribute(Attribute.HP, 2);
			player.setMana(5);
			List<Entity> targets = Collections.singletonList(minion);

			// Build a realistic 4-element suite of Option 1 Descs for each component kind (constant ints + different ValueProviders)
			ValueProvider[] vpsUnlowered = new ValueProvider[4];
			ValueProvider[] vpsLowered = new ValueProvider[4];
			EntityFilter[] filtersUnlowered = new EntityFilter[4];
			EntityFilter[] filtersLowered = new EntityFilter[4];
			Condition[] condsUnlowered = new Condition[4];
			Condition[] condsLowered = new Condition[4];
			SpellDesc[] spellDescsUnlowered = new SpellDesc[4];
			SpellDesc[] spellDescsLowered = new SpellDesc[4];
			Spell[] spellsUnlowered = new Spell[4];
			Spell[] spellsLowered = new Spell[4];

			// Compile Unlowered suite while ENABLE_OPTION1_LOWERING = false
			DescBytecodeCompiler.clearCaches();
			DescBytecodeCompiler.ENABLE_OPTION1_LOWERING = false;
			for (int k = 0; k < 4; k++) {
				ValueProvider innerVp = switch (k) {
					case 0 -> createVp(PlayerAttributeValueProvider.class, ValueProviderArg.PLAYER_ATTRIBUTE, PlayerAttribute.MANA, ValueProviderArg.OFFSET, 1);
					case 1 -> createVp(AttributeValueProvider.class, ValueProviderArg.TARGET, EntityReference.SELF, ValueProviderArg.ATTRIBUTE, Attribute.ATTACK, ValueProviderArg.OFFSET, 1);
					case 2 -> createVp(AlgebraicValueProvider.class, ValueProviderArg.VALUE1, 2, ValueProviderArg.VALUE2, 3, ValueProviderArg.OPERATION, AlgebraicOperation.ADD);
					default -> createVp(ReduceValueProvider.class, ValueProviderArg.TARGET, EntityReference.FRIENDLY_MINIONS, ValueProviderArg.VALUE1, 2, ValueProviderArg.OPERATION, AlgebraicOperation.ADD);
				};
				vpsUnlowered[k] = innerVp;
				DescBytecodeCompiler.getCompiled(vpsUnlowered[k]);

				EntityFilterDesc fd = new EntityFilterDesc(k % 2 == 0 ? AttributeFilter.class : CardFilter.class);
				if (k % 2 != 0) {
					fd.put(EntityFilterArg.CARD_TYPE, CardType.MINION);
					fd.put(EntityFilterArg.RACE, "BEAST");
				}
				fd.put(EntityFilterArg.ATTRIBUTE, Attribute.ATTACK);
				fd.put(EntityFilterArg.OPERATION, ComparisonOperation.LESS_OR_EQUAL);
				fd.put(EntityFilterArg.VALUE, k == 0 ? 5 : innerVp);
				fd.setReadOnly(true);
				filtersUnlowered[k] = fd.create();
				DescBytecodeCompiler.getCompiled(filtersUnlowered[k]);

				ConditionDesc cd = new ConditionDesc(AttributeCondition.class);
				cd.put(ConditionArg.ATTRIBUTE, Attribute.ATTACK);
				cd.put(ConditionArg.OPERATION, ComparisonOperation.LESS_OR_EQUAL);
				cd.put(ConditionArg.VALUE, k == 0 ? 5 : innerVp);
				cd.setReadOnly(true);
				condsUnlowered[k] = cd.create();
				DescBytecodeCompiler.getCompiled(condsUnlowered[k]);

				SpellDesc sd = new SpellDesc(k % 2 == 0 ? BuffSpell.class : ModifyAttributeSpell.class);
				sd.put(SpellArg.TARGET, EntityReference.SELF);
				if (k % 2 == 0) {
					sd.put(SpellArg.ATTACK_BONUS, k == 0 ? 2 : innerVp);
					sd.put(SpellArg.HP_BONUS, 1);
				} else {
					sd.put(SpellArg.ATTRIBUTE, Attribute.ATTACK_BONUS);
					sd.put(SpellArg.VALUE, innerVp);
				}
				sd.setReadOnly(true);
				spellDescsUnlowered[k] = sd;
				spellsUnlowered[k] = sd.create();
				DescBytecodeCompiler.getCompiledSpell(sd);
			}

			// Compile Lowered suite while ENABLE_OPTION1_LOWERING = true
			DescBytecodeCompiler.clearCaches();
			DescBytecodeCompiler.ENABLE_OPTION1_LOWERING = true;
			for (int k = 0; k < 4; k++) {
				ValueProvider innerVp = switch (k) {
					case 0 -> createVp(PlayerAttributeValueProvider.class, ValueProviderArg.PLAYER_ATTRIBUTE, PlayerAttribute.MANA, ValueProviderArg.OFFSET, 1);
					case 1 -> createVp(AttributeValueProvider.class, ValueProviderArg.TARGET, EntityReference.SELF, ValueProviderArg.ATTRIBUTE, Attribute.ATTACK, ValueProviderArg.OFFSET, 1);
					case 2 -> createVp(AlgebraicValueProvider.class, ValueProviderArg.VALUE1, 2, ValueProviderArg.VALUE2, 3, ValueProviderArg.OPERATION, AlgebraicOperation.ADD);
					default -> createVp(ReduceValueProvider.class, ValueProviderArg.TARGET, EntityReference.FRIENDLY_MINIONS, ValueProviderArg.VALUE1, 2, ValueProviderArg.OPERATION, AlgebraicOperation.ADD);
				};
				vpsLowered[k] = innerVp;
				DescBytecodeCompiler.getCompiled(vpsLowered[k]);

				EntityFilterDesc fd = new EntityFilterDesc(k % 2 == 0 ? AttributeFilter.class : CardFilter.class);
				if (k % 2 != 0) {
					fd.put(EntityFilterArg.CARD_TYPE, CardType.MINION);
					fd.put(EntityFilterArg.RACE, "BEAST");
				}
				fd.put(EntityFilterArg.ATTRIBUTE, Attribute.ATTACK);
				fd.put(EntityFilterArg.OPERATION, ComparisonOperation.LESS_OR_EQUAL);
				fd.put(EntityFilterArg.VALUE, k == 0 ? 5 : innerVp);
				fd.setReadOnly(true);
				filtersLowered[k] = fd.create();
				DescBytecodeCompiler.getCompiled(filtersLowered[k]);

				ConditionDesc cd = new ConditionDesc(AttributeCondition.class);
				cd.put(ConditionArg.ATTRIBUTE, Attribute.ATTACK);
				cd.put(ConditionArg.OPERATION, ComparisonOperation.LESS_OR_EQUAL);
				cd.put(ConditionArg.VALUE, k == 0 ? 5 : innerVp);
				cd.setReadOnly(true);
				condsLowered[k] = cd.create();
				DescBytecodeCompiler.getCompiled(condsLowered[k]);

				SpellDesc sd = new SpellDesc(k % 2 == 0 ? BuffSpell.class : ModifyAttributeSpell.class);
				sd.put(SpellArg.TARGET, EntityReference.SELF);
				if (k % 2 == 0) {
					sd.put(SpellArg.ATTACK_BONUS, k == 0 ? 2 : innerVp);
					sd.put(SpellArg.HP_BONUS, 1);
				} else {
					sd.put(SpellArg.ATTRIBUTE, Attribute.ATTACK_BONUS);
					sd.put(SpellArg.VALUE, innerVp);
				}
				sd.setReadOnly(true);
				spellDescsLowered[k] = sd;
				spellsLowered[k] = sd.create();
				DescBytecodeCompiler.getCompiledSpell(sd);
			}

			final int warmupIters = 1_000_000;
			final int benchIters = 10_000_000;

			// Warmup
			long sink = 0;
			for (int i = 0; i < warmupIters; i++) {
				int k = i & 3;
				sink += vpsUnlowered[k].getValue(context, player, minion, minion);
				sink += vpsLowered[k].getValue(context, player, minion, minion);
				if (filtersUnlowered[k].matches(context, player, minion, minion)) sink++;
				if (filtersLowered[k].matches(context, player, minion, minion)) sink++;
				if (condsUnlowered[k].isFulfilled(context, player, minion, minion)) sink++;
				if (condsLowered[k].isFulfilled(context, player, minion, minion)) sink++;
			}
			for (int i = 0; i < 400_000; i++) {
				int k = i & 3;
				minion.setAttribute(Attribute.ATTACK_BONUS, 0);
				minion.setAttribute(Attribute.HP_BONUS, 0);
				DescBytecodeCompiler.castSpell(spellsUnlowered[k], context, player, spellDescsUnlowered[k], minion, targets);
				DescBytecodeCompiler.castSpell(spellsLowered[k], context, player, spellDescsLowered[k], minion, targets);
			}

			// Benchmark ValueProviders (10M)
			long t0 = System.nanoTime();
			for (int i = 0; i < benchIters; i++) {
				sink += vpsUnlowered[i & 3].getValue(context, player, minion, minion);
			}
			long vpUnloweredNs = System.nanoTime() - t0;

			t0 = System.nanoTime();
			for (int i = 0; i < benchIters; i++) {
				sink += vpsLowered[i & 3].getValue(context, player, minion, minion);
			}
			long vpLoweredNs = System.nanoTime() - t0;

			// Benchmark EntityFilters (10M)
			t0 = System.nanoTime();
			for (int i = 0; i < benchIters; i++) {
				if (filtersUnlowered[i & 3].matches(context, player, minion, minion)) sink++;
			}
			long filterUnloweredNs = System.nanoTime() - t0;

			t0 = System.nanoTime();
			for (int i = 0; i < benchIters; i++) {
				if (filtersLowered[i & 3].matches(context, player, minion, minion)) sink++;
			}
			long filterLoweredNs = System.nanoTime() - t0;

			// Benchmark Conditions (10M)
			t0 = System.nanoTime();
			for (int i = 0; i < benchIters; i++) {
				if (condsUnlowered[i & 3].isFulfilled(context, player, minion, minion)) sink++;
			}
			long condUnloweredNs = System.nanoTime() - t0;

			t0 = System.nanoTime();
			for (int i = 0; i < benchIters; i++) {
				if (condsLowered[i & 3].isFulfilled(context, player, minion, minion)) sink++;
			}
			long condLoweredNs = System.nanoTime() - t0;

			// Benchmark Spells (5M)
			final int spellBenchIters = 5_000_000;
			t0 = System.nanoTime();
			for (int i = 0; i < spellBenchIters; i++) {
				int k = i & 3;
				DescBytecodeCompiler.castSpell(spellsUnlowered[k], context, player, spellDescsUnlowered[k], minion, targets);
			}
			long spellUnloweredNs = System.nanoTime() - t0;

			t0 = System.nanoTime();
			for (int i = 0; i < spellBenchIters; i++) {
				int k = i & 3;
				DescBytecodeCompiler.castSpell(spellsLowered[k], context, player, spellDescsLowered[k], minion, targets);
			}
			long spellLoweredNs = System.nanoTime() - t0;

			System.out.println(String.format("=== Polymorphic Option 1 Microbenchmark Results (sink=%d) ===", sink));
			System.out.println(String.format("Option 1 ValueProviders (10M): Unlowered = %6.2f ms (%5.2f ns/op) | Lowered = %6.2f ms (%5.2f ns/op) | Speedup = %.2fx",
					vpUnloweredNs / 1e6, (double) vpUnloweredNs / benchIters,
					vpLoweredNs / 1e6, (double) vpLoweredNs / benchIters,
					(double) vpUnloweredNs / vpLoweredNs));
			System.out.println(String.format("Option 1 EntityFilters (10M):  Unlowered = %6.2f ms (%5.2f ns/op) | Lowered = %6.2f ms (%5.2f ns/op) | Speedup = %.2fx",
					filterUnloweredNs / 1e6, (double) filterUnloweredNs / benchIters,
					filterLoweredNs / 1e6, (double) filterLoweredNs / benchIters,
					(double) filterUnloweredNs / filterLoweredNs));
			System.out.println(String.format("Option 1 Conditions (10M):     Unlowered = %6.2f ms (%5.2f ns/op) | Lowered = %6.2f ms (%5.2f ns/op) | Speedup = %.2fx",
					condUnloweredNs / 1e6, (double) condUnloweredNs / benchIters,
					condLoweredNs / 1e6, (double) condLoweredNs / benchIters,
					(double) condUnloweredNs / condLoweredNs));
			System.out.println(String.format("Option 1 Spells+InlinedVP(5M): Unlowered = %6.2f ms (%5.2f ns/op) | Lowered = %6.2f ms (%5.2f ns/op) | Speedup = %.2fx",
					spellUnloweredNs / 1e6, (double) spellUnloweredNs / spellBenchIters,
					spellLoweredNs / 1e6, (double) spellLoweredNs / spellBenchIters,
					(double) spellUnloweredNs / spellLoweredNs));
		}, HeroClass.BROWN, HeroClass.BROWN);

		// 2. End-to-End Gameplay Benchmark: PlayRandomBehaviour (100 matches) + GameStateValueBehaviour (15 matches, maxDepth=2)
		final int randomMatches = 100;
		final int gsvbMatches = 15;

		// Warmup
		DescBytecodeCompiler.clearCaches();
		DescBytecodeCompiler.ENABLE_OPTION1_LOWERING = true;
		ClasspathCardCatalogue warmupCatalogue = new TestClasspathCardCatalogue();
		warmupCatalogue.loadCardsFromPackage();
		for (int i = 0; i < 30; i++) {
			try {
				GameContext ctx = TestBase.fromTwoRandomDecks(1000L + i, warmupCatalogue);
				ctx.play();
			} catch (Exception ignored) {
			}
		}
		warmupCatalogue = null;

		// Run with ENABLE_OPTION1_LOWERING = false (fresh catalogue so Descs compile with lowering disabled)
		DescBytecodeCompiler.clearCaches();
		DescBytecodeCompiler.ENABLE_OPTION1_LOWERING = false;
		ClasspathCardCatalogue unloweredCatalogue = new TestClasspathCardCatalogue();
		unloweredCatalogue.loadCardsFromPackage();

		long t0 = System.nanoTime();
		int unloweredTurns = 0;
		for (int i = 0; i < randomMatches; i++) {
			try {
				GameContext ctx = TestBase.fromTwoRandomDecks(1000L + i, unloweredCatalogue);
				ctx.play();
				unloweredTurns += ctx.getTurn();
			} catch (Exception ignored) {
			}
		}
		long randomUnloweredNs = System.nanoTime() - t0;

		t0 = System.nanoTime();
		int gsvbUnloweredTurns = 0;
		for (int i = 0; i < gsvbMatches; i++) {
			try {
				GameContext ctx = TestBase.fromTwoRandomDecks(2000L + i, unloweredCatalogue);
				ctx.setBehaviour(0, new GameStateValueBehaviour().setParallel(false).setMaxDepth(2).setTimeout(5000).setLethalTimeout(5000));
				ctx.setBehaviour(1, new GameStateValueBehaviour().setParallel(false).setMaxDepth(2).setTimeout(5000).setLethalTimeout(5000));
				ctx.play();
				gsvbUnloweredTurns += ctx.getTurn();
			} catch (Exception ignored) {
			}
		}
		long gsvbUnloweredNs = System.nanoTime() - t0;
		int unloweredHiddenClasses = DescBytecodeCompiler.getHiddenClassesDefinedCount();
		int unloweredInternedComponents = DescBytecodeCompiler.getInternedComponentCount();
		System.gc();
		long unloweredHeapBytes = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		unloweredCatalogue = null;

		// Run with ENABLE_OPTION1_LOWERING = true (fresh catalogue so Descs compile with lowering enabled)
		DescBytecodeCompiler.clearCaches();
		DescBytecodeCompiler.ENABLE_OPTION1_LOWERING = true;
		ClasspathCardCatalogue loweredCatalogue = new TestClasspathCardCatalogue();
		loweredCatalogue.loadCardsFromPackage();

		t0 = System.nanoTime();
		int loweredTurns = 0;
		for (int i = 0; i < randomMatches; i++) {
			try {
				GameContext ctx = TestBase.fromTwoRandomDecks(1000L + i, loweredCatalogue);
				ctx.play();
				loweredTurns += ctx.getTurn();
			} catch (Exception ignored) {
			}
		}
		long randomLoweredNs = System.nanoTime() - t0;

		t0 = System.nanoTime();
		int gsvbLoweredTurns = 0;
		for (int i = 0; i < gsvbMatches; i++) {
			try {
				GameContext ctx = TestBase.fromTwoRandomDecks(2000L + i, loweredCatalogue);
				ctx.setBehaviour(0, new GameStateValueBehaviour().setParallel(false).setMaxDepth(2).setTimeout(5000).setLethalTimeout(5000));
				ctx.setBehaviour(1, new GameStateValueBehaviour().setParallel(false).setMaxDepth(2).setTimeout(5000).setLethalTimeout(5000));
				ctx.play();
				gsvbLoweredTurns += ctx.getTurn();
			} catch (Exception ignored) {
			}
		}
		long gsvbLoweredNs = System.nanoTime() - t0;
		int loweredHiddenClasses = DescBytecodeCompiler.getHiddenClassesDefinedCount();
		int loweredInternedComponents = DescBytecodeCompiler.getInternedComponentCount();
		int loweredCompiledCacheSize = DescBytecodeCompiler.getCompiledCacheSize();
		System.gc();
		long loweredHeapBytes = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

		System.out.println("=== End-to-End Gameplay Benchmark ===");
		System.out.println(String.format("PlayRandomBehaviour (%d matches):    Unlowered = %7.2f ms (%5.2f ms/match, %d turns) | Lowered = %7.2f ms (%5.2f ms/match, %d turns) | Speedup = %.2fx",
				randomMatches,
				randomUnloweredNs / 1e6, randomUnloweredNs / 1e6 / randomMatches, unloweredTurns,
				randomLoweredNs / 1e6, randomLoweredNs / 1e6 / randomMatches, loweredTurns,
				(double) randomUnloweredNs / randomLoweredNs));
		System.out.println(String.format("GameStateValueBehaviour (%d matches): Unlowered = %7.2f ms (%6.2f ms/match, %d turns) | Lowered = %7.2f ms (%6.2f ms/match, %d turns) | Speedup = %.2fx",
				gsvbMatches,
				gsvbUnloweredNs / 1e6, gsvbUnloweredNs / 1e6 / gsvbMatches, gsvbUnloweredTurns,
				gsvbLoweredNs / 1e6, gsvbLoweredNs / 1e6 / gsvbMatches, gsvbLoweredTurns,
				(double) gsvbUnloweredNs / gsvbLoweredNs));
		System.out.println("=== Memory & Hidden-Class Deduplication Metrics ===");
		System.out.println(String.format("Hidden Classes Defined: Unlowered (no dedup) = %d | Lowered (Desc cache dedup) = %d (%.2fx reduction)",
				unloweredHiddenClasses, loweredHiddenClasses,
				loweredHiddenClasses > 0 ? (double) unloweredHiddenClasses / loweredHiddenClasses : 1.0));
		System.out.println(String.format("Interned Read-Only Component Singletons: Unlowered = %d | Lowered = %d | Compiled Cache Entries = %d",
				unloweredInternedComponents, loweredInternedComponents, loweredCompiledCacheSize));
		System.out.println(String.format("Post-GC Used Heap: Unlowered = %.2f MB | Lowered = %.2f MB",
				unloweredHeapBytes / (1024.0 * 1024.0), loweredHeapBytes / (1024.0 * 1024.0)));
	}

	@Test
	public void testMigratedOption1ComponentsEquivalence() {
		runGym((context, player, opponent) -> {
			Minion m1 = playMinionCard(context, player, 3, 4);
			m1.setRace("BEAST");
			Minion m2 = playMinionCard(context, player, 5, 2);
			m2.setRace("DEMON");
			Minion enemy = playMinionCard(context, opponent, 2, 6);
			enemy.setRace("BEAST");
			player.setMaxMana(10);
			player.setMana(10);

			for (boolean lowered : new boolean[]{false, true}) {
				DescBytecodeCompiler.clearCaches();
				DescBytecodeCompiler.ENABLE_OPTION1_LOWERING = lowered;

				// 1. Conditions
				ConditionDesc attrExists = new ConditionDesc(net.demilich.metastone.game.spells.desc.condition.AttributeExistsCondition.class);
				attrExists.put(ConditionArg.TARGET, EntityReference.FRIENDLY_MINIONS);
				attrExists.put(ConditionArg.ATTRIBUTE, Attribute.ATTACK);
				attrExists.setReadOnly(true);
				org.junit.jupiter.api.Assertions.assertTrue(attrExists.create().isFulfilled(context, player, m1, m1));

				ConditionDesc hasEntities = new ConditionDesc(net.demilich.metastone.game.spells.desc.condition.HasEntitiesOnBoardCondition.class);
				hasEntities.put(ConditionArg.CARDS, new String[]{m1.getSourceCard().getCardId(), m2.getSourceCard().getCardId()});
				hasEntities.setReadOnly(true);
				org.junit.jupiter.api.Assertions.assertTrue(hasEntities.create().isFulfilled(context, player, m1, m1));

				ConditionDesc manaMaxed = new ConditionDesc(net.demilich.metastone.game.spells.desc.condition.ManaMaxedCondition.class);
				manaMaxed.setReadOnly(true);
				org.junit.jupiter.api.Assertions.assertTrue(manaMaxed.create().isFulfilled(context, player, m1, m1));

				ConditionDesc minionOnBoard = new ConditionDesc(net.demilich.metastone.game.spells.desc.condition.MinionOnBoardCondition.class);
				minionOnBoard.put(ConditionArg.VALUE, 2);
				minionOnBoard.setReadOnly(true);
				org.junit.jupiter.api.Assertions.assertTrue(minionOnBoard.create().isFulfilled(context, player, m1, m1));

				ConditionDesc heroClassCond = new ConditionDesc(net.demilich.metastone.game.spells.desc.condition.HeroClassCondition.class);
				heroClassCond.put(ConditionArg.HERO_CLASS, player.getHero().getHeroClass());
				heroClassCond.setReadOnly(true);
				org.junit.jupiter.api.Assertions.assertEquals(
						player.getHero().getSourceCard().hasHeroClass(player.getHero().getHeroClass()),
						heroClassCond.create().isFulfilled(context, player, m1, m1));

				ConditionDesc deckContains = new ConditionDesc(net.demilich.metastone.game.spells.desc.condition.DeckContainsCondition.class);
				deckContains.setReadOnly(true);
				org.junit.jupiter.api.Assertions.assertEquals(!player.getDeck().isEmpty(), deckContains.create().isFulfilled(context, player, m1, m1));

				ConditionDesc graveyardContains = new ConditionDesc(net.demilich.metastone.game.spells.desc.condition.GraveyardContainsCondition.class);
				graveyardContains.setReadOnly(true);
				org.junit.jupiter.api.Assertions.assertFalse(graveyardContains.create().isFulfilled(context, player, m1, m1));

				ConditionDesc numCopies = new ConditionDesc(net.demilich.metastone.game.spells.desc.condition.NumberOfCopiesCondition.class);
				numCopies.put(ConditionArg.TARGET, EntityReference.FRIENDLY_MINIONS);
				numCopies.put(ConditionArg.OPERATION, ComparisonOperation.LESS_OR_EQUAL);
				numCopies.put(ConditionArg.VALUE, 2);
				numCopies.setReadOnly(true);
				org.junit.jupiter.api.Assertions.assertTrue(numCopies.create().isFulfilled(context, player, m1, m1));

				ConditionDesc playedLastTurn = new ConditionDesc(net.demilich.metastone.game.spells.desc.condition.PlayedLastTurnCondition.class);
				playedLastTurn.setReadOnly(true);
				org.junit.jupiter.api.Assertions.assertFalse(playedLastTurn.create().isFulfilled(context, player, m1, m1));

				ConditionDesc allModZero = new ConditionDesc(net.demilich.metastone.game.spells.desc.condition.AllModuloValueEqualsZeroCondition.class);
				allModZero.put(ConditionArg.TARGET, EntityReference.FRIENDLY_MINIONS);
				allModZero.put(ConditionArg.ATTRIBUTE, Attribute.HP);
				allModZero.put(ConditionArg.VALUE, 2);
				allModZero.setReadOnly(true);
				org.junit.jupiter.api.Assertions.assertTrue(allModZero.create().isFulfilled(context, player, m1, m1));

				ConditionDesc hasHeroPower = new ConditionDesc(net.demilich.metastone.game.spells.desc.condition.HasHeroPowerCondition.class);
				hasHeroPower.put(ConditionArg.CARD, player.getHeroPowerZone().get(0).getCardId());
				hasHeroPower.setReadOnly(true);
				org.junit.jupiter.api.Assertions.assertTrue(hasHeroPower.create().isFulfilled(context, player, m1, m1));

				ConditionDesc hasWeapon = new ConditionDesc(net.demilich.metastone.game.spells.desc.condition.HasWeaponCondition.class);
				hasWeapon.setReadOnly(true);
				org.junit.jupiter.api.Assertions.assertFalse(hasWeapon.create().isFulfilled(context, player, m1, m1));

				// 2. EntityFilters
				EntityFilterDesc highestAttr = new EntityFilterDesc(net.demilich.metastone.game.spells.desc.filter.HighestAttributeFilter.class);
				highestAttr.put(EntityFilterArg.ATTRIBUTE, Attribute.ATTACK);
				highestAttr.put(EntityFilterArg.TARGET, EntityReference.FRIENDLY_MINIONS);
				highestAttr.setReadOnly(true);
				org.junit.jupiter.api.Assertions.assertFalse(highestAttr.create().matches(context, player, m1, m1));
				org.junit.jupiter.api.Assertions.assertTrue(highestAttr.create().matches(context, player, m2, m1));

				EntityFilterDesc raceEquals = new EntityFilterDesc(net.demilich.metastone.game.spells.desc.filter.RaceEqualsFilter.class);
				raceEquals.put(EntityFilterArg.SECONDARY_TARGET, EntityReference.SELF);
				raceEquals.setReadOnly(true);
				org.junit.jupiter.api.Assertions.assertTrue(raceEquals.create().matches(context, player, enemy, m1));
				org.junit.jupiter.api.Assertions.assertFalse(raceEquals.create().matches(context, player, m2, m1));

				EntityFilterDesc raceFromSec = new EntityFilterDesc(net.demilich.metastone.game.spells.desc.filter.RaceFromSecondaryTargetFilter.class);
				raceFromSec.put(EntityFilterArg.SECONDARY_TARGET, EntityReference.SELF);
				raceFromSec.setReadOnly(true);
				org.junit.jupiter.api.Assertions.assertTrue(raceFromSec.create().matches(context, player, enemy, m1));

				EntityFilterDesc entityEquals = new EntityFilterDesc(net.demilich.metastone.game.spells.desc.filter.EntityEqualsFilter.class);
				entityEquals.put(EntityFilterArg.SECONDARY_TARGET, EntityReference.SELF);
				entityEquals.setReadOnly(true);
				org.junit.jupiter.api.Assertions.assertTrue(entityEquals.create().matches(context, player, m1, m1));
				org.junit.jupiter.api.Assertions.assertFalse(entityEquals.create().matches(context, player, m2, m1));

				// 3. ValueProviders
				ValueProvider origVp = createVp(net.demilich.metastone.game.spells.desc.valueprovider.OriginalValueProvider.class,
						ValueProviderArg.VALUE, 7);
				org.junit.jupiter.api.Assertions.assertEquals(7, origVp.getValue(context, player, m1, m1));

				ValueProvider spellDmgVp = createVp(net.demilich.metastone.game.spells.desc.valueprovider.SpellDamageValueProvider.class,
						ValueProviderArg.VALUE, 4);
				org.junit.jupiter.api.Assertions.assertEquals(4, spellDmgVp.getValue(context, player, m1, m1));

				ValueProvider distinctTribesVp = createVp(net.demilich.metastone.game.spells.desc.valueprovider.DistinctTribesValueProvider.class,
						ValueProviderArg.TARGET, EntityReference.FRIENDLY_MINIONS);
				org.junit.jupiter.api.Assertions.assertEquals(2, distinctTribesVp.getValue(context, player, m1, m1));

				ValueProvider minionSummonVp = createVp(net.demilich.metastone.game.spells.desc.valueprovider.MinionSummonValueProvider.class);
				org.junit.jupiter.api.Assertions.assertTrue(minionSummonVp.getValue(context, player, m1, m1) >= 2);

				// 4. Spells
				SpellDesc eitherOr = new SpellDesc(net.demilich.metastone.game.spells.EitherOrSpell.class);
				eitherOr.put(SpellArg.CONDITION, manaMaxed.create());
				eitherOr.put(SpellArg.SPELL1, BuffSpell.create(EntityReference.SELF, 1, 1));
				eitherOr.put(SpellArg.SPELL2, BuffSpell.create(EntityReference.SELF, 0, 0));
				eitherOr.setReadOnly(true);
				int prevAttack = m1.getAttack();
				DescBytecodeCompiler.castSpell(eitherOr.create(), context, player, eitherOr, m1, Collections.singletonList(m1));
				org.junit.jupiter.api.Assertions.assertEquals(prevAttack + 1, m1.getAttack());
			}
		}, HeroClass.BROWN, HeroClass.BROWN);
	}
}
