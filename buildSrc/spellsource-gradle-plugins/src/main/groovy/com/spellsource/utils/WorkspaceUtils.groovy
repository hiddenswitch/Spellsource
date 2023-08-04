package com.spellsource.utils

import groovy.json.JsonSlurper
import org.gradle.api.Project
import org.gradle.api.file.CopySpec

class WorkspaceUtils {

  static ArrayList<String> gitignoreToList(File f) {
    def ignores = []
    if (f != null && f.exists()) {
      f.eachLine { line ->
        //ignore comments and empty lines
        if (!line.startsWith('#') && !line.isEmpty()) {
          ignores.add(line)
        }
        return
      }
    }
    return ignores
  }

  static def createDist(Project project, String distDir, boolean includeSelf) {
    def slurper = new JsonSlurper()
    def parent = project.parent
    def parentPackage = slurper.parse(project.file("${parent.projectDir}/package.json"))

    // Map of Gradle project names to their slurped package.json object
    def packages = (Map<String, Object>) parentPackage.workspaces.collectEntries({
      def pkg = project.file("$parent.projectDir/$it/package.json")
      return [parent.subprojects.find({ p -> it.startsWith(p.name) })?.name, pkg.exists() ? slurper.parse(pkg) : null]
    })

    def dependencies = new HashSet([project.name])
    while (true) {
      def prevSize = dependencies.size()
      dependencies.addAll(packages.findAll({ entry ->
        dependencies.any { p ->
          entry.value && packages.get(p)?.dependencies?.containsKey(entry.value.name)
        }
      }).keySet())
      if (dependencies.size() == prevSize) {
        break
      }
    }

    if (!includeSelf) {
      dependencies.remove(project.name)
    }

    def ignores = gitignoreToList(project.file("$parent.projectDir/.gitignore")) + gitignoreToList(project.file("$project.projectDir/.gitignore"))
    ignores.add 'build.gradle'
    ignores.add '**/csharp'
    ignores.add 'docker/'
    ignores.add '**/node_modules'

    dependencies.forEach { p ->
      project.copy { CopySpec c ->
        c.from "$parent.projectDir/$p"
        c.into "$distDir/$p/"
        c.exclude ignores
      }
    }

    project.copy { CopySpec c ->
      c.from "$parent.projectDir/yarn.lock", "$parent.projectDir/package.json"
      c.into distDir
    }
  }
}
