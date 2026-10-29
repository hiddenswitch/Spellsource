package com.spellsource.tasks

import com.spellsource.utils.WorkspaceUtils
import com.github.gradle.node.NodePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.CopySpec
import org.gradle.api.file.FileSystemOperations
import javax.inject.Inject

class NextPlugin implements Plugin<Project> {
  private final FileSystemOperations fs

  @Inject
  NextPlugin(FileSystemOperations fs) {
    this.fs = fs
  }

  @Override
  void apply(Project project) {
    project.plugins.apply(NodePlugin.class)

    def distDir = "${project.layout.buildDirectory.asFile.get().path}/dist"
    def parentProjectDir = project.parent.projectDir
    def projectDir = project.projectDir
    def projectName = project.name
    def workspaceDeps = WorkspaceUtils.resolveWorkspaceDependencies(project, true)
    def fileSystemOps = this.fs

    def createDistTask = project.tasks.register('createDist') { task ->
      // this is already configuring the task
      if (project.file('public').exists()) {
        inputs.dir('public')
      }
      if (project.file('src').exists()) {
        inputs.dir('src')
      }
      if (project.file('next.config.js').exists()) {
        inputs.file('next.config.js')
      }
      inputs.file('package.json')
      outputs.dir(distDir)

      def hasDockerProvider = project.provider { project.plugins.hasPlugin("docker") }
      doLast {
        WorkspaceUtils.createDist(fileSystemOps, parentProjectDir, projectDir, workspaceDeps, distDir)

        if (hasDockerProvider.get()) {
          def dockerDir = new File(projectDir, "docker/spellsource/${projectName.replaceFirst('spellsource-', '')}")
          fileSystemOps.copy { CopySpec c ->
            c.from(distDir)
            c.into(dockerDir)
          }
          def gitIgnore = new File(dockerDir, ".gitignore")
          if (!gitIgnore.exists()) {
            gitIgnore.append('*\n!.gitignore\n!Dockerfile')
          }
        }
      }
    }

    project.afterEvaluate {
      def prepareContextTasks = project.tasks.findAll { task -> task?.name?.startsWith("prepareContext") }
      if (prepareContextTasks) {
        prepareContextTasks.forEach { task ->
          task.configure {
            task.dependsOn(createDistTask)
          }
        }
      }
    }
  }
}
