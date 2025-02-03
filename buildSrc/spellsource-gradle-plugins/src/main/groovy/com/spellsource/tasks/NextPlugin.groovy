package com.spellsource.tasks

import com.spellsource.utils.WorkspaceUtils
import com.github.gradle.node.NodePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.CopySpec

class NextPlugin implements Plugin<Project> {
  @Override
  void apply(Project project) {
    project.plugins.apply(NodePlugin.class)

    def distDir = "$project.buildDir/dist"

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

      doLast {
        WorkspaceUtils.createDist(project, distDir, true)

        if (project.plugins.hasPlugin("docker")) {
          def dockerDir = "$project.projectDir/docker/spellsource/${project.name.replaceFirst('spellsource-', '')}"
          project.copy { CopySpec c ->
            c.from(distDir)
            c.into(dockerDir)
          }
          def gitIgnore = project.file("$dockerDir/.gitignore")
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
