package com.spellsource.tasks

import com.google.common.base.Strings
import groovy.json.JsonSlurper
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Plugin
import org.gradle.api.Project

class UnityPlugin implements Plugin<Project> {
  static class UnityArgsTuple {
    List<String> args
    String taskName
    String kind = 'Scene'
    String outputPath
  }

  @Override
  void apply(Project project) {
    var tasks = project.getTasks()
    def extension = project.extensions.create('unity', UnityPluginExtension.class)
    def buildUnityDependenciesTask = tasks.register('buildUnityDependencies') {
      group = 'unity'
      // gives the end user a chance to specify common dependencies for all scenes
    }

    def thisUnityProjectPath = extension.unityProjectPath.get()

    def configuredProjectSettingsFile = project.file("${thisUnityProjectPath.asFile.path}/ProjectSettings/ProjectSettings.asset")
    if (!configuredProjectSettingsFile.exists()) {
      return
    }
    def projectSettingsText = configuredProjectSettingsFile.text =~ /productName: (\w+)/
    def productName = projectSettingsText[0][1]

    def buildProjectTask = tasks.register("buildUnityProject") {
      it.group = 'unity'
    }

    def getLicenseTask = tasks.register("getLicense", UnityTask.class) {
      group = 'unity'

      def serial = System.getenv().getOrDefault("UNITY_SERIAL", "")
      def username = System.getenv().getOrDefault("UNITY_USERNAME", "")
      def password = System.getenv().getOrDefault("UNITY_PASSWORD", "")

      unityCommandLineArgs.set([
              "-serial",
              serial,
              "-username",
              username,
              "-password",
              password
      ])

      logToStdOut.set(true)
    }

    def returnLicenseTask = tasks.register("returnLicense", UnityTask.class) {
      group = 'unity'
      dependsOn getLicenseTask

      def username = System.getenv().getOrDefault("UNITY_USERNAME", "")
      def password = System.getenv().getOrDefault("UNITY_PASSWORD", "")

      unityCommandLineArgs.set([
              "-returnlicense",
              "-username",
              username,
              "-password",
              password
      ])
      logToStdOut.set(true)
    }

    [['iOS', '', false],
     ['Android', '', false],
     ['WebGL', '', false],
     ['StandaloneWindows64', '.exe', Os.isFamily(Os.FAMILY_WINDOWS)],
     ['StandaloneLinux64', '', Os.isFamily(Os.FAMILY_UNIX) && !Os.isFamily(Os.FAMILY_MAC)],
     ['StandaloneOSX', '', Os.isFamily(Os.FAMILY_UNIX) && Os.isFamily(Os.FAMILY_MAC)]].each { tuple ->
      def buildTarget = tuple[0] as String
      def buildExtension = tuple[1] as String
      def isThisPlatform = tuple[2] as Boolean
      var buildOutputPath = "${project.layout.buildDirectory.asFile.get().path}/$buildTarget";
      var taskOutputPath = buildOutputPath
      if (buildExtension != '') {
        buildOutputPath = "$buildOutputPath/${productName}${buildExtension}"
      } else if (buildTarget == 'StandaloneOSX') {
        buildOutputPath = "$buildOutputPath/${productName}"
      }

      var eachArgs = [
              new UnityArgsTuple(
                      kind: 'Project',
                      outputPath: buildOutputPath,
                      args: ["--editor-scenes", "--output=${project.file(buildOutputPath).absolutePath}"],
                      taskName: productName)
      ]

      eachArgs.each { additionalArgs ->
        def thisTask = tasks.register("build${additionalArgs.kind}_${additionalArgs.taskName.replace(" ", "")}_$buildTarget", UnityTask.class) {
          group = 'unity'
          dependsOn(buildUnityDependenciesTask)
          projectPath.set(extension.unityProjectPath.asFile.get().path)
          outputPath.set(taskOutputPath)


          if (!Strings.isNullOrEmpty(System.getenv().getOrDefault("UNITY_SERIAL", null))) {
            dependsOn(getLicenseTask)
            finalizedBy(returnLicenseTask)
          }

          // the unity task has good defaults for inputs
          unityCommandLineArgs.set([
                  "-quit",
                  "-executeMethod",
                  // from the csharp script
                  "Spellsource.Editor.BuildScripts.SpellsourceCli",
                  "build",
                  *additionalArgs.args,
                  "--build-target",
                  buildTarget
          ])
        }

        if (additionalArgs.kind == "Project") {
          def projectTask = tasks.register("buildProject_${buildTarget}") {
            it.group = 'unity'
            it.dependsOn(thisTask)
          }

          if (isThisPlatform) {
            buildProjectTask.configure { bpt ->
              bpt.dependsOn(projectTask)
            }
          }
        }
      }
    }

    def projectSettingsFile = project.file("ProjectSettings/ProjectSettings.asset")
    def editorSettingsFile = project.file("ProjectSettings/EditorSettings.asset")
    def sep = Os.isFamily(Os.FAMILY_WINDOWS) ? "\r\n" : "\n"

    def configureAccelerator = tasks.register("configureAccelerator") {
      group = 'unity'
      inputs.file(editorSettingsFile)
      outputs.file(editorSettingsFile)

      doLast {
        if (System.getenv().containsKey("UNITY_ACCELERATOR_ENDPOINT")) {
          def editorSettings = editorSettingsFile.getText()

          editorSettings = editorSettings.replaceFirst(
                  /m_CacheServerMode: \d*/,
                  "m_CacheServerMode: 1"
          )

          editorSettings = editorSettings.replaceFirst(
                  /m_CacheServerEndpoint: [\w \.\\/-]*/,
                  "m_CacheServerEndpoint: ${System.getenv().get("UNITY_ACCELERATOR_ENDPOINT")}"
          )

          editorSettings = editorSettings.replaceFirst(
                  /m_CacheServerNamespacePrefix: [\w \.\\/-]*/,
                  "m_CacheServerNamespacePrefix: ${System.getenv().get("UNITY_ACCELERATOR_NAMESPACE") ?: "default"}"
          )

          editorSettings = editorSettings.replaceFirst(
                  /m_CacheServerEnableDownload: \d*/,
                  "m_CacheServerEnableDownload: 1"
          )

          editorSettings = editorSettings.replaceFirst(
                  /m_CacheServerEnableUpload: \d*/,
                  "m_CacheServerEnableUpload: 1"
          )

          editorSettings = editorSettings.replaceFirst(
                  /m_CacheServerEnableAuth: \d*/,
                  "m_CacheServerEnableAuth: 0"
          )

          editorSettings = editorSettings.replaceFirst(
                  /m_CacheServerEnableTls: \d*/,
                  "m_CacheServerEnableTls: 0"
          )

          editorSettingsFile.setText(editorSettings)
        }
      }
    }

    def fixHeadlessBuild = tasks.register("fixHeadlessBuild") {
      group = 'unity'
      dependsOn(configureAccelerator)
      inputs.file(projectSettingsFile)
      outputs.file(projectSettingsFile)

      doLast {
        def projectSettings = projectSettingsFile.getText()

        projectSettings = projectSettings.replaceFirst(
                /m_BuildTarget: WindowsStandaloneSupport[\r\n]+    m_APIs: (\d+)[\r\n]+    m_Automatic: \d/,
                "m_BuildTarget: WindowsStandaloneSupport${sep}    m_APIs: \$1${sep}    m_Automatic: 0"
        )

        projectSettingsFile.setText(projectSettings)
      }
    }

    tasks.register("switchToMono") {
      group = 'unity'
      dependsOn(fixHeadlessBuild)
      inputs.file(projectSettingsFile)
      outputs.file(projectSettingsFile)

      doLast {
        def projectSettings = projectSettingsFile.getText()

        projectSettings = projectSettings.replaceFirst(
                /  scriptingBackend:[\r\n]+    Standalone: \d/,
                "  scriptingBackend: {}"
        )
        projectSettings = projectSettings.replaceFirst(
                /  scriptingBackend:[\r\n]+((?:    \w+: \d[\r\n]+)*)    Standalone: \d/,
                "  scriptingBackend:$sep\$1    Standalone: 0"
        )

        projectSettings = projectSettings.replaceFirst(
                /  il2cppCompilerConfiguration:[\r\n]+    Standalone: \d/,
                "  il2cppCompilerConfiguration: {}"
        )
        projectSettings = projectSettings.replaceFirst(
                /  il2cppCompilerConfiguration:[\r\n]+((?:    \w+: \d[\r\n]+)*)    Standalone: \d/,
                "  il2cppCompilerConfiguration:$sep\$1    Standalone: 0"
        )

        projectSettings = projectSettings.replaceFirst(
                /  managedStrippingLevel:[\r\n]+    Standalone: \d/,
                "  managedStrippingLevel: {}"
        )
        projectSettings = projectSettings.replaceFirst(
                /  managedStrippingLevel:[\r\n]+((?:    \w+: \d[\r\n]+)*)    Standalone: \d/,
                "  managedStrippingLevel:$sep\$1    Standalone: 0"
        )

        projectSettingsFile.setText(projectSettings)
      }
    }

    tasks.register("switchToIl2cpp") {
      group = 'unity'
      dependsOn(fixHeadlessBuild)
      inputs.file(projectSettingsFile)
      outputs.file(projectSettingsFile)

      doLast {
        def projectSettings = projectSettingsFile.getText()


        projectSettings = projectSettings.replaceFirst(
                /  scriptingBackend: \{\}/,
                "  scriptingBackend:$sep    Standalone: 1"
        )
        projectSettings = projectSettings.replaceFirst(
                /  scriptingBackend:[\r\n]+((?:    \w+: \d[\r\n]+)*)    Standalone: \d/,
                "  scriptingBackend:$sep\$1    Standalone: 1"
        )

        projectSettings = projectSettings.replaceFirst(
                /  il2cppCompilerConfiguration: \{\}/,
                "  il2cppCompilerConfiguration:$sep    Standalone: 1"
        )
        projectSettings = projectSettings.replaceFirst(
                /  il2cppCompilerConfiguration:[\r\n]+((?:    \w+: \d[\r\n]+)*)    Standalone: \d/,
                "  il2cppCompilerConfiguration:$sep\$1    Standalone: 1"
        )

        projectSettings = projectSettings.replaceFirst(
                /  managedStrippingLevel: \{\}/,
                "  managedStrippingLevel:$sep    Standalone: 1"
        )
        projectSettings = projectSettings.replaceFirst(
                /  managedStrippingLevel:[\r\n]+((?:    \w+: \d[\r\n]+)*)    Standalone: \d/,
                "  managedStrippingLevel:$sep\$1    Standalone: 1"
        )

        projectSettingsFile.setText(projectSettings)
      }
    }
  }
}