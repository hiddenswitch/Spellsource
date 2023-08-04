package com.spellsource.tasks

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

abstract class UnityPluginExtension {
  final DirectoryProperty unityProjectPath

  @Inject
  UnityPluginExtension(ObjectFactory objects, Project project) {
    unityProjectPath = objects.directoryProperty().convention(project.layout.projectDirectory.dir("src/Unity"))
  }
}
