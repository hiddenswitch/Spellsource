package com.hiddenswitch.spellsource;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

/**
 * Implementors of this interface will get loaded by the {@code CardCatalogue} to find all the cards available to play
 * in the game.
 *
 * @see AbstractCardResources for more about adding more cards to Spellsource.
 */
public interface CardResources {
	@NotNull
	static InputStream getInputStream(ClassLoader loader, boolean fromJar, String filePath) {
		InputStream inputStream;
		if (fromJar) {
			inputStream = Object.class.getResourceAsStream(filePath.toString());
			// Try a variety of ways to access the resource. The way that works depends on whether or not this is
			// a shadow JAR or running inside a special environment.
			if (inputStream == null) {
				inputStream = loader.getResourceAsStream(filePath);
			}
			if (inputStream == null) {
				inputStream = loader.getResourceAsStream(filePath.substring(1));
			}
			if (inputStream == null) {
				inputStream = loader.getResourceAsStream("/" + filePath);
			}
			if (inputStream == null) {
				throw new NullPointerException("The path to the resources are still wrong!");
			}
		} else {
			try {
				inputStream = new FileInputStream(new File(filePath));
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		return inputStream;
	}

	void load();

	List<? extends CardResource> getResources();
}

