package net.demilich.metastone.game.gameconfig;

import com.hiddenswitch.spellsource.common.ClientConnectionConfiguration;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.behaviour.PlayRandomBehaviour;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.decks.DeckFormat;

import java.io.Serializable;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class GameConfig implements Cloneable, Serializable {

	private int numberOfGames;
	private PlayerConfig playerConfig1;
	private PlayerConfig playerConfig2;
	private DeckFormat deckFormat;
	private boolean isMultiplayer;

	private ClientConnectionConfiguration connection;
	private boolean casual;

	public GameConfig() {
	}

	/**
	 * Get a game config from a given deck pair with {@link PlayRandomBehaviour} as the player behaviour.
	 *
	 * @param deckPair Two {@link Deck} objects.
	 * @return A {@link GameConfig} from which you can retrieve a {@link GameContext}.
	 */
	public static GameConfig fromDecks(List<Deck> deckPair) {
		return fromDecks(deckPair, PlayRandomBehaviour::new, PlayRandomBehaviour::new);
	}

	/**
	 * Get a game config from a given deck pair.
	 *
	 * @param deckPair Two {@link Deck} objects.
	 * @param player1  A {@link Behaviour}, typically an AI behvaiour, to use in the game configuration.
	 * @param player2  A {@link Behaviour}, typically an AI behvaiour, to use in the game configuration.
	 * @return A {@link GameConfig} from which you can retrieve a {@link GameContext}.
	 */
	public static GameConfig fromDecks(List<Deck> deckPair, Supplier<Behaviour> player1, Supplier<Behaviour> player2) {
		final GameConfig config = new GameConfig();

		List<PlayerConfig> playerConfigs = deckPair.stream()
				.map(deck -> {
					PlayerConfig playerConfig = new PlayerConfig();
					playerConfig.setDeck(deck);
					playerConfig.setHeroCard(deck.getHeroCard());
					playerConfig.setName(deck.getName());
					return playerConfig;
				})
				.collect(Collectors.toList());

		playerConfigs.get(0).setBehaviour(player1.get());
		playerConfigs.get(1).setBehaviour(player2.get());
		config.setNumberOfGames(1);
		config.setPlayerConfig1(playerConfigs.get(0));
		config.setPlayerConfig2(playerConfigs.get(1));
		DeckFormat impliedFormat =
				deckPair.get(0).getFormat().equals(deckPair.get(1).getFormat())
						? deckPair.get(0).getFormat()
						: DeckFormat.getSmallestSupersetFormat(deckPair.stream().flatMap(deck -> deck.getCards().stream())
						.map(Card::getCardSet).collect(Collectors.toSet()));

		config.setDeckFormat(impliedFormat);
		return config;
	}

	public DeckFormat getDeckFormat() {
		return deckFormat;
	}

	public int getNumberOfGames() {
		return numberOfGames;
	}

	public PlayerConfig getPlayerConfig1() {
		return playerConfig1;
	}

	public PlayerConfig getPlayerConfig2() {
		return playerConfig2;
	}

	public void setDeckFormat(DeckFormat deckFormat) {
		this.deckFormat = deckFormat;
	}

	public void setNumberOfGames(int numberOfGames) {
		this.numberOfGames = numberOfGames;
	}

	public void setPlayerConfig1(PlayerConfig playerConfig1) {
		this.playerConfig1 = playerConfig1;
	}

	public void setPlayerConfig2(PlayerConfig playerConfig2) {
		this.playerConfig2 = playerConfig2;
	}

	public boolean isMultiplayer() {
		return isMultiplayer;
	}

	public void setMultiplayer(boolean multiplayer) {
		isMultiplayer = multiplayer;
	}

	public ClientConnectionConfiguration getConnection() {
		return connection;
	}

	public void setConnection(ClientConnectionConfiguration connection) {
		this.connection = connection;
	}

	@Override
	public GameConfig clone() throws CloneNotSupportedException {
		GameConfig config = new GameConfig();
		config.setNumberOfGames(getNumberOfGames());
		config.setPlayerConfig1(getPlayerConfig1().clone());
		config.setPlayerConfig2(getPlayerConfig2().clone());
		config.setDeckFormat(getDeckFormat().clone());
		config.setMultiplayer(isMultiplayer());
		config.setConnection(getConnection());
		return config;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[GameConfig]\n");
		builder.append("playerConfig1:\n");
		builder.append(getPlayerConfig1().toString());
		builder.append("\nplayerConfig2:\n");
		builder.append(getPlayerConfig2().toString());
		return builder.toString();
	}

	public void setCasual(boolean casual) {
		this.casual = casual;
	}

}
