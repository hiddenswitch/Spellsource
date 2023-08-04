package com.spellsource.tasks

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class GoPlugin implements Plugin<org.gradle.api.Project> {

  @Override
  void apply(Project project) {
    def gooses = ["windows", "linux", "darwin"]
    def goarches = ["amd64", "arm64"]
    def ext = project.extensions.create('go', GoPluginExtension)
    def myGoos = Os.isFamily(Os.FAMILY_WINDOWS) ? "windows" : (Os.isFamily(Os.FAMILY_MAC) ? "darwin" : "linux")
    def myArch = Os.isArch("amd64") ? "amd64" : "arm64"
    project.afterEvaluate {
      def single = false;
      def buildTasks =
              ext.cmd.files.collectMany { cmdFile ->
                gooses.collectMany { goos ->
                  goarches.collect { goarch ->
                    def baseName = cmdFile.name.replace(".go", "")
                    def task = project.tasks.register("goBuild_${baseName}_${goos}_${goarch}", GoTask) { GoTask t ->
                      t.group('go')
                      t.workingDir.set(ext.workingDir.get().asFile)

                      def path = "$goos/$goarch/${baseName}${goos == "windows" ? ".exe" : ""}"
                      t.source(ext.workingDir)
                      t.include("**/*.go")
                      t.include("**/go.mod")
                      t.include("**/go.sum")
                      t.outputs.file("$project.buildDir/$path")
                      t.goCommandLineArgs.set(['build', '-o', "$project.buildDir/$path", cmdFile.path])
                      t.environment.putAll(ext.environment.get())
                      t.environment.put("GOOS", goos)
                      t.environment.put("GOARCH", goarch)
                    }
                    if (goos == myGoos && goarch == myArch && !single) {
                      single = true
                      project.tasks.register("goBuildCmdAsSingle", DefaultTask) {
                        group('go')
                        dependsOn(task)
                        outputs.file(project.layout.buildDirectory.file("bins/this_cmd"))

                        doLast {
                          project.mkdir("$project.buildDir/bin/")
                          project.copy {
                            from task.get().outputs.files.singleFile
                            into "$project.buildDir/bin"
                            rename ".*", "this_cmd"
                          }
                        }
                      }
                    }
                    return task
                  }
                }
              }

      project.tasks.register("goBuildAllCmds", DefaultTask) {
        group('go')
        dependsOn buildTasks
      }

    }
  }
}
