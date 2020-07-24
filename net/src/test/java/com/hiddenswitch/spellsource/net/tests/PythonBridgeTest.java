package com.hiddenswitch.spellsource.net.tests;

import co.paralleluniverse.strands.concurrent.CountDownLatch;
import com.hiddenswitch.spellsource.net.applications.PythonBridge;
import com.hiddenswitch.spellsource.net.impl.util.SimulationResultGenerator;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.behaviour.GameStateValueBehaviour;
import net.demilich.metastone.game.cards.CardCatalogue;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

public class PythonBridgeTest {
	String DECK_1 = "### Aggro Outlaw\n" +
			"# Class: COPPER\n" +
			"# Format: Spellsource\n" +
			"#\n" +
			"# 1x (1) Doodles\n" +
			"# 2x (1) Enhancing Shaman\n" +
			"# 2x (1) Hired Gunsmith\n" +
			"# 2x (1) Plan Ahead\n" +
			"# 2x (2) Bang!\n" +
			"# 2x (2) Beauregard Bouncer\n" +
			"# 2x (2) Shedding Chameleon\n" +
			"# 2x (2) Spooky Turret\n" +
			"# 2x (3) Cheating Wrangler\n" +
			"# 2x (3) Reloading\n" +
			"# 2x (3) Ride like the Wind!\n" +
			"# 2x (3) Trigger Happy Rebel\n" +
			"# 2x (4) Carriage Abductor\n" +
			"# 2x (4) Dustbowl Vigilante\n" +
			"# 1x (4) McGrief\n" +
			"# 2x (4) Silvershot Pistol";
	String DECK_2 = "### Baron: Big Baron\n" +
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
			"# 1x (8) Maskless Manhorse, Revengeance\n" +
			"# 1x (9) Gor'thal the Ravager\n" +
			"# 1x (10) Raid Boss Gnaxx\n" +
			"# 1x (10) Sorceress Eka\n" +
			"#\n";
	String DECK_3 = "### Big Defense Baron\n" +
			"# Class: NAVY\n" +
			"# Format: Spellsource\n" +
			"#\n" +
			"# 2x (1) Doom Sergeant\n" +
			"# 2x (2) Double Defender\n" +
			"# 2x (3) Defenses Up\n" +
			"# 2x (3) Extract\n" +
			"# 2x (3) Fellow Academite\n" +
			"# 2x (3) Final Defenses\n" +
			"# 2x (3) Oni Entrapper\n" +
			"# 2x (3) Reinforcements\n" +
			"# 2x (4) Double Down\n" +
			"# 2x (4) Immunize\n" +
			"# 2x (4) Stone Obelisk\n" +
			"# 1x (5) Moon Gladiator\n" +
			"# 2x (5) Royal Protector\n" +
			"# 1x (6) Attrition Master Rictor\n" +
			"# 1x (8) Shapesifter Ryal\n" +
			"# 2x (10) Fel Manticore\n" +
			"# 1x (10) Sourceborn Aelin\n" +
			"#";

	String DECK_4 = "### Blitzkrieg Dragoon\n" +
			"# Class: RUST\n" +
			"# Format: Spellsource\n" +
			"#\n" +
			"# 2x (1) Dragon Caretaker\n" +
			"# 2x (1) Timelost Sarcophagus\n" +
			"# 2x (1) Wink Dog\n" +
			"# 2x (2) Ankylo Devotee\n" +
			"# 2x (2) Augmented Pixie\n" +
			"# 2x (2) Katar\n" +
			"# 2x (3) Dragonhorn\n" +
			"# 1x (3) Irena, Dragon Knight\n" +
			"# 2x (3) Supersonic Roar\n" +
			"# 2x (3) Molten Whelp\n" +
			"# 2x (4) Guild Guard\n" +
			"# 2x (4) Sweeping Swarm\n" +
			"# 1x (5) Conflagration\n" +
			"# 2x (5) Drakonid Bruiser\n" +
			"# 2x (5) Vermillion Glider\n" +
			"# 2x (6) Crimson Blades\n" +
			"#";

	@Test
	public void testSimulateMethod() throws InterruptedException {
		CardCatalogue.loadCardsFromPackage();
		var deckLists = Arrays.asList(DECK_1, DECK_2, DECK_3, DECK_4);
		var n = 1;
		var latch = new CountDownLatch(1);
		Supplier<Behaviour> behaviourSupplier = () -> {
			var inst = new GameStateValueBehaviour();
			inst.setMaxDepth(1);
			return inst;
		};
		var counter = new AtomicInteger();
		PythonBridge.simulate(new SimulationResultGenerator() {
			@Override
			public void offer(String obj) {
				assertNotNull(obj);
				counter.incrementAndGet();
			}

			@Override
			public void stopIteration() {
				latch.countDown();
			}
		}, deckLists, n, Arrays.asList(behaviourSupplier, behaviourSupplier), false, true);
		latch.await();
		assertTrue(counter.get() > 0);
	}
}
