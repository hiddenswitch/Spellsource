package com.hiddenswitch.cluster.models;

import com.hiddenswitch.spellsource.util.Simulation;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.behaviour.PlayRandomBehaviour;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class SimulationConfig implements Serializable {
	private static final String DECKS = "decks";
	private static final String NUMBER = "number";
	private static final String OUTPUT = "output";
	private static final String BEHAVIOUR = "behaviour";
	private static final String QUIET = "quiet";
	private static final String MIRRORS = "mirrors";

	private boolean invalid;
	private Supplier<Behaviour> behaviourSupplier1;
	private Supplier<Behaviour> behaviourSupplier2;
	private PrintStream out;
	private List<String> deckPaths;
	private int number;
	private boolean quiet;
	private boolean mirrors;
	private boolean twoDifferentBehaviours;

	public SimulationConfig() {
	}

	public boolean isValid() {
		return !invalid;
	}

	public Supplier<Behaviour> getBehaviourSupplier1() {
		return behaviourSupplier1;
	}

	public Supplier<Behaviour> getBehaviourSupplier2() {
		return behaviourSupplier2;
	}

	public PrintStream getOutput() {
		return out;
	}

	public List<String> getDeckPaths() {
		return deckPaths;
	}

	public int getNumber() {
		return number;
	}

	public boolean isQuiet() {
		return quiet;
	}

	public boolean playMirrorMatchups() {
		return mirrors;
	}

	/**
	 * Returns true if the player specified two different behaviours. This would indicate that the deck list needs to be
	 * a full table and not an upper triangle.
	 * @return {@code true} if the user specified two different behaviours.
	 */
	public boolean twoDifferentBehaviours() {
		return twoDifferentBehaviours;
	}

	public SimulationConfig fromCommandLine(String... args) {
		Map<String, Class<? extends Behaviour>> availableBehaviours =
				Simulation.getAllBehaviours();

		Options options = new Options();
		Option decksOption = new Option(Character.toString(DECKS.charAt(0)), DECKS, true, "A comma-separated list of paths to decks written in the conventional community decklist format.");
		Option numberOption = new Option(Character.toString(NUMBER.charAt(0)), NUMBER, true, "The number of matches to run per unique matchup.");
		Option outputOption = new Option(Character.toString(OUTPUT.charAt(0)), OUTPUT, true, "The file path to write the simulation results to. When unspecified, the simulation results will be printed to standard out.");
		Option behaviourOption = new Option(Character.toString(BEHAVIOUR.charAt(0)), BEHAVIOUR, true,
				String.format("The class name of a Behaviour to instantiate for both players. The available options are %s. If you specify two behaviours in a comma-separated list, player 1 in each matchup will use the first, and player 2 will use the second. (Unsupported) If three or more behaviours are specified, a cartesian product of behaviours will be played.",
						availableBehaviours.keySet().toString()));
		Option quietOption = new Option(Character.toString(QUIET.charAt(0)), QUIET, false, "When set, does not print progress to the standard error stream.");
		Option mirrorOption = new Option(Character.toString(MIRRORS.charAt(0)), MIRRORS, false, "When set, include the mirror matchups for decks.");

		decksOption.setRequired(true);
		decksOption.setArgs(Option.UNLIMITED_VALUES);
		decksOption.setValueSeparator(',');
		behaviourOption.setArgs(Option.UNLIMITED_VALUES);
		behaviourOption.setValueSeparator(',');
		outputOption.setArgs(1);
		numberOption.setRequired(true);

		options.addOption(decksOption);
		options.addOption(numberOption);
		options.addOption(outputOption);
		options.addOption(behaviourOption);
		options.addOption(quietOption);
		options.addOption(mirrorOption);

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("--decks \"net/src/main/resources/decklists/current/Big Priest.txt\",\"net/src/main/resources/decklists/current/Tempo Rogue.txt\" --number 1000 -b GameStateValueBehaviour", options);
			invalid = true;
			return this;
		}

		if (cmd.hasOption(mirrorOption.getOpt())) {
			mirrors = true;
		}

		if (cmd.hasOption(quietOption.getOpt())) {
			quiet = true;
		}

		if (cmd.hasOption(outputOption.getOpt())) {
			String filePath = cmd.getOptionValue(outputOption.getOpt());
			try {
				out = new PrintStream(FileUtils.openOutputStream(new File(filePath)));
			} catch (Exception ex) {
				System.err.println(
						String.format("An error occurred while attempting to open the file path %s for writing output: %s", filePath, ex.getMessage()));
				invalid = true;
				return this;
			}
		} else {
			out = System.out;
		}

		if (cmd.hasOption(behaviourOption.getOpt())) {
			List<String> behaviours = Arrays.asList(cmd.getOptionValues(behaviourOption.getOpt()));

			if (behaviours.stream().distinct().count() > 1L) {
				twoDifferentBehaviours = true;
			}

			List<Supplier<Behaviour>> suppliers = Simulation.getBehaviourSuppliers(availableBehaviours, behaviours);

			for (int i = 0; i < behaviours.size(); i++) {
				if (suppliers.get(i) == null) {
					System.err.println(String.format("The supplied behaviour name %s is missing a no-args constructor or cannot be found on the classpath (check the spelling).", behaviours.get(0)));
					invalid = true;
					return this;
				}
			}

			behaviourSupplier1 = suppliers.get(0);
			if (suppliers.size() == 1) {
				behaviourSupplier2 = suppliers.get(0);
			} else if (suppliers.size() == 2) {
				behaviourSupplier2 = suppliers.get(1);
			} else {
				System.err.println("Using more than 2 behaviours in this matchup is currently not supported. We're seeking contributors to improve this functionality.");
				invalid = true;
				return this;
			}

		} else {
			behaviourSupplier1 = PlayRandomBehaviour::new;
			behaviourSupplier2 = PlayRandomBehaviour::new;
		}

		deckPaths = Arrays.asList(cmd.getOptionValues(decksOption.getOpt()));
		number = Integer.parseInt(cmd.getOptionValue(numberOption.getOpt()));
		invalid = false;
		return this;
	}

}
