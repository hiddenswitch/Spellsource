package com.spellsource.utils

import groovy.json.JsonSlurper
import org.gradle.api.Project
import org.gradle.api.file.CopySpec
import org.gradle.api.file.FileSystemOperations

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

  static Set<String> resolveWorkspaceDependencies(Project project, boolean includeSelf) {
    def slurper = new JsonSlurper()
    def parent = project.parent
    def parentPackage = slurper.parse(project.file("${parent.projectDir}/package.json"))

    // Map of Gradle project names to their slurped package.json object
    def packages = (Map<String, Object>) parentPackage.workspaces.collectEntries({
      def pkg = project.file("$parent.projectDir/$it/package.json")
      return [parent.subprojects.find({ p -> it.startsWith(p.name) })?.name, pkg.exists() ? slurper.parse(pkg) : null]
    })

    def dependencies = new HashSet<String>([project.name])
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
    return dependencies
  }

  static def createDist(FileSystemOperations fs, File parentProjectDir, File projectDir, Set<String> dependencies, String distDir) {
    def ignores = gitignoreToList(new File(parentProjectDir, ".gitignore")) + gitignoreToList(new File(projectDir, ".gitignore"))
    ignores.add 'build.gradle'
    ignores.add '**/csharp'
    ignores.add 'docker/'
    ignores.add '**/node_modules'
    ignores.add '**/*Dockerfile'

    dependencies.forEach { p ->
      fs.copy { CopySpec c ->
        c.from new File(parentProjectDir, p)
        c.into "$distDir/$p/"
        c.exclude ignores
      }
    }

    fs.copy { CopySpec c ->
      c.from new File(parentProjectDir, "yarn.lock"), new File(parentProjectDir, "package.json"), new File(parentProjectDir, "tsconfig.json"), new File(parentProjectDir, ".yarnrc.yml")
      c.into distDir
    }

    fs.copy { CopySpec c ->
      c.from new File(parentProjectDir, ".yarn")
      c.into "$distDir/.yarn/"
    }
  }

  static def createDist(Project project, String distDir, boolean includeSelf) {
    def dependencies = resolveWorkspaceDependencies(project, includeSelf)
    def ignores = gitignoreToList(project.file("${project.parent.projectDir}/.gitignore")) + gitignoreToList(project.file("${project.projectDir}/.gitignore"))
    ignores.add 'build.gradle'
    ignores.add '**/csharp'
    ignores.add 'docker/'
    ignores.add '**/node_modules'
    ignores.add '**/*Dockerfile'

    def parentDir = project.parent.projectDir
    dependencies.forEach { p ->
      project.copy { CopySpec c ->
        c.from "$parentDir/$p"
        c.into "$distDir/$p/"
        c.exclude ignores
      }
    }

    project.copy { CopySpec c ->
      c.from "$parentDir/yarn.lock", "$parentDir/package.json", "$parentDir/tsconfig.json", "$parentDir/.yarnrc.yml"
      c.into distDir
    }

    project.copy { CopySpec c ->
      c.from "$parentDir/.yarn"
      c.into "$distDir/.yarn/"
    }
  }
}
