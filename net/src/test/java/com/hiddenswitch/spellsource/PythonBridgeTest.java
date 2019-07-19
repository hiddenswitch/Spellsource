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
	String DECK_1 = "### Baron: Big Baron\n" +
			"# Class: NAVY\n" +
			"# Format: Spellsource\n" +
			"#\n" +
			"# 2x (1) Enchanted Shield\n" +
			"# 2x (1) Gather Strength\n" +
			"# 2x (3) Bewitch\n" +
			"# 2x (3) Defenses Up\n" +
			"# 2x (3) Duplimancy\n" +
			"# 2x (4) Defender of Tomorrow\n" +
			"# 2x (4) Hidden Treasure\n" +
			"# 2x (4) Self-Appoint\n" +
			"# 2x (5) Bog Mutant\n" +
			"# 2x (5) Savage Werewolf\n" +
			"# 2x (7) Clash!\n" +
			"# 2x (7) Landsieged Drake\n" +
			"# 2x (7) Unstable Artifact\n" +
			"# 1x (8) Headless Horseman, Revengeance\n" +
			"# 1x (9) Gor'thal the Ravager\n" +
			"# 1x (10) Raid Boss Gnaxx\n" +
			"# 1x (10) Sorceress Eka\n" +
			"#";
	String DECK_2 = "### Summoner: Fifi Summoner\n" +
			"# Class: EGGPLANT\n" +
			"# Format: Custom\n" +
			"#\n" +
			"# 2x (0) Blackflame Ritual\n" +
			"# 2x (0) Rapier Rodent\n" +
			"# 2x (1) Double\n" +
			"# 2x (1) Hard Puncher\n" +
			"# 2x (1) Lackey Break\n" +
			"# 2x (2) Animation Surge\n" +
			"# 2x (2) Contemplate\n" +
			"# 1x (2) Fifi Fizzlewarp\n" +
			"# 2x (2) Thuggish Fae\n" +
			"# 2x (2) Whispers of Ruin\n" +
			"# 2x (4) Evil Laughter\n" +
			"# 1x (4) Monster Manual\n" +
			"# 2x (4) Mutated Brute\n" +
			"# 2x (5) Cybernetic Rager\n" +
			"# 2x (5) Fiery Tyrant\n" +
			"# 2x (6) Magma Hound";
	String DECK_3 = "### Chef: A Well Roasted Meal\n" +
			"Class: TOAST\n" +
			"Format: Spellsource\n" +
			"2x Pastry Cook\n" +
			"2x Guerrilla Chef\n" +
			"2x Unsatisfied Customer\n" +
			"2x Limb Tentacle\n" +
			"2x Lesser Opal Spellstone\n" +
			"2x Tuskarr Provisions\n" +
			"2x Onyx Pawn\n" +
			"2x Inedible Ghoul\n" +
			"2x Deathwing's Dinner\n" +
			"2x Stormwind Chef\n" +
			"2x Starving Myrmidon\n" +
			"1x Roasting Drake\n" +
			"1x Chef Stitches\n" +
			"2x Summoned Table\n" +
			"2x Fantastic Feast\n" +
			"1x Baul Pocuse\n" +
			"1x Boss Harambo";

	String DECK_4 = "### Witch Doctor: Spell Power Doctor\n" +
			"# Class: ROSE\n" +
			"# Format: Spellsource\n" +
			"#\n" +
			"# 2x (1) Wicked Insight\n" +
			"# 2x (1) Devil Within\n" +
			"# 2x (1) Hypnotic Chameleon\n" +
			"# 2x (1) Tiki Tokens\n" +
			"# 2x (2) Devilry Flare\n" +
			"# 2x (2) Dreamwing Scout\n" +
			"# 2x (2) Undergrowth Spirit\n" +
			"# 2x (2) Ghostly Essence\n" +
			"# 2x (2) Possessed Madness\n" +
			"# 2x (2) Sliver of Silver\n" +
			"# 2x (3) Old Jungle Masta\n" +
			"# 2x (4) Bat-talion\n" +
			"# 2x (5) Dawn Drake\n" +
			"# 2x (5) Jeering Troll\n" +
			"# 2x (6) Evocation\n" +
			"#";

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
