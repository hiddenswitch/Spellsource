package com.spellsource.tasks

import groovy.json.JsonSlurper
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Task
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.api.tasks.AbstractExecTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

abstract class UnityTask extends AbstractExecTask<UnityTask> {
  static final isWindows = Os.isFamily(Os.FAMILY_WINDOWS)
  static final isLinux = Os.isFamily(Os.FAMILY_UNIX) && !Os.isFamily(Os.FAMILY_MAC)
  static final isMac = Os.isFamily(Os.FAMILY_MAC)

  @Input
  abstract ListProperty<String> getUnityCommandLineArgs()

  @Input
  abstract Property<Boolean> getUseMajorVersion()

  @Input
  abstract Property<String> getProjectPath()

  @Input
  abstract Property<String> getOutputPath()

  @Input
  abstract Property<Boolean> getLogToStdOut()

  @Internal
  Provider<BuildService<BuildServiceParameters>> unity3d

  UnityTask() {
    super(UnityTask.class)
    getUseMajorVersion().convention(true)
    getLogToStdOut().convention(false)
    getOutputPath().convention(project.layout.buildDirectory.map { it.asFile.path })
  }

  @Override
  Task configure(groovy.lang.Closure closure) {
    unity3d = project.gradle.sharedServices.registerIfAbsent("unity3d", BuildService) {
      maxParallelUsages.set(1)
    }
    usesService(unity3d)
    outputs.cacheIf { true }

    super.configure(closure)

    def projectFiles = project.fileTree(projectPath.map { "${project.layout.projectDirectory.file(it).asFile.path}/Assets" }) {
      // this affects caching
      exclude "**/TextMesh Pro/Resources/Fonts & Materials/*.asset"
    }

    inputs.dir(projectPath.map { "$it/Packages" })
            .withPathSensitivity(PathSensitivity.RELATIVE)
            .withPropertyName("packagesDirectory")
    inputs.files(projectFiles)
            .withPathSensitivity(PathSensitivity.RELATIVE)
            .withPropertyName("assetsDirectory")
    inputs.files(projectPath.map { "$it/ProjectSettings/*.asset" })
            .withPathSensitivity(PathSensitivity.RELATIVE)
            .withPropertyName("projectSettings")
    outputs.dir(outputPath.map { project.layout.projectDirectory.file(it) })
            .withPropertyName("outputDirectory")


//    def fileDependencies = project.fileTree()
//    def packageJsonFile = project.file("$thisUnityProjectPath.asFile.path/Packages/manifest.json")
//    // parse the package.json for file references and use the sources there
//    def packageJson = new JsonSlurper().parseText(packageJsonFile.text)
//    packageJson.dependencies.each { depName, dep ->
//      def depStr = dep as String
//      if (depStr.startsWith("file:")) {
//        def relativeToPackages = depStr.split(":")[1]
//        inputs.files("Packages/$relativeToPackages/**/*.cs")
//      }
//    }

    return this
  }

  protected String pathForVersion(String editorVersion) {
    String basePath = unityBasePath()

    def path = "$basePath/$editorVersion/Unity.app/Contents/MacOS/Unity"
    if (isWindows) {
      if (basePath.contains("Hub")) {
        path = "$basePath\\$editorVersion\\Editor\\Unity.exe"
      } else {
        path = "$basePath\\unity.exe"
      }
    } else if (isLinux) {
      path = "$basePath/$editorVersion/Editor/Unity"
    }

    return path
  }

  private String unityBasePath() {
    def basePath = "/Applications/Unity/Hub/Editor"
    if (isWindows) {
      basePath = "C:\\Program Files\\Unity\\Hub\\Editor"
      if (!project.file(basePath).exists()) {
        basePath = "C:\\Program Files\\Unity\\Editor"
      }
    } else if (isLinux) {
      basePath = "/opt/unity/editors"
    }
    basePath
  }

  @TaskAction
  protected void exec() {
    workingDir "${project.projectDir}"
    def tempDir = getTemporaryDir()
    String editorVersion = project.file("${projectPath.get()}/ProjectSettings/ProjectVersion.txt").text.readLines()[0].split(':')[1].trim()
    def path = pathForVersion(editorVersion)

    def executableFile = project.file(path)
    if (!executableFile.exists() || !executableFile.canExecute()) {
      // check if a major version exists we can use instead
      def found = false;

      if (useMajorVersion.get()) {
        def (projectYear, projectMajorVersion, projectMinorVersion) = editorVersion.split("\\.")
        new File(unityBasePath()).eachDir { it ->
          if (found) {
            return;
          }
          def (installedYear, installedMajorVersion, installedMinorVersion) = it.name.split("\\.")
          if (installedYear == projectYear && (installedMajorVersion as int) >= (projectMajorVersion as int)) {
            path = pathForVersion("$projectYear.$projectMajorVersion.$installedMinorVersion")
            found = true
            return;
          }
        }
      }

      if (!found) {
        throw new NullPointerException("Unity version for $path not installed, install one")
      } else {
        executableFile = project.file(path)
        if (!executableFile.exists() || !executableFile.canExecute()) {
          throw new NullPointerException("Unity version for $path not installed, install one")
        }
      }

    }

    def baseArgs = [
            path,
            '-projectPath', project.file(projectPath).absolutePath,
            '-batchmode',
            '-nographics',
            '-silent-crashes',
            '-logFile', logToStdOut.get() ? '-' : "$tempDir/unity.log",
            '-quit']

    commandLine(baseArgs + unityCommandLineArgs.get())
    try {
      super.exec()
    } catch (Throwable t) {
      if (project.file("$tempDir/unity.log").exists()) {
        project.logger.log(LogLevel.ERROR, project.file("$tempDir/unity.log").text)
      }
      throw t;
    }
  }
}