package com.spellsource.tasks;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaApplication;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.testing.Test;
import org.gradle.jvm.tasks.Jar;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Support for building GraalVM native images.
 *
 * @author graemerocher
 * @since 1.0.0
 */
public class GraalPlugin implements Plugin<Project> {

	private static final List<String> DEPENDENT_CONFIGURATIONS = Arrays.asList(JavaPlugin.API_CONFIGURATION_NAME, JavaPlugin.RUNTIME_ONLY_CONFIGURATION_NAME, JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME);

	@Override
	public void apply(Project project) {

		if (project.getPlugins().hasPlugin("application")) {
			TaskContainer tasks = project.getTasks();
			TaskProvider<NativeImageTask> nit = tasks.register("nativeImage", NativeImageTask.class, nativeImageTask -> {
				nativeImageTask.dependsOn(tasks.findByName("classes"));
				nativeImageTask.setGroup(BasePlugin.BUILD_GROUP);
				nativeImageTask.setDescription("Builds a GraalVM Native Image");
				nativeImageTask.getMain().set(project.getExtensions().getByType(JavaApplication.class).getMainClass());
			});

			project.afterEvaluate(p -> p
					.getConfigurations()
					.configureEach(configuration -> {
						if (DEPENDENT_CONFIGURATIONS.contains(configuration.getName())) {
							final DependencySet dependencies = configuration.getDependencies();
							for (Dependency dependency : dependencies) {
								if (dependency instanceof ProjectDependency) {
									final Project otherProject = ((ProjectDependency) dependency).getDependencyProject();
									otherProject.getTasks().withType(Jar.class, jar -> {
										if (jar.getName().equals("jar")) {
											nit.configure(nativeImageTask -> nativeImageTask.dependsOn(jar));
										}
									});
								}
							}
						}
					}));

			tasks.withType(Test.class, (test ->
					tasks.register(test.getName() + "NativeImage", nativeImageTestTask -> {
						nativeImageTestTask.doLast((t) -> {
							NativeImageTask nativeImage = nit.get();
							File file = nativeImage.getNativeImageOutput();
							test.systemProperty("com.spellsource.build.test.server.executable", file.getAbsolutePath());
						});
						boolean enabled = test.isEnabled() && GraalUtil.isGraalJVM();
						nativeImageTestTask.onlyIf(task -> {
							boolean isGraal = GraalUtil.isGraalJVM();
							if (!isGraal) {
								project.getLogger().log(LogLevel.INFO, "Skipping testNativeImage because the configured JDK is not a GraalVM JDK");
							}
							return isGraal;
						});
						if (enabled) {
							nativeImageTestTask.dependsOn(nit);
							test.mustRunAfter(nativeImageTestTask);
							nativeImageTestTask.finalizedBy(test);
						}
						nativeImageTestTask.setDescription("Runs tests against a native image build of the server. Requires the server to allow the port to configurable with 'com.spellsource.build.server.port'.");
					})));


			project.afterEvaluate(p -> p.getTasks().withType(NativeImageTask.class, nativeImageTask -> {
				if (!nativeImageTask.getName().equals("internalDockerNativeImageTask")) {
					nativeImageTask.setEnabled(true);
					JavaApplication javaApplication = p.getExtensions().getByType(JavaApplication.class);
					String mainClassName = javaApplication.getMainClass().getOrNull();
					String imageName = p.getName();
					if (mainClassName != null && !nativeImageTask.getMain().isPresent()) {
						nativeImageTask.setMain(mainClassName);
					}
					if (!nativeImageTask.getImageName().isPresent()) {
						nativeImageTask.setImageName(imageName);
					}
				}
			}));
		}
	}
}