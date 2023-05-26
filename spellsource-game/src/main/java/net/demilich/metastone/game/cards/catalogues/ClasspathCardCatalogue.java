package net.demilich.metastone.game.cards.catalogues;

import com.hiddenswitch.spellsource.core.CardResource;
import com.hiddenswitch.spellsource.core.CardResources;
import com.hiddenswitch.spellsource.core.ResourceInputStream;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public final class ClasspathCardCatalogue extends ListCardCatalogue {
	/**
	 * Gets the card catalogue corresponding to the classpath.
	 *
	 * @return all the classpath cards
	 */
	public static final ClasspathCardCatalogue CLASSPATH = new ClasspathCardCatalogue();
	private AtomicBoolean loaded = new AtomicBoolean();
	private final ReentrantLock lock = new ReentrantLock();
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
	 * Loads all the cards from all classpath resources that are recursively inside the "cards" directory.
	 */
	public void loadAllCards() {
		loadAllCards("cards");
	}

	/**
	 * Loads all the cards from the specified directory.
	 * <p>
	 * Prevents {@link ClasspathCardCatalogue#loadCardsFromPackage()} from also redundantly loading the same cards.
	 * <p>
	 * Does <b>not</b> use ClassGraph so does not need to allocate direct byte buffers.
	 *
	 * @param directory
	 */
	public void loadAllCards(String directory) {
		lock.lock();

		try {
			if (!firstLoad()) {
				return;
			}

			var inputStreams = new ArrayList<ResourceInputStream>();
			var closeables = new ArrayList<AutoCloseable>();

			try {
				var allCardDirs = ClassLoader.getSystemClassLoader().getResources(directory).asIterator();
				for (; allCardDirs.hasNext(); ) {
					var uri = allCardDirs.next().toURI();
					var JAR = "jar";
					if (Objects.equals(uri.getScheme(), JAR)) {
						for (FileSystemProvider provider : FileSystemProvider.installedProviders()) {
							if (provider.getScheme().equalsIgnoreCase(JAR)) {
								try {
									closeables.add(provider.getFileSystem(uri));
								} catch (FileSystemNotFoundException e) {
									// in this case we need to initialize it first:
									closeables.add(provider.newFileSystem(uri, Collections.emptyMap()));
								}
							}
						}
					}
					var path = Paths.get(uri);
					var walk = Files.walk(path, FileVisitOption.FOLLOW_LINKS);
					for (var it = walk.iterator(); it.hasNext(); ) {
						var filename = it.next();
						if (filename.getFileName().toString().endsWith(".json")) {
							inputStreams.add(new ResourceInputStream(filename.getFileName().toString(), Files.newInputStream(filename)));
						}
					}
				}
			} catch (IOException | URISyntaxException e) {
				throw new RuntimeException(e);
			}

			loadCards(inputStreams);

			for (var inputStream : inputStreams) {
				try {
					inputStream.getInputStream().close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

			for (var closeable : closeables) {
				try {
					closeable.close();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Loads all the cards specified in the {@code "cards/src/main/resources" + DEFAULT_CARDS_FOLDER } directory in the
	 * {@code cards} module. This can be called multiple times, but will not "refresh" the catalogue file.
	 */
	public void loadCardsFromPackage()  /*IOException, URISyntaxException*/ /*, CardParseException*/ {
		lock.lock();
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
			lock.unlock();
		}
	}

	private boolean firstLoad() {
		return loaded.compareAndSet(false, true);
	}
}
