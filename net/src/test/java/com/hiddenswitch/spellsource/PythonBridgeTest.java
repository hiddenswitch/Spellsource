package com.hiddenswitch.spellsource;

import com.hiddenswitch.spellsource.applications.PythonBridge;
import com.hiddenswitch.spellsource.impl.util.SimulationResultGenerator;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.behaviour.GameStateValueBehaviour;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class PythonBridgeTest {
	String DECK_1 = "### Cubelock - Standard Meta Snapshot - May 9, 2018\n" +
			"# Class: Warlock\n" +
			"# Format: Standard\n" +
			"# Year of the Raven\n" +
			"#\n" +
			"# 2x (1) Dark Pact\n" +
			"# 2x (1) Kobold Librarian\n" +
			"# 1x (2) Acidic Swamp Ooze\n" +
			"# 2x (2) Defile\n" +
			"# 2x (2) Plated Beetle\n" +
			"# 2x (3) Stonehill Defender\n" +
			"# 2x (4) Hellfire\n" +
			"# 2x (4) Lesser Amethyst Spellstone\n" +
			"# 1x (4) Spiritsinger Umbra\n" +
			"# 2x (5) Carnivorous Cube\n" +
			"# 2x (5) Doomguard\n" +
			"# 1x (5) Faceless Manipulator\n" +
			"# 2x (5) Possessed Lackey\n" +
			"# 1x (5) Skull of the Man'ari\n" +
			"# 1x (6) Rin, the First Disciple\n" +
			"# 1x (7) Lord Godfrey\n" +
			"# 2x (9) Voidlord\n" +
			"# 1x (10) Bloodreaver Gul'dan\n" +
			"# 1x (12) Mountain Giant";
	String DECK_2 = "### Even Paladin - Standard Meta Snapshot - May 9, 2018\n" +
			"# Class: Paladin\n" +
			"# Format: Standard\n" +
			"# Year of the Raven\n" +
			"#\n" +
			"# 1x (2) Acidic Swamp Ooze\n" +
			"# 2x (2) Amani Berserker\n" +
			"# 2x (2) Dire Wolf Alpha\n" +
			"# 2x (2) Equality\n" +
			"# 2x (2) Knife Juggler\n" +
			"# 2x (2) Loot Hoarder\n" +
			"# 2x (4) Blessing of Kings\n" +
			"# 2x (4) Call to Arms\n" +
			"# 2x (4) Consecration\n" +
			"# 2x (4) Saronite Chain Gang\n" +
			"# 2x (4) Spellbreaker\n" +
			"# 2x (4) Truesilver Champion\n" +
			"# 2x (6) Argent Commander\n" +
			"# 2x (6) Avenging Wrath\n" +
			"# 1x (6) Genn Greymane\n" +
			"# 1x (6) Sunkeeper Tarim\n" +
			"# 1x (6) Val'anyr";
	String DECK_3 = "### Spiteful Druid - Standard Meta Snapshot - May 9, 2018\n" +
			"# Class: Druid\n" +
			"# Format: Standard\n" +
			"# Year of the Raven\n" +
			"#\n" +
			"# 2x (1) Fire Fly\n" +
			"# 2x (1) Glacial Shard\n" +
			"# 1x (2) Prince Keleseth\n" +
			"# 2x (3) Crypt Lord\n" +
			"# 2x (3) Druid of the Scythe\n" +
			"# 2x (3) Greedy Sprite\n" +
			"# 2x (3) Mind Control Tech\n" +
			"# 1x (3) Tar Creeper\n" +
			"# 2x (4) Saronite Chain Gang\n" +
			"# 2x (4) Spellbreaker\n" +
			"# 2x (5) Cobalt Scalebane\n" +
			"# 2x (5) Fungalmancer\n" +
			"# 1x (5) Leeroy Jenkins\n" +
			"# 2x (6) Spiteful Summoner\n" +
			"# 1x (7) Malfurion the Pestilent\n" +
			"# 1x (8) Grand Archivist\n" +
			"# 1x (8) The Lich King\n" +
			"# 2x (10) Ultimate Infestation";

	String DECK_4 = "### Aggro Mage - Standard Meta Snapshot - Apr. 30, 2018\n" +
			"# Class: Mage\n" +
			"# Format: Standard\n" +
			"# Year of the Raven\n" +
			"#\n" +
			"# 2x (1) Arcane Missiles\n" +
			"# 2x (1) Mana Wyrm\n" +
			"# 1x (1) Mirror Image\n" +
			"# 1x (2) Amani Berserker\n" +
			"# 2x (2) Arcanologist\n" +
			"# 1x (2) Bloodmage Thalnos\n" +
			"# 2x (2) Frostbolt\n" +
			"# 2x (2) Primordial Glyph\n" +
			"# 2x (2) Sorcerer's Apprentice\n" +
			"# 2x (3) Arcane Intellect\n" +
			"# 2x (3) Cinderstorm\n" +
			"# 2x (3) Counterspell\n" +
			"# 2x (3) Explosive Runes\n" +
			"# 2x (3) Kirin Tor Mage\n" +
			"# 2x (4) Fireball\n" +
			"# 1x (4) Lifedrinker\n" +
			"# 1x (6) Aluneth\n" +
			"# 1x (10) Pyroblast";

	@Test
	public void testSimulateMethod() throws InterruptedException {
		CardCatalogue.loadCardsFromPackage();
		List<String> deckLists = Arrays.asList(DECK_1, DECK_2, DECK_3, DECK_4);
		int n = 20;
		CountDownLatch latch = new CountDownLatch(1);
		Supplier<Behaviour> behaviourSupplier = () -> {
			GameStateValueBehaviour inst = new GameStateValueBehaviour();
			inst.setMaxDepth(1);
			return inst;
		};
		AtomicInteger counter = new AtomicInteger();
		PythonBridge.simulate(new SimulationResultGenerator() {
			@Override
			public void offer(String obj) {
				Assert.assertNotNull(obj);
				counter.incrementAndGet();
			}

			@Override
			public void stopIteration() {
				latch.countDown();
			}
		}, deckLists, n, Arrays.asList(behaviourSupplier, behaviourSupplier), false, true);
		latch.await();
		Assert.assertTrue(counter.get() > 0);
	}
}
