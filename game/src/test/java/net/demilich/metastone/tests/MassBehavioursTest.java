package net.demilich.metastone.tests;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.behaviour.CarlStermannLueckeBehaviour;
import net.demilich.metastone.game.behaviour.GameStateValueBehaviour;
import net.demilich.metastone.game.behaviour.TataTeBehaviour;
import net.demilich.metastone.game.behaviour.salimcts.SaliMCTSBehaviour;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.decks.RandomDeck;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.tests.util.TestBase;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.stream.IntStream;
import java.nio.file.Path;

public class MassBehavioursTest extends TestBase {

    int win0 = 0, win1 = 0;
    int gameNumber = 0;

    @BeforeTest
    private void loggerSetup() {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.ERROR);
    }

    @Test
    public void testBehaviorsBattle() {
        //CarlStermannLuecke tests
        Behaviour b1 = new CarlStermannLueckeBehaviour("CarlStermannLuecke");
        Behaviour b2 = new TataTeBehaviour("TataTe");
        System.out.println(b1.getName() + " VS " + b2.getName());
        randomMassPlay(b1, b2);

        b1 = new CarlStermannLueckeBehaviour("CarlStermannLuecke");
        b2 = new SaliMCTSBehaviour("SaliMCTS");
        System.out.println(b1.getName() + " VS " + b2.getName());
        randomMassPlay(b1, b2);

        b1 = new CarlStermannLueckeBehaviour("CarlStermannLuecke");
        b2 = new GameStateValueBehaviour();
        System.out.println(b1.getName() + " VS " + "GSVB");
        randomMassPlay(b1, b2);

//        //TataTe
        b1 = new TataTeBehaviour("TataTe");
        b2 = new SaliMCTSBehaviour("SaliMCTS");
        System.out.println(b1.getName() + " VS " + b2.getName());
        randomMassPlay(b1, b2);

        b1 = new TataTeBehaviour("TataTe");
        b2 = new GameStateValueBehaviour();
        System.out.println(b1.getName() + " VS " + "GSVB");
        randomMassPlay(b1, b2);

    //        //SaliMCTS
        b1 = new SaliMCTSBehaviour("SaliMCTS");
        b2 = new GameStateValueBehaviour();
        System.out.println(b1.getName() + " VS " + "GSVB");
        randomMassPlay(b1, b2);
    }


    public void randomMassPlay(Behaviour b1, Behaviour b2) {
        loggerSetup();
//        int tests = Boolean.parseBoolean(System.getenv("CI")) ? 1000 : 1000;

        win1 = 0;
        win0 = 0;
        gameNumber = 0;

        LocalDateTime start = LocalDateTime.now();
        IntStream.range(0, 100).parallel().forEach(i -> oneGame(b1, b2));

        LocalDateTime end = LocalDateTime.now();
        Duration timeTaken = Duration.between(start, end);
        long s = timeTaken.getSeconds();

        String timeTakenStr = String.format("%02d:%02d:%02d", s / 3600, (s % 3600) / 60, (s % 60));

        System.out.println("Start time: " + start.toString());
        System.out.println("End time: " + end.toString());
        System.out.println("Time taken: " + timeTakenStr);
        System.out.println("Games played: " + gameNumber);
        System.out.println("First player win rate: " + (double) win0 / gameNumber);
        System.out.println("Second player win rate: " + (double) win1 / gameNumber);
        System.out.println("######################################\n");

    }

    private void oneGame(Behaviour b1, Behaviour b2) {
//        GameContext context = GameContext.fromTwoRandomDecks();

        GameContext context = new GameContext();

        RandomDeck randomDeck1 = new RandomDeck(HeroClass.BLUE, DeckFormat.STANDARD);
        RandomDeck randomDeck2 = new RandomDeck(HeroClass.BLUE, DeckFormat.STANDARD);
        Player player1 = new Player(randomDeck1, b1.getName());
        Player player2 = new Player(randomDeck2, b2.getName());

        context.setPlayer1(player1);
        context.setPlayer2(player2);
        context.setDeckFormat(DeckFormat.STANDARD);
        context.setBehaviour(player1.getId(), b1);
        context.setBehaviour(player2.getId(), b2);

        try {
            gameNumber++;
//            System.out.println("Game number: " + gameNumber);
            context.play();
            int winner = context.getWinningPlayerId();
            if (winner == 0) win0++;
            if (winner == 1) win1++;
        } catch (RuntimeException any) {
            gameNumber--;
            any.printStackTrace();
//            try {
//                Files.writeString(FileSystems.getDefault().getPath("masstest-trace-" + Instant.now().toString().replaceAll("[/\\\\?%*:|\".<>\\s]", "_") + ".json"), context.getTrace().dump());
//            } catch (IOException e) {
//                return;
//            }
//            throw any;
        }
    }
}