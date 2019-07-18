package net.demilich.metastone.game.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;

public class ResourceLoader {

	private static Logger logger = LoggerFactory.getLogger(ResourceLoader.class);

	/**
	 * Utility method to get a PathReference from a given sourceDir that's in the Resources dir or a Jar file.
	 *
	 * @param sourceDir the dir of interest in the Resources dir or Jar file
	 * @return a PathReference which contains a Path and boolean indicating the path is in a Jar fle.
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	private static PathReference getPathFromResources(String sourceDir) throws URISyntaxException, IOException {
		URI uri;
		try {
			URL resource = ClassLoader.getSystemClassLoader().getResource("/" + sourceDir);
			if (resource == null) {
				resource = ClassLoader.getSystemClassLoader().getResource(sourceDir);
			}
			if (resource == null) {
				resource = ResourceLoader.class.getClassLoader().getResource("/" + sourceDir);
			}
			if (resource == null) {
				resource = ResourceLoader.class.getClassLoader().getResource(sourceDir);
			}
			uri = resource.toURI();
		} catch (NullPointerException ex1) {
			logger.error(sourceDir + " directory not found in resources");
			throw new RuntimeException(sourceDir + " directory not found in resources");
		}

		// handle case where resources are on the filesystem instead of jar. ie:
		// running form within IntelliJ
		boolean fromJar = uri.getScheme().equals("jar");
		Path path;
		FileSystem fileSystem;
		if (fromJar) { // from jar file on the classpath
			try {
				fileSystem = FileSystems.getFileSystem(uri);
			} catch (FileSystemNotFoundException ex) {
				fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
			}
			path = fileSystem.getPath(sourceDir);
		} else { // from resources folder on the filesystem
			path = Paths.get(uri);
		}

		return new PathReference(path, fromJar);
	}

	/**
	 * Copy all files from the Resources sourceDir subfolder to the targetDir on the filesystem.
	 *
	 * @param sourceDir path to dir who's contents to copy
	 * @param targetdir path to dir where we want to copy the files to
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public static void copyFromResources(final String sourceDir, final String targetdir) throws URISyntaxException, IOException {

		ClassLoader cl = ClassLoader.getSystemClassLoader();
		URL[] urls = ((URLClassLoader) cl).getURLs();
		URL cardsUrl = null;
		String jarFileName = null;
		for (URL url : urls) {
			jarFileName = new File(url.toURI()).getName();
			if (jarFileName.startsWith("cards")) {
				cardsUrl = url;
				break;
			}
		}

		final String cardsJarFile = jarFileName;
		final PathReference sourcePathReference = getPathFromResources(sourceDir);
		final Path targetDirPath = Paths.get(targetdir);

		logger.info("Copying resources from " + cardsUrl + " to " + targetDirPath);

		Files.walkFileTree(sourcePathReference.path, new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
				//Path currentTargetDir = Paths.get(targetDirPath.toString() + File.separator + dir.getFileName());
				String relativePath = dir.toString().replace(sourcePathReference.path.toString(), "");
				Path currentTargetDir = Paths.get(targetDirPath + relativePath);
				Files.createDirectories(currentTargetDir);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
				String relativePath = file.toString().replace(sourcePathReference.path.toString(), "");
				Path currentTargetFile = Paths.get(targetDirPath + relativePath);

				logger.info(cardsJarFile + "!" + file + "  -->  " + currentTargetFile);
				Files.copy(file, currentTargetFile, StandardCopyOption.REPLACE_EXISTING);
				return FileVisitResult.CONTINUE;
			}

		});
	}

	/**
	 * Data tuple which holds a path and boolean flag indicating that the path is from a jar resource file.
	 */
	private static class PathReference {
		final Path path;
		final boolean fromJar;

		public PathReference(Path path, boolean fromJar) {
			this.path = path;
			this.fromJar = fromJar;
		}
	}
}
