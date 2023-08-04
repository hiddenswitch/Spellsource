package com.spellsource.tasks;

import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Console;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Interface that declares native image options.
 *
 * @author gkrocher
 * @since 1.0.0
 */
public interface NativeImageOptions {
	/**
	 * @return Whether to enable fallbacks (defaults to false).
	 */
	Property<Boolean> isFallback();

	/**
	 * Gets the name of the native executable to be generated.
	 */
	@Input
	Property<String> getImageName();

	/**
	 * Sets the name of the native executable to be generated.
	 *
	 * @param name The name.
	 * @return this
	 */
	NativeImageOptions setImageName(@Nullable String name);

	/**
	 * Returns the fully qualified name of the Main class to be executed.
	 * <p>
	 * This does not need to be set if using an <a href="https://docs.oracle.com/javase/tutorial/deployment/jar/appman.html">Executable
	 * Jar</a> with a {@code Main-Class} attribute.
	 * </p>
	 */
	@Input
	Property<String> getMain();

	/**
	 * Sets the fully qualified name of the main class to be executed.
	 *
	 * @param main the fully qualified name of the main class to be executed.
	 * @return this
	 */
	NativeImageOptions setMain(@Nullable String main);

	/**
	 * Adds args for the main class to be executed.
	 *
	 * @param args Args for the main class.
	 * @return this
	 */
	NativeImageOptions args(Object... args);

	/**
	 * Adds args for the main class to be executed.
	 *
	 * @param args Args for the main class.
	 * @return this
	 */
	NativeImageOptions args(Iterable<?> args);

	/**
	 * Sets the args for the main class to be executed.
	 *
	 * @param args Args for the main class.
	 * @return this
	 * @since 4.0
	 */
	NativeImageOptions setArgs(@Nullable List<String> args);

	/**
	 * Sets the args for the main class to be executed.
	 *
	 * @param args Args for the main class.
	 * @return this
	 */
	NativeImageOptions setArgs(@Nullable Iterable<?> args);

	/**
	 * Returns the system properties which will be used for the process.
	 *
	 * @return The system properties. Returns an empty map when there are no system properties.
	 */
	@Input
	MapProperty<String, Object> getSystemProperties();

	/**
	 * Sets the system properties to use for the process.
	 *
	 * @param properties The system properties. Must not be null.
	 */
	void setSystemProperties(Map<String, ?> properties);

	/**
	 * Adds some system properties to use for the process.
	 *
	 * @param properties The system properties. Must not be null.
	 * @return this
	 */
	NativeImageOptions systemProperties(Map<String, ?> properties);

	/**
	 * Adds a system property to use for the process.
	 *
	 * @param name  The name of the property
	 * @param value The value for the property. May be null.
	 * @return this
	 */
	NativeImageOptions systemProperty(String name, Object value);

	/**
	 * Adds elements to the classpath for executing the main class.
	 *
	 * @param paths classpath elements
	 * @return this
	 */
	NativeImageOptions classpath(Object... paths);

	/**
	 * Returns the classpath for executing the main class.
	 */
	@Classpath
	@InputFiles
	FileCollection getClasspath();

	/**
	 * Sets the classpath for executing the main class.
	 *
	 * @param classpath the classpath
	 * @return this
	 */
	NativeImageOptions setClasspath(FileCollection classpath);

	/**
	 * Returns the extra arguments to use to launch the JVM for the process. Does not include system properties and the
	 * minimum/maximum heap size.
	 *
	 * @return The arguments. Returns an empty list if there are no arguments.
	 */
	@Input
	ListProperty<String> getJvmArgs();

	/**
	 * Sets the extra arguments to use to launch the JVM for the process. System properties and minimum/maximum heap size
	 * are updated.
	 *
	 * @param arguments The arguments. Must not be null.
	 * @since 4.0
	 */
	void setJvmArgs(@Nullable List<String> arguments);

	/**
	 * Sets the extra arguments to use to launch the JVM for the process. System properties and minimum/maximum heap size
	 * are updated.
	 *
	 * @param arguments The arguments. Must not be null.
	 */
	void setJvmArgs(@Nullable Iterable<?> arguments);

	/**
	 * Adds some arguments to use to launch the JVM for the process.
	 *
	 * @param arguments The arguments. Must not be null.
	 * @return this
	 */
	NativeImageOptions jvmArgs(Iterable<?> arguments);

	/**
	 * Adds some arguments to use to launch the JVM for the process.
	 *
	 * @param arguments The arguments.
	 * @return this
	 */
	NativeImageOptions jvmArgs(Object... arguments);


	/**
	 * Sets the native image build to be verbose
	 *
	 * @return this
	 */
	NativeImageOptions verbose(boolean verbose);

	/**
	 * Enables server build. Server build is disabled by default
	 *
	 * @return this
	 */
	NativeImageOptions enableServerBuild(boolean enabled);

	/**
	 * Builds a native image with debug symbols
	 *
	 * @return this
	 */
	NativeImageOptions debug(boolean debug);

	/**
	 * Sets whether to enable a fallback or not
	 *
	 * @return this
	 */
	NativeImageOptions fallback(boolean fallback);

	/**
	 * @return Is debug enabled
	 */
	@Input
	Property<Boolean> isDebug();

	/**
	 * @return Is verbose output
	 */
	@Console
	Property<Boolean> isVerbose();

	/**
	 * The path to the visual studio vars batch file
	 *
	 * @return
	 */
	@Input
	Property<String> getWindowsVsVarsPath();
}