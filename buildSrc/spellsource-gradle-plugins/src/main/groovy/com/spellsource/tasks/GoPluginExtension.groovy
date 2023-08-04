package com.spellsource.tasks

import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty

abstract class GoPluginExtension {
  abstract DirectoryProperty getWorkingDir()
  abstract MapProperty<String, String> getEnvironment()
  abstract ConfigurableFileTree getCmd()

  GoPluginExtension(Project project) {
    workingDir.set(project.layout.projectDirectory.dir('src'))
    cmd.from(project.layout.projectDirectory.dir('src/cmd')).include("**/*.go")
  }
}
