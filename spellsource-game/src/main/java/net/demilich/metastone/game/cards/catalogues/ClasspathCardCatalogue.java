package net.demilich.metastone.game.cards.catalogues;

import com.hiddenswitch.spellsource.core.CardResource;
import com.hiddenswitch.spellsource.core.CardResources;
import com.hiddenswitch.spellsource.core.ResourceInputStream;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class ClasspathCardCatalogue extends ListCardCatalogue {
	/**
	 * Gets the card catalogue corresponding to the classpath.
	 */
	public static final ClasspathCardCatalogue INSTANCE = new ClasspathCardCatalogue();
	private AtomicBoolean loaded = new AtomicBoolean();
	private static final Logger LOGGER = LoggerFactory.getLogger(ClasspathCardCatalogue.class);

	/**
	 * Loads all the cards from the specified {@link CardResources} instances.
	 *
	 * @param cardResources
	 */
	public void loadCardsFromPackage(List<CardResources> cardResources) {
		for (CardResources cardResource : cardResources) {
			bannedCardIds.addAll(cardResource.getDraftBannedCardIds());
			hardRemovalCardIds.addAll(cardResource.getHardRemovalCardIds());
		}
		Collection<ResourceInputStream> inputStreams = cardResources
				.stream()
				.flatMap(resource -> resource.getResources().stream())
				.map(resource -> ((CardResource) resource))
				.map(resource -> (ResourceInputStream) resource)
				.collect(Collectors.toList());
		loadCards(inputStreams);

		LOGGER.debug("loadCards: {} cards loaded.", this.cards.size());
	}

	/**
	 * Loads all the cards specified in the {@code "cards/src/main/resources" + DEFAULT_CARDS_FOLDER } directory in the
	 * {@code cards} module. This can be called multiple times, but will not "refresh" the catalogue file.
	 */
	public void loadCardsFromPackage()  /*IOException, URISyntaxException*/ /*, CardParseException*/ {
        lock.writeLock().lock();
        try {
			if (!firstLoad()) {
				return;
			}

			List<CardResources> cardResources = null;
			try (ScanResult scanResult =
					     new ClassGraph()
							     .enableClassInfo()
							     .disableRuntimeInvisibleAnnotations()
							     .acceptPackages("com.hiddenswitch.spellsource.cards.*")
							     .scan()) {
				cardResources = scanResult
						.getAllClasses()
						.stream()
						.filter(info -> info.getName().contains("CardResources"))
						.map(ClassInfo::loadClass)
						.filter(CardResources.class::isAssignableFrom)
						.filter(c -> !Modifier.isAbstract(c.getModifiers()))
						.map(thisClass -> {
							try {
								return (CardResources) thisClass.getConstructor().newInstance();
							} catch (InstantiationException | IllegalAccessException | InvocationTargetException |
							         NoSuchMethodException e) {
								throw new RuntimeException(e);
							}
						})
						.collect(Collectors.toList());
				loadCardsFromPackage(cardResources);
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				if (cardResources != null) {
					for (CardResources cardResources1 : cardResources) {
						try {
							cardResources1.close();
						} catch (Exception ignored) {
						}
					}
				}
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	private boolean firstLoad() {
		return loaded.compareAndSet(false, true);
	}
}
