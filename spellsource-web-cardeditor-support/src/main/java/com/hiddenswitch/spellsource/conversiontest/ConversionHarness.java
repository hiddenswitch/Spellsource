package com.hiddenswitch.spellsource.conversiontest;

import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import io.vertx.core.json.Json;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardCatalogueRecord;
import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;
import net.demilich.metastone.game.cards.catalogues.ConcatenatedCardCatalogues;
import net.demilich.metastone.game.cards.catalogues.ListCardCatalogue;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.tests.util.TestBase;
import org.junit.platform.commons.util.ExceptionUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.LongStream;

public class ConversionHarness {

    static {
        ClasspathCardCatalogue.INSTANCE.loadCardsFromPackage();
    }

    private static final Object PROBE = new Object();

    protected static class Tuple {
        GameContext context;
        long seed;

        public Tuple(GameContext context, long seed) {
            this.context = context;
            this.seed = seed;
        }
    }

    public static boolean assertCardReplaysTheSame(int seed1, int seed2, String cardId, String replacementJson) throws IOException {
        try {
            return assertCardReplaysTheSame(new long[]{seed1, seed2}, cardId, replacementJson);
        } catch (Throwable t) {
            throw new RuntimeException(t.getMessage() + "\n" + ExceptionUtils.readStackTrace(t));
        }
    }

    public static boolean assertCardReplaysTheSame(long[] seeds, String cardId, String replacementJson) throws IOException {
        var baseSingletonCatalogue = new ListCardCatalogue();
        baseSingletonCatalogue.addOrReplaceCard(ClasspathCardCatalogue.INSTANCE.getCardById(cardId).getDesc());
        var baseCatalogue = new ConcatenatedCardCatalogues(Arrays.asList(baseSingletonCatalogue, ClasspathCardCatalogue.INSTANCE));
        
        var replacedSingletonCatalogue = new ListCardCatalogue();
        replacedSingletonCatalogue.addOrReplaceCard(replacementJson);
        var replacedCatalogue = new ConcatenatedCardCatalogues(Arrays.asList(replacedSingletonCatalogue, ClasspathCardCatalogue.INSTANCE));
        
        return LongStream.of(seeds)
                .mapToObj(seed -> {
                    // ClasspathCardCatalogue.INSTANCE.loadCardsFromPackage();
                    // test the game without the replacement
                    GameContext context = TestBase.fromTwoRandomDecks(seed, baseCatalogue);
                    ensureCardIsInDeck(context, cardId);
                    context.play();
                    return new Tuple(context, seed);
                })
                .filter(tuple -> tuple.context.getTrace().getRawActions().stream().anyMatch(ga -> {
                    if (ga.getSourceReference() == null) {
                        return false;
                    }
                    var source = ga.getSource(tuple.context);
                    if (source == null) {
                        return false;
                    }
                    var sourceCard = source.getSourceCard();
                    if (sourceCard == null) {
                        return false;
                    }
                    return Objects.equals(sourceCard.getCardId(), cardId);
                }))
                .allMatch(tuple -> {
                    GameContext reproduction = TestBase.fromTwoRandomDecks(tuple.seed, replacedCatalogue);
                    ensureCardIsInDeck(reproduction, cardId);
                    reproduction.play();

                    return tuple.context.getTurn() == reproduction.getTurn();
                });
    }

    static void ensureCardIsInDeck(GameContext context, String cardId) {
        var card = context.getCardById(cardId);
        var cardType = card.getCardType();
        if (cardType == CardType.CLASS || cardType == CardType.ENCHANTMENT || cardType == CardType.HERO_POWER ||
                (cardType == CardType.HERO && context.getCardById(cardId).hasAttribute(Attribute.HP))) {
            return;
        }
        for (var player : context.getPlayers()) {
            for (var i = 0; i < 5; i++) {
                player.getDeck().addCard(context.getCardCatalogue(), cardId);
            }
        }
    }
}
