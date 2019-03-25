package net.demilich.metastone.tests;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.hiddenswitch.spellsource.util.Logging;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.behaviour.CarlStermannLueckeBehaviour;
import net.demilich.metastone.game.behaviour.GameStateValueBehaviour;
import net.demilich.metastone.game.behaviour.heuristic.Heuristic;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.decks.RandomDeck;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.tests.util.TestBase;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.IntStream;

public class MassTest2 extends TestBase {

    int win0 = 0, win1 = 0;
    int gameNumber = 0;

    @BeforeTest
    private void loggerSetup() {
        win1 = 0;
        win0 = 0;
        gameNumber = 0;
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.ERROR);
    }

    @Test
    public void testRandomMassPlay() {
        loggerSetup();
        int tests = Boolean.parseBoolean(System.getenv("CI")) ? 1000 : 100;
        LocalDateTime start = LocalDateTime.now();
        IntStream.range(0, tests).parallel().forEach(i -> oneGame());
        LocalDateTime end = LocalDateTime.now();
        Duration timeTaken = Duration.between(start, end);
        long s = timeTaken.getSeconds();

        String timeTakenStr = String.format("%02d:%02d:%02d", s / 3600, (s % 3600) / 60, (s % 60));

        System.out.println("Start time: " + start.toString());
        System.out.println("End time: " + end.toString());
        System.out.println("Time taken: " + timeTakenStr);
        System.out.println("First player won: " + win0);
        System.out.println("Second player won: " + win1);
    }

    @Test
    public void testRandomMassPlayForLoop() {
        loggerSetup();
        LocalDateTime start = LocalDateTime.now();
        for (int i = 0; i < 10; i++) {
            oneGame();
        }
        LocalDateTime end = LocalDateTime.now();
        Duration timeTaken = Duration.between(start, end);
        long s = timeTaken.getSeconds();

        String timeTakenStr = String.format("%02d:%02d:%02d", s / 3600, (s % 3600) / 60, (s % 60));

        System.out.println("Start time: " + start.toString());
        System.out.println("End time: " + end.toString());
        System.out.println("Time taken: " + timeTakenStr);
        System.out.println("First player won: " + win0);
        System.out.println("Second player won: " + win1);
    }

    private void oneGame() {
        try {
            gameNumber++;
            System.out.println("Game number: " + gameNumber);
            RandomDeck randomDeck1 = new RandomDeck(HeroClass.BLUE, DeckFormat.STANDARD);
            RandomDeck randomDeck2 = new RandomDeck(HeroClass.BLUE, DeckFormat.STANDARD);
            Player player1 = new Player(randomDeck1, "player1");
            Player player2 = new Player(randomDeck2, "player2");
            GameContext context = new GameContext();
            context.setPlayer1(player1);
            context.setPlayer2(player2);


            context.setBehaviour(player1.getId(), new CarlStermannLueckeBehaviour("test Player"));
            context.setBehaviour(player2.getId(), new GameStateValueBehaviour());
            Logging.setLoggingLevel(Level.ERROR);
            context.play();
            int winner = context.getWinningPlayerId();
            if (winner == 0) win0++;
            if (winner == 1) win1++;
        } catch (NullPointerException e) {
            System.out.println("NullPointerException: " + gameNumber);
        }
    }
}