package com.spellsource.tasks

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileSystemOperations
import javax.inject.Inject

class GoPlugin implements Plugin<org.gradle.api.Project> {
  private final FileSystemOperations fs

  @Inject
  GoPlugin(FileSystemOperations fs) {
    this.fs = fs
  }

  @Override
  void apply(Project project) {
    def gooses = ["windows", "linux", "darwin"]
    def goarches = ["amd64", "arm64"]
    def ext = project.extensions.create('go', GoPluginExtension)
    def myGoos = Os.isFamily(Os.FAMILY_WINDOWS) ? "windows" : (Os.isFamily(Os.FAMILY_MAC) ? "darwin" : "linux")
    def myArch = Os.isArch("amd64") ? "amd64" : "arm64"
    def buildDirPath = project.layout.buildDirectory.asFile.get().path
    def fileSystemOps = this.fs
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
                      t.outputs.file("$buildDirPath/$path")
                      t.goCommandLineArgs.set(['build', '-o', "$buildDirPath/$path", cmdFile.path])
                      t.environment.putAll(ext.environment.get())
                      t.environment.put("GOOS", goos)
                      t.environment.put("GOARCH", goarch)
                    }
                    if (goos == myGoos && goarch == myArch && !single) {
                      single = true
                      def binDir = new File("$buildDirPath/bin")
                      project.tasks.register("goBuildCmdAsSingle", DefaultTask) {
                        group('go')
                        dependsOn(task)
                        outputs.file(project.layout.buildDirectory.file("bins/this_cmd"))

                        doLast {
                          binDir.mkdirs()
                          fileSystemOps.copy {
                            from task.get().outputs.files.singleFile
                            into binDir
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
